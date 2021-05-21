package com.hugman.the_towers;

import net.fabricmc.api.ModInitializer;
import xyz.nucleoid.plasmid.game.GameType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.hugman.the_towers.game.TheTowersConfig;
import com.hugman.the_towers.game.TheTowersWaiting;

public class TheTowers implements ModInitializer {

    public static final String ID = "the_towers";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final GameType<TheTowersConfig> TYPE = GameType.register(
            new Identifier(ID, "the_towers"),
            TheTowersWaiting::open,
            TheTowersConfig.CODEC
    );

    @Override
    public void onInitialize() {}
}
