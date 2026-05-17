package com.zackbailey.immersivechests.client;

import com.zackbailey.immersivechests.client.records.ImmersiveResolvedTarget;
import com.zackbailey.immersivechests.client.records.ImmersiveTargetContext;
import com.zackbailey.immersivechests.client.records.ImmersiveTargetProfile;
import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientation;
import com.zackbailey.immersivechests.enums.ImmersiveTargetType;

import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;

public final class ImmersiveTargetResolver {

    /**
     * Keep a memory of the last valid context only for entity targets.
     * Block targets should be re-resolved from screen + world state so an Ender Chest
     * or workstation never inherits the previous chest/barrel camera.
     */
    private static ImmersiveTargetContext lastEntityContext;

    private ImmersiveTargetResolver() {}

    public static ImmersiveResolvedTarget resolve(
            MinecraftClient client,
            Screen screen,
            Vec3d playerCameraPos,
            float playerYaw
    ) {
        if (client == null || screen == null || playerCameraPos == null) {
            return null;
        }

        ImmersiveTargetProfile screenProfile = screenProfile(screen);


        debugChat(client,
        "screen=" + screen.getClass().getName()
        + " profile=" + (screenProfile == null ? "null" : screenProfile.type()));


        if (screenProfile == null) {
            return null;
        }

        ImmersiveTargetContext context = findTargetContext(
                client,
                screen,
                screenProfile
        );

        if (context == null || context.profile() == null || context.currentCenter() == null) {
            return null;
        }

        if (context.entity() != null) {
            lastEntityContext = context;
        }

        return resolveContext(client, context, playerCameraPos, playerYaw);
    }

    private static ImmersiveTargetContext findTargetContext(
            MinecraftClient client,
            Screen screen,
            ImmersiveTargetProfile screenProfile
    ) {
        ImmersiveTargetContext vehicleContext = findVehicleTarget(client, screen);
        if (vehicleContext != null) {
            return vehicleContext;
        }

        ImmersiveTargetContext entityContext = findEntityTarget(client, screenProfile);
        if (entityContext != null) {
            return entityContext;
        }

        if (isBackpackScreen(screen)) {
            switch (ImmersiveChestsConfigScreen.backpackCameraMode) {
                case DISABLED:
                    return null;

                case CROSSHAIR:
                    break;

                case FEET:
                default:
                    return resolveBackpackTarget(client);
            }
        }

        ImmersiveTargetContext blockContext = findBlockTarget(client, screenProfile);
        if (blockContext != null) {
            return blockContext;
        }

        ImmersiveTargetContext screenAnchoredContext = findScreenAnchoredTarget(
                client,
                screenProfile
        );
        if (screenAnchoredContext != null) {
            return screenAnchoredContext;
        }

        return findSafeEntityFallback(screenProfile);
    }

    private static ImmersiveResolvedTarget resolveContext(
            MinecraftClient client,
            ImmersiveTargetContext context,
            Vec3d playerCameraPos,
            float playerYaw
    ) {
        ImmersiveTargetProfile profile = context.profile();

        Vec3d currentCenter = context.currentCenter();
        Float currentYaw = context.currentYaw();

        ImmersiveCameraOrientation resolvedOrientation =
                ImmersiveOrientationResolver.resolve(
                        client,
                        profile.orientationMode(),
                        profile.orientation(),
                        context.blockPos(),
                        currentCenter,
                        playerCameraPos
                );

        float yaw =
                ImmersiveYawResolver.resolve(
                        client,
                        profile,
                        resolvedOrientation,
                        context.blockPos(),
                        currentCenter,
                        playerCameraPos,
                        playerYaw,
                        currentYaw
                );

        Vec3d cameraOffset =
                ImmersiveOffsetResolver.resolve(
                        profile,
                        resolvedOrientation,
                        yaw
                );

        float pitch =
                ImmersivePitchResolver.resolve(
                        profile,
                        resolvedOrientation
                );

        return new ImmersiveResolvedTarget(
                profile.type(),
                context.blockPos(),
                currentCenter,
                cameraOffset,
                yaw,
                pitch,
                context.entity(),
                profile,
                resolvedOrientation
        );
    }

