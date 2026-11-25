package com.sanchous98.eldritchhorror.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Guards the liner's physical layout: the deck-floor AABB (top face is the deck at any heading),
 * the superstructure parts, the seat points, the chunk ticket radius and the gangway approach.
 *
 * <p>Everything read here is the same pure table {@code CoastalPacket}, {@code ShipCollisionPart}
 * and the renderer use, so the collision box, the riders and the drawn ship cannot drift apart.
 */
class ShipGeometryTest {

    @Test
    void theVesselIsALinerNotAPacketBoat() {
        assertTrue(ShipGeometry.LENGTH >= 88.0 && ShipGeometry.LENGTH <= 92.0,
                "hull length " + ShipGeometry.LENGTH);
        assertTrue(ShipGeometry.BEAM >= 18.0 && ShipGeometry.BEAM <= 20.0,
                "beam " + ShipGeometry.BEAM);
        assertTrue(ShipGeometry.DECK_Y - ShipGeometry.HULL_BOTTOM_Y >= 9.0,
                "hull depth " + (ShipGeometry.DECK_Y - ShipGeometry.HULL_BOTTOM_Y));
        assertTrue(ShipGeometry.DECK_ABOVE_WATER >= 6.0
                        && ShipGeometry.DECK_ABOVE_WATER <= 8.0,
                "deck above waterline " + ShipGeometry.DECK_ABOVE_WATER);
        assertTrue(ShipGeometry.ROOF_Y > ShipGeometry.DECK_Y, "promenade above the deck");
        assertTrue(ShipGeometry.BRIDGE_TOP_Y > ShipGeometry.ROOF_Y, "bridge above the roof");
    }

    @Test
    void theHullBoxTopIsTheDeckAtEveryHeading() {
        double x = 120.5;
        double y = 64.0;
        double z = -330.25;
        for (float yaw : new float[]{0.0F, 17.0F, 45.0F, 90.0F, 133.0F, 180.0F, 271.0F}) {
            ShipGeometry.Box box = ShipGeometry.hullBox(x, y, z, yaw);
            assertEquals(y + ShipGeometry.DECK_Y - ShipGeometry.HULL_FLOOR_THICKNESS,
                    box.minY(), 1.0E-9, "deck slab bottom at yaw " + yaw);
            assertEquals(y + ShipGeometry.DECK_Y, box.maxY(), 1.0E-9,
                    "deck top at yaw " + yaw);
            // Every corner of the rotated 91 x 18 deck must be inside the box.
            for (double localX : new double[]{-ShipGeometry.HALF_BEAM, ShipGeometry.HALF_BEAM}) {
                for (double localZ : new double[]{-ShipGeometry.HALF_LENGTH,
                        ShipGeometry.HALF_LENGTH}) {
                    double[] world = ShipGeometry.rotateLocal(localX, localZ, yaw);
                    assertTrue(box.contains(x + world[0], y + ShipGeometry.DECK_Y,
                                    z + world[1]),
                            "deck corner outside the hull box at yaw " + yaw);
                }
            }
        }
    }

    @Test
    void theAxisAlignedEnclosureIsAsTightAsAnEntityBoxCanBe() {
        // Side-on, the box is exactly the hull; diagonally it is the bounding square of the hull.
        ShipGeometry.Box side = ShipGeometry.hullBox(0.0, 0.0, 0.0, 0.0F);
        assertEquals(ShipGeometry.BEAM, side.width(), 1.0E-9);
        assertEquals(ShipGeometry.LENGTH, side.depth(), 1.0E-9);

        ShipGeometry.Box diagonal = ShipGeometry.hullBox(0.0, 0.0, 0.0, 45.0F);
        double expected = (ShipGeometry.BEAM + ShipGeometry.LENGTH) / Math.sqrt(2.0);
        assertEquals(expected, diagonal.width(), 1.0E-9);
        assertEquals(expected, diagonal.depth(), 1.0E-9);
    }

