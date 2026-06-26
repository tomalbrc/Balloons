package de.tomalbrc.balloons.util;

import de.tomalbrc.filament.util.FilamentFormatter;
import net.minecraft.network.chat.Component;

public class TextUtil {
    public static Component parse(String s) {
        return s == null ? Component.empty() : FilamentFormatter.parse(s);
    }
}
