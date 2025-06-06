package de.tomalbrc.balloons;

import com.mojang.logging.LogUtils;
import de.tomalbrc.balloons.command.BalloonCommand;
import de.tomalbrc.balloons.filament.TrinketCompat;
import de.tomalbrc.balloons.impl.VirtualBalloon;
import de.tomalbrc.balloons.filament.FilamentCompat;
import de.tomalbrc.balloons.util.BalloonDatabase;
import de.tomalbrc.balloons.util.ModConfig;
import de.tomalbrc.balloons.util.PlayerBalloonData;
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
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Balloons implements ModInitializer {
    public static Map<ResourceLocation, ModConfig.ConfigBalloon> REGISTERED_BALLOONS = new Object2ObjectArrayMap<>();
    public static Map<ServerPlayer, VirtualBalloon> ACTIVE_BALLOONS = new ConcurrentHashMap<>();

    public static BalloonDatabase DATABASE = null;
    public static PlayerBalloonData PERSISTENT_DATA = null;

    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> BalloonCommand.register(dispatcher));

        Models.load();
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((minecraftServer, closeableResourceManager) -> Models.load());

        if (FilamentCompat.isLoaded()) {
            FilamentCompat.init();
            if (TrinketCompat.isLoaded()) {
                TrinketCompat.init();
            }
        }

        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
            // load configs
            ModConfig.load();
            for (ModConfig.ConfigBalloon configBalloon : ModConfig.getInstance().balloons) {
                REGISTERED_BALLOONS.put(configBalloon.id(), configBalloon);
            }

            PERSISTENT_DATA = minecraftServer.overworld().getDataStorage().computeIfAbsent(PlayerBalloonData.TYPE);

            if (ModConfig.getInstance().mongoDb.enabled)
                DATABASE = new BalloonDatabase(ModConfig.getInstance().mongoDb);
        });

        ServerTickEvents.END_SERVER_TICK.register(Balloons::onTick);
        ServerPlayConnectionEvents.JOIN.register(Balloons::onJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(Balloons::onDisconnect);
        ServerLivingEntityEvents.AFTER_DEATH.register((livingEntity, damageSource) -> {
            if (livingEntity instanceof ServerPlayer serverPlayer) {
                LOGGER.info("remove after death");
                removeBalloonIfActive(serverPlayer);
            }
        });
        ServerPlayerEvents.AFTER_RESPAWN.register(((oldPlayer, newPlayer, b) -> {
            LOGGER.info("after respawn");
            removeBalloonIfActive(oldPlayer);
            addBalloonIfActive(newPlayer);
        }));
        ServerPlayerEvents.COPY_FROM.register((serverPlayer, serverPlayer1, b) -> {
            LOGGER.info("copy from");

            removeBalloonIfActive(serverPlayer);
            addBalloonIfActive(serverPlayer1);
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, serverLevel) -> {
            if (entity instanceof ServerPlayer serverPlayer) {
                LOGGER.info("unload");

                removeBalloonIfActive(serverPlayer);
            }
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, serverLevel) -> {
            if (entity instanceof ServerPlayer serverPlayer) {
                LOGGER.info("entity load");

                addBalloonIfActive(serverPlayer);
            }
        });
    }

    private static void onJoin(ServerGamePacketListenerImpl serverGamePacketListener, PacketSender packetSender, MinecraftServer server) {
        LOGGER.info("onJoin");

        addBalloonIfActive(serverGamePacketListener.player);
    }

    private static void onDisconnect(ServerGamePacketListenerImpl serverGamePacketListener, MinecraftServer server) {
        LOGGER.info("onDisconnect");
        removeBalloonIfActive(serverGamePacketListener.player);
    }

    private static void onTick(MinecraftServer server) {
        CompletableFuture.runAsync(() -> {
            for (Map.Entry<ServerPlayer, VirtualBalloon> balloon : ACTIVE_BALLOONS.entrySet()) {
                if (!balloon.getKey().hasDisconnected()) balloon.getValue().tick();
            }
        });
    }

    private static void setBalloon(ServerPlayer serverPlayer, ResourceLocation balloonId) {
        var balloon = Balloons.REGISTERED_BALLOONS.get(balloonId);
        Model model = Models.getModel(balloon.data().model);
        var virtualBalloon = new VirtualBalloon(serverPlayer);
        virtualBalloon.setModel(model, balloon.data().showLeash);
        ACTIVE_BALLOONS.put(serverPlayer, virtualBalloon);
        virtualBalloon.play(balloon.data().animation);
        virtualBalloon.attach(balloon.data());
    }

    public static void removeBalloonIfActive(ServerPlayer serverPlayer) {
        var balloon = ACTIVE_BALLOONS.get(serverPlayer);
        if (balloon != null) {
            balloon.getHolder().destroy();
            ACTIVE_BALLOONS.remove(serverPlayer);
        }
    }

    public static void addBalloonIfActive(ServerPlayer serverPlayer) {
        if (!Balloons.ACTIVE_BALLOONS.containsKey(serverPlayer)) {
            var active = StorageUtil.getActive(serverPlayer);
            if (active != null && Balloons.REGISTERED_BALLOONS.containsKey(active)) {
                setBalloon(serverPlayer, active);
            }
        }
    }
}
