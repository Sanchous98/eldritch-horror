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
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.RailShape;

/**
 * Authored 1930s Los Angeles: the white-stucco Spanish mission with its bell tower, arcaded
 * cloister and palm courtyard; Hollywood's studio lot, boulevard theatres and cafés; the downtown
 * grid of offices, banks, shops and a deco hotel; the oil fields with derricks, tanks, sheds and a
 * rail station; the beach pier, pavilion, huts and promenade; and the studio annex cellar where the
 * missing starlet's reel is hidden.
 *
 * <p>Everything is written through {@link StructureBuilder}. There is no RNG, no chunk-local read
 * and no shared prefab: every decision is absolute and identical in every chunk that stamps the
 * city. Coordinates are relative to {@code (cx, ground, cz)} and stay inside {@code [-340, 340]}.
 * The flat authored heart uses {@code ground}; the graded rim (arrival gate, beach shore, pier,
 * hillside sign, rail approach) uses the shared {@link CityPlanContext#surface()} level.
 */
public final class LosAngelesPlan implements CityPlan {

    /** Authored half-extent; all geometry stays inside +/-340. */
    private static final int HALF = 340;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState OAK = Blocks.OAK_PLANKS.defaultBlockState();
    private static final BlockState OAK_LOG = Blocks.OAK_LOG.defaultBlockState();
    private static final BlockState DARK_OAK = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private static final BlockState PALM_LOG = Blocks.JUNGLE_LOG.defaultBlockState();
    private static final BlockState PALM_LEAF = Blocks.JUNGLE_LEAVES.defaultBlockState();
    private static final BlockState CYPRESS_LOG = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState CYPRESS_LEAF = Blocks.SPRUCE_LEAVES.defaultBlockState();
    private static final BlockState HEDGE = Blocks.OAK_LEAVES.defaultBlockState();
    private static final BlockState IRON = Blocks.IRON_BLOCK.defaultBlockState();
    private static final BlockState GOLD = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final BlockState GLASS = Blocks.GLASS.defaultBlockState();
    private static final BlockState NEON = Blocks.SEA_LANTERN.defaultBlockState();
    private static final BlockState BOOKSHELF = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState BARREL = Blocks.BARREL.defaultBlockState();
    private static final BlockState CAULDRON = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState HAY = Blocks.HAY_BLOCK.defaultBlockState();
    private static final BlockState WHEEL = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
    private static final BlockState RAIL_EW = Blocks.RAIL.defaultBlockState()
            .setValue(BlockStateProperties.RAIL_SHAPE, RailShape.EAST_WEST);

