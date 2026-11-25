package com.sanchous98.eldritchhorror.entity;

import java.util.List;

/**
 * The ocean liner's physical layout, independent of Minecraft classes.
 *
 * <p>A ship entity has exactly <b>one</b> {@code AABB}, and it cannot be rotated. The vessel
 * therefore uses a clean composite:
 *
 * <ul>
 *   <li>the hull's own box is the deck floor slab: one block of planking whose
 *       <b>top face is the walkable main deck</b>, so the sea is never "walked on" through a roof
 *       collider and a tall invisible volume cannot trap players on the quay;</li>
 *   <li>the box is computed from the current heading ({@link #hullBox}), so it always contains
 *       the whole 91-block deck (an axis-aligned enclosing box, as an entity cannot rotate);
 *   <li>the superstructure is a handful of {@link PartSpec}s, each carried by an invisible
 *       {@code ShipCollisionPart} entity that follows the hull every tick. They are split into
 *       two beam strips by four length chunks so their enclosing boxes stay close to the deckhouse
 *       and do not swallow the open fore/aft decks on a diagonal heading.</li>
 * </ul>
 *
 * <p>Local frame: {@code +Z} is the bow, {@code +X} is starboard, {@code +Y} is up, and the origin
 * is the hull's waterline reference at {@code waterSurface + 0.85}. All values are blocks.
 *
 * <p>Pure Java on purpose: the entity, the collision parts, the renderer and the headless tests all
 * read the same table, so the physical layout cannot drift from what is drawn.
 */
public final class ShipGeometry {

    /** Overall deck length from stern counter to stem, in blocks. */
    public static final double LENGTH = 91.0;
    /** Half of {@link #LENGTH}. */
    public static final double HALF_LENGTH = LENGTH / 2.0;
    /** Beam (width) at the deck edge. */
    public static final double BEAM = 18.0;
    /** Half beam. */
    public static final double HALF_BEAM = BEAM / 2.0;

    /**
     * Deck top above the entity origin. The origin spawns at {@code waterY + 0.85}, so the main
     * deck sits {@link #DECK_ABOVE_WATER} blocks above the sea.
     */
    public static final double DECK_Y = 6.4;
    /** World-space deck height above the waterline at the spawn datum. */
    public static final double DECK_ABOVE_WATER = DECK_Y + 0.85;
    /** Keel bottom, relative to the origin (the hull is about ten blocks deep in total). */
    public static final double HULL_BOTTOM_Y = -2.75;
    /** Top of the bulwark/cap rail above the deck. */
    public static final double BULWARK_TOP_Y = DECK_Y + 1.15;
    /**
     * Thickness of the hull's collidable deck floor. Keeping it to the planking lets the ship's
     * box sit above a quay instead of trapping players who are merely standing on the pier.
     */
    public static final double HULL_FLOOR_THICKNESS = 1.0;

    /** Cabin block: 44 blocks long, 16 wide, from the deck to the promenade roof. */
    public static final double DECKHOUSE_Z_HALF = 22.0;
    public static final double DECKHOUSE_X_HALF = 8.0;
    /** Promenade / boat deck on top of the cabin block. */
    public static final double ROOF_Y = 13.6;
    /** Navigation bridge, sitting on the promenade deck at the front. */
    public static final double BRIDGE_Z_CENTER = 15.0;
    public static final double BRIDGE_Z_HALF = 5.0;
    public static final double BRIDGE_X_HALF = 7.0;
    public static final double BRIDGE_TOP_Y = 16.6;

    /** Deck length that stays at full beam before the bow/stern counters taper. */
    public static final double FULL_BEAM_Z = 38.0;
    /** Half beam at the very tip of the bow/stern counter. */
    public static final double TIP_BEAM = 4.0;

    /** Passenger seats: sixteen on the foredeck, sixteen on the aft deck. */
    public static final int SEATS = 32;
    /**
     * How far a seated rider's feet sink into the planking. The passenger vehicle attachment is
     * 0.6 above the rider's feet, so the attachment point is {@code deck - 0.3 + 0.6}.
     */
    public static final double SEAT_SINK = 0.3;
    /** {@code Avatar.DEFAULT_VEHICLE_ATTACHMENT.y}: rider feet are this far below the attachment. */
    public static final double RIDER_VEHICLE_ATTACHMENT_Y = 0.6;
    /** Passenger attachment height for a main-deck seat. */
    public static final double RIDE_HEIGHT =
            DECK_Y - SEAT_SINK + RIDER_VEHICLE_ATTACHMENT_Y;

