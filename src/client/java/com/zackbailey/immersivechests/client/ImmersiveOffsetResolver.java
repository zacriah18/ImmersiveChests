package com.zackbailey.immersivechests.client;

import com.zackbailey.immersivechests.client.records.ImmersiveTargetProfile;
import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientation;

import net.minecraft.util.math.Vec3d;

public final class ImmersiveOffsetResolver {

    private ImmersiveOffsetResolver() {}

    public static Vec3d resolve(
            ImmersiveTargetProfile profile,
            ImmersiveCameraOrientation resolvedOrientation,
            float resolvedYaw
    ) {
        if (profile == null || resolvedOrientation == null) {
            return Vec3d.ZERO;
        }

        return switch (resolvedOrientation) {
            case TOP -> yawRelativeOffset(
                    profile.offsetX(),
                    profile.offsetY(),
                    profile.offsetZ(),
                    resolvedYaw
            );

            case BOTTOM -> yawRelativeOffset(
                    profile.offsetX(),
                    -profile.offsetY(),
                    profile.offsetZ(),
                    resolvedYaw
            );

            case NORTH, SOUTH, EAST, WEST -> orientationRelativeOffset(
                    profile.hozOffsetX(),
                    profile.hozOffsetY(),
                    profile.hozOffsetZ(),
                    resolvedOrientation
            );
        };
    }

    private static Vec3d yawRelativeOffset(
            double offsetX,
            double offsetY,
            double offsetZ,
            float resolvedYaw
    ) {
        Vec3d forward = forwardFromYaw(resolvedYaw);
        Vec3d right = rightFromForward(forward);

        return relativeOffset(
                offsetX,
                offsetY,
                offsetZ,
                forward,
                right
        );
    }

    private static Vec3d orientationRelativeOffset(
            double offsetX,
            double offsetY,
            double offsetZ,
            ImmersiveCameraOrientation orientation
    ) {
        Vec3d forward = forwardFromOrientation(orientation);
        Vec3d right = rightFromOrientation(orientation);

        return relativeOffset(
                offsetX,
                offsetY,
                offsetZ,
                forward,
                right
        );
    }

    private static Vec3d relativeOffset(
            double offsetX,
            double offsetY,
            double offsetZ,
            Vec3d forward,
            Vec3d right
    ) {
        return right.multiply(offsetX)
                .add(0.0, offsetY, 0.0)
                .add(forward.multiply(offsetZ));
    }

    private static Vec3d forwardFromYaw(float yaw) {
        double yawRad = Math.toRadians(yaw);

        return new Vec3d(
                Math.sin(yawRad),
                0.0,
                -Math.cos(yawRad)
        );
    }

    private static Vec3d rightFromForward(Vec3d forward) {
        return new Vec3d(
                -forward.z,
                0.0,
                forward.x
        );
    }

    private static Vec3d forwardFromOrientation(
            ImmersiveCameraOrientation orientation
    ) {
        return switch (orientation) {
            case NORTH -> new Vec3d(0.0, 0.0, -1.0);
            case SOUTH -> new Vec3d(0.0, 0.0, 1.0);
            case EAST -> new Vec3d(1.0, 0.0, 0.0);
            case WEST -> new Vec3d(-1.0, 0.0, 0.0);
            case TOP, BOTTOM -> new Vec3d(0.0, 0.0, 1.0);
        };
    }

    private static Vec3d rightFromOrientation(
            ImmersiveCameraOrientation orientation
    ) {
        Vec3d forward = forwardFromOrientation(orientation);

        return new Vec3d(
                forward.z,
                0.0,
                -forward.x
        );
    }
}