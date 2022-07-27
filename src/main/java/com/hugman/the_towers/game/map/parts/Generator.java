package com.hugman.the_towers.game.map.parts;

import net.minecraft.entity.ItemEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.game.GameOpenException;

import java.util.Objects;

public record Generator(GeneratorType type, Vec3d pos) {
	/**
	 * Creates a generator by reading a template region. Can throw a {@link NullPointerException} if the data of the generator is missing.
	 *
	 * @param region the region of the generator
	 */
	public static Generator fromTemplate(TemplateRegion region) {
		try {
			GeneratorType type = Objects.requireNonNull(GeneratorType.get(Identifier.tryParse(region.getData().getString("Type"))));
			Vec3d vec3d = region.getBounds().center();

			return new Generator(type, vec3d);
		}
		catch(NullPointerException e) {
			throw new GameOpenException(Text.translatable("error.the_towers.generator_load"), e);
		}
	}

	public void tick(ServerWorld world, long gameTime) {
		if(gameTime % type.interval() == 0) {
			ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), type.stack().copy());
			itemEntity.setVelocity(0.0D, 0.2D, 0.0D);
			world.spawnEntity(itemEntity);
			world.spawnParticles(ParticleTypes.CLOUD, pos.getX(), pos.getY(), pos.getZ(), 2, 0.0D, 0.0D, 0.0D, 0.0D);
			world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.8F, 0.7F);
		}
		if(world.random.nextFloat() * 2 < (float) (gameTime % type.interval()) / type.interval()) world.spawnParticles(ParticleTypes.SMOKE, pos.getX(), pos.getY(), pos.getZ(), 1, 0.01D, 0.01D, 0.01D, 0.0D);
	}
}
