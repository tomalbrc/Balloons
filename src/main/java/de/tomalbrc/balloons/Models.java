package de.tomalbrc.balloons;

import com.google.common.collect.ImmutableList;
import de.tomalbrc.balloons.filament.FilamentCompat;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.file.loader.AjBlueprintLoader;
import de.tomalbrc.bil.file.loader.AjModelLoader;
import de.tomalbrc.bil.file.loader.BbModelLoader;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Models {
    private static final Map<String, Model> MODELS = new Object2ObjectArrayMap<>();
    private static final List<String> SUPPORTED_EXTENSIONS = ImmutableList.of("bbmodel", "ajmodel", "ajblueprint");

    public static Model getModel(String name) {
        Model model = null;
        if (FilamentCompat.isLoaded())
            model = FilamentCompat.getModel(name);

        if (model == null) {
            model = MODELS.get(name);
        }

        return model;
    }

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("balloons/models");

        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            return;
        }

        try (Stream<Path> files = Files.list(path)) {
            files.filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return SUPPORTED_EXTENSIONS.stream().anyMatch(name::endsWith);
                    })
                    .forEach(Models::processModelFile);

        } catch (IOException e) {
            Balloons.LOGGER.error("Error reading model files from config: {}", e.getMessage());
        }
    }

    private static void processModelFile(Path filePath) {
        Model model = null;
        String name = filePath.toString();
        if (FilenameUtils.isExtension(name, "bbmodel")) {
            model = BbModelLoader.load(name);
        }
        else if (FilenameUtils.isExtension(name, "ajmodel")) {
            model = AjModelLoader.load(name);
        }
        else if (FilenameUtils.isExtension(name, "ajblueprint")) {
            model = AjBlueprintLoader.load(name);
        }

        MODELS.put(FilenameUtils.getBaseName(name), model);
    }
}
