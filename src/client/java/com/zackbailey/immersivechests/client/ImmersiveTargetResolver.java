package com.zackbailey.immersivechests.client;

import com.zackbailey.immersivechests.client.records.ImmersiveResolvedTarget;
import com.zackbailey.immersivechests.client.records.ImmersiveTargetProfile;
import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientation;
import com.zackbailey.immersivechests.enums.ImmersiveTargetType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CrafterScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.inventory.GrindstoneScreen;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.client.gui.screens.inventory.SmokerScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class ImmersiveTargetResolver {

    private ImmersiveTargetResolver() {}

    public static ImmersiveResolvedTarget resolve(
            Minecraft client,
            Screen screen,
            Vec3 playerCameraPos,
            float playerYaw
    ) {
        if (client == null || screen == null || playerCameraPos == null) {
            return null;
        }

        TargetContext context = findTargetContext(client, screen);

        if (context == null || context.profile() == null || context.center() == null) {
            return null;
        }

        return resolveContext(client, context, playerCameraPos, playerYaw);
    }

    private static ImmersiveResolvedTarget resolveContext(
            Minecraft client,
            TargetContext context,
            Vec3 playerCameraPos,
            float playerYaw
    ) {
        ImmersiveTargetProfile profile = context.profile();

        if (profile.type() == ImmersiveTargetType.CHEST_BOAT && context.chestBoat() != null) {
            return ImmersiveBoatTargetResolver.resolve(
                    context.chestBoat(),
                    profile
            );
        }

        ImmersiveCameraOrientation resolvedOrientation =
                ImmersiveOrientationResolver.resolve(
                        client,
                        profile.orientationMode(),
                        profile.orientation(),
                        context.blockPos(),
                        context.center(),
                        playerCameraPos
                );

        float yaw =
                ImmersiveYawResolver.resolve(
                        client,
                        profile,
                        resolvedOrientation,
                        context.blockPos(),
                        context.center(),
                        playerCameraPos,
                        playerYaw
                );

        Vec3 cameraOffset =
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
                context.center(),
                cameraOffset,
                yaw,
                pitch
        );
    }

    private static TargetContext findTargetContext(
            Minecraft client,
            Screen screen
    ) {
        TargetContext entityContext = findEntityTarget(client);
        if (entityContext != null) {
            return entityContext;
        }

        TargetContext blockContext = findBlockTarget(client);
        if (blockContext != null) {
            return blockContext;
        }

        TargetContext vehicleContext = findVehicleTarget(client);
        if (vehicleContext != null) {
            return vehicleContext;
        }

        return findFallbackTarget(client, screen);
    }

    private static TargetContext findVehicleTarget(Minecraft client) {
        if (client.player == null || !client.player.isPassenger()) {
            return null;
        }

        if (!(client.player.getVehicle() instanceof ChestBoat chestBoat)) {
            return null;
        }

        return chestBoatContext(chestBoat);
    }

    private static TargetContext findEntityTarget(Minecraft client) {
        if (!(client.hitResult instanceof EntityHitResult hit)) {
            return null;
        }

        var entity = hit.getEntity();

        if (entity instanceof ChestBoat chestBoat) {
            return chestBoatContext(chestBoat);
        }

        if (entity instanceof MinecartChest minecart) {
            Vec3 center = new Vec3(
                    minecart.getX(),
                    minecart.getY(),
                    minecart.getZ()
            );

            return new TargetContext(
                    profile(ImmersiveTargetType.CHEST_MINECART, ImmersiveChestsConfigScreen.CHEST_MINECART),
                    null,
                    center,
                    null
            );
        }

        return null;
    }

    private static TargetContext chestBoatContext(ChestBoat chestBoat) {
        Vec3 center = new Vec3(
                chestBoat.getX(),
                chestBoat.getY(),
                chestBoat.getZ()
        );

        return new TargetContext(
                profile(ImmersiveTargetType.CHEST_BOAT, ImmersiveChestsConfigScreen.CHEST_BOAT),
                null,
                center,
                chestBoat
        );
    }

    private static TargetContext findBlockTarget(Minecraft client) {
        if (!(client.hitResult instanceof BlockHitResult hit) || client.level == null) {
            return null;
        }

        BlockPos pos = hit.getBlockPos();
        var state = client.level.getBlockState(pos);
        Vec3 center = Vec3.atCenterOf(pos);

        if (state.getBlock() instanceof BarrelBlock) {
            return new TargetContext(
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

    private static TargetContext chestTargetContext(
            Minecraft client,
            BlockPos pos,
            Vec3 center
    ) {
        var state = client.level.getBlockState(pos);
        ChestType chestType = state.getValue(ChestBlock.TYPE);

        boolean stacked =
                ImmersiveChestsConfigScreen.stackedChestSupport
                        && ImmersiveOrientationResolver.isBlockedAbove(client, pos);

        if (stacked) {
            return new TargetContext(
                    profile(ImmersiveTargetType.CHEST, ImmersiveChestsConfigScreen.CHEST),
                    pos,
                    center,
                    null
            );
        }

        if (chestType == ChestType.LEFT) {
            return new TargetContext(
                    profile(ImmersiveTargetType.DOUBLE_CHEST_LEFT, ImmersiveChestsConfigScreen.DOUBLE_CHEST_LEFT),
                    pos,
                    center,
                    null
            );
        }

        if (chestType == ChestType.RIGHT) {
            return new TargetContext(
                    profile(ImmersiveTargetType.DOUBLE_CHEST_RIGHT, ImmersiveChestsConfigScreen.DOUBLE_CHEST_RIGHT),
                    pos,
                    center,
                    null
            );
        }

        return new TargetContext(
                profile(ImmersiveTargetType.CHEST, ImmersiveChestsConfigScreen.CHEST),
                pos,
                center,
                null
        );
    }

    private static TargetContext findFallbackTarget(
            Minecraft client,
            Screen screen
    ) {
        ImmersiveTargetProfile fallbackProfile = fallbackProfile(screen);

        if (fallbackProfile == null) {
            return null;
        }

        BlockPos fallbackBlockPos = null;
        Vec3 fallbackCenter = null;

        if (client.hitResult instanceof BlockHitResult hit) {
            fallbackBlockPos = hit.getBlockPos();
            fallbackCenter = Vec3.atCenterOf(fallbackBlockPos);
        } else if (client.player != null) {
            fallbackCenter = client.player.getEyePosition(1.0f);
        }

        return new TargetContext(
                fallbackProfile,
                fallbackBlockPos,
                fallbackCenter,
                null
        );
    }

    private static ImmersiveTargetProfile fallbackProfile(Screen screen) {
        if (screen instanceof ContainerScreen) {
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

    private record TargetContext(
            ImmersiveTargetProfile profile,
            BlockPos blockPos,
            Vec3 center,
            ChestBoat chestBoat
    ) {}
}