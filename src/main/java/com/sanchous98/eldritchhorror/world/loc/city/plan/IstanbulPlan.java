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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Authored Constantinople/Istanbul between empires: Hagia Sophia's dome and four minarets, the
 * vaulted lanes and hans of the Grand Bazaar, Galata's tower and Genoese merchants, the Bosphorus
 * quay with its ferries and fish market, the Hippodrome's obelisks and tea gardens, and the sunken
 * cistern under the old city.
 *
 * <p>Everything is written through {@link StructureBuilder}; there is no RNG, no chunk-local read
 * and no shared prefab. Coordinates are relative to the city centre {@code (cx, ground, cz)} and
 * stay inside {@code [-340, 340]}.
 */
public final class IstanbulPlan implements CityPlan {

    /** Authored half-extent (blocks). */
    private static final int HALF = 340;

    @Override
    public String cityId() {
        return "istanbul";
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
        arrivalGate(b, cx, cz, p, surface);
        hagiaSophia(b, cx, cz, ground, p);
        grandBazaar(b, cx, cz, ground, p);
        oldCity(b, cx, cz, ground, p, surface);
        cistern(b, cx, cz, ground, p);
        hippodrome(b, cx, cz, ground, p);
        galata(b, cx, cz, ground, p, surface);
        bosphorus(b, cx, cz, p, surface);
        railStation(b, cx, cz, p, surface);
        dressing(b, cx, cz, ground, p);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.LANDMARK, 0, 0, 320, "south_gate"),
                new CityPoi("guard", CityPoi.Kind.NPC, 0, 0, 310, "guard"),
                new CityPoi("hagia_sophia", CityPoi.Kind.LANDMARK, 0, 0, -200, "great_dome"),
                new CityPoi("great_palace", CityPoi.Kind.LANDMARK, 76, 0, -196, "palace_gate"),
                new CityPoi("bazaar_quest_board", CityPoi.Kind.QUEST_BOARD, -220, 0, 0,
                        "istanbul_smuggled_icon,istanbul_missing_dragoman"),
                new CityPoi("caravanserai", CityPoi.Kind.INN, -240, 0, -80, "innkeeper"),
                new CityPoi("innkeeper", CityPoi.Kind.NPC, -240, 0, -96, "innkeeper"),
                new CityPoi("bazaar_shop", CityPoi.Kind.SHOP, -240, 0, 60, "merchant"),
                new CityPoi("merchant", CityPoi.Kind.NPC, -240, 0, 60, "merchant"),
                new CityPoi("hakim", CityPoi.Kind.HEALER, -200, 0, 120, "hakim"),
                new CityPoi("hakim_keeper", CityPoi.Kind.NPC, -200, 0, 120, "hakim"),
                new CityPoi("icon_dealer", CityPoi.Kind.NPC, -260, 0, -120,
                        "icon_dealer|istanbul_smuggled_icon"),
                new CityPoi("tea_house", CityPoi.Kind.INN, -160, 0, 60, "tea_keeper"),
                new CityPoi("tea_keeper", CityPoi.Kind.NPC, -160, 0, 60, "tea_keeper"),
                new CityPoi("cistern", CityPoi.Kind.SECRET, 0, -9, 40,
                        "istanbul_smuggled_icon,istanbul_missing_dragoman"),
                new CityPoi("cistern_mouth", CityPoi.Kind.SIGN, 19, 0, 40, "cistern_warning"),
                new CityPoi("icon_church", CityPoi.Kind.LANDMARK, -80, 0, 80,
                        "istanbul_smuggled_icon"),
                new CityPoi("priest", CityPoi.Kind.NPC, -80, 0, 90, "priest"),
                new CityPoi("hippodrome", CityPoi.Kind.LANDMARK, 0, 0, 240, "obelisks"),
                new CityPoi("tea_garden", CityPoi.Kind.INN, -80, 0, 230, "tea_keeper"),
                new CityPoi("hippodrome_tea_keeper", CityPoi.Kind.NPC, -80, 0, 230, "tea_keeper"),
                new CityPoi("galata_tower", CityPoi.Kind.LANDMARK, 240, 0, -160, "tower_view"),
                new CityPoi("dragoman", CityPoi.Kind.NPC, 220, 0, -120,
                        "dragoman|istanbul_missing_dragoman"),
                new CityPoi("st_george_church", CityPoi.Kind.LANDMARK, 306, 0, -100,
                        "galata_church"),
                new CityPoi("ferry_quay", CityPoi.Kind.LANDMARK, 240, 0, 220, "ferries"),
                new CityPoi("ferryman", CityPoi.Kind.NPC, 240, 0, 216, "ferryman"),
                new CityPoi("fish_market", CityPoi.Kind.SHOP, 246, 0, 212, "fishmonger"),
                new CityPoi("missing_dragoman", CityPoi.Kind.NPC, -12, -9, 44,
                        "istanbul_missing_dragoman"),
                new CityPoi("rail_station", CityPoi.Kind.STATION, -134, 1, 275,
                        "rail_yard;north_south"),
                new CityPoi("istanbul_port", CityPoi.Kind.PORT, 240, 0, 220, "bosphorus_quay"));
    }

    // ------------------------------------------------------------------ streets

    private static void streets(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // The long avenue from the south gate to Hagia Sophia's courtyard.
        street(b, cx - 7, cz - 100, cx + 6, cz + 320, ground, p.ground(), p);
        // Divan Yolu, the fourteen-wide cross axis through the old city.
        street(b, cx - 320, cz - 27, cx + 320, cz - 14, ground, p.ground(), p);

        // Grand Bazaar lanes: three north-south, six east-west.
        int[] bazaarNs = {-300, -210, -150};
        for (int x : bazaarNs) {
            street(b, cx + x - 2, cz - 240, cx + x + 2, cz + 200, ground, p.ground(), p);
        }
        int[] bazaarEw = {-200, -140, 40, 100, 160};
        for (int z : bazaarEw) {
            street(b, cx - 320, cz + z - 2, cx - 140, cz + z + 2, ground, p.ground(), p);
        }
        // The -80 lane stops at the caravanserai gate and resumes beyond it.
        street(b, cx - 320, cz - 82, cx - 262, cz - 78, ground, p.ground(), p);
        street(b, cx - 218, cz - 82, cx - 140, cz - 78, ground, p.ground(), p);

        // Galata: two north-south lanes, three cross lanes, all in grey stone.
        street(b, cx + 177, cz - 240, cx + 182, cz + 120, ground, p.foundation(), p);
        street(b, cx + 287, cz - 240, cx + 292, cz + 120, ground, p.foundation(), p);
        int[] galataEw = {-200, -60, 40};
        for (int z : galataEw) {
            street(b, cx + 140, cz + z - 2, cx + 320, cz + z + 2, ground, p.foundation(), p);
        }

        // Old-city lanes: one east-west below the church, one north-south past it.
        street(b, cx - 120, cz + 98, cx + 120, cz + 102, ground, p.ground(), p);
        street(b, cx - 62, cz - 60, cx - 58, cz + 160, ground, p.ground(), p);

        // The Hippodrome square; the quay is laid by the harbour pass at its own level.
        paveArea(b, cx - 110, cz + 190, cx + 110, cz + 295, ground, p.foundation(), p);
    }

    private static void street(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
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

    private static void paveArea(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
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

    /** Cuts terrain above the authored level and fills hollows below it. */
    private static void settle(StructureBuilder b, int x, int z, int ground, Palette p) {
        int surface = b.groundY(x, z);
        if (surface > ground) {
            b.fill(x, ground + 1, z, x, surface + 2, z, air());
        } else if (surface < ground) {
            b.fill(x, surface + 1, z, x, ground, z, p.foundation());
        }
    }

    /** Levels a building footprint and caps it with a foundation course. */
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

    // ------------------------------------------------------------------ arrival gate

    private static void arrivalGate(StructureBuilder b, int cx, int cz, Palette p,
                                    IntBinaryOperator surface) {
        int z = cz + 320;
        int base = surface.applyAsInt(cx, z);
        paveArea(b, cx - 20, cz + 306, cx + 20, cz + 334, base, p.foundation(), p);

        for (int sx : new int[]{-14, 8}) {
            b.fill(cx + sx, base + 1, z - 8, cx + sx + 6, base + 14, z + 6, p.wall());
            b.fill(cx + sx + 1, base + 2, z - 7, cx + sx + 5, base + 13, z + 5, air());
            b.crenellations(cx + sx, z - 8, cx + sx + 6, z + 6, base + 15);
            b.put(cx + sx + 3, base + 12, z - 8, p.window());
            b.put(cx + sx + 3, base + 12, z + 6, p.window());
            b.put(cx + sx + 3, base + 15, z, p.light());
        }
        // Lintel and parapet over the fifteen-wide passage.
        b.fill(cx - 8, base + 10, z - 3, cx + 8, base + 12, z + 3, p.wall());
        b.fill(cx - 9, base + 13, z - 4, cx + 9, base + 13, z + 4, p.roofSlab());
        b.crenellations(cx - 9, z - 4, cx + 9, z + 4, base + 14);
        b.put(cx - 7, base + 9, z + 4, p.light());
        b.put(cx + 7, base + 9, z + 4, p.light());

        signAt(b, cx, base + 1, cz + 333, Direction.SOUTH, "ISTANBUL", "SOUTH GATE");
        signAt(b, cx - 18, base + 1, cz + 330, Direction.SOUTH, "HIPPODROME ->");
        signAt(b, cx - 12, base + 1, cz + 330, Direction.SOUTH, "HAGIA SOPHIA ->");
        signAt(b, cx - 6, base + 1, cz + 330, Direction.SOUTH, "GRAND BAZAAR ->");
        signAt(b, cx + 6, base + 1, cz + 330, Direction.SOUTH, "GALATA ->");
        signAt(b, cx + 12, base + 1, cz + 330, Direction.SOUTH, "FERRIES ->");

        // Guardhouse beside the gate.
        pad(b, cx - 27, cz + 303, cx - 15, cz + 315, base, p);
        b.room(cx - 26, base, cz + 304, cx - 16, base + 4, cz + 314,
                new StructureBuilder.Doorway(Side.E, 4));
        placeDoor(b, p, cx - 26, cz + 304, cx - 16, cz + 314, base + 1, Side.E, 4);
        signAt(b, cx - 21, base + 1, cz + 315, Direction.SOUTH, "GUARD");
        lanternPost(b, cx - 16, cz + 320, base, p);
        lanternPost(b, cx + 16, cz + 320, base, p);
    }

    // ------------------------------------------------------------------ Hagia Sophia

    private static void hagiaSophia(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 30;
        int x1 = cx + 30;
        int z0 = cz - 232;
        int z1 = cz - 172;
        int y1 = ground + 26;

        pad(b, x0 - 3, z0 - 3, x1 + 3, z1 + 3, ground, p);
        b.room(x0, ground, z0, x1, y1, z1, new StructureBuilder.Doorway(Side.S, 30));
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        for (int y = ground; y <= y1; y++) {
            b.put(x0, y, z0, p.accent());
            b.put(x1, y, z0, p.accent());
            b.put(x0, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
        b.walls(x0, y1 - 1, z0, x1, y1 - 1, z1, p.accent());
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y1 + 1);

        for (int z = z0 + 5; z <= z1 - 5; z += 6) {
            b.window(x0, ground + 8, z, 8, 1, true);
            b.window(x1, ground + 8, z, 8, 1, true);
        }
        for (int x = x0 + 5; x <= x1 - 5; x += 6) {
            b.window(x, ground + 8, z0, 8, 2, true);
            b.window(x, ground + 8, z1, 8, 2, true);
        }

        // Interior columns and a field of red prayer carpets.
        for (int x = x0 + 8; x <= x1 - 8; x += 10) {
            for (int z = z0 + 8; z <= z1 - 8; z += 10) {
                b.fill(x, ground + 1, z, x, ground + 14, z, p.accent());
                b.put(x, ground + 15, z, p.roofSlab());
            }
        }
        for (int z = cz - 218; z <= cz - 184; z += 8) {
            b.fill(x0 + 5, ground, z, x1 - 5, ground, z + 3, Materials.wool(DyeColor.RED));
        }
        b.put(cx, ground + 12, cz - 174, p.light());
        b.put(cx - 20, ground + 12, cz - 176, p.light());
        b.put(cx + 20, ground + 12, cz - 176, p.light());

        // Drum, great dome and the two cascading semi-domes.
        int drumBase = y1 + 1;
        drum(b, cx, drumBase, cz - 202, 16, 10, p);
        dome(b, cx, drumBase + 10, cz - 202, 16, p);
        dome(b, cx, y1, cz - 226, 8, p);
        dome(b, cx, y1, cz - 178, 8, p);

        // Four slender minarets at the corners of the basilica.
        minaret(b, cx - 38, cz - 240, ground, 62, p);
        minaret(b, cx + 38, cz - 240, ground, 62, p);
        minaret(b, cx - 38, cz - 164, ground, 62, p);
        minaret(b, cx + 38, cz - 164, ground, 62, p);

        // Arcaded courtyard with a fountain, south of the basilica.
        int cy0 = cz - 160;
        int cy1 = cz - 106;
        paveArea(b, cx - 44, cy0, cx + 44, cy1, ground, p.ground(), p);
        paveArea(b, cx - 8, z1 + 1, cx + 8, cy0, ground, p.ground(), p);
        paveArea(b, cx - 8, cy1, cx + 8, cz - 100, ground, p.ground(), p);
        for (int z = cy0 + 4; z <= cy1 - 4; z += 6) {
            column(b, cx - 42, z, ground, p);
            column(b, cx + 42, z, ground, p);
            b.fill(cx - 43, ground + 5, z, cx - 41, ground + 5, z, p.roofSlab());
            b.fill(cx + 41, ground + 5, z, cx + 43, ground + 5, z, p.roofSlab());
        }
        fountain(b, cx, cz - 132, ground, p);
        cypress(b, cx - 36, cz - 150, ground, p);
        cypress(b, cx + 36, cz - 150, ground, p);
        signAt(b, cx, ground + 1, cy1 + 2, Direction.SOUTH, "HAGIA SOPHIA");
        signAt(b, cx - 22, ground + 1, cy1 + 2, Direction.SOUTH, "COURTYARD");

        palace(b, cx, cz, ground, p);
    }

    /** The ruined Great Palace east of the basilica: a gate, a wall and a broken arcade. */
    private static void palace(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 58;
        int x1 = cx + 94;
        int z0 = cz - 228;
        int z1 = cz - 176;
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p);
        b.walls(x0, ground + 1, z0, x1, ground + 8, z1, p.weathered());
        b.crenellations(x0, z0, x1, z1, ground + 9);
        b.fill(x0, ground + 1, cz - 206, x0, ground + 6, cz - 196, air());
        b.fill(x0 - 1, ground + 7, cz - 207, x0 + 1, ground + 7, cz - 195, p.roofSlab());
        b.put(x0, ground + 8, cz - 206, p.light());
        b.put(x0, ground + 8, cz - 196, p.light());
        // The palace court: broken columns and a dry fountain.
        for (int x = x0 + 6; x <= x1 - 6; x += 9) {
            b.fill(x, ground + 1, cz - 212, x, ground + 5, cz - 212, p.accent());
            b.fill(x, ground + 1, cz - 190, x, ground + 3, cz - 190, p.accent());
        }
        b.fill(x0 + 4, ground + 1, z0 + 4, x1 - 4, ground + 1, z0 + 5, p.rubble());
        signAt(b, x0 - 2, ground + 1, cz - 208, Direction.WEST, "PALACE GATE");
        signAt(b, x0 - 2, ground + 1, cz - 194, Direction.WEST, "PALACE GATE");
    }

    private static void drum(StructureBuilder b, int cx, int y0, int cz, int r, int h, Palette p) {
        for (int y = y0; y < y0 + h; y++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 <= r * r && d2 > (r - 1) * (r - 1)) {
                        b.put(cx + dx, y, cz + dz, (y - y0) % 4 == 0 ? p.accent() : p.wall());
                    }
                }
            }
        }
        for (int y = y0 + 2; y <= y0 + h - 2; y++) {
            b.put(cx - r, y, cz, p.window());
            b.put(cx + r, y, cz, p.window());
            b.put(cx, y, cz - r, p.window());
            b.put(cx, y, cz + r, p.window());
        }
    }

    private static void dome(StructureBuilder b, int cx, int y0, int cz, int r, Palette p) {
        for (int dy = 0; dy <= r; dy++) {
            int rr = (int) Math.round(Math.sqrt((double) (r * r - dy * dy)));
            if (rr <= 0) {
                b.put(cx, y0 + dy, cz, p.accent());
                continue;
            }
            for (int dx = -rr; dx <= rr; dx++) {
                for (int dz = -rr; dz <= rr; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 <= rr * rr && d2 > (rr - 1) * (rr - 1)) {
                        b.put(cx + dx, y0 + dy, cz + dz, dy % 5 == 0 ? p.accent() : p.roof());
                    }
                }
            }
        }
        b.put(cx, y0 + r + 1, cz, p.accent());
        b.put(cx, y0 + r + 2, cz, p.light());
    }

    private static void minaret(StructureBuilder b, int x, int z, int ground, int h, Palette p) {
        b.fill(x - 3, ground, z - 3, x + 3, ground + 2, z + 3, p.foundation());
        b.walls(x - 2, ground + 3, z - 2, x + 2, ground + 3, z + 2, p.foundation());
        int shaftTop = ground + h - 12;
        for (int y = ground + 3; y <= shaftTop; y++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (Math.abs(dx) == 1 || Math.abs(dz) == 1) {
                        b.put(x + dx, y, z + dz, y % 6 == 0 ? p.accent() : p.wall());
                    }
                }
            }
        }
        b.fill(x - 3, shaftTop + 1, z - 3, x + 3, shaftTop + 1, z + 3, p.accent());
        for (int i = -3; i <= 3; i++) {
            b.put(x + i, shaftTop + 2, z - 3, p.rail());
            b.put(x + i, shaftTop + 2, z + 3, p.rail());
            b.put(x - 3, shaftTop + 2, z + i, p.rail());
            b.put(x + 3, shaftTop + 2, z + i, p.rail());
        }
        for (int y = shaftTop + 2; y <= ground + h - 4; y++) {
            b.put(x, y, z, p.wall());
            b.put(x - 1, y, z, p.accent());
            b.put(x + 1, y, z, p.accent());
            b.put(x, y, z - 1, p.accent());
            b.put(x, y, z + 1, p.accent());
        }
        int top = ground + h - 4;
        for (int i = 0; i < 4; i++) {
            int rr = 3 - i;
            for (int dx = -rr; dx <= rr; dx++) {
                for (int dz = -rr; dz <= rr; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 <= rr * rr && d2 > (rr - 1) * (rr - 1)) {
                        b.put(x + dx, top + i, z + dz, p.roofSlab());
                    }
                }
            }
        }
        b.put(x, top + 4, z, p.accent());
        b.put(x, top + 5, z, p.light());
    }

    // ------------------------------------------------------------------ Grand Bazaar

    private static void grandBazaar(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        caravanserai(b, cx - 262, cz - 102, cx - 218, cz - 58, ground, p);
        iconDealer(b, cx - 274, cz - 132, cx - 258, cz - 108, ground, p);
        bazaarShop(b, cx - 256, cz + 48, cx - 224, cz + 72, ground, p);
        hakimClinic(b, cx - 206, cz + 108, cx - 182, cz + 132, ground, p);
        teaHouse(b, cx - 176, cz + 48, cx - 156, cz + 72, ground, p);
        bazaarGarden(b, cx - 296, cz + 46, cx - 264, cz + 94, ground, p);
        bazaarGarden(b, cx - 200, cz - 72, cx - 162, cz - 36, ground, p);
        bazaarRuin(b, cx - 296, cz + 108, cx - 264, cz + 154, ground, p);
        carpetMarket(b, cx - 296, cz - 60, cx - 244, cz - 28, ground, p);
        questBoard(b, cx - 236, cz - 14, cx - 214, cz + 14, ground, p);

        int[][] allRuns = {
                {-236, -206}, {-196, -146}, {-136, -86}, {-76, -32},
                {-10, 36}, {46, 96}, {106, 156}, {166, 196}
        };
        int[][] eastRuns = {
                {-236, -206}, {-196, -146}, {-136, -86}, {-10, 36}, {166, 196}
        };
        int[][] westRuns = {
                {-236, -206}, {-196, -146}, {-136, -86}, {-76, -32},
                {-10, 36}, {106, 156}, {166, 196}
        };
        terraceRunsZ(b, cx, cz, -303, -1, 6, ground, p, allRuns);
        terraceRunsZ(b, cx, cz, -297, 1, 6, ground, p, eastRuns);
        terraceRunsZ(b, cx, cz, -153, -1, 6, ground, p, westRuns);
        terraceRunsZ(b, cx, cz, -147, 1, 5, ground, p, allRuns);

        // Vaulted lanes: three north-south runs and the six cross lanes.
        int[] bazaarNs = {-300, -210, -150};
        int[] bazaarEw = {-200, -140, 40, 100, 160};
        for (int x : bazaarNs) {
            vaultLaneZ(b, cx + x, cz - 240, cz + 200, ground, p);
        }
        for (int z : bazaarEw) {
            vaultLaneX(b, cx - 320, cx - 140, cz + z, ground, p);
        }
        vaultLaneX(b, cx - 320, cx - 262, cz - 80, ground, p);
        vaultLaneX(b, cx - 218, cx - 140, cz - 80, ground, p);

        signAt(b, cx - 318, ground + 1, cz - 30, Direction.EAST, "GRAND BAZAAR");
        signAt(b, cx - 220, ground + 1, cz + 202, Direction.NORTH, "GRAND BAZAAR");
    }

    private static void caravanserai(StructureBuilder b, int x0, int z0, int x1, int z1,
                                     int ground, Palette p) {
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 9, z1, p.wall());
        b.crenellations(x0, z0, x1, z1, ground + 10);
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 9, z1 - 1, air());

        int gz = (z0 + z1) / 2;
        b.fill(x1, ground + 1, gz - 3, x1, ground + 6, gz + 3, air());
        b.fill(x1 - 1, ground + 7, gz - 4, x1 + 1, ground + 7, gz + 4, p.roofSlab());
        b.put(x1, ground + 8, gz - 3, p.light());
        b.put(x1, ground + 8, gz + 3, p.light());

        // Dormitory hall along the north side.
        int dx0 = x0 + 3;
        int dx1 = x1 - 3;
        int dz0 = z0 + 3;
        int dz1 = z0 + 15;
        b.room(dx0, ground, dz0, dx1, ground + 6, dz1,
                new StructureBuilder.Doorway(Side.S, (dx1 - dx0) / 2));
        b.fill(dx0, ground, dz0, dx1, ground, dz1, p.foundation());
        placeDoor(b, p, dx0, dz0, dx1, dz1, ground + 1, Side.S, (dx1 - dx0) / 2);
        for (int x = dx0 + 2; x <= dx1 - 2; x += 4) {
            b.fill(x, ground + 1, dz0 + 1, x + 1, ground + 1, dz0 + 2,
                    Materials.wool(DyeColor.RED));
            b.put(x, ground + 1, dz0 + 3, p.roofSlab());
        }
        b.chest(dx1 - 2, ground + 1, dz0 + 2, SiteLoot.COMMON);

        // Arcade around the court, a fountain in the middle and stables at the south.
        for (int x = x0 + 4; x <= x1 - 4; x += 6) {
            column(b, x, z0 + 20, ground, p);
            column(b, x, z1 - 4, ground, p);
        }
        for (int z = z0 + 24; z <= z1 - 8; z += 6) {
            column(b, x0 + 4, z, ground, p);
            column(b, x1 - 4, z, ground, p);
        }
        fountain(b, (x0 + x1) / 2, (z0 + z1) / 2 + 6, ground, p);
        for (int x = x0 + 4; x <= x1 - 4; x += 6) {
            b.put(x, ground + 1, z1 - 6, Blocks.HAY_BLOCK.defaultBlockState());
            b.fill(x, ground + 1, z1 - 8, x, ground + 3, z1 - 8, p.rail());
        }
        signAt(b, x1 + 2, ground + 1, gz - 6, Direction.EAST, "CARAVANSERAI");
        signAt(b, x1 + 2, ground + 1, gz + 6, Direction.EAST, "BEDS & TEA");
    }

    private static void iconDealer(StructureBuilder b, int x0, int z0, int x1, int z1,
                                   int ground, Palette p) {
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x0, ground, z0, x1, ground + 5, z1, new StructureBuilder.Doorway(Side.E, 12));
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        placeDoor(b, p, x0, z0, x1, z1, ground + 1, Side.E, 12);
        // The smuggled icon is kept behind a red curtain in the back room.
        b.fill(x0 + 2, ground + 1, z0 + 4, x0 + 7, ground + 4, z0 + 4, p.wall());
        b.fill(x0 + 3, ground + 1, z0 + 4, x0 + 3, ground + 3, z0 + 4, air());
        b.fill(x0 + 4, ground + 1, z0 + 4, x0 + 6, ground + 3, z0 + 4,
                Materials.wool(DyeColor.RED));
        b.chest(x0 + 4, ground + 1, z0 + 2, SiteLoot.CULT);
        b.put(x0 + 3, ground + 3, z0 + 1, p.light());
        signAt(b, x1 + 1, ground + 1, z0 + 8, Direction.EAST, "SMUGGLED ICON");
        signAt(b, x1 + 1, ground + 1, z0 + 16, Direction.EAST, "PRICE: 40G");
    }

    private static void bazaarShop(StructureBuilder b, int x0, int z0, int x1, int z1,
                                   int ground, Palette p) {
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x0, ground, z0, x1, ground + 7, z1, new StructureBuilder.Doorway(Side.E, 12));
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        placeDoor(b, p, x0, z0, x1, z1, ground + 1, Side.E, 12);
        for (int z = z0 + 3; z <= z1 - 3; z += 5) {
            b.fill(x0 + 2, ground, z, x0 + 6, ground, z + 2,
                    (z & 1) == 0 ? Materials.wool(DyeColor.RED) : Materials.wool(DyeColor.BLUE));
        }
        for (int z = z0 + 3; z <= z1 - 3; z += 6) {
            b.put(x0 + 2, ground + 1, z, Blocks.BARREL.defaultBlockState());
            b.put(x0 + 3, ground + 2, z, p.foundation());
        }
        b.fill(x1 - 4, ground + 1, z0 + 4, x1 - 4, ground + 3, z1 - 4, p.accent());
        dome(b, (x0 + x1) / 2, ground + 8, (z0 + z1) / 2, 7, p);
        b.put((x0 + x1) / 2, ground + 3, (z0 + z1) / 2, p.light());
        signAt(b, x1 + 1, ground + 1, (z0 + z1) / 2 - 4, Direction.EAST, "CARPETS");
        signAt(b, x1 + 1, ground + 1, (z0 + z1) / 2 + 4, Direction.EAST, "SILK & SPICE");
    }

    private static void hakimClinic(StructureBuilder b, int x0, int z0, int x1, int z1,
                                    int ground, Palette p) {
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x0, ground, z0, x1, ground + 6, z1, new StructureBuilder.Doorway(Side.W, 12));
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        placeDoor(b, p, x0, z0, x1, z1, ground + 1, Side.W, 12);
        b.put(x0 + 2, ground + 1, z0 + 2, Blocks.BARREL.defaultBlockState());
        b.put(x0 + 3, ground + 1, z0 + 2, Blocks.CAULDRON.defaultBlockState());
        b.fill(x0 + 2, ground + 1, z1 - 4, x0 + 3, ground + 1, z1 - 2,
                Materials.wool(DyeColor.WHITE));
        b.put(x0 + 2, ground + 1, z1 - 1, p.roofSlab());
        b.fill(x0 + 4, ground + 1, z0 + 4, x0 + 4, ground + 3, z0 + 8, p.accent());
        // Herb garden east of the clinic.
        b.walls(x1 + 2, ground + 1, z0 + 2, x1 + 10, ground + 1, z1 - 2, p.rail());
        b.fill(x1 + 3, ground, z0 + 3, x1 + 9, ground, z1 - 3,
                Blocks.GRASS_BLOCK.defaultBlockState());
        for (int z = z0 + 4; z <= z1 - 4; z += 4) {
            b.put(x1 + 4, ground + 1, z, Blocks.FERN.defaultBlockState());
            b.put(x1 + 7, ground + 1, z, Blocks.DANDELION.defaultBlockState());
        }
        signAt(b, x0 - 2, ground + 1, (z0 + z1) / 2 - 6, Direction.WEST, "HAKIM");
        signAt(b, x0 - 2, ground + 1, (z0 + z1) / 2 + 6, Direction.WEST, "REMEDIES");
    }

    private static void teaHouse(StructureBuilder b, int x0, int z0, int x1, int z1,
                                 int ground, Palette p) {
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x0, ground, z0, x1, ground + 6, z1, new StructureBuilder.Doorway(Side.N, 10));
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        placeDoor(b, p, x0, z0, x1, z1, ground + 1, Side.N, 10);
        b.fill(x0 + 2, ground, z0 + 3, x1 - 2, ground, z0 + 6,
                Materials.wool(DyeColor.LIGHT_BLUE));
        for (int x = x0 + 3; x <= x1 - 3; x += 5) {
            b.put(x, ground + 1, z1 - 5, p.rail());
            b.put(x, ground + 2, z1 - 5, p.roofSlab());
            b.put(x - 1, ground + 1, z1 - 5, p.roofSlab());
            b.put(x + 1, ground + 1, z1 - 5, p.roofSlab());
        }
        b.put(x0 + 2, ground + 1, z0 + 2, Blocks.CAMPFIRE.defaultBlockState());
        b.put(x0 + 3, ground + 1, z0 + 2, Blocks.CAULDRON.defaultBlockState());
        dome(b, (x0 + x1) / 2, ground + 7, (z0 + z1) / 2, 5, p);
        b.put((x0 + x1) / 2, ground + 3, (z0 + z1) / 2, p.light());
        signAt(b, (x0 + x1) / 2 - 6, ground + 1, z0 - 2, Direction.NORTH, "TEA GARDEN");
        signAt(b, (x0 + x1) / 2 + 6, ground + 1, z0 - 2, Direction.NORTH, "SAMOVAR");
    }

    private static void bazaarGarden(StructureBuilder b, int x0, int z0, int x1, int z1,
                                     int ground, Palette p) {
        pad(b, x0, z0, x1, z1, ground, p);
        b.walls(x0, ground + 1, z0, x1, ground + 2, z1, p.weathered());
        b.fill((x0 + x1) / 2 - 2, ground + 1, z1, (x0 + x1) / 2 + 2, ground + 2, z1, air());
        fountain(b, (x0 + x1) / 2, (z0 + z1) / 2, ground, p);
        cypress(b, x0 + 4, z0 + 4, ground, p);
        cypress(b, x1 - 4, z0 + 4, ground, p);
        cypress(b, x0 + 4, z1 - 4, ground, p);
        cypress(b, x1 - 4, z1 - 4, ground, p);
        planeTree(b, x0 + 8, z1 - 10, ground, p);
        stall(b, x1 - 8, z0 + 6, ground, p, DyeColor.YELLOW);
        signAt(b, (x0 + x1) / 2 - 6, ground + 1, z1 + 2, Direction.SOUTH, "BAZAAR GARDEN");
        signAt(b, (x0 + x1) / 2 + 6, ground + 1, z1 + 2, Direction.SOUTH, "GARDEN");
    }

    private static void bazaarRuin(StructureBuilder b, int x0, int z0, int x1, int z1,
                                   int ground, Palette p) {
        pad(b, x0, z0, x1, z1, ground, p);
        int[] xs = {x0 + 4, x0 + 12, x0 + 20, x0 + 26};
        int[] hs = {8, 3, 6, 2};
        for (int i = 0; i < xs.length; i++) {
            b.fill(xs[i], ground + 1, z0 + 6, xs[i], ground + hs[i], z0 + 6, p.accent());
            b.fill(xs[i], ground + 1, z1 - 6, xs[i], ground + Math.max(2, hs[i] - 2), z1 - 6,
                    p.accent());
        }
        b.fill(x0 + 4, ground + 1, z0 + 12, x0 + 10, ground + 1, z0 + 14, p.roofSlab());
        b.fill(x0 + 16, ground + 2, z0 + 20, x0 + 20, ground + 2, z0 + 22, p.roofSlab());
        for (int x = x0; x <= x1; x += 2) {
            b.put(x, ground + 1, z1 - 2, p.rubble());
        }
        b.put(x0 + 6, ground + 1, z0 + 2, Blocks.SHORT_GRASS.defaultBlockState());
        signAt(b, (x0 + x1) / 2, ground + 1, z0 - 2, Direction.NORTH, "OLD BAZAAR");
    }

    private static void carpetMarket(StructureBuilder b, int x0, int z0, int x1, int z1,
                                     int ground, Palette p) {
        pad(b, x0, z0, x1, z1, ground, p);
        for (int z = z0 + 4; z <= z1 - 4; z += 10) {
            for (int x = x0 + 4; x <= x1 - 4; x += 12) {
                stall(b, x, z, ground, p, ((x + z) & 8) == 0 ? DyeColor.RED : DyeColor.BLUE);
                b.fill(x + 1, ground, z + 4, x + 5, ground, z + 6,
                        ((x >> 2) & 1) == 0
                                ? Materials.wool(DyeColor.RED)
                                : Materials.wool(DyeColor.YELLOW));
            }
        }
        signAt(b, (x0 + x1) / 2, ground + 1, z0 - 2, Direction.NORTH, "CARPETS");
    }

    private static void questBoard(StructureBuilder b, int x0, int z0, int x1, int z1,
                                   int ground, Palette p) {
        pad(b, x0, z0, x1, z1, ground, p);
        int mx = (x0 + x1) / 2;
        b.fill(x0 + 2, ground + 1, z0 + 2, x0 + 2, ground + 4, z0 + 2, p.accent());
        b.fill(x1 - 2, ground + 1, z0 + 2, x1 - 2, ground + 4, z0 + 2, p.accent());
        b.fill(x0 + 2, ground + 2, z0 + 2, x1 - 2, ground + 4, z0 + 2, p.frame());
        b.fill(x0 + 2, ground + 5, z0 + 1, x1 - 2, ground + 5, z0 + 3, p.roofSlab());
        signAt(b, mx - 2, ground + 1, z0 + 3, Direction.SOUTH,
                "CITY NOTICES", "ICON QUEST", "ASK THE DEALER", "DRAGOMAN LOST");
        signAt(b, mx, ground + 1, z0 + 3, Direction.SOUTH, "DRAGOMAN LOST", "ASK IN GALATA");
        signAt(b, mx + 2, ground + 1, z0 + 3, Direction.SOUTH, "REWARDS", "AT THE CHURCH");
        lanternPost(b, x0 + 1, z0 + 1, ground, p);
        lanternPost(b, x1 - 1, z1 - 1, ground, p);
        b.put(x0 + 1, ground + 1, z1 - 1, p.rail());
        b.put(x0 + 2, ground + 1, z1 - 1, p.rail());
    }

    // ------------------------------------------------------------------ bazaar shops

    private static void terraceRunsZ(StructureBuilder b, int cx, int cz, int xFront, int dir,
                                     int depth, int ground, Palette p, int[][] runs) {
        for (int[] run : runs) {
            shopTerraceZ(b, cx + xFront, cz + run[0], cz + run[1], depth, dir, ground, p);
        }
    }

    private static void shopTerraceZ(StructureBuilder b, int xFront, int z0, int z1, int depth,
                                     int dir, int ground, Palette p) {
        int xBody = xFront + dir * depth;
        int xMin = Math.min(xFront, xBody);
        int xMax = Math.max(xFront, xBody);
        pad(b, xMin - 1, z0 - 1, xMax + 1, z1 + 1, ground, p);
        b.fill(xMin, ground, z0, xMax, ground, z1, p.foundation());
        b.walls(xMin, ground + 1, z0, xMax, ground + 5, z1, p.wall());
        b.fill(xMin + 1, ground + 1, z0 + 1, xMax - 1, ground + 4, z1 - 1, air());
        for (int z = z0 + 3; z <= z1 - 3; z += 8) {
            b.fill(xFront, ground + 1, z, xFront, ground + 3, z + 1, air());
            b.put(xFront, ground + 4, z, p.accent());
            b.put(xFront, ground + 4, z + 1, p.accent());
            int ax = xFront - dir * 2;
            b.fill(Math.min(xFront, ax), ground + 4, z - 1,
                    Math.max(xFront, ax), ground + 4, z + 2, p.roofSlab());
            b.put(xFront - dir, ground + 3, z, p.light());
            b.put(xFront + dir * 2, ground + 1, z, p.foundation());
            b.put(xFront + dir * 2, ground + 1, z + 1, p.foundation());
            b.put(xFront + dir * 3, ground, z,
                    ((z >> 3) & 1) == 0 ? Materials.wool(DyeColor.RED)
                            : Materials.wool(DyeColor.BLUE));
        }
        for (int z = z0 + 7; z <= z1 - 7; z += 8) {
            b.put(xFront, ground + 2, z, p.window());
            b.put(xFront, ground + 3, z, p.window());
            b.put(xFront, ground + 2, z + 1, p.window());
            b.put(xFront, ground + 3, z + 1, p.window());
        }
        b.pitchedRoof(xMin - 1, z0 - 1, xMax + 1, z1 + 1, ground + 6, 2, 1);
        b.put(xBody - dir, ground + 3, z0 + 2, p.light());
        b.put(xBody - dir, ground + 3, z1 - 2, p.light());
    }

    private static void shopTerraceX(StructureBuilder b, int x0, int x1, int zFront, int depth,
                                     int dir, Palette p, IntBinaryOperator surface) {
        int base = surface.applyAsInt((x0 + x1) / 2, zFront);
        int zBody = zFront + dir * depth;
        int zMin = Math.min(zFront, zBody);
        int zMax = Math.max(zFront, zBody);
        pad(b, x0 - 1, zMin - 1, x1 + 1, zMax + 1, base, p);
        b.fill(x0, base, zMin, x1, base, zMax, p.foundation());
        b.walls(x0, base + 1, zMin, x1, base + 5, zMax, p.wall());
        b.fill(x0 + 1, base + 1, zMin + 1, x1 - 1, base + 4, zMax - 1, air());
        for (int x = x0 + 3; x <= x1 - 3; x += 8) {
            b.fill(x, base + 1, zFront, x + 1, base + 3, zFront, air());
            b.put(x, base + 4, zFront, p.accent());
            b.put(x + 1, base + 4, zFront, p.accent());
            int az = zFront - dir * 2;
            b.fill(x - 1, base + 4, Math.min(zFront, az),
                    x + 2, base + 4, Math.max(zFront, az), p.roofSlab());
            b.put(x, base + 3, zFront - dir, p.light());
            b.put(x, base + 1, zFront + dir * 2, p.foundation());
            b.put(x + 1, base + 1, zFront + dir * 2, p.foundation());
            b.put(x, base, zFront + dir * 3,
                    ((x >> 3) & 1) == 0 ? Materials.wool(DyeColor.RED)
                            : Materials.wool(DyeColor.BLUE));
        }
        for (int x = x0 + 7; x <= x1 - 7; x += 8) {
            b.window(x, base + 2, zFront, 2, 2, false);
        }
        b.pitchedRoof(x0 - 1, zMin - 1, x1 + 1, zMax + 1, base + 6, 2, 0);
        b.put(x0 + 2, base + 3, zBody - dir, p.light());
        b.put(x1 - 2, base + 3, zBody - dir, p.light());
    }

    private static void vaultLaneZ(StructureBuilder b, int xc, int z0, int z1, int ground,
                                   Palette p) {
        for (int z = z0; z <= z1; z++) {
            b.put(xc - 2, ground + 6, z, p.roofSlab());
            b.put(xc + 2, ground + 6, z, p.roofSlab());
            b.put(xc - 1, ground + 7, z, p.roofSlab());
            b.put(xc, ground + 7, z, p.roof());
            b.put(xc + 1, ground + 7, z, p.roofSlab());
            if ((z & 15) == 0) {
                b.put(xc, ground + 6, z, p.light());
            }
        }
        for (int z = z0 + 4; z <= z1; z += 8) {
            for (int y = ground + 1; y <= ground + 5; y++) {
                b.put(xc - 2, y, z, p.wall());
                b.put(xc + 2, y, z, p.wall());
            }
            b.put(xc - 2, ground + 5, z, p.accent());
            b.put(xc + 2, ground + 5, z, p.accent());
            b.fill(xc - 2, ground + 6, z, xc + 2, ground + 6, z, p.accent());
        }
    }

    private static void vaultLaneX(StructureBuilder b, int x0, int x1, int zc, int ground,
                                   Palette p) {
        for (int x = x0; x <= x1; x++) {
            b.put(x, ground + 6, zc - 2, p.roofSlab());
            b.put(x, ground + 6, zc + 2, p.roofSlab());
            b.put(x, ground + 7, zc - 1, p.roofSlab());
            b.put(x, ground + 7, zc, p.roof());
            b.put(x, ground + 7, zc + 1, p.roofSlab());
            if ((x & 15) == 0) {
                b.put(x, ground + 6, zc, p.light());
            }
        }
        for (int x = x0 + 4; x <= x1; x += 8) {
            for (int y = ground + 1; y <= ground + 5; y++) {
                b.put(x, y, zc - 2, p.wall());
                b.put(x, y, zc + 2, p.wall());
            }
            b.put(x, ground + 5, zc - 2, p.accent());
            b.put(x, ground + 5, zc + 2, p.accent());
            b.fill(x, ground + 6, zc - 2, x, ground + 6, zc + 2, p.accent());
        }
    }

    // ------------------------------------------------------------------ old city

    private static void oldCity(StructureBuilder b, int cx, int cz, int ground, Palette p,
                                IntBinaryOperator surface) {
        iconChurch(b, cx - 96, cz + 64, cx - 64, cz + 96, ground, p);
        byzantineRuin(b, cx - 44, cz + 108, cx - 16, cz + 134, ground, p);
        oldGarden(b, cx + 36, cz + 104, cx + 66, cz + 126, ground, p);

        house(b, cx - 115, cz + 20, 14, 12, 6, p, Side.S, surface);
        house(b, cx - 115, cz + 55, 12, 14, 6, p, Side.E, surface);
        house(b, cx - 40, cz - 50, 14, 14, 6, p, Side.E, surface);
        house(b, cx + 20, cz - 40, 14, 12, 6, p, Side.W, surface);
        house(b, cx + 30, cz + 60, 16, 12, 6, p, Side.S, surface);
        house(b, cx + 70, cz + 20, 14, 14, 6, p, Side.W, surface);
        house(b, cx + 90, cz + 60, 14, 12, 6, p, Side.S, surface);
        house(b, cx + 90, cz + 120, 14, 12, 6, p, Side.W, surface);
        house(b, cx + 30, cz + 130, 16, 12, 6, p, Side.N, surface);
        house(b, cx - 100, cz + 120, 16, 14, 6, p, Side.E, surface);
        house(b, cx - 40, cz + 138, 14, 12, 6, p, Side.N, surface);
        house(b, cx + 60, cz + 140, 12, 12, 6, p, Side.W, surface);

        // A small paved square with a fountain, east of the church.
        pad(b, cx - 32, cz + 60, cx - 8, cz + 84, ground, p);
        fountain(b, cx - 20, cz + 72, ground, p);
        b.put(cx - 30, ground + 1, cz + 62, p.rail());
        b.put(cx - 28, ground + 1, cz + 62, p.roofSlab());

        signAt(b, cx - 20, ground + 1, cz + 86, Direction.SOUTH, "OLD CITY", "CISTERN");
        signAt(b, cx - 30, ground + 1, cz + 106, Direction.SOUTH, "BYZANTINE", "RUINS");
    }

    private static void iconChurch(StructureBuilder b, int x0, int z0, int x1, int z1,
                                   int ground, Palette p) {
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x0, ground, z0, x1, ground + 9, z1,
                new StructureBuilder.Doorway(Side.N, (x1 - x0) / 2));
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 8, z0, x1, ground + 8, z1, p.accent());
        for (int z = z0 + 5; z <= z1 - 5; z += 6) {
            b.window(x0, ground + 4, z, 3, 1, true);
            b.window(x1, ground + 4, z, 3, 1, true);
        }
        // West apse with its own half-dome.
        b.fill(x0 - 5, ground, z0 + 10, x0 - 1, ground + 6, z1 - 10, p.wall());
        b.fill(x0 - 4, ground + 1, z0 + 11, x0 - 1, ground + 5, z1 - 11, air());
        b.fill(x0 - 5, ground + 7, z0 + 10, x0 - 1, ground + 7, z1 - 10, p.roofSlab());
        b.fill(x0, ground + 1, z0 + 11, x0, ground + 5, z1 - 11, air());
        dome(b, x0 - 3, ground + 8, (z0 + z1) / 2, 5, p);

        drum(b, (x0 + x1) / 2, ground + 10, (z0 + z1) / 2, 7, 4, p);
        dome(b, (x0 + x1) / 2, ground + 14, (z0 + z1) / 2, 7, p);

        placeDoor(b, p, x0, z0, x1, z1, ground + 1, Side.N, (x1 - x0) / 2);
        b.put(x0 + 2, ground + 3, z0 + 2, p.light());
        b.put(x1 - 2, ground + 3, z1 - 2, p.light());
        b.chest(x1 - 3, ground + 1, z1 - 3, SiteLoot.ORDER);

        // Walled forecourt north of the church.
        pad(b, x0 + 4, z0 - 16, x1 - 4, z0 - 2, ground, p);
        b.walls(x0 + 4, ground + 1, z0 - 16, x1 - 4, ground + 2, z0 - 2, p.rail());
        b.fill((x0 + x1) / 2 - 2, ground + 1, z0 - 2,
                (x0 + x1) / 2 + 2, ground + 2, z0 - 2, air());
        fountain(b, (x0 + x1) / 2, z0 - 9, ground, p);
        cypress(b, x0 + 7, z0 - 13, ground, p);
        cypress(b, x1 - 7, z0 - 13, ground, p);
        signAt(b, (x0 + x1) / 2, ground + 1, z0 - 18, Direction.NORTH,
                "HAGIA EIRENE", "ICON CHURCH");
    }

    private static void byzantineRuin(StructureBuilder b, int x0, int z0, int x1, int z1,
                                      int ground, Palette p) {
        pad(b, x0, z0, x1, z1, ground, p);
        int[] xs = {x0 + 3, x0 + 11, x0 + 19, x0 + 25};
        int[] zs = {z0 + 4, z0 + 12, z0 + 20};
        int[] hs = {7, 3, 5, 2};
        for (int i = 0; i < xs.length; i++) {
            for (int j = 0; j < zs.length; j++) {
                if (((i + j) & 1) == 1) {
                    continue;
                }
                int h = hs[(i + j) % hs.length];
                b.fill(xs[i], ground + 1, zs[j], xs[i], ground + h, zs[j], p.accent());
                b.put(xs[i], ground + h + 1, zs[j], p.roofSlab());
            }
        }
        b.fill(x0 + 5, ground + 1, z1 - 6, x0 + 9, ground + 1, z1 - 6, p.accent());
        b.fill(x0 + 14, ground + 1, z1 - 9, x0 + 16, ground + 1, z1 - 9, p.accent());
        b.fill(x0 + 18, ground + 1, z0 + 4, x0 + 18, ground + 8, z0 + 4, p.accent());
        b.fill(x0 + 24, ground + 1, z0 + 4, x0 + 24, ground + 8, z0 + 4, p.accent());
        b.fill(x0 + 18, ground + 9, z0 + 4, x0 + 24, ground + 9, z0 + 4, p.accent());
        for (int x = x0; x <= x1; x += 3) {
            b.put(x, ground + 1, z1 - 2, p.rubble());
        }
        b.put(x0 + 2, ground + 1, z0 + 2, Blocks.SHORT_GRASS.defaultBlockState());
        b.put(x1 - 2, ground + 1, z1 - 2, Blocks.SHORT_GRASS.defaultBlockState());
        signAt(b, (x0 + x1) / 2, ground + 1, z0 - 2, Direction.NORTH, "BYZANTINE", "RUINS");
    }

    private static void oldGarden(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  int ground, Palette p) {
        pad(b, x0, z0, x1, z1, ground, p);
        b.walls(x0, ground + 1, z0, x1, ground + 2, z1, p.weathered());
        b.fill(x0, ground + 1, z0 + 10, x0, ground + 2, z0 + 14, air());
        fountain(b, (x0 + x1) / 2, (z0 + z1) / 2, ground, p);
        cypress(b, x0 + 4, z0 + 4, ground, p);
        cypress(b, x1 - 4, z0 + 4, ground, p);
        cypress(b, x0 + 4, z1 - 4, ground, p);
        cypress(b, x1 - 4, z1 - 4, ground, p);
        for (int x = x0 + 10; x <= x1 - 10; x += 6) {
            b.put(x, ground, z0 + 4, Blocks.GRASS_BLOCK.defaultBlockState());
            b.put(x, ground + 1, z0 + 4, Blocks.POPPY.defaultBlockState());
            b.put(x, ground, z1 - 4, Blocks.GRASS_BLOCK.defaultBlockState());
            b.put(x, ground + 1, z1 - 4, Blocks.DANDELION.defaultBlockState());
        }
        signAt(b, x0 - 2, ground + 1, z0 + 8, Direction.WEST, "GARDEN");
        signAt(b, x0 - 2, ground + 1, z0 + 18, Direction.WEST, "OLD CITY");
    }

    // ------------------------------------------------------------------ cistern

    private static void cistern(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int floor = ground - 10;
        int ceil = ground - 5;
        int x0 = cx - 28;
        int x1 = cx + 8;
        int z0 = cz + 20;
        int z1 = cz + 60;

        b.fill(x0 - 1, floor - 1, z0 - 1, x1 + 1, floor - 1, z1 + 1, p.foundation());
        b.walls(x0, floor, z0, x1, ceil, z1, Blocks.STONE_BRICKS.defaultBlockState());
        b.fill(x0, ceil, z0, x1, ceil, z1, Blocks.STONE_BRICKS.defaultBlockState());
        b.fill(x0 + 1, floor, z0 + 1, x1 - 1, ceil - 1, z1 - 1, air());
        b.fill(x0 + 1, floor, z0 + 1, x1 - 1, floor, z1 - 1, p.foundation());

        b.fill(x0 + 1, floor, z0 + 6, x1 - 1, floor, z0 + 7,
                Blocks.WATER.defaultBlockState());
        b.fill(x0 + 1, floor, z0 + 33, x1 - 1, floor, z0 + 34,
                Blocks.WATER.defaultBlockState());

        int[] colX = {-24, -16, -8, 0};
        int[] colZ = {24, 32, 40, 48, 56};
        for (int dx : colX) {
            for (int dz : colZ) {
                if (dx == 0 && dz == 40) {
                    continue;
                }
                int x = cx + dx;
                int z = cz + dz;
                b.fill(x, floor + 1, z, x, ceil - 1, z, p.accent());
                b.put(x, ceil - 1, z, p.roofSlab());
            }
        }
        for (int dz : colZ) {
            b.fill(cx - 24, ceil - 1, cz + dz, cx, ceil - 1, cz + dz, p.accent());
        }

        // Descending stair from the street, in an open trench east of the avenue.
        for (int i = 0; i <= 9; i++) {
            int sx = cx + 19 - i;
            int sy = ground - i;
            b.fill(sx, sy - 1, cz + 37, sx, sy - 1, cz + 43, p.foundation());
            b.fill(sx, sy, cz + 37, sx, ground + 3, cz + 43, air());
        }
        b.fill(x1, floor + 1, cz + 37, x1, floor + 3, cz + 43, air());
        b.fill(x1 - 1, floor, cz + 37, x1, floor, cz + 43, p.foundation());
        b.fill(cx + 9, floor + 1, cz + 37, cx + 9, ground + 3, cz + 43, air());
        b.fill(cx + 9, floor, cz + 37, cx + 9, floor, cz + 43, p.foundation());
        b.walls(cx + 8, ground + 1, cz + 35, cx + 20, ground + 2, cz + 45, p.foundation());
        b.fill(cx + 9, ground + 1, cz + 37, cx + 19, ground + 2, cz + 43, air());

        pad(b, cx + 20, cz + 33, cx + 28, cz + 47, ground, p);
        b.room(cx + 20, ground, cz + 34, cx + 27, ground + 4, cz + 46,
                new StructureBuilder.Doorway(Side.S, 3));
        placeDoor(b, p, cx + 20, cz + 34, cx + 27, cz + 46, ground + 1, Side.S, 3);
        b.fill(cx + 20, ground + 1, cz + 37, cx + 20, ground + 3, cz + 43, air());
        paveArea(b, cx - 7, cz + 47, cx + 27, cz + 51, ground, p.ground(), p);

        b.chest(cx - 20, floor + 1, cz + 44, SiteLoot.CULT);
        b.chest(cx, floor + 1, cz + 44, SiteLoot.RELIC);
        signAt(b, cx - 4, floor + 1, cz + 34, Direction.SOUTH,
                "THE SUNKEN", "CISTERN", "THE ICON WAS", "HIDDEN HERE");
        b.put(cx - 12, ceil - 1, cz + 28, p.light());
        b.put(cx + 4, ceil - 1, cz + 52, p.light());
        b.put(cx - 26, ceil - 1, cz + 22, Palette.cobweb());
        b.put(cx + 6, ceil - 1, cz + 22, Palette.cobweb());
        b.put(cx - 26, ceil - 1, cz + 58, Palette.cobweb());
        b.put(cx + 6, ceil - 1, cz + 58, Palette.cobweb());

        signAt(b, cx + 23, ground + 1, cz + 52, Direction.SOUTH,
                "CISTERN STAIRS", "MIND THE WATER");
    }

    // ------------------------------------------------------------------ Hippodrome

    private static void hippodrome(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        triumphalArch(b, cx, cz + 192, ground, p);
        obelisk(b, cx - 45, cz + 225, ground, 22, p);
        obelisk(b, cx + 45, cz + 225, ground, 22, p);
        serpentColumn(b, cx - 20, cz + 265, ground, p);
        b.monument(cx + 70, cz + 285, ground + 1);
        teaGarden(b, cx - 100, cz + 205, cx - 62, cz + 255, ground, p);
        teaGarden(b, cx + 62, cz + 205, cx + 100, cz + 255, ground, p);
        cypress(b, cx - 70, cz + 225, ground, p);
        cypress(b, cx + 70, cz + 225, ground, p);
        signAt(b, cx + 18, ground + 1, cz + 188, Direction.NORTH, "HIPPODROME", "OBELISKS");
        signAt(b, cx - 45, ground + 1, cz + 242, Direction.SOUTH, "OBELISKS");
    }

    private static void triumphalArch(StructureBuilder b, int cx, int z, int ground, Palette p) {
        pad(b, cx - 16, z - 6, cx + 16, z + 6, ground, p);
        b.fill(cx - 14, ground + 1, z - 4, cx - 8, ground + 10, z + 4, p.wall());
        b.fill(cx + 8, ground + 1, z - 4, cx + 14, ground + 10, z + 4, p.wall());
        b.fill(cx - 8, ground + 8, z - 3, cx + 8, ground + 10, z + 3, p.wall());
        b.fill(cx - 7, ground + 1, z - 2, cx + 7, ground + 7, z + 2, air());
        b.fill(cx - 15, ground + 11, z - 5, cx + 15, ground + 11, z + 5, p.roofSlab());
        b.crenellations(cx - 15, z - 5, cx + 15, z + 5, ground + 12);
        for (int sx : new int[]{-14, 9}) {
            b.fill(cx + sx, ground + 1, z - 4, cx + sx + 4, ground + 7, z - 4, p.accent());
            b.fill(cx + sx, ground + 1, z + 4, cx + sx + 4, ground + 7, z + 4, p.accent());
        }
        b.put(cx - 10, ground + 9, z - 4, p.light());
        b.put(cx + 10, ground + 9, z - 4, p.light());
    }

    private static void obelisk(StructureBuilder b, int x, int z, int ground, int h, Palette p) {
        BlockState stone = Blocks.CUT_SANDSTONE.defaultBlockState();
        BlockState band = Blocks.SANDSTONE.defaultBlockState();
        b.fill(x - 3, ground, z - 3, x + 3, ground + 1, z + 3, p.foundation());
        b.fill(x - 2, ground + 2, z - 2, x + 2, ground + 2, z + 2, p.foundation());
        b.fill(x - 1, ground + 3, z - 1, x + 1, ground + h, z + 1, stone);
        for (int y = ground + 4; y <= ground + h; y += 3) {
            b.put(x - 1, y, z - 1, band);
            b.put(x + 1, y, z + 1, band);
        }
        b.fill(x, ground + h + 1, z, x, ground + h + 3, z, stone);
        b.put(x, ground + h + 4, z, Blocks.GOLD_BLOCK.defaultBlockState());
    }

    private static void serpentColumn(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 2, ground, z - 2, x + 2, ground + 1, z + 2, p.foundation());
        BlockState bronze = Blocks.COPPER_BLOCK.weathering().oxidized().defaultBlockState();
        b.fill(x - 1, ground + 2, z, x - 1, ground + 9, z, bronze);
        b.fill(x + 1, ground + 2, z, x + 1, ground + 8, z, bronze);
        b.fill(x, ground + 2, z + 1, x, ground + 10, z + 1, bronze);
        b.fill(x - 1, ground + 10, z - 1, x + 1, ground + 10, z + 1, p.accent());
        b.put(x, ground + 11, z, Blocks.GOLD_BLOCK.defaultBlockState());
        b.put(x, ground + 12, z, p.light());
    }

    private static void teaGarden(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  int ground, Palette p) {
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p);
        b.walls(x0, ground + 1, z0, x1, ground + 2, z1, p.rail());
        int gx = (x0 + x1) / 2;
        b.fill(gx - 2, ground + 1, z1, gx + 2, ground + 2, z1, air());
        cypress(b, x0 + 4, z0 + 5, ground, p);
        cypress(b, x1 - 4, z0 + 5, ground, p);
        cypress(b, x0 + 4, z1 - 5, ground, p);
        cypress(b, x1 - 4, z1 - 5, ground, p);
        for (int x = x0 + 8; x <= x1 - 8; x += 8) {
            b.put(x, ground + 1, z0 + 10, p.rail());
            b.put(x, ground + 2, z0 + 10, p.roofSlab());
            b.put(x - 1, ground + 1, z0 + 10, p.roofSlab());
            b.put(x + 1, ground + 1, z0 + 10, p.roofSlab());
        }
        stall(b, gx - 1, z0 + 3, ground, p, DyeColor.YELLOW);
        signAt(b, gx - 6, ground + 1, z1 + 2, Direction.SOUTH, "TEA GARDEN");
        signAt(b, gx + 6, ground + 1, z1 + 2, Direction.SOUTH, "SAMOVAR");
        lanternPost(b, x0 + 2, z1 - 2, ground, p);
        lanternPost(b, x1 - 2, z1 - 2, ground, p);
    }

    // ------------------------------------------------------------------ Galata

    private static void galata(StructureBuilder b, int cx, int cz, int ground, Palette p,
                               IntBinaryOperator surface) {
        galataTower(b, cx + 240, cz - 160, p, surface);
        stGeorgeChurch(b, cx + 296, cz - 116, cx + 318, cz - 84, p, surface);
        dragomanHouse(b, cx + 208, cz - 132, cx + 232, cz - 108, p, surface);

        house(b, cx + 190, cz - 190, 14, 16, 7, p, Side.S, surface);
        house(b, cx + 250, cz - 196, 16, 14, 7, p, Side.W, surface);
        house(b, cx + 192, cz - 140, 12, 14, 7, p, Side.E, surface);
        house(b, cx + 250, cz - 130, 14, 12, 7, p, Side.S, surface);
        house(b, cx + 200, cz - 100, 12, 12, 7, p, Side.W, surface);
        house(b, cx + 260, cz - 80, 14, 14, 7, p, Side.W, surface);
        house(b, cx + 150, cz - 180, 12, 12, 7, p, Side.E, surface);
        house(b, cx + 300, cz - 180, 12, 14, 7, p, Side.W, surface);
        house(b, cx + 150, cz + 0, 14, 16, 7, p, Side.E, surface);
        house(b, cx + 200, cz + 0, 16, 14, 7, p, Side.W, surface);
        house(b, cx + 250, cz + 0, 14, 16, 7, p, Side.E, surface);
        house(b, cx + 300, cz + 0, 14, 16, 7, p, Side.W, surface);

        shopTerraceX(b, cx + 186, cx + 284, cz - 57, 6, 1, p, surface);
        shopTerraceX(b, cx + 186, cx + 246, cz - 63, 6, -1, p, surface);
        shopTerraceX(b, cx + 186, cx + 284, cz + 37, 6, -1, p, surface);
        shopTerraceX(b, cx + 186, cx + 284, cz + 43, 6, 1, p, surface);

        warehouse(b, cx + 145, cz + 80, cx + 175, cz + 110, p, surface);
        warehouse(b, cx + 200, cz + 80, cx + 224, cz + 110, p, surface);
        warehouse(b, cx + 260, cz + 80, cx + 284, cz + 110, p, surface);

        pad(b, cx + 145, cz - 111, cx + 147, cz - 69, ground, p);
        b.walls(cx + 146, ground + 1, cz - 110, cx + 146, ground + 5, cz - 70, p.weathered());
        b.crenellations(cx + 146, cz - 110, cx + 146, cz - 70, ground + 6);
        signAt(b, cx + 146, ground + 1, cz - 112, Direction.WEST, "GENOESE WALL");
        signAt(b, cx + 140, ground + 1, cz - 30, Direction.WEST, "GALATA");
    }

    private static void galataTower(StructureBuilder b, int x, int z, Palette p,
                                    IntBinaryOperator surface) {
        int base = surface.applyAsInt(x, z);
        b.fill(x - 10, base, z - 10, x + 10, base + 1, z + 10, p.foundation());
        for (int y = base + 2; y <= base + 30; y++) {
            for (int dx = -9; dx <= 9; dx++) {
                for (int dz = -9; dz <= 9; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 <= 81 && d2 > 64) {
                        b.put(x + dx, y, z + dz, y % 6 == 0 ? p.accent() : p.wall());
                    }
                }
            }
        }
        b.fill(x - 2, base + 2, z + 8, x + 2, base + 5, z + 9, air());
        for (int y = base + 8; y <= base + 26; y += 6) {
            b.put(x, y, z - 9, p.window());
            b.put(x, y, z + 9, p.window());
            b.put(x - 9, y, z, p.window());
            b.put(x + 9, y, z, p.window());
        }
        for (int dx = -11; dx <= 11; dx++) {
            for (int dz = -11; dz <= 11; dz++) {
                if (dx * dx + dz * dz <= 121) {
                    b.put(x + dx, base + 30, z + dz, p.roofSlab());
                }
            }
        }
        for (int dx = -11; dx <= 11; dx++) {
            for (int dz = -11; dz <= 11; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 121 && d2 > 100) {
                    b.put(x + dx, base + 31, z + dz, p.rail());
                }
            }
        }
        for (int y = base + 31; y <= base + 38; y++) {
            for (int dx = -7; dx <= 7; dx++) {
                for (int dz = -7; dz <= 7; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 <= 49 && d2 > 36) {
                        b.put(x + dx, y, z + dz, y % 4 == 0 ? p.accent() : p.wall());
                    }
                }
            }
        }
        for (int i = 0; i <= 7; i++) {
            int rr = 7 - i;
            for (int dx = -rr; dx <= rr; dx++) {
                for (int dz = -rr; dz <= rr; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 <= rr * rr && (d2 > (rr - 1) * (rr - 1) || i == 7)) {
                        b.put(x + dx, base + 39 + i, z + dz, p.roof());
                    }
                }
            }
        }
        b.put(x, base + 47, z, p.accent());
        b.put(x, base + 48, z, p.light());

        b.chest(x + 4, base + 2, z - 4, SiteLoot.ORDER);
        signAt(b, x - 4, base + 2, z - 2, Direction.SOUTH, "LEDGER ROOM", "KEEP QUIET");

        b.walls(x - 14, base + 1, z - 14, x + 14, base + 3, z + 14, p.weathered());
        b.fill(x - 3, base + 1, z + 14, x + 3, base + 3, z + 14, air());
        signAt(b, x - 8, base + 1, z + 16, Direction.SOUTH, "GALATA TOWER");
        signAt(b, x + 8, base + 1, z + 16, Direction.SOUTH, "CLIMB FOR VIEW");
        b.put(x - 14, base + 4, z - 14, p.light());
        b.put(x + 14, base + 4, z + 14, p.light());
    }

    private static void stGeorgeChurch(StructureBuilder b, int x0, int z0, int x1, int z1,
                                       Palette p, IntBinaryOperator surface) {
        int base = surface.applyAsInt((x0 + x1) / 2, (z0 + z1) / 2);
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, base, p);
        b.room(x0, base, z0, x1, base + 8, z1,
                new StructureBuilder.Doorway(Side.W, (z1 - z0) / 2));
        b.fill(x0, base, z0, x1, base, z1, p.foundation());
        placeDoor(b, p, x0, z0, x1, z1, base + 1, Side.W, (z1 - z0) / 2);
        for (int z = z0 + 4; z <= z1 - 4; z += 5) {
            b.window(x1, base + 3, z, 3, 1, true);
        }
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, base + 9, 4, 1);
        // Bell gable on the west facade.
        b.fill(x0, base + 9, z0 + 12, x0, base + 13, z0 + 20, p.accent());
        b.put(x0, base + 14, z0 + 16, Blocks.BELL.defaultBlockState());
        b.put(x0, base + 15, z0 + 16, Blocks.GOLD_BLOCK.defaultBlockState());
        cypress(b, x1 + 4, z0 + 3, base, p);
        cypress(b, x1 + 4, z1 - 3, base, p);
        signAt(b, x0 - 2, base + 1, (z0 + z1) / 2 - 8, Direction.WEST, "ST GEORGE");
        signAt(b, x0 - 2, base + 1, (z0 + z1) / 2 + 8, Direction.WEST, "GENOESE");
        b.put(x0 + 2, base + 3, z0 + 2, p.light());
        b.put(x1 - 2, base + 3, z1 - 2, p.light());
    }

    private static void dragomanHouse(StructureBuilder b, int x0, int z0, int x1, int z1,
                                      Palette p, IntBinaryOperator surface) {
        int base = surface.applyAsInt((x0 + x1) / 2, (z0 + z1) / 2);
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, base, p);
        b.room(x0, base, z0, x1, base + 6, z1, new StructureBuilder.Doorway(Side.S, 12));
        b.fill(x0, base, z0, x1, base, z1, p.foundation());
        placeDoor(b, p, x0, z0, x1, z1, base + 1, Side.S, 12);
        b.put(x0 + 3, base + 1, z0 + 4, Blocks.LECTERN.defaultBlockState());
        b.put(x0 + 4, base + 1, z0 + 4, p.rail());
        b.chest(x1 - 3, base + 1, z0 + 3, SiteLoot.ORDER);
        b.put(x0 + 2, base + 3, z0 + 2, p.light());
        b.fill(x0 + 2, base + 1, z1 - 3, x0 + 4, base + 1, z1 - 3, p.accent());
        signAt(b, x0 + 4, base + 1, z1 + 2, Direction.SOUTH, "DRAGOMAN");
        signAt(b, x1 - 4, base + 1, z1 + 2, Direction.SOUTH, "ASK AT TOWER");
    }

    private static void warehouse(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  Palette p, IntBinaryOperator surface) {
        int base = surface.applyAsInt((x0 + x1) / 2, (z0 + z1) / 2);
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, base, p);
        b.room(x0, base, z0, x1, base + 7, z1,
                new StructureBuilder.Doorway(Side.S, (x1 - x0) / 2));
        b.fill(x0, base, z0, x1, base, z1, p.foundation());
        placeDoor(b, p, x0, z0, x1, z1, base + 1, Side.S, (x1 - x0) / 2);
        for (int x = x0 + 3; x <= x1 - 3; x += 5) {
            b.put(x, base + 1, z1 - 3, Blocks.BARREL.defaultBlockState());
            b.put(x, base + 2, z1 - 3, Blocks.BARREL.defaultBlockState());
            b.put(x + 1, base + 1, z0 + 3, p.foundation());
        }
        b.fill(x0 + 3, base + 1, z0 + 6, x1 - 3, base + 1, z0 + 7,
                Materials.wool(DyeColor.BROWN));
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, base + 8, 3, x1 - x0 >= z1 - z0 ? 0 : 1);
        signAt(b, x0 + 4, base + 1, z1 + 2, Direction.SOUTH, "WAREHOUSE");
        signAt(b, x1 - 4, base + 1, z1 + 2, Direction.SOUTH, "GENOA");
    }

    // ------------------------------------------------------------------ Bosphorus

    private static void bosphorus(StructureBuilder b, int cx, int cz, Palette p,
                                  IntBinaryOperator surface) {
        BlockState water = Blocks.WATER.defaultBlockState();
        int harbourY = surface.applyAsInt(cx + 246, cz + 220);
        int wx0 = cx + 260;
        int wx1 = cx + 340;
        int wz0 = cz + 160;
        int wz1 = cz + 340;
        // The quay follows the harbour's own level at the graded rim.
        paveArea(b, cx + 232, cz + 120, cx + 259, cz + 340, harbourY, p.foundation(), p);
        paveArea(b, cx + 232, cz + 120, cx + 292, cz + 139, harbourY, p.foundation(), p);
        paveArea(b, cx + 260, cz + 140, cx + 340, cz + 159, harbourY, p.foundation(), p);
        for (int x = wx0; x <= wx1; x++) {
            for (int z = wz0; z <= wz1; z++) {
                int natural = b.groundY(x, z);
                b.put(x, harbourY, z, air());
                if (natural > harbourY) {
                    b.fill(x, harbourY + 1, z, x, natural + 2, z, air());
                }
            }
        }
        b.fill(wx0, harbourY - 5, wz0, wx1, harbourY - 5, wz1, p.rubble());
        b.fill(wx0, harbourY - 4, wz0, wx1, harbourY - 1, wz1, water);
        b.fill(cx + 259, harbourY - 5, cz + 160, cx + 259, harbourY, cz + 340,
                p.foundation());

        for (int z = cz + 160; z <= cz + 340; z += 2) {
            if (isPierZ(z - cz)) {
                continue;
            }
            b.put(cx + 259, harbourY + 1, z, p.rail());
        }
        dock(b, cx + 260, cx + 292, cz + 180, harbourY, p);
        dock(b, cx + 260, cx + 292, cz + 230, harbourY, p);
        dock(b, cx + 260, cx + 292, cz + 280, harbourY, p);
        boat(b, cx + 296, cz + 176, harbourY, p);
        boat(b, cx + 296, cz + 226, harbourY, p);
        boat(b, cx + 296, cz + 276, harbourY, p);

        fishMarket(b, cx + 236, cz + 196, cx + 254, cz + 240, harbourY, p);
        ferryOffice(b, cx + 228, cz + 144, cx + 242, cz + 156, harbourY, p);
        lighthouse(b, cx + 250, cz + 315, harbourY, p);
        b.put(cx + 240, harbourY + 1, cz + 250, Blocks.BARREL.defaultBlockState());
        b.put(cx + 241, harbourY + 1, cz + 250, Blocks.BARREL.defaultBlockState());
        b.put(cx + 236, harbourY + 1, cz + 196, Blocks.BARREL.defaultBlockState());

        signAt(b, cx + 250, harbourY + 1, cz + 140, Direction.WEST, "FERRIES");
        signAt(b, cx + 244, harbourY + 1, cz + 242, Direction.SOUTH, "FISH MARKET");
        signAt(b, cx + 258, harbourY + 1, cz + 300, Direction.WEST, "MIND THE EDGE");
        signAt(b, cx + 236, harbourY + 1, cz + 120, Direction.SOUTH, "QUAY");
    }

    private static boolean isPierZ(int z) {
        return Math.abs(z - 180) <= 3 || Math.abs(z - 230) <= 3 || Math.abs(z - 280) <= 3;
    }

    private static void dock(StructureBuilder b, int x0, int x1, int zc, int ground, Palette p) {
        b.fill(x0, ground, zc - 2, x1, ground, zc + 2, Blocks.SPRUCE_PLANKS.defaultBlockState());
        for (int x = x0; x <= x1; x += 6) {
            b.fill(x, ground - 4, zc - 2, x, ground - 1, zc - 2, p.accent());
            b.fill(x, ground - 4, zc + 2, x, ground - 1, zc + 2, p.accent());
        }
        for (int x = x0; x <= x1; x += 2) {
            b.put(x, ground + 1, zc - 2, p.rail());
            b.put(x, ground + 1, zc + 2, p.rail());
        }
        b.put(x1, ground + 2, zc - 2, p.light());
        b.put(x1, ground + 2, zc + 2, p.light());
    }

    private static void boat(StructureBuilder b, int x, int z, int ground, Palette p) {
        BlockState hull = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        b.fill(x, ground - 2, z - 3, x + 16, ground - 1, z + 3, hull);
        b.fill(x + 1, ground - 1, z - 2, x + 15, ground - 1, z + 2, air());
        b.fill(x, ground, z - 3, x + 16, ground, z - 3, hull);
        b.fill(x, ground, z + 3, x + 16, ground, z + 3, hull);
        b.fill(x, ground, z - 2, x, ground, z + 2, hull);
        b.fill(x + 16, ground, z - 2, x + 16, ground, z + 2, hull);
        b.fill(x + 7, ground, z, x + 7, ground + 9, z, p.accent());
        b.fill(x + 7, ground + 5, z - 2, x + 7, ground + 8, z + 2,
                Materials.wool(DyeColor.WHITE));
        b.put(x + 7, ground + 10, z, p.light());
        b.fill(x + 2, ground, z - 1, x + 5, ground, z + 1, p.roofSlab());
        b.fill(x + 9, ground, z - 1, x + 12, ground, z + 1, p.roofSlab());
    }

    private static void fishMarket(StructureBuilder b, int x0, int z0, int x1, int z1,
                                   int ground, Palette p) {
        pad(b, x0, z0, x1, z1, ground, p);
        b.fill(x0 + 2, ground, z0 + 1, x1 - 2, ground, z0 + 2,
                Blocks.PACKED_ICE.defaultBlockState());
        for (int z = z0 + 4; z <= z1 - 4; z += 8) {
            stall(b, x0 + 2, z, ground, p, DyeColor.LIGHT_BLUE);
            b.fill(x0 + 7, ground + 1, z, x0 + 7, ground + 2, z + 2,
                    Blocks.DRIED_KELP_BLOCK.defaultBlockState());
            b.put(x0 + 8, ground + 1, z, Blocks.BARREL.defaultBlockState());
        }
        signAt(b, (x0 + x1) / 2, ground + 1, z1 + 2, Direction.SOUTH, "FISH MARKET");
    }

    private static void ferryOffice(StructureBuilder b, int x0, int z0, int x1, int z1,
                                    int ground, Palette p) {
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x0, ground, z0, x1, ground + 5, z1, new StructureBuilder.Doorway(Side.E, 6));
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        placeDoor(b, p, x0, z0, x1, z1, ground + 1, Side.E, 6);
        b.fill(x0 + 2, ground + 2, z1, x0 + 5, ground + 3, z1, p.window());
        b.put(x0 + 2, ground + 1, z0 + 2, Blocks.BARREL.defaultBlockState());
        b.put(x0 + 3, ground + 1, z0 + 2, Blocks.LECTERN.defaultBlockState());
        signAt(b, x1 + 1, ground + 1, (z0 + z1) / 2 - 4, Direction.EAST, "FERRIES");
        signAt(b, x1 + 1, ground + 1, (z0 + z1) / 2 + 4, Direction.EAST, "TICKETS");
    }

    private static void lighthouse(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 3, ground, z - 3, x + 3, ground + 1, z + 3, p.foundation());
        for (int y = ground + 2; y <= ground + 14; y++) {
            int r = y < ground + 8 ? 2 : 1;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) == r || Math.abs(dz) == r) {
                        b.put(x + dx, y, z + dz,
                                y % 4 == 0 ? Materials.concrete(DyeColor.RED)
                                        : Materials.concrete(DyeColor.WHITE));
                    }
                }
            }
        }
        b.fill(x - 2, ground + 15, z - 2, x + 2, ground + 15, z + 2, p.foundation());
        b.fill(x - 1, ground + 16, z - 1, x + 1, ground + 17, z + 1, p.light());
        b.put(x, ground + 18, z, p.roofSlab());
    }

    // ------------------------------------------------------------------ rail station

    private static void railStation(StructureBuilder b, int cx, int cz, Palette p,
                                    IntBinaryOperator surface) {
        int x0 = cx - 160;
        int x1 = cx - 120;
        int z0 = cz + 260;
        int z1 = cz + 290;
        int trackEnd = cz + 310;
        int base = surface.applyAsInt(cx - 140, cz + 275);
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 2, base, p);

        // A straight, level track approach to the district edge.
        for (int z = z1 + 1; z <= trackEnd; z++) {
            for (int x = cx - 141; x <= cx - 139; x++) {
                int natural = b.groundY(x, z);
                if (natural < base) {
                    b.fill(x, natural + 1, z, x, base, z, p.foundation());
                } else if (natural > base) {
                    b.fill(x, base + 1, z, x, natural + 2, z, air());
                }
            }
        }
        b.fill(cx - 141, base, z0, cx - 139, base, trackEnd, p.rubble());
        for (int z = z0; z <= trackEnd; z++) {
            b.put(cx - 140, base + 1, z, Blocks.RAIL.defaultBlockState());
        }
        b.fill(x0 + 2, base + 1, z0 + 2, cx - 143, base + 1, z1 - 2, p.foundation());
        b.fill(cx - 137, base + 1, z0 + 2, x1 - 1, base + 1, z1 - 2, p.foundation());

        b.walls(x0, base + 1, z0, x1, base + 7, z1, p.wall());
        b.fill(cx - 142, base + 1, z0, cx - 138, base + 6, z0, air());
        b.fill(cx - 142, base + 1, z1, cx - 138, base + 6, z1, air());
        b.fill(x1, base + 1, cz + 273, x1, base + 4, cz + 277, air());
        b.fill(x1 - 1, base + 1, cz + 273, x1 - 1, base + 4, cz + 277, air());

        b.room(x0, base, z0, cx - 152, base + 5, z0 + 10,
                new StructureBuilder.Doorway(Side.E, 5));
        b.fill(x0, base, z0, cx - 152, base, z0 + 10, p.foundation());
        placeDoor(b, p, x0, z0, cx - 152, z0 + 10, base + 1, Side.E, 5);
        b.put(x0 + 2, base + 1, z0 + 2, Blocks.LECTERN.defaultBlockState());
        b.put(x0 + 3, base + 1, z0 + 2, Blocks.BARREL.defaultBlockState());

        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, base + 8, 4, 1);
        lanternPost(b, x0 + 2, z1 - 2, base, p);
        lanternPost(b, x1 - 2, z1 - 2, base, p);
        b.put(x1 - 2, base + 1, z0 + 2, Blocks.BARREL.defaultBlockState());
        b.put(x1 - 3, base + 1, z0 + 2, Blocks.BARREL.defaultBlockState());

        signAt(b, cx - 132, base + 1, z1 - 1, Direction.SOUTH, "RAILWAY STATION");
        signAt(b, cx - 124, base + 1, z1 - 1, Direction.SOUTH, "PLATFORM 1");
    }

    // ------------------------------------------------------------------ dressing

    private static void dressing(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        for (int z = -90; z <= 300; z += 40) {
            if (z == 190) {
                continue;
            }
            lanternPost(b, cx - 9, cz + z, ground, p);
            lanternPost(b, cx + 8, cz + z, ground, p);
        }
        planeTree(b, cx - 30, cz + 80, ground, p);
        planeTree(b, cx + 40, cz + 40, ground, p);
        planeTree(b, cx - 20, cz + 200, ground, p);
        planeTree(b, cx + 20, cz + 200, ground, p);

        // Stone runnel from the old-city fountain toward the avenue.
        b.fill(cx - 15, ground, cz + 71, cx - 9, ground, cz + 71, p.foundation());
        b.fill(cx - 15, ground, cz + 73, cx - 9, ground, cz + 73, p.foundation());
        b.fill(cx - 15, ground, cz + 72, cx - 9, ground, cz + 72,
                Blocks.WATER.defaultBlockState());

        b.put(cx - 300, ground + 1, cz + 44, p.foundation());
        b.put(cx - 299, ground + 1, cz + 44, p.foundation());
        b.put(cx - 299, ground + 2, cz + 44, p.foundation());

        b.fill(cx - 147, ground + 1, cz + 78, cx - 147, ground + 3, cz + 82,
                Blocks.DARK_OAK_PLANKS.defaultBlockState());
        signAt(b, cx - 147, ground + 1, cz + 80, Direction.WEST, "BOARDED");
        b.fill(cx - 303, ground + 1, cz - 122, cx - 303, ground + 3, cz - 118,
                Blocks.DARK_OAK_PLANKS.defaultBlockState());
        signAt(b, cx - 303, ground + 1, cz - 120, Direction.EAST, "BOARDED");

        b.put(cx - 230, ground + 1, cz + 12, Blocks.CAMPFIRE.defaultBlockState());
        b.put(cx - 230, ground + 1, cz + 13, p.foundation());
    }

    // ------------------------------------------------------------------ small pieces

    private static void column(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground, z, p.foundation());
        b.fill(x, ground + 1, z, x, ground + 4, z, p.accent());
        b.put(x, ground + 5, z, p.roofSlab());
    }

    private static void fountain(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 4, ground, z - 4, x + 4, ground, z + 4, p.foundation());
        b.walls(x - 4, ground + 1, z - 4, x + 4, ground + 1, z + 4, p.accent());
        b.fill(x - 3, ground, z - 3, x + 3, ground, z + 3, Blocks.WATER.defaultBlockState());
        b.put(x, ground, z, p.foundation());
        b.fill(x, ground + 1, z, x, ground + 3, z, p.accent());
        b.put(x, ground + 4, z, p.light());
    }

    private static void cypress(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground, z, p.ground());
        b.fill(x, ground + 1, z, x, ground + 8, z, Blocks.DARK_OAK_LOG.defaultBlockState());
        b.fill(x - 1, ground + 4, z - 1, x + 1, ground + 10, z + 1,
                Blocks.DARK_OAK_LEAVES.defaultBlockState());
        b.put(x, ground + 11, z, Blocks.DARK_OAK_LEAVES.defaultBlockState());
    }

    private static void planeTree(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground, z, p.ground());
        b.fill(x, ground + 1, z, x, ground + 5, z, Blocks.OAK_LOG.defaultBlockState());
        b.fill(x - 2, ground + 5, z - 2, x + 2, ground + 7, z + 2,
                Blocks.OAK_LEAVES.defaultBlockState());
        b.fill(x - 1, ground + 8, z - 1, x + 1, ground + 8, z + 1,
                Blocks.OAK_LEAVES.defaultBlockState());
    }

    private static void lanternPost(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground, z, p.foundation());
        b.fill(x, ground + 1, z, x, ground + 3, z, p.accent());
        b.put(x, ground + 3, z, p.light());
        b.put(x, ground + 4, z, p.roofSlab());
    }

    private static void stall(StructureBuilder b, int x, int z, int ground, Palette p,
                              DyeColor cloth) {
        BlockState awning = Materials.wool(cloth);
        b.put(x, ground, z, p.foundation());
        b.put(x + 2, ground, z, p.foundation());
        b.put(x, ground, z + 2, p.foundation());
        b.put(x + 2, ground, z + 2, p.foundation());
        b.fill(x, ground + 1, z, x, ground + 3, z, p.accent());
        b.fill(x + 2, ground + 1, z, x + 2, ground + 3, z, p.accent());
        b.fill(x, ground + 1, z + 2, x, ground + 3, z + 2, p.accent());
        b.fill(x + 2, ground + 1, z + 2, x + 2, ground + 3, z + 2, p.accent());
        b.fill(x, ground + 4, z, x + 2, ground + 4, z + 2, awning);
        b.put(x + 1, ground + 2, z + 1, p.foundation());
        b.put(x + 1, ground + 3, z + 1, p.light());
    }

    private static void house(StructureBuilder b, int x0, int z0, int w, int d, int h,
                              Palette p, Side door, IntBinaryOperator surface) {
        int x1 = x0 + w - 1;
        int z1 = z0 + d - 1;
        int base = surface.applyAsInt(x0 + w / 2, z0 + d / 2);
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, base, p);
        int offset = (door == Side.N || door == Side.S) ? w / 2 : d / 2;
        b.room(x0, base, z0, x1, base + h, z1,
                new StructureBuilder.Doorway(door, offset));
        b.fill(x0, base, z0, x1, base, z1, p.foundation());
        for (int y = base; y <= base + h; y++) {
            b.put(x0, y, z0, p.accent());
            b.put(x1, y, z0, p.accent());
            b.put(x0, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
        for (int wx = x0 + 2; wx <= x1 - 2; wx += 4) {
            b.window(wx, base + 2, z0, 2, 2, false);
            b.window(wx, base + 2, z1, 2, 2, false);
        }
        for (int wz = z0 + 2; wz <= z1 - 2; wz += 4) {
            b.put(x0, base + 2, wz, p.window());
            b.put(x0, base + 3, wz, p.window());
            b.put(x1, base + 2, wz, p.window());
            b.put(x1, base + 3, wz, p.window());
        }
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, base + h + 1, 3, w >= d ? 0 : 1);
        b.put(x0 + 2, base + h + 2, z0 + 2, p.accent());
        b.put(x0 + 2, base + h + 3, z0 + 2, p.accent());
        placeDoor(b, p, x0, z0, x1, z1, base + 1, door, offset);
        b.put(x0 + 1, base + 3, z0 + 1, p.light());
    }

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
        b.put(dx, y, dz, air());
        b.put(dx, y + 1, dz, air());
        BlockState lower = p.door()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        b.put(dx, y, dz, lower);
        b.put(dx, y + 1, dz,
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
