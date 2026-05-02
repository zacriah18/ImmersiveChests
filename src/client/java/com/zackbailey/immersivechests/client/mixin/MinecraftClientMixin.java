package com.zackbailey.immersivechests.client.mixin;

import com.zackbailey.immersivechests.client.ImmersiveCameraState;
import com.zackbailey.immersivechests.client.ImmersiveTargetResolver;
import com.zackbailey.immersivechests.client.records.ImmersiveResolvedTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void immersivechests_onSetScreen(Screen screen, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();

        if (screen instanceof PauseScreen) {
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

        Vec3 playerCameraPos = client.player.getEyePosition(1.0f);
        float playerYaw = client.player.getYRot();
        float playerPitch = client.player.getXRot();

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