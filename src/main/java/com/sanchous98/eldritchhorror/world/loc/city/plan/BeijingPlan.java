package com.sanchous98.eldritchhorror.world.loc.city.plan;

import net.minecraft.world.item.DyeColor;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.SiteLoot;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Imperial Beijing at the end of the dynasty: the Forbidden City's red walls and golden roofs on
 * the northern axis, Tiananmen Square, grey hutong lanes, the blue-tiled Temple of Heaven, the silk
 * market, the railway/legation quarter and the canal.
 *
 * <p>Everything here is authored and deterministic. The plan writes the whole city on every
 * generating chunk and the builder keeps the slice inside the chunk; there is no RNG, no static
 * mutable state and no read of the generating chunk. The shared pass has already flattened the
 * authored district to {@code ground}, so every lot, street and prop sits at {@code ground} plus
 * authored constant offsets; there are no terrain-derived heights.
 */
public final class BeijingPlan implements CityPlan {

    /** Authored half-extent (blocks); the district pass uses the same radius. */
    private static final int HALF = 340;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState GREY_ROOF = Blocks.DEEPSLATE_TILES.defaultBlockState();
    private static final BlockState BLUE_ROOF = Blocks.GLAZED_TERRACOTTA.pick(DyeColor.BLUE).defaultBlockState();
    private static final BlockState BLUE_RIDGE = Blocks.CONCRETE.pick(DyeColor.BLUE).defaultBlockState();
    private static final BlockState MARBLE = Blocks.QUARTZ_BLOCK.defaultBlockState();
    private static final BlockState LEAVES = Blocks.DARK_OAK_LEAVES.defaultBlockState()
            .setValue(BlockStateProperties.PERSISTENT, true);
    private static final BlockState RAIL = Blocks.RAIL.defaultBlockState()
            .setValue(BlockStateProperties.RAIL_SHAPE, RailShape.EAST_WEST);
    private static final BlockState LAMP = Blocks.LANTERN.defaultBlockState();

    /**
     * Translates the plan's authored, city-relative coordinates to absolute world coordinates.
     * Only the write operations the plan uses are exposed; the plan never reads terrain.
     */
    private static final class Ctx {

        private final StructureBuilder delegate;
        private final int cx;
        private final int cz;

        private Ctx(StructureBuilder delegate, int cx, int cz) {
            this.delegate = delegate;
            this.cx = cx;
            this.cz = cz;
        }

        private void put(int x, int y, int z, BlockState state) {
            this.delegate.put(this.cx + x, y, this.cz + z, state);
        }

        private void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
            this.delegate.fill(this.cx + x0, y0, this.cz + z0,
                    this.cx + x1, y1, this.cz + z1, state);
        }

        private void walls(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
            this.delegate.walls(this.cx + x0, y0, this.cz + z0,
                    this.cx + x1, y1, this.cz + z1, state);
        }

        private void pitchedRoof(int x0, int z0, int x1, int z1, int y, int height, int ridgeAxis) {
            this.delegate.pitchedRoof(this.cx + x0, this.cz + z0, this.cx + x1, this.cz + z1,
                    y, height, ridgeAxis);
        }

        private void window(int x, int y, int z, int height, int width, boolean vertical) {
            this.delegate.window(this.cx + x, y, this.cz + z, height, width, vertical);
        }

        private void monument(int mx, int mz, int baseY) {
            this.delegate.monument(this.cx + mx, this.cz + mz, baseY);
        }

        private void sign(int x, int y, int z, Direction facing, String... lines) {
            this.delegate.sign(this.cx + x, y, this.cz + z, facing, lines);
        }

