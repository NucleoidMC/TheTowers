package com.hugman.the_towers.game.map;

import com.hugman.the_towers.TheTowers;
import com.hugman.the_towers.config.TheTowersConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheTowersMap {
	private final MapTemplate template;
	private final TheTowersConfig config;
	private final Map<GameTeam, TheTowersTeamRegion> teamRegions = new HashMap<>();
	private BlockBounds centerBounds;
	private final List<BlockBounds> protectedBounds = new ArrayList<>();

	public TheTowersMap(MapTemplate template, TheTowersConfig config, BlockBounds centerBounds) {
		this.template = template;
		this.config = config;
		this.centerBounds = template.getMetadata().getFirstRegionBounds("center");
		if(centerBounds == null) {
			TheTowers.LOGGER.warn("Missing map center, set to default [0 50 0]");
			this.centerBounds = BlockBounds.of(new BlockPos(0, 50, 0), new BlockPos(0, 50, 0));
		}
	}

	public void addTeamRegions(GameTeam team, TheTowersTeamRegion region) {
		this.teamRegions.put(team, region);
	}

	public void addProtectedBounds(BlockBounds bounds) {
		protectedBounds.add(bounds);
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
