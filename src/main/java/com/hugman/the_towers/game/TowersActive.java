package com.hugman.the_towers.game;

import com.hugman.the_towers.TheTowers;
import com.hugman.the_towers.config.TowersConfig;
import com.hugman.the_towers.map.TeamRegion;
import com.hugman.the_towers.map.TowersMap;
import com.hugman.the_towers.util.FormattingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.PlayerLimiter;
import xyz.nucleoid.plasmid.api.game.common.team.*;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.api.util.ItemStackBuilder;
import xyz.nucleoid.plasmid.api.util.PlayerPos;
import xyz.nucleoid.plasmid.api.util.PlayerRef;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.block.BlockPlaceEvent;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
import xyz.nucleoid.stimuli.event.item.ItemThrowEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.Set;

public class TowersActive {
    private final GameSpace gameSpace;
    private final GameActivity activity;
    private final ServerWorld world;
    private final TowersConfig config;
    private final TowersMap map;

    private Object2ObjectMap<ServerPlayerEntity, TowersParticipant> participantMap;
    private Object2ObjectMap<GameTeam, TeamData> teamMap;
    private TeamManager teamManager;

    private final TowersSidebar sidebar;

    private long gameTick = 0;
    private long nextRefillTick;
    private boolean hasEnded = false;
    private long gameCloseTick = -1L;

    private TowersActive(GameSpace gameSpace, GameActivity activity, ServerWorld world, TowersMap map, TowersConfig config, TowersSidebar sidebar, TeamSelectionLobby teamSelection) {
        this.gameSpace = gameSpace;
        this.activity = activity;
        this.world = world;
        this.config = config;
        this.map = map;

        fillTeams(teamSelection);

        this.sidebar = sidebar;
    }

    private void fillTeams(TeamSelectionLobby teamSelection) {
        this.participantMap = new Object2ObjectOpenHashMap<>();
        this.teamMap = new Object2ObjectOpenHashMap<>();

        this.teamManager = TeamManager.addTo(this.activity);

        for (GameTeam team : this.config.teamConfig()) {
            team = team.withConfig(GameTeamConfig.builder(team.config())
                    .setFriendlyFire(false)
                    .setCollision(AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS)
                    .build());
            this.teamManager.addTeam(team);
            this.teamMap.put(team, new TeamData(this.config.maxHealth()));
        }

        teamSelection.allocate(this.gameSpace.getPlayers(), (gameTeam, player) -> {
            participantMap.put(player, new TowersParticipant());
            teamManager.addPlayerTo(player, gameTeam);
        });
    }

    public static void enable(GameSpace gameSpace, ServerWorld world, TowersMap map, TowersConfig config, TeamSelectionLobby teamSelection) {
        gameSpace.setActivity(activity -> {
            PlayerLimiter.addTo(activity, config.playerConfig().playerConfig());
            GlobalWidgets widgets = GlobalWidgets.addTo(activity);
            TowersSidebar sidebar = TowersSidebar.create(widgets, gameSpace);
            TowersActive active = new TowersActive(gameSpace, activity, world, map, config, sidebar, teamSelection);

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

            activity.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
            activity.listen(GamePlayerEvents.ACCEPT, active::accept);

            activity.listen(PlayerDeathEvent.EVENT, active::killPlayer);
            activity.listen(ItemThrowEvent.EVENT, active::dropItem);
            activity.listen(BlockPlaceEvent.BEFORE, active::placeBlock);
            activity.listen(BlockUseEvent.EVENT, active::useBlock);
            activity.listen(BlockBreakEvent.EVENT, active::breakBlock);
        });
    }

    // GENERAL GAME MANAGEMENT
    private void enable() {
        this.nextRefillTick = this.gameTick + this.config.refillCooldown();

        Text[] GUIDE_LINES = {
                Text.literal("+--------------------------------------+").formatted(Formatting.DARK_GRAY),
                this.gameSpace.getMetadata().sourceConfig().value().name().copy().formatted(Formatting.BOLD, Formatting.GOLD),
                Text.translatable("text.the_towers.guide.craft_stuff").formatted(Formatting.WHITE),
                Text.translatable("text.the_towers.guide.jumping_into_pool").formatted(Formatting.WHITE),
                Text.translatable("text.the_towers.guide.protect_your_pool").formatted(Formatting.WHITE),
                Text.literal("+--------------------------------------+").formatted(Formatting.DARK_GRAY),
        };

        for (Text text : GUIDE_LINES) {
            this.gameSpace.getPlayers().sendMessage(text);
        }
        this.teamMap.keySet().forEach(gameTeam -> {
            this.teamManager.playersIn(gameTeam.key()).forEach(player -> {
                if (player != null) {
                    player.changeGameMode(GameMode.SURVIVAL);
                    this.resetPlayer(player);
                    this.spawnPlayerAtTheirSpawn(player);
                    this.resetPlayerInventory(player);
                }
            });
            //TODO
            //WorldHologram hologram = Holograms.create(this.world, map.teamRegions().get(gameTeam.key()).pool().centerTop().add(0.0D, 0.5D, 0.0D), Text.translatable("text.the_towers.pool", gameTeam.config().name()).formatted(gameTeam.config().chatFormatting()));
            //hologram.setAlignment(AbstractHologram.VerticalAlign.CENTER);
            //hologram.show();
        });
    }