    /**
     * Screen decides which profiles are immersive.
     * This keeps unknown HandledScreens from accidentally inheriting old targets.
     */
    private static ImmersiveTargetProfile screenProfile(Screen screen) {
        if (screen instanceof ShulkerBoxScreen) {
            return profile(ImmersiveTargetType.SHULKER_BOX, ImmersiveChestsConfigScreen.SHULKER_BOX);
        }

        if (screen instanceof FurnaceScreen) {
            return profile(ImmersiveTargetType.FURNACE, ImmersiveChestsConfigScreen.FURNACE);
        }

        if (screen instanceof SmokerScreen) {
            return profile(ImmersiveTargetType.SMOKER, ImmersiveChestsConfigScreen.SMOKER);
        }

        if (screen instanceof BlastFurnaceScreen) {
            return profile(ImmersiveTargetType.BLAST_FURNACE, ImmersiveChestsConfigScreen.BLAST_FURNACE);
        }

        if (screen instanceof CraftingScreen) {
            return profile(ImmersiveTargetType.CRAFTING_TABLE, ImmersiveChestsConfigScreen.CRAFTING_TABLE);
        }

        if (screen instanceof CrafterScreen) {
            return profile(ImmersiveTargetType.AUTO_CRAFTER, ImmersiveChestsConfigScreen.AUTO_CRAFTER);
        }

        if (screen instanceof StonecutterScreen) {
            return profile(ImmersiveTargetType.STONECUTTER, ImmersiveChestsConfigScreen.STONECUTTER);
        }

        if (screen instanceof CartographyTableScreen) {
            return profile(ImmersiveTargetType.CARTOGRAPHY_TABLE, ImmersiveChestsConfigScreen.CARTOGRAPHY_TABLE);
        }

        if (screen instanceof SmithingScreen) {
            return profile(ImmersiveTargetType.SMITHING_TABLE, ImmersiveChestsConfigScreen.SMITHING_TABLE);
        }

        if (screen instanceof LoomScreen) {
            return profile(ImmersiveTargetType.LOOM, ImmersiveChestsConfigScreen.LOOM);
        }

        if (screen instanceof GrindstoneScreen) {
            return profile(ImmersiveTargetType.GRINDSTONE, ImmersiveChestsConfigScreen.GRINDSTONE);
        }

        if (screen instanceof AnvilScreen) {
            return profile(ImmersiveTargetType.ANVIL, ImmersiveChestsConfigScreen.ANVIL);
        }

        if (screen instanceof EnchantmentScreen) {
            return profile(ImmersiveTargetType.ENCHANTING_TABLE, ImmersiveChestsConfigScreen.ENCHANTING_TABLE);
        }

        if (screen instanceof BrewingStandScreen) {
            return profile(ImmersiveTargetType.BREWING_STAND, ImmersiveChestsConfigScreen.BREWING_STAND);
        }

        if (screen instanceof BeaconScreen) {
            return profile(ImmersiveTargetType.BEACON, ImmersiveChestsConfigScreen.BEACON);
        }

        if (screen instanceof LecternScreen) {
            return profile(ImmersiveTargetType.LECTERN_BOOK, ImmersiveChestsConfigScreen.LECTERN);
        }

        if (screen instanceof CommandBlockScreen) {
            return profile(ImmersiveTargetType.COMMAND_BLOCK, ImmersiveChestsConfigScreen.COMMAND_BLOCK);
        }

        if (screen instanceof HopperScreen) {
            return profile(ImmersiveTargetType.HOPPER, ImmersiveChestsConfigScreen.HOPPER);
        }

        if (screen instanceof Generic3x3ContainerScreen) {
            return profile(ImmersiveTargetType.DISPENSER, ImmersiveChestsConfigScreen.DISPENSER);
        }

        if (screen instanceof GenericContainerScreen) {
            return profile(ImmersiveTargetType.CHEST, ImmersiveChestsConfigScreen.CHEST);
        }

        if (screen instanceof HandledScreen<?>
                && !(screen instanceof InventoryScreen)
                && !(screen instanceof CreativeInventoryScreen)) {
            return profile(
                    ImmersiveTargetType.MODDED_CONTAINER,
                    ImmersiveChestsConfigScreen.MODDED_CONTAINER
            );
        }
        return null;
    }

