package de.tomalbrc.balloons.config;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.balloons.Balloons;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import java.lang.reflect.Type;

public class ItemStackTemplateDeserializer implements JsonDeserializer<ItemStackTemplate>, JsonSerializer<ItemStackTemplate> {

    @Override
    public ItemStackTemplate deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        DataResult<Pair<ItemStackTemplate, JsonElement>> result =
                ItemStackTemplate.CODEC.decode(createContext(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)), jsonElement);

        if (result.resultOrPartial().isEmpty()) {
            Balloons.LOGGER.error("Skipping broken ItemStackTemplate; could not load: {}", jsonElement.toString());
            Balloons.LOGGER.error("Minecraft error message: {}", result.error().orElseThrow().message());
            return null;
        } else if (result.error().isPresent()) {
            Balloons.LOGGER.warn("Could not fully load ItemStack: {}", jsonElement.toString());
            Balloons.LOGGER.warn("Minecraft warning: {}", result.error().orElseThrow().message());
        }

        return result.resultOrPartial().orElseThrow().getFirst();
    }

    @Override
    public JsonElement serialize(ItemStackTemplate src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return JsonOps.INSTANCE.empty();
        }

        RegistryOps<JsonElement> ops = createContext(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        DataResult<JsonElement> result = ItemStackTemplate.CODEC.encodeStart(ops, src);

        if (result.error().isPresent()) {
            Balloons.LOGGER.error("Error serializing ItemStack: {}", result.error().orElseThrow().message());
        }

        return result.result().orElse(JsonOps.INSTANCE.empty());
    }

    public static RegistryOps<JsonElement> createContext(RegistryAccess registryAccess) {
        return registryAccess.createSerializationContext(JsonOps.INSTANCE);
    }
}