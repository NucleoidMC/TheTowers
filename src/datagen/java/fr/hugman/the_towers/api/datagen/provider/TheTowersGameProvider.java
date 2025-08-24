package fr.hugman.the_towers.api.datagen.provider;

import fr.hugman.plasmid.api.game_map.GameMap;
import fr.hugman.plasmid.api.registry.PlasmidRegistryKeys;
import fr.hugman.the_towers.api.game.TheTowersGameConfigs;
import fr.hugman.the_towers.api.game_map.TheTowersGameMaps;
import fr.hugman.the_towers.config.TowersConfig;
import fr.hugman.the_towers.game.TheTowersGameTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.api.game.common.config.PlayerLimiterConfig;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;
import xyz.nucleoid.plasmid.api.game.common.team.provider.TeamListProvider;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;

import java.util.concurrent.CompletableFuture;

public class TheTowersGameProvider extends FabricDynamicRegistryProvider {
    public TheTowersGameProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public String getName() {
        return "Game Configurations";
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        entries.addAll(registries.getOrThrow(PlasmidRegistryKeys.GAME_CONFIG));
    }

    public static void register(Registerable<GameConfig<?>> registerable) {
        var maps = registerable.getRegistryLookup(PlasmidRegistryKeys.GAME_MAP);
        var classicMapName = Text.translatable("game_map.the_towers.classic");
        registerable.register(TheTowersGameConfigs.CLASSIC_TWO_TEAMS, create(maps.getOrThrow(TheTowersGameMaps.CLASSIC_TWO_TEAMS), classicMapName, 2));
        registerable.register(TheTowersGameConfigs.CLASSIC_FOUR_TEAMS, create(maps.getOrThrow(TheTowersGameMaps.CLASSIC_FOUR_TEAMS), classicMapName, 4));
    }

    private static GameConfig<?> create(RegistryEntry<GameMap> map, Text mapName, int teamAmount) {
        return new GameConfig<>(
                TheTowersGameTypes.STANDARD,
                Text.translatable("game.generic.mode", Text.translatable("game.the_towers"),
                        mapName.copy()
                                .append(" (")
                                .append(Text.translatable("game.generic.teams", teamAmount))
                                .append(")")),
                Text.translatable("game.the_towers"),
                null, new ItemStack(Items.GRASS_BLOCK), CustomValuesConfig.empty(),
                new TowersConfig(
                        new WaitingLobbyConfig(new PlayerLimiterConfig(teamAmount * 8), 1, teamAmount * 4, WaitingLobbyConfig.Countdown.DEFAULT),
                        TeamListProvider.of(teamAmount), map, 20 / teamAmount
                )
        );
    }
}