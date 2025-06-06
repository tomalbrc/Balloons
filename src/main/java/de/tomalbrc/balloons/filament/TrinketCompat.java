package de.tomalbrc.balloons.filament;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.filament.item.FilamentItem;
import dev.emi.trinkets.api.event.TrinketEquipCallback;
import dev.emi.trinkets.api.event.TrinketUnequipCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class TrinketCompat {
    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded("trinkets");
    }

    public static void init() {
        TrinketEquipCallback.EVENT.register(((itemStack, slotReference, livingEntity) -> {
            if (livingEntity instanceof ServerPlayer serverPlayer && isValidBalloonItem(itemStack)) {

                Balloons.removeBalloonIfActive(serverPlayer);
            }
        }));
        TrinketUnequipCallback.EVENT.register(((itemStack, slotReference, livingEntity) -> {
            if (livingEntity instanceof ServerPlayer serverPlayer && isValidBalloonItem(itemStack)) {
                Balloons.removeBalloonIfActive(serverPlayer);
            }
        }));
    }

    public static boolean isValidBalloonItem(ItemStack itemStack) {
        return itemStack.getItem() instanceof FilamentItem filamentItem && filamentItem.has(FilamentCompat.BALLON_BEHAVIOUR) && Balloons.REGISTERED_BALLOONS.containsKey(filamentItem.get(FilamentCompat.BALLON_BEHAVIOUR).getConfig().modelId());
    }
}
