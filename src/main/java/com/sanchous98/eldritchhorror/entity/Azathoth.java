package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Azathoth — the blind, idiot god at the centre of the <b>rift_scar</b>/Veil, replacing the Wither
 * ({@code design/28-ancient-ones.md}, roster "no fight — the music at the centre unmakes the will to
 * act"). It is the purest expression of the rule that an Ancient One is a <i>presence</i>, not a
 * health bar:
 *
 * <ul>
 *   <li><b>No melee.</b> {@link #registerGoals} deliberately omits the attack and target goals the
 *       shared {@link AncientOne} base installs, so it never swings — only {@link FloatGoal} and
 *       the look goals remain.</li>
 *   <li><b>Hard sanity drain</b> over a wide radius (the music unmakes the will to act).</li>
 *   <li><b>Non-combat solve:</b> a rite that stills the music ({@code still_azathoth}, resolve
 *       {@link Solve#SOOTHED}). Once soothed it is awake but no longer drains.</li>
 * </ul>
 *
 * <p>Spawn attachment reuses the shared, bounded {@link SitePopulationSpawner} {@code rift_scar}
 * case (one loaded-only presence per player, capped); there is no per-boss ticker. Model reuse is
 * the vanilla wither model as a placeholder.
 */
public final class Azathoth extends AncientOne {

    public Azathoth(EntityType<? extends Azathoth> type, Level level) {
        super(type, level, BossEvent.BossBarColor.PURPLE, Component.translatable("entity.eldritch_horror.azathoth"));
        this.xpReward = 60;
        if (!ModConfig.AZATHOTH_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 250 HP, no real melee (attack is vestigial), barely moves, 64 follow range, very heavy. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 250.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.10)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    /** No fight: only float and look goals; never a melee or target goal. */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 32.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // --- Aura: SANITY (the music unmakes the will to act) ---------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        // Soothed: the music stills; the presence remains but no longer drains.
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_SANITY_AZATHOTH.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_AZATHOTH_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_AZATHOTH_RADIUS.get();
    }

    /** Soothed: the music stills at the centre of the scar. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.SOOTHED) {
            this.playSound(ModSounds.AZATHOTH_DEATH.get(), 1.2F, 0.5F);
        }
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.AZATHOTH_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.AZATHOTH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.AZATHOTH_DEATH.get();
    }

    /** A vast, slow drone heard across the whole scar. */
    @Override
    protected float getSoundVolume() {
        return 4.0F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    /** Unused in practice (no combat), kept so the base class contract holds. */
    @Override
    public Phase phaseFor(ServerLevel level, float healthFraction) {
        return Phase.DORMANT;
    }
}
