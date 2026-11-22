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
import org.jspecify.annotations.Nullable;

/**
 * Sydney between the wars: the harbour and its ferries, the Opera House sails on Bennelong Point,
 * The Rocks' sandstone lanes and pubs, the rail yards and their coal smoke, Bondi's beach and
 * pavilion, the Botanic Garden's avenue and museum, and the bush and sea cave beyond.
 *
 * <p>Every block is authored. There is no {@code RandomSource}, no layout hash, no static mutable
 * state and no read of the generating chunk: the whole city is written on every call and
 * {@link StructureBuilder} keeps only the slice inside the current chunk. The flat heart uses
 * {@code ground}; rim and outskirt props sample {@link CitySurface#at(int, int)} through the
 * terrain-aware {@link #build(StructureBuilder, CityPlanContext, Palette)} overload.
 */
public final class SydneyPlan implements CityPlan {

    /** Authored half-extent; every offset below stays inside +/-336. */
    private static final int HALF_EXTENT = 340;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState SANDSTONE = Blocks.SANDSTONE.defaultBlockState();
    private static final BlockState CUT_SANDSTONE = Blocks.CUT_SANDSTONE.defaultBlockState();
    private static final BlockState SAND_STAIRS = Blocks.SANDSTONE_STAIRS.defaultBlockState();
    private static final BlockState GLASS = Blocks.GLASS.defaultBlockState();
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState COAL = Blocks.COAL_BLOCK.defaultBlockState();
    private static final BlockState HAY = Blocks.HAY_BLOCK.defaultBlockState();
    private static final BlockState BARREL = Blocks.BARREL.defaultBlockState();
    private static final BlockState BOOKS = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState CAULDRON = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState LILY = Blocks.LILY_PAD.defaultBlockState();
    private static final BlockState OAK_LOG = Blocks.OAK_LOG.defaultBlockState();
    private static final BlockState OAK_LEAF = Blocks.OAK_LEAVES.defaultBlockState()
            .setValue(BlockStateProperties.PERSISTENT, true);
    private static final BlockState PINE_LOG = Blocks.SPRUCE_LOG.defaultBlockState();
    private static final BlockState PINE_LEAF = Blocks.SPRUCE_LEAVES.defaultBlockState()
            .setValue(BlockStateProperties.PERSISTENT, true);
    private static final BlockState JUNGLE_LOG = Blocks.JUNGLE_LOG.defaultBlockState();
    private static final BlockState JUNGLE_LEAF = Blocks.JUNGLE_LEAVES.defaultBlockState()
            .setValue(BlockStateProperties.PERSISTENT, true);
    private static final BlockState WHITE_WOOL = Materials.wool(DyeColor.WHITE);
    private static final BlockState RED_WOOL = Materials.wool(DyeColor.RED);
    private static final BlockState BLUE_WOOL = Materials.wool(DyeColor.BLUE);
    private static final BlockState YELLOW_WOOL = Materials.wool(DyeColor.YELLOW);
    private static final BlockState WHITE = Materials.concrete(DyeColor.WHITE);
    private static final BlockState LIGHT = Materials.lightGrayConcrete();
    private static final BlockState DARK = Materials.concrete(DyeColor.BLACK);
    private static final BlockState RED_TERRACOTTA = Materials.terracotta(DyeColor.RED);
    private static final BlockState RAIL_EW = Blocks.RAIL.defaultBlockState()
            .setValue(BlockStateProperties.RAIL_SHAPE, RailShape.EAST_WEST);
    private static final BlockState RAIL_NS = Blocks.RAIL.defaultBlockState();

    @Override
    public String cityId() {
        return "sydney";
    }

    @Override
    public int halfExtent() {
        return HALF_EXTENT;
    }

    @Override
    public void build(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        buildCity(b, cx, cz, ground, null, p);
    }

