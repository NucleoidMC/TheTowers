package fr.hugman.plasmid.api.game.team.provider;

import com.mojang.serialization.MapCodec;
import fr.hugman.plasmid.api.registry.PlasmidRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.impl.Plasmid;

public record TeamListProviderType<T extends TeamListProvider>(MapCodec<T> codec) {
    public static final TeamListProviderType<ConstantTeamListProvider> CONSTANT = of("constant", ConstantTeamListProvider.CODEC);
    public static final TeamListProviderType<TrimTeamListProvider> TRIM = of("trim", TrimTeamListProvider.CODEC);
    public static final TeamListProviderType<RandomTeamListProvider> RANDOM = of("random", RandomTeamListProvider.CODEC);
    public static final TeamListProviderType<StandardTeamListProvider> STANDARD = of("standard", StandardTeamListProvider.CODEC);

    private static <T extends TeamListProvider> TeamListProviderType<T> of(String name, MapCodec<T> codec) {
        return of(Identifier.of(Plasmid.ID, name), codec);
    }

    public static <T extends TeamListProvider> TeamListProviderType<T> of(Identifier identifier, MapCodec<T> codec) {
        return Registry.register(PlasmidRegistries.TEAM_LIST_PROVIDER_TYPE, identifier, new TeamListProviderType<>(codec));
    }
}
