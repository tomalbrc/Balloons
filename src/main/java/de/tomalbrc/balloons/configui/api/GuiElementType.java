package de.tomalbrc.balloons.configui.api;

import eu.pb4.sgui.api.elements.GuiElementBuilder;

public interface GuiElementType<T extends GuiElementData, E> {
    GuiElementBuilder build(ConfiguredGui<T, E> player, T data);
}