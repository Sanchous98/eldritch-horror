package com.sanchous98.eldritchhorror.world.loc.city.plan;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.SiteLoot;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.city.CitySurface;
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
 * Mumbai / Bombay: the harbour gate and the Gateway of India, the docks with their warehouses,
 * cranes and railway sidings, Crawford Market's awnings, Malabar Hill's bungalows and gardens,
 * the mill district's brick chimneys and workers' housing, and the Fort's offices, church and
 * theatre. Every block is placed deliberately; there is no layout RNG.
 *
 * <p>The whole city is written on every call, as the plan contract requires: the builder keeps
 * only the slice that falls inside the generating chunk, so every decision is absolute and
 * chunk-independent. The harbour water, the quay, the pier and the dock basin are authored cuts
 * into the levelled city surface; the districts sit around them.
 */
public final class MumbaiPlan implements CityPlan {

    /** Authored half-extent; everything below stays inside +/-340. */
    private static final int HALF_EXTENT = 340;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState DECK = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private static final BlockState TRUNK = Blocks.JUNGLE_LOG.defaultBlockState();
    private static final BlockState PALM_LEAF = Blocks.JUNGLE_LEAVES.defaultBlockState()
            .setValue(BlockStateProperties.PERSISTENT, true);
    private static final BlockState HEDGE = Blocks.OAK_LEAVES.defaultBlockState()
            .setValue(BlockStateProperties.PERSISTENT, true);
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState COAL = Blocks.COAL_BLOCK.defaultBlockState();
    private static final BlockState HAY = Blocks.HAY_BLOCK.defaultBlockState();
    private static final BlockState BOOKS = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState BARREL = Blocks.BARREL.defaultBlockState();
    private static final BlockState CAULDRON = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState LILY = Blocks.LILY_PAD.defaultBlockState();
    private static final BlockState RAIL_EW = Blocks.RAIL.defaultBlockState()
            .setValue(BlockStateProperties.RAIL_SHAPE, RailShape.EAST_WEST);

    @Override
    public String cityId() {
        return "mumbai";
    }

    @Override
    public int halfExtent() {
        return HALF_EXTENT;
    }

    @Override
    public void build(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Legacy entry point: the shared pass now calls the context overload, but keeping this
        // implementation makes the plan usable from older callers. A flat surface at `ground`
        // reproduces the old behaviour exactly.
        CitySurface flat = CitySurface.of(b, cx, cz, 10000, ground, 0);
        build(b, new CityPlanContext(cx, cz, ground, HALF_EXTENT, flat), p);
    }

