package com.zackbailey.immersivechests.client.records;

import com.zackbailey.immersivechests.client.ImmersiveOffsetResolver;
import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientation;
import com.zackbailey.immersivechests.enums.ImmersiveTargetType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public record ImmersiveResolvedTarget(
        ImmersiveTargetType type,
        BlockPos blockPos,
        Vec3 center,
        Vec3 cameraOffset,
        float yaw,
        float pitch,
        Entity entity,
        ImmersiveTargetProfile profile,
        ImmersiveCameraOrientation resolvedOrientation
) {
        public boolean isEntityTarget() {
                return entity != null;
        }

        public Vec3 currentCenter() {
                return entity != null ? new Vec3(entity.getX(), entity.getY(), entity.getZ()) : center;
        }

        public Float currentYaw() {
                return entity != null ? entity.getYRot() : null;
        }

        public float currentYawValue() {
                if (entity instanceof ChestBoat) {
                        return entity.getYRot();
                }

                if (entity != null) {
                        return entity.getYRot();
                }

                return yaw;
        }

        public float currentFrameYawValue() {
                return entity != null ? entity.getYRot() : yaw;
                }

                public float currentLookYawValue() {
                if (entity instanceof ChestBoat) {
                        return entity.getYRot() + 180.0f;
                }

                if (entity != null) {
                        return entity.getYRot();
                }

                return yaw;
        }

        public float currentPitchValue() {
                if (entity instanceof MinecartChest) {
                        return pitch + entity.getXRot();
                }

                return pitch;
        }

        public Vec3 currentCameraOffset() {
                if (entity != null) {
                        return ImmersiveOffsetResolver.resolve(
                                profile,
                                resolvedOrientation,
                                currentFrameYawValue()
                        );
                }

                return cameraOffset;
        }
}