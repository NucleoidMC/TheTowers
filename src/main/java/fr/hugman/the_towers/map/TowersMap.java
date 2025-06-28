package fr.hugman.the_towers.map;

import fr.hugman.the_towers.TheTowers;
import fr.hugman.the_towers.config.TowersConfig;
import fr.hugman.plasmid.api.game_map.GameMapLoadResult;
import fr.hugman.the_towers.map.generator.Generator;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplateMetadata;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenException;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record TowersMap(
        Vec3d spawn,
        Vec3d rules,
        List<BlockBounds> protectedBounds,
        List<Generator> generators,
        Map<GameTeamKey, TeamRegion> teamRegions,
        RuntimeWorldConfig worldConfig
) {
    /**
     * Creates the map from a map template by reading its metadata.
     */
    public static TowersMap build(GameOpenContext<TowersConfig> context, GameMapLoadResult result) throws GameOpenException {
        var config = context.config();

        MapTemplateMetadata metadata = result.templateMetadata().orElseThrow();
        Vec3d spawn = new Vec3d(0, 50, 0);
        BlockBounds spawnBounds = metadata.getFirstRegionBounds("spawn");
        if (spawnBounds != null) {
            spawn = spawnBounds.center();
        } else {
            TheTowers.LOGGER.warn("Missing spawn position, set to default [0 50 0]");
        }
        Vec3d rules = spawn;
        BlockBounds rulesBounds = metadata.getFirstRegionBounds("rules");
        if (rulesBounds != null) {
            rules = rulesBounds.center();
        } else {
            TheTowers.LOGGER.warn("Missing rules display position, set to spawn position");
        }

        List<BlockBounds> protectedBounds = metadata.getRegionBounds("protected").collect(Collectors.toList());
        List<Generator> generators = new ArrayList<>();
        Map<GameTeamKey, TeamRegion> teamRegions = new HashMap<>();

        for (TemplateRegion region : metadata.getRegions("generator").toList()) {
            generators.add(Generator.fromTemplate(context, region));
        }

        for (GameTeam team : config.teamConfig()) {
            TeamRegion region = TeamRegion.fromTemplate(team.key(), metadata);
            teamRegions.put(team.key(), region);
        }

        var worldConfig = new RuntimeWorldConfig().setGenerator(result.chunkGenerator(context.server()));

        return new TowersMap(spawn, rules, protectedBounds, generators, teamRegions, worldConfig);
    }
}