    @Override
    public void build(StructureBuilder b, CityPlanContext ctx, Palette p) {
        buildCity(b, ctx.cx(), ctx.cz(), ctx.ground(), ctx.surface(), p);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.LANDMARK, 0, 0, 320, "gate"),
                new CityPoi("gate_signpost", CityPoi.Kind.SIGN, -18, 3, 334,
                        "OPERA HOUSE|THE ROCKS|RAIL YARDS|BONDI"),
                new CityPoi("city_notices", CityPoi.Kind.SIGN, 44, 3, 328, "CITY NOTICES"),
                new CityPoi("harbour_quest_board", CityPoi.Kind.QUEST_BOARD, 0, 1, -120,
                        "sydney_missing_sailor,sydney_smuggled_cargo"),
                new CityPoi("quest_board_sign", CityPoi.Kind.SIGN, 0, 3, -128,
                        "MISSING SAILOR|SMUGGLED CARGO"),
                new CityPoi("opera_house", CityPoi.Kind.LANDMARK, 0, 0, -200,
                        "sails,podium,promenade"),
                new CityPoi("opera_sign", CityPoi.Kind.SIGN, 0, 3, -150, "OPERA HOUSE"),
                new CityPoi("harbour_port", CityPoi.Kind.PORT, -38, 1, -240,
                        "ferry,wharf,deep_water"),
                new CityPoi("ferry_master", CityPoi.Kind.NPC, 220, 0, -120,
                        "ferry_master,sydney_missing_sailor"),
                new CityPoi("ferry_sign", CityPoi.Kind.SIGN, 232, 3, -136,
                        "FERRY|WOOLOOMOOLOO"),
                new CityPoi("customs_officer", CityPoi.Kind.NPC, 238, 0, 2,
                        "customs_officer,sydney_smuggled_cargo"),
                new CityPoi("customs_sign", CityPoi.Kind.SIGN, 232, 3, 14,
                        "SMUGGLED CARGO|SEE CUSTOMS"),
                new CityPoi("rocks_pub", CityPoi.Kind.INN, -240, 0, -60, "publican"),
                new CityPoi("publican", CityPoi.Kind.NPC, -238, 0, -58, "publican"),
                new CityPoi("rocks_shop", CityPoi.Kind.SHOP, -240, 0, 60, "shopkeeper"),
                new CityPoi("shopkeeper", CityPoi.Kind.NPC, -238, 0, 58, "shopkeeper"),
                new CityPoi("apothecary", CityPoi.Kind.HEALER, -200, 0, 120, "remedies"),
                new CityPoi("apothecary_npc", CityPoi.Kind.NPC, -198, 0, 118, "apothecary"),
                new CityPoi("rocks_market", CityPoi.Kind.LANDMARK, -285, 0, -90,
                        "market,stalls"),
                new CityPoi("constable", CityPoi.Kind.NPC, -285, 0, -88, "constable"),
                new CityPoi("rocks_sign", CityPoi.Kind.SIGN, -136, 3, 6, "THE ROCKS"),
                new CityPoi("rail_station", CityPoi.Kind.STATION, 210, 1, -40,
                        "station,clock,tracks_east"),
                new CityPoi("rail_sign", CityPoi.Kind.SIGN, 136, 3, 146, "RAIL YARDS"),
                new CityPoi("bondi_pavilion", CityPoi.Kind.LANDMARK, 240, 0, 220,
                        "pavilion,beach"),
                new CityPoi("surf_lifesaver", CityPoi.Kind.NPC, 240, 0, 250, "surf_lifesaver"),
                new CityPoi("bondi_sign", CityPoi.Kind.SIGN, 146, 3, 194, "BONDI"),
                new CityPoi("sea_cave", CityPoi.Kind.SECRET, 220, -6, 260, "chests,lore,sailor"),
                new CityPoi("cave_sign", CityPoi.Kind.SIGN, 184, 3, 304, "SEA CAVE|KEEP OUT"),
                new CityPoi("botanic_garden", CityPoi.Kind.LANDMARK, 0, 0, 60,
                        "avenue,fountain,museum"),
                new CityPoi("botanic_fountain", CityPoi.Kind.LANDMARK, 40, 0, 60, "fountain"),
                new CityPoi("museum", CityPoi.Kind.LANDMARK, 70, 0, -40, "museum,curator"),
                new CityPoi("curator", CityPoi.Kind.NPC, 70, 0, -23, "curator"),
                new CityPoi("botanic_sign", CityPoi.Kind.SIGN, 10, 3, 132, "BOTANIC GARDEN"),
                new CityPoi("cliff_warning", CityPoi.Kind.SIGN, 187, 3, 260,
                        "CLIFF EDGE|KEEP BACK"),
                new CityPoi("macquarie_point", CityPoi.Kind.LANDMARK, -60, 0, -300,
                        "beacon,harbour_view"));
    }

    private static void buildCity(StructureBuilder b, int cx, int cz, int ground,
                                  @Nullable CitySurface surface, Palette p) {
        streets(b, cx, cz, ground, surface, p);
        harbour(b, cx, cz, ground, p);
        arrivalGate(b, cx, cz, ground, surface, p);
        operaHouse(b, cx, cz, ground, p);
        theRocks(b, cx, cz, ground, surface, p);
        railYards(b, cx, cz, ground, p);
        botanicGarden(b, cx, cz, ground, p);
        bondi(b, cx, cz, ground, surface, p);
        seaCave(b, cx, cz, ground, surface, p);
        northShore(b, cx, cz, ground, surface, p);
    }

    // ------------------------------------------------------------------ shared helpers

    /** The authored surface Y at (x,z): {@code ground} in the heart, graded on the rim. */
    private static int at(@Nullable CitySurface surface, int x, int z, int ground) {
        return surface == null ? ground : surface.at(x, z);
    }

    /** A street along a centre line, one paving course on the authored surface. */
    private static void path(StructureBuilder b, int x0, int z0, int x1, int z1, int width,
                             int ground, @Nullable CitySurface surface, BlockState state) {
        int w = Math.max(1, width);
        int lo = -(w - 1) / 2;
        int hi = w / 2;
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        if (bx - ax >= bz - az) {
            int zc = (z0 + z1) / 2;
            for (int x = ax; x <= bx; x++) {
                for (int z = zc + lo; z <= zc + hi; z++) {
                    b.put(x, at(surface, x, z, ground), z, state);
                }
            }
        } else {
            int xc = (x0 + x1) / 2;
            for (int z = az; z <= bz; z++) {
                for (int x = xc + lo; x <= xc + hi; x++) {
                    b.put(x, at(surface, x, z, ground), z, state);
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
        b.put(x, y, z, AIR);
        b.put(x, y + 1, z, AIR);
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

    private static void bed(StructureBuilder b, int x, int y, int z, Palette p) {
        b.fill(x, y, z, x + 1, y, z + 2, p.roofSlab());
        b.fill(x, y + 1, z, x + 1, y + 1, z + 1, WHITE_WOOL);
        b.put(x, y + 1, z + 2, RED_WOOL);
    }

    private static void table(StructureBuilder b, int x, int y, int z, Palette p) {
        b.put(x, y, z, p.accent());
        b.fill(x - 1, y + 1, z - 1, x + 1, y + 1, z + 1, p.roofSlab());
        b.put(x - 1, y + 1, z - 1, p.accent());
        b.put(x + 1, y + 1, z + 1, p.accent());
    }

    private static void crate(StructureBuilder b, int x, int z, int y, Palette p, int height) {
        for (int i = 1; i <= height; i++) {
            b.put(x, y + i, z, p.foundation());
        }
    }

    private static void barrel(StructureBuilder b, int x, int z, int y) {
        b.put(x, y + 1, z, BARREL);
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

    private static void hedge(StructureBuilder b, int x0, int z0, int x1, int z1, int y) {
        b.fill(x0, y, z0, x1, y + 1, z0, OAK_LEAF);
        b.fill(x0, y, z1, x1, y + 1, z1, OAK_LEAF);
        b.fill(x0, y, z0, x0, y + 1, z1, OAK_LEAF);
        b.fill(x1, y, z0, x1, y + 1, z1, OAK_LEAF);
    }

    private static void lawn(StructureBuilder b, int x0, int z0, int x1, int z1, int y) {
        b.fill(x0, y, z0, x1, y, z1, GRASS);
    }

    private static void parterre(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                                 Palette p) {
        b.fill(x0, y, z0, x1, y, z1, p.weathered());
        b.fill(x0 + 1, y, z0 + 1, x1 - 1, y, z1 - 1, DIRT);
        for (int x = x0 + 3; x <= x1 - 3; x += 4) {
            for (int z = z0 + 3; z <= z1 - 3; z += 4) {
                b.put(x, y + 1, z, flower(x + z));
            }
        }
    }

    private static void fountain(StructureBuilder b, int x, int z, int y, Palette p) {
        b.fill(x - 7, y, z - 7, x + 7, y, z + 7, p.foundation());
        b.fill(x - 6, y, z - 6, x + 6, y, z + 6, p.ground());
        b.fill(x - 5, y, z - 5, x + 5, y, z + 5, WATER);
        b.fill(x - 7, y + 1, z - 7, x + 7, y + 1, z - 7, p.foundation());
        b.fill(x - 7, y + 1, z + 7, x + 7, y + 1, z + 7, p.foundation());
        b.fill(x - 7, y + 1, z - 7, x - 7, y + 1, z + 7, p.foundation());
        b.fill(x + 7, y + 1, z - 7, x + 7, y + 1, z + 7, p.foundation());
        b.fill(x, y + 1, z, x, y + 4, z, p.accent());
        b.put(x, y + 5, z, p.light());
        b.fill(x - 1, y + 2, z, x + 1, y + 2, z, p.foundation());
        b.fill(x, y + 2, z - 1, x, y + 2, z + 1, p.foundation());
    }

    private static void well(StructureBuilder b, int x, int z, int y, Palette p) {
        b.fill(x - 2, y, z - 2, x + 2, y, z + 2, p.foundation());
        b.fill(x - 1, y, z - 1, x + 1, y, z + 1, WATER);
        b.fill(x - 2, y + 1, z - 2, x + 2, y + 1, z - 2, p.foundation());
        b.fill(x - 2, y + 1, z + 2, x + 2, y + 1, z + 2, p.foundation());
        b.fill(x - 2, y + 1, z - 2, x - 2, y + 1, z + 2, p.foundation());
        b.fill(x + 2, y + 1, z - 2, x + 2, y + 1, z + 2, p.foundation());
        b.fill(x - 2, y + 2, z - 2, x - 2, y + 3, z - 2, p.accent());
        b.fill(x + 2, y + 2, z - 2, x + 2, y + 3, z - 2, p.accent());
        b.fill(x - 2, y + 4, z - 2, x + 2, y + 4, z + 2, p.roofSlab());
        b.put(x, y + 3, z, p.light());
    }

    private static void stall(StructureBuilder b, int x, int z, int y, Palette p,
                              BlockState canopy) {
        for (int dx = 0; dx <= 3; dx += 3) {
            for (int dz = 0; dz <= 2; dz += 2) {
                b.put(x + dx, y + 1, z + dz, p.accent());
                b.put(x + dx, y + 2, z + dz, p.accent());
            }
        }
        b.fill(x, y + 3, z, x + 3, y + 3, z + 2, canopy);
        b.fill(x + 1, y + 1, z + 1, x + 2, y + 1, z + 1, p.foundation());
        b.put(x + 1, y + 2, z + 1, p.light());
    }

    private static void ferry(StructureBuilder b, int x, int z, int y, Palette p) {
        int hx = 4;
        int hz = 9;
        b.fill(x - hx, y - 2, z - hz, x + hx, y - 2, z + hz, p.accent());
        b.fill(x - hx, y - 1, z - hz, x + hx, y - 1, z - hz, p.accent());
        b.fill(x - hx, y - 1, z + hz, x + hx, y - 1, z + hz, p.accent());
        b.fill(x - hx, y - 1, z - hz, x - hx, y - 1, z + hz, p.accent());
        b.fill(x + hx, y - 1, z - hz, x + hx, y - 1, z + hz, p.accent());
        b.fill(x - hx + 1, y - 1, z - hz + 1, x + hx - 1, y - 1, z + hz - 1,
                p.roofSlab());
        b.fill(x - 2, y, z - 3, x + 2, y + 2, z + 3, WHITE_WOOL);
        b.fill(x - 1, y, z - 2, x + 1, y + 1, z + 2, AIR);
        b.put(x, y + 3, z, p.light());
        b.fill(x, y, z + 6, x, y + 7, z + 6, p.accent());
        b.put(x, y + 8, z + 6, WHITE_WOOL);
    }

    private static void steamer(StructureBuilder b, int x, int z, int y, Palette p) {
        int hx = 6;
        int hz = 16;
        b.fill(x - hx, y - 2, z - hz, x + hx, y - 2, z + hz, DARK);
        b.fill(x - hx, y - 1, z - hz, x + hx, y - 1, z - hz, DARK);
        b.fill(x - hx, y - 1, z + hz, x + hx, y - 1, z + hz, DARK);
        b.fill(x - hx, y - 1, z - hz, x - hx, y - 1, z + hz, DARK);
        b.fill(x + hx, y - 1, z - hz, x + hx, y - 1, z + hz, DARK);
        b.fill(x - hx + 1, y - 1, z - hz + 1, x + hx - 1, y - 1, z + hz - 1, p.accent());
        b.fill(x - 4, y, z - 12, x + 4, y + 4, z - 4, WHITE);
        b.fill(x - 4, y + 1, z - 11, x - 4, y + 3, z - 5, p.window());
        b.fill(x + 4, y + 1, z - 11, x + 4, y + 3, z - 5, p.window());
        b.fill(x - 3, y + 1, z - 12, x + 3, y + 3, z - 12, p.window());
        b.fill(x - 1, y + 5, z - 10, x + 1, y + 9, z - 8, DARK);
        b.put(x, y + 10, z - 9, p.light());
        b.fill(x, y, z + 12, x, y + 12, z + 12, p.accent());
        for (int i = 0; i < 4; i++) {
            b.fill(x - 5, y, z - 2 + i * 4, x + 5, y + 1, z + 1 + i * 4, p.foundation());
        }
    }

    private static void crane(StructureBuilder b, int x, int z, int y, Palette p) {
        b.fill(x - 1, y + 1, z - 1, x + 1, y + 1, z + 1, p.foundation());
        b.fill(x, y + 2, z, x, y + 12, z, p.accent());
        b.fill(x, y + 11, z, x + 10, y + 11, z, p.accent());
        b.fill(x + 10, y + 6, z, x + 10, y + 10, z, p.rail());
        b.put(x + 10, y + 5, z, p.foundation());
        b.put(x, y + 13, z, p.light());
    }

    private static void wagon(StructureBuilder b, int x, int z, int y, Palette p, boolean coal) {
        b.fill(x - 3, y + 1, z - 1, x + 3, y + 1, z + 1, p.accent());
        b.fill(x - 3, y + 2, z - 1, x - 3, y + 2, z + 1, p.accent());
        b.fill(x + 3, y + 2, z - 1, x + 3, y + 2, z + 1, p.accent());
        b.fill(x - 3, y + 3, z - 1, x + 3, y + 3, z + 1, p.roofSlab());
        if (coal) {
            b.fill(x - 2, y + 2, z, x + 2, y + 2, z, COAL);
            b.put(x, y + 3, z, COAL);
        }
    }

    private static void railLine(StructureBuilder b, int x0, int x1, int z, int y, Palette p) {
        b.fill(x0, y, z - 2, x1, y, z + 2, p.rubble());
        for (int x = x0; x <= x1; x++) {
            if (Math.floorMod(x, 4) == 0) {
                b.put(x, y, z, p.foundation());
            }
            b.put(x, y + 1, z, RAIL_EW);
        }
    }

    private static void eucalyptus(StructureBuilder b, int x, int z, int y) {
        int h = 9 + Math.floorMod(x * 3 + z * 5, 4);
        for (int i = 1; i <= h; i++) {
            b.put(x, y + i, z, OAK_LOG);
        }
        for (int dy = -2; dy <= 2; dy++) {
            int r = dy == 0 ? 3 : 3 - Math.abs(dy);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) <= r + 1
                            && Math.floorMod(x + z + dx + dz, 2) == 0) {
                        b.put(x + dx, y + h + dy, z + dz, OAK_LEAF);
                    }
                }
            }
        }
        b.put(x, y + h + 1, z, OAK_LEAF);
    }

    private static void pine(StructureBuilder b, int x, int z, int y) {
        int h = 12 + Math.floorMod(x + z, 4);
        for (int i = 1; i <= h; i++) {
            b.put(x, y + i, z, PINE_LOG);
        }
        for (int i = 3; i < h; i += 2) {
            int r = Math.max(1, (h - i) / 3);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) <= r) {
                        b.put(x + dx, y + i, z + dz, PINE_LEAF);
                    }
                }
            }
        }
        b.put(x, y + h + 1, z, PINE_LEAF);
    }

    private static void fig(StructureBuilder b, int x, int z, int y) {
        for (int i = 1; i <= 5; i++) {
            b.put(x, y + i, z, OAK_LOG);
        }
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                int d = Math.abs(dx) + Math.abs(dz);
                if (d <= 5) {
                    b.put(x + dx, y + 6, z + dz, OAK_LEAF);
                }
                if (d <= 3) {
                    b.put(x + dx, y + 7, z + dz, OAK_LEAF);
                }
            }
        }
        b.fill(x - 2, y + 5, z - 2, x + 2, y + 5, z + 2, OAK_LEAF);
    }

    private static void palm(StructureBuilder b, int x, int z, int y) {
        int h = 7 + Math.floorMod(x + z, 3);
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

    private static void umbrella(StructureBuilder b, int x, int z, int y, BlockState canopy,
                                 Palette p) {
        b.put(x, y + 1, z, p.accent());
        b.put(x, y + 2, z, p.accent());
        b.fill(x - 2, y + 3, z - 2, x + 2, y + 3, z + 2, canopy);
        b.put(x - 2, y + 3, z - 2, WHITE_WOOL);
        b.put(x + 2, y + 3, z + 2, WHITE_WOOL);
    }

    private static void shed(StructureBuilder b, int x0, int z0, int y, Palette p) {
        int x1 = x0 + 5;
        int z1 = z0 + 4;
        b.fill(x0, y, z0, x1, y, z1, p.foundation());
        b.walls(x0, y + 1, z0, x1, y + 3, z1, p.weathered());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 3, z1 - 1, AIR);
        b.fill(x0 - 1, y + 4, z0 - 1, x1 + 1, y + 4, z1 + 1, p.roofSlab());
        door(b, p, x0 + 2, y + 1, z1, Direction.SOUTH);
        b.put(x0 + 1, y + 2, z0 + 1, BARREL);
    }

    private static void caveShell(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  int floorY, int height) {
        b.fill(x0 - 1, floorY - 1, z0 - 1, x1 + 1, floorY + height + 1, z1 + 1, SANDSTONE);
        b.fill(x0, floorY, z0, x1, floorY + height, z1, AIR);
    }

    private static void clockFace(StructureBuilder b, int x, int y, int z, Direction face) {
        boolean alongZ = face.getAxis() == Direction.Axis.Z;
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                int px = alongZ ? x + i : x;
                int pz = alongZ ? z : z + i;
                boolean rim = Math.abs(i) == 2 || Math.abs(j) == 2;
                boolean hand = i == 0 || j == 0;
                b.put(px, y + j, pz, rim || hand ? DARK : WHITE);
            }
        }
    }

    // ------------------------------------------------------------------ streets

    private static void streets(StructureBuilder b, int cx, int cz, int ground,
                                @Nullable CitySurface surface, Palette p) {
        path(b, cx, cz + 336, cx, cz - 136, 14, ground, surface, p.ground());
        path(b, cx, cz - 136, cx, cz - 152, 14, ground, surface, p.ground());
        path(b, cx - 170, cz - 146, cx + 170, cz - 146, 12, ground, surface, p.ground());
        path(b, cx - 320, cz, cx - 7, cz, 8, ground, surface, p.ground());
        path(b, cx - 310, cz - 230, cx - 310, cz + 150, 5, ground, surface, p.ground());
        path(b, cx - 260, cz - 230, cx - 260, cz + 150, 5, ground, surface, p.ground());
        path(b, cx - 210, cz - 230, cx - 210, cz + 150, 5, ground, surface, p.ground());
        path(b, cx - 160, cz - 230, cx - 160, cz + 150, 5, ground, surface, p.ground());
        int[] lanes = {-230, -190, -150, -110, -70, 70, 110, 150};
        for (int z : lanes) {
            path(b, cx - 320, cz + z, cx - 140, cz + z, 5, ground, surface, p.ground());
        }
        path(b, cx + 7, cz + 140, cx + 320, cz + 140, 10, ground, surface, p.ground());
        path(b, cx + 140, cz - 120, cx + 140, cz + 320, 8, ground, surface, p.ground());
        path(b, cx + 140, cz + 200, cx + 320, cz + 200, 8, ground, surface, p.ground());
        path(b, cx + 140, cz + 20, cx + 320, cz + 20, 10, ground, surface, p.ground());
        path(b, cx + 160, cz - 40, cx + 160, cz + 20, 8, ground, surface, p.ground());
        path(b, cx + 144, cz - 120, cx + 204, cz - 120, 6, ground, surface, p.ground());
        path(b, cx - 110, cz + 60, cx + 110, cz + 60, 6, ground, surface, p.ground());
        path(b, cx - 110, cz + 100, cx + 110, cz + 100, 6, ground, surface, p.ground());
        for (int z = -136; z <= 336; z++) {
            b.put(cx - 8, at(surface, cx - 8, cz + z, ground), cz + z, p.foundation());
            b.put(cx + 8, at(surface, cx + 8, cz + z, ground), cz + z, p.foundation());
        }
    }

    // ------------------------------------------------------------------ arrival gate

    private static void arrivalGate(StructureBuilder b, int cx, int cz, int ground,
                                    @Nullable CitySurface surface, Palette p) {
        int gy = at(surface, cx, cz + 320, ground);
        gateTower(b, cx - 30, cz + 308, cx - 16, cz + 324, gy, p);
        gateTower(b, cx + 16, cz + 308, cx + 30, cz + 324, gy, p);
        b.fill(cx - 16, gy + 7, cz + 310, cx + 16, gy + 7, cz + 322, p.wall());
        b.fill(cx - 16, gy + 8, cz + 310, cx + 16, gy + 12, cz + 322, p.wall());
        b.fill(cx - 16, gy + 13, cz + 310, cx + 16, gy + 13, cz + 322, p.roofSlab());
        b.crenellations(cx - 16, cz + 310, cx + 16, cz + 322, gy + 14);
        b.fill(cx - 7, gy + 1, cz + 310, cx + 7, gy + 6, cz + 322, AIR);
        b.fill(cx - 30, gy - 3, cz + 308, cx + 30, gy - 1, cz + 324, p.foundation());
        b.fill(cx - 2, gy + 8, cz + 322, cx + 2, gy + 8, cz + 322, p.foundation());
        sign(b, p, cx, gy + 9, cz + 322, Direction.SOUTH, "SYDNEY");
        b.put(cx - 15, gy + 1, cz + 326, p.light());
        b.put(cx + 15, gy + 1, cz + 326, p.light());
        String[][] dirs = {{"OPERA HOUSE"}, {"THE ROCKS"}, {"RAIL YARDS"}, {"BONDI"},
                {"BOTANIC GARDEN"}};
        int[] posts = {-26, -18, -10, 10, 18};
        for (int i = 0; i < posts.length; i++) {
            signPost(b, cx + posts[i], cz + 334, gy, p, Direction.SOUTH, dirs[i]);
        }
        b.fill(cx + 34, gy + 1, cz + 326, cx + 56, gy + 3, cz + 328, p.foundation());
        b.fill(cx + 33, gy + 4, cz + 325, cx + 57, gy + 4, cz + 329, p.roofSlab());
        sign(b, p, cx + 38, gy + 3, cz + 328, Direction.SOUTH, "CITY NOTICES");
        sign(b, p, cx + 44, gy + 3, cz + 328, Direction.SOUTH, "CURFEW AT DUSK");
        sign(b, p, cx + 50, gy + 3, cz + 328, Direction.SOUTH, "NO WAGONS");
    }

    private static void gateTower(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                                  Palette p) {
        b.fill(x0, y, z0, x1, y, z1, p.foundation());
        b.walls(x0, y + 1, z0, x1, y + 11, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 10, z1 - 1, AIR);
        b.fill(x0, y + 12, z0, x1, y + 12, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y + 12, 4, 0);
        quoins(b, x0, z0, x1, z1, y + 1, y + 11, p);
        b.window(x0 + 4, y + 4, z1, 3, 2, true);
        b.window(x1 - 4, y + 4, z1, 3, 2, true);
        b.window(x0, y + 6, z0 + 6, 3, 1, true);
        b.put(x0 + 2, y + 1, z1 + 2, p.light());
    }

    // ------------------------------------------------------------------ harbour and quay

    private static void harbour(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int g = ground;
        b.fill(cx - 140, g - 3, cz - 262, cx + 160, g - 3, cz - 216, SAND);
        b.fill(cx - 140, g - 2, cz - 262, cx + 160, g - 1, cz - 216, WATER);
        b.fill(cx + 160, g - 3, cz - 220, cx + 250, g - 3, cz - 140, SAND);
        b.fill(cx + 160, g - 2, cz - 220, cx + 250, g - 1, cz - 140, WATER);
        b.fill(cx - 141, g - 5, cz - 263, cx + 161, g, cz - 263, p.foundation());
        b.fill(cx + 161, g - 5, cz - 221, cx + 251, g, cz - 221, p.foundation());
        b.fill(cx + 251, g - 5, cz - 221, cx + 251, g, cz - 139, p.foundation());
        b.fill(cx + 159, g - 5, cz - 139, cx + 251, g, cz - 139, p.foundation());
        b.fill(cx + 159, g - 5, cz - 215, cx + 159, g, cz - 139, p.foundation());
        b.fill(cx - 141, g - 5, cz - 215, cx + 159, g, cz - 215, p.foundation());
        b.fill(cx - 141, g - 5, cz - 263, cx - 141, g, cz - 215, p.foundation());
        b.fill(cx + 161, g - 5, cz - 263, cx + 161, g, cz - 221, p.foundation());

        pave(b, cx - 80, cz - 136, cx + 80, cz - 88, g, p.ground());
        outline(b, cx - 80, cz - 136, cx + 80, cz - 88, g, p.foundation());
        for (int x = -70; x <= 70; x += 20) {
            lanternPost(b, cx + x, cz - 92, g, p);
        }
        b.monument(cx + 54, cz - 110, g + 1);
        crate(b, cx - 60, cz - 100, g, p, 1);
        crate(b, cx - 57, cz - 100, g, p, 2);
        barrel(b, cx + 60, cz - 96, g);
        fig(b, cx - 70, cz - 120, g);
        questBoard(b, cx, cz, g, p);

        pave(b, cx - 140, cz - 176, cx + 140, cz - 150, g, p.ground());
        outline(b, cx - 140, cz - 176, cx + 140, cz - 150, g, p.foundation());
        for (int x = -130; x <= 130; x += 20) {
            lanternPost(b, cx + x, cz - 152, g, p);
        }
        fig(b, cx - 120, cz - 170, g);
        fig(b, cx + 120, cz - 170, g);
        signPost(b, cx, cz - 150, g, p, Direction.SOUTH, "OPERA HOUSE");

        harbourBoard(b, cx, cz, g, p);
        steamer(b, cx + 110, cz - 240, g, p);
        crane(b, cx + 120, cz - 214, g, p);
        for (int i = 0; i < 5; i++) {
            crate(b, cx + 70 + i * 6, cz - 212, g, p, 1 + (i & 1));
        }
        for (int z = -212; z <= -190; z += 6) {
            b.put(cx - 141, g + 1, cz + z, p.accent());
            b.put(cx - 141, g + 2, cz + z, p.rail());
        }
        ferryOffice(b, cx, cz, g, p);
        jetty(b, cx, cz, g, p);
        crane(b, cx + 245, cz - 138, g, p);
        for (int i = 0; i < 4; i++) {
            crate(b, cx + 236, cz - 130 + i * 4, g, p, 1);
        }
        coveWarehouse(b, cx, cz, g, p);
    }

    private static void questBoard(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int z = cz - 128;
        b.fill(cx - 8, ground + 1, z, cx - 8, ground + 4, z, p.accent());
        b.fill(cx + 8, ground + 1, z, cx + 8, ground + 4, z, p.accent());
        b.fill(cx - 9, ground + 5, z - 1, cx + 9, ground + 5, z + 1, p.roofSlab());
        b.fill(cx - 7, ground + 2, z - 1, cx + 7, ground + 4, z - 1, p.foundation());
        sign(b, p, cx - 5, ground + 3, z, Direction.SOUTH, "QUEST NOTICES");
        sign(b, p, cx, ground + 3, z, Direction.SOUTH, "MISSING SAILOR");
        sign(b, p, cx + 5, ground + 3, z, Direction.SOUTH, "SMUGGLED CARGO");
        b.put(cx - 7, ground + 4, z, p.light());
        b.put(cx + 7, ground + 4, z, p.light());
    }

    private static void harbourBoard(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 155;
        int x1 = cx - 143;
        int z0 = cz - 218;
        int z1 = cz - 200;
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 6, z1, p.wall());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 5, z1 - 1, AIR);
        b.fill(x0, ground + 6, z0, x1, ground + 6, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 6, 3, 1);
        door(b, p, x1, ground + 1, cz - 209, Direction.EAST);
        sign(b, p, x1, ground + 4, cz - 209, Direction.EAST, "HARBOUR", "BOARD");
        b.put(x0 + 2, ground + 1, z1 - 2, p.light());
    }

    private static void ferryOffice(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 208;
        int x1 = cx + 232;
        int z0 = cz - 132;
        int z1 = cz - 108;
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 7, z1, p.wall());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 6, z1 - 1, AIR);
        b.fill(x0, ground + 7, z0, x1, ground + 7, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 7, 4, 1);
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 6, p);
        door(b, p, cx + 220, ground + 1, z0, Direction.NORTH);
        door(b, p, x0, ground + 1, cz - 120, Direction.WEST);
        b.window(cx + 214, ground + 3, z0, 2, 1, true);
        b.window(cx + 226, ground + 3, z0, 2, 1, true);
        b.fill(cx + 212, ground + 1, z1 - 6, cx + 228, ground + 1, z1 - 5, p.accent());
        b.fill(cx + 212, ground + 2, z1 - 6, cx + 228, ground + 2, z1 - 6, p.foundation());
        barrel(b, x0 + 2, z1 - 2, ground);
        crate(b, x0 + 4, z1 - 2, ground, p, 1);
        sign(b, p, cx + 220, ground + 5, z0, Direction.NORTH, "FERRY");
        sign(b, p, cx + 220, ground + 3, z1, Direction.SOUTH, "MISSING SAILOR");
        sign(b, p, cx + 214, ground + 3, z1, Direction.SOUTH, "LAST SEEN ROCKS");
        signPost(b, cx + 232, cz - 136, ground, p, Direction.SOUTH, "FERRY", "WOOLOOMOOLOO");
    }

    private static void jetty(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 210;
        int x1 = cx + 226;
        for (int z = -140; z >= -180; z--) {
            b.fill(x0, ground, cz + z, x1, ground, cz + z, p.roofSlab());
            if (Math.floorMod(z, 6) == 0) {
                b.fill(x0, ground - 4, cz + z, x0, ground - 1, cz + z, p.accent());
                b.fill(x1, ground - 4, cz + z, x1, ground - 1, cz + z, p.accent());
            }
        }
        b.fill(x0, ground + 1, cz - 140, x0, ground + 2, cz - 140, p.accent());
        b.fill(x1, ground + 1, cz - 140, x1, ground + 2, cz - 140, p.accent());
        signPost(b, x0 + 8, cz - 140, ground, p, Direction.SOUTH, "FERRY");
        ferry(b, cx + 218, cz - 190, ground, p);
    }

    private static void coveWarehouse(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 260;
        int x1 = cx + 300;
        int z0 = cz - 130;
        int z1 = cz - 100;
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 9, z1, p.weathered());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 8, z1 - 1, AIR);
        b.fill(x0, ground + 9, z0, x1, ground + 9, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 9, 4, 0);
        door(b, p, cx + 280, ground + 1, z1, Direction.SOUTH);
        sign(b, p, cx + 280, ground + 6, z1, Direction.SOUTH, "BONDED", "CARGO");
        crate(b, x0 + 4, z0 + 4, ground, p, 2);
        crate(b, x1 - 4, z0 + 4, ground, p, 1);
        b.chest(cx + 292, ground + 1, z0 + 4, SiteLoot.CULT);
    }

    // ------------------------------------------------------------------ Opera House

    private static void operaHouse(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int g = ground;
        b.fill(cx - 55, g - 6, cz - 214, cx + 55, g - 1, cz - 176, p.foundation());
        b.fill(cx - 55, g, cz - 214, cx + 55, g, cz - 176, p.foundation());
        b.fill(cx - 55, g + 1, cz - 214, cx + 55, g + 1, cz - 176, p.ground());
        b.fill(cx - 50, g + 2, cz - 210, cx + 50, g + 2, cz - 180, p.foundation());
        b.fill(cx - 46, g + 3, cz - 206, cx + 46, g + 3, cz - 184, p.ground());
        b.fill(cx - 55, g + 4, cz - 214, cx - 47, g + 4, cz - 214, p.foundation());
        b.fill(cx - 29, g + 4, cz - 214, cx + 55, g + 4, cz - 214, p.foundation());
        b.fill(cx - 46, g + 2, cz - 215, cx - 30, g + 2, cz - 215, p.foundation());
        b.fill(cx - 46, g + 1, cz - 216, cx - 30, g + 1, cz - 216, p.foundation());
        for (int i = 0; i < 3; i++) {
            b.fill(cx - 40 + i * 4, g + 1 + i, cz - 184, cx + 40 - i * 4, g + 1 + i, cz - 184,
                    p.foundation());
        }
        b.fill(cx - 14, g + 4, cz - 188, cx + 14, g + 14, cz - 178, GLASS);
        b.fill(cx - 13, g + 4, cz - 187, cx + 13, g + 13, cz - 179, AIR);
        for (int x = -14; x <= 14; x += 7) {
            b.fill(cx + x, g + 4, cz - 188, cx + x, g + 14, cz - 188, p.accent());
        }
        door(b, p, cx, g + 4, cz - 178, Direction.SOUTH);
        b.fill(cx - 2, g + 5, cz - 176, cx + 2, g + 5, cz - 176, p.foundation());
        sign(b, p, cx, g + 6, cz - 176, Direction.SOUTH, "OPERA HOUSE");
        sail(b, cx - 34, cz - 188, g + 3, 12, 46, 10, WHITE, LIGHT);
        sail(b, cx, cz - 188, g + 3, 16, 64, 14, WHITE, LIGHT);
        sail(b, cx + 34, cz - 188, g + 3, 11, 40, 8, WHITE, LIGHT);
        b.fill(cx - 30, g + 6, cz - 214, cx - 26, g + 9, cz - 214, p.accent());
        b.fill(cx + 26, g + 6, cz - 214, cx + 30, g + 9, cz - 214, p.accent());
        b.put(cx - 28, g + 10, cz - 214, p.light());
        b.put(cx + 28, g + 10, cz - 214, p.light());
        wharf(b, cx, cz, g, p);
    }

    private static void sail(StructureBuilder b, int x, int z, int y, int halfW, int height,
                             int lean, BlockState shell, BlockState shade) {
        for (int dx = -halfW; dx <= halfW; dx++) {
            double u = dx / (double) halfW;
            int h = (int) Math.round(height * (1.0 - u * u));
            if (h < 1) {
                h = 1;
            }
            for (int i = 0; i <= h; i++) {
                int zz = z + (int) Math.round((double) i * lean / height);
                b.put(x + dx, y + i, zz, shell);
                b.put(x + dx, y + i, zz - 1, i == 0 ? shade : shell);
                if (Math.floorMod(dx, 4) == 0) {
                    b.put(x + dx, y + i, zz + 1, shade);
                }
            }
        }
        b.fill(x - halfW - 1, y - 1, z - 2, x + halfW + 1, y - 1, z + 3, shade);
    }

    private static void wharf(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 46;
        int x1 = cx - 30;
        for (int z = -216; z >= -246; z--) {
            b.fill(x0, ground + 1, cz + z, x1, ground + 1, cz + z, p.roofSlab());
            if (Math.floorMod(z, 6) == 0) {
                b.fill(x0, ground - 4, cz + z, x0, ground, cz + z, p.accent());
                b.fill(x1, ground - 4, cz + z, x1, ground, cz + z, p.accent());
            }
        }
        b.fill(x0, ground + 2, cz - 216, x0, ground + 3, cz - 216, p.accent());
        b.fill(x1, ground + 2, cz - 216, x1, ground + 3, cz - 216, p.accent());
        b.put(x0, ground + 4, cz - 216, p.light());
        b.put(x1, ground + 4, cz - 216, p.light());
        signPost(b, cx - 38, cz - 216, ground + 1, p, Direction.NORTH, "FERRY");
        ferry(b, cx - 38, cz - 252, ground, p);
    }

    // ------------------------------------------------------------------ The Rocks

    private static void theRocks(StructureBuilder b, int cx, int cz, int ground,
                                 @Nullable CitySurface surface, Palette p) {
        int[] lanes = {-230, -150, -70, 70, 110, 150};
        int[] blocks = {-305, -255, -205};
        for (int li = 0; li < lanes.length; li++) {
            for (int bi = 0; bi < blocks.length; bi++) {
                int x = cx + blocks[bi];
                int z = cz + lanes[li];
                boolean north = !(li == 2 && bi == 0) && !(li == 3 && bi == 1);
                boolean south = !(li == 2 && bi == 0) && !(li == 2 && bi == 1)
                        && !(li == 3 && bi == 1) && !(li == 4 && bi == 2);
                if (north) {
                    terraceBlock(b, x, z - 3, -1, ground, surface, p, li * 7 + bi);
                }
                if (south) {
                    terraceBlock(b, x, z + 3, 1, ground, surface, p, li * 7 + bi + 3);
                }
            }
        }
        market(b, cx, cz, ground, p);
        pub(b, cx, cz, ground, p);
        shop(b, cx, cz, ground, p);
        apothecary(b, cx, cz, ground, p);
        watchHouse(b, cx, cz, ground, p);
        chapel(b, cx, cz, ground, p);
        timberYard(b, cx, cz, ground, p);
        ruin(b, cx, cz, ground, p);
        yard(b, cx - 300, cz - 178, cx - 272, cz - 165,
                at(surface, cx - 300, cz - 178, ground), p, 0);
        yard(b, cx - 300, cz - 134, cx - 272, cz - 116,
                at(surface, cx - 300, cz - 134, ground), p, 1);
        yard(b, cx - 250, cz - 134, cx - 222, cz - 116,
                at(surface, cx - 250, cz - 134, ground), p, 2);
        yard(b, cx - 300, cz - 20, cx - 272, cz - 8,
                at(surface, cx - 300, cz - 20, ground), p, 3);
        yard(b, cx - 250, cz - 20, cx - 222, cz - 8,
                at(surface, cx - 250, cz - 20, ground), p, 4);
        yard(b, cx - 200, cz - 20, cx - 172, cz - 8,
                at(surface, cx - 200, cz - 20, ground), p, 5);
        yard(b, cx - 300, cz + 126, cx - 272, cz + 134,
                at(surface, cx - 300, cz + 126, ground), p, 6);
        yard(b, cx - 250, cz + 126, cx - 222, cz + 134,
                at(surface, cx - 250, cz + 126, ground), p, 7);
        yard(b, cx - 200, cz + 130, cx - 172, cz + 136,
                at(surface, cx - 200, cz + 130, ground), p, 8);
        lawn(b, cx - 300, cz + 36, cx - 272, cz + 54, ground);
        parterre(b, cx - 298, cz + 38, cx - 274, cz + 52, ground, p);
        fig(b, cx - 286, cz + 45, ground);
        lawn(b, cx - 200, cz + 36, cx - 172, cz + 54, ground);
        parterre(b, cx - 198, cz + 38, cx - 174, cz + 52, ground, p);
        fig(b, cx - 186, cz + 45, ground);
        for (int z = -230; z <= 150; z++) {
            boolean gateGap = z >= -4 && z <= 4;
            if (Math.floorMod(z, 12) < 3 || gateGap) {
                continue;
            }
            int wy = at(surface, cx - 320, cz + z, ground);
            b.put(cx - 320, wy + 1, cz + z, p.weathered());
            b.put(cx - 320, wy + 2, cz + z, p.weathered());
        }
        signPost(b, cx - 136, cz + 6, ground, p, Direction.SOUTH, "THE ROCKS");
        signPost(b, cx - 268, cz - 72, ground, p, Direction.SOUTH, "ROCKS MARKET");
        signPost(b, cx - 226, cz - 50, ground, p, Direction.SOUTH, "PUB", "BEDS");
        signPost(b, cx - 226, cz + 70, ground, p, Direction.SOUTH, "SHOP", "DRY GOODS");
        signPost(b, cx - 178, cz + 104, ground, p, Direction.SOUTH, "APOTHECARY");
    }

    private static void terraceBlock(StructureBuilder b, int x0, int zFront, int dir, int ground,
                                     @Nullable CitySurface surface, Palette p, int v) {
        for (int i = 0; i < 2; i++) {
            int x = x0 + i * 8;
            int y = at(surface, x + 4, zFront, ground);
            terrace(b, x, zFront, dir, y, p, v + i);
        }
    }

    private static void terrace(StructureBuilder b, int x0, int zFront, int dir, int y, Palette p,
                                int v) {
        int z0 = dir > 0 ? zFront : zFront - 10;
        int z1 = dir > 0 ? zFront + 10 : zFront;
        int x1 = x0 + 7;
        int h = 6 + Math.floorMod(v, 3);
        b.fill(x0, y, z0, x1, y, z1, p.foundation());
        b.fill(x0, y + h, z0, x1, y + h, z1, p.roof());
        b.walls(x0, y + 1, z0, x1, y + h - 1, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + h - 1, z1 - 1, AIR);
        b.fill(x0, y - 6, z0, x1, y - 1, z0, p.foundation());
        b.fill(x0, y - 6, z1, x1, y - 1, z1, p.foundation());
        b.fill(x0, y - 6, z0, x0, y - 1, z1, p.foundation());
        b.fill(x1, y - 6, z0, x1, y - 1, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, y + 1, y + h - 1, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y + h, 3, 0);
        int face = dir > 0 ? z0 : z1;
        Direction facing = dir > 0 ? Direction.NORTH : Direction.SOUTH;
        int doorX = x0 + 2 + Math.floorMod(v, 3) * 2;
        door(b, p, doorX, y + 1, face, facing);
        b.window(x0 + 1, y + 2, face, 1, 1, false);
        b.window(x0 + 2, y + 4, face, 2, 1, true);
        int bal = dir > 0 ? face - 1 : face + 1;
        b.fill(x0, y + 3, bal, x1, y + 3, bal, p.roofSlab());
        b.fill(x0, y + 4, bal, x1, y + 4, bal, p.rail());
        b.put(x0, y + 3, bal, p.accent());
        b.put(x1, y + 3, bal, p.accent());
        b.put(x0 + 2, y + h + 1, z0 + 2, p.accent());
        b.put(x0 + 2, y + h + 2, z0 + 2, p.accent());
        if (Math.floorMod(v, 5) == 4) {
            b.put(x0 + 1, y + 2, face, p.frame());
            b.put(x0 + 1, y + 3, face, p.frame());
            sign(b, p, x0 + 4, y + 5, face, facing, "TO LET");
        }
        if (Math.floorMod(v, 7) == 6) {
            b.fill(x0 + 5, y + h, z0 + 7, x1, y + h + 2, z1, AIR);
            b.fill(x0 + 5, y + 1, z0 + 7, x1, y + 3, z1, AIR);
            b.fill(x0 + 5, y, z0 + 7, x1, y, z1, p.rubble());
        }
    }

    private static void market(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 305;
        int x1 = cx - 265;
        int z0 = cz - 105;
        int z1 = cz - 75;
        pave(b, x0, z0, x1, z1, ground, p.ground());
        outline(b, x0, z0, x1, z1, ground, p.foundation());
        int[] sx = {x0 + 4, x0 + 16, x0 + 28};
        int[] sz = {z0 + 5, z0 + 18};
        int i = 0;
        for (int x : sx) {
            for (int z : sz) {
                stall(b, x, z, ground, p, Math.floorMod(i, 2) == 0 ? WHITE_WOOL : BLUE_WOOL);
                i++;
            }
        }
        well(b, x0 + 20, z0 + 12, ground, p);
        crate(b, x0 + 2, z1 - 3, ground, p, 1);
        crate(b, x0 + 5, z1 - 3, ground, p, 2);
        barrel(b, x1 - 3, z1 - 3, ground);
        signPost(b, x1 - 4, z1 + 2, ground, p, Direction.SOUTH, "ROCKS MARKET");
        b.put(x0 + 2, ground + 1, z1 - 6, p.light());
        b.put(x1 - 2, ground + 1, z1 - 6, p.light());
    }

    private static void pub(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 252;
        int x1 = cx - 228;
        int z0 = cz - 67;
        int z1 = cz - 56;
        int top = ground + 9;
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, top - 1, z1, p.wall());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, top - 1, z1 - 1, AIR);
        b.fill(x0, top, z0, x1, top, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, top, 4, 0);
        quoins(b, x0, z0, x1, z1, ground + 1, top - 1, p);
        door(b, p, x0 + 6, ground + 1, z0, Direction.NORTH);
        b.window(x0 + 3, ground + 2, z0, 2, 1, true);
        b.window(x1 - 4, ground + 2, z0, 2, 1, true);
        b.fill(x0 + 1, ground + 5, z0 + 1, x1 - 1, ground + 5, z1 - 1, p.roofSlab());
        b.fill(x0 + 2, ground + 5, z0 + 2, x0 + 5, ground + 5, z0 + 5, AIR);
        for (int i = 0; i < 4; i++) {
            stairs(b, p.roofStairs(), x0 + 2 + i, ground + 1 + i, z0 + 2 + i, Direction.EAST);
        }
        b.fill(x0 + 2, ground + 1, z1 - 2, x1 - 2, ground + 1, z1 - 2, p.accent());
        b.fill(x0 + 2, ground + 2, z1 - 2, x1 - 2, ground + 2, z1 - 2, p.foundation());
        barrel(b, x0 + 2, z1 - 1, ground);
        barrel(b, x0 + 4, z1 - 1, ground);
        barrel(b, x1 - 3, z1 - 1, ground);
        table(b, x0 + 6, ground + 1, z1 - 5, p);
        table(b, x1 - 6, ground + 1, z1 - 5, p);
        table(b, x0 + 12, ground + 1, z0 + 5, p);
        b.put(x1 - 1, ground + 1, z0 + 1, Blocks.CAMPFIRE.defaultBlockState());
        b.fill(x1 - 1, top, z0 + 1, x1 - 1, top + 4, z0 + 1, p.accent());
        bed(b, x0 + 2, ground + 6, z1 - 4, p);
        bed(b, x1 - 4, ground + 6, z0 + 2, p);
        bed(b, x0 + 10, ground + 6, z1 - 4, p);
        sign(b, p, x0 + 6, ground + 5, z0, Direction.NORTH, "THE SAILOR'S", "REST");
        sign(b, p, x1, ground + 4, z0 + 6, Direction.EAST, "BEDS", "ALE");
        sign(b, p, x0 + 12, ground + 4, z1, Direction.SOUTH, "SAILOR SEEN", "AT THE CAVE");
        b.put(x0 + 2, ground + 1, z0 + 1, p.light());
        b.put(x1 - 2, ground + 1, z0 + 1, p.light());
    }

    private static void shop(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 252;
        int x1 = cx - 228;
        int z0 = cz + 56;
        int z1 = cz + 67;
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 7, z1, p.wall());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 6, z1 - 1, AIR);
        b.fill(x0, ground + 7, z0, x1, ground + 7, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 7, 4, 0);
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 6, p);
        door(b, p, cx - 240, ground + 1, z1, Direction.SOUTH);
        b.window(cx - 248, ground + 2, z1, 2, 1, true);
        b.window(cx - 232, ground + 2, z1, 2, 1, true);
        b.fill(x0 + 2, ground + 4, z1 + 2, x1 - 2, ground + 4, z1 + 2, WHITE_WOOL);
        b.fill(x0 + 2, ground + 1, z1 + 2, x0 + 2, ground + 3, z1 + 2, p.accent());
        b.fill(x1 - 2, ground + 1, z1 + 2, x1 - 2, ground + 3, z1 + 2, p.accent());
        b.fill(x0 + 2, ground + 1, z1 - 2, x1 - 2, ground + 1, z1 - 2, p.accent());
        b.fill(x0 + 2, ground + 2, z1 - 2, x1 - 2, ground + 2, z1 - 2, p.foundation());
        crate(b, x0 + 2, z0 + 2, ground, p, 1);
        crate(b, x0 + 2, z0 + 4, ground, p, 2);
        barrel(b, x1 - 2, z0 + 2, ground);
        b.fill(x0 + 1, ground + 3, z0 + 1, x0 + 1, ground + 5, z1 - 1, BOOKS);
        sign(b, p, cx - 240, ground + 5, z1, Direction.SOUTH, "ROCKS SHOP", "DRY GOODS");
        b.put(x0 + 2, ground + 1, z0 + 1, p.light());
    }

    private static void apothecary(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 205;
        int x1 = cx - 183;
        int z0 = cz + 116;
        int z1 = cz + 128;
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 7, z1, p.wall());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 6, z1 - 1, AIR);
        b.fill(x0, ground + 7, z0, x1, ground + 7, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 7, 3, 1);
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 6, p);
        door(b, p, cx - 200, ground + 1, z0, Direction.NORTH);
        b.window(cx - 203, ground + 2, z0, 2, 1, true);
        b.window(cx - 189, ground + 2, z0, 2, 1, true);
        b.fill(x0 + 2, ground + 1, z0 + 2, x0 + 6, ground + 1, z0 + 6, DIRT);
        b.put(x0 + 3, ground + 1, z0 + 3, CAULDRON);
        b.put(x0 + 5, ground + 1, z0 + 3, Blocks.CAMPFIRE.defaultBlockState());
        b.put(x0 + 5, ground + 2, z0 + 3, CAULDRON);
        barrel(b, x0 + 2, z1 - 2, ground);
        barrel(b, x1 - 2, z1 - 2, ground);
        b.fill(x1 - 1, ground + 1, z0 + 2, x1 - 1, ground + 4, z1 - 2, BOOKS);
        for (int z = z0 + 3; z <= z1 - 3; z += 3) {
            b.put(x0 + 1, ground + 1, z, flower(z));
        }
        sign(b, p, cx - 200, ground + 5, z0, Direction.NORTH, "APOTHECARY", "REMEDIES");
        b.fill(x0 + 2, ground + 1, z0 - 2, x1 - 2, ground + 1, z0 - 2, DIRT);
        for (int x = x0 + 3; x <= x1 - 3; x += 4) {
            b.put(x, ground + 2, z0 - 2, flower(x));
        }
        b.put(x0 + 2, ground + 1, z1 - 2, p.light());
    }

    private static void watchHouse(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 250;
        int x1 = cx - 230;
        int z0 = cz - 107;
        int z1 = cz - 93;
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 5, z1, p.wall());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 4, z1 - 1, AIR);
        b.fill(x0, ground + 5, z0, x1, ground + 5, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 5, 3, 0);
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 4, p);
        door(b, p, x0 + 3, ground + 1, z0, Direction.NORTH);
        b.fill(x0 + 2, ground + 1, z1 - 1, x0 + 4, ground + 3, z1 - 1, p.rail());
        b.fill(x0 + 1, ground + 1, z0 + 2, x0 + 2, ground + 1, z0 + 3, p.accent());
        b.put(x0 + 1, ground + 2, z0 + 2, p.light());
        sign(b, p, x0 + 3, ground + 4, z0, Direction.NORTH, "WATCH HOUSE");
        b.fill(x0 + 1, ground + 6, z0 + 1, x0 + 1, ground + 7, z0 + 1, p.accent());
        b.put(x0 + 1, ground + 8, z0 + 1, p.light());
    }

    private static void chapel(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 250;
        int x1 = cx - 226;
        int z0 = cz + 76;
        int z1 = cz + 100;
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 8, z1, p.wall());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 7, z1 - 1, AIR);
        b.fill(x0, ground + 8, z0, x1, ground + 8, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 8, 5, 1);
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 7, p);
        door(b, p, cx - 238, ground + 1, z0, Direction.NORTH);
        b.window(cx - 244, ground + 3, z0, 3, 1, true);
        b.window(cx - 232, ground + 3, z0, 3, 1, true);
        b.window(x0, ground + 3, cz + 88, 3, 1, true);
        b.window(x1, ground + 3, cz + 88, 3, 1, true);
        b.fill(x0 + 2, ground + 1, z1 - 3, x1 - 2, ground + 1, z1 - 3, p.accent());
        b.fill(x0 + 3, ground + 1, z1 - 6, x0 + 5, ground + 1, z1 - 6, p.roofSlab());
        b.fill(x0 + 6, ground + 1, z1 - 6, x0 + 8, ground + 1, z1 - 6, p.roofSlab());
        b.put(cx - 238, ground + 3, z1 - 4, p.light());
        sign(b, p, cx - 238, ground + 5, z0, Direction.NORTH, "CHAPEL");
        b.spire(cx - 238, z0 + 3, ground + 9, 5);
    }

    private static void timberYard(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 300;
        int x1 = cx - 270;
        int z0 = cz - 60;
        int z1 = cz - 36;
        pave(b, x0, z0, x1, z1, ground, GRAVEL);
        for (int i = 0; i < 4; i++) {
            b.fill(x0 + 2, ground + 1, z0 + 4 + i * 3, x0 + 10, ground + 2, z0 + 5 + i * 3,
                    OAK_LOG);
        }
        b.fill(x0 + 14, ground - 1, z0 + 6, x0 + 20, ground - 1, z0 + 10, p.foundation());
        b.fill(x0 + 14, ground, z0 + 6, x0 + 20, ground, z0 + 10, AIR);
        b.fill(x0 + 14, ground + 1, z0 + 6, x0 + 20, ground + 1, z0 + 10, p.roofSlab());
        shed(b, x0 + 2, z1 - 6, ground, p);
        signPost(b, x1 - 2, z1 + 2, ground, p, Direction.SOUTH, "TIMBER YARD");
    }

    private static void ruin(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 205;
        int x1 = cx - 180;
        int z0 = cz - 134;
        int z1 = cz - 114;
        b.fill(x0, ground, z0, x1, ground, z1, p.rubble());
        b.walls(x0, ground + 1, z0, x1, ground + 4, z1, p.weathered());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 4, z1 - 1, AIR);
        b.fill(x0 + 2, ground + 1, z0, x0 + 5, ground + 4, z0, AIR);
        b.fill(x0, ground + 2, z0 + 3, x0, ground + 4, z1 - 3, AIR);
        b.fill(x0 + 3, ground + 1, z0 + 5, x1 - 3, ground + 1, z0 + 5, p.accent());
        b.put(x0 + 6, ground + 1, z0 + 2, p.rubble());
        b.put(x0 + 8, ground + 2, z0 + 2, p.rubble());
        signPost(b, x1 + 2, z1, ground, p, Direction.SOUTH, "DANGER", "DEMOLISHED");
    }

    private static void yard(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                             Palette p, int v) {
        b.fill(x0, y, z0, x1, y, z1, Math.floorMod(v, 2) == 0 ? GRAVEL : DIRT);
        hedge(b, x0, z0, x1, z1, y + 1);
        if (Math.floorMod(v, 3) == 0) {
            shed(b, x0 + 2, z0 + 2, y, p);
        } else if (Math.floorMod(v, 3) == 1) {
            eucalyptus(b, (x0 + x1) / 2, (z0 + z1) / 2, y);
        } else {
            crate(b, x0 + 3, z0 + 3, y, p, 1);
            barrel(b, x0 + 5, z0 + 3, y);
            b.put(x0 + 7, y + 1, z0 + 3, HAY);
        }
        b.put(x0 + 1, y + 1, z1 - 1, p.light());
    }

    // ------------------------------------------------------------------ rail yards

    private static void railYards(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        station(b, cx, cz, ground, p);
        railLine(b, cx + 140, cx + 320, cz - 28, ground, p);
        railLine(b, cx + 140, cx + 320, cz - 16, ground, p);
        railLine(b, cx + 140, cx + 220, cz - 4, ground, p);
        engineShed(b, cx, cz, ground, p);
        coalYard(b, cx, cz, ground, p);
        customsHouse(b, cx, cz, ground, p);
        bondedStore(b, cx, cz, ground, p);
        goodsShed(b, cx + 160, cz + 40, cx + 210, cz + 70, ground, p);
        goodsShed(b, cx + 220, cz + 40, cx + 270, cz + 70, ground, p);
        goodsShed(b, cx + 280, cz + 40, cx + 320, cz + 70, ground, p);
        workshop(b, cx + 160, cz + 80, cx + 200, cz + 110, ground, p, 0);
        workshop(b, cx + 210, cz + 80, cx + 250, cz + 110, ground, p, 1);
        workshop(b, cx + 260, cz + 80, cx + 300, cz + 110, ground, p, 2);
        turntable(b, cx + 305, cz - 40, ground, p);
        for (int x = cx + 150; x <= cx + 310; x += 40) {
            lanternPost(b, x, cz + 30, ground, p);
        }
        signPost(b, cx + 136, cz + 146, ground, p, Direction.SOUTH, "RAIL YARDS");
        signPost(b, cx + 140, cz + 24, ground, p, Direction.SOUTH, "STATION");
        b.put(cx + 140, ground + 1, cz + 150, p.light());
        b.put(cx + 140, ground + 1, cz + 130, p.light());
    }

    private static void station(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 175;
        int x1 = cx + 255;
        int z0 = cz - 72;
        int z1 = cz - 44;
        int top = ground + 10;
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, top - 1, z1, p.wall());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, top - 1, z1 - 1, AIR);
        b.fill(x0, top, z0, x1, top, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, top, 5, 0);
        quoins(b, x0, z0, x1, z1, ground + 1, top - 1, p);
        for (int i = 0; i < 3; i++) {
            door(b, p, x0 + 24 + i * 24, ground + 1, z1, Direction.SOUTH);
            b.fill(x0 + 21 + i * 24, ground + 5, z1, x0 + 27 + i * 24, ground + 5, z1,
                    p.roofSlab());
        }
        for (int x = x0 + 6; x <= x1 - 6; x += 12) {
            b.window(x, ground + 4, z1, 3, 2, true);
        }
        for (int z = z0 + 6; z <= z1 - 6; z += 10) {
            b.window(x0, ground + 4, z, 3, 1, true);
            b.window(x1, ground + 4, z, 3, 1, true);
        }
        clockTower(b, x0 + 8, z1 - 8, ground, p);
        b.room(x0 + 30, ground + 1, z0 + 6, x0 + 44, ground + 5, z0 + 16);
        b.fill(x0 + 32, ground + 2, z0 + 6, x0 + 42, ground + 2, z0 + 6, p.foundation());
        sign(b, p, x0 + 37, ground + 4, z0 + 6, Direction.NORTH, "TICKETS");
        b.put(x0 + 32, ground + 3, z0 + 7, p.light());
        b.put(x0 + 42, ground + 3, z0 + 7, p.light());
        sign(b, p, x0 + 40, ground + 8, z1, Direction.SOUTH, "STATION");
        sign(b, p, x0 + 52, ground + 8, z1, Direction.SOUTH, "RAIL YARDS");
        int px0 = x0 - 10;
        int px1 = x1 + 30;
        b.fill(px0, ground, z1 + 2, px1, ground, z1 + 10, p.foundation());
        b.fill(px0, ground + 1, z1 + 2, px1, ground + 1, z1 + 10, p.ground());
        b.fill(x0 + 20, ground + 1, z1, x1 - 20, ground + 1, z1 + 2, p.ground());
        for (int x = px0 + 4; x <= px1 - 4; x += 12) {
            b.fill(x, ground + 2, z1 + 8, x, ground + 6, z1 + 8, p.accent());
            b.put(x, ground + 7, z1 + 8, p.light());
        }
        b.fill(px0, ground + 7, z1 + 7, px1, ground + 7, z1 + 9, p.roofSlab());
        b.fill(px0 + 10, ground + 2, z1 + 4, px0 + 12, ground + 2, z1 + 4, p.roofSlab());
        b.fill(px0 + 24, ground + 2, z1 + 4, px0 + 26, ground + 2, z1 + 4, p.roofSlab());
        b.fill(px0 + 38, ground + 2, z1 + 4, px0 + 40, ground + 2, z1 + 4, p.roofSlab());
        signPost(b, x0 + 30, z1 + 4, ground + 1, p, Direction.NORTH, "TICKETS");
        b.put(x0 + 60, ground + 1, z1 + 5, p.light());
        b.put(x0 + 68, ground + 1, z1 + 5, p.light());
    }

    private static void clockTower(StructureBuilder b, int x, int z, int ground, Palette p) {
        int x0 = x - 8;
        int x1 = x + 8;
        int z0 = z - 8;
        int z1 = z + 8;
        b.room(x0, ground, z0, x1, ground + 26, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 25, p);
        b.fill(x0, ground + 26, z0, x1, ground + 26, z1, p.roofSlab());
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 27);
        b.spire(x, z, ground + 27, 6);
        clockFace(b, x, ground + 20, z1, Direction.SOUTH);
        clockFace(b, x1, ground + 20, z, Direction.EAST);
        b.window(x, ground + 8, z1, 3, 2, true);
        b.window(x, ground + 14, z1, 3, 2, true);
        b.put(x0 + 2, ground + 1, z1 - 2, p.light());
    }

    private static void engineShed(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 260;
        int x1 = cx + 310;
        int z0 = cz - 100;
        int z1 = cz - 70;
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 11, z1, p.wall());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 10, z1 - 1, AIR);
        b.fill(x0, ground + 11, z0, x1, ground + 11, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 11, 5, 0);
        int dcx = cx + 285;
        for (int dx = -8; dx <= 8; dx++) {
            int h = 9 - Math.abs(dx) / 2;
            b.fill(dcx + dx, ground + 1, z1, dcx + dx, ground + h, z1, AIR);
        }
        b.fill(dcx, ground + 1, z0 + 2, dcx, ground + 1, z1, RAIL_NS);
        b.put(dcx, ground + 9, z1, p.foundation());
        sign(b, p, dcx, ground + 10, z1, Direction.SOUTH, "ENGINE SHED");
        b.put(x0 + 2, ground + 1, z1 - 2, p.light());
        b.put(x1 - 2, ground + 1, z1 - 2, p.light());
        waterTower(b, cx + 252, cz - 95, ground, p);
    }

    private static void waterTower(StructureBuilder b, int x, int z, int y, Palette p) {
        b.fill(x - 3, y, z - 3, x + 3, y, z + 3, p.foundation());
        for (int dx = -3; dx <= 3; dx += 6) {
            for (int dz = -3; dz <= 3; dz += 6) {
                b.fill(x + dx, y + 1, z + dz, x + dx, y + 6, z + dz, p.accent());
            }
        }
        b.fill(x - 4, y + 7, z - 4, x + 4, y + 10, z + 4, p.foundation());
        b.fill(x - 3, y + 8, z - 3, x + 3, y + 9, z + 3, WATER);
        b.fill(x - 4, y + 11, z - 4, x + 4, y + 11, z + 4, p.roofSlab());
        b.put(x, y + 12, z, p.light());
    }

    private static void coalYard(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 145;
        int x1 = cx + 200;
        int z0 = cz - 10;
        int z1 = cz + 12;
        pave(b, x0, z0, x1, z1, ground, GRAVEL);
        for (int i = 0; i < 5; i++) {
            int r = 4 - i;
            b.fill(cx + 160 - r, ground + i, cz + 2 - r, cx + 160 + r, ground + i, cz + 2 + r,
                    COAL);
        }
        b.fill(x0, ground, z0, x0, ground + 1, z1, p.weathered());
        wagon(b, cx + 180, cz - 4, ground, p, true);
        wagon(b, cx + 192, cz - 4, ground, p, false);
        crane(b, cx + 195, cz + 4, ground, p);
        signPost(b, cx + 150, cz + 14, ground, p, Direction.SOUTH, "COAL YARD");
        b.put(cx + 148, ground + 1, cz + 10, p.light());
    }

    private static void customsHouse(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 224;
        int x1 = cx + 256;
        int z0 = cz - 12;
        int z1 = cz + 12;
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 8, z1, p.wall());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 7, z1 - 1, AIR);
        b.fill(x0, ground + 8, z0, x1, ground + 8, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 8, 4, 0);
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 7, p);
        for (int x = x0 + 4; x <= x1 - 4; x += 6) {
            if (x == cx + 240) {
                continue;
            }
            b.fill(x, ground + 1, z1 + 2, x, ground + 6, z1 + 2, p.foundation());
            b.put(x, ground + 7, z1 + 2, p.accent());
        }
        b.fill(x0 + 2, ground + 7, z1 + 1, x1 - 2, ground + 7, z1 + 3, p.roofSlab());
        door(b, p, cx + 240, ground + 1, z1, Direction.SOUTH);
        sign(b, p, cx + 240, ground + 5, z1, Direction.SOUTH, "CUSTOMS");
        b.fill(x0 + 3, ground + 1, z0 + 3, x0 + 9, ground + 1, z0 + 3, p.accent());
        b.fill(x0 + 3, ground + 2, z0 + 3, x0 + 9, ground + 2, z0 + 3, p.foundation());
        b.fill(x1 - 9, ground + 1, z0 + 2, x1 - 9, ground + 4, z1 - 2, BOOKS);
        crate(b, x1 - 4, z0 + 3, ground, p, 1);
        b.put(x0 + 2, ground + 1, z1 - 2, p.light());
        signPost(b, cx + 232, cz + 14, ground, p, Direction.SOUTH, "SMUGGLED CARGO",
                "SEE CUSTOMS");
    }

    private static void bondedStore(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 262;
        int x1 = cx + 300;
        int z0 = cz - 8;
        int z1 = cz + 12;
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 8, z1, p.weathered());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 7, z1 - 1, AIR);
        b.fill(x0, ground + 8, z0, x1, ground + 8, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 8, 4, 0);
        door(b, p, cx + 280, ground + 1, z1, Direction.SOUTH);
        sign(b, p, cx + 280, ground + 5, z1, Direction.SOUTH, "BONDED STORE");
        crate(b, x0 + 4, z0 + 3, ground, p, 2);
        crate(b, x1 - 4, z0 + 3, ground, p, 1);
        barrel(b, x0 + 6, z1 - 3, ground);
        b.chest(cx + 292, ground + 1, z0 + 4, SiteLoot.ORDER);
        b.put(x0 + 2, ground + 1, z1 - 2, p.light());
    }

    private static void goodsShed(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  int ground, Palette p) {
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 8, z1, p.wall());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 7, z1 - 1, AIR);
        b.fill(x0, ground + 8, z0, x1, ground + 8, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 8, 4, 1);
        door(b, p, (x0 + x1) / 2, ground + 1, z1, Direction.SOUTH);
        sign(b, p, (x0 + x1) / 2, ground + 6, z1, Direction.SOUTH, "GOODS");
        for (int i = 0; i < 3; i++) {
            crate(b, x0 + 4 + i * 4, z0 + 4, ground, p, 1 + (i & 1));
        }
        b.put(x0 + 2, ground + 1, z1 - 2, BARREL);
        b.put(x1 - 2, ground + 1, z1 - 2, BARREL);
    }

    private static void workshop(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                                 Palette p, int variant) {
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 8, z1, p.wall());
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 7, z1 - 1, AIR);
        b.fill(x0, ground + 8, z0, x1, ground + 8, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 8, 4, 0);
        door(b, p, (x0 + x1) / 2, ground + 1, z1, Direction.SOUTH);
        sign(b, p, (x0 + x1) / 2, ground + 6, z1, Direction.SOUTH, "WORKSHOPS");
        int chx = x0 + 3 + variant * 2;
        for (int i = 0; i < 9; i++) {
            b.fill(chx - 1, ground + 9 + i, z0 + 2, chx + 1, ground + 9 + i, z0 + 4,
                    RED_TERRACOTTA);
        }
        b.put(chx, ground + 18, z0 + 3, p.light());
        barrel(b, x0 + 2, z1 - 2, ground);
        b.put(x0 + 3, ground + 1, z1 - 3, CAULDRON);
        crate(b, x1 - 3, z1 - 3, ground, p, 1);
        b.put(x0 + 2, ground + 1, z0 + 2, p.light());
    }

    private static void turntable(StructureBuilder b, int x, int z, int ground, Palette p) {
        for (int dx = -9; dx <= 9; dx++) {
            for (int dz = -9; dz <= 9; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > 81) {
                    continue;
                }
                b.put(x + dx, ground, z + dz, d2 > 64 ? p.foundation() : p.rubble());
            }
        }
        b.fill(x - 9, ground + 1, z, x + 9, ground + 1, z, RAIL_EW);
        b.fill(x - 2, ground, z - 2, x + 2, ground, z + 2, p.foundation());
        b.put(x, ground + 2, z, p.light());
    }

    // ------------------------------------------------------------------ Botanic Garden and Domain

    private static void botanicGarden(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        lawn(b, cx - 115, cz + 2, cx - 12, cz + 128, ground);
        lawn(b, cx + 12, cz + 2, cx + 115, cz + 128, ground);
        hedge(b, cx - 116, cz + 2, cx - 116, cz + 128, ground);
        hedge(b, cx + 116, cz + 2, cx + 116, cz + 128, ground);
        hedge(b, cx - 115, cz + 130, cx - 8, cz + 130, ground);
        hedge(b, cx + 8, cz + 130, cx + 115, cz + 130, ground);
        for (int z = 8; z <= 124; z += 18) {
            fig(b, cx - 12, cz + z, ground);
            fig(b, cx + 12, cz + z, ground);
        }
        parterre(b, cx - 100, cz + 20, cx - 50, cz + 48, ground, p);
        parterre(b, cx - 100, cz + 72, cx - 50, cz + 100, ground, p);
        parterre(b, cx + 20, cz + 18, cx + 70, cz + 44, ground, p);
        parterre(b, cx + 80, cz + 72, cx + 110, cz + 100, ground, p);
        fountain(b, cx + 40, cz + 60, ground, p);
        pond(b, cx + 60, cz + 100, ground, p);
        bandstand(b, cx - 40, cz + 40, ground, p);
        palmHouse(b, cx - 90, cz + 110, ground, p);
        museum(b, cx + 70, cz - 40, ground, p);
        lawn(b, cx - 115, cz - 80, cx - 12, cz - 2, ground);
        lawn(b, cx + 12, cz - 80, cx + 115, cz - 2, ground);
        for (int x = -100; x <= 100; x += 25) {
            if (Math.abs(x) < 16) {
                continue;
            }
            eucalyptus(b, cx + x, cz - 70, ground);
        }
        for (int z = -70; z <= -10; z += 20) {
            lanternPost(b, cx - 10, cz + z, ground, p);
            lanternPost(b, cx + 10, cz + z, ground, p);
        }
        signPost(b, cx + 10, cz + 132, ground, p, Direction.SOUTH, "BOTANIC GARDEN");
        signPost(b, cx + 40, cz + 70, ground, p, Direction.SOUTH, "FOUNTAIN");
        signPost(b, cx + 70, cz - 24, ground, p, Direction.SOUTH, "MUSEUM");
        signPost(b, cx - 66, cz + 120, ground, p, Direction.SOUTH, "PALM HOUSE");
        b.put(cx - 10, ground + 1, cz + 132, p.light());
        b.put(cx + 10, ground + 1, cz + 132, p.light());
    }

    private static void pond(StructureBuilder b, int x, int z, int y, Palette p) {
        b.fill(x - 12, y, z - 9, x + 12, y, z + 9, p.weathered());
        b.fill(x - 11, y, z - 8, x + 11, y, z + 8, WATER);
        b.fill(x - 11, y - 1, z - 8, x + 11, y - 1, z + 8, SAND);
        for (int i = 0; i < 6; i++) {
            b.put(x - 8 + i * 3, y, z - 4 + Math.floorMod(i, 3) * 3, LILY);
        }
        b.fill(x - 2, y + 1, z - 9, x + 2, y + 1, z + 9, p.roofSlab());
        b.fill(x - 2, y + 2, z - 9, x - 2, y + 2, z + 9, p.rail());
        b.fill(x + 2, y + 2, z - 9, x + 2, y + 2, z + 9, p.rail());
    }

    private static void bandstand(StructureBuilder b, int x, int z, int y, Palette p) {
        b.fill(x - 6, y, z - 6, x + 6, y, z + 6, p.foundation());
        b.fill(x - 6, y + 1, z - 6, x + 6, y + 1, z + 6, p.ground());
        for (int dx = -6; dx <= 6; dx += 6) {
            for (int dz = -6; dz <= 6; dz += 6) {
                b.fill(x + dx, y + 2, z + dz, x + dx, y + 5, z + dz, p.accent());
            }
        }
        b.fill(x - 7, y + 6, z - 7, x + 7, y + 6, z + 7, p.roofSlab());
        b.fill(x - 4, y + 7, z - 4, x + 4, y + 7, z + 4, p.roofSlab());
        b.put(x, y + 8, z, p.light());
        b.fill(x - 6, y + 2, z - 6, x + 6, y + 2, z - 6, p.rail());
        b.fill(x - 6, y + 2, z + 6, x + 6, y + 2, z + 6, p.rail());
    }

    private static void palmHouse(StructureBuilder b, int x, int z, int y, Palette p) {
        int x0 = x - 13;
        int x1 = x + 13;
        int z0 = z - 7;
        int z1 = z + 7;
        b.fill(x0, y, z0, x1, y, z1, p.foundation());
        b.fill(x0, y + 1, z0, x1, y + 1, z1, p.ground());
        b.walls(x0, y + 1, z0, x1, y + 6, z1, GLASS);
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 5, z1 - 1, AIR);
        for (int xx = x0; xx <= x1; xx += 4) {
            b.fill(xx, y + 1, z0, xx, y + 6, z0, p.rail());
            b.fill(xx, y + 1, z1, xx, y + 6, z1, p.rail());
        }
        b.fill(x0, y + 7, z0, x1, y + 7, z1, GLASS);
        b.fill(x0 + 3, y + 8, z0 + 2, x1 - 3, y + 8, z1 - 2, GLASS);
        b.fill(x0 + 6, y + 9, z0 + 4, x1 - 6, y + 9, z1 - 4, GLASS);
        for (int i = 0; i < 4; i++) {
            b.put(x0 + 4 + i * 6, y + 1, z0 + 3, JUNGLE_LOG);
            b.put(x0 + 4 + i * 6, y + 2, z0 + 3, JUNGLE_LOG);
            b.put(x0 + 4 + i * 6, y + 3, z0 + 3, JUNGLE_LEAF);
        }
        door(b, p, x, y + 1, z1, Direction.SOUTH);
        sign(b, p, x, y + 5, z1, Direction.SOUTH, "PALM HOUSE");
    }

    private static void museum(StructureBuilder b, int x, int z, int y, Palette p) {
        int x0 = x - 20;
        int x1 = x + 20;
        int z0 = z - 15;
        int z1 = z + 15;
        b.fill(x0 - 2, y, z0 - 2, x1 + 2, y, z1 + 2, p.foundation());
        b.fill(x0 - 1, y + 1, z0 - 1, x1 + 1, y + 1, z1 + 1, p.ground());
        b.walls(x0, y + 2, z0, x1, y + 11, z1, p.wall());
        b.fill(x0 + 1, y + 2, z0 + 1, x1 - 1, y + 10, z1 - 1, AIR);
        b.fill(x0, y + 12, z0, x1, y + 12, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y + 12, 4, 0);
        for (int col = x0 + 4; col <= x1 - 4; col += 6) {
            b.fill(col, y + 2, z1 + 2, col, y + 8, z1 + 2, p.foundation());
            b.put(col, y + 9, z1 + 2, p.accent());
        }
        b.fill(x0 + 2, y + 10, z1 + 1, x1 - 2, y + 10, z1 + 3, p.roofSlab());
        for (int i = 0; i <= 4; i++) {
            b.fill(x0 + 4 + i * 2, y + 11 + i, z1 + 2, x1 - 4 - i * 2, y + 11 + i, z1 + 2,
                    p.wall());
        }
        b.fill(x - 3, y + 1, z1 + 1, x + 3, y + 1, z1 + 1, p.foundation());
        door(b, p, x, y + 2, z1, Direction.SOUTH);
        sign(b, p, x, y + 7, z1, Direction.SOUTH, "MUSEUM");
        for (int i = 0; i < 3; i++) {
            int dx = x0 + 8 + i * 12;
            b.fill(dx, y + 1, z0 + 6, dx + 2, y + 2, z0 + 8, p.foundation());
            b.put(dx + 1, y + 3, z0 + 7, GLASS);
            b.put(dx + 1, y + 4, z0 + 7, p.light());
        }
        b.fill(x0 + 2, y + 1, z0 + 1, x0 + 2, y + 4, z1 - 1, BOOKS);
        b.fill(x1 - 2, y + 1, z0 + 1, x1 - 2, y + 4, z1 - 1, BOOKS);
        b.fill(x0 + 4, y + 1, z0 + 1, x0 + 8, y + 1, z0 + 3, p.accent());
        b.put(x0 + 6, y + 2, z0 + 2, p.light());
        b.put(x0 + 2, y + 1, z1 - 2, p.light());
    }

    // ------------------------------------------------------------------ Bondi

    private static void bondi(StructureBuilder b, int cx, int cz, int ground,
                              @Nullable CitySurface surface, Palette p) {
        int beachY = at(surface, cx + 250, cz + 290, ground);
        int promY = at(surface, cx + 240, cz + 246, ground);
        int pavY = at(surface, cx + 240, cz + 220, ground);
        b.fill(cx + 190, beachY - 3, cz + 317, cx + 320, beachY - 3, cz + 336, SAND);
        b.fill(cx + 190, beachY - 2, cz + 317, cx + 320, beachY - 1, cz + 336, WATER);
        b.fill(cx + 190, beachY, cz + 256, cx + 320, beachY, cz + 316, SAND);
        b.fill(cx + 190, beachY - 1, cz + 256, cx + 320, beachY - 1, cz + 316, SAND);
        pave(b, cx + 140, cz + 236, cx + 320, cz + 256, promY, p.ground());
        b.fill(cx + 140, promY - 1, cz + 236, cx + 320, promY - 1, cz + 256, p.foundation());
        steps(b, cx + 228, cx + 252, cz + 252, cz + 256, promY, beachY, p);
        steps(b, cx + 222, cx + 258, cz + 232, cz + 236, pavY, promY, p);
        pavilion(b, cx, cz, pavY, p);
        surfSheds(b, cx, cz, promY, p);
        lifeguard(b, cx, cz, beachY, p);
        for (int x = 150; x <= 250; x += 25) {
            palm(b, cx + x, cz + 242, promY);
        }
        umbrella(b, cx + 210, cz + 270, beachY, RED_WOOL, p);
        umbrella(b, cx + 235, cz + 285, beachY, BLUE_WOOL, p);
        umbrella(b, cx + 265, cz + 275, beachY, YELLOW_WOOL, p);
        umbrella(b, cx + 300, cz + 268, beachY, WHITE_WOOL, p);
        b.fill(cx + 212, beachY + 1, cz + 280, cx + 216, beachY + 1, cz + 282, WHITE_WOOL);
        b.fill(cx + 270, beachY + 1, cz + 292, cx + 274, beachY + 1, cz + 294, BLUE_WOOL);
        cliffs(b, cx, cz, beachY, p);
        cliffPath(b, cx, cz, ground, surface, p);
        signPost(b, cx + 146, cz + 194, ground, p, Direction.SOUTH, "BONDI");
        signPost(b, cx + 150, cz + 206, ground, p, Direction.SOUTH, "BONDI BEACH");
        signPost(b, cx + 187, cz + 260, at(surface, cx + 187, cz + 260, ground), p,
                Direction.SOUTH, "CLIFF EDGE", "KEEP BACK");
        b.put(cx + 148, ground + 1, cz + 206, p.light());
    }

    private static void steps(StructureBuilder b, int x0, int x1, int z0, int z1,
                              int fromY, int toY, Palette p) {
        int n = Math.abs(toY - fromY);
        int dir = Integer.compare(toY, fromY);
        for (int i = 0; i <= n; i++) {
            int y = fromY + dir * i;
            int z = z0 + (z1 - z0) * i / Math.max(1, n);
            b.fill(x0, y, z, x1, y, z, p.foundation());
            b.fill(x0, y - 1, z, x1, y - 1, z, p.foundation());
        }
    }

    private static void pavilion(StructureBuilder b, int cx, int cz, int y, Palette p) {
        int x0 = cx + 222;
        int x1 = cx + 258;
        int z0 = cz + 208;
        int z1 = cz + 232;
        b.fill(x0 - 1, y - 4, z0 - 1, x1 + 1, y, z1 + 1, p.foundation());
        b.fill(x0 - 1, y + 1, z0 - 1, x1 + 1, y + 1, z1 + 1, p.ground());
        b.walls(x0, y + 1, z0, x1, y + 10, z1, p.wall());
        b.fill(x0 + 1, y + 1, z0 + 1, x1 - 1, y + 9, z1 - 1, AIR);
        b.fill(x0, y + 11, z0, x1, y + 11, z1, p.roof());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y + 11, 5, 0);
        quoins(b, x0, z0, x1, z1, y + 1, y + 10, p);
        b.fill(x0 - 2, y + 1, z1 + 1, x1 + 2, y + 1, z1 + 4, p.roofSlab());
        b.fill(x0 - 2, y + 2, z1 + 4, x1 + 2, y + 2, z1 + 4, p.rail());
        for (int x = x0 - 2; x <= x1 + 2; x += 6) {
            b.fill(x, y + 1, z1 + 4, x, y + 3, z1 + 4, p.accent());
            b.put(x, y + 4, z1 + 4, p.light());
        }
        b.fill(x0 - 2, y + 4, z1 + 1, x1 + 2, y + 4, z1 + 4, p.roofSlab());
        door(b, p, cx + 240, y + 1, z1, Direction.SOUTH);
        b.window(cx + 230, y + 3, z1, 3, 2, true);
        b.window(cx + 248, y + 3, z1, 3, 2, true);
        b.window(x0, y + 3, cz + 220, 3, 1, true);
        b.window(x1, y + 3, cz + 220, 3, 1, true);
        sign(b, p, cx + 240, y + 7, z1, Direction.SOUTH, "BONDI", "PAVILION");
        b.fill(x0 + 2, y + 1, z0 + 2, x0 + 8, y + 3, z0 + 5, p.foundation());
        b.fill(x0 + 2, y + 1, z0 + 7, x0 + 5, y + 3, z0 + 10, p.accent());
        b.fill(x1 - 8, y + 1, z0 + 2, x1 - 2, y + 3, z0 + 5, p.foundation());
        b.fill(x1 - 5, y + 1, z0 + 7, x1 - 2, y + 3, z0 + 10, p.accent());
        b.put(cx + 240, y + 1, z0 + 2, p.light());
    }

    private static void surfSheds(StructureBuilder b, int cx, int cz, int y, Palette p) {
        for (int i = 0; i < 6; i++) {
            int x0 = cx + 264 + i * 8;
            b.fill(x0, y, cz + 240, x0 + 7, y, cz + 252, p.foundation());
            b.walls(x0, y + 1, cz + 240, x0 + 7, y + 4, cz + 252, p.weathered());
            b.fill(x0 + 1, y + 1, cz + 241, x0 + 6, y + 3, cz + 251, AIR);
            b.fill(x0 - 1, y + 5, cz + 239, x0 + 8, y + 5, cz + 253, p.roofSlab());
            door(b, p, x0 + 3, y + 1, cz + 252, Direction.SOUTH);
            b.put(x0 + 3, y + 3, cz + 252, Materials.concrete(
                    Math.floorMod(i, 2) == 0 ? DyeColor.RED : DyeColor.BLUE));
        }
        signPost(b, cx + 262, cz + 256, y, p, Direction.SOUTH, "SURF SHEDS");
    }

    private static void lifeguard(StructureBuilder b, int cx, int cz, int y, Palette p) {
        int x = cx + 290;
        int z = cz + 280;
        for (int dx = -3; dx <= 3; dx += 6) {
            for (int dz = -3; dz <= 3; dz += 6) {
                b.fill(x + dx, y + 1, z + dz, x + dx, y + 3, z + dz, p.accent());
            }
        }
        b.fill(x - 4, y + 4, z - 4, x + 4, y + 4, z + 4, p.roofSlab());
        b.fill(x - 3, y + 5, z - 3, x + 3, y + 5, z + 3, p.rail());
        b.put(x, y + 6, z, RED_WOOL);
        b.put(x, y + 7, z, YELLOW_WOOL);
        sign(b, p, x, y + 5, z + 4, Direction.SOUTH, "LIFEGUARD");
        b.put(x - 6, y + 1, z + 6, RED_WOOL);
        b.put(x + 6, y + 1, z + 6, YELLOW_WOOL);
    }

    private static void cliffs(StructureBuilder b, int cx, int cz, int beachY, Palette p) {
        int x0 = cx + 140;
        int x1 = cx + 180;
        int z0 = cz + 268;
        int z1 = cz + 318;
        for (int i = 0; i < 10; i++) {
            int inset = i / 2;
            b.walls(x0 + inset, beachY + i, z0 + inset, x1 - inset, beachY + i, z1 - inset,
                    SANDSTONE);
        }
        b.fill(x0 + 5, beachY + 9, z0 + 5, x1 - 5, beachY + 9, z1 - 5, SANDSTONE);
        b.fill(x0 + 5, beachY + 10, z0 + 5, x1 - 5, beachY + 10, z1 - 5, GRASS);
        for (int i = 0; i < 6; i++) {
            b.put(x0 + 2 + i * 5, beachY + 3, z1 - 1, CUT_SANDSTONE);
        }
        b.fill(x0 + 8, beachY + 11, z0 + 8, x0 + 22, beachY + 11, z0 + 14, p.foundation());
        b.fill(x0 + 8, beachY + 12, z0 + 8, x0 + 22, beachY + 12, z0 + 8, p.rail());
        b.fill(x0 + 8, beachY + 12, z0 + 14, x0 + 22, beachY + 12, z0 + 14, p.rail());
        b.put(x0 + 10, beachY + 12, z0 + 11, p.light());
        for (int i = 0; i < 10; i++) {
            b.put(cx + 181 - i, beachY + 1 + i, cz + 280, SAND_STAIRS);
            b.put(cx + 181 - i, beachY + 2 + i, cz + 280, AIR);
        }
    }

    private static void cliffPath(StructureBuilder b, int cx, int cz, int ground,
                                  @Nullable CitySurface surface, Palette p) {
        for (int z = 236; z <= 300; z++) {
            int y = at(surface, cx + 184, cz + z, ground);
            b.fill(cx + 182, y, cz + z, cx + 186, y, cz + z, p.ground());
            b.fill(cx + 182, y - 1, cz + z, cx + 186, y - 1, cz + z, p.foundation());
            b.put(cx + 181, y + 1, cz + z, p.weathered());
            if (Math.floorMod(z, 8) == 0) {
                b.put(cx + 186, y + 1, cz + z, p.accent());
                b.put(cx + 186, y + 2, cz + z, p.light());
            }
        }
    }

    // ------------------------------------------------------------------ secret sea cave

    private static void seaCave(StructureBuilder b, int cx, int cz, int ground,
                                @Nullable CitySurface surface, Palette p) {
        int mouthY = at(surface, cx + 186, cz + 300, ground);
        int floorY = mouthY - 6;
        int baseY = Math.min(mouthY, at(surface, cx + 250, cz + 290, ground));
        b.fill(cx + 186, baseY, cz + 292, cx + 202, mouthY + 6, cz + 308, SANDSTONE);
        caveShell(b, cx + 192, cz + 298, cx + 225, cz + 302, floorY, 4);
        caveShell(b, cx + 221, cz + 266, cx + 225, cz + 302, floorY, 4);
        caveShell(b, cx + 208, cz + 252, cx + 232, cz + 270, floorY, 5);
        b.fill(cx + 186, mouthY + 1, cz + 298, cx + 186, mouthY + 4, cz + 302, AIR);
        for (int i = 0; i <= 6; i++) {
            int x = cx + 186 + i;
            int y = mouthY - i;
            b.fill(x, y - 1, cz + 298, x, y - 1, cz + 302, SANDSTONE);
            b.fill(x, y, cz + 298, x, y + 3, cz + 302, AIR);
        }
        b.fill(cx + 210, floorY, cz + 254, cx + 214, floorY, cz + 258, WATER);
        b.fill(cx + 210, floorY - 1, cz + 254, cx + 214, floorY - 1, cz + 258, SAND);
        b.chest(cx + 228, floorY, cz + 256, SiteLoot.CULT);
        b.chest(cx + 212, floorY, cz + 266, SiteLoot.RELIC);
        barrel(b, cx + 230, cz + 264, floorY - 1);
        barrel(b, cx + 226, cz + 268, floorY - 1);
        b.fill(cx + 210, floorY, cz + 268, cx + 218, floorY, cz + 269, p.accent());
        b.fill(cx + 211, floorY + 1, cz + 268, cx + 217, floorY + 1, cz + 269, p.roofSlab());
        b.put(cx + 226, floorY + 3, cz + 262, p.light());
        b.put(cx + 214, floorY + 3, cz + 256, p.light());
        b.fill(cx + 220, floorY, cz + 253, cx + 220, floorY + 1, cz + 253, SANDSTONE);
        sign(b, p, cx + 220, floorY + 2, cz + 253, Direction.SOUTH, "SMUGGLERS", "CACHE",
                "THE SAILOR", "IS HERE");
        signPost(b, cx + 184, cz + 304, at(surface, cx + 184, cz + 304, ground), p,
                Direction.SOUTH, "SEA CAVE", "KEEP OUT");
        b.put(cx + 188, mouthY + 5, cz + 298, p.light());
    }

    // ------------------------------------------------------------------ north shore

    private static void northShore(StructureBuilder b, int cx, int cz, int ground,
                                   @Nullable CitySurface surface, Palette p) {
        int ny = at(surface, cx, cz - 300, ground);
        lawn(b, cx - 120, cz - 330, cx - 12, cz - 268, ny);
        lawn(b, cx + 12, cz - 330, cx + 120, cz - 268, ny);
        for (int x = -110; x <= 110; x += 22) {
            if (Math.abs(x) < 16) {
                continue;
            }
            eucalyptus(b, cx + x, cz - 300, at(surface, cx + x, cz - 300, ground));
        }
        pine(b, cx - 90, cz - 320, at(surface, cx - 90, cz - 320, ground));
        pine(b, cx + 90, cz - 320, at(surface, cx + 90, cz - 320, ground));
        beacon(b, cx - 60, cz - 300, ny, p);
        lookout(b, cx + 60, cz - 300, ny, p);
        signPost(b, cx, cz - 266, ny, p, Direction.SOUTH, "HARBOUR", "DEEP WATER");
        for (int i = 0; i < 5; i++) {
            b.put(cx - 100 + i * 50, ny + 1, cz - 272, p.weathered());
            b.put(cx - 100 + i * 50, ny + 1, cz - 271, p.weathered());
        }
        b.put(cx, ny + 1, cz - 270, p.light());
    }

    private static void beacon(StructureBuilder b, int x, int z, int y, Palette p) {
        b.fill(x - 4, y, z - 4, x + 4, y, z + 4, p.foundation());
        b.walls(x - 3, y + 1, z - 3, x + 3, y + 10, z + 3, p.wall());
        b.fill(x - 2, y + 1, z - 2, x + 2, y + 9, z + 2, AIR);
        b.fill(x - 3, y + 10, z - 3, x + 3, y + 10, z + 3, p.foundation());
        b.crenellations(x - 4, z - 4, x + 4, z + 4, y + 11);
        b.put(x, y + 11, z, p.light());
        b.fill(x - 1, y + 5, z - 3, x + 1, y + 7, z - 3, p.window());
        b.fill(x - 1, y + 5, z + 3, x + 1, y + 7, z + 3, p.window());
        sign(b, p, x, y + 8, z + 3, Direction.SOUTH, "MACQUARIE", "POINT");
    }

    private static void lookout(StructureBuilder b, int x, int z, int y, Palette p) {
        b.fill(x - 5, y, z - 5, x + 5, y, z + 5, p.foundation());
        b.fill(x - 5, y + 1, z - 5, x - 5, y + 2, z + 5, p.rail());
        b.fill(x + 5, y + 1, z - 5, x + 5, y + 2, z + 5, p.rail());
        b.fill(x - 5, y + 1, z - 5, x + 5, y + 1, z - 5, p.rail());
        b.fill(x - 5, y + 1, z + 5, x + 5, y + 1, z + 5, p.rail());
        b.put(x, y + 1, z, p.light());
        b.put(x - 3, y + 1, z - 3, p.accent());
        b.put(x + 3, y + 1, z + 3, p.accent());
    }
}
