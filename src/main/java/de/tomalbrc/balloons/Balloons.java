package de.tomalbrc.balloons;

import com.mojang.logging.LogUtils;
import de.tomalbrc.balloons.command.BalloonCommand;
import de.tomalbrc.balloons.config.ModConfig;
import de.tomalbrc.balloons.config.ModConfigBalloon;
import de.tomalbrc.balloons.filament.FilamentCompat;
import de.tomalbrc.balloons.filament.TrinketCompat;
import de.tomalbrc.balloons.filament.VanillaCompat;
import de.tomalbrc.balloons.impl.VirtualBalloon;
import de.tomalbrc.balloons.util.BalloonDatabaseStorage;
import de.tomalbrc.balloons.util.PlayerBalloonDataStorage;
import de.tomalbrc.balloons.util.StorageUtil;
import de.tomalbrc.bil.core.model.Model;
import eu.pb4.polymer.core.api.other.PolymerComponent;
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
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
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
    public static Map<ResourceLocation, ModConfigBalloon> REGISTERED_BALLOONS = new Object2ObjectArrayMap<>();
    public static Map<UUID, Map<ResourceLocation, VirtualBalloon>> SPAWNED_BALLOONS = new ConcurrentHashMap<>();

    public static BalloonDatabaseStorage DATABASE = null;
    public static PlayerBalloonDataStorage PERSISTENT_DATA = null;

    public static final Logger LOGGER = LogUtils.getLogger();

    public static DataComponentType<BalloonComponent> COMPONENT = DataComponentType.<BalloonComponent>builder().persistent(BalloonComponent.CODEC).build();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> BalloonCommand.register(dispatcher));

        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ResourceLocation.fromNamespaceAndPath("balloons", "balloon"), COMPONENT);
        PolymerComponent.registerDataComponent(COMPONENT);

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
            for (ModConfigBalloon configBalloon : ModConfig.getInstance().balloons) {
                REGISTERED_BALLOONS.put(configBalloon.id(), configBalloon);
            }

            if (ModConfig.getInstance().mongoDb != null && ModConfig.getInstance().mongoDb.enabled)
                DATABASE = new BalloonDatabaseStorage(ModConfig.getInstance().mongoDb);
            else
                PERSISTENT_DATA = minecraftServer.overworld().getDataStorage().computeIfAbsent(PlayerBalloonDataStorage.TYPE);
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
        var balloon = Balloons.REGISTERED_BALLOONS.get(balloonId);
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

        VanillaCompat.TEMP_PROVIDER.removeActive(livingEntity.getUUID());
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
            if (Balloons.REGISTERED_BALLOONS.containsKey(balloonId)) {
                spawnBalloon(livingEntity, balloonId);
            }
        }
    }

    public static void addBalloonIfActive(LivingEntity livingEntity) {
        var balloonMap = SPAWNED_BALLOONS.computeIfAbsent(livingEntity.getUUID(), (living) -> new Object2ObjectArrayMap<>());
        var active = StorageUtil.getActive(livingEntity);
        if (active != null && !balloonMap.containsKey(active) && Balloons.REGISTERED_BALLOONS.containsKey(active)) {
            spawnBalloon(livingEntity, active);
        }
    }
}
