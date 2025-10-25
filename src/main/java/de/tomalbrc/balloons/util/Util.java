package de.tomalbrc.balloons.util;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class Util {
    public static List<Pair<EquipmentSlot, ItemStack>> getEquipment(LivingEntity entity, boolean empty) {
        List<Pair<EquipmentSlot, ItemStack>> equipment = new ObjectArrayList<>();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (empty) {
                equipment.add(Pair.of(slot, ItemStack.EMPTY));
            } else {
                ItemStack stack = entity.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    equipment.add(Pair.of(slot, stack.copy()));
                }
            }
        }

        return equipment;
    }

    public static MenuType<?> menuTypeForHeight(int height) {
        return switch (height) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            case 6 -> MenuType.GENERIC_9x6;
            default -> MenuType.GENERIC_3x3;
        };
    }

    public static void clickSound(ServerPlayer player) {
        player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 0.5f, 1F);
    }
}
