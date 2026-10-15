package com.sanchous98.eldritchhorror.endgame;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * The per-world endgame outcome ({@code design/11-endgame.md}): which way the decision at The Horror
 * went. It is a {@link SavedData} for the overworld, so the choice survives restarts and is shared by
 * everyone on the server — the fork is a world fact, not a per-player flag.
 *
 * <p>Deliberately small: the outcome plus the two helpers that read and write it. All of the world
 * work (rifts closing or multiplying, the corruption floor, the rewards) lives in {@link Endgame},
 * and the rites that trigger it in {@code rite/RiteEngine}.
 *
 * <p>The codec is lenient on read: an unknown stored name falls back to {@link Outcome#NONE}, so a
 * data pack downgrade can never leave the save unreadable.
 */
public final class EndgameState extends SavedData {

    /** How the world's story ended. */
    public enum Outcome {
        /** The decision has not been made. */
        NONE,
        /** Path A: the Horror was banished — costly, the world recedes. */
        BANISHED,
        /** Path B: the Horror was summoned — the Choir's ending, the world tips. */
        SUMMONED,
        /**
         * The third outcome ({@code design/11-endgame.md}, "seal every rift without banishing"): the
         * Horror came for the decision and found every rift already closed — nothing left to hold it.
         * Nothing was banished and no cost was paid; the world simply stands sealed.
         */
        SEALED
    }

    private static final Codec<Outcome> OUTCOME_CODEC = Codec.STRING.xmap(EndgameState::parse, Enum::name);

    private static final Codec<EndgameState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            OUTCOME_CODEC.optionalFieldOf("outcome", Outcome.NONE).forGetter(EndgameState::outcome)
    ).apply(instance, EndgameState::new));

    /** The overworld's endgame save entry. */
    private static final SavedDataType<EndgameState> TYPE =
            new SavedDataType<>(EldritchHorror.id("endgame"), EndgameState::new, CODEC);

    private Outcome outcome = Outcome.NONE;

    private EndgameState() {
    }

    private EndgameState(Outcome outcome) {
        this.outcome = outcome;
    }

    /** @return how the world's story ended so far. */
    public Outcome outcome() {
        return this.outcome;
    }

    /** Records the decision (dirty), so it is written back with the next save. */
    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
        this.setDirty();
    }

    /** @return the overworld's endgame state (created on first read). */
    public static EndgameState of(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    private static Outcome parse(String name) {
        for (Outcome value : Outcome.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return Outcome.NONE;
    }
}