    @Override
    public String cityId() {
        return "los_angeles";
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
        mission(b, cx, cz, ground, p);
        hollywood(b, cx, cz, ground, p, surface);
        downtown(b, cx, cz, ground, p);
        oilFields(b, cx, cz, ground, p, surface);
        beach(b, cx, cz, ground, p, surface);
        expositionPark(b, cx, cz, ground, p);
        southSide(b, cx, cz, ground, p, surface);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.LANDMARK, 0, 0, 320, "gate"),
                new CityPoi("gate_signpost", CityPoi.Kind.SIGN, -18, 1, 314,
                        "MISSION|HOLLYWOOD|DOWNTOWN|OIL FIELDS"),
                new CityPoi("gate_signpost_beach", CityPoi.Kind.SIGN, 18, 1, 314,
                        "BEACH|CITY NOTICES"),
                new CityPoi("city_notices", CityPoi.Kind.SIGN, 22, 1, 324, "CITY NOTICES"),
                new CityPoi("mission", CityPoi.Kind.LANDMARK, 0, 0, -200,
                        "church,bell tower,cloister,palms"),
                new CityPoi("mission_sign", CityPoi.Kind.SIGN, -36, 1, -128, "MISSION"),
                new CityPoi("mission_garden", CityPoi.Kind.LANDMARK, -75, 0, -210,
                        "cypress,graves,ruin"),
                new CityPoi("palm_court", CityPoi.Kind.LANDMARK, 0, 0, -150,
                        "plaza,fountain"),
                new CityPoi("hollywood_sign", CityPoi.Kind.LANDMARK, -200, 0, -238,
                        "hillside letters"),
                new CityPoi("hollywood_boulevard", CityPoi.Kind.LANDMARK, -240, 0, -90,
                        "theatres,cafes,neon"),
                new CityPoi("hollywood_quest_board", CityPoi.Kind.QUEST_BOARD, -220, 1, 0,
                        "los_angeles_missing_starlet,los_angeles_cult_studio"),
                new CityPoi("quest_sign_missing", CityPoi.Kind.SIGN, -224, 1, 4,
                        "MISSING STARLET"),
                new CityPoi("quest_sign_cult", CityPoi.Kind.SIGN, -216, 1, 4, "CULT STUDIO"),
                new CityPoi("hotel", CityPoi.Kind.INN, -80, 0, -60, "hotel_clerk"),
                new CityPoi("hotel_clerk", CityPoi.Kind.NPC, -64, 0, -50, "hotel"),
                new CityPoi("hollywood_shop", CityPoi.Kind.SHOP, -240, 0, -60, "shopkeeper"),
                new CityPoi("shopkeeper", CityPoi.Kind.NPC, -240, 0, -52, "hollywood_shop"),
                new CityPoi("apothecary", CityPoi.Kind.HEALER, -200, 0, 120, "remedies"),
                new CityPoi("apothecary_npc", CityPoi.Kind.NPC, -200, 0, 120, "apothecary"),
                new CityPoi("studio", CityPoi.Kind.LANDMARK, -240, 0, 80,
                        "soundstages,missing starlet"),
                new CityPoi("director", CityPoi.Kind.NPC, -236, 0, 80,
                        "los_angeles_missing_starlet"),
                new CityPoi("studio_basement", CityPoi.Kind.SECRET, -200, -5, 80,
                        "chests,lore,hidden set"),
                new CityPoi("oil_office", CityPoi.Kind.NPC, 240, 0, 0,
                        "oilman,los_angeles_cult_studio"),
                new CityPoi("oilman", CityPoi.Kind.NPC, 250, 0, -6, "oil_office"),
                new CityPoi("oil_station", CityPoi.Kind.STATION, 175, 0, -265,
                        "rail station, oil fields"),
                new CityPoi("derrick_field", CityPoi.Kind.LANDMARK, 240, 0, -120,
                        "derricks,tanks,sheds"),
                new CityPoi("beach_pier", CityPoi.Kind.LANDMARK, 240, 0, 240,
                        "pier,pavilion"),
                new CityPoi("beach_port", CityPoi.Kind.PORT, 240, 0, 326,
                        "pier head, harbour"),
                new CityPoi("lifeguard", CityPoi.Kind.NPC, 200, 0, 250, "beach"),
                new CityPoi("beach_sign", CityPoi.Kind.SIGN, 150, 1, 236, "BEACH"),
                new CityPoi("beach_warning", CityPoi.Kind.SIGN, 210, 1, 246,
                        "RIP TIDE|NO SWIMMING"),
                new CityPoi("pier_sign", CityPoi.Kind.SIGN, 240, 1, 244, "PIER|PAVILION"),
                new CityPoi("downtown_sign", CityPoi.Kind.SIGN, -60, 1, -54, "DOWNTOWN"),
                new CityPoi("hollywood_signpost", CityPoi.Kind.SIGN, -260, 1, -54,
                        "HOLLYWOOD"),
                new CityPoi("oil_sign", CityPoi.Kind.SIGN, 150, 1, -54, "OIL FIELDS"),
                new CityPoi("studio_warning", CityPoi.Kind.SIGN, -238, 1, 84,
                        "STAGE 4 CLOSED|KEEP OUT"),
                new CityPoi("city_hall", CityPoi.Kind.LANDMARK, -39, 0, 132, "city hall"),
                new CityPoi("bank", CityPoi.Kind.LANDMARK, -50, 0, 40, "bank,vault"),
                new CityPoi("cop", CityPoi.Kind.NPC, -30, 0, 20, "los_angeles_cult_studio"),
                new CityPoi("rose_garden", CityPoi.Kind.LANDMARK, -75, 0, 235,
                        "roses,pergola,fountain"),
                new CityPoi("museum", CityPoi.Kind.LANDMARK, 65, 0, 227, "museum,rotunda"));
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

    /** A wall-mounted sign with a small bracket underneath it. */
    private static void sign(StructureBuilder b, Palette p, int x, int y, int z,
                             Direction facing, String... lines) {
        b.put(x, y - 1, z, p.accent());
        CityPlanSupport.sign(b, x, y, z, facing, lines);
    }

    /** A freestanding signpost: two timber blocks and the sign on top. */
    private static void signPost(StructureBuilder b, int x, int z, int y, Palette p,
                                 Direction facing, String... lines) {
        b.put(x, y + 1, z, p.accent());
        b.put(x, y + 2, z, p.accent());
        CityPlanSupport.sign(b, x, y + 3, z, facing, lines);
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

    private static void bed(StructureBuilder b, int x, int y, int z, Direction facing) {
        BlockState foot = Blocks.BED.pick(DyeColor.RED).defaultBlockState()
                .setValue(BlockStateProperties.BED_PART, BedPart.FOOT)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        b.put(x, y, z, foot);
        b.put(x + facing.getStepX(), y, z + facing.getStepZ(), foot
                .setValue(BlockStateProperties.BED_PART, BedPart.HEAD));
    }

    private static void ladder(StructureBuilder b, int x, int y, int z, Direction facing,
                               int height) {
        BlockState state = Blocks.LADDER.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        for (int i = 0; i < height; i++) {
            b.put(x, y + i, z, state);
        }
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

    private static void lanternPost(StructureBuilder b, int x, int z, int y, Palette p) {
        b.put(x, y + 1, z, p.accent());
        b.put(x, y + 2, z, p.accent());
        b.put(x, y + 3, z, p.light());
    }

    private static void palm(StructureBuilder b, int x, int z, int y) {
        int height = 5 + Math.floorMod(x * 5 + z * 11, 3);
        for (int i = 1; i <= height; i++) {
            b.put(x, y + i, z, PALM_LOG);
        }
        int top = y + height;
        b.fill(x - 2, top, z, x + 2, top, z, PALM_LEAF);
        b.fill(x, top, z - 2, x, top, z + 2, PALM_LEAF);
        b.fill(x - 1, top + 1, z - 1, x + 1, top + 1, z + 1, PALM_LEAF);
        b.put(x, top + 2, z, PALM_LEAF);
    }

    private static void cypress(StructureBuilder b, int x, int z, int y) {
        int height = 7 + Math.floorMod(x * 3 + z * 7, 3);
        for (int i = 1; i <= height; i++) {
            b.put(x, y + i, z, CYPRESS_LOG);
        }
        for (int i = 2; i <= height; i++) {
            b.put(x, y + i, z, CYPRESS_LEAF);
            if (i < height - 2) {
                b.put(x - 1, y + i, z, CYPRESS_LEAF);
                b.put(x + 1, y + i, z, CYPRESS_LEAF);
                b.put(x, y + i, z - 1, CYPRESS_LEAF);
                b.put(x, y + i, z + 1, CYPRESS_LEAF);
            }
        }
        b.put(x, y + height + 1, z, CYPRESS_LEAF);
    }

    private static void bench(StructureBuilder b, int x, int z, int y, Palette p,
                              Direction facing) {
        BlockState stair = p.roofStairs()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        b.put(x, y + 1, z, stair);
        b.put(x + 1, y + 1, z, stair);
        b.put(x, y + 2, z, p.rail());
        b.put(x + 1, y + 2, z, p.rail());
    }

    private static void crate(StructureBuilder b, int x, int z, int y, int height) {
        for (int i = 1; i <= height; i++) {
            b.put(x, y + i, z, Blocks.OAK_PLANKS.defaultBlockState());
        }
        b.put(x, y + height + 1, z, Blocks.OAK_SLAB.defaultBlockState());
    }

    private static void car(StructureBuilder b, int x, int z, int y, int color) {
        BlockState body = paint(color);
        b.fill(x, y + 1, z, x + 4, y + 1, z + 1, body);
        b.fill(x + 1, y + 2, z, x + 3, y + 2, z + 1, body);
        b.put(x + 1, y + 2, z, GLASS);
        b.put(x + 3, y + 2, z, GLASS);
        b.put(x, y, z, WHEEL);
        b.put(x + 4, y, z, WHEEL);
        b.put(x, y, z + 1, WHEEL);
        b.put(x + 4, y, z + 1, WHEEL);
    }

    private static void awning(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                               BlockState state) {
        b.fill(x0, y, z0, x1, y, z1, state);
        b.fill(x0, y - 1, z1, x1, y - 1, z1, state);
    }

    private static void fenceLine(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                                  Palette p) {
        b.fill(x0, y, z0, x1, y, z0, p.rail());
        if (z1 != z0) {
            b.fill(x0, y, z1, x1, y, z1, p.rail());
        }
        if (x1 != x0) {
            b.fill(x0, y, z0, x0, y, z1, p.rail());
            b.fill(x1, y, z0, x1, y, z1, p.rail());
        }
    }

    private static void windowX(StructureBuilder b, Palette p, int x, int y, int z,
                                int height, int width) {
        for (int dx = 0; dx < width; dx++) {
            for (int dy = 0; dy < height; dy++) {
                boolean border = (height >= 3 && (dy == 0 || dy == height - 1))
                        || (width >= 3 && (dx == 0 || dx == width - 1));
                b.put(x + dx, y + dy, z, border ? p.frame() : p.window());
            }
        }
        b.fill(x - 1, y - 1, z, x + width, y - 1, z, p.roofSlab());
    }

    private static void windowZ(StructureBuilder b, Palette p, int x, int y, int z,
                                int height, int width) {
        for (int dz = 0; dz < width; dz++) {
            for (int dy = 0; dy < height; dy++) {
                boolean border = (height >= 3 && (dy == 0 || dy == height - 1))
                        || (width >= 3 && (dz == 0 || dz == width - 1));
                b.put(x, y + dy, z + dz, border ? p.frame() : p.window());
            }
        }
        b.fill(x, y - 1, z - 1, x, y - 1, z + width, p.roofSlab());
    }

    private static void fountain(StructureBuilder b, int x, int z, int y, Palette p) {
        b.fill(x - 4, y, z - 4, x + 4, y, z + 4, p.foundation());
        b.fill(x - 3, y, z - 3, x + 3, y, z + 3, WATER);
        b.fill(x - 4, y + 1, z - 4, x + 4, y + 1, z - 4, p.accent());
        b.fill(x - 4, y + 1, z + 4, x + 4, y + 1, z + 4, p.accent());
        b.fill(x - 4, y + 1, z - 3, x - 4, y + 1, z + 3, p.accent());
        b.fill(x + 4, y + 1, z - 3, x + 4, y + 1, z + 3, p.accent());
        b.fill(x - 1, y + 1, z - 1, x + 1, y + 2, z + 1, p.accent());
        b.put(x, y + 3, z, p.light());
    }

    private static void headstone(StructureBuilder b, int x, int z, int y, Palette p, int i) {
        b.put(x, y + 1, z, p.foundation());
        b.put(x, y + 2, z, p.accent());
        if ((i & 1) == 0) {
            b.put(x - 1, y + 2, z, p.accent());
            b.put(x + 1, y + 2, z, p.accent());
        }
    }

    private static BlockState paint(int i) {
        return switch (Math.floorMod(i, 6)) {
            case 0 -> Materials.concrete(DyeColor.RED);
            case 1 -> Materials.concrete(DyeColor.BLUE);
            case 2 -> Materials.concrete(DyeColor.YELLOW);
            case 3 -> Materials.concrete(DyeColor.GREEN);
            case 4 -> Materials.concrete(DyeColor.ORANGE);
            default -> Materials.concrete(DyeColor.LIGHT_GRAY);
        };
    }

    private static BlockState wool(int i) {
        return switch (Math.floorMod(i, 6)) {
            case 0 -> Materials.wool(DyeColor.RED);
            case 1 -> Materials.wool(DyeColor.CYAN);
            case 2 -> Materials.wool(DyeColor.YELLOW);
            case 3 -> Materials.wool(DyeColor.LIME);
            case 4 -> Materials.wool(DyeColor.ORANGE);
            default -> Materials.wool(DyeColor.WHITE);
        };
    }

    private static BlockState flower(int i) {
        return switch (Math.floorMod(i, 5)) {
            case 0 -> Blocks.POPPY.defaultBlockState();
            case 1 -> Blocks.DANDELION.defaultBlockState();
            case 2 -> Blocks.CORNFLOWER.defaultBlockState();
            case 3 -> Blocks.AZURE_BLUET.defaultBlockState();
            default -> Blocks.OXEYE_DAISY.defaultBlockState();
        };
    }

    // ------------------------------------------------------------------ streets

    private static void streets(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        CityPlanSupport.path(b, cx, cz + 310, cx, cz - 120, 14, ground, p.ground());
        CityPlanSupport.path(b, cx - 310, cz - 60, cx + 310, cz - 60, 14, ground, p.ground());

        CityPlanSupport.path(b, cx - 120, cz + 20, cx + 120, cz + 20, 8, ground, p.ground());
        CityPlanSupport.path(b, cx - 120, cz + 100, cx + 120, cz + 100, 8, ground, p.ground());
        CityPlanSupport.path(b, cx - 80, cz - 80, cx - 80, cz + 180, 8, ground, p.ground());
        CityPlanSupport.path(b, cx + 80, cz - 80, cx + 80, cz + 180, 8, ground, p.ground());

        CityPlanSupport.path(b, cx - 310, cz - 240, cx - 310, cz + 160, 10, ground, p.ground());
        CityPlanSupport.path(b, cx - 230, cz - 240, cx - 230, cz + 160, 10, ground, p.ground());
        CityPlanSupport.path(b, cx - 160, cz - 240, cx - 160, cz + 160, 10, ground, p.ground());
        CityPlanSupport.path(b, cx - 320, cz - 220, cx - 140, cz - 220, 10, ground, p.ground());
        CityPlanSupport.path(b, cx - 320, cz - 140, cx - 140, cz - 140, 10, ground, p.ground());
        CityPlanSupport.path(b, cx - 320, cz + 40, cx - 140, cz + 40, 10, ground, p.ground());
        CityPlanSupport.path(b, cx - 320, cz + 140, cx - 140, cz + 140, 10, ground, p.ground());

        CityPlanSupport.path(b, cx + 180, cz - 250, cx + 180, cz + 120, 6, ground, p.ground());
        CityPlanSupport.path(b, cx + 240, cz - 250, cx + 240, cz + 120, 6, ground, p.ground());
        CityPlanSupport.path(b, cx + 300, cz - 250, cx + 300, cz + 120, 6, ground, p.ground());
        CityPlanSupport.path(b, cx + 140, cz - 200, cx + 315, cz - 200, 6, ground, p.ground());
        CityPlanSupport.path(b, cx + 140, cz - 100, cx + 315, cz - 100, 6, ground, p.ground());
        CityPlanSupport.path(b, cx + 140, cz, cx + 315, cz, 6, ground, p.ground());
        CityPlanSupport.path(b, cx + 140, cz + 100, cx + 315, cz + 100, 6, ground, p.ground());

        CityPlanSupport.path(b, cx + 140, cz + 120, cx + 140, cz + 240, 8, ground, p.ground());
    }

    // ------------------------------------------------------------------ arrival gate

    private static void arrivalGate(StructureBuilder b, int cx, int cz, int ground, Palette p,
                                    IntBinaryOperator surface) {
        int gz = cz + 320;
        int gy = Math.max(ground, surface.applyAsInt(cx, gz));
        for (int dx = -26; dx <= 26; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                int x = cx + dx;
                int z = gz + dz;
                int s = surface.applyAsInt(x, z);
                if (s < gy) {
                    b.fill(x, s + 1, z, x, gy - 1, z, p.foundation());
                } else if (s > gy) {
                    b.fill(x, gy + 1, z, x, s, z, AIR);
                }
                b.put(x, gy, z, p.ground());
            }
        }
        for (int side = -1; side <= 1; side += 2) {
            int px = cx + side * 12;
            b.fill(px - 2, gy + 1, gz - 2, px + 2, gy + 8, gz + 2, p.wall());
            b.fill(px - 3, gy + 1, gz - 3, px + 3, gy + 2, gz + 3, p.foundation());
            b.fill(px - 3, gy + 7, gz - 3, px + 3, gy + 8, gz + 3, p.accent());
            b.put(px, gy + 9, gz, p.light());
            b.fill(px - 1, gy + 5, gz - 2, px + 1, gy + 6, gz + 2, p.frame());
        }
        b.fill(cx - 12, gy + 7, gz - 2, cx + 12, gy + 8, gz + 2, p.accent());
        b.fill(cx - 10, gy + 7, gz - 2, cx + 10, gy + 7, gz - 2, p.wall());
        CityPlanSupport.sign(b, cx, gy + 9, gz - 2, Direction.SOUTH, "LOS ANGELES");
        signPost(b, cx - 18, gz - 6, gy, p,
                Direction.SOUTH, "MISSION", "HOLLYWOOD", "DOWNTOWN", "OIL FIELDS");
        signPost(b, cx + 18, gz - 6, gy, p,
                Direction.SOUTH, "BEACH", "CITY NOTICES");
        noticeBoard(b, cx + 22, gz + 4, gy, p);
        for (int dx = -20; dx <= 20; dx += 10) {
            palm(b, cx + dx, gz + 8, gy);
        }
    }

    private static void noticeBoard(StructureBuilder b, int x, int z, int y, Palette p) {
        b.fill(x, y + 1, z, x, y + 3, z, p.accent());
        b.fill(x + 4, y + 1, z, x + 4, y + 3, z, p.accent());
        b.fill(x, y + 3, z, x + 4, y + 3, z, p.frame());
        b.fill(x + 1, y + 4, z, x + 3, y + 4, z, p.roofSlab());
        b.put(x + 1, y + 2, z, p.wall());
        b.put(x + 2, y + 2, z, p.wall());
        b.put(x + 3, y + 2, z, p.wall());
        b.put(x + 2, y + 1, z, p.wall());
        CityPlanSupport.sign(b, x + 2, y + 5, z, Direction.SOUTH, "CITY NOTICES");
    }

    // ------------------------------------------------------------------ mission

    private static void mission(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        missionPlaza(b, cx, cz, ground, p);
        church(b, cx, cz - 200, ground, p);
        bellTower(b, cx - 20, cz - 192, ground, p);
        cloister(b, cx + 46, cz - 206, ground, p);
        missionGarden(b, cx - 75, cz - 210, ground, p);
        adobeRow(b, cx + 80, cz - 212, ground, p);
        for (int dz = -176; dz <= -120; dz += 8) {
            palm(b, cx - 12, cz + dz, ground);
            palm(b, cx + 12, cz + dz, ground);
        }
        signPost(b, cx - 40, cz - 172, ground, p, Direction.EAST, "MISSION");
    }

    private static void church(StructureBuilder b, int x, int z, int ground, Palette p) {
        int y = ground;
        int x0 = x - 16;
        int x1 = x + 16;
        int z0 = z - 28;
        int z1 = z + 12;
        b.fill(x0 - 1, y, z0 - 1, x1 + 1, y, z1 + 1, p.foundation());
        b.walls(x0, y + 1, z0, x1, y + 13, z1, p.wall());
        b.fill(x0 + 1, y + 2, z0 + 1, x1 - 1, y + 12, z1 - 1, AIR);
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 1, z1 - 1, p.ground());
        for (int bz = z0 + 4; bz <= z1 - 4; bz += 6) {
            b.buttress(x0, bz, y + 1, 5, StructureBuilder.Side.W);
            b.buttress(x1, bz, y + 1, 5, StructureBuilder.Side.E);
            windowZ(b, p, x0, y + 5, bz, 5, 2);
            windowZ(b, p, x1, y + 5, bz, 5, 2);
        }
        for (int pz = z0 + 5; pz <= z1 - 8; pz += 3) {
            b.fill(x - 10, y + 2, pz, x - 4, y + 2, pz, p.frame());
            b.fill(x + 4, y + 2, pz, x + 10, y + 2, pz, p.frame());
            b.put(x - 10, y + 3, pz, p.rail());
            b.put(x + 10, y + 3, pz, p.rail());
        }
        b.fill(x - 6, y + 2, z0 + 2, x + 6, y + 3, z0 + 5, p.foundation());
        b.put(x, y + 4, z0 + 3, p.accent());
        b.put(x, y + 5, z0 + 3, p.light());
        door(b, p, x, y + 1, z1, Direction.SOUTH);
        windowX(b, p, x - 5, y + 8, z1, 5, 10);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y + 13, 16, 1);
        b.fill(x - 10, y + 14, z1 + 1, x + 10, y + 18, z1 + 1, p.wall());
        b.fill(x - 7, y + 19, z1 + 1, x + 7, y + 19, z1 + 1, p.wall());
        b.fill(x - 3, y + 20, z1 + 1, x + 3, y + 20, z1 + 1, p.accent());
        b.put(x, y + 21, z1 + 1, p.light());
        sign(b, p, x + 10, y + 3, z1, Direction.SOUTH, "MISSION");
    }

    private static void bellTower(StructureBuilder b, int x, int z, int ground, Palette p) {
        int y = ground;
        int x0 = x - 4;
        int x1 = x + 4;
        int z0 = z - 4;
        int z1 = z + 4;
        b.fill(x0, y, z0, x1, y, z1, p.foundation());
        b.walls(x0, y + 1, z0, x1, y + 27, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 26, z1 - 1, AIR);
        b.fill(x0, y + 1, z0, x1, y + 1, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, y + 1, y + 27, p);
        for (int oy = y + 19; oy <= y + 23; oy++) {
            b.put(x, oy, z0, AIR);
            b.put(x, oy, z1, AIR);
            b.put(x0, oy, z, AIR);
            b.put(x1, oy, z, AIR);
        }
        b.fill(x0 - 1, y + 18, z0 - 1, x1 + 1, y + 18, z1 + 1, p.accent());
        b.fill(x0 - 1, y + 17, z0 - 1, x1 + 1, y + 17, z1 + 1, p.frame());
        b.put(x, y + 24, z, GOLD);
        b.put(x, y + 25, z, p.light());
        for (int i = 0; i <= 4; i++) {
            b.fill(x0 + i, y + 28 + i, z0 + i, x1 - i, y + 28 + i, z1 - i,
                    i == 4 ? p.accent() : p.roof());
        }
        b.put(x, y + 33, z, p.light());
        ladder(b, x, y + 1, z0 + 1, Direction.SOUTH, 24);
        door(b, p, x, y + 1, z1, Direction.SOUTH);
        sign(b, p, x1, y + 3, z1, Direction.SOUTH, "BELL TOWER");
    }

    private static void cloister(StructureBuilder b, int x, int z, int ground, Palette p) {
        int y = ground;
        int x0 = x - 22;
        int x1 = x + 22;
        int z0 = z - 22;
        int z1 = z + 22;
        b.fill(x0 - 1, y, z0 - 1, x1 + 1, y, z1 + 1, p.foundation());
        b.walls(x0, y + 1, z0, x1, y + 5, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 5, z1 - 1, AIR);
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 1, z1 - 1, p.ground());
        int cx0 = x0 + 7;
        int cx1 = x1 - 7;
        int cz0 = z0 + 7;
        int cz1 = z1 - 7;
        for (int px = cx0; px <= cx1; px += 4) {
            b.fill(px, y + 1, cz0, px, y + 4, cz0, p.accent());
            b.fill(px, y + 1, cz1, px, y + 4, cz1, p.accent());
            b.put(px, y + 5, cz0, p.roofSlab());
            b.put(px, y + 5, cz1, p.roofSlab());
        }
        for (int pz = cz0; pz <= cz1; pz += 4) {
            b.fill(cx0, y + 1, pz, cx0, y + 4, pz, p.accent());
            b.fill(cx1, y + 1, pz, cx1, y + 4, pz, p.accent());
            b.put(cx0, y + 5, pz, p.roofSlab());
            b.put(cx1, y + 5, pz, p.roofSlab());
        }
        b.fill(x0, y + 6, z0, x1, y + 6, z1, p.roof());
        b.fill(cx0 + 1, y + 6, cz0 + 1, cx1 - 1, y + 6, cz1 - 1, AIR);
        b.fill(x0, y + 7, z0, x1, y + 7, z0, p.accent());
        b.fill(x0, y + 7, z1, x1, y + 7, z1, p.accent());
        b.fill(x0, y + 7, z0, x0, y + 7, z1, p.accent());
        b.fill(x1, y + 7, z0, x1, y + 7, z1, p.accent());
        fountain(b, x, z, y + 1, p);
        for (int dx = -10; dx <= 10; dx += 10) {
            for (int dz = -10; dz <= 10; dz += 10) {
                if (dx != 0 || dz != 0) {
                    palm(b, x + dx, z + dz, y + 1);
                }
            }
        }
        for (int dx = -14; dx <= 14; dx += 7) {
            b.fill(x + dx, y + 1, cz0 + 2, x + dx, y + 1, cz0 + 4, HEDGE);
            b.fill(x + dx, y + 1, cz1 - 4, x + dx, y + 1, cz1 - 2, HEDGE);
        }
        door(b, p, x, y + 1, z1, Direction.SOUTH);
        CityPlanSupport.sign(b, x + 8, y + 3, z1, Direction.SOUTH, "PALM COURT");
    }

    private static void missionPlaza(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int y = ground;
        int z = cz - 150;
        pave(b, cx - 44, z - 28, cx + 44, z + 28, y, p.ground());
        outline(b, cx - 44, z - 28, cx + 44, z + 28, y, p.foundation());
        fountain(b, cx, z, y, p);
        for (int dx = -36; dx <= 36; dx += 12) {
            palm(b, cx + dx, z - 24, y);
            palm(b, cx + dx, z + 24, y);
        }
        for (int dx = -24; dx <= 24; dx += 12) {
            bench(b, cx + dx, z - 12, y, p, Direction.NORTH);
            bench(b, cx + dx, z + 12, y, p, Direction.SOUTH);
        }
        signPost(b, cx - 36, z + 20, y, p, Direction.SOUTH, "MISSION", "BELL TOWER");
        signPost(b, cx + 40, z - 26, y, p, Direction.WEST, "PALM COURT");
        for (int dx = -40; dx <= 40; dx += 8) {
            b.fill(cx + dx, y + 1, z - 27, cx + dx, y + 1, z - 27, flower(dx));
            b.fill(cx + dx, y + 1, z + 27, cx + dx, y + 1, z + 27, flower(dx + 1));
        }
    }

    private static void missionGarden(StructureBuilder b, int x, int z, int ground, Palette p) {
        int y = ground;
        int x0 = x - 35;
        int x1 = x + 35;
        int z0 = z - 40;
        int z1 = z + 30;
        b.fill(x0, y, z0, x1, y, z1, p.ground());
        fenceLine(b, x0, z0, x1, z1, y + 1, p);
        pave(b, x0 + 2, z - 3, x1 - 2, z + 3, y, p.rubble());
        pave(b, x - 3, z0 + 2, x + 3, z1 - 2, y, p.rubble());
        for (int dx = -30; dx <= 30; dx += 10) {
            cypress(b, x + dx, z0 + 6, y);
            cypress(b, x + dx, z1 - 6, y);
        }
        for (int i = 0; i < 12; i++) {
            int hx = x - 27 + i % 4 * 18;
            int hz = z - 24 + i / 4 * 20;
            headstone(b, hx, hz, y, p, i);
        }
        int rx = x + 18;
        int rz = z - 26;
        b.fill(rx - 8, y + 1, rz - 8, rx + 8, y + 6, rz - 8, p.wall());
        b.fill(rx - 8, y + 1, rz + 8, rx + 8, y + 6, rz + 8, p.wall());
        b.fill(rx - 8, y + 1, rz - 8, rx - 8, y + 6, rz + 8, p.wall());
        b.fill(rx + 8, y + 1, rz - 8, rx + 8, y + 6, rz + 8, p.wall());
        b.fill(rx - 7, y + 1, rz - 7, rx + 7, y + 6, rz + 7, AIR);
        for (int i = 0; i < 8; i++) {
            int gx = rx - 7 + i;
            if ((i % 3) != 0) {
                b.fill(gx, y + 1, rz - 8, gx, y + 6, rz - 8, p.weathered());
            }
        }
        b.pitchedRoof(rx - 9, rz - 9, rx + 9, rz + 9, y + 6, 6, 0);
        b.put(rx, y + 13, rz, p.accent());
        signPost(b, x, z1 + 2, y, p, Direction.SOUTH, "MISSION GARDEN");
        signPost(b, x - 28, z + 26, y, p, Direction.EAST, "GRAVES", "RUIN");
    }

    private static void adobeRow(StructureBuilder b, int x, int z, int ground, Palette p) {
        for (int i = 0; i < 3; i++) {
            int bx = x + i * 26;
            adobeHouse(b, bx, z, ground, p, i);
        }
    }

    private static void adobeHouse(StructureBuilder b, int x, int z, int ground, Palette p,
                                   int index) {
        int y = ground;
        b.fill(x - 7, y, z - 7, x + 7, y, z + 7, p.foundation());
        b.walls(x - 7, y + 1, z - 7, x + 7, y + 6, z + 7, p.wall());
        b.fill(x - 6, y + 1, z - 6, x + 6, y + 6, z + 6, AIR);
        b.fill(x - 6, y + 1, z - 6, x + 6, y + 1, z + 6, p.ground());
        b.fill(x - 8, y + 7, z - 8, x + 8, y + 7, z + 8, p.roof());
        b.fill(x - 7, y + 8, z - 7, x + 7, y + 8, z + 7, p.roofSlab());
        door(b, p, x, y + 1, z + 7, Direction.SOUTH);
        windowX(b, p, x - 4, y + 3, z + 7, 2, 3);
        b.fill(x + 8, y + 1, z - 6, x + 11, y + 1, z + 6, p.ground());
        b.fill(x + 11, y + 1, z - 6, x + 11, y + 2, z + 6, p.foundation());
        palm(b, x + 10, z + 5, y);
        crate(b, x - 5, z + 5, y + 1, 1);
        sign(b, p, x + 4, y + 3, z + 7, Direction.SOUTH,
                index == 0 ? "ADOBE" : index == 1 ? "SCHOOL" : "STORE");
    }

    // ------------------------------------------------------------------ Hollywood

    private static void hollywood(StructureBuilder b, int cx, int cz, int ground, Palette p,
                                  IntBinaryOperator surface) {
        hollywoodSign(b, cx - 200, cz - 238, ground, p, surface);
        theatres(b, cx, cz, ground, p);
        cafes(b, cx, cz, ground, p);
        questBoard(b, cx, cz, ground, p);
        bungalowCourt(b, cx, cz, ground, p);
        hollywoodHotel(b, cx, cz, ground, p);
        moviePalace(b, cx, cz, ground, p);
        studioLot(b, cx, cz, ground, p);
        studioAnnex(b, cx, cz, ground, p);
        cellar(b, cx, cz, ground, p);
        apothecary(b, cx, cz, ground, p);
        signPost(b, cx - 260, cz - 54, ground, p, Direction.SOUTH, "HOLLYWOOD");
        signPost(b, cx - 150, cz - 54, ground, p, Direction.SOUTH, "HOLLYWOOD");
        for (int dz = -200; dz <= 120; dz += 40) {
            if (dz == 40) {
                continue;
            }
            palm(b, cx - 302, cz + dz, ground);
            palm(b, cx - 238, cz + dz, ground);
        }
    }

    private static void hollywoodSign(StructureBuilder b, int x, int z, int ground, Palette p,
                                      IntBinaryOperator surface) {
        int y = Math.max(ground, surface.applyAsInt(x, z));
        b.fill(x - 20, y, z - 2, x + 20, y, z + 2, p.ground());
        for (int side = -1; side <= 1; side += 2) {
            b.fill(x + side * 20, y + 1, z, x + side * 20, y + 8, z, p.accent());
        }
        for (int dx = -18; dx <= 18; dx++) {
            int h = 3 + Math.floorMod(dx * 3, 5);
            b.fill(x + dx, y + 1, z, x + dx, y + h, z, p.wall());
            if (Math.floorMod(dx, 5) == 0) {
                b.put(x + dx, y + h, z, p.accent());
            }
        }
        b.fill(x - 22, y + 9, z, x + 22, y + 9, z, p.accent());
        b.fill(x - 18, y + 10, z, x + 18, y + 10, z, p.accent());
        for (int dx = -16; dx <= 16; dx += 16) {
            b.fill(x + dx, y + 1, z - 2, x + dx, y + 6, z - 2, p.foundation());
        }
        signPost(b, x, z + 6, y, p, Direction.SOUTH, "HOLLYWOOD");
    }

    private static void theatres(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        theatre(b, cx - 290, cz - 105, ground, p, 0, "CHINESE");
        theatre(b, cx - 254, cz - 105, ground, p, 1, "EGYPTIAN");
        theatre(b, cx - 210, cz - 105, ground, p, 2, "PANTAGES");
        theatre(b, cx - 178, cz - 105, ground, p, 3, "MOVIES");
    }

    private static void theatre(StructureBuilder b, int x, int z, int ground, Palette p,
                                int index, String name) {
        int y = ground;
        int x0 = x - 14;
        int x1 = x + 14;
        int z0 = z - 23;
        int z1 = z + 23;
        b.fill(x0 - 1, y, z0 - 1, x1 + 1, y, z1 + 3, p.ground());
        b.fill(x0, y, z0, x1, y, z1, p.foundation());
        b.walls(x0, y + 1, z0, x1, y + 12, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 12, z1 - 1, AIR);
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 1, z1 - 1, p.frame());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y + 12, 14, 1);
        b.fill(x0 + 2, y + 13, z1 + 1, x1 - 2, y + 15, z1 + 1, p.accent());
        b.fill(x0 + 4, y + 16, z1 + 1, x1 - 4, y + 16, z1 + 1, p.accent());
        b.fill(x0 + 2, y + 4, z1 + 1, x1 - 2, y + 6, z1 + 1, p.frame());
        b.fill(x0 + 3, y + 5, z1 + 1, x1 - 3, y + 5, z1 + 1, NEON);
        b.fill(x0 + 3, y + 7, z1 + 2, x1 - 3, y + 8, z1 + 4, p.accent());
        for (int dx = x0 + 4; dx <= x1 - 4; dx += 5) {
            b.put(dx, y + 7, z1 + 4, p.light());
        }
        door(b, p, x, y + 1, z1, Direction.SOUTH);
        windowX(b, p, x - 10, y + 3, z1, 3, 6);
        windowX(b, p, x + 4, y + 3, z1, 3, 6);
        for (int dx = -10; dx <= 10; dx += 5) {
            b.fill(x + dx, y + 1, z1 + 2, x + dx, y + 1, z1 + 2,
                    index == 0 ? GOLD : p.foundation());
        }
        b.put(x - 10, y + 2, z1 + 2, GOLD);
        b.put(x + 10, y + 2, z1 + 2, GOLD);
        b.fill(x - 3, y + 1, z1 + 3, x + 3, y + 1, z1 + 3, p.foundation());
        b.fill(x - 3, y + 2, z1 + 3, x + 3, y + 2, z1 + 3, p.frame());
        b.put(x - 2, y + 3, z1 + 3, p.light());
        b.put(x + 2, y + 3, z1 + 3, p.light());
        signPost(b, x, z1 + 6, y, p, Direction.SOUTH, name);
        if (index == 0) {
            for (int dx = -8; dx <= 8; dx += 4) {
                b.put(x + dx, y + 1, z1 + 3, p.accent());
            }
        }
    }

    private static void cafes(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        cafe(b, cx - 292, cz - 40, ground, p, 0, "CAFE");
        cafe(b, cx - 268, cz - 40, ground, p, 1, "DINER");
        cafe(b, cx - 248, cz - 40, ground, p, 2, "SHOP");
        cafe(b, cx - 210, cz - 40, ground, p, 3, "RECORDS");
        cafe(b, cx - 180, cz - 40, ground, p, 4, "CAFE");
    }

    private static void cafe(StructureBuilder b, int x, int z, int ground, Palette p,
                             int index, String name) {
        int y = ground;
        int x0 = x - 11;
        int x1 = x + 11;
        int z0 = z - 10;
        int z1 = z + 10;
        b.fill(x0 - 1, y, z0 - 1, x1 + 1, y, z1 + 3, p.ground());
        b.walls(x0, y + 1, z0, x1, y + 8, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 7, z1 - 1, AIR);
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 1, z1 - 1, p.frame());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y + 8, 6, 0);
        door(b, p, x, y + 1, z1, Direction.SOUTH);
        windowX(b, p, x0 + 2, y + 3, z1, 3, 5);
        windowX(b, p, x1 - 6, y + 3, z1, 3, 5);
        awning(b, x0, z1 + 1, x1, z1 + 4, y + 5, wool(index));
        for (int dx = x0 + 1; dx <= x1 - 1; dx += 4) {
            b.put(dx, y + 4, z1 + 4, p.light());
        }
        b.fill(x - 5, y + 1, z1 + 5, x - 3, y + 1, z1 + 5, p.frame());
        b.fill(x + 3, y + 1, z1 + 5, x + 5, y + 1, z1 + 5, p.frame());
        b.put(x - 4, y + 2, z1 + 5, p.roofSlab());
        b.put(x + 4, y + 2, z1 + 5, p.roofSlab());
        sign(b, p, x + 4, y + 3, z1, Direction.SOUTH, name);
        b.fill(x0 + 1, y + 1, z0 + 1, x0 + 3, y + 1, z0 + 3, p.frame());
        b.put(x0 + 2, y + 2, z0 + 2, BARREL);
        b.put(x1 - 2, y + 2, z1 - 2, p.light());
        car(b, x - 2, z1 + 6, y, index);
    }

    private static void questBoard(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x = cx - 214;
        int z = cz;
        int y = ground;
        pave(b, x - 12, z - 20, x + 12, z + 20, y, p.ground());
        outline(b, x - 12, z - 20, x + 12, z + 20, y, p.foundation());
        for (int dx = -12; dx <= 12; dx += 12) {
            palm(b, x + dx, z - 16, y);
            palm(b, x + dx, z + 16, y);
        }
        b.fill(x - 8, y + 1, z - 2, x - 8, y + 4, z - 2, p.accent());
        b.fill(x + 8, y + 1, z - 2, x + 8, y + 4, z - 2, p.accent());
        b.fill(x - 8, y + 4, z - 2, x + 8, y + 5, z - 2, p.frame());
        b.fill(x - 9, y + 6, z - 3, x + 9, y + 6, z - 1, p.roofSlab());
        b.fill(x - 7, y + 2, z - 2, x + 7, y + 3, z - 2, p.wall());
        b.put(x - 7, y + 2, z - 3, p.frame());
        b.put(x + 7, y + 2, z - 3, p.frame());
        b.fill(x - 4, y + 1, z + 2, x - 4, y + 2, z + 2, p.accent());
        b.fill(x + 4, y + 1, z + 2, x + 4, y + 2, z + 2, p.accent());
        CityPlanSupport.sign(b, x - 4, y + 3, z + 2, Direction.SOUTH,
                "MISSING", "STARLET", "REWARD");
        CityPlanSupport.sign(b, x + 4, y + 3, z + 2, Direction.SOUTH,
                "CULT", "STUDIO", "REWARD");
        signPost(b, x, z - 6, y, p, Direction.SOUTH, "HOLLYWOOD", "QUEST BOARD");
        bench(b, x - 10, z + 8, y, p, Direction.NORTH);
        bench(b, x + 10, z + 8, y, p, Direction.NORTH);
        b.put(x, y + 7, z - 2, p.light());
    }

    private static void bungalowCourt(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x = cx - 287;
        int z = cz + 4;
        pave(b, x - 16, z - 18, x + 16, z + 18, ground, p.ground());
        for (int i = 0; i < 3; i++) {
            int bx = x - 12 + i * 12;
            bungalow(b, bx, z - 10, ground, p, i);
        }
        fountain(b, x, z + 8, ground, p);
        palm(b, x - 14, z + 12, ground);
        palm(b, x + 14, z + 12, ground);
        car(b, x + 4, z + 2, ground, 2);
        signPost(b, x, z - 16, ground, p, Direction.SOUTH, "BUNGALOWS");
    }

    private static void bungalow(StructureBuilder b, int x, int z, int ground, Palette p,
                                 int index) {
        int y = ground;
        b.fill(x - 5, y, z - 6, x + 5, y, z + 6, p.foundation());
        b.walls(x - 5, y + 1, z - 6, x + 5, y + 5, z + 6, p.wall());
        b.fill(x - 4, y + 1, z - 5, x + 4, y + 5, z + 5, AIR);
        b.fill(x - 4, y + 1, z - 5, x + 4, y + 1, z + 5, p.frame());
        b.pitchedRoof(x - 6, z - 7, x + 6, z + 7, y + 5, 5, 0);
        door(b, p, x, y + 1, z + 6, Direction.SOUTH);
        windowX(b, p, x - 3, y + 2, z + 6, 2, 2);
        windowX(b, p, x + 1, y + 2, z + 6, 2, 2);
        b.fill(x - 4, y + 1, z + 7, x + 4, y + 1, z + 9, p.ground());
        awning(b, x - 4, z + 7, x + 4, z + 9, y + 4, wool(index));
        palm(b, x + 4, z + 11, y);
        sign(b, p, x + 3, y + 3, z + 6, Direction.SOUTH, "HOME");
    }

    private static void hollywoodHotel(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x = cx - 264;
        int z = cz + 4;
        int y = ground;
        b.fill(x - 11, y, z - 15, x + 11, y, z + 15, p.foundation());
        b.walls(x - 11, y + 1, z - 15, x + 11, y + 16, z + 15, p.wall());
        b.fill(x - 10, y + 1, z - 14, x + 10, y + 16, z + 14, AIR);
        for (int floor = 0; floor < 4; floor++) {
            int fy = y + 1 + floor * 4;
            b.fill(x - 10, fy, z - 14, x + 10, fy, z + 14, p.frame());
            b.fill(x - 9, fy + 1, z - 13, x + 9, fy + 3, z + 13, AIR);
        }
        for (int floor = 0; floor < 4; floor++) {
            int fy = y + 2 + floor * 4;
            windowX(b, p, x - 8, fy, z + 15, 2, 4);
            windowX(b, p, x + 4, fy, z + 15, 2, 4);
            windowZ(b, p, x - 11, fy, z - 10, 2, 4);
            windowZ(b, p, x - 11, fy, z + 6, 2, 4);
        }
        b.fill(x - 12, y + 17, z - 16, x + 12, y + 17, z + 16, p.roof());
        b.fill(x - 11, y + 18, z - 15, x + 11, y + 18, z + 15, p.roofSlab());
        b.fill(x - 11, y + 18, z - 15, x + 11, y + 18, z - 15, p.accent());
        b.fill(x - 11, y + 18, z + 15, x + 11, y + 18, z + 15, p.accent());
        door(b, p, x, y + 1, z + 15, Direction.SOUTH);
        awning(b, x - 4, z + 16, x + 4, z + 19, y + 4, wool(1));
        b.fill(x - 4, y + 5, z + 16, x + 4, y + 5, z + 16, p.accent());
        b.fill(x - 2, y + 6, z + 16, x + 2, y + 6, z + 16, NEON);
        sign(b, p, x - 2, y + 3, z + 15, Direction.SOUTH, "HOTEL");
        b.fill(x - 10, y + 1, z - 14, x - 6, y + 1, z - 8, p.frame());
        b.put(x - 8, y + 2, z - 10, p.light());
        b.put(x - 10, y + 2, z - 14, BARREL);
        fountain(b, x, z + 4, y, p);
        palm(b, x - 8, z + 10, y);
        palm(b, x + 8, z + 10, y);
        car(b, x + 14, z + 10, y, 1);
    }

    private static void moviePalace(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x = cx - 182;
        int z = cz + 4;
        int y = ground;
        b.fill(x - 11, y, z - 15, x + 11, y, z + 15, p.foundation());
        b.walls(x - 11, y + 1, z - 15, x + 11, y + 14, z + 15, p.wall());
        b.fill(x - 10, y + 1, z - 14, x + 10, y + 14, z + 14, AIR);
        b.fill(x - 10, y + 1, z - 14, x + 10, y + 1, z + 14, p.frame());
        b.pitchedRoof(x - 12, z - 16, x + 12, z + 16, y + 14, 7, 1);
        door(b, p, x, y + 1, z + 15, Direction.SOUTH);
        windowX(b, p, x - 7, y + 3, z + 15, 4, 5);
        windowX(b, p, x + 2, y + 3, z + 15, 4, 5);
        b.fill(x - 8, y + 9, z + 16, x + 8, y + 10, z + 16, NEON);
        b.fill(x - 9, y + 11, z + 16, x + 9, y + 11, z + 16, p.accent());
        b.fill(x - 2, y + 12, z + 16, x + 2, y + 12, z + 16, p.frame());
        sign(b, p, x - 1, y + 3, z + 15, Direction.SOUTH, "PALACE");
        car(b, x - 16, z + 10, y, 3);
        palm(b, x + 16, z + 10, y);
    }

    private static void studioLot(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 304;
        int x1 = cx - 238;
        int z0 = cz + 50;
        int z1 = cz + 130;
        int y = ground;
        b.fill(x0, y, z0, x1, y, z1, p.ground());
        b.walls(x0, y + 1, z0, x1, y + 4, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 4, z1 - 1, AIR);
        b.fill(x1 - 1, y + 1, z0 + 34, x1, y + 4, z0 + 46, AIR);
        b.fill(x1, y + 5, z0 + 32, x1, y + 5, z0 + 48, p.accent());
        b.fill(x0, y + 5, z0, x1, y + 5, z0, p.accent());
        b.fill(x0, y + 5, z1, x1, y + 5, z1, p.accent());
        b.fill(x0, y + 5, z0, x0, y + 5, z1, p.accent());
        b.fill(x1, y + 5, z0, x1, y + 5, z1, p.accent());
        signPost(b, x1 + 2, z0 + 40, y, p, Direction.EAST, "STUDIO", "GATE");
        b.fill(x1 + 1, y + 1, z0 + 36, x1 + 1, y + 4, z0 + 37, p.accent());
        b.fill(x1 + 1, y + 1, z0 + 43, x1 + 1, y + 4, z0 + 44, p.accent());
        soundStage(b, cx - 288, cz + 70, y, p, 0, "STAGE 1");
        soundStage(b, cx - 288, cz + 106, y, p, 1, "STAGE 2");
        soundStage(b, cx - 262, cz + 70, y, p, 2, "STAGE 3");
        backlot(b, cx - 262, cz + 106, y, p);
        waterTower(b, cx - 292, cz + 124, y, p);
        filmLight(b, cx - 280, cz + 92, y);
        filmLight(b, cx - 274, cz + 94, y);
        filmLight(b, cx - 268, cz + 92, y);
        car(b, cx - 296, cz + 96, y, 0);
        car(b, cx - 246, cz + 60, y, 4);
        crate(b, cx - 300, cz + 128, y, 2);
        crate(b, cx - 298, cz + 128, y, 1);
        b.fill(cx - 250, y + 1, cz + 92, cx - 250, y + 3, cz + 92, p.accent());
        sign(b, p, cx - 250, y + 4, cz + 92, Direction.EAST, "STAGE 4 CLOSED");
    }

    private static void soundStage(StructureBuilder b, int x, int z, int y, Palette p,
                                   int index, String name) {
        int x0 = x - 12;
        int x1 = x + 12;
        int z0 = z - 14;
        int z1 = z + 14;
        b.fill(x0, y, z0, x1, y, z1, p.foundation());
        b.walls(x0, y + 1, z0, x1, y + 14, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 14, z1 - 1, AIR);
        b.fill(x0, y + 15, z0, x1, y + 15, z1, p.roof());
        b.fill(x0 + 1, y + 16, z0 + 1, x1 - 1, y + 16, z1 - 1, p.roofSlab());
        b.fill(x0, y + 17, z0, x1, y + 17, z0, p.accent());
        b.fill(x0, y + 17, z1, x1, y + 17, z1, p.accent());
        b.fill(x0, y + 17, z0, x0, y + 17, z1, p.accent());
        b.fill(x1, y + 17, z0, x1, y + 17, z1, p.accent());
        b.fill(x0 + 3, y + 1, z1, x0 + 9, y + 6, z1, p.frame());
        b.fill(x0 + 4, y + 1, z1 + 1, x0 + 8, y + 5, z1 + 1, DARK_OAK);
        b.fill(x0 + 4, y + 6, z1 + 1, x0 + 8, y + 6, z1 + 1, p.accent());
        b.fill(x1 - 9, y + 1, z1, x1 - 3, y + 6, z1, p.frame());
        b.fill(x1 - 8, y + 1, z1 + 1, x1 - 4, y + 5, z1 + 1, DARK_OAK);
        for (int dx = x0 + 2; dx <= x1 - 2; dx += 5) {
            b.put(dx, y + 9, z0, p.window());
            b.put(dx, y + 12, z0, p.window());
        }
        b.put(x, y + 18, z, p.light());
        if (index == 1) {
            b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 13, z1 - 1, AIR);
            for (int dx = x0 + 2; dx <= x1 - 2; dx += 3) {
                b.put(dx, y + 1, z0 + 1, p.weathered());
            }
        }
        sign(b, p, x, y + 3, z1, Direction.SOUTH, name);
    }

    private static void backlot(StructureBuilder b, int x, int z, int y, Palette p) {
        int x0 = x - 12;
        int x1 = x + 12;
        int z0 = z - 12;
        int z1 = z + 12;
        b.fill(x0, y, z0, x1, y, z1, p.ground());
        for (int i = 0; i < 4; i++) {
            int bx = x0 + 2 + i * 6;
            b.walls(bx, y + 1, z0 + 2, bx + 4, y + 6, z0 + 8, p.wall());
            b.fill(bx + 1, y + 1, z0 + 3, bx + 3, y + 5, z0 + 7, AIR);
            b.pitchedRoof(bx - 1, z0 + 1, bx + 5, z0 + 9, y + 6, 3, 0);
            b.put(bx + 2, y + 2, z0 + 8, p.light());
        }
        b.fill(x0 + 1, y + 1, z1 - 2, x1 - 1, y + 1, z1 - 2, p.frame());
        b.fill(x0 + 1, y + 2, z1 - 2, x1 - 1, y + 2, z1 - 2, p.rail());
        b.fill(x0 + 2, y + 1, z1 - 1, x0 + 5, y + 1, z1 - 1, HAY);
        b.put(x0 + 3, y + 2, z1 - 1, BARREL);
        signPost(b, x, z1 + 2, y, p, Direction.SOUTH, "BACKLOT");
    }

    private static void waterTower(StructureBuilder b, int x, int z, int y, Palette p) {
        for (int dx = -3; dx <= 3; dx += 6) {
            for (int dz = -3; dz <= 3; dz += 6) {
                b.fill(x + dx, y + 1, z + dz, x + dx, y + 8, z + dz, OAK_LOG);
            }
        }
        b.fill(x - 3, y + 6, z - 3, x + 3, y + 6, z + 3, p.frame());
        b.fill(x - 3, y + 9, z - 3, x + 3, y + 12, z + 3, p.wall());
        b.fill(x - 2, y + 10, z - 2, x + 2, y + 11, z + 2, WATER);
        b.fill(x - 4, y + 13, z - 4, x + 4, y + 13, z + 4, p.roof());
        b.fill(x - 3, y + 14, z - 3, x + 3, y + 14, z + 3, p.roofSlab());
        b.put(x, y + 15, z, p.light());
        ladder(b, x - 3, y + 1, z, Direction.EAST, 8);
        signPost(b, x, z + 5, y, p, Direction.SOUTH, "STUDIO");
    }

    private static void filmLight(StructureBuilder b, int x, int z, int y) {
        b.put(x, y + 1, z, OAK_LOG);
        b.put(x, y + 2, z, OAK_LOG);
        b.put(x, y + 3, z, WHEEL);
        b.put(x, y + 4, z, NEON);
        b.put(x + 1, y + 4, z, WHEEL);
        b.put(x - 1, y + 4, z, WHEEL);
    }

    private static void studioAnnex(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 212;
        int x1 = cx - 188;
        int z0 = cz + 68;
        int z1 = cz + 92;
        int y = ground;
        b.fill(x0 - 1, y, z0 - 1, x1 + 1, y, z1 + 1, p.foundation());
        b.walls(x0, y + 1, z0, x1, y + 8, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 7, z1 - 1, AIR);
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 1, z1 - 1, p.ground());
        b.fill(cx - 196, y + 1, cz + 78, cx - 186, y + 1, cz + 82, AIR);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y + 9, 6, 0);
        door(b, p, cx - 200, y + 1, z1, Direction.SOUTH);
        windowX(b, p, x0 + 3, y + 3, z1, 3, 4);
        windowX(b, p, x0 + 12, y + 3, z1, 3, 4);
        sign(b, p, cx - 194, y + 3, z1, Direction.SOUTH, "ARCHIVE");
        b.put(cx - 190, y + 3, z1 + 1, p.light());
        b.fill(cx - 204, y + 1, z0 + 2, cx - 196, y + 1, z0 + 4, p.frame());
        b.put(cx - 202, y + 2, z0 + 3, CAULDRON);
        b.put(cx - 198, y + 2, z0 + 3, BOOKSHELF);
        signPost(b, cx - 206, z0 - 2, y, p, Direction.NORTH, "STUDIO", "ARCHIVE");
    }

    private static void cellar(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 216;
        int x1 = cx - 184;
        int z0 = cz + 64;
        int z1 = cz + 96;
        int floor = ground - 8;
        int ceiling = ground - 3;
        b.fill(x0, floor, z0, x1, floor, z1, p.foundation());
        b.walls(x0, floor + 1, z0, x1, ceiling, z1, p.wall());
        b.fill(x0 + 1, floor + 1, z0 + 1, x1 - 1, ceiling - 1, z1 - 1, AIR);
        b.fill(x0 + 1, ceiling, z0 + 1, x1 - 1, ceiling, z1 - 1, p.roof());
        b.fill(cx - 196, floor + 1, cz + 78, cx - 186, ceiling, cz + 82, AIR);
        for (int i = 0; i < 9; i++) {
            int sx = cx - 186 - i;
            int sy = ground + 1 - i;
            b.fill(sx, sy, cz + 79, sx, sy, cz + 80, p.roofStairs()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
            b.fill(sx, sy + 1, cz + 79, sx, sy + 4, cz + 80, AIR);
            b.put(sx, sy + 1, cz + 78, p.rail());
            b.put(sx, sy + 1, cz + 81, p.rail());
        }
        b.put(cx - 186, ground - 6, cz + 78, p.light());
        b.put(cx - 194, ground - 6, cz + 82, p.light());
        b.fill(x0 + 2, floor + 1, z0 + 2, x0 + 3, floor + 1, z0 + 3, p.frame());
        b.put(x0 + 2, floor + 2, z0 + 2, BARREL);
        b.put(x0 + 3, floor + 2, z0 + 3, BARREL);
        b.chest(x0 + 5, floor + 1, z0 + 6, SiteLoot.CULT);
        b.chest(x1 - 5, floor + 1, z1 - 6, SiteLoot.RELIC);
        b.chest(x0 + 5, floor + 1, z1 - 6, SiteLoot.COMMON);
        b.fill(cx - 208, floor + 1, cz + 84, cx - 198, floor + 1, cz + 92, p.frame());
        b.fill(cx - 208, floor + 2, cz + 92, cx - 198, floor + 3, cz + 92, p.wall());
        b.fill(cx - 208, floor + 2, cz + 84, cx - 198, floor + 3, cz + 84, p.wall());
        b.fill(cx - 208, floor + 2, cz + 84, cx - 208, floor + 3, cz + 92, p.wall());
        b.fill(cx - 198, floor + 2, cz + 84, cx - 198, floor + 3, cz + 92, p.wall());
        b.put(cx - 203, floor + 2, cz + 88, IRON);
        b.put(cx - 203, floor + 3, cz + 88, IRON);
        b.put(cx - 202, floor + 3, cz + 88, WHEEL);
        b.put(cx - 204, floor + 3, cz + 88, WHEEL);
        b.fill(cx - 212, floor + 1, cz + 72, cx - 209, floor + 2, cz + 75, p.frame());
        b.put(cx - 210, floor + 3, cz + 73, BARREL);
        b.put(cx - 211, floor + 1, cz + 70, p.light());
        b.fill(cx - 206, floor + 1, cz + 68, cx - 204, floor + 1, cz + 68, p.frame());
        b.put(cx - 205, floor + 2, cz + 68, p.accent());
        sign(b, p, cx - 208, floor + 2, cz + 76, Direction.SOUTH,
                "THE REEL", "IS REAL", "DO NOT SCREEN");
        for (int dx = x0 + 2; dx <= x1 - 2; dx += 6) {
            b.put(dx, floor + 1, z0 + 1, Palette.cobweb());
        }
        for (int dz = z0 + 4; dz <= z1 - 4; dz += 8) {
            b.put(x0 + 1, floor + 2, dz, p.rail());
            b.put(x1 - 1, floor + 2, dz, p.rail());
        }
        for (int tx = cx - 238; tx <= cx - 211; tx++) {
            b.fill(tx, floor + 1, cz + 78, tx, floor + 3, cz + 82, AIR);
            b.fill(tx, floor, cz + 78, tx, floor, cz + 82, p.foundation());
            b.fill(tx, floor + 4, cz + 78, tx, floor + 4, cz + 82, p.wall());
            b.fill(tx, floor + 1, cz + 78, tx, floor + 3, cz + 78, p.wall());
            b.fill(tx, floor + 1, cz + 82, tx, floor + 3, cz + 82, p.wall());
        }
        b.fill(cx - 240, floor + 1, cz + 78, cx - 240, floor + 3, cz + 82, p.wall());
        sign(b, p, cx - 239, floor + 2, cz + 80, Direction.EAST,
                "STUDIO", "BASEMENT", "KEEP OUT");
        b.put(cx - 220, floor + 4, cz + 80, p.light());
    }

    private static void apothecary(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 216;
        int x1 = cx - 184;
        int z0 = cz + 102;
        int z1 = cz + 128;
        int y = ground;
        b.fill(x0 - 1, y, z0 - 1, x1 + 1, y, z1 + 1, p.ground());
        b.walls(x0, y + 1, z0, x1, y + 9, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 8, z1 - 1, AIR);
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 1, z1 - 1, p.frame());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y + 9, 6, 0);
        door(b, p, cx - 200, y + 1, z1, Direction.SOUTH);
        windowX(b, p, x0 + 3, y + 3, z1, 3, 5);
        windowX(b, p, x0 + 17, y + 3, z1, 3, 5);
        awning(b, x0, z1 + 1, x1, z1 + 4, y + 6, wool(3));
        for (int dx = x0 + 2; dx <= x1 - 2; dx += 5) {
            b.put(dx, y + 5, z1 + 4, p.light());
        }
        b.fill(x0 + 2, y + 2, z0 + 2, x0 + 4, y + 2, z0 + 5, p.frame());
        b.put(x0 + 3, y + 3, z0 + 3, CAULDRON);
        b.put(x0 + 5, y + 2, z0 + 2, BARREL);
        b.put(x1 - 3, y + 2, z0 + 2, BOOKSHELF);
        b.put(x1 - 5, y + 2, z0 + 2, p.light());
        sign(b, p, cx - 192, y + 3, z1, Direction.SOUTH, "APOTHECARY");
        b.fill(x1 + 1, y, z1 - 6, x1 + 4, y, z1 - 2, p.ground());
        lanternPost(b, x1 + 3, z1 - 4, y, p);
        b.put(x1 + 2, y + 1, z1 - 4, Blocks.FLOWER_POT.defaultBlockState());
    }

    // ------------------------------------------------------------------ downtown

    private static void downtown(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        hotel(b, cx, cz, ground, p);
        bank(b, cx, cz, ground, p);
        officeTower(b, cx, cz, ground, p);
        shopRows(b, cx, cz, ground, p);
        cityHall(b, cx, cz, ground, p);
        decoCorner(b, cx, cz, ground, p);
        alley(b, cx, cz, ground, p);
        signPost(b, cx - 60, cz - 54, ground, p, Direction.SOUTH, "DOWNTOWN");
        signPost(b, cx + 30, cz - 54, ground, p, Direction.SOUTH, "BROADWAY");
        for (int dz = -120; dz <= 160; dz += 20) {
            if (dz == -60 || dz == 20 || dz == 100) {
                continue;
            }
            lanternPost(b, cx - 9, cz + dz, ground, p);
            lanternPost(b, cx + 9, cz + dz, ground, p);
        }
        for (int dz = -120; dz <= 160; dz += 40) {
            palm(b, cx - 12, cz + dz, ground);
            palm(b, cx + 12, cz + dz, ground);
        }
    }

    private static void hotel(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 74;
        int x1 = cx - 46;
        int z0 = cz - 52;
        int z1 = cz - 18;
        int y = ground;
        b.fill(x0 - 1, y, z0 - 1, x1 + 1, y, z1 + 1, p.foundation());
        b.walls(x0, y + 1, z0, x1, y + 20, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 20, z1 - 1, AIR);
        for (int floor = 0; floor < 5; floor++) {
            int fy = y + 1 + floor * 4;
            b.fill(x0 + 1, fy, z0 + 1, x1 - 1, fy, z1 - 1, p.frame());
            b.fill(x0 + 1, fy + 1, z0 + 1, x1 - 1, fy + 3, z1 - 1, AIR);
        }
        for (int floor = 0; floor < 5; floor++) {
            int fy = y + 2 + floor * 4;
            windowX(b, p, x0 + 3, fy, z1, 2, 5);
            windowX(b, p, x0 + 13, fy, z1, 2, 5);
            windowZ(b, p, x0, fy, z0 + 4, 2, 5);
            windowZ(b, p, x0, fy, z0 + 18, 2, 5);
            windowZ(b, p, x1, fy, z0 + 4, 2, 5);
            windowZ(b, p, x1, fy, z0 + 18, 2, 5);
        }
        b.fill(x0 - 1, y + 21, z0 - 1, x1 + 1, y + 21, z1 + 1, p.roof());
        b.fill(x0, y + 22, z0, x1, y + 22, z1, p.roofSlab());
        b.fill(x0, y + 22, z0, x1, y + 22, z0, p.accent());
        b.fill(x0, y + 22, z1, x1, y + 22, z1, p.accent());
        b.fill(x0, y + 22, z0, x0, y + 22, z1, p.accent());
        b.fill(x1, y + 22, z0, x1, y + 22, z1, p.accent());
        door(b, p, cx - 60, y + 1, z1, Direction.SOUTH);
        awning(b, x0 + 4, z1 + 1, x1 - 4, z1 + 4, y + 5, wool(0));
        b.fill(x0 + 6, y + 6, z1 + 1, x1 - 6, y + 6, z1 + 1, p.accent());
        b.fill(x0 + 8, y + 7, z1 + 1, x1 - 8, y + 7, z1 + 1, NEON);
        sign(b, p, cx - 56, y + 3, z1, Direction.SOUTH, "HOTEL");
        b.fill(x0 + 1, y + 1, z0 + 1, x0 + 6, y + 1, z0 + 6, p.frame());
        b.put(x0 + 3, y + 2, z0 + 3, p.light());
        bed(b, x0 + 3, y + 2, z0 + 9, Direction.SOUTH);
        b.put(x0 + 5, y + 2, z0 + 5, BARREL);
        b.fill(x1 - 8, y + 1, z0 + 2, x1 - 2, y + 2, z0 + 4, p.frame());
        b.put(x1 - 5, y + 3, z0 + 3, p.light());
        b.put(x1 - 3, y + 2, z0 + 6, BOOKSHELF);
        b.put(x0 + 2, y + 2, z1 - 2, Blocks.FLOWER_POT.defaultBlockState());
        signPost(b, cx - 80, cz - 58, y, p, Direction.SOUTH, "HOTEL", "ROOMS");
        car(b, cx - 90, cz - 48, y, 2);
    }

    private static void bank(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 70;
        int x1 = cx - 30;
        int z0 = cz + 28;
        int z1 = cz + 52;
        int y = ground;
        b.fill(x0 - 1, y, z0 - 1, x1 + 1, y, z1 + 1, p.foundation());
        b.walls(x0, y + 1, z0, x1, y + 15, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 15, z1 - 1, AIR);
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 1, z1 - 1, p.frame());
        for (int px = x0 + 3; px <= x1 - 3; px += 6) {
            b.fill(px, y + 1, z0 - 2, px, y + 9, z0 - 2, p.accent());
            b.put(px, y + 10, z0 - 2, p.roofSlab());
        }
        b.fill(x0 + 1, y + 10, z0 - 2, x1 - 1, y + 11, z0 - 2, p.foundation());
        b.fill(x0 + 1, y + 12, z0 - 2, x1 - 1, y + 12, z0 - 2, p.accent());
        b.fill(x0 + 5, y + 13, z0 - 2, x1 - 5, y + 13, z0 - 2, p.accent());
        b.pitchedRoof(x0, z0, x1, z1, y + 15, 7, 0);
        door(b, p, cx - 50, y + 1, z0, Direction.NORTH);
        windowX(b, p, x0 + 6, y + 4, z0, 4, 6);
        windowX(b, p, x1 - 12, y + 4, z0, 4, 6);
        b.fill(x0 + 2, y + 2, z1 - 4, x1 - 2, y + 2, z1 - 4, p.frame());
        b.fill(x0 + 2, y + 3, z1 - 4, x1 - 2, y + 3, z1 - 4, p.rail());
        b.fill(x0 + 2, y + 1, z1 - 6, x0 + 5, y + 3, z1 - 3, IRON);
        b.fill(x1 - 5, y + 1, z1 - 6, x1 - 2, y + 3, z1 - 3, IRON);
        b.put(cx - 50, y + 2, z1 - 5, p.light());
        b.chest(cx - 56, y + 2, z1 - 8, SiteLoot.ORDER);
        b.chest(cx - 44, y + 2, z1 - 8, SiteLoot.COMMON);
        signPost(b, cx - 50, cz + 24, y, p, Direction.NORTH, "BANK");
        signPost(b, cx - 76, cz + 40, y, p, Direction.EAST, "BANK", "VAULT");
        car(b, cx - 34, cz + 60, y, 5);
    }

    private static void officeTower(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 20;
        int x1 = cx + 70;
        int z0 = cz + 28;
        int z1 = cz + 52;
        int y = ground;
        b.fill(x0 - 1, y, z0 - 1, x1 + 1, y, z1 + 1, p.foundation());
        b.walls(x0, y + 1, z0, x1, y + 39, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 39, z1 - 1, AIR);
        for (int floor = 0; floor < 10; floor++) {
            int fy = y + 1 + floor * 4;
            b.fill(x0 + 1, fy, z0 + 1, x1 - 1, fy, z1 - 1, p.frame());
            b.fill(x0 + 1, fy + 1, z0 + 1, x1 - 1, fy + 3, z1 - 1, AIR);
            if (floor % 3 == 1) {
                b.fill(x0, fy + 3, z0, x1, fy + 3, z1, p.accent());
            }
        }
        for (int floor = 0; floor < 10; floor++) {
            int fy = y + 2 + floor * 4;
            for (int dx = x0 + 4; dx <= x1 - 4; dx += 6) {
                b.put(dx, fy, z0, p.window());
                b.put(dx, fy + 1, z0, p.window());
                b.put(dx, fy, z1, p.window());
                b.put(dx, fy + 1, z1, p.window());
            }
            for (int dz = z0 + 4; dz <= z1 - 4; dz += 6) {
                b.put(x0, fy, dz, p.window());
                b.put(x0, fy + 1, dz, p.window());
                b.put(x1, fy, dz, p.window());
                b.put(x1, fy + 1, dz, p.window());
            }
        }
        b.fill(x0, y + 40, z0, x1, y + 40, z1, p.roof());
        b.fill(x0 + 4, y + 41, z0 + 4, x1 - 4, y + 41, z1 - 4, p.roofSlab());
        b.fill(x0 + 10, y + 42, z0 + 6, x1 - 10, y + 42, z1 - 6, p.accent());
        b.fill(cx + 40, y + 43, cz + 40, cx + 40, y + 51, cz + 40, p.accent());
        b.put(cx + 40, y + 52, cz + 40, p.light());
        door(b, p, x0, y + 1, cz + 40, Direction.WEST);
        b.fill(x0 - 4, y, cz + 36, x0 - 1, y, cz + 44, p.ground());
        b.fill(x0 - 4, y + 4, cz + 36, x0 - 1, y + 4, cz + 44, p.accent());
        b.fill(x0 - 4, y + 1, cz + 36, x0 - 4, y + 3, cz + 36, p.accent());
        b.fill(x0 - 4, y + 1, cz + 44, x0 - 4, y + 3, cz + 44, p.accent());
        sign(b, p, x0, y + 3, cz + 38, Direction.WEST, "OFFICES");
        b.fill(x0 + 2, y + 1, z0 + 2, x0 + 8, y + 1, z0 + 8, p.frame());
        b.put(x0 + 5, y + 2, z0 + 5, p.light());
        b.put(x0 + 2, y + 2, z0 + 2, BOOKSHELF);
        b.put(x1 - 2, y + 2, z0 + 2, BARREL);
    }

    private static void shopRows(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int[] xs = {cx - 70, cx - 52, cx - 34, cx + 20, cx + 38, cx + 56};
        String[] names = {"SHOES", "BOOKS", "DRUGS", "CAFE", "TAILOR", "RADIO"};
        for (int i = 0; i < xs.length; i++) {
            shop(b, xs[i], cz + 80, ground, p, i, names[i]);
        }
    }

    private static void shop(StructureBuilder b, int x, int z, int ground, Palette p,
                             int index, String name) {
        int y = ground;
        int x0 = x - 8;
        int x1 = x + 8;
        int z0 = z - 12;
        int z1 = z + 12;
        b.fill(x0 - 1, y, z0 - 1, x1 + 1, y, z1 + 3, p.ground());
        b.walls(x0, y + 1, z0, x1, y + 9, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 8, z1 - 1, AIR);
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 1, z1 - 1, p.frame());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y + 9, 5, 0);
        door(b, p, x, y + 1, z1, Direction.SOUTH);
        windowX(b, p, x0 + 2, y + 3, z1, 3, 4);
        windowX(b, p, x1 - 6, y + 3, z1, 3, 4);
        awning(b, x0, z1 + 1, x1, z1 + 3, y + 5, wool(index));
        b.fill(x - 3, y + 6, z1 + 1, x + 3, y + 6, z1 + 1, p.accent());
        b.fill(x - 2, y + 6, z1 + 2, x + 2, y + 6, z1 + 2, NEON);
        sign(b, p, x + 1, y + 3, z1, Direction.SOUTH, name);
        b.fill(x0 + 2, y + 2, z0 + 2, x1 - 2, y + 2, z0 + 3, p.frame());
        b.put(x0 + 3, y + 3, z0 + 2, BARREL);
        b.put(x1 - 3, y + 2, z0 + 2, p.light());
        b.put(x0 + 2, y + 2, z1 - 2, Blocks.FLOWER_POT.defaultBlockState());
    }

    private static void cityHall(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 64;
        int x1 = cx - 14;
        int z0 = cz + 108;
        int z1 = cz + 156;
        int y = ground;
        b.fill(x0 - 1, y, z0 - 1, x1 + 1, y, z1 + 1, p.foundation());
        b.walls(x0, y + 1, z0, x1, y + 24, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 24, z1 - 1, AIR);
        for (int floor = 0; floor < 6; floor++) {
            int fy = y + 1 + floor * 4;
            b.fill(x0 + 1, fy, z0 + 1, x1 - 1, fy, z1 - 1, p.frame());
            b.fill(x0 + 1, fy + 1, z0 + 1, x1 - 1, fy + 3, z1 - 1, AIR);
        }
        for (int floor = 0; floor < 6; floor++) {
            int fy = y + 2 + floor * 4;
            windowX(b, p, x0 + 4, fy, z1, 2, 6);
            windowX(b, p, x0 + 30, fy, z1, 2, 6);
            windowZ(b, p, x1, fy, z0 + 6, 2, 6);
            windowZ(b, p, x1, fy, z0 + 28, 2, 6);
        }
        for (int pz = z0 + 6; pz <= z1 - 6; pz += 6) {
            b.fill(x1 + 1, y + 1, pz, x1 + 1, y + 10, pz, p.accent());
            b.put(x1 + 1, y + 11, pz, p.roofSlab());
        }
        b.fill(x1 + 1, y + 11, z0 + 6, x1 + 1, y + 12, z1 - 6, p.foundation());
        b.fill(x1 + 1, y + 13, z0 + 6, x1 + 1, y + 13, z1 - 6, p.accent());
        door(b, p, x1, y + 1, cz + 132, Direction.EAST);
        b.fill(x1 + 1, y + 1, cz + 128, x1 + 4, y + 1, cz + 136, p.ground());
        b.fill(x1 + 4, y + 1, cz + 128, x1 + 4, y + 2, cz + 136, p.foundation());
        b.fill(x0 + 10, y + 25, z0 + 8, x1 - 10, y + 25, z1 - 8, p.roof());
        b.fill(x0 + 10, y + 26, z0 + 8, x1 - 10, y + 26, z1 - 8, p.roofSlab());
        int tx = cx - 39;
        int tz = cz + 132;
        b.fill(tx - 6, y + 25, tz - 6, tx + 6, y + 34, tz + 6, p.wall());
        b.fill(tx - 5, y + 25, tz - 5, tx + 5, y + 33, tz + 5, AIR);
        b.fill(tx - 7, y + 35, tz - 7, tx + 7, y + 35, tz + 7, p.accent());
        b.fill(tx - 4, y + 36, tz - 4, tx + 4, y + 36, tz + 4, p.wall());
        b.fill(tx - 2, y + 37, tz - 2, tx + 2, y + 37, tz + 2, p.accent());
        b.put(tx, y + 38, tz, GOLD);
        b.put(tx, y + 39, tz, p.light());
        sign(b, p, x1, y + 3, cz + 126, Direction.EAST, "CITY HALL");
        fountain(b, cx - 39, cz + 166, y, p);
        for (int dx = -20; dx <= 20; dx += 10) {
            palm(b, cx - 39 + dx, cz + 170, y);
        }
        bench(b, cx - 52, cz + 162, y, p, Direction.SOUTH);
        bench(b, cx - 26, cz + 162, y, p, Direction.SOUTH);
    }

    private static void decoCorner(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 16;
        int x1 = cx + 66;
        int z0 = cz + 108;
        int z1 = cz + 156;
        int y = ground;
        b.fill(x0 - 1, y, z0 - 1, x1 + 1, y, z1 + 1, p.foundation());
        b.walls(x0, y + 1, z0, x1, y + 33, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 33, z1 - 1, AIR);
        for (int floor = 0; floor < 8; floor++) {
            int fy = y + 1 + floor * 4;
            b.fill(x0 + 1, fy, z0 + 1, x1 - 1, fy, z1 - 1, p.frame());
            b.fill(x0 + 1, fy + 1, z0 + 1, x1 - 1, fy + 3, z1 - 1, AIR);
            if (floor % 2 == 1) {
                b.fill(x0, fy + 3, z0, x1, fy + 3, z1, Materials.terracotta(
                        floor % 4 == 1 ? DyeColor.PINK : DyeColor.LIGHT_BLUE));
            }
        }
        for (int floor = 0; floor < 8; floor++) {
            int fy = y + 2 + floor * 4;
            for (int dx = x0 + 4; dx <= x1 - 4; dx += 7) {
                b.put(dx, fy, z1, p.window());
                b.put(dx, fy + 1, z1, p.window());
            }
            for (int dz = z0 + 4; dz <= z1 - 4; dz += 7) {
                b.put(x0, fy, dz, p.window());
                b.put(x0, fy + 1, dz, p.window());
            }
        }
        b.fill(x0, y + 34, z0, x1, y + 34, z1, p.roof());
        b.fill(x0 + 6, y + 35, z0 + 6, x1 - 6, y + 35, z1 - 6, p.roofSlab());
        b.fill(x0 + 12, y + 36, z0 + 12, x1 - 12, y + 36, z1 - 12, p.accent());
        b.fill(cx + 41, y + 37, cz + 132, cx + 41, y + 42, cz + 132, p.accent());
        b.put(cx + 41, y + 43, cz + 132, NEON);
        door(b, p, x0, y + 1, cz + 132, Direction.WEST);
        b.fill(x0 - 3, y + 1, cz + 128, x0 - 1, y + 1, cz + 136, p.ground());
        b.fill(x0 - 3, y + 4, cz + 128, x0 - 1, y + 4, cz + 136, p.accent());
        b.fill(x0 - 3, y + 1, cz + 128, x0 - 3, y + 3, cz + 128, p.accent());
        b.fill(x0 - 3, y + 1, cz + 136, x0 - 3, y + 3, cz + 136, p.accent());
        sign(b, p, x0, y + 3, cz + 125, Direction.WEST, "DECO");
        b.fill(x0 + 2, y + 1, z0 + 2, x0 + 8, y + 1, z0 + 8, p.frame());
        b.put(x0 + 5, y + 2, z0 + 5, p.light());
        b.put(x0 + 2, y + 2, z0 + 2, BOOKSHELF);
        b.put(x1 - 2, y + 2, z0 + 2, BARREL);
    }

    private static void alley(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        pave(b, cx - 76, cz + 56, cx + 76, cz + 62, ground, p.rubble());
        for (int dx = -70; dx <= 70; dx += 20) {
            crate(b, cx + dx, cz + 58, ground, 1);
            if (Math.abs(dx + 5) > 8) {
                lanternPost(b, cx + dx + 5, cz + 61, ground, p);
            }
        }
        car(b, cx - 20, cz + 57, ground, 3);
        b.fill(cx + 20, ground + 1, cz + 60, cx + 24, ground + 2, cz + 60, p.rail());
        b.fill(cx + 10, ground + 1, cz + 62, cx + 10, ground + 2, cz + 62, p.accent());
        CityPlanSupport.sign(b, cx + 10, ground + 3, cz + 62, Direction.SOUTH, "ALLEY");
    }

    // ------------------------------------------------------------------ oil fields

    private static void oilFields(StructureBuilder b, int cx, int cz, int ground, Palette p,
                                  IntBinaryOperator surface) {
        oilStation(b, cx, cz, ground, p, surface);
        int[][] derricks = {
                {160, -230}, {200, -190}, {250, -215}, {285, -165}, {155, -120},
                {205, -70}, {265, -90}, {310, -40}, {168, -20}, {225, 30},
                {285, 10}, {165, 70}, {255, 80}, {310, 90}
        };
        for (int i = 0; i < derricks.length; i++) {
            int dx = derricks[i][0];
            int dz = derricks[i][1];
            derrick(b, cx + dx, cz + dz, surface.applyAsInt(cx + dx, cz + dz), p, i);
        }
        storageTank(b, cx + 210, cz - 140, surface.applyAsInt(cx + 210, cz - 140), p);
        storageTank(b, cx + 280, cz - 130, surface.applyAsInt(cx + 280, cz - 130), p);
        storageTank(b, cx + 160, cz + 10, surface.applyAsInt(cx + 160, cz + 10), p);
        storageTank(b, cx + 290, cz + 60, surface.applyAsInt(cx + 290, cz + 60), p);
        storageTank(b, cx + 225, cz - 235, surface.applyAsInt(cx + 225, cz - 235), p);
        oilShed(b, cx + 165, cz - 160, surface.applyAsInt(cx + 165, cz - 160), p, 0);
        oilShed(b, cx + 215, cz - 245, surface.applyAsInt(cx + 215, cz - 245), p, 1);
        oilShed(b, cx + 270, cz - 185, surface.applyAsInt(cx + 270, cz - 185), p, 2);
        oilShed(b, cx + 200, cz + 90, surface.applyAsInt(cx + 200, cz + 90), p, 3);
        oilShed(b, cx + 312, cz + 110, surface.applyAsInt(cx + 312, cz + 110), p, 4);
        oilShed(b, cx + 150, cz + 30, surface.applyAsInt(cx + 150, cz + 30), p, 5);
        oilOffice(b, cx, cz, ground, p);
        pipeRack(b, cx + 220, cz - 160, cx + 220, cz - 130, ground, p);
        pipeRack(b, cx + 200, cz + 20, cx + 200, cz + 60, ground, p);
        for (int dx = 145; dx <= 315; dx += 10) {
            b.put(cx + dx, surface.applyAsInt(cx + dx, cz - 252) + 1, cz - 252, p.rail());
        }
        signPost(b, cx + 150, cz - 54, ground, p, Direction.SOUTH, "OIL FIELDS");
        signPost(b, cx + 230, cz - 10, ground, p, Direction.SOUTH, "OIL OFFICE");
        for (int dz = -240; dz <= 110; dz += 50) {
            palm(b, cx + 145, cz + dz, ground);
        }
    }

    private static void oilStation(StructureBuilder b, int cx, int cz, int ground, Palette p,
                                   IntBinaryOperator surface) {
        int z = cz - 260;
        levelTrack(b, cx + 140, cx + 335, z, ground, p);
        int y = ground + 1;
        for (int px = cx + 145; px <= cx + 205; px++) {
            for (int pz = cz - 268; pz <= cz - 262; pz++) {
                int s = surface.applyAsInt(px, pz);
                if (s < ground) {
                    b.fill(px, s + 1, pz, px, ground, pz, p.foundation());
                }
            }
        }
        b.fill(cx + 145, y, cz - 268, cx + 205, y, cz - 262, p.ground());
        b.fill(cx + 145, y - 1, cz - 268, cx + 205, y - 1, cz - 262, p.foundation());
        b.fill(cx + 145, y, cz - 268, cx + 205, y, cz - 268, p.accent());
        for (int dx = 150; dx <= 200; dx += 10) {
            b.fill(cx + dx, y + 1, cz - 268, cx + dx, y + 4, cz - 268, p.accent());
            b.put(cx + dx, y + 5, cz - 268, p.roofSlab());
        }
        b.fill(cx + 148, y + 5, cz - 270, cx + 202, y + 5, cz - 264, p.roof());
        for (int dx = 152; dx <= 198; dx += 12) {
            bench(b, cx + dx, cz - 265, y, p, Direction.NORTH);
        }
        int sx = cx + 163;
        int sz = cz - 278;
        int sy = ground;
        for (int px = sx - 17; px <= sx + 17; px++) {
            for (int pz = sz - 6; pz <= sz + 6; pz++) {
                int s = surface.applyAsInt(px, pz);
                if (s < sy) {
                    b.fill(px, s + 1, pz, px, sy, pz, p.foundation());
                }
            }
        }
        b.fill(sx - 17, sy, sz - 6, sx + 17, sy, sz + 6, p.foundation());
        b.walls(sx - 17, sy + 1, sz - 6, sx + 17, sy + 9, sz + 6, p.wall());
        b.fill(sx - 16, sy + 1, sz - 5, sx + 16, sy + 9, sz + 5, AIR);
        b.fill(sx - 16, sy + 1, sz - 5, sx + 16, sy + 1, sz + 5, p.frame());
        b.pitchedRoof(sx - 18, sz - 7, sx + 18, sz + 7, sy + 9, 7, 0);
        door(b, p, sx, sy + 1, sz + 6, Direction.SOUTH);
        windowX(b, p, sx - 12, sy + 3, sz + 6, 3, 5);
        windowX(b, p, sx + 7, sy + 3, sz + 6, 3, 5);
        b.fill(sx - 4, sy + 5, sz + 7, sx + 4, sy + 5, sz + 7, p.accent());
        sign(b, p, sx + 6, sy + 3, sz + 6, Direction.SOUTH, "OIL STATION");
        b.fill(sx - 14, sy + 2, sz - 4, sx - 8, sy + 2, sz - 2, p.frame());
        b.put(sx - 11, sy + 3, sz - 3, p.light());
        b.put(sx + 12, sy + 2, sz - 4, BARREL);
        b.put(sx + 14, sy + 2, sz - 4, BARREL);
        b.fill(sx + 10, sy + 2, sz + 2, sx + 14, sy + 2, sz + 4, p.frame());
        b.put(sx + 12, sy + 3, sz + 3, p.light());
        b.fill(cx + 196, y + 1, cz - 266, cx + 200, y + 1, cz - 262, p.frame());
        b.put(cx + 198, y + 2, cz - 264, CAULDRON);
        signPost(b, cx + 204, cz - 264, y, p, Direction.EAST, "STATION");
        b.fill(cx + 150, y, cz - 264, cx + 150, y + 3, cz - 264, p.accent());
        b.put(cx + 150, y + 4, cz - 264, p.light());
        railCar(b, cx + 230, cz - 260, ground, 0, p);
        railCar(b, cx + 250, cz - 260, ground, 1, p);
        b.put(cx + 270, ground + 1, cz - 260, RAIL_EW);
        b.put(cx + 272, ground + 1, cz - 260, RAIL_EW);
        b.put(cx + 274, ground + 1, cz - 260, RAIL_EW);
        b.put(cx + 276, ground + 1, cz - 260, RAIL_EW);
        b.put(cx + 278, ground + 1, cz - 260, RAIL_EW);
        b.put(cx + 280, ground + 1, cz - 260, RAIL_EW);
        signPost(b, cx + 204, cz - 268, y, p, Direction.WEST, "OIL FIELDS");
    }

    private static void levelTrack(StructureBuilder b, int x0, int x1, int z, int ground,
                                   Palette p) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
            int surface = b.groundY(x, z);
            if (surface > ground) {
                b.fill(x, ground + 1, z - 1, x, surface, z + 1, AIR);
            } else if (surface < ground) {
                b.fill(x, surface + 1, z - 1, x, ground, z + 1, p.rubble());
            }
            b.fill(x, ground, z - 1, x, ground, z + 1, p.rubble());
            b.put(x, ground + 1, z, RAIL_EW);
        }
    }

    private static void railCar(StructureBuilder b, int x, int z, int ground, int index,
                                   Palette p) {
        if (index == 0) {
            b.fill(x - 6, ground + 1, z - 1, x + 6, ground + 5, z + 1, p.wall());
            b.fill(x - 5, ground + 2, z, x + 5, ground + 4, z, p.frame());
            b.fill(x - 6, ground + 6, z - 1, x + 6, ground + 6, z + 1, p.roof());
            b.put(x - 5, ground + 2, z + 2, p.light());
        } else {
            b.fill(x - 5, ground + 1, z - 1, x + 5, ground + 4, z + 1, IRON);
            b.fill(x - 4, ground + 5, z - 1, x + 4, ground + 5, z + 1, p.roof());
            b.put(x, ground + 6, z, p.accent());
        }
        b.put(x - 4, ground, z, WHEEL);
        b.put(x + 4, ground, z, WHEEL);
        b.put(x - 4, ground, z + 1, WHEEL);
        b.put(x + 4, ground, z + 1, WHEEL);
    }

    private static void derrick(StructureBuilder b, int x, int z, int ground, Palette p,
                                int index) {
        int y = ground;
        b.fill(x - 4, y, z - 4, x + 4, y, z + 4, p.foundation());
        for (int dx = -4; dx <= 4; dx += 8) {
            for (int dz = -4; dz <= 4; dz += 8) {
                b.fill(x + dx, y + 1, z + dz, x + dx, y + 12, z + dz, OAK_LOG);
            }
        }
        for (int i = 0; i < 5; i++) {
            int ly = y + 2 + i * 2;
            b.fill(x - 4, ly, z - 4, x + 4, ly, z - 4, p.frame());
            b.fill(x - 4, ly, z + 4, x + 4, ly, z + 4, p.frame());
            b.fill(x - 4, ly, z - 4, x - 4, ly, z + 4, p.frame());
            b.fill(x + 4, ly, z - 4, x + 4, ly, z + 4, p.frame());
        }
        b.fill(x - 2, y + 12, z - 2, x + 2, y + 12, z + 2, p.frame());
        b.fill(x - 1, y + 13, z - 1, x + 1, y + 13, z + 1, p.accent());
        b.put(x, y + 14, z, p.accent());
        b.put(x, y + 15, z, p.light());
        b.fill(x - 4, y + 1, z + 1, x - 3, y + 1, z + 3, IRON);
        b.fill(x + 3, y + 1, z - 3, x + 4, y + 1, z - 1, IRON);
        b.put(x - 4, y + 2, z + 2, p.frame());
        b.put(x + 4, y + 2, z - 2, p.frame());
        if (index % 3 == 0) {
            b.fill(x - 2, y + 1, z - 2, x + 2, y + 1, z + 2, p.rubble());
            b.put(x, y + 2, z, p.accent());
        }
    }

    private static void storageTank(StructureBuilder b, int x, int z, int ground, Palette p) {
        int y = ground;
        int r = 6;
        b.fill(x - r, y, z - r, x + r, y, z + r, p.foundation());
        for (int dy = 1; dy <= 8; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > r * r || d2 < (r - 1) * (r - 1)) {
                        continue;
                    }
                    b.put(x + dx, y + dy, z + dz,
                            dy == 8 || dy == 4 ? p.accent() : IRON);
                }
            }
        }
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz <= r * r) {
                    b.put(x + dx, y + 9, z + dz, p.roof());
                }
            }
        }
        b.fill(x + r, y + 1, z, x + r, y + 7, z, p.frame());
        b.put(x, y + 10, z, p.light());
        b.fill(x - 3, y + 1, z + r + 1, x + 3, y + 1, z + r + 2, p.foundation());
        b.put(x, y + 2, z + r + 1, p.accent());
        sign(b, p, x, y + 3, z + r + 2, Direction.SOUTH, "OIL");
    }

    private static void oilShed(StructureBuilder b, int x, int z, int ground, Palette p,
                                int index) {
        int y = ground;
        int x0 = x - 8;
        int x1 = x + 8;
        int z0 = z - 6;
        int z1 = z + 6;
        b.fill(x0, y, z0, x1, y, z1, p.foundation());
        b.walls(x0, y + 1, z0, x1, y + 6, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 5, z1 - 1, AIR);
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 1, z1 - 1, p.rubble());
        b.fill(x0 - 1, y + 7, z0 - 1, x1 + 1, y + 7, z1 + 1, p.roof());
        b.fill(x0, y + 8, z0, x1, y + 8, z1, p.roofSlab());
        b.fill(x0 + 3, y + 1, z1, x0 + 6, y + 4, z1, AIR);
        b.fill(x0 + 2, y + 4, z1, x0 + 7, y + 4, z1, p.frame());
        b.put(x0 + 2, y + 2, z0 + 2, BARREL);
        b.put(x1 - 2, y + 2, z0 + 2, BARREL);
        b.put(x1 - 4, y + 2, z1 - 2, CAULDRON);
        b.put(x, y + 6, z0, p.light());
        sign(b, p, x, y + 3, z1, Direction.SOUTH,
                index % 2 == 0 ? "TOOLS" : "PUMP");
        if (index == 3) {
            b.fill(x0 + 1, y + 1, z1 + 1, x0 + 3, y + 1, z1 + 4, p.rubble());
            b.put(x0 + 2, y + 2, z1 + 2, IRON);
        }
    }

    private static void oilOffice(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 246;
        int x1 = cx + 270;
        int z0 = cz - 28;
        int z1 = cz - 8;
        int y = ground;
        b.fill(x0 - 1, y, z0 - 1, x1 + 1, y, z1 + 1, p.foundation());
        b.walls(x0, y + 1, z0, x1, y + 8, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 7, z1 - 1, AIR);
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 1, z1 - 1, p.frame());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y + 8, 5, 0);
        door(b, p, x0 + 12, y + 1, z1, Direction.SOUTH);
        windowX(b, p, x0 + 3, y + 3, z1, 2, 4);
        windowX(b, p, x1 - 7, y + 3, z1, 2, 4);
        b.fill(x0 + 2, y, z1 + 1, x0 + 10, y, z1 + 4, p.foundation());
        b.fill(x0 + 2, y + 1, z1 + 1, x0 + 10, y + 1, z1 + 4, p.frame());
        b.fill(x0 + 2, y + 4, z1 + 1, x0 + 10, y + 4, z1 + 4, p.roofSlab());
        b.fill(x0 + 2, y + 1, z1 + 4, x0 + 2, y + 3, z1 + 4, p.accent());
        b.fill(x0 + 10, y + 1, z1 + 4, x0 + 10, y + 3, z1 + 4, p.accent());
        for (int dx = x0 + 3; dx <= x0 + 9; dx += 3) {
            b.put(dx, y + 3, z1 + 4, p.light());
        }
        b.fill(x0 + 2, y + 2, z0 + 2, x0 + 8, y + 2, z0 + 4, p.frame());
        b.put(x0 + 5, y + 3, z0 + 3, p.light());
        b.put(x0 + 10, y + 2, z0 + 2, BOOKSHELF);
        b.put(x1 - 3, y + 2, z0 + 2, BARREL);
        b.put(x1 - 5, y + 2, z0 + 2, CAULDRON);
        b.fill(x1 - 4, y + 1, z1 - 4, x1 - 2, y + 1, z1 - 2, p.frame());
        b.put(x1 - 3, y + 2, z1 - 3, IRON);
        b.chest(x1 - 4, y + 2, z0 + 5, SiteLoot.CULT);
        sign(b, p, x0 + 8, y + 3, z1, Direction.SOUTH, "OIL OFFICE");
        signPost(b, cx + 240, cz - 8, y, p, Direction.SOUTH, "OIL", "OFFICE");
        car(b, cx + 248, cz - 2, y, 4);
        b.fill(cx + 252, y + 1, cz + 5, cx + 254, y + 1, cz + 7, p.rubble());
        b.put(cx + 253, y + 2, cz + 6, IRON);
        b.put(cx + 253, y + 3, cz + 6, p.frame());
    }

    private static void pipeRack(StructureBuilder b, int x0, int z0, int x1, int z1,
                                 int ground, Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        if (bx - ax >= bz - az) {
            for (int x = ax; x <= bx; x += 4) {
                b.fill(x, ground + 1, az, x, ground + 3, az, p.accent());
                b.fill(x, ground + 1, bz, x, ground + 3, bz, p.accent());
            }
            b.fill(ax, ground + 3, az, bx, ground + 3, az, p.frame());
            b.fill(ax, ground + 3, bz, bx, ground + 3, bz, p.frame());
        } else {
            for (int z = az; z <= bz; z += 4) {
                b.fill(ax, ground + 1, z, ax, ground + 3, z, p.accent());
                b.fill(bx, ground + 1, z, bx, ground + 3, z, p.accent());
            }
            b.fill(ax, ground + 3, az, ax, ground + 3, bz, p.frame());
            b.fill(bx, ground + 3, az, bx, ground + 3, bz, p.frame());
        }
    }

    // ------------------------------------------------------------------ beach

    private static void beach(StructureBuilder b, int cx, int cz, int ground, Palette p,
                              IntBinaryOperator surface) {
        sandSheet(b, cx, cz, surface);
        promenade(b, cx, cz, p, surface);
        int pierDeck = surface.applyAsInt(cx + 240, cz + 240) + 2;
        pier(b, cx + 240, cz + 240, cz + 330, pierDeck, p, surface);
        pavilion(b, cx + 240, cz + 274, pierDeck, p);
        beachHut(b, cx + 170, cz + 220, p, 0, surface);
        beachHut(b, cx + 205, cz + 215, p, 1, surface);
        beachHut(b, cx + 260, cz + 195, p, 2, surface);
        beachHut(b, cx + 275, cz + 175, p, 3, surface);
        lifeguardTower(b, cx + 200, cz + 250, p, surface);
        beachCafe(b, cx + 160, cz + 256, p, surface);
        bathhouse(b, cx + 158, cz + 225, p, surface);
        tidalPool(b, cx + 235, cz + 205, p, surface);
        umbrella(b, cx + 180, cz + 228, p, surface, 0);
        umbrella(b, cx + 220, cz + 230, p, surface, 1);
        umbrella(b, cx + 245, cz + 215, p, surface, 2);
        umbrella(b, cx + 220, cz + 240, p, surface, 3);
        signPost(b, cx + 150, cz + 234, ground, p, Direction.SOUTH, "BEACH");
        signPost(b, cx + 210, cz + 246, surface.applyAsInt(cx + 210, cz + 246), p,
                Direction.SOUTH, "RIP TIDE", "NO SWIMMING");
        signPost(b, cx + 240, cz + 244, pierDeck, p, Direction.SOUTH, "PIER", "PAVILION");
        signPost(b, cx + 240, cz + 324, pierDeck, p, Direction.NORTH, "PIER END");
        for (int dx = 150; dx <= 270; dx += 30) {
            palm(b, cx + dx, cz + 228, surface.applyAsInt(cx + dx, cz + 228));
        }
    }

    private static void sandSheet(StructureBuilder b, int cx, int cz,
                                  IntBinaryOperator surface) {
        for (int dx = 140; dx <= 300; dx += 3) {
            for (int dz = 160; dz <= 300; dz += 3) {
                if (dx * dx + dz * dz > 330 * 330) {
                    continue;
                }
                int y = surface.applyAsInt(cx + dx, cz + dz);
                b.fill(cx + dx, y, cz + dz, cx + dx + 2, y, cz + dz + 2, SAND);
            }
        }
    }

    private static void promenade(StructureBuilder b, int cx, int cz, Palette p,
                                  IntBinaryOperator surface) {
        int z = cz + 240;
        for (int dx = 145; dx <= 285; dx += 8) {
            int y = surface.applyAsInt(cx + dx + 4, z) + 1;
            b.fill(cx + dx, y, z - 4, cx + dx + 7, y, z + 4, OAK);
            b.fill(cx + dx, y + 1, z + 4, cx + dx + 7, y + 1, z + 4, p.rail());
            if ((dx - 145) / 8 % 2 == 0) {
                lanternPost(b, cx + dx + 1, z - 4, y, p);
            }
            for (int pz = z - 3; pz <= z + 3; pz += 6) {
                int base = surface.applyAsInt(cx + dx + 4, pz);
                b.fill(cx + dx + 4, base, pz, cx + dx + 4, y - 1, pz, OAK_LOG);
            }
        }
        signPost(b, cx + 150, z - 6, surface.applyAsInt(cx + 150, z - 6), p,
                Direction.NORTH, "PROMENADE");
    }

    private static void pier(StructureBuilder b, int x, int z0, int z1, int deck, Palette p,
                             IntBinaryOperator surface) {
        int x0 = x - 4;
        int x1 = x + 4;
        for (int z = z0; z <= z1; z++) {
            b.fill(x0, deck, z, x1, deck, z, OAK);
            if ((z & 1) == 0) {
                b.put(x0, deck + 1, z, p.rail());
                b.put(x1, deck + 1, z, p.rail());
            }
            if ((z - z0) % 6 == 0) {
                int b0 = surface.applyAsInt(x0 + 1, z);
                int b1 = surface.applyAsInt(x1 - 1, z);
                b.fill(x0 + 1, b0, z, x0 + 1, deck - 1, z, OAK_LOG);
                b.fill(x1 - 1, b1, z, x1 - 1, deck - 1, z, OAK_LOG);
            }
        }
        for (int z = z0; z <= z1; z += 3) {
            b.fill(x0, deck - 1, z, x1, deck - 1, z, OAK_LOG);
        }
        b.fill(x0, deck - 1, z0, x1, deck - 1, z0, OAK_LOG);
        b.fill(x0 + 1, deck - 1, z0 - 1, x1 - 1, deck - 1, z0 - 1, OAK);
        b.fill(x0 + 1, deck, z0 - 1, x1 - 1, deck, z0 - 1, p.roofSlab());
        b.fill(x0, deck + 1, z1, x1, deck + 1, z1, p.rail());
        lanternPost(b, x0, z0 + 12, deck, p);
        lanternPost(b, x1, z0 + 36, deck, p);
        lanternPost(b, x0, z0 + 60, deck, p);
        b.put(x, deck + 2, z1, p.light());
    }

    private static void pavilion(StructureBuilder b, int x, int z, int deck, Palette p) {
        int x0 = x - 4;
        int x1 = x + 4;
        int z0 = z - 10;
        int z1 = z + 10;
        b.fill(x0, deck, z0, x1, deck, z1, OAK);
        for (int px = x0; px <= x1; px += 8) {
            for (int pz = z0; pz <= z1; pz += 10) {
                b.fill(px, deck + 1, pz, px, deck + 5, pz, p.accent());
            }
        }
        b.fill(x0, deck + 6, z0, x1, deck + 6, z1, p.roof());
        b.fill(x0 + 1, deck + 7, z0 + 1, x1 - 1, deck + 7, z1 - 1, p.roofSlab());
        b.fill(x - 2, deck + 7, z - 2, x + 2, deck + 7, z + 2, p.accent());
        b.fill(x - 1, deck + 8, z - 1, x + 1, deck + 8, z + 1, p.roof());
        b.put(x, deck + 9, z, p.light());
        b.fill(x0, deck + 1, z1, x1, deck + 1, z1, p.rail());
        b.fill(x0, deck + 1, z0, x1, deck + 1, z0, p.rail());
        b.fill(x0, deck + 1, z0, x0, deck + 1, z1, p.rail());
        b.fill(x1, deck + 1, z0, x1, deck + 1, z1, p.rail());
        b.fill(x - 3, deck + 1, z - 6, x + 3, deck + 1, z + 6, p.frame());
        b.put(x - 3, deck + 2, z - 5, p.light());
        b.put(x + 3, deck + 2, z + 5, p.light());
        b.put(x, deck + 2, z, BARREL);
        sign(b, p, x, deck + 2, z + 10, Direction.SOUTH, "PAVILION");
    }

    private static void beachHut(StructureBuilder b, int x, int z, Palette p, int index,
                                 IntBinaryOperator surface) {
        int y = surface.applyAsInt(x, z);
        for (int dx = -3; dx <= 3; dx += 6) {
            for (int dz = -3; dz <= 3; dz += 6) {
                b.fill(x + dx, y, z + dz, x + dx, y + 2, z + dz, OAK_LOG);
            }
        }
        b.fill(x - 3, y + 3, z - 3, x + 3, y + 3, z + 3, OAK);
        b.walls(x - 3, y + 4, z - 3, x + 3, y + 6, z + 3, p.wall());
        b.fill(x - 2, y + 4, z - 2, x + 2, y + 6, z + 2, AIR);
        b.pitchedRoof(x - 4, z - 4, x + 4, z + 4, y + 7, 4, 0);
        door(b, p, x, y + 4, z + 3, Direction.SOUTH);
        awning(b, x - 3, z + 4, x + 3, z + 6, y + 6, wool(index));
        b.put(x - 2, y + 4, z + 3, p.window());
        b.put(x + 2, y + 4, z + 3, p.window());
        b.put(x, y + 12, z, p.light());
        b.fill(x - 5, y + 1, z - 5, x - 5, y + 2, z - 5, p.accent());
        b.put(x - 5, y + 3, z - 5, p.light());
    }

    private static void lifeguardTower(StructureBuilder b, int x, int z, Palette p,
                                       IntBinaryOperator surface) {
        int y = surface.applyAsInt(x, z);
        for (int dx = -3; dx <= 3; dx += 6) {
            for (int dz = -3; dz <= 3; dz += 6) {
                b.fill(x + dx, y, z + dz, x + dx, y + 3, z + dz, OAK_LOG);
            }
        }
        b.fill(x - 3, y + 4, z - 3, x + 3, y + 4, z + 3, OAK);
        b.walls(x - 3, y + 5, z - 3, x + 3, y + 7, z + 3, p.wall());
        b.fill(x - 2, y + 5, z - 2, x + 2, y + 7, z + 2, AIR);
        b.fill(x - 2, y + 6, z + 3, x - 1, y + 6, z + 3, p.window());
        b.fill(x + 1, y + 6, z + 3, x + 2, y + 6, z + 3, p.window());
        b.fill(x - 4, y + 8, z - 4, x + 4, y + 8, z + 4, p.roof());
        b.fill(x - 3, y + 9, z - 3, x + 3, y + 9, z + 3, p.roofSlab());
        b.put(x, y + 10, z, p.light());
        b.fill(x + 3, y + 5, z - 3, x + 3, y + 10, z - 3, p.accent());
        b.fill(x + 3, y + 10, z - 4, x + 3, y + 10, z - 2,
                Materials.wool(DyeColor.RED));
        door(b, p, x, y + 5, z + 3, Direction.SOUTH);
        sign(b, p, x + 3, y + 6, z + 3, Direction.SOUTH, "LIFEGUARD");
        ladder(b, x - 3, y + 1, z, Direction.EAST, 3);
    }

    private static void beachCafe(StructureBuilder b, int x, int z, Palette p,
                                  IntBinaryOperator surface) {
        int y = surface.applyAsInt(x, z);
        b.fill(x - 9, y, z - 6, x + 9, y, z + 6, OAK);
        b.walls(x - 9, y + 1, z - 6, x + 9, y + 6, z + 6, p.wall());
        b.fill(x - 8, y + 1, z - 5, x + 8, y + 5, z + 5, AIR);
        b.pitchedRoof(x - 10, z - 7, x + 10, z + 7, y + 6, 4, 0);
        door(b, p, x, y + 1, z + 6, Direction.SOUTH);
        b.fill(x - 7, y + 1, z - 5, x + 7, y + 1, z - 3, p.frame());
        b.fill(x - 6, y + 2, z - 5, x + 6, y + 2, z - 5, p.rail());
        awning(b, x - 8, z + 7, x + 8, z + 10, y + 5, wool(1));
        for (int dx = -6; dx <= 6; dx += 6) {
            b.put(x + dx, y + 4, z + 10, p.light());
            b.fill(x + dx - 1, y + 1, z + 12, x + dx + 1, y + 1, z + 12, p.frame());
            b.put(x + dx, y + 2, z + 12, p.roofSlab());
        }
        b.put(x - 2, y + 1, z - 2, BARREL);
        b.put(x + 2, y + 1, z - 2, BARREL);
        CityPlanSupport.sign(b, x + 3, y + 3, z + 6, Direction.SOUTH, "BEACH CAFE");
        b.put(x + 8, y + 1, z + 3, p.light());
    }

    private static void bathhouse(StructureBuilder b, int x, int z, Palette p,
                                  IntBinaryOperator surface) {
        int y = surface.applyAsInt(x, z);
        b.fill(x - 10, y, z - 8, x + 10, y, z + 8, OAK);
        b.walls(x - 10, y + 1, z - 8, x + 10, y + 7, z + 8, p.wall());
        b.fill(x - 9, y + 1, z - 7, x + 9, y + 7, z + 7, AIR);
        b.fill(x - 9, y + 1, z - 7, x + 9, y + 1, z + 7, p.frame());
        b.pitchedRoof(x - 11, z - 9, x + 11, z + 9, y + 7, 5, 0);
        door(b, p, x, y + 1, z + 8, Direction.SOUTH);
        for (int dx = -7; dx <= 7; dx += 7) {
            b.fill(x + dx, y + 1, z - 7, x + dx, y + 7, z - 7, p.accent());
            b.put(x + dx, y + 4, z - 7, p.window());
        }
        b.fill(x - 6, y + 1, z + 9, x - 1, y + 1, z + 9, p.rail());
        b.fill(x + 1, y + 1, z + 9, x + 6, y + 1, z + 9, p.rail());
        b.put(x - 6, y + 1, z + 9, p.light());
        b.put(x + 6, y + 1, z + 9, p.light());
        b.put(x - 4, y + 2, z - 4, CAULDRON);
        b.put(x + 4, y + 2, z - 4, BARREL);
        CityPlanSupport.sign(b, x + 3, y + 3, z + 8, Direction.SOUTH, "BATHHOUSE");
    }

    private static void tidalPool(StructureBuilder b, int x, int z, Palette p,
                                  IntBinaryOperator surface) {
        int y = surface.applyAsInt(x, z);
        b.fill(x - 10, y, z - 10, x + 10, y, z + 10, p.foundation());
        b.fill(x - 8, y, z - 8, x + 8, y, z + 8, WATER);
        b.fill(x - 10, y + 1, z - 10, x + 10, y + 1, z - 10, p.foundation());
        b.fill(x - 10, y + 1, z + 10, x + 10, y + 1, z + 10, p.foundation());
        b.fill(x - 10, y + 1, z - 9, x - 10, y + 1, z + 9, p.foundation());
        b.fill(x + 10, y + 1, z - 9, x + 10, y + 1, z + 9, p.foundation());
        for (int i = 0; i < 4; i++) {
            b.put(x - 6 + i * 4, y + 1, z - 9, p.accent());
            b.put(x - 6 + i * 4, y + 1, z + 9, p.accent());
        }
        b.put(x - 9, y + 1, z, p.light());
        b.put(x + 9, y + 1, z, p.light());
        CityPlanSupport.sign(b, x, y + 2, z + 10, Direction.SOUTH, "TIDAL POOL");
    }

    private static void umbrella(StructureBuilder b, int x, int z, Palette p,
                                 IntBinaryOperator surface, int color) {
        int y = surface.applyAsInt(x, z);
        b.put(x, y + 1, z, OAK_LOG);
        b.put(x, y + 2, z, OAK_LOG);
        b.fill(x - 2, y + 3, z - 2, x + 2, y + 3, z + 2, wool(color));
        b.put(x - 1, y + 4, z, wool(color));
        b.put(x + 1, y + 4, z, wool(color));
        b.put(x, y + 4, z - 1, wool(color));
        b.put(x, y + 4, z + 1, wool(color));
        b.fill(x - 2, y + 1, z + 3, x + 2, y + 1, z + 5, wool(color + 1));
        b.put(x - 2, y + 2, z + 4, p.roofSlab());
    }

    // ------------------------------------------------------------------ exposition park

    private static void expositionPark(StructureBuilder b, int cx, int cz, int ground,
                                       Palette p) {
        roseGarden(b, cx - 75, cz + 235, ground, p);
        museum(b, cx + 65, cz + 225, ground, p);
        signPost(b, cx - 110, cz + 190, ground, p, Direction.SOUTH, "ROSE GARDEN");
        signPost(b, cx + 100, cz + 190, ground, p, Direction.SOUTH, "MUSEUM");
        for (int dx = -120; dx <= -30; dx += 30) {
            palm(b, cx + dx, cz + 190, ground);
        }
        for (int dx = 30; dx <= 100; dx += 20) {
            palm(b, cx + dx, cz + 195, ground);
        }
    }

    private static void roseGarden(StructureBuilder b, int x, int z, int ground, Palette p) {
        int y = ground;
        int x0 = x - 35;
        int x1 = x + 35;
        int z0 = z - 35;
        int z1 = z + 35;
        b.fill(x0, y, z0, x1, y, z1, p.ground());
        fenceLine(b, x0, z0, x1, z1, y + 1, p);
        pave(b, x0 + 2, z - 3, x1 - 2, z + 3, y, p.rubble());
        pave(b, x - 3, z0 + 2, x + 3, z1 - 2, y, p.rubble());
        fountain(b, x, z, y, p);
        for (int dx = -28; dx <= 28; dx += 14) {
            for (int dz = -28; dz <= 28; dz += 14) {
                if (Math.abs(dx) < 8 && Math.abs(dz) < 8) {
                    continue;
                }
                b.fill(x + dx - 4, y + 1, z + dz - 2, x + dx + 4, y + 1, z + dz + 2, HEDGE);
                for (int i = -3; i <= 3; i++) {
                    b.put(x + dx + i, y + 2, z + dz, flower(dx + dz + i));
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            int px = x0 + 4 + i * 18;
            b.fill(px, y + 1, z0 + 4, px, y + 4, z0 + 4, p.accent());
            b.fill(px, y + 1, z1 - 4, px, y + 4, z1 - 4, p.accent());
        }
        b.fill(x0 + 4, y + 5, z0 + 4, x0 + 58, y + 5, z0 + 4, p.frame());
        b.fill(x0 + 4, y + 5, z1 - 4, x0 + 58, y + 5, z1 - 4, p.frame());
        for (int i = 0; i < 5; i++) {
            b.fill(x0 + 4 + i * 13, y + 5, z0 + 4, x0 + 6 + i * 13, y + 5, z1 - 4, p.frame());
        }
        for (int dx = -20; dx <= 20; dx += 10) {
            bench(b, x + dx, z + 12, y, p, Direction.NORTH);
        }
        signPost(b, x, z + 37, y, p, Direction.SOUTH, "ROSE GARDEN");
    }

    private static void museum(StructureBuilder b, int x, int z, int ground, Palette p) {
        int y = ground;
        int x0 = x - 25;
        int x1 = x + 25;
        int z0 = z - 20;
        int z1 = z + 20;
        b.fill(x0 - 1, y, z0 - 1, x1 + 1, y, z1 + 1, p.foundation());
        b.walls(x0, y + 1, z0, x1, y + 13, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 13, z1 - 1, AIR);
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 1, z1 - 1, p.ground());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y + 13, 9, 0);
        for (int px = x0 + 3; px <= x1 - 3; px += 5) {
            b.fill(px, y + 1, z1 + 1, px, y + 9, z1 + 1, p.accent());
            b.put(px, y + 10, z1 + 1, p.roofSlab());
        }
        b.fill(x0 + 1, y + 10, z1 + 1, x1 - 1, y + 10, z1 + 1, p.foundation());
        b.fill(x0 + 1, y + 11, z1 + 1, x1 - 1, y + 11, z1 + 1, p.accent());
        b.fill(x0 + 8, y + 12, z1 + 1, x1 - 8, y + 12, z1 + 1, p.accent());
        b.fill(x - 4, y, z1 + 2, x + 4, y, z1 + 6, p.foundation());
        b.fill(x - 3, y + 1, z1 + 2, x + 3, y + 1, z1 + 6, p.ground());
        door(b, p, x, y + 1, z1, Direction.SOUTH);
        windowX(b, p, x0 + 4, y + 4, z1, 4, 6);
        windowX(b, p, x1 - 10, y + 4, z1, 4, 6);
        b.fill(x - 6, y + 2, z0 + 3, x - 2, y + 3, z0 + 5, p.frame());
        b.put(x - 4, y + 4, z0 + 4, GOLD);
        b.fill(x + 2, y + 2, z0 + 3, x + 6, y + 3, z0 + 5, p.frame());
        b.put(x + 4, y + 4, z0 + 4, GOLD);
        b.fill(x - 8, y + 2, z0 + 8, x + 8, y + 2, z0 + 10, p.frame());
        b.fill(x - 8, y + 3, z0 + 8, x + 8, y + 3, z0 + 8, p.rail());
        b.put(x, y + 3, z0 + 6, p.light());
        b.put(x - 10, y + 2, z1 - 4, BARREL);
        b.put(x + 10, y + 2, z1 - 4, BARREL);
        signPost(b, x, z1 + 8, y, p, Direction.SOUTH, "MUSEUM");
        for (int dx = -18; dx <= 18; dx += 12) {
            palm(b, x + dx, z1 + 10, y);
        }
    }

    // ------------------------------------------------------------------ south side

    private static void southSide(StructureBuilder b, int cx, int cz, int ground, Palette p,
                                  IntBinaryOperator surface) {
        warehouse(b, cx - 75, cz + 294, surface.applyAsInt(cx - 75, cz + 294), p, 0);
        warehouse(b, cx + 65, cz + 294, surface.applyAsInt(cx + 65, cz + 294), p, 1);
        smallHouse(b, cx - 128, cz + 220, ground, p, 0);
        smallHouse(b, cx - 128, cz + 250, ground, p, 1);
        smallHouse(b, cx + 125, cz + 220, ground, p, 2);
        smallHouse(b, cx + 125, cz + 250, ground, p, 3);
        for (int dz = 200; dz <= 300; dz += 20) {
            palm(b, cx - 112, cz + dz, ground);
            palm(b, cx + 112, cz + dz, ground);
        }
        signPost(b, cx - 20, cz + 306, ground, p, Direction.SOUTH, "FREIGHT");
        signPost(b, cx + 20, cz + 306, ground, p, Direction.SOUTH, "PRODUCE");
    }

    private static void warehouse(StructureBuilder b, int x, int z, int ground, Palette p,
                                  int index) {
        int y = ground;
        int x0 = x - 25;
        int x1 = x + 25;
        int z0 = z - 12;
        int z1 = z + 12;
        b.fill(x0 - 1, y, z0 - 1, x1 + 1, y, z1 + 1, p.foundation());
        b.walls(x0, y + 1, z0, x1, y + 10, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 10, z1 - 1, AIR);
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 1, z1 - 1, p.rubble());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y + 10, 6, 0);
        b.fill(x0 + 4, y + 1, z1, x0 + 14, y + 7, z1, p.frame());
        b.fill(x0 + 5, y + 1, z1 + 1, x0 + 13, y + 6, z1 + 1, DARK_OAK);
        b.fill(x0 + 5, y + 7, z1 + 1, x0 + 13, y + 7, z1 + 1, p.accent());
        b.fill(x1 - 14, y + 1, z1, x1 - 4, y + 7, z1, p.frame());
        b.fill(x1 - 13, y + 1, z1 + 1, x1 - 5, y + 6, z1 + 1, DARK_OAK);
        for (int dx = x0 + 2; dx <= x1 - 2; dx += 6) {
            b.put(dx, y + 8, z0, p.window());
        }
        b.fill(x0 + 2, y + 1, z0 + 2, x0 + 8, y + 2, z0 + 4, p.frame());
        b.put(x0 + 5, y + 3, z0 + 3, p.light());
        b.put(x1 - 3, y + 2, z0 + 2, BARREL);
        b.put(x1 - 5, y + 2, z0 + 2, BARREL);
        b.put(x1 - 7, y + 2, z0 + 2, BARREL);
        b.fill(x0 + 2, y + 2, z1 - 4, x0 + 6, y + 2, z1 - 2, HAY);
        sign(b, p, x, y + 3, z1, Direction.SOUTH,
                index == 0 ? "FREIGHT" : index == 1 ? "PRODUCE" : "DEPOT");
        car(b, x + 30, z + 18, y, index);
    }

    private static void smallHouse(StructureBuilder b, int x, int z, int ground, Palette p,
                                   int index) {
        int y = ground;
        b.fill(x - 7, y, z - 7, x + 7, y, z + 7, p.foundation());
        b.walls(x - 7, y + 1, z - 7, x + 7, y + 6, z + 7, p.wall());
        b.fill(x - 6, y + 1, z - 6, x + 6, y + 6, z + 6, AIR);
        b.fill(x - 6, y + 1, z - 6, x + 6, y + 1, z + 6, p.frame());
        b.pitchedRoof(x - 8, z - 8, x + 8, z + 8, y + 6, 5, 0);
        door(b, p, x, y + 1, z + 7, Direction.SOUTH);
        windowX(b, p, x - 4, y + 2, z + 7, 2, 3);
        windowX(b, p, x + 1, y + 2, z + 7, 2, 3);
        b.fill(x - 7, y + 1, z + 8, x + 7, y + 1, z + 11, p.ground());
        awning(b, x - 7, z + 8, x + 7, z + 10, y + 4, wool(index));
        palm(b, x + 6, z + 13, y);
        car(b, x - 12, z + 9, y, index);
        b.put(x - 6, y + 2, z - 6, Blocks.FLOWER_POT.defaultBlockState());
        sign(b, p, x - 1, y + 3, z + 7, Direction.SOUTH, "HOME");
    }
}
