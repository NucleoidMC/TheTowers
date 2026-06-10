package fr.hugman.the_towers.game;

import fr.hugman.plasmid.api.game_map.GameMapMetadata;
import fr.hugman.the_towers.config.TowersConfig;
import fr.hugman.the_towers.util.FormattingUtil;
import fr.hugman.the_towers.util.TickUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.api.game.common.widget.SidebarWidget;
import xyz.nucleoid.plasmid.api.game.config.GameConfig;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public record TowersSidebar(SidebarWidget sidebarWidget, Optional<Component> mapName) {
    public static TowersSidebar create(GlobalWidgets widgets, GameSpace gameSpace) {
        var config = gameSpace.getMetadata().sourceConfig();
        var gameName = config.value().shortName();
        if (gameName == null) gameName = Component.translatable("game.the_towers");
        return new TowersSidebar(
                widgets.addSidebar(gameName.copy().withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD)),
                ((TowersConfig) config.value().config()).map().value().getMetadata().flatMap(GameMapMetadata::name)
        );
    }

    /**
     * Updates the sidebar.
     *
     * @param time    the game's time in ticks
     * @param teamMap the map of team
     */
    public void update(long time, long nextRefillTick, TeamManager teamManager, Object2ObjectMap<GameTeamKey, TeamData> teamMap) {
        sidebarWidget.set(content -> {
            content.add(Component.literal(""));
            teamMap.forEach((teamKey, team) -> {
                MutableComponent text = Component.literal("");
                if (team.health > 0) {
                    text.append(teamManager.getTeamConfig(teamKey).name().copy().withStyle(ChatFormatting.BOLD))
                            .append(Component.literal(" " + FormattingUtil.GENERAL_SYMBOL + " ").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(String.valueOf(team.health)).withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(FormattingUtil.HEALTH_SYMBOL).withStyle(ChatFormatting.GREEN));
                } else {
                    text.append(teamManager.getTeamConfig(teamKey).name().copy().withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                            .append(Component.literal(" " + FormattingUtil.GENERAL_SYMBOL + " ").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(FormattingUtil.X_SYMBOL).withStyle(ChatFormatting.DARK_GRAY));
                }
                content.add(text);
            });
            //TODO add current map
            //TODO: fix the refill method
            //content.add(Text.literal(""));
            //content.add(Text.translatable("text.the_towers.sidebar.refill_in", TickUtil.format(nextRefillTick - time).shallowCopy().formatted(Formatting.WHITE)).formatted(Formatting.GRAY));
            content.add(Component.literal(""));
            content.add(Component.literal(FormattingUtil.CLOCK_SYMBOL + " ").withStyle(ChatFormatting.GRAY).append(Component.translatable("text.the_towers.sidebar.time", TickUtil.format(time).plainCopy().withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY)));
            if(mapName.isPresent()) {
                content.add(Component.literal(""));
                content.add(Component.literal(FormattingUtil.GENERAL_SYMBOL + " ").withStyle(ChatFormatting.GRAY).append(Component.translatable("text.the_towers.sidebar.map", mapName.get().plainCopy().withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY)));
            }
        });
    }
}