package com.sanchous98.eldritchhorror.world.loc.city.plan;

import net.minecraft.world.item.DyeColor;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.SiteLoot;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import com.sanchous98.eldritchhorror.world.loc.city.CitySurface;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.RailShape;

/**
 * Moscow between revolutions: St Basil's coloured onions on Red Square, the Kremlin's red wall
 * and white palace, Arbat's tenements and cafés, a marble metro under the avenue, factory chimneys
 * on the south bank, and the frozen Moskva with its bridges and barges.
 *
 * <p>Everything is authored: absolute offsets from {@code (cx, cz)}, the city's {@code ground}
 * level, and explicit arrays/loops. There is no {@code RandomSource}, no layout hash and no
 * chunk-local read. {@link StructureBuilder} clips every write to the generating chunk, so the
 * same call builds the same city slice in every chunk. Streets and water are laid first, then the
 * districts, so no facade can overwrite a road or a quay.
 */
public final class MoscowPlan implements CityPlan {

    /** Authored half-extent (blocks). */
    private static final int HALF = 340;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState GOLD = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final BlockState END_ROD = Blocks.END_ROD.defaultBlockState();
    private static final BlockState BLACKSTONE = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
    private static final BlockState DARK_STONE = Blocks.BLACKSTONE.defaultBlockState();
    private static final BlockState PALACE = Blocks.CONCRETE.pick(DyeColor.YELLOW).defaultBlockState();
    private static final BlockState WHITE = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState MARBLE = Blocks.QUARTZ_BLOCK.defaultBlockState();
    private static final BlockState IRON = Blocks.IRON_BLOCK.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState COAL = Blocks.COAL_BLOCK.defaultBlockState();
    private static final BlockState SPRUCE_LOG = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState SPRUCE_LEAVES = Blocks.SPRUCE_LEAVES.defaultBlockState();
    private static final BlockState SNOW_LAYER = Blocks.SNOW.defaultBlockState();
    private static final BlockState BOOKSHELF = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState BARREL = Blocks.BARREL.defaultBlockState();
    private static final BlockState RAIL_EW = Blocks.RAIL.defaultBlockState()
            .setValue(BlockStateProperties.RAIL_SHAPE, RailShape.EAST_WEST);
    private static final BlockState RAIL_NS = Blocks.RAIL.defaultBlockState()
            .setValue(BlockStateProperties.RAIL_SHAPE, RailShape.NORTH_SOUTH);

    /** Arbat west tenements: x, z, width, depth, height, door side (0 S, 1 W, 2 E, 3 N). */
    private static final int[][] ARBAT_WEST = {
            {-318, -238, 30, 14, 16, 2},
            {-318, -214, 30, 18, 14, 2},
            {-284, -214, 30, 18, 18, 2},
            {-318, -164, 30, 20, 15, 2},
            {-284, -164, 30, 20, 17, 2},
            {-318, -104, 32, 14, 14, 2},
            {-282, -104, 30, 14, 16, 2},
            {-318, -44, 30, 20, 18, 2},
            {-284, -44, 30, 20, 15, 2},
            {-318, 16, 30, 20, 16, 2},
            {-284, 16, 30, 20, 14, 2},
            {-318, 76, 30, 20, 15, 2},
            {-284, 76, 30, 20, 17, 2},
            {-318, 136, 30, 14, 12, 2},
            {-284, 136, 30, 14, 14, 2}
    };

    /** Arbat east tenements: x, z, width, depth, height, door side. */
    private static final int[][] ARBAT_EAST = {
            {-232, -238, 30, 14, 15, 1},
            {-232, -214, 30, 18, 17, 1},
            {-198, -214, 30, 18, 14, 1},
            {-232, -164, 30, 20, 16, 1},
            {-198, -164, 30, 20, 18, 1},
            {-232, -104, 32, 14, 15, 1},
            {-196, -104, 30, 14, 17, 1},
            {-232, -44, 30, 20, 16, 1},
            {-196, -44, 30, 20, 14, 1},
            {-232, 16, 30, 20, 18, 1},
            {-196, 16, 30, 20, 15, 1},
            {-232, 76, 30, 20, 16, 1},
            {-196, 76, 30, 20, 14, 1},
            {-232, 136, 30, 14, 13, 1},
            {-196, 136, 30, 14, 15, 1}
    };

    /** Arbat cafés: x, z, width, depth, height, door side. */
    private static final int[][] ARBAT_CAFES = {
            {-318, -76, 16, 14, 7, 2},
            {-296, 104, 16, 14, 7, 2},
            {-232, -18, 18, 14, 7, 1}
    };

    /** Arbat courtyards: x0, z0, x1, z1. */
    private static final int[][] ARBAT_COURTS = {
            {-284, -82, -254, -60},
            {-196, -26, -166, -8},
            {-318, 104, -302, 124}
    };

    /** Trees in the Patriarch's Ponds park: x, z. */
    private static final int[][] PARK_TREES = {
            {-140, 20}, {-134, 22}, {-120, 20}, {-100, 18}, {-82, 20}, {-58, 18},
            {-140, 34}, {-140, 56}, {-140, 78}, {-136, 90}, {-108, 90}, {-80, 90},
            {-56, 90}, {-50, 72}, {-50, 44}, {-58, 28}, {-130, 44}, {-130, 64}
    };

    /** Trees on the embankments: x, z. */
    private static final int[][] BANK_TREES = {
            {-312, 160}, {-272, 162}, {-200, 160}, {-160, 162}, {-120, 160}, {-50, 162},
            {40, 160}, {90, 162}, {130, 160}, {170, 162}, {270, 160}, {312, 162},
            {-312, 194}, {-272, 196}, {-200, 194}, {-160, 196}, {-120, 194}, {-50, 196},
            {40, 194}, {90, 196}, {130, 194}, {170, 196}, {270, 194}, {312, 196}
    };

    /** Trees in the Kremlin garden: x, z. */
    private static final int[][] KREMLIN_TREES = {
            {270, -140}, {282, -140}, {296, -140}, {304, -128}, {304, -116},
            {270, -116}, {270, -128}, {294, -112}
    };

    @Override
    public String cityId() {
        return "moscow";
    }

    @Override
    public int halfExtent() {
        return HALF;
    }

    @Override
    public void build(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Legacy entry point: approximate the shared graded surface with a 48-block rim.
        buildCity(b, cx, cz, ground, CitySurface.of(b, cx, cz, HALF, ground, 48), p);
    }

    @Override
    public void build(StructureBuilder b, CityPlanContext ctx, Palette p) {
        buildCity(b, ctx.cx(), ctx.cz(), ctx.ground(), ctx.surface(), p);
    }

    private static void buildCity(StructureBuilder b, int cx, int cz, int ground,
                                  CitySurface surface, Palette p) {
        // 1. Streets and water first, so no facade can overwrite a road or a quay.
        streets(b, cx, cz, ground, surface, p);
        river(b, cx, cz, ground, surface, p);

        // 2. The districts themselves.
        arrival(b, cx, cz, ground, surface, p);
        redSquare(b, cx, cz, ground, p);
        stBasil(b, cx, cz, ground, p);
        kremlin(b, cx, cz, ground, surface, p);
        arbat(b, cx, cz, ground, surface, p);
        metro(b, cx, cz, ground, p);
        industrial(b, cx, cz, ground, surface, p);
        parks(b, cx, cz, ground, p);
        embankment(b, cx, cz, ground, surface, p);

        // 3. Readable notices last, on top of the built fabric.
        signs(b, cx, cz, ground, surface, p);

        b.marker("moscow_center", cx, ground + 1, cz);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.SIGN, 16, 1, 310, "districts"),
                new CityPoi("square_quest_board", CityPoi.Kind.QUEST_BOARD, 0, 0, -120,
                        "moscow_stolen_icon;moscow_missing_commissar"),
                new CityPoi("metropol_hotel", CityPoi.Kind.INN, -80, 0, -120, "hotel_keeper"),
                new CityPoi("arbat_shop", CityPoi.Kind.SHOP, -240, 0, -60, "merchant"),
                new CityPoi("herbalist", CityPoi.Kind.HEALER, -240, 0, 120, "herbalist"),
                new CityPoi("icon_chapel", CityPoi.Kind.NPC, -260, 0, -120,
                        "moscow_stolen_icon"),
                new CityPoi("commissar_office", CityPoi.Kind.NPC, 240, 0, -160,
                        "moscow_missing_commissar"),
                new CityPoi("metro_tunnel", CityPoi.Kind.SECRET, 0, -8, 40,
                        "moscow_stolen_icon"),
                new CityPoi("kremlin_gate", CityPoi.Kind.LANDMARK, 150, 0, -160, "guards"),
                new CityPoi("factory", CityPoi.Kind.LANDMARK, 240, 0, 240, "chimneys"),
                new CityPoi("rail_station", CityPoi.Kind.STATION, 200, 0, 288,
                        "moscow_rail;east_west"),
                new CityPoi("hotel_keeper", CityPoi.Kind.NPC, -86, 0, -114, "hotel_keeper"),
                new CityPoi("merchant", CityPoi.Kind.NPC, -228, 0, -66, "merchant"),
                new CityPoi("herbalist_keeper", CityPoi.Kind.NPC, -228, 0, 114, "herbalist"),
                new CityPoi("priest", CityPoi.Kind.NPC, -266, 0, -124, "moscow_stolen_icon"),
                new CityPoi("commissar", CityPoi.Kind.NPC, 242, 0, -158,
                        "moscow_missing_commissar"),
                new CityPoi("station_chief", CityPoi.Kind.NPC, 50, 0, 90, "station_chief"),
                new CityPoi("worker", CityPoi.Kind.NPC, 250, 0, 232,
                        "moscow_missing_commissar"),
                new CityPoi("metro_sign", CityPoi.Kind.SIGN, 20, 1, 112, "metro"),
                new CityPoi("arbat_sign", CityPoi.Kind.SIGN, -240, 1, -150, "arbat"),
                new CityPoi("kremlin_sign", CityPoi.Kind.SIGN, 136, 1, -144, "kremlin"),
                new CityPoi("factory_sign", CityPoi.Kind.SIGN, 232, 1, 208, "factory"),
                new CityPoi("river_sign", CityPoi.Kind.SIGN, 20, 1, 158, "river"),
                new CityPoi("secret_lore", CityPoi.Kind.SIGN, -8, -8, 22,
                        "moscow_stolen_icon")
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

