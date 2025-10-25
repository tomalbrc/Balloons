package de.tomalbrc.balloons.gui;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.config.ConfiguredBalloon;
import de.tomalbrc.balloons.config.ModConfig;
import de.tomalbrc.balloons.util.TextUtil;
import de.tomalbrc.balloons.util.Util;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.layered.Layer;
import eu.pb4.sgui.api.gui.layered.LayeredGui;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class SelectionGui extends LayeredGui {
    protected final boolean addBackButton;
    private final Layer contentLayer;
    private int page = 0;

    public SelectionGui(ServerPlayer player, boolean addBackButton) {
        super(Util.menuTypeForHeight(ModConfig.getInstance().gui.selectionMenuHeight), player, false);
        this.addBackButton = addBackButton;

        this.contentLayer = new Layer(this.getHeight() - 2, this.getWidth() - 2);
        addLayer(this.contentLayer, 1, 1);

        this.setTitle(TextUtil.parse(ModConfig.getInstance().gui.selectionMenuTitle));
        setupStaticButtons();
        renderPage();
    }

    private List<ConfiguredBalloon> availableBalloons() {
        List<ConfiguredBalloon> res = new ObjectArrayList<>();
        var owned = Balloons.getStorage().list(getPlayer().getUUID());
        for (Map.Entry<ResourceLocation, ConfiguredBalloon> entry : Balloons.all().entrySet()) {
            if (owned.contains(entry.getKey())) {
                res.add(entry.getValue());
            }
        }
        return res;
    }

    private void setupStaticButtons() {
        if (this.addBackButton && ModConfig.getInstance().gui.addBackButton) {
            var loc = ModConfig.getInstance().gui.backButtonLocation;
            int idx = GuiHelpers.posToIndex(loc.x - 1, loc.y - 1, getHeight(), getWidth());
            this.setSlot(idx, GuiElementBuilder.from(ModConfig.getInstance().gui.backItem()).setCallback(() -> {
                Util.clickSound(getPlayer());
                this.close();
            }));
        }

        if (ModConfig.getInstance().gui.addBrowseButton) {
            var loc = ModConfig.getInstance().gui.browseButtonLocation;
            int idx = GuiHelpers.posToIndex(loc.x - 1, loc.y - 1, getHeight(), getWidth());
            this.setSlot(idx, GuiElementBuilder.from(ModConfig.getInstance().gui.browseItem()).setCallback(() -> {
                Util.clickSound(getPlayer());
                new BrowseGui(getPlayer(), true, null).open();
            }));
        }
    }

    private void renderPage() {
        var available = availableBalloons();

        final int interiorWidth = this.getWidth() - 2;
        final int interiorHeight = this.getHeight() - 2;
        final int layerCapacity = interiorWidth * interiorHeight;

        int totalPages = Math.max(1, (int) Math.ceil((double) available.size() / layerCapacity));
        if (this.page < 0) this.page = 0;
        if (this.page >= totalPages) this.page = totalPages - 1;

        this.contentLayer.clearSlots();

        int start = this.page * layerCapacity;
        int end = Math.min(start + layerCapacity, available.size());

        for (int i = start; i < end; i++) {
            ConfiguredBalloon balloon = available.get(i);
            GuiElementBuilder builder = getGuiElementBuilder(balloon);
            this.contentLayer.addSlot(builder.build());
        }

        var prevLoc = ModConfig.getInstance().gui.prevButtonLocation;
        int prevIdx = GuiHelpers.posToIndex(prevLoc.x - 1, prevLoc.y - 1, getHeight(), getWidth());

        var nextLoc = ModConfig.getInstance().gui.nextButtonLocation;
        int nextIdx = GuiHelpers.posToIndex(nextLoc.x - 1, nextLoc.y - 1, getHeight(), getWidth());

        if (totalPages > 1) {
            if (this.page > 0) {
                this.setSlot(prevIdx, GuiElementBuilder.from(ModConfig.getInstance().gui.prevItem()).setCallback(() -> {
                    Util.clickSound(getPlayer());
                    this.page = Math.max(0, this.page - 1);
                    renderPage();
                }));
            } else {
                this.setSlot(prevIdx, ItemStack.EMPTY);
            }

            if (this.page < totalPages - 1) {
                this.setSlot(nextIdx, GuiElementBuilder.from(ModConfig.getInstance().gui.nextItem()).setCallback(() -> {
                    Util.clickSound(getPlayer());
                    this.page = Math.min(totalPages - 1, this.page + 1);
                    renderPage();
                }));
            } else {
                this.setSlot(nextIdx, ItemStack.EMPTY);
            }
        } else {
            this.setSlot(prevIdx, ItemStack.EMPTY);
            this.setSlot(nextIdx, ItemStack.EMPTY);
        }
    }

    private @NotNull GuiElementBuilder getGuiElementBuilder(ConfiguredBalloon configuredBalloon) {
        GuiElementBuilder builder = configuredBalloon.guiElementBuilder();
        builder.setCallback((index, type, action) -> {
            Util.clickSound(getPlayer());

            if (type == ClickType.MOUSE_LEFT) {
                Balloons.getStorage().setActive(getPlayer().getUUID(), configuredBalloon.id());
                this.close();
            } else {
                for (Map.Entry<ResourceLocation, ConfiguredBalloon> entry : Balloons.all().entrySet()) {
                    if (entry.getValue() == configuredBalloon) {
                        var ignored = new ConfirmationGui(getPlayer(), entry.getKey(), () -> new SelectionGui(getPlayer(), false)).open();
                        return;
                    }
                }
            }
        });
        return builder;
    }
}