    /**
     * Chunk ticket radius. The half diagonal of the 91 x 18 deck is 46.4 blocks, so a radius-3
     * ticket (7 x 7 chunks, 112 blocks across) covers the whole vessel under any heading.
     */
    public static final int TICKET_RADIUS = 3;

    /** Gangway: the stepped approach on the starboard side amidships. */
    public static final double GANGWAY_INNER_X = HALF_BEAM;
    public static final double GANGWAY_OUTER_X = 16.0;
    public static final double GANGWAY_Z_HALF = 3.0;
    public static final double GANGWAY_BOTTOM_Y = 0.2;

    /**
     * Invisible collision parts for the superstructure. Each one is a local box that follows the
     * hull; the deckhouse is split into two beam strips by four length chunks, so a diagonal
     * heading cannot inflate one box over the open decks.
     */
    public static final List<PartSpec> COLLISION_PARTS = buildCollisionParts();

    private ShipGeometry() {
    }

    private static List<PartSpec> buildCollisionParts() {
        // Two beam strips x four length chunks: a narrow strip's enclosing box does not inflate
        // far along the deck when the heading is diagonal, so the open decks stay walkable.
        double halfBeam = DECKHOUSE_X_HALF / 2.0;
        double chunk = DECKHOUSE_Z_HALF * 2.0 / 4.0;
        double halfZ = chunk / 2.0;
        PartSpec[] parts = new PartSpec[9];
        for (int strip = 0; strip < 2; strip++) {
            double x = strip == 0 ? -halfBeam : halfBeam;
            for (int i = 0; i < 4; i++) {
                double centre = -DECKHOUSE_Z_HALF + halfZ + i * chunk;
                parts[strip * 4 + i] = new PartSpec(x, centre, halfBeam, halfZ,
                        DECK_Y, ROOF_Y);
            }
        }
        parts[8] = new PartSpec(0.0, BRIDGE_Z_CENTER, BRIDGE_X_HALF, BRIDGE_Z_HALF,
                ROOF_Y, BRIDGE_TOP_Y);
        return List.of(parts);
    }

    /** @return the deck half beam at a local {@code z}; the counters taper past full beam. */
    public static double halfBeamAt(double z) {
        double distance = Math.abs(z);
        if (distance <= FULL_BEAM_Z) {
            return HALF_BEAM;
        }
        double t = Math.min(1.0, (distance - FULL_BEAM_Z) / (HALF_LENGTH - FULL_BEAM_Z));
        return HALF_BEAM + (TIP_BEAM - HALF_BEAM) * t;
    }

    /** @return whether a local deck point is inside the planked footprint (with a small margin). */
    public static boolean onDeck(double localX, double localZ) {
        return Math.abs(localZ) <= HALF_LENGTH - 0.5
                && Math.abs(localX) <= halfBeamAt(localZ) - 0.3;
    }

    /**
     * The world point of a hull-local offset.
     *
     * @param localX starboard offset
     * @param localZ forward offset
     * @param yaw    hull yaw in degrees (Minecraft convention)
     * @return {world x offset, world z offset}
     */
    public static double[] rotateLocal(double localX, double localZ, float yaw) {
        double radians = Math.toRadians(yaw);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new double[]{
                localX * cos - localZ * sin,
                localX * sin + localZ * cos
        };
    }

    /**
     * The heading a moored liner lies on: parallel to the shore, starboard (gangway) side toward
     * the quay. Local +X maps to {@code (cos yaw, sin yaw)}, so the yaw points the gangway at the
     * shore.
     *
     * @param waterX berth x
     * @param waterZ berth z
     * @param shoreX nearest land x
     * @param shoreZ nearest land z
     * @return yaw in degrees
     */
    public static float dockedYaw(double waterX, double waterZ, double shoreX, double shoreZ) {
        return (float) Math.toDegrees(Math.atan2(shoreZ - waterZ, shoreX - waterX));
    }

