package com.hugman.the_towers.game;

import fr.catcore.server.translations.api.mixin.text.TranslatableTextMixin;
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
			active.getParticipantMap().keys().forEach(team -> {
				// TODO: make translated when possible
				content.writeLine(team.getFormatting().toString() + Formatting.BOLD + (team.health <= 0 ? Formatting.STRIKETHROUGH.toString() : "") + team.getDisplay() +
						Formatting.GRAY + " » " +
						Formatting.WHITE + team.health +
						Formatting.GRAY + "/" + Formatting.WHITE +
						active.config.getTeamHealth() +
						Formatting.WHITE);
			});
		});
	}

	public void end() {
		sidebarWidget.close();
	}
}