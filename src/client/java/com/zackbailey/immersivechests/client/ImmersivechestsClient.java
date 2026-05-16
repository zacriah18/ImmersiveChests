package com.zackbailey.immersivechests.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImmersivechestsClient implements ClientModInitializer {

    public static final Logger LOGGER =
            LoggerFactory.getLogger("immersivechests");

    @Override
    public void onInitializeClient() {

        LOGGER.info("Immersive Chests Loaded!");

        ImmersiveChestsConfigScreen.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ImmersivePendingScreenState.tick(client);
        });
    }
}