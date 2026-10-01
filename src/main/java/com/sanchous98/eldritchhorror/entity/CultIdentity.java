package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.cult.CultRank;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The owning cult identity of a cultist mob: which faction it belongs to and its rank band within
 * that ladder. Plain data held by each mob (not synced — the placeholder client models do not vary
 * by cult, so server authority plus NBT persistence is enough; {@code design/25} says "synced data /
 * NBT" and this takes the NBT half. {@code design/25}: "cult combatant that scales with the owning
 * cult's rank").
 *
 * <p>Ranks use the shared {@link CultRank} ladder; {@code OUTSIDER} is a <em>player</em> standing,
 * so a member can never be assigned it.
 */
public final class CultIdentity {

    private static final String TAG_CULT = "CultId";
    private static final String TAG_RANK = "CultRank";

    private String cultId;
    private CultRank rank;

    public CultIdentity(String cultId, CultRank rank) {
        this.cultId = cultId;
        this.rank = rank;
    }

    /** A fresh identity drawn from the cult registry with a weighted member rank. */
    public static CultIdentity random(RandomSource random) {
        return new CultIdentity(CultistSupport.randomCult(random), CultistSupport.randomRank(random));
    }

    /** The owning cult's id (also the reputation key). */
    public String cultId() {
        return this.cultId;
    }

    /** The mob's rank band in its own cult's ladder. */
    public CultRank rank() {
        return this.rank;
    }

    /** Reassigns the identity (used at spawn and by the spawn egg / commands later). */
    public void set(String cultId, CultRank rank) {
        this.cultId = cultId;
        this.rank = rank == CultRank.OUTSIDER ? CultRank.NEUTRAL : rank;
    }

    public void save(ValueOutput output) {
        output.putString(TAG_CULT, this.cultId);
        output.putInt(TAG_RANK, this.rank.ordinal());
    }

    public void load(ValueInput input) {
        this.cultId = input.getStringOr(TAG_CULT, CultistSupport.defaultCult());
        int ordinal = input.getIntOr(TAG_RANK, CultRank.NEUTRAL.ordinal());
        CultRank[] values = CultRank.values();
        this.rank = values[Math.clamp(ordinal, 0, values.length - 1)];
        if (this.rank == CultRank.OUTSIDER) {
            this.rank = CultRank.NEUTRAL;
        }
    }
}
