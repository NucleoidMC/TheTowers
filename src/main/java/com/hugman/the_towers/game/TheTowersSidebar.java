package com.hugman.the_towers.game;

import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;
import xyz.nucleoid.plasmid.widget.SidebarWidget;

public class TheTowersSidebar {
	private final SidebarWidget sidebarWidget;
	private final TheTowersActive active;

	private TheTowersSidebar(SidebarWidget sidebarWidget, TheTowersActive active) {
		this.sidebarWidget = sidebarWidget;
		this.active = active;
	}

	public static TheTowersSidebar create(GlobalWidgets widgets, TheTowersActive active) {
		return new TheTowersSidebar(widgets.addSidebar(active.gameSpace.getGameConfig().getNameText().copy().formatted(Formatting.BOLD, Formatting.GOLD)), active);
	}

	public void update() {
		sidebarWidget.set(content -> {
			content.writeLine("");
			active.getTeamMap().forEach((team, theTowersTeam) -> {
				content.writeFormattedTranslated(Formatting.WHITE, "text.the_towers.sidebar.entry", team.getDisplay(), new LiteralText(String.valueOf(theTowersTeam.getHealth())).formatted(Formatting.GREEN));
			});
		});
	}
}