package fr.hugman.the_towers.map;

import fr.hugman.plasmid.api.game.attachment.PlasmidGameAttachments;
import fr.hugman.plasmid.api.game_map.GameMapLoadResult;
import fr.hugman.the_towers.TheTowers;
import fr.hugman.the_towers.config.TowersConfig;
import fr.hugman.the_towers.map.generator.ItemGenerator;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplateMetadata;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenException;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;

import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public record TowersMap(
        Vec3 spawn,
        Vec3 rules,
        List<BlockBounds> protectedBounds,
        List<ItemGenerator> itemGenerators,
        Map<GameTeamKey, TeamRegion> teamRegions,
        RuntimeWorldConfig worldConfig
) {
    /**
     * Creates the map from a map template by reading its metadata.
     */
    public static TowersMap build(GameActivity activity, GameMapLoadResult result) throws GameOpenException {
        MapTemplateMetadata metadata = result.templateMetadata().orElseThrow();
        Vec3 spawn = new Vec3(0, 50, 0);
        BlockBounds spawnBounds = metadata.getFirstRegionBounds("spawn");
        if (spawnBounds != null) {
            spawn = spawnBounds.center();
        } else {
            TheTowers.LOGGER.warn("Missing spawn position, set to default [0 50 0]");
        }
        Vec3 rules = spawn;
        BlockBounds rulesBounds = metadata.getFirstRegionBounds("rules");
        if (rulesBounds != null) {
            rules = rulesBounds.center();
        } else {
            TheTowers.LOGGER.warn("Missing rules display position, set to spawn position");
        }

        List<BlockBounds> protectedBounds = metadata.getRegionBounds("protected").collect(Collectors.toList());
        List<ItemGenerator> itemGenerators = new ArrayList<>();
        Map<GameTeamKey, TeamRegion> teamRegions = new HashMap<>();

        for (TemplateRegion region : metadata.getRegions("generator").toList()) {
            itemGenerators.add(ItemGenerator.fromTemplate(activity, region));
        }

        var teamlist = activity.getGameSpace().getAttachment(PlasmidGameAttachments.TEAM_LIST);

        int i = 0;
        for (GameTeam team : teamlist) {
            try {
                TeamRegion region = TeamRegion.fromTemplate(++i, metadata);
                teamRegions.put(team.key(), region);
            } catch (NullPointerException e) {
                throw new GameOpenException(Component.translatable("error.the_towers.team_region_load", team.key(), i), e);
            }
        }

        var worldConfig = new RuntimeWorldConfig().setGenerator(result.chunkGenerator());

        return new TowersMap(spawn, rules, protectedBounds, itemGenerators, teamRegions, worldConfig);
    }
}
