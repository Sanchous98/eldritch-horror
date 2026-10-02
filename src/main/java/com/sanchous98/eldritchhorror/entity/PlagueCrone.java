package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.cult.CultRank;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Plague crone — the hag-alchemist of the cults (replaces the witch role;
 * {@code design/25-bestiary-and-entities.md}: "hag-alchemist … trades curses for taint"). It is a
 * <b>debuffer, not a melee bruiser</b>: melee is slow and weak, and its real hook is a bounded curse
 * applied on a cooldown to a hostile-standing player close by (poison + weakness, fixed short
 * durations from config). Its corruption vector runs on the shared {@link DreadAura}.
 *
 * <p>The curse respects cult standing: a player above the threshold is not cursed and is not a
 * target. Bounded by cooldown, radius and effect duration; deterministic, server-only.
 */
public final class PlagueCrone extends Monster implements DreadAura {

    /** Ticks until the next curse; counted down server-side. */
    private int curseCooldown;

    private final CultIdentity identity = new CultIdentity(CultistSupport.defaultCult(), CultRank.NEUTRAL);

    public PlagueCrone(EntityType<? extends PlagueCrone> type, Level level) {
        super(type, level);
        this.xpReward = 7;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 0.8, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> target instanceof ServerPlayer player
                        && CultistSupport.hostileTo(player, this.identity.cultId(), ModConfig.CULT_HOSTILE_BELOW.get())));
    }

    /** Base attributes: 18 HP, 2 attack, slow — a debuffer must not also be a bruiser. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.FOLLOW_RANGE, 28.0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        CultIdentity rolled = CultIdentity.random(this.getRandom());
        this.identity.set(rolled.cultId(), rolled.rank());
        this.curseCooldown = ModConfig.PLAGUE_CRONE_CURSE_COOLDOWN_TICKS.get();
        this.setPersistenceRequired();
        return data;
    }

    public CultIdentity identity() {
        return this.identity;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() || !ModConfig.ENABLE_PLAGUE_CRONE_CURSE.get()) {
            return;
        }
        if (--this.curseCooldown > 0) {
            return;
        }
        Player target = this.level().getNearestPlayer(this, ModConfig.PLAGUE_CRONE_CURSE_RADIUS.get());
        if (!(target instanceof ServerPlayer serverPlayer)
                || !CultistSupport.hostileTo(serverPlayer, this.identity.cultId(), ModConfig.CULT_HOSTILE_BELOW.get())) {
            return;
        }
        // Only a landed curse costs the cooldown; a whiffed attempt retries next tick.
        this.curseCooldown = ModConfig.PLAGUE_CRONE_CURSE_COOLDOWN_TICKS.get();
        int duration = ModConfig.PLAGUE_CRONE_CURSE_DURATION_TICKS.get();
        target.addEffect(new MobEffectInstance(MobEffects.POISON, duration, 0), this);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0), this);
        this.playSound(SoundEvents.WITCH_DRINK, 1.0F, 0.6F);
    }

    // --- Curse corruption vector (DreadAura) ----------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.CORRUPTION;
    }

    @Override
    public boolean isAuraActive() {
        return masterAuraEnabled() && ModConfig.ENABLE_CORRUPTION_PLAGUE_CRONE.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.CORRUPTION_PLAGUE_CRONE_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.CORRUPTION_PLAGUE_CRONE_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.CORRUPTION_PLAGUE_CRONE_MAX.get();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        this.identity.save(output);
        output.putInt("CurseCooldown", this.curseCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.identity.load(input);
        this.curseCooldown = input.getIntOr("CurseCooldown", ModConfig.PLAGUE_CRONE_CURSE_COOLDOWN_TICKS.get());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.PLAGUE_CRONE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.PLAGUE_CRONE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.PLAGUE_CRONE_AMBIENT.get();
    }
}
