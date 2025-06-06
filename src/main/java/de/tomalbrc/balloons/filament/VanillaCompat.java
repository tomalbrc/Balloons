package de.tomalbrc.balloons.filament;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.util.StorageUtil;
import de.tomalbrc.balloons.util.TempStorageProvider;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class VanillaCompat {
    // not persistent when dead, tied to armor
    public static final TempStorageProvider TEMP_PROVIDER = new TempStorageProvider();

    public static void init() {
        ServerEntityEvents.EQUIPMENT_CHANGE.register(((livingEntity, slot, prev, next) -> {
            if (prev.has(Balloons.COMPONENT) && livingEntity.getEquipmentSlotForItem(prev) == slot) {
                Balloons.removeBalloon(livingEntity, BuiltInRegistries.ITEM.getKey(prev.getItem()));
                TEMP_PROVIDER.removeActive(livingEntity.getUUID());
            }

            if (next.has(Balloons.COMPONENT) && livingEntity.getEquipmentSlotForItem(next) == slot) {
                TEMP_PROVIDER.setActive(livingEntity.getUUID(), getBalloonId(next));
                Balloons.addBalloon(livingEntity, BuiltInRegistries.ITEM.getKey(next.getItem()));
            }
        }));

        StorageUtil.addProvider(TEMP_PROVIDER);
    }

    public static boolean isValidBalloonItem(ItemStack itemStack) {
        return itemStack.has(Balloons.COMPONENT);
    }

    public static ResourceLocation getBalloonId(ItemStack itemStack) {
        if (isValidBalloonItem(itemStack)) {
            return itemStack.getItem().builtInRegistryHolder().key().location();
        }

        return null;
    }
}
