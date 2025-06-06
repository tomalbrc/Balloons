package de.tomalbrc.balloons.config;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import de.tomalbrc.balloons.BalloonComponent;
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
    static Gson JSON = de.tomalbrc.bil.json.JSON.GENERIC_BUILDER
            .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackDeserializer())
            .registerTypeHierarchyAdapter(BalloonComponent.class, new SimpleCodecDeserializer<>(BalloonComponent.CODEC))
            .create();

    @SerializedName("mongo_db")
    public MongoConfig mongoDb = new MongoConfig();
    public List<ModConfigBalloon> balloons = List.of();

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
}
