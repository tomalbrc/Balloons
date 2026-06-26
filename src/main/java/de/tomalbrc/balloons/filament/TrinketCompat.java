package de.tomalbrc.balloons.filament;

import de.tomalbrc.balloons.Balloons;
import eu.pb4.trinkets.api.event.TrinketEquipCallback;
import eu.pb4.trinkets.api.event.TrinketUnequipCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;

public class TrinketCompat {
    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded("trinkets");
    }

    public static void init() {
        TrinketEquipCallback.EVENT.register(((itemStack, slotReference, livingEntity) -> {
            if (livingEntity instanceof ServerPlayer serverPlayer && VanillaCompat.isValidBalloonItem(itemStack)) {
                VanillaCompat.TEMP_PROVIDER.setActive(serverPlayer.getUUID(), VanillaCompat.getBalloonId(itemStack));
                Balloons.spawnBalloon(serverPlayer, BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
            }
        }));

        TrinketUnequipCallback.EVENT.register(((itemStack, slotReference, livingEntity) -> {
            if (livingEntity instanceof ServerPlayer serverPlayer && VanillaCompat.isValidBalloonItem(itemStack)) {
                Balloons.despawnBalloon(serverPlayer);
                VanillaCompat.TEMP_PROVIDER.removeActive(serverPlayer.getUUID());
            }
        }));
    }
}
