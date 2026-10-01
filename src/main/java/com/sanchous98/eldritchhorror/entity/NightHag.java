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
 * Night hag — the swooper that feeds on the frayed (replaces the phantom role;
 * {@code design/25-bestiary-and-entities.md}: "swoops at low sanity, not low sleep; feeds on the
 * frayed"). The design's distinction from vanilla is the <b>gate</b>: it does not care about sleep,
 * only about a nearby player's sanity being below a configured threshold — see
 * {@code NightHagSpawner}, which reads {@code SanityAPI} before placing one. Its hook is a heavier
 * sanity drain than the byakhee's, on the shared {@link DreadAura} framework.
 *
 * <p>Base {@link Monster}. Flight is the byakhee's verified composition ({@link FlyingMoveControl} +
 * {@link FlyingPathNavigation}, {@code noGravity}, {@link #travelFlying}).
 */
public final class NightHag extends Monster implements DreadAura {

    public NightHag(EntityType<? extends NightHag> type, Level level) {
        super(type, level);
        this.xpReward = 7;
        this.moveControl = new FlyingMoveControl<>(this, 20, true);
        this.setNoGravity(true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.15, false));
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

    /** Base attributes: 18 HP, 4 attack, quick flier, wide follow range (it comes to you). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 44.0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setPersistenceRequired();
        return data;
    }

    // --- Feeds-on-the-frayed sanity drain (DreadAura) ------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        return ModConfig.ENABLE_SANITY.get() && ModConfig.ENABLE_SANITY_NIGHT_HAG.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_NIGHT_HAG_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_NIGHT_HAG_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.SANITY_NIGHT_HAG_MAX.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.NIGHT_HAG_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.NIGHT_HAG_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.NIGHT_HAG_AMBIENT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        // It does not walk; it beats the air. No footfall.
    }
}
