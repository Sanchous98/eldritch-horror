package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;

/**
 * Nairobi — cultural city style. East African savannah capital: warm earth-toned stone with heavy
 * timber lintels, low single-storey shophouses under corrugated metal roofs, and a few modern
 * towers rising over them. The acacia tree is the recurring motif, and the <em>boma</em> thorn
 * kraal its post-and-thorn analogue.
 *
 * <p>Landmark: a tall modern tower with a stepped, crenellated crown and a spire (KICC / Times
 * Tower silhouette). Street props: timber-framed acacia canopy posts, rusty corrugated street
 * awnings, iron market stalls, a civic obelisk monument and a scattering of dead-bush scrub.
 * Everything is deterministic and uses only {@link StructureBuilder} + {@link Palette}.
 */
public final class NairobiStyle implements CityStyle {

    @Override
    public String id() {
        return "nairobi";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: earth-toned Nairobi stone + acacia timber + corrugated tin, regardless of
        // Köppen (Nairobi is Cfb, but must not read as European). Climate only supplies the
        // overgrowth (savannah → dead bush, coast → kelp), exactly as Palette.fromBiome defines.
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.DIRT_PATH.defaultBlockState(),               // ground: packed red earth street
                Blocks.TUFF.defaultBlockState(),                    // foundation: grey volcanic stone base
                Blocks.TUFF_BRICKS.defaultBlockState(),             // wall: earth-toned stone
                Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),    // weathered: eroded stone
                Blocks.STRIPPED_ACACIA_LOG.defaultBlockState(),     // accent: warm timber lintels
                Blocks.RAW_COPPER_BLOCK.defaultBlockState(),        // roof: rusted corrugated tin
                Blocks.ACACIA_STAIRS.defaultBlockState(),           // roof stairs
                Blocks.ACACIA_SLAB.defaultBlockState(),             // roof slabs / eaves
                Blocks.TINTED_GLASS.defaultBlockState(),            // window: dark modern glazing / shopfront
                Blocks.ACACIA_TRAPDOOR.defaultBlockState(),         // frame: timber mullions
                Blocks.ACACIA_DOOR.defaultBlockState(),             // door
                Blocks.ACACIA_FENCE.defaultBlockState(),            // rail / timber post
                Blocks.LANTERN.defaultBlockState(),                 // light: street lantern
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.COARSE_DIRT.defaultBlockState());            // rubble: dry red debris
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        modernTower(b, cx, cz, ground, p);
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Lantern posts down the two main axes, between the plaza and the district edge.
        for (int d = 26; d <= district - 8; d += 12) {
            StyleKit.lanternPost(b, cx + d, cz + 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 3, ground, p);
            StyleKit.lanternPost(b, cx + 3, cz + d, ground, p);
            StyleKit.lanternPost(b, cx - 3, cz - d, ground, p);
        }
        // Acacia-like posts and corrugated market awnings on the four district corners.
        acaciaPost(b, cx + 30, cz + 30, ground, p);
        acaciaPost(b, cx - 30, cz + 30, ground, p);
        acaciaPost(b, cx + 30, cz - 30, ground, p);
        acaciaPost(b, cx - 30, cz - 30, ground, p);
        marketStall(b, cx + 20, cz - 2, ground, p);
        marketStall(b, cx - 20, cz + 2, ground, p);
        // A civic monument and statues framing the main approach.
        StyleKit.obelisk(b, cx, cz - district / 2, ground, p);
        StyleKit.statue(b, cx + district / 2, cz, ground, p);
        StyleKit.statue(b, cx - district / 2, cz, ground, p);
        // Dry savannah scrub at the plaza fringe.
        b.scatter(cx - 12, cz - 12, cx + 12, cz + 12, ground + 1, ground + 1,
                p.overgrowth() != null ? p.overgrowth() : Blocks.DEAD_BUSH.defaultBlockState(), 0.08f);
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        if (rng.nextFloat() > 0.55f) {
            return;
        }
        // A corrugated lean-to awning over the frontage (min-Z facade), on timber posts.
        int depth = 2;
        int y = y0 + 2;
        for (int fx = x + 1; fx <= x1 - 1; fx++) {
            b.put(fx, y, z - depth, p.wall());
        }
        for (int fx = x + 1; fx <= x1 - 1; fx += 3) {
            b.put(fx, y0 + 1, z - depth, p.frame());
        }
    }

    /**
     * A modern tower: a wide podium with a glazed lobby, over which a slimmer shaft rises in
     * glazed bands, capped by a stepped crown (crenellations) and a spire — the KICC/Times Tower
     * silhouette, deliberately taller than the shophouses around it.
     */
    private static void modernTower(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int r = 6;
        int pod0 = ground + 1;
        int pod1 = pod0 + 6;
        b.ground(cx - r - 2, cz - r - 2, cx + r + 2, cz + r + 2, ground, ground, p.foundation());
        b.room(cx - r, pod0, cz - r, cx + r, pod1, cz + r,
                new Doorway(Side.S, r), new Doorway(Side.N, r));
        for (int z = cz - r + 2; z <= cz + r - 2; z += 3) {
            b.window(cx - r, pod0 + 2, z, 3, 2, false);
            b.window(cx + r, pod0 + 2, z, 3, 2, false);
        }
        b.crenellations(cx - r - 1, cz - r - 1, cx + r + 1, cz + r + 1, pod1 + 1);

        // The shaft: three glazed bands per face.
        int s = 3;
        int shaft0 = pod1 + 2;
        int shaft1 = pod1 + 26;
        b.room(cx - s, shaft0, cz - s, cx + s, shaft1, cz + s);
        for (int y = ground + 1; y <= shaft1; y++) {
            b.put(cx - s, y, cz - s, p.accent());
            b.put(cx + s, y, cz - s, p.accent());
            b.put(cx - s, y, cz + s, p.accent());
            b.put(cx + s, y, cz + s, p.accent());
        }
        for (int y = shaft0 + 3; y <= shaft1 - 3; y += 5) {
            b.window(cx - s, y, cz, 3, 1, true);
            b.window(cx + s, y, cz, 3, 1, true);
            b.window(cx, y, cz - s, 3, 1, true);
            b.window(cx, y, cz + s, 3, 1, true);
        }

        // Stepped crown: a wider ring and a narrower ring, then crenellations and a spire.
        int crown = shaft1 + 1;
        b.room(cx - 5, crown, cz - 5, cx + 5, crown + 1, cz + 5);
        b.room(cx - 3, crown + 2, cz - 3, cx + 3, crown + 3, cz + 3);
        b.crenellations(cx - 4, cz - 4, cx + 4, cz + 4, crown + 4);
        b.spire(cx, cz, crown + 5, 16);
        StyleKit.twoHighDoor(b, p, cx, cz + r, pod0 + 1, Direction.SOUTH);
    }

    /** An acacia: a timber post with an outspread, flattened canopy of acacia leaves. */
    private static void acaciaPost(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int y0 = ground + 1;
        int trunk = 4;
        for (int y = y0; y < y0 + trunk; y++) {
            b.put(cx, y, cz, p.frame());
        }
        int top = y0 + trunk;
        b.fill(cx - 2, top, cz - 2, cx + 2, top, cz + 2, Blocks.ACACIA_LEAVES.defaultBlockState());
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) + Math.abs(dz) == 4) {
                    b.put(cx + dx, top - 1, cz + dz, Blocks.ACACIA_LEAVES.defaultBlockState());
                }
            }
        }
        b.put(cx, top + 1, cz, Blocks.ACACIA_LEAVES.defaultBlockState());
    }

    /** A market stall: timber posts, an acacia-plank table and a rusted corrugated awning. */
    private static void marketStall(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int y = ground + 1;
        b.fill(cx - 1, y, cz - 1, cx + 1, y, cz + 1, p.roofSlab());
        for (int dx = -2; dx <= 2; dx += 4) {
            for (int dz = -2; dz <= 2; dz += 4) {
                b.fill(cx + dx, y + 1, cz + dz, cx + dx, y + 3, cz + dz, p.frame());
            }
        }
        b.fill(cx - 2, y + 4, cz - 2, cx + 2, y + 4, cz + 2, p.roof());
        b.put(cx, y + 2, cz, p.light());
    }
}
