package com.zackbailey.immersivechests.client;

import com.zackbailey.immersivechests.client.records.ImmersiveResolvedTarget;
import com.zackbailey.immersivechests.client.records.ImmersiveTargetProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.phys.Vec3;

public final class ImmersiveBoatTargetResolver {

    private ImmersiveBoatTargetResolver() {}

    public static ImmersiveResolvedTarget resolve(
            ChestBoat boat,
            ImmersiveTargetProfile profile
    ) {
        Vec3 boatCenter = new Vec3(
                boat.getX(),
                boat.getY(),
                boat.getZ()
        );

        float boatYaw = boat.getYRot();
        double yawRad = Math.toRadians(boatYaw);

        Vec3 backward = new Vec3(
                Math.sin(yawRad),
                0.0,
                -Math.cos(yawRad)
        );

        Vec3 right = new Vec3(
                Math.cos(yawRad),
                0.0,
                Math.sin(yawRad)
        );

        Vec3 cameraOffset = right.scale(profile.offsetX())
                .add(0.0, profile.offsetY(), 0.0)
                .add(backward.scale(
                        ImmersiveChestsConfigScreen.boatDistanceBehind + profile.offsetZ()
                ));

        float yaw = Mth.wrapDegrees(boatYaw + 180.0f);

        if (profile.flipYaw()) {
            yaw = Mth.wrapDegrees(yaw + 180.0f);
        }

        float pitch = Mth.clamp(
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

    public static ChestBoat ridingChestBoat(Minecraft client) {
        if (client == null || client.player == null || !client.player.isPassenger()) {
            return null;
        }

        Entity vehicle = client.player.getVehicle();

        if (vehicle instanceof ChestBoat chestBoat) {
            return chestBoat;
        }

        return null;
    }
}