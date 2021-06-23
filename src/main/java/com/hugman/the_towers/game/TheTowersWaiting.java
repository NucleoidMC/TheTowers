package com.hugman.the_towers.game;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hugman.the_towers.TheTowers;
import com.hugman.the_towers.config.TheTowersConfig;
import com.hugman.the_towers.game.map.TheTowersMap;
import com.hugman.the_towers.game.map.TheTowersMapGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.apache.commons.lang3.RandomStringUtils;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.plasmid.entity.FloatingText;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.TeamSelectionLobby;
import xyz.nucleoid.plasmid.game.event.AttackEntityListener;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDamageListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

			game.on(GameOpenListener.EVENT, waiting::open);

			game.on(PlayerAddListener.EVENT, waiting::addPlayer);

			game.on(RequestStartListener.EVENT, waiting::requestStart);

			game.on(PlayerDamageListener.EVENT, waiting::damagePlayer);
			game.on(PlayerDeathListener.EVENT, waiting::killPlayer);
			game.on(AttackEntityListener.EVENT, waiting::damageEntity);
		});
	}

	private void open() {
		Text[] GUIDE_LINES = {
				this.gameSpace.getGameConfig().getNameText().copy().formatted(Formatting.BOLD, Formatting.GOLD),
				new TranslatableText("text.the_towers.guide.craft_stuff").formatted(Formatting.YELLOW),
				new TranslatableText("text.the_towers.guide.jumping_into_pool").formatted(Formatting.YELLOW),
				new TranslatableText("text.the_towers.guide.protect_your_pool").formatted(Formatting.YELLOW),
		};

		Vec3d pos = this.map.getCenter().add(0.0D, 2.3D, 0.0D);
		this.gameSpace.getWorld().getChunk(new BlockPos(pos));
		FloatingText.spawn(this.gameSpace.getWorld(), pos, GUIDE_LINES, FloatingText.VerticalAlign.CENTER);
	}

	private StartResult requestStart() {
		ServerScoreboard scoreboard = gameSpace.getServer().getScoreboard();

		List<TheTowersTeam> teamList = new ArrayList<>();
		Map<GameTeam, TheTowersTeam> gameTeamsToEntries = new HashMap<>(this.config.getTeams().size());

		teamSelection.allocate((gameTeam, player) -> {
			// Get or create team
			TheTowersTeam team = gameTeamsToEntries.get(gameTeam);
			if (team == null) {
				team = new TheTowersTeam(scoreboard, gameTeam, this.config.getTeamHealth());
				gameTeamsToEntries.put(gameTeam, team);
				teamList.add(team);
			}
			scoreboard.addPlayerToTeam(player.getEntityName(), team.getScoreboardTeam());
			team.addParticipant(new TheTowersParticipant(PlayerRef.of(player), gameSpace));
		});

		TheTowersActive.open(this.gameSpace, this.map, this.config, teamList);
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
		BlockPos pos = new BlockPos(this.map.getCenter());
		ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
		this.gameSpace.getWorld().getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getEntityId());
		player.teleport(this.gameSpace.getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0F, 0.0F);
	}
}