    @Test
    void aMooredLinerLiesBroadsideWithHerGangwayTowardTheQuay() {
        // Shore due east: the starboard side must face +X, the hull runs north-south.
        float east = ShipGeometry.dockedYaw(0.0, 0.0, 20.0, 0.0);
        assertEquals(0.0F, east, 1.0E-4F);
        double[] toShore = ShipGeometry.rotateLocal(1.0, 0.0, east);
        assertEquals(1.0, toShore[0], 1.0E-9, "gangway points east");
        assertEquals(0.0, toShore[1], 1.0E-9);

        // Shore due south: rotate a quarter turn; the bow runs east-west along the coast.
        float south = ShipGeometry.dockedYaw(0.0, 0.0, 0.0, 30.0);
        double[] southward = ShipGeometry.rotateLocal(1.0, 0.0, south);
        assertEquals(0.0, southward[0], 1.0E-9);
        assertEquals(1.0, southward[1], 1.0E-9);
        double[] bow = ShipGeometry.rotateLocal(0.0, 1.0, south);
        assertEquals(0.0, bow[1], 1.0E-9, "the bow is parallel to the shore");
    }

    @Test
    void theLocalFrameRotatesAndInvertsConsistently() {
        for (float yaw : new float[]{0.0F, 30.0F, 90.0F, -130.0F}) {
            double[] world = ShipGeometry.rotateLocal(3.5, -12.25, yaw);
            double[] local = ShipGeometry.toLocal(world[0], world[1], yaw);
            assertEquals(3.5, local[0], 1.0E-9, "x round trip at yaw " + yaw);
            assertEquals(-12.25, local[1], 1.0E-9, "z round trip at yaw " + yaw);
        }
    }

    @Test
    void theSuperstructureIsNineSolidPartsAboveTheDeck() {
        assertEquals(9, ShipGeometry.COLLISION_PARTS.size(),
                "eight deckhouse chunks and the bridge");
        for (ShipGeometry.PartSpec part : ShipGeometry.COLLISION_PARTS) {
            assertTrue(part.bottomY() >= ShipGeometry.DECK_Y - 1.0E-9,
                    "a part must not hang below the deck");
            assertTrue(part.topY() > part.bottomY(), "positive height");
            assertTrue(Math.abs(part.localX()) + part.halfX() <= ShipGeometry.HALF_BEAM + 1.0E-9,
                    "part stays inside the beam");
            assertTrue(Math.abs(part.localZ()) + part.halfZ()
                            <= ShipGeometry.HALF_LENGTH + 1.0E-9,
                    "part stays inside the hull length");
            assertTrue(ShipGeometry.surfaceY(part.localX(), part.localZ()) >= part.topY(),
                    "the surface above a part is at least its top");
        }
    }

    @Test
    void neighbouringPartsOverlapSoTheCabinWallHasNoSeams() {
        for (float yaw : new float[]{0.0F, 12.0F, 45.0F, 90.0F}) {
            for (int strip = 0; strip < 2; strip++) {
                for (int i = 1; i < 4; i++) {
                    int a = strip * 4 + i - 1;
                    ShipGeometry.Box left = ShipGeometry.partBox(
                            ShipGeometry.COLLISION_PARTS.get(a), 10.0, 70.0, -20.0, yaw);
                    ShipGeometry.Box right = ShipGeometry.partBox(
                            ShipGeometry.COLLISION_PARTS.get(a + 1), 10.0, 70.0, -20.0, yaw);
                    assertTrue(overlaps(left, right),
                            "deckhouse chunks " + a + "/" + (a + 1) + " gap at yaw " + yaw);
                }
                // The two beam strips must meet along the centreline.
                for (int i = 0; i < 4; i++) {
                    ShipGeometry.Box port = ShipGeometry.partBox(
                            ShipGeometry.COLLISION_PARTS.get(i), 10.0, 70.0, -20.0, yaw);
                    ShipGeometry.Box starboard = ShipGeometry.partBox(
                            ShipGeometry.COLLISION_PARTS.get(4 + i), 10.0, 70.0, -20.0, yaw);
                    assertTrue(overlaps(port, starboard),
                            "deckhouse beam strips " + i + " gap at yaw " + yaw);
                }
            }
        }
    }

