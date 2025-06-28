package com.hugman.plasmid.api.game_map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GameMapMetadata(
        String author,
        String description
) {
    public static final MapCodec<GameMapMetadata> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("author").forGetter(GameMapMetadata::author),
            Codec.STRING.fieldOf("description").forGetter(GameMapMetadata::description)
    ).apply(instance, GameMapMetadata::new));
}
