package de.tomalbrc.balloons.configui.api;

import java.util.List;
import java.util.Map;

public record GuiData<T>(String title, List<String> layout, Map<Character, T> keys, boolean manipulatePlayerSlots) {}
