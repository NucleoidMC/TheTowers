package fr.hugman.the_towers.api.game_map;

import fr.hugman.plasmid.api.game_map.GameMap;
import fr.hugman.plasmid.api.registry.PlasmidRegistryKeys;
import fr.hugman.the_towers.TheTowers;
import net.minecraft.registry.RegistryKey;

public class TheTowersGameMaps {
    public static final RegistryKey<GameMap> CLASSIC_TWO_TEAMS = of("classic/two_teams");
    public static final RegistryKey<GameMap> CLASSIC_FOUR_TEAMS = of("classic/four_teams");

    private static RegistryKey<GameMap> of(String path) {
        return RegistryKey.of(PlasmidRegistryKeys.GAME_MAP, TheTowers.id(path));
    }
}
