package com.sanchous98.eldritchhorror.client.model;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.TrainRenderState;
import com.sanchous98.eldritchhorror.entity.TrainSeats;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * The carriage and locomotive geometry, built from {@code ModelPart} cubes at 16 units per block.
 *
 * <p>Both variants share the same construction: a wooden floor on a riveted underframe with two
 * bogies, framed side walls split into window bays, a roof with an overhanging eaves board, a door
 * at each car end and twelve chairs in two rows facing the windows. The locomotive is four blocks
 * longer, so its nose has a boiler, funnel, steam dome, headlamp, cowcatcher and cylinder blocks;
 * its cab starts at {@code z = -68} and carries all twelve chairs.
 *
 * <p>Window openings are real holes between pillars and wall bands; the thin pane cube in each
 * hole samples a mostly transparent region of the atlas, so the player looks through the glass
 * into an empty interior instead of at a textured wall. The glass is drawn with
 * {@code entityCutout}: fully transparent texels are discarded, a few pale reflection streaks
 * remain. There is no collision inside the car (the entity is {@code noPhysics}) — nothing can
 * block a seated player's view.
 *
 * <p>The model is authored in pixels (1 block = 16) and is not scaled by the renderer, so the
 * exterior is exactly 3 blocks wide and 3.8 tall; {@link #TEXTURE} is a 256×256 atlas generated
 * by {@code tools/generate_train_textures.py}. The UV layout is documented in
 * {@code docs/TRANSPORT-VEHICLES.md} — change both together.
 */
public final class TrainCarModel extends Model<TrainRenderState> {
    /** Baked layer of the passenger wagon. */
    public static final ModelLayerLocation WAGON_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "train_wagon"), "main");
    /** Baked layer of the locomotive. */
    public static final ModelLayerLocation LOCOMOTIVE_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "train_locomotive"), "main");

    /** Wagon atlas. */
    public static final Identifier WAGON_TEXTURE = Identifier.fromNamespaceAndPath(
            EldritchHorror.MODID, "textures/entity/train_wagon.png");
    /** Locomotive atlas (same layout, darker boiler palette). */
    public static final Identifier LOCOMOTIVE_TEXTURE = Identifier.fromNamespaceAndPath(
            EldritchHorror.MODID, "textures/entity/train_locomotive.png");

    // --- atlas regions (u, v). See docs/TRANSPORT-VEHICLES.md for the full map. --------------
    private static final int FLOOR_U = 0;
    private static final int FLOOR_V = 0;
    private static final int GLASS_U = 116;
    private static final int GLASS_V = 0;
    private static final int METAL_U = 140;
    private static final int METAL_V = 180;
    private static final int FRAME_U = 0;
    private static final int FRAME_V = 40;
    private static final int WALL_U = 72;
    private static final int WALL_V = 56;
    private static final int PILLAR_U = 142;
    private static final int PILLAR_V = 56;
    private static final int DOOR_U = 166;
    private static final int DOOR_V = 56;
    private static final int WHEEL_U = 230;
    private static final int WHEEL_V = 56;
    private static final int UPPER_U = 0;
    private static final int UPPER_V = 82;
    private static final int BENCH_U = 76;
    private static final int BENCH_V = 180;
    private static final int ROOF_U = 136;
    private static final int ROOF_V = 110;
    private static final int BRASS_U = 0;
    private static final int BRASS_V = 124;
    private static final int NOSE_U = 44;
    private static final int NOSE_V = 124;
    private static final int LANTERN_U = 128;
    private static final int LANTERN_V = 148;
    private static final int ENDWALL_U = 0;
    private static final int ENDWALL_V = 174;
    private static final int GLOW_U = 44;
    private static final int GLOW_V = 174;

    // --- body sections (pixels; 16 = one block) ----------------------------------------------
    private static final int WAGON_HALF = 112;
    private static final int LOCOMOTIVE_HALF = 128;
    /** One window bay is 28 px (1.75 blocks) long. */
    private static final int BAY = 28;
    private static final float WIDTH = 24.0F;
    /** Floor / underframe heights. */
    private static final float FLOOR_BOTTOM = 6.0F;
    private static final float WALL_BOTTOM = 9.0F;
    private static final float WINDOW_BOTTOM = 23.0F;
    private static final float WINDOW_TOP = 48.0F;
    private static final float WALL_TOP = 54.0F;
    private static final float ROOF_TOP = 59.0F;
    /** Locomotive cab starts here; everything ahead of it is the engine nose. */
    private static final int CAB_FRONT = -68;

    /**
     * @param root the baked layer ({@link #WAGON_LAYER} or {@link #LOCOMOTIVE_LAYER})
     */
    public TrainCarModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
    }

    /** @return the wagon layer, baked by the client layer-definition event. */
    public static LayerDefinition createWagonLayer() {
        return create(false);
    }

    /** @return the locomotive layer, baked by the client layer-definition event. */
    public static LayerDefinition createLocomotiveLayer() {
        return create(true);
    }

    private static LayerDefinition create(boolean locomotive) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        int half = locomotive ? LOCOMOTIVE_HALF : WAGON_HALF;
        addRunningGear(root, half, locomotive);
        if (locomotive) {
            addWalls(root, CAB_FRONT, half);
            addRoof(root, CAB_FRONT, half);
            addDoor(root, half);
            addCabFront(root);
            addNose(root);
            addSeats(root, TrainSeats.locomotiveRows());
        } else {
            addWalls(root, -half, half);
            addRoof(root, -half, half);
            addDoor(root, -half);
            addDoor(root, half);
            addSeats(root, TrainSeats.wagonRows());
        }
        return LayerDefinition.create(mesh, 256, 256);
    }

    // --- running gear -------------------------------------------------------------------------

    private static void addRunningGear(PartDefinition root, int half, boolean locomotive) {
        CubeListBuilder floor = CubeListBuilder.create().texOffs(FLOOR_U, FLOOR_V);
        CubeListBuilder frames = CubeListBuilder.create().texOffs(FRAME_U, FRAME_V);
        int segment = locomotive ? 32 : BAY;
        for (int z = -half; z < half; z += segment) {
            floor.addBox(-WIDTH, FLOOR_BOTTOM, z, 48, 3, segment);
            frames.addBox(-WIDTH, 3.0F, z, 4, 5, segment);
            frames.addBox(WIDTH - 4.0F, 3.0F, z, 4, 5, segment);
        }
        root.addOrReplaceChild("floor", floor, PartPose.ZERO);
        root.addOrReplaceChild("solebars", frames, PartPose.ZERO);

        CubeListBuilder bogies = CubeListBuilder.create().texOffs(METAL_U, METAL_V);
        CubeListBuilder wheels = CubeListBuilder.create().texOffs(WHEEL_U, WHEEL_V);
        int bogie = locomotive ? 88 : 72;
        for (int z : new int[]{-bogie, bogie}) {
            bogies.addBox(-20.0F, 1.0F, z - 15.0F, 40, 6, 30);
            for (int offset : new int[]{-10, 10}) {
                wheels.addBox(-10.0F, -2.0F, z + offset - 5.0F, 4, 10, 10);
                wheels.addBox(6.0F, -2.0F, z + offset - 5.0F, 4, 10, 10);
            }
        }
        root.addOrReplaceChild("bogies", bogies, PartPose.ZERO);
        root.addOrReplaceChild("wheels", wheels, PartPose.ZERO);

        CubeListBuilder buffers = CubeListBuilder.create().texOffs(METAL_U, METAL_V);
        buffers.addBox(-WIDTH, 3.0F, -half, 48, 5, 4);
        buffers.addBox(-WIDTH, 3.0F, half - 4.0F, 48, 5, 4);
        CubeListBuilder couplers = CubeListBuilder.create().texOffs(BRASS_U, BRASS_V);
        couplers.addBox(-3.0F, 7.0F, -half - 3.0F, 6, 3, 4);
        couplers.addBox(-3.0F, 7.0F, half - 1.0F, 6, 3, 4);
        root.addOrReplaceChild("buffers", buffers, PartPose.ZERO);
        root.addOrReplaceChild("couplers", couplers, PartPose.ZERO);
    }

    // --- body shell ---------------------------------------------------------------------------

    private static void addWalls(PartDefinition root, int zFrom, int zTo) {
        CubeListBuilder lower = CubeListBuilder.create().texOffs(WALL_U, WALL_V);
        CubeListBuilder upper = CubeListBuilder.create().texOffs(UPPER_U, UPPER_V);
        CubeListBuilder pillars = CubeListBuilder.create().texOffs(PILLAR_U, PILLAR_V);
        CubeListBuilder glass = CubeListBuilder.create().texOffs(GLASS_U, GLASS_V);
        CubeListBuilder mullions = CubeListBuilder.create().texOffs(BRASS_U, BRASS_V);
        int bays = (zTo - zFrom) / BAY;
        for (int i = 0; i < bays; i++) {
            int z = zFrom + i * BAY;
            // Wall bands below and above the window opening.
            lower.addBox(-WIDTH, WALL_BOTTOM, z, 2, 14, BAY);
            lower.addBox(WIDTH - 2.0F, WALL_BOTTOM, z, 2, 14, BAY);
            upper.addBox(-WIDTH, WINDOW_TOP, z, 2, 6, BAY);
            upper.addBox(WIDTH - 2.0F, WINDOW_TOP, z, 2, 6, BAY);
            // One pane per bay, inset into the wall thickness.
            glass.addBox(-WIDTH, WINDOW_BOTTOM, z + 3.0F, 1, 25, BAY - 6);
            glass.addBox(WIDTH - 1.0F, WINDOW_BOTTOM, z + 3.0F, 1, 25, BAY - 6);
            // A brass centre mullion and a horizontal transom read as glazing bars.
            for (int zBar : new int[]{z + BAY / 2, z + 3}) {
                mullions.addBox(-WIDTH, WINDOW_BOTTOM, zBar, 1, 25, 1);
                mullions.addBox(WIDTH - 1.0F, WINDOW_BOTTOM, zBar, 1, 25, 1);
            }
            mullions.addBox(-WIDTH, 35.0F, z + 3.0F, 1, 1, BAY - 6);
            mullions.addBox(WIDTH - 1.0F, 35.0F, z + 3.0F, 1, 1, BAY - 6);
        }
        for (int i = 0; i <= bays; i++) {
            // Corner posts stay inside the shell; interior posts straddle a bay boundary.
            float z = Math.max(zFrom, Math.min(zTo - 6.0F, zFrom + i * (float) BAY - 3.0F));
            pillars.addBox(-WIDTH, 22.0F, z, 3, 27, 6);
            pillars.addBox(WIDTH - 3.0F, 22.0F, z, 3, 27, 6);
        }
        root.addOrReplaceChild("lower_walls", lower, PartPose.ZERO);
        root.addOrReplaceChild("upper_walls", upper, PartPose.ZERO);
        root.addOrReplaceChild("pillars", pillars, PartPose.ZERO);
        root.addOrReplaceChild("glass", glass, PartPose.ZERO);
        root.addOrReplaceChild("mullions", mullions, PartPose.ZERO);
    }

    private static void addRoof(PartDefinition root, int zFrom, int zTo) {
        CubeListBuilder roof = CubeListBuilder.create().texOffs(ROOF_U, ROOF_V);
        CubeListBuilder ridge = CubeListBuilder.create().texOffs(ROOF_U, ROOF_V);
        int bays = (zTo - zFrom) / BAY;
        for (int i = 0; i < bays; i++) {
            int z = zFrom + i * BAY;
            roof.addBox(-26.0F, WALL_TOP, z, 52, 5, BAY);
            ridge.addBox(-20.0F, ROOF_TOP, z, 40, 3, BAY);
        }
        root.addOrReplaceChild("roof", roof, PartPose.ZERO);
        root.addOrReplaceChild("ridge", ridge, PartPose.ZERO);
    }

    /**
     * A car end: two wall panels, a lintel and a closed door with a small window. The wall is
     * three pixels thick and the door leaf sits just inside it, so the door reads from outside
     * without filling the geometry thickness.
     */
    private static void addDoor(PartDefinition root, int zOuter) {
        boolean rear = zOuter > 0;
        float zWall = rear ? zOuter - 3.0F : zOuter;
        CubeListBuilder panels = CubeListBuilder.create().texOffs(ENDWALL_U, ENDWALL_V);
        panels.addBox(-WIDTH, 9.0F, zWall, 15, 45, 3);
        panels.addBox(9.0F, 9.0F, zWall, 15, 45, 3);
        panels.addBox(-9.0F, 42.0F, zWall, 18, 12, 3);
        CubeListBuilder leaf = CubeListBuilder.create().texOffs(DOOR_U, DOOR_V);
        float zLeaf = zWall + 1.0F;
        leaf.addBox(-8.0F, 9.0F, zLeaf, 16, 30, 1);
        CubeListBuilder glass = CubeListBuilder.create().texOffs(GLASS_U, GLASS_V);
        // The pane sits on the outer face of the leaf, so the door window reads from the platform.
        float zGlass = rear ? zWall + 2.0F : zWall;
        glass.addBox(-5.0F, 27.0F, zGlass, 10, 11, 1);
        CubeListBuilder handle = CubeListBuilder.create().texOffs(BRASS_U, BRASS_V);
        handle.addBox(5.0F, 21.0F, zGlass, 2, 3, 1);
        String suffix = rear ? "rear" : "front";
        root.addOrReplaceChild("door_wall_" + suffix, panels, PartPose.ZERO);
        root.addOrReplaceChild("door_leaf_" + suffix, leaf, PartPose.ZERO);
        root.addOrReplaceChild("door_glass_" + suffix, glass, PartPose.ZERO);
        root.addOrReplaceChild("door_handle_" + suffix, handle, PartPose.ZERO);
    }

    /** The cab's forward bulkhead, with a windscreen looking over the boiler. */
    private static void addCabFront(PartDefinition root) {
        CubeListBuilder panels = CubeListBuilder.create().texOffs(ENDWALL_U, ENDWALL_V);
        panels.addBox(-WIDTH, 9.0F, CAB_FRONT, 15, 45, 3);
        panels.addBox(9.0F, 9.0F, CAB_FRONT, 15, 45, 3);
        panels.addBox(-9.0F, 9.0F, CAB_FRONT, 18, 15, 3);
        panels.addBox(-9.0F, 46.0F, CAB_FRONT, 18, 8, 3);
        CubeListBuilder glass = CubeListBuilder.create().texOffs(GLASS_U, GLASS_V);
        glass.addBox(-9.0F, 24.0F, CAB_FRONT + 1.0F, 18, 22, 1);
        CubeListBuilder mullion = CubeListBuilder.create().texOffs(BRASS_U, BRASS_V);
        mullion.addBox(-0.5F, 24.0F, CAB_FRONT + 1.5F, 1, 22, 1);
        root.addOrReplaceChild("cab_front", panels, PartPose.ZERO);
        root.addOrReplaceChild("cab_windscreen", glass, PartPose.ZERO);
        root.addOrReplaceChild("cab_windscreen_bar", mullion, PartPose.ZERO);
    }

    // --- locomotive nose ----------------------------------------------------------------------

    private static void addNose(PartDefinition root) {
        CubeListBuilder deck = CubeListBuilder.create().texOffs(FLOOR_U, FLOOR_V);
        for (int z = -LOCOMOTIVE_HALF; z < CAB_FRONT; z += 20) {
            deck.addBox(-WIDTH, FLOOR_BOTTOM, z, 48, 3, 20);
        }
        CubeListBuilder boiler = CubeListBuilder.create().texOffs(NOSE_U, NOSE_V);
        boiler.addBox(-14.0F, 22.0F, -122.0F, 28, 18, 24);
        boiler.addBox(-14.0F, 22.0F, -98.0F, 28, 18, 24);
        CubeListBuilder metal = CubeListBuilder.create().texOffs(METAL_U, METAL_V);
        metal.addBox(-16.0F, 20.0F, -124.0F, 32, 26, 2);
        metal.addBox(-10.0F, 26.0F, -125.0F, 20, 14, 1);
        metal.addBox(-22.0F, 10.0F, -112.0F, 4, 14, 24);
        metal.addBox(18.0F, 10.0F, -112.0F, 4, 14, 24);
        metal.addBox(-7.0F, 40.0F, -114.0F, 14, 2, 12);
        metal.addBox(-5.0F, 42.0F, -112.0F, 10, 12, 10);
        CubeListBuilder brass = CubeListBuilder.create().texOffs(BRASS_U, BRASS_V);
        for (int z : new int[]{-116, -100, -84}) {
            brass.addBox(-15.0F, 23.0F, z, 30, 2, 3);
        }
        brass.addBox(-7.0F, 54.0F, -114.0F, 14, 2, 14);
        brass.addBox(-6.0F, 40.0F, -92.0F, 12, 8, 14);
        for (int z = -120; z < -76; z += 11) {
            brass.addBox(-16.0F, 36.0F, z, 1, 1, 11);
            brass.addBox(15.0F, 36.0F, z, 1, 1, 11);
        }
        CubeListBuilder lamp = CubeListBuilder.create().texOffs(LANTERN_U, LANTERN_V);
        lamp.addBox(-5.0F, 31.0F, -128.0F, 10, 10, 8);
        CubeListBuilder glow = CubeListBuilder.create().texOffs(GLOW_U, GLOW_V);
        glow.addBox(-4.0F, 32.0F, -129.0F, 8, 8, 1);

        root.addOrReplaceChild("nose_deck", deck, PartPose.ZERO);
        root.addOrReplaceChild("boiler", boiler, PartPose.ZERO);
        root.addOrReplaceChild("nose_metal", metal, PartPose.ZERO);
        root.addOrReplaceChild("nose_brass", brass, PartPose.ZERO);
        root.addOrReplaceChild("headlamp", lamp, PartPose.ZERO);
        root.addOrReplaceChild("headlamp_glow", glow, PartPose.ZERO);

        CubeListBuilder plough = CubeListBuilder.create().texOffs(METAL_U, METAL_V);
        plough.addBox(-20.0F, -1.0F, -6.0F, 40, 3, 14);
        root.addOrReplaceChild("cowcatcher", plough,
                PartPose.offsetAndRotation(0.0F, 7.0F, -LOCOMOTIVE_HALF + 2.0F, 0.55F, 0.0F, 0.0F));
    }

    // --- seating ------------------------------------------------------------------------------

    private static void addSeats(PartDefinition root, double[] rows) {
        CubeListBuilder pedestals = CubeListBuilder.create().texOffs(FRAME_U, FRAME_V);
        CubeListBuilder pads = CubeListBuilder.create().texOffs(BENCH_U, BENCH_V);
        CubeListBuilder backs = CubeListBuilder.create().texOffs(BENCH_U, BENCH_V);
        for (double row : rows) {
            float z = (float) row * 16.0F - 11.0F;
            // Right-hand row, looking out of the +X window.
            pedestals.addBox(12.0F, 9.0F, z + 2.0F, 10, 6, 18);
            pads.addBox(10.0F, 15.0F, z, 13, 3, 22);
            backs.addBox(8.0F, 15.0F, z, 2, 13, 22);
            // Left-hand row, mirrored.
            pedestals.addBox(-22.0F, 9.0F, z + 2.0F, 10, 6, 18);
            pads.addBox(-23.0F, 15.0F, z, 13, 3, 22);
            backs.addBox(-10.0F, 15.0F, z, 2, 13, 22);
        }
        root.addOrReplaceChild("seat_pedestals", pedestals, PartPose.ZERO);
        root.addOrReplaceChild("seat_pads", pads, PartPose.ZERO);
        root.addOrReplaceChild("seat_backs", backs, PartPose.ZERO);
    }
}
