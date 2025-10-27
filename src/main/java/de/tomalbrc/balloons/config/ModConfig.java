package de.tomalbrc.balloons.config;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.Categories;
import de.tomalbrc.balloons.component.BalloonProperties;
import de.tomalbrc.balloons.storage.DatabaseConfig;
import de.tomalbrc.balloons.util.StorageUtil;
import de.tomalbrc.bil.json.SimpleCodecDeserializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ModConfig {
    static Path CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("balloons.json");
    static ModConfig instance;
    public static Gson JSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackDeserializer())
            .registerTypeHierarchyAdapter(ResourceLocation.class, new SimpleCodecDeserializer<>(ResourceLocation.CODEC))
            .registerTypeHierarchyAdapter(BalloonProperties.class, new SimpleCodecDeserializer<>(BalloonProperties.CODEC))
            .registerTypeHierarchyAdapter(Vec3.class, new SimpleCodecDeserializer<>(Vec3.CODEC))
            .registerTypeHierarchyAdapter(Vector2i.class, new Vector2iDeserializer())
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
            .disableHtmlEscaping()
            .create();

    public Map<String, Integer> permissions = Map.of(
            "balloons.command", 2,
            "balloons.give", 2,
            "balloons.remove", 2,
            "balloons.list", 2,
            "balloons.reload", 2,
            "balloons.direct", 2
    );

    @SerializedName("messages")
    public Messages messages = new Messages();

    public GuiConfig gui = new GuiConfig();

    public StorageUtil.Type storageType = StorageUtil.Type.SQLITE;
    public DatabaseConfig database = new DatabaseConfig.Builder()
            .host("localhost")
            .port(3306)
            .user("username")
            .password("secret")
            .maxPoolSize(10)
            .sslEnabled(false)
            .database("balloons")
            .filepath("cosmetic.sqlite")
            .build();

    public List<ConfiguredBalloon> balloons = List.of();

    public static ModConfig getInstance() {
        if (instance == null) {
            if (!load())
                save();
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
                    stream.close();

                    Categories.saveExamples();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }

        try {
            ModConfig.instance = JSON.fromJson(new FileReader(ModConfig.CONFIG_FILE_PATH.toFile()), ModConfig.class);

            if (instance != null) {
                if (Balloons.STORAGE != null) {
                    Balloons.STORAGE.close();
                    Balloons.STORAGE = null;
                }
                Balloons.STORAGE = Balloons.getStorage();
            }
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

    public static class Vector2iDeserializer implements JsonDeserializer<Vector2i>, JsonSerializer<Vector2i> {
        @Override
        public Vector2i deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            int x;
            int y;

            if (jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                x = jsonArray.get(0).getAsInt();
                y = jsonArray.get(1).getAsInt();
            } else {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                x = jsonObject.has("x") ? jsonObject.get("x").getAsInt() :
                        jsonObject.has("width") ? jsonObject.get("width").getAsInt() : 0;
                y = jsonObject.has("y") ? jsonObject.get("y").getAsInt() :
                        jsonObject.has("height") ? jsonObject.get("height").getAsInt() : 0;
            }

            return new Vector2i(x, y);
        }

        @Override
        public JsonElement serialize(Vector2i src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            array.add(src.x);
            array.add(src.y);
            return array;
        }
    }
}
