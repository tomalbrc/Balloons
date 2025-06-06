package de.tomalbrc.balloons;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public record BalloonComponent(
        String model,
        String animation,
        boolean showLeash,
        boolean tilt,
        boolean rotate,
        float followSpeed,
        float drag,
        float bobFrequency,
        float bobAmplitude,
        Vec3 offset
) implements PolymerComponent {
    public static final Codec<BalloonComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("model").forGetter(BalloonComponent::model),
            Codec.STRING.fieldOf("animation").forGetter(BalloonComponent::animation),
            Codec.BOOL.optionalFieldOf("show_leash", true).forGetter(BalloonComponent::showLeash),
            Codec.BOOL.optionalFieldOf("tilt", true).forGetter(BalloonComponent::tilt),
            Codec.BOOL.optionalFieldOf("rotate", true).forGetter(BalloonComponent::rotate),
            Codec.FLOAT.optionalFieldOf("follow_speed", 0.25f).forGetter(BalloonComponent::followSpeed),
            Codec.FLOAT.optionalFieldOf("drag", 0.2f).forGetter(BalloonComponent::drag),
            Codec.FLOAT.optionalFieldOf("bob_frequency", 0.2f).forGetter(BalloonComponent::bobFrequency),
            Codec.FLOAT.optionalFieldOf("bob_amplitude", 0.2f).forGetter(BalloonComponent::bobAmplitude),
            Vec3.CODEC.optionalFieldOf("offset", new Vec3(0.5f, 2.0f, 0.5f)).forGetter(BalloonComponent::offset)
    ).apply(instance, BalloonComponent::new));

    public ResourceLocation modelId() {
        return ResourceLocation.tryParse(model);
    }
}