package com.sanchous98.eldritchhorror.client.model;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

/**
 * The coastal packet's hull, deck and superstructure, built from {@code ModelPart} cubes.
 *
 * <p>Model space follows the vanilla boat convention used by the renderer: one unit is 1/16 block,
 * {@code +X} points at the bow and {@code +Y} points <em>down</em>. The renderer's
 * {@code scale(-1, -1, 1)} plus quarter turn puts the bow on the entity's forward axis and the
 * deck right way up. The entity origin is the physics origin: the deck top is modelled at
 * {@code y = -12} (0.75 blocks above it) and the renderer applies the buoyancy lift that keeps
 * that deck about 1.3 blocks above the sea.
 *
 * <p>Dimensions: 19.5 blocks overall including the bowsprit (17.75 of hull), 5.5 blocks on the
 * beam, a 5-block-wide walkable deck, a 4.25-block cabin with windows and a railed poop deck, a
 * mast with two yards and square sails, a funnel and six lanterns on a separate layer so they can
 * be drawn full-bright.
 *
 * <p>Seat geometry lives in {@code CoastalPacket}: the two long benches along the bulwarks and
 * the two inside the cabin line up with its attachment points, so a rider's eyeline sits about
 * 1.2 blocks over the deck and can see the sea past the cap rail.
 */
