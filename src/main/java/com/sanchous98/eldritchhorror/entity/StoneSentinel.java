package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Stone sentinel — the iron/snow golem role: an <b>Order-built guard of vaults</b>. Family: order.
 * It is not a food animal. Its hook is a small, bounded <b>warding deterrent</b>: lesser bestiary
 * mobs within its radius are pushed back and have any player target cleared, so it holds a line
 * around a vault without becoming a farm. Server-side, config-gated, loaded entities only, and it
 * never targets the player unless struck. Ambient spawn defaults OFF — vaults/sites populate it
 * later, and the egg/command work now.
 */
public final class StoneSentinel extends AbstractGolem {

    public StoneSentinel(EntityType<? extends StoneSentinel> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // Wards our lesser mobs by fighting them, but never the player unless struck.
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, true,
                (target, level) -> isWardable(target)));
    }

    /** The set of lesser bestiary mobs the sentinel is allowed to deter. */
    private static boolean isWardable(LivingEntity target) {
        return target instanceof RisenHusk
                || target instanceof LesserSwarm
                || target instanceof TaintedFauna
                || target instanceof WeaverSpawn
                || target instanceof RiftMite
                || target instanceof ShamblerOoze
                || target instanceof NightHag;
    }

    /** 60 HP, 6 attack, slow and armoured. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.tickCount % 20 != 0 || !ModConfig.ENABLE_STONE_SENTINEL_WARD.get()) {
            return;
        }
        int cap = ModConfig.STONE_SENTINEL_WARD_MAX.get();
        AABB box = this.getBoundingBox().inflate(ModConfig.STONE_SENTINEL_WARD_RADIUS.get());
        int warded = 0;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, box,
                m -> m != this && m.isAlive() && isWardable(m))) {
            if (warded++ >= cap) {
                break;
            }
            LivingEntity target = mob.getTarget();
            if (target instanceof Player) {
                mob.setTarget(null);
            }
            // A gentle outward nudge, centred on the sentinel; no damage, just a line held.
            double dx = mob.getX() - this.getX();
            double dz = mob.getZ() - this.getZ();
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0.001) {
                double strength = ModConfig.STONE_SENTINEL_WARD_PUSH.get();
                mob.push(dx / len * strength, 0.0, dz / len * strength);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.STONE_SENTINEL_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 0.5F, 1.0F);
    }
}
