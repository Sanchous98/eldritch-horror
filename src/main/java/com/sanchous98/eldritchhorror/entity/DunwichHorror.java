package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Dunwich Horror — a mobile settlement/wild threat, replacing the Ravager
 * ({@code design/28-ancient-ones.md}, roster ★2). It is fast and ambush-minded and, per the design,
 * <b>heard before it is seen</b>: a very loud, frequent ambient roar is its tell (its sound volume
 * is set high and it is near-continuous). Standing near it adds corruption and drains sanity; it
 * uses the fewer two-phase ladder (no middle band) — the "settlement it is eating" escalates at
 * low health only.
 *
 * <p>Bounded spawns near players in dark/tainted loaded terrain are in {@link AncientOneSpawner};
 * model reuse is the vanilla ravager.
 */
public final class DunwichHorror extends AncientOne {

    public DunwichHorror(EntityType<? extends DunwichHorror> type, Level level) {
        super(type, level, BossEvent.BossBarColor.RED, Component.translatable("entity.eldritch_horror.dunwich_horror"));
        this.xpReward = 25;
        if (!ModConfig.DUNWICH_HORROR_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 90 HP, 6 attack, 0.34 speed (fast), 40 follow range. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 90.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    /**
     * Fewer phases: only the enrage band matters. Above the shared enrage threshold it stays
     * DORMANT (never STIRRING), so the horror has a single escalation at low health.
     */
    @Override
    public Phase phaseFor(ServerLevel level, float healthFraction) {
        return healthFraction <= ModConfig.BOSS_ENRAGE_HEALTH.get() ? Phase.ENRAGED : Phase.DORMANT;
    }

    @Override
    protected void onPhaseChanged(ServerLevel level, Phase previous, Phase now) {
        if (now == Phase.ENRAGED) {
            this.playSound(SoundEvents.RAVAGER_ROAR, 1.5F, 0.5F);
        }
    }

    // --- Corruption vector + presence -----------------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.CORRUPTION;
    }

    @Override
    public boolean isAuraActive() {
        return ModConfig.ENABLE_CORRUPTION.get() && ModConfig.ENABLE_CORRUPTION_DUNWICH_HORROR.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.CORRUPTION_DUNWICH_HORROR_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.CORRUPTION_DUNWICH_HORROR_RADIUS.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.DUNWICH_HORROR_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.DUNWICH_HORROR_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DUNWICH_HORROR_DEATH.get();
    }

    /** Heard not seen: the tell is a roar you cannot place. Loud and frequent. */
    @Override
    public int getAmbientSoundInterval() {
        return 40;
    }

    @Override
    protected float getSoundVolume() {
        return 3.0F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.RAVAGER_STEP, 0.4F, 0.7F);
    }
}
