package de.tomalbrc.balloons.util;

import de.tomalbrc.balloons.Balloons;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class StorageUtil {
    public static ResourceLocation getActive(LivingEntity entity) {
        return Balloons.getStorage().getActive(entity.getUUID());
    }

    public static boolean setActive(ServerPlayer player, ResourceLocation id) {
        return Balloons.getStorage().setActive(player.getUUID(), id);
    }

    public static boolean removeActive(ServerPlayer player) {
        return Balloons.getStorage().removeActive(player.getUUID());
    }

    public static boolean add(UUID target, ResourceLocation id) {
        return Balloons.getStorage().add(target, id);
    }

    public static boolean remove(UUID target, ResourceLocation id) {
        return Balloons.getStorage().remove(target, id);
    }

    public static List<ResourceLocation> list(UUID target) {
        return Balloons.getStorage().list(target);
    }

    public static boolean owns(ServerPlayer player, ResourceLocation id) {
        return Balloons.getStorage().list(player.getUUID()).contains(id);
    }


    public enum Type {
        MARIADB,
        POSTGRESQL,
        SQLITE,
        MONGODB,
    }

    public interface Provider {
        boolean add(UUID playerUUID, ResourceLocation id);

        boolean remove(UUID playerUUID, ResourceLocation id);

        boolean removeActive(UUID playerUUID);

        boolean setActive(UUID playerUUID, ResourceLocation id);

        @Nullable ResourceLocation getActive(UUID serverPlayer);

        List<ResourceLocation> list(UUID player);

        default void close() {}
    }
}
