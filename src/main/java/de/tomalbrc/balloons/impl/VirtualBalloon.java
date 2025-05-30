package de.tomalbrc.balloons.impl;

import de.tomalbrc.bil.core.model.Model;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.server.level.ServerPlayer;

public class VirtualBalloon {
    private AnimatedBalloonHolder animatedHolder;

    private final ServerPlayer owner;

    private BalloonLink follower;

    public VirtualBalloon(ServerPlayer owner) {
        super();
        this.owner = owner;
    }

    public ElementHolder getHolder() {
        return this.animatedHolder;
    }

    public void setModel(Model model) {
        this.animatedHolder = new AnimatedBalloonHolder(model);
    }

    public int getLeashedEntityId() {
        return this.animatedHolder.leashedEntityId();
    }

    public void play(String animation) {
        if (this.animatedHolder != null)
            this.animatedHolder.getAnimator().playAnimation(animation);
    }

    public void attach() {
        if (getHolder().getAttachment() == null) {
            EntityAttachment.ofTicking(this.getHolder(), this.owner);
            this.follower = new BalloonLink(
                    this.owner.position().add(0.5f, 2.f, 0.5f),
                    0.25,
                    0.2,
                    0.2f,
                    0.2f
            );
            this.getHolder().startWatching(this.owner);
        }
    }

    public void tick() {
        if (this.owner != null && !this.owner.hasDisconnected()) {
            var newPos = this.follower.update(this.owner.position(), this.owner.level().getGameTime());
            this.animatedHolder.setPosition(newPos);
            this.animatedHolder.setYaw(this.follower.getYaw());
            this.animatedHolder.setPitch(this.follower.getPitch());
        }
    }
}
