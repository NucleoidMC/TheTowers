package fr.hugman.plasmid.api.game_map;

import xyz.nucleoid.map_templates.MapTemplateMetadata;

import java.util.Optional;
import net.minecraft.world.level.chunk.ChunkGenerator;

public record GameMapLoadResult(
        ChunkGenerator chunkGenerator,
        Optional<MapTemplateMetadata> templateMetadata
) {
}
