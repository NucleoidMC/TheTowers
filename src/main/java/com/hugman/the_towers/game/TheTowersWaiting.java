package com.hugman.the_towers.game;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hugman.the_towers.config.TheTowersConfig;
import com.hugman.the_towers.game.map.TheTowersMap;
import com.hugman.the_towers.game.map.TheTowersMapGenerator;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.TeamSelectionLobby;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDamageListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

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
				.setDefaultGameMode(GameMode.SPECTATOR);

		return context.createOpenProcedure(worldConfig, game -> {
			GameWaitingLobby.applyTo(game, config.getPlayerConfig());

			TeamSelectionLobby teamSelection = TeamSelectionLobby.applyTo(game, config.getTeams());
			TheTowersWaiting waiting = new TheTowersWaiting(game.getSpace(), map, context.getConfig(), teamSelection);

			game.setRule(GameRule.INTERACTION, RuleResult.ALLOW);

			game.on(RequestStartListener.EVENT, waiting::requestStart);
			game.on(PlayerAddListener.EVENT, waiting::addPlayer);
			game.on(PlayerDamageListener.EVENT, waiting::damagePlayer);
			game.on(PlayerDeathListener.EVENT, waiting::killPlayer);
		});
	}

	private StartResult requestStart() {
		Multimap<GameTeam, ServerPlayerEntity> players = HashMultimap.create();
		this.teamSelection.allocate(players::put);

		TheTowersActive.open(this.gameSpace, this.map, this.config, players);
		return StartResult.OK;
	}

	private void addPlayer(ServerPlayerEntity player) {
		// TODO: spawn player
	}

	private ActionResult damagePlayer(ServerPlayerEntity player, DamageSource source, float amount) {
		return ActionResult.FAIL;
	}

	private ActionResult killPlayer(ServerPlayerEntity player, DamageSource source) {
		player.setHealth(20.0f);
		// TODO: respawn player
		return ActionResult.FAIL;
	}
}
