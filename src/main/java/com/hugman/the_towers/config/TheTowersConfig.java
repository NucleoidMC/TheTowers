package com.hugman.the_towers.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.player.GameTeam;

import java.util.List;

public class TheTowersConfig {
	public static final Codec<TheTowersConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
			GameTeam.CODEC.listOf().fieldOf("teams").forGetter(config -> config.teams),
			Identifier.CODEC.fieldOf("map").forGetter(config -> config.map)
	).apply(instance, TheTowersConfig::new));

	private final PlayerConfig playerConfig;
	private final List<GameTeam> teams;
	private final Identifier map;

	public TheTowersConfig(PlayerConfig players, List<GameTeam> teams, Identifier map) {
		this.playerConfig = players;
		this.teams = teams;
		this.map = map;
	}

	public PlayerConfig getPlayerConfig() {
		return playerConfig;
	}

	public List<GameTeam> getTeams() {
		return teams;
	}

	public Identifier getMap() {
		return map;
	}
}
