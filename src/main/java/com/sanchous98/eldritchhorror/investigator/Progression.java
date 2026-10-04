package com.sanchous98.eldritchhorror.investigator;

import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * The investigator multipliers the per-second systems read, as fractions of the baseline
 * {@code 1.0}. {@code SanityTicker} scales sanity <b>drain</b> by {@link #sanityDrainMultiplier} and
 * {@code CorruptionTicker} scales corruption <b>gain</b> by {@link #corruptionGainMultiplier} —
 * recovery and cleansing are never penalised. {@link #maxSanityBonus} is the single source of the
 * bonus {@link InvestigatorAPI} applies to the {@code max_sanity} attribute.
 *
 * <p>Values from {@code design/14-classes.md}. Only bonuses whose systems exist are live here
 * (max sanity, sanity drain, corruption gain); the rest (city recovery, loot) are recorded in the
 * design doc for when their systems land.
 */
public final class Progression {

    private Progression() {
    }

    /** The investigator's max-sanity bonus fraction applied by {@link InvestigatorAPI} ({@code 0.0} = none). */
    public static double maxSanityBonus(Investigator id) {
        return switch (id) {
            case ELEANOR_VANCE -> 0.15;
            case MARION_DELACROIX -> 0.10;
            case NIKOLAI_VOLKOV -> -0.10;
            case JACK_CORRIGAN, TOM_MALLORY, CORMAC_BLACKWOOD, SISTER_AGATHA, VERA_NIGHTINGALE,
                 DR_AMOS_HARTLEY, EVELYN_ASHCOMBE, ALDOUS_PEMBERTON, HAZEL_QUINN -> 0.0;
        };
    }

    /** The investigator's sanity-drain multiplier ({@code 1.0} = no change). */
    public static double sanityDrainMultiplier(ServerPlayer player) {
        return switch (investigatorOf(player)) {
            case TOM_MALLORY -> 0.75;
            case CORMAC_BLACKWOOD -> 0.85;
            case NIKOLAI_VOLKOV -> 1.15;
            case HAZEL_QUINN -> 0.95;
            case ELEANOR_VANCE, JACK_CORRIGAN, SISTER_AGATHA, MARION_DELACROIX, VERA_NIGHTINGALE,
                 DR_AMOS_HARTLEY, EVELYN_ASHCOMBE, ALDOUS_PEMBERTON -> 1.0;
            case null -> 1.0;
        };
    }

    /** The investigator's corruption-gain multiplier ({@code 1.0} = no change). */
    public static double corruptionGainMultiplier(ServerPlayer player) {
        return switch (investigatorOf(player)) {
            case JACK_CORRIGAN -> 0.80;
            case CORMAC_BLACKWOOD -> 1.25;
            case SISTER_AGATHA -> 0.75;
            case VERA_NIGHTINGALE -> 1.25;
            case HAZEL_QUINN -> 0.95;
            case ELEANOR_VANCE, TOM_MALLORY, MARION_DELACROIX, NIKOLAI_VOLKOV, DR_AMOS_HARTLEY,
                 EVELYN_ASHCOMBE, ALDOUS_PEMBERTON -> 1.0;
            case null -> 1.0;
        };
    }

    private static @Nullable Investigator investigatorOf(ServerPlayer player) {
        return InvestigatorAPI.get(player);
    }
}
