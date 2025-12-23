package de.tomalbrc.balloons.filament;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.component.BalloonProperties;
import de.tomalbrc.balloons.component.ModComponents;
import de.tomalbrc.balloons.config.ConfiguredBalloon;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.filament.api.event.FilamentRegistrationEvents;
import de.tomalbrc.filament.registry.ModelRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.Map;

public class FilamentCompat {
    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded("filament");
    }

    public static void init() {
        FilamentRegistrationEvents.ITEM.register((itemData, item) -> registerBalloon(itemData.id(), item));
        FilamentRegistrationEvents.BLOCK.register((itemData, item, block) -> registerBalloon(itemData.id(), item));
        FilamentRegistrationEvents.DECORATION.register((itemData, item, block) -> registerBalloon(itemData.id(), item));

        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
            for (Map.Entry<Identifier, ConfiguredBalloon> entry : Balloons.all().entrySet()) {
                if (entry.getValue().itemStack() == null)
                    entry.getValue().setItem(BuiltInRegistries.ITEM.getValue(entry.getKey()).getDefaultInstance());
            }
        });
    }

    private static void registerBalloon(Identifier id, Item item) {
        if (item.components().has(ModComponents.BALLOON)) {
            BalloonProperties properties = item.components().get(ModComponents.BALLOON);
            assert properties != null;
            ConfiguredBalloon configBalloon = new ConfiguredBalloon(id, properties.title(), null, properties);
            Balloons.UNGROUPED.put(id, configBalloon);
        }
    }

    public static Model getModel(String name) {
        return ModelRegistry.getModel(Identifier.parse(name));
    }
}
