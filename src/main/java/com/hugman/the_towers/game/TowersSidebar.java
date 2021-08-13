package com.hugman.the_towers.game;

import com.hugman.the_towers.util.FormattingUtil;
import com.hugman.the_towers.util.TickUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.widget.SidebarWidget;

public record TowersSidebar(SidebarWidget sidebarWidget) {
	public static TowersSidebar create(GlobalWidgets widgets, GameSpace gameSpace) {
		return new TowersSidebar(widgets.addSidebar(gameSpace.getSourceConfig().getName().copy().formatted(Formatting.BOLD, Formatting.GOLD)));
	}

	/**
	 * Updates the sidebar.
	 *
	 * @param time    the game's time in ticks
	 * @param teamMap the map of team
	 */
	public void update(long time, long nextRefillTick, Object2ObjectMap<GameTeam, TeamData> teamMap) {
		sidebarWidget.set(content -> {
			content.add(new LiteralText(""));
			teamMap.forEach((gameTeam, team) -> {
				MutableText text = new LiteralText("");
				if(team.health > 0) {
					text.append(gameTeam.display().shallowCopy().formatted(Formatting.BOLD))
							.append(new LiteralText(" " + FormattingUtil.GENERAL_SYMBOL + " ").formatted(Formatting.GRAY))
							.append(new LiteralText(String.valueOf(team.health)).formatted(Formatting.WHITE))
							.append(new LiteralText(FormattingUtil.HEALTH_SYMBOL).formatted(Formatting.GREEN));
				}
				else {
					text.append(gameTeam.display().shallowCopy().formatted(Formatting.DARK_GRAY, Formatting.BOLD))
							.append(new LiteralText(" " + FormattingUtil.GENERAL_SYMBOL + " ").formatted(Formatting.GRAY))
							.append(new LiteralText(FormattingUtil.X_SYMBOL).formatted(Formatting.DARK_GRAY));
				}
				content.add(text);
			});
			//TODO: fix the refill method
			//content.add(new LiteralText(""));
			//content.add(new TranslatableText("text.the_towers.sidebar.refill_in", TickUtil.format(nextRefillTick - time).shallowCopy().formatted(Formatting.WHITE)).formatted(Formatting.GRAY));
			content.add(new LiteralText(""));
			content.add(new LiteralText(FormattingUtil.CLOCK_SYMBOL + " ").formatted(Formatting.GRAY).append(new TranslatableText("text.the_towers.sidebar.time", TickUtil.format(time).shallowCopy().formatted(Formatting.WHITE)).formatted(Formatting.GRAY)));
		});
	}
}