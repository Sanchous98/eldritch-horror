package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mumbai — Victorian-Gothic + Maratha + Art-Deco port city. Dark Deccan basalt walls with pale
 * carved Indo-Saracenic arches, Art-Deco bands, crowded market stalls and a harbour feel.
 *
 * <p>Landmark: a grand arched gateway (Gateway of India silhouette) — two massive basalt piers
 * carrying a semicircular arch, crowned with a low gable and domed turrets. Street props: a
 * clock tower, striped market stalls, statues and lantern posts.
 *
 * <p>Deterministic and cheap: only {@link StructureBuilder} + {@link Palette} blocks (plus
 * {@link StyleKit}/{@link Materials}); all randomness comes from the passed {@link RandomSource}.
 */
public final class MumbaiStyle implements CityStyle {

    @Override
    public String id() {
        return "mumbai";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: dark basalt shell with pale carved stone trim, regardless of Köppen.
        // Climate only supplies the overgrowth (monsoon vines; the coast swaps it for kelp).
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.COBBLED_DEEPSLATE.defaultBlockState(),        // ground: basalt paving
                Blocks.SMOOTH_BASALT.defaultBlockState(),            // foundation: basalt base course
                Blocks.DEEPSLATE_BRICKS.defaultBlockState(),         // wall: dark Deccan basalt
                Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState(), // weathered (rain-worn) basalt
                Blocks.CHISELED_STONE_BRICKS.defaultBlockState(),    // accent: carved Indo-Saracenic trim
                Blocks.DEEPSLATE_TILES.defaultBlockState(),          // roof: dark slate/basalt tile
                Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState(),    // roof stairs
                Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState(),      // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),               // window
                Blocks.DARK_OAK_TRAPDOOR.defaultBlockState(),        // frame: carved balcony lattice
                Blocks.DARK_OAK_DOOR.defaultBlockState(),            // door
                Blocks.IRON_BARS.defaultBlockState(),                // rail: Victorian / Deco ironwork
                Blocks.LANTERN.defaultBlockState(),                  // light
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                  // rubble
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        gateway(b, cx, cz, ground, p);
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // Art-Deco band: a pale chevron course just below the eaves.
        int band = y1 - 1;
        if (band <= y0 || x1 - x < 2) {
            return;
        }
        if (rng.nextFloat() < 0.55f) {
            BlockState deco = Materials.concrete(DyeColor.LIGHT_GRAY);
            b.fill(x + 1, band, z, x1 - 1, band, z, deco);
            b.fill(x + 1, band, z1, x1 - 1, band, z1, deco);
        }
        // A narrow iron balcony on one facade over the street.
        if (rng.nextFloat() < 0.35f) {
            int by = y0 + 2;
            switch (rng.nextInt(4)) {
                case 0 -> b.fill(x + 1, by, z - 1, x1 - 1, by, z - 1, p.rail());
                case 1 -> b.fill(x + 1, by, z1 + 1, x1 - 1, by, z1 + 1, p.rail());
                case 2 -> b.fill(x - 1, by, z + 1, x - 1, by, z1 - 1, p.rail());
                default -> b.fill(x1 + 1, by, z + 1, x1 + 1, by, z1 - 1, p.rail());
            }
        }
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // A clock tower marks one corner of the plaza.
        clockTower(b, cx + 34, cz - 34, ground, p);

        // Statues on the four approaches.
        int s = Math.max(24, district - 30);
        StyleKit.statue(b, cx, cz - s, ground, p);
        StyleKit.statue(b, cx, cz + s, ground, p);
        StyleKit.statue(b, cx - s, cz, ground, p);
        StyleKit.statue(b, cx + s, cz, ground, p);

        // Lantern posts down the two main axes.
        for (int d = 26; d <= district - 12; d += 14) {
            StyleKit.lanternPost(b, cx + d, cz + 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 3, ground, p);
        }

        // Crowded bazaar rows: striped market stalls flanking both market streets.
        int variant = 0;
        for (int d = 30; d <= district - 30; d += 16) {
            int jz = rng.nextInt(3);
            marketStall(b, cx + d, cz + 22 + jz, ground, p, variant++);
            marketStall(b, cx - d, cz - 22 - jz, ground, p, variant++);
        }
    }

    // ------------------------------------------------------------------ Indo-Saracenic forms

