package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.entity.combat.BossAbility;
import com.sanchous98.eldritchhorror.entity.combat.BossAbilities;
import com.sanchous98.eldritchhorror.entity.combat.SimpleBossAbility;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
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
 * Atlach-Nacha — the spider that <b>weaves the rift wider</b> in the <b>rift_scar</b>
 * ({@code design/28-ancient-ones.md}). It is the inverse of Azathoth: where that one unmakes the
 * will to act, this one turns the scar itself into a weapon. It is a fast, patient weaver whose
 * aura is <b>corruption</b> (every moment near it widens the tear) and whose non-combat solve is a
 * binding rite ({@code bind_atlach_nacha}, resolve {@link Solve#STILLED}) that undoes the web and
 * lets the scar close for a time.
 *
 * <p>Spawn attachment is the shared, bounded {@link SitePopulationSpawner} {@code rift_scar} case;
 * no per-boss ticker. Placeholder model: the vanilla spider.
 */
public final class AtlachNacha extends AncientOne {

    public AtlachNacha(EntityType<? extends AtlachNacha> type, Level level) {
        super(type, level, BossEvent.BossBarColor.PURPLE, Component.translatable("entity.eldritch_horror.atlach_nacha"));
        this.xpReward = 50;
        if (!ModConfig.ATLACH_NACHA_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 110 HP, 6 attack, 0.32 speed (a skittering weaver), 40 follow range, low armour. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 110.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 3.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2);
    }

    // --- Aura: CORRUPTION (the web widens the tear) ---------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.CORRUPTION;
    }

    @Override
    public boolean isAuraActive() {
        // Bound: the web is unwoven and the scar stops widening.
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_CORRUPTION_ATLACH_NACHA.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.CORRUPTION_ATLACH_NACHA_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.CORRUPTION_ATLACH_NACHA_RADIUS.get();
    }

    /** Bound: the web comes apart and the widening stops for a time. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.STILLED) {
            this.playSound(ModSounds.ATLACH_NACHA_DEATH.get(), 1.0F, 0.5F);
        }
    }

    // --- Abilities (BossCombat) -----------------------------------------------------------------

    @Override
    protected List<BossAbility> abilities() {
        return List.of(
                SimpleBossAbility.builder("web_snare")
                        .cooldown(150)
                        .telegraph(24)
                        .range(14.0)
                        .warning("The web tightens — cut free!")
                        .sound(SoundEvents.SPIDER_HURT)
                        .onTelegraph((boss, level, target, ticksLeft) -> {
                            BossAbilities.telegraphAt(boss, level, target, ParticleTypes.ITEM_COBWEB, 4, 1.0);
                            BossAbilities.ring(boss, level, target.position(), 2.5, ParticleTypes.ITEM_COBWEB, 10);
                        })
                        .onExecute((boss, level, target) -> {
                            BossAbilities.effect(target, MobEffects.SLOWNESS, 100, 3);
                            BossAbilities.hit(boss, level, target, 5.0F);
                        })
                        .build(),
                SimpleBossAbility.builder("brood_call")
                        .cooldown(190)
                        .telegraph(26)
                        .range(20.0)
                        .minPhase(Phase.STIRRING)
                        .warning("The brood descends — watch the ceiling!")
                        .sound(SoundEvents.SPIDER_HURT)
                        .onTelegraph((boss, level, target, ticksLeft) ->
                                BossAbilities.telegraphBurst(level, boss, ParticleTypes.ITEM_COBWEB, 5))
                        .onExecute((boss, level, target) ->
                                boss.bossCombat().summon(level, ModEntities.WEAVER_SPAWN.get(), 2, target))
                        .build()
        );
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ATLACH_NACHA_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ATLACH_NACHA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ATLACH_NACHA_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 90;
    }
}
