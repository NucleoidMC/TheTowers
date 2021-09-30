package com.hugman.the_towers.game.map;

import com.hugman.the_towers.TheTowers;
import com.hugman.the_towers.config.TowersConfig;
import com.hugman.the_towers.game.map.parts.Generator;
import com.hugman.the_towers.game.map.parts.TeamRegion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateMetadata;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record TowersMap(MapTemplate template, Vec3d spawn, Vec3d rules, List<BlockBounds> protectedBounds, List<Generator> generators, Map<GameTeamKey, TeamRegion> teamRegions) {
	/**
	 * Creates the map from a map template by reading its metadata.
	 */
	public static TowersMap fromTemplate(MapTemplate template, TowersConfig config) {
		MapTemplateMetadata metadata = template.getMetadata();
		Vec3d spawn = new Vec3d(0, 50, 0);
		BlockBounds spawnBounds = metadata.getFirstRegionBounds("spawn");
		if(spawnBounds != null) {
			spawn = spawnBounds.center();
		}
		else {
			TheTowers.LOGGER.warn("Missing spawn position, set to default [0 50 0]");
		}
		Vec3d rules = spawn;
		BlockBounds rulesBounds = metadata.getFirstRegionBounds("rules");
		if(rulesBounds != null) {
			rules = rulesBounds.center();
		}
		else {
			TheTowers.LOGGER.warn("Missing rules display position, set to spawn position");
		}

		List<BlockBounds> protectedBounds = metadata.getRegionBounds("protected").collect(Collectors.toList());
		List<Generator> generators = new ArrayList<>();
		Map<GameTeamKey, TeamRegion> teamRegions = new HashMap<>();

		for(TemplateRegion region : metadata.getRegions("generator").collect(Collectors.toList())) {
			generators.add(Generator.fromTemplate(region));
		}

		for(GameTeam team : config.teamConfig()) {
			TeamRegion region = TeamRegion.fromTemplate(team.key(), metadata);
			teamRegions.put(team.key(), region);
		}

		return new TowersMap(template, spawn, rules, protectedBounds, generators, teamRegions);
	}

	public ChunkGenerator asGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}
