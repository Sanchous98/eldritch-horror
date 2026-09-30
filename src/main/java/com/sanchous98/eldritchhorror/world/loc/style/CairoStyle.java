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
                bio.overgrowth(),
                Blocks.COARSE_DIRT.defaultBlockState());            // rubble: dry debris
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // Giza: the Great Pyramid dominates the plaza, a smaller companion pyramid stands off to
        // one side, and a recumbent sphinx faces east along the causeway. Cairo's own Mamluk
        // minaret lingers on the north-east edge as a slender counterpoint.
        int half = 30;    // 61-block base — broad, stepped, unmistakably a pyramid
        int height = 74;  // apex ~75 blocks above the plaza
        b.ground(cx - half, cz - half, cx + half, cz + half, ground, ground, p.foundation());
        for (int layer = 0; layer <= height; layer++) {
            int h = half - (layer * half) / height;
            if (h < 0) {
                break;
            }
            int y = ground + 1 + layer;
            // Banded casing courses: dark mudbrick against light sandstone, so the tapering mass
            // reads against the sand plateau instead of blending into it.
            b.fill(cx - h, y, cz - h, cx + h, y, cz + h,
                    (layer & 1) == 0 ? p.wall() : p.foundation());
        }
        // A gilt capstone (benben) crowning the apex.
        b.put(cx, ground + 1 + height, cz, p.accent());
        b.put(cx, ground + 2 + height, cz, p.light());

        // Companion pyramid (Khafre-sized): a smaller stepped mass just beyond the great one.
        int chalf = 8;
        int cheight = 24;
        int px = cx;
        int pz = b.rng().nextBoolean() ? cz - 36 : cz + 36;
        b.ground(px - chalf, pz - chalf, px + chalf, pz + chalf, ground, ground, p.foundation());
        for (int layer = 0; layer <= cheight; layer++) {
            int h = chalf - (layer * chalf) / cheight;
            if (h < 0) {
                break;
            }
            int y = ground + 1 + layer;
            b.fill(px - h, y, pz - h, px + h, y, pz + h,
                    (layer & 1) == 0 ? p.roof() : p.foundation());
        }
        b.put(px, ground + 1 + cheight, pz, p.accent());

        // A recumbent sphinx east of the pyramids, facing +X: lion body, outstretched paws, nemes.
        int rx = cx + 34;  // haunch / tail end (clear of the great pyramid's base)
        int hx = cx + 47;  // head end
        b.fill(rx, ground + 1, cz - 3, hx, ground + 5, cz + 3, p.roof());               // body
        b.fill(rx, ground + 1, cz - 2, rx + 4, ground + 7, cz + 2, p.roof());           // raised haunch
        b.fill(hx, ground + 1, cz - 3, hx + 6, ground + 2, cz + 3, p.roof());           // outstretched paws
        b.fill(hx - 1, ground + 6, cz - 2, hx + 2, ground + 10, cz + 2, p.accent());    // head
        b.fill(hx - 1, ground + 7, cz - 3, hx + 1, ground + 9, cz - 2, p.accent());     // nemes lappets
        b.fill(hx - 1, ground + 7, cz + 2, hx + 1, ground + 9, cz + 3, p.accent());
        b.put(hx + 2, ground + 9, cz - 1, p.window());                                  // eyes
        b.put(hx + 2, ground + 9, cz + 1, p.window());

        // A Mamluk minaret keeps the Cairo skyline recognisably Mamluk.
        mamlukMinaret(b, cx + 36, cz - 32, ground, p);
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
