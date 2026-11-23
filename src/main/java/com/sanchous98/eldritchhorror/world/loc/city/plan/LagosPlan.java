package com.sanchous98.eldritchhorror.world.loc.city.plan;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.SiteLoot;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.style.Materials;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Authored colonial Lagos: the Oba's walled courtyard palace, the market lanes and their striped
 * awnings, the marina and lagoon with its quays, canoes and warehouses, the whitewashed colonial
 * grid, and the stilt lagoon shrine that hides the oil ledger's last page.
 *
 * <p>Everything is placed deliberately through {@link StructureBuilder}: no layout RNG, no chunk
 * reads, no static mutable state. Coordinates are offsets from {@code (cx, ground, cz)}; the
 * water, streets and quays are authored first, then each district, so gaps between buildings are
 * gardens, yards, ruins or water rather than filler.
 */
public final class LagosPlan implements CityPlan {

    /** Authored half-extent: 680 blocks across. */
    private static final int HALF = 340;

    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState LILY = Blocks.LILY_PAD.defaultBlockState();
    private static final BlockState LEAVES = Blocks.JUNGLE_LEAVES.defaultBlockState();
    private static final BlockState GOLD = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final BlockState NOTE = Blocks.NOTE_BLOCK.defaultBlockState();
    private static final BlockState BOOKS = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState BARREL = Blocks.BARREL.defaultBlockState();
    private static final BlockState KELP = Blocks.DRIED_KELP_BLOCK.defaultBlockState();
    private static final BlockState HAY = Blocks.HAY_BLOCK.defaultBlockState();
    private static final BlockState FARMLAND = Blocks.FARMLAND.defaultBlockState();
    private static final BlockState WHEAT = Blocks.WHEAT.defaultBlockState();
    private static final BlockState RED_BED = Blocks.WOOL.pick(DyeColor.RED).defaultBlockState();
    private static final BlockState WHITE = Materials.whiteConcrete();
    private static final BlockState CYAN_GLASS = Materials.stainedGlass(DyeColor.CYAN);
    private static final BlockState MAGENTA_GLASS = Materials.stainedGlass(DyeColor.MAGENTA);

    /** Market awning canvas, rotated deterministically by stall index. */
    private static final List<DyeColor> AWNINGS = List.of(
            DyeColor.RED, DyeColor.YELLOW, DyeColor.ORANGE, DyeColor.LIME, DyeColor.CYAN
    );

    @Override
    public String cityId() {
        return "lagos";
    }

    @Override
    public int halfExtent() {
        return HALF;
    }

    @Override
    public void build(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        buildCity(b, cx, cz, ground, (x, z) -> ground, p);
    }

    @Override
    public void build(StructureBuilder b, CityPlanContext ctx, Palette p) {
        buildCity(b, ctx.cx(), ctx.cz(), ctx.ground(), ctx.surface()::at, p);
    }

    /** The graded city surface, or the flat heart level on the legacy entry point. */
    @FunctionalInterface
    private interface Surface {
        int at(int x, int z);
    }

