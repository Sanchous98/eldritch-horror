package com.sanchous98.eldritchhorror.world.loc.city.plan;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.SiteLoot;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.style.Materials;
import java.util.List;
import java.util.function.IntBinaryOperator;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.RailShape;

/**
 * Jakarta / colonial Batavia: the whitewashed stadhuis on its square, the Dutch harbour with
 * warehouses and cranes, Glodok's Chinese shophouses, the kampung lanes on the canal's south
 * bank, and the canal itself. Every block is placed deliberately; there is no layout RNG.
 *
 * <p>The whole city is written on every call, as the plan contract requires: the builder keeps
 * only the slice that falls inside the generating chunk, so every layout decision is absolute and
 * chunk-independent.
 */
public final class JakartaPlan implements CityPlan {

    /** Authored half-extent; everything below stays inside +/-335. */
    private static final int HALF_EXTENT = 340;

    @Override
    public String cityId() {
        return "jakarta";
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

    /**
     * One authored city. The surface function is used only for the pieces on the graded rim
     * (the arrival gate and the rail station); the core stays on the flat heart at {@code ground}.
     */
    private static void compose(StructureBuilder b, int cx, int cz, int ground, Palette p,
                                IntBinaryOperator surface) {
        mainAxis(b, cx, cz, ground, p, surface);
        townHallSquare(b, cx, cz, ground, p);
        arrivalGate(b, cx, cz, p, surface);
        railStation(b, cx, cz, p, surface);
        harbour(b, cx, cz, ground, p);
        portDock(b, cx, cz, ground, p);
        glodok(b, cx, cz, ground, p);
        kampung(b, cx, cz, ground, p);
        canal(b, cx, cz, ground, p);
        secretCellar(b, cx, cz, ground, p);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.LANDMARK, 0, 0, 320, ""),
                new CityPoi("rail_station", CityPoi.Kind.STATION, 3, 1, -300,
                        "goods_yard;north_south"),
                new CityPoi("gate_signpost", CityPoi.Kind.SIGN, 10, 1, 328,
                        "TOWN HALL|OLD HARBOUR|GLODOK|KAMPUNG|CANAL"),
                new CityPoi("city_notices", CityPoi.Kind.SIGN, -12, 1, 331, "CITY NOTICES"),
                new CityPoi("town_hall", CityPoi.Kind.LANDMARK, 0, 0, -180, "stadhuis"),
                new CityPoi("church", CityPoi.Kind.LANDMARK, -116, 0, -202, "de kerk"),
                new CityPoi("batavia_hotel", CityPoi.Kind.INN, -80, 0, -120, "hotel_keeper"),
                new CityPoi("hotel_keeper", CityPoi.Kind.NPC, -80, 0, -116, "batavia_hotel"),
                new CityPoi("harbour_quest_board", CityPoi.Kind.QUEST_BOARD, 220, 0, 0,
                        "jakarta_drowned_ledger,jakarta_missing_sailor"),
                new CityPoi("customs_house", CityPoi.Kind.NPC, 240, 0, -120,
                        "customs_clerk,jakarta_drowned_ledger"),
                new CityPoi("dockworker", CityPoi.Kind.NPC, 250, 0, 20, "old_harbour"),
                new CityPoi("old_harbour_port", CityPoi.Kind.PORT, 268, 1, 60, "quay_jetty"),
                new CityPoi("warehouse_cellar", CityPoi.Kind.SECRET, 240, -3, 200,
                        "smuggled_cargo,shrine"),
                new CityPoi("glodok_shop", CityPoi.Kind.SHOP, -240, 0, -60, "shop_merchant"),
                new CityPoi("shop_merchant", CityPoi.Kind.NPC, -238, 0, -60, "glodok_shop"),
                new CityPoi("glodok_market", CityPoi.Kind.LANDMARK, -294, 0, 28, "pasar"),
                new CityPoi("herbalist", CityPoi.Kind.HEALER, -240, 0, 120, "herbalist"),
                new CityPoi("herbalist_npc", CityPoi.Kind.NPC, -238, 0, 120, "herbalist"),
                new CityPoi("canal_bridge", CityPoi.Kind.LANDMARK, 0, 0, 95, "barges"),
                new CityPoi("canal_warning", CityPoi.Kind.SIGN, 30, 1, 111, "DEEP WATER"),
                new CityPoi("kampung_well", CityPoi.Kind.NPC, -200, 0, 220,
                        "fisherman,jakarta_missing_sailor"),
                new CityPoi("fisherman", CityPoi.Kind.NPC, -199, 0, 222, "kampung_well"),
                new CityPoi("mosque", CityPoi.Kind.LANDMARK, 120, 0, 220, "imam"),
                new CityPoi("imam", CityPoi.Kind.NPC, 120, 0, 218, "mosque"));
    }

    // ------------------------------------------------------------------ shared helpers

    private static void pave(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                             BlockState state) {
        b.fill(x0, y, z0, x1, y, z1, state);
    }

    private static void outline(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                                BlockState state) {
        b.fill(x0, y, z0, x1, y, z0, state);
        b.fill(x0, y, z1, x1, y, z1, state);
        b.fill(x0, y, z0, x0, y, z1, state);
        b.fill(x1, y, z0, x1, y, z1, state);
    }

    /** A wall-mounted sign with a small timber bracket beneath it. */
    private static void sign(StructureBuilder b, Palette p, int x, int y, int z,
                             Direction facing, String... lines) {
        b.put(x, y - 1, z, p.accent());
        CityPlanSupport.sign(b, x, y, z, facing, lines);
    }

    /** A freestanding signpost: two timber blocks and the sign on top. */
    private static void signPost(StructureBuilder b, int x, int z, int ground, Palette p,
                                 Direction facing, String... lines) {
        signPostAt(b, x, z, ground, p, facing, lines);
    }

    /** A signpost whose base is an explicit Y (used on the graded rim). */
    private static void signPostAt(StructureBuilder b, int x, int z, int y, Palette p,
                                   Direction facing, String... lines) {
        b.put(x, y + 1, z, p.accent());
        b.put(x, y + 2, z, p.accent());
        CityPlanSupport.sign(b, x, y + 3, z, facing, lines);
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

    private static void stairs(StructureBuilder b, BlockState state, int x, int y, int z,
                               Direction facing) {
        b.put(x, y, z, state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing));
    }

    private static void quoins(StructureBuilder b, int x0, int z0, int x1, int z1,
                               int y0, int y1, Palette p) {
        for (int y = y0; y <= y1; y++) {
            if (((y - y0) & 1) != 0) {
                continue;
            }
            b.put(x0, y, z0, p.accent());
            b.put(x1, y, z0, p.accent());
            b.put(x0, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
    }

    private static void lanternPost(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground + 1, z, p.accent());
        b.put(x, ground + 2, z, p.accent());
        b.put(x, ground + 3, z, p.light());
    }

    private static void palm(StructureBuilder b, int x, int z, int ground, Palette p) {
        int height = 6 + Math.floorMod(x * 7 + z * 13, 3);
        for (int y = ground + 1; y <= ground + height; y++) {
            b.put(x, y, z, p.accent());
        }
        BlockState leaf = Blocks.JUNGLE_LEAVES.defaultBlockState();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 2) {
                    b.put(x + dx, ground + height + 1, z + dz, leaf);
                }
            }
        }
        b.put(x, ground + height + 2, z, leaf);
        b.put(x - 1, ground + height, z, leaf);
        b.put(x + 1, ground + height, z, leaf);
        b.put(x, ground + height, z - 1, leaf);
        b.put(x, ground + height, z + 1, leaf);
    }

    private static void grove(StructureBuilder b, int cx, int cz, int ground, Palette p,
                              int x0, int z0, int x1, int z1, int count) {
        for (int i = 0; i < count; i++) {
            int x = x0 + Math.floorMod(i * 17 + i * i * 5, x1 - x0);
            int z = z0 + Math.floorMod(i * 29 + i * i * 7, z1 - z0);
            palm(b, cx + x, cz + z, ground, p);
        }
    }

    private static void crate(StructureBuilder b, int x, int z, int ground, Palette p,
                              int height) {
        for (int i = 1; i <= height; i++) {
            b.put(x, ground + i, z, p.foundation());
        }
    }

    private static void marketStall(StructureBuilder b, int x, int z, int ground, Palette p,
                                    BlockState canopy) {
        b.fill(x, ground + 1, z, x, ground + 2, z, p.accent());
        b.fill(x + 3, ground + 1, z, x + 3, ground + 2, z, p.accent());
        b.fill(x, ground + 1, z + 2, x, ground + 2, z + 2, p.accent());
        b.fill(x + 3, ground + 1, z + 2, x + 3, ground + 2, z + 2, p.accent());
        b.fill(x, ground + 3, z, x + 3, ground + 3, z + 2, canopy);
        b.fill(x, ground + 1, z + 1, x + 2, ground + 1, z + 1, p.foundation());
        b.put(x + 1, ground + 2, z + 1, p.light());
    }

    private static void wellHead(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 2, ground, z - 2, x + 2, ground, z + 2, p.foundation());
        b.fill(x - 1, ground, z - 1, x + 1, ground, z + 1, Blocks.AIR.defaultBlockState());
        b.fill(x - 1, ground - 1, z - 1, x + 1, ground - 1, z + 1,
                Blocks.WATER.defaultBlockState());
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                b.put(x + dx, ground, z + dz, p.weathered());
            }
        }
        b.fill(x - 1, ground + 1, z - 1, x - 1, ground + 3, z - 1, p.accent());
        b.fill(x + 1, ground + 1, z - 1, x + 1, ground + 3, z - 1, p.accent());
        b.fill(x - 1, ground + 1, z + 1, x - 1, ground + 3, z + 1, p.accent());
        b.fill(x + 1, ground + 1, z + 1, x + 1, ground + 3, z + 1, p.accent());
        b.fill(x - 2, ground + 4, z - 2, x + 2, ground + 4, z + 2, p.roofSlab());
        b.put(x, ground + 2, z, p.rail());
        b.put(x, ground + 3, z, p.light());
    }

    private static void parterre(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 7, ground + 1, z - 4, x + 7, ground + 1, z + 4, p.weathered());
        b.fill(x - 6, ground + 1, z - 3, x + 6, ground + 1, z + 3,
                Blocks.DIRT.defaultBlockState());
        BlockState leaf = p.overgrowth() != null ? p.overgrowth() : p.rail();
        for (int dx = -6; dx <= 6; dx += 3) {
            b.put(x + dx, ground + 2, z - 3, leaf);
            b.put(x + dx, ground + 2, z + 3, leaf);
        }
        b.put(x, ground + 2, z, p.accent());
        b.put(x, ground + 3, z, leaf);
    }

    private static void flagpole(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 3, ground, z - 3, x + 3, ground, z + 3, p.foundation());
        b.fill(x - 2, ground + 1, z - 2, x + 2, ground + 1, z + 2, p.wall());
        for (int y = ground + 2; y <= ground + 17; y++) {
            b.put(x, y, z, p.accent());
        }
        b.put(x, ground + 18, z, p.light());
        b.fill(x + 1, ground + 13, z, x + 5, ground + 14, z, Materials.wool(DyeColor.RED));
        b.fill(x + 1, ground + 15, z, x + 5, ground + 16, z, Materials.wool(DyeColor.WHITE));
        b.put(x - 3, ground + 1, z - 4, p.foundation());
        b.put(x + 3, ground + 1, z - 4, p.foundation());
        b.put(x - 3, ground + 1, z + 4, p.foundation());
        b.put(x + 3, ground + 1, z + 4, p.foundation());
    }

    private static int hash(int x, int z) {
        int h = x * 0x9E3779B9 ^ z * 0x85EBCA6B;
        h ^= h >>> 16;
        h *= 0x7FEB352D;
        h ^= h >>> 15;
        return h;
    }

    private static boolean overlaps(int a0, int a1, int b0, int b1) {
        return a0 <= b1 && b0 <= a1;
    }

    /** Cuts/fills one footprint to a level Y, using the authored surface as the existing ground. */
    private static void seat(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                             Palette p, IntBinaryOperator surface) {
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                int s = surface.applyAsInt(x, z);
                if (s > y) {
                    b.fill(x, y + 1, z, x, s, z, air);
                } else if (s < y) {
                    b.fill(x, s + 1, z, x, y, z, p.foundation());
                }
            }
        }
    }

    // ------------------------------------------------------------------ arrival gate

    private static void mainAxis(StructureBuilder b, int cx, int cz, int ground, Palette p,
                                 IntBinaryOperator surface) {
        CityPlanSupport.path(b, cx, cz + 336, cx, cz - 100, 14, ground, p.ground());
        for (int z = -100; z <= 336; z++) {
            b.put(cx - 7, ground, cz + z, p.foundation());
            b.put(cx + 8, ground, cz + z, p.foundation());
        }
        // The last stretch to the gate follows the graded rim instead of a plate at ground.
        for (int z = 300; z <= 336; z++) {
            for (int x = -7; x <= 8; x++) {
                int y = surface.applyAsInt(cx + x, cz + z);
                b.put(cx + x, y, cz + z, x == -7 || x == 8 ? p.foundation() : p.ground());
            }
        }
        for (int z = -80; z <= 280; z += 40) {
            if (z == 80 || z == 160) {
                continue; // the canal bridge and the kampung lane have their own lights
            }
            lanternPost(b, cx - 10, cz + z, ground, p);
            lanternPost(b, cx + 11, cz + z, ground, p);
        }
    }

    private static void arrivalGate(StructureBuilder b, int cx, int cz, Palette p,
                                    IntBinaryOperator surface) {
        int gateY = surface.applyAsInt(cx, cz + 319);
        // Level the gate pad on the graded rim; the road through it stays walkable.
        for (int x = cx - 18; x <= cx + 18; x++) {
            for (int z = cz + 312; z <= cz + 326; z++) {
                int s = surface.applyAsInt(x, z);
                if (s > gateY) {
                    b.fill(x, gateY + 1, z, x, s, z, Blocks.AIR.defaultBlockState());
                } else if (s < gateY) {
                    b.fill(x, s + 1, z, x, gateY, z, p.foundation());
                }
            }
        }
        b.fill(cx - 7, gateY, cz + 312, cx + 7, gateY, cz + 326, p.ground());
        gateTower(b, cx - 16, cz + 314, cx - 8, cz + 322, gateY, p);
        gateTower(b, cx + 8, cz + 314, cx + 16, cz + 322, gateY, p);
        b.fill(cx - 8, gateY + 6, cz + 318, cx + 8, gateY + 8, cz + 320, p.wall());
        b.fill(cx - 8, gateY + 9, cz + 318, cx + 8, gateY + 9, cz + 320, p.roofSlab());
        b.fill(cx - 7, gateY + 1, cz + 314, cx + 7, gateY + 5, cz + 324,
                Blocks.AIR.defaultBlockState());
        sign(b, p, cx, gateY + 10, cz + 319, Direction.SOUTH, "JAKARTA");
        b.fill(cx - 16, gateY + 5, cz + 322, cx - 15, gateY + 8, cz + 322,
                Materials.wool(DyeColor.RED));
        b.fill(cx + 15, gateY + 5, cz + 322, cx + 16, gateY + 8, cz + 322,
                Materials.wool(DyeColor.WHITE));
        String[] lines = {"TOWN HALL", "OLD HARBOUR", "GLODOK", "KAMPUNG", "CANAL"};
        for (int i = 0; i < lines.length; i++) {
            int x = cx + 9 + i * 4;
            signPostAt(b, x, cz + 328, surface.applyAsInt(x, cz + 328), p,
                    Direction.SOUTH, lines[i]);
        }
        int noticeY = surface.applyAsInt(cx - 11, cz + 331);
        b.fill(cx - 15, noticeY + 1, cz + 331, cx - 7, noticeY + 2, cz + 331, p.accent());
        sign(b, p, cx - 14, noticeY + 3, cz + 331, Direction.SOUTH, "CITY NOTICES");
        sign(b, p, cx - 12, noticeY + 3, cz + 331, Direction.SOUTH, "CURFEW AT DUSK");
        sign(b, p, cx - 10, noticeY + 3, cz + 331, Direction.SOUTH, "BEWARE CANAL");
        b.fill(cx - 16, noticeY + 4, cz + 330, cx - 6, noticeY + 4, cz + 332, p.roofSlab());
        b.put(cx - 17, gateY + 1, cz + 318, p.light());
        b.put(cx + 17, gateY + 1, cz + 318, p.light());
    }

    private static void gateTower(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  int ground, Palette p) {
        b.room(x0, ground, z0, x1, ground + 9, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 9, p);
        b.window(x0 + 2, ground + 4, z0, 2, 1, true);
        b.window(x1 - 2, ground + 4, z1, 2, 1, true);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 9, 4, 0);
        b.put(x0 + 1, ground + 10, z0 + 1, p.light());
    }

    // ------------------------------------------------------------------ rail station

    private static void railStation(StructureBuilder b, int cx, int cz, Palette p,
                                    IntBinaryOperator surface) {
        int stationY = surface.applyAsInt(cx, cz - 300);
        // Straight, level track from the district edge to the yard.
        seat(b, cx - 1, cz - 340, cx, cz - 272, stationY, p, surface);
        b.fill(cx - 1, stationY, cz - 340, cx, stationY, cz - 272, p.rubble());
        BlockState rail = Blocks.RAIL.defaultBlockState()
                .setValue(BlockStateProperties.RAIL_SHAPE, RailShape.NORTH_SOUTH);
        for (int z = cz - 340; z <= cz - 272; z++) {
            b.put(cx, stationY + 1, z, rail);
        }
        // Platform east of the track: five wide, its edge three blocks off the centreline, with
        // six blocks of shelter and a lantern under the canopy.
        seat(b, cx + 3, cz - 322, cx + 7, cz - 282, stationY + 1, p, surface);
        b.fill(cx + 3, stationY + 1, cz - 322, cx + 7, stationY + 1, cz - 282,
                p.foundation());
        for (int z = cz - 318; z <= cz - 288; z += 6) {
            b.fill(cx + 7, stationY + 2, z, cx + 7, stationY + 6, z, p.accent());
            b.put(cx + 8, stationY + 3, z, p.light());
        }
        b.fill(cx + 3, stationY + 7, cz - 322, cx + 8, stationY + 7, cz - 282,
                p.roofSlab());
        // Station building east of the platform.
        int x0 = cx + 8;
        int x1 = cx + 30;
        int z0 = cz - 320;
        int z1 = cz - 296;
        seat(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, stationY + 1, p, surface);
        b.room(x0, stationY + 1, z0, x1, stationY + 11, z1);
        b.fill(x0, stationY + 1, z0, x1, stationY + 1, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, stationY + 2, stationY + 11, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, stationY + 11, 6, 0);
        door(b, p, x0, stationY + 2, cz - 308, Direction.WEST);
        door(b, p, cx + 12, stationY + 2, z1, Direction.SOUTH);
        for (int x = x0 + 4; x <= x1 - 4; x += 6) {
            b.window(x, stationY + 7, z0, 2, 2, true);
            b.window(x, stationY + 7, z1, 2, 2, true);
        }
        for (int z = z0 + 5; z <= z1 - 5; z += 6) {
            b.window(x1, stationY + 4, z, 2, 1, true);
        }
        sign(b, p, x0 - 1, stationY + 6, cz - 308, Direction.WEST, "RAILWAY STATION");
        sign(b, p, cx + 12, stationY + 4, z1 + 1, Direction.SOUTH, "JAKARTA STATION");
        b.put(cx + 12, stationY + 11, cz - 306, p.light());
        // Goods yard east of the station.
        int yx0 = cx + 34;
        int yx1 = cx + 52;
        int yz0 = cz - 318;
        int yz1 = cz - 298;
        seat(b, yx0 - 1, yz0 - 1, yx1 + 1, yz1 + 1, stationY + 1, p, surface);
        b.fill(yx0, stationY + 1, yz0, yx1, stationY + 1, yz1, p.rubble());
        b.walls(yx0, stationY + 2, yz0, yx1, stationY + 5, yz1, p.weathered());
        b.fill(yx0 + 1, stationY + 2, yz0 + 1, yx1 - 1, stationY + 5, yz1 - 1,
                Blocks.AIR.defaultBlockState());
        b.pitchedRoof(yx0 - 1, yz0 - 1, yx1 + 1, yz1 + 1, stationY + 6, 4, 1);
        crate(b, yx0 + 3, yz0 + 3, stationY + 1, p, 2);
        crate(b, yx0 + 4, yz0 + 3, stationY + 1, p, 1);
        crate(b, yx1 - 4, yz1 - 3, stationY + 1, p, 2);
        b.put(yx0 + 2, stationY + 5, yz0 + 2, p.light());
        // Paved approach from the town-hall square.
        for (int z = cz - 296; z <= cz - 245; z++) {
            for (int x = cx + 8; x <= cx + 16; x++) {
                b.put(x, surface.applyAsInt(x, z), z, p.ground());
            }
        }
        stairs(b, p.roofStairs(), cx + 12, stationY, z1 + 2, Direction.SOUTH);
        signPostAt(b, cx + 10, cz - 244, surface.applyAsInt(cx + 10, cz - 244), p,
                Direction.SOUTH, "RAILWAY STATION");
    }

    // ------------------------------------------------------------------ town hall square

    private static void townHallSquare(StructureBuilder b, int cx, int cz, int ground,
                                       Palette p) {
        pave(b, cx - 100, cz - 245, cx + 100, cz - 100, ground, p.ground());
        outline(b, cx - 100, cz - 245, cx + 100, cz - 100, ground, p.foundation());
        flagpole(b, cx, cz - 132, ground, p);
        parterre(b, cx - 62, cz - 232, ground, p);
        parterre(b, cx + 54, cz - 232, ground, p);
        parterre(b, cx + 60, cz - 112, ground, p);
        b.monument(cx - 84, cz - 236, ground);
        b.monument(cx + 84, cz - 236, ground);
        int[][] palms = {{-96, -240}, {96, -240}, {-96, -104}, {96, -104}, {-46, -244},
                {46, -244}, {-40, -108}, {40, -108}};
        for (int[] spot : palms) {
            palm(b, cx + spot[0], cz + spot[1], ground, p);
        }
        townHall(b, cx, cz, ground, p);
        church(b, cx, cz, ground, p);
        hotel(b, cx, cz, ground, p);
        cafe(b, cx + 66, cz - 232, 22, 18, ground, p, "CAFE BATAVIA");
        cafe(b, cx + 66, cz - 152, 22, 18, ground, p, "DE KROON");
        signPost(b, cx + 10, cz - 100, ground, p, Direction.SOUTH, "TOWN HALL");
        signPost(b, cx - 40, cz - 160, ground, p, Direction.SOUTH, "STADHUIS");
    }

    private static void townHall(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 32;
        int x1 = cx + 32;
        int z0 = cz - 196;
        int z1 = cz - 166;
        b.room(x0, ground, z0, x1, ground + 13, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 13, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 13, 8, 0);
        for (int x = x0 + 4; x <= x1 - 4; x += 7) {
            b.fill(x, ground + 1, z1 + 2, x, ground + 7, z1 + 2, p.wall());
            b.put(x, ground + 1, z1 + 2, p.foundation());
        }
        b.fill(x0 - 2, ground + 7, z1, x1 + 2, ground + 7, z1 + 3, p.roofSlab());
        b.fill(x0 - 2, ground + 8, z1, x1 + 2, ground + 8, z1 + 3, p.wall());
        b.fill(x0, ground + 9, z1 + 1, x1, ground + 9, z1 + 2, p.wall());
        b.fill(cx - 20, ground + 10, z1 + 1, cx + 20, ground + 10, z1 + 2, p.wall());
        b.fill(cx - 10, ground + 11, z1 + 1, cx + 10, ground + 11, z1 + 2, p.wall());
        b.fill(cx - 6, ground, z1 + 4, cx + 6, ground, z1 + 6, p.foundation());
        door(b, p, cx, ground + 1, z1, Direction.SOUTH);
        for (int z = z0 + 4; z <= z1 - 4; z += 6) {
            b.window(x0, ground + 4, z, 3, 1, true);
            b.window(x1, ground + 4, z, 3, 1, true);
            b.window(x0, ground + 9, z, 2, 1, true);
            b.window(x1, ground + 9, z, 2, 1, true);
        }
        clockTower(b, cx, cz - 181, ground, p);
        for (int x = x0 + 8; x <= x1 - 8; x += 8) {
            b.fill(x, ground + 1, cz - 190, x, ground + 12, cz - 190, p.accent());
            b.fill(x, ground + 1, cz - 172, x, ground + 12, cz - 172, p.accent());
        }
        b.fill(cx - 10, ground + 1, z0 + 1, cx + 10, ground + 2, z0 + 4, p.foundation());
        b.put(cx, ground + 3, z0 + 2, p.accent());
        b.put(cx, ground + 12, cz - 181, p.light());
        sign(b, p, cx + 8, ground + 8, z1 + 3, Direction.SOUTH, "STADHUIS");
    }

    private static void clockTower(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.room(x - 6, ground, z - 6, x + 6, ground + 27, z + 6);
        b.fill(x - 6, ground, z - 6, x + 6, ground, z + 6, p.foundation());
        quoins(b, x - 6, z - 6, x + 6, z + 6, ground + 1, ground + 27, p);
        for (int y = ground + 4; y <= ground + 24; y += 5) {
            b.window(x - 6, y, z, 2, 1, true);
            b.window(x + 6, y, z, 2, 1, true);
            b.window(x, y, z - 6, 2, 1, true);
        }
        b.fill(x - 3, ground + 22, z + 7, x + 3, ground + 26, z + 7, p.wall());
        b.fill(x - 2, ground + 23, z + 8, x + 2, ground + 25, z + 8,
                Materials.whiteConcrete());
        b.put(x, ground + 23, z + 8, p.frame());
        b.put(x, ground + 24, z + 8, p.frame());
        b.put(x + 1, ground + 23, z + 8, p.frame());
        for (int i = 0; i < 4; i++) {
            int r = 6 - i;
            b.fill(x - r, ground + 28 + i, z - r, x + r, ground + 28 + i, z + r, p.roof());
        }
        b.put(x, ground + 32, z, p.light());
        b.put(x, ground + 33, z, p.light());
    }

    private static void church(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 130;
        int x1 = cx - 102;
        int z0 = cz - 222;
        int z1 = cz - 182;
        b.room(x0, ground, z0, x1, ground + 14, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 14, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 14, 10, 1);
        b.room(cx - 112, ground, cz - 218, cx - 104, ground + 26, cz - 210);
        b.fill(cx - 112, ground, cz - 218, cx - 104, ground, cz - 210, p.foundation());
        quoins(b, cx - 112, cz - 218, cx - 104, cz - 210, ground + 1, ground + 26, p);
        for (int y = ground + 6; y <= ground + 22; y += 5) {
            b.window(cx - 112, y, cz - 214, 2, 1, true);
            b.window(cx - 104, y, cz - 214, 2, 1, true);
        }
        for (int i = 0; i < 5; i++) {
            int r = 4 - i;
            b.fill(cx - 108 - r, ground + 27 + i, cz - 214 - r,
                    cx - 108 + r, ground + 27 + i, cz - 214 + r, p.roof());
        }
        b.put(cx - 108, ground + 32, cz - 214, p.light());
        door(b, p, x1, ground + 1, cz - 202, Direction.EAST);
        for (int z = z0 + 6; z <= z1 - 6; z += 7) {
            b.window(x0, ground + 5, z, 4, 1, true);
            b.window(x1, ground + 5, z, 4, 1, true);
        }
        b.window(x0, ground + 10, cz - 202, 3, 1, true);
        b.window(x1, ground + 10, cz - 202, 3, 1, true);
        sign(b, p, x1 + 1, ground + 5, cz - 202, Direction.EAST, "DE KERK");
        // Churchyard: low wall, headstones and a cypress or two.
        int gx0 = cx - 142;
        int gx1 = cx - 132;
        int gz0 = cz - 220;
        int gz1 = cz - 184;
        b.walls(gx0, ground + 1, gz0, gx1, ground + 1, gz1, p.weathered());
        for (int z = gz0 + 2; z <= gz1 - 2; z += 5) {
            b.put(gx0 + 2, ground + 1, z, p.weathered());
            b.put(gx0 + 2, ground + 2, z, p.weathered());
            b.put(gx0 + 7, ground + 1, z, p.foundation());
            b.put(gx0 + 7, ground + 2, z, p.foundation());
        }
        palm(b, cx - 146, cz - 226, ground, p);
        palm(b, cx - 146, cz - 178, ground, p);
    }

    private static void hotel(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 92;
        int x1 = cx - 68;
        int z0 = cz - 132;
        int z1 = cz - 108;
        b.room(x0, ground, z0, x1, ground + 12, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 12, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 12, 7, 0);
        for (int x = x0 + 2; x <= x1 - 2; x += 5) {
            b.fill(x, ground + 1, z1 + 2, x, ground + 5, z1 + 2, p.accent());
        }
        b.fill(x0 - 1, ground + 6, z1 + 1, x1 + 1, ground + 6, z1 + 3, p.roofSlab());
        b.fill(x0 - 1, ground + 1, z1 + 3, cx - 82, ground + 1, z1 + 3, p.rail());
        b.fill(cx - 78, ground + 1, z1 + 3, x1 + 1, ground + 1, z1 + 3, p.rail());
        door(b, p, cx - 80, ground + 1, z1, Direction.SOUTH);
        for (int x = x0 + 3; x <= x1 - 3; x += 5) {
            b.window(x, ground + 8, z1, 2, 2, true);
        }
        for (int z = z0 + 3; z <= z1 - 3; z += 5) {
            b.window(x0, ground + 4, z, 3, 1, true);
            b.window(x1, ground + 4, z, 3, 1, true);
        }
        b.fill(x0 + 1, ground + 7, z0 + 1, x1 - 1, ground + 7, z0 + 7, p.roofSlab());
        b.fill(cx - 88, ground + 8, z0 + 2, cx - 86, ground + 8, z0 + 4,
                Materials.wool(DyeColor.RED));
        b.fill(cx - 78, ground + 8, z0 + 2, cx - 76, ground + 8, z0 + 4,
                Materials.wool(DyeColor.WHITE));
        b.put(cx - 80, ground + 12, cz - 120, p.light());
        b.put(cx - 84, ground + 1, cz - 116, p.accent());
        b.put(cx - 84, ground + 2, cz - 116, p.light());
        sign(b, p, cx - 80, ground + 7, z1 + 2, Direction.SOUTH, "BATAVIA HOTEL");
    }

    private static void cafe(StructureBuilder b, int x0, int z0, int w, int d, int ground,
                             Palette p, String name) {
        int x1 = x0 + w - 1;
        int z1 = z0 + d - 1;
        b.room(x0, ground, z0, x1, ground + 10, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 10, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 10, 6, 0);
        for (int z = z0 + 2; z <= z1 - 2; z += 4) {
            b.fill(x0 - 2, ground + 1, z, x0 - 2, ground + 4, z, p.accent());
        }
        b.fill(x0 - 3, ground + 5, z0 + 1, x0 - 1, ground + 5, z1 - 1, p.roofSlab());
        door(b, p, x0, ground + 1, z0 + d / 2, Direction.WEST);
        b.window(x1, ground + 6, z0 + 5, 2, 1, true);
        b.window(x1, ground + 6, z1 - 5, 2, 1, true);
        b.window(x0, ground + 7, z0 + 3, 2, 1, true);
        b.window(x0, ground + 7, z1 - 3, 2, 1, true);
        b.put(x0 + 1, ground + 1, z0 + 1, p.accent());
        b.put(x0 + 2, ground + 1, z0 + 1, p.accent());
        b.put(x0 + 1, ground + 2, z0 + 1, p.light());
        sign(b, p, x0 - 2, ground + 4, z0 + 3, Direction.WEST, name);
        b.put(x0 - 5, ground + 1, z0 + 7, p.rail());
        b.put(x0 - 6, ground + 1, z0 + 7, p.roofSlab());
        b.put(x0 - 5, ground + 1, z0 + 10, p.rail());
        b.put(x0 - 6, ground + 1, z0 + 10, p.roofSlab());
    }

    // ------------------------------------------------------------------ harbour

    private static void harbour(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        CityPlanSupport.path(b, cx + 250, cz - 255, cx + 250, cz + 285, 10, ground, p.ground());
        for (int z = -255; z <= 285; z++) {
            b.put(cx + 245, ground, cz + z, p.foundation());
        }
        harbourBasin(b, cx, cz, ground, p);
        pave(b, cx + 160, cz - 36, cx + 244, cz + 36, ground, p.ground());
        outline(b, cx + 160, cz - 36, cx + 244, cz + 36, ground, p.foundation());
        questBoard(b, cx, cz, ground, p);
        harbourArch(b, cx, cz, ground, p);
        for (int z = -220; z <= 20; z += 60) {
            crane(b, cx + 257, cz + z, ground, p);
        }
        for (int z = -240; z <= 280; z += 40) {
            if (z == 80) {
                continue; // the canal culvert crosses the quay here
            }
            lanternPost(b, cx + 245, cz + z, ground, p);
        }
        customsHouse(b, cx, cz, ground, p);
        warehouse(b, cx + 168, cz - 244, cx + 206, cz - 220, ground, p, "VOC 1", 0);
        warehouse(b, cx + 168, cz - 196, cx + 206, cz - 172, ground, p, "VOC 2", 0);
        warehouse(b, cx + 168, cz - 150, cx + 206, cz - 126, ground, p, "VOC 3", 0);
        warehouse(b, cx + 168, cz - 80, cx + 206, cz - 56, ground, p, "VOC 4", 1);
        warehouse(b, cx + 168, cz + 40, cx + 206, cz + 64, ground, p, "VOC 5", 1);
        warehouse(b, cx + 168, cz + 150, cx + 206, cz + 174, ground, p, "VOC 6", 0);
        warehouse(b, cx + 210, cz + 140, cx + 244, cz + 164, ground, p, "VOC 7", 2);
        warehouse(b, cx + 210, cz + 176, cx + 244, cz + 214, ground, p, "VOC 8", 2);
        warehouse(b, cx + 168, cz + 236, cx + 206, cz + 260, ground, p, "VOC 9", 1);
        warehouse(b, cx + 210, cz + 236, cx + 244, cz + 264, ground, p, "VOC 10", 2);
        ruin(b, cx + 150, cz + 250, cx + 166, cz + 266, ground, p);
        for (int i = 0; i < 12; i++) {
            crate(b, cx + 247, cz - 240 + i * 9, ground, p, 1 + (i & 1));
        }
        signPost(b, cx + 220, cz + 38, ground, p, Direction.SOUTH, "OLD HARBOUR");
    }

    private static void harbourBasin(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 258;
        int x1 = cx + 334;
        int z0 = cz - 250;
        int z1 = cz + 125;
        b.fill(x0, ground - 5, z0, x1, ground - 5, z1, Blocks.SAND.defaultBlockState());
        b.fill(x0, ground - 4, z0, x1, ground - 1, z1, Blocks.WATER.defaultBlockState());
        b.fill(cx + 256, ground - 4, cz - 250, cx + 257, ground + 1, cz + 77, p.foundation());
        b.fill(cx + 256, ground - 4, cz + 113, cx + 257, ground + 1, cz + 125, p.foundation());
        b.fill(cx + 256, ground - 4, cz - 252, cx + 335, ground + 1, cz - 250, p.foundation());
        b.fill(cx + 256, ground - 4, cz + 125, cx + 335, ground + 1, cz + 127, p.foundation());
        b.fill(cx + 334, ground - 4, cz - 250, cx + 335, ground + 1, cz + 127, p.foundation());
        for (int z = -250; z <= 77; z += 6) {
            b.put(cx + 256, ground + 2, cz + z, p.weathered());
        }
        for (int z = 113; z <= 125; z += 6) {
            b.put(cx + 256, ground + 2, cz + z, p.weathered());
        }
    }

    // ------------------------------------------------------------------ harbour jetty

    private static void portDock(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 258;
        int x1 = cx + 272;
        int z0 = cz + 57;
        int z1 = cz + 63;
        b.fill(x0, ground + 1, z0, x1, ground + 1, z1, p.roofSlab());
        for (int x = x0; x <= x1; x += 4) {
            b.fill(x, ground - 2, z0, x, ground + 1, z0, p.accent());
            b.fill(x, ground - 2, z1, x, ground + 1, z1, p.accent());
        }
        for (int z = z0; z <= z1; z += 3) {
            b.put(x1, ground + 2, z, p.foundation());
            b.put(x1, ground + 3, z, p.accent());
        }
        b.put(x1 - 2, ground + 2, z0, p.accent());
        b.put(x1 - 2, ground + 3, z0, p.light());
        b.fill(cx + 256, ground + 1, cz + 59, cx + 257, ground + 1, cz + 61,
                p.foundation());
        stairs(b, p.roofStairs(), cx + 255, ground, cz + 60, Direction.EAST);
        signPostAt(b, cx + 256, cz + 56, ground + 1, p, Direction.SOUTH, "OLD PIER");
    }

    private static void questBoard(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x = cx + 220;
        int z = cz;
        b.fill(x - 8, ground + 1, z - 1, x + 8, ground + 1, z + 1, p.foundation());
        b.fill(x - 8, ground + 1, z - 1, x - 8, ground + 4, z - 1, p.accent());
        b.fill(x + 8, ground + 1, z - 1, x + 8, ground + 4, z - 1, p.accent());
        b.fill(x - 8, ground + 5, z - 1, x + 8, ground + 5, z + 1, p.roofSlab());
        b.fill(x - 7, ground + 2, z + 1, x + 7, ground + 3, z + 1, p.accent());
        sign(b, p, x - 4, ground + 4, z + 1, Direction.SOUTH, "HARBOUR NOTICES");
        sign(b, p, x, ground + 4, z + 1, Direction.SOUTH, "DROWNED LEDGER");
        sign(b, p, x + 4, ground + 4, z + 1, Direction.SOUTH, "MISSING SAILOR");
        b.put(x - 6, ground + 3, z - 1, p.light());
        b.put(x + 6, ground + 3, z - 1, p.light());
    }

    private static void harbourArch(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x = cx + 150;
        b.fill(x, ground + 1, cz - 28, x + 1, ground + 7, cz - 26, p.wall());
        b.fill(x, ground + 1, cz - 12, x + 1, ground + 7, cz - 10, p.wall());
        b.fill(x, ground + 7, cz - 28, x + 1, ground + 8, cz - 10, p.wall());
        b.fill(x, ground + 9, cz - 28, x + 1, ground + 9, cz - 10, p.roofSlab());
        sign(b, p, x - 1, ground + 8, cz - 20, Direction.WEST, "OLD HARBOUR");
        b.put(x, ground + 1, cz - 29, p.foundation());
        b.put(x, ground + 1, cz - 9, p.foundation());
    }

    private static void crane(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 1, ground, z - 1, x + 1, ground, z + 1, p.foundation());
        b.fill(x, ground + 1, z, x, ground + 10, z, p.accent());
        b.fill(x, ground + 9, z, x + 7, ground + 9, z, p.accent());
        b.fill(x + 3, ground + 9, z - 1, x + 3, ground + 10, z, p.accent());
        b.fill(x + 7, ground + 5, z, x + 7, ground + 8, z, p.rail());
        b.put(x + 7, ground + 4, z, p.foundation());
        b.put(x + 6, ground + 4, z, p.foundation());
        b.put(x, ground + 11, z, p.light());
    }

    private static void customsHouse(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 226;
        int x1 = cx + 244;
        int z0 = cz - 136;
        int z1 = cz - 108;
        b.room(x0, ground, z0, x1, ground + 12, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 12, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 12, 7, 0);
        for (int z = z0 + 3; z <= z1 - 3; z += 6) {
            b.fill(x1 + 1, ground + 1, z, x1 + 1, ground + 6, z, p.wall());
            b.put(x1 + 1, ground + 1, z, p.foundation());
        }
        b.fill(x1, ground + 7, z0 + 1, x1 + 2, ground + 7, z1 - 1, p.roofSlab());
        door(b, p, x1, ground + 1, cz - 120, Direction.EAST);
        for (int z = z0 + 5; z <= z1 - 5; z += 7) {
            b.window(x0, ground + 4, z, 3, 1, true);
            b.window(x0, ground + 9, z, 2, 1, true);
        }
        b.window(x1, ground + 9, cz - 120, 2, 2, true);
        b.fill(x0 + 1, ground + 1, z0 + 1, x0 + 1, ground + 10, z0 + 1, p.accent());
        b.fill(x0 + 2, ground + 9, z0 + 1, x0 + 5, ground + 10, z0 + 1,
                Materials.wool(DyeColor.RED));
        b.fill(x0 + 2, ground + 7, z0 + 1, x0 + 5, ground + 8, z0 + 1,
                Materials.wool(DyeColor.WHITE));
        sign(b, p, x1 + 1, ground + 6, cz - 120, Direction.EAST, "CUSTOMS");
        b.fill(x0 + 2, ground + 1, cz - 126, x0 + 8, ground + 1, cz - 124, p.accent());
        b.put(x0 + 3, ground + 2, cz - 126, p.light());
        b.put(x0 + 2, ground + 1, cz - 112, p.foundation());
        b.put(x0 + 3, ground + 1, cz - 112, p.foundation());
        b.put(x0 + 2, ground + 2, cz - 112, p.foundation());
    }

    private static void warehouse(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  int ground, Palette p, String label, int variant) {
        b.room(x0, ground, z0, x1, ground + 11, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 11, p);
        int ridge = x1 - x0 >= z1 - z0 ? 0 : 1;
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 11, 7, ridge);
        int doorX;
        int doorZ;
        Direction facing;
        if (variant % 3 == 0) {
            doorX = x1;
            doorZ = (z0 + z1) / 2;
            facing = Direction.EAST;
        } else if (variant % 3 == 1) {
            doorX = (x0 + x1) / 2;
            doorZ = z1;
            facing = Direction.SOUTH;
        } else {
            doorX = x0;
            doorZ = (z0 + z1) / 2;
            facing = Direction.WEST;
        }
        door(b, p, doorX, ground + 1, doorZ, facing);
        int sx = doorX + facing.getStepX();
        int sz = doorZ + facing.getStepZ();
        sign(b, p, sx, ground + 5, sz, facing, label, "CARGO MANIFEST");
        // Loading hatch and hoist beam on the long face.
        b.fill((x0 + x1) / 2 - 2, ground + 5, z0, (x0 + x1) / 2 + 2, ground + 8, z0,
                Materials.terracotta(DyeColor.RED));
        b.fill((x0 + x1) / 2, ground + 11, z0 - 1, (x0 + x1) / 2, ground + 13, z0 - 1,
                p.accent());
        b.put((x0 + x1) / 2, ground + 10, z0 - 1, p.rail());
        for (int x = x0 + 4; x <= x1 - 4; x += 6) {
            b.window(x, ground + 9, z0, 2, 2, true);
            b.window(x, ground + 9, z1, 2, 2, true);
        }
        b.put(x0 + 2, ground + 1, z0 + 2, p.foundation());
        b.put(x0 + 3, ground + 1, z0 + 2, p.foundation());
        b.put(x0 + 2, ground + 2, z0 + 2, p.foundation());
        b.put(x0 + 2, ground + 1, z0 + 3, p.foundation());
        b.put(x0 + 1, ground + 1, z0 + 2, p.accent());
        b.put(x0 + 1, ground + 2, z0 + 2, p.light());
    }

    // ------------------------------------------------------------------ Glodok

    private static void glodok(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        CityPlanSupport.path(b, cx - 240, cz - 235, cx - 240, cz + 195, 6, ground, p.ground());
        CityPlanSupport.path(b, cx - 318, cz - 80, cx - 142, cz - 80, 6, ground, p.ground());
        CityPlanSupport.path(b, cx - 318, cz + 100, cx - 142, cz + 100, 6, ground, p.ground());
        for (int z = -235; z <= 195; z++) {
            b.put(cx - 243, ground, cz + z, p.foundation());
            b.put(cx - 236, ground, cz + z, p.foundation());
        }
        String[] names = {"TEA", "SILK", "SPICES", "JADE", "GOLD", "INCENSE", "RICE",
                "CLOTH", "PAPER", "TIN", "RATTAN", "DYES", "MEDICINE", "SUGAR"};
        int[] modules = {-232, -216, -200, -184, -168, -152, -136, -120, -104, -88, -72,
                -56, -40, -24, -8, 8, 24, 40, 56, 72, 88, 104, 120, 136, 152, 168, 184};
        for (int i = 0; i < modules.length; i++) {
            int z0 = modules[i];
            if (blockedInGlodok(z0)) {
                continue;
            }
            shophouse(b, cx - 262, cz + z0, cx - 248, cz + z0 + 14, ground, p, 0,
                    names[i % names.length]);
            if (z0 != -72 && z0 != 120) {
                shophouse(b, cx - 234, cz + z0, cx - 220, cz + z0 + 14, ground, p, 1,
                        names[(i + 5) % names.length]);
            }
        }
        shophouse(b, cx - 234, cz - 68, cx - 220, cz - 57, ground, p, 1, "GLODOK GOODS");
        shophouse(b, cx - 234, cz + 120, cx - 220, cz + 134, ground, p, 1, "HERBAL REMEDIES");
        market(b, cx, cz, ground, p);
        temple(b, cx, cz, ground, p);
        teaHouse(b, cx, cz, ground, p);
        for (int z = -220; z <= 180; z += 30) {
            if (z >= 74 && z <= 114) {
                continue; // canal and its bridges
            }
            lanternPost(b, cx - 246, cz + z, ground, p);
            lanternPost(b, cx - 235, cz + z, ground, p);
        }
        signPost(b, cx - 246, cz + 188, ground, p, Direction.SOUTH, "GLODOK");
        signPost(b, cx - 246, cz - 88, ground, p, Direction.SOUTH, "PASAR GLODOK");
    }

    private static boolean blockedInGlodok(int z0) {
        int z1 = z0 + 14;
        return overlaps(z0, z1, -84, -76) || overlaps(z0, z1, 96, 104)
                || overlaps(z0, z1, -27, -12) || overlaps(z0, z1, 66, 78)
                || overlaps(z0, z1, 76, 122);
    }

    private static void shophouse(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  int ground, Palette p, int side, String name) {
        b.room(x0, ground, z0, x1, ground + 9, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 9, p);
        boolean laneEast = side == 0;
        int face = laneEast ? x1 : x0;
        int zc = (z0 + z1) / 2;
        door(b, p, face, ground + 1, zc, laneEast ? Direction.EAST : Direction.WEST);
        int ax0 = laneEast ? x1 + 1 : x0 - 2;
        int ax1 = laneEast ? x1 + 2 : x0 - 1;
        b.fill(ax0, ground + 4, z0 + 2, ax1, ground + 4, z1 - 2,
                Materials.terracotta(DyeColor.RED));
        b.fill(ax0, ground + 1, z0 + 2, ax0, ground + 3, z0 + 2, p.accent());
        b.fill(ax0, ground + 1, z1 - 2, ax0, ground + 3, z1 - 2, p.accent());
        b.window(face, ground + 6, z0 + 3, 2, 1, true);
        b.window(face, ground + 6, z1 - 3, 2, 1, true);
        b.window(x0, ground + 6, zc, 2, 1, true);
        b.window(x1, ground + 6, zc, 2, 1, true);
        int sx = laneEast ? x1 + 1 : x0 - 1;
        sign(b, p, sx, ground + 3, zc, laneEast ? Direction.EAST : Direction.WEST,
                name, "OPEN");
        b.put(sx, ground + 3, z0 + 2, p.light());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 1, z0 + 3, p.accent());
        b.fill(x0 + 1, ground + 5, z0 + 1, x1 - 1, ground + 5, z0 + 4, p.roofSlab());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 9, 5, 1);
        b.put(x0 + 2, ground + 8, z0 + 2, p.light());
    }

    private static void market(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 318;
        int x1 = cx - 270;
        int z0 = cz - 4;
        int z1 = cz + 60;
        pave(b, x0, z0, x1, z1, ground, p.foundation());
        outline(b, x0, z0, x1, z1, ground, p.accent());
        for (int x = x0 + 6; x <= x1 - 6; x += 12) {
            for (int z = z0 + 8; z <= z1 - 8; z += 16) {
                marketStall(b, x, z, ground, p, Materials.terracotta(DyeColor.RED));
            }
        }
        wellHead(b, cx - 294, cz + 28, ground, p);
        for (int i = 0; i < 6; i++) {
            crate(b, x0 + 4 + i * 7, z1 - 3, ground, p, 1 + (i & 1));
        }
        b.put(cx - 300, ground + 1, cz + 4, p.light());
        b.put(cx - 282, ground + 1, cz + 4, p.light());
        signPost(b, cx - 294, cz + 62, ground, p, Direction.SOUTH, "PASAR GLODOK");
    }

    private static void temple(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 316;
        int x1 = cx - 286;
        int z0 = cz - 166;
        int z1 = cz - 136;
        b.fill(x0, ground, z0, x1, ground, z1, p.ground());
        b.walls(x0, ground + 1, z0, x1, ground + 2, z1, p.foundation());
        b.fill(cx - 304, ground + 1, z1, cx - 298, ground + 2, z1,
                Blocks.AIR.defaultBlockState());
        b.fill(cx - 306, ground + 3, z1, cx - 296, ground + 3, z1, p.roofSlab());
        int hx0 = cx - 310;
        int hx1 = cx - 292;
        int hz0 = cz - 160;
        int hz1 = cz - 142;
        b.room(hx0, ground, hz0, hx1, ground + 6, hz1);
        b.walls(hx0, ground + 1, hz0, hx1, ground + 6, hz1,
                Materials.terracotta(DyeColor.RED));
        door(b, p, (hx0 + hx1) / 2, ground + 1, hz1, Direction.SOUTH);
        for (int i = 0; i < 5; i++) {
            b.fill(hx0 - 2 + i, ground + 7 + i, hz0 - 2 + i,
                    hx1 + 2 - i, ground + 7 + i, hz1 + 2 - i, p.roof());
        }
        b.put((hx0 + hx1) / 2, ground + 12, (hz0 + hz1) / 2, p.light());
        b.put(hx0, ground + 7, hz0, Materials.glazed(DyeColor.YELLOW));
        b.put(hx1, ground + 7, hz0, Materials.glazed(DyeColor.YELLOW));
        b.put(hx0, ground + 7, hz1, Materials.glazed(DyeColor.YELLOW));
        b.put(hx1, ground + 7, hz1, Materials.glazed(DyeColor.YELLOW));
        b.fill(cx - 306, ground + 1, cz - 138, cx - 304, ground + 1, cz - 138, p.light());
        b.fill(cx - 298, ground + 1, cz - 138, cx - 296, ground + 1, cz - 138, p.light());
        palm(b, cx - 318, cz - 132, ground, p);
        palm(b, cx - 284, cz - 132, ground, p);
        signPost(b, cx - 300, cz - 132, ground, p, Direction.SOUTH, "KELENTENG");
    }

    private static void teaHouse(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 316;
        int x1 = cx - 290;
        int z0 = cz + 124;
        int z1 = cz + 148;
        b.room(x0, ground, z0, x1, ground + 9, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 9, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 9, 6, 0);
        for (int z = z0 + 2; z <= z1 - 2; z += 4) {
            b.fill(x1 + 2, ground + 1, z, x1 + 2, ground + 4, z, p.accent());
        }
        b.fill(x1 + 1, ground + 5, z0 + 1, x1 + 3, ground + 5, z1 - 1, p.roofSlab());
        door(b, p, x1, ground + 1, z0 + 12, Direction.EAST);
        b.window(x1, ground + 6, z0 + 4, 2, 1, true);
        b.window(x1, ground + 6, z1 - 4, 2, 1, true);
        b.window(x0, ground + 4, z0 + 5, 3, 1, true);
        b.window(x0, ground + 4, z1 - 5, 3, 1, true);
        b.fill(x0 + 2, ground + 1, z0 + 2, x0 + 6, ground + 1, z0 + 4, p.accent());
        b.put(x0 + 3, ground + 2, z0 + 2, p.light());
        sign(b, p, x1 + 2, ground + 4, z0 + 3, Direction.EAST, "TEA HOUSE");
        b.put(x1 + 3, ground + 1, z0 + 8, p.rail());
        b.put(x1 + 4, ground + 1, z0 + 8, p.roofSlab());
        b.put(x1 + 3, ground + 1, z0 + 12, p.rail());
        b.put(x1 + 4, ground + 1, z0 + 12, p.roofSlab());
        crate(b, x0 + 1, z1 - 1, ground, p, 2);
        crate(b, x0 + 2, z1 - 1, ground, p, 1);
    }

    // ------------------------------------------------------------------ kampung

    private static void kampung(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        CityPlanSupport.path(b, cx - 318, cz + 160, cx + 318, cz + 160, 4, ground, p.rubble());
        CityPlanSupport.path(b, cx - 318, cz + 250, cx + 318, cz + 250, 4, ground, p.rubble());
        CityPlanSupport.path(b, cx - 120, cz + 124, cx - 120, cz + 305, 4, ground, p.rubble());
        CityPlanSupport.path(b, cx + 80, cz + 124, cx + 80, cz + 305, 4, ground, p.rubble());
        for (int x = -306; x <= 306; x += 18) {
            kampungHouse(b, cx + x, cz + 144, ground, p, false, true);
            kampungHouse(b, cx + x, cz + 166, ground, p, false, false);
            kampungHouse(b, cx + x, cz + 234, ground, p, false, true);
            kampungHouse(b, cx + x, cz + 256, ground, p, false, false);
        }
        kampungHouse(b, cx - 144, cz + 190, ground, p, true, true);
        pond(b, cx + 96, cz + 270, cx + 200, cz + 305, ground, p);
        pond(b, cx - 270, cz + 270, cx - 170, cz + 305, ground, p);
        stiltHouse(b, cx + 110, cz + 274, ground, p);
        stiltHouse(b, cx + 146, cz + 274, ground, p);
        stiltHouse(b, cx + 182, cz + 274, ground, p);
        stiltHouse(b, cx + 128, cz + 292, ground, p);
        stiltHouse(b, cx + 164, cz + 292, ground, p);
        stiltHouse(b, cx - 258, cz + 274, ground, p);
        stiltHouse(b, cx - 224, cz + 274, ground, p);
        stiltHouse(b, cx - 190, cz + 274, ground, p);
        ruin(b, cx - 176, cz + 184, cx - 160, cz + 200, ground, p);
        mosque(b, cx, cz, ground, p);
        kampungWell(b, cx, cz, ground, p);
        grove(b, cx, cz, ground, p, -338, 130, -318, 154, 5);
        grove(b, cx, cz, ground, p, 318, 130, 336, 154, 4);
        for (int z = 126; z <= 300; z += 42) {
            lanternPost(b, cx - 10, cz + z, ground, p);
        }
        signPost(b, cx + 10, cz + 126, ground, p, Direction.SOUTH, "KAMPUNG");
        signPost(b, cx - 130, cz + 154, ground, p, Direction.SOUTH, "KAMPUNG LANES");
        signPost(b, cx + 92, cz + 154, ground, p, Direction.SOUTH, "MASJID");
    }

    private static boolean reserved(int x0, int z0, int x1, int z1) {
        return overlaps(x0, x1, -14, 14)
                || overlaps(x0, x1, -126, -114)
                || overlaps(x0, x1, 74, 86)
                || overlaps(z0, z1, 156, 164)
                || overlaps(z0, z1, 246, 254)
                || overlaps(x0, x1, 94, 146) && overlaps(z0, z1, 194, 246)
                || overlaps(x0, x1, -212, -188) && overlaps(z0, z1, 208, 232)
                || overlaps(x0, x1, 90, 206) && overlaps(z0, z1, 264, 310)
                || overlaps(x0, x1, -276, -164) && overlaps(z0, z1, 264, 310)
                || overlaps(x0, x1, -182, -152) && overlaps(z0, z1, 178, 206)
                || overlaps(x0, x1, 144, 172) && overlaps(z0, z1, 246, 270)
                || overlaps(x0, x1, -318, -288) && overlaps(z0, z1, 122, 150);
    }

    private static void kampungHouse(StructureBuilder b, int x0, int z0, int ground,
                                     Palette p, boolean flooded, boolean south) {
        int x1 = x0 + 8;
        int z1 = z0 + 8;
        if (reserved(x0 - 1, z0 - 1, x1 + 1, z1 + 2)) {
            return;
        }
        // Low stilts and a timber floor one course above the lane.
        for (int x = x0; x <= x1; x += 4) {
            b.put(x, ground, z0, p.accent());
            b.put(x, ground, z1, p.accent());
        }
        b.put(x0, ground, z0 + 4, p.accent());
        b.put(x1, ground, z0 + 4, p.accent());
        if (flooded) {
            b.fill(x0, ground + 1, z0, x1, ground + 1, z1,
                    Blocks.WATER.defaultBlockState());
        } else {
            b.fill(x0, ground + 1, z0, x1, ground + 1, z1, p.roofSlab());
        }
        b.walls(x0, ground + 2, z0, x1, ground + 4, z1, p.accent());
        b.fill(x0 + 1, ground + 2, z0 + 1, x1 - 1, ground + 4, z1 - 1,
                Blocks.AIR.defaultBlockState());
        if (flooded) {
            b.fill(x0 + 1, ground + 2, z0 + 1, x1 - 1, ground + 3, z1 - 1,
                    Blocks.WATER.defaultBlockState());
        }
        int doorX = x0 + 4;
        if (south) {
            door(b, p, doorX, ground + 2, z1, Direction.SOUTH);
        } else {
            door(b, p, doorX, ground + 2, z0, Direction.NORTH);
        }
        b.window(x0, ground + 3, z0 + 4, 1, 1, true);
        b.window(x1, ground + 3, z0 + 4, 1, 1, true);
        int roof = flooded ? ground + 4 : ground + 5;
        BlockState thatch = Math.floorMod(x0 + z0, 3) == 0
                ? Blocks.HAY_BLOCK.defaultBlockState() : p.roofSlab();
        for (int i = 0; i < 4; i++) {
            b.fill(x0 - 1 + i, roof + i, z0 - 1, x1 + 1 - i, roof + i, z1 + 1, thatch);
        }
        // Veranda and the stair up to it, on the lane side of the house.
        int verandaNear = south ? z1 + 1 : z0 - 2;
        int verandaFar = south ? z1 + 2 : z0 - 1;
        b.fill(x0 + 2, ground + 1, verandaNear, x1 - 2, ground + 1, verandaFar, p.roofSlab());
        b.put(x0 + 2, ground + 1, verandaFar, p.accent());
        b.put(x1 - 2, ground + 1, verandaFar, p.accent());
        int stairZ = south ? z1 + 3 : z0 - 3;
        stairs(b, p.roofStairs(), doorX, ground, stairZ,
                south ? Direction.SOUTH : Direction.NORTH);
        b.put(x0 + 2, ground + 2, verandaFar, p.light());
        if (!flooded && (hash(x0, z0) & 3) == 0) {
            b.put(x0 + 1, ground + 1, z0 + 1, p.light());
        }
        if (flooded) {
            b.fill(x0 + 1, ground + 5, z0 + 1, x1 - 1, ground + 5, z0 + 3,
                    Blocks.AIR.defaultBlockState());
        }
    }

    private static void pond(StructureBuilder b, int x0, int z0, int x1, int z1,
                             int ground, Palette p) {
        b.fill(x0, ground - 2, z0, x1, ground - 2, z1, Blocks.SAND.defaultBlockState());
        b.fill(x0, ground - 1, z0, x1, ground - 1, z1, Blocks.WATER.defaultBlockState());
        outline(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p.foundation());
        for (int x = x0 + 3; x <= x1 - 3; x += 9) {
            b.put(x, ground, z0, Blocks.SUGAR_CANE.defaultBlockState());
            b.put(x, ground + 1, z0, Blocks.SUGAR_CANE.defaultBlockState());
        }
    }

    private static void stiltHouse(StructureBuilder b, int x0, int z0, int ground, Palette p) {
        int x1 = x0 + 6;
        int z1 = z0 + 6;
        for (int x = x0; x <= x1; x += 3) {
            b.fill(x, ground - 1, z0, x, ground + 1, z0, p.accent());
            b.fill(x, ground - 1, z1, x, ground + 1, z1, p.accent());
        }
        b.fill(x0, ground + 2, z0, x1, ground + 2, z1, p.roofSlab());
        b.walls(x0, ground + 3, z0, x1, ground + 5, z1, p.accent());
        b.fill(x0 + 1, ground + 3, z0 + 1, x1 - 1, ground + 5, z1 - 1,
                Blocks.AIR.defaultBlockState());
        door(b, p, x0 + 3, ground + 3, z0, Direction.NORTH);
        b.window(x0, ground + 4, z0 + 3, 1, 1, true);
        b.window(x1, ground + 4, z0 + 3, 1, 1, true);
        for (int i = 0; i < 3; i++) {
            b.fill(x0 - 1 + i, ground + 6 + i, z0 - 1, x1 + 1 - i, ground + 6 + i, z1 + 1,
                    Blocks.HAY_BLOCK.defaultBlockState());
        }
        // Plank walk from the shore, with a lantern at the head.
        b.fill(x0 + 3, ground + 2, z0 - 3, x0 + 3, ground + 2, z0 - 1, p.roofSlab());
        b.put(x0 + 3, ground + 2, z0 - 4, p.roofSlab());
        stairs(b, p.roofStairs(), x0 + 3, ground + 1, z0 - 4, Direction.SOUTH);
        stairs(b, p.roofStairs(), x0 + 3, ground, z0 - 5, Direction.SOUTH);
        b.put(x0 + 4, ground + 3, z0 - 1, p.accent());
        b.put(x0 + 4, ground + 4, z0 - 1, p.light());
    }

    private static void mosque(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 98;
        int x1 = cx + 142;
        int z0 = cz + 198;
        int z1 = cz + 242;
        b.fill(x0, ground, z0, x1, ground, z1, p.ground());
        b.walls(x0, ground + 1, z0, x1, ground + 2, z1, p.weathered());
        b.fill(cx + 118, ground + 1, z0, cx + 122, ground + 2, z0,
                Blocks.AIR.defaultBlockState());
        b.fill(cx + 116, ground + 3, z0, cx + 124, ground + 3, z0, p.roofSlab());
        int hx0 = cx + 104;
        int hx1 = cx + 136;
        int hz0 = cz + 204;
        int hz1 = cz + 236;
        b.room(hx0, ground, hz0, hx1, ground + 6, hz1);
        b.walls(hx0, ground + 1, hz0, hx1, ground + 6, hz1, Materials.whiteConcrete());
        door(b, p, cx + 120, ground + 1, hz0, Direction.NORTH);
        for (int i = 0; i < 6; i++) {
            int inset = i * 2;
            b.fill(hx0 - 2 + inset, ground + 7 + i, hz0 - 2 + inset,
                    hx1 + 2 - inset, ground + 7 + i, hz1 + 2 - inset, p.roof());
        }
        b.put(cx + 120, ground + 13, cz + 220, p.light());
        for (int z = hz0 + 5; z <= hz1 - 5; z += 7) {
            b.window(hx0, ground + 3, z, 2, 1, true);
            b.window(hx1, ground + 3, z, 2, 1, true);
        }
        // Minaret in the east corner of the courtyard.
        b.room(cx + 136, ground, cz + 228, cx + 140, ground + 18, cz + 232);
        b.fill(cx + 136, ground, cz + 228, cx + 140, ground, cz + 232, p.foundation());
        for (int y = ground + 5; y <= ground + 16; y += 4) {
            b.window(cx + 136, y, cz + 230, 2, 1, true);
            b.window(cx + 140, y, cz + 230, 2, 1, true);
        }
        b.fill(cx + 135, ground + 19, cz + 227, cx + 141, ground + 19, cz + 233, p.roof());
        b.fill(cx + 137, ground + 20, cz + 229, cx + 139, ground + 20, cz + 231, p.roof());
        b.put(cx + 138, ground + 21, cz + 230, p.light());
        // Ablution pool against the west courtyard wall.
        b.fill(cx + 99, ground + 1, cz + 212, cx + 103, ground + 1, cz + 216, p.foundation());
        b.fill(cx + 100, ground + 1, cz + 213, cx + 102, ground + 1, cz + 215,
                Blocks.WATER.defaultBlockState());
        palm(b, cx + 100, cz + 202, ground, p);
        palm(b, cx + 140, cz + 202, ground, p);
        signPost(b, cx + 120, cz + 196, ground, p, Direction.SOUTH, "MASJID");
    }

    private static void kampungWell(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x = cx - 200;
        int z = cz + 220;
        pave(b, x - 7, z - 7, x + 7, z + 7, ground, p.ground());
        outline(b, x - 7, z - 7, x + 7, z + 7, ground, p.foundation());
        wellHead(b, x - 2, z, ground, p);
        b.put(x + 3, ground + 1, z - 3, p.accent());
        b.put(x + 3, ground + 2, z - 3, p.light());
        b.put(x + 3, ground + 1, z + 3, p.rail());
        b.put(x + 4, ground + 1, z + 3, p.roofSlab());
        signPost(b, x, z - 7, ground, p, Direction.SOUTH, "KAMPUNG WELL");
        b.put(x - 5, ground + 1, z + 5, p.light());
    }

    // ------------------------------------------------------------------ canal

    private static void canal(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        b.fill(cx - 334, ground - 4, cz + 82, cx + 334, ground - 4, cz + 108,
                Blocks.SAND.defaultBlockState());
        b.fill(cx - 334, ground - 3, cz + 82, cx + 334, ground - 2, cz + 108,
                Blocks.WATER.defaultBlockState());
        b.fill(cx - 334, ground - 3, cz + 78, cx + 256, ground + 1, cz + 81,
                p.foundation());
        b.fill(cx - 334, ground - 3, cz + 109, cx + 256, ground + 1, cz + 111,
                p.foundation());
        CityPlanSupport.path(b, cx - 334, cz + 72, cx + 245, cz + 72, 10, ground, p.ground());
        CityPlanSupport.path(b, cx - 334, cz + 115, cx + 245, cz + 115, 8, ground, p.ground());
        bridge(b, cx, cz + 95, ground, p, 20, p.ground(), p.foundation());
        bridge(b, cx - 240, cz + 95, ground, p, 16, p.accent(), p.accent());
        culvert(b, cx + 250, cz + 95, ground, p);
        barge(b, cx - 70, cz + 94, ground, p);
        barge(b, cx + 40, cz + 92, ground, p);
        barge(b, cx + 200, cz + 96, ground, p);
        b.fill(cx - 334, ground - 3, cz + 78, cx - 330, ground + 5, cz + 84,
                p.foundation());
        b.fill(cx - 334, ground - 3, cz + 106, cx - 330, ground + 5, cz + 112,
                p.foundation());
        b.fill(cx - 334, ground + 5, cz + 78, cx - 330, ground + 5, cz + 112,
                p.roofSlab());
        for (int z = 84; z <= 106; z += 4) {
            b.put(cx - 332, ground + 1, cz + z, p.rail());
        }
        for (int x = -300; x <= 240; x += 40) {
            b.put(cx + x, ground + 1, cz + 80, p.rail());
            b.put(cx + x, ground + 1, cz + 110, p.rail());
        }
        signPost(b, cx + 20, cz + 79, ground, p, Direction.NORTH, "CANAL LOCKS");
        signPost(b, cx - 130, cz + 79, ground, p, Direction.NORTH, "MIND THE BARGES");
        signPost(b, cx + 30, cz + 111, ground, p, Direction.SOUTH, "DEEP WATER");
    }

    private static void bridge(StructureBuilder b, int x, int z, int ground, Palette p,
                               int width, BlockState deck, BlockState edge) {
        int half = width / 2;
        int x0 = x - half;
        int x1 = x + half - 1;
        b.fill(x0, ground + 1, z - 19, x1, ground + 3, z + 19,
                Blocks.AIR.defaultBlockState());
        b.fill(x0, ground, z - 19, x1, ground, z + 19, deck);
        b.fill(x0, ground, z - 19, x1, ground, z - 17, edge);
        b.fill(x0, ground, z + 17, x1, ground, z + 19, edge);
        b.fill(x0, ground + 1, z - 15, x0, ground + 1, z + 15, p.rail());
        b.fill(x1, ground + 1, z - 15, x1, ground + 1, z + 15, p.rail());
        b.fill(x0, ground - 3, z - 4, x0 + 1, ground - 1, z + 4, p.foundation());
        b.fill(x1 - 1, ground - 3, z - 4, x1, ground - 1, z + 4, p.foundation());
        lanternPost(b, x0, z - 16, ground, p);
        lanternPost(b, x1, z - 16, ground, p);
        lanternPost(b, x0, z + 16, ground, p);
        lanternPost(b, x1, z + 16, ground, p);
    }

    private static void culvert(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 6, ground + 1, z - 19, x + 6, ground + 2, z + 19,
                Blocks.AIR.defaultBlockState());
        b.fill(x - 6, ground, z - 19, x + 6, ground, z + 19, p.ground());
        b.fill(x - 6, ground - 3, z - 19, x - 5, ground - 1, z + 19, p.foundation());
        b.fill(x + 5, ground - 3, z - 19, x + 6, ground - 1, z + 19, p.foundation());
        b.fill(x - 6, ground + 1, z - 19, x + 6, ground + 1, z - 17, p.foundation());
        b.fill(x - 6, ground + 1, z + 17, x + 6, ground + 1, z + 19, p.foundation());
        b.put(x - 5, ground + 2, z - 18, p.light());
        b.put(x + 5, ground + 2, z + 18, p.light());
    }

    private static void barge(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 6, ground - 2, z - 2, x + 6, ground - 2, z + 2, p.accent());
        b.fill(x - 5, ground - 1, z - 1, x + 5, ground - 1, z + 1, p.roofSlab());
        b.fill(x - 6, ground - 1, z - 2, x - 6, ground - 1, z + 2, p.accent());
        b.fill(x + 6, ground - 1, z - 2, x + 6, ground - 1, z + 2, p.accent());
        b.fill(x, ground, z, x, ground + 5, z, p.accent());
        b.put(x + 3, ground, z, p.foundation());
        b.put(x - 3, ground, z, p.foundation());
        b.put(x, ground + 6, z, p.light());
        b.fill(x - 2, ground + 3, z, x - 2, ground + 4, z, Materials.wool(DyeColor.RED));
    }

    // ------------------------------------------------------------------ secret cellar

    private static void secretCellar(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 214;
        int x1 = cx + 242;
        int z0 = cz + 180;
        int z1 = cz + 210;
        BlockState air = Blocks.AIR.defaultBlockState();
        b.fill(x0, ground - 4, z0, x1, ground - 1, z1, air);
        b.fill(x0, ground - 4, z0, x1, ground - 4, z1, p.foundation());
        b.walls(x0, ground - 3, z0, x1, ground - 2, z1, p.foundation());
        b.fill(x0, ground - 1, z0, x1, ground - 1, z1, p.foundation());
        // Ladder shaft: a brick pier inside the cellar carries the ladder up to the floor.
        b.fill(cx + 222, ground - 4, cz + 185, cx + 222, ground - 1, cz + 185,
                p.foundation());
        b.fill(cx + 222, ground - 3, cz + 184, cx + 222, ground, cz + 184, air);
        for (int y = ground - 3; y <= ground; y++) {
            b.put(cx + 222, y, cz + 184,
                    Blocks.LADDER.defaultBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        }
        b.put(cx + 222, ground, cz + 185, p.accent());
        // Smuggled cargo.
        crate(b, cx + 216, cz + 196, ground - 4, p, 1);
        crate(b, cx + 217, cz + 196, ground - 4, p, 2);
        crate(b, cx + 216, cz + 197, ground - 4, p, 1);
        crate(b, cx + 239, cz + 206, ground - 4, p, 2);
        crate(b, cx + 238, cz + 206, ground - 4, p, 1);
        b.put(cx + 217, ground - 3, cz + 196, p.foundation());
        // The shrine under the warehouse: a low altar and a sailor's charm.
        b.fill(cx + 224, ground - 3, cz + 181, cx + 232, ground - 3, cz + 182,
                p.foundation());
        b.put(cx + 226, ground - 3, cz + 182, p.light());
        b.put(cx + 230, ground - 3, cz + 182, p.light());
        b.fill(cx + 227, ground - 3, cz + 181, cx + 229, ground - 3, cz + 181,
                Materials.wool(DyeColor.RED));
        b.put(cx + 228, ground - 2, cz + 181, p.accent());
        b.put(cx + 228, ground - 2, cz + 182, p.light());
        b.chest(cx + 218, ground - 3, cz + 206, SiteLoot.RELIC);
        b.chest(cx + 238, ground - 3, cz + 184, SiteLoot.COMMON);
        sign(b, p, cx + 222, ground - 3, cz + 190, Direction.SOUTH, "DROWNED LEDGER");
        sign(b, p, cx + 228, ground - 3, cz + 183, Direction.SOUTH, "SHRINE");
        b.put(cx + 234, ground - 3, cz + 204, p.light());
        b.put(cx + 220, ground - 2, cz + 202, p.rail());
    }

    // ------------------------------------------------------------------ small ruins

    private static void ruin(StructureBuilder b, int x0, int z0, int x1, int z1,
                             int ground, Palette p) {
        for (int y = ground + 1; y <= ground + 3; y++) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                    if (!edge || (hash(x, z) + y) % 3 == 0) {
                        continue;
                    }
                    b.put(x, y, z, y == ground + 1 ? p.foundation() : p.weathered());
                }
            }
        }
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                if ((hash(x, z) & 3) == 0) {
                    b.put(x, ground, z, p.rubble());
                }
            }
        }
        b.fill(x0, ground + 3, (z0 + z1) / 2, x1, ground + 3, (z0 + z1) / 2, p.accent());
        b.put(x0 + 2, ground + 1, z0 + 2, p.light());
    }
}
