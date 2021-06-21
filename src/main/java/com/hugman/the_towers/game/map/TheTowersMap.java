package com.hugman.the_towers.game.map;

import com.hugman.the_towers.TheTowers;
import com.hugman.the_towers.config.TheTowersConfig;
import com.hugman.the_towers.game.TheTowersTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheTowersMap {
	private final MapTemplate template;
	private final TheTowersConfig config;
	private final Map<GameTeam, TheTowersTeamRegion> teamRegions = new HashMap<>();
	private final BlockBounds center;
	private final List<BlockBounds> protectedBounds = new ArrayList<>();

	public TheTowersMap(MapTemplate template, TheTowersConfig config, BlockBounds center) {
		this.template = template;
		this.config = config;
		if(center != null) {
			this.center = center;
		}
		else {
			TheTowers.LOGGER.warn("Missing map center");
			this.center = new BlockBounds(new BlockPos(0, 50, 0), new BlockPos(0, 50, 0));
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
		return center.getCenter();
	}

	public TheTowersTeamRegion getTeamRegion(TheTowersTeam team) {
		return teamRegions.get(team.getGameTeam());
	}

	public ChunkGenerator asGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}
