package de.tomalbrc.balloons.filament;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.config.ModConfigBalloon;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.filament.api.event.FilamentRegistrationEvents;
import de.tomalbrc.filament.registry.ModelRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class FilamentCompat {
    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded("filament");
    }

    public static void init() {
        FilamentRegistrationEvents.ITEM.register((itemData, simpleItem) -> {
            if (simpleItem.components().has(Balloons.COMPONENT)) {
                var behaviourConf = simpleItem.components().get(Balloons.COMPONENT);
                var configBalloon = new ModConfigBalloon(itemData.id(), null, behaviourConf);
                Balloons.REGISTERED_BALLOONS.put(itemData.id(), configBalloon);
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
            for (Map.Entry<ResourceLocation, ModConfigBalloon> entry : Balloons.REGISTERED_BALLOONS.entrySet()) {
                if (entry.getValue().item() == null)
                    entry.getValue().setItem(BuiltInRegistries.ITEM.getValue(entry.getKey()).getDefaultInstance());
            }
        });
    }

    public static Model getModel(String name) {
        return ModelRegistry.getModel(ResourceLocation.parse(name));
    }
}
