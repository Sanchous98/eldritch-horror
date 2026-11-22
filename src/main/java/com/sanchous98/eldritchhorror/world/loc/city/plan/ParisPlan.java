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
 * Paris between the wars: Notre-Dame on its island, the Louvre and its gardens, Montmartre's
 * hill and dance halls, the Seine with its quays and booksellers, the grands boulevards, and the
 * catacombs beneath the south bank.
 *
 * <p>Everything here is authored: absolute offsets from {@code (cx, cz)}, the city's {@code ground}
 * level, and explicit arrays and loops. There is no {@code RandomSource}, no layout hash and no
 * chunk-local read. {@link StructureBuilder} clips every write to the generating chunk, so the
 * same call builds the same city slice in every chunk. Streets and water are laid first, then the
 * districts, so no facade can overwrite a road, a quay or a bridge.
 */
public final class ParisPlan implements CityPlan {

    /** Authored half-extent (blocks). */
    private static final int HALF = 340;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState LIMESTONE = Blocks.CALCITE.defaultBlockState();
    private static final BlockState QUARTZ = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState SLATE = Blocks.DEEPSLATE_TILES.defaultBlockState();
    private static final BlockState BONE = Blocks.BONE_BLOCK.defaultBlockState();
    private static final BlockState SKULL = Blocks.SKELETON_SKULL.defaultBlockState();
    private static final BlockState BOOKS = Blocks.BOOKSHELF.defaultBlockState();
    private static final BlockState GREEN = Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN).defaultBlockState();
    private static final BlockState RED = Blocks.DYED_TERRACOTTA.pick(DyeColor.RED).defaultBlockState();
    private static final BlockState WHITE = Blocks.DYED_TERRACOTTA.pick(DyeColor.WHITE).defaultBlockState();
    private static final BlockState DARK_WOOD = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private static final BlockState DARK_SLAB = Blocks.DARK_OAK_SLAB.defaultBlockState();
    private static final BlockState GOLD = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final BlockState GLASS = Blocks.GLASS.defaultBlockState();
    private static final BlockState STAINED_BLUE = Blocks.STAINED_GLASS_PANE.pick(DyeColor.BLUE).defaultBlockState();
    private static final BlockState STAINED_RED = Blocks.STAINED_GLASS_PANE.pick(DyeColor.RED).defaultBlockState();
    private static final BlockState STAINED_GOLD = Blocks.STAINED_GLASS_PANE.pick(DyeColor.YELLOW).defaultBlockState();
    private static final BlockState STAINED_PURPLE = Blocks.STAINED_GLASS_PANE.pick(DyeColor.PURPLE).defaultBlockState();
    private static final BlockState HEDGE = Blocks.OAK_LEAVES.defaultBlockState()
            .setValue(BlockStateProperties.PERSISTENT, true);
    private static final BlockState PLANE_LOG = Blocks.OAK_LOG.defaultBlockState();
    private static final BlockState PLANE_LEAVES = Blocks.OAK_LEAVES.defaultBlockState()
            .setValue(BlockStateProperties.PERSISTENT, true);
    private static final BlockState POPPY = Blocks.POPPY.defaultBlockState();
    private static final BlockState DANDELION = Blocks.DANDELION.defaultBlockState();
    private static final BlockState SOUL = Blocks.SOUL_LANTERN.defaultBlockState();
    private static final BlockState CLOCK = Blocks.TARGET.defaultBlockState();
    private static final BlockState RAIL = Blocks.RAIL.defaultBlockState();

    @Override
    public String cityId() {
        return "paris";
    }

    @Override
    public int halfExtent() {
        return HALF;
    }

    @Override
    public void build(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        // Fallback for callers that only know the flat-heart level. The generator calls the
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

        // 1. Streets and water first, so no facade can overwrite a road, quay or bridge.
        streets(b, cx, cz, s, p);
        river(b, cx, cz, s, p);
        quays(b, cx, cz, s, p);

        // 2. The districts. The island and cathedral sit in the flat heart; the rim districts
        //    follow the graded surface through `s`.
        arrival(b, cx, cz, s, p);
        station(b, cx, cz, s, p);
        island(b, cx, cz, ground, p);
        notreDame(b, cx, cz, ground, p);
        louvre(b, cx, cz, s, p);
        tuileries(b, cx, cz, s, p);
        montmartre(b, cx, cz, s, p);
        boulevards(b, cx, cz, s, p);
        catacombs(b, cx, cz, ground, p);

        // 3. Trees, then the readable notices on top of the built fabric.
        greenery(b, cx, cz, s, p);
        signs(b, cx, cz, s, p);

        b.marker("paris_center", cx, ground + 1, cz);
    }

    @Override
    public List<CityPoi> pois() {
        return List.of(
                // Arrival and notices.
                new CityPoi("arrival_gate", CityPoi.Kind.SIGN, 8, 1, 306, "districts"),
                new CityPoi("city_notices", CityPoi.Kind.SIGN, -24, 1, 306, "quests"),
                new CityPoi("gare_du_nord", CityPoi.Kind.STATION, 56, 1, 300, "rail"),
                // Landmarks.
                new CityPoi("notre_dame", CityPoi.Kind.LANDMARK, 0, 0, -200, "cathedral"),
                new CityPoi("seine_quay", CityPoi.Kind.LANDMARK, 0, 0, -64, "booksellers"),
                new CityPoi("louvre_palace", CityPoi.Kind.LANDMARK, -255, 0, -135, "louvre"),
                new CityPoi("montmartre_church", CityPoi.Kind.LANDMARK, -240, 11, 296,
                        "sacre-coeur"),
                // Services.
                new CityPoi("boulevard_quest_board", CityPoi.Kind.QUEST_BOARD, 220, 0, -8,
                        "paris_stolen_painting;paris_missing_dancer"),
                new CityPoi("hotel_de_ville", CityPoi.Kind.INN, 226, 0, -120, "hotel_keeper"),
                new CityPoi("louvre_shop", CityPoi.Kind.SHOP, -234, 0, -62, "shopkeeper"),
                new CityPoi("apothecary", CityPoi.Kind.HEALER, -234, 0, 120, "apothecary"),
                // Quest givers and witnesses.
                new CityPoi("louvre_curator", CityPoi.Kind.NPC, -232, 0, 0,
                        "paris_stolen_painting"),
                new CityPoi("dance_hall", CityPoi.Kind.NPC, -240, 0, 220,
                        "paris_missing_dancer"),
                new CityPoi("dance_hall_owner", CityPoi.Kind.NPC, -240, 0, 212, "owner"),
                new CityPoi("hotel_keeper", CityPoi.Kind.NPC, 230, 0, -120, "hotel_keeper"),
                new CityPoi("shopkeeper", CityPoi.Kind.NPC, -236, 0, -64, "shopkeeper"),
                new CityPoi("apothecary_keeper", CityPoi.Kind.NPC, -236, 0, 120, "apothecary"),
                new CityPoi("gendarme", CityPoi.Kind.NPC, 220, 0, 26, "gendarme"),
                new CityPoi("bookseller", CityPoi.Kind.NPC, 24, 0, -64, "bookseller"),
                // The secret.
                new CityPoi("catacombs", CityPoi.Kind.SECRET, 0, -8, 120, "ossuary"),
                new CityPoi("catacomb_chamber", CityPoi.Kind.SECRET, 39, -8, 166, "hidden chamber"),
                // District signs.
                new CityPoi("notre_dame_sign", CityPoi.Kind.SIGN, 10, 1, -102, "notre-dame"),
                new CityPoi("louvre_sign", CityPoi.Kind.SIGN, -160, 1, -92, "louvre"),
                new CityPoi("montmartre_sign", CityPoi.Kind.SIGN, -240, 1, 60, "montmartre"),
                new CityPoi("boulevard_sign", CityPoi.Kind.SIGN, 220, 1, 8, "boulevards"),
                new CityPoi("seine_warning", CityPoi.Kind.SIGN, 0, 1, -12, "river"),
                new CityPoi("quay_books", CityPoi.Kind.SIGN, 24, 1, -66, "books"),
                new CityPoi("catacomb_warning", CityPoi.Kind.SIGN, -8, 1, 96, "catacombs"),
                new CityPoi("dance_hall_sign", CityPoi.Kind.SIGN, -240, 1, 206, "dance")
        );
    }

    // ------------------------------------------------------------------ terrain helpers

    /** Cuts terrain above the authored level and fills hollows below it. */
    private static void settle(StructureBuilder b, int x, int z, int y, Palette p) {
        int surface = b.groundY(x, z);
        if (surface > y) {
            b.fill(x, y + 1, z, x, surface + 2, z, AIR);
        } else if (surface < y) {
            b.fill(x, surface + 1, z, x, y, z, p.foundation());
        }
    }

    /** Levels a footprint to {@code ground} and caps it with a foundation course. */
    private static void pad(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                            Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                settle(b, x, z, ground, p);
                b.put(x, ground, z, p.foundation());
            }
        }
    }

    /** Levels and paves an authored rectangle at {@code ground}. */
    private static void pave(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                             BlockState state, Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                settle(b, x, z, ground, p);
                b.put(x, ground, z, state);
            }
        }
    }

    /** Levels and paves a rectangle at an explicit authored level (used on the Montmartre hill). */
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

    // ------------------------------------------------------------------ streets

    private static void streets(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        // The main axis: arrival gate to the cathedral parvis, 14 wide.
        paveOn(b, s, cx - 7, cz - 160, cx + 6, cz + 334, p.ground(), p);

        // Grands boulevards, east district: the east-west boulevard and its north-south street.
        paveOn(b, s, cx + 130, cz + 10, cx + 334, cz + 21, p.ground(), p);
        paveOn(b, s, cx + 240, cz - 244, cx + 251, cz + 160, p.ground(), p);

        // Louvre streets: the Rivoli arcade street and the east street.
        paveOn(b, s, cx - 330, cz - 88, cx - 145, cz - 77, p.ground(), p);
        paveOn(b, s, cx - 156, cz - 244, cx - 145, cz - 77, p.ground(), p);

        // Rue des Martyrs: the Louvre, across the Seine, to Montmartre.
        paveOn(b, s, cx - 252, cz - 88, cx - 241, cz + 207, p.ground(), p);

        // Montmartre lanes, 5 wide.
        paveOn(b, s, cx - 302, cz + 120, cx - 298, cz + 250, p.ground(), p);
        paveOn(b, s, cx - 182, cz + 120, cx - 178, cz + 320, p.ground(), p);
        paveOn(b, s, cx - 320, cz + 136, cx - 145, cz + 140, p.ground(), p);
        paveOn(b, s, cx - 320, cz + 196, cx - 145, cz + 200, p.ground(), p);
        paveOn(b, s, cx - 320, cz + 246, cx - 145, cz + 250, p.ground(), p);

        // Squares and forecourts.
        paveOn(b, s, cx - 24, cz + 70, cx + 20, cz + 100, p.ground(), p);
        paveOn(b, s, cx - 60, cz - 158, cx + 60, cz - 100, p.ground(), p);
        paveOn(b, s, cx - 284, cz - 164, cx - 226, cz - 106, p.ground(), p);
        paveOn(b, s, cx - 190, cz - 180, cx - 160, cz - 90, p.ground(), p);
        paveOn(b, s, cx - 260, cz + 200, cx - 220, cz + 209, p.ground(), p);
        paveOn(b, s, cx + 206, cz - 8, cx + 234, cz + 8, p.ground(), p);
    }

    // ------------------------------------------------------------------ the Seine

    private static void river(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        // The central reach, then the western reach that swings around the Louvre.
        waterRectOn(b, s, cx - 140, cz - 58, cx + 334, cz - 22, p);
        waterRectOn(b, s, cx - 334, cz + 12, cx - 200, cz + 48, p);
        for (int x = -200; x <= -140; x++) {
            int zc = -40 - (x + 140) * 70 / 60;
            waterRectOn(b, s, cx + x, cz + zc - 18, cx + x, cz + zc + 18, p);
        }

        // The island moat: north, south, east and west channels, linked to the Seine.
        waterRectOn(b, s, cx - 116, cz - 276, cx + 116, cz - 270, p);
        waterRectOn(b, s, cx - 116, cz - 96, cx + 116, cz - 90, p);
        waterRectOn(b, s, cx - 116, cz - 270, cx - 110, cz - 90, p);
        waterRectOn(b, s, cx + 110, cz - 270, cx + 116, cz - 90, p);
        waterRectOn(b, s, cx - 116, cz - 90, cx - 110, cz - 70, p);
        waterRectOn(b, s, cx + 110, cz - 90, cx + 116, cz - 70, p);

        // Bridges: the axis, the boulevard, the west street, the garden and the island footbridges.
        bridgeOn(b, s, cx - 7, cz - 60, cx + 6, cz - 20, p);
        bridgeOn(b, s, cx - 7, cz - 98, cx + 6, cz - 88, p);
        bridgeOn(b, s, cx + 240, cz - 60, cx + 251, cz - 20, p);
        bridgeOn(b, s, cx - 252, cz + 8, cx - 241, cz + 52, p);
        bridgeOn(b, s, cx - 306, cz + 8, cx - 295, cz + 52, p);
        bridgeOn(b, s, cx + 104, cz - 150, cx + 122, cz - 144, p);
        bridgeOn(b, s, cx - 122, cz - 150, cx - 104, cz - 144, p);
    }

    private static void waterRectOn(StructureBuilder b, CitySurface s, int x0, int z0, int x1,
                                    int z1, Palette p) {
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
                b.put(x, y - 3, z, SAND);
                b.put(x, y - 4, z, p.foundation());
            }
        }
    }

    private static void bridgeOn(StructureBuilder b, CitySurface s, int x0, int z0, int x1, int z1,
                                 Palette p) {
        int y = s.at((x0 + x1) / 2, (z0 + z1) / 2);
        bridge(b, x0, z0, x1, z1, y, p);
    }

    private static void bridge(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                               Palette p) {
        b.fill(x0, ground, z0, x1, ground, z1, p.foundation());
        boolean alongX = (x1 - x0) >= (z1 - z0);
        if (alongX) {
            for (int x = x0; x <= x1; x++) {
                b.put(x, ground + 1, z0, p.rail());
                b.put(x, ground + 1, z1, p.rail());
            }
            for (int x = x0 + 6; x <= x1 - 6; x += 12) {
                b.fill(x, ground - 3, z0 + 2, x, ground - 1, z1 - 2, p.foundation());
            }
        } else {
            for (int z = z0; z <= z1; z++) {
                b.put(x0, ground + 1, z, p.rail());
                b.put(x1, ground + 1, z, p.rail());
            }
            for (int z = z0 + 6; z <= z1 - 6; z += 12) {
                b.fill(x0 + 2, ground - 3, z, x1 - 2, ground - 1, z, p.foundation());
            }
        }
        b.put(x0, ground + 2, z0, p.light());
        b.put(x1, ground + 2, z1, p.light());
    }

    private static void quays(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        quayOn(b, s, cx - 140, cz - 70, cx + 334, cz - 59, p, true);
        quayOn(b, s, cx - 140, cz - 21, cx + 334, cz - 10, p, false);
        quayOn(b, s, cx - 334, cz, cx - 200, cz + 11, p, true);
        quayOn(b, s, cx - 334, cz + 49, cx - 200, cz + 60, p, false);

        // The island walks.
        paveOn(b, s, cx - 109, cz - 99, cx + 109, cz - 97, p.ground(), p);
        paveOn(b, s, cx - 109, cz - 269, cx + 109, cz - 264, p.ground(), p);
        paveOn(b, s, cx - 109, cz - 264, cx - 104, cz - 97, p.ground(), p);
        paveOn(b, s, cx + 104, cz - 264, cx + 109, cz - 97, p.ground(), p);

        // Booksellers along both banks.
        int[] north = {-120, -80, -40, 20, 60, 100, 150, 200, 260, 310};
        for (int x : north) {
            bouquiniste(b, cx + x, cz - 64, s.at(cx + x, cz - 64), p);
        }
        int[] south = {-100, -50, 10, 70, 130, 190, 230};
        for (int x : south) {
            bouquiniste(b, cx + x, cz - 18, s.at(cx + x, cz - 18), p);
        }
    }

    private static void quayOn(StructureBuilder b, CitySurface s, int x0, int z0, int x1, int z1,
                               Palette p, boolean northBank) {
        paveOn(b, s, x0, z0, x1, z1, p.ground(), p);
        int edgeZ = northBank ? z1 : z0;
        for (int x = x0; x <= x1; x++) {
            if (bridgeGap(x)) {
                continue;
            }
            int y = s.at(x, edgeZ);
            b.put(x, y + 1, edgeZ, p.foundation());
            b.put(x, y + 2, edgeZ, p.rail());
        }
        int lampZ = northBank ? z1 - 7 : z0 + 7;
        for (int x = x0 + 12; x <= x1; x += 24) {
            if (!bridgeGap(x)) {
                lamp(b, x, lampZ, s.at(x, lampZ), p);
            }
        }
    }

    /** True where a bridge crosses a quay, so the quay parapet leaves a gap. */
    private static boolean bridgeGap(int x) {
        return x >= -8 && x <= 7
                || x >= 239 && x <= 252
                || x >= -253 && x <= -240
                || x >= -307 && x <= -294;
    }

    private static void bouquiniste(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x, ground + 1, z, x + 3, ground + 2, z + 1, GREEN);
        b.fill(x + 1, ground + 3, z, x + 2, ground + 3, z + 1, BOOKS);
        b.put(x, ground + 3, z, p.roofSlab());
        b.put(x + 3, ground + 3, z, p.roofSlab());
        b.put(x + 1, ground + 4, z, p.roofSlab());
        b.put(x + 2, ground + 4, z, p.roofSlab());
    }

    // ------------------------------------------------------------------ arrival

    private static void arrival(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        paveOn(b, s, cx - 30, cz + 300, cx + 30, cz + 338, p.ground(), p);
        int base = s.at(cx, cz + 319);
        // Two stone pylons flanking the axis.
        for (int side = -1; side <= 1; side += 2) {
            int x0 = cx + side * 18;
            int x1 = cx + side * 9;
            int ax = Math.min(x0, x1);
            int bx = Math.max(x0, x1);
            pad(b, ax, cz + 312, bx, cz + 326, base, p);
            b.fill(ax, base + 1, cz + 312, bx, base + 14, cz + 326, p.wall());
            b.fill(ax, base + 14, cz + 312, bx, base + 14, cz + 326, p.accent());
            b.crenellations(ax, cz + 312, bx, cz + 326, base + 15);
            b.window(ax + 3, base + 5, cz + 326, 5, 3, false);
            b.put(ax + 1, base + 8, cz + 312, p.light());
        }
        // The wrought-iron arch over the passage.
        b.fill(cx - 9, base + 10, cz + 318, cx + 9, base + 10, cz + 318, p.rail());
        b.fill(cx - 9, base + 11, cz + 318, cx - 8, base + 11, cz + 318, p.accent());
        b.fill(cx + 8, base + 11, cz + 318, cx + 9, base + 11, cz + 318, p.accent());
        b.put(cx, base + 9, cz + 318, p.light());
        // Guardians.
        statue(b, cx - 24, cz + 330, s.at(cx - 24, cz + 330), p);
        statue(b, cx + 24, cz + 330, s.at(cx + 24, cz + 330), p);
    }

    /**
     * The Gare du Nord at the south edge: a level platform, a straight track approach to the
     * district boundary, and the station hall. The transport pass will connect the track.
     */
    private static void station(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int base = s.at(cx + 56, cz + 297) + 1;
        int x0 = cx + 36;
        int x1 = cx + 76;
        int z0 = cz + 286;
        int z1 = cz + 308;
        pad(b, x0, z0, x1, z1, base, p);
        shell(b, x0, base, z0, x1, base + 16, z1, p.wall(), p.foundation(), p.roof());
        for (int y = base; y <= base + 16; y++) {
            b.put(x0, y, z0, p.accent());
            b.put(x1, y, z0, p.accent());
            b.put(x0, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
        windows(b, x0, z0, x1, z1, base, base + 16);
        balcony(b, x0, z0, x1, z1, base + 5, p);
        mansard(b, x0, z0, x1, z1, base + 16, p);
        b.fill(cx + 50, base + 1, z1, cx + 62, base + 5, z1, AIR);
        door(b, p, cx + 56, base + 1, z1, Direction.SOUTH);
        b.fill(cx + 52, base + 10, z0, cx + 60, base + 16, z0, CLOCK);

        // Platform and the straight, level track approach to the south boundary.
        b.fill(x0, base + 1, cz + 308, x1, base + 4, cz + 318, AIR);
        for (int z = cz + 308; z <= cz + 334; z++) {
            b.fill(cx + 52, base + 1, z, cx + 56, base + 4, z, AIR);
        }
        b.fill(x0, base, cz + 308, cx + 51, base, cz + 318, p.foundation());
        b.fill(cx + 57, base, cz + 308, x1, base, cz + 318, p.foundation());
        b.fill(cx + 52, base - 1, cz + 308, cx + 56, base - 1, cz + 334, GRAVEL);
        for (int z = cz + 308; z <= cz + 334; z++) {
            b.put(cx + 54, base, z, RAIL);
            b.put(cx + 56, base, z, RAIL);
        }
        for (int z = cz + 310; z <= cz + 316; z += 3) {
            lamp(b, cx + 50, z, base, p);
            lamp(b, cx + 58, z, base, p);
        }
        signPost(b, p, cx + 60, base, cz + 310, Direction.SOUTH,
                "GARE DU NORD", "PARIS", "PLATFORM 1");
    }

    // ------------------------------------------------------------------ the island

    private static void island(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        pave(b, cx - 60, cz - 158, cx + 60, cz - 100, ground, p.ground(), p);
        gardenBed(b, cx - 80, cz - 264, cx - 50, cz - 244, ground, p);
        gardenBed(b, cx + 50, cz - 264, cx + 80, cz - 244, ground, p);
        for (int x = -50; x <= 50; x += 20) {
            lamp(b, cx + x, cz - 102, ground, p);
        }
    }

    // ------------------------------------------------------------------ Notre-Dame

    private static void notreDame(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 28;
        int x1 = cx + 28;
        int z0 = cz - 244;
        int z1 = cz - 160;
        pad(b, x0 - 2, z0 - 2, x1 + 2, z1 + 2, ground, p);

        // Nave and choir.
        shell(b, x0, ground, z0, x1, ground + 30, z1, LIMESTONE, LIMESTONE, LIMESTONE);

        // Side aisles, low and lean-to roofed.
        shell(b, cx - 36, ground, z0, x0, ground + 16, cz - 184, LIMESTONE, LIMESTONE, LIMESTONE);
        shell(b, x1, ground, z0, cx + 36, ground + 16, cz - 184, LIMESTONE, LIMESTONE, LIMESTONE);
        b.pitchedRoof(cx - 38, z0 - 2, cx - 26, cz - 182, ground + 17, 4, 1);
        b.pitchedRoof(cx + 26, z0 - 2, cx + 38, cz - 182, ground + 17, 4, 1);

        // The main roof, the transept and the apse.
        b.pitchedRoof(cx - 30, z0 - 2, cx + 30, z1 + 2, ground + 31, 10, 1);
        shell(b, cx - 42, ground, cz - 216, cx + 42, ground + 30, cz - 204,
                LIMESTONE, LIMESTONE, LIMESTONE);
        b.fill(cx - 41, ground + 1, cz - 215, cx + 41, ground + 29, cz - 205, AIR);
        b.pitchedRoof(cx - 44, cz - 218, cx + 44, cz - 202, ground + 31, 8, 0);
        apse(b, cx, cz, ground, p);

        // Twin west towers.
        towerAt(b, cx - 27, cz - 171, 11, 46, ground, p, LIMESTONE);
        towerAt(b, cx + 27, cz - 171, 11, 46, ground, p, LIMESTONE);

        // The west front: portals, rose window, gable.
        facade(b, cx, cz, ground, p);

        // Flying buttresses down both sides.
        for (int z = cz - 238; z <= cz - 190; z += 12) {
            if (z == cz - 214) {
                continue;
            }
            flyingButtress(b, cx - 42, z, ground, 18, true);
            flyingButtress(b, cx + 42, z, ground, 18, false);
        }

        // The crossing spire.
        gothicSpire(b, cx, cz - 210, ground + 31, 30);

        // Clerestory and aisle windows.
        for (int z = cz - 238; z <= cz - 190; z += 12) {
            stainedLancet(b, cx - 28, ground + 18, z, 8, 3, false);
            stainedLancet(b, cx + 28, ground + 18, z, 8, 3, false);
            stainedLancet(b, cx - 36, ground + 5, z, 6, 3, false);
            stainedLancet(b, cx + 36, ground + 5, z, 6, 3, false);
        }

        // Interior: floor, columns, pews, altar.
        for (int x = cx - 27; x <= cx + 27; x++) {
            for (int z = cz - 243; z <= cz - 161; z++) {
                b.put(x, ground, z, ((x >> 1) + (z >> 1)) % 2 == 0 ? LIMESTONE : p.foundation());
            }
        }
        for (int z = cz - 238; z <= cz - 190; z += 12) {
            b.fill(cx - 20, ground + 1, z, cx - 20, ground + 28, z, LIMESTONE);
            b.fill(cx + 20, ground + 1, z, cx + 20, ground + 28, z, LIMESTONE);
            b.put(cx - 20, ground + 27, z, p.accent());
            b.put(cx + 20, ground + 27, z, p.accent());
        }
        for (int z = cz - 236; z <= cz - 196; z += 4) {
            b.fill(cx - 18, ground + 1, z, cx - 2, ground + 1, z, DARK_SLAB);
            b.fill(cx + 2, ground + 1, z, cx + 18, ground + 1, z, DARK_SLAB);
        }
        b.fill(cx - 4, ground + 1, cz - 242, cx + 4, ground + 1, cz - 240, LIMESTONE);
        b.fill(cx, ground + 3, cz - 242, cx, ground + 6, cz - 242, LIMESTONE);
        b.fill(cx - 2, ground + 5, cz - 242, cx + 2, ground + 5, cz - 242, LIMESTONE);
        b.put(cx - 4, ground + 2, cz - 242, GOLD);
        b.put(cx + 4, ground + 2, cz - 242, GOLD);
        b.put(cx - 4, ground + 3, cz - 242, p.light());
        b.put(cx + 4, ground + 3, cz - 242, p.light());
        b.put(cx, ground + 20, cz - 162, p.light());
    }

    private static void facade(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int z = cz - 160;
        // Central portal and two side portals.
        b.fill(cx - 5, ground + 1, z, cx + 5, ground + 6, z, AIR);
        b.fill(cx - 13, ground + 1, z, cx - 8, ground + 5, z, AIR);
        b.fill(cx + 8, ground + 1, z, cx + 13, ground + 5, z, AIR);
        door(b, p, cx, ground + 1, z, Direction.SOUTH);
        door(b, p, cx - 10, ground + 1, z, Direction.SOUTH);
        door(b, p, cx + 10, ground + 1, z, Direction.SOUTH);
        // Limestone trim around the portals.
        for (int y = ground + 1; y <= ground + 7; y++) {
            b.put(cx - 6, y, z, LIMESTONE);
            b.put(cx + 6, y, z, LIMESTONE);
            b.put(cx - 14, y, z, LIMESTONE);
            b.put(cx + 14, y, z, LIMESTONE);
        }
        b.fill(cx - 6, ground + 7, z, cx + 6, ground + 7, z, LIMESTONE);
        b.fill(cx - 14, ground + 6, z, cx - 8, ground + 6, z, LIMESTONE);
        b.fill(cx + 8, ground + 6, z, cx + 14, ground + 6, z, LIMESTONE);
        // The rose window and the gable above it.
        roseWindow(b, cx, ground + 20, z, 6);
        for (int i = 0; i <= 7; i++) {
            b.fill(cx - 16 + i, ground + 31 + i, z, cx + 16 - i, ground + 31 + i, z, LIMESTONE);
        }
        b.fill(cx, ground + 39, z, cx, ground + 41, z, GOLD);
        b.fill(cx - 2, ground + 40, z, cx + 2, ground + 40, z, GOLD);
        // A balustrade between the towers.
        b.fill(cx - 16, ground + 30, z, cx + 16, ground + 30, z, p.rail());
    }

    private static void roseWindow(StructureBuilder b, int cx, int cy, int cz, int r) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                int d2 = dx * dx + dy * dy;
                if (d2 > r * r) {
                    continue;
                }
                BlockState state;
                if (d2 > (r - 1) * (r - 1)) {
                    state = LIMESTONE;
                } else {
                    state = switch (Math.floorMod(dx + dy, 4)) {
                        case 0 -> STAINED_BLUE;
                        case 1 -> STAINED_RED;
                        case 2 -> STAINED_PURPLE;
                        default -> STAINED_GOLD;
                    };
                }
                b.put(cx + dx, cy + dy, cz, state);
            }
        }
        b.put(cx, cy, cz, GOLD);
    }

    private static void apse(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        for (int dx = -28; dx <= 28; dx++) {
            int dz = (int) Math.round(16.0 * Math.sqrt(Math.max(0.0,
                    1.0 - dx * dx / (28.0 * 28.0))));
            int az = cz - 244 - dz;
            b.fill(cx + dx, ground + 1, az, cx + dx, ground + 30, az, LIMESTONE);
            b.fill(cx + dx, ground + 1, az - 1, cx + dx, ground + 30, az - 1, LIMESTONE);
        }
        for (int dx = -20; dx <= 20; dx += 10) {
            int dz = (int) Math.round(16.0 * Math.sqrt(Math.max(0.0,
                    1.0 - dx * dx / (28.0 * 28.0))));
            stainedLancet(b, cx + dx, ground + 10, cz - 244 - dz - 1, 10, 3, true);
        }
        b.pitchedRoof(cx - 30, cz - 260, cx + 30, cz - 244, ground + 31, 8, 0);
        b.fill(cx - 10, ground + 1, cz - 244, cx + 10, ground + 8, cz - 244, AIR);
    }

    private static void gothicSpire(StructureBuilder b, int cx, int cz, int baseY, int height) {
        for (int i = 0; i < height; i++) {
            int r = 3 - i * 3 / height;
            if (r <= 0) {
                b.put(cx, baseY + i, cz, SLATE);
                continue;
            }
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d = Math.abs(dx) + Math.abs(dz);
                    if (d > r) {
                        continue;
                    }
                    b.put(cx + dx, baseY + i, cz + dz, d == r ? LIMESTONE : SLATE);
                }
            }
        }
        b.put(cx, baseY + height, cz, GOLD);
        b.put(cx, baseY + height + 1, cz, GOLD);
    }

    private static void flyingButtress(StructureBuilder b, int x, int z, int ground, int height,
                                       boolean west) {
        int dir = west ? 1 : -1;
        b.fill(x, ground, z, x, ground + height, z, LIMESTONE);
        b.fill(x - 1, ground, z - 1, x + 1, ground + 2, z + 1, LIMESTONE);
        b.put(x, ground + height + 1, z, LIMESTONE);
        for (int i = 0; i <= 14; i++) {
            int px = x + dir * i;
            int py = ground + height + i / 2;
            b.put(px, py, z, LIMESTONE);
            b.put(px, py - 1, z, LIMESTONE);
        }
    }

    private static void stainedLancet(StructureBuilder b, int x, int y, int z, int h, int w,
                                      boolean alongX) {
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int px = alongX ? x + j : x;
                int pz = alongX ? z : z + j;
                boolean frame = i == 0 || i == h - 1 || j == 0 || j == w - 1;
                b.put(px, y + i, pz, frame ? LIMESTONE : STAINED_BLUE);
            }
        }
    }

    // ------------------------------------------------------------------ the Louvre

    private static void louvre(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int northBase = s.at(cx - 255, cz - 172);
        int southBase = s.at(cx - 255, cz - 98);
        int westBase = s.at(cx - 292, cz - 135);
        int eastBase = s.at(cx - 218, cz - 135);

        // The Cour Carrée: four wings around an open court.
        palaceWing(b, cx - 300, cz - 180, cx - 210, cz - 164, northBase, 24, p);
        palaceWing(b, cx - 300, cz - 106, cx - 210, cz - 90, southBase, 24, p);
        palaceWing(b, cx - 300, cz - 180, cx - 284, cz - 90, westBase, 24, p);
        palaceWing(b, cx - 226, cz - 180, cx - 210, cz - 90, eastBase, 24, p);

        // Corner pavilions and the Pavillon de l'Horloge.
        pavilion(b, cx - 300, cz - 180, s.at(cx - 300, cz - 180), p);
        pavilion(b, cx - 210, cz - 180, s.at(cx - 210, cz - 180), p);
        pavilion(b, cx - 300, cz - 100, s.at(cx - 300, cz - 100), p);
        pavilion(b, cx - 210, cz - 100, s.at(cx - 210, cz - 100), p);
        pavilionTall(b, cx - 255, cz - 98, southBase, p);

        // The court: fountain and statues.
        fountain(b, cx - 255, cz - 135, s.at(cx - 255, cz - 135), p, 6);
        statue(b, cx - 270, cz - 150, s.at(cx - 270, cz - 150), p);
        statue(b, cx - 240, cz - 150, s.at(cx - 240, cz - 150), p);

        // The Grande Galerie and its east arcade.
        int galleryBase = s.at(cx - 200, cz - 135);
        palaceWing(b, cx - 210, cz - 180, cx - 190, cz - 90, galleryBase, 20, p);
        arcadeEast(b, cx - 190, cz - 176, cz - 94, galleryBase, p);
        colonnade(b, cx - 188, cz - 176, cz - 94, galleryBase, p);

        // Rivoli arcade on the south wing.
        arcadeSouth(b, cx - 296, cx - 214, cz - 90, southBase, p);

        // The curatorial annex in the Tuileries approach.
        int annexBase = s.at(cx - 232, cz);
        house(b, cx - 240, cz - 12, cx - 224, cz + 12, annexBase, 12, p, Side.W, 12);
        frontSign(b, p, cx - 240, cz - 12, cx - 224, cz + 12, annexBase, Side.W,
                "LOUVRE ANNEX", "CURATOR", "KNOCK");

        // The print shop on the quay.
        int shopBase = s.at(cx - 234, cz - 66);
        shop(b, cx - 240, cz - 70, cx - 228, cz - 62, shopBase, p, Side.W);
    }

    private static void palaceWing(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                                   int height, Palette p) {
        pad(b, x0, z0, x1, z1, ground, p);
        shell(b, x0, ground, z0, x1, ground + height, z1, p.wall(), p.foundation(), p.roof());
        for (int y = ground; y <= ground + height; y++) {
            b.put(x0, y, z0, p.accent());
            b.put(x1, y, z0, p.accent());
            b.put(x0, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
        windows(b, x0, z0, x1, z1, ground, ground + height);
        balcony(b, x0, z0, x1, z1, ground + 5, p);
        mansard(b, x0, z0, x1, z1, ground + height, p);
    }

    private static void pavilion(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int half = 10;
        pad(b, cx - half, cz - half, cx + half, cz + half, ground, p);
        shell(b, cx - half, ground, cz - half, cx + half, ground + 30, cz + half,
                p.wall(), p.foundation(), p.roof());
        for (int y = ground; y <= ground + 30; y++) {
            b.put(cx - half, y, cz - half, p.accent());
            b.put(cx + half, y, cz - half, p.accent());
            b.put(cx - half, y, cz + half, p.accent());
            b.put(cx + half, y, cz + half, p.accent());
        }
        windows(b, cx - half, cz - half, cx + half, cz + half, ground, ground + 30);
        mansard(b, cx - half, cz - half, cx + half, cz + half, ground + 30, p);
        b.spire(cx, cz, ground + 36, 8);
    }

    private static void pavilionTall(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int hx = 12;
        int hz = 8;
        pad(b, cx - hx, cz - hz, cx + hx, cz + hz, ground, p);
        shell(b, cx - hx, ground, cz - hz, cx + hx, ground + 36, cz + hz,
                p.wall(), p.foundation(), p.roof());
        for (int y = ground; y <= ground + 36; y++) {
            b.put(cx - hx, y, cz - hz, p.accent());
            b.put(cx + hx, y, cz - hz, p.accent());
            b.put(cx - hx, y, cz + hz, p.accent());
            b.put(cx + hx, y, cz + hz, p.accent());
        }
        b.fill(cx - 3, ground + 22, cz + hz, cx + 3, ground + 28, cz + hz, CLOCK);
        windows(b, cx - hx, cz - hz, cx + hx, cz + hz, ground, ground + 36);
        mansard(b, cx - hx, cz - hz, cx + hx, cz + hz, ground + 36, p);
        b.spire(cx, cz, ground + 42, 10);
    }

    private static void arcadeSouth(StructureBuilder b, int x0, int x1, int z, int ground,
                                    Palette p) {
        for (int x = x0 + 2; x <= x1 - 4; x += 8) {
            b.fill(x, ground + 1, z, x + 3, ground + 5, z, AIR);
            b.put(x, ground + 6, z, p.accent());
            b.put(x + 3, ground + 6, z, p.accent());
            b.put(x + 1, ground + 6, z, p.roofSlab());
            b.put(x + 2, ground + 6, z, p.roofSlab());
            b.fill(x - 1, ground + 1, z, x - 1, ground + 6, z, p.accent());
        }
        b.fill(x0, ground + 7, z, x1, ground + 7, z, p.roofSlab());
    }

    private static void arcadeEast(StructureBuilder b, int x, int z0, int z1, int ground,
                                   Palette p) {
        for (int z = z0 + 2; z <= z1 - 4; z += 8) {
            b.fill(x, ground + 1, z, x, ground + 5, z + 3, AIR);
            b.put(x, ground + 6, z, p.accent());
            b.put(x, ground + 6, z + 3, p.accent());
            b.put(x, ground + 6, z + 1, p.roofSlab());
            b.put(x, ground + 6, z + 2, p.roofSlab());
            b.fill(x, ground + 1, z - 1, x, ground + 6, z - 1, p.accent());
        }
        b.fill(x, ground + 7, z0, x, ground + 7, z1, p.roofSlab());
    }

    private static void colonnade(StructureBuilder b, int x, int z0, int z1, int ground, Palette p) {
        for (int z = z0; z <= z1; z += 8) {
            b.fill(x, ground + 1, z, x, ground + 7, z, p.accent());
        }
        b.fill(x - 1, ground + 8, z0, x + 1, ground + 8, z1, p.roof());
        b.fill(x, ground + 8, z0, x, ground + 8, z1, p.roofSlab());
    }

    private static void tuileries(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int x0 = cx - 330;
        int x1 = cx - 196;
        int z0 = cz - 244;
        int z1 = cz - 190;
        paveOn(b, s, x0, z0, x1, z1, GRAVEL, p);
        paveOn(b, s, cx - 300, z0, cx - 296, z1, p.ground(), p);
        paveOn(b, s, x0, cz - 220, x1, cz - 216, p.ground(), p);

        parterre(b, cx - 326, cz - 240, cx - 304, cz - 224, s.at(cx - 315, cz - 232), p);
        parterre(b, cx - 292, cz - 240, cx - 270, cz - 224, s.at(cx - 281, cz - 232), p);
        parterre(b, cx - 326, cz - 212, cx - 304, cz - 196, s.at(cx - 315, cz - 204), p);
        parterre(b, cx - 292, cz - 212, cx - 270, cz - 196, s.at(cx - 281, cz - 204), p);
        parterre(b, cx - 262, cz - 240, cx - 240, cz - 224, s.at(cx - 251, cz - 232), p);
        parterre(b, cx - 228, cz - 240, cx - 206, cz - 224, s.at(cx - 217, cz - 232), p);

        fountain(b, cx - 263, cz - 217, s.at(cx - 263, cz - 217), p, 7);
        statue(b, cx - 300, cz - 200, s.at(cx - 300, cz - 200), p);
        statue(b, cx - 226, cz - 200, s.at(cx - 226, cz - 200), p);
        tree(b, cx - 334, cz - 232, s.at(cx - 334, cz - 232), p);
        tree(b, cx - 200, cz - 232, s.at(cx - 200, cz - 232), p);
        tree(b, cx - 334, cz - 196, s.at(cx - 334, cz - 196), p);
        tree(b, cx - 200, cz - 196, s.at(cx - 200, cz - 196), p);
    }

    // ------------------------------------------------------------------ Montmartre

    private static void montmartre(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int hillBase = s.at(cx - 240, cz + 300);
        hill(b, s, cx - 240, cz + 300, 52, 11, p);
        plateau(b, cx - 262, cz + 276, cx - 218, cz + 318, hillBase + 11, p);
        steps(b, cx - 246, cz + 250, cx - 234, cz + 276,
                s.at(cx - 240, cz + 250), hillBase + 11, p);
        church(b, cx, cz, hillBase + 11, p);
        danceHall(b, cx, cz, s.at(cx - 240, cz + 220), p);
        apothecary(b, cx, cz, s.at(cx - 240, cz + 120), p);
        cafes(b, cx, cz, s, p);
        studios(b, cx, cz, s, p);

        paveOn(b, s, cx - 260, cz + 200, cx - 220, cz + 209, p.ground(), p);
        fountain(b, cx - 270, cz + 205, s.at(cx - 270, cz + 205), p, 3);

        treeAt(b, cx - 240, cz + 270, s.at(cx - 240, cz + 270) + 5, p);
        treeAt(b, cx - 270, cz + 270, s.at(cx - 270, cz + 270) + 3, p);
        treeAt(b, cx - 210, cz + 270, s.at(cx - 210, cz + 270) + 3, p);
    }

    private static void hill(StructureBuilder b, CitySurface s, int cx, int cz, int radius,
                             int height, Palette p) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > radius * radius) {
                    continue;
                }
                int h = height - (int) (Math.sqrt(d2) * height / radius);
                if (h <= 0) {
                    continue;
                }
                int x = cx + dx;
                int z = cz + dz;
                int base = s.at(x, z);
                b.fill(x, base + 1, z, x, base + h, z, p.foundation());
                b.put(x, base + h, z, GRASS);
            }
        }
    }

    private static void plateau(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                                Palette p) {
        paveAt(b, x0, z0, x1, z1, y, QUARTZ, p);
    }

    private static void steps(StructureBuilder b, int x0, int z0, int x1, int z1, int y0, int y1,
                              Palette p) {
        int run = z1 - z0;
        for (int z = z0; z <= z1; z++) {
            int y = y0 + (z - z0) * (y1 - y0) / run;
            b.fill(x0, y, z, x1, y, z, p.foundation());
            b.fill(x0, y + 1, z, x1, y + 3, z, AIR);
            b.put(x0 - 1, y + 1, z, p.rail());
            b.put(x1 + 1, y + 1, z, p.rail());
        }
    }

    private static void church(StructureBuilder b, int cx, int cz, int base, Palette p) {
        int x0 = cx - 252;
        int x1 = cx - 228;
        int z0 = cz + 282;
        int z1 = cz + 312;
        shell(b, x0, base, z0, x1, base + 16, z1, QUARTZ, QUARTZ, QUARTZ);
        shell(b, cx - 258, base, cz + 292, cx - 222, base + 16, cz + 302,
                QUARTZ, QUARTZ, QUARTZ);
        door(b, p, cx - 240, base + 1, z0, Direction.SOUTH);

        for (int dx = -10; dx <= 10; dx += 20) {
            stainedLancet(b, cx + dx, base + 4, z0, 8, 3, true);
            stainedLancet(b, cx + dx, base + 4, z1, 8, 3, true);
        }
        for (int dz = -8; dz <= 8; dz += 8) {
            stainedLancet(b, x0, base + 4, cz + 297 + dz, 8, 3, false);
            stainedLancet(b, x1, base + 4, cz + 297 + dz, 8, 3, false);
        }

        dome(b, cx - 240, cz + 297, base + 17, 8, 8, p);
        dome(b, cx - 252, cz + 297, base + 17, 5, 6, p);
        dome(b, cx - 228, cz + 297, base + 17, 5, 6, p);
        towerAt(b, cx - 258, cz + 308, 4, 26, base, p, QUARTZ);

        b.fill(cx - 244, base + 1, cz + 286, cx - 236, base + 1, cz + 306, DARK_SLAB);
        b.fill(cx - 242, base + 3, cz + 306, cx - 238, base + 5, cz + 306, LIMESTONE);
        b.put(cx - 240, base + 6, cz + 306, GOLD);
        b.put(cx - 240, base + 7, cz + 306, p.light());
        signPost(b, p, cx - 240, base, cz + 278, Direction.SOUTH,
                "SACRE-COEUR", "OPEN", "NO CLIMBING");
    }

    private static void danceHall(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 256;
        int x1 = cx - 224;
        int z0 = cz + 208;
        int z1 = cz + 232;
        house(b, x0, z0, x1, z1, ground, 14, p, Side.N, 16);
        b.fill(cx - 6, ground + 1, z0, cx + 6, ground + 6, z0, AIR);
        for (int x = x0; x <= x1; x++) {
            b.put(x, ground + 7, z0 - 1, ((x - x0) & 1) == 0 ? RED : WHITE);
            b.put(x, ground + 7, z0 - 2, ((x - x0) & 1) == 0 ? RED : WHITE);
        }
        // A red carpet and the stage.
        b.fill(cx - 4, ground + 1, z0, cx + 4, ground + 1, z1 - 8, RED);
        b.fill(cx - 12, ground + 1, z1 - 8, cx + 12, ground + 1, z1 - 2, DARK_WOOD);
        b.fill(cx - 12, ground + 2, z1 - 8, cx + 12, ground + 5, z1 - 8, DARK_WOOD);
        b.put(cx, ground + 13, cz + 220, GOLD);
        b.put(cx, ground + 12, cz + 220, p.light());
        windmill(b, cx - 252, cz + 212, ground, p);
        signPost(b, p, cx - 240, ground, cz + 206, Direction.NORTH,
                "DANCE HALL", "TONIGHT", "MISSING GIRL");
    }

    private static void windmill(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        b.fill(cx - 4, ground + 1, cz - 4, cx + 4, ground + 22, cz + 4, WHITE);
        b.fill(cx - 3, ground + 1, cz - 3, cx + 3, ground + 21, cz + 3, AIR);
        b.fill(cx - 4, ground + 22, cz - 4, cx + 4, ground + 22, cz + 4, RED);
        int sy = ground + 16;
        for (int i = -8; i <= 8; i++) {
            b.put(cx + i, sy, cz - 5, RED);
            b.put(cx, sy + i, cz - 5, RED);
        }
        for (int i = -8; i <= 8; i += 2) {
            b.put(cx + i, sy + i, cz - 5, DARK_WOOD);
            b.put(cx + i, sy - i, cz - 5, DARK_WOOD);
        }
        b.put(cx, sy, cz - 5, GOLD);
        b.put(cx, ground + 23, cz, p.light());
    }

    private static void apothecary(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 240;
        int x1 = cx - 226;
        int z0 = cz + 112;
        int z1 = cz + 126;
        house(b, x0, z0, x1, z1, ground, 10, p, Side.W, 8);
        b.fill(x0 - 1, ground + 6, z0 + 5, x0 - 1, ground + 8, z0 + 5, GREEN);
        b.fill(x0 - 1, ground + 7, z0 + 4, x0 - 1, ground + 7, z0 + 6, GREEN);
        frontSign(b, p, x0, z0, x1, z1, ground, Side.W,
                "APOTHECARY", "REMEDIES", "HERBS & SALTS");
    }

    private static void cafes(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        cafe(b, cx - 234, cz + 64, s.at(cx - 228, cz + 68), p, Side.W);
        cafe(b, cx - 234, cz + 154, s.at(cx - 228, cz + 158), p, Side.W);
        cafe(b, cx - 294, cz + 150, s.at(cx - 288, cz + 154), p, Side.W);
        cafe(b, cx - 200, cz + 154, s.at(cx - 194, cz + 158), p, Side.E);
        cafe(b, cx - 294, cz + 210, s.at(cx - 288, cz + 214), p, Side.W);
    }

    private static void cafe(StructureBuilder b, int x0, int z0, int ground, Palette p, Side front) {
        int x1 = x0 + 13;
        int z1 = z0 + 9;
        house(b, x0, z0, x1, z1, ground, 10, p, front, 6);

        int[] terrace = switch (front) {
            case W -> new int[]{x0 - 5, z0, x0 - 1, z1};
            case E -> new int[]{x1 + 1, z0, x1 + 5, z1};
            case N -> new int[]{x0, z0 - 4, x1, z0 - 1};
            case S -> new int[]{x0, z1 + 1, x1, z1 + 4};
        };
        pave(b, terrace[0], terrace[1], terrace[2], terrace[3], ground, p.ground(), p);

        switch (front) {
            case W -> {
                for (int z = z0; z <= z1; z++) {
                    b.put(x0 - 1, ground + 4, z, ((z - z0) & 1) == 0 ? RED : WHITE);
                    b.put(x0 - 2, ground + 4, z, ((z - z0) & 1) == 0 ? RED : WHITE);
                }
            }
            case E -> {
                for (int z = z0; z <= z1; z++) {
                    b.put(x1 + 1, ground + 4, z, ((z - z0) & 1) == 0 ? RED : WHITE);
                    b.put(x1 + 2, ground + 4, z, ((z - z0) & 1) == 0 ? RED : WHITE);
                }
            }
            case N -> {
                for (int x = x0; x <= x1; x++) {
                    b.put(x, ground + 4, z0 - 1, ((x - x0) & 1) == 0 ? RED : WHITE);
                    b.put(x, ground + 4, z0 - 2, ((x - x0) & 1) == 0 ? RED : WHITE);
                }
            }
            case S -> {
                for (int x = x0; x <= x1; x++) {
                    b.put(x, ground + 4, z1 + 1, ((x - x0) & 1) == 0 ? RED : WHITE);
                    b.put(x, ground + 4, z1 + 2, ((x - x0) & 1) == 0 ? RED : WHITE);
                }
            }
        }

        for (int x = terrace[0] + 2; x <= terrace[2] - 2; x += 3) {
            for (int z = terrace[1] + 2; z <= terrace[3] - 2; z += 3) {
                b.put(x, ground + 1, z, p.foundation());
                b.put(x, ground + 2, z, p.roofSlab());
            }
        }
        frontSign(b, p, x0, z0, x1, z1, ground, front, "CAFE", "COFFEE", "OPEN LATE");
    }

    private static void studios(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        studio(b, cx - 320, cz + 160, s.at(cx - 314, cz + 164), p, Side.E, false);
        studio(b, cx - 196, cz + 210, s.at(cx - 190, cz + 214), p, Side.W, false);
        studio(b, cx - 270, cz + 150, s.at(cx - 264, cz + 154), p, Side.S, true);
        studio(b, cx - 316, cz + 210, s.at(cx - 310, cz + 214), p, Side.E, true);
    }

    private static void studio(StructureBuilder b, int x0, int z0, int ground, Palette p, Side front,
                               boolean boarded) {
        int x1 = x0 + 11;
        int z1 = z0 + 9;
        house(b, x0, z0, x1, z1, ground, 9, p, front, 5);
        b.window(x0 + 3, ground + 5, z0, 4, 4, false);
        b.put((x0 + x1) / 2, ground + 10, (z0 + z1) / 2, GLASS);
        if (boarded) {
            for (int x = x0 + 3; x <= x0 + 6; x++) {
                b.put(x, ground + 6, z0, p.frame());
                b.put(x, ground + 7, z0, p.frame());
            }
        }
        frontSign(b, p, x0, z0, x1, z1, ground, front,
                "ART STUDIO", boarded ? "BOARDED" : "FOR RENT");
    }

    // ------------------------------------------------------------------ grands boulevards

    private static void boulevards(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        hotel(b, cx + 216, cz - 140, cx + 238, cz - 100, s.at(cx + 227, cz - 120), p);
        theatre(b, cx + 262, cz + 40, cx + 308, cz + 80, s.at(cx + 285, cz + 60), p);

        shopRow(b, cx + 150, cz - 2, cx + 200, cz + 6, s, p, Side.S);
        shopRow(b, cx + 260, cz - 2, cx + 330, cz + 6, s, p, Side.S);
        shopRow(b, cx + 150, cz + 24, cx + 200, cz + 38, s, p, Side.N);
        shopRow(b, cx + 260, cz + 24, cx + 330, cz + 38, s, p, Side.N);
        cafe(b, cx + 206, cz - 2, s.at(cx + 212, cz + 2), p, Side.S);

        questBoard(b, cx + 220, cz - 8, s.at(cx + 220, cz - 8), p);
        gendarmePost(b, cx + 220, cz + 26, s.at(cx + 220, cz + 26), p);

        marketHall(b, cx + 160, cz - 130, cx + 200, cz - 100, s.at(cx + 180, cz - 115), p);
        park(b, cx + 270, cz - 200, cx + 330, cz - 140, s.at(cx + 300, cz - 170), p);
        park(b, cx + 290, cz + 100, cx + 330, cz + 150, s.at(cx + 310, cz + 125), p);

        house(b, cx + 254, cz - 220, cx + 270, cz - 200, s.at(cx + 262, cz - 210),
                12, p, Side.W, 6);
        house(b, cx + 254, cz - 190, cx + 270, cz - 170, s.at(cx + 262, cz - 180),
                12, p, Side.W, 6);
        house(b, cx + 254, cz - 96, cx + 270, cz - 76, s.at(cx + 262, cz - 86),
                12, p, Side.W, 6);
    }

    private static void hotel(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                              Palette p) {
        palaceWing(b, x0, z0, x1, z0 + 8, ground, 26, p);
        palaceWing(b, x0, z1 - 8, x1, z1, ground, 26, p);
        palaceWing(b, x0, z0 + 8, x0 + 8, z1 - 8, ground, 26, p);
        pave(b, x0 + 8, z0 + 8, x1, z1 - 8, ground, p.ground(), p);
        fountain(b, (x0 + x1) / 2, (z0 + z1) / 2, ground, p, 3);
        door(b, p, (x0 + x1) / 2, ground + 1, z0 + 8, Direction.SOUTH);

        for (int x = x0 + 3; x <= x1 - 3; x += 4) {
            b.put(x, ground + 1, z0 + 2, RED);
            b.put(x, ground + 1, z0 + 3, DARK_SLAB);
        }

        // A gate wall on the street side, with a five-wide opening.
        b.walls(x1, ground + 1, z0 + 8, x1, ground + 6, z1 - 8, p.wall());
        b.fill(x1, ground + 1, (z0 + z1) / 2 - 2, x1, ground + 5, (z0 + z1) / 2 + 2, AIR);
        b.put(x1, ground + 6, (z0 + z1) / 2, p.light());
        signPost(b, p, x1 - 1, ground, (z0 + z1) / 2, Direction.EAST,
                "HOTEL DE VILLE", "ROOMS & BEDS", "KEEPER INSIDE");
    }

    private static void theatre(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                                Palette p) {
        pad(b, x0, z0, x1, z1, ground, p);
        shell(b, x0, ground, z0, x1, ground + 24, z1, p.wall(), p.foundation(), p.roof());
        b.fill(x0, ground + 1, (z0 + z1) / 2 - 2, x0, ground + 5, (z0 + z1) / 2 + 2, AIR);
        door(b, p, x0, ground + 1, (z0 + z1) / 2, Direction.WEST);

        for (int z = z0 + 4; z <= z1 - 4; z += 6) {
            b.fill(x0, ground + 1, z, x0, ground + 8, z, p.accent());
        }
        b.fill(x0 - 1, ground + 9, z0 + 4, x0 - 1, ground + 9, z1 - 4, p.roofSlab());
        b.fill(x0 - 2, ground + 8, z0 + 6, x0 - 2, ground + 8, z1 - 6, RED);

        b.fill(x1 - 10, ground + 1, z0 + 4, x1 - 1, ground + 6, z1 - 4, DARK_WOOD);
        for (int z = z0 + 6; z <= z1 - 6; z += 3) {
            b.fill(x0 + 6, ground + 1, z, x1 - 12, ground + 1, z, RED);
        }
        b.put((x0 + x1) / 2, ground + 22, (z0 + z1) / 2, GOLD);
        b.put((x0 + x1) / 2, ground + 21, (z0 + z1) / 2, p.light());
        dome(b, (x0 + x1) / 2, (z0 + z1) / 2, ground + 25, 8, 7, p);
        signPost(b, p, x0 - 2, ground, (z0 + z1) / 2, Direction.WEST,
                "THEATRE", "TONIGHT", "BOX OFFICE");
    }

    private static void shopRow(StructureBuilder b, int x0, int z0, int x1, int z1, CitySurface s,
                                Palette p, Side front) {
        for (int x = x0; x + 9 <= x1; x += 10) {
            int base = s.at(x + 4, (z0 + z1) / 2);
            shop(b, x, z0, x + 9, z1, base, p, front);
        }
    }

    private static void shop(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                             Palette p, Side front) {
        house(b, x0, z0, x1, z1, ground, 12, p, front, (x1 - x0) / 2);
        switch (front) {
            case S -> b.window(x0 + 1, ground + 1, z1, 3, 3, false);
            case N -> b.window(x0 + 1, ground + 1, z0, 3, 3, false);
            case W -> windowZ(b, x0, ground + 1, z0 + 2, 3, 3, p);
            case E -> windowZ(b, x1, ground + 1, z0 + 2, 3, 3, p);
        }
        switch (front) {
            case S -> {
                for (int x = x0; x <= x1; x++) {
                    b.put(x, ground + 3, z1 + 1, ((x - x0) & 1) == 0 ? RED : WHITE);
                    b.put(x, ground + 3, z1 + 2, ((x - x0) & 1) == 0 ? RED : WHITE);
                }
            }
            case N -> {
                for (int x = x0; x <= x1; x++) {
                    b.put(x, ground + 3, z0 - 1, ((x - x0) & 1) == 0 ? RED : WHITE);
                    b.put(x, ground + 3, z0 - 2, ((x - x0) & 1) == 0 ? RED : WHITE);
                }
            }
            case W -> {
                for (int z = z0; z <= z1; z++) {
                    b.put(x0 - 1, ground + 3, z, ((z - z0) & 1) == 0 ? RED : WHITE);
                    b.put(x0 - 2, ground + 3, z, ((z - z0) & 1) == 0 ? RED : WHITE);
                }
            }
            case E -> {
                for (int z = z0; z <= z1; z++) {
                    b.put(x1 + 1, ground + 3, z, ((z - z0) & 1) == 0 ? RED : WHITE);
                    b.put(x1 + 2, ground + 3, z, ((z - z0) & 1) == 0 ? RED : WHITE);
                }
            }
        }
        frontSign(b, p, x0, z0, x1, z1, ground, front, "SHOP", "OPEN");
    }

    private static void questBoard(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 4, ground + 1, z, x - 4, ground + 4, z, p.accent());
        b.fill(x + 4, ground + 1, z, x + 4, ground + 4, z, p.accent());
        b.fill(x - 4, ground + 2, z, x + 4, ground + 4, z, DARK_WOOD);
        b.put(x - 2, ground + 3, z, WHITE);
        b.put(x + 2, ground + 3, z, RED);
        signPost(b, p, x, ground, z + 2, Direction.SOUTH,
                "CITY NOTICES", "STOLEN ART", "SEE CURATOR", "DANCER LOST");
    }

    private static void gendarmePost(StructureBuilder b, int x, int z, int ground, Palette p) {
        int x0 = x - 4;
        int x1 = x + 4;
        int z0 = z - 4;
        int z1 = z + 4;
        pad(b, x0, z0, x1, z1, ground, p);
        shell(b, x0, ground, z0, x1, ground + 6, z1, p.wall(), p.foundation(), p.roof());
        door(b, p, x, ground + 1, z0, Direction.NORTH);
        signPost(b, p, x, ground, z0 - 2, Direction.NORTH,
                "GENDARME", "REPORT CRIMES", "HERE");
    }

    private static void marketHall(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                                   Palette p) {
        pad(b, x0, z0, x1, z1, ground, p);
        b.walls(x0, ground + 1, z0, x1, ground + 8, z1, p.foundation());
        b.fill(x0, ground + 9, z0, x1, ground + 9, z1, GLASS);
        for (int x = x0 + 4; x <= x1 - 4; x += 8) {
            b.fill(x, ground + 1, z0 + 4, x + 2, ground + 1, z1 - 4, DARK_WOOD);
            b.fill(x, ground + 4, z0 + 4, x + 2, ground + 4, z1 - 4, RED);
        }
        b.fill(x0, ground + 1, (z0 + z1) / 2 - 3, x0, ground + 5, (z0 + z1) / 2 + 3, AIR);
        b.fill(x1, ground + 1, (z0 + z1) / 2 - 3, x1, ground + 5, (z0 + z1) / 2 + 3, AIR);
    }

    private static void park(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                             Palette p) {
        pave(b, x0, z0, x1, z1, ground, GRASS, p);
        pave(b, (x0 + x1) / 2 - 2, z0, (x0 + x1) / 2 + 2, z1, ground, p.ground(), p);
        pave(b, x0, (z0 + z1) / 2 - 2, x1, (z0 + z1) / 2 + 2, ground, p.ground(), p);
        tree(b, x0 + 6, z0 + 6, ground, p);
        tree(b, x1 - 6, z0 + 6, ground, p);
        tree(b, x0 + 6, z1 - 6, ground, p);
        tree(b, x1 - 6, z1 - 6, ground, p);
        fountain(b, (x0 + x1) / 2, (z0 + z1) / 2, ground, p, 4);
        bench(b, (x0 + x1) / 2 - 8, (z0 + z1) / 2, ground, p);
        bench(b, (x0 + x1) / 2 + 8, (z0 + z1) / 2, ground, p);
    }

    // ------------------------------------------------------------------ the catacombs

    private static void catacombs(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        entrance(b, cx, cz, ground, p);

        int y = ground - 8;
        tunnel(b, cx - 55, cz + 119, cx + 55, cz + 121, y, p);
        tunnel(b, cx - 17, cz + 110, cx - 15, cz + 121, y, p);

        chamber(b, cx - 55, cz + 128, cx - 25, cz + 152, y, p);
        tunnel(b, cx - 41, cz + 121, cx - 39, cz + 128, y, p);

        chamber(b, cx + 8, cz + 130, cx + 24, cz + 144, y, p);
        tunnel(b, cx + 15, cz + 121, cx + 17, cz + 130, y, p);

        tunnel(b, cx + 38, cz + 121, cx + 40, cz + 158, y, p);
        chamber(b, cx + 30, cz + 158, cx + 48, cz + 174, y, p);
        tunnel(b, cx - 25, cz + 150, cx + 40, cz + 152, y, p);

        bones(b, cx - 55, cz + 128, cx - 25, cz + 152, y, p);
        bones(b, cx + 8, cz + 130, cx + 24, cz + 144, y, p);
        bones(b, cx + 30, cz + 158, cx + 48, cz + 174, y, p);

        b.chest(cx - 50, y + 1, cz + 145, SiteLoot.RELIC);
        b.chest(cx + 14, y + 1, cz + 138, SiteLoot.COMMON);
        b.chest(cx + 44, y + 1, cz + 168, SiteLoot.CULT);

        undergroundSign(b, cx - 50, y + 1, cz + 130, Direction.NORTH,
                "OSSUARY", "RESPECT THE DEAD");
        undergroundSign(b, cx + 38, y + 1, cz + 166, Direction.NORTH,
                "THE PAINTING", "RESTS HERE", "TAKE IT OR", "TELL THE LAW");
        undergroundSign(b, cx + 39, y + 1, cz + 147, Direction.NORTH,
                "DEAD END", "TURN BACK");
        falseWall(b, cx + 39, cz + 149, y);
    }

    private static void entrance(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = cx - 22;
        int x1 = cx - 10;
        int z0 = cz + 84;
        int z1 = cz + 96;
        pad(b, x0, z0, x1, z1, ground, p);
        b.walls(x0, ground + 1, z0, x1, ground + 8, z1, LIMESTONE);
        b.fill(x0 + 1, ground + 1, z0 + 1, x1 - 1, ground + 7, z1 - 1, AIR);
        b.fill(cx - 19, ground + 1, z0, cx - 13, ground + 4, z0, AIR);
        b.fill(cx - 19, ground + 1, z1, cx - 13, ground + 4, z1, AIR);
        b.fill(x0, ground + 1, cz + 87, x0, ground + 4, cz + 93, AIR);
        b.fill(x1, ground + 1, cz + 87, x1, ground + 4, cz + 93, AIR);
        b.fill(x0 - 1, ground + 8, z0 - 1, x1 + 1, ground + 8, z1 + 1, p.roofSlab());
        b.put(cx - 16, ground + 9, cz + 90, p.light());

        for (int z = cz + 86; z <= cz + 110; z++) {
            int y = ground - 1 - (z - (cz + 86)) * 7 / 24;
            b.fill(cx - 17, y, z, cx - 15, y, z, p.foundation());
            b.fill(cx - 17, y + 1, z, cx - 15, y + 4, z, AIR);
            b.fill(cx - 18, y + 1, z, cx - 18, y + 4, z, LIMESTONE);
            b.fill(cx - 14, y + 1, z, cx - 14, y + 4, z, LIMESTONE);
            if ((z - (cz + 86)) % 6 == 0) {
                b.put(cx - 18, y + 3, z, SOUL);
            }
        }
        for (int z = cz + 86; z <= cz + 96; z++) {
            b.put(cx - 18, ground + 1, z, p.rail());
            b.put(cx - 14, ground + 1, z, p.rail());
        }
        b.fill(cx - 18, ground + 1, cz + 96, cx - 14, ground + 1, cz + 96, p.rail());
        signPost(b, p, cx - 8, ground, cz + 96, Direction.SOUTH,
                "CATACOMBS", "KEEP TO LAMPS", "DO NOT WANDER");
    }

    private static void tunnel(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                               Palette p) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        b.fill(ax, y, az, bx, y, bz, GRAVEL);
        b.fill(ax, y + 1, az, bx, y + 4, bz, AIR);
        b.fill(ax, y + 5, az, bx, y + 5, bz, p.roof());
    }

    private static void chamber(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                                Palette p) {
        b.fill(x0, y, z0, x1, y, z1, GRAVEL);
        b.fill(x0, y + 1, z0, x1, y + 4, z1, AIR);
        b.fill(x0, y + 5, z0, x1, y + 5, z1, p.roof());
        b.fill(x0, y + 1, z0, x0, y + 4, z0, p.foundation());
        b.fill(x1, y + 1, z0, x1, y + 4, z0, p.foundation());
        b.fill(x0, y + 1, z1, x0, y + 4, z1, p.foundation());
        b.fill(x1, y + 1, z1, x1, y + 4, z1, p.foundation());
        b.put(x0 + 1, y + 4, z0 + 1, Palette.cobweb());
        b.put(x1 - 1, y + 4, z1 - 1, Palette.cobweb());
    }

    private static void bones(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                              Palette p) {
        for (int x = x0 + 1; x < x1; x += 3) {
            b.fill(x, y + 1, z0 + 1, x, y + 2, z0 + 1, BONE);
            b.put(x, y + 3, z0 + 1, SKULL);
            b.fill(x, y + 1, z1 - 1, x, y + 2, z1 - 1, BONE);
            b.put(x, y + 3, z1 - 1, SKULL);
        }
        for (int z = z0 + 1; z < z1; z += 3) {
            b.fill(x0 + 1, y + 1, z, x0 + 1, y + 2, z, BONE);
            b.put(x0 + 1, y + 3, z, SKULL);
            b.fill(x1 - 1, y + 1, z, x1 - 1, y + 2, z, BONE);
            b.put(x1 - 1, y + 3, z, SKULL);
        }
        b.fill((x0 + x1) / 2, y + 1, (z0 + z1) / 2, (x0 + x1) / 2, y + 4, (z0 + z1) / 2, BONE);
    }

    private static void falseWall(StructureBuilder b, int x, int z, int y) {
        b.fill(x - 2, y + 1, z, x + 2, y + 4, z, BONE);
        b.fill(x, y + 1, z, x, y + 2, z, AIR);
    }

    // ------------------------------------------------------------------ greenery

    private static void greenery(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        int[] boulevard = {150, 180, 198, 258, 282, 306, 330};
        for (int x : boulevard) {
            tree(b, cx + x, cz + 9, s.at(cx + x, cz + 9), p);
        }
        int[] quayTrees = {-120, -80, -40, 40, 80, 120, 160, 200, 280, 320};
        for (int x : quayTrees) {
            tree(b, cx + x, cz - 68, s.at(cx + x, cz - 68), p);
        }
        tree(b, cx - 70, cz - 250, s.at(cx - 70, cz - 250), p);
        tree(b, cx + 70, cz - 250, s.at(cx + 70, cz - 250), p);
        tree(b, cx - 290, cz + 260, s.at(cx - 290, cz + 260), p);
        tree(b, cx - 190, cz + 260, s.at(cx - 190, cz + 260), p);
        tree(b, cx - 280, cz + 230, s.at(cx - 280, cz + 230), p);
    }

    private static void tree(StructureBuilder b, int x, int z, int ground, Palette p) {
        treeAt(b, x, z, ground, p);
    }

    private static void treeAt(StructureBuilder b, int x, int z, int baseY, Palette p) {
        int h = 5 + Math.floorMod(x + z, 3);
        b.fill(x, baseY + 1, z, x, baseY + h, z, PLANE_LOG);
        for (int dy = -1; dy <= 2; dy++) {
            int r = dy <= 0 ? 2 : 1;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) > r + 1) {
                        continue;
                    }
                    b.put(x + dx, baseY + h + dy, z + dz, PLANE_LEAVES);
                }
            }
        }
    }

    private static void gardenBed(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                                  Palette p) {
        pave(b, x0, z0, x1, z1, ground, GRASS, p);
        b.walls(x0, ground + 1, z0, x1, ground + 1, z1, HEDGE);
        for (int x = x0 + 1; x < x1; x += 2) {
            for (int z = z0 + 1; z < z1; z += 2) {
                b.put(x, ground + 2, z, ((x + z) & 1) == 0 ? POPPY : DANDELION);
            }
        }
    }

    private static void parterre(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                                 Palette p) {
        gardenBed(b, x0, z0, x1, z1, ground, p);
    }

    private static void fountain(StructureBuilder b, int cx, int cz, int ground, Palette p, int r) {
        b.fill(cx - r, ground, cz - r, cx + r, ground, cz + r, p.foundation());
        b.walls(cx - r, ground + 1, cz - r, cx + r, ground + 1, cz + r, p.foundation());
        b.fill(cx - r + 1, ground + 1, cz - r + 1, cx + r - 1, ground + 1, cz + r - 1, WATER);
        b.fill(cx, ground + 2, cz, cx, ground + 4, cz, p.accent());
        b.put(cx, ground + 5, cz, WATER);
    }

    private static void statue(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 1, ground, z - 1, x + 1, ground, z + 1, p.foundation());
        b.fill(x, ground + 1, z, x, ground + 4, z, LIMESTONE);
        b.put(x, ground + 5, z, LIMESTONE);
        b.put(x - 1, ground + 4, z, LIMESTONE);
        b.put(x + 1, ground + 4, z, LIMESTONE);
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
        for (int y = ground; y <= top; y++) {
            b.put(x0, y, z0, p.accent());
            b.put(x1, y, z0, p.accent());
            b.put(x0, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
        windows(b, x0, z0, x1, z1, ground, top);
        balcony(b, x0, z0, x1, z1, ground + 4, p);
        mansard(b, x0, z0, x1, z1, top, p);
        doorFor(b, p, x0, z0, x1, z1, ground + 1, doorSide, doorOffset);
    }

    private static void windows(StructureBuilder b, int x0, int z0, int x1, int z1, int ground,
                                int top) {
        for (int y = ground + 2; y <= top - 2; y += 3) {
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

    private static void windowZ(StructureBuilder b, int x, int y, int z, int h, int w, Palette p) {
        for (int dz = 0; dz < w; dz++) {
            for (int dy = 0; dy < h; dy++) {
                boolean frame = dy == 0 || dy == h - 1 || dz == 0 || dz == w - 1;
                b.put(x, y + dy, z + dz, frame ? p.frame() : p.window());
            }
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
        }
        for (int x = x0 + 2; x <= x1 - 2; x += 7) {
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

    private static void towerAt(StructureBuilder b, int cx, int cz, int half, int height,
                                int ground, Palette p, BlockState wall) {
        int x0 = cx - half;
        int x1 = cx + half;
        int z0 = cz - half;
        int z1 = cz + half;
        pad(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, p);
        shell(b, x0, ground, z0, x1, ground + height, z1, wall, p.foundation(), p.roof());
        for (int y = ground; y <= ground + height; y++) {
            b.put(x0, y, z0, p.accent());
            b.put(x1, y, z0, p.accent());
            b.put(x0, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
        for (int y = ground + 10; y <= ground + height - 8; y += 9) {
            stainedLancet(b, x0 + 3, y, z1, 5, 3, true);
            stainedLancet(b, x1 - 5, y, z1, 5, 3, true);
            stainedLancet(b, x0 + 3, y, z0, 5, 3, true);
            stainedLancet(b, x1 - 5, y, z0, 5, 3, true);
            stainedLancet(b, x0, y, z0 + 3, 5, 3, false);
            stainedLancet(b, x1, y, z0 + 3, 5, 3, false);
        }
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground + height + 1);
    }

    private static void dome(StructureBuilder b, int cx, int cz, int baseY, int radius, int height,
                             Palette p) {
        for (int i = 0; i < height; i++) {
            int r = radius - i * radius / height;
            if (r <= 0) {
                b.put(cx, baseY + i, cz, QUARTZ);
                continue;
            }
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > r * r) {
                        continue;
                    }
                    boolean edge = d2 > (r - 1) * (r - 1);
                    b.put(cx + dx, baseY + i, cz + dz, edge ? p.accent() : QUARTZ);
                }
            }
        }
        b.put(cx, baseY + height, cz, GOLD);
        b.put(cx, baseY + height + 1, cz, GOLD);
    }

    // ------------------------------------------------------------------ readable signs

    private static void signs(StructureBuilder b, int cx, int cz, CitySurface s, Palette p) {
        signPost(b, p, cx + 8, s.at(cx + 8, cz + 306), cz + 306, Direction.SOUTH,
                "NOTRE-DAME", "LOUVRE", "MONTMARTRE", "GRANDS BLVDS");
        signPost(b, p, cx - 24, s.at(cx - 24, cz + 306), cz + 306, Direction.SOUTH,
                "SEINE", "CATACOMBS", "CITY NOTICES");
        signPost(b, p, cx + 10, s.at(cx + 10, cz - 102), cz - 102, Direction.SOUTH,
                "NOTRE-DAME", "WEST FRONT", "OPEN DAWN", "NO GUIDE");
        signPost(b, p, cx - 160, s.at(cx - 160, cz - 92), cz - 92, Direction.EAST,
                "LOUVRE", "PALACE", "GARDENS", "NO DOGS");
        signPost(b, p, cx - 240, s.at(cx - 240, cz + 60), cz + 60, Direction.SOUTH,
                "MONTMARTRE", "CAFES", "DANCE HALL", "STUDIOS");
        signPost(b, p, cx + 220, s.at(cx + 220, cz + 8), cz + 8, Direction.SOUTH,
                "GRANDS BLVDS", "SHOPS", "THEATRE", "HOTEL");
        signPost(b, p, cx, s.at(cx, cz - 12), cz - 12, Direction.SOUTH,
                "SEINE", "QUAYS", "BOOKSELLERS", "BRIDGES");
        signPost(b, p, cx + 24, s.at(cx + 24, cz - 66), cz - 66, Direction.SOUTH,
                "OLD BOOKS", "MAPS & PRINTS");
    }

    private static void signPost(StructureBuilder b, Palette p, int x, int y, int z,
                                 Direction facing, String... lines) {
        settle(b, x, z, y, p);
        b.put(x, y, z, p.accent());
        CityPlanSupport.sign(b, x, y + 1, z, facing, lines);
    }

    private static void undergroundSign(StructureBuilder b, int x, int y, int z, Direction facing,
                                        String... lines) {
        b.put(x, y - 1, z, LIMESTONE);
        CityPlanSupport.sign(b, x, y, z, facing, lines);
    }

    private static void frontSign(StructureBuilder b, Palette p, int x0, int z0, int x1, int z1,
                                  int ground, Side front, String... lines) {
        switch (front) {
            case S -> signPost(b, p, x0 + 1, ground, z1 + 1, Direction.SOUTH, lines);
            case N -> signPost(b, p, x0 + 1, ground, z0 - 1, Direction.NORTH, lines);
            case W -> signPost(b, p, x0 - 1, ground, z0 + 1, Direction.WEST, lines);
            case E -> signPost(b, p, x1 + 1, ground, z0 + 1, Direction.EAST, lines);
        }
    }
}
