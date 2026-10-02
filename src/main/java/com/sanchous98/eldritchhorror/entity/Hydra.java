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

    /** Game tick of the last budding; transient (a reload simply re-arms it). */
    private long lastEscalation = Long.MIN_VALUE;

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
                .add(Attributes.ATTACK_DAMAGE, 6.0)
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
        return !isSolved() && ModConfig.ENABLE_SANITY.get() && ModConfig.ENABLE_SANITY_HYDRA.get();
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

    /** Escalates at low health only; the world state is the heads it has budded. */
    @Override
    public Phase phaseFor(ServerLevel level, float healthFraction) {
        return healthFraction <= ModConfig.BOSS_ENRAGE_HEALTH.get() ? Phase.ENRAGED : Phase.DORMANT;
    }
}
