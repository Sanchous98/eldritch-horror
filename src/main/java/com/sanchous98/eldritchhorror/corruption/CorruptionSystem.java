package com.sanchous98.eldritchhorror.corruption;

/**
 * Corruption / taint: a durable, per-player and per-chunk exposure value that grows from
 * forbidden knowledge, ritual use, and proximity to rifts or altars, and recedes very slowly.
 *
 * <p>Planned shape:
 * <ul>
 *   <li>A per-player corruption value with stages (Dormant → Touched → Marked → Claimed).</li>
 *   <li>A per-chunk corruption field that drives block conversion and ambient spawns.</li>
 *   <li>Content hooks so items, blocks and entities can declare how corrupt they are.</li>
 * </ul>
 *
 * <p>Corruption is meant to be the RPG "cost" axis: sanity can be restored, corruption mostly
 * cannot, and high corruption unlocks the darker rituals the cults offer.
 */
public final class CorruptionSystem {
    private CorruptionSystem() {
    }
}
