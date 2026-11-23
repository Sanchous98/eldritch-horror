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
 * Cape Town under the Company: the long whitewashed manor and its stepped gable behind a walled
 * werf, Table Bay's quay with warehouses, cranes and ships, the painted slopes and narrow lanes of
 * Bo-Kaap, the formal Company's Garden, District Six's brick terraces, and the mountain foot with
 * its signal station. Every block is placed deliberately; there is no layout RNG.
 *
 * <p>The whole city is written on every call, as the plan contract requires: the builder keeps
 * only the slice that falls inside the generating chunk, so every layout decision is absolute and
 * chunk-independent. Nothing here reads the generating chunk or the builder's RNG.
 */
public final class CapeTownPlan implements CityPlan {

    /** Authored half-extent; everything below stays inside +/-336. */
    private static final int HALF_EXTENT = 340;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState BRICK = Blocks.BRICKS.defaultBlockState();
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    private static final BlockState OAK_LOG = Blocks.OAK_LOG.defaultBlockState();
    private static final BlockState OAK_LEAVES = Blocks.OAK_LEAVES.defaultBlockState();
    private static final BlockState PINE_LOG = Blocks.DARK_OAK_LOG.defaultBlockState();
    private static final BlockState PINE_LEAVES = Blocks.SPRUCE_LEAVES.defaultBlockState();
    private static final BlockState BARREL = Blocks.BARREL.defaultBlockState();
    private static final BlockState BOOKSHELF = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState CAULDRON = Blocks.CAULDRON.defaultBlockState();

