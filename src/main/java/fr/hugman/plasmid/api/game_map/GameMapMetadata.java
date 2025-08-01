package fr.hugman.plasmid.api.game_map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.hugman.plasmid.api.author.Author;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.api.util.PlasmidCodecs;

import java.util.List;
import java.util.Optional;

public record GameMapMetadata(
        Optional<Text> name,
        List<Author> authors
) {
    public static final Codec<GameMapMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlasmidCodecs.TEXT.optionalFieldOf("name").forGetter(GameMapMetadata::name),
            Author.CODEC.listOf().fieldOf("authors").forGetter(GameMapMetadata::authors)
    ).apply(instance, GameMapMetadata::new));

    public GameMapMetadata(Text name, Author... authors) {
        this(Optional.of(name), List.of(authors));
    }
}
