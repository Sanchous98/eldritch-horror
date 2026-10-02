package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.cult.CultRank;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
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
 * Cult zealot — the rank-scaled cult combatant ({@code design/25-bestiary-and-entities.md}: "cult
 * combatant that scales with the owning cult's rank"; the pillager/vindicator replacement). Base
 * {@link Monster}; it is a straightforward melee soldier whose distinguishing hook is <b>scaling</b>:
 * at spawn it draws a {@link CultIdentity} and multiplies {@code MAX_HEALTH}/{@code ATTACK_DAMAGE} by
 * its rank band ({@link CultistSupport#applyRankScaling}, config-gated). Its sanity drain is the
 * shared low {@link DreadAura}.
 *
 * <p>It is hostile only to players whose standing with its own cult is below the configured
 * threshold, so a player who has earned the cult's favour can walk past it — cult standing matters.
 */
public final class CultZealot extends Monster implements DreadAura {

    private final CultIdentity identity = new CultIdentity(CultistSupport.defaultCult(), CultRank.NEUTRAL);

    public CultZealot(EntityType<? extends CultZealot> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.05, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> target instanceof ServerPlayer player
                        && CultistSupport.hostileTo(player, this.identity.cultId(), ModConfig.CULT_HOSTILE_BELOW.get())));
    }

    /** Base attributes (before rank scaling): 16 HP, 3 attack, 0.28 speed. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        CultIdentity rolled = CultIdentity.random(this.getRandom());
        this.identity.set(rolled.cultId(), rolled.rank());
        CultistSupport.applyRankScaling(this, this.identity.rank(),
                ModConfig.ENABLE_CULT_RANK_SCALING.get(), ModConfig.CULT_RANK_SCALE_PER_BAND.get());
        this.setPersistenceRequired();
        return data;
    }


    // --- Low presence sanity drain (DreadAura) --------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        return masterAuraEnabled() && ModConfig.ENABLE_SANITY_CULT_ZEALOT.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_CULT_ZEALOT_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_CULT_ZEALOT_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.SANITY_CULT_ZEALOT_MAX.get();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        this.identity.save(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.identity.load(input);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CULT_ZEALOT_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CULT_ZEALOT_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CULT_ZEALOT_AMBIENT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.ZOMBIE_STEP, 0.14F, 1.05F);
    }
}
