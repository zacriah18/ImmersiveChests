package com.zackbailey.immersivechests.client.mixin;

import com.zackbailey.immersivechests.client.ImmersiveCameraState;
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

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void immersivechests_onSetScreen(Screen screen, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (screen instanceof GameMenuScreen) {
            ImmersiveCameraState.finishClosing();
            return;
        }

        boolean closingImmersiveTarget = screen == null && ImmersiveCameraState.active;

        if (closingImmersiveTarget) {
            ImmersiveCameraState.setActive(false);
            return;
        }

        if (screen == null || client.player == null) {
            return;
        }

        Vec3d playerCameraPos = client.player.getCameraPosVec(1.0f);
        float playerYaw = client.player.getYaw();
        float playerPitch = client.player.getPitch();

        ImmersiveResolvedTarget target = ImmersiveTargetResolver.resolve(
                client,
                screen,
                playerCameraPos,
                playerYaw
        );

        System.out.println("setScreen called: " + screen.getClass().getName());

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
    }
}