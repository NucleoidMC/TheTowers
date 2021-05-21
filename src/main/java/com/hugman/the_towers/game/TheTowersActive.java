package com.hugman.the_towers.game;

import com.google.common.collect.Multimap;
import com.hugman.the_towers.config.TheTowersConfig;
import com.hugman.the_towers.game.map.TheTowersMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameCloseListener;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.OfferPlayerListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.PlayerRef;

public class TheTowersActive {
	public final GameSpace gameSpace;
	private final TheTowersConfig config;
	private final TheTowersMap gameMap;

	private final Object2ObjectMap<PlayerRef, TheTowersParticipant> participants = new Object2ObjectOpenHashMap<>();
	private final TheTowersSpawner spawnLogic;

	private long gameStartTick;

	private TheTowersActive(GameSpace gameSpace, TheTowersMap map, TheTowersConfig config) {
		this.gameSpace = gameSpace;
		this.config = config;
		this.gameMap = map;
		this.spawnLogic = new TheTowersSpawner(gameSpace);
	}

	public static void open(GameSpace gameSpace, TheTowersMap map, TheTowersConfig config, Multimap<GameTeam, ServerPlayerEntity> players) {
		gameSpace.openGame(game -> {
			TheTowersActive active = new TheTowersActive(gameSpace, map, config);
			active.addPlayers(players);

			game.setRule(GameRule.CRAFTING, RuleResult.ALLOW);
			game.setRule(GameRule.PORTALS, RuleResult.DENY);
			game.setRule(GameRule.PVP, RuleResult.ALLOW);
			game.setRule(GameRule.HUNGER, RuleResult.ALLOW);
			game.setRule(GameRule.FALL_DAMAGE, RuleResult.ALLOW);
			game.setRule(GameRule.INTERACTION, RuleResult.ALLOW);
			game.setRule(GameRule.BLOCK_DROPS, RuleResult.ALLOW);
			game.setRule(GameRule.THROW_ITEMS, RuleResult.ALLOW);

			game.on(GameOpenListener.EVENT, active::open);
			game.on(GameCloseListener.EVENT, active::close);

			game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());
			game.on(PlayerAddListener.EVENT, active::addPlayer);

			game.on(GameTickListener.EVENT, active::tick);

			game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
		});
	}

	// GENERAL GAME MANAGEMENT
	private void open() {
		ServerWorld world = this.gameSpace.getWorld();
		this.gameStartTick = world.getTime();
		this.participants.forEach((playerRef, participant) -> {
			ServerPlayerEntity player = playerRef.getEntity(world);
			if(player != null) {
				player.setGameMode(GameMode.SURVIVAL);
				this.resetPlayer(player);
				this.respawnPlayer(player);
			}
		});
	}

	private void tick() {
		ServerWorld world = this.gameSpace.getWorld();
		long time = world.getTime();

		this.gameMap.getTeamRegions().forEach((team, teamRegion) -> this.participants.forEach((playerRef, participant) -> {
			if(team != participant.getTeam()) {
				ServerPlayerEntity player = playerRef.getEntity(world);
				if(player != null) {
					if(teamRegion.getPool().contains(player.getBlockPos()) && player.interactionManager.isSurvivalLike()) {
						// TODO: add a point to the team of the player and celebrate it :fireworks:
						this.respawnPlayer(player);
					};
				}
			}
		}));
	}

	private void close() {
		for(ServerPlayerEntity player : this.gameSpace.getPlayers()) {
			player.setGameMode(GameMode.ADVENTURE);
		}
	}

	// GENERAL PLAYER MANAGEMENT
	private void addPlayers(Multimap<GameTeam, ServerPlayerEntity> players) {
		players.forEach((team, player) -> {
			TheTowersParticipant participant = new TheTowersParticipant(team);
			this.participants.put(PlayerRef.of(player), participant);
		});
	}

	private void addPlayer(ServerPlayerEntity player) {
		if(!this.participants.containsKey(PlayerRef.of(player))) {
			player.setGameMode(GameMode.SPECTATOR);
			this.resetPlayer(player);
			this.respawnPlayer(player);
		}
	}

	public void respawnPlayer(ServerPlayerEntity player) {
		TheTowersParticipant participant = getParticipant(player);
		if(participant != null) {
			BlockPos spawnPosition = new BlockPos(this.gameMap.getTeamRegion(participant.getTeam()).getSpawn().getCenter());
			this.spawnLogic.spawnPlayerAt(player, spawnPosition);
		}
		else {
			this.spawnLogic.spawnPlayerAtCenter(player);
		}
	}

	@Nullable
	public TheTowersParticipant getParticipant(PlayerEntity player) {
		return this.participants.get(PlayerRef.of(player));
	}

	public void resetPlayer(ServerPlayerEntity player) {
		this.clearPlayer(player);
		player.inventory.clear();
		player.getEnderChestInventory().clear();
		player.clearStatusEffects();
		player.getHungerManager().setFoodLevel(20);
		player.setExperienceLevel(0);
		player.setExperiencePoints(0);
		player.setHealth(player.getMaxHealth());
	}

	public void clearPlayer(ServerPlayerEntity player) {
		player.extinguish();
		player.fallDistance = 0.0F;
	}

	// GENERAL LISTENERS
	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		if(participants.containsKey(PlayerRef.of(player))) {
			this.resetPlayer(player);
			this.respawnPlayer(player);
		}
		else {
			this.spawnLogic.spawnPlayerAtCenter(player);
		}
		return ActionResult.FAIL;
	}
}
