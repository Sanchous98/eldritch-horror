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
 * Mexico City — the authored city over the drained lake: the Metropolitan Cathedral on the Zócalo,
 * the Palacio Nacional and its arcades, the market lanes of La Merced, the canals and chinampas of
 * Xochimilco, the stepped pyramid and ball court, the Aztec tunnel under the museum, and the
 * Reforma boulevard with its hotels and cafés.
 *
 * <p>Every block is written deliberately through {@link StructureBuilder}; there is no layout RNG
 * and no chunk-local read, so every chunk that overlaps the city writes the same blocks. The
 * shared pass has already levelled and paved the district; this plan lays its own plinths, streets,
 * gardens and water on top of that surface.
 */
public final class MexicoCityPlan implements CityPlan {

    /** Authored half-extent: 680 blocks across, matching the brief. */
    private static final int HALF_EXTENT = 340;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState CLAY = Blocks.CLAY.defaultBlockState();
    private static final BlockState MUD = Blocks.MUD_BRICKS.defaultBlockState();
    private static final BlockState MOSS = Blocks.MOSS_BLOCK.defaultBlockState();
    private static final BlockState AZALEA = Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState();
    private static final BlockState JACARANDA = Blocks.CHERRY_LEAVES.defaultBlockState();
    private static final BlockState JACARANDA_LOG = Blocks.CHERRY_LOG.defaultBlockState();
    private static final BlockState CYPRESS_LOG = Blocks.DARK_OAK_LOG.defaultBlockState();
    private static final BlockState CYPRESS_LEAF = Blocks.DARK_OAK_LEAVES.defaultBlockState();
    private static final BlockState MARIGOLD = Blocks.ORANGE_TULIP.defaultBlockState();
    private static final BlockState GRASS_TUFT = Blocks.SHORT_GRASS.defaultBlockState();
    private static final BlockState FERN = Blocks.FERN.defaultBlockState();
    private static final BlockState DEAD_BUSH = Blocks.DEAD_BUSH.defaultBlockState();
    private static final BlockState CACTUS = Blocks.CACTUS.defaultBlockState();
    private static final BlockState SUGAR_CANE = Blocks.SUGAR_CANE.defaultBlockState();
    private static final BlockState WHEAT = Blocks.WHEAT.defaultBlockState();
    private static final BlockState FARMLAND = Blocks.FARMLAND.defaultBlockState();
    private static final BlockState HAY = Blocks.HAY_BLOCK.defaultBlockState();
    private static final BlockState BOOKS = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState CAMPFIRE = Blocks.CAMPFIRE.defaultBlockState();
    private static final BlockState LECTERN = Blocks.LECTERN.defaultBlockState();
    private static final BlockState BARREL = Blocks.BARREL.defaultBlockState();
    private static final BlockState BELL = Blocks.BELL.defaultBlockState();
    private static final BlockState LILY = Blocks.LILY_PAD.defaultBlockState();
    private static final BlockState SEA_LANTERN = Blocks.SEA_LANTERN.defaultBlockState();
    private static final BlockState GOLD = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final BlockState COBWEB = Blocks.COBWEB.defaultBlockState();
    private static final BlockState SMOOTH = Blocks.SMOOTH_STONE.defaultBlockState();
    private static final BlockState ANDESITE = Blocks.POLISHED_ANDESITE.defaultBlockState();
    private static final BlockState CARVED = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
    private static final BlockState STONE_SLAB = Blocks.STONE_BRICK_SLAB.defaultBlockState();
    private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
    private static final BlockState CUT_SAND = Blocks.CUT_SANDSTONE.defaultBlockState();
    private static final BlockState MOSSY = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
    private static final BlockState CRACKED = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
    private static final BlockState WHITE_WOOL = Materials.wool(DyeColor.WHITE);
    private static final BlockState RED_WOOL = Materials.wool(DyeColor.RED);
    private static final BlockState GREEN_WOOL = Materials.wool(DyeColor.GREEN);
    private static final BlockState ORANGE_WOOL = Materials.wool(DyeColor.ORANGE);
    private static final BlockState BLUE_GLAZED = Materials.glazed(DyeColor.LIGHT_BLUE);
    private static final BlockState RED_BED = Blocks.BED.pick(DyeColor.RED).defaultBlockState();
    private static final BlockState WHITE_CARPET = Blocks.CARPET.pick(DyeColor.WHITE).defaultBlockState();
    private static final BlockState RAIL_EW = Blocks.RAIL.defaultBlockState()
            .setValue(BlockStateProperties.RAIL_SHAPE, RailShape.EAST_WEST);

    @Override
    public String cityId() {
        return "mexico_city";
    }

    @Override
    public int halfExtent() {
        return HALF_EXTENT;
    }

    @Override
    public void build(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        city(b, cx, cz, ground, p);
    }

