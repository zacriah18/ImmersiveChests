package com.zackbailey.immersivechests.client;

import com.zackbailey.immersivechests.client.records.ImmersiveResolvedTarget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ImmersiveCameraState {
    public static boolean active = false;

    private static float progress = 0.0f;
    private static double animationDistance = 1.0;

    private static ImmersiveResolvedTarget target = null;

    private static Vec3d startPos = null;
    private static Vec3d finalPos = null;

    private static float startYaw = 0.0f;
    private static float startPitch = 0.0f;

    private static float finalYaw = 0.0f;
    private static float finalPitch = 0.0f;

    private static Vec3d smoothedFinalPos = null;
    private static float smoothedFinalYaw = 0.0f;
    private static float smoothedFinalPitch = 0.0f;

    private ImmersiveCameraState() {}

    public static void setTarget(ImmersiveResolvedTarget resolvedTarget) {
        target = resolvedTarget;

        if (target != null && startPos != null) {
            refreshFinalTarget();
        }
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
            Vec3d playerCameraPos,
            float playerYaw,
            float playerPitch
    ) {
        if (target == null || playerCameraPos == null) {
            return;
        }

        startPos = playerCameraPos;
        startYaw = playerYaw;
        startPitch = playerPitch;

        refreshFinalTarget();

        if (finalPos != null) {
            animationDistance = Math.max(0.5, playerCameraPos.distanceTo(finalPos));
        }
    }

    private static void refreshFinalTarget() {
        if (target == null) {
            return;
        }

        Vec3d rawFinalPos = target.currentCenter().add(target.currentCameraOffset());
        float rawFinalYaw = target.currentLookYawValue();
        float rawFinalPitch = MathHelper.clamp(target.currentPitchValue(), -90.0f, 90.0f);

        if (smoothedFinalPos == null) {
            smoothedFinalPos = rawFinalPos;
            smoothedFinalYaw = rawFinalYaw;
            smoothedFinalPitch = rawFinalPitch;
        } else {
            double smoothing = 0.35;

            smoothedFinalPos = smoothedFinalPos.lerp(rawFinalPos, smoothing);
            smoothedFinalYaw = MathHelper.lerpAngleDegrees((float) smoothing, smoothedFinalYaw, rawFinalYaw);
            smoothedFinalPitch = MathHelper.lerp((float) smoothing, smoothedFinalPitch, rawFinalPitch);
        }

        finalPos = smoothedFinalPos;
        finalYaw = smoothedFinalYaw;
        finalPitch = smoothedFinalPitch;
    }

    public static Vec3d animateToFinalPosition(Vec3d livePlayerPos) {
        if (startPos == null || finalPos == null) {
            return livePlayerPos;
        }

        refreshFinalTarget();

        if (target != null && target.isEntityTarget() && progress >= 1.0f) {
            return finalPos;
        }

        return startPos.lerp(finalPos, progress);
    }

    public static Vec3d animateToStartPosition(Vec3d livePlayerPos) {
        if (finalPos == null) {
            return livePlayerPos;
        }

        return finalPos.lerp(livePlayerPos, 1.0f - progress);
    }

    public static Vec3d animatePosition(Vec3d livePlayerPos) {
        return active
                ? animateToFinalPosition(livePlayerPos)
                : animateToStartPosition(livePlayerPos);
    }

    public static float animateYaw(float liveYaw) {
        if (active) {
            refreshFinalTarget();
            return MathHelper.lerpAngleDegrees(progress, startYaw, finalYaw);
        }

        return MathHelper.lerpAngleDegrees(1.0f - progress, finalYaw, liveYaw);
    }

    public static float animatePitch(float livePitch) {
        if (active) {
            refreshFinalTarget();
            return MathHelper.lerp(progress, startPitch, finalPitch);
        }

        return MathHelper.lerp(1.0f - progress, finalPitch, livePitch);
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

        smoothedFinalPos = null;
        smoothedFinalYaw = 0.0f;
        smoothedFinalPitch = 0.0f;
    }

    public static void refreshActiveTarget(
            MinecraftClient client,
            Vec3d playerCameraPos,
            float playerYaw
    ) {
        if (!active
                || target == null
                || !target.isEntityTarget()) {
            return;
        }

        refreshFinalTarget();
    }
}