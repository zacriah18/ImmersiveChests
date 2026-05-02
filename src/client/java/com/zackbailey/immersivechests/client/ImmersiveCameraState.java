package com.zackbailey.immersivechests.client;

import com.zackbailey.immersivechests.client.records.ImmersiveResolvedTarget;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ImmersiveCameraState {
    public static boolean active = false;

    private static float progress = 0.0f;
    private static double animationDistance = 1.0;

    private static ImmersiveResolvedTarget target = null;

    private static Vec3 startPos = null;
    private static Vec3 finalPos = null;

    private static float startYaw = 0.0f;
    private static float startPitch = 0.0f;

    private static float finalYaw = 0.0f;
    private static float finalPitch = 0.0f;

    public static void setTarget(ImmersiveResolvedTarget resolvedTarget) {
        target = resolvedTarget;
    }

    public static void setActive(boolean value) {
        active = value;
    }

    public static boolean hasTarget() {
        return target != null;
    }

    public static float getProgress() {
        return progress;
    }

    public static void tick(boolean playerMoving) {
        if (ImmersiveChestsConfigScreen.instantAnimate) {
            progress = active ? 1.0f : 0.0f;
            return;
        }

        float baseSpeed = (float) ImmersiveChestsConfigScreen.animationSpeed;

        float speed = baseSpeed / (float) Math.pow(
                animationDistance,
                ImmersiveChestsConfigScreen.distanceSpeedScalar
        );

        if (active) {
            progress = Math.min(1.0f, progress + speed);
        } else {
            float closeSpeed = playerMoving ? speed * 2.5f : speed;
            progress = Math.max(0.0f, progress - closeSpeed);
        }
    }

    public static void prepareCameraTransition(
            Vec3 playerCameraPos,
            float playerYaw,
            float playerPitch
    ) {
        if (target == null || playerCameraPos == null) {
            return;
        }

        startPos = playerCameraPos;
        startYaw = playerYaw;
        startPitch = playerPitch;

        finalPos = target.center().add(target.cameraOffset());
        finalYaw = target.yaw();
        finalPitch = Mth.clamp(target.pitch(), -90.0f, 90.0f);

        animationDistance = Math.max(0.5, playerCameraPos.distanceTo(finalPos));
    }

    public static Vec3 animateToFinalPosition(Vec3 livePlayerPos) {
        if (startPos == null || finalPos == null) {
            return livePlayerPos;
        }

        return startPos.lerp(finalPos, progress);
    }

    public static Vec3 animateToStartPosition(Vec3 livePlayerPos) {
        if (finalPos == null) {
            return livePlayerPos;
        }

        return finalPos.lerp(livePlayerPos, 1.0f - progress);
    }

    public static Vec3 animatePosition(Vec3 livePlayerPos) {
        return active
                ? animateToFinalPosition(livePlayerPos)
                : animateToStartPosition(livePlayerPos);
    }

    public static float animateYaw(float liveYaw) {
        if (active) {
            return Mth.rotLerp(progress, startYaw, finalYaw);
        }

        return Mth.rotLerp(1.0f - progress, finalYaw, liveYaw);
    }

    public static float animatePitch(float livePitch) {
        return active
                ? Mth.lerp(progress, startPitch, finalPitch)
                : Mth.lerp(1.0f - progress, finalPitch, livePitch);
    }

    public static void finishClosing() {
        active = false;
        progress = 0.0f;
        animationDistance = 1.0;

        target = null;

        startPos = null;
        finalPos = null;

        startYaw = 0.0f;
        startPitch = 0.0f;
        finalYaw = 0.0f;
        finalPitch = 0.0f;
    }
}