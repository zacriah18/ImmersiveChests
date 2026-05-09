package com.zackbailey.immersivechests.client.records;

import com.zackbailey.immersivechests.client.ImmersiveOffsetResolver;
import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientation;
import com.zackbailey.immersivechests.enums.ImmersiveTargetType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public record ImmersiveResolvedTarget(
        ImmersiveTargetType type,
        BlockPos blockPos,
        Vec3d center,
        Vec3d cameraOffset,
        float yaw,
        float pitch,
        Entity entity,
        ImmersiveTargetProfile profile,
        ImmersiveCameraOrientation resolvedOrientation
) {
        public boolean isEntityTarget() {
                return entity != null;
        }

        public Vec3d currentCenter() {
                return entity != null ? new Vec3d(entity.getX(), entity.getY(), entity.getZ()) : center;
        }

        public Float currentYaw() {
                return entity != null ? entity.getYaw() : null;
        }

        public float currentYawValue() {
                if (entity instanceof ChestBoatEntity) {
                        return entity.getYaw();
                }

                if (entity != null) {
                        return entity.getYaw();
                }

                return yaw;
        }

        public float currentFrameYawValue() {
                return entity != null ? entity.getYaw() : yaw;
                }

                public float currentLookYawValue() {
                if (entity instanceof ChestBoatEntity) {
                        return entity.getYaw() + 180.0f;
                }

                if (entity != null) {
                        return entity.getYaw();
                }

                return yaw;
        }

        public float currentPitchValue() {
                if (entity instanceof ChestMinecartEntity) {
                        return pitch + entity.getPitch();
                }

                return pitch;
        }

        public Vec3d currentCameraOffset() {
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