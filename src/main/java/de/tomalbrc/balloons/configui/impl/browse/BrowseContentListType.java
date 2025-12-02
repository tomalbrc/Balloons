package de.tomalbrc.balloons.configui.impl.browse;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.config.ConfiguredBalloon;
import de.tomalbrc.balloons.configui.api.ConfiguredGui;
import de.tomalbrc.balloons.configui.api.GuiElementData;
import de.tomalbrc.balloons.configui.api.ListGuiElementType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.world.item.ItemStack;

class BrowseContentListType implements ListGuiElementType<GuiElementData, ConfiguredBalloon> {
    @Override
    public GuiElementBuilder build(ConfiguredGui<GuiElementData, ConfiguredBalloon> player, GuiElementData data) {
        return new GuiElementBuilder();
    }

    @Override
    public GuiElementBuilder buildEntry(ConfiguredGui<GuiElementData, ConfiguredBalloon> gui, GuiElementData data, ConfiguredBalloon element) {
        ItemStack stack = element.itemStack().copy();
        return data.decorate(new GuiElementBuilder(stack).setCallback((slot, click, action) -> {

        }), element.placeholder(), Balloons.getStorage().list(gui.getPlayer().getUUID()).contains(element.id()));
    }
}
