package com.zackbailey.immersivechests.client;

import com.zackbailey.immersivechests.client.records.ImmersiveResolvedTarget;
import com.zackbailey.immersivechests.client.records.ImmersiveTargetProfile;
import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientation;
import com.zackbailey.immersivechests.enums.ImmersiveTargetType;

import net.minecraft.block.BarrelBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class ImmersiveTargetResolver {

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

        TargetContext context = findTargetContext(client, screen);

        if (context == null || context.profile() == null || context.center() == null) {
            return null;
        }

        return resolveContext(client, context, playerCameraPos, playerYaw);
    }

    private static ImmersiveResolvedTarget resolveContext(
            MinecraftClient client,
            TargetContext context,
            Vec3d playerCameraPos,
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
                context.center(),
                cameraOffset,
                yaw,
                pitch
        );
    }

    private static TargetContext findTargetContext(
            MinecraftClient client,
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

    private static TargetContext findVehicleTarget(MinecraftClient client) {
        if (client.player == null || !client.player.hasVehicle()) {
            return null;
        }

        if (!(client.player.getVehicle() instanceof ChestBoatEntity chestBoat)) {
            return null;
        }

        return chestBoatContext(chestBoat);
    }

    private static TargetContext findEntityTarget(MinecraftClient client) {
        if (!(client.crosshairTarget instanceof EntityHitResult hit)) {
            return null;
        }

        var entity = hit.getEntity();

        if (entity instanceof ChestBoatEntity chestBoat) {
            return chestBoatContext(chestBoat);
        }

        if (entity instanceof ChestMinecartEntity minecart) {
            Vec3d center = new Vec3d(
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

    private static TargetContext chestBoatContext(ChestBoatEntity chestBoat) {
        Vec3d center = new Vec3d(
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

    private static TargetContext findBlockTarget(MinecraftClient client) {
        if (!(client.crosshairTarget instanceof BlockHitResult hit) || client.world == null) {
            return null;
        }

        BlockPos pos = hit.getBlockPos();
        var state = client.world.getBlockState(pos);
        Vec3d center = Vec3d.ofCenter(pos);

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
            MinecraftClient client,
            BlockPos pos,
            Vec3d center
    ) {
        var state = client.world.getBlockState(pos);
        ChestType chestType = state.get(ChestBlock.CHEST_TYPE);

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
            MinecraftClient client,
            Screen screen
    ) {
        ImmersiveTargetProfile fallbackProfile = fallbackProfile(screen);

        if (fallbackProfile == null) {
            return null;
        }

        BlockPos fallbackBlockPos = null;
        Vec3d fallbackCenter = null;

        if (client.crosshairTarget instanceof BlockHitResult hit) {
            fallbackBlockPos = hit.getBlockPos();
            fallbackCenter = Vec3d.ofCenter(fallbackBlockPos);
        } else if (client.player != null) {
            fallbackCenter = client.player.getCameraPosVec(1.0f);
        }

        return new TargetContext(
                fallbackProfile,
                fallbackBlockPos,
                fallbackCenter,
                null
        );
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
            Vec3d center,
            ChestBoatEntity chestBoat
    ) {}
}