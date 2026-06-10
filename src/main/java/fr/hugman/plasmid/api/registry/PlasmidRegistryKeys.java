package fr.hugman.plasmid.api.registry;

import fr.hugman.plasmid.api.game_map.GameMap;
import fr.hugman.plasmid.api.game_map.GameMapType;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;
import xyz.nucleoid.plasmid.api.game.config.GameConfigs;
import xyz.nucleoid.plasmid.impl.Plasmid;

public class PlasmidRegistryKeys {
    public static final ResourceKey<Registry<GameConfig<?>>> GAME_CONFIG = GameConfigs.REGISTRY_KEY;
    public static final ResourceKey<Registry<GameMap>> GAME_MAP = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Plasmid.ID, "game_map"));
    public static final ResourceKey<Registry<GameMapType<?>>> GAME_MAP_TYPE = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Plasmid.ID, "game_map_type"));

    public static void register() {
        DynamicRegistries.register(GAME_MAP, GameMap.TYPE_CODEC);
    }
}