package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Lagos — Yoruba / West African with a modern overlay: ochre earth and mudbrick compound walls,
 * rusty corrugated-metal roofs, and bright painted trim (the signature green/yellow band over a
 * doorway). Culture leads; climate only supplies the overgrowth (jungle creepers, or kelp on the
 * lagoon).
 *
 * <p>Landmark: a walled <b>courtyard palace</b> — a colonnaded hall under a pitched metal roof,
 * ringed by a low compound wall and entered through an ornate painted gate. Street props: market
 * stalls with striped awnings, lantern posts and guardian statues. Follows {@link TokyoStyle};
 * shared shapes live in {@link StyleKit}.
 */
public final class LagosStyle implements CityStyle {

    /** The bright house-paint palette daubed on pillars, gates and trim. */
    private static final DyeColor[] PAINT = {
            DyeColor.GREEN, DyeColor.YELLOW, DyeColor.RED, DyeColor.CYAN, DyeColor.ORANGE
    };

    /** Market awning canvas. */
    private static final DyeColor[] AWNING = {
            DyeColor.RED, DyeColor.YELLOW, DyeColor.ORANGE, DyeColor.LIME, DyeColor.CYAN
    };

    /** A rotation of the painted-trim colours, deterministic in {@code i}. */
    private static BlockState paint(int i) {
        return Materials.concrete(PAINT[Math.floorMod(i, PAINT.length)]);
    }

