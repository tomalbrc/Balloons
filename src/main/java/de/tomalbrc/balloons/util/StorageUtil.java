package de.tomalbrc.balloons.util;

import de.tomalbrc.balloons.Balloons;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class StorageUtil {
    static List<Provider> providers = new ObjectArrayList<>();

    public static void addProvider(Provider provider) {
        providers.add(provider);
    }

    public static ResourceLocation getActive(ServerPlayer player) {
        for (Provider provider : providers) {
            var active = provider.getActiveBalloon(player.getUUID());
            if (active != null) {
                return active;
            }
        }

        return null;
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

    public interface Provider {
        @Nullable ResourceLocation getActiveBalloon(UUID serverPlayer);
    }
}
