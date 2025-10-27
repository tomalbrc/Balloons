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
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
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
                addUngrouped(configBalloon.id(), configBalloon);
            }

            STORAGE = getStorage();
        });

        ServerTickEvents.END_SERVER_TICK.register(Balloons::onTick);

        ServerPlayConnectionEvents.JOIN.register(Balloons::onJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(Balloons::onDisconnect);

        ServerLivingEntityEvents.AFTER_DEATH.register((livingEntity, damageSource) -> {
            despawnBalloon(livingEntity);
        });
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
    }

    private static void onTick(MinecraftServer server) {
        CompletableFuture.runAsync(() -> {
            for (Map.Entry<UUID, VirtualBalloon> balloon : SPAWNED_BALLOONS.entrySet()) {
                balloon.getValue().tick();
            }
        });
    }

    public static void spawnBalloon(LivingEntity livingEntity, ResourceLocation balloonId) {
        if (!(livingEntity.level() instanceof ServerLevel))
            return;

        var balloon = Balloons.all().get(balloonId);
        Model model = Models.getModel(balloon.data().model());
        var virtualBalloon = new VirtualBalloon(livingEntity);
        virtualBalloon.setModel(model, balloon.data().showLeash());

        var old = SPAWNED_BALLOONS.put(livingEntity.getUUID(), virtualBalloon);
        if (old != null && old.getHolder() != null) {
            old.getHolder().destroy();
        }

        if (balloon.data().animation() != null)
            virtualBalloon.play(balloon.data().animation());

        virtualBalloon.attach(balloon.data());
    }

    public static void despawnBalloon(LivingEntity livingEntity) {
        var virtualBalloon = SPAWNED_BALLOONS.remove(livingEntity.getUUID());
        if (virtualBalloon != null) {
            virtualBalloon.getHolder().destroy();
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

        return STORAGE;
    }
}
