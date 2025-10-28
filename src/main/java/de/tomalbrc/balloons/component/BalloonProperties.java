package de.tomalbrc.balloons.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record BalloonProperties(
        String title,
        String model,
        String animation,
        boolean showLeash,
        boolean tilt,
        boolean rotate,
        float followSpeed,
        float drag,
        float bobFrequency,
        float bobAmplitude,
        Vec3 offset,
        List<BalloonSegment> segments
) implements PolymerComponent {
    public static final Codec<BalloonProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("title", "Balloon").forGetter(BalloonProperties::title),
            Codec.STRING.fieldOf("model").forGetter(BalloonProperties::model),
            Codec.STRING.optionalFieldOf("animation", "idle").forGetter(BalloonProperties::animation),
            Codec.BOOL.optionalFieldOf("show_leash", true).forGetter(BalloonProperties::showLeash),
            Codec.BOOL.optionalFieldOf("tilt", true).forGetter(BalloonProperties::tilt),
            Codec.BOOL.optionalFieldOf("rotate", true).forGetter(BalloonProperties::rotate),
            Codec.FLOAT.optionalFieldOf("follow_speed", 0.25f).forGetter(BalloonProperties::followSpeed),
            Codec.FLOAT.optionalFieldOf("drag", 0.2f).forGetter(BalloonProperties::drag),
            Codec.FLOAT.optionalFieldOf("bob_frequency", 0.2f).forGetter(BalloonProperties::bobFrequency),
            Codec.FLOAT.optionalFieldOf("bob_amplitude", 0.2f).forGetter(BalloonProperties::bobAmplitude),
            Vec3.CODEC.optionalFieldOf("offset", new Vec3(0.5f, 2.0f, 0.5f)).forGetter(BalloonProperties::offset),
            BalloonSegment.CODEC.listOf().optionalFieldOf("segments", List.of()).forGetter(BalloonProperties::segments)
    ).apply(instance, BalloonProperties::new));

    public record BalloonSegment(
            String model,
            String animation,
            float distance
    ) {
        public static final Codec<BalloonSegment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("model").forGetter(BalloonSegment::model),
                Codec.STRING.optionalFieldOf("animation", "idle").forGetter(BalloonSegment::animation),
                Codec.FLOAT.optionalFieldOf("distance", 1.f).forGetter(BalloonSegment::distance)
        ).apply(instance, BalloonSegment::new));
    }
}