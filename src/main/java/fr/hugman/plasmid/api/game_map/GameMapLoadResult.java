package fr.hugman.plasmid.api.game_map;

import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.MapTemplateMetadata;

import java.util.Optional;

public record GameMapLoadResult(
        ChunkGenerator chunkGenerator,
        Optional<MapTemplateMetadata> templateMetadata
) {
}
