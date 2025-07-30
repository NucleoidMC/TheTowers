package fr.hugman.plasmid.api.game.team.provider;

import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;

import java.util.List;

public record RandomTeamListProvider(int size) implements TeamListProvider {
    private static final GameTeam BLUE = createTeam(DyeColor.BLUE);
    private static final GameTeam GREEN = createTeam(DyeColor.GREEN);
    private static final GameTeam YELLOW = createTeam(DyeColor.YELLOW);
    private static final GameTeam ORANGE = createTeam(DyeColor.ORANGE);
    private static final GameTeam RED = createTeam(DyeColor.RED);
    private static final GameTeam BROWN = createTeam(DyeColor.BROWN);

    private static final GameTeam LIME = createTeam(DyeColor.LIME);
    private static final GameTeam LIGHT_BLUE = createTeam(DyeColor.LIGHT_BLUE);
    private static final GameTeam PINK = createTeam(DyeColor.PINK);
    private static final GameTeam PURPLE = createTeam(DyeColor.PURPLE);

    private static final GameTeam CYAN = createTeam(DyeColor.CYAN);
    private static final GameTeam MAGENTA = createTeam(DyeColor.MAGENTA);

    private static final GameTeam WHITE = createTeam(DyeColor.WHITE);
    private static final GameTeam BLACK = createTeam(DyeColor.BLACK);

    private static final GameTeam GRAY = createTeam(DyeColor.GRAY);
    private static final GameTeam LIGHT_GRAY = createTeam(DyeColor.LIGHT_GRAY);

    private static final GameTeam LIGHT_BLUE_AS_BLUE = createTeam("blue", DyeColor.LIGHT_BLUE);
    private static final GameTeam LIME_AS_GREEN = createTeam("green", DyeColor.LIME);
    private static final GameTeam MAGENTA_AS_PURPLE = createTeam("purple", DyeColor.MAGENTA);

    private static final List<List<GameTeam>> RANDOM_TWO = List.of(
            List.of(RED, BLUE),
            List.of(RED, YELLOW),
            List.of(LIME_AS_GREEN, BLUE),
            List.of(LIME_AS_GREEN, YELLOW),
            List.of(RED, LIME_AS_GREEN),
            List.of(LIME_AS_GREEN, PINK),
            List.of(MAGENTA_AS_PURPLE, YELLOW),
            List.of(YELLOW, LIGHT_BLUE_AS_BLUE),
            List.of(MAGENTA_AS_PURPLE, ORANGE),
            List.of(WHITE, BLACK)
    );

    private static final List<List<GameTeam>> RANDOM_FOUR = List.of(
            List.of(RED, BLUE, LIME_AS_GREEN, YELLOW),
            List.of(LIME_AS_GREEN, ORANGE, PINK, LIGHT_BLUE_AS_BLUE)
    );

    private static final List<GameTeam> POOL_SMALLEST = List.of(LIGHT_BLUE_AS_BLUE, LIME_AS_GREEN, YELLOW, RED);
    private static final List<GameTeam> POOL_FIVE = List.of(LIGHT_BLUE_AS_BLUE, LIME_AS_GREEN, YELLOW, RED, MAGENTA_AS_PURPLE);
    private static final List<GameTeam> POOL_SEVEN = List.of(LIGHT_BLUE_AS_BLUE, LIME_AS_GREEN, YELLOW, ORANGE, RED, MAGENTA_AS_PURPLE, BROWN);
    private static final List<GameTeam> POOL_TEN = List.of(BLUE, LIGHT_BLUE, GREEN, LIME, YELLOW, ORANGE, RED, BROWN, PINK, PURPLE);
    private static final List<GameTeam> POOL_TWELVE = List.of(BLUE, CYAN, LIGHT_BLUE, GREEN, LIME, YELLOW, ORANGE, RED, BROWN, PINK, MAGENTA, PURPLE);
    private static final List<GameTeam> POOL_FOURTEEN = List.of(BLUE, CYAN, LIGHT_BLUE, GREEN, LIME, YELLOW, ORANGE, RED, BROWN, PINK, MAGENTA, PURPLE, WHITE, BLACK);
    private static final List<GameTeam> POOL_BIGGEST = List.of(BLUE, CYAN, LIGHT_BLUE, GREEN, LIME, YELLOW, ORANGE, RED, BROWN, PINK, MAGENTA, PURPLE, WHITE, LIGHT_GRAY, GRAY, BLACK);

    @Override
    public GameTeamList get(Random random) {
        if (size <= 0) {
            throw new IllegalArgumentException("Team list cannot be empty. Please provide a valid size between 0 and 16.");
        }
        if (size > 16) {
            throw new IllegalArgumentException("Team list cannot be over 16. Please provide a valid size between than 0 and 16.");
        }
        if (size == 2) {
            return new GameTeamList(RANDOM_TWO.get(random.nextInt(RANDOM_TWO.size())));
        }
        if (size == 4) {
            return new GameTeamList(RANDOM_FOUR.get(random.nextInt(RANDOM_FOUR.size())));
        }
        if (size > 14) {
            return ofPool(POOL_BIGGEST, random);
        }
        if (size > 12) {
            return ofPool(POOL_FOURTEEN, random);
        }
        if (size > 10) {
            return ofPool(POOL_TWELVE, random);
        }
        if (size > 7) {
            return ofPool(POOL_TEN, random);
        }
        if (size > 5) {
            return ofPool(POOL_SEVEN, random);
        }
        if (size > 4) {
            return ofPool(POOL_FIVE, random);
        }
        return ofPool(POOL_SMALLEST, random);
    }

    private GameTeamList ofPool(List<GameTeam> pool, Random random) {
        pool = pool.stream().sorted((a, b) -> random.nextInt(2) - 1).toList();
        return new GameTeamList(pool.subList(0, Math.min(size, pool.size())));
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
