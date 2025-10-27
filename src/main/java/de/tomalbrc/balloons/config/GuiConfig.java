package de.tomalbrc.balloons.config;

import de.tomalbrc.balloons.util.TextUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Vector2i;

public class GuiConfig {
    public boolean addBackButton = true;
    public ResourceLocation backButtonItem = Items.ARROW.builtInRegistryHolder().key().location();
    public Vector2i backButtonLocation = new Vector2i(1, 1);
    public ItemStack backItem() {
        var item = BuiltInRegistries.ITEM.getValue(backButtonItem).getDefaultInstance();
        item.set(DataComponents.ITEM_NAME, TextUtil.parse(de.tomalbrc.balloons.config.ModConfig.getInstance().messages.back));
        return item;
    }

    public int selectionMenuHeight = 6;
    public String selectionMenuTitle = "Select Balloon";

    public ResourceLocation prevButtonItem = Items.ARROW.builtInRegistryHolder().key().location();
    public ResourceLocation nextButtonItem = Items.ARROW.builtInRegistryHolder().key().location();
    public Vector2i prevButtonLocation = new Vector2i(8, 6);
    public Vector2i nextButtonLocation = new Vector2i(9, 6);

    public ItemStack prevItem() {
        var item = BuiltInRegistries.ITEM.getValue(prevButtonItem).getDefaultInstance();
        item.set(DataComponents.ITEM_NAME, TextUtil.parse(de.tomalbrc.balloons.config.ModConfig.getInstance().messages.prev));
        return item;
    }

    public ItemStack nextItem() {
        var item = BuiltInRegistries.ITEM.getValue(nextButtonItem).getDefaultInstance();
        item.set(DataComponents.ITEM_NAME, TextUtil.parse(de.tomalbrc.balloons.config.ModConfig.getInstance().messages.next));
        return item;
    }

    public int browseMenuHeight = 6;
    public String browseMenuTitle = "Browse Balloons";

    public int confirmationMenuHeight = 1;
    public String confirmationMenuTitle = "Confirm";

    public boolean addBrowseButton = true;
    public ResourceLocation browseButtonItem = Items.CHEST.builtInRegistryHolder().key().location();
    public Vector2i browseButtonLocation = new Vector2i(1, 6);
    public ItemStack browseItem() {
        var item = BuiltInRegistries.ITEM.getValue(browseButtonItem).getDefaultInstance();
        item.set(DataComponents.ITEM_NAME, TextUtil.parse(de.tomalbrc.balloons.config.ModConfig.getInstance().messages.browse));
        return item;
    }

    public ResourceLocation confirmButtonItem = Items.EMERALD.builtInRegistryHolder().key().location();
    public Vector2i confirmButtonLocation = new Vector2i(7, 1);
    public Vector2i cancelButtonLocation = new Vector2i(3, 1);
    public ItemStack confirmItem() {
        var item = BuiltInRegistries.ITEM.getValue(confirmButtonItem).getDefaultInstance();
        item.set(DataComponents.ITEM_NAME, TextUtil.parse(ModConfig.getInstance().messages.confirm));
        return item;
    }
}
