package com.zackbailey.immersivechests.client;

import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientation;
import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientationMode;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public final class ImmersiveOrientationResolver {

    private ImmersiveOrientationResolver() {}

    public static ImmersiveCameraOrientation resolve(
            Minecraft client,
            ImmersiveCameraOrientationMode orientationMode,
            ImmersiveCameraOrientation fixedOrientation,
            BlockPos blockPos,
            Vec3 center,
            Vec3 playerCameraPos
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
            Minecraft client,
            ImmersiveCameraOrientationMode orientationMode,
            ImmersiveCameraOrientation fixedOrientation,
            BlockPos blockPos,
            Vec3 center,
            Vec3 playerCameraPos
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
            Minecraft client,
            BlockPos blockPos,
            Vec3 center,
            Vec3 playerCameraPos,
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

        resolvedOrientation = applySmokerBlastFurnaceHorizontalFlip(
                client,
                blockPos,
                resolvedOrientation
        );

        resolvedOrientation = applyStonecutterAxisSymmetry(
            client,
            blockPos,
            center,
            playerCameraPos,
            resolvedOrientation
    );

        return resolvedOrientation;
    }

    private static boolean isBarrel(
            Minecraft client,
            BlockPos blockPos
    ) {
        return client != null
                && client.level != null
                && blockPos != null
                && client.level.getBlockState(blockPos).getBlock() instanceof BarrelBlock;
    }

    public static ImmersiveCameraOrientation resolveBarrelOrientation(
            Minecraft client,
            BlockPos blockPos,
            Vec3 center,
            Vec3 playerCameraPos
    ) {
        if (client == null || client.level == null || blockPos == null) {
            return safeFallback(center, playerCameraPos, ImmersiveCameraOrientation.SOUTH);
        }

        var state = client.level.getBlockState(blockPos);

        if (!state.hasProperty(BlockStateProperties.FACING)) {
            return safeFallback(center, playerCameraPos, ImmersiveCameraOrientation.SOUTH);
        }

        Direction facing = state.getValue(BlockStateProperties.FACING);

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

    private static ImmersiveCameraOrientation applySmokerBlastFurnaceHorizontalFlip(
            Minecraft client,
            BlockPos blockPos,
            ImmersiveCameraOrientation orientation
    ) {
        if (client == null || client.level == null || blockPos == null || orientation == null) {
            return orientation;
        }

        if (orientation == ImmersiveCameraOrientation.TOP
                || orientation == ImmersiveCameraOrientation.BOTTOM) {
            return orientation;
        }

        var block = client.level.getBlockState(blockPos).getBlock();

        String blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString();

        if (blockId.equals("minecraft:smoker")
                || blockId.equals("minecraft:blast_furnace")
                || blockId.equals("minecraft:furnace")) {
            return oppositeAxis(orientation);
        }

        return orientation;
    }

    private static ImmersiveCameraOrientation applyStonecutterAxisSymmetry(
            Minecraft client,
            BlockPos blockPos,
            Vec3 center,
            Vec3 playerCameraPos,
            ImmersiveCameraOrientation resolvedOrientation
    ) {
        if (client == null
                || client.level == null
                || blockPos == null
                || center == null
                || playerCameraPos == null
                || resolvedOrientation == null) {
            return resolvedOrientation;
        }

        var block = client.level.getBlockState(blockPos).getBlock();
        String blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString();

        if (!blockId.equals("minecraft:stonecutter")) {
            return resolvedOrientation;
        }

        if (resolvedOrientation == ImmersiveCameraOrientation.TOP
                || resolvedOrientation == ImmersiveCameraOrientation.BOTTOM) {
            return resolvedOrientation;
        }

        ImmersiveCameraOrientation face = resolvedOrientation;
        ImmersiveCameraOrientation opposite = oppositeAxis(face);
        ImmersiveCameraOrientation nearest = nearestSideIgnoringBlocks(center, playerCameraPos);

        if (nearest == face || nearest == opposite) {
            return nearest;
        }

        return face;
    }
    
    public static ImmersiveCameraOrientation applyAirPriority(
            Minecraft client,
            BlockPos blockPos,
            Vec3 playerCameraPos,
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

    private static boolean isOrientationObstructed(
            Minecraft client,
            BlockPos blockPos,
            ImmersiveCameraOrientation orientation
    ) {
        if (client == null || client.level == null || blockPos == null || orientation == null) {
            return false;
        }

        BlockPos checkPos = switch (orientation) {
            case TOP -> blockPos.above();
            case BOTTOM -> blockPos.below();
            case NORTH -> blockPos.north();
            case SOUTH -> blockPos.south();
            case EAST -> blockPos.east();
            case WEST -> blockPos.west();
        };

        return !client.level.getBlockState(checkPos).isAir();
    }

    public static ImmersiveCameraOrientation nearestSideIgnoringBlocks(
            Vec3 center,
            Vec3 playerCameraPos
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
            Minecraft client,
            BlockPos blockPos,
            Vec3 center,
            Vec3 playerCameraPos
    ) {
        Direction facing = getFacingOrNull(client, blockPos);

        if (facing == null || facing == Direction.UP || facing == Direction.DOWN) {
            return nearestSideIgnoringBlocks(center, playerCameraPos);
        }

        return oppositeAxis(directionToOrientation(facing));
    }

    public static ImmersiveCameraOrientation closestOpenSide(
            Minecraft client,
            BlockPos blockPos,
            Vec3 playerCameraPos
    ) {
        if (client == null || client.level == null || blockPos == null || playerCameraPos == null) {
            return null;
        }

        ImmersiveCameraOrientation horizontal = closestOpenHorizontalSide(
                client,
                blockPos,
                playerCameraPos
        );

        if (horizontal != null) {
            return horizontal;
        }

        return closestOpenVerticalSide(
                client,
                blockPos,
                playerCameraPos
        );
    }

    private static ImmersiveCameraOrientation closestOpenHorizontalSide(
            Minecraft client,
            BlockPos blockPos,
            Vec3 playerCameraPos
    ) {
        Vec3 flatPlayer = new Vec3(
                playerCameraPos.x,
                0.0,
                playerCameraPos.z
        );

        ImmersiveCameraOrientation best = null;
        double bestDistanceSq = Double.MAX_VALUE;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos sidePos = blockPos.relative(direction);

            if (!client.level.getBlockState(sidePos).isAir()) {
                continue;
            }

            Vec3 sideCenter = Vec3.atCenterOf(sidePos);
            Vec3 flatSideCenter = new Vec3(
                    sideCenter.x,
                    0.0,
                    sideCenter.z
            );

            double distanceSq = flatPlayer.distanceToSqr(flatSideCenter);

            if (distanceSq < bestDistanceSq) {
                bestDistanceSq = distanceSq;
                best = directionToOrientation(direction);
            }
        }

        return best;
    }

    private static ImmersiveCameraOrientation closestOpenVerticalSide(
            Minecraft client,
            BlockPos blockPos,
            Vec3 playerCameraPos
    ) {
        boolean topOpen = client.level.getBlockState(blockPos.above()).isAir();
        boolean bottomOpen = client.level.getBlockState(blockPos.below()).isAir();

        if (topOpen && !bottomOpen) {
            return ImmersiveCameraOrientation.TOP;
        }

        if (bottomOpen && !topOpen) {
            return ImmersiveCameraOrientation.BOTTOM;
        }

        if (topOpen && bottomOpen) {
            double topDistanceSq = playerCameraPos.distanceToSqr(Vec3.atCenterOf(blockPos.above()));
            double bottomDistanceSq = playerCameraPos.distanceToSqr(Vec3.atCenterOf(blockPos.below()));

            return topDistanceSq <= bottomDistanceSq
                    ? ImmersiveCameraOrientation.TOP
                    : ImmersiveCameraOrientation.BOTTOM;
        }

        return null;
    }

    public static boolean isBlockedAbove(
            Minecraft client,
            BlockPos pos
    ) {
        return client != null
                && client.level != null
                && pos != null
                && !client.level.getBlockState(pos.above()).isAir();
    }

    private static Direction getFacingOrNull(
            Minecraft client,
            BlockPos blockPos
    ) {
        if (client == null || client.level == null || blockPos == null) {
            return null;
        }

        var state = client.level.getBlockState(blockPos);

        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }

        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
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
            Minecraft client,
            BlockPos blockPos
    ) {
        Direction facing = getFacingOrNull(client, blockPos);

        if (facing == null) {
            return null;
        }

        return directionToOrientation(facing);
    }

    private static ImmersiveCameraOrientation safeFallback(
            Vec3 center,
            Vec3 playerCameraPos,
            ImmersiveCameraOrientation fallback
    ) {
        if (center != null && playerCameraPos != null) {
            return nearestSideIgnoringBlocks(center, playerCameraPos);
        }

        return fallback != null ? fallback : ImmersiveCameraOrientation.SOUTH;
    }
}