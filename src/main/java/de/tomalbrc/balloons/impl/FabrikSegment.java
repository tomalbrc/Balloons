package de.tomalbrc.balloons.impl;

import de.tomalbrc.balloons.component.BalloonProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class FabrikSegment {
    public Vector3d pointA;
    public Vector3d pointB = new Vector3d();

    final public FabrikSegment parent;
    public FabrikSegment child = null;

    public float length;

    public AnimatedBalloonHolder holder;

    final BalloonProperties.BalloonSegment segmentData;

    public FabrikSegment(double x, double y, double z, BalloonProperties.BalloonSegment segmentData) {
        this.parent = null;
        this.pointA = new Vector3d(x, y, z);
        this.length = segmentData.distance();

        this.pointB.set(this.pointA.x + length, this.pointA.y, this.pointA.z);

        this.segmentData = segmentData;
    }

    public FabrikSegment(FabrikSegment parent, BalloonProperties.BalloonSegment segmentData) {
        this.parent = parent;
        this.pointA = parent.pointB.get(new Vector3d());
        this.length = segmentData.distance();

        this.pointB.set(this.pointA.x + length, this.pointA.y, this.pointA.z);

        this.segmentData = segmentData;
    }

    public void follow() {
        if (this.child != null) {
            follow(this.child.pointB.x, this.child.pointB.y, this.child.pointB.z);
        }
    }

    public void follow(double targetX, double targetY, double targetZ) {
        Vector3d target = new Vector3d(targetX, targetY, targetZ);

        Vector3d dirToTarget = new Vector3d(target).sub(this.pointB);
        if (dirToTarget.lengthSquared() == 0f) dirToTarget.set(1f, 0f, 0f);
        dirToTarget.normalize();

        Vector3d desiredDir = new Vector3d(dirToTarget).negate();

        if (this.child != null) {
            Vector3d childDir = heading();

            double maxAngleRad = Math.toRadians(35.0);
            double dot = childDir.dot(desiredDir);
            dot = Math.max(-1f, Math.min(1f, dot));
            double angleBetween = Math.acos(dot);

            if (angleBetween > maxAngleRad) {
                Vector3d axis = new Vector3d(childDir).cross(desiredDir);
                if (axis.lengthSquared() == 0f) {
                    axis.set(Math.abs(childDir.x) < 0.9f ? 1f : 0f,
                            Math.abs(childDir.x) < 0.9f ? 0f : 1f,
                            0f);
                    axis.cross(childDir).normalize();
                } else {
                    axis.normalize();
                }

                // Rodrigues' rotation: rotate childDir toward desiredDir by maxAngleRad
                double cosT = Math.cos(maxAngleRad);
                double sinT = Math.sin(maxAngleRad);
                double kDotV = axis.dot(childDir);

                Vector3d kCrossV = new Vector3d(axis).cross(childDir);
                Vector3d rotated = new Vector3d(childDir).mul(cosT)
                        .add(kCrossV.mul(sinT))
                        .add(new Vector3d(axis).mul(kDotV * (1.0f - cosT)))
                        .normalize();

                desiredDir.set(rotated);
            }
        }

        Vector3d currentDir = heading();
        double t = 0.35f;

        double cosOmega = currentDir.dot(desiredDir);
        cosOmega = Math.max(-1f, Math.min(1f, cosOmega));
        double omega = Math.acos(cosOmega);

        Vector3d newDir;
        if (Math.abs(omega) < 1e-6) {
            newDir = new Vector3d(currentDir);
        } else {
            double sinOmega = Math.sin(omega);
            double factor0 = Math.sin((1 - t) * omega) / sinOmega;
            double factor1 = Math.sin(t * omega) / sinOmega;
            newDir = new Vector3d(currentDir).mul(factor0)
                    .add(new Vector3d(desiredDir).mul(factor1))
                    .normalize();
        }

        this.pointA.set(target);
        Vector3d offset = newDir.mul(this.length);
        this.pointB.set(this.pointA).add(offset);

        this.holder.setPosition(new Vec3(pointB.x(), pointB.y(), pointB.z()));

        Vector3d head = heading();
        float yaw = (float) Math.toDegrees(Math.atan2(head.z, head.x)) - 90 - 180;
        if (yaw < 0) yaw += 360;

        float pitch = (float) Math.toDegrees(Math.asin(-head.y));
        this.holder.setYaw(yaw);
        this.holder.setPitch(pitch);
    }

    public Vector3d heading() {
        Vector3d dir = pointB.sub(pointA, new Vector3d());
        dir.normalize();
        return dir;
    }

    public AnimatedBalloonHolder getHolder() {
        return holder;
    }
}