package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.corruption.TaintAPI;
import com.sanchous98.eldritchhorror.endgame.Endgame;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * The Horror — the presence the whole mod points at ({@code design/18-bosses.md},
 * {@code design/11-endgame.md}). It is not a health-bar fight: the Horror is
 * <b>invulnerable</b> and its four stages escalate the world around it, because the real meter is
 * sanity and the real resolution is the endgame fork ({@code rite/RiteEngine}'s banish/summon rites).
 *
 * <p>Stages, one per {@code HORROR_STAGE_TICKS} (design/18's four phases):
 * <ol>
 *   <li>{@code ATTENTION} — it hangs still; reality distorts (nausea pulses) and its sanity aura
 *       drains at half strength.</li>
 *   <li>{@code APPROACH} — it moves; the arena taints and lesser swarms answer (capped).</li>
 *   <li>{@code GRASP} — melee reach and a stronger aura; star-spawn answer (capped).</li>
 *   <li>{@code DECISION} — escalation stops and the fork is available; the boss bar reads full.</li>
 * </ol>
 *
 * <p>Model reuse: the vanilla wither silhouette with our own flat placeholder texture; the
 * barely-embodied look is art's job.
 */
public final class TheHorror extends AncientOne {

    /** The four escalation stages of design/18. */
    public enum Stage {
        /** Reality distorts; no damage yet. */
        ATTENTION,
        /** The arena corrupts; adds spawn; it advances. */
        APPROACH,
        /** Direct attacks; the aura peaks. */
        GRASP,
        /** The endgame fork is open. */
        DECISION
    }

    private static final String TAG_AGE = "HorrorAge";
    /** Caps on the escalating adds, per design/18 (bounded, never a swarm). */
    private static final int LESSER_CAP = 4;
    private static final int STAR_CAP = 2;

    /** Ticks since it was awakened; drives the stages and survives a reload. */
    private int ageTicks;
    private Stage stage = Stage.ATTENTION;

    public TheHorror(EntityType<? extends TheHorror> type, Level level) {
        super(type, level, BossEvent.BossBarColor.PURPLE,
                Component.translatable("entity.eldritch_horror.the_horror"));
        this.xpReward = 200;
        // design/18: "sanity is the real health bar" — the encounter is resolved by the fork rites,
        // so conventional damage must not shortcut it (the same call Cthulhu makes, NeoForge's).
        this.setPermanentlyInvulnerable(true);
    }

    /** 300 HP (display only), 12 attack, slow, heavy, huge follow range. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    /**
     * The escalation clock. It lives in {@link #tick()} rather than {@code customServerAiStep}
     * because {@code ATTENTION} puts the presence on {@code NoAI}: the AI step stops, and a clock
     * there would freeze the encounter before it ever advanced (found by playtesting).
     */
    @Override
    public void tick() {
        super.tick(); // AncientOne runs its solve maintenance here
        if (this.level().isClientSide()) {
            return;
        }
        this.ageTicks++;
        if (this.ageTicks % 20 != 0) {
            return;
        }
        this.advance((ServerLevel) this.level());
        // The bar reads the escalation, not health: full = the decision is due.
        this.bossEvent().setProgress(this.escalationProgress());
    }

    /** One second of escalation: state transitions, the arena and the capped answers. */
    private void advance(ServerLevel level) {
        if (this.ageTicks <= 20) {
            // It arrives: the horn sounds once, before the first stage change (design/11).
            this.playSound(ModSounds.ENDGAME_AWAKEN.get(), 4.0F, 1.0F);
        }
        Stage next = this.stageFor(this.ageTicks);
        if (next != this.stage) {
            this.stage = next;
            this.playSound(ModSounds.THE_HORROR_STAGE.get(), 3.0F, next == Stage.DECISION ? 0.6F : 0.9F);
        }
        // The third outcome (design/11): at the decision an already-empty rift ledger seals the world
        // instead of opening the fork. Checked every second, so closing the last rift during the wait
        // seals too.
        if (this.stage == Stage.DECISION && Endgame.trySeal(level, this)) {
            return; // sealed: the fork never opens (the solve has already receded it)
        }
        switch (this.stage) {
            case ATTENTION -> {
                this.setNoAi(true); // it hangs, barely embodied
                this.distort(level);
            }
            case APPROACH -> {
                this.setNoAi(false); // it advances
                this.taintAround(level, 0.05);
                this.answer(level, ModEntities.LESSER_SWARM.get(), LESSER_CAP);
            }
            case GRASP -> {
                this.taintAround(level, 0.10);
                this.answer(level, ModEntities.STAR_SPAWN.get(), STAR_CAP);
            }
            case DECISION -> {
                // Nothing new: the fork rites decide the world now.
            }
        }
    }

    /** Which stage an age falls in. */
    public Stage stageFor(int age) {
        int per = Math.max(20, ModConfig.HORROR_STAGE_TICKS.get());
        if (age < per) {
            return Stage.ATTENTION;
        }
        if (age < per * 2) {
            return Stage.APPROACH;
        }
        if (age < per * 3) {
            return Stage.GRASP;
        }
        return Stage.DECISION;
    }

    /** @return escalation progress 0..1 for the boss bar (three stages, then the decision). */
    private float escalationProgress() {
        int per = Math.max(20, ModConfig.HORROR_STAGE_TICKS.get());
        return Mth.clamp(this.ageTicks / (float) (per * 3), 0.0F, 1.0F);
    }

    /** The current stage (server-side). */
    public Stage stage() {
        return this.stage;
    }

    /** Attention: short nausea pulses for players inside the aura, on a slow cadence. */
    private void distort(ServerLevel level) {
        if (this.ageTicks % 100 != 0) {
            return;
        }
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class,
                this.getBoundingBox().inflate(this.auraRadius()))) {
            player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 80, 0, true, false, true));
        }
    }

    /** Taints the loaded 3×3-chunk patch around the presence (bounded, loaded chunks only). */
    private void taintAround(ServerLevel level, double amount) {
        if (this.ageTicks % 100 != 0) {
            return;
        }
        ChunkPos centre = this.chunkPosition();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(centre.x() + dx, centre.z() + dz);
                if (chunk != null) {
                    TaintAPI.set(chunk, Math.min(1.0, TaintAPI.get(chunk) + amount));
                }
            }
        }
    }

    /** Spawns one answer per cadence while its kind stays under {@code cap} around the presence. */
    private void answer(ServerLevel level, EntityType<?> type, int cap) {
        if (this.ageTicks % 200 != 0) {
            return;
        }
        int existing = level.getEntitiesOfClass(Monster.class,
                this.getBoundingBox().inflate(this.auraRadius()), mob -> mob.getType() == type).size();
        if (existing >= cap) {
            return;
        }
        BlockPos spot = this.surfaceSpot(level, 6, 0);
        if (spot != null) {
            type.spawn(level, spot, EntitySpawnReason.MOB_SUMMONED);
        }
    }

    /** A loaded surface spot near the presence, or {@code null} (never force-loads a chunk). */
    private @Nullable BlockPos surfaceSpot(ServerLevel level, int radius, int salt) {
        for (int attempt = 0; attempt < 8; attempt++) {
            int angle = (salt * 8 + attempt) * 45;
            int x = this.blockPosition().getX() + Mth.floor(Mth.cos(angle * Mth.DEG_TO_RAD) * radius);
            int z = this.blockPosition().getZ() + Mth.floor(Mth.sin(angle * Mth.DEG_TO_RAD) * radius);
            if (!level.hasChunkAt(x, z)) {
                continue;
            }
            int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (level.isLoaded(pos) && level.getBlockState(pos).canBeReplaced()
                    && !level.getBlockState(pos.below()).canBeReplaced()) {
                return pos;
            }
        }
        return null;
    }

    // --- Presence -------------------------------------------------------------------------------

    /** Its aura is sanity, and it never stands down — not even after the summon (the world tipped). */
    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        if (this.solveKind() == Solve.SUMMONED) {
            return masterAuraEnabled() && ModConfig.ENABLE_SANITY_HORROR.get();
        }
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_SANITY_HORROR.get();
    }

    /** The aura deepens as it closes in: half at attention, peak at the grasp. */
    @Override
    public double auraRate() {
        double base = ModConfig.SANITY_HORROR_RATE.get();
        return switch (this.stage) {
            case ATTENTION -> base * 0.5;
            case APPROACH -> base;
            case GRASP -> base * 1.5;
            case DECISION -> base;
        };
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_HORROR_RADIUS.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.THE_HORROR_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.THE_HORROR_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.THE_HORROR_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(TAG_AGE, this.ageTicks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.ageTicks = input.getIntOr(TAG_AGE, 0);
        this.stage = this.stageFor(this.ageTicks);
    }
}
