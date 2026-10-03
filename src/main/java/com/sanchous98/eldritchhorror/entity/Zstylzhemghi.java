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
 * Zstylzhemghi — the <b>name forgotten on purpose</b>, whose world erodes around it, at the
 * <b>rift_scar</b> (eroding court) proxy ({@code design/28-ancient-ones.md}). Its aura is
 * <b>corruption</b>, and the non-combat solve ({@code erase_zstylzhemghi}, resolve
 * {@link Solve#STILLED}) forgets the name again and pauses the erosion for a time.
 *
 * <p>Spawn attachment is the shared, bounded {@code SitePopulationSpawner} {@code rift_scar} case;
 * no per-boss ticker. Placeholder model: the vanilla enderman.
 */
public final class Zstylzhemghi extends AncientOne {

    public Zstylzhemghi(EntityType<? extends Zstylzhemghi> type, Level level) {
        super(type, level, BossEvent.BossBarColor.PURPLE,
                Component.translatable("entity.eldritch_horror.zstylzhemghi"));
        this.xpReward = 50;
        if (!ModConfig.ZSTYLZHEMGHI_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 150 HP, 6 attack, 0.24 speed, 40 follow range, tall and hard to shift. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 150.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    // --- Aura: CORRUPTION (the world erodes around it) ------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.CORRUPTION;
    }

    @Override
    public boolean isAuraActive() {
        // Forgotten again: the erosion pauses while the name is unremembered.
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_CORRUPTION_ZSTYLZHEMGHI.get();
    }

    @Override
    public double auraRate() {
        // Remembered, it erodes faster: the enraged band taints faster.
        double base = ModConfig.CORRUPTION_ZSTYLZHEMGHI_RATE.get();
        return this.phase() == Phase.ENRAGED ? base * 1.5 : base;
    }

    @Override
    public double auraRadius() {
        return ModConfig.CORRUPTION_ZSTYLZHEMGHI_RADIUS.get();
    }

    /** Forgotten: the name slips away again and the erosion pauses for a time. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.STILLED) {
            this.playSound(ModSounds.ZSTYLZHEMGHI_DEATH.get(), 1.0F, 0.5F);
        }
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ZSTYLZHEMGHI_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ZSTYLZHEMGHI_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ZSTYLZHEMGHI_DEATH.get();
    }

    /** A name at the edge of hearing: it fills the scar. */
    @Override
    protected float getSoundVolume() {
        return 2.5F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 180;
    }

    @Override
    public Phase phaseFor(ServerLevel level, float healthFraction) {
        return healthFraction <= ModConfig.BOSS_ENRAGE_HEALTH.get() ? Phase.ENRAGED : Phase.DORMANT;
    }
}
