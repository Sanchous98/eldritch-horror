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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Ithaqua — "wind that walks" at the polar edge / Morok, replacing the Warden
 * ({@code design/28-ancient-ones.md}). It is a <b>roaming</b> presence: it spawns only on cold,
 * loaded ground near a player (the shared, bounded {@link AncientOneSpawner} {@code icy} pass), and
 * its hook is a sanity drain with a <b>corruption</b> bite — the wind drags cold and wrongness into
 * you. The non-combat solve is a ward against the wind ({@code ward_ithaqua}, resolve
 * {@link Solve#STILLED}, refreshed while the ward holds).
 *
 * <p>Placeholder model: the vanilla warden model (a tall, hunched silhouette); art is the user's job.
 */
public final class Ithaqua extends AncientOne {

    public Ithaqua(EntityType<? extends Ithaqua> type, Level level) {
        super(type, level, BossEvent.BossBarColor.BLUE, Component.translatable("entity.eldritch_horror.ithaqua"));
        this.xpReward = 35;
        if (!ModConfig.ITHAQUA_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 120 HP, 7 attack, 0.30 speed (fast, wind-like), 40 follow range, light. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3);
    }

    // --- Aura: SANITY (its single shared axis; the cold is carried in the drain) ----------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        // Warded: the wind cannot find the seam to blow through.
        return !isSolved() && ModConfig.ENABLE_SANITY.get() && ModConfig.ENABLE_SANITY_ITHAQUA.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_ITHAQUA_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_ITHAQUA_RADIUS.get();
    }

    /** Warded: the gust is turned aside for a while. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.STILLED) {
            this.playSound(ModSounds.ITHAQUA_DEATH.get(), 1.0F, 0.6F);
        }
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ITHAQUA_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ITHAQUA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ITHAQUA_DEATH.get();
    }

    /** Heard before felt: a howl carried on the wind. */
    @Override
    protected float getSoundVolume() {
        return 3.0F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 70;
    }

    @Override
    public Phase phaseFor(ServerLevel level, float healthFraction) {
        return healthFraction <= ModConfig.BOSS_ENRAGE_HEALTH.get() ? Phase.ENRAGED : Phase.DORMANT;
    }

    @Override
    protected void onPhaseChanged(ServerLevel level, Phase previous, Phase now) {
        if (now == Phase.ENRAGED) {
            this.playSound(ModSounds.ITHAQUA_AMBIENT.get(), 2.0F, 0.5F);
        }
    }
}
