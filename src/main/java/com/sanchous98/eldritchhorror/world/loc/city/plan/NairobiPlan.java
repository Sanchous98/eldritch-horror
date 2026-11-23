package com.sanchous98.eldritchhorror.world.loc.city.plan;

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
 * Nairobi — the authored colonial railway capital: the KICC-style tower over a civic plaza, the
 * station with its platforms and goods yards, the Indian bazaar, the colonial club and bungalows,
 * and the dusty market-garden outskirts. Beneath the station lies the flooded undercroft where a
 * surveyor vanished and a poached relic was hidden.
 *
 * <p>Everything is placed deliberately through {@link StructureBuilder}; there is no layout RNG,
 * no chunk-local read and no static mutable state, so every chunk that overlaps the city writes
 * the same blocks. Coordinates are relative to {@code (cx, ground, cz)} in the private methods and
 * absolute in {@link #build}. All districts and POIs stay inside {@code [-340, 340]}.
 */
public final class NairobiPlan implements CityPlan {

    /** Authored half-extent: 680 blocks across, matching the brief. */
    private static final int HALF_EXTENT = 340;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState FARMLAND = Blocks.FARMLAND.defaultBlockState();
    private static final BlockState WHEAT = Blocks.WHEAT.defaultBlockState();
    private static final BlockState LEAF = Blocks.ACACIA_LEAVES.defaultBlockState();
    private static final BlockState LOG = Blocks.ACACIA_LOG.defaultBlockState();
    private static final BlockState BUSH = Blocks.DEAD_BUSH.defaultBlockState();
    private static final BlockState TURF = Blocks.SHORT_GRASS.defaultBlockState();
    private static final BlockState FLOWER = Blocks.DANDELION.defaultBlockState();
    private static final BlockState RAIL = Blocks.RAIL.defaultBlockState();
    private static final BlockState IRON = Blocks.IRON_BLOCK.defaultBlockState();
    private static final BlockState COAL = Blocks.COAL_BLOCK.defaultBlockState();
    private static final BlockState BOOKS = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState BARREL = Blocks.BARREL.defaultBlockState();
    private static final BlockState WHITE = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState RED = Blocks.RED_SANDSTONE.defaultBlockState();
    private static final BlockState BLACK = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
    private static final BlockState GREEN = Blocks.MOSS_BLOCK.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState PAD = Blocks.LILY_PAD.defaultBlockState();
    private static final BlockState HAY = Blocks.HAY_BLOCK.defaultBlockState();

    @Override
    public String cityId() {
        return "nairobi";
    }

    @Override
    public int halfExtent() {
        return HALF_EXTENT;
    }

    @Override
    public void build(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        build(b, cx, cz, ground, (x, z) -> ground, p);
    }

    @Override
    public void build(StructureBuilder b, CityPlanContext ctx, Palette p) {
        build(b, ctx.cx(), ctx.cz(), ctx.ground(), ctx.surface()::at, p);
    }

    private static void build(StructureBuilder b, int cx, int cz, int ground,
                              IntBinaryOperator surface, Palette p) {
        streets(b, cx, cz, ground, surface, p);
        arrivalGate(b, cx, cz, surface, p);
        civicCentre(b, cx, cz, ground, p);
        bazaar(b, cx, cz, ground, surface, p);
        railway(b, cx, cz, ground, p);
        colonialClub(b, cx, cz, surface, p);
        outskirts(b, cx, cz, surface, p);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.LANDMARK, 0, 0, 320, "nairobi"),
                new CityPoi("arrival_sign", CityPoi.Kind.SIGN, 10, 1, 306, "districts"),
                new CityPoi("kicc_tower", CityPoi.Kind.LANDMARK, 0, 0, -180, "kicc"),
                new CityPoi("railway_hotel", CityPoi.Kind.INN, -80, 0, -120, "hotel_keeper"),
                new CityPoi("hotel_keeper", CityPoi.Kind.NPC, -72, 0, -112, "hotel_keeper"),
                new CityPoi("bazaar_quest_board", CityPoi.Kind.QUEST_BOARD, -220, 0, 0,
                        "nairobi_missing_surveyor;nairobi_poached_relic"),
                new CityPoi("bazaar_shop", CityPoi.Kind.SHOP, -240, 0, -60, "merchant"),
                new CityPoi("merchant", CityPoi.Kind.NPC, -244, 0, -70, "merchant"),
                new CityPoi("herbalist", CityPoi.Kind.HEALER, -240, 0, 120, "herbalist"),
                new CityPoi("herbalist_npc", CityPoi.Kind.NPC, -244, 0, 132, "herbalist"),
                new CityPoi("survey_office", CityPoi.Kind.NPC, 220, 0, -120,
                        "nairobi_missing_surveyor"),
                new CityPoi("survey_clerk", CityPoi.Kind.NPC, 212, 0, -112, "surveyor"),
                new CityPoi("station", CityPoi.Kind.STATION, 258, 0, 0, "railway_station;north_south"),
                new CityPoi("station_yard", CityPoi.Kind.STATION, 270, 0, 136,
                        "rail_yard;north_south"),
                new CityPoi("stationmaster", CityPoi.Kind.NPC, 196, 0, 24, "stationmaster"),
                new CityPoi("askari", CityPoi.Kind.NPC, 160, 0, 20, "askari"),
                new CityPoi("station_undercroft", CityPoi.Kind.SECRET, 220, -6, 60, "cellar"),
                new CityPoi("club", CityPoi.Kind.LANDMARK, 240, 0, 240, "colonial_club"),
                new CityPoi("club_steward", CityPoi.Kind.NPC, 240, 0, 220,
                        "nairobi_poached_relic"),
                new CityPoi("outskirts_sign", CityPoi.Kind.SIGN, -20, 1, 300, "outskirts"),
                new CityPoi("old_kraal", CityPoi.Kind.LANDMARK, -115, 0, 285, "ruin"));
    }

    // ------------------------------------------------------------------ streets

    private static void streets(StructureBuilder b, int cx, int cz, int g,
                                IntBinaryOperator surface, Palette p) {
        // The 14-wide processional axis from the arrival gate to the civic plaza.
        path(b, cx, cz - 150, cx, cz + 322, 14, surface, p.ground());
        // Station road and forecourt.
        path(b, cx + 7, cz + 20, cx + 180, cz + 20, 12, surface, p.ground());
        b.fill(cx + 140, g, cz, cx + 180, g, cz + 40, p.ground());
        // Club avenue.
        path(b, cx + 7, cz + 240, cx + 210, cz + 240, 8, surface, p.ground());
        // Bazaar spine, cross lanes and the two back lanes.
        path(b, cx - 220, cz - 236, cx - 220, cz + 204, 6, surface, p.ground());
        for (int dz : new int[]{-60, 60, 120, 180}) {
            path(b, cx - 320, cz + dz, cx - 140, cz + dz, 6, surface, p.ground());
        }
        path(b, cx - 286, cz - 236, cx - 286, cz + 204, 5, surface, p.ground());
        path(b, cx - 181, cz - 236, cx - 181, cz + 204, 5, surface, p.ground());
        // The bazaar's east gate lane, linking the quest square to the axis.
        path(b, cx - 204, cz, cx - 7, cz, 6, surface, p.ground());
        // Hotel lane and the outskirt tracks.
        path(b, cx - 60, cz - 120, cx - 7, cz - 120, 4, surface, p.ground());
        path(b, cx - 320, cz + 256, cx - 7, cz + 256, 4, surface, p.ground());
        path(b, cx + 7, cz + 280, cx + 130, cz + 280, 4, surface, p.ground());
        // Church walk.
        path(b, cx + 178, cz + 244, cx + 182, cz + 266, 4, surface, p.ground());
        // Station goods yard, gravel.
        b.fill(cx + 180, g, cz + 32, cx + 250, g, cz + 100, GRAVEL);
    }

    // ------------------------------------------------------------------ arrival

    private static void arrivalGate(StructureBuilder b, int cx, int cz,
                                    IntBinaryOperator surface, Palette p) {
        int g = surface.applyAsInt(cx, cz + 320);
        int z0 = cz + 310;
        int z1 = cz + 322;
        surfaceApron(b, cx - 26, cz + 304, cx + 26, cz + 330, surface, p);

        gatePylon(b, cx - 14, cx - 8, z0, z1, g, p);
        gatePylon(b, cx + 8, cx + 14, z0, z1, g, p);
        b.fill(cx - 14, g + 12, z0, cx + 14, g + 13, z1, p.wall());
        b.fill(cx - 14, g + 12, z0, cx + 14, g + 12, z1, p.accent());
        b.fill(cx - 14, g + 14, z0, cx + 14, g + 14, z1, p.accent());
        b.sign(cx, g + 15, z1, Direction.SOUTH, "NAIROBI");

        signpost(b, cx + 10, surface.applyAsInt(cx + 10, cz + 306) + 1, cz + 306,
                Direction.SOUTH, p, "CIVIC CENTRE", "RAILWAY STATION", "BAZAAR",
                "COLONIAL CLUB");
        signpost(b, cx - 10, surface.applyAsInt(cx - 10, cz + 306) + 1, cz + 306,
                Direction.SOUTH, p, "OUTSKIRTS", "CITY NOTICES", "MARKET GARDENS");

        // Low flanking walls with crenellations, lamps and scrub.
        b.fill(cx - 26, g + 1, z0, cx - 15, g + 2, z1, p.weathered());
        b.fill(cx + 15, g + 1, z0, cx + 26, g + 2, z1, p.weathered());
        b.crenellations(cx - 26, z0, cx - 15, z1, g + 3);
        b.crenellations(cx + 15, z0, cx + 26, z1, g + 3);
        lamp(b, cx - 20, g + 3, z1, p);
        lamp(b, cx + 20, g + 3, z1, p);
        b.put(cx - 24, surface.applyAsInt(cx - 24, z0 - 2) + 1, z0 - 2, BUSH);
        b.put(cx + 24, surface.applyAsInt(cx + 24, z0 - 2) + 1, z0 - 2, BUSH);
    }

    private static void gatePylon(StructureBuilder b, int x0, int x1, int z0, int z1,
                                  int g, Palette p) {
        b.fill(x0, g, z0, x1, g + 12, z1, p.wall());
        b.fill(x0, g, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0, g + 4, z0, x1, g + 4, z1, p.accent());
        b.fill(x0, g + 9, z0, x1, g + 9, z1, p.accent());
        b.fill(x0, g + 12, z0, x1, g + 12, z1, p.accent());
    }

    // ------------------------------------------------------------------ civic centre

    private static void civicCentre(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // The paved plaza around the tower.
        CityPlanSupport.plaza(b, cx - 110, cz - 260, cx + 110, cz - 150, g, p.ground());
        b.walls(cx - 110, g, cz - 260, cx + 110, g, cz - 150, p.foundation());
        // Openings where the axis and the office walks cross the plaza kerb.
        b.fill(cx - 7, g, cz - 150, cx + 7, g, cz - 150, p.ground());
        b.fill(cx - 110, g, cz - 220, cx - 110, g, cz - 215, p.ground());
        b.fill(cx + 110, g, cz - 220, cx + 110, g, cz - 215, p.ground());

        kiccTower(b, cx, cz - 180, g, p);
        office(b, cx - 138, cz - 230, cx - 116, cz - 200, g, p, Direction.EAST, "CIVIC OFFICES");
        office(b, cx + 116, cz - 230, cx + 138, cz - 200, g, p, Direction.WEST, "POST OFFICE");
        railwayHotel(b, cx - 100, cz - 140, cx - 60, cz - 100, g, p);

        // Paths from the plaza to the two offices.
        CityPlanSupport.path(b, cx - 116, cz - 218, cx - 110, cz - 218, 6, g, p.ground());
        CityPlanSupport.path(b, cx + 110, cz - 218, cx + 116, cz - 218, 6, g, p.ground());

        // Flagpoles framing the tower.
        flagpole(b, cx - 80, cz - 240, g, p);
        flagpole(b, cx + 80, cz - 240, g, p);
        flagpole(b, cx - 80, cz - 160, g, p);
        flagpole(b, cx + 80, cz - 160, g, p);

        // Plaza lamps and benches.
        for (int dz = -250; dz <= -160; dz += 30) {
            lamp(b, cx - 96, g + 1, cz + dz, p);
            lamp(b, cx + 96, g + 1, cz + dz, p);
        }
        b.fill(cx - 30, g + 1, cz - 246, cx - 26, g + 1, cz - 244, p.roofSlab());
        b.fill(cx + 26, g + 1, cz - 246, cx + 30, g + 1, cz - 244, p.roofSlab());
        b.put(cx - 28, g + 2, cz - 245, p.light());
        b.put(cx + 28, g + 2, cz - 245, p.light());

        // City notices board at the plaza entrance.
        b.fill(cx + 24, g, cz - 148, cx + 38, g + 2, cz - 144, p.wall());
        b.fill(cx + 25, g + 1, cz - 147, cx + 37, g + 1, cz - 145, p.foundation());
        b.fill(cx + 23, g + 3, cz - 149, cx + 39, g + 3, cz - 143, p.roofSlab());
        b.sign(cx + 27, g + 2, cz - 144, Direction.SOUTH, "CITY NOTICES");
        b.sign(cx + 33, g + 2, cz - 144, Direction.SOUTH, "SURVEYOR LOST");
        signpost(b, cx + 12, g + 1, cz - 140, Direction.SOUTH, p,
                "CIVIC CENTRE", "KICC TOWER", "RAILWAY STATION", "BAZAAR");
    }

    private static void kiccTower(StructureBuilder b, int tx, int tz, int g, Palette p) {
        // Broad foundation disc and the low podium.
        disc(b, tx, tz, g, 20, p.foundation());
        disc(b, tx, tz, g + 1, 17, p.foundation());
        for (int y = g + 2; y <= g + 9; y++) {
            ring(b, tx, tz, y, 16, p.wall());
        }
        disc(b, tx, tz, g + 2, 16, p.foundation());
        disc(b, tx, tz, g + 9, 16, p.foundation());

        // Podium glazing arcs and the four doors.
        ringBand(b, tx, tz, g + 5, g + 7, 16, Direction.NORTH, p.window());
        ringBand(b, tx, tz, g + 5, g + 7, 16, Direction.SOUTH, p.window());
        ringBand(b, tx, tz, g + 5, g + 7, 16, Direction.EAST, p.window());
        ringBand(b, tx, tz, g + 5, g + 7, 16, Direction.WEST, p.window());
        podiumDoor(b, tx, tz + 16, g, Direction.SOUTH, p);
        podiumDoor(b, tx, tz - 16, g, Direction.NORTH, p);
        podiumDoor(b, tx + 16, tz, g, Direction.EAST, p);
        podiumDoor(b, tx - 16, tz, g, Direction.WEST, p);

        // The shaft: banded glazing and vertical concrete ribs.
        int shaft0 = g + 10;
        int shaft1 = g + 52;
        for (int y = shaft0; y <= shaft1; y++) {
            boolean band = (y - shaft0) % 10 == 0;
            ring(b, tx, tz, y, 6, band ? p.window() : p.wall());
            b.put(tx + 6, y, tz, p.accent());
            b.put(tx - 6, y, tz, p.accent());
            b.put(tx, y, tz + 6, p.accent());
            b.put(tx, y, tz - 6, p.accent());
            b.put(tx + 4, y, tz + 4, p.accent());
            b.put(tx - 4, y, tz + 4, p.accent());
            b.put(tx + 4, y, tz - 4, p.accent());
            b.put(tx - 4, y, tz - 4, p.accent());
        }

        // The flared, stepped crown.
        int crownH = 10;
        for (int i = 0; i <= crownH; i++) {
            int r = 6 + i * 11 / crownH;
            disc(b, tx, tz, g + 53 + i, r, p.wall());
            ring(b, tx, tz, g + 53 + i, r, p.accent());
        }
        disc(b, tx, tz, g + 63, 17, p.foundation());
        b.crenellations(tx - 12, tz - 12, tx + 12, tz + 12, g + 64);
        b.spire(tx, tz, g + 65, 16);
    }

    private static void podiumDoor(StructureBuilder b, int x, int z, int g,
                                   Direction facing, Palette p) {
        b.fill(x - 1, g + 2, z - 1, x + 1, g + 4, z + 1, AIR);
        door(b, x, g + 2, z, facing, p);
        if (facing == Direction.EAST || facing == Direction.WEST) {
            b.put(x, g + 5, z - 2, p.accent());
            b.put(x, g + 5, z + 2, p.accent());
        } else {
            b.put(x - 2, g + 5, z, p.accent());
            b.put(x + 2, g + 5, z, p.accent());
        }
    }

    private static void office(StructureBuilder b, int x0, int z0, int x1, int z1,
                               int g, Palette p, Direction front, String label) {
        b.fill(x0 - 1, g - 1, z0 - 1, x1 + 1, g - 1, z1 + 1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 10, z1, p.wall());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 9, z1 - 1, AIR);
        b.fill(x0 + 1, g + 4, z0 + 1, x1 - 1, g + 4, z1 - 1, p.foundation());
        b.fill(x0, g + 11, z0, x1, g + 11, z1, p.roof());
        b.walls(x0, g + 12, z0, x1, g + 12, z1, p.rail());

        int dx = (x0 + x1) / 2;
        int dz = (z0 + z1) / 2;
        int doorX;
        int doorZ;
        if (front == Direction.EAST) {
            doorX = x1;
            doorZ = dz;
        } else if (front == Direction.WEST) {
            doorX = x0;
            doorZ = dz;
        } else if (front == Direction.SOUTH) {
            doorX = dx;
            doorZ = z1;
        } else {
            doorX = dx;
            doorZ = z0;
        }
        b.fill(doorX, g + 1, doorZ, doorX, g + 2, doorZ, AIR);
        door(b, doorX, g + 1, doorZ, front, p);
        b.sign(doorX, g + 8, doorZ, front, label);

        // South windows, upper and lower.
        b.fill(x0 + 3, g + 2, z1, x0 + 4, g + 3, z1, p.window());
        b.fill(x1 - 4, g + 2, z1, x1 - 3, g + 3, z1, p.window());
        b.fill(x0 + 3, g + 6, z1, x0 + 4, g + 7, z1, p.window());
        b.fill(x1 - 4, g + 6, z1, x1 - 3, g + 7, z1, p.window());

        // A counter, a desk and a lamp in each storey.
        b.fill(x0 + 2, g + 1, z0 + 2, x0 + 6, g + 2, z0 + 2, p.foundation());
        b.put(x0 + 4, g + 3, z0 + 2, p.light());
        b.fill(x1 - 6, g + 5, z0 + 3, x1 - 3, g + 6, z0 + 5, p.foundation());
        b.put(x1 - 5, g + 7, z0 + 4, BOOKS);
        b.put(x1 - 2, g + 1, z1 - 2, BARREL);
    }

    private static void railwayHotel(StructureBuilder b, int x0, int z0, int x1, int z1,
                                     int g, Palette p) {
        b.fill(x0 - 1, g - 1, z0 - 1, x1 + 1, g - 1, z1 + 1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 10, z1, p.wall());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 9, z1 - 1, AIR);
        b.fill(x0 + 1, g + 5, z0 + 1, x1 - 1, g + 5, z1 - 1, p.foundation());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 10, 4, 0);

        int dz = (z0 + z1) / 2;
        // East entrance from the hotel lane.
        b.fill(x1, g + 1, dz - 1, x1, g + 3, dz + 1, AIR);
        door(b, x1, g + 1, dz, Direction.EAST, p);
        b.sign(x1, g + 8, dz, Direction.EAST, "RAILWAY HOTEL");
        // Door canopy on two posts.
        b.fill(x1 + 1, g + 5, dz - 4, x1 + 2, g + 5, dz + 4, p.roofSlab());
        b.fill(x1 + 2, g + 1, dz - 4, x1 + 2, g + 4, dz - 4, p.accent());
        b.fill(x1 + 2, g + 1, dz + 4, x1 + 2, g + 4, dz + 4, p.accent());

        // Windows to the lane and the plaza.
        for (int z = z0 + 6; z <= z1 - 6; z += 8) {
            b.fill(x1, g + 2, z, x1, g + 3, z + 1, p.window());
            b.fill(x1, g + 6, z, x1, g + 7, z + 1, p.window());
        }
        b.fill(x0, g + 2, z0 + 6, x0, g + 3, z0 + 8, p.window());
        b.fill(x0, g + 6, z0 + 6, x0, g + 7, z0 + 8, p.window());

        // Reception, bar, dining table and a guest bed.
        b.fill(x0 + 2, g + 1, z0 + 3, x0 + 10, g + 2, z0 + 3, p.foundation());
        b.put(x0 + 6, g + 3, z0 + 3, p.light());
        b.fill(x0 + 2, g + 1, z1 - 5, x0 + 5, g + 2, z1 - 3, p.foundation());
        b.put(x0 + 3, g + 3, z1 - 4, BARREL);
        b.fill(x1 - 8, g + 1, z0 + 4, x1 - 4, g + 1, z0 + 7, p.roofSlab());
        b.put(x1 - 6, g + 2, z0 + 5, p.light());
        b.fill(x1 - 8, g + 6, z1 - 8, x1 - 5, g + 6, z1 - 5, RED);
        b.put(x1 - 7, g + 7, z1 - 7, WHITE);
        b.put(x1 - 3, g + 1, z1 - 2, BOOKS);
    }

    // ------------------------------------------------------------------ bazaar

    private static void bazaar(StructureBuilder b, int cx, int cz, int g,
                               IntBinaryOperator surface, Palette p) {
        // The quest square, a widening of the spine at the crossing.
        CityPlanSupport.plaza(b, cx - 236, cz - 16, cx - 204, cz + 16, g, p.ground());
        questBoard(b, cx - 236, cz - 12, g, p);

        String[] wares = {"SPICE & DYE", "CLOTH", "TEA HOUSE", "CRAFTS", "LEATHER",
                "GRAIN", "POTTERY", "TOOLS"};
        int[] westZ = {-232, -200, -168, -136, -104, -48, 24, 68, 156};
        for (int i = 0; i < westZ.length; i++) {
            int sy = surface.applyAsInt(cx - 234, cz + westZ[i] + 10);
            shophouse(b, cx - 256, cz + westZ[i], cx - 234, cz + westZ[i] + 20, sy, p,
                    Direction.EAST, wares[i % wares.length]);
        }
        int[] eastZ = {-232, -200, -168, -136, -104, -48, 24, 68, 156};
        for (int i = 0; i < eastZ.length; i++) {
            int sy = surface.applyAsInt(cx - 206, cz + eastZ[i] + 10);
            shophouse(b, cx - 206, cz + eastZ[i], cx - 184, cz + eastZ[i] + 20, sy, p,
                    Direction.WEST, wares[(i + 3) % wares.length]);
        }
        int[] outerWestZ = {-232, -196, -152, -100, -44, 28};
        for (int i = 0; i < outerWestZ.length; i++) {
            int sy = surface.applyAsInt(cx - 294, cz + outerWestZ[i] + 10);
            shophouse(b, cx - 316, cz + outerWestZ[i], cx - 294, cz + outerWestZ[i] + 20, sy, p,
                    Direction.EAST, wares[(i + 1) % wares.length]);
        }
        int[] outerEastZ = {-232, -196, -152, -100, -44, 28};
        for (int i = 0; i < outerEastZ.length; i++) {
            int sy = surface.applyAsInt(cx - 176, cz + outerEastZ[i] + 10);
            shophouse(b, cx - 176, cz + outerEastZ[i], cx - 154, cz + outerEastZ[i] + 20, sy, p,
                    Direction.WEST, wares[(i + 5) % wares.length]);
        }

        // The spice merchant's shop on the south cross lane.
        int spiceY = surface.applyAsInt(cx - 245, cz - 66);
        shophouse(b, cx - 256, cz - 78, cx - 234, cz - 66, spiceY, p, Direction.SOUTH,
                "SPICE & DYE");
        // The herbalist's house on the north cross lane.
        int healerY = surface.applyAsInt(cx - 245, cz + 126);
        shophouse(b, cx - 256, cz + 126, cx - 234, cz + 146, healerY, p, Direction.NORTH,
                "HERBALIST");

        // Tea courtyards between the outer and inner rows.
        int[][] yards = {{-236, -68}, {-52, 54}, {66, 114}, {126, 174}, {186, 204}};
        for (int[] yard : yards) {
            surfacePad(b, cx - 283, cz + yard[0], cx - 257, cz + yard[1], surface, p.rubble());
        }
        acacia(b, cx - 270, cz - 140, surface.applyAsInt(cx - 270, cz - 140));
        acacia(b, cx - 270, cz - 20, surface.applyAsInt(cx - 270, cz - 20));
        acacia(b, cx - 270, cz + 90, surface.applyAsInt(cx - 270, cz + 90));
        acacia(b, cx - 270, cz + 170, surface.applyAsInt(cx - 270, cz + 170));

        // Market stalls and tea braziers in the quest square.
        marketStall(b, cx - 228, cz + 10, g, p);
        marketStall(b, cx - 212, cz + 10, g, p);

        // The eastern edge: a dusty strip with thorn scrub.
        for (int dz = -220; dz <= 190; dz += 34) {
            if (Math.abs(dz + 60) < 10 || Math.abs(dz - 60) < 10
                    || Math.abs(dz - 120) < 10 || Math.abs(dz - 180) < 10) {
                continue;
            }
            b.put(cx - 146, surface.applyAsInt(cx - 146, cz + dz) + 1, cz + dz, BUSH);
            b.put(cx - 143, surface.applyAsInt(cx - 143, cz + dz + 6) + 1, cz + dz + 6, BUSH);
        }
        // The dusty maidan between the bazaar and the processional axis.
        acacia(b, cx - 120, cz + 40, surface.applyAsInt(cx - 120, cz + 40));
        acacia(b, cx - 80, cz - 40, surface.applyAsInt(cx - 80, cz - 40));
        acacia(b, cx - 40, cz + 70, surface.applyAsInt(cx - 40, cz + 70));
        signpost(b, cx - 228, g + 1, cz + 16, Direction.SOUTH, p,
                "BAZAAR", "TEA & SPICE", "CRAFTS");
    }

    private static void questBoard(StructureBuilder b, int x0, int z0, int g, Palette p) {
        b.fill(x0, g, z0, x0 + 6, g + 3, z0 + 8, p.wall());
        b.fill(x0 + 1, g + 1, z0 + 1, x0 + 5, g + 2, z0 + 7, p.foundation());
        b.fill(x0 - 1, g + 4, z0 - 1, x0 + 7, g + 4, z0 + 9, p.roofSlab());
        b.sign(x0 + 6, g + 2, z0 + 2, Direction.EAST, "SURVEYOR LOST");
        b.sign(x0 + 6, g + 2, z0 + 6, Direction.EAST, "POACHED RELIC");
        b.put(x0 + 6, g + 3, z0 + 4, p.light());
        b.put(x0 - 1, g + 1, z0 + 8, p.rail());
        b.put(x0 - 1, g + 2, z0 + 8, p.light());
    }

    private static void shophouse(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  int g, Palette p, Direction front, String label) {
        b.fill(x0 - 1, g - 1, z0 - 1, x1 + 1, g - 1, z1 + 1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 5, z1, p.wall());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 4, z1 - 1, AIR);
        int ridge = x1 - x0 >= z1 - z0 ? 0 : 1;
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 5, 3, ridge);

        int mx = (x0 + x1) / 2;
        int mz = (z0 + z1) / 2;
        int dx;
        int dz;
        if (front == Direction.WEST) {
            dx = x0;
            dz = mz;
        } else if (front == Direction.EAST) {
            dx = x1;
            dz = mz;
        } else if (front == Direction.NORTH) {
            dx = mx;
            dz = z0;
        } else {
            dx = mx;
            dz = z1;
        }
        b.fill(dx, g + 1, dz, dx, g + 2, dz, AIR);
        door(b, dx, g + 1, dz, front, p);
        b.sign(dx, g + 5, dz, front, label);

        if (front == Direction.WEST || front == Direction.EAST) {
            b.fill(dx, g + 2, dz - 7, dx, g + 3, dz - 6, p.window());
            b.fill(dx, g + 2, dz + 6, dx, g + 3, dz + 7, p.window());
        } else {
            b.fill(dx - 7, g + 2, dz, dx - 6, g + 3, dz, p.window());
            b.fill(dx + 6, g + 2, dz, dx + 7, g + 3, dz, p.window());
        }

        // Corrugated awning on two timber posts, over the shopfront.
        if (front == Direction.WEST) {
            awning(b, x0 - 2, z0 + 1, x0 - 1, z1 - 1, g, p);
        } else if (front == Direction.EAST) {
            awning(b, x1 + 1, z0 + 1, x1 + 2, z1 - 1, g, p);
        } else if (front == Direction.NORTH) {
            awning(b, x0 + 1, z0 - 2, x1 - 1, z0 - 1, g, p);
        } else {
            awning(b, x0 + 1, z1 + 1, x1 - 1, z1 + 2, g, p);
        }

        // Counter, barrel and a hanging lamp inside.
        if (front == Direction.WEST || front == Direction.EAST) {
            int bx = front == Direction.EAST ? x0 + 2 : x1 - 3;
            b.fill(bx, g + 1, z0 + 3, bx + 1, g + 2, z1 - 3, p.foundation());
        } else {
            int bz = front == Direction.NORTH ? z0 + 2 : z1 - 3;
            b.fill(x0 + 3, g + 1, bz, x1 - 3, g + 2, bz + 1, p.foundation());
        }
        b.put(mx, g + 1, mz, BARREL);
        b.put(x0 + 2, g + 4, z0 + 2, p.light());
    }

    private static void awning(StructureBuilder b, int x0, int z0, int x1, int z1,
                               int g, Palette p) {
        b.fill(x0, g + 4, z0, x1, g + 4, z1, p.roof());
        b.fill(x0, g + 1, z0, x0, g + 3, z0, p.accent());
        b.fill(x1, g + 1, z1, x1, g + 3, z1, p.accent());
    }

    private static void marketStall(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx - 2, g + 1, cz - 2, cx + 2, g + 1, cz + 2, p.roofSlab());
        b.fill(cx - 2, g + 2, cz - 2, cx - 2, g + 4, cz - 2, p.rail());
        b.fill(cx + 2, g + 2, cz - 2, cx + 2, g + 4, cz - 2, p.rail());
        b.fill(cx - 2, g + 2, cz + 2, cx - 2, g + 4, cz + 2, p.rail());
        b.fill(cx + 2, g + 2, cz + 2, cx + 2, g + 4, cz + 2, p.rail());
        b.fill(cx - 3, g + 5, cz - 3, cx + 3, g + 5, cz + 3, p.roof());
        b.put(cx, g + 2, cz, p.light());
        b.put(cx - 1, g + 2, cz + 1, HAY);
        b.put(cx + 1, g + 2, cz - 1, BARREL);
    }

    // ------------------------------------------------------------------ railway

    private static void railway(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // Survey office lane north from the forecourt.
        CityPlanSupport.path(b, cx + 162, cz, cx + 162, cz - 105, 4, g, p.ground());
        CityPlanSupport.path(b, cx + 162, cz - 105, cx + 220, cz - 105, 4, g, p.ground());

        stationBuilding(b, cx + 180, cz - 30, cx + 250, cz + 30, g, p);
        platform(b, cx + 252, cz - 180, cx + 264, cz + 110, g, p, 258);
        platform(b, cx + 279, cz - 180, cx + 291, cz + 110, g, p, 285);
        track(b, cx + 266, cz - 220, cz + 136, g, p);
        track(b, cx + 293, cz - 220, cz + 136, g, p);

        waterTower(b, cx + 158, cz - 120, g, p);
        warehouse(b, cx + 144, cz - 200, g, p);
        warehouse(b, cx + 144, cz + 70, g, p);
        surveyOffice(b, cx + 205, cz - 135, g, p);
        train(b, cx, cz, g, p);
        wagon(b, cx + 293, cz - 160, g, p);
        wagon(b, cx + 293, cz - 140, g, p);
        undercroft(b, cx, cz, g, p);

        // East goods yard, coal heaps and a crane.
        b.fill(cx + 304, g, cz - 220, cx + 320, g, cz + 120, p.rubble());
        b.fill(cx + 308, g + 1, cz - 60, cx + 316, g + 3, cz - 52, COAL);
        b.fill(cx + 306, g + 1, cz - 40, cx + 312, g + 2, cz - 34, p.rubble());
        b.fill(cx + 310, g + 1, cz + 70, cx + 310, g + 8, cz + 70, p.accent());
        b.fill(cx + 306, g + 8, cz + 70, cx + 314, g + 8, cz + 70, p.accent());
        b.put(cx + 306, g + 7, cz + 70, p.light());

        signpost(b, cx + 258, g + 2, cz - 60, Direction.WEST, p,
                "PLATFORM 1", "NAIROBI");
        signpost(b, cx + 252, g + 2, cz + 30, Direction.WEST, p,
                "KEEP OFF TRACKS");
        signpost(b, cx + 230, g + 1, cz + 92, Direction.SOUTH, p,
                "DANGER", "OLD CELLAR");
        signpost(b, cx + 238, g + 1, cz - 100, Direction.SOUTH, p,
                "SURVEY OFFICE", "SURVEYOR LOST");
        signpost(b, cx + 260, g + 1, cz + 132, Direction.SOUTH, p,
                "RAIL YARD", "NAIROBI");
        b.sign(cx + 180, g + 9, cz + 20, Direction.WEST, "RAILWAY STATION");
    }

    private static void stationBuilding(StructureBuilder b, int x0, int z0, int x1, int z1,
                                        int g, Palette p) {
        b.fill(x0 - 1, g - 1, z0 - 1, x1 + 1, g - 1, z1 + 1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 12, z1, p.wall());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 11, z1 - 1, AIR);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 12, 5, 0);

        int dz = (z0 + z1) / 2;
        // West entrance facing the forecourt.
        b.fill(x0, g + 1, dz - 2, x0, g + 4, dz + 2, AIR);
        door(b, x0, g + 1, dz, Direction.WEST, p);
        // East door onto the platform side.
        b.fill(x1, g + 1, dz - 1, x1, g + 2, dz + 1, AIR);
        door(b, x1, g + 1, dz, Direction.EAST, p);
        b.fill(x0, g + 1, dz - 3, x0, g + 5, dz - 3, p.accent());
        b.fill(x0, g + 1, dz + 3, x0, g + 5, dz + 3, p.accent());
        b.fill(x0, g + 5, dz - 3, x0, g + 5, dz + 3, p.accent());
        b.sign(x0, g + 9, dz, Direction.WEST, "RAILWAY STATION");

        // Windows down the west facade and the upper storey.
        for (int z = z0 + 6; z <= z1 - 6; z += 8) {
            if (Math.abs(z - dz) < 6) {
                continue;
            }
            b.fill(x0, g + 3, z, x0, g + 5, z + 1, p.window());
            b.fill(x0, g + 8, z, x0, g + 10, z + 1, p.window());
        }
        // Clock face above the entrance, slightly south.
        b.fill(x0, g + 8, dz + 10, x0, g + 12, dz + 14, p.accent());
        b.fill(x0, g + 9, dz + 11, x0, g + 11, dz + 13, p.window());
        b.put(x0, g + 10, dz + 12, p.accent());

        // Ticket counter, benches, lamps and the stationmaster's desk.
        b.fill(x0 + 20, g + 1, z0 + 4, x0 + 24, g + 2, z0 + 14, p.foundation());
        b.put(x0 + 22, g + 3, z0 + 6, p.light());
        b.fill(x0 + 4, g + 1, z0 + 6, x0 + 8, g + 1, z0 + 8, p.roofSlab());
        b.fill(x0 + 4, g + 1, z1 - 8, x0 + 8, g + 1, z1 - 6, p.roofSlab());
        b.fill(x1 - 12, g + 1, z1 - 12, x1 - 6, g + 2, z1 - 9, p.foundation());
        b.put(x1 - 8, g + 3, z1 - 10, BOOKS);
        lamp(b, x0 + 12, g + 1, z0 + 4, p);
        lamp(b, x0 + 12, g + 1, z1 - 4, p);
    }

    private static void platform(StructureBuilder b, int x0, int z0, int x1, int z1,
                                 int g, Palette p, int postX) {
        b.fill(x0, g, z0, x1, g, z1, p.foundation());
        b.fill(x0, g + 1, z0, x1, g + 1, z1, p.accent());
        b.fill(x0 - 2, g + 7, z0, x1 + 2, g + 7, z1, p.roofSlab());
        for (int z = z0 + 4; z <= z1 - 4; z += 20) {
            b.fill(postX, g + 2, z, postX, g + 6, z, p.accent());
            lamp(b, postX, g + 5, z, p);
        }
        b.fill(x0 - 2, g + 8, z0, x0 - 2, g + 8, z1, p.rail());
        b.fill(x1 + 2, g + 8, z0, x1 + 2, g + 8, z1, p.rail());
    }

    private static void track(StructureBuilder b, int x0, int z0, int z1, int g, Palette p) {
        b.fill(x0, g, z0, x0 + 9, g, z1, p.rubble());
        for (int z = z0; z <= z1; z++) {
            b.put(x0 + 2, g + 1, z, RAIL);
            b.put(x0 + 7, g + 1, z, RAIL);
        }
    }

    private static void waterTower(StructureBuilder b, int tx, int tz, int g, Palette p) {
        for (int dx = -4; dx <= 4; dx += 8) {
            for (int dz = -4; dz <= 4; dz += 8) {
                b.fill(tx + dx, g + 1, tz + dz, tx + dx, g + 10, tz + dz, p.accent());
            }
        }
        b.fill(tx - 5, g + 9, tz - 5, tx + 5, g + 9, tz + 5, p.foundation());
        for (int y = g + 10; y <= g + 15; y++) {
            ring(b, tx, tz, y, 6, p.wall());
            b.put(tx, y, tz - 6, p.foundation());
            b.put(tx, y, tz + 6, p.foundation());
        }
        disc(b, tx, tz, g + 16, 6, p.foundation());
        for (int i = 0; i <= 3; i++) {
            disc(b, tx, tz, g + 17 + i, 5 - i, p.roof());
        }
        b.put(tx, g + 21, tz, p.light());
        b.fill(tx - 4, g + 1, tz - 4, tx - 4, g + 8, tz - 4, p.rail());
    }

    private static void warehouse(StructureBuilder b, int x0, int z0, int g, Palette p) {
        int x1 = x0 + 30;
        int z1 = z0 + 32;
        b.fill(x0 - 1, g - 1, z0 - 1, x1 + 1, g - 1, z1 + 1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 8, z1, p.wall());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 7, z1 - 1, AIR);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 8, 4, 1);

        int mz = (z0 + z1) / 2;
        b.fill(x1, g + 1, mz - 3, x1, g + 5, mz + 3, AIR);
        b.fill(x1, g + 1, mz - 4, x1, g + 6, mz - 4, p.accent());
        b.fill(x1, g + 1, mz + 4, x1, g + 6, mz + 4, p.accent());
        b.sign(x1, g + 7, mz, Direction.EAST, "GOODS SHED");

        b.fill(x0 + 3, g + 1, z0 + 4, x0 + 9, g + 2, z0 + 10, BARREL);
        b.fill(x0 + 12, g + 1, z1 - 10, x0 + 18, g + 2, z1 - 4, HAY);
        b.fill(x0 + 20, g + 1, z0 + 5, x0 + 24, g + 3, z0 + 9, p.foundation());
        b.put(x0 + 22, g + 4, z0 + 7, p.light());
        b.put(x0 + 2, g + 7, z0 + 2, Palette.cobweb());
    }

    private static void surveyOffice(StructureBuilder b, int x0, int z0, int g, Palette p) {
        int x1 = x0 + 30;
        int z1 = z0 + 30;
        b.fill(x0 - 1, g - 1, z0 - 1, x1 + 1, g - 1, z1 + 1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 7, z1, p.wall());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 6, z1 - 1, AIR);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 7, 3, 0);

        int mx = (x0 + x1) / 2;
        b.fill(mx - 1, g + 1, z1, mx + 1, g + 3, z1, AIR);
        door(b, mx, g + 1, z1, Direction.SOUTH, p);
        b.sign(mx, g + 6, z1, Direction.SOUTH, "SURVEY OFFICE");
        b.fill(x0 + 4, g + 2, z1, x0 + 5, g + 3, z1, p.window());
        b.fill(x1 - 5, g + 2, z1, x1 - 4, g + 3, z1, p.window());

        // Map table, shelves, survey cases and a cot.
        b.fill(x0 + 6, g + 1, z0 + 6, x1 - 8, g + 1, z0 + 10, p.roofSlab());
        b.put(x0 + 8, g + 2, z0 + 7, BOOKS);
        b.put(x1 - 9, g + 2, z0 + 8, BARREL);
        b.fill(x1 - 6, g + 1, z1 - 7, x1 - 3, g + 2, z1 - 4, RED);
        b.put(x0 + 3, g + 2, z0 + 3, p.light());
        b.put(x0 + 3, g + 3, z0 + 3, Palette.cobweb());
    }

    private static void train(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int y = g + 3;
        // Locomotive: wheels, boiler, cab and chimney on track A.
        for (int z = cz + 52; z <= cz + 64; z += 4) {
            b.put(cx + 268, g + 2, z, IRON);
            b.put(cx + 273, g + 2, z, IRON);
        }
        b.fill(cx + 267, y, cz + 50, cx + 275, y + 3, cz + 64, BLACK);
        b.fill(cx + 267, y + 1, cz + 50, cx + 275, y + 1, cz + 60, IRON);
        b.fill(cx + 267, y + 2, cz + 50, cx + 275, y + 2, cz + 50, p.accent());
        b.fill(cx + 267, y, cz + 64, cx + 275, y + 5, cz + 69, BLACK);
        b.fill(cx + 267, y + 3, cz + 66, cx + 267, y + 4, cz + 67, p.window());
        b.fill(cx + 275, y + 3, cz + 66, cx + 275, y + 4, cz + 67, p.window());
        b.fill(cx + 270, y + 4, cz + 52, cx + 272, y + 6, cz + 54, BLACK);
        b.fill(cx + 270, y + 7, cz + 52, cx + 272, y + 7, cz + 54, IRON);
        b.put(cx + 271, y + 2, cz + 69, p.light());

        // Two passenger carriages.
        for (int i = 0; i < 2; i++) {
            int z0 = cz + 74 + i * 16;
            int z1 = z0 + 12;
            b.fill(cx + 267, y, z0, cx + 275, y + 4, z1, p.wall());
            b.fill(cx + 268, y + 1, z0 + 1, cx + 274, y + 3, z1 - 1, AIR);
            b.fill(cx + 267, y + 5, z0, cx + 275, y + 5, z1, p.roofSlab());
            b.fill(cx + 267, y + 1, z0 + 3, cx + 267, y + 2, z0 + 4, p.window());
            b.fill(cx + 275, y + 1, z0 + 3, cx + 275, y + 2, z0 + 4, p.window());
            b.fill(cx + 267, y + 1, z0 + 8, cx + 267, y + 2, z0 + 9, p.window());
            b.fill(cx + 275, y + 1, z0 + 8, cx + 275, y + 2, z0 + 9, p.window());
            b.put(cx + 268, g + 2, z0 + 2, IRON);
            b.put(cx + 273, g + 2, z0 + 2, IRON);
            b.put(cx + 268, g + 2, z1 - 2, IRON);
            b.put(cx + 273, g + 2, z1 - 2, IRON);
        }
    }

    private static void wagon(StructureBuilder b, int x0, int cz0, int g, Palette p) {
        b.fill(x0 + 2, g + 3, cz0, x0 + 9, g + 5, cz0 + 10, p.weathered());
        b.fill(x0 + 3, g + 4, cz0 + 1, x0 + 8, g + 4, cz0 + 9, AIR);
        b.fill(x0 + 2, g + 6, cz0, x0 + 9, g + 6, cz0 + 10, p.roof());
        b.put(x0 + 2, g + 2, cz0 + 2, IRON);
        b.put(x0 + 7, g + 2, cz0 + 2, IRON);
        b.put(x0 + 2, g + 2, cz0 + 8, IRON);
        b.put(x0 + 7, g + 2, cz0 + 8, IRON);
        b.put(x0 + 5, g + 4, cz0 + 5, COAL);
    }

    private static void undercroft(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx + 196;
        int x1 = cx + 244;
        int z0 = cz + 20;
        int z1 = cz + 44;
        int floor = g - 7;
        int ceil = g - 2;

        // Carve the cellar, tunnel and stair shaft.
        b.fill(x0, floor, z0, x1, ceil, z1, AIR);
        b.fill(cx + 216, floor, cz + 44, cx + 224, ceil, cz + 70, AIR);
        b.fill(cx + 217, floor, cz + 70, cx + 223, g + 2, cz + 86, AIR);

        // Cellar shell.
        b.fill(x0, floor - 1, z0, x1, floor - 1, z1, p.foundation());
        b.fill(x0, floor, z0, x1, floor, z1, p.foundation());
        b.walls(x0, floor + 1, z0, x1, ceil, z1, p.wall());
        b.fill(x0, ceil, z0, x1, ceil, z1, p.foundation());

        // Tunnel shell.
        b.fill(cx + 216, floor - 1, cz + 44, cx + 224, floor - 1, cz + 70, p.foundation());
        b.fill(cx + 216, floor, cz + 44, cx + 224, floor, cz + 70, p.foundation());
        b.walls(cx + 216, floor + 1, cz + 44, cx + 224, ceil, cz + 70, p.wall());
        b.fill(cx + 216, ceil, cz + 44, cx + 224, ceil, cz + 70, p.foundation());

        // Openings between cellar, tunnel and stair.
        b.fill(cx + 217, floor + 1, cz + 44, cx + 223, floor + 4, cz + 44, AIR);
        b.fill(cx + 217, floor + 1, cz + 70, cx + 223, floor + 4, cz + 70, AIR);

        // Eight two-deep steps down from the kiosk.
        for (int i = 0; i <= 7; i++) {
            int zs = cz + 84 - i * 2;
            b.fill(cx + 218, floor, zs, cx + 222, g - i, zs + 1, p.foundation());
            b.fill(cx + 218, g - i + 1, zs, cx + 222, g + 2, zs + 1, AIR);
        }
        b.fill(cx + 217, floor + 1, cz + 70, cx + 217, g + 2, cz + 86, p.wall());
        b.fill(cx + 223, floor + 1, cz + 70, cx + 223, g + 2, cz + 86, p.wall());
        b.fill(cx + 215, g + 3, cz + 70, cx + 225, g + 3, cz + 86, p.foundation());
        b.put(cx + 217, g - 1, cz + 76, p.light());
        b.put(cx + 223, g - 1, cz + 82, p.light());

        // Surface kiosk over the stair head.
        b.fill(cx + 214, g, cz + 86, cx + 226, g, cz + 96, p.foundation());
        b.fill(cx + 214, g + 1, cz + 86, cx + 226, g + 3, cz + 96, p.wall());
        b.fill(cx + 215, g + 1, cz + 86, cx + 225, g + 3, cz + 95, AIR);
        b.fill(cx + 217, g + 1, cz + 86, cx + 223, g + 3, cz + 86, AIR);
        b.fill(cx + 214, g + 4, cz + 84, cx + 226, g + 4, cz + 96, p.roof());
        b.fill(cx + 219, g + 1, cz + 96, cx + 221, g + 2, cz + 96, AIR);
        door(b, cx + 220, g + 1, cz + 96, Direction.SOUTH, p);
        b.sign(cx + 220, g + 4, cz + 96, Direction.SOUTH, "UNDERCROFT", "KEEP OUT");

        // Secret west alcove with the relic chest.
        b.fill(cx + 186, floor, cz + 28, cx + 196, ceil, cz + 36, AIR);
        b.fill(cx + 186, floor - 1, cz + 28, cx + 196, floor - 1, cz + 36, p.foundation());
        b.fill(cx + 186, floor, cz + 28, cx + 196, floor, cz + 36, p.foundation());
        b.walls(cx + 186, floor + 1, cz + 28, cx + 196, ceil, cz + 36, p.wall());
        b.fill(cx + 186, ceil, cz + 28, cx + 196, ceil, cz + 36, p.foundation());
        b.fill(cx + 196, floor + 1, cz + 30, cx + 196, floor + 3, cz + 34, AIR);

        // Cellar furniture, crates and the surveyor's abandoned camp.
        b.fill(cx + 200, floor + 1, cz + 24, cx + 204, floor + 2, cz + 24, p.foundation());
        b.put(cx + 202, floor + 3, cz + 24, p.light());
        b.fill(cx + 230, floor + 1, cz + 38, cx + 234, floor + 2, cz + 40, BARREL);
        b.fill(cx + 236, floor + 1, cz + 22, cx + 242, floor + 2, cz + 26, HAY);
        b.put(cx + 238, floor + 1, cz + 40, COAL);
        b.put(cx + 239, floor + 1, cz + 39, COAL);
        b.put(cx + 199, floor + 1, cz + 42, Palette.cobweb());
        b.put(cx + 243, floor + 1, cz + 21, Palette.cobweb());
        b.put(cx + 197, floor + 1, cz + 33, RAIL);
        b.put(cx + 198, floor + 1, cz + 33, RAIL);

        // Loot: the relic and the surveyor's stores.
        b.chest(cx + 190, floor + 1, cz + 32, SiteLoot.RELIC);
        b.chest(cx + 200, floor + 1, cz + 26, SiteLoot.COMMON);
        b.chest(cx + 240, floor + 1, cz + 42, SiteLoot.COMMON);

        // Readable lore and warnings.
        b.sign(cx + 220, floor + 3, cz + 20, Direction.SOUTH,
                "SURVEYOR'S LOG", "THE RELIC CAME", "FROM THE DIG", "DO NOT TELL");
        b.sign(cx + 216, floor + 3, cz + 60, Direction.EAST, "UNDERCROFT", "KEEP OUT");
        b.sign(cx + 224, floor + 3, cz + 54, Direction.WEST, "NO LIGHT", "PAST HERE");
    }

    // ------------------------------------------------------------------ colonial club

    private static void colonialClub(StructureBuilder b, int cx, int cz,
                                     IntBinaryOperator surface, Palette p) {
        // Lawns, kept clear of the avenue and the church walk.
        surfacePad(b, cx + 145, cz + 160, cx + 205, cz + 230, surface, GRASS);
        surfacePad(b, cx + 280, cz + 160, cx + 320, cz + 230, surface, GRASS);
        surfacePad(b, cx + 145, cz + 248, cx + 177, cz + 266, surface, GRASS);
        surfacePad(b, cx + 183, cz + 248, cx + 205, cz + 266, surface, GRASS);
        surfacePad(b, cx + 145, cz + 268, cx + 205, cz + 305, surface, GRASS);
        surfacePad(b, cx + 280, cz + 248, cx + 320, cz + 305, surface, GRASS);
        surfacePad(b, cx + 205, cz + 268, cx + 280, cz + 305, surface, GRASS);

        clubHouse(b, cx + 210, cz + 218, cx + 272, cz + 262,
                surface.applyAsInt(cx + 210, cz + 240), p);
        bungalow(b, cx + 150, cz + 168, surface.applyAsInt(cx + 163, cz + 188), p,
                Direction.SOUTH);
        bungalow(b, cx + 150, cz + 198, surface.applyAsInt(cx + 163, cz + 218), p,
                Direction.SOUTH);
        bungalow(b, cx + 286, cz + 170, surface.applyAsInt(cx + 286, cz + 180), p,
                Direction.WEST);
        bungalow(b, cx + 286, cz + 200, surface.applyAsInt(cx + 286, cz + 210), p,
                Direction.WEST);
        bungalow(b, cx + 286, cz + 262, surface.applyAsInt(cx + 286, cz + 272), p,
                Direction.WEST);
        church(b, cx + 160, cz + 266, surface.applyAsInt(cx + 180, cz + 266), p);

        // Garden lamps, hedges and flowers.
        for (int z = cz + 170; z <= cz + 220; z += 25) {
            lamp(b, cx + 208, surface.applyAsInt(cx + 208, z) + 1, z, p);
            lamp(b, cx + 278, surface.applyAsInt(cx + 278, z) + 1, z, p);
        }
        surfaceLine(b, cx + 146, cz + 232, cx + 204, cz + 232, surface, p.rail());
        surfaceLine(b, cx + 280, cz + 232, cx + 318, cz + 232, surface, p.rail());
        b.put(cx + 184, surface.applyAsInt(cx + 184, cz + 176) + 1, cz + 176, FLOWER);
        b.put(cx + 188, surface.applyAsInt(cx + 188, cz + 182) + 1, cz + 182, FLOWER);
        b.put(cx + 316, surface.applyAsInt(cx + 316, cz + 192) + 1, cz + 192, FLOWER);
        b.put(cx + 318, surface.applyAsInt(cx + 318, cz + 216) + 1, cz + 216, FLOWER);
        b.put(cx + 210, surface.applyAsInt(cx + 210, cz + 290) + 1, cz + 290, TURF);
        b.put(cx + 216, surface.applyAsInt(cx + 216, cz + 294) + 1, cz + 294, TURF);

        signpost(b, cx + 202, surface.applyAsInt(cx + 202, cz + 246) + 1, cz + 246,
                Direction.NORTH, p, "COLONIAL CLUB", "MEMBERS ONLY", "RELIC NOTICES");
    }

    private static void clubHouse(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  int g, Palette p) {
        b.fill(x0 - 1, g - 1, z0 - 1, x1 + 1, g - 1, z1 + 1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 10, z1, p.wall());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 9, z1 - 1, AIR);
        b.fill(x0 + 1, g + 5, z0 + 1, x1 - 1, g + 5, z1 - 1, p.foundation());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 10, 5, 0);

        int mz = (z0 + z1) / 2;
        // West entrance with a verandah and a pediment.
        b.fill(x0, g + 1, mz - 1, x0, g + 3, mz + 1, AIR);
        door(b, x0, g + 1, mz, Direction.WEST, p);
        b.fill(x0 - 4, g + 5, z0 + 3, x0 - 1, g + 5, z1 - 3, p.roofSlab());
        for (int z = z0 + 4; z <= z1 - 4; z += 8) {
            if (Math.abs(z - mz) < 6) {
                continue; // leave the avenue and the entrance clear
            }
            b.fill(x0 - 4, g + 1, z, x0 - 4, g + 4, z, p.accent());
        }
        b.fill(x0 - 2, g + 6, mz - 4, x0 - 1, g + 6, mz + 4, p.accent());
        b.sign(x0, g + 8, mz, Direction.WEST, "COLONIAL CLUB");

        // Windows to the lawn.
        for (int z = z0 + 6; z <= z1 - 6; z += 8) {
            if (Math.abs(z - mz) < 5) {
                continue;
            }
            b.fill(x0, g + 2, z, x0, g + 3, z + 1, p.window());
            b.fill(x0, g + 6, z, x0, g + 7, z + 1, p.window());
        }
        b.fill(x1, g + 2, z0 + 8, x1, g + 3, z0 + 10, p.window());
        b.fill(x1, g + 2, z1 - 10, x1, g + 3, z1 - 8, p.window());

        // Bar, lounge, library and billiard table.
        b.fill(x0 + 2, g + 1, z0 + 2, x0 + 12, g + 2, z0 + 3, p.foundation());
        b.put(x0 + 4, g + 3, z0 + 2, BARREL);
        b.put(x0 + 8, g + 3, z0 + 2, BARREL);
        b.fill(x0 + 4, g + 1, z1 - 6, x0 + 8, g + 1, z1 - 4, p.roofSlab());
        b.fill(x1 - 10, g + 1, z0 + 2, x1 - 2, g + 2, z0 + 2, BOOKS);
        b.fill(x0 + 20, g + 1, z0 + 8, x0 + 26, g + 2, z0 + 12, GREEN);
        b.put(x0 + 23, g + 3, z0 + 10, WHITE);
        lamp(b, x0 + 6, g + 1, z0 + 4, p);
        lamp(b, x1 - 4, g + 1, z1 - 4, p);
        b.put(x1 - 2, g + 9, z1 - 2, Palette.cobweb());
    }

    private static void bungalow(StructureBuilder b, int x0, int z0, int g,
                                 Palette p, Direction front) {
        int x1 = x0 + 26;
        int z1 = z0 + 20;
        b.fill(x0 - 1, g - 1, z0 - 1, x1 + 1, g - 1, z1 + 1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 5, z1, p.wall());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 4, z1 - 1, AIR);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 5, 4, 0);

        int dx = (x0 + x1) / 2;
        int dz = (z0 + z1) / 2;
        int doorX;
        int doorZ;
        if (front == Direction.SOUTH) {
            doorX = dx;
            doorZ = z1;
        } else if (front == Direction.NORTH) {
            doorX = dx;
            doorZ = z0;
        } else if (front == Direction.WEST) {
            doorX = x0;
            doorZ = dz;
        } else {
            doorX = x1;
            doorZ = dz;
        }
        b.fill(doorX, g + 1, doorZ, doorX, g + 2, doorZ, AIR);
        door(b, doorX, g + 1, doorZ, front, p);
        b.fill(doorX - 4, g + 1, doorZ, doorX - 4, g + 4, doorZ, p.accent());
        b.fill(doorX + 4, g + 1, doorZ, doorX + 4, g + 4, doorZ, p.accent());
        b.fill(doorX - 4, g + 4, doorZ, doorX + 4, g + 4, doorZ, p.roofSlab());

        if (front == Direction.SOUTH || front == Direction.NORTH) {
            b.fill(doorX - 9, g + 2, doorZ, doorX - 8, g + 3, doorZ, p.window());
            b.fill(doorX + 8, g + 2, doorZ, doorX + 9, g + 3, doorZ, p.window());
        } else {
            b.fill(doorX, g + 2, doorZ - 7, doorX, g + 3, doorZ - 6, p.window());
            b.fill(doorX, g + 2, doorZ + 6, doorX, g + 3, doorZ + 7, p.window());
        }

        b.fill(x0 + 3, g + 1, z0 + 3, x0 + 7, g + 1, z0 + 6, p.roofSlab());
        b.fill(x1 - 6, g + 1, z1 - 6, x1 - 3, g + 2, z1 - 4, RED);
        b.put(x0 + 4, g + 3, z0 + 4, p.light());
        b.put(x0 + 2, g + 1, z1 - 2, BARREL);
        b.put(x1 - 2, g + 1, z0 + 2, TURF);
    }

    private static void church(StructureBuilder b, int x0, int z0, int g, Palette p) {
        int x1 = x0 + 40;
        int z1 = z0 + 30;
        b.fill(x0 - 1, g - 1, z0 - 1, x1 + 1, g - 1, z1 + 1, p.foundation());
        b.fill(x0, g, z0, x1, g, z1, p.ground());

        // Nave.
        int nx0 = x0 + 8;
        int nx1 = x1 - 8;
        b.walls(nx0, g + 1, z0 + 4, nx1, g + 8, z1 - 2, p.wall());
        b.fill(nx0 + 1, g + 1, z0 + 5, nx1 - 1, g + 7, z1 - 3, AIR);
        b.fill(nx0, g, z0 + 4, nx1, g, z1 - 2, p.ground());
        b.pitchedRoof(nx0 - 1, z0 + 3, nx1 + 1, z1 - 1, g + 8, 5, 0);

        for (int z = z0 + 7; z <= z1 - 5; z += 6) {
            b.fill(nx0, g + 3, z, nx0, g + 5, z + 1, p.window());
            b.fill(nx1, g + 3, z, nx1, g + 5, z + 1, p.window());
        }

        // North tower, spire and cross.
        int tx = x0 + 20;
        int tz = z0 + 4;
        b.fill(tx - 4, g, tz - 4, tx + 4, g + 1, tz + 4, p.foundation());
        b.walls(tx - 4, g + 1, tz - 4, tx + 4, g + 20, tz + 4, p.wall());
        b.fill(tx - 3, g + 1, tz - 3, tx + 3, g + 19, tz + 3, AIR);
        b.fill(tx - 1, g + 1, tz - 4, tx + 1, g + 3, tz - 4, AIR);
        door(b, tx, g + 1, tz - 4, Direction.NORTH, p);
        b.fill(tx - 4, g + 14, tz - 4, tx + 4, g + 14, tz + 4, p.accent());
        b.fill(tx - 2, g + 6, tz - 4, tx + 2, g + 10, tz - 4, p.window());
        b.sign(tx, g + 12, tz - 4, Direction.NORTH, "ST ANDREW");
        b.spire(tx, tz, g + 21, 10);
        b.fill(tx, g + 32, tz, tx, g + 35, tz, p.accent());
        b.fill(tx - 2, g + 33, tz, tx + 2, g + 33, tz, p.accent());

        // Pews, altar and a small graveyard east of the nave.
        b.fill(nx0 + 3, g + 1, z0 + 10, nx0 + 6, g + 1, z0 + 12, p.roofSlab());
        b.fill(nx1 - 6, g + 1, z0 + 10, nx1 - 3, g + 1, z0 + 12, p.roofSlab());
        b.fill(nx0 + 2, g + 1, z1 - 5, nx1 - 2, g + 2, z1 - 4, p.foundation());
        b.put(tx, g + 3, z1 - 4, p.light());
        for (int i = 0; i < 4; i++) {
            int gx = x1 - 3;
            int gz = z0 + 8 + i * 4;
            b.put(gx, g + 1, gz, p.roofSlab());
            b.put(gx, g + 2, gz, p.accent());
        }
        acacia(b, x0 - 4, z1 + 4, g);
    }

    // ------------------------------------------------------------------ outskirts

    private static void outskirts(StructureBuilder b, int cx, int cz,
                                  IntBinaryOperator surface, Palette p) {
        int[] gardenX = {-308, -282, -256, -230, -204};
        int[] gardenZ = {242, 260, 276, 292};
        for (int x : gardenX) {
            for (int z : gardenZ) {
                gardenPlot(b, cx + x, cz + z, 18, 12,
                        surface.applyAsInt(cx + x + 9, cz + z + 6), p);
            }
        }

        kraalRuin(b, cx - 115, cz + 285, surface.applyAsInt(cx - 115, cz + 285), p);
        waterhole(b, cx + 52, cz + 256, cx + 68, cz + 268,
                surface.applyAsInt(cx + 60, cz + 262), p);

        int[][] trees = {
                {24, 256}, {40, 266}, {56, 252}, {72, 300}, {88, 262}, {104, 294},
                {120, 256}, {128, 286}, {36, 294}, {96, 250}, {112, 272}, {24, 296}
        };
        for (int[] tree : trees) {
            acacia(b, cx + tree[0], cz + tree[1],
                    surface.applyAsInt(cx + tree[0], cz + tree[1]));
        }

        // Dry scrub along the lane edges.
        for (int x = -300; x <= -40; x += 18) {
            b.put(cx + x, surface.applyAsInt(cx + x, cz + 238) + 1, cz + 238, BUSH);
            b.put(cx + x + 6, surface.applyAsInt(cx + x + 6, cz + 306) + 1, cz + 306, BUSH);
        }

        signpost(b, cx - 20, surface.applyAsInt(cx - 20, cz + 300) + 1, cz + 300,
                Direction.SOUTH, p, "OUTSKIRTS", "MARKET GARDENS", "ACACIA GROVE");
        signpost(b, cx - 182, surface.applyAsInt(cx - 182, cz + 262) + 1, cz + 262,
                Direction.SOUTH, p, "MARKET GARDENS");
        signpost(b, cx - 90, surface.applyAsInt(cx - 90, cz + 300) + 1, cz + 300,
                Direction.SOUTH, p, "OLD KRAAL");
    }

    private static void gardenPlot(StructureBuilder b, int x0, int z0, int w, int d,
                                   int g, Palette p) {
        int x1 = x0 + w - 1;
        int z1 = z0 + d - 1;
        b.fill(x0, g, z0, x1, g, z1, p.foundation());
        b.fill(x0 + 1, g, z0 + 1, x1 - 1, g, z1 - 1, FARMLAND);
        int wz = z0 + d / 2;
        b.fill(x0 + 1, g, wz, x1 - 1, g, wz, WATER);
        for (int x = x0 + 1; x <= x1 - 1; x++) {
            for (int z = z0 + 1; z <= z1 - 1; z++) {
                if (z != wz) {
                    b.put(x, g + 1, z, WHEAT);
                }
            }
        }
        b.fill(x0, g + 1, z0, x0, g + 2, z0, p.rail());
        b.fill(x1, g + 1, z1, x1, g + 2, z1, p.rail());
        b.put(x1 - 1, g + 1, z0, HAY);
        b.put(x1 - 1, g + 2, z0, p.rail());
        b.put(x0 + 2, g + 1, z1 - 1, TURF);
    }

    private static void kraalRuin(StructureBuilder b, int cx, int cz, int g, Palette p) {
        for (int dx = -14; dx <= 14; dx++) {
            for (int dz = -14; dz <= 14; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > 196 || d2 <= 169) {
                    continue;
                }
                if ((dx > 8 && dz > 4) || (dx < -9 && dz < -3)) {
                    continue; // authored breaches
                }
                b.put(cx + dx, g + 1, cz + dz, p.weathered());
                if ((dx + dz) % 3 != 0) {
                    b.put(cx + dx, g + 2, cz + dz, p.weathered());
                }
            }
        }
        b.fill(cx - 6, g, cz - 2, cx + 6, g, cz + 8, p.ground());
        b.fill(cx - 5, g + 1, cz + 1, cx - 2, g + 2, cz + 3, p.rubble());
        b.fill(cx + 2, g + 1, cz + 4, cx + 5, g + 1, cz + 6, p.rubble());
        b.put(cx - 10, g + 1, cz + 10, BUSH);
        b.put(cx + 10, g + 1, cz - 8, BUSH);
        b.put(cx + 2, g + 1, cz - 10, HAY);
        b.put(cx + 3, g + 1, cz - 10, HAY);
        b.fill(cx - 2, g + 1, cz + 6, cx + 2, g + 2, cz + 6, p.weathered());
        b.put(cx, g + 1, cz + 9, p.light());
    }

    private static void waterhole(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  int g, Palette p) {
        b.fill(x0 - 1, g - 2, z0 - 1, x1 + 1, g - 2, z1 + 1, p.foundation());
        b.fill(x0, g - 1, z0, x1, g - 1, z1, WATER);
        b.walls(x0 - 1, g, z0 - 1, x1 + 1, g, z1 + 1, p.foundation());
        b.put(x0 + 3, g, z0 + 3, PAD);
        b.put(x1 - 3, g, z0 + 5, PAD);
        b.put(x0 + 5, g, z1 - 3, PAD);
        b.put(x0 - 1, g + 1, z0 - 1, TURF);
        b.put(x1 + 1, g + 1, z1 + 1, TURF);
        b.put(x1 + 1, g + 1, z0 - 1, BUSH);
    }

    private static void acacia(StructureBuilder b, int x, int z, int g) {
        for (int y = g; y < g + 4; y++) {
            b.put(x, y, z, LOG);
        }
        int top = g + 4;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 4) {
                    b.put(x + dx, top, z + dz, LEAF);
                }
            }
        }
        b.fill(x - 1, top + 1, z - 1, x + 1, top + 1, z + 1, LEAF);
        b.put(x - 2, top, z, LOG);
        b.put(x + 2, top, z, LOG);
        b.put(x, top, z - 2, LOG);
        b.put(x, top, z + 2, LOG);
    }

    // ------------------------------------------------------------------ shared forms

    private static void path(StructureBuilder b, int x0, int z0, int x1, int z1, int width,
                             IntBinaryOperator surface, BlockState state) {
        int w = Math.max(1, width);
        int lo = -(w - 1) / 2;
        int hi = w / 2;
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        if (bx - ax >= bz - az) {
            int cz = (z0 + z1) / 2;
            for (int x = ax; x <= bx; x++) {
                for (int z = cz + lo; z <= cz + hi; z++) {
                    b.put(x, surface.applyAsInt(x, z), z, state);
                }
            }
        } else {
            int cx = (x0 + x1) / 2;
            for (int z = az; z <= bz; z++) {
                for (int x = cx + lo; x <= cx + hi; x++) {
                    b.put(x, surface.applyAsInt(x, z), z, state);
                }
            }
        }
    }

    private static void surfacePad(StructureBuilder b, int x0, int z0, int x1, int z1,
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

    private static void surfaceApron(StructureBuilder b, int x0, int z0, int x1, int z1,
                                     IntBinaryOperator surface, Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                int y = surface.applyAsInt(x, z);
                b.put(x, y - 1, z, p.foundation());
                b.put(x, y, z, p.ground());
            }
        }
    }

    private static void surfaceLine(StructureBuilder b, int x0, int z0, int x1, int z1,
                                    IntBinaryOperator surface, BlockState state) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                b.put(x, surface.applyAsInt(x, z) + 1, z, state);
            }
        }
    }

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

    private static void flagpole(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x - 1, g, z - 1, x + 1, g, z + 1, p.foundation());
        b.fill(x, g + 1, z, x, g + 7, z, p.accent());
        b.fill(x + 1, g + 7, z, x + 2, g + 7, z, WHITE);
        b.put(x + 1, g + 6, z, WHITE);
    }

    private static void disc(StructureBuilder b, int cx, int cz, int y, int r, BlockState state) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz <= r * r) {
                    b.put(cx + dx, y, cz + dz, state);
                }
            }
        }
    }

    private static void ring(StructureBuilder b, int cx, int cz, int y, int r, BlockState state) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= r * r && d2 > (r - 1) * (r - 1)) {
                    b.put(cx + dx, y, cz + dz, state);
                }
            }
        }
    }

    private static void ringBand(StructureBuilder b, int cx, int cz, int y0, int y1, int r,
                                 Direction side, BlockState state) {
        for (int y = y0; y <= y1; y++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > r * r || d2 <= (r - 1) * (r - 1)) {
                        continue;
                    }
                    boolean front;
                    if (side == Direction.NORTH) {
                        front = dz < 0 && Math.abs(dx) <= 3;
                    } else if (side == Direction.SOUTH) {
                        front = dz > 0 && Math.abs(dx) <= 3;
                    } else if (side == Direction.EAST) {
                        front = dx > 0 && Math.abs(dz) <= 3;
                    } else {
                        front = dx < 0 && Math.abs(dz) <= 3;
                    }
                    if (front) {
                        b.put(cx + dx, y, cz + dz, state);
                    }
                }
            }
        }
    }
}
