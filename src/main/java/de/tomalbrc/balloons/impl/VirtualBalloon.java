package de.tomalbrc.balloons.impl;

import de.tomalbrc.balloons.component.BalloonProperties;
import de.tomalbrc.bil.core.model.Model;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class VirtualBalloon {
    private AnimatedBalloonHolder animatedHolder;

    private final LivingEntity owner;

    private BalloonLink follower;

    public VirtualBalloon(LivingEntity owner) {
        super();
        this.owner = owner;
    }

    public ElementHolder getHolder() {
        return this.animatedHolder;
    }

    public void setModel(Model model, boolean leashed) {
        this.animatedHolder = new AnimatedBalloonHolder(model, leashed);
    }

    public int getLeashedEntityId() {
        return this.animatedHolder.leashedEntityId();
    }

    public void play(String animation) {
        if (this.animatedHolder != null)
            this.animatedHolder.getAnimator().playAnimation(animation);
    }

    public void attach(BalloonProperties config) {
        if (getHolder().getAttachment() == null) {
            EntityAttachment.ofTicking(this.getHolder(), this.owner);
            this.follower = new BalloonLink(
                    this.owner.position().add(config.offset()),
                    config.followSpeed(),
                    config.drag(),
                    config.bobFrequency(),
                    config.bobAmplitude(),
                    config.rotate(),
                    config.tilt()
            );
            if (this.owner instanceof ServerPlayer serverPlayer) this.getHolder().startWatching(serverPlayer);
        }
    }

    public void tick() {
        if (this.owner != null && !this.owner.isRemoved()) {
            var newPos = this.follower.update(this.owner.position(), this.owner.level().getGameTime());
            this.animatedHolder.setPosition(newPos);
            this.animatedHolder.setYaw(this.follower.getYaw());
            this.animatedHolder.setPitch(this.follower.getPitch());
        }
    }
}