    private static void buildCity(StructureBuilder b, int cx, int cz, int ground,
                                  Surface surface, Palette p) {
        water(b, cx, cz, ground, p);
        streets(b, cx, cz, ground, p);
        arrivalGate(b, cx, cz, ground, surface, p);
        obaPalace(b, cx, cz, ground, p);
        market(b, cx, cz, ground, p);
        marina(b, cx, cz, ground, p);
        colonialQuarter(b, cx, cz, ground, p);
        lagoonShrine(b, cx, cz, ground, p);
        southShore(b, cx, cz, ground, surface, p);
        railStation(b, cx, cz, ground, surface, p);
        b.marker("lagos_center", cx, ground + 1, cz);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.LANDMARK, 0, 0, 320, "lagos gate"),
                new CityPoi("gate_signpost", CityPoi.Kind.SIGN, 14, 1, 330,
                        "OBA'S PALACE|MARKET|MARINA|COLONIAL QTR"),
                new CityPoi("city_notices", CityPoi.Kind.SIGN, -20, 1, 331, "CITY NOTICES"),
                new CityPoi("palace_gate", CityPoi.Kind.LANDMARK, 0, 0, -80, "palace_guard"),
                new CityPoi("palace_guard", CityPoi.Kind.NPC, 4, 0, -76, "palace_guard"),
                new CityPoi("oba_palace", CityPoi.Kind.LANDMARK, 0, 0, -200, "colonnaded hall"),
                new CityPoi("palace_shrine", CityPoi.Kind.LANDMARK, 0, 0, -252,
                        "ancestral shrine"),
                new CityPoi("priest", CityPoi.Kind.NPC, 0, 0, -244, "priest"),
                new CityPoi("market_quest_board", CityPoi.Kind.QUEST_BOARD, -220, 0, 0,
                        "lagos_oil_ledger;lagos_missing_fisherman"),
                new CityPoi("market_shop", CityPoi.Kind.SHOP, -240, 0, -60, "merchant"),
                new CityPoi("merchant", CityPoi.Kind.NPC, -238, 0, -60, "market_shop"),
                new CityPoi("herbalist", CityPoi.Kind.HEALER, -240, 0, 120, "herbalist"),
                new CityPoi("herbalist_npc", CityPoi.Kind.NPC, -238, 0, 120, "herbalist"),
                new CityPoi("colonial_hotel", CityPoi.Kind.INN, -80, 0, -120, "hotel_keeper"),
                new CityPoi("hotel_keeper", CityPoi.Kind.NPC, -80, 0, -116, "colonial_hotel"),
                new CityPoi("oil_office", CityPoi.Kind.NPC, 220, 0, -120,
                        "oil_clerk;lagos_oil_ledger"),
                new CityPoi("oil_clerk", CityPoi.Kind.NPC, 218, 0, -118, "lagos_oil_ledger"),
                new CityPoi("marina_quay", CityPoi.Kind.LANDMARK, 220, 0, 40, "fish market"),
                new CityPoi("lagos_port", CityPoi.Kind.PORT, 254, 0, 40, "marina_basin"),
                new CityPoi("lagos_station", CityPoi.Kind.STATION, -116, 1, 305,
                        "lagos_junction;north_south"),
                new CityPoi("fishmonger", CityPoi.Kind.NPC, 240, 0, 40, "fish market"),
                new CityPoi("fisherman", CityPoi.Kind.NPC, 240, 0, 120,
                        "lagos_missing_fisherman"),
                new CityPoi("lagoon_shrine", CityPoi.Kind.SECRET, 240, 0, 260,
                        "lagos_missing_fisherman;offerings"),
                new CityPoi("lagoon_warning", CityPoi.Kind.SIGN, 240, 1, 212, "LAGOON"),
                new CityPoi("marina_sign", CityPoi.Kind.SIGN, 220, 1, 196, "MARINA"),
                new CityPoi("market_sign", CityPoi.Kind.SIGN, -216, 1, -124, "MARKET"),
                new CityPoi("colonial_sign", CityPoi.Kind.SIGN, -84, 1, -56, "COLONIAL QTR"),
                new CityPoi("church", CityPoi.Kind.LANDMARK, 30, 0, -26, "church"),
                new CityPoi("club", CityPoi.Kind.LANDMARK, -36, 0, 20, "lagos club"),
                new CityPoi("cloth_shop", CityPoi.Kind.SHOP, -202, 0, 144, "tailor"));
    }

    // ------------------------------------------------------------------ water

    /** The marina basin, the southern lagoon and the market drain that feeds them. */
    private static void water(StructureBuilder b, int cx, int cz, int g, Palette p) {
        basin(b, cx + 256, cz - 244, cx + 332, cz + 196, g);
        basin(b, cx + 150, cz + 230, cx + 332, cz + 326, g);
        // Stone quay wall along the marina basin's city side.
        b.fill(cx + 254, g - 4, cz - 245, cx + 255, g + 1, cz + 197, p.foundation());
        for (int z = cz - 240; z <= cz + 190; z += 8) {
            b.put(cx + 254, g + 2, z, p.accent());
        }
        for (int z = cz - 232; z <= cz + 184; z += 32) {
            b.put(cx + 254, g + 3, z, p.light());
        }
        canal(b, cx, cz, g, p);
    }

    /** One water body: a sand bed under two courses of water. */
    private static void basin(StructureBuilder b, int x0, int z0, int x1, int z1, int g) {
        b.fill(x0, g - 5, z0, x1, g - 5, z1, SAND);
        b.fill(x0, g - 4, z0, x1, g - 1, z1, WATER);
    }

    /** A north-south market drain: stone banks, two courses of water, lilies and reeds. */
    private static void canal(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 134;
        int x1 = cx - 127;
        int z0 = cz - 244;
        int z1 = cz + 204;
        b.fill(x0, g - 3, z0, x1, g - 3, z1, SAND);
        b.fill(x0, g - 2, z0, x1, g - 1, z1, WATER);
        b.fill(x0 - 2, g - 3, z0, x0 - 1, g + 1, z1, p.foundation());
        b.fill(x1 + 1, g - 3, z0, x1 + 2, g + 1, z1, p.foundation());
        for (int z = z0 + 8; z <= z1 - 8; z += 11) {
            b.put(cx - 131, g, cz + z, LILY);
            b.put(cx - 135, g + 1, cz + z, LEAVES);
        }
    }

    /** A timber footbridge over the canal at {@code dz}; the street deck is already laid. */
    private static void canalBridge(StructureBuilder b, int cx, int cz, int g, Palette p, int dz) {
        int z0 = cz + dz - 2;
        int z1 = cz + dz + 3;
        b.fill(cx - 137, g + 1, z0, cx - 124, g + 1, z1, AIR);
        b.fill(cx - 137, g, z0, cx - 124, g, z1, p.frame());
        b.fill(cx - 137, g + 1, z0, cx - 124, g + 1, z0, p.rail());
        b.fill(cx - 137, g + 1, z1, cx - 124, g + 1, z1, p.rail());
        for (int x = cx - 136; x <= cx - 125; x += 3) {
            b.fill(x, g - 1, z0, x, g, z0, p.accent());
            b.fill(x, g - 1, z1, x, g, z1, p.accent());
        }
    }

    // ------------------------------------------------------------------ streets

    private static void streets(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // Processional axis: arrival gate to the palace's inner gate.
        CityPlanSupport.path(b, cx, cz + 336, cx, cz - 150, 14, g, p.ground());
        for (int z = -150; z <= 336; z++) {
            b.put(cx - 7, g, cz + z, p.foundation());
            b.put(cx + 8, g, cz + z, p.foundation());
        }
        // Waterfront road on the strip between the marina basin and the lagoon.
        CityPlanSupport.path(b, cx - 320, cz + 210, cx + 320, cz + 210, 10, g, p.ground());
        for (int x = -320; x <= 320; x++) {
            b.put(cx + x, g, cz + 205, p.foundation());
            b.put(cx + x, g, cz + 216, p.foundation());
        }
        // Market lanes: two north-south, three east-west, all six wide.
        CityPlanSupport.path(b, cx - 220, cz - 240, cx - 220, cz + 210, 6, g, p.ground());
        CityPlanSupport.path(b, cx - 282, cz - 240, cx - 282, cz + 210, 6, g, p.foundation());
        CityPlanSupport.path(b, cx - 118, cz - 240, cx - 118, cz + 210, 6, g, p.ground());
        CityPlanSupport.path(b, cx - 316, cz, cx, cz, 6, g, p.ground());
        CityPlanSupport.path(b, cx - 316, cz - 120, cx - 140, cz - 120, 6, g, p.ground());
        CityPlanSupport.path(b, cx - 316, cz + 110, cx - 118, cz + 110, 6, g, p.ground());
        canalBridge(b, cx, cz, g, p, 0);
        canalBridge(b, cx, cz, g, p, 110);
        // Colonial grid: two north-south, three east-west, all eight wide.
        CityPlanSupport.path(b, cx - 88, cz - 140, cx - 88, cz + 200, 8, g, p.ground());
        CityPlanSupport.path(b, cx + 60, cz - 140, cx + 60, cz + 200, 8, g, p.ground());
        CityPlanSupport.path(b, cx - 120, cz - 60, cx + 120, cz - 60, 8, g, p.ground());
        CityPlanSupport.path(b, cx - 120, cz + 60, cx + 216, cz + 60, 8, g, p.ground());
        CityPlanSupport.path(b, cx - 120, cz + 140, cx + 120, cz + 140, 8, g, p.ground());
        // Marina approach from the colonial offices to the oil quay.
        CityPlanSupport.path(b, cx + 60, cz - 140, cx + 216, cz - 140, 8, g, p.ground());
        // Arrival plaza.
        b.fill(cx - 60, g, cz + 216, cx + 60, g, cz + 336, p.ground());
        outline(b, cx - 60, cz + 216, cx + 60, cz + 336, g, p.foundation());
    }

    // ------------------------------------------------------------------ arrival

    private static void arrivalGate(StructureBuilder b, int cx, int cz, int g, Surface surface,
                                    Palette p) {
        int gy = Math.max(g, surface.at(cx, cz + 320));
        // A plinth under the gate, so the rim-built gate never floats over the plaza.
        b.fill(cx - 22, Math.min(g, gy) - 1, cz + 312, cx + 22, gy, cz + 328, p.foundation());
        // A short ramp from the plaza up to the gate floor.
        for (int z = cz + 328; z <= cz + 336; z++) {
            int h = g + (cz + 336 - z) * (gy - g) / 8;
            b.fill(cx - 10, g, z, cx + 10, h, z, p.foundation());
        }
        gateTower(b, cx - 22, cz + 312, cx - 10, cz + 328, gy, p);
        gateTower(b, cx + 10, cz + 312, cx + 22, cz + 328, gy, p);
        // The lintel and its painted fascia.
        b.fill(cx - 10, gy + 7, cz + 316, cx + 10, gy + 9, cz + 324, p.wall());
        b.fill(cx - 10, gy + 8, cz + 315, cx + 10, gy + 8, cz + 325, p.accent());
        b.fill(cx - 10, gy + 10, cz + 315, cx + 10, gy + 10, cz + 325, p.roofSlab());
        b.fill(cx - 9, gy + 9, cz + 324, cx + 9, gy + 9, cz + 324,
                Materials.wool(DyeColor.YELLOW));
        wallSign(b, cx, gy + 11, cz + 324, Direction.SOUTH, "LAGOS");
        // Signposts and the city notice board.
        signPost(b, cx + 14, cz + 330, g, p, Direction.SOUTH,
                "OBA'S PALACE", "MARKET", "MARINA", "COLONIAL QTR");
        signPost(b, cx - 44, cz + 330, g, p, Direction.SOUTH,
                "LAGOON", "CITY NOTICES", "KEEP LEFT");
        b.fill(cx - 30, g + 1, cz + 331, cx - 30, g + 3, cz + 331, p.accent());
        b.fill(cx - 14, g + 1, cz + 331, cx - 14, g + 3, cz + 331, p.accent());
        b.fill(cx - 30, g + 1, cz + 331, cx - 14, g + 3, cz + 331, p.accent());
        b.fill(cx - 31, g + 4, cz + 330, cx - 13, g + 4, cz + 332, p.roofSlab());
        wallSign(b, cx - 26, g + 3, cz + 331, Direction.SOUTH,
                "CITY NOTICES", "CURFEW AT DUSK");
        wallSign(b, cx - 18, g + 3, cz + 331, Direction.SOUTH,
                "KEEP TO PATHS", "NO FIRES");
        // Plaza furniture: palms, a monument and the caravan stable.
        palm(b, cx - 52, cz + 228, g, p);
        palm(b, cx + 52, cz + 228, g, p);
        palm(b, cx - 50, cz + 282, g, p);
        palm(b, cx + 52, cz + 300, g, p);
        palm(b, cx + 30, cz + 250, g, p);
        palm(b, cx - 30, cz + 250, g, p);
        b.monument(cx + 40, cz + 292, g + 1);
        house(b, cx - 58, cz + 246, cx - 32, cz + 272, g, p, p.wall(), g + 4, true,
                Direction.SOUTH, "STRANGERS' YARD");
        house(b, cx + 32, cz + 246, cx + 58, cz + 272, g, p, p.wall(), g + 4, true,
                Direction.SOUTH, "TRADERS' REST");
        // Open stable: four posts, a long roof, hay and a trough.
        for (int x = cx - 56; x <= cx - 34; x += 7) {
            b.fill(x, g + 1, cz + 288, x, g + 4, cz + 288, p.accent());
            b.fill(x, g + 1, cz + 308, x, g + 4, cz + 308, p.accent());
        }
        b.fill(cx - 58, g + 5, cz + 286, cx - 32, g + 5, cz + 310, p.roof());
        b.pitchedRoof(cx - 58, cz + 286, cx - 32, cz + 310, g + 5, 4, 1);
        b.fill(cx - 52, g + 1, cz + 292, cx - 48, g + 1, cz + 294, HAY);
        b.fill(cx - 42, g + 1, cz + 292, cx - 38, g + 1, cz + 294, HAY);
        b.fill(cx - 47, g, cz + 302, cx - 43, g, cz + 304, WATER);
        crateStack(b, cx - 54, cz + 304, g, p, 2);
        crateStack(b, cx - 36, cz + 302, g, p, 1);
        lamp(b, cx - 24, cz + 320, g, p);
        lamp(b, cx + 24, cz + 320, g, p);
    }

    /** A gate tower: a mudbrick shaft with painted bands, crenellations and a metal roof. */
    private static void gateTower(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                  Palette p) {
        b.room(x0, g, z0, x1, g + 11, z1);
        b.fill(x0, g, z0, x1, g, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, g + 1, g + 11, p);
        b.walls(x0, g + 4, z0, x1, g + 4, z1, p.accent());
        b.walls(x0, g + 9, z0, x1, g + 9, z1, p.accent());
        b.window((x0 + x1) / 2, g + 6, z0, 2, 1, true);
        b.window((x0 + x1) / 2, g + 6, z1, 2, 1, true);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 12, 4, 0);
        b.put(x0 + 1, g + 13, z0 + 1, p.light());
    }

    // ------------------------------------------------------------------ palace

    private static void obaPalace(StructureBuilder b, int cx, int cz, int g, Palette p) {
        palaceWall(b, cx, cz, g, p);
        forecourt(b, cx, cz, g, p);
        palaceHall(b, cx, cz, g, p);
        palaceCourtyard(b, cx, cz, g, p);
        palaceShrine(b, cx, cz, g, p);
        palaceQuarters(b, cx, cz, g, p);
    }

    /** The compound wall with its inner gate, painted band and crenellated cap. */
    private static void palaceWall(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 110;
        int x1 = cx + 110;
        int z0 = cz - 300;
        int z1 = cz - 150;
        b.walls(x0, g + 1, z0, x1, g + 6, z1, p.wall());
        b.walls(x0, g + 4, z0, x1, g + 4, z1, p.accent());
        b.crenellations(x0, z0, x1, z1, g + 7);
        // Inner gate on the south wall, flanked by two towers.
        b.fill(cx - 12, g + 1, z1, cx + 12, g + 6, z1, AIR);
        gateTower(b, cx - 26, cz - 158, cx - 14, cz - 142, g, p);
        gateTower(b, cx + 14, cz - 158, cx + 26, cz - 142, g, p);
        b.fill(cx - 12, g + 7, cz - 152, cx + 12, g + 9, cz - 148, p.wall());
        b.fill(cx - 12, g + 8, cz - 153, cx + 12, g + 8, cz - 147, p.accent());
        b.fill(cx - 12, g + 10, cz - 153, cx + 12, g + 10, cz - 147, p.roofSlab());
        wallSign(b, cx, g + 11, cz - 147, Direction.SOUTH, "OBA'S PALACE");
    }

    /** The walled processional forecourt between the outer arch and the inner gate. */
    private static void forecourt(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx - 52, g, cz - 150, cx + 52, g, cz - 80, p.ground());
        outline(b, cx - 52, cz - 150, cx + 52, cz - 80, g, p.foundation());
        b.walls(cx - 52, g + 1, cz - 150, cx - 52, g + 2, cz - 80, p.weathered());
        b.walls(cx + 52, g + 1, cz - 150, cx + 52, g + 2, cz - 80, p.weathered());
        b.fill(cx - 52, g + 1, cz - 98, cx - 52, g + 2, cz - 94, AIR);
        b.fill(cx + 52, g + 1, cz - 122, cx + 52, g + 2, cz - 118, AIR);
        b.fill(cx - 58, g, cz - 98, cx - 52, g, cz - 94, p.ground());
        b.fill(cx + 52, g, cz - 122, cx + 57, g, cz - 118, p.ground());
        b.monument(cx - 16, cz - 96, g + 1);
        b.monument(cx + 16, cz - 96, g + 1);
        palm(b, cx - 46, cz - 144, g, p);
        palm(b, cx + 46, cz - 144, g, p);
        palm(b, cx - 46, cz - 90, g, p);
        palm(b, cx + 46, cz - 90, g, p);
        // Outer arch: the palace gate proper.
        gateTower(b, cx - 24, cz - 86, cx - 12, cz - 74, g, p);
        gateTower(b, cx + 12, cz - 86, cx + 24, cz - 74, g, p);
        b.fill(cx - 12, g + 7, cz - 84, cx + 12, g + 9, cz - 76, p.wall());
        b.fill(cx - 12, g + 8, cz - 85, cx + 12, g + 8, cz - 75, p.accent());
        b.fill(cx - 12, g + 10, cz - 85, cx + 12, g + 10, cz - 75, p.roofSlab());
        wallSign(b, cx, g + 11, cz - 75, Direction.SOUTH, "OBA'S PALACE");
        wallSign(b, cx - 12, g + 4, cz - 74, Direction.SOUTH, "NO WEAPONS");
        // Guard shelter just outside the arch.
        b.fill(cx + 16, g + 1, cz - 70, cx + 16, g + 3, cz - 70, p.accent());
        b.fill(cx + 16, g + 4, cz - 72, cx + 22, g + 4, cz - 68, p.roofSlab());
        b.put(cx + 20, g + 1, cz - 70, p.frame());
        b.put(cx + 18, g + 2, cz - 70, p.light());
        signPost(b, cx - 24, cz - 72, g, p, Direction.SOUTH, "OBA'S PALACE", "NO WEAPONS");
    }

    /** The colonnaded hall under its pitched metal roof, the city's skyline landmark. */
    private static void palaceHall(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // Two-course platform and the south steps.
        b.fill(cx - 46, g, cz - 228, cx + 46, g + 1, cz - 172, p.foundation());
        b.fill(cx - 12, g, cz - 171, cx + 12, g, cz - 168, p.foundation());
        // The inner hall.
        int hx0 = cx - 26;
        int hx1 = cx + 26;
        int hz0 = cz - 214;
        int hz1 = cz - 186;
        b.fill(hx0, g + 1, hz0, hx1, g + 1, hz1, p.ground());
        b.walls(hx0, g + 2, hz0, hx1, g + 13, hz1, p.wall());
        b.fill(hx0 + 1, g + 2, hz0 + 1, hx1 - 1, g + 13, hz1 - 1, AIR);
        b.fill(hx0 + 1, g + 14, hz0 + 1, hx1 - 1, g + 14, hz1 - 1, p.roof());
        b.walls(hx0, g + 5, hz0, hx1, g + 5, hz1, p.accent());
        b.walls(hx0, g + 11, hz0, hx1, g + 11, hz1, p.accent());
        door(b, p, cx - 2, g + 2, hz1, Direction.SOUTH);
        door(b, p, cx + 2, g + 2, hz1, Direction.SOUTH);
        for (int z = hz0 + 4; z <= hz1 - 4; z += 6) {
            b.window(hx0, g + 4, z, 3, 1, true);
            b.window(hx1, g + 4, z, 3, 1, true);
            b.window(hx0, g + 8, z, 3, 1, true);
            b.window(hx1, g + 8, z, 3, 1, true);
        }
        // The colonnade: carved posts on all four sides.
        for (int x = cx - 40; x <= cx + 40; x += 8) {
            column(b, x, cz - 222, g + 2, g + 14, p);
            column(b, x, cz - 178, g + 2, g + 14, p);
        }
        for (int z = cz - 214; z <= cz - 186; z += 8) {
            column(b, cx - 40, z, g + 2, g + 14, p);
            column(b, cx + 40, z, g + 2, g + 14, p);
        }
        // Eaves and the great roof.
        b.fill(cx - 46, g + 15, cz - 228, cx + 46, g + 15, cz - 172, p.roof());
        b.pitchedRoof(cx - 46, cz - 228, cx + 46, cz - 172, g + 15, 9, 0);
        b.fill(cx - 46, g + 14, cz - 172, cx + 46, g + 14, cz - 171, p.accent());
        b.fill(cx - 46, g + 14, cz - 228, cx + 46, g + 14, cz - 227, p.accent());
        // The central tower over the hall.
        int tx0 = cx - 8;
        int tx1 = cx + 8;
        int tz0 = cz - 208;
        int tz1 = cz - 192;
        b.walls(tx0, g + 15, tz0, tx1, g + 33, tz1, p.wall());
        b.fill(tx0 + 1, g + 16, tz0 + 1, tx1 - 1, g + 33, tz1 - 1, AIR);
        b.walls(tx0, g + 19, tz0, tx1, g + 19, tz1, p.accent());
        b.walls(tx0, g + 25, tz0, tx1, g + 25, tz1, p.accent());
        b.window(cx, g + 21, tz0, 3, 1, true);
        b.window(cx, g + 21, tz1, 3, 1, true);
        b.window(tx0, g + 21, cz - 200, 3, 1, true);
        b.window(tx1, g + 21, cz - 200, 3, 1, true);
        b.fill(tx0 - 2, g + 34, tz0 - 2, tx1 + 2, g + 34, tz1 + 2, p.roofSlab());
        b.pitchedRoof(tx0 - 2, tz0 - 2, tx1 + 2, tz1 + 2, g + 35, 5, 0);
        b.spire(cx, cz - 200, g + 40, 6);
        // Lamps along the verandah, and the hall's sign.
        for (int x = cx - 32; x <= cx + 32; x += 16) {
            b.put(x, g + 3, cz - 176, p.light());
        }
        signPost(b, cx + 16, cz - 166, g, p, Direction.SOUTH, "OBA'S HALL");
        b.put(cx + 16, g + 2, cz - 168, p.frame());
        b.put(cx + 16, g + 3, cz - 168, GOLD);
    }

    /** The courtyard between the hall and the inner gate: pool, palms, drums, benches. */
    private static void palaceCourtyard(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx - 60, g, cz - 172, cx + 60, g, cz - 150, p.ground());
        outline(b, cx - 60, cz - 172, cx + 60, cz - 150, g, p.foundation());
        // The pool.
        b.fill(cx - 5, g, cz - 166, cx + 5, g, cz - 158, p.foundation());
        b.fill(cx - 4, g, cz - 165, cx + 4, g, cz - 159, WATER);
        b.put(cx, g + 1, cz - 162, p.accent());
        b.put(cx, g + 2, cz - 162, p.accent());
        b.put(cx, g + 3, cz - 162, p.light());
        palm(b, cx - 52, cz - 156, g, p);
        palm(b, cx + 52, cz - 156, g, p);
        palm(b, cx - 52, cz - 168, g, p);
        palm(b, cx + 52, cz - 168, g, p);
        drum(b, cx - 32, cz - 160, g, p);
        drum(b, cx - 28, cz - 160, g, p);
        drum(b, cx + 28, cz - 160, g, p);
        drum(b, cx + 32, cz - 160, g, p);
        b.fill(cx - 40, g + 1, cz - 154, cx - 36, g + 1, cz - 154, p.roofSlab());
        b.fill(cx + 36, g + 1, cz - 154, cx + 40, g + 1, cz - 154, p.roofSlab());
        b.put(cx - 40, g + 1, cz - 153, p.accent());
        b.put(cx + 40, g + 1, cz - 153, p.accent());
        lamp(b, cx - 20, cz - 152, g, p);
        lamp(b, cx + 20, cz - 152, g, p);
    }

    /** The ancestral shrine in the north of the compound. */
    private static void palaceShrine(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 34;
        int x1 = cx + 34;
        int z0 = cz - 276;
        int z1 = cz - 236;
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        outline(b, x0, z0, x1, z1, g, p.foundation());
        b.walls(x0, g + 1, z0, x1, g + 3, z1, p.weathered());
        b.fill(cx - 4, g + 1, z1, cx + 4, g + 3, z1, AIR);
        b.fill(cx - 6, g, cz - 236, cx + 7, g, cz - 228, p.ground());
        // The shrine house.
        b.fill(cx - 12, g, cz - 264, cx + 12, g, cz - 246, p.foundation());
        b.walls(cx - 12, g + 1, cz - 264, cx + 12, g + 8, cz - 246, p.wall());
        b.fill(cx - 11, g + 1, cz - 263, cx + 11, g + 8, cz - 247, AIR);
        b.walls(cx - 12, g + 5, cz - 264, cx + 12, g + 5, cz - 246, p.accent());
        door(b, p, cx, g + 1, cz - 246, Direction.SOUTH);
        b.fill(cx - 12, g + 9, cz - 264, cx + 12, g + 9, cz - 246, p.roof());
        b.pitchedRoof(cx - 13, cz - 265, cx + 13, cz - 245, g + 9, 5, 0);
        b.fill(cx - 2, g + 1, cz - 255, cx + 2, g + 1, cz - 253, p.foundation());
        b.put(cx, g + 2, cz - 254, GOLD);
        b.put(cx, g + 3, cz - 254, p.light());
        lamp(b, cx - 10, cz - 248, g, p);
        lamp(b, cx + 10, cz - 248, g, p);
        bigTree(b, cx - 24, cz - 268, g, p);
        bigTree(b, cx + 24, cz - 268, g, p);
        signPost(b, cx + 8, cz - 234, g, p, Direction.SOUTH,
                "PALACE SHRINE", "REMOVE SHOES");
    }

    /** Low quarters, kitchens and storehouses along the compound walls. */
    private static void palaceQuarters(StructureBuilder b, int cx, int cz, int g, Palette p) {
        house(b, cx - 104, cz - 282, cx - 84, cz - 252, g, p, p.wall(), g + 4, true,
                Direction.EAST, "WEST QUARTERS");
        house(b, cx - 104, cz - 232, cx - 84, cz - 202, g, p, p.wall(), g + 4, true,
                Direction.EAST, "WEST QUARTERS");
        house(b, cx + 84, cz - 282, cx + 104, cz - 252, g, p, p.wall(), g + 4, true,
                Direction.WEST, "EAST QUARTERS");
        house(b, cx + 84, cz - 232, cx + 104, cz - 202, g, p, p.wall(), g + 4, true,
                Direction.WEST, "EAST QUARTERS");
        house(b, cx - 104, cz - 182, cx - 84, cz - 158, g, p, p.wall(), g + 4, true,
                Direction.EAST, "KITCHEN");
        wellHead(b, cx - 78, cz - 170, g, p);
        bigTree(b, cx - 70, cz - 260, g, p);
        bigTree(b, cx + 70, cz - 260, g, p);
        palm(b, cx - 70, cz - 190, g, p);
        palm(b, cx + 70, cz - 190, g, p);
        palm(b, cx - 70, cz - 216, g, p);
        palm(b, cx + 70, cz - 216, g, p);
        parterre(b, cx - 66, cz - 200, g, p);
        parterre(b, cx + 66, cz - 200, g, p);
        palm(b, cx - 76, cz - 224, g, p);
        palm(b, cx + 76, cz - 224, g, p);
        palm(b, cx - 76, cz - 176, g, p);
        palm(b, cx + 76, cz - 176, g, p);
        b.fill(cx - 60, g + 1, cz - 286, cx - 40, g + 1, cz - 286, p.rail());
        b.fill(cx + 40, g + 1, cz - 286, cx + 60, g + 1, cz - 286, p.rail());
        b.put(cx - 50, g + 1, cz - 286, p.light());
        b.put(cx + 50, g + 1, cz - 286, p.light());
    }

    /** A painted carved post with a timber collar every third course. */
    private static void column(StructureBuilder b, int x, int z, int y0, int y1, Palette p) {
        for (int y = y0; y <= y1; y++) {
            b.put(x, y, z, Math.floorMod(y - y0, 3) == 0 ? p.frame() : p.accent());
        }
    }

    // ------------------------------------------------------------------ market

    private static void market(StructureBuilder b, int cx, int cz, int g, Palette p) {
        marketSquare(b, cx, cz, g, p);
        marketShops(b, cx, cz, g, p);
        marketStalls(b, cx, cz, g, p);
        marketSheds(b, cx, cz, g, p);
        marketFloodLane(b, cx, cz, g, p);
        marketGardens(b, cx, cz, g, p);
    }

    /** The crossing at (-220, 0): the notice board, well, tree and drums. */
    private static void marketSquare(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx - 236, g, cz - 16, cx - 204, g, cz + 16, p.ground());
        outline(b, cx - 236, cz - 16, cx - 204, cz + 16, g, p.foundation());
        // The quest board in the square's north-east corner, facing the lane.
        b.fill(cx - 210, g + 1, cz - 14, cx - 210, g + 4, cz - 6, p.accent());
        b.fill(cx - 212, g + 5, cz - 15, cx - 208, g + 5, cz - 5, p.roofSlab());
        wallSign(b, cx - 210, g + 3, cz - 12, Direction.WEST,
                "CITY NOTICES", "THE OIL LEDGER");
        wallSign(b, cx - 210, g + 3, cz - 8, Direction.WEST,
                "FISHER MISSING", "ASK THE CLERK");
        b.put(cx - 212, g + 1, cz - 14, p.light());
        b.put(cx - 212, g + 1, cz - 6, p.light());
        bigTree(b, cx - 232, cz - 12, g, p);
        wellHead(b, cx - 228, cz + 10, g, p);
        drum(b, cx - 206, cz + 10, g, p);
        drum(b, cx - 202, cz + 10, g, p);
        signPost(b, cx - 216, cz - 124, g, p, Direction.SOUTH,
                "MARKET", "FRESH & DRIED", "BARGAIN HARD");
    }

    private static void marketShops(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // The cloth-and-oil shop west of the lane.
        house(b, cx - 254, cz - 74, cx - 232, cz - 46, g, p, p.wall(), g + 8, true,
                Direction.EAST, "MARKET SHOP", "CLOTH & OIL");
        b.fill(cx - 231, g, cz - 62, cx - 222, g, cz - 58, p.ground());
        for (int z : new int[]{cz - 70, cz - 66, cz - 54, cz - 50}) {
            b.fill(cx - 228, g + 1, z, cx - 228, g + 3, z, p.frame());
        }
        b.fill(cx - 232, g + 4, cz - 70, cx - 228, g + 4, cz - 50, p.roofSlab());
        // The herbalist, with a fenced physic garden.
        house(b, cx - 254, cz + 116, cx - 228, cz + 140, g, p, p.wall(), g + 4, true,
                Direction.EAST, "HERBALIST", "REMEDIES", "NO CREDIT");
        b.fill(cx - 227, g, cz + 126, cx - 222, g, cz + 130, p.ground());
        farmPlot(b, cx - 266, cz + 116, cx - 256, cz + 144, g, p);
        // A tailor south of the lane.
        house(b, cx - 214, cz + 132, cx - 190, cz + 156, g, p, p.wall(), g + 4, true,
                Direction.WEST, "CLOTH SHOP", "TAILOR");
        b.fill(cx - 217, g, cz + 142, cx - 214, g, cz + 146, p.ground());
        // A cookhouse among the stalls.
        house(b, cx - 204, cz + 36, cx - 184, cz + 56, g, p, p.wall(), g + 4, false,
                Direction.WEST, "COOKHOUSE", "HOT FOOD");
        b.fill(cx - 216, g, cz + 44, cx - 204, g, cz + 48, p.ground());
        b.put(cx - 202, g + 1, cz + 38, BARREL);
        b.put(cx - 200, g + 1, cz + 38, HAY);
        b.put(cx - 202, g + 1, cz + 50, p.light());
    }

    /** Two dozen striped stalls on both sides of the market lanes. */
    private static void marketStalls(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int[][] spots = {
                {-268, -210}, {-246, -210}, {-268, -160}, {-246, -160},
                {-268, -106}, {-246, -86}, {-268, 16}, {-244, 16},
                {-266, 60}, {-244, 60}, {-266, 88}, {-244, 88},
                {-268, 150}, {-246, 170}, {-210, -210}, {-192, -210},
                {-210, -160}, {-192, -160}, {-212, -100}, {-194, -80},
                {-212, 20}, {-194, 20}, {-212, 70}, {-194, 70},
                {-212, 170}, {-194, 170}
        };
        for (int i = 0; i < spots.length; i++) {
            stall(b, cx + spots[i][0], cz + spots[i][1], g, p,
                    Materials.wool(AWNINGS.get(i % AWNINGS.size())), i);
        }
        signPost(b, cx - 226, cz + 6, g, p, Direction.NORTH,
                "MARKET", "DRUMS & CLOTH");
        signPost(b, cx - 200, cz - 114, g, p, Direction.NORTH,
                "MARKET", "FOOD LANE");
    }

    /** Open timber sheds for crafts, pottery and grain. */
    private static void marketSheds(StructureBuilder b, int cx, int cz, int g, Palette p) {
        marketShed(b, cx - 312, cz + 8, cx - 288, cz + 44, g, p, "CRAFT MARKET");
        marketShed(b, cx - 312, cz + 52, cx - 290, cz + 84, g, p, "POTTERY");
        house(b, cx - 312, cz - 80, cx - 292, cz - 52, g, p, p.weathered(), g + 6, false,
                Direction.EAST, "GRAIN STORE");
        b.fill(cx - 298, g + 1, cz - 74, cx - 294, g + 1, cz - 70, HAY);
        b.fill(cx - 302, g + 1, cz - 62, cx - 298, g + 1, cz - 58, HAY);
        // The market yard between the lane and the drain.
        b.walls(cx - 186, g + 1, cz - 56, cx - 150, g + 2, cz - 24, p.weathered());
        b.fill(cx - 168, g + 1, cz - 56, cx - 164, g + 2, cz - 56, AIR);
        b.fill(cx - 186, g, cz - 56, cx - 150, g, cz - 24, p.ground());
        b.fill(cx - 184, g + 3, cz - 54, cx - 152, g + 3, cz - 40, p.roofSlab());
        for (int x = cx - 182; x <= cx - 154; x += 7) {
            b.fill(x, g + 1, cz - 54, x, g + 2, cz - 54, p.frame());
            b.fill(x, g + 1, cz - 42, x, g + 2, cz - 42, p.frame());
        }
        crateStack(b, cx - 180, cz - 30, g, p, 2);
        crateStack(b, cx - 176, cz - 30, g, p, 1);
        b.put(cx - 170, g + 1, cz - 46, BARREL);
        b.put(cx - 160, g + 1, cz - 46, BARREL);
        signPost(b, cx - 168, cz - 20, g, p, Direction.SOUTH, "MARKET YARD");
    }

    private static void marketShed(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                   Palette p, String label) {
        for (int x = x0; x <= x1; x += 6) {
            for (int z = z0; z <= z1; z += 6) {
                b.fill(x, g + 1, z, x, g + 3, z, p.frame());
            }
        }
        b.fill(x0 - 1, g + 4, z0 - 1, x1 + 1, g + 4, z1 + 1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 4, 3,
                z1 - z0 >= x1 - x0 ? 1 : 0);
        for (int x = x0 + 2; x <= x1 - 2; x += 5) {
            b.fill(x, g + 1, z0 + 2, x, g + 1, z1 - 2, p.foundation());
        }
        b.put(x0 + 2, g + 2, z0 + 2, p.light());
        b.put(x1 - 2, g + 2, z1 - 2, p.light());
        signPost(b, x1 + 2, (z0 + z1) / 2, g, p, Direction.EAST, label);
    }

    /** The shallow flooded lane north of the market, crossed by duckboards. */
    private static void marketFloodLane(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx - 312, g, cz - 188, cx - 150, g, cz - 185, WATER);
        for (int x = cx - 310; x <= cx - 152; x += 4) {
            b.put(x, g, cz - 187, p.foundation());
            b.put(x, g, cz - 186, p.foundation());
        }
        for (int x : new int[]{cx - 282, cx - 220}) {
            b.fill(x - 3, g, cz - 189, x + 3, g, cz - 184, p.frame());
            b.put(x - 3, g + 1, cz - 189, p.rail());
            b.put(x + 3, g + 1, cz - 189, p.rail());
        }
        for (int x = cx - 300; x <= cx - 160; x += 28) {
            b.put(x, g, cz - 186, LILY);
            b.put(x + 12, g, cz - 187, LILY);
        }
        signPost(b, cx - 214, cz - 190, g, p, Direction.SOUTH,
                "FLOODED LANE", "MIND THE WATER");
    }

    /** Palms, plots and the ruined stall corners between the lanes. */
    private static void marketGardens(StructureBuilder b, int cx, int cz, int g, Palette p) {
        farmPlot(b, cx - 306, cz + 132, cx - 286, cz + 160, g, p);
        palm(b, cx - 300, cz - 176, g, p);
        palm(b, cx - 292, cz - 150, g, p);
        palm(b, cx - 160, cz + 40, g, p);
        palm(b, cx - 152, cz + 90, g, p);
        palm(b, cx - 300, cz + 96, g, p);
        // A collapsed stall and its spilled goods.
        b.fill(cx - 298, g + 1, cz - 136, cx - 294, g + 1, cz - 134, p.weathered());
        b.put(cx - 296, g + 2, cz - 136, p.accent());
        b.put(cx - 296, g + 1, cz - 138, p.rubble());
        b.put(cx - 292, g + 1, cz - 138, p.rubble());
        b.put(cx - 294, g + 1, cz - 140, p.frame());
        b.fill(cx - 188, g + 1, cz + 92, cx - 184, g + 1, cz + 94, p.weathered());
        b.put(cx - 186, g + 2, cz + 92, p.accent());
        b.put(cx - 186, g + 1, cz + 96, p.rubble());
        // A palm grove against the palace wall.
        for (int z = -236; z <= -156; z += 20) {
            palm(b, cx - 112, cz + z, g, p);
        }
    }

    // ------------------------------------------------------------------ marina

    private static void marina(StructureBuilder b, int cx, int cz, int g, Palette p) {
        quay(b, cx, cz, g, p);
        warehouse(b, cx + 228, cz - 232, cx + 252, cz - 196, g, p, "GODOWN 1");
        warehouse(b, cx + 228, cz - 188, cx + 252, cz - 152, g, p, "GODOWN 2");
        warehouse(b, cx + 228, cz - 84, cx + 252, cz - 48, g, p, "GODOWN 3");
        warehouse(b, cx + 228, cz + 68, cx + 252, cz + 100, g, p, "GODOWN 4");
        oilOffice(b, cx, cz, g, p);
        fishMarket(b, cx, cz, g, p);
        fishermanJetty(b, cx, cz, g, p);
        marinaProps(b, cx, cz, g, p);
    }

    private static void quay(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx + 226, g, cz - 240, cx + 254, g, cz + 196, p.ground());
        outline(b, cx + 226, cz - 240, cx + 254, cz + 196, g, p.foundation());
        b.fill(cx + 226, g, cz + 196, cx + 254, g, cz + 214, p.ground());
        for (int z = cz - 236; z <= cz + 190; z += 20) {
            b.fill(cx + 254, g + 1, z, cx + 254, g + 2, z, p.accent());
        }
        for (int z = cz - 220; z <= cz + 180; z += 60) {
            lamp(b, cx + 226, z, g, p);
        }
        signPost(b, cx + 220, cz + 196, g, p, Direction.NORTH,
                "MARINA", "FISH MARKET", "CANOES FOR HIRE");
    }

    /** A corrugated godown with a loading door onto the quay. */
    private static void warehouse(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                  Palette p, String label) {
        b.fill(x0, g, z0, x1, g, z1, p.foundation());
        b.walls(x0, g + 1, z0, x1, g + 9, z1, p.weathered());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 9, z1 - 1, AIR);
        b.fill(x0, g + 10, z0, x1, g + 10, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 10, 6,
                z1 - z0 >= x1 - x0 ? 1 : 0);
        door(b, p, x0, g + 1, z0 + 5, Direction.WEST);
        b.fill(x0, g + 6, z0 + 2, x0, g + 7, z0 + 3, p.frame());
        wallSign(b, x0, g + 7, z0 + 2, Direction.WEST, label, "NO ENTRY");
        for (int z = z0 + 2; z <= z1 - 2; z += 5) {
            b.put(x1 - 2, g + 1, z, BARREL);
            b.put(x1 - 3, g + 1, z, HAY);
            b.put(x1 - 2, g + 2, z, BARREL);
        }
        b.put(x0 + 2, g + 8, z1 - 2, p.light());
    }

    private static void oilOffice(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx + 192;
        int x1 = cx + 214;
        int z0 = cz - 132;
        int z1 = cz - 104;
        office(b, x0, z0, x1, z1, g, p, Direction.EAST,
                "OIL OFFICE", "SHIPMENTS", "NO ENTRY");
        // The ledger room and its locked cabinet.
        b.fill(x0 + 2, g + 1, z0 + 2, x0 + 4, g + 1, z0 + 2, BOOKS);
        b.fill(x0 + 2, g + 2, z0 + 2, x0 + 4, g + 2, z0 + 2, BOOKS);
        b.fill(x0 + 2, g + 1, z0 + 4, x0 + 4, g + 1, z0 + 4, BOOKS);
        wallSign(b, x0, g + 3, z0 + 3, Direction.EAST, "THE OIL LEDGER", "SHIPMENT 47");
        b.put(x1 - 2, g + 1, z1 - 3, p.accent());
        b.put(x1 - 2, g + 2, z1 - 3, p.light());
        // Drums on the quay.
        b.fill(cx + 217, g + 1, cz - 102, cx + 218, g + 2, cz - 101, p.accent());
        b.fill(cx + 217, g + 1, cz - 98, cx + 218, g + 2, cz - 97, p.accent());
        signPost(b, cx + 216, cz - 96, g, p, Direction.NORTH,
                "OIL OFFICE", "THE OIL LEDGER");
    }

    private static void fishMarket(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx + 230;
        int x1 = cx + 254;
        int z0 = cz + 20;
        int z1 = cz + 60;
        for (int x = x0; x <= x1; x += 6) {
            for (int z = z0; z <= z1; z += 8) {
                b.fill(x, g + 1, z, x, g + 4, z, p.accent());
            }
        }
        b.fill(x0 - 1, g + 5, z0 - 1, x1 + 1, g + 5, z1 + 1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 5, 3, 1);
        for (int z = z0 + 2; z <= z1 - 2; z += 6) {
            b.fill(x0 + 2, g + 1, z, x1 - 2, g + 1, z, p.foundation());
            b.put(x0 + 3, g + 2, z, KELP);
            b.put(x1 - 3, g + 2, z, BARREL);
        }
        b.put(x0 + 2, g + 4, z0 + 2, p.light());
        b.put(x1 - 2, g + 4, z1 - 2, p.light());
        signPost(b, x0, z1 + 3, g, p, Direction.SOUTH, "FISH MARKET", "FRESH DAILY");
        fishRack(b, x0, z1 + 4, g, p);
        fishRack(b, x0 + 8, z1 + 4, g, p);
        fishRack(b, x0 + 16, z1 + 4, g, p);
        canoe(b, cx + 258, cz + 28, g, p, true);
        canoe(b, cx + 258, cz + 40, g, p, true);
        canoe(b, cx + 258, cz + 52, g, p, true);
    }

    private static void fishermanJetty(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx + 240;
        int x1 = cx + 270;
        int z0 = cz + 116;
        int z1 = cz + 124;
        b.fill(x0, g + 1, z0, x1, g + 1, z1, p.frame());
        for (int x = x0; x <= x1; x += 6) {
            b.fill(x, g - 3, z0, x, g, z0, p.accent());
            b.fill(x, g - 3, z1, x, g, z1, p.accent());
        }
        b.fill(x0, g + 2, z0, x1, g + 2, z0, p.rail());
        b.fill(x0, g + 2, z1, x1, g + 2, z1, p.rail());
        int hx0 = cx + 244;
        int hx1 = cx + 254;
        int hz0 = cz + 130;
        int hz1 = cz + 144;
        b.walls(hx0, g + 1, hz0, hx1, g + 5, hz1, p.weathered());
        b.fill(hx0 + 1, g + 1, hz0 + 1, hx1 - 1, g + 5, hz1 - 1, AIR);
        b.fill(hx0, g + 6, hz0, hx1, g + 6, hz1, p.roof());
        b.pitchedRoof(hx0 - 1, hz0 - 1, hx1 + 1, hz1 + 1, g + 6, 3, 0);
        door(b, p, cx + 249, g + 1, hz1, Direction.SOUTH);
        b.put(cx + 246, g + 1, hz0 + 2, BARREL);
        b.put(cx + 252, g + 1, hz0 + 2, KELP);
        b.put(cx + 246, g + 2, hz0 + 2, p.light());
        signPost(b, cx + 240, cz + 128, g, p, Direction.SOUTH, "FISHERMAN", "HIS CANOE?");
        signPost(b, cx + 240, cz + 112, g, p, Direction.SOUTH,
                "FISHER MISSING", "LAST SEEN", "AT THE SHRINE");
        canoe(b, cx + 266, cz + 132, g, p, true);
    }

    private static void marinaProps(StructureBuilder b, int cx, int cz, int g, Palette p) {
        crane(b, cx + 250, cz - 144, g, p);
        crane(b, cx + 250, cz - 100, g, p);
        crane(b, cx + 250, cz + 4, g, p);
        canoe(b, cx + 262, cz - 220, g, p, true);
        canoe(b, cx + 262, cz - 160, g, p, true);
        canoe(b, cx + 262, cz - 90, g, p, true);
        canoe(b, cx + 262, cz + 10, g, p, true);
        canoe(b, cx + 262, cz + 70, g, p, true);
        canoe(b, cx + 262, cz + 150, g, p, true);
        palm(b, cx + 230, cz - 240, g, p);
        palm(b, cx + 230, cz - 140, g, p);
        palm(b, cx + 230, cz - 30, g, p);
        palm(b, cx + 230, cz + 10, g, p);
        palm(b, cx + 230, cz + 104, g, p);
        palm(b, cx + 230, cz + 190, g, p);
        palm(b, cx - 280, cz + 218, g, p);
        palm(b, cx - 220, cz + 218, g, p);
        palm(b, cx - 160, cz + 218, g, p);
        palm(b, cx - 100, cz + 218, g, p);
        palm(b, cx - 40, cz + 218, g, p);
        palm(b, cx + 40, cz + 218, g, p);
        palm(b, cx + 100, cz + 218, g, p);
        palm(b, cx + 140, cz + 218, g, p);
        crateStack(b, cx + 253, cz - 180, g, p, 2);
        crateStack(b, cx + 253, cz - 60, g, p, 1);
        crateStack(b, cx + 253, cz + 30, g, p, 2);
        crateStack(b, cx + 253, cz + 90, g, p, 1);
        b.put(cx + 253, g + 1, cz - 120, BARREL);
        b.put(cx + 253, g + 1, cz + 160, BARREL);
        b.put(cx + 253, g + 1, cz + 180, BARREL);
    }

    // ------------------------------------------------------------------ colonial quarter

    private static void colonialQuarter(StructureBuilder b, int cx, int cz, int g, Palette p) {
        hotel(b, cx, cz, g, p);
        church(b, cx, cz, g, p);
        club(b, cx, cz, g, p);
        office(b, cx + 14, cz + 8, cx + 40, cz + 40, g, p, Direction.WEST,
                "COLONIAL OFFICE", "PASSES & TAXES");
        office(b, cx + 14, cz + 72, cx + 40, cz + 104, g, p, Direction.WEST,
                "LANDS OFFICE", "MAPS & DEEDS");
        office(b, cx + 70, cz + 8, cx + 94, cz + 40, g, p, Direction.WEST,
                "MARINE OFFICE", "SHIPPING");
        office(b, cx + 70, cz + 72, cx + 94, cz + 104, g, p, Direction.WEST,
                "TELEGRAPH", "POST & WIRE");
        // Whitewashed residences: west row, middle row and the southern terraces.
        house(b, cx - 116, cz + 8, cx - 98, cz + 26, g, p, WHITE, g + 4, true,
                Direction.EAST, "RESIDENCE");
        house(b, cx - 116, cz + 32, cx - 98, cz + 50, g, p, WHITE, g + 4, true,
                Direction.EAST, "RESIDENCE");
        house(b, cx - 52, cz + 72, cx - 34, cz + 90, g, p, WHITE, g + 4, true,
                Direction.EAST, "RESIDENCE");
        house(b, cx - 52, cz + 96, cx - 34, cz + 114, g, p, WHITE, g + 4, true,
                Direction.EAST, "RESIDENCE");
        house(b, cx - 52, cz + 152, cx - 34, cz + 170, g, p, WHITE, g + 4, true,
                Direction.EAST, "RESIDENCE");
        house(b, cx - 28, cz + 152, cx - 10, cz + 170, g, p, WHITE, g + 4, true,
                Direction.WEST, "RESIDENCE");
        house(b, cx + 12, cz + 152, cx + 30, cz + 170, g, p, WHITE, g + 4, true,
                Direction.EAST, "RESIDENCE");
        house(b, cx + 68, cz + 152, cx + 86, cz + 170, g, p, WHITE, g + 4, true,
                Direction.WEST, "RESIDENCE");
        colonialGreen(b, cx, cz, g, p);
        colonialYards(b, cx, cz, g, p);
        signPost(b, cx - 84, cz - 56, g, p, Direction.SOUTH,
                "COLONIAL QTR", "OFFICES", "THE CLUB");
        for (int z = cz + 12; z <= cz + 188; z += 24) {
            lamp(b, cx - 92, z, g, p);
            lamp(b, cx + 64, z, g, p);
        }
    }

    private static void hotel(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 84;
        int x1 = cx - 56;
        int z0 = cz - 136;
        int z1 = cz - 112;
        int top = g + 9;
        b.fill(x0, g, z0, x1, g, z1, p.foundation());
        b.walls(x0, g + 1, z0, x1, top, z1, WHITE);
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, top, z1 - 1, AIR);
        b.fill(x0 + 1, g + 4, z0 + 1, x1 - 1, g + 4, z1 - 1, p.roof());
        b.walls(x0, g + 4, z0, x1, g + 4, z1, p.accent());
        b.fill(x0, top + 1, z0, x1, top + 1, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, top + 1, 5, 0);
        for (int x = x0 + 3; x <= x1 - 3; x += 5) {
            b.window(x, g + 2, z0, 2, 1, true);
            b.window(x, g + 2, z1, 2, 1, true);
            b.window(x, g + 6, z0, 2, 1, true);
            b.window(x, g + 6, z1, 2, 1, true);
        }
        for (int z = z0 + 4; z <= z1 - 4; z += 6) {
            b.window(x0, g + 2, z, 2, 1, true);
            b.window(x0, g + 6, z, 2, 1, true);
            b.window(x1, g + 2, z, 2, 1, true);
            b.window(x1, g + 6, z, 2, 1, true);
        }
        door(b, p, x0, g + 1, cz - 120, Direction.WEST);
        // A shallow metal awning cantilevered over the street front.
        b.fill(x0 - 1, g + 5, z0, x0, g + 5, z1, p.roofSlab());
        // Beds upstairs and down, and the bar.
        b.fill(x0 + 2, g + 1, z0 + 2, x0 + 4, g + 1, z0 + 4, RED_BED);
        b.fill(x1 - 6, g + 1, z1 - 5, x1 - 4, g + 1, z1 - 3, RED_BED);
        b.fill(x0 + 2, g + 5, z0 + 2, x0 + 4, g + 5, z0 + 4, RED_BED);
        b.fill(x1 - 6, g + 5, z1 - 5, x1 - 4, g + 5, z1 - 3, RED_BED);
        b.fill(x0 + 2, g + 1, z1 - 4, x0 + 8, g + 1, z1 - 3, p.accent());
        b.put(x0 + 3, g + 2, z1 - 3, p.light());
        wallSign(b, x0, g + 7, cz - 120, Direction.WEST, "COLONIAL HOTEL", "ROOMS & MEALS");
    }

    private static void church(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx + 16;
        int x1 = cx + 44;
        int z0 = cz - 44;
        int z1 = cz - 8;
        b.fill(x0, g, z0, x1, g, z1, p.foundation());
        b.walls(x0, g + 1, z0, x1, g + 12, z1, WHITE);
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 12, z1 - 1, AIR);
        b.fill(x0, g + 13, z0, x1, g + 13, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + 13, 7, 1);
        // The tower at the north-west corner.
        int tx0 = cx + 16;
        int tx1 = cx + 26;
        int tz0 = cz - 44;
        int tz1 = cz - 34;
        b.walls(tx0, g + 1, tz0, tx1, g + 26, tz1, WHITE);
        b.fill(tx0 + 1, g + 1, tz0 + 1, tx1 - 1, g + 26, tz1 - 1, AIR);
        b.walls(tx0, g + 5, tz0, tx1, g + 5, tz1, p.accent());
        b.walls(tx0, g + 18, tz0, tx1, g + 18, tz1, p.accent());
        b.window(cx + 21, g + 20, tz0, 3, 1, true);
        b.window(cx + 21, g + 20, tz1, 3, 1, true);
        b.window(tx0, g + 20, cz - 39, 3, 1, true);
        b.window(tx1, g + 20, cz - 39, 3, 1, true);
        b.fill(tx0 - 1, g + 27, tz0 - 1, tx1 + 1, g + 27, tz1 + 1, p.roofSlab());
        b.pitchedRoof(tx0 - 1, tz0 - 1, tx1 + 1, tz1 + 1, g + 27, 5, 0);
        b.spire(cx + 21, cz - 39, g + 32, 6);
        // Stained glazing and the south door.
        for (int z = z0 + 6; z <= z1 - 6; z += 8) {
            b.put(x0, g + 5, z, CYAN_GLASS);
            b.put(x1, g + 5, z, CYAN_GLASS);
            b.put(x0, g + 9, z, MAGENTA_GLASS);
            b.put(x1, g + 9, z, MAGENTA_GLASS);
        }
        door(b, p, cx + 30, g + 1, z1, Direction.SOUTH);
        wallSign(b, cx + 30, g + 5, z1, Direction.SOUTH, "ST. JOHN'S", "ALL WELCOME");
        // Churchyard: low wall, gate, headstones and palms.
        b.walls(cx + 12, g + 1, cz - 48, cx + 48, g + 1, cz - 4, p.weathered());
        b.fill(cx + 28, g + 1, cz - 4, cx + 32, g + 1, cz - 4, AIR);
        for (int z = cz - 44; z <= cz - 12; z += 8) {
            headstone(b, cx + 15, z, g, p, z);
            headstone(b, cx + 46, z, g, p, z + 1);
        }
        palm(b, cx + 46, cz - 48, g, p);
        palm(b, cx + 14, cz - 50, g, p);
    }

    private static void club(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 52;
        int x1 = cx - 20;
        int z0 = cz + 4;
        int z1 = cz + 36;
        house(b, x0, z0, x1, z1, g, p, WHITE, g + 8, true, Direction.EAST,
                "LAGOS CLUB", "MEMBERS ONLY");
        // Verandah facing the processional axis.
        for (int z = z0 + 2; z <= z1 - 2; z += 5) {
            b.fill(x1 + 3, g + 1, z, x1 + 3, g + 4, z, p.accent());
        }
        b.fill(x1 + 1, g + 5, z0 + 1, x1 + 4, g + 5, z1 - 1, p.roofSlab());
        b.fill(x1 + 1, g + 1, z0 + 2, x1 + 2, g + 1, z1 - 2, p.frame());
        // Billiard table, bar and lamps.
        b.fill(x0 + 4, g + 1, z0 + 6, x0 + 8, g + 1, z0 + 8,
                Materials.wool(DyeColor.GREEN));
        b.fill(x0 + 2, g + 1, z0 + 2, x0 + 6, g + 1, z0 + 3, p.accent());
        b.put(x0 + 3, g + 2, z0 + 2, p.light());
        b.put(x0 + 10, g + 1, z0 + 10, p.frame());
        b.put(x1 - 4, g + 1, z1 - 6, p.frame());
    }

    /** The colonial green with its bandstand, and the smaller east garden. */
    private static void colonialGreen(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx - 50, g, cz + 118, cx - 20, g, cz + 134, p.ground());
        outline(b, cx - 50, cz + 118, cx - 20, cz + 134, g, p.foundation());
        int bx = cx - 35;
        int bz = cz + 126;
        b.fill(bx - 4, g, bz - 4, bx + 4, g, bz + 4, p.foundation());
        for (int dx = -3; dx <= 3; dx += 6) {
            for (int dz = -3; dz <= 3; dz += 6) {
                b.fill(bx + dx, g + 1, bz + dz, bx + dx, g + 4, bz + dz, p.accent());
            }
        }
        b.fill(bx - 4, g + 5, bz - 4, bx + 4, g + 5, bz + 4, p.roof());
        b.pitchedRoof(bx - 4, bz - 4, bx + 4, bz + 4, g + 5, 4, 0);
        b.put(bx, g + 2, bz, p.light());
        palm(b, cx - 46, cz + 122, g, p);
        palm(b, cx - 24, cz + 130, g, p);
        b.fill(cx - 44, g + 1, cz + 128, cx - 40, g + 1, cz + 128, p.roofSlab());
        b.fill(cx - 26, g + 1, cz + 120, cx - 22, g + 1, cz + 120, p.roofSlab());
        // East garden and its fountain.
        b.fill(cx + 12, g, cz + 112, cx + 44, g, cz + 132, p.ground());
        outline(b, cx + 12, cz + 112, cx + 44, cz + 132, g, p.foundation());
        b.fill(cx + 26, g, cz + 118, cx + 30, g, cz + 126, p.foundation());
        b.fill(cx + 27, g, cz + 119, cx + 29, g, cz + 125, WATER);
        b.put(cx + 28, g + 1, cz + 122, p.accent());
        b.put(cx + 28, g + 2, cz + 122, p.light());
        palm(b, cx + 16, cz + 116, g, p);
        palm(b, cx + 40, cz + 128, g, p);
        b.fill(cx + 18, g + 1, cz + 124, cx + 22, g + 1, cz + 124, p.rail());
        b.fill(cx + 34, g + 1, cz + 124, cx + 38, g + 1, cz + 124, p.rail());
    }

    /** Parterres, yards and palm walks in the blocks the colonial buildings leave open. */
    private static void colonialYards(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // Hotel garden south of the inn.
        b.fill(cx - 82, g, cz - 108, cx - 58, g, cz - 70, p.ground());
        outline(b, cx - 82, cz - 108, cx - 58, cz - 70, g, p.foundation());
        parterre(b, cx - 70, cz - 88, g, p);
        palm(b, cx - 80, cz - 104, g, p);
        palm(b, cx - 60, cz - 74, g, p);
        // Garden north of the club.
        b.fill(cx - 50, g, cz - 52, cx - 22, g, cz - 4, p.ground());
        outline(b, cx - 50, cz - 52, cx - 22, cz - 4, g, p.foundation());
        wellHead(b, cx - 36, cz - 28, g, p);
        palm(b, cx - 46, cz - 48, g, p);
        palm(b, cx - 26, cz - 8, g, p);
        // A long garden strip between the residences and the processional axis.
        b.fill(cx - 20, g, cz + 64, cx - 8, g, cz + 136, p.ground());
        outline(b, cx - 20, cz + 64, cx - 8, cz + 136, g, p.foundation());
        b.monument(cx - 14, cz + 100, g + 1);
        palm(b, cx - 18, cz + 72, g, p);
        palm(b, cx - 10, cz + 76, g, p);
        palm(b, cx - 18, cz + 126, g, p);
        palm(b, cx - 10, cz + 130, g, p);
        b.fill(cx - 18, g + 1, cz + 88, cx - 10, g + 1, cz + 88, p.rail());
        b.fill(cx - 18, g + 1, cz + 112, cx - 10, g + 1, cz + 112, p.rail());
        // South-west garden and its well.
        b.fill(cx - 114, g, cz + 148, cx - 92, g, cz + 196, p.ground());
        outline(b, cx - 114, cz + 148, cx - 92, cz + 196, g, p.foundation());
        palm(b, cx - 114, cz + 152, g, p);
        palm(b, cx - 96, cz + 192, g, p);
        wellHead(b, cx - 105, cz + 172, g, p);
        // Palm walks along the east edge and the waterfront.
        for (int z = cz + 12; z <= cz + 96; z += 28) {
            palm(b, cx + 100, z, g, p);
        }
        for (int x : new int[]{cx - 60, cx - 20, cx + 20, cx + 44, cx + 100}) {
            palm(b, x, cz + 188, g, p);
        }
    }

    /** A raised rectangular parterre: kerb, soil and a low hedge on both long sides. */
    private static void parterre(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x - 6, g + 1, z - 4, x + 6, g + 1, z + 4, p.weathered());
        b.fill(x - 5, g + 1, z - 3, x + 5, g + 1, z + 3, FARMLAND);
        for (int dx = -4; dx <= 4; dx += 2) {
            b.put(x + dx, g + 2, z - 2, LEAVES);
            b.put(x + dx, g + 2, z + 2, LEAVES);
        }
        b.put(x, g + 2, z, p.accent());
        b.put(x, g + 3, z, p.light());
    }

    // ------------------------------------------------------------------ lagoon shrine

    private static void lagoonShrine(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // The boardwalk from the waterfront road.
        b.fill(cx + 238, g + 1, cz + 214, cx + 241, g + 1, cz + 246, p.frame());
        for (int z = cz + 214; z <= cz + 246; z += 6) {
            b.fill(cx + 238, g - 3, z, cx + 238, g, z, p.accent());
            b.fill(cx + 241, g - 3, z, cx + 241, g, z, p.accent());
        }
        b.fill(cx + 238, g + 2, cz + 214, cx + 238, g + 2, cz + 246, p.rail());
        b.fill(cx + 241, g + 2, cz + 214, cx + 241, g + 2, cz + 246, p.rail());
        // The stilt platform.
        b.fill(cx + 228, g + 1, cz + 246, cx + 252, g + 1, cz + 274, p.frame());
        for (int x = cx + 228; x <= cx + 252; x += 6) {
            for (int z = cz + 246; z <= cz + 274; z += 6) {
                b.fill(x, g - 3, z, x, g, z, p.accent());
            }
        }
        b.fill(cx + 228, g + 2, cz + 274, cx + 252, g + 2, cz + 274, p.rail());
        b.fill(cx + 228, g + 2, cz + 246, cx + 228, g + 2, cz + 274, p.rail());
        b.fill(cx + 252, g + 2, cz + 246, cx + 252, g + 2, cz + 274, p.rail());
        // The shrine hut: front altar room, hidden back sanctum.
        int hx0 = cx + 234;
        int hx1 = cx + 246;
        int hz0 = cz + 252;
        int hz1 = cz + 266;
        b.walls(hx0, g + 2, hz0, hx1, g + 6, hz1, p.weathered());
        b.fill(hx0 + 1, g + 2, hz0 + 1, hx1 - 1, g + 6, hz1 - 1, AIR);
        b.fill(hx0, g + 7, hz0, hx1, g + 7, hz1, p.roof());
        b.pitchedRoof(hx0 - 1, hz0 - 1, hx1 + 1, hz1 + 1, g + 7, 4, 0);
        b.fill(hx0 + 1, g + 2, cz + 260, hx1 - 1, g + 6, cz + 260, p.weathered());
        b.fill(cx + 240, g + 2, cz + 260, cx + 240, g + 3, cz + 260, AIR);
        door(b, p, cx + 240, g + 2, hz1, Direction.SOUTH);
        // The front altar.
        b.fill(cx + 239, g + 2, cz + 263, cx + 241, g + 2, cz + 263, p.foundation());
        b.put(cx + 240, g + 3, cz + 263, GOLD);
        b.put(cx + 240, g + 4, cz + 263, p.light());
        b.put(hx0 + 1, g + 4, hz1, p.light());
        b.put(hx1 - 1, g + 4, hz1, p.light());
        wallSign(b, cx + 235, g + 4, hz1, Direction.SOUTH,
                "LAGOON SHRINE", "LEAVE OFFERINGS");
        // The hidden sanctum.
        b.chest(cx + 236, g + 2, cz + 256, SiteLoot.RELIC);
        b.chest(cx + 244, g + 2, cz + 256, SiteLoot.CULT);
        b.put(cx + 240, g + 2, cz + 254, GOLD);
        wallSign(b, cx + 240, g + 4, hz0, Direction.SOUTH,
                "LAGOON MOTHER", "OFFERINGS TAKEN", "SHE WAITS");
        b.put(hx0 + 1, g + 5, hz0 + 1, Palette.cobweb());
        b.put(hx1 - 1, g + 5, hz0 + 1, Palette.cobweb());
        // The water around it.
        canoe(b, cx + 226, cz + 268, g, p, true);
        canoe(b, cx + 254, cz + 252, g, p, true);
        for (int i = 0; i < 10; i++) {
            int lx = cx + 224 + Math.floorMod(i * 7, 34);
            int lz = cz + 248 + Math.floorMod(i * 11, 30);
            b.put(lx, g, lz, LILY);
        }
        signPost(b, cx + 240, cz + 212, g, p, Direction.NORTH,
                "LAGOON", "DEEP WATER", "NO SWIMMING");
        signPost(b, cx + 234, cz + 270, g + 1, p, Direction.SOUTH,
                "LAGOON SHRINE", "DO NOT TAKE");
        b.put(cx + 226, g + 1, cz + 274, LEAVES);
        b.put(cx + 254, g + 1, cz + 246, LEAVES);
    }

    // ------------------------------------------------------------------ rail station

    /** A small terminus at the south-west edge, with a straight level track approach. */
    private static void railStation(StructureBuilder b, int cx, int cz, int g, Surface surface,
                                    Palette p) {
        int sy = surface.at(cx - 106, cz + 305);
        int sbase = Math.min(sy - 4, b.groundY(cx - 106, cz + 305) - 1);
        // Straight, level track: a solid bed cut/filled to the station level, rails on top.
        int tx = cx - 120;
        for (int z = cz + 240; z <= cz + 314; z++) {
            int base = Math.min(sy, b.groundY(tx, z));
            int clearTop = Math.max(sy + 4, b.groundY(tx, z) + 1);
            b.fill(tx - 1, base, z, tx + 1, sy, z, p.foundation());
            b.fill(tx - 1, sy + 1, z, tx + 1, clearTop, z, AIR);
            b.put(tx, sy + 1, z, Blocks.RAIL.defaultBlockState());
        }
        // Platform, station building and its plinth.
        b.fill(cx - 118, sbase, cz + 296, cx - 114, sy + 1, cz + 314, p.foundation());
        b.fill(cx - 118, sy + 1, cz + 296, cx - 114, sy + 1, cz + 314, p.ground());
        b.fill(cx - 118, sy + 1, cz + 296, cx - 118, sy + 1, cz + 314, p.roofSlab());
        b.fill(cx - 114, sbase, cz + 298, cx - 98, sy + 1, cz + 312, p.foundation());
        b.walls(cx - 114, sy + 2, cz + 298, cx - 98, sy + 7, cz + 312, WHITE);
        b.fill(cx - 113, sy + 2, cz + 299, cx - 99, sy + 7, cz + 311, AIR);
        b.fill(cx - 114, sy + 8, cz + 298, cx - 98, sy + 8, cz + 312, p.roof());
        b.pitchedRoof(cx - 115, cz + 297, cx - 97, cz + 313, sy + 8, 4, 1);
        door(b, p, cx - 114, sy + 2, cz + 305, Direction.WEST);
        b.window(cx - 106, sy + 4, cz + 298, 2, 1, true);
        b.window(cx - 106, sy + 4, cz + 312, 2, 1, true);
        wallSign(b, cx - 114, sy + 6, cz + 305, Direction.WEST,
                "RAILWAY STATION", "LAGOS JUNCTION");
        b.put(cx - 112, sy + 5, cz + 300, p.light());
        b.put(cx - 100, sy + 5, cz + 310, p.light());
        b.put(cx - 106, sy + 2, cz + 305, p.frame());
        // Water tower at the platform end.
        b.fill(cx - 96, sbase, cz + 300, cx - 90, sy + 1, cz + 304, p.foundation());
        b.fill(cx - 94, sy + 2, cz + 301, cx - 92, sy + 7, cz + 303, p.accent());
        b.fill(cx - 96, sy + 8, cz + 300, cx - 90, sy + 10, cz + 304, p.roof());
        b.put(cx - 93, sy + 11, cz + 302, p.light());
        // Goods shed in the yard south of the platform.
        b.fill(cx - 114, sbase, cz + 316, cx - 98, sy + 1, cz + 326, p.foundation());
        b.walls(cx - 114, sy + 2, cz + 316, cx - 98, sy + 5, cz + 326, p.weathered());
        b.fill(cx - 113, sy + 2, cz + 317, cx - 99, sy + 5, cz + 325, AIR);
        b.fill(cx - 114, sy + 6, cz + 316, cx - 98, sy + 6, cz + 326, p.roofSlab());
        door(b, p, cx - 114, sy + 2, cz + 321, Direction.WEST);
        b.put(cx - 102, sy + 2, cz + 318, BARREL);
        b.put(cx - 106, sy + 2, cz + 324, HAY);
        b.put(cx - 110, sy + 2, cz + 318, p.foundation());
        // Yard signs and lamps.
        signPost(b, cx - 118, cz + 290, surface.at(cx - 118, cz + 290), p, Direction.NORTH,
                "RAILWAY STATION", "GOODS YARD");
        signPost(b, cx - 96, cz + 318, sy, p, Direction.SOUTH, "GOODS YARD");
        lamp(b, cx - 118, cz + 294, sy, p);
        lamp(b, cx - 118, cz + 316, sy, p);
    }

    // ------------------------------------------------------------------ south shore

    private static void southShore(StructureBuilder b, int cx, int cz, int g, Surface surface,
                                   Palette p) {
        // The palm grove and its plots, ruin and cemetery.
        int fy = surface.at(cx - 294, cz + 262);
        int cy = surface.at(cx - 225, cz + 264);
        farmPlot(b, cx - 306, cz + 248, cx - 282, cz + 276, fy, p);
        b.walls(cx - 242, cy + 1, cz + 250, cx - 208, cy + 2, cz + 278, p.weathered());
        b.fill(cx - 226, cy + 1, cz + 278, cx - 222, cy + 2, cz + 278, AIR);
        for (int x = cx - 238; x <= cx - 212; x += 8) {
            headstone(b, x, cz + 256, cy, p, x);
            headstone(b, x + 4, cz + 270, cy, p, x + 1);
        }
        house(b, cx - 188, cz + 252, cx - 166, cz + 270, g, p, p.wall(), g + 4, true,
                Direction.SOUTH, "FISHER HAMLET");
        house(b, cx - 160, cz + 252, cx - 140, cz + 270, g, p, p.wall(), g + 4, true,
                Direction.SOUTH, "FISHER HAMLET");
        wellHead(b, cx - 174, cz + 278, g, p);
        b.fill(cx - 165, g, cz + 270, cx - 162, g, cz + 284, p.ground());
        // The ruined compound, on the graded rim.
        int ry = surface.at(cx - 266, cz + 310);
        b.walls(cx - 282, ry + 1, cz + 296, cx - 250, ry + 3, cz + 324, p.weathered());
        b.fill(cx - 278, ry + 1, cz + 296, cx - 274, ry + 3, cz + 296, AIR);
        b.fill(cx - 266, ry + 1, cz + 324, cx - 262, ry + 3, cz + 324, AIR);
        b.fill(cx - 282, ry + 1, cz + 312, cx - 272, ry + 1, cz + 312, AIR);
        b.fill(cx - 278, ry + 1, cz + 300, cx - 276, ry + 1, cz + 304, p.rubble());
        b.put(cx - 260, ry + 1, cz + 306, p.rubble());
        b.put(cx - 258, ry + 1, cz + 318, p.weathered());
        b.put(cx - 268, ry + 1, cz + 316, p.frame());
        b.put(cx - 268, ry + 2, cz + 316, p.frame());
        bigTree(b, cx - 266, cz + 314, ry, p);
        // The palm grove: authored positions only, each on the graded surface.
        int[][] palms = {
                {-316, 238}, {-300, 330}, {-268, 242}, {-246, 238}, {-214, 238},
                {-188, 240}, {-160, 244}, {-132, 244}, {-88, 330}, {-80, 250},
                {-320, 318}, {-296, 296}, {-232, 330}, {-204, 320}, {-176, 332},
                {-140, 322}, {-88, 310}, {-72, 244}
        };
        for (int[] spot : palms) {
            palmAt(b, surface, cx + spot[0], cz + spot[1], p);
        }
        // The paths through the shore follow the graded rim.
        surfacePath(b, surface, cx - 320, cz + 286, cx - 60, cz + 286, 4, p.ground());
        surfacePath(b, surface, cx + 60, cz + 286, cx + 150, cz + 286, 4, p.ground());
        surfacePath(b, surface, cx + 120, cz + 278, cx + 120, cz + 300, 4, p.frame());
        // The east fish yard.
        b.fill(cx + 76, g, cz + 244, cx + 146, g, cz + 278, p.ground());
        outline(b, cx + 76, cz + 244, cx + 146, cz + 278, g, p.foundation());
        for (int x = cx + 100; x <= cx + 124; x += 6) {
            b.fill(x, g + 1, cz + 252, x, g + 4, cz + 252, p.accent());
            b.fill(x, g + 1, cz + 268, x, g + 4, cz + 268, p.accent());
        }
        b.fill(cx + 98, g + 5, cz + 250, cx + 126, g + 5, cz + 270, p.roof());
        b.pitchedRoof(cx + 98, cz + 250, cx + 126, cz + 270, g + 5, 4, 1);
        b.fill(cx + 102, g + 1, cz + 256, cx + 122, g + 1, cz + 256, p.foundation());
        b.put(cx + 104, g + 2, cz + 256, KELP);
        b.put(cx + 118, g + 2, cz + 256, KELP);
        fishRack(b, cx + 84, cz + 272, g, p);
        fishRack(b, cx + 94, cz + 272, g, p);
        fishRack(b, cx + 128, cz + 272, g, p);
        fishRack(b, cx + 138, cz + 272, g, p);
        canoe(b, cx + 86, cz + 258, g + 1, p, true);
        canoe(b, cx + 132, cz + 262, g + 1, p, true);
        b.put(cx + 112, g + 1, cz + 276, BARREL);
        b.put(cx + 116, g + 1, cz + 276, BARREL);
        b.put(cx + 90, g + 1, cz + 248, HAY);
        signPost(b, cx + 110, cz + 244, g, p, Direction.NORTH, "FISH YARD", "SMOKING");
        // A small landing on the lagoon, lifted to the graded shore.
        int ly = Math.max(g - 1, surface.at(cx + 152, cz + 302));
        b.fill(cx + 146, ly + 1, cz + 300, cx + 158, ly + 1, cz + 304, p.frame());
        for (int x = cx + 148; x <= cx + 158; x += 5) {
            b.fill(x, ly - 3, cz + 300, x, ly, cz + 300, p.accent());
            b.fill(x, ly - 3, cz + 304, x, ly, cz + 304, p.accent());
        }
        b.fill(cx + 146, ly + 2, cz + 300, cx + 158, ly + 2, cz + 300, p.rail());
        b.fill(cx + 146, ly + 2, cz + 304, cx + 158, ly + 2, cz + 304, p.rail());
        canoe(b, cx + 166, cz + 302, g, p, false);
        // Shore palms.
        palmAt(b, surface, cx + 70, cz + 250, p);
        palmAt(b, surface, cx + 144, cz + 248, p);
        palmAt(b, surface, cx + 64, cz + 330, p);
        palmAt(b, surface, cx + 146, cz + 330, p);
        palmAt(b, surface, cx + 30, cz + 330, p);
        palmAt(b, surface, cx - 30, cz + 330, p);
    }

    // ------------------------------------------------------------------ shared helpers

    /** A street that follows the graded city surface along its dominant axis. */
    private static void surfacePath(StructureBuilder b, Surface surface, int x0, int z0, int x1,
                                    int z1, int width, BlockState state) {
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
                int y = surface.at(x, cz);
                for (int z = cz + lo; z <= cz + hi; z++) {
                    b.put(x, y, z, state);
                }
            }
        } else {
            int cx = (x0 + x1) / 2;
            for (int z = az; z <= bz; z++) {
                int y = surface.at(cx, z);
                for (int x = cx + lo; x <= cx + hi; x++) {
                    b.put(x, y, z, state);
                }
            }
        }
    }

    /** A palm whose base sits on the graded surface. */
    private static void palmAt(StructureBuilder b, Surface surface, int x, int z, Palette p) {
        palm(b, x, z, surface.at(x, z), p);
    }

    private static void outline(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                                BlockState state) {
        b.fill(x0, y, z0, x1, y, z0, state);
        b.fill(x0, y, z1, x1, y, z1, state);
        b.fill(x0, y, z0, x0, y, z1, state);
        b.fill(x1, y, z0, x1, y, z1, state);
    }

    private static void quoins(StructureBuilder b, int x0, int z0, int x1, int z1, int y0, int y1,
                               Palette p) {
        for (int y = y0; y <= y1; y += 2) {
            b.put(x0, y, z0, p.accent());
            b.put(x1, y, z0, p.accent());
            b.put(x0, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
    }

    /** A wall-mounted sign, flush with the wall plane and supported by the course below. */
    private static void wallSign(StructureBuilder b, int x, int y, int z, Direction facing,
                                 String... lines) {
        CityPlanSupport.sign(b, x, y, z, facing, lines);
    }

    /** A freestanding signpost: two timber blocks and the sign on top. */
    private static void signPost(StructureBuilder b, int x, int z, int g, Palette p,
                                 Direction facing, String... lines) {
        b.put(x, g + 1, z, p.accent());
        b.put(x, g + 2, z, p.accent());
        CityPlanSupport.sign(b, x, g + 3, z, facing, lines);
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

    private static void lamp(StructureBuilder b, int x, int z, int g, Palette p) {
        b.put(x, g + 1, z, p.accent());
        b.put(x, g + 2, z, p.accent());
        b.put(x, g + 3, z, p.light());
    }

    private static void palm(StructureBuilder b, int x, int z, int g, Palette p) {
        int h = 6 + Math.floorMod(x * 7 + z * 13, 3);
        for (int y = 1; y <= h; y++) {
            b.put(x, g + y, z, p.frame());
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 3) {
                    b.put(x + dx, g + h + 1, z + dz, LEAVES);
                }
            }
        }
        b.put(x, g + h + 2, z, LEAVES);
        b.put(x - 2, g + h, z, LEAVES);
        b.put(x + 2, g + h, z, LEAVES);
        b.put(x, g + h, z - 2, LEAVES);
        b.put(x, g + h, z + 2, LEAVES);
    }

    private static void bigTree(StructureBuilder b, int x, int z, int g, Palette p) {
        for (int y = 1; y <= 11; y++) {
            b.put(x, g + y, z, p.accent());
        }
        for (int i = 0; i < 3; i++) {
            int r = 5 - i;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) <= r + 1) {
                        b.put(x + dx, g + 10 + i, z + dz, LEAVES);
                    }
                }
            }
        }
        b.put(x, g + 13, z, LEAVES);
    }

    private static void stall(StructureBuilder b, int x, int z, int g, Palette p,
                              BlockState awning, int variant) {
        int y = g + 1;
        b.fill(x, y, z, x, y + 2, z, p.accent());
        b.fill(x + 4, y, z, x + 4, y + 2, z, p.accent());
        b.fill(x, y, z + 3, x, y + 2, z + 3, p.accent());
        b.fill(x + 4, y, z + 3, x + 4, y + 2, z + 3, p.accent());
        b.fill(x, y + 3, z, x + 4, y + 3, z + 3, awning);
        b.fill(x + 1, y, z + 1, x + 3, y, z + 1, p.foundation());
        b.put(x + 1, y + 1, z + 1, variant % 3 == 0 ? BARREL : p.foundation());
        b.put(x + 3, y + 1, z + 1, KELP);
        b.put(x + 2, y + 2, z + 2, p.light());
        if (variant % 2 == 0) {
            b.put(x + 1, y + 1, z + 2, p.frame());
        }
    }

    private static void canoe(StructureBuilder b, int x, int z, int g, Palette p,
                              boolean alongZ) {
        if (alongZ) {
            b.fill(x, g - 1, z - 4, x + 1, g - 1, z + 4, p.frame());
            b.fill(x - 1, g - 1, z - 3, x - 1, g, z + 3, p.accent());
            b.fill(x + 2, g - 1, z - 3, x + 2, g, z + 3, p.accent());
            b.fill(x, g, z - 1, x + 1, g, z + 1, p.roofSlab());
            b.put(x, g + 1, z + 3, p.rail());
            b.put(x + 1, g + 1, z - 3, p.rail());
        } else {
            b.fill(x - 4, g - 1, z, x + 4, g - 1, z + 1, p.frame());
            b.fill(x - 3, g - 1, z - 1, x + 3, g, z - 1, p.accent());
            b.fill(x - 3, g - 1, z + 2, x + 3, g, z + 2, p.accent());
            b.fill(x - 1, g, z, x + 1, g, z + 1, p.roofSlab());
            b.put(x + 3, g + 1, z, p.rail());
            b.put(x - 3, g + 1, z + 1, p.rail());
        }
    }

    private static void wellHead(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x - 2, g, z - 2, x + 2, g, z + 2, p.foundation());
        b.fill(x - 1, g - 1, z - 1, x + 1, g - 1, z + 1, WATER);
        b.fill(x - 1, g, z - 1, x + 1, g, z + 1, AIR);
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                b.fill(x + dx, g + 1, z + dz, x + dx, g + 3, z + dz, p.accent());
            }
        }
        b.fill(x - 2, g + 4, z - 2, x + 2, g + 4, z + 2, p.roofSlab());
        b.put(x, g + 2, z, p.light());
    }

    private static void drum(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x, g + 1, z, x + 1, g + 1, z, p.accent());
        b.put(x, g + 2, z, NOTE);
        b.put(x + 1, g + 2, z, NOTE);
        b.put(x, g + 3, z, p.frame());
    }

    private static void crateStack(StructureBuilder b, int x, int z, int g, Palette p, int height) {
        for (int i = 1; i <= height; i++) {
            b.put(x, g + i, z, p.foundation());
        }
        if (height > 1) {
            b.put(x + 1, g + 1, z, p.foundation());
        }
    }

    private static void fishRack(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x, g + 1, z, x, g + 2, z, p.accent());
        b.fill(x + 4, g + 1, z, x + 4, g + 2, z, p.accent());
        b.fill(x, g + 3, z, x + 4, g + 3, z, p.rail());
        b.put(x + 1, g + 2, z, KELP);
        b.put(x + 3, g + 2, z, KELP);
    }

    private static void headstone(StructureBuilder b, int x, int z, int g, Palette p, int variant) {
        b.put(x, g + 1, z, variant % 2 == 0 ? p.weathered() : p.foundation());
        b.put(x, g + 2, z, p.roofSlab());
    }

    private static void farmPlot(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                 Palette p) {
        b.fill(x0, g, z0, x1, g, z1, FARMLAND);
        for (int x = x0 + 1; x <= x1 - 1; x += 2) {
            b.put(x, g + 1, z0 + 1, WHEAT);
            b.put(x, g + 1, z1 - 1, WHEAT);
        }
        b.fill(x0 - 1, g + 1, z0 - 1, x1 + 1, g + 1, z0 - 1, p.rail());
        b.fill(x0 - 1, g + 1, z1 + 1, x1 + 1, g + 1, z1 + 1, p.rail());
        b.fill(x0 - 1, g + 1, z0 - 1, x0 - 1, g + 1, z1 + 1, p.rail());
        b.fill(x1 + 1, g + 1, z0 - 1, x1 + 1, g + 1, z1 + 1, p.rail());
    }

    private static void crane(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x - 1, g, z - 1, x + 1, g, z + 1, p.foundation());
        b.fill(x, g + 1, z, x, g + 10, z, p.accent());
        b.fill(x, g + 9, z, x + 7, g + 9, z, p.accent());
        b.fill(x + 7, g + 5, z, x + 7, g + 8, z, p.rail());
        b.put(x + 7, g + 4, z, p.foundation());
        b.put(x + 6, g + 4, z, p.foundation());
        b.put(x, g + 11, z, p.light());
    }

    /** A mudbrick or whitewashed house with a door on the requested face. */
    private static void house(StructureBuilder b, int x0, int z0, int x1, int z1, int g, Palette p,
                              BlockState wall, int top, boolean pitched, Direction face,
                              String... lines) {
        b.fill(x0, g, z0, x1, g, z1, p.foundation());
        b.walls(x0, g + 1, z0, x1, top, z1, wall);
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, top, z1 - 1, AIR);
        if (top >= g + 7) {
            b.fill(x0 + 1, g + 4, z0 + 1, x1 - 1, g + 4, z1 - 1, p.roof());
        }
        if (pitched) {
            b.fill(x0, top + 1, z0, x1, top + 1, z1, p.roof());
            b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, top + 1, 4,
                    z1 - z0 >= x1 - x0 ? 1 : 0);
        } else {
            b.fill(x0 - 1, top + 1, z0 - 1, x1 + 1, top + 1, z1 + 1, p.roofSlab());
            b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, top + 2);
        }
        for (int x = x0 + 2; x <= x1 - 2; x += 4) {
            b.window(x, g + 2, z0, 2, 1, true);
            b.window(x, g + 2, z1, 2, 1, true);
        }
        for (int z = z0 + 3; z <= z1 - 3; z += 5) {
            b.window(x0, g + 2, z, 2, 1, true);
            b.window(x1, g + 2, z, 2, 1, true);
        }
        if (top >= g + 7) {
            for (int x = x0 + 2; x <= x1 - 2; x += 4) {
                b.window(x, g + 5, z0, 2, 1, true);
                b.window(x, g + 5, z1, 2, 1, true);
            }
        }
        int dx = (x0 + x1) / 2;
        int dz = (z0 + z1) / 2;
        switch (face) {
            case NORTH -> door(b, p, dx, g + 1, z0, face);
            case SOUTH -> door(b, p, dx, g + 1, z1, face);
            case WEST -> door(b, p, x0, g + 1, dz, face);
            default -> door(b, p, x1, g + 1, dz, face);
        }
        if (lines.length > 0) {
            switch (face) {
                case NORTH -> wallSign(b, dx, top, z0, face, lines);
                case SOUTH -> wallSign(b, dx, top, z1, face, lines);
                case WEST -> wallSign(b, x0, top, dz, face, lines);
                default -> wallSign(b, x1, top, dz, face, lines);
            }
        }
    }

    /** A three-storey whitewashed office block with a parapet. */
    private static void office(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                               Palette p, Direction face, String... lines) {
        int top = g + 12;
        b.fill(x0, g, z0, x1, g, z1, p.foundation());
        b.walls(x0, g + 1, z0, x1, top, z1, WHITE);
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, top, z1 - 1, AIR);
        for (int f = 1; f <= 2; f++) {
            b.fill(x0 + 1, g + f * 4, z0 + 1, x1 - 1, g + f * 4, z1 - 1, p.roof());
            b.walls(x0, g + f * 4, z0, x1, g + f * 4, z1, p.accent());
        }
        b.fill(x0 - 1, top + 1, z0 - 1, x1 + 1, top + 1, z1 + 1, p.roofSlab());
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, top + 2);
        for (int f = 0; f < 3; f++) {
            int wy = g + 2 + f * 4;
            for (int x = x0 + 2; x <= x1 - 2; x += 4) {
                b.window(x, wy, z0, 2, 1, true);
                b.window(x, wy, z1, 2, 1, true);
            }
            for (int z = z0 + 3; z <= z1 - 3; z += 5) {
                b.window(x0, wy, z, 2, 1, true);
                b.window(x1, wy, z, 2, 1, true);
            }
        }
        int dx = (x0 + x1) / 2;
        int dz = (z0 + z1) / 2;
        switch (face) {
            case NORTH -> door(b, p, dx, g + 1, z0, face);
            case SOUTH -> door(b, p, dx, g + 1, z1, face);
            case WEST -> door(b, p, x0, g + 1, dz, face);
            default -> door(b, p, x1, g + 1, dz, face);
        }
        if (lines.length > 0) {
            switch (face) {
                case NORTH -> wallSign(b, dx, top - 1, z0, face, lines);
                case SOUTH -> wallSign(b, dx, top - 1, z1, face, lines);
                case WEST -> wallSign(b, x0, top - 1, dz, face, lines);
                default -> wallSign(b, x1, top - 1, dz, face, lines);
            }
        }
    }
}