    @Override
    public void build(StructureBuilder b, CityPlanContext ctx, Palette p) {
        city(b, ctx.cx(), ctx.cz(), ctx.ground(), p);
        railStation(b, ctx.cx(), ctx.cz(), p, ctx.surface());
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.LANDMARK, 0, 0, 320, "gate"),
                new CityPoi("arrival_sign", CityPoi.Kind.SIGN, 18, 1, 304, "districts"),
                new CityPoi("city_notices", CityPoi.Kind.SIGN, 20, 1, 308, "quests"),
                new CityPoi("guide", CityPoi.Kind.NPC, 10, 0, 304, "guide"),
                new CityPoi("zocalo_quest_board", CityPoi.Kind.QUEST_BOARD, 0, 1, -112,
                        "mexico_city_missing_curator;mexico_city_stolen_idol"),
                new CityPoi("zocalo_sign", CityPoi.Kind.SIGN, -20, 1, -100, "zocalo"),
                new CityPoi("cathedral", CityPoi.Kind.LANDMARK, 0, 0, -200, "cathedral"),
                new CityPoi("sacristan", CityPoi.Kind.NPC, -12, 0, -176, "sacristan"),
                new CityPoi("palacio_nacional", CityPoi.Kind.LANDMARK, 64, 0, -200,
                        "palace;arcades"),
                new CityPoi("guard", CityPoi.Kind.NPC, 60, 0, -180, "guard"),
                new CityPoi("hotel", CityPoi.Kind.INN, -80, 0, -60, "hotel_clerk"),
                new CityPoi("hotel_clerk", CityPoi.Kind.NPC, -80, 0, -72, "hotel_clerk"),
                new CityPoi("market_shop", CityPoi.Kind.SHOP, -240, 0, -60, "shopkeeper"),
                new CityPoi("shopkeeper", CityPoi.Kind.NPC, -240, 0, -72, "shopkeeper"),
                new CityPoi("market_sign", CityPoi.Kind.SIGN, -240, 1, -52, "market"),
                new CityPoi("curandero", CityPoi.Kind.HEALER, -240, 0, 120, "curandero"),
                new CityPoi("curandero_npc", CityPoi.Kind.NPC, -240, 0, 112, "curandero"),
                new CityPoi("ruins", CityPoi.Kind.LANDMARK, 0, 0, 60, "pyramid;ball_court"),
                new CityPoi("ruins_sign", CityPoi.Kind.SIGN, 0, 1, 90, "ruins"),
                new CityPoi("museum", CityPoi.Kind.LANDMARK, 0, 0, 120, "museum"),
                new CityPoi("museum_curator", CityPoi.Kind.NPC, 0, 0, 120,
                        "mexico_city_missing_curator"),
                new CityPoi("aztec_tunnel", CityPoi.Kind.SECRET, 0, -7, 120, "shrine;lore"),
                new CityPoi("aztec_shrine", CityPoi.Kind.SECRET, 0, -7, 70, "altar;idol"),
                new CityPoi("tunnel_warning", CityPoi.Kind.SIGN, 8, 1, 138, "tunnel_warning"),
                new CityPoi("canal_boatman", CityPoi.Kind.NPC, 240, 0, 0,
                        "mexico_city_stolen_idol"),
                new CityPoi("canals_sign", CityPoi.Kind.SIGN, 240, 1, 10, "canals"),
                new CityPoi("chinampas", CityPoi.Kind.LANDMARK, 290, 0, 40, "chinampas;boats"),
                new CityPoi("canal_port", CityPoi.Kind.PORT, 316, 0, -40, "canal_basin;chinampas"),
                new CityPoi("port_sign", CityPoi.Kind.SIGN, 312, 1, -20, "port"),
                new CityPoi("canal_warning", CityPoi.Kind.SIGN, 240, 1, -58, "canal_warning"),
                new CityPoi("rail_station", CityPoi.Kind.STATION, 120, 0, 305,
                        "station;east_west"),
                new CityPoi("station_sign", CityPoi.Kind.SIGN, 120, 0, 302, "station"),
                new CityPoi("reforma", CityPoi.Kind.LANDMARK, 230, 0, 240, "boulevard;hotels"),
                new CityPoi("reforma_sign", CityPoi.Kind.SIGN, 230, 1, 190, "reforma"),
                new CityPoi("reforma_hotel", CityPoi.Kind.INN, 199, 0, 240, "reforma_clerk"),
                new CityPoi("reforma_clerk", CityPoi.Kind.NPC, 199, 0, 230, "reforma_clerk"),
                new CityPoi("alameda", CityPoi.Kind.LANDMARK, 230, 0, 163, "park"));
    }

    // ------------------------------------------------------------------ whole city

    private static void city(StructureBuilder b, int cx, int cz, int g, Palette p) {
        plazas(b, cx, cz, g, p);
        streets(b, cx, cz, g, p);
        water(b, cx, cz, g, p);
        arrivalGate(b, cx, cz, g, p);
        cathedral(b, cx, cz, g, p);
        zocalo(b, cx, cz, g, p);
        palace(b, cx, cz, g, p);
        arcades(b, cx, cz, g, p);
        southRow(b, cx, cz, g, p);
        market(b, cx, cz, g, p);
        canals(b, cx, cz, g, p);
        alameda(b, cx, cz, g, p);
        ruins(b, cx, cz, g, p);
        museum(b, cx, cz, g, p);
        tunnel(b, cx, cz, g, p);
        reforma(b, cx, cz, g, p);
        avenueLamps(b, cx, cz, g, p);
        b.marker("mexico_city_center", cx, g + 1, cz);
    }

    private static void plazas(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // The Zócalo, the ruins plaza, the museum forecourt and the arrival apron.
        b.fill(cx - 110, g, cz - 168, cx + 110, g, cz - 96, p.ground());
        for (int x = -110; x <= 110; x += 22) {
            b.fill(cx + x, g, cz - 168, cx + x, g, cz - 96, p.foundation());
        }
        b.fill(cx - 110, g, cz - 168, cx + 110, g, cz - 168, p.foundation());
        b.fill(cx - 110, g, cz - 96, cx + 110, g, cz - 96, p.foundation());

        b.fill(cx - 40, g, cz, cx + 40, g, cz + 90, p.ground());
        b.fill(cx - 40, g, cz + 90, cx + 40, g, cz + 96, p.ground());
        b.fill(cx - 24, g, cz + 104, cx + 24, g, cz + 140, p.ground());

        b.fill(cx - 24, g, cz + 300, cx + 24, g, cz + 338, p.ground());
        b.fill(cx - 24, g, cz + 300, cx + 24, g, cz + 300, p.foundation());
        b.fill(cx - 24, g, cz + 338, cx + 24, g, cz + 338, p.foundation());

        greenWay(b, cx, cz, g, p);
    }

    private static void greenWay(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx + 136, g, cz - 220, cx + 148, g, cz + 160, GRASS);
        for (int z = cz - 210; z <= cz + 150; z += 40) {
            tree(b, cx + 142, z, g, 1);
            b.put(cx + 138, g + 1, z, MARIGOLD);
        }
        b.fill(cx + 130, g, cz - 240, cx + 133, g, cz + 180, p.ground());
    }

    private static void streets(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // The arrival avenue, the two cross streets, and the market's six-wide lanes.
        CityPlanSupport.path(b, cx, cz - 96, cx, cz + 290, 14, g, p.ground());
        CityPlanSupport.path(b, cx, cz + 290, cx, cz + 300, 14, g, p.ground());
        CityPlanSupport.path(b, cx - 330, cz - 60, cx + 240, cz - 60, 14, g, p.ground());
        CityPlanSupport.path(b, cx + 259, cz - 60, cx + 330, cz - 60, 14, g, p.ground());
        CityPlanSupport.path(b, cx - 330, cz + 180, cx + 330, cz + 180, 12, g, p.ground());

        for (int x : new int[]{-300, -250, -200, -150}) {
            CityPlanSupport.path(b, cx + x, cz - 230, cx + x, cz + 170, 6, g, p.ground());
        }
        for (int z : new int[]{-190, -130, 10, 80, 150}) {
            CityPlanSupport.path(b, cx - 320, cz + z, cx - 146, cz + z, 6, g, p.ground());
        }

        // Central side streets and the lanes around the Zócalo.
        CityPlanSupport.path(b, cx - 130, cz - 240, cx - 130, cz + 180, 8, g, p.ground());
        CityPlanSupport.path(b, cx - 122, cz - 300, cx - 122, cz - 96, 6, g, p.ground());
        CityPlanSupport.path(b, cx + 122, cz - 300, cx + 122, cz - 96, 6, g, p.ground());
        CityPlanSupport.path(b, cx - 120, cz - 290, cx + 120, cz - 290, 8, g, p.ground());

        // The ruins district's lanes and the paths to pyramid and ball court.
        CityPlanSupport.path(b, cx - 120, cz - 60, cx - 120, cz + 180, 6, g, p.ground());
        CityPlanSupport.path(b, cx + 120, cz - 60, cx + 120, cz + 180, 6, g, p.ground());
        CityPlanSupport.path(b, cx - 120, cz + 90, cx + 120, cz + 90, 6, g, p.ground());
        CityPlanSupport.path(b, cx - 120, cz + 160, cx + 120, cz + 160, 6, g, p.ground());
        CityPlanSupport.path(b, cx - 40, cz + 60, cx - 8, cz + 60, 6, g, p.ground());
        CityPlanSupport.path(b, cx + 8, cz + 60, cx + 50, cz + 60, 6, g, p.ground());

        // The canal quays, west lanes and the cross streets of the water district.
        CityPlanSupport.path(b, cx + 238, cz - 220, cx + 238, cz + 150, 5, g, p.ground());
        CityPlanSupport.path(b, cx + 261, cz - 220, cx + 261, cz + 150, 5, g, p.ground());
        for (int x : new int[]{150, 190, 225}) {
            CityPlanSupport.path(b, cx + x, cz - 220, cx + x, cz + 150, 6, g, p.ground());
        }
        for (int z : new int[]{-160, 40, 120}) {
            CityPlanSupport.path(b, cx + 140, cz + z, cx + 236, cz + z, 6, g, p.ground());
        }

        // Reforma: the boulevard, its side lanes and the two cross lanes.
        CityPlanSupport.path(b, cx + 230, cz + 186, cx + 230, cz + 300, 12, g, p.ground());
        for (int x : new int[]{155, 300}) {
            CityPlanSupport.path(b, cx + x, cz + 186, cx + x, cz + 300, 8, g, p.ground());
        }
        for (int z : new int[]{215, 265}) {
            CityPlanSupport.path(b, cx + 140, cz + z, cx + 320, cz + z, 8, g, p.ground());
        }

        // Station approach: the gate apron, the forecourt and the walk north.
        CityPlanSupport.path(b, cx, cz + 285, cx + 96, cz + 285, 6, g, p.ground());
        CityPlanSupport.path(b, cx + 24, cz + 285, cx + 24, cz + 300, 6, g, p.ground());
        CityPlanSupport.path(b, cx + 120, cz + 190, cx + 120, cz + 288, 8, g, p.ground());
    }

    private static void avenueLamps(StructureBuilder b, int cx, int cz, int g, Palette p) {
        for (int z = cz - 84; z <= cz + 284; z += 24) {
            if (z > cz - 68 && z < cz - 52) {
                continue;
            }
            if (z > cz + 172 && z < cz + 189) {
                continue;
            }
            lamp(b, cx - 9, g + 1, z, p);
            lamp(b, cx + 8, g + 1, z, p);
        }
    }

    // ------------------------------------------------------------------ water

    private static void water(StructureBuilder b, int cx, int cz, int g, Palette p) {
        mainCanal(b, cx + 243, cz - 220, cx + 256, cz + 150, g, p);
        crossCanal(b, cx + 259, cz - 45, cx + 288, cz - 36, g, p);
        crossCanal(b, cx + 259, cz + 86, cx + 310, cz + 93, g, p);
        b.fill(cx + 311, g - 3, cz + 84, cx + 312, g, cz + 95, p.foundation());

        // Open the main canal's east wall at both side-canal mouths.
        b.fill(cx + 257, g - 3, cz - 45, cx + 258, g, cz - 36, AIR);
        b.fill(cx + 257, g - 2, cz - 45, cx + 258, g - 1, cz - 36, WATER);
        b.fill(cx + 257, g + 1, cz - 45, cx + 258, g + 1, cz - 36, AIR);
        b.fill(cx + 257, g - 3, cz + 86, cx + 258, g, cz + 93, AIR);
        b.fill(cx + 257, g - 2, cz + 86, cx + 258, g - 1, cz + 93, WATER);
        b.fill(cx + 257, g + 1, cz + 86, cx + 258, g + 1, cz + 93, AIR);

        basin(b, cx + 288, cz - 70, cx + 310, cz - 10, g, p);
        b.fill(cx + 286, g - 3, cz - 45, cx + 287, g, cz - 36, AIR);
        b.fill(cx + 286, g - 2, cz - 45, cx + 287, g - 1, cz - 36, WATER);
        b.fill(cx + 286, g + 1, cz - 45, cx + 287, g + 1, cz - 36, AIR);
    }

    private static void mainCanal(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                  Palette p) {
        b.fill(x0, g - 5, z0, x1, g - 5, z1, p.foundation());
        b.fill(x0, g - 4, z0, x1, g - 1, z1, WATER);
        b.fill(x0, g, z0, x1, g, z1, AIR);
        b.fill(x0 - 2, g - 3, z0, x0 - 1, g, z1, p.foundation());
        b.fill(x1 + 1, g - 3, z0, x1 + 2, g, z1, p.foundation());
        b.fill(x0 - 2, g - 3, z0 - 2, x1 + 2, g, z0 - 1, p.foundation());
        b.fill(x0 - 2, g - 3, z1 + 1, x1 + 2, g, z1 + 2, p.foundation());
        b.fill(x0 - 2, g + 1, z0, x0 - 2, g + 1, z1, p.accent());
        b.fill(x1 + 2, g + 1, z0, x1 + 2, g + 1, z1, p.accent());
    }

    private static void crossCanal(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                   Palette p) {
        b.fill(x0, g - 5, z0, x1, g - 5, z1, p.foundation());
        b.fill(x0, g - 4, z0, x1, g - 1, z1, WATER);
        b.fill(x0, g, z0, x1, g, z1, AIR);
        b.fill(x0, g - 3, z0 - 2, x1, g, z0 - 1, p.foundation());
        b.fill(x0, g - 3, z1 + 1, x1, g, z1 + 2, p.foundation());
    }

    private static void basin(StructureBuilder b, int x0, int z0, int x1, int z1, int g, Palette p) {
        b.fill(x0, g - 5, z0, x1, g - 5, z1, p.foundation());
        b.fill(x0, g - 4, z0, x1, g - 1, z1, WATER);
        b.fill(x0, g, z0, x1, g, z1, AIR);
        b.fill(x0 - 2, g - 3, z0 - 2, x0 - 1, g, z1 + 2, p.foundation());
        b.fill(x1 + 1, g - 3, z0 - 2, x1 + 2, g, z1 + 2, p.foundation());
        b.fill(x0 - 2, g - 3, z0 - 2, x1 + 2, g, z0 - 1, p.foundation());
        b.fill(x0 - 2, g - 3, z1 + 1, x1 + 2, g, z1 + 2, p.foundation());
        b.fill(x0 - 2, g + 1, z0 - 2, x0 - 2, g + 1, z1 + 2, p.accent());
        b.fill(x1 + 2, g + 1, z0 - 2, x1 + 2, g + 1, z1 + 2, p.accent());
    }

    // ------------------------------------------------------------------ arrival

    private static void arrivalGate(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int z = cz + 320;
        for (int s = -1; s <= 1; s += 2) {
            int x = cx + s * 10;
            b.fill(x - 4, g, z - 8, x + 4, g, z + 4, p.foundation());
            b.fill(x - 4, g + 1, z - 8, x + 4, g + 14, z + 4, p.wall());
            b.fill(x - 4, g + 4, z - 8, x + 4, g + 4, z + 4, p.accent());
            b.fill(x - 4, g + 10, z - 8, x + 4, g + 10, z + 4, p.accent());
            b.fill(x - 2, g + 1, z - 6, x + 2, g + 13, z + 2, AIR);
            b.fill(x - 4, g + 2, z - 8, x - 4, g + 9, z - 8, BLUE_GLAZED);
            towerCap(b, x, z - 2, g + 15, 4, p);
        }
        b.fill(cx - 14, g + 12, z - 8, cx + 14, g + 14, z + 4, p.wall());
        b.fill(cx - 14, g + 12, z - 8, cx + 14, g + 12, z + 4, p.accent());
        b.sign(cx, g + 13, z + 4, Direction.SOUTH, "CIUDAD DE", "MEXICO");

        b.fill(cx - 24, g + 1, z - 8, cx - 15, g + 3, z + 4, p.weathered());
        b.fill(cx + 15, g + 1, z - 8, cx + 24, g + 3, z + 4, p.weathered());
        b.fill(cx - 24, g + 4, z - 8, cx - 15, g + 4, z + 4, p.accent());
        b.fill(cx + 15, g + 4, z - 8, cx + 24, g + 4, z + 4, p.accent());

        signpost(b, cx + 18, g, z - 16, Direction.SOUTH, p,
                "CATEDRAL", "ZOCALO", "MERCADO", "CANALES");
        signpost(b, cx - 18, g, z - 16, Direction.SOUTH, p,
                "RUINAS", "REFORMA", "TUNEL", "AVISO");
        noticeBoard(b, cx + 20, g, z - 12, Direction.SOUTH, p,
                "CITY NOTICES", "CURADOR", "IDOLO", "PREGUNTEN");
        lamp(b, cx - 12, g + 1, z + 4, p);
        lamp(b, cx + 12, g + 1, z + 4, p);

        // A small guard shelter east of the gate.
        b.fill(cx + 24, g, z - 8, cx + 32, g, z, p.ground());
        b.fill(cx + 24, g + 1, z - 8, cx + 32, g + 4, z, p.wall());
        b.fill(cx + 25, g + 1, z - 7, cx + 31, g + 3, z - 1, AIR);
        b.fill(cx + 27, g + 1, z, cx + 29, g + 2, z, AIR);
        b.pitchedRoof(cx + 23, z - 9, cx + 33, z + 1, g + 5, 2, 0);
        b.put(cx + 28, g + 2, z - 4, p.light());
    }

    private static void noticeBoard(StructureBuilder b, int x, int y, int z, Direction facing,
                                    Palette p, String... lines) {
        b.fill(x - 4, y + 1, z, x + 4, y + 4, z, p.frame());
        b.fill(x - 3, y + 2, z, x + 3, y + 3, z, p.wall());
        b.fill(x - 4, y + 5, z, x + 4, y + 5, z, p.roofSlab());
        b.put(x - 4, y + 1, z, p.foundation());
        b.put(x + 4, y + 1, z, p.foundation());
        b.put(x - 4, y + 5, z, p.accent());
        b.put(x + 4, y + 5, z, p.accent());
        b.sign(x, y + 3, z, facing, lines);
    }

    // ------------------------------------------------------------------ cathedral

    private static void cathedral(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int px = cx;
        int pz = cz - 200;

        b.fill(px - 50, g, pz - 76, px + 50, g, pz + 38, p.ground());
        b.fill(px - 50, g, pz - 76, px + 50, g, pz - 76, p.foundation());
        b.fill(px - 50, g, pz + 38, px + 50, g, pz + 38, p.foundation());

        // Long nave under a flat parapet.
        b.walls(px - 18, g + 1, pz - 66, px + 18, g + 20, pz + 14, p.wall());
        b.fill(px - 18, g + 1, pz - 66, px + 18, g + 1, pz + 14, p.foundation());
        b.fill(px - 17, g + 2, pz - 65, px + 17, g + 19, pz + 13, AIR);
        b.fill(px - 12, g + 1, pz - 60, px + 12, g + 1, pz - 56, p.accent());
        b.put(px, g + 2, pz - 58, GOLD);
        b.put(px - 8, g + 2, pz - 62, p.light());
        b.put(px + 8, g + 2, pz - 62, p.light());
        for (int z = pz - 50; z <= pz - 10; z += 6) {
            b.fill(px - 14, g + 1, z, px - 4, g + 1, z, p.roofSlab());
            b.fill(px + 4, g + 1, z, px + 14, g + 1, z, p.roofSlab());
        }
        b.fill(px - 19, g + 21, pz - 67, px + 19, g + 21, pz + 15, p.roof());
        b.fill(px - 19, g + 22, pz - 67, px + 19, g + 22, pz + 15, p.accent());
        for (int z = pz - 60; z <= pz + 8; z += 8) {
            b.fill(px - 18, g + 1, z, px - 18, g + 20, z, p.accent());
            b.fill(px + 18, g + 1, z, px + 18, g + 20, z, p.accent());
            b.window(px - 18, g + 6, z + 3, 4, 1, true);
            b.window(px + 18, g + 6, z + 3, 4, 1, true);
        }

        // Lower side aisles with tiled roofs.
        for (int s = -1; s <= 1; s += 2) {
            int x0 = px + (s < 0 ? -44 : 20);
            int x1 = px + (s < 0 ? -20 : 44);
            b.walls(x0, g + 1, pz - 62, x1, g + 12, pz + 6, p.wall());
            b.fill(x0, g + 1, pz - 62, x1, g + 1, pz + 6, p.foundation());
            b.fill(x0 + 1, g + 2, pz - 61, x1 - 1, g + 11, pz + 5, AIR);
            b.fill(x0 - 1, g + 13, pz - 63, x1 + 1, g + 13, pz + 7, p.roof());
            b.fill(x0 - 1, g + 14, pz - 63, x1 + 1, g + 14, pz + 7, p.accent());
            for (int z = pz - 56; z <= pz; z += 8) {
                b.window(s < 0 ? x0 : x1, g + 5, z, 3, 1, true);
            }
        }

        // Vestibule joining the aisles to the towers.
        b.walls(px - 44, g + 1, pz + 6, px + 44, g + 14, pz + 14, p.wall());
        b.fill(px - 44, g + 1, pz + 6, px + 44, g + 1, pz + 14, p.foundation());
        b.fill(px - 43, g + 2, pz + 7, px + 43, g + 13, pz + 13, AIR);
        b.fill(px - 45, g + 15, pz + 5, px + 45, g + 15, pz + 15, p.roof());
        b.fill(px - 45, g + 16, pz + 5, px + 45, g + 16, pz + 15, p.accent());

        // Baroque façade between the towers.
        b.walls(px - 26, g + 1, pz + 14, px + 26, g + 22, pz + 18, p.wall());
        b.fill(px - 26, g + 1, pz + 14, px + 26, g + 1, pz + 18, p.foundation());
        b.fill(px - 6, g + 1, pz + 14, px + 6, g + 10, pz + 18, AIR);
        b.fill(px - 8, g + 1, pz + 14, px - 7, g + 13, pz + 14, p.accent());
        b.fill(px + 7, g + 1, pz + 14, px + 8, g + 13, pz + 14, p.accent());
        b.fill(px - 8, g + 12, pz + 14, px + 8, g + 13, pz + 14, p.accent());
        b.fill(px - 26, g + 23, pz + 14, px + 26, g + 23, pz + 18, p.accent());
        b.fill(px - 26, g + 24, pz + 14, px + 26, g + 24, pz + 18, p.rail());
        b.sign(px, g + 14, pz + 18, Direction.SOUTH, "CATEDRAL", "METROPOLITANA");
        b.fill(px - 20, g + 10, pz + 14, px - 18, g + 13, pz + 14, BLUE_GLAZED);
        b.fill(px + 18, g + 10, pz + 14, px + 20, g + 13, pz + 14, BLUE_GLAZED);

        // Twin low bell towers with tiled pyramidal caps.
        for (int s = -1; s <= 1; s += 2) {
            int tx = px + s * 35;
            b.walls(tx - 9, g + 1, pz + 14, tx + 9, g + 30, pz + 34, p.wall());
            b.fill(tx - 9, g + 1, pz + 14, tx + 9, g + 1, pz + 34, p.foundation());
            b.fill(tx - 8, g + 2, pz + 15, tx + 8, g + 29, pz + 33, AIR);
            b.fill(tx - 9, g + 19, pz + 15, tx + 9, g + 19, pz + 33, p.foundation());
            b.put(tx, g + 20, pz + 24, BELL);
            for (int y = g + 20; y <= g + 24; y += 2) {
                b.fill(tx - 5, y, pz + 14, tx + 5, y + 1, pz + 14, AIR);
                b.fill(tx - 5, y, pz + 34, tx + 5, y + 1, pz + 34, AIR);
                b.fill(tx - 9, y, pz + 19, tx - 9, y + 1, pz + 29, AIR);
                b.fill(tx + 9, y, pz + 19, tx + 9, y + 1, pz + 29, AIR);
            }
            b.fill(tx - 9, g + 25, pz + 14, tx + 9, g + 25, pz + 34, p.accent());
            towerCap(b, tx, pz + 24, g + 31, 8, p);
            b.put(tx - 9, g + 1, pz + 14, p.accent());
            b.put(tx + 9, g + 1, pz + 34, p.accent());
        }

        // The great tiled dome over the crossing.
        b.fill(px - 14, g + 21, pz - 42, px + 14, g + 25, pz - 14, p.accent());
        for (int y = g + 22; y <= g + 24; y += 2) {
            b.window(px - 14, y, pz - 28, 2, 1, true);
            b.window(px + 14, y, pz - 28, 2, 1, true);
        }
        dome(b, px, pz - 28, g + 26, 13, p);
        lamp(b, px - 8, g + 1, pz + 22, p);
        lamp(b, px + 8, g + 1, pz + 22, p);
    }

    private static void towerCap(StructureBuilder b, int x, int z, int y, int half, Palette p) {
        b.fill(x - half - 1, y, z - half - 1, x + half + 1, y, z + half + 1, p.roof());
        b.fill(x - half, y + 1, z - half, x + half, y + 1, z + half, p.roof());
        b.fill(x - half + 1, y + 2, z - half + 1, x + half - 1, y + 2, z + half - 1,
                p.roofSlab());
        b.put(x, y + 3, z, p.accent());
        b.put(x, y + 4, z, GOLD);
    }

    private static void dome(StructureBuilder b, int x, int z, int baseY, int radius, Palette p) {
        for (int i = 0; i <= radius; i++) {
            int r = radius - i;
            b.fill(x - r, baseY + i, z - r, x + r, baseY + i, z + r, p.roof());
            if (r > 0) {
                for (int d = -r; d <= r; d++) {
                    b.put(x + d, baseY + i, z - r, p.accent());
                    b.put(x + d, baseY + i, z + r, p.accent());
                    b.put(x - r, baseY + i, z + d, p.accent());
                    b.put(x + r, baseY + i, z + d, p.accent());
                }
            }
        }
        b.put(x, baseY + radius + 1, z, p.accent());
        b.put(x, baseY + radius + 2, z, GOLD);
    }

    // ------------------------------------------------------------------ Zócalo

    private static void zocalo(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // The national flag on its stone base.
        b.fill(cx - 2, g, cz - 142, cx + 2, g, cz - 138, p.foundation());
        b.fill(cx, g + 1, cz - 140, cx, g + 28, cz - 140, p.accent());
        b.fill(cx + 1, g + 24, cz - 140, cx + 3, g + 26, cz - 140, GREEN_WOOL);
        b.fill(cx + 4, g + 24, cz - 140, cx + 6, g + 26, cz - 140, WHITE_WOOL);
        b.fill(cx + 7, g + 24, cz - 140, cx + 9, g + 26, cz - 140, RED_WOOL);
        b.put(cx, g + 29, cz - 140, GOLD);

        noticeBoard(b, cx, g, cz - 112, Direction.SOUTH, p,
                "CITY NOTICES", "CURADOR", "IDOLO", "PREGUNTEN");
        signpost(b, cx - 20, g, cz - 100, Direction.SOUTH, p, "ZOCALO", "PLAZA MAYOR");

        for (int s = -1; s <= 1; s += 2) {
            lamp(b, cx + s * 50, g + 1, cz - 160, p);
            lamp(b, cx + s * 50, g + 1, cz - 100, p);
            tree(b, cx + s * 40, cz - 160, g, s < 0 ? 1 : 0);
            tree(b, cx + s * 40, cz - 104, g, s < 0 ? 0 : 1);
        }
        marigoldBed(b, cx - 100, cz - 164, cx - 96, cz - 100, g);
        marigoldBed(b, cx + 96, cz - 164, cx + 100, cz - 100, g);
    }

    // ------------------------------------------------------------------ palace + arcades

    private static void palace(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx + 66;
        int x1 = cx + 112;
        int z0 = cz - 264;
        int z1 = cz - 154;

        b.fill(cx + 58, g, cz - 272, cx + 120, g, cz - 146, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 14, z1, p.wall());
        b.fill(x0, g + 1, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0 + 1, g + 2, z0 + 1, x1 - 1, g + 13, z1 - 1, AIR);
        b.fill(x0 - 1, g + 15, z0 - 1, x1 + 1, g + 15, z1 + 1, p.roof());
        b.fill(x0 - 1, g + 16, z0 - 1, x1 + 1, g + 16, z1 + 1, p.accent());

        // The west arcade facing the Zócalo, broken by the central portal.
        for (int z = z0 + 4; z <= z1 - 4; z += 6) {
            if (z > cz - 216 && z < cz - 200) {
                continue;
            }
            b.fill(x0 - 2, g + 1, z, x0 - 2, g + 5, z, p.accent());
            b.fill(x0 - 2, g + 1, z + 2, x0 - 2, g + 5, z + 2, p.accent());
        }
        b.fill(x0 - 3, g + 6, z0 + 2, x0 - 3, g + 6, z1 - 2, p.roofSlab());
        b.fill(x0 - 2, g + 7, z0 + 2, x0 - 2, g + 7, z1 - 2, p.roof());

        b.fill(x0, g + 1, cz - 212, x0, g + 8, cz - 204, AIR);
        b.fill(x0 - 1, g + 1, cz - 214, x0 - 1, g + 10, cz - 214, p.accent());
        b.fill(x0 - 1, g + 1, cz - 202, x0 - 1, g + 10, cz - 202, p.accent());
        b.fill(x0 - 1, g + 10, cz - 214, x0 - 1, g + 10, cz - 202, p.accent());
        b.fill(x0 - 2, g + 7, cz - 212, x0 - 2, g + 7, cz - 204, p.roofSlab());
        b.walls(x0 - 2, g + 8, cz - 212, x0 - 2, g + 8, cz - 204, p.rail());
        b.sign(x0, g + 9, cz - 200, Direction.WEST, "PALACIO", "NACIONAL");

        for (int y = g + 4; y <= g + 12; y += 4) {
            for (int z = z0 + 6; z <= z1 - 6; z += 8) {
                b.window(x0, y, z, 2, 1, true);
            }
        }

        // The central patio and a flag on the roof.
        b.fill(cx + 78, g + 1, cz - 240, cx + 100, g + 13, cz - 220, p.wall());
        b.fill(cx + 79, g + 2, cz - 239, cx + 99, g + 12, cz - 221, AIR);
        b.fill(cx + 80, g + 2, cz - 238, cx + 98, g + 2, cz - 222, p.ground());
        b.fill(cx + 78, g + 14, cz - 240, cx + 100, g + 14, cz - 220, p.roofSlab());
        b.fill(cx + 89, g + 15, cz - 209, cx + 89, g + 23, cz - 209, p.accent());
        b.fill(cx + 90, g + 19, cz - 209, cx + 92, g + 21, cz - 209, GREEN_WOOL);
        b.fill(cx + 93, g + 19, cz - 209, cx + 95, g + 21, cz - 209, WHITE_WOOL);
        b.fill(cx + 96, g + 19, cz - 209, cx + 98, g + 21, cz - 209, RED_WOOL);
        lamp(b, x0 - 2, g + 1, z0 + 4, p);
        lamp(b, x0 - 2, g + 1, z1 - 4, p);
    }

    private static void arcades(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx - 112;
        int x1 = cx - 70;
        int z0 = cz - 262;
        int z1 = cz - 156;

        b.fill(cx - 120, g, cz - 270, cx - 58, g, cz - 150, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 9, z1, p.wall());
        b.fill(x0, g + 1, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0 + 1, g + 2, z0 + 1, x1 - 1, g + 8, z1 - 1, AIR);
        b.fill(x0 - 1, g + 10, z0 - 1, x1 + 1, g + 10, z1 + 1, p.roof());
        b.fill(x0 - 1, g + 11, z0 - 1, x1 + 1, g + 11, z1 + 1, p.accent());

        for (int z = z0 + 4; z <= z1 - 4; z += 6) {
            b.fill(x1 + 2, g + 1, z, x1 + 2, g + 5, z, p.accent());
            b.fill(x1 + 2, g + 1, z + 2, x1 + 2, g + 5, z + 2, p.accent());
        }
        b.fill(x1 + 1, g + 6, z0 + 2, x1 + 3, g + 6, z1 - 2, p.roofSlab());
        b.fill(x1 + 1, g + 7, z0 + 2, x1 + 3, g + 7, z1 - 2, p.roof());

        String[] names = {"ARCOS", "TIENDAS", "CAFE"};
        for (int i = 0; i < names.length; i++) {
            int z = z0 + 20 + i * 30;
            b.fill(x1, g + 1, z, x1, g + 2, z, AIR);
            door(b, x1, g + 1, z, Direction.EAST, p);
            b.sign(x1, g + 3, z, Direction.EAST, names[i]);
        }
        b.fill(x1 + 2, g + 4, cz - 188, x1 + 4, g + 4, cz - 176, p.roofSlab());
        for (int z = cz - 186; z <= cz - 178; z += 4) {
            b.put(x1 + 4, g + 1, z, p.accent());
            b.put(x1 + 4, g + 2, z, p.accent());
            b.put(x1 + 4, g + 3, z, p.accent());
        }
        lamp(b, x1 + 2, g + 1, z0 + 6, p);
        lamp(b, x1 + 2, g + 1, z1 - 6, p);
    }

    // ------------------------------------------------------------------ south row

    private static void southRow(StructureBuilder b, int cx, int cz, int g, Palette p) {
        hotel(b, cx - 104, cz - 94, cx - 62, cz - 72, g, p);
        shop(b, cx - 118, cz - 94, cx - 108, cz - 72, g, p);
        house(b, cx + 20, cz - 94, cx + 56, cz - 72, g, p, 5, Direction.SOUTH, 18,
                "CAFE", "TACOS");
        awning(b, cx + 24, cx + 52, cz - 71, g, p);
        shop(b, cx + 64, cz - 94, cx + 100, cz - 72, g, p);
        house(b, cx + 104, cz - 94, cx + 118, cz - 72, g, p, 5, Direction.SOUTH, 7,
                "LIBROS", "MAPAS");

        // The plaza between the hotel and the avenue.
        b.fill(cx - 58, g, cz - 92, cx - 12, g, cz - 74, p.ground());
        fountain(b, cx - 35, cz - 83, g, p);
        tree(b, cx - 52, cz - 88, g, 1);
        tree(b, cx - 18, cz - 88, g, 0);
        lamp(b, cx - 52, g + 1, cz - 76, p);
        lamp(b, cx - 18, g + 1, cz - 76, p);
    }

    private static void hotel(StructureBuilder b, int x0, int z0, int x1, int z1, int g, Palette p) {
        house(b, x0, z0, x1, z1, g, p, 12, Direction.SOUTH, (x1 - x0) / 2,
                "HOTEL", "CENTRAL");
        int mx = (x0 + x1) / 2;
        int mz = (z0 + z1) / 2;
        b.fill(mx - 5, g + 1, mz - 5, mx + 5, g + 11, mz + 5, p.wall());
        b.fill(mx - 4, g + 2, mz - 4, mx + 4, g + 10, mz + 4, AIR);
        b.fill(mx - 4, g + 2, mz - 4, mx + 4, g + 2, mz + 4, p.ground());
        b.put(mx, g + 3, mz, p.accent());
        b.put(mx, g + 4, mz, p.light());
        for (int z = z0 + 3; z <= z1 - 3; z += 6) {
            b.put(x0 + 2, g + 1, z, RED_BED);
            b.put(x0 + 3, g + 1, z, RED_BED);
            b.put(x1 - 2, g + 1, z, BARREL);
        }
        b.fill(x0 + 2, g + 7, z1 + 1, x1 - 2, g + 7, z1 + 2, p.roofSlab());
        b.walls(x0 + 2, g + 8, z1 + 2, x1 - 2, g + 8, z1 + 2, p.rail());
        b.sign(mx, g + 9, z1, Direction.SOUTH, "HOTEL", "CENTRAL");
        lamp(b, x0 + 1, g + 1, z1 + 1, p);
        lamp(b, x1 - 1, g + 1, z1 + 1, p);
    }

    private static void shop(StructureBuilder b, int x0, int z0, int x1, int z1, int g, Palette p) {
        house(b, x0, z0, x1, z1, g, p, 6, Direction.SOUTH, (x1 - x0) / 2,
                "TIENDA", "ARTESANIAS");
        b.fill(x0 + 2, g + 1, z1 - 3, x1 - 2, g + 1, z1 - 3, p.foundation());
        for (int x = x0 + 3; x <= x1 - 3; x += 3) {
            b.put(x, g + 2, z1 - 3, TERRACOTTA);
            b.put(x, g + 2, z1 - 5, WHITE_CARPET);
        }
        b.put(x0 + 2, g + 1, z0 + 2, BARREL);
        b.put(x1 - 2, g + 2, z0 + 2, BLUE_GLAZED);
        b.put(x0 + 2, g + 3, z0 + 2, p.light());
    }

    // ------------------------------------------------------------------ market

    private static void market(StructureBuilder b, int cx, int cz, int g, Palette p) {
        // The old lake shore in the north-west corner: collapsed lanes on natural ground.
        naturalRuin(b, cx - 320, cz - 240, cx - 304, cz - 194, p);
        naturalRuin(b, cx - 320, cz - 187, cx - 304, cz - 134, p);
        naturalRuin(b, cx - 297, cz - 240, cx - 254, cz - 194, p);
        naturalRuin(b, cx - 297, cz - 187, cx - 254, cz - 134, p);

        int[][] blocks = {
                {-320, -127, -304, -68, 9},
                {-320, -52, -304, 6, 9},
                {-320, 13, -304, 76, 9},
                {-320, 83, -304, 146, 9},
                {-320, 153, -304, 170, 9},
                {-297, -127, -254, -68, 6},
                {-297, -52, -254, 6, 2},
                {-297, 13, -254, 76, 2},
                {-297, 83, -254, 146, 3},
                {-297, 153, -254, 170, 0},
                {-247, -240, -204, -194, 4},
                {-247, -187, -204, -134, 1},
                {-247, -127, -204, -68, 0},
                {-247, -52, -204, 6, 2},
                {-247, 13, -204, 76, 2},
                {-247, 83, -204, 146, 3},
                {-247, 153, -204, 170, 0},
                {-197, -240, -154, -194, 8},
                {-197, -187, -154, -134, 0},
                {-197, -127, -154, -68, 5},
                {-197, -52, -154, 6, 2},
                {-197, 13, -154, 76, 3},
                {-197, 83, -154, 146, 3},
                {-197, 153, -154, 170, 0},
                {-147, -240, -135, -194, 7},
                {-147, -187, -135, -134, 7},
                {-147, -127, -135, -68, 7},
                {-147, -52, -135, 6, 7},
                {-147, 13, -135, 76, 7},
                {-147, 83, -135, 146, 7},
                {-147, 153, -135, 170, 7}
        };
        for (int[] r : blocks) {
            marketBlock(b, cx + r[0], cz + r[1], cx + r[2], cz + r[3], g, p, r[4]);
        }

        shop(b, cx - 246, cz - 94, cx - 216, cz - 72, g, p);
        clinic(b, cx - 246, cz + 96, cx - 214, cz + 116, g, p);
        signpost(b, cx - 240, g, cz - 52, Direction.NORTH, p, "MERCADO", "LA MERCED");

        int sy = b.groundY(cx - 270, cz - 185);
        b.put(cx - 270, sy, cz - 185, p.foundation());
        CityPlanSupport.sign(b, cx - 270, sy + 1, cz - 185, Direction.NORTH,
                "DERRUMBE", "PELIGRO");

        for (int z = cz - 120; z <= cz + 160; z += 40) {
            if (z > cz - 68 && z < cz - 52) {
                continue;
            }
            lamp(b, cx - 301, g + 1, z, p);
            lamp(b, cx - 149, g + 1, z, p);
        }
    }

    private static void marketBlock(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                    Palette p, int type) {
        switch (type) {
            case 0 -> stallCourt(b, x0, z0, x1, z1, g, p);
            case 1 -> mercado(b, x0, z0, x1, z1, g, p);
            case 2 -> tenement(b, x0, z0, x1, z1, g, p);
            case 3 -> gardenPlot(b, x0, z0, x1, z1, g, p);
            case 4 -> ruinPlot(b, x0, z0, x1, z1, g, p);
            case 5 -> workshop(b, x0, z0, x1, z1, g, p);
            case 6 -> foodCourt(b, x0, z0, x1, z1, g, p);
            case 7 -> shopRow(b, x0, z0, x1, z1, g, p, "TIENDA", "OFICIOS");
            case 8 -> warehouse(b, x0, z0, x1, z1, g, p);
            case 9 -> rimGarden(b, x0, z0, x1, z1, p);
            default -> stallCourt(b, x0, z0, x1, z1, g, p);
        }
    }

    private static void rimGarden(StructureBuilder b, int x0, int z0, int x1, int z1, Palette p) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                int y = b.groundY(x, z);
                b.put(x, y, z, GRASS);
                if ((x * 7 + z * 13) % 5 == 0) {
                    b.put(x, y + 1, z, MARIGOLD);
                }
            }
        }
        for (int x = x0; x <= x1; x++) {
            int yn = b.groundY(x, z0);
            b.put(x, yn + 1, z0, p.weathered());
            int ys = b.groundY(x, z1);
            b.put(x, ys + 1, z1, p.weathered());
        }
        tree(b, x0 + 3, z0 + 3, b.groundY(x0 + 3, z0 + 3), 1);
        tree(b, x1 - 3, z1 - 3, b.groundY(x1 - 3, z1 - 3), 0);
    }

    private static void stallCourt(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                   Palette p) {
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        for (int x = x0 + 5; x <= x1 - 5; x += 12) {
            for (int z = z0 + 5; z <= z1 - 5; z += 12) {
                stall(b, x, z, g, p, (x + z) % 2 == 0 ? ORANGE_WOOL : WHITE_WOOL);
            }
        }
        signpost(b, (x0 + x1) / 2, g, z1 - 1, Direction.SOUTH, p, "PUESTO", "MERCADO");
        lamp(b, x0 + 2, g + 1, z0 + 2, p);
        lamp(b, x1 - 2, g + 1, z1 - 2, p);
    }

    private static void mercado(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                Palette p) {
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + 6, z1, p.wall());
        b.fill(x0, g + 1, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0 + 1, g + 2, z0 + 1, x1 - 1, g + 5, z1 - 1, AIR);
        for (int x = x0 + 4; x <= x1 - 4; x += 6) {
            b.fill(x, g + 1, z0, x + 1, g + 3, z0, AIR);
            b.fill(x, g + 1, z1, x + 1, g + 3, z1, AIR);
        }
        b.pitchedRoof(x0 - 2, z0 - 2, x1 + 2, z1 + 2, g + 7, 6, x1 - x0 >= z1 - z0 ? 0 : 1);
        for (int x = x0 + 6; x <= x1 - 6; x += 10) {
            for (int z = z0 + 6; z <= z1 - 6; z += 10) {
                stall(b, x, z, g, p, (x + z) % 2 == 0 ? WHITE_WOOL : ORANGE_WOOL);
            }
        }
        b.sign((x0 + x1) / 2, g + 4, z1, Direction.SOUTH, "MERCADO", "GRANDE");
        lamp(b, x0 + 2, g + 1, z0 + 2, p);
        lamp(b, x1 - 2, g + 1, z1 - 2, p);
    }

    private static void tenement(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                 Palette p) {
        house(b, x0, z0, x1, z1, g, p, 10, Direction.SOUTH, (x1 - x0) / 2, "VIVIENDAS");
        for (int z = z0 + 3; z <= z1 - 3; z += 6) {
            b.put(x0 + 2, g + 1, z, RED_BED);
            b.put(x1 - 2, g + 1, z, RED_BED);
        }
        b.put(x0 + 2, g + 2, z0 + 2, BARREL);
        b.put(x1 - 2, g + 3, z0 + 2, p.light());
    }

    private static void gardenPlot(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                   Palette p) {
        b.fill(x0, g, z0, x1, g, z1, GRASS);
        b.walls(x0, g + 1, z0, x1, g + 2, z1, p.weathered());
        b.fill(x0 + 2, g + 1, z1, x1 - 2, g + 2, z1, AIR);
        fountain(b, (x0 + x1) / 2, (z0 + z1) / 2, g, p);
        tree(b, x0 + 3, z0 + 3, g, 1);
        tree(b, x1 - 3, z0 + 3, g, 0);
        tree(b, x0 + 3, z1 - 3, g, 0);
        tree(b, x1 - 3, z1 - 3, g, 1);
        b.put(x0 + 2, g + 1, z0 + 2, AZALEA);
        marigoldBed(b, x0 + 1, z0 + 1, x1 - 1, z1 - 1, g);
        agave(b, x0 + 2, z1 - 2, g);
        lamp(b, (x0 + x1) / 2, g + 1, z0 + 2, p);
    }

    private static void ruinPlot(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                 Palette p) {
        b.fill(x0, g, z0, x1, g, z1, p.rubble());
        b.walls(x0, g + 1, z0, x1, g + 3, z1, p.weathered());
        b.fill(x0 + 3, g + 1, z0, x0 + 8, g + 3, z0, AIR);
        b.fill(x1, g + 1, z0 + 4, x1, g + 2, z0 + 10, AIR);
        b.fill(x0 + 2, g + 1, z0 + 2, x0 + 6, g + 1, z0 + 6, p.rubble());
        b.fill(x1 - 6, g + 1, z1 - 6, x1 - 2, g + 2, z1 - 2, p.rubble());
        b.put(x0 + 5, g + 2, z0 + 5, MOSSY);
        b.put(x1 - 4, g + 1, z1 - 4, CRACKED);
        b.put(x0 + 3, g + 2, z0 + 3, p.light());
        b.put(x0 + 4, g + 1, z0 + 4, MOSS);
        agave(b, x1 - 3, z1 - 3, g);
        signpost(b, (x0 + x1) / 2, g, z1 + 1, Direction.NORTH, p, "DERRUMBE", "PELIGRO");
    }

    private static void workshop(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                 Palette p) {
        house(b, x0, z0, x1, z1, g, p, 5, Direction.SOUTH, (x1 - x0) / 2, "TALLER");
        b.fill(x0 + 2, g + 1, z0 + 2, x0 + 4, g + 1, z0 + 4, p.foundation());
        b.put(x0 + 3, g + 2, z0 + 3, CAMPFIRE);
        b.put(x1 - 3, g + 1, z0 + 2, BARREL);
        b.put(x1 - 2, g + 2, z0 + 2, HAY);
        b.put(x0 + 2, g + 3, z1 - 2, p.light());
    }

    private static void foodCourt(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                  Palette p) {
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        for (int x = x0 + 5; x <= x1 - 5; x += 10) {
            for (int z = z0 + 5; z <= z1 - 5; z += 10) {
                b.put(x, g + 1, z, p.rail());
                b.put(x, g + 2, z, p.roofSlab());
                b.put(x - 1, g + 1, z, p.roofStairs());
                b.put(x + 1, g + 1, z, p.roofStairs());
                b.put(x, g + 1, z + 2, CAMPFIRE);
                b.put(x, g + 3, z, p.light());
            }
        }
        b.fill(x0 + 2, g + 5, z0 + 2, x1 - 2, g + 5, z1 - 2, p.roofSlab());
        for (int x = x0 + 2; x <= x1 - 2; x += 8) {
            b.put(x, g + 1, z0 + 2, p.accent());
            b.put(x, g + 2, z0 + 2, p.accent());
            b.put(x, g + 3, z0 + 2, p.accent());
            b.put(x, g + 4, z0 + 2, p.accent());
            b.put(x, g + 1, z1 - 2, p.accent());
            b.put(x, g + 2, z1 - 2, p.accent());
            b.put(x, g + 3, z1 - 2, p.accent());
            b.put(x, g + 4, z1 - 2, p.accent());
        }
        signpost(b, (x0 + x1) / 2, g, z1 - 1, Direction.SOUTH, p, "COMIDA", "TACOS");
    }

    private static void shopRow(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                Palette p, String... names) {
        house(b, x0, z0, x1, z1, g, p, 5, Direction.SOUTH, (x1 - x0) / 2, names[0]);
        int step = Math.max(6, (x1 - x0) / names.length);
        for (int i = 1; i < names.length; i++) {
            int x = x0 + 2 + i * step;
            if (x > x1 - 2) {
                break;
            }
            b.fill(x, g + 1, z1, x, g + 2, z1, AIR);
            door(b, x, g + 1, z1, Direction.SOUTH, p);
            b.sign(x, g + 3, z1, Direction.SOUTH, names[i]);
            awning(b, x - 2, x + 2, z1 + 1, g, p);
        }
    }

    private static void warehouse(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                  Palette p) {
        house(b, x0, z0, x1, z1, g, p, 8, Direction.SOUTH, (x1 - x0) / 2, "BODEGA");
        for (int x = x0 + 2; x <= x1 - 2; x += 4) {
            b.put(x, g + 1, z0 + 2, p.foundation());
            b.put(x, g + 2, z0 + 2, BARREL);
        }
        b.fill(x0 + 2, g + 1, z1 - 4, x0 + 5, g + 2, z1 - 2, HAY);
        b.put(x1 - 2, g + 3, z0 + 2, p.light());
    }

    private static void clinic(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                               Palette p) {
        house(b, x0, z0, x1, z1, g, p, 6, Direction.SOUTH, (x1 - x0) / 2,
                "CURANDERO", "REMEDIOS");
        b.fill(x0 + 2, g + 1, z0 + 2, x0 + 4, g + 1, z0 + 3, RED_BED);
        b.put(x0 + 6, g + 1, z0 + 2, Blocks.CAULDRON.defaultBlockState());
        for (int x = x0 + 2; x <= x1 - 2; x += 3) {
            b.put(x, g + 3, z0 + 2, MARIGOLD);
        }
        b.put(x1 - 2, g + 2, z1 - 4, BARREL);
        b.put(x0 + 2, g + 2, z1 - 4, LECTERN);
        b.put(x1 - 2, g + 3, z0 + 2, p.light());
    }

    // ------------------------------------------------------------------ canals

    private static void canals(StructureBuilder b, int cx, int cz, int g, Palette p) {
        canalHouses(b, cx, cz, g, p);
        chinampas(b, cx, cz, g, p);
        port(b, cx, cz, g, p);

        roadBridge(b, cx, cz, g, p, -60);
        bridge(b, cx + 250, cz + 20, g, p);
        bridge(b, cx + 250, cz + 120, g, p);

        boat(b, cx + 250, cz - 160, g, p, true);
        boat(b, cx + 250, cz - 80, g, p, true);
        boat(b, cx + 250, cz + 60, g, p, true);

        for (int z = cz - 200; z <= cz + 140; z += 40) {
            b.put(cx + 250, g - 3, z, SEA_LANTERN);
        }
        for (int z = cz - 180; z <= cz + 140; z += 48) {
            tree(b, cx + 240, z, g, 1);
        }
        tree(b, cx + 262, cz - 180, g, 0);
        tree(b, cx + 262, cz - 100, g, 0);
        tree(b, cx + 262, cz + 60, g, 0);
        tree(b, cx + 262, cz + 140, g, 0);
        for (int z = cz - 200; z <= cz + 140; z += 24) {
            if (z > cz - 68 && z < cz - 52) {
                continue;
            }
            if (z > cz - 47 && z < cz - 34) {
                continue;
            }
            if (z > cz + 84 && z < cz + 95) {
                continue;
            }
            lamp(b, cx + 238, g + 1, z, p);
            lamp(b, cx + 261, g + 1, z, p);
        }
        for (int z = cz - 190; z <= cz + 130; z += 60) {
            b.put(cx + 250, g - 1, z, LILY);
        }

        signpost(b, cx + 240, g, cz + 10, Direction.WEST, p, "CANALES", "CHINAMPAS");
        signpost(b, cx + 240, g, cz - 58, Direction.WEST, p, "AGUAS", "PROFUNDAS");
        signpost(b, cx + 236, g, cz + 2, Direction.WEST, p, "EMBARCADERO");
        marshEdge(b, cx + 280, cz - 220, cx + 320, cz - 120);
    }

    private static void canalHouses(StructureBuilder b, int cx, int cz, int g, Palette p) {
        boatyard(b, cx + 156, cz - 200, cx + 184, cz - 168, g, p);
        tenement(b, cx + 196, cz - 200, cx + 220, cz - 168, g, p);
        warehouse(b, cx + 156, cz - 130, cx + 184, cz - 100, g, p);
        cantina(b, cx + 196, cz - 120, cx + 220, cz - 92, g, p);
        canalHouse(b, cx + 156, cz + 10, cx + 184, cz + 34, g, p, Direction.EAST);
        canalHouse(b, cx + 196, cz + 10, cx + 220, cz + 34, g, p, Direction.WEST);
        warehouse(b, cx + 156, cz + 46, cx + 184, cz + 76, g, p);
        gardenPlot(b, cx + 196, cz + 46, cx + 220, cz + 76, g, p);
        canalHouse(b, cx + 156, cz + 128, cx + 184, cz + 150, g, p, Direction.EAST);
        gardenPlot(b, cx + 196, cz + 128, cx + 220, cz + 150, g, p);
    }

    private static void boatyard(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                 Palette p) {
        house(b, x0, z0, x1, z1, g, p, 7, Direction.EAST, (z1 - z0) / 2, "ASTILLERO");
        int mz = (z0 + z1) / 2;
        b.fill(x1, g + 1, mz - 4, x1, g + 4, mz + 4, AIR);
        b.fill(x0 + 4, g + 1, mz - 4, x0 + 12, g + 1, mz + 4, p.accent());
        b.fill(x0 + 4, g + 2, mz - 4, x0 + 4, g + 3, mz + 4, p.wall());
        b.fill(x0 + 12, g + 2, mz - 4, x0 + 12, g + 3, mz + 4, p.wall());
        b.fill(x0 + 2, g + 1, z0 + 2, x0 + 6, g + 2, z0 + 3, HAY);
        crane(b, x1 - 4, z0 + 4, g, p);
    }

    private static void cantina(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                Palette p) {
        house(b, x0, z0, x1, z1, g, p, 6, Direction.WEST, (z1 - z0) / 2,
                "CANTINA", "LA LUZ");
        b.fill(x0 - 3, g + 4, z0 + 2, x0 - 1, g + 4, z1 - 2, p.roofSlab());
        for (int z = z0 + 3; z <= z1 - 3; z += 4) {
            b.put(x0 - 3, g + 1, z, p.accent());
            b.put(x0 - 3, g + 2, z, p.accent());
            b.put(x0 - 3, g + 3, z, p.accent());
        }
        for (int z = z0 + 4; z <= z1 - 4; z += 4) {
            b.put(x0 - 4, g + 1, z, p.rail());
            b.put(x0 - 4, g + 2, z, p.roofSlab());
        }
        lamp(b, x0 - 2, g + 1, z0 + 4, p);
    }

    private static void canalHouse(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                   Palette p, Direction front) {
        house(b, x0, z0, x1, z1, g, p, 8, front, (z1 - z0) / 2, "CASA", "CANAL");
        if (front == Direction.EAST) {
            b.fill(x1 + 1, g + 6, z0 + 2, x1 + 2, g + 6, z1 - 2, p.roofSlab());
            b.walls(x1 + 2, g + 7, z0 + 2, x1 + 2, g + 7, z1 - 2, p.rail());
            b.put(x1 + 1, g + 1, (z0 + z1) / 2, BARREL);
        } else {
            b.fill(x0 - 2, g + 6, z0 + 2, x0 - 1, g + 6, z1 - 2, p.roofSlab());
            b.walls(x0 - 2, g + 7, z0 + 2, x0 - 2, g + 7, z1 - 2, p.rail());
            b.put(x0 - 1, g + 1, (z0 + z1) / 2, BARREL);
        }
        lamp(b, front == Direction.EAST ? x1 + 1 : x0 - 1, g + 1,
                (z0 + z1) / 2 + 3, p);
    }

    private static void boat(StructureBuilder b, int x, int z, int g, Palette p, boolean large) {
        int w = large ? 2 : 1;
        int l = large ? 5 : 3;
        b.fill(x - w, g - 1, z - l, x + w, g - 1, z + l, p.accent());
        b.fill(x - w - 1, g - 1, z - l + 1, x - w - 1, g - 1, z + l - 1, p.wall());
        b.fill(x + w + 1, g - 1, z - l + 1, x + w + 1, g - 1, z + l - 1, p.wall());
        b.put(x - w, g - 1, z - l, p.wall());
        b.put(x + w, g - 1, z - l, p.wall());
        b.put(x - w, g - 1, z + l, p.wall());
        b.put(x + w, g - 1, z + l, p.wall());
        for (int dz = -l + 1; dz <= l - 1; dz += 2) {
            b.put(x - w, g + 1, z + dz, p.accent());
            b.put(x + w, g + 1, z + dz, p.accent());
            b.put(x - w, g + 2, z + dz, p.accent());
            b.put(x + w, g + 2, z + dz, p.accent());
        }
        b.fill(x - w - 1, g + 3, z - l, x + w + 1, g + 3, z + l, p.roofSlab());
        b.put(x, g + 1, z - l + 2, p.light());
        b.put(x, g, z + l - 1, p.rail());
    }

    private static void bridge(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x - 1, g, z - 2, x + 1, g, z + 2, p.foundation());
        b.fill(x - 1, g + 1, z - 2, x + 1, g + 1, z - 2, p.accent());
        b.fill(x - 1, g + 1, z + 2, x + 1, g + 1, z + 2, p.accent());
        b.fill(x, g - 3, z - 2, x, g - 1, z + 2, p.foundation());
        lamp(b, x, g + 1, z - 2, p);
    }

    private static void roadBridge(StructureBuilder b, int cx, int cz, int g, Palette p, int z) {
        int z0 = cz + z - 7;
        int z1 = cz + z + 7;
        b.fill(cx + 240, g, z0, cx + 259, g, z1, p.foundation());
        b.fill(cx + 240, g + 1, z0, cx + 259, g + 1, z0, p.accent());
        b.fill(cx + 240, g + 1, z1, cx + 259, g + 1, z1, p.accent());
        b.fill(cx + 240, g + 2, z0, cx + 259, g + 2, z0, p.rail());
        b.fill(cx + 240, g + 2, z1, cx + 259, g + 2, z1, p.rail());
        b.fill(cx + 242, g - 3, z0, cx + 242, g - 1, z1, p.foundation());
        b.fill(cx + 257, g - 3, z0, cx + 257, g - 1, z1, p.foundation());
        lamp(b, cx + 242, g + 1, z0, p);
        lamp(b, cx + 257, g + 1, z1, p);
    }

    private static void chinampas(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx + 264;
        int x1 = cx + 316;
        int z0 = cz;
        int z1 = cz + 80;
        b.fill(x0, g - 3, z0, x1, g - 3, z1, p.foundation());
        b.fill(x0, g - 2, z0, x1, g - 1, z1, WATER);
        b.fill(x0, g, z0, x1, g, z1, AIR);

        int[][] plots = {
                {268, 0, 281, 7}, {286, 0, 296, 7}, {301, 0, 311, 7},
                {268, 12, 281, 33}, {286, 12, 296, 33}, {301, 12, 311, 33},
                {268, 38, 281, 59}, {286, 38, 296, 59}, {301, 38, 311, 59},
                {268, 64, 281, 80}, {286, 64, 296, 80}, {301, 64, 311, 80}
        };
        for (int[] r : plots) {
            chinampaPlot(b, cx + r[0], cz + r[1], cx + r[2], cz + r[3], g, p);
        }
        chinampaHut(b, cx + 274, cz + 22, g, p);
        chinampaHut(b, cx + 306, cz + 48, g, p);
        boat(b, cx + 266, cz + 20, g, p, false);
        boat(b, cx + 284, cz + 50, g, p, false);
        boat(b, cx + 298, cz + 70, g, p, false);
        marigoldBed(b, cx + 272, cz + 4, cx + 280, cz + 6, g);
        marigoldBed(b, cx + 288, cz + 42, cx + 294, cz + 44, g);
        signpost(b, cx + 261, g, cz + 78, Direction.WEST, p, "CHINAMPAS", "XOCHIMILCO");
    }

    private static void chinampaPlot(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                     Palette p) {
        b.fill(x0, g, z0, x1, g, z1, CLAY);
        b.fill(x0 + 1, g, z0 + 1, x1 - 1, g, z1 - 1, GRASS);
        for (int x = x0 + 1; x <= x1 - 1; x++) {
            for (int z = z0 + 1; z <= z1 - 1; z++) {
                if ((x + z) % 4 == 0) {
                    b.put(x, g, z, FARMLAND);
                }
            }
        }
        for (int x = x0 + 2; x <= x1 - 2; x += 3) {
            for (int z = z0 + 2; z <= z1 - 2; z += 3) {
                b.put(x, g + 1, z, WHEAT);
            }
        }
        for (int z = z0 + 1; z <= z1 - 1; z += 4) {
            b.put(x0, g + 1, z, SUGAR_CANE);
            b.put(x1, g + 1, z, SUGAR_CANE);
        }
        b.put(x0 + 1, g + 1, z0 + 1, MARIGOLD);
        b.put(x1 - 1, g + 1, z1 - 1, MARIGOLD);
    }

    private static void chinampaHut(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x - 2, g, z - 2, x + 2, g, z + 2, CLAY);
        b.walls(x - 2, g + 1, z - 2, x + 2, g + 3, z + 2, MUD);
        b.fill(x - 1, g + 1, z - 1, x + 1, g + 2, z + 1, AIR);
        b.fill(x - 1, g + 1, z + 2, x + 1, g + 2, z + 2, AIR);
        b.fill(x - 3, g + 4, z - 3, x + 3, g + 4, z + 3, p.roofSlab());
        b.fill(x - 2, g + 5, z - 2, x + 2, g + 5, z + 2, p.roof());
        b.put(x, g + 1, z - 1, RED_BED);
        b.put(x + 1, g + 1, z - 1, BARREL);
        b.put(x - 1, g + 2, z - 1, p.light());
    }

    private static void port(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int x0 = cx + 311;
        int z0 = cz - 70;
        int z1 = cz - 10;
        b.fill(x0, g, z0, cx + 320, g, z1, p.foundation());
        b.fill(x0, g + 1, z0, x0, g + 1, z1, p.accent());
        for (int z = z0 + 2; z <= z1 - 2; z += 8) {
            b.put(x0, g + 1, z, p.rail());
            b.put(x0 + 2, g + 1, z, BARREL);
        }
        house(b, cx + 312, cz - 64, cx + 320, cz - 50, g, p, 6, Direction.WEST, 7, "ADUANA");
        crane(b, cx + 314, cz - 30, g, p);
        signpost(b, cx + 312, g, cz - 20, Direction.WEST, p, "PUERTO", "CANAL");
        boat(b, cx + 306, cz - 40, g, p, true);
        lamp(b, cx + 316, g + 1, cz - 12, p);
    }

    private static void crane(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x, g + 1, z, x, g + 8, z, p.accent());
        b.fill(x - 5, g + 8, z, x + 2, g + 8, z, p.accent());
        b.put(x - 5, g + 7, z, p.rail());
        b.put(x - 5, g + 6, z, BARREL);
        b.put(x, g + 9, z, p.light());
    }

    // ------------------------------------------------------------------ alameda

    private static void alameda(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx + 158, g, cz + 154, cx + 302, g, cz + 172, GRASS);
        b.fill(cx + 158, g, cz + 162, cx + 302, g, cz + 163, p.ground());
        b.fill(cx + 226, g, cz + 154, cx + 230, g, cz + 172, p.ground());
        fountain(b, cx + 230, cz + 163, g, p);
        for (int x = cx + 170; x <= cx + 290; x += 24) {
            tree(b, x, cz + 156, g, 1);
            tree(b, x, cz + 170, g, 0);
        }
        for (int x = cx + 170; x <= cx + 290; x += 24) {
            lamp(b, x, g + 1, cz + 162, p);
            b.put(x, g + 1, cz + 166, STONE_SLAB);
        }
        signpost(b, cx + 160, g, cz + 166, Direction.EAST, p, "ALAMEDA", "JARDIN");
    }

    // ------------------------------------------------------------------ ruins

    private static void ruins(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx - 40, g, cz, cx + 40, g, cz + 90, p.ground());
        b.fill(cx - 38, g - 1, cz + 18, cx - 14, g - 1, cz + 40, WATER);
        b.fill(cx - 38, g, cz + 18, cx - 14, g, cz + 40, AIR);
        b.fill(cx - 39, g, cz + 17, cx - 13, g, cz + 17, p.accent());
        b.fill(cx - 39, g, cz + 41, cx - 13, g, cz + 41, p.accent());
        b.fill(cx - 39, g, cz + 18, cx - 39, g, cz + 40, p.accent());
        b.fill(cx - 13, g, cz + 18, cx - 13, g, cz + 40, p.accent());
        b.put(cx - 26, g - 1, cz + 29, LILY);

        pyramid(b, cx - 70, cz + 30, g, p);
        ballCourt(b, cx + 50, cz + 4, cx + 86, cz + 64, g, p);

        for (int x = cx - 30; x <= cx + 30; x += 20) {
            b.fill(cx + x, g + 1, cz + 10, cx + x, g + 3, cz + 10, p.foundation());
            b.put(cx + x, g + 4, cz + 10, p.accent());
        }
        for (int s = -1; s <= 1; s += 2) {
            lamp(b, cx + s * 36, g + 1, cz + 4, p);
            lamp(b, cx + s * 36, g + 1, cz + 86, p);
            tree(b, cx + s * 40, cz + 20, g, s < 0 ? 1 : 0);
            tree(b, cx + s * 40, cz + 70, g, s < 0 ? 0 : 1);
            agave(b, cx + s * 22, cz + 84, g);
        }
        marigoldBed(b, cx - 38, cz + 44, cx - 14, cz + 48, g);
        signpost(b, cx, g, cz + 90, Direction.SOUTH, p, "RUINAS", "TEMPLO MAYOR");
    }

    private static void pyramid(StructureBuilder b, int px, int pz, int g, Palette p) {
        b.fill(px - 26, g, pz - 26, px + 26, g, pz + 26, p.ground());
        b.fill(px - 24, g + 1, pz - 24, px + 24, g + 1, pz + 24, p.foundation());
        int x0 = px - 22;
        int x1 = px + 22;
        int z0 = pz - 22;
        int z1 = pz + 22;
        for (int i = 0; i < 5; i++) {
            int y0 = g + 2 + i * 4;
            b.fill(x0, y0, z0, x1, y0 + 3, z1, p.weathered());
            b.fill(x0, y0, z0, x1, y0, z1, p.foundation());
            for (int x = x0 + 2; x <= x1 - 2; x += 4) {
                b.put(x, y0 + 2, z0, p.accent());
                b.put(x, y0 + 2, z1, p.accent());
            }
            x0 += 4;
            x1 -= 4;
            z0 += 4;
            z1 -= 4;
        }

        int ty = g + 22;
        b.fill(px - 8, ty - 1, pz - 8, px + 8, ty - 1, pz + 8, p.foundation());
        b.fill(px - 6, ty, pz - 6, px + 6, ty, pz + 5, p.foundation());
        b.walls(px - 5, ty + 1, pz - 5, px + 5, ty + 4, pz + 5, p.wall());
        b.fill(px - 4, ty + 1, pz - 4, px + 4, ty + 3, pz + 4, AIR);
        b.fill(px - 6, ty + 5, pz - 6, px + 6, ty + 5, pz + 5, p.roof());
        b.fill(px - 5, ty + 6, pz - 5, px + 5, ty + 6, pz + 5, p.roofSlab());
        b.put(px, ty + 7, pz, p.accent());
        b.fill(px - 1, ty + 1, pz + 5, px + 1, ty + 2, pz + 5, AIR);
        b.put(px, ty + 1, pz - 2, p.accent());
        b.put(px, ty + 2, pz - 2, GOLD);
        b.put(px - 4, ty + 1, pz, p.light());
        b.put(px + 4, ty + 1, pz, p.light());

        for (int i = 0; i <= 20; i++) {
            int y = g + 1 + i;
            int z = pz + 26 - i;
            b.fill(px - 3, g, z, px + 3, y, z, p.foundation());
            b.put(px - 3, y + 1, z, p.accent());
            b.put(px + 3, y + 1, z, p.accent());
        }
        b.fill(px - 6, g + 1, pz + 26, px - 4, g + 2, pz + 26, p.accent());
        b.fill(px + 4, g + 1, pz + 26, px + 6, g + 2, pz + 26, p.accent());
        b.put(px - 5, g + 2, pz + 26, BLUE_GLAZED);
        b.put(px + 5, g + 2, pz + 26, BLUE_GLAZED);
        b.fill(px - 20, g + 1, pz + 24, px - 8, g + 1, pz + 26, p.rubble());
        b.fill(px + 8, g + 1, pz + 24, px + 20, g + 1, pz + 26, p.rubble());
        agave(b, px - 18, pz + 20, g);
        agave(b, px + 18, pz + 20, g);
        lamp(b, px - 4, g + 1, pz + 28, p);
        lamp(b, px + 4, g + 1, pz + 28, p);
        signpost(b, px, g, pz + 30, Direction.EAST, p, "PIRAMIDE", "NO SUBIR");
    }

    private static void ballCourt(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                  Palette p) {
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.fill(x0 + 4, g, z0 + 8, x1 - 4, g, z1 - 8, p.foundation());
        b.fill(x0 + 4, g, z0, x1 - 4, g, z0 + 8, CUT_SAND);
        b.fill(x0 + 4, g, z1 - 8, x1 - 4, g, z1, CUT_SAND);
        b.fill(x0, g + 1, z0, x0 + 4, g + 5, z1, p.weathered());
        b.fill(x1 - 4, g + 1, z0, x1, g + 5, z1, p.weathered());        b.fill(x0, g + 6, z0, x0 + 4, g + 6, z1, p.accent());
        b.fill(x1 - 4, g + 6, z0, x1, g + 6, z1, p.accent());
        for (int z = z0 + 8; z <= z1 - 8; z += 4) {
            b.put(x0 + 5, g + 1, z, p.roofStairs());
            b.put(x1 - 5, g + 1, z, p.roofStairs());
        }
        int mz = (z0 + z1) / 2;
        b.fill(x0 + 1, g + 3, mz, x0 + 3, g + 5, mz + 2, p.accent());
        b.fill(x0 + 2, g + 4, mz + 1, x0 + 2, g + 4, mz + 1, AIR);
        b.fill(x1 - 3, g + 3, mz, x1 - 1, g + 5, mz + 2, p.accent());
        b.fill(x1 - 2, g + 4, mz + 1, x1 - 2, g + 4, mz + 1, AIR);
        b.put(x0 + 8, g + 1, z0 + 2, p.light());
        b.put(x1 - 8, g + 1, z1 - 2, p.light());
        signpost(b, x0 - 2, g, z1 - 4, Direction.WEST, p, "JUEGO", "DE PELOTA");
    }

    // ------------------------------------------------------------------ museum + tunnel

    private static void museum(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx - 34, g, cz + 96, cx + 34, g, cz + 148, p.ground());
        b.walls(cx - 34, g + 1, cz + 96, cx + 34, g + 7, cz + 148, p.wall());
        b.fill(cx - 34, g + 1, cz + 96, cx + 34, g + 1, cz + 148, p.foundation());
        b.fill(cx - 33, g + 2, cz + 97, cx + 33, g + 6, cz + 147, AIR);
        b.fill(cx - 24, g + 2, cz + 104, cx + 24, g + 7, cz + 140, AIR);
        b.fill(cx - 5, g + 1, cz + 148, cx + 5, g + 5, cz + 148, AIR);
        b.fill(cx - 5, g + 1, cz + 96, cx + 5, g + 5, cz + 96, AIR);

        b.fill(cx - 34, g + 8, cz + 96, cx - 24, g + 8, cz + 148, p.roof());
        b.fill(cx + 24, g + 8, cz + 96, cx + 34, g + 8, cz + 148, p.roof());
        b.fill(cx - 24, g + 8, cz + 96, cx + 24, g + 8, cz + 104, p.roof());
        b.fill(cx - 24, g + 8, cz + 140, cx + 24, g + 8, cz + 148, p.roof());
        b.fill(cx - 34, g + 9, cz + 96, cx + 34, g + 9, cz + 104, p.accent());
        b.fill(cx - 34, g + 9, cz + 140, cx + 34, g + 9, cz + 148, p.accent());

        b.fill(cx - 24, g, cz + 104, cx + 24, g, cz + 140, p.ground());
        b.fill(cx - 20, g, cz + 108, cx + 20, g, cz + 136, p.foundation());
        b.fill(cx - 18, g, cz + 110, cx + 18, g, cz + 134, p.ground());

        // The curator's desk, left as if he stepped away.
        b.put(cx, g + 1, cz + 120, LECTERN);
        signpost(b, cx + 2, g, cz + 120, Direction.WEST, p, "CURADOR", "PERDIDO");
        b.put(cx - 2, g + 1, cz + 118, BOOKS);
        b.put(cx + 2, g + 1, cz + 122, WHITE_CARPET);

        // The galleries and their idols.
        for (int s = -1; s <= 1; s += 2) {
            int gx = cx + s * 29;
            for (int z = cz + 110; z <= cz + 134; z += 8) {
                b.fill(gx - 1, g + 1, z, gx + 1, g + 1, z + 2, p.foundation());
                b.put(gx, g + 2, z + 1, s < 0 ? TERRACOTTA : CARVED);
                b.put(gx, g + 3, z + 1, s < 0 ? MARIGOLD : GOLD);
            }
            b.sign(cx + s * 24, g + 4, cz + 112, s < 0 ? Direction.EAST : Direction.WEST,
                    "SALA", "TEMPLO");
        }
        b.sign(cx, g + 7, cz + 148, Direction.SOUTH, "MUSEO", "TEMPLO MAYOR");
        lamp(b, cx - 20, g + 1, cz + 108, p);
        lamp(b, cx + 20, g + 1, cz + 136, p);
        b.put(cx + 22, g + 1, cz + 106, BARREL);
        b.put(cx - 22, g + 1, cz + 138, BARREL);
    }

    private static void tunnel(StructureBuilder b, int cx, int cz, int g, Palette p) {
        int floor = g - 8;

        // The stair down from the museum court.
        for (int i = 0; i < 9; i++) {
            int y = g - i;
            int z = cz + 136 - i;
            b.fill(cx + 8, y, z, cx + 12, y, z, p.foundation());
            b.put(cx + 7, y, z, p.weathered());
            b.put(cx + 13, y, z, p.weathered());
        }
        b.fill(cx + 7, floor, cz + 128, cx + 7, g, cz + 136, p.weathered());
        b.fill(cx + 13, floor, cz + 128, cx + 13, g, cz + 136, p.weathered());
        b.walls(cx + 7, g, cz + 127, cx + 13, g, cz + 137, p.accent());
        b.fill(cx + 8, g, cz + 137, cx + 12, g, cz + 137, AIR);

        // Connector from the stair foot to the long corridor.
        b.fill(cx - 4, floor, cz + 124, cx + 12, floor, cz + 128, p.foundation());
        b.fill(cx - 4, floor + 1, cz + 124, cx + 12, floor + 3, cz + 128, AIR);
        b.fill(cx - 4, floor + 4, cz + 124, cx + 12, floor + 4, cz + 128, p.foundation());
        b.fill(cx - 4, floor, cz + 124, cx - 4, floor + 4, cz + 128, p.weathered());
        b.fill(cx + 12, floor, cz + 124, cx + 12, floor + 4, cz + 128, p.weathered());

        // The long corridor north to the shrine.
        b.fill(cx - 3, floor, cz + 84, cx + 3, floor, cz + 124, p.foundation());
        b.fill(cx - 3, floor + 1, cz + 84, cx + 3, floor + 3, cz + 124, AIR);
        b.fill(cx - 3, floor + 4, cz + 84, cx + 3, floor + 4, cz + 124, p.foundation());
        b.fill(cx - 4, floor, cz + 84, cx - 4, floor + 4, cz + 124, p.weathered());
        b.fill(cx + 4, floor, cz + 84, cx + 4, floor + 4, cz + 124, p.weathered());
        for (int z = cz + 88; z <= cz + 124; z += 12) {
            b.put(cx - 4, floor + 2, z, CARVED);
            b.put(cx + 4, floor + 2, z, CARVED);
            b.put(cx, floor + 4, z, p.light());
        }

        // The shrine chamber.
        b.fill(cx - 12, floor, cz + 62, cx + 12, floor, cz + 83, p.foundation());
        b.fill(cx - 12, floor + 1, cz + 62, cx + 12, floor + 5, cz + 83, AIR);
        b.fill(cx - 12, floor + 6, cz + 62, cx + 12, floor + 6, cz + 83, p.foundation());
        b.walls(cx - 13, floor, cz + 61, cx + 13, floor + 6, cz + 84, p.weathered());
        b.fill(cx - 3, floor + 1, cz + 84, cx + 3, floor + 4, cz + 84, AIR);
        b.fill(cx - 13, floor, cz + 61, cx + 13, floor, cz + 61, p.accent());

        b.fill(cx - 4, floor + 1, cz + 70, cx + 4, floor + 1, cz + 74, p.accent());
        b.fill(cx - 3, floor + 2, cz + 71, cx + 3, floor + 2, cz + 73, p.foundation());
        b.put(cx, floor + 3, cz + 72, GOLD);
        b.put(cx, floor + 4, cz + 72, p.light());
        b.chest(cx - 8, floor + 1, cz + 76, SiteLoot.RELIC);
        b.chest(cx + 8, floor + 1, cz + 76, SiteLoot.CULT);
        b.put(cx - 11, floor + 5, cz + 63, COBWEB);
        b.put(cx + 11, floor + 5, cz + 81, COBWEB);
        b.put(cx - 10, floor + 1, cz + 66, p.light());
        b.put(cx + 5, floor + 1, cz + 66, p.light());
        b.sign(cx, floor + 3, cz + 61, Direction.SOUTH,
                "ALTAR", "NO TOCAR", "EL DIOS", "DESPIERTA");

        // The second stair back up into the ruins plaza.
        for (int i = 0; i < 9; i++) {
            int y = floor + i;
            int z = cz + 62 + i;
            b.fill(cx + 8, y, z, cx + 12, y, z, p.foundation());
        }
        b.fill(cx + 7, floor, cz + 62, cx + 7, g, cz + 70, p.weathered());
        b.fill(cx + 13, floor, cz + 62, cx + 13, g, cz + 70, p.weathered());
        b.fill(cx + 7, floor + 4, cz + 62, cx + 13, floor + 6, cz + 70, AIR);
        b.walls(cx + 7, g, cz + 61, cx + 13, g, cz + 71, p.foundation());
        b.fill(cx + 8, g, cz + 62, cx + 12, g, cz + 70, AIR);
        signpost(b, cx + 8, g, cz + 138, Direction.SOUTH, p, "TUNEL", "PELIGRO", "NO PASAR");
        signpost(b, cx + 14, g, cz + 70, Direction.WEST, p, "SALIDA", "RUINAS");
    }

    // ------------------------------------------------------------------ reforma

    private static void reforma(StructureBuilder b, int cx, int cz, int g, Palette p) {
        b.fill(cx + 229, g, cz + 186, cx + 231, g, cz + 300, p.foundation());
        reformaHotel(b, cx + 164, cz + 222, cx + 196, cz + 258, g, p);
        cafe(b, cx + 164, cz + 190, cx + 196, cz + 208, g, p, Direction.EAST);
        shopRow(b, cx + 164, cz + 272, cx + 196, cz + 298, g, p, "LIBROS", "TALAVERA");
        shopRow(b, cx + 240, cz + 190, cx + 290, cz + 208, g, p, "SOMBREROS", "DULCES");
        glorieta(b, cx + 240, cz + 222, cx + 290, cz + 258, g, p);
        bank(b, cx + 240, cz + 272, cx + 290, cz + 298, g, p);

        house(b, cx + 140, cz + 190, cx + 150, cz + 208, g, p, 6, Direction.EAST, 8,
                "VECINDAD");
        house(b, cx + 140, cz + 222, cx + 150, cz + 258, g, p, 6, Direction.EAST, 16,
                "VECINDAD");
        house(b, cx + 140, cz + 272, cx + 150, cz + 298, g, p, 6, Direction.EAST, 12,
                "VECINDAD");
        house(b, cx + 304, cz + 190, cx + 320, cz + 208, g, p, 7, Direction.WEST, 8,
                "TALLERES");
        house(b, cx + 304, cz + 222, cx + 320, cz + 258, g, p, 7, Direction.WEST, 16,
                "TALLERES");
        house(b, cx + 304, cz + 272, cx + 320, cz + 298, g, p, 7, Direction.WEST, 12,
                "TALLERES");

        for (int z = cz + 192; z <= cz + 296; z += 12) {
            tree(b, cx + 230, z, g, z / 12 % 2 == 0 ? 1 : 0);
        }
        for (int z = cz + 198; z <= cz + 294; z += 24) {
            lamp(b, cx + 232, g + 1, z, p);
        }
        signpost(b, cx + 230, g, cz + 190, Direction.NORTH, p, "REFORMA", "HOTELES");
    }

    private static void reformaHotel(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                     Palette p) {
        house(b, x0, z0, x1, z1, g, p, 16, Direction.EAST, (z1 - z0) / 2,
                "HOTEL", "REFORMA");
        for (int z = z0 + 4; z <= z1 - 4; z += 6) {
            b.fill(x1 + 2, g + 1, z, x1 + 2, g + 7, z, p.accent());
        }
        b.fill(x1 + 1, g + 8, z0 + 2, x1 + 3, g + 8, z1 - 2, p.roofSlab());
        b.fill(x1 + 1, g + 9, z0 + 2, x1 + 3, g + 9, z1 - 2, p.roof());
        for (int z = z0 + 3; z <= z1 - 3; z += 6) {
            b.put(x0 + 2, g + 1, z, RED_BED);
            b.put(x0 + 3, g + 1, z, RED_BED);
            b.put(x1 - 2, g + 1, z, BOOKS);
        }
        b.sign(x1, g + 12, z0 + (z1 - z0) / 2, Direction.EAST, "HOTEL", "REFORMA");
        b.put(x1 + 2, g + 1, z0 + 4, BARREL);
        lamp(b, x1 + 3, g + 1, z1 - 4, p);
    }

    private static void cafe(StructureBuilder b, int x0, int z0, int x1, int z1, int g, Palette p,
                             Direction front) {
        house(b, x0, z0, x1, z1, g, p, 5, front, (z1 - z0) / 2, "CAFE", "LA ROSA");
        if (front == Direction.EAST) {
            b.fill(x1 + 1, g + 4, z0 + 2, x1 + 3, g + 4, z1 - 2, p.roofSlab());
            for (int z = z0 + 3; z <= z1 - 3; z += 4) {
                b.put(x1 + 3, g + 1, z, p.accent());
                b.put(x1 + 3, g + 2, z, p.accent());
                b.put(x1 + 3, g + 3, z, p.accent());
                b.put(x1 + 4, g + 1, z, p.rail());
                b.put(x1 + 4, g + 2, z, p.roofSlab());
            }
        } else if (front == Direction.WEST) {
            b.fill(x0 - 3, g + 4, z0 + 2, x0 - 1, g + 4, z1 - 2, p.roofSlab());
            for (int z = z0 + 3; z <= z1 - 3; z += 4) {
                b.put(x0 - 3, g + 1, z, p.accent());
                b.put(x0 - 3, g + 2, z, p.accent());
                b.put(x0 - 3, g + 3, z, p.accent());
                b.put(x0 - 4, g + 1, z, p.rail());
                b.put(x0 - 4, g + 2, z, p.roofSlab());
            }
        }
        lamp(b, x1 + 2, g + 1, z1 - 2, p);
    }

    private static void glorieta(StructureBuilder b, int x0, int z0, int x1, int z1, int g,
                                 Palette p) {
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.fill(x0 + 2, g, z0 + 2, x0 + 4, g, z1 - 2, p.foundation());
        b.fill(x1 - 4, g, z0 + 2, x1 - 2, g, z1 - 2, p.foundation());
        b.monument((x0 + x1) / 2, (z0 + z1) / 2, g + 1);
        tree(b, x0 + 5, z0 + 5, g, 1);
        tree(b, x1 - 5, z0 + 5, g, 0);
        tree(b, x0 + 5, z1 - 5, g, 0);
        tree(b, x1 - 5, z1 - 5, g, 1);
        lamp(b, x0 + 8, g + 1, (z0 + z1) / 2, p);
        lamp(b, x1 - 8, g + 1, (z0 + z1) / 2, p);
        signpost(b, x0 + 2, g, z0 + 2, Direction.SOUTH, p, "GLORIETA", "REFORMA");
    }

    private static void bank(StructureBuilder b, int x0, int z0, int x1, int z1, int g, Palette p) {
        house(b, x0, z0, x1, z1, g, p, 20, Direction.WEST, (z1 - z0) / 2,
                "BANCO", "CENTRAL");
        for (int z = z0 + 4; z <= z1 - 4; z += 6) {
            b.fill(x0 - 2, g + 1, z, x0 - 2, g + 8, z, p.accent());
        }
        b.fill(x0 - 3, g + 9, z0 + 2, x0 - 1, g + 9, z1 - 2, p.roofSlab());
        b.fill(x0 - 3, g + 10, z0 + 2, x0 - 1, g + 10, z1 - 2, p.roof());
        b.sign(x0, g + 12, z0 + (z1 - z0) / 2, Direction.WEST, "BANCO", "CENTRAL");
        b.fill(x0 - 1, g + 3, z0 + 4, x0 - 1, g + 3, z1 - 4, ANDESITE);
        b.fill(x0 - 1, g + 3, z0 + 2, x0 - 1, g + 3, z0 + 4, BLUE_GLAZED);
        lamp(b, x0 - 2, g + 1, z1 - 4, p);
    }

    // ------------------------------------------------------------------ rail

    private static void railStation(StructureBuilder b, int cx, int cz, Palette p,
                                    CitySurface surface) {
        int trackZ = cz + 310;
        int trackY = surface.at(cx + 120, trackZ);

        for (int x = cx + 60; x <= cx + 338; x++) {
            int sy = surface.at(x, trackZ);
            if (sy > trackY) {
                b.fill(x, trackY + 1, trackZ - 2, x, sy, trackZ + 2, AIR);
            } else if (sy < trackY) {
                b.fill(x, sy + 1, trackZ - 2, x, trackY, trackZ + 2, p.foundation());
            }
            b.fill(x, trackY, trackZ - 2, x, trackY, trackZ + 2, p.foundation());
            b.put(x, trackY + 1, trackZ - 1, RAIL_EW);
            b.put(x, trackY + 1, trackZ + 1, RAIL_EW);
            b.put(x, trackY + 1, trackZ, p.roofSlab());
        }

        // Platform and station building north of the track.
        gradePad(b, surface, cx + 96, cz + 303, cx + 140, cz + 307, trackY + 1, p);
        b.fill(cx + 96, trackY + 1, cz + 303, cx + 140, trackY + 1, cz + 307, p.foundation());
        b.fill(cx + 98, trackY + 2, cz + 304, cx + 138, trackY + 2, cz + 306, SMOOTH);
        b.fill(cx + 98, trackY + 2, cz + 303, cx + 138, trackY + 2, cz + 303, p.accent());
        for (int x = cx + 100; x <= cx + 136; x += 12) {
            b.fill(x, trackY + 3, cz + 307, x, trackY + 7, cz + 307, p.accent());
            b.put(x, trackY + 8, cz + 305, p.light());
        }
        b.fill(cx + 96, trackY + 8, cz + 303, cx + 140, trackY + 8, cz + 307, p.roofSlab());
        b.fill(cx + 96, trackY + 9, cz + 303, cx + 140, trackY + 9, cz + 307, p.roof());

        gradePad(b, surface, cx + 100, cz + 292, cx + 136, cz + 302, trackY + 1, p);
        house(b, cx + 100, cz + 292, cx + 136, cz + 302, trackY + 1, p, 7,
                Direction.SOUTH, 18, "ESTACION", "CENTRAL");
        b.fill(cx + 120, trackY + 2, cz + 292, cx + 120, trackY + 3, cz + 292, AIR);
        door(b, cx + 120, trackY + 2, cz + 292, Direction.NORTH, p);
        b.sign(cx + 120, trackY + 7, cz + 292, Direction.NORTH, "ESTACION", "CENTRAL");
        b.sign(cx + 120, trackY + 6, cz + 302, Direction.SOUTH, "ESTACION", "CENTRAL");
        b.put(cx + 104, trackY + 2, cz + 296, LECTERN);
        b.put(cx + 108, trackY + 2, cz + 296, BARREL);
        lamp(b, cx + 104, trackY + 1, cz + 303, p);
        lamp(b, cx + 136, trackY + 1, cz + 303, p);

        // The goods shed west of the station.
        gradePad(b, surface, cx + 70, cz + 292, cx + 90, cz + 302, trackY + 1, p);
        house(b, cx + 70, cz + 292, cx + 90, cz + 302, trackY + 1, p, 5,
                Direction.SOUTH, 10, "CARGA");
        b.put(cx + 74, trackY + 2, cz + 296, HAY);
        b.put(cx + 86, trackY + 2, cz + 296, BARREL);
        b.sign(cx + 80, trackY + 5, cz + 302, Direction.SOUTH, "CARGA", "YARDA");

        signpost(b, cx + 120, trackY + 1, cz + 307, Direction.SOUTH, p,
                "ANDEN", "CUIDADO");
        b.fill(cx + 96, trackY + 1, cz + 282, cx + 136, trackY + 1, cz + 290, p.ground());
        b.fill(cx + 96, trackY + 1, cz + 282, cx + 136, trackY + 1, cz + 282, p.accent());
    }

    private static void gradePad(StructureBuilder b, CitySurface s, int x0, int z0, int x1,
                                 int z1, int y, Palette p) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                int sy = s.at(x, z);
                if (sy > y) {
                    b.fill(x, y + 1, z, x, sy, z, AIR);
                } else if (sy < y) {
                    b.fill(x, sy + 1, z, x, y, z, p.foundation());
                }
            }
        }
    }

    // ------------------------------------------------------------------ shared forms

    private static void house(StructureBuilder b, int x0, int z0, int x1, int z1, int g, Palette p,
                              int h, Direction front, int offset, String... lines) {
        b.fill(x0, g, z0, x1, g, z1, p.ground());
        b.walls(x0, g + 1, z0, x1, g + h, z1, p.wall());
        b.fill(x0, g + 1, z0, x1, g + 1, z1, p.foundation());
        b.fill(x0 + 1, g + 1, z0 + 1, x1 - 1, g + 1, z1 - 1, p.ground());
        b.fill(x0 + 1, g + 2, z0 + 1, x1 - 1, g + h - 1, z1 - 1, AIR);

        int len = front == Direction.NORTH || front == Direction.SOUTH ? x1 - x0 : z1 - z0;
        int off = Math.clamp(offset, 2, Math.max(2, len - 2));
        int dx;
        int dz;
        switch (front) {
            case NORTH -> {
                dx = x0 + off;
                dz = z0;
            }
            case SOUTH -> {
                dx = x0 + off;
                dz = z1;
            }
            case WEST -> {
                dx = x0;
                dz = z0 + off;
            }
            default -> {
                dx = x1;
                dz = z0 + off;
            }
        }
        b.fill(dx, g + 1, dz, dx, g + 2, dz, AIR);
        door(b, dx, g + 1, dz, front, p);

        for (int y = g + 3; y <= g + h - 2; y += 3) {
            for (int x = x0 + 3; x <= x1 - 3; x += 4) {
                b.window(x, y, z0, 2, 2, true);
                b.window(x, y, z1, 2, 2, true);
            }
            for (int z = z0 + 3; z <= z1 - 3; z += 4) {
                b.window(x0, y, z, 2, 1, true);
                b.window(x1, y, z, 2, 1, true);
            }
        }

        if (h >= 8) {
            b.fill(x0 - 1, g + h + 1, z0 - 1, x1 + 1, g + h + 1, z1 + 1, p.roof());
            b.walls(x0 - 1, g + h + 2, z0 - 1, x1 + 1, g + h + 2, z1 + 1, p.accent());
        } else {
            b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, g + h + 1, 4,
                    x1 - x0 >= z1 - z0 ? 0 : 1);
        }
        b.sign(dx, g + 3, dz, front, lines);
    }

    private static void door(StructureBuilder b, int x, int y, int z, Direction facing, Palette p) {
        BlockState lower = p.door()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        b.put(x, y, z, lower);
        b.put(x, y + 1, z,
                lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
    }

    private static void lamp(StructureBuilder b, int x, int y, int z, Palette p) {
        b.put(x, y, z, p.rail());
        b.put(x, y + 1, z, p.light());
    }

    private static void signpost(StructureBuilder b, int x, int y, int z, Direction facing,
                                 Palette p, String... lines) {
        b.put(x, y, z, p.foundation());
        CityPlanSupport.sign(b, x, y + 1, z, facing, lines);
    }

    private static void fountain(StructureBuilder b, int x, int z, int g, Palette p) {
        b.fill(x - 3, g, z - 3, x + 3, g, z + 3, p.foundation());
        b.fill(x - 2, g + 1, z - 2, x + 2, g + 1, z + 2, WATER);
        b.walls(x - 2, g + 1, z - 2, x + 2, g + 1, z + 2, p.accent());
        b.put(x, g + 2, z, p.accent());
        b.put(x, g + 3, z, p.light());
    }

    private static void tree(StructureBuilder b, int x, int z, int g, int kind) {
        if (kind == 0) {
            b.fill(x, g + 1, z, x, g + 7, z, CYPRESS_LOG);
            for (int dy = 5; dy <= 8; dy++) {
                int r = dy <= 6 ? 2 : 1;
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (dx == 0 && dz == 0) {
                            continue;
                        }
                        if (Math.abs(dx) + Math.abs(dz) > r + 1) {
                            continue;
                        }
                        b.put(x + dx, g + dy, z + dz, CYPRESS_LEAF);
                    }
                }
            }
            b.put(x, g + 8, z, CYPRESS_LEAF);
        } else {
            b.fill(x, g + 1, z, x, g + 5, z, JACARANDA_LOG);
            for (int dy = 4; dy <= 7; dy++) {
                int r = dy <= 5 ? 2 : 1;
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (dx == 0 && dz == 0) {
                            continue;
                        }
                        if (Math.abs(dx) + Math.abs(dz) > r + 1) {
                            continue;
                        }
                        b.put(x + dx, g + dy, z + dz, JACARANDA);
                    }
                }
            }
            b.put(x, g + 7, z, JACARANDA);
        }
    }

    private static void agave(StructureBuilder b, int x, int z, int g) {
        b.put(x, g, z, SAND);
        b.put(x, g + 1, z, CACTUS);
        b.put(x - 1, g + 1, z, GRASS_TUFT);
        b.put(x + 1, g + 1, z, GRASS_TUFT);
        b.put(x, g + 1, z - 1, GRASS_TUFT);
        b.put(x, g + 1, z + 1, GRASS_TUFT);
    }

    private static void marigoldBed(StructureBuilder b, int x0, int z0, int x1, int z1, int g) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                if ((x * 7 + z * 13) % 4 == 0) {
                    b.put(x, g + 1, z, MARIGOLD);
                }
            }
        }
    }

    private static void stall(StructureBuilder b, int x, int z, int g, Palette p, BlockState cloth) {
        b.fill(x - 2, g, z - 2, x + 2, g, z + 2, p.ground());
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                b.put(x + sx * 2, g + 1, z + sz * 2, p.accent());
                b.put(x + sx * 2, g + 2, z + sz * 2, p.accent());
                b.put(x + sx * 2, g + 3, z + sz * 2, p.accent());
            }
        }
        b.fill(x - 2, g + 4, z - 2, x + 2, g + 4, z + 2, p.roofSlab());
        b.put(x - 1, g + 4, z - 2, cloth);
        b.put(x + 1, g + 4, z - 2, cloth);
        b.put(x - 1, g + 4, z + 2, cloth);
        b.put(x + 1, g + 4, z + 2, cloth);
        b.put(x, g + 1, z - 1, p.foundation());
        b.put(x, g + 2, z - 1, (x + z) % 2 == 0 ? HAY : BARREL);
        b.put(x - 1, g + 1, z + 1, MARIGOLD);
        b.put(x + 1, g + 3, z + 2, p.light());
    }

    private static void awning(StructureBuilder b, int x0, int x1, int z, int g, Palette p) {
        b.fill(x0, g + 4, z, x1, g + 4, z + 1, p.roofSlab());
        b.put(x0, g + 1, z + 1, p.accent());
        b.put(x0, g + 2, z + 1, p.accent());
        b.put(x0, g + 3, z + 1, p.accent());
        b.put(x1, g + 1, z + 1, p.accent());
        b.put(x1, g + 2, z + 1, p.accent());
        b.put(x1, g + 3, z + 1, p.accent());
    }

    private static void naturalRuin(StructureBuilder b, int x0, int z0, int x1, int z1, Palette p) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                int y = b.groundY(x, z);
                int h = x * 31 + z * 17 & 15;
                if (h < 3) {
                    b.put(x, y + 1, z, p.rubble());
                } else if (h < 5) {
                    b.put(x, y + 1, z, p.weathered());
                } else if (h < 6) {
                    b.put(x, y + 1, z, p.weathered());
                    b.put(x, y + 2, z, p.weathered());
                } else if (h == 6) {
                    b.put(x, y + 1, z, DEAD_BUSH);
                }
            }
        }
        for (int x = x0; x <= x1; x += 4) {
            int y = b.groundY(x, z0);
            b.put(x, y + 1, z0, p.weathered());
            b.put(x, y + 2, z0, p.weathered());
        }
    }

    private static void marshEdge(StructureBuilder b, int x0, int z0, int x1, int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                int y = b.groundY(x, z);
                int h = x * 11 + z * 29 & 7;
                if (h == 0) {
                    b.put(x, y + 1, z, SUGAR_CANE);
                } else if (h == 1) {
                    b.put(x, y, z, CLAY);
                } else if (h == 2) {
                    b.put(x, y + 1, z, FERN);
                }
            }
        }
    }
}
