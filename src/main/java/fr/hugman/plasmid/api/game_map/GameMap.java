package fr.hugman.plasmid.api.game_map;

import fr.hugman.plasmid.api.registry.PlasmidRegistries;
import fr.hugman.plasmid.api.registry.PlasmidRegistryKeys;
import com.mojang.serialization.Codec;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;

public interface GameMap {
    Codec<GameMap> TYPE_CODEC = PlasmidRegistries.GAME_MAP_TYPE.getCodec().dispatch(GameMap::getType, GameMapType::codec);

    Codec<RegistryEntry<GameMap>> ENTRY_CODEC = RegistryElementCodec.of(PlasmidRegistryKeys.GAME_MAP, TYPE_CODEC);
    Codec<RegistryEntryList<GameMap>> ENTRY_LIST_CODEC = RegistryCodecs.entryList(PlasmidRegistryKeys.GAME_MAP, TYPE_CODEC);

    GameMapLoadResult load(GameOpenContext<?> context);

    GameMapType<?> getType();
}
