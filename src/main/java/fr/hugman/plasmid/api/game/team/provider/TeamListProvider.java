package fr.hugman.plasmid.api.game.team.provider;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import fr.hugman.plasmid.api.registry.PlasmidRegistries;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProviderType;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;

public abstract class TeamListProvider {
    private static final Codec<TeamListProvider> BASE_CODEC = PlasmidRegistries.TEAM_LIST_PROVIDER_TYPE.getCodec().dispatch(TeamListProvider::getType, TeamListProviderType::codec);
    private static final Codec<TeamListProvider> INLINE_LIST_CODEC = Codec.either(GameTeamList.CODEC, BASE_CODEC).xmap(
            either -> either.map(TeamListProvider::of, provider -> provider),
            provider -> provider.getType() == TeamListProviderType.CONSTANT ?
                    Either.left(new GameTeamList(((ConstantTeamListProvider) provider).teams())) :
                    Either.right(provider)
    );
    public static final Codec<TeamListProvider> TYPE_CODEC = Codec.either(Codec.INT, INLINE_LIST_CODEC).xmap(
            either -> either.map(TeamListProvider::of, provider -> provider),
            provider -> {
                if (provider.getType() == TeamListProviderType.STANDARD) {
                    var size = ((StandardTeamListProvider) provider).size();
                    if (size.getType() == IntProviderType.CONSTANT) {
                        return Either.left(((ConstantIntProvider) size).getValue());
                    }
                }
                return Either.right(provider);
            }
    );

    public abstract GameTeamList get(Random random);

    public abstract TeamListProviderType<?> getType();

    public static TeamListProvider of(GameTeamList teams) {
        return new ConstantTeamListProvider(teams.list());
    }

    public static TeamListProvider of(int size) {
        return new StandardTeamListProvider(ConstantIntProvider.create(size));
    }
}
