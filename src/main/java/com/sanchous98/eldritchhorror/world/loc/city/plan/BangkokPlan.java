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
import org.jspecify.annotations.Nullable;

/**
 * Authored 1920s Bangkok: the Grand Palace and its ordination hall, the klong water market,
 * Chinatown's shophouse terraces, the backpacker guesthouse street, and the Chao Phraya with its
 * ferry and river shrine.
 *
 * <p>Everything is placed deliberately through {@link StructureBuilder}: no layout RNG, no chunk
 * reads, no static mutable state. Coordinates are offsets from {@code (cx, ground, cz)}; the
 * streets and quays are authored, and the gaps between buildings are gardens, courtyards or
 * water rather than filler.
 */
public final class BangkokPlan implements CityPlan {

    /** Authored half-extent: 680 blocks across. */
    private static final int HALF = 340;

    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState GOLD = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final BlockState LILY = Blocks.LILY_PAD.defaultBlockState();
    private static final BlockState BAMBOO = Blocks.BAMBOO.defaultBlockState();
    private static final BlockState LEAVES = Blocks.JUNGLE_LEAVES.defaultBlockState();
    private static final BlockState RED_WOOL = Blocks.WOOL.pick(DyeColor.RED).defaultBlockState();
    private static final BlockState ORANGE_WOOL = Blocks.WOOL.pick(DyeColor.ORANGE).defaultBlockState();
    private static final BlockState HAY = Blocks.HAY_BLOCK.defaultBlockState();
    private static final BlockState SHROOM = Blocks.SHROOMLIGHT.defaultBlockState();
    private static final BlockState BOOKS = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState BARREL = Blocks.BARREL.defaultBlockState();
    private static final BlockState RAIL = Blocks.RAIL.defaultBlockState()
            .setValue(BlockStateProperties.RAIL_SHAPE, RailShape.EAST_WEST);

    @Override
    public String cityId() {
        return "bangkok";
    }

    @Override
    public int halfExtent() {
        return HALF;
    }

    @Override
    public void build(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        buildCore(b, cx, cz, ground, p);
        railStation(b, cx, cz, ground, p, null);
    }

    @Override
    public void build(StructureBuilder b, CityPlanContext ctx, Palette p) {
        buildCore(b, ctx.cx(), ctx.cz(), ctx.ground(), p);
        railStation(b, ctx.cx(), ctx.cz(), ctx.ground(), p, ctx.surface());
    }

