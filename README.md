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
