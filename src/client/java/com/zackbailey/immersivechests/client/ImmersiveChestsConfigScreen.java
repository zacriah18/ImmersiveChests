package com.zackbailey.immersivechests.client;

import java.util.function.Consumer;

import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientation;
import com.zackbailey.immersivechests.enums.ImmersiveCameraOrientationMode;
import com.zackbailey.immersivechests.enums.ImmersiveYawMode;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ImmersiveChestsConfigScreen {

        // --- GENERAL CAMERA ---
        public static boolean enabled = true;
        public static double animationSpeed = 0.10;
        public static boolean debugLogging = false;
        public static double distanceSpeedScalar = 0.20;
        public static boolean instantAnimate = false;

        public static final boolean defaultenabled = enabled;
        public static final double defaultanimationSpeed = animationSpeed;
        public static final boolean defaultdebugLogging = debugLogging;
        public static final double defaultdistanceSpeedScalar = distanceSpeedScalar;
        public static final boolean defaultinstantAnimate = instantAnimate;

        // --- SPECIAL ---
        public static double boatDistanceBehind = 0.5;
        public static boolean alwaysBarrelFace = true;
        public static boolean stackedChestSupport = true;
        public static boolean prioritizeAirBlock = true;
        
        public static final double defaultboatDistanceBehind = boatDistanceBehind;
        public static final boolean defaultalwaysBarrelFace = alwaysBarrelFace;
        public static final boolean defaultstackedChestSupport = stackedChestSupport;
        public static final boolean defaultprioritizeAirBlock = prioritizeAirBlock;
        
        public static class BlockSettings {
                public double offsetX;
                public double offsetY;
                public double offsetZ;

                public final double defaultOffsetX;
                public final double defaultOffsetY;
                public final double defaultOffsetZ;

                public double hozOffsetX;
                public double hozOffsetY;
                public double hozOffsetZ;

                public final double defaultHozOffsetX;
                public final double defaultHozOffsetY;
                public final double defaultHozOffsetZ;

                public double tilt;
                public final double defaultTilt;

                public boolean flipYaw;
                public final boolean defaultFlipYaw;

                public ImmersiveCameraOrientationMode orientationMode;
                public final ImmersiveCameraOrientationMode defaultOrientationMode;

                public ImmersiveCameraOrientation orientation;
                public final ImmersiveCameraOrientation defaultOrientation;

                public ImmersiveYawMode yawMode;
                public final ImmersiveYawMode defaultYawMode;

                public BlockSettings(
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
                ) {
                this.offsetX = offsetX;
                this.offsetY = offsetY;
                this.offsetZ = offsetZ;

                this.defaultOffsetX = offsetX;
                this.defaultOffsetY = offsetY;
                this.defaultOffsetZ = offsetZ;

                this.hozOffsetX = hozOffsetX;
                this.hozOffsetY = hozOffsetY;
                this.hozOffsetZ = hozOffsetZ;

                this.defaultHozOffsetX = hozOffsetX;
                this.defaultHozOffsetY = hozOffsetY;
                this.defaultHozOffsetZ = hozOffsetZ;

                this.tilt = tilt;
                this.defaultTilt = tilt;

                this.flipYaw = flipYaw;
                this.defaultFlipYaw = flipYaw;

                this.orientationMode = orientationMode;
                this.defaultOrientationMode = orientationMode;

                this.orientation = orientation;
                this.defaultOrientation = orientation;

                this.yawMode = yawMode;
                this.defaultYawMode = yawMode;
                }
        }

        // Offset meaning:
        // X = local left/right
        // Y = vertical up/down
        // Z = local forward/back / distance along selected orientation
        public static final BlockSettings CHEST = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.FIXED,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.FACE);

        public static final BlockSettings BARREL = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.FIXED,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.AXIS);
        public static final BlockSettings DOUBLE_CHEST_LEFT = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.FIXED,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.FACE);

        public static final BlockSettings DOUBLE_CHEST_RIGHT = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.FIXED,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.FACE);

        public static final BlockSettings ENDER_CHEST = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.FIXED,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.FACE);

        public static final BlockSettings CRAFTING_TABLE = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.FIXED,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.AXIS);
        
                        
        public static final BlockSettings AUTO_CRAFTER = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.FIXED,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.AXIS);

        public static final BlockSettings STONECUTTER = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.BLOCK_FACE,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.FACE);

        public static final BlockSettings CARTOGRAPHY_TABLE = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.FIXED,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.AXIS);

        public static final BlockSettings SMITHING_TABLE = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.FIXED,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.AXIS);

        public static final BlockSettings LOOM = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.FIXED,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.AXIS);

        public static final BlockSettings GRINDSTONE = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.FIXED,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.AXIS);

        public static final BlockSettings FURNACE = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1.5,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.BLOCK_FACE,
                        ImmersiveCameraOrientation.NORTH,
                        ImmersiveYawMode.FACE);

        public static final BlockSettings SMOKER = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, -1.5,
                        0.0,
                        true,
                        ImmersiveCameraOrientationMode.BLOCK_FACE,
                        ImmersiveCameraOrientation.NORTH,
                        ImmersiveYawMode.FACE);

        public static final BlockSettings BLAST_FURNACE = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, -1.5,
                        0.0,
                        true,
                        ImmersiveCameraOrientationMode.BLOCK_FACE,
                        ImmersiveCameraOrientation.NORTH,
                        ImmersiveYawMode.FACE);

        public static final BlockSettings ANVIL = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.FIXED,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.AXIS);

        public static final BlockSettings ENCHANTING_TABLE = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.FIXED,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.PLAYER_RAW);

        public static final BlockSettings BREWING_STAND = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.NEAREST_AXIS,
                        ImmersiveCameraOrientation.NORTH,
                        ImmersiveYawMode.AXIS);

        public static final BlockSettings BEACON = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.FIXED,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.AXIS);

        public static final BlockSettings LECTERN = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.FIXED,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.FACE);

        public static final BlockSettings CHEST_MINECART = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.FIXED,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.FACE);

        public static final BlockSettings CHEST_BOAT = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.FIXED,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.FACE);

        public static final BlockSettings SHULKER_BOX = new BlockSettings(
                        0.0, 1, 0.0,
                        0.0, 0.0, 1,
                        0.0,
                        false,
                        ImmersiveCameraOrientationMode.NEAREST_AXIS,
                        ImmersiveCameraOrientation.TOP,
                        ImmersiveYawMode.FACE);

        public static Screen create(Screen parent) {
                ConfigBuilder builder = ConfigBuilder.create()
                                .setParentScreen(parent)
                                .setTitle(Text.literal("Immersive Chests"));

                ConfigEntryBuilder entryBuilder = builder.entryBuilder();

                addCameraCategory(builder, entryBuilder);
                addSpecialCategory(builder, entryBuilder);

                addBlockCategory(builder, entryBuilder, "Chest", CHEST);
                addBlockCategory(builder, entryBuilder, "Double Chest Left", DOUBLE_CHEST_LEFT);
                addBlockCategory(builder, entryBuilder, "Double Chest Right", DOUBLE_CHEST_RIGHT);
                addBlockCategory(builder, entryBuilder, "Barrel", BARREL);
                addBlockCategory(builder, entryBuilder, "Ender Chest", ENDER_CHEST);
                addBlockCategory(builder, entryBuilder, "Crafting Table", CRAFTING_TABLE);
                addBlockCategory(builder, entryBuilder, "Stonecutter", STONECUTTER);
                addBlockCategory(builder, entryBuilder, "Cartography Table", CARTOGRAPHY_TABLE);
                addBlockCategory(builder, entryBuilder, "Smithing Table", SMITHING_TABLE);
                addBlockCategory(builder, entryBuilder, "Loom", LOOM);
                addBlockCategory(builder, entryBuilder, "Grindstone", GRINDSTONE);
                addBlockCategory(builder, entryBuilder, "Furnace", FURNACE);
                addBlockCategory(builder, entryBuilder, "Smoker", SMOKER);
                addBlockCategory(builder, entryBuilder, "Blast Furnace", BLAST_FURNACE);
                addBlockCategory(builder, entryBuilder, "Anvil", ANVIL);
                addBlockCategory(builder, entryBuilder, "Enchanting Table", ENCHANTING_TABLE);
                addBlockCategory(builder, entryBuilder, "Brewing Stand", BREWING_STAND);
                addBlockCategory(builder, entryBuilder, "Beacon", BEACON);
                addBlockCategory(builder, entryBuilder, "Lectern", LECTERN);
                addBlockCategory(builder, entryBuilder, "Chest Minecart", CHEST_MINECART);
                addBlockCategory(builder, entryBuilder, "Chest Boat", CHEST_BOAT);
                addBlockCategory(builder, entryBuilder, "Shulker Box", SHULKER_BOX);

                return builder.build();
        }

        private static void addCameraCategory(ConfigBuilder builder, ConfigEntryBuilder entryBuilder) {
                ConfigCategory camera = builder.getOrCreateCategory(Text.literal("Camera"));

                camera.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enable Immersive Camera"), enabled)
                                .setDefaultValue(defaultenabled)
                                .setSaveConsumer(v -> enabled = v)
                                .build());

                addDouble(camera, entryBuilder, "Animation Speed", animationSpeed, defaultanimationSpeed,
                                v -> animationSpeed = v);
                addDouble(camera, entryBuilder, "Distance Smoothing Strength", distanceSpeedScalar,
                                defaultdistanceSpeedScalar, v -> distanceSpeedScalar = v);

                camera.addEntry(entryBuilder.startBooleanToggle(Text.literal("Instant Animate"), instantAnimate)
                                .setDefaultValue(defaultinstantAnimate)
                                .setSaveConsumer(v -> instantAnimate = v)
                                .build());

                camera.addEntry(entryBuilder.startBooleanToggle(Text.literal("Debug Logging"), debugLogging)
                                .setDefaultValue(defaultdebugLogging)
                                .setSaveConsumer(v -> debugLogging = v)
                                .build());
        }

        private static void addSpecialCategory(ConfigBuilder builder, ConfigEntryBuilder entryBuilder) {
                ConfigCategory special = builder.getOrCreateCategory(Text.literal("Special"));

                addDouble(special, entryBuilder, "Boat Distance Behind", boatDistanceBehind, defaultboatDistanceBehind,
                                v -> boatDistanceBehind = v);

                special.addEntry(entryBuilder
                                .startBooleanToggle(Text.literal("Always Orientate Barrel Face"), alwaysBarrelFace)
                                .setDefaultValue(defaultalwaysBarrelFace)
                                .setSaveConsumer(v -> alwaysBarrelFace = v)
                                .build());

                special.addEntry(entryBuilder.startBooleanToggle(Text.literal("Stacked Chest Support"), stackedChestSupport)
                                .setDefaultValue(defaultstackedChestSupport)
                                .setSaveConsumer(v -> stackedChestSupport = v)
                                .build());

                special.addEntry(entryBuilder.startBooleanToggle(Text.literal("Prioritize Air Block"), prioritizeAirBlock)
                                .setDefaultValue(defaultprioritizeAirBlock)
                                .setSaveConsumer(v -> prioritizeAirBlock = v)
                                .build());
        }

        private static void addBlockCategory(
                        ConfigBuilder builder,
                        ConfigEntryBuilder entryBuilder,
                        String name,
                        BlockSettings settings) {
                ConfigCategory category = builder.getOrCreateCategory(Text.literal(name));

                addDouble(category, entryBuilder, "Offset X / Left-Right", settings.offsetX, settings.defaultOffsetX,
                                v -> settings.offsetX = v);
                addDouble(category, entryBuilder, "Offset Y / Up-Down", settings.offsetY, settings.defaultOffsetY,
                                v -> settings.offsetY = v);
                addDouble(category, entryBuilder, "Offset Z / Forward-Back", settings.offsetZ, settings.defaultOffsetZ,
                                v -> settings.offsetZ = v);

                addDouble(category, entryBuilder, "Side Offset X / Left-Right", settings.hozOffsetX, settings.defaultHozOffsetX, 
                                v -> settings.hozOffsetX = v);   
                addDouble(category, entryBuilder, "Side Offset Y / Up-Down", settings.hozOffsetY, settings.defaultHozOffsetY, 
                                v -> settings.hozOffsetY = v);
                addDouble(category, entryBuilder, "Side Offset Z / Forward-Back", settings.hozOffsetZ, settings.defaultHozOffsetZ, 
                                v -> settings.hozOffsetZ = v);
                
                category.addEntry(entryBuilder.startDoubleField(Text.literal("POV Tilt"), settings.tilt)
                                .setDefaultValue(settings.defaultTilt)
                                .setMin(-90.0)
                                .setMax(90.0)
                                .setSaveConsumer(v -> settings.tilt = v)
                                .build());
                
                category.addEntry(entryBuilder
                                .startBooleanToggle(Text.literal("Flip Yaw"), settings.flipYaw)
                                .setDefaultValue(settings.defaultFlipYaw)
                                .setSaveConsumer(v -> settings.flipYaw = v)
                                .build());
                
                category.addEntry(entryBuilder
                        .startEnumSelector(
                                Text.literal("Orientation Mode"),
                                ImmersiveCameraOrientationMode.class,
                                settings.orientationMode
                        )
                        .setDefaultValue(settings.defaultOrientationMode)
                        .setEnumNameProvider(mode ->
                                Text.translatable("immersivechests.orientation_mode." + mode.name().toLowerCase())
                        )
                        .setSaveConsumer(v -> settings.orientationMode = v)
                        .build());
                
                        category.addEntry(entryBuilder
                        .startEnumSelector(
                                Text.literal("Fixed Orientation"),
                                ImmersiveCameraOrientation.class,
                                settings.orientation
                        )
                        .setDefaultValue(settings.defaultOrientation)
                        .setTooltip(Text.literal("Only used when Orientation Mode is Fixed."))
                        .setEnumNameProvider(orientation ->
                                Text.translatable("immersivechests.orientation." + orientation.name().toLowerCase())
                        )
                        .setSaveConsumer(v -> settings.orientation = v)
                        .build());

                category.addEntry(entryBuilder
                                .startEnumSelector(Text.literal("Yaw Mode"), ImmersiveYawMode.class, settings.yawMode)
                                .setDefaultValue(settings.defaultYawMode)
                                .setEnumNameProvider(mode ->
                                        Text.translatable("immersivechests.yaw." + mode.name().toLowerCase())
                                )
                                .setSaveConsumer(v -> settings.yawMode = v)
                                .build());
        }

        private static void addDouble(
                        ConfigCategory category,
                        ConfigEntryBuilder entryBuilder,
                        String label,
                        double value,
                        double defaultValue,
                        Consumer<Double> saveConsumer) {
                category.addEntry(entryBuilder.startDoubleField(Text.literal(label), value)
                                .setDefaultValue(defaultValue)
                                .setMin(-10.0)
                                .setMax(10.0)
                                .setSaveConsumer(saveConsumer)
                                .build());
        }
}