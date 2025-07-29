package fr.hugman.plasmid.api.author;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.hugman.plasmid.api.codec.PlasmidCodecs;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

import java.util.Optional;
import java.util.UUID;

public record Author(
        PlayerRef player,
        Optional<String> description
) {
    private static final Codec<Author> NO_DESCRIPTION_CODEC = PlasmidCodecs.PLAYER_REF.comapFlatMap(playerRef -> DataResult.success(new Author(playerRef)), Author::player);
    private static final Codec<Author> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlasmidCodecs.PLAYER_REF.fieldOf("player").forGetter(Author::player),
            Codec.STRING.optionalFieldOf("description").forGetter(Author::description)
    ).apply(instance, Author::new));

    public static final Codec<Author> CODEC = Codec.withAlternative(NO_DESCRIPTION_CODEC, FULL_CODEC);

    public Author(PlayerRef player) {
        this(player, Optional.empty());
    }

    public Author(UUID uuid, String description) {
        this(PlayerRef.ofUnchecked(uuid), Optional.of(description));
    }

    public Author(UUID uuid) {
        this(PlayerRef.ofUnchecked(uuid));
    }
}
