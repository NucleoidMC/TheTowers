package fr.hugman.plasmid.api.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.Uuids;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

public class PlasmidCodecs {
    public static final Codec<PlayerRef> PLAYER_REF = Uuids.STRICT_CODEC.comapFlatMap(uuid -> DataResult.success(PlayerRef.ofUnchecked(uuid)), PlayerRef::id);
}
