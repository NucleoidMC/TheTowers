package com.hugman.the_towers.game.map;

import com.hugman.the_towers.TheTowers;
import com.hugman.the_towers.config.TheTowersConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateMetadata;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record TheTowersMap(MapTemplate template, TheTowersConfig config, Vec3d center, List<BlockBounds> protectedBounds, List<Vec3d> ironGenerators, Map<GameTeam, TheTowersTeamRegion> teamRegions) {
	public static TheTowersMap fromTemplate(MapTemplate template, TheTowersConfig config) {
		MapTemplateMetadata metadata = template.getMetadata();
		Vec3d center = new Vec3d(0, 50, 0);
		BlockBounds centerBounds = metadata.getFirstRegionBounds("center");
		if(centerBounds != null) {
			center = centerBounds.center();
		}
		else {
			TheTowers.LOGGER.warn("Missing map center, set to default [0 50 0]");
		}

		List<BlockBounds> protectedBounds = metadata.getRegionBounds("protected").collect(Collectors.toList());
		List<Vec3d> ironGenerators = metadata.getRegionBounds("iron_generators").map(BlockBounds::center).collect(Collectors.toList());

		Map<GameTeam, TheTowersTeamRegion> teamRegions = new HashMap<>();
		for(GameTeam team : config.getTeams()) {
			TheTowersTeamRegion region = TheTowersTeamRegion.fromTemplate(team, metadata);
			teamRegions.put(team, region);
		}

		return new TheTowersMap(template, config, center, protectedBounds, ironGenerators, teamRegions);
	}

	public ChunkGenerator asGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}
