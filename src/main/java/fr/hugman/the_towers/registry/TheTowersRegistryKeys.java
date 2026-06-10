package fr.hugman.the_towers.registry;

import fr.hugman.the_towers.TheTowers;
import fr.hugman.the_towers.map.generator.ItemGeneratorConfig;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class TheTowersRegistryKeys {
    public static final ResourceKey<Registry<ItemGeneratorConfig>> GENERATOR = ResourceKey.createRegistryKey(TheTowers.id("generator"));

    public static void register() {
        DynamicRegistries.register(GENERATOR, ItemGeneratorConfig.CODEC);
    }
}