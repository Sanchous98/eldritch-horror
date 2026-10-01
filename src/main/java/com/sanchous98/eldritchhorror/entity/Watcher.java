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
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * The Watcher — a presence, not a fight. Family: lesser horror (build order 3). It follows at a
 * distance, drains sanity, and has <b>no attack</b>: its whole existence is the attention
 * ({@code design/25-bestiary-and-entities.md}). Early on it cannot be killed — it is permanently
 * invulnerable until the config flag {@code watcherKillable} is set, and even then it takes no
 * melee loot. The design's "hides when directly watched" is implemented by {@link StalkGoal}: it
 * approaches only while unobserved and holds or slips away while the player's gaze is on it
 * (config-gated by {@code watcherStalkEnabled}).
 *
 * <p>Behaviour composes from the shared goals library: {@link StalkGoal} (or {@link RangedFollowGoal}
 * when stalking is off) to hold distance and {@link NearestAttackableTargetGoal} to acquire the
 * player without a {@code MeleeAttackGoal}.
 */
public final class Watcher extends Monster implements DreadAura {

    /** Persisted flag: false while the Watcher is still invulnerable. */
    private static final String TAG_KILLABLE = "WatcherKillable";

    private boolean killable;

    public Watcher(EntityType<? extends Watcher> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.killable = ModConfig.WATCHER_KILLABLE.get();
        this.applyInvulnerability();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        // It follows at a distance: with stalking on it only closes while the player is not looking;
        // otherwise it holds arm's length (the original behaviour) as a safe fallback.
        if (ModConfig.WATCHER_STALK_ENABLED.get()) {
            this.goalSelector.addGoal(4, new StalkGoal(this, 1.0, 4.0F, 10.0F));
        } else {
            this.goalSelector.addGoal(4, new RangedFollowGoal(this, 1.0, 4.0F, 10.0F));
        }
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 32.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        // Acquires the player for RangedFollowGoal; no MeleeAttackGoal is ever added.
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Presence attributes: 40 HP, 0.28 speed, 48 follow range; no attack damage. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setPersistenceRequired();
        this.applyInvulnerability();
        return data;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean(TAG_KILLABLE, this.killable);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.killable = input.getBooleanOr(TAG_KILLABLE, ModConfig.WATCHER_KILLABLE.get());
        this.applyInvulnerability();
    }

    /** Permanently invulnerable until {@code watcherKillable} is set (config-gated "impossible early"). */
    private void applyInvulnerability() {
        this.setPermanentlyInvulnerable(!this.killable);
    }

    // --- Presence sanity drain (DreadAura) ------------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        return ModConfig.ENABLE_SANITY.get() && ModConfig.ENABLE_SANITY_WATCHER.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_WATCHER_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_WATCHER_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.SANITY_WATCHER_MAX.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.WATCHER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WATCHER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WATCHER_AMBIENT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        // Near-absent: it does not seem to touch the ground.
        this.playSound(SoundEvents.ENDERMAN_STARE, 0.05F, 0.6F);
    }
}
