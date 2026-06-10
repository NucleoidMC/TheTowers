package fr.hugman.the_towers.game;

import com.mojang.serialization.MapCodec;
import fr.hugman.the_towers.TheTowers;
import fr.hugman.the_towers.config.TowersConfig;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.api.game.GameTypes;

public class TheTowersGameTypes {
    public static final GameType<TowersConfig> STANDARD = of("standard", TowersConfig.CODEC, TowersWaiting::open);

    public static <C> GameType<C> of(String path, MapCodec<C> configCodec, GameType.Open<C> open) {
        return GameTypes.register(TheTowers.id(path), configCodec, open);
    }
}
