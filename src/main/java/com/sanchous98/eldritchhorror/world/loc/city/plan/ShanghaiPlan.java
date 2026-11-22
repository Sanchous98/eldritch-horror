package com.sanchous98.eldritchhorror.world.loc.city.plan;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.SiteLoot;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import com.sanchous98.eldritchhorror.world.loc.city.CitySurface;
import com.sanchous98.eldritchhorror.world.loc.style.Materials;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.RailShape;

import java.util.List;

/**
 * Shanghai, 1920s treaty port: the Bund's grey stone waterfront, Nanjing Road's neon shops, the
 * Old City's crooked lanes, the French Concession's plane trees and villas, and Pudong's docks
 * under the Pearl Tower.
 *
 * <p>Everything here is authored: absolute offsets from {@code (cx, cz)}, the city's {@code ground}
 * level, and explicit loops/arrays. There is no {@code RandomSource}, no position hash for layout
 * and no chunk-local read; {@link StructureBuilder} clips every write to the generating chunk, so
 * the same call builds the same city slice in every chunk. Streets are paved in a first pass and
 * buildings are raised afterwards, which keeps a long facade from ever overwriting a road.
 */
public final class ShanghaiPlan implements CityPlan {

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState LEAVES = Blocks.OAK_LEAVES.defaultBlockState();
    private static final BlockState BOOKS = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState CAULDRON = Blocks.CAULDRON.defaultBlockState();
    private static final BlockState BARREL = Blocks.BARREL.defaultBlockState();
    private static final BlockState RAIL = Blocks.RAIL.defaultBlockState()
            .setValue(BlockStateProperties.RAIL_SHAPE, RailShape.NORTH_SOUTH);

    @Override
    public String cityId() {
        return "shanghai";
    }

    @Override
    public int halfExtent() {
        return 340;
    }

    @Override
    public void build(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // 1. Water and streets first, so no facade can later pave over a road or a quay.
        river(b, cx, cz, ground, p);
        streets(b, cx, cz, ground, p);

        // 2. The five districts, the gate and the landmark.
        gate(b, cx, cz, ground, p);
        signpost(b, cx, cz, ground, p);
        bund(b, cx, cz, ground, p);
        nanjing(b, cx, cz, ground, p);
        peoplesSquare(b, cx, cz, ground, p);
        oldCity(b, cx, cz, ground, p);
        frenchConcession(b, cx, cz, ground, p);
        pudong(b, cx, cz, ground, p);
        pearlTower(b, cx, cz, ground, p);

        b.marker("shanghai_center", cx, ground + 1, cz);
    }

