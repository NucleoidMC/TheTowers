package com.hugman.the_towers.game;

import com.hugman.the_towers.config.TowersConfig;
import com.hugman.the_towers.game.map.TowersMap;
import com.hugman.the_towers.game.map.TowersMapGenerator;
import eu.pb4.holograms.api.Holograms;
import eu.pb4.holograms.api.holograms.AbstractHologram;
import eu.pb4.holograms.api.holograms.WorldHologram;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameResult;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.common.team.TeamSelectionLobby;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.player.PlayerAttackEntityEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public record TowersWaiting(GameSpace gameSpace, ServerWorld world, TowersMap map, TowersConfig config, TeamSelectionLobby teamSelection) {
	public static GameOpenProcedure open(GameOpenContext<TowersConfig> context) {
		TowersConfig config = context.config();
		TowersMap map = TowersMapGenerator.loadFromConfig(context.server(), config);

		RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
				.setGenerator(map.asGenerator(context.server()));

		return context.openWithWorld(worldConfig, (activity, world) -> {
			GameWaitingLobby.addTo(activity, config.playerConfig());

			TeamSelectionLobby teamSelection = TeamSelectionLobby.addTo(activity, config.teamConfig());
			TowersWaiting waiting = new TowersWaiting(activity.getGameSpace(), world, map, context.config(), teamSelection);

			activity.setRule(GameRuleType.INTERACTION, ActionResult.FAIL);

			activity.listen(GameActivityEvents.ENABLE, waiting::enable);

			activity.listen(GamePlayerEvents.OFFER, waiting::offerPlayer);

			activity.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);

			activity.listen(PlayerDamageEvent.EVENT, (player, source, amount) -> ActionResult.FAIL);
			activity.listen(PlayerDeathEvent.EVENT, waiting::killPlayer);
			activity.listen(PlayerAttackEntityEvent.EVENT, (attacker, hand, attacked, hitResult) -> ActionResult.FAIL);
		});
	}

	private void enable() {
		Text[] GUIDE_LINES = {
				this.gameSpace.getMetadata().sourceConfig().name().copy().formatted(Formatting.BOLD, Formatting.GOLD),
				new TranslatableText("text.the_towers.guide.craft_stuff").formatted(Formatting.YELLOW),
				new TranslatableText("text.the_towers.guide.jumping_into_pool").formatted(Formatting.YELLOW),
				new TranslatableText("text.the_towers.guide.protect_your_pool").formatted(Formatting.YELLOW),
		};

		Vec3d pos = this.map.rules();
		this.world.getChunk(new BlockPos(pos));
		WorldHologram hologram = Holograms.create(this.world, pos, GUIDE_LINES);
		hologram.setAlignment(AbstractHologram.VerticalAlign.TOP);
		hologram.show();
	}

	private GameResult requestStart() {
		TowersActive.enable(this.gameSpace, this.world, this.map, this.config, this.teamSelection);
		return GameResult.ok();
	}

	private PlayerOfferResult offerPlayer(PlayerOffer offer) {
		return offer.accept(this.world, this.map.spawn()).and(() -> {
			ServerPlayerEntity player = offer.player();
			player.changeGameMode(GameMode.ADVENTURE);
		});
	}

	private ActionResult killPlayer(ServerPlayerEntity player, DamageSource source) {
		player.setHealth(20.0f);
		this.tpPlayer(player);
		return ActionResult.FAIL;
	}

	private void tpPlayer(ServerPlayerEntity player) {
		BlockPos pos = new BlockPos(this.map.spawn());
		ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
		this.world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getId());
		player.teleport(this.world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0F, 0.0F);
	}
}
