package de.tomalbrc.balloons.util;

import eu.pb4.placeholders.api.TextParserUtils;
import net.minecraft.network.chat.Component;

public class TextUtil {
    public static Component parse(String s) {
        return s == null ? Component.empty() : TextParserUtils.formatText(s);
    }
}
