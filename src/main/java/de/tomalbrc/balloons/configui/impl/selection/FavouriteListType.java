package de.tomalbrc.balloons.configui.impl.selection;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.config.ConfiguredBalloon;
import de.tomalbrc.balloons.configui.api.ConfiguredGui;
import de.tomalbrc.balloons.configui.api.GuiElementData;
import de.tomalbrc.balloons.configui.api.ListGuiElementType;
import de.tomalbrc.balloons.util.StorageUtil;
import de.tomalbrc.balloons.util.Util;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.world.item.ItemStack;

class FavouriteListType implements ListGuiElementType<GuiElementData, ConfiguredBalloon> {
    @Override
    public GuiElementBuilder build(ConfiguredGui<GuiElementData, ConfiguredBalloon> gui, GuiElementData data) {
        return new GuiElementBuilder(ItemStack.EMPTY);
    }

    @Override
    public GuiElementBuilder buildEntry(ConfiguredGui<GuiElementData, ConfiguredBalloon> gui, GuiElementData data, ConfiguredBalloon element) {
        return data.decorate(new GuiElementBuilder(element.itemStack()), element.placeholder(), element.id().equals(StorageUtil.getActive(gui.getPlayer()))).setCallback((s, c, a) -> {
            Util.clickSound(gui.getPlayer());

            boolean isActive = element.id().equals(StorageUtil.getActive(gui.getPlayer()));

            if (c == ClickType.MOUSE_LEFT_SHIFT) {
                if (Balloons.getStorage().removeFav(gui.getPlayer().getUUID(), element.id()))
                    gui.setPage(data.type(), 0);
            } else if (c == ClickType.MOUSE_RIGHT && isActive) {
                Balloons.despawnBalloon(gui.getPlayer());
                Balloons.getStorage().removeActive(gui.getPlayer().getUUID());
            } else if (c == ClickType.MOUSE_LEFT && !isActive) {
                Balloons.getStorage().setActive(gui.getPlayer().getUUID(), element.id());
                Balloons.spawnActive(gui.getPlayer());
                gui.close();
            }
        });
    }
}
