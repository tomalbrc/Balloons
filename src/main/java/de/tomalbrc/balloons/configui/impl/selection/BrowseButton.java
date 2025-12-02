package de.tomalbrc.balloons.configui.impl.selection;

import de.tomalbrc.balloons.config.ConfiguredBalloon;
import de.tomalbrc.balloons.configui.api.ConfiguredGui;
import de.tomalbrc.balloons.configui.api.GuiElementData;
import de.tomalbrc.balloons.configui.api.GuiElementType;
import de.tomalbrc.balloons.configui.impl.browse.BrowseGui;
import de.tomalbrc.balloons.util.Util;
import eu.pb4.sgui.api.elements.GuiElementBuilder;

public class BrowseButton implements GuiElementType<GuiElementData, ConfiguredBalloon> {
    @Override
    public GuiElementBuilder build(ConfiguredGui<GuiElementData, ConfiguredBalloon> gui, GuiElementData data) {
        return data.decorate(new GuiElementBuilder(data.item().copy())).setCallback(() -> {
            Util.clickSound(gui.getPlayer());

            var gui2 = new BrowseGui(gui.getPlayer());
            gui2.open();
        });
    }
}
