package fr.hugman.plasmid.api.registry;

import fr.hugman.plasmid.api.game_map.GameMap;
import fr.hugman.plasmid.api.game_map.GameMapType;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import xyz.nucleoid.plasmid.impl.Plasmid;

public class PlasmidRegistryKeys {
    public static final ResourceKey<Registry<GameMap>> GAME_MAP = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(Plasmid.ID, "game_map"));
    public static final ResourceKey<Registry<GameMapType<?>>> GAME_MAP_TYPE = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(Plasmid.ID, "game_map_type"));

    public static void register() {
        DynamicRegistries.register(GAME_MAP, GameMap.TYPE_CODEC);
    }
}