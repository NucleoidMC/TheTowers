package fr.hugman.the_towers.game;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import fr.hugman.plasmid.api.game.attachment.PlasmidGameAttachments;
import fr.hugman.the_towers.config.TowersConfig;
import fr.hugman.the_towers.map.TowersMap;
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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Brightness;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public record TowersWaiting(
        GameSpace gameSpace,
        ServerLevel world,
        TowersMap map,
        TowersConfig config,
        TeamSelectionLobby teamSelection
) {
    public static GameOpenProcedure open(GameOpenContext<TowersConfig> context) {
        return context.open((activity) -> {
            TowersConfig config = context.config();

            var teams = config.teamConfig().get(context.server().overworld().getRandom());

            activity.getGameSpace().setAttachment(PlasmidGameAttachments.TEAM_LIST, teams);

            var mapLoadResult = config.map().value().load(activity, config);
            if (null == mapLoadResult) {
                throw new GameOpenException(Component.literal("Failed to load map"));  //TODO: translate
            }

            TowersMap map = TowersMap.build(activity, mapLoadResult);
            ServerLevel world = activity.getGameSpace().getWorlds().add(map.worldConfig());

            GameWaitingLobby.addTo(activity, config.playerConfig());

            TeamSelectionLobby teamSelection = TeamSelectionLobby.addTo(activity, teams);
            TowersWaiting waiting = new TowersWaiting(activity.getGameSpace(), world, map, config, teamSelection);

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
        this.displayRules();
    }

    private void displayRules() {
        var gameName = this.gameSpace.getMetadata().sourceConfig().value().shortName();
        if (gameName == null) gameName = Component.translatable("game.the_towers");
        Component guideLines = gameName.copy().withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD).append("\n")
                .append(Component.translatable("text.the_towers.guide.craft_stuff").withStyle(ChatFormatting.YELLOW)).append("\n")
                .append(Component.translatable("text.the_towers.guide.jumping_into_pool").withStyle(ChatFormatting.YELLOW)).append("\n")
                .append(Component.translatable("text.the_towers.guide.protect_your_pool").withStyle(ChatFormatting.YELLOW));
        Vec3 pos = this.map.rules();
        this.world.getChunk(BlockPos.containing(pos));

        TextDisplayElement element = new TextDisplayElement(guideLines);
        element.setBillboardMode(Display.BillboardConstraints.VERTICAL);
        element.setBrightness(Brightness.FULL_BRIGHT);
        ElementHolder holder = new ElementHolder();
        holder.addElement(element);

        ChunkAttachment.of(holder, world, pos);
    }

    private GameResult requestStart() {
        TowersActive.enable(this.gameSpace, this.world, this.map, this.config, this.teamSelection);
        return GameResult.ok();
    }

    private JoinAcceptorResult offerPlayer(JoinAcceptor acceptor) {
        return acceptor.teleport(this.world, this.map.spawn()).thenRun((players) -> {
            players.forEach((player) -> {
                player.setGameMode(GameType.ADVENTURE);
            });
        });
    }

    private EventResult killPlayer(ServerPlayer player, DamageSource source) {
        player.setHealth(20.0f);
        this.tpPlayer(player);
        return EventResult.DENY;
    }

    private void tpPlayer(ServerPlayer player) {
        var pos = this.map.spawn();
        player.teleportTo(this.world, pos.x() + 0.5, pos.y() + 0.5, pos.z() + 0.5, Set.of(), 0.0F, 0.0F, false);
    }
}
