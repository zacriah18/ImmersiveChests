package com.zackbailey.immersivechests.client.records;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;

public record ImmersiveTargetContext(
        ImmersiveTargetProfile profile,
        BlockPos blockPos,
        Vec3 center,
        Entity entity
) {

        public Vec3 currentCenter() {
        return entity != null
                ? new Vec3(
                        entity.getX(),
                        entity.getY(),
                        entity.getZ()
                )
                : center;
        }

        public Float currentYaw() {
        return entity != null
                ? entity.getYRot()
                : null;
        }
}