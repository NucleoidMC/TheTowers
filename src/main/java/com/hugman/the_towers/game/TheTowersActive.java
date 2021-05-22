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
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.ItemScatterer;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameCloseReason;
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

	private final Object2ObjectMap<PlayerRef, TheTowersParticipant> participantMap = new Object2ObjectOpenHashMap<>();
	private final Object2ObjectMap<GameTeam, TheTowersTeam> teamMap = new Object2ObjectOpenHashMap<>();
	private final TheTowersSpawner spawnLogic;

	private long gameStartTick;
	private boolean hasEnded;
	private long gameCloseTick = -1L;

	private TheTowersActive(GameSpace gameSpace, TheTowersMap map, TheTowersConfig config) {
		this.gameSpace = gameSpace;
		this.config = config;
		this.gameMap = map;
		this.spawnLogic = new TheTowersSpawner(gameSpace, map);
	}

	public static void open(GameSpace gameSpace, TheTowersMap map, TheTowersConfig config, Multimap<GameTeam, ServerPlayerEntity> playerMap) {
		gameSpace.openGame(game -> {
			TheTowersActive active = new TheTowersActive(gameSpace, map, config);
			active.addPlayers(playerMap);

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
		this.participantMap.forEach((playerRef, participant) -> {
			ServerPlayerEntity player = playerRef.getEntity(world);
			if(player != null) {
				player.setGameMode(GameMode.SURVIVAL);
				this.resetPlayer(player);
				this.respawnPlayer(player);
			}
		});
		hasEnded = false;
	}

	private void tick() {
		ServerWorld world = this.gameSpace.getWorld();
		long time = world.getTime();

		if(!hasEnded) {
			this.participantMap.forEach((playerRef, participant) -> {
				ServerPlayerEntity player = playerRef.getEntity(world);
				TheTowersTeam playerTeam = getTeam(participant.getTeam());

				if(participant.isRespawning && player != null) {
					participant.ticksUntilRespawn--;
					player.sendMessage(new TranslatableText("text.the_towers.respawn_in", (int) (participant.ticksUntilRespawn / 20)).formatted(Formatting.YELLOW), true);
					if(participant.ticksUntilRespawn == 0) {
						player.setGameMode(GameMode.SURVIVAL);
						this.resetPlayer(player);
						this.respawnPlayer(player);
						participant.isRespawning = false;
					}
				}

				this.gameMap.getTeamRegions().forEach((team, teamRegion) -> {
					TheTowersTeam enemyTeam = this.teamMap.get(team);
					if(enemyTeam != playerTeam && player != null && playerTeam != null && enemyTeam != null) {
						if(teamRegion.getPool().contains(player.getBlockPos()) && player.interactionManager.isSurvivalLike()) {
							this.respawnPlayer(player);
							enemyTeam.removeHealth();
							if(this.config.canSteal()) {
								this.gameSpace.getPlayers().sendMessage(new TranslatableText("text.the_towers.point_stole", player.getName(), new LiteralText(team.getDisplay()).formatted(Formatting.BOLD, team.getFormatting())).formatted(Formatting.YELLOW));
								playerTeam.addHealth();
							}
							else {
								this.gameSpace.getPlayers().sendMessage(new TranslatableText("text.the_towers.point_scored", player.getName(), new LiteralText(team.getDisplay()).formatted(Formatting.BOLD, team.getFormatting())).formatted(Formatting.YELLOW));
							}
							this.gameSpace.getPlayers().sendSound(SoundEvents.ENTITY_BLAZE_HURT);
							this.checkWin();
						}
					}
				});
			});
		}

		// Game has finished
		if(time == gameCloseTick) {
			this.gameSpace.close(GameCloseReason.FINISHED);
		}
	}

	private void close() {
		for(ServerPlayerEntity player : this.gameSpace.getPlayers()) {
			player.setGameMode(GameMode.ADVENTURE);
		}
	}

	private void checkWin() {
		ServerWorld world = this.gameSpace.getWorld();

		teamMap.forEach((team, theTowersTeam) -> {
			if(theTowersTeam.getHealth() <= 0) {
				participantMap.forEach((playerRef, participant) -> {
					if(participant.getTeam() == team) {
						participantMap.remove(playerRef);
						ServerPlayerEntity player = playerRef.getEntity(world);
						if(player != null) {
							player.setGameMode(GameMode.SPECTATOR);
							this.resetPlayer(player);
						}
					}
				});
				teamMap.remove(team);
				this.gameSpace.getPlayers().sendMessage(new LiteralText("\n").append(new TranslatableText("text.the_towers.team_eliminated", new LiteralText(team.getDisplay()).formatted(Formatting.BOLD, team.getFormatting())).formatted(Formatting.GOLD)).append("\n"));
				this.gameSpace.getPlayers().sendSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST);
			}
			if(teamMap.size() == 1) {
				this.gameSpace.getPlayers().sendMessage(new LiteralText("\n").append(new TranslatableText("text.the_towers.team_won", new LiteralText(team.getDisplay()).formatted(Formatting.BOLD, team.getFormatting())).formatted(Formatting.GOLD)).append("\n"));
				this.gameSpace.getPlayers().sendSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
				this.hasEnded = true;
			}
		});

		if(teamMap.size() == 0) {
			this.gameSpace.getPlayers().sendMessage(new LiteralText("\n").append(new TranslatableText("text.the_towers.nobody_won").formatted(Formatting.GOLD)).append("\n"));
			this.hasEnded = true;
		}

		// Close game after 30 seconds.
		if(this.hasEnded) {
			this.gameCloseTick = world.getTime() + 600;
			participantMap.forEach((playerRef, participant) -> {
				ServerPlayerEntity player = playerRef.getEntity(world);
				if(player != null) {
					player.setGameMode(GameMode.SPECTATOR);
					this.resetPlayer(player);
				}
			});
		}
	}

	// GENERAL PLAYER MANAGEMENT
	private void addPlayers(Multimap<GameTeam, ServerPlayerEntity> players) {
		players.forEach((team, player) -> {
			TheTowersParticipant participant = new TheTowersParticipant(team);
			this.participantMap.put(PlayerRef.of(player), participant);
		});
		players.keys().forEach(team -> {
			TheTowersTeam theTowersTeam = new TheTowersTeam(this.config.getTeamHealth());
			this.teamMap.put(team, theTowersTeam);
		});
	}

	private void addPlayer(ServerPlayerEntity player) {
		if(!this.participantMap.containsKey(PlayerRef.of(player))) {
			player.setGameMode(GameMode.SPECTATOR);
			this.resetPlayer(player);
			this.respawnPlayer(player);
		}
	}

	public void respawnPlayer(ServerPlayerEntity player) {
		TheTowersParticipant participant = getParticipant(player);
		if(participant != null) {
			this.spawnLogic.spawnPlayerAtSpawn(player, participant.getTeam());
		}
		else {
			this.spawnLogic.spawnPlayerAtCenter(player);
		}
	}

	@Nullable
	public TheTowersParticipant getParticipant(PlayerEntity player) {
		return this.participantMap.get(PlayerRef.of(player));
	}

	@Nullable
	public TheTowersTeam getTeam(GameTeam team) {
		return this.teamMap.get(team);
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
		PlayerRef ref = PlayerRef.of(player);
		if(participantMap.containsKey(ref)) {
			TheTowersParticipant participant = participantMap.get(ref);
			participant.ticksUntilRespawn = this.config.getRespawnCooldown() * 20L;
			participant.isRespawning = true;
			player.setGameMode(GameMode.SPECTATOR);
			ItemScatterer.spawn(this.gameSpace.getWorld(), player.getBlockPos(), player.inventory);
			this.resetPlayer(player);
		}
		this.spawnLogic.spawnPlayerAtCenter(player);
		return ActionResult.FAIL;
	}
}
