package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
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
                bio.overgrowth(),
                Blocks.COARSE_DIRT.defaultBlockState());            // rubble: dry red debris
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // KICC: a broad, low podium carrying a tall cylindrical shaft that flares outward into an
        // inverted-cone crown, capped by a slender spire. Deliberately over-tall on the skyline.
        b.ground(cx - 22, cz - 22, cx + 22, cz + 22, ground, ground, p.foundation());

        // Low podium: a battered cylindrical drum, radius 13, with a solid roof deck.
        for (int y = ground + 1; y <= ground + 8; y++) {
            int rr = 13 - (y - (ground + 1)) / 4;
            for (int dx = -rr; dx <= rr; dx++) {
                for (int dz = -rr; dz <= rr; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 <= rr * rr && d2 > (rr - 1) * (rr - 1)) {
                        b.put(cx + dx, y, cz + dz, p.wall());
                    }
                }
            }
        }
        for (int dx = -13; dx <= 13; dx++) {
            for (int dz = -13; dz <= 13; dz++) {
                if (dx * dx + dz * dz <= 169) {
                    b.put(cx + dx, ground + 8, cz + dz, p.foundation());
                }
            }
        }
        // Podium glazing and the four entrances.
        int wy = ground + 4;
        b.window(cx - 13, wy, cz, 3, 2, true);
        b.window(cx + 13, wy, cz, 3, 2, true);
        b.window(cx, wy, cz - 13, 3, 2, true);
        b.window(cx, wy, cz + 13, 3, 2, true);
        StyleKit.twoHighDoor(b, p, cx, cz + 13, ground + 1, Direction.SOUTH);
        StyleKit.twoHighDoor(b, p, cx, cz - 13, ground + 1, Direction.NORTH);
        StyleKit.twoHighDoor(b, p, cx + 13, cz, ground + 1, Direction.EAST);
        StyleKit.twoHighDoor(b, p, cx - 13, cz, ground + 1, Direction.WEST);

        // The shaft: a 46-block cylindrical tower, radius 5, banded with glazing and vertical ribs.
        int shaft0 = ground + 9;
        int shaft1 = ground + 54;
        for (int y = shaft0; y <= shaft1; y++) {
            boolean band = ((y - shaft0) % 11) == 0;
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 <= 25 && d2 > 16) {
                        b.put(cx + dx, y, cz + dz, band ? p.window() : p.wall());
                    }
                }
            }
        }
        for (int y = shaft0; y <= shaft1; y++) {
            b.put(cx + 5, y, cz, p.accent());
            b.put(cx - 5, y, cz, p.accent());
            b.put(cx, y, cz + 5, p.accent());
            b.put(cx, y, cz - 5, p.accent());
            b.put(cx + 3, y, cz + 4, p.accent());
            b.put(cx - 3, y, cz + 4, p.accent());
            b.put(cx + 3, y, cz - 4, p.accent());
            b.put(cx - 3, y, cz - 4, p.accent());
        }

        // The flared crown: a solid inverted cone widening from radius 5 to 14, accent-rimmed.
        int crown0 = shaft1 + 1;
        int crownH = 9;
        for (int dy = 0; dy <= crownH; dy++) {
            int y = crown0 + dy;
            int r = 5 + (dy * 9) / crownH;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 <= r * r) {
                        b.put(cx + dx, y, cz + dz,
                                d2 > (r - 1) * (r - 1) ? p.accent() : p.wall());
                    }
                }
            }
        }
        int topY = crown0 + crownH;
        for (int dx = -14; dx <= 14; dx++) {
            for (int dz = -14; dz <= 14; dz++) {
                if (dx * dx + dz * dz <= 196) {
                    b.put(cx + dx, topY + 1, cz + dz, p.roof());
                }
            }
        }

        // The spire crowning the flare.
        b.spire(cx, cz, topY + 2, 16);

        // Sparse debris over the plaza fringe.
        b.scatter(cx - 22, cz - 22, cx + 22, cz + 22, ground + 1, ground + 1, p.rubble(), 0.04f);
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
