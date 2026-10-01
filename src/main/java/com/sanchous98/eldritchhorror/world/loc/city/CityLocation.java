package com.sanchous98.eldritchhorror.world.loc.city;

import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import com.sanchous98.eldritchhorror.world.loc.Tier;
import com.sanchous98.eldritchhorror.world.loc.style.CityStyle;
import com.sanchous98.eldritchhorror.world.loc.style.CityStyles;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * The reference {@link Tier#METROPOLIS}: an irregular, decaying gothic district for a curated
 * city. A clear silhouette upgrade over a box grid — every building has a steep pitched roof,
 * quoins and tall narrow windows, a few sprout spires or buttresses, and one cathedral-spire
 * dominates the skyline over a plaza with a monument.
 *
 * <p>All deterministic (seeded by {@link #id()}) and chunk-clipped: the layout is generated the
 * same way for every chunk, and {@link StructureBuilder} drops writes outside the generating
 * chunk. Only {@link Palette} blocks are used — no hardcoded block types.
 *
 * <p>Decay (see "Atmosphere rules"): weathered courses low on walls, overgrowth on wet walls,
 * rubble at foundations, cobwebs in the corners, and punched holes in a fraction of buildings.
 */
public final class CityLocation implements Location {

    /** Layout pitch of the jittered building grid (blocks). */
    private static final int CELL = 13;
    /** Built district half-extent (blocks); the cull radius ({@link #radius()}) can be larger. */
    private static final int DISTRICT_CAP = 380;
    /** Plaza half-extent: wide enough to seat the enlarged style landmarks on paving. */
    private static final int PLAZA = 30;
    /** Radius kept clear of ordinary buildings so the (now large) landmark has room. */
    private static final int INNER_CLEAR = 46;
    /**
     * Height above the surface cleared of vanilla vegetation inside the district, so a forest or
     * jungle city is not buried by the biome's own trees (which run before this generator's city
     * pass). Taller than the tallest tree we expect to remove; buildings are placed afterwards.
     */
    private static final int CLEAR_ABOVE = 40;
    /** Minimum width (blocks) of the graded rim that blends the flat city into the wild terrain. */
    private static final int EDGE_RING_MIN = 24;
    /** Maximum rim width: on very steep sites the slope widens (flatter) but never eats the city. */
    private static final int EDGE_RING_MAX = 64;

    private final City city;

    public CityLocation(City city) {
        this.city = city;
    }

    /** The city this location represents (used to resolve its cultural style). */
    public City city() {
        return this.city;
    }

    /** Half-extent in blocks, scaled from real population: {@code clamp(220 + sqrt(pop)/40, 220, 400)}. */
    @Override
    public int radius() {
        double r = 220.0 + Math.sqrt(Math.max(this.city.population(), 1)) / 40.0;
        int rounded = (int) Math.round(r);
        return Math.max(220, Math.min(400, rounded));
    }

    /** The built district half-extent (blocks), not the larger cull radius. */
    @Override
    public int renderRadius() {
        return Math.min(radius(), DISTRICT_CAP);
    }

    @Override
    public String id() {
        return "eldritch_horror:city/" + this.city.id();
    }

    @Override
    public Tier tier() {
        return Tier.METROPOLIS;
    }

    @Override
    public void build(StructureBuilder b) {
        int cx = this.city.x();
        int cz = this.city.z();
        int district = Math.min(radius(), DISTRICT_CAP);
        int plaza = PLAZA;
        // The city level is the MEAN terrain over the flat interior, not the single centre point:
        // a centre-only level can sit far above or below most of a district that sprawls over a
        // hill, forcing a huge cut and a terraced edge. The mean balances cut and fill so the
        // flattening stays shallow wherever possible (e.g. Lima cuts 115 blocks at the centre
        // alone, but only a few with the mean). b.groundY is pure (no rng), so this stays
        // deterministic per column and the chunk-local loop below can reuse it.
        // The rim width adapts to how rugged the site is: a flat site needs only a short blend,
        // while a steep one gets a wider, gentler slope so the flattening never reads as a cliff.
        int interior = district - EDGE_RING_MIN;
        int ground = meanLevel(b, cx, cz, interior);
        int edgeRing = edgeRingWidth(b, cx, cz, interior, ground);
        Palette p = b.palette();
        RandomSource rng = b.rng();
        CityStyle style = CityStyles.forCity(this.city.name());

        int inner = INNER_CLEAR;
        // The flat interior is edgeRing smaller than the district, and lots need another ~10 blocks
        // for their footprint/roof overhang, so buildings and street furniture stay fully on the
        // flat ground and off the graded transition ring.
        int buildRadius = district - edgeRing - 10;

        // 1. Plaza: a paved apron at the heart of the city.
        b.ground(cx - plaza, cz - plaza, cx + plaza, cz + plaza, ground - 2, ground, p.ground());
        b.ground(cx - plaza, cz - plaza, cx + plaza, cz + plaza, ground + 1, ground + 1, p.ground());

        // 1b. Pave the whole district on land, so the city reads as urban fabric rather than
        // scattered buildings on wild terrain. Chunk-clipped: only this chunk's columns write.
        paveDistrict(b, cx, cz, district, plaza, p, ground, edgeRing);

        // 2. Landmark: the cultural skyline piece (cathedral / temple / mosque / pagoda …).
        style.landmark(b, rng, cx, cz, ground, p);

        // 3. Plaza monument, off the landmark axis.
        b.monument(cx + plaza - 5, cz + plaza - 5, ground + 1);

        // 3b. Generic street dressing shared by every culture: stalls, crates and a courtyard well
        // or two. Placed BEFORE buildings, so it can never punch through a wall, a door or a road;
        // every write is also gated on the cell being air/replaceable. Off the landmark plaza and
        // inside the paved fabric, deterministic like the rest.
        streetDetails(b, rng, cx, cz, buildRadius, inner, ground, p);

        // 4. Buildings on a jittered grid — irregular blocks and 2–3 wide alleys, never a grid.
        // Chunk-local and grid-canonical: each cell's jitter and vacancy come from a position hash
        // (not the per-chunk rng), and we only build cells whose jittered origin lands inside THIS
        // chunk. The layout is therefore identical in every chunk that overlaps a building, costs
        // O(chunk), and never leaves half the district empty the way a global "built < MAX" cap did.
        int cellMinX = -buildRadius - 2;
        int cellMaxX = buildRadius + 2;
        int cellMinZ = -buildRadius - 2;
        int cellMaxZ = buildRadius + 2;
        int chunkMaxX = b.chunkMinX() + 15;
        int chunkMaxZ = b.chunkMinZ() + 15;
        for (int gx = cellMinX; gx <= cellMaxX; gx += CELL) {
            int bx = cx + gx;
            if (bx + 2 < b.chunkMinX() || bx - 2 > chunkMaxX) {
                continue; // no jitter of this cell can fall in this chunk
            }
            for (int gz = cellMinZ; gz <= cellMaxZ; gz += CELL) {
                int bz = cz + gz;
                if (bz + 2 < b.chunkMinZ() || bz - 2 > chunkMaxZ) {
                    continue;
                }
                int h = hash(cx, cz, gx, gz);
                int x = bx + ((h >>> 8) % 5) - 2;
                int z = bz + ((h >>> 13) % 5) - 2;
                if (x < b.chunkMinX() || x > chunkMaxX || z < b.chunkMinZ() || z > chunkMaxZ) {
                    continue; // this building is drawn by the chunk that owns its origin
                }
                int dx = x - cx;
                int dz = z - cz;
                if (dx * dx + dz * dz > buildRadius * buildRadius) {
                    continue;
                }
                if (Math.abs(dx) < inner && Math.abs(dz) < inner) {
                    continue; // keep the landmark and plaza clear
                }
                if (((h >>> 5) & 0xFF) > 209) {
                    continue; // ~18% vacant lots
                }
                building(b, rng, x, z, ground, p, style);
            }
        }

        // 5. Street furniture for the culture (lanterns, torii, neon, statues…). Also before the
        // buildings, so it cannot overwrite them, and bounded to the flattened interior.
        style.streetProps(b, rng, cx, cz, buildRadius, ground, p);

        b.marker("city_center", cx, ground + 1, cz);
    }

    // ------------------------------------------------------------------ pieces

    /**
     * A stable position hash for the building grid: mixes the city centre and cell coordinates into
     * a 32-bit value, so cell jitter and vacancy are pure functions of (city, cell) and the layout
     * is identical in every chunk that overlaps a lot.
     */
    private static int hash(int cx, int cz, int gx, int gz) {
        int h = cx * 0x9E3779B9 ^ cz * 0x85EBCA6B ^ gx * 0xC2B2AE35 ^ gz * 0x27D4EB2F;
        h ^= h >>> 15;
        h *= 0x2C1B3C6D;
        h ^= h >>> 12;
        return h;
    }

    /**
     * The city's build level: the mean terrain height over the flat interior, sampled on a grid.
     * Using the mean instead of the single centre point keeps cut-and-fill balanced, so a district
     * on a slope is levelled gently rather than cut in half. Pure ({@code b.groundY} only, no rng),
     * so it is identical for every generating chunk.
     */
    private static int meanLevel(StructureBuilder b, int cx, int cz, int radius) {
        long sum = 0;
        int n = 0;
        int step = 16;
        for (int dx = -radius; dx <= radius; dx += step) {
            for (int dz = -radius; dz <= radius; dz += step) {
                if (dx * dx + dz * dz > radius * radius) {
                    continue;
                }
                int x = cx + dx;
                int z = cz + dz;
                if (b.isLand(x, z)) {
                    sum += b.groundY(x, z);
                    n++;
                }
            }
        }
        return n == 0 ? b.groundY(cx, cz) : (int) Math.round(sum / (double) n);
    }

    /**
     * The rim width for this site: proportional to how far the terrain departs from the city level,
     * so a steep site gets a wide gentle slope (about one block of drop per two blocks of run) and
     * a flat site keeps a short blend. Clamped so it never consumes the whole district. Pure.
     */
    private static int edgeRingWidth(StructureBuilder b, int cx, int cz, int radius, int ground) {
        int maxDeparture = 0;
        int step = 16;
        for (int dx = -radius; dx <= radius; dx += step) {
            for (int dz = -radius; dz <= radius; dz += step) {
                if (dx * dx + dz * dz > radius * radius) {
                    continue;
                }
                int x = cx + dx;
                int z = cz + dz;
                if (!b.isLand(x, z)) {
                    continue;
                }
                maxDeparture = Math.max(maxDeparture, Math.abs(b.groundY(x, z) - ground));
            }
        }
        return Math.clamp(maxDeparture * 2, EDGE_RING_MIN, EDGE_RING_MAX);
    }

    /**
     * Paves the district on land only: a ground course on every land column within the district
     * radius. This gives the city a continuous urban surface (streets and courts) instead of
     * buildings floating on untouched terrain. The rim grades the height from the city level out to
     * the natural terrain over {@code edgeRing} blocks. Deterministic and chunk-clipped.
     */
    private static void paveDistrict(StructureBuilder b, int cx, int cz,
                                     int district, int plaza, Palette p, int ground, int edgeRing) {
        // Only the columns of the chunk currently generating are visited (O(256) per chunk),
        // so paving costs the same regardless of district size.
        int x0 = b.chunkMinX();
        int z0 = b.chunkMinZ();
        for (int x = x0; x < x0 + 16; x++) {
            int dx = x - cx;
            if (Math.abs(dx) > district) {
                continue;
            }
            for (int z = z0; z < z0 + 16; z++) {
                int dz = z - cz;
                if (dx * dx + dz * dz > district * district) {
                    continue;
                }
                if (!b.isLand(x, z)) {
                    continue;
                }
                int surface = b.groundY(x, z);
                int dist2 = dx * dx + dz * dz;
                int flat = district - edgeRing;
                int flat2 = flat * flat;
                if (dist2 > flat2) {
                    // Transition ring: blend from the flat city level to the natural terrain
                    // instead of cutting a vertical wall. 0 at the flat edge, 1 at the rim.
                    double t = Math.clamp((Math.sqrt(dist2) - flat) / (double) edgeRing, 0.0, 1.0);
                    int target = (int) Math.round(ground * (1.0 - t) + surface * t);
                    if (surface > target) {
                        b.fill(x, target + 1, z, x, surface, z, Blocks.AIR.defaultBlockState());
                    } else if (surface < target) {
                        b.fill(x, surface + 1, z, x, target, z, p.foundation());
                    }
                    b.put(x, target, z, p.ground());
                    int clearTop = Math.max(surface, target) + 4;
                    for (int y = target + 1; y <= clearTop; y++) {
                        b.put(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                    continue;
                }
                // Flat interior: cut hills down, fill hollows up, then cap every column with one
                // paving course.
                if (surface > ground) {
                    b.fill(x, ground + 1, z, x, surface, z, Blocks.AIR.defaultBlockState());
                } else if (surface < ground) {
                    b.fill(x, surface + 1, z, x, ground, z, p.foundation());
                }
                b.put(x, ground, z, streetSurface(x, z, cx, cz, plaza, p));
                // Clear the vanilla vegetation the biome decoration planted here (trees/leaves/
                // grass). Clear all the way up to the ORIGINAL surface plus CLEAR_ABOVE, not a
                // fixed cap: where a hill was cut down, its trees sat above the old surface, so a
                // ground-relative cap left their trunks dangling in the air.
                int clearTop = Math.max(surface, ground) + CLEAR_ABOVE;
                for (int y = ground + 1; y <= clearTop; y++) {
                    b.put(x, y, z, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    /**
     * The paving block for one street column: {@link Palette#ground()} as the base, with a small
     * deterministic texture scattered by a position hash — worn {@link Palette#rubble()} patches
     * and slightly different {@link Palette#foundation()} pavers. A pure hash of {@code (x,z)} (no
     * {@link RandomSource}) keeps the stream consumed by the rest of the layout unchanged and the
     * texture stable regardless of chunk order. The plaza is skipped so its deliberate, uniform
     * {@code ground()} apron is never clobbered.
     */
    private static BlockState streetSurface(int x, int z, int cx, int cz, int plaza, Palette p) {
        int dx = x - cx;
        int dz = z - cz;
        if (Math.abs(dx) <= plaza && Math.abs(dz) <= plaza) {
            return p.ground();
        }
        int h = (x * 0x9E3779B9) ^ (z * 0x85EBCA6B);
        h ^= h >>> 16;
        h *= 0x7FEB352D;
        h ^= h >>> 15;
        int v = h & 0x7F;
        if (v < 6) {
            return p.rubble();      // ~5% worn patch
        }
        if (v < 14) {
            return p.foundation();  // ~6% a different paver
        }
        return p.ground();
    }

    /**
     * Shared, culture-neutral street dressing: a handful of market stalls, loose crates and a
     * couple of courtyard wells, scattered deterministically in a band beyond the landmark
     * plaza. Everything sits on the district surface (already paved by {@link #paveDistrict});
     * the plaza proper (|dx| ≤ plaza && |dz| ≤ plaza) is deliberately left untouched so the
     * landmark reads clean. A few dozen blocks at most, chunk-clipped like the rest.
     */
    private static void streetDetails(StructureBuilder b, RandomSource rng, int cx, int cz,
                                      int buildRadius, int inner, int ground, Palette p) {
        int plaza = PLAZA;
        int outer = buildRadius - 6; // keep the whole prop inside the flat built fabric

        // A few market stalls in the mid ring, clear of alleys and the plaza.
        for (int i = 0; i < 7; i++) {
            int x = cx + rng.nextInt(2 * outer + 1) - outer;
            int z = cz + rng.nextInt(2 * outer + 1) - outer;
            int dx = x - cx;
            int dz = z - cz;
            if (dx * dx + dz * dz > outer * outer) {
                continue; // outside the paved fabric
            }
            if (inLandmarkClear(dx, dz, inner)) {
                continue; // off the landmark / plaza keep-out box
            }
            // The whole 3x3 stall footprint must be land and stand on replaceable (open) ground.
            if (!landBox(b, x, z, x + 2, z + 2) || !b.isReplaceable(x, ground + 1, z)) {
                continue;
            }
            marketStall(b, rng, x, z, ground, p);
        }

        // Loose crates: single foundation blocks on the paved surface, never on the plaza.
        for (int i = 0; i < 24; i++) {
            int x = cx + rng.nextInt(2 * outer + 1) - outer;
            int z = cz + rng.nextInt(2 * outer + 1) - outer;
            int dx = x - cx;
            int dz = z - cz;
            if (dx * dx + dz * dz > outer * outer) {
                continue;
            }
            if (inLandmarkClear(dx, dz, inner) || onPlaza(x, z, cx, cz, plaza)) {
                continue;
            }
            if (!b.isLand(x, z) || !b.isReplaceable(x, ground + 1, z)) {
                continue; // never overwrite a wall, a door or a landmark with a crate
            }
            b.put(x, ground + 1, z, p.foundation());
            if (rng.nextFloat() < 0.4f && b.isReplaceable(x, ground + 2, z)) {
                b.put(x, ground + 2, z, p.foundation());
            }
        }

        // One or two small paved courtyards, each with a curb ring and a dark mouth (a well).
        int wells = 1 + rng.nextInt(2);
        for (int i = 0; i < wells; i++) {
            int x = cx + rng.nextInt(2 * outer + 1) - outer;
            int z = cz + rng.nextInt(2 * outer + 1) - outer;
            int dx = x - cx;
            int dz = z - cz;
            if (dx * dx + dz * dz > outer * outer) {
                continue;
            }
            if (inLandmarkClear(dx, dz, inner) || onPlaza(x, z, cx, cz, plaza)) {
                continue;
            }
            // half is 2..3, so a 3-block box fully covers the court before it is laid.
            if (!landBox(b, x - 3, z - 3, x + 3, z + 3)) {
                continue; // never hang a paved court over water
            }
            courtyardWell(b, rng, x, z, ground, p);
        }
    }

    /** True if every column in the inclusive box {@code [x0..x1] × [z0..z1]} is real land. */
    private static boolean landBox(StructureBuilder b, int x0, int z0, int x1, int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                if (!b.isLand(x, z)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** True if (x,z) lies inside the landmark plaza apron (inclusive of its edge). */
    private static boolean onPlaza(int x, int z, int cx, int cz, int plaza) {
        return Math.abs(x - cx) <= plaza && Math.abs(z - cz) <= plaza;
    }

    /** True if the offset (dx,dz) falls in the square the landmark/buildings keep clear. */
    private static boolean inLandmarkClear(int dx, int dz, int inner) {
        return Math.abs(dx) < inner && Math.abs(dz) < inner;
    }

    /**
     * A market stall: two posts carrying a roofSlab awning, with a foundation counter beneath.
     * Compact (3×3) and culture-neutral, so it can drop into any city's street fabric.
     */
    private static void marketStall(StructureBuilder b, RandomSource rng, int x, int z,
                                    int ground, Palette p) {
        BlockState post = rng.nextBoolean() ? p.accent() : p.wall();
        // Counter along one diagonal, two posts at the other corners.
        b.put(x, ground + 1, z, p.foundation());
        b.put(x + 1, ground + 1, z, p.foundation());
        b.put(x, ground + 1, z + 1, p.foundation());
        b.put(x + 1, ground + 1, z + 1, post);
        b.put(x + 1, ground + 2, z + 1, post);
        b.put(x, ground + 2, z, post);
        // Two more posts so every awning slab has support beneath it (no floating roof).
        b.put(x + 2, ground + 1, z + 1, post);
        b.put(x + 2, ground + 2, z + 1, post);
        b.put(x + 1, ground + 1, z + 2, post);
        b.put(x + 1, ground + 2, z + 2, post);
        b.put(x, ground + 3, z, p.roofSlab());
        b.put(x + 1, ground + 3, z + 1, p.roofSlab());
        b.put(x + 2, ground + 3, z + 1, p.roofSlab());
        b.put(x + 1, ground + 3, z + 2, p.roofSlab());
        b.put(x + 1, ground + 3, z, p.roofSlab());
        b.put(x, ground + 3, z + 1, p.roofSlab());
        if (rng.nextFloat() < 0.5f) {
            b.put(x, ground + 2, z + 1, p.light());
        }
    }

    /** A small paved court with a low well: a cobbled apron, a curb ring and a dark mouth. */
    private static void courtyardWell(StructureBuilder b, RandomSource rng, int x, int z,
                                      int ground, Palette p) {
        int half = 2 + rng.nextInt(2); // 2..3 -> a 5..7 wide court
        b.ground(x - half, z - half, x + half, z + half, ground, ground, p.ground());
        b.fill(x - 1, ground + 1, z - 1, x + 1, ground + 1, z + 1, p.foundation());
        b.put(x, ground + 1, z, p.ground());
        b.put(x, ground + 1, z - 2, p.light());
    }

    /** One lot: an ordinary house, a tall tower, an open square, or a walled garden. */
    private static void building(StructureBuilder b, RandomSource rng, int x, int z, int ground,
                                 Palette p, CityStyle style) {
        if (!b.isLand(x, z)) {
            return; // never build on water
        }
        // Break the "carpet of identical roofs": a few lots are not buildings at all, and the
        // rest vary strongly in height and roof silhouette.
        float lot = rng.nextFloat();
        if (lot < 0.05f) {
            square(b, rng, x, z, ground, p);
            return;
        }
        if (lot < 0.10f) {
            garden(b, rng, x, z, ground, p);
            return;
        }
        if (lot < 0.12f) {
            tower(b, rng, x, z, ground, p, style);
            return;
        }
        house(b, rng, x, z, ground, p, style);
    }

    /** An ordinary building: varied footprint, strongly varied height, flat or pitched roof. */
    private static void house(StructureBuilder b, RandomSource rng, int x, int z, int ground,
                              Palette p, CityStyle style) {
        int w = 5 + rng.nextInt(5);   // 5..9 across X
        int d = 5 + rng.nextInt(5);   // 5..9 across Z
        int h;
        float hr = rng.nextFloat();
        if (hr < 0.15f) {
            h = 2 + rng.nextInt(3);        // low shed / workshop
        } else if (hr < 0.75f) {
            h = 4 + rng.nextInt(4);        // 4..7 ordinary house
        } else {
            h = 7 + rng.nextInt(4);        // 7..10 tenement, taller than its neighbours
        }
        int x1 = x + w - 1;
        int z1 = z + d - 1;

        int gy = ground;
        int y0 = gy;                  // interior floor is flush with the street, so the door is walkable
        int y1 = y0 + h;

        // Paved margin acts as the alley surface.
        b.ground(x - 1, z - 1, x1 + 1, z1 + 1, gy - 1, gy, p.ground());

        // Shell + hollow interior + floor.
        Side doorSide = Side.values()[rng.nextInt(4)];
        int doorOffset = 1 + rng.nextInt(Math.max(1, (doorSide == Side.N || doorSide == Side.S ? w : d) - 2));
        b.room(x, y0, z, x1, y1, z1, new StructureBuilder.Doorway(doorSide, doorOffset));

        // Foundation course and corner quoins.
        b.fill(x, y0, z, x1, y0, z1, p.foundation());
        for (int y = y0; y < y1; y++) {
            b.put(x, y, z, p.accent());
            b.put(x1, y, z, p.accent());
            b.put(x, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }

        // Roof silhouette: a low flat parapet here and there, otherwise a steep ridge whose
        // height varies a lot; some get a chimney.
        if (rng.nextFloat() < 0.28f) {
            b.fill(x - 1, y1, z - 1, x1 + 1, y1, z1 + 1, p.roofSlab());
            b.crenellations(x - 1, z - 1, x1 + 1, z1 + 1, y1 + 1);
        } else {
            int roofH = 2 + rng.nextInt(6);
            int axis = w >= d ? 0 : 1;
            b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, y1, roofH, axis);
            if (rng.nextFloat() < 0.5f) {
                int chx = x + 1 + rng.nextInt(Math.max(1, w - 2));
                int chz = z + 1 + rng.nextInt(Math.max(1, d - 2));
                b.put(chx, y1 + roofH, chz, p.accent());
                b.put(chx, y1 + roofH + 1, chz, p.accent());
            }
        }

        // Tall narrow windows, a row on every storey (not just the ground floor), so a tall facade
        // is not a blank wall.
        for (int wy = y0 + 2; wy <= y1 - 2; wy += 3) {
            for (int wx = x + 2; wx <= x1 - 2; wx += 3) {
                b.window(wx, wy, z, 3, 2, true);
                b.window(wx, wy, z1, 3, 2, true);
            }
            for (int wz = z + 2; wz <= z1 - 2; wz += 3) {
                b.window(x, wy, wz, 3, 1, true);
                b.window(x1, wy, wz, 3, 1, true);
            }
        }

        // Some buildings sprout a small spire or lean on a buttress.
        float roll = rng.nextFloat();
        if (roll < 0.18f) {
            b.spire((x + x1) / 2, (z + z1) / 2, y1 + 1, 4 + rng.nextInt(4));
        } else if (roll < 0.5f) {
            Side out = Side.values()[rng.nextInt(4)];
            switch (out) {
                case N -> b.buttress(x, z, y0, Math.max(2, h / 2), Side.N);
                case S -> b.buttress(x1, z1, y0, Math.max(2, h / 2), Side.S);
                case W -> b.buttress(x, z1, y0, Math.max(2, h / 2), Side.W);
                case E -> b.buttress(x1, z, y0, Math.max(2, h / 2), Side.E);
            }
        }

        decay(b, rng, x, y0, z, x1, y1, z1, p);
        style.flourish(b, rng, x, y0, z, x1, y1, z1, p);

        // Door LAST, so windows/decay/flourish cannot overwrite it; it also re-carves its opening
        // and lays a threshold so the doorway is walkable from the street.
        placeDoor(b, p, x, z, x1, z1, y0 + 1, doorSide, doorOffset);

        // A single guttering light in some buildings only.
        if (rng.nextFloat() < 0.3f) {
            b.put((x + x1) / 2, y1 - 2, (z + z1) / 2, p.light());
        }
    }

    /** A tall, narrow tower/watchtower: the vertical accents that break the roofline. */
    private static void tower(StructureBuilder b, RandomSource rng, int x, int z, int ground,
                              Palette p, CityStyle style) {
        int w = 4 + rng.nextInt(3);   // 4..6
        int d = 4 + rng.nextInt(3);   // 4..6
        int h = 12 + rng.nextInt(9);  // 12..20 — well above the houses
        int x1 = x + w - 1;
        int z1 = z + d - 1;

        int gy = ground;
        int y0 = gy;                  // floor flush with the street (walkable door)
        int y1 = y0 + h;

        b.ground(x - 1, z - 1, x1 + 1, z1 + 1, gy - 1, gy, p.ground());
        Side doorSide = Side.values()[rng.nextInt(4)];
        int doorOffset = 1 + rng.nextInt(Math.max(1, (doorSide == Side.N || doorSide == Side.S ? w : d) - 2));
        b.room(x, y0, z, x1, y1, z1, new StructureBuilder.Doorway(doorSide, doorOffset));
        for (int y = y0; y <= y1; y++) {
            b.put(x, y, z, p.accent());
            b.put(x1, y, z, p.accent());
            b.put(x, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }

        // Stacked windows up the shaft, and small lights near the top (a beacon).
        for (int yy = y0 + 3; yy <= y1 - 3; yy += 4) {
            b.window((x + x1) / 2, yy, z, 2, 1, true);
            b.window((x + x1) / 2, yy, z1, 2, 1, true);
            b.window(x, yy, (z + z1) / 2, 2, 1, true);
            b.window(x1, yy, (z + z1) / 2, 2, 1, true);
        }
        b.crenellations(x - 1, z - 1, x1 + 1, z1 + 1, y1 + 1);
        b.spire((x + x1) / 2, (z + z1) / 2, y1 + 2, 6 + rng.nextInt(6));
        b.put((x + x1) / 2, y1 - 1, (z + z1) / 2, p.light());

        // A weathered skirt and rubble so it does not look freshly built.
        b.scatter(x - 1, z - 1, x1 + 1, z1 + 1, y0, y0, p.rubble(), 0.15f);
        style.flourish(b, rng, x, y0, z, x1, y1, z1, p);

        // Door LAST so windows do not overwrite it; re-carves its opening.
        placeDoor(b, p, x, z, x1, z1, y0 + 1, doorSide, doorOffset);
    }

    /**
     * An open square: paved, with a low well/curb and a pair of lights. Deliberately leaves a
     * hole in the built fabric so a district does not read as one solid carpet of roofs.
     */
    private static void square(StructureBuilder b, RandomSource rng, int x, int z, int ground, Palette p) {
        int half = 5 + rng.nextInt(3);   // a 10..16-wide paved court
        int gy = ground;
        b.ground(x - half, z - half, x + half, z + half, gy - 1, gy, p.ground());
        // A low curb ring with a dark mouth — a well.
        b.fill(x - 1, gy + 1, z - 1, x + 1, gy + 1, z + 1, p.foundation());
        b.put(x, gy + 1, z, p.ground());
        b.put(x, gy + 1, z - 2, p.light());
        b.put(x, gy + 1, z + 2, p.light());
        if (p.overgrowth() != null) {
            b.scatter(x - half, z - half, x + half, z + half, gy + 1, gy + 1, p.overgrowth(), 0.03f);
        }
    }

    /** A walled garden/courtyard: low walls, overgrowth and a single stunted tree. */
    private static void garden(StructureBuilder b, RandomSource rng, int x, int z, int ground, Palette p) {
        int w = 6 + rng.nextInt(4);   // 6..9
        int d = 6 + rng.nextInt(4);
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int gy = ground;
        b.ground(x - 1, z - 1, x1 + 1, z1 + 1, gy - 1, gy, p.ground());
        b.walls(x, gy + 1, z, x1, gy + 2, z1, p.weathered());
        BlockState leaf = p.overgrowth() != null ? p.overgrowth() : p.accent();
        b.scatter(x, z, x1, z1, gy + 1, gy + 2, leaf, 0.20f);
        int tx = (x + x1) / 2;
        int tz = (z + z1) / 2;
        b.put(tx, gy + 1, tz, p.accent());
        b.put(tx, gy + 2, tz, leaf);
        b.put(tx, gy + 3, tz, leaf);
        if (rng.nextFloat() < 0.5f) {
            b.put(x + 1, gy + 1, z + 1, p.light());
        }
    }

    /** The decay pass: weathering, overgrowth, rubble, cobwebs and punched holes. */
    private static void decay(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                              int x1, int y1, int z1, Palette p) {
        // Weathered courses low on the walls.
        for (int y = y0 + 1; y <= y0 + 2 && y < y1; y++) {
            for (int wx = x; wx <= x1; wx++) {
                if (rng.nextFloat() < 0.3f) {
                    b.put(wx, y, z, p.weathered());
                }
                if (rng.nextFloat() < 0.3f) {
                    b.put(wx, y, z1, p.weathered());
                }
            }
            for (int wz = z; wz <= z1; wz++) {
                if (rng.nextFloat() < 0.3f) {
                    b.put(x, y, wz, p.weathered());
                }
                if (rng.nextFloat() < 0.3f) {
                    b.put(x1, y, wz, p.weathered());
                }
            }
        }

        // Rubble piled at the foundations (only lands on the air of the outer margin).
        b.scatter(x - 1, z - 1, x1 + 1, z1 + 1, y0, y0, p.rubble(), 0.12f);

        // Overgrowth crawling up the wet walls.
        if (p.overgrowth() != null) {
            b.scatter(x - 1, z - 1, x1 + 1, z1 + 1, y0 + 1, y0 + 3, p.overgrowth(), 0.08f);
        }

        // Cobwebs in the upper interior corners.
        b.scatter(x + 1, z + 1, x1 - 1, z1 - 1, y1 - 3, y1 - 1, Palette.cobweb(), 0.06f);

        // A fraction of buildings have collapsed: punch holes in walls and the roof.
        if (rng.nextFloat() < 0.22f) {
            b.ruins(x, y0 + 1, z, x1, y1, z1, 0.10f);
            b.ruins(x - 1, y1 + 1, z - 1, x1 + 1, y1 + 2, z1 + 1, 0.12f);
        }
    }

    // ------------------------------------------------------------------ helpers

    private static void placeDoor(StructureBuilder b, Palette p, int x0, int z0, int x1, int z1,
                                  int y, Side side, int offset) {
        int dx;
        int dz;
        Direction facing;
        switch (side) {
            case N -> { dx = x0 + offset; dz = z0; facing = Direction.NORTH; }
            case S -> { dx = x0 + offset; dz = z1; facing = Direction.SOUTH; }
            case W -> { dx = x0; dz = z0 + offset; facing = Direction.WEST; }
            default -> { dx = x1; dz = z0 + offset; facing = Direction.EAST; }
        }
        BlockState air = Blocks.AIR.defaultBlockState();
        // Carve the passage so nothing (quoin, window, rubble) blocks it.
        b.put(dx, y, dz, air);
        b.put(dx, y + 1, dz, air);
        BlockState lower = p.door()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        b.put(dx, y, dz, lower);
        b.put(dx, y + 1, dz, lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
    }
}
