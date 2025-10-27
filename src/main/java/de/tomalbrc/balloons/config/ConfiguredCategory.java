package de.tomalbrc.balloons.config;

import com.google.common.collect.ImmutableMap;
import de.tomalbrc.balloons.util.TextUtil;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;

public record ConfiguredCategory(
        String id,
        String title,
        ResourceLocation item,
        List<String> lore,
        ResourceLocation model,
        boolean glint,
        Map<ResourceLocation, de.tomalbrc.balloons.config.ConfiguredBalloon> balloons
) {
    public ItemStack itemStack() {
        ItemStack itemStack;

        if (item == null)
            itemStack = Items.ROTTEN_FLESH.getDefaultInstance();
        else
            itemStack = BuiltInRegistries.ITEM.getValue(item).getDefaultInstance();

        if (title != null) itemStack.set(DataComponents.ITEM_NAME, Component.empty().append(Component.empty().withStyle(de.tomalbrc.balloons.config.ConfiguredBalloon.EMPTY).append(TextUtil.parse(title))));
        if (model != null)
            itemStack.set(DataComponents.ITEM_MODEL, model);

        return itemStack;
    }

    public GuiElementBuilder guiElementBuilder() {
        var builder = GuiElementBuilder.from(itemStack());
        if (lore != null) for (String string : lore) {
            builder.addLoreLine(Component.empty().append(Component.empty().withStyle(de.tomalbrc.balloons.config.ConfiguredBalloon.EMPTY).append(TextUtil.parse(string))));
        }
        builder.glow(glint());
        return builder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String title;
        private ResourceLocation item;
        private final List<String> lore = new ObjectArrayList<>();
        private ResourceLocation model;
        private boolean glint;
        private final Map<ResourceLocation, de.tomalbrc.balloons.config.ConfiguredBalloon> balloons = new Object2ObjectOpenHashMap<>();

        private Builder() {}

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setItem(ResourceLocation item) {
            this.item = item;
            return this;
        }

        public Builder setModel(ResourceLocation model) {
            this.model = model;
            return this;
        }

        public Builder setGlint(boolean glint) {
            this.glint = glint;
            return this;
        }

        public Builder addBalloon(ResourceLocation id, de.tomalbrc.balloons.config.ConfiguredBalloon balloon) {
            this.balloons.put(id, balloon);
            return this;
        }

        public Builder addBalloons(Map<ResourceLocation, ConfiguredBalloon> balloons) {
            this.balloons.putAll(balloons);
            return this;
        }

        public Builder addLore(String lore) {
            this.lore.add(lore);
            return this;
        }

        public Builder addLore(List<String> lore) {
            this.lore.addAll(lore);
            return this;
        }

        public ConfiguredCategory build() {
            return new ConfiguredCategory(
                    id,
                    title,
                    item,
                    lore,
                    model,
                    glint,
                    ImmutableMap.copyOf(balloons) // immutable copy
            );
        }
    }
}