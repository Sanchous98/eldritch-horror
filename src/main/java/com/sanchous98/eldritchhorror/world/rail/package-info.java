/**
 * Scheduled rail transport ({@code design/30-transport.md}, stage 2): curated station sites, cached
 * A* rail lines over the baked landmask, real block track (ballast, sleepers, bridges, tunnels) and
 * the consist's coupling maths.
 *
 * <p>The line finder and the coupling/timetable maths are pure and unit-tested; the server-side
 * service only spawns and prunes consists, while a spawned locomotive owns its movement and its
 * 5x5 chunk ticket. There is no line-wide chunk loading and no teleport: the train physically
 * crosses the ground on its rails.
 *
 * <p>Non-null by default: every reference in this package is non-null unless annotated
 * {@link org.jspecify.annotations.Nullable}.
 */
@NullMarked
package com.sanchous98.eldritchhorror.world.rail;

import org.jspecify.annotations.NullMarked;