    @Override
    public String id() {
        return "lagos";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: ochre earth, mudbrick and painted trim, whatever the Köppen class.
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.PACKED_MUD.defaultBlockState(),               // ground: beaten earth street
                Blocks.TERRACOTTA.defaultBlockState(),               // foundation: fired-clay footing
                Blocks.MUD_BRICKS.defaultBlockState(),               // wall: ochre mudbrick
                Materials.concrete(DyeColor.LIGHT_GRAY),             // weathered: faded plaster patch
                Materials.concrete(DyeColor.GREEN),                  // accent: bright painted trim
                Materials.terracotta(DyeColor.BROWN),                // roof: rusty corrugated sheet
                Blocks.BRICK_STAIRS.defaultBlockState(),             // roof stairs (folded sheet)
                Blocks.BRICK_SLAB.defaultBlockState(),               // roof slabs / eaves
                Materials.stainedPane(DyeColor.LIGHT_BLUE),          // window: tinted glazing
                Blocks.ACACIA_TRAPDOOR.defaultBlockState(),          // frame: carved timber louvre
                Blocks.DARK_OAK_DOOR.defaultBlockState(),            // door: heavy carved gate leaf
                Blocks.ACACIA_FENCE.defaultBlockState(),             // rail
                Blocks.LANTERN.defaultBlockState(),                  // light: hanging lantern
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.COARSE_DIRT.defaultBlockState());             // rubble: dry earth
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        palace(b, rng, cx, cz, ground, p);
    }

    /**
     * A walled palace compound: a paved court ringed by painted piers, a long pillared hall under
     * a pitched metal roof, and an ornate bright gate on the southern approach.
     */
    private static void palace(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        int w = 15;
        int d = 11;
        int h = 7;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - d / 2;
        int z1 = z0 + d - 1;
        int y0 = ground + 1;
        int y1 = y0 + h;

        // Paved courtyard, then the compound wall out to its edge.
        b.ground(cx - 13, cz - 12, cx + 13, cz + 16, ground, ground, p.ground());
        courtyard(b, cx, cz, 13, 12, ground, p);

        b.room(x0, y0, z0, x1, y1, z1, new Doorway(Side.S, w / 2));

        // Painted pillars down both long flanks — the bright daub of a Lagos compound.
        for (int x = x0 + 1; x <= x1 - 1; x += 2) {
            BlockState trim = paint(x);
            b.fill(x, y0, z0, x, y1, z0, trim);
            b.fill(x, y0, z1, x, y1, z1, trim);
        }
        // Tall windows between the pillars.
        for (int x = x0 + 3; x <= x1 - 3; x += 3) {
            b.window(x, y0 + 3, z0, 3, 1, true);
            b.window(x, y0 + 3, z1, 3, 1, true);
        }

        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y1, 4, 1);
        ornateGate(b, cx, z1 + 2, ground, p);
    }

    /** Painted piers spaced around a compound rectangle, with an opening on the south flank. */
    private static void courtyard(StructureBuilder b, int cx, int cz, int hx, int hz, int ground, Palette p) {
        int y = ground + 1;
        for (int x = cx - hx; x <= cx + hx; x += 3) {
            pier(b, x, cz - hz, y, p);
            if (Math.abs(x - cx) > 3) {
                pier(b, x, cz + hz, y, p); // leave the gateway clear
            }
        }
        for (int z = cz - hz; z <= cz + hz; z += 3) {
            pier(b, cx - hx, z, y, p);
            pier(b, cx + hx, z, y, p);
        }
    }

    /** A short painted compound pier with a slab cap. */
    private static void pier(StructureBuilder b, int x, int z, int y, Palette p) {
        for (int i = 0; i < 3; i++) {
            b.put(x, y + i, z, paint(x + z + i));
        }
        b.put(x, y + 3, z, p.roofSlab());
    }

    /** An ornate gate: two tall painted piers carrying a glazed crest, with bright gate leaves. */
    private static void ornateGate(StructureBuilder b, int cx, int z, int ground, Palette p) {
        int y = ground + 1;
        int top = y + 4;
        b.fill(cx - 3, y, z, cx - 3, top, z, p.wall());
        b.fill(cx + 3, y, z, cx + 3, top, z, p.wall());
        b.fill(cx - 3, y, z, cx - 3, y + 1, z, paint(1));
        b.fill(cx + 3, y, z, cx + 3, y + 1, z, paint(4));
        // Painted lintel and a glazed crest over the opening.
        b.fill(cx - 3, top + 1, z, cx + 3, top + 1, z, Materials.glazed(DyeColor.YELLOW));
        b.fill(cx - 1, top + 2, z, cx + 1, top + 2, z, p.roof());
        b.put(cx, top + 3, z, p.light());
        // Bright timber gate leaves.
        b.fill(cx - 2, y, z, cx - 1, y + 2, z, paint(0));
        b.fill(cx + 1, y, z, cx + 2, y + 2, z, paint(2));
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // The signature paint band under the eaves.
        b.fill(x, y1, z, x1, y1, z1, paint(rng.nextInt(PAINT.length)));
        // A corrugated awning over some facades, on short timber brackets.
        if (rng.nextFloat() < 0.5F) {
            b.fill(x - 1, y0 + 2, z - 1, x1 + 1, y0 + 2, z - 1, p.roof());
            for (int xx = x; xx <= x1; xx += 3) {
                b.put(xx, y0 + 1, z - 1, p.frame());
            }
        }
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Guardian statues flanking the palace approach.
        StyleKit.statue(b, cx - 9, cz + district - 30, ground, p);
        StyleKit.statue(b, cx + 9, cz + district - 30, ground, p);
        // Lantern posts down the two main axes, spaced deterministically.
        int step = 12 + rng.nextInt(3);
        for (int d = 26; d <= district - 12; d += step) {
            StyleKit.lanternPost(b, cx + d, cz - 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz + 3, ground, p);
        }
        // Market stalls clustered off the main street.
        for (int i = 0; i < 6; i++) {
            int sx = cx + rng.nextInt(district) - district / 2;
            int sz = cz + district / 2 - rng.nextInt(12);
            marketStall(b, sx, sz, ground, p, rng);
        }
    }

    /** A small market stall: a raised counter under a striped awning with a hanging light. */
    private static void marketStall(StructureBuilder b, int x, int z, int ground, Palette p, RandomSource rng) {
        int y = ground + 1;
        b.fill(x, y, z, x + 1, y, z + 1, p.foundation());          // counter
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                b.put(x + dx, y + 1, z + dz, p.accent());          // corner posts
                b.put(x + dx, y + 2, z + dz, p.accent());
            }
        }
        b.fill(x - 1, y + 3, z - 1, x + 2, y + 3, z + 2, Materials.wool(AWNING[rng.nextInt(AWNING.length)]));
        b.put(x, y + 2, z, p.light());                             // a lamp under the canvas
    }
}
