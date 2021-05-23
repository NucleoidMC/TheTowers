package com.hugman.the_towers.game;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.player.GameTeam;

public class TheTowersTeam {
	private final Team scoreboardTeam;
	private final GameTeam gameTeam;
	private final Text name;
	public int health;

	TheTowersTeam(Team scoreboardTeam, GameTeam gameTeam, int health) {
		this.scoreboardTeam = scoreboardTeam;
		this.gameTeam = gameTeam;
		this.health = health;

		this.name = new LiteralText(gameTeam.getDisplay()).formatted(Formatting.BOLD, gameTeam.getFormatting());

		this.scoreboardTeam.setFriendlyFireAllowed(false);
		this.scoreboardTeam.setShowFriendlyInvisibles(true);
		this.scoreboardTeam.setCollisionRule(AbstractTeam.CollisionRule.PUSH_OTHER_TEAMS);
		this.scoreboardTeam.setDisplayName(new LiteralText(gameTeam.getDisplay()));
		this.scoreboardTeam.setColor(gameTeam.getFormatting());
	}

	public Team getScoreboardTeam() {
		return scoreboardTeam;
	}

	public GameTeam getGameTeam() {
		return gameTeam;
	}

	public Text getName() {
		return name;
	}

	public DyeColor getDye() {
		return this.gameTeam.getDye();
	}

	public Formatting getFormatting() {
		return this.gameTeam.getFormatting();
	}

	public String getDisplay() {
		return this.gameTeam.getDisplay();
	}

	public ItemStack dye(Item item) {
		return dye(new ItemStack(item));
	}

	public ItemStack dye(ItemStack stack) {
		return this.gameTeam.dye(stack);
	}
}
