package de.tomalbrc.balloons.impl;

import de.tomalbrc.balloons.Models;
import de.tomalbrc.balloons.component.BalloonProperties;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SegmentHolder {
    private final List<FabrikSegment> segments;

    public SegmentHolder(Entity owner, BalloonProperties config) {
        this.segments = new ObjectArrayList<>();
        FabrikSegment current = new FabrikSegment(owner.position().x, owner.position().y, owner.position().z, config.segments().getFirst());
        segments.add(current);

        for (int i = 1; i < config.segments().size(); i++) {
            var balloonSegment = config.segments().get(config.segments().size() -1 -i);
            FabrikSegment next = new FabrikSegment(current, balloonSegment);

            current.child = next;
            current = next;
            segments.add(current);
        }

        for (FabrikSegment segment : segments) {
            if (segment == segments.getLast()) {
                var animatedHolder = new AnimatedBalloonHolder(owner, Models.getModel(config.model()), config.showLeash(), config.glint());
                PlayerAttachment.ofTicking(animatedHolder, owner);
                segment.holder = animatedHolder;

                if (config.animation() != null) {
                    animatedHolder.getAnimator().playAnimation(config.animation());
                }
            } else {
                var holder = new AnimatedBalloonHolder(owner, Models.getModel(segment.segmentData.model()), false, config.glint());
                PlayerAttachment.ofTicking(holder, owner);
                segment.holder = holder;
                if (segment.segmentData.animation() != null) {
                    holder.getAnimator().playAnimation(segment.segmentData.animation());
                }
            }
        }
    }

    public void add(FabrikSegment segment) {
        this.segments.add(segment);
    }

    public void follow(Vec3 pos) {
        var root = getRoot();
        root.follow(pos.x, pos.y, pos.z);

        FabrikSegment next = root.parent;
        while (next != null) {
            next.follow();

            next = next.parent;
        }
    }

    public FabrikSegment getRoot() {
        return segments.getLast();
    }

    public void destroy() {
        for (FabrikSegment segment : this.segments) {
            segment.holder.destroy();
        }
    }
}
