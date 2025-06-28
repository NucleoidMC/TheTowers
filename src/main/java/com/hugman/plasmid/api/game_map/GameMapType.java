package com.hugman.plasmid.api.game_map;

import com.hugman.plasmid.api.registry.PlasmidRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.impl.Plasmid;

public record GameMapType<T extends GameMap>(MapCodec<T> codec) {
    public static final GameMapType<TemplateGameMap> TEMPLATE = of("template", TemplateGameMap.CODEC);

    private static <T extends GameMap> GameMapType<T> of(String name, MapCodec<T> codec) {
        return of(Identifier.of(Plasmid.ID, name), codec);
    }

    public static <T extends GameMap> GameMapType<T> of(Identifier identifier, MapCodec<T> codec) {
        return Registry.register(PlasmidRegistries.GAME_MAP_TYPE, identifier, new GameMapType<>(codec));
    }
}
