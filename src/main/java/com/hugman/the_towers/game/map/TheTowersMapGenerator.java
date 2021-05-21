package com.hugman.the_towers.game.map;

import com.hugman.the_towers.config.TheTowersConfig;
import net.minecraft.text.LiteralText;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.MapTemplateMetadata;
import xyz.nucleoid.plasmid.map.template.MapTemplateSerializer;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.io.IOException;

public class TheTowersMapGenerator {

	private final TheTowersConfig config;

	public TheTowersMapGenerator(TheTowersConfig config) {
		this.config = config;
	}

	public TheTowersMap build() throws GameOpenException {
		try {
			MapTemplate template = MapTemplateSerializer.INSTANCE.loadFromResource(this.config.getMap());
            MapTemplateMetadata metadata = template.getMetadata();

            for (GameTeam team : this.config.getTeams()) {
                BlockBounds spawn = metadata.getFirstRegionBounds(team.getKey() + "_spawn");
            }

            TheTowersMap map = new TheTowersMap(template, this.config);

            return map;
		}
		catch(IOException e) {
			throw new GameOpenException(new LiteralText("Failed to load template"), e);
		}
	}
}