    private static ImmersiveTargetContext findVehicleTarget(
            MinecraftClient client,
            Screen screen
    ) {
        if (!(screen instanceof GenericContainerScreen)) {
            return null;
        }

        if (client.player == null || !client.player.hasVehicle()) {
            return null;
        }

        if (!(client.player.getVehicle() instanceof ChestBoatEntity chestBoat)) {
            return null;
        }

        return entityTargetContext(
                chestBoat,
                ImmersiveTargetType.CHEST_BOAT,
                ImmersiveChestsConfigScreen.CHEST_BOAT
        );
    }

    private static ImmersiveTargetContext findEntityTarget(
            MinecraftClient client,
            ImmersiveTargetProfile screenProfile
    ) {
        if (!(client.crosshairTarget instanceof EntityHitResult hit)) {
            return null;
        }

        Entity entity = hit.getEntity();

        if (entity instanceof ChestBoatEntity) {
            return entityTargetContext(
                    entity,
                    ImmersiveTargetType.CHEST_BOAT,
                    ImmersiveChestsConfigScreen.CHEST_BOAT
            );
        }

        if (entity instanceof HopperMinecartEntity) {
            return entityTargetContext(
                    entity,
                    ImmersiveTargetType.HOPPER_MINECART,
                    ImmersiveChestsConfigScreen.HOPPER_MINECART
            );
        }

        if (entity instanceof ChestMinecartEntity) {
            return entityTargetContext(
                    entity,
                    ImmersiveTargetType.CHEST_MINECART,
                    ImmersiveChestsConfigScreen.CHEST_MINECART
            );
        }

        ImmersiveTargetContext decorativeContext = targetBehindDecorativeEntity(
                client,
                entity,
                screenProfile
        );

        if (decorativeContext != null) {
            return decorativeContext;
        }

        return null;
    }

    private static ImmersiveTargetContext entityTargetContext(
            Entity entity,
            ImmersiveTargetType type,
            ImmersiveChestsConfigScreen.BlockSettings settings
    ) {
        Vec3d center = new Vec3d(
                entity.getX(),
                entity.getY(),
                entity.getZ()
        );

        return new ImmersiveTargetContext(
                profile(type, settings),
                null,
                center,
                entity
        );
    }

    private static ImmersiveTargetContext findBlockTarget(
            MinecraftClient client,
            ImmersiveTargetProfile screenProfile
    ) {
        if (!(client.crosshairTarget instanceof BlockHitResult hit) || client.world == null) {
            return null;
        }

        BlockPos pos = hit.getBlockPos();

        ImmersiveTargetContext directTarget = blockTargetAt(client, pos);
        if (isCompatibleWithScreenProfile(directTarget, screenProfile)) {
            return directTarget;
        }

        ImmersiveTargetContext decorativeTarget = targetBehindDecorativeBlock(
                client,
                pos,
                screenProfile
        );
        if (decorativeTarget != null) {
            return decorativeTarget;
        }

        if (screenProfile.type() == ImmersiveTargetType.MODDED_CONTAINER) {
            ImmersiveTargetContext directModdedTarget =
                    moddedContainerTargetAt(client, pos);

            if (directModdedTarget != null) {
                return directModdedTarget;
            }
        }

        ImmersiveTargetContext nearbyTarget = nearbyTargetForScreen(
                client,
                pos,
                screenProfile
        );
        if (nearbyTarget != null) {
            return nearbyTarget;
        }

        return null;
    }

