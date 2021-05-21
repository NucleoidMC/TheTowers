package com.hugman.the_towers;

import net.fabricmc.api.ModInitializer;
import xyz.nucleoid.plasmid.game.GameType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.hugman.the_towers.config.TheTowersConfig;
import com.hugman.the_towers.game.TheTowersWaiting;

public class TheTowers implements ModInitializer {
    private static final String MOD_ID = "the_towers";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        GameType.register(TheTowers.id("the_towers"), TheTowersWaiting::open, TheTowersConfig.CODEC);
    }

    public static Identifier id(String s) {
        return new Identifier(MOD_ID, s);
    }
}