public final class CoastalPacketModel extends EntityModel<CoastalPacketRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "coastal_packet"), "main");
    /** Lanterns are baked separately so the renderer can draw them with the eyes render type. */
    public static final ModelLayerLocation LANTERN_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "coastal_packet"), "lanterns");

    /** Texture regions painted by {@code tools/generate_ship_texture.py}. */
    private static final int HULL_U = 0;
    private static final int HULL_V = 0;
    private static final int HULL_ALT_V = 132;
    private static final int DECK_U = 0;
    private static final int DECK_V = 264;
    private static final int ROOF_U = 0;
    private static final int ROOF_V = 352;
    private static final int SAIL_U = 0;
    private static final int SAIL_V = 440;
    private static final int CABIN_U = 256;
    private static final int CABIN_V = 0;
    private static final int WINDOW_U = 256;
    private static final int WINDOW_V = 48;
    private static final int WINDOW_TALL_U = 290;
    private static final int WINDOW_TALL_V = 48;
    private static final int BENCH_U = 400;
    private static final int BENCH_V = 160;
    private static final int KEEL_U = 256;
    private static final int KEEL_V = 80;
    private static final int MAST_U = 440;
    private static final int MAST_V = 0;
    private static final int YARD_U = 440;
    private static final int YARD_V = 104;
    private static final int FUNNEL_U = 256;
    private static final int FUNNEL_V = 160;
    private static final int TRIM_U = 340;
    private static final int TRIM_V = 176;
    private static final int FOREDECK_U = 256;
    private static final int FOREDECK_V = 240;
    private static final int LANTERN_U = 460;
    private static final int LANTERN_V = 140;
    private static final int PORTHOLE_U = 490;
    private static final int PORTHOLE_V = 140;

    /** Half length of the straight hull, in model units (7 blocks). */
    private static final float BOW = 112.0F;
    private static final float STERN = -112.0F;
    /** Outer and inner half beam (2.75 and 2.5 blocks). */
    private static final float HALF_BEAM = 44.0F;
    private static final float INNER_BEAM = 40.0F;
    /** Deck top and hull top, in model units above the origin. */
    private static final float DECK_Y = -12.0F;
    private static final float HULL_TOP = -13.0F;
    private static final float HULL_BOTTOM = 26.0F;
    private static final int HULL_SEGMENTS = 14;
    private static final float SEGMENT = (BOW - STERN) / HULL_SEGMENTS;

    public CoastalPacketModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        buildHull(root);
        buildDeck(root);
        buildRails(root);
        buildCabin(root);
        buildForecastle(root);
        buildMast(root);
        buildFunnel(root);
        buildBenches(root);
        buildDetails(root);
        return LayerDefinition.create(mesh, 1024, 1024);
    }

    /** @return a lantern-only layer for the full-bright pass */
    public static LayerDefinition createLanternLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition lanterns = root.addOrReplaceChild("lanterns", CubeListBuilder.create(),
                PartPose.ZERO);
        buildLanterns(lanterns);
        return LayerDefinition.create(mesh, 1024, 1024);
    }

    private static PartDefinition cube(PartDefinition parent, String name, int u, int v,
                                       float x, float y, float z, float w, float h, float d) {
        return parent.addOrReplaceChild(name,
                CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, w, h, d),
                PartPose.ZERO);
    }

    private static void buildHull(PartDefinition root) {
        PartDefinition hull = root.addOrReplaceChild("hull", CubeListBuilder.create(),
                PartPose.ZERO);
        float height = HULL_BOTTOM - HULL_TOP;
        for (int i = 0; i < HULL_SEGMENTS; i++) {
            float x = STERN + i * SEGMENT;
            cube(hull, "plank_" + i, HULL_U, i % 2 == 0 ? HULL_V : HULL_ALT_V,
                    x, HULL_TOP, -HALF_BEAM, SEGMENT, height, HALF_BEAM * 2.0F);
        }
        // Stepped bow and stern counters, narrowing toward the tips.
        cube(hull, "bow_1", HULL_U, HULL_V, 112.0F, HULL_TOP, -30.0F, 16.0F, height, 60.0F);
        cube(hull, "bow_2", HULL_U, HULL_ALT_V, 128.0F, HULL_TOP, -20.0F, 12.0F, height, 40.0F);
        cube(hull, "bow_3", HULL_U, HULL_V, 140.0F, HULL_TOP, -8.0F, 8.0F, height, 16.0F);
        cube(hull, "stern_1", HULL_U, HULL_ALT_V, -128.0F, HULL_TOP, -30.0F, 16.0F, height, 60.0F);
        cube(hull, "stern_2", HULL_U, HULL_V, -136.0F, HULL_TOP, -20.0F, 8.0F, height, 40.0F);
        // Keel and rudder keep the profile from ending in a flat slab under water.
        for (int i = 0; i < 12; i++) {
            cube(hull, "keel_" + i, KEEL_U, KEEL_V, STERN + i * 20.0F, 26.0F, -32.0F,
                    20.0F, 8.0F, 64.0F);
        }
        cube(hull, "keel_bow", KEEL_U, KEEL_V, 120.0F, 26.0F, -14.0F, 20.0F, 8.0F, 28.0F);
        cube(hull, "keel_stern", KEEL_U, KEEL_V, -132.0F, 26.0F, -14.0F, 20.0F, 8.0F, 28.0F);
        cube(hull, "rudder", TRIM_U, TRIM_V, -140.0F, 4.0F, -4.0F, 6.0F, 20.0F, 8.0F);
        // Painted sheer strake along the topsides.
        for (int i = 0; i < HULL_SEGMENTS; i++) {
            float x = STERN + i * SEGMENT;
            cube(hull, "strake_" + i, TRIM_U, TRIM_V, x, -13.0F, HALF_BEAM, SEGMENT, 4.0F, 1.0F);
            cube(hull, "strake_l_" + i, TRIM_U, TRIM_V, x, -13.0F, -HALF_BEAM - 1.0F,
                    SEGMENT, 4.0F, 1.0F);
        }
        // Portholes along the cabin's stretch of hull, below the sheer strake.
        for (int i = 0; i < 3; i++) {
            float x = -96.0F + i * 20.0F;
            cube(hull, "porthole_r_" + i, PORTHOLE_U, PORTHOLE_V, x, -8.0F,
                    HALF_BEAM, 7.0F, 7.0F, 1.0F);
            cube(hull, "porthole_l_" + i, PORTHOLE_U, PORTHOLE_V, x, -8.0F,
                    -HALF_BEAM - 1.0F, 7.0F, 7.0F, 1.0F);
        }
    }

    private static void buildDeck(PartDefinition root) {
        PartDefinition deck = root.addOrReplaceChild("deck", CubeListBuilder.create(),
                PartPose.ZERO);
        for (int i = 0; i < HULL_SEGMENTS; i++) {
            float x = STERN + i * SEGMENT;
            cube(deck, "plank_" + i, DECK_U, DECK_V, x, DECK_Y - 1.0F, -INNER_BEAM,
                    SEGMENT, 1.0F, INNER_BEAM * 2.0F);
        }
        cube(deck, "stern_deck", DECK_U, DECK_V, -128.0F, DECK_Y - 1.0F, -36.0F,
                16.0F, 1.0F, 72.0F);
        // Two steps up to the forecastle.
        cube(deck, "step_low", DECK_U, DECK_V, 44.0F, -14.0F, -36.0F, 8.0F, 2.0F, 72.0F);
        cube(deck, "step_high", DECK_U, DECK_V, 48.0F, -17.0F, -36.0F, 8.0F, 5.0F, 72.0F);
    }

    private static void buildRails(PartDefinition root) {
        PartDefinition rails = root.addOrReplaceChild("rails", CubeListBuilder.create(),
                PartPose.ZERO);
        for (int i = 0; i < HULL_SEGMENTS; i++) {
            float x = STERN + i * SEGMENT;
            // Bulwark: a solid waist-high wall above the deck edge.
            cube(rails, "bulwark_r_" + i, HULL_U, HULL_V, x, -19.0F,
                    INNER_BEAM, SEGMENT, 6.0F, 4.0F);
            cube(rails, "bulwark_l_" + i, HULL_U, HULL_ALT_V, x, -19.0F,
                    -HALF_BEAM, SEGMENT, 6.0F, 4.0F);
            // Cap rail on posts, the part a passenger sees the sea past.
            cube(rails, "cap_r_" + i, TRIM_U, TRIM_V, x, -26.0F,
                    INNER_BEAM - 0.5F, SEGMENT, 2.0F, 5.0F);
            cube(rails, "cap_l_" + i, TRIM_U, TRIM_V, x, -26.0F,
                    -HALF_BEAM - 0.5F, SEGMENT, 2.0F, 5.0F);
        }
        for (int i = 0; i <= HULL_SEGMENTS; i++) {
            float x = STERN + i * SEGMENT;
            cube(rails, "post_r_" + i, TRIM_U, TRIM_V, x, -24.0F, 41.0F,
                    2.0F, 5.0F, 2.0F);
            cube(rails, "post_l_" + i, TRIM_U, TRIM_V, x, -24.0F, -43.0F,
                    2.0F, 5.0F, 2.0F);
        }
    }

    private static void buildCabin(PartDefinition root) {
        PartDefinition cabin = root.addOrReplaceChild("cabin", CubeListBuilder.create(),
                PartPose.ZERO);
        // Side walls: two lower panels, a window band, two upper panels per side.
        for (int i = 0; i < 2; i++) {
            float x = -108.0F + i * 34.0F;
            cube(cabin, "wall_low_r_" + i, CABIN_U, CABIN_V, x, -26.0F, 32.0F,
                    34.0F, 14.0F, 4.0F);
            cube(cabin, "wall_low_l_" + i, CABIN_U, CABIN_V, x, -26.0F, -36.0F,
                    34.0F, 14.0F, 4.0F);
            cube(cabin, "wall_high_r_" + i, CABIN_U, CABIN_V, x, -40.0F, 32.0F,
                    34.0F, 4.0F, 4.0F);
            cube(cabin, "wall_high_l_" + i, CABIN_U, CABIN_V, x, -40.0F, -36.0F,
                    34.0F, 4.0F, 4.0F);
        }
        // Front and rear bulkheads, split along the beam so the UV stays on the painted boards.
        for (int i = 0; i < 4; i++) {
            float z = -36.0F + i * 18.0F;
            cube(cabin, "front_low_" + i, CABIN_U, CABIN_V, -44.0F, -26.0F, z,
                    4.0F, 14.0F, 18.0F);
            cube(cabin, "front_high_" + i, CABIN_U, CABIN_V, -44.0F, -40.0F, z,
                    4.0F, 4.0F, 18.0F);
            cube(cabin, "rear_low_" + i, CABIN_U, CABIN_V, -108.0F, -26.0F, z,
                    4.0F, 14.0F, 18.0F);
            cube(cabin, "rear_high_" + i, CABIN_U, CABIN_V, -108.0F, -40.0F, z,
                    4.0F, 4.0F, 18.0F);
        }
        // Side windows, set into the lower/upper gap, four a side.
        for (int i = 0; i < 4; i++) {
            float x = -104.0F + i * 16.0F;
            cube(cabin, "window_r_" + i, WINDOW_U, WINDOW_V, x, -34.0F, 35.0F,
                    14.0F, 7.0F, 1.0F);
            cube(cabin, "window_l_" + i, WINDOW_U, WINDOW_V, x, -34.0F, -36.0F,
                    14.0F, 7.0F, 1.0F);
        }
        // Front and rear windows plus a cabin door.
        cube(cabin, "window_front_r", WINDOW_TALL_U, WINDOW_TALL_V, -44.5F, -34.0F, 7.0F,
                1.0F, 7.0F, 14.0F);
        cube(cabin, "window_front_l", WINDOW_TALL_U, WINDOW_TALL_V, -44.5F, -34.0F, -21.0F,
                1.0F, 7.0F, 14.0F);
        cube(cabin, "window_rear", WINDOW_TALL_U, WINDOW_TALL_V, -108.5F, -34.0F, -7.0F,
                1.0F, 7.0F, 14.0F);
        cube(cabin, "door", TRIM_U, TRIM_V, -44.5F, -32.0F, -5.0F,
                1.0F, 20.0F, 10.0F);
        // Roof: the walkable poop deck over the cabin.
        for (int i = 0; i < 4; i++) {
            cube(cabin, "roof_" + i, ROOF_U, ROOF_V, -112.0F + i * 19.0F, -44.0F,
                    -40.0F, 19.0F, 4.0F, 80.0F);
        }
        // A low rail around the poop deck, so the roof reads as a deck, not a lid.
        for (int i = 0; i < 4; i++) {
            float x = -112.0F + i * 19.0F;
            cube(cabin, "poop_cap_r_" + i, TRIM_U, TRIM_V, x, -52.0F, 37.0F,
                    19.0F, 2.0F, 3.0F);
            cube(cabin, "poop_cap_l_" + i, TRIM_U, TRIM_V, x, -52.0F, -40.0F,
                    19.0F, 2.0F, 3.0F);
        }
        for (int i = 0; i < 4; i++) {
            float x = -110.0F + i * 24.0F;
            cube(cabin, "poop_post_r_" + i, TRIM_U, TRIM_V, x, -50.0F, 37.0F,
                    2.0F, 6.0F, 2.0F);
            cube(cabin, "poop_post_l_" + i, TRIM_U, TRIM_V, x, -50.0F, -39.0F,
                    2.0F, 6.0F, 2.0F);
        }
        for (int i = 0; i < 4; i++) {
            cube(cabin, "poop_cap_stern_" + i, TRIM_U, TRIM_V, -112.0F, -52.0F,
                    -40.0F + i * 20.0F, 4.0F, 2.0F, 20.0F);
        }
        // Cabin fascia band under the roof, hiding the wall/roof seam.
        for (int i = 0; i < 4; i++) {
            float x = -112.0F + i * 19.0F;
            cube(cabin, "fascia_r_" + i, TRIM_U, TRIM_V, x, -42.0F, 40.0F,
                    19.0F, 2.0F, 1.0F);
            cube(cabin, "fascia_l_" + i, TRIM_U, TRIM_V, x, -42.0F, -41.0F,
                    19.0F, 2.0F, 1.0F);
        }
    }

    private static void buildForecastle(PartDefinition root) {
        PartDefinition fore = root.addOrReplaceChild("forecastle", CubeListBuilder.create(),
                PartPose.ZERO);
        for (int i = 0; i < 4; i++) {
            cube(fore, "deck_" + i, FOREDECK_U, FOREDECK_V, 56.0F + i * 14.0F, -20.0F,
                    -36.0F, 14.0F, 8.0F, 72.0F);
        }
        // Rail around the raised bow deck.
        for (int i = 0; i < 3; i++) {
            cube(fore, "rail_front_" + i, TRIM_U, TRIM_V, 108.0F, -34.0F,
                    -36.0F + i * 24.0F, 4.0F, 2.0F, 24.0F);
        }
        for (int i = 0; i < 4; i++) {
            float x = 60.0F + i * 16.0F;
            cube(fore, "rail_cap_r_" + i, TRIM_U, TRIM_V, x, -34.0F, 36.0F,
                    16.0F, 2.0F, 3.0F);
            cube(fore, "rail_cap_l_" + i, TRIM_U, TRIM_V, x, -34.0F, -39.0F,
                    16.0F, 2.0F, 3.0F);
            cube(fore, "rail_post_r_" + i, TRIM_U, TRIM_V, x, -32.0F, 36.0F,
                    2.0F, 12.0F, 2.0F);
            cube(fore, "rail_post_l_" + i, TRIM_U, TRIM_V, x, -32.0F, -38.0F,
                    2.0F, 12.0F, 2.0F);
        }
        // Bowsprit, stepping up toward the tip.
        cube(fore, "bowsprit_1", YARD_U, YARD_V, 148.0F, -26.0F, -4.0F, 12.0F, 4.0F, 8.0F);
        cube(fore, "bowsprit_2", YARD_U, YARD_V, 160.0F, -29.0F, -3.0F, 10.0F, 4.0F, 6.0F);
        cube(fore, "bowsprit_3", MAST_U, MAST_V, 170.0F, -32.0F, -2.0F, 6.0F, 3.0F, 4.0F);
        cube(fore, "figurehead", TRIM_U, TRIM_V, 144.0F, -30.0F, -4.0F, 6.0F, 12.0F, 8.0F);
        // Deck cargo.
        cube(fore, "crate_r", TRIM_U, TRIM_V, 76.0F, -32.0F, 18.0F, 16.0F, 12.0F, 16.0F);
        cube(fore, "crate_l", TRIM_U, TRIM_V, 80.0F, -32.0F, -34.0F, 16.0F, 12.0F, 16.0F);
        cube(fore, "barrel", TRIM_U, TRIM_V, 94.0F, -30.0F, -10.0F, 10.0F, 10.0F, 10.0F);
        cube(fore, "anchor", TRIM_U, TRIM_V, 120.0F, -14.0F, 40.0F, 8.0F, 12.0F, 6.0F);
    }

    private static void buildMast(PartDefinition root) {
        PartDefinition mast = root.addOrReplaceChild("mast", CubeListBuilder.create(),
                PartPose.ZERO);
        cube(mast, "lower", MAST_U, MAST_V, 36.0F, -100.0F, -4.0F, 8.0F, 88.0F, 8.0F);
        cube(mast, "upper", MAST_U, MAST_V, 38.0F, -124.0F, -2.0F, 4.0F, 24.0F, 4.0F);
        for (int i = 0; i < 2; i++) {
            cube(mast, "nest_" + i, TRIM_U, TRIM_V, 30.0F + i * 10.0F, -96.0F, -11.0F,
                    10.0F, 4.0F, 22.0F);
        }
        cube(mast, "top_platform", TRIM_U, TRIM_V, 34.0F, -124.0F, -5.0F,
                12.0F, 2.0F, 10.0F);
        // Main yard, in four segments so the UV stays on the spar texture.
        for (int i = 0; i < 4; i++) {
            cube(mast, "yard_" + i, YARD_U, YARD_V, 38.0F, -104.0F, -56.0F + i * 28.0F,
                    4.0F, 4.0F, 28.0F);
        }
        for (int i = 0; i < 3; i++) {
            cube(mast, "yard_upper_" + i, YARD_U, YARD_V, 38.5F, -118.0F,
                    -36.0F + i * 24.0F, 3.0F, 3.0F, 24.0F);
        }
        cube(mast, "sail", SAIL_U, SAIL_V, 38.0F, -102.0F, -46.0F, 2.0F, 46.0F, 92.0F);
        cube(mast, "topsail", SAIL_U, SAIL_V, 39.0F, -120.0F, -30.0F, 2.0F, 16.0F, 60.0F);
        cube(mast, "flag", TRIM_U, TRIM_V, 38.0F, -136.0F, -8.0F, 2.0F, 12.0F, 16.0F);
        // Forestay and backstay: thin spars rotated in the X/Y plane, split into short cubes
        // so each one stays inside the spar texture region.
        PartDefinition forestay = mast.addOrReplaceChild("forestay", CubeListBuilder.create(),
                PartPose.offsetAndRotation(40.0F, -122.0F, 0.0F, 0.0F, 0.0F, 0.74F));
        for (int i = 0; i < 8; i++) {
            cube(forestay, "rope_" + i, MAST_U, MAST_V, i * 18.25F, -0.75F, -0.75F,
                    18.25F, 1.5F, 1.5F);
        }
        PartDefinition backstay = mast.addOrReplaceChild("backstay", CubeListBuilder.create(),
                PartPose.offsetAndRotation(40.0F, -122.0F, 0.0F, 0.0F, 0.0F, 2.57F));
        for (int i = 0; i < 8; i++) {
            cube(backstay, "rope_" + i, MAST_U, MAST_V, i * 22.75F, -0.75F, -0.75F,
                    22.75F, 1.5F, 1.5F);
        }
    }

    private static void buildFunnel(PartDefinition root) {
        PartDefinition funnel = root.addOrReplaceChild("funnel", CubeListBuilder.create(),
                PartPose.ZERO);
        cube(funnel, "base", FUNNEL_U, FUNNEL_V, -93.0F, -54.0F, -9.0F, 18.0F, 10.0F, 18.0F);
        cube(funnel, "mid", FUNNEL_U, FUNNEL_V, -90.0F, -64.0F, -7.0F, 14.0F, 10.0F, 14.0F);
        cube(funnel, "top", FUNNEL_U, FUNNEL_V, -89.0F, -70.0F, -6.0F, 12.0F, 6.0F, 12.0F);
        cube(funnel, "rim", FUNNEL_U, FUNNEL_V, -91.0F, -72.0F, -8.0F, 16.0F, 2.0F, 16.0F);
        cube(funnel, "steam_pipe", MAST_U, MAST_V, -76.0F, -64.0F, -2.0F, 3.0F, 20.0F, 3.0F);
    }

    private static void buildBenches(PartDefinition root) {
        PartDefinition benches = root.addOrReplaceChild("benches", CubeListBuilder.create(),
                PartPose.ZERO);
        // Two long benches along the bulwarks, four inside the cabin.
        cube(benches, "rail_r_fore", BENCH_U, BENCH_V, -56.0F, -14.0F, 29.0F,
                56.0F, 2.0F, 8.0F);
        cube(benches, "rail_r_aft", BENCH_U, BENCH_V, 0.0F, -14.0F, 29.0F,
                56.0F, 2.0F, 8.0F);
        cube(benches, "rail_l_fore", BENCH_U, BENCH_V, -56.0F, -14.0F, -37.0F,
                56.0F, 2.0F, 8.0F);
        cube(benches, "rail_l_aft", BENCH_U, BENCH_V, 0.0F, -14.0F, -37.0F,
                56.0F, 2.0F, 8.0F);
        cube(benches, "cabin_r", BENCH_U, BENCH_V, -104.0F, -14.0F, 24.0F,
                56.0F, 2.0F, 8.0F);
        cube(benches, "cabin_l", BENCH_U, BENCH_V, -104.0F, -14.0F, -32.0F,
                56.0F, 2.0F, 8.0F);
    }

    private static void buildDetails(PartDefinition root) {
        PartDefinition details = root.addOrReplaceChild("details", CubeListBuilder.create(),
                PartPose.ZERO);
        // Ship's wheel on the cabin front, and a pair of braces along the deck edge.
        cube(details, "wheel_post", TRIM_U, TRIM_V, -46.0F, -30.0F, -1.0F, 2.0F, 18.0F, 2.0F);
        cube(details, "wheel", TRIM_U, TRIM_V, -47.0F, -30.0F, -6.0F, 1.0F, 10.0F, 12.0F);
    }

    private static void buildLanterns(PartDefinition lanterns) {
        float[][] posts = {
                {102.0F, 40.0F}, {102.0F, -44.0F},
                {-104.0F, 40.0F}, {-104.0F, -44.0F},
                {40.0F, 28.0F}, {40.0F, -32.0F},
        };
        for (int i = 0; i < posts.length; i++) {
            float x = posts[i][0];
            float z = posts[i][1];
            cube(lanterns, "hanger_" + i, MAST_U, MAST_V, x, -32.0F, z, 2.0F, 8.0F, 2.0F);
            cube(lanterns, "body_" + i, LANTERN_U, LANTERN_V, x - 2.0F, -36.0F, z - 2.0F,
                    6.0F, 8.0F, 6.0F);
            cube(lanterns, "cap_" + i, TRIM_U, TRIM_V, x - 3.0F, -38.0F, z - 3.0F,
                    8.0F, 2.0F, 8.0F);
        }
    }
}
