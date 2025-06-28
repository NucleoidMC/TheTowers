package com.hugman.the_towers.config;

import com.hugman.plasmid.api.game_map.GameMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.entry.RegistryEntry;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;

public record TowersConfig(
        WaitingLobbyConfig playerConfig,
        GameTeamList teamConfig,
        RegistryEntry<GameMap> map,
        int maxHealth,
        boolean healthStealth,
        int respawnCooldown,
        int refillCooldown
) {
    public static final MapCodec<TowersConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            WaitingLobbyConfig.CODEC.fieldOf("players").forGetter(TowersConfig::playerConfig),
            GameTeamList.CODEC.fieldOf("teams").forGetter(TowersConfig::teamConfig),
            GameMap.ENTRY_CODEC.fieldOf("map").forGetter(TowersConfig::map),
            Codec.INT.fieldOf("max_health").forGetter(TowersConfig::maxHealth),
            Codec.BOOL.fieldOf("health_stealth").forGetter(TowersConfig::healthStealth),
            Codec.INT.optionalFieldOf("respawn_cooldown", 5).forGetter(TowersConfig::respawnCooldown),
            Codec.INT.optionalFieldOf("refill_cooldown", 5 * 60 * 20).forGetter(TowersConfig::refillCooldown)
    ).apply(instance, TowersConfig::new));
}
