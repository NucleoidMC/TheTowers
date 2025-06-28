package fr.hugman.plasmid.api.game_map;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.world.generator.TemplateChunkGenerator;

import java.io.IOException;
import java.util.Optional;

public record TemplateGameMap(Identifier id) implements GameMap {
    public static final MapCodec<TemplateGameMap> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(TemplateGameMap::id)
    ).apply(instance, TemplateGameMap::new));

    @Override
    public GameMapType<?> getType() {
        return GameMapType.TEMPLATE;
    }

    @Override
    public GameMapLoadResult load(GameOpenContext<?> context) {
        try {
            var template = MapTemplateSerializer.loadFromResource(context.server(), this.id);
            return new GameMapLoadResult(s -> new TemplateChunkGenerator(s, template), Optional.of(template.getMetadata()));
        } catch (IOException e) {
            return null;
        }
    }
}
