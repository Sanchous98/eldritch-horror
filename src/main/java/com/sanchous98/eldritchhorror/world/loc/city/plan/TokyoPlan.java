package com.sanchous98.eldritchhorror.world.loc.city.plan;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.SiteLoot;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import com.sanchous98.eldritchhorror.world.loc.style.Materials;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.RailShape;

/**
 * Authored Taisho-era Tokyo: Asakusa's temple yard and five-level pagoda, the Nakamise lantern
 * street, Ginza's brick shops and cinema, Yanaka's wooden lanes and cemetery, the Sumida docks,
 * and the shrine hill above them all.
 *
 * <p>Everything is written through {@link StructureBuilder}; there is no RNG, no chunk-local read
 * and no shared prefab. Coordinates are relative to the city centre {@code (cx, ground, cz)} and
 * stay inside {@code [-340, 340]}. The shared generator flattens the district to {@code ground}
 * first, so every lot sits on that plate; only the shrine hill and the crypt use authored offsets.
 */
public final class TokyoPlan implements CityPlan {

    /** Authored half-extent (blocks). */
    private static final int HALF = 340;

    /** The pagoda (and its crypt) sit here in the Asakusa yard. */
    private static final int PAGODA_X = 190;
    private static final int PAGODA_Z = -160;

    /** The hill shrine, at the top of the torii path. */
    private static final int SHRINE_X = 0;
    private static final int SHRINE_Z = -220;

    @Override
    public String cityId() {
        return "tokyo";
    }

    @Override
    public int halfExtent() {
        return HALF;
    }

    @Override
    public void build(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        streets(b, cx, cz, ground, p);
        arrivalGate(b, cx, cz, ground, p);
        asakusa(b, cx, cz, ground, p);
        ginza(b, cx, cz, ground, p);
        yanaka(b, cx, cz, ground, p);
        docks(b, cx, cz, ground, p);
        shrineHill(b, cx, cz, ground, p);
        crypt(b, cx, cz, ground, p);
        dressing(b, cx, cz, ground, p);
    }

