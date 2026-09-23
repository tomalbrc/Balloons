package de.tomalbrc.balloons.configui.impl.confirm;

import de.tomalbrc.balloons.config.ConfiguredBalloon;
import de.tomalbrc.balloons.configui.api.ConfiguredGui;
import de.tomalbrc.balloons.configui.api.GuiElementData;
import de.tomalbrc.balloons.configui.api.GuiElementType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.world.item.Items;

public class ConfirmButton implements GuiElementType<GuiElementData, ConfiguredBalloon> {
    @Override
    public GuiElementBuilder build(ConfiguredGui<GuiElementData, ConfiguredBalloon> g, GuiElementData data) {
        return data.decorate(new GuiElementBuilder(data.item() == null ? Items.AIR.getDefaultInstance() : data.item().create())).setCallback(() -> {
            if (g instanceof ConfirmationGui confirmationGui)
                confirmationGui.confirm();
        });
    }
}
