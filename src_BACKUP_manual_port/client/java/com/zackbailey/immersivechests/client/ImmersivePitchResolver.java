package com.zackbailey.immersivechests.client;

import com.zackbailey.immersivechests.client.records.ImmersiveTargetProfile;
import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientation;

public final class ImmersivePitchResolver {

    private ImmersivePitchResolver() {}

    public static float resolve(
            ImmersiveTargetProfile profile,
            ImmersiveCameraOrientation resolvedOrientation
    ) {
        if (profile == null || resolvedOrientation == null) {
            return 0.0f;
        }

        float tilt = (float) profile.tilt();

        return switch (resolvedOrientation) {
            case TOP -> 90.0f + tilt;
            case BOTTOM -> -90.0f + tilt;
            case NORTH, SOUTH, EAST, WEST -> tilt;
        };
    }
}