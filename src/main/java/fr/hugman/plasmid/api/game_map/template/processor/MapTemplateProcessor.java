package fr.hugman.plasmid.api.game_map.template.processor;

import com.mojang.serialization.Codec;
import fr.hugman.plasmid.api.registry.PlasmidRegistries;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.GameActivity;

public interface MapTemplateProcessor {
    Codec<MapTemplateProcessor> TYPE_CODEC = PlasmidRegistries.MAP_TEMPLATE_PROCESSOR_TYPE.getCodec().dispatch(MapTemplateProcessor::getType, MapTemplateProcessorType::codec);

    void processTemplate(GameActivity activity, MapTemplate template);

    MapTemplateProcessorType<?> getType();
}
