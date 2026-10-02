package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.corruption.TaintAPI;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Shambler ooze — the splitter (replaces the slime/magma-cube role;
 * {@code design/25-bestiary-and-entities.md}: "splits when struck; drops tainted residue"). The split
 * is <b>bounded and one-shot per ooze</b>: on death an ooze above {@code minSplitSize} releases up to
 * {@code splitCount} copies exactly one size smaller, and at or below the minimum it dies without
 * splitting — so the recursion is finite (a size-3 ooze at minimum 1 yields at most size 2, then size
 * 1, then nothing). Its hook is a corruption vector on the shared {@link DreadAura}; its death also
 * leaves a small taint residue.
 *
 * <p>Base {@link Monster}; melee goal composition is the risen husk's, verified in 26.3. The size is
 * one synced byte used only to drive the slime render scale (as {@code ShoggothMassRenderer} does).
 */
public final class ShamblerOoze extends Monster implements DreadAura {

    /** Render/split size, 0..3 (0 is the smallest). Synced so the client can scale the model. */
    private static final EntityDataAccessor<Byte> DATA_SIZE_ID =
            SynchedEntityData.defineId(ShamblerOoze.class, EntityDataSerializers.BYTE);

    public ShamblerOoze(EntityType<? extends ShamblerOoze> type, Level level) {
        super(type, level);
        this.xpReward = 3;
    }

    /** Whether this ooze was released by a split; split children do not drop loot. */
    private boolean splitChild;

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Base attributes: 10 HP, 2 attack, 0.2 speed, 28 follow range (a slow, soft vector). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.FOLLOW_RANGE, 28.0);
    }

    /** Save keys for size (render scale + split floor) and the no-loot split-child marker. */
    private static final String TAG_SIZE = "OozeSize";
    private static final String TAG_SPLIT_CHILD = "SplitChild";

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(TAG_SIZE, this.getOozeSize());
        output.putBoolean(TAG_SPLIT_CHILD, this.splitChild);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setOozeSize(input.getIntOr(TAG_SIZE, 2));
        this.splitChild = input.getBooleanOr(TAG_SPLIT_CHILD, false);
    }

    /** A copy released by {@link #split}: dies without dropping loot, so the split cannot multiply value. */
    @Override
    protected void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKilled) {
        if (this.splitChild) {
            return;
        }
        super.dropFromLootTable(level, source, playerKilled);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_SIZE_ID, (byte) 2);
    }

    /** The current size, 0..3; drives both the render scale and the split floor. */
    public int getOozeSize() {
        return this.entityData.get(DATA_SIZE_ID);
    }

    public void setOozeSize(int size) {
        this.entityData.set(DATA_SIZE_ID, (byte) Mth.clamp(size, 0, 3));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setPersistenceRequired();
        return data;
    }

    /** On death: a bounded split into one-size-smaller copies, plus a little taint residue. */
    @Override
    public void die(DamageSource source) {
        if (this.level() instanceof ServerLevel level) {
            split(level);
            if (ModConfig.ENABLE_SHAMBLER_OOZE_RESIDUE.get()) {
                TaintAPI.add(level, this.chunkPosition(), ModConfig.SHAMBLER_OOZE_RESIDUE_TAINT.get());
            }
        }
        super.die(source);
    }

    /**
     * Releases copies one size smaller, never recursing without bound: the child size is
     * {@code size - 1} and a child at or below {@code minSplitSize} will not split again. Every child
     * is placed only into an already-loaded chunk with a free hitbox; a failed placement is dropped.
     */
    private void split(ServerLevel level) {
        int size = this.getOozeSize();
        int minSize = ModConfig.SHAMBLER_OOZE_MIN_SPLIT_SIZE.get();
        int count = ModConfig.SHAMBLER_OOZE_SPLIT_COUNT.get();
        if (size <= minSize || count <= 0) {
            return;
        }
        RandomSource random = RandomSource.create(this.getUUID().getMostSignificantBits() ^ this.tickCount);
        for (int i = 0; i < count; i++) {
            int x = this.blockPosition().getX() + random.nextInt(3) - 1;
            int z = this.blockPosition().getZ() + random.nextInt(3) - 1;
            if (!level.hasChunkAt(x, z)) {
                continue; // never load/generate a chunk for a split
            }
            ShamblerOoze child = ModEntities.SHAMBLER_OOZE.get().create(level, EntitySpawnReason.EVENT);
            if (child == null) {
                continue;
            }
            child.snapTo(x + 0.5, this.getY(), z + 0.5, random.nextFloat() * 360.0F, 0.0F);
            child.setOozeSize(size - 1);
            child.splitChild = true;
            child.setPersistenceRequired();
            if (level.noCollision(child)) {
                level.addFreshEntity(child);
            }
        }
    }

    // --- Residue corruption vector (DreadAura) --------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.CORRUPTION;
    }

    @Override
    public boolean isAuraActive() {
        return masterAuraEnabled() && ModConfig.ENABLE_CORRUPTION_SHAMBLER_OOZE.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.CORRUPTION_SHAMBLER_OOZE_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.CORRUPTION_SHAMBLER_OOZE_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.CORRUPTION_SHAMBLER_OOZE_MAX.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SHAMBLER_OOZE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SHAMBLER_OOZE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SHAMBLER_OOZE_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.SLIME_SQUISH_SMALL, 0.2F, 1.0F);
    }
}
