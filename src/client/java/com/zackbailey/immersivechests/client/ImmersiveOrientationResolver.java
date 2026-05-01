package com.zackbailey.immersivechests.client;

import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientation;
import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientationMode;

import net.minecraft.block.BarrelBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class ImmersiveOrientationResolver {

    private ImmersiveOrientationResolver() {}

    public static ImmersiveCameraOrientation resolve(
            MinecraftClient client,
            ImmersiveCameraOrientationMode orientationMode,
            ImmersiveCameraOrientation fixedOrientation,
            BlockPos blockPos,
            Vec3d center,
            Vec3d playerCameraPos
    ) {
        ImmersiveCameraOrientation resolvedOrientation =
                resolveBaseOrientation(
                        client,
                        orientationMode,
                        fixedOrientation,
                        blockPos,
                        center,
                        playerCameraPos
                );

        resolvedOrientation =
                applyHardcodedOverrides(
                        client,
                        blockPos,
                        center,
                        playerCameraPos,
                        resolvedOrientation
                );

        resolvedOrientation =
                applyAirPriority(
                        client,
                        blockPos,
                        playerCameraPos,
                        resolvedOrientation
                );

        return resolvedOrientation;
    }

    private static ImmersiveCameraOrientation resolveBaseOrientation(
            MinecraftClient client,
            ImmersiveCameraOrientationMode orientationMode,
            ImmersiveCameraOrientation fixedOrientation,
            BlockPos blockPos,
            Vec3d center,
            Vec3d playerCameraPos
    ) {
        if (orientationMode == null) {
            return safeFallback(center, playerCameraPos, fixedOrientation);
        }

        return switch (orientationMode) {
            case FIXED -> fixedOrientation != null
                    ? fixedOrientation
                    : safeFallback(center, playerCameraPos, ImmersiveCameraOrientation.SOUTH);

            case NEAREST_AXIS -> nearestSideIgnoringBlocks(center, playerCameraPos);

            case BLOCK_FACE -> blockFaceOrientation(
                    client,
                    blockPos,
                    center,
                    playerCameraPos
            );
        };
    }

    private static ImmersiveCameraOrientation applyHardcodedOverrides(
            MinecraftClient client,
            BlockPos blockPos,
            Vec3d center,
            Vec3d playerCameraPos,
            ImmersiveCameraOrientation resolvedOrientation
    ) {
        if (ImmersiveChestsConfigScreen.alwaysBarrelFace && isBarrel(client, blockPos)) {
            return resolveBarrelOrientation(
                    client,
                    blockPos,
                    center,
                    playerCameraPos
            );
        }

        return resolvedOrientation;
    }

    public static ImmersiveCameraOrientation applyAirPriority(
            MinecraftClient client,
            BlockPos blockPos,
            Vec3d playerCameraPos,
            ImmersiveCameraOrientation preferred
    ) {
        if (!ImmersiveChestsConfigScreen.prioritizeAirBlock || blockPos == null) {
            return preferred;
        }

        if (!isOrientationObstructed(client, blockPos, preferred)) {
            return preferred;
        }

        ImmersiveCameraOrientation openSide =
                closestOpenSide(client, blockPos, playerCameraPos);

        return openSide != null ? openSide : preferred;
    }

    private static boolean isBarrel(
            MinecraftClient client,
            BlockPos blockPos
    ) {
        return client != null
                && client.world != null
                && blockPos != null
                && client.world.getBlockState(blockPos).getBlock() instanceof BarrelBlock;
    }

    public static ImmersiveCameraOrientation resolveBarrelOrientation(
            MinecraftClient client,
            BlockPos blockPos,
            Vec3d center,
            Vec3d playerCameraPos
    ) {
        if (client == null || client.world == null || blockPos == null) {
            return safeFallback(center, playerCameraPos, ImmersiveCameraOrientation.SOUTH);
        }

        var state = client.world.getBlockState(blockPos);

        if (!state.contains(Properties.FACING)) {
            return safeFallback(center, playerCameraPos, ImmersiveCameraOrientation.SOUTH);
        }

        Direction facing = state.get(Properties.FACING);

        if (ImmersiveChestsConfigScreen.debugLogging) {
            System.out.println("[ImmersiveChests] Barrel orientation override");
            System.out.println("[ImmersiveChests] Barrel FACING: " + facing);
        }

        return switch (facing) {
            case UP -> ImmersiveCameraOrientation.TOP;
            case DOWN -> ImmersiveCameraOrientation.BOTTOM;
            case NORTH -> ImmersiveCameraOrientation.NORTH;
            case SOUTH -> ImmersiveCameraOrientation.SOUTH;
            case EAST -> ImmersiveCameraOrientation.EAST;
            case WEST -> ImmersiveCameraOrientation.WEST;
        };
    }

    private static boolean isOrientationObstructed(
            MinecraftClient client,
            BlockPos blockPos,
            ImmersiveCameraOrientation orientation
    ) {
        if (client == null || client.world == null || blockPos == null || orientation == null) {
            return false;
        }

        BlockPos checkPos = switch (orientation) {
            case TOP -> blockPos.up();
            case BOTTOM -> blockPos.down();
            case NORTH -> blockPos.north();
            case SOUTH -> blockPos.south();
            case EAST -> blockPos.east();
            case WEST -> blockPos.west();
        };

        return !client.world.getBlockState(checkPos).isAir();
    }

    public static ImmersiveCameraOrientation nearestSideIgnoringBlocks(
            Vec3d center,
            Vec3d playerCameraPos
    ) {
        if (center == null || playerCameraPos == null) {
            return ImmersiveCameraOrientation.SOUTH;
        }

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

    public static ImmersiveCameraOrientation blockFaceOrientation(
            MinecraftClient client,
            BlockPos blockPos,
            Vec3d center,
            Vec3d playerCameraPos
    ) {
        Direction facing = getFacingOrNull(client, blockPos);

        if (facing == null || facing == Direction.UP || facing == Direction.DOWN) {
            return nearestSideIgnoringBlocks(center, playerCameraPos);
        }

        return oppositeAxis(directionToOrientation(facing));
    }

    public static ImmersiveCameraOrientation closestOpenSide(
            MinecraftClient client,
            BlockPos blockPos,
            Vec3d playerCameraPos
    ) {
        if (client == null || client.world == null || blockPos == null || playerCameraPos == null) {
            return null;
        }

        Vec3d flatPlayer = new Vec3d(
                playerCameraPos.x,
                0.0,
                playerCameraPos.z
        );

        ImmersiveCameraOrientation best = null;
        double bestDistanceSq = Double.MAX_VALUE;

        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos sidePos = blockPos.offset(direction);

            if (!client.world.getBlockState(sidePos).isAir()) {
                continue;
            }

            Vec3d sideCenter = Vec3d.ofCenter(sidePos);
            Vec3d flatSideCenter = new Vec3d(
                    sideCenter.x,
                    0.0,
                    sideCenter.z
            );

            double distanceSq = flatPlayer.squaredDistanceTo(flatSideCenter);

            if (distanceSq < bestDistanceSq) {
                bestDistanceSq = distanceSq;
                best = directionToOrientation(direction);
            }
        }

        return best;
    }

    public static boolean isBlockedAbove(
            MinecraftClient client,
            BlockPos pos
    ) {
        return client != null
                && client.world != null
                && pos != null
                && !client.world.getBlockState(pos.up()).isAir();
    }

    private static Direction getFacingOrNull(
            MinecraftClient client,
            BlockPos blockPos
    ) {
        if (client == null || client.world == null || blockPos == null) {
            return null;
        }

        var state = client.world.getBlockState(blockPos);

        if (state.contains(Properties.HORIZONTAL_FACING)) {
            return state.get(Properties.HORIZONTAL_FACING);
        }

        if (state.contains(Properties.FACING)) {
            return state.get(Properties.FACING);
        }

        return null;
    }

    private static ImmersiveCameraOrientation directionToOrientation(
            Direction direction
    ) {
        return switch (direction) {
            case NORTH -> ImmersiveCameraOrientation.NORTH;
            case SOUTH -> ImmersiveCameraOrientation.SOUTH;
            case EAST -> ImmersiveCameraOrientation.EAST;
            case WEST -> ImmersiveCameraOrientation.WEST;
            default -> ImmersiveCameraOrientation.SOUTH;
        };
    }

    private static ImmersiveCameraOrientation oppositeAxis(
            ImmersiveCameraOrientation axis
    ) {
        return switch (axis) {
            case NORTH -> ImmersiveCameraOrientation.SOUTH;
            case SOUTH -> ImmersiveCameraOrientation.NORTH;
            case EAST -> ImmersiveCameraOrientation.WEST;
            case WEST -> ImmersiveCameraOrientation.EAST;
            default -> axis;
        };
    }


    public static ImmersiveCameraOrientation blockFacingRaw(
            MinecraftClient client,
            BlockPos blockPos
    ) {
        Direction facing = getFacingOrNull(client, blockPos);

        if (facing == null) {
            return null;
        }

        return directionToOrientation(facing);
    }

    private static ImmersiveCameraOrientation safeFallback(
            Vec3d center,
            Vec3d playerCameraPos,
            ImmersiveCameraOrientation fallback
    ) {
        if (center != null && playerCameraPos != null) {
            return nearestSideIgnoringBlocks(center, playerCameraPos);
        }

        return fallback != null ? fallback : ImmersiveCameraOrientation.SOUTH;
    }
}