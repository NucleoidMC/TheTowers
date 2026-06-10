package fr.hugman.plasmid.api.registry;

import fr.hugman.plasmid.api.game_map.GameMapType;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.MappedRegistry;

public class PlasmidRegistries {
    public static final MappedRegistry<GameMapType<?>> GAME_MAP_TYPE = FabricRegistryBuilder.createSimple(PlasmidRegistryKeys.GAME_MAP_TYPE).buildAndRegister();
}