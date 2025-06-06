package de.tomalbrc.balloons.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.Map;
import java.util.UUID;

public class PlayerBalloonDataStorage extends SavedData implements StorageUtil.Provider {
    public static final Codec<Map<UUID, ResourceLocation>> ACTIVE_DATA_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, ResourceLocation.CODEC);
    public static final Codec<PlayerBalloonDataStorage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ACTIVE_DATA_CODEC.fieldOf("active").forGetter(data -> data.activeData)
    ).apply(instance, PlayerBalloonDataStorage::new));

    public static SavedDataType<PlayerBalloonDataStorage> TYPE = new SavedDataType<>("balloons", (context) -> new PlayerBalloonDataStorage(), ctx -> CODEC, null);

    private final Map<UUID, ResourceLocation> activeData = new Object2ObjectArrayMap<>();

    public PlayerBalloonDataStorage() {
        StorageUtil.addProvider(this);
    }

    public PlayerBalloonDataStorage(Map<UUID, ResourceLocation> activeData) {
        this();
        this.activeData.putAll(activeData);
    }

    @Override
    public ResourceLocation getActiveBalloon(UUID playerUuid) {
        return this.activeData.get(playerUuid);
    }

    public void setActiveBalloon(UUID playerUuid, ResourceLocation activeId) {
        if (activeId != null) {
            activeData.put(playerUuid, activeId);
        } else {
            activeData.remove(playerUuid);
        }
        setDirty();
    }

    public void removeActiveBalloon(UUID playerUuid) {
        activeData.remove(playerUuid);
        setDirty();
    }
}