package com.zackbailey.immersivechests.client.records;

import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientation;
import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientationMode;
import com.zackbailey.immersivechests.enums.ImmersiveTargetType;
import com.zackbailey.immersivechests.enums.ImmersiveYawMode;

public record ImmersiveTargetProfile(
        ImmersiveTargetType type,

        double offsetX,
        double offsetY,
        double offsetZ,

        double hozOffsetX,
        double hozOffsetY,
        double hozOffsetZ,

        double tilt,
        boolean flipYaw,

        ImmersiveCameraOrientationMode orientationMode,
        ImmersiveCameraOrientation orientation,
        ImmersiveYawMode yawMode
) {}
