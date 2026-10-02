package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.cult.CultRank;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Cult raider — the war-band skirmisher (the pillager replacement; {@code design/25-bestiary-and-entities.md}:
 * "cult war-band, rank-scaled"). <b>Chosen role: fast melee</b>, not ranged — the simplest readable
 * shape for a placeholder art pass and it needs no projectile render state (the same call the bone
 * choir made). Its hook is faction standing: it only attacks a player whose reputation with its own
 * cult is below the configured threshold, so a player in the cult's favour is passed by. The
 * faction's shared low sanity aura is its presence.
 */
public final class CultRaider extends Monster implements DreadAura {

    private final CultIdentity identity = new CultIdentity(CultistSupport.defaultCult(), CultRank.NEUTRAL);

    public CultRaider(EntityType<? extends CultRaider> type, Level level) {
        super(type, level);
        this.xpReward = 6;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.25, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.1));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (target, level) -> target instanceof ServerPlayer player
                        && CultistSupport.hostileTo(player, this.identity.cultId(), ModConfig.CULT_HOSTILE_BELOW.get())));
    }

    /** Base attributes: 14 HP, 3 attack, fast (0.32) — a skirmisher, not a bruiser. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.FOLLOW_RANGE, 36.0);
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


    // --- Faction presence sanity drain (DreadAura) ---------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        return masterAuraEnabled() && ModConfig.ENABLE_SANITY_CULT_RAIDER.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_CULT_RAIDER_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_CULT_RAIDER_RADIUS.get();
    }

    @Override
    public int auraMaxStack() {
        return ModConfig.SANITY_CULT_RAIDER_MAX.get();
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
        return ModSounds.CULT_RAIDER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CULT_RAIDER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CULT_RAIDER_AMBIENT.get();
    }
}
