package com.hugman.the_towers.game;

import com.hugman.the_towers.util.FormattingUtil;
import com.hugman.the_towers.util.TickUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.game.common.widget.SidebarWidget;

public record TowersSidebar(SidebarWidget sidebarWidget) {
    public static TowersSidebar create(GlobalWidgets widgets, GameSpace gameSpace) {
        var gameName = gameSpace.getMetadata().sourceConfig().value().name();
        if(gameName == null) gameName = Text.of("The Towers");
        return new TowersSidebar(widgets.addSidebar(gameName.copy().formatted(Formatting.BOLD, Formatting.GOLD)));
    }

    /**
     * Updates the sidebar.
     *
     * @param time    the game's time in ticks
     * @param teamMap the map of team
     */
    public void update(long time, long nextRefillTick, Object2ObjectMap<GameTeam, TeamData> teamMap) {
        sidebarWidget.set(content -> {
            content.add(Text.literal(""));
            teamMap.forEach((gameTeam, team) -> {
                MutableText text = Text.literal("");
                if (team.health > 0) {
                    text.append(gameTeam.config().name().copy().formatted(Formatting.BOLD))
                            .append(Text.literal(" " + FormattingUtil.GENERAL_SYMBOL + " ").formatted(Formatting.GRAY))
                            .append(Text.literal(String.valueOf(team.health)).formatted(Formatting.WHITE))
                            .append(Text.literal(FormattingUtil.HEALTH_SYMBOL).formatted(Formatting.GREEN));
                } else {
                    text.append(gameTeam.config().name().copy().formatted(Formatting.DARK_GRAY, Formatting.BOLD))
                            .append(Text.literal(" " + FormattingUtil.GENERAL_SYMBOL + " ").formatted(Formatting.GRAY))
                            .append(Text.literal(FormattingUtil.X_SYMBOL).formatted(Formatting.DARK_GRAY));
                }
                content.add(text);
            });
            //TODO: fix the refill method
            //content.add(Text.literal(""));
            //content.add(Text.translatable("text.the_towers.sidebar.refill_in", TickUtil.format(nextRefillTick - time).shallowCopy().formatted(Formatting.WHITE)).formatted(Formatting.GRAY));
            content.add(Text.literal(""));
            content.add(Text.literal(FormattingUtil.CLOCK_SYMBOL + " ").formatted(Formatting.GRAY).append(Text.translatable("text.the_towers.sidebar.time", TickUtil.format(time).copyContentOnly().formatted(Formatting.WHITE)).formatted(Formatting.GRAY)));
        });
    }
}