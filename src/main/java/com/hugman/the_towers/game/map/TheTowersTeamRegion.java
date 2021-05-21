package com.hugman.the_towers.game.map;

import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.map.template.MapTemplateMetadata;
import xyz.nucleoid.plasmid.util.BlockBounds;

public class TheTowersTeamRegion {
	private final BlockBounds spawn, pool;

	private TheTowersTeamRegion(BlockBounds spawn, BlockBounds pool) {
		this.spawn = spawn;
		this.pool = pool;
	}

	public static TheTowersTeamRegion fromTemplate(GameTeam team, MapTemplateMetadata metadata) {
		String teamKey = team.getKey();

		BlockBounds spawn = metadata.getFirstRegionBounds(teamKey + "_spawn");
		BlockBounds pool = metadata.getFirstRegionBounds(teamKey + "_pool");

		return new TheTowersTeamRegion(spawn, pool);
	}

	public BlockBounds getPool() {
		return pool;
	}

	public BlockBounds getSpawn() {
		return spawn;
	}
}
