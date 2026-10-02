package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Lesser swarm — weak, numerous, seeks you in the dark. Family: lesser horror (build order 4). Its
 * hook is the sanity it drains <b>in numbers</b>: each one is cheap, but the aura counts a horde up
 * to a cap, so a pack of them is worse than the sum of scares ({@code design/25-bestiary-and-
 * entities.md}). Small hitbox, small damage.
 *
 * <p>Base {@link Monster}; melee AI is the same vanilla goal composition as the risen husk.
 */
public final class LesserSwarm extends Monster implements DreadAura {

    public LesserSwarm(EntityType<? extends LesserSwarm> type, Level level) {
        super(type, level);
        this.xpReward = 3;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        if (ModConfig.BESTIARY_PACK_ALERT_ENABLED.get()) {
            // One swarm that spots you draws in nearby kin, so a pack turns as one.
            this.goalSelector.addGoal(2, new PackAlertGoal<>(this, LesserSwarm.class, 16.0, 12.0F, 4));
        }

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Base attributes: 6 HP, 1 attack, 0.3 speed, 32 follow range (fast and fragile). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setPersistenceRequired();
        return data;
    }

    // --- Group sanity drain (DreadAura) ---------------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        return masterAuraEnabled() && ModConfig.ENABLE_SANITY_LESSER_SWARM.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_LESSER_SWARM_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_LESSER_SWARM_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.SANITY_LESSER_SWARM_MAX.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.LESSER_SWARM_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.LESSER_SWARM_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.LESSER_SWARM_AMBIENT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.SILVERFISH_STEP, 0.12F, 1.15F);
    }
}
