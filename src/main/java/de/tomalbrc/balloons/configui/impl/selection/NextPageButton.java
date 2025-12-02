package de.tomalbrc.balloons.configui.impl.selection;

import de.tomalbrc.balloons.config.ConfiguredBalloon;
import de.tomalbrc.balloons.configui.api.ConfiguredGui;
import de.tomalbrc.balloons.configui.api.GuiElementData;
import de.tomalbrc.balloons.configui.api.GuiElementType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.world.item.Items;

public class NextPageButton implements GuiElementType<GuiElementData, ConfiguredBalloon> {
    @Override
    public GuiElementBuilder build(ConfiguredGui<GuiElementData, ConfiguredBalloon> g, GuiElementData data) {
        if (g.getCurrentPage("contents") == g.maxPage("contents")) {
            return new GuiElementBuilder(Items.AIR.getDefaultInstance());
        }

        return data.decorate(new GuiElementBuilder(data.item().copy())).setCallback(() -> g.nextPage(data.type()));
    }
}
