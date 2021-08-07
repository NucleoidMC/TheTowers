package com.hugman.the_towers.game.map;

import net.minecraft.text.LiteralText;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplateMetadata;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TheTowersTeamRegion {
	private final BlockBounds spawn;
	private final BlockBounds pool;
	private final List<BlockBounds> chests;
	private final float spawnYaw;
	private final float spawnPitch;

	private TheTowersTeamRegion(BlockBounds spawn, BlockBounds pool, List<BlockBounds> chests, float spawnYaw, float spawnPitch) {
		this.spawn = spawn;
		this.pool = pool;
		this.chests = chests;
		this.spawnYaw = spawnYaw;
		this.spawnPitch = spawnPitch;
	}

	public static TheTowersTeamRegion fromTemplate(GameTeam team, MapTemplateMetadata metadata) {
		try {
			String teamKey = team.key();

			TemplateRegion spawnRegion = metadata.getFirstRegion(teamKey + "_spawn");
			BlockBounds spawn = Objects.requireNonNull(spawnRegion).getBounds();
			float spawnYaw = spawnRegion.getData().getFloat("Yaw");
			float spawnPitch = spawnRegion.getData().getFloat("Pitch");

			TemplateRegion poolRegion = metadata.getFirstRegion(teamKey + "_pool");
			BlockBounds pool = Objects.requireNonNull(poolRegion).getBounds();

			List<BlockBounds> chests = metadata.getRegionBounds(teamKey + "_chest").collect(Collectors.toList());

			return new TheTowersTeamRegion(spawn, pool, chests, spawnYaw, spawnPitch);
		}
		catch(NullPointerException e) {
			throw new GameOpenException(new LiteralText("Failed to load map template" + team.key()), e);
		}
	}

	public BlockBounds getPool() {
		return pool;
	}

	public BlockBounds getSpawn() {
		return spawn;
	}

	public List<BlockBounds> getChests() {
		return chests;
	}

	public float getSpawnYaw() {
		return spawnYaw;
	}

	public float getSpawnPitch() {
		return spawnPitch;
	}
}
