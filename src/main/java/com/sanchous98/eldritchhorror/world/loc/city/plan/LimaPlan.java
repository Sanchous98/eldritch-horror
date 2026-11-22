package com.sanchous98.eldritchhorror.world.loc.city.plan;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.SiteLoot;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
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
 * Lima between the wars: the colonial cathedral and the Plaza Mayor, the port of Callao with its
 * quays, warehouses, customs and rail yard, the adobe barrios, the pre-Columbian huaca and its
 * museum, and the coastal tomb under the cliffs. Every block is placed deliberately; there is no
 * layout RNG and nothing reads the generating chunk.
 *
 * <p>The whole city is written on every call, as the plan contract requires: the builder keeps only
 * the slice that falls inside the generating chunk, so every layout decision is absolute and
 * chunk-independent. The shared pass keeps a flat authored heart at {@code ground}; lots in the
 * heart use it directly and the coastal outskirts use {@link CitySurface#at(int, int)} or
 * {@code b.groundY} exactly as the contract allows.
 */
public final class LimaPlan implements CityPlan {

    /** Authored half-extent; everything below stays inside +/-340. */
    private static final int HALF_EXTENT = 340;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState RAIL = Blocks.RAIL.defaultBlockState()
            .setValue(BlockStateProperties.RAIL_SHAPE, RailShape.EAST_WEST);
    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState STONE_BRICKS = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState MOSSY_STONE = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
    private static final BlockState SANDSTONE = Blocks.SANDSTONE.defaultBlockState();
    private static final BlockState CUT_SANDSTONE = Blocks.CUT_SANDSTONE.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState BARREL = Blocks.BARREL.defaultBlockState();
    private static final BlockState BOOKSHELF = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState CAULDRON = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState JUNGLE_LOG = Blocks.JUNGLE_LOG.defaultBlockState();
    private static final BlockState JUNGLE_LEAVES = Blocks.JUNGLE_LEAVES.defaultBlockState();

    @Override
    public String cityId() {
        return "lima";
    }

    @Override
    public int halfExtent() {
        return HALF_EXTENT;
    }

    @Override
    public void build(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Legacy entry point: use a conservative graded rim so older callers still get a
        // terrain-following city rather than a flat plate.
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
        plazaMayor(b, cx, cz, s, p);
        cathedral(b, cx, cz, s, p);
        palace(b, cx, cz, s, p);
        arcade(b, cx + 76, cz - 160, cx + 92, cz - 80,
                s.at(cx + 84, cz - 120), Direction.WEST, p, "ARCOS");
        arcade(b, cx - 64, cz - 64, cx - 12, cz - 52,
                s.at(cx - 38, cz - 58), Direction.NORTH, p, "ARCOS");
        arcade(b, cx + 12, cz - 64, cx + 64, cz - 52,
                s.at(cx + 38, cz - 58), Direction.NORTH, p, "ARCOS");
        center(b, cx, cz, s, p);
        barrios(b, cx, cz, s, p);
        ruins(b, cx, cz, s, p);
        int quay = s.at(cx + 240, cz);
        port(b, cx, cz, s, quay, p);
        coast(b, cx, cz, s, quay, p);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.LANDMARK, 0, 0, 320, "gate"),
                new CityPoi("gate_signpost", CityPoi.Kind.SIGN, -10, 3, 328,
                        "CATHEDRAL|PORT|BARRIOS|RUINS"),
                new CityPoi("city_notices", CityPoi.Kind.SIGN, -20, 4, 334, "CITY NOTICES"),
                new CityPoi("plaza_quest_board", CityPoi.Kind.QUEST_BOARD, 0, 0, -120,
                        "lima_missing_archaeologist,lima_stolen_quipu"),
                new CityPoi("plaza_board_sign", CityPoi.Kind.SIGN, 0, 4, -117,
                        "QUEST NOTICES|MISSING MAN|STOLEN QUIPU"),
                new CityPoi("cathedral", CityPoi.Kind.LANDMARK, 0, 0, -200,
                        "twin_towers,nave,dome,facade"),
                new CityPoi("cathedral_sign", CityPoi.Kind.SIGN, 0, 12, -177, "CATHEDRAL"),
                new CityPoi("plaza_mayor", CityPoi.Kind.LANDMARK, 0, 0, -140,
                        "fountain,arcades,palace"),
                new CityPoi("palace", CityPoi.Kind.LANDMARK, -92, 0, -130, "palace,balcony"),
                new CityPoi("arcades", CityPoi.Kind.LANDMARK, 84, 0, -120, "arcades,shops"),
                new CityPoi("hotel", CityPoi.Kind.INN, -80, 0, -60, "hotel_clerk"),
                new CityPoi("hotel_clerk", CityPoi.Kind.NPC, -80, 0, -52, "hotel_clerk"),
                new CityPoi("barrio_shop", CityPoi.Kind.SHOP, -240, 0, -60, "merchant"),
                new CityPoi("shopkeeper", CityPoi.Kind.NPC, -228, 0, -60, "shopkeeper"),
                new CityPoi("curandero", CityPoi.Kind.HEALER, -240, 0, 120, "remedies"),
                new CityPoi("curandero_npc", CityPoi.Kind.NPC, -230, 0, 120, "curandero"),
                new CityPoi("barrio_church", CityPoi.Kind.LANDMARK, -280, 0, 36, "church,bells"),
                new CityPoi("barrio_market", CityPoi.Kind.LANDMARK, -184, 0, -152,
                        "market,stalls"),
                new CityPoi("ruins", CityPoi.Kind.LANDMARK, 0, 0, 60, "pyramid,walls"),
                new CityPoi("ruins_warning", CityPoi.Kind.SIGN, 0, 3, 102,
                        "SACRED GROUND|DO NOT DIG"),
                new CityPoi("museum", CityPoi.Kind.LANDMARK, 0, 0, 124, "museum,quipu"),
                new CityPoi("museum_curator", CityPoi.Kind.NPC, 0, 0, 120,
                        "lima_missing_archaeologist"),
                new CityPoi("museum_sign", CityPoi.Kind.SIGN, 0, 5, 136, "MUSEO|DE SITIO"),
                new CityPoi("guide", CityPoi.Kind.NPC, 10, 0, 144,
                        "guide,lima_missing_archaeologist"),
                new CityPoi("port", CityPoi.Kind.LANDMARK, 240, 0, -60,
                        "docks,warehouses,cranes"),
                new CityPoi("port_customs", CityPoi.Kind.QUEST_BOARD, 240, 0, 0,
                        "lima_stolen_quipu"),
                new CityPoi("customs_officer", CityPoi.Kind.NPC, 236, 0, 0,
                        "lima_stolen_quipu"),
                new CityPoi("customs_sign", CityPoi.Kind.SIGN, 240, 5, -12,
                        "STOLEN QUIPU|SEE CUSTOMS"),
                new CityPoi("fish_market", CityPoi.Kind.LANDMARK, 168, 0, 8, "fish,stalls"),
                new CityPoi("fisherman", CityPoi.Kind.NPC, 284, 0, 60, "fisherman"),
                new CityPoi("harbour_port", CityPoi.Kind.PORT, 300, 0, 0,
                        "callao;deep_water;quay"),
                new CityPoi("rail_station", CityPoi.Kind.STATION, 230, 1, -210, "east_west"),
                new CityPoi("rail_sign", CityPoi.Kind.SIGN, 230, 6, -214,
                        "RAIL STATION|CALLAO"),
                new CityPoi("coastal_tomb", CityPoi.Kind.SECRET, 240, -3, 260,
                        "chests,lore,tomb"),
                new CityPoi("tomb_sign", CityPoi.Kind.SIGN, 236, 3, 244, "TOMB|KEEP OUT"),
                new CityPoi("cliff_warning", CityPoi.Kind.SIGN, 282, 9, 240,
                        "CLIFF EDGE|KEEP BACK"),
                new CityPoi("barrio_sign", CityPoi.Kind.SIGN, -140, 3, 0,
                        "BARRIOS|ADOBE LANES"),
                new CityPoi("port_sign", CityPoi.Kind.SIGN, 140, 3, -20, "PORT|CALLAO"),
                new CityPoi("center_sign", CityPoi.Kind.SIGN, -10, 3, 160,
                        "CENTER|PLAZA MAYOR"));
    }

    // ------------------------------------------------------------------ shared helpers

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
        int height = 6 + Math.floorMod(x * 3 + z * 5, 3);
        for (int y = ground + 1; y <= ground + height; y++) {
            b.put(x, y, z, JUNGLE_LOG);
        }
        for (int d = -2; d <= 2; d++) {
            b.put(x + d, ground + height, z, JUNGLE_LEAVES);
            b.put(x, ground + height, z + d, JUNGLE_LEAVES);
        }
        b.put(x - 1, ground + height + 1, z, JUNGLE_LEAVES);
        b.put(x + 1, ground + height + 1, z, JUNGLE_LEAVES);
        b.put(x, ground + height + 1, z - 1, JUNGLE_LEAVES);
        b.put(x, ground + height + 1, z + 1, JUNGLE_LEAVES);
    }

    private static void stall(StructureBuilder b, int x, int z, int ground, Palette p,
                              DyeColor colour) {
        for (int dx = 0; dx <= 4; dx += 4) {
            for (int dz = 0; dz <= 4; dz += 4) {
                b.put(x + dx, ground + 1, z + dz, p.accent());
                b.put(x + dx, ground + 2, z + dz, p.accent());
            }
        }
        b.fill(x, ground + 3, z, x + 4, ground + 3, z + 4, Materials.wool(colour));
        b.fill(x + 1, ground + 1, z + 2, x + 3, ground + 1, z + 2, p.foundation());
        b.put(x + 2, ground + 2, z + 2, BARREL);
        b.put(x, ground + 1, z + 2, p.light());
    }

    private static Side side(Direction facing) {
        return switch (facing) {
            case NORTH -> Side.N;
            case SOUTH -> Side.S;
            case WEST -> Side.W;
            case EAST -> Side.E;
            default -> Side.S;
        };
    }

    private static int[] front(Direction facing, int x0, int z0, int x1, int z1) {
        return switch (facing) {
            case NORTH -> new int[]{(x0 + x1) / 2, z0};
            case SOUTH -> new int[]{(x0 + x1) / 2, z1};
            case WEST -> new int[]{x0, (z0 + z1) / 2};
            default -> new int[]{x1, (z0 + z1) / 2};
        };
    }

    private static boolean overlaps(int x0, int z0, int x1, int z1,
                                    int ax0, int az0, int ax1, int az1) {
        return x0 <= ax1 && ax0 <= x1 && z0 <= az1 && az0 <= z1;
    }

    private static boolean blocked(int x0, int z0, int x1, int z1, int[][] rects) {
        for (int[] r : rects) {
            if (overlaps(x0, z0, x1, z1, r[0], r[1], r[2], r[3])) {
                return true;
            }
        }
        return false;
    }

    /** Levels a small authored pad to {@code y} and clears a little headroom. */
    private static void pad(StructureBuilder b, int x0, int z0, int x1, int z1, int y, Palette p) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                int surface = b.groundY(x, z);
                if (surface > y) {
                    b.fill(x, y + 1, z, x, surface, z, AIR);
                } else if (surface < y) {
                    b.fill(x, surface + 1, z, x, y, z, p.foundation());
                }
                b.put(x, y, z, p.ground());
                for (int yy = y + 1; yy <= y + 3; yy++) {
                    b.put(x, yy, z, AIR);
                }
            }
        }
    }

    /** A tapered stepped cap, used for the bell towers, the cupola and the church tower. */
    private static void pyramidCap(StructureBuilder b, int cx, int cz, int y, int radius,
                                   int height, BlockState state) {
        for (int i = 0; i < height; i++) {
            int r = radius - i;
            if (r < 0) {
                break;
            }
            b.fill(cx - r, y + i, cz - r, cx + r, y + i, cz + r, state);
        }
    }

    // ------------------------------------------------------------------ streets

    private static void streets(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        // The main north-south axis from the arrival gate to the museum forecourt.
        path(b, s, cx, cz + 320, cx, cz + 150, 14, p.ground());
        // The long east-west axis: barrios, centre, port.
        path(b, s, cx - 320, cz - 20, cx + 300, cz - 20, 14, p.ground());
        // The last approach into the Plaza Mayor.
        path(b, s, cx, cz - 20, cx, cz - 70, 14, p.ground());
        // Short lanes from the palace and the arcades onto the plaza.
        path(b, s, cx - 76, cz - 130, cx - 70, cz - 130, 4, p.ground());
        path(b, s, cx + 70, cz - 120, cx + 76, cz - 120, 4, p.ground());
        path(b, s, cx - 38, cz - 70, cx - 38, cz - 64, 4, p.ground());
        path(b, s, cx + 38, cz - 70, cx + 38, cz - 64, 4, p.ground());

        // Barrio lanes: five-wide, crossing the axis.
        for (int x : new int[]{-300, -260, -220, -180}) {
            path(b, s, cx + x, cz - 220, cx + x, cz + 160, 5, p.ground());
        }
        for (int z : new int[]{-180, -120, 0, 60, 120}) {
            path(b, s, cx - 320, cz + z, cx - 140, cz + z, 5, p.ground());
        }
        // The z = -60 and z = 120 lanes are interrupted by the shop and the curandero.
        path(b, s, cx - 320, cz - 60, cx - 258, cz - 60, 5, p.ground());
        path(b, s, cx - 222, cz - 60, cx - 140, cz - 60, 5, p.ground());
        path(b, s, cx - 320, cz + 120, cx - 258, cz + 120, 5, p.ground());
        path(b, s, cx - 222, cz + 120, cx - 140, cz + 120, 5, p.ground());

        // Callao roads: the ten-wide port road, the quay road and two cross streets.
        path(b, s, cx + 200, cz - 240, cx + 210, cz + 160, 10, p.ground());
        path(b, s, cx + 286, cz - 240, cx + 286, cz + 160, 8, p.ground());
        path(b, s, cx + 140, cz - 160, cx + 290, cz - 160, 8, p.ground());
        path(b, s, cx + 140, cz + 100, cx + 290, cz + 100, 8, p.ground());

        // Centre lanes in front of the shops, and the hotel lane.
        path(b, s, cx - 60, cz - 20, cx - 60, cz + 130, 5, p.ground());
        path(b, s, cx + 60, cz - 20, cx + 60, cz + 130, 5, p.ground());
        path(b, s, cx - 80, cz - 50, cx - 80, cz - 27, 5, p.ground());
        // Link the barrio's main lane to the east-west axis.
        path(b, s, cx - 140, cz - 20, cx - 140, cz, 5, p.ground());

        // Ruin paths: six-wide dusty ways around the huaca.
        path(b, s, cx - 46, cz + 136, cx - 46, cz - 20, 6, p.rubble());
        path(b, s, cx + 46, cz + 136, cx + 46, cz - 20, 6, p.rubble());
        path(b, s, cx - 46, cz + 104, cx + 46, cz + 104, 6, p.rubble());
        path(b, s, cx, cz + 104, cx, cz + 100, 6, p.rubble());
        path(b, s, cx - 46, cz + 140, cx - 30, cz + 140, 5, p.ground());
        path(b, s, cx + 46, cz + 140, cx + 30, cz + 140, 5, p.ground());

        // The road to the coast and the tomb.
        path(b, s, cx + 120, cz - 20, cx + 120, cz + 160, 6, p.ground());

        // Fog-lit lanterns down the main axis.
        for (int z = -160; z <= 300; z += 40) {
            lanternPost(b, cx - 12, cz + z, s.at(cx - 12, cz + z), p);
            lanternPost(b, cx + 12, cz + z, s.at(cx + 12, cz + z), p);
        }
        // Barrio lane lamps.
        for (int z = -200; z <= 150; z += 50) {
            if (z == 0) {
                continue;
            }
            lanternPost(b, cx - 296, cz + z, s.at(cx - 296, cz + z), p);
            lanternPost(b, cx - 216, cz + z, s.at(cx - 216, cz + z), p);
        }
    }

    // ------------------------------------------------------------------ arrival gate

    private static void arrivalGate(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx, cz + 320);
        gateTower(b, cx - 22, cz + 310, cx - 10, cz + 322, ground, p);
        gateTower(b, cx + 10, cz + 310, cx + 22, cz + 322, ground, p);
        b.fill(cx - 10, ground + 7, cz + 312, cx + 10, ground + 10, cz + 320, p.wall());
        b.fill(cx - 10, ground + 11, cz + 312, cx + 10, ground + 11, cz + 320, p.roofSlab());
        b.crenellations(cx - 10, cz + 312, cx + 10, cz + 320, ground + 12);
        b.fill(cx - 8, ground + 1, cz + 312, cx + 8, ground + 6, cz + 320, AIR);
        b.fill(cx - 9, ground, cz + 312, cx - 9, ground, cz + 320, p.foundation());
        b.fill(cx + 9, ground, cz + 312, cx + 9, ground, cz + 320, p.foundation());
        sign(b, p, cx, ground + 10, cz + 320, Direction.SOUTH, "LIMA");
        b.put(cx - 20, ground + 1, cz + 324, p.light());
        b.put(cx + 20, ground + 1, cz + 324, p.light());

        String[][] directions = {
                {"CATHEDRAL"},
                {"PORT"},
                {"BARRIOS"},
                {"RUINS"},
                {"CENTER"}};
        int[] posts = {-26, -18, -10, 10, 18};
        for (int i = 0; i < posts.length; i++) {
            signPost(b, cx + posts[i], cz + 328, ground, p, Direction.SOUTH, directions[i]);
        }

        b.fill(cx - 32, ground + 1, cz + 333, cx - 8, ground + 4, cz + 334, p.foundation());
        b.fill(cx - 33, ground + 5, cz + 333, cx - 7, ground + 5, cz + 335, p.roofSlab());
        sign(b, p, cx - 28, ground + 4, cz + 334, Direction.SOUTH, "CITY NOTICES");
        sign(b, p, cx - 22, ground + 4, cz + 334, Direction.SOUTH, "CURFEW");
        sign(b, p, cx - 16, ground + 4, cz + 334, Direction.SOUTH, "NO WAGONS");
    }

    private static void gateTower(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  int ground, Palette p) {
        b.room(x0, ground, z0, x1, ground + 9, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 9, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 9, 4, 0);
        b.window(x0 + 2, ground + 4, z0, 2, 1, true);
        b.window(x1 - 2, ground + 4, z1, 2, 1, true);
        b.put(x0 + 2, ground + 1, z0 + 2, p.light());
    }

    // ------------------------------------------------------------------ Plaza Mayor

    private static void plazaMayor(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx, cz - 120);
        pave(b, cx - 70, cz - 170, cx + 70, cz - 70, ground, p.ground());
        outline(b, cx - 70, cz - 170, cx + 70, cz - 70, ground, p.foundation());
        b.fill(cx - 1, ground, cz - 170, cx + 1, ground, cz - 70, p.foundation());
        b.fill(cx - 70, ground, cz - 120, cx + 70, ground, cz - 120, p.foundation());

        fountain(b, cx, cz - 140, ground, p);
        questBoard(b, cx, cz, ground, p);

        int[][] palms = {
                {-50, -160}, {-30, -90}, {30, -90}, {50, -160}, {-50, -80}, {50, -80}};
        for (int[] pt : palms) {
            palm(b, cx + pt[0], cz + pt[1], ground);
        }
        for (int z = -160; z <= -80; z += 20) {
            lanternPost(b, cx - 64, cz + z, ground, p);
            lanternPost(b, cx + 64, cz + z, ground, p);
        }
        signPost(b, cx - 14, cz - 68, ground, p, Direction.SOUTH, "PLAZA MAYOR");
    }

    private static void fountain(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        b.fill(cx - 5, ground, cz - 5, cx + 5, ground, cz + 5, p.foundation());
        b.fill(cx - 4, ground, cz - 4, cx + 4, ground, cz + 4, WATER);
        b.fill(cx - 5, ground + 1, cz - 5, cx + 5, ground + 1, cz - 5, p.foundation());
        b.fill(cx - 5, ground + 1, cz + 5, cx + 5, ground + 1, cz + 5, p.foundation());
        b.fill(cx - 5, ground + 1, cz - 5, cx - 5, ground + 1, cz + 5, p.foundation());
        b.fill(cx + 5, ground + 1, cz - 5, cx + 5, ground + 1, cz + 5, p.foundation());
        b.fill(cx - 2, ground + 1, cz - 2, cx + 2, ground + 1, cz + 2, p.foundation());
        b.monument(cx, cz, ground + 1);
        b.put(cx - 5, ground + 1, cz, p.light());
        b.put(cx + 5, ground + 1, cz, p.light());
    }

    private static void questBoard(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int z = cz - 118;
        b.fill(cx - 8, ground + 1, z, cx - 8, ground + 4, z, p.accent());
        b.fill(cx + 8, ground + 1, z, cx + 8, ground + 4, z, p.accent());
        b.fill(cx - 8, ground + 5, z - 1, cx + 8, ground + 5, z + 1, p.roofSlab());
        b.fill(cx - 7, ground + 2, z + 1, cx + 7, ground + 4, z + 1, p.foundation());
        sign(b, p, cx - 4, ground + 4, z + 1, Direction.SOUTH, "QUEST NOTICES");
        sign(b, p, cx + 2, ground + 4, z + 1, Direction.SOUTH, "MISSING MAN");
        sign(b, p, cx + 6, ground + 4, z + 1, Direction.SOUTH, "STOLEN QUIPU");
        b.put(cx - 6, ground + 3, z, p.light());
        b.put(cx + 6, ground + 3, z, p.light());
    }

    // ------------------------------------------------------------------ cathedral

    private static void cathedral(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx, cz - 210);
        int x0 = cx - 13;
        int x1 = cx + 13;
        int z0 = cz - 242;
        int z1 = cz - 178;
        int top = ground + 26;

        b.fill(x0 - 2, ground - 3, z0 - 2, x1 + 2, ground, z1 + 2, p.foundation());
        b.room(x0, ground, z0, x1, top, z1, new Doorway(Side.S, (x1 - x0) / 2));
        b.walls(x0, ground + 1, z0, x1, top - 1, z1, p.wall());
        b.fill(cx, ground + 1, z1, cx, ground + 2, z1, AIR);
        quoins(b, x0, z0, x1, z1, ground + 1, top - 1, p);
        for (int z = z0 + 6; z <= z1 - 6; z += 8) {
            b.buttress(x0, z, ground, 12, Side.W);
            b.buttress(x1, z, ground, 12, Side.E);
        }
        for (int z = z0 + 10; z <= z1 - 10; z += 8) {
            b.window(x0, ground + 12, z, 8, 1, true);
            b.window(x1, ground + 12, z, 8, 1, true);
        }
        b.fill(x0, top, z0, x0, top, z1, p.foundation());
        b.fill(x1, top, z0, x1, top, z1, p.foundation());
        b.pitchedRoof(x0 - 2, z0 - 2, x1 + 2, z1 + 2, top, 9, 1);

        // Baroque south front on the plaza.
        int fz = z1 + 1;
        b.fill(cx - 15, ground + 1, fz, cx + 15, ground + 28, fz, p.wall());
        b.fill(cx - 16, ground + 29, fz, cx + 16, ground + 29, fz, p.foundation());
        for (int px : new int[]{cx - 15, cx - 8, cx + 8, cx + 15}) {
            b.fill(px, ground + 1, fz, px, ground + 27, fz, p.foundation());
        }
        b.fill(cx - 4, ground + 1, fz, cx + 4, ground + 9, fz, AIR);
        b.fill(cx - 5, ground + 10, fz, cx + 5, ground + 10, fz, p.accent());
        door(b, p, cx, ground + 1, fz, Direction.SOUTH);
        b.window(cx - 3, ground + 17, fz, 6, 6, false);
        b.fill(cx, ground + 30, fz, cx, ground + 34, fz, p.accent());
        b.fill(cx - 2, ground + 32, fz, cx + 2, ground + 32, fz, p.accent());
        b.put(cx, ground + 35, fz, p.light());
        b.fill(cx - 8, ground + 1, cz - 230, cx - 6, ground + 1, cz - 220, p.accent());
        b.fill(cx + 6, ground + 1, cz - 230, cx + 8, ground + 1, cz - 220, p.accent());
        b.fill(cx - 4, ground + 1, cz - 236, cx + 4, ground + 1, cz - 234, p.foundation());
        b.put(cx, ground + 2, cz - 235, p.light());

        cathedralTower(b, cx - 24, cx - 14, cz - 186, cz - 176, ground, p);
        cathedralTower(b, cx + 14, cx + 24, cz - 186, cz - 176, ground, p);
        dome(b, cx, cz - 210, ground, p);

        // The forecourt between the west front and the plaza.
        pave(b, cx - 40, cz - 176, cx + 40, cz - 170, ground, p.ground());
        outline(b, cx - 40, cz - 176, cx + 40, cz - 170, ground, p.foundation());
        sign(b, p, cx, ground + 12, fz, Direction.SOUTH, "CATHEDRAL");
        signPost(b, cx + 20, cz - 170, ground, p, Direction.SOUTH, "CATHEDRAL", "PLAZA MAYOR");
    }

    private static void cathedralTower(StructureBuilder b, int x0, int x1, int z0, int z1,
                                       int ground, Palette p) {
        int top = ground + 52;
        int tx = (x0 + x1) / 2;
        int tz = (z0 + z1) / 2;
        b.fill(x0 - 1, ground - 3, z0 - 1, x1 + 1, ground, z1 + 1, p.foundation());
        b.room(x0, ground, z0, x1, top, z1);
        b.walls(x0, ground + 1, z0, x1, top - 1, z1, p.wall());
        quoins(b, x0, z0, x1, z1, ground + 1, top - 1, p);
        for (int y = ground + 12; y <= top - 8; y += 10) {
            outline(b, x0, z0, x1, z1, y, p.foundation());
        }
        for (int y = ground + 6; y <= top - 18; y += 10) {
            b.window(tx - 1, y, z0, 4, 2, true);
            b.window(tx - 1, y, z1, 4, 2, true);
            b.window(x0, y, tz, 4, 1, true);
            b.window(x1, y, tz, 4, 1, true);
        }
        b.window(tx - 1, top - 14, z0, 8, 2, true);
        b.window(tx - 1, top - 14, z1, 8, 2, true);
        b.window(x0, top - 14, tz, 8, 1, true);
        b.window(x1, top - 14, tz, 8, 1, true);
        for (int y = top - 12; y <= top; y++) {
            b.put(x0 + 1, y, z0, p.frame());
            b.put(x1 - 1, y, z0, p.frame());
            b.put(x0 + 1, y, z1, p.frame());
            b.put(x1 - 1, y, z1, p.frame());
        }
        b.fill(x0 - 1, top, z0 - 1, x1 + 1, top, z1 + 1, p.roofSlab());
        pyramidCap(b, tx, tz, top + 1, 5, 5, p.roof());
        b.put(tx, top + 6, tz, p.light());
        door(b, p, tx, ground + 1, z1, Direction.SOUTH);
        sign(b, p, tx, ground + 4, z1, Direction.SOUTH, "CAPILLA");
    }

    private static void dome(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 6;
        int x1 = cx + 6;
        int z0 = cz - 6;
        int z1 = cz + 6;
        b.fill(x0, ground + 27, z0, x1, ground + 27, z1, p.roof());
        b.room(x0, ground + 28, z0, x1, ground + 36, z1);
        b.walls(x0, ground + 28, z0, x1, ground + 35, z1, p.wall());
        b.window(cx - 2, ground + 30, z0, 4, 4, false);
        b.window(cx - 2, ground + 30, z1, 4, 4, false);
        b.window(x0, ground + 30, cz - 1, 4, 1, true);
        b.window(x1, ground + 30, cz - 1, 4, 1, true);
        pyramidCap(b, cx, cz, ground + 37, 6, 5, p.roof());
        b.put(cx, ground + 42, cz, p.light());
    }

    // ------------------------------------------------------------------ palace and arcades

    private static void palace(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx - 92, cz - 130);
        int x0 = cx - 110;
        int x1 = cx - 76;
        int z0 = cz - 160;
        int z1 = cz - 100;

        b.fill(x0 - 2, ground - 3, z0 - 2, x1 + 2, ground, z1 + 2, p.foundation());
        b.room(x0, ground, z0, x1, ground + 14, z1, new Doorway(Side.E, (z1 - z0) / 2));
        b.walls(x0, ground + 1, z0, x1, ground + 13, z1, p.wall());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 13, p);
        for (int z = z0 + 3; z <= z1 - 3; z += 7) {
            b.fill(x1, ground + 1, z, x1, ground + 7, z, p.foundation());
            b.put(x1, ground + 8, z, p.foundation());
        }
        b.fill(x1 + 1, ground + 8, z0 + 2, x1 + 3, ground + 8, z1 - 2, p.roofSlab());
        b.fill(x1 + 3, ground + 9, z0 + 2, x1 + 3, ground + 9, z1 - 2, p.rail());
        for (int z = z0 + 6; z <= z1 - 6; z += 10) {
            b.window(x1, ground + 10, z, 3, 1, true);
        }
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 14, 8, 1);
        door(b, p, x1, ground + 1, cz - 130, Direction.EAST);
        sign(b, p, x1, ground + 6, cz - 130, Direction.EAST, "PALACIO");
        b.fill(x0 + 4, ground + 1, z0 + 4, x0 + 10, ground + 1, z0 + 4, p.accent());
        b.put(x0 + 6, ground + 2, z0 + 4, p.light());
        b.chest(x0 + 8, ground + 1, z1 - 6, SiteLoot.ORDER);
        int flagX = (x0 + x1) / 2;
        b.fill(flagX, ground + 22, z0 + 8, flagX, ground + 27, z0 + 8, p.accent());
        b.fill(flagX, ground + 27, z0 + 8, flagX + 3, ground + 27, z0 + 8,
                Materials.wool(DyeColor.RED));
        b.put(flagX, ground + 28, z0 + 8, p.light());
        palm(b, x0 + 4, z1 + 6, ground);
        palm(b, x0 + 12, z1 + 6, ground);
    }

    private static void arcade(StructureBuilder b, int x0, int z0, int x1, int z1,
                               int ground, Direction open, Palette p, String label) {
        b.fill(x0 - 1, ground - 2, z0 - 1, x1 + 1, ground, z1 + 1, p.foundation());
        b.fill(x0, ground, z0, x1, ground, z1, p.ground());
        if (open == Direction.WEST) {
            b.fill(x1, ground + 1, z0, x1, ground + 8, z1, p.wall());
            b.fill(x0, ground + 1, z0, x1, ground + 8, z0, p.wall());
            b.fill(x0, ground + 1, z1, x1, ground + 8, z1, p.wall());
            for (int z = z0 + 2; z <= z1 - 2; z += 6) {
                b.fill(x0, ground + 1, z, x0, ground + 7, z, p.foundation());
            }
            b.fill(x0, ground + 8, z0, x0, ground + 8, z1, p.foundation());
            b.fill(x0, ground + 9, z0, x1, ground + 9, z1, p.roofSlab());
            b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 9, 4, 1);
            for (int z = z0 + 4; z <= z1 - 4; z += 12) {
                b.put(x1 - 3, ground + 1, z, BARREL);
                b.put(x1 - 5, ground + 1, z, BOOKSHELF);
            }
            sign(b, p, x1, ground + 6, (z0 + z1) / 2, Direction.WEST, label);
        } else {
            b.fill(x0, ground + 1, z1, x1, ground + 8, z1, p.wall());
            b.fill(x0, ground + 1, z0, x0, ground + 8, z1, p.wall());
            b.fill(x1, ground + 1, z0, x1, ground + 8, z1, p.wall());
            for (int x = x0 + 2; x <= x1 - 2; x += 6) {
                b.fill(x, ground + 1, z0, x, ground + 7, z0, p.foundation());
            }
            b.fill(x0, ground + 8, z0, x1, ground + 8, z0, p.foundation());
            b.fill(x0, ground + 9, z0, x1, ground + 9, z1, p.roofSlab());
            b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 9, 4, 0);
            for (int x = x0 + 4; x <= x1 - 4; x += 12) {
                b.put(x, ground + 1, z1 - 3, BARREL);
            }
            sign(b, p, (x0 + x1) / 2, ground + 6, z1, Direction.NORTH, label);
        }
        b.put(x0 + 1, ground + 1, z0 + 1, p.light());
        b.put(x1 - 1, ground + 1, z1 - 1, p.light());
    }

    // ------------------------------------------------------------------ centre

    private static void center(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int hg = s.at(cx - 80, cz - 64);
        townhouse(b, cx - 96, cz - 78, cx - 64, cz - 50, hg, 11, Direction.SOUTH, p,
                p.wall(), true, "HOTEL", "LIMA");
        b.fill(cx - 92, hg + 1, cz - 74, cx - 90, hg + 1, cz - 72,
                Materials.wool(DyeColor.RED));
        b.fill(cx - 92, hg + 2, cz - 74, cx - 90, hg + 2, cz - 72,
                Materials.wool(DyeColor.WHITE));
        b.put(cx - 94, hg + 1, cz - 74, BOOKSHELF);
        b.chest(cx - 94, hg + 1, cz - 56, SiteLoot.COMMON);
        b.put(cx - 70, hg + 1, cz - 56, BARREL);
        b.put(cx - 80, hg + 1, cz - 56, p.light());

        fountain(b, cx - 88, cz - 40, hg, p);
        palm(b, cx - 96, cz - 40, hg);
        palm(b, cx - 76, cz - 36, hg);

        townhouse(b, cx - 86, cz + 2, cx - 66, cz + 18, s.at(cx - 76, cz + 10), 7,
                Direction.EAST, p, p.wall(), false, "TIENDA");
        townhouse(b, cx - 86, cz + 32, cx - 66, cz + 48, s.at(cx - 76, cz + 40), 8,
                Direction.EAST, p, p.wall(), false, "CAFE");
        townhouse(b, cx - 86, cz + 62, cx - 66, cz + 78, s.at(cx - 76, cz + 70), 7,
                Direction.EAST, p, p.wall(), false, "OFICINA");
        townhouse(b, cx - 86, cz + 92, cx - 66, cz + 108, s.at(cx - 76, cz + 100), 8,
                Direction.EAST, p, p.wall(), false, "PANADERIA");

        townhouse(b, cx + 66, cz + 2, cx + 86, cz + 18, s.at(cx + 76, cz + 10), 8,
                Direction.WEST, p, p.wall(), false, "CAFE");
        townhouse(b, cx + 66, cz + 32, cx + 86, cz + 48, s.at(cx + 76, cz + 40), 7,
                Direction.WEST, p, p.wall(), false, "TIENDA");
        townhouse(b, cx + 66, cz + 62, cx + 86, cz + 78, s.at(cx + 76, cz + 70), 8,
                Direction.WEST, p, p.wall(), false, "OFICINA");
        townhouse(b, cx + 66, cz + 92, cx + 86, cz + 108, s.at(cx + 76, cz + 100), 7,
                Direction.WEST, p, p.wall(), false, "LIBRERIA");
    }

    private static void townhouse(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  int ground, int height, Direction facing, Palette p,
                                  BlockState wall, boolean balcony, String... lines) {
        b.fill(x0 - 1, ground - 2, z0 - 1, x1 + 1, ground, z1 + 1, p.foundation());
        Side s = side(facing);
        int offset = (s == Side.N || s == Side.S) ? (x1 - x0) / 2 : (z1 - z0) / 2;
        b.room(x0, ground, z0, x1, ground + height, z1, new Doorway(s, offset));
        b.walls(x0, ground + 1, z0, x1, ground + height - 1, z1, wall);
        quoins(b, x0, z0, x1, z1, ground + 1, ground + height - 1, p);
        int ridge = (x1 - x0) >= (z1 - z0) ? 0 : 1;
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + height,
                4 + Math.floorMod(x0 + z0, 3), ridge);
        int[] f = front(facing, x0, z0, x1, z1);
        door(b, p, f[0], ground + 1, f[1], facing);
        if (facing == Direction.EAST || facing == Direction.WEST) {
            for (int dz : new int[]{-5, 5}) {
                int wz = f[1] + dz;
                if (wz > z0 + 1 && wz < z1 - 1) {
                    b.window(f[0], ground + 3, wz, 2, 1, true);
                    if (height >= 8 && !balcony) {
                        b.window(f[0], ground + 7, wz, 2, 1, true);
                    }
                }
            }
        } else {
            for (int dx : new int[]{-6, 6}) {
                int wx = f[0] + dx;
                if (wx > x0 + 1 && wx < x1 - 1) {
                    b.window(wx, ground + 3, f[1], 2, 2, true);
                    if (height >= 8 && !balcony) {
                        b.window(wx, ground + 7, f[1], 2, 2, true);
                    }
                }
            }
        }
        if (balcony && height >= 9) {
            mirador(b, f[0], f[1], facing, ground + 4, 8, p);
            sign(b, p, f[0], ground + 8, f[1], facing, lines);
        } else if (lines.length > 0) {
            sign(b, p, f[0], ground + 5, f[1], facing, lines);
        }
    }

    private static void mirador(StructureBuilder b, int x, int z, Direction facing, int y,
                                int width, Palette p) {
        int half = width / 2;
        switch (facing) {
            case SOUTH -> {
                for (int i = -half; i <= half; i++) {
                    b.put(x + i, y, z + 1, p.frame());
                    b.put(x + i, y + 1, z + 1, p.window());
                    b.put(x + i, y + 2, z + 1, p.frame());
                }
                b.fill(x - half, y + 3, z, x + half, y + 3, z + 1, p.roofSlab());
                b.put(x, y + 1, z + 1, p.light());
            }
            case NORTH -> {
                for (int i = -half; i <= half; i++) {
                    b.put(x + i, y, z - 1, p.frame());
                    b.put(x + i, y + 1, z - 1, p.window());
                    b.put(x + i, y + 2, z - 1, p.frame());
                }
                b.fill(x - half, y + 3, z - 1, x + half, y + 3, z, p.roofSlab());
                b.put(x, y + 1, z - 1, p.light());
            }
            case EAST -> {
                for (int i = -half; i <= half; i++) {
                    b.put(x + 1, y, z + i, p.frame());
                    b.put(x + 1, y + 1, z + i, p.window());
                    b.put(x + 1, y + 2, z + i, p.frame());
                }
                b.fill(x, y + 3, z - half, x + 1, y + 3, z + half, p.roofSlab());
                b.put(x + 1, y + 1, z, p.light());
            }
            default -> {
                for (int i = -half; i <= half; i++) {
                    b.put(x - 1, y, z + i, p.frame());
                    b.put(x - 1, y + 1, z + i, p.window());
                    b.put(x - 1, y + 2, z + i, p.frame());
                }
                b.fill(x - 1, y + 3, z - half, x, y + 3, z + half, p.roofSlab());
                b.put(x - 1, y + 1, z, p.light());
            }
        }
    }

    // ------------------------------------------------------------------ barrios

    private static void barrios(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int[][] reserved = {
                {cx - 252, cz - 72, cx - 228, cz - 48},   // bodega
                {cx - 252, cz + 108, cx - 228, cz + 132}, // curandero
                {cx - 296, cz + 24, cx - 264, cz + 48},   // church
                {cx - 212, cz - 166, cx - 156, cz - 138}, // market
                {cx - 316, cz - 44, cx - 306, cz - 28},   // courtyard
                {cx - 214, cz + 72, cx - 199, cz + 88},   // courtyard
                {cx - 172, cz - 108, cx - 158, cz - 92},  // courtyard
                {cx - 316, cz + 132, cx - 306, cz + 152}, // garden
                {cx - 238, cz + 132, cx - 227, cz + 148}}; // garden

        int[] blockX = {cx - 316, cx - 294, cx - 254, cx - 214, cx - 174};
        int[] blockW = {10, 28, 28, 28, 28};
        int[] segmentZ = {cz - 172, cz - 112, cz - 52, cz + 8, cz + 68, cz + 128};
        int[] segmentD = {48, 48, 24, 48, 48, 28};

        for (int i = 0; i < blockX.length; i++) {
            for (int j = 0; j < segmentZ.length; j++) {
                int x0 = blockX[i];
                int z0 = segmentZ[j];
                if (blockW[i] >= 24 && segmentD[j] >= 40) {
                    lot(b, x0, z0, 12, 16, 4 + Math.floorMod(i * 7 + j * 3, 4),
                            Direction.WEST, s, p, reserved);
                    lot(b, x0 + 16, z0 + 24, 12, 16, 4 + Math.floorMod(i * 5 + j * 7, 4),
                            Direction.EAST, s, p, reserved);
                } else if (blockW[i] >= 24) {
                    lot(b, x0, z0 + 2, 12, 16, 4 + Math.floorMod(i * 7 + j * 3, 4),
                            Direction.WEST, s, p, reserved);
                } else {
                    lot(b, x0, z0 + 4, 10, 14, 4 + Math.floorMod(i * 7 + j * 3, 4),
                            Direction.EAST, s, p, reserved);
                }
            }
        }

        // Authored courtyards and gardens in the gaps.
        courtyard(b, cx - 316, cz - 44, cx - 306, cz - 28, s.at(cx - 311, cz - 36), p);
        courtyard(b, cx - 214, cz + 72, cx - 199, cz + 88, s.at(cx - 207, cz + 80), p);
        courtyard(b, cx - 172, cz - 108, cx - 158, cz - 92, s.at(cx - 165, cz - 100), p);
        garden(b, cx - 316, cz + 132, cx - 306, cz + 152, s.at(cx - 311, cz + 142), p);
        garden(b, cx - 238, cz + 132, cx - 227, cz + 148, s.at(cx - 233, cz + 140), p);

        // The bodega and the curandero, on the two interrupted lanes.
        int shopGround = s.at(cx - 240, cz - 60);
        townhouse(b, cx - 252, cz - 72, cx - 228, cz - 48, shopGround, 7,
                Direction.EAST, p, p.weathered(), false, "BODEGA", "ABARROTES");
        b.put(cx - 250, shopGround + 1, cz - 70, BARREL);
        b.put(cx - 250, shopGround + 1, cz - 68, BARREL);
        b.put(cx - 250, shopGround + 1, cz - 66, BOOKSHELF);
        b.chest(cx - 250, shopGround + 1, cz - 64, SiteLoot.COMMON);
        int healerGround = s.at(cx - 240, cz + 120);
        townhouse(b, cx - 252, cz + 108, cx - 228, cz + 132, healerGround, 8,
                Direction.EAST, p, p.weathered(), false, "CURANDERO", "REMEDIOS");
        b.put(cx - 250, healerGround + 1, cz + 110, CAULDRON);
        b.put(cx - 250, healerGround + 1, cz + 112, BARREL);
        b.put(cx - 250, healerGround + 1, cz + 128, BOOKSHELF);
        b.chest(cx - 250, healerGround + 1, cz + 126, SiteLoot.ORDER);

        barrioChurch(b, cx, cz, s, p);
        barrioMarket(b, cx, cz, s, p);

        signPost(b, cx - 140, cz, s.at(cx - 140, cz), p, Direction.EAST,
                "BARRIOS", "ADOBE LANES");
        b.put(cx - 226, s.at(cx - 226, cz + 120) + 1, cz + 120, p.light());
        b.put(cx - 150, s.at(cx - 150, cz - 62) + 1, cz - 62, p.light());
    }

    private static void lot(StructureBuilder b, int x0, int z0, int w, int d, int height,
                            Direction facing, CitySurface s, Palette p, int[][] reserved) {
        int x1 = x0 + w - 1;
        int z1 = z0 + d - 1;
        if (blocked(x0 - 1, z0 - 1, x1 + 1, z1 + 1, reserved)) {
            return;
        }
        adobeHouse(b, x0, z0, w, d, height, facing, s, p);
    }

    private static void adobeHouse(StructureBuilder b, int x0, int z0, int w, int d, int height,
                                   Direction facing, CitySurface s, Palette p) {
        int x1 = x0 + w - 1;
        int z1 = z0 + d - 1;
        int ground = s.at(x0 + w / 2, z0 + d / 2);
        b.fill(x0 - 1, ground - 2, z0 - 1, x1 + 1, ground, z1 + 1, p.foundation());
        Side side = side(facing);
        int offset = (side == Side.N || side == Side.S) ? (x1 - x0) / 2 : (z1 - z0) / 2;
        b.room(x0, ground, z0, x1, ground + height, z1, new Doorway(side, offset));
        b.walls(x0, ground + 1, z0, x1, ground + height - 1, z1, p.weathered());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + height - 1, p);
        for (int y = ground + 2; y <= ground + height - 2; y += 3) {
            outline(b, x0, z0, x1, z1, y, p.foundation());
        }
        int ridge = w >= d ? 0 : 1;
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + height,
                3 + Math.floorMod(x0 * 3 + z0, 3), ridge);
        int[] f = front(facing, x0, z0, x1, z1);
        door(b, p, f[0], ground + 1, f[1], facing);
        if (facing == Direction.EAST || facing == Direction.WEST) {
            for (int dz : new int[]{-4, 4}) {
                int wz = f[1] + dz;
                if (wz > z0 + 1 && wz < z1 - 1) {
                    b.window(f[0], ground + 2, wz, 2, 1, true);
                }
            }
        } else {
            for (int dx : new int[]{-3, 3}) {
                int wx = f[0] + dx;
                if (wx > x0 + 1 && wx < x1 - 1) {
                    b.window(wx, ground + 2, f[1], 2, 2, true);
                }
            }
        }
        if (height >= 7) {
            mirador(b, f[0], f[1], facing, ground + 3, 8, p);
        }
        if (facing == Direction.EAST || facing == Direction.WEST) {
            b.put(f[0], ground + 1, f[1] + 1, p.light());
        } else {
            b.put(f[0] + 1, ground + 1, f[1], p.light());
        }
        if ((x0 + z0) % 5 == 0) {
            b.put(x1 - 1, ground + 1, z1 - 1, BARREL);
        }
    }

    private static void courtyard(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  int ground, Palette p) {
        b.fill(x0, ground, z0, x1, ground, z1, p.ground());
        outline(b, x0, z0, x1, z1, ground, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 2, z1, p.weathered());
        int mx = (x0 + x1) / 2;
        int mz = (z0 + z1) / 2;
        b.fill(mx - 1, ground + 1, z1, mx + 1, ground + 2, z1, AIR);
        b.fill(mx - 1, ground + 1, mz - 1, mx + 1, ground + 1, mz + 1, p.foundation());
        b.put(mx, ground + 1, mz, p.ground());
        b.put(mx - 2, ground + 1, mz - 2, p.light());
        palm(b, mx + 3, mz - 2, ground);
        BlockState growth = p.overgrowth();
        if (growth != null) {
            b.put(x0 + 1, ground + 1, z0 + 1, growth);
            b.put(x1 - 1, ground + 1, z1 - 1, growth);
        }
    }

    private static void garden(StructureBuilder b, int x0, int z0, int x1, int z1,
                               int ground, Palette p) {
        b.fill(x0, ground, z0, x1, ground, z1, p.rubble());
        outline(b, x0, z0, x1, z1, ground, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 1, z1, p.weathered());
        int mx = (x0 + x1) / 2;
        b.fill(mx - 1, ground + 1, z1, mx + 1, ground + 1, z1, AIR);
        BlockState growth = p.overgrowth();
        for (int x = x0 + 2; x <= x1 - 2; x += 3) {
            for (int z = z0 + 2; z <= z1 - 2; z += 3) {
                b.put(x, ground + 1, z, growth != null ? growth : p.accent());
            }
        }
        palm(b, mx, (z0 + z1) / 2, ground);
    }

    private static void barrioChurch(StructureBuilder b, int cx, int cz, CitySurface s,
                                     Palette p) {
        int ground = s.at(cx - 280, cz + 36);
        int x0 = cx - 296;
        int x1 = cx - 264;
        int z0 = cz + 24;
        int z1 = cz + 48;
        b.fill(x0 - 1, ground - 2, z0 - 1, x1 + 1, ground, z1 + 1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 10, z1, new Doorway(Side.E, (z1 - z0) / 2));
        b.walls(x0, ground + 1, z0, x1, ground + 9, z1, p.weathered());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 9, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 10, 6, 0);
        door(b, p, x1, ground + 1, cz + 36, Direction.EAST);
        sign(b, p, x1, ground + 5, cz + 36, Direction.EAST, "IGLESIA", "SAN PEDRO");
        b.fill(x0 + 8, ground + 1, z0 + 4, x0 + 12, ground + 1, z0 + 4, p.foundation());
        b.put(x0 + 10, ground + 2, z0 + 4, p.light());
        b.window(x1, ground + 3, cz + 30, 3, 1, true);
        b.window(x1, ground + 3, cz + 42, 3, 1, true);

        int tx = cx - 288;
        int tz = cz + 30;
        b.room(tx - 4, ground, tz - 4, tx + 4, ground + 20, tz + 4);
        b.walls(tx - 4, ground + 1, tz - 4, tx + 4, ground + 19, tz + 4, p.wall());
        quoins(b, tx - 4, tz - 4, tx + 4, tz + 4, ground + 1, ground + 19, p);
        b.fill(tx - 4, ground + 20, tz - 4, tx + 4, ground + 20, tz + 4, p.roofSlab());
        b.crenellations(tx - 5, tz - 5, tx + 5, tz + 5, ground + 21);
        pyramidCap(b, tx, tz, ground + 22, 4, 4, p.roof());
        b.fill(tx, ground + 26, tz, tx, ground + 28, tz, p.accent());
        b.fill(tx - 2, ground + 27, tz, tx + 2, ground + 27, tz, p.accent());
        b.put(tx, ground + 29, tz, p.light());
        b.window(tx, ground + 14, tz - 4, 4, 2, true);
        b.window(tx, ground + 14, tz + 4, 4, 2, true);
    }

    private static void barrioMarket(StructureBuilder b, int cx, int cz, CitySurface s,
                                     Palette p) {
        int ground = s.at(cx - 184, cz - 152);
        int x0 = cx - 212;
        int x1 = cx - 156;
        int z0 = cz - 166;
        int z1 = cz - 138;
        b.fill(x0, ground, z0, x1, ground, z1, p.ground());
        outline(b, x0, z0, x1, z1, ground, p.foundation());
        stall(b, cx - 208, cz - 160, ground, p, DyeColor.RED);
        stall(b, cx - 208, cz - 148, ground, p, DyeColor.YELLOW);
        stall(b, cx - 200, cz - 154, ground, p, DyeColor.ORANGE);
        stall(b, cx - 170, cz - 160, ground, p, DyeColor.LIGHT_BLUE);
        stall(b, cx - 170, cz - 148, ground, p, DyeColor.RED);
        stall(b, cx - 162, cz - 154, ground, p, DyeColor.YELLOW);
        b.fill(cx - 186, ground + 1, cz - 154, cx - 182, ground + 1, cz - 150,
                p.foundation());
        b.put(cx - 184, ground + 1, cz - 152, p.ground());
        b.put(cx - 184, ground + 2, cz - 152, p.light());
        signPost(b, cx - 184, cz - 168, ground, p, Direction.NORTH, "MERCADO", "FRESH");
    }

    // ------------------------------------------------------------------ ruins and museum

    private static void ruins(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx, cz + 60);
        enclosure(b, cx, cz, ground, p);
        pyramid(b, cx, cz, ground, p);

        // The museum forecourt at the south end of the boulevard.
        pave(b, cx - 30, cz + 136, cx + 30, cz + 160, ground, p.ground());
        outline(b, cx - 30, cz + 136, cx + 30, cz + 160, ground, p.foundation());
        palm(b, cx - 24, cz + 150, ground);
        palm(b, cx + 24, cz + 150, ground);

        museum(b, cx, cz, s, p);
        signPost(b, cx, cz + 102, ground, p, Direction.SOUTH, "RUINS", "SACRED");
        signPost(b, cx - 10, cz + 160, ground, p, Direction.SOUTH,
                "CENTER", "PLAZA MAYOR");
    }

    private static void enclosure(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 40;
        int x1 = cx + 40;
        int z0 = cz + 20;
        int z1 = cz + 100;
        b.walls(x0, ground + 1, z0, x1, ground + 4, z1, p.foundation());
        b.fill(cx - 3, ground + 1, z1, cx + 3, ground + 4, z1, AIR);
        b.fill(cx - 4, ground + 5, z1, cx + 4, ground + 5, z1, p.foundation());
        for (int x = x0 + 4; x <= x1 - 4; x += 12) {
            b.fill(x, ground + 1, z0, x + 2, ground + 3, z0, AIR);
            b.fill(x, ground + 1, z1, x + 2, ground + 3, z1, AIR);
        }
        int[][] corners = {{x0, z0}, {x1, z0}, {x0, z1}, {x1, z1}};
        for (int[] c : corners) {
            b.fill(c[0] - 2, ground + 1, c[1] - 2, c[0] + 2, ground + 5, c[1] + 2,
                    p.foundation());
            b.put(c[0], ground + 6, c[1], p.light());
        }
        b.monument(cx - 34, cz + 88, ground + 1);
        b.monument(cx + 34, cz + 88, ground + 1);
        b.fill(cx - 3, ground, cz + 84, cx + 3, ground, cz + 100, p.rubble());
        b.fill(cx - 3, ground, cz + 20, cx + 3, ground, cz + 42, p.rubble());
        b.fill(cx - 30, ground, cz + 30, cx - 22, ground, cz + 90, p.rubble());
        b.fill(cx + 22, ground, cz + 30, cx + 30, ground, cz + 90, p.rubble());
    }

    private static void pyramid(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        for (int tier = 0; tier < 4; tier++) {
            int inset = tier * 3;
            int y = ground + tier * 4;
            BlockState state = tier % 2 == 0 ? CUT_SANDSTONE : MOSSY_STONE;
            b.fill(cx - 18 + inset, y, cz - 18 + inset,
                    cx + 18 - inset, y + 3, cz + 18 - inset, state);
        }
        int top = ground + 16;
        b.fill(cx - 6, top, cz - 6, cx + 6, top, cz + 6, CUT_SANDSTONE);
        b.room(cx - 3, top + 1, cz - 6, cx + 3, top + 6, cz,
                new Doorway(Side.S, 3));
        b.walls(cx - 3, top + 1, cz - 6, cx + 3, top + 5, cz, p.foundation());
        b.fill(cx - 4, top + 6, cz - 7, cx + 4, top + 6, cz + 1, p.roofSlab());
        b.put(cx, top + 7, cz - 3, p.light());
        door(b, p, cx, top + 2, cz, Direction.SOUTH);
        sign(b, p, cx, top + 5, cz, Direction.SOUTH, "HUACA", "SUN TEMPLE");
        b.chest(cx, top + 2, cz - 1, SiteLoot.RELIC);

        for (int i = 0; i < 17; i++) {
            int z = cz + 18 - i;
            int y = ground + 1 + i;
            b.fill(cx - 2, ground, z, cx + 2, y, z, AIR);
            b.fill(cx - 2, y, z, cx + 2, y, z, CUT_SANDSTONE);
        }
        b.fill(cx - 2, top, cz + 4, cx + 2, top, cz + 6, AIR);
        b.fill(cx - 2, ground, cz + 79, cx + 2, ground, cz + 84, p.rubble());
    }

    private static void museum(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx, cz + 124);
        int x0 = cx - 24;
        int x1 = cx + 24;
        int z0 = cz + 112;
        int z1 = cz + 136;
        b.fill(x0 - 2, ground - 2, z0 - 2, x1 + 2, ground, z1 + 2, p.foundation());
        b.room(x0, ground, z0, x1, ground + 10, z1, new Doorway(Side.S, (x1 - x0) / 2));
        b.walls(x0, ground + 1, z0, x1, ground + 9, z1, p.foundation());
        b.fill(x0, ground + 10, z0, x1, ground + 10, z1, p.roofSlab());
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 11);
        door(b, p, cx, ground + 1, z1, Direction.SOUTH);
        sign(b, p, cx, ground + 5, z1, Direction.SOUTH, "MUSEO", "DE SITIO");
        for (int x = x0 + 5; x <= x1 - 5; x += 8) {
            b.window(x, ground + 3, z1, 3, 2, true);
        }
        b.fill(x0 + 3, ground + 1, z0 + 3, x0 + 7, ground + 2, z0 + 6, p.foundation());
        b.put(x0 + 5, ground + 3, z0 + 4, p.accent());
        b.fill(x1 - 7, ground + 1, z0 + 3, x1 - 3, ground + 2, z0 + 6, p.foundation());
        b.put(x1 - 5, ground + 3, z0 + 4, p.accent());
        b.put(x0 + 3, ground + 1, z1 - 4, BOOKSHELF);
        b.put(x0 + 4, ground + 1, z1 - 4, BOOKSHELF);
        b.chest(x1 - 4, ground + 1, z1 - 4, SiteLoot.ORDER);
        sign(b, p, x1, ground + 4, cz + 124, Direction.WEST, "QUIPU", "STOLEN");
        b.fill(cx - 4, ground + 1, z0 + 4, cx + 4, ground + 1, z0 + 4, p.accent());
        b.put(cx, ground + 2, z0 + 4, p.light());
    }

    // ------------------------------------------------------------------ port of Callao

    private static void port(StructureBuilder b, int cx, int cz, CitySurface s, int quay,
                             Palette p) {
        // The harbour basin and its stone quay.
        b.fill(cx + 302, quay - 4, cz - 200, cx + 334, quay - 4, cz + 160, SAND);
        b.fill(cx + 302, quay - 3, cz - 200, cx + 334, quay - 1, cz + 160, WATER);
        b.fill(cx + 300, quay - 4, cz - 240, cx + 301, quay + 2, cz + 160, p.foundation());
        b.fill(cx + 300, quay + 2, cz - 240, cx + 300, quay + 2, cz + 160, p.weathered());
        b.fill(cx + 283, quay - 2, cz - 240, cx + 290, quay, cz + 160, p.ground());
        b.fill(cx + 283, quay - 2, cz - 240, cx + 283, quay, cz + 160, p.foundation());
        for (int z = -180; z <= 150; z += 12) {
            b.put(cx + 296, quay + 1, cz + z, p.accent());
            if ((z & 24) == 0) {
                b.put(cx + 296, quay + 2, cz + z, p.light());
            }
        }

        // Piers, ships and cranes.
        for (int z : new int[]{-100, 0, 100}) {
            pier(b, cx + 302, cz + z, quay, p);
            b.fill(cx + 300, quay, cz + z - 3, cx + 301, quay + 2, cz + z + 3, AIR);
            crane(b, cx + 294, cz + z, quay, p);
        }
        ship(b, cx + 318, cz - 125, quay, p, false);
        ship(b, cx + 318, cz + 25, quay, p, false);
        ship(b, cx + 318, cz + 125, quay, p, false);

        // Warehouses, the chandlery, the fonda and the cargo yard.
        warehouse(b, cx + 148, cz - 238, cx + 188, cz - 210, s, p, "ALMACEN 1", 0);
        warehouse(b, cx + 148, cz - 196, cx + 188, cz - 166, s, p, "ALMACEN 2", 1);
        warehouse(b, cx + 252, cz - 238, cx + 278, cz - 210, s, p, "ALMACEN 3", 0);
        warehouse(b, cx + 252, cz - 190, cx + 278, cz - 168, s, p, "ALMACEN 4", 2);
        warehouse(b, cx + 148, cz + 40, cx + 188, cz + 80, s, p, "CHANDLER", 1);
        warehouse(b, cx + 148, cz + 108, cx + 188, cz + 136, s, p, "FONDA", 2);
        warehouse(b, cx + 240, cz + 40, cx + 278, cz + 80, s, p, "ALMACEN 5", 0);
        warehouse(b, cx + 240, cz + 108, cx + 278, cz + 136, s, p, "ALMACEN 6", 1);

        b.fill(cx + 214, quay, cz - 140, cx + 274, quay, cz - 100, p.rubble());
        for (int x = cx + 218; x <= cx + 270; x += 8) {
            for (int z = cz - 136; z <= cz - 104; z += 8) {
                b.fill(x, quay + 1, z, x, quay + 2, z, p.foundation());
                if (((x + z) & 16) == 0) {
                    b.put(x, quay + 3, z, BARREL);
                }
            }
        }
        crane(b, cx + 270, cz - 120, quay, p);

        fishMarket(b, cx, cz, s, p);
        customs(b, cx, cz, s, p);
        railStation(b, cx, cz, s, p);

        signPost(b, cx + 140, cz - 20, s.at(cx + 140, cz - 20), p, Direction.WEST,
                "PORT", "CALLAO");
        signPost(b, cx + 220, cz + 24, s.at(cx + 220, cz + 24), p, Direction.NORTH,
                "ADUANA", "CUSTOMS");
        signPost(b, cx + 292, cz + 60, s.at(cx + 292, cz + 60), p, Direction.WEST,
                "CALLAO", "QUAY");
    }

    private static void pier(StructureBuilder b, int x, int z, int quay, Palette p) {
        b.fill(x, quay - 4, z - 3, x + 28, quay - 4, z + 3, p.foundation());
        b.fill(x, quay - 3, z - 3, x + 28, quay - 1, z + 3, AIR);
        b.fill(x, quay, z - 3, x + 28, quay, z + 3, p.accent());
        for (int px = x; px <= x + 28; px += 7) {
            b.fill(px, quay - 4, z - 3, px, quay, z - 3, p.foundation());
            b.fill(px, quay - 4, z + 3, px, quay, z + 3, p.foundation());
        }
        b.fill(x, quay + 1, z - 3, x + 28, quay + 1, z - 3, p.rail());
        b.fill(x, quay + 1, z + 3, x + 28, quay + 1, z + 3, p.rail());
        b.put(x + 28, quay + 2, z, p.light());
    }

    private static void ship(StructureBuilder b, int x, int z, int quay, Palette p,
                             boolean wreck) {
        int hx0 = x - 8;
        int hx1 = x + 8;
        int hz0 = z - 18;
        int hz1 = z + 18;
        b.fill(hx0, quay - 3, hz0, hx1, quay, hz1, p.weathered());
        b.fill(hx0 + 1, quay - 2, hz0 + 1, hx1 - 1, quay, hz1 - 1, AIR);
        b.fill(hx0 + 1, quay - 1, hz0 + 1, hx1 - 1, quay - 1, hz1 - 1, p.foundation());
        b.fill(hx0 + 1, quay, hz0 + 1, hx1 - 1, quay, hz1 - 1, p.roofSlab());
        b.fill(hx0, quay + 1, hz0, hx1, quay + 1, hz0 + 1, p.rail());
        b.fill(hx0, quay + 1, hz1 - 1, hx1, quay + 1, hz1, p.rail());
        b.fill(hx0, quay + 1, hz0, hx0 + 1, quay + 1, hz1, p.rail());
        b.fill(hx1 - 1, quay + 1, hz0, hx1, quay + 1, hz1, p.rail());
        int mast = wreck ? quay + 10 : quay + 18;
        b.fill(x, quay + 1, z, x, mast, z, p.accent());
        if (!wreck) {
            b.fill(x - 6, mast - 6, z, x + 6, mast - 2, z,
                    Materials.wool(DyeColor.WHITE));
            b.fill(x - 5, mast - 12, z, x + 5, mast - 8, z,
                    Materials.wool(DyeColor.RED));
            b.put(x, mast + 1, z, p.light());
        } else {
            b.fill(x - 5, quay + 2, z - 8, x + 5, quay + 5, z - 4, AIR);
            b.fill(x - 3, quay + 1, z + 6, x + 3, quay + 3, z + 10, p.rubble());
            b.put(x - 4, quay + 1, z, BARREL);
        }
    }

    private static void crane(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 1, ground, z - 1, x + 1, ground, z + 1, p.foundation());
        b.fill(x, ground + 1, z, x, ground + 10, z, p.accent());
        b.fill(x, ground + 9, z, x + 8, ground + 9, z, p.accent());
        b.put(x + 4, ground + 8, z, p.accent());
        b.fill(x + 8, ground + 5, z, x + 8, ground + 8, z, p.rail());
        b.put(x + 8, ground + 4, z, p.light());
        b.put(x, ground + 11, z, p.light());
    }

    private static void warehouse(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  CitySurface s, Palette p, String label, int variant) {
        int ground = s.at((x0 + x1) / 2, (z0 + z1) / 2);
        b.fill(x0 - 1, ground - 2, z0 - 1, x1 + 1, ground, z1 + 1, p.foundation());
        b.room(x0, ground, z0, x1, ground + 10, z1);
        b.walls(x0, ground + 1, z0, x1, ground + 9, z1, p.weathered());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 9, p);
        int ridge = (x1 - x0) >= (z1 - z0) ? 0 : 1;
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 10, 6, ridge);
        int dx;
        int dz;
        Direction facing;
        if (variant % 3 == 0) {
            dx = x1;
            dz = (z0 + z1) / 2;
            facing = Direction.EAST;
        } else if (variant % 3 == 1) {
            dx = (x0 + x1) / 2;
            dz = z1;
            facing = Direction.SOUTH;
        } else {
            dx = x0;
            dz = (z0 + z1) / 2;
            facing = Direction.WEST;
        }
        door(b, p, dx, ground + 1, dz, facing);
        sign(b, p, dx, ground + 5, dz, facing, label, "CARGO");
        for (int i = 0; i < 4; i++) {
            b.put(x0 + 3 + i * 3, ground + 1, z0 + 3, BARREL);
        }
        b.fill(x0 + 2, ground + 1, z1 - 5, x0 + 6, ground + 3, z1 - 2, p.foundation());
        b.put(x0 + 4, ground + 4, z1 - 3, p.light());
    }

    private static void customs(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx + 240, cz);
        int x0 = cx + 224;
        int x1 = cx + 256;
        int z0 = cz - 12;
        int z1 = cz + 16;
        b.fill(x0 - 2, ground - 2, z0 - 2, x1 + 2, ground, z1 + 2, p.foundation());
        b.room(x0, ground, z0, x1, ground + 12, z1, new Doorway(Side.N, (x1 - x0) / 2));
        b.walls(x0, ground + 1, z0, x1, ground + 11, z1, p.wall());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 11, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 12, 5, 1);
        door(b, p, cx + 240, ground + 1, z0, Direction.NORTH);
        sign(b, p, cx + 240, ground + 6, z0, Direction.NORTH, "ADUANA", "CUSTOMS");
        sign(b, p, cx + 248, ground + 5, z0, Direction.NORTH,
                "STOLEN QUIPU", "SEE CUSTOMS");

        int tx = cx + 240;
        int tz = z0 + 4;
        b.room(tx - 4, ground + 12, tz - 4, tx + 4, ground + 22, tz + 4);
        b.walls(tx - 4, ground + 13, tz - 4, tx + 4, ground + 21, tz + 4, p.wall());
        b.fill(tx - 4, ground + 22, tz - 4, tx + 4, ground + 22, tz + 4, p.roofSlab());
        pyramidCap(b, tx, tz, ground + 23, 4, 4, p.roof());
        b.window(tx - 2, ground + 17, tz - 4, 4, 4, false);
        b.put(tx, ground + 27, tz, p.light());

        b.fill(x0 + 3, ground + 1, z1 - 6, x0 + 8, ground + 1, z1 - 3, p.accent());
        b.put(x0 + 4, ground + 2, z1 - 5, BARREL);
        b.put(x1 - 4, ground + 1, z1 - 5, BOOKSHELF);
        b.chest(x1 - 3, ground + 1, z1 - 4, SiteLoot.ORDER);
        sign(b, p, x1, ground + 4, cz, Direction.WEST, "QUIPU", "STOLEN");
        b.put(cx + 238, ground + 1, z1 - 2, p.light());
        b.put(cx + 242, ground + 1, z1 - 2, p.light());
    }

    private static void fishMarket(StructureBuilder b, int cx, int cz, CitySurface s,
                                   Palette p) {
        int ground = s.at(cx + 168, cz + 8);
        int x0 = cx + 148;
        int x1 = cx + 188;
        int z0 = cz - 8;
        int z1 = cz + 20;
        b.fill(x0, ground, z0, x1, ground, z1, p.ground());
        outline(b, x0, z0, x1, z1, ground, p.foundation());
        stall(b, x0 + 4, z0 + 4, ground, p, DyeColor.LIGHT_BLUE);
        stall(b, x0 + 16, z0 + 4, ground, p, DyeColor.WHITE);
        stall(b, x0 + 28, z0 + 4, ground, p, DyeColor.LIGHT_BLUE);
        for (int i = 0; i < 4; i++) {
            b.put(x0 + 4 + i * 6, ground + 1, z1 - 4, BARREL);
        }
        b.put(x0 + 2, ground + 1, z0 + 2, p.light());
        b.put(x1 - 2, ground + 1, z0 + 2, p.light());
        signPost(b, x0 + 20, z1 + 2, ground, p, Direction.SOUTH, "PESCADO", "FRESH");
    }

    private static void railStation(StructureBuilder b, int cx, int cz, CitySurface s,
                                    Palette p) {
        int ground = s.at(cx + 230, cz - 226);
        int x0 = cx + 212;
        int x1 = cx + 248;
        int z0 = cz - 238;
        int z1 = cz - 214;
        b.fill(x0 - 4, ground - 2, z0 - 4, x1 + 6, ground, z1 + 6, p.foundation());
        b.room(x0, ground, z0, x1, ground + 9, z1, new Doorway(Side.S, (x1 - x0) / 2));
        b.walls(x0, ground + 1, z0, x1, ground + 8, z1, p.wall());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 8, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 9, 5, 0);
        door(b, p, cx + 230, ground + 1, z1, Direction.SOUTH);
        sign(b, p, cx + 230, ground + 6, z1, Direction.SOUTH,
                "RAIL STATION", "CALLAO");
        b.fill(cx + 216, ground + 1, cz - 230, cx + 222, ground + 1, cz - 230, p.accent());
        b.put(cx + 218, ground + 1, cz - 228, BOOKSHELF);
        b.put(cx + 220, ground + 1, cz - 228, BARREL);
        b.chest(cx + 220, ground + 1, cz - 232, SiteLoot.COMMON);
        b.put(cx + 230, ground + 1, cz - 226, p.light());

        b.fill(cx + 212, ground, z1 + 1, cx + 252, ground, z1 + 6, p.foundation());
        b.fill(cx + 212, ground + 1, z1 + 6, cx + 252, ground + 1, z1 + 6, p.rail());
        for (int x = cx + 216; x <= cx + 248; x += 8) {
            b.fill(x, ground + 1, z1 + 2, x, ground + 3, z1 + 2, p.accent());
            b.put(x, ground + 4, z1 + 2, p.light());
        }

        for (int x = cx + 140; x <= cx + 292; x++) {
            b.fill(x, ground, z1 + 8, x, ground, z1 + 12, p.rubble());
            if ((x & 3) == 0) {
                b.put(x, ground, z1 + 10, p.accent());
            }
            b.put(x, ground + 1, z1 + 9, RAIL);
            b.put(x, ground + 1, z1 + 11, RAIL);
        }
        b.fill(cx + 292, ground + 1, z1 + 10, cx + 292, ground + 4, z1 + 10, p.accent());
        b.put(cx + 292, ground + 5, z1 + 10, p.light());

        int towerGround = s.at(cx + 255, cz - 197);
        b.room(cx + 252, towerGround, cz - 200, cx + 258, towerGround + 8, cz - 194);
        b.walls(cx + 252, towerGround + 1, cz - 200, cx + 258, towerGround + 7, cz - 194,
                p.wall());
        b.fill(cx + 252, towerGround + 8, cz - 200, cx + 258, towerGround + 8, cz - 194,
                p.roofSlab());
        b.put(cx + 255, towerGround + 9, cz - 197, p.light());
        b.put(cx + 253, towerGround + 1, cz - 195, BARREL);
    }

    // ------------------------------------------------------------------ coast and tomb

    private static void coast(StructureBuilder b, int cx, int cz, CitySurface s, int quay,
                              Palette p) {
        // The dusty road from the city to the headland.
        path(b, s, cx + 120, cz + 160, cx + 200, cz + 220, 5, p.rubble());
        path(b, s, cx + 200, cz + 220, cx + 240, cz + 246, 5, p.rubble());

        // The cove south of Callao, continuous with the harbour water.
        b.fill(cx + 302, quay - 4, cz + 160, cx + 334, quay - 4, cz + 300, SAND);
        b.fill(cx + 302, quay - 3, cz + 160, cx + 334, quay - 1, cz + 300, WATER);
        b.fill(cx + 300, quay - 4, cz + 160, cx + 301, quay + 2, cz + 300, p.foundation());
        b.fill(cx + 300, quay + 2, cz + 160, cx + 300, quay + 2, cz + 300, p.weathered());
        b.fill(cx + 296, quay - 1, cz + 160, cx + 299, quay - 1, cz + 200, SAND);

        // The cliffs and the tomb.
        cliff(b, cx + 258, cz + 220, cx + 286, cz + 246, s, quay, p);
        cliff(b, cx + 268, cz + 246, cx + 300, cz + 272, s, quay, p);
        cliff(b, cx + 258, cz + 272, cx + 292, cz + 300, s, quay, p);
        tomb(b, cx + 240, cz + 260, s, quay, p);
        ship(b, cx + 318, cz + 220, quay, p, true);

        int cliffTop = Math.max(s.at(cx + 272, cz + 233), quay) + 7;
        signPost(b, cx + 236, cz + 244, s.at(cx + 236, cz + 244), p, Direction.NORTH,
                "TOMB", "KEEP OUT");
        signPost(b, cx + 282, cz + 240, cliffTop, p, Direction.WEST,
                "CLIFF EDGE", "KEEP BACK");
    }

    private static void cliff(StructureBuilder b, int x0, int z0, int x1, int z1,
                              CitySurface s, int quay, Palette p) {
        int base = Math.max(s.at((x0 + x1) / 2, (z0 + z1) / 2), quay);
        for (int tier = 0; tier < 4; tier++) {
            int inset = tier * 2;
            if (x0 + inset > x1 - inset || z0 + inset > z1 - inset) {
                break;
            }
            b.fill(x0 + inset, base + tier * 2, z0 + inset,
                    x1 - inset, base + tier * 2 + 1, z1 - inset,
                    tier == 0 ? p.foundation() : STONE);
        }
        for (int x = x0; x <= x1; x += 6) {
            b.put(x, base, z0, p.rubble());
            b.put(x, base, z1, p.rubble());
        }
    }

    private static void tomb(StructureBuilder b, int cx, int cz, CitySurface s, int quay,
                             Palette p) {
        int base = Math.max(b.groundY(cx, cz - 12), quay + 2);
        int x0 = cx - 12;
        int x1 = cx + 12;
        int z0 = cz - 10;
        int z1 = cz + 10;
        int top = base + 8;

        pad(b, cx - 16, cz - 14, cx + 16, cz + 14, base, p);
        b.fill(x0 - 1, base - 1, z0 - 1, x1 + 1, base, z1 + 1, p.foundation());
        b.room(x0, base, z0, x1, top, z1, new Doorway(Side.N, (x1 - x0) / 2));
        b.walls(x0, base + 1, z0, x1, top - 1, z1, STONE_BRICKS);
        quoins(b, x0, z0, x1, z1, base + 1, top - 1, p);
        b.fill(x0 - 1, top, z0 - 1, x1 + 1, top, z1 + 1, p.roofSlab());
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, top + 1);
        b.fill(x0 + 3, top + 1, z0 + 3, x1 - 3, top + 2, z1 - 3, p.roof());
        b.put(cx, top + 3, cz, p.light());
        b.fill(cx - 4, base + 1, z0, cx - 4, base + 5, z0, p.accent());
        b.fill(cx + 4, base + 1, z0, cx + 4, base + 5, z0, p.accent());
        b.fill(cx - 5, base + 6, z0, cx + 5, base + 6, z0, p.accent());
        door(b, p, cx, base + 1, z0, Direction.NORTH);
        sign(b, p, cx, base + 5, z0, Direction.NORTH, "TOMB", "KEEP OUT");

        // The stair shaft down into the crypt.
        for (int i = 0; i < 5; i++) {
            int z = z0 + 1 + i;
            int floor = base - 1 - i;
            b.fill(cx - 2, floor, z, cx + 2, base + 4, z, AIR);
            b.fill(cx - 2, floor, z, cx + 2, floor, z, p.foundation());
            b.fill(cx - 3, floor, z, cx - 3, base + 4, z, STONE_BRICKS);
            b.fill(cx + 3, floor, z, cx + 3, base + 4, z, STONE_BRICKS);
        }
        b.fill(cx - 3, base + 1, z0 + 1, cx - 3, base + 1, z0 + 5, p.rail());
        b.fill(cx + 3, base + 1, z0 + 1, cx + 3, base + 1, z0 + 5, p.rail());

        int floor = base - 4;
        int ceiling = base;
        b.fill(cx - 8, floor, cz - 4, cx + 8, ceiling, cz + 8, AIR);
        b.walls(cx - 9, floor, cz - 5, cx + 9, ceiling, cz + 9, STONE_BRICKS);
        b.fill(cx - 2, floor, cz - 5, cx + 2, floor + 2, cz - 5, AIR);
        b.fill(cx - 8, floor, cz - 4, cx + 8, floor, cz + 8, p.foundation());
        b.fill(cx - 8, ceiling, cz - 4, cx + 8, ceiling, cz + 8, STONE_BRICKS);
        b.fill(cx - 2, floor + 1, cz + 4, cx + 2, floor + 2, cz + 7, SANDSTONE);
        b.put(cx, floor + 3, cz + 5, p.accent());
        b.chest(cx - 6, floor + 1, cz, SiteLoot.RELIC);
        b.chest(cx + 6, floor + 1, cz, SiteLoot.CULT);
        b.chest(cx, floor + 1, cz + 8, SiteLoot.COMMON);
        sign(b, p, cx - 9, floor + 2, cz + 2, Direction.EAST, "OLD PRIEST", "QUIPU HERE");
        sign(b, p, cx + 9, floor + 2, cz + 2, Direction.WEST, "THE FIND", "TAKE IT?");
        b.put(cx - 7, floor + 1, cz - 3, p.light());
        b.put(cx + 7, floor + 1, cz - 3, p.light());
        b.put(cx - 7, floor + 1, cz + 7, p.light());
        b.put(cx + 7, floor + 1, cz + 7, p.light());
    }
}
