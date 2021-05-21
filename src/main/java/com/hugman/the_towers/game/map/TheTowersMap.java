package com.hugman.the_towers.game.map;

import com.hugman.the_towers.TheTowers;
import com.hugman.the_towers.config.TheTowersConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;

import java.util.HashMap;
import java.util.Map;

public class TheTowersMap {
	private final MapTemplate template;
	private final TheTowersConfig config;
	private final Map<GameTeam, TheTowersTeamRegion> teamRegions = new HashMap<>();

	public TheTowersMap(MapTemplate template, TheTowersConfig config) {
		this.template = template;
		this.config = config;
	}

	public void addTeamRegions(GameTeam team, TheTowersTeamRegion region) {
		this.teamRegions.put(team, region);

		if(region.getSpawn() == null) {
			TheTowers.LOGGER.warn("Missing spawn for {}", team.getKey());
		}
		if(region.getPool() == null) {
			TheTowers.LOGGER.warn("Missing pool for {}", team.getKey());
		}
	}

	public Map<GameTeam, TheTowersTeamRegion> getTeamRegions() {
		return teamRegions;
	}

	public TheTowersTeamRegion getTeamRegion(GameTeam team) {
		return teamRegions.get(team);
	}

	public ChunkGenerator asGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}
