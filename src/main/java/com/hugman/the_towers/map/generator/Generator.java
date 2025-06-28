package com.hugman.the_towers.map.generator;

import com.hugman.the_towers.config.TowersConfig;
import net.minecraft.entity.ItemEntity;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenException;

public record Generator(GeneratorConfig type, Vec3d pos) {
    public static final String CONFIG_KEY = "Type"; // we should use something else

    /**
     * Creates a generator by reading a template region. Can throw a {@link NullPointerException} if the data of the generator is missing.
     *
     * @param region the region of the generator
     */
    public static Generator fromTemplate(GameOpenContext<TowersConfig> context, TemplateRegion region) {
        var data = region.getData();
        if (!data.contains(CONFIG_KEY)) {
            throw new GameOpenException(Text.translatable("error.the_towers.generator.empty_config"));
        }

        var ops = RegistryOps.of(NbtOps.INSTANCE, context.server().getRegistryManager());
        var result = GeneratorConfig.REGISTRY_CODEC.parse(ops, data.get(CONFIG_KEY));

        if (result.error().isPresent()) {
            throw new GameOpenException(Text.translatable("error.the_towers.generator.invalid_config"), new IllegalArgumentException(result.error().get().toString()));
        }

        return result.result().map(entry -> {
            Vec3d vec3d = region.getBounds().center();
            return new Generator(entry.value(), vec3d);
        }).orElseThrow(() -> result.error().map(error -> new GameOpenException(Text.of(error.toString()))).orElse(new GameOpenException(Text.literal("Failed to decode The Towers generator config"))));
    }

    public void tick(ServerWorld world, long gameTime) {
        if (gameTime % type.interval() == 0) {
            ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), type.stack().copy());
            itemEntity.setVelocity(0.0D, 0.2D, 0.0D);
            world.spawnEntity(itemEntity);
            world.spawnParticles(ParticleTypes.CLOUD, pos.getX(), pos.getY(), pos.getZ(), 2, 0.0D, 0.0D, 0.0D, 0.0D);
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.8F, 0.7F);
        }
        if (world.random.nextFloat() * 2 < (float) (gameTime % type.interval()) / type.interval())
            world.spawnParticles(ParticleTypes.SMOKE, pos.getX(), pos.getY(), pos.getZ(), 1, 0.01D, 0.01D, 0.01D, 0.0D);
    }
}
