package com.sanchous98.eldritchhorror.entity.combat;

import com.sanchous98.eldritchhorror.entity.AncientOne;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Server-authoritative ability controller for every Ancient One. It owns the whole readable loop:
 *
 * <ol>
 *   <li><b>Choose</b> — a living player in range, an off-cooldown ability whose phase gate is met.
 *       The tie-break is a deterministic random seeded from the boss UUID and the world tick (never
 *       {@code Math.random}), so a replay of the same tick picks the same ability.</li>
 *   <li><b>Telegraph</b> — for {@link BossAbility#telegraphTicks()} the ability draws particles and
 *       the boss plays a warning sound; every player within 32 blocks gets an action-bar line. The
 *       wind-up deals no damage. This is when the player dodges.</li>
 *   <li><b>Interrupt</b> — damage the boss takes from players during the wind-up accumulates; at
 *       6% of its max health the ability is cancelled, the boss is staggered (NoAI 40 ticks) and
 *       becomes vulnerable (+25% damage taken) for 3 seconds. Bursting the boss is a real answer.</li>
 *   <li><b>Execute</b> — once, at the end of the wind-up, then 20–40 ticks of recovery so melee
 *       still matters. Enraged bosses wind up at ×0.8 and cool down at ×0.7.</li>
 * </ol>
 *
 * <p>Summons made by abilities are non-persistent and tracked by UUID; the controller discards them
 * after 1200 ticks (or when the boss dies). Maintenance runs from {@link AncientOne#tick()}, so
 * stagger and summon cleanup keep working while NoAI is set; ability ticking runs from the AI step.
 */
public final class BossCombat {

    /** Damage a player must deal during a wind-up to interrupt: 6% of the boss's max health. */
    private static final float STAGGER_FRACTION = 0.06F;
    /** How long the interrupted boss stands stunned. */
    private static final int STAGGER_TICKS = 40;
    /** The stagger's counter-attack window: +25% damage for three seconds. */
    private static final int VULNERABLE_TICKS = 60;
    /** Multiplier applied to incoming damage while vulnerable. */
    private static final float VULNERABLE_MULTIPLIER = 1.25F;
    /** Summoned minions are quietly removed after one minute. */
    private static final int SUMMON_LIFETIME_TICKS = 1200;
    /** Minimum pause after an ability, so the fight is not a pure ability turret. */
    private static final int RECOVERY_MIN_TICKS = 20;
    private static final int RECOVERY_SPAN_TICKS = 21;
    /** First sighting can repeat at most once every five minutes, per player per boss. */
    private static final long SIGHTING_COOLDOWN_TICKS = 6000L;
    /** The quiet proximity aura pulses once every five seconds, never per tick. */
    private static final int DREAD_AURA_PERIOD = 100;

    private final AncientOne boss;
    private final Map<String, Long> readyAt = new HashMap<>();
    private final Map<UUID, Long> summons = new HashMap<>();
    private final Map<UUID, Long> lastSighting = new HashMap<>();
    private final Set<UUID> nearby = new HashSet<>();
    private @Nullable List<BossAbility> abilities;
    private @Nullable BossAbility active;
    private int telegraphLeft;
    private int telegraphTotal;
    private float telegraphDamage;
    private int recoveryTicks;
    private int staggerTicks;
    private int vulnerableTicks;

    public BossCombat(AncientOne boss) {
        this.boss = boss;
    }

    // --- Ticking --------------------------------------------------------------------------------

    /**
     * Always-on maintenance, called from {@link AncientOne#tick()}. It deliberately does not touch
     * ability selection: that needs AI, and a staggered boss is NoAI for 40 ticks.
     */
    public void tickMaintenance(ServerLevel level) {
        long now = level.getGameTime();
        this.expireSummons(level, now);
        if (!this.boss.isAlive() || this.boss.isRemoved()) {
            this.discardSummons(level);
            this.active = null;
            return;
        }
        if (this.staggerTicks > 0) {
            this.staggerTicks--;
            if (this.staggerTicks == 0 && !this.boss.isSolved()) {
                this.boss.setNoAi(false);
            }
        }
        if (this.vulnerableTicks > 0) {
            this.vulnerableTicks--;
        }
        if (this.recoveryTicks > 0) {
            this.recoveryTicks--;
        }
        if (this.boss.isSolved()) {
            if (this.active != null) {
                this.cancelActive();
            }
            return;
        }
        this.tickDread(level, now);
    }

    /** Ability loop, called from the AI step after {@code super.customServerAiStep}. */
    public void tickAbilities(ServerLevel level) {
        if (!this.boss.isAlive() || this.boss.isSolved() || this.boss.isNoAi()
                || this.staggerTicks > 0) {
            return;
        }
        double maxRange = this.maxRange();
        if (maxRange <= 0.0) {
            return;
        }
        Player target = this.chooseTarget(level, maxRange);
        if (target == null) {
            if (this.active != null) {
                this.cancelActive();
            }
            return;
        }
        long now = level.getGameTime();
        if (this.active != null) {
            this.tickTelegraph(level, target, now);
            return;
        }
        if (this.recoveryTicks > 0) {
            return;
        }
        BossAbility next = this.chooseAbility(level, target, now);
        if (next != null) {
            this.begin(level, target, next, now);
        }
    }

    // --- Selection ------------------------------------------------------------------------------

    private @Nullable Player chooseTarget(ServerLevel level, double maxRange) {
        if (this.boss.getTarget() instanceof Player player && BossAbilities.isValidTarget(player)
                && this.boss.distanceToSqr(player) <= maxRange * maxRange) {
            return player;
        }
        Player nearest = level.getNearestPlayer(this.boss, maxRange);
        if (nearest != null && BossAbilities.isValidTarget(nearest)) {
            return nearest;
        }
        return null;
    }

    private @Nullable BossAbility chooseAbility(ServerLevel level, Player target, long now) {
        List<BossAbility> eligible = new ArrayList<>();
        int phaseRank = this.boss.phase().ordinal();
        for (BossAbility ability : this.abilities()) {
            if (ability.minPhase().ordinal() > phaseRank) {
                continue;
            }
            if (this.boss.distanceToSqr(target) > ability.range() * ability.range()) {
                continue;
            }
            Long ready = this.readyAt.get(ability.id());
            if (ready != null && now < ready) {
                continue;
            }
            if (!ability.canUse(this.boss, level, target)) {
                continue;
            }
            eligible.add(ability);
        }
        if (eligible.isEmpty()) {
            return null;
        }
        RandomSource random = this.random(level, 0x51ED);
        return eligible.get(random.nextInt(eligible.size()));
    }

    private void begin(ServerLevel level, Player target, BossAbility ability, long now) {
        boolean enraged = this.boss.phase() == AncientOne.Phase.ENRAGED;
        int baseTelegraph = Math.max(20, ability.telegraphTicks());
        this.telegraphTotal = enraged ? Math.max(20, Math.round(baseTelegraph * 0.8F)) : baseTelegraph;
        this.telegraphLeft = this.telegraphTotal;
        this.telegraphDamage = 0.0F;
        this.active = ability;
        long cooldown = ability.cooldownTicks();
        if (enraged) {
            cooldown = Math.max(20L, Math.round(cooldown * 0.7F));
        }
        this.readyAt.put(ability.id(), now + cooldown);
        this.boss.getNavigation().stop();
        this.boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        this.boss.playSound(ability.warningSound(), this.boss.soundVolume(), 0.8F);
        this.warn(level, ability.warning());
    }

    private void tickTelegraph(ServerLevel level, Player target, long now) {
        BossAbility ability = this.active;
        if (ability == null) {
            return;
        }
        if (!BossAbilities.isValidTarget(target)
                || this.boss.distanceToSqr(target) > ability.range() * ability.range() * 2.25) {
            this.cancelActive();
            return;
        }
        this.boss.getNavigation().stop();
        this.boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
        ability.telegraphTick(this.boss, level, target, this.telegraphLeft);
        if (this.telegraphLeft == this.telegraphTotal / 2) {
            this.warn(level, ability.warning());
        }
        this.telegraphLeft--;
        if (this.telegraphLeft <= 0) {
            this.active = null;
            this.telegraphDamage = 0.0F;
            ability.execute(this.boss, level, target);
            this.recoveryTicks = RECOVERY_MIN_TICKS + this.random(level, 0xC0DE).nextInt(RECOVERY_SPAN_TICKS);
        }
    }

    private void cancelActive() {
        this.active = null;
        this.telegraphLeft = 0;
        this.telegraphDamage = 0.0F;
        this.recoveryTicks = RECOVERY_MIN_TICKS;
    }

    // --- Dread presence -------------------------------------------------------------------------

    /**
     * The non-damaging pressure layer: a deep sting the first time a player comes near (at most once
     * per five minutes, per player per boss), then a quiet five-second particle pulse at the
     * player's eye line. Major presences add a brief nausea sting on sighting and a short
     * Slowness/Darkness pulse when enraged. Nothing here damages, stacks permanently, or fires while
     * the presence is solved.
     */
    private void tickDread(ServerLevel level, long now) {
        int rank = this.boss.phase().ordinal();
        boolean major = this.boss.majorPresence();
        double dread = this.boss.dreadRadius();
        if (now % DREAD_AURA_PERIOD == 0L) {
            Set<UUID> online = new HashSet<>();
            for (ServerPlayer onlinePlayer : level.players()) {
                online.add(onlinePlayer.getUUID());
            }
            this.nearby.retainAll(online);
        }
        for (ServerPlayer player : level.players()) {
            if (!BossAbilities.isValidTarget(player)) {
                continue;
            }
            UUID uuid = player.getUUID();
            double distanceSqr = player.distanceToSqr(this.boss);
            boolean inside = distanceSqr <= dread * dread;            if (!inside) {
                this.nearby.remove(uuid);
            } else if (this.nearby.add(uuid)) {
                // Entering, not every tick: first sighting is an event, not a repeating pulse.
                Long last = this.lastSighting.get(uuid);
                if (last == null || now - last >= SIGHTING_COOLDOWN_TICKS) {
                    this.lastSighting.put(uuid, now);
                    this.firstSighting(level, player, rank, major);
                    // The sting is the event for this pulse; do not also stack the aura on top of it.
                    continue;
                }
            }
            if (distanceSqr <= dread * dread
                    && now % DREAD_AURA_PERIOD == 0L) {
                this.proximityAura(level, player, rank, major);
            }
        }
    }

    private void firstSighting(ServerLevel level, ServerPlayer player, int rank, boolean major) {
        BossAbilities.effect(player, MobEffects.DARKNESS, 60 + rank * 20, 0);
        if (major) {
            BossAbilities.effect(player, MobEffects.NAUSEA, 40 + rank * 20, 0);
        }
        level.playSound(null, player.blockPosition(), this.boss.dreadSound(),
                SoundSource.AMBIENT, this.boss.soundVolume(), 0.5F);
        Component name = this.boss.getDisplayName().copy().withStyle(ChatFormatting.DARK_PURPLE);
        Component whisper = Component.empty()
                .append(Component.literal("You feel "))
                .append(name)
                .append(Component.literal(" turn its attention toward you."))
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        player.sendOverlayMessage(whisper);
    }

    private void proximityAura(ServerLevel level, ServerPlayer player, int rank, boolean major) {
        // The whole city feels a city-scale presence; how hard depends on how close it is and how
        // dangerous the presence is. A cosmic one at arm's length is a sanity emergency, the same
        // one across the river is a slow unease.
        double dread = Math.max(1.0, this.boss.dreadRadius());
        double distance = Math.sqrt(player.distanceToSqr(this.boss));
        double closeness = Math.clamp(1.0 - distance / dread, 0.0, 1.0);
        ParticleOptions particle = switch (rank) {
            case 0 -> ParticleTypes.SMOKE;
            case 1 -> ParticleTypes.SOUL;
            default -> ParticleTypes.SCULK_SOUL;
        };
        Vec3 eye = player.getEyePosition();
        Vec3 front = eye.add(player.getLookAngle().scale(2.5));
        int count = 1 + rank + (int) Math.round(closeness * 3.0);
        level.sendParticles(particle, front.x, front.y, front.z, count, 0.5, 0.35, 0.5, 0.0);
        double drain = (0.05 + 0.35 * closeness) * (1.0 + rank * 0.5) * (major ? 1.4 : 1.0);
        com.sanchous98.eldritchhorror.sanity.SanitySystem.add(player, -drain);
        if (major && closeness > 0.35 && this.boss.phase() == AncientOne.Phase.ENRAGED) {
            BossAbilities.effect(player, MobEffects.SLOWNESS, 40, 0);
            BossAbilities.effect(player, MobEffects.DARKNESS, 30, 0);
        }
    }

    // --- Damage / stagger -----------------------------------------------------------------------

    /** Damage multiplier applied by {@link AncientOne#hurtServer}; 1.0 outside the window. */
    public float damageTakenMultiplier() {
        return this.vulnerableTicks > 0 ? VULNERABLE_MULTIPLIER : 1.0F;
    }

    /** Called by {@link AncientOne#hurtServer} for every landed hit. */
    public void onDamaged(ServerLevel level, DamageSource source, float amount) {
        if (this.active == null || this.telegraphLeft <= 0 || this.staggerTicks > 0
                || !this.boss.isAlive() || this.boss.isSolved()) {
            return;
        }
        if (!(source.getEntity() instanceof Player)) {
            return;
        }
        this.telegraphDamage += amount;
        if (this.telegraphDamage >= this.boss.getMaxHealth() * STAGGER_FRACTION) {
            this.stagger(level);
        }
    }

    private void stagger(ServerLevel level) {
        this.cancelActive();
        this.staggerTicks = STAGGER_TICKS;
        this.vulnerableTicks = VULNERABLE_TICKS;
        this.boss.getNavigation().stop();
        this.boss.setNoAi(true);
        this.boss.playSound(SoundEvents.RAVAGER_STUNNED, this.boss.soundVolume(), 0.6F);
        double ring = Math.min(24.0, this.boss.scaled(2.6));
        BossAbilities.ring(this.boss, level, this.boss.position().add(0.0, 0.6, 0.0), ring,
                ParticleTypes.CRIT, 18);
        BossAbilities.telegraphBurst(level, this.boss, ParticleTypes.ENCHANTED_HIT, 10);
        this.warn(level, Component.literal("The presence is staggered — strike now!"));
    }

    /** Whether the boss is currently in its interrupt vulnerability window. */
    public boolean isVulnerable() {
        return this.vulnerableTicks > 0;
    }

    /** Whether an ability is currently winding up (used by debugging and tests). */
    public boolean isTelegraphing() {
        return this.active != null;
    }

    // --- Summons --------------------------------------------------------------------------------

    /** Spawns up to {@code count} non-persistent minions around the boss and tracks their lifetime. */
    public <T extends Mob> void summon(ServerLevel level, EntityType<T> type, int count,
                                       @Nullable Player target) {
        RandomSource random = this.random(level, 0xC0FFEE);
        int amount = this.boss.summonCount(count);
        for (int i = 0; i < amount; i++) {
            BlockPos spot = this.summonSpot(level, random);
            if (spot == null) {
                continue;
            }
            T mob = type.spawn(level, spot, EntitySpawnReason.MOB_SUMMONED);
            if (mob == null) {
                continue;
            }
            // Deliberately non-persistent: these are ability adds, not a new population.
            if (target != null) {
                mob.setTarget(target);
            }
            this.summons.put(mob.getUUID(), level.getGameTime() + SUMMON_LIFETIME_TICKS);
            level.sendParticles(ParticleTypes.POOF, mob.getX(), mob.getY() + 0.5, mob.getZ(),
                    8, 0.4, 0.4, 0.4, 0.01);
        }
    }

    private @Nullable BlockPos summonSpot(ServerLevel level, RandomSource random) {
        // Adds appear at the rim of the body, not under a 40-56 block foot.
        int spread = Math.max(3, Math.min(48, (int) Math.ceil(this.boss.getBbWidth() * 0.75)));
        for (int attempt = 0; attempt < 16; attempt++) {
            int dx = random.nextInt(spread * 2 + 1) - spread;
            int dz = random.nextInt(spread * 2 + 1) - spread;
            int dy = random.nextInt(3) - 1;
            BlockPos pos = this.boss.blockPosition().offset(dx, dy, dz);
            if (!level.isLoaded(pos) || !level.isLoaded(pos.above())) {
                continue;
            }
            BlockState floor = level.getBlockState(pos.below());
            if (floor.canBeReplaced()) {
                continue;
            }
            if (level.getBlockState(pos).canBeReplaced() && level.getBlockState(pos.above()).canBeReplaced()) {
                return pos;
            }
        }
        return null;
    }

    private void expireSummons(ServerLevel level, long now) {
        Iterator<Map.Entry<UUID, Long>> iterator = this.summons.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (entry.getValue() > now) {
                continue;
            }
            Entity entity = level.getEntity(entry.getKey());
            if (entity != null && entity.isAlive()) {
                entity.discard();
            }
            iterator.remove();
        }
    }

    private void discardSummons(ServerLevel level) {
        for (UUID uuid : this.summons.keySet()) {
            Entity entity = level.getEntity(uuid);
            if (entity != null) {
                entity.discard();
            }
        }
        this.summons.clear();
    }

    // --- Introspection / plumbing ---------------------------------------------------------------

    /** The ids of every ability in declaration order, for {@code /eh boss}. */
    public List<String> abilityIds() {
        return this.abilities().stream().map(BossAbility::id).toList();
    }

    /** All abilities this boss declares; resolved lazily so subclass fields are fully built. */
    public List<BossAbility> abilities() {
        if (this.abilities == null) {
            this.abilities = this.boss.abilityList().stream()
                    .map(ability -> (BossAbility) new ScaledAbility(ability, this.boss))
                    .toList();
        }
        return this.abilities;
    }

    private double maxRange() {
        double max = 0.0;
        for (BossAbility ability : this.abilities()) {
            max = Math.max(max, ability.range());
        }
        return max;
    }

    private void warn(ServerLevel level, Component message) {
        double radius = this.boss.warningRadius();
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this.boss) <= radius * radius) {
                player.sendOverlayMessage(message);
            }
        }
    }

    private RandomSource random(ServerLevel level, long salt) {
        UUID uuid = this.boss.getUUID();
        long seed = uuid.getMostSignificantBits()
                ^ Long.rotateLeft(uuid.getLeastSignificantBits(), 17)
                ^ level.getGameTime() * 0x9E3779B97F4A7C15L
                ^ salt;
        return RandomSource.create(seed);
    }

    /**
     * Distance-only view of an ability: range scales with the body, while telegraph ticks, cooldown,
     * phase gates, interrupt window and damage stay exactly as authored. This keeps the whole
     * per-boss ability table intact and makes the giant reach a property of its size.
     */
    private static final class ScaledAbility implements BossAbility {
        private final BossAbility delegate;
        private final double range;

        private ScaledAbility(BossAbility delegate, AncientOne boss) {
            this.delegate = delegate;
            double scaled = delegate.range() * boss.abilityScale();
            // Farm bosses stay local: even a 24-32 block body never gains an arena-wide opener.
            this.range = boss.farmable() ? Math.min(32.0, scaled) : scaled;
        }

        @Override
        public String id() {
            return this.delegate.id();
        }

        @Override
        public int cooldownTicks() {
            return this.delegate.cooldownTicks();
        }

        @Override
        public int telegraphTicks() {
            return this.delegate.telegraphTicks();
        }

        @Override
        public double range() {
            return this.range;
        }

        @Override
        public AncientOne.Phase minPhase() {
            return this.delegate.minPhase();
        }

        @Override
        public boolean canUse(AncientOne boss, ServerLevel level, Player target) {
            return this.delegate.canUse(boss, level, target);
        }

        @Override
        public void telegraphTick(AncientOne boss, ServerLevel level, Player target, int ticksLeft) {
            this.delegate.telegraphTick(boss, level, target, ticksLeft);
        }

        @Override
        public void execute(AncientOne boss, ServerLevel level, Player target) {
            this.delegate.execute(boss, level, target);
        }

        @Override
        public Component warning() {
            return this.delegate.warning();
        }

        @Override
        public SoundEvent warningSound() {
            return this.delegate.warningSound();
        }
    }
}
