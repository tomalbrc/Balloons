package de.tomalbrc.balloons.util;

import de.tomalbrc.balloons.Balloons;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class StorageUtil {
    public static Identifier getActive(LivingEntity entity) {
        return Balloons.getStorage().getActive(entity.getUUID());
    }

    public static boolean setActive(ServerPlayer player, Identifier id) {
        return Balloons.getStorage().setActive(player.getUUID(), id);
    }

    public static boolean removeActive(ServerPlayer player) {
        return Balloons.getStorage().removeActive(player.getUUID());
    }

    public static boolean add(UUID target, Identifier id) {
        return Balloons.getStorage().add(target, id);
    }

    public static boolean remove(UUID target, Identifier id) {
        return Balloons.getStorage().remove(target, id);
    }

    public static List<Identifier> list(UUID target) {
        return Balloons.getStorage().list(target);
    }

    public static boolean owns(ServerPlayer player, Identifier id) {
        return Balloons.getStorage().list(player.getUUID()).contains(id);
    }


    public enum Type {
        MARIADB,
        POSTGRESQL,
        SQLITE,
        MONGODB,
    }

    public interface Provider {
        boolean add(UUID playerUUID, Identifier id);

        boolean remove(UUID playerUUID, Identifier id);

        boolean removeActive(UUID playerUUID);

        boolean setActive(UUID playerUUID, Identifier id);

        @Nullable Identifier getActive(UUID serverPlayer);

        List<Identifier> list(UUID player);

        default void close() {}
        default void invalidate(UUID player) {}

        boolean addFav(UUID player, Identifier id);

        boolean removeFav(UUID player, Identifier id);

        List<Identifier> listFavs(UUID player);
    }
}
