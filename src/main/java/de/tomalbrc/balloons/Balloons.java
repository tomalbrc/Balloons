package de.tomalbrc.balloons;

import com.mojang.logging.LogUtils;
import de.tomalbrc.balloons.command.BalloonCommand;
import de.tomalbrc.balloons.component.ModComponents;
import de.tomalbrc.balloons.config.ConfiguredBalloon;
import de.tomalbrc.balloons.config.ModConfig;
import de.tomalbrc.balloons.filament.FilamentCompat;
import de.tomalbrc.balloons.filament.TrinketCompat;
import de.tomalbrc.balloons.filament.VanillaCompat;
import de.tomalbrc.balloons.impl.VirtualBalloon;
import de.tomalbrc.balloons.storage.DatabaseConfig;
import de.tomalbrc.balloons.storage.MongoStorage;
import de.tomalbrc.balloons.storage.hikari.MariaStorage;
import de.tomalbrc.balloons.storage.hikari.PostgresStorage;
import de.tomalbrc.balloons.storage.hikari.SqliteStorage;
import de.tomalbrc.balloons.util.StorageUtil;
import de.tomalbrc.bil.core.model.Model;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Balloons implements ModInitializer {
    public static final String MODID = "balloons";
    public static Map<UUID, Map<ResourceLocation, VirtualBalloon>> SPAWNED_BALLOONS = new ConcurrentHashMap<>();

    public static Map<ResourceLocation, ConfiguredBalloon> GROUPED = new Object2ObjectArrayMap<>();
    public static Map<ResourceLocation, ConfiguredBalloon> UNGROUPED = new Object2ObjectArrayMap<>();

    public static void addGrouped(ResourceLocation id, ConfiguredBalloon balloon) {
        GROUPED.put(id, balloon);
    }

    public static void addUngrouped(ResourceLocation id, ConfiguredBalloon balloon) {
        UNGROUPED.put(id, balloon);
    }

    public static Map<ResourceLocation, ConfiguredBalloon> all() {
        Map<ResourceLocation, ConfiguredBalloon> map = new Object2ObjectArrayMap<>();
        map.putAll(GROUPED);
        map.putAll(UNGROUPED);
        return map;
    }

    public static StorageUtil.Provider STORAGE = null;

    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        ModComponents.register();

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

        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
            // load configs
            ModConfig.load();
            for (ConfiguredBalloon configBalloon : ModConfig.getInstance().balloons) {
                UNGROUPED.put(configBalloon.id(), configBalloon);
            }

            STORAGE = getStorage();
        });

        ServerTickEvents.END_SERVER_TICK.register(Balloons::onTick);

        ServerPlayConnectionEvents.JOIN.register(Balloons::onJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(Balloons::onDisconnect);

        ServerLivingEntityEvents.AFTER_DEATH.register((livingEntity, damageSource) -> {
            removeAllBalloons(livingEntity);
        });
        ServerPlayerEvents.COPY_FROM.register((serverPlayer, serverPlayer1, b) -> {
            removeAllBalloons(serverPlayer);
            addBalloonIfActive(serverPlayer1);
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, serverLevel) -> {
            if (entity instanceof LivingEntity livingEntity) {
                removeAllBalloons(livingEntity);
            }
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, serverLevel) -> {
            if (entity instanceof ServerPlayer serverPlayer) {
                addBalloonIfActive(serverPlayer);
            }
        });
    }

    private static void onJoin(ServerGamePacketListenerImpl serverGamePacketListener, PacketSender packetSender, MinecraftServer server) {
        addBalloonIfActive(serverGamePacketListener.player);
    }

    private static void onDisconnect(ServerGamePacketListenerImpl serverGamePacketListener, MinecraftServer server) {
        removeAllBalloons(serverGamePacketListener.player);
    }

    private static void onTick(MinecraftServer server) {
        CompletableFuture.runAsync(() -> {
            for (Map.Entry<UUID, Map<ResourceLocation, VirtualBalloon>> balloon : SPAWNED_BALLOONS.entrySet()) {
                for (Map.Entry<ResourceLocation, VirtualBalloon> entry : balloon.getValue().entrySet()) {
                    entry.getValue().tick();
                }
            }
        });
    }

    private static void spawnBalloon(LivingEntity livingEntity, ResourceLocation balloonId) {
        var balloon = Balloons.all().get(balloonId);
        Model model = Models.getModel(balloon.data().model());
        var virtualBalloon = new VirtualBalloon(livingEntity);
        virtualBalloon.setModel(model, balloon.data().showLeash());

        var spawnData = SPAWNED_BALLOONS.computeIfAbsent(livingEntity.getUUID(), (living) -> new Object2ObjectArrayMap<>());
        spawnData.put(balloonId, virtualBalloon);

        if (balloon.data().animation() != null)
            virtualBalloon.play(balloon.data().animation());

        virtualBalloon.attach(balloon.data());
    }

    public static void removeAllBalloons(LivingEntity livingEntity) {
        var spawnData = SPAWNED_BALLOONS.get(livingEntity.getUUID());
        if (spawnData != null) {
            for (VirtualBalloon value : spawnData.values()) {
                value.getHolder().destroy();
            }
            SPAWNED_BALLOONS.remove(livingEntity.getUUID());
        }
    }

    public static void removeBalloon(LivingEntity livingEntity, ResourceLocation id) {
        var balloonMap = SPAWNED_BALLOONS.get(livingEntity.getUUID());
        if (balloonMap != null) {
            var virtualBalloon = balloonMap.get(id);
            virtualBalloon.getHolder().destroy();
            balloonMap.remove(id);

            if (balloonMap.isEmpty())
                SPAWNED_BALLOONS.remove(livingEntity.getUUID());
        }
    }

    public static void addBalloon(LivingEntity livingEntity, ResourceLocation balloonId) {
        var balloonMap = SPAWNED_BALLOONS.computeIfAbsent(livingEntity.getUUID(), (living) -> new Object2ObjectArrayMap<>());
        if (!balloonMap.containsKey(balloonId)) {
            if (Balloons.all().containsKey(balloonId)) {
                spawnBalloon(livingEntity, balloonId);
            }
        }
    }

    public static void addBalloonIfActive(LivingEntity livingEntity) {
        var balloonMap = SPAWNED_BALLOONS.computeIfAbsent(livingEntity.getUUID(), (living) -> new Object2ObjectArrayMap<>());
        var active = StorageUtil.getActive(livingEntity);
        if (active != null && !balloonMap.containsKey(active) && Balloons.all().containsKey(active)) {
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

        return STORAGE;
    }
}
