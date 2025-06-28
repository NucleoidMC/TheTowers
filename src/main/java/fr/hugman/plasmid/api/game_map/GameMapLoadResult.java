package fr.hugman.plasmid.api.game_map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.MapTemplateMetadata;

import java.util.Optional;
import java.util.function.Function;

public record GameMapLoadResult(
        Function<MinecraftServer, ChunkGenerator> chunkGenerator,
        Optional<MapTemplateMetadata> templateMetadata
) {
    public ChunkGenerator chunkGenerator(MinecraftServer server) {
        return this.chunkGenerator.apply(server);
    }
}
