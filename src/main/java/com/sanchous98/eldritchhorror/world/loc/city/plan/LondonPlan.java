package com.sanchous98.eldritchhorror.world.loc.city.plan;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.SiteLoot;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
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
 * Authored interwar London: St Paul's dome over the City, Big Ben and the Parliament front, the
 * docks with their quay, warehouses, cranes and railway, the East End terraces and market, the
 * British Museum and Bloomsbury squares, the West End's theatres and hotels, and the Underground
 * with its hidden junction.
 *
 * <p>Everything is written through {@link StructureBuilder}; there is no RNG, no chunk-local read
 * and no shared prefab. Coordinates are relative to the city centre {@code (cx, ground, cz)} and
 * stay inside {@code [-340, 340]}.
 */
public final class LondonPlan implements CityPlan {

    /** Authored half-extent (blocks). */
    private static final int HALF = 340;

    @Override
    public String cityId() {
        return "london";
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
        streets(b, cx, cz, p, surface);
        arrivalGate(b, cx, cz, p, surface);
        stPauls(b, cx, cz, ground, p);
        bigBen(b, cx, cz, ground, p);
        cityBlocks(b, cx, cz, p, surface);
        britishMuseum(b, cx, cz, ground, p);
        museumQuarter(b, cx, cz, p, surface);
        westEnd(b, cx, cz, ground, p);
        docks(b, cx, cz, p, surface);
        eastEnd(b, cx, cz, p, surface);
        underground(b, cx, cz, ground, p);
        dressing(b, cx, cz, p, surface);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                new CityPoi("arrival_gate", CityPoi.Kind.LANDMARK, 0, 1, 320, "south_gate"),
                new CityPoi("arrival_sign", CityPoi.Kind.SIGN, 0, 8, 321, "LONDON"),
                new CityPoi("city_quest_board", CityPoi.Kind.QUEST_BOARD, -80, 1, -120,
                        "london_missing_expedition,london_cursed_mummy"),
                new CityPoi("savoy_hotel", CityPoi.Kind.INN, -80, 1, 40, "hotel_keeper"),
                new CityPoi("hotel_keeper", CityPoi.Kind.NPC, -80, 2, 36, "hotel_keeper"),
                new CityPoi("west_end_shop", CityPoi.Kind.SHOP, -40, 1, 80, "shopkeeper"),
                new CityPoi("shopkeeper", CityPoi.Kind.NPC, -40, 2, 76, "shopkeeper"),
                new CityPoi("apothecary", CityPoi.Kind.HEALER, 180, 1, 220, "apothecary"),
                new CityPoi("apothecary_keeper", CityPoi.Kind.NPC, 180, 2, 224, "apothecary"),
                new CityPoi("museum_curator", CityPoi.Kind.NPC, -240, 2, -60,
                        "curator|london_missing_expedition"),
                new CityPoi("british_museum", CityPoi.Kind.LANDMARK, -240, 1, 0, "galleries"),
                new CityPoi("mummy_case", CityPoi.Kind.SECRET, -280, 2, 10,
                        "london_cursed_mummy"),
                new CityPoi("big_ben", CityPoi.Kind.LANDMARK, 80, 1, -120, "clock_tower"),
                new CityPoi("st_pauls", CityPoi.Kind.LANDMARK, 0, 1, -200, "dome"),
                new CityPoi("dock_pub", CityPoi.Kind.NPC, 240, 2, 60,
                        "dockworker|london_cursed_mummy"),
                new CityPoi("dockworker", CityPoi.Kind.NPC, 246, 2, 52, "dockworker"),
                new CityPoi("docks_quay", CityPoi.Kind.LANDMARK, 250, 1, -60, "cranes"),
                new CityPoi("rail_station", CityPoi.Kind.STATION, 160, 1, -247,
                        "goods_depot;east_west"),
                new CityPoi("dock_port", CityPoi.Kind.PORT, 270, -1, -60, "basin"),
                new CityPoi("east_end_market", CityPoi.Kind.LANDMARK, 235, 1, 230, "market"),
                new CityPoi("east_end_chapel", CityPoi.Kind.LANDMARK, 270, 1, 280, "chapel"),
                new CityPoi("west_end_theatre", CityPoi.Kind.LANDMARK, -100, 1, 120, "theatre"),
                new CityPoi("theatre_manager", CityPoi.Kind.NPC, -100, 2, 120, "theatre_manager"),
                new CityPoi("underground_station", CityPoi.Kind.LANDMARK, 0, 1, 90,
                        "ticket_hall"),
                new CityPoi("underground_junction", CityPoi.Kind.SECRET, 0, -8, 120,
                        "london_missing_expedition"),
                new CityPoi("bobby", CityPoi.Kind.NPC, 0, 2, 140, "bobby"),
                new CityPoi("dock_warning", CityPoi.Kind.SIGN, 250, 2, -112, "NO ENTRY"),
                new CityPoi("tunnel_warning", CityPoi.Kind.SIGN, 20, -5, 114, "MIND THE GAP"));
    }

    // ------------------------------------------------------------------ streets

    private static void streets(StructureBuilder b, int cx, int cz, Palette p,
                                IntBinaryOperator surface) {
        // The great north-south avenue from the arrival gate to St Paul's churchyard.
        pave(b, cx - 7, cz - 140, cx + 7, cz + 320, surface, p.ground());
        // The City cross street, running past Big Ben.
        pave(b, cx - 320, cz - 108, cx + 320, cz - 94, surface, p.ground());
        // Two side streets flanking the cathedral, and a north street behind it.
        pave(b, cx - 90, cz - 266, cx - 83, cz - 94, surface, p.ground());
        pave(b, cx + 83, cz - 266, cx + 90, cz - 140, surface, p.ground());
        pave(b, cx - 90, cz - 266, cx + 90, cz - 258, surface, p.ground());
        // West End streets: two east-west, two short north-south lanes.
        pave(b, cx - 140, cz + 44, cx + 140, cz + 51, surface, p.ground());
        pave(b, cx - 140, cz + 104, cx + 140, cz + 111, surface, p.ground());
        pave(b, cx - 108, cz - 60, cx - 101, cz + 104, surface, p.ground());
        pave(b, cx + 90, cz - 60, cx + 97, cz + 104, surface, p.ground());
        // Museum street and its approach from the West End.
        pave(b, cx - 320, cz + 92, cx - 140, cz + 101, surface, p.ground());
        pave(b, cx - 148, cz + 40, cx - 140, cz + 101, surface, p.ground());
        // The long road along the south edge of the docks, into the East End.
        pave(b, cx - 320, cz + 156, cx + 320, cz + 168, surface, p.ground());
        // The dock quay itself, and the goods road behind the railway.
        pave(b, cx + 244, cz - 260, cx + 255, cz + 180, surface, p.foundation());
        pave(b, cx + 140, cz - 266, cx + 320, cz - 254, surface, p.rubble());
        // East End high street, two long lanes and a cross lane.
        pave(b, cx + 164, cz + 180, cx + 171, cz + 320, surface, p.ground());
        pave(b, cx + 140, cz + 214, cx + 320, cz + 218, surface, p.ground());
        pave(b, cx + 140, cz + 262, cx + 320, cz + 266, surface, p.ground());
        pave(b, cx + 210, cz + 180, cx + 214, cz + 320, surface, p.ground());
        // The Underground's ticket-hall forecourt.
        pave(b, cx - 24, cz + 82, cx + 24, cz + 102, surface, p.foundation());
    }

    private static void pave(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                             BlockState state) {
        b.fill(x0, y, z0, x1, y, z1, state);
    }

    private static void pave(StructureBuilder b, int x0, int z0, int x1, int z1,
                             IntBinaryOperator surface, BlockState state) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                b.put(x, surface.applyAsInt(x, z), z, state);
            }
        }
    }

    // ------------------------------------------------------------------ arrival

    private static void arrivalGate(StructureBuilder b, int cx, int cz, Palette p,
                                    IntBinaryOperator surface) {
        int x = cx;
        int z = cz + 320;
        int ground = surface.applyAsInt(x, z);
        BlockState air = air();

        b.fill(x - 22, ground, z - 14, x + 22, ground, z + 14, p.foundation());

        // Two stone piers with a wide opening for the avenue.
        for (int side = -1; side <= 1; side += 2) {
            int px = x + side * 15;
            b.fill(px - 4, ground + 1, z - 6, px + 4, ground + 12, z, p.wall());
            b.fill(px - 5, ground + 12, z - 7, px + 5, ground + 13, z + 1, p.roof());
            b.crenellations(px - 5, z - 7, px + 5, z + 1, ground + 14);
            b.put(px, ground + 10, z + 1, p.light());
            for (int y = ground + 2; y <= ground + 11; y += 3) {
                b.put(px - 4, y, z + 1, p.accent());
                b.put(px + 4, y, z + 1, p.accent());
            }
        }
        // Iron arch over the road, high enough for a bus.
        b.fill(x - 13, ground + 11, z - 4, x + 13, ground + 11, z - 3, p.rail());
        b.fill(x - 11, ground + 13, z - 4, x + 11, ground + 13, z - 3, p.roofSlab());
        for (int px = -12; px <= 12; px += 4) {
            b.put(x + px, ground + 12, z - 3, p.rail());
        }

        signAt(b, x, ground + 8, z + 1, Direction.SOUTH, "LONDON", "SOUTH GATE");
        signAt(b, x - 15, ground + 6, z + 1, Direction.SOUTH, "CITY OF LONDON");

        // Signposts to every district, set just south of the gate.
        String[] posts = {"ST PAUL'S ^", "DOCKS ->", "EAST END ->", "MUSEUM <-",
                "WEST END ^", "UNDERGROUND ^"};
        for (int i = 0; i < posts.length; i++) {
            int px = x - 15 + i * 6;
            b.put(px, ground, z + 6, p.foundation());
            b.put(px, ground + 1, z + 6, p.accent());
            b.put(px, ground + 2, z + 6, p.accent());
            signAt(b, px, ground + 3, z + 6, Direction.SOUTH, posts[i]);
        }

        // The gate mouth is kept clear; two gas lamps flank it.
        b.put(x - 13, ground + 1, z + 5, p.accent());
        b.put(x - 13, ground + 2, z + 5, p.light());
        b.put(x + 13, ground + 1, z + 5, p.accent());
        b.put(x + 13, ground + 2, z + 5, p.light());

        // A little guard box and a bench on the apron.
        b.fill(x + 18, ground + 1, z + 2, x + 22, ground + 4, z + 6, p.wall());
        b.fill(x + 19, ground + 1, z + 6, x + 21, ground + 2, z + 6, air);
        b.fill(x + 18, ground + 5, z + 1, x + 23, ground + 5, z + 7, p.roofSlab());
        signAt(b, x + 20, ground + 4, z + 7, Direction.SOUTH, "POLICE");
    }

    // ------------------------------------------------------------------ St Paul's

    private static void stPauls(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x = cx;
        int z = cz - 200;

        // The churchyard podium and steps.
        b.fill(x - 40, ground, z - 54, x + 40, ground, z + 52, p.foundation());
        b.fill(x - 20, ground + 1, z + 46, x + 20, ground + 1, z + 52, p.foundation());
        b.fill(x - 14, ground + 2, z + 48, x + 14, ground + 2, z + 52, p.foundation());

        // A solid base under the raised nave and towers, so no floor floats above the podium.
        b.fill(x - 34, ground + 1, z - 46, x + 34, ground + 3, z + 42, p.foundation());

        // Nave and choir: a tall stone shell under a shallow roof.
        b.room(x - 20, ground + 3, z - 44, x + 20, ground + 22, z + 36);
        b.walls(x - 20, ground + 4, z - 44, x + 20, ground + 21, z + 36, p.ground());
        b.walls(x - 20, ground + 4, z - 44, x + 20, ground + 5, z + 36, p.accent());
        b.walls(x - 20, ground + 20, z - 44, x + 20, ground + 21, z + 36, p.accent());
        b.pitchedRoof(x - 22, z - 46, x + 22, z + 38, ground + 22, 6, 1);

        // Nave windows and buttresses down both flanks.
        for (int wz = z - 36; wz <= z + 28; wz += 12) {
            b.window(x - 20, ground + 8, wz, 6, 1, true);
            b.window(x + 20, ground + 8, wz, 6, 1, true);
            b.buttress(x - 21, wz, ground + 4, 6, Side.W);
            b.buttress(x + 21, wz, ground + 4, 6, Side.E);
        }

        // West front with its twin towers and the great door.
        b.fill(x - 20, ground + 3, z + 36, x + 20, ground + 22, z + 36, p.wall());
        placeDoor(b, p, x - 20, z - 44, x + 20, z + 36, ground + 4, Side.S, 20);
        b.window(x - 2, ground + 10, z + 36, 5, 5, true);
        // A stepped porch up to the west door.
        b.fill(x - 12, ground + 1, z + 37, x + 12, ground + 3, z + 40, p.foundation());
        b.fill(x - 12, ground + 1, z + 41, x + 12, ground + 2, z + 43, p.foundation());
        b.fill(x - 12, ground + 1, z + 44, x + 12, ground + 1, z + 46, p.foundation());
        for (int side = -1; side <= 1; side += 2) {
            int tx = x + side * 27;
            b.room(tx - 5, ground + 3, z + 28, tx + 5, ground + 30, z + 40);
            b.walls(tx - 5, ground + 4, z + 28, tx + 5, ground + 29, z + 40, p.ground());
            for (int y = ground + 4; y <= ground + 29; y++) {
                b.put(tx - 5, y, z + 28, (y & 1) == 0 ? p.accent() : p.ground());
                b.put(tx + 5, y, z + 28, (y & 1) == 0 ? p.accent() : p.ground());
                b.put(tx - 5, y, z + 40, (y & 1) == 0 ? p.accent() : p.ground());
                b.put(tx + 5, y, z + 40, (y & 1) == 0 ? p.accent() : p.ground());
            }
            for (int y = ground + 8; y <= ground + 24; y += 8) {
                b.window(tx, y, z + 40, 4, 1, true);
            }
            b.crenellations(tx - 6, z + 27, tx + 6, z + 41, ground + 31);
            b.spire(tx, z + 34, ground + 32, 10);
        }

        // The colonnaded west portico.
        for (int px = x - 28; px <= x + 28; px += 8) {
            b.fill(px, ground + 3, z + 48, px, ground + 13, z + 48, p.accent());
            b.put(px, ground + 14, z + 48, p.foundation());
        }
        b.fill(x - 32, ground + 15, z + 46, x + 32, ground + 16, z + 50, p.roof());
        b.fill(x - 34, ground + 16, z + 45, x + 34, ground + 16, z + 51, p.roofSlab());

        // The dome on its colonnaded drum.
        dome(b, x, z, ground + 23, p);

        // Churchyard railings and a few plane trees.
        railing(b, x - 40, z - 54, x + 40, z + 52, ground + 1, p);
        b.fill(x - 8, ground + 1, z + 52, x + 8, ground + 1, z + 54, air());
        tree(b, x - 34, z + 46, ground, p);
        tree(b, x + 34, z + 46, ground, p);
        tree(b, x - 34, z - 46, ground, p);
        tree(b, x + 34, z - 46, ground, p);

        signAt(b, x - 12, ground + 4, z + 42, Direction.SOUTH, "ST PAUL'S");
        signAt(b, x + 12, ground + 4, z + 42, Direction.SOUTH, "CITY CHURCH");
    }

    private static void dome(StructureBuilder b, int x, int z, int baseY, Palette p) {
        // The drum: a thick ring with pilasters, open enough to read as a colonnade.
        for (int y = baseY; y < baseY + 8; y++) {
            for (int dx = -16; dx <= 16; dx++) {
                for (int dz = -16; dz <= 16; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > 256 || d2 < 169) {
                        continue;
                    }
                    boolean rib = dx == 0 || dz == 0 || Math.abs(dx) == Math.abs(dz);
                    b.put(x + dx, y, z + dz, rib ? p.accent() : p.ground());
                }
            }
        }
        // Dark window slits at the cardinal and diagonal faces of the drum.
        b.put(x, baseY + 3, z - 16, p.window());
        b.put(x, baseY + 3, z + 16, p.window());
        b.put(x - 16, baseY + 3, z, p.window());
        b.put(x + 16, baseY + 3, z, p.window());
        b.put(x - 11, baseY + 3, z - 11, p.window());
        b.put(x + 11, baseY + 3, z - 11, p.window());
        b.put(x - 11, baseY + 3, z + 11, p.window());
        b.put(x + 11, baseY + 3, z + 11, p.window());
        // The dome shell: thirteen shrinking rings, ribbed in white stone.
        for (int i = 0; i < 13; i++) {
            int r = 16 - i;
            int y = baseY + 8 + i;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > r * r || d2 < (r - 2) * (r - 2)) {
                        continue;
                    }
                    boolean rib = dx == 0 || dz == 0 || Math.abs(dx) == Math.abs(dz);
                    b.put(x + dx, y, z + dz, rib ? p.accent() : p.roof());
                }
            }
        }
        int top = baseY + 21;
        b.fill(x - 2, top, z - 2, x + 2, top, z + 2, p.roof());
        b.fill(x - 2, top + 1, z - 2, x + 2, top + 6, z + 2, p.ground());
        b.fill(x - 1, top + 2, z - 1, x + 1, top + 5, z + 1, p.light());
        b.fill(x - 3, top + 7, z - 3, x + 3, top + 7, z + 3, p.roofSlab());
        // The golden cross.
        b.fill(x, top + 8, z, x, top + 11, z, Blocks.GOLD_BLOCK.defaultBlockState());
        b.fill(x - 1, top + 10, z, x + 1, top + 10, z, Blocks.GOLD_BLOCK.defaultBlockState());
    }

    // ------------------------------------------------------------------ Big Ben

    private static void bigBen(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x = cx + 80;
        int z = cz - 120;
        int tr = 3;

        b.fill(x - 10, ground, z - 10, x + 10, ground + 1, z + 10, p.foundation());
        b.room(x - tr, ground + 1, z - tr, x + tr, ground + 58, z + tr);

        for (int y = ground + 1; y <= ground + 58; y++) {
            b.put(x - tr, y, z - tr, p.accent());
            b.put(x + tr, y, z - tr, p.accent());
            b.put(x - tr, y, z + tr, p.accent());
            b.put(x + tr, y, z + tr, p.accent());
            if (y % 4 == 0) {
                b.fill(x - tr, y, z - tr, x + tr, y, z + tr, p.accent());
            }
        }
        for (int y = ground + 6; y <= ground + 44; y += 6) {
            b.window(x, y, z - tr, 3, 1, true);
            b.window(x, y, z + tr, 3, 1, true);
            b.window(x - tr, y, z, 3, 1, true);
            b.window(x + tr, y, z, 3, 1, true);
        }

        // The clock stage: four white dials with a lit centre and dark hands.
        int dial = ground + 50;
        for (int y = dial; y <= dial + 4; y++) {
            b.fill(x - 3, y, z - tr, x + 3, y, z - tr, p.accent());
            b.fill(x - 3, y, z + tr, x + 3, y, z + tr, p.accent());
            b.fill(x - tr, y, z - 3, x - tr, y, z + 3, p.accent());
            b.fill(x + tr, y, z - 3, x + tr, y, z + 3, p.accent());
        }
        b.put(x, dial + 2, z - tr, p.light());
        b.put(x, dial + 2, z + tr, p.light());
        b.put(x - tr, dial + 2, z, p.light());
        b.put(x + tr, dial + 2, z, p.light());
        BlockState hand = Blocks.CONCRETE.pick(DyeColor.BLACK).defaultBlockState();
        b.put(x, dial + 3, z - tr, hand);
        b.put(x + 1, dial + 2, z - tr, hand);
        b.put(x, dial + 3, z + tr, hand);
        b.put(x - 1, dial + 2, z + tr, hand);
        b.put(x - tr, dial + 3, z, hand);
        b.put(x - tr, dial + 2, z + 1, hand);
        b.put(x + tr, dial + 3, z, hand);
        b.put(x + tr, dial + 2, z - 1, hand);

        // Belfry, crenellations and the spire.
        b.fill(x - 2, ground + 55, z - tr, x + 2, ground + 58, z - tr, air());
        b.fill(x - 2, ground + 55, z + tr, x + 2, ground + 58, z + tr, air());
        b.fill(x - tr, ground + 55, z - 2, x - tr, ground + 58, z + 2, air());
        b.fill(x + tr, ground + 55, z - 2, x + tr, ground + 58, z + 2, air());
        b.crenellations(x - tr - 1, z - tr - 1, x + tr + 1, z + tr + 1, ground + 59);
        b.spire(x, z, ground + 60, 12);

        // The Gothic Parliament wing running west from the tower.
        int px0 = x + 10;
        int px1 = x + 52;
        int pz0 = z - 16;
        int pz1 = z + 8;
        b.room(px0, ground + 1, pz0, px1, ground + 14, pz1);
        b.walls(px0, ground + 2, pz0, px1, ground + 13, pz1, p.ground());
        for (int wx = px0 + 5; wx <= px1 - 5; wx += 8) {
            b.window(wx, ground + 5, pz0, 6, 2, true);
            b.window(wx, ground + 5, pz1, 6, 2, true);
            b.buttress(wx, pz0 - 1, ground + 2, 5, Side.N);
            b.buttress(wx, pz1 + 1, ground + 2, 5, Side.S);
        }
        b.pitchedRoof(px0 - 1, pz0 - 1, px1 + 1, pz1 + 1, ground + 14, 5, 0);
        b.crenellations(px0 - 1, pz0 - 1, px1 + 1, pz1 + 1, ground + 19);
        placeDoor(b, p, px0, pz0, px1, pz1, ground + 2, Side.W, 12);

        signAt(b, x, ground + 3, z + tr + 1, Direction.SOUTH, "BIG BEN");
        signAt(b, x + 6, ground + 2, z + tr + 4, Direction.SOUTH, "PARLIAMENT");
    }

    // ------------------------------------------------------------------ the City

    private static void cityBlocks(StructureBuilder b, int cx, int cz, Palette p,
                                   IntBinaryOperator surface) {
        office(b, cx - 130, cz - 260, 30, 30, 14,
                surface.applyAsInt(cx - 115, cz - 245), p, "BANK", Side.S);
        office(b, cx + 100, cz - 260, 30, 30, 12,
                surface.applyAsInt(cx + 115, cz - 245), p, "EXCHANGE", Side.S);
        office(b, cx - 130, cz - 180, 30, 30, 12,
                surface.applyAsInt(cx - 115, cz - 165), p, "GUILDHALL", Side.S);
        office(b, cx + 100, cz - 180, 30, 30, 10,
                surface.applyAsInt(cx + 115, cz - 165), p, "OFFICES", Side.S);
        office(b, cx - 70, cz - 250, 30, 30, 12,
                surface.applyAsInt(cx - 55, cz - 235), p, "LLOYD'S", Side.S);
        office(b, cx + 40, cz - 250, 30, 30, 12,
                surface.applyAsInt(cx + 55, cz - 235), p, "BANK", Side.S);
        office(b, cx - 70, cz - 180, 30, 30, 10,
                surface.applyAsInt(cx - 55, cz - 165), p, "COUNTING HOUSE", Side.S);
        office(b, cx + 40, cz - 180, 30, 30, 10,
                surface.applyAsInt(cx + 55, cz - 165), p, "OFFICES", Side.S);
        office(b, cx - 70, cz - 90, 30, 24, 12,
                surface.applyAsInt(cx - 55, cz - 78), p, "ROYAL EXCHANGE", Side.N);
        office(b, cx + 30, cz - 90, 30, 24, 12,
                surface.applyAsInt(cx + 45, cz - 78), p, "BANK OF ENGLAND", Side.N);

        // The City notice board, an island of timber in the cross street.
        int ground = surface.applyAsInt(cx, cz - 120);
        questBoard(b, cx - 80, cz - 120, ground, p);

        // Two City statues on the cross street.
        b.monument(cx - 40, cz - 120, ground + 1);
        b.monument(cx + 40, cz - 120, ground + 1);

        signAt(b, cx - 90, ground + 1, cz - 112, Direction.SOUTH, "CITY NOTICES");
        signAt(b, cx + 20, ground + 1, cz - 112, Direction.SOUTH, "BANK");
        signAt(b, cx - 120, ground + 1, cz - 112, Direction.SOUTH, "GUILDHALL");
    }

    private static void office(StructureBuilder b, int x, int z, int w, int d, int h,
                               int ground, Palette p, String name, Side door) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;

        b.fill(x, ground, z, x1, ground, z1, p.foundation());
        b.room(x, y0, z, x1, y0 + h, z1);
        b.walls(x, y0 + 1, z, x1, y0 + h - 1, z1, p.ground());
        b.walls(x, y0 + 1, z, x1, y0 + 1, z1, p.foundation());
        b.walls(x, y0 + h, z, x1, y0 + h, z1, p.accent());

        for (int y = y0 + 3; y <= y0 + h - 3; y += 4) {
            for (int wx = x + 3; wx <= x1 - 3; wx += 5) {
                b.window(wx, y, z, 3, 2, true);
                b.window(wx, y, z1, 3, 2, true);
            }
            for (int wz = z + 3; wz <= z1 - 3; wz += 5) {
                b.window(x, y, wz, 3, 1, true);
                b.window(x1, y, wz, 3, 1, true);
            }
        }
        // A portico of columns on the street front.
        int fz = door == Side.S ? z1 + 2 : z - 2;
        for (int px = x + 3; px <= x1 - 3; px += 5) {
            b.fill(px, ground + 1, fz, px, ground + 6, fz, p.accent());
        }
        b.fill(x + 1, ground + 7, fz - 1, x1 - 1, ground + 7, fz + 1, p.roofSlab());
        b.crenellations(x, z, x1, z1, y0 + h + 1);
        placeDoor(b, p, x, z, x1, z1, y0 + 1, door, w / 2);
        signAt(b, x + w / 2, ground + 5, fz + (door == Side.S ? 2 : -2), door == Side.S
                ? Direction.SOUTH : Direction.NORTH, name);
        b.put(x + 2, y0 + 2, z1 + 1, p.light());
        b.put(x1 - 2, y0 + 2, z1 + 1, p.light());
    }

    private static void questBoard(StructureBuilder b, int x, int z, int ground, Palette p) {
        // A timber kiosk with a pitched cap, four legs and two pinned notices.
        b.fill(x - 5, ground, z - 2, x + 5, ground, z + 2, p.foundation());
        for (int px = -5; px <= 5; px += 10) {
            for (int pz = -2; pz <= 2; pz += 4) {
                b.put(x + px, ground + 1, z + pz, p.accent());
                b.put(x + px, ground + 2, z + pz, p.accent());
            }
        }
        b.fill(x - 5, ground + 3, z - 2, x + 5, ground + 3, z + 2, p.roofSlab());
        b.fill(x - 4, ground + 4, z - 1, x + 4, ground + 4, z + 1, p.roof());
        b.fill(x - 4, ground + 1, z - 1, x + 4, ground + 2, z - 1, p.frame());
        signAt(b, x - 2, ground + 2, z, Direction.SOUTH, "CITY NOTICES");
        signAt(b, x + 2, ground + 2, z, Direction.SOUTH, "REWARDS");
        b.put(x - 4, ground + 2, z + 2, p.light());
        b.put(x + 4, ground + 2, z + 2, p.light());
    }

    // ------------------------------------------------------------------ British Museum

    private static void britishMuseum(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x = cx - 240;
        int z = cz;
        int x0 = x - 52;
        int x1 = x + 52;
        int z0 = z - 30;
        int z1 = z + 56;

        // Podium, steps and the long stone front.
        b.fill(x0 - 4, ground, z0 - 4, x1 + 4, ground, z1 + 10, p.foundation());
        b.fill(x - 16, ground + 1, z1, x + 16, ground + 1, z1 + 6, p.foundation());
        b.fill(x - 10, ground + 2, z1, x + 10, ground + 2, z1 + 3, p.foundation());

        b.room(x0, ground + 1, z0, x1, ground + 18, z1);
        b.walls(x0, ground + 2, z0, x1, ground + 17, z1, p.ground());
        b.walls(x0, ground + 2, z0, x1, ground + 3, z1, p.accent());
        b.walls(x0, ground + 16, z0, x1, ground + 17, z1, p.accent());
        b.fill(x0 - 1, ground + 18, z0 - 1, x1 + 1, ground + 18, z1 + 1, p.roof());
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + 19);

        // The south front: a colonnade and a stepped pediment.
        for (int px = x0 + 6; px <= x1 - 6; px += 8) {
            b.fill(px, ground + 1, z1 + 4, px, ground + 12, z1 + 4, p.accent());
        }
        b.fill(x0 + 2, ground + 13, z1 + 2, x1 - 2, ground + 14, z1 + 6, p.roof());
        for (int i = 0; i < 5; i++) {
            b.fill(x - 12 + i * 2, ground + 15 + i, z1 + 2, x + 12 - i * 2, ground + 15 + i,
                    z1 + 5, p.accent());
        }

        // Windows and pilasters down the long flanks.
        for (int wz = z0 + 6; wz <= z1 - 6; wz += 8) {
            b.window(x0, ground + 6, wz, 6, 1, true);
            b.window(x1, ground + 6, wz, 6, 1, true);
            b.buttress(x0 - 1, wz, ground + 2, 4, Side.W);
            b.buttress(x1 + 1, wz, ground + 2, 4, Side.E);
        }
        placeDoor(b, p, x0, z0, x1, z1, ground + 2, Side.S, 52);
        placeDoor(b, p, x0, z0, x1, z1, ground + 2, Side.N, 52);
        b.fill(x - 3, ground, z0 - 2, x + 3, ground + 1, z0 - 1, p.foundation());

        // The Great Court, its glass roof and the round reading room.
        b.fill(x - 12, ground + 2, z + 4, x + 12, ground + 17, z + 30, air());
        b.fill(x - 13, ground + 18, z + 3, x + 13, ground + 18, z + 31,
                Materials.stainedGlass(DyeColor.WHITE));
        readingRoom(b, x, z + 17, ground + 2, p);

        // Gallery partitions, leaving two openings in each.
        for (int gz = z - 8; gz <= z + 34; gz += 42) {
            b.walls(x0 + 1, ground + 2, gz, x1 - 1, ground + 17, gz, p.ground());
            b.fill(x - 30, ground + 2, gz, x - 26, ground + 6, gz, air());
            b.fill(x + 26, ground + 2, gz, x + 30, ground + 6, gz, air());
        }
        for (int gx = x - 30; gx <= x + 30; gx += 60) {
            b.walls(gx, ground + 2, z - 27, gx, ground + 17, z + 3, p.ground());
            b.walls(gx, ground + 2, z + 31, gx, ground + 17, z + 55, p.ground());
        }

        // Display cases along the galleries.
        for (int wx = x0 + 8; wx <= x1 - 8; wx += 14) {
            b.fill(wx, ground + 2, z - 24, wx + 2, ground + 3, z - 22, p.foundation());
            b.fill(wx, ground + 4, z - 24, wx + 2, ground + 4, z - 22, p.window());
            b.fill(wx, ground + 2, z + 44, wx + 2, ground + 3, z + 46, p.foundation());
            b.fill(wx, ground + 4, z + 44, wx + 2, ground + 4, z + 46, p.window());
        }

        // The mummy case: a black stone sarcophagus with a gold mask.
        int mx = x - 40;
        int mz = z + 10;
        b.fill(mx - 2, ground + 2, mz - 2, mx + 2, ground + 2, mz + 3, p.foundation());
        b.fill(mx - 1, ground + 3, mz - 1, mx + 1, ground + 3, mz + 2,
                Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        b.fill(mx - 1, ground + 4, mz - 1, mx + 1, ground + 4, mz + 2,
                Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        b.put(mx, ground + 4, mz, Blocks.GOLD_BLOCK.defaultBlockState());
        b.put(mx, ground + 3, mz + 2, p.light());
        b.chest(mx + 3, ground + 2, mz + 1, SiteLoot.CULT);
        signAt(b, mx, ground + 5, mz + 3, Direction.SOUTH, "MUMMY CASE", "DO NOT OPEN");

        // Gallery signs.
        signAt(b, x - 34, ground + 4, z - 9, Direction.SOUTH, "EGYPTIAN", "GALLERY");
        signAt(b, x + 34, ground + 4, z - 9, Direction.SOUTH, "ASSYRIAN", "GALLERY");
        signAt(b, x - 34, ground + 4, z + 33, Direction.SOUTH, "READING ROOM");
        signAt(b, x, ground + 9, z1 + 7, Direction.SOUTH, "BRITISH MUSEUM", "GALLERIES");

        b.put(x0 + 2, ground + 2, z1 + 1, p.light());
        b.put(x1 - 2, ground + 2, z1 + 1, p.light());
    }

    private static void readingRoom(StructureBuilder b, int x, int z, int baseY, Palette p) {
        // A round brick reading room inside the glass court.
        for (int y = baseY; y < baseY + 9; y++) {
            for (int dx = -7; dx <= 7; dx++) {
                for (int dz = -7; dz <= 7; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > 49 || d2 < 30) {
                        continue;
                    }
                    b.put(x + dx, y, z + dz, ((dx + dz) & 1) == 0 ? p.wall() : p.weathered());
                }
            }
        }
        for (int i = 0; i < 5; i++) {
            int r = 7 - i;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > r * r || d2 < (r - 2) * (r - 2)) {
                        continue;
                    }
                    b.put(x + dx, baseY + 9 + i, z + dz, p.accent());
                }
            }
        }
        b.put(x, baseY + 14, z, p.light());
        b.fill(x, baseY + 1, z - 7, x, baseY + 6, z - 7, p.window());
    }

    private static void museumQuarter(StructureBuilder b, int cx, int cz, Palette p,
                                      IntBinaryOperator surface) {
        // The forecourt, its fountain and the approach to the colonnade.
        pave(b, cx - 300, cz + 58, cx - 180, cz + 92, surface, p.foundation());
        fountain(b, cx - 240, cz + 78, surface.applyAsInt(cx - 240, cz + 78), p);
        signAt(b, cx - 240, surface.applyAsInt(cx - 240, cz + 92) + 1, cz + 92,
                Direction.SOUTH, "BRITISH MUSEUM");

        // Townhouses south of the museum street.
        int[] rowX = {-320, -288, -256, -224, -192, -160};
        for (int i = 0; i < rowX.length; i++) {
            int x = cx + rowX[i];
            townhouse(b, x, cz + 103, 24, 20, 12 + i % 3,
                    surface.applyAsInt(x + 12, cz + 113), p, Side.N);
        }

        // Bloomsbury square with its garden and surrounding houses.
        pave(b, cx - 232, cz - 192, cx - 168, cz - 128, surface, p.foundation());
        int sq = surface.applyAsInt(cx - 200, cz - 160);
        b.fill(cx - 216, sq, cz - 176, cx - 184, sq, cz - 144,
                Blocks.GRASS_BLOCK.defaultBlockState());
        railing(b, cx - 216, cz - 176, cx - 184, cz - 144, sq + 1, p);
        b.fill(cx - 202, sq + 1, cz - 176, cx - 198, sq + 1, cz - 172, air());
        fountain(b, cx - 200, cz - 160, sq, p);
        tree(b, cx - 210, cz - 170, sq, p);
        tree(b, cx - 190, cz - 170, sq, p);
        tree(b, cx - 210, cz - 150, sq, p);
        tree(b, cx - 190, cz - 150, sq, p);

        for (int i = 0; i < 3; i++) {
            int nx = cx - 232 + i * 32;
            townhouse(b, nx, cz - 226, 24, 20, 14,
                    surface.applyAsInt(nx + 12, cz - 216), p, Side.S);
            townhouse(b, nx, cz - 114, 24, 20, 14,
                    surface.applyAsInt(nx + 12, cz - 104), p, Side.N);
        }
        townhouse(b, cx - 320, cz - 180, 20, 24, 13,
                surface.applyAsInt(cx - 310, cz - 168), p, Side.E);
        townhouse(b, cx - 160, cz - 180, 20, 24, 13,
                surface.applyAsInt(cx - 150, cz - 168), p, Side.W);

        // The curator's office north of the museum.
        int ox = cx - 240;
        int oz = cz - 68;
        int ground = surface.applyAsInt(ox, oz);
        b.fill(ox - 16, ground, oz - 12, ox + 16, ground, oz + 12, p.foundation());
        b.room(ox - 14, ground + 1, oz - 10, ox + 14, ground + 10, oz + 10);
        b.walls(ox - 14, ground + 2, oz - 10, ox + 14, ground + 9, oz + 10, p.ground());
        b.pitchedRoof(ox - 15, oz - 11, ox + 15, oz + 11, ground + 10, 4, 0);
        placeDoor(b, p, ox - 14, oz - 10, ox + 14, oz + 10, ground + 2, Side.S, 14);
        b.fill(ox - 3, ground, oz + 11, ox + 3, ground + 1, oz + 12, p.foundation());
        for (int bx = ox - 12; bx <= ox + 12; bx += 4) {
            b.fill(bx, ground + 2, oz - 9, bx, ground + 5, oz - 9, Blocks.BOOKSHELF.defaultBlockState());
        }
        b.fill(ox - 4, ground + 2, oz, ox + 4, ground + 2, oz + 4, p.accent());
        b.put(ox - 4, ground + 3, oz, p.light());
        b.put(ox + 4, ground + 3, oz + 4, p.light());
        b.chest(ox + 11, ground + 2, oz + 7, SiteLoot.COMMON);
        signAt(b, ox, ground + 4, oz + 11, Direction.SOUTH, "CURATOR");
        pave(b, cx - 242, cz - 56, cx - 238, cz - 30, surface, p.ground());
        signAt(b, cx - 240, surface.applyAsInt(cx - 240, cz - 50) + 1, cz - 50,
                Direction.SOUTH, "CURATOR OFFICE");

        // Museum-street signs.
        signAt(b, cx - 300, surface.applyAsInt(cx - 300, cz + 101) + 1, cz + 101,
                Direction.SOUTH, "BRITISH MUSEUM");
        signAt(b, cx - 180, surface.applyAsInt(cx - 180, cz + 101) + 1, cz + 101,
                Direction.SOUTH, "MUSEUM STREET");
    }

    private static void townhouse(StructureBuilder b, int x, int z, int w, int d, int h,
                                  int ground, Palette p, Side door) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;

        b.fill(x, ground, z, x1, ground, z1, p.foundation());
        b.room(x, y0, z, x1, y0 + h, z1);
        b.walls(x, y0 + 1, z, x1, y0 + h - 1, z1, p.ground());
        b.walls(x, y0 + 1, z, x1, y0 + 1, z1, p.accent());
        for (int y = y0 + 1; y <= y0 + h; y++) {
            b.put(x, y, z, p.accent());
            b.put(x1, y, z, p.accent());
            b.put(x, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
        for (int y = y0 + 3; y <= y0 + h - 2; y += 3) {
            for (int wx = x + 3; wx <= x1 - 3; wx += 4) {
                b.window(wx, y, z, 2, 2, true);
                b.window(wx, y, z1, 2, 2, true);
            }
            for (int wz = z + 3; wz <= z1 - 3; wz += 4) {
                b.window(x, y, wz, 2, 1, true);
                b.window(x1, y, wz, 2, 1, true);
            }
        }
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, y0 + h, 4, w >= d ? 0 : 1);
        b.fill(x + 2, y0 + h + 2, z + 2, x + 2, y0 + h + 5, z + 2, p.wall());
        b.put(x + 2, y0 + h + 6, z + 2, p.accent());
        placeDoor(b, p, x, z, x1, z1, y0 + 1, door, w / 2);
        signAt(b, x + w / 2, ground + 4, door == Side.S ? z1 + 1 : z - 1, door == Side.S
                ? Direction.SOUTH : Direction.NORTH, "BLOOMSBURY");
    }

    // ------------------------------------------------------------------ West End

    private static void westEnd(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // North of the first street: hotels and tall shops.
        hotel(b, cx - 90, cz + 16, 32, 24, 20, ground, p, "SAVOY HOTEL", Side.S);
        hotel(b, cx + 24, cz + 16, 32, 24, 18, ground, p, "THE RITZ", Side.S);
        shop(b, cx - 140, cz + 16, 28, 24, 10, ground, p, "BOOKS", Side.S);
        shop(b, cx - 56, cz + 16, 28, 24, 9, ground, p, "TAILOR", Side.S);
        shop(b, cx + 64, cz + 16, 24, 24, 10, ground, p, "JEWELLER", Side.S);
        shop(b, cx + 100, cz + 16, 24, 24, 9, ground, p, "HATTER", Side.S);

        // Between the streets: cafes and the West End shop.
        shop(b, cx - 136, cz + 64, 24, 24, 9, ground, p, "CHEMIST", Side.N);
        cafe(b, cx - 88, cz + 64, 28, 24, ground, p, "CAFE");
        shop(b, cx - 52, cz + 64, 24, 24, 9, ground, p, "WEST END", Side.N);
        cafe(b, cx + 28, cz + 64, 24, 24, ground, p, "TEA ROOM");
        shop(b, cx + 56, cz + 64, 28, 24, 10, ground, p, "GALLERY", Side.N);
        shop(b, cx + 98, cz + 64, 24, 24, 9, ground, p, "FLORIST", Side.N);

        // South of the second street: theatres, hotels and the police box.
        theatre(b, cx - 124, cz + 114, 48, 32, 16, ground, p, "ALDWYCH");
        theatre(b, cx + 52, cz + 116, 48, 32, 16, ground, p, "PALACE");
        hotel(b, cx - 72, cz + 114, 28, 26, 15, ground, p, "HOTEL", Side.N);
        hotel(b, cx + 8, cz + 118, 24, 24, 14, ground, p, "BROWN'S", Side.N);
        policeBox(b, cx, cz + 136, ground, p);

        // Street signs.
        signAt(b, cx - 100, ground + 1, cz + 51, Direction.SOUTH, "WEST END");
        signAt(b, cx + 40, ground + 1, cz + 51, Direction.SOUTH, "THEATRELAND");
        signAt(b, cx - 100, ground + 1, cz + 111, Direction.SOUTH, "WEST END");
        signAt(b, cx + 80, ground + 1, cz + 111, Direction.SOUTH, "UNDERGROUND");
        signAt(b, cx - 80, ground + 1, cz + 41, Direction.SOUTH, "SAVOY HOTEL");
        signAt(b, cx - 40, ground + 1, cz + 63, Direction.NORTH, "WEST END SHOP");
    }

    private static void hotel(StructureBuilder b, int x, int z, int w, int d, int h,
                              int ground, Palette p, String name, Side door) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;
        int front = door == Side.S ? z1 : z;
        int dir = door == Side.S ? 1 : -1;
        Direction facing = door == Side.S ? Direction.SOUTH : Direction.NORTH;

        b.fill(x, ground, z, x1, ground, z1, p.foundation());
        b.room(x, y0, z, x1, y0 + h, z1);
        b.walls(x, y0 + 1, z, x1, y0 + h - 1, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 1, z1, p.accent());
        b.walls(x, y0 + 5, z, x1, y0 + 5, z1, p.accent());
        for (int y = y0 + 3; y <= y0 + h - 2; y += 3) {
            for (int wx = x + 3; wx <= x1 - 3; wx += 4) {
                b.window(wx, y, z1, 2, 2, true);
                b.window(wx, y, z, 2, 2, true);
            }
        }
        b.crenellations(x - 1, z - 1, x1 + 1, z1 + 1, y0 + h + 1);
        placeDoor(b, p, x, z, x1, z1, y0 + 1, door, w / 2);

        // A canopy over the entrance and a lit lobby.
        b.fill(x + 4, y0 + 4, front + dir, x1 - 4, y0 + 4, front + 4 * dir, p.roofSlab());
        b.fill(x + 5, y0 + 3, front + 4 * dir, x1 - 5, y0 + 3, front + 4 * dir, p.light());
        b.fill(x + 3, y0 + 1, front - 3 * dir, x + 5, y0 + 2, front - 3 * dir, p.accent());
        bed(b, x + 4, y0 + 1, front - 5 * dir, facing);
        b.put(x + 8, y0 + 2, front - 4 * dir, p.light());

        signAt(b, x + w / 2, y0 + 6, front + 5 * dir, facing, name);
        signAt(b, x + 4, ground + 1, front + 2 * dir, facing, "RECEPTION");
    }

    private static void shop(StructureBuilder b, int x, int z, int w, int d, int h,
                             int ground, Palette p, String name, Side door) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;

        b.fill(x, ground, z, x1, ground, z1, p.foundation());
        b.room(x, y0, z, x1, y0 + h, z1);
        b.walls(x, y0 + 1, z, x1, y0 + h - 1, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 1, z1, p.accent());
        // Big shop windows on the street front.
        int front = door == Side.S ? z1 : z;
        for (int wx = x + 3; wx <= x1 - 3; wx += 5) {
            b.window(wx, y0 + 2, front, 4, 2, false);
        }
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, y0 + h, 4, w >= d ? 0 : 1);
        placeDoor(b, p, x, z, x1, z1, y0 + 1, door, w / 2);
        // Striped awning over the pavement.
        int az = door == Side.S ? z1 + 1 : z - 2;
        b.fill(x + 2, y0 + 3, az, x1 - 2, y0 + 3, az + 1, Materials.wool(DyeColor.RED));
        b.fill(x + 2, y0 + 3, az + 1, x1 - 2, y0 + 3, az + 1, Materials.wool(DyeColor.WHITE));
        signAt(b, x + w / 2, y0 + 5, door == Side.S ? z1 + 1 : z - 1,
                door == Side.S ? Direction.SOUTH : Direction.NORTH, name);
        b.put(x + 2, y0 + 2, z + 2, p.light());
        b.chest(x1 - 2, y0 + 1, z + 2, SiteLoot.COMMON);
    }

    private static void cafe(StructureBuilder b, int x, int z, int w, int d, int ground,
                             Palette p, String name) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;

        b.fill(x, ground, z, x1, ground, z1, p.foundation());
        b.room(x, y0, z, x1, y0 + 7, z1);
        b.walls(x, y0 + 1, z, x1, y0 + 6, z1, p.wall());
        for (int wx = x + 2; wx <= x1 - 2; wx += 4) {
            b.window(wx, y0 + 2, z1, 3, 2, false);
        }
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, y0 + 7, 3, 0);
        placeDoor(b, p, x, z, x1, z1, y0 + 1, Side.S, w / 2);
        b.fill(x + 2, y0 + 3, z1 + 1, x1 - 2, y0 + 3, z1 + 3, p.roofSlab());
        b.fill(x + 3, y0 + 3, z1 + 3, x1 - 3, y0 + 3, z1 + 3, p.light());
        // Pavement tables under the awning.
        b.put(x + 4, ground + 1, z1 + 4, p.accent());
        b.put(x + 5, ground + 2, z1 + 4, p.roofSlab());
        b.put(x1 - 4, ground + 1, z1 + 4, p.accent());
        b.put(x1 - 5, ground + 2, z1 + 4, p.roofSlab());
        signAt(b, x + w / 2, y0 + 4, z1 + 4, Direction.SOUTH, name);
    }

    private static void theatre(StructureBuilder b, int x, int z, int w, int d, int h,
                                int ground, Palette p, String name) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;

        b.fill(x, ground, z, x1, ground, z1, p.foundation());
        b.room(x, y0, z, x1, y0 + h, z1);
        b.walls(x, y0 + 1, z, x1, y0 + h - 1, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 1, z1, p.accent());
        b.walls(x, y0 + h - 1, z, x1, y0 + h - 1, z1, p.accent());
        for (int wx = x + 4; wx <= x1 - 4; wx += 6) {
            b.window(wx, y0 + 4, z, 5, 2, true);
            b.window(wx, y0 + 4, z1, 5, 2, true);
        }
        b.pitchedRoof(x - 2, z - 2, x1 + 2, z1 + 2, y0 + h, 6, 0);
        placeDoor(b, p, x, z, x1, z1, y0 + 1, Side.N, w / 2);

        // A deep marquee with a lit fascia and poster boards.
        b.fill(x + 6, y0 + 7, z - 4, x1 - 6, y0 + 7, z - 1, p.roofSlab());
        b.fill(x + 7, y0 + 6, z - 4, x1 - 7, y0 + 6, z - 4, p.light());
        b.fill(x + 4, y0 + 2, z - 1, x + 8, y0 + 4, z - 1,
                Materials.stainedGlass(DyeColor.MAGENTA));
        b.fill(x1 - 8, y0 + 2, z - 1, x1 - 4, y0 + 4, z - 1,
                Materials.stainedGlass(DyeColor.CYAN));
        b.fill(x + 10, y0 + 1, z1 - 2, x1 - 10, y0 + 1, z1 - 2, p.accent());
        for (int sz = z + 5; sz <= z1 - 8; sz += 4) {
            b.fill(x + 6, y0 + 1, sz, x1 - 6, y0 + 1, sz, p.accent());
            b.fill(x + 6, y0 + 2, sz, x1 - 6, y0 + 2, sz, Materials.wool(DyeColor.RED));
        }
        signAt(b, x + w / 2, y0 + 8, z - 1, Direction.NORTH, name);
        signAt(b, x + 6, ground + 1, z - 5, Direction.NORTH, "THEATRE");
        b.put(x + 2, y0 + 3, z - 1, p.light());
        b.put(x1 - 2, y0 + 3, z - 1, p.light());
    }

    private static void policeBox(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 4, ground, z - 4, x + 4, ground, z + 4, p.foundation());
        b.room(x - 3, ground + 1, z - 3, x + 3, ground + 7, z + 3);
        b.walls(x - 3, ground + 2, z - 3, x + 3, ground + 6, z + 3, p.wall());
        b.walls(x - 3, ground + 6, z - 3, x + 3, ground + 6, z + 3, p.accent());
        placeDoor(b, p, x - 3, z - 3, x + 3, z + 3, ground + 2, Side.S, 3);
        b.put(x, ground + 5, z - 3, p.light());
        b.crenellations(x - 4, z - 4, x + 4, z + 4, ground + 8);
        signAt(b, x, ground + 5, z + 4, Direction.SOUTH, "POLICE");
    }

    // ------------------------------------------------------------------ docks

    private static void docks(StructureBuilder b, int cx, int cz, Palette p,
                              IntBinaryOperator surface) {
        int rim = surface.applyAsInt(cx + 288, cz - 10);
        int railY = surface.applyAsInt(cx + 230, cz - 250);

        // The water basin east of the quay, with stone edges and a moored barge.
        basin(b, cx + 258, cz - 110, cx + 318, cz + 96, rim, p);

        warehouse(b, cx + 176, cz - 226, 36, 40, 14, surface.applyAsInt(cx + 194, cz - 206),
                p, "WAREHOUSE", false);
        warehouse(b, cx + 176, cz - 166, 36, 40, 14, surface.applyAsInt(cx + 194, cz - 146),
                p, "TOBACCO", false);
        warehouse(b, cx + 176, cz - 96, 36, 40, 14, surface.applyAsInt(cx + 194, cz - 76),
                p, "TEA", false);
        warehouse(b, cx + 176, cz - 26, 36, 40, 14, surface.applyAsInt(cx + 194, cz - 6),
                p, "WOOL", false);
        warehouse(b, cx + 176, cz + 44, 36, 40, 14, surface.applyAsInt(cx + 194, cz + 64),
                p, "GRAIN", false);
        warehouse(b, cx + 176, cz + 104, 36, 40, 14, surface.applyAsInt(cx + 194, cz + 124),
                p, "SUGAR", true);

        // The dock pub, facing the quay.
        dockPub(b, cx + 222, cz + 46, 16, 28, surface.applyAsInt(cx + 230, cz + 60), p);

        // Three coal cranes over the water.
        crane(b, cx + 250, cz - 90, surface.applyAsInt(cx + 250, cz - 90), p);
        crane(b, cx + 250, cz - 20, surface.applyAsInt(cx + 250, cz - 20), p);
        crane(b, cx + 250, cz + 70, surface.applyAsInt(cx + 250, cz + 70), p);

        railway(b, cx, cz, railY, p);

        // Quay dressing: bollards, crates and a coal yard south of the basin.
        for (int z = -250; z <= 170; z += 20) {
            int by = surface.applyAsInt(cx + 246, cz + z);
            b.put(cx + 246, by + 1, cz + z, p.foundation());
            b.put(cx + 246, by + 2, cz + z, p.accent());
        }
        for (int i = 0; i < 8; i++) {
            int bx = cx + 216 + i % 4 * 8;
            int bz = cz - 230 + i / 4 * 6;
            int by = surface.applyAsInt(bx, bz);
            b.put(bx, by + 1, bz, Blocks.BARREL.defaultBlockState());
            b.put(bx + 1, by + 1, bz, Blocks.BARREL.defaultBlockState());
            b.put(bx, by + 2, bz, Blocks.BARREL.defaultBlockState());
        }
        for (int i = 0; i < 6; i++) {
            int bx = cx + 262 + i % 3 * 16;
            int bz = cz + 112 + i / 3 * 14;
            b.fill(bx, rim + 1, bz, bx + 2, rim + 2, bz + 2, p.foundation());
        }

        signAt(b, cx + 250, surface.applyAsInt(cx + 250, cz - 112) + 1, cz - 112,
                Direction.SOUTH, "DOCKS");
        signAt(b, cx + 250, surface.applyAsInt(cx + 250, cz - 108) + 1, cz - 108,
                Direction.SOUTH, "NO ENTRY");
        signAt(b, cx + 244, surface.applyAsInt(cx + 244, cz + 100) + 1, cz + 100,
                Direction.WEST, "DOCK QUAY");
        signAt(b, cx + 250, surface.applyAsInt(cx + 250, cz + 96) + 1, cz + 96,
                Direction.SOUTH, "MIND THE WATER");
        signAt(b, cx + 160, surface.applyAsInt(cx + 160, cz - 254) + 1, cz - 254,
                Direction.SOUTH, "GOODS DEPOT");
        b.put(cx + 250, surface.applyAsInt(cx + 250, cz - 112) + 3, cz - 112, p.light());
    }

    private static void basin(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                              Palette p) {
        // Carve the basin and fill it, leaving a stone lip on the quay side.
        b.fill(x0, ground - 6, z0, x1, ground, z1, air());
        b.fill(x0, ground - 6, z0, x1, ground - 6, z1, p.foundation());
        b.fill(x0, ground - 5, z0, x1, ground - 1, z1, Blocks.WATER.defaultBlockState());
        b.fill(x0 - 2, ground, z0, x0 - 1, ground, z1, p.foundation());
        b.walls(x0, ground - 1, z0, x1, ground - 1, z1, p.foundation());

        // A barge at the north end, with a mast and a lantern.
        int bx = x0 + 12;
        int bz = z0 + 24;
        b.fill(bx, ground - 2, bz, bx + 14, ground - 1, bz + 10,
                Blocks.DARK_OAK_PLANKS.defaultBlockState());
        b.fill(bx + 2, ground, bz + 2, bx + 12, ground + 2, bz + 8, p.wall());
        b.fill(bx + 4, ground + 3, bz + 4, bx + 10, ground + 3, bz + 6, p.roofSlab());
        b.fill(bx + 7, ground + 4, bz + 5, bx + 7, ground + 9, bz + 5, p.accent());
        b.put(bx + 7, ground + 10, bz + 5, p.light());
        b.put(bx + 2, ground + 2, bz + 5, p.light());
    }

    private static void warehouse(StructureBuilder b, int x, int z, int w, int d, int h,
                                  int ground, Palette p, String name, boolean ruined) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;

        b.fill(x - 2, ground, z - 2, x1 + 2, ground, z1 + 2, p.foundation());
        b.room(x, y0, z, x1, y0 + h, z1);
        b.walls(x, y0 + 1, z, x1, y0 + h - 1, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 1, z1, p.foundation());
        b.walls(x, y0 + h - 1, z, x1, y0 + h - 1, z1, p.accent());

        // Tall loading doors on the quay side (east).
        for (int dz = z + 6; dz <= z1 - 6; dz += 14) {
            b.fill(x1, y0 + 1, dz, x1, y0 + 7, dz + 4, air());
            b.fill(x1, y0 + 8, dz, x1, y0 + 8, dz + 4, p.accent());
            b.fill(x1 - 1, y0 + 1, dz + 4, x1 - 1, y0 + 7, dz + 4, p.frame());
        }
        for (int wz = z + 8; wz <= z1 - 8; wz += 10) {
            b.window(x, y0 + 9, wz, 3, 1, true);
            b.window(x1, y0 + 10, wz, 3, 1, true);
        }
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, y0 + h, 7, 1);

        // The hoist beam and its chain.
        b.fill(x1 + 1, y0 + h + 4, z + d / 2, x1 + 5, y0 + h + 4, z + d / 2, p.accent());
        b.fill(x1 + 4, y0 + h - 2, z + d / 2, x1 + 4, y0 + h + 3, z + d / 2, p.rail());

        // A few crates and a chest inside.
        b.fill(x + 3, y0 + 1, z + 3, x + 5, y0 + 2, z + 5, p.foundation());
        b.fill(x + 7, y0 + 1, z + 3, x + 8, y0 + 1, z + 4, p.foundation());
        b.chest(x + 10, y0 + 1, z + 8, ruined ? SiteLoot.RELIC : SiteLoot.COMMON);

        if (ruined) {
            // Authored decay: a collapsed corner and boarded windows.
            for (int i = 0; i < 8; i++) {
                int hx = x + 2 + i * 5 % (w - 4);
                int hy = y0 + 2 + i * 3 % (h - 2);
                b.put(hx, hy, z1, air());
                b.put(x1, hy, z + 2 + i * 7 % (d - 4), air());
            }
            b.fill(x1 - 4, y0 + h, z1 - 4, x1, y0 + h + 2, z1, air());
            b.fill(x + 2, y0 + 4, z + 2, x + 3, y0 + 6, z + 2,
                    Blocks.DARK_OAK_TRAPDOOR.defaultBlockState());
        }

        signAt(b, x1 + 1, y0 + 8, z + d / 2 + 6, Direction.EAST, name);
        b.put(x + 2, y0 + 2, z + 2, p.light());
        b.put(x1 - 2, y0 + 2, z1 - 2, p.light());
    }

    private static void crane(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 2, ground, z - 2, x + 2, ground, z + 2, p.foundation());
        b.fill(x, ground + 1, z, x, ground + 18, z, p.accent());
        b.fill(x - 1, ground + 18, z, x + 10, ground + 18, z, p.accent());
        b.fill(x + 10, ground + 12, z, x + 10, ground + 17, z, p.rail());
        b.put(x + 10, ground + 11, z, p.light());
        b.fill(x - 2, ground + 18, z, x - 2, ground + 20, z, p.rail());
        // The winding house at the foot.
        b.fill(x - 3, ground + 1, z + 3, x + 1, ground + 4, z + 6, p.wall());
        b.fill(x - 2, ground + 5, z + 3, x, ground + 5, z + 6, p.roofSlab());
    }

    private static void railway(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Level the approach: the rails run dead straight on one authored level.
        for (int x = cx + 138; x <= cx + 322; x++) {
            int lo = Math.min(b.groundY(x, cz - 248),
                    Math.min(b.groundY(x, cz - 241), b.groundY(x, cz - 234)));
            int hi = Math.max(b.groundY(x, cz - 248),
                    Math.max(b.groundY(x, cz - 241), b.groundY(x, cz - 234)));
            if (hi > ground) {
                b.fill(x, ground + 1, cz - 248, x, hi, cz - 234, air());
            }
            if (lo < ground) {
                b.fill(x, lo + 1, cz - 248, x, ground, cz - 234, p.foundation());
            }
        }
        // Ballast, sleepers and two east-west rails.
        b.fill(cx + 140, ground, cz - 246, cx + 320, ground, cz - 236, p.rubble());
        for (int x = cx + 140; x <= cx + 320; x += 3) {
            b.fill(x, ground + 1, cz - 245, x + 1, ground + 1, cz - 237,
                    Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }
        BlockState rail = Blocks.RAIL.defaultBlockState()
                .setValue(BlockStateProperties.RAIL_SHAPE, RailShape.EAST_WEST);
        for (int x = cx + 140; x <= cx + 320; x++) {
            b.put(x, ground + 2, cz - 244, rail);
            b.put(x, ground + 2, cz - 238, rail);
        }

        // A real platform inside the goods shed, five wide and level with the rails.
        b.fill(cx + 150, ground + 1, cz - 249, cx + 190, ground + 1, cz - 245,
                p.foundation());
        b.fill(cx + 150, ground + 2, cz - 249, cx + 190, ground + 2, cz - 245,
                p.accent());
        for (int px = cx + 154; px <= cx + 186; px += 16) {
            b.put(px, ground + 3, cz - 247, p.light());
        }

        // The goods station: a long iron shed on the north side.
        int sx0 = cx + 146;
        int sx1 = cx + 196;
        int sz0 = cz - 262;
        int sz1 = cz - 230;
        int padLo = Math.min(b.groundY((sx0 + sx1) / 2, (sz0 + sz1) / 2),
                Math.min(b.groundY(sx0 - 2, sz0 - 2),
                        Math.min(b.groundY(sx1 + 2, sz0 - 2),
                                Math.min(b.groundY(sx0 - 2, sz1 + 2),
                                        b.groundY(sx1 + 2, sz1 + 2)))));
        int padHi = Math.max(b.groundY((sx0 + sx1) / 2, (sz0 + sz1) / 2),
                Math.max(b.groundY(sx0 - 2, sz0 - 2),
                        Math.max(b.groundY(sx1 + 2, sz0 - 2),
                                Math.max(b.groundY(sx0 - 2, sz1 + 2),
                                        b.groundY(sx1 + 2, sz1 + 2)))));
        if (padHi > ground + 2) {
            b.fill(sx0 - 2, ground + 3, sz0 - 2, sx1 + 2, padHi, sz1 + 2, air());
        }
        if (padLo < ground) {
            b.fill(sx0 - 2, padLo + 1, sz0 - 2, sx1 + 2, ground, sz1 + 2, p.foundation());
        }
        b.fill(sx0 - 2, ground, sz0 - 2, sx1 + 2, ground, sz1 + 2, p.foundation());
        for (int px = sx0; px <= sx1; px += 10) {
            b.fill(px, ground + 1, sz0, px, ground + 9, sz0, p.rail());
            b.fill(px, ground + 1, sz1, px, ground + 9, sz1, p.rail());
        }
        b.fill(sx0 - 1, ground + 10, sz0 - 1, sx1 + 1, ground + 10, sz1 + 1, p.roof());
        b.pitchedRoof(sx0 - 2, sz0 - 2, sx1 + 2, sz1 + 2, ground + 10, 4, 0);
        b.fill(sx0, ground + 1, sz1 - 4, sx1, ground + 1, sz1, p.accent());
        signAt(b, sx0 + 4, ground + 1, sz1 + 1, Direction.SOUTH, "GOODS DEPOT");
        signAt(b, sx1 - 4, ground + 1, sz1 + 1, Direction.SOUTH, "RAILWAY");
        b.put(sx0 + 2, ground + 9, sz0 + 2, p.light());
        b.put(sx1 - 2, ground + 9, sz0 + 2, p.light());

        // A shunting engine on the eastern length of track.
        int ex = cx + 262;
        b.fill(ex, ground + 2, cz - 245, ex + 10, ground + 6, cz - 237,
                Materials.concrete(DyeColor.RED));
        b.fill(ex + 2, ground + 7, cz - 243, ex + 8, ground + 7, cz - 239,
                Blocks.CONCRETE.pick(DyeColor.BLACK).defaultBlockState());
        b.fill(ex + 1, ground + 5, cz - 244, ex + 9, ground + 5, cz - 244,
                Blocks.GLASS_PANE.defaultBlockState());
        b.fill(ex + 1, ground + 5, cz - 238, ex + 9, ground + 5, cz - 238,
                Blocks.GLASS_PANE.defaultBlockState());
        b.put(ex + 10, ground + 4, cz - 241, p.light());
        b.fill(ex + 3, ground + 2, cz - 244, ex + 5, ground + 3, cz - 244,
                Blocks.CONCRETE.pick(DyeColor.BLACK).defaultBlockState());
        b.fill(ex + 3, ground + 2, cz - 238, ex + 5, ground + 3, cz - 238,
                Blocks.CONCRETE.pick(DyeColor.BLACK).defaultBlockState());
    }

    private static void dockPub(StructureBuilder b, int x, int z, int w, int d, int ground,
                                Palette p) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;

        b.fill(x - 1, ground, z - 1, x1 + 1, ground, z1 + 1, p.foundation());
        b.room(x, y0, z, x1, y0 + 9, z1);
        b.walls(x, y0 + 1, z, x1, y0 + 8, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 1, z1, p.foundation());
        b.walls(x, y0 + 5, z, x1, y0 + 5, z1, p.accent());
        for (int wz = z + 4; wz <= z1 - 4; wz += 5) {
            b.window(x, y0 + 3, wz, 3, 1, true);
            b.window(x1, y0 + 3, wz, 3, 1, true);
        }
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, y0 + 9, 4, 1);
        placeDoor(b, p, x, z, x1, z1, y0 + 1, Side.E, d / 2);

        // The tap room: a counter, barrels, a hearth and a few tables.
        b.fill(x + 2, y0 + 1, z + 4, x + 3, y0 + 2, z1 - 4, p.accent());
        b.put(x + 2, y0 + 3, z + 4, p.light());
        b.put(x + 5, y0 + 1, z + 4, Blocks.BARREL.defaultBlockState());
        b.put(x + 5, y0 + 1, z + 5, Blocks.BARREL.defaultBlockState());
        b.fill(x + 8, y0 + 1, z + 6, x + 10, y0 + 1, z + 7, p.foundation());
        b.put(x + 8, y0 + 2, z + 6, Blocks.CAMPFIRE.defaultBlockState());
        for (int tz = z + 12; tz <= z1 - 5; tz += 5) {
            b.put(x + 8, y0 + 1, tz, p.accent());
            b.put(x + 9, y0 + 2, tz, p.roofSlab());
        }
        signAt(b, x1 + 2, y0 + 4, z + d / 2, Direction.EAST, "DOCK PUB");
        signAt(b, x1 + 2, ground + 1, z + d / 2 + 3, Direction.EAST, "CURSED MUMMY");
        b.put(x + 1, y0 + 2, z + 1, p.light());
        b.put(x1 - 1, y0 + 2, z + 1, p.light());
    }

    // ------------------------------------------------------------------ East End

    private static void eastEnd(StructureBuilder b, int cx, int cz, Palette p,
                                IntBinaryOperator surface) {
        // Terraces north of lane A.
        int[] rowX = {176, 196, 216, 236, 256, 276, 296};
        for (int i = 0; i < rowX.length; i++) {
            int x = cx + rowX[i];
            int z = cz + 188;
            terrace(b, x, z, 16, 20, 6 + i % 3, surface.applyAsInt(x + 8, z + 10),
                    p, Side.S, false);
        }
        // South of lane A: the apothecary, then the market frontage.
        apothecary(b, cx + 172, cz + 222, 18, 18, surface.applyAsInt(cx + 181, cz + 231), p);
        terrace(b, cx + 196, cz + 222, 16, 18, 7,
                surface.applyAsInt(cx + 204, cz + 231), p, Side.N, false);
        market(b, cx + 216, cz + 222, 40, 18, surface.applyAsInt(cx + 236, cz + 231), p);
        terrace(b, cx + 260, cz + 222, 16, 18, 6,
                surface.applyAsInt(cx + 268, cz + 231), p, Side.N, false);
        terrace(b, cx + 280, cz + 222, 16, 18, 7,
                surface.applyAsInt(cx + 288, cz + 231), p, Side.N, false);
        terrace(b, cx + 300, cz + 222, 16, 18, 6,
                surface.applyAsInt(cx + 308, cz + 231), p, Side.N, true);

        // North of lane B.
        for (int i = 0; i < rowX.length; i++) {
            int x = cx + rowX[i];
            int z = cz + 244;
            terrace(b, x, z, 16, 16, 6 + (i + 1) % 3, surface.applyAsInt(x + 8, z + 8),
                    p, Side.S, i == 4);
        }
        // South of lane B: terraces and the chapel.
        terrace(b, cx + 176, cz + 270, 16, 20, 7,
                surface.applyAsInt(cx + 184, cz + 280), p, Side.N, false);
        terrace(b, cx + 196, cz + 270, 16, 20, 6,
                surface.applyAsInt(cx + 204, cz + 280), p, Side.N, false);
        terrace(b, cx + 216, cz + 270, 16, 20, 7,
                surface.applyAsInt(cx + 224, cz + 280), p, Side.N, true);
        terrace(b, cx + 236, cz + 270, 16, 20, 6,
                surface.applyAsInt(cx + 244, cz + 280), p, Side.N, false);
        chapel(b, cx + 252, cz + 270, 40, 30, surface.applyAsInt(cx + 272, cz + 285), p);
        terrace(b, cx + 296, cz + 270, 16, 20, 6,
                surface.applyAsInt(cx + 304, cz + 280), p, Side.N, false);

        // The high street's west side.
        int[] westZ = {188, 222, 244, 270};
        for (int i = 0; i < westZ.length; i++) {
            int z = cz + westZ[i];
            terrace(b, cx + 144, z, 18, 18, 6 + i % 3, surface.applyAsInt(cx + 153, z + 9),
                    p, Side.E, false);
        }
        // A corner pub at the south end of the high street.
        int pubY = surface.applyAsInt(cx + 153, cz + 306);
        terrace(b, cx + 144, cz + 296, 18, 20, 8, pubY, p, Side.E, false);
        signAt(b, cx + 162, pubY + 4, cz + 306, Direction.EAST, "THE GLOBE");
        b.put(cx + 146, pubY + 1, cz + 300, Blocks.BARREL.defaultBlockState());
        b.put(cx + 146, pubY + 1, cz + 301, Blocks.BARREL.defaultBlockState());

        // Street dressing: washing lines, crates and a bombed gap.
        int[] washZ = {194, 206, 230, 250, 278, 298};
        for (int z : washZ) {
            int wy = surface.applyAsInt(cx + 167, cz + z);
            b.put(cx + 162, wy + 4, cz + z, p.rail());
            b.put(cx + 173, wy + 4, cz + z, p.rail());
            b.fill(cx + 162, wy + 4, cz + z, cx + 173, wy + 4, cz + z,
                    Materials.wool(DyeColor.WHITE));
        }
        for (int i = 0; i < 10; i++) {
            int bx = cx + 180 + i % 5 * 8;
            int bz = cz + 296 + i / 5 * 4;
            b.put(bx, surface.applyAsInt(bx, bz) + 1, bz, p.foundation());
        }

        signAt(b, cx + 164, surface.applyAsInt(cx + 164, cz + 180) + 1, cz + 180,
                Direction.SOUTH, "EAST END");
        signAt(b, cx + 200, surface.applyAsInt(cx + 200, cz + 218) + 1, cz + 218,
                Direction.SOUTH, "MARKET");
        signAt(b, cx + 250, surface.applyAsInt(cx + 250, cz + 266) + 1, cz + 266,
                Direction.SOUTH, "CHAPEL");
        signAt(b, cx + 172, surface.applyAsInt(cx + 172, cz + 220) + 1, cz + 220,
                Direction.NORTH, "APOTHECARY");
        signAt(b, cx + 220, surface.applyAsInt(cx + 220, cz + 222) + 1, cz + 222,
                Direction.SOUTH, "MISSING EXPED.");
        signAt(b, cx + 280, surface.applyAsInt(cx + 280, cz + 218) + 1, cz + 218,
                Direction.SOUTH, "EAST END LANE");
    }

    private static void terrace(StructureBuilder b, int x, int z, int w, int d, int h,
                                int ground, Palette p, Side door, boolean bombed) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;

        b.fill(x, ground, z, x1, ground, z1, p.foundation());
        b.room(x, y0, z, x1, y0 + h, z1);
        b.walls(x, y0 + 1, z, x1, y0 + h - 1, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 1, z1, p.foundation());
        for (int y = y0 + 1; y <= y0 + h; y++) {
            b.put(x, y, z, p.accent());
            b.put(x1, y, z, p.accent());
            b.put(x, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
        for (int wy = y0 + 3; wy <= y0 + h - 2; wy += 3) {
            for (int wx = x + 3; wx <= x1 - 3; wx += 4) {
                b.window(wx, wy, z, 2, 2, true);
                b.window(wx, wy, z1, 2, 2, true);
            }
        }
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, y0 + h, 4, w >= d ? 0 : 1);
        b.fill(x + 2, y0 + h + 2, z + 2, x + 2, y0 + h + 5, z + 2, p.wall());
        b.put(x + 2, y0 + h + 6, z + 2, p.accent());
        placeDoor(b, p, x, z, x1, z1, y0 + 1, door, w / 2);
        b.put(x + 1, y0 + 2, z1, p.light());

        if (bombed) {
            // Authored bomb damage: a broken roofline and punched walls.
            for (int i = 0; i < 6; i++) {
                int hx = x + 2 + i * 3 % (w - 4);
                int hy = y0 + 2 + i * 2 % (h - 1);
                b.put(hx, hy, z1, air());
                b.put(hx, hy, z, air());
            }
            b.fill(x + w / 2 - 2, y0 + h, z + d / 2 - 2, x + w / 2 + 2, y0 + h + 1,
                    z + d / 2 + 2, air());
            b.fill(x + 1, y0 + 1, z1, x + 2, y0 + 2, z1,
                    Blocks.DARK_OAK_TRAPDOOR.defaultBlockState());
            signAt(b, x + w / 2, ground + 1, z1 + 1, Direction.SOUTH, "KEEP OUT");
        }
    }

    private static void market(StructureBuilder b, int x, int z, int w, int d, int ground,
                               Palette p) {
        // A paved market square with six stalls under striped awnings.
        pave(b, x, z, x + w - 1, z + d - 1, ground, p.foundation());
        for (int i = 0; i < 6; i++) {
            int sx = x + 3 + i % 3 * 12;
            int sz = z + 3 + i / 3 * 9;
            stall(b, sx, sz, ground, p, (i & 1) == 0);
        }
        b.monument(x + w / 2, z + d / 2, ground + 1);
        signAt(b, x + w / 2, ground + 1, z + 1, Direction.NORTH, "EAST END MARKET");
        signAt(b, x + 2, ground + 1, z + d - 1, Direction.SOUTH, "MARKET");
        b.put(x + 1, ground + 2, z + 1, p.light());
        b.put(x + w - 2, ground + 2, z + d - 2, p.light());
    }

    private static void stall(StructureBuilder b, int x, int z, int ground, Palette p,
                              boolean red) {
        BlockState cloth = Materials.wool(red ? DyeColor.RED : DyeColor.WHITE);
        b.put(x, ground, z, p.foundation());
        b.put(x + 2, ground, z, p.foundation());
        b.put(x, ground, z + 2, p.foundation());
        b.put(x + 2, ground, z + 2, p.foundation());
        for (int y = ground + 1; y <= ground + 2; y++) {
            b.put(x, y, z, p.accent());
            b.put(x + 2, y, z, p.accent());
            b.put(x, y, z + 2, p.accent());
            b.put(x + 2, y, z + 2, p.accent());
        }
        b.fill(x, ground + 3, z, x + 2, ground + 3, z + 2, p.roofSlab());
        b.fill(x, ground + 4, z, x + 2, ground + 4, z + 2, cloth);
        b.fill(x + 1, ground + 1, z, x + 1, ground + 1, z + 1, p.foundation());
        b.put(x + 1, ground + 2, z + 1, p.light());
    }

    private static void chapel(StructureBuilder b, int x, int z, int w, int d, int ground,
                               Palette p) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;

        b.fill(x - 2, ground, z - 2, x1 + 2, ground, z1 + 2, p.foundation());
        b.room(x, y0, z, x1, y0 + 12, z1);
        b.walls(x, y0 + 1, z, x1, y0 + 11, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 1, z1, p.foundation());
        b.walls(x, y0 + 8, z, x1, y0 + 8, z1, p.accent());
        for (int wz = z + 5; wz <= z1 - 5; wz += 8) {
            b.window(x, y0 + 4, wz, 6, 1, true);
            b.window(x1, y0 + 4, wz, 6, 1, true);
        }
        b.pitchedRoof(x - 2, z - 2, x1 + 2, z1 + 2, y0 + 12, 8, 0);
        // A bellcote and cross at the west end.
        b.fill(x + w / 2 - 2, y0 + 15, z - 1, x + w / 2 + 2, y0 + 19, z + 1, p.wall());
        b.fill(x + w / 2 - 1, y0 + 16, z, x + w / 2 + 1, y0 + 18, z, p.light());
        b.fill(x + w / 2, y0 + 20, z, x + w / 2, y0 + 23, z, Blocks.GOLD_BLOCK.defaultBlockState());
        b.fill(x + w / 2 - 1, y0 + 22, z, x + w / 2 + 1, y0 + 22, z,
                Blocks.GOLD_BLOCK.defaultBlockState());

        placeDoor(b, p, x, z, x1, z1, y0 + 1, Side.N, w / 2);
        // Pews and a plain altar.
        for (int pz = z + 6; pz <= z1 - 8; pz += 4) {
            b.fill(x + 5, y0 + 1, pz, x1 - 5, y0 + 1, pz, p.accent());
            b.fill(x + 5, y0 + 2, pz, x1 - 5, y0 + 2, pz, p.roofSlab());
        }
        b.fill(x + w / 2 - 3, y0 + 1, z1 - 4, x + w / 2 + 3, y0 + 2, z1 - 2, p.foundation());
        b.put(x + w / 2, y0 + 3, z1 - 3, p.light());
        signAt(b, x + w / 2, y0 + 4, z1 + 1, Direction.SOUTH, "CHAPEL");
        signAt(b, x + w / 2, ground + 1, z - 3, Direction.NORTH, "EAST END CHAPEL");
        b.put(x + 2, y0 + 2, z + 2, p.light());
        b.put(x1 - 2, y0 + 2, z + 2, p.light());
    }

    private static void apothecary(StructureBuilder b, int x, int z, int w, int d, int ground,
                                   Palette p) {
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int y0 = ground + 1;

        b.fill(x, ground, z, x1, ground, z1, p.foundation());
        b.room(x, y0, z, x1, y0 + 8, z1);
        b.walls(x, y0 + 1, z, x1, y0 + 7, z1, p.wall());
        b.walls(x, y0 + 1, z, x1, y0 + 1, z1, p.accent());
        for (int wx = x + 3; wx <= x1 - 3; wx += 4) {
            b.window(wx, y0 + 2, z, 4, 2, false);
            b.window(wx, y0 + 2, z1, 4, 2, false);
        }
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, y0 + 8, 4, 0);
        placeDoor(b, p, x, z, x1, z1, y0 + 1, Side.N, w / 2);

        // Shelves of remedies, a counter and a chest of supplies.
        for (int sz = z + 4; sz <= z1 - 4; sz += 3) {
            b.fill(x + 1, y0 + 2, sz, x + 1, y0 + 5, sz, Blocks.BOOKSHELF.defaultBlockState());
            b.fill(x1 - 1, y0 + 2, sz, x1 - 1, y0 + 5, sz,
                    Blocks.BOOKSHELF.defaultBlockState());
        }
        b.fill(x + 3, y0 + 1, z + 6, x1 - 3, y0 + 2, z + 7, p.accent());
        b.put(x + 4, y0 + 3, z + 6, p.light());
        b.put(x1 - 4, y0 + 3, z + 6, p.light());
        b.chest(x1 - 2, y0 + 1, z + 10, SiteLoot.ORDER);
        signAt(b, x + w / 2, y0 + 4, z - 1, Direction.NORTH, "APOTHECARY");
        signAt(b, x + w / 2, ground + 1, z + d + 1, Direction.SOUTH, "REMEDIES");
    }

    // ------------------------------------------------------------------ Underground

    private static void underground(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x = cx;
        int z = cz;
        int floorY = ground - 9;
        int headY = ground - 4;

        ticketHall(b, x, z + 82, ground, p);

        // The north-south tunnel begins under the hall and runs south through the junction.
        tunnel(b, x - 5, z + 93, x + 5, z + 134, floorY, headY, p);

        // The stair shaft: an open well inside the hall, descending south to the tunnel floor.
        b.fill(x - 4, ground - 10, z + 84, x + 4, ground + 2, z + 94, air());
        for (int i = 0; i <= 9; i++) {
            int sz = z + 84 + i;
            int sy = ground - i;
            b.fill(x - 3, sy, sz, x + 3, sy, sz, p.foundation());
        }
        b.fill(x - 4, floorY, z + 84, x - 4, ground + 1, z + 94, p.wall());
        b.fill(x + 4, floorY, z + 84, x + 4, ground + 1, z + 94, p.wall());
        b.fill(x - 4, floorY, z + 94, x + 4, floorY, z + 94, p.foundation());
        // Open the tunnel's north wall at the foot of the stairs.
        b.fill(x - 4, floorY + 1, z + 93, x + 4, headY - 1, z + 93, air());
        // Iron handrails down both sides of the open well.
        b.fill(x - 5, ground + 1, z + 84, x - 5, ground + 2, z + 94, p.rail());
        b.fill(x + 5, ground + 1, z + 84, x + 5, ground + 2, z + 94, p.rail());
        signAt(b, x, ground + 4, z + 82, Direction.SOUTH, "STAIRS DOWN");

        // The tunnels: a long east-west line and the north-south line through the junction.
        tunnel(b, x - 110, z + 114, x + 110, z + 122, floorY, headY, p);
        // Widen the crossing into the junction chamber.
        b.fill(x - 12, floorY, z + 112, x + 12, headY, z + 124, air());
        b.fill(x - 12, floorY, z + 112, x + 12, floorY, z + 124, p.foundation());
        b.fill(x - 12, headY, z + 112, x + 12, headY, z + 124, p.roof());
        b.walls(x - 12, floorY + 1, z + 112, x + 12, headY - 1, z + 124, p.wall());
        b.fill(x - 12, floorY + 1, z + 114, x - 12, headY - 1, z + 122, air());
        b.fill(x + 12, floorY + 1, z + 114, x + 12, headY - 1, z + 122, air());
        b.fill(x - 2, floorY + 1, z + 112, x + 2, headY - 1, z + 112, air());
        b.fill(x - 2, floorY + 1, z + 124, x + 2, headY - 1, z + 124, air());

        secretJunction(b, x, z, floorY, headY, p);

        // Tunnel lighting and warnings.
        for (int tx = -100; tx <= 100; tx += 20) {
            b.put(x + tx, headY - 1, z + 118, p.light());
        }
        for (int tz = 90; tz <= 130; tz += 12) {
            b.put(x, headY - 1, z + tz, p.light());
        }
        signAt(b, x + 20, headY - 1, z + 114, Direction.SOUTH, "MIND THE GAP");
        signAt(b, x - 40, headY - 1, z + 114, Direction.SOUTH, "NO ENTRY");
        signAt(b, x, headY - 1, z + 112, Direction.SOUTH, "HIDDEN ROOM");
        // A sealed north branch off the east-west tunnel, ending in rubble.
        b.fill(x - 40, floorY, z + 100, x - 32, headY, z + 114, air());
        b.fill(x - 40, floorY, z + 100, x - 32, floorY, z + 114, p.foundation());
        b.fill(x - 40, headY, z + 100, x - 32, headY, z + 114, p.roof());
        b.walls(x - 40, floorY + 1, z + 100, x - 32, headY - 1, z + 114, p.wall());
        b.fill(x - 39, floorY + 1, z + 114, x - 33, headY - 1, z + 114, air());
        b.fill(x - 38, floorY + 1, z + 100, x - 34, headY - 1, z + 100,
                Blocks.COBBLESTONE.defaultBlockState());
    }

    private static void ticketHall(StructureBuilder b, int x, int z, int ground, Palette p) {
        int x0 = x - 12;
        int x1 = x + 12;
        int z0 = z;
        int z1 = z + 14;
        int y0 = ground + 1;

        b.fill(x0 - 1, ground, z0 - 1, x1 + 1, ground, z1 + 1, p.foundation());
        b.walls(x0, y0, z0, x1, y0 + 10, z1, p.wall());
        b.walls(x0, y0, z0, x1, y0, z1, p.accent());
        b.walls(x0, y0 + 10, z0, x1, y0 + 10, z1, p.accent());
        b.fill(x0, y0 + 11, z0, x1, y0 + 11, z1, p.roof());
        b.fill(x0 + 1, y0 + 1, z0 + 1, x1 - 1, y0 + 9, z1 - 1, air());
        b.fill(x0, y0 + 1, z0, x1, y0 + 1, z1, p.foundation());
        // The stair opening through the floor and the glass lantern above it.
        b.fill(x - 3, y0, z0 + 2, x + 3, y0 + 10, z0 + 12, air());
        b.fill(x - 3, y0 + 11, z0 + 2, x + 3, y0 + 11, z0 + 12,
                Materials.stainedGlass(DyeColor.WHITE));
        // Doorway on the south face.
        b.fill(x - 2, y0 + 1, z1, x + 2, y0 + 3, z1, air());
        signAt(b, x, y0 + 5, z1 + 1, Direction.SOUTH, "UNDERGROUND");
        signAt(b, x - 8, y0 + 4, z1 + 1, Direction.SOUTH, "TICKET HALL");
        signAt(b, x + 8, y0 + 4, z1 + 1, Direction.SOUTH, "MIND THE GAP");
        b.put(x0 + 2, y0 + 3, z0 + 2, p.light());
        b.put(x1 - 2, y0 + 3, z0 + 2, p.light());
        // A tiled dado and ticket windows inside.
        b.walls(x0 + 1, y0 + 3, z0 + 1, x1 - 1, y0 + 3, z1 - 1, p.accent());
        b.fill(x0 + 2, y0 + 2, z0 + 1, x0 + 6, y0 + 3, z0 + 1, p.frame());
        b.fill(x1 - 6, y0 + 2, z0 + 1, x1 - 2, y0 + 3, z0 + 1, p.frame());
    }

    private static void tunnel(StructureBuilder b, int x0, int z0, int x1, int z1,
                               int floorY, int headY, Palette p) {
        b.fill(x0, floorY, z0, x1, floorY, z1, p.foundation());
        b.fill(x0, headY, z0, x1, headY, z1, p.roof());
        b.walls(x0, floorY + 1, z0, x1, headY - 1, z1, p.wall());
        b.fill(x0 + 1, floorY + 1, z0 + 1, x1 - 1, headY - 1, z1 - 1, air());
        // Brick pilasters every twelve blocks.
        if (x1 - x0 >= z1 - z0) {
            for (int px = x0 + 4; px <= x1 - 4; px += 12) {
                b.fill(px, floorY + 1, z0, px, headY - 1, z0, p.weathered());
                b.fill(px, floorY + 1, z1, px, headY - 1, z1, p.weathered());
            }
        } else {
            for (int pz = z0 + 4; pz <= z1 - 4; pz += 12) {
                b.fill(x0, floorY + 1, pz, x0, headY - 1, pz, p.weathered());
                b.fill(x1, floorY + 1, pz, x1, headY - 1, pz, p.weathered());
            }
        }
    }

    private static void secretJunction(StructureBuilder b, int x, int z, int floorY, int headY,
                                       Palette p) {
        int z0 = z + 124;
        int z1 = z + 134;
        int x0 = x - 10;
        int x1 = x + 10;

        b.fill(x0, floorY, z0, x1, headY, z1, air());
        b.fill(x0, floorY, z0, x1, floorY, z1, p.foundation());
        b.fill(x0, headY, z0, x1, headY, z1, p.roof());
        b.walls(x0, floorY + 1, z0, x1, headY - 1, z1, p.wall());
        b.fill(x - 2, floorY + 1, z0, x + 2, headY - 1, z0, air());
        b.fill(x - 2, floorY + 1, z1, x + 2, headY - 1, z1, p.wall());

        // An altar of black stone, two chests and the lore.
        b.fill(x - 3, floorY + 1, z1 - 4, x + 3, floorY + 1, z1 - 2,
                Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        b.fill(x - 1, floorY + 2, z1 - 3, x + 1, floorY + 2, z1 - 3,
                Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        b.put(x, floorY + 3, z1 - 3, Blocks.GOLD_BLOCK.defaultBlockState());
        b.chest(x - 8, floorY + 1, z1 - 1, SiteLoot.RELIC);
        b.chest(x + 8, floorY + 1, z1 - 1, SiteLoot.CULT);
        b.chest(x, floorY + 1, z0 + 2, SiteLoot.COMMON);
        signAt(b, x, floorY + 3, z1, Direction.NORTH, "THE VEIL BELOW", "SEALED 1888");
        signAt(b, x - 8, floorY + 2, z0 + 1, Direction.SOUTH, "THEY CAME BACK");
        b.put(x0 + 1, headY - 1, z0 + 1, Palette.cobweb());
        b.put(x1 - 1, headY - 1, z0 + 1, Palette.cobweb());
        b.put(x0 + 1, headY - 1, z1 - 1, Palette.cobweb());
        b.put(x1 - 1, headY - 1, z1 - 1, Palette.cobweb());
        b.put(x0 + 1, floorY + 2, z0 + 1, p.light());
        b.put(x1 - 1, floorY + 2, z1 - 1, p.light());
    }

    // ------------------------------------------------------------------ dressing

    private static void dressing(StructureBuilder b, int cx, int cz, Palette p,
                                 IntBinaryOperator surface) {
        // Gas lamps down the avenue, at authored intervals.
        int[] lampZ = {-140, -80, -60, -40, 0, 20, 40, 70, 180, 210, 250, 290};
        for (int z : lampZ) {
            lanternPost(b, cx - 10, cz + z, surface.applyAsInt(cx - 10, cz + z), p);
            lanternPost(b, cx + 10, cz + z, surface.applyAsInt(cx + 10, cz + z), p);
        }
        // Lamps on the West End streets and the museum street.
        for (int dx = -120; dx <= 120; dx += 30) {
            lanternPost(b, cx + dx, cz + 43, surface.applyAsInt(cx + dx, cz + 43), p);
            lanternPost(b, cx + dx, cz + 112, surface.applyAsInt(cx + dx, cz + 112), p);
        }
        for (int dx = -300; dx <= -150; dx += 30) {
            lanternPost(b, cx + dx, cz + 102, surface.applyAsInt(cx + dx, cz + 102), p);
        }

        // Trees in the museum forecourt and the station forecourt.
        tree(b, cx - 280, cz + 70, surface.applyAsInt(cx - 280, cz + 70), p);
        tree(b, cx - 200, cz + 70, surface.applyAsInt(cx - 200, cz + 70), p);
        tree(b, cx - 280, cz + 86, surface.applyAsInt(cx - 280, cz + 86), p);
        tree(b, cx - 200, cz + 86, surface.applyAsInt(cx - 200, cz + 86), p);
        tree(b, cx - 20, cz + 84, surface.applyAsInt(cx - 20, cz + 84), p);
        tree(b, cx + 20, cz + 84, surface.applyAsInt(cx + 20, cz + 84), p);

        // Two red buses and a coal cart.
        redBus(b, cx + 9, cz + 272, surface.applyAsInt(cx + 9, cz + 272), false);
        redBus(b, cx + 50, cz + 45, surface.applyAsInt(cx + 50, cz + 45), true);
        coalCart(b, cx + 230, cz - 30, surface.applyAsInt(cx + 230, cz - 30), p);

        // Statues and a small green in the City.
        b.monument(cx + 20, cz - 140, surface.applyAsInt(cx + 20, cz - 140) + 1);
        tree(b, cx - 20, cz - 140, surface.applyAsInt(cx - 20, cz - 140), p);
        tree(b, cx + 30, cz - 140, surface.applyAsInt(cx + 30, cz - 140), p);

        // Benches along the quay and the station square.
        for (int z = -80; z <= 120; z += 40) {
            bench(b, cx + 256, cz + z, surface.applyAsInt(cx + 256, cz + z), p);
        }
        bench(b, cx - 18, cz + 100, surface.applyAsInt(cx - 18, cz + 100), p);
        bench(b, cx + 18, cz + 100, surface.applyAsInt(cx + 18, cz + 100), p);

        // A few crates and a post box at the arrival gate.
        int gateY = surface.applyAsInt(cx, cz + 310);
        b.put(cx + 18, gateY + 1, cz + 310, p.foundation());
        b.put(cx + 18, gateY + 2, cz + 310, p.foundation());
        b.put(cx - 18, gateY + 1, cz + 310, Materials.concrete(DyeColor.RED));
        b.put(cx - 18, gateY + 2, cz + 310, Materials.concrete(DyeColor.RED));
        b.put(cx - 18, gateY + 3, cz + 310, p.accent());
    }

    private static void lanternPost(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground, z, p.foundation());
        b.fill(x, ground + 1, z, x, ground + 3, z, p.accent());
        b.put(x, ground + 4, z, p.light());
    }

    private static void tree(StructureBuilder b, int x, int z, int ground, Palette p) {
        int h = 4 + Math.abs((x * 3 + z) % 3);
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState leaf = Blocks.OAK_LEAVES.defaultBlockState();
        b.put(x, ground, z, p.ground());
        b.fill(x, ground + 1, z, x, ground + h, z, log);
        b.fill(x - 2, ground + h - 1, z - 2, x + 2, ground + h + 1, z + 2, leaf);
        b.fill(x - 1, ground + h + 2, z - 1, x + 1, ground + h + 2, z + 1, leaf);
        b.put(x, ground + h + 3, z, leaf);
    }

    private static void redBus(StructureBuilder b, int x, int z, int ground, boolean alongX) {
        BlockState red = Materials.concrete(DyeColor.RED);
        BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
        BlockState tyre = Blocks.CONCRETE.pick(DyeColor.BLACK).defaultBlockState();
        int x1 = alongX ? x + 9 : x + 3;
        int z1 = alongX ? z + 3 : z + 9;
        b.fill(x, ground + 1, z, x1, ground + 5, z1, red);
        if (alongX) {
            b.fill(x + 2, ground + 3, z, x1 - 2, ground + 4, z, glass);
            b.fill(x + 2, ground + 3, z1, x1 - 2, ground + 4, z1, glass);
            b.put(x, ground + 1, z + 1, tyre);
            b.put(x1, ground + 1, z + 1, tyre);
            b.put(x, ground + 1, z1 - 1, tyre);
            b.put(x1, ground + 1, z1 - 1, tyre);
        } else {
            b.fill(x, ground + 3, z + 2, x, ground + 4, z1 - 2, glass);
            b.fill(x1, ground + 3, z + 2, x1, ground + 4, z1 - 2, glass);
            b.put(x + 1, ground + 1, z, tyre);
            b.put(x + 1, ground + 1, z1, tyre);
            b.put(x1 - 1, ground + 1, z, tyre);
            b.put(x1 - 1, ground + 1, z1, tyre);
        }
        b.put(alongX ? x1 - 1 : x + 1, ground + 6, z + 1,
                Blocks.LANTERN.defaultBlockState());
    }

    private static void coalCart(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x, ground + 1, z, x + 4, ground + 2, z + 2, p.accent());
        b.fill(x + 1, ground + 3, z, x + 3, ground + 3, z + 2,
                Blocks.COAL_BLOCK.defaultBlockState());
        b.put(x, ground + 1, z, Blocks.CONCRETE.pick(DyeColor.BLACK).defaultBlockState());
        b.put(x + 4, ground + 1, z, Blocks.CONCRETE.pick(DyeColor.BLACK).defaultBlockState());
        b.put(x, ground + 1, z + 2, Blocks.CONCRETE.pick(DyeColor.BLACK).defaultBlockState());
        b.put(x + 4, ground + 1, z + 2, Blocks.CONCRETE.pick(DyeColor.BLACK).defaultBlockState());
        b.put(x - 1, ground + 1, z + 1, p.rail());
    }

    private static void bench(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground + 1, z, p.roofSlab());
        b.put(x + 1, ground + 1, z, p.roofSlab());
        b.put(x, ground + 2, z + 1, p.accent());
        b.put(x + 1, ground + 2, z + 1, p.accent());
    }

    private static void fountain(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 4, ground, z - 4, x + 4, ground, z + 4, p.foundation());
        b.walls(x - 4, ground + 1, z - 4, x + 4, ground + 2, z + 4, p.foundation());
        b.fill(x - 3, ground + 1, z - 3, x + 3, ground + 1, z + 3,
                Blocks.WATER.defaultBlockState());
        b.fill(x, ground + 1, z, x, ground + 3, z, p.accent());
        b.put(x, ground + 4, z, p.light());
    }

    private static void railing(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                                Palette p) {
        b.walls(x0, y, z0, x1, y, z1, p.rail());
    }

    // ------------------------------------------------------------------ small helpers

    private static void placeDoor(StructureBuilder b, Palette p, int x0, int z0, int x1, int z1,
                                  int y, Side side, int offset) {
        int dx;
        int dz;
        Direction facing;
        switch (side) {
            case N -> {
                dx = x0 + Math.clamp(offset, 1, Math.max(1, x1 - x0 - 1));
                dz = z0;
                facing = Direction.NORTH;
            }
            case S -> {
                dx = x0 + Math.clamp(offset, 1, Math.max(1, x1 - x0 - 1));
                dz = z1;
                facing = Direction.SOUTH;
            }
            case W -> {
                dx = x0;
                dz = z0 + Math.clamp(offset, 1, Math.max(1, z1 - z0 - 1));
                facing = Direction.WEST;
            }
            default -> {
                dx = x1;
                dz = z0 + Math.clamp(offset, 1, Math.max(1, z1 - z0 - 1));
                facing = Direction.EAST;
            }
        }
        b.put(dx, y, dz, air());
        b.put(dx, y + 1, dz, air());
        BlockState lower = p.door()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        b.put(dx, y, dz, lower);
        b.put(dx, y + 1, dz,
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

    private static void signAt(StructureBuilder b, int x, int y, int z, Direction facing,
                               String... lines) {
        b.put(x, y - 1, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        CityPlanSupport.sign(b, x, y, z, facing, lines);
    }

    private static BlockState air() {
        return Blocks.AIR.defaultBlockState();
    }
}
