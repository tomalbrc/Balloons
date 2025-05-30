package de.tomalbrc.balloons.filament;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.util.ModConfig;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.filament.api.behaviour.BehaviourType;
import de.tomalbrc.filament.api.event.FilamentRegistrationEvents;
import de.tomalbrc.filament.api.registry.BehaviourRegistry;
import de.tomalbrc.filament.registry.ModelRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

public class FilamentCompat {
    public static BehaviourType<BalloonBehaviour, BalloonBehaviour.Config> BALLON_BEHAVIOUR;

    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded("filament");
    }

    public static void init() {
        BALLON_BEHAVIOUR = BehaviourRegistry.registerBehaviour(ResourceLocation.fromNamespaceAndPath("filament", "balloon"), BalloonBehaviour.class);
        FilamentRegistrationEvents.ITEM.register((itemData, simpleItem) -> {
            if (simpleItem.has(FilamentCompat.BALLON_BEHAVIOUR)) {
                var behaviourConf = simpleItem.get(BALLON_BEHAVIOUR).getConfig();
                var configBalloon = new ModConfig.ConfigBalloon(itemData.id(), simpleItem.getDefaultInstance(), behaviourConf);
                Balloons.REGISTERED_BALLOONS.put(itemData.id(), configBalloon);
            }
        });
    }

    public static Model getModel(String name) {
        return ModelRegistry.getModel(ResourceLocation.parse(name));
    }
}
