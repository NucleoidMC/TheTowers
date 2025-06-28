package com.hugman.the_towers.game;

import com.hugman.the_towers.config.TowersConfig;
import com.hugman.the_towers.map.TowersMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.api.game.*;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.common.team.TeamSelectionLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerAttackEntityEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.Set;

public record TowersWaiting(GameSpace gameSpace, ServerWorld world, TowersMap map, TowersConfig config,
                            TeamSelectionLobby teamSelection) {
    public static GameOpenProcedure open(GameOpenContext<TowersConfig> context) {
        var mapLoadResult = context.config().map().value().load(context);
        if (null == mapLoadResult) {
            throw new GameOpenException(Text.literal("Failed to load map"));
        }

        TowersMap map = TowersMap.build(context, mapLoadResult);
        return context.openWithWorld(map.worldConfig(), (activity, world) -> {

            TowersConfig config = context.config();
            GameWaitingLobby.addTo(activity, config.playerConfig());

            TeamSelectionLobby teamSelection = TeamSelectionLobby.addTo(activity, config.teamConfig());
            TowersWaiting waiting = new TowersWaiting(activity.getGameSpace(), world, map, context.config(), teamSelection);

            activity.setRule(GameRuleType.INTERACTION, EventResult.DENY);

            activity.listen(GameActivityEvents.ENABLE, waiting::enable);

            activity.listen(GamePlayerEvents.ACCEPT, waiting::offerPlayer);

            activity.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);

            activity.listen(PlayerDamageEvent.EVENT, (player, source, amount) -> EventResult.DENY);
            activity.listen(PlayerDeathEvent.EVENT, waiting::killPlayer);
            activity.listen(PlayerAttackEntityEvent.EVENT, (attacker, hand, attacked, hitResult) -> EventResult.DENY);
        });
    }

    private void enable() {
        var gameName = this.gameSpace.getMetadata().sourceConfig().value().name();
        if (gameName == null) gameName = Text.of("The Towers");
        Text[] GUIDE_LINES = {
                gameName.copy().formatted(Formatting.BOLD, Formatting.GOLD),
                Text.translatable("text.the_towers.guide.craft_stuff").formatted(Formatting.YELLOW),
                Text.translatable("text.the_towers.guide.jumping_into_pool").formatted(Formatting.YELLOW),
                Text.translatable("text.the_towers.guide.protect_your_pool").formatted(Formatting.YELLOW),
        };

        Vec3d pos = this.map.rules();
        this.world.getChunk(BlockPos.ofFloored(pos));
        //TODO
//        WorldHologram hologram = Holograms.create(this.world, pos, GUIDE_LINES);
//        hologram.setAlignment(AbstractHologram.VerticalAlign.TOP);
//        hologram.show();
    }

    private GameResult requestStart() {
        TowersActive.enable(this.gameSpace, this.world, this.map, this.config, this.teamSelection);
        return GameResult.ok();
    }

    private JoinAcceptorResult offerPlayer(JoinAcceptor acceptor) {
        return acceptor.teleport(this.world, this.map.spawn()).thenRun((players) -> {
            players.forEach((player) -> {
                player.changeGameMode(GameMode.ADVENTURE);
            });
        });
    }

    private EventResult killPlayer(ServerPlayerEntity player, DamageSource source) {
        player.setHealth(20.0f);
        this.tpPlayer(player);
        return EventResult.DENY;
    }

    private void tpPlayer(ServerPlayerEntity player) {
        var pos = this.map.spawn();
        player.teleport(this.world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, Set.of(), 0.0F, 0.0F, false);
    }
}
