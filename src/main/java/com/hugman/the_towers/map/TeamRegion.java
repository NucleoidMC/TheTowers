package com.hugman.the_towers.map;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.text.Text;
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
     * @param teamKey the team's key which needs its regions to be found
     */
    public static TeamRegion fromTemplate(GameTeamKey teamKey, MapTemplateMetadata metadata) {
        try {
            TemplateRegion spawnRegion = metadata.getFirstRegion(teamKey.id() + "_spawn");
            BlockBounds spawn = Objects.requireNonNull(spawnRegion).getBounds();
            float spawnYaw = spawnRegion.getData().getFloat("Yaw");
            float spawnPitch = spawnRegion.getData().getFloat("Pitch");

            TemplateRegion poolRegion = metadata.getFirstRegion(teamKey.id() + "_pool");
            BlockBounds pool = Objects.requireNonNull(poolRegion).getBounds();

            LongSet domains = new LongArraySet();
            metadata.getRegionBounds(teamKey + "_domain").forEach(blockPos -> blockPos.forEach(pos -> domains.add(pos.asLong())));

            return new TeamRegion(spawn, pool, domains, spawnYaw, spawnPitch);
        } catch (NullPointerException e) {
            throw new GameOpenException(Text.translatable("error.the_towers.team_region_load", teamKey.id()), e);
        }
    }
}