    @Override
    public String cityId() {
        return "cape_town";
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
        manor(b, cx, cz, s, p);
        wineCellar(b, cx, cz, s, p);
        harbour(b, cx, cz, s, p);
        boKaap(b, cx, cz, s, p);
        companyGarden(b, cx, cz, s, p);
        districtSix(b, cx, cz, s, p);
        railStation(b, cx, cz, s, p);
        mountainFoot(b, cx, cz, s, p);
        hotel(b, cx, cz, s, p);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.LANDMARK, 0, 0, 320, "gate"),
                new CityPoi("rail_station", CityPoi.Kind.STATION, 196, 1, 326,
                        "cape_town_rail;east_west"),
                new CityPoi("gate_signpost", CityPoi.Kind.SIGN, -10, 1, 328,
                        "CAPE MANOR|HARBOUR|BO-KAAP|GARDEN|DISTRICT SIX"),
                new CityPoi("city_notices", CityPoi.Kind.SIGN, -20, 1, 334, "CITY NOTICES"),
                new CityPoi("cape_manor", CityPoi.Kind.LANDMARK, 0, 0, -200,
                        "gable,werf,oaks"),
                new CityPoi("manor_steward", CityPoi.Kind.NPC, 0, 0, -160,
                        "steward,cape_town_sealed_letter"),
                new CityPoi("slave_lodge", CityPoi.Kind.LANDMARK, -76, 0, -266,
                        "company records"),
                new CityPoi("wine_cellar", CityPoi.Kind.SECRET, 40, -3, -180,
                        "chests,lore"),
                new CityPoi("harbour_quay", CityPoi.Kind.LANDMARK, 220, 0, 60,
                        "ships,cranes,fish"),
                new CityPoi("harbour_port", CityPoi.Kind.PORT, 299, 0, 0, "table_bay"),
                new CityPoi("harbour_quest_board", CityPoi.Kind.QUEST_BOARD, 220, 0, 0,
                        "cape_town_missing_mountaineer,cape_town_sealed_letter"),
                new CityPoi("dockworker", CityPoi.Kind.NPC, 270, 0, 30, "old_harbour"),
                new CityPoi("fish_market", CityPoi.Kind.LANDMARK, 220, 0, 44, "fish"),
                new CityPoi("cape_hotel", CityPoi.Kind.INN, -80, 0, -120, "hotel_keeper"),
                new CityPoi("hotel_keeper", CityPoi.Kind.NPC, -80, 0, -108, "cape_hotel"),
                new CityPoi("bo_kaap_shop", CityPoi.Kind.SHOP, -240, 0, -60, "merchant"),
                new CityPoi("merchant", CityPoi.Kind.NPC, -241, 0, -60, "bo_kaap_shop"),
                new CityPoi("herbalist", CityPoi.Kind.HEALER, -240, 0, 120, "remedies"),
                new CityPoi("herbalist_npc", CityPoi.Kind.NPC, -241, 0, 120, "herbalist"),
                new CityPoi("bo_kaap_mosque", CityPoi.Kind.LANDMARK, -279, 0, 66, "imam"),
                new CityPoi("imam", CityPoi.Kind.NPC, -268, 0, 66, "bo_kaap_mosque"),
                new CityPoi("company_garden", CityPoi.Kind.LANDMARK, 0, 0, 40,
                        "fountain,library"),
                new CityPoi("garden_library", CityPoi.Kind.LANDMARK, 58, 0, 20, "books"),
                new CityPoi("district_six", CityPoi.Kind.LANDMARK, -50, 0, 240,
                        "church,market"),
                new CityPoi("dominee", CityPoi.Kind.NPC, -50, 0, 254, "district_six"),
                new CityPoi("six_market", CityPoi.Kind.LANDMARK, 70, 0, 240, "market"),
                new CityPoi("signal_station", CityPoi.Kind.LANDMARK, 0, 0, -300,
                        "signal_keeper"),
                new CityPoi("signal_keeper", CityPoi.Kind.NPC, 0, 0, -302,
                        "cape_town_missing_mountaineer"),
                new CityPoi("mountain_cairn", CityPoi.Kind.LANDMARK, 80, 0, -330,
                        "missing_mountaineer"),
                new CityPoi("last_camp", CityPoi.Kind.LANDMARK, 100, 0, -332, "clue"));
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

    private static void crate(StructureBuilder b, int x, int z, int ground, Palette p,
                              int height) {
        for (int i = 1; i <= height; i++) {
            b.put(x, ground + i, z, p.foundation());
        }
    }

    private static void oak(StructureBuilder b, int x, int z, int ground) {
        int height = 5 + Math.floorMod(x * 3 + z * 5, 3);
        for (int y = ground + 1; y <= ground + height; y++) {
            b.put(x, y, z, OAK_LOG);
        }
        for (int dy = -2; dy <= 2; dy++) {
            int radius = dy == 2 ? 1 : 2;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) > radius + 1) {
                        continue;
                    }
                    b.put(x + dx, ground + height + dy, z + dz, OAK_LEAVES);
                }
            }
        }
    }

    private static void pine(StructureBuilder b, int x, int z, int ground) {
        int height = 7 + Math.floorMod(x * 5 + z * 7, 4);
        for (int y = ground + 1; y <= ground + height; y++) {
            b.put(x, y, z, PINE_LOG);
        }
        for (int dy = 2; dy <= 6; dy++) {
            int radius = (7 - dy) / 2;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) > radius) {
                        continue;
                    }
                    b.put(x + dx, ground + height - 6 + dy, z + dz, PINE_LEAVES);
                }
            }
        }
        b.put(x, ground + height + 1, z, PINE_LEAVES);
    }

    /** Levels a rectangular pad to {@code ground}, then clears a little headroom. */
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
                for (int y = ground + 1; y <= ground + 4; y++) {
                    b.put(x, y, z, AIR);
                }
            }
        }
    }

    private static boolean overlaps(int x0, int z0, int x1, int z1,
                                    int ax0, int az0, int ax1, int az1) {
        return x0 <= ax1 && ax0 <= x1 && z0 <= az1 && az0 <= z1;
    }

    private static BlockState paint(int index) {
        return switch (Math.floorMod(index, 10)) {
            case 0 -> Materials.concrete(DyeColor.RED);
            case 1 -> Materials.concrete(DyeColor.ORANGE);
            case 2 -> Materials.concrete(DyeColor.YELLOW);
            case 3 -> Materials.concrete(DyeColor.LIME);
            case 4 -> Materials.concrete(DyeColor.LIGHT_BLUE);
            case 5 -> Materials.concrete(DyeColor.BLUE);
            case 6 -> Materials.concrete(DyeColor.PURPLE);
            case 7 -> Materials.concrete(DyeColor.MAGENTA);
            case 8 -> Materials.concrete(DyeColor.PINK);
            default -> Materials.concrete(DyeColor.CYAN);
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
        path(b, s, cx, cz + 336, cx, cz - 150, 14, p.ground());
        path(b, s, cx - 320, cz, cx + 300, cz, 14, p.ground());
        for (int z = -150; z <= 336; z++) {
            b.put(cx - 8, s.at(cx - 8, cz + z), cz + z, p.foundation());
            b.put(cx + 8, s.at(cx + 8, cz + z), cz + z, p.foundation());
        }
        for (int x = -320; x <= 300; x++) {
            b.put(cx + x, s.at(cx + x, cz - 8), cz - 8, p.foundation());
            b.put(cx + x, s.at(cx + x, cz + 8), cz + 8, p.foundation());
        }
        // A short spur from the main axis to the Cape Hotel.
        path(b, s, cx - 7, cz - 108, cx - 100, cz - 108, 6, p.ground());
        for (int z = -140; z <= 300; z += 40) {
            if (z == 260) {
                continue; // the District Six brick lane has its own lights
            }
            lanternPost(b, cx - 12, cz + z, s.at(cx - 12, cz + z), p);
            lanternPost(b, cx + 12, cz + z, s.at(cx + 12, cz + z), p);
        }

        // Harbour approach and the 12-wide quay.
        path(b, s, cx + 270, cz - 240, cx + 270, cz + 180, 6, p.ground());
        path(b, s, cx + 294, cz - 240, cx + 294, cz + 180, 12, p.foundation());

        // Bo-Kaap: five long lanes crossed by narrow lanes.
        int[] lanes = {-300, -260, -240, -220, -180};
        for (int x : lanes) {
            path(b, s, cx + x, cz - 220, cx + x, cz + 160, 5, p.ground());
        }
        int[] cross = {-200, -140, -80, -20, 40, 100, 160};
        for (int z : cross) {
            path(b, s, cx - 320, cz + z, cx - 140, cz + z, 5, p.ground());
        }

        // Company's Garden cross avenue.
        path(b, s, cx - 110, cz + 40, cx + 110, cz + 40, 8, p.ground());

        // District Six: two brick lanes and four vertical lanes.
        path(b, s, cx - 320, cz + 200, cx + 320, cz + 200, 6, BRICK);
        path(b, s, cx - 320, cz + 260, cx + 320, cz + 260, 6, BRICK);
        int[] sixLanes = {-220, -120, 120, 220};
        for (int x : sixLanes) {
            path(b, s, cx + x, cz + 186, cx + x, cz + 300, 6, BRICK);
        }

        // Mountain approach.
        path(b, s, cx, cz - 300, cx, cz - 290, 6, p.ground());
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
        sign(b, p, cx, ground + 10, cz + 320, Direction.SOUTH, "CAPE TOWN");
        b.put(cx - 20, ground + 1, cz + 324, p.light());
        b.put(cx + 20, ground + 1, cz + 324, p.light());

        String[][] directions = {
                {"CAPE MANOR"},
                {"HARBOUR"},
                {"BO-KAAP"},
                {"COMPANY'S", "GARDEN"},
                {"DISTRICT SIX"}};
        int[] posts = {-26, -18, -10, 10, 18};
        for (int i = 0; i < posts.length; i++) {
            signPost(b, cx + posts[i], cz + 328, ground, p, Direction.SOUTH, directions[i]);
        }

        b.fill(cx - 32, ground + 1, cz + 333, cx - 8, ground + 4, cz + 334, p.foundation());
        b.fill(cx - 33, ground + 5, cz + 333, cx - 7, ground + 5, cz + 335, p.roofSlab());
        sign(b, p, cx - 28, ground + 4, cz + 334, Direction.SOUTH, "CITY NOTICES");
        sign(b, p, cx - 22, ground + 4, cz + 334, Direction.SOUTH, "CURFEW AT DUSK");
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

    // ------------------------------------------------------------------ manor and werf

    private static void manor(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx, cz - 200);
        int x0 = cx - 55;
        int x1 = cx + 55;
        int z0 = cz - 240;
        int z1 = cz - 190;
        int wallTop = ground + 12;

        b.room(x0, ground, z0, x1, wallTop, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, wallTop - 1, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, wallTop, 9, 0);
        gable(b, cx, cz, ground, p);

        // Broad stoep and its low rail.
        b.fill(cx - 58, ground, z1, cx + 58, ground, z1 + 7, p.foundation());
        b.fill(cx - 58, ground + 1, z1 + 7, cx + 58, ground + 1, z1 + 7, p.rail());
        b.fill(cx - 4, ground + 1, z1 + 7, cx + 4, ground + 1, z1 + 7, AIR);
        for (int x = cx - 56; x <= cx + 56; x += 8) {
            if (x == cx) {
                continue; // keep the door path clear
            }
            b.put(x, ground + 1, z1 + 6, p.rail());
            b.put(x, ground + 2, z1 + 6, p.light());
        }
        door(b, p, cx, ground + 1, z1, Direction.SOUTH);
        sign(b, p, cx, ground + 6, z1, Direction.SOUTH, "CAPE MANOR");

        // Tall shuttered windows on the long south face.
        int[] bays = {-44, -28, -12, 12, 28, 44};
        for (int x : bays) {
            b.window(cx + x, ground + 3, z1, 3, 2, true);
            b.put(cx + x - 1, ground + 3, z1, p.accent());
            b.put(cx + x - 1, ground + 4, z1, p.accent());
            b.put(cx + x + 2, ground + 3, z1, p.accent());
            b.put(cx + x + 2, ground + 4, z1, p.accent());
        }
        for (int z = z0 + 10; z <= z1 - 10; z += 15) {
            b.window(x0, ground + 4, z, 3, 1, true);
            b.window(x1, ground + 4, z, 3, 1, true);
        }
        b.fill(cx, wallTop + 9, cz - 215, cx, wallTop + 14, cz - 215, p.accent());
        b.put(cx, wallTop + 15, cz - 215, p.light());

        werf(b, cx, cz, ground, p);
        slaveLodge(b, cx, cz, s, p);
    }

    /** The stepped, scrolled Cape Dutch gable on the manor's south face. */
    private static void gable(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int face = cz - 189;
        int base = ground + 12;
        for (int i = 0; i <= 8; i++) {
            int half = 13 - i;
            b.fill(cx - half, base + i, face, cx + half, base + i, face, p.wall());
            b.put(cx - half, base + i, face, p.accent());
            b.put(cx + half, base + i, face, p.accent());
            if ((i & 1) == 0) {
                b.put(cx - half + 1, base + i, face, p.weathered());
                b.put(cx + half - 1, base + i, face, p.weathered());
            }
        }
        b.window(cx - 2, base + 2, face, 4, 4, true);
        b.put(cx, base + 9, face, p.accent());
        b.put(cx, base + 10, face, p.accent());
        b.put(cx, base + 11, face, p.light());
        for (int s = -1; s <= 1; s += 2) {
            b.put(cx + s * 12, base + 1, face, p.roofSlab());
            b.put(cx + s * 11, base + 2, face, p.roofStairs());
            b.put(cx + s * 10, base + 3, face, p.roofSlab());
        }
    }

    private static void werf(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 95;
        int x1 = cx + 95;
        int z0 = cz - 290;
        int z1 = cz - 152;
        b.fill(x0, ground, z0, x1, ground, z1, p.ground());
        outline(b, x0, z0, x1, z1, ground, p.foundation());
        b.walls(x0, ground + 1, z0, x1, ground + 2, z1, p.wall());
        b.fill(cx - 5, ground + 1, z1, cx + 5, ground + 2, z1, AIR);
        b.put(cx - 6, ground + 1, z1, p.accent());
        b.put(cx - 6, ground + 2, z1, p.accent());
        b.put(cx + 6, ground + 1, z1, p.accent());
        b.put(cx + 6, ground + 2, z1, p.accent());
        b.put(cx - 4, ground + 3, z1, p.light());
        b.put(cx + 4, ground + 3, z1, p.light());

        // North gate to the mountain path.
        b.fill(cx - 5, ground + 1, z0, cx + 5, ground + 2, z0, AIR);
        b.put(cx - 6, ground + 1, z0, p.accent());
        b.put(cx - 6, ground + 2, z0, p.accent());
        b.put(cx + 6, ground + 1, z0, p.accent());
        b.put(cx + 6, ground + 2, z0, p.accent());
        b.put(cx - 4, ground + 3, z0, p.light());
        b.put(cx + 4, ground + 3, z0, p.light());

        int[] oaks = {-80, -40, 40, 80};
        for (int x : oaks) {
            oak(b, cx + x, cz - 160, ground);
        }
        oak(b, cx - 80, cz - 175, ground);
        oak(b, cx - 40, cz - 175, ground);
        oak(b, cx + 60, cz - 175, ground);
        oak(b, cx + 80, cz - 175, ground);
        oak(b, cx - 40, cz - 280, ground);
        oak(b, cx + 40, cz - 280, ground);

        signPost(b, cx + 14, cz - 158, ground, p, Direction.SOUTH, "WERF");
        signPost(b, cx - 14, cz - 158, ground, p, Direction.SOUTH, "CAPE MANOR");
        signPost(b, cx + 22, cz - 156, ground, p, Direction.SOUTH, "STEWARD");
    }

    private static void slaveLodge(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx - 76, cz - 266);
        int x0 = cx - 90;
        int x1 = cx - 62;
        int z0 = cz - 280;
        int z1 = cz - 252;
        b.room(x0, ground, z0, x1, ground + 5, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 5, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 5, 4, 0);
        door(b, p, cx - 76, ground + 1, z1, Direction.SOUTH);
        for (int x = x0 + 4; x <= x1 - 4; x += 6) {
            b.window(x, ground + 2, z1, 2, 1, true);
            b.put(x, ground + 2, z1, p.frame());
        }
        sign(b, p, cx - 76, ground + 4, z1, Direction.SOUTH, "SLAVE LODGE");
        sign(b, p, cx - 70, ground + 4, z1, Direction.SOUTH, "COMPANY RECORDS");
        b.fill(cx - 94, ground + 1, cz - 278, cx - 94, ground + 2, cz - 254, p.rail());
        b.put(cx - 94, ground + 1, cz - 278, p.accent());
        b.put(cx - 94, ground + 1, cz - 254, p.accent());
    }

    // ------------------------------------------------------------------ secret wine cellar

    private static void wineCellar(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx + 40, cz - 190);
        int x0 = cx + 20;
        int x1 = cx + 58;
        int z0 = cz - 204;
        int z1 = cz - 184;
        b.room(x0, ground - 5, z0, x1, ground - 1, z1);
        b.walls(x0, ground - 4, z0, x1, ground - 2, z1, p.foundation());
        b.fill(x0, ground - 5, z0, x1, ground - 5, z1, p.foundation());
        b.fill(x0 + 1, ground - 1, z0 + 1, x1 - 1, ground - 1, z1 - 1, p.roofSlab());
        b.fill(cx + 40, ground - 5, z1, cx + 40, ground - 4, z1, AIR);
        door(b, p, cx + 40, ground - 5, z1, Direction.SOUTH);

        // A short half-step stair down from the werf.
        for (int i = 0; i <= 9; i++) {
            int z = cz - 174 - i;
            int floor = Math.max(ground - 5, ground - i);
            b.fill(cx + 38, ground - 6, z, cx + 42, floor - 1, z, p.foundation());
            for (int x = cx + 38; x <= cx + 42; x++) {
                b.put(x, floor, z, p.roofSlab());
            }
            b.fill(cx + 38, floor + 1, z, cx + 42, ground + 1, z, AIR);
            b.fill(cx + 37, ground - 6, z, cx + 37, ground + 1, z, p.foundation());
            b.fill(cx + 43, ground - 6, z, cx + 43, ground + 1, z, p.foundation());
        }
        b.put(cx + 37, ground + 2, cz - 174, p.accent());
        b.put(cx + 43, ground + 2, cz - 174, p.accent());
        b.put(cx + 37, ground + 3, cz - 174, p.light());
        b.put(cx + 43, ground + 3, cz - 174, p.light());

        // Barrels, two chests and the cellar's own warning.
        for (int i = 0; i < 7; i++) {
            int bx = x0 + 4 + i * 4;
            b.put(bx, ground - 4, z0 + 2, BARREL);
            if (bx != cx + 40) {
                b.put(bx, ground - 4, z1 - 2, BARREL);
            }
        }
        b.chest(x0 + 5, ground - 4, z0 + 5, SiteLoot.CULT);
        b.chest(x1 - 5, ground - 4, z1 - 5, SiteLoot.RELIC);
        sign(b, p, x1, ground - 3, cz - 194, Direction.WEST,
                "WINE CELLAR", "COMPANY RESERVE", "DO NOT TELL");
        b.put(cx + 44, ground - 4, z0 + 11, p.light());
        signPost(b, cx + 46, cz - 172, ground, p, Direction.SOUTH,
                "WINE CELLAR", "MIND THE STEPS");
    }

    // ------------------------------------------------------------------ harbour

    private static void harbour(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx + 294, cz);
        // Table Bay: water, sand, and a stone quay wall.
        b.fill(cx + 302, ground - 5, cz - 244, cx + 336, ground - 5, cz + 184,
                Blocks.SAND.defaultBlockState());
        b.fill(cx + 302, ground - 4, cz - 244, cx + 336, ground - 1, cz + 184, WATER);
        b.fill(cx + 300, ground - 5, cz - 244, cx + 301, ground + 1, cz + 184,
                p.foundation());
        b.fill(cx + 300, ground + 2, cz - 244, cx + 300, ground + 2, cz + 184,
                p.weathered());
        for (int z = cz - 240; z <= cz + 180; z += 8) {
            b.put(cx + 300, ground + 3, z, p.accent());
        }
        for (int z = cz - 232; z <= cz + 176; z += 32) {
            b.put(cx + 300, ground + 4, z, p.light());
        }

        ship(b, cx + 318, cz - 120, ground, p, false);
        ship(b, cx + 320, cz + 90, ground, p, false);
        ship(b, cx + 316, cz + 158, ground, p, true);

        int[] cranes = {-180, -60, 60, 140};
        for (int z : cranes) {
            crane(b, cx + 294, cz + z, ground, p);
        }

        warehouse(b, cx + 164, cz - 230, cx + 204, cz - 196, s, p, "VOC 1", 0);
        warehouse(b, cx + 212, cz - 230, cx + 252, cz - 196, s, p, "VOC 2", 1);
        warehouse(b, cx + 164, cz - 170, cx + 204, cz - 136, s, p, "VOC 3", 2);
        warehouse(b, cx + 212, cz - 170, cx + 252, cz - 136, s, p, "VOC 4", 0);
        warehouse(b, cx + 164, cz + 100, cx + 204, cz + 134, s, p, "VOC 5", 1);
        warehouse(b, cx + 212, cz + 100, cx + 252, cz + 134, s, p, "VOC 6", 2);
        warehouse(b, cx + 164, cz + 148, cx + 204, cz + 182, s, p, "VOC 7", 0);
        warehouse(b, cx + 212, cz + 148, cx + 252, cz + 182, s, p, "VOC 8", 1);

        fishMarket(b, cx, cz, s, p);
        questBoard(b, cx, cz, s, p);
        signPost(b, cx + 150, cz + 10, s.at(cx + 150, cz + 10), p, Direction.SOUTH,
                "HARBOUR", "OLD QUAY ->");
        signPost(b, cx + 220, cz + 82, s.at(cx + 220, cz + 82), p, Direction.SOUTH,
                "FISH MARKET");
        signPost(b, cx + 280, cz + 20, s.at(cx + 280, cz + 20), p, Direction.SOUTH, "OLD QUAY");

        for (int z = -240; z <= 180; z += 40) {
            if (z == 0) {
                continue;
            }
            lanternPost(b, cx + 288, cz + z, s.at(cx + 288, cz + z), p);
        }
        for (int i = 0; i < 10; i++) {
            int x = cx + 256;
            int z = cz - 232 + i * 18;
            crate(b, x, z, s.at(x, z), p, 1 + (i & 1));
        }
    }

    private static void warehouse(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  CitySurface s, Palette p, String label, int variant) {
        int ground = s.at((x0 + x1) / 2, (z0 + z1) / 2);
        b.room(x0, ground, z0, x1, ground + 10, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 10, p);
        int ridge = x1 - x0 >= z1 - z0 ? 0 : 1;
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
        sign(b, p, dx, ground + 6, dz, facing, label, "CARGO");
        b.fill(x0 + 3, ground + 1, z0 + 3, x0 + 8, ground + 3, z0 + 6, p.foundation());
        b.put(x0 + 4, ground + 1, z1 - 4, BARREL);
        b.put(x0 + 6, ground + 1, z1 - 4, BARREL);
        for (int x = x0 + 4; x <= x1 - 4; x += 6) {
            b.window(x, ground + 8, z0, 2, 1, true);
            b.window(x, ground + 8, z1, 2, 1, true);
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

    private static void ship(StructureBuilder b, int x, int z, int ground, Palette p,
                             boolean wreck) {
        int hx0 = x - 8;
        int hx1 = x + 8;
        int hz0 = z - 18;
        int hz1 = z + 18;
        b.fill(hx0, ground - 3, hz0, hx1, ground, hz1, p.weathered());
        b.fill(hx0 + 1, ground - 2, hz0 + 1, hx1 - 1, ground, hz1 - 1, AIR);
        b.fill(hx0 + 1, ground - 1, hz0 + 1, hx1 - 1, ground - 1, hz1 - 1, p.foundation());
        b.fill(hx0 + 1, ground, hz0 + 1, hx1 - 1, ground, hz1 - 1, p.roofSlab());
        b.fill(hx0, ground + 1, hz0, hx1, ground + 1, hz0 + 1, p.rail());
        b.fill(hx0, ground + 1, hz1 - 1, hx1, ground + 1, hz1, p.rail());
        b.fill(hx0, ground + 1, hz0, hx0 + 1, ground + 1, hz1, p.rail());
        b.fill(hx1 - 1, ground + 1, hz0, hx1, ground + 1, hz1, p.rail());
        int mast = wreck ? ground + 10 : ground + 18;
        b.fill(x, ground + 1, z, x, mast, z, p.accent());
        if (!wreck) {
            b.fill(x - 6, mast - 6, z, x + 6, mast - 2, z,
                    Materials.wool(DyeColor.WHITE));
            b.fill(x - 5, mast - 12, z, x + 5, mast - 8, z,
                    Materials.wool(DyeColor.RED));
            b.put(x, mast + 1, z, p.light());
        } else {
            b.fill(x - 5, ground + 2, z - 8, x + 5, ground + 5, z - 4, AIR);
            b.fill(x - 3, ground + 1, z + 6, x + 3, ground + 3, z + 10, p.rubble());
            b.put(x - 4, ground + 1, z, BARREL);
        }
    }

    private static void fishMarket(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx + 220, cz + 44);
        int x0 = cx + 200;
        int x1 = cx + 248;
        int z0 = cz + 24;
        int z1 = cz + 64;
        pave(b, x0, z0, x1, z1, ground, p.ground());
        outline(b, x0, z0, x1, z1, ground, p.foundation());
        for (int i = 0; i < 3; i++) {
            int sx = x0 + 8 + i * 16;
            int sz = z0 + 8;
            b.fill(sx, ground + 1, sz, sx, ground + 2, sz, p.accent());
            b.fill(sx + 5, ground + 1, sz, sx + 5, ground + 2, sz, p.accent());
            b.fill(sx, ground + 1, sz + 4, sx, ground + 2, sz + 4, p.accent());
            b.fill(sx + 5, ground + 1, sz + 4, sx + 5, ground + 2, sz + 4, p.accent());
            b.fill(sx, ground + 3, sz, sx + 5, ground + 3, sz + 4,
                    Materials.wool(DyeColor.LIGHT_BLUE));
            b.fill(sx + 1, ground + 1, sz + 2, sx + 4, ground + 1, sz + 2, p.foundation());
            b.put(sx + 2, ground + 2, sz + 2, BARREL);
            b.put(sx + 3, ground + 2, sz + 2, BARREL);
        }
        b.put(x0 + 2, ground + 1, z1 - 2, BARREL);
        b.put(x0 + 4, ground + 1, z1 - 2, BARREL);
        b.put(x1 - 2, ground + 1, z1 - 2, BARREL);
        b.put(x0 + 4, ground + 1, z0 + 2, p.light());
        b.put(x1 - 4, ground + 1, z0 + 2, p.light());
        b.fill(cx + 220, ground + 1, z1, cx + 220, ground + 4, z1, p.accent());
        sign(b, p, cx + 220, ground + 5, z1, Direction.SOUTH, "FISH MARKET");
    }

    private static void questBoard(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx + 220, cz + 11);
        int x0 = cx + 208;
        int x1 = cx + 232;
        int z = cz + 10;
        b.fill(x0, ground + 1, z, x0, ground + 4, z, p.accent());
        b.fill(x1, ground + 1, z, x1, ground + 4, z, p.accent());
        b.fill(x0, ground + 5, z - 1, x1, ground + 5, z + 1, p.roofSlab());
        b.fill(x0 + 1, ground + 2, z + 1, x1 - 1, ground + 4, z + 1, p.foundation());
        sign(b, p, cx + 214, ground + 4, z + 1, Direction.SOUTH, "QUEST NOTICES");
        sign(b, p, cx + 220, ground + 4, z + 1, Direction.SOUTH, "MOUNTAINEER");
        sign(b, p, cx + 226, ground + 4, z + 1, Direction.SOUTH, "SEALED LETTER");
        b.put(x0 + 2, ground + 3, z, p.light());
        b.put(x1 - 2, ground + 3, z, p.light());
    }

    // ------------------------------------------------------------------ Bo-Kaap

    private static void boKaap(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        shop(b, cx, cz, s, p);
        herbalist(b, cx, cz, s, p);
        mosque(b, cx, cz, s, p);

        int[] houseX = {-292, -276, -253, -236, -212, -196, -172, -156};
        int[] houseZ = {-190, -132, -70, 10, 52, 112};
        Direction[] faces = {Direction.EAST, Direction.WEST, Direction.WEST, Direction.EAST,
                Direction.WEST, Direction.EAST, Direction.WEST, Direction.EAST};
        for (int i = 0; i < houseX.length; i++) {
            for (int j = 0; j < houseZ.length; j++) {
                int x0 = cx + houseX[i];
                int z0 = cz + houseZ[j];
                int x1 = x0 + 11;
                int z1 = z0 + 13;
                if (overlaps(x0, z0, x1, z1, cx - 258, cz - 72, cx - 244, cz - 48)
                        || overlaps(x0, z0, x1, z1, cx - 258, cz + 108, cx - 244, cz + 132)
                        || overlaps(x0, z0, x1, z1, cx - 294, cz + 50, cx - 264, cz + 82)) {
                    continue;
                }
                paintedHouse(b, x0, z0, s, p, i * 3 + j, faces[i]);
            }
        }

        // Lane lamps and a small neighbourhood oven.
        int[] lampZ = {-220, -160, -100, -40, 30, 80, 140};
        for (int z : lampZ) {
            lanternPost(b, cx - 246, cz + z, s.at(cx - 246, cz + z), p);
            lanternPost(b, cx - 234, cz + z, s.at(cx - 234, cz + z), p);
        }
        int ovenGround = s.at(cx - 248, cz + 94);
        b.fill(cx - 250, ovenGround + 1, cz + 92, cx - 246, ovenGround + 3, cz + 96,
                Materials.concrete(DyeColor.RED));
        b.put(cx - 248, ovenGround + 4, cz + 94, p.light());
        signPost(b, cx - 160, cz + 8, s.at(cx - 160, cz + 8), p, Direction.SOUTH,
                "BO-KAAP", "PAINTED SLOPES");
        signPost(b, cx - 246, cz + 150, s.at(cx - 246, cz + 150), p, Direction.SOUTH,
                "BO-KAAP");
    }

    private static void paintedHouse(StructureBuilder b, int x0, int z0, CitySurface s,
                                     Palette p, int colour, Direction facing) {
        int ground = s.at(x0, z0);
        int x1 = x0 + 11;
        int z1 = z0 + 13;
        int height = 6 + Math.floorMod(colour, 3);
        b.room(x0, ground, z0, x1, ground + height, z1);
        b.walls(x0, ground + 1, z0, x1, ground + height - 1, z1, paint(colour));
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + height - 1, p);
        b.fill(x0 - 1, ground + height, z0 - 1, x1 + 1, ground + height, z1 + 1,
                p.roofSlab());
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + height + 1);

        int dx;
        int dz;
        switch (facing) {
            case EAST -> {
                dx = x1;
                dz = (z0 + z1) / 2;
            }
            case WEST -> {
                dx = x0;
                dz = (z0 + z1) / 2;
            }
            case SOUTH -> {
                dx = (x0 + x1) / 2;
                dz = z1;
            }
            default -> {
                dx = (x0 + x1) / 2;
                dz = z0;
            }
        }
        door(b, p, dx, ground + 1, dz, facing);
        b.fill(dx - 1, ground + 4, dz, dx + 1, ground + 4, dz, p.roofSlab());

        int wy = ground + 3;
        if (facing == Direction.EAST || facing == Direction.WEST) {
            b.window(dx, wy, dz - 4, 2, 1, true);
            b.window(dx, wy, dz + 4, 2, 1, true);
            b.put(dx, wy, dz - 5, p.accent());
            b.put(dx, wy + 1, dz - 5, p.accent());
            b.put(dx, wy, dz + 5, p.accent());
            b.put(dx, wy + 1, dz + 5, p.accent());
        } else {
            b.window(dx - 4, wy, dz, 2, 2, true);
            b.window(dx + 2, wy, dz, 2, 2, true);
            b.put(dx - 5, wy, dz, p.accent());
            b.put(dx - 5, wy + 1, dz, p.accent());
            b.put(dx + 4, wy, dz, p.accent());
            b.put(dx + 4, wy + 1, dz, p.accent());
        }
        b.put(x0 + 2, ground + height, z0 + 2, p.accent());
        b.put(x0 + 2, ground + height + 1, z0 + 2, p.accent());
        b.put(x0 + 2, ground + height + 2, z0 + 2, p.light());
    }

    private static void shop(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx - 251, cz - 60);
        int x0 = cx - 258;
        int x1 = cx - 244;
        int z0 = cz - 72;
        int z1 = cz - 48;
        b.room(x0, ground, z0, x1, ground + 11, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 11, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 11, 5, 1);
        door(b, p, x1, ground + 1, cz - 60, Direction.EAST);
        b.fill(x1 + 1, ground + 4, cz - 64, x1 + 2, ground + 4, cz - 56,
                Materials.wool(DyeColor.RED));
        b.put(x1 + 1, ground + 5, cz - 60, p.light());
        b.window(x1, ground + 7, cz - 66, 2, 1, true);
        b.window(x1, ground + 7, cz - 54, 2, 1, true);
        sign(b, p, x1, ground + 5, cz - 60, Direction.EAST, "BO-KAAP SHOP", "OPEN");
        b.put(x0 + 1, ground + 1, z0 + 1, BARREL);
        b.put(x0 + 2, ground + 1, z0 + 1, BARREL);
        b.put(x0 + 1, ground + 1, z1 - 1, BARREL);
        b.fill(x0 + 1, ground + 2, z0 + 1, x1 - 1, ground + 2, z0 + 4, p.accent());
        b.put(x0 + 2, ground + 3, z0 + 2, p.light());
    }

    private static void herbalist(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx - 251, cz + 120);
        int x0 = cx - 258;
        int x1 = cx - 244;
        int z0 = cz + 108;
        int z1 = cz + 132;
        b.room(x0, ground, z0, x1, ground + 8, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 8, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 8, 4, 1);
        door(b, p, x1, ground + 1, cz + 120, Direction.EAST);
        sign(b, p, x1, ground + 4, cz + 120, Direction.EAST, "HERBALIST", "REMEDIES");
        b.window(x1, ground + 6, cz + 114, 2, 1, true);
        b.window(x1, ground + 6, cz + 126, 2, 1, true);
        b.put(x0 + 2, ground + 1, z0 + 2, CAULDRON);
        b.put(x0 + 3, ground + 1, z0 + 2, BARREL);
        b.put(x0 + 2, ground + 1, z1 - 2, BARREL);
        b.fill(x0 + 1, ground + 2, z0 + 1, x0 + 4, ground + 2, z1 - 1, p.accent());
        for (int z = z0 + 3; z <= z1 - 3; z += 4) {
            b.put(x0 + 2, ground + 3, z, flower(z));
        }
        b.put(x1 + 1, ground + 1, z0 - 2, p.light());
    }

    private static void mosque(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx - 279, cz + 66);
        int x0 = cx - 292;
        int x1 = cx - 266;
        int z0 = cz + 52;
        int z1 = cz + 80;
        b.room(x0, ground, z0, x1, ground + 9, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 9, p);
        b.fill(x0 - 1, ground + 9, z0 - 1, x1 + 1, ground + 9, z1 + 1, p.roofSlab());
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 10);
        int domeX = cx - 279;
        int domeZ = cz + 66;
        for (int i = 0; i <= 4; i++) {
            int radius = 9 - i * 2;
            b.fill(domeX - radius, ground + 10 + i, domeZ - radius,
                    domeX + radius, ground + 10 + i, domeZ + radius, p.accent());
        }
        b.put(domeX, ground + 15, domeZ, p.accent());
        b.put(domeX, ground + 16, domeZ, p.light());
        b.fill(domeX - 1, ground + 13, domeZ, domeX + 1, ground + 14, domeZ,
                Materials.concrete(DyeColor.GREEN));

        // Minaret.
        int mx = cx - 270;
        int mz = cz + 76;
        b.fill(mx - 2, ground, mz - 2, mx + 2, ground + 1, mz + 2, p.foundation());
        b.room(mx - 2, ground + 1, mz - 2, mx + 2, ground + 18, mz + 2);
        b.fill(mx - 2, ground + 18, mz - 2, mx + 2, ground + 18, mz + 2, p.roofSlab());
        b.crenellations(mx - 3, mz - 3, mx + 3, mz + 3, ground + 19);
        b.spire(mx, mz, ground + 19, 5);
        b.window(mx + 2, ground + 12, mz, 3, 1, true);
        b.window(mx - 2, ground + 12, mz, 3, 1, true);
        b.put(mx, ground + 20, mz, p.light());

        // Courtyard wall and gate on the lane side.
        b.walls(cx - 294, ground + 1, cz + 50, cx - 264, ground + 2, cz + 82, p.wall());
        b.fill(cx - 264, ground + 1, cz + 64, cx - 264, ground + 2, cz + 68, AIR);
        door(b, p, x1, ground + 1, cz + 66, Direction.EAST);
        sign(b, p, x1, ground + 5, cz + 66, Direction.EAST,
                "BO-KAAP MOSQUE", "ALL WELCOME");
        b.put(cx - 264, ground + 3, cz + 62, p.light());
        b.put(cx - 264, ground + 3, cz + 70, p.light());
    }

    // ------------------------------------------------------------------ Company's Garden

    private static void companyGarden(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx, cz + 40);
        int gateGround = s.at(cx, cz - 80);
        // North gate pillars and the garden's formal frame.
        b.fill(cx - 12, gateGround + 1, cz - 82, cx - 10, gateGround + 5, cz - 78, p.wall());
        b.fill(cx + 10, gateGround + 1, cz - 82, cx + 12, gateGround + 5, cz - 78, p.wall());
        b.put(cx - 11, gateGround + 6, cz - 80, p.light());
        b.put(cx + 11, gateGround + 6, cz - 80, p.light());
        b.put(cx - 13, gateGround, cz - 80, p.foundation());
        b.put(cx + 13, gateGround, cz - 80, p.foundation());
        for (int x = -110; x <= 110; x++) {
            b.put(cx + x, s.at(cx + x, cz - 78), cz - 78, p.foundation());
            b.put(cx + x, s.at(cx + x, cz + 130), cz + 130, p.foundation());
        }

        fountain(b, cx, cz, ground, p);
        library(b, cx, cz, ground, p);

        int[][] beds = {
                {-108, 10, -80, 30}, {-108, 50, -80, 70},
                {-48, 10, -16, 30}, {-48, 50, -16, 70},
                {16, 80, 38, 100}, {16, 110, 38, 130},
                {80, 80, 108, 100}, {80, 110, 108, 130}};
        for (int[] bed : beds) {
            parterre(b, cx + bed[0], cz + bed[1], cx + bed[2], cz + bed[3],
                    s.at(cx + bed[0], cz + bed[1]), p);
        }

        int[] avenueZ = {12, 28, 56, 72, 88, 104, 120};
        for (int z : avenueZ) {
            oak(b, cx - 12, cz + z, s.at(cx - 12, cz + z));
            oak(b, cx + 12, cz + z, s.at(cx + 12, cz + z));
        }
        oak(b, cx - 72, cz + 20, s.at(cx - 72, cz + 20));
        oak(b, cx + 86, cz + 20, s.at(cx + 86, cz + 20));
        oak(b, cx - 72, cz + 60, s.at(cx - 72, cz + 60));
        oak(b, cx + 72, cz + 60, s.at(cx + 72, cz + 60));
        oak(b, cx - 72, cz + 120, s.at(cx - 72, cz + 120));
        oak(b, cx + 72, cz + 120, s.at(cx + 72, cz + 120));

        signPost(b, cx + 12, cz + 148, s.at(cx + 12, cz + 148), p, Direction.SOUTH,
                "COMPANY'S", "GARDEN", "EST. 1652");
        signPost(b, cx - 20, cz + 8, s.at(cx - 20, cz + 8), p, Direction.SOUTH, "FOUNTAIN");
        b.put(cx - 4, s.at(cx - 4, cz + 132) + 1, cz + 132, p.light());
        b.put(cx + 4, s.at(cx + 4, cz + 132) + 1, cz + 132, p.light());
    }

    private static void fountain(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int fx = cx;
        int fz = cz + 40;
        b.fill(fx - 7, ground, fz - 6, fx + 7, ground, fz + 6, p.foundation());
        b.fill(fx - 6, ground, fz - 5, fx + 6, ground, fz + 5, WATER);
        b.fill(fx - 7, ground + 1, fz - 6, fx + 7, ground + 1, fz - 6, p.foundation());
        b.fill(fx - 7, ground + 1, fz + 6, fx + 7, ground + 1, fz + 6, p.foundation());
        b.fill(fx - 7, ground + 1, fz - 6, fx - 7, ground + 1, fz + 6, p.foundation());
        b.fill(fx + 7, ground + 1, fz - 6, fx + 7, ground + 1, fz + 6, p.foundation());
        b.fill(fx - 2, ground + 1, fz - 2, fx + 2, ground + 1, fz + 2, p.foundation());
        b.monument(fx, fz, ground + 1);
        b.put(fx - 5, ground + 1, fz - 4, p.light());
        b.put(fx + 5, ground + 1, fz - 4, p.light());
        b.put(fx - 5, ground + 1, fz + 4, p.light());
        b.put(fx + 5, ground + 1, fz + 4, p.light());
    }

    private static void parterre(StructureBuilder b, int x0, int z0, int x1, int z1,
                                 int ground, Palette p) {
        b.fill(x0, ground, z0, x1, ground, z1, p.weathered());
        b.fill(x0 + 1, ground, z0 + 1, x1 - 1, ground, z1 - 1, DIRT);
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 1, z0 + 1, OAK_LEAVES);
        b.fill(x0 + 1, ground + 1, z1 - 1, x1 - 1, ground + 1, z1 - 1, OAK_LEAVES);
        b.fill(x0 + 1, ground + 1, z0 + 1, x0 + 1, ground + 1, z1 - 1, OAK_LEAVES);
        b.fill(x1 - 1, ground + 1, z0 + 1, x1 - 1, ground + 1, z1 - 1, OAK_LEAVES);
        for (int x = x0 + 3; x <= x1 - 3; x += 4) {
            for (int z = z0 + 3; z <= z1 - 3; z += 4) {
                b.put(x, ground + 1, z, flower(x + z));
            }
        }
        b.put(x0 + 2, ground + 1, z0 + 2, p.light());
        b.put(x1 - 2, ground + 1, z1 - 2, p.light());
    }

    private static void library(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 40;
        int x1 = cx + 76;
        int z0 = cz + 8;
        int z1 = cz + 32;
        b.room(x0, ground, z0, x1, ground + 11, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 11, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 11, 6, 0);
        for (int z = z0 + 2; z <= z1 - 2; z += 4) {
            b.fill(x0 - 1, ground + 1, z, x0 - 1, ground + 8, z, p.wall());
            b.put(x0 - 1, ground, z, p.foundation());
        }
        b.fill(x0 - 2, ground + 9, z0, x0, ground + 9, z1, p.roofSlab());
        door(b, p, x0, ground + 1, cz + 20, Direction.WEST);
        sign(b, p, x0, ground + 6, cz + 24, Direction.WEST, "LIBRARY", "COMPANY BOOKS");
        for (int z = z0 + 5; z <= z1 - 5; z += 6) {
            b.window(x1, ground + 4, z, 3, 1, true);
        }
        b.window(x1, ground + 8, cz + 20, 2, 2, true);
        b.fill(x0 + 2, ground + 1, z0 + 2, x0 + 4, ground + 3, z0 + 5, BOOKSHELF);
        b.fill(x0 + 2, ground + 1, z1 - 5, x0 + 4, ground + 3, z1 - 2, BOOKSHELF);
        b.fill(x1 - 4, ground + 1, z0 + 2, x1 - 2, ground + 3, z0 + 5, BOOKSHELF);
        b.put(x0 + 6, ground + 1, cz + 20, p.accent());
        b.put(x0 + 6, ground + 2, cz + 20, p.light());
    }

    // ------------------------------------------------------------------ District Six

    private static void districtSix(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx, cz + 240);
        int[] modules = {-310, -265, -215, -170, -110, -65, 20, 65, 130, 175, 230, 275};
        for (int x : modules) {
            terraceRow(b, cx + x, cz + 204, s, p, Direction.SOUTH);
            terraceRow(b, cx + x, cz + 284, s, p, Direction.NORTH);
        }
        church(b, cx, cz, ground, p);
        sixMarket(b, cx, cz, ground, p);
        ruin(b, cx, cz, ground, p);

        // Allotments in the gap between the lanes.
        int[][] plots = {
                {-300, 268, -270, 280}, {-200, 268, -170, 280},
                {-100, 268, -70, 280}, {30, 268, 60, 280},
                {90, 268, 110, 280}, {150, 268, 180, 280},
                {240, 268, 270, 280}};
        for (int[] plot : plots) {
            parterre(b, cx + plot[0], cz + plot[1], cx + plot[2], cz + plot[3],
                    s.at(cx + plot[0], cz + plot[1]), p);
        }

        signPost(b, cx + 12, cz + 188, s.at(cx + 12, cz + 188), p, Direction.SOUTH,
                "DISTRICT SIX", "SIX MARKET ->");
        signPost(b, cx - 12, cz + 264, s.at(cx - 12, cz + 264), p, Direction.NORTH,
                "DISTRICT SIX");
        b.put(cx - 4, s.at(cx - 4, cz + 180) + 1, cz + 180, p.light());
        b.put(cx + 4, s.at(cx + 4, cz + 180) + 1, cz + 180, p.light());
    }

    private static void terraceRow(StructureBuilder b, int x0, int z0, CitySurface s, Palette p,
                                   Direction facing) {
        int ground = s.at(x0, z0);
        int x1 = x0 + 35;
        int z1 = z0 + 11;
        b.room(x0, ground, z0, x1, ground + 7, z1);
        b.walls(x0, ground + 1, z0, x1, ground + 6, z1, BRICK);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 7, 4, 0);
        int face = facing == Direction.SOUTH ? z1 : z0;
        door(b, p, x0 + 9, ground + 1, face, facing);
        door(b, p, x0 + 27, ground + 1, face, facing);
        b.fill(x0 + 7, ground, face, x0 + 11, ground, face, p.foundation());
        b.fill(x0 + 25, ground, face, x0 + 29, ground, face, p.foundation());
        for (int x = x0 + 4; x <= x1 - 4; x += 6) {
            b.window(x, ground + 3, face, 2, 2, true);
            b.put(x - 1, ground + 3, face, p.accent());
            b.put(x - 1, ground + 4, face, p.accent());
        }
        b.window(x0 + 18, ground + 5, face, 2, 2, true);
        b.put(x0 + 2, ground + 11, z0 + 5, p.accent());
        b.put(x0 + 2, ground + 12, z0 + 5, p.accent());
        b.put(x0 + 33, ground + 11, z0 + 5, p.accent());
        b.put(x0 + 33, ground + 12, z0 + 5, p.accent());
        b.put(x0 + 4, ground + 1, z0 + 2, BARREL);
        b.put(x0 + 31, ground + 1, z1 - 2, BARREL);
    }

    private static void church(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 70;
        int x1 = cx - 30;
        int z0 = cz + 228;
        int z1 = cz + 252;
        b.room(x0, ground, z0, x1, ground + 13, z1);
        b.walls(x0, ground + 1, z0, x1, ground + 12, z1, BRICK);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 12, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 13, 7, 0);
        door(b, p, cx - 50, ground + 1, z1, Direction.SOUTH);
        b.window(cx - 60, ground + 4, z1, 3, 2, true);
        b.window(cx - 40, ground + 4, z1, 3, 2, true);
        b.window(cx - 60, ground + 9, z1, 2, 2, true);
        b.window(cx - 40, ground + 9, z1, 2, 2, true);
        sign(b, p, cx - 50, ground + 7, z1, Direction.SOUTH, "SIX CHURCH");

        // Steeple at the north end.
        int sx = cx - 50;
        int sz = cz + 232;
        b.room(sx - 4, ground, sz - 4, sx + 4, ground + 24, sz + 4);
        b.fill(sx - 4, ground, sz - 4, sx + 4, ground, sz + 4, p.foundation());
        quoins(b, sx - 4, sz - 4, sx + 4, sz + 4, ground + 1, ground + 24, p);
        b.fill(sx - 4, ground + 24, sz - 4, sx + 4, ground + 24, sz + 4, p.roofSlab());
        b.crenellations(sx - 5, sz - 5, sx + 5, sz + 5, ground + 25);
        b.spire(sx, sz, ground + 25, 7);
        b.window(sx, ground + 10, sz + 4, 4, 2, true);
        b.window(sx, ground + 17, sz + 4, 3, 2, true);
        b.put(sx, ground + 23, sz, p.light());

        // Small graveyard to the west.
        b.walls(cx - 84, ground + 1, cz + 230, cx - 74, ground + 2, cz + 250, p.weathered());
        for (int z = cz + 234; z <= cz + 246; z += 4) {
            b.put(cx - 80, ground + 1, z, p.weathered());
            b.put(cx - 80, ground + 2, z, p.weathered());
            b.put(cx - 78, ground + 1, z + 2, p.foundation());
            b.put(cx - 78, ground + 2, z + 2, p.foundation());
        }
        signPost(b, cx - 58, cz + 254, ground, p, Direction.SOUTH, "SIX CHURCH");
    }

    private static void sixMarket(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 40;
        int x1 = cx + 100;
        int z0 = cz + 228;
        int z1 = cz + 252;
        pave(b, x0, z0, x1, z1, ground, p.ground());
        outline(b, x0, z0, x1, z1, ground, BRICK);
        for (int i = 0; i < 3; i++) {
            int sx = x0 + 8 + i * 20;
            int sz = z0 + 8;
            b.fill(sx, ground + 1, sz, sx, ground + 2, sz, p.accent());
            b.fill(sx + 6, ground + 1, sz, sx + 6, ground + 2, sz, p.accent());
            b.fill(sx, ground + 1, sz + 4, sx, ground + 2, sz + 4, p.accent());
            b.fill(sx + 6, ground + 1, sz + 4, sx + 6, ground + 2, sz + 4, p.accent());
            b.fill(sx, ground + 3, sz, sx + 6, ground + 3, sz + 4,
                    Materials.wool(DyeColor.ORANGE));
            b.fill(sx + 1, ground + 1, sz + 2, sx + 5, ground + 1, sz + 2, p.foundation());
            b.put(sx + 2, ground + 2, sz + 2, BARREL);
            b.put(sx + 4, ground + 2, sz + 2, BARREL);
        }
        b.fill(x0 + 2, ground + 1, z1 - 2, x0 + 5, ground + 2, z1 - 2, p.foundation());
        b.put(x0 + 3, ground + 1, z1 - 3, BARREL);
        b.put(x1 - 3, ground + 1, z1 - 3, BARREL);
        b.put(x0 + 2, ground + 1, z0 + 2, p.light());
        b.put(x1 - 2, ground + 1, z0 + 2, p.light());
        signPost(b, cx + 70, cz + 254, ground, p, Direction.SOUTH,
                "SIX MARKET", "FRESH PRODUCE");
    }

    private static void ruin(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 150;
        int x1 = cx + 190;
        int z0 = cz + 228;
        int z1 = cz + 248;
        b.fill(x0, ground, z0, x1, ground, z1, p.rubble());
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                if (!edge) {
                    continue;
                }
                for (int y = ground + 1; y <= ground + 5; y++) {
                    if (Math.floorMod(x * 3 + z * 5 + y, 7) == 0) {
                        continue;
                    }
                    b.put(x, y, z, y == ground + 1 ? p.foundation() : BRICK);
                }
            }
        }
        b.put(x0 + 1, ground + 1, z0 + 1, Palette.cobweb());
        b.put(x1 - 1, ground + 1, z1 - 1, Palette.cobweb());
        b.put(x0 + 1, ground + 2, z1 - 1, Palette.cobweb());
        b.put(x0 + 10, ground + 1, z0 + 10, p.rubble());
        b.put(x1 - 10, ground + 1, z1 - 10, p.rubble());
        signPost(b, cx + 150, cz + 250, ground, p, Direction.SOUTH,
                "NO ENTRY", "UNSAFE");
    }

    // ------------------------------------------------------------------ rail station

    private static void railStation(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int trackY = s.at(cx + 258, cz + 321);
        int x0 = cx + 180;
        int x1 = cx + 212;
        int z0 = cz + 306;
        int z1 = cz + 316;

        // A straight, level track bed running east to the district edge.
        b.fill(cx + 180, trackY, cz + 320, cx + 336, trackY, cz + 322, p.foundation());
        b.fill(cx + 180, trackY, cz + 321, cx + 336, trackY, cz + 321, p.rubble());
        BlockState rail = Blocks.RAIL.defaultBlockState()
                .setValue(BlockStateProperties.RAIL_SHAPE, RailShape.EAST_WEST);
        for (int x = cx + 180; x <= cx + 336; x++) {
            b.put(x, trackY + 1, cz + 321, rail);
        }

        // Raised platform along the house.
        b.fill(cx + 178, trackY + 1, cz + 317, cx + 222, trackY + 1, cz + 319,
                p.foundation());
        b.fill(cx + 178, trackY, cz + 317, cx + 222, trackY, cz + 319, p.foundation());
        b.put(cx + 178, trackY + 2, cz + 318, p.light());
        b.put(cx + 222, trackY + 2, cz + 318, p.light());

        // The main passenger platform on the harbour side: five wide, its edge three blocks off
        // the centreline, with five clear blocks and a canopy at rail+6.
        b.fill(cx + 178, trackY, cz + 324, cx + 238, trackY, cz + 328, p.foundation());
        b.fill(cx + 178, trackY + 1, cz + 324, cx + 238, trackY + 1, cz + 328, p.accent());
        for (int x = cx + 182; x <= cx + 234; x += 10) {
            b.fill(x, trackY + 2, cz + 328, x, trackY + 6, cz + 328, p.accent());
            b.put(x, trackY + 7, cz + 326, p.light());
        }
        b.fill(cx + 176, trackY + 7, cz + 323, cx + 240, trackY + 7, cz + 329, p.roofSlab());

        // Station house.
        b.room(x0, trackY + 1, z0, x1, trackY + 7, z1);
        b.fill(x0, trackY + 1, z0, x1, trackY + 1, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, trackY + 2, trackY + 6, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, trackY + 7, 4, 0);
        door(b, p, cx + 196, trackY + 1, z1, Direction.SOUTH);
        sign(b, p, cx + 196, trackY + 5, z1, Direction.SOUTH, "RAIL STATION", "CAPE TOWN");
        b.window(x0 + 5, trackY + 3, z1, 3, 1, true);
        b.window(x1 - 6, trackY + 3, z1, 3, 1, true);
        b.put(x0 + 2, trackY + 2, z0 + 2, BARREL);
        b.put(x1 - 2, trackY + 2, z0 + 2, BARREL);
        signPost(b, cx + 196, cz + 324, s.at(cx + 196, cz + 324), p, Direction.SOUTH,
                "RAIL STATION");
    }

    // ------------------------------------------------------------------ mountain foot

    private static void mountainFoot(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx, cz - 314);
        pad(b, cx - 24, cz - 326, cx + 24, cz - 300, ground, p);
        signalStation(b, cx, cz, ground, p);

        path(b, s, cx, cz - 304, cx - 120, cz - 332, 3, p.rubble());
        path(b, s, cx, cz - 304, cx + 120, cz - 332, 3, p.rubble());
        path(b, s, cx - 120, cz - 332, cx - 180, cz - 310, 3, p.rubble());
        path(b, s, cx - 180, cz - 310, cx - 240, cz - 334, 3, p.rubble());
        path(b, s, cx + 120, cz - 332, cx + 180, cz - 316, 3, p.rubble());
        path(b, s, cx + 180, cz - 316, cx + 240, cz - 334, 3, p.rubble());

        cairn(b, cx + 80, cz - 330, s, p);
        cairn(b, cx - 120, cz - 332, s, p);
        cairn(b, cx - 240, cz - 334, s, p);
        cairn(b, cx + 240, cz - 334, s, p);

        // The missing mountaineer's last camp.
        int lx = cx + 100;
        int lz = cz - 332;
        int campGround = s.at(lx, lz);
        b.put(lx, campGround + 1, lz, BARREL);
        b.put(lx + 1, campGround + 1, lz, Materials.wool(DyeColor.RED));
        b.put(lx + 2, campGround + 1, lz, p.rail());
        b.put(lx + 1, campGround + 2, lz, p.light());
        signPost(b, lx + 6, lz - 4, s.at(lx + 6, lz - 4), p, Direction.SOUTH, "LAST CAMP");

        // A ruined shepherd's hut and a scatter of pines.
        int hx = cx - 180;
        int hz = cz - 330;
        int hutGround = s.at(hx, hz);
        b.fill(hx - 10, hutGround, hz - 8, hx + 10, hutGround, hz + 8, p.rubble());
        for (int x = hx - 10; x <= hx + 10; x++) {
            for (int z = hz - 8; z <= hz + 8; z++) {
                boolean edge = x == hx - 10 || x == hx + 10 || z == hz - 8 || z == hz + 8;
                if (!edge) {
                    continue;
                }
                for (int y = hutGround + 1; y <= hutGround + 4; y++) {
                    if (Math.floorMod(x * 7 + z * 3 + y, 5) == 0) {
                        continue;
                    }
                    b.put(x, y, z, p.foundation());
                }
            }
        }
        b.put(hx - 9, hutGround + 1, hz - 7, Palette.cobweb());
        b.put(hx + 9, hutGround + 1, hz + 7, Palette.cobweb());
        signPost(b, hx, hz + 10, s.at(hx, hz + 10), p, Direction.SOUTH, "OLD HUT");

        int[] pines = {-300, -270, -60, 30, 60, 130, 230, 280};
        for (int x : pines) {
            pine(b, cx + x, cz - 324, s.at(cx + x, cz - 324));
            pine(b, cx + x + 8, cz - 336, s.at(cx + x + 8, cz - 336));
        }
        signPost(b, cx + 8, cz - 292, s.at(cx + 8, cz - 292), p, Direction.SOUTH,
                "MOUNTAIN PATH", "MIST: TURN BACK");
    }

    private static void signalStation(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 22;
        int x1 = cx + 22;
        int z0 = cz - 324;
        int z1 = cz - 304;
        b.room(x0, ground, z0, x1, ground + 9, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 9, p);
        b.fill(x0 - 1, ground + 9, z0 - 1, x1 + 1, ground + 9, z1 + 1, p.roofSlab());
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 10);
        door(b, p, cx, ground + 1, z1, Direction.SOUTH);
        b.window(x0 + 5, ground + 4, z1, 3, 1, true);
        b.window(x1 - 6, ground + 4, z1, 3, 1, true);
        sign(b, p, cx, ground + 6, z1, Direction.SOUTH, "SIGNAL STATION");
        sign(b, p, cx + 8, ground + 6, z1, Direction.SOUTH, "MISSING CLIMBER");
        b.put(cx - 2, ground + 1, z0 + 3, BARREL);
        b.put(cx + 2, ground + 1, z0 + 3, BARREL);

        int mast = ground + 20;
        b.fill(cx, ground + 10, cz - 314, cx, mast, cz - 314, p.accent());
        b.fill(cx, mast - 4, cz - 314, cx + 6, mast - 3, cz - 314,
                Materials.wool(DyeColor.RED));
        b.fill(cx, mast - 8, cz - 314, cx + 6, mast - 7, cz - 314,
                Materials.wool(DyeColor.WHITE));
        b.put(cx, mast + 1, cz - 314, p.light());
        b.put(cx - 3, ground + 1, cz - 306, p.light());
        b.put(cx + 3, ground + 1, cz - 306, p.light());
    }

    private static void cairn(StructureBuilder b, int x, int z, CitySurface s, Palette p) {
        int ground = s.at(x, z);
        b.fill(x - 2, ground, z - 2, x + 2, ground, z + 2, p.foundation());
        b.fill(x - 1, ground + 1, z - 1, x + 1, ground + 1, z + 1, p.weathered());
        b.put(x, ground + 2, z, p.weathered());
        b.put(x, ground + 3, z, p.foundation());
        b.put(x, ground + 4, z, p.accent());
    }

    // ------------------------------------------------------------------ hotel

    private static void hotel(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int ground = s.at(cx - 80, cz - 108);
        int x0 = cx - 98;
        int x1 = cx - 62;
        int z0 = cz - 136;
        int z1 = cz - 112;
        b.room(x0, ground, z0, x1, ground + 10, z1);
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        quoins(b, x0, z0, x1, z1, ground + 1, ground + 10, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 10, 7, 0);
        b.fill(x0 - 2, ground, z1, x1 + 2, ground, z1 + 4, p.foundation());
        b.fill(x0 - 2, ground + 1, z1 + 4, x1 + 2, ground + 1, z1 + 4, p.rail());
        b.fill(cx - 3, ground + 1, z1 + 4, cx + 3, ground + 1, z1 + 4, AIR);
        door(b, p, cx - 80, ground + 1, z1, Direction.SOUTH);
        sign(b, p, cx - 80, ground + 7, z1, Direction.SOUTH, "CAPE HOTEL", "ROOMS");
        for (int x = x0 + 4; x <= x1 - 4; x += 6) {
            b.window(x, ground + 4, z1, 3, 2, true);
            b.put(x - 1, ground + 4, z1, p.accent());
            b.put(x - 1, ground + 5, z1, p.accent());
        }
        b.window(x0, ground + 4, cz - 124, 3, 1, true);
        b.window(x1, ground + 4, cz - 124, 3, 1, true);
        b.fill(cx - 95, ground + 1, cz - 132, cx - 92, ground + 1, cz - 129,
                Materials.wool(DyeColor.RED));
        b.fill(cx - 95, ground + 1, cz - 132, cx - 95, ground + 1, cz - 131,
                Materials.wool(DyeColor.WHITE));
        b.fill(cx - 69, ground + 1, cz - 132, cx - 66, ground + 1, cz - 129,
                Materials.wool(DyeColor.BLUE));
        b.fill(cx - 69, ground + 1, cz - 132, cx - 69, ground + 1, cz - 131,
                Materials.wool(DyeColor.WHITE));
        b.fill(cx - 88, ground + 1, cz - 120, cx - 72, ground + 1, cz - 120,
                p.accent());
        b.put(cx - 86, ground + 2, cz - 120, p.light());
        b.put(cx - 74, ground + 2, cz - 120, p.light());
        signPost(b, cx - 80, cz - 106, ground, p, Direction.SOUTH, "CAPE HOTEL");
    }
}
