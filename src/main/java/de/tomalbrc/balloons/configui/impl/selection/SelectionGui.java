package de.tomalbrc.balloons.configui.impl.selection;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.config.ConfiguredBalloon;
import de.tomalbrc.balloons.config.ModConfig;
import de.tomalbrc.balloons.configui.api.ConfiguredGui;
import de.tomalbrc.balloons.configui.api.GuiElementData;
import de.tomalbrc.balloons.configui.api.GuiTypeRegistry;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class SelectionGui extends ConfiguredGui<GuiElementData, ConfiguredBalloon> {
    public SelectionGui(ServerPlayer player) {
        super("balloon_selection", ModConfig.getInstance().selectionGui, player, false);
    }

    @Override
    protected void build() {
        GuiTypeRegistry.register(this.getGuiId(), "empty", new EmptyGuiType());
        GuiTypeRegistry.register(this.getGuiId(), "contents", new ContentListType());
        GuiTypeRegistry.register(this.getGuiId(), "browse", new BrowseButton());
        GuiTypeRegistry.register(this.getGuiId(), "favourites", new FavouriteListType());
        GuiTypeRegistry.register(this.getGuiId(), "prev_page", new PrevPageButton());
        GuiTypeRegistry.register(this.getGuiId(), "next_page", new NextPageButton());

        super.build();
    }

    @Override
    protected List<ConfiguredBalloon> getElementsForType(String typeName, ServerPlayer player) {
        if (typeName.equals("contents")) {
            return Balloons.getStorage().list(player.getUUID()).stream().map(x -> Balloons.all().get(x)).toList();
        } else if (typeName.equals("favourites")) {
            return Balloons.getStorage().listFavs(player.getUUID()).stream().map(x -> Balloons.all().get(x)).toList();
        }

        return super.getElementsForType(typeName, player);
    }
}
