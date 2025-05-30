package de.tomalbrc.balloons.filament;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import org.jetbrains.annotations.NotNull;

public class BalloonBehaviour implements ItemBehaviour<BalloonBehaviour.Config> {
    private final Config config;

    public BalloonBehaviour(Config config) {
        this.config = config;
    }

    @Override
    public @NotNull BalloonBehaviour.Config getConfig() {
        return config;
    }

    public static class Config {
        public String model;
        public String animation;
    }
}
