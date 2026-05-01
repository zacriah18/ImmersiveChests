package com.zackbailey.immersivechests.client.mixin;

import com.zackbailey.immersivechests.client.ImmersiveCameraState;
import com.zackbailey.immersivechests.client.ImmersiveChestsConfigScreen;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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
    protected abstract void setPos(double x, double y, double z);

    @Inject(method = "update", at = @At("TAIL"))
    private void immersivechests_afterCameraUpdate(
            World area,
            Entity focusedEntity,
            boolean thirdPerson,
            boolean inverseView,
            float tickProgress,
            CallbackInfo ci
    ) {
        if (!ImmersiveChestsConfigScreen.enabled || ImmersiveCameraState.getProgress() <= 0.0f) {
            return;
        }

        Vec3d livePos = focusedEntity.getCameraPosVec(tickProgress);
        Vec3d newPos = ImmersiveCameraState.animatePosition(livePos);

        this.setPos(newPos.x, newPos.y, newPos.z);
        this.setRotation(
            ImmersiveCameraState.animateYaw(focusedEntity.getYaw(tickProgress)),
            ImmersiveCameraState.animatePitch(focusedEntity.getPitch(tickProgress))
        );

        if (!ImmersiveCameraState.active && newPos.squaredDistanceTo(livePos) < 0.0001) {
            ImmersiveCameraState.finishClosing();
        }
    }

    
}