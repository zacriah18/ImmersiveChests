package com.zackbailey.immersivechests.client;

import com.zackbailey.immersivechests.client.records.ImmersiveResolvedTarget;
import com.zackbailey.immersivechests.client.records.ImmersiveTargetContext;
import com.zackbailey.immersivechests.client.records.ImmersiveTargetProfile;
import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientation;
import com.zackbailey.immersivechests.enums.ImmersiveTargetType;

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

public final class ImmersiveTargetResolver {

    private static ImmersiveTargetContext lastResolvedContext;

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

        if (!isValidImmersiveScreen(screen)) {
            return null;
        }

        ImmersiveTargetContext context = findTargetContext(client, screen);

        if (context == null || context.profile() == null || context.currentCenter() == null) {
            return null;
        }

        lastResolvedContext = context;

        return resolveContext(client, context, playerCameraPos, playerYaw);
    }

    private static boolean isValidImmersiveScreen(Screen screen) {
        return isKnownImmersiveScreen(screen)
                || isCompatibleHandledScreen(screen);
    }

    private static boolean isKnownImmersiveScreen(Screen screen) {
        return screen instanceof LecternScreen
                || screen instanceof CommandBlockScreen;
    }

    private static boolean isCompatibleHandledScreen(Screen screen) {
        if (!(screen instanceof HandledScreen<?>)) {
            return false;
        }

        if (screen instanceof InventoryScreen
                || screen instanceof CreativeInventoryScreen) {
            return false;
        }

        return true;
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

    private static ImmersiveTargetContext findTargetContext(
            MinecraftClient client,
            Screen screen
    ) {
        ImmersiveTargetContext blockContext = findBlockTarget(client);
        if (blockContext != null) {
            return blockContext;
        }

        ImmersiveTargetContext entityContext = findEntityTarget(client);
        if (entityContext != null) {
            return entityContext;
        }

        ImmersiveTargetContext vehicleContext = findVehicleTarget(client, screen);
        if (vehicleContext != null) {
            return vehicleContext;
        }

        return findFallbackTarget(screen);
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

    private static ImmersiveTargetContext findEntityTarget(MinecraftClient client) {
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

        if (entity instanceof ChestMinecartEntity) {
            return entityTargetContext(
                    entity,
                    ImmersiveTargetType.CHEST_MINECART,
                    ImmersiveChestsConfigScreen.CHEST_MINECART
            );
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

    private static ImmersiveTargetContext findBlockTarget(MinecraftClient client) {
        if (!(client.crosshairTarget instanceof BlockHitResult hit) || client.world == null) {
            return null;
        }

        BlockPos pos = hit.getBlockPos();

        ImmersiveTargetContext directTarget = blockTargetAt(client, pos);
        if (directTarget != null) {
            return directTarget;
        }

        return targetBehindDecorativeBlock(client, pos);
    }

    private static ImmersiveTargetContext blockTargetAt(
            MinecraftClient client,
            BlockPos pos
    ) {
        if (client.world == null || pos == null) {
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

        return null;
    }

    private static ImmersiveTargetContext targetBehindDecorativeBlock(
            MinecraftClient client,
            BlockPos pos
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
            ImmersiveTargetContext target =
                    blockTargetAt(client, pos.offset(facing.getOpposite()));

            if (target != null) {
                return target;
            }
        }

        for (Direction direction : Direction.values()) {
            ImmersiveTargetContext target =
                    blockTargetAt(client, pos.offset(direction));

            if (target != null) {
                return target;
            }
        }

        return null;
    }

    private static boolean isDecorativeProxyBlock(BlockState state) {
        if (state == null) {
            return false;
        }

        String blockId = Registries.BLOCK
                .getId(state.getBlock())
                .toString();

        return blockId.contains("item_frame")
                || blockId.contains("frame")
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

    private static ImmersiveTargetContext findFallbackTarget(Screen screen) {
        if (lastResolvedContext == null || lastResolvedContext.profile() == null) {
            return null;
        }

        if (!isLastContextCompatibleWithScreen(lastResolvedContext, screen)) {
            return null;
        }

        return lastResolvedContext;
    }

    private static boolean isLastContextCompatibleWithScreen(
            ImmersiveTargetContext context,
            Screen screen
    ) {
        ImmersiveTargetType type = context.profile().type();

        if (screen instanceof GenericContainerScreen) {
            return type == ImmersiveTargetType.CHEST
                    || type == ImmersiveTargetType.DOUBLE_CHEST_LEFT
                    || type == ImmersiveTargetType.DOUBLE_CHEST_RIGHT
                    || type == ImmersiveTargetType.TRAPPED_CHEST
                    || type == ImmersiveTargetType.COPPER_CHEST
                    || type == ImmersiveTargetType.ENDER_CHEST
                    || type == ImmersiveTargetType.CHEST_BOAT
                    || type == ImmersiveTargetType.CHEST_MINECART;
        }

        ImmersiveTargetProfile fallbackProfile = fallbackProfile(screen);

        return fallbackProfile != null
                && fallbackProfile.type() == type;
    }

    private static ImmersiveTargetProfile fallbackProfile(Screen screen) {
        if (screen instanceof GenericContainerScreen) {
            return profile(ImmersiveTargetType.CHEST, ImmersiveChestsConfigScreen.CHEST);
        }

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

        return null;
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
}