package com.hugman.the_towers.game;

public class TheTowersTeam {
	private int health;

	TheTowersTeam(int health) {
		this.health = health;
	}

	public int getHealth() {
		return health;
	}

	public void addHealth() {
		health++;
	}

	public void removeHealth() {
		health--;
	}
}
