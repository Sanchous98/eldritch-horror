package com.sanchous98.eldritchhorror.world.loc.city.plan;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.SiteLoot;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import com.sanchous98.eldritchhorror.world.loc.style.Materials;
import java.util.List;
import java.util.function.IntBinaryOperator;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.RailShape;

/**
 * Authored Prohibition-era New York: the Empire State's stepped tower over a limestone lobby, the
 * Manhattan grid of offices, theatres and hotels, the Hudson docks with piers, warehouses, cranes
 * and a goods railway, Harlem's brownstones, jazz clubs, chapel and market, the downtown banks and
 * city hall, the Central Park edge, and the subway with its hidden junction.
 *
 * <p>Everything is written through {@link StructureBuilder}; there is no RNG, no chunk-local read
 * and no shared prefab. Coordinates are relative to the city centre {@code (cx, ground, cz)} and
 * stay inside {@code [-340, 340]}.
 */
public final class NewYorkPlan implements CityPlan {

    /** Authored half-extent (blocks). */
    private static final int HALF = 340;

    @Override
    public String cityId() {
        return "new_york";
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
        streets(b, cx, cz, ground, p);
        arrivalGate(b, cx, cz, ground, p, surface);
        centralPark(b, cx, cz, p, surface);
        midtown(b, cx, cz, ground, p);
        empireState(b, cx, cz, ground, p);
        downtown(b, cx, cz, ground, p);
        harlem(b, cx, cz, ground, p, surface);
        docks(b, cx, cz, ground, p, surface);
        subway(b, cx, cz, ground, p);
        dressing(b, cx, cz, ground, p);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.LANDMARK, 0, 1, 320, "south_gate"),
                new CityPoi("arrival_sign", CityPoi.Kind.SIGN, 0, 4, 322, "NEW YORK"),
                new CityPoi("midtown_quest_board", CityPoi.Kind.QUEST_BOARD, 0, 1, -120,
                        "new_york_missing_reporter,new_york_bootlegger"),
                new CityPoi("city_notices", CityPoi.Kind.SIGN, -15, 1, -116, "CITY NOTICES"),
                new CityPoi("hotel", CityPoi.Kind.INN, -80, 1, -60, "hotel_clerk"),
                new CityPoi("hotel_clerk", CityPoi.Kind.NPC, -80, 2, -64, "hotel_clerk"),
                new CityPoi("midtown_shop", CityPoi.Kind.SHOP, 40, 1, -60, "shopkeeper"),
                new CityPoi("shopkeeper", CityPoi.Kind.NPC, 40, 2, -64, "shopkeeper"),
                new CityPoi("empire_state", CityPoi.Kind.LANDMARK, 0, 1, -200, "tower"),
                new CityPoi("newsroom", CityPoi.Kind.NPC, -240, 1, -120,
                        "editor|new_york_missing_reporter"),
                new CityPoi("editor", CityPoi.Kind.NPC, -252, 2, -134,
                        "editor|new_york_missing_reporter"),
                new CityPoi("jazz_singer", CityPoi.Kind.NPC, -296, 2, 64, "jazz_singer"),
                new CityPoi("harlem_market", CityPoi.Kind.LANDMARK, -210, 1, 98, "market"),
                new CityPoi("apothecary", CityPoi.Kind.HEALER, -240, 1, 120, "apothecary"),
                new CityPoi("apothecary_keeper", CityPoi.Kind.NPC, -248, 2, 140, "apothecary"),
                new CityPoi("downtown_bank", CityPoi.Kind.LANDMARK, -68, 1, 0, "bank"),
                new CityPoi("city_hall", CityPoi.Kind.LANDMARK, -64, 1, 80, "city_hall"),
                new CityPoi("cop", CityPoi.Kind.NPC, 40, 2, 160, "cop"),
                new CityPoi("subway_station", CityPoi.Kind.LANDMARK, 20, 1, 88, "ticket_hall"),
                new CityPoi("subway_junction", CityPoi.Kind.SECRET, 0, -8, 120,
                        "new_york_missing_reporter"),
                new CityPoi("subway_warning", CityPoi.Kind.SIGN, 0, -5, 110, "MIND THE GAP"),
                new CityPoi("docks", CityPoi.Kind.LANDMARK, 240, 1, -120, "piers"),
                new CityPoi("harbour", CityPoi.Kind.PORT, 300, 0, 0, "pier_basin"),
                new CityPoi("rail_station", CityPoi.Kind.STATION, 172, 2, -236, "goods_yard"),
                new CityPoi("dock_warning", CityPoi.Kind.SIGN, 240, 2, -124, "NO ENTRY"),
                new CityPoi("speakeasy", CityPoi.Kind.NPC, 240, 1, 40,
                        "bootlegger|new_york_bootlegger"),
                new CityPoi("bootlegger", CityPoi.Kind.NPC, 224, -3, 40,
                        "bootlegger|new_york_bootlegger"),
                new CityPoi("central_park", CityPoi.Kind.LANDMARK, 0, 1, -294, "park_edge"),
                new CityPoi("central_park_sign", CityPoi.Kind.SIGN, 0, 3, -292, "CENTRAL PARK")
        );
    }

    // ------------------------------------------------------------------ streets

    private static void streets(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Avenues, fourteen wide.
        pave(b, cx - 127, cz - 260, cx - 113, cz + 260, ground, p.foundation());
        pave(b, cx - 7, cz - 120, cx + 7, cz + 320, ground, p.foundation());
        pave(b, cx + 113, cz - 260, cx + 127, cz + 260, ground, p.foundation());
        // Cross streets, eight wide.
        pave(b, cx - 180, cz - 284, cx + 180, cz - 276, ground, p.foundation());
        pave(b, cx - 180, cz - 204, cx - 60, cz - 196, ground, p.foundation());
        pave(b, cx + 60, cz - 204, cx + 180, cz - 196, ground, p.foundation());
        pave(b, cx - 280, cz - 124, cx + 280, cz - 116, ground, p.foundation());
        pave(b, cx - 320, cz - 44, cx + 320, cz - 36, ground, p.foundation());
        pave(b, cx - 320, cz + 36, cx + 320, cz + 44, ground, p.foundation());
        pave(b, cx - 300, cz + 116, cx + 300, cz + 124, ground, p.foundation());
        pave(b, cx - 240, cz + 196, cx + 240, cz + 204, ground, p.foundation());
        pave(b, cx - 180, cz + 276, cx + 180, cz + 284, ground, p.foundation());
        // Harlem's six-wide lanes.
        pave(b, cx - 283, cz - 180, cx - 277, cz + 80, ground, p.ground());
        pave(b, cx - 243, cz - 110, cx - 237, cz + 116, ground, p.ground());
        pave(b, cx - 163, cz - 190, cx - 157, cz + 140, ground, p.ground());
        pave(b, cx - 290, cz - 163, cx - 150, cz - 157, ground, p.ground());
        // The twelve-wide dock road and its link to the city grid.
        pave(b, cx + 235, cz - 200, cx + 246, cz + 140, ground, p.rubble());
        pave(b, cx + 180, cz - 204, cx + 246, cz - 196, ground, p.rubble());
        // Centre lines on the avenues.
        for (int z = -250; z <= 250; z += 12) {
            b.put(cx - 120, ground, cz + z, p.accent());
            b.put(cx + 120, ground, cz + z, p.accent());
        }
        for (int z = -110; z <= 300; z += 12) {
            b.put(cx, ground, cz + z, p.accent());
        }
    }

    private static void pave(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                             BlockState state) {
        b.fill(x0, y, z0, x1, y, z1, state);
    }

    // ------------------------------------------------------------------ arrival

    private static void arrivalGate(StructureBuilder b, int cx, int cz, int ground, Palette p,
                                    IntBinaryOperator surface) {
        int x = cx;
        int z = cz + 320;
        int gy = Math.max(ground, surface.applyAsInt(x, z));
        // A stepped apron carries the avenue up to the gate's authored level.
        for (int i = 0; i < gy - ground; i++) {
            b.fill(x - 18, ground + i, z - 16, x + 18, ground + i, z - 11,
                    p.foundation());
        }
        b.fill(x - 26, gy, z - 10, x + 26, gy, z + 12, p.foundation());
        b.fill(x - 26, gy - 1, z - 10, x + 26, gy - 1, z + 12, p.foundation());

        for (int side = -1; side <= 1; side += 2) {
            int px = x + side * 18;
            b.fill(px - 4, gy + 1, z - 6, px + 4, gy + 12, z, p.wall());
            b.fill(px - 5, gy + 12, z - 7, px + 5, gy + 13, z + 1, p.roof());
            b.crenellations(px - 5, z - 7, px + 5, z + 1, gy + 14);
            for (int y = gy + 2; y <= gy + 11; y += 3) {
                b.put(px - 4, y, z + 1, p.accent());
                b.put(px + 4, y, z + 1, p.accent());
            }
            b.put(px, gy + 10, z + 1, p.light());
        }
        b.fill(x - 14, gy + 10, z - 4, x + 14, gy + 10, z - 3, p.rail());
        b.fill(x - 12, gy + 12, z - 4, x + 12, gy + 12, z - 3, p.roofSlab());
        for (int px = -12; px <= 12; px += 4) {
            b.put(x + px, gy + 11, z - 3, p.rail());
        }
        signAt(b, x, gy + 11, z - 3, Direction.SOUTH, "NEW YORK", "SOUTH GATE");
        signAt(b, x - 18, gy + 6, z, Direction.SOUTH, "NEW YORK CITY");

        // Signposts to every district.
        String[] posts = {"MIDTOWN ^", "DOCKS ->", "HARLEM <-", "DOWNTOWN ^",
                "CENTRAL PARK ^"};
        for (int i = 0; i < posts.length; i++) {
            int px = x - 24 + i * 12;
            b.put(px, gy, z + 6, p.foundation());
            b.put(px, gy + 1, z + 6, p.accent());
            b.put(px, gy + 2, z + 6, p.accent());
            signAt(b, px, gy + 3, z + 6, Direction.SOUTH, posts[i]);
        }
        b.put(x - 13, gy + 1, z + 4, p.accent());
        b.put(x - 13, gy + 2, z + 4, p.light());
        b.put(x + 13, gy + 1, z + 4, p.accent());
        b.put(x + 13, gy + 2, z + 4, p.light());
    }

    // ------------------------------------------------------------------ Central Park

    private static void centralPark(StructureBuilder b, int cx, int cz, Palette p,
                                    IntBinaryOperator surface) {
        // The park path along the northern edge.
        for (int rx = -140; rx <= 140; rx++) {
            int y = surface.applyAsInt(cx + rx, cz - 294);
            for (int dz = -2; dz <= 2; dz++) {
                b.put(cx + rx, y, cz - 294 + dz, p.ground());
            }
        }
        // A stone gate and its sign.
        int gy = surface.applyAsInt(cx, cz - 291);
        b.fill(cx - 7, gy + 1, cz - 293, cx - 4, gy + 4, cz - 289, p.foundation());
        b.fill(cx + 4, gy + 1, cz - 293, cx + 7, gy + 4, cz - 289, p.foundation());
        b.fill(cx - 8, gy + 5, cz - 294, cx + 8, gy + 5, cz - 288, p.roofSlab());
        signAt(b, cx, gy + 6, cz - 289, Direction.SOUTH, "CENTRAL PARK");

        // The lake: a shallow authored basin with a stone lip.
        for (int rx = -46; rx <= 38; rx++) {
            for (int rz = -316; rz <= -300; rz++) {
                int y = surface.applyAsInt(cx + rx, cz + rz);
                boolean edge = rx == -46 || rx == 38 || rz == -316 || rz == -300;
                if (edge) {
                    b.put(cx + rx, y, cz + rz, p.foundation());
                } else {
                    b.put(cx + rx, y, cz + rz, air());
                    b.put(cx + rx, y - 1, cz + rz, Blocks.WATER.defaultBlockState());
                    b.put(cx + rx, y - 2, cz + rz, Blocks.WATER.defaultBlockState());
                }
            }
        }

        // The tree line, following the curve of the district.
        int[][] trees = {
                {-150, -290}, {-120, -300}, {-90, -306}, {-58, -314}, {-30, -318},
                {0, -318}, {30, -318}, {56, -314}, {90, -306}, {120, -300}, {150, -290},
                {-132, -262}, {-96, -270}, {-48, -272}, {48, -272}, {96, -270},
                {132, -262}, {-164, -250}, {164, -250}
        };
        for (int[] t : trees) {
            int y = surface.applyAsInt(cx + t[0], cz + t[1]);
            tree(b, cx + t[0], y, cz + t[1], p);
        }
        for (int rx : new int[]{-120, -60, 60, 120}) {
            int y = surface.applyAsInt(cx + rx, cz - 292);
            bench(b, cx + rx, y, cz - 292, p);
        }
        b.monument(cx + 14, cz - 290, surface.applyAsInt(cx + 14, cz - 290) + 1);
    }

    // ------------------------------------------------------------------ Midtown

    private static void midtown(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        office(b, cx - 108, cz - 252, 44, 40, 22, ground, p, "MIDTOWN OFFICE");
        office(b, cx + 64, cz - 252, 44, 40, 26, ground, p, "OFFICE TOWER");
        theatre(b, cx - 108, cz - 186, 44, 50, 16, ground, p, "BROADWAY");
        office(b, cx + 64, cz - 186, 44, 50, 20, ground, p, "DAILY NEWS");
        hotel(b, cx - 100, cz - 110, 40, 58, 18, ground, p, "HOTEL ASTOR");
        shop(b, cx + 20, cz - 108, 40, 52, 9, ground, p, "MIDTOWN SHOP");
        shop(b, cx + 68, cz - 108, 36, 52, 10, ground, p, "BOOKSHOP");
        questBoard(b, cx + 12, cz - 114, ground, p);
        noticeBoard(b, cx - 15, cz - 114, ground, p);
        signAt(b, cx - 60, ground + 1, cz - 50, Direction.SOUTH, "42ND STREET");
        signAt(b, cx + 40, ground + 1, cz - 50, Direction.SOUTH, "MIDTOWN");
    }

    private static void questBoard(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 4, ground, z - 1, x + 4, ground, z + 1, p.foundation());
        b.put(x - 3, ground + 1, z, p.accent());
        b.put(x + 3, ground + 1, z, p.accent());
        b.fill(x - 3, ground + 2, z, x + 3, ground + 3, z, Blocks.DARK_OAK_PLANKS
                .defaultBlockState());
        signAt(b, x, ground + 3, z, Direction.SOUTH, "REPORTER LOST", "BOOTLEGGER",
                "REWARD 200");
        b.put(x - 3, ground + 2, z, p.light());
    }

    private static void noticeBoard(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 3, ground, z - 1, x + 3, ground, z + 1, p.foundation());
        b.put(x - 2, ground + 1, z, p.accent());
        b.put(x + 2, ground + 1, z, p.accent());
        b.fill(x - 2, ground + 2, z, x + 2, ground + 3, z, Blocks.DARK_OAK_PLANKS
                .defaultBlockState());
        signAt(b, x, ground + 3, z, Direction.SOUTH, "CITY NOTICES", "SUBWAY WORK");
    }

    // ------------------------------------------------------------------ Empire State

    private static void empireState(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x = cx;
        int z = cz - 200;
        b.fill(x - 48, ground, z - 58, x + 48, ground, z + 60, p.ground());
        b.fill(x - 44, ground + 1, z - 50, x + 44, ground + 1, z + 52, p.foundation());
        b.fill(x - 20, ground + 1, z + 52, x + 20, ground + 1, z + 58, p.foundation());
        b.fill(x - 12, ground + 2, z + 52, x + 12, ground + 2, z + 58, p.foundation());
        b.fill(x - 12, ground + 2, z + 49, x + 12, ground + 2, z + 51, p.foundation());

        int by = ground + 2;
        int top = ground + 16;
        b.room(x - 44, by, z - 48, x + 44, top, z + 48);
        b.walls(x - 44, by + 1, z - 48, x + 44, top - 1, z + 48, p.roof());
        b.walls(x - 44, by + 1, z - 48, x + 44, by + 2, z + 48, p.foundation());
        b.walls(x - 44, top - 1, z - 48, x + 44, top - 1, z + 48, p.accent());
        for (int wx = x - 38; wx <= x + 38; wx += 8) {
            b.window(wx, by + 5, z + 48, 5, 3, true);
            b.window(wx, by + 5, z - 48, 5, 3, true);
        }
        for (int wz = z - 40; wz <= z + 40; wz += 8) {
            b.window(x - 44, by + 5, wz, 5, 1, true);
            b.window(x + 44, by + 5, wz, 5, 1, true);
        }
        b.fill(x - 8, by + 1, z + 48, x + 8, by + 4, z + 48, air());
        b.fill(x - 8, by + 1, z + 46, x + 8, by + 4, z + 46, p.roofSlab());
        placeDoor(b, p, x - 4, by + 1, z + 48, Direction.SOUTH);
        placeDoor(b, p, x + 4, by + 1, z + 48, Direction.SOUTH);
        b.fill(x - 12, by + 5, z + 49, x + 12, by + 5, z + 51, p.roofSlab());
        signAt(b, x, by + 6, z + 49, Direction.SOUTH, "EMPIRE STATE");
        signAt(b, x - 30, by + 3, z + 48, Direction.SOUTH, "OBSERVATION");

        b.fill(x - 40, by, z - 44, x + 40, by, z + 44, p.foundation());
        for (int dx = -30; dx <= 30; dx += 15) {
            for (int dz = -36; dz <= 36; dz += 18) {
                b.fill(x + dx, by + 1, z + dz, x + dx, top - 1, z + dz, p.accent());
            }
        }
        b.fill(x - 6, top - 1, z - 6, x + 6, top - 1, z + 6, p.roofSlab());
        b.fill(x - 5, top - 2, z - 5, x + 5, top - 2, z + 5, p.light());

        // The Art-Deco setbacks.
        towerTier(b, x, z, 34, 38, top + 1, ground + 52, p);
        towerTier(b, x, z, 27, 31, ground + 53, ground + 82, p);
        towerTier(b, x, z, 20, 24, ground + 83, ground + 104, p);

        // Observation deck and the crown.
        b.fill(x - 20, ground + 105, z - 24, x + 20, ground + 105, z + 24, p.foundation());
        b.walls(x - 20, ground + 106, z - 24, x + 20, ground + 106, z + 24, p.rail());
        b.fill(x - 3, ground + 106, z + 24, x + 3, ground + 106, z + 24, air());
        b.fill(x - 13, ground + 105, z - 17, x + 13, ground + 118, z + 17, p.roof());
        b.crenellations(x - 14, z - 18, x + 14, z + 18, ground + 119);
        for (int dx = -12; dx <= 12; dx += 6) {
            b.put(x + dx, ground + 112, z - 17, p.window());
            b.put(x + dx, ground + 112, z + 17, p.window());
        }
        b.fill(x - 2, ground + 119, z - 2, x + 2, ground + 128, z + 2, p.accent());
        b.fill(x - 1, ground + 129, z - 1, x + 1, ground + 136, z + 1, p.rail());
        b.put(x, ground + 137, z, p.light());
        b.put(x, ground + 138, z, Materials.concrete(DyeColor.RED));
    }

    private static void towerTier(StructureBuilder b, int x, int z, int hx, int hz, int y0, int y1,
                                  Palette p) {
        b.room(x - hx, y0, z - hz, x + hx, y1, z + hz);
        b.walls(x - hx, y0 + 1, z - hz, x + hx, y1 - 1, z + hz, p.roof());
        b.walls(x - hx, y0 + 1, z - hz, x + hx, y0 + 1, z + hz, p.accent());
        b.walls(x - hx, y1 - 1, z - hz, x + hx, y1 - 1, z + hz, p.accent());
        for (int wy = y0 + 4; wy <= y1 - 4; wy += 6) {
            for (int dx = -hx + 4; dx <= hx - 4; dx += 6) {
                b.window(x + dx, wy, z - hz, 3, 2, true);
                b.window(x + dx, wy, z + hz, 3, 2, true);
            }
            for (int dz = -hz + 4; dz <= hz - 4; dz += 6) {
                b.window(x - hx, wy, z + dz, 3, 1, true);
                b.window(x + hx, wy, z + dz, 3, 1, true);
            }
        }
        for (int fy = y0 + 12; fy <= y1 - 2; fy += 12) {
            b.fill(x - hx + 1, fy, z - hz + 1, x + hx - 1, fy, z + hz - 1, p.foundation());
        }
    }

    // ------------------------------------------------------------------ Downtown

    private static void downtown(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        bank(b, cx - 100, cz - 28, 64, 56, ground, p, "BANK OF NY");
        bank(b, cx + 36, cz - 28, 64, 56, ground, p, "TRUST CO");
        cityHall(b, cx - 104, cz + 52, 80, 56, ground, p);
        shop(b, cx + 60, cz + 52, 44, 56, 12, ground, p, "DEPARTMENT");
        pocketPark(b, cx - 56, cz + 132, 36, 56, ground, p);
        bank(b, cx - 104, cz + 132, 40, 56, ground, p, "SAVINGS BANK");
        police(b, cx + 20, cz + 132, 40, 56, ground, p);
        tenement(b, cx + 68, cz + 132, 36, 56, 8, ground, p);
        tenement(b, cx - 104, cz + 212, 44, 56, 7, ground, p);
        diner(b, cx + 20, cz + 212, 40, 56, ground, p);
        tenement(b, cx + 68, cz + 212, 36, 56, 8, ground, p);
        ruinLot(b, cx - 52, cz + 212, 36, 56, ground, p);
        signAt(b, cx - 70, ground + 1, cz - 30, Direction.SOUTH, "WALL STREET");
        signAt(b, cx + 60, ground + 1, cz + 108, Direction.SOUTH, "CITY HALL");
        signAt(b, cx + 20, ground + 1, cz + 128, Direction.SOUTH, "POLICE");
    }

    private static void pocketPark(StructureBuilder b, int x, int z, int w, int d, int ground,
                                   Palette p) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        b.fill(x, ground, z, x1, ground, z1, Blocks.GRASS_BLOCK.defaultBlockState());
        b.fill(x + 4, ground, z + 2, x + 10, ground, z1 - 2, p.ground());
        b.fill(x + 4, ground, z + 12, x1 - 4, ground, z + 14, p.ground());
        b.walls(x, ground + 1, z, x1, ground + 1, z1, p.rail());
        b.fill(x + 4, ground + 1, z, x + 8, ground + 1, z, air());
        b.fill(x + 4, ground + 1, z1, x + 8, ground + 1, z1, air());
        b.fill(x + 16, ground, z + 24, x + 24, ground, z + 32, p.foundation());
        b.fill(x + 17, ground + 1, z + 25, x + 23, ground + 1, z + 31,
                Blocks.WATER.defaultBlockState());
        b.put(x + 20, ground + 2, z + 28, p.accent());
        b.put(x + 20, ground + 3, z + 28, p.light());
        tree(b, x + 2, ground, z + 6, p);
        tree(b, x1 - 2, ground, z + 6, p);
        tree(b, x + 2, ground, z1 - 6, p);
        tree(b, x1 - 2, ground, z1 - 6, p);
        bench(b, x + 2, ground, z + 20, p);
        bench(b, x1 - 4, ground, z + 20, p);
        signAt(b, x + 10, ground + 2, z, Direction.NORTH, "POCKET PARK");
    }

    private static void ruinLot(StructureBuilder b, int x, int z, int w, int d, int ground,
                                Palette p) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        b.fill(x, ground, z, x1, ground, z1, p.rubble());
        BlockState weed = p.overgrowth() != null ? p.overgrowth() : p.rubble();
        // Two surviving walls and a collapsed corner.
        b.fill(x, ground + 1, z, x1, ground + 4, z, p.weathered());
        b.fill(x, ground + 1, z, x, ground + 5, z1, p.weathered());
        for (int i = 0; i < 10; i++) {
            int hx = x + 3 + i * 3 % (w - 4);
            int hy = ground + 1 + i % 4;
            b.put(hx, hy, z, air());
        }
        b.fill(x1 - 6, ground + 1, z1 - 8, x1 - 2, ground + 1, z1 - 4, p.foundation());
        b.fill(x1 - 6, ground + 2, z1 - 8, x1 - 2, ground + 2, z1 - 8, p.rail());
        b.put(x1 - 4, ground + 3, z1 - 8, p.rail());
        for (int i = 0; i < 18; i++) {
            int wx = x + 1 + i * 7 % (w - 2);
            int wz = z + 1 + i * 11 % (d - 2);
            b.put(wx, ground + 1, wz, i % 3 == 0 ? weed : p.rubble());
        }
        b.chest(x1 - 3, ground + 1, z1 - 3, SiteLoot.COMMON);
        signAt(b, x + 2, ground + 2, z, Direction.NORTH, "CONDEMNED");
    }

    // ------------------------------------------------------------------ Harlem

    private static void harlem(StructureBuilder b, int cx, int cz, int ground, Palette p,
                               IntBinaryOperator surface) {
        newsroom(b, cx - 272, cz - 156, 24, 28, 12, ground, p);
        church(b, cx - 224, cz - 192, 48, 24, ground, p);
        // The churchyard garden.
        b.walls(cx - 228, ground + 1, cz - 196, cx - 172, ground + 2, cz - 164,
                p.weathered());
        b.fill(cx - 226, ground + 1, cz - 164, cx - 174, ground + 2, cz - 164, air());
        tree(b, cx - 226, ground, cz - 190, p);
        tree(b, cx - 174, ground, cz - 190, p);
        for (int i = 0; i < 5; i++) {
            b.put(cx - 214 + i * 8, ground + 1, cz - 166, p.roofSlab());
            b.put(cx - 214 + i * 8, ground + 2, cz - 166, p.accent());
        }

        brownstoneRow(b, cx - 236, cz - 152, 1, false, Side.W, ground, p);
        brownstoneRow(b, cx - 232, cz - 108, 3, false, Side.W, ground, p);
        brownstoneRow(b, cx - 276, cz - 108, 3, false, Side.E, ground, p);
        brownstoneRow(b, cx - 276, cz - 28, 3, false, Side.E, ground, p);
        brownstoneRow(b, cx - 232, cz - 28, 3, false, Side.W, ground, p);
        brownstoneRow(b, cx - 276, cz + 52, 3, false, Side.E, ground, p);
        brownstoneRow(b, cx - 232, cz + 52, 3, false, Side.W, ground, p);

        club(b, cx - 310, cz - 20, 26, 36, ground, p, "COTTON CLUB");
        club(b, cx - 308, cz + 52, 24, 24, ground, p, "SAVOY");
        marketHall(b, cx - 216, cz + 84, 48, 28, ground, p);
        int ay = surface.applyAsInt(cx - 248, cz + 142);
        b.fill(cx - 265, Math.min(ground, ay) - 1, cz + 127, cx - 232, ay, cz + 156,
                p.foundation());
        if (ay > ground) {
            for (int i = 0; i <= ay - ground; i++) {
                int sz = Math.min(cz + 124 + i, cz + 127);
                b.fill(cx - 252, ground + i, sz, cx - 244, ground + i, cz + 127,
                        p.foundation());
            }
        }
        apothecary(b, cx - 264, cz + 128, 32, 28, ay, p);

        // The tenement row along the avenue.
        int[] tz = {-196, -148, -108, -68, -28, 12, 52, 92, 132, 172};
        int[] td = {32, 21, 32, 21, 33, 21, 33, 21, 33, 21};
        for (int i = 0; i < tz.length; i++) {
            tenement(b, cx - 156, cz + tz[i], 24, td[i], 6 + i % 4, ground, p);
        }

        signAt(b, cx - 200, ground + 1, cz - 120, Direction.SOUTH, "HARLEM");
        signAt(b, cx - 240, ground + 1, cz - 116, Direction.SOUTH, "NEWSROOM");
        signAt(b, cx - 240, ground + 1, cz + 124, Direction.SOUTH, "APOTHECARY");
        signAt(b, cx - 210, ground + 1, cz + 84, Direction.SOUTH, "HARLEM MARKET");
        signAt(b, cx - 272, ground + 1, cz - 20, Direction.EAST, "JAZZ CLUB");
    }

    private static void newsroom(StructureBuilder b, int x, int z, int w, int d, int h,
                                 int ground, Palette p) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        b.fill(x - 1, ground, z - 1, x1 + 1, ground, z1 + 1, p.foundation());
        b.room(x, y0, z, x1, y0 + h, z1);
        b.walls(x, y0 + 1, z, x1, y0 + h - 1, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 1, z1, p.weathered());
        b.walls(x, y0 + h - 1, z, x1, y0 + h - 1, z1, p.accent());
        for (int wy = y0 + 3; wy <= y0 + h - 2; wy += 3) {
            for (int wx = x + 3; wx <= x1 - 3; wx += 5) {
                b.window(wx, wy, z1, 2, 2, true);
            }
        }
        b.fill(x + 2, y0 + 1, z + 2, x1 - 2, y0 + 2, z + 3, p.accent());
        b.fill(x + 3, y0 + 1, z + 6, x1 - 3, y0 + 1, z + 7, p.roofSlab());
        b.put(x1 - 3, y0 + 2, z + 6, p.light());
        placeDoor(b, p, x + w / 2, y0 + 1, z1, Direction.SOUTH);
        signAt(b, x + w / 2, y0 + 4, z1, Direction.SOUTH, "NEWSROOM");
        signAt(b, x + 4, y0 + 2, z1, Direction.SOUTH, "REPORTER LOST");
        b.chest(x1 - 3, y0 + 1, z + 2, SiteLoot.COMMON);
    }

    private static void church(StructureBuilder b, int x, int z, int w, int d, int ground,
                               Palette p) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        b.fill(x - 1, ground, z - 1, x1 + 1, ground, z1 + 1, p.foundation());
        b.room(x, y0, z, x1, y0 + 12, z1);
        b.walls(x, y0 + 1, z, x1, y0 + 11, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 2, z1, p.foundation());
        b.pitchedRoof(x - 2, z - 2, x1 + 2, z1 + 2, y0 + 12, 7, 0);
        // Stained glass down the nave.
        for (int wx = x + 16; wx <= x1 - 4; wx += 8) {
            b.fill(wx, y0 + 3, z1, wx + 2, y0 + 8, z1,
                    Materials.stainedGlass(DyeColor.RED));
            b.fill(wx, y0 + 3, z, wx + 2, y0 + 8, z,
                    Materials.stainedGlass(DyeColor.BLUE));
        }
        // The tower and spire at the west end.
        b.room(x, y0, z, x + 11, y0 + 24, z + 11);
        b.walls(x, y0 + 1, z, x + 11, y0 + 23, z + 11, p.wall());
        for (int wy = y0 + 4; wy <= y0 + 20; wy += 5) {
            b.window(x + 5, wy, z, 3, 1, true);
            b.window(x + 5, wy, z + 11, 3, 1, true);
            b.window(x, wy, z + 5, 3, 1, true);
            b.window(x + 11, wy, z + 5, 3, 1, true);
        }
        b.spire(x + 5, z + 5, y0 + 25, 12);
        placeDoor(b, p, x + 5, y0 + 1, z1, Direction.SOUTH);
        signAt(b, x + 5, y0 + 5, z1, Direction.SOUTH, "HARLEM CHAPEL");
        b.put(x + 3, y0 + 2, z + 3, p.light());
    }

    private static void club(StructureBuilder b, int x, int z, int w, int d, int ground, Palette p,
                             String name) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        b.fill(x - 1, ground, z - 1, x1 + 1, ground, z1 + 1, p.foundation());
        b.room(x, y0, z, x1, y0 + 9, z1);
        b.walls(x, y0 + 1, z, x1, y0 + 8, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 2, z1, p.weathered());
        b.fill(x - 2, y0 + 6, z1 + 1, x1 + 2, y0 + 6, z1 + 3, p.roofSlab());
        b.fill(x - 2, y0 + 5, z1 + 1, x1 + 2, y0 + 5, z1 + 1,
                Materials.stainedGlass(DyeColor.MAGENTA));
        // The stage at the east end and the bar at the west.
        b.fill(x1 - 8, y0 + 1, z + 3, x1 - 2, y0 + 2, z1 - 3, p.accent());
        b.fill(x + 2, y0 + 1, z + 4, x + 3, y0 + 2, z1 - 4, p.foundation());
        b.put(x + 2, y0 + 3, z + 5, p.light());
        for (int i = 0; i < 3; i++) {
            b.put(x + 8 + i * 4, y0 + 1, z + 6, p.accent());
            b.put(x + 8 + i * 4, y0 + 2, z + 6, p.roofSlab());
        }
        b.put(x1 - 4, y0 + 4, z + 4, p.light());
        placeDoor(b, p, x + w / 2, y0 + 1, z1, Direction.SOUTH);
        signAt(b, x + w / 2, y0 + 4, z1, Direction.SOUTH, name);
        signAt(b, x + 3, y0 + 2, z1, Direction.SOUTH, "LIVE JAZZ");
    }

    private static void marketHall(StructureBuilder b, int x, int z, int w, int d, int ground,
                                   Palette p) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        b.fill(x, ground, z, x1, ground, z1, p.ground());
        for (int px = x + 2; px <= x1 - 2; px += 8) {
            for (int pz = z + 2; pz <= z1 - 2; pz += 8) {
                b.fill(px, ground + 1, pz, px, ground + 5, pz, p.accent());
            }
        }
        b.fill(x, ground + 6, z, x1, ground + 6, z1, p.roof());
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, ground + 7, 3, 0);
        for (int i = 0; i < 4; i++) {
            int sx = x + 4 + i % 2 * 20;
            int sz = z + 4 + i / 2 * 14;
            b.fill(sx, ground + 1, sz, sx + 5, ground + 1, sz + 3, p.foundation());
            b.fill(sx, ground + 3, sz, sx + 5, ground + 3, sz + 3,
                    Materials.wool(i % 2 == 0 ? DyeColor.RED : DyeColor.WHITE));
            b.put(sx, ground + 2, sz, p.accent());
            b.put(sx + 5, ground + 2, sz + 3, p.accent());
            b.put(sx + 2, ground + 2, sz + 1, p.light());
        }
        b.chest(x1 - 3, ground + 1, z1 - 3, SiteLoot.COMMON);
        b.fill(x + w / 2 - 2, ground + 5, z1 + 1, x + w / 2 + 2, ground + 5, z1 + 1,
                p.roofSlab());
        signAt(b, x + w / 2, ground + 6, z1 + 1, Direction.SOUTH, "HARLEM MARKET");
    }

    private static void apothecary(StructureBuilder b, int x, int z, int w, int d, int ground,
                                   Palette p) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        b.fill(x - 1, ground, z - 1, x1 + 1, ground, z1 + 1, p.foundation());
        b.room(x, y0, z, x1, y0 + 8, z1);
        b.walls(x, y0 + 1, z, x1, y0 + 7, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 2, z1, p.weathered());
        b.fill(x + 2, y0 + 3, z, x1 - 2, y0 + 5, z, Materials.stainedGlass(DyeColor.GREEN));
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, y0 + 8, 4, 0);
        b.fill(x + 2, y0 + 1, z + 3, x + 3, y0 + 2, z1 - 3, p.accent());
        b.fill(x1 - 6, y0 + 1, z + 4, x1 - 4, y0 + 1, z + 5, p.foundation());
        b.put(x1 - 5, y0 + 2, z + 4, p.light());
        placeDoor(b, p, x + w / 2, y0 + 1, z, Direction.NORTH);
        signAt(b, x + w / 2, y0 + 4, z, Direction.NORTH, "APOTHECARY");
        signAt(b, x + 3, y0 + 2, z, Direction.NORTH, "REMEDIES");
        b.chest(x1 - 3, y0 + 1, z1 - 3, SiteLoot.ORDER);
    }

    private static void brownstoneRow(StructureBuilder b, int x, int z, int units, boolean alongX,
                                      Side door, int ground, Palette p) {
        for (int i = 0; i < units; i++) {
            int ux = alongX ? x + i * 13 : x;
            int uz = alongX ? z : z + i * 17;
            brownstone(b, ux, uz, door, ground, p, i);
        }
    }

    private static void brownstone(StructureBuilder b, int x, int z, Side door, int ground,
                                   Palette p, int index) {
        int x1 = x + 11;
        int z1 = z + 16;
        int y0 = ground + 1;
        b.fill(x - 1, ground, z - 1, x1 + 1, ground, z1 + 1, p.foundation());
        b.room(x, y0, z, x1, y0 + 5, z1);
        b.walls(x, y0 + 1, z, x1, y0 + 4, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 2, z1, p.weathered());
        b.walls(x, y0 + 4, z, x1, y0 + 4, z1, p.roofSlab());
        int front = door == Side.W ? x : x1;
        for (int wy = y0 + 2; wy <= y0 + 4; wy += 2) {
            for (int wz = z + 3; wz <= z1 - 3; wz += 4) {
                b.window(front, wy, wz, 2, 1, true);
            }
        }
        int dz = z + 8;
        int dx = door == Side.W ? x : x1;
        Direction facing = door == Side.W ? Direction.WEST : Direction.EAST;
        placeDoor(b, p, dx, y0 + 1, dz, facing);
        if (door == Side.W) {
            b.fill(x - 2, ground + 1, dz - 1, x - 1, ground + 1, dz + 1, p.foundation());
        } else {
            b.fill(x1 + 1, ground + 1, dz - 1, x1 + 2, ground + 1, dz + 1, p.foundation());
        }
        b.fill(x - 1, y0 + 6, z - 1, x1 + 1, y0 + 6, z1 + 1, p.roofSlab());
        int fx = door == Side.W ? x - 1 : x1 + 1;
        for (int wy = y0 + 2; wy <= y0 + 4; wy += 2) {
            b.fill(fx, wy, z + 3, fx, wy, z + 6, p.rail());
            b.put(fx, wy + 1, z + 3, p.rail());
            b.put(fx, wy + 1, z + 6, p.rail());
        }
        if (index % 3 == 1) {
            waterTower(b, x + 2, y0 + 7, z + 3, p);
        }
        b.put(x + 2, y0 + 2, z + 2, p.light());
    }

    private static void tenement(StructureBuilder b, int x, int z, int w, int d, int h,
                                 int ground, Palette p) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        b.fill(x - 1, ground, z - 1, x1 + 1, ground, z1 + 1, p.foundation());
        b.room(x, y0, z, x1, y0 + h, z1);
        b.walls(x, y0 + 1, z, x1, y0 + h - 1, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 2, z1, p.weathered());
        b.walls(x, y0 + h - 1, z, x1, y0 + h - 1, z1, p.accent());
        for (int wy = y0 + 3; wy <= y0 + h - 2; wy += 3) {
            for (int wx = x + 3; wx <= x1 - 3; wx += 4) {
                b.window(wx, wy, z1, 2, 1, true);
                b.window(wx, wy, z, 2, 1, true);
            }
            for (int wz = z + 3; wz <= z1 - 3; wz += 4) {
                b.window(x1, wy, wz, 2, 1, true);
            }
        }
        b.crenellations(x - 1, z - 1, x1 + 1, z1 + 1, y0 + h + 1);
        placeDoor(b, p, x + w / 2, y0 + 1, z1, Direction.SOUTH);
        fireEscape(b, x + 2, y0, z1 + 1, (h - 2) / 4, p);
        // Laundry lines across the alley frontage.
        for (int i = 0; i < 2; i++) {
            b.fill(x + 2, y0 + 4 + i * 2, z1 + 2, x1 - 2, y0 + 4 + i * 2, z1 + 2,
                    Materials.wool(i % 2 == 0 ? DyeColor.WHITE : DyeColor.LIGHT_GRAY));
        }
        b.put(x + 2, y0 + 2, z + 2, p.light());
    }

    // ------------------------------------------------------------------ Docks

    private static void docks(StructureBuilder b, int cx, int cz, int ground, Palette p,
                              IntBinaryOperator surface) {
        int wx0 = cx + 252;
        int wx1 = cx + 312;
        int wz0 = cz - 90;
        int wz1 = cz + 90;
        b.fill(wx0, ground - 6, wz0, wx1, ground - 6, wz1, p.foundation());
        b.fill(wx0, ground - 5, wz0, wx1, ground + 2, wz1, air());
        b.fill(wx0, ground - 5, wz0, wx1, ground - 1, wz1,
                Blocks.WATER.defaultBlockState());
        b.fill(wx0 - 2, ground, wz0 - 2, wx0 - 1, ground, wz1 + 2, p.foundation());
        b.fill(wx0, ground, wz0 - 2, wx1, ground, wz0 - 1, p.foundation());
        b.fill(wx0, ground, wz1 + 1, wx1, ground, wz1 + 2, p.foundation());

        warehouse(b, cx + 180, cz - 192, 48, 36, 12, ground, p, "WAREHOUSE");
        warehouse(b, cx + 180, cz - 148, 48, 20, 10, ground, p, "TOBACCO");
        warehouse(b, cx + 180, cz - 108, 48, 36, 12, ground, p, "COFFEE");
        warehouse(b, cx + 180, cz - 64, 48, 16, 9, ground, p, "GRAIN");
        warehouse(b, cx + 180, cz - 28, 48, 60, 14, ground, p, "FREIGHT");
        warehouse(b, cx + 180, cz + 48, 48, 36, 12, ground, p, "COTTON");
        warehouse(b, cx + 180, cz + 92, 48, 20, 9, ground, p, "ICE");
        dockOffice(b, cx + 180, cz + 128, 28, 12, ground, p);

        // The goods railway and its shed: one straight, level track on an embankment.
        int railY = surface.applyAsInt(cx + 188, cz - 236);
        b.fill(cx + 140, Math.min(ground, railY) - 3, cz - 239, cx + 236, railY, cz - 227,
                p.foundation());
        if (railY + 3 <= ground + 1) {
            b.fill(cx + 140, railY + 3, cz - 239, cx + 236, ground + 1, cz - 227, air());
        }
        b.fill(cx + 140, railY, cz - 238, cx + 236, railY, cz - 228, p.rubble());
        for (int rx = cx + 140; rx <= cx + 236; rx += 3) {
            b.fill(rx, railY + 1, cz - 237, rx + 1, railY + 1, cz - 229,
                    Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }
        BlockState rail = Blocks.RAIL.defaultBlockState()
                .setValue(BlockStateProperties.RAIL_SHAPE, RailShape.EAST_WEST);
        for (int rx = cx + 140; rx <= cx + 236; rx++) {
            b.put(rx, railY + 2, cz - 236, rail);
            b.put(rx, railY + 2, cz - 230, rail);
        }
        b.fill(cx + 147, Math.min(ground, railY) - 1, cz - 261, cx + 196, railY, cz - 241,
                p.foundation());
        if (railY + 10 <= ground + 1) {
            b.fill(cx + 147, railY + 10, cz - 261, cx + 196, ground + 1, cz - 241, air());
        }
        goodsShed(b, cx + 148, cz - 260, 48, 18, railY, p);
        boxcar(b, cx + 200, cz - 233, railY);

        // Piers and the moored barge.
        for (int pz : new int[]{-70, -20, 80}) {
            pier(b, cx + 246, cz + pz - 4, 66, 8, ground, p);
        }
        pier(b, cx + 246, cz - 44, 66, 9, ground, p);
        pier(b, cx + 246, cz + 36, 66, 9, ground, p);
        barge(b, cx + 268, cz - 60, ground, p);

        crane(b, cx + 250, cz - 100, ground, p);
        crane(b, cx + 250, cz - 30, ground, p);
        crane(b, cx + 250, cz + 50, ground, p);
        crane(b, cx + 250, cz + 132, ground, p);

        // Quay bollards, crates and the speakeasy hatch.
        for (int rz = -196; rz <= 136; rz += 16) {
            b.put(cx + 247, ground + 1, cz + rz, p.foundation());
            b.put(cx + 247, ground + 2, cz + rz, p.accent());
        }
        for (int i = 0; i < 10; i++) {
            int bx = cx + 230 + i % 3 * 5;
            int bz = cz - 190 + i / 3 * 12;
            b.put(bx, ground + 1, bz, Blocks.BARREL.defaultBlockState());
            b.put(bx + 1, ground + 1, bz, Blocks.BARREL.defaultBlockState());
        }
        speakeasy(b, cx + 210, cz + 30, 24, 20, ground, p);

        signAt(b, cx + 240, ground + 1, cz - 124, Direction.SOUTH, "DOCKS");
        signAt(b, cx + 240, ground + 1, cz - 120, Direction.SOUTH, "NO ENTRY");
        signAt(b, cx + 248, ground + 1, cz - 78, Direction.SOUTH, "MIND THE WATER");
        signAt(b, cx + 250, ground + 1, cz - 74, Direction.SOUTH, "PIER 17");
        signAt(b, cx + 236, ground + 1, cz + 128, Direction.WEST, "HARBOR OFFICE");
    }

    private static void warehouse(StructureBuilder b, int x, int z, int w, int d, int h,
                                  int ground, Palette p, String name) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        b.fill(x - 2, ground, z - 2, x1 + 2, ground, z1 + 2, p.foundation());
        b.room(x, y0, z, x1, y0 + h, z1);
        b.walls(x, y0 + 1, z, x1, y0 + h - 1, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 1, z1, p.foundation());
        b.walls(x, y0 + h - 1, z, x1, y0 + h - 1, z1, p.accent());
        for (int dz = z + 5; dz <= z1 - 5; dz += 12) {
            b.fill(x1, y0 + 1, dz, x1, y0 + 7, dz + 4, air());
            b.fill(x1, y0 + 8, dz, x1, y0 + 8, dz + 4, p.accent());
            b.fill(x1 - 1, y0 + 1, dz + 4, x1 - 1, y0 + 7, dz + 4, p.frame());
        }
        for (int wz = z + 6; wz <= z1 - 6; wz += 10) {
            b.window(x, y0 + 7, wz, 2, 1, true);
        }
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, y0 + h, 6, 1);
        b.fill(x1 + 1, y0 + h + 3, z + d / 2, x1 + 5, y0 + h + 3, z + d / 2, p.accent());
        b.fill(x1 + 4, y0 + h - 2, z + d / 2, x1 + 4, y0 + h + 2, z + d / 2, p.rail());
        b.fill(x + 3, y0 + 1, z + 3, x + 5, y0 + 2, z + 5, p.foundation());
        b.chest(x + 8, y0 + 1, z + 4, SiteLoot.COMMON);
        signAt(b, x1, y0 + 6, z + d / 2 + 4, Direction.EAST, name);
        b.put(x + 2, y0 + 2, z + 2, p.light());
        b.put(x1 - 2, y0 + 2, z1 - 2, p.light());
    }

    private static void dockOffice(StructureBuilder b, int x, int z, int w, int d, int ground,
                                   Palette p) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        b.fill(x - 1, ground, z - 1, x1 + 1, ground, z1 + 1, p.foundation());
        b.room(x, y0, z, x1, y0 + 6, z1);
        b.walls(x, y0 + 1, z, x1, y0 + 5, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 1, z1, p.weathered());
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, y0 + 6, 3, 0);
        placeDoor(b, p, x + w / 2, y0 + 1, z1, Direction.SOUTH);
        signAt(b, x + w / 2, y0 + 4, z1, Direction.SOUTH, "HARBOR");
        b.put(x + 2, y0 + 2, z + 2, p.light());
    }

    private static void goodsShed(StructureBuilder b, int x, int z, int w, int d, int ground,
                                  Palette p) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        b.fill(x, ground, z, x1, ground, z1, p.foundation());
        for (int px = x; px <= x1; px += 8) {
            b.fill(px, y0, z, px, y0 + 8, z, p.rail());
            b.fill(px, y0, z1, px, y0 + 8, z1, p.rail());
        }
        b.fill(x - 1, y0 + 9, z - 1, x1 + 1, y0 + 9, z1 + 1, p.roof());
        b.pitchedRoof(x - 2, z - 2, x1 + 2, z1 + 2, y0 + 9, 3, 0);
        b.fill(x, y0 + 4, z1, x1, y0 + 4, z1, p.accent());
        b.fill(x, y0 + 1, z1, x1, y0 + 1, z1, p.accent());
        signAt(b, x + 4, y0 + 5, z1, Direction.SOUTH, "GOODS DEPOT");
        signAt(b, x1 - 4, y0 + 5, z1, Direction.SOUTH, "RAILWAY");
        b.put(x + 2, y0 + 8, z + 2, p.light());
    }

    private static void boxcar(StructureBuilder b, int x, int z, int ground) {
        b.fill(x, ground + 2, z - 2, x + 12, ground + 6, z + 2,
                Blocks.DARK_OAK_PLANKS.defaultBlockState());
        b.fill(x + 2, ground + 4, z - 2, x + 10, ground + 5, z - 2,
                Blocks.GLASS_PANE.defaultBlockState());
        b.fill(x + 2, ground + 4, z + 2, x + 10, ground + 5, z + 2,
                Blocks.GLASS_PANE.defaultBlockState());
        b.put(x + 12, ground + 3, z, Blocks.LANTERN.defaultBlockState());
    }

    private static void pier(StructureBuilder b, int x, int z, int len, int width, int ground,
                             Palette p) {
        int z1 = z + width - 1;
        b.fill(x, ground, z, x + len, ground + 1, z1,
                Blocks.DARK_OAK_PLANKS.defaultBlockState());
        for (int px = x + 2; px <= x + len; px += 8) {
            b.fill(px, ground - 5, z, px, ground - 1, z, p.accent());
            b.fill(px, ground - 5, z1, px, ground - 1, z1, p.accent());
        }
        for (int px = x + 6; px <= x + len; px += 20) {
            b.put(px, ground + 2, z, p.accent());
            b.put(px, ground + 2, z1, p.accent());
        }
        b.put(x + 4, ground + 2, z + width / 2, p.light());
        b.fill(x + len - 6, ground + 2, z + 2, x + len - 4, ground + 2, z + 4,
                p.foundation());
    }

    private static void barge(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x, ground - 2, z, x + 20, ground - 1, z + 12,
                Blocks.DARK_OAK_PLANKS.defaultBlockState());
        b.fill(x + 2, ground, z + 2, x + 16, ground + 3, z + 10, p.wall());
        b.fill(x + 4, ground + 4, z + 4, x + 14, ground + 4, z + 8, p.roofSlab());
        b.fill(x + 9, ground + 5, z + 6, x + 9, ground + 12, z + 6, p.accent());
        b.put(x + 9, ground + 13, z + 6, p.light());
        b.put(x + 3, ground + 2, z + 6, p.light());
    }

    private static void crane(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 2, ground, z - 2, x + 2, ground, z + 2, p.foundation());
        b.fill(x, ground + 1, z, x, ground + 18, z, p.accent());
        b.fill(x - 1, ground + 18, z, x + 10, ground + 18, z, p.accent());
        b.fill(x + 10, ground + 12, z, x + 10, ground + 17, z, p.rail());
        b.put(x + 10, ground + 11, z, p.light());
        b.fill(x - 2, ground + 18, z, x - 2, ground + 20, z, p.rail());
        b.fill(x - 3, ground + 1, z + 3, x + 1, ground + 4, z + 6, p.wall());
        b.fill(x - 2, ground + 5, z + 3, x, ground + 5, z + 6, p.roofSlab());
    }

    private static void speakeasy(StructureBuilder b, int x, int z, int w, int d, int ground,
                                  Palette p) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int floor = ground - 4;
        int head = ground - 1;
        b.fill(x, floor - 1, z, x1, head, z1, air());
        b.fill(x, floor - 1, z, x1, floor - 1, z1, p.foundation());
        b.fill(x, head, z, x1, head, z1, p.roof());
        b.walls(x, floor, z, x1, head - 1, z1, p.wall());
        b.fill(x + 1, floor, z + 1, x1 - 1, head - 1, z1 - 1, air());
        b.fill(x1, floor, z + 7, x1, head - 1, z + 13, air());

        // The stair from the quay, descending west into the cellar.
        b.fill(x1 + 1, floor - 1, z + 7, x1 + 7, ground + 1, z + 13, air());
        for (int i = 0; i <= 4; i++) {
            b.fill(x1 + 6 - i, floor, z + 7, x1 + 6 - i, ground - i, z + 13, p.foundation());
        }
        b.fill(x1 + 1, ground + 1, z + 7, x1 + 7, ground + 1, z + 7, p.rail());
        b.fill(x1 + 1, ground + 1, z + 13, x1 + 7, ground + 1, z + 13, p.rail());
        b.fill(x1 + 7, ground + 1, z + 8, x1 + 7, ground + 1, z + 12, p.rail());
        signAt(b, x1 + 8, ground + 2, z + 10, Direction.EAST, "SPEAKEASY");
        signAt(b, x1 + 8, ground + 1, z + 14, Direction.SOUTH, "NO COPS");

        // The bar, tables and the bootlegger's crates.
        b.fill(x + 2, floor, z + 2, x + 3, floor + 2, z1 - 2, p.accent());
        b.put(x + 2, floor + 3, z + 3, p.light());
        for (int i = 0; i < 3; i++) {
            int tz = z + 4 + i * 5;
            b.put(x + 7, floor, tz, p.accent());
            b.put(x + 7, floor + 1, tz, p.roofSlab());
            b.put(x + 9, floor, tz, p.accent());
            b.put(x + 9, floor + 1, tz, p.roofSlab());
        }
        b.fill(x1 - 4, floor, z + 2, x1 - 2, floor + 1, z + 4,
                Blocks.BARREL.defaultBlockState());
        b.chest(x1 - 3, floor, z1 - 3, SiteLoot.CULT);
        b.chest(x + 3, floor, z1 - 3, SiteLoot.COMMON);
        signAt(b, x1 - 2, floor + 2, z1, Direction.NORTH, "THE CELLAR", "CARGO LEDGER");
    }

    // ------------------------------------------------------------------ Subway

    private static void subway(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int floorY = ground - 9;
        int headY = ground - 4;
        ticketHall(b, cx + 16, cz + 72, 24, 24, ground, p);
        // The stair shaft down from the ticket hall.
        b.fill(cx + 24, floorY - 1, cz + 84, cx + 32, ground + 8, cz + 94, air());
        b.fill(cx + 24, floorY - 1, cz + 94, cx + 32, headY, cz + 100, air());
        for (int i = 0; i <= 9; i++) {
            b.fill(cx + 24, floorY, cz + 84 + i, cx + 32, ground + 1 - i,
                    cz + 84 + i, p.foundation());
        }
        b.fill(cx + 23, floorY, cz + 84, cx + 23, ground + 8, cz + 94, p.wall());
        b.fill(cx + 33, floorY, cz + 84, cx + 33, ground + 8, cz + 94, p.wall());
        b.fill(cx + 23, floorY, cz + 94, cx + 23, headY, cz + 100, p.wall());
        b.fill(cx + 33, floorY, cz + 94, cx + 33, headY, cz + 100, p.wall());
        b.fill(cx + 24, ground + 8, cz + 84, cx + 32, ground + 8, cz + 94, p.roof());
        b.fill(cx + 24, headY, cz + 94, cx + 32, headY, cz + 100, p.roof());
        b.fill(cx + 23, ground + 1, cz + 84, cx + 23, ground + 2, cz + 94, p.rail());
        b.fill(cx + 33, ground + 1, cz + 84, cx + 33, ground + 2, cz + 94, p.rail());

        tunnel(b, cx - 100, cz + 100, cx + 100, cz + 108, floorY, headY, p);
        tunnel(b, cx - 4, cz + 104, cx + 4, cz + 134, floorY, headY, p);
        // Open the shaft into the east-west tunnel and lay its landing.
        b.fill(cx + 24, floorY, cz + 94, cx + 32, floorY, cz + 100, p.foundation());
        b.fill(cx + 24, floorY + 1, cz + 100, cx + 32, headY - 1, cz + 100, air());
        // The crossing chamber.
        b.fill(cx - 10, floorY, cz + 100, cx + 10, headY, cz + 108, air());
        b.fill(cx - 10, floorY, cz + 100, cx + 10, floorY, cz + 108, p.foundation());
        b.fill(cx - 10, headY, cz + 100, cx + 10, headY, cz + 108, p.roof());
        b.walls(cx - 10, floorY + 1, cz + 100, cx + 10, headY - 1, cz + 108, p.wall());
        b.fill(cx - 2, floorY + 1, cz + 100, cx + 2, headY - 1, cz + 100, air());
        b.fill(cx - 2, floorY + 1, cz + 108, cx + 2, headY - 1, cz + 108, air());
        // A disused platform and a sealed branch.
        tunnel(b, cx + 56, cz + 96, cx + 92, cz + 112, floorY, headY, p);
        b.fill(cx + 60, floorY + 1, cz + 96, cx + 88, floorY + 1, cz + 100, p.foundation());
        b.walls(cx + 60, floorY + 2, cz + 96, cx + 88, floorY + 2, cz + 96, p.rail());
        tunnel(b, cx - 84, cz + 84, cx - 76, cz + 100, floorY, headY, p);
        b.fill(cx - 84, floorY + 1, cz + 84, cx - 76, headY - 1, cz + 84,
                Blocks.COBBLESTONE.defaultBlockState());
        b.fill(cx - 83, floorY + 1, cz + 100, cx - 77, headY - 1, cz + 100, air());
        signAt(b, cx + 70, floorY + 2, cz + 96, Direction.SOUTH, "CLOSED");
        secretJunction(b, cx, cz + 118, floorY, headY, p);
        for (int tx = -90; tx <= 90; tx += 20) {
            b.put(cx + tx, headY - 1, cz + 104, p.light());
        }
        for (int tz = 108; tz <= 130; tz += 12) {
            b.put(cx, headY - 1, cz + tz, p.light());
        }
        signAt(b, cx - 40, floorY + 2, cz + 100, Direction.SOUTH, "MIND THE GAP");
        signAt(b, cx + 40, floorY + 2, cz + 100, Direction.SOUTH, "NO ENTRY");
        signAt(b, cx + 4, floorY + 2, cz + 116, Direction.WEST, "JUNCTION");
    }

    private static void ticketHall(StructureBuilder b, int x, int z, int w, int d, int ground,
                                   Palette p) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        b.fill(x - 1, ground, z - 1, x1 + 1, ground, z1 + 1, p.foundation());
        b.room(x, y0, z, x1, y0 + 11, z1);
        b.walls(x, y0 + 1, z, x1, y0 + 10, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 2, z1, p.foundation());
        b.walls(x, y0 + 10, z, x1, y0 + 10, z1, p.accent());
        b.fill(x, y0, z, x1, y0, z1, p.foundation());
        b.fill(x + 8, y0, z + 12, x + 16, y0 + 6, z + 22, air());
        b.fill(x + 8, y0, z + 12, x + 16, y0 + 1, z + 12, p.rail());
        b.fill(x + 8, y0, z + 22, x + 16, y0 + 1, z + 22, p.rail());
        b.fill(x + 8, y0, z + 12, x + 8, y0 + 1, z + 22, p.rail());
        b.fill(x + 16, y0, z + 12, x + 16, y0 + 1, z + 22, p.rail());
        b.fill(x + 11, y0, z + 12, x + 13, y0 + 1, z + 12, air());
        b.fill(x + 2, y0 + 2, z + 2, x + 6, y0 + 3, z + 2, p.frame());
        b.fill(x1 - 6, y0 + 2, z + 2, x1 - 2, y0 + 3, z + 2, p.frame());
        b.fill(x + 2, y0 + 1, z + 6, x1 - 2, y0 + 1, z + 8, p.foundation());
        b.fill(x - 2, y0 + 5, z1, x1 + 2, y0 + 5, z1 + 2, p.roofSlab());
        b.fill(x - 1, y0 + 4, z1 + 2, x1 + 1, y0 + 4, z1 + 2, p.light());
        placeDoor(b, p, x, y0 + 1, z + 8, Direction.WEST);
        signAt(b, x + 20, y0 + 6, z1, Direction.SOUTH, "SUBWAY");
        signAt(b, x + 3, y0 + 4, z1, Direction.SOUTH, "TICKETS");
        b.put(x + 2, y0 + 3, z + 2, p.light());
        b.put(x1 - 2, y0 + 3, z + 2, p.light());
    }

    private static void tunnel(StructureBuilder b, int x0, int z0, int x1, int z1,
                               int floorY, int headY, Palette p) {
        b.fill(x0, floorY, z0, x1, floorY, z1, p.foundation());
        b.fill(x0, headY, z0, x1, headY, z1, p.roof());
        b.walls(x0, floorY + 1, z0, x1, headY - 1, z1, p.wall());
        b.fill(x0 + 1, floorY + 1, z0 + 1, x1 - 1, headY - 1, z1 - 1, air());
        if (x1 - x0 >= z1 - z0) {
            for (int px = x0 + 4; px <= x1 - 4; px += 12) {
                b.fill(px, floorY + 1, z0, px, headY - 1, z0, p.weathered());
                b.fill(px, floorY + 1, z1, px, headY - 1, z1, p.weathered());
            }
        } else {
            for (int pz = z0 + 4; pz <= z1 - 4; pz += 12) {
                b.fill(x0, floorY + 1, pz, x0, headY - 1, pz, p.weathered());
                b.fill(x1, floorY + 1, pz, x1, headY - 1, pz, p.weathered());
            }
        }
    }

    private static void secretJunction(StructureBuilder b, int x, int z, int floorY, int headY,
                                       Palette p) {
        int z0 = z;
        int z1 = z + 16;
        int x0 = x - 10;
        int x1 = x + 10;
        b.fill(x0, floorY, z0, x1, headY, z1, air());
        b.fill(x0, floorY, z0, x1, floorY, z1, p.foundation());
        b.fill(x0, headY, z0, x1, headY, z1, p.roof());
        b.walls(x0, floorY + 1, z0, x1, headY - 1, z1, p.wall());
        b.fill(x - 2, floorY + 1, z0, x + 2, headY - 1, z0, air());
        b.fill(x - 2, floorY + 1, z1, x + 2, headY - 1, z1, p.wall());

        b.fill(x - 4, floorY + 1, z1 - 4, x + 4, floorY + 1, z1 - 2,
                Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        b.fill(x - 1, floorY + 2, z1 - 3, x + 1, floorY + 2, z1 - 3,
                Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        b.put(x, floorY + 3, z1 - 3, Blocks.GOLD_BLOCK.defaultBlockState());
        b.chest(x - 8, floorY + 1, z1 - 1, SiteLoot.RELIC);
        b.chest(x + 8, floorY + 1, z1 - 1, SiteLoot.CULT);
        b.chest(x, floorY + 1, z0 + 2, SiteLoot.COMMON);
        signAt(b, x, floorY + 3, z1, Direction.NORTH, "THE VEIL BELOW", "SEALED 1929");
        signAt(b, x - 8, floorY + 1, z0 + 1, Direction.SOUTH, "CARGO LEDGER");
        b.put(x0 + 1, headY - 1, z0 + 1, Palette.cobweb());
        b.put(x1 - 1, headY - 1, z0 + 1, Palette.cobweb());
        b.put(x0 + 1, headY - 1, z1 - 1, Palette.cobweb());
        b.put(x1 - 1, headY - 1, z1 - 1, Palette.cobweb());
        b.put(x0 + 1, floorY + 2, z0 + 1, p.light());
        b.put(x1 - 1, floorY + 2, z1 - 1, p.light());
    }

    // ------------------------------------------------------------------ dressing

    private static void dressing(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        for (int rz = -110; rz <= 300; rz += 40) {
            lamp(b, cx - 10, cz + rz, ground, p);
            lamp(b, cx + 10, cz + rz, ground, p);
        }
        for (int rx = -100; rx <= 100; rx += 50) {
            lamp(b, cx + rx, cz - 40, ground, p);
            lamp(b, cx + rx, cz + 40, ground, p);
        }
        taxi(b, cx - 5, cz + 200, ground);
        taxi(b, cx - 5, cz - 80, ground);
        taxi(b, cx - 5, cz + 260, ground);
        hydrant(b, cx + 12, cz + 12, ground, p);
        hydrant(b, cx - 12, cz + 132, ground, p);
        b.monument(cx - 24, cz - 136, ground + 1);
        b.monument(cx + 24, cz - 136, ground + 1);
        // A newspaper stand and a few crates at the gate.
        b.fill(cx - 20, ground + 1, cz + 306, cx - 17, ground + 2, cz + 309,
                Blocks.DARK_OAK_PLANKS.defaultBlockState());
        b.put(cx - 18, ground + 3, cz + 307, p.roofSlab());
        b.put(cx + 18, ground + 1, cz + 306, p.foundation());
        b.put(cx + 18, ground + 2, cz + 306, p.foundation());
        // Manhole covers down the avenue.
        for (int rz = -100; rz <= 300; rz += 50) {
            b.put(cx + 3, ground, cz + rz, p.accent());
        }
    }

    private static void lamp(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground, z, p.foundation());
        b.fill(x, ground + 1, z, x, ground + 3, z, p.accent());
        b.put(x, ground + 4, z, p.light());
    }

    private static void hydrant(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground + 1, z, p.accent());
        b.put(x, ground + 2, z, Materials.concrete(DyeColor.RED));
        b.put(x, ground + 3, z, p.accent());
    }

    private static void taxi(StructureBuilder b, int x, int z, int ground) {
        BlockState yellow = Materials.concrete(DyeColor.YELLOW);
        BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
        BlockState tyre = Blocks.CONCRETE.pick(DyeColor.BLACK).defaultBlockState();
        b.fill(x, ground + 1, z, x + 2, ground + 3, z + 5, yellow);
        b.fill(x + 1, ground + 4, z + 1, x + 1, ground + 4, z + 4, yellow);
        b.fill(x, ground + 3, z + 1, x, ground + 3, z + 4, glass);
        b.fill(x + 2, ground + 3, z + 1, x + 2, ground + 3, z + 4, glass);
        b.put(x, ground + 1, z, tyre);
        b.put(x + 2, ground + 1, z, tyre);
        b.put(x, ground + 1, z + 5, tyre);
        b.put(x + 2, ground + 1, z + 5, tyre);
        b.put(x + 1, ground + 5, z + 2, Blocks.LANTERN.defaultBlockState());
    }

    private static void bench(StructureBuilder b, int x, int y, int z, Palette p) {
        b.put(x, y + 1, z, p.roofSlab());
        b.put(x + 1, y + 1, z, p.roofSlab());
        b.put(x, y + 2, z + 1, p.accent());
        b.put(x + 1, y + 2, z + 1, p.accent());
    }

    private static void tree(StructureBuilder b, int x, int y, int z, Palette p) {
        int h = 4 + Math.abs(x * 3 + z) % 3;
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState leaf = Blocks.OAK_LEAVES.defaultBlockState();
        b.put(x, y, z, p.ground());
        b.fill(x, y + 1, z, x, y + h, z, log);
        b.fill(x - 2, y + h - 1, z - 2, x + 2, y + h + 1, z + 2, leaf);
        b.fill(x - 1, y + h + 2, z - 1, x + 1, y + h + 2, z + 1, leaf);
        b.put(x, y + h + 3, z, leaf);
    }

    // ------------------------------------------------------------------ building shells

    private static void office(StructureBuilder b, int x, int z, int w, int d, int h, int ground,
                               Palette p, String name) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        b.fill(x - 2, ground, z - 2, x1 + 2, ground, z1 + 2, p.foundation());
        b.room(x, y0, z, x1, y0 + h, z1);
        b.walls(x, y0 + 1, z, x1, y0 + h - 1, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 1, z1, p.foundation());
        b.walls(x, y0 + h - 1, z, x1, y0 + h - 1, z1, p.accent());
        for (int wy = y0 + 3; wy <= y0 + h - 3; wy += 3) {
            for (int wx = x + 3; wx <= x1 - 3; wx += 5) {
                b.window(wx, wy, z, 2, 2, true);
                b.window(wx, wy, z1, 2, 2, true);
            }
            for (int wz = z + 3; wz <= z1 - 3; wz += 5) {
                b.window(x, wy, wz, 2, 1, true);
                b.window(x1, wy, wz, 2, 1, true);
            }
        }
        b.crenellations(x - 1, z - 1, x1 + 1, z1 + 1, y0 + h + 1);
        placeDoor(b, p, x + w / 2, y0 + 1, z1, Direction.SOUTH);
        b.fill(x + w / 2 - 3, y0 + 4, z1 + 1, x + w / 2 + 3, y0 + 4, z1 + 3, p.roofSlab());
        signAt(b, x + w / 2, y0 + 5, z1 + 3, Direction.SOUTH, name);
        b.put(x + 2, y0 + 2, z + 2, p.light());
        b.put(x1 - 2, y0 + 2, z1 - 2, p.light());
        fireEscape(b, x + 2, y0, z1 + 1, h / 4, p);
        if ((x + z) % 3 == 0) {
            waterTower(b, x + 3, y0 + h + 1, z + 3, p);
        }
    }

    private static void hotel(StructureBuilder b, int x, int z, int w, int d, int h, int ground,
                              Palette p, String name) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        b.fill(x - 2, ground, z - 2, x1 + 2, ground, z1 + 2, p.foundation());
        b.room(x, y0, z, x1, y0 + h, z1);
        b.walls(x, y0 + 1, z, x1, y0 + h - 1, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 1, z1, p.foundation());
        b.walls(x, y0 + 5, z, x1, y0 + 5, z1, p.accent());
        b.walls(x, y0 + h - 1, z, x1, y0 + h - 1, z1, p.accent());
        for (int wy = y0 + 3; wy <= y0 + h - 2; wy += 3) {
            for (int wx = x + 3; wx <= x1 - 3; wx += 4) {
                b.window(wx, wy, z, 2, 2, true);
                b.window(wx, wy, z1, 2, 2, true);
            }
        }
        b.crenellations(x - 1, z - 1, x1 + 1, z1 + 1, y0 + h + 1);
        placeDoor(b, p, x + w / 2, y0 + 1, z1, Direction.SOUTH);
        b.fill(x + w / 2 - 6, y0 + 4, z1 + 1, x + w / 2 + 6, y0 + 4, z1 + 5, p.roofSlab());
        b.fill(x + w / 2 - 5, y0 + 3, z1 + 5, x + w / 2 + 5, y0 + 3, z1 + 5, p.light());
        b.fill(x + 3, y0 + 1, z + 3, x + 5, y0 + 2, z + 3, p.accent());
        bed(b, x + 4, y0 + 1, z + 6, Direction.NORTH);
        b.put(x + 8, y0 + 2, z + 4, p.light());
        signAt(b, x + w / 2, y0 + 5, z1 + 5, Direction.SOUTH, name);
        signAt(b, x + 4, y0 + 2, z1, Direction.SOUTH, "RECEPTION");
        fireEscape(b, x + 2, y0, z1 + 1, h / 4, p);
    }

    private static void shop(StructureBuilder b, int x, int z, int w, int d, int h, int ground,
                             Palette p, String name) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        b.fill(x - 1, ground, z - 1, x1 + 1, ground, z1 + 1, p.foundation());
        b.room(x, y0, z, x1, y0 + h, z1);
        b.walls(x, y0 + 1, z, x1, y0 + h - 1, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 1, z1, p.accent());
        for (int wx = x + 3; wx <= x1 - 3; wx += 5) {
            b.window(wx, y0 + 2, z1, 4, 2, false);
        }
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, y0 + h, 4, w >= d ? 0 : 1);
        placeDoor(b, p, x + w / 2, y0 + 1, z1, Direction.SOUTH);
        b.fill(x + 2, y0 + 3, z1 + 1, x1 - 2, y0 + 3, z1 + 2,
                Materials.wool(DyeColor.RED));
        b.fill(x + 2, y0 + 3, z1 + 2, x1 - 2, y0 + 3, z1 + 2,
                Materials.wool(DyeColor.WHITE));
        signAt(b, x + w / 2, y0 + 4, z1 + 2, Direction.SOUTH, name);
        b.put(x + 2, y0 + 2, z + 2, p.light());
        b.chest(x1 - 2, y0 + 1, z + 2, SiteLoot.COMMON);
    }

    private static void theatre(StructureBuilder b, int x, int z, int w, int d, int h,
                                int ground, Palette p, String name) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        b.fill(x - 2, ground, z - 2, x1 + 2, ground, z1 + 2, p.foundation());
        b.room(x, y0, z, x1, y0 + h, z1);
        b.walls(x, y0 + 1, z, x1, y0 + h - 1, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 1, z1, p.accent());
        b.walls(x, y0 + h - 1, z, x1, y0 + h - 1, z1, p.accent());
        for (int wx = x + 4; wx <= x1 - 4; wx += 6) {
            b.window(wx, y0 + 4, z, 5, 2, true);
            b.window(wx, y0 + 4, z1, 5, 2, true);
        }
        b.pitchedRoof(x - 2, z - 2, x1 + 2, z1 + 2, y0 + h, 6, 0);
        placeDoor(b, p, x + w / 2, y0 + 1, z1, Direction.SOUTH);
        b.fill(x + 6, y0 + 7, z1 + 1, x1 - 6, y0 + 7, z1 + 4, p.roofSlab());
        b.fill(x + 7, y0 + 6, z1 + 4, x1 - 7, y0 + 6, z1 + 4, p.light());
        b.fill(x + 4, y0 + 2, z1 + 1, x + 8, y0 + 4, z1 + 1,
                Materials.stainedGlass(DyeColor.MAGENTA));
        b.fill(x1 - 8, y0 + 2, z1 + 1, x1 - 4, y0 + 4, z1 + 1,
                Materials.stainedGlass(DyeColor.CYAN));
        b.fill(x + 10, y0 + 1, z + 3, x1 - 10, y0 + 1, z + 3, p.accent());
        for (int sz = z + 6; sz <= z1 - 6; sz += 5) {
            b.fill(x + 6, y0 + 1, sz, x1 - 6, y0 + 1, sz, p.accent());
            b.fill(x + 6, y0 + 2, sz, x1 - 6, y0 + 2, sz, Materials.wool(DyeColor.RED));
        }
        signAt(b, x + w / 2, y0 + 8, z1 + 4, Direction.SOUTH, name);
        signAt(b, x + 6, y0 + 2, z1, Direction.SOUTH, "THEATRE");
        b.put(x + 2, y0 + 3, z1 + 1, p.light());
        b.put(x1 - 2, y0 + 3, z1 + 1, p.light());
    }

    private static void bank(StructureBuilder b, int x, int z, int w, int d, int ground,
                             Palette p, String name) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        b.fill(x - 2, ground, z - 2, x1 + 2, ground, z1 + 2, p.foundation());
        b.room(x, y0, z, x1, y0 + 14, z1);
        b.walls(x, y0 + 1, z, x1, y0 + 13, z1, p.roof());
        b.walls(x, y0 + 1, z, x1, y0 + 2, z1, p.foundation());
        b.walls(x, y0 + 13, z, x1, y0 + 13, z1, p.accent());
        for (int wx = x + 6; wx <= x1 - 6; wx += 8) {
            b.window(wx, y0 + 5, z1, 6, 2, true);
            b.window(wx, y0 + 5, z, 6, 2, true);
        }
        // The portico: columns, entablature and a pediment.
        for (int px = x + 5; px <= x1 - 5; px += 8) {
            b.fill(px, y0 + 1, z1 + 2, px, y0 + 10, z1 + 2, p.accent());
            b.put(px, y0 + 11, z1 + 2, p.roof());
        }
        b.fill(x + 2, y0 + 11, z1 + 1, x1 - 2, y0 + 11, z1 + 3, p.roof());
        b.fill(x + 4, y0 + 12, z1 + 2, x1 - 4, y0 + 12, z1 + 2, p.roofSlab());
        b.fill(x + 8, y0 + 13, z1 + 2, x1 - 8, y0 + 13, z1 + 2, p.roof());
        b.fill(x + w / 2 - 3, y0 + 1, z1, x + w / 2 + 3, y0 + 4, z1, air());
        placeDoor(b, p, x + w / 2, y0 + 1, z1, Direction.SOUTH);
        // The vault.
        b.fill(x + 6, y0 + 1, z + 6, x1 - 6, y0 + 6, z1 - 8,
                Blocks.IRON_BLOCK.defaultBlockState());
        b.fill(x + 7, y0 + 1, z + 7, x1 - 7, y0 + 5, z1 - 9, p.roof());
        b.chest(x + 9, y0 + 1, z + 9, SiteLoot.ORDER);
        signAt(b, x + w / 2, y0 + 7, z1, Direction.SOUTH, name);
        signAt(b, x + 4, y0 + 3, z1, Direction.SOUTH, "VAULT");
        b.put(x + 2, y0 + 3, z + 2, p.light());
        b.put(x1 - 2, y0 + 3, z + 2, p.light());
    }

    private static void cityHall(StructureBuilder b, int x, int z, int w, int d, int ground,
                                 Palette p) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        b.fill(x - 2, ground, z - 2, x1 + 2, ground, z1 + 2, p.foundation());
        b.room(x, y0, z, x1, y0 + 12, z1);
        b.walls(x, y0 + 1, z, x1, y0 + 11, z1, p.roof());
        b.walls(x, y0 + 1, z, x1, y0 + 2, z1, p.foundation());
        b.walls(x, y0 + 11, z, x1, y0 + 11, z1, p.accent());
        for (int wx = x + 8; wx <= x1 - 8; wx += 10) {
            b.window(wx, y0 + 4, z1, 5, 2, true);
            b.window(wx, y0 + 4, z, 5, 2, true);
        }
        for (int px = x + 6; px <= x1 - 6; px += 10) {
            b.fill(px, y0 + 1, z1 + 3, px, y0 + 9, z1 + 3, p.accent());
        }
        b.fill(x + 2, y0 + 10, z1 + 1, x1 - 2, y0 + 10, z1 + 4, p.roof());
        b.fill(x + 4, y0 + 11, z1 + 3, x1 - 4, y0 + 11, z1 + 3, p.roofSlab());
        b.fill(x + w / 2 - 4, y0 + 1, z1, x + w / 2 + 4, y0 + 5, z1, air());
        placeDoor(b, p, x + w / 2, y0 + 1, z1, Direction.SOUTH);
        // The cupola and flag.
        b.room(x + w / 2 - 8, y0 + 13, z + d / 2 - 8, x + w / 2 + 8, y0 + 21,
                z + d / 2 + 8);
        b.walls(x + w / 2 - 8, y0 + 14, z + d / 2 - 8, x + w / 2 + 8, y0 + 20,
                z + d / 2 + 8, p.roof());
        b.fill(x + w / 2 - 9, y0 + 21, z + d / 2 - 9, x + w / 2 + 9, y0 + 21,
                z + d / 2 + 9, p.roofSlab());
        b.fill(x + w / 2, y0 + 22, z + d / 2, x + w / 2, y0 + 28, z + d / 2, p.accent());
        b.put(x + w / 2 + 1, y0 + 27, z + d / 2, Materials.wool(DyeColor.BLUE));
        b.put(x + w / 2 + 2, y0 + 27, z + d / 2, Materials.wool(DyeColor.WHITE));
        signAt(b, x + w / 2, y0 + 7, z1, Direction.SOUTH, "CITY HALL");
        b.put(x + 2, y0 + 3, z + 2, p.light());
        b.put(x1 - 2, y0 + 3, z1 - 2, p.light());
    }

    private static void police(StructureBuilder b, int x, int z, int w, int d, int ground,
                               Palette p) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        b.fill(x - 1, ground, z - 1, x1 + 1, ground, z1 + 1, p.foundation());
        b.room(x, y0, z, x1, y0 + 8, z1);
        b.walls(x, y0 + 1, z, x1, y0 + 7, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 2, z1, p.foundation());
        b.walls(x, y0 + 7, z, x1, y0 + 7, z1, p.accent());
        for (int wx = x + 3; wx <= x1 - 3; wx += 5) {
            b.window(wx, y0 + 3, z1, 2, 2, true);
        }
        b.crenellations(x - 1, z - 1, x1 + 1, z1 + 1, y0 + 9);
        placeDoor(b, p, x + w / 2, y0 + 1, z1, Direction.SOUTH);
        signAt(b, x + w / 2, y0 + 4, z1, Direction.SOUTH, "POLICE");
        b.put(x + 2, y0 + 2, z + 2, p.light());
        b.chest(x1 - 2, y0 + 1, z + 2, SiteLoot.COMMON);
    }

    private static void diner(StructureBuilder b, int x, int z, int w, int d, int ground,
                              Palette p) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        b.fill(x - 1, ground, z - 1, x1 + 1, ground, z1 + 1, p.foundation());
        b.room(x, y0, z, x1, y0 + 6, z1);
        b.walls(x, y0 + 1, z, x1, y0 + 5, z1, Materials.concrete(DyeColor.WHITE));
        b.fill(x + 2, y0 + 2, z1, x1 - 2, y0 + 4, z1, Blocks.GLASS_PANE.defaultBlockState());
        b.fill(x - 1, y0 + 6, z - 1, x1 + 1, y0 + 6, z1 + 1, p.roofSlab());
        b.fill(x + 2, y0 + 6, z1 + 1, x1 - 2, y0 + 6, z1 + 3, p.roofSlab());
        b.fill(x + 3, y0 + 5, z1 + 3, x1 - 3, y0 + 5, z1 + 3,
                Materials.stainedGlass(DyeColor.YELLOW));
        b.fill(x + 3, y0 + 1, z + 3, x1 - 3, y0 + 1, z + 4, p.accent());
        placeDoor(b, p, x + w / 2, y0 + 1, z1, Direction.SOUTH);
        signAt(b, x + w / 2, y0 + 7, z1 + 3, Direction.SOUTH, "DINER");
        b.put(x + 2, y0 + 2, z + 2, p.light());
    }

    private static void fireEscape(StructureBuilder b, int x, int y0, int z, int levels,
                                   Palette p) {
        for (int i = 1; i <= levels; i++) {
            int y = y0 + i * 4;
            b.fill(x, y, z, x + 4, y, z, p.rail());
            b.fill(x, y + 1, z, x + 4, y + 1, z, p.frame());
            int lx = i % 2 == 0 ? x : x + 4;
            b.put(lx, y + 2, z, Blocks.LADDER.defaultBlockState());
            b.put(lx, y + 3, z, Blocks.LADDER.defaultBlockState());
        }
    }

    private static void waterTower(StructureBuilder b, int x, int y, int z, Palette p) {
        b.fill(x, y, z, x + 2, y + 1, z + 2, p.accent());
        b.fill(x, y + 2, z, x + 2, y + 4, z + 2, p.wall());
        b.fill(x, y + 5, z, x + 2, y + 5, z + 2, p.roofSlab());
        b.put(x + 1, y + 6, z + 1, p.roof());
    }

    private static void bed(StructureBuilder b, int x, int y, int z, Direction facing) {
        BlockState foot = Blocks.BED.pick(DyeColor.RED).defaultBlockState()
                .setValue(BlockStateProperties.BED_PART, BedPart.FOOT)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        b.put(x, y, z, foot);
        b.put(x + facing.getStepX(), y, z + facing.getStepZ(), foot
                .setValue(BlockStateProperties.BED_PART, BedPart.HEAD));
    }

    private static void placeDoor(StructureBuilder b, Palette p, int x, int y, int z,
                                  Direction facing) {
        b.put(x, y, z, air());
        b.put(x, y + 1, z, air());
        BlockState lower = p.door()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        b.put(x, y, z, lower);
        b.put(x, y + 1, z,
                lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
    }

    private static void signAt(StructureBuilder b, int x, int y, int z, Direction facing,
                               String... lines) {
        b.put(x, y - 1, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        CityPlanSupport.sign(b, x, y, z, facing, lines);
    }

    private static BlockState air() {
        return Blocks.AIR.defaultBlockState();
    }
}
