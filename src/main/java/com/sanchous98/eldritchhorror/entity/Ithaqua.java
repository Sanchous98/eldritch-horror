package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.entity.combat.BossAbility;
import com.sanchous98.eldritchhorror.entity.combat.BossAbilities;
import com.sanchous98.eldritchhorror.entity.combat.SimpleBossAbility;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
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
                .add(Attributes.ATTACK_DAMAGE, 12.0)
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
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_SANITY_ITHAQUA.get();
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

    // --- Abilities (BossCombat) -----------------------------------------------------------------

    @Override
    protected List<BossAbility> abilities() {
        return List.of(
                SimpleBossAbility.builder("walking_wind")
                        .cooldown(140)
                        .telegraph(26)
                        .range(14.0)
                        .warning("The wind walks — get out of its path!")
                        .sound(SoundEvents.WARDEN_SONIC_CHARGE)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphBurst(level, boss, ParticleTypes.GUST, 3);
                            BossAbilities.telegraphAt(boss, level, target, ParticleTypes.SNOWFLAKE, 3, 0.8);
                        })
                        .onExecute((boss, level, target) -> BossAbilities.cone(level, boss, 12.0, 65.0, player -> {
                            BossAbilities.knockAway(boss, player, 0.9, 0.35);
                            BossAbilities.effect(player, MobEffects.LEVITATION, 40, 0);
                            BossAbilities.effect(player, MobEffects.SLOW_FALLING, 100, 0);
                            BossAbilities.hit(boss, level, player, 6.0F);
                        }))
                        .build(),
                SimpleBossAbility.builder("bitter_cold")
                        .cooldown(170)
                        .telegraph(28)
                        .range(12.0)
                        .minPhase(Phase.STIRRING)
                        .warning("The cold takes your hands — move!")
                        .sound(SoundEvents.SNOW_GOLEM_SHOOT)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphAt(boss, level, target, ParticleTypes.SNOWFLAKE, 5, 1.4);
                            BossAbilities.ring(boss, level, target.position(), 4.5, ParticleTypes.SNOWFLAKE, 12);
                        })
                        .onExecute((boss, level, target) -> {
                            BossAbilities.freezeAround(boss, level, target.position(), 5.0, 140);
                            BossAbilities.effectAround(boss, level, target.position(), 5.0, MobEffects.WEAKNESS, 120, 0);
                            BossAbilities.damageAround(boss, level, target.position(), 5.0, 5.0F);
                        })
                        .build()
        );
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
    protected void onPhaseChanged(ServerLevel level, Phase previous, Phase now) {
        if (now == Phase.ENRAGED) {
            this.playSound(ModSounds.ITHAQUA_AMBIENT.get(), 2.0F, 0.5F);
        }
    }
}
