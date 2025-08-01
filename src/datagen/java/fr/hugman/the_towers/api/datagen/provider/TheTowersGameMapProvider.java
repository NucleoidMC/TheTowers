package fr.hugman.the_towers.api.datagen.provider;

import fr.hugman.plasmid.api.author.Author;
import fr.hugman.plasmid.api.game_map.GameMap;
import fr.hugman.plasmid.api.game_map.GameMapMetadata;
import fr.hugman.plasmid.api.game_map.TemplateGameMap;
import fr.hugman.plasmid.api.game_map.template.processor.TeamColorMapTemplateProcessor;
import fr.hugman.plasmid.api.registry.PlasmidRegistryKeys;
import fr.hugman.the_towers.TheTowers;
import fr.hugman.the_towers.api.author.TheTowersUUIDs;
import fr.hugman.the_towers.api.game_map.TheTowersGameMaps;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TheTowersGameMapProvider extends FabricDynamicRegistryProvider {
    public TheTowersGameMapProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public String getName() {
        return "Game Maps";
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        entries.addAll(registries.getOrThrow(PlasmidRegistryKeys.GAME_MAP));
    }

    public static void register(Registerable<GameMap> registerable) {
        var classicMetadata = new GameMapMetadata(
                Text.translatable("game_map.the_towers.classic"),
                new Author(TheTowersUUIDs.QUIJX),
                new Author(TheTowersUUIDs.HUGMAN)
        );
        registerable.register(TheTowersGameMaps.CLASSIC_TWO_TEAMS, new TemplateGameMap(
                TheTowers.id("classic/two_teams"), classicMetadata, new TeamColorMapTemplateProcessor(List.of(DyeColor.BLUE, DyeColor.RED))
        ));
        registerable.register(TheTowersGameMaps.CLASSIC_FOUR_TEAMS, new TemplateGameMap(
                TheTowers.id("classic/four_teams"), classicMetadata, new TeamColorMapTemplateProcessor(List.of(DyeColor.BLUE, DyeColor.RED, DyeColor.LIME, DyeColor.YELLOW))
        ));
    }
}
