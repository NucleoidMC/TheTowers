package com.hugman.plasmid.api.registry;

import com.hugman.plasmid.api.game_map.GameMap;
import com.hugman.plasmid.api.game_map.GameMapType;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.impl.Plasmid;

public class PlasmidRegistryKeys {
    public static final RegistryKey<Registry<GameMap>> GAME_MAP = RegistryKey.ofRegistry(Identifier.of(Plasmid.ID, "game_map"));
    public static final RegistryKey<Registry<GameMapType<?>>> GAME_MAP_TYPE = RegistryKey.ofRegistry(Identifier.of(Plasmid.ID, "game_map_type"));

    public static void register() {
        DynamicRegistries.register(GAME_MAP, GameMap.TYPE_CODEC);
    }
}