    /**
     * The Gateway of India: two massive basalt piers joined by a semicircular arch, crowned with
     * a low gable, crenellations and two domed turrets (via {@link StyleKit#dome}).
     */
    private static void gateway(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int w = 23;
        int d = 11;
        int h = 15;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - d / 2;
        int z1 = z0 + d - 1;
        int y0 = ground + 1;
        int y1 = y0 + h;

        b.ground(x0 - 3, z0 - 3, x1 + 3, z1 + 3, ground, ground, p.foundation());

        // Two solid piers, leaving the central bay as the passage.
        int pier = 6;
        int px = x1 - pier + 1;
        b.fill(x0, y0, z0, x0 + pier - 1, y1, z1, p.wall());
        b.fill(px, y0, z0, x1, y1, z1, p.wall());

        // Quoined corner pilasters on the outer and inner corners of each pier.
        for (int y = y0; y <= y1; y++) {
            b.put(x0, y, z0, p.accent());
            b.put(x0, y, z1, p.accent());
            b.put(x1, y, z0, p.accent());
            b.put(x1, y, z1, p.accent());
            b.put(x0 + pier - 1, y, z0, p.accent());
            b.put(x0 + pier - 1, y, z1, p.accent());
            b.put(px, y, z0, p.accent());
            b.put(px, y, z1, p.accent());
        }

        // The great arch spans the bay; paved passage runs through it, north to south.
        int ax0 = x0 + pier;
        int ax1 = px - 1;
        b.ground(ax0, z0, ax1, z1, ground, ground, p.ground());
        archSpan(b, ax0, ax1, y0 + 3, y1, z0, z1, p);

        // Tall Gothic slits high on each pier.
        b.window(x0 + 2, y0 + 5, z0, 5, 1, true);
        b.window(x0 + 2, y0 + 5, z1, 5, 1, true);
        b.window(x1 - 2, y0 + 5, z0, 5, 1, true);
        b.window(x1 - 2, y0 + 5, z1, 5, 1, true);

        // Crown: parapet, a low gable over the arch and two domed turrets.
        b.crenellations(x0, z0, x1, z1, y1 + 1);
        b.pitchedRoof(ax0 - 1, z0 + 1, ax1 + 1, z1 - 1, y1 + 1, 3, 1);
        StyleKit.dome(b, x0 + pier / 2, cz, y1 + 2, 2, p);
        StyleKit.dome(b, px + (pier - 1) / 2, cz, y1 + 2, 2, p);
    }

    /** A semicircular arch bridging two piers: accent voussoirs on the facades, solid above. */
    private static void archSpan(StructureBuilder b, int x0, int x1, int springY, int topY,
                                 int z0, int z1, Palette p) {
        int r = (x1 - x0) / 2;
        int mx = (x0 + x1) / 2;
        for (int i = -r; i <= r; i++) {
            int rise = (int) Math.floor(Math.sqrt((double) (r * r - i * i)));
            int y = springY + rise;
            // The curved ceiling, full depth.
            b.fill(mx + i, y, z0 + 1, mx + i, y, z1 - 1, p.wall());
            // Carved voussoirs on the two facades.
            b.put(mx + i, y, z0, p.accent());
            b.put(mx + i, y, z1, p.accent());
            // Solid mass above the arch up to the wall head.
            for (int yy = y + 1; yy <= topY; yy++) {
                b.fill(mx + i, yy, z0, mx + i, yy, z1, p.wall());
            }
        }
        // Keystone at the crown of the arch.
        b.put(mx, springY + r, z0, p.accent());
        b.put(mx, springY + r, z1, p.accent());
    }

    /** A slender Decò clock tower: a basalt shaft with four dials, crenellations and a dome cap. */
    private static void clockTower(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int r = 2;
        int y0 = ground + 1;
        int top = y0 + 18;

        b.ground(cx - r - 1, cz - r - 1, cx + r + 1, cz + r + 1, ground, ground, p.foundation());
        b.room(cx - r, y0, cz - r, cx + r, top, cz + r);

        // A pale clock dial on each of the four faces, high up.
        int dy = top - 5;
        BlockState dial = Materials.concrete(DyeColor.WHITE);
        for (int i = -1; i <= 1; i++) {
            b.put(cx + i, dy, cz - r, dial);
            b.put(cx + i, dy + 2, cz - r, dial);
            b.put(cx + i, dy, cz + r, dial);
            b.put(cx + i, dy + 2, cz + r, dial);
            b.put(cx - r, dy, cz + i, dial);
            b.put(cx - r, dy + 2, cz + i, dial);
            b.put(cx + r, dy, cz + i, dial);
            b.put(cx + r, dy + 2, cz + i, dial);
        }
        b.put(cx, dy + 1, cz - r, p.light());
        b.put(cx, dy + 1, cz + r, p.light());

        b.crenellations(cx - r - 1, cz - r - 1, cx + r + 1, cz + r + 1, top + 1);
        StyleKit.dome(b, cx, cz, top + 2, 2, p);
    }

    /** A striped market stall: fence posts, a counter, an awning and a hanging lamp. */
    private static void marketStall(StructureBuilder b, int x, int z, int ground, Palette p,
                                    int variant) {
        for (int dx = 0; dx <= 2; dx += 2) {
            for (int dz = 0; dz <= 2; dz += 2) {
                for (int y = ground + 1; y <= ground + 3; y++) {
                    b.put(x + dx, y, z + dz, p.rail());
                }
            }
        }
        b.fill(x, ground + 2, z + 1, x + 2, ground + 2, z + 1, p.frame());
        BlockState a = Materials.wool((variant & 1) == 0 ? DyeColor.WHITE : DyeColor.RED);
        BlockState c = Materials.wool((variant & 1) == 0 ? DyeColor.RED : DyeColor.WHITE);
        for (int dx = -1; dx <= 3; dx++) {
            for (int dz = -1; dz <= 3; dz++) {
                b.put(x + dx, ground + 4, z + dz, ((dx + dz) & 1) == 0 ? a : c);
            }
        }
        b.put(x + 1, ground + 3, z + 1, p.light());
    }
}
