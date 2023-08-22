package com.hugman.the_towers.map;

import com.hugman.the_towers.registry.TheTowersRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;

import java.util.List;

public record GeneratorConfig(ItemStack stack, long interval) {
    public static final Codec<GeneratorConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("stack").forGetter(GeneratorConfig::stack),
            Codec.LONG.fieldOf("interval").forGetter(GeneratorConfig::interval)
    ).apply(instance, GeneratorConfig::new));

    public static final Codec<RegistryEntry<GeneratorConfig>> REGISTRY_CODEC = RegistryElementCodec.of(TheTowersRegistries.GENERATOR, CODEC);
    public static final Codec<RegistryEntryList<GeneratorConfig>> LIST_CODEC = RegistryCodecs.entryList(TheTowersRegistries.GENERATOR, CODEC);
    public static final Codec<List<RegistryEntryList<GeneratorConfig>>> LISTS_CODEC = RegistryCodecs.entryList(TheTowersRegistries.GENERATOR, CODEC, true).listOf();
}
