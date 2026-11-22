package com.sanchous98.eldritchhorror.world.loc.city.plan;

import net.minecraft.world.item.DyeColor;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.SiteLoot;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import java.util.List;
import java.util.function.IntBinaryOperator;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Seoul, late Joseon under a modernising edge: Gyeongbokgung's throne hall and stone courtyards,
 * Bukchon's tiled hanok lanes, Namdaemun market, the brick quarter south of the Han, the river
 * with its bridge and ferry, and a queen's tomb under the palace garden.
 *
 * <p>Everything is authored: absolute offsets from {@code (cx, cz)}, the city's {@code ground}
 * level, and explicit arrays/loops. CityLocation keeps a flat authored heart at {@code ground}
 * and grades the rim; the core is built at {@code ground} plus fixed authored offsets, while the
 * arrival gate sits on the shared graded surface at the south rim. No core height is derived from
 * terrain. There is no {@code RandomSource}, no layout hash and no chunk-local read.
 * {@link StructureBuilder} clips every write to the generating chunk, so the same call builds the
 * same city slice in every chunk. Streets and water are laid first, then the districts, so no
 * facade can overwrite a road or a quay.
 */
public final class SeoulPlan implements CityPlan {

    /** Authored half-extent (blocks). */
    private static final int HALF = 340;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState PINE_LOG = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState PINE_LEAVES = Blocks.SPRUCE_LEAVES.defaultBlockState();
    private static final BlockState GRASS = Blocks.SHORT_GRASS.defaultBlockState();
    private static final BlockState POPPY = Blocks.POPPY.defaultBlockState();
    private static final BlockState DANDELION = Blocks.DANDELION.defaultBlockState();
    private static final BlockState AZALEA = Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState();
    private static final BlockState BRICK = Blocks.BRICKS.defaultBlockState();
    private static final BlockState MUD_BRICK = Blocks.MUD_BRICKS.defaultBlockState();
    private static final BlockState QUARTZ = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState RED = Blocks.DYED_TERRACOTTA.pick(DyeColor.RED).defaultBlockState();
    private static final BlockState WHITE = Blocks.DYED_TERRACOTTA.pick(DyeColor.WHITE).defaultBlockState();
    private static final BlockState BLUE = Blocks.CONCRETE.pick(DyeColor.LIGHT_BLUE).defaultBlockState();
    private static final BlockState DEEPSLATE = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
    private static final BlockState POLISHED_DEEPSLATE = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
    private static final BlockState GOLD = Blocks.GILDED_BLACKSTONE.defaultBlockState();
    private static final BlockState SOUL = Blocks.SOUL_LANTERN.defaultBlockState();
    private static final BlockState WOOL_WHITE = Blocks.WOOL.pick(DyeColor.WHITE).defaultBlockState();
    private static final BlockState WOOL_RED = Blocks.WOOL.pick(DyeColor.RED).defaultBlockState();
    private static final BlockState IRON = Blocks.IRON_BLOCK.defaultBlockState();
    private static final BlockState RAIL = Blocks.RAIL.defaultBlockState();
    private static final BlockState LILY = Blocks.LILY_PAD.defaultBlockState();
    private static final BlockState DECK = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private static final BlockState BOOKSHELF = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState CLOCK_FACE = Blocks.TARGET.defaultBlockState();
    private static final BlockState BARREL = Blocks.BARREL.defaultBlockState();

    /** Bukchon north-south lane centres (4 wide, so each band is centre-1..centre+2). */
    private static final int[] BUKCHON_X = {-318, -278, -238, -198};
    /** Bukchon east-west lane centres (4 wide). */
    private static final int[] BUKCHON_Z = {-250, -200, -150, -100, -50, 0, 50, 100, 150};
    /** Namdaemun east-west lane centres (6 wide). */
    private static final int[] MARKET_Z = {-120, -60, 0, 40};
    /** Modern north-south grid lines (10 wide). */
    private static final int[] MODERN_X = {180, 240, 300};
    /** Modern east-west lanes on the north bank. */
    private static final int[] MODERN_Z_NORTH = {130, 170};
    /** Modern east-west lanes on the south bank. */
    private static final int[] MODERN_Z_SOUTH = {250, 290};

    /**
     * Bukchon hanok lots: x, z, width, depth, wall height, door offset, door side
     * (0 = south, 1 = west, 2 = east, 3 = north). Lanes are deliberately left clear.
     */
    private static final int[][] HANOKS = {
            {-314, -246, 9, 7, 5, 4, 0},
            {-300, -246, 9, 7, 4, 3, 0},
            {-286, -246, 7, 6, 5, 3, 0},
            {-314, -232, 9, 6, 4, 2, 0},
            {-300, -232, 9, 7, 5, 4, 0},
            {-312, -218, 8, 7, 4, 3, 0},
            {-298, -218, 9, 7, 5, 4, 0},
            {-286, -218, 7, 6, 4, 3, 2},
            {-274, -196, 9, 7, 5, 4, 0},
            {-260, -196, 9, 6, 4, 2, 0},
            {-246, -196, 7, 7, 4, 3, 0},
            {-274, -182, 9, 7, 4, 3, 0},
            {-258, -182, 9, 6, 5, 4, 0},
            {-272, -168, 8, 6, 4, 2, 0},
            {-258, -168, 9, 7, 5, 4, 0},
            {-246, -168, 7, 7, 4, 3, 3},
            {-234, -146, 9, 7, 4, 3, 0},
            {-220, -146, 9, 6, 5, 4, 0},
            {-206, -146, 7, 7, 4, 2, 0},
            {-232, -132, 9, 7, 5, 4, 1},
            {-218, -132, 9, 6, 4, 3, 0},
            {-220, -118, 9, 7, 5, 4, 0},
            {-206, -118, 7, 7, 4, 3, 0},
            {-194, -96, 9, 7, 5, 4, 0},
            {-180, -96, 9, 6, 4, 3, 0},
            {-166, -96, 8, 7, 4, 2, 2},
            {-192, -82, 9, 7, 4, 3, 0},
            {-178, -82, 9, 6, 5, 4, 0},
            {-194, -68, 8, 6, 4, 2, 0},
            {-180, -68, 9, 7, 5, 4, 0},
            {-166, -68, 9, 7, 4, 3, 0},
            {-314, -46, 9, 7, 4, 3, 0},
            {-300, -46, 9, 6, 5, 4, 0},
            {-286, -46, 7, 7, 4, 2, 0},
            {-314, -32, 9, 7, 5, 4, 0},
            {-300, -32, 9, 6, 4, 3, 0},
            {-274, 4, 9, 7, 4, 2, 0},
            {-260, 4, 9, 7, 5, 4, 0},
            {-246, 4, 7, 6, 4, 3, 1},
            {-274, 18, 9, 6, 4, 3, 0},
            {-258, 18, 9, 7, 5, 4, 0},
            {-234, 54, 9, 7, 4, 2, 0},
            {-220, 54, 9, 6, 5, 4, 0},
            {-206, 54, 7, 7, 4, 3, 0},
            {-232, 68, 9, 7, 5, 4, 1},
            {-218, 68, 9, 6, 4, 3, 0},
            {-194, 104, 9, 7, 4, 2, 0},
            {-180, 104, 9, 7, 5, 4, 0},
            {-166, 104, 7, 6, 4, 3, 3},
            {-192, 118, 9, 7, 4, 3, 0},
            {-178, 118, 9, 6, 5, 4, 0}
    };

    /** Market stalls: x, z, stripe (0 white, 1 red). */
    private static final int[][] STALLS = {
            {160, -128, 0}, {180, -128, 1}, {200, -128, 0}, {240, -128, 1},
            {260, -128, 0}, {280, -128, 1}, {300, -128, 0},
            {160, -112, 1}, {180, -112, 0}, {200, -112, 1}, {240, -112, 0},
            {260, -112, 1}, {280, -112, 0}, {300, -112, 1},
            {160, -54, 0}, {180, -54, 1}, {200, -54, 0}, {240, -54, 1},
            {260, -54, 0}, {280, -54, 1}, {300, -54, 0},
            {160, 9, 1}, {180, 9, 0}, {200, 9, 1}, {244, 9, 0},
            {264, 9, 1}, {284, 9, 0}, {304, 9, 1},
            {160, 32, 0}, {180, 32, 1}, {200, 32, 0}, {240, 32, 1},
            {260, 32, 0}, {280, 32, 1}, {300, 32, 0}
    };

    /** Loose market crates: x, z, tall (0/1). */
    private static final int[][] CRATES = {
            {232, -100, 1}, {236, -100, 0}, {232, -64, 0}, {300, -140, 1},
            {156, -56, 0}, {306, 20, 1}, {210, 70, 0}, {250, 70, 1},
            {180, -170, 0}, {290, -170, 1}, {166, -20, 0}, {270, 46, 1}
    };

    /** Pines: x, z. Kept clear of roofs, water and roads. */
    private static final int[][] PINES = {
            {-70, -250}, {-20, -260}, {30, -250}, {70, -240},
            {-60, -120}, {60, -120}, {-110, -150}, {110, -150},
            {-85, -60}, {-15, -60}, {-85, 15}, {-25, 25},
            {25, -65}, {85, -55}, {90, 20}, {30, 25},
            {-325, -255}, {-160, -255}, {-325, -120}, {-155, -80},
            {-325, 20}, {-160, 20}, {-325, 140}, {-155, 120}, {-320, 60},
            {150, -170}, {310, -170}, {310, 60}, {150, 60},
            {145, 110}, {315, 110}, {145, 300}, {315, 300},
            {-300, 242}, {-200, 242}, {-100, 242}, {50, 242}, {100, 242},
            {-300, 188}, {-200, 188}, {-100, 188}, {50, 188}, {100, 188}
    };

