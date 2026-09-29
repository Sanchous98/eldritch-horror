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
        // A domed prayer hall flanked by two crenellated minarets.
        StyleKit.mosque(b, rng, cx, cz, ground, p);
        // Arcaded courtyard on the south side, clear of the northern minarets.
        arcade(b, cx - 12, cx + 12, cz + 14, ground, p);
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
