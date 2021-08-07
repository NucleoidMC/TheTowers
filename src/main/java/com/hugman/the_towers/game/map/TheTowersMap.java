package com.hugman.the_towers.game.map;

import com.hugman.the_towers.TheTowers;
import com.hugman.the_towers.config.TheTowersConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
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

public record TheTowersMap(MapTemplate template, TheTowersConfig config, BlockBounds centerBounds, Map<GameTeam, TheTowersTeamRegion> teamRegions, List<BlockBounds> protectedBounds) {
	public static TheTowersMap fromTemplate(MapTemplate template, TheTowersConfig config) {
		MapTemplateMetadata metadata = template.getMetadata();
		BlockBounds centerBounds = metadata.getFirstRegionBounds("center");
		if(centerBounds == null) {
			TheTowers.LOGGER.warn("Missing map center, set to default [0 50 0]");
			centerBounds = BlockBounds.of(new BlockPos(0, 50, 0), new BlockPos(0, 50, 0));
		}

		List<BlockBounds> protectedBounds = metadata.getRegionBounds("protected").collect(Collectors.toList());
		Map<GameTeam, TheTowersTeamRegion> teamRegions = new HashMap<>();
		for(GameTeam team : config.getTeams()) {
			TheTowersTeamRegion region = TheTowersTeamRegion.fromTemplate(team, metadata);
			teamRegions.put(team, region);
		}

		return new TheTowersMap(template, config, centerBounds, teamRegions, protectedBounds);
	}

	public List<BlockBounds> getProtectedBounds() {
		return protectedBounds;
	}

	public Vec3d getCenter() {
		return centerBounds.center();
	}

	public TheTowersTeamRegion getTeamRegion(GameTeam team) {
		return teamRegions.get(team);
	}

	public ChunkGenerator asGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}
