package com.zackbailey.immersivechests.client.mixin;

import com.zackbailey.immersivechests.client.ImmersiveCameraState;
import com.zackbailey.immersivechests.client.ImmersiveChestsConfigScreen;
import com.zackbailey.immersivechests.client.ImmersivePendingScreenState;
import com.zackbailey.immersivechests.client.ImmersiveTargetResolver;
import com.zackbailey.immersivechests.client.records.ImmersiveResolvedTarget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.Vec3d;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void immersivechests_onSetScreen(Screen screen, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (ImmersivePendingScreenState.releasing) {
            return;
        }

        if (ImmersivePendingScreenState.delaying && screen != null) {
            String screenName = screen.getClass().getName();

            if (screenName.contains("InventoryScreen")
                    || screen instanceof GameMenuScreen) {
                ImmersivePendingScreenState.cancelPending(client);
                ci.cancel();
                return;
            }
        }

        if (screen instanceof GameMenuScreen) {
            ImmersivePendingScreenState.clear();
            ImmersiveCameraState.finishClosing();
            return;
        }

        boolean closingImmersiveTarget =
                screen == null && ImmersiveCameraState.active;

        if (closingImmersiveTarget) {
            ImmersivePendingScreenState.clear();
            ImmersiveCameraState.setActive(false);
            return;
        }

        if (screen == null || client.player == null) {
            return;
        }

        Vec3d playerCameraPos =
                client.player.getCameraPosVec(1.0f);

        float playerYaw = client.player.getYaw();
        float playerPitch = client.player.getPitch();

        ImmersiveResolvedTarget target =
                ImmersiveTargetResolver.resolve(
                        client,
                        screen,
                        playerCameraPos,
                        playerYaw
                );

        if (target == null) {
            return;
        }

        ImmersiveCameraState.setTarget(target);

        ImmersiveCameraState.prepareCameraTransition(
                playerCameraPos,
                playerYaw,
                playerPitch
        );

        ImmersiveCameraState.setActive(true);

        if (ImmersiveChestsConfigScreen.delayGUI
                && !shouldBypassPendingScreen(screen)) {
            ImmersivePendingScreenState.begin(screen);
            ci.cancel();
        }
    }

    private static boolean shouldBypassPendingScreen(Screen screen) {
        if (screen == null) {
            return false;
        }

        String name = screen.getClass().getName().toLowerCase();

        return name.contains("book")
                || name.contains("lectern");
    }
}