package com.hugman.plasmid.api.registry;

import com.hugman.plasmid.api.game_map.GameMapType;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.SimpleRegistry;

public class PlasmidRegistries {
    public static final SimpleRegistry<GameMapType<?>> GAME_MAP_TYPE = FabricRegistryBuilder.createSimple(PlasmidRegistryKeys.GAME_MAP_TYPE).buildAndRegister();
}