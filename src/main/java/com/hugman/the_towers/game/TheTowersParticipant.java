package com.hugman.the_towers.game;

import xyz.nucleoid.plasmid.game.player.GameTeam;

public class TheTowersParticipant {
	private final GameTeam team;
	public boolean isRespawning;
	public long ticksUntilRespawn;

	TheTowersParticipant(GameTeam team) {
		this.team = team;
	}

	public GameTeam getTeam() {
		return team;
	}
}
