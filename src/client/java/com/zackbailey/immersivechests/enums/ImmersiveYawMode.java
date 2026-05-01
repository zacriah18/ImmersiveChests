package com.zackbailey.immersivechests.enums;

public enum ImmersiveYawMode {
    PLAYER_FORWARD,
    PLAYER_BACK,
    PLAYER_LEFT,
    PLAYER_RIGHT,

    PLAYER_RAW,
    AXIS,
    FACE,

    WORLD_NORTH,
    WORLD_SOUTH,
    WORLD_EAST,
    WORLD_WEST;

    public String label() {
        return switch (this) {
            case PLAYER_FORWARD -> "Forward";
            case PLAYER_BACK -> "Back";
            case PLAYER_LEFT -> "Left";
            case PLAYER_RIGHT -> "Right";

            case PLAYER_RAW -> "Player Direction";
            case AXIS -> "Nearest Axis";
            case FACE -> "Block Face";

            case WORLD_NORTH -> "North";
            case WORLD_SOUTH -> "South";
            case WORLD_EAST -> "East";
            case WORLD_WEST -> "West";
        };
    }

    @Override
    public String toString() {
        return label();
    }
}