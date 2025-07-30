package fr.hugman.plasmid.api.game_map.template.processor;

import com.mojang.serialization.Codec;
import fr.hugman.plasmid.api.game_map.GameMap;
import fr.hugman.plasmid.api.game_map.GameMapType;
import fr.hugman.plasmid.api.registry.PlasmidRegistries;
import fr.hugman.plasmid.api.registry.PlasmidRegistryKeys;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.GameActivity;

public interface MapTemplateProcessor {
    Codec<MapTemplateProcessor> TYPE_CODEC = PlasmidRegistries.MAP_TEMPLATE_PROCESSOR_TYPE.getCodec().dispatch(MapTemplateProcessor::getType, MapTemplateProcessorType::codec);

    void processTemplate(GameActivity activity, MapTemplate template);

    MapTemplateProcessorType<?> getType();
}
