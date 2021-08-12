package com.hugman.the_towers.game.map;

import com.hugman.the_towers.config.TowersConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.plasmid.game.GameOpenException;

import java.io.IOException;

public class TowersMapGenerator {
	public static TowersMap loadFromConfig(MinecraftServer server, TowersConfig config) throws GameOpenException {
		try {
			MapTemplate template = MapTemplateSerializer.loadFromResource(server, config.mapTemplateId());
			return TowersMap.fromTemplate(template, config);
		}
		catch(IOException e) {
			throw new GameOpenException(new LiteralText("Failed to load map template"), e);
		}
	}
}
