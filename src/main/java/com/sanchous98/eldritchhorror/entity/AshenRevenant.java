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
 * Ashen revenant — the burning dead of the ashen waste (replaces the wither skeleton role;
 * {@code design/25-bestiary-and-entities.md}: "burning dead of the ashen waste; leaves scorch-taint").
 * It is a lesser horror whose point is the corruption it trails, not its swing: standing near one
 * slowly adds corruption on the shared {@link DreadAura} framework. Spawn attachment is the bounded
 * {@link AshenRevenantSpawner} dark/tainted pass, like the risen husk's.
 *
 * <p>Base {@link Monster}; melee goal composition is the risen husk's, verified in 26.3. Its
 * vocalisation is the vanilla wither-skeleton set (reused deliberately; no new sound event is
 * invented for a lesser role).
 */
public final class AshenRevenant extends Monster implements DreadAura {

    public AshenRevenant(EntityType<? extends AshenRevenant> type, Level level) {
        super(type, level);
        this.xpReward = 6;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Base attributes: 20 HP, 4 attack, 0.26 speed, 35 follow range, a little armour. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.FOLLOW_RANGE, 35.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setPersistenceRequired();
        return data;
    }

    // --- Scorch-taint corruption vector (DreadAura) ---------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.CORRUPTION;
    }

    @Override
    public boolean isAuraActive() {
        return masterAuraEnabled() && ModConfig.ENABLE_CORRUPTION_ASHEN_REVENANT.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.CORRUPTION_ASHEN_REVENANT_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.CORRUPTION_ASHEN_REVENANT_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.CORRUPTION_ASHEN_REVENANT_MAX.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.WITHER_SKELETON_STEP, 0.15F, 0.9F);
    }
}
