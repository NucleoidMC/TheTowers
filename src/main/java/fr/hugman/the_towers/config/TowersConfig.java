package fr.hugman.the_towers.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.hugman.plasmid.api.game.team.provider.TeamListProvider;
import fr.hugman.plasmid.api.game_map.GameMap;
import net.minecraft.SharedConstants;
import net.minecraft.registry.entry.RegistryEntry;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;

public record TowersConfig(
        WaitingLobbyConfig playerConfig,
        TeamListProvider teamConfig,
        RegistryEntry<GameMap> map,
        int maxHealth,
        boolean healthStealth,
        int respawnCooldown,
        int refillCooldown
) {
    public static final boolean DEFAULT_HEALTH_SLEATH = false;
    public static final int DEFAULT_RESPAWN_COOLDOWN = 5;
    public static final int DEFAULT_REFILL_COOLDOWN = 5 * 60 * SharedConstants.TICKS_PER_SECOND;

    public static final MapCodec<TowersConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            WaitingLobbyConfig.CODEC.fieldOf("players").forGetter(TowersConfig::playerConfig),
            TeamListProvider.TYPE_CODEC.fieldOf("teams").forGetter(TowersConfig::teamConfig),
            GameMap.ENTRY_CODEC.fieldOf("map").forGetter(TowersConfig::map),
            Codec.INT.fieldOf("max_health").forGetter(TowersConfig::maxHealth),
            Codec.BOOL.optionalFieldOf("health_stealth", DEFAULT_HEALTH_SLEATH).forGetter(TowersConfig::healthStealth),
            Codec.INT.optionalFieldOf("respawn_cooldown", DEFAULT_RESPAWN_COOLDOWN).forGetter(TowersConfig::respawnCooldown),
            Codec.INT.optionalFieldOf("refill_cooldown", DEFAULT_REFILL_COOLDOWN).forGetter(TowersConfig::refillCooldown)
    ).apply(instance, TowersConfig::new));

    public TowersConfig(WaitingLobbyConfig playerConfig, TeamListProvider teamConfig, RegistryEntry<GameMap> map, int maxHealth) {
        this(playerConfig, teamConfig, map, maxHealth, DEFAULT_HEALTH_SLEATH, DEFAULT_RESPAWN_COOLDOWN, DEFAULT_REFILL_COOLDOWN);
    }
}
