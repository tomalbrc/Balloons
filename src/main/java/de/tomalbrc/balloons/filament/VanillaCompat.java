package de.tomalbrc.balloons.filament;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.util.StorageUtil;
import de.tomalbrc.balloons.util.TempStorageProvider;
import dev.emi.trinkets.api.event.TrinketUnequipCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class VanillaCompat {
    public static final TempStorageProvider PROVIDER = new TempStorageProvider();

    public static void init() {
        ServerEntityEvents.EQUIPMENT_CHANGE.register(((livingEntity, slot, prev, next) -> {
            if (livingEntity instanceof ServerPlayer serverPlayer && prev.has(Balloons.COMPONENT)) {
                Balloons.removeBalloonIfActive(serverPlayer);
                PROVIDER.removeActive(serverPlayer.getUUID());
            }

            if (livingEntity instanceof ServerPlayer serverPlayer && next.has(Balloons.COMPONENT)) {
                PROVIDER.setActive(serverPlayer.getUUID(), getBalloonId(next));
                Balloons.addBalloonIfActive(serverPlayer);
            }
        }));
        
        TrinketUnequipCallback.EVENT.register(((itemStack, slotReference, livingEntity) -> {
            if (livingEntity instanceof ServerPlayer serverPlayer && isValidBalloonItem(itemStack)) {
                Balloons.removeBalloonIfActive(serverPlayer);
                PROVIDER.removeActive(serverPlayer.getUUID());
            }
        }));

        StorageUtil.addProvider(PROVIDER);
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