    /**
     * The inverse of {@link #rotateLocal}: a world offset back into the hull's local frame.
     *
     * @param worldX world x offset from the hull centre
     * @param worldZ world z offset from the hull centre
     * @param yaw    hull yaw in degrees
     * @return {local x (starboard), local z (forward)}
     */
    public static double[] toLocal(double worldX, double worldZ, float yaw) {
        double radians = Math.toRadians(yaw);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new double[]{
                worldX * cos + worldZ * sin,
                -worldX * sin + worldZ * cos
        };
    }

    /**
     * The hull's own AABB: the deck-floor slab. Its top face is the walkable main deck at
     * {@code originY + DECK_Y}.
     *
     * @param x    entity x
     * @param y    entity origin y
     * @param z    entity z
     * @param yaw  heading in degrees
     * @return the axis-aligned enclosure of the rotated deck rectangle
     */
    public static Box hullBox(double x, double y, double z, float yaw) {
        double radians = Math.toRadians(yaw);
        double cos = Math.abs(Math.cos(radians));
        double sin = Math.abs(Math.sin(radians));
        double halfX = HALF_BEAM * cos + HALF_LENGTH * sin;
        double halfZ = HALF_BEAM * sin + HALF_LENGTH * cos;
        double bottom = y + DECK_Y - HULL_FLOOR_THICKNESS;
        return new Box(x - halfX, bottom, z - halfZ, x + halfX, y + DECK_Y, z + halfZ);
    }

    /**
     * The world AABB of one collision part. It is the axis-aligned enclosure of that part's rotated
     * local box, so the parts always overlap and never leave a gap in the cabin wall.
     *
     * @param spec  part definition
     * @param hullX hull x
     * @param hullY hull origin y
     * @param hullZ hull z
     * @param yaw   heading in degrees
     * @return the part's world box
     */
    public static Box partBox(PartSpec spec, double hullX, double hullY, double hullZ, float yaw) {
        double[] centre = rotateLocal(spec.localX(), spec.localZ(), yaw);
        double radians = Math.toRadians(yaw);
        double cos = Math.abs(Math.cos(radians));
        double sin = Math.abs(Math.sin(radians));
        double halfX = spec.halfX() * cos + spec.halfZ() * sin;
        double halfZ = spec.halfX() * sin + spec.halfZ() * cos;
        double x = hullX + centre[0];
        double z = hullZ + centre[1];
        return new Box(x - halfX, hullY + spec.bottomY(), z - halfZ,
                x + halfX, hullY + spec.topY(), z + halfZ);
    }

    /**
     * @param localX starboard offset of a walker
     * @param localZ forward offset of a walker
     * @return the local height of the highest walkable surface there
     */
    public static double surfaceY(double localX, double localZ) {
        if (insideRect(localX, localZ, 0.0, BRIDGE_Z_CENTER, BRIDGE_X_HALF, BRIDGE_Z_HALF)) {
            return BRIDGE_TOP_Y;
        }
        if (insideRect(localX, localZ, 0.0, 0.0, DECKHOUSE_X_HALF, DECKHOUSE_Z_HALF)) {
            return ROOF_Y;
        }
        return DECK_Y;
    }

    private static boolean insideRect(double x, double z, double centreX, double centreZ,
                                      double halfX, double halfZ) {
        return Math.abs(x - centreX) <= halfX && Math.abs(z - centreZ) <= halfZ;
    }

    /** @return the half diagonal of the deck, the radius a chunk ticket must cover. */
    public static double halfDiagonal() {
        return Math.hypot(HALF_LENGTH, HALF_BEAM);
    }

    /** A rotated local box the hull carries as its superstructure. */
    public record PartSpec(double localX, double localZ, double halfX, double halfZ,
                           double bottomY, double topY) {
    }

    /** An axis-aligned world box, in the same shape as {@code net.minecraft.world.phys.AABB}. */
    public record Box(double minX, double minY, double minZ,
                      double maxX, double maxY, double maxZ) {

        /** @return whether the point is inside, borders included. */
        public boolean contains(double x, double y, double z) {
            return x >= this.minX && x <= this.maxX
                    && y >= this.minY && y <= this.maxY
                    && z >= this.minZ && z <= this.maxZ;
        }

        /** @return the depth along X. */
        public double width() {
            return this.maxX - this.minX;
        }

        /** @return the depth along Z. */
        public double depth() {
            return this.maxZ - this.minZ;
        }

        /** @return the height along Y. */
        public double height() {
            return this.maxY - this.minY;
        }
    }
}
