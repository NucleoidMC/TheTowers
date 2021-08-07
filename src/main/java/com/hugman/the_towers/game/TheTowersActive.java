package com.hugman.the_towers.game;

import com.hugman.the_towers.config.TheTowersConfig;
import com.hugman.the_towers.game.map.TheTowersMap;
import com.hugman.the_towers.game.map.TheTowersTeamRegion;
import com.hugman.the_towers.util.FormattingUtil;
import eu.pb4.holograms.api.Holograms;
import eu.pb4.holograms.api.holograms.AbstractHologram;
import eu.pb4.holograms.api.holograms.WorldHologram;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameMode;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;
import xyz.nucleoid.stimuli.event.item.ItemThrowEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class TheTowersActive {
	public final GameSpace gameSpace;
	public final ServerWorld world;
	private final TheTowersConfig config;
	private final TheTowersMap gameMap;

	private final Object2ObjectMap<ServerPlayerEntity, TheTowersParticipant> participantMap;
	private final Object2ObjectMap<GameTeam, TheTowersTeam> teamMap;
	private final TeamManager teamManager;

	private final TheTowersSidebar sidebar;

	private long gameStartTick;
	private boolean hasEnded = false;
	private long gameCloseTick = -1L;

	private TheTowersActive(GameSpace gameSpace, ServerWorld world, TheTowersMap map, TheTowersConfig config, GlobalWidgets widgets, Object2ObjectMap<ServerPlayerEntity, TheTowersParticipant> participantMap, Object2ObjectMap<GameTeam, TheTowersTeam> teamMap, TeamManager teamManager) {
		this.gameSpace = gameSpace;
		this.world = world;
		this.config = config;
		this.gameMap = map;

		this.participantMap = participantMap;
		this.teamMap = teamMap;
		this.teamManager = teamManager;

		this.sidebar = TheTowersSidebar.create(widgets, gameSpace);
	}

	public static void enable(GameSpace gameSpace, ServerWorld world, TheTowersMap map, TheTowersConfig config, Object2ObjectMap<ServerPlayerEntity, TheTowersParticipant> participantMap, Object2ObjectMap<GameTeam, TheTowersTeam> teamMap, TeamManager teamManager) {
		gameSpace.setActivity(activity -> {
			GlobalWidgets widgets = GlobalWidgets.addTo(activity);
			TheTowersActive active = new TheTowersActive(gameSpace, world, map, config, widgets, participantMap, teamMap, teamManager);

			activity.allow(GameRuleType.CRAFTING);
			activity.deny(GameRuleType.PORTALS);
			activity.allow(GameRuleType.PVP);
			activity.allow(GameRuleType.HUNGER);
			activity.allow(GameRuleType.FALL_DAMAGE);
			activity.allow(GameRuleType.INTERACTION);
			activity.allow(GameRuleType.BLOCK_DROPS);
			activity.allow(GameRuleType.THROW_ITEMS);

			activity.listen(GameActivityEvents.ENABLE, active::enable);
			activity.listen(GameActivityEvents.TICK, active::tick);

			activity.listen(GamePlayerEvents.OFFER, active::offerPlayer);

			activity.listen(PlayerDeathEvent.EVENT, active::killPlayer);
			activity.listen(BlockBreakEvent.EVENT, active::breakBlock);
			activity.listen(BlockPlaceEvent.BEFORE, active::placeBlock);
			activity.listen(ItemThrowEvent.EVENT, active::dropItem);
		});
	}

	// GENERAL GAME MANAGEMENT
	private void enable() {
		this.gameStartTick = world.getTime();
		this.teamMap.keySet().forEach(gameTeam -> {
			this.teamManager.playersIn(gameTeam).forEach(player -> {
				if(player != null) {
					player.changeGameMode(GameMode.SURVIVAL);
					this.resetPlayer(player);
					this.spawnPlayerAtTheirSpawn(player);
					this.resetPlayerInventory(player);
				}
			});
			WorldHologram hologram = Holograms.create(this.world, gameMap.getTeamRegion(gameTeam).getPool().centerTop().add(0.0D, 0.5D, 0.0D), new TranslatableText("text.the_towers.pool", gameTeam.display()).formatted(gameTeam.formatting()));
			hologram.setAlignment(AbstractHologram.VerticalAlign.CENTER);
		});
	}

	private void tick() {
		long worldTime = world.getTime();
		long gameTime = world.getTime() - gameStartTick;

		if(gameTime % 20 == 0) {
			this.sidebar.update(gameTime, this.teamMap, this.config.getTeamHealth());
		}
		if(!hasEnded) {
			this.teamMap.keySet().forEach(gameTeam -> {
				TheTowersTeam team = this.teamMap.get(gameTeam);
				BlockBounds pool = this.gameMap.getTeamRegion(gameTeam).getPool();
				if(worldTime % 60 == 0) {
					pool.iterator().forEachRemaining(pos -> world.spawnParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 2, 0.25D, 0.0D, 0.25D, 0.0D));
				}
				this.teamManager.playersIn(gameTeam).forEach(player -> {
					TheTowersParticipant participant = this.participantMap.get(player);
					if(player != null) {
						if(participant.isRespawning && team.health > 0) {
							participant.ticksUntilRespawn--;
							player.sendMessage(new TranslatableText("text.the_towers.respawn_in", (int) (participant.ticksUntilRespawn / 20)).formatted(Formatting.YELLOW), true);
							if(participant.ticksUntilRespawn == 0) {
								player.changeGameMode(GameMode.SURVIVAL);
								this.resetPlayer(player);
								this.resetPlayerInventory(player);
								this.spawnPlayerAtTheirSpawn(player);
								participant.isRespawning = false;
							}
						}

						this.teamMap.keySet().forEach(enemyGameTeam -> {
							TheTowersTeam enemyTeam = this.teamMap.get(gameTeam);
							if(team != enemyTeam) {
								TheTowersTeamRegion enemyRegion = this.gameMap.getTeamRegion(enemyGameTeam);
								if(enemyRegion.getPool().contains(player.getBlockPos()) && player.interactionManager.isSurvivalLike()) {
									this.spawnPlayerAtTheirSpawn(player);
									enemyTeam.health--;
									if(this.config.canSteal()) {
										Text msg = FormattingUtil.format(FormattingUtil.HEALTH_PREFIX, FormattingUtil.GENERAL_STYLE, new TranslatableText("text.the_towers.health_stole", player.getName(), enemyGameTeam.display()));
										this.gameSpace.getPlayers().sendMessage(msg);
										team.health++;
									}
									else {
										Text msg = FormattingUtil.format(FormattingUtil.HEALTH_PREFIX, FormattingUtil.GENERAL_STYLE, new TranslatableText("text.the_towers.health_removed", player.getName(), enemyGameTeam.display()));
										this.gameSpace.getPlayers().sendMessage(msg);
									}
									this.gameSpace.getPlayers().playSound(SoundEvents.ENTITY_BLAZE_HURT);
									this.checkWin();
								}
							}
						});
					}
				});
			});
		}

		// Game has finished
		if(worldTime == gameCloseTick) {
			this.gameSpace.close(GameCloseReason.FINISHED);
		}
	}

	private void checkWin() {
		long aliveCount = this.teamMap.values().stream().filter(team -> team.health > 0).count();
		if(aliveCount == 0) {
			Text msg = FormattingUtil.format(FormattingUtil.GENERAL_PREFIX, FormattingUtil.GENERAL_STYLE, new TranslatableText("text.the_towers.nobody_won"));
			this.gameSpace.getPlayers().sendMessage(new LiteralText("\n").append(msg).append("\n"));
			this.hasEnded = true;
		}
		this.teamMap.forEach((gameTeam, team) -> {
			if(team.health <= 0) {
				this.teamManager.playersIn(gameTeam).forEach(player -> {
					if(player != null) {
						player.changeGameMode(GameMode.SPECTATOR);
						this.resetPlayer(player);
					}
				});
				Text msg = FormattingUtil.format(FormattingUtil.X_PREFIX, FormattingUtil.GENERAL_STYLE, new TranslatableText("text.the_towers.team_eliminated", gameTeam.display()));
				this.gameSpace.getPlayers().sendMessage(new LiteralText("\n").append(msg).append("\n"));
				this.gameSpace.getPlayers().playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST);
			}

			if(aliveCount == 1) {
				Text msg = FormattingUtil.format(FormattingUtil.STAR_PREFIX, FormattingUtil.GENERAL_STYLE, new TranslatableText("text.the_towers.team_won", gameTeam.display()));
				this.gameSpace.getPlayers().sendMessage(new LiteralText("\n").append(msg).append("\n"));
				this.gameSpace.getPlayers().playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
				this.hasEnded = true;
			}
		});

		// Close game after 30 seconds.
		if(this.hasEnded) {
			this.gameCloseTick = world.getTime() + 600;
			this.participantMap.keySet().forEach(player -> {
				player.changeGameMode(GameMode.SPECTATOR);
				this.resetPlayer(player);
			});
		}
	}

	// GENERAL PLAYER MANAGEMENT
	private PlayerOfferResult offerPlayer(PlayerOffer offer) {
		ServerPlayerEntity player = offer.player();

		GameTeam gameTeam = this.teamManager.teamFor(player);
		TheTowersTeam theTowersTeam = teamMap.get(gameTeam);

		if(theTowersTeam != null) {
			if(theTowersTeam.health > 0) {
				return offer.accept(this.world, this.gameMap.getTeamRegion(gameTeam).getSpawn().center()).and(() -> {
					player.changeGameMode(GameMode.SURVIVAL);
					this.resetPlayer(player);
					this.resetPlayerInventory(player);
					this.spawnPlayerAtTheirSpawn(player);
				});
			}
		}
		return offer.accept(this.world, this.gameMap.getTeamRegion(gameTeam).getSpawn().center()).and(() -> {
			player.changeGameMode(GameMode.SPECTATOR);
			this.resetPlayer(player);
			this.spawnPlayerAtCenter(player);
		});
	}

	public void resetPlayerInventory(ServerPlayerEntity player) {
		GameTeam gameTeam = this.teamManager.teamFor(player);
		if(gameTeam != null) {
			player.equipStack(EquipmentSlot.HEAD, ItemStackBuilder.of(gameTeam.dye(new ItemStack(Items.LEATHER_HELMET))).setUnbreakable().build());
			player.equipStack(EquipmentSlot.CHEST, ItemStackBuilder.of(gameTeam.dye(new ItemStack(Items.LEATHER_CHESTPLATE))).setUnbreakable().build());
			player.equipStack(EquipmentSlot.LEGS, ItemStackBuilder.of(gameTeam.dye(new ItemStack(Items.LEATHER_LEGGINGS))).addEnchantment(Enchantments.PROJECTILE_PROTECTION, 2).setUnbreakable().build());
			player.equipStack(EquipmentSlot.FEET, ItemStackBuilder.of(gameTeam.dye(new ItemStack(Items.LEATHER_BOOTS))).setUnbreakable().build());
			player.getInventory().insertStack(ItemStackBuilder.of(Items.WOODEN_SWORD).build());
			player.getInventory().insertStack(ItemStackBuilder.of(Items.WOODEN_PICKAXE).build());
			player.getInventory().insertStack(ItemStackBuilder.of(Items.BAKED_POTATO).setCount(6).build());
		}
	}

	public void resetPlayer(ServerPlayerEntity player) {
		this.clearPlayer(player);
		player.getInventory().clear();
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

	public void spawnPlayerAtCenter(ServerPlayerEntity player) {
		this.spawnPlayerAt(player, new BlockPos(this.gameMap.getCenter()), 0.0F, 0.0F);
	}

	public void spawnPlayerAtTheirSpawn(ServerPlayerEntity player) {
		TheTowersTeamRegion region = this.gameMap.getTeamRegion(this.teamManager.teamFor(player));
		BlockPos spawnPosition = new BlockPos(region.getSpawn().center());
		this.spawnPlayerAt(player, spawnPosition, region.getSpawnYaw(), region.getSpawnPitch());
	}

	public void spawnPlayerAt(ServerPlayerEntity player, BlockPos pos, float yaw, float pitch) {
		ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
		player.fallDistance = 0.0f;
		this.world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getId());
		player.teleport(this.world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, yaw, pitch);
	}

	// GENERAL LISTENERS
	private ActionResult killPlayer(ServerPlayerEntity player, DamageSource source) {
		TheTowersParticipant participant = this.participantMap.get(player);
		if(participant != null) {
			participant.ticksUntilRespawn = this.config.getRespawnCooldown() * 20L;
			participant.isRespawning = true;
			player.changeGameMode(GameMode.SPECTATOR);
			for(int i = 0; i < player.getInventory().size(); ++i) {
				ItemStack stack = player.getInventory().getStack(i);
				if(canStackBeDropped(stack)) {
					ItemScatterer.spawn(this.world, player.getBlockPos().getX(), player.getBlockPos().getY(), player.getBlockPos().getZ(), player.getInventory().getStack(i));
				}
			}
			Text msg = FormattingUtil.format(FormattingUtil.DEATH_PREFIX, FormattingUtil.DEATH_STYLE, source.getDeathMessage(player).shallowCopy());
			this.gameSpace.getPlayers().sendMessage(msg);

			this.resetPlayer(player);
		}
		this.spawnPlayerAtCenter(player);
		return ActionResult.FAIL;
	}

	private ActionResult placeBlock(ServerPlayerEntity playerEntity, ServerWorld world, BlockPos pos, BlockState state, ItemUsageContext itemUsageContext) {
		for(BlockBounds bounds : this.gameMap.getProtectedBounds()) {
			if(bounds.contains(pos)) {
				Text msg = FormattingUtil.format(FormattingUtil.GENERAL_PREFIX, FormattingUtil.WARNING_STYLE, new TranslatableText("text.the_towers.cannot_place"));
				playerEntity.sendMessage(msg, false);
				return ActionResult.FAIL;
			}
		}
		return ActionResult.SUCCESS;
	}

	private ActionResult breakBlock(ServerPlayerEntity playerEntity, ServerWorld world, BlockPos pos) {
		for(BlockBounds bounds : this.gameMap.getProtectedBounds()) {
			if(bounds.contains(pos)) {
				Text msg = FormattingUtil.format(FormattingUtil.GENERAL_PREFIX, FormattingUtil.WARNING_STYLE, new TranslatableText("text.the_towers.cannot_break"));
				playerEntity.sendMessage(msg, false);
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

	// UTILITY
	private boolean canStackBeDropped(ItemStack stack) {
		if(stack != null) {
			Item item = stack.getItem();
			return item != Items.LEATHER_HELMET && item != Items.LEATHER_CHESTPLATE && item != Items.LEATHER_LEGGINGS && item != Items.LEATHER_BOOTS;
		}
		return true;
	}
}
