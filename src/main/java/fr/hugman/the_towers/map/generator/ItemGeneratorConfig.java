package fr.hugman.the_towers.map.generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.hugman.the_towers.registry.TheTowersRegistryKeys;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;

import java.util.List;

public record ItemGeneratorConfig(ItemStack stack, long interval) {
    public static final Codec<ItemGeneratorConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.UNCOUNTED_CODEC.fieldOf("stack").forGetter(ItemGeneratorConfig::stack),
            Codec.LONG.fieldOf("interval").forGetter(ItemGeneratorConfig::interval)
    ).apply(instance, ItemGeneratorConfig::new));

    public static final Codec<RegistryEntry<ItemGeneratorConfig>> REGISTRY_CODEC = RegistryElementCodec.of(TheTowersRegistryKeys.GENERATOR, CODEC);
    public static final Codec<RegistryEntryList<ItemGeneratorConfig>> LIST_CODEC = RegistryCodecs.entryList(TheTowersRegistryKeys.GENERATOR, CODEC);
    public static final Codec<List<RegistryEntryList<ItemGeneratorConfig>>> LISTS_CODEC = RegistryCodecs.entryList(TheTowersRegistryKeys.GENERATOR, CODEC, true).listOf();

    public ItemGeneratorConfig(Item item, long interval) {
        this(new ItemStack(item), interval);
    }
}
