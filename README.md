# Immersive Chests

Immersive Chests is a Minecraft client-side mod that dynamically repositions the camera when interacting with blocks and containers, creating a more immersive and contextual viewing experience.

---

## ✨ Features

- 📦 Dynamic camera positioning for containers and blocks
- 🧭 Smart orientation system (top, side, block-facing, nearest axis)
- 🎯 Accurate yaw alignment based on interaction context
- 🧱 Supports a wide range of blocks:
  - Chests (single + double)
  - Barrels
  - Furnaces, smokers, blast furnaces
  - Crafting tables, looms, smithing tables, cartography tables
  - Brewing stands, beacons, anvils, lecterns
  - Minecarts and chest boats
- 🔁 Handles edge cases:
  - Stacked chests
  - Block obstruction (air prioritization)
  - Barrel facing overrides
- ⚙️ Fully configurable camera offsets, tilt, and behaviour

---

## 🧠 Core Architecture

The mod uses a structured resolver pipeline to determine camera placement:

### 🔑 Key Design Principles

- **OrientationResolver is the single source of truth for camera placement**
- **YawResolver never re-resolves orientation — it only interprets it**
- **Offsets are orientation-relative, not player-relative**
- **Pitch is purely derived from orientation**

---

## 🔄 Resolver Responsibilities

| Resolver | Role |
|--------|------|
| OrientationResolver | Converts world + block + config → final orientation |
| YawResolver | Converts orientation → yaw frame |
| OffsetResolver | Applies positional offsets |
| PitchResolver | Applies tilt and vertical rotation |

---

## ⚙️ Configuration

Each block type has a configurable profile:

```java
new BlockSettings(
    offsetX, offsetY, offsetZ,
    hozOffsetX, hozOffsetY, hozOffsetZ,
    tilt,
    flipYaw,
    orientationMode,
    orientation,
    yawMode
)