    private static void buildCore(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        river(b, cx, cz, ground, p);
        canals(b, cx, cz, ground, p);
        streets(b, cx, cz, ground, p);
        arrivalGate(b, cx, cz, ground, p);
        palace(b, cx, cz, ground, p);
        templeGarden(b, cx, cz, ground, p);
        klongMarket(b, cx, cz, ground, p);
        chinatown(b, cx, cz, ground, p);
        guesthouseStreet(b, cx, cz, ground, p);
        riverbank(b, cx, cz, ground, p);
        riverPort(b, cx, cz, ground, p);
        b.marker("bangkok_center", cx, ground + 1, cz);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("klong_quest_board", CityPoi.Kind.QUEST_BOARD, -220, 3, 0,
                        "bangkok_stolen_amulet;bangkok_river_spirit"),
                new CityPoi("guesthouse", CityPoi.Kind.INN, 220, 1, 160, "guesthouse_keeper"),
                new CityPoi("gold_shop", CityPoi.Kind.SHOP, 220, 1, -80, "gold_merchant"),
                new CityPoi("monk_healer", CityPoi.Kind.HEALER, -60, 1, -120, "monk"),
                new CityPoi("amulet_stall", CityPoi.Kind.SHOP, -240, 1, -120,
                        "bangkok_stolen_amulet"),
                new CityPoi("river_shrine", CityPoi.Kind.LANDMARK, 0, 1, 215,
                        "bangkok_river_spirit"),
                new CityPoi("warehouse", CityPoi.Kind.SECRET, 260, 1, 40, "smuggled_cargo"),
                new CityPoi("rail_station", CityPoi.Kind.STATION, 315, 1, 75,
                        "rail_yard;track_east;east_west"),
                new CityPoi("river_port", CityPoi.Kind.PORT, 280, 1, 200,
                        "chao_phraya;cargo_quay"),
                new CityPoi("temple_crypt", CityPoi.Kind.SECRET, 60, 4, -20, "crypt_lore"),
                new CityPoi("palace_gate", CityPoi.Kind.LANDMARK, 0, 1, -80, "palace_guard"),
                new CityPoi("amulet_seller", CityPoi.Kind.NPC, -238, 0, -120,
                        "bangkok_stolen_amulet"),
                new CityPoi("boatman", CityPoi.Kind.NPC, 4, 1, 215, "bangkok_river_spirit"),
                new CityPoi("guesthouse_keeper", CityPoi.Kind.NPC, 216, 1, 160,
                        "guesthouse_keeper"),
                new CityPoi("gold_merchant", CityPoi.Kind.NPC, 216, 1, -80, "gold_merchant"),
                new CityPoi("monk", CityPoi.Kind.NPC, -58, 1, -120, "monk_healer"),
                new CityPoi("dockworker", CityPoi.Kind.NPC, 280, 0, 170, "river_warehouse"),
                new CityPoi("palace_guard", CityPoi.Kind.NPC, 5, 0, -78, "palace_guard"),
                new CityPoi("arrival_sign", CityPoi.Kind.SIGN, 8, 2, 314, "arrival"),
                new CityPoi("klong_sign", CityPoi.Kind.SIGN, -220, 4, 6, "klong"),
                new CityPoi("chinatown_sign", CityPoi.Kind.SIGN, 145, 8, -40, "chinatown"),
                new CityPoi("river_warning", CityPoi.Kind.SIGN, 7, 2, 194, "river"),
                new CityPoi("guesthouse_sign", CityPoi.Kind.SIGN, 231, 4, 83, "guesthouses"));
    }

    // ------------------------------------------------------------------ water

    /** The Chao Phraya: a broad river across the south, one course below the city street. */
    private static void river(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 330;
        int x1 = cx + 330;
        int z0 = cz + 200;
        int z1 = cz + 230;
        b.fill(x0, g - 5, z0, x1, g - 5, z1, p.foundation());
        b.fill(x0, g - 4, z0, x1, g - 1, z1, WATER);
        b.fill(x0, g - 5, z0 - 1, x1, g + 1, z0 - 1, p.foundation());
        b.fill(x0, g - 5, z1 + 1, x1, g + 1, z1 + 1, p.foundation());
    }

    /** The klong market's canals: one long north-south lane and two side lanes. */
    private static void canals(StructureBuilder b, int cx, int cz, int g, Palette p) {
        canalNS(b, cx, cz, g, p, -210, -250, 199);
        canalEW(b, cx, cz, g, p, -60, -300, -214);
        canalEW(b, cx, cz, g, p, 100, -300, -214);
        // Open the river quay where the main klong meets the Chao Phraya.
        b.fill(cx - 215, g - 1, cz + 199, cx - 205, g, cz + 199, WATER);
        b.fill(cx - 215, g + 1, cz + 199, cx - 205, g + 1, cz + 199, AIR);
        // Open the main canal's west quay at both side-canal mouths.
        b.fill(cx - 215, g - 1, cz - 64, cx - 215, g, cz - 56, WATER);
        b.fill(cx - 215, g + 1, cz - 64, cx - 215, g + 1, cz - 56, AIR);
        b.fill(cx - 215, g - 1, cz + 96, cx - 215, g, cz + 104, WATER);
        b.fill(cx - 215, g + 1, cz + 96, cx - 215, g + 1, cz + 104, AIR);
    }

    private static void canalNS(StructureBuilder b, int cx, int cz, int g, Palette p,
                                int dx, int zFrom, int zTo) {
        int x0 = cx + dx - 4;
        int x1 = cx + dx + 3;
        int z0 = cz + zFrom;
        int z1 = cz + zTo;
        b.fill(x0, g - 2, z0, x1, g - 2, z1, p.foundation());
        b.fill(x0, g - 1, z0, x1, g, z1, WATER);
        b.fill(x0 - 1, g - 2, z0, x0 - 1, g + 1, z1, p.foundation());
        b.fill(x1 + 1, g - 2, z0, x1 + 1, g + 1, z1, p.foundation());
    }

    private static void canalEW(StructureBuilder b, int cx, int cz, int g, Palette p,
                                int dz, int xFrom, int xTo) {
        int z0 = cz + dz - 4;
        int z1 = cz + dz + 3;
        int x0 = cx + xFrom;
        int x1 = cx + xTo;
        b.fill(x0, g - 2, z0, x1, g - 2, z1, p.foundation());
        b.fill(x0, g - 1, z0, x1, g, z1, WATER);
        b.fill(x0, g - 2, z0 - 1, x1, g + 1, z0 - 1, p.foundation());
        b.fill(x0, g - 2, z1 + 1, x1, g + 1, z1 + 1, p.foundation());
    }

    // ------------------------------------------------------------------ streets

    private static void streets(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // Palace avenue, split at the river and its ferry crossing.
        CityPlanSupport.path(b, cx, cz - 80, cx, cz + 199, 12, g, p.foundation());
        CityPlanSupport.path(b, cx, cz + 231, cx, cz + 320, 12, g, p.foundation());
        CityPlanSupport.path(b, cx, cz - 80, cx, cz + 199, 2, g, p.ground());
        CityPlanSupport.path(b, cx, cz + 231, cx, cz + 320, 2, g, p.ground());

        // River road on the north bank, broken at the klong mouth.
        CityPlanSupport.path(b, cx - 320, cz + 190, cx - 218, cz + 190, 8, g, p.ground());
        CityPlanSupport.path(b, cx - 202, cz + 190, cx + 320, cz + 190, 8, g, p.ground());
        // South bank road and the arrival approach.
        CityPlanSupport.path(b, cx - 320, cz + 240, cx + 320, cz + 240, 8, g, p.ground());

        // Klong boardwalk lanes, in three pieces so the side canals stay open.
        CityPlanSupport.path(b, cx - 251, cz - 250, cx - 251, cz - 66, 6, g, p.frame());
        CityPlanSupport.path(b, cx - 251, cz - 54, cx - 251, cz + 94, 6, g, p.frame());
        CityPlanSupport.path(b, cx - 251, cz + 106, cx - 251, cz + 190, 6, g, p.frame());
        CityPlanSupport.path(b, cx - 169, cz - 250, cx - 169, cz + 190, 6, g, p.frame());
        // Lane along the palace's west wall, plus the west gate spur.
        CityPlanSupport.path(b, cx - 155, cz - 250, cx - 155, cz + 190, 6, g, p.ground());
        CityPlanSupport.path(b, cx - 155, cz - 150, cx - 140, cz - 150, 6, g, p.ground());

        // Chinatown lanes: two north-south, three east-west.
        CityPlanSupport.path(b, cx + 180, cz - 218, cx + 180, cz + 57, 6, g, p.foundation());
        CityPlanSupport.path(b, cx + 250, cz - 218, cx + 250, cz + 76, 6, g, p.foundation());
        CityPlanSupport.path(b, cx + 140, cz - 160, cx + 320, cz - 160, 6, g, p.foundation());
        CityPlanSupport.path(b, cx + 140, cz - 40, cx + 320, cz - 40, 6, g, p.foundation());
        CityPlanSupport.path(b, cx + 140, cz + 20, cx + 320, cz + 20, 6, g, p.foundation());

        // Guesthouse street, and its link north into Chinatown.
        CityPlanSupport.path(b, cx + 226, cz + 80, cx + 226, cz + 199, 10, g, p.ground());
        CityPlanSupport.path(b, cx + 226, cz + 76, cx + 250, cz + 76, 6, g, p.ground());
    }

    // ------------------------------------------------------------------ arrival

    private static void arrivalGate(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int z = cz + 320;
        for (int s = -1; s <= 1; s += 2) {
            int x = cx + s * 10;
            b.fill(x - 3, g, z - 4, x + 3, g, z + 4, p.foundation());
            b.fill(x - 3, g + 1, z - 4, x + 3, g + 10, z + 4, p.wall());
            b.fill(x - 3, g + 4, z - 4, x + 3, g + 4, z + 4, p.accent());
            b.fill(x - 3, g + 9, z - 4, x + 3, g + 9, z + 4, p.accent());
            b.fill(x - 2, g + 11, z - 3, x + 2, g + 11, z + 3, p.roof());
            b.fill(x - 1, g + 12, z - 2, x + 1, g + 12, z + 2, p.roofSlab());
            b.put(x, g + 13, z, GOLD);
            b.put(x, g + 14, z, p.light());
        }
        b.fill(cx - 10, g + 8, z - 2, cx + 10, g + 9, z + 2, p.accent());
        b.pitchedRoof(cx - 13, z - 5, cx + 13, z + 5, g + 10, 4, 0);
        CityPlanSupport.sign(b, cx, g + 9, z - 2, Direction.SOUTH, "BANGKOK", "ARRIVAL GATE");

        signPost(b, cx + 8, z - 6, g, Direction.SOUTH, p,
                "GRAND PALACE", "KLONG MARKET", "CHINATOWN", "GUESTHOUSES");
        signPost(b, cx - 8, z - 6, g, Direction.SOUTH, p,
                "RIVER FERRY", "CITY NOTICES", "TEMPLE GARDEN", "RIVER WARNING");
        noticeBoard(b, cx + 18, z - 8, g, Direction.SOUTH, p,
                "CITY NOTICES", "STOLEN AMULET", "RIVER SPIRIT", "SEEK THE KLONG");

        lanternPost(b, cx + 14, z - 2, g, p);
        lanternPost(b, cx - 14, z - 2, g, p);
        // A small guard shelter beside the gate.
        b.fill(cx + 16, g + 1, z + 2, cx + 22, g + 3, z + 8, p.wall());
        b.fill(cx + 17, g + 1, z + 3, cx + 21, g + 2, z + 7, AIR);
        b.fill(cx + 16, g + 1, z + 4, cx + 16, g + 2, z + 6, AIR);
        b.pitchedRoof(cx + 15, z + 1, cx + 23, z + 9, g + 4, 2, 0);
        b.put(cx + 19, g + 2, z + 5, p.light());
    }

    private static void signPost(StructureBuilder b, int x, int z, int g, Direction facing,
                                 Palette p, String... lines) {
        b.put(x, g + 1, z, p.frame());
        CityPlanSupport.sign(b, x, g + 2, z, facing, lines);
    }

    /** A framed notice board on two posts; {@code y} is the level it stands on. */
    private static void noticeBoard(StructureBuilder b, int x, int z, int y, Direction facing,
                                    Palette p, String... lines) {
        if (facing == Direction.EAST) {
            b.fill(x, y + 1, z - 3, x, y + 4, z + 3, p.frame());
            b.fill(x, y + 2, z - 2, x, y + 3, z + 2, p.wall());
            b.fill(x, y + 5, z - 3, x, y + 5, z + 3, p.roofSlab());
            b.put(x, y + 1, z, p.frame());
            CityPlanSupport.sign(b, x, y + 2, z, facing, lines);
        } else {
            b.fill(x - 3, y + 1, z, x + 3, y + 4, z, p.frame());
            b.fill(x - 2, y + 2, z, x + 2, y + 3, z, p.wall());
            b.fill(x - 3, y + 5, z, x + 3, y + 5, z, p.roofSlab());
            b.put(x, y + 1, z, p.frame());
            CityPlanSupport.sign(b, x, y + 2, z, facing, lines);
        }
    }

    // ------------------------------------------------------------------ palace

    private static void palace(StructureBuilder b, int cx, int cz, int g, Palette p) {
        palaceWall(b, cx, cz, g, p);
        CityPlanSupport.path(b, cx, cz - 166, cx, cz - 80, 16, g, p.foundation());
        CityPlanSupport.path(b, cx - 70, cz - 200, cx + 70, cz - 200, 10, g, p.foundation());
        gatePavilion(b, cx, cz - 80, g, p);
        ordinationHall(b, cx, cz - 200, g, p);
        stupa(b, cx - 80, cz - 200, g, p, 1);
        stupa(b, cx + 80, cz - 200, g, p, 1);
        stupa(b, cx - 80, cz - 110, g, p, 0);
        stupa(b, cx + 80, cz - 110, g, p, 0);
        palaceHall(b, cx - 95, cz - 145, g, p, 44, 26, 12, 0);
        palaceHall(b, cx + 95, cz - 145, g, p, 40, 24, 11, 1);
        palaceHall(b, cx, cz - 272, g, p, 30, 18, 9, 2);
        monkQuarters(b, cx - 55, cz - 120, g, p);
        palaceGarden(b, cx - 60, cz - 255, g, p);
        palaceGarden(b, cx + 60, cz - 255, g, p);
        statue(b, cx - 14, cz - 82, g, p);
        statue(b, cx + 14, cz - 82, g, p);
    }

    private static void palaceWall(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 140;
        int x1 = cx + 140;
        int z0 = cz - 300;
        int z1 = cz - 80;
        b.walls(x0, g + 1, z0, x1, g + 4, z1, p.wall());
        b.crenellations(x0, z0, x1, z1, g + 5);
        b.fill(cx - 10, g + 1, z1, cx + 10, g + 4, z1, AIR);
        b.fill(x0, g + 1, cz - 160, x0, g + 4, cz - 140, AIR);
        cornerTower(b, x0 + 4, z0 + 4, g, p);
        cornerTower(b, x1 - 4, z0 + 4, g, p);
        cornerTower(b, x0 + 4, z1 - 4, g, p);
        cornerTower(b, x1 - 4, z1 - 4, g, p);
    }

    private static void cornerTower(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx - 4, g, cz - 4, cx + 4, g, cz + 4, p.foundation());
        b.room(cx - 4, g + 1, cz - 4, cx + 4, g + 14, cz + 4);
        for (int y = g + 1; y <= g + 14; y++) {
            b.put(cx - 4, y, cz - 4, p.accent());
            b.put(cx + 4, y, cz - 4, p.accent());
            b.put(cx - 4, y, cz + 4, p.accent());
            b.put(cx + 4, y, cz + 4, p.accent());
        }
        b.window(cx, g + 8, cz - 4, 4, 1, true);
        b.window(cx, g + 8, cz + 4, 4, 1, true);
        b.pitchedRoof(cx - 6, cz - 6, cx + 6, cz + 6, g + 15, 4, 0);
        b.put(cx, g + 19, cz, p.light());
    }

    private static void gatePavilion(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 12;
        int x1 = cx + 12;
        int z0 = cz - 10;
        int z1 = cz;
        b.fill(x0, g, z0, x1, g, z1, p.foundation());
        b.walls(x0, g + 1, z0, x1, g + 7, z1, p.wall());
        b.fill(cx - 3, g + 1, z1, cx + 3, g + 4, z1, AIR);
        b.fill(cx - 3, g + 1, z0, cx + 3, g + 4, z0, AIR);
        for (int x = x0; x <= x1; x += 6) {
            if (x > cx - 4 && x < cx + 4) {
                continue;
            }
            for (int y = g + 1; y <= g + 7; y++) {
                b.put(x, y, z1, p.accent());
            }
        }
        b.fill(x0, g + 8, z0, x1, g + 8, z1, p.roof());
        b.pitchedRoof(x0 - 2, z0 - 2, x1 + 2, z1 + 2, g + 8, 5, 1);
        CityPlanSupport.sign(b, cx, g + 6, z1, Direction.SOUTH,
                "GRAND PALACE", "NO SHOES", "IN THE HALL");
        b.put(cx - 8, g + 1, z1 - 2, p.light());
        b.put(cx + 8, g + 1, z1 - 2, p.light());
    }

    private static void ordinationHall(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // Tiered plinth.
        b.fill(cx - 28, g, cz - 36, cx + 28, g + 1, cz + 36, p.foundation());
        b.fill(cx - 25, g + 2, cz - 33, cx + 25, g + 3, cz + 33, p.foundation());
        b.fill(cx - 22, g + 4, cz - 30, cx + 22, g + 5, cz + 30, p.ground());
        // One-block steps up the south face of the tiered plinth.
        b.fill(cx - 6, g + 2, cz + 34, cx + 6, g + 2, cz + 34, p.foundation());
        b.fill(cx - 4, g + 4, cz + 31, cx + 4, g + 4, cz + 31, p.foundation());

        int fy = g + 6;
        int x0 = cx - 20;
        int x1 = cx + 20;
        int z0 = cz - 28;
        int z1 = cz + 28;
        b.room(x0, fy, z0, x1, fy + 18, z1);

        // Black lacquer pillars on the wall line.
        for (int x = x0; x <= x1; x += 8) {
            for (int y = fy; y <= fy + 18; y++) {
                b.put(x, y, z0, p.accent());
                b.put(x, y, z1, p.accent());
            }
        }
        for (int z = z0; z <= z1; z += 8) {
            for (int y = fy; y <= fy + 18; y++) {
                b.put(x0, y, z, p.accent());
                b.put(x1, y, z, p.accent());
            }
        }
        for (int x = x0 + 3; x <= x1 - 3; x += 6) {
            b.window(x, fy + 5, z0, 5, 1, true);
            b.window(x, fy + 5, z1, 5, 1, true);
        }
        for (int z = z0 + 3; z <= z1 - 3; z += 6) {
            b.window(x0, fy + 5, z, 5, 1, true);
            b.window(x1, fy + 5, z, 5, 1, true);
        }
        templeDoor(b, cx - 13, fy + 1, z1, p);
        templeDoor(b, cx - 1, fy + 1, z1, p);
        templeDoor(b, cx + 11, fy + 1, z1, p);

        // Three stacked gold roofs, each narrower and higher than the last.
        b.pitchedRoof(x0 - 3, z0 - 3, x1 + 3, z1 + 3, fy + 18, 8, 1);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, fy + 24, 7, 1);
        b.pitchedRoof(x0 + 4, z0 + 5, x1 - 4, z1 - 5, fy + 30, 6, 1);
        chofah(b, cx, cz - 23, fy + 36, p);
        chofah(b, cx, cz + 23, fy + 36, p);
        chofah(b, cx - 16, cz, fy + 30, p);
        chofah(b, cx + 16, cz, fy + 30, p);

        // The golden spire that dominates the skyline.
        int top = fy + 36;
        for (int i = 0; i < 18; i++) {
            int r = Math.max(0, 4 - i / 5);
            b.fill(cx - r, top + i, cz - r, cx + r, top + i, cz + r, GOLD);
        }
        b.put(cx, top + 18, cz, p.light());

        // Altar and offerings inside.
        b.fill(cx - 4, fy + 1, cz - 24, cx + 4, fy + 2, cz - 20, p.foundation());
        b.fill(cx - 2, fy + 3, cz - 23, cx + 2, fy + 6, cz - 21, GOLD);
        b.put(cx, fy + 7, cz - 22, GOLD);
        b.put(cx - 6, fy + 1, cz - 20, p.light());
        b.put(cx + 6, fy + 1, cz - 20, p.light());
        b.put(cx, fy + 10, cz, p.light());
    }

    private static void templeDoor(StructureBuilder b, int x, int y, int z, Palette p) {
        b.fill(x, y, z, x + 1, y + 2, z, AIR);
        for (int dy = 0; dy <= 2; dy++) {
            b.put(x - 1, y + dy, z, GOLD);
            b.put(x + 2, y + dy, z, GOLD);
        }
        b.put(x, y + 3, z, GOLD);
        b.put(x + 1, y + 3, z, GOLD);
        door(b, p, x, y, z, Direction.SOUTH);
        door(b, p, x + 1, y, z, Direction.SOUTH);
    }

    private static void chofah(StructureBuilder b, int x, int y, int z, Palette p) {
        b.put(x, y, z, GOLD);
        b.put(x, y + 1, z, GOLD);
        b.put(x, y + 2, z, GOLD);
        b.put(x, y + 3, z, p.roofSlab());
        b.put(x, y + 4, z, GOLD);
    }

    private static void stupa(StructureBuilder b, int cx, int cz, int g, Palette p, int big) {
        int s = big == 1 ? 9 : 7;
        b.fill(cx - s, g, cz - s, cx + s, g + 1, cz + s, p.foundation());
        b.fill(cx - s + 2, g + 2, cz - s + 2, cx + s - 2, g + 3, cz + s - 2, p.foundation());
        int y = g + 4;
        for (int r = s - 2; r >= 2; r--) {
            b.fill(cx - r, y, cz - r, cx + r, y, cz + r, p.wall());
            y++;
        }
        for (int i = 0; i < 3; i++) {
            int r = Math.max(1, 3 - i);
            b.fill(cx - r, y, cz - r, cx + r, y, cz + r, GOLD);
            y++;
        }
        for (int i = 0; i < 8; i++) {
            b.put(cx, y + i, cz, (i & 1) == 0 ? GOLD : p.wall());
        }
        b.put(cx, y + 8, cz, p.light());
        b.put(cx, g + 5, cz + s - 2, p.light());
    }

    private static void palaceHall(StructureBuilder b, int cx, int cz, int g, Palette p,
                                   int w, int d, int h, int kind) {
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - d / 2;
        int z1 = z0 + d - 1;
        b.fill(x0 - 2, g, z0 - 2, x1 + 2, g, z1 + 2, p.foundation());
        b.room(x0, g + 1, z0, x1, g + h, z1, new Doorway(Side.S, w / 2));
        for (int x = x0; x <= x1; x += 6) {
            for (int y = g + 1; y <= g + h; y++) {
                b.put(x, y, z0, p.accent());
                b.put(x, y, z1, p.accent());
            }
        }
        for (int z = z0; z <= z1; z += 6) {
            for (int y = g + 1; y <= g + h; y++) {
                b.put(x0, y, z, p.accent());
                b.put(x1, y, z, p.accent());
            }
        }
        frontWindows(b, x0, z0, x1, z1, g + 3, Direction.SOUTH);
        b.fill(x0 - 1, g + 3, z1 + 1, x1 + 1, g + 3, z1 + 3, p.roofSlab());
        b.fill(x0 - 1, g + 1, z1 + 3, x0 - 1, g + 3, z1 + 3, p.accent());
        b.fill(x1 + 1, g + 1, z1 + 3, x1 + 1, g + 3, z1 + 3, p.accent());
        b.pitchedRoof(x0 - 2, z0 - 2, x1 + 2, z1 + 2, g + h, 6, 0);
        b.pitchedRoof(x0 + 2, z0 + 2, x1 - 2, z1 - 2, g + h + 4, 4, 0);
        door(b, p, cx, g + 1, z1, Direction.SOUTH);
        switch (kind) {
            case 0 -> {
                b.fill(cx - 5, g + 1, cz - 8, cx + 5, g + 2, cz - 4, p.foundation());
                b.fill(cx - 2, g + 3, cz - 7, cx + 2, g + 5, cz - 5, GOLD);
                b.put(cx, g + 6, cz - 6, GOLD);
                b.put(cx - 4, g + 1, cz - 3, p.light());
                b.put(cx + 4, g + 1, cz - 3, p.light());
            }
            case 1 -> {
                b.fill(cx - 8, g + 1, cz - 6, cx - 5, g + 1, cz - 3, RED_WOOL);
                b.fill(cx + 5, g + 1, cz - 6, cx + 8, g + 1, cz - 3, RED_WOOL);
                b.fill(cx - 2, g + 1, cz + 2, cx + 2, g + 2, cz + 4, p.frame());
                b.put(cx, g + 3, cz + 3, p.light());
            }
            default -> {
                b.fill(cx - 3, g + 1, cz - 4, cx + 3, g + 2, cz - 2, p.foundation());
                b.fill(cx - 1, g + 3, cz - 3, cx + 1, g + 4, cz - 3, GOLD);
                b.put(cx, g + 5, cz - 3, p.light());
            }
        }
    }

    private static void monkQuarters(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 14;
        int x1 = cx + 14;
        int z0 = cz - 10;
        int z1 = cz + 10;
        for (int x = x0; x <= x1; x += 4) {
            for (int z = z0; z <= z1; z += 4) {
                b.fill(x, g - 1, z, x, g, z, p.frame());
            }
        }
        b.fill(x0, g + 1, z0, x1, g + 1, z1, p.frame());
        b.walls(x0, g + 2, z0, x1, g + 6, z1, p.wall());
        b.fill(cx - 6, g + 2, z1, cx + 6, g + 4, z1, AIR);
        b.fill(x0, g + 7, z0, x1, g + 7, z1, p.roof());
        b.pitchedRoof(x0 - 2, z0 - 2, x1 + 2, z1 + 2, g + 7, 5, 0);
        b.fill(cx - 10, g + 2, cz - 6, cx - 6, g + 2, cz - 2, RED_WOOL);
        b.fill(cx + 4, g + 2, cz - 6, cx + 8, g + 2, cz - 2, RED_WOOL);
        b.put(cx - 12, g + 2, cz - 4, ORANGE_WOOL);
        b.put(cx + 12, g + 2, cz - 4, ORANGE_WOOL);
        b.fill(cx - 3, g + 2, cz + 2, cx + 3, g + 2, cz + 4, p.frame());
        b.fill(cx - 3, g + 3, cz + 2, cx + 3, g + 3, cz + 4, BOOKS);
        b.put(cx, g + 2, cz, p.light());
        for (int dx = -6; dx <= 6; dx += 3) {
            b.put(cx + dx, g + 1, cz + 14, HAY);
            b.put(cx + dx, g + 1, cz + 16, BAMBOO);
        }
        b.put(cx + 12, g + 1, cz + 12, p.frame());
        CityPlanSupport.sign(b, cx + 12, g + 2, cz + 12, Direction.SOUTH,
                "MONK HEALER", "HERBAL REMEDIES");
    }

    private static void palaceGarden(StructureBuilder b, int cx, int cz, int g, Palette p) {
        lotusPond(b, cx, cz, g, p);
        int px = cx + 22;
        int pz = cz + 18;
        b.fill(px - 4, g + 1, pz - 3, px + 4, g + 1, pz + 3, p.foundation());
        for (int dx = -4; dx <= 4; dx += 8) {
            for (int dz = -3; dz <= 3; dz += 6) {
                b.fill(px + dx, g + 2, pz + dz, px + dx, g + 5, pz + dz, p.frame());
            }
        }
        b.pitchedRoof(px - 6, pz - 5, px + 6, pz + 5, g + 6, 3, 0);
        b.put(px, g + 2, pz, p.light());
        palm(b, cx - 26, cz - 20, g, p);
        palm(b, cx + 26, cz - 20, g, p);
    }

    private static void statue(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx - 1, g + 1, cz - 1, cx + 1, g + 2, cz + 1, p.foundation());
        b.put(cx, g + 3, cz, p.accent());
        b.put(cx, g + 4, cz, p.accent());
        b.put(cx - 1, g + 4, cz, p.accent());
        b.put(cx + 1, g + 4, cz, p.accent());
        b.put(cx, g + 5, cz, p.accent());
        b.put(cx, g + 6, cz, p.light());
    }

    // ------------------------------------------------------------------ temple garden

    private static void templeGarden(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 100;
        int x1 = cx + 100;
        int z0 = cz - 80;
        int z1 = cz + 60;
        b.walls(x0, g + 1, z0, x1, g + 2, z1, p.wall());
        b.crenellations(x0, z0, x1, z1, g + 3);
        b.fill(cx - 4, g + 1, z1, cx + 4, g + 3, z1, AIR);
        b.fill(cx - 4, g + 1, z0, cx + 4, g + 3, z0, AIR);
        b.fill(x1, g + 1, cz - 44, x1, g + 3, cz - 36, AIR);
        b.fill(x0, g + 1, cz - 44, x0, g + 3, cz - 36, AIR);

        CityPlanSupport.path(b, cx, cz - 80, cx, cz + 60, 8, g, p.ground());
        CityPlanSupport.path(b, cx - 100, cz - 40, cx + 100, cz - 40, 6, g, p.ground());
        CityPlanSupport.path(b, cx - 155, cz - 40, cx - 100, cz - 40, 6, g, p.ground());
        CityPlanSupport.path(b, cx + 100, cz - 40, cx + 140, cz - 40, 6, g, p.ground());

        bodhiTree(b, cx - 45, cz - 55, g, p);
        lotusPond(b, cx - 50, cz + 20, g, p);
        crypt(b, cx + 60, cz - 20, g, p);

        // A small open shrine east of the processional way.
        b.fill(cx + 26, g + 1, cz + 16, cx + 34, g + 1, cz + 24, p.foundation());
        for (int dx = 26; dx <= 34; dx += 8) {
            for (int dz = 16; dz <= 24; dz += 8) {
                b.fill(cx + dx, g + 2, cz + dz, cx + dx, g + 4, cz + dz, p.accent());
            }
        }
        b.pitchedRoof(cx + 24, cz + 14, cx + 36, cz + 26, g + 5, 3, 0);
        b.put(cx + 30, g + 2, cz + 20, GOLD);
        b.put(cx + 30, g + 3, cz + 20, p.light());

        palm(b, cx - 90, cz - 70, g, p);
        palm(b, cx + 90, cz + 50, g, p);
        bamboo(b, cx - 90, cz + 50, g, p);
        bamboo(b, cx + 90, cz - 70, g, p);
        spiritHouse(b, cx + 10, cz + 50, g, p);
        spiritHouse(b, cx - 10, cz - 70, g, p);
        b.put(cx + 7, g + 1, cz + 58, p.frame());
        CityPlanSupport.sign(b, cx + 7, g + 2, cz + 58, Direction.SOUTH,
                "TEMPLE GARDEN", "BODHI TREE", "LOTUS POND");
    }

    private static void bodhiTree(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx - 1, g + 1, cz - 1, cx + 1, g + 5, cz + 1, p.frame());
        for (int i = 0; i < 4; i++) {
            int r = 10 - i * 2;
            b.fill(cx - r, g + 6 + i, cz - r, cx + r, g + 6 + i, cz + r, LEAVES);
        }
        for (int i = -6; i <= 6; i += 2) {
            b.put(cx + i, g + 1, cz - 7, p.rail());
            b.put(cx + i, g + 1, cz + 7, p.rail());
        }
        b.fill(cx - 2, g + 1, cz + 4, cx + 2, g + 1, cz + 5, p.foundation());
        b.put(cx, g + 2, cz + 4, GOLD);
        b.put(cx, g + 3, cz + 4, p.light());
    }

    private static void lotusPond(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx - 24, g - 2, cz - 14, cx + 24, g - 2, cz + 14, p.foundation());
        b.fill(cx - 24, g - 1, cz - 14, cx + 24, g, cz + 14, WATER);
        b.fill(cx - 25, g, cz - 15, cx + 25, g, cz - 15, p.foundation());
        b.fill(cx - 25, g, cz + 15, cx + 25, g, cz + 15, p.foundation());
        b.fill(cx - 25, g, cz - 15, cx - 25, g, cz + 15, p.foundation());
        b.fill(cx + 25, g, cz - 15, cx + 25, g, cz + 15, p.foundation());
        for (int x = cx - 20; x <= cx + 20; x += 3) {
            for (int z = cz - 10; z <= cz + 10; z += 3) {
                if (((x + z) & 1) == 0) {
                    b.put(x, g, z, LILY);
                }
            }
        }
        b.fill(cx - 2, g + 1, cz - 15, cx + 2, g + 1, cz + 15, p.frame());
        b.fill(cx - 3, g + 2, cz - 15, cx - 3, g + 2, cz + 15, p.rail());
        b.fill(cx + 3, g + 2, cz - 15, cx + 3, g + 2, cz + 15, p.rail());
    }

    private static void crypt(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // Mound, entrance frame, and the open stair trench.
        b.fill(cx - 10, g + 1, cz - 14, cx + 10, g + 3, cz + 6, p.foundation());
        b.fill(cx - 3, g + 1, cz + 6, cx + 3, g + 3, cz + 6, AIR);
        b.fill(cx - 2, g + 1, cz - 1, cx + 2, g + 4, cz + 6, AIR);
        b.fill(cx - 3, g + 1, cz - 1, cx - 3, g + 2, cz + 6, p.accent());
        b.fill(cx + 3, g + 1, cz - 1, cx + 3, g + 2, cz + 6, p.accent());
        b.fill(cx - 3, g + 4, cz + 6, cx + 3, g + 4, cz + 6, p.accent());
        for (int i = 0; i < 7; i++) {
            b.fill(cx - 2, g - i, cz - 1 - i, cx + 2, g - i, cz - 1 - i, p.foundation());
            b.fill(cx - 2, g + 1 - i, cz - 1 - i, cx + 2, g + 2 - i, cz - 1 - i, AIR);
        }

        int fy = g - 7;
        b.fill(cx - 9, fy, cz - 14, cx + 9, fy, cz - 2, p.foundation());
        b.walls(cx - 9, fy + 1, cz - 14, cx + 9, fy + 3, cz - 2, p.weathered());
        b.fill(cx - 9, fy + 4, cz - 14, cx + 9, fy + 4, cz - 2, p.foundation());
        b.fill(cx - 8, fy + 1, cz - 13, cx + 8, fy + 3, cz - 3, AIR);
        b.chest(cx + 5, fy + 1, cz - 11, SiteLoot.RELIC);
        b.put(cx - 4, fy + 1, cz - 8, p.frame());
        CityPlanSupport.sign(b, cx - 4, fy + 2, cz - 8, Direction.SOUTH,
                "TEMPLE CRYPT", "KEEP OUT", "WARDEN SLEEPS");
        b.put(cx - 6, fy + 1, cz - 12, GOLD);
        b.put(cx + 6, fy + 1, cz - 12, p.light());
        b.fill(cx - 8, fy + 3, cz - 13, cx - 7, fy + 3, cz - 12, Palette.cobweb());
        b.fill(cx + 7, fy + 3, cz - 13, cx + 8, fy + 3, cz - 12, Palette.cobweb());

        b.put(cx + 6, g + 4, cz + 6, p.frame());
        CityPlanSupport.sign(b, cx + 6, g + 5, cz + 6, Direction.SOUTH,
                "TEMPLE CRYPT", "KEEP OUT");
        b.put(cx - 6, g + 4, cz + 6, p.light());
        // The ruined chedi over the mound.
        b.fill(cx - 3, g + 4, cz - 10, cx + 3, g + 6, cz - 4, p.weathered());
        b.put(cx, g + 7, cz - 7, GOLD);
        b.put(cx, g + 8, cz - 7, p.light());
    }

    // ------------------------------------------------------------------ klong market

    private static void klongMarket(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int[] westZ = {-235, -195, -80, 40, 120, 160};
        for (int dz : westZ) {
            stiltHouse(b, cx, cz, g, p, -246, dz - 10, false);
        }
        int[] eastZ = {-235, -195, -120, -80, 40, 70, 160};
        for (int dz : eastZ) {
            stiltHouse(b, cx, cz, g, p, -196, dz - 10, true);
        }

        bridgeX(b, cx, cz - 160, g, p, -216, -204);
        bridgeX(b, cx, cz - 20, g, p, -216, -204);
        bridgeX(b, cx, cz + 140, g, p, -216, -204);
        bridgeX(b, cx, cz + 190, g, p, -216, -204);
        bridgeZ(b, cx - 251, cz - 60, g, p, -66, -54);
        bridgeZ(b, cx - 251, cz + 100, g, p, -66, -54);

        // The market jetty carrying the quest board.
        b.fill(cx - 227, g + 1, cz - 8, cx - 214, g + 1, cz + 8, p.frame());
        for (int z = cz - 8; z <= cz + 8; z += 4) {
            b.fill(cx - 214, g - 1, z, cx - 214, g, z, p.accent());
        }
        noticeBoard(b, cx - 220, cz, g + 1, Direction.EAST, p,
                "KLONG MARKET", "STOLEN AMULET", "RIVER SPIRIT", "ASK THE SELLERS");
        b.fill(cx - 220, g + 1, cz + 6, cx - 220, g + 3, cz + 6, p.frame());
        CityPlanSupport.sign(b, cx - 220, g + 4, cz + 6, Direction.SOUTH,
                "KLONG", "MIND THE STEP");

        marketStall(b, cx - 240, cz - 120, g, p, RED_WOOL);
        marketStall(b, cx - 236, cz - 180, g, p, ORANGE_WOOL);
        marketStall(b, cx - 232, cz - 40, g, p, RED_WOOL);
        marketStall(b, cx - 238, cz + 60, g, p, ORANGE_WOOL);
        marketStall(b, cx - 188, cz - 160, g, p, RED_WOOL);
        marketStall(b, cx - 182, cz + 20, g, p, ORANGE_WOOL);
        b.put(cx - 238, g + 1, cz - 124, p.frame());
        CityPlanSupport.sign(b, cx - 238, g + 2, cz - 124, Direction.SOUTH,
                "AMULETS", "RIVER CHARMS");

        boat(b, cx - 212, cz - 180, g, p, false);
        boat(b, cx - 212, cz - 100, g, p, false);
        boat(b, cx - 212, cz, g, p, false);
        boat(b, cx - 212, cz + 54, g, p, false);
        boat(b, cx - 212, cz + 130, g, p, false);

        ruinedChedi(b, cx - 292, cz - 220, g, p);
        palm(b, cx - 300, cz - 120, g, p);
        palm(b, cx - 296, cz - 20, g, p);
        palm(b, cx - 302, cz + 60, g, p);
        palm(b, cx - 160, cz - 230, g, p);
        palm(b, cx - 162, cz + 160, g, p);
        bamboo(b, cx - 290, cz - 180, g, p);
        bamboo(b, cx - 288, cz + 140, g, p);
    }

    private static void stiltHouse(StructureBuilder b, int cx, int cz, int g, Palette p,
                                   int x, int z, boolean eastBank) {
        int x1 = x + 18;
        int z1 = z + 19;
        int hx0 = cx + x;
        int hx1 = cx + x1;
        int hz0 = cz + z;
        int hz1 = cz + z1;
        for (int px = hx0; px <= hx1; px += 4) {
            for (int pz = hz0; pz <= hz1; pz += 4) {
                b.fill(px, g - 2, pz, px, g, pz, p.frame());
            }
        }
        int dx0 = eastBank ? hx0 - 14 : hx1 + 1;
        int dx1 = eastBank ? hx0 - 1 : hx1 + 14;
        b.fill(dx0, g + 1, hz0, dx1, g + 1, hz1, p.frame());
        int postX = eastBank ? dx0 : dx1;
        for (int pz = hz0; pz <= hz1; pz += 4) {
            b.fill(postX, g - 2, pz, postX, g, pz, p.frame());
        }
        b.fill(hx0, g + 1, hz0, hx1, g + 1, hz1, p.frame());
        b.walls(hx0, g + 2, hz0, hx1, g + 7, hz1, p.frame());
        b.fill(hx0, g + 8, hz0, hx1, g + 8, hz1, p.roof());
        b.pitchedRoof(hx0 - 1, hz0 - 1, hx1 + 1, hz1 + 1, g + 8, 5, 0);
        int frontX = eastBank ? hx0 : hx1;
        b.fill(frontX, g + 2, hz0 + 4, frontX, g + 5, hz1 - 4, AIR);
        for (int pz = hz0 + 3; pz <= hz1 - 3; pz += 5) {
            b.put(frontX, g + 6, pz, p.window());
        }
        b.put(postX, g + 2, hz0 + 2, p.light());
        b.put(postX, g + 2, hz1 - 2, p.light());
        // Goods and a cooking pot on the deck.
        b.put(postX, g + 1, hz0 + 5, BARREL);
        b.put(postX, g + 1, hz0 + 6, HAY);
        b.put(postX, g + 1, hz1 - 5, RED_WOOL);
    }

    private static void bridgeX(StructureBuilder b, int cx, int cz, int g, Palette p,
                                int xFrom, int xTo) {
        int z0 = cz - 2;
        int z1 = cz + 1;
        b.fill(cx + xFrom, g + 1, z0, cx + xTo, g + 1, z1, p.frame());
        b.fill(cx + xFrom, g + 2, z0, cx + xTo, g + 2, z0, p.rail());
        b.fill(cx + xFrom, g + 2, z1, cx + xTo, g + 2, z1, p.rail());
        for (int x = xFrom; x <= xTo; x += 4) {
            b.fill(cx + x, g - 2, z0, cx + x, g, z0, p.accent());
            b.fill(cx + x, g - 2, z1, cx + x, g, z1, p.accent());
        }
    }

    private static void bridgeZ(StructureBuilder b, int cx, int cz, int g, Palette p,
                                int zFrom, int zTo) {
        int x0 = cx - 2;
        int x1 = cx + 1;
        b.fill(x0, g + 1, cz + zFrom, x1, g + 1, cz + zTo, p.frame());
        b.fill(x0, g + 2, cz + zFrom, x0, g + 2, cz + zTo, p.rail());
        b.fill(x1, g + 2, cz + zFrom, x1, g + 2, cz + zTo, p.rail());
        for (int z = zFrom; z <= zTo; z += 4) {
            b.fill(x0, g - 2, cz + z, x0, g, cz + z, p.accent());
            b.fill(x1, g - 2, cz + z, x1, g, cz + z, p.accent());
        }
    }

    private static void boat(StructureBuilder b, int cx, int cz, int g, Palette p,
                             boolean alongX) {
        if (alongX) {
            b.fill(cx - 5, g - 1, cz - 1, cx + 5, g, cz + 1, p.frame());
            b.fill(cx - 3, g + 1, cz - 1, cx + 3, g + 1, cz + 1, p.roofSlab());
        } else {
            b.fill(cx - 1, g - 1, cz - 5, cx + 1, g, cz + 5, p.frame());
            b.fill(cx - 1, g + 1, cz - 3, cx + 1, g + 1, cz + 3, p.roofSlab());
        }
        b.put(cx, g + 2, cz, p.light());
    }

    private static void marketStall(StructureBuilder b, int cx, int cz, int g, Palette p,
                                    BlockState awning) {
        for (int dx = -2; dx <= 2; dx += 4) {
            for (int dz = -2; dz <= 2; dz += 4) {
                b.fill(cx + dx, g + 1, cz + dz, cx + dx, g + 3, cz + dz, p.frame());
            }
        }
        b.fill(cx - 1, g + 1, cz - 1, cx + 1, g + 1, cz + 1, p.foundation());
        b.fill(cx - 2, g + 4, cz - 2, cx + 2, g + 4, cz + 2, awning);
        b.put(cx, g + 2, cz, p.light());
        b.put(cx - 1, g + 2, cz, GOLD);
    }

    private static void ruinedChedi(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx - 6, g + 1, cz - 6, cx + 6, g + 2, cz + 6, p.weathered());
        b.walls(cx - 5, g + 3, cz - 5, cx + 5, g + 7, cz + 5, p.weathered());
        b.fill(cx - 3, g + 8, cz - 3, cx + 1, g + 9, cz + 3, p.weathered());
        b.put(cx + 2, g + 8, cz + 2, p.rubble());
        b.put(cx - 4, g + 3, cz - 4, p.light());
        BlockState vine = p.overgrowth() != null ? p.overgrowth() : p.rubble();
        for (int y = g + 3; y <= g + 7; y += 2) {
            b.put(cx - 5, y, cz, vine);
        }
    }

    // ------------------------------------------------------------------ chinatown

    private static void chinatown(StructureBuilder b, int cx, int cz, int g, Palette p) {
        shophouseRow(b, cx, cz, g, p, 143, -182, 176, -166, Direction.SOUTH, 3, 1);
        shophouseRow(b, cx, cz, g, p, 186, -218, 244, -166, Direction.SOUTH, 5, 2);
        shophouseRow(b, cx, cz, g, p, 256, -218, 317, -166, Direction.WEST, 4, 2);
        shophouseRow(b, cx, cz, g, p, 143, -154, 176, -46, Direction.EAST, 6, 3);
        shophouseRow(b, cx, cz, g, p, 186, -154, 244, -140, Direction.NORTH, 4, 2);
        shophouseRow(b, cx, cz, g, p, 186, -58, 214, -46, Direction.SOUTH, 2, 0);
        shophouseRow(b, cx, cz, g, p, 226, -58, 244, -46, Direction.SOUTH, 2, 0);
        shophouseRow(b, cx, cz, g, p, 256, -154, 317, -46, Direction.WEST, 5, 2);
        shophouseRow(b, cx, cz, g, p, 143, -34, 176, 14, Direction.EAST, 3, 1);
        shophouseRow(b, cx, cz, g, p, 186, -34, 244, 14, Direction.NORTH, 4, 2);
        shophouseRow(b, cx, cz, g, p, 186, 26, 244, 57, Direction.NORTH, 4, 3);

        teaHouse(b, cx + 160, cz - 200, g, p);
        goldShop(b, cx + 220, cz - 80, g, p);
        opiumDen(b, cx + 290, cz - 22, g, p);
        warehouse(b, cx + 278, cz + 40, g, p);
        chineseGate(b, cx + 146, cz - 40, g, p);
        floodedRow(b, cx + 143, cz + 26, g, p);
        bamboo(b, cx + 306, cz + 40, g, p);
        b.put(cx + 184, g + 1, cz - 156, p.frame());
        CityPlanSupport.sign(b, cx + 184, g + 2, cz - 156, Direction.NORTH,
                "CHINATOWN", "GOLD & JADE", "TEA HOUSES");
    }

    private static void shophouseRow(StructureBuilder b, int cx, int cz, int g, Palette p,
                                     int x0, int z0, int x1, int z1, Direction front,
                                     int units, int variant) {
        int xa = cx + x0;
        int xb = cx + x1;
        int za = cz + z0;
        int zb = cz + z1;
        int h = 10 + variant % 2;
        BlockState awning = variant % 2 == 0 ? RED_WOOL : ORANGE_WOOL;
        b.fill(xa, g, za, xb, g, zb, p.foundation());
        b.room(xa, g + 1, za, xb, g + h, zb);
        boolean alongX = front == Direction.NORTH || front == Direction.SOUTH;
        if (variant < 2) {
            b.pitchedRoof(xa - 1, za - 1, xb + 1, zb + 1, g + h, 4, alongX ? 0 : 1);
        } else {
            b.fill(xa - 1, g + h, za - 1, xb + 1, g + h, zb + 1, p.roofSlab());
            b.crenellations(xa - 1, za - 1, xb + 1, zb + 1, g + h + 1);
        }
        int start = alongX ? xa : za;
        int end = alongX ? xb : zb;
        int len = end - start + 1;
        int step = Math.max(6, len / units);
        int wallX = switch (front) {
            case EAST -> xb;
            case WEST -> xa;
            default -> 0;
        };
        int wallZ = switch (front) {
            case SOUTH -> zb;
            case NORTH -> za;
            default -> 0;
        };
        for (int i = 0; i < units; i++) {
            int u0 = start + i * step;
            if (u0 > end) {
                break;
            }
            int u1 = Math.min(end, u0 + step - 1);
            int mid = (u0 + u1) / 2;
            int wx = alongX ? mid : wallX;
            int wz = alongX ? wallZ : mid;
            if (alongX) {
                b.fill(wx - 1, g + 1, wz, wx + 1, g + 3, wz, AIR);
                b.put(wx, g + 1, wz - (front == Direction.SOUTH ? 1 : -1), p.frame());
            } else {
                b.fill(wx, g + 1, wz - 1, wx, g + 3, wz + 1, AIR);
                b.put(wx - (front == Direction.EAST ? 1 : -1), g + 1, wz, p.frame());
            }
            switch (front) {
                case SOUTH -> b.fill(wx - 2, g + 4, wz + 1, wx + 2, g + 4, wz + 2, awning);
                case NORTH -> b.fill(wx - 2, g + 4, wz - 2, wx + 2, g + 4, wz - 1, awning);
                case EAST -> b.fill(wx + 1, g + 4, wz - 2, wx + 2, g + 4, wz + 2, awning);
                default -> b.fill(wx - 2, g + 4, wz - 2, wx - 1, g + 4, wz + 2, awning);
            }
            if (alongX) {
                b.window(wx - 1, g + 6, wz, 2, 2, true);
                b.window(wx - 1, g + 8, wz, 2, 2, true);
                b.fill(wx - 2, g + 5, wz, wx + 2, g + 5, wz, p.frame());
                b.put(wx + 2, g + 4, wz + (front == Direction.SOUTH ? 1 : -1), p.light());
            } else {
                b.window(wx, g + 6, wz - 1, 2, 2, true);
                b.window(wx, g + 8, wz - 1, 2, 2, true);
                b.fill(wx, g + 5, wz - 2, wx, g + 5, wz + 2, p.frame());
                b.put(wx + (front == Direction.EAST ? 1 : -1), g + 4, wz + 2, p.light());
            }
        }
        int rearZ = front == Direction.NORTH ? zb : za;
        for (int i = 0; i < 3; i++) {
            int wx = xa + 3 + i * 6;
            if (wx > xb - 2) {
                break;
            }
            b.window(wx, g + 6, rearZ, 2, 1, true);
        }
    }

    private static void teaHouse(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 14;
        int x1 = cx + 14;
        int z0 = cz - 14;
        int z1 = cz + 14;
        b.fill(x0, g, z0, x1, g, z1, p.foundation());
        b.room(x0, g + 1, z0, x1, g + 10, z1, new Doorway(Side.E, 14));
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 10, 5, 0);
        b.fill(x1 + 1, g + 1, z0 + 2, x1 + 2, g + 1, z1 - 2, p.frame());
        for (int z = z0 + 2; z <= z1 - 2; z += 5) {
            b.put(x1 + 2, g + 2, z, p.rail());
            b.put(x1 + 2, g + 3, z, p.rail());
        }
        b.fill(x1 + 1, g + 4, z0 + 2, x1 + 2, g + 4, z1 - 2, p.roofSlab());
        b.put(x1 + 2, g + 4, z0 + 4, p.light());
        b.put(x1 + 2, g + 4, z1 - 4, p.light());
        for (int x = x0 + 4; x <= x1 - 4; x += 7) {
            for (int z = z0 + 4; z <= z1 - 4; z += 7) {
                b.put(x, g + 1, z, p.frame());
                b.put(x, g + 2, z, p.roofSlab());
            }
        }
        b.fill(x0 + 2, g + 1, z1 - 3, x0 + 10, g + 2, z1 - 2, p.frame());
        b.fill(x0 + 2, g + 3, z1 - 3, x0 + 10, g + 4, z1 - 3, BOOKS);
        door(b, p, x1, g + 1, cz, Direction.EAST);
        b.put(x1 + 2, g + 5, z0 + 8, p.frame());
        CityPlanSupport.sign(b, x1 + 2, g + 6, z0 + 8, Direction.EAST,
                "TEA HOUSE", "JASMINE TEA");
    }

    private static void goldShop(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 16;
        int x1 = cx + 16;
        int z0 = cz - 18;
        int z1 = cz + 18;
        b.fill(x0, g, z0, x1, g, z1, p.foundation());
        b.room(x0, g + 1, z0, x1, g + 10, z1, new Doorway(Side.S, 16));
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 10, 6, 0);
        for (int x = x0 + 4; x <= x1 - 4; x += 6) {
            b.window(x, g + 4, z1, 3, 3, false);
            b.put(x, g + 3, z1, GOLD);
            b.put(x + 1, g + 3, z1, GOLD);
        }
        door(b, p, cx, g + 1, z1, Direction.SOUTH);
        for (int y = g + 1; y <= g + 10; y++) {
            b.put(x0, y, z0, GOLD);
            b.put(x1, y, z0, GOLD);
            b.put(x0, y, z1, GOLD);
            b.put(x1, y, z1, GOLD);
        }
        b.fill(x0 + 3, g + 1, z0 + 4, x1 - 3, g + 1, z0 + 8, p.frame());
        b.fill(x0 + 5, g + 2, z0 + 5, x1 - 5, g + 2, z0 + 7, GOLD);
        b.put(x0 + 4, g + 1, z1 - 3, BARREL);
        b.put(x1 - 4, g + 1, z1 - 3, BARREL);
        b.put(cx + 8, g + 6, z1, p.frame());
        CityPlanSupport.sign(b, cx + 8, g + 7, z1, Direction.SOUTH,
                "GOLD SHOP", "GOLD & JADE");

        // Guarded alley from the lane to the shop door.
        b.fill(cx - 2, g, z1 + 1, cx + 2, g, z1 + 22, p.foundation());
        b.walls(cx - 3, g + 1, z1 + 1, cx - 3, g + 4, z1 + 22, p.wall());
        b.walls(cx + 3, g + 1, z1 + 1, cx + 3, g + 4, z1 + 22, p.wall());
        b.fill(cx - 3, g + 4, z1 + 22, cx + 3, g + 5, z1 + 22, p.accent());
        CityPlanSupport.sign(b, cx, g + 5, z1 + 22, Direction.SOUTH,
                "GOLD SHOP", "GOLD & JADE");
    }

    private static void opiumDen(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 20;
        int x1 = cx + 20;
        int z0 = cz - 12;
        int z1 = cz + 12;
        b.fill(x0, g, z0, x1, g, z1, p.foundation());
        b.room(x0, g + 1, z0, x1, g + 5, z1, new Doorway(Side.N, 20));
        b.fill(x0, g + 6, z0, x1, g + 6, z1, p.roofSlab());
        for (int x = x0 + 3; x <= x1 - 3; x += 6) {
            b.fill(x, g + 1, z0 + 2, x + 2, g + 1, z0 + 4, RED_WOOL);
            b.put(x, g + 2, z0 + 3, p.light());
        }
        b.walls(x1 - 8, g + 1, z0, x1, g + 5, z0 + 6, p.frame());
        door(b, p, x1 - 8, g + 1, z0 + 3, Direction.WEST);
        b.put(x0 + 3, g + 1, z1 - 3, BARREL);
        b.put(x0 + 5, g + 1, z1 - 3, BOOKS);
        CityPlanSupport.sign(b, cx, g + 6, z0, Direction.NORTH,
                "TEA & REST", "QUIET ROOMS");
    }

    private static void warehouse(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 22;
        int x1 = cx + 22;
        int z0 = cz - 15;
        int z1 = cz + 15;
        b.fill(x0, g, z0, x1, g, z1, p.foundation());
        b.room(x0, g + 1, z0, x1, g + 12, z1, new Doorway(Side.S, 22));
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 12, 7, 0);
        b.fill(cx - 6, g + 1, z1, cx + 6, g + 6, z1, p.frame());
        b.fill(cx - 1, g + 1, z1, cx + 1, g + 3, z1, AIR);
        door(b, p, cx, g + 1, z1, Direction.SOUTH);
        for (int x = x0 + 3; x <= x1 - 3; x += 5) {
            b.put(x, g + 1, z0 + 3, BARREL);
            b.put(x, g + 1, z0 + 6, HAY);
        }
        b.walls(x1 - 10, g + 1, z0, x1, g + 5, z0 + 8, p.frame());
        door(b, p, x1 - 5, g + 1, z0 + 8, Direction.SOUTH);
        cellar(b, cx, cz, g, p);
        b.put(x0 + 2, g + 7, z1, p.frame());
        CityPlanSupport.sign(b, x0 + 2, g + 8, z1, Direction.SOUTH,
                "CUSTOMS", "NO ENTRY", "GODOWN 7");
    }

    private static void cellar(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 20;
        int x1 = cx - 8;
        int z0 = cz - 12;
        int z1 = cz - 2;
        int fy = g - 5;
        b.fill(x0, fy, z0, x1, fy, z1, p.foundation());
        b.walls(x0, fy + 1, z0, x1, fy + 3, z1, p.weathered());
        b.fill(x0, fy + 4, z0, x1, fy + 4, z1, p.foundation());
        b.fill(x0 + 1, fy + 1, z0 + 1, x1 - 1, fy + 3, z1 - 1, AIR);
        int sx = x0 + 4;
        b.fill(sx - 1, g + 1, z1, sx + 1, g + 1, z1 - 5, AIR);
        for (int i = 0; i < 6; i++) {
            b.fill(sx - 1, g - i, z1 - i, sx + 1, g - i, z1 - i, p.foundation());
            b.fill(sx - 1, g + 1 - i, z1 - i, sx + 1, g + 2 - i, z1 - i, AIR);
        }
        b.fill(sx - 1, fy + 1, z1, sx + 1, fy + 3, z1, AIR);
        b.chest(x1 - 2, fy + 1, z1 - 3, SiteLoot.CULT);
        b.put(x0 + 3, fy + 1, z1 - 3, p.frame());
        CityPlanSupport.sign(b, x0 + 3, fy + 2, z1 - 3, Direction.SOUTH,
                "SMUGGLED CARGO", "CHOIR'S DUE");
        b.put(x0 + 5, fy + 1, z0 + 2, BARREL);
        b.put(x0 + 7, fy + 1, z0 + 2, GOLD);
        b.fill(x0 + 1, fy + 3, z0 + 1, x0 + 2, fy + 3, z0 + 2, Palette.cobweb());
    }

    private static void chineseGate(StructureBuilder b, int x, int z, int g, Palette p) {
        for (int dz = -5; dz <= 5; dz += 10) {
            b.fill(x - 1, g + 1, z + dz - 1, x + 1, g + 8, z + dz + 1, p.accent());
            b.fill(x - 1, g + 1, z + dz - 1, x + 1, g + 3, z + dz + 1, RED_WOOL);
        }
        b.fill(x - 1, g + 7, z - 5, x + 1, g + 8, z + 5, p.accent());
        b.fill(x - 2, g + 9, z - 6, x + 2, g + 9, z + 6, p.roof());
        b.pitchedRoof(x - 3, z - 7, x + 3, z + 7, g + 9, 3, 1);
        CityPlanSupport.sign(b, x - 1, g + 8, z, Direction.WEST,
                "CHINATOWN", "GOLD & JADE");
    }

    private static void floodedRow(StructureBuilder b, int x0, int z0, int g, Palette p) {
        int x1 = x0 + 33;
        int z1 = z0 + 31;
        b.fill(x0, g - 1, z0, x1, g, z1, WATER);
        b.walls(x0, g + 1, z0, x1, g + 4, z1, p.weathered());
        b.fill(x0 + 4, g + 1, z0, x0 + 12, g + 4, z0, AIR);
        b.fill(x1, g + 1, z0 + 8, x1, g + 4, z0 + 20, AIR);
        b.fill(x0 + 6, g + 5, z0 + 4, x0 + 24, g + 5, z1 - 4, p.roof());
        for (int px = x0 + 2; px <= x1 - 2; px += 4) {
            for (int pz = z0 + 2; pz <= z1 - 2; pz += 4) {
                if (((px + pz) & 1) == 0) {
                    b.put(px, g, pz, LILY);
                }
            }
        }
        b.fill(x0 + 4, g + 3, z0 + 4, x0 + 8, g + 3, z1 - 4, p.frame());
    }

    // ------------------------------------------------------------------ guesthouse street

    private static void guesthouseStreet(StructureBuilder b, int cx, int cz, int g, Palette p) {
        guesthouseArch(b, cx + 226, cz + 82, g, p);
        guesthouse(b, cx, cz, g, p, 190, 92, 30, 26, Direction.EAST, "GUESTHOUSE");
        guesthouse(b, cx, cz, g, p, 186, 122, 34, 24, Direction.EAST, "THE LANTERN");
        guesthouse(b, cx, cz, g, p, 190, 150, 30, 30, Direction.EAST, "RIVER VIEW");
        guesthouse(b, cx, cz, g, p, 238, 96, 34, 26, Direction.WEST, "BEDS & ALE");
        guesthouse(b, cx, cz, g, p, 240, 126, 32, 24, Direction.WEST, "THE GECKO");
        streetBar(b, cx, cz, g, p, 186, 180, 20, 7, Direction.SOUTH, "COLD ALE");
        streetBar(b, cx, cz, g, p, 231, 84, 20, 10, Direction.WEST, "CAFE");
        riverWarehouse(b, cx, cz, g, p, 244, 152, 30, 34, Direction.SOUTH);
        riverWarehouse(b, cx, cz, g, p, 282, 152, 30, 34, Direction.SOUTH);

        // Inn veranda, so the required anchor sits under the lanterns.
        b.fill(cx + 220, g + 1, cz + 152, cx + 221, g + 1, cz + 177, p.frame());
        for (int z = 152; z <= 177; z += 5) {
            b.put(cx + 221, g + 2, cz + z, p.rail());
            b.put(cx + 221, g + 3, cz + z, p.rail());
        }
        b.fill(cx + 220, g + 4, cz + 152, cx + 221, g + 4, cz + 177, p.roofSlab());
        b.put(cx + 220, g + 4, cz + 158, p.light());
        b.put(cx + 220, g + 4, cz + 170, p.light());
        // The inn's beds and a lamp in the common room.
        b.fill(cx + 196, g + 1, cz + 154, cx + 198, g + 1, cz + 156, RED_WOOL);
        b.fill(cx + 196, g + 1, cz + 158, cx + 198, g + 1, cz + 160, RED_WOOL);
        b.put(cx + 200, g + 3, cz + 160, p.light());

        for (int dz = 96; dz <= 188; dz += 12) {
            lanternPost(b, cx + 220, cz + dz, g, p);
            lanternPost(b, cx + 233, cz + dz, g, p);
        }
        spiritHouse(b, cx + 218, cz + 80, g, p);
        spiritHouse(b, cx + 234, cz + 100, g, p);
    }

    private static void guesthouseArch(StructureBuilder b, int cx, int cz, int g, Palette p) {
        for (int s = -1; s <= 1; s += 2) {
            int x = cx + s * 5;
            b.fill(x - 1, g + 1, cz - 1, x + 1, g + 7, cz + 1, p.accent());
            b.fill(x - 1, g + 1, cz - 1, x + 1, g + 3, cz + 1, RED_WOOL);
        }
        b.fill(cx - 6, g + 7, cz, cx + 6, g + 8, cz, p.accent());
        b.pitchedRoof(cx - 7, cz - 2, cx + 7, cz + 2, g + 8, 3, 0);
        CityPlanSupport.sign(b, cx + 5, g + 4, cz + 1, Direction.SOUTH,
                "GUESTHOUSES", "BEDS & ALE", "LANTERNS");
    }

    private static void guesthouse(StructureBuilder b, int cx, int cz, int g, Palette p,
                                   int x, int z, int w, int d, Direction front, String label) {
        int x0 = cx + x;
        int x1 = x0 + w - 1;
        int z0 = cz + z;
        int z1 = z0 + d - 1;
        b.fill(x0, g, z0, x1, g, z1, p.foundation());
        b.room(x0, g + 1, z0, x1, g + 12, z1);
        b.fill(x0 + 1, g + 5, z0 + 1, x1 - 1, g + 5, z1 - 1, p.frame());
        b.fill(x0 + 1, g + 9, z0 + 1, x1 - 1, g + 9, z1 - 1, p.frame());
        b.fill(x0 + 2, g + 5, z0 + 2, x0 + 3, g + 5, z0 + 3, AIR);
        b.fill(x0 + 2, g + 9, z0 + 2, x0 + 3, g + 9, z0 + 3, AIR);
        frontWindows(b, x0, z0, x1, z1, g + 2, front);
        frontWindows(b, x0, z0, x1, z1, g + 6, front);
        frontWindows(b, x0, z0, x1, z1, g + 10, front);
        balcony(b, p, x0, z0, x1, z1, g + 5, front);
        balcony(b, p, x0, z0, x1, z1, g + 9, front);
        int doorX;
        int doorZ;
        switch (front) {
            case EAST -> {
                doorX = x1;
                doorZ = (z0 + z1) / 2;
            }
            case WEST -> {
                doorX = x0;
                doorZ = (z0 + z1) / 2;
            }
            case NORTH -> {
                doorX = (x0 + x1) / 2;
                doorZ = z0;
            }
            default -> {
                doorX = (x0 + x1) / 2;
                doorZ = z1;
            }
        }
        door(b, p, doorX, g + 1, doorZ, front);
        b.put(doorX, g + 4, doorZ, p.frame());
        CityPlanSupport.sign(b, doorX, g + 5, doorZ, front, label, "ROOMS & ALE");
        b.put(doorX, g + 6, doorZ, SHROOM);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 12, 4, w >= d ? 0 : 1);
    }

    private static void balcony(StructureBuilder b, Palette p, int x0, int z0, int x1, int z1,
                                int y, Direction front) {
        switch (front) {
            case EAST -> {
                b.fill(x1 + 1, y, z0 + 1, x1 + 2, y, z1 - 1, p.roofSlab());
                b.fill(x1 + 2, y + 1, z0 + 1, x1 + 2, y + 1, z1 - 1, p.rail());
            }
            case WEST -> {
                b.fill(x0 - 2, y, z0 + 1, x0 - 1, y, z1 - 1, p.roofSlab());
                b.fill(x0 - 2, y + 1, z0 + 1, x0 - 2, y + 1, z1 - 1, p.rail());
            }
            case NORTH -> {
                b.fill(x0 + 1, y, z0 - 2, x1 - 1, y, z0 - 1, p.roofSlab());
                b.fill(x0 + 1, y + 1, z0 - 2, x1 - 1, y + 1, z0 - 2, p.rail());
            }
            default -> {
                b.fill(x0 + 1, y, z1 + 1, x1 - 1, y, z1 + 2, p.roofSlab());
                b.fill(x0 + 1, y + 1, z1 + 2, x1 - 1, y + 1, z1 + 2, p.rail());
            }
        }
    }

    private static void frontWindows(StructureBuilder b, int x0, int z0, int x1, int z1,
                                     int y, Direction front) {
        switch (front) {
            case EAST -> {
                for (int z = z0 + 3; z <= z1 - 3; z += 4) {
                    b.window(x1, y, z, 2, 1, true);
                }
            }
            case WEST -> {
                for (int z = z0 + 3; z <= z1 - 3; z += 4) {
                    b.window(x0, y, z, 2, 1, true);
                }
            }
            case NORTH -> {
                for (int x = x0 + 3; x <= x1 - 3; x += 4) {
                    b.window(x, y, z0, 2, 1, true);
                }
            }
            default -> {
                for (int x = x0 + 3; x <= x1 - 3; x += 4) {
                    b.window(x, y, z1, 2, 1, true);
                }
            }
        }
    }

    private static void streetBar(StructureBuilder b, int cx, int cz, int g, Palette p,
                                  int x, int z, int w, int d, Direction front, String label) {
        int x0 = cx + x;
        int x1 = x0 + w - 1;
        int z0 = cz + z;
        int z1 = z0 + d - 1;
        b.fill(x0, g, z0, x1, g, z1, p.foundation());
        b.room(x0, g + 1, z0, x1, g + 5, z1);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 5, 3, w >= d ? 0 : 1);
        int midX = (x0 + x1) / 2;
        int midZ = (z0 + z1) / 2;
        switch (front) {
            case EAST -> {
                b.fill(x1, g + 1, midZ - 3, x1, g + 3, midZ + 3, AIR);
                b.fill(x1 + 1, g + 4, midZ - 3, x1 + 1, g + 4, midZ + 3, p.roofSlab());
                b.put(x1 + 1, g + 1, midZ + 4, p.frame());
                CityPlanSupport.sign(b, x1 + 1, g + 2, midZ + 4, Direction.EAST, label, "OPEN LATE");
            }
            case WEST -> {
                b.fill(x0, g + 1, midZ - 3, x0, g + 3, midZ + 3, AIR);
                b.fill(x0 - 1, g + 4, midZ - 3, x0 - 1, g + 4, midZ + 3, p.roofSlab());
                b.put(x0 - 1, g + 1, midZ + 4, p.frame());
                CityPlanSupport.sign(b, x0 - 1, g + 2, midZ + 4, Direction.WEST, label, "OPEN LATE");
            }
            case NORTH -> {
                b.fill(midX - 3, g + 1, z0, midX + 3, g + 3, z0, AIR);
                b.fill(midX - 3, g + 4, z0 - 1, midX + 3, g + 4, z0 - 1, p.roofSlab());
                b.put(midX + 4, g + 1, z0 - 1, p.frame());
                CityPlanSupport.sign(b, midX + 4, g + 2, z0 - 1, Direction.NORTH, label, "OPEN LATE");
            }
            default -> {
                b.fill(midX - 3, g + 1, z1, midX + 3, g + 3, z1, AIR);
                b.fill(midX - 3, g + 4, z1 + 1, midX + 3, g + 4, z1 + 1, p.roofSlab());
                b.put(midX + 4, g + 1, z1 + 1, p.frame());
                CityPlanSupport.sign(b, midX + 4, g + 2, z1 + 1, Direction.SOUTH, label, "OPEN LATE");
            }
        }
        b.put(midX, g + 1, midZ, p.frame());
        b.put(midX, g + 2, midZ, p.roofSlab());
        b.put(midX - 3, g + 1, midZ - 2, BARREL);
        b.put(midX + 3, g + 1, midZ - 2, BARREL);
        b.put(midX, g + 3, midZ, p.light());
    }

    // ------------------------------------------------------------------ riverbank

    private static void riverbank(StructureBuilder b, int cx, int cz, int g, Palette p) {
        ferryLanding(b, cx, cz, g, p, 190, true);
        ferryLanding(b, cx, cz, g, p, 231, false);
        riverShrine(b, cx, cz, g, p);
        riverBridge(b, cx + 226, cz, g, p, 5);

        b.put(cx + 218, g + 1, cz + 197, p.frame());
        CityPlanSupport.sign(b, cx + 218, g + 2, cz + 197, Direction.NORTH,
                "RIVER", "FERRY & BRIDGE");

        riverWarehouse(b, cx, cz, g, p, 120, 250, 30, 30, Direction.NORTH);
        riverWarehouse(b, cx, cz, g, p, 154, 250, 30, 30, Direction.NORTH);

        fishermanHut(b, cx, cz, g, p, -160, 250);
        fishermanHut(b, cx, cz, g, p, -135, 250);
        fishermanHut(b, cx, cz, g, p, -110, 250);

        palm(b, cx + 60, cz + 250, g, p);
        palm(b, cx + 90, cz + 262, g, p);
        palm(b, cx - 90, cz + 260, g, p);
        bamboo(b, cx - 200, cz + 260, g, p);
        noticeBoard(b, cx + 10, cz + 250, g, Direction.NORTH, p,
                "CITY GATE", "GRAND PALACE", "KLONG MARKET");
    }

    private static void ferryLanding(StructureBuilder b, int cx, int cz, int g, Palette p,
                                     int z, boolean north) {
        int x0 = cx - 6;
        int x1 = cx + 6;
        int z0 = cz + z;
        int z1 = z0 + 9;
        b.fill(x0, g + 1, z0, x1, g + 1, z1, p.frame());
        for (int px = x0; px <= x1; px += 3) {
            b.fill(px, g - 1, z1, px, g, z1, p.accent());
        }
        b.fill(x0, g + 2, z0, x0, g + 2, z1, p.rail());
        b.fill(x1, g + 2, z0, x1, g + 2, z1, p.rail());
        b.fill(x0 + 2, g + 2, z0 + 2, x1 - 2, g + 4, z0 + 2, p.frame());
        b.pitchedRoof(x0 + 1, z0 + 1, x1 - 1, z0 + 5, g + 5, 2, 0);
        b.put(x0 + 3, g + 3, z0 + 3, p.light());
        if (north) {
            b.put(x1 + 1, g + 1, z0 + 4, p.frame());
            CityPlanSupport.sign(b, x1 + 1, g + 2, z0 + 4, Direction.NORTH,
                    "RIVER FERRY", "STRONG CURRENT", "NO SWIMMING");
        }
    }

    private static void riverShrine(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int z = cz + 215;
        int x0 = cx - 7;
        int x1 = cx + 7;
        int z0 = z - 7;
        int z1 = z + 7;
        b.fill(x0, g - 2, z0, x1, g, z1, p.foundation());
        b.fill(x0, g + 1, z0, x1, g + 1, z1, p.frame());
        b.fill(cx - 3, g + 1, cz + 199, cx + 3, g + 1, z0 - 1, p.frame());
        b.fill(cx - 3, g + 1, z1 + 1, cx + 3, g + 1, cz + 231, p.frame());
        b.fill(cx - 3, g + 2, cz + 199, cx - 3, g + 2, z0 - 1, p.rail());
        b.fill(cx + 3, g + 2, cz + 199, cx + 3, g + 2, z0 - 1, p.rail());
        b.fill(cx - 3, g + 2, z1 + 1, cx - 3, g + 2, cz + 231, p.rail());
        b.fill(cx + 3, g + 2, z1 + 1, cx + 3, g + 2, cz + 231, p.rail());
        for (int dx = -4; dx <= 4; dx += 8) {
            for (int dz = -4; dz <= 4; dz += 8) {
                b.fill(cx + dx, g + 2, z + dz, cx + dx, g + 5, z + dz, p.accent());
            }
        }
        b.fill(cx - 5, g + 6, z - 5, cx + 5, g + 6, z + 5, p.roof());
        b.pitchedRoof(cx - 6, z - 6, cx + 6, z + 6, g + 6, 3, 0);
        b.fill(cx - 2, g + 2, z - 2, cx + 2, g + 2, z + 2, p.foundation());
        b.fill(cx - 1, g + 3, z - 1, cx + 1, g + 4, z + 1, GOLD);
        b.put(cx, g + 5, z, p.light());
        b.put(cx - 5, g + 2, z + 5, p.light());
        b.put(cx + 5, g + 2, z + 5, p.light());
        b.put(cx + 5, g + 1, z, p.frame());
        CityPlanSupport.sign(b, cx + 5, g + 2, z, Direction.EAST,
                "RIVER SPIRIT", "MAKE OFFERING", "DO NOT DISTURB");
        boat(b, cx + 12, z, g, p, false);
        boat(b, cx - 12, z, g, p, false);
    }

    private static void riverBridge(StructureBuilder b, int cx, int cz, int g, Palette p,
                                    int halfW) {
        int x0 = cx - halfW;
        int x1 = cx + halfW;
        int z0 = cz + 199;
        int z1 = cz + 231;
        b.fill(x0, g + 1, z0, x1, g + 1, z1, p.frame());
        b.fill(x0, g + 2, z0, x0, g + 2, z1, p.rail());
        b.fill(x1, g + 2, z0, x1, g + 2, z1, p.rail());
        for (int z = z0; z <= z1; z += 6) {
            b.fill(x0, g - 2, z, x0, g, z, p.accent());
            b.fill(x1, g - 2, z, x1, g, z, p.accent());
        }
        b.fill(x0, g, z0 - 1, x1, g, z0 - 1, p.foundation());
        b.fill(x0, g, z1 + 1, x1, g, z1 + 1, p.foundation());
    }

    private static void riverWarehouse(StructureBuilder b, int cx, int cz, int g, Palette p,
                                       int x, int z, int w, int d, Direction front) {
        int x0 = cx + x;
        int x1 = x0 + w - 1;
        int z0 = cz + z;
        int z1 = z0 + d - 1;
        b.fill(x0, g, z0, x1, g, z1, p.foundation());
        b.room(x0, g + 1, z0, x1, g + 10, z1);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 10, 6, w >= d ? 0 : 1);
        if (front == Direction.NORTH) {
            b.fill(x0 + 4, g + 1, z0, x1 - 4, g + 6, z0, p.frame());
            b.fill(x0 + 6, g + 1, z0, x0 + 10, g + 3, z0, AIR);
            door(b, p, x0 + 8, g + 1, z0, Direction.NORTH);
            b.put(x0 + 2, g + 7, z0 - 1, p.frame());
            CityPlanSupport.sign(b, x0 + 2, g + 8, z0 - 1, Direction.NORTH,
                    "GODOWN", "NO ENTRY");
        } else {
            b.fill(x0 + 4, g + 1, z1, x1 - 4, g + 6, z1, p.frame());
            b.fill(x0 + 6, g + 1, z1, x0 + 10, g + 3, z1, AIR);
            door(b, p, x0 + 8, g + 1, z1, Direction.SOUTH);
            b.put(x0 + 2, g + 7, z1 + 1, p.frame());
            CityPlanSupport.sign(b, x0 + 2, g + 8, z1 + 1, Direction.SOUTH,
                    "GODOWN", "NO ENTRY");
        }
        for (int px = x0 + 3; px <= x1 - 3; px += 6) {
            b.put(px, g + 1, z0 + 3, BARREL);
            b.put(px, g + 2, z0 + 3, HAY);
        }
    }

    private static void fishermanHut(StructureBuilder b, int cx, int cz, int g, Palette p,
                                     int x, int z) {
        int x0 = cx + x;
        int x1 = x0 + 12;
        int z0 = cz + z;
        int z1 = z0 + 10;
        for (int px = x0; px <= x1; px += 4) {
            for (int pz = z0; pz <= z1; pz += 5) {
                b.fill(px, g - 1, pz, px, g, pz, p.frame());
            }
        }
        b.fill(x0, g + 1, z0, x1, g + 1, z1, p.frame());
        b.walls(x0, g + 2, z0, x1, g + 4, z1, p.frame());
        b.fill(x0 + 5, g + 2, z0, x0 + 8, g + 4, z0, AIR);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 5, 3, 0);
        b.put(x0 + 2, g + 2, z0 + 2, p.light());
        b.fill(x0 + 8, g + 2, z1, x1, g + 2, z1, p.rail());
        b.put(x0 + 10, g + 1, z0 + 4, BARREL);
    }

    /** Cargo dock on the Chao Phraya, below the river godowns and east of the bridge. */
    private static void riverPort(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx + 276;
        int x1 = cx + 284;
        int z0 = cz + 195;
        int z1 = cz + 207;
        b.fill(x0, g + 1, z0, x1, g + 1, z1, p.frame());
        for (int x = x0; x <= x1; x += 4) {
            b.fill(x, g - 2, z1, x, g, z1, p.accent());
        }
        for (int z = z0 + 2; z <= z1 - 2; z += 4) {
            b.fill(x0, g - 2, z, x0, g, z, p.accent());
            b.fill(x1, g - 2, z, x1, g, z, p.accent());
        }
        b.put(x0, g + 2, z0, p.accent());
        b.put(x1, g + 2, z0, p.accent());
        b.put(x0, g + 2, z1, p.accent());
        b.put(x1, g + 2, z1, p.accent());
        b.put(cx + 280, g + 1, cz + 203, BARREL);
        b.put(cx + 282, g + 1, cz + 203, BARREL);
        b.put(cx + 280, g + 1, cz + 198, HAY);
        b.put(x0, g + 3, z1, p.light());
        b.put(cx + 280, g + 1, cz + 196, p.frame());
        CityPlanSupport.sign(b, cx + 280, g + 2, cz + 196, Direction.NORTH,
                "RIVER PORT", "CARGO QUAY");
        // The quay edge is lit and moored: bollards every eight, lanterns every twenty-four.
        for (int x = cx + 252; x <= cx + 318; x += 8) {
            b.put(x, g + 1, cz + 198, p.accent());
        }
        for (int x = cx + 256; x <= cx + 316; x += 24) {
            b.put(x, g + 2, cz + 198, p.light());
        }
    }

    /**
     * The 1920s Hua Lamphong terminus at the east edge: station hall, platform and train shed,
     * with a straight, level track approach. The surface-aware overload sits the yard and track on
     * {@link CitySurface#at(int, int)}; the legacy entry point keeps them at {@code ground}.
     */
    private static void railStation(StructureBuilder b, int cx, int cz, int ground, Palette p,
                                    @Nullable CitySurface surface) {
        int tx0 = cx + 256;
        int tx1 = cx + 334;
        int tz = cz + 70;
        int ty;
        if (surface == null) {
            ty = ground + 1;
        } else {
            ty = ground;
            for (int x = tx0; x <= tx1; x++) {
                ty = Math.max(ty, surface.at(x, tz));
            }
            for (int x = cx + 300; x <= cx + 330; x++) {
                for (int z = cz + 60; z <= cz + 90; z++) {
                    ty = Math.max(ty, surface.at(x, z));
                }
            }
            ty++;
        }

        // Yard apron: a level foundation course under the whole station.
        for (int x = cx + 300; x <= cx + 330; x++) {
            for (int z = cz + 60; z <= cz + 90; z++) {
                int sy = surface == null ? ground : surface.at(x, z);
                if (sy < ty - 1) {
                    b.fill(x, sy, z, x, ty - 2, z, p.foundation());
                }
                b.put(x, ty - 1, z, p.foundation());
            }
        }

        // Straight, level track approach east out of the city.
        for (int x = tx0; x <= tx1; x++) {
            int sy = surface == null ? ground : surface.at(x, tz);
            if (sy < ty - 1) {
                b.fill(x, sy, tz - 1, x, ty - 2, tz + 1, p.foundation());
            }
            b.fill(x, ty - 1, tz - 1, x, ty - 1, tz + 1, p.rubble());
            b.put(x, ty, tz, RAIL);
        }
        b.fill(cx + 255, ty - 1, tz - 1, cx + 255, ty, tz + 1, p.accent());

        // Station hall.
        int hx0 = cx + 302;
        int hx1 = cx + 318;
        int hz0 = cz + 60;
        int hz1 = cz + 68;
        b.fill(hx0, ty, hz0, hx1, ty, hz1, p.ground());
        b.walls(hx0, ty + 1, hz0, hx1, ty + 7, hz1, p.wall());
        b.fill(hx0, ty + 8, hz0, hx1, ty + 8, hz1, p.roof());
        b.pitchedRoof(hx0 - 2, hz0 - 2, hx1 + 2, hz1 + 2, ty + 8, 4, 0);
        b.fill(cx + 309, ty + 1, hz1, cx + 311, ty + 3, hz1, AIR);
        b.fill(cx + 309, ty + 1, hz0, cx + 311, ty + 3, hz0, AIR);
        door(b, p, cx + 310, ty + 1, hz1, Direction.SOUTH);
        door(b, p, cx + 310, ty + 1, hz0, Direction.NORTH);
        b.fill(cx + 309, ty, hz0 - 1, cx + 311, ty, hz0 - 1, p.foundation());
        for (int z = hz0 + 2; z <= hz1 - 2; z += 2) {
            b.window(hx0, ty + 3, z, 2, 1, true);
            b.window(hx1, ty + 3, z, 2, 1, true);
        }
        b.put(cx + 310, ty + 6, hz1, p.frame());
        CityPlanSupport.sign(b, cx + 310, ty + 7, hz1, Direction.SOUTH,
                "RAILWAY STATION", "PLATFORM 1", "HUA LAMPHONG");

        // Platform and train shed.
        b.fill(cx + 300, ty, cz + 69, cx + 330, ty, cz + 69, p.foundation());
        b.fill(cx + 300, ty, cz + 72, cx + 330, ty, cz + 78, p.foundation());
        b.fill(cx + 300, ty + 1, cz + 78, cx + 330, ty + 1, cz + 78, p.rail());
        for (int x = cx + 304; x <= cx + 328; x += 8) {
            b.fill(x, ty + 1, cz + 76, x, ty + 3, cz + 76, p.rail());
            b.put(x, ty + 6, cz + 76, p.light());
        }
        b.put(cx + 300, ty + 1, cz + 75, p.frame());
        CityPlanSupport.sign(b, cx + 300, ty + 2, cz + 75, Direction.WEST,
                "RAILWAY", "STATION", "PLATFORM 1");

        b.fill(cx + 300, ty + 7, cz + 69, cx + 330, ty + 7, cz + 80, p.roofSlab());
        for (int x = cx + 302; x <= cx + 328; x += 6) {
            b.fill(x, ty - 1, cz + 69, x, ty + 4, cz + 69, p.accent());
            b.fill(x, ty - 1, cz + 80, x, ty + 4, cz + 80, p.accent());
        }

        // Water tower, coal heap and crates in the yard.
        b.fill(cx + 320, ty - 1, cz + 60, cx + 324, ty - 1, cz + 64, p.foundation());
        for (int dx = 320; dx <= 324; dx += 4) {
            for (int dz = 60; dz <= 64; dz += 4) {
                b.fill(cx + dx, ty, cz + dz, cx + dx, ty + 4, cz + dz, p.frame());
            }
        }
        b.fill(cx + 320, ty + 5, cz + 60, cx + 324, ty + 6, cz + 64, p.wall());
        b.put(cx + 322, ty + 7, cz + 62, p.light());
        b.fill(cx + 325, ty - 1, cz + 63, cx + 329, ty, cz + 67, p.rubble());
        b.put(cx + 308, ty + 1, cz + 74, BARREL);
        b.put(cx + 310, ty + 1, cz + 74, BARREL);
        b.put(cx + 310, ty + 1, cz + 73, p.light());
    }

    // ------------------------------------------------------------------ props

    private static void lanternPost(StructureBuilder b, int x, int z, int g, Palette p) {
        b.put(x, g + 1, z, p.rail());
        b.put(x, g + 2, z, p.rail());
        b.put(x, g + 3, z, p.light());
    }

    private static void spiritHouse(StructureBuilder b, int x, int z, int g, Palette p) {
        b.put(x, g + 1, z, p.accent());
        b.put(x, g + 2, z, p.accent());
        b.fill(x - 1, g + 3, z - 1, x + 1, g + 3, z + 1, GOLD);
        b.put(x, g + 4, z, p.wall());
        b.pitchedRoof(x - 1, z - 1, x + 1, z + 1, g + 5, 2, 0);
        b.put(x, g + 7, z, p.light());
    }

    private static void palm(StructureBuilder b, int x, int z, int g, Palette p) {
        for (int y = 1; y <= 6; y++) {
            b.put(x, g + y, z, p.frame());
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 3) {
                    b.put(x + dx, g + 6, z + dz, LEAVES);
                }
            }
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 2) {
                    b.put(x + dx, g + 7, z + dz, LEAVES);
                }
            }
        }
    }

    private static void bamboo(StructureBuilder b, int x, int z, int g, Palette p) {
        int[] heights = {5, 7, 6, 8, 4};
        for (int i = 0; i < 5; i++) {
            int px = x + i % 3 - 1;
            int pz = z + i / 3 - 1;
            b.fill(px, g + 1, pz, px, g + heights[i], pz, BAMBOO);
        }
        b.put(x, g + 8, z, LEAVES);
    }

    private static void door(StructureBuilder b, Palette p, int x, int y, int z,
                             Direction facing) {
        BlockState lower = p.door()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        b.put(x, y, z, lower);
        b.put(x, y + 1, z,
                lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
    }
}
