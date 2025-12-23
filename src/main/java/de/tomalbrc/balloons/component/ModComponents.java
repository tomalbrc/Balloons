package de.tomalbrc.balloons.component;

import de.tomalbrc.balloons.Balloons;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class ModComponents {
    public static final DataComponentType<BalloonToken> TOKEN = new DataComponentType.Builder<BalloonToken>().persistent(BalloonToken.CODEC).build();
    public static final DataComponentType<BalloonProperties> BALLOON = new DataComponentType.Builder<BalloonProperties>().persistent(BalloonProperties.CODEC).build();

    public static void register() {
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(Balloons.MODID, "token"), TOKEN);
        PolymerComponent.registerDataComponent(TOKEN);

        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(Balloons.MODID, "balloon"), ModComponents.BALLOON);
        PolymerComponent.registerDataComponent(ModComponents.BALLOON);

    }
}