    @Override
    public void build(StructureBuilder b, CityPlanContext ctx, Palette p) {
        CitySurface s = ctx.surface();
        int cx = ctx.cx();
        int cz = ctx.cz();
        int harbourY = s.at(cx, cz + 260);
        streets(b, s, cx, cz, p);
        arrivalGate(b, cx, cz, harbourY, p);
        harbour(b, s, cx, cz, harbourY, p);
        gatewayOfIndia(b, cx, cz, harbourY, p);
        docks(b, s, cx, cz, p);
        crawford(b, s, cx, cz, p);
        malabar(b, s, cx, cz, p);
        mills(b, s, cx, cz, p);
        fort(b, cx, cz, s.at(cx, cz), p);
        northGate(b, cx, cz, s.at(cx, cz - 296), p);
        puddles(b, s, cx, cz, p);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.LANDMARK, 0, 0, 300, "harbour gate"),
                new CityPoi("gate_signpost", CityPoi.Kind.SIGN, 0, 3, 316,
                        "GATEWAY|DOCKS|CRAWFORD MARKET|MALABAR HILL|MILLS"),
                new CityPoi("city_notices", CityPoi.Kind.SIGN, 13, 3, 313, "CITY NOTICES"),
                new CityPoi("gateway", CityPoi.Kind.LANDMARK, 0, 0, 220, "gateway of india"),
                new CityPoi("ferry_dock", CityPoi.Kind.LANDMARK, 60, 0, 260, "ferries,boats"),
                new CityPoi("harbour_port", CityPoi.Kind.PORT, 60, 0, 252, "quay,ferries"),
                new CityPoi("harbour_quest_board", CityPoi.Kind.QUEST_BOARD, 0, 0, 180,
                        "mumbai_dock_strike,mumbai_missing_actor"),
                new CityPoi("taj_hotel", CityPoi.Kind.INN, -80, 0, 120, "hotel_keeper"),
                new CityPoi("hotel_keeper", CityPoi.Kind.NPC, -80, 0, 124, "taj_hotel"),
                new CityPoi("crawford_shop", CityPoi.Kind.SHOP, -240, 0, -80, "merchant"),
                new CityPoi("merchant", CityPoi.Kind.NPC, -238, 0, -78, "crawford_shop"),
                new CityPoi("hakim", CityPoi.Kind.HEALER, -240, 0, 120, "hakim"),
                new CityPoi("hakim_npc", CityPoi.Kind.NPC, -238, 0, 118, "hakim"),
                new CityPoi("dock_office", CityPoi.Kind.NPC, 220, 0, 0,
                        "dock_master,mumbai_dock_strike"),
                new CityPoi("dock_master", CityPoi.Kind.NPC, 218, 0, -2,
                        "dock_master,mumbai_dock_strike"),
                new CityPoi("docks_station", CityPoi.Kind.STATION, 320, 1, -21,
                        "docks rail yard;east_west"),
                new CityPoi("theatre", CityPoi.Kind.NPC, -60, 0, -120,
                        "theatre_manager,mumbai_missing_actor"),
                new CityPoi("theatre_manager", CityPoi.Kind.NPC, -60, 0, -124,
                        "theatre_manager,mumbai_missing_actor"),
                new CityPoi("mill_cellar", CityPoi.Kind.SECRET, 240, -4, 240,
                        "ledgers,shrine"),
                new CityPoi("mill_worker", CityPoi.Kind.NPC, 198, 0, 250,
                        "mill_worker,mumbai_dock_strike"),
                new CityPoi("malabar_bungalow", CityPoi.Kind.SECRET, -240, 1, 220,
                        "locked_study,mask"),
                new CityPoi("bungalow_servant", CityPoi.Kind.NPC, -240, 0, 236,
                        "bungalow_servant,mumbai_missing_actor"),
                new CityPoi("crawford_clock", CityPoi.Kind.LANDMARK, -270, 0, -16, "market clock"),
                new CityPoi("towers_of_silence", CityPoi.Kind.LANDMARK, -301, 0, 302, "dakhma"),
                new CityPoi("fort_church", CityPoi.Kind.LANDMARK, 49, 0, -175, "st thomas"),
                new CityPoi("fort_offices", CityPoi.Kind.LANDMARK, 0, 0, -240, "secretariat"),
                new CityPoi("lookout", CityPoi.Kind.LANDMARK, -120, 2, 260, "sea view"));
    }

    // ================================================================== shared helpers

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

    /** A wall-mounted sign with a small bracket beneath it. */
    private static void sign(StructureBuilder b, Palette p, int x, int y, int z,
                             Direction facing, String... lines) {
        b.put(x, y - 1, z, p.accent());
        CityPlanSupport.sign(b, x, y, z, facing, lines);
    }

    /** A freestanding signpost: two timber blocks and the sign on top. */
    private static void signPost(StructureBuilder b, int x, int z, int ground, Palette p,
                                 Direction facing, String... lines) {
        b.put(x, ground + 1, z, p.accent());
        b.put(x, ground + 2, z, p.accent());
        CityPlanSupport.sign(b, x, ground + 3, z, facing, lines);
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

    private static void palm(StructureBuilder b, int x, int z, int ground) {
        int height = 6 + Math.floorMod(x * 7 + z * 13, 3);
        for (int y = ground + 1; y <= ground + height; y++) {
            b.put(x, y, z, TRUNK);
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 2) {
                    b.put(x + dx, ground + height + 1, z + dz, PALM_LEAF);
                }
            }
        }
        b.put(x, ground + height + 2, z, PALM_LEAF);
        b.put(x - 1, ground + height, z, PALM_LEAF);
        b.put(x + 1, ground + height, z, PALM_LEAF);
        b.put(x, ground + height, z - 1, PALM_LEAF);
        b.put(x, ground + height, z + 1, PALM_LEAF);
    }

    private static void tree(StructureBuilder b, int x, int z, int ground) {
        int height = 4 + Math.floorMod(x + z, 3);
        for (int y = ground + 1; y <= ground + height; y++) {
            b.put(x, y, z, TRUNK);
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 2) {
                    b.put(x + dx, ground + height + 1, z + dz, HEDGE);
                }
            }
        }
        b.put(x, ground + height + 2, z, HEDGE);
    }

    private static void hedge(StructureBuilder b, int x0, int z0, int x1, int z1, int ground) {
        b.fill(x0, ground + 1, z0, x1, ground + 2, z0, HEDGE);
        b.fill(x0, ground + 1, z1, x1, ground + 2, z1, HEDGE);
        b.fill(x0, ground + 1, z0, x0, ground + 2, z1, HEDGE);
        b.fill(x1, ground + 1, z0, x1, ground + 2, z1, HEDGE);
    }

    private static void crate(StructureBuilder b, int x, int z, int ground, Palette p,
                              int height) {
        for (int i = 1; i <= height; i++) {
            b.put(x, ground + i, z, p.foundation());
        }
    }

    private static void barrel(StructureBuilder b, int x, int z, int ground) {
        b.put(x, ground + 1, z, BARREL);
    }

    /** A simple hotel/clinic bed: a timber frame, a white mattress and a red pillow. */
    private static void bed(StructureBuilder b, int x, int y, int z, Palette p) {
        b.fill(x, y, z, x + 1, y, z + 2, p.roofSlab());
        b.fill(x, y + 1, z, x + 1, y + 1, z + 1, Materials.wool(DyeColor.WHITE));
        b.put(x, y + 1, z + 2, Materials.wool(DyeColor.RED));
    }

    private static void marketStall(StructureBuilder b, int x, int z, int ground, Palette p,
                                    BlockState canopy) {
        for (int dx = 0; dx <= 3; dx += 3) {
            for (int dz = 0; dz <= 2; dz += 2) {
                b.put(x + dx, ground + 1, z + dz, p.accent());
                b.put(x + dx, ground + 2, z + dz, p.accent());
            }
        }
        b.fill(x, ground + 3, z, x + 3, ground + 3, z + 2, canopy);
        b.fill(x, ground + 1, z + 1, x + 2, ground + 1, z + 1, p.foundation());
        b.put(x + 1, ground + 2, z + 1, p.light());
    }

    private static void well(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 2, ground, z - 2, x + 2, ground, z + 2, p.foundation());
        b.fill(x - 1, ground, z - 1, x + 1, ground, z + 1, AIR);
        b.fill(x - 1, ground - 1, z - 1, x + 1, ground - 1, z + 1, WATER);
        b.fill(x - 1, ground + 1, z - 1, x - 1, ground + 3, z - 1, p.accent());
        b.fill(x + 1, ground + 1, z - 1, x + 1, ground + 3, z - 1, p.accent());
        b.fill(x - 1, ground + 1, z + 1, x - 1, ground + 3, z + 1, p.accent());
        b.fill(x + 1, ground + 1, z + 1, x + 1, ground + 3, z + 1, p.accent());
        b.fill(x - 2, ground + 4, z - 2, x + 2, ground + 4, z + 2, p.roofSlab());
        b.put(x, ground + 3, z, p.light());
    }

    private static void fountain(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 5, ground, z - 5, x + 5, ground, z + 5, p.foundation());
        b.fill(x - 4, ground, z - 4, x + 4, ground, z + 4, p.ground());
        b.fill(x - 3, ground, z - 3, x + 3, ground, z + 3, WATER);
        b.fill(x - 3, ground - 1, z - 3, x + 3, ground - 1, z + 3, p.foundation());
        b.fill(x, ground + 1, z, x, ground + 3, z, p.accent());
        b.put(x, ground + 4, z, p.light());
        for (int dx = -4; dx <= 4; dx += 4) {
            for (int dz = -4; dz <= 4; dz += 4) {
                b.put(x + dx, ground + 1, z + dz, p.accent());
            }
        }
    }

    /** A solid pale-stone dome; the city uses them for the Gateway, the Taj and the clock tower. */
    private static void dome(StructureBuilder b, int x, int z, int y, int r, Palette p) {
        for (int i = 0; i <= r; i++) {
            int rr = r - i;
            for (int dx = -rr; dx <= rr; dx++) {
                for (int dz = -rr; dz <= rr; dz++) {
                    if (dx * dx + dz * dz <= rr * rr) {
                        b.put(x + dx, y + i, z + dz, p.accent());
                    }
                }
            }
        }
        b.put(x, y + r + 1, z, p.accent());
    }

    /** A brick mill chimney with pale basalt bands and a lantern at the mouth. */
    private static void chimney(StructureBuilder b, int x, int z, int ground, int height,
                                Palette p) {
        BlockState brick = Materials.terracotta(DyeColor.RED);
        for (int i = 0; i < height; i++) {
            int r = i < height / 2 ? 2 : (i < height - 2 ? 1 : 0);
            BlockState band = ((i / 4) & 1) == 0 ? brick : p.foundation();
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (r == 0 || Math.abs(dx) == r || Math.abs(dz) == r) {
                        b.put(x + dx, ground + 1 + i, z + dz, band);
                    }
                }
            }
        }
        b.fill(x - 1, ground + 1 + height, z - 1, x + 1, ground + 1 + height, z + 1, brick);
        b.put(x, ground + 2 + height, z, p.light());
    }

    /** A small boat/ferry: a dark timber hull, a deck and (for ferries) a white cabin and mast. */
    private static void boat(StructureBuilder b, int x, int z, int ground, int length,
                             int width, boolean ferry, Palette p) {
        int hx = length / 2;
        int hz = width / 2;
        b.fill(x - hx, ground - 2, z - hz, x + hx, ground - 2, z + hz, DECK);
        b.fill(x - hx, ground - 1, z - hz, x + hx, ground - 1, z - hz, DECK);
        b.fill(x - hx, ground - 1, z + hz, x + hx, ground - 1, z + hz, DECK);
        b.fill(x - hx, ground - 1, z - hz, x - hx, ground - 1, z + hz, DECK);
        b.fill(x + hx, ground - 1, z - hz, x + hx, ground - 1, z + hz, DECK);
        b.fill(x - hx + 1, ground, z - hz + 1, x + hx - 1, ground, z + hz - 1, DECK);
        if (ferry) {
            b.fill(x - 2, ground + 1, z - 1, x + 2, ground + 3, z + 1,
                    Materials.wool(DyeColor.WHITE));
            b.fill(x - 1, ground + 1, z, x + 1, ground + 2, z, AIR);
            b.put(x, ground + 4, z, p.light());
            b.fill(x + 4, ground + 1, z, x + 4, ground + 7, z, p.accent());
            b.put(x + 4, ground + 8, z, p.light());
        } else {
            b.fill(x - 1, ground + 1, z, x + 1, ground + 1, z, p.roofSlab());
            b.put(x, ground + 1, z - 1, p.accent());
        }
    }

    private static void crane(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 1, ground, z - 1, x + 1, ground, z + 1, p.foundation());
        b.fill(x, ground + 1, z, x, ground + 13, z, p.accent());
        b.fill(x, ground + 12, z, x + 14, ground + 12, z, p.accent());
        b.fill(x + 14, ground + 7, z, x + 14, ground + 11, z, p.rail());
        b.put(x + 14, ground + 6, z, p.foundation());
        b.fill(x - 2, ground + 10, z, x - 2, ground + 12, z, p.accent());
        b.put(x, ground + 14, z, p.light());
        for (int i = 0; i < 6; i++) {
            b.put(x + i, ground + 13 - i / 2, z, p.accent());
        }
    }

    private static void railLine(StructureBuilder b, int x0, int x1, int z, int ground,
                                 Palette p) {
        b.fill(x0, ground, z - 2, x1, ground, z + 2, p.rubble());
        for (int x = x0; x <= x1; x++) {
            if (x % 4 == 0) {
                b.put(x, ground, z, p.foundation());
            }
            b.put(x, ground + 1, z, RAIL_EW);
        }
    }

    private static void wagon(StructureBuilder b, int x, int z, int ground, Palette p,
                              boolean coal) {
        b.fill(x - 3, ground + 1, z - 1, x + 3, ground + 1, z + 1, p.accent());
        b.fill(x - 3, ground + 2, z - 1, x - 3, ground + 2, z + 1, p.accent());
        b.fill(x + 3, ground + 2, z - 1, x + 3, ground + 2, z + 1, p.accent());
        b.fill(x - 3, ground + 3, z - 1, x + 3, ground + 3, z + 1, p.roofSlab());
        if (coal) {
            b.fill(x - 2, ground + 2, z, x + 2, ground + 2, z, COAL);
            b.put(x, ground + 3, z, COAL);
        }
    }

    /** A 3x3 pale mask with dark eyes and mouth, mounted in the X-Y plane at {@code z}. */
    private static void maskPanel(StructureBuilder b, int x, int y, int z) {
        BlockState pale = Materials.concrete(DyeColor.WHITE);
        BlockState dark = Materials.concrete(DyeColor.BLACK);
        b.fill(x, y, z, x + 2, y + 2, z, pale);
        b.put(x, y + 1, z, dark);
        b.put(x + 2, y + 1, z, dark);
        b.put(x + 1, y, z, dark);
    }

    /** A 3x3 clock dial on the face of a tower. */
    private static void clockFace(StructureBuilder b, int x, int y, int z, Direction face) {
        BlockState pale = Materials.concrete(DyeColor.WHITE);
        BlockState dark = Materials.concrete(DyeColor.BLACK);
        boolean alongZ = face.getAxis() == Direction.Axis.Z;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int px = alongZ ? x + i : x;
                int pz = alongZ ? z : z + i;
                b.put(px, y + j, pz, i == 0 && j == 0 ? dark : pale);
            }
        }
    }

    /** A 3-deep striped awning in front of a shop face, with two timber posts. */
    private static void awning(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                               Direction face, BlockState canopy, Palette p) {
        switch (face) {
            case WEST -> {
                b.fill(x0 - 3, ground + 4, z0 + 2, x0 - 1, ground + 4, z1 - 2, canopy);
                b.fill(x0 - 3, ground + 1, z0 + 2, x0 - 3, ground + 3, z0 + 2, p.accent());
                b.fill(x0 - 3, ground + 1, z1 - 2, x0 - 3, ground + 3, z1 - 2, p.accent());
            }
            case EAST -> {
                b.fill(x1 + 1, ground + 4, z0 + 2, x1 + 3, ground + 4, z1 - 2, canopy);
                b.fill(x1 + 3, ground + 1, z0 + 2, x1 + 3, ground + 3, z0 + 2, p.accent());
                b.fill(x1 + 3, ground + 1, z1 - 2, x1 + 3, ground + 3, z1 - 2, p.accent());
            }
            case NORTH -> {
                b.fill(x0 + 2, ground + 4, z0 - 3, x1 - 2, ground + 4, z0 - 1, canopy);
                b.fill(x0 + 2, ground + 1, z0 - 3, x0 + 2, ground + 3, z0 - 3, p.accent());
                b.fill(x1 - 2, ground + 1, z0 - 3, x1 - 2, ground + 3, z0 - 3, p.accent());
            }
            default -> {
                b.fill(x0 + 2, ground + 4, z1 + 1, x1 - 2, ground + 4, z1 + 3, canopy);
                b.fill(x0 + 2, ground + 1, z1 + 3, x0 + 2, ground + 3, z1 + 3, p.accent());
                b.fill(x1 - 2, ground + 1, z1 + 3, x1 - 2, ground + 3, z1 + 3, p.accent());
            }
        }
    }

    /** A boat-shaped cut: sand floor, two courses of water and a foundation ring. */
    private static void basin(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                              Palette p) {
        b.fill(x0, ground - 5, z0, x1, ground - 5, z1, SAND);
        b.fill(x0, ground - 4, z0, x1, ground - 1, z1, WATER);
        b.fill(x0 - 1, ground - 4, z0 - 1, x1 + 1, ground + 1, z0 - 1, p.foundation());
        b.fill(x0 - 1, ground - 4, z1 + 1, x1 + 1, ground + 1, z1 + 1, p.foundation());
        b.fill(x0 - 1, ground - 4, z0 - 1, x0 - 1, ground + 1, z1 + 1, p.foundation());
        b.fill(x1 + 1, ground - 4, z0 - 1, x1 + 1, ground + 1, z1 + 1, p.foundation());
    }

    // ================================================================== streets

    private static void streets(StructureBuilder b, CitySurface s, int cx, int cz, Palette p) {
        road(b, s, cx, cz - 300, cx, cz + 336, 14, p.ground());
        // Market / Fort links.
        road(b, s, cx - 320, cz, cx - 7, cz, 10, p.ground());
        road(b, s, cx - 320, cz - 100, cx - 7, cz - 100, 10, p.ground());
        road(b, s, cx - 320, cz - 160, cx + 120, cz - 160, 10, p.ground());
        // Crawford lanes.
        road(b, s, cx - 256, cz - 260, cx - 256, cz + 80, 10, p.ground());
        road(b, s, cx - 146, cz - 260, cx - 146, cz + 80, 8, p.ground());
        road(b, s, cx - 320, cz + 64, cx - 140, cz + 64, 8, p.ground());
        // Malabar Hill lanes.
        road(b, s, cx - 320, cz + 148, cx - 7, cz + 148, 8, p.ground());
        road(b, s, cx - 320, cz + 102, cx - 100, cz + 102, 5, p.ground());
        road(b, s, cx - 320, cz + 244, cx - 100, cz + 244, 5, p.ground());
        road(b, s, cx - 320, cz + 286, cx - 100, cz + 286, 5, p.ground());
        road(b, s, cx - 278, cz + 100, cx - 278, cz + 300, 5, p.ground());
        road(b, s, cx - 198, cz + 100, cx - 198, cz + 300, 5, p.ground());
        road(b, s, cx - 138, cz + 100, cx - 138, cz + 300, 5, p.ground());
        // Fort grid.
        road(b, s, cx - 120, cz - 40, cx + 120, cz - 40, 8, p.ground());
        road(b, s, cx - 120, cz - 112, cx + 120, cz - 112, 8, p.ground());
        road(b, s, cx - 80, cz - 260, cx - 80, cz + 80, 8, p.ground());
        road(b, s, cx + 80, cz - 260, cx + 80, cz + 80, 8, p.ground());
        // Docks: the west edge road and the north quay road.
        road(b, s, cx + 146, cz - 260, cx + 146, cz + 240, 10, p.ground());
        road(b, s, cx + 140, cz - 236, cx + 340, cz - 236, 10, p.ground());
        // Mill roads.
        road(b, s, cx + 200, cz + 180, cx + 200, cz + 320, 10, p.ground());
        road(b, s, cx + 140, cz + 184, cx + 320, cz + 184, 8, p.ground());
        // Kerbs on the two long avenues.
        for (int z = -300; z <= 336; z++) {
            b.put(cx - 7, s.at(cx - 7, cz + z), cz + z, p.foundation());
            b.put(cx + 8, s.at(cx + 8, cz + z), cz + z, p.foundation());
        }
        for (int z = -260; z <= 240; z++) {
            b.put(cx + 141, s.at(cx + 141, cz + z), cz + z, p.foundation());
            b.put(cx + 152, s.at(cx + 152, cz + z), cz + z, p.foundation());
        }
    }

    /** A street that follows the shared authored surface column by column. */
    private static void road(StructureBuilder b, CitySurface s, int x0, int z0, int x1, int z1,
                             int width, BlockState state) {
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
                    b.put(x, s.at(x, z), z, state);
                }
            }
        } else {
            int cx = (x0 + x1) / 2;
            for (int z = az; z <= bz; z++) {
                for (int x = cx + lo; x <= cx + hi; x++) {
                    b.put(x, s.at(x, z), z, state);
                }
            }
        }
    }

    // ================================================================== arrival gate

    private static void arrivalGate(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        b.fill(cx - 18, ground - 3, cz + 236, cx + 18, ground - 1, cz + 340, p.foundation());
        b.fill(cx - 18, ground, cz + 236, cx + 18, ground, cz + 340, p.ground());
        gateTower(b, cx - 18, cz + 292, cx - 9, cz + 308, ground, p, Direction.EAST);
        gateTower(b, cx + 9, cz + 292, cx + 18, cz + 308, ground, p, Direction.WEST);
        b.fill(cx - 9, ground + 9, cz + 291, cx + 9, ground + 11, cz + 309, p.wall());
        b.fill(cx - 10, ground + 12, cz + 290, cx + 10, ground + 12, cz + 308, p.roofSlab());
        b.fill(cx - 8, ground + 1, cz + 291, cx + 8, ground + 8, cz + 309, AIR);
        b.crenellations(cx - 10, cz + 290, cx + 10, cz + 308, ground + 12);
        sign(b, p, cx, ground + 12, cz + 309, Direction.SOUTH, "MUMBAI");
        String[] posts = {"GATEWAY", "DOCKS", "CRAWFORD MARKET", "MALABAR HILL", "MILLS"};
        int[] px = {cx - 16, cx - 12, cx - 8, cx + 8, cx + 12};
        for (int i = 0; i < posts.length; i++) {
            signPost(b, px[i], cz + 316, ground, p, Direction.SOUTH, posts[i]);
        }
        b.fill(cx + 10, ground + 1, cz + 310, cx + 17, ground + 4, cz + 312, p.accent());
        b.fill(cx + 9, ground + 5, cz + 309, cx + 18, ground + 5, cz + 313, p.roofSlab());
        sign(b, p, cx + 12, ground + 3, cz + 313, Direction.SOUTH, "CITY NOTICES");
        sign(b, p, cx + 14, ground + 3, cz + 313, Direction.SOUTH, "CURFEW AT DUSK");
        sign(b, p, cx + 16, ground + 3, cz + 313, Direction.SOUTH, "BEWARE WATER");
        for (int dz = 244; dz <= 336; dz += 8) {
            if (dz >= 290 && dz <= 310) {
                continue; // the gate towers stand here
            }
            b.put(cx - 17, ground + 1, cz + dz, p.accent());
            b.put(cx + 17, ground + 1, cz + dz, p.accent());
        }
        b.put(cx - 17, ground + 1, cz + 314, p.light());
        b.put(cx + 17, ground + 1, cz + 314, p.light());
    }

    private static void gateTower(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  int ground, Palette p, Direction doorFace) {
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 16, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 16, p);
        b.window((x0 + x1) / 2, ground + 5, z0, 3, 2, true);
        b.window((x0 + x1) / 2, ground + 5, z1, 3, 2, true);
        b.window(x0, ground + 5, (z0 + z1) / 2, 3, 1, true);
        b.window(x1, ground + 5, (z0 + z1) / 2, 3, 1, true);
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 17);
        dome(b, (x0 + x1) / 2, (z0 + z1) / 2, ground + 18, 3, p);
        int dx = doorFace == Direction.EAST ? x1 : x0;
        int dz = (z0 + z1) / 2;
        door(b, p, dx, ground + 1, dz, doorFace);
        b.put((x0 + x1) / 2, ground + 1, (z0 + z1) / 2, p.light());
    }

    // ================================================================== harbour

    private static void harbour(StructureBuilder b, CitySurface s, int cx, int cz, int ground,
                                Palette p) {
        // Quay apron along the north shore, and the broad link to the dock road.
        b.fill(cx - 90, ground - 3, cz + 240, cx + 138, ground - 1, cz + 252, p.foundation());
        b.fill(cx - 90, ground, cz + 240, cx + 138, ground, cz + 252, p.ground());
        outline(b, cx - 90, cz + 240, cx + 138, cz + 252, ground, p.foundation());
        b.fill(cx + 138, ground - 3, cz + 240, cx + 204, ground - 1, cz + 252, p.foundation());
        b.fill(cx + 138, ground, cz + 240, cx + 204, ground, cz + 252, p.ground());
        // The two water lobes, split by the arrival causeway.
        basin(b, cx - 90, cz + 252, cx - 19, cz + 338, ground, p);
        basin(b, cx + 19, cz + 252, cx + 138, cz + 338, ground, p);
        // Ferry pier and its boats.
        ferryDock(b, cx, cz, ground, p);
        // Quay furniture, kept clear of the causeway and the pier.
        for (int x = -80; x <= 130; x += 8) {
            if (Math.abs(x) < 20 || (x >= 54 && x <= 66)) {
                continue;
            }
            b.put(cx + x, ground + 1, cz + 250, p.accent());
        }
        for (int x = -80; x <= 130; x += 16) {
            if (Math.abs(x) < 20) {
                continue;
            }
            lanternPost(b, cx + x, cz + 248, ground, p);
        }
        signPost(b, cx + 40, cz + 246, ground, p, Direction.SOUTH, "DEEP WATER");
        signPost(b, cx - 60, cz + 246, ground, p, Direction.SOUTH, "GATEWAY");
        // Customs shed on the east quay.
        customs(b, cx, cz, s.at(cx + 112, cz + 231), p);
        // Harbour chowk: two rows of shophouses flanking the axis.
        BlockState red = Materials.wool(DyeColor.RED);
        BlockState white = Materials.wool(DyeColor.WHITE);
        int eastRowY = s.at(cx + 24, cz + 166);
        int westRowY = s.at(cx - 27, cz + 166);
        shophouse(b, cx + 20, cz + 156, cx + 28, cz + 176, eastRowY, p, Direction.WEST,
                "TEA", red);
        shophouse(b, cx + 30, cz + 156, cx + 38, cz + 176, eastRowY, p, Direction.WEST,
                "TOBACCO", white);
        shophouse(b, cx + 40, cz + 156, cx + 48, cz + 176, eastRowY, p, Direction.WEST,
                "CLOTH", red);
        shophouse(b, cx - 40, cz + 156, cx - 30, cz + 176, westRowY, p, Direction.EAST,
                "FRUIT", white);
        shophouse(b, cx - 28, cz + 156, cx - 14, cz + 176, westRowY, p, Direction.EAST,
                "HARBOUR STORES", red);
        // The maidan between the Taj and the Gateway plaza.
        maidan(b, cx, cz, s.at(cx - 72, cz + 173), p);
        // The Taj hotel.
        hotel(b, cx, cz, s.at(cx - 80, cz + 120), p);
        // Quest board either side of the axis.
        questBoard(b, cx, cz, s.at(cx, cz + 180), p);
        // Crowd dressing on the quay, clear of the causeway and the pier.
        for (int i = 0; i < 8; i++) {
            int x = -70 + i * 24;
            if (Math.abs(x) < 20) {
                continue;
            }
            crate(b, cx + x, cz + 243, ground, p, 1 + (i & 1));
        }
        for (int i = 0; i < 6; i++) {
            int x = -66 + i * 26;
            if (Math.abs(x) < 20 || (x >= 54 && x <= 66)) {
                continue;
            }
            barrel(b, cx + x, cz + 251, ground);
        }
    }

    private static void ferryDock(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 54;
        int x1 = cx + 66;
        int z0 = cz + 244;
        int z1 = cz + 282;
        // Cut the quay curb where the pier crosses it.
        b.fill(x0, ground + 1, cz + 251, x1, ground + 1, cz + 252, AIR);
        for (int x = x0; x <= x1; x += 6) {
            for (int z = cz + 254; z <= z1; z += 8) {
                b.fill(x, ground - 4, z, x, ground - 1, z, p.foundation());
            }
        }
        b.fill(x0, ground, z0, x1, ground, z1, DECK);
        for (int z = z0; z <= z1; z++) {
            b.put(x0, ground + 1, z, p.rail());
            b.put(x1, ground + 1, z, p.rail());
        }
        b.fill(x0, ground + 1, z1 - 4, x0, ground + 1, z1 - 2, AIR);
        b.fill(x1, ground + 1, z1 - 4, x1, ground + 1, z1 - 2, AIR);
        // The pier-root kiosk.
        b.room(cx + 56, ground, cz + 246, cx + 64, ground + 4, cz + 250);
        b.fill(cx + 56, ground, cz + 246, cx + 64, ground, cz + 250, p.foundation());
        door(b, p, cx + 60, ground + 1, cz + 250, Direction.SOUTH);
        sign(b, p, cx + 60, ground + 4, cz + 251, Direction.SOUTH, "FERRIES");
        signPost(b, cx + 60, cz + 278, ground, p, Direction.SOUTH, "FERRY");
        b.put(x0 + 1, ground + 1, z1 - 1, p.light());
        b.put(x1 - 1, ground + 1, z1 - 1, p.light());
        boat(b, cx + 80, cz + 300, ground, 14, 6, true, p);
        boat(b, cx + 110, cz + 286, ground, 10, 5, false, p);
        boat(b, cx - 50, cz + 300, ground, 8, 4, false, p);
        boat(b, cx - 70, cz + 320, ground, 12, 6, true, p);
    }

    private static void customs(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 96;
        int x1 = cx + 128;
        int z0 = cz + 222;
        int z1 = cz + 240;
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 6, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 6, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 6, 4, 0);
        for (int x = x0 + 3; x <= x1 - 3; x += 5) {
            b.fill(x, ground + 1, z1, x, ground + 4, z1, AIR);
            b.put(x, ground + 5, z1, p.accent());
        }
        sign(b, p, cx + 112, ground + 5, cz + 241, Direction.SOUTH, "CUSTOMS");
        crate(b, x0 + 2, z0 + 2, ground, p, 2);
        crate(b, x1 - 2, z0 + 2, ground, p, 1);
        b.put(x0 + 1, ground + 2, z0 + 1, p.light());
    }

    private static void maidan(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 100;
        int x1 = cx - 44;
        int z0 = cz + 156;
        int z1 = cz + 190;
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.fill(x0, ground, z0, x1, ground, z1, GRASS);
        outline(b, x0, z0, x1, z1, ground, p.foundation());
        bandstand(b, cx - 72, cz + 173, ground, p);
        bench(b, cx - 92, cz + 164, ground, p);
        bench(b, cx - 92, cz + 182, ground, p);
        bench(b, cx - 52, cz + 164, ground, p);
        bench(b, cx - 52, cz + 182, ground, p);
        palm(b, cx - 96, cz + 160, ground);
        palm(b, cx - 96, cz + 186, ground);
        palm(b, cx - 48, cz + 160, ground);
        palm(b, cx - 48, cz + 186, ground);
        signPost(b, cx - 48, cz + 154, ground, p, Direction.SOUTH, "MAIDAN");
    }

    private static void bandstand(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 6, ground, z - 6, x + 6, ground, z + 6, p.foundation());
        b.fill(x - 5, ground + 1, z - 5, x + 5, ground + 1, z + 5, p.roofSlab());
        for (int dx = -4; dx <= 4; dx += 4) {
            for (int dz = -4; dz <= 4; dz += 4) {
                b.fill(x + dx, ground + 1, z + dz, x + dx, ground + 5, z + dz, p.accent());
            }
        }
        b.fill(x - 6, ground + 6, z - 6, x + 6, ground + 6, z + 6, p.roofSlab());
        dome(b, x, z, ground + 7, 3, p);
        b.put(x, ground + 1, z, p.light());
    }

    private static void bench(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground + 1, z, p.roofSlab());
        b.put(x + 1, ground + 1, z, p.roofSlab());
        b.put(x, ground + 2, z - 1, p.roofSlab());
        b.put(x + 1, ground + 2, z - 1, p.roofSlab());
    }

    private static void hotel(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 102;
        int x1 = cx - 58;
        int z0 = cz + 104;
        int z1 = cz + 136;
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 18, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 18, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 18, 6, 0);
        // Central drum and dome, with four corner turrets.
        b.fill(cx - 6, ground + 18, cz + 114, cx + 6, ground + 21, cz + 126, p.wall());
        dome(b, cx, cz + 120, ground + 22, 6, p);
        int[] txs = {x0, x1};
        int[] tzs = {z0, z1};
        for (int tx : txs) {
            for (int tz : tzs) {
                b.fill(tx - 1, ground + 16, tz - 1, tx + 1, ground + 24, tz + 1, p.wall());
                b.fill(tx - 1, ground + 16, tz - 1, tx + 1, ground + 16, tz + 1, p.accent());
                dome(b, tx, tz, ground + 25, 2, p);
            }
        }
        // Arcaded veranda and porte-cochere on the south face.
        for (int x = x0 + 4; x <= x1 - 4; x += 8) {
            b.fill(x, ground + 1, z1 + 1, x, ground + 6, z1 + 1, p.accent());
            b.put(x, ground + 7, z1 + 1, p.accent());
        }
        b.fill(x0 + 2, ground + 7, z1, x1 - 2, ground + 7, z1 + 2, p.roofSlab());
        b.fill(x0 + 2, ground + 8, z1, x1 - 2, ground + 8, z1 + 2, p.wall());
        b.fill(cx - 8, ground + 8, z1 + 3, cx + 8, ground + 8, z1 + 6, p.roofSlab());
        for (int x = cx - 8; x <= cx + 8; x += 8) {
            b.fill(x, ground + 1, z1 + 6, x, ground + 7, z1 + 6, p.accent());
        }
        door(b, p, cx, ground + 1, z1, Direction.SOUTH);
        sign(b, p, cx, ground + 6, z1 + 2, Direction.SOUTH, "TAJ HOTEL");
        for (int x = x0 + 5; x <= x1 - 5; x += 7) {
            b.window(x, ground + 4, z1, 3, 2, true);
            b.window(x, ground + 10, z1, 3, 2, true);
            b.window(x, ground + 14, z1, 2, 2, true);
            b.window(x, ground + 4, z0, 3, 2, true);
            b.window(x, ground + 10, z0, 3, 2, true);
        }
        for (int z = z0 + 5; z <= z1 - 5; z += 7) {
            b.window(x0, ground + 4, z, 3, 1, true);
            b.window(x0, ground + 10, z, 3, 1, true);
            b.window(x1, ground + 4, z, 3, 1, true);
            b.window(x1, ground + 10, z, 3, 1, true);
        }
        // Lobby, a mezzanine and the guest beds.
        b.fill(x0 + 2, ground + 1, z0 + 2, x0 + 8, ground + 1, z0 + 4, p.roofSlab());
        b.put(x0 + 3, ground + 2, z0 + 2, p.light());
        b.fill(x0 + 2, ground + 5, z0 + 2, x1 - 2, ground + 5, z1 - 2, p.roofSlab());
        for (int y = ground + 1; y <= ground + 5; y++) {
            b.put(x0 + 1, y, z0 + 2,
                    Blocks.LADDER.defaultBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        }
        bed(b, x0 + 3, ground + 1, z1 - 4, p);
        bed(b, x0 + 8, ground + 1, z1 - 4, p);
        bed(b, x1 - 8, ground + 1, z1 - 4, p);
        b.chest(x1 - 2, ground + 1, z1 - 2, SiteLoot.COMMON);
        b.put(x1 - 2, ground + 1, z0 + 2, BOOKS);
        b.put(x1 - 2, ground + 2, z0 + 2, BOOKS);
        b.put(x0 + 1, ground + 1, z1 - 2, BARREL);
    }

    private static void questBoard(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int z = cz + 180;
        int[][] panels = {{cx - 22, cx - 9}, {cx + 9, cx + 22}};
        for (int[] panel : panels) {
            int x0 = panel[0];
            int x1 = panel[1];
            b.fill(x0, ground + 1, z - 2, x1, ground + 1, z + 1, p.foundation());
            b.fill(x0, ground + 2, z + 1, x1, ground + 4, z + 1, p.accent());
            b.fill(x0, ground + 2, z - 2, x0, ground + 4, z - 2, p.accent());
            b.fill(x1, ground + 2, z - 2, x1, ground + 4, z - 2, p.accent());
            b.fill(x0, ground + 5, z - 2, x1, ground + 5, z + 2, p.roofSlab());
        }
        sign(b, p, cx - 20, ground + 4, z + 2, Direction.SOUTH, "HARBOUR NOTICES");
        sign(b, p, cx - 16, ground + 4, z + 2, Direction.SOUTH, "DOCK STRIKE");
        sign(b, p, cx - 12, ground + 4, z + 2, Direction.SOUTH, "REWARD");
        sign(b, p, cx + 12, ground + 4, z + 2, Direction.SOUTH, "MISSING ACTOR");
        sign(b, p, cx + 16, ground + 4, z + 2, Direction.SOUTH, "REWARD");
        sign(b, p, cx + 20, ground + 4, z + 2, Direction.SOUTH, "ASK AT THEATRE");
        b.put(cx - 9, ground + 3, z - 2, p.light());
        b.put(cx + 9, ground + 3, z - 2, p.light());
    }

    // ================================================================== Gateway of India

    private static void gatewayOfIndia(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int zc = cz + 220;
        b.fill(cx - 34, ground - 2, cz + 192, cx + 34, ground - 1, cz + 244, p.foundation());
        b.fill(cx - 34, ground, cz + 192, cx + 34, ground, cz + 244, p.ground());
        outline(b, cx - 34, cz + 192, cx + 34, cz + 244, ground, p.foundation());
        b.fill(cx - 17, ground + 1, zc - 8, cx + 17, ground + 1, zc + 8, p.foundation());
        for (int x = cx - 10; x <= cx + 10; x++) {
            stairs(b, p.roofStairs(), x, ground + 1, zc + 9, Direction.SOUTH);
        }
        // The two massy piers.
        for (int s = 0; s < 2; s++) {
            int x0 = s == 0 ? cx - 16 : cx + 7;
            int x1 = x0 + 9;
            b.room(x0, ground + 1, zc - 7, x1, ground + 30, zc + 7);
            b.fill(x0, ground + 1, zc - 7, x1, ground + 1, zc + 7, p.foundation());
            quoins(b, x0, zc - 7, x1, zc + 7, ground + 2, ground + 30, p);
            for (int y = ground + 8; y <= ground + 28; y += 8) {
                b.fill(x0, y, zc - 7, x1, y, zc - 7, p.accent());
                b.fill(x0, y, zc + 7, x1, y, zc + 7, p.accent());
                b.fill(x0, y, zc - 7, x0, y, zc + 7, p.accent());
                b.fill(x1, y, zc - 7, x1, y, zc + 7, p.accent());
            }
            b.window(x0 + 3, ground + 5, zc + 7, 10, 4, true);
            b.window(x0 + 3, ground + 18, zc + 7, 8, 4, true);
            b.window(x0 + 3, ground + 5, zc - 7, 10, 4, true);
            b.window(x0 + 3, ground + 18, zc - 7, 8, 4, true);
            b.window(x0, ground + 6, zc, 10, 1, true);
            b.window(x1, ground + 6, zc, 10, 1, true);
        }
        // The central bay: solid spandrels with the great semicircular arch carved out.
        b.fill(cx - 7, ground + 1, zc - 7, cx + 7, ground + 30, zc + 7, p.wall());
        b.fill(cx - 7, ground + 1, zc - 7, cx + 7, ground + 1, zc + 7, p.foundation());
        for (int x = cx - 6; x <= cx + 6; x++) {
            int dx = x - cx;
            int rise = (int) Math.round(Math.sqrt(36 - dx * dx));
            int yA = ground + 16 + rise;
            b.fill(x, ground + 2, zc - 7, x, yA - 1, zc + 7, AIR);
            b.put(x, yA, zc - 7, p.accent());
            b.put(x, yA, zc + 7, p.accent());
            b.put(x, yA - 1, zc - 7, p.accent());
            b.put(x, yA - 1, zc + 7, p.accent());
        }
        // Crown: parapet, drum, dome, and four corner turrets.
        b.crenellations(cx - 17, zc - 8, cx + 17, zc + 8, ground + 31);
        b.fill(cx - 4, ground + 31, zc - 4, cx + 4, ground + 34, zc + 4, p.wall());
        dome(b, cx, zc, ground + 35, 5, p);
        b.put(cx, ground + 41, zc, p.accent());
        int[] txs = {cx - 16, cx + 16};
        int[] tzs = {zc - 7, zc + 7};
        for (int tx : txs) {
            for (int tz : tzs) {
                b.fill(tx - 1, ground + 24, tz - 1, tx + 1, ground + 36, tz + 1, p.wall());
                b.fill(tx - 1, ground + 24, tz - 1, tx + 1, ground + 24, tz + 1, p.accent());
                dome(b, tx, tz, ground + 37, 2, p);
            }
        }
        signPost(b, cx - 14, zc + 11, ground, p, Direction.SOUTH, "GATEWAY");
        signPost(b, cx + 14, zc + 11, ground, p, Direction.SOUTH, "GATEWAY 1911");
        int[] posts = {cx - 16, cx - 8, cx + 8, cx + 16};
        for (int px : posts) {
            b.put(px, ground + 1, zc + 12, p.accent());
            b.put(px, ground + 2, zc + 12, p.rail());
        }
        b.put(cx - 20, ground + 1, zc - 2, p.light());
        b.put(cx + 20, ground + 1, zc - 2, p.light());
    }

    // ================================================================== docks

    private static void docks(StructureBuilder b, CitySurface s, int cx, int cz, Palette p) {
        int gy = s.at(cx + 224, cz);
        int dy = s.at(cx + 300, cz - 140);
        // The dock basin in the east, its quay ring, and the cranes.
        basin(b, cx + 276, cz - 196, cx + 333, cz - 92, dy, p);
        b.fill(cx + 260, dy - 2, cz - 240, cx + 274, dy - 1, cz - 78, p.foundation());
        b.fill(cx + 260, dy, cz - 240, cx + 274, dy, cz - 78, p.ground());
        b.fill(cx + 335, dy - 2, cz - 240, cx + 340, dy - 1, cz - 78, p.foundation());
        b.fill(cx + 335, dy, cz - 240, cx + 340, dy, cz - 78, p.ground());
        b.fill(cx + 275, dy - 2, cz - 240, cx + 334, dy - 1, cz - 198, p.foundation());
        b.fill(cx + 275, dy, cz - 240, cx + 334, dy, cz - 198, p.ground());
        b.fill(cx + 275, dy - 2, cz - 90, cx + 334, dy - 1, cz - 78, p.foundation());
        b.fill(cx + 275, dy, cz - 90, cx + 334, dy, cz - 78, p.ground());
        for (int z = -180; z <= -100; z += 30) {
            crane(b, cx + 268, cz + z, dy, p);
        }
        signPost(b, cx + 264, cz - 88, dy, p, Direction.WEST, "NO ENTRY");
        signPost(b, cx + 264, cz - 140, dy, p, Direction.WEST, "CRANES WORKING");
        signPost(b, cx + 300, cz - 90, dy, p, Direction.SOUTH, "DEEP WATER");
        // Warehouses along the dock apron.
        warehouse(b, cx + 196, cz - 212, cx + 252, cz - 176, s.at(cx + 224, cz - 194), p, "WAREHOUSE 1", 0);
        warehouse(b, cx + 196, cz - 160, cx + 252, cz - 124, s.at(cx + 224, cz - 142), p, "WAREHOUSE 2", 2);
        warehouse(b, cx + 196, cz - 112, cx + 252, cz - 80, s.at(cx + 224, cz - 96), p, "WAREHOUSE 3", 0);
        warehouse(b, cx + 196, cz - 72, cx + 252, cz - 40, s.at(cx + 224, cz - 56), p, "WAREHOUSE 4", 1);
        warehouse(b, cx + 196, cz + 40, cx + 252, cz + 76, s.at(cx + 224, cz + 58), p, "WAREHOUSE 5", 2);
        warehouse(b, cx + 196, cz + 100, cx + 252, cz + 136, s.at(cx + 224, cz + 118), p, "WAREHOUSE 6", 0);
        ruin(b, cx + 196, cz + 152, cx + 236, cz + 178, s.at(cx + 216, cz + 165), p);
        // Rail sidings and coal heaps between the warehouses and the office.
        railLine(b, cx + 150, cx + 334, cz - 31, gy, p);
        railLine(b, cx + 150, cx + 334, cz - 27, gy, p);
        wagon(b, cx + 210, cz - 31, gy, p, true);
        wagon(b, cx + 250, cz - 27, gy, p, false);
        wagon(b, cx + 292, cz - 31, gy, p, true);
        b.fill(cx + 334, gy + 1, cz - 33, cx + 334, gy + 3, cz - 25, p.accent());
        for (int i = 0; i < 4; i++) {
            b.fill(cx + 300 + i * 8, gy + 1, cz - 50 + i, cx + 305 + i * 8,
                    gy + 2, cz - 44 + i, COAL);
        }
        // The rail station at the district edge, on the level sidings.
        station(b, cx + 320, cz - 12, gy, p);
        // Dock office and the strike notice.
        dockOffice(b, cx, cz, s.at(cx + 220, cz), p);
        signPost(b, cx + 180, cz + 236, gy, p, Direction.SOUTH, "DOCKS");
        for (int i = 0; i < 8; i++) {
            crate(b, cx + 180 + (i & 1) * 3, cz - 224 + i * 22, gy, p, 1 + (i % 3));
        }
        for (int i = 0; i < 8; i++) {
            barrel(b, cx + 258, cz - 230 + i * 20, gy);
        }
        for (int z = -220; z <= 120; z += 40) {
            lanternPost(b, cx + 158, cz + z, gy, p);
        }
    }

    /** The docks rail station: a platform on the level sidings and a small ticket hall. */
    private static void station(StructureBuilder b, int x, int z, int ground, Palette p) {
        int x0 = x - 14;
        int x1 = x + 14;
        int z0 = z - 6;
        int z1 = z + 6;
        // The platform sits between the sidings and the station hall.
        b.fill(x0, ground, z0 - 6, x1, ground, z0 - 1, p.foundation());
        b.fill(x0, ground + 1, z0 - 6, x1, ground + 1, z0 - 1, p.ground());
        for (int px = x0 + 2; px <= x1 - 2; px += 6) {
            b.fill(px, ground + 1, z0 - 5, px, ground + 4, z0 - 5, p.accent());
        }
        b.fill(x0, ground + 5, z0 - 6, x1, ground + 5, z0 - 1, p.roofSlab());
        // The ticket hall.
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.room(x0, ground + 1, z0, x1, ground + 8, z1);
        b.fill(x0, ground + 1, z0, x1, ground + 1, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 2, ground + 8, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 8, 4, 0);
        door(b, p, x, ground + 2, z1, Direction.SOUTH);
        door(b, p, x, ground + 2, z0, Direction.NORTH);
        b.fill(x - 1, ground + 1, z1 + 1, x + 1, ground + 1, z1 + 1, p.foundation());
        sign(b, p, x, ground + 5, z1 + 1, Direction.SOUTH, "DOCKS STATION");
        for (int px = x0 + 3; px <= x1 - 3; px += 6) {
            b.window(px, ground + 5, z1, 2, 2, true);
        }
        b.fill(x0 + 2, ground + 2, z0 + 1, x0 + 8, ground + 2, z0 + 2, p.roofSlab());
        b.put(x0 + 3, ground + 3, z0 + 1, p.light());
        b.put(x1 - 3, ground + 2, z0 + 1, BOOKS);
        b.put(x1 - 3, ground + 3, z0 + 1, BOOKS);
        bench(b, x0 + 3, z0 - 3, ground + 1, p);
        bench(b, x1 - 4, z0 - 3, ground + 1, p);
        b.put(x0 + 1, ground + 2, z1 - 2, p.light());
        b.put(x1 - 1, ground + 2, z1 - 2, p.light());
        b.put(x0 + 2, ground + 4, z0 - 5, p.light());
        b.put(x1 - 2, ground + 4, z0 - 5, p.light());
    }

    private static void dockOffice(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 204;
        int x1 = cx + 236;
        int z0 = cz - 14;
        int z1 = cz + 14;
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 12, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 12, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 12, 5, 0);
        b.fill(x0 + 1, ground + 6, z0 + 1, x1 - 1, ground + 6, z1 - 1, p.roofSlab());
        b.fill(x0 + 1, ground + 6, z0 + 1, x0 + 3, ground + 6, z0 + 3, AIR);
        for (int y = ground + 1; y <= ground + 5; y++) {
            b.put(x0 + 1, y, z0 + 2,
                    Blocks.LADDER.defaultBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        }
        door(b, p, cx + 220, ground + 1, z1, Direction.SOUTH);
        b.window(x0, ground + 8, cz - 6, 2, 1, true);
        b.window(x1, ground + 8, cz + 6, 2, 1, true);
        b.window(x0 + 5, ground + 9, z0, 2, 2, true);
        b.window(x1 - 5, ground + 9, z0, 2, 2, true);
        sign(b, p, cx + 220, ground + 5, z1 + 1, Direction.SOUTH, "DOCK OFFICE");
        sign(b, p, cx + 220, ground + 3, z1 + 1, Direction.SOUTH, "NO WORK TODAY");
        b.fill(x0 + 2, ground + 1, z0 + 2, x0 + 8, ground + 1, z0 + 3, p.roofSlab());
        b.fill(x0 + 10, ground + 1, z0 + 2, x1 - 2, ground + 1, z0 + 3, p.roofSlab());
        b.put(x0 + 3, ground + 2, z0 + 2, p.light());
        b.put(x1 - 3, ground + 1, z1 - 2, p.light());
        b.chest(x0 + 2, ground + 1, z1 - 2, SiteLoot.COMMON);
        b.put(x0 + 4, ground + 1, z1 - 2, BOOKS);
        b.put(x0 + 5, ground + 1, z1 - 2, BOOKS);
    }

    private static void warehouse(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  int ground, Palette p, String label, int variant) {
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 12, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 12, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 12, 7,
                x1 - x0 >= z1 - z0 ? 0 : 1);
        int fx;
        int fz;
        Direction face;
        switch (variant % 4) {
            case 0 -> {
                fx = x1;
                fz = (z0 + z1) / 2;
                face = Direction.EAST;
            }
            case 1 -> {
                fx = (x0 + x1) / 2;
                fz = z1;
                face = Direction.SOUTH;
            }
            case 2 -> {
                fx = x0;
                fz = (z0 + z1) / 2;
                face = Direction.WEST;
            }
            default -> {
                fx = (x0 + x1) / 2;
                fz = z0;
                face = Direction.NORTH;
            }
        }
        boolean alongZ = face.getAxis() == Direction.Axis.Z;
        if (alongZ) {
            for (int x = fx - 2; x <= fx + 2; x++) {
                b.fill(x, ground + 1, fz, x, ground + 6, fz, AIR);
            }
            b.fill(fx - 2, ground + 7, fz, fx + 2, ground + 7, fz, p.accent());
        } else {
            for (int z = fz - 2; z <= fz + 2; z++) {
                b.fill(fx, ground + 1, z, fx, ground + 6, z, AIR);
            }
            b.fill(fx, ground + 7, fz - 2, fx, ground + 7, fz + 2, p.accent());
        }
        sign(b, p, fx + face.getStepX(), ground + 8, fz + face.getStepZ(), face, label);
        for (int x = x0 + 4; x <= x1 - 4; x += 6) {
            b.window(x, ground + 9, z0, 2, 2, true);
            b.window(x, ground + 9, z1, 2, 2, true);
        }
        // A hoist beam and a loading platform.
        b.fill(fx, ground + 11, fz, fx + face.getStepX() * 3, ground + 11, fz
                + face.getStepZ() * 3, p.accent());
        b.put(fx + face.getStepX() * 3, ground + 9, fz + face.getStepZ() * 3, p.rail());
        crate(b, x0 + 2, z0 + 2, ground, p, 2);
        crate(b, x0 + 3, z0 + 2, ground, p, 1);
        crate(b, x1 - 2, z1 - 2, ground, p, 2);
        barrel(b, x1 - 3, z1 - 2, ground);
        b.put(x0 + 1, ground + 1, z0 + 1, p.light());
        b.chest(x1 - 2, ground + 1, z0 + 2, SiteLoot.COMMON);
    }

    /** A deliberately collapsed warehouse: a partial shell, rubble and a fallen roof beam. */
    private static void ruin(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                             Palette p) {
        for (int y = ground + 1; y <= ground + 8; y++) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                    if (!edge) {
                        continue;
                    }
                    if ((x + z + y) % 4 == 0) {
                        continue;
                    }
                    b.put(x, y, z, y <= ground + 2 ? p.foundation() : p.weathered());
                }
            }
        }
        b.fill(x0, ground, z0, x1, ground, z1, p.rubble());
        for (int i = 0; i < 5; i++) {
            b.put(x0 + 4 + i * 4, ground + 3, (z0 + z1) / 2, p.roofSlab());
        }
        b.put(x0 + 2, ground + 1, z0 + 2, p.light());
        signPost(b, x0 - 3, z0, ground, p, Direction.WEST, "KEEP OUT");
    }

    // ================================================================== Crawford Market

    private static void crawford(StructureBuilder b, CitySurface s, int cx, int cz, Palette p) {
        BlockState red = Materials.wool(DyeColor.RED);
        BlockState white = Materials.wool(DyeColor.WHITE);
        // Halls around the north-south market lane and the two cross lanes.
        marketHall(b, cx - 312, cz - 92, cx - 268, cz - 12, s.at(cx - 290, cz - 52), p,
                "CRAWFORD MARKET", Direction.EAST, red, white);
        marketHall(b, cx - 244, cz - 56, cx - 200, cz - 12, s.at(cx - 222, cz - 34), p,
                "PRODUCE", Direction.WEST, white, red);
        marketHall(b, cx - 312, cz - 152, cx - 268, cz - 112, s.at(cx - 290, cz - 132), p,
                "SPICES", Direction.EAST, white, red);
        marketHall(b, cx - 244, cz - 152, cx - 200, cz - 112, s.at(cx - 222, cz - 132), p,
                "GRAIN", Direction.WEST, red, white);
        marketHall(b, cx - 312, cz - 244, cx - 268, cz - 204, s.at(cx - 290, cz - 224), p,
                "FISH", Direction.EAST, red, white);
        // The clock tower on the main hall's north-east corner.
        clockTower(b, cx - 270, cz - 16, s.at(cx - 270, cz - 16), p);
        // The shop that anchors the merchant.
        shop(b, cx - 246, cz - 88, cx - 228, cz - 64, s.at(cx - 237, cz - 76), p);
        // Tea row north of the market street.
        shophouse(b, cx - 300, cz + 16, cx - 286, cz + 30, s.at(cx - 293, cz + 23), p,
                Direction.SOUTH, "TEA", red);
        shophouse(b, cx - 284, cz + 16, cx - 270, cz + 30, s.at(cx - 277, cz + 23), p,
                Direction.SOUTH, "SPICE", white);
        shophouse(b, cx - 244, cz + 16, cx - 230, cz + 30, s.at(cx - 237, cz + 23), p,
                Direction.SOUTH, "RICE", red);
        shophouse(b, cx - 228, cz + 16, cx - 214, cz + 30, s.at(cx - 221, cz + 23), p,
                Direction.SOUTH, "DAL", white);
        shophouse(b, cx - 212, cz + 16, cx - 196, cz + 30, s.at(cx - 204, cz + 23), p,
                Direction.SOUTH, "OIL", red);
        // The produce yard south of the halls, east of the market lane.
        int py = s.at(cx - 223, cz - 224);
        b.fill(cx - 246, py - 1, cz - 244, cx - 200, py - 1, cz - 204, p.foundation());
        b.fill(cx - 246, py, cz - 244, cx - 200, py, cz - 204, p.ground());
        for (int i = 0; i < 4; i++) {
            marketStall(b, cx - 242 + i * 13, cz - 240, py, p,
                    (i & 1) == 0 ? red : white);
        }
        for (int i = 0; i < 4; i++) {
            marketStall(b, cx - 242 + i * 13, cz - 210, py, p,
                    (i & 1) == 0 ? white : red);
        }
        for (int i = 0; i < 5; i++) {
            crate(b, cx - 240 + i * 9, cz - 202, py, p, 1 + (i & 1));
        }
        // Awnings and stalls down the market lane.
        int[] stallZ = {cz - 220, cz - 180, cz - 140, cz - 60, cz - 20, cz + 40};
        for (int i = 0; i < stallZ.length; i++) {
            marketStall(b, cx - 250, stallZ[i], s.at(cx - 250, stallZ[i]), p,
                    (i & 1) == 0 ? red : white);
            marketStall(b, cx - 264, stallZ[i] + 6, s.at(cx - 264, stallZ[i] + 6), p,
                    (i & 1) == 0 ? white : red);
        }
        // The market fountain and its plaza.
        fountain(b, cx - 220, cz + 46, s.at(cx - 220, cz + 46), p);
        for (int i = 0; i < 4; i++) {
            int fx = cx - 232 + i * 8;
            int fy = s.at(fx, cz + 56);
            b.put(fx, fy + 1, cz + 56, p.accent());
            b.put(fx, fy + 2, cz + 56, p.light());
        }
        signPost(b, cx - 246, cz + 74, s.at(cx - 246, cz + 74), p, Direction.SOUTH,
                "CRAWFORD MARKET");
        signPost(b, cx - 248, cz - 92, s.at(cx - 248, cz - 92), p, Direction.WEST,
                "FRESH PRODUCE");
        signPost(b, cx - 154, cz + 10, s.at(cx - 154, cz + 10), p, Direction.EAST,
                "CRAWFORD MARKET");
        lanternPost(b, cx - 262, cz - 240, s.at(cx - 262, cz - 240), p);
        lanternPost(b, cx - 262, cz + 20, s.at(cx - 262, cz + 20), p);
    }

    private static void marketHall(StructureBuilder b, int x0, int z0, int x1, int z1,
                                   int ground, Palette p, String label, Direction face,
                                   BlockState a, BlockState c) {
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 10, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 10, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 10, 6,
                x1 - x0 >= z1 - z0 ? 0 : 1);
        int dx;
        int dz;
        switch (face) {
            case WEST -> {
                dx = x0;
                dz = (z0 + z1) / 2;
            }
            case EAST -> {
                dx = x1;
                dz = (z0 + z1) / 2;
            }
            case NORTH -> {
                dx = (x0 + x1) / 2;
                dz = z0;
            }
            default -> {
                dx = (x0 + x1) / 2;
                dz = z1;
            }
        }
        // A 5-wide arched entrance.
        boolean alongZ = face.getAxis() == Direction.Axis.Z;
        if (alongZ) {
            for (int x = dx - 2; x <= dx + 2; x++) {
                b.fill(x, ground + 1, dz, x, ground + 5, dz, AIR);
            }
            b.fill(dx - 3, ground + 6, dz, dx + 3, ground + 6, dz, p.accent());
        } else {
            for (int z = dz - 2; z <= dz + 2; z++) {
                b.fill(dx, ground + 1, z, dx, ground + 5, z, AIR);
            }
            b.fill(dx, ground + 6, dz - 3, dx, ground + 6, dz + 3, p.accent());
        }
        sign(b, p, dx + face.getStepX(), ground + 7, dz + face.getStepZ(), face, label);
        for (int i = 0; i < 6; i++) {
            int sx = x0 + 4 + i * ((x1 - x0 - 8) / 5);
            marketStall(b, sx, z0 + 4, ground, p, (i & 1) == 0 ? a : c);
            marketStall(b, sx, z1 - 7, ground, p, (i & 1) == 0 ? c : a);
        }
        for (int z = z0 + 5; z <= z1 - 5; z += 6) {
            b.window(x0, ground + 7, z, 2, 1, true);
            b.window(x1, ground + 7, z, 2, 1, true);
        }
        b.put(x0 + 1, ground + 1, z0 + 1, p.light());
        b.put(x1 - 1, ground + 1, z1 - 1, p.light());
        b.chest(x0 + 2, ground + 1, z1 - 2, SiteLoot.COMMON);
    }

    private static void clockTower(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 6, ground - 2, z - 6, x + 6, ground - 1, z + 6, p.foundation());
        b.room(x - 6, ground, z - 6, x + 6, ground + 26, z + 6);
        b.fill(x - 6, ground, z - 6, x + 6, ground, z + 6, p.foundation());
        quoins(b, x - 6, z - 6, x + 6, z + 6, ground + 1, ground + 26, p);
        for (int y = ground + 6; y <= ground + 24; y += 6) {
            b.window(x - 6, y, z, 2, 1, true);
            b.window(x + 6, y, z, 2, 1, true);
            b.window(x, y, z - 6, 2, 1, true);
            b.window(x, y, z + 6, 2, 1, true);
        }
        clockFace(b, x, ground + 21, z - 6, Direction.NORTH);
        clockFace(b, x, ground + 21, z + 6, Direction.SOUTH);
        clockFace(b, x - 6, ground + 21, z, Direction.WEST);
        clockFace(b, x + 6, ground + 21, z, Direction.EAST);
        b.fill(x - 7, ground + 27, z - 7, x + 7, ground + 27, z + 7, p.roofSlab());
        dome(b, x, z, ground + 28, 4, p);
        sign(b, p, x + 7, ground + 5, z, Direction.EAST, "CRAWFORD MARKET");
        b.put(x, ground + 2, z, p.light());
    }

    private static void shop(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                             Palette p) {
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 9, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 9, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 9, 4, 1);
        door(b, p, x0, ground + 1, (z0 + z1) / 2, Direction.WEST);
        awning(b, x0, z0, x1, z1, ground, Direction.WEST,
                Materials.wool(DyeColor.RED), p);
        sign(b, p, x0 - 4, ground + 5, (z0 + z1) / 2, Direction.WEST, "CRAWFORD SHOP");
        sign(b, p, x0 - 4, ground + 3, (z0 + z1) / 2, Direction.WEST, "SPICES & TEA");
        b.fill(x0 + 1, ground + 1, z0 + 2, x0 + 5, ground + 1, z0 + 3, p.roofSlab());
        b.fill(x0 + 1, ground + 1, z1 - 3, x1 - 1, ground + 1, z1 - 2, p.roofSlab());
        b.put(x0 + 2, ground + 1, z0 + 2, HAY);
        b.put(x0 + 3, ground + 1, z0 + 2, HAY);
        b.put(x1 - 2, ground + 1, z0 + 2, BOOKS);
        b.put(x1 - 2, ground + 2, z0 + 2, BOOKS);
        b.put(x0 + 2, ground + 1, z1 - 3, p.light());
        b.chest(x1 - 2, ground + 1, z1 - 2, SiteLoot.COMMON);
        b.put(x0 + 3, ground + 1, z1 - 3, BARREL);
        b.put(x0 + 4, ground + 1, z1 - 3, BARREL);
    }

    private static void shophouse(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  int ground, Palette p, Direction face, String label,
                                  BlockState canopy) {
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 9, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 9, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 9, 4,
                x1 - x0 >= z1 - z0 ? 0 : 1);
        int dx;
        int dz;
        switch (face) {
            case WEST -> {
                dx = x0;
                dz = (z0 + z1) / 2;
            }
            case EAST -> {
                dx = x1;
                dz = (z0 + z1) / 2;
            }
            case NORTH -> {
                dx = (x0 + x1) / 2;
                dz = z0;
            }
            default -> {
                dx = (x0 + x1) / 2;
                dz = z1;
            }
        }
        door(b, p, dx, ground + 1, dz, face);
        awning(b, x0, z0, x1, z1, ground, face, canopy, p);
        sign(b, p, dx + face.getStepX() * 4, ground + 6, dz + face.getStepZ() * 4,
                face, label);
        if (face.getAxis() == Direction.Axis.X) {
            b.window(x0, ground + 6, z0 + 2, 2, 1, true);
            b.window(x0, ground + 6, z1 - 2, 2, 1, true);
            b.window(x1, ground + 6, z0 + 2, 2, 1, true);
            b.window(x1, ground + 6, z1 - 2, 2, 1, true);
        } else {
            b.window(x0 + 2, ground + 6, z0, 2, 1, true);
            b.window(x1 - 2, ground + 6, z0, 2, 1, true);
            b.window(x0 + 2, ground + 6, z1, 2, 1, true);
            b.window(x1 - 2, ground + 6, z1, 2, 1, true);
        }
        b.fill(x0 + 1, ground + 1, z0 + 1, x0 + 3, ground + 1, z0 + 2, p.roofSlab());
        b.put(x0 + 2, ground + 2, z0 + 1, p.light());
        crate(b, x1 - 2, z0 + 2, ground, p, 1);
    }

    // ================================================================== Malabar Hill

    private static void malabar(StructureBuilder b, CitySurface s, int cx, int cz, Palette p) {
        // The healer's clinic at the foot of the hill.
        clinic(b, cx - 254, cz + 106, cx - 226, cz + 134, s.at(cx - 240, cz + 120), p);
        // Bungalows on their garden plots.
        bungalow(b, cx - 312, cz + 158, cx - 286, cz + 184, s.at(cx - 299, cz + 171), p,
                "BUNGALOW", Direction.EAST);
        bungalow(b, cx - 266, cz + 158, cx - 238, cz + 184, s.at(cx - 252, cz + 171), p,
                "ROSE COTTAGE", Direction.SOUTH);
        bungalow(b, cx - 312, cz + 252, cx - 284, cz + 278, s.at(cx - 298, cz + 265), p,
                "SEA BREEZE", Direction.EAST);
        bungalow(b, cx - 232, cz + 252, cx - 204, cz + 278, s.at(cx - 218, cz + 265), p,
                "PALM HOUSE", Direction.SOUTH);
        // The secret bungalow with the locked study.
        secretBungalow(b, cx - 248, cz + 200, cx - 216, cz + 232, s.at(cx - 232, cz + 216), p);
        // The Towers of Silence, kept abstract and quiet.
        towersOfSilence(b, cx - 316, cz + 288, cx - 286, cz + 316, s.at(cx - 301, cz + 302), p);
        // The lookout over the harbour.
        lookout(b, cx - 132, cz + 250, cx - 112, cz + 270, s.at(cx - 122, cz + 260), p);
        // Authored gardens between the plots.
        gardenPond(b, cx - 298, cz + 214, s.at(cx - 298, cz + 214), p);
        palm(b, cx - 310, cz + 190, s.at(cx - 310, cz + 190));
        palm(b, cx - 270, cz + 190, s.at(cx - 270, cz + 190));
        palm(b, cx - 220, cz + 190, s.at(cx - 220, cz + 190));
        palm(b, cx - 180, cz + 190, s.at(cx - 180, cz + 190));
        palm(b, cx - 310, cz + 240, s.at(cx - 310, cz + 240));
        palm(b, cx - 260, cz + 240, s.at(cx - 260, cz + 240));
        palm(b, cx - 190, cz + 240, s.at(cx - 190, cz + 240));
        palm(b, cx - 160, cz + 160, s.at(cx - 160, cz + 160));
        palm(b, cx - 130, cz + 220, s.at(cx - 130, cz + 220));
        tree(b, cx - 320, cz + 230, s.at(cx - 320, cz + 230));
        tree(b, cx - 240, cz + 292, s.at(cx - 240, cz + 292));
        tree(b, cx - 180, cz + 292, s.at(cx - 180, cz + 292));
        signPost(b, cx - 290, cz + 96, s.at(cx - 290, cz + 96), p, Direction.SOUTH,
                "MALABAR HILL");
        signPost(b, cx - 150, cz + 152, s.at(cx - 150, cz + 152), p, Direction.WEST,
                "MALABAR HILL");
        signPost(b, cx - 122, cz + 246, s.at(cx - 122, cz + 246), p, Direction.SOUTH,
                "SEA VIEW");
    }

    private static void clinic(StructureBuilder b, int x0, int z0, int x1, int z1,
                               int ground, Palette p) {
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 8, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 8, p);
        b.pitchedRoof(x0 - 2, z0 - 2, x1 + 2, z1 + 2, ground + 8, 5, 0);
        door(b, p, (x0 + x1) / 2, ground + 1, z1, Direction.SOUTH);
        awning(b, x0, z0, x1, z1, ground, Direction.SOUTH,
                Materials.wool(DyeColor.WHITE), p);
        sign(b, p, (x0 + x1) / 2, ground + 6, z1 + 4, Direction.SOUTH, "HAKIM");
        sign(b, p, (x0 + x1) / 2 + 4, ground + 4, z1 + 4, Direction.SOUTH, "REMEDIES");
        b.fill(x0 + 1, ground + 1, z0 + 1, x0 + 4, ground + 1, z0 + 3, p.roofSlab());
        b.put(x0 + 2, ground + 1, z0 + 2, CAULDRON);
        b.put(x0 + 3, ground + 2, z0 + 2, p.light());
        bed(b, x1 - 4, ground + 1, z0 + 2, p);
        b.put(x0 + 1, ground + 1, z1 - 2, BOOKS);
        b.put(x0 + 1, ground + 2, z1 - 2, BOOKS);
        b.put(x1 - 1, ground + 1, z1 - 2, BARREL);
        b.chest(x1 - 2, ground + 1, z1 - 2, SiteLoot.RELIC);
        // Herb garden east of the clinic.
        b.fill(x1 + 2, ground, z0 + 2, x1 + 8, ground, z1 - 2, GRASS);
        hedge(b, x1 + 2, z0 + 2, x1 + 8, z0 + 3, ground);
        hedge(b, x1 + 2, z1 - 3, x1 + 8, z1 - 2, ground);
        palm(b, x1 + 6, z1 - 6, ground);
    }

    private static void bungalow(StructureBuilder b, int x0, int z0, int x1, int z1,
                                 int ground, Palette p, String label, Direction face) {
        // Garden: a lawn, a low wall and a gate on the road side.
        int gx0 = x0 - 3;
        int gx1 = x1 + 3;
        int gz0 = z0 - 3;
        int gz1 = z1 + 3;
        b.fill(gx0, ground, gz0, gx1, ground, gz1, GRASS);
        b.walls(gx0, ground + 1, gz0, gx1, ground + 1, gz1, p.weathered());
        int gx = (x0 + x1) / 2;
        int gz = (z0 + z1) / 2;
        switch (face) {
            case EAST -> b.fill(gx1, ground + 1, gz - 1, gx1, ground + 1, gz + 1, AIR);
            case WEST -> b.fill(gx0, ground + 1, gz - 1, gx0, ground + 1, gz + 1, AIR);
            case NORTH -> b.fill(gx - 1, ground + 1, gz0, gx + 1, ground + 1, gz0, AIR);
            default -> b.fill(gx - 1, ground + 1, gz1, gx + 1, ground + 1, gz1, AIR);
        }
        switch (face) {
            case EAST -> b.fill(x1, ground, gz, gx1, ground, gz, p.ground());
            case WEST -> b.fill(gx0, ground, gz, x0, ground, gz, p.ground());
            case NORTH -> b.fill(gx, ground, gz0, gx, ground, z0, p.ground());
            default -> b.fill(gx, ground, z1, gx, ground, gz1, p.ground());
        }
        // The house itself.
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 8, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 8, p);
        b.fill(x0 - 2, ground + 8, z0 - 2, x1 + 2, ground + 8, z1 + 2, p.roofSlab());
        b.pitchedRoof(x0 - 2, z0 - 2, x1 + 2, z1 + 2, ground + 8, 5,
                x1 - x0 >= z1 - z0 ? 0 : 1);
        int dx;
        int dz;
        switch (face) {
            case EAST -> {
                dx = x1;
                dz = gz;
            }
            case WEST -> {
                dx = x0;
                dz = gz;
            }
            case NORTH -> {
                dx = gx;
                dz = z0;
            }
            default -> {
                dx = gx;
                dz = z1;
            }
        }
        door(b, p, dx, ground + 1, dz, face);
        // A veranda along the face.
        if (face.getAxis() == Direction.Axis.X) {
            int ax = face == Direction.EAST ? x1 + 1 : x0 - 3;
            b.fill(ax, ground + 6, z0 + 2, ax + 2, ground + 6, z1 - 2, p.roofSlab());
            b.put(ax + 1, ground + 1, z0 + 2, p.accent());
            b.put(ax + 1, ground + 1, z1 - 2, p.accent());
            b.put(ax + 1, ground + 5, z0 + 2, p.accent());
            b.put(ax + 1, ground + 5, z1 - 2, p.accent());
        } else {
            int az = face == Direction.SOUTH ? z1 + 1 : z0 - 3;
            b.fill(x0 + 2, ground + 6, az, x1 - 2, ground + 6, az + 2, p.roofSlab());
            b.put(x0 + 2, ground + 1, az + 1, p.accent());
            b.put(x1 - 2, ground + 1, az + 1, p.accent());
            b.put(x0 + 2, ground + 5, az + 1, p.accent());
            b.put(x1 - 2, ground + 5, az + 1, p.accent());
        }
        sign(b, p, dx + face.getStepX(), ground + 5, dz + face.getStepZ(), face, label);
        for (int x = x0 + 3; x <= x1 - 3; x += 5) {
            b.window(x, ground + 4, z0, 2, 1, true);
            b.window(x, ground + 4, z1, 2, 1, true);
        }
        b.fill(x0 + 1, ground + 1, z0 + 1, x0 + 4, ground + 1, z0 + 3, p.roofSlab());
        b.put(x0 + 2, ground + 2, z0 + 2, p.light());
        b.put(x1 - 1, ground + 1, z1 - 2, BARREL);
        b.chest(x1 - 2, ground + 1, z1 - 2, SiteLoot.COMMON);
        palm(b, gx0 + 2, gz0 + 2, ground);
        palm(b, gx1 - 2, gz1 - 2, ground);
    }

    private static void secretBungalow(StructureBuilder b, int x0, int z0, int x1, int z1,
                                       int ground, Palette p) {
        // A walled garden with a single gate to the south lane.
        b.fill(x0 - 6, ground, z0 - 6, x1 + 6, ground, z1 + 6, GRASS);
        b.walls(x0 - 6, ground + 1, z0 - 6, x1 + 6, ground + 2, z1 + 6, p.weathered());
        b.fill((x0 + x1) / 2 - 2, ground + 1, z1 + 6, (x0 + x1) / 2 + 2, ground + 2,
                z1 + 6, AIR);
        b.fill((x0 + x1) / 2, ground, z1, (x0 + x1) / 2 + 2, ground, z1 + 6, p.ground());
        // The house.
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 9, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 9, p);
        b.fill(x0 - 2, ground + 9, z0 - 2, x1 + 2, ground + 9, z1 + 2, p.roofSlab());
        b.pitchedRoof(x0 - 2, z0 - 2, x1 + 2, z1 + 2, ground + 9, 6, 1);
        door(b, p, (x0 + x1) / 2, ground + 1, z1, Direction.SOUTH);
        sign(b, p, (x0 + x1) / 2, ground + 6, z1 + 1, Direction.SOUTH, "BUNGALOW");
        // The locked study in the north-east corner.
        int sx0 = x1 - 12;
        int sz0 = z0 + 2;
        b.walls(sx0, ground + 1, sz0, x1 - 1, ground + 4, sz0 + 10, p.wall());
        b.fill(sx0 + 1, ground + 1, sz0 + 1, x1 - 2, ground + 4, sz0 + 9, AIR);
        b.fill(x1 - 6, ground + 1, sz0 + 10, x1 - 5, ground + 2, sz0 + 10, AIR);
        b.put(x1 - 8, ground + 1, sz0 + 10, p.rail());
        b.put(x1 - 8, ground + 2, sz0 + 10, p.rail());
        b.put(x1 - 3, ground + 1, sz0 + 10, p.rail());
        b.put(x1 - 3, ground + 2, sz0 + 10, p.rail());
        sign(b, p, x1 - 6, ground + 3, sz0 + 11, Direction.SOUTH, "STUDY LOCKED");
        // The mask on the study wall and the desk beneath it.
        maskPanel(b, sx0 + 3, ground + 3, sz0 + 1);
        b.put(sx0 + 4, ground + 1, sz0 + 2, p.roofSlab());
        b.put(sx0 + 4, ground + 2, sz0 + 2, p.light());
        sign(b, p, sx0 + 4, ground + 4, sz0 + 2, Direction.SOUTH, "THE MASK");
        b.chest(sx0 + 2, ground + 1, sz0 + 8, SiteLoot.CULT);
        b.chest(x1 - 2, ground + 1, sz0 + 2, SiteLoot.RELIC);
        sign(b, p, sx0 + 7, ground + 3, sz0 + 9, Direction.NORTH, "SERVANT'S NOTE");
        b.put(sx0 + 2, ground + 1, sz0 + 4, BOOKS);
        b.put(sx0 + 2, ground + 2, sz0 + 4, BOOKS);
        b.put(sx0 + 2, ground + 3, sz0 + 4, Palette.cobweb());
        // The hall, its table and a portrait.
        b.fill(x0 + 2, ground + 1, z0 + 3, x0 + 8, ground + 1, z0 + 4, p.roofSlab());
        b.put(x0 + 3, ground + 2, z0 + 3, p.light());
        b.put(x0 + 2, ground + 1, z1 - 2, BARREL);
        b.put(x0 + 3, ground + 1, z1 - 2, BARREL);
        b.chest(x0 + 2, ground + 1, z0 + 8, SiteLoot.COMMON);
        b.put(x1 - 1, ground + 1, z0 + 1, Palette.cobweb());
        palm(b, x0 - 4, z1 + 3, ground);
        tree(b, x1 + 4, z1 + 3, ground);
        signPost(b, (x0 + x1) / 2, z1 + 7, ground, p, Direction.SOUTH, "MALABAR HILL");
    }

    private static void towersOfSilence(StructureBuilder b, int x0, int z0, int x1, int z1,
                                        int ground, Palette p) {
        b.fill(x0, ground, z0, x1, ground, z1, GRASS);
        b.walls(x0, ground + 1, z0, x1, ground + 3, z1, p.foundation());
        b.fill((x0 + x1) / 2 - 1, ground + 1, z0, (x0 + x1) / 2 + 1, ground + 3, z0, AIR);
        int cx = (x0 + x1) / 2;
        int cz = (z0 + z1) / 2;
        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 100) {
                    b.put(cx + dx, ground + 1, cz + dz, p.foundation());
                }
                if (d2 <= 100 && d2 >= 64) {
                    b.put(cx + dx, ground + 2, cz + dz, p.accent());
                    b.put(cx + dx, ground + 3, cz + dz, p.accent());
                }
                if (d2 <= 9) {
                    b.put(cx + dx, ground + 1, cz + dz, AIR);
                    b.put(cx + dx, ground, cz + dz, WATER);
                }
            }
        }
        b.put(cx, ground + 4, cz + 10, p.light());
        tree(b, x0 + 3, z0 + 4, ground);
        tree(b, x1 - 3, z0 + 4, ground);
        tree(b, x0 + 3, z1 - 4, ground);
        tree(b, x1 - 3, z1 - 4, ground);
        signPost(b, cx, z0 + 1, ground, p, Direction.NORTH, "SILENCE TOWERS");
    }

    private static void lookout(StructureBuilder b, int x0, int z0, int x1, int z1,
                                int ground, Palette p) {
        b.fill(x0, ground - 1, z0, x1, ground + 2, z1, p.foundation());
        b.fill(x0 + 1, ground + 2, z0 + 1, x1 - 1, ground + 2, z1 - 1, p.ground());
        b.fill(x1, ground + 3, z0, x1, ground + 3, z1, p.rail());
        b.fill(x0, ground + 3, z1, x1, ground + 3, z1, p.rail());
        b.fill(x0, ground + 3, z0, x0, ground + 3, z0 + 2, p.rail());
        b.fill(x0, ground + 3, z1 - 2, x0, ground + 3, z1, p.rail());
        b.fill(x0, ground + 3, z0 + 3, x0, ground + 3, z1 - 3, AIR);
        stairs(b, p.roofStairs(), x0 - 2, ground + 1, (z0 + z1) / 2, Direction.EAST);
        stairs(b, p.roofStairs(), x0 - 1, ground + 2, (z0 + z1) / 2, Direction.EAST);
        bench(b, x0 + 4, z1 - 3, ground + 2, p);
        b.put(x1 - 2, ground + 3, z0 + 2, p.light());
        signPost(b, x0 + 2, z1 + 2, ground, p, Direction.SOUTH, "SEA VIEW");
    }

    private static void gardenPond(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 8, ground, z - 6, x + 8, ground, z + 6, GRASS);
        b.fill(x - 6, ground - 1, z - 4, x + 6, ground - 1, z + 4, p.foundation());
        b.fill(x - 5, ground - 1, z - 3, x + 5, ground - 1, z + 3, WATER);
        b.fill(x - 5, ground, z - 3, x + 5, ground, z + 3, WATER);
        b.put(x - 3, ground, z - 1, LILY);
        b.put(x + 2, ground, z + 1, LILY);
        b.put(x + 4, ground, z - 2, LILY);
        for (int dx = -6; dx <= 6; dx += 3) {
            b.put(x + dx, ground, z - 5, p.weathered());
            b.put(x + dx, ground, z + 5, p.weathered());
        }
        tree(b, x - 8, z + 6, ground);
        palm(b, x + 8, z - 6, ground);
    }

    // ================================================================== mills

    private static void mills(StructureBuilder b, CitySurface s, int cx, int cz, Palette p) {
        // Workers' terraces, split by the harbour link road.
        for (int i = 0; i < 3; i++) {
            int z0 = cz + 196 + i * 12;
            housing(b, cx + 154, z0, cx + 184, z0 + 11, s.at(cx + 169, z0 + 5), p);
        }
        for (int i = 0; i < 4; i++) {
            int z0 = cz + 256 + i * 12;
            housing(b, cx + 154, z0, cx + 184, z0 + 11, s.at(cx + 169, z0 + 5), p);
        }
        well(b, cx + 190, cz + 214, s.at(cx + 190, cz + 214), p);
        // Laundry lines in the yard.
        for (int i = 0; i < 3; i++) {
            int lz = cz + 220 + i * 4;
            int ly = s.at(cx + 190, lz);
            b.put(cx + 186, ly + 1, lz, p.accent());
            b.put(cx + 186, ly + 2, lz, p.accent());
            b.put(cx + 193, ly + 1, lz, p.accent());
            b.put(cx + 193, ly + 2, lz, p.accent());
            b.fill(cx + 187, ly + 2, lz, cx + 192, ly + 2, lz,
                    Materials.wool((i & 1) == 0 ? DyeColor.WHITE : DyeColor.RED));
        }
        // The mill gate across the mill road.
        int gy = s.at(cx + 200, cz + 190);
        b.fill(cx + 196, gy + 1, cz + 189, cx + 198, gy + 8, cz + 191, p.wall());
        b.fill(cx + 203, gy + 1, cz + 189, cx + 205, gy + 8, cz + 191, p.wall());
        b.fill(cx + 196, gy + 9, cz + 189, cx + 205, gy + 9, cz + 191, p.wall());
        b.fill(cx + 196, gy + 10, cz + 188, cx + 205, gy + 10, cz + 192, p.roofSlab());
        b.fill(cx + 199, gy + 1, cz + 190, cx + 202, gy + 7, cz + 190, AIR);
        sign(b, p, cx + 200, gy + 9, cz + 192, Direction.SOUTH, "MILLS");
        // The mills and their chimneys.
        int m1 = s.at(cx + 286, cz + 214);
        int m2 = s.at(cx + 286, cz + 278);
        mill(b, cx + 254, cz + 192, cx + 318, cz + 236, m1, p, "MILL NO 1",
                cx + 306, cz + 228, 34);
        mill(b, cx + 254, cz + 256, cx + 318, cz + 300, m2, p, "MILL NO 2",
                cx + 272, cz + 264, 30);
        sign(b, p, cx + 286, m2 + 5, cz + 255, Direction.NORTH, "MILL NO 2");
        millRuin(b, cx + 214, cz + 280, cx + 246, cz + 316, s.at(cx + 230, cz + 298), p);
        // The mill office above the cellar.
        int oy = s.at(cx + 236, cz + 226);
        millOffice(b, cx + 220, cz + 208, cx + 252, cz + 244, oy, p);
        // The secret cellar beneath the office and yard.
        millCellar(b, cx, cz, oy, p);
        // Cotton bales and coal in the yard.
        int cy = s.at(cx + 223, cz + 250);
        for (int i = 0; i < 3; i++) {
            b.fill(cx + 220 + i * 3, cy + 1, cz + 250, cx + 221 + i * 3,
                    cy + 1 + (i & 1), cz + 251,
                    Materials.wool(DyeColor.WHITE));
        }
        int c2 = s.at(cx + 258, cz + 250);
        b.fill(cx + 256, c2 + 1, cz + 248, cx + 261, c2 + 2, cz + 252, COAL);
        int c3 = s.at(cx + 242, cz + 277);
        b.fill(cx + 240, c3 + 1, cz + 276, cx + 244, c3 + 1, cz + 279, COAL);
        signPost(b, cx + 210, cz + 276, s.at(cx + 210, cz + 276), p, Direction.WEST,
                "KEEP OUT");
        signPost(b, cx + 208, cz + 312, s.at(cx + 208, cz + 312), p, Direction.NORTH,
                "MILLS");
        lanternPost(b, cx + 192, cz + 236, s.at(cx + 192, cz + 236), p);
        lanternPost(b, cx + 192, cz + 300, s.at(cx + 192, cz + 300), p);
    }

    private static void housing(StructureBuilder b, int x0, int z0, int x1, int z1,
                                int ground, Palette p) {
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 8, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 8, p);
        b.fill(x0 - 1, ground + 8, z0 - 1, x1 + 1, ground + 8, z1 + 1, p.roofSlab());
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 9);
        for (int z = z0 + 2; z <= z1 - 2; z += 4) {
            door(b, p, x1, ground + 1, z, Direction.EAST);
            b.put(x1 + 1, ground, z, p.ground());
            b.window(x0, ground + 4, z, 2, 1, true);
            b.window(x1, ground + 5, z, 2, 1, true);
        }
        b.fill(x0 + 1, ground + 1, z0 + 1, x0 + 4, ground + 1, z0 + 3, p.roofSlab());
        b.put(x0 + 2, ground + 2, z0 + 2, p.light());
        b.put(x0 + 1, ground + 1, z1 - 2, BARREL);
        b.chest(x0 + 2, ground + 1, z1 - 2, SiteLoot.COMMON);
    }

    private static void mill(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                             Palette p, String label, int chimX, int chimZ, int chimH) {
        BlockState brick = Materials.terracotta(DyeColor.RED);
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 16, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        for (int y = ground + 1; y <= ground + 16; y += 4) {
            b.fill(x0, y, z0, x1, y, z0, brick);
            b.fill(x0, y, z1, x1, y, z1, brick);
            b.fill(x0, y, z0, x0, y, z1, brick);
            b.fill(x1, y, z0, x1, y, z1, brick);
        }
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 16, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 16, 8,
                x1 - x0 >= z1 - z0 ? 0 : 1);
        // Two intermediate floors, with a ladder well at the west end.
        b.fill(x0 + 1, ground + 6, z0 + 1, x1 - 1, ground + 6, z1 - 1, p.roofSlab());
        b.fill(x0 + 1, ground + 11, z0 + 1, x1 - 1, ground + 11, z1 - 1, p.roofSlab());
        b.fill(x0 + 1, ground + 6, z0 + 2, x0 + 2, ground + 6, z0 + 3, AIR);
        b.fill(x0 + 1, ground + 11, z0 + 2, x0 + 2, ground + 11, z0 + 3, AIR);
        for (int y = ground + 1; y <= ground + 11; y++) {
            b.put(x0 + 1, y, z0 + 2,
                    Blocks.LADDER.defaultBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
        }
        for (int x = x0 + 4; x <= x1 - 4; x += 6) {
            b.window(x, ground + 3, z0, 3, 2, true);
            b.window(x, ground + 8, z0, 3, 2, true);
            b.window(x, ground + 13, z0, 2, 2, true);
            b.window(x, ground + 3, z1, 3, 2, true);
            b.window(x, ground + 8, z1, 3, 2, true);
            b.window(x, ground + 13, z1, 2, 2, true);
        }
        int dx = (x0 + x1) / 2;
        door(b, p, dx, ground + 1, z1, Direction.SOUTH);
        sign(b, p, dx, ground + 5, z1 + 1, Direction.SOUTH, label);
        for (int x = x0 + 6; x <= x1 - 6; x += 12) {
            b.fill(x, ground + 1, z0 + 6, x, ground + 15, z0 + 6, p.wall());
            b.fill(x, ground + 1, z1 - 6, x, ground + 15, z1 - 6, p.wall());
        }
        chimney(b, chimX, chimZ, ground, chimH, p);
        b.put(x0 + 1, ground + 1, z1 - 2, p.light());
        b.chest(x1 - 2, ground + 1, z1 - 2, SiteLoot.COMMON);
    }

    private static void millRuin(StructureBuilder b, int x0, int z0, int x1, int z1,
                                 int ground, Palette p) {
        BlockState brick = Materials.terracotta(DyeColor.RED);
        for (int y = ground + 1; y <= ground + 12; y++) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                    if (!edge) {
                        continue;
                    }
                    if ((x + z + y) % 5 == 0) {
                        continue;
                    }
                    b.put(x, y, z, y <= ground + 2 ? p.foundation() : brick);
                }
            }
        }
        b.fill(x0, ground, z0, x1, ground, z1, p.rubble());
        b.fill(x0 + 2, ground + 1, z0 + 2, x1 - 2, ground + 1, z1 - 2, p.rubble());
        for (int i = 0; i < 6; i++) {
            b.put(x0 + 2 + i * 5, ground + 5, (z0 + z1) / 2, p.roofSlab());
        }
        chimney(b, x0 + 8, z0 + 8, ground, 12, p);
        b.put(x0 + 3, ground + 1, z0 + 3, Palette.cobweb());
        b.put(x1 - 3, ground + 2, z1 - 3, Palette.cobweb());
        signPost(b, x0 - 2, z0 + 2, ground, p, Direction.WEST, "MILLS CLOSED");
    }

    private static void millOffice(StructureBuilder b, int x0, int z0, int x1, int z1,
                                   int ground, Palette p) {
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 10, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 10, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 10, 5, 0);
        door(b, p, x0, ground + 1, (z0 + z1) / 2, Direction.WEST);
        sign(b, p, x0 - 1, ground + 5, (z0 + z1) / 2, Direction.WEST, "MILL OFFICE");
        b.fill(x0 + 1, ground + 1, z0 + 2, x0 + 6, ground + 1, z0 + 3, p.roofSlab());
        b.put(x0 + 2, ground + 2, z0 + 2, p.light());
        b.put(x0 + 1, ground + 1, z1 - 2, BOOKS);
        b.put(x0 + 2, ground + 1, z1 - 2, BOOKS);
        b.chest(x1 - 2, ground + 1, z1 - 2, SiteLoot.ORDER);
        signPost(b, x0 - 4, z1 + 2, ground, p, Direction.SOUTH, "MILLS CLOSED");
    }

    private static void millCellar(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 216;
        int x1 = cx + 256;
        int z0 = cz + 204;
        int z1 = cz + 248;
        b.fill(x0, ground - 5, z0, x1, ground - 5, z1, p.foundation());
        b.walls(x0, ground - 4, z0, x1, ground - 2, z1, p.foundation());
        b.fill(x0 + 1, ground - 4, z0 + 1, x1 - 1, ground - 2, z1 - 1, AIR);
        b.fill(x0, ground - 1, z0, x1, ground - 1, z1, p.foundation());
        // Ladder shaft up into the office.
        int lx = cx + 236;
        int lz = cz + 212;
        b.fill(lx, ground - 4, lz + 1, lx, ground - 1, lz + 1, p.foundation());
        b.fill(lx, ground - 4, lz, lx, ground, lz, AIR);
        for (int y = ground - 4; y <= ground; y++) {
            b.put(lx, y, lz,
                    Blocks.LADDER.defaultBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        }
        // Ledgers and the shrine.
        b.fill(x0 + 2, ground - 4, z0 + 2, x0 + 4, ground - 4, z0 + 2, BOOKS);
        b.put(x0 + 2, ground - 3, z0 + 2, BOOKS);
        b.put(x0 + 3, ground - 3, z0 + 2, BOOKS);
        b.put(x0 + 4, ground - 3, z0 + 2, BOOKS);
        sign(b, p, x0 + 3, ground - 3, z0 + 3, Direction.SOUTH, "LEDGERS");
        b.fill(x0 + 8, ground - 4, z1 - 4, x1 - 2, ground - 4, z1 - 2, p.foundation());
        b.fill(x0 + 10, ground - 4, z1 - 3, x0 + 12, ground - 3, z1 - 3,
                Materials.wool(DyeColor.RED));
        b.put(x0 + 11, ground - 3, z1 - 3, p.accent());
        b.put(x0 + 9, ground - 3, z1 - 3, p.light());
        b.put(x0 + 13, ground - 3, z1 - 3, p.light());
        sign(b, p, x0 + 11, ground - 3, z1 - 2, Direction.SOUTH, "SHRINE");
        b.chest(x0 + 2, ground - 4, z1 - 2, SiteLoot.ORDER);
        b.chest(x1 - 2, ground - 4, z0 + 2, SiteLoot.RELIC);
        crate(b, x0 + 6, z0 + 2, ground - 4, p, 1);
        crate(b, x0 + 7, z0 + 2, ground - 4, p, 2);
        crate(b, x1 - 2, z1 - 5, ground - 4, p, 1);
        b.put(x0 + 1, ground - 2, z0 + 1, Palette.cobweb());
        b.put(x1 - 1, ground - 2, z1 - 1, Palette.cobweb());
        b.put(x0 + 1, ground - 2, z1 - 1, Palette.cobweb());
    }

    // ================================================================== Fort

    private static void fort(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Secretariat and offices along the axis.
        office(b, cx - 72, cz - 252, cx - 16, cz - 224, ground, p, "FORT OFFICES",
                Direction.EAST, 12);
        office(b, cx - 112, cz - 252, cx - 92, cz - 224, ground, p, "SECRETARIAT",
                Direction.EAST, 10);
        office(b, cx + 44, cz - 252, cx + 72, cz - 224, ground, p, "FORT HOTEL",
                Direction.WEST, 11);
        office(b, cx + 92, cz - 252, cx + 116, cz - 224, ground, p, "RECORDS",
                Direction.WEST, 10);
        church(b, cx, cz, ground, p);
        office(b, cx - 72, cz - 204, cx - 16, cz - 172, ground, p, "FORT OFFICES",
                Direction.EAST, 12);
        office(b, cx - 112, cz - 204, cx - 92, cz - 172, ground, p, "CUSTOMS",
                Direction.EAST, 10);
        theatre(b, cx - 72, cz - 152, cx - 28, cz - 128, ground, p);
        bank(b, cx + 24, cz - 152, cx + 64, cz - 128, ground, p);
        office(b, cx + 92, cz - 152, cx + 116, cz - 128, ground, p, "OFFICES",
                Direction.WEST, 10);
        office(b, cx - 72, cz - 92, cx - 16, cz - 72, ground, p, "OFFICES",
                Direction.EAST, 10);
        office(b, cx - 112, cz - 92, cx - 92, cz - 72, ground, p, "CUSTOMS",
                Direction.EAST, 10);
        postOffice(b, cx + 24, cz - 92, cx + 64, cz - 72, ground, p);
        courthouse(b, cx - 72, cz - 32, cx - 16, cz - 6, ground, p);
        office(b, cx - 112, cz - 32, cx - 92, cz - 6, ground, p, "OFFICES",
                Direction.EAST, 10);
        office(b, cx + 24, cz - 32, cx + 64, cz - 6, ground, p, "OFFICES",
                Direction.WEST, 10);
        // The Fort square and its monument.
        pave(b, cx - 34, cz - 70, cx + 34, cz - 48, ground, p.foundation());
        outline(b, cx - 34, cz - 70, cx + 34, cz - 48, ground, p.accent());
        b.monument(cx + 20, cz - 60, ground + 1);
        bench(b, cx - 24, cz - 66, ground, p);
        bench(b, cx - 24, cz - 54, ground, p);
        bench(b, cx + 6, cz - 66, ground, p);
        lanternPost(b, cx - 30, cz - 70, ground, p);
        lanternPost(b, cx + 30, cz - 70, ground, p);
        signPost(b, cx + 12, cz - 46, ground, p, Direction.SOUTH, "FORT OFFICES");
        signPost(b, cx - 10, cz - 46, ground, p, Direction.SOUTH, "FORT");
        // The north square at the head of the axis.
        pave(b, cx - 24, cz - 292, cx + 24, cz - 254, ground, p.ground());
        outline(b, cx - 24, cz - 292, cx + 24, cz - 254, ground, p.foundation());
        b.monument(cx - 14, cz - 272, ground + 1);
        bench(b, cx + 8, cz - 280, ground, p);
        lanternPost(b, cx - 20, cz - 288, ground, p);
        lanternPost(b, cx + 20, cz - 288, ground, p);
    }

    private static void office(StructureBuilder b, int x0, int z0, int x1, int z1,
                               int ground, Palette p, String label, Direction face,
                               int height) {
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + height, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + height, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + height, 5,
                x1 - x0 >= z1 - z0 ? 0 : 1);
        int dx;
        int dz;
        switch (face) {
            case WEST -> {
                dx = x0;
                dz = (z0 + z1) / 2;
            }
            case EAST -> {
                dx = x1;
                dz = (z0 + z1) / 2;
            }
            case NORTH -> {
                dx = (x0 + x1) / 2;
                dz = z0;
            }
            default -> {
                dx = (x0 + x1) / 2;
                dz = z1;
            }
        }
        door(b, p, dx, ground + 1, dz, face);
        sign(b, p, dx + face.getStepX(), ground + 5, dz + face.getStepZ(), face, label);
        for (int x = x0 + 3; x <= x1 - 3; x += 5) {
            b.window(x, ground + 3, z0, 2, 1, true);
            b.window(x, ground + 3, z1, 2, 1, true);
            if (height > 9) {
                b.window(x, ground + 8, z0, 2, 1, true);
                b.window(x, ground + 8, z1, 2, 1, true);
            }
        }
        for (int z = z0 + 3; z <= z1 - 3; z += 5) {
            b.window(x0, ground + 3, z, 2, 1, true);
            b.window(x1, ground + 3, z, 2, 1, true);
            if (height > 9) {
                b.window(x0, ground + 8, z, 2, 1, true);
                b.window(x1, ground + 8, z, 2, 1, true);
            }
        }
        b.fill(x0 + 1, ground + 1, z0 + 1, x0 + 5, ground + 1, z0 + 3, p.roofSlab());
        b.put(x0 + 2, ground + 2, z0 + 2, p.light());
        b.put(x0 + 1, ground + 1, z1 - 2, BOOKS);
        b.put(x0 + 2, ground + 1, z1 - 2, BOOKS);
        b.chest(x1 - 2, ground + 1, z1 - 2, SiteLoot.COMMON);
    }

    private static void church(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 44;
        int x1 = cx + 72;
        int z0 = cz - 204;
        int z1 = cz - 180;
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 14, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 14, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 14, 9, 0);
        // The entrance tower and spire at the south-west corner.
        int tx0 = cx + 44;
        int tx1 = cx + 54;
        int tz0 = cz - 180;
        int tz1 = cz - 170;
        b.room(tx0, ground, tz0, tx1, ground + 28, tz1);
        b.fill(tx0, ground, tz0, tx1, ground, tz1, p.foundation());
        quoins(b, tx0, tz0, tx1, tz1, ground + 1, ground + 28, p);
        for (int y = ground + 6; y <= ground + 24; y += 6) {
            b.window(tx0, y, (tz0 + tz1) / 2, 3, 1, true);
            b.window(tx1, y, (tz0 + tz1) / 2, 3, 1, true);
        }
        b.crenellations(tx0 - 1, tz0 - 1, tx1 + 1, tz1 + 1, ground + 29);
        b.spire((tx0 + tx1) / 2, (tz0 + tz1) / 2, ground + 30, 16);
        door(b, p, (tx0 + tx1) / 2, ground + 1, tz1, Direction.SOUTH);
        sign(b, p, (tx0 + tx1) / 2, ground + 7, tz1 + 1, Direction.SOUTH, "ST THOMAS");
        for (int z = z0 + 5; z <= z1 - 5; z += 6) {
            b.window(x0, ground + 5, z, 4, 1, true);
            b.window(x1, ground + 5, z, 4, 1, true);
        }
        b.fill(x0 + 2, ground + 1, z0 + 2, x0 + 6, ground + 1, z0 + 4, p.roofSlab());
        b.put(x0 + 3, ground + 2, z0 + 3, p.light());
        b.put(x0 + 1, ground + 1, z1 - 2, BOOKS);
        // A small graveyard west of the nave.
        int gx0 = cx + 30;
        int gx1 = cx + 40;
        b.walls(gx0, ground + 1, z0, gx1, ground + 1, z1, p.weathered());
        for (int z = z0 + 3; z <= z1 - 3; z += 5) {
            b.put(gx0 + 3, ground + 1, z, p.weathered());
            b.put(gx0 + 3, ground + 2, z, p.weathered());
            b.put(gx0 + 7, ground + 1, z + 2, p.foundation());
            b.put(gx0 + 7, ground + 2, z + 2, p.foundation());
        }
        tree(b, gx0 + 2, z1 - 3, ground);
    }

    private static void theatre(StructureBuilder b, int x0, int z0, int x1, int z1,
                                int ground, Palette p) {
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 12, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 12, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 12, 6, 0);
        // The forecourt and the marquee.
        b.fill(x0, ground, z1, x1, ground, z1 + 10, p.ground());
        b.fill(x0 + 2, ground + 8, z1, x1 - 2, ground + 8, z1 + 2, p.roofSlab());
        for (int x = x0 + 4; x <= x1 - 4; x += 8) {
            b.fill(x, ground + 1, z1 + 2, x, ground + 7, z1 + 2, p.accent());
        }
        b.fill(x0 + 2, ground + 9, z1, x1 - 2, ground + 9, z1, p.accent());
        b.fill(x0 + 8, ground + 10, z1, x1 - 8, ground + 10, z1, p.wall());
        b.fill(x0 + 12, ground + 11, z1, x1 - 12, ground + 11, z1, p.wall());
        door(b, p, (x0 + x1) / 2, ground + 1, z1, Direction.SOUTH);
        sign(b, p, (x0 + x1) / 2, ground + 7, z1 + 3, Direction.SOUTH, "PARSEE THEATRE");
        // Stage at the north end, red curtain, and rows of seats.
        b.fill(x0 + 2, ground + 1, z0 + 2, x1 - 2, ground + 1, z0 + 6, p.accent());
        b.fill(x0 + 2, ground + 2, z0 + 2, x1 - 2, ground + 6, z0 + 2,
                Materials.wool(DyeColor.RED));
        maskPanel(b, (x0 + x1) / 2 - 1, ground + 4, z0 + 3);
        for (int z = z0 + 9; z <= z1 - 4; z += 4) {
            for (int x = x0 + 3; x <= x1 - 3; x += 3) {
                stairs(b, p.roofStairs(), x, ground + 1, z, Direction.NORTH);
            }
        }
        // The missing actor's chest sits behind the curtain; the notice is in the lobby.
        b.chest(x1 - 3, ground + 1, z0 + 1, SiteLoot.CULT);
        sign(b, p, x0 + 6, ground + 3, z1 - 2, Direction.SOUTH, "ACTOR MISSING");
        b.put(x0 + 2, ground + 1, z1 - 2, BOOKS);
        b.put(x0 + 3, ground + 1, z1 - 2, BOOKS);
        b.put(x0 + 2, ground + 2, z1 - 2, p.light());
        signPost(b, (x0 + x1) / 2, z1 + 11, ground, p, Direction.SOUTH, "PARSEE THEATRE");
    }

    private static void bank(StructureBuilder b, int x0, int z0, int x1, int z1,
                             int ground, Palette p) {
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 12, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 12, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 12, 6, 0);
        // A columned portico on the west face.
        for (int z = z0 + 4; z <= z1 - 4; z += 6) {
            b.fill(x0 - 3, ground + 1, z, x0 - 3, ground + 8, z, p.accent());
            b.put(x0 - 3, ground + 9, z, p.accent());
        }
        b.fill(x0 - 4, ground + 10, z0 + 2, x0 - 1, ground + 10, z1 - 2, p.roofSlab());
        b.fill(x0 - 4, ground + 11, z0 + 6, x0 - 1, ground + 11, z1 - 6, p.wall());
        door(b, p, x0, ground + 1, (z0 + z1) / 2, Direction.WEST);
        sign(b, p, x0 - 2, ground + 5, (z0 + z1) / 2, Direction.WEST, "BANK");
        b.fill(x0 + 1, ground + 1, z0 + 2, x0 + 6, ground + 1, z0 + 3, p.roofSlab());
        b.put(x0 + 2, ground + 2, z0 + 2, p.light());
        b.walls(x1 - 6, ground + 1, z1 - 5, x1 - 2, ground + 3, z1 - 2, p.rail());
        b.fill(x1 - 4, ground + 1, z1 - 5, x1 - 4, ground + 2, z1 - 5, AIR);
        b.chest(x1 - 4, ground + 1, z1 - 3, SiteLoot.ORDER);
    }

    private static void postOffice(StructureBuilder b, int x0, int z0, int x1, int z1,
                                   int ground, Palette p) {
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 10, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 10, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 10, 5, 0);
        door(b, p, x0, ground + 1, (z0 + z1) / 2, Direction.WEST);
        sign(b, p, x0 - 1, ground + 5, (z0 + z1) / 2, Direction.WEST, "POST");
        b.fill(x0 + 1, ground + 1, z0 + 2, x0 + 5, ground + 1, z0 + 3, p.roofSlab());
        for (int x = x0 + 1; x <= x1 - 2; x += 2) {
            b.put(x, ground + 1, z1 - 2, BOOKS);
            b.put(x, ground + 2, z1 - 2, BOOKS);
        }
        b.put(x0 + 2, ground + 2, z0 + 2, p.light());
        b.chest(x1 - 2, ground + 1, z1 - 2, SiteLoot.COMMON);
    }

    private static void courthouse(StructureBuilder b, int x0, int z0, int x1, int z1,
                                   int ground, Palette p) {
        b.fill(x0, ground - 2, z0, x1, ground - 1, z1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 14, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 14, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 14, 6, 0);
        for (int x = x0 + 6; x <= x1 - 6; x += 8) {
            b.fill(x, ground + 1, z1, x, ground + 9, z1, p.accent());
        }
        b.fill(x0 + 3, ground + 10, z1, x1 - 3, ground + 10, z1, p.roofSlab());
        b.fill(x0 + 6, ground + 11, z1, x1 - 6, ground + 11, z1, p.wall());
        b.fill(x0 + 10, ground + 12, z1, x1 - 10, ground + 12, z1, p.wall());
        door(b, p, (x0 + x1) / 2, ground + 1, z1, Direction.SOUTH);
        sign(b, p, (x0 + x1) / 2, ground + 7, z1 + 1, Direction.SOUTH, "COURT");
        for (int z = z0 + 5; z <= z1 - 5; z += 6) {
            b.window(x0, ground + 5, z, 4, 1, true);
            b.window(x1, ground + 5, z, 4, 1, true);
        }
        b.fill(x0 + 2, ground + 1, z0 + 2, x0 + 8, ground + 1, z0 + 4, p.roofSlab());
        b.put(x0 + 3, ground + 2, z0 + 3, p.light());
        b.chest(x1 - 2, ground + 1, z1 - 2, SiteLoot.ORDER);
    }

    private static void northGate(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int z = cz - 296;
        b.fill(cx - 10, ground + 1, z - 3, cx - 8, ground + 11, z + 3, p.wall());
        b.fill(cx + 8, ground + 1, z - 3, cx + 10, ground + 11, z + 3, p.wall());
        b.fill(cx - 10, ground + 12, z - 3, cx + 10, ground + 12, z + 3, p.wall());
        b.fill(cx - 11, ground + 13, z - 4, cx + 11, ground + 13, z + 4, p.roofSlab());
        b.fill(cx - 7, ground + 1, z - 2, cx + 7, ground + 10, z + 2, AIR);
        b.crenellations(cx - 11, z - 4, cx + 11, z + 4, ground + 14);
        sign(b, p, cx, ground + 14, z + 4, Direction.SOUTH, "FORT");
        b.put(cx - 12, ground + 1, z, p.light());
        b.put(cx + 12, ground + 1, z, p.light());
    }

    // ================================================================== atmosphere

    private static void puddles(StructureBuilder b, CitySurface s, int cx, int cz, Palette p) {
        puddle(b, cx - 256, cz - 120, s.at(cx - 256, cz - 120), p);
        puddle(b, cx - 270, cz + 130, s.at(cx - 270, cz + 130), p);
        puddle(b, cx - 180, cz + 270, s.at(cx - 180, cz + 270), p);
        puddle(b, cx - 150, cz + 150, s.at(cx - 150, cz + 150), p);
        puddle(b, cx + 200, cz - 120, s.at(cx + 200, cz - 120), p);
        puddle(b, cx + 210, cz + 260, s.at(cx + 210, cz + 260), p);
        puddle(b, cx - 20, cz - 60, s.at(cx - 20, cz - 60), p);
        puddle(b, cx + 60, cz + 120, s.at(cx + 60, cz + 120), p);
    }

    private static void puddle(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 2, ground, z - 1, x + 2, ground, z + 1, WATER);
        b.put(x - 3, ground, z - 1, p.rubble());
        b.put(x + 3, ground, z + 1, p.rubble());
    }
}
