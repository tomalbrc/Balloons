package de.tomalbrc.balloons.filament;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.world.phys.Vec3;
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
        public boolean showLeash = true;
        public boolean tilt = true;
        public boolean rotate = true;
        public float followSpeed = 0.25f;
        public float drag = 0.2f;
        public float bobFrequency = 0.2f;
        public float bobAmplitude = 0.2f;
        public Vec3 offset = new Vec3(0.5f, 2.f, 0.5f);
    }
}
