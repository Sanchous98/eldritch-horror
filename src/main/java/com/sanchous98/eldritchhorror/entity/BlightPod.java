package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.corruption.TaintAPI;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

/**
 * Blight pod — the walking corruption vector (replaces the creeper role;
 * {@code design/25-bestiary-and-entities.md}). It does not explode; on death it releases a
 * <b>bounded</b> spore burst: a small chunk patch is tainted through {@link TaintAPI} and a handful
 * of blocks become {@code sculk} / our {@code tainted_soil}. The hard cap on placed blocks keeps the
 * terrain edit tiny. Its aura is a low, close corruption vector on the shared {@link DreadAura}
 * framework.
 *
 * <p>Base {@link Monster}; melee goal composition is the risen husk's, verified in 26.3. The burst
 * is server-side only and touches loaded chunks only ({@link TaintAPI#add(ServerLevel, ChunkPos,
 * double)} is a no-op for an unloaded chunk).
 */
public final class BlightPod extends Monster implements DreadAura {

    /** Resolved lazily from the registry; {@code Blocks.AIR} until the mod's block exists. */
    private static @Nullable Block taintedSoil;

    public BlightPod(EntityType<? extends BlightPod> type, Level level) {
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

    /** Base attributes: 16 HP, 2 attack, 0.24 speed, 32 follow range (a slow vector, not a bruiser). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setPersistenceRequired();
        return data;
    }

    /** A creeper-role burst, but of taint rather than fire. Server-side and bounded. */
    @Override
    public void die(DamageSource source) {
        if (this.level() instanceof ServerLevel level) {
            burst(level);
        }
        super.die(source);
    }

    /** Taints a small loaded chunk patch, then places at most the configured handful of blocks. */
    private void burst(ServerLevel level) {
        int chunkRadius = ModConfig.BLIGHT_POD_BURST_RADIUS.get();
        double taint = ModConfig.BLIGHT_POD_BURST_TAINT.get();
        ChunkPos centre = this.chunkPosition();
        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                TaintAPI.add(level, new ChunkPos(centre.x() + dx, centre.z() + dz), taint);
            }
        }

        int maxBlocks = ModConfig.BLIGHT_POD_BURST_MAX_BLOCKS.get();
        if (maxBlocks <= 0) {
            return;
        }
        ensureBlocks();
        BlockPos origin = this.blockPosition();
        RandomSource random = RandomSource.create(this.getUUID().getMostSignificantBits() ^ this.tickCount);
        int placed = 0;
        int probes = maxBlocks * 8;
        for (int i = 0; i < probes && placed < maxBlocks; i++) {
            int x = origin.getX() + random.nextInt(5) - 2;
            int z = origin.getZ() + random.nextInt(5) - 2;
            if (!level.hasChunkAt(x, z)) {
                continue; // never load/generate a chunk for a death burst
            }
            int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            BlockPos ground = new BlockPos(x, surfaceY - 1, z);
            BlockPos air = new BlockPos(x, surfaceY, z);
            if (!level.isLoaded(ground)) {
                continue;
            }
            BlockState old = level.getBlockState(ground);
            if (old.isAir() || old.hasBlockEntity() || !old.getFluidState().isEmpty()) {
                continue;
            }
            // Only the exposed top block of a column open to the sky: never a cave, roof or build.
            if (!level.getBlockState(air).isAir() || !level.canSeeSky(air)) {
                continue;
            }
            BlockState replacement = (placed % 2 == 0 || taintedSoil == Blocks.AIR)
                    ? Blocks.SCULK.defaultBlockState()
                    : taintedSoil.defaultBlockState();
            if (level.setBlock(ground, replacement, Block.UPDATE_ALL)) {
                placed++;
            }
        }
    }

    /** Resolve {@code tainted_soil} from the registry once (blocks are registered at setup). */
    private static void ensureBlocks() {
        if (taintedSoil == null) {
            taintedSoil = BuiltInRegistries.BLOCK.getValue(EldritchHorror.id("tainted_soil"));
        }
    }

    // --- Spore corruption vector (DreadAura) ----------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.CORRUPTION;
    }

    @Override
    public boolean isAuraActive() {
        return masterAuraEnabled() && ModConfig.ENABLE_CORRUPTION_BLIGHT_POD.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.CORRUPTION_BLIGHT_POD_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.CORRUPTION_BLIGHT_POD_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.CORRUPTION_BLIGHT_POD_MAX.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.BLIGHT_POD_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.BLIGHT_POD_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.BLIGHT_POD_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.CREEPER_PRIMED, 0.15F, 0.7F);
    }
}
