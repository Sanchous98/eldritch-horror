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
 * Ashen wisp — fire that watches (replaces the blaze role;
 * {@code design/25-bestiary-and-entities.md}: "fire that watches (Cthugha's kin); light no longer
 * comforts"). It is Cthugha's lesser kin in the ashen waste: a corruption vector on the shared
 * {@link DreadAura} framework, and it is a light source that is <b>wrong</b> — its glow is flavour
 * only, it places no block light and brightens nothing. Spawn attachment is the bounded
 * {@link AshenWispSpawner} tainted-ground pass, like Yig's and Cthugha's country.
 *
 * <p>Base {@link Monster}. Flight is the byakhee's verified composition ({@link FlyingMoveControl}
 * + {@link FlyingPathNavigation}, {@code noGravity}, {@link #travelFlying}). Reused vanilla blaze
 * vocalisations; the renderer draws it at full block light, which is exactly the false comfort.
 */
public final class AshenWisp extends Monster implements DreadAura {

    public AshenWisp(EntityType<? extends AshenWisp> type, Level level) {
        super(type, level);
        this.xpReward = 7;
        this.moveControl = new FlyingMoveControl<>(this, 20, true);
        this.setNoGravity(true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.05, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0F));
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

    /** Base attributes: 14 HP, 5 attack (fire), quick, short follow range (it waits where it burns). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FLYING_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setPersistenceRequired();
        return data;
    }

    // --- The watching flame (DreadAura) ----------------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.CORRUPTION;
    }

    @Override
    public boolean isAuraActive() {
        return masterAuraEnabled() && ModConfig.ENABLE_CORRUPTION_ASHEN_WISP.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.CORRUPTION_ASHEN_WISP_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.CORRUPTION_ASHEN_WISP_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.CORRUPTION_ASHEN_WISP_MAX.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLAZE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        // It hovers; there is no footfall, only a crackle.
    }
}
