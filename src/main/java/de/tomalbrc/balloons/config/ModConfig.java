package de.tomalbrc.balloons.config;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.Categories;
import de.tomalbrc.balloons.component.BalloonProperties;
import de.tomalbrc.balloons.configui.api.GuiData;
import de.tomalbrc.balloons.configui.api.GuiElementData;
import de.tomalbrc.balloons.storage.DatabaseConfig;
import de.tomalbrc.balloons.util.SimpleCodecDeserializer;
import de.tomalbrc.balloons.util.StorageUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    static Path CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("balloons/balloons.json");
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

    public GuiData<GuiElementData> selectionGui = new GuiData<>("Balloon Selection", List.of(
            "B        ",
            " EEEEEEE ",
            "PEEEEEEEN",
            " EEEEEEE ",
            "         ",
            " FFFFFFF "
    ), Map.of(
            ' ', new GuiElementData("empty", null, Items.AIR.getDefaultInstance(), List.of(), List.of(), false),
            'N', new GuiElementData("next_page", "Next Page", Items.ARROW.getDefaultInstance(), List.of(), List.of(), false),
            'P', new GuiElementData("prev_page", "Previous Page", Items.ARROW.getDefaultInstance(), List.of(), List.of(), false),
            'B', new GuiElementData("browse", "Browse all Balloons", Items.CHEST.getDefaultInstance(), List.of(), List.of(), false),
            'E', new GuiElementData("contents", null, Items.EMERALD.getDefaultInstance(),
                    List.of("",
                            "<green>🎈</green> Press <keybind:key.attack> to equip",
                            "",
                            "<color:#800080>↔</color> Press <keybind:key.use> to get as item",
                            "",
                            "<color:#800080>★</color> Press <keybind:key.sneak> + <keybind:key.attack> to add to favourites"),
                    List.of("",
                            "<green>🎈</green> Press <keybind:key.attack> to equip",
                            "",
                            "<color:#800080>↔</color> Press <keybind:key.use> to unequip",
                            "",
                            "<color:#800080>★</color> Press <keybind:key.sneak> + <keybind:key.attack> to add to favourites"), false),
            'F', new GuiElementData("favourites", null, Items.DIAMOND.getDefaultInstance(),
                    List.of("",
                            "<green>🎈</green> Press <keybind:key.attack> to equip",
                            "",
                            "<color:#800080>↔</color> Press <keybind:key.use> to unequip",
                            "",
                            "<color:#800080>☆</color> Press <keybind:key.sneak> + <keybind:key.attack> to remove from favourites"),
                    List.of("",
                            "<green>🎈</green> Press <keybind:key.attack> to equip",
                            "",
                            "<color:#800080>↔</color> Press <keybind:key.use> to equip",
                            "",
                            "<color:#800080>☆</color> Press <keybind:key.sneak> + <keybind:key.attack> to remove from favourites"), false)
    ), false);

    public GuiData<GuiElementData> browseGui = new GuiData<>("Browse Balloons", List.of(
            "B        ",
            " EEEEEEE ",
            "PEEEEEEEN",
            " EEEEEEE ",
            " EEEEEEE ",
            "         "
    ), Map.of(
            ' ', new GuiElementData("empty", null, Items.AIR.getDefaultInstance(), List.of(), List.of(), false),
            'N', new GuiElementData("next_page", "Next Page", Items.ARROW.getDefaultInstance(), List.of(), List.of(), false),
            'P', new GuiElementData("prev_page", "Previous Page", Items.ARROW.getDefaultInstance(), List.of(), List.of(), false),
            'B', new GuiElementData("back", "Back", Items.ARROW.getDefaultInstance(), List.of(), List.of(), false),
            'E', new GuiElementData("contents", null, Items.EMERALD.getDefaultInstance(),
                    List.of("", "<gold>You do not own this balloon!"),
                    List.of("", "<green>You own this balloon!"), false)
    ), false);

    public GuiData<GuiElementData> confirmationGui = new GuiData<>("Confirm", List.of(
            " C     A "
    ), Map.of(
            ' ', new GuiElementData("empty", null, Items.AIR.getDefaultInstance(), List.of(), List.of(), false),
            'A', new GuiElementData("confirm", "Confirm", Items.LIME_CONCRETE.getDefaultInstance(), List.of(), List.of(), false),
            'C', new GuiElementData("cancel", "Cancel", Items.RED_CONCRETE.getDefaultInstance(), List.of(), List.of(), false)
    ), false);

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
        } else try {
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
