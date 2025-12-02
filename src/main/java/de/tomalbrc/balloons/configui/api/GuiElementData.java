package de.tomalbrc.balloons.configui.api;

import de.tomalbrc.balloons.util.TextUtil;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public record GuiElementData(String type, String title, ItemStack item, List<String> lore, List<String> altLore, boolean glint) {
    public static Style EMPTY_STYLE = Style.EMPTY.withColor(ChatFormatting.WHITE).withUnderlined(false).withItalic(false).withObfuscated(false).withStrikethrough(false);

    public GuiElementBuilder decorate(GuiElementBuilder builder) {
        return decorate(builder, Map.of(), false);
    }

    public GuiElementBuilder decorate(GuiElementBuilder builder, Map<String, String> placeholder) {
        return decorate(builder, placeholder, false);
    }

    public GuiElementBuilder decorate(GuiElementBuilder builder, Map<String, String> placeholder, boolean alt) {
        if (title != null) {
            builder.setName(TextUtil.parse(title));
        }

        if (!alt && lore != null) {
            for (String string : lore) {
                for (Map.Entry<String, String> entry : placeholder.entrySet()) {
                    string = string.replace(entry.getKey(), entry.getValue());
                }

                builder.addLoreLine(Component.empty().append(Component.empty().withStyle(EMPTY_STYLE).append(TextUtil.parse(string))));
            }
        }

        if (alt && altLore != null) {
            for (String string : altLore) {
                for (Map.Entry<String, String> entry : placeholder.entrySet()) {
                    string = string.replace(entry.getKey(), entry.getValue());
                }

                builder.addLoreLine(Component.empty().append(Component.empty().withStyle(EMPTY_STYLE).append(TextUtil.parse(string))));
            }
        }

        if (glint)
            builder.glow();

        return builder;
    }
}
