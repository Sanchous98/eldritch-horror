package com.sanchous98.eldritchhorror.world.loc.city.plan;

import net.minecraft.world.item.DyeColor;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.SiteLoot;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.city.CitySurface;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.RailShape;

/**
 * Cairo — the authored colonial-era city: the great mosque with its dome and two minarets, the
 * covered lanes of Khan el-Khalili, the Nile corniche with its quay and feluccas, the Citadel,
 * the City of the Dead, and the European quarter between them.
 *
 * <p>Every block is written deliberately through {@link StructureBuilder}; there is no layout RNG
 * and no chunk-local read, so every chunk that overlaps the city writes the same blocks. The
 * shared generator has already levelled and paved the district; this plan lays its own plinths,
 * streets, gardens and water on top of that surface.
 */
public final class CairoPlan implements CityPlan {

    /** Authored half-extent: 680 blocks across, matching the brief. */
    private static final int HALF_EXTENT = 340;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState PALM_LOG = Blocks.JUNGLE_LOG.defaultBlockState();
    private static final BlockState PALM_LEAF = Blocks.JUNGLE_LEAVES.defaultBlockState();
    private static final BlockState HAY = Blocks.HAY_BLOCK.defaultBlockState();
    private static final BlockState BED = Blocks.WOOL.pick(DyeColor.RED).defaultBlockState();
    private static final BlockState BOOKS = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState CAULDRON = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState TERRACOTTA = Blocks.DYED_TERRACOTTA.pick(DyeColor.ORANGE).defaultBlockState();
    private static final BlockState RED_CLAY = Blocks.DYED_TERRACOTTA.pick(DyeColor.RED).defaultBlockState();
    private static final BlockState BLUE_CLAY = Blocks.DYED_TERRACOTTA.pick(DyeColor.BLUE).defaultBlockState();
    private static final BlockState YELLOW_CLAY = Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW).defaultBlockState();
    private static final BlockState IRON = Blocks.IRON_BLOCK.defaultBlockState();
    private static final BlockState WHITE = Blocks.WOOL.pick(DyeColor.WHITE).defaultBlockState();
    private static final BlockState WHITE_CARPET = Blocks.CARPET.pick(DyeColor.WHITE).defaultBlockState();
    private static final BlockState DARK_PLANK = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private static final BlockState DARK_SLAB = Blocks.DARK_OAK_SLAB.defaultBlockState();
    private static final BlockState RAIL = Blocks.RAIL.defaultBlockState()
            .setValue(RailBlock.SHAPE, RailShape.EAST_WEST);

    @Override
    public String cityId() {
        return "cairo";
    }

    @Override
    public int halfExtent() {
        return HALF_EXTENT;
    }

    @Override
    public void build(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        streets(b, cx, cz, ground, p);
        arrivalGate(b, cx, cz, ground, p);
        greatMosque(b, cx, cz, ground, p);
        khanElKhalili(b, cx, cz, ground, p);
        nileCorniche(b, cx, cz, ground, p);
        citadel(b, cx, cz, ground, p);
        cityOfTheDead(b, cx, cz, ground, p);
        europeanQuarter(b, cx, cz, ground, p);
        gardens(b, cx, cz, ground, p);
    }

    @Override
    public void build(StructureBuilder b, CityPlanContext ctx, Palette p) {
        build(b, ctx.cx(), ctx.cz(), ctx.ground(), p);
        railStation(b, ctx.cx(), ctx.cz(), p, ctx.surface());
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.LANDMARK, 0, 0, 318, "cairo"),
                new CityPoi("arrival_sign", CityPoi.Kind.SIGN, 20, 1, 304, "districts"),
                new CityPoi("great_mosque", CityPoi.Kind.LANDMARK, 0, 0, -200, "great_mosque"),
                new CityPoi("mosque_imam", CityPoi.Kind.NPC, 0, 0, -190, "imam"),
                new CityPoi("oriental_hotel", CityPoi.Kind.INN, -80, 0, -120, "hotel_keeper"),
                new CityPoi("hotel_keeper", CityPoi.Kind.NPC, -80, 0, -114, "hotel_keeper"),
                new CityPoi("bazaar_quest_board", CityPoi.Kind.QUEST_BOARD, -220, 0, 0,
                        "cairo_stolen_papyrus;cairo_missing_archaeologist"),
                new CityPoi("papyrus_shop", CityPoi.Kind.SHOP, -240, 0, -60, "papyrus_merchant"),
                new CityPoi("merchant", CityPoi.Kind.NPC, -240, 0, -54, "merchant"),
                new CityPoi("papyrus_dealer", CityPoi.Kind.NPC, -260, 0, -120,
                        "cairo_stolen_papyrus"),
                new CityPoi("tea_house", CityPoi.Kind.INN, -200, 0, 30, "tea_house"),
                new CityPoi("hakim", CityPoi.Kind.HEALER, -240, 0, 120, "hakim"),
                new CityPoi("hakim_npc", CityPoi.Kind.NPC, -240, 0, 126, "hakim"),
                new CityPoi("museum", CityPoi.Kind.LANDMARK, 205, 0, -120, "cairo_museum"),
                new CityPoi("museum_curator", CityPoi.Kind.NPC, 220, 0, -120,
                        "cairo_missing_archaeologist"),
                new CityPoi("nile_quay", CityPoi.Kind.LANDMARK, 220, 0, 60, "feluccas;customs"),
                new CityPoi("ferryman", CityPoi.Kind.NPC, 246, 0, 60, "ferryman"),
                new CityPoi("citadel_gate", CityPoi.Kind.LANDMARK, 160, 0, 220, "citadel"),
                new CityPoi("guard", CityPoi.Kind.NPC, 170, 0, 220, "guard"),
                new CityPoi("city_of_the_dead", CityPoi.Kind.SECRET, -200, 0, 260, "necropolis"),
                new CityPoi("secret_tomb", CityPoi.Kind.SECRET, -212, -7, 278, "scribe_tomb"),
                new CityPoi("rail_station", CityPoi.Kind.STATION, 136, 0, 316, "rail_station"),
                new CityPoi("nile_port", CityPoi.Kind.PORT, 252, 0, 60, "feluccas;customs"),
                new CityPoi("european_square", CityPoi.Kind.LANDMARK, 0, 0, 0, "fountain"),
                new CityPoi("all_saints_church", CityPoi.Kind.LANDMARK, -75, 0, 118, "church"),
                new CityPoi("city_notices", CityPoi.Kind.SIGN, -226, 1, 14, "quests"));
    }

    // ------------------------------------------------------------------ streets

    private static void streets(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // Processional axis from the mosque gate to the arrival gate, and the great cross road.
        CityPlanSupport.path(b, cx, cz - 140, cx, cz + 322, 14, g, p.ground());
        CityPlanSupport.path(b, cx - 330, cz, cx + 224, cz, 14, g, p.ground());
        // Nile corniche (12 wide) and the citadel boulevard (10 wide).
        CityPlanSupport.path(b, cx + 224, cz - 250, cx + 224, cz + 170, 12, g, p.ground());
        CityPlanSupport.path(b, cx + 140, cz, cx + 140, cz + 222, 10, g, p.ground());
        CityPlanSupport.path(b, cx + 140, cz + 222, cx + 152, cz + 222, 10, g, p.ground());
        CityPlanSupport.path(b, cx + 140, cz + 60, cx + 252, cz + 60, 10, g, p.ground());
        // Hotel approach off the main axis.
        CityPlanSupport.path(b, cx - 52, cz - 112, cx - 7, cz - 112, 6, g, p.ground());
        // Khan el-Khalili: three north-south lanes and six cross lanes, all 5 wide.
        CityPlanSupport.path(b, cx - 220, cz - 240, cx - 220, cz + 200, 5, g, p.ground());
        CityPlanSupport.path(b, cx - 280, cz - 240, cx - 280, cz + 200, 5, g, p.ground());
        CityPlanSupport.path(b, cx - 160, cz - 240, cx - 160, cz + 200, 5, g, p.ground());
        for (int z : new int[]{-180, -120, -60, 60, 120, 180}) {
            CityPlanSupport.path(b, cx - 320, cz + z, cx - 140, cz + z, 5, g, p.ground());
        }
        // European quarter side streets (8 wide).
        CityPlanSupport.path(b, cx - 120, cz + 80, cx + 120, cz + 80, 8, g, p.ground());
        CityPlanSupport.path(b, cx - 70, cz - 60, cx - 70, cz + 80, 8, g, p.ground());
        CityPlanSupport.path(b, cx + 70, cz - 60, cx + 70, cz + 80, 8, g, p.ground());
        // City of the Dead: one long tomb lane and six 4-wide cross lanes.
        CityPlanSupport.path(b, cx - 310, cz + 260, cx + 110, cz + 260, 4, g, p.ground());
        for (int x : new int[]{-260, -200, -140, -80, 40, 100}) {
            CityPlanSupport.path(b, cx + x, cz + 232, cx + x, cz + 320, 4, g, p.ground());
        }
    }

    // ------------------------------------------------------------------ arrival

    private static void arrivalGate(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // A paved apron outside the southern gate.
        b.fill(cx - 22, g - 1, cz + 304, cx + 22, g - 1, cz + 332, p.foundation());
        b.fill(cx - 22, g, cz + 304, cx + 22, g, cz + 332, p.ground());

        // Two great pylons and the lintel across the 17-wide passage.
        pylon(b, cx - 16, cx - 9, cz + 314, cz + 324, g, p);
        pylon(b, cx + 9, cx + 16, cz + 314, cz + 324, g, p);
        b.fill(cx - 16, g + 15, cz + 314, cx + 16, g + 17, cz + 324, p.wall());
        b.fill(cx - 16, g + 15, cz + 314, cx + 16, g + 15, cz + 324, p.accent());
        b.crenellations(cx - 16, cz + 314, cx + 16, cz + 324, g + 18);

        // Pointed arch frame on the southern face.
        b.fill(cx - 9, g + 1, cz + 324, cx - 9, g + 13, cz + 324, p.accent());
        b.fill(cx + 9, g + 1, cz + 324, cx + 9, g + 13, cz + 324, p.accent());
        b.fill(cx - 9, g + 13, cz + 324, cx + 9, g + 13, cz + 324, p.accent());
        b.fill(cx - 6, g + 13, cz + 324, cx + 6, g + 14, cz + 324, p.accent());

        // Low flanking walls, crenellated, and lamps on the pylons.
        b.fill(cx - 22, g + 1, cz + 314, cx - 17, g + 3, cz + 324, p.weathered());
        b.fill(cx + 17, g + 1, cz + 314, cx + 22, g + 3, cz + 324, p.weathered());
        b.crenellations(cx - 22, cz + 314, cx - 17, cz + 324, g + 4);
        b.crenellations(cx + 17, cz + 314, cx + 22, cz + 324, g + 4);
        lamp(b, cx - 12, g + 13, cz + 324, p);
        lamp(b, cx + 12, g + 13, cz + 324, p);

        // Gate name and the district signposts.
        b.sign(cx, g + 16, cz + 324, Direction.SOUTH, "CAIRO");
        signpost(b, cx + 20, g + 1, cz + 304, Direction.SOUTH, p,
                "GREAT MOSQUE", "KHAN EL-KHALILI", "NILE", "CITADEL");
        signpost(b, cx - 20, g + 1, cz + 304, Direction.SOUTH, p,
                "CITY OF THE", "DEAD", "CITY NOTICES", "TRAVEL SAFE");
    }

    private static void pylon(StructureBuilder b, int x0, int x1, int z0, int z1, int g,
                              Palette p) {
        b.fill(x0, g, z0, x1, g + 14, z1, p.wall());
        b.fill(x0, g, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0, g + 5, z0, x1, g + 5, z1, p.accent());
        b.fill(x0, g + 11, z0, x1, g + 11, z1, p.accent());
        b.fill(x0 + 1, g + 2, z0, x0 + 1, g + 10, z1, p.weathered());
    }

    // ------------------------------------------------------------------ mosque

    private static void greatMosque(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 58;
        int x1 = cx + 58;
        int z0 = cz - 264;
        int z1 = cz - 140;

        // Precinct paving, curtain wall and crenellations.
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 4, z1, p.wall());
        b.fill(x0, g + 1, z0, x1, g + 1, z1, p.foundation());
        b.crenellations(x0, z0, x1, z1, g + 5);

        // South gate through the precinct wall, with a tall pointed portal.
        b.fill(cx - 8, g + 1, z1 - 1, cx + 8, g + 12, z1 + 1, AIR);
        b.fill(cx - 12, g + 1, cz - 142, cx + 12, g + 20, cz - 138, p.wall());
        b.fill(cx - 8, g + 1, cz - 142, cx + 8, g + 12, cz - 138, AIR);
        b.fill(cx - 12, g + 1, cz - 138, cx - 10, g + 14, cz - 138, p.accent());
        b.fill(cx + 10, g + 1, cz - 138, cx + 12, g + 14, cz - 138, p.accent());
        b.fill(cx - 12, g + 14, cz - 138, cx + 12, g + 14, cz - 138, p.accent());
        b.fill(cx - 7, g + 14, cz - 138, cx + 7, g + 15, cz - 138, p.accent());
        b.crenellations(cx - 12, cz - 142, cx + 12, cz - 138, g + 21);
        b.sign(cx, g + 14, cz - 138, Direction.SOUTH, "GREAT MOSQUE", "AL-AZHAR");

        // Prayer hall: a hypostyle box under a flat roof.
        int hx0 = cx - 44;
        int hx1 = cx + 44;
        int hz0 = cz - 260;
        int hz1 = cz - 224;
        b.fill(hx0, g, hz0, hx1, g, hz1, p.foundation());
        b.walls(hx0, g + 1, hz0, hx1, g + 22, hz1, p.wall());
        b.fill(hx0 + 1, g + 1, hz0 + 1, hx1 - 1, g + 21, hz1 - 1, AIR);
        b.fill(hx0, g + 23, hz0, hx1, g + 23, hz1, p.roof());
        b.fill(hx0, g + 9, hz0, hx0, g + 9, hz1, p.accent());
        b.fill(hx1, g + 9, hz0, hx1, g + 9, hz1, p.accent());
        b.fill(hx0, g + 9, hz0, hx1, g + 9, hz0, p.accent());
        b.fill(hx0, g + 9, hz1, hx1, g + 9, hz1, p.accent());
        for (int x = hx0 + 12; x <= hx1 - 12; x += 12) {
            for (int z = hz0 + 6; z <= hz1 - 6; z += 8) {
                b.fill(x, g + 1, z, x, g + 20, z, p.accent());
                b.put(x, g + 21, z, p.roofSlab());
            }
        }

        // The south iwan of the hall opens onto the courtyard.
        b.fill(cx - 6, g + 1, hz1 - 1, cx + 6, g + 12, hz1 + 1, AIR);
        b.fill(cx - 9, g + 1, hz1, cx - 7, g + 14, hz1, p.accent());
        b.fill(cx + 7, g + 1, hz1, cx + 9, g + 14, hz1, p.accent());
        b.fill(cx - 9, g + 14, hz1, cx + 9, g + 14, hz1, p.accent());
        b.fill(cx - 5, g + 14, hz1, cx + 5, g + 15, hz1, p.accent());
        b.fill(cx - 10, g + 1, hz1 + 1, cx + 10, g + 16, hz1 + 3, p.wall());
        b.fill(cx - 6, g + 1, hz1 + 1, cx + 6, g + 12, hz1 + 3, AIR);
        b.fill(cx - 6, g + 13, hz1 + 3, cx + 6, g + 13, hz1 + 3, p.accent());
        b.crenellations(cx - 10, hz1 + 1, cx + 10, hz1 + 3, g + 17);

        // The great dome on its drum.
        b.fill(cx - 18, g + 23, cz - 260, cx + 18, g + 24, cz - 224, p.accent());
        onionDome(b, cx, cz - 242, g + 25, p);

        // Two slender minarets framing the hall.
        minaret(b, cx - 50, cz - 252, g, p);
        minaret(b, cx + 50, cz - 252, g, p);

        // Arcaded courtyard walks down the long sides.
        arcade(b, cx - 56, cz - 220, cz - 146, g, p);
        arcade(b, cx + 56, cz - 220, cz - 146, g, p);

        // The ablution fountain under a small canopy, in the courtyard.
        fountain(b, cx, cz - 184, g, p);
        for (int dx = -3; dx <= 3; dx += 6) {
            for (int dz = -3; dz <= 3; dz += 6) {
                b.fill(cx + dx, g + 2, cz - 184 + dz, cx + dx, g + 6, cz - 184 + dz, p.accent());
            }
        }
        b.fill(cx - 4, g + 7, cz - 188, cx + 4, g + 7, cz - 180, p.roofSlab());
        dome(b, cx, cz - 184, g + 8, 3, p);
        signpost(b, cx + 6, g + 1, cz - 180, Direction.SOUTH, p, "WASH FIRST");

        // Courtyard palms and lamps.
        palm(b, cx - 48, g, cz - 152);
        palm(b, cx + 48, g, cz - 152);
        palm(b, cx - 48, g, cz - 212);
        palm(b, cx + 48, g, cz - 212);
        lamp(b, cx - 54, g + 1, cz - 160, p);
        lamp(b, cx + 54, g + 1, cz - 160, p);
        lamp(b, cx - 54, g + 1, cz - 204, p);
        lamp(b, cx + 54, g + 1, cz - 204, p);

        // The mosque's walled palm garden, east of the processional axis.
        b.fill(cx + 34, g, cz - 136, cx + 122, g, cz - 84, SAND);
        b.walls(cx + 34, g + 1, cz - 136, cx + 122, g + 2, cz - 84, p.weathered());
        b.fill(cx + 40, g + 1, cz - 84, cx + 50, g + 2, cz - 84, AIR);
        fountain(b, cx + 78, cz - 110, g, p);
        palm(b, cx + 46, g, cz - 128);
        palm(b, cx + 110, g, cz - 128);
        palm(b, cx + 46, g, cz - 92);
        palm(b, cx + 110, g, cz - 92);
    }

    private static void arcade(StructureBuilder b, int x, int z0, int z1, int g, Palette p) {
        for (int z = z0; z <= z1; z += 6) {
            b.fill(x, g + 1, z, x, g + 5, z, p.accent());
        }
        for (int z = z0 + 3; z < z1; z += 6) {
            b.put(x, g + 5, z, p.roofStairs());
        }
        b.fill(x - 2, g + 6, z0, x + 2, g + 6, z1, p.roofSlab());
        b.fill(x - 2, g + 7, z0, x + 2, g + 7, z1, p.roof());
    }

    private static void onionDome(StructureBuilder b, int cx, int cz, int baseY, Palette p) {
        int[] rings = {18, 18, 17, 16, 15, 14, 13, 11, 9, 7, 5, 3, 1};
        for (int i = 0; i < rings.length; i++) {
            int r = rings[i];
            int y = baseY + i;
            b.fill(cx - r, y, cz - r, cx + r, y, cz + r, p.roof());
            for (int d = -r; d <= r; d++) {
                b.put(cx + d, y, cz - r, p.accent());
                b.put(cx + d, y, cz + r, p.accent());
                b.put(cx - r, y, cz + d, p.accent());
                b.put(cx + r, y, cz + d, p.accent());
            }
        }
        b.fill(cx, baseY + rings.length, cz, cx, baseY + rings.length + 3, cz, p.accent());
        b.put(cx, baseY + rings.length + 4, cz, p.light());
    }

    private static void dome(StructureBuilder b, int cx, int cz, int baseY, int radius, Palette p) {
        for (int i = 0; i <= radius; i++) {
            int r = radius - i;
            b.fill(cx - r, baseY + i, cz - r, cx + r, baseY + i, cz + r, p.roof());
            if (r > 0) {
                for (int d = -r; d <= r; d++) {
                    b.put(cx + d, baseY + i, cz - r, p.accent());
                    b.put(cx + d, baseY + i, cz + r, p.accent());
                    b.put(cx - r, baseY + i, cz + d, p.accent());
                    b.put(cx + r, baseY + i, cz + d, p.accent());
                }
            }
        }
        b.put(cx, baseY + radius + 1, cz, p.accent());
        b.put(cx, baseY + radius + 2, cz, p.light());
    }

    private static void minaret(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x - 2, g, z - 2, x + 2, g + 2, z + 2, p.foundation());
        for (int y = g + 3; y <= g + 40; y++) {
            b.fill(x - 1, y, z - 1, x + 1, y, z + 1, p.accent());
            b.put(x, y, z, AIR);
            if (y == g + 18 || y == g + 32) {
                b.fill(x - 3, y, z - 3, x + 3, y, z + 3, p.roofSlab());
                b.walls(x - 3, y + 1, z - 3, x + 3, y + 1, z + 3, p.rail());
            }
        }
        b.fill(x - 2, g + 41, z - 2, x + 2, g + 41, z + 2, p.foundation());
        dome(b, x, z, g + 42, 2, p);
    }

    private static void fountain(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x - 4, g, z - 4, x + 4, g, z + 4, p.foundation());
        b.fill(x - 3, g + 1, z - 3, x + 3, g + 1, z + 3, WATER);
        b.walls(x - 3, g + 1, z - 3, x + 3, g + 1, z + 3, p.accent());
        b.put(x, g + 2, z, p.accent());
        b.put(x, g + 3, z, p.light());
    }

    // ------------------------------------------------------------------ bazaar

    private static void khanElKhalili(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // The authored courtyard blocks between the three long lanes and six cross lanes.
        int[][] blocks = {
                {-316, -286, -236, -186, 0},
                {-274, -226, -236, -186, 1},
                {-214, -166, -236, -186, 2},
                {-154, -142, -236, -186, 3},
                {-316, -286, -174, -126, 2},
                {-274, -226, -174, -126, 0},
                {-214, -166, -174, -126, 1},
                {-154, -142, -174, -126, 3},
                {-316, -286, -114, -66, 1},
                {-214, -166, -114, -66, 0},
                {-154, -142, -114, -66, 3},
                {-274, -226, -54, -9, 2},
                {-274, -226, 9, 54, 2},
                {-154, -142, -54, -9, 3},
                {-154, -142, 9, 54, 3},
                {-316, -286, 66, 114, 3},
                {-274, -226, 66, 114, 1},
                {-214, -166, 66, 114, 2},
                {-154, -142, 66, 114, 3},
                {-316, -286, 126, 174, 2},
                {-214, -166, 126, 174, 0},
                {-154, -142, 126, 174, 3},
                {-316, -286, 186, 200, 1},
                {-274, -226, 186, 200, 0},
                {-214, -166, 186, 200, 2},
                {-154, -142, 186, 200, 3}
        };
        for (int[] blk : blocks) {
            int x0 = cx + blk[0];
            int x1 = cx + blk[1];
            int z0 = cz + blk[2];
            int z1 = cz + blk[3];
            int variant = blk[4];
            if (x1 - x0 < 16) {
                bazaarStrip(b, x0, z0, x1, z1, g, p);
            } else if (variant == 3) {
                bazaarRuin(b, x0, z0, x1, z1, g, p);
            } else {
                bazaarCourt(b, x0, z0, x1, z1, g, p, variant);
            }
        }

        // The special buildings of the bazaar.
        papyrusShop(b, cx - 252, cz - 80, cx - 228, cz - 63, g, p);
        papyrusYard(b, cx - 270, cz - 110, cx - 254, cz - 70, g, p);
        bazaarCourt(b, cx - 252, cz - 112, cx - 230, cz - 84, g, p, 0);
        papyrusStall(b, cx - 260, cz - 114, g, p);
        questBoard(b, cx - 226, cz + 14, g, p);
        teaHouse(b, cx - 212, cz + 16, cx - 188, cz + 44, g, p);
        bazaarCourt(b, cx - 184, cz + 16, cx - 168, cz + 52, g, p, 1);
        teaGarden(b, cx - 210, cz - 50, cx - 170, cz - 9, g, p);
        hakimClinic(b, cx - 252, cz + 123, cx - 228, cz + 145, g, p);
        herbGarden(b, cx - 270, cz + 148, cx - 230, cz + 172, g, p);
        bazaarGarden(b, cx - 316, cz - 54, cx - 286, cz - 9, g, p);
        bazaarGarden(b, cx - 316, cz + 9, cx - 286, cz + 54, g, p);
        bazaarGate(b, cx - 140, cz, g, p);

        // Timber roofs over the busiest stretches of the spine and the west lane.
        coveredLane(b, cx - 220, cz - 40, cz - 10, g, p);
        coveredLane(b, cx - 220, cz + 10, cz + 40, g, p);
        coveredLane(b, cx - 280, cz - 40, cz - 10, g, p);
        coveredLane(b, cx - 280, cz + 10, cz + 40, g, p);

        // Lamps down the spine and the cross axis.
        for (int z = cz - 230; z <= cz + 190; z += 32) {
            if (Math.abs(z - cz) < 12 || Math.abs(z - (cz - 60)) < 8
                    || Math.abs(z - (cz + 60)) < 8) {
                continue;
            }
            lamp(b, cx - 224, g + 1, z, p);
            lamp(b, cx - 216, g + 1, z, p);
        }
        signpost(b, cx - 224, g + 1, cz - 236, Direction.SOUTH, p,
                "KHAN EL-KHALILI", "COVERED BAZAAR");
        signpost(b, cx - 224, g + 1, cz + 196, Direction.NORTH, p,
                "KHAN EL-KHALILI", "OLD CAIRO");
    }

    private static void bazaarCourt(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                    Palette p, int variant) {
        int gateX = (x0 + x1) / 2;
        int gateZ = (z0 + z1) / 2;
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 5, z1, p.wall());
        b.walls(x0, g + 1, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 4, z1 - 1, AIR);
        b.fill(x0 + 4, g, z0 + 4, x1 - 4, g, z1 - 4, p.accent());

        // A south gate, framed with carved sandstone.
        b.fill(gateX - 1, g + 1, z1, gateX + 1, g + 3, z1, AIR);
        b.fill(gateX - 2, g + 1, z1, gateX - 2, g + 4, z1, p.accent());
        b.fill(gateX + 2, g + 1, z1, gateX + 2, g + 4, z1, p.accent());
        b.fill(gateX - 2, g + 4, z1, gateX + 2, g + 4, z1, p.accent());

        // Roofed perimeter rooms around an open courtyard.
        b.fill(x0, g + 5, z0, x1, g + 5, z0 + 3, p.roofSlab());
        b.fill(x0, g + 5, z1 - 3, x1, g + 5, z1, p.roofSlab());
        b.fill(x0, g + 5, z0 + 4, x0 + 3, g + 5, z1 - 4, p.roofSlab());
        b.fill(x1 - 3, g + 5, z0 + 4, x1, g + 5, z1 - 4, p.roofSlab());
        b.crenellations(x0, z0, x1, z1, g + 6);

        if (variant == 0) {
            fountain(b, gateX, gateZ, g, p);
        } else if (variant == 1) {
            palm(b, gateX - 4, g, gateZ);
            palm(b, gateX + 4, g, gateZ);
        } else {
            b.fill(gateX - 2, g + 1, gateZ - 2, gateX + 2, g + 1, gateZ + 2, p.foundation());
            b.put(gateX, g + 2, gateZ, CAULDRON);
        }

        // Counters and wares along the gate wall.
        for (int x = x0 + 4; x <= x1 - 4; x += 6) {
            if (Math.abs(x - gateX) <= 2) {
                continue;
            }
            b.put(x, g + 1, z1 - 1, p.foundation());
            b.put(x, g + 2, z1 - 1, variant == 1 ? BLUE_CLAY : TERRACOTTA);
            b.put(x, g + 3, z1 - 1, p.roofSlab());
        }

        // An awning projecting over the lane.
        b.fill(gateX - 2, g + 4, z1 + 1, gateX + 2, g + 4, z1 + 2, p.roofSlab());
        b.put(gateX - 2, g + 1, z1 + 2, p.accent());
        b.put(gateX - 2, g + 2, z1 + 2, p.accent());
        b.put(gateX - 2, g + 3, z1 + 2, p.accent());
        b.put(gateX + 2, g + 1, z1 + 2, p.accent());
        b.put(gateX + 2, g + 2, z1 + 2, p.accent());
        b.put(gateX + 2, g + 3, z1 + 2, p.accent());

        String[][] signs = {{"KHAN", "COURTYARD"}, {"SILKS", "RUGS & DYE"}, {"COPPER", "BRASS WARES"}};
        b.sign(gateX - 3, g + 3, z1, Direction.SOUTH, signs[variant]);
    }

    private static void bazaarStrip(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                    Palette p) {
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 4, z1, p.wall());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 3, z1 - 1, AIR);
        b.fill(x0, g + 4, z0, x1, g + 4, z1, p.roofSlab());
        for (int z = z0 + 3; z <= z1 - 3; z += 6) {
            b.fill(x0, g + 1, z, x0, g + 3, z, AIR);
            b.put(x0 + 1, g + 1, z, p.foundation());
            b.put(x0 + 1, g + 2, z, TERRACOTTA);
        }
        b.sign(x0, g + 3, z0 + 2, Direction.WEST, "SHOPS");
    }

    private static void bazaarRuin(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                   Palette p) {
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 4, z1, p.weathered());
        b.fill(x0 + 6, g + 1, z0, x0 + 14, g + 4, z0, AIR);
        b.fill(x1, g + 1, z0 + 8, x1, g + 4, z0 + 20, AIR);
        b.fill(x0 + 2, g + 1, z0 + 2, x0 + 8, g + 1, z0 + 8, p.rubble());
        b.fill(x0 + 10, g + 1, z1 - 8, x1 - 2, g + 2, z1 - 2, p.rubble());
        b.put(x0 + 4, g + 3, z0 + 4, p.light());
        b.sign(x0 + 1, g + 3, z1, Direction.SOUTH, "UNSAFE", "KEEP OUT");
    }

    private static void bazaarGarden(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                     Palette p) {
        b.fill(x0, g, z0, x1, g, z1, GRASS);
        b.walls(x0, g + 1, z0, x1, g + 2, z1, p.weathered());
        b.fill(x0 + 6, g + 1, z0, x0 + 8, g + 2, z0, AIR);
        b.fill(x0 + 6, g + 1, z1, x0 + 8, g + 2, z1, AIR);
        fountain(b, (x0 + x1) / 2, (z0 + z1) / 2, g, p);
        palm(b, x0 + 4, g, z0 + 8);
        palm(b, x1 - 4, g, z0 + 8);
        palm(b, x0 + 4, g, z1 - 8);
        palm(b, x1 - 4, g, z1 - 8);
        for (int z = z0 + 12; z <= z1 - 12; z += 12) {
            b.put(x0 + 2, g + 1, z, p.light());
            b.put(x1 - 2, g + 1, z, p.light());
        }
    }

    private static void papyrusShop(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                    Palette p) {
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 6, z1, p.wall());
        b.walls(x0, g + 1, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 5, z1 - 1, AIR);
        b.fill(x0, g + 7, z0, x1, g + 7, z1, p.roof());

        // Open shopfront onto the papyrus lane.
        b.fill(x0 + 2, g + 1, z1, x1 - 2, g + 3, z1, AIR);
        b.fill(x0 + 2, g + 1, z1 - 1, x1 - 2, g + 1, z1 - 1, p.foundation());
        b.fill(x0 + 1, g + 4, z1 + 1, x1 - 1, g + 4, z1 + 2, p.roofSlab());
        b.put(x0 + 1, g + 1, z1 + 2, p.accent());
        b.put(x0 + 1, g + 2, z1 + 2, p.accent());
        b.put(x0 + 1, g + 3, z1 + 2, p.accent());
        b.put(x1 - 1, g + 1, z1 + 2, p.accent());
        b.put(x1 - 1, g + 2, z1 + 2, p.accent());
        b.put(x1 - 1, g + 3, z1 + 2, p.accent());

        // Scrolls on the counter, shelves at the back.
        for (int x = x0 + 3; x <= x1 - 3; x += 3) {
            b.put(x, g + 2, z1 - 1, WHITE_CARPET);
        }
        b.fill(x0 + 2, g + 1, z0 + 2, x0 + 4, g + 3, z0 + 2, BOOKS);
        b.put(x0 + 5, g + 2, z0 + 3, p.light());
        b.sign(x0 + 2, g + 5, z1, Direction.SOUTH, "PAPYRUS", "SCROLLS & INK");
    }

    private static void papyrusYard(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                    Palette p) {
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 4, z1, p.wall());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 3, z1 - 1, AIR);
        b.fill(x0, g + 4, z0, x1, g + 4, z1, p.roofSlab());
        b.fill(x1, g + 1, (z0 + z1) / 2, x1, g + 3, (z0 + z1) / 2, AIR);
        b.fill(x0 + 2, g + 1, z0 + 2, x0 + 5, g + 2, z0 + 2, BOOKS);
        b.put(x0 + 3, g + 1, z1 - 3, CAULDRON);
        b.sign(x1, g + 3, (z0 + z1) / 2 + 3, Direction.EAST, "PAPYRUS YARD");
    }

    private static void papyrusStall(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x - 3, g, z - 3, x + 3, g, z + 3, p.ground());
        b.fill(x - 2, g + 1, z - 2, x + 2, g + 1, z - 2, p.foundation());
        b.put(x - 2, g + 1, z + 2, p.accent());
        b.put(x - 2, g + 2, z + 2, p.accent());
        b.put(x + 2, g + 1, z + 2, p.accent());
        b.put(x + 2, g + 2, z + 2, p.accent());
        b.fill(x - 3, g + 3, z - 3, x + 3, g + 3, z + 3, p.roofSlab());
        b.put(x - 1, g + 2, z - 2, WHITE_CARPET);
        b.put(x + 1, g + 2, z - 2, WHITE_CARPET);
        b.put(x + 2, g + 2, z + 1, p.light());
        b.sign(x, g + 1, z - 3, Direction.NORTH, "PAPYRUS DEALER", "REWARD");
    }

    private static void questBoard(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x, g, z - 4, x, g, z + 4, p.foundation());
        b.fill(x, g + 1, z - 4, x, g + 4, z + 4, p.frame());
        b.fill(x, g + 1, z - 4, x, g + 4, z - 4, p.accent());
        b.fill(x, g + 1, z + 4, x, g + 4, z + 4, p.accent());
        b.fill(x - 1, g + 5, z - 4, x + 1, g + 5, z + 4, p.roofSlab());
        b.put(x - 1, g + 4, z - 4, p.accent());
        b.put(x + 1, g + 4, z + 4, p.accent());
        b.sign(x, g + 3, z - 2, Direction.EAST, "CITY NOTICES");
        b.sign(x, g + 3, z, Direction.EAST, "STOLEN SCROLL");
        b.sign(x, g + 3, z + 2, Direction.EAST, "LOST SCHOLAR");
        b.put(x - 1, g + 1, z + 4, p.light());
    }

    private static void teaHouse(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                 Palette p) {
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 7, z1, p.wall());
        b.walls(x0, g + 1, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 6, z1 - 1, AIR);
        b.fill(x0, g + 8, z0, x1, g + 8, z1, p.roof());
        b.walls(x0, g + 9, z0, x1, g + 9, z1, p.rail());

        // West entrance facing the bazaar spine.
        b.fill(x0, g + 1, z0 + 10, x0, g + 3, z0 + 12, AIR);
        b.fill(x0, g + 4, z0 + 9, x0, g + 4, z0 + 13, p.accent());
        b.sign(x0, g + 6, z0 + 10, Direction.WEST, "TEA HOUSE", "MINT & COFFEE");

        // A shaded veranda over the lane.
        b.fill(x0 - 3, g + 4, z0 + 2, x0 - 1, g + 4, z1 - 2, p.roofSlab());
        for (int z = z0 + 3; z <= z1 - 3; z += 5) {
            b.put(x0 - 3, g + 1, z, p.accent());
            b.put(x0 - 3, g + 2, z, p.accent());
            b.put(x0 - 3, g + 3, z, p.accent());
        }

        // Tables, urns and cushions.
        for (int z = z0 + 4; z <= z1 - 4; z += 6) {
            b.put(x0 + 4, g + 1, z, p.foundation());
            b.put(x0 + 4, g + 2, z, CAULDRON);
            b.put(x0 + 8, g + 1, z, p.rail());
            b.put(x0 + 8, g + 2, z, p.light());
        }
        b.fill(x0 + 2, g + 1, z1 - 3, x0 + 6, g + 1, z1 - 1, BED);
        b.put(x1 - 2, g + 3, z0 + 2, BOOKS);
    }

    private static void teaGarden(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                  Palette p) {
        b.fill(x0, g, z0, x1, g, z1, GRASS);
        b.walls(x0, g + 1, z0, x1, g + 2, z1, p.weathered());
        b.fill(x0 + 2, g + 1, z0, x0 + 4, g + 2, z0, AIR);
        fountain(b, (x0 + x1) / 2, (z0 + z1) / 2, g, p);
        palm(b, x0 + 4, g, z0 + 6);
        palm(b, x1 - 4, g, z0 + 6);
        for (int x = x0 + 6; x <= x1 - 6; x += 8) {
            b.put(x, g + 1, z1 - 4, p.rail());
            b.put(x, g + 2, z1 - 4, p.light());
        }
    }

    private static void hakimClinic(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                    Palette p) {
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 6, z1, p.wall());
        b.walls(x0, g + 1, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 5, z1 - 1, AIR);
        b.fill(x0, g + 7, z0, x1, g + 7, z1, p.roof());
        b.walls(x0, g + 8, z0, x1, g + 8, z1, p.rail());

        // North door onto the hakim lane.
        b.fill(x0 + 8, g + 1, z0, x0 + 10, g + 3, z0, AIR);
        door(b, x0 + 8, g + 1, z0, Direction.NORTH, p);
        b.sign(x0 + 8, g + 5, z0, Direction.NORTH, "HAKIM", "REMEDIES");

        // Two rooms divided by an interior wall.
        b.fill(x0 + 1, g + 1, z0 + 10, x1 - 1, g + 5, z0 + 10, p.wall());
        b.fill(x0 + 8, g + 1, z0 + 10, x0 + 9, g + 3, z0 + 10, AIR);

        // Shelves of jars, a cauldron and a sickbed.
        for (int x = x0 + 2; x <= x1 - 2; x += 4) {
            b.put(x, g + 3, z0 + 2, x % 2 == 0 ? TERRACOTTA : BLUE_CLAY);
            b.put(x, g + 3, z1 - 2, x % 2 == 0 ? YELLOW_CLAY : RED_CLAY);
        }
        b.put(x0 + 3, g + 1, z0 + 4, CAULDRON);
        b.fill(x0 + 12, g + 1, z1 - 4, x0 + 14, g + 1, z1 - 2, BED);
        b.put(x0 + 4, g + 2, z0 + 12, p.light());
    }

    private static void herbGarden(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                   Palette p) {
        b.fill(x0, g, z0, x1, g, z1, GRASS);
        b.walls(x0, g + 1, z0, x1, g + 2, z1, p.weathered());
        b.fill(x0 + 2, g + 1, z1, x1 - 2, g + 2, z1, AIR);
        for (int x = x0 + 3; x <= x1 - 3; x += 5) {
            b.put(x, g + 1, z0 + 3, PALM_LEAF);
            b.put(x, g + 1, z1 - 3, PALM_LEAF);
            b.put(x, g + 1, (z0 + z1) / 2, TERRACOTTA);
        }
        b.put(x0 + 3, g + 2, z0 + 3, p.light());
    }

    private static void coveredLane(StructureBuilder b, int x, int z0, int z1, int g, Palette p) {
        for (int z = z0; z <= z1; z += 8) {
            b.fill(x - 4, g + 1, z, x - 4, g + 6, z, p.accent());
            b.fill(x + 4, g + 1, z, x + 4, g + 6, z, p.accent());
            b.put(x, g + 6, z, p.light());
        }
        for (int z = z0; z <= z1; z += 16) {
            b.fill(x - 5, g + 7, z, x + 5, g + 7, Math.min(z + 12, z1), p.roofSlab());
        }
    }

    private static void bazaarGate(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x, g, z - 10, x, g + 8, z - 10, p.wall());
        b.fill(x, g, z + 10, x, g + 8, z + 10, p.wall());
        b.fill(x, g + 8, z - 10, x, g + 9, z + 10, p.wall());
        b.fill(x, g + 9, z - 10, x, g + 9, z + 10, p.accent());
        b.crenellations(x, z - 10, x, z + 10, g + 10);
        signpost(b, x + 4, g + 1, z - 8, Direction.EAST, p, "KHAN EL-KHALILI", "COVERED BAZAAR");
        signpost(b, x + 4, g + 1, z + 8, Direction.EAST, p, "CITY NOTICES", "BAZAAR WEST");
    }

    // ------------------------------------------------------------------ Nile

    private static void nileCorniche(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int wz0 = cz - 260;
        int wz1 = cz + 160;

        // The river, its bed, the quay wall and the promenade.
        b.fill(cx + 258, g - 3, wz0, cx + 336, g - 3, wz1, p.foundation());
        b.fill(cx + 258, g - 2, wz0, cx + 336, g, wz1, WATER);
        b.fill(cx + 252, g - 3, wz0, cx + 257, g + 1, wz1, p.foundation());
        b.fill(cx + 240, g, wz0, cx + 251, g, wz1, p.ground());
        b.fill(cx + 231, g, wz0, cx + 239, g, wz1, p.ground());

        // A low parapet along the water, with a landing gap at the ferry stairs.
        for (int z = wz0; z <= wz1; z += 4) {
            if (z > cz + 52 && z < cz + 68) {
                continue;
            }
            b.put(cx + 252, g + 1, z, p.foundation());
            b.put(cx + 253, g + 1, z, p.foundation());
        }
        for (int x = cx + 252; x <= cx + 257; x++) {
            b.put(x, g, cz + 60, p.accent());
        }
        b.fill(cx + 254, g - 1, cz + 56, cx + 256, g - 1, cz + 64, p.foundation());

        // Promenade palms, lamps and benches.
        for (int z = wz0 + 12; z <= wz1 - 12; z += 24) {
            if (z > cz + 48 && z < cz + 72) {
                continue;
            }
            palm(b, cx + 244, g, z);
            lamp(b, cx + 236, g + 1, z, p);
        }
        for (int z = wz0 + 20; z <= wz1 - 20; z += 48) {
            b.put(cx + 241, g + 1, z, p.roofSlab());
            b.put(cx + 242, g + 1, z, p.roofSlab());
            b.put(cx + 241, g + 2, z, p.rail());
        }

        // The quay square at the Nile landing.
        b.fill(cx + 220, g, cz + 48, cx + 252, g, cz + 72, p.accent());
        b.fill(cx + 224, g, cz + 52, cx + 248, g, cz + 68, p.ground());
        b.sign(cx + 236, g + 1, cz + 44, Direction.SOUTH, "NILE", "FELUCCAS", "CUSTOMS");
        b.sign(cx + 252, g + 2, cz + 68, Direction.WEST, "DEEP WATER", "NO SWIMMING");
        lamp(b, cx + 222, g + 1, cz + 50, p);
        lamp(b, cx + 222, g + 1, cz + 70, p);

        // The river craft.
        felucca(b, cx + 280, cz - 80, g);
        felucca(b, cx + 300, cz + 20, g);
        felucca(b, cx + 270, cz + 120, g);

        // The waterfront buildings, all facing the corniche road.
        nileBuilding(b, cx + 166, cz - 230, cx + 212, cz - 160, g, p, 8,
                "BONDED STORE");
        museum(b, cx + 166, cz - 145, cx + 216, cz - 95, g, p);
        nileBuilding(b, cx + 166, cz - 88, cx + 214, cz - 52, g, p, 9,
                "NILE CLUB", "MEMBERS ONLY");
        nileBuilding(b, cx + 166, cz - 45, cx + 216, cz + 5, g, p, 12,
                "NILE HOTEL", "ROOMS & BATHS");
        nileGarden(b, cx + 162, cz + 12, cx + 212, cz + 48, g, p);
        nileBuilding(b, cx + 168, cz + 88, cx + 216, cz + 132, g, p, 14,
                "CUSTOMS", "NILE PORT");
        nileBuilding(b, cx + 166, cz + 138, cx + 210, cz + 158, g, p, 7,
                "FERRY");
        crane(b, cx + 248, cz + 104, g);
    }

    private static void nileBuilding(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                     Palette p, int h, String... lines) {
        shell(b, x0, z0, x1, z1, g, h, p, Direction.EAST, (z1 - z0) / 2, false);
        b.sign(x1, g + h - 2, (z0 + z1) / 2, Direction.EAST, lines);
    }

    private static void museum(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                               Palette p) {
        shell(b, x0, z0, x1, z1, g, 12, p, Direction.EAST, (z1 - z0) / 2, false);
        for (int z = z0 + 4; z <= z1 - 4; z += 6) {
            b.fill(x1 + 1, g + 1, z, x1 + 1, g + 9, z, p.accent());
        }
        b.fill(x1 + 1, g + 10, z0 + 3, x1 + 1, g + 10, z1 - 3, p.roofSlab());
        b.sign(x1, g + 10, (z0 + z1) / 2, Direction.EAST,
                "MUSEUM", "ANTIQUITIES", "CURATOR");
        for (int z = z0 + 5; z <= z1 - 5; z += 8) {
            b.fill(x0 + 4, g + 1, z, x0 + 6, g + 2, z + 2, p.foundation());
            b.put(x0 + 5, g + 3, z + 1, TERRACOTTA);
            b.put(x1 - 6, g + 1, z, p.foundation());
            b.put(x1 - 6, g + 2, z, BOOKS);
        }
        b.put(x0 + 8, g + 2, z1 - 8, p.light());
    }

    private static void nileGarden(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                   Palette p) {
        b.fill(x0, g, z0, x1, g, z1, GRASS);
        b.walls(x0, g + 1, z0, x1, g + 2, z1, p.weathered());
        b.fill(x0 + 10, g + 1, z0, x0 + 14, g + 2, z0, AIR);
        b.fill(x0 + 10, g + 1, z1, x0 + 14, g + 2, z1, AIR);
        fountain(b, (x0 + x1) / 2, (z0 + z1) / 2, g, p);
        palm(b, x0 + 5, g, z0 + 6);
        palm(b, x1 - 5, g, z0 + 6);
        palm(b, x0 + 5, g, z1 - 6);
        palm(b, x1 - 5, g, z1 - 6);
    }

    private static void felucca(StructureBuilder b, int x, int z, int g) {
        b.fill(x - 4, g - 1, z - 1, x + 4, g, z + 1, DARK_PLANK);
        b.fill(x - 5, g, z, x - 5, g + 1, z, DARK_PLANK);
        b.fill(x + 5, g, z, x + 5, g + 1, z, DARK_PLANK);
        b.fill(x, g + 1, z, x, g + 7, z, PALM_LOG);
        for (int i = 0; i < 5; i++) {
            b.fill(x + 1, g + 2 + i, z - i / 2, x + 1, g + 2 + i, z + i / 2, WHITE);
        }
        b.put(x - 4, g + 1, z, DARK_SLAB);
    }

    private static void crane(StructureBuilder b, int x, int z, int g) {
        b.fill(x, g + 1, z, x, g + 8, z, DARK_PLANK);
        b.fill(x - 6, g + 8, z, x + 2, g + 8, z, DARK_PLANK);
        b.fill(x - 6, g + 7, z, x - 6, g + 8, z, DARK_PLANK);
        b.put(x - 6, g + 6, z, CAULDRON);
        b.put(x, g + 9, z, IRON);
    }

    // ------------------------------------------------------------------ citadel

    private static void citadel(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx + 150;
        int x1 = cx + 310;
        int z0 = cz + 170;
        int z1 = cz + 260;

        citadelWall(b, x0, z0, x0 + 2, z1, g, p);
        citadelWall(b, x1 - 2, z0, x1, z1, g, p);
        citadelWall(b, x0, z0, x1, z0 + 2, g, p);
        citadelWall(b, x0, z1 - 2, x1, z1, g, p);
        bastion(b, x0 + 1, z0 + 1, g, p);
        bastion(b, x1 - 1, z0 + 1, g, p);
        bastion(b, x0 + 1, z1 - 1, g, p);
        bastion(b, x1 - 1, z1 - 1, g, p);

        // Courtyard paving inside the walls.
        b.fill(x0 + 3, g, z0 + 3, x1 - 3, g, z1 - 3, p.ground());

        // The west gate: a deep arch through the curtain, flanked by two towers.
        b.fill(x0, g + 1, cz + 214, x0 + 2, g + 12, cz + 226, AIR);
        b.fill(x0 - 6, g, cz + 208, x0 + 14, g + 22, cz + 232, p.wall());
        b.fill(x0 - 6, g + 1, cz + 214, x0 + 14, g + 12, cz + 226, AIR);
        b.fill(x0 - 6, g + 1, cz + 212, x0 - 4, g + 14, cz + 212, p.accent());
        b.fill(x0 - 6, g + 1, cz + 228, x0 - 4, g + 14, cz + 228, p.accent());
        b.fill(x0 - 6, g + 14, cz + 212, x0 - 4, g + 14, cz + 228, p.accent());
        b.fill(x0 - 6, g + 15, cz + 212, x0 + 14, g + 15, cz + 228, p.roofSlab());
        b.crenellations(x0 - 6, cz + 208, x0 + 14, cz + 232, g + 23);
        tower(b, x0 - 2, cz + 204, g, p);
        tower(b, x0 - 2, cz + 236, g, p);
        b.sign(x0 - 6, g + 14, cz + 220, Direction.WEST, "CITADEL", "MUHAMMAD ALI");
        lamp(b, x0 - 5, g + 13, cz + 214, p);
        lamp(b, x0 - 5, g + 13, cz + 226, p);

        // Barracks on the four sides of the parade ground.
        barracks(b, cx + 180, cz + 185, g, p);
        barracks(b, cx + 240, cz + 185, g, p);
        barracks(b, cx + 180, cz + 230, g, p);
        barracks(b, cx + 240, cz + 230, g, p);

        // Stables, hay and the great well.
        b.fill(cx + 285, g + 1, cz + 180, cx + 300, g + 1, cz + 184, HAY);
        b.fill(cx + 285, g + 2, cz + 180, cx + 300, g + 2, cz + 184, HAY);
        b.put(cx + 292, g + 3, cz + 182, p.light());
        citadelWell(b, cx + 292, cz + 220, g, p);

        // A small powder magazine against the east wall.
        b.fill(cx + 280, g + 1, cz + 244, cx + 296, g + 5, cz + 254, p.wall());
        b.fill(cx + 281, g + 1, cz + 245, cx + 295, g + 4, cz + 253, AIR);
        b.fill(cx + 280, g + 6, cz + 244, cx + 296, g + 6, cz + 254, p.roofSlab());
        b.put(cx + 280, g + 2, cz + 248, IRON);
    }

    private static void citadelWall(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                    Palette p) {
        b.fill(x0, g, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0, g + 2, z0, x1, g + 16, z1, p.wall());
        b.fill(x0, g + 9, z0, x1, g + 9, z1, p.accent());
        b.fill(x0, g + 17, z0, x1, g + 17, z1, p.wall());
        b.crenellations(x0, z0, x1, z1, g + 18);
    }

    private static void bastion(StructureBuilder b, int x, int z, int g, Palette p) {
        for (int y = g; y <= g + 20; y++) {
            for (int dx = -7; dx <= 7; dx++) {
                for (int dz = -7; dz <= 7; dz++) {
                    if (dx * dx + dz * dz > 49) {
                        continue;
                    }
                    b.put(x + dx, y, z + dz, y <= g + 1 ? p.foundation() : p.wall());
                }
            }
        }
        b.fill(x - 7, g + 21, z - 7, x + 7, g + 21, z + 7, p.accent());
        dome(b, x, z, g + 22, 4, p);
    }

    private static void tower(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x - 5, g, z - 5, x + 5, g + 24, z + 5, p.wall());
        b.fill(x - 4, g + 1, z - 4, x + 4, g + 23, z + 4, AIR);
        b.fill(x - 5, g + 18, z - 5, x + 5, g + 18, z + 5, p.accent());
        b.crenellations(x - 5, z - 5, x + 5, z + 5, g + 25);
        b.window(x, g + 8, z - 5, 3, 1, true);
        b.window(x, g + 8, z + 5, 3, 1, true);
        dome(b, x, z, g + 26, 3, p);
    }

    private static void barracks(StructureBuilder b, int x0, int z0, int g, Palette p) {
        int x1 = x0 + 34;
        int z1 = z0 + 18;
        shell(b, x0, z0, x1, z1, g, 7, p, Direction.SOUTH, 8, true);
        for (int x = x0 + 3; x <= x1 - 3; x += 6) {
            b.put(x, g + 1, z0 + 3, BED);
            b.put(x, g + 1, z1 - 3, BED);
        }
        b.fill(x0 + 2, g + 1, z0 + 2, x0 + 6, g + 2, z0 + 2, HAY);
        b.put(x0 + 2, g + 3, z0 + 2, p.light());
    }

    private static void citadelWell(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x - 4, g, z - 4, x + 4, g, z + 4, p.foundation());
        b.walls(x - 3, g - 6, z - 3, x + 3, g + 1, z + 3, p.wall());
        b.fill(x - 2, g - 6, z - 2, x + 2, g - 1, z + 2, AIR);
        b.fill(x - 2, g - 5, z - 2, x + 2, g - 1, z + 2, WATER);
        b.fill(x - 2, g, z - 2, x + 2, g, z + 2, AIR);
        b.walls(x - 2, g + 1, z - 2, x + 2, g + 1, z + 2, p.accent());
        b.fill(x - 2, g + 2, z - 2, x - 2, g + 5, z - 2, p.accent());
        b.fill(x + 2, g + 2, z - 2, x + 2, g + 5, z - 2, p.accent());
        b.fill(x - 2, g + 2, z + 2, x - 2, g + 5, z + 2, p.accent());
        b.fill(x + 2, g + 2, z + 2, x + 2, g + 5, z + 2, p.accent());
        b.fill(x - 3, g + 6, z - 3, x + 3, g + 6, z + 3, p.roofSlab());
        b.put(x, g + 5, z, p.light());
        signpost(b, x, g + 1, z + 5, Direction.SOUTH, p, "WELL", "DEEP SHAFT", "MIND THE ROPE");
    }

    // ------------------------------------------------------------------ rail

    private static void railStation(StructureBuilder b, int cx, int cz, Palette p,
                                    CitySurface surface) {
        int trackZ = cz + 320;
        int trackY = surface.at(cx + 124, trackZ);

        // A straight, level track bed from the yard to the district edge.
        for (int x = cx + 124; x <= cx + 338; x++) {
            int sy = surface.at(x, trackZ);
            if (sy > trackY) {
                b.fill(x, trackY + 1, trackZ - 2, x, sy, trackZ + 2, AIR);
            } else if (sy < trackY) {
                b.fill(x, sy + 1, trackZ - 2, x, trackY, trackZ + 2, p.foundation());
            }
            b.fill(x, trackY, trackZ - 2, x, trackY, trackZ + 2, p.foundation());
            b.put(x, trackY + 1, trackZ - 1, RAIL);
            b.put(x, trackY + 1, trackZ + 1, RAIL);
            b.put(x, trackY + 1, trackZ, p.roofSlab());
        }

        // Station building and platform on the north side of the track.
        int bx0 = cx + 124;
        int bx1 = cx + 148;
        int bz0 = cz + 308;
        int bz1 = cz + 316;
        int by = trackY + 1;
        for (int x = bx0; x <= bx1; x++) {
            for (int z = bz0; z <= bz1; z++) {
                int sy = surface.at(x, z);
                if (sy < by) {
                    b.fill(x, sy + 1, z, x, by, z, p.foundation());
                } else if (sy > by) {
                    b.fill(x, by + 1, z, x, sy, z, AIR);
                }
            }
        }
        b.fill(bx0, by, bz0, bx1, by, bz1, p.ground());
        b.walls(bx0, by + 1, bz0, bx1, by + 6, bz1, p.wall());
        b.fill(bx0, by + 1, bz0, bx1, by + 1, bz1, p.foundation());
        b.fill(bx0 + 1, by + 1, bz0 + 1, bx1 - 1, by + 5, bz1 - 1, AIR);
        b.fill(bx0, by + 7, bz0, bx1, by + 7, bz1, p.roof());
        b.walls(bx0, by + 8, bz0, bx1, by + 8, bz1, p.rail());
        b.fill(cx + 136, by + 1, bz1, cx + 136, by + 2, bz1, AIR);
        door(b, cx + 136, by + 1, bz1, Direction.SOUTH, p);
        b.sign(cx + 136, by + 5, bz1, Direction.SOUTH, "CAIRO STATION", "RAIL YARD");

        for (int x = bx0; x <= bx1; x++) {
            for (int z = bz1; z <= cz + 318; z++) {
                int ps = surface.at(x, z);
                if (ps < by) {
                    b.fill(x, ps + 1, z, x, by - 1, z, p.foundation());
                } else if (ps > by) {
                    b.fill(x, by, z, x, ps, z, AIR);
                }
            }
        }
        b.fill(bx0, by, bz1, bx1, by, cz + 318, p.accent());
        b.fill(bx0, by + 4, cz + 317, bx1, by + 4, cz + 318, p.roofSlab());
        b.fill(bx0, by + 1, cz + 317, bx0, by + 3, cz + 317, p.accent());
        b.fill(bx1, by + 1, cz + 317, bx1, by + 3, cz + 317, p.accent());
        lamp(b, bx0 + 2, by, cz + 317, p);

        // The goods shed in the yard.
        int sx0 = cx + 128;
        int sx1 = cx + 142;
        int sz0 = cz + 300;
        int sz1 = cz + 306;
        int sy = surface.at(cx + 135, cz + 303);
        for (int x = sx0; x <= sx1; x++) {
            for (int z = sz0; z <= sz1; z++) {
                int ps = surface.at(x, z);
                if (ps < sy) {
                    b.fill(x, ps + 1, z, x, sy, z, p.foundation());
                } else if (ps > sy) {
                    b.fill(x, sy + 1, z, x, ps, z, AIR);
                }
            }
        }
        b.fill(sx0, sy, sz0, sx1, sy, sz1, p.ground());
        b.walls(sx0, sy + 1, sz0, sx1, sy + 4, sz1, p.wall());
        b.fill(sx0 + 1, sy + 1, sz0 + 1, sx1 - 1, sy + 3, sz1 - 1, AIR);
        b.fill(sx0, sy + 5, sz0, sx1, sy + 5, sz1, p.roofSlab());
        b.fill(sx0, sy + 1, sz1, sx1, sy + 2, sz1, AIR);
        b.sign(sx0 + 2, sy + 4, sz1, Direction.SOUTH, "GOODS", "YARD");
        b.fill(sx0 + 2, sy + 1, sz0 + 2, sx0 + 5, sy + 1, sz0 + 3, HAY);
        b.put(sx1 - 2, sy + 1, sz0 + 2, p.foundation());
    }

    // ------------------------------------------------------------------ necropolis

    private static void cityOfTheDead(StructureBuilder b, int cx, int cz, int g, Palette p) {
        necropolisGate(b, cx, cz + 244, g, p);

        int[][] tombs = {
                {-300, 240, 0}, {-240, 240, 1}, {-170, 240, 2}, {-100, 240, 0},
                {-40, 240, 1}, {-300, 286, 2}, {-240, 286, 0}, {-170, 286, 1},
                {-100, 286, 0}, {-300, 306, 1}, {-240, 306, 2}, {-100, 306, 0},
                {-40, 306, 1}, {50, 240, 0}, {115, 240, 2}, {50, 286, 1},
                {115, 286, 0}, {50, 306, 2}, {115, 306, 1}
        };
        for (int[] tomb : tombs) {
            mausoleum(b, cx + tomb[0], cz + tomb[1], g, p, tomb[2]);
        }

        // A few leaning markers and collapsed plots between the lanes.
        for (int x = -310; x <= 110; x += 20) {
            if (Math.abs(x) < 18) {
                continue;
            }
            b.put(cx + x, g + 1, cz + 252, p.roofSlab());
            b.put(cx + x, g + 2, cz + 252, p.weathered());
        }

        secretTomb(b, cx, cz, g, p);
    }

    private static void necropolisGate(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x - 11, g, z - 4, x - 8, g + 9, z + 4, p.wall());
        b.fill(x + 8, g, z - 4, x + 11, g + 9, z + 4, p.wall());
        b.fill(x - 11, g + 9, z - 4, x + 11, g + 10, z + 4, p.wall());
        b.fill(x - 11, g + 10, z - 4, x + 11, g + 10, z + 4, p.accent());
        b.crenellations(x - 11, z - 4, x + 11, z + 4, g + 11);
        b.put(x - 10, g + 1, z + 4, p.light());
        b.put(x + 10, g + 1, z + 4, p.light());
        signpost(b, x - 13, g + 1, z + 6, Direction.SOUTH, p,
                "CITY OF THE", "DEAD", "SILENCE PLEASE");
        signpost(b, x + 13, g + 1, z + 6, Direction.SOUTH, p,
                "TOMBS", "NO DIGGING");
    }

    private static void mausoleum(StructureBuilder b, int x, int z, int g, Palette p, int variant) {
        b.fill(x - 7, g, z - 7, x + 7, g, z + 7, SAND);
        b.walls(x - 7, g + 1, z - 7, x + 7, g + 2, z + 7, p.weathered());
        b.fill(x - 1, g + 1, z - 7, x + 1, g + 2, z - 7, AIR);

        int x0 = x - 4;
        int x1 = x + 4;
        int z0 = z - 4;
        int z1 = z + 4;
        if (variant == 0) {
            b.walls(x0, g + 1, z0, x1, g + 6, z1, p.wall());
            b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 5, z1 - 1, AIR);
            b.fill(x0, g + 7, z0, x1, g + 7, z1, p.roof());
            b.fill(x, g + 1, z1, x, g + 2, z1, AIR);
            door(b, x, g + 1, z1, Direction.SOUTH, p);
            dome(b, x, z, g + 8, 4, p);
            b.put(x - 3, g + 3, z0, p.light());
        } else if (variant == 1) {
            for (int dx = -3; dx <= 3; dx += 6) {
                for (int dz = -3; dz <= 3; dz += 6) {
                    b.fill(x + dx, g + 1, z + dz, x + dx, g + 5, z + dz, p.accent());
                }
            }
            b.fill(x0, g + 6, z0, x1, g + 6, z1, p.roofSlab());
            dome(b, x, z, g + 7, 4, p);
            b.fill(x - 2, g + 1, z - 2, x + 2, g + 1, z + 2, p.accent());
            b.fill(x - 1, g + 2, z - 1, x + 1, g + 2, z + 1, p.foundation());
        } else {
            b.walls(x0, g + 1, z0, x1, g + 5, z1, p.weathered());
            b.fill(x0 + 4, g + 1, z0, x1, g + 4, z0 + 2, AIR);
            b.fill(x1, g + 1, z1 - 4, x1, g + 3, z1, AIR);
            b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 1, z1 - 1, p.rubble());
            b.put(x - 2, g + 2, z, p.roofSlab());
            b.put(x + 2, g + 3, z + 1, p.roof());
        }

        for (int dz = -6; dz <= 6; dz += 3) {
            b.put(x - 6, g + 1, z + dz, p.roofSlab());
            b.put(x + 6, g + 1, z + dz, p.roofSlab());
        }
    }

    private static void secretTomb(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x = cx - 216;
        int z = cz + 278;

        // A small surface kiosk and its walled plot.
        b.fill(x - 8, g, z - 9, x + 8, g, z + 7, SAND);
        b.walls(x - 8, g + 1, z - 9, x + 8, g + 2, z + 7, p.weathered());
        b.walls(x - 4, g + 1, z - 9, x + 4, g + 5, z - 1, p.wall());
        b.fill(x - 3, g + 1, z - 8, x + 3, g + 4, z - 2, AIR);
        b.fill(x - 1, g + 1, z - 9, x + 1, g + 3, z - 9, AIR);
        b.fill(x - 4, g + 6, z - 9, x + 4, g + 6, z - 1, p.roofSlab());
        dome(b, x, z - 5, g + 7, 3, p);
        b.sign(x, g + 4, z - 9, Direction.NORTH,
                "SCRIBE'S TOMB", "TURN BACK", "SCROLL WAITS");

        // A stair from the kiosk down to the burial chamber.
        int sx0 = x - 2;
        int sx1 = x + 2;
        b.fill(sx0, g - 8, z - 7, sx1, g + 1, z + 3, AIR);
        for (int i = 0; i < 7; i++) {
            int top = g - 1 - i;
            int sz = z - 7 + i;
            b.fill(sx0, g - 8, sz, sx1, top, sz, p.foundation());
            b.put(sx0, top, sz, p.accent());
            b.put(sx1, top, sz, p.accent());
        }
        b.fill(sx0 - 1, g - 8, z - 8, sx0 - 1, g + 1, z + 4, p.wall());
        b.fill(sx1 + 1, g - 8, z - 8, sx1 + 1, g + 1, z + 4, p.wall());

        // The chamber beneath the plot.
        int x0 = x - 12;
        int x1 = x + 12;
        int z0 = z - 1;
        int z1 = z + 15;
        int floor = g - 8;
        b.fill(x0 - 1, floor - 1, z0 - 1, x1 + 1, floor - 1, z1 + 1, p.foundation());
        b.walls(x0, floor, z0, x1, floor + 6, z1, p.wall());
        b.fill(x0 + 1, floor, z0 + 1, x1 - 1, floor + 5, z1 - 1, AIR);
        b.fill(x0, floor + 6, z0, x1, floor + 6, z1, p.foundation());
        b.fill(x0 + 1, floor, z0 + 1, x1 - 1, floor, z1 - 1, p.accent());
        b.fill(x - 2, floor, z0, x + 2, floor + 3, z0, AIR);

        // Sarcophagus, chests, shrine and the warning.
        b.fill(x - 2, floor + 1, z + 8, x + 2, floor + 1, z + 11, p.accent());
        b.fill(x - 1, floor + 2, z + 9, x + 1, floor + 2, z + 10, p.foundation());
        b.chest(x - 8, floor + 1, z + 12, SiteLoot.RELIC);
        b.chest(x + 8, floor + 1, z + 12, SiteLoot.CULT);
        b.fill(x - 2, floor + 1, z1 - 2, x + 2, floor + 3, z1 - 2, p.accent());
        b.put(x, floor + 4, z1 - 2, p.light());
        b.put(x0 + 2, floor + 5, z0 + 2, Palette.cobweb());
        b.put(x1 - 2, floor + 5, z0 + 2, Palette.cobweb());
        lamp(b, x - 6, floor + 1, z + 6, p);
        lamp(b, x + 6, floor + 1, z + 6, p);
        b.sign(x, floor + 3, z1, Direction.NORTH,
                "SCRIBE RESTS", "SCROLL IS GONE", "TAKE IT AND PAY");
    }

    // ------------------------------------------------------------------ European quarter

    private static void europeanQuarter(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // The central square with its fountain.
        b.fill(cx - 14, g, cz - 14, cx + 14, g, cz + 14, p.accent());
        b.fill(cx - 12, g, cz - 12, cx + 12, g, cz + 12, p.ground());
        fountain(b, cx, cz, g, p);
        lamp(b, cx - 10, g + 1, cz - 10, p);
        lamp(b, cx + 10, g + 1, cz - 10, p);
        lamp(b, cx - 10, g + 1, cz + 10, p);
        lamp(b, cx + 10, g + 1, cz + 10, p);
        signpost(b, cx + 16, g + 1, cz + 12, Direction.WEST, p,
                "EUROPEAN QTR", "BANKS & HOTELS");

        // The banks and hotels around the square.
        euroBlock(b, cx - 60, cz - 48, cx - 24, cz - 12, g, p, 10,
                Direction.EAST, false, "NATIONAL BANK");
        euroBlock(b, cx + 24, cz - 48, cx + 60, cz - 12, g, p, 12,
                Direction.WEST, false, "GRAND HOTEL", "ROOMS");
        euroBlock(b, cx - 60, cz + 22, cx - 24, cz + 58, g, p, 7,
                Direction.EAST, false, "CAFE RICHE");
        euroBlock(b, cx + 24, cz + 22, cx + 60, cz + 58, g, p, 10,
                Direction.WEST, false, "OTTOMAN BANK");
        portico(b, cx - 23, cz - 44, cz - 16, g, p);
        portico(b, cx + 23, cz - 44, cz - 16, g, p);
        portico(b, cx + 23, cz + 26, cz + 54, g, p);
        awning(b, cx - 23, cz + 26, cz + 54, g, p, Direction.EAST);
        awning(b, cx + 77, cz + 26, cz + 54, g, p, Direction.WEST);

        // The quieter blocks behind the frontage.
        euroBlock(b, cx - 112, cz - 45, cx - 78, cz - 8, g, p, 9,
                Direction.EAST, true, "PENSION");
        euroBlock(b, cx - 112, cz + 22, cx - 78, cz + 55, g, p, 7,
                Direction.EAST, true, "BOOKSELLER");
        euroBlock(b, cx + 78, cz - 45, cx + 112, cz - 8, g, p, 11,
                Direction.WEST, false, "CONSULATE");
        euroBlock(b, cx + 78, cz + 22, cx + 112, cz + 55, g, p, 7,
                Direction.WEST, false, "CAFE");
        portico(b, cx + 77, cz - 41, cz - 12, g, p);

        // North of the cross street: the church, the hospital and two garden squares.
        church(b, cx - 105, cz + 98, g, p);
        euroBlock(b, cx + 30, cz + 95, cx + 105, cz + 135, g, p, 10,
                Direction.SOUTH, false, "HOSPITAL", "ST MARY");

        // Townhouses along the southern edge.
        euroBlock(b, cx - 112, cz + 145, cx - 72, cz + 175, g, p, 8,
                Direction.SOUTH, true, "TOWNHOUSE");
        euroBlock(b, cx - 66, cz + 145, cx - 26, cz + 175, g, p, 8,
                Direction.SOUTH, true, "TOWNHOUSE");
        euroBlock(b, cx + 26, cz + 145, cx + 66, cz + 175, g, p, 8,
                Direction.SOUTH, true, "TOWNHOUSE");
        euroBlock(b, cx + 72, cz + 145, cx + 112, cz + 175, g, p, 8,
                Direction.SOUTH, true, "TOWNHOUSE");

        // Lamps down the two main axes through the quarter.
        for (int z = cz - 50; z <= cz + 180; z += 24) {
            if (Math.abs(z - cz) < 16) {
                continue;
            }
            lamp(b, cx - 9, g + 1, z, p);
            lamp(b, cx + 9, g + 1, z, p);
        }

        orientalHotel(b, cx, cz, g, p);
    }

    private static void euroBlock(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                  Palette p, int h, Direction front, boolean pitched,
                                  String... lines) {
        int offset = front == Direction.EAST || front == Direction.WEST
                ? (z1 - z0) / 2 : (x1 - x0) / 2;
        shell(b, x0, z0, x1, z1, g, h, p, front, offset, pitched);
        int sx;
        int sz;
        switch (front) {
            case NORTH -> {
                sx = x0 + offset;
                sz = z0;
            }
            case SOUTH -> {
                sx = x0 + offset;
                sz = z1;
            }
            case WEST -> {
                sx = x0;
                sz = z0 + offset;
            }
            default -> {
                sx = x1;
                sz = z0 + offset;
            }
        }
        b.sign(sx, g + h - 2, sz, front, lines);
    }

    private static void shell(StructureBuilder b, int x0, int z0, int x1, int z1, int g, int h,
                              Palette p, Direction doorSide, int doorOffset, boolean pitched) {
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + h, z1, p.wall());
        b.fill(x0, g + 1, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + h - 1, z1 - 1, AIR);

        int doorX;
        int doorZ;
        switch (doorSide) {
            case NORTH -> {
                doorX = x0 + doorOffset;
                doorZ = z0;
            }
            case SOUTH -> {
                doorX = x0 + doorOffset;
                doorZ = z1;
            }
            case WEST -> {
                doorX = x0;
                doorZ = z0 + doorOffset;
            }
            default -> {
                doorX = x1;
                doorZ = z0 + doorOffset;
            }
        }
        b.fill(doorX, g + 1, doorZ, doorX, g + 2, doorZ, AIR);
        door(b, doorX, g + 1, doorZ, doorSide, p);

        for (int y = g + 3; y <= g + h - 2; y += 3) {
            for (int x = x0 + 2; x <= x1 - 2; x += 4) {
                if ((doorSide == Direction.NORTH || doorSide == Direction.SOUTH)
                        && x == doorX) {
                    continue;
                }
                b.window(x, y, z0, 2, 2, true);
                b.window(x, y, z1, 2, 2, true);
            }
            for (int z = z0 + 2; z <= z1 - 2; z += 4) {
                if ((doorSide == Direction.EAST || doorSide == Direction.WEST)
                        && z == doorZ) {
                    continue;
                }
                b.window(x0, y, z, 2, 2, true);
                b.window(x1, y, z, 2, 2, true);
            }
        }

        if (pitched) {
            b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + h, 4,
                    x1 - x0 >= z1 - z0 ? 0 : 1);
        } else {
            b.fill(x0, g + h, z0, x1, g + h, z1, p.roof());
            b.walls(x0, g + h + 1, z0, x1, g + h + 1, z1, p.rail());
        }
    }

    private static void portico(StructureBuilder b, int x, int z0, int z1, int g, Palette p) {
        for (int z = z0; z <= z1; z += 4) {
            b.fill(x, g + 1, z, x, g + 7, z, p.accent());
        }
        b.fill(x, g + 8, z0, x, g + 8, z1, p.roofSlab());
    }

    private static void awning(StructureBuilder b, int x, int z0, int z1, int g, Palette p,
                               Direction out) {
        if (out == Direction.EAST) {
            b.fill(x, g + 4, z0, x + 2, g + 4, z1, p.roofSlab());
            b.put(x + 2, g + 1, z0, p.accent());
            b.put(x + 2, g + 2, z0, p.accent());
            b.put(x + 2, g + 3, z0, p.accent());
            b.put(x + 2, g + 1, z1, p.accent());
            b.put(x + 2, g + 2, z1, p.accent());
            b.put(x + 2, g + 3, z1, p.accent());
        } else if (out == Direction.WEST) {
            b.fill(x - 2, g + 4, z0, x, g + 4, z1, p.roofSlab());
            b.put(x - 2, g + 1, z0, p.accent());
            b.put(x - 2, g + 2, z0, p.accent());
            b.put(x - 2, g + 3, z0, p.accent());
            b.put(x - 2, g + 1, z1, p.accent());
            b.put(x - 2, g + 2, z1, p.accent());
            b.put(x - 2, g + 3, z1, p.accent());
        }
    }

    private static void church(StructureBuilder b, int x0, int z0, int g, Palette p) {
        int x1 = x0 + 60;
        int z1 = z0 + 36;
        b.fill(x0, g, z0, x1, g, z1, p.ground());

        int nx0 = x0 + 12;
        int nx1 = x1 - 4;
        int nz0 = z0 + 6;
        int nz1 = z1 - 6;
        b.walls(nx0, g + 1, nz0, nx1, g + 12, nz1, p.wall());
        b.fill(nx0, g + 1, nz0, nx1, g + 1, nz1, p.foundation());
        b.fill(nx0 + 1, g + 1, nz0 + 1, nx1 - 1, g + 11, nz1 - 1, AIR);
        b.pitchedRoof(nx0 - 1, nz0 - 1, nx1 + 1, nz1 + 1, g + 12, 6, 1);

        // The west tower and its spire.
        int tx = x0 + 8;
        int tz = z0 + 8;
        b.fill(tx - 4, g, tz - 4, tx + 4, g + 24, tz + 4, p.wall());
        b.fill(tx - 3, g + 1, tz - 3, tx + 3, g + 23, tz + 3, AIR);
        b.fill(tx - 4, g + 18, tz - 4, tx + 4, g + 18, tz + 4, p.accent());
        b.fill(tx - 2, g + 10, tz - 4, tx + 2, g + 14, tz - 4, p.window());
        b.fill(tx + 4, g + 1, tz, tx + 4, g + 2, tz, AIR);
        door(b, tx + 4, g + 1, tz, Direction.EAST, p);
        spireRoof(b, tx, tz, g + 25, 8, p);
        b.fill(tx, g + 34, tz, tx, g + 36, tz, p.accent());
        b.fill(tx - 2, g + 35, tz, tx + 2, g + 35, tz, p.accent());

        // South door and sign on the nave.
        int nx = (nx0 + nx1) / 2;
        b.fill(nx, g + 1, nz1, nx, g + 2, nz1, AIR);
        door(b, nx, g + 1, nz1, Direction.SOUTH, p);
        b.sign(nx, g + 8, nz1, Direction.SOUTH, "ALL SAINTS", "CHURCH");

        for (int z = nz0 + 6; z <= nz1 - 6; z += 6) {
            b.window(nx0, g + 5, z, 4, 2, true);
            b.window(nx1, g + 5, z, 4, 2, true);
        }
    }

    private static void spireRoof(StructureBuilder b, int x, int z, int baseY, int height,
                                  Palette p) {
        for (int i = 0; i < height; i++) {
            int r = Math.max(0, 3 - i * 3 / height);
            b.fill(x - r, baseY + i, z - r, x + r, baseY + i, z + r, p.roof());
        }
        b.put(x, baseY + height, z, p.accent());
    }

    private static void orientalHotel(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 112;
        int x1 = cx - 52;
        int z0 = cz - 136;
        int z1 = cz - 88;
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 8, z1, p.wall());
        b.walls(x0, g + 1, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 7, z1 - 1, AIR);
        b.fill(x0, g + 9, z0, x1, g + 9, z1, p.roof());
        b.walls(x0, g + 10, z0, x1, g + 10, z1, p.rail());

        // The east gate opens toward the main axis.
        b.fill(x1, g + 1, cz - 114, x1, g + 4, cz - 110, AIR);
        b.fill(x1, g + 1, cz - 116, x1, g + 5, cz - 116, p.accent());
        b.fill(x1, g + 1, cz - 108, x1, g + 5, cz - 108, p.accent());
        b.fill(x1, g + 5, cz - 116, x1, g + 5, cz - 108, p.accent());
        b.sign(x1, g + 6, cz - 112, Direction.EAST, "ORIENTAL HOTEL", "ROOMS & BATHS");

        // Courtyard fountain, palms and the room cells along the west wall.
        b.fill(x0 + 10, g, z0 + 10, x1 - 10, g, z1 - 10, p.accent());
        fountain(b, cx - 82, cz - 112, g, p);
        palm(b, cx - 100, g, cz - 128);
        palm(b, cx - 62, g, cz - 128);
        for (int z = z0 + 6; z <= z1 - 12; z += 12) {
            b.walls(x0 + 1, g + 1, z, x0 + 11, g + 4, z + 8, p.wall());
            b.fill(x0 + 2, g + 1, z + 1, x0 + 10, g + 3, z + 7, AIR);
            b.fill(x0 + 2, g + 1, z + 1, x0 + 4, g + 1, z + 2, BED);
            b.fill(x0 + 11, g + 1, z + 4, x0 + 11, g + 2, z + 4, AIR);
            b.put(x0 + 10, g + 3, z + 1, p.light());
        }
        b.put(x0 + 4, g + 1, z1 - 4, CAULDRON);
    }

    // ------------------------------------------------------------------ gardens

    private static void gardens(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // The palm grove north of the mosque.
        b.fill(cx - 100, g, cz - 300, cx + 100, g, cz - 272, SAND);
        for (int x = -90; x <= 90; x += 30) {
            palm(b, cx + x, g, cz - 296);
            palm(b, cx + x, g, cz - 280);
        }
        for (int x = -30; x <= 30; x += 10) {
            b.fill(cx + x, g + 1, cz - 276, cx + x, g + 4, cz - 276, p.weathered());
        }
        b.fill(cx - 32, g + 5, cz - 276, cx + 32, g + 5, cz - 276, p.roofSlab());

        // The open maidan between the bazaar and the necropolis.
        b.fill(cx - 300, g, cz + 202, cx - 160, g, cz + 226, SAND);
        palm(b, cx - 290, g, cz + 214);
        palm(b, cx - 270, g, cz + 214);
        palm(b, cx - 190, g, cz + 214);
        palm(b, cx - 170, g, cz + 214);
        b.fill(cx - 240, g + 1, cz + 210, cx - 236, g + 1, cz + 214, p.foundation());
        b.put(cx - 238, g + 2, cz + 212, p.light());

        // Processional palms on the southern approach.
        for (int z = 190; z <= 230; z += 20) {
            palm(b, cx - 11, g, cz + z);
            palm(b, cx + 11, g, cz + z);
        }

        // The two garden squares flanking the European quarter.
        for (int dx = -1; dx <= 1; dx += 2) {
            int gx = cx + dx * 20;
            b.fill(gx - 8, g, cz + 92, gx + 8, g, cz + 134, GRASS);
            fountain(b, gx, cz + 112, g, p);
            palm(b, gx - 6, g, cz + 98);
            palm(b, gx + 6, g, cz + 98);
            palm(b, gx - 6, g, cz + 128);
            palm(b, gx + 6, g, cz + 128);
        }

        // Avenue palms on the strips between the bazaar, the quarter and the citadel.
        for (int z = -50; z <= 170; z += 24) {
            if (Math.abs(z) < 10 || Math.abs(z - 80) < 10) {
                continue;
            }
            palm(b, cx - 130, g, cz + z);
            palm(b, cx + 130, g, cz + z);
        }
        for (int z = 180; z <= 260; z += 20) {
            palm(b, cx + 322, g, cz + z);
            palm(b, cx + 334, g, cz + z);
        }

        // A few palms inside the City of the Dead.
        palm(b, cx - 312, g, cz + 252);
        palm(b, cx + 112, g, cz + 252);
    }

    private static void palm(StructureBuilder b, int x, int y, int z) {
        b.fill(x, y, z, x, y + 5, z, PALM_LOG);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 3) {
                    b.put(x + dx, y + 6, z + dz, PALM_LEAF);
                }
            }
        }
        b.put(x, y + 7, z, PALM_LEAF);
        b.put(x - 3, y + 5, z, PALM_LEAF);
        b.put(x + 3, y + 5, z, PALM_LEAF);
        b.put(x, y + 5, z - 3, PALM_LEAF);
        b.put(x, y + 5, z + 3, PALM_LEAF);
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
}
