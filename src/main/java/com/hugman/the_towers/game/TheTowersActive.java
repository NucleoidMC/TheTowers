package com.hugman.the_towers.game;

import com.google.common.collect.Multimap;
import com.hugman.the_towers.config.TheTowersConfig;
import com.hugman.the_towers.game.map.TheTowersMap;
import com.hugman.the_towers.game.map.TheTowersTeamRegion;
import com.hugman.the_towers.util.FormattingUtil;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.BlockBounds;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

import java.util.List;

public class TheTowersActive {
	public final GameSpace gameSpace;
	public final TheTowersConfig config;
	private final TheTowersMap gameMap;

	private final Multimap<TheTowersTeam, TheTowersParticipant> participantMap;

	private final TheTowersSpawner spawner;
	private final TheTowersSidebar sidebar;

	private long gameStartTick;
	private boolean hasEnded;
	private long gameCloseTick = -1L;

	private TheTowersActive(GameSpace gameSpace, TheTowersMap map, TheTowersConfig config, GlobalWidgets widgets, Multimap<TheTowersTeam, TheTowersParticipant> participantMap) {
		this.gameSpace = gameSpace;
		this.config = config;
		this.gameMap = map;
		this.spawner = new TheTowersSpawner(gameSpace, map);
		this.sidebar = TheTowersSidebar.create(widgets, this);
		this.participantMap = participantMap;
	}