    private void tick() {
        long worldTime = world.getTime();
        this.gameTick++;

        if (this.gameTick == this.nextRefillTick) {
            //TODO: fix the refill method
            //this.refill();
            this.nextRefillTick = this.gameTick + this.config.refillCooldown();
        }

        if (!hasEnded) {
            this.map.generators().forEach(generator -> generator.tick(world, this.gameTick));
            this.teamMap.keySet().forEach(team -> {
                TeamData teamData = this.teamMap.get(team);
                BlockBounds pool = this.map.teamRegions().get(team.key()).pool();
                if (this.gameTick % 60 == 0) {
                    pool.iterator().forEachRemaining(pos -> world.spawnParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 2, 0.25D, 0.0D, 0.25D, 0.0D));
                }
                this.teamManager.playersIn(team.key()).forEach(player -> {
                    TowersParticipant participant = this.participantMap.get(player);
                    if (player != null) {
                        if (participant.ticksUntilRespawn >= 0 && teamData.health > 0) {
                            if ((participant.ticksUntilRespawn + 1) % 20 == 0) {
                                player.networkHandler.sendPacket(new TitleFadeS2CPacket(0, 90, 0));
                                player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("text.the_towers.respawn_in", (int) (participant.ticksUntilRespawn / 20 + 1)).formatted(Formatting.GOLD)));
                            }
                            if (participant.ticksUntilRespawn == 0) {
                                player.changeGameMode(GameMode.SURVIVAL);
                                player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("")));
                                this.resetPlayer(player);
                                this.resetPlayerInventory(player);
                                this.spawnPlayerAtTheirSpawn(player);
                            }
                            participant.ticksUntilRespawn--;
                        }

                        // Check for players in pools.
                        this.teamMap.keySet().forEach(enemyTeam -> {
                            TeamData enemyTeamData = this.teamMap.get(enemyTeam);
                            if (team != enemyTeam && enemyTeamData.health > 0) {
                                TeamRegion enemyRegion = this.map.teamRegions().get(enemyTeam.key());
                                if (enemyRegion.pool().contains(player.getBlockPos()) && player.interactionManager.isSurvivalLike()) {
                                    // The player is in an enemy's pool. They make them lose a point and steal them if the configuration allows it.
                                    this.spawnPlayerAtTheirSpawn(player);
                                    enemyTeamData.health--;
                                    if (this.config.healthStealth()) {
                                        Text msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.GENERAL_STYLE, Text.translatable("text.the_towers.health_stole", player.getName(), enemyTeam.config().name()));
                                        this.gameSpace.getPlayers().sendMessage(msg);
                                        teamData.health++;
                                    } else {
                                        Text msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.GENERAL_STYLE, Text.translatable("text.the_towers.health_removed", player.getName(), enemyTeam.config().name()));
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
            if (this.gameTick % 20 == 0) {
                this.sidebar.update(this.gameTick, this.nextRefillTick, this.teamMap);
            }
        }

        // Game has finished
        if (worldTime == gameCloseTick) {
            this.gameSpace.close(GameCloseReason.FINISHED);
        }
    }

    private void checkWin() {
        long aliveCount = this.teamMap.values().stream().filter(team -> team.health > 0).count();
        // No teamConfig are alive. Weird!
        if (aliveCount == 0) {
            Text msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.GENERAL_STYLE, Text.translatable("text.the_towers.nobody_won"));
            this.gameSpace.getPlayers().sendMessage(Text.literal("\n").append(msg).append("\n"));
            this.hasEnded = true;
        }
        this.teamMap.forEach((team, teamData) -> {
            if (teamData.health == 0) {
                // The selected team has not enough health to be alive. They are eliminated.
                teamData.health = -1;
                this.teamManager.playersIn(team.key()).forEach(player -> {
                    if (player != null) {
                        player.changeGameMode(GameMode.SPECTATOR);
                        this.resetPlayer(player);
                    }
                });
                Text msg = FormattingUtil.format(FormattingUtil.X_SYMBOL, FormattingUtil.GENERAL_STYLE, Text.translatable("text.the_towers.team_eliminated", team.config().name()));
                this.gameSpace.getPlayers().sendMessage(Text.literal("\n").append(msg).append("\n"));
                this.gameSpace.getPlayers().playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST);
            }
        });
        this.teamMap.forEach((gameTeam, team) -> {
            if (aliveCount == 1 && team.health > 0) {
                // The selected team is the only team left that is alive. They win.
                Text msg = FormattingUtil.format(FormattingUtil.STAR_SYMBOL, FormattingUtil.GENERAL_STYLE, Text.translatable("text.the_towers.team_won", gameTeam.config().name()));
                this.gameSpace.getPlayers().sendMessage(Text.literal("\n").append(msg).append("\n"));
                this.gameSpace.getPlayers().playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
                this.hasEnded = true;
            }
        });

        // Close game after 30 seconds.
        if (this.hasEnded) {
            this.gameCloseTick = world.getTime() + 600;
            this.participantMap.keySet().forEach(player -> {
                player.changeGameMode(GameMode.SPECTATOR);
                this.resetPlayer(player);
                this.sidebar.update(this.gameTick, this.nextRefillTick, this.teamMap);
            });
        }
    }

    // GENERAL PLAYER MANAGEMENT
    private JoinAcceptorResult accept(JoinAcceptor acceptor) {
        return acceptor.teleport(profile -> {
            GameTeamKey gameTeamKey = this.teamManager.teamFor(PlayerRef.of(profile));
            if (gameTeamKey instanceof GameTeamKey) {
                return new PlayerPos(this.world, this.map.teamRegions().get(gameTeamKey).spawn().center(), 0.0f, 0.0f);
            }
            return new PlayerPos(this.world, this.map.spawn(), 0.0f, 0.0f);
        }).thenRunForEach(player -> {
            GameTeamKey gameTeamKey = this.teamManager.teamFor(player);
            if (gameTeamKey instanceof GameTeamKey) {
                TeamData theTowersTeam = teamMap.get(gameTeamKey);
                if (theTowersTeam instanceof TeamData && theTowersTeam.health > 0) {
                    player.changeGameMode(GameMode.SURVIVAL);
                    this.resetPlayer(player);
                    this.resetPlayerInventory(player);
                    this.spawnPlayerAtTheirSpawn(player);
                    return;
                }
            }
            player.changeGameMode(GameMode.SPECTATOR);
            this.resetPlayer(player);
            this.spawnPlayerAtCenter(player);
        });
    }

    public void resetPlayerInventory(ServerPlayerEntity player) {
        GameTeam gameTeam = this.config.teamConfig().byKey(this.teamManager.teamFor(player));
        if (gameTeam != null) {
            player.equipStack(EquipmentSlot.HEAD, ItemStackBuilder.of(gameTeam.config().applyDye(new ItemStack(Items.LEATHER_HELMET))).setUnbreakable().build());
            player.equipStack(EquipmentSlot.CHEST, ItemStackBuilder.of(gameTeam.config().applyDye(new ItemStack(Items.LEATHER_CHESTPLATE))).setUnbreakable().build());
            player.equipStack(EquipmentSlot.LEGS, ItemStackBuilder.of(gameTeam.config().applyDye(new ItemStack(Items.LEATHER_LEGGINGS))).addEnchantment(this.world, Enchantments.PROJECTILE_PROTECTION, 2).setUnbreakable().build());
            player.equipStack(EquipmentSlot.FEET, ItemStackBuilder.of(gameTeam.config().applyDye(new ItemStack(Items.LEATHER_BOOTS))).setUnbreakable().build());
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
        this.spawnPlayerAt(player, this.map.spawn(), 0.0F, 0.0F);
    }

    public void spawnPlayerAtTheirSpawn(ServerPlayerEntity player) {
        TeamRegion region = this.map.teamRegions().get(this.teamManager.teamFor(player));
        Vec3d spawnPosition = region.spawn().center();
        this.spawnPlayerAt(player, spawnPosition, region.spawnYaw(), region.spawnPitch());
    }

    public void spawnPlayerAt(ServerPlayerEntity player, Vec3d pos, float yaw, float pitch) {
        player.teleport(this.world, pos.getX(), pos.getY(), pos.getZ(), Set.of(), yaw, pitch, false);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;
    }

    // GENERAL LISTENERS
    private EventResult dropItem(PlayerEntity player, int i, ItemStack stack) {
        if (canStackBeDropped(stack)) {
            return EventResult.ALLOW;
        } else {
            Text msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.WARNING_STYLE, Text.translatable("text.the_towers.cannot_drop_armor"));
            player.sendMessage(msg, false);
            return EventResult.DENY;
        }
    }

    private EventResult killPlayer(ServerPlayerEntity player, DamageSource source) {
        TowersParticipant participant = this.participantMap.get(player);
        if (participant == null) {
            this.spawnPlayerAtCenter(player);
        } else {
            participant.ticksUntilRespawn = this.config.respawnCooldown() * 20L;
            player.changeGameMode(GameMode.SPECTATOR);
            for (int i = 0; i < player.getInventory().size(); ++i) {
                ItemStack stack = player.getInventory().getStack(i);
                if (canStackBeDropped(stack)) {
                    ItemScatterer.spawn(this.world, player.getBlockPos().getX(), player.getBlockPos().getY(), player.getBlockPos().getZ(), player.getInventory().getStack(i));
                }
            }
            Text msg = FormattingUtil.format(FormattingUtil.SKULL_SYMBOL, FormattingUtil.DEATH_STYLE, source.getDeathMessage(player).copyContentOnly());
            this.gameSpace.getPlayers().sendMessage(msg);

            this.resetPlayer(player);

            player.teleport(player.getX(), player.getY() + 1000, player.getZ(), false);
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_MODE_CHANGED, 3));
            PlayerAbilities abilities = new PlayerAbilities();
            abilities.allowFlying = false;
            player.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(abilities));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, (this.config.respawnCooldown() + 1) * 20, 1, true, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, (this.config.respawnCooldown() + 1) * 20, 10, true, false));
        }
        return EventResult.DENY;
    }

    private EventResult placeBlock(ServerPlayerEntity playerEntity, ServerWorld world, BlockPos pos, BlockState state, ItemUsageContext itemUsageContext) {
        for (BlockBounds bounds : this.map.protectedBounds()) {
            if (bounds.contains(pos)) {
                Text msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.WARNING_STYLE, Text.translatable("text.the_towers.cannot_place"));
                playerEntity.sendMessage(msg, false);
                return EventResult.DENY;
            }
        }
        for (GameTeam team : this.teamMap.keySet()) {
            if (team.key() != teamManager.teamFor(playerEntity)) {
                if (this.map.teamRegions().get(team.key()).domains().contains(pos.asLong())) {
                    Text msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.WARNING_STYLE, Text.translatable("text.the_towers.cannot_place"));
                    playerEntity.sendMessage(msg, false);
                    return EventResult.DENY;
                }
            }
        }
        return EventResult.PASS;
    }

    private ActionResult useBlock(ServerPlayerEntity playerEntity, Hand hand, BlockHitResult blockHitResult) {
        // TODO: can't place blocks when trying to place on the side of a protected block. Must fix.
        BlockPos pos = blockHitResult.getBlockPos();
        for (BlockBounds bounds : this.map.protectedBounds()) {
            if (bounds.contains(pos)) {
                Text msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.WARNING_STYLE, Text.translatable("text.the_towers.cannot_use"));
                playerEntity.sendMessage(msg, false);
                return ActionResult.FAIL;
            }
        }
        for (GameTeam team : this.teamMap.keySet()) {
            if (team.key() != teamManager.teamFor(playerEntity)) {
                if (this.map.teamRegions().get(team.key()).domains().contains(pos.asLong())) {
                    Text msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.WARNING_STYLE, Text.translatable("text.the_towers.cannot_use"));
                    playerEntity.sendMessage(msg, false);
                    return ActionResult.FAIL;
                }
            }
        }
        return ActionResult.PASS;
    }

    private EventResult breakBlock(ServerPlayerEntity playerEntity, ServerWorld world, BlockPos pos) {
        for (BlockBounds bounds : this.map.protectedBounds()) {
            if (bounds.contains(pos)) {
                Text msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.WARNING_STYLE, Text.translatable("text.the_towers.cannot_break"));
                playerEntity.sendMessage(msg, false);
                return EventResult.DENY;
            }
        }
        for (GameTeam team : this.teamMap.keySet()) {
            if (team.key() != teamManager.teamFor(playerEntity)) {
                if (this.map.teamRegions().get(team.key()).domains().contains(pos.asLong())) {
                    Text msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.WARNING_STYLE, Text.translatable("text.the_towers.cannot_break"));
                    playerEntity.sendMessage(msg, false);
                    return EventResult.DENY;
                }
            }
        }

        return EventResult.PASS;
    }

    // UTILITY
    private boolean canStackBeDropped(ItemStack stack) {
        if (stack != null) {
            Item item = stack.getItem();
            return item != Items.LEATHER_HELMET && item != Items.LEATHER_CHESTPLATE && item != Items.LEATHER_LEGGINGS && item != Items.LEATHER_BOOTS;
        }
        return true;
    }

    private void refill() {
        for (GameTeam team : this.teamMap.keySet()) {
            this.map.teamRegions().get(team.key()).domains().stream().iterator().forEachRemaining(aLong -> {
                BlockPos pos = BlockPos.fromLong(aLong);
                BlockState state = this.map.template().getBlockState(pos);

                this.world.setBlockState(pos, this.map.template().getBlockState(pos));
                var blockEntity = this.map.template().getBlockEntityNbt(pos);
                if (blockEntity != null) {
                    // TODO: block entities
                }
            });
        }
    }
}
