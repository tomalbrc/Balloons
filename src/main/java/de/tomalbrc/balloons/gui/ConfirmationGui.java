package de.tomalbrc.balloons.gui;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.component.BalloonToken;
import de.tomalbrc.balloons.component.ModComponents;
import de.tomalbrc.balloons.config.ConfiguredBalloon;
import de.tomalbrc.balloons.config.ModConfig;
import de.tomalbrc.balloons.util.StorageUtil;
import de.tomalbrc.balloons.util.TextUtil;
import de.tomalbrc.balloons.util.Util;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ConfirmationGui extends SimpleGui {
    private final ResourceLocation id;
    private final Runnable onClose;

    public ConfirmationGui(ServerPlayer player, ResourceLocation id, Runnable onClose) {
        super(Util.menuTypeForHeight(ModConfig.getInstance().gui.confirmationMenuHeight), player, false);

        this.onClose = onClose;
        this.id = id;
        this.setTitle(TextUtil.parse(ModConfig.getInstance().gui.confirmationMenuTitle));

        setupButtons();
    }

    private void setupButtons() {
        var gui = ModConfig.getInstance().gui;

        if (gui.addBackButton) {
            var loc = gui.cancelButtonLocation;
            int idx = GuiHelpers.posToIndex(loc.x - 1, loc.y - 1, getHeight(), getWidth());
            this.setSlot(idx, GuiElementBuilder.from(gui.backItem())
                    .setName(TextUtil.parse(ModConfig.getInstance().messages.cancel))
                    .setCallback(this::cancel));
        }

        var confirmLoc = gui.confirmButtonLocation;
        int confirmIdx = GuiHelpers.posToIndex(confirmLoc.x - 1, confirmLoc.y - 1, getHeight(), getWidth());
        this.setSlot(confirmIdx, GuiElementBuilder.from(gui.confirmItem())
                .setName(TextUtil.parse(ModConfig.getInstance().messages.confirm))
                .setCallback(this::confirm));
    }

    private void confirm() {
        Util.clickSound(player);

        if (StorageUtil.remove(player.getUUID(), id)) {
            ConfiguredBalloon balloon = Balloons.all().get(id);
            if (balloon == null) {
                close();
                return;
            }

            ItemStack item = balloon.item();
            if (item != null) {
                item.set(ModComponents.TOKEN, new BalloonToken(id, balloon.permission(), balloon.permissionLevel()));
                player.addItem(item);

                if (!item.isEmpty() && item.getCount() > 0) {
                    player.spawnAtLocation(player.level(), item);
                }
            }
        }

        this.close();
        if (this.onClose != null) this.onClose.run();
    }

    private void cancel() {
        Util.clickSound(player);
        this.close();
        if (this.onClose != null) this.onClose.run();
    }
}
