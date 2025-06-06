package de.tomalbrc.balloons.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.balloons.Balloons;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ItemStackDeserializer implements JsonDeserializer<ItemStack> {
    @Override
    public ItemStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        RegistryOps.RegistryInfoLookup registryInfoLookup = createContext(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        DataResult<Pair<ItemStack, JsonElement>> result = ItemStack.CODEC.decode(RegistryOps.create(JsonOps.INSTANCE, registryInfoLookup), jsonElement);

        if (result.resultOrPartial().isEmpty()) {
            Balloons.LOGGER.error("Skipping broken itemstack; could not load: {}", jsonElement.toString());
            Balloons.LOGGER.error("Minecraft error message: {}", result.error().orElseThrow().message());
            return null;
        } else if (result.error().isPresent()) {
            Balloons.LOGGER.warn("Could not load itemstack: {}", jsonElement.toString());
            Balloons.LOGGER.warn("Minecraft error message: {}", result.error().orElseThrow().message());
        }

        return result.resultOrPartial().get().getFirst();
    }

    public static RegistryOps.RegistryInfoLookup createContext(RegistryAccess registryAccess) {
        final Map<ResourceKey<? extends Registry<?>>, RegistryOps.RegistryInfo<?>> map = new HashMap<>();
        registryAccess.registries().forEach((registryEntry) -> map.put(registryEntry.key(), createInfoForContextRegistry(registryEntry.value())));
        return new RegistryOps.RegistryInfoLookup() {
            @NotNull
            @SuppressWarnings("unchecked")
            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                return Optional.ofNullable((RegistryOps.RegistryInfo<T>) map.get(resourceKey));
            }
        };
    }

    public static <T> RegistryOps.RegistryInfo<T> createInfoForContextRegistry(Registry<T> registry) {
        return new RegistryOps.RegistryInfo<>(registry, registry, registry.registryLifecycle());
    }
}
