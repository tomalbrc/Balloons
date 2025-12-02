package de.tomalbrc.balloons;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static final EntityType<BalloonFenceLeashKnot> LEASH_KNOT = register(BalloonFenceLeashKnot::new, ResourceLocation.fromNamespaceAndPath("balloons", "knot"));

    public static void init() {}

    public static <T extends Entity> EntityType<T> register(EntityType.EntityFactory<T> f, ResourceLocation id) {
        var type = EntityType.Builder.of(f, MobCategory.MISC).build(ResourceKey.create(Registries.ENTITY_TYPE, id));
        Registry.register(BuiltInRegistries.ENTITY_TYPE, ResourceKey.create(Registries.ENTITY_TYPE, id), type);
        PolymerEntityUtils.registerType(type);
        return type;
    }
}
