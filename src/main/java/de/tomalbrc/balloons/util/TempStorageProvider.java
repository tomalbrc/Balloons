package de.tomalbrc.balloons.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TempStorageProvider implements StorageUtil.Provider {
    Map<UUID, ResourceLocation> map = new Object2ObjectArrayMap<>();

    @Override
    public @Nullable ResourceLocation getActive(UUID uuid) {
        return this.map.get(uuid);
    }

    @Override
    public List<ResourceLocation> list(UUID player) {
        return List.of();
    }

    public boolean setActive(UUID uuid, ResourceLocation id) {
        this.map.put(uuid, id);
        return true;
    }

    @Override
    public boolean add(UUID playerUUID, ResourceLocation id) {
        return false;
    }

    @Override
    public boolean remove(UUID playerUUID, ResourceLocation id) {
        return false;
    }

    public boolean removeActive(UUID uuid) {
        return this.map.remove(uuid) != null;
    }
}
