package com.zackbailey.immersivechests.client;

import com.zackbailey.immersivechests.client.records.ImmersiveTargetProfile;
import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientation;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class ImmersiveYawResolver {

    private ImmersiveYawResolver() {}

    public static float resolve(
            MinecraftClient client,
            ImmersiveTargetProfile profile,
            ImmersiveCameraOrientation resolvedOrientation,
            BlockPos blockPos,
            Vec3d center,
            Vec3d playerCameraPos,
            float playerYaw
    ) {
        if (profile == null) {
            return MathHelper.wrapDegrees(playerYaw);
        }

        float yaw = switch (profile.yawMode()) {
            case PLAYER_RAW -> yawTowardPlayer(center, playerCameraPos, playerYaw);
            case PLAYER_FORWARD -> playerYaw;
            case PLAYER_BACK -> playerYaw + 180.0f;
            case PLAYER_LEFT -> playerYaw - 90.0f;
            case PLAYER_RIGHT -> playerYaw + 90.0f;

            case AXIS -> yawForAxisMode(
                    resolvedOrientation,
                    center,
                    playerCameraPos,
                    playerYaw
            );

            case FACE -> yawForFaceMode(
                    client,
                    blockPos,
                    resolvedOrientation,
                    center,
                    playerCameraPos,
                    playerYaw
            );

            case WORLD_NORTH -> yawForFacingAxis(ImmersiveCameraOrientation.NORTH);
            case WORLD_SOUTH -> yawForFacingAxis(ImmersiveCameraOrientation.SOUTH);
            case WORLD_EAST -> yawForFacingAxis(ImmersiveCameraOrientation.EAST);
            case WORLD_WEST -> yawForFacingAxis(ImmersiveCameraOrientation.WEST);
        };

        if (profile.flipYaw()) {
            yaw += 180.0f;
        }

        return MathHelper.wrapDegrees(yaw);
    }

    private static float yawForAxisMode(
            ImmersiveCameraOrientation resolvedOrientation,
            Vec3d center,
            Vec3d playerCameraPos,
            float fallbackYaw
    ) {
        if (isSide(resolvedOrientation)) {
            return yawLookingInFromOrientation(resolvedOrientation);
        }

        return snappedYawTowardPlayer(center, playerCameraPos, fallbackYaw);
    }

    private static float yawForFaceMode(
            MinecraftClient client,
            BlockPos blockPos,
            ImmersiveCameraOrientation resolvedOrientation,
            Vec3d center,
            Vec3d playerCameraPos,
            float fallbackYaw
    ) {
        if (isSide(resolvedOrientation)) {
            return yawLookingInFromOrientation(resolvedOrientation);
        }

        ImmersiveCameraOrientation blockFacing =
                ImmersiveOrientationResolver.blockFacingRaw(client, blockPos);

        if (isSide(blockFacing)) {
            return yawLookingAtBlockFront(blockFacing);
        }

        return snappedYawTowardPlayer(center, playerCameraPos, fallbackYaw);
    }

    private static float yawLookingInFromOrientation(
            ImmersiveCameraOrientation orientation
    ) {
        return yawForFacingAxis(oppositeAxis(orientation));
    }

    private static float yawLookingAtBlockFront(
            ImmersiveCameraOrientation blockFacing
    ) {
        return yawForFacingAxis(oppositeAxis(blockFacing));
    }

    private static float snappedYawTowardPlayer(
            Vec3d center,
            Vec3d playerCameraPos,
            float fallbackYaw
    ) {
        if (center == null || playerCameraPos == null) {
            return fallbackYaw;
        }

        ImmersiveCameraOrientation playerSide =
                sideFromBlockToPlayer(center, playerCameraPos);

        return yawLookingInFromOrientation(playerSide);
    }

    private static ImmersiveCameraOrientation sideFromBlockToPlayer(
            Vec3d center,
            Vec3d playerCameraPos
    ) {
        double dx = playerCameraPos.x - center.x;
        double dz = playerCameraPos.z - center.z;

        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0.0
                    ? ImmersiveCameraOrientation.EAST
                    : ImmersiveCameraOrientation.WEST;
        }

        return dz > 0.0
                ? ImmersiveCameraOrientation.SOUTH
                : ImmersiveCameraOrientation.NORTH;
    }

    private static float yawTowardPlayer(
            Vec3d center,
            Vec3d playerCameraPos,
            float fallbackYaw
    ) {
        if (center == null || playerCameraPos == null) {
            return fallbackYaw;
        }

        Vec3d dir = new Vec3d(
                playerCameraPos.x - center.x,
                0.0,
                playerCameraPos.z - center.z
        );

        if (dir.lengthSquared() < 0.001) {
            return fallbackYaw;
        }

        dir = dir.normalize();

        return MathHelper.wrapDegrees(
                (float) (Math.atan2(-dir.x, dir.z) * (180.0 / Math.PI)) + 180.0f
        );
    }

    private static boolean isSide(
            ImmersiveCameraOrientation orientation
    ) {
        return orientation == ImmersiveCameraOrientation.NORTH
                || orientation == ImmersiveCameraOrientation.SOUTH
                || orientation == ImmersiveCameraOrientation.EAST
                || orientation == ImmersiveCameraOrientation.WEST;
    }

    private static ImmersiveCameraOrientation oppositeAxis(
            ImmersiveCameraOrientation axis
    ) {
        return switch (axis) {
            case NORTH -> ImmersiveCameraOrientation.SOUTH;
            case SOUTH -> ImmersiveCameraOrientation.NORTH;
            case EAST -> ImmersiveCameraOrientation.WEST;
            case WEST -> ImmersiveCameraOrientation.EAST;
            case TOP, BOTTOM -> axis;
        };
    }

    public static float yawForFacingAxis(
            ImmersiveCameraOrientation axis
    ) {
        return switch (axis) {
            case NORTH -> 180.0f;
            case SOUTH -> 0.0f;
            case EAST -> -90.0f;
            case WEST -> 90.0f;
            case TOP, BOTTOM -> 0.0f;
        };
    }
}