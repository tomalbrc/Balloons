package de.tomalbrc.balloons.storage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.tomalbrc.balloons.util.StorageUtil;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class CachedBalloonsStorageProxy implements StorageUtil.Provider {

    private final StorageUtil.Provider delegate;

    private final Cache<String, List<Identifier>> availableCache;
    private final Cache<String, List<Identifier>> favCache;
    private final Cache<String, Identifier> activeCache;

    public CachedBalloonsStorageProxy(StorageUtil.Provider delegate) {
        this.delegate = delegate;

        this.availableCache = CacheBuilder.newBuilder()
                .maximumSize(200)
                .build();

        this.favCache = CacheBuilder.newBuilder()
                .maximumSize(200)
                .build();

        this.activeCache = CacheBuilder.newBuilder()
                .maximumSize(200)
                .build();
    }

    private String key(UUID uuid) {
        return uuid.toString();
    }

    @Override
    public boolean add(UUID playerUUID, Identifier id) {
        boolean ok = delegate.add(playerUUID, id);
        if (ok) {
            availableCache.invalidate(key(playerUUID));
        }
        return ok;
    }

    @Override
    public boolean remove(UUID playerUUID, Identifier id) {
        boolean ok = delegate.remove(playerUUID, id);
        if (ok) {
            String k = key(playerUUID);
            availableCache.invalidate(k);
            favCache.invalidate(k);
            activeCache.invalidate(k);
        }
        return ok;
    }

    @Override
    public boolean removeActive(UUID playerUUID) {
        boolean ok = delegate.removeActive(playerUUID);
        if (ok) {
            activeCache.invalidate(key(playerUUID));
        }
        return ok;
    }

    @Override
    public boolean setActive(UUID playerUUID, Identifier id) {
        boolean ok = delegate.setActive(playerUUID, id);
        if (ok) {
            activeCache.invalidate(key(playerUUID));
        }
        return ok;
    }

    @Override
    public Identifier getActive(UUID playerUUID) {
        String k = key(playerUUID);
        Identifier cached = activeCache.getIfPresent(k);
        if (cached != null) return cached;

        Identifier fromDelegate = delegate.getActive(playerUUID);
        if (fromDelegate != null) {
            activeCache.put(k, fromDelegate);
        }
        return fromDelegate;
    }

    @Override
    public List<Identifier> list(UUID playerUUID) {
        String k = key(playerUUID);
        try {
            return availableCache.get(k, () -> delegate.list(playerUUID));
        } catch (ExecutionException e) {
            return delegate.list(playerUUID);
        }
    }

    @Override
    public void close() {
        availableCache.invalidateAll();
        favCache.invalidateAll();
        activeCache.invalidateAll();
        delegate.close();
    }

    @Override
    public boolean addFav(UUID player, Identifier id) {
        boolean ok = delegate.addFav(player, id);
        if (ok) {
            favCache.invalidate(key(player));
        }
        return ok;
    }

    @Override
    public boolean removeFav(UUID player, Identifier id) {
        boolean ok = delegate.removeFav(player, id);
        if (ok) {
            favCache.invalidate(key(player));
        }
        return ok;
    }

    @Override
    public List<Identifier> listFavs(UUID player) {
        String k = key(player);
        try {
            return favCache.get(k, () -> delegate.listFavs(player));
        } catch (ExecutionException e) {
            return delegate.listFavs(player);
        }
    }

    @Override
    public void invalidate(UUID player) {
        favCache.invalidate(key(player));
        availableCache.invalidate(key(player));
        activeCache.invalidate(key(player));
    }
}
