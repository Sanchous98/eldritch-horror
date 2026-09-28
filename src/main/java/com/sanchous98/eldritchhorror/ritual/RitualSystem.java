package com.sanchous98.eldritchhorror.ritual;

/**
 * Rituals: the mod's quest-like progression engine. A ritual is a named, data-driven recipe
 * (altar + surrounding runes/offerings + a time/condition window) that resolves to a set of
 * outcomes (spawn, transform, grant, curse).
 *
 * <p>Planned shape:
 * <ul>
 *   <li>A {@code RitualDefinition} datapack type (JSON) with a block-pattern matcher.</li>
 *   <li>A resolver that validates the pattern and consumes inputs.</li>
 *   <li>Outcome effects as a small registry so content can add its own.</li>
 * </ul>
 *
 * <p>Rituals are intentionally dangerous: a failed or refused ritual should cost sanity or
 * corruption rather than simply do nothing.
 */
public final class RitualSystem {
    private RitualSystem() {
    }
}
