package com.zackbailey.immersivechests.client.records;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;

public record ImmersiveTargetContext(
        ImmersiveTargetProfile profile,
        BlockPos blockPos,
        Vec3d center,
        Entity entity
) {

        public Vec3d currentCenter() {
        return entity != null
                ? new Vec3d(
                        entity.getX(),
                        entity.getY(),
                        entity.getZ()
                )
                : center;
        }

        public Float currentYaw() {
        return entity != null
                ? entity.getYaw()
                : null;
        }
}