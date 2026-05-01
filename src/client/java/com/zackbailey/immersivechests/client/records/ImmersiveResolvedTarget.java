package com.zackbailey.immersivechests.client.records;

import com.zackbailey.immersivechests.enums.ImmersiveTargetType;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public record ImmersiveResolvedTarget(
        ImmersiveTargetType type,
        BlockPos blockPos,
        Vec3d center,
        Vec3d cameraOffset,
        float yaw,
        float pitch
) {}