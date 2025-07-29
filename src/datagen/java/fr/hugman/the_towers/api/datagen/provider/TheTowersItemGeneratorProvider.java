package fr.hugman.the_towers.api.datagen.provider;

import fr.hugman.the_towers.api.map.generator.TheTowersGenerators;
import fr.hugman.the_towers.map.generator.ItemGeneratorConfig;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.SharedConstants;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class TheTowersItemGeneratorProvider extends FabricDynamicRegistryProvider {
    public TheTowersItemGeneratorProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public String getName() {
        return "The Towers Item Generator Configurations";
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        var baseCooldown = SharedConstants.TICKS_PER_SECOND * 15;
        entries.add(TheTowersGenerators.IRON_LOW, new ItemGeneratorConfig(Items.IRON_INGOT, baseCooldown * 2));
        entries.add(TheTowersGenerators.IRON_MEDIUM, new ItemGeneratorConfig(Items.IRON_INGOT, baseCooldown));
        entries.add(TheTowersGenerators.IRON_HIGH, new ItemGeneratorConfig(Items.IRON_INGOT, baseCooldown / 2));
    }
}