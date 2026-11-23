package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.entity.combat.BossAbility;
import com.sanchous98.eldritchhorror.entity.combat.BossAbilities;
import com.sanchous98.eldritchhorror.entity.combat.SimpleBossAbility;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Dunwich Horror — a mobile settlement/wild threat, replacing the Ravager
 * ({@code design/28-ancient-ones.md}, roster ★2). It is fast and ambush-minded and, per the design,
 * <b>heard before it is seen</b>: a very loud, frequent ambient roar is its tell (its sound volume
 * is set high and it is near-continuous). Standing near it adds corruption and drains sanity; it
 * uses the shared three-band phase ladder, so its ambush kit escalates at half health and again at
 * the enrage threshold.
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
                .add(Attributes.ATTACK_DAMAGE, 11.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }
    @Override
    protected void onPhaseChanged(ServerLevel level, Phase previous, Phase now) {
        if (now == Phase.ENRAGED) {
            this.playSound(SoundEvents.RAVAGER_ROAR, 1.5F, 0.5F);
        }
    }

    /** Drawn away: the roar recedes downhill instead of on top of the settlement. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.BANISHED) {
            this.playSound(SoundEvents.RAVAGER_ROAR, 1.5F, 0.4F);
        }
    }

    // --- Abilities (BossCombat) -----------------------------------------------------------------

    @Override
    protected List<BossAbility> abilities() {
        return List.of(
                SimpleBossAbility.builder("unnatural_speed")
                        .cooldown(150)
                        .telegraph(24)
                        .range(18.0)
                        .warning("The Horror moves wrong — watch your back!")
                        .sound(SoundEvents.RAVAGER_ROAR)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphBurst(level, boss, ParticleTypes.SMOKE, 5);
                            BossAbilities.telegraphAt(level, target, ParticleTypes.CRIT, 3, 0.8);
                        })
                        .onExecute((boss, level, target) -> {
                            BossAbilities.teleportBehind(boss, level, target);
                            BossAbilities.effect(target, MobEffects.NAUSEA, 100, 0);
                            BossAbilities.hit(boss, level, target, 9.0F);
                        })
                        .build(),
                SimpleBossAbility.builder("wind_of_the_whateley")
                        .cooldown(180)
                        .telegraph(28)
                        .range(12.0)
                        .minPhase(Phase.STIRRING)
                        .warning("The Whateley wind hits — brace!")
                        .sound(SoundEvents.RAVAGER_ROAR)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphBurst(level, boss, ParticleTypes.GUST, 4);
                            BossAbilities.telegraphAt(level, target, ParticleTypes.SMOKE, 2, 1.0);
                        })
                        .onExecute((boss, level, target) -> BossAbilities.cone(level, boss, 12.0, 70.0, player -> {
                            BossAbilities.knockAway(boss, player, 0.8, 0.3);
                            BossAbilities.effect(player, MobEffects.NAUSEA, 120, 0);
                            BossAbilities.hit(boss, level, player, 5.0F);
                        }))
                        .build()
        );
    }

    // --- Corruption vector + presence -----------------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.CORRUPTION;
    }

    @Override
    public boolean isAuraActive() {
        // A banished Horror has been drawn off the settlement: it no longer taints there.
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_CORRUPTION_DUNWICH_HORROR.get();
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