    /**
     * Last safe repair path: the screen supplies the profile, and the crosshair supplies
     * only a block position. This is intentionally not allowed for GenericContainerScreen,
     * because GenericContainerScreen needs world/block detection to distinguish chest,
     * Ender Chest, shulker-like containers, boats, minecarts, etc.
     */
    private static ImmersiveTargetContext findScreenAnchoredTarget(
            MinecraftClient client,
            ImmersiveTargetProfile screenProfile
    ) {
        if (client == null
                || client.world == null
                || screenProfile == null
                || isGenericContainerFamily(screenProfile.type())) {
            return null;
        }

        if (!(client.crosshairTarget instanceof BlockHitResult hit)) {
            return null;
        }

        BlockPos pos = hit.getBlockPos();

        return new ImmersiveTargetContext(
                screenProfile,
                pos,
                Vec3d.ofCenter(pos),
                null
        );
    }

    private static ImmersiveTargetContext blockTargetAt(
            MinecraftClient client,
            BlockPos pos
    ) {
        if (client == null || client.world == null || pos == null) {
            return null;
        }

        BlockState state = client.world.getBlockState(pos);
        Vec3d center = Vec3d.ofCenter(pos);

        if (state.getBlock() instanceof BarrelBlock) {
            return new ImmersiveTargetContext(
                    profile(ImmersiveTargetType.BARREL, ImmersiveChestsConfigScreen.BARREL),
                    pos,
                    center,
                    null
            );
        }

        if (state.getBlock() instanceof ChestBlock) {
            return chestTargetContext(client, pos, center);
        }

        String blockId = blockId(state);

        if (blockId.equals("minecraft:ender_chest")) {
            return new ImmersiveTargetContext(
                    profile(ImmersiveTargetType.ENDER_CHEST, ImmersiveChestsConfigScreen.CHEST),
                    pos,
                    center,
                    null
            );
        }

        if (blockId.endsWith("shulker_box") || blockId.contains("shulker_box")) {
            return new ImmersiveTargetContext(
                    profile(ImmersiveTargetType.SHULKER_BOX, ImmersiveChestsConfigScreen.SHULKER_BOX),
                    pos,
                    center,
                    null
            );
        }

        if (blockId.equals("minecraft:furnace")) {
            return blockContext(ImmersiveTargetType.FURNACE, ImmersiveChestsConfigScreen.FURNACE, pos, center);
        }

        if (blockId.equals("minecraft:smoker")) {
            return blockContext(ImmersiveTargetType.SMOKER, ImmersiveChestsConfigScreen.SMOKER, pos, center);
        }

        if (blockId.equals("minecraft:blast_furnace")) {
            return blockContext(ImmersiveTargetType.BLAST_FURNACE, ImmersiveChestsConfigScreen.BLAST_FURNACE, pos, center);
        }

        if (blockId.equals("minecraft:crafting_table")) {
            return blockContext(ImmersiveTargetType.CRAFTING_TABLE, ImmersiveChestsConfigScreen.CRAFTING_TABLE, pos, center);
        }

        if (blockId.equals("minecraft:crafter")) {
            return blockContext(ImmersiveTargetType.AUTO_CRAFTER, ImmersiveChestsConfigScreen.AUTO_CRAFTER, pos, center);
        }

        if (blockId.equals("minecraft:stonecutter")) {
            return blockContext(ImmersiveTargetType.STONECUTTER, ImmersiveChestsConfigScreen.STONECUTTER, pos, center);
        }

        if (blockId.equals("minecraft:cartography_table")) {
            return blockContext(ImmersiveTargetType.CARTOGRAPHY_TABLE, ImmersiveChestsConfigScreen.CARTOGRAPHY_TABLE, pos, center);
        }

        if (blockId.equals("minecraft:smithing_table")) {
            return blockContext(ImmersiveTargetType.SMITHING_TABLE, ImmersiveChestsConfigScreen.SMITHING_TABLE, pos, center);
        }

        if (blockId.equals("minecraft:loom")) {
            return blockContext(ImmersiveTargetType.LOOM, ImmersiveChestsConfigScreen.LOOM, pos, center);
        }

        if (blockId.equals("minecraft:grindstone")) {
            return blockContext(ImmersiveTargetType.GRINDSTONE, ImmersiveChestsConfigScreen.GRINDSTONE, pos, center);
        }

        if (blockId.endsWith("anvil") || blockId.equals("minecraft:chipped_anvil") || blockId.equals("minecraft:damaged_anvil")) {
            return blockContext(ImmersiveTargetType.ANVIL, ImmersiveChestsConfigScreen.ANVIL, pos, center);
        }

        if (blockId.equals("minecraft:enchanting_table")) {
            return blockContext(ImmersiveTargetType.ENCHANTING_TABLE, ImmersiveChestsConfigScreen.ENCHANTING_TABLE, pos, center);
        }

        if (blockId.equals("minecraft:brewing_stand")) {
            return blockContext(ImmersiveTargetType.BREWING_STAND, ImmersiveChestsConfigScreen.BREWING_STAND, pos, center);
        }

        if (blockId.equals("minecraft:beacon")) {
            return blockContext(ImmersiveTargetType.BEACON, ImmersiveChestsConfigScreen.BEACON, pos, center);
        }

        if (blockId.equals("minecraft:lectern")) {
            return blockContext(ImmersiveTargetType.LECTERN_BOOK, ImmersiveChestsConfigScreen.LECTERN, pos, center);
        }

        if (blockId.endsWith("command_block")) {
            return blockContext(ImmersiveTargetType.COMMAND_BLOCK, ImmersiveChestsConfigScreen.COMMAND_BLOCK, pos, center);
        }

        if (blockId.equals("minecraft:hopper")) {
            return blockContext(ImmersiveTargetType.HOPPER, ImmersiveChestsConfigScreen.HOPPER, pos, center);
        }

        if (blockId.equals("minecraft:dispenser")) {
            return blockContext(ImmersiveTargetType.DISPENSER, ImmersiveChestsConfigScreen.DISPENSER, pos, center);
        }

        if (blockId.equals("minecraft:dropper")) {
            return blockContext(ImmersiveTargetType.DROPPER, ImmersiveChestsConfigScreen.DROPPER, pos, center);
        }

        return null;
    }

