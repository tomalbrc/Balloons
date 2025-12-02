package de.tomalbrc.balloons.configui.impl.selection;

import de.tomalbrc.balloons.config.ConfiguredBalloon;
import de.tomalbrc.balloons.configui.api.ConfiguredGui;
import de.tomalbrc.balloons.configui.api.GuiElementData;
import de.tomalbrc.balloons.configui.api.GuiElementType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.world.item.Items;

public class EmptyGuiType implements GuiElementType<GuiElementData, ConfiguredBalloon> {
    @Override
    public GuiElementBuilder build(ConfiguredGui<GuiElementData, ConfiguredBalloon> player, GuiElementData data) {
        return data.decorate(new GuiElementBuilder(Items.AIR.getDefaultInstance()));
    }
}
