package com.zackbailey.immersivechests.client;

import com.zackbailey.immersivechests.client.records.ImmersiveTargetProfile;
import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientation;
import com.zackbailey.immersivechests.enums.ImmersiveTargetType;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class ImmersiveOffsetResolver {

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

    public static Vec3d resolve(
            ImmersiveTargetProfile profile,
            ImmersiveCameraOrientation resolvedOrientation,
            float resolvedYaw
    ) {
        if (profile == null || resolvedOrientation == null) {
            return Vec3d.ZERO;
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

    private static Vec3d resolveEntityBackOffset(
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

    private static Vec3d scaleTopBottomOffset(
            double offsetX,
            double offsetY,
            double offsetZ
    ) {
        double depthScale = depthScale();
        double planeScale = planeScale(depthScale);

        return new Vec3d(
                offsetX * planeScale,
                offsetY * depthScale,
                offsetZ * planeScale
        );
    }

    private static Vec3d scaleSideOffset(
            double offsetX,
            double offsetY,
            double offsetZ
    ) {
        double depthScale = depthScale();
        double planeScale = planeScale(depthScale);

        return new Vec3d(
                offsetX * planeScale,
                offsetY * planeScale,
                offsetZ * depthScale
        );
    }

    private static Vec3d yawRelativeOffset(
            Vec3d localOffset,
            float resolvedYaw
    ) {
        Vec3d forward = forwardFromYaw(resolvedYaw);
        Vec3d right = rightFromForward(forward);

        return relativeOffset(localOffset, forward, right);
    }

    private static Vec3d orientationRelativeOffset(
            Vec3d localOffset,
            ImmersiveCameraOrientation orientation
    ) {
        Vec3d forward = forwardFromOrientation(orientation);
        Vec3d right = rightFromOrientation(orientation);

        return relativeOffset(localOffset, forward, right);
    }

    private static Vec3d relativeOffset(
            Vec3d localOffset,
            Vec3d forward,
            Vec3d right
    ) {
        return right.multiply(localOffset.x)
                .add(0.0, localOffset.y, 0.0)
                .add(forward.multiply(localOffset.z));
    }

    private static Vec3d forwardFromYaw(float yaw) {
        double radians = Math.toRadians(yaw);

        return new Vec3d(
                -Math.sin(radians),
                0.0,
                Math.cos(radians)
        ).normalize();
    }

    private static Vec3d rightFromForward(Vec3d forward) {
        return new Vec3d(
                forward.z,
                0.0,
                -forward.x
        ).normalize();
    }

    private static Vec3d forwardFromOrientation(ImmersiveCameraOrientation orientation) {
        return switch (orientation) {
            case NORTH -> new Vec3d(0.0, 0.0, -1.0);
            case SOUTH -> new Vec3d(0.0, 0.0, 1.0);
            case EAST -> new Vec3d(1.0, 0.0, 0.0);
            case WEST -> new Vec3d(-1.0, 0.0, 0.0);
            case TOP -> new Vec3d(0.0, -1.0, 0.0);
            case BOTTOM -> new Vec3d(0.0, 1.0, 0.0);
        };
    }

    private static Vec3d rightFromOrientation(ImmersiveCameraOrientation orientation) {
        return switch (orientation) {
            case NORTH -> new Vec3d(1.0, 0.0, 0.0);
            case SOUTH -> new Vec3d(-1.0, 0.0, 0.0);
            case EAST -> new Vec3d(0.0, 0.0, 1.0);
            case WEST -> new Vec3d(0.0, 0.0, -1.0);
            case TOP, BOTTOM -> new Vec3d(1.0, 0.0, 0.0);
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
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null || client.options == null) {
            return 1.0;
        }

        double fov = MathHelper.clamp(
                client.options.getFov().getValue(),
                FOV_MIN,
                FOV_MAX
        );

        double fovCompensation = Math.pow(
                FOV_REFERENCE / fov,
                FOV_COMPENSATION_STRENGTH
        );

        return MathHelper.clamp(
                fovCompensation,
                FOV_COMPENSATION_MIN,
                FOV_COMPENSATION_MAX
        );
    }

    private static double scaledConfig(double value, double min, double max) {
        double t = MathHelper.clamp((value - 1.0) / 9.0, 0.0, 1.0);
        return min + (max - min) * t;
    }
}
