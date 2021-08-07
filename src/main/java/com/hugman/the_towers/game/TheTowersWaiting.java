package com.hugman.the_towers.game;

import com.hugman.the_towers.config.TheTowersConfig;
import com.hugman.the_towers.game.map.TheTowersMap;
import com.hugman.the_towers.game.map.TheTowersMapGenerator;
import eu.pb4.holograms.api.Holograms;
import eu.pb4.holograms.api.holograms.AbstractHologram;
import eu.pb4.holograms.api.holograms.WorldHologram;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.scoreboard.AbstractTeam;
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
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.game.common.team.TeamSelectionLobby;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.player.PlayerAttackEntityEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.List;

public record TheTowersWaiting(GameSpace gameSpace, ServerWorld world, TheTowersMap map, TheTowersConfig config, TeamSelectionLobby teamSelection, TeamManager teamManager) {
	public static GameOpenProcedure open(GameOpenContext<TheTowersConfig> context) {
		TheTowersConfig config = context.config();
		TheTowersMapGenerator generator = new TheTowersMapGenerator(config);
		TheTowersMap map = generator.build(context.server());

		RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
				.setGenerator(map.asGenerator(context.server()));

		return context.openWithWorld(worldConfig, (activity, world) -> {
			GameWaitingLobby.addTo(activity, config.getPlayerConfig());
			TeamManager teamManager = TeamManager.addTo(activity);

			TeamSelectionLobby teamSelection = TeamSelectionLobby.applyTo(activity, config.getTeams());
			TheTowersWaiting waiting = new TheTowersWaiting(activity.getGameSpace(), world, map, context.config(), teamSelection, teamManager);

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
				this.gameSpace.getSourceConfig().getName().copy().formatted(Formatting.BOLD, Formatting.GOLD),
				new TranslatableText("text.the_towers.guide.craft_stuff").formatted(Formatting.YELLOW),
				new TranslatableText("text.the_towers.guide.jumping_into_pool").formatted(Formatting.YELLOW),
				new TranslatableText("text.the_towers.guide.protect_your_pool").formatted(Formatting.YELLOW),
		};

		Vec3d pos = this.map.getCenter().add(0.0D, 2.3D, 0.0D);
		this.world.getChunk(new BlockPos(pos));
		WorldHologram hologram = Holograms.create(this.world, pos, GUIDE_LINES);
		hologram.setAlignment(AbstractHologram.VerticalAlign.CENTER);
	}

	private GameResult requestStart() {
		List<GameTeam> teams = this.config().getTeams();

		Object2ObjectMap<ServerPlayerEntity, TheTowersParticipant> participantMap = new Object2ObjectOpenHashMap<>();
		Object2ObjectMap<GameTeam, TheTowersTeam> teamMap = new Object2ObjectOpenHashMap<>();

		for(GameTeam team : teams) {
			this.teamManager.setFriendlyFire(team, false);
			this.teamManager.setCollisionRule(team, AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS);
		}

		teamSelection.allocate((gameTeam, player) -> {
			teamManager.addPlayerTo(player, gameTeam);
			participantMap.put(player, new TheTowersParticipant());

			// Get or create team
			TheTowersTeam team = teamMap.get(gameTeam);
			if(team == null) {
				teamMap.put(gameTeam, new TheTowersTeam(this.config.getTeamHealth()));
			}
		});

		TheTowersActive.enable(this.gameSpace, this.world, this.map, this.config, participantMap, teamMap, this.teamManager);
		return GameResult.ok();
	}

	private PlayerOfferResult offerPlayer(PlayerOffer offer) {
		return offer.accept(this.world, this.map.getCenter().add(0.5, 0.5, 0.5))
				.and(() -> {
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
		BlockPos pos = new BlockPos(this.map.getCenter());
		ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
		this.world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getId());
		player.teleport(this.world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0F, 0.0F);
	}
}
