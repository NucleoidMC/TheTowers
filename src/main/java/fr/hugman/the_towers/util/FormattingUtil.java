package fr.hugman.the_towers.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class FormattingUtil {
    public static final Style PREFIX_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0x858585));
    public static final Style WHITE_STYLE = Style.EMPTY.withColor(ChatFormatting.WHITE);
    public static final Style PLAYER_MESSAGE_STYLE = Style.EMPTY.withColor(ChatFormatting.YELLOW);
    public static final Style GENERAL_STYLE = Style.EMPTY.withColor(ChatFormatting.GOLD);
    public static final Style WARNING_STYLE = Style.EMPTY.withColor(ChatFormatting.RED);
    public static final Style DEATH_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xbfbfbf));

    public static final String GENERAL_SYMBOL = "»";
    public static final String CHECKMARK_SYMBOL = "✔";
    public static final String X_SYMBOL = "✘";
    public static final String HEALTH_SYMBOL = "✚";
    public static final String SKULL_SYMBOL = "☠";
    public static final String STAR_SYMBOL = "★";
    public static final String CLOCK_SYMBOL = "⌚";

    public static MutableComponent format(String prefix, Style style, Component message) {
        return Component.literal(prefix + " ").setStyle(PREFIX_STYLE).append(message.plainCopy().withStyle(style));
    }

    public static MutableComponent format(String prefix, Component message) {
        return Component.literal(prefix + " ").setStyle(PREFIX_STYLE).append(message.plainCopy());
    }
}