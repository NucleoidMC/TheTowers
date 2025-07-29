package fr.hugman.the_towers.registry;

import fr.hugman.the_towers.TheTowers;
import fr.hugman.the_towers.map.generator.ItemGeneratorConfig;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class TheTowersRegistryKeys {
    public static final RegistryKey<Registry<ItemGeneratorConfig>> GENERATOR = RegistryKey.ofRegistry(TheTowers.id("generator"));

    public static void register() {
        DynamicRegistries.register(GENERATOR, ItemGeneratorConfig.CODEC);
    }
}