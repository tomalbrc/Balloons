package de.tomalbrc.balloons.config;

import com.google.common.collect.ImmutableMap;
import de.tomalbrc.balloons.component.BalloonProperties;
import de.tomalbrc.balloons.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ConfiguredBalloon {
    public static Style EMPTY = Style.EMPTY.withColor(ChatFormatting.WHITE).withUnderlined(false).withItalic(false).withObfuscated(false).withStrikethrough(false);

    Identifier id;
    @Nullable ItemStack item;
    BalloonProperties data;
    String title;
    List<String> lore;
    boolean glint;
    String permission;
    int permissionLevel;

    public ConfiguredBalloon(Identifier id, String title, @Nullable ItemStack item, BalloonProperties data) {
        this.id = id;
        this.item = item;
        this.data = data;
        this.title = title;
    }

    public void setItem(@Nullable ItemStack item) {
        this.item = item;
    }

    public BalloonProperties data() {
        return data;
    }

    public ItemStack itemStack() {
        ItemStack itemStack = item;

        if (item == null)
            itemStack = Items.ROTTEN_FLESH.getDefaultInstance();

        if (title != null)
            itemStack.set(DataComponents.ITEM_NAME, Component.empty().append(Component.empty().withStyle(EMPTY).append(TextUtil.parse(title))));

        if (glint)
            itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        return itemStack;
    }

    public Identifier id() {
        return id;
    }

    public String title() {
        return title;
    }

    public @Nullable String permission() {
        return permission;
    }

    public int permissionLevel() {
        return permissionLevel;
    }

    public Map<String, String> placeholder() {
        return ImmutableMap.of(
                "<title>", title,
                "<id>", id.toString()
        );
    }
}
