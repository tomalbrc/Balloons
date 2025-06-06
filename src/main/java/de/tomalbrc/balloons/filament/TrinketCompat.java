package de.tomalbrc.balloons.filament;

import de.tomalbrc.balloons.Balloons;
import dev.emi.trinkets.api.event.TrinketEquipCallback;
import dev.emi.trinkets.api.event.TrinketUnequipCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

public class TrinketCompat {
    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded("trinkets");
    }

    public static void init() {
        TrinketEquipCallback.EVENT.register(((itemStack, slotReference, livingEntity) -> {
            if (livingEntity instanceof ServerPlayer serverPlayer && VanillaCompat.isValidBalloonItem(itemStack)) {
                VanillaCompat.PROVIDER.setActive(serverPlayer.getUUID(), VanillaCompat.getBalloonId(itemStack));
                Balloons.addBalloonIfActive(serverPlayer);
            }
        }));

        TrinketUnequipCallback.EVENT.register(((itemStack, slotReference, livingEntity) -> {
            if (livingEntity instanceof ServerPlayer serverPlayer && VanillaCompat.isValidBalloonItem(itemStack)) {
                Balloons.removeBalloonIfActive(serverPlayer);
                VanillaCompat.PROVIDER.removeActive(serverPlayer.getUUID());
            }
        }));
    }
}
