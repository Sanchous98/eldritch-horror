package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
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
 * Veil swine — a parody of a herd from behind the veil (replaces the zombified-piglin/hoglin/piglin
 * roles; {@code design/25-bestiary-and-entities.md}: "aggressive, territorial"). It is a herd
 * animal: the bounded {@link VeilSwineSpawner} places a small group per pass, and the shared
 * {@link PackAlertGoal} makes one spotting swine turn the whole group at once. Its hook is the
 * sanity the herd costs you, on the shared {@link DreadAura} framework — the wrongness is in its
 * shape, not its swing.
 *
 * <p>Base {@link Monster}; melee goal composition is the risen husk's, plus the pack-alert goal
 * ({@link PackAlertGoal}) the lesser swarm already uses. Reused vanilla hoglin vocalisations.
 */
public final class VeilSwine extends Monster implements DreadAura {

    public VeilSwine(EntityType<? extends VeilSwine> type, Level level) {
        super(type, level);
        this.xpReward = 6;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        // One swine that spots you draws in nearby kin, so the herd turns as one.
        this.goalSelector.addGoal(2, new PackAlertGoal<>(this, VeilSwine.class, 16.0, 12.0F, 4));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Base attributes: 24 HP, 4 attack, 0.28 speed, 32 follow range, hard to shove aside. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setPersistenceRequired();
        return data;
    }

    // --- The herd's wrongness (DreadAura) --------------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        return masterAuraEnabled() && ModConfig.ENABLE_SANITY_VEIL_SWINE.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_VEIL_SWINE_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_VEIL_SWINE_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.SANITY_VEIL_SWINE_MAX.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.HOGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.HOGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HOGLIN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.HOGLIN_STEP, 0.15F, 1.0F);
    }
}
