package com.sanchous98.eldritchhorror.world.loc.city.plan;

import net.minecraft.world.item.DyeColor;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.SiteLoot;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import java.util.List;
import java.util.function.IntBinaryOperator;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.RailShape;

/**
 * Rome between the wars: St Peter's dome over a sweeping colonnade, the Forum's broken temples,
 * Trastevere's ochre lanes and laundry, the catacombs under the centro, the Tiber with its
 * bridges, and an arrival gate on the south bank.
 *
 * <p>Everything is authored through {@link StructureBuilder}: absolute offsets from
 * {@code (cx, ground, cz)}, fixed arrays and index-driven loops. There is no {@code RandomSource},
 * no layout hash and no chunk-local read. The shared generator flattens the district to
 * {@code ground} first, so every surface lot sits on that plate; the catacombs are authored
 * offsets below it. Every coordinate stays inside {@code [-340, 340]}.
 */
public final class RomePlan implements CityPlan {

    /** Authored half-extent (blocks). */
    private static final int HALF = 340;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState PINE_LOG = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState PINE_LEAVES = Blocks.SPRUCE_LEAVES.defaultBlockState();
    private static final BlockState CYPRESS = Blocks.DARK_OAK_LEAVES.defaultBlockState();
    private static final BlockState GRASS = Blocks.SHORT_GRASS.defaultBlockState();
    private static final BlockState POPPY = Blocks.POPPY.defaultBlockState();
    private static final BlockState DANDELION = Blocks.DANDELION.defaultBlockState();
    private static final BlockState OCHRE = Blocks.DYED_TERRACOTTA.pick(DyeColor.ORANGE).defaultBlockState();
    private static final BlockState PLASTER = Blocks.DYED_TERRACOTTA.pick(DyeColor.WHITE).defaultBlockState();
    private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
    private static final BlockState DECK = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private static final BlockState BOARD = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private static final BlockState WHITE_WOOL = Blocks.WOOL.pick(DyeColor.WHITE).defaultBlockState();
    private static final BlockState RED_WOOL = Blocks.WOOL.pick(DyeColor.RED).defaultBlockState();
    private static final BlockState BLACK_WOOL = Blocks.WOOL.pick(DyeColor.BLACK).defaultBlockState();
    private static final BlockState BONE = Blocks.BONE_BLOCK.defaultBlockState();
    private static final BlockState COBWEB = Blocks.COBWEB.defaultBlockState();
    private static final BlockState CANDLE = Blocks.CANDLE.defaultBlockState();
    private static final BlockState BOOKSHELF = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState BARREL = Blocks.BARREL.defaultBlockState();
    private static final BlockState CAULDRON = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState HAY = Blocks.HAY_BLOCK.defaultBlockState();
    private static final BlockState IRON_BARS = Blocks.IRON_BARS.defaultBlockState();
    private static final BlockState BELL = Blocks.BELL.defaultBlockState();

    /** Trastevere lane centres (4 wide). */
    private static final int[] TRAST_LANES = {-304, -264, -224, -184, -144};
    /** Trastevere east-west lane centres (4 wide). */
    private static final int[] TRAST_CROSS = {-224, -160, -96, -32, 32, 96, 160};
    /** Trastevere building-band origins (west edge of each 30-wide block). */
    private static final int[] TRAST_X = {-296, -256, -216, -176};
    /** Trastevere building-band origins (north edge of each 50-deep block). */
    private static final int[] TRAST_Z = {-212, -148, -84, -20, 44, 108};
    /** Forum north-south paths (6 wide). */
    private static final int[] FORUM_X = {160, 200, 240, 280};
    /** Forum east-west paths (6 wide). */
    private static final int[] FORUM_Z = {-240, -200, -160, -120, -80, -40, 0};

    /** Umbrella pines: x, z. */
    private static final int[][] PINES = {
            {-150, -280}, {-164, -266}, {-150, -252},
            {66, -270}, {66, -250}, {136, -270}, {136, -250},
            {150, -248}, {310, -248}, {150, 28}, {310, 28},
            {-268, -172}, {-136, -126}, {-136, -112},
            {-36, -84}, {36, -84}, {-36, 68}, {36, 68},
            {-312, 186}, {-300, 186}, {300, 186}, {312, 186},
            {-120, 246}, {-80, 246}, {80, 246}, {120, 246},
            {-216, 186}, {-200, 186}
    };

    /** Cypress: x, z. */
    private static final int[][] CYPRI = {
            {-152, -270}, {-146, -258}, {66, -244}, {136, -244},
            {-300, -184}, {-140, -120},
            {-24, 176}, {24, 176}
    };

    /** Broken Forum columns: x, z, height. */
    private static final int[][] FORUM_COLUMNS = {
            {144, -232, 7}, {148, -224, 3}, {144, -216, 9}, {148, -208, 5},
            {190, -244, 6}, {196, -236, 10}, {204, -228, 4},
            {246, -244, 8}, {254, -236, 3}, {262, -228, 6},
            {150, -100, 5}, {152, -92, 9}, {150, -84, 3},
            {246, -70, 6}, {272, -56, 9}, {266, -48, 4},
            {152, 8, 7}, {168, 16, 4}, {176, 24, 8},
            {300, -60, 5}, {306, -52, 8}, {312, -44, 3}
    };

    /** Centro lots: x, z, width, depth, storeys, kind (0 shop, 1 cafe, 2 hotel, 3 boarded). */
    private static final int[][] CENTRO_LOTS = {
            {-96, -52, 24, 12, 2, 0},
            {-68, -52, 22, 12, 2, 1},
            {-96, -30, 24, 14, 3, 2},
            {-70, -28, 20, 12, 2, 0},
            {-96, 0, 22, 18, 3, 2},
            {-70, 26, 20, 14, 2, 1},
            {-96, 70, 24, 16, 2, 0},
            {-74, 110, 22, 16, 2, 3},
            {24, -52, 24, 12, 2, 0},
            {52, -52, 22, 12, 2, 1},
            {24, -28, 20, 12, 2, 3},
            {48, 0, 22, 16, 3, 2},
            {48, 30, 20, 14, 2, 1},
            {24, 70, 24, 16, 2, 0},
            {52, 80, 22, 16, 2, 2},
            {24, 110, 22, 16, 2, 0},
            {56, 120, 20, 14, 2, 3}
    };

    /** Cafe tables: x, z. */
    private static final int[][] CAFE_TABLES = {
            {-48, 8}, {-48, 20}, {-48, 32}, {-48, 44},
            {44, 8}, {44, 20}, {44, 32}, {44, 44},
            {-66, 74}, {-60, 86}, {44, 66}, {44, 92},
            {-206, 58}, {-194, 70}, {-206, 82}
    };

    /** Vespas: x, z, facing (0 north, 1 east, 2 south, 3 west). */
    private static final int[][] VESPAS = {
            {-44, -44, 1}, {40, -36, 3}, {-58, 52, 0}, {58, 54, 2},
            {-206, 46, 1}, {-190, 78, 3}, {12, 188, 0}, {-12, 188, 2}
    };

    @Override
    public String cityId() {
        return "rome";
    }

    @Override
    public int halfExtent() {
        return HALF;
    }

    @Override
    public void build(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        compose(b, cx, cz, ground, p, (x, z) -> ground);
    }

    @Override
    public void build(StructureBuilder b, CityPlanContext ctx, Palette p) {
        compose(b, ctx.cx(), ctx.cz(), ctx.ground(), p, ctx.surface()::at);
    }

