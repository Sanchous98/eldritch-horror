package com.sanchous98.eldritchhorror.world.loc.city.plan;

import net.minecraft.world.item.DyeColor;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.SiteLoot;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import com.sanchous98.eldritchhorror.world.loc.city.CitySurface;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Buenos Aires between the wars: the beaux-arts palace over the Plaza de Mayo, the tango halls of
 * the west, the port and its rail yard on the river, the Recoleta cemetery and its crypt, and the
 * shops and banks of the centro.
 *
 * <p>Everything is authored: absolute offsets from {@code (cx, cz)}, the city's {@code ground}
 * level and the shared graded {@link CitySurface}, with explicit arrays and loops. There is no
 * {@code RandomSource}, no layout hash and no chunk-local read. {@link StructureBuilder} clips
 * every write to the generating chunk, so the same call builds the same city slice everywhere.
 * Streets and the harbour are laid first, then the districts, so no facade can overwrite a road,
 * a quay or the water.
 */
public final class BuenosAiresPlan implements CityPlan {

    /** Authored half-extent (blocks). */
    private static final int HALF = 340;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState STONE = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState SLATE = Blocks.DEEPSLATE_TILES.defaultBlockState();
    private static final BlockState MARBLE = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState CALCITE = Blocks.CALCITE.defaultBlockState();
    private static final BlockState GOLD = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final BlockState DARK_WOOD = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private static final BlockState BOOKS = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState BONE = Blocks.BONE_BLOCK.defaultBlockState();
    private static final BlockState SKULL = Blocks.SKELETON_SKULL.defaultBlockState();
    private static final BlockState TRUNK = Blocks.DARK_OAK_LOG.defaultBlockState();
    private static final BlockState LEAVES = Blocks.OAK_LEAVES.defaultBlockState()
            .setValue(BlockStateProperties.PERSISTENT, true);
    private static final BlockState CYPRESS = Blocks.DARK_OAK_LEAVES.defaultBlockState()
            .setValue(BlockStateProperties.PERSISTENT, true);
    private static final BlockState RED = Blocks.DYED_TERRACOTTA.pick(DyeColor.RED).defaultBlockState();
    private static final BlockState BLUE = Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_BLUE).defaultBlockState();
    private static final BlockState YELLOW = Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW).defaultBlockState();
    private static final BlockState GREEN = Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN).defaultBlockState();
    private static final BlockState WHITE = Blocks.DYED_TERRACOTTA.pick(DyeColor.WHITE).defaultBlockState();
    private static final BlockState RAIL = Blocks.RAIL.defaultBlockState();

    @Override
    public String cityId() {
        return "buenos_aires";
    }

    @Override
    public int halfExtent() {
        return HALF;
    }

    @Override
    public void build(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Fallback for callers that only know the flat-heart level; the generator calls the
        // context overload below, which knows the graded rim.
        build(b, new CityPlanContext(cx, cz, ground, HALF,
                CitySurface.of(b, cx, cz, HALF, ground, 24)), p);
    }

    @Override
    public void build(StructureBuilder b, CityPlanContext ctx, Palette p) {
        int cx = ctx.cx();
        int cz = ctx.cz();
        int ground = ctx.ground();
        CitySurface s = ctx.surface();

        // 1. The street grid and the river first, so no facade overwrites a road or the water.
        streets(b, cx, cz, ground, s, p);
        harbour(b, cx, cz, s, p);

        // 2. The districts.
        plazaMayo(b, cx, cz, ground, s, p);
        palace(b, cx, cz, s, p);
        cathedral(b, cx, cz, s, p);
        governmentHouse(b, cx, cz, s, p);
        rearGarden(b, cx, cz, s, p);
        tangoDistrict(b, cx, cz, s, p);
        centro(b, cx, cz, s, p);
        port(b, cx, cz, s, p);
        recoleta(b, cx, cz, s, p);
        arrival(b, cx, cz, s, p);

        // 3. Greenery and the readable notices on top of the built fabric.
        greenery(b, cx, cz, s, p);
        signs(b, cx, cz, s, p);

        b.marker("buenos_aires_center", cx, ground + 1, cz);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                // Arrival and notices.
                new CityPoi("arrival_gate", CityPoi.Kind.SIGN, 8, 1, 306, "arrival"),
                new CityPoi("city_notices", CityPoi.Kind.SIGN, -24, 1, 306, "notices"),
                new CityPoi("plaza_quest_board", CityPoi.Kind.QUEST_BOARD, 0, 0, -120,
                        "buenos_aires_missing_heiress;buenos_aires_stolen_tango"),
                // The Plaza de Mayo and the palace.
                new CityPoi("palace", CityPoi.Kind.LANDMARK, 0, 3, -205,
                        "portico,dome,staircase"),
                new CityPoi("plaza_mayo", CityPoi.Kind.LANDMARK, 0, 0, -130, "plaza,pyramid"),
                new CityPoi("cathedral", CityPoi.Kind.LANDMARK, -130, 0, -205, "cathedral"),
                new CityPoi("government_house", CityPoi.Kind.LANDMARK, 130, 0, -205,
                        "casa_rosada"),
                new CityPoi("palace_sign", CityPoi.Kind.SIGN, 34, 1, -150, "palace"),
                // Tango halls.
                new CityPoi("tango_hall", CityPoi.Kind.LANDMARK, -240, 0, 10, "milonga,stage"),
                new CityPoi("tango_singer", CityPoi.Kind.NPC, -240, 0, 30,
                        "buenos_aires_missing_heiress"),
                new CityPoi("tango_hall_owner", CityPoi.Kind.NPC, -240, 0, 16, "owner"),
                new CityPoi("theatre", CityPoi.Kind.LANDMARK, -282, 0, -40, "theatre"),
                new CityPoi("tango_sign", CityPoi.Kind.SIGN, -240, 1, 8, "tango"),
                new CityPoi("apothecary", CityPoi.Kind.HEALER, -240, 0, 120, "remedies"),
                new CityPoi("apothecary_keeper", CityPoi.Kind.NPC, -250, 0, 134, "apothecary"),
                // Centro.
                new CityPoi("hotel", CityPoi.Kind.INN, -80, 0, -60, "rooms"),
                new CityPoi("hotel_clerk", CityPoi.Kind.NPC, -80, 0, -70, "hotel_clerk"),
                new CityPoi("centro_shop", CityPoi.Kind.SHOP, -40, 0, 120, "dry_goods"),
                new CityPoi("shopkeeper", CityPoi.Kind.NPC, -40, 0, 132, "shopkeeper"),
                new CityPoi("centro_bank", CityPoi.Kind.LANDMARK, 40, 0, 60, "bank"),
                new CityPoi("constable", CityPoi.Kind.NPC, -30, 0, -70, "constable"),
                // Port, customs and rail.
                new CityPoi("port_customs", CityPoi.Kind.NPC, 240, 0, -20,
                        "buenos_aires_stolen_tango"),
                new CityPoi("customs_officer", CityPoi.Kind.NPC, 232, 0, -28, "customs_officer"),
                new CityPoi("harbour", CityPoi.Kind.PORT, 302, 1, -60, "pier,deep_water"),
                new CityPoi("docks", CityPoi.Kind.LANDMARK, 292, 0, 60, "piers,cranes"),
                new CityPoi("rail_station", CityPoi.Kind.STATION, 228, 1, 155,
                        "rail station;east_west"),
                new CityPoi("station_sign", CityPoi.Kind.SIGN, 200, 1, 148, "rail"),
                new CityPoi("port_warning", CityPoi.Kind.SIGN, 296, 1, 20, "warning"),
                new CityPoi("port_sign", CityPoi.Kind.SIGN, 140, 1, 10, "port"),
                // Recoleta and the crypt.
                new CityPoi("recoleta", CityPoi.Kind.LANDMARK, 240, 0, 220, "cemetery,basilica"),
                new CityPoi("basilica", CityPoi.Kind.LANDMARK, 166, 0, 224, "basilica"),
                new CityPoi("gardener", CityPoi.Kind.NPC, 160, 0, 270, "gardener"),
                new CityPoi("recoleta_crypt", CityPoi.Kind.SECRET, 240, -8, 240,
                        "chests,lore,heiress"),
                new CityPoi("crypt_entrance", CityPoi.Kind.SIGN, 240, 1, 252, "crypt"),
                new CityPoi("recoleta_sign", CityPoi.Kind.SIGN, 240, 1, 190, "recoleta"),
                new CityPoi("cemetery_warning", CityPoi.Kind.SIGN, 236, 1, 200, "warning")
        );
    }

    // ------------------------------------------------------------------ terrain helpers

    /** Cuts terrain above the authored level and fills hollows (and shallow water) below it. */
    private static void settle(StructureBuilder b, int x, int z, int y, Palette p) {
        int surface = b.groundY(x, z);
        if (surface > y) {
            b.fill(x, y + 1, z, x, surface + 2, z, AIR);
        } else if (surface < y) {
            b.fill(x, surface + 1, z, x, y, z, p.foundation());
        }
    }

    /** Fills a three-course slab at a fixed level (heart ground, already graded by the pass). */
    private static void slab(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                             BlockState state) {
        b.fill(x0, y - 2, z0, x1, y, z1, state);
    }

    /** Levels a footprint to {@code y} and caps it with a foundation course. */
    private static void pad(StructureBuilder b, int x0, int z0, int x1, int z1, int y, Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                settle(b, x, z, y, p);
                b.put(x, y, z, p.foundation());
            }
        }
    }

    /** Levels and paves an authored rectangle at a fixed level. */
    private static void paveAt(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                               BlockState state, Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                settle(b, x, z, y, p);
                b.put(x, y, z, state);
            }
        }
    }

    /** Levels and paves a rectangle on the shared graded surface (heart or rim). */
    private static void paveOn(StructureBuilder b, CitySurface s, int x0, int z0, int x1, int z1,
                               BlockState state, Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                int y = s.at(x, z);
                settle(b, x, z, y, p);
                b.put(x, y, z, state);
            }
        }
    }

    /** Carves a dock basin one course below the surrounding quay. */
    private static void basin(StructureBuilder b, CitySurface s, int x0, int z0, int x1, int z1,
                              Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                int y = s.at(x, z);
                b.put(x, y, z, AIR);
                b.put(x, y - 1, z, WATER);
                b.put(x, y - 2, z, WATER);
                b.put(x, y - 3, z, WATER);
                b.put(x, y - 4, z, WATER);
                b.put(x, y - 5, z, p.foundation());
            }
        }
    }

    // ------------------------------------------------------------------ streets

    private static void streets(StructureBuilder b, int cx, int cz, int ground, CitySurface s,
                                Palette p) {
        // The main axis: arrival gate to the Plaza de Mayo, 14 wide.
        paveOn(b, s, cx - 7, cz - 96, cx + 6, cz + 334, p.ground(), p);
        // The grand avenue: tango halls to the port, 14 wide.
        paveOn(b, s, cx - 330, cz - 7, cx + 296, cz + 6, p.ground(), p);

        // Tango lanes, 6 wide.
        paveOn(b, s, cx - 312, cz - 240, cx - 307, cz + 180, p.ground(), p);
        paveOn(b, s, cx - 192, cz - 240, cx - 187, cz + 180, p.ground(), p);
        paveOn(b, s, cx - 320, cz - 72, cx - 140, cz - 67, p.ground(), p);
        paveOn(b, s, cx - 320, cz + 48, cx - 140, cz + 53, p.ground(), p);
        paveOn(b, s, cx - 320, cz + 114, cx - 140, cz + 119, p.ground(), p);
        paveOn(b, s, cx - 320, cz - 178, cx - 140, cz - 173, p.ground(), p);

        // Centro lanes.
        paveOn(b, s, cx - 104, cz - 58, cx - 8, cz - 53, p.ground(), p);
        paveOn(b, s, cx - 130, cz + 116, cx + 150, cz + 121, p.ground(), p);

        // Recoleta approach and the port's own lanes.
        paveOn(b, s, cx - 7, cz + 184, cx + 300, cz + 189, p.ground(), p);
        paveOn(b, s, cx + 142, cz - 140, cx + 147, cz + 150, p.ground(), p);
        paveOn(b, s, cx + 142, cz - 126, cx + 296, cz - 117, p.ground(), p);
        paveOn(b, s, cx + 142, cz + 84, cx + 296, cz + 93, p.ground(), p);

        // The plaza and the arrival forecourt.
        slab(b, cx - 112, cz - 264, cx + 112, cz - 96, ground, p.ground());
        paveOn(b, s, cx - 30, cz + 300, cx + 30, cz + 336, p.ground(), p);
    }

    // ------------------------------------------------------------------ the river

    private static void harbour(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int g = s.at(cx, cz);
        // A deliberate harbour channel east of the quay, so the docks always meet water.
        for (int x = cx + 301; x <= cx + 330; x++) {
            for (int z = cz - 200; z <= cz + 145; z++) {
                b.fill(x, g - 4, z, x, g - 1, z, WATER);
                b.put(x, g, z, AIR);
                b.fill(x, g + 1, z, x, g + 10, z, AIR);
                b.put(x, g - 5, z, p.foundation());
            }
        }
        // Two timber piers reaching into the channel.
        pier(b, s, cx + 301, cz - 60, 26, p);
        pier(b, s, cx + 301, cz + 60, 26, p);
    }

    private static void pier(StructureBuilder b, CitySurface s, int x, int z, int length,
                             Palette p) {
        int base = s.at(x, z) + 1;
        b.fill(x, base, z - 3, x + length, base, z + 3, STONE);
        for (int px = x; px <= x + length; px += 8) {
            b.fill(px, base - 3, z - 3, px, base - 1, z - 3, p.foundation());
            b.fill(px, base - 3, z + 3, px, base - 1, z + 3, p.foundation());
        }
        for (int px = x + 4; px <= x + length; px += 8) {
            b.put(px, base + 1, z - 3, p.accent());
            b.put(px, base + 1, z + 3, p.accent());
        }
        b.put(x + length, base + 1, z - 3, p.light());
        b.put(x + length, base + 1, z + 3, p.light());
    }

    // ------------------------------------------------------------------ Plaza de Mayo

    private static void plazaMayo(StructureBuilder b, int cx, int cz, int ground, CitySurface s,
                                  Palette p) {
        // The Pirámide de Mayo and the plaza furniture.
        b.monument(cx - 40, cz - 130, ground + 1);
        fountain(b, cx + 50, cz - 120, s.at(cx + 50, cz - 120), p, 5);
        questBoard(b, cx, cz - 120, s.at(cx, cz - 120), p);

        for (int x = -96; x <= 96; x += 24) {
            lamp(b, cx + x, cz - 102, s.at(cx + x, cz - 102), p);
        }
        bench(b, cx - 30, cz - 110, ground, p);
        bench(b, cx + 30, cz - 110, ground, p);
        statue(b, cx - 60, cz - 150, s.at(cx - 60, cz - 150), p);
        statue(b, cx + 60, cz - 150, s.at(cx + 60, cz - 150), p);
        tree(b, cx - 90, cz - 112, s.at(cx - 90, cz - 112), p);
        tree(b, cx + 90, cz - 112, s.at(cx + 90, cz - 112), p);
    }

    private static void palace(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int x0 = cx - 70;
        int x1 = cx + 70;
        int z0 = cz - 244;
        int z1 = cz - 166;
        int floor = s.at(cx, cz - 205) + 3;
        pad(b, x0 - 3, z0 - 3, x1 + 3, z1 + 13, floor, p);

        // The main block, its quoins, glazing and cornice.
        shell(b, x0, floor, z0, x1, floor + 24, z1, p.wall(), p.foundation(), p.roof());
        quoins(b, x0, z0, x1, z1, floor, floor + 24, p);
        windows(b, x0, z0, x1, z1, floor, floor + 24);
        b.fill(x0 - 1, floor + 25, z0 - 1, x1 + 1, floor + 25, z1 + 1, p.accent());

        // The central pavilion, projecting south over the portico.
        int px0 = cx - 26;
        int px1 = cx + 26;
        int pz0 = cz - 244;
        int pz1 = cz - 158;
        shell(b, px0, floor, pz0, px1, floor + 32, pz1, p.wall(), p.foundation(), p.roof());
        quoins(b, px0, pz0, px1, pz1, floor, floor + 32, p);
        windows(b, px0, pz0, px1, pz1, floor, floor + 32);
        b.fill(px0 - 1, floor + 33, pz0 - 1, px1 + 1, floor + 33, pz1 + 1, p.accent());
        pedimentSouth(b, cx, pz1, floor + 34, 30, p);

        // The colonnaded portico and the grand staircase on the plaza.
        for (int x = cx - 28; x <= cx + 28; x += 8) {
            b.fill(x, floor + 1, pz1 + 4, x, floor + 14, pz1 + 4, p.accent());
            b.put(x, floor + 15, pz1 + 4, p.accent());
        }
        b.fill(px0 - 4, floor + 16, pz1, px1 + 4, floor + 16, pz1 + 5, p.accent());
        for (int i = 0; i < 3; i++) {
            int y = floor - 1 - i;
            int za = z1 + 13 + i * 2;
            b.fill(cx - 30, y, za, cx + 30, y, za + 1, p.foundation());
            b.fill(cx - 30, y + 1, za, cx + 30, y + 4, za + 1, AIR);
        }
        statue(b, cx - 40, cz - 150, s.at(cx - 40, cz - 150), p);
        statue(b, cx + 40, cz - 150, s.at(cx + 40, cz - 150), p);

        // The high slate dome on its colonnaded drum, with a gold finial.
        int dcx = cx;
        int dcz = cz - 201;
        drum(b, dcx, dcz, floor + 33, 20, 8, p);
        dome(b, dcx, dcz, floor + 41, 20, 14, SLATE, p.accent());
        b.fill(dcx - 2, floor + 55, dcz - 2, dcx + 2, floor + 58, dcz + 2, p.accent());
        b.fill(dcx - 1, floor + 56, dcz - 1, dcx + 1, floor + 57, dcz + 1, AIR);
        b.put(dcx, floor + 59, dcz, GOLD);

        signPost(b, p, cx + 34, cz - 150, s.at(cx + 34, cz - 150), Direction.SOUTH,
                "PALACE", "PLAZA DE MAYO", "NO CLIMBING");
    }

    private static void cathedral(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int x0 = cx - 150;
        int x1 = cx - 114;
        int z0 = cz - 236;
        int z1 = cz - 178;
        int base = s.at(cx - 132, cz - 207);
        pad(b, x0 - 2, z0 - 2, x1 + 2, z1 + 2, base, p);

        shell(b, x0, base, z0, x1, base + 24, z1, p.wall(), p.foundation(), p.roof());
        quoins(b, x0, z0, x1, z1, base, base + 24, p);
        windows(b, x0, z0, x1, z1, base, base + 24);
        b.pitchedRoof(x0 - 2, z0 - 2, x1 + 2, z1 + 2, base + 25, 8, 0);

        // East portico, facing the plaza.
        for (int z = z0 + 6; z <= z1 - 6; z += 6) {
            b.fill(x1 + 4, base + 1, z, x1 + 4, base + 13, z, p.accent());
        }
        b.fill(x1 + 3, base + 14, z0 + 2, x1 + 5, base + 14, z1 - 2, p.accent());
        pedimentEast(b, x1 + 5, cz - 207, base + 15, 12, p);
        b.fill(x1, base + 1, cz - 210, x1, base + 5, cz - 204, AIR);
        door(b, p, x1, base + 1, cz - 207, Direction.EAST);

        // Twin towers at the west end and a dome over the crossing.
        towerAt(b, x0 + 7, z0 + 7, 5, 32, base, p);
        towerAt(b, x0 + 7, z1 - 7, 5, 32, base, p);
        drum(b, x0 + 22, cz - 207, base + 25, 8, 5, p);
        dome(b, x0 + 22, cz - 207, base + 30, 8, 6, MARBLE, p.accent());

        signPost(b, p, x1 + 2, cz - 172, s.at(x1 + 2, cz - 172), Direction.EAST,
                "CATHEDRAL", "OPEN DAWN", "NO GUIDES");
    }

    private static void governmentHouse(StructureBuilder b, int cx, int cz, CitySurface s,
                                        Palette p) {
        int x0 = cx + 112;
        int x1 = cx + 150;
        int z0 = cz - 236;
        int z1 = cz - 178;
        int base = s.at(cx + 130, cz - 207);
        pad(b, x0 - 2, z0 - 2, x1 + 2, z1 + 2, base, p);

        shell(b, x0, base, z0, x1, base + 18, z1, p.wall(), p.foundation(), p.roof());
        quoins(b, x0, z0, x1, z1, base, base + 18, p);
        windows(b, x0, z0, x1, z1, base, base + 18);
        mansard(b, x0, z0, x1, z1, base + 18, p);

        // The west front: a balcony over the door and a low arcade.
        b.fill(x0, base + 1, cz - 216, x0, base + 6, cz - 198, AIR);
        for (int z = cz - 216; z <= cz - 198; z += 6) {
            b.fill(x0 - 1, base + 1, z, x0 - 1, base + 6, z, p.accent());
        }
        b.fill(x0 - 1, base + 7, cz - 216, x0 - 1, base + 7, cz - 198, p.rail());
        b.fill(x0, base + 7, cz - 216, x0, base + 7, cz - 198, p.rail());
        door(b, p, x0, base + 1, cz - 207, Direction.WEST);
        b.put(x0, base + 9, cz - 207, p.light());

        signPost(b, p, x0 - 3, cz - 170, s.at(x0 - 3, cz - 170), Direction.WEST,
                "CASA ROSADA", "GOVERNMENT", "BY APPOINTMENT");
    }

    private static void rearGarden(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        paveOn(b, s, cx - 80, cz - 300, cx + 80, cz - 266, GRASS, p);
        for (int x = -76; x <= 76; x += 12) {
            tree(b, cx + x, cz - 296, s.at(cx + x, cz - 296), p);
            cypress(b, cx + x + 6, cz - 270, s.at(cx + x + 6, cz - 270));
        }
        fountain(b, cx, cz - 284, s.at(cx, cz - 284), p, 5);
    }

    // ------------------------------------------------------------------ tango halls

    private static void tangoDistrict(StructureBuilder b, int cx, int cz, CitySurface s,
                                      Palette p) {
        tangoHall(b, cx, cz, s, p);
        theatre(b, cx, cz, s, p);
        milonga(b, cx - 165, cz + 75, s, p);
        apothecary(b, cx, cz, s, p);

        cafe(b, cx - 296, cz - 100, cx - 280, cz - 84, s, p, Side.W);
        cafe(b, cx - 178, cz - 102, cx - 162, cz - 86, s, p, Side.W);
        cafe(b, cx - 296, cz + 64, cx - 280, cz + 80, s, p, Side.W);
        cafe(b, cx - 178, cz + 130, cx - 162, cz + 146, s, p, Side.W);

        laBoca(b, cx - 300, cz + 148, cx - 274, cz + 176, s, p, RED);
        laBoca(b, cx - 260, cz + 148, cx - 234, cz + 176, s, p, BLUE);
        laBoca(b, cx - 220, cz + 148, cx - 194, cz + 176, s, p, YELLOW);

        signPost(b, p, cx - 160, cz - 64, s.at(cx - 160, cz - 64), Direction.NORTH,
                "TANGO HALLS", "CAFES", "MILONGAS");
        signPost(b, p, cx - 282, cz + 132, s.at(cx - 282, cz + 132), Direction.NORTH,
                "LA BOCA", "PAINTED HOUSES");
    }

    private static void tangoHall(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int x0 = cx - 278;
        int x1 = cx - 212;
        int z0 = cz + 12;
        int z1 = cz + 44;
        int base = s.at(cx - 245, cz + 28);
        pad(b, x0 - 2, z0 - 2, x1 + 2, z1 + 2, base, p);
        shell(b, x0, base, z0, x1, base + 18, z1, p.wall(), p.foundation(), p.roof());
        quoins(b, x0, z0, x1, z1, base, base + 18, p);
        windows(b, x0, z0, x1, z1, base, base + 18);
        mansard(b, x0, z0, x1, z1, base + 18, p);

        // The north front opens onto the avenue; the stage is at the far end.
        b.fill(x0 + 8, base + 1, z0, x1 - 8, base + 6, z0, AIR);
        for (int x = x0 + 10; x <= x1 - 10; x += 6) {
            b.fill(x, base + 1, z0 - 1, x, base + 6, z0 - 1, p.accent());
        }
        b.fill(x0 + 8, base + 7, z0 - 1, x1 - 8, base + 7, z0 - 1, p.rail());
        door(b, p, cx - 240, base + 1, z0, Direction.NORTH);

        b.fill(x0 + 4, base + 1, z1 - 10, x1 - 4, base + 3, z1 - 4, DARK_WOOD);
        b.fill(x0 + 6, base + 1, z0 + 4, x1 - 6, base + 1, z1 - 12, RED);
        for (int x = x0 + 12; x <= x1 - 12; x += 12) {
            b.put(x, base + 14, cz + 28, p.light());
        }
        b.put(cx - 240, base + 12, z1 - 8, GOLD);

        frontSign(b, p, x0, z0, x1, z1, base, Side.N,
                "TANGO HALL", "MILONGA", "TONIGHT", "MISSING HEIRESS");
    }

    private static void theatre(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int x0 = cx - 300;
        int x1 = cx - 264;
        int z0 = cz - 64;
        int z1 = cz - 20;
        int base = s.at(cx - 282, cz - 42);
        pad(b, x0 - 2, z0 - 2, x1 + 2, z1 + 2, base, p);
        shell(b, x0, base, z0, x1, base + 22, z1, p.wall(), p.foundation(), p.roof());
        quoins(b, x0, z0, x1, z1, base, base + 22, p);
        windows(b, x0, z0, x1, z1, base, base + 22);
        dome(b, cx - 282, cz - 42, base + 23, 8, 7, SLATE, p.accent());

        // The south front and its marquee.
        b.fill(cx - 288, base + 1, z1, cx - 276, base + 6, z1, AIR);
        door(b, p, cx - 282, base + 1, z1, Direction.SOUTH);
        for (int x = x0 + 2; x <= x1 - 2; x++) {
            b.put(x, base + 8, z1 + 1, (x & 1) == 0 ? RED : WHITE);
            b.put(x, base + 9, z1 + 1, (x & 1) == 0 ? WHITE : RED);
        }
        frontSign(b, p, x0, z0, x1, z1, base, Side.S,
                "THEATRE", "TANGO OPERA", "BOX OFFICE");
    }

    private static void milonga(StructureBuilder b, int x, int z, CitySurface s, Palette p) {
        int x0 = x - 15;
        int x1 = x + 15;
        int z0 = z - 15;
        int z1 = z + 15;
        int base = s.at(x, z);
        house(b, x0, z0, x1, z1, base, 12, p, Side.W, 12);
        b.fill(x0 - 1, base + 1, z0 + 4, x0 - 1, base + 6, z1 - 4, AIR);
        b.fill(x0 - 1, base + 7, z0 + 4, x0 - 1, base + 7, z1 - 4, p.rail());
        frontSign(b, p, x0, z0, x1, z1, base, Side.W, "MILONGA", "TANGO", "OPEN LATE");
    }

    private static void apothecary(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int x0 = cx - 264;
        int x1 = cx - 236;
        int z0 = cz + 124;
        int z1 = cz + 144;
        int base = s.at(cx - 250, cz + 134);
        house(b, x0, z0, x1, z1, base, 10, p, Side.N, 10);
        // A green cross over the door.
        b.fill(x0 + 10, base + 7, z0 - 1, x0 + 14, base + 7, z0 - 1, GREEN);
        b.fill(x0 + 12, base + 5, z0 - 1, x0 + 12, base + 9, z0 - 1, GREEN);
        frontSign(b, p, x0, z0, x1, z1, base, Side.N,
                "APOTHECARY", "REMEDIES", "HERBS & SALTS");
    }

    // ------------------------------------------------------------------ centro

    private static void centro(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        hotel(b, cx, cz, s, p);
        bank(b, cx, cz, s, p);
        offices(b, cx, cz, s, p);
        shop(b, cx - 40, cz + 132, s, p);
        constablePost(b, cx, cz, s, p);

        cafe(b, cx - 34, cz + 28, cx - 18, cz + 44, s, p, Side.E);
        cafe(b, cx + 18, cz + 92, cx + 34, cz + 108, s, p, Side.W);
        cafe(b, cx - 34, cz + 92, cx - 18, cz + 108, s, p, Side.E);

        fountain(b, cx + 70, cz + 96, s.at(cx + 70, cz + 96), p, 4);
        signPost(b, p, cx + 12, cz + 170, s.at(cx + 12, cz + 170), Direction.SOUTH,
                "CENTRO", "SHOPS", "CAFES", "BANKS");
    }

    private static void hotel(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int x0 = cx - 96;
        int x1 = cx - 64;
        int z0 = cz - 82;
        int z1 = cz - 61;
        int base = s.at(cx - 80, cz - 71);
        house(b, x0, z0, x1, z1, base, 16, p, Side.S, 16);
        // A porte-cochère and the room lights.
        b.fill(cx - 86, base + 1, z1, cx - 74, base + 5, z1, AIR);
        door(b, p, cx - 80, base + 1, z1, Direction.SOUTH);
        b.put(cx - 92, base + 6, z1 - 1, p.light());
        b.put(cx - 68, base + 6, z1 - 1, p.light());
        frontSign(b, p, x0, z0, x1, z1, base, Side.S,
                "HOTEL", "ROOMS & BEDS", "KEEPER INSIDE");
    }

    private static void bank(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int x0 = cx + 20;
        int x1 = cx + 64;
        int z0 = cz + 40;
        int z1 = cz + 80;
        int base = s.at(cx + 42, cz + 60);
        pad(b, x0 - 2, z0 - 2, x1 + 2, z1 + 2, base, p);
        shell(b, x0, base, z0, x1, base + 20, z1, p.wall(), p.foundation(), p.roof());
        quoins(b, x0, z0, x1, z1, base, base + 20, p);
        windows(b, x0, z0, x1, z1, base, base + 20);

        // The west portico: six columns, a pediment and a gold-lettered frieze.
        for (int z = z0 + 6; z <= z1 - 6; z += 6) {
            b.fill(x0 - 3, base + 1, z, x0 - 3, base + 12, z, p.accent());
        }
        b.fill(x0 - 4, base + 13, z0 + 2, x0 - 2, base + 13, z1 - 2, p.accent());
        pedimentWest(b, x0 - 4, cz + 60, base + 14, 10, p);
        b.fill(x0, base + 1, cz + 54, x0, base + 5, cz + 66, AIR);
        door(b, p, x0, base + 1, cz + 60, Direction.WEST);
        b.fill(x0, base + 8, z0 + 4, x0, base + 8, z1 - 4, GOLD);

        // A shallow dome and the vault sign.
        dome(b, cx + 42, cz + 60, base + 21, 10, 6, SLATE, p.accent());
        frontSign(b, p, x0, z0, x1, z1, base, Side.W, "CENTRO BANK", "DEPOSITS", "VAULTS");
    }

    private static void offices(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int x0 = cx + 84;
        int x1 = cx + 120;
        int z0 = cz + 84;
        int z1 = cz + 114;
        int base = s.at(cx + 102, cz + 99);
        pad(b, x0 - 2, z0 - 2, x1 + 2, z1 + 2, base, p);
        shell(b, x0, base, z0, x1, base + 28, z1, STONE, p.foundation(), p.roof());
        quoins(b, x0, z0, x1, z1, base, base + 28, p);
        windows(b, x0, z0, x1, z1, base, base + 28);
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, base + 29);
        b.fill(x0, base + 1, cz + 96, x0, base + 4, cz + 102, AIR);
        door(b, p, x0, base + 1, cz + 99, Direction.WEST);
        b.fill(x0 + 4, base + 20, z1, x0 + 12, base + 24, z1, BOOKS);
    }

    private static void shop(StructureBuilder b, int x, int z, CitySurface s, Palette p) {
        int x0 = x - 12;
        int x1 = x + 12;
        int z0 = z - 10;
        int z1 = z + 10;
        int base = s.at(x, z);
        house(b, x0, z0, x1, z1, base, 10, p, Side.N, 12);
        b.window(x0 + 3, base + 1, z0, 3, 4, false);
        b.fill(x0 + 2, base + 5, z0 - 1, x1 - 2, base + 5, z0 - 1, RED);
        frontSign(b, p, x0, z0, x1, z1, base, Side.N, "SHOP", "DRY GOODS", "OPEN");
    }

    private static void constablePost(StructureBuilder b, int cx, int cz, CitySurface s,
                                      Palette p) {
        int x0 = cx - 40;
        int x1 = cx - 24;
        int z0 = cz - 78;
        int z1 = cz - 62;
        int base = s.at(cx - 32, cz - 70);
        house(b, x0, z0, x1, z1, base, 7, p, Side.E, 8);
        b.put(x1 + 1, base + 5, z0 - 1, p.light());
        frontSign(b, p, x0, z0, x1, z1, base, Side.E, "CONSTABLE", "REPORT CRIMES");
    }

    // ------------------------------------------------------------------ port

    private static void port(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int g = s.at(cx, cz);
        // The wharf apron: a flat stone platform reclaimed along the river.
        slab(b, cx + 136, cz - 200, cx + 300, cz + 150, g, STONE);

        warehouse(b, cx + 152, cz - 196, cx + 196, cz - 146, s, p, Side.S);
        warehouse(b, cx + 204, cz - 196, cx + 248, cz - 146, s, p, Side.S);
        warehouse(b, cx + 152, cz + 30, cx + 196, cz + 78, s, p, Side.S);
        warehouse(b, cx + 204, cz + 40, cx + 246, cz + 78, s, p, Side.E);
        warehouse(b, cx + 152, cz + 96, cx + 196, cz + 116, s, p, Side.N);
        warehouse(b, cx + 204, cz + 96, cx + 246, cz + 116, s, p, Side.N);

        customs(b, cx, cz, s, p);
        station(b, cx, cz, s, p);
        quay(b, cx, cz, g, p);
        dockBasin(b, cx, cz, s, p);
        cranes(b, cx, cz, g, p);
        lighthouse(b, cx + 296, cz - 196, g, p);

        signPost(b, p, cx + 140, cz + 10, s.at(cx + 140, cz + 10), Direction.NORTH,
                "PORT", "DOCKS", "CUSTOMS");
        signPost(b, p, cx + 196, cz - 146, s.at(cx + 196, cz - 146), Direction.SOUTH,
                "WAREHOUSE", "DOCK HANDS");
    }

    private static void warehouse(StructureBuilder b, int x0, int z0, int x1, int z1,
                                  CitySurface s, Palette p, Side front) {
        int base = s.at((x0 + x1) / 2, (z0 + z1) / 2);
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, base, p);
        shell(b, x0, base, z0, x1, base + 10, z1, STONE, p.foundation(), p.roof());
        for (int y = base; y <= base + 10; y++) {
            b.put(x0, y, z0, p.accent());
            b.put(x1, y, z0, p.accent());
            b.put(x0, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
        int axis = (x1 - x0) >= (z1 - z0) ? 0 : 1;
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, base + 11, 4, axis);

        switch (front) {
            case S -> {
                b.fill((x0 + x1) / 2 - 4, base + 1, z1, (x0 + x1) / 2 + 4, base + 6, z1, AIR);
                door(b, p, (x0 + x1) / 2, base + 1, z1, Direction.SOUTH);
            }
            case N -> {
                b.fill((x0 + x1) / 2 - 4, base + 1, z0, (x0 + x1) / 2 + 4, base + 6, z0, AIR);
                door(b, p, (x0 + x1) / 2, base + 1, z0, Direction.NORTH);
            }
            case W -> {
                b.fill(x0, base + 1, (z0 + z1) / 2 - 4, x0, base + 6, (z0 + z1) / 2 + 4, AIR);
                door(b, p, x0, base + 1, (z0 + z1) / 2, Direction.WEST);
            }
            case E -> {
                b.fill(x1, base + 1, (z0 + z1) / 2 - 4, x1, base + 6, (z0 + z1) / 2 + 4, AIR);
                door(b, p, x1, base + 1, (z0 + z1) / 2, Direction.EAST);
            }
        }
        frontSign(b, p, x0, z0, x1, z1, base, front, "WAREHOUSE", "DOCK HANDS");
    }

    private static void customs(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int x0 = cx + 214;
        int x1 = cx + 250;
        int z0 = cz - 44;
        int z1 = cz - 12;
        int base = s.at(cx + 232, cz - 28);
        pad(b, x0 - 2, z0 - 2, x1 + 2, z1 + 2, base, p);
        shell(b, x0, base, z0, x1, base + 16, z1, p.wall(), p.foundation(), p.roof());
        quoins(b, x0, z0, x1, z1, base, base + 16, p);
        windows(b, x0, z0, x1, z1, base, base + 16);

        // The south portico faces the avenue.
        for (int x = x0 + 4; x <= x1 - 4; x += 6) {
            b.fill(x, base + 1, z1 + 3, x, base + 10, z1 + 3, p.accent());
        }
        b.fill(x0 + 2, base + 11, z1 + 2, x1 - 2, base + 11, z1 + 4, p.accent());
        pedimentSouth(b, cx + 232, z1 + 4, base + 12, 12, p);
        b.fill(cx + 228, base + 1, z1, cx + 236, base + 5, z1, AIR);
        door(b, p, cx + 232, base + 1, z1, Direction.SOUTH);
        b.fill(cx + 226, base + 12, z1, cx + 238, base + 15, z1, GOLD);

        // Shelves and the seized crates inside.
        b.fill(x0 + 3, base + 1, z0 + 3, x0 + 4, base + 4, z1 - 3, BOOKS);
        b.fill(x1 - 4, base + 1, z0 + 3, x1 - 3, base + 3, z1 - 3, DARK_WOOD);

        frontSign(b, p, x0, z0, x1, z1, base, Side.S,
                "CUSTOMS", "STOLEN TANGO", "REPORT IT HERE");
    }

    private static void station(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int sx = cx + 228;
        int sz = cz + 135;
        int x0 = sx - 30;
        int x1 = sx + 30;
        int z0 = sz - 11;
        int z1 = sz + 11;
        int base = s.at(sx, sz);
        pad(b, x0 - 2, z0 - 2, x1 + 2, z1 + 2, base, p);
        shell(b, x0, base, z0, x1, base + 14, z1, p.wall(), p.foundation(), p.roof());
        quoins(b, x0, z0, x1, z1, base, base + 14, p);
        windows(b, x0, z0, x1, z1, base, base + 14);
        b.pitchedRoof(x0 - 2, z0 - 2, x1 + 2, z1 + 2, base + 15, 5, 0);

        b.fill(sx - 4, base + 1, z1, sx + 4, base + 6, z1, AIR);
        door(b, p, sx, base + 1, z1, Direction.SOUTH);
        b.fill(sx - 5, base + 10, z1, sx + 5, base + 13, z1, GOLD);

        // Platform, then a straight level track east-west to the district edge.
        b.fill(x0 - 8, base + 1, z1 + 1, x1 + 8, base + 1, z1 + 2, STONE);
        int trackZ = cz + 150;
        int xa = cx + 140;
        int xb = cx + 296;
        for (int x = xa; x <= xb; x++) {
            settle(b, x, trackZ, base, p);
        }
        b.fill(xa, base, trackZ - 1, xb, base, trackZ + 3, GRAVEL);
        for (int x = xa; x <= xb; x++) {
            b.put(x, base + 1, trackZ, RAIL);
            b.put(x, base + 1, trackZ + 2, RAIL);
        }
        b.fill(xb - 1, base + 1, trackZ - 1, xb, base + 2, trackZ + 3, p.accent());
        for (int x = xa + 8; x <= xb - 8; x += 24) {
            lamp(b, x, trackZ - 3, base + 1, p);
        }
        // The main passenger platform on the south side: five wide, edge three blocks off the
        // centreline, five clear blocks and a canopy at rail+6.
        b.fill(x0 - 4, base, trackZ + 3, x1 + 4, base, trackZ + 7, STONE);
        b.fill(x0 - 4, base + 1, trackZ + 3, x1 + 4, base + 1, trackZ + 7, p.accent());
        for (int x = x0; x <= x1; x += 12) {
            b.fill(x, base + 2, trackZ + 7, x, base + 6, trackZ + 7, p.accent());
            b.put(x, base + 7, trackZ + 5, p.light());
        }
        b.fill(x0 - 5, base + 7, trackZ + 2, x1 + 5, base + 7, trackZ + 8, p.roofSlab());

        signPost(b, p, x0 + 2, z1 + 2, base, Direction.SOUTH,
                "RAIL STATION", "PLATFORM 1");
    }

    private static void quay(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        for (int z = cz - 196; z <= cz + 146; z += 12) {
            if (quayGap(z - cz)) {
                continue;
            }
            b.put(cx + 296, ground + 1, z, p.foundation());
            b.put(cx + 296, ground + 2, z, p.accent());
        }
        for (int z = cz - 200; z <= cz + 150; z += 4) {
            if (quayGap(z - cz)) {
                continue;
            }
            b.put(cx + 300, ground + 1, z, p.rail());
        }
        for (int z = cz - 190; z <= cz + 140; z += 30) {
            if (quayGap(z - cz)) {
                continue;
            }
            lamp(b, cx + 294, z, ground, p);
        }
    }

    private static boolean quayGap(int dz) {
        return Math.abs(dz + 60) <= 4 || Math.abs(dz - 60) <= 4 || dz >= 66 && dz <= 86;
    }

    private static void dockBasin(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        basin(b, s, cx + 250, cz + 36, cx + 290, cz + 116, p);
        basin(b, s, cx + 290, cz + 66, cx + 301, cz + 86, p);
        for (int z = cz + 40; z <= cz + 112; z += 8) {
            b.put(cx + 248, s.at(cx + 248, z) + 1, z, p.foundation());
            if (z < cz + 66 || z > cz + 86) {
                b.put(cx + 292, s.at(cx + 292, z) + 1, z, p.foundation());
            }
        }
        for (int x = cx + 254; x <= cx + 286; x += 8) {
            b.put(x, s.at(x, cz + 34) + 1, cz + 34, p.foundation());
            b.put(x, s.at(x, cz + 118) + 1, cz + 118, p.foundation());
        }
    }

    private static void cranes(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int[] zs = {cz - 180, cz - 100, cz + 20, cz + 110};
        for (int z : zs) {
            b.fill(cx + 297, ground + 1, z, cx + 297, ground + 14, z, STONE);
            b.fill(cx + 291, ground + 14, z, cx + 307, ground + 14, z, p.accent());
            b.fill(cx + 291, ground + 13, z, cx + 291, ground + 14, z, p.accent());
            b.put(cx + 307, ground + 13, z, p.light());
        }
    }

    private static void lighthouse(StructureBuilder b, int x, int z, int base, Palette p) {
        for (int i = 0; i < 18; i++) {
            int r = i < 14 ? 3 : 2;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > r * r || d2 < (r - 1) * (r - 1)) {
                        continue;
                    }
                    b.put(x + dx, base + i, z + dz, (i / 4 & 1) == 0 ? WHITE : RED);
                }
            }
        }
        b.fill(x - 3, base + 18, z - 3, x + 3, base + 18, z + 3, p.accent());
        b.put(x, base + 19, z, p.light());
        b.put(x, base + 20, z, GOLD);
    }

    // ------------------------------------------------------------------ Recoleta

    private static void recoleta(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        cemetery(b, cx, cz, s, p);
        basilica(b, cx, cz, s, p);
        gardens(b, cx, cz, s, p);
        mansion(b, cx + 196, cz + 156, cx + 226, cz + 180, s, p, Side.S);
        mansion(b, cx + 248, cz + 156, cx + 278, cz + 180, s, p, Side.S);
    }

    private static void cemetery(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        // The perimeter, with a north gate and the crypt mausoleum inside.
        wallRun(b, s, cx + 186, cz + 196, cx + 235, cz + 196, p);
        wallRun(b, s, cx + 240, cz + 196, cx + 290, cz + 196, p);
        wallRun(b, s, cx + 186, cz + 292, cx + 290, cz + 292, p);
        wallRun(b, s, cx + 186, cz + 196, cx + 186, cz + 292, p);
        wallRun(b, s, cx + 290, cz + 196, cx + 290, cz + 292, p);

        // The 4-wide paths, kept one block clear of the walls so no levelling cuts them.
        paveOn(b, s, cx + 236, cz + 189, cx + 239, cz + 291, GRAVEL, p);
        paveOn(b, s, cx + 187, cz + 242, cx + 289, cz + 245, GRAVEL, p);
        paveOn(b, s, cx + 208, cz + 197, cx + 211, cz + 291, GRAVEL, p);
        paveOn(b, s, cx + 266, cz + 197, cx + 269, cz + 291, GRAVEL, p);
        paveOn(b, s, cx + 187, cz + 218, cx + 289, cz + 221, GRAVEL, p);
        paveOn(b, s, cx + 187, cz + 268, cx + 289, cz + 271, GRAVEL, p);

        // Authored rows of mausoleums; heights vary by position, never by RNG.
        int[][] tombs = {
                {192, 200}, {216, 200}, {244, 200}, {274, 200},
                {192, 224}, {216, 224}, {244, 224}, {274, 224},
                {192, 248}, {216, 248}, {256, 248}, {274, 248},
                {196, 274}, {244, 274}, {274, 274}
        };
        for (int[] t : tombs) {
            tomb(b, cx + t[0], cz + t[1], s.at(cx + t[0] + 4, cz + t[1] + 4), p);
        }

        // Cypress avenues between the rows.
        int[] cypressX = {205, 228, 262, 286};
        int[] cypressZ = {202, 230, 258, 286};
        for (int x : cypressX) {
            for (int z : cypressZ) {
                cypress(b, cx + x, cz + z, s.at(cx + x, cz + z));
            }
        }

        mausoleum(b, cx, cz, s, p);
        signPost(b, p, cx + 236, cz + 200, s.at(cx + 236, cz + 200), Direction.NORTH,
                "HONOUR THE DEAD", "NO PICNICS");
        signPost(b, p, cx + 240, cz + 190, s.at(cx + 240, cz + 190), Direction.NORTH,
                "RECOLETA", "CEMETERY", "KEEP TO PATHS");
    }

    private static void wallRun(StructureBuilder b, CitySurface s, int x0, int z0, int x1, int z1,
                                Palette p) {
        if (x0 == x1) {
            int az = Math.min(z0, z1);
            int bz = Math.max(z0, z1);
            for (int z = az; z <= bz; z++) {
                int y = s.at(x0, z);
                b.fill(x0, y + 1, z, x0, y + 3, z, p.weathered());
                b.put(x0, y + 4, z, p.accent());
            }
        } else {
            int ax = Math.min(x0, x1);
            int bx = Math.max(x0, x1);
            for (int x = ax; x <= bx; x++) {
                int y = s.at(x, z0);
                b.fill(x, y + 1, z0, x, y + 3, z0, p.weathered());
                b.put(x, y + 4, z0, p.accent());
            }
        }
    }

    private static void tomb(StructureBuilder b, int x, int z, int base, Palette p) {
        pad(b, x - 1, z - 1, x + 9, z + 9, base, p);
        shell(b, x, base, z, x + 8, base + 5, z + 8, MARBLE, MARBLE, MARBLE);
        b.pitchedRoof(x - 1, z - 1, x + 9, z + 9, base + 6, 3, 0);
        b.fill(x + 3, base + 1, z + 8, x + 5, base + 3, z + 8, AIR);
        b.put(x + 4, base + 4, z + 8, p.accent());
        b.put(x + 4, base + 7, z + 4, CALCITE);
        b.put(x + 4, base + 8, z + 4, CALCITE);
    }

    private static void mausoleum(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int x0 = cx + 230;
        int x1 = cx + 250;
        int z0 = cz + 252;
        int z1 = cz + 270;
        int base = s.at(cx + 240, cz + 260);
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, base, p);
        shell(b, x0, base, z0, x1, base + 6, z1, MARBLE, MARBLE, MARBLE);
        quoins(b, x0, z0, x1, z1, base, base + 6, p);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, base + 7, 4, 0);
        b.fill(x0 + 8, base + 1, z0, x1 - 8, base + 4, z0, AIR);
        door(b, p, cx + 240, base + 1, z0, Direction.NORTH);
        b.put(x0 + 4, base + 5, z0 - 1, p.light());
        b.put(x1 - 4, base + 5, z0 - 1, p.light());

        // Stairs down, the corridor and the chamber beneath the cemetery.
        int cryptY = base - 9;
        b.fill(cx + 238, base - 8, cz + 254, cx + 241, base + 5, cz + 262, AIR);
        for (int i = 0; i <= 8; i++) {
            int y = base - i;
            int z = cz + 262 - i;
            b.fill(cx + 238, y, z, cx + 241, y, z, MARBLE);
        }
        b.fill(cx + 236, cryptY, cz + 250, cx + 241, cryptY, cz + 255, GRAVEL);
        b.fill(cx + 236, cryptY + 1, cz + 250, cx + 241, cryptY + 4, cz + 255, AIR);
        b.fill(cx + 236, cryptY + 5, cz + 250, cx + 241, cryptY + 5, cz + 255, MARBLE);

        b.fill(cx + 226, cryptY, cz + 226, cx + 254, cryptY, cz + 250, GRAVEL);
        b.fill(cx + 226, cryptY + 1, cz + 226, cx + 254, cryptY + 4, cz + 250, AIR);
        b.fill(cx + 226, cryptY + 5, cz + 226, cx + 254, cryptY + 5, cz + 250, MARBLE);
        b.walls(cx + 226, cryptY + 1, cz + 226, cx + 254, cryptY + 4, cz + 250, MARBLE);
        b.fill(cx + 236, cryptY + 1, cz + 250, cx + 241, cryptY + 4, cz + 250, AIR);

        // Sarcophagi, bones, the chests and the two readable stones.
        b.fill(cx + 230, cryptY + 1, cz + 232, cx + 234, cryptY + 2, cz + 236, MARBLE);
        b.fill(cx + 246, cryptY + 1, cz + 232, cx + 250, cryptY + 2, cz + 236, MARBLE);
        for (int x = cx + 228; x <= cx + 252; x += 4) {
            b.fill(x, cryptY + 1, cz + 228, x, cryptY + 2, cz + 228, BONE);
            b.put(x, cryptY + 3, cz + 228, SKULL);
        }
        b.fill(cx + 228, cryptY + 1, cz + 246, cx + 231, cryptY + 3, cz + 246, BOOKS);
        b.chest(cx + 230, cryptY + 1, cz + 240, SiteLoot.RELIC);
        b.chest(cx + 250, cryptY + 1, cz + 244, SiteLoot.CULT);
        b.chest(cx + 236, cryptY + 1, cz + 246, SiteLoot.COMMON);
        b.put(cx + 228, cryptY + 4, cz + 228, p.light());
        b.put(cx + 252, cryptY + 4, cz + 228, p.light());

        undergroundSign(b, cx + 232, cryptY + 1, cz + 230, Direction.SOUTH,
                "STOLEN TANGO", "SOLD AT PORT", "TAKE IT OR", "TELL THE LAW");
        undergroundSign(b, cx + 248, cryptY + 1, cz + 230, Direction.SOUTH,
                "FAMILY SECRET", "THE HEIRESS", "IS SAFE", "SAY NOTHING");
        signPost(b, p, cx + 242, cz + 251, base, Direction.NORTH,
                "FAMILY CRYPT", "KEEP OUT", "SHE IS HERE");
    }

    private static void basilica(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int x0 = cx + 150;
        int x1 = cx + 182;
        int z0 = cz + 200;
        int z1 = cz + 248;
        int base = s.at(cx + 166, cz + 224);
        pad(b, x0 - 2, z0 - 2, x1 + 2, z1 + 2, base, p);
        paveAt(b, x0 - 2, cz + 190, x1 + 2, z0 - 1, base, p.ground(), p);

        shell(b, x0, base, z0, x1, base + 22, z1, MARBLE, MARBLE, MARBLE);
        quoins(b, x0, z0, x1, z1, base, base + 22, p);
        windows(b, x0, z0, x1, z1, base, base + 22);
        b.pitchedRoof(x0 - 2, z0 - 2, x1 + 2, z1 + 2, base + 23, 8, 1);

        // The north portico and forecourt.
        for (int x = x0 + 4; x <= x1 - 4; x += 6) {
            b.fill(x, base + 1, z0 - 3, x, base + 12, z0 - 3, p.accent());
        }
        b.fill(x0 + 2, base + 13, z0 - 4, x1 - 2, base + 13, z0 - 2, p.accent());
        pedimentNorth(b, cx + 166, z0 - 4, base + 14, 12, p);
        b.fill(cx + 160, base + 1, z0, cx + 172, base + 5, z0, AIR);
        door(b, p, cx + 166, base + 1, z0, Direction.NORTH);

        towerAt(b, x0 + 7, z1 - 7, 4, 30, base, p);
        towerAt(b, x1 - 7, z1 - 7, 4, 30, base, p);
        drum(b, cx + 166, cz + 224, base + 23, 9, 5, p);
        dome(b, cx + 166, cz + 224, base + 28, 9, 7, MARBLE, p.accent());

        frontSign(b, p, x0, z0, x1, z1, base, Side.N,
                "BASILICA", "OUR LADY", "PILGRIMS");
    }

    private static void gardens(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        paveOn(b, s, cx + 150, cz + 252, cx + 184, cz + 292, GRASS, p);
        fountain(b, cx + 166, cz + 270, s.at(cx + 166, cz + 270), p, 4);
        for (int x = cx + 154; x <= cx + 180; x += 6) {
            cypress(b, x, cz + 258, s.at(x, cz + 258));
            cypress(b, x, cz + 286, s.at(x, cz + 286));
        }
        bench(b, cx + 160, cz + 264, s.at(cx + 160, cz + 264), p);
        bench(b, cx + 172, cz + 264, s.at(cx + 172, cz + 264), p);
    }

    private static void mansion(StructureBuilder b, int x0, int z0, int x1, int z1,
                                CitySurface s, Palette p, Side front) {
        int base = s.at((x0 + x1) / 2, (z0 + z1) / 2);
        house(b, x0, z0, x1, z1, base, 14, p, front, 12);
        b.put(x0 + 2, base + 15, z1 + 1, p.light());
        b.put(x1 - 2, base + 15, z1 + 1, p.light());
    }

    // ------------------------------------------------------------------ arrival

    private static void arrival(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int base = s.at(cx, cz + 319);
        for (int side = -1; side <= 1; side += 2) {
            int ax = side < 0 ? cx - 18 : cx + 9;
            int bx = side < 0 ? cx - 9 : cx + 18;
            pad(b, ax, cz + 312, bx, cz + 326, base, p);
            b.fill(ax, base + 1, cz + 312, bx, base + 14, cz + 326, p.wall());
            b.fill(ax, base + 14, cz + 312, bx, base + 14, cz + 326, p.accent());
            b.crenellations(ax, cz + 312, bx, cz + 326, base + 15);
            b.window(ax + 3, base + 5, cz + 326, 5, 3, false);
            b.put(ax + 1, base + 8, cz + 312, p.light());
        }
        b.fill(cx - 9, base + 10, cz + 318, cx + 9, base + 10, cz + 318, p.rail());
        b.fill(cx - 9, base + 11, cz + 318, cx - 8, base + 11, cz + 318, p.accent());
        b.fill(cx + 8, base + 11, cz + 318, cx + 9, base + 11, cz + 318, p.accent());
        b.put(cx, base + 9, cz + 318, p.light());
        statue(b, cx - 24, cz + 330, s.at(cx - 24, cz + 330), p);
        statue(b, cx + 24, cz + 330, s.at(cx + 24, cz + 330), p);

        signPost(b, p, cx + 8, cz + 306, s.at(cx + 8, cz + 306), Direction.SOUTH,
                "PALACE", "TANGO HALLS", "PORT", "RECOLETA");
        signPost(b, p, cx - 24, cz + 306, s.at(cx - 24, cz + 306), Direction.SOUTH,
                "CENTRO", "CITY NOTICES", "HOTEL", "STATION");
    }

    // ------------------------------------------------------------------ greenery

    private static void greenery(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        for (int x = -300; x <= -180; x += 40) {
            tree(b, cx + x, cz - 10, s.at(cx + x, cz - 10), p);
            tree(b, cx + x, cz + 9, s.at(cx + x, cz + 9), p);
        }
        for (int z = 20; z <= 160; z += 40) {
            tree(b, cx - 130, cz + z, s.at(cx - 130, cz + z), p);
        }
    }

    // ------------------------------------------------------------------ readable signs

    private static void signs(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        signPost(b, p, cx - 240, cz + 8, s.at(cx - 240, cz + 8), Direction.SOUTH,
                "TANGO HALL", "MILONGA", "TONIGHT");
        signPost(b, p, cx - 282, cz - 16, s.at(cx - 282, cz - 16), Direction.SOUTH,
                "THEATRE", "TANGO OPERA", "BOX OFFICE");
        signPost(b, p, cx - 250, cz + 122, s.at(cx - 250, cz + 122), Direction.NORTH,
                "APOTHECARY", "REMEDIES", "HERBS & SALTS");
        signPost(b, p, cx - 80, cz - 58, s.at(cx - 80, cz - 58), Direction.SOUTH,
                "HOTEL", "ROOMS & BEDS", "KEEPER INSIDE");
        signPost(b, p, cx - 40, cz + 119, s.at(cx - 40, cz + 119), Direction.NORTH,
                "SHOP", "DRY GOODS", "OPEN");
        signPost(b, p, cx + 18, cz + 60, s.at(cx + 18, cz + 60), Direction.WEST,
                "CENTRO BANK", "DEPOSITS", "VAULTS");
        signPost(b, p, cx + 210, cz - 20, s.at(cx + 210, cz - 20), Direction.WEST,
                "CUSTOMS", "STOLEN TANGO", "REPORT IT HERE");
        signPost(b, p, cx + 296, cz + 20, s.at(cx + 296, cz + 20), Direction.WEST,
                "PORT WARNING", "DEEP WATER", "NO SWIMMING");
        signPost(b, p, cx + 182, cz + 224, s.at(cx + 182, cz + 224), Direction.EAST,
                "BASILICA", "OUR LADY", "PILGRIMS");
        signPost(b, p, cx + 166, cz + 190, s.at(cx + 166, cz + 190), Direction.NORTH,
                "BASILICA", "OPEN DAWN", "NO GUIDES");
    }

    // ------------------------------------------------------------------ building helpers

    private static void shell(StructureBuilder b, int x0, int y0, int z0, int x1, int y1, int z1,
                              BlockState wall, BlockState floor, BlockState ceiling) {
        b.fill(x0, y0, z0, x1, y0, z1, floor);
        b.fill(x0, y1, z0, x1, y1, z1, ceiling);
        if (y1 - 1 >= y0 + 1) {
            b.walls(x0, y0 + 1, z0, x1, y1 - 1, z1, wall);
        }
    }

    private static void house(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                              int height, Palette p, Side doorSide, int doorOffset) {
        pad(b, x0, z0, x1, z1, ground, p);
        int top = ground + height;
        shell(b, x0, ground, z0, x1, top, z1, p.wall(), p.foundation(), p.roof());
        quoins(b, x0, z0, x1, z1, ground, top, p);
        windows(b, x0, z0, x1, z1, ground, top);
        balcony(b, x0, z0, x1, z1, ground + 4, p);
        mansard(b, x0, z0, x1, z1, top, p);
        doorFor(b, p, x0, z0, x1, z1, ground + 1, doorSide, doorOffset);
    }

    private static void windows(StructureBuilder b, int x0, int z0, int x1, int z1, int y0,
                                int y1) {
        for (int y = y0 + 2; y <= y1 - 2; y += 3) {
            for (int x = x0 + 2; x <= x1 - 2; x += 3) {
                b.window(x, y, z0, 2, 2, true);
                b.window(x, y, z1, 2, 2, true);
            }
            for (int z = z0 + 2; z <= z1 - 2; z += 3) {
                b.window(x0, y, z, 2, 1, true);
                b.window(x1, y, z, 2, 1, true);
            }
        }
    }

    private static void quoins(StructureBuilder b, int x0, int z0, int x1, int z1, int y0, int y1,
                               Palette p) {
        for (int y = y0; y <= y1; y++) {
            b.put(x0, y, z0, p.accent());
            b.put(x1, y, z0, p.accent());
            b.put(x0, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
    }

    private static void balcony(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                                Palette p) {
        for (int x = x0; x <= x1; x++) {
            b.put(x, y, z0 - 1, p.rail());
            b.put(x, y, z1 + 1, p.rail());
        }
        for (int z = z0; z <= z1; z++) {
            b.put(x0 - 1, y, z, p.rail());
            b.put(x1 + 1, y, z, p.rail());
        }
    }

    private static void mansard(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                                Palette p) {
        int axis = (x1 - x0) >= (z1 - z0) ? 0 : 1;
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y, 5, axis);
        for (int x = x0 + 2; x <= x1 - 2; x += 5) {
            b.put(x, y + 1, z1 + 1, p.frame());
            b.put(x, y + 2, z1 + 1, p.window());
            b.put(x, y + 1, z0 - 1, p.frame());
            b.put(x, y + 2, z0 - 1, p.window());
        }
    }

    private static void doorFor(StructureBuilder b, Palette p, int x0, int z0, int x1, int z1,
                                int y, Side side, int offset) {
        switch (side) {
            case S -> door(b, p, x0 + offset, y, z1, Direction.SOUTH);
            case N -> door(b, p, x0 + offset, y, z0, Direction.NORTH);
            case W -> door(b, p, x0, y, z0 + offset, Direction.WEST);
            case E -> door(b, p, x1, y, z0 + offset, Direction.EAST);
        }
    }

    private static void door(StructureBuilder b, Palette p, int x, int y, int z, Direction facing) {
        b.put(x, y, z, AIR);
        b.put(x, y + 1, z, AIR);
        BlockState lower = p.door()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        b.put(x, y, z, lower);
        b.put(x, y + 1, z,
                lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
    }

    private static void signPost(StructureBuilder b, Palette p, int x, int z, int y,
                                 Direction facing, String... lines) {
        b.put(x, y, z, p.accent());
        CityPlanSupport.sign(b, x, y + 1, z, facing, lines);
    }

    private static void frontSign(StructureBuilder b, Palette p, int x0, int z0, int x1, int z1,
                                  int ground, Side front, String... lines) {
        switch (front) {
            case S -> signPost(b, p, x0 + 1, z1 + 1, ground, Direction.SOUTH, lines);
            case N -> signPost(b, p, x0 + 1, z0 - 1, ground, Direction.NORTH, lines);
            case W -> signPost(b, p, x0 - 1, z0 + 1, ground, Direction.WEST, lines);
            case E -> signPost(b, p, x1 + 1, z0 + 1, ground, Direction.EAST, lines);
        }
    }

    private static void undergroundSign(StructureBuilder b, int x, int y, int z, Direction facing,
                                        String... lines) {
        b.put(x, y - 1, z, MARBLE);
        CityPlanSupport.sign(b, x, y, z, facing, lines);
    }

    private static void questBoard(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 5, ground + 1, z, x - 5, ground + 4, z, p.accent());
        b.fill(x + 5, ground + 1, z, x + 5, ground + 4, z, p.accent());
        b.fill(x - 5, ground + 2, z, x + 5, ground + 4, z, DARK_WOOD);
        b.put(x - 3, ground + 3, z, WHITE);
        b.put(x + 3, ground + 3, z, RED);
        signPost(b, p, x, z + 2, ground, Direction.SOUTH,
                "CITY NOTICES", "MISSING HEIRESS", "STOLEN TANGO", "ASK INSIDE");
    }

    private static void cafe(StructureBuilder b, int x0, int z0, int x1, int z1, CitySurface s,
                             Palette p, Side front) {
        int base = s.at((x0 + x1) / 2, (z0 + z1) / 2);
        house(b, x0, z0, x1, z1, base, 9, p, front, 7);
        int[] terrace = switch (front) {
            case W -> new int[]{x0 - 4, z0, x0 - 1, z1};
            case E -> new int[]{x1 + 1, z0, x1 + 4, z1};
            case N -> new int[]{x0, z0 - 4, x1, z0 - 1};
            case S -> new int[]{x0, z1 + 1, x1, z1 + 4};
        };
        paveAt(b, terrace[0], terrace[1], terrace[2], terrace[3], base, p.ground(), p);
        for (int x = terrace[0] + 1; x <= terrace[2] - 1; x += 3) {
            for (int z = terrace[1] + 1; z <= terrace[3] - 1; z += 3) {
                b.put(x, base + 1, z, p.foundation());
                b.put(x, base + 2, z, p.roofSlab());
            }
        }
        switch (front) {
            case W -> {
                for (int z = z0; z <= z1; z++) {
                    b.put(x0 - 1, base + 4, z, (z & 1) == 0 ? RED : WHITE);
                }
            }
            case E -> {
                for (int z = z0; z <= z1; z++) {
                    b.put(x1 + 1, base + 4, z, (z & 1) == 0 ? RED : WHITE);
                }
            }
            case N -> {
                for (int x = x0; x <= x1; x++) {
                    b.put(x, base + 4, z0 - 1, (x & 1) == 0 ? RED : WHITE);
                }
            }
            case S -> {
                for (int x = x0; x <= x1; x++) {
                    b.put(x, base + 4, z1 + 1, (x & 1) == 0 ? RED : WHITE);
                }
            }
        }
        frontSign(b, p, x0, z0, x1, z1, base, front, "CAFE", "TANGO", "OPEN LATE");
    }

    private static void laBoca(StructureBuilder b, int x0, int z0, int x1, int z1, CitySurface s,
                               Palette p, BlockState paint) {
        int base = s.at((x0 + x1) / 2, (z0 + z1) / 2);
        pad(b, x0, z0, x1, z1, base, p);
        shell(b, x0, base, z0, x1, base + 9, z1, paint, p.foundation(), p.roof());
        quoins(b, x0, z0, x1, z1, base, base + 9, p);
        windows(b, x0, z0, x1, z1, base, base + 9);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, base + 10, 4, 0);
        doorFor(b, p, x0, z0, x1, z1, base + 1, Side.S, 6);
        b.put(x0 + 2, base + 6, z0 - 1, YELLOW);
        b.put(x1 - 2, base + 6, z0 - 1, WHITE);
    }

    private static void towerAt(StructureBuilder b, int cx, int cz, int half, int height, int ground,
                                Palette p) {
        int x0 = cx - half;
        int x1 = cx + half;
        int z0 = cz - half;
        int z1 = cz + half;
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p);
        shell(b, x0, ground, z0, x1, ground + height, z1, p.wall(), p.foundation(), p.roof());
        quoins(b, x0, z0, x1, z1, ground, ground + height, p);
        windows(b, x0, z0, x1, z1, ground, ground + height);
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + height + 1);
        b.put(cx, ground + height + 2, cz, p.accent());
        b.put(cx, ground + height + 3, cz, p.light());
    }

    private static void drum(StructureBuilder b, int cx, int cz, int y0, int radius, int height,
                             Palette p) {
        for (int i = 0; i < height; i++) {
            int y = y0 + i;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > radius * radius || d2 < (radius - 1) * (radius - 1)) {
                        continue;
                    }
                    boolean column = Math.floorMod(dx + dz, 4) == 0;
                    b.put(cx + dx, y, cz + dz, column ? p.accent() : p.wall());
                }
            }
        }
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > radius * radius || d2 < (radius - 1) * (radius - 1)) {
                    continue;
                }
                if (Math.floorMod(dx, 3) == 0) {
                    b.put(cx + dx, y0 + height / 2, cz + dz, p.window());
                }
            }
        }
    }

    private static void dome(StructureBuilder b, int cx, int cz, int baseY, int radius, int height,
                             BlockState shell, BlockState trim) {
        for (int i = 0; i < height; i++) {
            int r = radius - i * radius / height;
            if (r <= 0) {
                b.put(cx, baseY + i, cz, trim);
                continue;
            }
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > r * r) {
                        continue;
                    }
                    boolean edge = d2 > (r - 1) * (r - 1);
                    b.put(cx + dx, baseY + i, cz + dz, edge ? trim : shell);
                }
            }
        }
    }

    private static void pedimentSouth(StructureBuilder b, int cx, int z, int baseY, int half,
                                      Palette p) {
        for (int i = 0; i <= half / 2; i++) {
            int w = half - i * 2;
            b.fill(cx - w, baseY + i, z, cx + w, baseY + i, z, p.accent());
        }
    }

    private static void pedimentNorth(StructureBuilder b, int cx, int z, int baseY, int half,
                                      Palette p) {
        pedimentSouth(b, cx, z, baseY, half, p);
    }

    private static void pedimentEast(StructureBuilder b, int x, int cz, int baseY, int half,
                                     Palette p) {
        for (int i = 0; i <= half / 2; i++) {
            int w = half - i * 2;
            b.fill(x, baseY + i, cz - w, x, baseY + i, cz + w, p.accent());
        }
    }

    private static void pedimentWest(StructureBuilder b, int x, int cz, int baseY, int half,
                                     Palette p) {
        pedimentEast(b, x, cz, baseY, half, p);
    }

    // ------------------------------------------------------------------ small props

    private static void statue(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 1, ground, z - 1, x + 1, ground, z + 1, p.foundation());
        b.fill(x, ground + 1, z, x, ground + 4, z, CALCITE);
        b.put(x, ground + 5, z, CALCITE);
        b.put(x - 1, ground + 4, z, CALCITE);
        b.put(x + 1, ground + 4, z, CALCITE);
    }

    private static void bench(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x - 1, ground + 1, z, p.roofSlab());
        b.put(x, ground + 1, z, p.roofSlab());
        b.put(x + 1, ground + 1, z, p.roofSlab());
        b.put(x - 1, ground, z, p.accent());
        b.put(x + 1, ground, z, p.accent());
    }

    private static void lamp(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x, ground + 1, z, x, ground + 3, z, p.accent());
        b.put(x, ground + 4, z, p.light());
    }

    private static void fountain(StructureBuilder b, int cx, int cz, int ground, Palette p, int r) {
        b.fill(cx - r, ground, cz - r, cx + r, ground, cz + r, p.foundation());
        b.walls(cx - r, ground + 1, cz - r, cx + r, ground + 1, cz + r, p.foundation());
        b.fill(cx - r + 1, ground + 1, cz - r + 1, cx + r - 1, ground + 1, cz + r - 1, WATER);
        b.fill(cx, ground + 2, cz, cx, ground + 4, cz, p.accent());
        b.put(cx, ground + 5, cz, WATER);
    }

    private static void tree(StructureBuilder b, int x, int z, int base, Palette p) {
        int h = 5 + Math.floorMod(x + z, 3);
        b.fill(x, base + 1, z, x, base + h, z, TRUNK);
        for (int dy = -1; dy <= 2; dy++) {
            int r = dy <= 0 ? 2 : 1;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) > r + 1) {
                        continue;
                    }
                    b.put(x + dx, base + h + dy, z + dz, LEAVES);
                }
            }
        }
    }

    private static void cypress(StructureBuilder b, int x, int z, int base) {
        int h = 7 + Math.floorMod(x * 3 + z, 3);
        b.fill(x, base + 1, z, x, base + 3, z, TRUNK);
        for (int dy = 2; dy <= h; dy++) {
            int r = dy < h - 3 ? 1 : 0;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) > r) {
                        continue;
                    }
                    b.put(x + dx, base + dy, z + dz, CYPRESS);
                }
            }
        }
    }
}
