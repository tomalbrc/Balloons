package de.tomalbrc.balloons.filament;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.component.ModComponents;
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
            if (prev.has(ModComponents.BALLOON) && livingEntity.getEquipmentSlotForItem(prev) == slot) {
                Balloons.despawnBalloon(livingEntity);
                TEMP_PROVIDER.removeActive(livingEntity.getUUID());
            }

            if (next.has(ModComponents.BALLOON) && livingEntity.getEquipmentSlotForItem(next) == slot) {
                Balloons.spawnBalloon(livingEntity, BuiltInRegistries.ITEM.getKey(next.getItem()));
                TEMP_PROVIDER.setActive(livingEntity.getUUID(), getBalloonId(next));
            }
        }));
    }

    public static boolean isValidBalloonItem(ItemStack itemStack) {
        return itemStack.has(ModComponents.BALLOON);
    }

    public static ResourceLocation getBalloonId(ItemStack itemStack) {
        if (isValidBalloonItem(itemStack)) {
            return itemStack.getItem().builtInRegistryHolder().key().location();
        }

        return null;
    }
}
