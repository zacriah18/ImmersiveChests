package com.zackbailey.immersivechests.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class ImmersivePendingScreenState {

    public static Screen pendingScreen = null;

    public static boolean delaying = false;
    public static boolean releasing = false;

    private static int ticksWaiting = 0;
    private static float lastProgress = 0.0f;

    private ImmersivePendingScreenState() {}

    public static void begin(Screen screen) {
        pendingScreen = screen;
        delaying = true;
        releasing = false;
        ticksWaiting = 0;
        lastProgress = ImmersiveCameraState.getProgress();

        ImmersivechestsClient.LOGGER.info(
                "[DelayGUI] Pending begin screen={}",
                screen == null ? "null" : screen.getClass().getName()
        );
    }

    public static void tick(Minecraft client) {
        if (!delaying || pendingScreen == null) {
            return;
        }

        ticksWaiting++;

        boolean shouldRelease = ImmersiveCameraState.isOpeningFinished();

        float progress = ImmersiveCameraState.getProgress();
        float delta = progress - lastProgress;

        lastProgress = progress;

        if (!shouldRelease && ticksWaiting > 2 && delta > 0.0f) {
            float remaining = Math.max(0.0f, 1.0f - progress);

            int estimatedTicksRemaining =
                    (int) Math.ceil(remaining / delta);

            shouldRelease =
                    estimatedTicksRemaining <= ImmersiveChestsConfigScreen.delayGUIReleaseTicks;
        }

        if (shouldRelease) {
            release(client);
        }
    }

    public static void release(Minecraft client) {
        if (pendingScreen == null) {
            clear();
            return;
        }

        releasing = true;

        Screen screen = pendingScreen;

        pendingScreen = null;
        delaying = false;
        ticksWaiting = 0;
        lastProgress = 0.0f;

        ImmersivechestsClient.LOGGER.info(
                "[DelayGUI] Releasing screen={}",
                screen.getClass().getName()
        );

        client.setScreen(screen);

        releasing = false;
    }

    public static void cancelPending(Minecraft client) {
        ImmersivechestsClient.LOGGER.info("[DelayGUI] Pending screen cancelled");

        if (client != null && client.player != null) {
            client.player.closeContainer();
        }

        clear();

        if (ImmersiveCameraState.active) {
            ImmersiveCameraState.setActive(false);
        }
    }

    public static void clear() {
        pendingScreen = null;
        delaying = false;
        releasing = false;
        ticksWaiting = 0;
        lastProgress = 0.0f;
    }

    public static boolean hasPendingScreen() {
        return pendingScreen != null;
    }
}