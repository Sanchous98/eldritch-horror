package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * The Choir Leviathan — the drowned-temple boss of {@code design/18-bosses.md}: "a tide-thing the
 * Choir keeps half-asleep", summoned via the {@code drowned_blessing} chain and soothable by a
 * Choir rite instead of killed.
 *
 * <p>Three phases ride the shared {@link AncientOne} bands by health fraction:
 *
 * <ol>
 *   <li><b>Stirring</b> ({@link Phase#DORMANT}) — slow sweeps: it keeps a small, capped number of
 *       {@link WailHusk} mourners summoned around itself (loaded chunks only, bounded).</li>
 *   <li><b>Tidebreak</b> ({@link Phase#STIRRING}) — the arena floods: {@link #flood} places water
 *       only in already-loaded columns around it, strictly below each column's original surface and
 *       under a hard block cap, so the tide is bounded and cannot stack above the land.</li>
 *   <li><b>Frenzy</b> ({@link Phase#ENRAGED}) — fast and enraged: the same transient attack-damage
 *       modifier shape as Cthulhu's enrage plus a movement-speed step.</li>
 * </ol>
 *
 * <p>It is aquatic ({@link #canBreatheUnderwater}) so it owns its own flood. The non-combat solve
 * is {@code still_the_leviathan} (SOOTHE, taught by the Drowned Choir); a soothed Leviathan drops
 * its aura and stops hunting like every solved presence. The {@code drowned_blessing} rank gate and
 * the once-per-process summoning live in {@link ChoirLeviathanTrigger}.
 */
public final class ChoirLeviathan extends AncientOne {

    /** Transient attack-damage bump applied while the frenzy band is current. */
    private static final Identifier FRENZY_ATTACK =
            Identifier.fromNamespaceAndPath("eldritch_horror", "choir_leviathan_frenzy_attack");
    /** Transient movement-speed step applied while the frenzy band is current. */
    private static final Identifier FRENZY_SPEED =
            Identifier.fromNamespaceAndPath("eldritch_horror", "choir_leviathan_frenzy_speed");

    public ChoirLeviathan(EntityType<? extends ChoirLeviathan> type, Level level) {
        super(type, level, BossEvent.BossBarColor.BLUE,
                Component.translatable("entity.eldritch_horror.choir_leviathan"));
        this.xpReward = 80;
        if (!ModConfig.CHOIR_LEVIATHAN_KILLABLE.get()) {
            this.setPermanentlyInvulnerable(true);
        }
    }

    /** 200 HP, 8 attack, 0.22 speed, 48 follow range, heavy armour and knockback resistance. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    /** Aquatic: the flooded hall it owns cannot drown it. */
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    // --- Phase 2 (Tidebreak) and 3 (Frenzy) ------------------------------------------------------

    @Override
    protected void onPhaseChanged(ServerLevel level, Phase previous, Phase now) {
        if (now == Phase.STIRRING) {
            this.flood(level);
        }
        if (now == Phase.ENRAGED) {
            this.enrage();
        } else if (previous == Phase.ENRAGED) {
            this.calm();
        }
    }

    /**
     * Tidebreak: floods the loaded arena around the Leviathan in a bounded way. Every placement is
     * in an already-loaded chunk, strictly below that column's <b>original surface</b>
     * ({@code min(this.y, surfaceY - 1)}), and the total is hard-capped by
     * {@code choirLeviathanFloodBlocks}; a deterministic {@link RandomSource} (never
     * {@code Math.random}) makes the same flood replay the same way. Unloaded chunks are never
     * touched or generated.
     */
    private void flood(ServerLevel level) {
        int radius = ModConfig.CHOIR_LEVIATHAN_FLOOD_RADIUS.get();
        int maxBlocks = ModConfig.CHOIR_LEVIATHAN_FLOOD_BLOCKS.get();
        int depth = ModConfig.CHOIR_LEVIATHAN_FLOOD_DEPTH.get();
        if (radius <= 0 || maxBlocks <= 0 || depth <= 0) {
            return;
        }
        BlockPos origin = this.blockPosition();
        RandomSource random = RandomSource.create(this.getUUID().getMostSignificantBits() ^ this.tickCount);
        int placed = 0;
        int attempts = maxBlocks * BestiarySupport.PROBE_FACTOR;
        for (int i = 0; i < attempts && placed < maxBlocks; i++) {
            int x = origin.getX() + random.nextInt(radius * 2 + 1) - radius;
            int z = origin.getZ() + random.nextInt(radius * 2 + 1) - radius;
            if (level.getChunkSource().getChunkNow(x >> 4, z >> 4) == null) {
                continue; // never load/generate a chunk to flood
            }
            int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            int top = Math.min(origin.getY(), surfaceY - 1); // never above the original surface
            for (int y = top; y > top - depth && placed < maxBlocks; y--) {
                BlockPos pos = new BlockPos(x, y, z);
                if (!level.isLoaded(pos)) {
                    break; // never read unloaded cells
                }
                if (!level.getBlockState(pos).isAir()) {
                    continue; // only fill open air pockets; never displace terrain or builds
                }
                if (level.setBlock(pos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL)) {
                    placed++;
                }
            }
        }
        this.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 1.5F, 0.5F);
    }

    /** Frenzy: the enrage bump (Cthulhu's shape) plus a speed step, both transient while enraged. */
    private void enrage() {
        AttributeInstance attack = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.addOrUpdateTransientModifier(new AttributeModifier(
                    FRENZY_ATTACK, ModConfig.CHOIR_LEVIATHAN_FRENZY_ATTACK.get(),
                    AttributeModifier.Operation.ADD_VALUE));
        }
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.addOrUpdateTransientModifier(new AttributeModifier(
                    FRENZY_SPEED, ModConfig.CHOIR_LEVIATHAN_FRENZY_SPEED.get(),
                    AttributeModifier.Operation.ADD_VALUE));
        }
        this.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 1.5F, 0.6F);
    }

    /** Removes the frenzy modifiers if the presence is somehow calmed below the band. */
    private void calm() {
        AttributeInstance attack = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.removeModifier(FRENZY_ATTACK);
        }
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(FRENZY_SPEED);
        }
    }

    // --- Phase 1 (Stirring): the summons ---------------------------------------------------------

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        int interval = ModConfig.CHOIR_LEVIATHAN_SUMMON_INTERVAL_TICKS.get();
        if (this.phase() == Phase.DORMANT && interval > 0 && level.getGameTime() % interval == 0L) {
            this.summonMourner(level);
        }
    }

    /**
     * Keeps up to {@code choirLeviathanSummonCap} wail husks around the Leviathan while it is still
     * Stirring. One bounded attempt per pass: loaded chunks only, air spots above the terrain only,
     * deterministic sampling. The mourners are its "lesser swarms" (design/25 pairs the ghast role
     * with the Leviathan's spawns).
     */
    private void summonMourner(ServerLevel level) {
        int cap = ModConfig.CHOIR_LEVIATHAN_SUMMON_CAP.get();
        int radius = ModConfig.CHOIR_LEVIATHAN_SUMMON_RADIUS.get();
        if (cap <= 0 || radius <= 0) {
            return;
        }
        int present = level.getEntitiesOfClass(WailHusk.class, this.getBoundingBox().inflate(radius),
                husk -> husk.isAlive()).size();
        if (present >= cap) {
            return;
        }
        BlockPos origin = this.blockPosition();
        RandomSource random = RandomSource.create(this.getUUID().getLeastSignificantBits() ^ this.tickCount);
        for (int i = 0; i < BestiarySupport.PROBE_FACTOR; i++) {
            int x = origin.getX() + random.nextInt(radius * 2 + 1) - radius;
            int z = origin.getZ() + random.nextInt(radius * 2 + 1) - radius;
            if (level.getChunkSource().getChunkNow(x >> 4, z >> 4) == null) {
                continue; // never load/generate a chunk to summon
            }
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) + BestiarySupport.AIR_CLEARANCE;
            BlockPos spot = new BlockPos(x, y, z);
            if (!level.isLoaded(spot)
                    || !level.getBlockState(spot).isAir()
                    || !level.getBlockState(spot.above()).isAir()) {
                continue;
            }
            WailHusk husk = ModEntities.WAIL_HUSK.get().spawn(level, spot, EntitySpawnReason.EVENT);
            if (husk == null) {
                continue;
            }
            husk.setPersistenceRequired();
            return;
        }
    }

    // --- Aura: SANITY (the tide-song) ------------------------------------------------------------

    @Override
    public Axis auraAxis() {
        return Axis.SANITY;
    }

    @Override
    public boolean isAuraActive() {
        // Soothed: the tide-song is sung down and the presence sleeps again.
        return !isSolved() && masterAuraEnabled() && ModConfig.ENABLE_SANITY_CHOIR_LEVIATHAN.get();
    }

    @Override
    public double auraRate() {
        // The frenzy band works the tide harder.
        return this.phase() == Phase.ENRAGED
                ? ModConfig.SANITY_CHOIR_LEVIATHAN_RATE.get() * 1.5
                : ModConfig.SANITY_CHOIR_LEVIATHAN_RATE.get();
    }

    @Override
    public double auraRadius() {
        return ModConfig.SANITY_CHOIR_LEVIATHAN_RADIUS.get();
    }

    /** Soothed: the Choir answers and the Leviathan settles half-asleep. */
    @Override
    protected void onSolved(Solve kind) {
        if (kind == Solve.SOOTHED) {
            this.playSound(SoundEvents.ELDER_GUARDIAN_DEATH, 1.0F, 0.4F);
        }
    }

    // --- Presence --------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ELDER_GUARDIAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ELDER_GUARDIAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ELDER_GUARDIAN_DEATH;
    }

    /** Heard before seen: the tide-song carries across the water. */
    @Override
    protected float getSoundVolume() {
        return 3.0F;
    }

    /** Rarer than the default: a slow, vast presence, not a chatterbox. */
    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.ELDER_GUARDIAN_FLOP, 0.35F, 0.6F);
    }
}
