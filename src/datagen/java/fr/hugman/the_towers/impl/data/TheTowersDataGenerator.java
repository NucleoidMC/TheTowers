package fr.hugman.the_towers.impl.data;

import fr.hugman.plasmid.api.registry.PlasmidRegistryKeys;
import fr.hugman.the_towers.TheTowers;
import fr.hugman.the_towers.api.datagen.provider.TheTowersGameMapProvider;
import fr.hugman.the_towers.api.datagen.provider.TheTowersGameProvider;
import fr.hugman.the_towers.api.datagen.provider.TheTowersItemGeneratorProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import org.jetbrains.annotations.Nullable;

public class TheTowersDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        // Plasmid
        pack.addProvider(TheTowersGameProvider::new);
        pack.addProvider(TheTowersGameMapProvider::new);

        // The Towers
        pack.addProvider(TheTowersItemGeneratorProvider::new);
    }

    @Override
    public void buildRegistry(RegistryBuilder registryBuilder) {
        registryBuilder.addRegistry(PlasmidRegistryKeys.GAME_MAP, TheTowersGameMapProvider::register);
        registryBuilder.addRegistry(PlasmidRegistryKeys.GAME_CONFIG, TheTowersGameProvider::register);
    }

    @Override
    @Nullable
    public String getEffectiveModId() {
        return TheTowers.MOD_ID;
    }
}
