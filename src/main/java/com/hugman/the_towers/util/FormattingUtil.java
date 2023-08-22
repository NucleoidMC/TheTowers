package com.hugman.the_towers.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class FormattingUtil {
    public static final Style PREFIX_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0x858585));
    public static final Style WHITE_STYLE = Style.EMPTY.withColor(Formatting.WHITE);
    public static final Style PLAYER_MESSAGE_STYLE = Style.EMPTY.withColor(Formatting.YELLOW);
    public static final Style GENERAL_STYLE = Style.EMPTY.withColor(Formatting.GOLD);
    public static final Style WARNING_STYLE = Style.EMPTY.withColor(Formatting.RED);
    public static final Style DEATH_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xbfbfbf));

    public static final String GENERAL_SYMBOL = "»";
    public static final String CHECKMARK_SYMBOL = "✔";
    public static final String X_SYMBOL = "✘";
    public static final String HEALTH_SYMBOL = "✚";
    public static final String SKULL_SYMBOL = "☠";
    public static final String STAR_SYMBOL = "★";
    public static final String CLOCK_SYMBOL = "⌚";

    public static MutableText format(String prefix, Style style, Text message) {
        return Text.literal(prefix + " ").setStyle(PREFIX_STYLE).append(message.copyContentOnly().fillStyle(style));
    }

    public static MutableText format(String prefix, Text message) {
        return Text.literal(prefix + " ").setStyle(PREFIX_STYLE).append(message.copyContentOnly());
    }
}