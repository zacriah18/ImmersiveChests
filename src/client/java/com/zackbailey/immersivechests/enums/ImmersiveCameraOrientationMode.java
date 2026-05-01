package com.zackbailey.immersivechests.enums;

public enum ImmersiveCameraOrientationMode {
    FIXED,        // use the configured ImmersiveCameraOrientation directly
    NEAREST_AXIS, // choose nearest horizontal side from player position
    BLOCK_FACE,   // choose the block's facing/front/interaction face
}
