package fr.hugman.the_towers.api.map.generator;

import fr.hugman.the_towers.TheTowers;
import fr.hugman.the_towers.map.generator.ItemGeneratorConfig;
import fr.hugman.the_towers.registry.TheTowersRegistryKeys;
import net.minecraft.registry.RegistryKey;

public class TheTowersGenerators {
    public static final RegistryKey<ItemGeneratorConfig> IRON_LOW = of("iron/low");
    public static final RegistryKey<ItemGeneratorConfig> IRON_MEDIUM = of("iron/medium");
    public static final RegistryKey<ItemGeneratorConfig> IRON_HIGH = of("iron/high");

    private static RegistryKey<ItemGeneratorConfig> of(String path) {
        return RegistryKey.of(TheTowersRegistryKeys.GENERATOR, TheTowers.id(path));
    }
}
