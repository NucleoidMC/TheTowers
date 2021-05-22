package com.hugman.the_towers.game.map;

import net.minecraft.text.LiteralText;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.map.template.MapTemplateMetadata;
import xyz.nucleoid.plasmid.map.template.TemplateRegion;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.Objects;

public class TheTowersTeamRegion {
	private final BlockBounds spawn;
	private final BlockBounds pool;
	private final float spawnYaw;
	private final float spawnPitch;

	private TheTowersTeamRegion(BlockBounds spawn, BlockBounds pool, float spawnYaw, float spawnPitch) {
		this.spawn = spawn;
		this.pool = pool;
		this.spawnYaw = spawnYaw;
		this.spawnPitch = spawnPitch;
	}

	public static TheTowersTeamRegion fromTemplate(GameTeam team, MapTemplateMetadata metadata) {
		try {
			String teamKey = team.getKey();

			TemplateRegion spawnRegion = metadata.getFirstRegion(teamKey + "_spawn");
			BlockBounds spawn = Objects.requireNonNull(spawnRegion).getBounds();
			float spawnYaw = spawnRegion.getData().getFloat("Yaw");
			float spawnPitch = spawnRegion.getData().getFloat("Pitch");

			TemplateRegion poolRegion = metadata.getFirstRegion(teamKey + "_pool");
			BlockBounds pool = Objects.requireNonNull(poolRegion).getBounds();

			return new TheTowersTeamRegion(spawn, pool, spawnYaw, spawnPitch);
		}
		catch(NullPointerException e) {
			throw new GameOpenException(new LiteralText("Failed to load map template" + team.getKey()), e);
		}
	}

	public BlockBounds getPool() {
		return pool;
	}

	public BlockBounds getSpawn() {
		return spawn;
	}

	public float getSpawnYaw() {
		return spawnYaw;
	}

	public float getSpawnPitch() {
		return spawnPitch;
	}
}
