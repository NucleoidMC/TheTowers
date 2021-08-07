package com.hugman.the_towers.game.map;

import com.hugman.the_towers.config.TheTowersConfig;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateMetadata;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;

import java.io.IOException;

public record TheTowersMapGenerator(TheTowersConfig config) {
	public TheTowersMap build(MinecraftServer server) throws GameOpenException {
		try {
			MapTemplate template = MapTemplateSerializer.loadFromResource(server, this.config.getMap());
			return TheTowersMap.fromTemplate(template, this.config);
		}
		catch(IOException e) {
			throw new GameOpenException(new LiteralText("Failed to load map template"), e);
		}
	}
}
