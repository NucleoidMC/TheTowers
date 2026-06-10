package fr.hugman.the_towers.map;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplateMetadata;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.api.game.GameOpenException;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;

import java.util.Objects;

public record TeamRegion(BlockBounds spawn, BlockBounds pool, LongSet domains, float spawnYaw, float spawnPitch) {
    /**
     * Creates a team region by reading the map template's metadata. Can throw a {@link NullPointerException} if the regions are not specified in the map template.
     *
     * @throws NullPointerException
     */
    public static TeamRegion fromTemplate(int i, MapTemplateMetadata metadata) {
            TemplateRegion spawnRegion = metadata.getFirstRegion(i + "_spawn");
            BlockBounds spawn = Objects.requireNonNull(spawnRegion).getBounds();
            float spawnYaw = spawnRegion.getData().getFloatOr("Yaw", 0);
            float spawnPitch = spawnRegion.getData().getFloatOr("Pitch", 0);

            TemplateRegion poolRegion = metadata.getFirstRegion(i + "_pool");
            BlockBounds pool = Objects.requireNonNull(poolRegion).getBounds();

            LongSet domains = new LongArraySet();
            metadata.getRegionBounds(i + "_domain").forEach(blockPos -> blockPos.forEach(pos -> domains.add(pos.asLong())));

            return new TeamRegion(spawn, pool, domains, spawnYaw, spawnPitch);
    }
}
