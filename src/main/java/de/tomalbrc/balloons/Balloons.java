package de.tomalbrc.balloons;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import de.tomalbrc.balloons.command.BalloonCommand;
import de.tomalbrc.balloons.component.ModComponents;
import de.tomalbrc.balloons.config.ConfiguredBalloon;
import de.tomalbrc.balloons.config.ModConfig;
import de.tomalbrc.balloons.filament.FilamentCompat;
import de.tomalbrc.balloons.filament.TrinketCompat;
import de.tomalbrc.balloons.filament.VanillaCompat;
import de.tomalbrc.balloons.impl.VirtualBalloon;
import de.tomalbrc.balloons.storage.CachedBalloonsStorageProxy;
import de.tomalbrc.balloons.storage.DatabaseConfig;
import de.tomalbrc.balloons.storage.MongoStorage;
import de.tomalbrc.balloons.storage.hikari.MariaStorage;
import de.tomalbrc.balloons.storage.hikari.PostgresStorage;
import de.tomalbrc.balloons.storage.hikari.SqliteStorage;
import de.tomalbrc.balloons.util.StorageUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Balloons implements ModInitializer {
    public static final String MODID = "balloons";

    // map of player-uuid => map of spawned ballons
    public static Map<UUID, VirtualBalloon> SPAWNED_BALLOONS = new ConcurrentHashMap<>();

    public static Map<Identifier, ConfiguredBalloon> GROUPED = new Object2ObjectArrayMap<>();
    public static Map<Identifier, ConfiguredBalloon> UNGROUPED = new Object2ObjectArrayMap<>();

    public static void addGrouped(Identifier id, ConfiguredBalloon balloon) {
        GROUPED.put(id, balloon);
    }

    public static void addUngrouped(Identifier id, ConfiguredBalloon balloon) {
        UNGROUPED.put(id, balloon);
    }

    public static ImmutableMap<Identifier, ConfiguredBalloon> all() {
        return ImmutableMap.<Identifier, ConfiguredBalloon>builder().putAll(GROUPED).putAll(UNGROUPED).build();
    }

    public static StorageUtil.Provider STORAGE = null;

    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        ModComponents.register();
        ModEntities.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> BalloonCommand.register(dispatcher));

        Models.load();
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((minecraftServer, closeableResourceManager) -> Models.load());

        VanillaCompat.init();

        if (FilamentCompat.isLoaded()) {
            FilamentCompat.init();
        }

        if (TrinketCompat.isLoaded()) {
            TrinketCompat.init();
        }

        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
            // load configs
            ModConfig.load();
            for (ConfiguredBalloon configBalloon : ModConfig.getInstance().balloons) {
                addUngrouped(configBalloon.id(), configBalloon);
            }
            BalloonFiles.load();
           // Categories.load();

            STORAGE = getStorage();
        });

        ServerTickEvents.END_SERVER_TICK.register(Balloons::onTick);

        ServerPlayConnectionEvents.JOIN.register(Balloons::onJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(Balloons::onDisconnect);

        ServerLivingEntityEvents.AFTER_DEATH.register((livingEntity, damageSource) -> despawnBalloon(livingEntity));
        ServerPlayerEvents.COPY_FROM.register((serverPlayer, serverPlayer1, b) -> {
            despawnBalloon(serverPlayer);
            spawnActive(serverPlayer1);
        });
    }

    private static void onJoin(ServerGamePacketListenerImpl serverGamePacketListener, PacketSender packetSender, MinecraftServer server) {
        spawnActive(serverGamePacketListener.player);
    }

    private static void onDisconnect(ServerGamePacketListenerImpl serverGamePacketListener, MinecraftServer server) {
        despawnBalloon(serverGamePacketListener.player);
        Balloons.getStorage().invalidate(serverGamePacketListener.player.getUUID());
    }

    private static void onTick(MinecraftServer server) {
        CompletableFuture.runAsync(() -> {
            for (Map.Entry<UUID, VirtualBalloon> balloon : SPAWNED_BALLOONS.entrySet()) {
                balloon.getValue().tick();
            }
        });
    }

    public static void spawnBalloon(Entity livingEntity, Identifier balloonId) {
        if (!(livingEntity.level() instanceof ServerLevel))
            return;

        var balloon = Balloons.all().get(balloonId);
        if (balloon == null)
            return;

        var virtualBalloon = new VirtualBalloon(livingEntity);

        var old = SPAWNED_BALLOONS.put(livingEntity.getUUID(), virtualBalloon);
        if (old != null && old.getHolder() != null) {
            old.destroy();
        }

        virtualBalloon.setup(balloon.data());
    }

    public static void despawnBalloon(Entity livingEntity) {
        var virtualBalloon = SPAWNED_BALLOONS.remove(livingEntity.getUUID());
        if (virtualBalloon != null) {
            virtualBalloon.destroy();
        }
    }

    public static void spawnActive(LivingEntity livingEntity) {
        var active = StorageUtil.getActive(livingEntity);
        if (active != null && Balloons.all().containsKey(active)) {
            spawnBalloon(livingEntity, active);
        }
    }

    public static StorageUtil.Provider getStorage() {
        if (STORAGE != null) return STORAGE;

        ModConfig config = ModConfig.getInstance();
        DatabaseConfig dbConfig = config.database;
        StorageUtil.Type type = config.storageType;

        if (dbConfig != null && type != null && type != StorageUtil.Type.SQLITE) {
            switch (type) {
                case MARIADB -> STORAGE = new MariaStorage(dbConfig);
                case MONGODB -> STORAGE = new MongoStorage(dbConfig);
                case POSTGRESQL -> STORAGE = new PostgresStorage(dbConfig);
            }
        } else {
            STORAGE = new SqliteStorage(dbConfig);
        }

        STORAGE = new CachedBalloonsStorageProxy(STORAGE);

        return STORAGE;
    }
}
