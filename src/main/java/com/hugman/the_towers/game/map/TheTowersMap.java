package com.hugman.the_towers.game.map;

import com.hugman.the_towers.TheTowers;
import com.hugman.the_towers.config.TheTowersConfig;
import com.hugman.the_towers.game.map.parts.Generator;
import com.hugman.the_towers.game.map.parts.GeneratorType;
import com.hugman.the_towers.game.map.parts.TeamRegion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateMetadata;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record TheTowersMap(MapTemplate template, Vec3d spawn, Vec3d rules, List<BlockBounds> protectedBounds, List<Generator> generators, Map<GameTeam, TeamRegion> teamRegions) {
	/**
	 * Creates the map from a map template by reading its metadata.
	 */
	public static TheTowersMap fromTemplate(MapTemplate template, TheTowersConfig config) {
		MapTemplateMetadata metadata = template.getMetadata();
		Vec3d spawn = new Vec3d(0, 50, 0);
		BlockBounds spawnBounds = metadata.getFirstRegionBounds("spawn");
		if(spawnBounds != null) {
			spawn = spawnBounds.center();
		}
		else {
			TheTowers.LOGGER.warn("Missing map spawn, set to default [0 50 0]");
		}
		Vec3d rules = spawn;
		BlockBounds rulesBounds = metadata.getFirstRegionBounds("rules");
		if(rulesBounds != null) {
			rules = rulesBounds.center();
		}
		else {
			TheTowers.LOGGER.warn("Missing map rules, set to spawn position");
		}

		List<BlockBounds> protectedBounds = metadata.getRegionBounds("protected").collect(Collectors.toList());
		List<Generator> generators = new ArrayList<>();
		Map<GameTeam, TeamRegion> teamRegions = new HashMap<>();

		GeneratorType ironGeneratorType = new GeneratorType(new ItemStack(Items.IRON_INGOT), 30 * 20);
		for(Vec3d vec3d : metadata.getRegionBounds("iron_generator").map(BlockBounds::center).collect(Collectors.toList())) {
			generators.add(new Generator(ironGeneratorType, vec3d));
		}

		for(GameTeam team : config.teamConfig()) {
			TeamRegion region = TeamRegion.fromTemplate(team, metadata);
			teamRegions.put(team, region);
		}

		return new TheTowersMap(template, spawn, rules, protectedBounds, generators, teamRegions);
	}

	public ChunkGenerator asGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}
