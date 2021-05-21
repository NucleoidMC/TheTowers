package com.hugman.the_towers.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import com.hugman.the_towers.game.map.TheTowersMapConfig;

public class TheTowersConfig {
    public static final Codec<TheTowersConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
            TheTowersMapConfig.CODEC.fieldOf("map").forGetter(config -> config.mapConfig),
            Codec.INT.fieldOf("time_limit_secs").forGetter(config -> config.timeLimitSecs)
    ).apply(instance, TheTowersConfig::new));

    public final PlayerConfig playerConfig;
    public final TheTowersMapConfig mapConfig;
    public final int timeLimitSecs;

    public TheTowersConfig(PlayerConfig players, TheTowersMapConfig mapConfig, int timeLimitSecs) {
        this.playerConfig = players;
        this.mapConfig = mapConfig;
        this.timeLimitSecs = timeLimitSecs;
    }
}
