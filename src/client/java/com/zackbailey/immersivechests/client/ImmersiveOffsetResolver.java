package com.zackbailey.immersivechests.client;

import com.zackbailey.immersivechests.client.records.ImmersiveTargetProfile;
import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientation;
import net.minecraft.world.phys.Vec3;

public final class ImmersiveOffsetResolver {

    private ImmersiveOffsetResolver() {}

    public static Vec3 resolve(
            ImmersiveTargetProfile profile,
            ImmersiveCameraOrientation resolvedOrientation,
            float resolvedYaw
    ) {
        if (profile == null || resolvedOrientation == null) {
            return Vec3.ZERO;
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

    private static Vec3 yawRelativeOffset(
            double offsetX,
            double offsetY,
            double offsetZ,
            float resolvedYaw
    ) {
        Vec3 forward = forwardFromYaw(resolvedYaw);
        Vec3 right = rightFromForward(forward);

        return relativeOffset(
                offsetX,
                offsetY,
                offsetZ,
                forward,
                right
        );
    }

    private static Vec3 orientationRelativeOffset(
            double offsetX,
            double offsetY,
            double offsetZ,
            ImmersiveCameraOrientation orientation
    ) {
        Vec3 forward = forwardFromOrientation(orientation);
        Vec3 right = rightFromOrientation(orientation);

        return relativeOffset(
                offsetX,
                offsetY,
                offsetZ,
                forward,
                right
        );
    }

    private static Vec3 relativeOffset(
            double offsetX,
            double offsetY,
            double offsetZ,
            Vec3 forward,
            Vec3 right
    ) {
        return right.scale(offsetX)
                .add(0.0, offsetY, 0.0)
                .add(forward.scale(offsetZ));
    }

    private static Vec3 forwardFromYaw(float yaw) {
        double yawRad = Math.toRadians(yaw);

        return new Vec3(
                Math.sin(yawRad),
                0.0,
                -Math.cos(yawRad)
        );
    }

    private static Vec3 rightFromForward(Vec3 forward) {
        return new Vec3(
                -forward.z,
                0.0,
                forward.x
        );
    }

    private static Vec3 forwardFromOrientation(
            ImmersiveCameraOrientation orientation
    ) {
        return switch (orientation) {
            case NORTH -> new Vec3(0.0, 0.0, -1.0);
            case SOUTH -> new Vec3(0.0, 0.0, 1.0);
            case EAST -> new Vec3(1.0, 0.0, 0.0);
            case WEST -> new Vec3(-1.0, 0.0, 0.0);
            case TOP, BOTTOM -> new Vec3(0.0, 0.0, 1.0);
        };
    }

    private static Vec3 rightFromOrientation(
            ImmersiveCameraOrientation orientation
    ) {
        Vec3 forward = forwardFromOrientation(orientation);

        return new Vec3(
                forward.z,
                0.0,
                -forward.x
        );
    }
}