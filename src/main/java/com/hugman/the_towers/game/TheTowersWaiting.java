package com.hugman.the_towers.game;

import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.event.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import com.hugman.the_towers.game.map.TheTowersMap;
import com.hugman.the_towers.game.map.TheTowersMapGenerator;
import xyz.nucleoid.fantasy.BubbleWorldConfig;

public class TheTowersWaiting {
    private final GameSpace gameSpace;
    private final TheTowersMap map;
    private final TheTowersConfig config;
    private final TheTowersSpawnLogic spawnLogic;

    private TheTowersWaiting(GameSpace gameSpace, TheTowersMap map, TheTowersConfig config) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;
        this.spawnLogic = new TheTowersSpawnLogic(gameSpace, map);
    }

    public static GameOpenProcedure open(GameOpenContext<TheTowersConfig> context) {
        TheTowersConfig config = context.getConfig();
        TheTowersMapGenerator generator = new TheTowersMapGenerator(config.mapConfig);
        TheTowersMap map = generator.build();

        BubbleWorldConfig worldConfig = new BubbleWorldConfig()
                .setGenerator(map.asGenerator(context.getServer()))
                .setDefaultGameMode(GameMode.SPECTATOR);

        return context.createOpenProcedure(worldConfig, game -> {
            TheTowersWaiting waiting = new TheTowersWaiting(game.getSpace(), map, context.getConfig());

            GameWaitingLobby.applyTo(game, config.playerConfig);

            game.on(RequestStartListener.EVENT, waiting::requestStart);
            game.on(PlayerAddListener.EVENT, waiting::addPlayer);
            game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
        });
    }

    private StartResult requestStart() {
        TheTowersActive.open(this.gameSpace, this.map, this.config);
        return StartResult.OK;
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnPlayer(player);
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        player.setHealth(20.0f);
        this.spawnPlayer(player);
        return ActionResult.FAIL;
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player);
    }
}
