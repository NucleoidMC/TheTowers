package fr.hugman.the_towers.api.game;

import fr.hugman.the_towers.TheTowers;
import net.minecraft.registry.RegistryKey;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.game.config.GameConfigs;

public class TheTowersGameConfigs {
    public static final RegistryKey<GameConfig<?>> CLASSIC_TWO_TEAMS = of("classic/two_teams");
    public static final RegistryKey<GameConfig<?>> CLASSIC_FOUR_TEAMS = of("classic/four_teams");

    private static RegistryKey<GameConfig<?>> of(String path) {
        return RegistryKey.of(GameConfigs.REGISTRY_KEY, TheTowers.id(path));
    }
}
