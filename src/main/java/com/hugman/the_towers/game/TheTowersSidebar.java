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

public record TheTowersSidebar(SidebarWidget sidebarWidget) {
	public static TheTowersSidebar create(GlobalWidgets widgets, GameSpace gameSpace) {
		return new TheTowersSidebar(widgets.addSidebar(gameSpace.getSourceConfig().getName().copy().formatted(Formatting.BOLD, Formatting.GOLD)));
	}

	/**
	 * Updates the sidebar.
	 *
	 * @param time      the game's time in ticks
	 * @param teamMap   the mapTemplateId of teamConfig
	 * @param maxHealth the maximum health of teamConfig configured by the config
	 */
	public void update(long time, Object2ObjectMap<GameTeam, TheTowersTeam> teamMap, int maxHealth) {
		sidebarWidget.set(content -> {
			content.add(new LiteralText(""));
			teamMap.forEach((gameTeam, team) -> {
				MutableText text = new LiteralText("");
				if(team.health > 0) {
					text.append(gameTeam.display().shallowCopy().formatted(Formatting.BOLD))
							.append(new LiteralText(" " + FormattingUtil.GENERAL_PREFIX + " ").formatted(Formatting.GRAY))
							.append(new LiteralText(String.valueOf(team.health)).formatted(Formatting.WHITE))
							.append(new LiteralText("/").formatted(Formatting.GRAY))
							.append(new LiteralText(String.valueOf(maxHealth)).formatted(Formatting.WHITE));
				}
				else {
					text.append(gameTeam.display().shallowCopy().formatted(Formatting.DARK_GRAY, Formatting.BOLD, Formatting.STRIKETHROUGH))
							.append(new LiteralText(" " + FormattingUtil.GENERAL_PREFIX + " ").formatted(Formatting.GRAY, Formatting.STRIKETHROUGH))
							.append(new LiteralText(String.valueOf(team.health)).formatted(Formatting.GRAY, Formatting.STRIKETHROUGH))
							.append(new LiteralText("/").formatted(Formatting.GRAY, Formatting.STRIKETHROUGH))
							.append(new LiteralText(String.valueOf(maxHealth)).formatted(Formatting.GRAY, Formatting.STRIKETHROUGH));
				}
				content.add(text);
			});
			content.add(new LiteralText(""));
			content.add(new TranslatableText("text.the_towers.time", TickUtil.format(time).shallowCopy().formatted(Formatting.WHITE)).formatted(Formatting.GRAY));
		});
	}
}