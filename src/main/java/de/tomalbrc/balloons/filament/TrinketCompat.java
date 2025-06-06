package de.tomalbrc.balloons.filament;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.util.StorageUtil;
import de.tomalbrc.filament.item.FilamentItem;
import dev.emi.trinkets.api.event.TrinketEquipCallback;
import dev.emi.trinkets.api.event.TrinketUnequipCallback;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class TrinketCompat {
    public static TempStorageProvider TRINKET_PROVIDER = new TempStorageProvider();

    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded("trinkets");
    }

    public static void init() {
        TrinketEquipCallback.EVENT.register(((itemStack, slotReference, livingEntity) -> {
            if (livingEntity instanceof ServerPlayer serverPlayer && isValidBalloonItem(itemStack)) {
                TRINKET_PROVIDER.setActive(serverPlayer.getUUID(), getBalloonId(itemStack));
                Balloons.addBalloonIfActive(serverPlayer);
            }
        }));
        TrinketUnequipCallback.EVENT.register(((itemStack, slotReference, livingEntity) -> {
            if (livingEntity instanceof ServerPlayer serverPlayer && isValidBalloonItem(itemStack)) {
                Balloons.removeBalloonIfActive(serverPlayer);
                TRINKET_PROVIDER.removeActive(serverPlayer.getUUID());
            }
        }));

        StorageUtil.addProvider(TRINKET_PROVIDER);
    }

    public static boolean isValidBalloonItem(ItemStack itemStack) {
        return itemStack.getItem() instanceof FilamentItem filamentItem && filamentItem.has(FilamentCompat.BALLON_BEHAVIOUR);
    }

    public static ResourceLocation getBalloonId(ItemStack itemStack) {
        if (isValidBalloonItem(itemStack)) {
            return itemStack.getItem().builtInRegistryHolder().key().location();
        }

        return null;
    }

    private static class TempStorageProvider implements StorageUtil.Provider {
        Map<UUID, ResourceLocation> map = new Object2ObjectArrayMap<>();

        @Override
        public @Nullable ResourceLocation getActiveBalloon(UUID uuid) {
            return this.map.get(uuid);
        }

        public void setActive(UUID uuid, ResourceLocation id) {
            this.map.put(uuid, id);
        }

        public void removeActive(UUID uuid) {
            this.map.remove(uuid);
        }
    }
}
