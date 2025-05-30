package de.tomalbrc.balloons.util;

import de.tomalbrc.balloons.Balloons;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class StorageUtil {
    public static ResourceLocation getActive(ServerPlayer player) {
        if (ModConfig.getInstance().mongoDb.enabled) {
            return Balloons.DATABASE.getActiveBalloon(player.getUUID());
        } else {
            return Balloons.PERSISTENT_DATA.getActiveBalloon(player.getUUID());
        }
    }

    public static void setActive(ServerPlayer player, ResourceLocation id) {
        if (ModConfig.getInstance().mongoDb.enabled) {
            Balloons.DATABASE.setActiveBalloon(player.getUUID(), id);
        } else {
            Balloons.PERSISTENT_DATA.setActiveBalloon(player.getUUID(), id);
        }
    }

    public static void removeActive(ServerPlayer player) {
        if (ModConfig.getInstance().mongoDb.enabled) {
            Balloons.DATABASE.removeActiveBalloon(player.getUUID());
        } else {
            Balloons.PERSISTENT_DATA.removeActiveBalloon(player.getUUID());
        }
    }
}
