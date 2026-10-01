package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.cult.CultRank;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * Rite binder — the cult caster (the evoker replacement; {@code design/25-bestiary-and-entities.md}:
 * "the binder raises the dead"). <b>Hook: a rite outcome spawn.</b> On a config-gated cooldown, and
 * only when a hostile-standing player is near, it calls a bounded number of {@code risen_husk} to the
 * loaded ground around it. Bounded three ways: a per-rite cap, a standing {@code risen_husk} cap in
 * its radius, and loaded-chunks-only placement (a failed drop is simply skipped, never force-loads).
 * Its own presence is a corruption vector on the shared {@link DreadAura}.
 *
 * <p>Base {@link Monster}, melee but weak: the caster is not a bruiser, the raised dead are the
 * threat. Cult identity is held for the reputation gate and for the faction save data.
 */
public final class RiteBinder extends Monster implements DreadAura {

    /** Ticks until the next rite; counted down server-side. */
    private int riteCooldown;

    private final CultIdentity identity = new CultIdentity(CultistSupport.defaultCult(), CultRank.NEUTRAL);

    public RiteBinder(EntityType<? extends RiteBinder> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 0.9, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> target instanceof ServerPlayer player
                        && CultistSupport.hostileTo(player, this.identity.cultId(), ModConfig.CULT_HOSTILE_BELOW.get())));
    }

    /** Base attributes: 20 HP, 2 attack, slow — it stays back and calls. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        CultIdentity rolled = CultIdentity.random(this.getRandom());
        this.identity.set(rolled.cultId(), rolled.rank());
        this.riteCooldown = ModConfig.RITE_BINDER_COOLDOWN_TICKS.get();
        this.setPersistenceRequired();
        return data;
    }

    public CultIdentity identity() {
        return this.identity;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        if (!ModConfig.ENABLE_RITE_BINDER_RAISING.get()) {
            return;
        }
        if (--this.riteCooldown > 0) {
            return;
        }
        this.riteCooldown = ModConfig.RITE_BINDER_COOLDOWN_TICKS.get();
        ServerPlayer nearest = this.level().getNearestPlayer(this, 16.0) instanceof ServerPlayer sp ? sp : null;
        if (nearest == null || !CultistSupport.hostileTo(nearest, this.identity.cultId(), ModConfig.CULT_HOSTILE_BELOW.get())) {
            return;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            raiseTheDead(serverLevel);
        }
    }

    /**
     * Summons at most {@code riteBinderRaiseCount} risen husks on loaded ground around the binder,
     * never exceeding the standing cap nearby. Deterministic, loaded-only. The husks carry no cult
     * identity, so (unlike the binder) they do not respect the reputation gate.
     */
    private void raiseTheDead(ServerLevel level) {
        int perRite = ModConfig.RITE_BINDER_RAISE_COUNT.get();
        int cap = ModConfig.RITE_BINDER_RAISE_CAP.get();
        int radius = ModConfig.RITE_BINDER_RAISE_RADIUS.get();
        if (perRite <= 0 || cap <= 0) {
            return;
        }
        AABB area = this.getBoundingBox().inflate(radius);
        int present = level.getEntitiesOfClass(RisenHusk.class, area, husk -> husk.isAlive()).size();
        int budget = Math.min(perRite, cap - present);
        if (budget <= 0) {
            return;
        }
        for (int i = 0; i < budget; i++) {
            int x = this.blockPosition().getX() + this.getRandom().nextInt(7) - 3;
            int z = this.blockPosition().getZ() + this.getRandom().nextInt(7) - 3;
            if (!level.hasChunkAt(x, z)) {
                continue; // never force-load a chunk for a rite outcome
            }
            BlockPos spot = BestiarySupport.surfaceSpot(level, x, z, true);
            if (spot == null) {
                continue;
            }
            RisenHusk husk = ModEntities.RISEN_HUSK.get().spawn(level, spot, EntitySpawnReason.EVENT);
            if (husk != null) {
                husk.setPersistenceRequired();
            }
        }
        this.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0F, 0.7F);
    }

    // --- Rite corruption vector (DreadAura) -----------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.CORRUPTION;
    }

    @Override
    public boolean isAuraActive() {
        return ModConfig.ENABLE_CORRUPTION.get() && ModConfig.ENABLE_CORRUPTION_RITE_BINDER.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.CORRUPTION_RITE_BINDER_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.CORRUPTION_RITE_BINDER_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.CORRUPTION_RITE_BINDER_MAX.get();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        this.identity.save(output);
        output.putInt("RiteCooldown", this.riteCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.identity.load(input);
        this.riteCooldown = input.getIntOr("RiteCooldown", ModConfig.RITE_BINDER_COOLDOWN_TICKS.get());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.RITE_BINDER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.RITE_BINDER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.RITE_BINDER_AMBIENT.get();
    }
}
