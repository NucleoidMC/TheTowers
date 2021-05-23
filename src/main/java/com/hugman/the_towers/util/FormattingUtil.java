package com.hugman.the_towers.util;

import net.minecraft.text.LiteralText;
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

	public static final String GENERAL_PREFIX = "»";
	public static final String DEATH_PREFIX = "☠";
	public static final String PICKAXE_PREFIX = "⛏";
	public static final String HEALTH_PREFIX = "✚";
	public static final String SUN_PREFIX = "☀";
	public static final String UMBRELLA_PREFIX = "☂";
	public static final String CLOUD_PREFIX = "☁";
	public static final String MUSIC_PREFIX = "♫";
	public static final String HEART_PREFIX = "♥";
	public static final String X_PREFIX = "✘";
	public static final String HOURGLASS_PREFIX = "⌛";
	public static final String CLOCK_PREFIX = "⌚";
	public static final String STAR_PREFIX = "★";

	public static final String CHECKMARK = "✔";
	public static final String HEALTH = "✚";


	public static MutableText format(String prefix, Style style, Text message) {
		return new LiteralText(prefix + " ").setStyle(PREFIX_STYLE).append(message.shallowCopy().fillStyle(style));
	}

	public static MutableText format(String prefix, Text message) {
		return new LiteralText(prefix + " ").setStyle(PREFIX_STYLE).append(message.shallowCopy());
	}
}