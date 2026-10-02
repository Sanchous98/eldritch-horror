package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Choir spite — the wall-passing mote (replaces the vex role;
 * {@code design/25-bestiary-and-entities.md}: "a spiteful mote of the Choir; passes walls, drains
 * focus"). It flies with no physics while moving, so walls do not stop it; it is low HP and short
 * lived (a bounded lifetime from config), so a spite is a <i>moment</i>, not a resident. Its hook is
 * a focus/sanity drain on the shared {@link DreadAura} framework.
 *
 * <p>Base {@link Monster}. Flight is the byakhee's verified composition ({@link FlyingMoveControl} +
 * {@link FlyingPathNavigation}, {@code noGravity}, {@link #travelFlying}); the wall-passing is vanilla
 * {@code Vex}'s {@code noPhysics} trick (set around the super tick).
 */
public final class ChoirSpite extends Monster implements DreadAura {

    /** Ticks left before the mote begins to gutter out; {@code -1} until spawn finalises it. */
    private int lifeTicks = -1;

    public ChoirSpite(EntityType<? extends ChoirSpite> type, Level level) {
        super(type, level);
        this.xpReward = 3;
        this.moveControl = new FlyingMoveControl<>(this, 20, true);
        this.setNoGravity(true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.3, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setRequiredPathLength(48.0F);
        return navigation;
    }

    @Override
    public void travel(Vec3 input) {
        this.travelFlying(input, 0.2F);
    }

    @Override
    public void tick() {
        boolean phasing = ModConfig.ENABLE_CHOIR_SPITE_PHASING.get();
        if (phasing) {
            this.noPhysics = true;
        }
        super.tick();
        this.noPhysics = false;
        this.setNoGravity(true);
        if (!this.level().isClientSide() && this.lifeTicks >= 0 && --this.lifeTicks <= 0) {
            this.lifeTicks = 20;
            this.hurt(this.damageSources().starve(), 1.0F);
        }
    }

    /** Base attributes: 8 HP, 3 attack, quick flier, short follow range (a harasser). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 36.0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setPersistenceRequired();
        this.lifeTicks = ModConfig.CHOIR_SPITE_LIFETIME_SECONDS.get() * 20;
        return data;
    }

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        return masterAuraEnabled() && ModConfig.ENABLE_SANITY_CHOIR_SPITE.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_CHOIR_SPITE_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_CHOIR_SPITE_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.SANITY_CHOIR_SPITE_MAX.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CHOIR_SPITE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CHOIR_SPITE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CHOIR_SPITE_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        // It does not walk; it hums through the wall.
    }
}