	public static void open(GameSpace gameSpace, TheTowersMap map, TheTowersConfig config, Multimap<TheTowersTeam, TheTowersParticipant> participantMap) {
		gameSpace.openGame(game -> {
			GlobalWidgets widgets = new GlobalWidgets(game);
			TheTowersActive active = new TheTowersActive(gameSpace, map, config, widgets, participantMap);

			game.setRule(GameRule.CRAFTING, RuleResult.ALLOW);
			game.setRule(GameRule.PORTALS, RuleResult.DENY);
			game.setRule(GameRule.PVP, RuleResult.ALLOW);
			game.setRule(GameRule.HUNGER, RuleResult.ALLOW);
			game.setRule(GameRule.FALL_DAMAGE, RuleResult.ALLOW);
			game.setRule(GameRule.INTERACTION, RuleResult.ALLOW);
			game.setRule(GameRule.BLOCK_DROPS, RuleResult.ALLOW);
			game.setRule(GameRule.THROW_ITEMS, RuleResult.ALLOW);

			game.on(GameOpenListener.EVENT, active::open);
			game.on(GameTickListener.EVENT, active::tick);
			game.on(GameCloseListener.EVENT, active::close);

			game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());
			game.on(PlayerAddListener.EVENT, active::addPlayer);
			game.on(PlayerRemoveListener.EVENT, active::removePlayer);

			game.on(PlayerDeathListener.EVENT, active::killPlayer);
			game.on(BreakBlockListener.EVENT, active::breakBlock);
			game.on(PlaceBlockListener.EVENT, active::placeBlock);
			game.on(DropItemListener.EVENT, active::dropItem);
		});
	}

	// GENERAL GAME MANAGEMENT
	private void open() {
		ServerWorld world = this.gameSpace.getWorld();
		this.gameStartTick = world.getTime();
		this.participantMap.forEach((team, participant) -> {
			ServerPlayerEntity player = participant.getPlayer();
			if(player != null) {
				player.setGameMode(GameMode.SURVIVAL);
				this.resetPlayer(player);
				this.respawnPlayer(player);
				this.resetPlayerInventory(player);
			}
		});
		hasEnded = false;
		this.sidebar.update();
	}

	private void tick() {
		ServerWorld world = this.gameSpace.getWorld();
		long time = world.getTime();

		if(!hasEnded) {
			this.participantMap.forEach((team, participant) -> {
				ServerPlayerEntity player = participant.getPlayer();
				if(player != null) {
					if(participant.isRespawning) {
						participant.ticksUntilRespawn--;
						player.sendMessage(new TranslatableText("text.the_towers.respawn_in", (int) (participant.ticksUntilRespawn / 20)).formatted(Formatting.YELLOW), true);
						if(participant.ticksUntilRespawn == 0) {
							player.setGameMode(GameMode.SURVIVAL);
							this.resetPlayer(player);
							this.respawnPlayer(player);
							this.resetPlayerInventory(player);
							participant.isRespawning = false;
						}
					}

					this.participantMap.keys().forEach(enemyTeam -> {
						if(true) {
							TheTowersTeamRegion enemyRegion = this.gameMap.getTeamRegion(enemyTeam);
							if(enemyRegion.getPool().contains(player.getBlockPos()) && player.interactionManager.isSurvivalLike()) {
								this.respawnPlayer(player);
								enemyTeam.health--;
								if(this.config.canSteal()) {
									this.gameSpace.getPlayers().sendMessage(new TranslatableText("text.the_towers.point_stole", player.getName(), enemyTeam.getName()).formatted(Formatting.YELLOW));
									team.health++;
								}
								else {
									this.gameSpace.getPlayers().sendMessage(new TranslatableText("text.the_towers.point_scored", player.getName(), enemyTeam.getName()).formatted(Formatting.YELLOW));
								}
								this.gameSpace.getPlayers().sendSound(SoundEvents.ENTITY_BLAZE_HURT);
								this.checkWin();
							}
						}
					});
				}
			});
		}

		// Game has finished
		if(time == gameCloseTick) {
			this.gameSpace.close(GameCloseReason.FINISHED);
		}
	}

	private void close() {
		this.sidebar.end();
		this.participantMap.keys().forEach(team -> this.gameSpace.getWorld().getScoreboard().removeTeam(team.getScoreboardTeam()));
	}

	private void checkWin() {
		ServerWorld world = this.gameSpace.getWorld();

		this.sidebar.update();
		this.participantMap.keys().forEach(team -> {
			if(team.health <= 0) {
				this.participantMap.get(team).forEach(participant -> {
					ServerPlayerEntity player = participant.getPlayer();
					if(player != null) {
						player.setGameMode(GameMode.SPECTATOR);
						this.resetPlayer(player);
					}
				});
				this.gameSpace.getPlayers().sendMessage(new LiteralText("\n").append(new TranslatableText("text.the_towers.team_eliminated", team.getName()).formatted(Formatting.GOLD)).append("\n"));
				this.gameSpace.getPlayers().sendSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST);
			}
			if(participantMap.keys().size() == 1) {
				this.gameSpace.getPlayers().sendMessage(new LiteralText("\n").append(new TranslatableText("text.the_towers.team_won", team.getName()).formatted(Formatting.GOLD)).append("\n"));
				this.gameSpace.getPlayers().sendSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
				this.hasEnded = true;
			}
		});

		if(participantMap.keys().size() == 0) {
			Text msg = FormattingUtil.format(FormattingUtil.GENERAL_PREFIX, FormattingUtil.GENERAL_STYLE, new TranslatableText("text.the_towers.nobody_won"));
			this.gameSpace.getPlayers().sendMessage(new LiteralText("\n").append(new TranslatableText("text.the_towers.nobody_won").formatted(Formatting.GOLD)).append("\n"));
			this.hasEnded = true;
		}

		// Close game after 30 seconds.
		if(this.hasEnded) {
			this.gameCloseTick = world.getTime() + 600;
			participantMap.values().forEach(participant -> {
				ServerPlayerEntity player = participant.getPlayer();
				if(player != null) {
					player.setGameMode(GameMode.SPECTATOR);
					this.resetPlayer(player);
				}
			});
		}
	}

	// GENERAL PLAYER MANAGEMENT
	private void addPlayer(ServerPlayerEntity player) {
		ServerScoreboard scoreboard = this.gameSpace.getWorld().getScoreboard();
		TheTowersParticipant participant = getParticipant(player);
		if(participant != null) {
			TheTowersTeam team = getTeam(participant);
			if(team != null) {
				if(team.health > 0) {
					scoreboard.addPlayerToTeam(player.getEntityName(), team.getScoreboardTeam());
					player.setGameMode(GameMode.SURVIVAL);
					this.resetPlayer(player);
					this.respawnPlayer(player);
					this.resetPlayerInventory(player);
					return;
				}
			}
		}
		player.setGameMode(GameMode.SPECTATOR);
		this.resetPlayer(player);
		this.spawner.spawnPlayerAtCenter(player);
	}

	public void respawnPlayer(ServerPlayerEntity player) {
		TheTowersParticipant participant = getParticipant(player);
		if(participant != null) {
			this.spawner.spawnPlayerAtSpawn(player, this.getTeam(participant));
		}
		else {
			this.spawner.spawnPlayerAtCenter(player);
		}
	}

	private void removePlayer(ServerPlayerEntity player) {
		ServerScoreboard scoreboard = this.gameSpace.getWorld().getScoreboard();
		TheTowersParticipant participant = getParticipant(player);
		if(participant != null) {
			TheTowersTeam team = getTeam(participant);
			if(team != null) {
				scoreboard.removePlayerFromTeam(player.getEntityName(), team.getScoreboardTeam());
			}
		}
	}

	public Multimap<TheTowersTeam, TheTowersParticipant> getParticipantMap() {
		return participantMap;
	}

	public void resetPlayerInventory(ServerPlayerEntity player) {
		TheTowersParticipant participant = getParticipant(player);
		if(participant != null) {
			TheTowersTeam team = getTeam(participant);
			if(team != null) {
				player.equipStack(EquipmentSlot.HEAD, ItemStackBuilder.of(team.dye(Items.LEATHER_HELMET)).setUnbreakable().build());
				player.equipStack(EquipmentSlot.CHEST, ItemStackBuilder.of(team.dye(Items.LEATHER_CHESTPLATE)).setUnbreakable().build());
				player.equipStack(EquipmentSlot.LEGS, ItemStackBuilder.of(team.dye(Items.LEATHER_LEGGINGS)).addEnchantment(Enchantments.PROJECTILE_PROTECTION, 2).setUnbreakable().build());
				player.equipStack(EquipmentSlot.FEET, ItemStackBuilder.of(team.dye(Items.LEATHER_BOOTS)).setUnbreakable().build());
				player.inventory.insertStack(ItemStackBuilder.of(Items.BAKED_POTATO).setCount(6).build());
			}
		}
	}

	@Nullable
	public TheTowersParticipant getParticipant(ServerPlayerEntity player) {
		for(TheTowersParticipant participant : this.participantMap.values()) {
			if(participant.getPlayer() == player) return participant;
		}
		return null;
	}

	@Nullable
	public TheTowersTeam getTeam(TheTowersParticipant participant) {
		for(TheTowersTeam theTowersTeam : this.participantMap.keys()) {
			if(this.participantMap.get(theTowersTeam).contains(participant)) return theTowersTeam;
		}
		return null;
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
	private ActionResult killPlayer(ServerPlayerEntity player, DamageSource source) {
		TheTowersParticipant participant = getParticipant(player);
		if(participant != null) {
			participant.ticksUntilRespawn = this.config.getRespawnCooldown() * 20L;
			participant.isRespawning = true;
			player.setGameMode(GameMode.SPECTATOR);
			for(int i = 0; i < player.inventory.size(); ++i) {
				ItemStack stack = player.inventory.getStack(i);
				if(canStackBeDropped(stack)) {
					ItemScatterer.spawn(this.gameSpace.getWorld(), player.getBlockPos().getX(), player.getBlockPos().getY(), player.getBlockPos().getZ(), player.inventory.getStack(i));
				}
			}
			Text msg = FormattingUtil.format(FormattingUtil.DEATH_PREFIX, FormattingUtil.DEATH_STYLE, source.getDeathMessage(player).shallowCopy());
			this.gameSpace.getPlayers().sendMessage(msg);

			this.resetPlayer(player);
		}
		this.spawner.spawnPlayerAtCenter(player);
		return ActionResult.FAIL;
	}

	private ActionResult placeBlock(ServerPlayerEntity player, BlockPos pos, BlockState state, ItemUsageContext itemUsageContext) {
		for(BlockBounds bounds : this.gameMap.getProtectedBounds()) {
			if(bounds.contains(pos)) {
				Text msg = FormattingUtil.format(FormattingUtil.GENERAL_PREFIX, FormattingUtil.WARNING_STYLE, new TranslatableText("text.the_towers.cannot_place"));
				player.sendMessage(msg, false);
				return ActionResult.FAIL;
			}
		}
		return ActionResult.SUCCESS;
	}

	private ActionResult breakBlock(ServerPlayerEntity player, BlockPos pos) {
		for(BlockBounds bounds : this.gameMap.getProtectedBounds()) {
			if(bounds.contains(pos)) {
				Text msg = FormattingUtil.format(FormattingUtil.GENERAL_PREFIX, FormattingUtil.WARNING_STYLE, new TranslatableText("text.the_towers.cannot_break"));
				player.sendMessage(msg, false);
				return ActionResult.FAIL;
			}
		}
		return ActionResult.SUCCESS;
	}

	private ActionResult dropItem(PlayerEntity player, int i, ItemStack stack) {
		if(canStackBeDropped(stack)) {
			return ActionResult.SUCCESS;
		}
		else {
			Text msg = FormattingUtil.format(FormattingUtil.GENERAL_PREFIX, FormattingUtil.WARNING_STYLE, new TranslatableText("text.the_towers.cannot_drop_armor"));
			player.sendMessage(msg, false);
			return ActionResult.FAIL;
		}
	}

	private boolean canStackBeDropped(ItemStack stack) {
		if(stack != null) {
			Item item = stack.getItem();
			return item != Items.LEATHER_HELMET && item != Items.LEATHER_CHESTPLATE && item != Items.LEATHER_LEGGINGS && item != Items.LEATHER_BOOTS;
		}
		return true;
	}
}