    @Test
    void everyPartEnclosesItsRotatedBox() {
        double hullX = -40.0;
        double hullY = 63.5;
        double hullZ = 210.0;
        for (float yaw : new float[]{0.0F, 27.0F, 45.0F, 101.0F}) {
            for (ShipGeometry.PartSpec part : ShipGeometry.COLLISION_PARTS) {
                ShipGeometry.Box box = ShipGeometry.partBox(part, hullX, hullY, hullZ, yaw);
                for (double sx : new double[]{-1.0, 1.0}) {
                    for (double sz : new double[]{-1.0, 1.0}) {
                        double[] centre = ShipGeometry.rotateLocal(part.localX(), part.localZ(),
                                yaw);
                        double[] corner = ShipGeometry.rotateLocal(
                                part.localX() + sx * part.halfX(),
                                part.localZ() + sz * part.halfZ(), yaw);
                        assertInside(box, hullX + corner[0], hullY + part.bottomY(),
                                hullZ + corner[1], "bottom corner at yaw " + yaw);
                        assertInside(box, hullX + corner[0], hullY + part.topY(),
                                hullZ + corner[1], "top corner at yaw " + yaw);
                        assertInside(box, hullX + centre[0], hullY + part.topY(),
                                hullZ + centre[1], "part centre at yaw " + yaw);
                    }
                }
            }
        }
    }

    @Test
    void theChunkTicketCoversTheWholeDeck() {
        int needed = (int) Math.ceil(ShipGeometry.halfDiagonal() / 16.0);
        assertTrue(ShipGeometry.TICKET_RADIUS >= needed,
                "radius " + ShipGeometry.TICKET_RADIUS + " < " + needed);
        assertTrue(ShipGeometry.TICKET_RADIUS * 16.0 >= ShipGeometry.halfDiagonal(),
                "ticket block radius must reach the bow corner");
    }

    @Test
    void theGangwayIsAShortSteppedApproachFromThePier() {
        assertTrue(ShipGeometry.GANGWAY_INNER_X >= ShipGeometry.HALF_BEAM - 1.0E-9,
                "gangway starts at the deck edge");
        assertTrue(ShipGeometry.GANGWAY_OUTER_X > ShipGeometry.HALF_BEAM,
                "gangway projects from the hull");
        assertTrue(ShipGeometry.GANGWAY_BOTTOM_Y > 0.0
                        && ShipGeometry.GANGWAY_BOTTOM_Y < 1.5,
                "gangway foot just above the water");
        assertEquals(ShipGeometry.DECK_Y, ShipGeometry.surfaceY(ShipGeometry.HALF_BEAM,
                0.0), 1.0E-9);
        assertTrue(ShipGeometry.GANGWAY_Z_HALF * 2.0 <= ShipGeometry.LENGTH);
        assertFalse(ShipGeometry.onDeck(ShipGeometry.HALF_BEAM + 1.0, 0.0),
                "the gangway is not deck collision");
    }

    @Test
    void theDeckIsWalkableAndTheCabinBlockIsNot() {
        // Main deck fore and aft, promenade over the cabin, bridge on top of that.
        assertEquals(ShipGeometry.DECK_Y, ShipGeometry.surfaceY(0.0, 35.0), 1.0E-9);
        assertEquals(ShipGeometry.DECK_Y, ShipGeometry.surfaceY(0.0, -35.0), 1.0E-9);
        assertEquals(ShipGeometry.ROOF_Y, ShipGeometry.surfaceY(0.0, 0.0), 1.0E-9);
        assertEquals(ShipGeometry.BRIDGE_TOP_Y,
                ShipGeometry.surfaceY(0.0, ShipGeometry.BRIDGE_Z_CENTER), 1.0E-9);
        assertTrue(ShipGeometry.onDeck(0.0, 0.0), "the hull deck exists under the cabin");
        assertTrue(ShipGeometry.onDeck(8.0, 30.0), "the deck reaches the bulwark");
        assertFalse(ShipGeometry.onDeck(8.8, 30.0), "the bulwark margin is not walkable");
        assertFalse(ShipGeometry.onDeck(9.1, 30.0), "nothing walkable outside the beam");
        assertFalse(ShipGeometry.onDeck(0.0, 46.0), "nothing walkable past the stem");
    }

    private static void assertInside(ShipGeometry.Box box, double x, double y, double z,
                                     String message) {
        double e = 1.0E-9;
        assertTrue(x >= box.minX() - e && x <= box.maxX() + e
                        && y >= box.minY() - e && y <= box.maxY() + e
                        && z >= box.minZ() - e && z <= box.maxZ() + e,
                message + " outside its enclosing box");
    }

    private static boolean overlaps(ShipGeometry.Box a, ShipGeometry.Box b) {
        return a.minX() <= b.maxX() && b.minX() <= a.maxX()
                && a.minZ() <= b.maxZ() && b.minZ() <= a.maxZ();
    }
}