    /** Pond lily pads: x, z (inside the palace-garden pond). */
    private static final int[][] LILIES = {
            {-60, -45}, {-55, -40}, {-35, -45}, {-30, -15},
            {-65, -10}, {-25, -35}, {-50, -15}, {-40, -50}
    };

    /** River-bank reeds: x, z (on the quay tops). */
    private static final int[][] REEDS = {
            {-310, 201}, {-250, 201}, {-150, 229}, {-60, 201},
            {90, 229}, {140, 201}, {200, 229}, {280, 201}
    };

    /** Stones breaking the river surface: x, z. */
    private static final int[][] BANK_ROCKS = {
            {-300, 204}, {-200, 226}, {-80, 204}, {60, 226}, {180, 204}, {310, 226}
    };

    @Override
    public String cityId() {
        return "seoul";
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
        // 1. Streets and water first, so no facade can overwrite a road or a quay.
        streets(b, cx, cz, ground, p);
        river(b, cx, cz, ground, p);

        // 2. The city itself. The arrival gate sits on the graded rim; the rest is flat-heart.
        arrival(b, cx, cz, ground, p, surface);
        palace(b, cx, cz, ground, p);
        garden(b, cx, cz, ground, p);
        bukchon(b, cx, cz, ground, p);
        market(b, cx, cz, ground, p);
        modern(b, cx, cz, ground, p);
        station(b, cx, cz, ground, p);

        // 3. Trees and readable notices last, on top of the built fabric.
        trees(b, cx, cz, ground);
        signs(b, cx, cz, ground, p);

        b.marker("seoul_center", cx, ground + 1, cz);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.SIGN, 8, 1, 306, "districts"),
                new CityPoi("market_quest_board", CityPoi.Kind.QUEST_BOARD, 220, 0, 0,
                        "seoul_royal_ghost;seoul_missing_scholar"),
                new CityPoi("hanok_inn", CityPoi.Kind.INN, -240, 0, 80, "innkeeper"),
                new CityPoi("market_shop", CityPoi.Kind.SHOP, 220, 0, -80, "merchant"),
                new CityPoi("herbalist", CityPoi.Kind.HEALER, -240, 0, -100, "herbalist"),
                new CityPoi("palace_clerk", CityPoi.Kind.NPC, 0, 0, -90, "seoul_royal_ghost"),
                new CityPoi("scholar_house", CityPoi.Kind.NPC, -240, 0, 0,
                        "seoul_missing_scholar"),
                new CityPoi("police_station", CityPoi.Kind.NPC, 240, 1, 200, "constable"),
                new CityPoi("seoul_station", CityPoi.Kind.STATION, 220, 1, -199, "east_line"),
                new CityPoi("palace_tomb", CityPoi.Kind.SECRET, 60, -6, -20,
                        "seoul_royal_ghost"),
                new CityPoi("river_ferry", CityPoi.Kind.LANDMARK, 0, 0, 215, "ferryman"),
                new CityPoi("throne_hall", CityPoi.Kind.LANDMARK, 0, 0, -200, "gyeongbokgung"),
                new CityPoi("innkeeper", CityPoi.Kind.NPC, -230, 1, 80, "innkeeper"),
                new CityPoi("merchant", CityPoi.Kind.NPC, 230, 1, -80, "merchant"),
                new CityPoi("herbalist_keeper", CityPoi.Kind.NPC, -228, 1, -106, "herbalist"),
                new CityPoi("scholar", CityPoi.Kind.NPC, -229, 1, 10, "seoul_missing_scholar"),
                new CityPoi("ferryman", CityPoi.Kind.NPC, 20, 0, 212, "ferryman"),
                new CityPoi("bukchon_sign", CityPoi.Kind.SIGN, -150, 1, 64, "bukchon"),
                new CityPoi("namdaemun_sign", CityPoi.Kind.SIGN, 218, 1, 88, "market"),
                new CityPoi("modern_sign", CityPoi.Kind.SIGN, 146, 1, 244, "modern"),
                new CityPoi("river_warning", CityPoi.Kind.SIGN, 12, 1, 240, "river"),
                new CityPoi("tomb_warning", CityPoi.Kind.SIGN, 42, 1, -30, "tomb")
        );
    }

    // ------------------------------------------------------------------ level helpers

    /** Caps a building footprint with one foundation course on the flat city plate. */
    private static void pad(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                            Palette p) {
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
    }

    /** Paves an authored street or plaza rectangle at the flat city level. */
    private static void pave(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                             BlockState state) {
        b.fill(x0, ground, z0, x1, ground, z1, state);
    }

    /** Opens a two-deep water channel with a sand bed at fixed offsets from {@code ground}. */
    private static void channel(StructureBuilder b, int x0, int z0, int x1, int z1, int ground) {
        b.fill(x0, ground, z0, x1, ground + 1, z1, AIR);
        b.fill(x0, ground - 3, z0, x1, ground - 3, z1, SAND);
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, WATER);
    }

    // ------------------------------------------------------------------ streets

    private static void streets(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // The palace avenue, 14 wide on x = 0, from the arrival gate to the palace gate.
        pave(b, cx - 6, cz - 80, cx + 7, cz + 314, ground, p.ground());

        // Bukchon's lanes.
        for (int lx : BUKCHON_X) {
            pave(b, cx + lx - 1, cz - 260, cx + lx + 2, cz + 160, ground, p.ground());
        }
        for (int lz : BUKCHON_Z) {
            pave(b, cx - 330, cz + lz - 1, cx - 150, cz + lz + 2, ground, p.ground());
        }
        // Two connectors from the avenue into the hanok district.
        pave(b, cx - 150, cz - 125, cx - 6, cz - 122, ground, p.ground());
        pave(b, cx - 150, cz + 63, cx - 6, cz + 66, ground, p.ground());

        // Namdaemun's main lane and cross lanes.
        pave(b, cx + 218, cz - 180, cx + 223, cz + 80, ground, p.ground());
        for (int lz : MARKET_Z) {
            pave(b, cx + 150, cz + lz - 2, cx + 310, cz + lz + 3, ground, p.ground());
        }

        // The modern grid, north and south of the river.
        for (int lx : MODERN_X) {
            pave(b, cx + lx - 4, cz + 100, cx + lx + 5, cz + 190, ground, p.ground());
            pave(b, cx + lx - 4, cz + 239, cx + lx + 5, cz + 300, ground, p.ground());
        }
        for (int lz : MODERN_Z_NORTH) {
            pave(b, cx + 140, cz + lz - 4, cx + 320, cz + lz + 5, ground, p.ground());
        }
        for (int lz : MODERN_Z_SOUTH) {
            pave(b, cx + 140, cz + lz - 4, cx + 320, cz + lz + 5, ground, p.ground());
        }

        // River roads along both banks.
        pave(b, cx - 320, cz + 192, cx + 320, cz + 199, ground, p.ground());
        pave(b, cx - 320, cz + 231, cx + 320, cz + 238, ground, p.ground());
    }

    // ------------------------------------------------------------------ the Han

    private static void river(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Water, bed and banks across the full district.
        channel(b, cx - 320, cz + 203, cx + 320, cz + 227, ground);
        b.fill(cx - 320, ground, cz + 200, cx + 320, ground + 1, cz + 202, p.foundation());
        b.fill(cx - 320, ground, cz + 228, cx + 320, ground + 1, cz + 230, p.foundation());

        // The avenue bridge and the modern bridge.
        bridge(b, cx, cz, ground, p, 0, 7, 196, 234, false);
        bridge(b, cx, cz, ground, p, 240, 5, 196, 234, true);

        // The ferry landing and its moored boat.
        ferry(b, cx, cz, ground, p);

        for (int[] reed : REEDS) {
            b.put(cx + reed[0], ground + 2, cz + reed[1], GRASS);
        }
        for (int[] rock : BANK_ROCKS) {
            b.put(cx + rock[0], ground - 1, cz + rock[1], p.rubble());
        }
    }

    private static void bridge(StructureBuilder b, int cx, int cz, int ground, Palette p,
                               int xCenter, int half, int z0, int z1, boolean stone) {
        BlockState deck = stone ? p.foundation() : DECK;
        int x0 = cx + xCenter - half;
        int x1 = cx + xCenter + half;
        for (int z = cz + z0; z <= cz + z1; z++) {
            for (int x = x0; x <= x1; x++) {
                b.put(x, ground, z, deck);
            }
        }
        // Railings and lantern posts.
        for (int z = cz + z0; z <= cz + z1; z++) {
            b.put(x0, ground + 1, z, p.rail());
            b.put(x1, ground + 1, z, p.rail());
        }
        for (int z = cz + z0; z <= cz + z1; z += 6) {
            b.fill(x0, ground + 1, z, x0, ground + 2, z, p.accent());
            b.fill(x1, ground + 1, z, x1, ground + 2, z, p.accent());
        }
        // Stone piers in the water.
        for (int z = cz + z0 + 7; z <= cz + z1 - 7; z += 9) {
            for (int x = x0; x <= x1; x += 3) {
                b.fill(x, ground - 3, z, x, ground - 1, z, p.foundation());
            }
        }
        b.put(x0 + 1, ground + 2, cz + z0, p.light());
        b.put(x1 - 1, ground + 2, cz + z1, p.light());
    }

    private static void ferry(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Timber landing on posts, east of the avenue bridge.
        for (int x = cx + 12; x <= cx + 30; x++) {
            for (int z = cz + 206; z <= cz + 226; z++) {
                b.put(x, ground, z, DECK);
            }
        }
        for (int x = cx + 12; x <= cx + 30; x += 6) {
            for (int z = cz + 206; z <= cz + 226; z += 10) {
                b.fill(x, ground - 2, z, x, ground - 1, z, p.accent());
            }
        }
        b.fill(cx + 12, ground + 1, cz + 206, cx + 30, ground + 1, cz + 206, p.rail());
        b.fill(cx + 12, ground + 1, cz + 226, cx + 30, ground + 1, cz + 226, p.rail());
        // Gangway from the quay down to the landing.
        b.fill(cx + 20, ground, cz + 202, cx + 24, ground, cz + 206, DECK);
        // Moored boat.
        b.fill(cx + 32, ground - 1, cz + 210, cx + 44, ground - 1, cz + 222, DECK);
        b.walls(cx + 32, ground, cz + 210, cx + 44, ground + 1, cz + 222, p.accent());
        b.fill(cx + 35, ground + 1, cz + 213, cx + 41, ground + 3, cz + 219, p.roof());
        b.fill(cx + 35, ground + 4, cz + 213, cx + 41, ground + 4, cz + 219, p.roofSlab());
        b.fill(cx + 33, ground + 1, cz + 216, cx + 33, ground + 7, cz + 216, p.accent());
        b.put(cx + 33, ground + 8, cz + 216, p.light());
        b.fill(cx + 30, ground, cz + 214, cx + 32, ground, cz + 220, DECK);
    }

    // ------------------------------------------------------------------ arrival

    private static void arrival(StructureBuilder b, int cx, int cz, int ground, Palette p,
                                IntBinaryOperator surface) {
        int gy = Math.max(ground, surface.applyAsInt(cx, cz + 338));
        pave(b, cx - 30, cz + 312, cx + 30, cz + 338, gy, p.ground());
        // Stepped descent from the raised gate to the avenue on the city side.
        for (int i = 0; i < 4; i++) {
            int z = cz + 308 + i;
            int top = ground + (gy - ground) * (i + 1) / 4;
            b.fill(cx - 8, ground, z, cx + 8, top, z, p.foundation());
        }
        // Two stone bastions flanking the passage.
        for (int s = -1; s <= 1; s += 2) {
            int x0 = cx + s * 16;
            int x1 = cx + s * 9;
            pad(b, x0, cz + 312, x1, cz + 324, gy, p);
            b.fill(x0, gy + 1, cz + 312, x1, gy + 12, cz + 324, p.wall());
            b.fill(x0, gy + 6, cz + 312, x1, gy + 6, cz + 312, p.accent());
            b.crenellations(x0, cz + 312, x1, cz + 324, gy + 13);
            b.put(x0, gy + 8, cz + 312, p.light());
        }
        // Lintel and tiled roof over the gate.
        b.fill(cx - 9, gy + 9, cz + 312, cx + 9, gy + 10, cz + 324, p.accent());
        b.pitchedRoof(cx - 18, cz + 310, cx + 18, cz + 326, gy + 11, 4, 0);
        b.put(cx - 8, gy + 5, cz + 311, p.light());
        b.put(cx + 8, gy + 5, cz + 311, p.light());
        // Guardian figures.
        stoneGuard(b, cx - 22, cz + 328, gy, p);
        stoneGuard(b, cx + 22, cz + 328, gy, p);
    }

    private static void stoneGuard(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 1, ground, z - 1, x + 1, ground + 1, z + 1, p.foundation());
        b.fill(x, ground + 2, z, x, ground + 3, z, p.wall());
        b.put(x, ground + 4, z, p.accent());
        b.put(x - 1, ground + 3, z, p.wall());
        b.put(x + 1, ground + 3, z, p.wall());
    }

    // ------------------------------------------------------------------ Gyeongbokgung

    private static void palace(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Curtain walls, with the south, east and west gates left open.
        palaceWall(b, p, ground, cx - 140, cz - 300, cx + 140, cz - 299);
        palaceWall(b, p, ground, cx - 140, cz - 300, cx - 139, cz - 80);
        palaceWall(b, p, ground, cx + 139, cz - 300, cx + 140, cz - 80);
        palaceWall(b, p, ground, cx - 140, cz - 81, cx - 20, cz - 80);
        palaceWall(b, p, ground, cx + 20, cz - 81, cx + 140, cz - 80);
        tower(b, p, ground, cx - 140, cz - 300);
        tower(b, p, ground, cx + 140, cz - 300);
        tower(b, p, ground, cx - 140, cz - 80);
        tower(b, p, ground, cx + 140, cz - 80);

        // The south gate, and the two side gates.
        palaceGate(b, p, ground, cx, cz - 84);
        eastGate(b, p, ground, cx + 140, cz - 180);
        eastGate(b, p, ground, cx - 140, cz - 180);

        // Stone courtyard and the axial approach.
        pave(b, cx - 45, cz - 235, cx + 45, cz - 95, ground, p.ground());
        pave(b, cx - 6, cz - 260, cx + 7, cz - 95, ground, p.ground());

        throneHall(b, cx, cz, ground, p);
        openPavilion(b, p, ground, cx - 52, cz - 195);
        openPavilion(b, p, ground, cx + 52, cz - 195);
        longHall(b, p, ground, cx - 108, cz - 235, 17, 71, 6, 8);
        longHall(b, p, ground, cx + 92, cz - 235, 17, 71, 6, 8);

        // The clerk's booth beside the south gate.
        hanok(b, p, ground, cx + 12, cz - 99, 8, 6, 4, 3, Side.S);
        b.fill(cx + 14, ground + 1, cz - 96, cx + 18, ground + 1, cz - 96, p.accent());
        b.put(cx + 15, ground + 2, cz - 98, p.light());

        // Stone lanterns down the courtyard, clear of the hall platform.
        for (int lz = -230; lz <= -100; lz += 26) {
            stoneLantern(b, cx - 44, cz + lz, ground, p);
            stoneLantern(b, cx + 44, cz + lz, ground, p);
        }
    }

    private static void palaceWall(StructureBuilder b, Palette p, int ground,
                                   int x0, int z0, int x1, int z1) {
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.fill(x0, ground + 1, z0, x1, ground + 7, z1, p.wall());
        b.fill(x0, ground + 1, z0, x1, ground + 1, z1, p.weathered());
        b.crenellations(x0, z0, x1, z1, ground + 8);
    }

    private static void tower(StructureBuilder b, Palette p, int ground, int x, int z) {
        pad(b, x - 5, z - 5, x + 5, z + 5, ground, p);
        b.fill(x - 5, ground + 1, z - 5, x + 5, ground + 12, z + 5, p.wall());
        b.fill(x - 5, ground + 12, z - 5, x + 5, ground + 12, z + 5, p.accent());
        b.pitchedRoof(x - 7, z - 7, x + 7, z + 7, ground + 13, 4, 0);
        b.window(x - 3, ground + 5, z - 5, 3, 1, true);
        b.window(x + 3, ground + 5, z - 5, 3, 1, true);
        b.window(x - 3, ground + 5, z + 5, 3, 1, true);
        b.window(x + 3, ground + 5, z + 5, 3, 1, true);
    }

    private static void palaceGate(StructureBuilder b, Palette p, int ground, int x, int z) {
        int z0 = z - 8;
        int z1 = z + 8;
        pad(b, x - 22, z0, x + 22, z1, ground, p);
        b.fill(x - 22, ground + 1, z0, x + 22, ground + 10, z1, p.wall());
        // Three passages: the central one and a pair of side gates.
        b.fill(x - 6, ground + 1, z0, x + 7, ground + 6, z1, AIR);
        b.fill(x - 16, ground + 1, z0, x - 13, ground + 5, z1, AIR);
        b.fill(x + 13, ground + 1, z0, x + 16, ground + 5, z1, AIR);
        // Painted timber frame, skipping the openings.
        for (int px = x - 20; px <= x + 20; px += 5) {
            boolean opening = px >= x - 16 && px <= x - 13
                    || px >= x - 6 && px <= x + 7
                    || px >= x + 13 && px <= x + 16;
            if (opening) {
                continue;
            }
            b.fill(px, ground + 1, z0, px, ground + 10, z0, RED);
            b.fill(px, ground + 1, z1, px, ground + 10, z1, RED);
        }
        b.fill(x - 22, ground + 8, z0, x + 22, ground + 8, z0, WHITE);
        b.fill(x - 22, ground + 8, z1, x + 22, ground + 8, z1, WHITE);
        // The broad lower eave first, then the storey that rises through it.
        b.pitchedRoof(x - 24, z0 - 2, x + 24, z1 + 2, ground + 10, 6, 0);
        b.fill(x - 16, ground + 11, z - 6, x + 16, ground + 17, z + 6, RED);
        b.fill(x - 14, ground + 12, z - 4, x + 14, ground + 16, z + 4, AIR);
        for (int px = x - 12; px <= x + 12; px += 4) {
            b.window(px, ground + 12, z - 6, 4, 1, true);
            b.window(px, ground + 12, z + 6, 4, 1, true);
        }
        b.pitchedRoof(x - 18, z - 8, x + 18, z + 8, ground + 17, 7, 0);
        b.fill(x - 3, ground + 25, z, x + 3, ground + 25, z, p.roof());
        b.put(x, ground + 26, z, GOLD);
        b.put(x, ground + 27, z, p.light());
    }

    private static void eastGate(StructureBuilder b, Palette p, int ground, int x, int z) {
        pad(b, x - 8, z - 12, x + 8, z + 12, ground, p);
        b.fill(x - 8, ground + 1, z - 12, x + 8, ground + 10, z + 12, p.wall());
        b.fill(x - 8, ground + 1, z - 6, x + 8, ground + 5, z + 6, AIR);
        b.fill(x - 8, ground + 6, z - 12, x + 8, ground + 6, z + 12, p.accent());
        b.pitchedRoof(x - 10, z - 14, x + 10, z + 14, ground + 11, 4, 1);
        b.put(x, ground + 15, z, p.light());
    }

    private static void throneHall(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Two-tier stone terrace with a stepped south approach.
        pad(b, cx - 40, cz - 225, cx + 40, cz - 175, ground, p);
        b.fill(cx - 34, ground + 1, cz - 219, cx + 34, ground + 3, cz - 181, p.foundation());
        for (int s = 0; s < 6; s++) {
            int z = cz - 175 - s;
            int top = ground + (s + 1) / 2;
            b.fill(cx - 8, ground, z, cx + 8, top, z, p.foundation());
        }

        int x0 = cx - 28;
        int x1 = cx + 28;
        int z0 = cz - 214;
        int z1 = cz - 186;
        int y0 = ground + 4;
        b.room(x0, y0, z0, x1, y0 + 14, z1, new Doorway(Side.S, 28),
                new Doorway(Side.N, 28));
        b.fill(x0, y0, z0, x1, y0, z1, p.foundation());
        // Red columns and white eave bands.
        for (int x = x0; x <= x1; x += 4) {
            b.fill(x, y0 + 1, z0, x, y0 + 14, z0, RED);
            b.fill(x, y0 + 1, z1, x, y0 + 14, z1, RED);
        }
        for (int z = z0 + 1; z <= z1 - 1; z += 4) {
            b.fill(x0, y0 + 1, z, x0, y0 + 14, z, RED);
            b.fill(x1, y0 + 1, z, x1, y0 + 14, z, RED);
        }
        b.fill(x0, y0 + 11, z0, x1, y0 + 11, z0, WHITE);
        b.fill(x0, y0 + 11, z1, x1, y0 + 11, z1, WHITE);
        for (int x = x0 + 2; x <= x1 - 2; x += 4) {
            b.window(x, y0 + 2, z0, 6, 1, true);
            b.window(x, y0 + 2, z1, 6, 1, true);
        }
        b.pitchedRoof(x0 - 4, z0 - 4, x1 + 4, z1 + 4, y0 + 14, 8, 0);

        // The clerestory between the two tiled eaves.
        int ux0 = cx - 18;
        int ux1 = cx + 18;
        int uz0 = cz - 206;
        int uz1 = cz - 194;
        for (int y = y0 + 22; y <= y0 + 28; y++) {
            for (int x = ux0; x <= ux1; x++) {
                b.put(x, y, uz0, RED);
                b.put(x, y, uz1, RED);
            }
            for (int z = uz0 + 1; z <= uz1 - 1; z++) {
                b.put(ux0, y, z, RED);
                b.put(ux1, y, z, RED);
            }
        }
        for (int x = ux0 + 2; x <= ux1 - 2; x += 4) {
            b.window(x, y0 + 23, uz0, 4, 1, true);
            b.window(x, y0 + 23, uz1, 4, 1, true);
        }
        b.pitchedRoof(ux0 - 3, uz0 - 3, ux1 + 3, uz1 + 3, y0 + 28, 9, 0);
        b.fill(cx - 3, y0 + 38, cz - 200, cx + 3, y0 + 38, cz - 200, p.roof());
        b.put(cx, y0 + 39, cz - 200, GOLD);
        b.put(cx, y0 + 40, cz - 200, p.light());

        // The throne dais inside.
        b.fill(cx - 6, y0 + 1, cz - 204, cx + 6, y0 + 2, cz - 196, p.foundation());
        b.put(cx, y0 + 3, cz - 200, GOLD);
        b.put(cx, y0 + 4, cz - 200, RED);
    }

    private static void openPavilion(StructureBuilder b, Palette p, int ground, int x, int z) {
        pad(b, x - 6, z - 6, x + 6, z + 6, ground, p);
        b.fill(x - 5, ground + 1, z - 5, x + 5, ground + 1, z + 5, p.foundation());
        for (int dx = -5; dx <= 5; dx += 5) {
            for (int dz = -5; dz <= 5; dz += 5) {
                b.fill(x + dx, ground + 2, z + dz, x + dx, ground + 7, z + dz, RED);
            }
        }
        b.fill(x - 5, ground + 2, z - 5, x + 5, ground + 2, z - 5, p.rail());
        b.fill(x - 5, ground + 2, z - 5, x - 5, ground + 2, z + 5, p.rail());
        b.fill(x + 5, ground + 2, z - 5, x + 5, ground + 2, z + 5, p.rail());
        b.pitchedRoof(x - 8, z - 8, x + 8, z + 8, ground + 8, 4, 0);
        b.put(x, ground + 3, z, p.light());
    }

    private static void longHall(StructureBuilder b, Palette p, int ground, int x, int z,
                                 int w, int d, int h, int door) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        pave(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p.ground());
        b.room(x, ground + 1, z, x1, ground + h, z1, new Doorway(Side.S, door));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        for (int px = x; px <= x1; px += 4) {
            b.fill(px, ground + 2, z, px, ground + h - 1, z, RED);
            b.fill(px, ground + 2, z1, px, ground + h - 1, z1, RED);
        }
        for (int pz = z; pz <= z1; pz += 4) {
            b.fill(x, ground + 2, pz, x, ground + h - 1, pz, RED);
            b.fill(x1, ground + 2, pz, x1, ground + h - 1, pz, RED);
        }
        for (int px = x + 2; px <= x1 - 2; px += 4) {
            b.window(px, ground + 3, z, 3, 1, true);
            b.window(px, ground + 3, z1, 3, 1, true);
        }
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, ground + h, 4, 1);
        doorFor(b, p, x, z, x1, z1, ground + 2, Side.S, door);
    }

    // ------------------------------------------------------------------ palace garden

    private static void garden(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // A low wall around the garden, with a south gate on the avenue.
        gardenWall(b, p, ground, cx - 100, cz + 40, cx - 9, cz + 40);
        gardenWall(b, p, ground, cx + 9, cz + 40, cx + 100, cz + 40);
        gardenWall(b, p, ground, cx - 100, cz - 80, cx - 100, cz + 40);
        gardenWall(b, p, ground, cx + 100, cz - 80, cx + 100, cz + 40);
        for (int s = -1; s <= 1; s += 2) {
            pave(b, cx + s * 10, cz + 38, cx + s * 9, cz + 42, ground, p.ground());
            b.fill(cx + s * 9, ground, cz + 38, cx + s * 10, ground + 4, cz + 42, p.accent());
        }
        b.fill(cx - 10, ground + 4, cz + 40, cx + 10, ground + 4, cz + 40, p.roof());

        pond(b, cx, cz, ground, p);
        pave(b, cx - 18, cz - 32, cx - 6, cz - 28, ground, p.ground());
        b.fill(cx - 18, ground + 1, cz - 31, cx - 17, ground + 1, cz - 29, AIR);
        pave(b, cx + 7, cz - 22, cx + 42, cz - 18, ground, p.ground());
        tomb(b, cx, cz, ground, p);

        flowerBed(b, p, ground, cx + 20, cz + 10, 8, 6);
        flowerBed(b, p, ground, cx - 90, cz - 70, 7, 5);
        flowerBed(b, p, ground, cx + 70, cz + 15, 9, 5);
    }

    private static void gardenWall(StructureBuilder b, Palette p, int ground,
                                   int x0, int z0, int x1, int z1) {
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.fill(x0, ground + 1, z0, x1, ground + 2, z1, p.wall());
        b.fill(x0, ground + 3, z0, x1, ground + 3, z1, p.roofSlab());
    }

    private static void pond(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 70;
        int x1 = cx - 20;
        int z0 = cz - 55;
        int z1 = cz - 5;
        channel(b, x0, z0, x1, z1, ground);
        b.walls(x0 - 2, ground, z0 - 2, x1 + 2, ground + 1, z1 + 2, p.foundation());
        // The islet and its open pavilion.
        b.fill(cx - 53, ground - 2, cz - 38, cx - 37, ground, cz - 22, p.foundation());
        openPavilion(b, p, ground, cx - 45, cz - 30);
        // A short timber bridge from the west bank.
        for (int x = cx - 72; x <= cx - 53; x++) {
            for (int z = cz - 32; z <= cz - 28; z++) {
                b.put(x, ground, z, DECK);
            }
        }
        for (int x = cx - 72; x <= cx - 53; x += 3) {
            b.put(x, ground + 1, cz - 32, p.rail());
            b.put(x, ground + 1, cz - 28, p.rail());
        }
        // Open the rim where the bridge meets the bank.
        b.fill(cx - 72, ground + 1, cz - 31, cx - 71, ground + 1, cz - 29, AIR);
        for (int[] pad : LILIES) {
            b.put(cx + pad[0], ground - 1, cz + pad[1], LILY);
        }
    }

    private static void tomb(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // The stair well descends east from the garden path.
        for (int i = 0; i <= 6; i++) {
            int x = cx + 42 + i;
            b.fill(x, ground - i + 1, cz - 22, x, ground + 1, cz - 18, AIR);
            b.fill(x, ground - i, cz - 22, x, ground - i, cz - 18, p.foundation());
        }
        // A deepslate portal at the head of the stair.
        b.fill(cx + 41, ground + 1, cz - 24, cx + 41, ground + 4, cz - 24, DEEPSLATE);
        b.fill(cx + 41, ground + 1, cz - 16, cx + 41, ground + 4, cz - 16, DEEPSLATE);
        b.fill(cx + 41, ground + 4, cz - 24, cx + 41, ground + 4, cz - 16, DEEPSLATE);

        int x0 = cx + 49;
        int x1 = cx + 66;
        int z0 = cz - 28;
        int z1 = cz - 12;
        b.fill(x0, ground - 7, z0, x1, ground - 7, z1, POLISHED_DEEPSLATE);
        b.fill(x0, ground - 3, z0, x1, ground - 3, z1, DEEPSLATE);
        b.fill(x0, ground - 6, z0, x1, ground - 4, z1, AIR);
        b.walls(x0, ground - 7, z0, x1, ground - 3, z1, DEEPSLATE);
        b.fill(x0, ground - 6, cz - 22, x0, ground - 5, cz - 18, AIR);

        // The queen's sarcophagus, the broken tablet and the seal.
        b.fill(cx + 59, ground - 6, cz - 17, cx + 63, ground - 6, cz - 15,
                POLISHED_DEEPSLATE);
        b.fill(cx + 60, ground - 5, cz - 16, cx + 62, ground - 5, cz - 16, GOLD);
        b.put(cx + 52, ground - 6, cz - 26, DEEPSLATE);
        b.put(cx + 52, ground - 5, cz - 26, DEEPSLATE);
        b.put(cx + 53, ground - 6, cz - 26, p.rubble());
        b.chest(cx + 64, ground - 6, cz - 25, SiteLoot.RELIC);
        b.put(cx + 64, ground - 5, cz - 25, p.light());
        b.put(cx + 51, ground - 6, cz - 27, SOUL);
        b.put(cx + 64, ground - 6, cz - 13, SOUL);
        b.put(cx + 51, ground - 5, cz - 13, Palette.cobweb());
        b.put(cx + 65, ground - 5, cz - 27, Palette.cobweb());
    }

    private static void flowerBed(StructureBuilder b, Palette p, int ground, int x, int z,
                                  int w, int d) {
        pave(b, x, z, x + w - 1, z + d - 1, ground, p.ground());
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                if (((dx + dz) & 1) == 0) {
                    b.put(x + dx, ground + 1, z + dz, GRASS);
                }
            }
        }
        b.put(x + 1, ground + 1, z + 1, POPPY);
        b.put(x + w - 2, ground + 1, z + 1, DANDELION);
        b.put(x + 1, ground + 1, z + d - 2, AZALEA);
    }

    // ------------------------------------------------------------------ Bukchon

    private static void bukchon(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        for (int[] h : HANOKS) {
            hanok(b, p, ground, cx + h[0], cz + h[1], h[2], h[3], h[4], h[5], side(h[6]));
        }

        // The scholar's house, with a forbidden text and a strongbox.
        int sx = cx - 235;
        int sz = cz + 4;
        hanok(b, p, ground, sx, sz, 16, 14, 6, 7, Side.W);
        b.fill(sx + 13, ground + 2, sz + 2, sx + 13, ground + 3, sz + 6, BOOKSHELF);
        b.chest(sx + 13, ground + 2, sz + 9, SiteLoot.CULT);
        b.put(sx + 4, ground + 2, sz + 11, p.roofSlab());

        // The hanok inn.
        int ix = cx - 232;
        int iz = cz + 76;
        hanok(b, p, ground, ix, iz, 16, 14, 7, 8, Side.S);
        b.fill(ix, ground + 4, iz + 13, ix + 15, ground + 4, iz + 13, p.accent());
        b.put(ix + 2, ground + 2, iz + 3, WOOL_WHITE);
        b.put(ix + 3, ground + 2, iz + 3, WOOL_WHITE);
        b.put(ix + 2, ground + 2, iz + 6, WOOL_RED);
        b.put(ix + 3, ground + 2, iz + 6, WOOL_RED);
        b.put(ix + 7, ground + 2, iz + 11, p.light());

        // The herbalist and the physic garden behind it.
        int hx = cx - 234;
        int hz = cz - 114;
        hanok(b, p, ground, hx, hz, 13, 12, 5, 6, Side.S);
        herbGarden(b, p, ground, hx, hz - 11);

        // Walled gardens, ruins and a small mountain shrine.
        walledGarden(b, p, ground, cx - 310, cz - 176, 14, 12);
        walledGarden(b, p, ground, cx - 176, cz - 40, 13, 11);
        walledGarden(b, p, ground, cx - 300, cz + 78, 14, 12);
        ruin(b, p, ground, cx - 266, cz - 30, 10, 8);
        ruin(b, p, ground, cx - 186, cz + 34, 9, 7);
        shrine(b, p, ground, cx - 164, cz + 140);

        // Low walls edging two lanes.
        pave(b, cx - 315, cz - 180, cx - 315, cz - 160, ground, p.ground());
        b.fill(cx - 315, ground + 1, cz - 180, cx - 315, ground + 2, cz - 160, p.wall());
        pave(b, cx - 195, cz + 20, cx - 195, cz + 40, ground, p.ground());
        b.fill(cx - 195, ground + 1, cz + 20, cx - 195, ground + 2, cz + 40, p.wall());
        pave(b, cx - 290, cz - 60, cx - 270, cz - 60, ground, p.ground());
        b.fill(cx - 290, ground + 1, cz - 60, cx - 270, ground + 2, cz - 60, p.wall());
    }

    private static void hanok(StructureBuilder b, Palette p, int ground, int x, int z,
                              int w, int d, int h, int door, Side doorSide) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        pave(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p.ground());
        pad(b, x, z, x1, z1, ground, p);
        b.room(x, ground + 1, z, x1, ground + h, z1, new Doorway(doorSide, door));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        // Dark timber posts on every facade.
        for (int px = x; px <= x1; px += 3) {
            b.fill(px, ground + 2, z, px, ground + h - 1, z, p.accent());
            b.fill(px, ground + 2, z1, px, ground + h - 1, z1, p.accent());
        }
        for (int pz = z; pz <= z1; pz += 3) {
            b.fill(x, ground + 2, pz, x, ground + h - 1, pz, p.accent());
            b.fill(x1, ground + 2, pz, x1, ground + h - 1, pz, p.accent());
        }
        // Papered lattice between the posts.
        for (int px = x + 1; px <= x1 - 1; px += 3) {
            b.window(px, ground + 2, z, Math.max(1, h - 3), 1, true);
            b.window(px, ground + 2, z1, Math.max(1, h - 3), 1, true);
        }
        for (int pz = z + 1; pz <= z1 - 1; pz += 3) {
            b.window(x, ground + 2, pz, Math.max(1, h - 3), 1, true);
            b.window(x1, ground + 2, pz, Math.max(1, h - 3), 1, true);
        }
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, ground + h, 3, 0);
        doorFor(b, p, x, z, x1, z1, ground + 2, doorSide, door);
        b.put(x + 1, ground + 3, z1, p.light());
        b.put((x + x1) / 2, ground + 2, (z + z1) / 2, p.roofSlab());
    }

    private static void herbGarden(StructureBuilder b, Palette p, int ground, int x, int z) {
        pave(b, x, z, x + 9, z + 7, ground, p.ground());
        b.walls(x, ground + 1, z, x + 9, ground + 2, z + 7, p.rail());
        for (int dx = 1; dx < 9; dx += 2) {
            for (int dz = 1; dz < 7; dz += 2) {
                b.put(x + dx, ground + 1, z + dz, GRASS);
                b.put(x + dx + 1, ground + 1, z + dz, AZALEA);
            }
        }
        b.put(x + 4, ground + 1, z + 3, BARREL);
    }

    private static void walledGarden(StructureBuilder b, Palette p, int ground, int x, int z,
                                     int w, int d) {
        pave(b, x, z, x + w - 1, z + d - 1, ground, p.ground());
        b.walls(x, ground + 1, z, x + w - 1, ground + 2, z + d - 1, p.weathered());
        b.put(x + w / 2, ground + 1, z + d - 1, AIR);
        b.put(x + w / 2, ground + 2, z + d - 1, AIR);
        flowerBed(b, p, ground, x + 2, z + 2, w - 4, d - 4);
        pine(b, x + w / 2, z + d / 2, ground);
        b.put(x + 1, ground + 1, z + 1, p.light());
    }

    private static void ruin(StructureBuilder b, Palette p, int ground, int x, int z,
                             int w, int d) {
        pave(b, x - 1, z - 1, x + w, z + d, ground, p.ground());
        b.walls(x, ground + 1, z, x + w - 1, ground + 3, z + d - 1, p.weathered());
        b.put(x + 1, ground + 1, z + d - 1, AIR);
        b.put(x + 1, ground + 2, z + d - 1, AIR);
        b.put(x + w - 1, ground + 2, z + 1, AIR);
        b.fill(x, ground + 1, z, x, ground + 4, z, p.accent());
        b.fill(x + w - 1, ground + 1, z + d - 1, x + w - 1, ground + 3, z + d - 1,
                p.accent());
        b.fill(x + 2, ground + 1, z + 2, x + 5, ground + 1, z + 2, p.accent());
        b.put(x + 3, ground + 1, z + 3, p.rubble());
        b.put(x + 6, ground + 1, z + 4, p.rubble());
        b.put(x + 2, ground + 1, z + d - 3, p.rubble());
        b.fill(x + w - 4, ground + 4, z + 1, x + w - 2, ground + 4, z + 3, p.roofSlab());
    }

    private static void shrine(StructureBuilder b, Palette p, int ground, int x, int z) {
        pave(b, x - 2, z - 2, x + 10, z + 9, ground, p.ground());
        b.fill(x, ground, z, x + 8, ground + 1, z + 7, p.foundation());
        for (int dx : new int[]{1, 7}) {
            for (int dz : new int[]{1, 6}) {
                b.fill(x + dx, ground + 2, z + dz, x + dx, ground + 5, z + dz, RED);
            }
        }
        b.fill(x + 1, ground + 5, z + 1, x + 7, ground + 5, z + 6, p.accent());
        b.pitchedRoof(x - 1, z - 1, x + 9, z + 8, ground + 6, 3, 0);
        b.fill(x + 3, ground + 2, z + 3, x + 5, ground + 2, z + 5, POLISHED_DEEPSLATE);
        b.put(x + 4, ground + 3, z + 4, SOUL);
        b.put(x + 1, ground + 2, z + 7, p.light());
        stoneLantern(b, x - 3, z + 8, ground, p);
        stoneLantern(b, x + 11, z + 8, ground, p);
    }

    // ------------------------------------------------------------------ Namdaemun

    private static void market(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        marketGate(b, p, ground, cx + 220, cz + 80);

        for (int[] s : STALLS) {
            stall(b, p, ground, cx + s[0], cz + s[1], s[2]);
        }

        shop(b, p, ground, cx + 226, cz - 92, 22, 24, 5, 12);
        hanok(b, p, ground, cx + 160, cz - 146, 12, 10, 5, 5, Side.S);
        hanok(b, p, ground, cx + 280, cz + 16, 11, 9, 4, 4, Side.S);
        shop(b, p, ground, cx + 270, cz - 74, 12, 12, 4, 6);
        questBoard(b, p, ground, cx + 226, cz + 6);
        marketWell(b, p, ground, cx + 250, cz - 30);

        for (int[] c : CRATES) {
            b.put(cx + c[0], ground + 1, cz + c[1], BARREL);
            if (c[2] != 0) {
                b.put(cx + c[0], ground + 2, cz + c[1], BARREL);
            }
        }

        // Lantern posts down the main lane.
        for (int lz = -170; lz <= 70; lz += 24) {
            b.fill(cx + 216, ground + 1, cz + lz, cx + 216, ground + 2, cz + lz, p.accent());
            b.put(cx + 216, ground + 3, cz + lz, p.light());
            b.fill(cx + 224, ground + 1, cz + lz, cx + 224, ground + 2, cz + lz, p.accent());
            b.put(cx + 224, ground + 3, cz + lz, p.light());
        }
    }

    private static void marketGate(StructureBuilder b, Palette p, int ground, int x, int z) {
        pad(b, x - 24, z - 6, x + 24, z + 6, ground, p);
        b.fill(x - 24, ground + 1, z - 6, x - 8, ground + 9, z + 6, p.wall());
        b.fill(x + 8, ground + 1, z - 6, x + 24, ground + 9, z + 6, p.wall());
        b.fill(x - 8, ground + 6, z - 6, x + 8, ground + 9, z + 6, p.accent());
        // The broad lower eave first, then the storey that rises through it.
        b.pitchedRoof(x - 26, z - 8, x + 26, z + 8, ground + 9, 5, 0);
        b.fill(x - 16, ground + 10, z - 5, x + 16, ground + 15, z + 5, p.accent());
        b.fill(x - 14, ground + 11, z - 3, x + 14, ground + 14, z + 3, AIR);
        for (int px = x - 12; px <= x + 12; px += 4) {
            b.window(px, ground + 11, z - 5, 3, 1, true);
            b.window(px, ground + 11, z + 5, 3, 1, true);
        }
        b.pitchedRoof(x - 18, z - 7, x + 18, z + 7, ground + 15, 5, 0);
        b.fill(x - 3, ground + 21, z, x + 3, ground + 21, z, p.roof());
        b.put(x, ground + 22, z, p.light());
    }

    private static void shop(StructureBuilder b, Palette p, int ground, int x, int z,
                             int w, int d, int h, int door) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        pave(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p.ground());
        pad(b, x, z, x1, z1, ground, p);
        b.room(x, ground + 1, z, x1, ground + h, z1, new Doorway(Side.W, door));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        for (int pz = z; pz <= z1; pz += 3) {
            b.fill(x, ground + 2, pz, x, ground + h - 1, pz, p.accent());
            b.fill(x1, ground + 2, pz, x1, ground + h - 1, pz, p.accent());
        }
        for (int px = x; px <= x1; px += 4) {
            b.fill(px, ground + 2, z, px, ground + h - 1, z, p.accent());
            b.fill(px, ground + 2, z1, px, ground + h - 1, z1, p.accent());
        }
        // A striped awning over the west front.
        b.fill(x - 3, ground + h, z + 1, x - 1, ground + h, z1 - 1, p.roofSlab());
        for (int pz = z + 1; pz <= z1 - 1; pz += 4) {
            b.fill(x - 3, ground + 2, pz, x - 3, ground + h - 1, pz, p.accent());
        }
        b.fill(x + 1, ground + 2, z + 2, x + 1, ground + 2, z1 - 2, p.foundation());
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, ground + h, 3, 0);
        doorFor(b, p, x, z, x1, z1, ground + 2, Side.W, door);
        b.put(x + 2, ground + 2, z + 2, p.light());
    }

    private static void stall(StructureBuilder b, Palette p, int ground, int x, int z,
                              int stripe) {
        BlockState cloth = (stripe & 1) == 0 ? WOOL_WHITE : WOOL_RED;
        pave(b, x, z, x + 2, z + 2, ground, p.ground());
        for (int dx : new int[]{0, 2}) {
            for (int dz : new int[]{0, 2}) {
                b.fill(x + dx, ground + 1, z + dz, x + dx, ground + 2, z + dz, p.accent());
            }
        }
        b.fill(x, ground + 3, z, x + 2, ground + 3, z + 2, cloth);
        b.put(x + 1, ground + 1, z, p.foundation());
        b.put(x + 1, ground + 1, z + 2, BARREL);
    }

    private static void questBoard(StructureBuilder b, Palette p, int ground, int x, int z) {
        pave(b, x - 1, z - 1, x + 11, z + 9, ground, p.ground());
        for (int dx : new int[]{0, 10}) {
            for (int dz : new int[]{0, 8}) {
                b.fill(x + dx, ground + 1, z + dz, x + dx, ground + 4, z + dz, p.accent());
            }
        }
        b.fill(x - 1, ground + 5, z - 1, x + 11, ground + 5, z + 9, p.roofSlab());
        b.fill(x + 9, ground + 2, z + 1, x + 9, ground + 4, z + 7, p.frame());
        b.fill(x + 8, ground + 1, z + 2, x + 8, ground + 2, z + 2, p.accent());
        b.fill(x + 8, ground + 1, z + 6, x + 8, ground + 2, z + 6, p.accent());
        CityPlanSupport.sign(b, x + 8, ground + 3, z + 2, Direction.WEST,
                "ROYAL GHOST", "PALACE CLERK", "SEEK THE TOMB", "UNDER GARDEN");
        CityPlanSupport.sign(b, x + 8, ground + 3, z + 6, Direction.WEST,
                "MISSING", "SCHOLAR", "BUKCHON HOUSE", "REWARD 50");
        b.fill(x + 1, ground + 1, z + 1, x + 1, ground + 2, z + 1, p.accent());
        b.put(x + 1, ground + 3, z + 1, p.light());
    }

    private static void marketWell(StructureBuilder b, Palette p, int ground, int x, int z) {
        pave(b, x - 3, z - 3, x + 3, z + 3, ground, p.ground());
        b.fill(x - 2, ground + 1, z - 2, x + 2, ground + 1, z + 2, p.foundation());
        b.put(x, ground + 1, z, WATER);
        b.put(x, ground + 2, z - 2, p.light());
        b.put(x - 2, ground + 1, z, p.rail());
        b.put(x + 2, ground + 1, z, p.rail());
    }

    // ------------------------------------------------------------------ modern quarter

    private static void modern(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        bank(b, p, ground, cx + 150, cz + 136);
        telegraph(b, p, ground, cx + 268, cz + 136);
        police(b, p, ground, cx + 248, cz + 176);
        brickBuilding(b, p, ground, cx + 200, cz + 108, 22, 16, 7, 10, BRICK);
        brickBuilding(b, p, ground, cx + 252, cz + 108, 22, 16, 8, 10, MUD_BRICK);
        brickBuilding(b, p, ground, cx + 200, cz + 144, 22, 16, 6, 10, MUD_BRICK);

        brickBuilding(b, p, ground, cx + 150, cz + 258, 20, 14, 6, 9, BRICK);
        brickBuilding(b, p, ground, cx + 188, cz + 258, 20, 14, 7, 9, MUD_BRICK);
        brickBuilding(b, p, ground, cx + 252, cz + 258, 20, 14, 6, 9, BRICK);
        brickBuilding(b, p, ground, cx + 276, cz + 258, 16, 14, 7, 7, MUD_BRICK);
        clockTower(b, p, ground, cx + 230, cz + 268);
        tram(b, p, ground, cx, cz);
        park(b, p, ground, cx + 306, cz + 258);

        for (int lx : MODERN_X) {
            for (int lz : MODERN_Z_NORTH) {
                streetLamp(b, p, ground, cx + lx + 6, cz + lz);
            }
            for (int lz : MODERN_Z_SOUTH) {
                streetLamp(b, p, ground, cx + lx + 6, cz + lz);
            }
        }
    }

    private static void bank(StructureBuilder b, Palette p, int ground, int x, int z) {
        int x1 = x + 22;
        int z1 = z + 28;
        pave(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p.ground());
        pad(b, x, z, x1, z1, ground, p);
        b.room(x, ground + 1, z, x1, ground + 12, z1, new Doorway(Side.S, 11));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        b.walls(x, ground + 2, z, x1, ground + 11, z1, QUARTZ);
        for (int px = x + 2; px <= x1 - 2; px += 5) {
            b.fill(px, ground + 2, z1, px, ground + 9, z1, QUARTZ);
            b.put(px, ground + 10, z1, p.foundation());
        }
        for (int wy = ground + 3; wy <= ground + 9; wy += 3) {
            for (int px = x + 3; px <= x1 - 3; px += 4) {
                b.window(px, wy, z, 2, 1, true);
            }
        }
        b.fill(x - 2, ground + 12, z - 1, x1 + 2, ground + 12, z1 + 1, p.roofSlab());
        b.pitchedRoof(x + 2, z1 - 4, x1 - 2, z1 + 4, ground + 12, 3, 0);
        door(b, p, x + 11, ground + 2, z1, Direction.SOUTH);
        b.put(x + 11, ground + 3, z1 - 1, p.light());
    }

    private static void police(StructureBuilder b, Palette p, int ground, int x, int z) {
        int x1 = x + 24;
        int z1 = z + 15;
        pave(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p.ground());
        pad(b, x, z, x1, z1, ground, p);
        b.room(x, ground + 1, z, x1, ground + 9, z1, new Doorway(Side.S, 12));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        b.walls(x, ground + 2, z, x1, ground + 8, z1, BRICK);
        b.fill(x, ground + 4, z1, x1, ground + 4, z1, BLUE);
        b.fill(x, ground + 4, z, x1, ground + 4, z, BLUE);
        for (int wy = ground + 5; wy <= ground + 7; wy += 2) {
            for (int px = x + 3; px <= x1 - 3; px += 4) {
                b.window(px, wy, z1, 1, 1, true);
            }
        }
        b.fill(x, ground + 9, z, x1, ground + 9, z1, p.roofSlab());
        b.walls(x, ground + 10, z, x1, ground + 10, z1, p.foundation());
        door(b, p, x + 12, ground + 2, z1, Direction.SOUTH);
        b.put(x + 1, ground + 5, z1, p.light());
    }

    private static void telegraph(StructureBuilder b, Palette p, int ground, int x, int z) {
        int x1 = x + 22;
        int z1 = z + 28;
        pave(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p.ground());
        pad(b, x, z, x1, z1, ground, p);
        b.room(x, ground + 1, z, x1, ground + 10, z1, new Doorway(Side.S, 11));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        b.walls(x, ground + 2, z, x1, ground + 9, z1, MUD_BRICK);
        for (int wy = ground + 3; wy <= ground + 8; wy += 3) {
            for (int px = x + 3; px <= x1 - 3; px += 4) {
                b.window(px, wy, z1, 1, 1, true);
                b.window(px, wy, z, 1, 1, true);
            }
        }
        b.fill(x, ground + 10, z, x1, ground + 10, z1, p.roofSlab());
        b.fill(x + 11, ground + 11, z + 4, x + 11, ground + 24, z + 4, p.accent());
        b.fill(x + 7, ground + 20, z + 4, x + 15, ground + 20, z + 4, p.rail());
        b.fill(x + 8, ground + 23, z + 4, x + 14, ground + 23, z + 4, p.rail());
        b.put(x + 11, ground + 25, z + 4, p.light());
        door(b, p, x + 11, ground + 2, z1, Direction.SOUTH);
    }

    private static void brickBuilding(StructureBuilder b, Palette p, int ground, int x, int z,
                                      int w, int d, int h, int door, BlockState brick) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        pave(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p.ground());
        pad(b, x, z, x1, z1, ground, p);
        b.room(x, ground + 1, z, x1, ground + h, z1, new Doorway(Side.S, door));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        b.walls(x, ground + 2, z, x1, ground + h - 1, z1, brick);
        for (int y = ground + 2; y <= ground + h; y++) {
            b.put(x, y, z, p.foundation());
            b.put(x1, y, z, p.foundation());
            b.put(x, y, z1, p.foundation());
            b.put(x1, y, z1, p.foundation());
        }
        for (int wy = ground + 3; wy <= ground + h - 2; wy += 3) {
            for (int px = x + 2; px <= x1 - 2; px += 3) {
                b.window(px, wy, z, 2, 1, true);
                b.window(px, wy, z1, 2, 1, true);
            }
            for (int pz = z + 2; pz <= z1 - 2; pz += 3) {
                b.window(x, wy, pz, 2, 1, true);
                b.window(x1, wy, pz, 2, 1, true);
            }
        }
        b.fill(x, ground + h, z, x1, ground + h, z1, p.roofSlab());
        b.walls(x, ground + h + 1, z, x1, ground + h + 1, z1, p.foundation());
        door(b, p, x + door, ground + 2, z1, Direction.SOUTH);
        b.put(x + 2, ground + 3, z1, p.light());
    }

    private static void clockTower(StructureBuilder b, Palette p, int ground, int x, int z) {
        pad(b, x - 4, z - 4, x + 4, z + 4, ground, p);
        b.fill(x - 4, ground + 1, z - 4, x + 4, ground + 18, z + 4, p.wall());
        b.fill(x - 3, ground + 2, z - 3, x + 3, ground + 17, z + 3, AIR);
        b.walls(x - 4, ground + 2, z - 4, x + 4, ground + 17, z + 4, p.wall());
        for (int y = ground + 3; y <= ground + 15; y += 4) {
            b.window(x - 1, y, z - 4, 2, 1, true);
            b.window(x + 1, y, z + 4, 2, 1, true);
        }
        b.put(x, ground + 14, z + 4, CLOCK_FACE);
        b.put(x, ground + 14, z - 4, CLOCK_FACE);
        b.put(x + 4, ground + 14, z, CLOCK_FACE);
        b.put(x - 4, ground + 14, z, CLOCK_FACE);
        b.fill(x - 5, ground + 19, z - 5, x + 5, ground + 19, z + 5, p.foundation());
        b.pitchedRoof(x - 5, z - 5, x + 5, z + 5, ground + 19, 6, 0);
        b.put(x, ground + 26, z, p.light());
    }

    private static void tram(StructureBuilder b, Palette p, int ground, int cx, int cz) {
        for (int x = cx + 148; x <= cx + 312; x++) {
            b.put(x, ground, cz + 280, RAIL);
        }
        int x0 = cx + 218;
        int x1 = cx + 232;
        b.fill(x0, ground + 1, cz + 278, x1, ground + 1, cz + 282, IRON);
        b.walls(x0, ground + 2, cz + 278, x1, ground + 4, cz + 282, DECK);
        b.fill(x0 + 2, ground + 2, cz + 278, x1 - 2, ground + 3, cz + 278, p.window());
        b.fill(x0 + 2, ground + 2, cz + 282, x1 - 2, ground + 3, cz + 282, p.window());
        b.fill(x0, ground + 5, cz + 278, x1, ground + 5, cz + 282, p.roofSlab());
        b.put(x0, ground + 3, cz + 280, p.light());
        b.put(x1, ground + 3, cz + 280, p.light());
        for (int x = cx + 160; x <= cx + 300; x += 35) {
            b.fill(x, ground + 1, cz + 284, x, ground + 6, cz + 284, p.accent());
            b.put(x, ground + 6, cz + 284, p.light());
        }
    }

    private static void park(StructureBuilder b, Palette p, int ground, int x, int z) {
        pave(b, x, z, x + 14, z + 18, ground, p.ground());
        b.walls(x, ground + 1, z, x + 14, ground + 1, z + 18, p.rail());
        b.put(x + 7, ground + 1, z, AIR);
        b.put(x + 7, ground + 1, z + 18, AIR);
        b.fill(x + 6, ground + 1, z + 8, x + 8, ground + 1, z + 10, p.foundation());
        b.put(x + 7, ground + 1, z + 9, WATER);
        b.put(x + 7, ground + 2, z + 9, p.light());
        b.fill(x + 2, ground + 1, z + 4, x + 4, ground + 1, z + 4, p.roofSlab());
        b.fill(x + 10, ground + 1, z + 14, x + 12, ground + 1, z + 14, p.roofSlab());
    }

    private static void streetLamp(StructureBuilder b, Palette p, int ground, int x, int z) {
        b.fill(x, ground + 1, z, x, ground + 4, z, p.accent());
        b.put(x, ground + 5, z, p.light());
    }

    // ------------------------------------------------------------------ rail station

    private static void station(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 210;
        int x1 = cx + 235;
        int z0 = cz - 211;
        int z1 = cz - 184;
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0 - 1, ground + 1, z0 - 1, x1 + 1, ground + 1, z1 + 1, p.rail());
        b.fill(cx + 215, ground + 1, z1 + 1, cx + 223, ground + 1, z1 + 1, AIR);
        b.fill(cx + 221, ground + 1, z0 - 1, cx + 223, ground + 1, z0 - 1, AIR);
        b.fill(cx + 210, ground, cz - 189, cx + 235, ground, cz - 185, p.ground());
        // Straight, level track from the north district edge through the yard.
        b.fill(cx + 221, ground, cz - 340, cx + 223, ground, cz - 185, p.rubble());
        for (int z = cz - 340; z <= cz - 185; z++) {
            b.put(cx + 222, ground, z, RAIL);
        }
        b.fill(cx + 219, ground + 1, cz - 208, cx + 221, ground + 1, cz - 190, p.foundation());
        b.fill(cx + 223, ground + 1, cz - 208, cx + 225, ground + 1, cz - 190, p.foundation());
        stationHall(b, p, ground, cx + 210, cz - 208, 9, 19);
        goodsShed(b, p, ground, cx + 226, cz - 208, 10, 19);
        for (int z = cz - 206; z <= cz - 192; z += 4) {
            b.fill(cx + 219, ground + 2, z, cx + 219, ground + 5, z, p.accent());
            b.fill(cx + 225, ground + 2, z, cx + 225, ground + 5, z, p.accent());
        }
        b.fill(cx + 218, ground + 6, cz - 209, cx + 226, ground + 6, cz - 189, p.roofSlab());
        b.fill(cx + 221, ground + 1, cz - 186, cx + 223, ground + 1, cz - 185, p.foundation());
        b.put(cx + 215, ground + 1, cz - 188, BARREL);
        b.put(cx + 230, ground + 1, cz - 188, BARREL);
        b.fill(cx + 215, ground, cz - 183, cx + 223, ground, cz - 180, p.ground());
        CityPlanSupport.sign(b, cx + 218, ground + 4, cz - 194, Direction.EAST,
                "SEOUL STATION", "EAST LINE", "GOODS YARD");
    }

    private static void stationHall(StructureBuilder b, Palette p, int ground, int x, int z,
                                    int w, int d) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        b.fill(x, ground, z, x1, ground, z1, p.foundation());
        b.room(x, ground + 1, z, x1, ground + 6, z1, new Doorway(Side.E, d / 2));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        for (int y = ground + 2; y <= ground + 6; y++) {
            b.put(x, y, z, p.accent());
            b.put(x1, y, z, p.accent());
            b.put(x, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
        b.window(x1, ground + 3, z + 3, 2, 1, true);
        b.window(x1, ground + 3, z1 - 3, 2, 1, true);
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, ground + 6, 4, 1);
        doorFor(b, p, x, z, x1, z1, ground + 2, Side.E, d / 2);
        b.fill(x + 2, ground + 2, z + 2, x + 2, ground + 2, z1 - 2, p.foundation());
        b.put(x + 1, ground + 2, z + 1, p.light());
    }

    private static void goodsShed(StructureBuilder b, Palette p, int ground, int x, int z,
                                  int w, int d) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        b.fill(x, ground, z, x1, ground, z1, p.foundation());
        b.room(x, ground + 1, z, x1, ground + 5, z1, new Doorway(Side.W, d / 2));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        for (int y = ground + 2; y <= ground + 5; y++) {
            b.put(x, y, z, p.accent());
            b.put(x1, y, z, p.accent());
            b.put(x, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
        b.window(x, ground + 3, z + 3, 1, 1, true);
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, ground + 5, 3, 1);
        doorFor(b, p, x, z, x1, z1, ground + 2, Side.W, d / 2);
        b.put(x + 2, ground + 2, z + 2, BARREL);
        b.put(x + 3, ground + 2, z + 3, BARREL);
        b.put(x + 2, ground + 2, z1 - 2, BARREL);
    }

    // ------------------------------------------------------------------ trees

    private static void trees(StructureBuilder b, int cx, int cz, int ground) {
        for (int[] t : PINES) {
            pine(b, cx + t[0], cz + t[1], ground);
        }
    }

    private static void pine(StructureBuilder b, int x, int z, int ground) {
        int trunk = 3 + ((x + z) & 1);
        for (int i = 1; i <= trunk; i++) {
            b.put(x, ground + i, z, PINE_LOG);
        }
        for (int dy = 2; dy <= trunk + 1; dy++) {
            int r = dy >= trunk ? 1 : 2;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) > r + 1) {
                        continue;
                    }
                    if (dx == 0 && dz == 0 && dy <= trunk) {
                        continue;
                    }
                    b.put(x + dx, ground + dy, z + dz, PINE_LEAVES);
                }
            }
        }
        b.put(x, ground + trunk + 2, z, PINE_LEAVES);
    }

    private static void stoneLantern(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground + 1, z, p.foundation());
        b.put(x, ground + 2, z, p.wall());
        b.put(x, ground + 3, z, p.light());
        b.put(x, ground + 4, z, p.roofSlab());
    }

    // ------------------------------------------------------------------ readable signs

    private static void signs(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        signPost(b, p, cx + 8, ground, cz + 306, Direction.SOUTH,
                "GYEONGBOKGUNG", "BUKCHON", "NAMDAEMUN", "MODERN QTR");
        signPost(b, p, cx - 150, ground, cz + 64, Direction.EAST,
                "BUKCHON", "HANOK LANES", "INN & TEA", "SCHOLAR HOUSE");
        signPost(b, p, cx + 218, ground, cz + 88, Direction.SOUTH,
                "NAMDAEMUN", "MARKET", "TEA HOUSES", "CITY NOTICES");
        signPost(b, p, cx + 146, ground, cz + 244, Direction.SOUTH,
                "MODERN QTR", "BANK", "POLICE", "TELEGRAPH");
        signPost(b, p, cx + 12, ground, cz + 240, Direction.SOUTH,
                "HAN RIVER", "DEEP WATER", "NO SWIMMING");
        signPost(b, p, cx + 42, ground, cz - 30, Direction.WEST,
                "PALACE TOMB", "KEEP OUT", "THE QUEEN", "SLEEPS BELOW");
        signPost(b, p, cx - 234, ground, cz + 91, Direction.SOUTH,
                "HANOK INN", "BEDS & TEA", "KEEPER INSIDE");
        signPost(b, p, cx + 224, ground, cz - 80, Direction.WEST,
                "MARKET SHOP", "BRASS & SILK");
        signPost(b, p, cx - 228, ground, cz - 101, Direction.SOUTH,
                "HERBALIST", "REMEDIES", "HERBS & ROOTS");
        signPost(b, p, cx - 14, ground, cz - 74, Direction.SOUTH,
                "GYEONGBOKGUNG", "THRONE HALL", "GARDEN TOMB");
        signPost(b, p, cx + 30, ground + 1, cz + 201, Direction.WEST,
                "FERRY", "HAN RIVER", "CROSS HERE");
        signPost(b, p, cx + 216, ground, cz + 284, Direction.SOUTH,
                "STEAM TRAM", "RIVER STOP");
        signPost(b, p, cx + 160, ground, cz + 166, Direction.SOUTH,
                "BANK", "COIN & PAPER");
        signPost(b, p, cx + 260, ground, cz + 193, Direction.SOUTH,
                "POLICE", "CONSTABLE", "RUMOURS");
        signPost(b, p, cx - 160, ground, cz + 148, Direction.SOUTH,
                "SHRINE", "MOUNTAIN SPIRIT");
        signPost(b, p, cx - 235, ground, cz + 18, Direction.WEST,
                "SCHOLAR HOUSE", "FORBIDDEN TEXT", "MISSING OWNER");
        signPost(b, p, cx + 160, ground, cz - 148, Direction.SOUTH,
                "TEA HOUSE", "GREEN TEA", "REST HERE");
        signPost(b, p, cx + 14, ground, cz - 100, Direction.NORTH,
                "PALACE CLERK", "ROYAL GHOST", "SPEAK HERE");
        CityPlanSupport.sign(b, cx + 52, ground - 6, cz - 20, Direction.WEST,
                "HERE LIES THE", "FORGOTTEN QUEEN", "HER SEAL IS", "TAKE IT NOT");
        CityPlanSupport.sign(b, cx + 54, ground - 6, cz - 26, Direction.WEST,
                "BROKEN TABLET", "RESTORE IT");
    }

    private static void signPost(StructureBuilder b, Palette p, int x, int y, int z,
                                 Direction facing, String... lines) {
        b.put(x, y, z, p.accent());
        CityPlanSupport.sign(b, x, y + 1, z, facing, lines);
    }

    // ------------------------------------------------------------------ doors

    private static void doorFor(StructureBuilder b, Palette p, int x0, int z0, int x1, int z1,
                                int y, Side side, int offset) {
        switch (side) {
            case S -> door(b, p, x0 + offset, y, z1, Direction.SOUTH);
            case N -> door(b, p, x0 + offset, y, z0, Direction.NORTH);
            case W -> door(b, p, x0, y, z0 + offset, Direction.WEST);
            case E -> door(b, p, x1, y, z0 + offset, Direction.EAST);
        }
    }

    private static void door(StructureBuilder b, Palette p, int x, int y, int z,
                             Direction facing) {
        b.put(x, y, z, AIR);
        b.put(x, y + 1, z, AIR);
        BlockState lower = p.door()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        b.put(x, y, z, lower);
        b.put(x, y + 1, z,
                lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
    }

    private static Side side(int code) {
        return switch (code) {
            case 1 -> Side.W;
            case 2 -> Side.E;
            case 3 -> Side.N;
            default -> Side.S;
        };
    }
}
