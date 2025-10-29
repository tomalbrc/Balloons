package de.tomalbrc.balloons.impl;

import de.tomalbrc.balloons.Models;
import de.tomalbrc.balloons.component.BalloonProperties;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.world.entity.LivingEntity;

public class VirtualBalloon {
    private final LivingEntity owner;
    private BalloonLink follower;

    private AnimatedBalloonHolder animatedHolder;
    private SegmentHolder fabrik;

    public VirtualBalloon(LivingEntity owner) {
        super();
        this.owner = owner;
    }

    public ElementHolder getHolder() {
        return this.animatedHolder;
    }

    public void setup(BalloonProperties config) {
        if (config.segments().isEmpty()) {
            this.animatedHolder = new AnimatedBalloonHolder(Models.getModel(config.model()), config.showLeash(), config.glint());
            PlayerAttachment.ofTicking(this.getHolder(), this.owner);
            if (config.animation() != null) this.animatedHolder.getAnimator().playAnimation(config.animation());
        } else {
            this.fabrik = new SegmentHolder(owner, config);
            this.animatedHolder = this.fabrik.getRoot().getHolder();
        }

        this.follower = new BalloonLink(
                this.owner.position().add(config.offset()),
                config.followSpeed(),
                config.drag(),
                config.bobFrequency(),
                config.bobAmplitude(),
                config.rotate(),
                config.tilt()
        );
    }

    public void tick() {
        if (this.owner != null && !this.owner.isRemoved()) {
            var newPos = this.follower.update(this.owner.position(), this.owner.level().getGameTime());
            if (fabrik == null) {
                this.animatedHolder.setPosition(newPos);
                this.animatedHolder.setYaw(this.follower.getYaw());
                this.animatedHolder.setPitch(this.follower.getPitch());
            } else {
                fabrik.follow(newPos);
            }
        }
    }

    public void destroy() {
        if (this.fabrik != null)
            this.fabrik.destroy();
        else
            this.getHolder().destroy();
    }
}
