package com.hugman.the_towers.registry;

import com.hugman.the_towers.TheTowers;
import com.hugman.the_towers.map.generator.GeneratorConfig;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class TheTowersRegistryKeys {
    public static final RegistryKey<Registry<GeneratorConfig>> GENERATOR = RegistryKey.ofRegistry(TheTowers.id("generator"));

    public static void register() {
        DynamicRegistries.register(GENERATOR, GeneratorConfig.CODEC);
    }
}