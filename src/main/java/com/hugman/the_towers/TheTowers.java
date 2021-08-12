package com.hugman.the_towers;

import com.hugman.the_towers.config.TowersConfig;
import com.hugman.the_towers.game.TowersWaiting;
import com.hugman.the_towers.game.map.parts.GeneratorType;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.nucleoid.plasmid.game.GameType;

public class TheTowers implements ModInitializer {
	private static final String MOD_ID = "the_towers";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static Identifier id(String s) {
		return new Identifier(MOD_ID, s);
	}

	@Override
	public void onInitialize() {
		GeneratorType.register();
		GameType.register(TheTowers.id("the_towers"), TowersConfig.CODEC, TowersWaiting::open);
	}
}
