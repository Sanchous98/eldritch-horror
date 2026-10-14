package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
 * Wail husk — the floating mourner (replaces the ghast role;
 * {@code design/25-bestiary-and-entities.md}: "floating mourner whose wail is a line-of-sight
 * drain"). It drifts in the dark above players and its wail only costs you while it can actually
 * see you: {@link #auraRateFor} returns zero when {@code hasLineOfSight(player)} is false, so
 * breaking sight breaks the drain ({@code design/25}: "witnessing/line-of-sight is the primary
 * drain").
 *
 * <p>Base {@link Monster}. Flight is the byakhee's verified composition ({@link FlyingMoveControl}
 * + {@link FlyingPathNavigation}, {@code noGravity}, {@link #travelFlying}); the spawner is the
 * bounded air pass in {@link WailHuskSpawner}. Reused vanilla ghast vocalisations.
 */
public final class WailHusk extends Monster implements DreadAura {

    public WailHusk(EntityType<? extends WailHusk> type, Level level) {
        super(type, level);
        this.xpReward = 8;
        this.moveControl = new FlyingMoveControl<>(this, 20, true);
        this.setNoGravity(true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 16.0F));
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

    /** Base attributes: 20 HP, 4 attack, a slow drifter, wide follow range (it finds you). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 44.0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setPersistenceRequired();
        return data;
    }

    // --- The mourning wail: line-of-sight only (DreadAura) --------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        return masterAuraEnabled() && ModConfig.ENABLE_SANITY_WAIL_HUSK.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_WAIL_HUSK_RATE.get();
    }

    /**
     * The wail is a line-of-sight threat: it only drains a player it can actually see, so ducking
     * behind cover or a hill is the answer. Verified 26.3: {@code LivingEntity.hasLineOfSight(Entity)}.
     */
    @Override
    public double auraRateFor(ServerPlayer player) {
        return this.hasLineOfSight(player) ? auraRate() : 0.0;
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_WAIL_HUSK_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.SANITY_WAIL_HUSK_MAX.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.GHAST_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GHAST_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GHAST_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        // It does not walk; it mourns overhead. No footfall.
    }
}
