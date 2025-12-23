package de.tomalbrc.balloons.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TempStorageProvider implements StorageUtil.Provider {
    Map<UUID, Identifier> map = new Object2ObjectArrayMap<>();

    @Override
    public @Nullable Identifier getActive(UUID uuid) {
        return this.map.get(uuid);
    }

    @Override
    public List<Identifier> list(UUID player) {
        return List.of();
    }

    public boolean setActive(UUID uuid, Identifier id) {
        this.map.put(uuid, id);
        return true;
    }

    @Override
    public boolean add(UUID playerUUID, Identifier id) {
        return false;
    }

    @Override
    public boolean remove(UUID playerUUID, Identifier id) {
        return false;
    }

    public boolean removeActive(UUID uuid) {
        return this.map.remove(uuid) != null;
    }

    @Override
    public boolean addFav(UUID player, Identifier id) {
        return false;
    }

    @Override
    public boolean removeFav(UUID player, Identifier id) {
        return false;
    }

    @Override
    public List<Identifier> listFavs(UUID player) {
        return List.of();
    }
}
