package com.hugman.the_towers.game.map.parts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;

public record GeneratorType(ItemStack stack, long interval) {
	public static final Codec<GeneratorType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ItemStack.CODEC.fieldOf("stack").forGetter(GeneratorType::stack),
			Codec.LONG.fieldOf("interval").forGetter(GeneratorType::interval)
	).apply(instance, GeneratorType::new));
}
