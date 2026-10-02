package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Yig — "the more you kill, the more are drawn" in the <b>ashen_waste</b> ({@code design/28}).
 * Every time it is damaged (i.e. every time you choose the sword) it may draw another of the dead
 * (a {@link RisenHusk}) to the death, bounded by a cooldown, a radius count and a hard cap so the
 * escalation can never run away. The only right answer is the rite ({@code appease_yig}, resolve
 * {@link Solve#SOOTHED}): once appeased the presence stops drinking the deaths and stops draining.
 *
 * <p>Its aura is the shared single-axis {@link BestiarySupport#tickAura} corruption pass
 * ({@link DreadAuraTicker}), gated on {@link #isSolved()}. Spawn attachment is the shared, bounded
 * {@link AncientOneSpawner} {@code ashen} pass (tainted loaded ground); no per-boss ticker.
 * Placeholder model: the vanilla slime cube.
 */
public final class Yig extends AncientOne {

    /** Damage events closer together than this do not each draw a new revenant. */
    private static final int ESCALATION_COOLDOWN_TICKS = 40;
    /** Drawn dead must stay within this radius to count toward the cap (loaded-only query). */
    private static final double ESCALATION_QUERY_RADIUS = 24.0;

    /** Game tick of the last escalation; transient (a reload simply re-arms it). */
    private long lastEscalation = Long.MIN_VALUE;

    public Yig(EntityType<? extends Yig> type, Level level) {
        super(type, level, BossEvent.BossBarColor.GREEN, Component.translatable("entity.eldritch_horror.yig"));
        this.xpReward = 45;
        if (!ModConfig.YIG_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 140 HP, 6 attack, 0.28 speed, 40 follow range, moderate. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 140.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    // --- Escalation: being damaged draws the dead -----------------------------------------------

    /**
     * Every landed hit may draw one more revenant, bounded by {@link #ESCALATION_COOLDOWN_TICKS},
     * the configured cap within {@link #ESCALATION_QUERY_RADIUS}, and loaded chunks only. An
     * appeased presence no longer drinks the deaths, so the escalation stops.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && this.isAlive() && !isSolved() && ModConfig.ENABLE_YIG_ESCALATION.get()
                && level.getGameTime() - this.lastEscalation >= ESCALATION_COOLDOWN_TICKS) {
            this.lastEscalation = level.getGameTime();
            this.drawTheDead(level);
        }
        return hurt;
    }

    private void drawTheDead(ServerLevel level) {
        int present = level.getEntitiesOfClass(RisenHusk.class,
                this.getBoundingBox().inflate(ESCALATION_QUERY_RADIUS), Mob::isAlive).size();
        if (present >= ModConfig.YIG_ESCALATION_CAP.get()) {
            return;
        }
        for (int i = 0; i < 8; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double distance = 2.0 + this.random.nextDouble() * 3.0;
            int x = (int) Math.floor(this.getX() + Math.cos(angle) * distance);
            int z = (int) Math.floor(this.getZ() + Math.sin(angle) * distance);
            if (!level.hasChunkAt(x, z)) {
                continue; // never force-load/generate a chunk to summon
            }
            int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            RisenHusk husk = ModEntities.RISEN_HUSK.get().spawn(level, new BlockPos(x, y, z),
                    EntitySpawnReason.MOB_SUMMONED);
            if (husk != null) {
                husk.setPersistenceRequired();
                return; // one per escalation, never a swarm
            }
        }
    }

    // --- Aura: CORRUPTION (fed by the deaths) ---------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.CORRUPTION;
    }

    @Override
    public boolean isAuraActive() {
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_CORRUPTION_YIG.get();
    }

    @Override
    public double auraRate() {
        return ModConfig.CORRUPTION_YIG_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.CORRUPTION_YIG_RADIUS.get();
    }

    /** Appeased: the deaths no longer feed it. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.SOOTHED) {
            this.playSound(ModSounds.YIG_DEATH.get(), 1.0F, 0.5F);
        }
    }

    // --- Presence -------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.YIG_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.YIG_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.YIG_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 100;
    }

    /** Escalates at low health only; the world state is the swarm it has drawn. */
    @Override
    public Phase phaseFor(ServerLevel level, float healthFraction) {
        return healthFraction <= ModConfig.BOSS_ENRAGE_HEALTH.get() ? Phase.ENRAGED : Phase.DORMANT;
    }
}
