package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Tainted fauna — a familiar animal made wrong. Family: tainted (build order 2). Low threat; its
 * whole role is the <b>corruption vector</b>: standing near it slowly adds corruption to the
 * player, mirroring the risen husk's sanity drain but on the other axis
 * ({@code design/25-bestiary-and-entities.md}). It is neutral until provoked and only becomes
 * aggressive after being hurt, so a player can choose to keep their distance.
 *
 * <p>Base {@link Animal} (not {@code Monster}) on purpose: it keeps the animal shape and gait, so
 * the contrast with the mundane world reads. {@code isFood} and {@code getBreedOffspring} are the
 * two abstract methods it forces; both are inert stubs (tainted things do not breed or eat).
 */
public final class TaintedFauna extends Animal implements DreadAura {

    public TaintedFauna(EntityType<? extends TaintedFauna> type, Level level) {
        super(type, level);
        this.xpReward = 3;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        // Retaliatory only: a tainted animal is not a hunter, but it is wrong, so if you strike it
        // it answers. MeleeAttackGoal runs only once HurtByTargetGoal has set a target.
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** Base attributes: 10 HP, 2 attack, 0.25 speed, 24 follow range. */
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setPersistenceRequired();
        return data;
    }

    // --- Corruption vector (DreadAura) ----------------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.CORRUPTION;
    }

    @Override
    public boolean isAuraActive() {
        return masterAuraEnabled() && ModConfig.ENABLE_CORRUPTION_TAINTED_FAUNA.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.CORRUPTION_TAINTED_FAUNA_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.CORRUPTION_TAINTED_FAUNA_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.CORRUPTION_TAINTED_FAUNA_MAX.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.TAINTED_FAUNA_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.TAINTED_FAUNA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TAINTED_FAUNA_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        // 26.3 has no COW_STEP (cow audio is variant-based via COW_SOUNDS); a sheep step is the
        // closest generic quadruped footfall for the placeholder.
        this.playSound(SoundEvents.SHEEP_STEP, 0.12F, 0.7F);
    }

    /** Tainted things do not breed; the abstraction is satisfied inertly. */
    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    /** Not livestock: nothing tempts or feeds it. */
    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }
}
