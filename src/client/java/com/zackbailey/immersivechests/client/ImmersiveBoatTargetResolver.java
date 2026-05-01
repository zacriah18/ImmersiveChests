package com.zackbailey.immersivechests.client;

import com.zackbailey.immersivechests.client.records.ImmersiveResolvedTarget;
import com.zackbailey.immersivechests.client.records.ImmersiveTargetProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class ImmersiveBoatTargetResolver {

    private ImmersiveBoatTargetResolver() {}

    public static ImmersiveResolvedTarget resolve(
            ChestBoatEntity boat,
            ImmersiveTargetProfile profile
    ) {
        Vec3d boatCenter = new Vec3d(
                boat.getX(),
                boat.getY(),
                boat.getZ()
        );

        float boatYaw = boat.getYaw();
        double yawRad = Math.toRadians(boatYaw);

        Vec3d backward = new Vec3d(
                Math.sin(yawRad),
                0.0,
                -Math.cos(yawRad)
        );

        Vec3d right = new Vec3d(
                Math.cos(yawRad),
                0.0,
                Math.sin(yawRad)
        );

        Vec3d cameraOffset = right.multiply(profile.offsetX())
                .add(0.0, profile.offsetY(), 0.0)
                .add(backward.multiply(
                        ImmersiveChestsConfigScreen.boatDistanceBehind + profile.offsetZ()
                ));

        float yaw = MathHelper.wrapDegrees(boatYaw + 180.0f);

        if (profile.flipYaw()) {
            yaw = MathHelper.wrapDegrees(yaw + 180.0f);
        }

        float pitch = MathHelper.clamp(
                90.0f + (float) profile.tilt(),
                -90.0f,
                90.0f
        );

        return new ImmersiveResolvedTarget(
                profile.type(),
                null,
                boatCenter,
                cameraOffset,
                yaw,
                pitch
        );
    }

    public static ChestBoatEntity ridingChestBoat(MinecraftClient client) {
        if (client == null || client.player == null || !client.player.hasVehicle()) {
            return null;
        }

        Entity vehicle = client.player.getVehicle();

        if (vehicle instanceof ChestBoatEntity chestBoat) {
            return chestBoat;
        }

        return null;
    }
}