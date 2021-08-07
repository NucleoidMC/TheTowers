package com.hugman.the_towers.game.map;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplateMetadata;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;

import java.util.Objects;

public record TheTowersTeamRegion(BlockBounds spawn, BlockBounds pool, LongSet domain, float spawnYaw, float spawnPitch) {
	public static TheTowersTeamRegion fromTemplate(GameTeam team, MapTemplateMetadata metadata) {
		try {
			String teamKey = team.key();

			TemplateRegion spawnRegion = metadata.getFirstRegion(teamKey + "_spawn");
			BlockBounds spawn = Objects.requireNonNull(spawnRegion).getBounds();
			float spawnYaw = spawnRegion.getData().getFloat("Yaw");
			float spawnPitch = spawnRegion.getData().getFloat("Pitch");

			TemplateRegion poolRegion = metadata.getFirstRegion(teamKey + "_pool");
			BlockBounds pool = Objects.requireNonNull(poolRegion).getBounds();

			LongSet domain = new LongArraySet();
			metadata.getRegionBounds(teamKey + "_domain").forEach(blockPos -> blockPos.forEach(pos -> domain.add(pos.asLong())));

			return new TheTowersTeamRegion(spawn, pool, domain, spawnYaw, spawnPitch);
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

	public LongSet getDomain() {
		return domain;
	}

	public float getSpawnYaw() {
		return spawnYaw;
	}

	public float getSpawnPitch() {
		return spawnPitch;
	}
}
