package fr.hugman.plasmid.api.game_map;

import com.mojang.serialization.Codec;
import fr.hugman.plasmid.api.registry.PlasmidRegistries;
import fr.hugman.plasmid.api.registry.PlasmidRegistryKeys;
import xyz.nucleoid.plasmid.api.game.GameActivity;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;

public interface GameMap {
    Codec<GameMap> TYPE_CODEC = PlasmidRegistries.GAME_MAP_TYPE.byNameCodec().dispatch(GameMap::getType, GameMapType::codec);

    Codec<Holder<GameMap>> ENTRY_CODEC = RegistryFileCodec.create(PlasmidRegistryKeys.GAME_MAP, TYPE_CODEC);
    Codec<HolderSet<GameMap>> ENTRY_LIST_CODEC = RegistryCodecs.homogeneousList(PlasmidRegistryKeys.GAME_MAP, TYPE_CODEC);

    <Config> GameMapLoadResult load(GameActivity activity, Config config);

    GameMapType<?> getType();

    Optional<GameMapMetadata> getMetadata();
}
