package com.sanchous98.eldritchhorror.client.model;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.ShipGeometry;
import com.sanchous98.eldritchhorror.entity.ShipSeats;
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
 * The liner: a genuine 91-block vessel, not a scaled dinghy.
 *
 * <p>Model space follows the vanilla boat convention used by the renderer: one unit is 1/16 block,
 * {@code +X} points at the bow and {@code +Y} points <em>down</em>. The renderer's
 * {@code scale(-1, -1, 1)} plus quarter turn puts the bow on the entity's forward axis and the deck
 * right way up. Model {@code y = 0} is the entity origin, so the deck top is modelled exactly at
 * {@code -DECK_Y * 16} and the model needs no buoyancy lift: the deck the player stands on is the
 * deck that is drawn.
 *
 * <p>Contents: a stepped 91 x 18 dark hull with a red boot topping, a planked main deck, a
 * bulwark and stanchion rail, a 44 x 16 white deckhouse with two window bands, a promenade/boat
 * deck on its roof, a navigation bridge, three funnels, two masts, eight lifeboats, portholes,
 * deck chairs for all {@link ShipSeats#TOTAL} seats, a canvas awning over the aft deck, and stepped
 * gangways on both beams. Geometry constants come from {@link ShipGeometry}, so the
 * hull collision box, the seat points and the drawn hull cannot drift apart.
 *
 * <p>The lanterns are a separate baked layer drawn with the eyes render type, so they stay lit at
 * night without making the whole ship full-bright.
 */
public final class CoastalPacketModel extends EntityModel<CoastalPacketRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "coastal_packet"), "main");
    /** Lanterns are baked separately so the renderer can draw them with the eyes render type. */
    public static final ModelLayerLocation LANTERN_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "coastal_packet"), "lanterns");

    // Texture regions painted by tools/generate_ship_texture.py (2048 x 2048, one sheet).
    private static final int HULL_U = 0;
    private static final int HULL_V = 0;
    private static final int HULL_ALT_U = 832;
    private static final int HULL_ALT_V = 0;
    private static final int DECK_U = 0;
    private static final int DECK_V = 456;
    private static final int WHITE_U = 896;
    private static final int WHITE_V = 456;
    private static final int ROOF_U = 0;
    private static final int ROOF_V = 784;
    private static final int WINDOW_U = 1584;
    private static final int WINDOW_V = 456;
    private static final int WINDOW_TALL_U = 1584;
    private static final int WINDOW_TALL_V = 496;
    private static final int LIFEBOAT_U = 0;
    private static final int LIFEBOAT_V = 1112;
    private static final int FUNNEL_U = 176;
    private static final int FUNNEL_V = 1112;
    private static final int MAST_U = 432;
    private static final int MAST_V = 1112;
    private static final int YARD_U = 496;
    private static final int YARD_V = 1112;
    private static final int KEEL_U = 0;
    private static final int KEEL_V = 1664;
    private static final int RAIL_U = 0;
    private static final int RAIL_V = 1456;
    private static final int BENCH_U = 344;
    private static final int BENCH_V = 1456;
    private static final int TRIM_U = 688;
    private static final int TRIM_V = 1456;
    private static final int RED_U = 1032;
    private static final int RED_V = 1456;
    private static final int CANVAS_U = 0;
    private static final int CANVAS_V = 1560;
    private static final int LANTERN_U = 344;
    private static final int LANTERN_V = 1560;
    private static final int PORTHOLE_U = 680;
    private static final int PORTHOLE_V = 1560;

    // Hull and deck, in model units. These mirror ShipGeometry, converted to 1/16 blocks and y-down.
    private static final float DECK_Y = -102.4F;
    private static final float KEEL_Y = 44.0F;
    private static final float ROOF_Y = -217.6F;
    private static final float BRIDGE_TOP_Y = -265.6F;
    private static final float BEAM = 144.0F;
    private static final float STRAIGHT = 640.0F;
    /** Hull sides stop one block below the walk surface; the deck slab covers the gap. */
    private static final float HULL_TOP_Y = DECK_Y + 16.0F;
    private static final float HULL_HEIGHT = KEEL_Y - HULL_TOP_Y;
    private static final float DECKHOUSE_HALF_Z = (float) ShipGeometry.DECKHOUSE_Z_HALF * 16.0F;
    private static final float DECKHOUSE_HALF_X = (float) ShipGeometry.DECKHOUSE_X_HALF * 16.0F;
    private static final float BRIDGE_HALF_Z = (float) ShipGeometry.BRIDGE_Z_HALF * 16.0F;
    private static final float BRIDGE_HALF_X = (float) ShipGeometry.BRIDGE_X_HALF * 16.0F;
    private static final float BRIDGE_CENTRE_X = (float) ShipGeometry.BRIDGE_Z_CENTER * 16.0F;
    private static final float BULWARK_TOP = DECK_Y - 18.0F;
    private static final float SUPERSTRUCTURE_HEIGHT = DECK_Y - ROOF_Y;

    public CoastalPacketModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        buildHull(root);
        buildDeck(root);
        buildBulwark(root);
        buildSuperstructure(root);
        buildBridge(root);
        buildRoofRail(root);
        buildFunnels(root);
        buildMasts(root);
        buildLifeboats(root);
        buildSeats(root);
        buildGangway(root);
        buildAwning(root);
        buildDetails(root);
        return LayerDefinition.create(mesh, 2048, 2048);
    }

    /** @return a lantern-only layer for the full-bright pass */
    public static LayerDefinition createLanternLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition lanterns = root.addOrReplaceChild("lanterns", CubeListBuilder.create(),
                PartPose.ZERO);
        buildLanterns(lanterns);
        return LayerDefinition.create(mesh, 2048, 2048);
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
        // Ten 8-block strakes from the stern counter to the bow shoulder.
        for (int i = 0; i < 10; i++) {
            float x = -STRAIGHT + i * 128.0F;
            cube(hull, "strake_" + i, i % 2 == 0 ? HULL_U : HULL_ALT_U,
                    i % 2 == 0 ? HULL_V : HULL_ALT_V,
                    x, HULL_TOP_Y, -BEAM, 128.0F, HULL_HEIGHT, BEAM * 2.0F);
        }
        // Stepped bow, narrowing to the stem.
        cube(hull, "bow_1", HULL_U, HULL_V, STRAIGHT, HULL_TOP_Y, -120.0F, 40.0F, HULL_HEIGHT, 240.0F);
        cube(hull, "bow_2", HULL_ALT_U, HULL_ALT_V, 680.0F, HULL_TOP_Y, -80.0F, 32.0F,
                HULL_HEIGHT, 160.0F);
        cube(hull, "bow_3", HULL_U, HULL_V, 712.0F, HULL_TOP_Y, -40.0F, 16.0F,
                HULL_HEIGHT, 80.0F);
        // Stepped stern counter.
        cube(hull, "stern_1", HULL_ALT_U, HULL_ALT_V, -680.0F, HULL_TOP_Y, -120.0F, 40.0F,
                HULL_HEIGHT, 240.0F);
        cube(hull, "stern_2", HULL_U, HULL_V, -712.0F, HULL_TOP_Y, -80.0F, 32.0F,
                HULL_HEIGHT, 160.0F);
        cube(hull, "stern_3", HULL_ALT_U, HULL_ALT_V, -728.0F, HULL_TOP_Y, -40.0F, 16.0F,
                HULL_HEIGHT, 80.0F);
        // Keel ribbon and a red boot topping at the waterline, cut into short cubes so each
        // sampled UV stays inside its painted region.
        for (int i = 0; i < 20; i++) {
            float x = -600.0F + i * 60.0F;
            cube(hull, "keel_" + i, KEEL_U, KEEL_V, x, KEEL_Y, -BEAM + 8.0F, 60.0F, 10.0F, 272.0F);
        }
        for (int i = 0; i < 10; i++) {
            float x = -600.0F + i * 120.0F;
            cube(hull, "boot_r_" + i, RED_U, RED_V, x, 6.0F, BEAM - 1.0F, 120.0F, 10.0F, 2.0F);
            cube(hull, "boot_l_" + i, RED_U, RED_V, x, 6.0F, -BEAM - 1.0F, 120.0F, 10.0F, 2.0F);
        }
        // Portholes: two rows down the long hull side.
        for (int i = 0; i < 24; i++) {
            float x = -560.0F + i * 48.0F;
            cube(hull, "porthole_r_" + i, PORTHOLE_U, PORTHOLE_V, x, -48.0F,
                    BEAM - 1.0F, 14.0F, 14.0F, 2.0F);
            cube(hull, "porthole_l_" + i, PORTHOLE_U, PORTHOLE_V, x, -48.0F,
                    -BEAM - 1.0F, 14.0F, 14.0F, 2.0F);
        }
        // A second, higher row along the cabin's stretch.
        for (int i = 0; i < 10; i++) {
            float x = -400.0F + i * 88.0F;
            cube(hull, "porthole_hi_r_" + i, PORTHOLE_U, PORTHOLE_V, x, -78.0F,
                    BEAM - 1.0F, 12.0F, 12.0F, 2.0F);
            cube(hull, "porthole_hi_l_" + i, PORTHOLE_U, PORTHOLE_V, x, -78.0F,
                    -BEAM - 1.0F, 12.0F, 12.0F, 2.0F);
        }
    }

    private static void buildDeck(PartDefinition root) {
        PartDefinition deck = root.addOrReplaceChild("deck", CubeListBuilder.create(),
                PartPose.ZERO);
        for (int i = 0; i < 8; i++) {
            float x = -STRAIGHT + i * 160.0F;
            cube(deck, "plank_" + i, DECK_U, DECK_V, x, DECK_Y, -BEAM,
                    160.0F, 16.0F, BEAM * 2.0F);
        }
        cube(deck, "bow_plank_1", DECK_U, DECK_V, STRAIGHT, DECK_Y, -120.0F, 40.0F, 16.0F, 240.0F);
        cube(deck, "bow_plank_2", DECK_U, DECK_V, 680.0F, HULL_TOP_Y, -80.0F, 32.0F, 16.0F, 160.0F);
        cube(deck, "bow_plank_3", DECK_U, DECK_V, 712.0F, HULL_TOP_Y, -40.0F, 16.0F, 16.0F, 80.0F);
        cube(deck, "stern_plank_1", DECK_U, DECK_V, -680.0F, HULL_TOP_Y, -120.0F, 40.0F, 16.0F, 240.0F);
        cube(deck, "stern_plank_2", DECK_U, DECK_V, -712.0F, HULL_TOP_Y, -80.0F, 32.0F, 16.0F, 160.0F);
        cube(deck, "stern_plank_3", DECK_U, DECK_V, -728.0F, HULL_TOP_Y, -40.0F, 16.0F, 16.0F, 80.0F);
    }

    private static void buildBulwark(PartDefinition root) {
        PartDefinition rail = root.addOrReplaceChild("bulwark", CubeListBuilder.create(),
                PartPose.ZERO);
        for (int i = 0; i < 8; i++) {
            float x = -STRAIGHT + i * 160.0F;
            // Waist-high solid bulwark, a little inboard of the sheer.
            cube(rail, "wall_r_" + i, RAIL_U, RAIL_V, x, BULWARK_TOP, BEAM - 5.0F,
                    160.0F, 18.0F, 5.0F);
            cube(rail, "wall_l_" + i, RAIL_U, RAIL_V, x, BULWARK_TOP, -BEAM,
                    160.0F, 18.0F, 5.0F);
            // Cap rail with a square section, the line a passenger sees the sea over.
            cube(rail, "cap_r_" + i, TRIM_U, TRIM_V, x, BULWARK_TOP - 4.0F, BEAM - 6.0F,
                    160.0F, 4.0F, 7.0F);
            cube(rail, "cap_l_" + i, TRIM_U, TRIM_V, x, BULWARK_TOP - 4.0F, -BEAM - 1.0F,
                    160.0F, 4.0F, 7.0F);
        }
        for (int i = 0; i <= 16; i++) {
            float x = -640.0F + i * 80.0F;
            cube(rail, "post_r_" + i, TRIM_U, TRIM_V, x, DECK_Y - 24.0F, BEAM - 4.0F,
                    4.0F, 26.0F, 4.0F);
            cube(rail, "post_l_" + i, TRIM_U, TRIM_V, x, DECK_Y - 24.0F, -BEAM,
                    4.0F, 26.0F, 4.0F);
        }
        // Bow and stern rails across the taper.
        for (int i = 0; i < 5; i++) {
            float z = -80.0F + i * 40.0F;
            cube(rail, "bow_rail_" + i, TRIM_U, TRIM_V, 720.0F, BULWARK_TOP - 4.0F, z,
                    8.0F, 4.0F, 40.0F);
            cube(rail, "stern_rail_" + i, TRIM_U, TRIM_V, -728.0F, BULWARK_TOP - 4.0F, z,
                    8.0F, 4.0F, 40.0F);
        }
    }

    private static void buildSuperstructure(PartDefinition root) {
        PartDefinition house = root.addOrReplaceChild("deckhouse", CubeListBuilder.create(),
                PartPose.ZERO);
        // Eight 5.5-block panels: 44 blocks long, 16 wide, two decks tall.
        for (int i = 0; i < 8; i++) {
            float x = -DECKHOUSE_HALF_Z + i * (DECKHOUSE_HALF_Z * 2.0F / 8.0F);
            float width = DECKHOUSE_HALF_Z * 2.0F / 8.0F;
            cube(house, "wall_" + i, WHITE_U, WHITE_V, x, ROOF_Y, -DECKHOUSE_HALF_X,
                    width, SUPERSTRUCTURE_HEIGHT, DECKHOUSE_HALF_X * 2.0F);
        }
        // Window bands on both sides, two decks of them.
        for (int i = 0; i < 8; i++) {
            float x = -DECKHOUSE_HALF_Z + 4.0F + i * (DECKHOUSE_HALF_Z * 2.0F / 8.0F);
            cube(house, "window_a_r_" + i, WINDOW_U, WINDOW_V, x, -148.0F,
                    DECKHOUSE_HALF_X - 1.0F, 72.0F, 24.0F, 2.0F);
            cube(house, "window_a_l_" + i, WINDOW_U, WINDOW_V, x, -148.0F,
                    -DECKHOUSE_HALF_X - 1.0F, 72.0F, 24.0F, 2.0F);
            cube(house, "window_b_r_" + i, WINDOW_U, WINDOW_V, x, -194.0F,
                    DECKHOUSE_HALF_X - 1.0F, 72.0F, 24.0F, 2.0F);
            cube(house, "window_b_l_" + i, WINDOW_U, WINDOW_V, x, -194.0F,
                    -DECKHOUSE_HALF_X - 1.0F, 72.0F, 24.0F, 2.0F);
        }
        // Front and rear window bays of the cabin block.
        for (int i = 0; i < 3; i++) {
            float z = -96.0F + i * 64.0F;
            cube(house, "window_front_" + i, WINDOW_TALL_U, WINDOW_TALL_V,
                    DECKHOUSE_HALF_Z - 1.0F, -200.0F, z, 2.0F, 32.0F, 48.0F);
            cube(house, "window_rear_" + i, WINDOW_TALL_U, WINDOW_TALL_V,
                    -DECKHOUSE_HALF_Z - 1.0F, -200.0F, z, 2.0F, 32.0F, 48.0F);
        }
        // Promenade roof slab.
        for (int i = 0; i < 8; i++) {
            float x = -DECKHOUSE_HALF_Z + i * (DECKHOUSE_HALF_Z * 2.0F / 8.0F);
            float width = DECKHOUSE_HALF_Z * 2.0F / 8.0F;
            cube(house, "roof_" + i, ROOF_U, ROOF_V, x, ROOF_Y - 8.0F,
                    -DECKHOUSE_HALF_X - 8.0F, width, 8.0F, DECKHOUSE_HALF_X * 2.0F + 16.0F);
        }
        // Fascia band under the promenade deck.
        for (int i = 0; i < 8; i++) {
            float x = -DECKHOUSE_HALF_Z + i * (DECKHOUSE_HALF_Z * 2.0F / 8.0F);
            float width = DECKHOUSE_HALF_Z * 2.0F / 8.0F;
            cube(house, "fascia_r_" + i, TRIM_U, TRIM_V, x, ROOF_Y - 6.0F,
                    DECKHOUSE_HALF_X, width, 8.0F, 1.0F);
            cube(house, "fascia_l_" + i, TRIM_U, TRIM_V, x, ROOF_Y - 6.0F,
                    -DECKHOUSE_HALF_X - 1.0F, width, 8.0F, 1.0F);
        }
    }

    private static void buildBridge(PartDefinition root) {
        PartDefinition bridge = root.addOrReplaceChild("bridge", CubeListBuilder.create(),
                PartPose.ZERO);
        for (int i = 0; i < 2; i++) {
            float x = BRIDGE_CENTRE_X - BRIDGE_HALF_Z + i * BRIDGE_HALF_Z;
            cube(bridge, "wall_" + i, WHITE_U, WHITE_V, x, BRIDGE_TOP_Y,
                    -BRIDGE_HALF_X, BRIDGE_HALF_Z, ROOF_Y - BRIDGE_TOP_Y,
                    BRIDGE_HALF_X * 2.0F);
        }
        // Wrap-around bridge windows.
        for (int i = 0; i < 4; i++) {
            float z = -BRIDGE_HALF_X + 8.0F + i * 32.0F;
            cube(bridge, "window_" + i, WINDOW_TALL_U, WINDOW_TALL_V,
                    BRIDGE_CENTRE_X + BRIDGE_HALF_Z - 1.0F, -254.0F, z, 2.0F, 28.0F, 30.0F);
        }
        cube(bridge, "window_port", WINDOW_TALL_U, WINDOW_TALL_V,
                BRIDGE_CENTRE_X - 40.0F, -254.0F, -BRIDGE_HALF_X - 1.0F, 80.0F, 28.0F, 2.0F);
        cube(bridge, "window_starboard", WINDOW_TALL_U, WINDOW_TALL_V,
                BRIDGE_CENTRE_X - 40.0F, -254.0F, BRIDGE_HALF_X - 1.0F, 80.0F, 28.0F, 2.0F);
        // Bridge roof and wings; the wings reach past the deckhouse for the classic profile.
        for (int i = 0; i < 2; i++) {
            cube(bridge, "roof_" + i, ROOF_U, ROOF_V,
                    BRIDGE_CENTRE_X - BRIDGE_HALF_Z - 8.0F + i * (BRIDGE_HALF_Z + 8.0F),
                    BRIDGE_TOP_Y - 8.0F, -BRIDGE_HALF_X,
                    BRIDGE_HALF_Z + 8.0F, 8.0F, BRIDGE_HALF_X * 2.0F);
        }
        cube(bridge, "wing_r", WHITE_U, WHITE_V, BRIDGE_CENTRE_X - BRIDGE_HALF_Z,
                ROOF_Y - 44.0F, BRIDGE_HALF_X - 2.0F, BRIDGE_HALF_Z * 2.0F, 8.0F, 42.0F);
        cube(bridge, "wing_l", WHITE_U, WHITE_V, BRIDGE_CENTRE_X - BRIDGE_HALF_Z,
                ROOF_Y - 44.0F, -BRIDGE_HALF_X - 40.0F, BRIDGE_HALF_Z * 2.0F, 8.0F, 42.0F);
        // Wheelhouse door and a pair of lamps.
        cube(bridge, "door", TRIM_U, TRIM_V, BRIDGE_CENTRE_X - BRIDGE_HALF_Z - 4.0F,
                ROOF_Y - 32.0F, -10.0F, 2.0F, 32.0F, 20.0F);
    }

    private static void buildRoofRail(PartDefinition root) {
        PartDefinition rail = root.addOrReplaceChild("promenade_rail", CubeListBuilder.create(),
                PartPose.ZERO);
        float top = ROOF_Y - 16.0F;
        for (int i = 0; i < 8; i++) {
            float x = -DECKHOUSE_HALF_Z + i * (DECKHOUSE_HALF_Z * 2.0F / 8.0F);
            float width = DECKHOUSE_HALF_Z * 2.0F / 8.0F;
            cube(rail, "cap_r_" + i, TRIM_U, TRIM_V, x, top, DECKHOUSE_HALF_X + 6.0F,
                    width, 4.0F, 4.0F);
            cube(rail, "cap_l_" + i, TRIM_U, TRIM_V, x, top, -DECKHOUSE_HALF_X - 10.0F,
                    width, 4.0F, 4.0F);
        }
        for (int i = 0; i <= 12; i++) {
            float x = -DECKHOUSE_HALF_Z + i * (DECKHOUSE_HALF_Z * 2.0F / 12.0F);
            cube(rail, "post_r_" + i, TRIM_U, TRIM_V, x, ROOF_Y - 16.0F,
                    DECKHOUSE_HALF_X + 6.0F, 3.0F, 16.0F, 3.0F);
            cube(rail, "post_l_" + i, TRIM_U, TRIM_V, x, ROOF_Y - 16.0F,
                    -DECKHOUSE_HALF_X - 9.0F, 3.0F, 16.0F, 3.0F);
        }
        for (int i = 0; i < 4; i++) {
            float z = -DECKHOUSE_HALF_X + i * (DECKHOUSE_HALF_X * 2.0F / 4.0F);
            cube(rail, "cap_fore_" + i, TRIM_U, TRIM_V, DECKHOUSE_HALF_Z - 4.0F, top, z,
                    4.0F, 4.0F, DECKHOUSE_HALF_X * 2.0F / 4.0F);
            cube(rail, "cap_aft_" + i, TRIM_U, TRIM_V, -DECKHOUSE_HALF_Z, top, z,
                    4.0F, 4.0F, DECKHOUSE_HALF_X * 2.0F / 4.0F);
        }
    }

    private static void buildFunnels(PartDefinition root) {
        PartDefinition funnels = root.addOrReplaceChild("funnels", CubeListBuilder.create(),
                PartPose.ZERO);
        float[] centres = {-240.0F, -80.0F, 80.0F};
        for (int i = 0; i < centres.length; i++) {
            float x = centres[i];
            cube(funnels, "base_" + i, FUNNEL_U, FUNNEL_V, x - 32.0F, ROOF_Y - 16.0F,
                    -32.0F, 64.0F, 16.0F, 64.0F);
            cube(funnels, "stem_" + i, FUNNEL_U, FUNNEL_V, x - 28.0F, ROOF_Y - 104.0F,
                    -28.0F, 56.0F, 88.0F, 56.0F);
            cube(funnels, "band_" + i, RED_U, RED_V, x - 30.0F, ROOF_Y - 76.0F,
                    -30.0F, 60.0F, 20.0F, 60.0F);
            cube(funnels, "cap_" + i, FUNNEL_U, FUNNEL_V, x - 32.0F, ROOF_Y - 112.0F,
                    -32.0F, 64.0F, 8.0F, 64.0F);
            cube(funnels, "pipe_" + i, MAST_U, MAST_V, x + 20.0F, ROOF_Y - 104.0F,
                    12.0F, 6.0F, 34.0F, 6.0F);
        }
    }

    private static void buildMasts(PartDefinition root) {
        PartDefinition masts = root.addOrReplaceChild("masts", CubeListBuilder.create(),
                PartPose.ZERO);
        float[] centres = {560.0F, -560.0F};
        for (int i = 0; i < centres.length; i++) {
            float x = centres[i];
            cube(masts, "mast_" + i, MAST_U, MAST_V, x, -390.0F, -8.0F, 16.0F, 288.0F, 16.0F);
            cube(masts, "nest_" + i, TRIM_U, TRIM_V, x - 14.0F, -330.0F, -14.0F,
                    28.0F, 6.0F, 28.0F);
            cube(masts, "yard_low_" + i, YARD_U, YARD_V, x, -320.0F, -80.0F,
                    12.0F, 12.0F, 160.0F);
            cube(masts, "yard_high_" + i, YARD_U, YARD_V, x, -356.0F, -56.0F,
                    10.0F, 10.0F, 112.0F);
            cube(masts, "truck_" + i, TRIM_U, TRIM_V, x + 2.0F, -402.0F, -2.0F,
                    12.0F, 12.0F, 4.0F);
            // Fore-and-aft stays, split into short cubes to stay inside the mast texture.
            cube(masts, "stay_fore_" + i, MAST_U, MAST_V, x, -380.0F, 8.0F, 6.0F, 260.0F, 6.0F);
            cube(masts, "stay_aft_" + i, MAST_U, MAST_V, x, -380.0F, -14.0F, 6.0F, 260.0F, 6.0F);
        }
    }

    private static void buildLifeboats(PartDefinition root) {
        PartDefinition boats = root.addOrReplaceChild("lifeboats", CubeListBuilder.create(),
                PartPose.ZERO);
        float[] centres = {-260.0F, -120.0F, 120.0F, 260.0F};
        for (int i = 0; i < centres.length; i++) {
            for (int side = 0; side < 2; side++) {
                float z = side == 0 ? BEAM + 4.0F : -BEAM - 28.0F;
                float x = centres[i];
                cube(boats, "boat_" + i + "_" + side, LIFEBOAT_U, LIFEBOAT_V,
                        x - 32.0F, ROOF_Y - 8.0F, z, 64.0F, 16.0F, 24.0F);
                cube(boats, "gunwale_" + i + "_" + side, TRIM_U, TRIM_V,
                        x - 34.0F, ROOF_Y - 12.0F, z - 2.0F, 68.0F, 4.0F, 28.0F);
                cube(boats, "davit_a_" + i + "_" + side, TRIM_U, TRIM_V,
                        x - 26.0F, ROOF_Y - 40.0F, z, 5.0F, 40.0F, 5.0F);
                cube(boats, "davit_b_" + i + "_" + side, TRIM_U, TRIM_V,
                        x + 22.0F, ROOF_Y - 40.0F, z, 5.0F, 40.0F, 5.0F);
            }
        }
    }

    private static void buildSeats(PartDefinition root) {
        PartDefinition seats = root.addOrReplaceChild("seats", CubeListBuilder.create(),
                PartPose.ZERO);
        for (int i = 0; i < ShipSeats.TOTAL; i++) {
            double[] seat = ShipSeats.offset(i);
            float x = (float) (seat[2] * 16.0);
            float z = (float) (seat[0] * 16.0);
            cube(seats, "bench_" + i, BENCH_U, BENCH_V, x - 12.0F, DECK_Y - 4.0F, z - 8.0F,
                    24.0F, 4.0F, 16.0F);
            cube(seats, "back_" + i, BENCH_U, BENCH_V, x - 12.0F, DECK_Y - 16.0F,
                    z - 8.0F, 24.0F, 12.0F, 3.0F);
        }
    }

    private static void buildGangway(PartDefinition root) {
        PartDefinition gangway = root.addOrReplaceChild("gangway", CubeListBuilder.create(),
                PartPose.ZERO);
        float inner = BEAM;
        float outer = (float) (ShipGeometry.GANGWAY_OUTER_X * 16.0);
        float bottom = (float) (-ShipGeometry.GANGWAY_BOTTOM_Y * 16.0);
        int steps = 8;
        float stepDepth = (outer - inner) / steps;
        // One gangway on each beam, so a berth always has a stepped approach facing the quay.
        for (int side = 0; side < 2; side++) {
            float sign = side == 0 ? 1.0F : -1.0F;
            for (int i = 0; i < steps; i++) {
                float z = sign > 0.0F ? inner + i * stepDepth
                        : -inner - (i + 1) * stepDepth;
                float top = bottom + (DECK_Y - bottom) * (steps - i) / steps;
                cube(gangway, "step_" + side + "_" + i, DECK_U, DECK_V,
                        -48.0F, top, z, 96.0F, 8.0F, stepDepth);
                cube(gangway, "stringer_r_" + side + "_" + i, TRIM_U, TRIM_V,
                        -50.0F, top - 4.0F, z, 4.0F, 6.0F, stepDepth);
                cube(gangway, "stringer_l_" + side + "_" + i, TRIM_U, TRIM_V,
                        46.0F, top - 4.0F, z, 4.0F, 6.0F, stepDepth);
            }
            float landingZ = sign > 0.0F ? outer : -outer - 48.0F;
            cube(gangway, "landing_" + side, DECK_U, DECK_V, -64.0F, bottom,
                    landingZ, 128.0F, 8.0F, 48.0F);
            for (int i = 0; i < 4; i++) {
                float postZ = sign > 0.0F ? inner + 12.0F + i * 28.0F
                        : -(inner + 12.0F + i * 28.0F) - 4.0F;
                cube(gangway, "post_a_" + side + "_" + i, TRIM_U, TRIM_V,
                        -52.0F, bottom - 54.0F, postZ, 4.0F, 54.0F, 4.0F);
                cube(gangway, "post_b_" + side + "_" + i, TRIM_U, TRIM_V,
                        48.0F, bottom - 54.0F, postZ, 4.0F, 54.0F, 4.0F);
            }
        }
    }

    private static void buildAwning(PartDefinition root) {
        PartDefinition awning = root.addOrReplaceChild("awning", CubeListBuilder.create(),
                PartPose.ZERO);
        // Posts.
        for (int i = 0; i < 4; i++) {
            float x = -620.0F + i * 60.0F;
            cube(awning, "post_r_" + i, TRIM_U, TRIM_V, x, DECK_Y - 56.0F, 116.0F,
                    4.0F, 56.0F, 4.0F);
            cube(awning, "post_l_" + i, TRIM_U, TRIM_V, x, DECK_Y - 56.0F, -120.0F,
                    4.0F, 56.0F, 4.0F);
        }
        // Fifteen canvas panels make the striped canopy.
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 3; j++) {
                cube(awning, "canvas_" + i + "_" + j, CANVAS_U, CANVAS_V,
                        -624.0F + i * 40.0F, DECK_Y - 64.0F, -120.0F + j * 80.0F,
                        40.0F, 8.0F, 80.0F);
            }
        }
    }

    private static void buildDetails(PartDefinition root) {
        PartDefinition details = root.addOrReplaceChild("details", CubeListBuilder.create(),
                PartPose.ZERO);
        // Cargo cranes and a pair of ventilators on the foredeck.
        cube(details, "crane_base", TRIM_U, TRIM_V, 420.0F, DECK_Y - 30.0F, -90.0F,
                24.0F, 30.0F, 24.0F);
        cube(details, "crane_jib", TRIM_U, TRIM_V, 402.0F, DECK_Y - 84.0F, -84.0F,
                60.0F, 8.0F, 8.0F);
        cube(details, "vent_a", TRIM_U, TRIM_V, 300.0F, DECK_Y - 46.0F, 96.0F,
                10.0F, 46.0F, 10.0F);
        cube(details, "vent_b", TRIM_U, TRIM_V, 300.0F, DECK_Y - 46.0F, -106.0F,
                10.0F, 46.0F, 10.0F);
        // Ship's bell and binnacle on the foredeck.
        cube(details, "binnacle", TRIM_U, TRIM_V, 600.0F, DECK_Y - 20.0F, 0.0F,
                16.0F, 20.0F, 16.0F);
        // Anchor hawse pipes at the bow.
        cube(details, "hawse_r", RED_U, RED_V, 690.0F, -42.0F, BEAM - 2.0F, 20.0F, 20.0F, 2.0F);
        cube(details, "hawse_l", RED_U, RED_V, 690.0F, -42.0F, -BEAM - 1.0F, 20.0F, 20.0F, 2.0F);
    }

    private static void buildLanterns(PartDefinition lanterns) {
        float[][] posts = {
                {-340.0F, 150.0F}, {-340.0F, -162.0F},
                {-20.0F, 150.0F}, {-20.0F, -162.0F},
                {300.0F, 150.0F}, {300.0F, -162.0F},
                {700.0F, 130.0F}, {700.0F, -142.0F},
        };
        for (int i = 0; i < posts.length; i++) {
            float x = posts[i][0];
            float z = posts[i][1];
            cube(lanterns, "hanger_" + i, TRIM_U, TRIM_V, x, ROOF_Y - 28.0F, z,
                    4.0F, 12.0F, 4.0F);
            cube(lanterns, "body_" + i, LANTERN_U, LANTERN_V, x - 3.0F, ROOF_Y - 36.0F,
                    z - 3.0F, 10.0F, 8.0F, 10.0F);
            cube(lanterns, "cap_" + i, TRIM_U, TRIM_V, x - 5.0F, ROOF_Y - 40.0F,
                    z - 5.0F, 14.0F, 4.0F, 14.0F);
        }
    }
}