    /** Paves a street on the shared graded surface, one column at a time. */
    private static void paveSurface(StructureBuilder b, CitySurface surface, int x0, int z0,
                                    int x1, int z1, BlockState state, Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                int y = surface.at(x, z);
                settle(b, x, z, y, p);
                b.put(x, y, z, state);
            }
        }
    }

    /** Cuts the river channel on the shared graded surface, one column at a time. */
    private static void channelSurface(StructureBuilder b, CitySurface surface, int x0, int z0,
                                       int x1, int z1, Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                int y = surface.at(x, z);
                int terrain = b.groundY(x, z);
                if (terrain > y - 2) {
                    b.fill(x, y - 1, z, x, terrain + 2, z, AIR);
                } else if (terrain < y - 4) {
                    b.fill(x, terrain + 1, z, x, y - 4, z, p.foundation());
                }
                b.put(x, y - 4, z, p.foundation());
                b.put(x, y - 3, z, SAND);
                b.put(x, y - 2, z, WATER);
                b.put(x, y - 1, z, WATER);
                b.put(x, y, z, AIR);
            }
        }
    }

    // ------------------------------------------------------------------ streets

    private static void streets(StructureBuilder b, int cx, int cz, int ground,
                                CitySurface surface, Palette p) {
        // The main avenue (Tverskaya) from the arrival gate to Red Square.
        paveSurface(b, surface, cx - 8, cz - 100, cx + 7, cz + 320, p.ground(), p);
        // Red Square and the cathedral forecourt.
        paveSurface(b, surface, cx - 70, cz - 165, cx + 140, cz - 100, p.ground(), p);
        paveSurface(b, surface, cx - 8, cz - 175, cx + 7, cz - 165, p.ground(), p);
        // The east-west street south of the Kremlin wall.
        paveSurface(b, surface, cx - 330, cz - 90, cx + 330, cz - 75, p.ground(), p);

        // Arbat: one broad pedestrian lane and narrow cross lanes.
        paveSurface(b, surface, cx - 244, cz - 240, cx - 237, cz + 164, p.ground(), p);
        for (int lz : new int[]{-220, -170, -110, -50, 10, 70, 130}) {
            paveSurface(b, surface, cx - 330, cz + lz - 2, cx - 150, cz + lz + 3,
                    p.ground(), p);
        }
        // Two connectors from Arbat to the main avenue.
        paveSurface(b, surface, cx - 150, cz - 22, cx - 8, cz - 17, p.ground(), p);
        paveSurface(b, surface, cx - 150, cz + 128, cx - 8, cz + 133, p.ground(), p);

        // Industrial grid: ten-wide streets, north and south of the river.
        for (int lx : new int[]{165, 230, 295}) {
            paveSurface(b, surface, cx + lx - 4, cz + 110, cx + lx + 5, cz + 157,
                    p.ground(), p);
            paveSurface(b, surface, cx + lx - 4, cz + 200, cx + lx + 5, cz + 305,
                    p.ground(), p);
        }
        for (int lz : new int[]{205, 270}) {
            paveSurface(b, surface, cx + 150, cz + lz - 4, cx + 330, cz + lz + 5,
                    p.ground(), p);
        }
        // The link from the avenue to the industrial north bank.
        paveSurface(b, surface, cx + 7, cz + 110, cx + 330, cz + 117, p.ground(), p);

        // River promenades and the metro plaza.
        paveSurface(b, surface, cx - 330, cz + 156, cx + 330, cz + 167, p.ground(), p);
        paveSurface(b, surface, cx - 330, cz + 189, cx + 330, cz + 200, p.ground(), p);
        paveSurface(b, surface, cx + 10, cz + 40, cx + 100, cz + 115, p.ground(), p);

        // The arrival apron under the gate.
        paveSurface(b, surface, cx - 24, cz + 300, cx + 24, cz + 338, p.ground(), p);
    }

    // ------------------------------------------------------------------ the Moskva

    private static void river(StructureBuilder b, int cx, int cz, int ground,
                              CitySurface surface, Palette p) {
        channelSurface(b, surface, cx - 330, cz + 168, cx + 330, cz + 188, p);
        // Quay walls: the promenade sits one course above the water.
        for (int x = cx - 330; x <= cx + 330; x++) {
            int north = surface.at(x, cz + 167);
            int south = surface.at(x, cz + 189);
            b.fill(x, north - 2, cz + 167, x, north - 1, cz + 167, p.foundation());
            b.fill(x, south - 2, cz + 189, x, south - 1, cz + 189, p.foundation());
            b.put(x, north, cz + 167, p.accent());
            b.put(x, south, cz + 189, p.accent());
        }
    }

    // ------------------------------------------------------------------ arrival

    private static void arrival(StructureBuilder b, int cx, int cz, int ground,
                                CitySurface surface, Palette p) {
        int base = surface.at(cx, cz + 325);
        int z0 = cz + 318;
        int z1 = cz + 332;
        // Two red-brick bastions flanking the passage.
        for (int s = -1; s <= 1; s += 2) {
            int x0 = cx + s * 24;
            int x1 = cx + s * 12;
            pad(b, Math.min(x0, x1), z0, Math.max(x0, x1), z1, base, p);
            b.fill(Math.min(x0, x1), base + 1, z0, Math.max(x0, x1), base + 16, z1, p.wall());
            b.fill(Math.min(x0, x1), base + 1, z0, Math.max(x0, x1), base + 1, z1,
                    p.weathered());
            b.fill(Math.min(x0, x1), base + 9, z0, Math.max(x0, x1), base + 9, z1, p.accent());
            b.crenellations(Math.min(x0, x1), z0, Math.max(x0, x1), z1, base + 17);
            b.put(cx + s * 18, base + 8, z0, p.light());
        }
        // Lintel and a tiled cap over the passage.
        b.fill(cx - 12, base + 10, z0, cx + 12, base + 12, z1, p.wall());
        b.fill(cx - 12, base + 10, z0, cx + 12, base + 10, z1, p.accent());
        b.fill(cx - 8, base + 1, z0, cx + 8, base + 9, z1, AIR);
        b.pitchedRoof(cx - 28, z0 - 2, cx + 28, z1 + 2, base + 17, 4, 0);
        b.put(cx - 8, base + 9, z0, p.light());
        b.put(cx + 8, base + 9, z0, p.light());

        // Two signposts just south of the gate.
        signPost(b, p, cx + 16, surface.at(cx + 16, cz + 310), cz + 310, Direction.SOUTH,
                "RED SQUARE", "KREMLIN", "ARBAT", "METRO");
        signPost(b, p, cx + 18, surface.at(cx + 18, cz + 306), cz + 306, Direction.SOUTH,
                "FACTORIES", "RIVER", "CITY NOTICES");

        // A pair of white-stone guards at the threshold.
        stoneGuard(b, cx - 20, cz + 312, surface.at(cx - 20, cz + 312), p);
        stoneGuard(b, cx + 20, cz + 312, surface.at(cx + 20, cz + 312), p);
    }

    private static void stoneGuard(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 1, ground, z - 1, x + 1, ground + 1, z + 1, p.foundation());
        b.fill(x, ground + 2, z, x, ground + 3, z, p.wall());
        b.put(x, ground + 4, z, p.accent());
        b.put(x - 1, ground + 3, z, p.wall());
        b.put(x + 1, ground + 3, z, p.wall());
    }

    // ------------------------------------------------------------------ Red Square

    private static void redSquare(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // The Metropol, west of the square.
        hotel(b, p, ground, cx - 120, cz - 150, 35, 45, 18);
        pave(b, cx - 85, cz - 124, cx - 70, cz - 116, ground, p.ground(), p);
        // GUM-style trading arcade along the east side.
        arcade(b, p, ground, cx + 108, cz - 165, cx + 134, cz - 105, 0);
        // Lenin's mausoleum, a low dark slab at the square's heart.
        mausoleum(b, p, ground, cx + 70, cz - 132);
        // The notice board with both local quests.
        questBoard(b, p, ground, cx, cz - 120);

        // A white-stone execution platform, deliberately empty.
        pad(b, cx - 52, cz - 142, cx - 28, cz - 122, ground, p);
        b.fill(cx - 48, ground + 1, cz - 138, cx - 32, ground + 2, cz - 126, p.foundation());
        b.crenellations(cx - 48, cz - 138, cx - 32, cz - 126, ground + 3);
        b.put(cx - 40, ground + 2, cz - 132, p.light());

        // Lamps down the square.
        for (int lz = -160; lz <= -110; lz += 16) {
            lamp(b, p, ground, cx - 64, cz + lz);
            lamp(b, p, ground, cx + 100, cz + lz);
        }
    }

    private static void hotel(StructureBuilder b, Palette p, int ground, int x, int z,
                              int w, int d, int h) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        pad(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x, ground + 1, z, x1, ground + h, z1, new Doorway(Side.E, 30));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        b.walls(x, ground + 2, z, x1, ground + h - 1, z1, p.wall());
        // White string courses and quoins.
        for (int y = ground + 5; y <= ground + h - 2; y += 5) {
            b.fill(x, y, z, x1, y, z, p.accent());
            b.fill(x, y, z1, x1, y, z1, p.accent());
        }
        for (int y = ground + 1; y <= ground + h; y++) {
            b.put(x, y, z, p.accent());
            b.put(x1, y, z, p.accent());
            b.put(x, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
        // Three storeys of tall windows on the square front.
        for (int wy = ground + 3; wy <= ground + h - 3; wy += 5) {
            for (int wz = z + 4; wz <= z1 - 4; wz += 5) {
                b.window(x1, wy, wz, 3, 1, true);
            }
            for (int wx = x + 4; wx <= x1 - 4; wx += 5) {
                b.window(wx, wy, z1, 3, 2, true);
            }
        }
        // Balconies over the square.
        for (int wy = ground + 8; wy <= ground + h - 2; wy += 5) {
            b.fill(x1 + 1, wy, z + 6, x1 + 2, wy, z1 - 6, p.rail());
        }
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, ground + h, 5, 0);
        doorFor(b, p, x, z, x1, z1, ground + 2, Side.E, 30);
        b.put(x1 - 2, ground + 3, z + 30, p.light());
    }

    private static void arcade(StructureBuilder b, Palette p, int ground, int x0, int z0,
                               int x1, int z1, int openSide) {
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x0, ground + 1, z0, x1, ground + 10, z1);
        b.fill(x0, ground + 1, z0, x1, ground + 1, z1, p.foundation());
        b.walls(x0, ground + 2, z0, x1, ground + 9, z1, p.wall());
        // Open arches along the square or the avenue.
        if (openSide == 0) {
            for (int z = z0 + 3; z <= z1 - 3; z += 8) {
                b.fill(x0, ground + 2, z, x0, ground + 6, z + 3, AIR);
                b.fill(x0, ground + 7, z, x0, ground + 7, z + 3, p.accent());
            }
        } else {
            for (int x = x0 + 3; x <= x1 - 3; x += 8) {
                b.fill(x, ground + 2, z1, x + 3, ground + 6, z1, AIR);
                b.fill(x, ground + 7, z1, x + 3, ground + 7, z1, p.accent());
            }
        }
        // Shop counters inside.
        for (int x = x0 + 4; x <= x1 - 4; x += 10) {
            b.fill(x, ground + 1, z0 + 2, x + 3, ground + 1, z0 + 4, p.accent());
            b.put(x + 1, ground + 2, z0 + 3, p.light());
        }
        b.pitchedRoof(x0 - 2, z0 - 2, x1 + 2, z1 + 2, ground + 10, 4, 0);
        b.put(x0 + 3, ground + 2, z1 - 1, p.light());
    }

    private static void mausoleum(StructureBuilder b, Palette p, int ground, int x, int z) {
        pad(b, x - 14, z - 10, x + 14, z + 10, ground, p);
        b.fill(x - 12, ground + 1, z - 8, x + 12, ground + 3, z + 8, BLACKSTONE);
        b.fill(x - 10, ground + 4, z - 6, x + 10, ground + 6, z + 6, DARK_STONE);
        b.fill(x - 8, ground + 7, z - 4, x + 8, ground + 8, z + 4, BLACKSTONE);
        for (int i = 0; i < 5; i++) {
            b.fill(x - 6 + i, ground + 1, z + 10 + i, x + 6 - i, ground + 1 + i, z + 10 + i,
                    p.foundation());
        }
        signPost(b, p, x, ground, z + 16, Direction.SOUTH,
                "MAUSOLEUM", "SILENT", "KEEP OUT");
    }

    private static void questBoard(StructureBuilder b, Palette p, int ground, int x, int z) {
        int x0 = x - 10;
        int x1 = x + 10;
        int z0 = z - 12;
        int z1 = z - 2;
        pave(b, x0 - 2, z0 - 2, x1 + 2, z1 + 2, ground, p.ground(), p);
        for (int px : new int[]{x0, x1}) {
            for (int pz : new int[]{z0, z1}) {
                b.fill(px, ground + 1, pz, px, ground + 5, pz, p.accent());
            }
        }
        b.fill(x0, ground + 6, z0, x1, ground + 6, z1, p.roofSlab());
        b.fill(x0, ground + 2, z1, x1, ground + 4, z1, p.frame());
        b.fill(x0 + 2, ground + 2, z1, x1 - 2, ground + 4, z1, p.wall());
        b.put(x - 3, ground + 1, z1 + 1, p.accent());
        CityPlanSupport.sign(b, x - 3, ground + 2, z1 + 1, Direction.SOUTH,
                "STOLEN ICON", "CHAPEL ARBAT", "REWARD");
        b.put(x + 3, ground + 1, z1 + 1, p.accent());
        CityPlanSupport.sign(b, x + 3, ground + 2, z1 + 1, Direction.SOUTH,
                "MISSING", "COMMISSAR", "FACTORY & METRO");
        b.put(x + 8, ground + 1, z0 + 2, p.accent());
        b.put(x + 8, ground + 2, z0 + 2, p.light());
    }

    // ------------------------------------------------------------------ St Basil's

    private static void stBasil(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x = cx;
        int z = cz - 200;
        int half = 25;
        pad(b, x - half, z - half, x + half, z + half, ground, p);
        b.fill(x - half, ground + 1, z - half, x + half, ground + 1, z + half, p.wall());
        b.walls(x - half, ground + 1, z - half, x + half, ground + 1, z + half, p.accent());

        // The central tent tower with its gold onion.
        tentTower(b, x, z, ground + 2, 7, 46, p);

        // Eight coloured onions on their own brick towers and white drums.
        BlockState[] shells = {
                Blocks.GLAZED_TERRACOTTA.pick(DyeColor.GREEN).defaultBlockState(),
                Blocks.GLAZED_TERRACOTTA.pick(DyeColor.RED).defaultBlockState(),
                Blocks.GLAZED_TERRACOTTA.pick(DyeColor.WHITE).defaultBlockState(),
                Blocks.GLAZED_TERRACOTTA.pick(DyeColor.BLUE).defaultBlockState(),
                Blocks.GLAZED_TERRACOTTA.pick(DyeColor.YELLOW).defaultBlockState(),
                Blocks.GLAZED_TERRACOTTA.pick(DyeColor.PURPLE).defaultBlockState(),
                Blocks.GLAZED_TERRACOTTA.pick(DyeColor.LIGHT_BLUE).defaultBlockState(),
                Blocks.GLAZED_TERRACOTTA.pick(DyeColor.ORANGE).defaultBlockState()
        };
        int[] towerH = {12, 15, 13, 16, 14, 17, 12, 15};
        for (int k = 0; k < 8; k++) {
            double a = Math.PI * 2 / 8 * k;
            int tx = x + (int) Math.round(Math.cos(a) * 15);
            int tz = z + (int) Math.round(Math.sin(a) * 15);
            for (int y = ground + 2; y < ground + 2 + towerH[k]; y++) {
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        int d2 = dx * dx + dz * dz;
                        if (d2 <= 9 && d2 > 4) {
                            b.put(tx + dx, y, tz + dz, p.wall());
                        }
                    }
                }
            }
            b.window(tx - 3, ground + 5, tz, 3, 1, true);
            b.window(tx + 3, ground + 5, tz, 3, 1, true);
            drum(b, tx, tz, ground + 2 + towerH[k], 2, 3, p);
            onion(b, tx, tz, ground + 2 + towerH[k] + 3, 2 + (k & 1), shells[k], p.accent());
        }

        // The low arcade around the south front.
        for (int ax = -20; ax <= 20; ax += 5) {
            b.fill(x + ax, ground + 2, z + 20, x + ax, ground + 5, z + 20, p.accent());
        }
        b.fill(x - 20, ground + 6, z + 20, x + 20, ground + 6, z + 20, p.roofSlab());
        for (int ax = -20; ax <= 20; ax += 10) {
            b.put(x + ax, ground + 7, z + 20, p.light());
        }

        // A south door into the central tower.
        b.fill(x - 2, ground + 2, z + 6, x + 2, ground + 5, z + 9, AIR);
        b.fill(x - 2, ground + 2, z + 9, x + 2, ground + 5, z + 9, p.accent());
        door(b, p, x, ground + 2, z + 9, Direction.SOUTH);
        b.put(x + 4, ground + 2, z + 9, p.light());
        signPost(b, p, x - 6, ground + 1, z + 10, Direction.SOUTH,
                "ST BASIL", "CATHEDRAL", "OPEN");
    }

    /** A steeply tapering red-brick pyramid with white ribs and a gold onion. */
    private static void tentTower(StructureBuilder b, int cx, int cz, int baseY, int baseR,
                                  int height, Palette p) {
        for (int i = 0; i <= height; i++) {
            int s = Math.round(baseR * (1.0f - (float) i / height));
            if (s < 1) {
                s = 1;
            }
            ring(b, cx, cz, baseY + i, s, p.wall(), p.accent());
        }
        int topY = baseY + height + 1;
        b.fill(cx - 2, topY, cz - 2, cx + 2, topY, cz + 2, p.accent());
        b.fill(cx - 1, topY + 1, cz - 1, cx + 1, topY + 1, cz + 1, p.accent());
        onion(b, cx, cz, topY + 2, 2, GOLD, p.accent());
    }

    /** A short brick drum with white ribs, carrying an onion dome. */
    private static void drum(StructureBuilder b, int cx, int cz, int baseY, int r, int height,
                             Palette p) {
        for (int y = baseY; y < baseY + height; y++) {
            ring(b, cx, cz, y, r, p.wall(), p.accent());
        }
    }

    /** An onion dome: rings that bulge out and taper to a gold finial. */
    private static void onion(StructureBuilder b, int cx, int cz, int baseY, int maxR,
                              BlockState shell, BlockState trim) {
        int h = maxR * 2 + 2;
        for (int i = 0; i <= h; i++) {
            double t = (double) i / h;
            int r = (int) Math.round(1 + (maxR - 1) * Math.sin(Math.PI * t));
            if (r < 1) {
                r = 1;
            }
            ring(b, cx, cz, baseY + i, r, shell, trim);
            if (t >= 0.5 && r <= 1) {
                b.put(cx, baseY + i + 1, cz, GOLD);
                b.put(cx, baseY + i + 2, cz, trim);
                b.put(cx, baseY + i + 3, cz, END_ROD);
                return;
            }
        }
    }

    /** One solid horizontal ring: {@code shell} fill with an accent perimeter. */
    private static void ring(StructureBuilder b, int cx, int cz, int y, int r, BlockState shell,
                             BlockState trim) {
        b.fill(cx - r, y, cz - r, cx + r, y, cz + r, shell);
        for (int i = -r; i <= r; i++) {
            b.put(cx + i, y, cz - r, trim);
            b.put(cx + i, y, cz + r, trim);
            b.put(cx - r, y, cz + i, trim);
            b.put(cx + r, y, cz + i, trim);
        }
    }

    // ------------------------------------------------------------------ the Kremlin

    private static void kremlin(StructureBuilder b, int cx, int cz, int ground,
                                CitySurface surface, Palette p) {
        int northEast = surface.at(cx + 300, cz - 290);
        int east = surface.at(cx + 314, cz - 192);
        int southEast = surface.at(cx + 300, cz - 95);
        int gardenY = surface.at(cx + 285, cz - 125);
        int chapelY = surface.at(cx + 298, cz - 247);

        // Curtain wall with a gate gap on the west. The east end sits on the graded rim.
        kremlinWall(b, p, ground, cx + 150, cz - 290, cx + 152, cz - 172);
        kremlinWall(b, p, ground, cx + 150, cz - 148, cx + 152, cz - 95);
        kremlinWall(b, p, ground, cx + 150, cz - 290, cx + 290, cz - 288);
        kremlinWall(b, p, northEast, cx + 290, cz - 290, cx + 315, cz - 288);
        kremlinWall(b, p, northEast, cx + 313, cz - 290, cx + 315, cz - 192);
        kremlinWall(b, p, southEast, cx + 313, cz - 192, cx + 315, cz - 95);
        kremlinWall(b, p, ground, cx + 150, cz - 95, cx + 290, cz - 93);
        kremlinWall(b, p, southEast, cx + 290, cz - 95, cx + 315, cz - 93);

        // Corner and interval towers: west and middle in the heart, east on the rim.
        kremlinTower(b, p, ground, cx + 150, cz - 290);
        kremlinTower(b, p, ground, cx + 232, cz - 290);
        kremlinTower(b, p, northEast, cx + 315, cz - 290);
        kremlinTower(b, p, east, cx + 315, cz - 192);
        kremlinTower(b, p, southEast, cx + 315, cz - 95);
        kremlinTower(b, p, ground, cx + 232, cz - 95);
        kremlinTower(b, p, ground, cx + 150, cz - 95);

        // The west gate and its guards.
        kremlinGate(b, p, ground, cx + 150, cz - 160);
        stoneGuard(b, cx + 136, cz - 160, ground, p);
        stoneGuard(b, cx + 166, cz - 160, ground, p);

        // Palace, armory, barracks, garden and chapel.
        palace(b, p, ground, cx + 190, cz - 260, 90, 55, 18);
        armory(b, p, ground, cx + 190, cz - 180, 60, 35, 12);
        apartment(b, p, ground, cx + 166, cz - 200, 20, 25, 8, 1);
        kremlinGarden(b, p, gardenY, cx + 265, cz - 145, cx + 305, cz - 105);
        chapel(b, p, chapelY, cx + 288, cz - 258, 22, 22, 10, 0, false);

        // Interior paths.
        pave(b, cx + 162, cz - 166, cx + 188, cz - 154, ground, p.ground(), p);
        pave(b, cx + 186, cz - 260, cx + 189, cz - 105, ground, p.ground(), p);
        pave(b, cx + 190, cz - 195, cx + 280, cz - 190, ground, p.ground(), p);
        pave(b, cx + 250, cz - 205, cx + 255, cz - 180, ground, p.ground(), p);

        // Lamps along the gate approach.
        lamp(b, p, ground, cx + 170, cz - 168);
        lamp(b, p, ground, cx + 170, cz - 152);
    }

    private static void kremlinWall(StructureBuilder b, Palette p, int ground,
                                    int x0, int z0, int x1, int z1) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                settle(b, x, z, ground, p);
            }
        }
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.fill(x0, ground + 1, z0, x1, ground + 8, z1, p.wall());
        b.fill(x0, ground + 1, z0, x1, ground + 1, z1, p.weathered());
        b.fill(x0, ground + 7, z0, x1, ground + 7, z1, p.accent());
        b.crenellations(x0, z0, x1, z1, ground + 9);
    }

    private static void kremlinTower(StructureBuilder b, Palette p, int ground, int x, int z) {
        pad(b, x - 5, z - 5, x + 5, z + 5, ground, p);
        b.fill(x - 5, ground + 1, z - 5, x + 5, ground + 14, z + 5, p.wall());
        b.fill(x - 5, ground + 1, z - 5, x + 5, ground + 1, z + 5, p.weathered());
        b.fill(x - 5, ground + 12, z - 5, x + 5, ground + 12, z + 5, p.accent());
        b.crenellations(x - 5, z - 5, x + 5, z + 5, ground + 15);
        b.window(x - 3, ground + 5, z - 5, 3, 1, true);
        b.window(x + 3, ground + 5, z - 5, 3, 1, true);
        b.window(x - 3, ground + 5, z + 5, 3, 1, true);
        b.window(x + 3, ground + 5, z + 5, 3, 1, true);
        b.pitchedRoof(x - 6, z - 6, x + 6, z + 6, ground + 16, 5, 0);
        b.fill(x - 2, ground + 22, z - 2, x + 2, ground + 22, z + 2, p.accent());
        onion(b, x, z, ground + 23, 2, GOLD, p.accent());
        b.put(x, ground + 2, z - 5, p.light());
    }

    private static void kremlinGate(StructureBuilder b, Palette p, int ground, int x, int z) {
        pad(b, x - 12, z - 12, x + 12, z + 12, ground, p);
        b.fill(x - 12, ground + 1, z - 12, x + 12, ground + 14, z + 12, p.wall());
        b.fill(x - 12, ground + 1, z - 12, x + 12, ground + 1, z + 12, p.weathered());
        // The east-west passage through the wall.
        b.fill(x - 12, ground + 1, z - 4, x + 12, ground + 7, z + 4, AIR);
        b.fill(x - 12, ground + 8, z - 4, x + 12, ground + 8, z + 4, p.accent());
        b.fill(x - 12, ground + 9, z - 12, x + 12, ground + 9, z + 12, p.accent());
        b.crenellations(x - 12, z - 12, x + 12, z + 12, ground + 15);
        b.pitchedRoof(x - 14, z - 14, x + 14, z + 14, ground + 16, 5, 0);
        onion(b, x, z, ground + 23, 2, GOLD, p.accent());
        b.put(x - 4, ground + 8, z, p.light());
        b.put(x + 4, ground + 8, z, p.light());
        // The gate is open: no door block, just a threshold.
        b.fill(x - 12, ground, z - 4, x + 12, ground, z + 4, p.foundation());
    }

    private static void palace(StructureBuilder b, Palette p, int ground, int x, int z,
                               int w, int d, int h) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        pad(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x, ground + 1, z, x1, ground + h, z1, new Doorway(Side.S, w / 2));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        b.walls(x, ground + 2, z, x1, ground + h - 1, z1, PALACE);
        // White pilasters and a heavy cornice.
        for (int px = x; px <= x1; px += 6) {
            b.fill(px, ground + 2, z1, px, ground + h - 1, z1, WHITE);
            b.fill(px, ground + 2, z, px, ground + h - 1, z, WHITE);
        }
        b.fill(x, ground + h - 2, z, x1, ground + h - 2, z1, WHITE);
        for (int wy = ground + 4; wy <= ground + h - 5; wy += 4) {
            for (int wx = x + 4; wx <= x1 - 4; wx += 6) {
                b.window(wx, wy, z1, 3, 2, true);
                b.window(wx, wy, z, 3, 2, true);
            }
        }
        b.pitchedRoof(x - 2, z - 2, x1 + 2, z1 + 2, ground + h, 6, 0);
        b.fill(x + w / 2 - 8, ground + h + 6, z1 - 1, x + w / 2 + 8, ground + h + 8, z1 + 1,
                WHITE);
        b.put(x + w / 2, ground + h + 9, z1, GOLD);
        b.put(x + w / 2, ground + h + 10, z1, END_ROD);
        doorFor(b, p, x, z, x1, z1, ground + 2, Side.S, w / 2);
        b.put(x + w / 2 - 2, ground + 3, z1 + 1, p.light());
    }

    private static void armory(StructureBuilder b, Palette p, int ground, int x, int z,
                               int w, int d, int h) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        pad(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x, ground + 1, z, x1, ground + h, z1, new Doorway(Side.S, w / 2));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        b.walls(x, ground + 2, z, x1, ground + h - 1, z1, p.wall());
        b.fill(x, ground + h - 2, z, x1, ground + h - 2, z1, p.accent());
        for (int wy = ground + 3; wy <= ground + h - 3; wy += 3) {
            for (int wx = x + 3; wx <= x1 - 3; wx += 4) {
                b.window(wx, wy, z1, 2, 1, true);
                b.window(wx, wy, z, 2, 1, true);
            }
        }
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, ground + h, 4, 0);
        // Regimental stores and the commissar's desk.
        for (int sx = x + 4; sx <= x1 - 4; sx += 8) {
            b.fill(sx, ground + 2, z + 2, sx + 2, ground + 3, z + 3, BOOKSHELF);
        }
        b.fill(x + w / 2 - 3, ground + 2, z + d / 2, x + w / 2 + 3, ground + 2, z + d / 2 + 1,
                p.accent());
        b.put(x + w / 2, ground + 3, z + d / 2, p.light());
        doorFor(b, p, x, z, x1, z1, ground + 2, Side.S, w / 2);
        b.put(x + w / 2 + 2, ground + 3, z1 + 1, p.light());
    }

    private static void kremlinGarden(StructureBuilder b, Palette p, int ground, int x0, int z0,
                                      int x1, int z1) {
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p);
        b.fill(x0, ground + 1, z0, x1, ground + 1, z1, p.foundation());
        // Paths crossing the garden.
        b.fill(x0, ground + 1, (z0 + z1) / 2 - 1, x1, ground + 1, (z0 + z1) / 2 + 1,
                p.ground());
        b.fill((x0 + x1) / 2 - 1, ground + 1, z0, (x0 + x1) / 2 + 1, ground + 1, z1,
                p.ground());
        fountain(b, p, ground + 1, (x0 + x1) / 2, (z0 + z1) / 2);
        for (int[] t : KREMLIN_TREES) {
            tree(b, p, ground + 1, t[0], t[1]);
        }
        bench(b, p, ground + 1, x0 + 4, z1 - 4);
        bench(b, p, ground + 1, x1 - 4, z0 + 4);
        b.put(x0 + 2, ground + 2, z0 + 2, p.light());
        b.put(x1 - 2, ground + 2, z1 - 2, p.light());
    }

    private static void chapel(StructureBuilder b, Palette p, int ground, int x, int z,
                               int w, int d, int h, int doorSide, boolean icon) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        pad(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p);
        int offset = (doorSide == 0 || doorSide == 3 ? w : d) / 2;
        b.room(x, ground + 1, z, x1, ground + h, z1, new Doorway(side(doorSide), offset));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        b.walls(x, ground + 2, z, x1, ground + h - 1, z1, p.wall());
        b.fill(x, ground + h - 1, z, x1, ground + h - 1, z1, p.accent());
        b.window(x1, ground + 5, frontageMid(z, d), 3, 1, true);
        // A small drum and onion over the nave.
        int mx = (x + x1) / 2;
        int mz = (z + z1) / 2;
        drum(b, mx, mz, ground + h, 2, 2, p);
        onion(b, mx, mz, ground + h + 2, 2, GOLD, p.accent());
        // Altar and candles.
        b.fill(mx - 2, ground + 2, z + 2, mx + 2, ground + 3, z + 3, p.accent());
        b.put(mx, ground + 4, z + 2, p.light());
        if (icon) {
            b.fill(mx - 1, ground + 3, z + 1, mx + 1, ground + 5, z + 1, GOLD);
            b.put(mx, ground + 4, z + 1, p.frame());
            b.put(x + 2, ground + 2, z1 - 2, p.accent());
            CityPlanSupport.sign(b, x + 2, ground + 3, z1 - 2, Direction.NORTH,
                    "THE ICON", "IS GONE", "PRAY FOR IT");
        }
        doorFor(b, p, x, z, x1, z1, ground + 2, side(doorSide), offset);
        b.put(mx + 3, ground + 2, z1, p.light());
    }

    /** Midpoint of a chapel frontage, kept as a tiny named helper for readability. */
    private static int frontageMid(int z, int d) {
        return z + d / 2;
    }

    // ------------------------------------------------------------------ Arbat

    private static void arbat(StructureBuilder b, int cx, int cz, int ground,
                              CitySurface surface, Palette p) {
        for (int[] a : ARBAT_WEST) {
            int base = surface.at(cx + a[0] + a[2] / 2, cz + a[1] + a[3] / 2);
            apartment(b, p, base, cx + a[0], cz + a[1], a[2], a[3], a[4], a[5]);
        }
        for (int[] a : ARBAT_EAST) {
            int base = surface.at(cx + a[0] + a[2] / 2, cz + a[1] + a[3] / 2);
            apartment(b, p, base, cx + a[0], cz + a[1], a[2], a[3], a[4], a[5]);
        }
        for (int[] c : ARBAT_CAFES) {
            int base = surface.at(cx + c[0] + c[2] / 2, cz + c[1] + c[3] / 2);
            cafe(b, p, base, cx + c[0], cz + c[1], c[2], c[3], c[4], c[5]);
        }
        int shopBase = surface.at(cx - 223, cz - 66);
        shop(b, p, shopBase, cx - 232, cz - 74, 18, 16, 8, 1,
                "ARBAT SHOP", "BRASS & SILK");
        int herbBase = surface.at(cx - 223, cz + 113);
        shop(b, p, herbBase, cx - 232, cz + 104, 18, 18, 8, 1,
                "HERBALIST", "REMEDIES");
        int chapelBase = surface.at(cx - 272, cz - 127);
        chapel(b, p, chapelBase, cx - 284, cz - 138, 24, 22, 10, 2, true);
        int ruinBase = surface.at(cx - 310, cz - 138);
        ruin(b, p, ruinBase, cx - 318, cz - 142, 18, 9);
        for (int[] c : ARBAT_COURTS) {
            int base = surface.at(cx + (c[0] + c[2]) / 2, cz + (c[1] + c[3]) / 2);
            courtyard(b, p, base, cx + c[0], cz + c[1], cx + c[2], cz + c[3]);
        }
        // Lamps down the lane.
        for (int lz = -230; lz <= 150; lz += 24) {
            lamp(b, p, surface.at(cx - 246, cz + lz), cx - 246, cz + lz);
            lamp(b, p, surface.at(cx - 235, cz + lz), cx - 235, cz + lz);
        }
    }

    private static void apartment(StructureBuilder b, Palette p, int ground, int x, int z,
                                  int w, int d, int h, int doorSide) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        pad(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p);
        int offset = (doorSide == 0 || doorSide == 3 ? w : d) / 2;
        b.room(x, ground + 1, z, x1, ground + h, z1, new Doorway(side(doorSide), offset));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        b.walls(x, ground + 2, z, x1, ground + h - 1, z1, p.wall());
        // Quoins and a white band under the eaves.
        for (int y = ground + 1; y <= ground + h; y++) {
            b.put(x, y, z, p.accent());
            b.put(x1, y, z, p.accent());
            b.put(x, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
        b.fill(x, ground + h - 1, z, x1, ground + h - 1, z1, p.accent());
        // Three or four storeys of windows on every face.
        for (int wy = ground + 3; wy <= ground + h - 2; wy += 3) {
            for (int wx = x + 3; wx <= x1 - 3; wx += 4) {
                b.window(wx, wy, z, 2, 1, true);
                b.window(wx, wy, z1, 2, 1, true);
            }
            for (int wz = z + 3; wz <= z1 - 3; wz += 4) {
                b.window(x, wy, wz, 2, 1, true);
                b.window(x1, wy, wz, 2, 1, true);
            }
        }
        // A balcony over the lane.
        b.fill(x1 + 1, ground + 5, z + 4, x1 + 2, ground + 5, z1 - 4, p.rail());
        b.fill(x - 2, ground + 8, z + 4, x - 1, ground + 8, z1 - 4, p.rail());
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, ground + h, 3 + h % 3, w >= d ? 0 : 1);
        b.put(x + 2, ground + h + 2, z + 2, p.accent());
        b.put(x + 2, ground + h + 3, z + 2, p.accent());
        doorFor(b, p, x, z, x1, z1, ground + 2, side(doorSide), offset);
        b.put(x + 4, ground + 2, z1 + 1, p.light());
    }

    private static void cafe(StructureBuilder b, Palette p, int ground, int x, int z,
                             int w, int d, int h, int doorSide) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        pad(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p);
        int offset = (doorSide == 0 || doorSide == 3 ? w : d) / 2;
        b.room(x, ground + 1, z, x1, ground + h, z1, new Doorway(side(doorSide), offset));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        b.walls(x, ground + 2, z, x1, ground + h - 1, z1, p.wall());
        b.fill(x, ground + h - 1, z, x1, ground + h - 1, z1, p.accent());
        b.window(x1, ground + 2, z + 3, 2, 2, true);
        b.window(x, ground + 2, z + 3, 2, 2, true);
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, ground + h, 3, 0);
        // A striped awning and a pair of outdoor tables.
        if (doorSide == 1) {
            b.fill(x - 3, ground + 4, z + 2, x - 1, ground + 4, z1 - 2, p.roofSlab());
            b.put(x - 2, ground + 2, z + 2, BARREL);
            b.put(x - 2, ground + 2, z1 - 2, BARREL);
        } else {
            b.fill(x + 2, ground + 4, z1 + 1, x1 - 2, ground + 4, z1 + 3, p.roofSlab());
            b.put(x + 2, ground + 2, z1 + 2, BARREL);
            b.put(x1 - 2, ground + 2, z1 + 2, BARREL);
        }
        b.put(x + 2, ground + 2, z + 2, p.light());
        doorFor(b, p, x, z, x1, z1, ground + 2, side(doorSide), offset);
        b.fill(x + 2, ground + 1, z1 + 1, x + 2, ground + 3, z1 + 1, p.accent());
        CityPlanSupport.sign(b, x + 2, ground + 4, z1 + 1, Direction.SOUTH,
                "CAFE", "SAMOVAR", "TEA & PIE");
    }

    private static void shop(StructureBuilder b, Palette p, int ground, int x, int z,
                             int w, int d, int h, int doorSide, String l1, String l2) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        pad(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p);
        int offset = (doorSide == 0 || doorSide == 3 ? w : d) / 2;
        b.room(x, ground + 1, z, x1, ground + h, z1, new Doorway(side(doorSide), offset));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        b.walls(x, ground + 2, z, x1, ground + h - 1, z1, p.wall());
        b.fill(x, ground + h - 1, z, x1, ground + h - 1, z1, p.accent());
        for (int wy = ground + 3; wy <= ground + h - 2; wy += 3) {
            b.window(x1, wy, z + 3, 2, 2, true);
            b.window(x, wy, z + 3, 2, 2, true);
        }
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, ground + h, 3, 0);
        // Counter, crates and a hanging sign.
        b.fill(x + 2, ground + 2, z + 2, x + 5, ground + 2, z + 4, p.accent());
        b.put(x + 3, ground + 3, z + 2, BARREL);
        b.put(x + 6, ground + 2, z + 2, p.foundation());
        b.put(x + 7, ground + 2, z + 2, p.foundation());
        b.put(x + 2, ground + 2, z1 - 2, p.light());
        doorFor(b, p, x, z, x1, z1, ground + 2, side(doorSide), offset);
        if (doorSide == 1) {
            b.fill(x - 2, ground + 1, z + offset, x - 2, ground + 3, z + offset, p.accent());
            CityPlanSupport.sign(b, x - 2, ground + 4, z + offset, Direction.WEST, l1, l2);
        } else {
            b.fill(x1 + 2, ground + 1, z + offset, x1 + 2, ground + 3, z + offset,
                    p.accent());
            CityPlanSupport.sign(b, x1 + 2, ground + 4, z + offset, Direction.EAST, l1, l2);
        }
    }

    private static void courtyard(StructureBuilder b, Palette p, int ground, int x0, int z0,
                                  int x1, int z1) {
        pave(b, x0, z0, x1, z1, ground, p.ground(), p);
        // A low kerb, deliberately broken at the lane side.
        b.fill(x0, ground + 1, z0, x1, ground + 1, z0, p.foundation());
        b.fill(x0, ground + 1, z1, x1, ground + 1, z1, p.foundation());
        b.fill(x0, ground + 1, z0, x0, ground + 1, z1, p.foundation());
        b.fill(x1, ground + 1, z0, x1, ground + 1, z1, p.foundation());
        int mx = (x0 + x1) / 2;
        int mz = (z0 + z1) / 2;
        // A well and a tree.
        b.fill(mx - 1, ground + 1, mz - 1, mx + 1, ground + 1, mz + 1, p.accent());
        b.put(mx, ground + 1, mz, WATER);
        b.put(mx + 2, ground + 2, mz - 2, p.light());
        tree(b, p, ground + 1, x0 + 4, z0 + 4);
        bench(b, p, ground + 1, x1 - 4, z0 + 3);
        bench(b, p, ground + 1, x0 + 3, z1 - 3);
        b.put(x1 - 2, ground + 2, z1 - 2, BARREL);
    }

    /** A deliberately broken brick shell: missing wall courses, rubble, cobwebs and a chimney. */
    private static void ruin(StructureBuilder b, Palette p, int ground, int x, int z,
                             int w, int d) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        pad(b, x, z, x1, z1, ground, p);
        b.walls(x, ground + 1, z, x1, ground + 4, z1, p.weathered());
        // Authored collapses, not a random decay pass.
        b.fill(x + 2, ground + 1, z, x + 5, ground + 3, z, AIR);
        b.fill(x1 - 2, ground + 1, z1, x1, ground + 2, z1, AIR);
        b.fill(x1, ground + 1, z + 3, x1, ground + 4, z + 5, AIR);
        b.fill(x + 3, ground + 1, z + 2, x1 - 4, ground + 1, z + 2, p.rubble());
        b.fill(x + 2, ground + 1, z1 - 2, x1 - 3, ground + 2, z1 - 2, p.rubble());
        b.put(x + 1, ground + 1, z + 1, Palette.cobweb());
        b.put(x1 - 1, ground + 1, z1 - 1, Palette.cobweb());
        b.fill(x1 - 3, ground + 1, z + 2, x1 - 3, ground + 6, z + 2, p.accent());
        b.put(x + 2, ground + 1, z + 2, p.light());
    }

    // ------------------------------------------------------------------ metro

    private static void metro(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        station(b, p, ground, cx + 25, cz + 55, 51, 46, 14);
        metroHall(b, p, ground, cx + 15, cz + 45, cx + 85, cz + 95);
        hiddenRoom(b, p, ground, cx - 18, cz + 22, cx + 14, cz + 58);
        // The escalator is carved last, so it opens the station floor and the hall wall.
        escalator(b, p, ground, cx + 50, cz + 98);
        // The tunnel from the hall to the hidden room.
        b.fill(cx + 14, ground - 10, cz + 38, cx + 30, ground - 10, cz + 44, p.foundation());
        b.fill(cx + 14, ground - 5, cz + 38, cx + 30, ground - 5, cz + 44, p.foundation());
        b.fill(cx + 14, ground - 9, cz + 38, cx + 30, ground - 6, cz + 44, AIR);
        b.fill(cx + 18, ground - 9, cz + 45, cx + 26, ground - 6, cz + 45, AIR);
        // A lamp at each end of the tunnel.
        b.put(cx + 16, ground - 6, cz + 41, p.light());
        b.put(cx + 28, ground - 6, cz + 41, p.light());
        // Plaza furniture.
        for (int lz = 48; lz <= 108; lz += 20) {
            lamp(b, p, ground, cx + 14, cz + lz);
            lamp(b, p, ground, cx + 96, cz + lz);
        }
        bench(b, p, ground, cx + 20, cz + 46);
        bench(b, p, ground, cx + 90, cz + 46);
    }

    private static void station(StructureBuilder b, Palette p, int ground, int x, int z,
                                int w, int d, int h) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        pad(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x, ground + 1, z, x1, ground + h, z1, new Doorway(Side.S, w / 2));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        b.walls(x, ground + 2, z, x1, ground + h - 1, z1, p.wall());
        b.fill(x, ground + 5, z1, x1, ground + 5, z1, p.accent());
        b.fill(x, ground + 9, z1, x1, ground + 9, z1, p.accent());
        for (int wx = x + 4; wx <= x1 - 4; wx += 6) {
            b.window(wx, ground + 3, z1, 3, 2, true);
            b.window(wx, ground + 7, z1, 3, 2, true);
        }
        b.pitchedRoof(x - 2, z - 2, x1 + 2, z1 + 2, ground + h, 4, 0);
        // The portico over the entrance.
        b.fill(x + 13, ground + 1, z1 + 1, x + 37, ground + 1, z1 + 9, p.foundation());
        for (int px = x + 13; px <= x + 37; px += 6) {
            b.fill(px, ground + 2, z1 + 8, px, ground + 7, z1 + 8, p.accent());
        }
        b.fill(x + 11, ground + 8, z1 + 6, x + 39, ground + 8, z1 + 10, p.roofSlab());
        // The name board and clock.
        b.put(x + w / 2 + 6, ground + 1, z1 + 1, p.accent());
        CityPlanSupport.sign(b, x + w / 2 + 6, ground + 2, z1 + 1, Direction.SOUTH,
                "METRO", "STATION", "MIND THE GAP");
        b.fill(x + w / 2 - 2, ground + 11, z1, x + w / 2 + 2, ground + 11, z1, p.accent());
        b.put(x + w / 2, ground + 11, z1, GOLD);
        doorFor(b, p, x, z, x1, z1, ground + 2, Side.S, w / 2);
        b.put(x + w / 2 - 3, ground + 3, z1 + 1, p.light());
        b.put(x + w / 2 + 3, ground + 3, z1 + 1, p.light());
    }

    private static void escalator(StructureBuilder b, Palette p, int ground, int x, int z) {
        b.fill(x - 4, ground - 10, z - 12, x + 4, ground + 2, z, AIR);
        for (int i = 0; i <= 10; i++) {
            int zz = z - i;
            int top = ground + 1 - i;
            b.fill(x - 3, top - 1, zz, x + 3, top - 1, zz, p.accent());
            b.fill(x - 4, top - 1, zz, x - 4, top + 1, zz, p.foundation());
            b.fill(x + 4, top - 1, zz, x + 4, top + 1, zz, p.foundation());
            b.put(x - 3, top, zz, p.rail());
            b.put(x + 3, top, zz, p.rail());
        }
        b.put(x, ground - 6, z - 11, p.light());
        b.put(x, ground - 9, z - 10, p.light());
    }

    private static void metroHall(StructureBuilder b, Palette p, int ground, int x0, int z0,
                                  int x1, int z1) {
        int y0 = ground - 10;
        int y1 = ground - 5;
        b.fill(x0, y0, z0, x1, y1, z1, AIR);
        b.fill(x0, y0, z0, x1, y0, z1, MARBLE);
        b.fill(x0, y1, z0, x1, y1, z1, p.foundation());
        b.walls(x0, y0 + 1, z0, x1, y1 - 1, z1, MARBLE);
        // Marble piers and warm lamps.
        for (int px = x0 + 15; px < x1; px += 20) {
            for (int pz = z0 + 12; pz < z1; pz += 20) {
                b.fill(px, y0 + 1, pz, px, y1 - 1, pz, p.accent());
                b.put(px, y1 - 2, pz, p.light());
            }
        }
        // A gold frieze and benches.
        b.fill(x0, y0 + 1, z0, x1, y0 + 1, z0, GOLD);
        b.fill(x0, y0 + 1, z1, x1, y0 + 1, z1, GOLD);
        bench(b, p, y0 + 1, x0 + 4, z0 + 4);
        bench(b, p, y0 + 1, x1 - 4, z0 + 4);
        bench(b, p, y0 + 1, x0 + 4, z1 - 4);
        // The hall name board.
        b.put(x0 + 3, y0 + 1, z0 + 1, p.accent());
        CityPlanSupport.sign(b, x0 + 3, y0 + 2, z0 + 1, Direction.SOUTH,
                "METRO HALL", "MIND THE GAP");
        b.chest(x1 - 4, y0 + 1, z1 - 4, SiteLoot.COMMON);
    }

    private static void hiddenRoom(StructureBuilder b, Palette p, int ground, int x0, int z0,
                                   int x1, int z1) {
        int y0 = ground - 10;
        int y1 = ground - 5;
        b.fill(x0, y0, z0, x1, y1, z1, AIR);
        b.fill(x0, y0, z0, x1, y0, z1, p.foundation());
        b.fill(x0, y1, z0, x1, y1, z1, p.foundation());
        b.walls(x0, y0 + 1, z0, x1, y1 - 1, z1, p.wall());
        // The altar and its gold finial.
        b.fill(x0 + 12, y0 + 1, z0 + 14, x0 + 20, y0 + 2, z0 + 18, BLACKSTONE);
        b.put(x0 + 16, y0 + 3, z0 + 16, GOLD);
        b.put(x0 + 16, y0 + 4, z0 + 16, END_ROD);
        // The Choir's strongbox and the commissar's papers.
        b.chest(x0 + 2, y0 + 1, z0 + 2, SiteLoot.RELIC);
        b.chest(x1 - 2, y0 + 1, z1 - 2, SiteLoot.CULT);
        b.chest(x1 - 2, y0 + 1, z0 + 2, SiteLoot.ORDER);
        // Lore signs on the north wall.
        b.put(x0 + 10, y0 + 1, z0, p.accent());
        CityPlanSupport.sign(b, x0 + 10, y0 + 2, z0, Direction.SOUTH,
                "THE ICON IS", "NOT LOST", "THE CHOIR", "WATCHES");
        b.put(x0 + 14, y0 + 1, z0, p.accent());
        CityPlanSupport.sign(b, x0 + 14, y0 + 2, z0, Direction.SOUTH,
                "PAPERS HERE", "TAKE THEM", "OR BURN THEM");
        b.put(x0 + 18, y0 + 1, z0, p.accent());
        CityPlanSupport.sign(b, x0 + 18, y0 + 2, z0, Direction.SOUTH,
                "SEALED VAULT", "OF THE CHOIR");
        // A second pair of signs by the altar.
        b.put(x0 + 12, y0 + 1, z0 + 20, p.accent());
        CityPlanSupport.sign(b, x0 + 12, y0 + 2, z0 + 20, Direction.NORTH,
                "SING LOW", "THE WALLS", "HAVE EARS");
        // Cobwebs in the corners.
        b.fill(x0 + 1, y1 - 1, z0 + 1, x0 + 3, y1 - 1, z0 + 3, Palette.cobweb());
        b.fill(x1 - 3, y1 - 1, z1 - 3, x1 - 1, y1 - 1, z1 - 1, Palette.cobweb());
        b.put(x0 + 4, y1 - 1, z0 + 4, Palette.cobweb());
        b.put(x1 - 4, y1 - 1, z0 + 4, Palette.cobweb());
    }

    // ------------------------------------------------------------------ industry

    private static void industrial(StructureBuilder b, int cx, int cz, int ground,
                                   CitySurface surface, Palette p) {
        int housing1 = surface.at(cx + 188, cz + 231);
        int housing2 = surface.at(cx + 215, cz + 231);
        int housing3 = surface.at(cx + 202, cz + 257);
        int factoryBase = surface.at(cx + 267, cz + 242);
        int eastBase = surface.at(cx + 317, cz + 242);
        int northBase = surface.at(cx + 199, cz + 145);
        int northEastBase = surface.at(cx + 317, cz + 145);
        int ruinBase = surface.at(cx + 155, cz + 145);
        int railSouth = surface.at(cx + 240, cz + 293);
        int railNorth = surface.at(cx + 240, cz + 123);
        int platformSouth = surface.at(cx + 198, cz + 285);
        int platformNorth = surface.at(cx + 198, cz + 131);

        // Workers' housing west of the main factory.
        tenement(b, p, housing1, cx + 178, cz + 220, 22, 23, 10);
        tenement(b, p, housing2, cx + 205, cz + 220, 22, 23, 10);
        tenement(b, p, housing3, cx + 178, cz + 248, 49, 19, 10);
        // The main factory and its chimneys.
        factory(b, p, factoryBase, cx + 240, cz + 218, 54, 49, 14);
        // East warehouse and the north-bank stores.
        warehouse(b, p, eastBase, cx + 306, cz + 218, 22, 49, 10);
        warehouse(b, p, northBase, cx + 178, cz + 136, 42, 19, 9);
        warehouse(b, p, northEastBase, cx + 306, cz + 136, 22, 19, 8);
        ruin(b, p, ruinBase, cx + 150, cz + 136, 10, 19);
        // The north-bank coal yard.
        int coalBase = surface.at(cx + 248, cz + 141);
        pad(b, cx + 242, cz + 132, cx + 254, cz + 150, coalBase, p);
        for (int i = 0; i < 5; i++) {
            b.fill(cx + 242 + i * 3, coalBase + 1, cz + 132, cx + 254 - i * 3,
                    coalBase + 1 + i, cz + 150, COAL);
        }
        // Rail: two yards and a siding into the factory. Both yards are straight and level.
        railYard(b, p, railSouth, cx + 150, cz + 290, cx + 330, cz + 296);
        railYard(b, p, railNorth, cx + 150, cz + 120, cx + 330, cz + 126);
        platform(b, p, platformSouth, cx + 178, cz + 282, cx + 219, cz + 288);
        platform(b, p, platformNorth, cx + 178, cz + 128, cx + 219, cz + 134);
        for (int z = 267; z <= 293; z++) {
            settle(b, cx + 250, cz + z, railSouth, p);
            b.put(cx + 250, railSouth, cz + z, GRAVEL);
            b.put(cx + 250, railSouth + 1, cz + z, RAIL_NS);
        }
        // A few loose crates and barrels along the sidings.
        for (int i = 0; i < 8; i++) {
            b.put(cx + 178 + i * 5, platformSouth + 1, cz + 286, p.foundation());
        }
        for (int i = 0; i < 5; i++) {
            b.put(cx + 260 + i * 6, surface.at(cx + 260 + i * 6, cz + 130) + 1,
                    cz + 130, BARREL);
        }
    }

    private static void factory(StructureBuilder b, Palette p, int ground, int x, int z,
                                int w, int d, int h) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        pad(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x, ground + 1, z, x1, ground + h, z1,
                new Doorway(Side.S, w / 2), new Doorway(Side.W, d / 2));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        b.walls(x, ground + 2, z, x1, ground + h - 1, z1, p.wall());
        b.fill(x, ground + h - 1, z, x1, ground + h - 1, z1, p.accent());
        // Tall industrial windows.
        for (int wx = x + 4; wx <= x1 - 4; wx += 8) {
            b.window(wx, ground + 3, z1, 6, 2, true);
            b.window(wx, ground + 3, z, 6, 2, true);
        }
        // Sawtooth roof.
        for (int rx = x; rx < x1; rx += 12) {
            b.pitchedRoof(rx, z - 1, Math.min(rx + 11, x1), z1 + 1, ground + h, 4, 1);
        }
        // Machines and a boiler.
        for (int mx = x + 8; mx <= x1 - 8; mx += 12) {
            b.fill(mx, ground + 2, z + 8, mx + 3, ground + 4, z + 14, IRON);
            b.put(mx + 1, ground + 5, z + 10, COAL);
        }
        chimney(b, p, ground, x + 6, z + 6, 34);
        chimney(b, p, ground, x1 - 6, z + 6, 34);
        doorFor(b, p, x, z, x1, z1, ground + 2, Side.S, w / 2);
        doorFor(b, p, x, z, x1, z1, ground + 2, Side.W, d / 2);
        b.put(x + w / 2, ground + 3, z1 + 1, p.light());
        b.put(x + w / 2 - 3, ground + 3, z1 + 1, p.light());
        b.fill(x + 2, ground + 1, z1 + 1, x + 2, ground + 3, z1 + 1, p.accent());
        CityPlanSupport.sign(b, x + 2, ground + 4, z1 + 1, Direction.SOUTH,
                "FACTORY No.3", "NO ENTRY", "DANGER", "SMOKE");
    }

    private static void chimney(StructureBuilder b, Palette p, int ground, int x, int z,
                                int height) {
        for (int i = 0; i < height; i++) {
            int r = i < height - 6 ? 2 : 1;
            b.fill(x - r, ground + 1 + i, z - r, x + r, ground + 1 + i, z + r, p.wall());
            if (i % 4 == 0) {
                b.walls(x - r, ground + 1 + i, z - r, x + r, ground + 1 + i, z + r,
                        p.weathered());
            }
        }
        b.fill(x - 1, ground + height, z - 1, x + 1, ground + height, z + 1, COAL);
        b.put(x, ground + height + 1, z, COAL);
    }

    private static void tenement(StructureBuilder b, Palette p, int ground, int x, int z,
                                 int w, int d, int h) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        pad(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x, ground + 1, z, x1, ground + h, z1,
                new Doorway(Side.S, w / 3), new Doorway(Side.S, 2 * w / 3));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        b.walls(x, ground + 2, z, x1, ground + h - 1, z1, p.wall());
        b.fill(x, ground + h - 1, z, x1, ground + h - 1, z1, p.accent());
        for (int wy = ground + 3; wy <= ground + h - 2; wy += 3) {
            for (int wx = x + 3; wx <= x1 - 3; wx += 4) {
                b.window(wx, wy, z, 2, 1, true);
                b.window(wx, wy, z1, 2, 1, true);
            }
        }
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, ground + h, 3, w >= d ? 0 : 1);
        b.put(x + 3, ground + h + 2, z + 3, p.accent());
        b.put(x + 3, ground + h + 3, z + 3, p.accent());
        doorFor(b, p, x, z, x1, z1, ground + 2, Side.S, w / 3);
        doorFor(b, p, x, z, x1, z1, ground + 2, Side.S, 2 * w / 3);
        b.put(x + w / 2, ground + 2, z1 + 1, p.light());
        // A laundry line across the front.
        b.fill(x + 4, ground + 5, z1 + 2, x1 - 4, ground + 5, z1 + 2, p.rail());
    }

    private static void warehouse(StructureBuilder b, Palette p, int ground, int x, int z,
                                  int w, int d, int h) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        pad(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x, ground + 1, z, x1, ground + h, z1, new Doorway(Side.S, w / 2));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        b.walls(x, ground + 2, z, x1, ground + h - 1, z1, p.wall());
        b.fill(x, ground + h - 1, z, x1, ground + h - 1, z1, p.accent());
        // Big loading doors.
        b.fill(x + 3, ground + 2, z1, x + 9, ground + 5, z1, AIR);
        b.fill(x + 3, ground + 6, z1, x + 9, ground + 6, z1, p.accent());
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, ground + h, 3, w >= d ? 0 : 1);
        b.put(x + 2, ground + 2, z + 2, BARREL);
        b.put(x + 4, ground + 2, z + 2, BARREL);
        b.put(x1 - 2, ground + 2, z1 - 2, p.foundation());
        doorFor(b, p, x, z, x1, z1, ground + 2, Side.S, w / 2);
        b.put(x + w / 2, ground + 3, z1 + 1, p.light());
    }

    private static void railYard(StructureBuilder b, Palette p, int ground, int x0, int z0,
                                 int x1, int z1) {
        int cz = (z0 + z1) / 2;
        pad(b, x0, z0, x1, z1, ground, p);
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                b.put(x, ground, z, GRAVEL);
            }
            if ((x & 1) == 0) {
                b.put(x, ground, cz, p.accent());
            }
            b.put(x, ground + 1, cz, RAIL_EW);
        }
        // Buffer stops at both ends.
        b.fill(x0, ground + 1, cz - 1, x0, ground + 2, cz + 1, IRON);
        b.fill(x1, ground + 1, cz - 1, x1, ground + 2, cz + 1, IRON);
        // A five-wide platform north of the rail: its edge three blocks off the centreline, with
        // five clear blocks and a canopy high enough for the four-tall carriage.
        b.fill(x0 + 10, ground, cz - 7, x1 - 10, ground, cz - 3, p.foundation());
        b.fill(x0 + 10, ground + 1, cz - 7, x1 - 10, ground + 1, cz - 3, p.accent());
        for (int px = x0 + 10; px <= x1 - 10; px += 16) {
            b.fill(px, ground + 2, cz - 3, px, ground + 6, cz - 3, p.accent());
            b.put(px, ground + 7, cz - 5, p.light());
        }
        b.fill(x0 + 8, ground + 7, cz - 8, x1 - 8, ground + 7, cz - 2, p.roofSlab());
    }

    private static void platform(StructureBuilder b, Palette p, int ground, int x0, int z0,
                                 int x1, int z1) {
        pave(b, x0, z0, x1, z1, ground, p.foundation(), p);
        for (int x = x0 + 2; x <= x1 - 2; x += 8) {
            b.fill(x, ground + 1, z0 + 1, x, ground + 4, z0 + 1, p.accent());
            b.fill(x, ground + 1, z1 - 1, x, ground + 4, z1 - 1, p.accent());
        }
        b.fill(x0, ground + 5, z0, x1, ground + 5, z1, p.roofSlab());
        bench(b, p, ground + 1, x0 + 6, z0 + 3);
        bench(b, p, ground + 1, x1 - 6, z1 - 3);
        b.put(x0 + 2, ground + 2, z0 + 2, p.light());
    }

    // ------------------------------------------------------------------ parks

    private static void parks(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        patriarchPond(b, p, ground, cx - 150, cz + 10, cx - 40, cz + 100);
        theatre(b, p, ground, cx - 100, cz - 74, 60, 40, 18);
        revolutionGarden(b, p, ground, cx + 20, cz - 70, cx + 100, cz - 24);
        alexanderGarden(b, p, ground, cx + 136, cz - 290, cx + 144, cz - 176);
        alexanderGarden(b, p, ground, cx + 136, cz - 144, cx + 144, cz - 100);
        arcade(b, p, ground, cx + 20, cz - 20, cx + 80, cz + 30, 1);
    }

    private static void patriarchPond(StructureBuilder b, Palette p, int ground, int x0, int z0,
                                      int x1, int z1) {
        pave(b, x0, z0, x1, z0 + 6, ground, p.ground(), p);
        pave(b, x0, z1 - 6, x1, z1, ground, p.ground(), p);
        pave(b, x0, z0, x0 + 6, z1, ground, p.ground(), p);
        pave(b, x1 - 6, z0, x1, z1, ground, p.ground(), p);
        pond(b, p, ground, x0 + 30, z0 + 25, x1 - 30, z1 - 25);
        for (int[] t : PARK_TREES) {
            tree(b, p, ground + 1, t[0], t[1]);
        }
        for (int lz = z0 + 12; lz <= z1 - 12; lz += 24) {
            bench(b, p, ground, x0 + 8, lz);
            bench(b, p, ground, x1 - 8, lz);
        }
        b.put(x0 + 2, ground + 2, z0 + 2, p.light());
        b.put(x1 - 2, ground + 2, z1 - 2, p.light());
    }

    private static void pond(StructureBuilder b, Palette p, int ground, int x0, int z0,
                             int x1, int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                int surface = b.groundY(x, z);
                if (surface > ground - 2) {
                    b.fill(x, ground - 1, z, x, surface + 2, z, AIR);
                }
                b.put(x, ground - 2, z, SAND);
                b.put(x, ground - 1, z, WATER);
            }
        }
        b.walls(x0 - 1, ground, z0 - 1, x1 + 1, ground, z1 + 1, p.foundation());
    }

    private static void theatre(StructureBuilder b, Palette p, int ground, int x, int z,
                                int w, int d, int h) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        pad(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x, ground + 1, z, x1, ground + h, z1, new Doorway(Side.S, w / 2));
        b.fill(x, ground + 1, z, x1, ground + 1, z1, p.foundation());
        b.walls(x, ground + 2, z, x1, ground + h - 1, z1, p.wall());
        b.fill(x, ground + h - 1, z, x1, ground + h - 1, z1, p.accent());
        // A white portico of columns.
        b.fill(x + 4, ground + 1, z1 + 1, x1 - 4, ground + 1, z1 + 7, p.foundation());
        for (int px = x + 6; px <= x1 - 6; px += 6) {
            b.fill(px, ground + 2, z1 + 6, px, ground + 9, z1 + 6, WHITE);
        }
        b.fill(x + 4, ground + 10, z1 + 4, x1 - 4, ground + 10, z1 + 8, p.accent());
        b.pitchedRoof(x - 2, z - 2, x1 + 2, z1 + 2, ground + h, 5, 0);
        b.fill(x + w / 2 - 6, ground + h + 6, z1 - 1, x + w / 2 + 6, ground + h + 8, z1 + 1,
                WHITE);
        b.put(x + w / 2, ground + h + 9, z1, GOLD);
        b.put(x + w / 2, ground + h + 10, z1, END_ROD);
        doorFor(b, p, x, z, x1, z1, ground + 2, Side.S, w / 2);
        b.put(x + w / 2, ground + 2, z1 + 7, p.light());
        b.put(x + 8, ground + 2, z1 + 7, p.light());
        b.put(x1 - 8, ground + 2, z1 + 7, p.light());
        b.monument(x + w / 2 + 30, z1 + 6, ground + 1);
    }

    private static void revolutionGarden(StructureBuilder b, Palette p, int ground, int x0,
                                         int z0, int x1, int z1) {
        pad(b, x0, z0, x1, z1, ground, p);
        b.fill(x0, ground + 1, z0, x1, ground + 1, z1, p.ground());
        fountain(b, p, ground + 1, (x0 + x1) / 2, (z0 + z1) / 2);
        for (int[] t : new int[][]{{x0 + 8, z0 + 8}, {x1 - 8, z0 + 8},
                {x0 + 8, z1 - 8}, {x1 - 8, z1 - 8}}) {
            tree(b, p, ground + 1, t[0], t[1]);
        }
        bench(b, p, ground + 1, x0 + 6, (z0 + z1) / 2);
        bench(b, p, ground + 1, x1 - 6, (z0 + z1) / 2);
        b.put(x0 + 2, ground + 2, z1 - 2, p.light());
    }

    private static void alexanderGarden(StructureBuilder b, Palette p, int ground, int x0,
                                        int z0, int x1, int z1) {
        pad(b, x0, z0, x1, z1, ground, p);
        b.fill(x0, ground + 1, z0, x1, ground + 1, z1, p.ground());
        for (int z = z0 + 6; z <= z1 - 6; z += 16) {
            tree(b, p, ground + 1, x0 + 3, z);
            tree(b, p, ground + 1, x1 - 3, z);
        }
        b.put(x0, ground + 1, z0, p.rail());
        b.put(x1, ground + 1, z1, p.rail());
    }

    private static void fountain(StructureBuilder b, Palette p, int ground, int x, int z) {
        b.fill(x - 4, ground, z - 4, x + 4, ground, z + 4, p.foundation());
        b.fill(x - 3, ground, z - 3, x + 3, ground, z + 3, WATER);
        b.fill(x - 1, ground + 1, z - 1, x + 1, ground + 2, z + 1, p.accent());
        b.put(x, ground + 3, z, p.light());
        b.put(x + 4, ground + 1, z, p.rail());
        b.put(x - 4, ground + 1, z, p.rail());
        b.put(x, ground + 1, z + 4, p.rail());
        b.put(x, ground + 1, z - 4, p.rail());
    }

    // ------------------------------------------------------------------ embankment

    private static void embankment(StructureBuilder b, int cx, int cz, int ground,
                                   CitySurface surface, Palette p) {
        // Iron railings along both quays, broken at the bridges.
        for (int x = -330; x <= 330; x++) {
            boolean gap = x >= -12 && x <= 11
                    || x >= 224 && x <= 247
                    || x >= -248 && x <= -223;
            if (!gap) {
                b.put(cx + x, surface.at(cx + x, cz + 166) + 1, cz + 166, p.rail());
                b.put(cx + x, surface.at(cx + x, cz + 190) + 1, cz + 190, p.rail());
            }
        }
        // Lamps and benches along the promenades.
        for (int x = -300; x <= 300; x += 40) {
            lamp(b, p, surface.at(cx + x, cz + 160), cx + x, cz + 160);
            lamp(b, p, surface.at(cx + x, cz + 196), cx + x, cz + 196);
            bench(b, p, surface.at(cx + x + 8, cz + 158), cx + x + 8, cz + 158);
            bench(b, p, surface.at(cx + x + 8, cz + 198), cx + x + 8, cz + 198);
        }
        for (int[] t : BANK_TREES) {
            tree(b, p, surface.at(cx + t[0], cz + t[1]) + 1, cx + t[0], cz + t[1]);
        }
        // Three bridges: the avenue, the factory line and Arbat.
        bridge(b, p, surface.at(cx, cz + 178), cx, 10, cz + 164, cz + 192);
        bridge(b, p, surface.at(cx + 235, cz + 178), cx + 235, 11, cz + 164, cz + 192);
        bridge(b, p, surface.at(cx - 236, cz + 178), cx - 236, 11, cz + 164, cz + 192);
        // Barges at the quays.
        barge(b, p, surface.at(cx + 72, cz + 178), cx + 60, cz + 173);
        barge(b, p, surface.at(cx - 168, cz + 178), cx - 180, cz + 173);
        barge(b, p, surface.at(cx + 292, cz + 178), cx + 280, cz + 173);
    }

    private static void bridge(StructureBuilder b, Palette p, int ground, int xCenter, int half,
                               int z0, int z1) {
        int x0 = xCenter - half;
        int x1 = xCenter + half;
        for (int z = z0; z <= z1; z++) {
            for (int x = x0; x <= x1; x++) {
                b.put(x, ground, z, p.foundation());
            }
            b.put(x0, ground + 1, z, p.rail());
            b.put(x1, ground + 1, z, p.rail());
        }
        for (int z = z0 + 6; z <= z1 - 6; z += 8) {
            for (int x = x0 + 2; x <= x1 - 2; x += 4) {
                b.fill(x, ground - 3, z, x, ground - 1, z, p.foundation());
            }
        }
        for (int x = x0; x <= x1; x += 4) {
            b.fill(x, ground + 1, z0, x, ground + 2, z0, p.accent());
            b.fill(x, ground + 1, z1, x, ground + 2, z1, p.accent());
        }
        b.put(x0 + 1, ground + 2, z0, p.light());
        b.put(x1 - 1, ground + 2, z1, p.light());
    }

    private static void barge(StructureBuilder b, Palette p, int ground, int x, int z) {
        // A low timber hull with a cabin and a cargo hold.
        b.fill(x, ground - 1, z, x + 24, ground - 1, z + 10, p.accent());
        b.walls(x, ground, z, x + 24, ground, z + 10, p.accent());
        b.fill(x + 2, ground, z + 2, x + 22, ground, z + 8, p.foundation());
        b.fill(x + 4, ground + 1, z + 3, x + 10, ground + 4, z + 7, p.wall());
        b.pitchedRoof(x + 3, z + 2, x + 11, z + 8, ground + 4, 2, 0);
        b.fill(x + 14, ground + 1, z + 3, x + 20, ground + 2, z + 7, p.rubble());
        b.put(x + 12, ground + 2, z + 5, p.light());
        b.fill(x + 22, ground + 1, z + 4, x + 23, ground + 3, z + 6, p.accent());
    }

    // ------------------------------------------------------------------ small props

    private static void tree(StructureBuilder b, Palette p, int ground, int x, int z) {
        int h = 4 + ((x + z) & 3);
        b.fill(x, ground, z, x, ground + h, z, SPRUCE_LOG);
        for (int i = 0; i < 4; i++) {
            int r = 3 - i;
            if (r < 1) {
                r = 1;
            }
            b.fill(x - r, ground + h - i, z - r, x + r, ground + h - i, z + r,
                    SPRUCE_LEAVES);
        }
        b.put(x, ground + h + 1, z, SNOW_LAYER);
    }

    private static void bench(StructureBuilder b, Palette p, int ground, int x, int z) {
        b.put(x - 1, ground, z, p.accent());
        b.put(x + 1, ground, z, p.accent());
        b.fill(x - 1, ground + 1, z, x + 1, ground + 1, z, p.roofSlab());
        b.put(x - 1, ground + 2, z, p.rail());
        b.put(x + 1, ground + 2, z, p.rail());
    }

    private static void lamp(StructureBuilder b, Palette p, int ground, int x, int z) {
        b.put(x, ground, z, p.foundation());
        b.fill(x, ground + 1, z, x, ground + 3, z, p.accent());
        b.put(x, ground + 4, z, p.light());
        b.put(x, ground + 5, z, p.roofSlab());
    }

    // ------------------------------------------------------------------ readable signs

    private static void signs(StructureBuilder b, int cx, int cz, int ground,
                              CitySurface surface, Palette p) {
        signPostAt(b, p, surface, cx - 60, cz - 100, Direction.SOUTH,
                "RED SQUARE", "ST BASIL", "KREMLIN GATE");
        signPostAt(b, p, surface, cx - 240, cz - 150, Direction.EAST,
                "ARBAT", "CAFES", "SHOPS", "INN");
        signPostAt(b, p, surface, cx + 20, cz + 112, Direction.SOUTH,
                "METRO", "STATION", "MIND THE GAP");
        signPostAt(b, p, surface, cx + 136, cz - 144, Direction.WEST,
                "KREMLIN", "PALACE", "ARMORY");
        signPostAt(b, p, surface, cx + 232, cz + 208, Direction.SOUTH,
                "FACTORY No.3", "NO ENTRY", "DANGER", "SMOKE");
        signPostAt(b, p, surface, cx + 20, cz + 158, Direction.SOUTH,
                "MOSKVA RIVER", "NO SWIMMING");
        signPostAt(b, p, surface, cx - 82, cz - 112, Direction.EAST,
                "METROPOL", "HOTEL", "ROOMS");
        signPostAt(b, p, surface, cx - 236, cz - 64, Direction.EAST,
                "ARBAT SHOP", "BRASS & SILK");
        signPostAt(b, p, surface, cx - 236, cz + 116, Direction.EAST,
                "HERBALIST", "REMEDIES", "HERBS & ROOTS");
        signPostAt(b, p, surface, cx - 258, cz - 120, Direction.EAST,
                "CHAPEL", "STOLEN ICON", "PRAY HERE");
        signPostAt(b, p, surface, cx + 252, cz - 158, Direction.WEST,
                "COMMISSAR", "MISSING", "FACTORY MEETING");
        signPostAt(b, p, surface, cx + 30, cz + 108, Direction.SOUTH,
                "TUNNEL", "SEALED", "KEEP OUT");
        signPostAt(b, p, surface, cx + 250, cz + 210, Direction.SOUTH,
                "WORKERS", "HOUSING", "SHIFT BELL");
        signPostAt(b, p, surface, cx - 236, cz + 130, Direction.EAST,
                "ARBAT END", "RIVER AHEAD");
        signPostAt(b, p, surface, cx + 200, cz + 280, Direction.SOUTH,
                "RAIL STATION", "MOSCOW YARD", "NO LOITERING");
        // A notice board by the metro stairs.
        int boardY = surface.at(cx + 88, cz + 108);
        b.put(cx + 88, boardY + 1, cz + 108, p.accent());
        CityPlanSupport.sign(b, cx + 88, boardY + 2, cz + 108, Direction.WEST,
                "CITY NOTICES", "METRO OPEN", "CURFEW AT", "MIDNIGHT");
    }

    private static void signPostAt(StructureBuilder b, Palette p, CitySurface surface,
                                   int x, int z, Direction facing, String... lines) {
        signPost(b, p, x, surface.at(x, z), z, facing, lines);
    }

    private static void signPost(StructureBuilder b, Palette p, int x, int y, int z,
                                 Direction facing, String... lines) {
        settle(b, x, z, y, p);
        b.put(x, y, z, p.accent());
        CityPlanSupport.sign(b, x, y + 1, z, facing, lines);
    }

    // ------------------------------------------------------------------ doors

    private static Side side(int code) {
        return switch (code) {
            case 1 -> Side.W;
            case 2 -> Side.E;
            case 3 -> Side.N;
            default -> Side.S;
        };
    }

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
}
