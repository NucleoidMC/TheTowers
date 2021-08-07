package com.hugman.the_towers.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;

import java.util.List;

public class TheTowersConfig {
	public static final Codec<TheTowersConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
			GameTeam.CODEC.listOf().fieldOf("teams").forGetter(config -> config.teams),
			Identifier.CODEC.fieldOf("map").forGetter(config -> config.map),
			Codec.INT.fieldOf("team_health").forGetter(config -> config.teamHealth),
			Codec.BOOL.fieldOf("stealing").forGetter(config -> config.stealing),
			Codec.LONG.optionalFieldOf("respawn_cooldown", 5L).forGetter(config -> config.respawnCooldown)
	).apply(instance, TheTowersConfig::new));

	private final PlayerConfig playerConfig;
	private final List<GameTeam> teams;
	private final Identifier map;
	private final int teamHealth;
	private final boolean stealing;
	private final long respawnCooldown;

	public TheTowersConfig(PlayerConfig players, List<GameTeam> teams, Identifier map, int teamHealth, boolean stealing, long respawnCooldown) {
		this.playerConfig = players;
		this.teams = teams;
		this.map = map;
		this.teamHealth = teamHealth;
		this.stealing = stealing;
		this.respawnCooldown = respawnCooldown;
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

	public int getTeamHealth() {
		return teamHealth;
	}

	public boolean canSteal() {
		return stealing;
	}

	public long getRespawnCooldown() {
		return respawnCooldown;
	}
}
