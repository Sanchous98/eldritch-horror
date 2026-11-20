/**
 * Scheduled sea transport ({@code design/30-transport.md}, stage 1): curated harbours, cached A* sea
 * lanes over the baked landmask, the passenger packet's timetable, and the mid-voyage logout rule.
 *
 * <p>The lane and timetable maths are pure and unit-tested; the server-side service only spawns and
 * prunes hulls, while a spawned {@code CoastalPacket} owns its movement and its 5x5 chunk ticket.
 * There is no lane-wide chunk loading and no teleport: the ship physically crosses the water.
 *
 * <p>Non-null by default: every reference in this package is non-null unless annotated
 * {@link org.jspecify.annotations.Nullable}.
 */
@NullMarked
package com.sanchous98.eldritchhorror.world.transport;

import org.jspecify.annotations.NullMarked;
