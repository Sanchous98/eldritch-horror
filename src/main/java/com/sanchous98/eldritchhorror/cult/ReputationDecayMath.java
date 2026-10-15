package com.sanchous98.eldritchhorror.cult;

/**
 * The drift arithmetic behind {@link ReputationDecayTicker}, kept deliberately Minecraft-free (the
 * {@code EarthMap} precedent) so {@code ReputationDecayMathTest} can load it in a bare JVM — the
 * ticker class itself references {@code ServerPlayer} and NeoForge events, which the test runtime
 * classpath does not carry.
 */
public final class ReputationDecayMath {

    private ReputationDecayMath() {
    }

    /**
     * The number of points to take from a standing of {@code value} idle for {@code idleTicks}: one
     * per full period, never more than the standing itself (so the result can reach 0 but never
     * cross below it), and never anything at all for a standing already at or below 0 — the drift
     * is one-way, toward 0 from above, and time alone does not forgive a grudge.
     */
    public static int decayPoints(int value, long idleTicks, long periodTicks) {
        if (value <= 0 || periodTicks <= 0 || idleTicks < periodTicks) {
            return 0;
        }
        return (int) Math.min(value, idleTicks / periodTicks);
    }
}
