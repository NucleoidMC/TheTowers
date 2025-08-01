package fr.hugman.plasmid.api.registry;

import fr.hugman.plasmid.api.game.team.provider.TeamListProviderType;
import fr.hugman.plasmid.api.game_map.GameMapType;
import fr.hugman.plasmid.api.game_map.template.processor.MapTemplateProcessorType;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.SimpleRegistry;

public class PlasmidRegistries {
    public static final SimpleRegistry<GameMapType<?>> GAME_MAP_TYPE = FabricRegistryBuilder.createSimple(PlasmidRegistryKeys.GAME_MAP_TYPE).buildAndRegister();
    public static final SimpleRegistry<MapTemplateProcessorType<?>> MAP_TEMPLATE_PROCESSOR_TYPE = FabricRegistryBuilder.createSimple(PlasmidRegistryKeys.MAP_TEMPLATE_PROCESSOR_TYPE).buildAndRegister();
    public static final SimpleRegistry<TeamListProviderType<?>> TEAM_LIST_PROVIDER_TYPE = FabricRegistryBuilder.createSimple(PlasmidRegistryKeys.TEAM_LIST_PROVIDER_TYPE).buildAndRegister();
}