package de.tomalbrc.balloons;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import de.tomalbrc.balloons.config.ConfiguredBalloon;
import de.tomalbrc.balloons.config.ConfiguredCategory;
import de.tomalbrc.balloons.config.ModConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class Categories {
    public static Map<String, ConfiguredCategory> CATEGORIES = new Object2ObjectArrayMap<>();

    public static void add(String id, ConfiguredCategory category) {
        CATEGORIES.put(id, category);
        for (var entry : category.balloons().entrySet()) {
            Balloons.addGrouped(entry.getKey(), entry.getValue());
        }
    }

    public static void load() {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("balloons/categories");
        dir.toFile().mkdirs();

        if (Files.exists(dir) && Files.isDirectory(dir)) {
            try (var list = Files.list(dir)) {
                list.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                    try (Reader reader = Files.newBufferedReader(path)) {
                        JsonReader jsonReader = new JsonReader(reader);
                        ConfiguredCategory category = ModConfig.JSON.fromJson(jsonReader, ConfiguredCategory.class);

                        if (category == null) {
                            return;
                        }

                        String id = category.id();
                        if (id == null || id.isBlank()) {
                            Balloons.LOGGER.error("Warning: category in {} has no id; skipping", path);
                            return;
                        }

                        add(id, category);
                    } catch (JsonSyntaxException e) {
                        Balloons.LOGGER.error("JSON syntax error in file {}: {}", path, e.getMessage());
                    } catch (IOException e) {
                        Balloons.LOGGER.error("I/O error reading file {}: {}", path, e.getMessage());
                    }
                });
            } catch (IOException e) {
                Balloons.LOGGER.error("I/O error listing directory {}: {}", dir, e.getMessage());
            }
        } else {
            Balloons.LOGGER.error("Categories directory not found: {}", dir);
        }
    }

    public static void saveExamples() {
        Map<ResourceLocation, ConfiguredBalloon> balloonMap = Map.of();

        ConfiguredCategory category = new ConfiguredCategory("example_category", "<green>Example Category</green>", Items.KNOWLEDGE_BOOK.builtInRegistryHolder().key().location(), null, null, false, balloonMap);

        Path dir = FabricLoader.getInstance().getConfigDir().resolve("balloons/categories");
        dir.toFile().mkdirs();
        var filepath = dir.resolve("example.json");
        try (FileOutputStream stream = new FileOutputStream(filepath.toFile())) {
            stream.write(ModConfig.JSON.toJson(category).getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {}
    }
}
