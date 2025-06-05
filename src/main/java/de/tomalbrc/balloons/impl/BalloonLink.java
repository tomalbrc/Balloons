package de.tomalbrc.balloons.impl;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class BalloonLink {
    private Vec3 position;
    private Vec3 velocity = Vec3.ZERO;

    private float yaw, pitch;

    private final double followSpeed;
    private final double drag;

    private final float bobFrequency;   // How fast it bobs up and down
    private final float bobAmplitude;   // How high it bobs

    private final boolean rotate;
    private final boolean tilt;

    public BalloonLink(Vec3 initialPos, double followSpeed, double drag, float bobFrequency, float bobAmplitude, boolean rotate, boolean tilt) {
        this.position = initialPos;
        this.followSpeed = followSpeed;
        this.drag = drag;
        this.bobFrequency = bobFrequency;
        this.bobAmplitude = bobAmplitude;
        this.rotate = rotate;
        this.tilt = tilt;
    }

    public Vec3 update(Vec3 playerPos, long tick) {
        Vec3 baseTarget = new Vec3(
                playerPos.x(),
                playerPos.y() + 2.0,
                playerPos.z()
        );

        Vec3 toTarget = baseTarget.subtract(position);
        Vec3 toTargetHorizontal = new Vec3(toTarget.x(), 0, toTarget.z());
        double horizontalDist = toTargetHorizontal.length();

        double minDistance = 1.5;

        if (horizontalDist < minDistance) {
            velocity = new Vec3(
                    velocity.x() * (1.0 - drag),
                    velocity.y() * (1.0 - drag),
                    velocity.z() * (1.0 - drag)
            );
        } else {
            Vec3 springForce = toTargetHorizontal.normalize().scale((horizontalDist - minDistance) * followSpeed);
            velocity = velocity.add(springForce).scale(1.0 - drag);
        }

        position = new Vec3(
                position.x() + velocity.x(),
                baseTarget.y(),
                position.z() + velocity.z()
        );

        double vx = velocity.x();
        double vz = velocity.z();

        if (this.rotate) this.yaw = (float) Math.toDegrees(Math.atan2(-vx, vz));
        if (this.tilt) this.pitch = (float) (Mth.RAD_TO_DEG * velocity.horizontalDistance()*1.5f);

        double timeSeconds = tick / 20.0;
        double bobOffset = bobAmplitude * Math.sin(timeSeconds * bobFrequency * 2 * Math.PI);
        return new Vec3(position.x(), position.y() + bobOffset, position.z());
    }

    public Vec3 getPosition() {
        return position;
    }

    public void setPosition(Vec3 pos) {
        this.position = pos;
        this.velocity = Vec3.ZERO;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}