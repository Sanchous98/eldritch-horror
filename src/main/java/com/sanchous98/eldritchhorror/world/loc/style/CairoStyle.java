package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;

/**
 * Cairo — cultural city style. Islamic Cairo: mudbrick and sandstone walls, stone domes and
 * minarets, pointed arches, carved trim and wooden <em>mashrabiya</em> lattice windows, arcaded
 * courtyards.
 *
 * <p>Landmark: a great mosque with a large dome and two minarets. Street props: lantern posts,
 * arcaded colonnades, obelisks and statues. Everything is deterministic and uses only
 * {@link StructureBuilder} + {@link Palette} (via {@link StyleKit}).
 */
public final class CairoStyle implements CityStyle {

    @Override
    public String id() {
        return "cairo";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: warm mudbrick with pale sandstone coursing; climate only supplies the
        // overgrowth (arid → dead bush, coast → kelp).
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.SMOOTH_SANDSTONE.defaultBlockState(),        // ground: paved plaza
                Blocks.CUT_SANDSTONE.defaultBlockState(),           // foundation: stone base course
                Blocks.MUD_BRICKS.defaultBlockState(),              // wall: mudbrick
                Blocks.PACKED_MUD.defaultBlockState(),              // weathered: crumbled mudbrick
                Blocks.CHISELED_SANDSTONE.defaultBlockState(),      // accent: carved arches / quoins
                Blocks.SANDSTONE.defaultBlockState(),               // roof: pale dome / terrace
                Blocks.SANDSTONE_STAIRS.defaultBlockState(),        // roof stairs
                Blocks.SANDSTONE_SLAB.defaultBlockState(),          // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),              // window: glazing
                Blocks.ACACIA_TRAPDOOR.defaultBlockState(),         // frame: mashrabiya lattice
                Blocks.ACACIA_DOOR.defaultBlockState(),             // door: pointed-arch gate
                Blocks.ACACIA_FENCE.defaultBlockState(),            // rail
                Blocks.LANTERN.defaultBlockState(),                 // light: hanging lamp
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.COARSE_DIRT.defaultBlockState());            // rubble: dry debris
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        int y0 = ground + 1;
        // A Mamluk madrasa: a low rectangular prayer hall with a grand pointed-arch portal and a
        // flat parapet. No central Ottoman dome-mosque silhouette — that belongs to Istanbul.
        int hx = 14;
        int hz = 12;
        int wallTop = y0 + 9;
        b.ground(cx - hx - 3, cz - hz - 3, cx + hx + 3, cz + hz + 3, ground, ground, p.foundation());
        b.room(cx - hx, y0, cz - hz, cx + hx, wallTop, cz + hz,
                new StructureBuilder.Doorway(StructureBuilder.Side.S, hx));
        // Flat terrace roof with a low parapet.
        b.fill(cx - hx - 1, wallTop + 1, cz - hz - 1, cx + hx + 1, wallTop + 1, cz + hz + 1, p.roof());
        // Mashrabiya lattice high on the outer walls.
        for (int x = cx - hx + 3; x <= cx + hx - 3; x += 4) {
            b.window(x, y0 + 5, cz - hz, 3, 1, true);
            b.window(x, y0 + 5, cz + hz, 3, 1, true);
        }
        // An ogee entrance portal in front of the south gate.
        b.fill(cx - 3, y0, cz + hz + 1, cx + 3, y0 + 6, cz + hz + 1, p.accent());
        b.put(cx, y0 + 1, cz + hz + 1, p.door());
        b.crenellations(cx - 3, cz + hz + 1, cx + 3, cz + hz + 1, y0 + 7);

        // A carved stone dome over the south-east corner (a mausoleum chamber), on a short drum.
        int mx = cx + hx - 5;
        int mz = cz - hz + 5;
        b.room(mx - 4, y0, mz - 4, mx + 4, y0 + 10, mz + 4);
        StyleKit.dome(b, mx, mz, y0 + 11, 6, p);

        // One tall, slender Mamluk minaret in the north-west corner (multi-balcony).
        mamlukMinaret(b, cx - hx + 3, cz + hz - 3, ground, p);
    }

    /** A slender Mamluk minaret: a tall square shaft with two balcony rings and a ribbed cap. */
    private static void mamlukMinaret(StructureBuilder b, int x, int z, int ground, Palette p) {
        int y0 = ground + 1;
        int top = y0 + 30;
        b.room(x - 1, y0, z - 1, x + 1, top, z + 1);
        for (int y = y0; y <= top; y++) {
            b.put(x - 1, y, z - 1, p.accent());
            b.put(x + 1, y, z - 1, p.accent());
            b.put(x - 1, y, z + 1, p.accent());
            b.put(x + 1, y, z + 1, p.accent());
        }
        // Balcony rings at two heights, then a ribbed lantern cap and finial.
        b.crenellations(x - 2, z - 2, x + 2, z + 2, y0 + 14);
        b.crenellations(x - 2, z - 2, x + 2, z + 2, y0 + 24);
        b.pitchedRoof(x - 1, z - 1, x + 1, z + 1, top, 3, 0);
        b.spire(x, z, top + 3, 5);
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        if (rng.nextFloat() > 0.6f) {
            return;
        }
        // Mashrabiya: a carved wooden lattice band on one upper wall.
        int y = y1 - 2;
        switch (rng.nextInt(4)) {
            case 0 -> b.fill(x + 1, y, z, x1 - 1, y + 1, z, p.frame());
            case 1 -> b.fill(x + 1, y, z1, x1 - 1, y + 1, z1, p.frame());
            case 2 -> b.fill(x, y, z + 1, x, y + 1, z1 - 1, p.frame());
            default -> b.fill(x1, y, z + 1, x1, y + 1, z1 - 1, p.frame());
        }
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Lantern posts down both main axes, between the plaza and the district edge.
        for (int d = 26; d <= district - 8; d += 12) {
            StyleKit.lanternPost(b, cx + d, cz + 4, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 4, ground, p);
            StyleKit.lanternPost(b, cx + 4, cz + d, ground, p);
            StyleKit.lanternPost(b, cx - 4, cz - d, ground, p);
        }
        // Obelisks frame the southern approach (the minarets mark the north).
        StyleKit.obelisk(b, cx - 10, cz + district - 40, ground, p);
        StyleKit.obelisk(b, cx + 10, cz + district - 40, ground, p);
        // Statues flank the eastern and western gates.
        StyleKit.statue(b, cx - district + 40, cz - 9, ground, p);
        StyleKit.statue(b, cx + district - 40, cz + 9, ground, p);
        // Arcaded colonnades frame two sides of the plaza.
        arcade(b, cx - 20, cx + 20, cz + 24, ground, p);
        arcade(b, cx - 20, cx + 20, cz - 24, ground, p);
    }

    /**
     * A short run of pointed arches along X at fixed Z: stone piers every three blocks under a
     * lintel, with stair heads suggesting the arch. Small and cheap — safe to call per chunk.
     */
    private static void arcade(StructureBuilder b, int x0, int x1, int z, int ground, Palette p) {
        int y = ground + 1;
        int top = y + 3;
        for (int x = x0; x <= x1; x += 3) {
            b.fill(x, y, z, x, top, z, p.accent());
        }
        b.fill(x0 - 1, top + 1, z, x1 + 1, top + 1, z, p.roof());
        for (int x = x0 + 2; x <= x1 - 2; x += 3) {
            b.put(x, top, z, p.roofStairs());
        }
    }
}