    private static ImmersiveTargetContext moddedContainerTargetAt(
            MinecraftClient client,
            BlockPos pos
    ) {
        if (client == null || client.world == null || pos == null) {
            return null;
        }

        if (client.world.getBlockEntity(pos) == null) {
            return null;
        }

        BlockState state = client.world.getBlockState(pos);

        debugChat(client,
                "modded check pos=" + pos
                + " block=" + blockId(state)
                + " blockEntity=" + client.world.getBlockEntity(pos));

        return blockContext(
                ImmersiveTargetType.MODDED_CONTAINER,
                ImmersiveChestsConfigScreen.MODDED_CONTAINER,
                pos,
                Vec3d.ofCenter(pos)
        );
    }

    private static ImmersiveTargetContext blockContext(
            ImmersiveTargetType type,
            ImmersiveChestsConfigScreen.BlockSettings settings,
            BlockPos pos,
            Vec3d center
    ) {
        return new ImmersiveTargetContext(
                profile(type, settings),
                pos,
                center,
                null
        );
    }

    private static ImmersiveTargetContext chestTargetContext(
            MinecraftClient client,
            BlockPos pos,
            Vec3d center
    ) {
        BlockState state = client.world.getBlockState(pos);
        ChestType chestType = state.get(ChestBlock.CHEST_TYPE);

        if (chestType == ChestType.LEFT) {
            return new ImmersiveTargetContext(
                    profile(ImmersiveTargetType.DOUBLE_CHEST_LEFT, ImmersiveChestsConfigScreen.DOUBLE_CHEST_LEFT),
                    pos,
                    center,
                    null
            );
        }

        if (chestType == ChestType.RIGHT) {
            return new ImmersiveTargetContext(
                    profile(ImmersiveTargetType.DOUBLE_CHEST_RIGHT, ImmersiveChestsConfigScreen.DOUBLE_CHEST_RIGHT),
                    pos,
                    center,
                    null
            );
        }

        return new ImmersiveTargetContext(
                profile(ImmersiveTargetType.CHEST, ImmersiveChestsConfigScreen.CHEST),
                pos,
                center,
                null
        );
    }

