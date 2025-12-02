package de.tomalbrc.balloons.configui.impl.selection;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.config.ConfiguredBalloon;
import de.tomalbrc.balloons.configui.api.ConfiguredGui;
import de.tomalbrc.balloons.configui.api.GuiElementData;
import de.tomalbrc.balloons.configui.api.ListGuiElementType;
import de.tomalbrc.balloons.configui.impl.confirm.ConfirmationGui;
import de.tomalbrc.balloons.util.StorageUtil;
import de.tomalbrc.balloons.util.Util;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.world.item.ItemStack;

class ContentListType implements ListGuiElementType<GuiElementData, ConfiguredBalloon> {
    @Override
    public GuiElementBuilder build(ConfiguredGui<GuiElementData, ConfiguredBalloon> player, GuiElementData data) {
        return null;
    }

    @Override
    public GuiElementBuilder buildEntry(ConfiguredGui<GuiElementData, ConfiguredBalloon> gui, GuiElementData data, ConfiguredBalloon element) {
        ItemStack stack = element.itemStack().copy();
        boolean isActive1 = element.id().equals(StorageUtil.getActive(gui.getPlayer()));

        return data.decorate(new GuiElementBuilder(stack), element.placeholder(), isActive1).setCallback((slot, click, action) -> {
            Util.clickSound(gui.getPlayer());

            boolean isActive = element.id().equals(StorageUtil.getActive(gui.getPlayer()));

            if (click == ClickType.MOUSE_LEFT) {
                if (!isActive) {
                    StorageUtil.setActive(gui.getPlayer(), element.id());
                    Balloons.spawnActive(gui.getPlayer());
                    gui.close();
                }
            } else if (click == ClickType.MOUSE_LEFT_SHIFT) {
                if (Balloons.getStorage().listFavs(gui.getPlayer().getUUID()).size() < gui.getPageSize("favourites") && Balloons.getStorage().addFav(gui.getPlayer().getUUID(), element.id()))
                    gui.setPage("favourites", 0);
            } else {
                if (isActive) {
                    StorageUtil.removeActive(gui.getPlayer());
                    Balloons.despawnBalloon(gui.getPlayer());
                 } else {
                    var gui2 = new ConfirmationGui(gui.getPlayer(), element.id());
                    gui2.open();
                }
            }
        });
    }
}