    @Override
    public void build(StructureBuilder b, CityPlanContext ctx, Palette p) {
        // The authored core is flat at ground; only the new rim terminus uses the graded surface.
        build(b, ctx.cx(), ctx.cz(), ctx.ground(), p);
        railStation(b, ctx.cx(), ctx.cz(), p, ctx.surface());
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                // Arrival and the five-way signpost.
                new CityPoi("gate_signpost", CityPoi.Kind.SIGN, -312, 1, 0,
                        "five-way signpost at the west gate"),
                // The Bund.
                new CityPoi("bund_quest_board", CityPoi.Kind.QUEST_BOARD, 132, 1, 0,
                        "quest=shanghai_bund_cargo; notice board at the Bund"),
                new CityPoi("astor_house", CityPoi.Kind.INN, 105, 1, -80,
                        "hotel; clerk; rentable room"),
                new CityPoi("customs_house", CityPoi.Kind.LANDMARK, 105, 1, 60,
                        "customs hall; clock tower; manifest"),
                new CityPoi("clerk", CityPoi.Kind.NPC, 105, 1, -80,
                        "npc=clerk; points to the quest board"),
                // Nanjing Road.
                new CityPoi("wing_on", CityPoi.Kind.SHOP, 20, 1, -20,
                        "department store; shopkeeper; crates"),
                // People's Square.
                new CityPoi("peoples_square", CityPoi.Kind.LANDMARK, -48, 1, -40,
                        "paved plaza; monument; musicians"),
                // Old City.
                new CityPoi("yu_garden", CityPoi.Kind.LANDMARK, -190, 1, 120,
                        "pond; zigzag bridge; willows"),
                new CityPoi("old_temple", CityPoi.Kind.LANDMARK, -238, 1, 172,
                        "temple hall; incense burner"),
                new CityPoi("herbalist", CityPoi.Kind.HEALER, -150, 1, 150,
                        "npc=herbalist; remedy; cauldron"),
                new CityPoi("tea_house", CityPoi.Kind.INN, -180, 1, 190,
                        "npc=tea_keeper; cellar stairs"),
                new CityPoi("tea_keeper", CityPoi.Kind.NPC, -180, 1, 190,
                        "npc=tea_keeper; pours tea"),
                new CityPoi("old_city_cellar", CityPoi.Kind.SECRET, -190, -6, 210,
                        "chest; lore book; cult relic"),
                new CityPoi("cult_defector", CityPoi.Kind.NPC, -186, -6, 214,
                        "npc=cult_defector; spare or report"),
                // French Concession.
                new CityPoi("order_safehouse", CityPoi.Kind.SECRET, -220, 1, -170,
                        "npc=order_agent; unmarked door; ledger"),
                new CityPoi("order_agent", CityPoi.Kind.NPC, -220, 1, -170,
                        "npc=order_agent; The Bund Cargo"),
                new CityPoi("french_cafe", CityPoi.Kind.INN, -160, 1, -150,
                        "npc=cafe_owner; quest=shanghai_vanishing"),
                new CityPoi("cafe_owner", CityPoi.Kind.NPC, -160, 1, -150,
                        "npc=cafe_owner; missing violinist"),
                new CityPoi("concession_clinic", CityPoi.Kind.HEALER, -170, 1, -120,
                        "small clinic; beds"),
                // Pudong.
                new CityPoi("dock_7", CityPoi.Kind.LANDMARK, 250, 1, 120,
                        "npc=dockworker; crane; cult shrine"),
                new CityPoi("dockworker", CityPoi.Kind.NPC, 250, 1, 120,
                        "npc=dockworker; strike talk; shrine hint"),
                new CityPoi("pearl_tower", CityPoi.Kind.LANDMARK, 260, 1, 0,
                        "three spheres; observation decks; spire"),
                new CityPoi("notice_board", CityPoi.Kind.SIGN, -48, 1, 40,
                        "city notices; quests"),
                // Transport anchors.
                new CityPoi("shanghai_station", CityPoi.Kind.STATION, 0, 1, -310,
                        "rail terminus; straight level track north to the edge"),
                new CityPoi("huangpu_port", CityPoi.Kind.PORT, 190, 1, 120,
                        "Pudong quay water edge; Dock 7")
        );
    }

    // ------------------------------------------------------------------ water + streets

    private static void river(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int z0 = cz - 340;
        int z1 = cz + 340;
        // A five-deep channel, bedded on gravel, with the banks cut down to the water.
        box(b, cx + 155, ground - 8, z0, cx + 190, ground - 8, z1, p.rubble());
        box(b, cx + 155, ground - 7, z0, cx + 190, ground - 4, z1, WATER);
        box(b, cx + 155, ground - 3, z0, cx + 190, ground + 1, z1, AIR);
        // Stone quays on both banks.
        box(b, cx + 150, ground - 4, z0, cx + 155, ground, z1, p.foundation());
        box(b, cx + 190, ground - 4, z0, cx + 195, ground, z1, p.foundation());
        pave(b, cx + 150, z0, cx + 154, z1, ground, p);
        pave(b, cx + 191, z0, cx + 195, z1, ground, p);
        // Mooring bollards along the Bund quay.
        for (int z = z0 + 12; z < z1; z += 24) {
            if (Math.abs(z - (cz - 40)) < 8 || Math.abs(z - (cz + 140)) < 8) {
                continue;
            }
            b.put(cx + 151, ground + 1, z, p.accent());
            b.put(cx + 151, ground + 2, z, p.rail());
        }
    }

    private static void streets(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Nanjing Road: the long shopping spine from the west gate to the Bund.
        pave(b, cx - 322, cz - 7, cx + 152, cz + 7, ground, p);
        box(b, cx - 322, ground, cz - 8, cx + 152, ground, cz - 8, p.foundation());
        box(b, cx - 322, ground, cz + 8, cx + 152, ground, cz + 8, p.foundation());

        // The Bund promenade, with a kerb at the building line and a balustrade at the river.
        pave(b, cx + 120, cz - 264, cx + 150, cz + 264, ground, p);
        box(b, cx + 120, ground, cz - 264, cx + 120, ground, cz + 264, p.foundation());
        balustrade(b, cx + 150, cz - 264, cz + 264, ground, p);

        // People's Square: an 82-wide paved plaza inside a ring road, split by Nanjing Road.
        box(b, cx - 90, ground, cz - 70, cx - 8, ground, cz + 70, p.ground());
        box(b, cx - 90, ground, cz - 70, cx - 90, ground, cz + 70, p.foundation());
        box(b, cx - 8, ground, cz - 70, cx - 8, ground, cz + 70, p.foundation());
        box(b, cx - 90, ground, cz - 70, cx - 8, ground, cz - 70, p.foundation());
        box(b, cx - 90, ground, cz + 70, cx - 8, ground, cz + 70, p.foundation());
        pave(b, cx - 104, cz - 70, cx - 90, cz + 70, ground, p);
        pave(b, cx - 8, cz - 70, cx + 6, cz + 70, ground, p);
        pave(b, cx - 104, cz - 84, cx + 6, cz - 70, ground, p);
        pave(b, cx - 104, cz + 70, cx + 6, cz + 84, ground, p);

        oldCityStreets(b, cx, cz, ground, p);
        frenchStreets(b, cx, cz, ground, p);

        // Pudong: the quay road, the wharf apron and the tower plaza.
        pave(b, cx + 210, cz - 264, cx + 222, cz + 264, ground, p);
        pave(b, cx + 195, cz - 264, cx + 210, cz + 264, ground, p);
        box(b, cx + 210, ground, cz - 264, cx + 210, ground, cz + 264, p.foundation());
        box(b, cx + 230, ground, cz - 30, cx + 290, ground, cz + 30, p.foundation());
        pave(b, cx + 234, cz - 26, cx + 286, cz + 26, ground, p);
        pave(b, cx + 222, cz - 6, cx + 230, cz + 6, ground, p);

        // The arrival gate's apron.
        pave(b, cx - 330, cz - 14, cx - 310, cz + 14, ground, p);

        // The two stone bridges over the Huangpu.
        bridge(b, cx, cz, ground, p, -40);
        bridge(b, cx, cz, ground, p, 140);
    }

    private static void oldCityStreets(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Four north-south lanes and three east-west lanes, width 6.
        int[] vx = {-258, -218, -166, -126};
        for (int x : vx) {
            pave(b, cx + x - 3, cz + 84, cx + x + 2, cz + 276, ground, p);
        }
        int[] hz = {92, 236, 272};
        for (int z : hz) {
            pave(b, cx - 276, cz + z - 3, cx - 84, cz + z + 2, ground, p);
        }
        // The crooked lane: north along the garden, east under the tea house, then south-east.
        pave(b, cx - 221, cz + 96, cx - 215, cz + 168, ground, p);
        pave(b, cx - 221, cz + 165, cx - 163, cz + 171, ground, p);
        pave(b, cx - 169, cz + 165, cx - 163, cz + 232, ground, p);
        pave(b, cx - 169, cz + 229, cx - 123, cz + 235, ground, p);
        // A short back lane behind the herbalist.
        pave(b, cx - 137, cz + 147, cx - 123, cz + 153, ground, p);
        // The temple forecourt.
        box(b, cx - 224, ground, cz + 148, cx - 215, ground, cz + 198, p.ground());
    }

    private static void frenchStreets(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int[] vx = {-248, -196, -144, -92};
        for (int x : vx) {
            pave(b, cx + x - 4, cz - 280, cx + x + 3, cz - 88, ground, p);
            box(b, cx + x - 4, ground, cz - 280, cx + x - 4, ground, cz - 88, p.foundation());
            box(b, cx + x + 3, ground, cz - 280, cx + x + 3, ground, cz - 88, p.foundation());
        }
        int[] hz = {-248, -196, -144, -92};
        for (int z : hz) {
            pave(b, cx - 280, cz + z - 4, cx - 88, cz + z + 3, ground, p);
        }
    }

    private static void bridge(StructureBuilder b, int cx, int cz, int ground, Palette p, int zc) {
        int x0 = cx + 155;
        int x1 = cx + 190;
        int z0 = cz + zc - 4;
        int z1 = cz + zc + 4;
        box(b, x0, ground, z0, x1, ground, z1, p.foundation());
        // A stepped stone arch below the deck, deep enough to clear the water.
        box(b, x0 + 1, ground - 1, z0 + 1, x1 - 1, ground - 1, z1 - 1, p.foundation());
        box(b, x0 + 4, ground - 2, z0 + 1, x1 - 4, ground - 2, z1 - 1, p.foundation());
        box(b, x0 + 8, ground - 3, z0 + 2, x1 - 8, ground - 3, z1 - 2, p.foundation());
        box(b, cx + 171, ground - 4, z0 + 3, cx + 174, ground - 4, z1 - 3, p.accent());
        // Rails and lamps at the approaches.
        for (int x = x0; x <= x1; x++) {
            if ((x & 1) == 0) {
                b.put(x, ground + 1, z0, p.rail());
                b.put(x, ground + 1, z1, p.rail());
            }
        }
        for (int x = x0 + 2; x <= x1 - 2; x += 8) {
            b.put(x, ground + 2, z0, p.rail());
            b.put(x, ground + 2, z1, p.rail());
        }
        lamp(b, x0, z0, ground, p);
        lamp(b, x1, z0, ground, p);
        lamp(b, x0, z1, ground, p);
        lamp(b, x1, z1, ground, p);
    }

    // ------------------------------------------------------------------ arrival gate + signpost

    private static void gate(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 326;
        int x1 = cx - 320;
        // Two square pylons flanking the road, with a stone lintel between them.
        for (int s = -1; s <= 1; s += 2) {
            int z0 = cz + (s < 0 ? -12 : 7);
            int z1 = cz + (s < 0 ? -7 : 12);
            box(b, x0, ground + 1, z0, x1, ground + 12, z1, p.wall());
            box(b, x0, ground + 1, z0, x1, ground + 2, z1, p.foundation());
            for (int y = ground + 1; y <= ground + 12; y++) {
                b.put(x0, y, z0, p.accent());
                b.put(x1, y, z1, p.accent());
            }
            b.crenellations(x0, z0, x1, z1, ground + 13);
            b.put(cx - 323, ground + 13, cz + s * 10, p.light());
        }
        box(b, x0, ground + 9, cz - 12, x1, ground + 10, cz + 12, p.accent());
        box(b, x0, ground + 11, cz - 12, x1, ground + 11, cz + 12, p.roofSlab());
        // The city's name over the road, readable from the approach. The signs stand on the lintel.
        sign(b, cx - 322, ground + 11, cz, Direction.EAST, "SHANGHAI", "TREATY PORT");
        sign(b, cx - 324, ground + 11, cz, Direction.WEST, "SHANGHAI", "WEST GATE");
    }

    private static void signpost(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x = cx - 312;
        box(b, x - 2, ground, cz - 2, x + 2, ground, cz + 2, p.foundation());
        for (int y = ground + 1; y <= ground + 3; y++) {
            b.put(x, y, cz, p.accent());
        }
        b.put(x, ground + 4, cz, p.light());
        // Two side posts carry the two sign boards; the central post carries the lamp.
        for (int s = -1; s <= 1; s += 2) {
            b.put(x + s, ground + 1, cz, p.accent());
            b.put(x + s, ground + 2, cz, p.accent());
        }
        // Four lines on the east face and the fifth on the west face: a vanilla sign holds four.
        sign(b, x + 1, ground + 3, cz, Direction.EAST,
                "THE BUND ->", "NANJING RD ->", "OLD CITY v", "FRENCH CONC ^");
        sign(b, x - 1, ground + 3, cz, Direction.WEST,
                "PUDONG DOCKS >", "SHANGHAI", "WEST GATE", "5 DISTRICTS");
    }

    // ------------------------------------------------------------------ rail terminus

    /**
     * The northern rail terminus. The station straddles the graded rim, so the bed and the shed
     * floor follow {@code surface.at(x,z)}; the rail itself is one straight, level line from the
     * city edge into the train shed, ready for the transport pass to connect at {@code z=-340}.
     */
    private static void railStation(StructureBuilder b, int cx, int cz, Palette p,
                                    CitySurface surface) {
        int trackY = surface.at(cx, cz - 320);
        int zEdge = cz - 340;
        int zHall = cz - 300;
        // Level bed: fill hollows and cut rises so the rail stays at one Y.
        for (int z = zEdge; z <= zHall; z++) {
            for (int x = -4; x <= 4; x++) {
                int wx = cx + x;
                int surf = surface.at(wx, z);
                if (surf < trackY - 1) {
                    b.fill(wx, surf + 1, z, wx, trackY - 1, z, p.foundation());
                } else if (surf > trackY) {
                    b.fill(wx, trackY + 1, z, wx, surf, z, AIR);
                }
                b.put(wx, trackY - 1, z, p.foundation());
                b.put(wx, trackY, z, p.rubble());
            }
        }
        // The straight, level rail from the edge into the shed.
        for (int z = zEdge; z <= cz - 302; z++) {
            b.put(cx, trackY, z, RAIL);
            if (((z - zEdge) & 1) == 0) {
                b.put(cx, trackY - 1, z, p.accent());
            }
        }
        // Train shed: platforms either side of the track trench, high roof and south entrance.
        b.fill(cx - 24, trackY + 1, cz - 320, cx - 4, trackY + 1, cz - 300, p.foundation());
        b.fill(cx + 4, trackY + 1, cz - 320, cx + 24, trackY + 1, cz - 300, p.foundation());
        b.fill(cx - 3, trackY, cz - 302, cx + 3, trackY, cz - 300, p.foundation());
        b.walls(cx - 24, trackY + 2, cz - 320, cx + 24, trackY + 12, cz - 300, p.wall());
        b.fill(cx - 23, trackY + 2, cz - 319, cx + 23, trackY + 11, cz - 301, AIR);
        b.fill(cx - 4, trackY, cz - 320, cx + 4, trackY + 5, cz - 320, AIR);
        b.fill(cx - 6, trackY + 2, cz - 300, cx + 6, trackY + 4, cz - 300, AIR);
        b.pitchedRoof(cx - 26, cz - 322, cx + 26, cz - 298, trackY + 12, 5, 0);
        for (int z = cz - 318; z <= cz - 302; z += 4) {
            b.put(cx - 4, trackY + 1, z, p.accent());
            b.put(cx + 4, trackY + 1, z, p.accent());
            b.put(cx - 4, trackY + 2, z, p.light());
            b.put(cx + 4, trackY + 2, z, p.light());
        }
        sign(b, cx + 10, trackY + 2, cz - 300, Direction.SOUTH,
                "SHANGHAI STN", "PLATFORM 1");
        // Yard: water tower, signal, crates and the edge sign, all on the graded surface.
        int towerY = surface.at(cx + 16, cz - 330) + 1;
        box(b, cx + 15, towerY, cz - 331, cx + 17, towerY, cz - 329, p.foundation());
        for (int dx = 15; dx <= 17; dx += 2) {
            for (int dz = -331; dz <= -329; dz += 2) {
                b.fill(cx + dx, towerY + 1, cz + dz, cx + dx, towerY + 3, cz + dz, p.accent());
            }
        }
        box(b, cx + 15, towerY + 4, cz - 331, cx + 17, towerY + 5, cz - 329, p.foundation());
        b.put(cx + 16, towerY + 4, cz - 330, WATER);
        b.put(cx + 16, towerY + 1, cz - 330, p.rail());
        b.put(cx + 16, towerY + 6, cz - 330, p.light());
        int signalY = surface.at(cx - 12, cz - 324) + 1;
        b.put(cx - 12, signalY, cz - 324, p.rail());
        b.put(cx - 12, signalY + 1, cz - 324, p.rail());
        b.put(cx - 12, signalY + 2, cz - 324, p.light());
        crates(b, cx - 20, cz - 334, surface.at(cx - 20, cz - 334), p, 4);
        b.put(cx + 20, surface.at(cx + 20, cz - 326) + 1, cz - 326, BARREL);
        sign(b, cx - 2, trackY + 1, cz - 338, Direction.SOUTH, "RAILWAY", "SHANGHAI");
    }

    // ------------------------------------------------------------------ the Bund

    private static void bund(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // The long colonial frontage, broken into authored blocks by cross alleys.
        colonial(b, cx, cz, ground, p, -260, -220, 5);
        colonial(b, cx, cz, ground, p, -212, -168, 4);
        colonial(b, cx, cz, ground, p, -160, -120, 6);
        colonial(b, cx, cz, ground, p, -112, -100, 3);
        astorHouse(b, cx, cz, ground, p);
        colonial(b, cx, cz, ground, p, -56, -20, 4);
        colonial(b, cx, cz, ground, p, 20, 40, 3);
        customsHouse(b, cx, cz, ground, p);
        colonial(b, cx, cz, ground, p, 80, 120, 5);
        ruinedFacade(b, cx, cz, ground, p, 128, 168);
        colonial(b, cx, cz, ground, p, 176, 216, 4);
        colonial(b, cx, cz, ground, p, 224, 260, 6);

        // Street furniture: lamps, benches, lions, the notice board, sampans.
        for (int z = cz - 252; z <= cz + 252; z += 28) {
            lamp(b, cx + 124, z, ground, p);
            lamp(b, cx + 146, z + 14, ground, p);
        }
        for (int z = cz - 236; z <= cz + 236; z += 56) {
            bench(b, cx + 142, z, ground, p);
        }
        statueLion(b, cx + 119, cz + 42, ground, p);
        statueLion(b, cx + 119, cz + 78, ground, p);
        questBoard(b, cx, cz, ground, p);
        for (int z : new int[]{-120, -20, 80, 200}) {
            sampan(b, cx + 158, cz + z, ground, p);
        }
        for (int z : new int[]{-140, -50, 110, 220}) {
            crates(b, cx + 144, cz + z, ground, p, 3);
        }
    }

    /**
     * One colonial waterfront block: a stone shell of {@code storeys} floors, an iron-mullioned
     * window grid, a ground-floor arcade and a shallow slate roof.
     */
    private static void colonial(StructureBuilder b, int cx, int cz, int ground, Palette p,
                                 int z0, int z1, int storeys) {
        int x0 = cx + 92;
        int x1 = cx + 118;
        int top = ground + storeys * 4;
        for (int s = 0; s < storeys; s++) {
            int y = ground + s * 4;
            b.room(x0, y, z0 + cz, x1, y + 4, z1 + cz);
            for (int wz = z0 + 3; wz <= z1 - 3; wz += 4) {
                b.window(x1, y + 2, cz + wz, 2, 1, true);
                b.window(x0, y + 2, cz + wz, 2, 1, true);
            }
            for (int wx = x0 + 3; wx <= x1 - 3; wx += 4) {
                b.window(wx, y + 2, cz + z0, 2, 1, true);
                b.window(wx, y + 2, cz + z1, 2, 1, true);
            }
            b.walls(x0 - 1, y + 4, cz + z0 - 1, x1 + 1, y + 4, cz + z1 + 1, p.accent());
        }
        b.walls(x0, ground + 1, cz + z0, x1, ground + 1, cz + z1, p.foundation());
        for (int y = ground + 1; y <= top; y++) {
            b.put(x0, y, cz + z0, p.accent());
            b.put(x1, y, cz + z1, p.accent());
        }
        // The arcade columns stand one block out from the front wall.
        for (int wz = z0 + 2; wz <= z1 - 2; wz += 4) {
            box(b, cx + 119, ground + 1, cz + wz, cx + 119, ground + 3, cz + wz, p.accent());
        }
        box(b, cx + 119, ground + 4, cz + z0, cx + 119, ground + 4, cz + z1, p.accent());
        // A door into the arcade, clear of the window grid.
        int dz = (z0 + z1) / 2;
        door(b, p, x1, ground + 1, cz + dz, Direction.EAST);
        b.pitchedRoof(x0 - 1, cz + z0 - 1, x1 + 1, cz + z1 + 1, top, 3, 1);
    }

    private static void astorHouse(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        colonial(b, cx, cz, ground, p, -96, -64, 6);
        int top = ground + 24;
        // The corner entrance faces Nanjing Road.
        door(b, p, cx + 105, ground + 1, cz - 64, Direction.SOUTH);
        sign(b, cx + 100, ground + 1, cz - 65, Direction.SOUTH, "ASTOR HOUSE", "ROOMS TO LET");
        // A balcony on the river face.
        box(b, cx + 119, ground + 12, cz - 92, cx + 119, ground + 12, cz - 68, p.roofSlab());
        for (int z = cz - 92; z <= cz - 68; z += 3) {
            b.put(cx + 119, ground + 13, z, p.rail());
        }
        // A flagpole and a red flag on the roof.
        for (int y = top + 4; y <= top + 9; y++) {
            b.put(cx + 105, y, cz - 80, p.accent());
        }
        box(b, cx + 105, top + 9, cz - 79, cx + 105, top + 9, cz - 77, Materials.wool(DyeColor.RED));
        // A lit ground-floor lounge, visible from the promenade.
        for (int z = cz - 90; z <= cz - 70; z += 5) {
            b.put(cx + 119, ground + 2, z, p.light());
        }
        b.put(cx + 100, ground + 3, cz - 80, p.light());
        b.put(cx + 110, ground + 3, cz - 80, p.light());
        b.marker("astor_clerk", cx + 105, ground + 1, cz - 80);
    }

    private static void customsHouse(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        colonial(b, cx, cz, ground, p, 46, 74, 5);
        int tx = cx + 105;
        int tz = cz + 60;
        // The clock tower rises out of the roof.
        b.room(tx - 3, ground + 20, tz - 3, tx + 3, ground + 34, tz + 3);
        b.walls(tx - 3, ground + 20, tz - 3, tx + 3, ground + 34, tz + 3, p.wall());
        for (int y = ground + 20; y <= ground + 34; y++) {
            b.put(tx - 3, y, tz - 3, p.accent());
            b.put(tx + 3, y, tz + 3, p.accent());
        }
        clockFace(b, tx, tz, ground + 28, p, Direction.EAST);
        clockFace(b, tx, tz, ground + 28, p, Direction.SOUTH);
        b.crenellations(tx - 4, tz - 4, tx + 4, tz + 4, ground + 35);
        b.spire(tx, tz, ground + 36, 8);
        // The hall entrance and its sign.
        door(b, p, cx + 105, ground + 1, cz + 46, Direction.NORTH);
        sign(b, cx + 120, ground + 1, cz + 60, Direction.EAST, "CUSTOMS", "HIS MAJESTY'S");
        sign(b, cx + 110, ground + 1, cz + 45, Direction.NORTH, "CUSTOMS HOUSE", "MANIFESTS");
        b.marker("customs_manifest", tx, ground + 2, cz + 70);
    }

    /** A 3x3 lit clock face on one side of the customs tower. */
    private static void clockFace(StructureBuilder b, int tx, int tz, int y, Palette p,
                                  Direction face) {
        int fx = tx;
        int fz = tz;
        if (face == Direction.EAST) {
            fx = tx + 3;
        } else if (face == Direction.SOUTH) {
            fz = tz + 3;
        }
        for (int d = -1; d <= 1; d++) {
            for (int e = -1; e <= 1; e++) {
                if (face == Direction.EAST) {
                    b.put(fx, y + d, tz + e, p.light());
                } else {
                    b.put(tx + d, y + e, fz, p.light());
                }
            }
        }
        // The hands, in dark iron over the glow.
        if (face == Direction.EAST) {
            b.put(fx, y, tz, p.frame());
            b.put(fx, y + 1, tz, p.frame());
            b.put(fx, y, tz + 1, p.frame());
        } else {
            b.put(tx, y, fz, p.frame());
            b.put(tx, y + 1, fz, p.frame());
            b.put(tx + 1, y, fz, p.frame());
        }
    }

    /** A Bund block that was shelled in the fighting: authored holes, boards and rubble. */
    private static void ruinedFacade(StructureBuilder b, int cx, int cz, int ground, Palette p,
                                     int z0, int z1) {
        int x0 = cx + 92;
        int x1 = cx + 118;
        int top = ground + 18;
        b.room(x0, ground, cz + z0, x1, top, cz + z1);
        b.walls(x0, ground + 1, cz + z0, x1, ground + 1, cz + z1, p.foundation());
        // Punch a few authored gaps through the front wall.
        int[] holes = {z0 + 6, z0 + 14, z0 + 26, z0 + 34};
        for (int hz : holes) {
            for (int y = ground + 2; y <= ground + 8; y += 3) {
                b.put(x1, y, cz + hz, AIR);
                b.put(x1, y + 1, cz + hz, AIR);
            }
        }
        // Board the surviving windows with dark planks.
        for (int wz = z0 + 4; wz <= z1 - 4; wz += 5) {
            b.put(x1, ground + 11, cz + wz, p.frame());
            b.put(x1, ground + 14, cz + wz, p.frame());
        }
        // Rubble at the foot and a fallen beam across the pavement.
        for (int z = z0; z <= z1; z += 2) {
            b.put(cx + 119, ground + 1, cz + z, p.rubble());
        }
        box(b, cx + 117, ground + 1, cz + z0 + 10, cx + 121, ground + 1, cz + z0 + 16, p.accent());
        box(b, cx + 119, ground + 2, cz + z0 + 12, cx + 121, ground + 2, cz + z0 + 14, p.rubble());
        b.put(cx + 100, ground + 3, cz + z0 + 8, p.light());
    }

    private static void questBoard(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x = cx + 132;
        for (int z = cz - 3; z <= cz + 3; z += 6) {
            b.put(x, ground + 1, z, p.accent());
            b.put(x, ground + 2, z, p.accent());
        }
        box(b, x, ground + 3, cz - 3, x, ground + 3, cz + 3, p.roofSlab());
        box(b, x, ground + 2, cz - 3, x, ground + 2, cz + 3, p.foundation());
        b.put(x - 1, ground + 1, cz, p.accent());
        b.put(x - 1, ground + 2, cz, p.accent());
        sign(b, x - 1, ground + 3, cz, Direction.WEST, "CITY NOTICES", "THE BUND CARGO",
                "VANISHING", "ASK THE CLERK");
        b.put(x, ground + 1, cz, p.light());
        b.marker("quest_board_bund", x, ground + 2, cz);
    }

    private static void statueLion(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground + 1, z, p.foundation());
        b.put(x, ground + 2, z, p.accent());
        b.put(x, ground + 3, z, p.accent());
        b.put(x, ground + 4, z, p.accent());
        b.put(x, ground + 4, z - 1, p.accent());
        b.put(x, ground + 5, z, p.accent());
    }

    private static void sampan(StructureBuilder b, int x, int z, int ground, Palette p) {
        // A low river hull, a mast and a stern lantern; sits at the water line.
        int y = ground - 4;
        box(b, x - 3, y, z - 1, x + 3, y, z + 1, p.accent());
        box(b, x - 2, y + 1, z - 1, x + 2, y + 1, z - 1, p.accent());
        box(b, x - 2, y + 1, z + 1, x + 2, y + 1, z + 1, p.accent());
        b.put(x, y + 2, z, p.accent());
        b.put(x, y + 3, z, p.accent());
        b.put(x, y + 4, z, p.light());
        b.put(x - 3, y + 1, z, p.rail());
    }

    // ------------------------------------------------------------------ Nanjing Road

    private static void nanjing(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // North side.
        shophouse(b, cx - 300, cz - 42, cx - 262, cz - 12, ground, p, 3, Direction.SOUTH,
                DyeColor.CYAN);
        shophouse(b, cx - 252, cz - 42, cx - 214, cz - 12, ground, p, 2, Direction.SOUTH,
                DyeColor.MAGENTA);
        shophouse(b, cx - 204, cz - 42, cx - 166, cz - 12, ground, p, 3, Direction.SOUTH,
                DyeColor.YELLOW);
        shophouse(b, cx - 156, cz - 42, cx - 118, cz - 12, ground, p, 2, Direction.SOUTH,
                DyeColor.LIGHT_BLUE);
        wingOn(b, cx, cz, ground, p);
        shophouse(b, cx + 48, cz - 42, cx + 84, cz - 12, ground, p, 3, Direction.SOUTH,
                DyeColor.RED);
        // South side.
        shophouse(b, cx - 300, cz + 12, cx - 262, cz + 44, ground, p, 2, Direction.NORTH,
                DyeColor.MAGENTA);
        shophouse(b, cx - 252, cz + 12, cx - 214, cz + 44, ground, p, 3, Direction.NORTH,
                DyeColor.CYAN);
        shophouse(b, cx - 204, cz + 12, cx - 166, cz + 44, ground, p, 2, Direction.NORTH,
                DyeColor.RED);
        shophouse(b, cx - 156, cz + 12, cx - 118, cz + 44, ground, p, 3, Direction.NORTH,
                DyeColor.YELLOW);
        odeon(b, cx, cz, ground, p);

        // A boarded shell on the south side, between the square and the Bund.
        boardedShop(b, cx + 48, cz + 12, cx + 84, cz + 44, ground, p);

        // Lamps, awnings, stalls and crates down the spine.
        for (int x = cx - 312; x <= cx + 144; x += 24) {
            lamp(b, x, cz - 9, ground, p);
            lamp(b, x + 12, cz + 9, ground, p);
        }
        for (int x : new int[]{-290, -230, -180, -140, 30, 60}) {
            stall(b, cx + x, cz - 10, ground, p, DyeColor.RED);
            stall(b, cx + x + 7, cz + 8, ground, p, DyeColor.CYAN);
        }
        for (int x : new int[]{-270, -200, -130, 10, 90}) {
            crates(b, cx + x, cz - 10, ground, p, 4);
            crates(b, cx + x + 10, cz + 10, ground, p, 3);
        }
        // A handcart and a rickshaw stand.
        cart(b, cx - 80, cz - 9, ground, p);
        cart(b, cx + 110, cz + 9, ground, p);
    }

    /**
     * A two- or three-storey shop: an open front onto the street, a striped awning, a neon band
     * and small windows above. {@code face} is the street-facing wall.
     */
    private static void shophouse(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                                  Palette p, int storeys, Direction face, DyeColor c) {
        int top = ground + storeys * 4;
        b.room(x0, ground, z0, x1, top, z1);
        b.walls(x0, ground + 1, z0, x1, ground + 1, z1, p.foundation());
        int faceZ = face == Direction.SOUTH ? z1 : z0;
        int shopX = (x0 + x1) / 2 - 2;
        for (int s = 1; s < storeys; s++) {
            int y = ground + s * 4 + 2;
            for (int wx = x0 + 2; wx <= x1 - 2; wx += 3) {
                b.window(wx, y, faceZ, 2, 1, true);
            }
            box(b, x0 + 1, ground + s * 4, z0 + 1, x1 - 1, ground + s * 4, z1 - 1, p.roofSlab());
        }
        // The shopfront: a 4-wide, 3-high opening with a lit interior.
        if (face == Direction.SOUTH) {
            box(b, shopX, ground + 1, z1, shopX + 3, ground + 3, z1, AIR);
            awning(b, shopX - 1, shopX + 4, z1 + 1, ground, p, c, 1);
            b.put(shopX + 1, ground + 1, z1 - 1, p.foundation());
            b.put(shopX + 2, ground + 1, z1 - 1, p.foundation());
            b.put(shopX + 1, ground + 2, z1 - 1, p.light());
        } else {
            box(b, shopX, ground + 1, z0, shopX + 3, ground + 3, z0, AIR);
            awning(b, shopX - 1, shopX + 4, z0 - 1, ground, p, c, -1);
            b.put(shopX + 1, ground + 1, z0 + 1, p.foundation());
            b.put(shopX + 2, ground + 1, z0 + 1, p.foundation());
            b.put(shopX + 1, ground + 2, z0 + 1, p.light());
        }
        neonBand(b, x0 + 1, faceZ, x1 - 1, ground + 4, p, c);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, top, 4, 0);
        // A back door and a chimney.
        int backZ = face == Direction.SOUTH ? z0 : z1;
        door(b, p, x0 + 2, ground + 1, backZ, face == Direction.SOUTH ? Direction.NORTH : Direction.SOUTH);
        b.put(x1 - 2, top + 5, z0 + 2, p.accent());
        b.put(x1 - 2, top + 6, z0 + 2, p.accent());
    }

    private static void wingOn(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 8;
        int x1 = cx + 40;
        int z0 = cz - 44;
        int z1 = cz - 12;
        int top = ground + 20;
        for (int s = 0; s < 5; s++) {
            b.room(x0, ground + s * 4, z0, x1, ground + s * 4 + 4, z1);
            for (int wx = x0 + 3; wx <= x1 - 3; wx += 4) {
                b.window(wx, ground + s * 4 + 2, z1, 2, 1, true);
            }
            for (int wz = z0 + 4; wz <= z1 - 4; wz += 4) {
                b.window(x1, ground + s * 4 + 2, wz, 2, 1, true);
            }
            b.walls(x0 - 1, ground + s * 4 + 4, z0 - 1, x1 + 1, ground + s * 4 + 4, z1 + 1, p.accent());
        }
        b.walls(x0, ground + 1, z0, x1, ground + 1, z1, p.foundation());
        // The corner entrance and display windows.
        box(b, x0 + 6, ground + 1, z1, x0 + 10, ground + 3, z1, AIR);
        b.put(x0 + 6, ground + 4, z1, p.roofSlab());
        b.put(x0 + 10, ground + 4, z1, p.roofSlab());
        sign(b, x0 + 4, ground + 1, z1 + 1, Direction.SOUTH, "WING ON", "DEPT STORE");
        sign(b, x1 + 1, ground + 1, z1 - 4, Direction.EAST, "WING ON", "SILKS - TEAS");
        neonBand(b, x0 + 1, z1, x1 - 1, ground + 4, p, DyeColor.CYAN);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, top, 3, 1);
        // Rooftop sign frame.
        box(b, x0 + 4, top + 4, z1 - 2, x0 + 20, top + 4, z1 - 2, p.accent());
        for (int x = x0 + 4; x <= x0 + 20; x += 4) {
            b.put(x, top + 5, z1 - 2, p.light());
        }
        crates(b, x0 + 1, z1 + 1, ground, p, 4);
        b.marker("wing_on_counter", x0 + 8, ground + 1, z0 + 8);
    }

    private static void odeon(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx + 8;
        int x1 = cx + 40;
        int z0 = cz + 12;
        int z1 = cz + 44;
        int top = ground + 16;
        b.room(x0, ground, z0, x1, top, z1);
        b.walls(x0, ground + 1, z0, x1, ground + 1, z1, p.foundation());
        for (int s = 1; s < 4; s++) {
            for (int wx = x0 + 3; wx <= x1 - 3; wx += 4) {
                b.window(wx, ground + s * 4 + 2, z0, 2, 1, true);
            }
            box(b, x0 + 1, ground + s * 4, z0 + 1, x1 - 1, ground + s * 4, z1 - 1, p.roofSlab());
        }
        // The marquee over the north entrance, facing Nanjing Road.
        box(b, x0 + 4, ground + 5, z0 - 3, x1 - 4, ground + 5, z0 - 1, p.roofSlab());
        for (int x = x0 + 5; x <= x1 - 5; x += 3) {
            b.put(x, ground + 4, z0 - 2, p.light());
        }
        box(b, x0 + 8, ground + 1, z0, x0 + 18, ground + 4, z0, AIR);
        sign(b, x0 + 4, ground + 1, z0 - 1, Direction.NORTH, "ODEON", "CINEMA", "TONIGHT 8");
        neonBand(b, x0 + 1, z0, x1 - 1, ground + 4, p, DyeColor.MAGENTA);
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, top, 4, 0);
        b.put(x0 + 6, ground + 1, z0 + 4, p.foundation());
        b.put(x0 + 6, ground + 2, z0 + 4, p.light());
        cart(b, x0 + 24, z0 - 5, ground, p);
    }

    /** A shop that has been shut up: boards over the windows and rubble at the door. */
    private static void boardedShop(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                                    Palette p) {
        int top = ground + 8;
        b.room(x0, ground, z0, x1, top, z1);
        b.walls(x0, ground + 1, z0, x1, ground + 1, z1, p.foundation());
        for (int wx = x0 + 2; wx <= x1 - 2; wx += 3) {
            b.put(wx, ground + 2, z0, p.frame());
            b.put(wx, ground + 3, z0, p.frame());
            b.put(wx, ground + 6, z0, p.frame());
        }
        box(b, x0 + 4, ground + 1, z0, x0 + 6, ground + 3, z0, AIR);
        b.put(x0 + 5, ground + 1, z0, p.rubble());
        b.put(x0 + 5, ground + 2, z0, p.frame());
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, top, 3, 0);
        for (int x = x0; x <= x1; x += 2) {
            b.put(x, ground + 1, z0 - 1, p.rubble());
        }
    }

    // ------------------------------------------------------------------ People's Square

    private static void peoplesSquare(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // The monument on the north half.
        b.monument(cx - 48, cz - 40, ground + 1);
        box(b, cx - 52, ground + 1, cz - 44, cx - 44, ground + 1, cz - 36, p.foundation());
        b.put(cx - 48, ground + 1, cz - 35, p.light());
        b.marker("peoples_square_monument", cx - 48, ground + 2, cz - 40);
        // The notice kiosk on the south half.
        int nx = cx - 48;
        int nz = cz + 40;
        for (int dx = -4; dx <= 4; dx += 8) {
            b.put(nx + dx, ground + 1, nz, p.accent());
            b.put(nx + dx, ground + 2, nz, p.accent());
            b.put(nx + dx, ground + 3, nz, p.accent());
        }
        box(b, nx - 4, ground + 3, nz, nx + 4, ground + 3, nz, p.foundation());
        box(b, nx - 4, ground + 4, nz, nx + 4, ground + 4, nz, p.roofSlab());
        b.put(nx, ground + 1, nz + 1, p.accent());
        b.put(nx, ground + 2, nz + 1, p.accent());
        sign(b, nx, ground + 3, nz + 1, Direction.SOUTH, "CITY NOTICES", "THE BUND CARGO",
                "VANISHING", "ASK AROUND");
        // A bandstand for the street musicians, in the south-west corner.
        bandstand(b, cx - 74, cz + 44, ground, p);
        // Benches, lamps and trees around the edges, clear of the Nanjing Road crossing.
        for (int z = cz - 60; z <= cz + 60; z += 20) {
            if (Math.abs(z - cz) < 9) {
                continue;
            }
            bench(b, cx - 84, z, ground, p);
            bench(b, cx - 14, z, ground, p);
        }
        for (int z = cz - 64; z <= cz + 64; z += 32) {
            if (Math.abs(z - cz) < 9) {
                continue;
            }
            lamp(b, cx - 92, z, ground, p);
            lamp(b, cx - 6, z, ground, p);
        }
        for (int x = cx - 80; x <= cx - 24; x += 16) {
            tree(b, x, cz - 62, ground, p, 5);
            tree(b, x + 8, cz + 62, ground, p, 5);
        }
        // A pair of stone lions at the monument steps.
        statueLion(b, cx - 56, cz - 40, ground, p);
        statueLion(b, cx - 40, cz - 40, ground, p);
    }

    private static void bandstand(StructureBuilder b, int x, int z, int ground, Palette p) {
        box(b, x - 3, ground + 1, z - 3, x + 3, ground + 1, z + 3, p.foundation());
        for (int dx = -3; dx <= 3; dx += 6) {
            for (int dz = -3; dz <= 3; dz += 6) {
                box(b, x + dx, ground + 2, z + dz, x + dx, ground + 5, z + dz, p.accent());
            }
        }
        b.pitchedRoof(x - 4, z - 4, x + 4, z + 4, ground + 5, 3, 0);
        b.put(x, ground + 2, z, p.light());
        b.put(x - 2, ground + 2, z - 2, p.roofSlab());
        b.put(x + 2, ground + 2, z - 2, p.roofSlab());
    }

    // ------------------------------------------------------------------ Old City

    private static void oldCity(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        templeHall(b, cx, cz, ground, p);
        yuGarden(b, cx, cz, ground, p);
        teaHouse(b, cx, cz, ground, p);
        herbalist(b, cx, cz, ground, p);
        // Lane houses in the authored blocks between the lanes.
        laneHouse(b, cx - 252, cz + 100, cx - 232, cz + 124, ground, p, 2, Direction.NORTH, false);
        laneHouse(b, cx - 118, cz + 100, cx - 96, cz + 124, ground, p, 2, Direction.WEST, false);
        laneHouse(b, cx - 118, cz + 140, cx - 96, cz + 164, ground, p, 1, Direction.WEST, false);
        laneHouse(b, cx - 118, cz + 180, cx - 96, cz + 204, ground, p, 2, Direction.WEST, true);
        laneHouse(b, cx - 118, cz + 244, cx - 96, cz + 266, ground, p, 1, Direction.SOUTH, false);
        laneHouse(b, cx - 250, cz + 244, cx - 226, cz + 268, ground, p, 2, Direction.NORTH, false);
        laneHouse(b, cx - 214, cz + 244, cx - 190, cz + 268, ground, p, 1, Direction.NORTH, true);
        laneHouse(b, cx - 150, cz + 244, cx - 130, cz + 268, ground, p, 2, Direction.NORTH, false);
        laneHouse(b, cx - 252, cz + 204, cx - 230, cz + 228, ground, p, 1, Direction.WEST, false);

        // Lane furniture: lamps and willows on the kerbs, clear of the lanes themselves.
        for (int z : new int[]{100, 160, 220, 260}) {
            lamp(b, cx - 122, cz + z, ground, p);
        }
        for (int z : new int[]{100, 260}) {
            lamp(b, cx - 254, cz + z, ground, p);
        }
        lamp(b, cx - 170, cz + 100, ground, p);
        lamp(b, cx - 170, cz + 260, ground, p);
        for (int z : new int[]{110, 160, 210, 260}) {
            willow(b, cx - 266, cz + z, ground, p);
        }
        willow(b, cx - 134, cz + 120, ground, p);
        willow(b, cx - 134, cz + 180, ground, p);
        stall(b, cx - 224, cz + 230, ground, p, DyeColor.RED);
        stall(b, cx - 140, cz + 100, ground, p, DyeColor.YELLOW);
        crates(b, cx - 240, cz + 230, ground, p, 4);
        crates(b, cx - 110, cz + 130, ground, p, 3);
    }

    private static void templeHall(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 252;
        int x1 = cx - 224;
        int z0 = cz + 150;
        int z1 = cz + 196;
        box(b, x0 - 2, ground, z0 - 2, x1 + 2, ground, z1 + 2, p.foundation());
        b.room(x0, ground, z0, x1, ground + 9, z1, new Doorway(Side.E, 20));
        b.walls(x0, ground + 1, z0, x1, ground + 1, z1, p.foundation());
        // Vermilion columns down the long walls.
        for (int z = z0 + 4; z <= z1 - 4; z += 8) {
            for (int y = ground + 1; y <= ground + 9; y++) {
                b.put(x0, y, z, p.accent());
                b.put(x1, y, z, p.accent());
            }
        }
        // A wide front porch and a pair of doors.
        box(b, x1 + 1, ground + 4, z0 + 14, x1 + 2, ground + 4, z1 - 14, p.roofSlab());
        box(b, x1 + 2, ground + 1, z0 + 16, x1 + 2, ground + 3, z0 + 16, p.accent());
        box(b, x1 + 2, ground + 1, z1 - 16, x1 + 2, ground + 3, z1 - 16, p.accent());
        for (int z = z0 + 6; z <= z1 - 6; z += 8) {
            b.window(x0, ground + 4, z, 3, 1, true);
            b.window(x1, ground + 4, z, 3, 1, true);
        }
        b.pitchedRoof(x0 - 3, z0 - 3, x1 + 3, z1 + 3, ground + 9, 7, 1);
        b.put(x1, ground + 10, (z0 + z1) / 2, p.light());
        sign(b, x1 + 1, ground + 1, cz + 176, Direction.EAST, "OLD TEMPLE", "INCENSE HALL");
        // The incense burner on the forecourt, against the temple wall and clear of the lane.
        int ix = cx - 223;
        int iz = cz + 188;
        box(b, ix - 1, ground + 1, iz - 1, ix + 1, ground + 1, iz + 1, p.foundation());
        b.put(ix, ground + 2, iz, CAULDRON);
        b.put(ix, ground + 3, iz, p.rail());
        b.put(ix, ground + 4, iz, p.light());
        b.put(ix - 1, ground + 2, iz, p.light());
        b.put(ix + 1, ground + 2, iz, p.light());
        b.marker("old_temple_incense", ix, ground + 2, iz);
    }

    private static void yuGarden(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 212;
        int x1 = cx - 170;
        int z0 = cz + 96;
        int z1 = cz + 150;
        // Garden ground and the pond.
        box(b, x0, ground, z0, x1, ground, z1, p.ground());
        box(b, cx - 206, ground - 2, cz + 108, cx - 182, ground - 2, cz + 136, p.foundation());
        box(b, cx - 206, ground - 1, cz + 108, cx - 182, ground - 1, cz + 136, WATER);
        box(b, cx - 206, ground, cz + 108, cx - 182, ground + 1, cz + 136, AIR);
        // Rockery on the north-west bank.
        for (int i = 0; i < 5; i++) {
            box(b, cx - 210 + i, ground + 1, cz + 100 + i, cx - 208 + i, ground + 2 + i,
                    cz + 102 + i, p.weathered());
        }
        // The zigzag bridge: five short spans over the pond.
        zig(b, cx - 204, cz + 110, cx - 196, cz + 111, ground, p);
        zig(b, cx - 197, cz + 110, cx - 196, cz + 120, ground, p);
        zig(b, cx - 196, cz + 119, cx - 186, cz + 120, ground, p);
        zig(b, cx - 187, cz + 119, cx - 186, cz + 130, ground, p);
        zig(b, cx - 186, cz + 129, cx - 178, cz + 130, ground, p);
        // The moon gate in the south wall.
        box(b, x0, ground + 1, z1, x1, ground + 5, z1, p.wall());
        box(b, cx - 193, ground + 1, z1, cx - 191, ground + 4, z1, AIR);
        box(b, x0, ground + 6, z1, x1, ground + 6, z1, p.roofSlab());
        sign(b, cx - 198, ground + 1, z1 + 1, Direction.SOUTH, "YU GARDEN", "OPEN DUSK");
        // The tea pavilion on the east bank.
        pavilion(b, cx - 176, cz + 144, ground, p);
        // Willows and a stone lantern.
        willow(b, cx - 208, cz + 104, ground, p);
        willow(b, cx - 178, cz + 106, ground, p);
        willow(b, cx - 208, cz + 140, ground, p);
        willow(b, cx - 186, cz + 138, ground, p);
        b.put(cx - 186, ground + 1, cz + 142, p.foundation());
        b.put(cx - 186, ground + 2, cz + 142, p.accent());
        b.put(cx - 186, ground + 3, cz + 142, p.light());
        b.marker("yu_garden_bridge", cx - 190, ground + 2, cz + 120);
    }

    /** One 2-wide span of the zigzag bridge, railed along its long sides only. */
    private static void zig(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                            Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        box(b, ax, ground + 1, az, bx, ground + 1, bz, p.roofSlab());
        boolean alongX = bx - ax >= bz - az;
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                boolean longSide = alongX ? z == az || z == bz : x == ax || x == bx;
                boolean end = alongX ? x == ax || x == bx : z == az || z == bz;
                if (longSide && !end) {
                    b.put(x, ground + 2, z, p.rail());
                }
            }
        }
    }

    private static void teaHouse(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 200;
        int x1 = cx - 170;
        int z0 = cz + 174;
        int z1 = cz + 206;
        // Two storeys, the upper one set back behind a balcony.
        b.room(x0, ground, z0, x1, ground + 7, z1, new Doorway(Side.E, 16));
        b.room(x0 + 2, ground + 7, z0 + 2, x1 - 2, ground + 12, z1 - 2,
                new Doorway(Side.E, 16));
        b.walls(x0, ground + 1, z0, x1, ground + 1, z1, p.foundation());
        for (int z = z0 + 3; z <= z1 - 3; z += 4) {
            b.window(x0, ground + 4, z, 2, 1, true);
            b.window(x1, ground + 4, z, 2, 1, true);
            b.window(x0 + 2, ground + 9, z, 2, 1, true);
            b.window(x1 - 2, ground + 9, z, 2, 1, true);
        }
        // Balcony ring and rails: a one-block walkway around the set-back upper storey.
        b.walls(x0 + 1, ground + 8, z0 + 1, x1 - 1, ground + 8, z1 - 1, p.roofSlab());
        for (int z = z0 + 1; z <= z1 - 1; z += 2) {
            b.put(x0 + 1, ground + 9, z, p.rail());
            b.put(x1 - 1, ground + 9, z, p.rail());
        }
        b.pitchedRoof(x0 - 3, z0 - 3, x1 + 3, z1 + 3, ground + 12, 5, 0);
        // A flared skirt eave over the ground floor, around the upper storey (not across it).
        b.walls(x0 - 2, ground + 7, z0 - 2, x1 + 2, ground + 7, z1 + 2, p.roofSlab());
        b.walls(x0 - 1, ground + 8, z0 - 1, x1 + 1, ground + 8, z1 + 1, p.roofSlab());
        sign(b, x1 + 1, ground + 1, cz + 196, Direction.EAST, "TEA HOUSE", "YU GARDEN");
        // Hanging lanterns under the east eave.
        for (int z = z0 + 4; z <= z1 - 4; z += 6) {
            b.put(x1 + 1, ground + 6, z, p.light());
        }
        // The cellar stair, inside the south-east room.
        cellarStairs(b, cx, cz, ground, p);
        b.put(cx - 190, ground + 1, cz + 195, p.accent());
        sign(b, cx - 190, ground + 2, cz + 195, Direction.SOUTH, "CELLAR", "KEEP OUT");
        cellar(b, cx, cz, ground, p);
        b.marker("tea_keeper", cx - 180, ground + 1, cz + 190);
    }

    private static void cellarStairs(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        for (int i = 0; i <= 6; i++) {
            int z = cz + 196 + i;
            box(b, cx - 188, ground - 1 - i, z, cx - 186, ground - 1 - i, z, p.foundation());
            box(b, cx - 188, ground - i, z, cx - 186, ground + 1, z, AIR);
        }
        // The landing at the cellar door.
        box(b, cx - 188, ground - 7, cz + 203, cx - 186, ground - 7, cz + 203, p.foundation());
        box(b, cx - 188, ground - 6, cz + 203, cx - 186, ground + 1, cz + 203, AIR);
        // A rail around the stair head.
        for (int x = cx - 189; x <= cx - 185; x++) {
            b.put(x, ground + 1, cz + 195, p.rail());
        }
        b.put(cx - 189, ground + 2, cz + 195, p.rail());
        b.put(cx - 185, ground + 2, cz + 195, p.rail());
        b.put(cx - 187, ground + 1, cz + 194, p.light());
    }

    private static void cellar(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 206;
        int x1 = cx - 176;
        int z0 = cz + 204;
        int z1 = cz + 228;
        b.room(x0, ground - 7, z0, x1, ground - 3, z1);
        // The stair opening in the north wall.
        box(b, cx - 188, ground - 6, z0, cx - 186, ground - 4, z0, AIR);
        b.walls(x0, ground - 7, z0, x1, ground - 7, z1, p.foundation());
        // Shelves, cobwebs and the cult's altar at the south end.
        for (int x = x0 + 2; x <= x1 - 2; x += 4) {
            b.put(x, ground - 5, z0 + 1, BOOKS);
            b.put(x, ground - 4, z0 + 1, BOOKS);
            b.put(x, ground - 5, z1 - 1, p.accent());
        }
        for (int x = x0 + 1; x <= x1 - 1; x += 5) {
            b.put(x, ground - 4, z0 + 1, Palette.cobweb());
            b.put(x, ground - 4, z1 - 1, Palette.cobweb());
        }
        int ax = cx - 191;
        int az = cz + 224;
        box(b, ax - 2, ground - 7, az - 1, ax + 2, ground - 7, az + 1, p.accent());
        b.put(ax, ground - 6, az, p.accent());
        b.put(ax, ground - 5, az, p.accent());
        b.put(ax, ground - 4, az, p.light());
        b.put(ax - 1, ground - 6, az, p.light());
        b.put(ax + 1, ground - 6, az, p.light());
        // The relic chest and the lore chest.
        b.chest(cx - 200, ground - 6, cz + 212, SiteLoot.RELIC);
        b.chest(cx - 182, ground - 6, cz + 224, SiteLoot.CULT);
        b.put(cx - 200, ground - 5, cz + 212, p.light());
        b.marker("old_city_cellar", cx - 190, ground - 6, cz + 210);
    }

    private static void herbalist(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 162;
        int x1 = cx - 138;
        int z0 = cz + 138;
        int z1 = cz + 162;
        b.room(x0, ground, z0, x1, ground + 6, z1, new Doorway(Side.W, 12));
        b.walls(x0, ground + 1, z0, x1, ground + 1, z1, p.foundation());
        // Open shopfront onto the crooked lane, with shelves behind.
        box(b, x0, ground + 1, z0 + 4, x0, ground + 3, z0 + 10, AIR);
        for (int z = z0 + 2; z <= z1 - 2; z += 2) {
            b.put(x0 + 1, ground + 2, z, BOOKS);
            b.put(x0 + 1, ground + 3, z, BOOKS);
        }
        b.put(x0 + 4, ground + 1, z1 - 3, CAULDRON);
        b.put(x0 + 4, ground + 2, z1 - 3, p.light());
        BlockState herb = p.overgrowth();
        for (int z = z0 + 2; z <= z1 - 2; z += 4) {
            b.put(x1 - 1, ground + 2, z, herb == null ? p.accent() : herb);
        }
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 6, 3, 1);
        sign(b, x0 - 1, ground + 1, cz + 146, Direction.WEST, "HERBALIST", "REMEDIES");
        sign(b, x1 + 1, ground + 1, cz + 150, Direction.EAST, "HERBS", "TONICS");
        // A herb planter outside the east door.
        box(b, cx - 136, ground + 1, cz + 146, cx - 134, ground + 1, cz + 154, p.foundation());
        b.put(cx - 135, ground + 2, cz + 150, herb == null ? p.accent() : herb);
        b.marker("herbalist_counter", x0 + 3, ground + 1, z1 - 3);
    }

    /** A small courtyard house: one or two storeys, a tiled roof, a laundry line. */
    private static void laneHouse(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                                  Palette p, int storeys, Direction face, boolean decayed) {
        int top = ground + storeys * 4;
        b.room(x0, ground, z0, x1, top, z1);
        b.walls(x0, ground + 1, z0, x1, ground + 1, z1, p.foundation());
        int fx = (x0 + x1) / 2;
        int fz = (z0 + z1) / 2;
        boolean sideWall = face == Direction.WEST || face == Direction.EAST;
        if (face == Direction.WEST) {
            fx = x0;
        } else if (face == Direction.EAST) {
            fx = x1;
        } else if (face == Direction.SOUTH) {
            fz = z1;
        } else {
            fz = z0;
        }
        door(b, p, fx, ground + 1, fz, face);
        for (int s = 1; s < storeys; s++) {
            int y = ground + s * 4 + 2;
            if (sideWall) {
                b.window(fx, y, fz - 2, 2, 1, true);
                b.window(fx, y, fz + 2, 2, 1, true);
            } else {
                b.window(fx - 2, y, fz, 2, 1, true);
                b.window(fx + 2, y, fz, 2, 1, true);
            }
        }
        for (int s = 1; s < storeys; s++) {
            box(b, x0 + 1, ground + s * 4, z0 + 1, x1 - 1, ground + s * 4, z1 - 1, p.roofSlab());
        }
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, top, 4, 0);
        // A low courtyard wall on the street side, with a gate gap.
        int wallZ = face == Direction.SOUTH ? z1 + 1 : z0 - 1;
        if (!sideWall) {
            box(b, x0 - 1, ground + 1, wallZ, x1 + 1, ground + 2, wallZ, p.weathered());
            b.put(fx, ground + 1, wallZ, AIR);
            b.put(fx, ground + 2, wallZ, AIR);
        }
        if (decayed) {
            // Authored collapse: a broken corner and boards over the windows.
            box(b, x1 - 1, ground + 1, z0, x1, ground + 4, z0 + 1, AIR);
            b.put(x0 + 1, ground + 1, z0 - 1, p.rubble());
            b.put(x0 + 2, ground + 1, z0 - 1, p.rubble());
            if (sideWall) {
                b.put(fx, ground + 3, fz - 2, p.frame());
                b.put(fx, ground + 3, fz + 2, p.frame());
            } else {
                b.put(fx - 2, ground + 3, fz, p.frame());
                b.put(fx + 2, ground + 3, fz, p.frame());
            }
        }
        // A laundry line across the courtyard.
        if (!decayed) {
            b.put(x0 + 1, ground + 5, z0 - 1, p.rail());
            b.put(x1 - 1, ground + 5, z0 - 1, p.rail());
            for (int x = x0 + 2; x <= x1 - 2; x += 2) {
                b.put(x, ground + 5, z0 - 1, Materials.wool(DyeColor.WHITE));
            }
        }
    }

    // ------------------------------------------------------------------ French Concession

    private static void frenchConcession(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        villa(b, cx - 240, cz - 240, cx - 208, cz - 208, ground, p, 2, Direction.SOUTH);
        safehouse(b, cx, cz, ground, p);
        villa(b, cx - 240, cz - 136, cx - 212, cz - 104, ground, p, 2, Direction.NORTH);
        villa(b, cx - 188, cz - 240, cx - 156, cz - 208, ground, p, 2, Direction.SOUTH);
        cafe(b, cx, cz, ground, p);
        clinic(b, cx, cz, ground, p);
        villa(b, cx - 136, cz - 240, cx - 104, cz - 212, ground, p, 2, Direction.SOUTH);
        villa(b, cx - 136, cz - 188, cx - 104, cz - 156, ground, p, 2, Direction.WEST);
        fountain(b, cx - 118, cz - 118, ground, p);

        // Plane trees down both kerbs of every avenue.
        int[] vx = {-248, -196, -144, -92};
        for (int x : vx) {
            for (int z = cz - 268; z <= cz - 100; z += 24) {
                planeTree(b, cx + x - 6, z, ground, p);
                planeTree(b, cx + x + 6, z + 12, ground, p);
            }
        }
        int[] hz = {-248, -196, -144, -92};
        for (int z : hz) {
            for (int x = cx - 268; x <= cx - 100; x += 24) {
                planeTree(b, x, cz + z - 6, ground, p);
                planeTree(b, x + 12, cz + z + 6, ground, p);
            }
        }
        // Benches and lamps around the fountain.
        for (int d = -10; d <= 10; d += 10) {
            bench(b, cx - 118 + d, cz - 128, ground, p);
            bench(b, cx - 118 + d, cz - 108, ground, p);
        }
        lamp(b, cx - 128, cz - 118, ground, p);
        lamp(b, cx - 108, cz - 118, ground, p);
    }

    /** A stucco villa with a slate roof, iron balconies and a walled garden. */
    private static void villa(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                              Palette p, int storeys, Direction face) {
        int top = ground + storeys * 4;
        b.room(x0, ground, z0, x1, top, z1);
        b.walls(x0, ground + 1, z0, x1, ground + 1, z1, p.foundation());
        int faceZ = face == Direction.SOUTH ? z1 : face == Direction.NORTH ? z0 : z1;
        int doorX = (x0 + x1) / 2;
        if (face == Direction.WEST || face == Direction.EAST) {
            int faceX = face == Direction.WEST ? x0 : x1;
            door(b, p, faceX, ground + 1, (z0 + z1) / 2, face);
            for (int s = 1; s < storeys; s++) {
                b.window(faceX, ground + s * 4 + 2, (z0 + z1) / 2 - 3, 2, 1, true);
                b.window(faceX, ground + s * 4 + 2, (z0 + z1) / 2 + 3, 2, 1, true);
            }
        } else {
            door(b, p, doorX, ground + 1, faceZ, face);
            for (int s = 1; s < storeys; s++) {
                b.window(doorX - 3, ground + s * 4 + 2, faceZ, 2, 1, true);
                b.window(doorX + 3, ground + s * 4 + 2, faceZ, 2, 1, true);
            }
        }
        for (int s = 1; s < storeys; s++) {
            box(b, x0 + 1, ground + s * 4, z0 + 1, x1 - 1, ground + s * 4, z1 - 1, p.roofSlab());
            // Iron balcony on the garden side.
            if (face == Direction.SOUTH || face == Direction.NORTH) {
                int bz = face == Direction.SOUTH ? z1 + 1 : z0 - 1;
                box(b, doorX - 4, ground + s * 4 + 1, bz, doorX + 4, ground + s * 4 + 1, bz,
                        p.roofSlab());
                for (int x = doorX - 4; x <= doorX + 4; x += 2) {
                    b.put(x, ground + s * 4 + 2, bz, p.rail());
                }
            }
        }
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, top, 4, 0);
        // The garden wall and gate.
        int wallZ = face == Direction.SOUTH ? z1 + 4 : z0 - 3;
        if (face == Direction.SOUTH || face == Direction.NORTH) {
            box(b, x0 - 2, ground + 1, wallZ, x1 + 2, ground + 2, wallZ, p.weathered());
            b.put(doorX, ground + 1, wallZ, AIR);
            b.put(doorX, ground + 2, wallZ, AIR);
            for (int x = x0 - 2; x <= x1 + 2; x += 2) {
                b.put(x, ground + 3, wallZ, p.rail());
            }
        }
        tree(b, x0 - 3, z0 - 3, ground, p, 5);
        tree(b, x1 + 3, z1 + 3, ground, p, 5);
    }

    private static void safehouse(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 236;
        int x1 = cx - 208;
        int z0 = cz - 184;
        int z1 = cz - 156;
        villa(b, x0, z0, x1, z1, ground, p, 2, Direction.SOUTH);
        // Unmarked iron door, a house number the only clue.
        door(b, p, cx - 222, ground + 1, z1, Direction.SOUTH);
        sign(b, cx - 226, ground + 1, z1 + 1, Direction.SOUTH, "NO. 7", "DELIVERIES");
        // A meeting room: a table, a ledger chest and a shuttered lamp.
        box(b, cx - 228, ground + 1, cz - 172, cx - 216, ground + 1, cz - 170, p.roofSlab());
        b.put(cx - 228, ground + 1, cz - 171, p.accent());
        b.put(cx - 216, ground + 1, cz - 171, p.accent());
        b.chest(cx - 230, ground + 1, cz - 180, SiteLoot.ORDER);
        b.put(cx - 220, ground + 3, cz - 170, p.light());
        b.put(cx - 214, ground + 2, cz - 178, p.frame());
        b.marker("order_ledger", cx - 222, ground + 1, cz - 171);
    }

    private static void cafe(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 188;
        int x1 = cx - 152;
        int z0 = cz - 188;
        int z1 = cz - 149;
        b.room(x0, ground, z0, x1, ground + 8, z1);
        b.room(x0 + 2, ground + 8, z0 + 2, x1 - 2, ground + 12, z1 - 2);
        b.walls(x0, ground + 1, z0, x1, ground + 1, z1, p.foundation());
        door(b, p, cx - 160, ground + 1, z1, Direction.SOUTH);
        for (int x = x0 + 4; x <= x1 - 4; x += 4) {
            b.window(x, ground + 2, z1, 2, 1, true);
            b.window(x, ground + 6, z1, 2, 1, true);
            b.window(x, ground + 10, z1 - 2, 2, 1, true);
        }
        // Striped awning over the street front, and a side terrace in the garden strip.
        awning(b, x0 + 3, x1 - 3, z1 + 1, ground, p, DyeColor.RED, 1);
        for (int z = z0 + 10; z <= z1 - 10; z += 6) {
            b.put(x1 + 2, ground + 1, z, p.accent());
            b.put(x1 + 2, ground + 2, z, p.roofSlab());
            b.put(x1 + 2, ground + 1, z + 2, p.rail());
            b.put(x1 + 2, ground + 2, z + 2, p.roofSlab());
        }
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 12, 4, 1);
        sign(b, cx - 170, ground + 1, z1 + 1, Direction.SOUTH, "CAFE", "PASTRIES", "LIVE MUSIC");
        sign(b, x1 + 1, ground + 1, cz - 168, Direction.EAST, "MISSING VIOLIN", "ASK INSIDE");
        // A small stage and a music stand for the lodger.
        box(b, x1 - 10, ground + 1, z0 + 4, x1 - 4, ground + 1, z0 + 8, p.accent());
        b.put(x1 - 7, ground + 2, z0 + 6, p.rail());
        b.put(x1 - 7, ground + 3, z0 + 6, p.roofSlab());
        b.put(x1 - 7, ground + 2, z0 + 7, p.light());
        b.chest(x1 - 12, ground + 1, z0 + 10, SiteLoot.COMMON);
        b.marker("cafe_owner", cx - 160, ground + 1, cz - 152);
    }

    private static void clinic(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 188;
        int x1 = cx - 156;
        int z0 = cz - 136;
        int z1 = cz - 104;
        b.room(x0, ground, z0, x1, ground + 8, z1);
        b.walls(x0, ground + 1, z0, x1, ground + 1, z1, p.foundation());
        door(b, p, cx - 172, ground + 1, z1, Direction.SOUTH);
        for (int x = x0 + 4; x <= x1 - 4; x += 4) {
            b.window(x, ground + 2, z1, 2, 1, true);
            b.window(x, ground + 6, z1, 2, 1, true);
        }
        box(b, x0 + 1, ground + 4, z0 + 1, x1 - 1, ground + 4, z1 - 1, p.roofSlab());
        // A red cross on the white front.
        int mx = cx - 172;
        box(b, mx - 1, ground + 4, z1, mx + 1, ground + 4, z1, Materials.wool(DyeColor.RED));
        b.put(mx, ground + 3, z1, Materials.wool(DyeColor.RED));
        b.put(mx, ground + 5, z1, Materials.wool(DyeColor.RED));
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 8, 3, 0);
        sign(b, cx - 180, ground + 1, z1 + 1, Direction.SOUTH, "CLINIC", "OPEN ALL HOURS");
        // Two beds and a cabinet.
        for (int x = x0 + 3; x <= x0 + 9; x += 6) {
            b.put(x, ground + 1, z0 + 3, Materials.wool(DyeColor.WHITE));
            b.put(x + 1, ground + 1, z0 + 3, Materials.wool(DyeColor.WHITE));
            b.put(x, ground + 2, z0 + 2, p.rail());
        }
        b.put(x1 - 2, ground + 1, z0 + 2, BOOKS);
    }

    private static void fountain(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > 25) {
                    continue;
                }
                if (d2 >= 16) {
                    b.put(cx + dx, ground + 1, cz + dz, p.foundation());
                } else {
                    b.put(cx + dx, ground, cz + dz, WATER);
                }
            }
        }
        b.put(cx, ground + 1, cz, p.accent());
        b.put(cx, ground + 2, cz, p.accent());
        b.put(cx, ground + 3, cz, p.accent());
        b.put(cx, ground + 4, cz, p.light());
    }

    // ------------------------------------------------------------------ Pudong

    private static void pudong(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        warehouse(b, cx + 232, cz - 250, cx + 300, cz - 210, ground, p, 0);
        warehouse(b, cx + 232, cz - 190, cx + 296, cz - 150, ground, p, 1);
        warehouse(b, cx + 232, cz - 130, cx + 300, cz - 90, ground, p, 0);
        warehouse(b, cx + 232, cz - 70, cx + 290, cz - 40, ground, p, 2);
        warehouse(b, cx + 232, cz + 40, cx + 296, cz + 80, ground, p, 0);
        dock7(b, cx, cz, ground, p);
        warehouse(b, cx + 232, cz + 160, cx + 300, cz + 200, ground, p, 3);
        warehouse(b, cx + 232, cz + 220, cx + 296, cz + 260, ground, p, 1);

        // Wharf cranes, coal heaps, barrels and the strike banner.
        crane(b, cx + 200, cz - 220, ground, p);
        crane(b, cx + 200, cz - 60, ground, p);
        crane(b, cx + 200, cz + 180, ground, p);
        coal(b, cx + 306, cz - 100, ground, p);
        coal(b, cx + 302, cz + 60, ground, p);
        coal(b, cx + 308, cz + 210, ground, p);
        for (int z = cz - 240; z <= cz + 240; z += 30) {
            b.put(cx + 198, ground + 1, z, BARREL);
            b.put(cx + 206, ground + 1, z + 12, BARREL);
            crates(b, cx + 226, z, ground, p, 3);
        }
        strikeBanner(b, cx + 224, cz + 60, ground, p);
        // A moored lighter at the Pudong quay.
        sampan(b, cx + 184, cz + 20, ground, p);
    }

    /**
     * A long godown: stone base, corrugated walls, a big west door, high windows and a slate roof.
     * {@code kind}: 0 stores, 1 offices, 2 shelled, 3 flooded.
     */
    private static void warehouse(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                                  Palette p, int kind) {
        int top = ground + 10;
        b.room(x0, ground, z0, x1, top, z1);
        b.walls(x0, ground + 1, z0, x1, ground + 1, z1, p.foundation());
        // Corrugated bands: alternating wall and weathered courses.
        for (int y = ground + 2; y <= top - 1; y += 3) {
            b.walls(x0, y, z0, x1, y, z1, p.weathered());
        }
        // The loading door on the quay side.
        int dz = (z0 + z1) / 2;
        box(b, x0, ground + 1, dz - 3, x0, ground + 5, dz + 3, AIR);
        box(b, x0 - 1, ground + 6, dz - 4, x0, ground + 6, dz + 4, p.roofSlab());
        box(b, x0 - 2, ground + 1, dz - 3, x0 - 1, ground + 1, dz + 3, p.foundation());
        b.put(x0, ground + 1, dz - 4, p.accent());
        b.put(x0, ground + 1, dz + 4, p.accent());
        // High windows along the long walls.
        for (int x = x0 + 5; x <= x1 - 5; x += 8) {
            b.window(x, ground + 7, z0, 2, 1, true);
            b.window(x, ground + 7, z1, 2, 1, true);
        }
        if (kind == 1) {
            for (int x = x0 + 4; x <= x1 - 4; x += 5) {
                b.window(x, ground + 3, z0, 2, 1, true);
            }
        }
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, top, 6, 1);
        if (kind == 2) {
            // A shelled roof: authored holes and a fallen truss.
            box(b, x0 + 8, top + 1, z0 + 6, x0 + 12, top + 2, z0 + 10, AIR);
            box(b, x1 - 12, top + 1, z1 - 8, x1 - 8, top + 2, z1 - 4, AIR);
            box(b, x0 + 14, top + 1, z0 + 2, x0 + 20, top + 1, z0 + 3, p.accent());
            b.put(x0 + 10, ground + 1, z0 + 8, p.rubble());
        }
        if (kind == 3) {
            // Flooded: one block of river water across the floor and floating crates.
            box(b, x0 + 2, ground, z0 + 2, x1 - 2, ground, z1 - 2, WATER);
            b.put(x0 + 6, ground + 1, z0 + 6, p.foundation());
            b.put(x0 + 7, ground + 1, z0 + 6, p.foundation());
            b.put(x1 - 8, ground + 1, z1 - 8, p.foundation());
            sign(b, x0 - 1, ground + 1, dz + 6, Direction.WEST, "BEWARE THE TIDE", "FLOODED");
        }
        crates(b, x0 + 2, z0 + 2, ground, p, 4);
        crates(b, x1 - 6, z1 - 5, ground, p, 3);
        b.put(x1 - 2, ground + 1, z0 + 2, BARREL);
        b.put(x1 - 2, ground + 1, z1 - 2, BARREL);
    }

    private static void dock7(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        warehouse(b, cx + 232, cz + 100, cx + 274, cz + 140, ground, p, 1);
        // The gantry crane beside the godown.
        int gx = cx + 280;
        int gz = cz + 120;
        for (int dz = -8; dz <= 8; dz += 16) {
            for (int y = ground + 1; y <= ground + 16; y++) {
                b.put(gx, y, gz + dz, p.accent());
                b.put(gx + 6, y, gz + dz, p.accent());
            }
            box(b, gx, ground + 1, gz + dz, gx + 6, ground + 1, gz + dz, p.foundation());
        }
        box(b, gx - 1, ground + 16, gz - 9, gx + 7, ground + 16, gz + 9, p.accent());
        b.put(gx + 3, ground + 15, gz, p.rail());
        b.put(gx + 3, ground + 14, gz, p.rail());
        b.put(gx + 3, ground + 13, gz, p.rail());
        b.put(gx + 3, ground + 12, gz, p.light());
        box(b, gx, ground + 17, gz - 9, gx + 6, ground + 17, gz - 8, p.foundation());
        // Dock 7's sign and the warning at the quay.
        sign(b, cx + 231, ground + 1, cz + 126, Direction.WEST, "DOCK 7", "GODOWN");
        sign(b, cx + 226, ground + 1, cz + 104, Direction.WEST, "DOCK 7 - NO", "ENTRY",
                "BEWARE THE TIDE");
        // The shrine in the back of the godown: boarded, lit, and wrong.
        int sx = cx + 268;
        int sz = cz + 108;
        box(b, sx - 3, ground + 1, sz - 3, sx + 3, ground + 5, sz + 3, p.accent());
        box(b, sx - 2, ground + 1, sz - 2, sx + 2, ground + 4, sz + 2, AIR);
        for (int y = ground + 1; y <= ground + 5; y++) {
            b.put(sx - 3, y, sz - 3, p.accent());
            b.put(sx + 3, y, sz + 3, p.accent());
        }
        b.put(sx, ground + 2, sz, p.light());
        b.put(sx, ground + 3, sz, p.light());
        b.put(sx - 2, ground + 2, sz - 2, p.light());
        b.put(sx + 2, ground + 2, sz - 2, p.light());
        b.chest(sx, ground + 1, sz + 1, SiteLoot.CULT);
        box(b, sx - 3, ground + 6, sz - 3, sx + 3, ground + 6, sz + 3, p.roofSlab());
        b.put(sx - 3, ground + 1, sz - 3, p.frame());
        b.put(sx + 3, ground + 1, sz + 3, p.frame());
        b.marker("dock7_shrine", sx, ground + 1, sz);
        b.marker("dockworker", cx + 250, ground + 1, cz + 120);
    }

    private static void crane(StructureBuilder b, int x, int z, int ground, Palette p) {
        for (int dz = -6; dz <= 6; dz += 12) {
            for (int y = ground + 1; y <= ground + 14; y++) {
                b.put(x, y, z + dz, p.accent());
                b.put(x + 8, y, z + dz, p.accent());
            }
            box(b, x, ground + 1, z + dz, x + 8, ground + 1, z + dz, p.foundation());
        }
        box(b, x - 1, ground + 14, z - 7, x + 9, ground + 14, z + 7, p.accent());
        for (int y = ground + 10; y <= ground + 13; y++) {
            b.put(x + 4, y, z, p.rail());
        }
        b.put(x + 4, ground + 9, z, p.light());
    }

    private static void coal(StructureBuilder b, int x, int z, int ground, Palette p) {
        for (int i = 0; i < 5; i++) {
            box(b, x - 4 + i, ground + 1, z - 4 + i, x + 4 - i, ground + 1 + i, z + 4 - i,
                    p.rubble());
        }
        b.put(x, ground + 2, z, p.rubble());
        b.put(x + 2, ground + 2, z - 1, p.rubble());
        b.put(x - 2, ground + 2, z + 2, p.rubble());
    }

    private static void strikeBanner(StructureBuilder b, int x, int z, int ground, Palette p) {
        for (int y = ground + 1; y <= ground + 7; y++) {
            b.put(x, y, z, p.accent());
        }
        box(b, x, ground + 7, z - 3, x, ground + 7, z + 3, p.rail());
        for (int dz = -3; dz <= 3; dz++) {
            b.put(x, ground + 6, z + dz, Materials.wool(DyeColor.RED));
            b.put(x, ground + 5, z + dz, Materials.wool(DyeColor.RED));
        }
        sign(b, x + 1, ground + 1, z, Direction.WEST, "STRIKE", "FAIR PAY");
    }

    // ------------------------------------------------------------------ the Pearl Tower

    private static void pearlTower(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int tx = cx + 260;
        int tz = cz;
        // The plaza's low wall, open to the quay road on the west.
        for (int x = cx + 230; x <= cx + 290; x++) {
            if (Math.abs(x - (cx + 260)) > 4) {
                b.put(x, ground + 1, cz - 30, p.wall());
                b.put(x, ground + 2, cz - 30, p.wall());
                b.put(x, ground + 1, cz + 30, p.wall());
                b.put(x, ground + 2, cz + 30, p.wall());
            }
        }
        for (int z = cz - 30; z <= cz + 30; z++) {
            if (Math.abs(z) > 4) {
                b.put(cx + 230, ground + 1, z, p.wall());
                b.put(cx + 230, ground + 2, z, p.wall());
                b.put(cx + 290, ground + 1, z, p.wall());
                b.put(cx + 290, ground + 2, z, p.wall());
            }
        }
        // Two neon pylons flanking the west gate.
        pylon(b, cx + 236, cz - 26, ground, p);
        pylon(b, cx + 236, cz + 26, ground, p);
        // The colonnaded base.
        box(b, tx - 10, ground + 1, tz - 10, tx + 10, ground + 2, tz + 10, p.foundation());
        for (int dx = -10; dx <= 10; dx += 5) {
            for (int dz = -10; dz <= 10; dz += 10) {
                box(b, tx + dx, ground + 3, tz + dz, tx + dx, ground + 10, tz + dz, p.accent());
            }
        }
        for (int dz = -10; dz <= 10; dz += 5) {
            for (int dx = -10; dx <= 10; dx += 10) {
                box(b, tx + dx, ground + 3, tz + dz, tx + dx, ground + 10, tz + dz, p.accent());
            }
        }
        box(b, tx - 11, ground + 11, tz - 11, tx + 11, ground + 11, tz + 11, p.roofSlab());
        box(b, tx - 11, ground + 12, tz - 11, tx + 11, ground + 12, tz + 11, p.accent());
        // The tapering shaft.
        int shaftBase = ground + 13;
        int shaftTop = ground + 82;
        for (int y = shaftBase; y <= shaftTop; y++) {
            int r = Math.max(2, 5 - (y - shaftBase) / 16);
            box(b, tx - r, y, tz - r, tx + r, y, tz + r, p.wall());
            for (int i = -r; i <= r; i++) {
                b.put(tx + i, y, tz - r, p.accent());
                b.put(tx + i, y, tz + r, p.accent());
                b.put(tx - r, y, tz + i, p.accent());
                b.put(tx + r, y, tz + i, p.accent());
            }
            if ((y - shaftBase) % 12 == 6) {
                for (int i = -r + 1; i <= r - 1; i++) {
                    b.put(tx + i, y, tz - r, p.window());
                    b.put(tx + i, y, tz + r, p.window());
                }
            }
        }
        // Three observation spheres, each with a glowing equatorial band.
        sphere(b, tx, tz, ground + 26, 6, p);
        sphere(b, tx, tz, ground + 46, 9, p);
        sphere(b, tx, tz, ground + 66, 5, p);
        b.spire(tx, tz, shaftTop + 1, 18);
        b.marker("pearl_tower", tx, ground + 1, tz);
    }

    private static void sphere(StructureBuilder b, int tx, int tz, int cy, int r, Palette p) {
        for (int dy = -r; dy <= r; dy++) {
            int rr = (int) Math.round(Math.sqrt((double) (r * r - dy * dy)));
            int y = cy + dy;
            box(b, tx - rr, y, tz - rr, tx + rr, y, tz + rr, p.roof());
            for (int i = -rr; i <= rr; i++) {
                b.put(tx + i, y, tz - rr, p.accent());
                b.put(tx + i, y, tz + rr, p.accent());
                b.put(tx - rr, y, tz + i, p.accent());
                b.put(tx + rr, y, tz + i, p.accent());
            }
            if (dy == 0) {
                for (int i = -rr; i <= rr; i++) {
                    b.put(tx + i, y, tz - rr, p.light());
                    b.put(tx + i, y, tz + rr, p.light());
                    b.put(tx - rr, y, tz + i, p.light());
                    b.put(tx + rr, y, tz + i, p.light());
                }
            }
        }
    }

    private static void pylon(StructureBuilder b, int x, int z, int ground, Palette p) {
        for (int y = ground + 1; y <= ground + 10; y++) {
            b.put(x, y, z, p.rail());
        }
        for (int y = ground + 4; y <= ground + 8; y++) {
            b.put(x, y, z - 1, Materials.stainedGlass(DyeColor.CYAN));
            b.put(x, y, z + 1, Materials.stainedGlass(DyeColor.CYAN));
        }
        b.put(x, ground + 11, z, p.light());
    }

    // ------------------------------------------------------------------ shared props

    private static void lamp(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground + 1, z, p.rail());
        b.put(x, ground + 2, z, p.rail());
        b.put(x, ground + 3, z, p.light());
    }

    private static void bench(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground + 1, z, p.roofSlab());
        b.put(x + 1, ground + 1, z, p.roofSlab());
        b.put(x, ground + 2, z, p.rail());
        b.put(x + 1, ground + 2, z, p.rail());
    }

    private static void tree(StructureBuilder b, int x, int z, int ground, Palette p, int height) {
        for (int y = 1; y <= height; y++) {
            b.put(x, ground + y, z, p.accent());
        }
        int top = ground + height;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 3) {
                    b.put(x + dx, top, z + dz, LEAVES);
                    if (Math.abs(dx) + Math.abs(dz) <= 2) {
                        b.put(x + dx, top + 1, z + dz, LEAVES);
                    }
                }
            }
        }
    }

    /** A narrow plane tree for an avenue kerb: a small canopy that clears the set-back facades. */
    private static void planeTree(StructureBuilder b, int x, int z, int ground, Palette p) {
        for (int y = 1; y <= 6; y++) {
            b.put(x, ground + y, z, p.accent());
        }
        int top = ground + 6;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                b.put(x + dx, top, z + dz, LEAVES);
            }
        }
        b.put(x, top + 1, z, LEAVES);
        b.put(x, top - 1, z - 2, LEAVES);
        b.put(x, top - 1, z + 2, LEAVES);
    }

    private static void willow(StructureBuilder b, int x, int z, int ground, Palette p) {
        for (int y = 1; y <= 4; y++) {
            b.put(x, ground + y, z, p.accent());
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 9) {
                    b.put(x + dx, ground + 5, z + dz, LEAVES);
                    if (d2 <= 4) {
                        b.put(x + dx, ground + 6, z + dz, LEAVES);
                    }
                }
            }
        }
        BlockState vine = p.overgrowth();
        if (vine != null) {
            int[][] strands = {{-3, 0}, {3, 0}, {0, -3}, {0, 3}, {-2, -2}, {2, 2}};
            for (int[] d : strands) {
                b.put(x + d[0], ground + 4, z + d[1], vine);
                b.put(x + d[0], ground + 3, z + d[1], vine);
            }
        }
    }

    private static void pavilion(StructureBuilder b, int x, int z, int ground, Palette p) {
        for (int dx = -2; dx <= 2; dx += 4) {
            for (int dz = -2; dz <= 2; dz += 4) {
                box(b, x + dx, ground + 1, z + dz, x + dx, ground + 4, z + dz, p.accent());
            }
        }
        b.pitchedRoof(x - 3, z - 3, x + 3, z + 3, ground + 4, 3, 0);
        b.put(x, ground + 1, z, p.light());
        b.put(x + 2, ground + 1, z, p.roofSlab());
        b.put(x - 2, ground + 1, z, p.roofSlab());
    }

    private static void stall(StructureBuilder b, int x, int z, int ground, Palette p, DyeColor c) {
        b.put(x, ground + 1, z, p.foundation());
        b.put(x + 1, ground + 1, z, p.foundation());
        b.put(x, ground + 1, z + 1, p.foundation());
        b.put(x + 2, ground + 1, z + 1, p.rail());
        b.put(x + 2, ground + 2, z + 1, p.rail());
        b.put(x + 1, ground + 1, z + 2, p.rail());
        b.put(x + 1, ground + 2, z + 2, p.rail());
        b.put(x, ground + 2, z, p.rail());
        for (int dx = 0; dx <= 2; dx++) {
            for (int dz = 0; dz <= 2; dz++) {
                b.put(x + dx, ground + 3, z + dz,
                        Materials.wool(((dx + dz) & 1) == 0 ? c : DyeColor.WHITE));
            }
        }
        b.put(x + 1, ground + 2, z + 1, p.light());
    }

    private static void crates(StructureBuilder b, int x, int z, int ground, Palette p, int count) {
        for (int i = 0; i < count; i++) {
            int px = x + (i % 3);
            int pz = z + (i / 3);
            b.put(px, ground + 1, pz, p.foundation());
            if ((i & 3) == 0) {
                b.put(px, ground + 2, pz, p.foundation());
            }
        }
    }

    private static void cart(StructureBuilder b, int x, int z, int ground, Palette p) {
        box(b, x, ground + 1, z, x + 2, ground + 1, z + 1, p.accent());
        b.put(x, ground + 2, z, p.rail());
        b.put(x + 2, ground + 2, z, p.rail());
        b.put(x + 1, ground + 2, z + 1, p.foundation());
        b.put(x + 1, ground + 3, z + 1, p.light());
    }

    private static void awning(StructureBuilder b, int x0, int x1, int z, int ground, Palette p,
                               DyeColor c, int step) {
        for (int x = x0; x <= x1; x++) {
            b.put(x, ground + 4, z, Materials.wool(((x - x0) & 1) == 0 ? c : DyeColor.WHITE));
            b.put(x, ground + 3, z + step,
                    Materials.wool(((x - x0) & 1) == 0 ? c : DyeColor.WHITE));
        }
    }

    private static void neonBand(StructureBuilder b, int x0, int z, int x1, int y, Palette p,
                                 DyeColor c) {
        for (int x = x0; x <= x1; x++) {
            b.put(x, y, z, (x & 1) == 0 ? Materials.stainedGlass(c) : p.light());
        }
    }

    private static void balustrade(StructureBuilder b, int x, int z0, int z1, int ground, Palette p) {
        int mid = (z0 + z1) / 2;
        for (int z = z0; z <= z1; z++) {
            // Leave the Nanjing Road mouth and both bridge approaches open.
            if (Math.abs(z - mid) < 8 || Math.abs(z - (mid - 40)) < 6
                    || Math.abs(z - (mid + 140)) < 6) {
                continue;
            }
            b.put(x, ground + 1, z, p.rail());
            if ((z & 3) == 0) {
                b.put(x, ground + 2, z, p.rail());
            }
        }
    }

    // ------------------------------------------------------------------ framework glue

    /** Writes one readable sign through the city-plan helper. */
    private static void sign(StructureBuilder b, int x, int y, int z, Direction facing,
                             String... lines) {
        CityPlanSupport.sign(b, x, y, z, facing, lines);
    }

    /** A 1x2 door in a wall, facing the street. */
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

    /** A box fill; the builder keeps the slice inside the generating chunk. */
    private static void box(StructureBuilder b, int x0, int y0, int z0, int x1, int y1, int z1,
                            BlockState state) {
        b.fill(x0, y0, z0, x1, y1, z1, state);
    }

    /** Paving with an authored three-tone wear pattern (no RNG). */
    private static void pave(StructureBuilder b, int x0, int z0, int x1, int z1, int y, Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                int v = Math.floorMod(x * 3 + z * 5, 19);
                BlockState state = v == 0 ? p.rubble() : v < 3 ? p.foundation() : p.ground();
                b.put(x, y, z, state);
            }
        }
    }
}
