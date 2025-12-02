package de.tomalbrc.balloons.configui.impl.confirm;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.component.BalloonToken;
import de.tomalbrc.balloons.component.ModComponents;
import de.tomalbrc.balloons.config.ConfiguredBalloon;
import de.tomalbrc.balloons.config.ModConfig;
import de.tomalbrc.balloons.configui.api.ConfiguredGui;
import de.tomalbrc.balloons.configui.api.GuiElementData;
import de.tomalbrc.balloons.configui.api.GuiTypeRegistry;
import de.tomalbrc.balloons.configui.impl.selection.EmptyGuiType;
import de.tomalbrc.balloons.configui.impl.selection.SelectionGui;
import de.tomalbrc.balloons.util.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ConfirmationGui extends ConfiguredGui<GuiElementData, ConfiguredBalloon> {
    final ResourceLocation id;

    public ConfirmationGui(ServerPlayer player, ResourceLocation id) {
        super("balloon_confirm", ModConfig.getInstance().confirmationGui, player, false);
        this.id = id;
    }

    @Override
    protected void build() {
        GuiTypeRegistry.register(this.getGuiId(), "empty", new EmptyGuiType());
        GuiTypeRegistry.register(this.getGuiId(), "cancel", new CancelButton());
        GuiTypeRegistry.register(this.getGuiId(), "confirm", new ConfirmButton());

        super.build();
    }

    @Override
    protected List<ConfiguredBalloon> getElementsForType(String typeName, ServerPlayer player) {
        return super.getElementsForType(typeName, player);
    }

    public void confirm() {
        Util.clickSound(this.getPlayer());

        if (Balloons.getStorage().remove(player.getUUID(), id)) {
            var balloon = Balloons.all().get(id);
            if (balloon == null) {
                close();
                return;
            }

            ItemStack item = balloon.itemStack();
            item.set(ModComponents.TOKEN, new BalloonToken(id, balloon.permission(), balloon.permissionLevel()));

            player.addItem(item);

            if (!item.isEmpty() && item.getCount() > 0) {
                player.spawnAtLocation(player.level(), item);
            }
        }

        back();
    }

    @Override
    public void back() {
        final var gui = new SelectionGui(player);
        gui.open();
    }
}
