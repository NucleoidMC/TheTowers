package fr.hugman.the_towers.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class TickUtil {
    private TickUtil() {
    }

    public static long asSeconds(long t) {
        return t / 20;
    }

    public static int getSeconds(long t) {
        return (int) asSeconds(t) % 60;
    }

    public static long asMinutes(long t) {
        return asSeconds(t) / 60;
    }

    public static int getMinutes(long t) {
        return (int) asMinutes(t) % 60;
    }

    public static long asHours(long t) {
        return asMinutes(t) / 60;
    }

    public static int getHours(long t) {
        return (int) asHours(t) % 24;
    }

    public static boolean blink(long l, int max, int each) {
        return l <= max && l % each == 0;
    }

    public static Component format(long t) {
        if (getHours(t) > 0) {
            return Component.literal(String.format("%02d:%02d:%02d", getHours(t), getMinutes(t), getSeconds(t)));
        } else {
            return Component.literal(String.format("%02d:%02d", getMinutes(t), getSeconds(t)));
        }
    }

    public static MutableComponent formatPretty(long t) {
        MutableComponent text = Component.literal("");
        long hours = getHours(t);
        long minutes = getMinutes(t);
        long seconds = getSeconds(t);

        boolean textBefore = false;
        if (hours > 0) {
            if (hours == 1) {
                text.append(Component.translatable("text.the_towers.time.hour"));
            } else {
                text.append(Component.translatable("text.the_towers.time.hours", hours));
            }
            textBefore = true;
        }
        if (minutes > 0) {
            if (textBefore)
                text.append(Component.literal(" ")).append(Component.translatable("text.the_towers.and")).append(Component.literal(" "));
            if (minutes == 1) {
                text.append(Component.translatable("text.the_towers.time.minute"));
            } else {
                text.append(Component.translatable("text.the_towers.time.minutes", minutes));
            }
            textBefore = true;
        }
        if (seconds > 0) {
            if (textBefore)
                text.append(Component.literal(" ")).append(Component.translatable("text.the_towers.and")).append(Component.literal(" "));
            if (seconds == 1) {
                text.append(Component.translatable("text.the_towers.time.second"));
            } else {
                text.append(Component.translatable("text.the_towers.time.seconds", seconds));
            }
        }

        return text;
    }
}
