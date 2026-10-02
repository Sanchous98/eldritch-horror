package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
 * Star-spawn — a minion summoned by the {@code summon_star_spawn} rite
 * ({@code design/25-bestiary-and-entities.md}). It hunts by sound: it acquires the player without
 * requiring line of sight (the vanilla {@code NearestAttackableTargetGoal} "mustSee = false"), so a
 * still, quiet player is found from farther than one can see it. It <b>resists mundane weapons</b>
 * — see {@link EldritchWarded} for exactly what that blocks and the one intended means that still
 * wounds it — and its hook is a heavy <b>line-of-sight</b> sanity drain (an aura that only counts
 * while it can see you). It is a boss-like silhouette, so it reuses the Watcher's enderman render.
 *
 * <p>Base {@link Monster}; melee goal composition is the risen husk's, verified in 26.3.
 */
public final class StarSpawn extends Monster implements DreadAura {

    public StarSpawn(EntityType<? extends StarSpawn> type, Level level) {
        super(type, level);
        this.xpReward = 12;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // Hunts by sound: mustSee = false, so it acquires the player through walls and silence alike.
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    /** Base attributes: 30 HP, 5 attack, 0.32 speed, 48 follow range, some armour (an elite). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.ARMOR, 4.0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setPersistenceRequired();
        return data;
    }

    /**
     * Mundane weapons do nothing (config-gated). {@link EldritchWarded} decides; a warded-off hit
     * returns {@code false} and the swing has no effect, so the fight is only winnable with the
     * tagged ward-breaker item or an allowed bypass (explosion/sonic/mace/generic_kill).
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (ModConfig.STAR_SPAWN_RESISTS_MUNDANE.get() && EldritchWarded.wardsOff(source)) {
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    // --- Heavy line-of-sight sanity drain (DreadAura) -------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        return masterAuraEnabled() && ModConfig.ENABLE_SANITY_STAR_SPAWN.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_STAR_SPAWN_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_STAR_SPAWN_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.SANITY_STAR_SPAWN_MAX.get();
    }

    /** The heavy drain only lands while it can see you; break sight to break the drain. */
    @Override
    public boolean auraRequiresLineOfSight() {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.STAR_SPAWN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.STAR_SPAWN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.STAR_SPAWN_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.ENDERMAN_STARE, 0.2F, 0.4F);
    }
}
