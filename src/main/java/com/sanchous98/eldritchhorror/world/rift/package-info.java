/**
 * Rift gates: the occult fast-travel branch (design/26, "The rift gate"; design/23, "Travel").
 *
 * <p>A gate is a standing {@code rift_anchor} block. A player who knows {@code open_rift} links two
 * gates by using one and then the other, and afterwards travels between them for a corruption and
 * sanity price. Each world (level) keeps its own {@link GateLinks} world data, so links are always
 * same-dimension; the pure cost/arrival arithmetic lives in {@link GateMath}.
 *
 * <p>Non-null by default: every reference in this package is non-null unless annotated
 * {@link org.jspecify.annotations.Nullable}.
 */
@NullMarked
package com.sanchous98.eldritchhorror.world.rift;

import org.jspecify.annotations.NullMarked;
