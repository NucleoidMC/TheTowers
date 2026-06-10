package fr.hugman.the_towers.api.map.generator;

import fr.hugman.the_towers.TheTowers;
import fr.hugman.the_towers.map.generator.ItemGeneratorConfig;
import fr.hugman.the_towers.registry.TheTowersRegistryKeys;
import net.minecraft.resources.ResourceKey;

public class TheTowersGenerators {
    public static final ResourceKey<ItemGeneratorConfig> IRON_LOW = of("iron/low");
    public static final ResourceKey<ItemGeneratorConfig> IRON_MEDIUM = of("iron/medium");
    public static final ResourceKey<ItemGeneratorConfig> IRON_HIGH = of("iron/high");

    private static ResourceKey<ItemGeneratorConfig> of(String path) {
        return ResourceKey.create(TheTowersRegistryKeys.GENERATOR, TheTowers.id(path));
    }
}
