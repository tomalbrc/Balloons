package de.tomalbrc.balloons;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import de.tomalbrc.balloons.config.ConfiguredBalloon;
import de.tomalbrc.balloons.config.ModConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class BalloonFiles {
    public static void load() {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("balloons/balloons");
        dir.toFile().mkdirs();

        if (Files.exists(dir) && Files.isDirectory(dir)) {
            try (var list = Files.list(dir)) {
                list.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                    try (Reader reader = Files.newBufferedReader(path)) {
                        JsonReader jsonReader = new JsonReader(reader);
                        ConfiguredBalloon category = ModConfig.JSON.fromJson(jsonReader, ConfiguredBalloon.class);

                        if (category == null) {
                            return;
                        }

                        Identifier id = category.id();
                        if (id == null) {
                            Balloons.LOGGER.error("Warning: balloon json in {} has no id; skipping", path);
                            return;
                        }

                        Balloons.addUngrouped(id, category);
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
            Balloons.LOGGER.error("Balloons directory not found: {}", dir);
        }
    }
}
