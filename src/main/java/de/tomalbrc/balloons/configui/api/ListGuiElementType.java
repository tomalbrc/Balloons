package de.tomalbrc.balloons.configui.api;

import eu.pb4.sgui.api.elements.GuiElementBuilder;

public interface ListGuiElementType<T extends GuiElementData, E> extends GuiElementType<T, E> {
    GuiElementBuilder buildEntry(ConfiguredGui<T, E> gui, GuiElementData data, E element);
}
