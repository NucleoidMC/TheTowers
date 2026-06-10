package fr.hugman.plasmid.api.game_map;

import com.mojang.serialization.MapCodec;
import fr.hugman.plasmid.api.registry.PlasmidRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import xyz.nucleoid.plasmid.impl.Plasmid;

public record GameMapType<T extends GameMap>(MapCodec<T> codec) {
    public static final GameMapType<TemplateGameMap> TEMPLATE = of("template", TemplateGameMap.CODEC);

    private static <T extends GameMap> GameMapType<T> of(String name, MapCodec<T> codec) {
        return of(ResourceLocation.fromNamespaceAndPath(Plasmid.ID, name), codec);
    }

    public static <T extends GameMap> GameMapType<T> of(ResourceLocation identifier, MapCodec<T> codec) {
        return Registry.register(PlasmidRegistries.GAME_MAP_TYPE, identifier, new GameMapType<>(codec));
    }
}
