package com.sanchous98.eldritchhorror.world.loc.city.plan;

import net.minecraft.world.item.DyeColor;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.SiteLoot;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import java.util.List;
import java.util.function.IntBinaryOperator;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Delhi — the authored late-colonial capital: a domed marble mausoleum in a walled charbagh, the
 * Chandni Chowk bazaar, the Red Fort, the railway station, the civil lines and a sunken stepwell.
 *
 * <p>Everything is placed deliberately through {@link StructureBuilder}; there is no layout RNG and
 * no chunk-local read, so every chunk that overlaps the city writes the same blocks. Coordinates
 * are relative to {@code (cx, ground, cz)} in the plan's private methods and absolute in
 * {@link #build}. The shared generator has already levelled and paved the district; this plan lays
 * its own plinths, streets, gardens and water on top of that surface.
 */
public final class DelhiPlan implements CityPlan {

    /** Authored half-extent: 680 blocks across, matching the brief. */
    private static final int HALF_EXTENT = 340;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState LEAF = Blocks.DARK_OAK_LEAVES.defaultBlockState();
    private static final BlockState TRUNK = Blocks.DARK_OAK_LOG.defaultBlockState();
    private static final BlockState IRON = Blocks.IRON_BLOCK.defaultBlockState();
    private static final BlockState BLACK = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
    private static final BlockState RAIL = Blocks.RAIL.defaultBlockState();
    private static final BlockState HAY = Blocks.HAY_BLOCK.defaultBlockState();
    private static final BlockState BED = Blocks.WOOL.pick(DyeColor.RED).defaultBlockState();
    private static final BlockState BOOKS = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState CAULDRON = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState ORANGE = Blocks.DYED_TERRACOTTA.pick(DyeColor.ORANGE).defaultBlockState();
    private static final BlockState RED_CLAY = Blocks.DYED_TERRACOTTA.pick(DyeColor.RED).defaultBlockState();
    private static final BlockState YELLOW = Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW).defaultBlockState();
    private static final BlockState BLUE = Blocks.DYED_TERRACOTTA.pick(DyeColor.BLUE).defaultBlockState();

    @Override
    public String cityId() {
        return "delhi";
    }

    @Override
    public int halfExtent() {
        return HALF_EXTENT;
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
        streets(b, cx, cz, ground, p);
        arrivalGate(b, cx, cz, ground, p, surface);
        centralChowk(b, cx, cz, ground, p);
        charbagh(b, cx, cz, ground, p);
        mausoleum(b, cx, cz, ground, p);
        tombCrypt(b, cx, cz, ground, p);
        chandniChowk(b, cx, cz, ground, p);
        stepwell(b, cx, cz, ground, p);
        redFort(b, cx, cz, ground, p);
        railwayStation(b, cx, cz, ground, p);
        civilLines(b, cx, cz, ground, p);
        outskirts(b, cx, cz, ground, p);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.LANDMARK, 0, 0, 320, "delhi"),
                new CityPoi("arrival_sign", CityPoi.Kind.SIGN, 9, 1, 312, "districts"),
                new CityPoi("mausoleum", CityPoi.Kind.LANDMARK, 0, 0, -200, "humayun_tomb"),
                new CityPoi("tomb_crypt", CityPoi.Kind.SECRET, 0, -6, -160, "crypt"),
                new CityPoi("bazaar_quest_board", CityPoi.Kind.QUEST_BOARD, -220, 0, 0,
                        "delhi_missing_bride;delhi_stolen_relic"),
                new CityPoi("caravanserai_inn", CityPoi.Kind.INN, -240, 0, -80, "innkeeper"),
                new CityPoi("innkeeper", CityPoi.Kind.NPC, -240, 0, -86, "innkeeper"),
                new CityPoi("spice_shop", CityPoi.Kind.SHOP, -240, 0, 60, "spice_merchant"),
                new CityPoi("spice_merchant", CityPoi.Kind.NPC, -249, 0, 52, "spice_merchant"),
                new CityPoi("hakim", CityPoi.Kind.HEALER, -200, 0, 120, "delhi_missing_bride"),
                new CityPoi("hakim_npc", CityPoi.Kind.NPC, -188, 0, 110, "hakim"),
                new CityPoi("stepwell", CityPoi.Kind.SECRET, -200, -5, 140, "hidden_shrine"),
                new CityPoi("fort_gate", CityPoi.Kind.LANDMARK, 160, 0, -80, "lahori_gate"),
                new CityPoi("guard", CityPoi.Kind.NPC, 154, 0, -58, "guard"),
                new CityPoi("fort_clerk", CityPoi.Kind.NPC, 240, 0, -120, "delhi_stolen_relic"),
                new CityPoi("station", CityPoi.Kind.STATION, 220, 0, 120, "railway_station"),
                new CityPoi("rail_yard", CityPoi.Kind.STATION, 272, 1, 218, "delhi_rail_yard"),
                new CityPoi("stationmaster", CityPoi.Kind.NPC, 240, 0, 120, "railway_rumours"),
                new CityPoi("guide", CityPoi.Kind.NPC, 8, 0, 306, "guide"));
    }

    // ------------------------------------------------------------------ streets

    private static void streets(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // The 14-wide processional axis from the arrival gate to the mausoleum garden.
        CityPlanSupport.path(b, cx, cz - 80, cx, cz + 322, 14, g, p.ground());
        // The central east-west road: bazaar west, fort and station east.
        CityPlanSupport.path(b, cx - 322, cz, cx + 322, cz, 10, g, p.ground());
        // Fort road (10 wide) and station road (10 wide).
        CityPlanSupport.path(b, cx + 7, cz - 80, cx + 162, cz - 80, 10, g, p.ground());
        CityPlanSupport.path(b, cx + 7, cz + 120, cx + 190, cz + 120, 10, g, p.ground());
        // Chandni Chowk spine and its cross lanes (5 wide).
        CityPlanSupport.path(b, cx - 220, cz - 262, cx - 220, cz + 202, 5, g, p.ground());
        CityPlanSupport.path(b, cx - 322, cz - 80, cx - 140, cz - 80, 5, g, p.ground());
        CityPlanSupport.path(b, cx - 322, cz + 60, cx - 140, cz + 60, 5, g, p.ground());
        CityPlanSupport.path(b, cx - 220, cz - 40, cx, cz - 40, 5, g, p.ground());
        CityPlanSupport.path(b, cx - 220, cz + 80, cx, cz + 80, 5, g, p.ground());
        // The stepwell lane drops south from the spice lane.
        CityPlanSupport.path(b, cx - 200, cz + 60, cx - 200, cz + 122, 5, g, p.ground());
        // Civil lines road (8 wide).
        CityPlanSupport.path(b, cx - 322, cz + 250, cx + 322, cz + 250, 8, g, p.ground());
    }

    // ------------------------------------------------------------------ arrival

    private static void arrivalGate(StructureBuilder b, int cx, int cz, int g, Palette p,
                                    IntBinaryOperator surface) {
        int z0 = cz + 314;
        int z1 = cz + 326;
        // Sit the gate on the graded surface, with a stepped apron from the flat road.
        int gy = Math.max(g, surface.applyAsInt(cx, cz + 320));
        for (int i = 0; i < gy - g; i++) {
            b.fill(cx - 18, g + i, cz + 306, cx + 18, g + i, cz + 312, p.foundation());
        }
        b.fill(cx - 22, gy - 1, cz + 306, cx + 22, gy - 1, cz + 332, p.foundation());
        b.fill(cx - 22, gy, cz + 306, cx + 22, gy, cz + 332, p.ground());

        // Two sandstone pylons and the lintel across the 15-wide passage.
        pylon(b, cx - 13, cx - 8, z0, z1, gy, p);
        pylon(b, cx + 8, cx + 13, z0, z1, gy, p);
        b.fill(cx - 13, gy + 12, z0, cx + 13, gy + 14, z1, p.wall());
        b.fill(cx - 13, gy + 12, z0, cx + 13, gy + 12, z1, p.accent());
        b.crenellations(cx - 13, z0, cx + 13, z1, gy + 15);

        // A pointed quartz frame around the passage mouth.
        b.fill(cx - 8, gy + 1, z1, cx - 8, gy + 11, z1, p.accent());
        b.fill(cx + 8, gy + 1, z1, cx + 8, gy + 11, z1, p.accent());
        b.fill(cx - 7, gy + 11, z1, cx + 7, gy + 11, z1, p.accent());

        // Gate name and the two signposts that point at the districts.
        b.sign(cx, gy + 15, z1, Direction.SOUTH, "DELHI");
        signpost(b, cx + 9, gy + 1, cz + 312, Direction.SOUTH, p,
                "MAUSOLEUM", "CHANDNI CHOWK", "RED FORT", "RAILWAY STATION");
        signpost(b, cx - 9, gy + 1, cz + 312, Direction.SOUTH, p,
                "CITY NOTICES", "DELHI", "TRAVEL SAFE");

        // Lamps on the pylons and a pair of low flanking walls.
        lamp(b, cx - 11, gy + 12, z1, p);
        lamp(b, cx + 11, gy + 12, z1, p);
        b.fill(cx - 22, gy + 1, z0, cx - 14, gy + 2, z1, p.weathered());
        b.fill(cx + 14, gy + 1, z0, cx + 22, gy + 2, z1, p.weathered());
        b.crenellations(cx - 22, z0, cx - 14, z1, gy + 3);
        b.crenellations(cx + 14, z0, cx + 22, z1, gy + 3);
    }

    private static void pylon(StructureBuilder b, int x0, int x1, int z0, int z1, int g, Palette p) {
        b.fill(x0, g, z0, x1, g + 14, z1, p.wall());
        b.fill(x0, g, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0, g + 4, z0, x1, g + 4, z1, p.accent());
        b.fill(x0, g + 10, z0, x1, g + 10, z1, p.accent());
        b.fill(x0 + 1, g + 2, z0, x0 + 1, g + 9, z1, p.weathered());
    }

    private static void centralChowk(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // A marble traffic island with a fountain where the axis meets the cross road.
        b.fill(cx - 9, g, cz - 9, cx + 9, g, cz + 9, p.accent());
        fountain(b, cx, cz, g, p);
        lamp(b, cx - 8, g + 1, cz - 8, p);
        lamp(b, cx + 8, g + 1, cz - 8, p);
        lamp(b, cx - 8, g + 1, cz + 8, p);
        lamp(b, cx + 8, g + 1, cz + 8, p);
        signpost(b, cx - 12, g + 1, cz - 8, Direction.WEST, p, "CHANDNI CHOWK", "BAZAAR WEST");
    }

    // ------------------------------------------------------------------ charbagh

    private static void charbagh(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 140;
        int x1 = cx + 140;
        int z0 = cz - 300;
        int z1 = cz - 80;

        // Walled garden: red sandstone curtain wall with a crenellated top.
        b.walls(x0, g, z0, x1, g + 1, z1, p.foundation());
        b.walls(x0, g + 2, z0, x1, g + 5, z1, p.wall());
        b.walls(x0, g + 6, z0, x1, g + 6, z1, p.wall());
        b.crenellations(x0, z0, x1, z1, g + 7);

        // South and north gates: an opening, a gatehouse and two chhatris.
        b.fill(cx - 8, g + 1, z1 - 1, cx + 8, g + 8, z1 + 1, AIR);
        b.fill(cx - 8, g + 1, z0 - 1, cx + 8, g + 8, z0 + 1, AIR);
        gardenGate(b, cx, z1, g, p, true);
        gardenGate(b, cx, z0, g, p, false);

        // The four lawns of the charbagh, kept clear of the central plinth.
        b.fill(cx - 136, g, cz - 296, cx - 34, g, cz - 206, GRASS);
        b.fill(cx + 34, g, cz - 296, cx + 136, g, cz - 206, GRASS);
        b.fill(cx - 136, g, cz - 194, cx - 34, g, cz - 84, GRASS);
        b.fill(cx + 34, g, cz - 194, cx + 136, g, cz - 84, GRASS);

        // Marble walks along the two water axes and around the plinth.
        b.fill(cx - 4, g, z0 + 3, cx + 4, g, z1 - 3, p.accent());
        b.fill(x0 + 3, g, cz - 204, x1 - 3, g, cz - 196, p.accent());

        // Raised water channels, interrupted by the plinth.
        for (int z = z0 + 4; z <= z1 - 4; z++) {
            if (z > cz - 236 && z < cz - 164) {
                continue;
            }
            channelNS(b, cx, z, g, p);
        }
        for (int x = x0 + 4; x <= x1 - 4; x++) {
            if (x > cx - 36 && x < cx + 36) {
                continue;
            }
            channelEW(b, x, cz - 200, g, p);
        }

        // Reflecting pools on the axis, north and south of the tomb.
        pool(b, cx - 10, cz - 266, cx + 10, cz - 254, g, p);
        pool(b, cx - 10, cz - 150, cx + 10, cz - 130, g, p);
        fountain(b, cx, cz - 260, g, p);
        fountain(b, cx, cz - 140, g, p);

        // Cypress avenues: dark green columns framing the channels.
        for (int z = z0 + 12; z <= z1 - 12; z += 12) {
            if (z > cz - 240 && z < cz - 160) {
                continue;
            }
            cypress(b, cx - 8, g, z);
            cypress(b, cx + 8, g, z);
        }
        for (int x = x0 + 12; x <= x1 - 12; x += 12) {
            if (x > cx - 40 && x < cx + 40) {
                continue;
            }
            cypress(b, x, g, cz - 208);
            cypress(b, x, g, cz - 192);
        }

        // Corner pavilions and garden lamps.
        chhatri(b, x0 + 8, z0 + 8, g, p);
        chhatri(b, x1 - 8, z0 + 8, g, p);
        chhatri(b, x0 + 8, z1 - 8, g, p);
        chhatri(b, x1 - 8, z1 - 8, g, p);
        for (int z = cz - 288; z <= cz - 96; z += 24) {
            if (z > cz - 236 && z < cz - 164) {
                continue;
            }
            lamp(b, cx - 12, g + 1, z, p);
            lamp(b, cx + 12, g + 1, z, p);
        }
        signpost(b, cx + 12, g + 1, z1 + 6, Direction.SOUTH, p, "MAUSOLEUM", "TOMB GARDEN");
    }

    private static void channelNS(StructureBuilder b, int x, int z, int g, Palette p) {
        b.put(x, g - 1, z, p.foundation());
        b.put(x - 2, g + 1, z, p.accent());
        b.put(x - 1, g, z, WATER);
        b.put(x, g, z, WATER);
        b.put(x + 1, g, z, WATER);
        b.put(x + 2, g + 1, z, p.accent());
    }

    private static void channelEW(StructureBuilder b, int x, int z, int g, Palette p) {
        b.put(x, g - 1, z, p.foundation());
        b.put(x, g + 1, z - 2, p.accent());
        b.put(x, g, z - 1, WATER);
        b.put(x, g, z, WATER);
        b.put(x, g, z + 1, WATER);
        b.put(x, g + 1, z + 2, p.accent());
    }

    private static void gardenGate(StructureBuilder b, int cx, int z, int g, Palette p, boolean south) {
        int z0 = z - 3;
        int z1 = z + 3;
        b.fill(cx - 11, g, z0, cx + 11, g + 10, z1, p.wall());
        b.fill(cx - 11, g, z0, cx + 11, g + 1, z1, p.foundation());
        b.fill(cx - 8, g + 1, z0, cx + 8, g + 8, z1, AIR);
        b.fill(cx - 8, g + 1, z0, cx - 8, g + 8, z1, p.accent());
        b.fill(cx + 8, g + 1, z0, cx + 8, g + 8, z1, p.accent());
        b.fill(cx - 11, g + 9, z0, cx + 11, g + 9, z1, p.accent());
        b.crenellations(cx - 11, z0, cx + 11, z1, g + 11);
        chhatri(b, cx - 8, z, g + 10, p);
        chhatri(b, cx + 8, z, g + 10, p);
        if (south) {
            b.sign(cx, g + 10, z1, Direction.SOUTH, "MAUSOLEUM");
        } else {
            b.sign(cx, g + 10, z0, Direction.NORTH, "MAUSOLEUM");
        }
    }

    private static void pool(StructureBuilder b, int x0, int z0, int x1, int z1, int g, Palette p) {
        b.fill(x0, g - 1, z0, x1, g - 1, z1, p.foundation());
        b.fill(x0 + 1, g, z0 + 1, x1 - 1, g, z1 - 1, WATER);
        b.walls(x0, g + 1, z0, x1, g + 1, z1, p.accent());
    }

    private static void fountain(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx - 4, g - 1, cz - 4, cx + 4, g - 1, cz + 4, p.foundation());
        b.fill(cx - 3, g, cz - 3, cx + 3, g, cz + 3, WATER);
        b.walls(cx - 3, g + 1, cz - 3, cx + 3, g + 1, cz + 3, p.accent());
        b.put(cx, g + 1, cz, p.accent());
        b.put(cx, g + 2, cz, p.light());
    }

    // ------------------------------------------------------------------ mausoleum

    private static void mausoleum(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int mz = cz - 200;
        // Red sandstone plinth with a marble terrace; the stair climbs from the east lawn.
        b.fill(cx - 31, g + 1, mz - 31, cx + 31, g + 1, mz + 31, p.foundation());
        b.fill(cx - 31, g + 2, mz - 31, cx + 31, g + 2, mz + 31, p.accent());
        b.fill(cx + 32, g + 1, mz - 6, cx + 32, g + 1, mz + 6, p.accent());
        b.fill(cx + 33, g, mz - 6, cx + 33, g, mz + 6, p.accent());

        int x0 = cx - 15;
        int x1 = cx + 15;
        int z0 = mz - 15;
        int z1 = mz + 15;
        int y0 = g + 3;
        int y1 = g + 33;

        // The white marble cube: floor, shell, hollow hall, ceiling.
        b.fill(x0, y0, z0, x1, y0, z1, p.foundation());
        b.walls(x0, y0 + 1, z0, x1, y1 - 1, z1, p.accent());
        b.fill(x0 + 1, y0 + 1, z0 + 1, x1 - 1, y1 - 1, z1 - 1, AIR);
        b.fill(x0, y1, z0, x1, y1, z1, p.roof());
        b.walls(x0, y0, z0, x1, y0 + 2, z1, p.wall());

        // The great south iwan opens into the hall; the other faces carry blind arches.
        archX(b, cx - 5, cx + 5, y0 + 1, z1, 10, p);
        b.fill(cx - 4, y0 + 1, z0, cx + 4, y0 + 8, z0, p.weathered());
        b.fill(cx - 5, y0 + 1, z0, cx - 5, y0 + 9, z0, p.accent());
        b.fill(cx + 5, y0 + 1, z0, cx + 5, y0 + 9, z0, p.accent());
        b.fill(cx - 5, y0 + 9, z0, cx + 5, y0 + 9, z0, p.accent());
        b.fill(x0, y0 + 1, mz - 4, x0, y0 + 8, mz + 4, p.weathered());
        b.fill(x0, y0 + 1, mz - 5, x0, y0 + 9, mz - 5, p.accent());
        b.fill(x0, y0 + 1, mz + 5, x0, y0 + 9, mz + 5, p.accent());
        b.fill(x0, y0 + 9, mz - 5, x0, y0 + 9, mz + 5, p.accent());
        b.fill(x1, y0 + 1, mz - 4, x1, y0 + 8, mz + 4, p.weathered());
        b.fill(x1, y0 + 1, mz - 5, x1, y0 + 9, mz - 5, p.accent());
        b.fill(x1, y0 + 1, mz + 5, x1, y0 + 9, mz + 5, p.accent());
        b.fill(x1, y0 + 9, mz - 5, x1, y0 + 9, mz + 5, p.accent());

        // Chhajja cornice and the roof terrace.
        b.fill(x0 - 1, y1 + 1, z0 - 1, x1 + 1, y1 + 1, z1 + 1, p.accent());
        b.fill(x0 - 2, y1 + 2, z0 - 2, x1 + 2, y1 + 2, z1 + 2, p.roofSlab());
        b.walls(x0 - 2, y1 + 3, z0 - 2, x1 + 2, y1 + 3, z1 + 2, p.rail());
        chhatri(b, x0 + 3, z0 + 3, y1 + 3, p);
        chhatri(b, x1 - 3, z0 + 3, y1 + 3, p);
        chhatri(b, x0 + 3, z1 - 3, y1 + 3, p);
        chhatri(b, x1 - 3, z1 - 3, y1 + 3, p);

        // Tall marble windows on the long faces.
        for (int z = z0 + 4; z <= z1 - 4; z += 6) {
            b.window(x0, y0 + 14, z, 6, 2, true);
            b.window(x1 - 1, y0 + 14, z, 6, 2, true);
        }

        // Drum and onion dome.
        b.fill(cx - 9, y1 + 3, mz - 9, cx + 9, y1 + 3, mz + 9, p.foundation());
        b.fill(cx - 9, y1 + 4, mz - 9, cx + 9, y1 + 8, mz + 9, p.accent());
        onionDome(b, cx, mz, y1 + 9, p);
        for (int i = 18; i <= 23; i++) {
            b.put(cx, y1 + i, mz, p.accent());
        }
        b.put(cx, y1 + 24, mz, p.light());

        // Four marble minarets on the plinth corners.
        minaret(b, cx - 26, mz - 26, g + 2, 48, p);
        minaret(b, cx + 26, mz - 26, g + 2, 48, p);
        minaret(b, cx - 26, mz + 26, g + 2, 48, p);
        minaret(b, cx + 26, mz + 26, g + 2, 48, p);

        // The cenotaph in the hall.
        b.fill(cx - 2, y0 + 1, mz - 4, cx + 2, y0 + 1, mz + 4, p.accent());
        b.fill(cx - 1, y0 + 2, mz - 3, cx + 1, y0 + 2, mz + 3, p.foundation());
        b.put(cx - 2, y0 + 2, mz - 4, p.light());
        b.put(cx + 2, y0 + 2, mz + 4, p.light());
        signpost(b, cx, y0 + 1, mz + 10, Direction.SOUTH, p, "NAWAB'S TOMB");
    }

    private static void onionDome(StructureBuilder b, int cx, int cz, int baseY, Palette p) {
        int[] rings = {9, 10, 10, 9, 8, 7, 5, 3, 1};
        for (int i = 0; i < rings.length; i++) {
            int r = rings[i];
            int y = baseY + i;
            b.fill(cx - r, y, cz - r, cx + r, y, cz + r, p.roof());
            for (int j = -r; j <= r; j++) {
                b.put(cx + j, y, cz - r, p.accent());
                b.put(cx + j, y, cz + r, p.accent());
                b.put(cx - r, y, cz + j, p.accent());
                b.put(cx + r, y, cz + j, p.accent());
            }
        }
    }

    private static void minaret(StructureBuilder b, int x, int z, int baseY, int height, Palette p) {
        b.fill(x - 2, baseY, z - 2, x + 2, baseY, z + 2, p.foundation());
        for (int i = 1; i <= height; i++) {
            int y = baseY + i;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    b.put(x + dx, y, z + dz, p.accent());
                }
            }
            if (i % 12 == 0) {
                b.fill(x - 2, y, z - 2, x + 2, y, z + 2, p.roofSlab());
                b.walls(x - 2, y + 1, z - 2, x + 2, y + 1, z + 2, p.rail());
            } else if (i % 12 == 6) {
                b.fill(x - 1, y, z - 1, x + 1, y, z + 1, p.wall());
            }
        }
        int top = baseY + height + 1;
        domeCap(b, x, z, top, 2, p);
        b.put(x, top + 4, z, p.light());
    }

    // ------------------------------------------------------------------ crypt

    private static void tombCrypt(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // A stair well south of the plinth drops under the tomb.
        int ex = cx;
        int ez = cz - 160;
        b.fill(ex - 5, g - 12, ez - 12, ex + 5, g + 4, ez, AIR);
        b.fill(ex - 6, g - 12, ez - 13, ex - 6, g + 3, ez + 1, p.wall());
        b.fill(ex + 6, g - 12, ez - 13, ex + 6, g + 3, ez + 1, p.wall());
        b.fill(ex - 6, g + 4, ez - 13, ex + 6, g + 4, ez + 1, p.wall());
        b.fill(ex - 6, g + 5, ez - 1, ex + 6, g + 5, ez + 1, p.accent());
        b.fill(ex - 6, g + 1, ez, ex - 6, g + 4, ez, p.accent());
        b.fill(ex + 6, g + 1, ez, ex + 6, g + 4, ez, p.accent());
        for (int i = 0; i < 11; i++) {
            int top = g - 1 - i;
            int z = ez - i;
            b.fill(ex - 4, g - 12, z, ex + 4, top, z, p.foundation());
            b.put(ex - 4, top, z, p.accent());
            b.put(ex + 4, top, z, p.accent());
        }
        b.sign(ex, g + 5, ez, Direction.SOUTH, "TOMB CRYPT", "NO ENTRY");

        // The chamber under the plinth.
        int x0 = cx - 12;
        int x1 = cx + 12;
        int z0 = cz - 192;
        int z1 = cz - 172;
        int floor = g - 12;
        b.fill(x0 - 1, floor - 1, z0 - 1, x1 + 1, floor - 1, z1 + 1, p.foundation());
        b.walls(x0, floor, z0, x1, floor + 6, z1, p.wall());
        b.fill(x0 + 1, floor, z0 + 1, x1 - 1, floor + 5, z1 - 1, AIR);
        b.fill(x0, floor + 6, z0, x1, floor + 6, z1, p.foundation());
        b.fill(x0 + 1, floor, z0 + 1, x1 - 1, floor, z1 - 1, p.accent());
        // Opening to the stair well.
        b.fill(cx - 3, floor, z1, cx + 3, floor + 3, z1, AIR);

        // Sarcophagus, chests and the warning inscription.
        b.fill(cx - 2, floor + 1, cz - 186, cx + 2, floor + 1, cz - 180, p.accent());
        b.fill(cx - 1, floor + 2, cz - 185, cx + 1, floor + 2, cz - 181, p.foundation());
        b.chest(cx - 9, floor + 1, cz - 188, SiteLoot.RELIC);
        b.chest(cx + 9, floor + 1, cz - 188, SiteLoot.RELIC);
        b.put(cx - 11, floor + 5, cz - 191, Palette.cobweb());
        b.put(cx + 11, floor + 5, cz - 191, Palette.cobweb());
        b.put(cx - 11, floor + 5, cz - 173, Palette.cobweb());
        b.put(cx + 11, floor + 5, cz - 173, Palette.cobweb());
        lamp(b, cx - 8, floor + 1, cz - 178, p);
        lamp(b, cx + 8, floor + 1, cz - 178, p);
        b.sign(cx, floor + 3, z0, Direction.SOUTH, "TOMB CRYPT", "RELIC SLEEPS", "DO NOT DISTURB");
    }

    // ------------------------------------------------------------------ bazaar

    private static void chandniChowk(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int west = cx - 236;
        int east = cx - 214;
        // West row: the spine-facing shops, broken by the inn and the spice market.
        shopRun(b, west, cz - 256, cz - 124, g, p, Direction.EAST, 0);
        shopRun(b, west, cz - 76, cz - 44, g, p, Direction.EAST, 1);
        shopRun(b, west, cz - 36, cz - 8, g, p, Direction.EAST, 2);
        shopRun(b, west, cz + 8, cz + 36, g, p, Direction.EAST, 0);
        shopRun(b, west, cz + 64, cz + 76, g, p, Direction.EAST, 1);
        shopRun(b, west, cz + 84, cz + 196, g, p, Direction.EAST, 2);
        // East row, facing back across the lane.
        shopRun(b, east, cz - 256, cz - 124, g, p, Direction.WEST, 1);
        shopRun(b, east, cz - 76, cz - 44, g, p, Direction.WEST, 2);
        shopRun(b, east, cz - 36, cz - 8, g, p, Direction.WEST, 0);
        shopRun(b, east, cz + 8, cz + 36, g, p, Direction.WEST, 1);
        shopRun(b, east, cz + 64, cz + 76, g, p, Direction.WEST, 0);
        shopRun(b, east, cz + 84, cz + 100, g, p, Direction.WEST, 2);

        caravanserai(b, cx, cz, g, p);
        spiceShop(b, cx, cz, g, p);
        hakimClinic(b, cx, cz, g, p);
        noticeBoard(b, cx - 228, cz - 10, g, p);
        teaStall(b, cx - 231, cz - 18, g, p);
        teaStall(b, cx - 210, cz + 40, g, p);

        // A collapsed stretch of the west row, with a warning.
        collapsedLane(b, west, cz + 104, g, p);

        // Lamps down the spine.
        for (int z = cz - 248; z <= cz + 192; z += 32) {
            if (Math.abs(z - cz) < 12 || Math.abs(z - cz + 40) < 8
                    || Math.abs(z - cz - 60) < 8 || Math.abs(z - cz - 80) < 8
                    || (z > cz - 124 && z < cz - 80)) {
                continue;
            }
            lamp(b, cx - 224, g + 1, z, p);
            lamp(b, cx - 216, g + 1, z, p);
        }
        signpost(b, cx - 224, g + 1, cz - 70, Direction.SOUTH, p, "CHANDNI CHOWK", "BAZAAR");
        signpost(b, cx - 224, g + 1, cz + 70, Direction.NORTH, p, "CHANDNI CHOWK", "OLD DELHI");
    }

    private static void shopRun(StructureBuilder b, int x0, int zStart, int zEnd, int g, Palette p,
                                Direction front, int variant) {
        int z = zStart;
        int i = 0;
        while (z + 15 <= zEnd) {
            bazaarShop(b, x0, z, g, p, front, (variant + i) % 3);
            z += 20;
            i++;
        }
    }

    private static void bazaarShop(StructureBuilder b, int x0, int z0, int g, Palette p,
                                   Direction front, int variant) {
        boolean east = front == Direction.EAST;
        int x1 = x0 + 10;
        int z1 = z0 + 15;
        int h = variant == 1 ? 8 : 6;

        b.fill(x0, g - 1, z0, x1, g - 1, z1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + h, z1, p.wall());
        b.walls(x0, g + 1, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + h - 1, z1 - 1, AIR);

        int fx = east ? x1 : x0;
        b.fill(fx, g + 1, z0 + 4, fx, g + 3, z0 + 11, AIR);
        int counterX = east ? x1 - 1 : x0 + 1;
        b.fill(counterX, g + 1, z0 + 4, counterX, g + 1, z0 + 11, p.foundation());

        // Cloth awning on posts, projecting over the lane.
        int a0 = east ? x1 + 1 : x0 - 3;
        int a1 = east ? x1 + 3 : x0 - 1;
        b.fill(a0, g + 4, z0 + 3, a1, g + 4, z0 + 12, p.roofSlab());
        int px = east ? a1 : a0;
        b.put(px, g + 1, z0 + 4, p.accent());
        b.put(px, g + 2, z0 + 4, p.accent());
        b.put(px, g + 3, z0 + 4, p.accent());
        b.put(px, g + 1, z0 + 11, p.accent());
        b.put(px, g + 2, z0 + 11, p.accent());
        b.put(px, g + 3, z0 + 11, p.accent());

        b.fill(x0, g + h, z0, x1, g + h, z1, p.roof());
        b.walls(x0, g + h + 1, z0, x1, g + h + 1, z1, p.rail());

        if (variant == 1) {
            int wfx = east ? fx - 1 : fx;
            b.window(wfx, g + 5, z0 + 5, 2, 3, false);
            b.window(wfx, g + 5, z0 + 10, 2, 3, false);
            chhatri(b, (x0 + x1) / 2, (z0 + z1) / 2, g + h, p);
        }
        if (variant == 0) {
            b.put(x0 + 5, g + 1, z0 + 6, ORANGE);
            b.put(x0 + 5, g + 2, z0 + 6, YELLOW);
            b.put(x0 + 5, g + 1, z0 + 9, RED_CLAY);
            b.sign(fx, g + 5, z0 + 2, front, "CLOTH", "SILK & DYE");
        } else if (variant == 2) {
            b.put(x0 + 2, g + 1, z0 + 13, CAULDRON);
            b.put(x0 + 8, g + 1, z0 + 13, ORANGE);
            b.sign(fx, g + 5, z0 + 2, front, "TEA", "SPICE");
        } else {
            b.put(x0 + 5, g + 1, z0 + 8, p.rail());
            b.put(x0 + 5, g + 2, z0 + 8, p.light());
        }
    }

    private static void caravanserai(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 259;
        int x1 = cx - 223;
        int z0 = cz - 120;
        int z1 = cz - 84;
        b.fill(x0, g - 1, z0, x1, g - 1, z1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 7, z1, p.wall());
        b.walls(x0, g + 1, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 6, z1 - 1, AIR);

        // Arched south gate onto the bazaar lane.
        b.fill(cx - 244, g + 1, z1, cx - 236, g + 4, z1, AIR);
        b.fill(cx - 245, g + 1, z1, cx - 245, g + 5, z1, p.accent());
        b.fill(cx - 235, g + 1, z1, cx - 235, g + 5, z1, p.accent());
        b.fill(cx - 245, g + 5, z1, cx - 235, g + 5, z1, p.accent());
        b.sign(cx - 240, g + 6, z1, Direction.SOUTH, "CARAVANSERAI", "ROOMS & STABLES");

        // Courtyard well.
        b.fill(cx - 243, g + 1, cz - 105, cx - 237, g + 1, cz - 99, p.foundation());
        b.fill(cx - 242, g + 2, cz - 104, cx - 238, g + 2, cz - 100, WATER);
        b.walls(cx - 242, g + 2, cz - 104, cx - 238, g + 2, cz - 100, p.accent());
        b.put(cx - 242, g + 3, cz - 102, p.light());

        // Three cells along each side, each with a bed.
        for (int i = 0; i < 3; i++) {
            int zc = z0 + 3 + i * 11;
            innCell(b, x0 + 1, zc, g, p, Direction.EAST);
            innCell(b, x1 - 9, zc, g, p, Direction.WEST);
        }

        // Roof strips over the cells, crenellated; the courtyard stays open.
        b.fill(x0, g + 7, z0, x0 + 10, g + 7, z1, p.roofSlab());
        b.fill(x1 - 10, g + 7, z0, x1, g + 7, z1, p.roofSlab());
        b.crenellations(x0, z0, x1, z1, g + 8);
        chhatri(b, x0 + 5, z0 + 5, g + 7, p);
        chhatri(b, x1 - 5, z0 + 5, g + 7, p);
        chhatri(b, x0 + 5, z1 - 5, g + 7, p);
        chhatri(b, x1 - 5, z1 - 5, g + 7, p);

        // Stables along the north wall.
        b.fill(cx - 248, g + 1, cz - 118, cx - 234, g + 1, cz - 118, HAY);
        b.fill(cx - 248, g + 2, cz - 118, cx - 234, g + 2, cz - 118, p.rail());
        lamp(b, cx - 246, g + 1, cz - 96, p);
        lamp(b, cx - 234, g + 1, cz - 96, p);
    }

    private static void innCell(StructureBuilder b, int x0, int z0, int g, Palette p,
                                Direction doorFacing) {
        int x1 = x0 + 8;
        int z1 = z0 + 7;
        b.walls(x0, g + 1, z0, x1, g + 4, z1, p.wall());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 3, z1 - 1, AIR);
        b.fill(x0 + 1, g, z0 + 1, x1 - 1, g, z1 - 1, p.ground());
        b.fill(x0 + 2, g + 1, z0 + 2, x0 + 4, g + 1, z0 + 3, BED);
        b.put(x0 + 2, g + 1, z0 + 4, p.roofSlab());
        int dz = z0 + 4;
        if (doorFacing == Direction.EAST) {
            b.fill(x1, g + 1, dz, x1, g + 2, dz, AIR);
            door(b, x1, g + 1, dz, Direction.EAST, p);
        } else {
            b.fill(x0, g + 1, dz, x0, g + 2, dz, AIR);
            door(b, x0, g + 1, dz, Direction.WEST, p);
        }
        b.put(x0 + 7, g + 1, z0 + 1, p.rail());
        b.put(x0 + 7, g + 2, z0 + 1, p.light());
    }

    private static void spiceShop(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 258;
        int x1 = cx - 240;
        int z0 = cz + 36;
        int z1 = cz + 54;
        b.fill(x0, g - 1, z0, x1, g - 1, z1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 6, z1, p.wall());
        b.walls(x0, g + 1, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 5, z1 - 1, AIR);
        b.fill(x0 + 3, g + 1, z1, x1 - 3, g + 3, z1, AIR);
        b.fill(x0 + 3, g + 1, z1 - 1, x1 - 3, g + 1, z1 - 1, p.foundation());
        b.fill(x0 + 2, g + 5, z1 + 1, x1 - 2, g + 5, z1 + 3, p.roofSlab());
        b.put(x0 + 2, g + 1, z1 + 3, p.accent());
        b.put(x0 + 2, g + 2, z1 + 3, p.accent());
        b.put(x0 + 2, g + 3, z1 + 3, p.accent());
        b.put(x0 + 2, g + 4, z1 + 3, p.accent());
        b.put(x1 - 2, g + 1, z1 + 3, p.accent());
        b.put(x1 - 2, g + 2, z1 + 3, p.accent());
        b.put(x1 - 2, g + 3, z1 + 3, p.accent());
        b.put(x1 - 2, g + 4, z1 + 3, p.accent());
        b.fill(x0, g + 6, z0, x1, g + 6, z1, p.roof());
        b.walls(x0, g + 7, z0, x1, g + 7, z1, p.rail());
        b.sign(x0 + 5, g + 5, z1, Direction.SOUTH, "SPICE MARKET");

        // Sacks of chilli, turmeric and dye.
        b.put(x0 + 4, g + 1, z1 + 1, ORANGE);
        b.put(x0 + 5, g + 1, z1 + 1, RED_CLAY);
        b.put(x0 + 6, g + 1, z1 + 1, YELLOW);
        b.put(x0 + 5, g + 2, z1 + 1, ORANGE);
        b.put(x0 + 8, g + 1, z1 + 1, YELLOW);
        b.put(x0 + 9, g + 1, z1 + 1, RED_CLAY);
        b.put(x0 + 12, g + 1, z1 + 1, ORANGE);
        b.put(x0 + 13, g + 1, z1 + 1, YELLOW);
        b.put(x0 + 14, g + 2, z1 + 1, RED_CLAY);
        lamp(b, x0 + 8, g + 1, z1 + 2, p);
    }

    private static void hakimClinic(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 196;
        int x1 = cx - 176;
        int z0 = cz + 100;
        int z1 = cz + 120;
        b.fill(x0, g - 1, z0, x1, g - 1, z1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 6, z1, p.wall());
        b.walls(x0, g + 1, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 5, z1 - 1, AIR);
        // Two rooms divided by an interior wall.
        b.fill(x0 + 1, g + 1, z0 + 10, x1 - 1, g + 5, z0 + 10, p.wall());
        b.fill(x0 + 7, g + 1, z0 + 10, x0 + 8, g + 3, z0 + 10, AIR);
        // West door and sign.
        b.fill(x0, g + 1, z0 + 6, x0, g + 2, z0 + 6, AIR);
        door(b, x0, g + 1, z0 + 6, Direction.WEST, p);
        b.sign(x0, g + 5, z0 + 2, Direction.WEST, "HAKIM", "REMEDIES");
        b.fill(x0, g + 6, z0, x1, g + 6, z1, p.roof());
        b.walls(x0, g + 7, z0, x1, g + 7, z1, p.rail());
        // Shelves of jars in both rooms.
        for (int i = 0; i < 5; i++) {
            b.put(x0 + 2 + i, g + 3, z0 + 2, i % 2 == 0 ? ORANGE : BLUE);
            b.put(x0 + 2 + i, g + 3, z1 - 2, i % 2 == 0 ? YELLOW : RED_CLAY);
        }
        b.put(x0 + 2, g + 1, z0 + 4, p.foundation());
        b.put(x0 + 3, g + 1, z0 + 4, p.foundation());
        b.put(x0 + 2, g + 2, z0 + 4, CAULDRON);
        b.put(x0 + 12, g + 1, z0 + 14, BED);
        lamp(b, x0 + 4, g + 1, z0 + 12, p);
    }

    private static void noticeBoard(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x, g, z - 4, x, g, z + 4, p.foundation());
        b.put(x, g + 1, z - 4, p.accent());
        b.put(x, g + 2, z - 4, p.accent());
        b.put(x, g + 1, z + 4, p.accent());
        b.put(x, g + 2, z + 4, p.accent());
        b.fill(x, g + 1, z - 4, x, g + 4, z + 4, p.frame());
        b.sign(x, g + 3, z - 3, Direction.EAST, "CITY NOTICES");
        b.sign(x, g + 3, z, Direction.EAST, "MISSING BRIDE");
        b.sign(x, g + 3, z + 3, Direction.EAST, "STOLEN RELIC");
        b.fill(x - 1, g + 5, z - 4, x + 1, g + 5, z + 4, p.roofSlab());
        b.put(x - 1, g + 4, z - 4, p.accent());
        b.put(x + 1, g + 4, z + 4, p.accent());
    }

    private static void teaStall(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x, g - 1, z, x + 3, g - 1, z + 3, p.foundation());
        b.fill(x, g, z, x + 3, g, z + 3, p.ground());
        for (int dx = 0; dx <= 3; dx += 3) {
            for (int dz = 0; dz <= 3; dz += 3) {
                b.put(x + dx, g + 1, z + dz, p.accent());
                b.put(x + dx, g + 2, z + dz, p.accent());
            }
        }
        b.fill(x, g + 3, z, x + 3, g + 3, z + 3, p.roofSlab());
        b.put(x + 1, g + 1, z + 1, CAULDRON);
        b.put(x + 2, g + 4, z + 2, p.light());
    }

    private static void collapsedLane(StructureBuilder b, int x0, int z0, int g, Palette p) {
        // A gap in the shop row: broken walls, a fallen beam and rubble.
        int x1 = x0 + 10;
        int z1 = z0 + 15;
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.fill(x0, g + 1, z0, x1, g + 3, z0 + 2, p.weathered());
        b.fill(x0 + 7, g + 1, z1 - 2, x1, g + 4, z1, p.weathered());
        b.fill(x0, g + 1, z0 + 8, x0 + 2, g + 2, z1, p.weathered());
        b.fill(x0 + 3, g + 1, z0 + 4, x0 + 7, g + 1, z0 + 5, p.roofSlab());
        b.fill(x0 + 2, g + 1, z0 + 10, x0 + 6, g + 1, z0 + 12, p.rubble());
        b.put(x0 + 4, g + 2, z0 + 11, p.roof());
        b.put(x0 + 5, g + 2, z0 + 11, p.roof());
        b.put(x0 + 1, g + 1, z0 + 1, p.light());
        b.sign(x0, g + 3, z0 + 12, Direction.WEST, "UNSAFE", "KEEP OUT");
    }

    // ------------------------------------------------------------------ stepwell

    private static void stepwell(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 212;
        int x1 = cx - 188;
        int z0 = cz + 124;
        int z1 = cz + 168;

        b.fill(x0, g - 13, z0, x1, g + 2, z1, AIR);
        b.fill(x0 - 1, g - 13, z0 - 1, x0 - 1, g + 1, z1 + 1, p.wall());
        b.fill(x1 + 1, g - 13, z0 - 1, x1 + 1, g + 1, z1 + 1, p.wall());
        b.fill(x0 - 1, g - 13, z0 - 1, cx - 203, g + 1, z0 - 1, p.wall());
        b.fill(cx - 197, g - 13, z0 - 1, x1 + 1, g + 1, z0 - 1, p.wall());
        b.fill(x0 - 1, g - 13, z1 + 1, x1 + 1, g + 1, z1 + 1, p.wall());
        b.fill(x0, g - 13, z0, x1, g - 13, z1, p.foundation());

        // Eleven walkable one-block tiers descending from the bazaar side.
        for (int i = 0; i < 11; i++) {
            int top = g - i;
            int za = z0 + 3 * i;
            int zb = za + 2;
            b.fill(x0, g - 13, za, x1, top, zb, p.foundation());
            b.fill(x0, top, za, x1, top, zb, p.accent());
        }

        // The pool at the south end and the hidden shrine behind it.
        b.fill(x0 + 2, g - 13, z1 - 11, x1 - 2, g - 11, z1, WATER);
        int wx = cx - 200;
        int sx0 = wx - 10;
        int sx1 = wx + 10;
        int sz0 = z1 + 1;
        int sz1 = z1 + 13;
        int floor = g - 11;
        b.fill(sx0 - 1, floor - 1, sz0 - 1, sx1 + 1, floor - 1, sz1 + 1, p.foundation());
        b.walls(sx0, floor, sz0, sx1, floor + 7, sz1, p.wall());
        b.fill(sx0 + 1, floor, sz0 + 1, sx1 - 1, floor + 6, sz1 - 1, AIR);
        b.fill(sx0, floor + 7, sz0, sx1, floor + 7, sz1, p.foundation());
        b.fill(sx0 + 1, floor, sz0 + 1, sx1 - 1, floor, sz1 - 1, p.accent());
        b.fill(wx - 2, floor, sz0, wx + 2, floor + 3, sz0, AIR);
        // Idol, offering chest and warning.
        b.fill(wx - 2, floor + 1, sz1 - 4, wx + 2, floor + 2, sz1 - 3, p.accent());
        b.put(wx, floor + 3, sz1 - 3, BLUE);
        b.put(wx, floor + 4, sz1 - 3, p.light());
        b.chest(wx - 7, floor + 1, sz1 - 6, SiteLoot.CULT);
        b.put(wx - 9, floor + 6, sz1 - 1, Palette.cobweb());
        b.put(wx + 9, floor + 6, sz1 - 1, Palette.cobweb());
        b.sign(wx - 7, floor + 3, sz0, Direction.NORTH, "HIDDEN SHRINE", "THE BRIDE WEEPS",
                "LEAVE OFFERING");

        // Rim wall, entrance gap, lamps and a chhatri.
        b.fill(x0 - 2, g + 1, z0 - 2, x0 - 2, g + 3, z1 + 2, p.wall());
        b.fill(x1 + 2, g + 1, z0 - 2, x1 + 2, g + 3, z1 + 2, p.wall());
        b.fill(x0 - 2, g + 1, z1 + 2, x1 + 2, g + 3, z1 + 2, p.wall());
        b.fill(x0 - 2, g + 1, z0 - 2, cx - 203, g + 3, z0 - 2, p.wall());
        b.fill(cx - 197, g + 1, z0 - 2, x1 + 2, g + 3, z0 - 2, p.wall());
        b.crenellations(x0 - 2, z0 - 2, x0 - 2, z1 + 2, g + 3);
        b.crenellations(x1 + 2, z0 - 2, x1 + 2, z1 + 2, g + 3);
        b.crenellations(x0 - 2, z1 + 2, x1 + 2, z1 + 2, g + 3);
        b.crenellations(x0 - 2, z0 - 2, cx - 203, z0 - 2, g + 3);
        b.crenellations(cx - 197, z0 - 2, x1 + 2, z0 - 2, g + 3);
        lamp(b, x0 - 1, g + 1, z0 - 1, p);
        lamp(b, x1 + 1, g + 1, z0 - 1, p);
        chhatri(b, cx - 210, cz + 116, g, p);
        signpost(b, cx - 204, g + 1, z0 - 4, Direction.NORTH, p,
                "STEPWELL", "DEEP WATER", "MIND THE STEPS");
    }

    // ------------------------------------------------------------------ red fort

    private static void redFort(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx + 140;
        int x1 = cx + 320;
        int z0 = cz - 260;
        int z1 = cz - 20;

        fortWall(b, x0, z0, x0 + 2, z1, g, p);
        fortWall(b, x1 - 2, z0, x1, z1, g, p);
        fortWall(b, x0, z0, x1, z0 + 2, g, p);
        fortWall(b, x0, z1 - 2, x1, z1, g, p);

        bastion(b, x0 + 1, z0 + 1, g, p);
        bastion(b, x1 - 1, z0 + 1, g, p);
        bastion(b, x0 + 1, z1 - 1, g, p);
        bastion(b, x1 - 1, z1 - 1, g, p);

        gatehouse(b, cx, cz, g, p);
        diwanIAm(b, cx, cz, g, p);

        // Barracks along the east wall.
        barracks(b, cx + 294, cz - 230, g, p);
        barracks(b, cx + 294, cz - 186, g, p);

        // A small charbagh in the north-west quadrant of the fort.
        b.fill(cx + 170, g, cz - 240, cx + 250, g, cz - 170, p.ground());
        b.fill(cx + 180, g, cz - 232, cx + 240, g, cz - 178, GRASS);
        pool(b, cx + 206, cz - 226, cx + 214, cz - 184, g, p);
        for (int z = cz - 228; z <= cz - 182; z += 10) {
            cypress(b, cx + 176, g, z);
            cypress(b, cx + 244, g, z);
        }
        chhatri(b, cx + 180, cz - 236, g, p);
        chhatri(b, cx + 240, cz - 236, g, p);

        // Courtyard lamps and a well.
        for (int z = cz - 200; z <= cz - 40; z += 40) {
            lamp(b, cx + 180, g + 1, z, p);
            lamp(b, cx + 280, g + 1, z, p);
        }
        b.fill(cx + 266, g + 1, cz - 104, cx + 272, g + 1, cz - 98, p.foundation());
        b.fill(cx + 267, g + 2, cz - 103, cx + 271, g + 2, cz - 99, WATER);
        b.walls(cx + 267, g + 2, cz - 103, cx + 271, g + 2, cz - 99, p.accent());

        signpost(b, cx + 150, g + 1, cz - 60, Direction.SOUTH, p, "RED FORT", "LAHORI GATE");
    }

    private static void fortWall(StructureBuilder b, int x0, int z0, int x1, int z1, int g, Palette p) {
        b.fill(x0, g, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0, g + 2, z0, x1, g + 14, z1, p.wall());
        b.fill(x0, g + 8, z0, x1, g + 8, z1, p.accent());
        b.fill(x0, g + 15, z0, x1, g + 15, z1, p.wall());
        b.crenellations(x0, z0, x1, z1, g + 16);
    }

    private static void bastion(StructureBuilder b, int cx, int cz, int g, Palette p) {
        for (int y = g; y <= g + 19; y++) {
            for (int dx = -6; dx <= 6; dx++) {
                for (int dz = -6; dz <= 6; dz++) {
                    if (dx * dx + dz * dz > 36) {
                        continue;
                    }
                    b.put(cx + dx, y, cz + dz, y <= g + 1 ? p.foundation() : p.wall());
                }
            }
        }
        b.fill(cx - 7, g + 20, cz - 7, cx + 7, g + 20, cz + 7, p.accent());
        chhatri(b, cx, cz, g + 20, p);
    }

    private static void gatehouse(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx + 134;
        int x1 = cx + 162;
        int z0 = cz - 96;
        int z1 = cz - 64;
        b.fill(x0, g, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0, g + 2, z0, x1, g + 18, z1, p.wall());
        b.fill(x0, g + 1, cz - 85, x1, g + 12, cz - 75, AIR);
        b.fill(x0, g + 13, cz - 84, x1, g + 13, cz - 76, p.accent());
        // Quartz frame on the west approach.
        b.fill(x0, g + 2, cz - 86, x0, g + 13, cz - 86, p.accent());
        b.fill(x0, g + 2, cz - 74, x0, g + 13, cz - 74, p.accent());
        b.fill(x0, g + 13, cz - 86, x0, g + 13, cz - 74, p.accent());
        b.fill(x0, g + 9, cz - 96, x0, g + 9, cz - 87, p.accent());
        b.fill(x0, g + 9, cz - 73, x0, g + 9, cz - 64, p.accent());
        b.crenellations(x0, z0, x1, z1, g + 19);
        chhatri(b, cx + 140, cz - 88, g + 19, p);
        chhatri(b, cx + 140, cz - 72, g + 19, p);
        b.sign(x0, g + 10, cz - 92, Direction.WEST, "RED FORT", "LAHORI GATE");
    }

    private static void diwanIAm(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx + 196;
        int x1 = cx + 246;
        int z0 = cz - 120;
        int z1 = cz - 80;
        int hz = (z0 + z1) / 2;
        b.fill(x0 - 8, g, z0 - 8, x1 + 8, g, z1 + 8, p.ground());
        b.fill(x0 - 8, g + 1, z0 - 8, x1 + 8, g + 1, z1 + 8, p.accent());
        for (int x = x0; x <= x1; x += 12) {
            for (int z = z0; z <= z1; z += 10) {
                b.fill(x, g + 2, z, x + 1, g + 9, z + 1, p.accent());
                b.put(x, g + 10, z, p.foundation());
            }
        }
        b.fill(x0 - 2, g + 10, z0 - 2, x1 + 2, g + 10, z1 + 2, p.roofSlab());
        b.crenellations(x0 - 2, z0 - 2, x1 + 2, z1 + 2, g + 11);
        b.fill(x1 - 7, g + 2, hz - 6, x1 - 4, g + 4, hz + 6, p.accent());
        b.put(x1 - 6, g + 5, hz, BLUE);
        b.put(x1 - 6, g + 4, hz - 2, p.light());
        b.put(x1 - 6, g + 4, hz + 2, p.light());
        lamp(b, x0 + 2, g + 2, hz - 14, p);
        lamp(b, x0 + 2, g + 2, hz + 14, p);
    }

    private static void barracks(StructureBuilder b, int x0, int z0, int g, Palette p) {
        int x1 = x0 + 16;
        int z1 = z0 + 34;
        b.fill(x0, g - 1, z0, x1, g - 1, z1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 6, z1, p.wall());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 5, z1 - 1, AIR);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 6, 4, 1);
        for (int z = z0 + 6; z <= z1 - 6; z += 10) {
            b.fill(x0, g + 1, z, x0, g + 2, z, AIR);
            door(b, x0, g + 1, z, Direction.WEST, p);
            b.window(x1 - 1, g + 3, z, 2, 2, false);
        }
        b.put(x0 + 2, g + 1, z0 + 2, p.rail());
        b.put(x0 + 2, g + 2, z0 + 2, p.light());
        b.put(x1 - 2, g + 1, z1 - 2, p.rail());
        b.put(x1 - 2, g + 2, z1 - 2, p.light());
    }

    // ------------------------------------------------------------------ railway

    private static void railwayStation(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // Forecourt and station building.
        b.fill(cx + 140, g - 1, cz + 100, cx + 190, g - 1, cz + 140, p.foundation());
        b.fill(cx + 140, g, cz + 100, cx + 190, g, cz + 140, p.ground());
        stationBuilding(b, cx, cz, g, p);
        clockTower(b, cx + 220, cz + 104, g, p);

        // Tracks, platforms and the shed.
        int tz0 = cz + 48;
        int tz1 = cz + 218;
        b.fill(cx + 268, g, tz0, cx + 276, g, tz1, p.rubble());
        for (int z = tz0; z <= tz1; z++) {
            b.put(cx + 270, g + 1, z, RAIL);
            b.put(cx + 274, g + 1, z, RAIL);
        }
        b.fill(cx + 256, g, tz0, cx + 266, g, tz1, p.foundation());
        b.fill(cx + 256, g + 1, tz0, cx + 266, g + 1, tz1, p.accent());
        b.fill(cx + 278, g, tz0, cx + 288, g, tz1, p.foundation());
        b.fill(cx + 278, g + 1, tz0, cx + 288, g + 1, tz1, p.accent());
        for (int z = tz0 + 4; z <= cz + 138; z += 16) {
            b.fill(cx + 258, g + 2, z, cx + 258, g + 7, z, p.accent());
            b.fill(cx + 286, g + 2, z, cx + 286, g + 7, z, p.accent());
            lamp(b, cx + 262, g + 2, z, p);
        }
        b.fill(cx + 254, g + 8, tz0, cx + 290, g + 8, cz + 142, p.roofSlab());
        b.fill(cx + 254, g + 9, tz0, cx + 254, g + 9, cz + 142, p.rail());
        b.fill(cx + 290, g + 9, tz0, cx + 290, g + 9, cz + 142, p.rail());

        train(b, cx, cz, g, p);
        warehouse(b, cx + 292, cz + 58, g, p);
        warehouse(b, cx + 292, cz + 160, g, p);
        telegraph(b, cx + 292, cz + 112, g, p);

        signpost(b, cx + 150, g + 1, cz + 132, Direction.SOUTH, p,
                "RAILWAY STATION", "PLATFORMS 1-2");
        b.sign(cx + 262, g + 2, cz + 120, Direction.WEST, "PLATFORM 1", "DELHI JUNCTION");
    }

    private static void stationBuilding(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx + 190;
        int x1 = cx + 250;
        int z0 = cz + 100;
        int z1 = cz + 140;
        b.fill(x0, g - 1, z0, x1, g - 1, z1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 14, z1, p.wall());
        b.walls(x0, g + 1, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 13, z1 - 1, AIR);
        b.fill(x0, g + 15, z0, x1, g + 15, z1, p.roof());
        b.walls(x0, g + 16, z0, x1, g + 16, z1, p.rail());

        // Arched west entrance.
        archZ(b, cz + 114, cz + 126, g + 1, x0, 9, p);
        b.fill(x0 - 1, g, cz + 112, x0 - 1, g, cz + 128, p.accent());

        // Windows along the west facade and the upper storey.
        for (int z = cz + 104; z <= cz + 136; z += 8) {
            if (z > cz + 112 && z < cz + 128) {
                continue;
            }
            b.window(x0, g + 4, z, 4, 2, true);
        }
        for (int z = cz + 106; z <= cz + 134; z += 10) {
            b.window(x1 - 1, g + 5, z, 3, 2, true);
        }
        b.sign(x0, g + 11, cz + 108, Direction.WEST, "RAILWAY STATION");

        // Ticket counter, benches and lamps.
        b.fill(x0 + 20, g + 1, cz + 102, x0 + 24, g + 2, cz + 112, p.foundation());
        b.fill(x0 + 4, g + 1, cz + 104, x0 + 8, g + 1, cz + 106, p.roofSlab());
        b.fill(x0 + 4, g + 1, cz + 132, x0 + 8, g + 1, cz + 134, p.roofSlab());
        lamp(b, x0 + 10, g + 1, cz + 106, p);
        lamp(b, x0 + 10, g + 1, cz + 134, p);
    }

    private static void clockTower(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 6;
        int x1 = cx + 6;
        int z0 = cz - 6;
        int z1 = cz + 6;
        b.fill(x0, g, z0, x1, g + 1, z1, p.foundation());
        b.walls(x0, g + 2, z0, x1, g + 26, z1, p.wall());
        b.fill(x0 + 1, g + 2, z0 + 1, x1 - 1, g + 25, z1 - 1, AIR);
        b.fill(x0, g + 14, z0, x1, g + 14, z1, p.accent());
        b.fill(x0, g + 27, z0, x1, g + 27, z1, p.roofSlab());
        b.walls(x0, g + 28, z0, x1, g + 28, z1, p.rail());
        spireRoof(b, cx, cz, g + 29, 7, p);
        // Clock face on the west side.
        b.fill(x0, g + 18, cz - 3, x0, g + 24, cz + 3, p.accent());
        b.fill(x0, g + 19, cz - 2, x0, g + 23, cz + 2, BLACK);
        b.put(x0, g + 21, cz, p.accent());
        b.put(x0, g + 22, cz, p.accent());
        b.put(x0, g + 21, cz + 1, p.accent());
    }

    private static void spireRoof(StructureBuilder b, int cx, int cz, int baseY, int height, Palette p) {
        for (int i = 0; i < height; i++) {
            int r = Math.max(0, 4 - i * 4 / height);
            b.fill(cx - r, baseY + i, cz - r, cx + r, baseY + i, cz + r, p.roof());
        }
        b.put(cx, baseY + height, cz, p.accent());
    }

    private static void train(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // Locomotive: boiler, cab, chimney and iron bands, sitting on the rails.
        int y = g + 2;
        b.fill(cx + 269, y, cz + 150, cx + 275, y + 3, cz + 160, BLACK);
        b.fill(cx + 269, y, cz + 160, cx + 275, y + 5, cz + 165, BLACK);
        b.fill(cx + 270, y + 1, cz + 161, cx + 274, y + 4, cz + 164, AIR);
        b.fill(cx + 269, y + 2, cz + 150, cx + 275, y + 2, cz + 160, IRON);
        b.fill(cx + 271, y + 3, cz + 152, cx + 273, y + 6, cz + 154, BLACK);
        b.fill(cx + 271, y + 7, cz + 152, cx + 273, y + 7, cz + 154, IRON);
        b.fill(cx + 270, y + 2, cz + 150, cx + 270, y + 3, cz + 160, IRON);
        b.fill(cx + 274, y + 2, cz + 150, cx + 274, y + 3, cz + 160, IRON);
        b.put(cx + 272, y + 3, cz + 165, p.light());
        b.fill(cx + 269, y - 1, cz + 151, cx + 269, y - 1, cz + 153, IRON);
        b.fill(cx + 275, y - 1, cz + 151, cx + 275, y - 1, cz + 153, IRON);
        b.fill(cx + 269, y - 1, cz + 157, cx + 269, y - 1, cz + 159, IRON);
        b.fill(cx + 275, y - 1, cz + 157, cx + 275, y - 1, cz + 159, IRON);

        // Two carriages.
        for (int i = 0; i < 2; i++) {
            int z0 = cz + 169 + i * 14;
            int z1 = z0 + 9;
            b.fill(cx + 268, y, z0, cx + 276, y + 4, z1, p.wall());
            b.fill(cx + 269, y + 1, z0 + 1, cx + 275, y + 3, z1 - 1, AIR);
            b.fill(cx + 268, y + 4, z0, cx + 276, y + 4, z1, p.roofSlab());
            b.fill(cx + 268, y + 1, z0 + 2, cx + 268, y + 2, z0 + 3, p.window());
            b.fill(cx + 276, y + 1, z0 + 2, cx + 276, y + 2, z0 + 3, p.window());
            b.fill(cx + 268, y + 1, z0 + 6, cx + 268, y + 2, z0 + 7, p.window());
            b.fill(cx + 276, y + 1, z0 + 6, cx + 276, y + 2, z0 + 7, p.window());
            b.fill(cx + 269, y - 1, z0 + 1, cx + 269, y - 1, z0 + 2, IRON);
            b.fill(cx + 275, y - 1, z0 + 1, cx + 275, y - 1, z0 + 2, IRON);
        }
    }

    private static void warehouse(StructureBuilder b, int x0, int z0, int g, Palette p) {
        int x1 = x0 + 26;
        int z1 = z0 + 36;
        b.fill(x0, g - 1, z0, x1, g - 1, z1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 8, z1, p.wall());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 7, z1 - 1, AIR);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 8, 5, 1);
        b.fill(x0, g + 1, z0 + 14, x0, g + 5, z0 + 22, AIR);
        door(b, x0, g + 1, z0 + 14, Direction.WEST, p);
        b.put(x0 + 2, g + 1, z0 + 2, HAY);
        b.put(x0 + 3, g + 1, z0 + 2, HAY);
        b.put(x0 + 2, g + 2, z0 + 2, HAY);
        b.put(x1 - 2, g + 1, z1 - 2, p.foundation());
        b.put(x1 - 3, g + 1, z1 - 2, p.foundation());
        lamp(b, x0 + 4, g + 1, z0 + 4, p);
    }

    private static void telegraph(StructureBuilder b, int x0, int z0, int g, Palette p) {
        int x1 = x0 + 20;
        int z1 = z0 + 14;
        b.fill(x0, g - 1, z0, x1, g - 1, z1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 5, z1, p.wall());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 4, z1 - 1, AIR);
        b.fill(x0, g + 6, z0, x1, g + 6, z1, p.roofSlab());
        b.fill(x0, g + 1, z0 + 6, x0, g + 2, z0 + 6, AIR);
        door(b, x0, g + 1, z0 + 6, Direction.WEST, p);
        b.fill(x0 + 2, g + 1, z0 + 2, x0 + 5, g + 2, z0 + 3, p.foundation());
        // Telegraph pole with crossbars.
        b.fill(x1 + 4, g + 1, z0 + 7, x1 + 4, g + 11, z0 + 7, p.accent());
        b.fill(x1 + 2, g + 10, z0 + 7, x1 + 6, g + 10, z0 + 7, p.accent());
        b.fill(x1 + 3, g + 8, z0 + 7, x1 + 5, g + 8, z0 + 7, p.accent());
        b.put(x1 + 4, g + 12, z0 + 7, p.light());
    }

    // ------------------------------------------------------------------ civil lines

    private static void civilLines(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int[] northX = {cx - 300, cx - 262, cx - 224, cx - 186, cx - 148,
                cx + 112, cx + 150, cx + 188, cx + 226, cx + 264, cx + 302};
        int[] southX = {cx - 300, cx - 262, cx - 224, cx - 186, cx - 148,
                cx + 112, cx + 150, cx + 188, cx + 226, cx + 264, cx + 302};
        for (int i = 0; i < northX.length; i++) {
            bungalow(b, northX[i], cz + 222, g, p, Direction.SOUTH, i % 3);
            bungalow(b, southX[i], cz + 262, g, p, Direction.NORTH, (i + 1) % 3);
        }
        club(b, cx - 100, cz + 218, g, p);
        church(b, cx + 30, cz + 260, g, p);
        // A small park between the axis and the church.
        b.fill(cx - 40, g, cz + 258, cx - 12, g, cz + 278, GRASS);
        for (int i = 0; i < 4; i++) {
            neem(b, cx - 36 + i * 7, g, cz + 262 + i % 2 * 10);
        }
        b.fill(cx - 38, g, cz + 268, cx - 14, g, cz + 268, p.ground());
        b.put(cx - 34, g + 1, cz + 266, p.roofSlab());
        b.put(cx - 20, g + 1, cz + 270, p.roofSlab());

        // Avenue trees along the civil lines road.
        for (int x = cx - 310; x <= cx + 310; x += 20) {
            if (Math.abs(x - cx) < 12 || x > cx - 106 && x < cx - 58) {
                continue;
            }
            cypress(b, x, g, cz + 242);
            cypress(b, x, g, cz + 256);
        }
        signpost(b, cx - 60, g + 1, cz + 244, Direction.SOUTH, p, "CIVIL LINES");
        signpost(b, cx + 60, g + 1, cz + 244, Direction.SOUTH, p, "CIVIL LINES", "CHURCH");
    }

    private static void bungalow(StructureBuilder b, int x0, int z0, int g, Palette p,
                                 Direction front, int variant) {
        int x1 = x0 + 17;
        int z1 = z0 + 13;
        b.fill(x0, g - 1, z0, x1, g - 1, z1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        // Hedge around the plot, with a gate on the road side.
        b.walls(x0, g + 1, z0, x1, g + 2, z1, p.weathered());
        int gateX = (x0 + x1) / 2;
        if (front == Direction.SOUTH) {
            b.fill(gateX - 1, g + 1, z1, gateX + 1, g + 2, z1, AIR);
        } else {
            b.fill(gateX - 1, g + 1, z0, gateX + 1, g + 2, z0, AIR);
        }

        int bx0 = x0 + 4;
        int bx1 = x1 - 4;
        int bz0 = z0 + 3;
        int bz1 = z1 - 3;
        b.walls(bx0, g + 1, bz0, bx1, g + 5, bz1, p.wall());
        b.fill(bx0 + 1, g + 1, bz0 + 1, bx1 - 1, g + 4, bz1 - 1, AIR);
        b.fill(bx0, g, bz0, bx1, g, bz1, p.ground());
        b.walls(bx0, g + 1, bz0, bx1, g + 1, bz1, p.foundation());

        int roofH = 3 + variant % 2;
        b.pitchedRoof(bx0 - 2, bz0 - 2, bx1 + 2, bz1 + 2, g + 5, roofH, variant % 2);
        // Verandah columns on the road side.
        int vz = front == Direction.SOUTH ? bz1 + 2 : bz0 - 2;
        b.put(bx0, g + 1, vz, p.accent());
        b.put(bx0, g + 2, vz, p.accent());
        b.put(bx0, g + 3, vz, p.accent());
        b.put(bx1, g + 1, vz, p.accent());
        b.put(bx1, g + 2, vz, p.accent());
        b.put(bx1, g + 3, vz, p.accent());
        b.put(gateX, g + 1, vz, p.accent());
        b.put(gateX, g + 2, vz, p.accent());
        b.put(gateX, g + 3, vz, p.accent());

        int doorZ = front == Direction.SOUTH ? bz1 : bz0;
        b.fill(gateX, g + 1, doorZ, gateX, g + 2, doorZ, AIR);
        door(b, gateX, g + 1, doorZ, front, p);
        b.window(bx0, g + 3, (bz0 + bz1) / 2, 2, 2, false);
        b.window(bx1 - 1, g + 3, (bz0 + bz1) / 2, 2, 2, false);
        b.put(bx0 + 1, g + 4, bz0 + 1, p.light());
        neem(b, x0 + 2, g, z0 + 2);
        neem(b, x1 - 2, g, z1 - 2);
    }

    private static void club(StructureBuilder b, int x0, int z0, int g, Palette p) {
        int x1 = x0 + 36;
        int z1 = z0 + 22;
        b.fill(x0, g - 1, z0, x1, g - 1, z1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 7, z1, p.wall());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 6, z1 - 1, AIR);
        b.fill(x0, g + 8, z0, x1, g + 8, z1, p.roof());
        b.walls(x0, g + 9, z0, x1, g + 9, z1, p.rail());
        // Verandah on the south front.
        b.fill(x0 + 2, g + 5, z1 + 1, x1 - 2, g + 5, z1 + 4, p.roofSlab());
        for (int x = x0 + 3; x <= x1 - 3; x += 8) {
            b.put(x, g + 1, z1 + 4, p.accent());
            b.put(x, g + 2, z1 + 4, p.accent());
            b.put(x, g + 3, z1 + 4, p.accent());
            b.put(x, g + 4, z1 + 4, p.accent());
        }
        // Entrance, bar and library.
        b.fill(x0 + 17, g + 1, z1, x0 + 19, g + 3, z1, AIR);
        door(b, x0 + 17, g + 1, z1, Direction.SOUTH, p);
        b.fill(x0 + 2, g + 1, z0 + 2, x0 + 12, g + 2, z0 + 3, p.foundation());
        b.put(x0 + 3, g + 3, z0 + 2, p.light());
        b.put(x0 + 11, g + 3, z0 + 2, p.light());
        b.fill(x1 - 12, g + 1, z0 + 2, x1 - 2, g + 3, z0 + 2, BOOKS);
        b.fill(x0 + 4, g + 1, z0 + 10, x0 + 8, g + 1, z0 + 12, p.roofSlab());
        b.fill(x0 + 4, g + 1, z1 - 8, x0 + 8, g + 1, z1 - 6, p.roofSlab());
        lamp(b, x0 + 6, g + 3, z0 + 2, p);
        b.sign(x0 + 18, g + 6, z1, Direction.SOUTH, "DELHI CLUB");
        neem(b, x0 - 3, g, z1 + 2);
        neem(b, x1 + 3, g, z1 + 2);
    }

    private static void church(StructureBuilder b, int x0, int z0, int g, Palette p) {
        int x1 = x0 + 40;
        int z1 = z0 + 24;
        b.fill(x0, g - 1, z0, x1, g - 1, z1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        int nx0 = x0 + 8;
        int nx1 = x1 - 8;
        b.walls(nx0, g + 1, z0 + 2, nx1, g + 9, z1 - 2, p.wall());
        b.fill(nx0 + 1, g + 1, z0 + 3, nx1 - 1, g + 8, z1 - 3, AIR);
        b.fill(nx0, g, z0 + 2, nx1, g, z1 - 2, p.ground());
        b.pitchedRoof(nx0 - 1, z0 + 1, nx1 + 1, z1 - 1, g + 9, 6, 1);

        // Arched windows down the nave.
        for (int z = z0 + 6; z <= z1 - 6; z += 6) {
            b.window(nx0, g + 4, z, 4, 2, true);
            b.window(nx1 - 1, g + 4, z, 4, 2, true);
        }

        // North tower with a spire and a cross.
        int tx = x0 + 14;
        int tz = z0 + 4;
        b.fill(tx - 4, g, tz - 4, tx + 4, g + 1, tz + 4, p.foundation());
        b.walls(tx - 4, g + 1, tz - 4, tx + 4, g + 24, tz + 4, p.accent());
        b.fill(tx - 3, g + 1, tz - 3, tx + 3, g + 23, tz + 3, AIR);
        b.fill(tx - 1, g + 1, tz + 4, tx + 1, g + 3, tz + 4, AIR);
        b.fill(tx - 4, g + 18, tz - 4, tx + 4, g + 18, tz + 4, p.wall());
        b.fill(tx - 2, g + 8, tz - 4, tx + 2, g + 12, tz - 4, p.window());
        spireRoof(b, tx, tz, g + 25, 9, p);
        b.fill(tx, g + 34, tz, tx, g + 37, tz, p.accent());
        b.fill(tx - 2, g + 35, tz, tx + 2, g + 35, tz, p.accent());

        // Door on the north face and the church sign.
        b.fill(tx, g + 1, z0, tx, g + 2, z0, AIR);
        door(b, tx, g + 1, z0, Direction.NORTH, p);
        b.sign(tx, g + 8, z0, Direction.NORTH, "ST JAMES CHURCH");
        lamp(b, tx - 3, g + 6, z0, p);
        lamp(b, tx + 3, g + 6, z0, p);

        // A small graveyard east of the nave.
        for (int i = 0; i < 5; i++) {
            int gx = x1 - 4;
            int gz = z0 + 6 + i * 3;
            b.put(gx, g + 1, gz, p.roofSlab());
            b.put(gx, g + 2, gz, p.accent());
            cypress(b, gx + 2, g, gz);
        }
    }

    // ------------------------------------------------------------------ outskirts

    private static void outskirts(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // A maidan south of the bazaar, with a ruined nameless tomb.
        b.fill(cx - 210, g, cz + 190, cx - 150, g, cz + 220, GRASS);
        namelessTomb(b, cx - 180, cz + 206, g, p);
        for (int i = 0; i < 3; i++) {
            neem(b, cx - 208, g, cz + 192 + i * 10);
            neem(b, cx - 152, g, cz + 192 + i * 10);
        }
        neem(b, cx - 186, g, cz + 190);
        neem(b, cx - 174, g, cz + 190);

        // A ruined serai between the bazaar lanes.
        ruinedSerai(b, cx - 180, cz - 72, g, p);

        // The strip between the garden and the axis: grass and a dry well.
        b.fill(cx - 132, g, cz - 76, cx - 16, g, cz - 48, GRASS);
        b.fill(cx - 90, g + 1, cz - 60, cx - 86, g + 1, cz - 56, p.foundation());
        b.fill(cx - 89, g, cz - 59, cx - 87, g, cz - 57, p.ground());
        b.put(cx - 88, g + 1, cz - 58, p.light());
        for (int i = 0; i < 4; i++) {
            cypress(b, cx - 124 + i * 30, g, cz - 52);
        }

        // A ruined idgah (prayer wall) south of the bazaar.
        int ix0 = cx - 300;
        int ix1 = cx - 260;
        int iz0 = cz + 204;
        int iz1 = cz + 220;
        b.fill(ix0, g, iz0, ix1, g, iz1, GRASS);
        b.fill(ix0, g + 1, iz1 - 4, ix1, g + 5, iz1 - 4, p.weathered());
        b.fill(ix0, g + 1, iz1 - 4, ix0, g + 5, iz1, p.weathered());
        b.fill(ix1, g + 1, iz1 - 4, ix1, g + 5, iz1, p.weathered());
        b.fill(cx - 282, g + 1, iz1 - 4, cx - 278, g + 4, iz1 - 4, p.accent());
        b.fill(cx - 281, g + 1, iz1 - 4, cx - 279, g + 3, iz1 - 4, AIR);
        b.put(cx - 280, g + 5, iz1 - 4, p.accent());
        b.sign(cx - 280, g + 1, iz1 + 2, Direction.SOUTH, "IDGAH", "PRAYER WALL");
        cypress(b, ix0, g, iz0 - 2);
        cypress(b, ix1, g, iz0 - 2);

        // Trees along the bazaar's outer edge, clear of its lanes.
        for (int z = cz - 240; z <= cz + 180; z += 30) {
            if (Math.abs(z - (cz - 80)) < 8 || Math.abs(z - (cz + 60)) < 8) {
                continue;
            }
            neem(b, cx - 300, g, z);
        }
    }

    private static void namelessTomb(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx - 12, g, cz - 12, cx + 12, g, cz + 12, p.ground());
        b.fill(cx - 9, g + 1, cz - 9, cx + 9, g + 2, cz + 9, p.foundation());
        b.walls(cx - 7, g + 3, cz - 7, cx + 7, g + 9, cz + 7, p.weathered());
        // Collapsed corner and roof.
        b.fill(cx + 4, g + 3, cz - 7, cx + 7, g + 8, cz - 3, AIR);
        b.fill(cx - 6, g + 3, cz + 7, cx - 2, g + 5, cz + 7, AIR);
        domeCap(b, cx, cz, g + 10, 6, p);
        b.fill(cx - 5, g + 1, cz - 5, cx + 5, g + 1, cz + 5, p.rubble());
        b.put(cx - 3, g + 2, cz, p.roofSlab());
        b.put(cx + 2, g + 2, cz + 2, p.roofSlab());
        cypress(b, cx - 14, g, cz - 12);
        cypress(b, cx + 14, g, cz + 12);
    }

    private static void ruinedSerai(StructureBuilder b, int x0, int z0, int g, Palette p) {
        int x1 = x0 + 34;
        int z1 = z0 + 24;
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 5, z1, p.weathered());
        // Breach the enclosure in two places.
        b.fill(x0 + 10, g + 1, z0, x0 + 18, g + 5, z0, AIR);
        b.fill(x1, g + 1, z0 + 8, x1, g + 5, z0 + 16, AIR);
        b.fill(x0 + 4, g + 1, z1 - 3, x0 + 12, g + 1, z1 - 1, p.rubble());
        b.fill(x0 + 20, g + 1, z0 + 2, x0 + 24, g + 1, z0 + 4, p.rubble());
        b.fill(x0 + 6, g + 1, z0 + 10, x0 + 10, g + 3, z0 + 10, p.wall());
        b.put(x0 + 8, g + 4, z0 + 10, p.roof());
        b.put(x0 + 8, g + 3, z0 + 10, p.light());
        b.put(x0 + 28, g + 1, z0 + 18, p.foundation());
        b.put(x0 + 29, g + 1, z0 + 18, p.foundation());
        b.put(x0 + 28, g + 2, z0 + 18, p.foundation());
        cypress(b, x0 - 2, g, z1 + 2);
    }

    // ------------------------------------------------------------------ shared forms

    private static void signpost(StructureBuilder b, int x, int y, int z, Direction facing,
                                 Palette p, String... lines) {
        b.put(x, y - 1, z, p.foundation());
        b.sign(x, y, z, facing, lines);
    }

    private static void lamp(StructureBuilder b, int x, int y, int z, Palette p) {
        b.put(x, y, z, p.rail());
        b.put(x, y + 1, z, p.light());
    }

    private static void door(StructureBuilder b, int x, int y, int z, Direction facing, Palette p) {
        BlockState lower = p.door()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        b.put(x, y, z, lower);
        b.put(x, y + 1, z,
                lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
    }

    private static void chhatri(StructureBuilder b, int cx, int cz, int baseY, Palette p) {
        int r = 2;
        for (int y = baseY + 1; y <= baseY + 4; y++) {
            b.put(cx - r, y, cz - r, p.accent());
            b.put(cx + r, y, cz - r, p.accent());
            b.put(cx - r, y, cz + r, p.accent());
            b.put(cx + r, y, cz + r, p.accent());
        }
        b.fill(cx - r, baseY + 5, cz - r, cx + r, baseY + 5, cz + r, p.foundation());
        domeCap(b, cx, cz, baseY + 6, 2, p);
    }

    private static void domeCap(StructureBuilder b, int cx, int cz, int baseY, int radius,
                                Palette p) {
        for (int i = 0; i <= radius; i++) {
            int r = Math.max(0, radius - i);
            b.fill(cx - r, baseY + i, cz - r, cx + r, baseY + i, cz + r, p.roof());
            if (r > 0) {
                b.put(cx - r, baseY + i, cz, p.accent());
                b.put(cx + r, baseY + i, cz, p.accent());
                b.put(cx, baseY + i, cz - r, p.accent());
                b.put(cx, baseY + i, cz + r, p.accent());
            }
        }
        b.put(cx, baseY + radius + 1, cz, p.accent());
    }

    private static void cypress(StructureBuilder b, int x, int y, int z) {
        b.put(x, y, z, TRUNK);
        for (int i = 1; i <= 8; i++) {
            int r = i >= 6 ? 1 : 0;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    b.put(x + dx, y + i, z + dz, LEAF);
                }
            }
        }
        b.put(x, y + 9, z, LEAF);
    }

    private static void neem(StructureBuilder b, int x, int y, int z) {
        b.fill(x, y, z, x, y + 2, z, TRUNK);
        b.fill(x - 2, y + 3, z - 2, x + 2, y + 3, z + 2, LEAF);
        b.fill(x - 1, y + 4, z - 1, x + 1, y + 4, z + 1, LEAF);
        b.put(x, y + 5, z, LEAF);
        b.put(x - 3, y + 3, z, LEAF);
        b.put(x + 3, y + 3, z, LEAF);
        b.put(x, y + 3, z - 3, LEAF);
        b.put(x, y + 3, z + 3, LEAF);
    }

    private static void archX(StructureBuilder b, int x0, int x1, int y0, int z, int h, Palette p) {
        for (int y = 0; y < h; y++) {
            int inset = y >= h - 3 ? y - (h - 4) : 0;
            for (int x = x0 + inset; x <= x1 - inset; x++) {
                b.put(x, y0 + y, z, AIR);
            }
            if (inset > 0) {
                b.put(x0 + inset - 1, y0 + y, z, p.accent());
                b.put(x1 - inset + 1, y0 + y, z, p.accent());
            }
        }
        for (int x = x0; x <= x1; x++) {
            b.put(x, y0 + h, z, p.accent());
        }
    }

    private static void archZ(StructureBuilder b, int z0, int z1, int y0, int x, int h, Palette p) {
        for (int y = 0; y < h; y++) {
            int inset = y >= h - 3 ? y - (h - 4) : 0;
            for (int z = z0 + inset; z <= z1 - inset; z++) {
                b.put(x, y0 + y, z, AIR);
            }
            if (inset > 0) {
                b.put(x, y0 + y, z0 + inset - 1, p.accent());
                b.put(x, y0 + y, z1 - inset + 1, p.accent());
            }
        }
        for (int z = z0; z <= z1; z++) {
            b.put(x, y0 + h, z, p.accent());
        }
    }
}
