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
 * Rio de Janeiro between the wars: Christ the Redeemer on its terraced granite hill, Copacabana's
 * wave-mosaic promenade and beach hotels, the favela's stilt lanes and murals, the port's quays,
 * warehouses and rail yard, the Centro's offices, theatres and cathedral, and the Botanical
 * Garden's palms, glasshouse and hillside cave.
 *
 * <p>Every block is authored. There is no {@code RandomSource}, no layout hash, no static mutable
 * state and no read of the generating chunk: the whole city is written on every call and
 * {@link StructureBuilder} keeps only the slice inside the current chunk. The flat heart uses
 * {@code ground}; rim and outskirt props sample {@link CitySurface#at(int, int)} through the
 * terrain-aware {@link #build(StructureBuilder, CityPlanContext, Palette)} overload.
 */
public final class RioPlan implements CityPlan {

    /** Authored half-extent; every offset below stays inside +/-340. */
    private static final int HALF_EXTENT = 340;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState GRANITE = Blocks.POLISHED_GRANITE.defaultBlockState();
    private static final BlockState MOSSY = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
    private static final BlockState MOSS = Blocks.MOSS_BLOCK.defaultBlockState();
    private static final BlockState GLASS = Blocks.GLASS.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState COARSE = Blocks.COARSE_DIRT.defaultBlockState();
    private static final BlockState BARREL = Blocks.BARREL.defaultBlockState();
    private static final BlockState BOOKS = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState CAULDRON = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState LILY = Blocks.LILY_PAD.defaultBlockState();
    private static final BlockState HAY = Blocks.HAY_BLOCK.defaultBlockState();
    private static final BlockState JUNGLE_LOG = Blocks.JUNGLE_LOG.defaultBlockState();
    private static final BlockState JUNGLE_LEAF = Blocks.JUNGLE_LEAVES.defaultBlockState()
            .setValue(BlockStateProperties.PERSISTENT, true);
    private static final BlockState WHITE = Materials.concrete(DyeColor.WHITE);
    private static final BlockState LIGHT = Materials.lightGrayConcrete();
    private static final BlockState DARK = Materials.concrete(DyeColor.BLACK);
    private static final BlockState BLUE = Materials.concrete(DyeColor.LIGHT_BLUE);
    private static final BlockState RAIL_EW = Blocks.RAIL.defaultBlockState()
            .setValue(BlockStateProperties.RAIL_SHAPE, RailShape.EAST_WEST);

    @Override
    public String cityId() {
        return "rio_de_janeiro";
    }

    @Override
    public int halfExtent() {
        return HALF_EXTENT;
    }

    @Override
    public void build(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Legacy entry point: a conservative graded rim keeps the plan terrain-following.
        build(b, new CityPlanContext(cx, cz, ground, HALF_EXTENT,
                CitySurface.of(b, cx, cz, HALF_EXTENT, ground, 32)), p);
    }

    @Override
    public void build(StructureBuilder b, CityPlanContext ctx, Palette p) {
        int cx = ctx.cx();
        int cz = ctx.cz();
        CitySurface s = ctx.surface();
        streets(b, cx, cz, s, p);
        arrivalGate(b, cx, cz, s, p);
        southPlaza(b, cx, cz, s, p);
        redeemerHill(b, cx, cz, s, p);
        centro(b, cx, cz, s, p);
        copacabana(b, cx, cz, s, p);
        favela(b, cx, cz, s, p);
        port(b, cx, cz, s, p);
        botanicalGarden(b, cx, cz, s, p);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.LANDMARK, 0, 0, 320, "gate"),
                new CityPoi("gate_signpost", CityPoi.Kind.SIGN, -18, 3, 326,
                        "REDEEMER|COPACABANA|FAVELA|PORT"),
                new CityPoi("city_notices", CityPoi.Kind.SIGN, 40, 3, 326, "CITY NOTICES"),
                new CityPoi("centro_quest_board", CityPoi.Kind.QUEST_BOARD, 0, 1, 60,
                        "rio_missing_dancer,rio_smuggled_relic"),
                new CityPoi("quest_board_sign", CityPoi.Kind.SIGN, 12, 3, 66,
                        "MISSING DANCER|SMUGGLED RELIC"),
                new CityPoi("redeemer", CityPoi.Kind.LANDMARK, 0, 24, -200,
                        "statue,chapel,viewpoint"),
                new CityPoi("redeemer_chapel", CityPoi.Kind.LANDMARK, -21, 24, -212, "chapel"),
                new CityPoi("redeemer_view", CityPoi.Kind.LANDMARK, 21, 24, -206, "viewpoint"),
                new CityPoi("guide", CityPoi.Kind.NPC, 0, 24, -180, "guide"),
                new CityPoi("copacabana", CityPoi.Kind.LANDMARK, 240, 0, 40,
                        "beach,promenade"),
                new CityPoi("hotel", CityPoi.Kind.INN, 180, 0, -60, "hotel_keeper"),
                new CityPoi("hotel_clerk", CityPoi.Kind.NPC, 180, 0, -52, "hotel_clerk"),
                new CityPoi("beach_cafe", CityPoi.Kind.LANDMARK, 230, 0, 88, "cafe"),
                new CityPoi("centro_shop", CityPoi.Kind.SHOP, -40, 0, 120, "merchant"),
                new CityPoi("shopkeeper", CityPoi.Kind.NPC, -40, 0, 114, "shopkeeper"),
                new CityPoi("herbalist", CityPoi.Kind.HEALER, -240, 0, 120, "remedies"),
                new CityPoi("herbalist_npc", CityPoi.Kind.NPC, -240, 0, 114, "herbalist"),
                new CityPoi("dance_hall", CityPoi.Kind.LANDMARK, -240, 0, 14, "dance_hall"),
                new CityPoi("dance_teacher", CityPoi.Kind.NPC, -240, 0, 4,
                        "rio_missing_dancer"),
                new CityPoi("port_customs", CityPoi.Kind.NPC, 239, 0, 240,
                        "rio_smuggled_relic"),
                new CityPoi("fisherman", CityPoi.Kind.NPC, 298, 0, 230, "fisherman"),
                new CityPoi("port_market", CityPoi.Kind.LANDMARK, 220, 0, 282, "market,fish"),
                new CityPoi("harbour_port", CityPoi.Kind.PORT, 306, 0, 250, "guanabara_bay"),
                new CityPoi("rail_station", CityPoi.Kind.STATION, 120, 1, 328, "east_west"),
                new CityPoi("hillside_cave", CityPoi.Kind.SECRET, -240, 0, 222,
                        "chests,lore,missing_dancer,relic"),
                new CityPoi("botanical_garden", CityPoi.Kind.LANDMARK, -240, 0, 270,
                        "palms,greenhouse"),
                new CityPoi("greenhouse", CityPoi.Kind.LANDMARK, -184, 0, 266, "orchids"),
                new CityPoi("cathedral", CityPoi.Kind.LANDMARK, 50, 0, -40,
                        "cathedral,bells"),
                new CityPoi("theatre", CityPoi.Kind.LANDMARK, -64, 0, -40, "theatre"));
    }

    // ------------------------------------------------------------------ shared helpers

    private static int at(CitySurface s, int cx, int cz, int dx, int dz) {
        return s.at(cx + dx, cz + dz);
    }

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

    /** A street that follows the authored graded surface along its length. */
    private static void path(StructureBuilder b, CitySurface s, int x0, int z0, int x1, int z1,
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
                int y = s.at(x, cz);
                for (int z = cz + lo; z <= cz + hi; z++) {
                    b.put(x, y, z, state);
                }
            }
        } else {
            int cx = (x0 + x1) / 2;
            for (int z = az; z <= bz; z++) {
                int y = s.at(cx, z);
                for (int x = cx + lo; x <= cx + hi; x++) {
                    b.put(x, y, z, state);
                }
            }
        }
    }

    /** The Copacabana wave: pale stone with running dark and blue bands. */
    private static void mosaic(StructureBuilder b, CitySurface s, int x0, int z0, int x1, int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                int band = Math.floorMod(z + x / 2, 8);
                BlockState tile;
                if (band == 0 || band == 1) {
                    tile = DARK;
                } else if (band == 4) {
                    tile = BLUE;
                } else {
                    tile = WHITE;
                }
                b.put(x, s.at(x, z), z, tile);
            }
        }
    }

    /** A wall-mounted sign with a small trim block beneath it. */
    private static void sign(StructureBuilder b, Palette p, int x, int y, int z,
                             Direction facing, String... lines) {
        b.put(x, y - 1, z, p.accent());
        CityPlanSupport.sign(b, x, y, z, facing, lines);
    }

    /** A freestanding signpost: two trim blocks and the sign on top. */
    private static void signPost(StructureBuilder b, int x, int z, int ground, Palette p,
                                 Direction facing, String... lines) {
        b.put(x, ground + 1, z, p.accent());
        b.put(x, ground + 2, z, p.accent());
        CityPlanSupport.sign(b, x, ground + 3, z, facing, lines);
    }

    private static void door(StructureBuilder b, Palette p, int x, int y, int z,
                             Direction facing) {
        b.put(x, y, z, AIR);
        b.put(x, y + 1, z, AIR);
        BlockState lower = p.door()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        b.put(x, y, z, lower);
        b.put(x, y + 1, z,
                lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
    }

    private static void quoins(StructureBuilder b, int x0, int z0, int x1, int z1,
                               int y0, int y1, Palette p) {
        for (int y = y0; y <= y1; y += 2) {
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

    private static void crate(StructureBuilder b, int x, int z, int ground, Palette p,
                              int height) {
        for (int i = 1; i <= height; i++) {
            b.put(x, ground + i, z, p.foundation());
        }
    }

    private static void palm(StructureBuilder b, int x, int z, int y) {
        int h = 7 + Math.floorMod(x * 3 + z * 5, 3);
        for (int i = 1; i <= h; i++) {
            b.put(x, y + i, z, JUNGLE_LOG);
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 2) {
                    b.put(x + dx, y + h + 1, z + dz, JUNGLE_LEAF);
                }
            }
        }
        b.put(x, y + h + 2, z, JUNGLE_LEAF);
        b.put(x - 1, y + h, z, JUNGLE_LEAF);
        b.put(x + 1, y + h, z, JUNGLE_LEAF);
        b.put(x, y + h, z - 1, JUNGLE_LEAF);
        b.put(x, y + h, z + 1, JUNGLE_LEAF);
    }

    private static void umbrella(StructureBuilder b, int x, int z, int y, Palette p,
                                 BlockState canopy) {
        b.put(x, y + 1, z, p.accent());
        b.put(x, y + 2, z, p.accent());
        b.fill(x - 2, y + 3, z - 2, x + 2, y + 3, z + 2, canopy);
        b.put(x - 2, y + 3, z - 2, WHITE);
        b.put(x + 2, y + 3, z + 2, WHITE);
    }

    /** Levels a rectangular pad to {@code ground} and clears a little headroom. */
    private static void pad(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                            Palette p) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                int surface = b.groundY(x, z);
                if (surface > ground) {
                    b.fill(x, ground + 1, z, x, surface, z, AIR);
                } else if (surface < ground) {
                    b.fill(x, surface + 1, z, x, ground, z, p.foundation());
                }
                b.put(x, ground, z, p.ground());
                b.fill(x, ground + 1, z, x, ground + 4, z, AIR);
            }
        }
    }

    /** A bounded three-block plinth and headroom clear for outskirt buildings near water. */
    private static void plinth(StructureBuilder b, int x0, int z0, int x1, int z1, int base,
                               Palette p) {
        b.fill(x0, base - 3, z0, x1, base, z1, p.foundation());
        b.fill(x0, base + 1, z0, x1, base + 4, z1, AIR);
    }

    private static boolean overlaps(int x0, int z0, int x1, int z1,
                                    int ax0, int az0, int ax1, int az1) {
        return x0 <= ax1 && ax0 <= x1 && z0 <= az1 && az0 <= z1;
    }

    private static BlockState paint(int index) {
        return switch (Math.floorMod(index, 8)) {
            case 0 -> Materials.concrete(DyeColor.YELLOW);
            case 1 -> Materials.concrete(DyeColor.ORANGE);
            case 2 -> Materials.concrete(DyeColor.LIGHT_BLUE);
            case 3 -> Materials.concrete(DyeColor.PINK);
            case 4 -> Materials.concrete(DyeColor.LIME);
            case 5 -> Materials.concrete(DyeColor.CYAN);
            case 6 -> Materials.concrete(DyeColor.MAGENTA);
            default -> Materials.concrete(DyeColor.RED);
        };
    }

    private static BlockState flower(int index) {
        return switch (Math.floorMod(index, 5)) {
            case 0 -> Blocks.POPPY.defaultBlockState();
            case 1 -> Blocks.DANDELION.defaultBlockState();
            case 2 -> Blocks.AZURE_BLUET.defaultBlockState();
            case 3 -> Blocks.OXEYE_DAISY.defaultBlockState();
            default -> Blocks.CORNFLOWER.defaultBlockState();
        };
    }

    // ------------------------------------------------------------------ streets

    private static void streets(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        // Main north-south avenue: arrival gate to the foot of the Redeemer stair.
        path(b, s, cx, cz + 338, cx, cz - 124, 14, p.ground());
        // The two cross avenues of the flat heart.
        path(b, s, cx - 330, cz, cx + 330, cz, 14, p.ground());
        path(b, s, cx - 330, cz + 120, cx + 330, cz + 120, 10, p.ground());
        path(b, s, cx - 330, cz - 80, cx + 330, cz - 80, 10, p.ground());
        // Centro side streets.
        path(b, s, cx - 100, cz - 85, cx - 100, cz + 180, 8, p.ground());
        path(b, s, cx + 100, cz - 85, cx + 100, cz + 180, 8, p.ground());
        // Avenida Atlantica and the Copacabana wave promenade.
        path(b, s, cx + 213, cz - 236, cx + 213, cz + 160, 14, p.ground());
        mosaic(b, s, cx + 244, cz - 236, cx + 262, cz + 160);
        // Port road and quay walk.
        path(b, s, cx + 140, cz + 250, cx + 300, cz + 250, 10, p.ground());
        path(b, s, cx + 298, cz + 184, cx + 298, cz + 318, 6, p.foundation());
        // Favela lanes: eight long lanes crossed by ten narrow ones.
        int[] lanesX = {-312, -292, -272, -252, -232, -212, -192, -172, -152};
        for (int x : lanesX) {
            path(b, s, cx + x, cz - 240, cx + x, cz + 180, 4, p.ground());
        }
        int[] lanesZ = {-216, -176, -136, -96, -56, -16, 24, 64, 144};
        for (int z : lanesZ) {
            path(b, s, cx - 318, cz + z, cx - 142, cz + z, 4, p.ground());
        }
        // Botanical Garden walks.
        path(b, s, cx - 240, cz + 180, cx - 240, cz + 300, 5, GRAVEL);
        path(b, s, cx - 318, cz + 248, cx - 142, cz + 248, 5, GRAVEL);
        // Station approach from the city.
        path(b, s, cx + 40, cz + 300, cx + 100, cz + 330, 6, p.ground());
        path(b, s, cx + 96, cz + 312, cx + 122, cz + 312, 6, p.ground());
        // Lamps along the main avenue and the promenade.
        for (int z = -120; z <= 320; z += 40) {
            lanternPost(b, cx - 10, cz + z, at(s, cx, cz, -10, z), p);
            lanternPost(b, cx + 10, cz + z, at(s, cx, cz, 10, z), p);
        }
        for (int z = -228; z <= 156; z += 32) {
            lanternPost(b, cx + 243, cz + z, at(s, cx, cz, 243, z), p);
        }
    }

    // ------------------------------------------------------------------ arrival gate

    private static void arrivalGate(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = at(s, cx, cz, 0, 320);
        pad(b, cx - 24, cz + 308, cx + 24, cz + 324, ground, p);
        gateTower(b, cx - 22, cz + 310, cx - 12, cz + 322, ground, p);
        gateTower(b, cx + 12, cz + 310, cx + 22, cz + 322, ground, p);
        // The arch over the avenue.
        b.fill(cx - 12, ground + 8, cz + 312, cx + 12, ground + 11, cz + 320, p.wall());
        b.fill(cx - 13, ground + 12, cz + 311, cx + 13, ground + 12, cz + 321, p.roofSlab());
        b.crenellations(cx - 12, cz + 312, cx + 12, cz + 320, ground + 13);
        b.fill(cx - 8, ground + 1, cz + 312, cx + 8, ground + 7, cz + 320, AIR);
        sign(b, p, cx, ground + 10, cz + 320, Direction.SOUTH, "RIO DE JANEIRO");
        sign(b, p, cx, ground + 10, cz + 312, Direction.NORTH, "BEM-VINDO");
        b.put(cx - 10, ground + 1, cz + 324, p.light());
        b.put(cx + 10, ground + 1, cz + 324, p.light());
        b.put(cx - 10, ground + 1, cz + 308, p.light());
        b.put(cx + 10, ground + 1, cz + 308, p.light());

        String[][] directions = {
                {"REDEEMER", "NORTH"},
                {"COPACABANA", "EAST"},
                {"FAVELA", "WEST"},
                {"PORT", "EAST"},
                {"CENTRO", "NORTH"},
                {"BOTANICAL", "GARDEN"}};
        int[] posts = {-26, -18, -10, 10, 18, 26};
        for (int i = 0; i < posts.length; i++) {
            signPost(b, cx + posts[i], cz + 326, at(s, cx, cz, posts[i], 326), p,
                    Direction.SOUTH, directions[i]);
        }

        // City notices on a low wall beside the gate.
        int nz = cz + 326;
        int ng = at(s, cx, cz, 40, 326);
        b.fill(cx + 28, ng + 1, nz, cx + 54, ng + 4, nz, p.foundation());
        b.fill(cx + 27, ng + 5, nz - 1, cx + 55, ng + 5, nz + 1, p.roofSlab());
        sign(b, p, cx + 32, ng + 4, nz, Direction.SOUTH, "CITY NOTICES");
        sign(b, p, cx + 40, ng + 4, nz, Direction.SOUTH, "CURFEW AT DUSK");
        sign(b, p, cx + 48, ng + 4, nz, Direction.SOUTH, "NO WAGONS");
        b.put(cx + 30, ng + 1, nz + 1, p.light());
        b.put(cx + 52, ng + 1, nz + 1, p.light());
    }

    private static void gateTower(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  int ground, Palette p) {
        b.room(x0, ground, z0, x1, ground + 12, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 11, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 12, 4, 1);
        b.window(x0 + 3, ground + 4, z0, 3, 1, true);
        b.window(x1 - 3, ground + 4, z1, 3, 1, true);
        b.put(x0 + 2, ground + 1, z0 + 2, p.light());
    }

    // ------------------------------------------------------------------ south plaza

    private static void southPlaza(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        // Arrival apron and a palm court.
        mosaic(b, s, cx - 40, cz + 280, cx + 40, cz + 308);
        for (int dx = -36; dx <= 36; dx += 12) {
            if (dx == 0) {
                continue; // keep the avenue clear
            }
            palm(b, cx + dx, cz + 292, at(s, cx, cz, dx, 292));
        }
        // Market stalls on the east side of the avenue.
        for (int i = 0; i < 4; i++) {
            int x = cx + 52 + i * 12;
            int z = cz + 270;
            int y = at(s, cx, cz, 52 + i * 12, 270);
            b.fill(x, y + 1, z, x, y + 2, z, p.accent());
            b.fill(x + 6, y + 1, z, x + 6, y + 2, z, p.accent());
            b.fill(x, y + 3, z - 2, x + 6, y + 3, z + 2,
                    i % 2 == 0 ? Materials.wool(DyeColor.RED)
                            : Materials.wool(DyeColor.YELLOW));
            b.fill(x + 1, y + 1, z + 1, x + 5, y + 1, z + 1, p.foundation());
            b.put(x + 2, y + 2, z + 1, HAY);
            b.put(x + 4, y + 2, z + 1, BARREL);
        }
        signPost(b, cx + 44, cz + 264, at(s, cx, cz, 44, 264), p, Direction.SOUTH,
                "MERCADO", "FLOWERS");
        // A palm-lined approach up the avenue.
        for (int z = 200; z <= 280; z += 40) {
            palm(b, cx - 16, cz + z, at(s, cx, cz, -16, z));
            palm(b, cx + 16, cz + z, at(s, cx, cz, 16, z));
        }
        b.put(cx - 30, at(s, cx, cz, -30, 306) + 1, cz + 306, p.light());
        b.put(cx + 30, at(s, cx, cz, 30, 306) + 1, cz + 306, p.light());
    }

    // ------------------------------------------------------------------ Redeemer hill

    /** Terrace level of the Redeemer hill, or -1 outside its apron. */
    private static int hillLevel(int dx, int dz) {
        if (inEllipse(dx, dz, 30, 26)) {
            return 24;
        }
        if (inEllipse(dx, dz, 34, 29)) {
            return 20;
        }
        if (inEllipse(dx, dz, 40, 34)) {
            return 16;
        }
        if (inEllipse(dx, dz, 48, 42)) {
            return 12;
        }
        if (inEllipse(dx, dz, 58, 50)) {
            return 8;
        }
        if (inEllipse(dx, dz, 68, 60)) {
            return 4;
        }
        if (inEllipse(dx, dz, 78, 70)) {
            return 0;
        }
        return -1;
    }

    private static boolean inEllipse(int dx, int dz, int a, int b) {
        return dx * dx * b * b + dz * dz * a * a <= a * a * b * b;
    }

    private static void redeemerHill(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = at(s, cx, cz, 0, -200);
        int hz = cz - 200;
        for (int dx = -78; dx <= 78; dx++) {
            for (int dz = -70; dz <= 70; dz++) {
                int level = hillLevel(dx, dz);
                if (level < 0) {
                    continue;
                }
                int x = cx + dx;
                int z = hz + dz;
                if (level == 0) {
                    b.put(x, ground, z, GRASS);
                    continue;
                }
                b.fill(x, ground + 1, z, x, ground + level, z, STONE);
                b.put(x, ground + level, z, level == 24 ? p.ground() : GRASS);
                boolean edge = hillLevel(dx + 1, dz) < level
                        || hillLevel(dx - 1, dz) < level
                        || hillLevel(dx, dz + 1) < level
                        || hillLevel(dx, dz - 1) < level;
                if (edge) {
                    b.put(x, ground + level, z, p.foundation());
                    b.put(x, ground + level + 1, z, p.foundation());
                }
            }
        }

        // The grand stair cut up the south face, one course per step.
        for (int z = -124; z >= -174; z--) {
            int rise = (-124 - z) * 24 / 50;
            int y = ground + rise;
            b.fill(cx - 4, ground, cz + z, cx + 4, y, cz + z, STONE);
            for (int x = -4; x <= 4; x++) {
                b.put(cx + x, y, cz + z, p.roofSlab());
            }
            b.fill(cx - 4, y + 1, cz + z, cx + 4, y + 5, cz + z, AIR);
            b.fill(cx - 5, ground, cz + z, cx - 5, y + 1, cz + z, p.foundation());
            b.fill(cx + 5, ground, cz + z, cx + 5, y + 1, cz + z, p.foundation());
            if (Math.floorMod(z, 10) == 0) {
                b.put(cx - 5, y + 2, cz + z, p.light());
                b.put(cx + 5, y + 2, cz + z, p.light());
            }
        }

        int top = ground + 24;
        // Plateau walks to the chapel and the viewpoint.
        b.fill(cx - 2, top, cz - 226, cx + 2, top, cz - 174, p.ground());
        b.fill(cx - 28, top, cz - 212, cx - 2, top, cz - 208, p.ground());
        b.fill(cx + 2, top, cz - 206, cx + 30, top, cz - 202, p.ground());

        statue(b, cx, cz, top, p);
        chapel(b, cx, cz, top, p);
        viewpoint(b, cx, cz, top, p);

        // A few palms and a warning sign at the stair foot.
        palm(b, cx - 20, cz - 190, top);
        palm(b, cx + 24, cz - 190, top);
        palm(b, cx - 24, cz - 190, top);
        signPost(b, cx + 8, cz - 120, ground, p, Direction.SOUTH,
                "REDEEMER", "MIND THE STEPS");
    }

    private static void statue(StructureBuilder b, int cx, int cz, int top, Palette p) {
        int z = cz - 200;
        // Three-tier pedestal.
        b.fill(cx - 8, top + 1, z - 8, cx + 8, top + 2, z + 8, GRANITE);
        b.fill(cx - 6, top + 3, z - 6, cx + 6, top + 4, z + 6, p.wall());
        b.fill(cx - 5, top + 5, z - 5, cx + 5, top + 5, z + 5, p.accent());
        for (int y = top + 6; y <= top + 13; y++) {
            b.fill(cx - 2, y, z - 2, cx + 2, y, z + 2, p.wall());
            b.put(cx - 2, y, z - 2, p.accent());
            b.put(cx + 2, y, z + 2, p.accent());
        }
        b.fill(cx - 3, top + 14, z - 3, cx + 3, top + 14, z + 3, p.accent());

        // The Art-Deco figure: a flaring robe, outstretched arms, a small head.
        int fb = top + 15;
        b.fill(cx - 4, fb, z - 3, cx + 4, fb, z + 3, p.accent());
        b.fill(cx - 3, fb + 1, z - 3, cx + 3, fb + 1, z + 3, p.accent());
        b.fill(cx - 3, fb + 2, z - 2, cx + 3, fb + 2, z + 2, p.accent());
        b.fill(cx - 2, fb + 3, z - 2, cx + 2, fb + 19, z + 2, p.accent());
        int sy = fb + 20;
        b.fill(cx - 3, sy - 1, z - 2, cx + 3, sy, z + 2, p.accent());
        int arm = 20;
        for (int d = 4; d <= arm; d++) {
            int drop = d >= arm - 3 ? 1 : 0;
            b.put(cx - d, sy - drop, z, p.accent());
            b.put(cx + d, sy - drop, z, p.accent());
            b.put(cx - d, sy - 1 - drop, z, p.accent());
            b.put(cx + d, sy - 1 - drop, z, p.accent());
        }
        b.put(cx - arm, sy - 2, z, p.accent());
        b.put(cx + arm, sy - 2, z, p.accent());
        b.fill(cx - 1, sy + 2, z - 1, cx + 1, sy + 3, z + 1, p.accent());
        b.put(cx, sy + 4, z, p.light());
        // A small votive light at the foot of the pedestal.
        b.put(cx, top + 1, z + 9, p.light());
        b.put(cx - 3, top + 1, z + 9, p.light());
        b.put(cx + 3, top + 1, z + 9, p.light());
    }

    private static void chapel(StructureBuilder b, int cx, int cz, int top, Palette p) {
        int x0 = cx - 28;
        int x1 = cx - 14;
        int z0 = cz - 220;
        int z1 = cz - 204;
        b.room(x0, top, z0, x1, top + 8, z1);
        b.fill(x0, top, z0, x1, top, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, top + 1, top + 8, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, top + 8, 4, 1);
        door(b, p, x1, top + 1, cz - 212, Direction.EAST);
        sign(b, p, x1, top + 6, cz - 212, Direction.EAST, "CAPELA");
        b.window(x1, top + 4, cz - 217, 2, 1, true);
        b.window(x1, top + 4, cz - 207, 2, 1, true);
        b.fill(x0 + 2, top + 1, z0 + 2, x0 + 5, top + 2, z0 + 4, p.foundation());
        b.put(x0 + 3, top + 3, z0 + 3, p.light());
        b.put(x0 + 6, top + 1, z0 + 2, BOOKS);
        b.put(x0 + 7, top + 1, z0 + 2, BOOKS);
        sign(b, p, x0, top + 5, cz - 212, Direction.WEST, "SILENCE");
    }

    private static void viewpoint(StructureBuilder b, int cx, int cz, int top, Palette p) {
        int x0 = cx + 12;
        int x1 = cx + 30;
        int z0 = cz - 218;
        int z1 = cz - 202;
        b.fill(x0, top, z0, x1, top, z1, p.ground());
        // A low parapet along the east and north edges.
        b.fill(x1, top + 1, z0, x1, top + 1, z1, p.rail());
        b.fill(x0, top + 1, z0, x1, top + 1, z0, p.rail());
        b.fill(x0 + 8, top + 1, z1, x0 + 10, top + 1, z1, p.rail());
        b.put(x1, top + 2, z0, p.light());
        b.put(x1, top + 2, z1, p.light());
        // Two stone benches.
        b.fill(x0 + 3, top + 1, cz - 208, x0 + 6, top + 1, cz - 208, p.roofSlab());
        b.fill(x0 + 3, top + 1, cz - 206, x0 + 6, top + 1, cz - 206, p.roofSlab());
        // A sighting post.
        b.fill(x1 - 3, top + 1, cz - 210, x1 - 3, top + 3, cz - 210, p.accent());
        b.put(x1 - 3, top + 4, cz - 210, p.light());
        signPost(b, x0, z1 + 2, top, p, Direction.SOUTH, "VIEWPOINT");
        signPost(b, x0 + 6, z1 + 2, top, p, Direction.SOUTH, "THE BAY");
    }

    // ------------------------------------------------------------------ Centro

    private static void centro(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = at(s, cx, cz, 0, 0);
        cathedral(b, cx, cz, s, p);
        theatre(b, cx, cz, ground, p);
        questBoard(b, cx, cz, ground, p);

        // Offices north of the cross avenue.
        blockBuilding(b, cx + 14, cz + 14, cx + 38, cz + 50, ground, 11, p, 0, Direction.NORTH);
        blockBuilding(b, cx + 44, cz + 14, cx + 68, cz + 50, ground, 9, p, 1, Direction.NORTH);
        blockBuilding(b, cx + 74, cz + 14, cx + 92, cz + 50, ground, 13, p, 2, Direction.NORTH);
        // Shops between the quest board and the 120 avenue.
        blockBuilding(b, cx + 14, cz + 68, cx + 38, cz + 108, ground, 6, p, 3, Direction.SOUTH);
        blockBuilding(b, cx + 44, cz + 68, cx + 68, cz + 108, ground, 7, p, 0, Direction.SOUTH);
        blockBuilding(b, cx + 74, cz + 68, cx + 92, cz + 108, ground, 5, p, 1, Direction.SOUTH);
        // West side: shops, cafe and the theatre row.
        blockBuilding(b, cx - 90, cz + 14, cx - 66, cz + 52, ground, 8, p, 2, Direction.NORTH);
        blockBuilding(b, cx - 60, cz + 14, cx - 42, cz + 52, ground, 6, p, 3, Direction.NORTH);
        cafe(b, cx - 34, cz + 20, cx - 14, cz + 48, at(s, cx, cz, -24, 34), p, "CAFE");
        blockBuilding(b, cx - 90, cz + 66, cx - 64, cz + 94, ground, 7, p, 0, Direction.SOUTH);
        blockBuilding(b, cx - 58, cz + 66, cx - 42, cz + 94, ground, 5, p, 1, Direction.SOUTH);
        // South rows, facing the 120 avenue.
        blockBuilding(b, cx - 90, cz + 130, cx - 64, cz + 176, ground, 10, p, 2,
                Direction.NORTH);
        blockBuilding(b, cx - 58, cz + 130, cx - 32, cz + 176, ground, 8, p, 0,
                Direction.NORTH);
        blockBuilding(b, cx - 26, cz + 130, cx - 12, cz + 176, ground, 6, p, 3,
                Direction.NORTH);
        blockBuilding(b, cx + 14, cz + 130, cx + 40, cz + 176, ground, 9, p, 1,
                Direction.NORTH);
        blockBuilding(b, cx + 46, cz + 130, cx + 70, cz + 176, ground, 11, p, 2,
                Direction.NORTH);
        blockBuilding(b, cx + 76, cz + 130, cx + 92, cz + 176, ground, 7, p, 0,
                Direction.NORTH);
        // The shop on the 120 avenue.
        shop(b, cx - 56, cz + 96, cx - 24, cz + 112, at(s, cx, cz, -40, 104), p,
                "MERCEARIA");
        // A palm court in the green strip east of the Centro.
        int court = at(s, cx, cz, 120, 40);
        pave(b, cx + 108, cz + 20, cx + 136, cz + 60, court, p.ground());
        outline(b, cx + 108, cz + 20, cx + 136, cz + 60, court, p.foundation());
        for (int dx = 112; dx <= 132; dx += 10) {
            palm(b, cx + dx, cz + 26, at(s, cx, cz, dx, 26));
            palm(b, cx + dx, cz + 54, at(s, cx, cz, dx, 54));
        }
        b.fill(cx + 120, court + 1, cz + 38, cx + 124, court + 2, cz + 42, p.foundation());
        b.put(cx + 122, court + 2, cz + 40, WATER);
        b.put(cx + 121, court + 3, cz + 40, p.light());
        signPost(b, cx + 104, cz + 40, at(s, cx, cz, 104, 40), p, Direction.SOUTH,
                "PALM COURT");
    }

    private static void blockBuilding(StructureBuilder b, int x0, int z0, int x1, int z1,
                                      int base, int height, Palette p, int variant,
                                      Direction facing) {
        b.room(x0, base, z0, x1, base + height, z1);
        b.fill(x0, base, z0, x1, base, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, base + 1, base + height - 1, p);
        // An azulejo band over the shopfronts, on the street face.
        int bandZ = facing == Direction.NORTH ? z0 : z1;
        b.fill(x0, base + 1, bandZ, x1, base + 1, bandZ, p.frame());
        if (variant % 3 == 0) {
            b.fill(x0 - 1, base + height, z0 - 1, x1 + 1, base + height, z1 + 1, p.roofSlab());
            b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, base + height + 1);
        } else {
            int ridge = x1 - x0 >= z1 - z0 ? 0 : 1;
            b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, base + height, 4, ridge);
        }
        for (int y = base + 3; y <= base + height - 2; y += 3) {
            for (int x = x0 + 2; x <= x1 - 2; x += 4) {
                b.window(x, y, z1, 2, 2, true);
                b.window(x, y, z0, 2, 2, true);
            }
            for (int z = z0 + 2; z <= z1 - 2; z += 4) {
                b.window(x0, y, z, 2, 1, true);
                b.window(x1, y, z, 2, 1, true);
            }
        }
        int mx = (x0 + x1) / 2;
        int mz = (z0 + z1) / 2;
        if (facing == Direction.SOUTH) {
            door(b, p, mx, base + 1, z1, Direction.SOUTH);
        } else if (facing == Direction.NORTH) {
            door(b, p, mx, base + 1, z0, Direction.NORTH);
        } else if (facing == Direction.EAST) {
            door(b, p, x1, base + 1, mz, Direction.EAST);
        } else {
            door(b, p, x0, base + 1, mz, Direction.WEST);
        }
        b.put(x0 + 1, base + 1, z0 + 1, p.light());
    }

    private static void shop(StructureBuilder b, int x0, int z0, int x1, int z1, int base,
                             Palette p, String label) {
        blockBuilding(b, x0, z0, x1, z1, base, 6, p, 1, Direction.SOUTH);
        int mx = (x0 + x1) / 2;
        // Striped awning over the door.
        for (int x = x0 + 2; x <= x1 - 2; x++) {
            BlockState stripe = Math.floorMod(x, 2) == 0
                    ? Materials.wool(DyeColor.RED) : WHITE;
            b.put(x, base + 4, z1 + 1, stripe);
        }
        sign(b, p, mx, base + 5, z1, Direction.SOUTH, label);
        b.put(x0 + 2, base + 1, z1 + 1, BARREL);
        b.put(x1 - 2, base + 1, z1 + 1, BARREL);
        b.put(x0 + 1, base + 1, z0 + 2, CAULDRON);
    }

    private static void cafe(StructureBuilder b, int x0, int z0, int x1, int z1, int base,
                             Palette p, String label) {
        b.room(x0, base, z0, x1, base + 4, z1);
        b.fill(x0, base, z0, x1, base, z1, p.foundation());
        b.fill(x0 - 1, base + 5, z0 - 1, x1 + 1, base + 5, z1 + 1, p.roofSlab());
        int mx = (x0 + x1) / 2;
        door(b, p, mx, base + 1, z1, Direction.SOUTH);
        sign(b, p, mx, base + 4, z1, Direction.SOUTH, label);
        for (int x = x0 + 1; x <= x1 - 1; x += 3) {
            b.put(x, base + 4, z1 + 1, BLUE);
        }
        b.put(x0 + 1, base + 1, z0 + 1, BARREL);
        b.put(x1 - 1, base + 1, z0 + 1, CAULDRON);
        b.put(mx, base + 3, z0 + 1, p.light());
    }

    private static void cathedral(StructureBuilder b, int cx, int cz, CitySurface s,
                                  Palette p) {
        int ground = at(s, cx, cz, 50, -40);
        int x0 = cx + 30;
        int x1 = cx + 70;
        int z0 = cz - 60;
        int z1 = cz - 20;
        int top = ground + 16;
        b.room(x0, ground, z0, x1, top, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, top - 1, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, top, 8, 0);
        // The two west towers framing the entrance.
        tower(b, cx + 26, cz - 26, ground, 26, p);
        tower(b, cx + 74, cz - 26, ground, 26, p);
        // Entrance and rose window on the south front.
        door(b, p, cx + 50, ground + 1, z1, Direction.SOUTH);
        b.window(cx + 48, ground + 9, z1, 4, 4, true);
        sign(b, p, cx + 50, ground + 6, z1, Direction.SOUTH, "CATEDRAL");
        for (int x = x0 + 4; x <= x1 - 4; x += 8) {
            b.window(x, ground + 4, z1, 4, 1, true);
            b.window(x, ground + 4, z0, 4, 1, true);
        }
        for (int z = z0 + 6; z <= z1 - 6; z += 8) {
            b.window(x0, ground + 4, z, 4, 1, true);
            b.window(x1, ground + 4, z, 4, 1, true);
        }
        // Nave fittings.
        b.fill(cx + 34, ground + 1, cz - 54, cx + 38, ground + 2, cz - 26, p.foundation());
        b.fill(cx + 62, ground + 1, cz - 54, cx + 66, ground + 2, cz - 26, p.foundation());
        b.put(cx + 50, ground + 3, cz - 56, p.light());
        b.put(cx + 40, ground + 1, cz - 24, BOOKS);
        b.put(cx + 60, ground + 1, cz - 24, BOOKS);
        // The plaza before the doors.
        mosaic(b, s, cx + 20, cz - 16, cx + 80, cz - 8);
        b.fill(cx + 44, ground + 1, cz - 18, cx + 56, ground + 1, cz - 18, p.foundation());
        signPost(b, cx + 20, cz - 12, ground, p, Direction.SOUTH, "CATEDRAL", "OPEN");
    }

    private static void tower(StructureBuilder b, int x, int z, int ground, int height,
                              Palette p) {
        int x0 = x - 6;
        int x1 = x + 6;
        int z0 = z - 6;
        int z1 = z + 6;
        b.room(x0, ground, z0, x1, ground + height, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + height - 1, p);
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + height + 1);
        b.spire(x, z, ground + height + 2, 8);
        b.put(x, ground + height - 4, z, Blocks.BELL.defaultBlockState());
        b.window(x, ground + 8, z1, 3, 1, true);
        b.window(x, ground + 14, z1, 3, 1, true);
        b.window(x0, ground + 8, z, 3, 1, true);
        b.window(x1, ground + 8, z, 3, 1, true);
        b.put(x0 + 1, ground + 1, z0 + 1, p.light());
    }

    private static void theatre(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 88;
        int x1 = cx - 40;
        int z0 = cz - 64;
        int z1 = cz - 16;
        int top = ground + 12;
        b.room(x0, ground, z0, x1, top, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, top - 1, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, top, 4, 0);
        // Four-column portico.
        for (int x = x0 + 6; x <= x1 - 6; x += 10) {
            b.fill(x, ground + 1, z1 + 1, x, ground + 8, z1 + 1, p.accent());
            b.put(x, ground + 9, z1 + 1, p.roofSlab());
        }
        b.fill(x0 + 2, ground + 10, z1, x1 - 2, ground + 10, z1 + 2, p.roofSlab());
        door(b, p, cx - 64, ground + 1, z1, Direction.SOUTH);
        sign(b, p, cx - 64, ground + 7, z1, Direction.SOUTH, "TEATRO");
        sign(b, p, cx - 76, ground + 6, z1, Direction.SOUTH, "TONIGHT");
        sign(b, p, cx - 52, ground + 6, z1, Direction.SOUTH, "THE DANCER");
        for (int x = x0 + 4; x <= x1 - 4; x += 8) {
            b.window(x, ground + 4, z0, 3, 1, true);
        }
        b.put(x0 + 1, ground + 1, z0 + 1, p.light());
        b.put(x1 - 1, ground + 1, z0 + 1, p.light());
    }

    private static void questBoard(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x = cx + 10;
        int z0 = cz + 52;
        int z1 = cz + 64;
        b.fill(x, ground + 1, z0, x, ground + 4, z0, p.accent());
        b.fill(x, ground + 1, z1, x, ground + 4, z1, p.accent());
        b.fill(x, ground + 2, z0 + 1, x, ground + 4, z1 - 1, p.foundation());
        b.fill(x - 1, ground + 5, z0 - 1, x + 1, ground + 5, z1 + 1, p.roofSlab());
        sign(b, p, x, ground + 4, z0 + 2, Direction.WEST, "CITY NOTICES");
        sign(b, p, x, ground + 4, z0 + 5, Direction.WEST, "MISSING DANCER");
        sign(b, p, x, ground + 4, z0 + 8, Direction.WEST, "SMUGGLED RELIC");
        b.put(x, ground + 3, z0 + 11, p.light());
        b.put(x, ground + 1, z0 - 1, p.light());
        signPost(b, x + 2, z1 + 2, ground, p, Direction.SOUTH, "QUESTS", "NOTICES");
    }

    // ------------------------------------------------------------------ Copacabana

    private static void copacabana(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        // The beach and the sea.
        for (int x = 266; x <= 304; x++) {
            for (int z = -232; z <= 180; z++) {
                b.put(cx + x, at(s, cx, cz, x, z), cz + z, SAND);
            }
        }
        int sea = at(s, cx, cz, 280, 0) - 1;
        for (int x = 305; x <= 338; x++) {
            for (int z = -240; z <= 318; z++) {
                b.fill(cx + x, sea - 2, cz + z, cx + x, sea - 1, cz + z, SAND);
                b.put(cx + x, sea, cz + z, WATER);
            }
        }
        // Hotels facing the promenade.
        hotel(b, cx + 160, cz - 70, cx + 200, cz - 40, at(s, cx, cz, 180, -55), p,
                "HOTEL", 18, Direction.EAST);
        hotel(b, cx + 160, cz + 20, cx + 200, cz + 56, at(s, cx, cz, 180, 38), p,
                "PENSAO", 12, Direction.EAST);
        // Beach cafes.
        int cafe1 = at(s, cx, cz, 231, -143);
        pad(b, cx + 223, cz - 151, cx + 239, cz - 135, cafe1, p);
        cafe(b, cx + 224, cz - 150, cx + 238, cz - 136, cafe1, p, "CAFE");
        int cafe2 = at(s, cx, cz, 231, -15);
        pad(b, cx + 223, cz - 21, cx + 239, cz - 9, cafe2, p);
        cafe(b, cx + 224, cz - 20, cx + 238, cz - 10, cafe2, p, "BAR");
        int cafe3 = at(s, cx, cz, 231, 87);
        pad(b, cx + 223, cz + 79, cx + 239, cz + 95, cafe3, p);
        cafe(b, cx + 224, cz + 80, cx + 238, cz + 94, cafe3, p, "CAFE");
        // Palms down the promenade.
        for (int z = -228; z <= 156; z += 24) {
            palm(b, cx + 241, cz + z, at(s, cx, cz, 241, z));
            palm(b, cx + 264, cz + z, at(s, cx, cz, 264, z));
        }
        // Umbrellas and a lifeguard hut on the sand.
        int[] umbrellas = {-180, -100, -20, 60, 120};
        for (int i = 0; i < umbrellas.length; i++) {
            int z = umbrellas[i];
            int y = at(s, cx, cz, 282, z);
            umbrella(b, cx + 282, cz + z, y, p,
                    i % 2 == 0 ? Materials.wool(DyeColor.RED)
                            : Materials.wool(DyeColor.BLUE));
        }
        int hut = at(s, cx, cz, 276, 30);
        b.fill(cx + 270, hut + 1, cz + 24, cx + 282, hut + 1, cz + 36, p.foundation());
        b.fill(cx + 272, hut + 2, cz + 26, cx + 280, hut + 5, cz + 34, p.wall());
        b.fill(cx + 273, hut + 2, cz + 27, cx + 279, hut + 4, cz + 33, AIR);
        b.fill(cx + 271, hut + 6, cz + 25, cx + 281, hut + 6, cz + 35, p.roofSlab());
        sign(b, p, cx + 276, hut + 5, cz + 24, Direction.NORTH, "LIFEGUARD");
        b.put(cx + 272, hut + 2, cz + 25, p.light());
        signPost(b, cx + 238, cz + 160, at(s, cx, cz, 238, 160), p, Direction.SOUTH,
                "COPACABANA", "PRAIA");
    }

    private static void hotel(StructureBuilder b, int x0, int z0, int x1, int z1, int base,
                              Palette p, String label, int height, Direction facing) {
        b.room(x0, base, z0, x1, base + height, z1);
        b.fill(x0, base, z0, x1, base, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, base + 1, base + height - 1, p);
        b.fill(x0 - 1, base + height, z0 - 1, x1 + 1, base + height, z1 + 1, p.roofSlab());
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, base + height + 1);
        // The label band and balconies on the seaward face.
        int faceX = facing == Direction.EAST ? x1 : x0;
        for (int y = base + 3; y <= base + height - 2; y += 4) {
            for (int z = z0 + 3; z <= z1 - 3; z += 6) {
                b.window(faceX, y, z, 2, 1, true);
                b.put(faceX + (facing == Direction.EAST ? 1 : -1), y - 1, z, p.roofSlab());
            }
        }
        for (int x = x0 + 3; x <= x1 - 3; x += 6) {
            b.window(x, base + 5, z1, 2, 2, true);
            b.window(x, base + 5, z0, 2, 2, true);
        }
        int mz = (z0 + z1) / 2;
        door(b, p, faceX, base + 1, mz, facing);
        sign(b, p, faceX, base + 7, mz, facing, label);
        sign(b, p, faceX, base + 7, mz + 4, facing, "ROOMS");
        b.put(x0 + 1, base + 1, z0 + 1, p.light());
        b.put(x1 - 1, base + 1, z0 + 1, p.light());
        b.put(x0 + 1, base + 1, z1 - 1, BARREL);
        b.put(x1 - 1, base + 1, z1 - 1, BARREL);
    }

    // ------------------------------------------------------------------ Favela

    private static final int[] FAVELA_X = {-308, -288, -268, -248, -228, -208, -188, -168};
    private static final int[] FAVELA_Z = {-236, -196, -156, -116, -36, 44, 84, 164};

    private static void favela(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        danceHall(b, cx, cz, s, p);
        herbalist(b, cx, cz, s, p);
        for (int i = 0; i < FAVELA_X.length; i++) {
            for (int j = 0; j < FAVELA_Z.length; j++) {
                int x0 = cx + FAVELA_X[i];
                int z0 = cz + FAVELA_Z[j];
                int x1 = x0 + 11;
                int z1 = z0 + 13;
                if (favelaSkipped(i, j)) {
                    continue;
                }
                if (overlaps(x0, z0, x1, z1, cx - 250, cz + 6, cx - 232, cz + 22)
                        || overlaps(x0, z0, x1, z1, cx - 250, cz + 98, cx - 232, cz + 114)
                        || overlaps(x0, z0, x1, z1, cx - 280, cz - 190, cx - 272, cz - 172)
                        || overlaps(x0, z0, x1, z1, cx - 242, cz - 50, cx - 230, cz - 32)
                        || overlaps(x0, z0, x1, z1, cx - 202, cz + 58, cx - 190, cz + 76)
                        || overlaps(x0, z0, x1, z1, cx - 302, cz + 84, cx - 264, cz + 118)) {
                    continue;
                }
                int base = at(s, cx, cz, FAVELA_X[i] + 6, FAVELA_Z[j] + 7);
                favelaHouse(b, cx, cz, x0, z0, base, p, i * FAVELA_Z.length + j);
            }
        }
        // Three flights of favela stairs linking the lanes.
        favelaStairs(b, cx - 276, cz - 186, at(s, cx, cz, -272, -186), p);
        favelaStairs(b, cx - 236, cz - 46, at(s, cx, cz, -232, -46), p);
        favelaStairs(b, cx - 196, cz + 62, at(s, cx, cz, -192, 62), p);
        // A community court with an oven and a water tank.
        int court = at(s, cx, cz, -282, 96);
        pad(b, cx - 300, cz + 86, cx - 266, cz + 116, court, p);
        pave(b, cx - 300, cz + 86, cx - 266, cz + 116, court, p.ground());
        b.fill(cx - 292, court + 1, cz + 96, cx - 288, court + 3, cz + 100,
                Materials.concrete(DyeColor.RED));
        b.put(cx - 290, court + 4, cz + 98, p.light());
        b.fill(cx - 276, court + 1, cz + 108, cx - 274, court + 3, cz + 110, p.foundation());
        b.put(cx - 275, court + 4, cz + 109, CAULDRON);
        signPost(b, cx - 300, cz + 84, at(s, cx, cz, -300, 84), p, Direction.SOUTH,
                "FAVELA", "MIND THE STEPS");
        signPost(b, cx - 160, cz + 8, at(s, cx, cz, -160, 8), p, Direction.NORTH, "FAVELA");
    }

    /** Authored gaps in the favela grid: gardens, a ruin, a mural wall, the stairs. */
    private static boolean favelaSkipped(int i, int j) {
        int[] skipped = {
                0 * 8 + 3, 0 * 8 + 6, 1 * 8 + 1, 1 * 8 + 5, 2 * 8 + 0, 2 * 8 + 4,
                2 * 8 + 7, 3 * 8 + 2, 3 * 8 + 6, 4 * 8 + 1, 4 * 8 + 5, 4 * 8 + 7,
                5 * 8 + 0, 5 * 8 + 3, 5 * 8 + 6, 6 * 8 + 2, 6 * 8 + 5, 7 * 8 + 1,
                7 * 8 + 4, 7 * 8 + 7};
        int key = i * 8 + j;
        for (int k : skipped) {
            if (k == key) {
                return true;
            }
        }
        return false;
    }

    private static void favelaHouse(StructureBuilder b, int cx, int cz, int x0, int z0,
                                    int base, Palette p, int index) {
        int x1 = x0 + 11;
        int z1 = z0 + 13;
        int height = 5 + Math.floorMod(index, 3) * 2;
        BlockState wall = Math.floorMod(index, 4) == 0 ? LIGHT : paint(index);
        // Stilts where the slope falls away under the corners.
        int[][] posts = {{x0, z0}, {x1, z0}, {x0, z1}, {x1, z1},
                {x0 + 6, z0}, {x0 + 6, z1}, {x0, z0 + 7}, {x1, z0 + 7}};
        for (int[] post : posts) {
            int g = b.groundY(post[0], post[1]);
            if (g < base) {
                b.fill(post[0], g + 1, post[1], post[0], base - 1, post[1], p.accent());
            }
        }
        b.fill(x0, base, z0, x1, base, z1, p.foundation());
        b.room(x0, base, z0, x1, base + height, z1);
        b.walls(x0, base + 1, z0, x1, base + height - 1, z1, wall);
        b.fill(x0, base, z0, x1, base, z1, p.foundation());
        // Flat roof with a parapet and a water tank on some houses.
        b.fill(x0 - 1, base + height, z0 - 1, x1 + 1, base + height, z1 + 1, p.roofSlab());
        for (int x = x0 - 1; x <= x1 + 1; x += 2) {
            b.put(x, base + height + 1, z0 - 1, p.roof());
            b.put(x, base + height + 1, z1 + 1, p.roof());
        }
        for (int z = z0 - 1; z <= z1 + 1; z += 2) {
            b.put(x0 - 1, base + height + 1, z, p.roof());
            b.put(x1 + 1, base + height + 1, z, p.roof());
        }
        if (Math.floorMod(index, 3) == 0) {
            b.put(x0 + 3, base + height + 1, z0 + 3, CAULDRON);
            b.fill(x1 - 4, base + height + 1, z0 + 3, x1 - 4, base + height + 3, z0 + 3,
                    p.accent());
            b.put(x1 - 4, base + height + 4, z0 + 3, p.light());
        }
        // Door and windows.
        int mx = (x0 + x1) / 2;
        door(b, p, mx, base + 1, z1, Direction.SOUTH);
        b.window(x0 + 2, base + 3, z1, 2, 2, true);
        b.window(x1 - 3, base + 3, z1, 2, 2, true);
        b.window(x0, base + 3, z0 + 4, 2, 1, true);
        b.window(x1, base + 3, z0 + 4, 2, 1, true);
        // A mural on every fourth house.
        if (Math.floorMod(index, 4) == 1) {
            for (int mz = z0 + 2; mz <= z0 + 8; mz++) {
                for (int my = base + 2; my <= base + 4; my++) {
                    b.put(x1, my, mz, paint(mz + my + index));
                }
            }
        }
        b.put(x0 + 1, base + 1, z0 + 1, BARREL);
        b.put(x1 - 1, base + 1, z0 + 1, p.light());
    }

    private static void favelaStairs(StructureBuilder b, int x, int z, int base, Palette p) {
        // A short concrete flight up to the upper lane, with a landing and a rail.
        for (int i = 0; i <= 8; i++) {
            int y = base + i;
            b.fill(x - 3, base, z + i, x + 3, y, z + i, p.foundation());
            b.put(x - 3, y, z + i, p.roofSlab());
            b.put(x + 3, y, z + i, p.roofSlab());
            b.fill(x - 2, y + 1, z + i, x + 2, y + 3, z + i, AIR);
        }
        b.fill(x - 3, base + 9, z + 9, x + 3, base + 9, z + 11, p.foundation());
        b.fill(x - 3, base + 10, z + 11, x + 3, base + 10, z + 11, p.rail());
        b.put(x - 3, base + 9, z + 2, p.light());
        b.put(x + 3, base + 9, z + 2, p.light());
    }

    private static void danceHall(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int x0 = cx - 248;
        int x1 = cx - 234;
        int z0 = cz + 8;
        int z1 = cz + 20;
        int base = at(s, cx, cz, -241, 14);
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, base, p);
        b.room(x0, base, z0, x1, base + 9, z1);
        b.fill(x0, base, z0, x1, base, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, base + 1, base + 9, p);
        b.fill(x0 - 1, base + 10, z0 - 1, x1 + 1, base + 10, z1 + 1, p.roofSlab());
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, base + 11);
        door(b, p, cx - 241, base + 1, z0, Direction.NORTH);
        sign(b, p, cx - 241, base + 6, z0, Direction.NORTH, "DANCE HALL");
        sign(b, p, cx - 246, base + 6, z0, Direction.NORTH, "SATURDAY");
        // A stage and coloured lights inside.
        b.fill(x0 + 4, base + 1, z1 - 5, x1 - 4, base + 2, z1 - 3, p.accent());
        for (int x = x0 + 6; x <= x1 - 6; x += 6) {
            b.put(x, base + 8, z1 - 2, p.light());
            b.put(x, base + 4, z0 + 2, BLUE);
        }
        for (int x = x0 + 3; x <= x1 - 3; x += 5) {
            b.window(x, base + 4, z1, 2, 1, true);
        }
        b.put(x0 + 1, base + 1, z1 - 1, BARREL);
        b.put(x1 - 1, base + 1, z1 - 1, BARREL);
    }

    private static void herbalist(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int x0 = cx - 248;
        int x1 = cx - 234;
        int z0 = cz + 100;
        int z1 = cz + 112;
        int base = at(s, cx, cz, -241, 106);
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, base, p);
        b.room(x0, base, z0, x1, base + 6, z1);
        b.fill(x0, base, z0, x1, base, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, base + 1, base + 6, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, base + 6, 3, 0);
        door(b, p, cx - 241, base + 1, z1, Direction.SOUTH);
        sign(b, p, cx - 241, base + 4, z1, Direction.SOUTH, "HERBALIST");
        sign(b, p, cx - 246, base + 4, z1, Direction.SOUTH, "REMEDIES");
        b.window(x0 + 3, base + 3, z1, 2, 2, true);
        b.window(x1 - 4, base + 3, z1, 2, 2, true);
        b.put(x0 + 1, base + 1, z0 + 1, CAULDRON);
        b.put(x1 - 1, base + 1, z0 + 1, BOOKS);
        // The herb beds outside.
        for (int i = 0; i < 3; i++) {
            int hx = cx - 247 + i * 5;
            int hz = cz + 99;
            b.fill(hx - 1, base + 1, hz, hx + 1, base + 1, hz, COARSE);
            b.put(hx, base + 2, hz, flower(i));
        }
    }

    // ------------------------------------------------------------------ Port

    private static void port(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        // Quay wall along the harbour edge.
        int sea = at(s, cx, cz, 280, 0) - 1;
        int quay = at(s, cx, cz, 300, 250);
        int wallBase = Math.min(sea - 2, quay - 3);
        b.fill(cx + 300, wallBase, cz + 184, cx + 304, quay + 1, cz + 318, p.foundation());
        b.fill(cx + 300, quay + 2, cz + 184, cx + 300, quay + 2, cz + 318, p.weathered());
        // The road gate through the quay wall.
        b.fill(cx + 300, quay, cz + 245, cx + 304, quay + 3, cz + 255, AIR);
        b.fill(cx + 300, quay, cz + 245, cx + 304, quay, cz + 255, p.foundation());
        // The pier and its bollards.
        int pier = Math.max(at(s, cx, cz, 314, 232), Math.max(sea + 1, quay + 1));
        b.fill(cx + 300, pier, cz + 228, cx + 332, pier, cz + 236, p.accent());
        for (int x = 304; x <= 330; x += 6) {
            b.fill(cx + x, pier - 3, cz + 228, cx + x, pier - 1, cz + 228, p.foundation());
            b.fill(cx + x, pier - 3, cz + 236, cx + x, pier - 1, cz + 236, p.foundation());
            b.put(cx + x, pier + 1, cz + 228, p.rail());
            b.put(cx + x, pier + 1, cz + 236, p.rail());
        }
        for (int z = 190; z <= 314; z += 12) {
            if (z >= 245 && z <= 255) {
                continue; // the road gate stays open
            }
            b.put(cx + 300, quay + 3, cz + z, p.rail());
        }
        // Warehouses and the customs house.
        warehouse(b, cx + 150, cz + 194, cx + 194, cz + 236, at(s, cx, cz, 172, 215), p,
                "ARMAZEM 1", 0);
        warehouse(b, cx + 200, cz + 194, cx + 224, cz + 236, at(s, cx, cz, 212, 215), p,
                "ARMAZEM 2", 1);
        warehouse(b, cx + 264, cz + 194, cx + 296, cz + 236, at(s, cx, cz, 280, 215), p,
                "ALFANDEGA", 2);
        warehouse(b, cx + 150, cz + 264, cx + 194, cz + 300, at(s, cx, cz, 172, 282), p,
                "ARMAZEM 3", 1);
        warehouse(b, cx + 258, cz + 264, cx + 292, cz + 300, at(s, cx, cz, 275, 282), p,
                "ARMAZEM 4", 0);
        customs(b, cx + 226, cz + 210, cx + 252, cz + 240, at(s, cx, cz, 239, 225), p);
        // Market and the quay cranes.
        market(b, cx + 200, cz + 264, cx + 240, cz + 300, at(s, cx, cz, 220, 282), p);
        crane(b, cx + 298, cz + 200, at(s, cx, cz, 298, 200), p);
        crane(b, cx + 298, cz + 260, at(s, cx, cz, 298, 260), p);
        crane(b, cx + 298, cz + 310, at(s, cx, cz, 298, 310), p);
        // Loose cargo along the road.
        for (int i = 0; i < 8; i++) {
            int x = cx + 148 + i * 20;
            int z = cz + 242;
            crate(b, x, z, at(s, cx, cz, 148 + i * 20, 242), p, 1 + i % 2);
        }
        signPost(b, cx + 140, cz + 244, at(s, cx, cz, 140, 244), p, Direction.SOUTH,
                "PORT", "ALFANDEGA");
        signPost(b, cx + 296, cz + 240, at(s, cx, cz, 296, 240), p, Direction.SOUTH,
                "PIER", "NO LOITERING");
        signPost(b, cx + 200, cz + 240, at(s, cx, cz, 200, 240), p, Direction.SOUTH,
                "WAREHOUSES");
        station(b, cx, cz, s, p);
        railYard(b, cx, cz, s, p);
    }

    private static void warehouse(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  int base, Palette p, String label, int variant) {
        plinth(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, base, p);
        b.room(x0, base, z0, x1, base + 10, z1);
        b.fill(x0, base, z0, x1, base, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, base + 1, base + 10, p);
        int ridge = x1 - x0 >= z1 - z0 ? 0 : 1;
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, base + 10, 5, ridge);
        int dx = variant % 2 == 0 ? (x0 + x1) / 2 : x1;
        int dz = variant % 2 == 0 ? z1 : (z0 + z1) / 2;
        Direction facing = variant % 2 == 0 ? Direction.SOUTH : Direction.EAST;
        door(b, p, dx, base + 1, dz, facing);
        sign(b, p, dx, base + 6, dz, facing, label);
        for (int x = x0 + 4; x <= x1 - 4; x += 6) {
            b.window(x, base + 7, z0, 2, 1, true);
            b.window(x, base + 7, z1, 2, 1, true);
        }
        b.put(x0 + 2, base + 1, z0 + 2, BARREL);
        b.put(x0 + 4, base + 1, z0 + 2, BARREL);
        b.put(x1 - 2, base + 1, z1 - 2, p.foundation());
        b.put(x0 + 2, base + 1, z1 - 2, HAY);
        b.put(x1 - 2, base + 1, z0 + 2, p.light());
    }

    private static void customs(StructureBuilder b, int x0, int z0, int x1, int z1,
                                int base, Palette p) {
        plinth(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, base, p);
        b.room(x0, base, z0, x1, base + 8, z1);
        b.fill(x0, base, z0, x1, base, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, base + 1, base + 8, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, base + 8, 4, 1);
        door(b, p, (x0 + x1) / 2, base + 1, z1, Direction.SOUTH);
        sign(b, p, (x0 + x1) / 2, base + 5, z1, Direction.SOUTH, "ALFANDEGA");
        sign(b, p, (x0 + x1) / 2 + 6, base + 5, z1, Direction.SOUTH, "CUSTOMS");
        b.window(x0 + 3, base + 4, z1, 3, 2, true);
        b.window(x1 - 4, base + 4, z1, 3, 2, true);
        b.put(x0 + 2, base + 1, z0 + 2, BARREL);
        b.put(x1 - 2, base + 1, z0 + 2, BOOKS);
        b.put(x0 + 2, base + 1, z1 - 2, p.light());
    }

    private static void market(StructureBuilder b, int x0, int z0, int x1, int z1,
                               int base, Palette p) {
        plinth(b, x0, z0, x1, z1, base, p);
        pave(b, x0, z0, x1, z1, base, p.ground());
        outline(b, x0, z0, x1, z1, base, p.foundation());
        for (int i = 0; i < 3; i++) {
            int sx = x0 + 4 + i * 12;
            int sz = z0 + 4;
            b.fill(sx, base + 1, sz, sx, base + 2, sz, p.accent());
            b.fill(sx + 6, base + 1, sz, sx + 6, base + 2, sz, p.accent());
            b.fill(sx, base + 1, sz + 6, sx, base + 2, sz + 6, p.accent());
            b.fill(sx + 6, base + 1, sz + 6, sx + 6, base + 2, sz + 6, p.accent());
            b.fill(sx, base + 3, sz - 1, sx + 6, base + 3, sz + 7,
                    i % 2 == 0 ? Materials.wool(DyeColor.RED)
                            : Materials.wool(DyeColor.YELLOW));
            b.fill(sx + 1, base + 1, sz + 3, sx + 5, base + 1, sz + 3, p.foundation());
            b.put(sx + 2, base + 2, sz + 3, BARREL);
            b.put(sx + 4, base + 2, sz + 3, HAY);
        }
        signPost(b, (x0 + x1) / 2, z1 + 2, base, p, Direction.SOUTH, "MERCADO", "PEIXE");
        b.put(x0 + 1, base + 1, z1 - 1, p.light());
        b.put(x1 - 1, base + 1, z1 - 1, p.light());
    }

    private static void crane(StructureBuilder b, int x, int z, int base, Palette p) {
        b.fill(x - 1, base, z - 1, x + 1, base, z + 1, p.foundation());
        b.fill(x, base + 1, z, x, base + 10, z, p.accent());
        b.fill(x, base + 9, z, x + 8, base + 9, z, p.accent());
        b.put(x + 4, base + 8, z, p.accent());
        b.fill(x + 8, base + 5, z, x + 8, base + 8, z, p.rail());
        b.put(x + 8, base + 4, z, p.light());
        b.put(x, base + 11, z, p.light());
    }

    private static void station(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int railY = at(s, cx, cz, 120, 330);
        int base = railY + 1;
        int x0 = cx + 104;
        int x1 = cx + 140;
        int z0 = cz + 312;
        int z1 = cz + 326;
        plinth(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, base, p);
        b.room(x0, base, z0, x1, base + 8, z1);
        b.fill(x0, base, z0, x1, base, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, base + 1, base + 8, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, base + 8, 5, 0);
        door(b, p, (x0 + x1) / 2, base + 1, z0, Direction.NORTH);
        door(b, p, (x0 + x1) / 2, base + 1, z1, Direction.SOUTH);
        sign(b, p, (x0 + x1) / 2, base + 5, z0, Direction.NORTH, "ESTACAO");
        sign(b, p, (x0 + x1) / 2 + 8, base + 5, z0, Direction.NORTH, "RAIL");
        for (int x = x0 + 4; x <= x1 - 4; x += 8) {
            b.window(x, base + 4, z1, 3, 1, true);
        }
        b.put(x0 + 2, base + 1, z0 + 2, BARREL);
        b.put(x1 - 2, base + 1, z0 + 2, BARREL);
        // Platform and its canopy, north of the track.
        plinth(b, x0 - 4, z1 + 1, x1 + 6, z1 + 3, base, p);
        b.fill(x0 - 4, base, z1 + 1, x1 + 6, base, z1 + 3, p.foundation());
        for (int x = x0 - 2; x <= x1 + 4; x += 8) {
            b.fill(x, base + 1, z1 + 2, x, base + 5, z1 + 2, p.accent());
            b.put(x, base + 6, z1 + 2, p.light());
        }
        b.fill(x0 - 4, base + 6, z1 + 1, x1 + 6, base + 6, z1 + 3, p.roofSlab());
        signPost(b, x0 - 6, z1 + 1, base, p, Direction.NORTH, "TICKETS");
    }

    private static void railYard(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int railY = at(s, cx, cz, 120, 330);
        // The straight, level east-west approach across the whole southern edge.
        for (int x = -336; x <= 336; x++) {
            int ax = cx + x;
            int surface = b.groundY(ax, cz + 330);
            if (surface > railY) {
                b.fill(ax, railY + 1, cz + 330, ax, surface, cz + 330, AIR);
            } else if (surface < railY) {
                b.fill(ax, surface + 1, cz + 330, ax, railY, cz + 330, p.foundation());
            }
            b.put(ax, railY, cz + 330, p.rubble());
            b.put(ax, railY + 1, cz + 330, RAIL_EW);
        }
        // A level crossing where the main avenue meets the track.
        for (int x = -8; x <= 8; x++) {
            for (int z = 328; z <= 332; z++) {
                b.put(cx + x, railY + 1, cz + z, p.foundation());
            }
        }
        // A siding and a coal stack at the port end.
        for (int x = 160; x <= 296; x++) {
            b.put(cx + x, railY, cz + 338, p.rubble());
            b.put(cx + x, railY + 1, cz + 338, RAIL_EW);
        }
        for (int i = 0; i < 6; i++) {
            int x = cx + 170 + i * 14;
            b.fill(x - 2, railY + 1, cz + 334, x + 2, railY + 2, cz + 336,
                    Blocks.COAL_BLOCK.defaultBlockState());
        }
        b.fill(cx + 156, railY + 1, cz + 336, cx + 158, railY + 3, cz + 338, p.accent());
        b.put(cx + 157, railY + 4, cz + 337, p.light());
    }

    // ------------------------------------------------------------------ Botanical Garden

    private static void botanicalGarden(StructureBuilder b, int cx, int cz, CitySurface s,
                                        Palette p) {
        // Parterre beds either side of the main walk.
        parterre(b, cx - 310, cz + 186, cx - 252, cz + 198, at(s, cx, cz, -280, 192), p, 0);
        parterre(b, cx - 230, cz + 186, cx - 150, cz + 198, at(s, cx, cz, -190, 192), p, 1);
        parterre(b, cx - 310, cz + 252, cx - 302, cz + 292, at(s, cx, cz, -306, 272), p, 2);
        // The pond.
        int pond = at(s, cx, cz, -288, 264);
        pad(b, cx - 301, cz + 251, cx - 275, cz + 277, pond, p);
        b.fill(cx - 300, pond, cz + 252, cx - 276, pond, cz + 276, AIR);
        b.fill(cx - 300, pond - 1, cz + 252, cx - 276, pond - 1, cz + 276, WATER);
        outline(b, cx - 301, cz + 251, cx - 275, cz + 277, pond, p.foundation());
        for (int i = 0; i < 6; i++) {
            b.put(cx - 296 + i * 4, pond, cz + 258 + i % 3 * 5, LILY);
        }
        // Palms along the walks.
        for (int z = 190; z <= 300; z += 18) {
            palm(b, cx - 312, cz + z, at(s, cx, cz, -312, z));
            palm(b, cx - 150, cz + z, at(s, cx, cz, -150, z));
        }
        for (int x = -226; x <= -158; x += 16) {
            palm(b, cx + x, cz + 300, at(s, cx, cz, x, 300));
        }
        greenhouse(b, cx - 204, cz + 252, cx - 164, cz + 280, at(s, cx, cz, -184, 266), p);
        hillsideCave(b, cx, cz, s, p);
        signPost(b, cx - 236, cz + 182, at(s, cx, cz, -236, 182), p, Direction.SOUTH,
                "BOTANICAL", "GARDEN");
        signPost(b, cx - 244, cz + 236, at(s, cx, cz, -244, 236), p, Direction.SOUTH,
                "HILLSIDE CAVE");
    }

    private static void parterre(StructureBuilder b, int x0, int z0, int x1, int z1,
                                 int base, Palette p, int variant) {
        pad(b, x0, z0, x1, z1, base, p);
        pave(b, x0, z0, x1, z1, base, COARSE);
        outline(b, x0, z0, x1, z1, base, p.foundation());
        for (int x = x0 + 2; x <= x1 - 2; x += 3) {
            for (int z = z0 + 2; z <= z1 - 2; z += 3) {
                b.put(x, base + 1, z, flower(x + z + variant));
                if (Math.floorMod(x + z, 5) == 0) {
                    b.put(x, base + 1, z, JUNGLE_LEAF);
                }
            }
        }
        b.put(x0 + 1, base + 1, z0 + 1, p.light());
    }

    private static void greenhouse(StructureBuilder b, int x0, int z0, int x1, int z1,
                                   int base, Palette p) {
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, base, p);
        b.room(x0, base, z0, x1, base + 7, z1);
        b.fill(x0, base, z0, x1, base, z1, p.foundation());
        b.walls(x0, base + 1, z0, x1, base + 6, z1, GLASS);
        b.fill(x0 + 1, base + 1, z0 + 1, x1 - 1, base + 6, z1 - 1, AIR);
        // The white frame posts and the glass gable.
        for (int x = x0; x <= x1; x += 8) {
            b.fill(x, base + 1, z0, x, base + 6, z0, p.frame());
            b.fill(x, base + 1, z1, x, base + 6, z1, p.frame());
        }
        for (int z = z0; z <= z1; z += 7) {
            b.fill(x0, base + 1, z, x0, base + 6, z, p.frame());
            b.fill(x1, base + 1, z, x1, base + 6, z, p.frame());
        }
        for (int i = 0; i < 4; i++) {
            b.fill(x0 + i, base + 7 + i, z0 + i, x1 - i, base + 7 + i, z1 - i, GLASS);
        }
        b.fill(x0 + 4, base + 11, z0 + 4, x1 - 4, base + 11, z1 - 4, p.roofSlab());
        int mx = (x0 + x1) / 2;
        door(b, p, mx, base + 1, z1, Direction.SOUTH);
        sign(b, p, mx, base + 5, z1, Direction.SOUTH, "GREENHOUSE");
        // Beds and a fountain inside.
        for (int x = x0 + 3; x <= x1 - 3; x += 6) {
            b.fill(x - 1, base + 1, z0 + 3, x + 1, base + 1, z0 + 6, COARSE);
            b.put(x, base + 2, z0 + 4, JUNGLE_LEAF);
            b.put(x, base + 2, z0 + 5, flower(x));
        }
        b.fill(mx - 2, base + 1, z1 - 8, mx + 2, base + 1, z1 - 4, p.foundation());
        b.put(mx, base + 1, z1 - 6, WATER);
        b.put(mx, base + 2, z1 - 6, LILY);
        b.put(x0 + 1, base + 1, z1 - 1, CAULDRON);
        b.put(x1 - 1, base + 1, z1 - 1, CAULDRON);
    }

    private static void hillsideCave(StructureBuilder b, int cx, int cz, CitySurface s,
                                     Palette p) {
        int base = at(s, cx, cz, -240, 222);
        int hx = cx - 270;
        int hz = cz + 222;
        // The authored rock mound, solid under its grass cap.
        for (int dx = -26; dx <= 26; dx++) {
            for (int dz = -20; dz <= 20; dz++) {
                if (!inEllipse(dx, dz, 26, 20)) {
                    continue;
                }
                int x = hx + dx;
                int z = hz + dz;
                int top = base + 8 - Math.abs(dx) / 6 - Math.abs(dz) / 5;
                b.fill(x, base, z, x, top, z, MOSSY);
                b.put(x, top, z, MOSS);
                if (Math.floorMod(dx * 3 + dz * 5, 7) == 0) {
                    b.put(x, top + 1, z, JUNGLE_LEAF);
                }
            }
        }
        // The chamber and its mouth onto the garden walk.
        int floor = base;
        b.fill(cx - 278, floor, cz + 214, cx - 256, floor + 3, cz + 230, AIR);
        b.fill(cx - 256, floor, cz + 220, cx - 238, floor + 2, cz + 224, AIR);
        b.fill(cx - 278, floor - 1, cz + 214, cx - 256, floor - 1, cz + 230, STONE);
        b.fill(cx - 256, floor - 1, cz + 220, cx - 238, floor - 1, cz + 224, p.foundation());
        // A stone portal frame at the mouth.
        b.fill(cx - 238, floor, cz + 219, cx - 238, floor + 3, cz + 219, p.foundation());
        b.fill(cx - 238, floor, cz + 225, cx - 238, floor + 3, cz + 225, p.foundation());
        b.fill(cx - 238, floor + 3, cz + 219, cx - 238, floor + 3, cz + 225, p.foundation());
        b.put(cx - 238, floor + 3, cz + 222, p.light());
        signPost(b, cx - 234, cz + 218, at(s, cx, cz, -234, 218), p, Direction.SOUTH,
                "CAVE", "NO ENTRY");
        // Chests, lore and a little light.
        b.chest(cx - 274, floor, cz + 218, SiteLoot.CULT);
        b.chest(cx - 260, floor, cz + 228, SiteLoot.RELIC);
        b.chest(cx - 268, floor, cz + 216, SiteLoot.COMMON);
        sign(b, p, cx - 277, floor + 1, cz + 222, Direction.EAST,
                "SHE DANCED", "HERE LAST", "NIGHT");
        sign(b, p, cx - 257, floor + 1, cz + 226, Direction.WEST,
                "THE RELIC", "CAME ASHORE", "AT THE PIER");
        sign(b, p, cx - 266, floor + 1, cz + 229, Direction.NORTH,
                "DO NOT", "FOLLOW THE", "LANTERN");
        b.put(cx - 272, floor + 1, cz + 218, p.light());
        b.put(cx - 262, floor + 1, cz + 226, p.light());
        b.put(cx - 276, floor + 1, cz + 228, Palette.cobweb());
        b.put(cx - 258, floor + 1, cz + 216, Palette.cobweb());
        b.put(cx - 266, floor + 1, cz + 228, Palette.cobweb());
        b.put(cx - 260, floor + 1, cz + 218, Palette.cobweb());
    }
}
