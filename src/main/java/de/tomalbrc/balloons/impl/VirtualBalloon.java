package de.tomalbrc.balloons.impl;

import de.tomalbrc.balloons.Models;
import de.tomalbrc.balloons.component.BalloonProperties;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class VirtualBalloon {
    private final Entity owner;
    private BalloonLink follower;

    private AnimatedBalloonHolder animatedHolder;
    private SegmentHolder fabrik;

    public VirtualBalloon(Entity owner) {
        super();
        this.owner = owner;
    }

    public ElementHolder getHolder() {
        return this.animatedHolder;
    }

    public void setup(BalloonProperties config) {
        if (config.segments().isEmpty()) {
            this.animatedHolder = new AnimatedBalloonHolder(owner, Models.getModel(config.model()), config.showLeash(), config.glint());
            PlayerAttachment.ofTicking(this.getHolder(), this.owner);
            if (config.animation() != null) this.animatedHolder.getAnimator().playAnimation(config.animation());
        } else {
            this.fabrik = new SegmentHolder(owner, config);
            this.animatedHolder = this.fabrik.getRoot().getHolder();
        }

        this.follower = new BalloonLink(
                this.owner.position().add(new Vec3(Math.random()-0.5, Math.random()-0.5, Math.random()-0.5).scale(3.5)),
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
