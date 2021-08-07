package com.hugman.the_towers.game.map;

import com.hugman.the_towers.config.TheTowersConfig;
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
			MapTemplateMetadata metadata = template.getMetadata();
			BlockBounds center = metadata.getFirstRegionBounds("center");
			TheTowersMap map = new TheTowersMap(template, this.config, center);

			for(GameTeam team : this.config.getTeams()) {
				TheTowersTeamRegion region = TheTowersTeamRegion.fromTemplate(team, metadata);
				map.addTeamRegions(team, region);
			}
			metadata.getRegionBounds("protected").forEach(map::addProtectedBounds);

			return map;
		}
		catch(IOException e) {
			throw new GameOpenException(new LiteralText("Failed to load map template"), e);
		}
	}
}