    private static ImmersiveTargetContext targetBehindDecorativeBlock(
            MinecraftClient client,
            BlockPos pos,
            ImmersiveTargetProfile screenProfile
    ) {
        if (client.world == null || pos == null) {
            return null;
        }

        BlockState state = client.world.getBlockState(pos);

        if (!isDecorativeProxyBlock(state)) {
            return null;
        }

        Direction facing = decorativeFacing(state);

        if (facing != null) {
            ImmersiveTargetContext target = blockTargetAt(client, pos.offset(facing.getOpposite()));

            if (isCompatibleWithScreenProfile(target, screenProfile)) {
                return target;
            }
        }

        return nearbyTargetForScreen(client, pos, screenProfile);
    }

    private static ImmersiveTargetContext targetBehindDecorativeEntity(
            MinecraftClient client,
            Entity entity,
            ImmersiveTargetProfile screenProfile
    ) {
        if (client == null || client.world == null || entity == null) {
            return null;
        }

        String entityId = Registries.ENTITY_TYPE.getId(entity.getType()).toString();

        if (!entityId.contains("item_frame")
                && !entityId.contains("painting")
                && !entityId.contains("display")) {
            return null;
        }

        return nearbyTargetForScreen(client, entity.getBlockPos(), screenProfile);
    }

    private static ImmersiveTargetContext nearbyTargetForScreen(
            MinecraftClient client,
            BlockPos origin,
            ImmersiveTargetProfile screenProfile
    ) {
        if (client == null || client.world == null || origin == null || screenProfile == null) {
            return null;
        }

        ImmersiveTargetContext originTarget = blockTargetAt(client, origin);
        if (isCompatibleWithScreenProfile(originTarget, screenProfile)) {
            return originTarget;
        }

        if (screenProfile.type() == ImmersiveTargetType.MODDED_CONTAINER) {
            ImmersiveTargetContext originModdedTarget =
                    moddedContainerTargetAt(client, origin);

            if (originModdedTarget != null) {
                return originModdedTarget;
            }

            // Important:
            // Do not scan adjacent blocks for MODDED_CONTAINER by default.
            // Adjacent inventories/fireplaces/stoves can steal the camera anchor.
            return null;
        }

        for (Direction direction : Direction.values()) {
            BlockPos nearbyPos = origin.offset(direction);

            ImmersiveTargetContext target = blockTargetAt(client, nearbyPos);

            if (isCompatibleWithScreenProfile(target, screenProfile)) {
                return target;
            }
        }

        for (Direction first : Direction.Type.HORIZONTAL) {
            for (Direction second : Direction.Type.HORIZONTAL) {
                BlockPos nearbyPos = origin.offset(first).offset(second);

                ImmersiveTargetContext target = blockTargetAt(client, nearbyPos);

                if (isCompatibleWithScreenProfile(target, screenProfile)) {
                    return target;
                }
            }
        }

        return null;
    }

    private static boolean isDispenserDropperFamily(ImmersiveTargetType type) {
        return type == ImmersiveTargetType.DISPENSER
                || type == ImmersiveTargetType.DROPPER;
    }

    private static boolean isCompatibleWithScreenProfile(
            ImmersiveTargetContext context,
            ImmersiveTargetProfile screenProfile
    ) {
        if (context == null || context.profile() == null || screenProfile == null) {
            return false;
        }

        ImmersiveTargetType found = context.profile().type();
        ImmersiveTargetType expected = screenProfile.type();

        if (found == expected) {
            return true;
        }

        if (isGenericContainerFamily(expected)) {
            return isGenericContainerFamily(found);
        }

        if (isDispenserDropperFamily(expected)) {
            return isDispenserDropperFamily(found);
        }

        if (expected == ImmersiveTargetType.FURNACE) {
            return found == ImmersiveTargetType.FURNACE;
        }

        return false;
    }

