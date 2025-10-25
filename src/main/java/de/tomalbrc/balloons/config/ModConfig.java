package de.tomalbrc.balloons.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.Categories;
import de.tomalbrc.balloons.component.BalloonProperties;
import de.tomalbrc.balloons.storage.DatabaseConfig;
import de.tomalbrc.balloons.util.StorageUtil;
import de.tomalbrc.bil.json.SimpleCodecDeserializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.ItemStack;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public class ModConfig {
    static Path CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("balloons.json");
    static ModConfig instance;
    public static Gson JSON = de.tomalbrc.bil.json.JSON.GENERIC_BUILDER.create().newBuilder()
            .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackDeserializer())
            .registerTypeHierarchyAdapter(BalloonProperties.class, new SimpleCodecDeserializer<>(BalloonProperties.CODEC))
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
            .create();

    @SerializedName("messages")
    public Messages messages = new Messages();

    public GuiConfig gui = new GuiConfig();

    public StorageUtil.Type storageType = StorageUtil.Type.MARIADB;
    public DatabaseConfig database = new DatabaseConfig.Builder()
            .host("localhost")
            .port(3306)
            .user("username")
            .password("secret")
            .maxPoolSize(10)
            .sslEnabled(false)
            .database("emotes_db")
            .build();
    public List<ConfiguredBalloon> balloons = List.of();

    public static ModConfig getInstance() {
        if (instance == null) {
            load();
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
                Balloons.STORAGE.close();
                Balloons.STORAGE = null;
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
}
