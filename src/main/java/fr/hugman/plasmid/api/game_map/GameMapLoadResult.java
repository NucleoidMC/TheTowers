package fr.hugman.plasmid.api.game_map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.MapTemplateMetadata;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public record GameMapLoadResult(
        ChunkGenerator chunkGenerator,
        Optional<MapTemplateMetadata> templateMetadata
) {}
