package fr.hugman.the_towers.game;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import fr.hugman.plasmid.api.game.attachment.PlasmidGameAttachments;
import fr.hugman.the_towers.TheTowers;
import fr.hugman.the_towers.config.TowersConfig;
import fr.hugman.the_towers.map.TeamRegion;
import fr.hugman.the_towers.map.TowersMap;
import fr.hugman.the_towers.util.FormattingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Brightness;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public class TowersActive {
    private final GameSpace gameSpace;
    private final GameActivity activity;
    private final ServerLevel world;
    private final TowersConfig config;
    private final TowersMap map;
    private final GameTeamList teams;

    private Object2ObjectMap<ServerPlayer, TowersParticipant> participantMap;
    private Object2ObjectMap<GameTeamKey, TeamData> teamMap;
    private TeamManager teamManager;

    private final TowersSidebar sidebar;

    private long gameTick = 0;
    private long nextRefillTick;
    private boolean hasEnded = false;
    private long gameCloseTick = -1L;

    private TowersActive(GameSpace gameSpace, GameActivity activity, ServerLevel world, TowersMap map, TowersConfig config, TowersSidebar sidebar, TeamSelectionLobby teamSelection) {
        this.gameSpace = gameSpace;
        this.activity = activity;
        this.world = world;
        this.config = config;
        this.map = map;
        this.teams = gameSpace.getAttachment(PlasmidGameAttachments.TEAM_LIST);

        fillTeams(teamSelection);

        this.sidebar = sidebar;
    }

    private void fillTeams(TeamSelectionLobby teamSelection) {
        this.participantMap = new Object2ObjectOpenHashMap<>();
        this.teamMap = new Object2ObjectOpenHashMap<>();

        this.teamManager = TeamManager.addTo(this.activity);

        for (GameTeam team : this.teams) {
            team = team.withConfig(GameTeamConfig.builder(team.config())
                    .setFriendlyFire(false)
                    .setCollision(Team.CollisionRule.PUSH_OTHER_TEAMS)
                    .build());
            this.teamManager.addTeam(team);
            this.teamMap.put(team.key(), new TeamData(this.config.maxHealth()));
        }

        teamSelection.allocate(this.gameSpace.getPlayers(), (gameTeam, player) -> {
            participantMap.put(player, new TowersParticipant());
            teamManager.addPlayerTo(player, gameTeam);
        });
    }

    public static void enable(GameSpace gameSpace, ServerLevel world, TowersMap map, TowersConfig config, TeamSelectionLobby teamSelection) {
        gameSpace.setActivity(activity -> {
            PlayerLimiter.addTo(activity, config.playerConfig().playerConfig());
            GlobalWidgets widgets = GlobalWidgets.addTo(activity);
            TowersSidebar sidebar = TowersSidebar.create(widgets, gameSpace);
            TowersActive active = new TowersActive(gameSpace, activity, world, map, config, sidebar, teamSelection);

            TeamChat.addTo(activity, active.teamManager);

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

        Component[] guideLines = {
                Component.literal("+--------------------------------------+").withStyle(ChatFormatting.DARK_GRAY),
                this.gameSpace.getMetadata().sourceConfig().value().name().copy().withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD),
                Component.translatable("text.the_towers.guide.craft_stuff").withStyle(ChatFormatting.WHITE),
                Component.translatable("text.the_towers.guide.jumping_into_pool").withStyle(ChatFormatting.WHITE),
                Component.translatable("text.the_towers.guide.protect_your_pool").withStyle(ChatFormatting.WHITE),
                Component.literal("+--------------------------------------+").withStyle(ChatFormatting.DARK_GRAY),
        };

        for (Component text : guideLines) {
            this.gameSpace.getPlayers().sendMessage(text);
        }
        this.teamMap.keySet().forEach(teamKey -> {
            this.teamManager.playersIn(teamKey).forEach(player -> {
                if (player != null) {
                    player.setGameMode(GameType.SURVIVAL);
                    this.resetPlayer(player);
                    this.spawnPlayerAtTheirSpawn(player);
                    this.resetPlayerInventory(player);
                }
            });

            var gameTeam = this.teams.byKey(teamKey);

            TextDisplayElement element = new TextDisplayElement(Component.translatable("text.the_towers.pool", gameTeam.config().name()).withStyle(gameTeam.config().chatFormatting()));
            element.setBillboardMode(Display.BillboardConstraints.CENTER);
            element.setSeeThrough(true);
            element.setBrightness(Brightness.FULL_BRIGHT);
            ElementHolder holder = new ElementHolder();
            holder.addElement(element);

            ChunkAttachment.of(holder, world, map.teamRegions().get(teamKey).pool().centerTop().add(0.0D, 0.5D, 0.0D));
        });
        this.sidebar.update(this.gameTick, this.nextRefillTick, this.teamManager, this.teamMap);
    }

    private void tick() {
        long worldTime = world.getGameTime();
        this.gameTick++;

        if (this.gameTick == this.nextRefillTick) {
            //TODO: fix the refill method
            //this.refill();
            this.nextRefillTick = this.gameTick + this.config.refillCooldown();
        }

        if (!hasEnded) {
            this.map.itemGenerators().forEach(generator -> generator.tick(world, this.gameTick));
            this.teamMap.keySet().forEach(teamKey -> {
                TeamData teamData = this.teamMap.get(teamKey);
                BlockBounds pool = this.map.teamRegions().get(teamKey).pool();
                if (this.gameTick % 60 == 0) {
                    pool.iterator().forEachRemaining(pos -> world.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 2, 0.25D, 0.0D, 0.25D, 0.0D));
                }
                this.teamManager.playersIn(teamKey).forEach(player -> {
                    TowersParticipant participant = this.participantMap.get(player);
                    if(participant == null) {
                        throw new IllegalStateException("Player " + player.getName().getString() + " in team " + teamKey + " has no participant data!");
                    }
                    if (player != null) {
                        // death + respawn
                        if(player.getY() < world.getMinY() - 64) {
                            player.hurtServer(world, world.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
                        }
                        if (participant.ticksUntilRespawn >= 0 && teamData.health > 0) {
                            if ((participant.ticksUntilRespawn + 1) % 20 == 0) {
                                player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 90, 0));
                                player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("text.the_towers.respawn_in", (int) (participant.ticksUntilRespawn / 20 + 1)).withStyle(ChatFormatting.GOLD)));
                            }
                            if (participant.ticksUntilRespawn == 0) {
                                player.setGameMode(GameType.SURVIVAL);
                                player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("")));
                                this.resetPlayer(player);
                                this.resetPlayerInventory(player);
                                this.spawnPlayerAtTheirSpawn(player);
                            }
                            participant.ticksUntilRespawn--;
                        }

                        // Check for players in pools.
                        this.teamMap.keySet().forEach(enemyTeamKey -> {
                            TeamData enemyTeamData = this.teamMap.get(enemyTeamKey);
                            if (teamKey != enemyTeamKey && enemyTeamData.health > 0) {
                                TeamRegion enemyRegion = this.map.teamRegions().get(enemyTeamKey);
                                if (enemyRegion.pool().contains(player.blockPosition()) && player.gameMode.isSurvival()) {
                                    // The player is in an enemy's pool. They make them lose a point and steal them if the configuration allows it.
                                    this.spawnPlayerAtTheirSpawn(player);
                                    enemyTeamData.health--;
                                    if (this.config.healthStealth()) {
                                        var teamConfig = this.teamManager.getTeamConfig(enemyTeamKey);
                                        Component msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.GENERAL_STYLE, Component.translatable("text.the_towers.health_stole", player.getName(), teamConfig.name()));
                                        this.gameSpace.getPlayers().sendMessage(msg);
                                        teamData.health++;
                                    } else {
                                        var teamConfig = this.teamManager.getTeamConfig(enemyTeamKey);
                                        Component msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.GENERAL_STYLE, Component.translatable("text.the_towers.health_removed", player.getName(), teamConfig.name()));
                                        this.gameSpace.getPlayers().sendMessage(msg);
                                    }
                                    this.gameSpace.getPlayers().playSound(SoundEvents.BLAZE_HURT);
                                    this.checkWin();
                                }
                            }
                        });
                    }
                });
            });
            if (this.gameTick % 20 == 0) {
                this.sidebar.update(this.gameTick, this.nextRefillTick, this.teamManager, this.teamMap);
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
            Component msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.GENERAL_STYLE, Component.translatable("text.the_towers.nobody_won"));
            this.gameSpace.getPlayers().sendMessage(Component.literal("\n").append(msg).append("\n"));
            this.hasEnded = true;
        }
        this.teamMap.forEach((teamKey, teamData) -> {
            if (teamData.health == 0) {
                // The selected team has not enough health to be alive. They are eliminated.
                teamData.health = -1;
                this.teamManager.playersIn(teamKey).forEach(player -> {
                    if (player != null) {
                        player.setGameMode(GameType.SPECTATOR);
                        this.resetPlayer(player);
                    }
                });
                var config = this.teamManager.getTeamConfig(teamKey);
                Component msg = FormattingUtil.format(FormattingUtil.X_SYMBOL, FormattingUtil.GENERAL_STYLE, Component.translatable("text.the_towers.team_eliminated", config.name()));
                this.gameSpace.getPlayers().sendMessage(Component.literal("\n").append(msg).append("\n"));
                this.gameSpace.getPlayers().playSound(SoundEvents.FIREWORK_ROCKET_BLAST);
            }
        });
        this.teamMap.forEach((teamKey, team) -> {
            if (aliveCount == 1 && team.health > 0) {
                // The selected team is the only team left that is alive. They win.
                var config = this.teamManager.getTeamConfig(teamKey);
                Component msg = FormattingUtil.format(FormattingUtil.STAR_SYMBOL, FormattingUtil.GENERAL_STYLE, Component.translatable("text.the_towers.team_won", config.name()));
                this.gameSpace.getPlayers().sendMessage(Component.literal("\n").append(msg).append("\n"));
                this.gameSpace.getPlayers().playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
                this.hasEnded = true;
            }
        });

        // Close game after 30 seconds.
        if (this.hasEnded) {
            this.gameCloseTick = world.getGameTime() + 600;
            this.participantMap.keySet().forEach(player -> {
                if (this.gameSpace.getPlayers().contains(player)) {
                    player.setGameMode(GameType.SPECTATOR);
                    this.resetPlayer(player);
                    this.sidebar.update(this.gameTick, this.nextRefillTick, this.teamManager, this.teamMap);
                }
            });
        }
    }

    // GENERAL PLAYER MANAGEMENT
    private JoinAcceptorResult accept(JoinAcceptor acceptor) {
        return acceptor.teleport(profile -> {
            GameTeamKey gameTeamKey = this.teamManager.teamFor(PlayerRef.of(profile));
            if (gameTeamKey instanceof GameTeamKey) {
                // player is in a team, teleport to their spawn
                return new PlayerPos(this.world, this.map.teamRegions().get(gameTeamKey).spawn().center(), 0.0f, 0.0f);
            }
            // player has no team, teleport to spawn
            return new PlayerPos(this.world, this.map.spawn(), 0.0f, 0.0f);
        }).thenRunForEach(player -> {
            GameTeamKey gameTeamKey = this.teamManager.teamFor(player);
            if (gameTeamKey instanceof GameTeamKey) {
                GameTeam gameTeam = this.teams.byKey(this.teamManager.teamFor(player));
                TeamData theTowersTeam = teamMap.get(gameTeam);
                if (theTowersTeam instanceof TeamData && theTowersTeam.health > 0) {
                    player.setGameMode(GameType.SURVIVAL);
                    this.resetPlayer(player);
                    this.resetPlayerInventory(player);
                    this.spawnPlayerAtTheirSpawn(player);
                    return;
                }
            }
            player.setGameMode(GameType.SPECTATOR);
            this.resetPlayer(player);
            this.spawnPlayerAtCenter(player);
        });
    }

    public void resetPlayerInventory(ServerPlayer player) {
        GameTeam gameTeam = this.teams.byKey(this.teamManager.teamFor(player));
        if (gameTeam != null) {
            player.setItemSlot(EquipmentSlot.HEAD, ItemStackBuilder.of(gameTeam.config().applyDye(new ItemStack(Items.LEATHER_HELMET))).setUnbreakable().build());
            player.setItemSlot(EquipmentSlot.CHEST, ItemStackBuilder.of(gameTeam.config().applyDye(new ItemStack(Items.LEATHER_CHESTPLATE))).setUnbreakable().build());
            player.setItemSlot(EquipmentSlot.LEGS, ItemStackBuilder.of(gameTeam.config().applyDye(new ItemStack(Items.LEATHER_LEGGINGS))).addEnchantment(this.world, Enchantments.PROJECTILE_PROTECTION, 2).setUnbreakable().build());
            player.setItemSlot(EquipmentSlot.FEET, ItemStackBuilder.of(gameTeam.config().applyDye(new ItemStack(Items.LEATHER_BOOTS))).setUnbreakable().build());
            player.getInventory().add(ItemStackBuilder.of(Items.WOODEN_SWORD).build());
            player.getInventory().add(ItemStackBuilder.of(Items.WOODEN_PICKAXE).build());
            player.getInventory().add(ItemStackBuilder.of(Items.BAKED_POTATO).setCount(6).build());
        }
    }

    public void resetPlayer(ServerPlayer player) {
        this.clearPlayer(player);
        player.getInventory().clearContent();
        player.getEnderChestInventory().clearContent();
        player.removeAllEffects();
        player.getFoodData().setFoodLevel(20);
        player.setExperienceLevels(0);
        player.setExperiencePoints(0);
        player.setHealth(player.getMaxHealth());
    }

    public void clearPlayer(ServerPlayer player) {
        player.clearFire();
        player.fallDistance = 0.0F;
    }

    public void spawnPlayerAtCenter(ServerPlayer player) {
        this.spawnPlayerAt(player, this.map.spawn(), 0.0F, 0.0F);
    }

    public void spawnPlayerAtTheirSpawn(ServerPlayer player) {
        TeamRegion region = this.map.teamRegions().get(this.teamManager.teamFor(player));
        Vec3 spawnPosition = region.spawn().center();
        this.spawnPlayerAt(player, spawnPosition, region.spawnYaw(), region.spawnPitch());
    }

    public void spawnPlayerAt(ServerPlayer player, Vec3 pos, float yaw, float pitch) {
        player.teleportTo(this.world, pos.x(), pos.y(), pos.z(), Set.of(), yaw, pitch, false);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0f;
    }

    // GENERAL LISTENERS
    private EventResult dropItem(Player player, int i, ItemStack stack) {
        if (canStackBeDropped(stack)) {
            return EventResult.ALLOW;
        } else {
            Component msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.WARNING_STYLE, Component.translatable("text.the_towers.cannot_drop_armor"));
            player.sendSystemMessage(msg);
            return EventResult.DENY;
        }
    }

    private EventResult killPlayer(ServerPlayer player, DamageSource source) {
        TowersParticipant participant = this.participantMap.get(player);
        if (!this.gameSpace.getPlayers().contains(player)) {
            return EventResult.PASS;
        }
        if (participant == null) {
            this.spawnPlayerAtCenter(player);
        } else {
            if (participant.isDead()) {
                return EventResult.DENY;
            }
            participant.ticksUntilRespawn = this.config.respawnCooldown() * 20L;
            player.setGameMode(GameType.SPECTATOR);
            for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
                ItemStack stack = player.getInventory().getItem(i);
                if (canStackBeDropped(stack)) {
                    Containers.dropItemStack(this.world, player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ(), player.getInventory().getItem(i));
                }
            }
            Component msg = FormattingUtil.format(FormattingUtil.SKULL_SYMBOL, FormattingUtil.DEATH_STYLE, source.getLocalizedDeathMessage(player).plainCopy());
            this.gameSpace.getPlayers().sendMessage(msg);

            this.resetPlayer(player);
            this.spawnPlayerAt(player, player.position().with(Direction.Axis.Y, 1000), 0.0F, 0.0F);

            player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, 3));
            Abilities abilities = new Abilities();
            abilities.mayfly = false;
            player.connection.send(new ClientboundPlayerAbilitiesPacket(abilities));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, (this.config.respawnCooldown() + 1) * 20, 1, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, (this.config.respawnCooldown() + 1) * 20, 10, true, false));
        }
        return EventResult.DENY;
    }

    private EventResult placeBlock(ServerPlayer playerEntity, ServerLevel world, BlockPos pos, BlockState state, UseOnContext itemUsageContext) {
        for (BlockBounds bounds : this.map.protectedBounds()) {
            if (bounds.contains(pos)) {
                Component msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.WARNING_STYLE, Component.translatable("text.the_towers.cannot_place"));
                playerEntity.sendSystemMessage(msg);
                return EventResult.DENY;
            }
        }
        for (GameTeamKey teamKey : this.teamMap.keySet()) {
            if (teamKey != teamManager.teamFor(playerEntity)) {
                if (this.map.teamRegions().get(teamKey).domains().contains(pos.asLong())) {
                    Component msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.WARNING_STYLE, Component.translatable("text.the_towers.cannot_place"));
                    playerEntity.sendSystemMessage(msg);
                    return EventResult.DENY;
                }
            }
        }
        return EventResult.PASS;
    }

    private InteractionResult useBlock(ServerPlayer playerEntity, InteractionHand hand, BlockHitResult blockHitResult) {
        // TODO: can't place blocks when trying to place on the side of a protected block. Must fix.
        BlockPos pos = blockHitResult.getBlockPos();
        for (BlockBounds bounds : this.map.protectedBounds()) {
            if (bounds.contains(pos)) {
                Component msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.WARNING_STYLE, Component.translatable("text.the_towers.cannot_use"));
                playerEntity.sendSystemMessage(msg);
                return InteractionResult.FAIL;
            }
        }
        for (GameTeamKey teamKey : this.teamMap.keySet()) {
            if (teamKey != teamManager.teamFor(playerEntity)) {
                if (this.map.teamRegions().get(teamKey).domains().contains(pos.asLong())) {
                    Component msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.WARNING_STYLE, Component.translatable("text.the_towers.cannot_use"));
                    playerEntity.sendSystemMessage(msg);
                    return InteractionResult.FAIL;
                }
            }
        }
        return InteractionResult.PASS;
    }

    private EventResult breakBlock(ServerPlayer playerEntity, ServerLevel world, BlockPos pos) {
        for (BlockBounds bounds : this.map.protectedBounds()) {
            if (bounds.contains(pos)) {
                Component msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.WARNING_STYLE, Component.translatable("text.the_towers.cannot_break"));
                playerEntity.sendSystemMessage(msg);
                return EventResult.DENY;
            }
        }
        for (GameTeamKey teamKey : this.teamMap.keySet()) {
            if (teamKey != teamManager.teamFor(playerEntity)) {
                if (this.map.teamRegions().get(teamKey).domains().contains(pos.asLong())) {
                    Component msg = FormattingUtil.format(FormattingUtil.GENERAL_SYMBOL, FormattingUtil.WARNING_STYLE, Component.translatable("text.the_towers.cannot_break"));
                    playerEntity.sendSystemMessage(msg);
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

    /*
    private void refill() {
        for (GameTeamKey teamKey : this.teamMap.keySet()) {
            this.map.teamRegions().get(teamKey).domains().stream().iterator().forEachRemaining(aLong -> {
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
     */
}
