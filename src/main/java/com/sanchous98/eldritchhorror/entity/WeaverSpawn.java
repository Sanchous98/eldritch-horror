package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Weaver spawn — the nest-guard (replaces the spider/cave-spider role;
 * {@code design/25-bestiary-and-entities.md}: "skittering nest-guard; webs a room shut"). It is
 * deliberately <b>distinct from the veil stalker</b>, which took the ambush/ceiling niche: the
 * weaver instead makes a place <i>unenterable</i>. On arrival it places a small, deterministic,
 * capped patch of cobwebs around its nest (server-side, loaded chunks only, config-gated), and its
 * bite slows. Climbing is shared with the veil stalker, but the hook is the web and the slow.
 *
 * <p>Base {@link Monster}; the web pass runs once, on the first server tick after spawn, so it costs
 * nothing once the nest is built. Its dread is a small sanity drain on the shared {@link DreadAura}.
 */
public final class WeaverSpawn extends Monster implements DreadAura {

    /** Synced climbing flag, mirroring vanilla Spider's (a single bit). */
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID =
            SynchedEntityData.defineId(WeaverSpawn.class, EntityDataSerializers.BYTE);

    /** Whether the one-shot nest web has already been placed (server-side, persisted). */
    private boolean websPlaced;

    /** Save key for {@link #websPlaced}, so a reload does not re-web the nest. */
    private static final String TAG_WEBS_PLACED = "WebsPlaced";

    public WeaverSpawn(EntityType<? extends WeaverSpawn> type, Level level) {
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

    /** Base attributes: 14 HP, 2 attack, 0.3 speed, 32 follow range (fast, fragile nest-guard). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
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
            this.setClimbing(ModConfig.ENABLE_WEAVER_CLIMBING.get() && this.horizontalCollision);
            if (!this.websPlaced) {
                this.websPlaced = true;
                if (this.level() instanceof ServerLevel serverLevel && ModConfig.ENABLE_WEAVER_WEBS.get()) {
                    placeNestWebs(serverLevel);
                }
            }
        }
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbing() && ModConfig.ENABLE_WEAVER_CLIMBING.get();
    }

    public boolean isClimbing() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setClimbing(boolean value) {
        byte flags = this.entityData.get(DATA_FLAGS_ID);
        flags = value ? (byte) (flags | 1) : (byte) (flags & -2);
        this.entityData.set(DATA_FLAGS_ID, flags);
    }

    /**
     * The weaver's hook: a small, deterministic patch of cobwebs near the spawn point. Bounded three
     * ways — a hard block cap from config, a small fixed radius, and loaded-chunks-only (never
     * force-loads). Only replaces air, so it cannot carve a build. Called exactly once.
     */
    private void placeNestWebs(ServerLevel level) {
        int maxBlocks = ModConfig.WEAVER_WEB_MAX_BLOCKS.get();
        if (maxBlocks <= 0) {
            return;
        }
        BlockPos origin = this.blockPosition();
        RandomSource random = RandomSource.create(this.getUUID().getMostSignificantBits() ^ this.tickCount);
        int placed = 0;
        int probes = maxBlocks * 8;
        for (int i = 0; i < probes && placed < maxBlocks; i++) {
            int x = origin.getX() + random.nextInt(7) - 3;
            int y = origin.getY() + random.nextInt(5) - 2;
            int z = origin.getZ() + random.nextInt(7) - 3;
            BlockPos candidate = new BlockPos(x, y, z);
            if (!level.hasChunkAt(candidate) || !level.isLoaded(candidate)) {
                continue; // never load/generate a chunk just to web it
            }
            if (!level.getBlockState(candidate).isAir()) {
                continue;
            }
            if (level.setBlock(candidate, Blocks.COBWEB.defaultBlockState(), Block.UPDATE_ALL)) {
                placed++;
            }
        }
    }

    /** The bite slows: a little of the web ends up in the wound (the cave-spider shape). */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt && ModConfig.ENABLE_WEAVER_SLOW_BITE.get() && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 0), this);
        }
        return hurt;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setPersistenceRequired();
        return data;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean(TAG_WEBS_PLACED, this.websPlaced);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.websPlaced = input.getBooleanOr(TAG_WEBS_PLACED, false);
    }

    // --- Skitter sanity drain (DreadAura) -------------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        return ModConfig.ENABLE_SANITY.get() && ModConfig.ENABLE_SANITY_WEAVER_SPAWN.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_WEAVER_SPAWN_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_WEAVER_SPAWN_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.SANITY_WEAVER_SPAWN_MAX.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.WEAVER_SPAWN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WEAVER_SPAWN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WEAVER_SPAWN_AMBIENT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.12F, 1.0F);
    }
}
