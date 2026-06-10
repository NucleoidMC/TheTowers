package fr.hugman.the_towers.api.game;

import fr.hugman.the_towers.TheTowers;
import net.minecraft.resources.ResourceKey;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys;

public class TheTowersGameConfigs {
    public static final ResourceKey<GameConfig<?>> CLASSIC_TWO_TEAMS = of("classic/two_teams");
    public static final ResourceKey<GameConfig<?>> CLASSIC_FOUR_TEAMS = of("classic/four_teams");

    private static ResourceKey<GameConfig<?>> of(String path) {
        return ResourceKey.create(PlasmidRegistryKeys.GAME_CONFIG, TheTowers.id(path));
    }
}
