package com.zackbailey.immersivechests.client.mixin;

import com.zackbailey.immersivechests.client.ImmersiveCameraState;
import com.zackbailey.immersivechests.client.ImmersiveChestsConfigScreen;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Inject(method = "setup", at = @At("TAIL"))
    private void immersivechests_afterCameraUpdate(
            Level area,
            Entity focusedEntity,
            boolean thirdPerson,
            boolean inverseView,
            float tickProgress,
            CallbackInfo ci
    ) {
        if (!ImmersiveChestsConfigScreen.enabled || ImmersiveCameraState.getProgress() <= 0.0f) {
            return;
        }

        Vec3 livePos = focusedEntity.getEyePosition(tickProgress);
        Vec3 newPos = ImmersiveCameraState.animatePosition(livePos);

        this.setPosition(newPos.x, newPos.y, newPos.z);
        this.setRotation(
            ImmersiveCameraState.animateYaw(focusedEntity.getViewYRot(tickProgress)),
            ImmersiveCameraState.animatePitch(focusedEntity.getViewXRot(tickProgress))
        );

        if (!ImmersiveCameraState.active && newPos.distanceToSqr(livePos) < 0.0001) {
            ImmersiveCameraState.finishClosing();
        }
    }

    
}