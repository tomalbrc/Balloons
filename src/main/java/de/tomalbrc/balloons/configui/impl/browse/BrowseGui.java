package de.tomalbrc.balloons.configui.impl.browse;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.config.ConfiguredBalloon;
import de.tomalbrc.balloons.config.ModConfig;
import de.tomalbrc.balloons.configui.api.ConfiguredGui;
import de.tomalbrc.balloons.configui.api.GuiElementData;
import de.tomalbrc.balloons.configui.api.GuiTypeRegistry;
import de.tomalbrc.balloons.configui.impl.confirm.CancelButton;
import de.tomalbrc.balloons.configui.impl.selection.EmptyGuiType;
import de.tomalbrc.balloons.configui.impl.selection.NextPageButton;
import de.tomalbrc.balloons.configui.impl.selection.PrevPageButton;
import de.tomalbrc.balloons.configui.impl.selection.SelectionGui;
import de.tomalbrc.balloons.util.Util;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class BrowseGui extends ConfiguredGui<GuiElementData, ConfiguredBalloon> {
    public BrowseGui(ServerPlayer player) {
        super("browse", ModConfig.getInstance().browseGui, player, false);
    }

    @Override
    protected void build() {
        GuiTypeRegistry.register(this.getGuiId(), "empty", new EmptyGuiType());
        GuiTypeRegistry.register(this.getGuiId(), "contents", new BrowseContentListType());
        GuiTypeRegistry.register(this.getGuiId(), "prev_page", new PrevPageButton());
        GuiTypeRegistry.register(this.getGuiId(), "next_page", new NextPageButton());
        GuiTypeRegistry.register(this.getGuiId(), "back", new CancelButton());

        super.build();
    }

    @Override
    protected List<ConfiguredBalloon> getElementsForType(String typeName, ServerPlayer player) {
        if (typeName.equals("contents")) {
            return new ArrayList<>(Balloons.all().values());
        } else if (typeName.equals("favourites")) {
            return new ArrayList<>();
        }

        return super.getElementsForType(typeName, player);
    }

    @Override
    public void back() {
        Util.clickSound(this.getPlayer());

        final var gui = new SelectionGui(player);
        gui.open();
    }
}
