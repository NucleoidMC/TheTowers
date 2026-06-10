package fr.hugman.the_towers.api.datagen.provider;

import fr.hugman.plasmid.api.game_map.GameMap;
import fr.hugman.plasmid.api.registry.PlasmidRegistryKeys;
import fr.hugman.the_towers.api.game.TheTowersGameConfigs;
import fr.hugman.the_towers.api.game_map.TheTowersGameMaps;
import fr.hugman.the_towers.config.TowersConfig;
import fr.hugman.the_towers.game.TheTowersGameTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import xyz.nucleoid.plasmid.api.game.common.config.PlayerLimiterConfig;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;
import xyz.nucleoid.plasmid.api.game.common.team.provider.TeamListProvider;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;

import java.util.concurrent.CompletableFuture;

public class TheTowersGameProvider extends FabricDynamicRegistryProvider {
    public TheTowersGameProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public String getName() {
        return "Game Configurations";
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        entries.addAll(registries.lookupOrThrow(xyz.nucleoid.plasmid.api.registry.PlasmidRegistryKeys.GAME_CONFIG));
    }

    public static void register(BootstrapContext<GameConfig<?>> registerable) {
        var maps = registerable.lookup(PlasmidRegistryKeys.GAME_MAP);
        var classicMapName = Component.translatable("game_map.the_towers.classic");
        registerable.register(TheTowersGameConfigs.CLASSIC_TWO_TEAMS, create(maps.getOrThrow(TheTowersGameMaps.CLASSIC_TWO_TEAMS), classicMapName, 2));
        registerable.register(TheTowersGameConfigs.CLASSIC_FOUR_TEAMS, create(maps.getOrThrow(TheTowersGameMaps.CLASSIC_FOUR_TEAMS), classicMapName, 4));
    }

    private static GameConfig<?> create(Holder<GameMap> map, Component mapName, int teamAmount) {
        return new GameConfig<>(
                TheTowersGameTypes.STANDARD,
                Component.translatable("game.generic.mode", Component.translatable("game.the_towers"),
                        mapName.copy()
                                .append(" (")
                                .append(Component.translatable("game.generic.teams", teamAmount))
                                .append(")")),
                Component.translatable("game.the_towers"),
                null, new ItemStackTemplate(Items.GRASS_BLOCK), CustomValuesConfig.empty(),
                new TowersConfig(
                        new WaitingLobbyConfig(new PlayerLimiterConfig(teamAmount * 8), 1, teamAmount * 4, WaitingLobbyConfig.Countdown.DEFAULT),
                        TeamListProvider.of(teamAmount), map, 20 / teamAmount
                )
        );
    }
}