    private static boolean isGenericContainerFamily(ImmersiveTargetType type) {
        return type == ImmersiveTargetType.CHEST
                || type == ImmersiveTargetType.DOUBLE_CHEST_LEFT
                || type == ImmersiveTargetType.DOUBLE_CHEST_RIGHT
                || type == ImmersiveTargetType.TRAPPED_CHEST
                || type == ImmersiveTargetType.COPPER_CHEST
                || type == ImmersiveTargetType.ENDER_CHEST
                || type == ImmersiveTargetType.BARREL
                || type == ImmersiveTargetType.SHULKER_BOX
                || type == ImmersiveTargetType.CHEST_BOAT
                || type == ImmersiveTargetType.CHEST_MINECART
                || type == ImmersiveTargetType.HOPPER
                || type == ImmersiveTargetType.HOPPER_MINECART
                || type == ImmersiveTargetType.DISPENSER
                || type == ImmersiveTargetType.DROPPER;
    }


    private static ImmersiveTargetContext findSafeEntityFallback(
            ImmersiveTargetProfile screenProfile
    ) {
        if (screenProfile == null || isGenericContainerFamily(screenProfile.type())) {
            return null;
        }

        if (lastEntityContext == null
                || lastEntityContext.profile() == null
                || lastEntityContext.entity() == null) {
            return null;
        }

        if (!isCompatibleWithScreenProfile(lastEntityContext, screenProfile)) {
            return null;
        }

        return lastEntityContext;
    }

    private static boolean isDecorativeProxyBlock(BlockState state) {
        if (state == null) {
            return false;
        }

        String blockId = blockId(state);

        return blockId.contains("item_frame")
                || blockId.contains("frame")
                || blockId.contains("painting")
                || blockId.contains("display");
    }

    private static Direction decorativeFacing(BlockState state) {
        if (state == null) {
            return null;
        }

        if (state.contains(Properties.HORIZONTAL_FACING)) {
            return state.get(Properties.HORIZONTAL_FACING);
        }

        if (state.contains(Properties.FACING)) {
            return state.get(Properties.FACING);
        }

        return null;
    }

    private static String blockId(BlockState state) {
        return Registries.BLOCK.getId(state.getBlock()).toString();
    }

    private static ImmersiveTargetProfile profile(
            ImmersiveTargetType type,
            ImmersiveChestsConfigScreen.BlockSettings settings
    ) {
        return new ImmersiveTargetProfile(
                type,

                settings.offsetX,
                settings.offsetY,
                settings.offsetZ,

                settings.hozOffsetX,
                settings.hozOffsetY,
                settings.hozOffsetZ,

                settings.tilt,
                settings.flipYaw,

                settings.orientationMode,
                settings.orientation,
                settings.yawMode
        );
    }

    private static void debugChat(MinecraftClient client, String message) {
        if (!ImmersiveChestsConfigScreen.debugLogging
                || client == null
                || client.player == null) {
            return;
        }

        client.player.sendMessage(
                Text.literal("[ImmersiveChests] " + message),
                false
        );
    }

    private static boolean isBackpackScreen(Screen screen) {
        if (screen == null || ImmersiveChestsConfigScreen.backpackCameraMode == null) return false;

        String cls = screen.getClass().getName().toLowerCase();
        String title = screen.getTitle().getString().toLowerCase();

        return cls.contains("backpack")
                || title.contains("backpack")
                || cls.contains("travelersbackpack")
                || cls.contains("sophisticatedbackpacks");
    }

    private static ImmersiveTargetContext resolveBackpackTarget(
            MinecraftClient client
    ) {
        if (client == null || client.player == null) {
            return null;
        }

        BlockPos feetPos = client.player.getBlockPos();
        Vec3d feetCenter = Vec3d.ofCenter(feetPos).add(0.0, -0.35, 0.0);

        return new ImmersiveTargetContext(
                profile(ImmersiveTargetType.MODDED_CONTAINER, ImmersiveChestsConfigScreen.BACKPACK),
                feetPos,
                feetCenter,
                null
        );
    }
}
