package fr.hugman.the_towers.map.generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.hugman.the_towers.registry.TheTowersRegistryKeys;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public record ItemGeneratorConfig(ItemStackTemplate stack, long interval) {
    public static final Codec<ItemGeneratorConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStackTemplate.CODEC.fieldOf("stack").forGetter(ItemGeneratorConfig::stack),
            Codec.LONG.fieldOf("interval").forGetter(ItemGeneratorConfig::interval)
    ).apply(instance, ItemGeneratorConfig::new));

    public static final Codec<Holder<ItemGeneratorConfig>> REGISTRY_CODEC = RegistryFileCodec.create(TheTowersRegistryKeys.GENERATOR, CODEC);
    public static final Codec<HolderSet<ItemGeneratorConfig>> LIST_CODEC = RegistryCodecs.homogeneousList(TheTowersRegistryKeys.GENERATOR, CODEC);
    public static final Codec<List<HolderSet<ItemGeneratorConfig>>> LISTS_CODEC = RegistryCodecs.homogeneousList(TheTowersRegistryKeys.GENERATOR, CODEC, true).listOf();

    public ItemGeneratorConfig(Item item, long interval) {
        this(new ItemStackTemplate(item), interval);
    }
}
