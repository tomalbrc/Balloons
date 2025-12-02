package de.tomalbrc.balloons.configui.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GuiTypeRegistry {
    private static final Map<String, Map<String, GuiElementType<?,?>>> REG = new ConcurrentHashMap<>();

    private GuiTypeRegistry() {}

    public static void register(String guiId, String typeName, GuiElementType<? extends GuiElementData, ?> handler) {
        REG.computeIfAbsent(guiId, k -> new ConcurrentHashMap<>())
                  .put(typeName, handler);
    }

    public static <T extends GuiElementData, E> GuiElementType<T, E> get(String guiId, String typeName) {
        Map<String, GuiElementType<?, ?>> map = REG.get(guiId);
        if (map == null) return null;
        return (GuiElementType<T, E>) map.get(typeName);
    }
}
