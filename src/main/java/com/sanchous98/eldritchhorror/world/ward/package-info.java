/**
 * Wards: the server-authoritative "haven" a warding rite raises (design/26, "Warding and sealing").
 *
 * <p>A ward is a place, not a potion effect: it suppresses sanity drain, repels freshly spawned
 * lesser horrors and opens a damage window against an Ancient One standing in the fight. The
 * ledger is per-level world data and the effects are read only on the server.
 *
 * <p>Non-null by default: every reference in this package is non-null unless annotated
 * {@link org.jspecify.annotations.Nullable}.
 */
@NullMarked
package com.sanchous98.eldritchhorror.world.ward;

import org.jspecify.annotations.NullMarked;