    private static void compose(StructureBuilder b, int cx, int cz, int ground, Palette p,
                                IntBinaryOperator surface) {
        // Streets and water first, so no facade can overwrite a road, a quay or the river.
        streets(b, cx, cz, ground, p);
        river(b, cx, cz, ground, p);

        // The districts. The heart stays at `ground`; rim structures read the graded surface.
        arrival(b, cx, cz, p, surface);
        vatican(b, cx, cz, ground, p);
        forum(b, cx, cz, ground, p);
        trastevere(b, cx, cz, ground, p);
        centro(b, cx, cz, ground, p);
        catacombs(b, cx, cz, ground, p);
        station(b, cx, cz, p, surface);
        port(b, cx, cz, ground, p);

        // Trees, props and readable notices last, on top of the built fabric.
        dressing(b, cx, cz, ground, p, surface);
        signs(b, cx, cz, ground);

        b.marker("rome_center", cx, ground + 1, cz);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.SIGN, 0, 1, 316, "districts"),
                new CityPoi("city_notices", CityPoi.Kind.SIGN, 0, 1, 58, "notices"),
                new CityPoi("centro_quest_board", CityPoi.Kind.QUEST_BOARD, 0, 0, 60,
                        "rome_stolen_relic;rome_missing_priest"),
                new CityPoi("albergo", CityPoi.Kind.INN, -80, 1, -120, "hotel_keeper"),
                new CityPoi("hotel_keeper", CityPoi.Kind.NPC, -80, 1, -116, "hotel_keeper"),
                new CityPoi("centro_shop", CityPoi.Kind.SHOP, -40, 1, 120, "shopkeeper"),
                new CityPoi("shopkeeper", CityPoi.Kind.NPC, -40, 1, 116, "shopkeeper"),
                new CityPoi("apothecary", CityPoi.Kind.HEALER, -240, 1, 120, "apothecary"),
                new CityPoi("apothecary_keeper", CityPoi.Kind.NPC, -238, 1, 116, "apothecary"),
                new CityPoi("vatican_priest", CityPoi.Kind.NPC, 0, 1, -120, "rome_stolen_relic"),
                new CityPoi("trastevere_priest", CityPoi.Kind.NPC, -240, 1, 0,
                        "rome_missing_priest"),
                new CityPoi("catacombs", CityPoi.Kind.SECRET, 0, -5, 120,
                        "rome_missing_priest"),
                new CityPoi("catacomb_chapel", CityPoi.Kind.SECRET, 0, -5, 140,
                        "rome_stolen_relic"),
                new CityPoi("missing_priest", CityPoi.Kind.NPC, 10, -5, 150,
                        "rome_missing_priest"),
                new CityPoi("st_peters", CityPoi.Kind.LANDMARK, 0, 1, -200, "basilica"),
                new CityPoi("forum", CityPoi.Kind.LANDMARK, 220, 1, -120, "ruins"),
                new CityPoi("termini_station", CityPoi.Kind.STATION, 300, 1, 116, "rail"),
                new CityPoi("tiber_port", CityPoi.Kind.PORT, 60, 1, 199, "tiber_docks"),
                new CityPoi("guide", CityPoi.Kind.NPC, 0, 0, 300, "guide"),
                new CityPoi("carabiniere", CityPoi.Kind.NPC, 18, 0, 310, "carabiniere"),
                new CityPoi("st_peters_sign", CityPoi.Kind.SIGN, -20, 1, -92, "st_peters"),
                new CityPoi("forum_sign", CityPoi.Kind.SIGN, 142, 1, -56, "forum"),
                new CityPoi("trastevere_sign", CityPoi.Kind.SIGN, -138, 1, 40, "trastevere"),
                new CityPoi("catacombs_sign", CityPoi.Kind.SIGN, -10, 1, 82, "catacombs"),
                new CityPoi("river_warning", CityPoi.Kind.SIGN, 20, 1, 240, "river")
        );
    }

    // ------------------------------------------------------------------ terrain helpers

    /** Cuts terrain above the authored level and fills hollows below it. */
    private static void settle(StructureBuilder b, int x, int z, int ground, Palette p) {
        int surface = b.groundY(x, z);
        if (surface > ground) {
            b.fill(x, ground + 1, z, x, surface + 2, z, AIR);
        } else if (surface < ground) {
            b.fill(x, surface + 1, z, x, ground, z, p.foundation());
        }
    }

    /** Levels a footprint to {@code ground} and caps it with a foundation course. */
    private static void pad(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                            Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                settle(b, x, z, ground, p);
                b.put(x, ground, z, p.foundation());
            }
        }
    }

    /** Levels and paves an authored street or plaza rectangle. */
    private static void pave(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                             BlockState state, Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                settle(b, x, z, ground, p);
                b.put(x, ground, z, state);
            }
        }
    }

    /** Paves one course at the graded surface, used on the rim where the plate is not flat. */
    private static void paveAt(StructureBuilder b, int x0, int z0, int x1, int z1,
                               IntBinaryOperator surface, BlockState state) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                b.put(x, surface.applyAsInt(x, z), z, state);
            }
        }
    }

    /** Cuts a three-deep water channel with a sand bed into the authored rectangle. */
    private static void channel(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                                Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                int surface = b.groundY(x, z);
                if (surface > ground - 1) {
                    b.fill(x, ground, z, x, surface + 2, z, AIR);
                } else if (surface < ground - 3) {
                    b.fill(x, surface + 1, z, x, ground - 3, z, p.foundation());
                }
                b.put(x, ground - 3, z, SAND);
                b.put(x, ground - 2, z, WATER);
                b.put(x, ground - 1, z, WATER);
            }
        }
    }

    /** Carves an air box and gives it a foundation floor and a stone ceiling. */
    private static void gallery(StructureBuilder b, int x0, int z0, int x1, int z1,
                                int floor, int height, Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        b.fill(ax, floor, az, bx, floor, bz, p.foundation());
        b.fill(ax, floor + 1, az, bx, floor + height, bz, AIR);
        b.fill(ax, floor + height + 1, az, bx, floor + height + 1, bz, p.wall());
    }

    // ------------------------------------------------------------------ streets

    private static void streets(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // The main axis, 14 wide, from St Peter's square to the arrival gate.
        pave(b, cx - 7, cz - 96, cx + 7, cz + 334, ground, p.ground(), p);
        // Via del Corso, 8 wide, through the centro.
        pave(b, cx - 140, cz - 64, cx + 330, cz - 57, ground, p.ground(), p);
        // Via dei Fori, 8 wide, along the south edge of the centro.
        pave(b, cx - 140, cz + 56, cx + 330, cz + 63, ground, p.ground(), p);
        // Centro cross streets.
        pave(b, cx - 104, cz - 64, cx - 97, cz + 196, ground, p.ground(), p);
        pave(b, cx + 97, cz - 64, cx + 104, cz + 196, ground, p.ground(), p);
        pave(b, cx - 110, cz + 96, cx + 110, cz + 103, ground, p.ground(), p);
        pave(b, cx - 110, cz + 156, cx + 110, cz + 163, ground, p.ground(), p);
        // The Tiber embankment roads.
        pave(b, cx - 330, cz + 190, cx + 330, cz + 197, ground, p.ground(), p);
        pave(b, cx - 330, cz + 233, cx + 330, cz + 240, ground, p.ground(), p);
        // Trastevere's lanes.
        for (int x : TRAST_LANES) {
            pave(b, cx + x - 1, cz - 244, cx + x + 2, cz + 196, ground, p.ground(), p);
        }
        for (int z : TRAST_CROSS) {
            pave(b, cx - 320, cz + z - 1, cx - 140, cz + z + 2, ground, p.ground(), p);
        }
        // The Forum's paths.
        for (int x : FORUM_X) {
            pave(b, cx + x - 2, cz - 256, cx + x + 3, cz + 34, ground, p.foundation(), p);
        }
        for (int z : FORUM_Z) {
            pave(b, cx + 140, cz + z - 2, cx + 320, cz + z + 3, ground, p.foundation(), p);
        }
        // St Peter's square and its approach.
        pave(b, cx - 64, cz - 178, cx + 64, cz - 90, ground, p.ground(), p);
        // The green belt between Trastevere and the centro.
        pave(b, cx - 140, cz - 240, cx - 110, cz + 190, ground, p.ground(), p);
    }

    // ------------------------------------------------------------------ the Tiber

    private static void river(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        channel(b, cx - 330, cz + 200, cx + 330, cz + 230, ground, p);
        // Quay walls and parapets.
        b.fill(cx - 330, ground, cz + 198, cx + 330, ground + 1, cz + 199, p.foundation());
        b.fill(cx - 330, ground, cz + 231, cx + 330, ground + 1, cz + 232, p.foundation());
        b.fill(cx - 330, ground + 2, cz + 198, cx + 330, ground + 2, cz + 198, p.rail());
        b.fill(cx - 330, ground + 2, cz + 232, cx + 330, ground + 2, cz + 232, p.rail());

        // The main axis bridge and the Trastevere bridge.
        bridge(b, cx, cz, ground, p, 0, 7, 196, 234);
        bridge(b, cx, cz, ground, p, -240, 4, 196, 234);
        // Open the quay parapets where the two bridges land.
        openParapet(b, cx, cz, ground, -7, 7);
        openParapet(b, cx, cz, ground, -244, -236);

        // Boats on the water.
        boat(b, cx + 100, cz + 208, ground, p, 8);
        boat(b, cx - 104, cz + 220, ground, p, 6);
        boat(b, cx + 190, cz + 212, ground, p, 7);
        boat(b, cx - 286, cz + 216, ground, p, 5);

        // Reeds and bank rocks, authored.
        int[][] reeds = {{-300, 202}, {-296, 228}, {-60, 201}, {-54, 228},
                {130, 202}, {136, 228}, {286, 202}, {292, 228}};
        for (int[] reed : reeds) {
            b.put(cx + reed[0], ground - 1, cz + reed[1], GRASS);
        }
    }

    private static void bridge(StructureBuilder b, int cx, int cz, int ground, Palette p,
                               int xCenter, int half, int z0, int z1) {
        int x0 = cx + xCenter - half;
        int x1 = cx + xCenter + half;
        for (int z = cz + z0; z <= cz + z1; z++) {
            for (int x = x0; x <= x1; x++) {
                b.put(x, ground, z, p.foundation());
            }
        }
        for (int z = cz + z0; z <= cz + z1; z++) {
            b.put(x0, ground + 1, z, p.rail());
            b.put(x1, ground + 1, z, p.rail());
        }
        for (int z = cz + z0; z <= cz + z1; z += 6) {
            b.fill(x0, ground + 1, z, x0, ground + 2, z, p.accent());
            b.fill(x1, ground + 1, z, x1, ground + 2, z, p.accent());
        }
        for (int z = cz + z0 + 7; z <= cz + z1 - 7; z += 9) {
            for (int x = x0; x <= x1; x += 3) {
                b.fill(x, ground - 3, z, x, ground - 1, z, p.foundation());
            }
        }
        b.put(x0 + 1, ground + 2, cz + z0, p.light());
        b.put(x1 - 1, ground + 2, cz + z1, p.light());
    }

    private static void openParapet(StructureBuilder b, int cx, int cz, int ground,
                                    int x0, int x1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = 198; z <= 199; z++) {
                b.put(cx + x, ground + 1, cz + z, AIR);
                b.put(cx + x, ground + 2, cz + z, AIR);
            }
            for (int z = 231; z <= 232; z++) {
                b.put(cx + x, ground + 1, cz + z, AIR);
                b.put(cx + x, ground + 2, cz + z, AIR);
            }
        }
    }

    private static void boat(StructureBuilder b, int x, int z, int ground, Palette p, int length) {
        b.fill(x, ground - 1, z, x + length, ground - 1, z + 3, DECK);
        b.walls(x, ground, z, x + length, ground + 1, z + 3, p.accent());
        b.put(x + 1, ground + 2, z + 1, p.light());
        b.put(x + length - 1, ground + 2, z + 2, p.light());
    }

    // ------------------------------------------------------------------ arrival gate

    private static void arrival(StructureBuilder b, int cx, int cz, Palette p,
                                IntBinaryOperator surface) {
        int gy = surface.applyAsInt(cx, cz + 320);
        paveAt(b, cx - 42, cz + 300, cx + 42, cz + 338, surface, p.ground());
        // Two travertine bastions.
        for (int s = -1; s <= 1; s += 2) {
            int x0 = cx + s * 22;
            int x1 = cx + s * 12;
            pad(b, x0, cz + 312, x1, cz + 326, gy, p);
            b.fill(x0, gy + 1, cz + 312, x1, gy + 14, cz + 326, p.wall());
            b.fill(x0, gy + 7, cz + 312, x1, gy + 7, cz + 326, p.accent());
            b.crenellations(x0, cz + 312, x1, cz + 326, gy + 15);
            b.put(x0, gy + 9, cz + 312, p.light());
        }
        // Lintel and a tiled cap over the passage.
        b.fill(cx - 12, gy + 10, cz + 314, cx + 12, gy + 12, cz + 324, p.accent());
        b.pitchedRoof(cx - 24, cz + 310, cx + 24, cz + 328, gy + 13, 4, 0);
        b.fill(cx - 6, gy + 1, cz + 314, cx + 6, gy + 9, cz + 326, AIR);
        b.put(cx - 8, gy + 6, cz + 313, p.light());
        b.put(cx + 8, gy + 6, cz + 313, p.light());
        // Guardian obelisks and a pair of fountains.
        b.monument(cx - 34, cz + 332, gy);
        b.monument(cx + 34, cz + 332, gy);
        fountain(b, cx - 30, cz + 306, gy, p, 4);
        fountain(b, cx + 30, cz + 306, gy, p, 4);
        signAt(b, cx + 8, gy + 1, cz + 316, Direction.SOUTH, "ROME",
                "ST PETER'S  N", "FORUM  NE", "TRASTEVERE  W");
        signAt(b, cx - 8, gy + 1, cz + 316, Direction.SOUTH, "CENTRO  N",
                "CATACOMBS  N", "TIBER  N", "CITY GATE");
    }

    // ------------------------------------------------------------------ Vatican

    private static void vatican(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        square(b, cx, cz, ground, p);
        basilica(b, cx, cz, ground, p);
        palace(b, cx, cz, ground, p);
        vaticanGardens(b, cx, cz, ground, p);
    }

    private static void square(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        b.monument(cx, cz - 132, ground);
        fountain(b, cx - 26, cz - 132, ground, p, 5);
        fountain(b, cx + 26, cz - 132, ground, p, 5);
        colonnade(b, cx, cz, ground, p);
    }

    private static void colonnade(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int r = 52;
        int prevX = 0;
        int prevZ = 0;
        boolean has = false;
        for (int deg = 12; deg <= 168; deg += 4) {
            if (deg >= 82 && deg <= 98) {
                has = false; // the entrance gap on the main axis
                continue;
            }
            double t = Math.toRadians(deg);
            int x = cx + (int) Math.round(Math.cos(t) * r);
            int z = cz - 138 + (int) Math.round(Math.sin(t) * r);
            b.fill(x, ground + 1, z, x, ground + 9, z, p.accent());
            b.put(x, ground + 10, z, p.frame());
            if (has) {
                b.fill(Math.min(prevX, x), ground + 10, Math.min(prevZ, z),
                        Math.max(prevX, x), ground + 10, Math.max(prevZ, z), p.foundation());
                b.fill(Math.min(prevX, x), ground + 11, Math.min(prevZ, z),
                        Math.max(prevX, x), ground + 11, Math.max(prevZ, z), p.roofSlab());
            }
            prevX = x;
            prevZ = z;
            has = true;
        }
    }

    private static void basilica(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Platform and nave.
        pad(b, cx - 40, cz - 256, cx + 40, cz - 174, ground, p);
        // Steps up to the façade, laid after the pad so they survive it.
        for (int i = 0; i < 3; i++) {
            b.fill(cx - 24, ground + i, cz - 180 + i, cx + 24, ground + i, cz - 178 + i,
                    p.foundation());
        }
        b.fill(cx - 22, ground + 1, cz - 250, cx + 22, ground + 22, cz - 178, p.wall());
        b.fill(cx - 18, ground + 1, cz - 246, cx + 18, ground + 22, cz - 182, AIR);
        b.fill(cx - 18, ground + 1, cz - 246, cx + 18, ground + 1, cz - 182, p.ground());
        b.fill(cx - 22, ground + 23, cz - 252, cx + 22, ground + 23, cz - 176, p.roof());
        b.pitchedRoof(cx - 24, cz - 252, cx + 24, cz - 176, ground + 24, 7, 1);

        // Interior colonnade and apse.
        for (int x = -10; x <= 10; x += 20) {
            for (int z = cz - 240; z <= cz - 190; z += 10) {
                column(b, cx + x, z, ground + 2, 15, p);
            }
        }
        b.fill(cx - 14, ground + 1, cz - 256, cx + 14, ground + 10, cz - 250, p.wall());
        b.fill(cx - 4, ground + 1, cz - 254, cx + 4, ground + 5, cz - 252, p.frame());
        b.put(cx, ground + 6, cz - 253, p.light());

        // The façade, its columns and pediment.
        b.fill(cx - 24, ground + 1, cz - 180, cx + 24, ground + 24, cz - 178, p.wall());
        b.fill(cx - 5, ground + 1, cz - 179, cx + 5, ground + 11, cz - 177, AIR);
        for (int x = -18; x <= 18; x += 6) {
            column(b, cx + x, cz - 179, ground + 2, 18, p);
        }
        b.fill(cx - 24, ground + 20, cz - 180, cx + 24, ground + 21, cz - 177, p.accent());
        for (int i = 0; i <= 10; i++) {
            b.fill(cx - 12 + i, ground + 22 + i, cz - 180,
                    cx + 12 - i, ground + 22 + i, cz - 177, p.wall());
        }
        // Windows along the clerestory.
        for (int z = cz - 244; z <= cz - 188; z += 8) {
            b.window(cx - 22, ground + 14, z, 4, 2, true);
            b.window(cx + 22, ground + 14, z, 4, 2, true);
        }

        // The dome: drum, shell, lantern and cross.
        dome(b, cx, cz - 212, ground + 23, 13, p);
        int top = ground + 47;
        b.put(cx, top, cz - 212, p.accent());
        b.put(cx, top + 1, cz - 212, p.accent());
        b.fill(cx - 1, top + 1, cz - 212, cx + 1, top + 1, cz - 212, p.accent());
        b.put(cx, top + 2, cz - 212, p.light());
    }

    private static void dome(StructureBuilder b, int cx, int cz, int baseY, int radius,
                             Palette p) {
        for (int i = 0; i < 6; i++) {
            ring(b, cx, cz, radius, baseY + i, (i & 1) == 0 ? p.wall() : p.accent());
        }
        for (int k = 0; k < 8; k++) {
            double t = k * Math.PI / 4.0;
            int x = cx + (int) Math.round(Math.cos(t) * radius);
            int z = cz + (int) Math.round(Math.sin(t) * radius);
            b.put(x, baseY + 2, z, p.window());
            b.put(x, baseY + 3, z, p.window());
        }
        int domeBase = baseY + 6;
        for (int i = 0; i <= radius; i++) {
            int r = radius - i;
            int y = domeBase + i;
            if (r <= 0) {
                b.put(cx, y, cz, p.accent());
                break;
            }
            ring(b, cx, cz, r, y, p.roofSlab());
        }
        int top = domeBase + radius;
        b.fill(cx - 1, top + 1, cz - 1, cx + 1, top + 3, cz + 1, p.wall());
        b.put(cx, top + 4, cz, p.light());
    }

    private static void ring(StructureBuilder b, int cx, int cz, int r, int y, BlockState state) {
        if (r <= 0) {
            b.put(cx, y, cz, state);
            return;
        }
        for (int x = cx - r; x <= cx + r; x++) {
            for (int z = cz - r; z <= cz + r; z++) {
                int dx = x - cx;
                int dz = z - cz;
                int d2 = dx * dx + dz * dz;
                double lo = (r - 0.55) * (r - 0.55);
                double hi = (r + 0.55) * (r + 0.55);
                if (d2 >= lo && d2 <= hi) {
                    b.put(x, y, z, state);
                }
            }
        }
    }

    private static void palace(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Three wings around a courtyard.
        pad(b, cx - 138, cz - 292, cx - 64, cz - 236, ground, p);
        wing(b, cx - 138, cz - 292, cx - 64, cz - 278, ground, p, 16);
        wing(b, cx - 138, cz - 292, cx - 124, cz - 236, ground, p, 16);
        wing(b, cx - 78, cz - 292, cx - 64, cz - 236, ground, p, 16);
        // Courtyard.
        pave(b, cx - 122, cz - 276, cx - 80, cz - 238, ground, p.ground(), p);
        fountain(b, cx - 101, cz - 257, ground, p, 4);
        cypress(b, cx - 118, cz - 266, ground, p);
        cypress(b, cx - 84, cz - 266, ground, p);
        signAt(b, cx - 101, ground + 1, cz - 236, Direction.SOUTH, "VATICAN", "PALACE");
    }

    private static void wing(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                             Palette p, int height) {
        b.fill(x0, ground + 1, z0, x1, ground + height, z1, p.wall());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + height, z1 - 1, AIR);
        b.fill(x0, ground + height + 1, z0, x1, ground + height + 1, z1, p.roofSlab());
        b.crenellations(x0, z0, x1, z1, ground + height + 2);
        for (int y = ground + 3; y <= ground + height - 2; y += 4) {
            for (int x = x0 + 3; x <= x1 - 3; x += 5) {
                b.window(x, y, z0, 2, 2, true);
                b.window(x, y, z1, 2, 2, true);
            }
            for (int z = z0 + 3; z <= z1 - 3; z += 5) {
                b.window(x0, y, z, 2, 1, true);
                b.window(x1, y, z, 2, 1, true);
            }
        }
        b.fill(x0, ground + 2, z0, x1, ground + 2, z1, p.accent());
    }

    private static void vaticanGardens(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        pave(b, cx + 64, cz - 292, cx + 138, cz - 222, ground, p.ground(), p);
        // Low hedged parterres.
        for (int i = 0; i < 3; i++) {
            int x0 = cx + 70 + i * 22;
            b.walls(x0, ground + 1, cz - 286, x0 + 16, ground + 2, cz - 238, p.weathered());
            b.fill(x0 + 2, ground + 1, cz - 282, x0 + 14, ground + 1, cz - 242, GRASS);
            b.put(x0 + 8, ground + 1, cz - 262, p.accent());
            b.put(x0 + 8, ground + 2, cz - 262, p.light());
        }
        fountain(b, cx + 101, cz - 228, ground, p, 3);
        signAt(b, cx + 101, ground + 1, cz - 222, Direction.SOUTH, "VATICAN", "GARDENS");
    }

    // ------------------------------------------------------------------ Forum

    private static void forum(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        pave(b, cx + 144, cz - 256, cx + 316, cz + 36, ground, p.ground(), p);
        // The Via Sacra and its cross paths, in worn travertine.
        for (int x : FORUM_X) {
            pave(b, cx + x - 2, cz - 256, cx + x + 3, cz + 34, ground, p.foundation(), p);
        }
        for (int z : FORUM_Z) {
            pave(b, cx + 140, cz + z - 2, cx + 320, cz + z + 3, ground, p.foundation(), p);
        }

        temple(b, cx + 220, cz - 141, ground, p, 30, 30, 18, true);
        temple(b, cx + 180, cz - 180, ground, p, 28, 28, 14, true);
        triumphalArch(b, cx + 220, cz - 90, ground, p, 8, 14);
        triumphalArch(b, cx + 160, cz - 16, ground, p, 7, 12);
        roundTemple(b, cx + 260, cz - 100, ground, p, 10);
        basilicaMaxentius(b, cx + 302, cz - 180, ground, p);
        trajanColumn(b, cx + 220, cz + 18, ground, p);

        // Broken columns and fallen drums, authored.
        for (int[] c : FORUM_COLUMNS) {
            column(b, cx + c[0], cz + c[1], ground + 1, c[2], p);
        }
        fallenColumn(b, cx + 220, cz - 70, ground, p, 9);
        fallenColumn(b, cx + 290, cz + 12, ground, p, 7);
        fallenColumn(b, cx + 186, cz - 104, ground, p, 6);
        b.fill(cx + 206, ground + 1, cz - 112, cx + 218, ground + 2, cz - 100, p.rubble());
        b.fill(cx + 148, ground + 1, cz + 18, cx + 156, ground + 2, cz + 28, p.rubble());
        b.fill(cx + 296, ground + 1, cz - 132, cx + 306, ground + 3, cz - 124, p.rubble());
    }

    private static void temple(StructureBuilder b, int cx, int cz, int ground, Palette p,
                               int width, int depth, int colH, boolean ruined) {
        int hw = width / 2;
        int hd = depth / 2;
        // Podium and front steps.
        b.fill(cx - hw, ground, cz - hd, cx + hw, ground + 2, cz + hd, p.foundation());
        for (int i = 0; i < 3; i++) {
            b.fill(cx - 6 - i, ground + i, cz + hd + 1, cx + 6 + i, ground + i, cz + hd + 3,
                    p.foundation());
        }
        // Colonnade: front, back and sides.
        for (int x = cx - hw + 3; x <= cx + hw - 3; x += 5) {
            int front = ruined && (x - cx) % 10 == 0 ? colH - 8 : colH;
            column(b, x, cz - hd + 2, ground + 3, front, p);
            column(b, x, cz + hd - 2, ground + 3, colH, p);
        }
        for (int z = cz - hd + 7; z <= cz + hd - 7; z += 6) {
            column(b, cx - hw + 2, z, ground + 3, colH, p);
            column(b, cx + hw - 2, z, ground + 3, colH, p);
        }
        // Entablature and pediment.
        b.fill(cx - hw, ground + colH + 3, cz - hd, cx + hw, ground + colH + 4, cz + hd, p.wall());
        b.fill(cx - hw, ground + colH + 5, cz - hd, cx + hw, ground + colH + 5, cz + hd,
                p.roofSlab());
        for (int i = 0; i <= hw; i++) {
            b.fill(cx - hw + i, ground + colH + 5 + i, cz + hd,
                    cx + hw - i, ground + colH + 5 + i, cz + hd, p.wall());
        }
        b.pitchedRoof(cx - hw - 1, cz - hd - 1, cx + hw + 1, cz + hd + 1,
                ground + colH + 6, 4, 0);
        if (ruined) {
            // A collapsed corner and a broken back wall.
            b.fill(cx + hw - 6, ground + 1, cz - hd, cx + hw, ground + 8, cz - hd + 4, AIR);
            b.fill(cx - hw, ground + 1, cz + hd - 4, cx - hw + 4, ground + 6, cz + hd, AIR);
            b.fill(cx - hw + 2, ground + 1, cz - hd + 2, cx + hw - 2, ground + 2, cz - hd + 4,
                    p.rubble());
        }
    }

    private static void roundTemple(StructureBuilder b, int cx, int cz, int ground, Palette p,
                                    int r) {
        pad(b, cx - r - 2, cz - r - 2, cx + r + 2, cz + r + 2, ground, p);
        for (int deg = 0; deg < 360; deg += 30) {
            double t = Math.toRadians(deg);
            int x = cx + (int) Math.round(Math.cos(t) * r);
            int z = cz + (int) Math.round(Math.sin(t) * r);
            column(b, x, z, ground + 2, 9, p);
        }
        ring(b, cx, cz, r - 4, ground + 1, p.wall());
        ring(b, cx, cz, r - 4, ground + 2, p.wall());
        for (int i = 0; i < 8; i++) {
            ring(b, cx, cz, r - i, ground + 12 + i, p.roofSlab());
        }
        b.put(cx, ground + 19, cz, p.accent());
        b.fill(cx - 1, ground + 1, cz - r + 4, cx + 1, ground + 4, cz - r + 4, AIR);
    }

    private static void triumphalArch(StructureBuilder b, int cx, int cz, int ground, Palette p,
                                      int halfW, int height) {
        // Piers and attic.
        b.fill(cx - halfW, ground, cz - 3, cx - halfW + 3, ground + height, cz + 3, p.wall());
        b.fill(cx + halfW - 3, ground, cz - 3, cx + halfW, ground + height, cz + 3, p.wall());
        b.fill(cx - halfW - 1, ground + height + 1, cz - 3, cx + halfW + 1, ground + height + 4,
                cz + 3, p.wall());
        b.fill(cx - halfW - 1, ground + height + 5, cz - 3, cx + halfW + 1, ground + height + 5,
                cz + 3, p.roofSlab());
        // Engaged columns on both faces.
        for (int x = -halfW + 1; x <= halfW - 1; x += halfW - 2) {
            b.fill(cx + x, ground + 1, cz - 4, cx + x, ground + height - 2, cz - 4, p.accent());
            b.fill(cx + x, ground + 1, cz + 4, cx + x, ground + height - 2, cz + 4, p.accent());
        }
        // The opening, stepped into an arch.
        int open = halfW - 4;
        b.fill(cx - open, ground + 1, cz - 4, cx + open, ground + height - 4, cz + 4, AIR);
        for (int i = 1; i < 4; i++) {
            b.fill(cx - open + i, ground + height - 4 + i, cz - 4,
                    cx + open - i, ground + height - 4 + i, cz + 4, AIR);
        }
        b.put(cx, ground + height + 3, cz - 4, p.light());
        b.put(cx, ground + height + 3, cz + 4, p.light());
    }

    private static void basilicaMaxentius(StructureBuilder b, int cx, int cz, int ground,
                                          Palette p) {
        int x0 = cx - 16;
        int x1 = cx + 16;
        int z0 = cz - 14;
        int z1 = cz + 14;
        pad(b, x0, z0, x1, z1, ground, p);
        // Three tall bays with piers.
        for (int x = x0; x <= x1; x += 11) {
            b.fill(x, ground + 1, z0, x + 2, ground + 20, z1, p.wall());
        }
        for (int x = x0 + 3; x <= x1 - 3; x += 11) {
            b.fill(x, ground + 1, z0, x + 7, ground + 18, z1, AIR);
            b.fill(x, ground + 19, z0, x + 7, ground + 19, z1, p.roofSlab());
        }
        // The ruined east end.
        b.fill(x1 - 8, ground + 12, z0, x1, ground + 20, z1, AIR);
        b.fill(x1 - 10, ground + 1, z0, x1, ground + 2, z1, p.rubble());
        b.fill(x0, ground + 20, z0, x1, ground + 21, z1, p.roof());
        b.fill(x0, ground + 1, z1, x1, ground + 1, z1, p.foundation());
    }

    private static void trajanColumn(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        b.fill(cx - 2, ground, cz - 2, cx + 2, ground + 2, cz + 2, p.foundation());
        b.fill(cx - 1, ground + 3, cz - 1, cx + 1, ground + 3, cz + 1, p.accent());
        for (int y = ground + 4; y <= ground + 28; y++) {
            b.put(cx, y, cz, (y & 1) == 0 ? p.accent() : p.frame());
        }
        b.put(cx, ground + 29, cz, p.light());
        b.fill(cx - 1, ground + 30, cz - 1, cx + 1, ground + 30, cz + 1, p.roofSlab());
    }

    private static void fallenColumn(StructureBuilder b, int x, int z, int ground, Palette p,
                                     int length) {
        b.fill(x, ground + 1, z, x + length, ground + 1, z, p.accent());
        b.fill(x + 2, ground + 2, z, x + length - 2, ground + 2, z, p.accent());
        b.put(x + 1, ground + 2, z, p.frame());
        b.put(x + length, ground + 2, z, p.frame());
    }

    // ------------------------------------------------------------------ Trastevere

    private static void trastevere(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        for (int i = 0; i < TRAST_X.length; i++) {
            for (int j = 0; j < TRAST_Z.length; j++) {
                int bx = cx + TRAST_X[i];
                int bz = cz + TRAST_Z[j];
                if (i == 1 && j == 3) {
                    church(b, cx - 240, cz, ground, p);
                    continue;
                }
                if (i == 1 && j == 5) {
                    apothecary(b, cx - 240, cz + 120, ground, p);
                    continue;
                }
                if (i == 2 && j == 4) {
                    trattoria(b, cx - 200, cz + 70, ground, p);
                    continue;
                }
                if (i == 0 && j == 0) {
                    walledGarden(b, bx + 4, bz + 10, ground, p);
                    continue;
                }
                if (i == 3 && j == 1) {
                    laundryCourt(b, bx + 4, bz + 12, ground, p);
                    continue;
                }
                if (i == 3 && j == 5) {
                    ruinHouse(b, bx + 4, bz + 10, ground, p);
                    continue;
                }
                trastevereHouse(b, bx + 2, bz + 7, ground, p, 2 + (i + j) % 2, (i * 3 + j) % 3);
                trastevereHouse(b, bx + 16, bz + 7, ground, p, 2 + (i + j + 1) % 2,
                        (i * 5 + j) % 3);
                courtyard(b, bx + 4, bz + 28, ground, p, (i * 3 + j) % 4);
            }
        }
    }

    private static BlockState plaster(int v) {
        return switch (v) {
            case 0 -> OCHRE;
            case 1 -> PLASTER;
            default -> TERRACOTTA;
        };
    }

    private static void trastevereHouse(StructureBuilder b, int x, int z, int ground, Palette p,
                                        int storeys, int tint) {
        int w = 12;
        int d = 12;
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y1 = ground + storeys * 4 + 1;
        BlockState wall = plaster(tint);
        b.fill(x, ground, z, x1, ground, z1, p.foundation());
        b.walls(x, ground + 1, z, x1, y1 - 1, z1, wall);
        b.fill(x + 1, ground + 1, z + 1, x1 - 1, y1 - 1, z1 - 1, AIR);
        b.fill(x, y1, z, x1, y1, z1, p.roof());
        b.fill(x, y1 - 1, z, x1, y1 - 1, z1, p.accent());
        // Quoins and windows.
        for (int y = ground + 1; y < y1; y++) {
            b.put(x, y, z, p.accent());
            b.put(x1, y, z, p.accent());
            b.put(x, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
        for (int wy = ground + 3; wy < y1 - 1; wy += 4) {
            for (int wx = x + 2; wx < x1 - 1; wx += 4) {
                b.window(wx, wy, z, 2, 2, true);
                b.window(wx, wy, z1, 2, 2, true);
            }
        }
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, y1 + 1, 3 + tint % 3, w >= d ? 0 : 1);
        placeDoor(b, p, x, z, x1, z1, ground + 1, Side.S, 6);
        b.put(x + 1, ground + 2, z1, p.light());
        // A laundry line under one eave, on some houses.
        if (tint == 0) {
            b.fill(x + 2, ground + 1, z1 + 1, x + 2, ground + 4, z1 + 1, p.accent());
            b.fill(x + 9, ground + 1, z1 + 1, x + 9, ground + 4, z1 + 1, p.accent());
            b.fill(x + 2, ground + 4, z1 + 1, x + 9, ground + 4, z1 + 1, p.rail());
            b.put(x + 4, ground + 3, z1 + 1, WHITE_WOOL);
            b.put(x + 7, ground + 3, z1 + 1, RED_WOOL);
        }
    }

    private static void courtyard(StructureBuilder b, int x, int z, int ground, Palette p,
                                  int kind) {
        pave(b, x, z, x + 20, z + 20, ground, p.ground(), p);
        switch (kind) {
            case 0 -> {
                b.put(x + 2, ground + 1, z + 2, CAULDRON);
                b.put(x + 17, ground + 1, z + 2, CAULDRON);
                b.fill(x + 5, ground + 1, z + 4, x + 5, ground + 5, z + 4, p.accent());
                b.fill(x + 15, ground + 1, z + 4, x + 15, ground + 5, z + 4, p.accent());
                b.fill(x + 5, ground + 5, z + 4, x + 15, ground + 5, z + 4, p.rail());
                b.put(x + 5, ground + 6, z + 4, p.accent());
                b.put(x + 15, ground + 6, z + 4, p.accent());
                b.put(x + 8, ground + 4, z + 4, WHITE_WOOL);
                b.put(x + 12, ground + 4, z + 4, RED_WOOL);
            }
            case 1 -> {
                b.walls(x + 2, ground + 1, z + 2, x + 18, ground + 2, z + 18, p.weathered());
                b.fill(x + 9, ground + 1, z + 18, x + 11, ground + 2, z + 18, AIR);
                b.fill(x + 4, ground + 1, z + 4, x + 16, ground + 1, z + 16, GRASS);
                cypress(b, x + 7, z + 8, ground, p);
                b.put(x + 13, ground + 1, z + 12, POPPY);
                b.put(x + 12, ground + 1, z + 8, DANDELION);
            }
            case 2 -> {
                table(b, x + 6, z + 6, ground, p);
                table(b, x + 14, z + 6, ground, p);
                table(b, x + 10, z + 14, ground, p);
                b.put(x + 18, ground + 1, z + 16, BARREL);
            }
            default -> {
                b.put(x + 3, ground + 1, z + 3, BARREL);
                b.put(x + 4, ground + 1, z + 3, BARREL);
                b.put(x + 16, ground + 1, z + 15, HAY);
                b.put(x + 3, ground + 1, z + 16, CAULDRON);
                b.put(x + 10, ground + 1, z + 10, p.light());
            }
        }
    }

    private static void church(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Forecourt.
        pave(b, cx - 16, cz - 24, cx + 16, cz + 20, ground, p.ground(), p);
        // Nave.
        int x0 = cx - 14;
        int x1 = cx + 14;
        int z0 = cz - 18;
        int z1 = cz + 18;
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 14, z1, p.wall());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 14, z1 - 1, AIR);
        b.fill(x0, ground + 15, z0, x1, ground + 15, z1, p.roof());
        b.pitchedRoof(x0 - 2, z0 - 1, x1 + 2, z1 + 1, ground + 16, 6, 1);
        // Façade: door, rose window, pilasters.
        b.fill(cx - 3, ground + 1, z1, cx + 3, ground + 6, z1, AIR);
        b.fill(cx - 6, ground + 9, z1, cx + 6, ground + 13, z1, p.window());
        b.fill(cx - 7, ground + 8, z1, cx + 7, ground + 8, z1, p.frame());
        b.fill(cx - 7, ground + 14, z1, cx + 7, ground + 14, z1, p.frame());
        for (int x = -12; x <= 12; x += 6) {
            b.fill(cx + x, ground + 1, z1, cx + x, ground + 14, z1, p.accent());
        }
        // Campanile.
        int tx = cx - 18;
        int tz = cz + 10;
        b.fill(tx - 3, ground, tz - 3, tx + 3, ground + 26, tz + 3, p.wall());
        b.fill(tx - 2, ground + 20, tz - 2, tx + 2, ground + 24, tz + 2, AIR);
        b.put(tx, ground + 22, tz, BELL);
        b.fill(tx - 4, ground + 27, tz - 4, tx + 4, ground + 27, tz + 4, p.roofSlab());
        b.pitchedRoof(tx - 4, tz - 4, tx + 4, tz + 4, ground + 28, 4, 0);
        // Interior: altar, candles, pews.
        b.fill(cx - 4, ground + 1, cz - 16, cx + 4, ground + 2, cz - 12, p.accent());
        b.put(cx, ground + 3, cz - 14, p.light());
        b.put(cx - 3, ground + 3, cz - 14, CANDLE);
        b.put(cx + 3, ground + 3, cz - 14, CANDLE);
        for (int z = cz - 4; z <= cz + 8; z += 4) {
            b.fill(cx - 6, ground + 1, z, cx - 2, ground + 1, z, p.roofSlab());
            b.fill(cx + 2, ground + 1, z, cx + 6, ground + 1, z, p.roofSlab());
        }
        signAt(b, cx - 10, ground + 1, cz + 19, Direction.SOUTH, "S. MARIA", "IN TRASTEVERE");
    }

    private static void apothecary(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 12;
        int z0 = cz - 8;
        int x1 = cx + 12;
        int z1 = cz + 8;
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 9, z1, p.wall());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 9, z1 - 1, AIR);
        b.fill(x0, ground + 10, z0, x1, ground + 10, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 11, 4, 0);
        for (int wy = ground + 3; wy <= ground + 7; wy += 4) {
            for (int wx = x0 + 2; wx <= x1 - 2; wx += 4) {
                b.window(wx, wy, z0, 2, 2, true);
                b.window(wx, wy, z1, 2, 2, true);
            }
        }
        placeDoor(b, p, x0, z0, x1, z1, ground + 1, Side.S, 12);
        // Counter, shelves and herb boxes.
        b.fill(x0 + 2, ground + 1, z0 + 2, x0 + 5, ground + 2, z0 + 6, p.accent());
        b.fill(x0 + 7, ground + 1, z0 + 1, x1 - 1, ground + 3, z0 + 1, BOOKSHELF);
        b.put(x0 + 2, ground + 1, z1 + 1, p.overgrowth() != null ? p.overgrowth() : GRASS);
        b.put(x0 + 10, ground + 1, z1 + 1, POPPY);
        b.put(x0 + 11, ground + 1, z1 + 1, DANDELION);
        signAt(b, cx - 8, ground + 1, z1 + 1, Direction.SOUTH, "APOTHECARY", "REMEDIES");
    }

    private static void trattoria(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        pave(b, cx - 14, cz - 20, cx + 14, cz + 20, ground, p.ground(), p);
        // Kitchen block at the north end.
        int x0 = cx - 10;
        int z0 = cz - 18;
        int x1 = cx + 10;
        int z1 = cz - 6;
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 6, z1, plaster(1));
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 6, z1 - 1, AIR);
        b.fill(x0, ground + 7, z0, x1, ground + 7, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 8, 3, 0);
        b.fill(cx - 1, ground + 1, z1, cx + 1, ground + 3, z1, AIR);
        b.put(cx, ground + 1, z0 + 2, p.light());
        b.put(x0 + 2, ground + 1, z0 + 2, BARREL);
        b.put(x0 + 3, ground + 1, z0 + 2, CAULDRON);
        // Pergola and tables in the courtyard.
        for (int x = -10; x <= 10; x += 5) {
            b.fill(cx + x, ground + 1, cz + 2, cx + x, ground + 5, cz + 2, p.accent());
            b.fill(cx + x, ground + 1, cz + 14, cx + x, ground + 5, cz + 14, p.accent());
        }
        b.fill(cx - 10, ground + 5, cz + 2, cx + 10, ground + 5, cz + 14, p.rail());
        BlockState leaf = p.overgrowth() != null ? p.overgrowth() : PINE_LEAVES;
        b.fill(cx - 10, ground + 6, cz + 2, cx + 10, ground + 6, cz + 14, leaf);
        table(b, cx - 5, cz + 7, ground, p);
        table(b, cx + 5, cz + 7, ground, p);
        table(b, cx, cz + 12, ground, p);
        signAt(b, cx, ground + 1, cz + 18, Direction.SOUTH, "TRATTORIA", "OPEN LATE");
    }

    private static void walledGarden(StructureBuilder b, int x, int z, int ground, Palette p) {
        pave(b, x, z, x + 22, z + 24, ground, p.ground(), p);
        b.walls(x, ground + 1, z, x + 22, ground + 3, z + 24, p.weathered());
        b.fill(x + 10, ground + 1, z + 24, x + 12, ground + 3, z + 24, AIR);
        b.fill(x + 2, ground + 1, z + 2, x + 20, ground + 1, z + 22, GRASS);
        cypress(b, x + 6, z + 6, ground, p);
        cypress(b, x + 16, z + 6, ground, p);
        umbrellaPine(b, x + 11, z + 16, ground, p);
        b.put(x + 4, ground + 1, z + 18, POPPY);
        b.put(x + 18, ground + 1, z + 18, DANDELION);
        fountain(b, x + 11, z + 11, ground, p, 3);
    }

    private static void laundryCourt(StructureBuilder b, int x, int z, int ground, Palette p) {
        pave(b, x, z, x + 22, z + 20, ground, p.ground(), p);
        for (int i = 0; i < 3; i++) {
            int zz = z + 4 + i * 6;
            b.fill(x + 2, ground + 1, zz, x + 2, ground + 6, zz, p.accent());
            b.fill(x + 20, ground + 1, zz, x + 20, ground + 6, zz, p.accent());
            b.fill(x + 2, ground + 6, zz, x + 20, ground + 6, zz, p.rail());
            b.put(x + 5, ground + 5, zz, WHITE_WOOL);
            b.put(x + 9, ground + 5, zz, RED_WOOL);
            b.put(x + 14, ground + 5, zz, WHITE_WOOL);
            b.put(x + 18, ground + 5, zz, RED_WOOL);
        }
        b.put(x + 2, ground + 1, z + 18, CAULDRON);
        b.put(x + 3, ground + 1, z + 18, CAULDRON);
    }

    private static void ruinHouse(StructureBuilder b, int x, int z, int ground, Palette p) {
        int x1 = x + 18;
        int z1 = z + 18;
        b.fill(x, ground, z, x1, ground, z1, p.foundation());
        b.walls(x, ground + 1, z, x1, ground + 8, z1, p.weathered());
        b.fill(x + 1, ground + 1, z + 1, x1 - 1, ground + 8, z1 - 1, AIR);
        // Collapsed south and east walls, roof gone.
        b.fill(x + 4, ground + 4, z1, x + 12, ground + 8, z1, AIR);
        b.fill(x1, ground + 3, z + 5, x1, ground + 8, z + 14, AIR);
        b.fill(x + 2, ground + 1, z + 2, x + 8, ground + 2, z + 8, p.rubble());
        b.put(x + 3, ground + 3, z + 3, COBWEB);
        b.put(x + 14, ground + 5, z + 3, COBWEB);
        b.fill(x + 2, ground + 1, z1 + 1, x + 5, ground + 3, z1 + 1, BOARD);
        signAt(b, x + 9, ground + 1, z1 + 1, Direction.SOUTH, "CLOSED", "FOR REPAIRS");
    }

    // ------------------------------------------------------------------ centro

    private static void centro(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        piazzaNavona(b, cx, cz, ground, p);
        // The Pantheon's small piazza, laid before the building so no paving can cut its walls.
        pave(b, cx + 58, cz - 34, cx + 100, cz - 2, ground, p.ground(), p);
        b.monument(cx + 54, cz - 12, ground);
        pantheon(b, cx + 84, cz - 20, ground, p);
        questBoard(b, cx, cz + 60, ground, p);
        catacombEntrance(b, cx - 18, cz + 80, ground, p);
        albergo(b, cx - 80, cz - 120, ground, p);
        shop(b, cx - 40, cz + 120, ground, p);

        for (int[] lot : CENTRO_LOTS) {
            centroBuilding(b, cx + lot[0], cz + lot[1], ground, p, lot[2], lot[3], lot[4],
                    lot[5]);
        }
        // Piazza del Popolo, at the river end of the axis.
        pave(b, cx - 30, cz + 168, cx + 30, cz + 190, ground, p.ground(), p);
        b.monument(cx + 18, cz + 184, ground);
        fountain(b, cx - 18, cz + 178, ground, p, 3);
        fountain(b, cx - 18, cz + 186, ground, p, 3);
    }

    private static void piazzaNavona(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        pave(b, cx - 42, cz - 8, cx + 42, cz + 56, ground, p.ground(), p);
        // The long fountain and its obelisk, east of the main axis.
        b.fill(cx + 12, ground, cz + 18, cx + 24, ground, cz + 32, p.foundation());
        b.fill(cx + 13, ground, cz + 19, cx + 23, ground, cz + 31, WATER);
        b.monument(cx + 18, cz + 25, ground);
        b.put(cx + 13, ground + 1, cz + 25, p.accent());
        b.put(cx + 23, ground + 1, cz + 25, p.accent());
        // Cafe tables and market stalls around the piazza.
        for (int[] t : CAFE_TABLES) {
            if (t[0] > -50 && t[0] < 50 && t[1] >= 0 && t[1] <= 50) {
                table(b, cx + t[0], cz + t[1], ground, p);
            }
        }
        marketStall(b, cx - 30, cz + 2, ground, p);
        marketStall(b, cx + 26, cz + 48, ground, p);
        b.put(cx - 38, ground + 1, cz + 52, BARREL);
        b.put(cx - 36, ground + 1, cz + 52, BARREL);
    }

    private static void pantheon(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Rotunda wall, two blocks thick.
        ring(b, cx, cz, 11, ground + 1, p.wall());
        ring(b, cx, cz, 10, ground + 1, p.wall());
        for (int y = ground + 2; y <= ground + 14; y++) {
            ring(b, cx, cz, 11, y, p.wall());
            ring(b, cx, cz, 10, y, p.wall());
        }
        b.fill(cx - 9, ground + 1, cz - 9, cx + 9, ground + 14, cz + 9, AIR);
        b.fill(cx - 9, ground + 1, cz - 9, cx + 9, ground + 1, cz + 9, p.ground());
        // The oculus dome.
        for (int i = 0; i <= 10; i++) {
            int r = 10 - i;
            if (r <= 0) {
                break;
            }
            ring(b, cx, cz, r, ground + 15 + i, p.roofSlab());
        }
        b.put(cx, ground + 1, cz, p.light());
        // Portico and pediment, facing west.
        for (int x = cx - 22; x <= cx - 12; x += 5) {
            column(b, x, cz - 6, ground + 1, 12, p);
            column(b, x, cz + 6, ground + 1, 12, p);
        }
        b.fill(cx - 23, ground + 14, cz - 8, cx - 11, ground + 15, cz + 8, p.wall());
        b.fill(cx - 23, ground + 16, cz - 8, cx - 11, ground + 16, cz + 8, p.roofSlab());
        for (int i = 0; i <= 6; i++) {
            b.fill(cx - 23 + i, ground + 16 + i, cz - 8, cx - 11 - i, ground + 16 + i, cz + 8,
                    p.wall());
        }
        b.pitchedRoof(cx - 23, cz - 8, cx - 11, cz + 8, ground + 17, 3, 0);
        b.fill(cx - 11, ground + 1, cz - 2, cx - 9, ground + 6, cz + 2, AIR);
        signAt(b, cx - 24, ground + 1, cz, Direction.WEST, "PANTHEON", "ALL GODS");
    }

    private static void questBoard(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        pave(b, cx - 20, cz - 10, cx + 20, cz + 10, ground, p.ground(), p);
        // A notice board under a small tiled canopy, open at head height so the axis stays walkable.
        b.fill(cx - 6, ground + 1, cz - 1, cx - 5, ground + 7, cz + 1, p.accent());
        b.fill(cx + 5, ground + 1, cz - 1, cx + 6, ground + 7, cz + 1, p.accent());
        b.fill(cx - 5, ground + 3, cz, cx + 5, ground + 6, cz, BOARD);
        b.fill(cx - 8, ground + 7, cz - 2, cx + 8, ground + 7, cz + 2, p.roofSlab());
        b.pitchedRoof(cx - 9, cz - 3, cx + 9, cz + 3, ground + 8, 3, 0);
        b.put(cx - 4, ground + 5, cz + 1, p.light());
        b.put(cx + 4, ground + 5, cz + 1, p.light());
        signAt(b, cx, ground + 1, cz + 9, Direction.SOUTH, "CITY NOTICES");
    }

    private static void catacombEntrance(StructureBuilder b, int cx, int cz, int ground,
                                         Palette p) {
        // A small open aedicula over the stair.
        pave(b, cx - 12, cz - 8, cx + 12, cz + 10, ground, p.ground(), p);
        b.fill(cx - 10, ground + 1, cz - 6, cx - 9, ground + 8, cz + 8, p.wall());
        b.fill(cx + 9, ground + 1, cz - 6, cx + 10, ground + 8, cz + 8, p.wall());
        b.fill(cx - 10, ground + 1, cz - 6, cx + 10, ground + 2, cz - 5, p.wall());
        b.fill(cx - 10, ground + 9, cz - 6, cx + 10, ground + 9, cz + 8, p.roofSlab());
        b.pitchedRoof(cx - 11, cz - 7, cx + 11, cz + 9, ground + 10, 4, 0);
        b.put(cx - 9, ground + 6, cz + 8, p.light());
        b.put(cx + 9, ground + 6, cz + 8, p.light());
        // The stair mouth.
        b.fill(cx - 2, ground + 1, cz - 4, cx + 2, ground + 3, cz + 6, AIR);
        signAt(b, cx + 8, ground + 1, cz + 8, Direction.SOUTH, "CATACOMBS", "NO LAMPS");
    }

    private static void albergo(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 13;
        int z0 = cz - 9;
        int x1 = cx + 13;
        int z1 = cz + 9;
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 13, z1, p.wall());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 13, z1 - 1, AIR);
        b.fill(x0, ground + 14, z0, x1, ground + 14, z1, p.roofSlab());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 15, 4, 0);
        for (int y = ground + 3; y <= ground + 11; y += 4) {
            for (int x = x0 + 2; x <= x1 - 2; x += 4) {
                b.window(x, y, z0, 2, 2, true);
                b.window(x, y, z1, 2, 2, true);
            }
            for (int z = z0 + 2; z <= z1 - 2; z += 4) {
                b.window(x0, y, z, 2, 1, true);
                b.window(x1, y, z, 2, 1, true);
            }
        }
        placeDoor(b, p, x0, z0, x1, z1, ground + 1, Side.S, 13);
        // Balcony rails and lamps.
        b.fill(x0, ground + 8, z1, x1, ground + 8, z1, p.rail());
        b.put(x0 + 1, ground + 9, z1, p.light());
        b.put(x1 - 1, ground + 9, z1, p.light());
        b.fill(x0 + 2, ground + 1, z0 + 2, x0 + 6, ground + 2, z0 + 4, p.accent());
        b.put(x0 + 2, ground + 3, z0 + 2, BOOKSHELF);
        signAt(b, cx - 8, ground + 1, z1 + 1, Direction.SOUTH, "ALBERGO", "ROOMS");
    }

    private static void shop(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 9;
        int z0 = cz - 7;
        int x1 = cx + 9;
        int z1 = cz + 7;
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 8, z1, p.wall());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 8, z1 - 1, AIR);
        b.fill(x0, ground + 9, z0, x1, ground + 9, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 10, 3, 0);
        b.window(x0 + 2, ground + 3, z0, 2, 2, true);
        b.window(x1 - 3, ground + 3, z0, 2, 2, true);
        b.window(x0 + 2, ground + 3, z1, 2, 2, true);
        b.window(x1 - 3, ground + 3, z1, 2, 2, true);
        placeDoor(b, p, x0, z0, x1, z1, ground + 1, Side.S, 9);
        // Shopfront awning and goods.
        b.fill(x0 + 1, ground + 4, z1 + 1, x1 - 1, ground + 4, z1 + 2, p.roofSlab());
        b.put(x0 + 1, ground + 1, z1 + 1, BARREL);
        b.put(x1 - 1, ground + 1, z1 + 1, BARREL);
        b.put(x0 + 2, ground + 1, z1 + 1, p.foundation());
        signAt(b, cx - 6, ground + 1, z1 + 2, Direction.SOUTH, "MARKET", "OPEN");
    }

    private static void centroBuilding(StructureBuilder b, int x, int z, int ground, Palette p,
                                       int w, int d, int storeys, int kind) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y1 = ground + storeys * 4 + 1;
        BlockState wall = kind == 3 ? p.weathered() : p.wall();
        b.fill(x, ground, z, x1, ground, z1, p.foundation());
        b.walls(x, ground + 1, z, x1, y1 - 1, z1, wall);
        b.fill(x + 1, ground + 1, z + 1, x1 - 1, y1 - 1, z1 - 1, AIR);
        b.fill(x, y1, z, x1, y1, z1, p.roof());
        b.fill(x, y1 - 1, z, x1, y1 - 1, z1, p.accent());
        for (int y = ground + 1; y < y1; y++) {
            b.put(x, y, z, p.accent());
            b.put(x1, y, z, p.accent());
            b.put(x, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
        for (int wy = ground + 3; wy < y1 - 1; wy += 4) {
            for (int wx = x + 2; wx < x1 - 1; wx += 4) {
                b.window(wx, wy, z, 2, 2, true);
                b.window(wx, wy, z1, 2, 2, true);
            }
            for (int wz = z + 2; wz < z1 - 1; wz += 4) {
                b.window(x, wy, wz, 2, 1, true);
                b.window(x1, wy, wz, 2, 1, true);
            }
        }
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, y1 + 1, 3 + (w + d) % 3, w >= d ? 0 : 1);
        placeDoor(b, p, x, z, x1, z1, ground + 1, Side.S, w / 2);
        switch (kind) {
            case 0 -> {
                b.fill(x + 1, ground + 4, z1 + 1, x1 - 1, ground + 4, z1 + 2, p.roofSlab());
                b.put(x + 1, ground + 1, z1 + 1, BARREL);
                b.put(x1 - 1, ground + 1, z1 + 1, BARREL);
                signAt(b, x + 2, ground + 1, z1 + 2, Direction.SOUTH, "BOTTEGA", "OPEN");
            }
            case 1 -> {
                b.fill(x + 1, ground + 4, z1 + 1, x1 - 1, ground + 4, z1 + 3, p.roofSlab());
                table(b, x + 3, z1 + 2, ground, p);
                table(b, x1 - 3, z1 + 2, ground, p);
                signAt(b, x + 2, ground + 1, z1 + 2, Direction.SOUTH, "CAFFE", "BUONO");
            }
            case 2 -> {
                b.fill(x, ground + 8, z1, x1, ground + 8, z1, p.rail());
                b.put(x + 1, ground + 9, z1, p.light());
                b.put(x1 - 1, ground + 9, z1, p.light());
                signAt(b, x + 2, ground + 1, z1 + 1, Direction.SOUTH, "ALBERGO", "ROOMS");
            }
            case 3 -> {
                b.fill(x + 2, ground + 1, z1, x1 - 2, ground + 2, z1, BOARD);
                b.put(x + 2, ground + 1, z1 + 1, p.rubble());
                b.put(x1 - 2, ground + 1, z1 + 1, p.rubble());
                signAt(b, x + 2, ground + 1, z1 + 1, Direction.SOUTH, "CLOSED");
            }
            default -> b.put(x + 2, ground + 1, z1 + 1, BARREL);
        }
    }

    // ------------------------------------------------------------------ catacombs

    private static void catacombs(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int floor = ground - 6;
        // The entrance stair under the aedicula.
        for (int i = 0; i < 7; i++) {
            int y = ground - i;
            b.fill(cx - 20, y, cz + 82 + i, cx - 16, y, cz + 82 + i, p.foundation());
            b.fill(cx - 20, y + 1, cz + 82 + i, cx - 16, ground + 2, cz + 82 + i, AIR);
        }
        // The entry corridor.
        gallery(b, cx - 20, cz + 88, cx - 16, cz + 118, floor, 2, p);
        // The main gallery and its cross galleries.
        gallery(b, cx - 90, cz + 118, cx + 90, cz + 126, floor, 2, p);
        for (int x = -60; x <= 60; x += 40) {
            gallery(b, cx + x - 2, cz + 90, cx + x + 2, cz + 170, floor, 2, p);
        }
        // The chapel.
        gallery(b, cx - 16, cz + 128, cx + 16, cz + 154, floor, 4, p);
        b.fill(cx - 2, floor + 1, cz + 126, cx + 2, floor + 2, cz + 128, AIR);
        // The hidden relic chamber, east of the chapel.
        gallery(b, cx + 20, cz + 132, cx + 36, cz + 148, floor, 2, p);
        b.fill(cx + 16, floor + 1, cz + 138, cx + 20, floor + 2, cz + 142, AIR);
        b.fill(cx + 17, floor + 1, cz + 138, cx + 19, floor + 2, cz + 142, COBWEB);

        tombNiches(b, cx, cz, floor);
        chapel(b, cx, cz, floor, p);
        secretChamber(b, cx, cz, floor, p);
        missingPriest(b, cx, cz, floor, p);
    }

    private static void tombNiches(StructureBuilder b, int cx, int cz, int floor) {
        for (int x = -84; x <= 84; x += 8) {
            b.put(cx + x, floor + 1, cz + 118, BONE);
            b.put(cx + x, floor + 2, cz + 118, BONE);
            b.put(cx + x, floor + 1, cz + 126, BONE);
            b.put(cx + x, floor + 2, cz + 126, BONE);
            if ((x / 8 & 1) == 0) {
                b.chest(cx + x, floor + 1, cz + 120, SiteLoot.COMMON);
            }
            if (x % 24 == 0) {
                b.put(cx + x, floor + 3, cz + 122, COBWEB);
                b.put(cx + x, floor + 1, cz + 122, CANDLE);
            }
        }
        for (int z = 96; z <= 164; z += 8) {
            for (int x = -60; x <= 60; x += 40) {
                b.put(cx + x - 2, floor + 1, cz + z, BONE);
                b.put(cx + x + 2, floor + 1, cz + z, BONE);
                if (z % 24 == 0) {
                    b.chest(cx + x - 1, floor + 1, cz + z, SiteLoot.COMMON);
                }
            }
        }
    }

    private static void chapel(StructureBuilder b, int cx, int cz, int floor, Palette p) {
        // Columns down both sides and a stone altar at the south end.
        for (int z = cz + 132; z <= cz + 148; z += 4) {
            b.fill(cx - 14, floor + 1, z, cx - 14, floor + 4, z, p.accent());
            b.fill(cx + 14, floor + 1, z, cx + 14, floor + 4, z, p.accent());
        }
        b.fill(cx - 4, floor + 1, cz + 150, cx + 4, floor + 2, cz + 152, p.foundation());
        b.fill(cx - 2, floor + 3, cz + 151, cx + 2, floor + 3, cz + 151, p.accent());
        b.put(cx, floor + 4, cz + 151, p.accent());
        b.put(cx - 1, floor + 4, cz + 151, p.accent());
        b.put(cx + 1, floor + 4, cz + 151, p.accent());
        b.put(cx, floor + 1, cz + 149, CANDLE);
        b.put(cx - 2, floor + 1, cz + 149, CANDLE);
        b.put(cx + 2, floor + 1, cz + 149, CANDLE);
        // Pews.
        for (int z = cz + 134; z <= cz + 146; z += 4) {
            b.fill(cx - 8, floor + 1, z, cx - 4, floor + 1, z, p.roofSlab());
            b.fill(cx + 4, floor + 1, z, cx + 8, floor + 1, z, p.roofSlab());
        }
        b.chest(cx - 10, floor + 1, cz + 146, SiteLoot.RELIC);
        b.chest(cx + 10, floor + 1, cz + 146, SiteLoot.CULT);
        b.put(cx - 6, floor + 3, cz + 150, COBWEB);
        b.put(cx + 6, floor + 3, cz + 150, COBWEB);
        signAt(b, cx, floor + 1, cz + 153, Direction.NORTH, "THE CATACOMBS", "ST PETER'S",
                "RELIC", "FORGIVE US");
    }

    private static void secretChamber(StructureBuilder b, int cx, int cz, int floor, Palette p) {
        b.put(cx + 34, floor + 1, cz + 134, p.light());
        b.chest(cx + 32, floor + 1, cz + 140, SiteLoot.RELIC);
        b.chest(cx + 34, floor + 1, cz + 144, SiteLoot.CULT);
        b.fill(cx + 22, floor + 1, cz + 134, cx + 24, floor + 3, cz + 134, p.wall());
        b.put(cx + 25, floor + 2, cz + 134, COBWEB);
        signAt(b, cx + 28, floor + 1, cz + 146, Direction.NORTH, "THE RELIC", "IS HERE",
                "TAKE IT?");
    }

    private static void missingPriest(StructureBuilder b, int cx, int cz, int floor, Palette p) {
        // A barred cell in the chapel's east aisle.
        b.fill(cx + 6, floor + 1, cz + 148, cx + 14, floor + 3, cz + 148, IRON_BARS);
        b.put(cx + 8, floor + 1, cz + 149, RED_WOOL);
        b.put(cx + 8, floor + 2, cz + 149, RED_WOOL);
        b.put(cx + 10, floor + 1, cz + 149, p.light());
        signAt(b, cx + 4, floor + 1, cz + 152, Direction.NORTH, "HE IS HERE");
    }

    // ------------------------------------------------------------------ station and port

    private static void station(StructureBuilder b, int cx, int cz, Palette p,
                                IntBinaryOperator surface) {
        int base = surface.applyAsInt(cx + 300, cz + 120);
        // A straight, level approach: cut/fill each column to the bed, then lay one east-west track.
        for (int x = cx + 250; x <= cx + 340; x++) {
            for (int z = cz + 117; z <= cz + 124; z++) {
                int gy = surface.applyAsInt(x, z);
                if (gy < base) {
                    b.fill(x, gy, z, x, base, z, p.foundation());
                }
                b.fill(x, base + 1, z, x, Math.max(base + 8, gy + 2), z, AIR);
            }
        }
        b.fill(cx + 250, base, cz + 117, cx + 340, base, cz + 124, p.foundation());
        // Level the station and yard footprint onto the same datum.
        for (int x = cx + 280; x <= cx + 336; x++) {
            for (int z = cz + 102; z <= cz + 124; z++) {
                int gy = surface.applyAsInt(x, z);
                if (gy < base) {
                    b.fill(x, gy, z, x, base, z, p.foundation());
                } else if (gy > base) {
                    b.fill(x, base + 1, z, x, gy + 2, z, AIR);
                }
            }
        }
        b.fill(cx + 280, base, cz + 102, cx + 336, base, cz + 124, p.foundation());
        BlockState rail = Blocks.RAIL.defaultBlockState()
                .setValue(BlockStateProperties.RAIL_SHAPE, RailShape.EAST_WEST);
        for (int x = cx + 250; x <= cx + 340; x++) {
            b.put(x, base + 1, cz + 120, rail);
            if ((x & 1) == 0) {
                b.put(x, base, cz + 119, BOARD);
                b.put(x, base, cz + 121, BOARD);
            }
        }
        b.fill(cx + 250, base + 1, cz + 119, cx + 251, base + 2, cz + 121, p.accent());
        b.fill(cx + 339, base + 1, cz + 119, cx + 340, base + 2, cz + 121, p.accent());
        // Platforms and the station yard.
        b.fill(cx + 280, base, cz + 116, cx + 336, base, cz + 124, p.foundation());
        b.fill(cx + 280, base + 1, cz + 117, cx + 320, base + 1, cz + 119, p.ground());
        b.fill(cx + 280, base + 1, cz + 121, cx + 320, base + 1, cz + 124, p.ground());
        b.fill(cx + 320, base, cz + 104, cx + 336, base, cz + 116, p.foundation());
        // The terminus building.
        int x0 = cx + 288;
        int x1 = cx + 312;
        int z0 = cz + 102;
        int z1 = cz + 116;
        b.fill(x0, base, z0, x1, base, z1, p.foundation());
        b.walls(x0, base + 1, z0, x1, base + 10, z1, p.wall());
        b.fill(x0 + 1, base + 1, z0 + 1, x1 - 1, base + 10, z1 - 1, AIR);
        b.fill(x0, base + 11, z0, x1, base + 11, z1, p.roof());
        b.fill(x0, base + 10, z0, x1, base + 10, z1, p.accent());
        b.crenellations(x0, z0, x1, z1, base + 12);
        b.fill(cx + 298, base + 1, z1, cx + 302, base + 4, z1, AIR);
        b.window(x0 + 3, base + 4, z1, 2, 2, true);
        b.window(x1 - 4, base + 4, z1, 2, 2, true);
        b.fill(cx + 298, base + 7, z1, cx + 302, base + 9, z1, p.window());
        b.put(cx + 300, base + 12, z1, p.light());
        // Platform canopy.
        for (int x = cx + 284; x <= cx + 316; x += 8) {
            b.fill(x, base + 1, cz + 117, x, base + 6, cz + 117, p.accent());
            b.fill(x, base + 1, cz + 123, x, base + 6, cz + 123, p.accent());
        }
        b.fill(cx + 282, base + 6, cz + 117, cx + 318, base + 6, cz + 123, p.roofSlab());
        b.pitchedRoof(cx + 282, cz + 117, cx + 318, cz + 124, base + 7, 3, 0);
        // Sign, crates and a lamp in the yard.
        signAt(b, cx + 316, base + 1, cz + 117, Direction.SOUTH, "STAZIONE", "TERMINI");
        b.put(cx + 322, base + 1, cz + 112, BARREL);
        b.put(cx + 324, base + 1, cz + 112, BARREL);
        b.put(cx + 328, base + 1, cz + 110, CAULDRON);
        b.put(cx + 332, base + 1, cz + 110, p.light());
    }

    private static void port(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Quay apron, timber pier, and the opened parapet that steps down onto it.
        paveAt(b, cx + 40, cz + 190, cx + 90, cz + 199, (x, z) -> ground, p.ground());
        b.fill(cx + 58, ground - 1, cz + 199, cx + 68, ground - 1, cz + 218, DECK);
        for (int x = cx + 58; x <= cx + 68; x += 5) {
            for (int z = cz + 203; z <= cz + 215; z += 6) {
                b.fill(x, ground - 3, z, x, ground - 1, z, p.accent());
            }
        }
        b.fill(cx + 58, ground, cz + 199, cx + 58, ground, cz + 218, p.rail());
        b.fill(cx + 68, ground, cz + 199, cx + 68, ground, cz + 218, p.rail());
        b.fill(cx + 58, ground + 1, cz + 198, cx + 68, ground + 2, cz + 199, AIR);
        b.fill(cx + 58, ground, cz + 198, cx + 68, ground, cz + 199, DECK);
        // Warehouses, crane and cargo.
        warehouse(b, cx + 48, cz + 182, ground, p);
        warehouse(b, cx + 78, cz + 182, ground, p);
        b.fill(cx + 72, ground, cz + 199, cx + 72, ground + 12, cz + 199, p.accent());
        b.fill(cx + 72, ground + 12, cz + 199, cx + 84, ground + 12, cz + 199, p.accent());
        b.put(cx + 84, ground + 11, cz + 199, p.rail());
        b.put(cx + 84, ground + 10, cz + 199, p.light());
        b.put(cx + 56, ground + 1, cz + 196, BARREL);
        b.put(cx + 57, ground + 1, cz + 196, BARREL);
        b.put(cx + 70, ground + 1, cz + 194, HAY);
        b.put(cx + 71, ground + 1, cz + 194, HAY);
        signAt(b, cx + 42, ground + 1, cz + 190, Direction.SOUTH, "TIBER PORT", "CUSTOMS");
    }

    private static void warehouse(StructureBuilder b, int x, int z, int ground, Palette p) {
        int x1 = x + 10;
        int z1 = z + 8;
        b.fill(x, ground, z, x1, ground, z1, p.foundation());
        b.walls(x, ground + 1, z, x1, ground + 7, z1, p.weathered());
        b.fill(x + 1, ground + 1, z + 1, x1 - 1, ground + 7, z1 - 1, AIR);
        b.fill(x, ground + 8, z, x1, ground + 8, z1, p.roof());
        b.fill(x, ground + 7, z, x1, ground + 7, z1, p.accent());
        placeDoor(b, p, x, z, x1, z1, ground + 1, Side.S, 5);
        b.window(x + 2, ground + 4, z, 2, 2, true);
        b.window(x1 - 3, ground + 4, z, 2, 2, true);
        b.put(x + 1, ground + 1, z1 + 1, BARREL);
        b.put(x + 2, ground + 1, z1 + 1, BARREL);
        b.put(x1 - 1, ground + 1, z1 + 1, HAY);
    }

    // ------------------------------------------------------------------ dressing

    private static void dressing(StructureBuilder b, int cx, int cz, int ground, Palette p,
                                 IntBinaryOperator surface) {
        for (int[] pine : PINES) {
            int x = cx + pine[0];
            int z = cz + pine[1];
            umbrellaPine(b, x, z, surface.applyAsInt(x, z), p);
        }
        for (int[] cypress : CYPRI) {
            int x = cx + cypress[0];
            int z = cz + cypress[1];
            cypress(b, x, z, surface.applyAsInt(x, z), p);
        }
        for (int[] t : CAFE_TABLES) {
            if (!(t[0] > -50 && t[0] < 50 && t[1] >= 0 && t[1] <= 50)) {
                table(b, cx + t[0], cz + t[1], ground, p);
            }
        }
        for (int[] v : VESPAS) {
            vespa(b, cx + v[0], cz + v[1], ground, p, v[2]);
        }
        // A few cats' doorsteps, flower boxes and boarded shutters.
        int[][] boxes = {{-50, 70}, {50, 70}, {-88, 90}, {30, 90}, {-50, 130}, {30, 130}};
        for (int[] box : boxes) {
            b.put(cx + box[0], ground + 1, cz + box[1], p.overgrowth() != null
                    ? p.overgrowth() : GRASS);
        }
        b.put(cx - 60, ground + 1, cz + 130, CAULDRON);
        b.put(cx + 20, ground + 1, cz + 62, BARREL);
        b.put(cx - 30, ground + 1, cz + 62, BARREL);
    }

    private static void umbrellaPine(StructureBuilder b, int x, int z, int ground, Palette p) {
        for (int y = ground + 1; y <= ground + 5; y++) {
            b.put(x, y, z, PINE_LOG);
        }
        b.fill(x - 3, ground + 6, z - 3, x + 3, ground + 6, z + 3, PINE_LEAVES);
        b.fill(x - 2, ground + 7, z - 2, x + 2, ground + 7, z + 2, PINE_LEAVES);
        b.put(x, ground + 8, z, PINE_LEAVES);
        b.put(x, ground, z, p.ground());
    }

    private static void cypress(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x, ground + 1, z, x, ground + 7, z, PINE_LOG);
        for (int y = ground + 2; y <= ground + 8; y++) {
            b.put(x, y, z, CYPRESS);
            if (y <= ground + 5) {
                b.put(x - 1, y, z, CYPRESS);
                b.put(x + 1, y, z, CYPRESS);
            }
        }
        b.put(x, ground + 9, z, CYPRESS);
        b.put(x, ground, z, p.ground());
    }

    private static void table(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground + 1, z, p.rail());
        b.fill(x - 1, ground + 2, z - 1, x + 1, ground + 2, z + 1, p.roofSlab());
        b.put(x - 2, ground + 1, z, p.roofSlab());
        b.put(x + 2, ground + 1, z, p.roofSlab());
        b.put(x, ground + 3, z, p.light());
    }

    private static void marketStall(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x, ground + 1, z, x + 4, ground + 1, z + 2, p.foundation());
        for (int dx = 0; dx <= 4; dx += 4) {
            b.fill(x + dx, ground + 2, z, x + dx, ground + 3, z + 2, p.accent());
        }
        b.fill(x, ground + 4, z, x + 4, ground + 4, z + 2, p.roofSlab());
        b.put(x + 2, ground + 2, z + 2, p.light());
        b.put(x + 1, ground + 2, z + 1, p.foundation());
        b.put(x + 3, ground + 2, z + 1, p.foundation());
    }

    private static void fountain(StructureBuilder b, int cx, int cz, int ground, Palette p,
                                 int r) {
        b.fill(cx - r, ground, cz - r, cx + r, ground, cz + r, p.foundation());
        b.fill(cx - r + 1, ground, cz - r + 1, cx + r - 1, ground, cz + r - 1, WATER);
        b.fill(cx - r + 1, ground + 1, cz - r, cx + r - 1, ground + 1, cz - r, p.foundation());
        b.fill(cx - r + 1, ground + 1, cz + r, cx + r - 1, ground + 1, cz + r, p.foundation());
        b.fill(cx - r, ground + 1, cz - r + 1, cx - r, ground + 1, cz + r - 1, p.foundation());
        b.fill(cx + r, ground + 1, cz - r + 1, cx + r, ground + 1, cz + r - 1, p.foundation());
        b.put(cx, ground + 1, cz, p.accent());
        b.put(cx, ground + 2, cz, p.light());
    }

    private static void vespa(StructureBuilder b, int x, int z, int ground, Palette p, int facing) {
        int dx = facing == 1 ? 1 : facing == 3 ? -1 : 0;
        int dz = facing == 0 ? -1 : facing == 2 ? 1 : 0;
        b.put(x, ground + 1, z, BLACK_WOOL);
        b.put(x + dx, ground + 1, z + dz, BLACK_WOOL);
        b.put(x, ground + 2, z, p.accent());
        b.put(x + dx, ground + 2, z + dz, p.rail());
    }

    private static void column(StructureBuilder b, int x, int z, int baseY, int height,
                               Palette p) {
        if (height <= 0) {
            return;
        }
        b.put(x, baseY, z, p.foundation());
        b.fill(x, baseY + 1, z, x, baseY + height, z, p.accent());
        b.put(x, baseY + height + 1, z, p.frame());
    }

    // ------------------------------------------------------------------ readable signs

    private static void signs(StructureBuilder b, int cx, int cz, int ground) {
        signAt(b, cx - 20, ground + 1, cz - 92, Direction.SOUTH, "ST PETER'S", "BASILICA",
                "OPEN DAWN");
        signAt(b, cx + 142, ground + 1, cz - 56, Direction.WEST, "FORUM", "RUINS",
                "MIND THE", "STONES");
        signAt(b, cx - 138, ground + 1, cz + 40, Direction.EAST, "TRASTEVERE", "LANES",
                "TRATTORIE");
        signAt(b, cx - 10, ground + 1, cz + 82, Direction.SOUTH, "CATACOMBS", "NO LAMPS",
                "KEEP TO THE", "MAIN GALLERY");
        signAt(b, cx, ground + 1, cz + 58, Direction.SOUTH, "CITY NOTICES", "RELIC MISSING",
                "PRIEST MISSING", "SEE THE BOARD");
        signAt(b, cx + 20, ground + 1, cz + 240, Direction.NORTH, "TIBER", "NO SWIMMING",
                "STRONG CURRENT");
        // Shop and inn boards are placed with their buildings; these point the way.
        signAt(b, cx - 60, ground + 1, cz - 90, Direction.SOUTH, "ALBERGO  W");
        signAt(b, cx + 14, ground + 1, cz + 108, Direction.WEST, "MARKET  W");
        signAt(b, cx - 138, ground + 1, cz + 124, Direction.EAST, "APOTHECARY  W");
    }

    // ------------------------------------------------------------------ small helpers

    private static void placeDoor(StructureBuilder b, Palette p, int x0, int z0, int x1, int z1,
                                  int y, Side side, int offset) {
        int dx;
        int dz;
        Direction facing;
        switch (side) {
            case N -> {
                dx = x0 + Math.clamp(offset, 1, x1 - x0 - 1);
                dz = z0;
                facing = Direction.NORTH;
            }
            case S -> {
                dx = x0 + Math.clamp(offset, 1, x1 - x0 - 1);
                dz = z1;
                facing = Direction.SOUTH;
            }
            case W -> {
                dx = x0;
                dz = z0 + Math.clamp(offset, 1, z1 - z0 - 1);
                facing = Direction.WEST;
            }
            default -> {
                dx = x1;
                dz = z0 + Math.clamp(offset, 1, z1 - z0 - 1);
                facing = Direction.EAST;
            }
        }
        b.put(dx, y, dz, AIR);
        b.put(dx, y + 1, dz, AIR);
        BlockState lower = p.door()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        b.put(dx, y, dz, lower);
        b.put(dx, y + 1, dz,
                lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
    }

    private static void signAt(StructureBuilder b, int x, int y, int z, Direction facing,
                               String... lines) {
        b.put(x, y - 1, z, Blocks.SMOOTH_QUARTZ.defaultBlockState());
        CityPlanSupport.sign(b, x, y, z, facing, lines);
    }
}
