package fr.hugman.plasmid.api.game_map;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.hugman.plasmid.api.game.attachment.PlasmidGameAttachments;
import net.minecraft.util.Identifier;
import net.minecraft.util.context.ContextParameterMap;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.world.generator.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.api.map.MapLoadContexts;
import xyz.nucleoid.plasmid.api.map.template.processor.MapTemplateProcessor;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public record TemplateGameMap(
        Identifier id,
        Optional<GameMapMetadata> metadata,
        List<MapTemplateProcessor> processors
) implements GameMap {
    public static final MapCodec<TemplateGameMap> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(TemplateGameMap::id),
            GameMapMetadata.CODEC.optionalFieldOf("metadata").forGetter(TemplateGameMap::metadata),
            MapTemplateProcessor.CODEC.listOf().optionalFieldOf("processors", List.of()).forGetter(TemplateGameMap::processors)
    ).apply(instance, TemplateGameMap::new));

    public TemplateGameMap(Identifier id, GameMapMetadata metadata, MapTemplateProcessor... processors) {
        this(id, Optional.of(metadata), List.of(processors));
    }

    @Override
    public GameMapType<?> getType() {
        return GameMapType.TEMPLATE;
    }

    @Override
    public <Config> GameMapLoadResult load(GameActivity activity, Config config) {
        try {
            var server = activity.getGameSpace().getServer();
            var template = MapTemplateSerializer.loadFromResource(server, this.id);
            for (var processor : this.processors) {
                processor.processTemplate(template, getProcessorParameters(activity));
            }
            return new GameMapLoadResult(new TemplateChunkGenerator(server, template), Optional.of(template.getMetadata()));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Optional<GameMapMetadata> getMetadata() {
        return this.metadata;
    }

    private static ContextParameterMap.Builder getProcessorParameters(GameActivity activity) {
        return new ContextParameterMap.Builder()
                .addNullable(MapLoadContexts.TEAM_LIST, activity.getGameSpace().getAttachment(PlasmidGameAttachments.TEAM_LIST));
    }
}
