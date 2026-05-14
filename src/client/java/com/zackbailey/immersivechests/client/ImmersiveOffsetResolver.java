package com.zackbailey.immersivechests.client;

import com.zackbailey.immersivechests.client.records.ImmersiveTargetProfile;
import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientation;
import com.zackbailey.immersivechests.enums.ImmersiveTargetType;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ImmersiveOffsetResolver {

    private static final double OFFSET_SCALE_MIN = 0.65;
    private static final double OFFSET_SCALE_MAX = 1.45;

    private static final double FOV_REFERENCE = 70.0;
    private static final double FOV_MIN = 30.0;
    private static final double FOV_MAX = 110.0;
    private static final double FOV_COMPENSATION_STRENGTH = 0.85;
    private static final double FOV_COMPENSATION_MIN = 0.70;
    private static final double FOV_COMPENSATION_MAX = 1.60;

    private static final double PLANE_SCALE_STRENGTH = 0.35;

    private ImmersiveOffsetResolver() {}

    public static Vec3 resolve(
            ImmersiveTargetProfile profile,
            ImmersiveCameraOrientation resolvedOrientation,
            float resolvedYaw
    ) {
        if (profile == null || resolvedOrientation == null) {
            return Vec3.ZERO;
        }

        if (profile.type() == ImmersiveTargetType.CHEST_BOAT) {
            return resolveEntityBackOffset(profile, resolvedYaw);
        }

        return switch (resolvedOrientation) {
            case TOP -> yawRelativeOffset(
                    scaleTopBottomOffset(
                            profile.offsetX(),
                            profile.offsetY(),
                            profile.offsetZ()
                    ),
                    resolvedYaw
            );

            case BOTTOM -> yawRelativeOffset(
                    scaleTopBottomOffset(
                            profile.offsetX(),
                            -profile.offsetY(),
                            profile.offsetZ()
                    ),
                    resolvedYaw
            );

            case NORTH, SOUTH, EAST, WEST -> orientationRelativeOffset(
                    scaleSideOffset(
                            profile.hozOffsetX(),
                            profile.hozOffsetY(),
                            profile.hozOffsetZ()
                    ),
                    resolvedOrientation
            );
        };
    }

    private static Vec3 resolveEntityBackOffset(
            ImmersiveTargetProfile profile,
            float resolvedYaw
    ) {
        return yawRelativeOffset(
                scaleTopBottomOffset(
                        profile.offsetX(),
                        profile.offsetY(),
                        profile.offsetZ()
                ),
                resolvedYaw
        );
    }

    private static Vec3 scaleTopBottomOffset(
            double offsetX,
            double offsetY,
            double offsetZ
    ) {
        double depthScale = depthScale();
        double planeScale = planeScale(depthScale);

        return new Vec3(
                offsetX * planeScale,
                offsetY * depthScale,
                offsetZ * planeScale
        );
    }

    private static Vec3 scaleSideOffset(
            double offsetX,
            double offsetY,
            double offsetZ
    ) {
        double depthScale = depthScale();
        double planeScale = planeScale(depthScale);

        return new Vec3(
                offsetX * planeScale,
                offsetY * planeScale,
                offsetZ * depthScale
        );
    }

    private static Vec3 yawRelativeOffset(
            Vec3 localOffset,
            float resolvedYaw
    ) {
        Vec3 forward = forwardFromYaw(resolvedYaw);
        Vec3 right = rightFromForward(forward);

        return relativeOffset(localOffset, forward, right);
    }

    private static Vec3 orientationRelativeOffset(
            Vec3 localOffset,
            ImmersiveCameraOrientation orientation
    ) {
        Vec3 forward = forwardFromOrientation(orientation);
        Vec3 right = rightFromOrientation(orientation);

        return relativeOffset(localOffset, forward, right);
    }

    private static Vec3 relativeOffset(
            Vec3 localOffset,
            Vec3 forward,
            Vec3 right
    ) {
        return right.scale(localOffset.x)
                .add(0.0, localOffset.y, 0.0)
                .add(forward.scale(localOffset.z));
    }

    private static Vec3 forwardFromYaw(float yaw) {
        double radians = Math.toRadians(yaw);

        return new Vec3(
                -Math.sin(radians),
                0.0,
                Math.cos(radians)
        ).normalize();
    }

    private static Vec3 rightFromForward(Vec3 forward) {
        return new Vec3(
                forward.z,
                0.0,
                -forward.x
        ).normalize();
    }

    private static Vec3 forwardFromOrientation(ImmersiveCameraOrientation orientation) {
        return switch (orientation) {
            case NORTH -> new Vec3(0.0, 0.0, -1.0);
            case SOUTH -> new Vec3(0.0, 0.0, 1.0);
            case EAST -> new Vec3(1.0, 0.0, 0.0);
            case WEST -> new Vec3(-1.0, 0.0, 0.0);
            case TOP -> new Vec3(0.0, -1.0, 0.0);
            case BOTTOM -> new Vec3(0.0, 1.0, 0.0);
        };
    }

    private static Vec3 rightFromOrientation(ImmersiveCameraOrientation orientation) {
        return switch (orientation) {
            case NORTH -> new Vec3(1.0, 0.0, 0.0);
            case SOUTH -> new Vec3(-1.0, 0.0, 0.0);
            case EAST -> new Vec3(0.0, 0.0, 1.0);
            case WEST -> new Vec3(0.0, 0.0, -1.0);
            case TOP, BOTTOM -> new Vec3(1.0, 0.0, 0.0);
        };
    }

    private static double depthScale() {
        return userOffsetScale() * fovCompensationScale();
    }

    private static double planeScale(double depthScale) {
        return 1.0 + ((depthScale - 1.0) * PLANE_SCALE_STRENGTH);
    }

    private static double userOffsetScale() {
        return scaledConfig(
                ImmersiveChestsConfigScreen.offsetScale,
                OFFSET_SCALE_MIN,
                OFFSET_SCALE_MAX
        );
    }

    private static double fovCompensationScale() {
        Minecraft client = Minecraft.getInstance();

        if (client == null || client.options == null) {
            return 1.0;
        }

        double fov = Mth.clamp(
                client.options.fov().get(),
                FOV_MIN,
                FOV_MAX
        );

        double fovCompensation = Math.pow(
                FOV_REFERENCE / fov,
                FOV_COMPENSATION_STRENGTH
        );

        return Mth.clamp(
                fovCompensation,
                FOV_COMPENSATION_MIN,
                FOV_COMPENSATION_MAX
        );
    }

    private static double scaledConfig(double value, double min, double max) {
        double t = Mth.clamp((value - 1.0) / 9.0, 0.0, 1.0);
        return min + (max - min) * t;
    }
}