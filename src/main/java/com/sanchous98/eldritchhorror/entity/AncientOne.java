package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Shared base for every Ancient One (the boss layer, {@code design/28-ancient-ones.md}). It is the
 * reusable half of the framework: a server-owned boss bar, a health-and-world driven phase machine
 * with a change hook, and the {@link DreadAura} presence contract. It deliberately does <b>not</b>
 * carry a per-boss class: each Ancient One supplies only attributes, goals, aura numbers and (at
 * most) one phase hook.
 *
 * <p>Verified against the 26.3 patched sources:
 * <ul>
 *   <li>{@code net.minecraft.server.level.ServerBossEvent} is a public {@code BossEvent} with the
 *       constructor {@code (UUID, Component, BossBarColor, BossBarOverlay)} and
 *       {@code addPlayer}/{@code removePlayer}/{@code setProgress}/{@code setName}. The wither uses
 *       exactly this shape ({@code WitherBoss.bossEvent}), so we mirror it.</li>
 *   <li>{@code Mth.createInsecureUUID(RandomSource)} exists (same call the wither makes).</li>
 *   <li>{@code Entity.startSeenByPlayer}/{@code stopSeenByPlayer} are the tracking hooks the wither
 *       overrides; {@code Mob.removeWhenFarAway} is overridden so a persistent boss never despawns
 *       even if the persistence flag is somehow cleared.</li>
 *   <li>{@code customServerAiStep(ServerLevel)} is the server-AI tick the wither uses to push boss
 *       progress.</li>
 * </ul>
 */
public abstract class AncientOne extends Monster implements DreadAura {

    /** Coarse encounter band; driven by health fraction (and overridable with world state). */
    public enum Phase {
        /** Above the second threshold: the presence is merely present. */
        DORMANT,
        /** Between the two thresholds: the encounter has turned. */
        STIRRING,
        /** At or below the enrage threshold: the worst of it. */
        ENRAGED
    }

    private static final String TAG_PHASE = "AncientOnePhase";

    private final ServerBossEvent bossEvent;
    private Phase phase = Phase.DORMANT;

    protected AncientOne(EntityType<? extends AncientOne> type, Level level,
                         BossEvent.BossBarColor color, Component title) {
        super(type, level);
        // Mirrors WitherBoss: a per-instance random UUID so two of the same Ancient One never share
        // a bar, and the player's own display name when one is set.
        this.bossEvent = new ServerBossEvent(Mth.createInsecureUUID(this.random), title, color,
                BossEvent.BossBarOverlay.PROGRESS);
    }

    /** The bar, for subclasses that want to restyle it (colour/overlay set at construction). */
    protected final ServerBossEvent bossEvent() {
        return this.bossEvent;
    }

    // --- Boss bar plumbing (the wither's exact pattern) -----------------------------------------

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    // --- Phase machine --------------------------------------------------------------------------

    @Override
    protected void customServerAiStep(ServerLevel level) {
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.updatePhase(level);
        super.customServerAiStep(level);
    }

    /**
     * Phase for the current health fraction. Subclasses override to read the world (sanity, taint)
     * or to collapse to fewer bands. Default is the shared two-threshold ladder.
     */
    public Phase phaseFor(ServerLevel level, float healthFraction) {
        if (healthFraction <= ModConfig.BOSS_ENRAGE_HEALTH.get()) {
            return Phase.ENRAGED;
        }
        if (healthFraction <= ModConfig.BOSS_PHASE_TWO_HEALTH.get()) {
            return Phase.STIRRING;
        }
        return Phase.DORMANT;
    }

    private void updatePhase(ServerLevel level) {
        Phase next = this.phaseFor(level, this.getHealth() / this.getMaxHealth());
        if (next != this.phase) {
            Phase previous = this.phase;
            this.phase = next;
            this.onPhaseChanged(level, previous, next);
        }
    }

    /** Called when the band changes; default is inert. The one place a subclass adds enrage work. */
    protected void onPhaseChanged(ServerLevel level, Phase previous, Phase now) {
    }

    /** Current band. */
    public final Phase phase() {
        return this.phase;
    }

    // --- Presence / persistence -----------------------------------------------------------------

    /**
     * Persistent, so a boss never quietly despawns at a chunk border and robs the site of the
     * encounter the design wants it to own. {@code removeWhenFarAway} is also forced false.
     */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setPersistenceRequired();
        this.applyHealthMultiplier();
        return data;
    }

    /**
     * Applies the global boss-difficulty knob once, at spawn, by scaling the saved max-health base
     * value (so it survives a reload; {@code finalizeSpawn} is not re-run on load). Default 1.0 is
     * a no-op.
     */
    private void applyHealthMultiplier() {
        double multiplier = ModConfig.BOSS_HEALTH_MULTIPLIER.get();
        AttributeInstance maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null && multiplier != 1.0) {
            maxHealth.setBaseValue(maxHealth.getBaseValue() * multiplier);
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString(TAG_PHASE, this.phase.name());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String stored = input.getStringOr(TAG_PHASE, Phase.DORMANT.name());
        this.phase = parsePhase(stored);
    }

    private static Phase parsePhase(String name) {
        for (Phase value : Phase.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return Phase.DORMANT;
    }

    // --- Shared melee goals ---------------------------------------------------------------------

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // --- DreadAura defaults ---------------------------------------------------------------------

    /** An Ancient One is singular: a presence never stacks with itself. */
    @Override
    public int auraMaxStack() {
        return 1;
    }
}
