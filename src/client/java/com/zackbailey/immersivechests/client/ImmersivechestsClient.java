package com.zackbailey.immersivechests.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class ImmersivechestsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("Immersive Chests Loaded!");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean playerMoving = false;

            if (client.player != null) {
                playerMoving = client.player.getVelocity().lengthSquared() > 0.10;
            }

            if (ImmersiveCameraState.getProgress() > 0.0f || ImmersiveCameraState.active) {
                ImmersiveCameraState.tick(playerMoving);
            }
        });
    }
}