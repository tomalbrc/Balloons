package de.tomalbrc.balloons.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class TempStorageProvider implements StorageUtil.Provider {
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
