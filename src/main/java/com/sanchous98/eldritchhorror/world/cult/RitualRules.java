package com.sanchous98.eldritchhorror.world.cult;

/**
 * The game-free rules of an NPC ritual cell ({@code design/06-factions.md}, "Ritual cells", pinned
 * 2026-11): the GATHER → CHANT timing, the interruption precedence and the completion tier. Kept
 * apart from {@link CultRituals} so the state machine can be asserted without a running server.
 *
 * <p>Interruption precedence is deliberate and ordered: a dead leader outranks everything (the cell
 * cannot be led), then a broken core or ring, then a player standing inside the ring, then too few
 * cultists. The first failing condition is the one announced and rewarded, so the counter-play is
 * legible rather than a pile of simultaneous causes.
 */
public final class RitualRules {

    /** The two ritual phases; the cell begins in {@link #GATHER}. */
    public enum State {
        /** Cultists walk to the ring; ends after the gather timer or once enough arrive. */
        GATHER,
        /** Cultists hold the ring, particles and hum run, the ritual resolves after its timer. */
        CHANT
    }

    /** Why a ritual stopped early. */
    public enum Interruption {
        /** The ritual is still running. */
        NONE,
        /** The leading cultist died or vanished. */
        LEADER,
        /** The altar core is gone or its ring no longer validates. */
        CORE,
        /** A player stands inside the ring. */
        PLAYER,
        /** Too many cultists have died or left. */
        TOO_FEW
    }

    /** What a completed ritual does. */
    public enum Completion {
        /** No completion (unused sentinel). */
        NONE,
        /** The Hollow Choir's summon: star-spawn, taint, a doom push. */
        SUMMON,
        /** Every other cult's lesser rite: a whisper and a little taint, no doom. */
        LESSER
    }

    /** Cultists that must be within the cell before a ritual may start. */
    public static final int MIN_CULTISTS = 3;
    /** Rune/chalk marks a ring needs (the shared {@code RitualAltar} gate). */
    public static final int MIN_MARKS = 3;
    /** Cultists that must reach the ring to start chanting early. */
    public static final int REACHED_TO_CHANT = 2;
    /** How many cultists may die or leave before the ritual collapses. */
    public static final int MAX_LOSSES = 3;
    /** The Hollow Choir is the one cult whose completion performs the summon. */
    public static final String HOLLOW_CHOIR = "hollow_choir";

    private RitualRules() {
    }

    /** @return whether the GATHER phase may end: enough cultists arrived, or its timer ran out. */
    public static boolean gatherDone(int gatherElapsed, int gatherTicks, int reached) {
        return reached >= REACHED_TO_CHANT || gatherElapsed >= gatherTicks;
    }

    /** @return whether the CHANT phase has run its full length. */
    public static boolean chantDone(int chantElapsed, int chantTicks) {
        return chantElapsed >= chantTicks;
    }

    /** @return whether the cell has lost too many participants to continue. */
    public static boolean tooFew(int remaining, int started) {
        return started - remaining >= MAX_LOSSES;
    }

    /**
     * The first interruption that applies, in precedence order. {@code remaining} is the number of
     * starting participants still alive and present; {@code started} is the count at ritual start.
     */
    public static Interruption interruption(boolean leaderAlive, boolean coreValid,
                                            boolean playerInRing, int remaining, int started) {
        if (!leaderAlive) {
            return Interruption.LEADER;
        }
        if (!coreValid) {
            return Interruption.CORE;
        }
        if (playerInRing) {
            return Interruption.PLAYER;
        }
        if (tooFew(remaining, started)) {
            return Interruption.TOO_FEW;
        }
        return Interruption.NONE;
    }

    /** @return the completion tier for {@code cultId}: only the Hollow Choir performs the summon. */
    public static Completion completionFor(String cultId) {
        return HOLLOW_CHOIR.equals(cultId) ? Completion.SUMMON : Completion.LESSER;
    }
}