    /**
     * Terrain-aware entry point: the shared pass keeps the authored heart flat and grades the rim.
     * The core stays on {@code ground}; only the rail yard and its straight, level track approach
     * use the graded surface at the eastern rim.
     */
    @Override
    public void build(StructureBuilder b, CityPlanContext ctx, Palette p) {
        build(b, ctx.cx(), ctx.cz(), ctx.ground(), p);
        station(b, ctx, p);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.LANDMARK, 0, 1, 320, "south_gate"),
                new CityPoi("ginza_quest_board", CityPoi.Kind.QUEST_BOARD, 0, 1, 70,
                        "tokyo_cursed_reel,tokyo_missing_miko"),
                new CityPoi("ryokan", CityPoi.Kind.INN, -80, 1, 40, "innkeeper"),
                new CityPoi("innkeeper", CityPoi.Kind.NPC, -80, 2, 30, "innkeeper"),
                new CityPoi("department_store", CityPoi.Kind.SHOP, 40, 2, 100, "shop_clerk"),
                new CityPoi("shop_clerk", CityPoi.Kind.NPC, 40, 2, 100, "shop_clerk"),
                new CityPoi("herbalist", CityPoi.Kind.HEALER, -220, 2, 60, "herbalist"),
                new CityPoi("herbalist_keeper", CityPoi.Kind.NPC, -220, 2, 60, "herbalist"),
                new CityPoi("cinema", CityPoi.Kind.NPC, 60, 2, 20, "projectionist|tokyo_cursed_reel"),
                new CityPoi("shrine", CityPoi.Kind.NPC, SHRINE_X, 11, SHRINE_Z,
                        "shrine_priest|tokyo_missing_miko"),
                new CityPoi("shrine_hill", CityPoi.Kind.LANDMARK, 0, 8, -180, "torii_path"),
                new CityPoi("five_level_pagoda", CityPoi.Kind.LANDMARK, PAGODA_X, 3, PAGODA_Z,
                        "asakusa"),
                new CityPoi("sensoji_temple", CityPoi.Kind.LANDMARK, 250, 3, -190, "temple_hall"),
                new CityPoi("nakamise", CityPoi.Kind.LANDMARK, 153, 1, -55, "lantern_street"),
                new CityPoi("yanaka_cemetery", CityPoi.Kind.LANDMARK, -260, 1, -40, "warning"),
                new CityPoi("docks_warehouse", CityPoi.Kind.SECRET, 260, 2, 180, "smuggled_cargo"),
                new CityPoi("dockworker", CityPoi.Kind.NPC, 200, 1, 152, "dockworker"),
                new CityPoi("sumida_canal", CityPoi.Kind.LANDMARK, 220, 1, 133, "boats"),
                new CityPoi("sumida_port", CityPoi.Kind.PORT, 310, 2, 143, "canal_wharf"),
                new CityPoi("rail_station", CityPoi.Kind.STATION, 260, 2, -30,
                        "tokyo_rail_yard;east_west"),
                new CityPoi("police_box", CityPoi.Kind.NPC, 0, 1, 120, "constable"),
                new CityPoi("constable", CityPoi.Kind.NPC, 18, 2, 120, "constable"),
                new CityPoi("pagoda_crypt", CityPoi.Kind.SECRET, PAGODA_X, -8, PAGODA_Z,
                        "tokyo_missing_miko"),
                new CityPoi("missing_miko", CityPoi.Kind.NPC, PAGODA_X, -8, PAGODA_Z + 2,
                        "tokyo_missing_miko"));
    }

    // ------------------------------------------------------------------ streets

    private static void streets(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        BlockState gravel = p.ground();
        BlockState stone = p.foundation();

        // The main avenue from the arrival gate north to the shrine path.
        street(b, cx - 6, cz - 140, cx + 6, cz + 320, ground, gravel, p);
        // Ginza's brick-paved shopping street.
        street(b, cx - 120, cz + 40, cx + 120, cz + 50, ground, stone, p);
        // The east approach from the avenue to Nakamise's great gate.
        street(b, cx + 6, cz - 24, cx + 150, cz - 16, ground, gravel, p);
        // Nakamise itself, six blocks wide.
        street(b, cx + 150, cz - 90, cx + 156, cz - 20, ground, gravel, p);
        // Yanaka's long lane and its two cross lanes.
        street(b, cx - 180, cz - 260, cx - 177, cz + 220, ground, gravel, p);
        street(b, cx - 320, cz - 124, cx - 140, cz - 121, ground, gravel, p);
        street(b, cx - 320, cz + 100, cx - 140, cz + 103, ground, gravel, p);
        street(b, cx - 177, cz + 48, cx - 120, cz + 51, ground, gravel, p);
        // The docks road from the avenue, then east along the south quay.
        street(b, cx + 6, cz + 156, cx + 320, cz + 164, ground, stone, p);
        // Side lanes in Ginza.
        street(b, cx - 60, cz - 80, cx - 57, cz + 40, ground, gravel, p);
        street(b, cx + 84, cz + 50, cx + 87, cz + 160, ground, gravel, p);
        street(b, cx + 6, cz + 80, cx + 70, cz + 84, ground, gravel, p);
        // The temple yard's gravel apron.
        paveArea(b, cx + 120, cz - 260, cx + 290, cz - 90, ground, gravel, p);
        // Ginza's pavements.
        paveArea(b, cx - 120, cz + 30, cx + 120, cz + 39, ground, stone, p);
        paveArea(b, cx - 120, cz + 51, cx + 120, cz + 60, ground, stone, p);
        // Yanaka's shrine approach path to the herbalist, and the cemetery gate.
        paveArea(b, cx - 204, cz + 58, cx - 180, cz + 62, ground, stone, p);
        paveArea(b, cx - 260, cz + 1, cx - 177, cz + 6, ground, gravel, p);
        // Short links from the avenue and the quay to their doors.
        paveArea(b, cx + 6, cz + 118, cx + 12, cz + 122, ground, gravel, p);
        paveArea(b, cx + 255, cz + 164, cx + 265, cz + 168, ground, stone, p);
        paveArea(b, cx + 136, cz + 164, cx + 144, cz + 170, ground, stone, p);
    }

    private static void street(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                               BlockState state, Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
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
                b.put(x, ground, z, state);
            }
        }
    }

    /** Caps a building footprint with a foundation course on the flat authored plate. */
    private static void pad(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                            Palette p) {
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
    }

    // ------------------------------------------------------------------ arrival

    private static void arrivalGate(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x = cx;
        int z = cz + 320;
        BlockState red = Materials.concrete(DyeColor.RED);
        BlockState black = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();

        b.fill(x - 14, ground, z - 8, x + 14, ground, z + 8, p.foundation());
        b.fill(x - 11, ground + 1, z - 6, x - 8, ground + 10, z - 3, red);
        b.fill(x + 8, ground + 1, z - 6, x + 11, ground + 10, z - 3, red);
        b.fill(x - 8, ground + 1, z - 5, x - 4, ground + 6, z - 4, p.wall());
        b.fill(x + 4, ground + 1, z - 5, x + 8, ground + 6, z - 4, p.wall());
        b.fill(x - 14, ground + 10, z - 8, x + 14, ground + 11, z + 8, p.roof());
        b.pitchedRoof(x - 15, z - 9, x + 15, z + 9, ground + 11, 4, 0);
        b.fill(x - 3, ground + 12, z - 7, x + 3, ground + 12, z + 7, p.roofSlab());
        b.put(x - 11, ground + 9, z - 2, p.light());
        b.put(x + 11, ground + 9, z - 2, p.light());
        b.put(x - 7, ground + 1, z - 2, black);
        b.put(x + 7, ground + 1, z - 2, black);

        signAt(b, x, ground + 1, z + 8, Direction.SOUTH, "TOKYO", "SOUTH GATE");

        // Signposts pointing at the districts.
        signAt(b, x - 10, ground + 1, z - 10, Direction.SOUTH, "ASAKUSA ->");
        signAt(b, x - 5, ground + 1, z - 10, Direction.SOUTH, "GINZA ->");
        signAt(b, x, ground + 1, z - 10, Direction.SOUTH, "SHRINE ^");
        signAt(b, x + 5, ground + 1, z - 10, Direction.SOUTH, "YANAKA <-");
        signAt(b, x + 10, ground + 1, z - 10, Direction.SOUTH, "DOCKS ->");

        // Lanterns flanking the gate mouth.
        b.put(x - 13, ground + 1, z + 2, p.accent());
        b.put(x - 13, ground + 2, z + 2, p.light());
        b.put(x + 13, ground + 1, z + 2, p.accent());
        b.put(x + 13, ground + 2, z + 2, p.light());
    }

    // ------------------------------------------------------------------ Asakusa

    private static void asakusa(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // The temple yard's low perimeter wall, with a wide south opening.
        b.walls(cx + 120, ground + 1, cz - 260, cx + 290, ground + 2, cz - 90, p.weathered());
        b.fill(cx + 150, ground + 1, cz - 90, cx + 156, ground + 2, cz - 90, air());
        b.fill(cx + 120, ground + 1, cz - 165, cx + 120, ground + 2, cz - 145, air());

        nakamise(b, cx, cz, ground, p);
        greatGate(b, cx + 153, cz - 22, ground, p);
        pagoda(b, cx + PAGODA_X, cz + PAGODA_Z, ground, p);
        templeHall(b, cx + 250, cz - 190, ground, p);
        bellTower(b, cx + 148, cz - 210, ground, p);

        // Stone lanterns along the yard's processional path.
        stoneLantern(b, cx + 170, cz - 100, ground, p);
        stoneLantern(b, cx + 210, cz - 100, ground, p);
        stoneLantern(b, cx + 170, cz - 130, ground, p);
        stoneLantern(b, cx + 210, cz - 130, ground, p);
        stoneLantern(b, cx + 230, cz - 145, ground, p);
        stoneLantern(b, cx + 270, cz - 145, ground, p);

        // Cherry trees and the incense burner in front of the hall.
        cherryTree(b, cx + 135, cz - 110, ground, p);
        cherryTree(b, cx + 275, cz - 110, ground, p);
        cherryTree(b, cx + 275, cz - 230, ground, p);
        cherryTree(b, cx + 135, cz - 230, ground, p);
        b.fill(cx + 249, ground + 1, cz - 155, cx + 251, ground + 1, cz - 153,
                Blocks.CAMPFIRE.defaultBlockState());

        signAt(b, cx + 153, ground + 1, cz - 92, Direction.SOUTH, "NAKAMISE", "TEMPLE");
        signAt(b, cx + 120, ground + 1, cz - 150, Direction.WEST, "ASAKUSA");
        signAt(b, cx + 290, ground + 1, cz - 150, Direction.EAST, "PAGODA");
    }

    private static void nakamise(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Lantern posts down both sides of the six-wide street.
        for (int z = -84; z <= -34; z += 10) {
            lanternPost(b, cx + 147, cz + z, ground, p);
            lanternPost(b, cx + 159, cz + z, ground, p);
        }
        // Market stalls, set back from the walking line.
        for (int z = -82; z <= -38; z += 12) {
            stall(b, cx + 140, cz + z, ground, p, true);
            stall(b, cx + 160, cz + z, ground, p, false);
        }
        signAt(b, cx + 153, ground + 1, cz - 30, Direction.SOUTH, "NAKAMISE");
        signAt(b, cx + 144, ground + 1, cz - 50, Direction.WEST, "LANTERNS");
    }

    private static void stall(StructureBuilder b, int x, int z, int ground, Palette p,
                              boolean leftSide) {
        BlockState cloth = leftSide ? Materials.wool(DyeColor.RED)
                : Materials.wool(DyeColor.BLUE);
        b.put(x, ground, z, p.foundation());
        b.put(x + 3, ground, z, p.foundation());
        b.put(x + 3, ground, z + 4, p.foundation());
        b.put(x, ground, z + 4, p.foundation());
        for (int y = ground + 1; y <= ground + 3; y++) {
            b.put(x, y, z, p.accent());
            b.put(x + 3, y, z, p.accent());
            b.put(x, y, z + 4, p.accent());
            b.put(x + 3, y, z + 4, p.accent());
        }
        b.fill(x, ground + 4, z, x + 3, ground + 4, z + 4, p.roofSlab());
        b.fill(x, ground + 2, z + 1, x + 3, ground + 2, z + 1, p.foundation());
        b.fill(x, ground + 3, z + 1, x + 3, ground + 3, z + 1, cloth);
        b.put(x + 1, ground + 3, z + 3, p.light());
    }

    private static void greatGate(StructureBuilder b, int x, int z, int ground, Palette p) {
        BlockState red = Materials.concrete(DyeColor.RED);
        b.fill(x - 12, ground, z - 6, x + 12, ground, z + 6, p.foundation());
        b.fill(x - 11, ground + 1, z - 5, x - 8, ground + 11, z - 2, red);
        b.fill(x + 8, ground + 1, z - 5, x + 11, ground + 11, z - 2, red);
        b.fill(x - 11, ground + 1, z + 2, x - 7, ground + 7, z + 5, p.wall());
        b.fill(x + 7, ground + 1, z + 2, x + 11, ground + 7, z + 5, p.wall());
        b.fill(x - 12, ground + 11, z - 6, x + 12, ground + 12, z + 6, p.roof());
        b.pitchedRoof(x - 14, z - 7, x + 14, z + 7, ground + 12, 4, 0);
        // The great lantern hangs high enough to walk beneath.
        b.fill(x - 3, ground + 8, z - 1, x + 3, ground + 10, z + 1, red);
        b.fill(x - 2, ground + 8, z, x + 2, ground + 9, z, p.light());
        b.fill(x - 3, ground + 7, z, x + 3, ground + 7, z, p.accent());
        b.put(x - 6, ground + 8, z - 1, p.light());
        b.put(x + 6, ground + 8, z - 1, p.light());
        signAt(b, x, ground + 1, z + 7, Direction.SOUTH, "ASAKUSA");
        signAt(b, x - 12, ground + 1, z + 6, Direction.WEST, "NAKAMISE");
    }

    private static void pagoda(StructureBuilder b, int x, int z, int ground, Palette p) {
        // Stone tenshu-dai and its steps.
        b.fill(x - 16, ground, z - 16, x + 16, ground + 2, z + 16, p.foundation());
        b.walls(x - 16, ground + 2, z - 16, x + 16, ground + 2, z + 16, p.weathered());
        b.fill(x - 4, ground + 1, z + 17, x + 4, ground + 1, z + 20, p.foundation());
        b.fill(x - 4, ground + 2, z + 19, x + 4, ground + 2, z + 20, p.foundation());

        int half = 14;
        int y = ground + 3;
        for (int level = 0; level < 5; level++) {
            int h = 7 - level;
            int x0 = x - half;
            int x1 = x + half;
            int z0 = z - half;
            int z1 = z + half;
            b.room(x0, y, z0, x1, y + h, z1);
            b.fill(x0, y, z0, x1, y, z1, p.foundation());
            b.walls(x0, y + 1, z0, x1, y + 1, z1, p.accent());
            b.walls(x0, y + h, z0, x1, y + h, z1, p.accent());
            if (h >= 4) {
                int wy = y + 2;
                int wh = Math.min(3, h - 2);
                for (int d = -half + 3; d <= half - 3; d += 4) {
                    b.window(x0, wy, z + d, wh, 1, true);
                    b.window(x1, wy, z + d, wh, 1, true);
                    b.window(x + d, wy, z0, wh, 1, true);
                    b.window(x + d, wy, z1, wh, 1, true);
                }
            }
            b.pitchedRoof(x0 - 3, z0 - 3, x1 + 3, z1 + 3, y + h, 3, 0);
            // Bronze bells under the flared eaves.
            b.put(x0 - 1, y + h - 1, z0 - 1, Blocks.BELL.defaultBlockState());
            b.put(x1 + 1, y + h - 1, z0 - 1, Blocks.BELL.defaultBlockState());
            b.put(x0 - 1, y + h - 1, z1 + 1, Blocks.BELL.defaultBlockState());
            b.put(x1 + 1, y + h - 1, z1 + 1, Blocks.BELL.defaultBlockState());
            y += h + 3;
            half -= 2;
        }
        // Gilded finial.
        b.fill(x - 1, y, z - 1, x + 1, y + 2, z + 1, Blocks.GOLD_BLOCK.defaultBlockState());
        b.spire(x, z, y + 3, 10);
        placeDoor(b, p, x - 14, z - 14, x + 14, z + 14, ground + 4, Side.S, 14);
        signAt(b, x - 15, ground + 4, z + 16, Direction.SOUTH, "PAGODA", "CRYPT BELOW");
    }

    private static void templeHall(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 28, ground, z - 25, x + 28, ground + 1, z + 25, p.foundation());
        b.fill(x - 26, ground + 1, z - 23, x + 26, ground + 1, z + 23, p.accent());
        int y0 = ground + 2;
        b.room(x - 24, y0, z - 21, x + 24, y0 + 11, z + 21);
        b.fill(x - 24, y0, z - 21, x + 24, y0, z + 21, p.foundation());
        for (int y = y0 + 1; y <= y0 + 11; y++) {
            b.walls(x - 24, y, z - 21, x + 24, y, z + 21, p.wall());
            if (y == y0 + 1 || y == y0 + 6 || y == y0 + 11) {
                b.walls(x - 24, y, z - 21, x + 24, y, z + 21, p.accent());
            }
        }
        for (int px = -24; px <= 24; px += 8) {
            for (int y = y0 + 1; y <= y0 + 11; y++) {
                b.put(x + px, y, z - 21, p.accent());
                b.put(x + px, y, z + 21, p.accent());
            }
        }
        for (int d = -18; d <= 18; d += 6) {
            b.window(x - 24, y0 + 4, z + d, 4, 2, true);
            b.window(x + 24, y0 + 4, z + d, 4, 2, true);
        }
        b.pitchedRoof(x - 28, z - 25, x + 28, z + 25, y0 + 11, 8, 0);
        b.fill(x - 6, ground + 1, z + 22, x + 6, ground + 1, z + 28, p.foundation());
        placeDoor(b, p, x - 24, z - 21, x + 24, z + 21, y0 + 1, Side.S, 24);
        signAt(b, x, y0 + 9, z + 21, Direction.SOUTH, "SENSOJI");
        signAt(b, x - 22, ground + 1, z + 20, Direction.WEST, "TEMPLE");
        b.put(x - 20, ground + 1, z + 20, p.light());
        b.put(x + 20, ground + 1, z + 20, p.light());
    }

    private static void bellTower(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 5, ground, z - 5, x + 5, ground, z + 5, p.foundation());
        for (int y = ground + 1; y <= ground + 8; y++) {
            b.put(x - 4, y, z - 4, p.accent());
            b.put(x + 4, y, z - 4, p.accent());
            b.put(x - 4, y, z + 4, p.accent());
            b.put(x + 4, y, z + 4, p.accent());
        }
        b.fill(x - 5, ground + 8, z - 5, x + 5, ground + 8, z + 5, p.roof());
        b.pitchedRoof(x - 7, z - 7, x + 7, z + 7, ground + 8, 4, 0);
        b.put(x, ground + 6, z, Blocks.BELL.defaultBlockState());
        b.put(x, ground + 2, z, p.light());
        signAt(b, x + 5, ground + 1, z + 5, Direction.EAST, "BELL");
    }

    // ------------------------------------------------------------------ Ginza

    private static void ginza(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // North-side brick shops (cinema and ryokan occupy their own lots).
        int[] northX = {-116, -56, -36, 8, 88, 104};
        for (int dx : northX) {
            brickShop(b, cx + dx, cz + 12, ground, p, 16, 22, 9, Side.S, "GINZA");
        }
        // South-side brick shops.
        int[] southX = {-116, -96, -76, -56, -36, 8, 28, 48, 68, 88, 104};
        String[] southNames = {"CAFE", "KIMONO", "TOBACCO", "BOOKS", "RAMEN",
                "GINZA", "TOYS", "TEA", "ANTIQUES", "NEON", "CAFE"};
        for (int i = 0; i < southX.length; i++) {
            brickShop(b, cx + southX[i], cz + 56, ground, p, 16, 22, 8 + i % 3,
                    Side.N, southNames[i]);
        }
        // Two boarded-up shops, for the Taisho slump.
        b.fill(cx - 114, ground + 3, cz + 12, cx - 110, ground + 5, cz + 12,
                Blocks.DARK_OAK_TRAPDOOR.defaultBlockState());
        b.fill(cx + 104, ground + 3, cz + 56, cx + 108, ground + 5, cz + 56,
                Blocks.DARK_OAK_TRAPDOOR.defaultBlockState());

        cinema(b, cx + 60, cz + 20, ground, p);
        ryokan(b, cx - 80, cz + 40, ground, p);
        departmentStore(b, cx + 40, cz + 100, ground, p);
        policeBox(b, cx + 18, cz + 120, ground, p);
        questBoard(b, cx, cz + 70, ground, p);

        // Ginza dressing: neon signs and a rickshaw.
        b.fill(cx + 100, ground + 6, cz + 39, cx + 108, ground + 8, cz + 39,
                Materials.stainedGlass(DyeColor.MAGENTA));
        b.fill(cx + 100, ground + 6, cz + 40, cx + 108, ground + 6, cz + 40,
                Blocks.GLOWSTONE.defaultBlockState());
        b.fill(cx - 108, ground + 6, cz + 51, cx - 100, ground + 8, cz + 51,
                Materials.stainedGlass(DyeColor.CYAN));
        b.fill(cx - 108, ground + 6, cz + 50, cx - 100, ground + 6, cz + 50,
                Blocks.GLOWSTONE.defaultBlockState());
        b.fill(cx + 100, ground + 1, cz + 39, cx + 100, ground + 6, cz + 39, p.accent());
        b.fill(cx + 108, ground + 1, cz + 39, cx + 108, ground + 6, cz + 39, p.accent());
        b.fill(cx - 108, ground + 1, cz + 51, cx - 108, ground + 6, cz + 51, p.accent());
        b.fill(cx - 100, ground + 1, cz + 51, cx - 100, ground + 6, cz + 51, p.accent());
        rickshaw(b, cx - 13, cz + 90, ground, p);
        signAt(b, cx + 100, ground + 1, cz + 52, Direction.SOUTH, "GINZA");
        signAt(b, cx - 100, ground + 1, cz + 38, Direction.NORTH, "CAFE");
    }

    private static void brickShop(StructureBuilder b, int x, int z, int ground, Palette p,
                                  int w, int d, int h, Side doorSide, String name) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        pad(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x, y0, z, x1, y0 + h, z1);
        b.fill(x, y0, z, x1, y0, z1, p.foundation());
        for (int y = y0 + 1; y <= y0 + h; y++) {
            b.walls(x, y, z, x1, y, z1, Blocks.BRICKS.defaultBlockState());
        }
        b.walls(x, y0 + 1, z, x1, y0 + 1, z1, p.foundation());
        b.walls(x, y0 + h, z, x1, y0 + h, z1, p.foundation());
        for (int wx = x + 2; wx <= x1 - 2; wx += 5) {
            b.window(wx, y0 + 2, z1, 3, 2, false);
            b.window(wx, y0 + 2, z, 3, 2, false);
        }
        if (h >= 9) {
            b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, y0 + h, 3, 0);
        } else {
            b.crenellations(x - 1, z - 1, x1 + 1, z1 + 1, y0 + h + 1);
        }
        placeDoor(b, p, x, z, x1, z1, y0 + 1, doorSide, w / 2);
        if (doorSide == Side.N) {
            signAt(b, x + w / 2, y0 + h, z - 1, Direction.NORTH, name);
        } else {
            signAt(b, x + w / 2, y0 + h, z1 + 1, Direction.SOUTH, name);
        }
        b.put(x + 1, y0 + 2, z1, p.light());
        b.put(x + w / 2, y0 + h - 1, z + d / 2, p.light());
    }

    private static void cinema(StructureBuilder b, int x, int z, int ground, Palette p) {
        int x0 = x - 18;
        int x1 = x + 18;
        int z0 = z - 12;
        int z1 = z + 14;
        int y0 = ground + 1;
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x0, y0, z0, x1, y0 + 12, z1);
        b.fill(x0, y0, z0, x1, y0, z1, p.foundation());
        for (int y = y0 + 1; y <= y0 + 12; y++) {
            b.walls(x0, y, z0, x1, y, z1, Blocks.BRICKS.defaultBlockState());
        }
        b.walls(x0, y0 + 1, z0, x1, y0 + 1, z1, p.foundation());
        b.walls(x0, y0 + 12, z0, x1, y0 + 12, z1, p.foundation());
        // Marquee canopy over the entrance.
        b.fill(x0 + 4, y0 + 6, z1 + 1, x1 - 4, y0 + 6, z1 + 4, p.roofSlab());
        b.fill(x0 + 4, y0 + 5, z1 + 4, x1 - 4, y0 + 5, z1 + 4,
                Materials.stainedGlass(DyeColor.MAGENTA));
        b.fill(x0 + 4, y0 + 6, z1 + 5, x1 - 4, y0 + 6, z1 + 5,
                Blocks.GLOWSTONE.defaultBlockState());
        for (int px = x0 + 5; px <= x1 - 5; px += 6) {
            b.put(px, y0 + 1, z1 + 3, p.accent());
            b.put(px, y0 + 5, z1 + 3, p.accent());
        }
        // Screen and seats.
        b.fill(x0 + 5, y0 + 3, z0 + 1, x1 - 5, y0 + 8, z0 + 1,
                Materials.concrete(DyeColor.WHITE));
        for (int sz = z0 + 6; sz <= z1 - 4; sz += 3) {
            b.fill(x0 + 6, y0 + 1, sz, x1 - 6, y0 + 1, sz, p.accent());
            b.fill(x0 + 6, y0 + 2, sz, x1 - 6, y0 + 2, sz, Materials.wool(DyeColor.RED));
        }
        // Projection booth with the cursed reel.
        b.walls(x1 - 9, y0 + 1, z0 + 2, x1 - 2, y0 + 4, z0 + 8, p.accent());
        b.fill(x1 - 8, y0 + 1, z0 + 3, x1 - 3, y0 + 1, z0 + 7, p.foundation());
        b.chest(x1 - 5, y0 + 1, z0 + 5, SiteLoot.CULT);
        signAt(b, x1 - 5, y0 + 3, z0 + 8, Direction.SOUTH, "REEL INSIDE");
        placeDoor(b, p, x0, z0, x1, z1, y0 + 1, Side.S, 18);
        signAt(b, x, y0 + 9, z1 + 1, Direction.SOUTH, "CINEMA", "CURSED FILM");
        signAt(b, x, ground + 1, z1 + 6, Direction.SOUTH, "CURSED FILM", "REEL INSIDE");
        b.put(x0 + 2, y0 + 3, z1, p.light());
        b.put(x1 - 2, y0 + 3, z1, p.light());
    }

    private static void departmentStore(StructureBuilder b, int x, int z, int ground, Palette p) {
        int x0 = x - 24;
        int x1 = x + 24;
        int z0 = z - 13;
        int z1 = z + 13;
        int y0 = ground + 1;
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x0, y0, z0, x1, y0 + 14, z1);
        b.fill(x0, y0, z0, x1, y0, z1, p.foundation());
        for (int y = y0 + 1; y <= y0 + 14; y++) {
            b.walls(x0, y, z0, x1, y, z1, Blocks.BRICKS.defaultBlockState());
        }
        b.walls(x0, y0 + 1, z0, x1, y0 + 1, z1, p.foundation());
        b.walls(x0, y0 + 5, z0, x1, y0 + 5, z1, p.foundation());
        b.walls(x0, y0 + 10, z0, x1, y0 + 10, z1, p.foundation());
        b.walls(x0, y0 + 14, z0, x1, y0 + 14, z1, p.foundation());
        for (int wx = x0 + 4; wx <= x1 - 4; wx += 6) {
            b.window(wx, y0 + 2, z0, 3, 3, false);
            b.window(wx, y0 + 7, z0, 3, 3, false);
            b.window(wx, y0 + 12, z0, 2, 3, false);
        }
        b.fill(x0 + 1, y0 + 5, z0 + 1, x1 - 1, y0 + 5, z1 - 1,
                Blocks.DARK_OAK_PLANKS.defaultBlockState());
        b.fill(x0 + 1, y0 + 10, z0 + 1, x1 - 1, y0 + 10, z1 - 1,
                Blocks.DARK_OAK_PLANKS.defaultBlockState());
        // A simple stair between the sales floors.
        for (int i = 0; i < 5; i++) {
            b.put(x1 - 4, y0 + 1 + i, z1 - 4 - i, Blocks.DARK_OAK_STAIRS.defaultBlockState());
        }
        placeDoor(b, p, x0, z0, x1, z1, y0 + 1, Side.N, 24);
        signAt(b, x, y0 + 12, z0 - 1, Direction.NORTH, "DEPARTMENT", "STORE");
        b.put(x0 + 2, y0 + 3, z0, p.light());
        b.put(x1 - 2, y0 + 3, z0, p.light());
        // Delivery crates on the lane.
        b.put(x0 - 3, ground + 1, z0 - 2, p.foundation());
        b.put(x0 - 2, ground + 1, z0 - 2, p.foundation());
        b.put(x0 - 3, ground + 2, z0 - 2, p.foundation());
        b.chest(x0 + 3, y0 + 1, z1 - 3, SiteLoot.COMMON);
    }

    private static void ryokan(StructureBuilder b, int x, int z, int ground, Palette p) {
        int x0 = x - 16;
        int x1 = x + 16;
        int z0 = z - 20;
        int z1 = z - 2;
        int y0 = ground + 1;
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x0, y0, z0, x1, y0 + 12, z1);
        b.fill(x0, y0, z0, x1, y0, z1, p.foundation());
        for (int y = y0 + 1; y <= y0 + 12; y++) {
            b.walls(x0, y, z0, x1, y, z1, y <= y0 + 3 ? p.accent() : p.wall());
        }
        b.walls(x0, y0 + 4, z0, x1, y0 + 4, z1, p.accent());
        b.walls(x0, y0 + 8, z0, x1, y0 + 8, z1, p.accent());
        for (int px = x0 + 3; px <= x1 - 3; px += 5) {
            b.window(px, y0 + 2, z1, 2, 2, false);
            b.window(px, y0 + 6, z1, 2, 2, false);
        }
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y0 + 12, 4, 0);
        // Upper-floor guest beds on a timber floor, reached by a stair.
        b.fill(x0 + 1, y0 + 7, z0 + 1, x1 - 1, y0 + 7, z1 - 1,
                Blocks.DARK_OAK_PLANKS.defaultBlockState());
        b.fill(x0 + 2, y0 + 8, z0 + 2, x0 + 6, y0 + 8, z0 + 5,
                Materials.wool(DyeColor.RED));
        b.fill(x1 - 6, y0 + 8, z0 + 2, x1 - 2, y0 + 8, z0 + 5,
                Materials.wool(DyeColor.RED));
        for (int i = 0; i < 6; i++) {
            b.put(x1 - 1, y0 + 1 + i, z0 + 2 + i, Blocks.DARK_OAK_STAIRS.defaultBlockState());
        }
        b.chest(x0 + 2, y0 + 8, z1 - 4, SiteLoot.COMMON);
        placeDoor(b, p, x0, z0, x1, z1, y0 + 1, Side.S, 16);
        // Noren above the door.
        b.fill(x - 3, y0 + 4, z1, x + 3, y0 + 5, z1, Materials.wool(DyeColor.RED));
        signAt(b, x, y0 + 9, z1 + 1, Direction.SOUTH, "RYOKAN");
        signAt(b, x - 12, ground + 1, z1 + 2, Direction.SOUTH, "INN");
        b.put(x - 10, y0 + 3, z1, p.light());
        b.put(x + 10, y0 + 3, z1, p.light());
    }

    private static void policeBox(StructureBuilder b, int x, int z, int ground, Palette p) {
        int x0 = x - 6;
        int x1 = x + 6;
        int z0 = z - 6;
        int z1 = z + 6;
        int y0 = ground + 1;
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x0, y0, z0, x1, y0 + 6, z1);
        b.fill(x0, y0, z0, x1, y0, z1, p.foundation());
        for (int y = y0 + 1; y <= y0 + 6; y++) {
            b.walls(x0, y, z0, x1, y, z1, Blocks.BRICKS.defaultBlockState());
        }
        b.walls(x0, y0 + 1, z0, x1, y0 + 1, z1, p.foundation());
        b.walls(x0, y0 + 6, z0, x1, y0 + 6, z1, p.foundation());
        b.window(x1, y0 + 3, z0 + 3, 2, 2, false);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y0 + 6, 3, 1);
        placeDoor(b, p, x0, z0, x1, z1, y0 + 1, Side.W, 6);
        signAt(b, x0 - 1, y0 + 4, z, Direction.WEST, "POLICE");
        b.put(x0 + 1, y0 + 5, z0 + 1, Blocks.SEA_LANTERN.defaultBlockState());
        b.put(x, y0 + 4, z0 + 1, p.light());
    }

    private static void questBoard(StructureBuilder b, int x, int z, int ground, Palette p) {
        int x0 = x;
        int x1 = x + 4;
        int z0 = z - 2;
        int z1 = z + 2;
        for (int px : new int[]{x0, x1}) {
            for (int pz : new int[]{z0, z1}) {
                b.put(px, ground, pz, p.foundation());
                b.put(px, ground + 1, pz, p.accent());
                b.put(px, ground + 2, pz, p.accent());
                b.put(px, ground + 3, pz, p.accent());
            }
        }
        b.fill(x0, ground + 2, z0, x1, ground + 3, z0, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        b.fill(x0, ground + 2, z1, x1, ground + 3, z1, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        b.fill(x0 - 1, ground + 4, z0 - 1, x1 + 1, ground + 4, z1 + 1, p.roofSlab());
        signAt(b, x + 2, ground + 4, z0, Direction.SOUTH, "CITY NOTICES");
        signAt(b, x - 1, ground + 1, z, Direction.WEST, "CURSED REEL");
        signAt(b, x + 5, ground + 1, z, Direction.EAST, "MISSING MIKO");
    }

    // ------------------------------------------------------------------ Yanaka

    private static void yanaka(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int[][] westHouses = {
                {-312, -240}, {-288, -240}, {-264, -240}, {-240, -240},
                {-312, -216}, {-288, -216}, {-264, -216}, {-240, -216},
                {-312, -192}, {-288, -192}, {-264, -192}, {-240, -192},
                {-312, -168}, {-288, -168}, {-264, -168}, {-240, -168},
                {-312, -144}, {-288, -144}, {-264, -144}, {-240, -144},
                {-312, 12}, {-288, 12}, {-264, 12}, {-240, 12},
                {-312, 36}, {-288, 36}, {-264, 36},
                {-312, 60}, {-288, 60}, {-264, 60},
                {-312, 84}, {-288, 84}, {-264, 84}, {-240, 84},
                {-312, 120}, {-288, 120}, {-264, 120}, {-240, 120},
                {-312, 144}, {-288, 144}, {-264, 144}, {-240, 144},
                {-312, 168}, {-288, 168}, {-264, 168}, {-240, 168},
                {-312, 192}, {-288, 192}, {-264, 192}, {-240, 192},
                {-312, 216}, {-288, 216}, {-264, 216}, {-240, 216}
        };
        for (int[] h : westHouses) {
            machiya(b, cx + h[0], cz + h[1], ground, p, 12, 12, 4 + Math.abs(h[1] / 48) % 3,
                    Side.S);
        }
        int[][] eastHouses = {
                {-168, -240}, {-148, -240}, {-168, -216}, {-148, -216},
                {-168, -192}, {-148, -192}, {-168, -168}, {-148, -168},
                {-168, -144}, {-148, -144}, {-168, 12}, {-148, 12},
                {-168, 36}, {-148, 36}, {-168, 60}, {-148, 60},
                {-168, 84}, {-148, 84}, {-168, 120}, {-148, 120},
                {-168, 144}, {-148, 144}, {-168, 168}, {-148, 168},
                {-168, 192}, {-148, 192}, {-168, 216}, {-148, 216}
        };
        for (int[] h : eastHouses) {
            machiya(b, cx + h[0], cz + h[1], ground, p, 10, 12, 4 + Math.abs(h[0] / 20) % 3,
                    Side.W);
        }
        cemetery(b, cx - 260, cz - 50, ground, p);
        herbalist(b, cx - 220, cz + 60, ground, p);
        signAt(b, cx - 180, ground + 1, cz - 120, Direction.SOUTH, "YANAKA");
        signAt(b, cx - 180, ground + 1, cz + 180, Direction.NORTH, "YANAKA");
        signAt(b, cx - 310, ground + 1, cz - 122, Direction.EAST, "YANAKA LANES");
        // A couple of cats on a wall, as small stone figures.
        cat(b, cx - 170, cz + 90, ground, p);
        cat(b, cx - 260, cz + 30, ground, p);
    }

    private static void machiya(StructureBuilder b, int x, int z, int ground, Palette p, int w,
                                int d, int h, Side doorSide) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        pad(b, x - 1, z - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x, y0, z, x1, y0 + h, z1);
        b.fill(x, y0, z, x1, y0, z1, p.foundation());
        for (int y = y0 + 1; y <= y0 + h; y++) {
            b.walls(x, y, z, x1, y, z1, y <= y0 + 2 ? p.accent() : p.wall());
        }
        for (int y = y0 + 1; y <= y0 + h; y++) {
            b.put(x, y, z, p.accent());
            b.put(x1, y, z, p.accent());
            b.put(x, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
        b.walls(x, y0 + 4, z, x1, y0 + 4, z1, p.accent());
        b.walls(x, y0 + h, z, x1, y0 + h, z1, p.accent());
        b.window(x + 2, y0 + 2, z1, 2, 2, false);
        b.window(x1 - 3, y0 + 2, z1, 2, 2, false);
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, y0 + h, 3, 0);
        placeDoor(b, p, x, z, x1, z1, y0 + 1, doorSide, w / 2);
        b.fill(x + 3, y0 + 3, z1, x + 6, y0 + 4, z1, Materials.wool(DyeColor.RED));
        b.put(x + 1, y0 + 3, z1, p.light());
        // A tiny raked-gravel garden on one side.
        b.fill(x - 3, ground, z, x - 2, ground, z1, p.ground());
        b.put(x - 2, ground + 1, z + 2, Blocks.AZALEA.defaultBlockState());
        b.put(x - 2, ground + 1, z + 6, Blocks.FERN.defaultBlockState());
    }

    private static void cemetery(StructureBuilder b, int x, int z, int ground, Palette p) {
        // A walled, gravel yard with rows of low gravestones.
        b.fill(x - 40, ground, z - 50, x + 40, ground, z + 50, p.ground());
        b.walls(x - 40, ground + 1, z - 50, x + 40, ground + 2, z + 50, p.weathered());
        b.fill(x - 3, ground + 1, z + 50, x + 3, ground + 2, z + 50, air());
        for (int gx = -34; gx <= 34; gx += 9) {
            for (int gz = -42; gz <= 42; gz += 10) {
                if (Math.abs(gx) <= 10 && Math.abs(gz) <= 8) {
                    continue; // keep the chapel clear
                }
                grave(b, x + gx, z + gz, ground, p, (gx + gz) % 3 == 0);
            }
        }
        // A small wooden chapel at the heart of the yard.
        b.room(x - 8, ground + 1, z - 6, x + 8, ground + 9, z + 6);
        b.fill(x - 8, ground + 1, z - 6, x + 8, ground + 1, z + 6, p.foundation());
        for (int y = ground + 2; y <= ground + 9; y++) {
            b.walls(x - 8, y, z - 6, x + 8, y, z + 6, p.accent());
        }
        b.pitchedRoof(x - 10, z - 8, x + 10, z + 8, ground + 9, 4, 1);
        placeDoor(b, p, x - 8, z - 6, x + 8, z + 6, ground + 2, Side.S, 8);
        signAt(b, x, ground + 1, z + 51, Direction.SOUTH, "THE DEAD REST", "KEEP OUT");
        signAt(b, x - 12, ground + 1, z + 8, Direction.SOUTH, "YANAKA");
        // Jizo figures and a cherry tree.
        for (int jx = -6; jx <= 6; jx += 4) {
            b.put(x + jx, ground + 1, z - 10, Blocks.STONE_BRICKS.defaultBlockState());
            b.put(x + jx, ground + 2, z - 10, Blocks.STONE_BRICK_SLAB.defaultBlockState());
        }
        cherryTree(b, x - 30, z + 30, ground, p);
        cherryTree(b, x + 35, z - 45, ground, p);
    }

    private static void grave(StructureBuilder b, int x, int z, int ground, Palette p,
                              boolean mossy) {
        BlockState stone = mossy ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState()
                : Blocks.STONE_BRICKS.defaultBlockState();
        b.fill(x - 1, ground, z - 1, x + 1, ground, z + 1, p.foundation());
        b.fill(x, ground + 1, z, x, ground + 3, z, stone);
        b.put(x, ground + 4, z, Blocks.STONE_BRICK_SLAB.defaultBlockState());
        b.put(x - 1, ground + 1, z, Blocks.STONE_BRICK_WALL.defaultBlockState());
        b.put(x + 1, ground + 1, z, Blocks.STONE_BRICK_WALL.defaultBlockState());
    }

    private static void cat(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground, z, p.foundation());
        b.put(x, ground + 1, z, Materials.concrete(DyeColor.BLACK));
        b.put(x, ground + 2, z, Materials.concrete(DyeColor.BLACK));
        b.put(x, ground + 3, z, Materials.concrete(DyeColor.BLACK));
        b.put(x, ground + 3, z - 1, Materials.concrete(DyeColor.BLACK));
        b.put(x - 1, ground + 3, z, Materials.concrete(DyeColor.BLACK));
        b.put(x + 1, ground + 3, z, Materials.concrete(DyeColor.BLACK));
        b.put(x, ground + 2, z - 1, p.light());
    }

    private static void herbalist(StructureBuilder b, int x, int z, int ground, Palette p) {
        int x0 = x - 16;
        int x1 = x + 16;
        int z0 = z - 16;
        int z1 = z + 16;
        int y0 = ground + 1;
        pad(b, x0, z0, x1, z1, ground, p);
        // A low garden wall with a gate on the east side.
        b.walls(x0, ground + 1, z0, x1, ground + 2, z1, p.weathered());
        b.fill(x1, ground + 1, z - 2, x1, ground + 2, z + 2, air());
        b.fill(x0 + 2, ground + 1, z0 + 2, x1 - 2, ground + 1, z1 - 2,
                Blocks.MOSS_BLOCK.defaultBlockState());
        b.room(x0 + 4, y0, z0 + 4, x1 - 4, y0 + 7, z1 - 4);
        b.fill(x0 + 4, y0, z0 + 4, x1 - 4, y0, z1 - 4, p.foundation());
        for (int y = y0 + 1; y <= y0 + 7; y++) {
            b.walls(x0 + 4, y, z0 + 4, x1 - 4, y, z1 - 4, p.accent());
        }
        b.pitchedRoof(x0 + 2, z0 + 2, x1 - 2, z1 - 2, y0 + 7, 3, 0);
        placeDoor(b, p, x0 + 4, z0 + 4, x1 - 4, z1 - 4, y0 + 1, Side.E, 8);
        // Drying racks and herb beds.
        for (int i = 0; i < 4; i++) {
            b.put(x0 + 6 + i * 5, ground + 1, z1 - 2, p.rail());
            b.put(x0 + 6 + i * 5, ground + 2, z1 - 2, p.rail());
            b.put(x0 + 6 + i * 5, ground + 3, z1 - 2, Blocks.OAK_LEAVES.defaultBlockState());
        }
        for (int i = 0; i < 6; i++) {
            b.put(x0 + 5 + i * 4, ground + 1, z0 + 2, Blocks.FERN.defaultBlockState());
            b.put(x0 + 7 + i * 4, ground + 1, z0 + 2, Blocks.POPPY.defaultBlockState());
        }
        b.chest(x0 + 6, y0 + 1, z0 + 6, SiteLoot.COMMON);
        signAt(b, x1 + 1, ground + 1, z, Direction.EAST, "HERBALIST");
        signAt(b, x0 + 4, y0 + 2, z1 - 4, Direction.SOUTH, "REMEDIES");
        b.put(x0 + 6, y0 + 3, z0 + 6, p.light());
    }

    // ------------------------------------------------------------------ docks

    private static void docks(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        canal(b, cx, cz, ground, p);
        // Quays on both banks.
        paveArea(b, cx + 120, cz + 110, cx + 320, cz + 119, ground, p.foundation(), p);
        paveArea(b, cx + 120, cz + 147, cx + 320, cz + 156, ground, p.foundation(), p);
        // Paths from the north warehouses down to the quay.
        paveArea(b, cx + 150, cz + 100, cx + 160, cz + 110, ground, p.foundation(), p);
        paveArea(b, cx + 275, cz + 100, cx + 285, cz + 110, ground, p.foundation(), p);

        warehouse(b, cx + 155, cz + 75, ground, p, 50, 50, false, Side.S);
        warehouse(b, cx + 280, cz + 70, ground, p, 60, 60, false, Side.S);
        warehouse(b, cx + 260, cz + 191, ground, p, 60, 46, true, Side.N);
        // Fishing sheds.
        pad(b, cx + 131, cz + 169, cx + 149, cz + 191, ground, p);
        b.room(cx + 132, ground + 1, cz + 170, cx + 148, ground + 7, cz + 190);
        b.fill(cx + 132, ground + 1, cz + 170, cx + 148, ground + 1, cz + 190, p.foundation());
        for (int y = ground + 2; y <= ground + 7; y++) {
            b.walls(cx + 132, y, cz + 170, cx + 148, y, cz + 190, p.accent());
        }
        b.pitchedRoof(cx + 130, cz + 168, cx + 150, cz + 192, ground + 7, 3, 0);
        placeDoor(b, p, cx + 132, cz + 170, cx + 148, cz + 190, ground + 2, Side.N, 8);
        signAt(b, cx + 140, ground + 1, cz + 169, Direction.NORTH, "FISH");

        // Rope, crates and barrels along the quay.
        for (int i = 0; i < 8; i++) {
            b.put(cx + 140 + i * 20, ground + 1, cz + 152, p.foundation());
            b.put(cx + 144 + i * 20, ground + 1, cz + 154, Blocks.BARREL.defaultBlockState());
        }
        b.fill(cx + 190, ground + 1, cz + 148, cx + 194, ground + 2, cz + 152,
                Blocks.OAK_FENCE.defaultBlockState());
        signAt(b, cx + 118, ground + 1, cz + 158, Direction.WEST, "DOCKS");
        signAt(b, cx + 200, ground + 1, cz + 158, Direction.SOUTH, "SUMIDA");
        signAt(b, cx + 320, ground + 1, cz + 158, Direction.WEST, "CANAL");
        canalPier(b, cx, cz, ground, p);
    }

    /** A timber wharf on the canal's water edge, reached from the south quay. */
    private static void canalPier(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 300;
        int x1 = cx + 320;
        int z0 = cz + 140;
        int z1 = cz + 146;
        b.fill(x0, ground + 1, z0, x1, ground + 1, z1,
                Blocks.DARK_OAK_PLANKS.defaultBlockState());
        for (int x : new int[]{x0, x0 + 10, x1}) {
            for (int z : new int[]{z0, z0 + 3, z1}) {
                b.fill(x, ground - 2, z, x, ground + 1, z, p.accent());
            }
        }
        b.walls(x0, ground + 1, z0, x1, ground + 1, z1, p.rail());
        b.fill(x0 + 12, ground + 1, z1, x0 + 16, ground + 2, z1, air());
        for (int x : new int[]{x0 + 2, x1 - 2}) {
            b.put(x, ground + 2, z0, Blocks.DARK_OAK_FENCE.defaultBlockState());
            b.put(x, ground + 2, z1, Blocks.DARK_OAK_FENCE.defaultBlockState());
        }
        b.put(x0 + 3, ground + 2, z0 + 2, Blocks.BARREL.defaultBlockState());
        b.put(x0 + 5, ground + 2, z0 + 3, p.foundation());
        b.put(x0 + 6, ground + 2, z0 + 2, p.foundation());
        signAt(b, x0 + 4, ground + 2, z1, Direction.SOUTH, "SUMIDA PORT");
    }

    private static void canal(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 120;
        int x1 = cx + 320;
        int z0 = cz + 120;
        int z1 = cz + 146;
        b.fill(x0, ground - 5, z0, x1, ground - 5, z1, p.rubble());
        b.fill(x0, ground - 4, z0, x1, ground - 1, z1,
                Blocks.WATER.defaultBlockState());
        // Stone quay walls.
        b.fill(x0, ground - 3, z0 - 1, x1, ground, z0 - 1, p.foundation());
        b.fill(x0, ground - 3, z1 + 1, x1, ground, z1 + 1, p.foundation());
        b.fill(x0 - 1, ground - 3, z0, x0 - 1, ground, z1, p.foundation());
        b.fill(x1 + 1, ground - 3, z0, x1 + 1, ground, z1, p.foundation());
        // Mooring posts.
        for (int x = x0 + 10; x <= x1 - 10; x += 30) {
            b.put(x, ground + 1, z0 - 1, Blocks.DARK_OAK_FENCE.defaultBlockState());
            b.put(x, ground + 1, z1 + 1, Blocks.DARK_OAK_FENCE.defaultBlockState());
        }
        boat(b, cx + 160, cz + 133, ground, p);
        boat(b, cx + 220, cz + 133, ground, p);
        boat(b, cx + 280, cz + 133, ground, p);
    }

    private static void boat(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 7, ground, z - 2, x + 7, ground, z + 2, Blocks.DARK_OAK_SLAB.defaultBlockState());
        b.walls(x - 7, ground, z - 2, x + 7, ground + 1, z + 2,
                Blocks.DARK_OAK_PLANKS.defaultBlockState());
        b.put(x - 8, ground, z, Blocks.DARK_OAK_STAIRS.defaultBlockState());
        b.put(x + 8, ground, z, Blocks.DARK_OAK_STAIRS.defaultBlockState());
        b.fill(x, ground + 1, z, x, ground + 7, z, Blocks.DARK_OAK_LOG.defaultBlockState());
        b.fill(x - 3, ground + 3, z, x + 3, ground + 6, z, Materials.wool(DyeColor.WHITE));
        b.put(x, ground + 7, z, p.light());
        b.put(x - 6, ground + 1, z, Blocks.BARREL.defaultBlockState());
    }

    private static void warehouse(StructureBuilder b, int x, int z, int ground, Palette p, int w,
                                  int d, boolean secret, Side doorSide) {
        int x0 = x - w / 2;
        int x1 = x + w / 2;
        int z0 = z - d / 2;
        int z1 = z + d / 2;
        int y0 = ground + 1;
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p);
        b.room(x0, y0, z0, x1, y0 + 11, z1);
        b.fill(x0, y0, z0, x1, y0, z1, p.foundation());
        for (int y = y0 + 1; y <= y0 + 11; y++) {
            b.walls(x0, y, z0, x1, y, z1, Blocks.BRICKS.defaultBlockState());
        }
        b.walls(x0, y0 + 1, z0, x1, y0 + 1, z1, p.foundation());
        b.walls(x0, y0 + 11, z0, x1, y0 + 11, z1, p.accent());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y0 + 11, 5, 0);
        placeDoor(b, p, x0, z0, x1, z1, y0 + 1, doorSide, w / 2);
        if (doorSide == Side.S) {
            signAt(b, x, y0 + 9, z1 + 1, Direction.SOUTH, "WAREHOUSE");
        } else {
            signAt(b, x, y0 + 9, z0 - 1, Direction.NORTH, "WAREHOUSE");
        }
        // Cargo stacks.
        for (int i = 0; i < 6; i++) {
            b.put(x0 + 3 + i * 4, y0 + 1, z1 - 3, Blocks.BARREL.defaultBlockState());
            b.put(x0 + 3 + i * 4, y0 + 1, z1 - 5, p.foundation());
            b.put(x0 + 3 + i * 4, y0 + 2, z1 - 5, p.foundation());
        }
        if (secret) {
            // A hidden back room with the smuggled cargo.
            int sx0 = x1 - 18;
            int sz0 = z1 - 18;
            b.walls(sx0, y0 + 1, sz0, x1 - 2, y0 + 6, z1 - 2, p.accent());
            b.fill(sx0 + 1, y0 + 1, sz0 + 1, x1 - 3, y0 + 1, z1 - 3, p.foundation());
            b.fill(sx0, y0 + 2, z1 - 9, sx0, y0 + 3, z1 - 9, air());
            b.chest(x1 - 5, y0 + 1, z1 - 5, SiteLoot.CULT);
            b.chest(x1 - 9, y0 + 1, z1 - 5, SiteLoot.COMMON);
            signAt(b, x1 - 7, y0 + 2, z1 - 3, Direction.SOUTH, "SMUGGLED", "CARGO");
            b.put(x1 - 7, y0 + 4, z1 - 6, p.light());
        }
        b.put(x0 + 2, y0 + 3, z1 - 1, p.light());
        b.put(x1 - 2, y0 + 3, z1 - 1, p.light());
    }

    // ------------------------------------------------------------------ rail station

    /**
     * The rail station and goods yard on the eastern rim. The core city stays on {@code ground};
     * the yard and its straight, level track approach are graded to the same level with the shared
     * surface, so the transport pass gets a level line to the district edge.
     */
    private static void station(StructureBuilder b, CityPlanContext ctx, Palette p) {
        int cx = ctx.cx();
        int cz = ctx.cz();
        int ground = ctx.ground();
        rimPad(b, ctx, cx + 230, cz - 64, cx + 335, cz - 20, ground, p);
        rimPad(b, ctx, cx + 180, cz - 24, cx + 340, cz - 22, ground, p);

        int bx0 = cx + 240;
        int bx1 = cx + 280;
        int bz0 = cz - 60;
        int bz1 = cz - 36;
        int y0 = ground + 1;
        b.room(bx0, y0, bz0, bx1, y0 + 9, bz1);
        b.fill(bx0, y0, bz0, bx1, y0, bz1, p.foundation());
        for (int y = y0 + 1; y <= y0 + 9; y++) {
            b.walls(bx0, y, bz0, bx1, y, bz1,
                    y <= y0 + 3 ? Blocks.BRICKS.defaultBlockState() : p.wall());
        }
        b.walls(bx0, y0 + 4, bz0, bx1, y0 + 4, bz1, p.accent());
        b.walls(bx0, y0 + 9, bz0, bx1, y0 + 9, bz1, p.accent());
        for (int wx = bx0 + 4; wx <= bx1 - 4; wx += 6) {
            b.window(wx, y0 + 2, bz1, 2, 3, false);
            b.window(wx, y0 + 6, bz1, 2, 3, false);
            b.window(wx, y0 + 2, bz0, 2, 3, false);
        }
        b.pitchedRoof(bx0 - 1, bz0 - 1, bx1 + 1, bz1 + 1, y0 + 9, 5, 0);
        placeDoor(b, p, bx0, bz0, bx1, bz1, y0 + 1, Side.S, 20);
        signAt(b, cx + 260, y0 + 8, bz1 + 1, Direction.SOUTH, "TOKYO STATION");

        // Platform and its canopy.
        b.fill(cx + 230, y0, cz - 36, cx + 290, y0, cz - 25, p.foundation());
        for (int px = cx + 235; px <= cx + 285; px += 10) {
            b.fill(px, y0 + 1, cz - 34, px, y0 + 5, cz - 34, p.accent());
            b.fill(px, y0 + 1, cz - 28, px, y0 + 5, cz - 28, p.accent());
        }
        b.fill(cx + 230, y0 + 6, cz - 35, cx + 290, y0 + 6, cz - 25, p.roofSlab());
        signAt(b, cx + 260, y0 + 1, cz - 25, Direction.SOUTH, "TOKYO STATION");
        signAt(b, cx + 290, y0 + 1, cz - 34, Direction.EAST, "PLATFORM 1");

        // Straight, level track approach to the eastern rim.
        b.fill(cx + 180, ground, cz - 24, cx + 340, ground, cz - 22, p.rubble());
        BlockState rail = Blocks.RAIL.defaultBlockState()
                .setValue(BlockStateProperties.RAIL_SHAPE, RailShape.EAST_WEST);
        b.fill(cx + 180, ground + 1, cz - 23, cx + 340, ground + 1, cz - 23, rail);
        b.fill(cx + 180, ground + 1, cz - 24, cx + 180, ground + 2, cz - 22, p.accent());
        b.put(cx + 340, ground + 2, cz - 23, p.light());

        // Goods shed, water tower and coal heap.
        b.room(cx + 300, y0, cz - 60, cx + 330, y0 + 7, cz - 40);
        b.fill(cx + 300, y0, cz - 60, cx + 330, y0, cz - 40, p.foundation());
        for (int y = y0 + 1; y <= y0 + 7; y++) {
            b.walls(cx + 300, y, cz - 60, cx + 330, y, cz - 40,
                    Blocks.BRICKS.defaultBlockState());
        }
        b.pitchedRoof(cx + 299, cz - 61, cx + 331, cz - 39, y0 + 7, 4, 0);
        placeDoor(b, p, cx + 300, cz - 60, cx + 330, cz - 40, y0 + 1, Side.S, 15);
        signAt(b, cx + 315, y0 + 6, cz - 39, Direction.SOUTH, "GOODS");

        b.fill(cx + 232, y0, cz - 58, cx + 237, y0 + 1, cz - 53, p.foundation());
        for (int wx : new int[]{cx + 232, cx + 237}) {
            for (int wz : new int[]{cz - 58, cz - 53}) {
                b.fill(wx, y0 + 1, wz, wx, y0 + 9, wz, p.accent());
            }
        }
        b.fill(cx + 231, y0 + 10, cz - 59, cx + 238, y0 + 12, cz - 52, p.foundation());
        b.fill(cx + 231, y0 + 13, cz - 59, cx + 238, y0 + 13, cz - 52, p.roofSlab());
        b.fill(cx + 285, y0, cz - 58, cx + 295, y0 + 1, cz - 52, p.rubble());

        // Yard fence, with the track and the south gate left open.
        b.walls(cx + 230, y0, cz - 64, cx + 335, y0 + 1, cz - 20, p.rail());
        b.fill(cx + 230, y0, cz - 24, cx + 230, y0 + 1, cz - 22, air());
        b.fill(cx + 335, y0, cz - 24, cx + 335, y0 + 1, cz - 22, air());
        b.fill(cx + 260, y0, cz - 20, cx + 270, y0 + 1, cz - 20, air());
        b.put(cx + 265, y0, cz - 64, p.light());
        signAt(b, cx + 265, y0, cz - 21, Direction.SOUTH, "RAIL YARD");

        // A short path from the yard gate into the docks district.
        paveArea(b, cx + 260, cz - 20, cx + 270, cz + 20, ground, p.ground(), p);
    }

    /**
     * Grades one authored yard/track column to {@code base} using the shared surface. This is used
     * only for the station shelf on the rim; the core city never reads terrain.
     */
    private static void rimPad(StructureBuilder b, CityPlanContext ctx, int x0, int z0, int x1,
                               int z1, int base, Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                int surface = ctx.surface().at(x, z);
                if (surface > base) {
                    b.fill(x, base + 1, z, x, surface + 1, z, air());
                } else if (surface < base) {
                    b.fill(x, surface + 1, z, x, base, z, p.foundation());
                }
                b.put(x, base, z, p.foundation());
            }
        }
    }

    // ------------------------------------------------------------------ shrine hill

    private static void shrineHill(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        hillTier(b, cx, cz, ground, -52, -282, 52, -142, 3, p);
        hillTier(b, cx, cz, ground, -38, -264, 38, -156, 6, p);
        hillTier(b, cx, cz, ground, -26, -246, 26, -174, 9, p);
        b.fill(cx - 20, ground + 10, cz - 240, cx + 20, ground + 10, cz - 196, p.foundation());
        shrinePath(b, cx, cz, ground, p);
        shrine(b, cx + SHRINE_X, cz + SHRINE_Z, ground + 10, p);
        torii(b, cx, cz - 150, ground + 3, p);
        torii(b, cx, cz - 172, ground + 7, p);
        torii(b, cx, cz - 190, ground + 10, p);
        torii(b, cx, cz - 204, ground + 11, p);
        signAt(b, cx - 3, ground + 1, cz - 138, Direction.SOUTH, "SHRINE HILL");
        signAt(b, cx + 5, ground + 7, cz - 176, Direction.EAST, "TORII PATH");
        cherryTree(b, cx - 18, cz - 160, ground + 3, p);
        cherryTree(b, cx + 18, cz - 160, ground + 3, p);
        stoneLantern(b, cx - 8, cz - 210, ground + 10, p);
        stoneLantern(b, cx + 8, cz - 210, ground + 10, p);
    }

    private static void hillTier(StructureBuilder b, int cx, int cz, int ground, int x0, int z0,
                                 int x1, int z1, int top, Palette p) {
        b.fill(cx + x0, ground + 1, cz + z0, cx + x1, ground + top, cz + z1,
                Blocks.DIRT.defaultBlockState());
        b.fill(cx + x0, ground + top, cz + z0, cx + x1, ground + top, cz + z1,
                Blocks.GRASS_BLOCK.defaultBlockState());
        b.walls(cx + x0, ground + top - 1, cz + z0, cx + x1, ground + top, cz + z1,
                p.foundation());
    }

    private static void shrinePath(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        for (int z = -140; z >= -196; z--) {
            int y = ground + 1 + ((-z - 140) * 9 / 56);
            for (int x = -3; x <= 3; x++) {
                b.fill(cx + x, ground + 1, cz + z, cx + x, y, cz + z, p.foundation());
                b.put(cx + x, y, cz + z, p.foundation());
                b.fill(cx + x, y + 1, cz + z, cx + x, y + 4, cz + z, air());
            }
        }
    }

    private static void torii(StructureBuilder b, int x, int z, int y, Palette p) {
        BlockState red = Materials.concrete(DyeColor.RED);
        b.fill(x - 5, y - 1, z, x + 5, y - 1, z, p.foundation());
        b.fill(x - 4, y, z, x - 3, y + 5, z, red);
        b.fill(x + 3, y, z, x + 4, y + 5, z, red);
        b.fill(x - 5, y + 6, z, x + 5, y + 6, z, red);
        b.fill(x - 4, y + 7, z, x + 4, y + 7, z, p.roof());
        b.put(x - 5, y + 7, z, p.roofSlab());
        b.put(x + 5, y + 7, z, p.roofSlab());
        b.fill(x - 4, y + 4, z, x + 4, y + 4, z, red);
        b.put(x, y + 5, z, p.accent());
    }

    private static void shrine(StructureBuilder b, int x, int z, int y0, Palette p) {
        int x0 = x - 12;
        int x1 = x + 12;
        int z0 = z - 10;
        int z1 = z + 10;
        b.room(x0, y0, z0, x1, y0 + 8, z1);
        b.fill(x0, y0, z0, x1, y0, z1, p.foundation());
        for (int y = y0 + 1; y <= y0 + 8; y++) {
            b.walls(x0, y, z0, x1, y, z1, p.accent());
        }
        b.walls(x0, y0 + 4, z0, x1, y0 + 4, z1, p.wall());
        b.pitchedRoof(x0 - 3, z0 - 3, x1 + 3, z1 + 3, y0 + 8, 5, 0);
        placeDoor(b, p, x0, z0, x1, z1, y0 + 1, Side.S, 12);
        // Offering box beside the door, and the bell above it.
        b.fill(x - 6, y0 + 1, z1 + 1, x - 2, y0 + 1, z1 + 2, p.accent());
        b.put(x, y0 + 6, z1, Blocks.BELL.defaultBlockState());
        // Stone foxes.
        b.fill(x - 7, y0 + 1, z1 + 2, x - 6, y0 + 3, z1 + 2, Blocks.STONE_BRICKS.defaultBlockState());
        b.fill(x + 6, y0 + 1, z1 + 2, x + 7, y0 + 3, z1 + 2, Blocks.STONE_BRICKS.defaultBlockState());
        b.put(x - 7, y0 + 4, z1 + 2, Blocks.STONE_BRICK_SLAB.defaultBlockState());
        b.put(x + 7, y0 + 4, z1 + 2, Blocks.STONE_BRICK_SLAB.defaultBlockState());
        signAt(b, x + 10, y0 + 1, z1 + 3, Direction.SOUTH, "MISSING MIKO");
        signAt(b, x - 10, y0 + 1, z1 + 3, Direction.SOUTH, "SHRINE");
        b.put(x - 10, y0 + 3, z1, p.light());
        b.put(x + 10, y0 + 3, z1, p.light());
    }

    // ------------------------------------------------------------------ crypt

    private static void crypt(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x = cx + PAGODA_X;
        int z = cz + PAGODA_Z;
        int floor = ground - 9;
        int ceiling = ground - 4;
        // Chamber under the pagoda.
        b.fill(x - 14, floor - 1, z - 16, x + 14, floor - 1, z + 16, p.foundation());
        b.fill(x - 14, floor, z - 16, x + 14, ceiling, z + 16, air());
        b.walls(x - 14, floor, z - 16, x + 14, ceiling, z + 16,
                Blocks.STONE_BRICKS.defaultBlockState());
        b.fill(x - 14, ceiling, z - 16, x + 14, ceiling, z + 16,
                Blocks.STONE_BRICKS.defaultBlockState());
        b.fill(x - 14, floor, z - 16, x + 14, floor, z + 16, p.foundation());
        // Pillars.
        for (int px : new int[]{-10, 10}) {
            for (int pz : new int[]{-12, 0, 12}) {
                b.fill(x + px, floor + 1, z + pz, x + px, ceiling - 1, z + pz, p.accent());
            }
        }
        // Descending entrance stair on the east side: ground level at x+24, chamber at x+15.
        for (int i = 0; i <= 9; i++) {
            int sx = x + 15 + i;
            int sy = floor + i;
            b.fill(sx, sy - 1, z - 3, sx, sy - 1, z + 3, p.foundation());
            b.fill(sx, sy, z - 3, sx, sy + 3, z + 3, air());
        }
        // Open the chamber's east wall into the stair foot.
        b.fill(x + 14, floor + 1, z - 3, x + 14, floor + 3, z + 3, air());
        b.fill(x + 14, floor, z - 4, x + 14, floor, z + 4, p.foundation());
        // A stone rim around the stair mouth.
        b.fill(x + 15, ground + 1, z - 4, x + 24, ground + 1, z - 4,
                Blocks.STONE_BRICKS.defaultBlockState());
        b.fill(x + 15, ground + 1, z + 4, x + 24, ground + 1, z + 4,
                Blocks.STONE_BRICKS.defaultBlockState());
        // Sarcophagus, chest and lore.
        b.fill(x - 4, floor + 1, z - 2, x + 4, floor + 1, z + 2,
                Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        b.fill(x - 3, floor + 2, z - 1, x + 3, floor + 2, z + 1,
                Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        b.chest(x + 8, floor + 1, z + 10, SiteLoot.RELIC);
        signAt(b, x + 8, floor + 1, z + 6, Direction.SOUTH, "CRYPT BELOW", "DO NOT ENTER");
        b.put(x - 8, floor + 3, z - 10, p.light());
        b.put(x + 8, floor + 3, z - 10, p.light());
        b.put(x - 12, floor + 2, z + 14, Palette.cobweb());
        b.put(x + 12, floor + 2, z + 14, Palette.cobweb());
        // A warning at the stair mouth.
        signAt(b, x + 25, ground + 1, z, Direction.EAST, "CRYPT", "KEEP OUT");
    }

    // ------------------------------------------------------------------ dressing

    private static void dressing(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Telegraph poles down the avenue.
        for (int z = -120; z <= 300; z += 40) {
            telegraphPole(b, cx - 8, cz + z, ground, p);
        }
        // Paper lanterns on the avenue.
        for (int z = -130; z <= 290; z += 60) {
            lanternPost(b, cx - 9, cz + z, ground, p);
            lanternPost(b, cx + 9, cz + z, ground, p);
        }
        // Cherry trees in the open squares and along the temple walk.
        cherryTree(b, cx - 40, cz + 36, ground, p);
        cherryTree(b, cx + 30, cz + 36, ground, p);
        cherryTree(b, cx + 100, cz + 36, ground, p);
        cherryTree(b, cx - 96, cz + 53, ground, p);
        cherryTree(b, cx + 120, cz + 150, ground, p);
        cherryTree(b, cx + 20, cz - 120, ground, p);
        cherryTree(b, cx - 20, cz - 120, ground, p);
        // Market stalls beside the notice board.
        stall(b, cx - 13, cz + 58, ground, p, true);
        stall(b, cx - 13, cz + 78, ground, p, false);
        // A rickshaw in the avenue, and a few crates.
        rickshaw(b, cx - 2, cz + 92, ground, p);
        b.put(cx + 2, ground + 1, cz + 64, p.foundation());
        b.put(cx + 3, ground + 1, cz + 64, p.foundation());
        b.put(cx + 2, ground + 2, cz + 64, p.foundation());
    }

    private static void telegraphPole(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x, ground + 1, z, x, ground + 8, z, p.accent());
        b.fill(x - 2, ground + 7, z, x + 2, ground + 7, z, p.rail());
        b.put(x - 2, ground + 8, z, Materials.concrete(DyeColor.WHITE));
        b.put(x + 2, ground + 8, z, Materials.concrete(DyeColor.WHITE));
        b.put(x, ground + 9, z, p.light());
    }

    private static void lanternPost(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground, z, p.foundation());
        b.fill(x, ground + 1, z, x, ground + 3, z, p.accent());
        b.put(x, ground + 4, z, p.roofSlab());
        b.put(x, ground + 3, z, p.light());
        b.put(x + 1, ground + 3, z, p.light());
    }

    private static void stoneLantern(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 1, ground, z - 1, x + 1, ground, z + 1, p.foundation());
        b.fill(x, ground + 1, z, x, ground + 2, z, p.foundation());
        b.put(x, ground + 3, z, p.light());
        b.fill(x - 1, ground + 4, z - 1, x + 1, ground + 4, z + 1, p.roofSlab());
    }

    private static void cherryTree(StructureBuilder b, int x, int z, int ground, Palette p) {
        int h = 4 + Math.abs((x + z) % 3);
        b.fill(x, ground + 1, z, x, ground + h, z, Blocks.CHERRY_LOG.defaultBlockState());
        b.fill(x - 2, ground + h - 1, z - 2, x + 2, ground + h + 1, z + 2,
                Blocks.CHERRY_LEAVES.defaultBlockState());
        b.fill(x - 1, ground + h + 2, z - 1, x + 1, ground + h + 2, z + 1,
                Blocks.CHERRY_LEAVES.defaultBlockState());
        b.put(x, ground + h + 3, z, Blocks.CHERRY_LEAVES.defaultBlockState());
        b.put(x, ground, z, p.ground());
    }

    private static void rickshaw(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x, ground + 1, z, x + 3, ground + 1, z + 2, p.accent());
        b.put(x, ground + 2, z, p.rail());
        b.put(x + 3, ground + 2, z, p.rail());
        b.put(x, ground + 2, z + 2, p.rail());
        b.put(x + 3, ground + 2, z + 2, p.rail());
        b.fill(x, ground + 3, z, x + 3, ground + 3, z + 2, Materials.wool(DyeColor.RED));
        b.put(x - 1, ground + 1, z, Blocks.DARK_OAK_FENCE.defaultBlockState());
        b.put(x + 4, ground + 1, z, Blocks.DARK_OAK_FENCE.defaultBlockState());
        b.put(x - 1, ground, z, p.foundation());
        b.put(x + 4, ground, z, p.foundation());
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
