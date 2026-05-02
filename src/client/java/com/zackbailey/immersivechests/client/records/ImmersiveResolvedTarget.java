package com.zackbailey.immersivechests.client.records;

import com.zackbailey.immersivechests.enums.ImmersiveTargetType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public record ImmersiveResolvedTarget(
        ImmersiveTargetType type,
        BlockPos blockPos,
        Vec3 center,
        Vec3 cameraOffset,
        float yaw,
        float pitch
) {}