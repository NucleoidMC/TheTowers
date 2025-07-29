package fr.hugman.the_towers.api.datagen.provider;

import fr.hugman.plasmid.api.author.Author;
import fr.hugman.plasmid.api.game_map.GameMap;
import fr.hugman.plasmid.api.game_map.GameMapMetadata;
import fr.hugman.plasmid.api.game_map.TemplateGameMap;
import fr.hugman.plasmid.api.registry.PlasmidRegistryKeys;
import fr.hugman.the_towers.api.author.TheTowersUUIDs;
import fr.hugman.the_towers.api.game_map.TheTowersGameMaps;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;

import java.util.Arrays;
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
        registerable.register(TheTowersGameMaps.CLASSIC_TWO_TEAMS, ofTemplate(TheTowersGameMaps.CLASSIC_TWO_TEAMS, TheTowersUUIDs.QUIJX));
        registerable.register(TheTowersGameMaps.CLASSIC_FOUR_TEAMS, ofTemplate(TheTowersGameMaps.CLASSIC_FOUR_TEAMS, TheTowersUUIDs.QUIJX, TheTowersUUIDs.HUGMAN));
    }

    private static TemplateGameMap ofTemplate(RegistryKey<GameMap> key, Author... authors) {
        return new TemplateGameMap(new GameMapMetadata(authors), key.getValue());
    }

    private static TemplateGameMap ofTemplate(RegistryKey<GameMap> key, UUID... authors) {
        return ofTemplate(key, Arrays.stream(authors).map(Author::new).toArray(Author[]::new));
    }


}