package de.tomalbrc.balloons.util;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.config.ModConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class StorageUtil {
    private static final List<Provider> PROVIDERS = new ObjectArrayList<>();

    public static void addProvider(Provider provider) {
        PROVIDERS.add(provider);
    }

    public static ResourceLocation getActive(LivingEntity entity) {
        for (Provider provider : PROVIDERS) {
            var active = provider.getActiveBalloon(entity.getUUID());
            if (active != null) {
                return active;
            }
        }

        return null;
    }

    public static void setActive(ServerPlayer player, ResourceLocation id) {
        if (ModConfig.getInstance().mongoDb != null && ModConfig.getInstance().mongoDb.enabled) {
            Balloons.DATABASE.setActiveBalloon(player.getUUID(), id);
        } else {
            Balloons.PERSISTENT_DATA.setActiveBalloon(player.getUUID(), id);
        }
    }

    public static void removeActive(ServerPlayer player) {
        if (ModConfig.getInstance().mongoDb != null && ModConfig.getInstance().mongoDb.enabled) {
            Balloons.DATABASE.removeActiveBalloon(player.getUUID());
        } else {
            Balloons.PERSISTENT_DATA.removeActiveBalloon(player.getUUID());
        }
    }

    public interface Provider {
        @Nullable ResourceLocation getActiveBalloon(UUID serverPlayer);
    }
}
