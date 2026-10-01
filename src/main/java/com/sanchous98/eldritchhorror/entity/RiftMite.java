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
 * Rift mite — the tainted-chunk vermin (replaces the silverfish/endermite role;
 * {@code design/25-bestiary-and-entities.md}: "tiny rift vermin; swarms when a chunk is tainted").
 * It is near-harmless alone: its whole hook is the <b>swarm</b> — the shared {@link DreadAura} counts
 * a group up to a cap, so a tainted chunk's mites are a corruption vector in aggregate. Tiny hitbox,
 * very low HP, fast.
 *
 * <p>Base {@link Monster}; melee goal composition is the risen husk's, verified in 26.3. Its
 * spawner is tainted-only and arrives in a small group (the shared {@code topUp} per-pass count).
 */
public final class RiftMite extends Monster implements DreadAura {

    public RiftMite(EntityType<? extends RiftMite> type, Level level) {
        super(type, level);
        this.xpReward = 2;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.3, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Base attributes: 4 HP, 1 attack, 0.36 speed, 28 follow range (fast, expendable vermin). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.36)
                .add(Attributes.FOLLOW_RANGE, 28.0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setPersistenceRequired();
        return data;
    }

    // --- Swarm corruption vector (DreadAura) ----------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.CORRUPTION;
    }

    @Override
    public boolean isAuraActive() {
        return ModConfig.ENABLE_CORRUPTION.get() && ModConfig.ENABLE_CORRUPTION_RIFT_MITE.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.CORRUPTION_RIFT_MITE_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.CORRUPTION_RIFT_MITE_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.CORRUPTION_RIFT_MITE_MAX.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.RIFT_MITE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.RIFT_MITE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.RIFT_MITE_AMBIENT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.SILVERFISH_STEP, 0.08F, 1.4F);
    }
}
