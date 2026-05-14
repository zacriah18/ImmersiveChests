package com.zackbailey.immersivechests.client;

import com.zackbailey.immersivechests.client.records.ImmersiveResolvedTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ImmersiveCameraState {
    public static boolean active = false;

    private enum Phase {
        IDLE,
        OPENING,
        ACTIVE,
        CLOSING
    }

    private static Phase phase = Phase.IDLE;

    private static ImmersiveResolvedTarget target = null;

    private static Vec3 startPos = null;
    private static Vec3 targetPos = null;
    private static Vec3 currentPos = null;

    private static float startYaw = 0.0f;
    private static float startPitch = 0.0f;

    private static float targetYaw = 0.0f;
    private static float targetPitch = 0.0f;

    private static float currentYaw = 0.0f;
    private static float currentPitch = 0.0f;

    private static float progress = 0.0f;

    private ImmersiveCameraState() {}

    public static void setTarget(ImmersiveResolvedTarget resolvedTarget) {
        target = resolvedTarget;
    }

    public static void setActive(boolean value) {
        if (active == value) {
            return;
        }

        active = value;

        if (active) {
            phase = Phase.OPENING;
            progress = 0.0f;
        } else {
            phase = Phase.CLOSING;
            progress = 0.0f;

            startPos = currentPos;
            startYaw = currentYaw;
            startPitch = currentPitch;
        }
    }

    public static boolean hasTarget() {
        return target != null;
    }

    public static float getProgress() {
        return progress;
    }

    public static void prepareCameraTransition(
            Vec3 playerCameraPos,
            float playerYaw,
            float playerPitch
    ) {
        if (playerCameraPos == null || target == null) {
            return;
        }

        currentPos = playerCameraPos;
        currentYaw = playerYaw;
        currentPitch = playerPitch;

        startPos = playerCameraPos;
        startYaw = playerYaw;
        startPitch = playerPitch;

        refreshTargetState();

        progress = 0.0f;
        phase = active ? Phase.OPENING : Phase.IDLE;
    }

    public static void tick(boolean playerMoving) {
        if (phase == Phase.IDLE || currentPos == null) {
            return;
        }

        if (phase == Phase.OPENING || phase == Phase.ACTIVE) {
            refreshTargetState();
        }

        if (startPos == null || targetPos == null) {
            return;
        }

        double speed = animationStepSpeed();

        if (phase == Phase.CLOSING) {
            speed *= ImmersiveChestsConfigScreen.closeAnimationScale;

            if (playerMoving) {
                speed *= 1.35;
            }
        }

        speed = Mth.clamp(speed, 0.001, 0.08);

        progress = (float) clamp01(progress + speed);

        double eased = smootherStep(progress);

        currentPos = startPos.lerp(targetPos, eased);
        currentYaw = Mth.rotLerp((float) eased, startYaw, targetYaw);
        currentPitch = Mth.rotLerp((float) eased, startPitch, targetPitch);

        applySnapThreshold();

        if (progress >= 1.0f) {
            if (phase == Phase.OPENING) {
                phase = Phase.ACTIVE;
            } else if (phase == Phase.CLOSING) {
                finishClosing();
            }
        }
    }

    public static Vec3 animatePosition(Vec3 livePlayerPos) {
        if (currentPos == null) {
            return livePlayerPos;
        }

        if (phase == Phase.CLOSING && livePlayerPos != null) {
            targetPos = livePlayerPos;
        }

        return currentPos;
    }

    public static Vec3 animateToFinalPosition(Vec3 livePlayerPos) {
        return animatePosition(livePlayerPos);
    }

    public static Vec3 animateToStartPosition(Vec3 livePlayerPos) {
        return animatePosition(livePlayerPos);
    }

    public static float animateYaw(float liveYaw) {
        if (currentPos == null) {
            return liveYaw;
        }

        if (phase == Phase.CLOSING) {
            targetYaw = liveYaw;
        }

        return currentYaw;
    }

    public static float animatePitch(float livePitch) {
        if (currentPos == null) {
            return livePitch;
        }

        if (phase == Phase.CLOSING) {
            targetPitch = livePitch;
        }

        return currentPitch;
    }

    public static void refreshActiveTarget(
            Minecraft client,
            Vec3 playerCameraPos,
            float playerYaw
    ) {
        if (!active || target == null) {
            return;
        }

        refreshTargetState();
    }

    public static void finishClosing() {
        active = false;
        phase = Phase.IDLE;

        target = null;

        startPos = null;
        targetPos = null;
        currentPos = null;

        startYaw = 0.0f;
        startPitch = 0.0f;

        targetYaw = 0.0f;
        targetPitch = 0.0f;

        currentYaw = 0.0f;
        currentPitch = 0.0f;

        progress = 0.0f;
    }

    private static void refreshTargetState() {
        if (target == null) {
            return;
        }

        targetPos = target.currentCenter().add(target.currentCameraOffset());
        targetYaw = target.currentLookYawValue();
        targetPitch = Mth.clamp(target.currentPitchValue(), -90.0f, 90.0f);
    }

    private static double animationStepSpeed() {
        double animationSpeed = scaledSpeed(
                ImmersiveChestsConfigScreen.animationSpeed,
                0.005,
                0.045
        );

        if (animationSpeed <= 0.0) {
            return 1.0;
        }

        double distanceScale = scaledSpeed(
                1,
                0.0,
                0.03
        );

        double openScale = scaledSpeed(
                ImmersiveChestsConfigScreen.openAnimationScale,
                0.5,
                2.0
        );

        double closeScale = scaledSpeed(
                ImmersiveChestsConfigScreen.closeAnimationScale,
                0.5,
                2.0
        );

        double distance = startPos.distanceTo(targetPos);
        double normalizedDistance = Math.min(distance / 4.0, 1.0);

        double distanceBoost =
                normalizedDistance * distanceScale;

        double speed = animationSpeed + distanceBoost;

        if (phase == Phase.OPENING) {
            speed *= openScale;
        }

        if (phase == Phase.CLOSING) {
            speed *= closeScale;
        }

        return speed;
    }

    private static void applySnapThreshold() {
        double snapThreshold = scaledSpeed(
                1, // removed user configurability but left architecture for future retrospectoptics
                0.00001,
                0.003
        );

        if (targetPos != null && currentPos.distanceTo(targetPos) <= snapThreshold) {
            currentPos = targetPos;
        }

        if (Math.abs(Mth.wrapDegrees(targetYaw - currentYaw)) <= snapThreshold) {
            currentYaw = targetYaw;
        }

        if (Math.abs(targetPitch - currentPitch) <= snapThreshold) {
            currentPitch = targetPitch;
        }
    }

    private static double smootherStep(double value) {
        double t = clamp01(value);
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    public static boolean shouldOverrideCamera() {
        return phase != Phase.IDLE;
    }

    public static boolean isClosing() {
        return phase == Phase.CLOSING;
    }

    public static boolean isCloseAnimationFinished() {
        return phase == Phase.CLOSING && progress >= 1.0f;
    }

    private static double scaledSpeed(double value, double min, double max) {
        double t = Mth.clamp((value - 1.0) / 9.0, 0.0, 1.0);
        return min + (max - min) * t;
    }

    public static void updateClosingTarget(Vec3 livePlayerPos, float liveYaw, float livePitch) {
        if (phase != Phase.CLOSING || livePlayerPos == null) {
            return;
        }

        targetPos = livePlayerPos;
        targetYaw = liveYaw;
        targetPitch = livePitch;
    }
}