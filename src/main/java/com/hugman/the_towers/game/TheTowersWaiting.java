package com.hugman.the_towers.game;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hugman.the_towers.config.TheTowersConfig;
import com.hugman.the_towers.game.map.TheTowersMap;
import com.hugman.the_towers.game.map.TheTowersMapGenerator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameMode;
import org.apache.commons.lang3.RandomStringUtils;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.TeamSelectionLobby;
import xyz.nucleoid.plasmid.game.event.AttackEntityListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDamageListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.PlayerRef;

public class TheTowersWaiting {
	private final GameSpace gameSpace;
	private final TheTowersMap map;
	private final TheTowersConfig config;

	private final TeamSelectionLobby teamSelection;

	private TheTowersWaiting(GameSpace gameSpace, TheTowersMap map, TheTowersConfig config, TeamSelectionLobby teamSelection) {
		this.gameSpace = gameSpace;
		this.map = map;
		this.config = config;
		this.teamSelection = teamSelection;
	}

	public static GameOpenProcedure open(GameOpenContext<TheTowersConfig> context) {
		TheTowersConfig config = context.getConfig();
		TheTowersMapGenerator generator = new TheTowersMapGenerator(config);
		TheTowersMap map = generator.build();

		BubbleWorldConfig worldConfig = new BubbleWorldConfig()
				.setGenerator(map.asGenerator(context.getServer()))
				.setDefaultGameMode(GameMode.ADVENTURE);

		return context.createOpenProcedure(worldConfig, game -> {
			GameWaitingLobby.applyTo(game, config.getPlayerConfig());

			TeamSelectionLobby teamSelection = TeamSelectionLobby.applyTo(game, config.getTeams());
			TheTowersWaiting waiting = new TheTowersWaiting(game.getSpace(), map, context.getConfig(), teamSelection);

			game.setRule(GameRule.INTERACTION, RuleResult.ALLOW);

			game.on(RequestStartListener.EVENT, waiting::requestStart);
			game.on(PlayerAddListener.EVENT, waiting::addPlayer);
			game.on(PlayerDamageListener.EVENT, waiting::damagePlayer);
			game.on(PlayerDeathListener.EVENT, waiting::killPlayer);
			game.on(AttackEntityListener.EVENT, waiting::damageEntity);
		});
	}

	private StartResult requestStart() {
		ServerScoreboard scoreboard = gameSpace.getServer().getScoreboard();

		Multimap<GameTeam, ServerPlayerEntity> playerMap = HashMultimap.create();
		this.teamSelection.allocate(playerMap::put);

		Multimap<TheTowersTeam, TheTowersParticipant> participantMap = HashMultimap.create();
		playerMap.keys().forEach(gameTeam -> {
			Team scoreboardTeam = scoreboard.addTeam(RandomStringUtils.randomAlphabetic(16));
			TheTowersTeam team = new TheTowersTeam(scoreboardTeam, gameTeam, this.config.getTeamHealth());

			playerMap.get(gameTeam).forEach(player -> {
				scoreboard.addPlayerToTeam(player.getEntityName(), scoreboardTeam);
				participantMap.put(team, new TheTowersParticipant(PlayerRef.of(player), gameSpace));
			});
		});

		TheTowersActive.open(this.gameSpace, this.map, this.config, participantMap);
		return StartResult.OK;
	}

	private void addPlayer(ServerPlayerEntity player) {
		this.tpPlayer(player);
	}

	private ActionResult damagePlayer(ServerPlayerEntity player, DamageSource source, float amount) {
		return ActionResult.FAIL;
	}

	private ActionResult killPlayer(ServerPlayerEntity player, DamageSource source) {
		player.setHealth(20.0f);
		this.tpPlayer(player);
		return ActionResult.FAIL;
	}

	private ActionResult damageEntity(ServerPlayerEntity entity, Hand hand, Entity entity1, EntityHitResult entityHitResult) {
		return ActionResult.FAIL;
	}

	private void tpPlayer(ServerPlayerEntity player) {
		BlockPos pos = this.map.getCenter();
		ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
		this.gameSpace.getWorld().getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getEntityId());
		player.teleport(this.gameSpace.getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0F, 0.0F);
	}
}
