package fr.hugman.the_towers.map.generator;

import fr.hugman.the_towers.config.TowersConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenException;

public record ItemGenerator(ItemGeneratorConfig type, Vec3 pos) {
    public static final String CONFIG_KEY = "Type"; // we should use something else

    /**
     * Creates a generator by reading a template region. Can throw a {@link NullPointerException} if the data of the generator is missing.
     *
     * @param region the region of the generator
     */
    public static ItemGenerator fromTemplate(GameActivity activity, TemplateRegion region) {
        var data = region.getData();
        if (!data.contains(CONFIG_KEY)) {
            throw new GameOpenException(Component.translatable("error.the_towers.generator.empty_config"));
        }

        var ops = RegistryOps.create(NbtOps.INSTANCE, activity.getGameSpace().getServer().registryAccess());
        var result = ItemGeneratorConfig.REGISTRY_CODEC.parse(ops, data.get(CONFIG_KEY));

        if (result.error().isPresent()) {
            throw new GameOpenException(Component.translatable("error.the_towers.generator.invalid_config"), new IllegalArgumentException(result.error().get().toString()));
        }

        return result.result().map(entry -> {
            Vec3 vec3d = region.getBounds().center();
            return new ItemGenerator(entry.value(), vec3d);
        }).orElseThrow(() -> result.error().map(error -> new GameOpenException(Component.nullToEmpty(error.toString()))).orElse(new GameOpenException(Component.literal("Failed to decode The Towers generator config"))));
    }

    public void tick(ServerLevel world, long gameTime) {
        if (gameTime % type.interval() == 0) {
            ItemEntity itemEntity = new ItemEntity(world, pos.x(), pos.y(), pos.z(), type.stack().copy());
            itemEntity.setDeltaMovement(0.0D, 0.2D, 0.0D);
            world.addFreshEntity(itemEntity);
            world.sendParticles(ParticleTypes.CLOUD, pos.x(), pos.y(), pos.z(), 2, 0.0D, 0.0D, 0.0D, 0.0D);
            world.playSound(null, pos.x(), pos.y(), pos.z(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8F, 0.7F);
        }
        if (world.random.nextFloat() * 2 < (float) (gameTime % type.interval()) / type.interval())
            world.sendParticles(ParticleTypes.SMOKE, pos.x(), pos.y(), pos.z(), 1, 0.01D, 0.01D, 0.01D, 0.0D);
    }
}
