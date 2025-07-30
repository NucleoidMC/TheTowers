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
import net.minecraft.util.DyeColor;
import net.minecraft.util.Util;
import xyz.nucleoid.plasmid.api.game.common.config.PlayerLimiterConfig;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;
import xyz.nucleoid.plasmid.api.game.config.CustomValuesConfig;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;

import java.util.ArrayList;
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
        registerable.register(TheTowersGameConfigs.CLASSIC_TWO_TEAMS, create(maps.getOrThrow(TheTowersGameMaps.CLASSIC_TWO_TEAMS), TeamAmount.TWO));
        registerable.register(TheTowersGameConfigs.CLASSIC_FOUR_TEAMS, create(maps.getOrThrow(TheTowersGameMaps.CLASSIC_FOUR_TEAMS), TeamAmount.FOUR));
    }

    private static GameConfig<?> create(RegistryEntry<GameMap> map, TeamAmount teamAmount) {
        return new GameConfig<>(
                TheTowersGameTypes.STANDARD,
                Text.translatable("game.generic.mode", Text.translatable("game.the_towers"), Text.translatable(Util.createTranslationKey("game_map", map.getKey().get().getValue()))),
                null, null, new ItemStack(Items.GRASS_BLOCK), CustomValuesConfig.empty(),
                new TowersConfig(
                        new WaitingLobbyConfig(new PlayerLimiterConfig(teamAmount.amount * 8), 1, teamAmount.amount * 4, WaitingLobbyConfig.Countdown.DEFAULT),
                        createTeams(teamAmount.amount), map, 20 / teamAmount.amount
                )
        );
    }

    private enum TeamAmount {
        TWO(2, "two"),
        FOUR(4, "four");

        private final int amount;
        private final String name;

        TeamAmount(int amount, String name) {
            this.amount = amount;
            this.name = name;
        }
    }

    private static GameTeamList createTeams(int amount) {
        var list = new ArrayList<GameTeam>();
        list.add(createTeam(DyeColor.BLUE));
        list.add(createTeam(DyeColor.RED));
        if (amount == 4) {
            list.add(createTeam(DyeColor.YELLOW));
            list.add(createTeam("green", DyeColor.LIME));
        }
        return new GameTeamList(list);
    }

    private static GameTeam createTeam(DyeColor dyeColor) {
        return new GameTeam(
                new GameTeamKey(dyeColor.getId()),
                GameTeamConfig.builder()
                        .setName(Text.translatable("color.minecraft." + dyeColor.getId()))
                        .setColors(GameTeamConfig.Colors.from(dyeColor))
                        .build()
        );
    }


    private static GameTeam createTeam(String name, DyeColor dyeColor) {
        return new GameTeam(
                new GameTeamKey(name),
                GameTeamConfig.builder()
                        .setName(Text.translatable("color.minecraft." + name))
                        .setColors(GameTeamConfig.Colors.from(dyeColor))
                        .build()
        );
    }

}