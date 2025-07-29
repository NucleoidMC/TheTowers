package fr.hugman.plasmid.api.game_map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.hugman.plasmid.api.author.Author;

import java.util.List;

public record GameMapMetadata(
        List<Author> authors
) {
    public static final Codec<GameMapMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Author.CODEC.listOf().fieldOf("authors").forGetter(GameMapMetadata::authors)
    ).apply(instance, GameMapMetadata::new));

    public GameMapMetadata(Author... authors) {
        this(List.of(authors));
    }
}
