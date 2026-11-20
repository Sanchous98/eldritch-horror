package com.sanchous98.eldritchhorror.world.cult;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;

/**
 * One in-memory ritual cell ({@code design/06-factions.md}, "Ritual cells"): the altar it is
 * anchored on, the cult that gathered, its leader and starting participants, and the current phase
 * and phase timer. Server lifetime is enough — a restart simply rebuilds cells from the cultists
 * that are still standing at their altars, so nothing here is persisted.
 *
 * <p>Immutable except for the phase and timer, which only {@link CultRituals} moves.
 */
final class RitualCell {

    private final BlockPos core;
    private final String cultId;
    private final UUID leader;
    private final List<UUID> participants;
    private final int startCount;
    private RitualRules.State state = RitualRules.State.GATHER;
    private int elapsed;

    RitualCell(BlockPos core, String cultId, UUID leader, List<UUID> participants) {
        this.core = core;
        this.cultId = cultId;
        this.leader = leader;
        this.participants = List.copyOf(participants);
        this.startCount = participants.size();
    }

    /** @return the altar core the cell is anchored on. */
    BlockPos core() {
        return this.core;
    }

    /** @return the owning cult's id. */
    String cultId() {
        return this.cultId;
    }

    /** @return the leader chosen when the cell formed. */
    UUID leader() {
        return this.leader;
    }

    /** @return the starting participants (leader included). */
    List<UUID> participants() {
        return this.participants;
    }

    /** @return how many participants the cell started with. */
    int startCount() {
        return this.startCount;
    }

    /** @return the current phase. */
    RitualRules.State state() {
        return this.state;
    }

    /** @return ticks spent in the current phase. */
    int elapsed() {
        return this.elapsed;
    }

    /** Moves to CHANT and restarts the phase timer. */
    void beginChant() {
        this.state = RitualRules.State.CHANT;
        this.elapsed = 0;
    }

    /** Advances the phase timer. */
    void advance(int ticks) {
        this.elapsed += ticks;
    }
}