        private void chest(int x, int y, int z, ResourceKey<LootTable> table) {
            this.delegate.chest(this.cx + x, y, this.cz + z, table);
        }
    }

    @Override
    public String cityId() {
        return "beijing";
    }

    @Override
    public int halfExtent() {
        return HALF;
    }

    @Override
    public void build(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        build(b, cx, cz, ground, p, ground, ground, ground);
    }

    @Override
    public void build(StructureBuilder b, CityPlanContext ctx, Palette p) {
        int stationBase = ctx.surface().at(ctx.cx() + 220, ctx.cz() + 254);
        int legationBase = ctx.surface().at(ctx.cx() + 300, ctx.cz() + 184);
        int arrivalBase = ctx.surface().at(ctx.cx(), ctx.cz() + 323);
        build(b, ctx.cx(), ctx.cz(), ctx.ground(), p, stationBase, legationBase, arrivalBase);
    }

    private static void build(StructureBuilder b, int cx, int cz, int ground, Palette p,
                              int stationBase, int legationBase, int arrivalBase) {
        Ctx c = new Ctx(b, cx, cz);
        streets(c, ground, p);
        moat(c, ground);
        canal(c, ground, p);
        palace(c, ground, p);
        square(c, ground, p);
        hutongs(c, ground, p);
        temple(c, ground, p);
        market(c, ground, p);
        railway(c, ground, p, stationBase, legationBase);
        bridges(c, ground, p);
        arrival(c, arrivalBase, p);
        gardens(c, ground, p);
        secrets(c, ground, p);
        signs(c, ground, p);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("square_quest_board", CityPoi.Kind.QUEST_BOARD, 14, 1, 60,
                        "beijing_missing_eunuch;beijing_sealed_decree"),
                new CityPoi("city_notices", CityPoi.Kind.SIGN, 11, 1, 63, "notices"),
                new CityPoi("palace_clerk", CityPoi.Kind.NPC, 0, 0, -90, "beijing_sealed_decree"),
                new CityPoi("hutong_elder", CityPoi.Kind.NPC, -228, 0, -98,
                        "beijing_missing_eunuch"),
                new CityPoi("hutong_well", CityPoi.Kind.SECRET, -240, 0, -120,
                        "beijing_missing_eunuch"),
                new CityPoi("temple_priest", CityPoi.Kind.HEALER, 20, 0, 240,
                        "decodes_the_decree"),
                new CityPoi("silk_merchant", CityPoi.Kind.SHOP, 220, 0, 20, "silk;rumours"),
                new CityPoi("tea_house", CityPoi.Kind.INN, 220, 0, -40, "keeper"),
                new CityPoi("tea_keeper", CityPoi.Kind.NPC, 214, 0, -40, "keeper"),
                new CityPoi("railway_station", CityPoi.Kind.STATION, 220, 0, 286,
                        "stationmaster;east_west"),
                new CityPoi("station_yard", CityPoi.Kind.STATION, 170, 1, 290,
                        "rail yard; straight level tracks east_west"),
                new CityPoi("stationmaster", CityPoi.Kind.NPC, 220, 0, 244, "cargo"),
                new CityPoi("legation", CityPoi.Kind.SECRET, 300, 0, 200, "locked_archive"),
                new CityPoi("legate", CityPoi.Kind.NPC, 300, 0, 208, "archive"),
                new CityPoi("canal_bridge", CityPoi.Kind.LANDMARK, 151, 3, 0, "moored_boats"),
                new CityPoi("arrival_signpost", CityPoi.Kind.SIGN, 0, 1, 326, "arrival"),
                new CityPoi("moat_warning", CityPoi.Kind.SIGN, -140, 1, -60, "warning"),
                new CityPoi("well_warning", CityPoi.Kind.SIGN, -236, 1, -118, "warning"));
    }

    // ------------------------------------------------------------------ primitive helpers

    private static void pave(Ctx c, int ground, int x0, int z0, int x1, int z1,
                             BlockState state) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                c.put(x, ground, z, state);
            }
        }
    }

    private static void waterStrip(Ctx c, int ground, int x0, int z0, int x1, int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                int y = ground;
                c.fill(x, y - 3, z, x, y - 1, z, WATER);
                c.put(x, y, z, AIR);
                c.put(x, y + 1, z, AIR);
            }
        }
    }

    private static void quay(Ctx c, int ground, Palette p, int x0, int x1, int z0, int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                int y = ground;
                c.fill(x, y - 3, z, x, y + 1, z, p.foundation());
            }
        }
    }

    private static void disc(Ctx c, int cx, int cz, int r, int y, BlockState state) {
        for (int x = cx - r; x <= cx + r; x++) {
            for (int z = cz - r; z <= cz + r; z++) {
                if ((x - cx) * (x - cx) + (z - cz) * (z - cz) <= r * r) {
                    c.put(x, y, z, state);
                }
            }
        }
    }

    private static void ring(Ctx c, int cx, int cz, int r0, int r1, int y0, int y1,
                             BlockState state) {
        for (int x = cx - r1; x <= cx + r1; x++) {
            for (int z = cz - r1; z <= cz + r1; z++) {
                int d2 = (x - cx) * (x - cx) + (z - cz) * (z - cz);
                if (d2 > r1 * r1 || d2 < r0 * r0) {
                    continue;
                }
                for (int y = y0; y <= y1; y++) {
                    c.put(x, y, z, state);
                }
            }
        }
    }

    private static void shell(Ctx c, int x0, int z0, int x1, int z1, int floorY,
                              int wallH, BlockState wall, BlockState floor, BlockState ceiling) {
        c.fill(x0, floorY, z0, x1, floorY, z1, floor);
        c.fill(x0, floorY + wallH, z0, x1, floorY + wallH, z1, ceiling);
        c.walls(x0, floorY + 1, z0, x1, floorY + wallH - 1, z1, wall);
        c.fill(x0 + 1, floorY + 1, z0 + 1, x1 - 1, floorY + wallH - 1, z1 - 1, AIR);
    }

    private static void door(Ctx c, Palette p, int x, int y, int z,
                             Direction facing) {
        BlockState lower = p.door()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        c.put(x, y, z, lower);
        c.put(x, y + 1, z, lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,
                DoubleBlockHalf.UPPER));
    }

    private static void greyRoof(Ctx c, int x0, int z0, int x1, int z1, int y, int h,
                                 int axis) {
        for (int i = 0; i < h; i++) {
            int ix0 = x0 + (axis == 0 ? 0 : i);
            int ix1 = x1 - (axis == 0 ? 0 : i);
            int iz0 = z0 + (axis == 0 ? i : 0);
            int iz1 = z1 - (axis == 0 ? i : 0);
            if (ix0 > ix1 || iz0 > iz1) {
                break;
            }
            c.fill(ix0, y + i, iz0, ix1, y + i, iz1, GREY_ROOF);
        }
    }

    private static void signAt(Ctx c, int ground, Palette p, int x, int z,
                               Direction facing, String... lines) {
        int y = ground;
        c.put(x, y, z, p.foundation());
        c.sign(x, y + 1, z, facing, lines);
    }

    private static void signOn(Ctx c, Palette p, int x, int y, int z,
                               Direction facing, String... lines) {
        c.put(x, y, z, p.foundation());
        c.sign(x, y + 1, z, facing, lines);
    }

    private static void lanternPost(Ctx c, int ground, Palette p, int x, int z) {
        int y = ground;
        c.put(x, y + 1, z, p.rail());
        c.put(x, y + 2, z, p.rail());
        c.put(x, y + 3, z, p.light());
    }

    private static void cypress(Ctx c, int ground, int x, int z) {
        cypressAt(c, x, ground, z);
    }

    private static void cypressAt(Ctx c, int x, int y, int z) {
        for (int i = 1; i <= 7; i++) {
            c.put(x, y + i, z, Blocks.SPRUCE_LOG.defaultBlockState());
        }
        for (int i = 3; i <= 9; i++) {
            int r = i <= 6 ? 1 : 0;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx == 0 && dz == 0) {
                        continue;
                    }
                    if (Math.abs(dx) + Math.abs(dz) <= r) {
                        c.put(x + dx, y + i, z + dz, LEAVES);
                    }
                }
            }
        }
        c.put(x, y + 8, z, LEAVES);
    }

    private static void broadTreeAt(Ctx c, int x, int y, int z) {
        for (int i = 1; i <= 4; i++) {
            c.put(x, y + i, z, Blocks.SPRUCE_LOG.defaultBlockState());
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 4; dy <= 6; dy++) {
                    if (Math.abs(dx) + Math.abs(dz) + dy - 4 <= 4) {
                        c.put(x + dx, y + dy, z + dz, LEAVES);
                    }
                }
            }
        }
    }

    private static void wallRect(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                 int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                int y = ground;
                c.fill(x, y, z, x, y + 7, z, p.accent());
                c.put(x, y + 8, z, p.roofSlab());
            }
        }
    }

    // ------------------------------------------------------------------ streets / water

    private static void streets(Ctx c, int ground, Palette p) {
        pave(c, ground, -7, -70, 7, 320, p.ground());
        pave(c, ground, -340, -7, 340, 7, p.ground());
        pave(c, ground, -1, -70, 1, 320, p.foundation());
        pave(c, ground, -340, -1, 340, 1, p.foundation());
        pave(c, ground, 137, -300, 139, 310, p.ground());
        pave(c, ground, 163, -300, 172, 310, p.ground());
        pave(c, ground, -148, -310, -138, 220, p.ground());
        pave(c, ground, -90, 291, 90, 300, p.ground());
        pave(c, ground, -40, -340, 40, -314, p.foundation());
    }

    private static void moat(Ctx c, int ground) {
        waterStrip(c, ground, -136, -312, -129, -58);
        waterStrip(c, ground, 129, -312, 136, -58);
        waterStrip(c, ground, -136, -312, 136, -305);
        waterStrip(c, ground, -136, -65, 136, -58);
    }

    private static void canal(Ctx c, int ground, Palette p) {
        waterStrip(c, ground, 142, -300, 160, 310);
        quay(c, ground, p, 140, 141, -300, 310);
        quay(c, ground, p, 161, 162, -300, 310);
    }

    // ------------------------------------------------------------------ Forbidden City

    private static void palace(Ctx c, int ground, Palette p) {
        wallRect(c, ground, p, -124, -300, 124, -297);
        wallRect(c, ground, p, -124, -73, 124, -70);
        wallRect(c, ground, p, -124, -300, -121, -70);
        wallRect(c, ground, p, 121, -300, 124, -70);
        cornerTower(c, ground, p, -122, -298);
        cornerTower(c, ground, p, 122, -298);
        cornerTower(c, ground, p, -122, -72);
        cornerTower(c, ground, p, 122, -72);
        gateTower(c, ground, p, 0, -73, 24, 5, 10);
        gateTower(c, ground, p, 0, -300, 20, 5, 10);
        palaceBridge(c, ground, p, -5, 5, -68, -58);
        palaceBridge(c, ground, p, -5, 5, -312, -304);
        supremeHall(c, ground, p, 0, -200);
        sideHall(c, ground, p, -56, -200, 9, 13);
        sideHall(c, ground, p, 56, -200, 9, 13);
        rearHall(c, ground, p, 0, -262, 15, 7);
        pave(c, ground, -4, -298, 4, -74, p.foundation());
        pave(c, ground, -120, -124, 120, -120, p.foundation());
        pave(c, ground, -120, -244, 120, -240, p.foundation());
        for (int z = -280; z <= -90; z += 20) {
            lanternPost(c, ground, p, -44, z);
            lanternPost(c, ground, p, 44, z);
        }
        int ky = ground;
        shell(c, -26, -104, -16, -94, ky, 4, p.accent(), p.ground(), p.roof());
        c.fill(-16, ky + 1, -100, -16, ky + 2, -98, AIR);
        door(c, p, -16, ky + 1, -99, Direction.EAST);
        signAt(c, ground, p, -15, -99, Direction.EAST, "PALACE CLERK", "SEALS & WRITS", "BY APPOINTMENT");
    }

    private static void cornerTower(Ctx c, int ground, Palette p, int cx, int cz) {
        int y = ground;
        shell(c, cx - 5, cz - 5, cx + 5, cz + 5, y, 12, p.accent(), p.ground(), p.roof());
        c.window(cx, y + 5, cz + 5, 4, 1, true);
        c.window(cx, y + 5, cz - 5, 4, 1, true);
        c.pitchedRoof(cx - 7, cz - 7, cx + 7, cz + 7, y + 12, 4, 0);
        c.put(cx, y + 17, cz, p.light());
    }

    private static void gateTower(Ctx c, int ground, Palette p, int cx, int cz,
                                  int halfW, int halfD, int h) {
        int y = ground;
        c.fill(cx - halfW, y, cz - halfD, cx + halfW, y + h, cz + halfD, p.accent());
        c.fill(cx - 4, y, cz - halfD, cx + 4, y + 7, cz + halfD, AIR);
        c.fill(cx - 14, y, cz - halfD, cx - 10, y + 5, cz + halfD, AIR);
        c.fill(cx + 10, y, cz - halfD, cx + 14, y + 5, cz + halfD, AIR);
        c.fill(cx - halfW, y + 8, cz - halfD, cx + halfW, y + 8, cz + halfD, p.roof());
        c.pitchedRoof(cx - halfW - 2, cz - halfD - 2, cx + halfW + 2, cz + halfD + 2, y + h + 1,
                4, 0);
        shell(c, cx - 12, cz - 3, cx + 12, cz + 3, y + h + 2, 5, p.accent(), p.ground(), p.roof());
        c.fill(cx, y + h + 3, cz + 3, cx, y + h + 4, cz + 3, AIR);
        c.pitchedRoof(cx - 16, cz - 7, cx + 16, cz + 7, y + h + 7, 4, 0);
        c.put(cx, y + h + 12, cz, p.light());
    }

    private static void palaceBridge(Ctx c, int ground, Palette p, int x0, int x1,
                                     int z0, int z1) {
        int deck = ground + 1;
        c.fill(x0, deck, z0, x1, deck, z1, p.ground());
        for (int z = z0; z <= z1; z++) {
            c.put(x0, deck + 1, z, p.rail());
            c.put(x1, deck + 1, z, p.rail());
        }
        c.fill(x0, deck - 1, z0, x1, deck - 1, z1, p.foundation());
    }

    private static void supremeHall(Ctx c, int ground, Palette p, int cx, int cz) {
        int y = ground;
        c.fill(cx - 40, y, cz - 28, cx + 40, y, cz + 28, p.foundation());
        c.fill(cx - 36, y + 1, cz - 24, cx + 36, y + 1, cz + 24, p.foundation());
        c.fill(cx - 32, y + 2, cz - 20, cx + 32, y + 2, cz + 20, p.foundation());
        int floor = y + 3;
        int x0 = cx - 28;
        int x1 = cx + 28;
        int z0 = cz - 16;
        int z1 = cz + 16;
        shell(c, x0, z0, x1, z1, floor, 12, p.accent(), p.ground(), p.roof());
        c.fill(cx - 1, floor + 1, z1, cx + 1, floor + 2, z1, AIR);
        door(c, p, cx, floor + 1, z1, Direction.SOUTH);
        c.fill(cx - 1, floor + 1, z0, cx + 1, floor + 2, z0, AIR);
        door(c, p, cx, floor + 1, z0, Direction.NORTH);
        for (int x = x0 + 5; x <= x1 - 5; x += 5) {
            c.window(x, floor + 4, z1, 5, 1, true);
            c.window(x, floor + 4, z0, 5, 1, true);
            for (int y2 = floor + 1; y2 <= floor + 11; y2++) {
                c.put(x, y2, z1 + 1, p.accent());
            }
        }
        for (int z = z0 + 5; z <= z1 - 5; z += 5) {
            c.window(x0, floor + 4, z, 5, 1, true);
            c.window(x1, floor + 4, z, 5, 1, true);
        }
        c.pitchedRoof(x0 - 4, z0 - 4, x1 + 4, z1 + 4, floor + 12, 6, 0);
        int uf = floor + 19;
        shell(c, cx - 16, cz - 8, cx + 16, cz + 8, uf, 4, p.accent(), p.ground(), p.roof());
        for (int x = cx - 12; x <= cx + 12; x += 4) {
            c.window(x, uf + 2, cz + 8, 2, 1, true);
            c.window(x, uf + 2, cz - 8, 2, 1, true);
        }
        c.pitchedRoof(cx - 20, cz - 12, cx + 20, cz + 12, uf + 4, 5, 0);
        c.put(cx, uf + 10, cz, p.roof());
        c.put(cx, uf + 11, cz, p.light());
    }

    private static void sideHall(Ctx c, int ground, Palette p, int cx, int cz, int halfW,
                                 int halfD) {
        int y = ground;
        shell(c, cx - halfW, cz - halfD, cx + halfW, cz + halfD, y, 8, p.accent(), p.ground(),
                p.roof());
        c.fill(cx, y + 1, cz + halfD, cx, y + 2, cz + halfD, AIR);
        door(c, p, cx, y + 1, cz + halfD, Direction.SOUTH);
        for (int x = cx - halfW + 3; x <= cx + halfW - 3; x += 4) {
            c.window(x, y + 3, cz + halfD, 3, 1, true);
            c.window(x, y + 3, cz - halfD, 3, 1, true);
        }
        c.pitchedRoof(cx - halfW - 2, cz - halfD - 2, cx + halfW + 2, cz + halfD + 2, y + 8, 4, 0);
    }

    private static void rearHall(Ctx c, int ground, Palette p, int cx, int cz, int halfW,
                                 int halfD) {
        int y = ground;
        shell(c, cx - halfW, cz - halfD, cx + halfW, cz + halfD, y, 8, p.accent(), p.ground(),
                p.roof());
        c.fill(cx, y + 1, cz + halfD, cx, y + 2, cz + halfD, AIR);
        door(c, p, cx, y + 1, cz + halfD, Direction.SOUTH);
        for (int x = cx - halfW + 4; x <= cx + halfW - 4; x += 5) {
            c.window(x, y + 3, cz + halfD, 3, 1, true);
        }
        c.pitchedRoof(cx - halfW - 2, cz - halfD - 2, cx + halfW + 2, cz + halfD + 2, y + 8, 4, 0);
    }

    // ------------------------------------------------------------------ Tiananmen Square

    private static void square(Ctx c, int ground, Palette p) {
        pave(c, ground, -116, -50, 116, -47, p.foundation());
        pave(c, ground, -116, 77, 116, 80, p.foundation());
        pave(c, ground, -116, -50, -113, 80, p.foundation());
        pave(c, ground, 113, -50, 116, 80, p.foundation());
        int my = ground;
        c.monument(-70, 40, my);
        for (int x = -100; x <= -20; x += 20) {
            flagpole(c, ground, p, x, 70);
        }
        publicHall(c, ground, p, -140, -44, -124, -14);
        publicHall(c, ground, p, -140, 14, -124, 44);
        publicHall(c, ground, p, 96, -44, 112, -14);
        publicHall(c, ground, p, 96, 14, 112, 44);
        noticeBoard(c, ground, p, 14, 60);
        for (int z = -40; z <= 70; z += 22) {
            if (Math.abs(z) <= 7) {
                continue;
            }
            lanternPost(c, ground, p, -92, z);
            lanternPost(c, ground, p, 92, z);
        }
        for (int x = -90; x <= 90; x += 30) {
            c.put(x, ground, 30, p.foundation());
        }
    }

    private static void flagpole(Ctx c, int ground, Palette p, int x, int z) {
        int y = ground;
        c.fill(x, y + 1, z, x, y + 10, z, p.rail());
        c.fill(x + 1, y + 7, z, x + 3, y + 9, z, Blocks.WOOL.pick(DyeColor.RED).defaultBlockState());
        c.put(x, y + 11, z, p.light());
    }

    private static void publicHall(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                   int z1) {
        int y = ground;
        shell(c, x0, z0, x1, z1, y, 9, p.wall(), p.ground(), p.roof());
        int cx = (x0 + x1) / 2;
        c.fill(cx - 1, y + 1, z1, cx + 1, y + 2, z1, AIR);
        door(c, p, cx, y + 1, z1, Direction.SOUTH);
        c.fill(cx - 1, y + 1, z0, cx + 1, y + 2, z0, AIR);
        door(c, p, cx, y + 1, z0, Direction.NORTH);
        for (int x = x0 + 3; x <= x1 - 3; x += 4) {
            c.window(x, y + 3, z1, 4, 1, true);
            c.window(x, y + 3, z0, 4, 1, true);
        }
        c.pitchedRoof(x0 - 2, z0 - 2, x1 + 2, z1 + 2, y + 9, 4, 1);
    }

    private static void noticeBoard(Ctx c, int ground, Palette p, int x, int z) {
        int y = ground;
        c.fill(x - 5, y + 1, z, x + 5, y + 3, z, p.frame());
        c.fill(x - 5, y + 1, z - 1, x - 5, y + 4, z - 1, p.accent());
        c.fill(x + 5, y + 1, z - 1, x + 5, y + 4, z - 1, p.accent());
        c.pitchedRoof(x - 6, z - 2, x + 6, z + 2, y + 4, 2, 0);
        signAt(c, ground, p, x - 3, z + 3, Direction.SOUTH, "CITY NOTICES", "MISSING EUNUCH",
                "SEALED DECREE", "ASK THE ELDER");
        signAt(c, ground, p, x + 3, z + 3, Direction.SOUTH, "REWARDS PAID", "BY THE ORDER",
                "INQUIRE HERE");
    }

    // ------------------------------------------------------------------ hutongs

    private static void hutongs(Ctx c, int ground, Palette p) {
        int[] zLanes = {-280, -240, -200, -160, -120, -80, -40, 40, 80, 120, 160, 200};
        for (int z : zLanes) {
            pave(c, ground, -330, z - 2, -150, z + 1, p.ground());
            pave(c, ground, -330, z - 2, -150, z - 2, p.foundation());
            pave(c, ground, -330, z + 1, -150, z + 1, p.foundation());
        }
        int[] xLanes = {-320, -280, -240, -200, -160};
        for (int x : xLanes) {
            pave(c, ground, x - 2, -280, x + 1, 220, p.ground());
            pave(c, ground, x - 2, -280, x - 2, 220, p.foundation());
            pave(c, ground, x + 1, -280, x + 1, 220, p.foundation());
        }
        pave(c, ground, -244, -124, -236, -116, p.foundation());
        int[] x0s = {-317, -277, -237, -197};
        int[] x1s = {-283, -243, -203, -163};
        int[] z0s = {-278, -238, -198, -158, -118, -78, -38, 10, 42, 82, 122, 162, 202};
        int[] z1s = {-243, -203, -163, -123, -83, -43, -10, 37, 77, 117, 157, 197, 220};
        for (int xi = 0; xi < x0s.length; xi++) {
            for (int zi = 0; zi < z0s.length; zi++) {
                if (xi == 2 && zi == 4) {
                    continue; // the elder's court and the well
                }
                if (xi == 0 && zi == 9) {
                    hutongShrine(c, ground, p, x0s[xi] + 2, z0s[zi] + 2, x1s[xi] - 2, z1s[zi] - 2);
                    continue;
                }
                if (xi == 1 && zi == 5) {
                    ruinCourt(c, ground, p, x0s[xi] + 2, z0s[zi] + 2, x1s[xi] - 2, z1s[zi] - 2);
                    continue;
                }
                if (zi == 12 && xi <= 1) {
                    continue; // deliberate ruin field on the far edge
                }
                int variant = Math.floorMod(xi * 7 + zi * 3, 6);
                siheyuan(c, ground, p, x0s[xi] + 2, z0s[zi] + 2, x1s[xi] - 2, z1s[zi] - 2, variant);
            }
        }
        elderCourt(c, ground, p, -235, -116, -205, -85);
    }

    private static void siheyuan(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                 int z1, int variant) {
        int y = ground;
        if (z1 - z0 < 18 || x1 - x0 < 18) {
            gardenCourt(c, ground, p, x0, z0, x1, z1);
            return;
        }
        wallRing(c, p, x0, z0, x1, z1, y, 3, p.wall());
        int gate = (x0 + x1) / 2;
        c.fill(gate, y + 1, z1, gate + 1, y + 2, z1, AIR);
        door(c, p, gate, y + 1, z1, Direction.SOUTH);
        greyRoof(c, gate - 2, z1 - 1, gate + 2, z1 + 1, y + 4, 2, 0);
        if (variant == 4) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    if ((x == x0 || x == x1 || z == z0 || z == z1)
                            && Math.floorMod(x * 13 + z * 7, 5) == 0) {
                        c.fill(x, y + 1, z, x, y + 4, z, AIR);
                    }
                }
            }
            c.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 2, z1 - 1, p.rubble());
            cypressAt(c, (x0 + x1) / 2, y, (z0 + z1) / 2);
            return;
        }
        if (variant == 3) {
            c.fill(x0 + 3, y, z1 - 6, x0 + 7, y, z1 - 2, WATER);
            cypressAt(c, x1 - 5, y, z1 - 6);
            broadTreeAt(c, x0 + 6, y, z0 + 14);
            return;
        }
        int hz0 = z0 + 2;
        int hz1 = z0 + 10;
        int hx0 = variant == 1 ? x0 + 6 : x0 + 3;
        int hx1 = variant == 1 ? x1 - 6 : x1 - 3;
        shell(c, hx0, hz0, hx1, hz1, y, 5, p.wall(), p.ground(), GREY_ROOF);
        int hc = (hx0 + hx1) / 2;
        c.fill(hc - 1, y + 1, hz1, hc + 1, y + 2, hz1, AIR);
        door(c, p, hc, y + 1, hz1, Direction.SOUTH);
        for (int x = hx0 + 3; x <= hx1 - 3; x += 4) {
            c.window(x, y + 3, hz1, 2, 1, true);
        }
        greyRoof(c, hx0 - 1, hz0 - 1, hx1 + 1, hz1 + 1, y + 5, 3, 0);
        if (variant == 2) {
            shell(c, x0 + 2, z0 + 14, x0 + 9, z0 + 22, y, 4, p.wall(), p.ground(), GREY_ROOF);
            greyRoof(c, x0 + 1, z0 + 13, x0 + 10, z0 + 23, y + 4, 2, 1);
        }
        if (variant == 5) {
            for (int x = x0 + 3; x <= x1 - 3; x += 6) {
                c.fill(x, y + 1, z1 - 8, x, y + 3, z1 - 8, p.frame());
            }
            c.fill(x0 + 2, y + 4, z1 - 9, x1 - 2, y + 4, z1 - 7, p.roofSlab());
            c.put(x0 + 3, y + 1, z1 - 5, Blocks.BARREL.defaultBlockState());
        }
        cypressAt(c, x1 - 4, y, z1 - 5);
        c.put(x0 + 3, y + 1, z1 - 3, Blocks.CAULDRON.defaultBlockState());
    }

    private static void gardenCourt(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                    int z1) {
        int y = ground;
        wallRing(c, p, x0, z0, x1, z1, y, 2, p.weathered());
        int gate = (x0 + x1) / 2;
        c.fill(gate, y + 1, z1, gate + 1, y + 2, z1, AIR);
        cypressAt(c, x0 + 4, y, z0 + 4);
        cypressAt(c, x1 - 4, y, z1 - 4);
        broadTreeAt(c, (x0 + x1) / 2, y, (z0 + z1) / 2);
        c.put(x0 + 3, y + 1, z1 - 3, Blocks.CAULDRON.defaultBlockState());
    }

    private static void wallRing(Ctx c, Palette p, int x0, int z0, int x1, int z1,
                                 int y, int h, BlockState state) {
        c.walls(x0, y + 1, z0, x1, y + h, z1, state);
        for (int x = x0; x <= x1; x++) {
            c.put(x, y + h + 1, z0, GREY_ROOF);
            c.put(x, y + h + 1, z1, GREY_ROOF);
        }
        for (int z = z0; z <= z1; z++) {
            c.put(x0, y + h + 1, z, GREY_ROOF);
            c.put(x1, y + h + 1, z, GREY_ROOF);
        }
    }

    private static void ruinCourt(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                  int z1) {
        int y = ground;
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                if (edge && (x * 31 + z * 17) % 5 != 0) {
                    int h = 1 + Math.floorMod(x * 13 + z * 7, 3);
                    c.fill(x, y + 1, z, x, y + h, z, p.weathered());
                }
            }
        }
        c.fill(x0 + 4, y + 1, z0 + 4, x0 + 6, y + 1, z0 + 6, p.rubble());
        c.put(x0 + 8, y + 1, z0 + 8, Palette.cobweb());
        cypressAt(c, x1 - 4, y, z0 + 4);
    }

    private static void hutongShrine(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                     int z1) {
        int y = ground;
        wallRing(c, p, x0, z0, x1, z1, y, 3, p.wall());
        int gate = (x0 + x1) / 2;
        c.fill(gate, y + 1, z1, gate + 1, y + 3, z1, AIR);
        door(c, p, gate, y + 1, z1, Direction.SOUTH);
        c.pitchedRoof(gate - 3, z1 - 1, gate + 3, z1 + 1, y + 4, 2, 0);
        shell(c, x0 + 4, z0 + 3, x1 - 4, z0 + 11, y, 6, p.accent(), p.ground(), p.roof());
        c.fill(gate, y + 1, z0 + 11, gate, y + 2, z0 + 11, AIR);
        door(c, p, gate, y + 1, z0 + 11, Direction.SOUTH);
        c.pitchedRoof(x0 + 2, z0 + 1, x1 - 2, z0 + 13, y + 6, 3, 0);
        c.put(x0 + 5, y + 1, z0 + 14, Blocks.CAULDRON.defaultBlockState());
        c.put(x0 + 5, y + 2, z0 + 14, p.light());
        signAt(c, ground, p, gate, z1 + 2, Direction.SOUTH, "ANCESTORS", "INCENSE", "QUIET");
    }

    private static void elderCourt(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                   int z1) {
        int y = ground;
        wallRing(c, p, x0, z0, x1, z1, y, 3, p.wall());
        c.fill(x0, y + 1, z0 + 18, x0, y + 2, z0 + 20, AIR);
        door(c, p, x0, y + 1, z0 + 19, Direction.WEST);
        greyRoof(c, x0 - 1, z0 + 17, x0 + 1, z0 + 21, y + 4, 2, 0);
        shell(c, x0 + 4, z0 + 2, x1 - 4, z0 + 10, y, 5, p.wall(), p.ground(), GREY_ROOF);
        c.fill((x0 + x1) / 2, y + 1, z0 + 10, (x0 + x1) / 2, y + 2, z0 + 10, AIR);
        door(c, p, (x0 + x1) / 2, y + 1, z0 + 10, Direction.SOUTH);
        greyRoof(c, x0 + 3, z0 + 1, x1 - 3, z0 + 11, y + 5, 3, 0);
        cypressAt(c, x1 - 5, y, z1 - 5);
        c.put(x0 + 5, y + 1, z1 - 4, Blocks.CAULDRON.defaultBlockState());
        signAt(c, ground, p, (x0 + x1) / 2, z1 + 2, Direction.SOUTH, "ELDER'S COURT", "MIND THE WELL");
    }

    // ------------------------------------------------------------------ Temple of Heaven

    private static void temple(Ctx c, int ground, Palette p) {
        parkWall(c, ground, p, -90, 140, -14, 290, new int[]{160, 270}, true);
        parkWall(c, ground, p, 14, 140, 90, 290, new int[]{160, 240}, false);
        parkGate(c, ground, p, -14, 160);
        parkGate(c, ground, p, -14, 270);
        parkGate(c, ground, p, 14, 160);
        parkGate(c, ground, p, 14, 240);
        templeHall(c, ground, p, 52, 210);
        altar(c, ground, p, 52, 265);
        pavilion(c, ground, p, -52, 210);
        pave(c, ground, 16, 159, 52, 161, p.foundation());
        pave(c, ground, 51, 159, 53, 194, p.foundation());
        pave(c, ground, 16, 239, 52, 241, p.foundation());
        pave(c, ground, 51, 226, 53, 253, p.foundation());
        pave(c, ground, -40, 159, -16, 161, p.foundation());
        pave(c, ground, -41, 159, -39, 271, p.foundation());
        pave(c, ground, -40, 269, -16, 271, p.foundation());
        pave(c, ground, -43, 209, -39, 211, p.foundation());
        pave(c, ground, -52, 219, -39, 219, p.foundation());
        int[] tx = {24, 34, 76, 80, 24, 76, 30, 70};
        int[] tz = {170, 185, 170, 200, 232, 250, 275, 280};
        for (int i = 0; i < tx.length; i++) {
            cypress(c, ground, tx[i], tz[i]);
        }
        int[] wx = {-80, -70, -84, -60, -80, -34, -30, -70};
        int[] wz = {160, 180, 210, 240, 270, 170, 250, 285};
        for (int i = 0; i < wx.length; i++) {
            cypress(c, ground, wx[i], wz[i]);
        }
    }

    private static void parkWall(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                 int z1, int[] gates, boolean eastWall) {
        wallSegments(c, ground, p, x0, z0, x1, z0 + 1);
        wallSegments(c, ground, p, x0, z1 - 1, x1, z1);
        int gateX = eastWall ? x1 - 1 : x0;
        int outerX = eastWall ? x0 : x1 - 1;
        wallSegments(c, ground, p, outerX, z0, outerX + 1, z1);
        int prev = z0;
        for (int gate : gates) {
            wallSegments(c, ground, p, gateX, prev, gateX + 1, gate - 2);
            prev = gate + 2;
        }
        wallSegments(c, ground, p, gateX, prev, gateX + 1, z1);
    }

    private static void wallSegments(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                     int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                int y = ground;
                c.fill(x, y, z, x, y + 2, z, p.wall());
                c.put(x, y + 3, z, GREY_ROOF);
            }
        }
    }

    private static void parkGate(Ctx c, int ground, Palette p, int x, int z) {
        int y = ground;
        c.fill(x, y + 1, z - 2, x, y + 4, z - 2, p.accent());
        c.fill(x, y + 1, z + 2, x, y + 4, z + 2, p.accent());
        c.fill(x, y + 4, z - 2, x, y + 4, z + 2, p.accent());
        c.pitchedRoof(x - 1, z - 3, x + 1, z + 3, y + 5, 2, 0);
        c.put(x, y + 4, z, p.light());
    }

    private static void templeHall(Ctx c, int ground, Palette p, int cx, int cz) {
        int y = ground;
        disc(c, cx, cz, 15, y, MARBLE);
        disc(c, cx, cz, 12, y + 1, MARBLE);
        disc(c, cx, cz, 9, y + 2, MARBLE);
        ring(c, cx, cz, 6, 7, y + 3, y + 11, p.accent());
        for (int a = 0; a < 16; a++) {
            double ang = a * Math.PI / 8.0;
            int wx = cx + (int) Math.round(Math.cos(ang) * 7);
            int wz = cz + (int) Math.round(Math.sin(ang) * 7);
            c.put(wx, y + 6, wz, p.window());
            c.put(wx, y + 7, wz, p.window());
        }
        disc(c, cx, cz, 6, y + 3, p.ground());
        disc(c, cx, cz, 6, y + 11, p.roof());
        c.fill(cx - 1, y + 3, cz + 7, cx + 1, y + 5, cz + 7, AIR);
        for (int i = 0; i < 8; i++) {
            disc(c, cx, cz, 10 - i, y + 12 + i, BLUE_ROOF);
        }
        c.put(cx, y + 20, cz, BLUE_RIDGE);
        c.put(cx, y + 21, cz, p.roof());
        c.put(cx, y + 22, cz, p.light());
    }

    private static void altar(Ctx c, int ground, Palette p, int cx, int cz) {
        int y = ground;
        disc(c, cx, cz, 10, y, MARBLE);
        disc(c, cx, cz, 7, y + 1, MARBLE);
        disc(c, cx, cz, 4, y + 2, MARBLE);
        c.fill(cx - 1, y + 3, cz - 1, cx + 1, y + 3, cz + 1, p.foundation());
        c.put(cx, y + 4, cz, p.light());
    }

    private static void pavilion(Ctx c, int ground, Palette p, int cx, int cz) {
        int y = ground;
        c.fill(cx - 8, y, cz - 8, cx + 8, y, cz + 8, p.foundation());
        shell(c, cx - 6, cz - 6, cx + 6, cz + 6, y + 1, 6, p.accent(), p.ground(), p.roof());
        c.fill(cx, y + 2, cz + 6, cx, y + 3, cz + 6, AIR);
        door(c, p, cx, y + 2, cz + 6, Direction.SOUTH);
        c.pitchedRoof(cx - 8, cz - 8, cx + 8, cz + 8, y + 7, 4, 0);
    }

    // ------------------------------------------------------------------ silk market

    private static void market(Ctx c, int ground, Palette p) {
        pave(c, ground, 216, -90, 223, 120, p.ground());
        pave(c, ground, 246, -90, 253, 120, p.ground());
        pave(c, ground, 164, -64, 320, -56, p.ground());
        pave(c, ground, 164, 56, 320, 64, p.ground());
        paifang(c, ground, p, 220, -90, 5);
        paifang(c, ground, p, 220, 120, 5);
        paifang(c, ground, p, 250, -90, 4);
        paifang(c, ground, p, 250, 120, 4);
        marketShop(c, ground, p, 198, -88, 214, -70, 0, 0);
        marketShop(c, ground, p, 198, -26, 214, -12, 0, 2);
        marketShop(c, ground, p, 198, 12, 214, 30, 0, 3);
        marketShop(c, ground, p, 198, 32, 214, 52, 0, 4);
        marketShop(c, ground, p, 198, 66, 214, 84, 0, 5);
        marketShop(c, ground, p, 198, 86, 214, 110, 0, 6);
        marketShop(c, ground, p, 225, -88, 241, -70, 1, 7);
        marketShop(c, ground, p, 225, -54, 241, -36, 1, 8);
        marketShop(c, ground, p, 225, -34, 241, -12, 1, 9);
        marketShop(c, ground, p, 225, 34, 241, 52, 1, 10);
        marketShop(c, ground, p, 225, 66, 241, 84, 1, 11);
        marketShop(c, ground, p, 225, 86, 241, 110, 1, 12);
        marketShop(c, ground, p, 176, -80, 194, -66, 0, 20);
        marketShop(c, ground, p, 176, -54, 194, -30, 0, 21);
        marketShop(c, ground, p, 176, 10, 194, 50, 0, 22);
        marketShop(c, ground, p, 176, 66, 194, 90, 0, 23);
        marketShop(c, ground, p, 256, -88, 274, -70, 1, 13);
        marketShop(c, ground, p, 256, -54, 274, -36, 1, 14);
        marketShop(c, ground, p, 256, -34, 274, -12, 1, 15);
        marketShop(c, ground, p, 256, 12, 274, 30, 1, 16);
        marketShop(c, ground, p, 256, 34, 274, 52, 1, 17);
        marketShop(c, ground, p, 256, 66, 274, 84, 1, 18);
        marketShop(c, ground, p, 256, 86, 274, 110, 1, 19);
        teaHouse(c, ground, p, 196, -52, 214, -28);
        silkShop(c, ground, p, 225, 12, 241, 32);
        for (int z = -80; z <= 110; z += 30) {
            c.put(216, ground + 1, z, Blocks.BARREL.defaultBlockState());
            c.put(223, ground + 1, z + 8, Blocks.BARREL.defaultBlockState());
        }
    }

    private static void paifang(Ctx c, int ground, Palette p, int cx, int cz, int halfW) {
        int y = ground;
        c.fill(cx - halfW, y + 1, cz, cx - halfW, y + 7, cz, p.accent());
        c.fill(cx + halfW, y + 1, cz, cx + halfW, y + 7, cz, p.accent());
        c.fill(cx - halfW, y + 6, cz, cx + halfW, y + 7, cz, p.accent());
        c.pitchedRoof(cx - halfW - 2, cz - 2, cx + halfW + 2, cz + 2, y + 8, 3, 0);
        c.put(cx, y + 6, cz, p.light());
    }

    private static void marketShop(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                   int z1, int side, int idx) {
        int y = ground;
        shell(c, x0, z0, x1, z1, y, 5, p.wall(), p.ground(), GREY_ROOF);
        int front = side == 0 ? x1 : x0;
        c.fill(front, y + 1, z0 + 1, front, y + 4, z1 - 1, AIR);
        if (idx % 7 == 0) {
            c.fill(front, y + 1, z0 + 1, front, y + 4, z1 - 1, p.frame());
            c.put(front, y + 2, z0 + 2, Palette.cobweb());
        } else {
            c.fill(front, y + 1, z0 + 2, front, y + 1, z1 - 2, p.foundation());
            c.put(front, y + 2, z0 + 2, Blocks.BARREL.defaultBlockState());
            c.put(front, y + 2, z1 - 3, Blocks.BARREL.defaultBlockState());
        }
        BlockState cloth = idx % 2 == 0 ? Blocks.WOOL.pick(DyeColor.RED).defaultBlockState()
                : Blocks.WOOL.pick(DyeColor.YELLOW).defaultBlockState();
        int ax0 = side == 0 ? x1 + 1 : x0 - 3;
        int ax1 = side == 0 ? x1 + 3 : x0 - 1;
        c.fill(ax0, y + 4, z0 + 1, ax1, y + 4, z1 - 1, cloth);
        greyRoof(c, x0 - 1, z0 - 1, x1 + 1, z1 + 1, y + 5, 3, 1);
        int sx = side == 0 ? x1 + 2 : x0 - 2;
        signAt(c, ground, p, sx, z0 + 2, side == 0 ? Direction.EAST : Direction.WEST,
                "GOODS FOR SALE", "FAIR PRICES");
    }

    private static void teaHouse(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                 int z1) {
        int y = ground;
        shell(c, x0, z0, x1, z1, y, 6, p.wall(), p.ground(), GREY_ROOF);
        c.fill(x1, y + 1, z0 + 8, x1, y + 2, z0 + 10, AIR);
        door(c, p, x1, y + 1, z0 + 9, Direction.EAST);
        for (int x = x0 + 3; x <= x1 - 3; x += 4) {
            c.window(x, y + 3, z1, 2, 1, true);
        }
        greyRoof(c, x0 - 1, z0 - 1, x1 + 1, z1 + 1, y + 6, 3, 1);
        shell(c, x0 + 2, z0 + 2, x1 - 2, z1 - 2, y + 9, 4, p.accent(), p.ground(), GREY_ROOF);
        greyRoof(c, x0 + 1, z0 + 1, x1 - 1, z1 - 1, y + 13, 3, 1);
        c.fill(x1 + 1, y + 6, z0 + 2, x1 + 3, y + 6, z1 - 2, p.roofSlab());
        c.fill(x1 + 3, y + 7, z0 + 2, x1 + 3, y + 8, z1 - 2, p.rail());
        c.fill(x1 + 1, y + 7, z0 + 5, x1 + 1, y + 7, z0 + 5, Blocks.CAULDRON.defaultBlockState());
        c.put(x1 + 2, y + 7, z0 + 8, p.light());
    }

    private static void silkShop(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                 int z1) {
        int y = ground;
        shell(c, x0, z0, x1, z1, y, 5, p.wall(), p.ground(), GREY_ROOF);
        c.fill(x0, y + 1, z0 + 1, x0, y + 4, z1 - 1, AIR);
        c.fill(x0, y + 1, z0 + 2, x0, y + 1, z1 - 2, p.foundation());
        for (int z = z0 + 2; z <= z1 - 2; z += 4) {
            c.put(x0 + 2, y + 2, z, Blocks.WOOL.pick(DyeColor.WHITE).defaultBlockState());
            c.put(x0 + 3, y + 2, z, Blocks.WOOL.pick(DyeColor.RED).defaultBlockState());
            c.put(x0 + 4, y + 2, z, Blocks.WOOL.pick(DyeColor.YELLOW).defaultBlockState());
        }
        c.fill(x0 - 3, y + 4, z0 + 1, x0 - 1, y + 4, z1 - 1, Blocks.WOOL.pick(DyeColor.RED).defaultBlockState());
        greyRoof(c, x0 - 1, z0 - 1, x1 + 1, z1 + 1, y + 5, 3, 1);
    }

    // ------------------------------------------------------------------ railway / legation

    private static void railway(Ctx c, int ground, Palette p, int stationBase, int legationBase) {
        pave(c, stationBase, 176, 218, 264, 234, p.foundation());
        stationHall(c, stationBase, p, 190, 236, 250, 272);
        clockTower(c, stationBase, p, 196, 242, 206, 252);
        pave(c, stationBase, 163, 276, 270, 284, p.foundation());
        for (int x = 170; x <= 260; x += 15) {
            int py = stationBase + 1;
            c.fill(x, py + 1, 280, x, py + 5, 280, p.rail());
            c.put(x, py + 6, 280, p.light());
        }
        c.fill(163, stationBase + 1, 285, 270, stationBase + 1, 289, p.foundation());
        c.fill(165, stationBase + 7, 277, 265, stationBase + 7, 289, p.roofSlab());
        for (int z : new int[]{290, 298}) {
            for (int x = 164; x <= 330; x++) {
                int y = stationBase;
                c.fill(x, y, z - 1, x, y, z + 1, Blocks.GRAVEL.defaultBlockState());
                c.put(x, y + 1, z, RAIL);
            }
        }
        c.fill(164, stationBase, 288, 164, stationBase + 2, 300,
                p.foundation());
        warehouse(c, ground, p, 176, 150, 198, 168);
        warehouse(c, ground, p, 176, 174, 198, 190);
        warehouse(c, ground, p, 176, 196, 198, 210);
        coalYard(c, ground, p, 204, 150, 224, 172);
        legation(c, legationBase, p);
    }

    private static void stationHall(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                    int z1) {
        int y = ground;
        shell(c, x0, z0, x1, z1, y, 12, p.wall(), p.ground(), GREY_ROOF);
        int cx = (x0 + x1) / 2;
        c.fill(cx - 2, y + 1, z1, cx + 2, y + 3, z1, AIR);
        door(c, p, cx, y + 1, z1, Direction.SOUTH);
        c.fill(cx - 1, y + 1, z0, cx + 1, y + 2, z0, AIR);
        door(c, p, cx, y + 1, z0, Direction.NORTH);
        for (int x = x0 + 5; x <= x1 - 5; x += 7) {
            c.window(x, y + 3, z1, 6, 2, true);
            c.window(x, y + 3, z0, 6, 2, true);
            c.window(x, y + 8, z1, 3, 2, true);
        }
        for (int z = z0 + 6; z <= z1 - 6; z += 7) {
            c.window(x0, y + 3, z, 6, 2, true);
            c.window(x1, y + 3, z, 6, 2, true);
        }
        c.fill(x0 - 2, y + 12, z0 - 2, x1 + 2, y + 12, z1 + 2, GREY_ROOF);
        c.pitchedRoof(x0 - 3, z0 - 3, x1 + 3, z1 + 3, y + 13, 5, 0);
        c.fill(cx - 4, y + 12, z1 + 3, cx + 4, y + 12, z1 + 5, p.roofSlab());
        for (int x = cx - 4; x <= cx + 4; x += 4) {
            c.fill(x, y + 1, z1 + 4, x, y + 11, z1 + 4, p.wall());
        }
        c.put(cx, y + 11, z1 + 4, p.light());
    }

    private static void clockTower(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                   int z1) {
        int y = ground;
        c.fill(x0, y, z0, x1, y + 24, z1, p.wall());
        c.fill(x0, y + 20, z0, x1, y + 21, z1, p.accent());
        for (int x = x0 + 2; x <= x1 - 2; x += 3) {
            c.window(x, y + 6, z1, 5, 1, true);
            c.window(x, y + 13, z1, 4, 1, true);
        }
        c.fill(x0 + 3, y + 18, z1, x1 - 3, y + 18, z1, Blocks.CONCRETE.pick(DyeColor.WHITE).defaultBlockState());
        c.fill(x0 + 4, y + 19, z1, x1 - 4, y + 19, z1, Blocks.CONCRETE.pick(DyeColor.WHITE).defaultBlockState());
        c.put((x0 + x1) / 2, y + 19, z1, Blocks.CONCRETE.pick(DyeColor.BLACK).defaultBlockState());
        c.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y + 25, 4, 0);
        c.put((x0 + x1) / 2, y + 29, (z0 + z1) / 2, p.light());
    }

    private static void warehouse(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                  int z1) {
        int y = ground;
        shell(c, x0, z0, x1, z1, y, 7, p.wall(), p.ground(), GREY_ROOF);
        int cx = (x0 + x1) / 2;
        c.fill(x1, y + 1, midZ(z0, z1), x1, y + 4, midZ(z0, z1) + 4, AIR);
        c.fill(cx - 2, y + 1, z1, cx + 2, y + 4, z1, AIR);
        greyRoof(c, x0 - 1, z0 - 1, x1 + 1, z1 + 1, y + 7, 3, 1);
        c.fill(x1 + 1, y + 1, z0 + 2, x1 + 4, y + 1, z1 - 2, p.foundation());
        c.put(x1 + 2, y + 2, z0 + 3, Blocks.BARREL.defaultBlockState());
        c.put(x1 + 3, y + 2, z1 - 4, Blocks.BARREL.defaultBlockState());
    }

    private static int midZ(int z0, int z1) {
        return (z0 + z1) / 2;
    }

    private static void coalYard(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                 int z1) {
        int y = ground;
        c.fill(x0, y + 1, z0, x0, y + 2, z1, p.rail());
        c.fill(x1, y + 1, z0, x1, y + 2, z1, p.rail());
        c.fill(x0, y + 1, z0, x1, y + 1, z0, p.rail());
        c.fill(x0 + 3, y + 1, z0 + 3, x0 + 8, y + 2, z0 + 7,
                Blocks.COAL_BLOCK.defaultBlockState());
        c.put(x0 + 10, y + 1, z1 - 4, Blocks.BARREL.defaultBlockState());
        c.put(x0 + 12, y + 1, z1 - 4, Blocks.BARREL.defaultBlockState());
        c.fill(x0 + 10, y + 1, z0 + 2, x0 + 10, y + 5, z0 + 2, p.rail());
        c.fill(x0 + 8, y + 5, z0 + 2, x0 + 10, y + 5, z0 + 2, p.rail());
    }

    private static void legation(Ctx c, int ground, Palette p) {
        int y = ground;
        c.fill(272, y, 152, 330, y + 4, 155, p.wall());
        c.fill(272, y, 152, 275, y + 4, 216, p.wall());
        c.fill(327, y, 152, 330, y + 4, 216, p.wall());
        c.fill(272, y, 213, 295, y + 4, 216, p.wall());
        c.fill(305, y, 213, 330, y + 4, 216, p.wall());
        pave(c, ground, 276, 156, 326, 212, p.foundation());
        int hy = y + 1;
        shell(c, 280, 160, 320, 192, hy, 10, p.wall(), p.foundation(), GREY_ROOF);
        c.fill(298, hy + 1, 192, 302, hy + 3, 192, AIR);
        door(c, p, 300, hy + 1, 192, Direction.SOUTH);
        for (int x = 284; x <= 316; x += 8) {
            c.window(x, hy + 3, 192, 5, 2, true);
            c.window(x, hy + 3, 160, 5, 2, true);
            c.fill(x, hy + 1, 193, x, hy + 6, 193, p.accent());
        }
        c.fill(282, hy + 1, 193, 318, hy + 1, 195, p.foundation());
        c.fill(282, hy + 7, 193, 318, hy + 7, 195, p.roofSlab());
        c.pitchedRoof(277, 157, 323, 195, hy + 10, 5, 0);
        c.fill(284, hy + 1, 196, 316, hy + 1, 198, p.foundation());
        c.put(290, hy + 2, 197, Blocks.CAULDRON.defaultBlockState());
        c.put(310, hy + 2, 197, p.light());
        cellar(c, p, 310, 200, y, 300, 196, 310, 204);
        signOn(c, p, 312, y, 204, Direction.SOUTH, "ARCHIVE", "BY TREATY", "NO ENTRY");
        signOn(c, p, 300, y, 211, Direction.SOUTH, "LEGATION QTR", "FOREIGN POWERS",
                "NO LOITERING");
    }

    private static void cellar(Ctx c, Palette p, int sx, int sz, int top, int x0,
                               int z0, int x1, int z1) {
        int floor = top - 4;
        chamber(c, p, x0, z0, x1, z1, floor, 3);
        ladderShaft(c, p, sx, sz, top, floor, Direction.WEST);
        c.chest(sx - 4, floor + 1, sz - 2, SiteLoot.CULT);
        c.sign(sx - 3, floor + 1, sz, Direction.WEST, "THE CHOIR", "BUYS SEALS", "SAY NOTHING");
    }

    // ------------------------------------------------------------------ bridges / arrival

    private static void bridges(Ctx c, int ground, Palette p) {
        bridge(c, ground, p, 0, 7);
        bridge(c, ground, p, 250, 4);
    }

    private static void bridge(Ctx c, int ground, Palette p, int zc, int halfW) {
        int deck = ground + 3;
        ramp(c, ground, p, 128, 137, zc - halfW, zc + halfW, deck);
        ramp(c, ground, p, 174, 165, zc - halfW, zc + halfW, deck);
        c.fill(138, deck, zc - halfW, 164, deck, zc + halfW, p.ground());
        for (int x = 138; x <= 164; x++) {
            c.put(x, deck + 1, zc - halfW, p.rail());
            c.put(x, deck + 1, zc + halfW, p.rail());
        }
        c.fill(140, deck - 3, zc - halfW, 142, deck - 1, zc + halfW, p.foundation());
        c.fill(160, deck - 3, zc - halfW, 162, deck - 1, zc + halfW, p.foundation());
        boat(c, ground, p, 151, zc - 14);
    }

    private static void ramp(Ctx c, int ground, Palette p, int lowX, int highX, int z0,
                             int z1, int deck) {
        int n = Math.abs(highX - lowX);
        int step = Integer.signum(highX - lowX);
        for (int i = 0; i <= n; i++) {
            int x = lowX + i * step;
            int base = ground;
            int h = base + (int) Math.round((deck - base) * (i / (double) n));
            c.fill(x, base, z0, x, h, z1, p.foundation());
            c.fill(x, h, z0, x, h, z1, p.ground());
        }
    }

    private static void boat(Ctx c, int ground, Palette p, int x, int z) {
        int y = ground - 1;
        c.fill(x - 1, y, z - 4, x + 1, y, z + 4, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        c.fill(x - 1, y + 1, z - 4, x - 1, y + 1, z + 4, Blocks.DARK_OAK_SLAB.defaultBlockState());
        c.fill(x + 1, y + 1, z - 4, x + 1, y + 1, z + 4, Blocks.DARK_OAK_SLAB.defaultBlockState());
        c.fill(x - 1, y + 1, z - 4, x + 1, y + 1, z - 4, Blocks.DARK_OAK_SLAB.defaultBlockState());
        c.fill(x - 1, y + 1, z + 4, x + 1, y + 1, z + 4, Blocks.DARK_OAK_SLAB.defaultBlockState());
        c.fill(x, y + 1, z, x, y + 5, z, p.rail());
        c.put(x, y + 6, z, p.light());
    }

    private static void arrival(Ctx c, int ground, Palette p) {
        int y = ground;
        towerBlock(c, p, -14, 316, -9, 324, y, 13);
        towerBlock(c, p, 9, 316, 14, 324, y, 13);
        c.fill(-9, y + 9, 318, 9, y + 11, 322, p.accent());
        c.pitchedRoof(-17, 313, 17, 327, y + 14, 4, 0);
        c.fill(-7, y, 316, 7, y, 324, p.ground());
        wallFragments(c, ground, p, -44, 320, -15, 323);
        wallFragments(c, ground, p, 15, 320, 44, 323);
        stoneLion(c, ground, p, -10, 330);
        stoneLion(c, ground, p, 10, 330);
        signAt(c, ground, p, 0, 326, Direction.SOUTH, "BEIJING", "IMPERIAL CITY", "ALL DISTRICTS",
                "PALACE NORTH");
        signAt(c, ground, p, -10, 326, Direction.SOUTH, "PALACE - N", "TEMPLE - S", "HUTONGS - W",
                "MARKET - E");
    }

    private static void towerBlock(Ctx c, Palette p, int x0, int z0, int x1,
                                   int z1, int y, int h) {
        c.fill(x0, y, z0, x1, y + h, z1, p.wall());
        c.fill(x0, y + 3, z0, x1, y + 4, z1, p.accent());
        c.fill(x0, y + 9, z0, x1, y + 10, z1, p.accent());
    }

    private static void wallFragments(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                      int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                int y = ground;
                c.fill(x, y, z, x, y + 6, z, p.wall());
            }
        }
        for (int x = x0; x <= x1; x++) {
            int y = ground;
            if ((x & 1) == 0) {
                c.fill(x, y + 7, z0, x, y + 8, z0, p.accent());
            }
        }
    }

    private static void stoneLion(Ctx c, int ground, Palette p, int x, int z) {
        int y = ground;
        c.fill(x - 1, y, z - 1, x + 1, y, z + 1, p.foundation());
        c.put(x, y + 1, z, p.wall());
        c.put(x, y + 2, z, p.wall());
        c.put(x + 1, y + 1, z, p.wall());
        c.put(x - 1, y + 2, z, p.wall());
    }

    // ------------------------------------------------------------------ gardens / secrets

    private static void gardens(Ctx c, int ground, Palette p) {
        gardenPond(c, ground, p, 94, 84, 132, 130);
        gardenPond(c, ground, p, -132, 150, -96, 240);
        ruinField(c, ground, p, -330, 202, -243, 220);
        ruinField(c, ground, p, -330, -280, -323, -243);
        for (int z = -336; z <= -318; z += 6) {
            cypress(c, ground, -20, z);
            cypress(c, ground, 20, z);
        }
        pave(c, ground, -4, -340, 4, -314, p.foundation());
    }

    private static void gardenPond(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                   int z1) {
        int y = ground;
        c.walls(x0, y + 1, z0, x1, y + 2, z1, p.weathered());
        int cx = (x0 + x1) / 2;
        int cz = (z0 + z1) / 2;
        c.fill(cx - 1, y + 1, z1, cx + 1, y + 2, z1, AIR);
        disc(c, cx, cz, 5, y, WATER);
        ring(c, cx, cz, 6, 6, y, y, p.foundation());
        broadTreeAt(c, x0 + 5, y, z0 + 5);
        broadTreeAt(c, x1 - 5, y, z1 - 5);
        cypressAt(c, x0 + 5, y, z1 - 5);
        c.put(cx, y + 1, cz - 7, p.light());
        c.fill(cx - 2, y + 1, cz + 7, cx + 2, y + 1, cz + 7, p.foundation());
    }

    private static void ruinField(Ctx c, int ground, Palette p, int x0, int z0, int x1,
                                  int z1) {
        int y = ground;
        for (int x = x0; x <= x1; x += 4) {
            for (int z = z0; z <= z1; z += 4) {
                if ((x * 17 + z * 29) % 3 == 0) {
                    c.fill(x, y, z, x + 2, y + 1 + Math.floorMod(x + z, 3), z, p.weathered());
                }
            }
        }
        c.fill(x0 + 4, y, z0 + 4, x0 + 8, y, z0 + 8, p.rubble());
        cypressAt(c, x0 + 10, y, z0 + 10);
        cypressAt(c, x1 - 6, y, z1 - 6);
    }

    private static void secrets(Ctx c, int ground, Palette p) {
        well(c, ground, p, -240, -120);
        int top = ground;
        int floor = top - 7;
        chamber(c, p, -237, -122, -228, -118, floor, 3);
        chamber(c, p, -228, -124, -218, -116, floor, 4);
        c.fill(-237, floor + 1, -120, -237, floor + 2, -120, AIR);
        c.fill(-228, floor + 1, -120, -228, floor + 2, -120, AIR);
        ladderShaft(c, p, -238, -120, top, floor, Direction.SOUTH);
        c.chest(-225, floor + 1, -122, SiteLoot.RELIC);
        c.chest(-220, floor + 1, -118, SiteLoot.COMMON);
        c.put(-227, floor, -120, p.foundation());
        c.sign(-227, floor + 1, -120, Direction.EAST, "THE SEAL", "WAS NEVER", "HERE",
                "ONLY ASHES");
        c.put(-232, floor + 1, -119, Palette.cobweb());
        c.put(-223, floor + 1, -121, Palette.cobweb());
        c.put(-230, floor + 1, -120, p.light());
    }

    private static void well(Ctx c, int ground, Palette p, int x, int z) {
        int y = ground;
        c.fill(x - 1, y + 1, z - 1, x + 1, y + 1, z + 1, p.foundation());
        c.put(x, y + 1, z, AIR);
        c.fill(x, y - 4, z, x, y - 1, z, AIR);
        c.put(x, y - 4, z, WATER);
        c.put(x - 2, y + 1, z, p.rail());
        c.put(x - 2, y + 2, z, p.rail());
        c.put(x - 2, y + 3, z, p.light());
    }

    private static void chamber(Ctx c, Palette p, int x0, int z0, int x1, int z1,
                                int floorY, int height) {
        c.fill(x0, floorY, z0, x1, floorY + height, z1, p.wall());
        c.fill(x0 + 1, floorY, z0 + 1, x1 - 1, floorY, z1 - 1, p.foundation());
        c.fill(x0 + 1, floorY + 1, z0 + 1, x1 - 1, floorY + height - 1, z1 - 1, AIR);
    }

    private static void ladderShaft(Ctx c, Palette p, int x, int z, int topY,
                                    int bottomY, Direction wall) {
        for (int y = bottomY; y <= topY; y++) {
            c.put(x, y, z, AIR);
            if (y < topY) {
                c.put(x, y, z, Blocks.LADDER.defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, wall));
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx != 0 || dz != 0) {
                    c.put(x + dx, topY + 1, z + dz, p.foundation());
                }
            }
        }
        c.put(x + 1, topY + 1, z + 1, LAMP);
    }

    // ------------------------------------------------------------------ signs

    private static void signs(Ctx c, int ground, Palette p) {
        signAt(c, ground, p, 10, -55, Direction.SOUTH, "FORBIDDEN CITY", "NO ENTRY", "AFTER DUSK",
                "BY ORDER");
        signAt(c, ground, p, 10, -160, Direction.SOUTH, "SUPREME HARMONY", "THRONE HALL", "KNEEL");
        signAt(c, ground, p, 10, -290, Direction.SOUTH, "MERIDIAN GATE", "NO PASSAGE", "BY ORDER");
        signAt(c, ground, p, 10, 145, Direction.SOUTH, "TEMPLE OF", "HEAVEN", "PRIEST HEALS",
                "PARK OPEN");
        signAt(c, ground, p, -152, 10, Direction.EAST, "HUTONGS", "OLD LANES", "WELL AT -240",
                "MIND THE DARK");
        signAt(c, ground, p, 170, 10, Direction.WEST, "SILK MARKET", "TEA HOUSES", "MIND PURSES");
        signAt(c, ground, p, 168, 135, Direction.NORTH, "RAILWAY", "STATION AHEAD", "CARGO OFFICE");
        signAt(c, ground, p, 270, 218, Direction.SOUTH, "LEGATION QTR", "FOREIGN POWERS",
                "NO LOITERING");
        signAt(c, ground, p, -140, -60, Direction.EAST, "DEEP WATER", "NO SWIMMING", "KEEP BACK");
        signAt(c, ground, p, -236, -118, Direction.EAST, "OLD WELL", "DO NOT FALL", "STAIRS BELOW");
        signAt(c, ground, p, 216, -40, Direction.EAST, "TEA HOUSE", "BEDS UPSTAIRS", "HOT WATER");
        signAt(c, ground, p, 224, 20, Direction.WEST, "SILK & TEA", "FAIR PRICES", "NO CREDIT");
        signAt(c, ground, p, -300, 82, Direction.NORTH, "ANCESTORS", "INCENSE", "QUIET");
        signAt(c, ground, p, 220, 234, Direction.NORTH, "RAILWAY STATION", "PLATFORM 1",
                "CARGO WEST");
        signAt(c, ground, p, 139, -8, Direction.SOUTH, "CANAL BRIDGE", "MOORED BOATS", "EAST QUAY");
    }
}
