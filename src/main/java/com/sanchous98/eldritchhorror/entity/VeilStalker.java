package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Veil stalker — the ambusher (replaces the spider/enderman role;
 * {@code design/25-bestiary-and-entities.md}). It <b>climbs</b> walls (the same
 * {@link WallClimberNavigation} + climbing flag vanilla {@code Spider} uses, reimplemented here so
 * no vanilla spider behaviour comes with it) and waits above the line of sight. Its hook is a small,
 * quiet sanity whisper on the shared {@link DreadAura} framework.
 *
 * <p>Base {@link Monster}. The leap-at-target goal is deliberately omitted: the whisper is the
 * threat, and a plain melee rush reads better as an ambush than a jump.
 */
public final class VeilStalker extends Monster implements DreadAura {

    /** Synced climbing flag, mirroring vanilla Spider's (a single bit). */
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID =
            SynchedEntityData.defineId(VeilStalker.class, EntityDataSerializers.BYTE);

    public VeilStalker(EntityType<? extends VeilStalker> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Base attributes: 12 HP, 2 attack, 0.3 speed, 32 follow range (fast, fragile, ambushing). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_FLAGS_ID, (byte) 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            this.setClimbing(ModConfig.ENABLE_VEIL_STALKER_CLIMBING.get() && this.horizontalCollision);
        }
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbing() && ModConfig.ENABLE_VEIL_STALKER_CLIMBING.get();
    }

    public boolean isClimbing() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setClimbing(boolean value) {
        byte flags = this.entityData.get(DATA_FLAGS_ID);
        flags = value ? (byte) (flags | 1) : (byte) (flags & -2);
        this.entityData.set(DATA_FLAGS_ID, flags);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setPersistenceRequired();
        return data;
    }

    // --- Whisper sanity drain (DreadAura) -------------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        return ModConfig.ENABLE_SANITY.get() && ModConfig.ENABLE_SANITY_VEIL_STALKER.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_VEIL_STALKER_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_VEIL_STALKER_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.SANITY_VEIL_STALKER_MAX.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.VEIL_STALKER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.VEIL_STALKER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.VEIL_STALKER_AMBIENT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.12F, 1.1F);
    }
}
