package de.tomalbrc.balloons.util;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import de.tomalbrc.balloons.filament.BalloonBehaviour;
import de.tomalbrc.filament.Filament;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ModConfig {
    static Path CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("balloons.json");
    static ModConfig instance;
    static Gson JSON = de.tomalbrc.bil.json.JSON.GENERIC_BUILDER
            .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackDeserializer())
            .create();

    public static class ConfigBalloon {
        ResourceLocation id;
        ItemStack item;
        BalloonBehaviour.Config data;

        public ConfigBalloon(ResourceLocation id, @Nullable ItemStack item, BalloonBehaviour.Config data) {
            this.id = id;
            this.item = item;
            this.data = data;
        }

        public void setItem(ItemStack item) {
            this.item = item;
        }

        public BalloonBehaviour.Config data() {
            return data;
        }

        public ItemStack item() {
            return item;
        }

        public ResourceLocation id() {
            return id;
        }
    }

    @SerializedName("mongo_db")
    public MongoConfig mongoDb = new MongoConfig();
    public List<ConfigBalloon> balloons = List.of();

    public static ModConfig getInstance() {
        if (instance == null) {
            if (!load()) // only save if file wasn't just created
                save(); // save since newer versions may contain new options, also removes old options
        }
        return instance;
    }

    public static boolean load() {
        if (!CONFIG_FILE_PATH.toFile().exists()) {
            instance = new ModConfig();
            try {
                if (CONFIG_FILE_PATH.toFile().createNewFile()) {
                    FileOutputStream stream = new FileOutputStream(CONFIG_FILE_PATH.toFile());
                    stream.write(JSON.toJson(instance).getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }

        try {
            ModConfig.instance = JSON.fromJson(new FileReader(ModConfig.CONFIG_FILE_PATH.toFile()), ModConfig.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    private static void save() {
        try (FileOutputStream stream = new FileOutputStream(CONFIG_FILE_PATH.toFile())) {
            stream.write(JSON.toJson(instance).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class ItemStackDeserializer implements JsonDeserializer<ItemStack> {
        @Override
        public ItemStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            RegistryOps.RegistryInfoLookup registryInfoLookup = createContext(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
            DataResult<Pair<ItemStack, JsonElement>> result = ItemStack.CODEC.decode(RegistryOps.create(JsonOps.INSTANCE, registryInfoLookup), jsonElement);

            if (result.resultOrPartial().isEmpty()) {
                Filament.LOGGER.error("Skipping broken itemstack; could not load: {}", jsonElement.toString());
                Filament.LOGGER.error("Minecraft error message: {}", result.error().orElseThrow().message());
                return null;
            } else if (result.error().isPresent()) {
                Filament.LOGGER.warn("Could not load itemstack: {}", jsonElement.toString());
                Filament.LOGGER.warn("Minecraft error message: {}", result.error().orElseThrow().message());
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
}
