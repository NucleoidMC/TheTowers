package com.hugman.the_towers.game.map;

import com.hugman.the_towers.config.TheTowersConfig;
import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class TheTowersMap {
    private final MapTemplate template;
    private final TheTowersConfig config;
    public BlockPos spawn;

    public TheTowersMap(MapTemplate template, TheTowersConfig config) {
        this.template = template;
        this.config = config;
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }
}
