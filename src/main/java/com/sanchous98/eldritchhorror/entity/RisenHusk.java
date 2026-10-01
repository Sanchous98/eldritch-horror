package com.sanchous98.eldritchhorror.entity;

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
 * The risen husk — a wrong, dried human that walks. Family: tainted/undead. The first real bestiary
 * entry ({@code design/25-bestiary-and-entities.md}, build order 1) and the low bar of the wrong: it
 * is slow, does little damage, and its hook is the sanity it drains nearby, not its claws.
 *
 * <p>Base class is {@link Monster} (not {@code Zombie}) on purpose: {@code Zombie} carries drowning
 * conversion, reinforcement, villager-conversion and sun-burning behaviour we do not want (the brief
 * asks for no daylight surprises). {@code Monster} gives the same hostile shape with none of that.
 *
 * <p>Melee AI is composed from vanilla goals (verified in the 26.3 sources):
 * {@link FloatGoal}, {@link MeleeAttackGoal}, {@link WaterAvoidingRandomStrollGoal},
 * {@link LookAtPlayerGoal}, {@link RandomLookAroundGoal}, {@link HurtByTargetGoal},
 * {@link NearestAttackableTargetGoal}.
 */
public final class RisenHusk extends Monster {

    public RisenHusk(EntityType<? extends RisenHusk> type, Level level) {
        super(type, level);
        this.xpReward = 5;
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

    /** Base attributes: 12 HP, 2 attack, 0.22 speed, 35 follow range (verified builder shape). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 35.0)
                .add(Attributes.ARMOR, 1.0);
    }

    /**
     * Spawned by our spawner, so it is persistent: it must not quietly despawn at chunk borders and
     * rob the player of the encounter the design wants to be present ("Spawns attach to the world").
     */
    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setPersistenceRequired();
        return data;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.RISEN_HUSK_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.RISEN_HUSK_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.RISEN_HUSK_AMBIENT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.HUSK_STEP, 0.15F, 0.85F);
    }
}
