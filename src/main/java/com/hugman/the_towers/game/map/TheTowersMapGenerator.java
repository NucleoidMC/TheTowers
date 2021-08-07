package com.hugman.the_towers.game.map;

import com.hugman.the_towers.config.TheTowersConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.plasmid.game.GameOpenException;

import java.io.IOException;

public record TheTowersMapGenerator(TheTowersConfig config) {
	public TheTowersMap build(MinecraftServer server) throws GameOpenException {
		try {
			MapTemplate template = MapTemplateSerializer.loadFromResource(server, this.config.mapTemplateId());
			return TheTowersMap.fromTemplate(template, this.config);
		}
		catch(IOException e) {
			throw new GameOpenException(new LiteralText("Failed to load mapTemplateId template"), e);
		}
	}
}
