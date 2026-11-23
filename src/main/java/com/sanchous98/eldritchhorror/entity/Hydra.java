package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.entity.combat.BossAbility;
import com.sanchous98.eldritchhorror.entity.combat.BossAbilities;
import com.sanchous98.eldritchhorror.entity.combat.SimpleBossAbility;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Hydra — "cut one head and the presence grows; the answer is never the sword" in the
 * <b>drowned_marsh</b> ({@code design/28-ancient-ones.md}). Where Yig draws the dead when struck, the
 * Hydra <b>buds a new head</b>: every landed hit (bounded by a cooldown, a radius count and a hard
 * cap) releases another {@link ShamblerOoze} — cutting it literally grows it. The only right answer is
 * the rite ({@code sever_hydra}, resolve {@link Solve#STILLED}): once severed the heads stop budding
 * and the presence stops draining for a time.
 *
 * <p>Its aura is the shared single-axis {@link BestiarySupport#tickAura} sanity pass, gated on
 * {@link #isSolved()}. Spawn attachment is the shared, bounded {@link AncientOneSpawner} {@code marsh}
 * pass (water on loaded ground); no per-boss ticker. Placeholder model: the vanilla creeper (a head).
 */
public final class Hydra extends AncientOne {

    /** Damage events closer together than this do not each bud a new head. */
    private static final int ESCALATION_COOLDOWN_TICKS = 40;
    /** Budded heads must stay within this radius to count toward the cap (loaded-only query). */
    private static final double ESCALATION_QUERY_RADIUS = 24.0;

    /**
     * Game tick of the last budding; transient (a reload simply re-arms it). Seeded to
     * {@code -ESCALATION_COOLDOWN_TICKS}, not {@link Long#MIN_VALUE}: the cooldown check subtracts
     * this from {@code gameTime}, and subtracting {@code Long.MIN_VALUE} overflows negative forever,
     * so the first hit would never bud a head.
     */
    private long lastEscalation = -ESCALATION_COOLDOWN_TICKS;

    public Hydra(EntityType<? extends Hydra> type, Level level) {
        super(type, level, BossEvent.BossBarColor.GREEN, Component.translatable("entity.eldritch_horror.hydra"));
        this.xpReward = 50;
        if (!ModConfig.HYDRA_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 150 HP, 6 attack, 0.22 speed, 40 follow range, scaled hide. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 150.0)
                .add(Attributes.ATTACK_DAMAGE, 11.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 5.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    // --- Escalation: being damaged buds a new head ----------------------------------------------

    /**
     * Every landed hit may release one more head, bounded by {@link #ESCALATION_COOLDOWN_TICKS}, the
     * configured cap within {@link #ESCALATION_QUERY_RADIUS}, and loaded chunks only. A severed
     * presence no longer buds: the answer is the rite, never the sword.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && this.isAlive() && !isSolved() && ModConfig.ENABLE_HYDRA_ESCALATION.get()
                && level.getGameTime() - this.lastEscalation >= ESCALATION_COOLDOWN_TICKS) {
            this.lastEscalation = level.getGameTime();
            this.budHead(level);
        }
        return hurt;
    }

    private void budHead(ServerLevel level) {
        int present = level.getEntitiesOfClass(ShamblerOoze.class,
                this.getBoundingBox().inflate(ESCALATION_QUERY_RADIUS), Mob::isAlive).size();
        if (present >= ModConfig.HYDRA_ESCALATION_CAP.get()) {
            return;
        }
        for (int i = 0; i < 8; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double distance = 2.0 + this.random.nextDouble() * 3.0;
            int x = (int) Math.floor(this.getX() + Math.cos(angle) * distance);
            int z = (int) Math.floor(this.getZ() + Math.sin(angle) * distance);
            if (!level.hasChunkAt(x, z)) {
                continue; // never force-load/generate a chunk to bud
            }
            int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            ShamblerOoze head = ModEntities.SHAMBLER_OOZE.get().spawn(level, new BlockPos(x, y, z),
                    EntitySpawnReason.MOB_SUMMONED);
            if (head != null) {
                head.setPersistenceRequired();
                return; // one per escalation, never a swarm
            }
        }
    }

    // --- Aura: SANITY (the heads' gaze) ---------------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_SANITY_HYDRA.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.SANITY_HYDRA_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_HYDRA_RADIUS.get();
    }

    /** Severed: the heads stop budding and the gaze dulls. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.STILLED) {
            this.playSound(ModSounds.HYDRA_DEATH.get(), 1.0F, 0.5F);
        }
    }

    // --- Abilities (BossCombat) -----------------------------------------------------------------

    @Override
    protected List<BossAbility> abilities() {
        return List.of(
                SimpleBossAbility.builder("acid_spit")
                        .cooldown(120)
                        .telegraph(20)
                        .range(24.0)
                        .warning("Acid spit — move!")
                        .sound(SoundEvents.SHULKER_SHOOT)
                        .onlyWhen((boss, level, target) -> boss.hasLineOfSight(target))
                        .onTelegraph((boss, level, target, ticksLeft) -> BossAbilities.line(level,
                                boss.getEyePosition(), target.getEyePosition(), ParticleTypes.ITEM_SLIME, 8))
                        .onExecute((boss, level, target) -> {
                            BossAbilities.line(level, boss.getEyePosition(), target.getEyePosition(),
                                    ParticleTypes.ITEM_SLIME, 10);
                            BossAbilities.hit(boss, level, target, 7.0F);
                            BossAbilities.venom(target, 80, 0);
                        })
                        .build(),
                SimpleBossAbility.builder("many_heads")
                        .cooldown(160)
                        .telegraph(22)
                        .range(10.0)
                        .minPhase(Phase.STIRRING)
                        .warning("Heads strike from every side!")
                        .sound(SoundEvents.SPIDER_HURT)
                        .onTelegraph((boss, level, target, ticksLeft) ->
                                BossAbilities.telegraphBurst(level, boss, ParticleTypes.CRIT, 5))
                        .onExecute((boss, level, target) -> BossAbilities.cone(level, boss, 8.0, 75.0, player -> {
                            BossAbilities.hit(boss, level, player, 8.0F);
                            BossAbilities.effect(player, MobEffects.WEAKNESS, 60, 0);
                        }))
                        .build()
        );
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.HYDRA_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.HYDRA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.HYDRA_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 90;
    }
}
