package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.entity.combat.BossAbility;
import com.sanchous98.eldritchhorror.entity.combat.BossAbilities;
import com.sanchous98.eldritchhorror.entity.combat.BossCombat;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Shared base for every Ancient One (the boss layer, {@code design/28-ancient-ones.md}). It is the
 * reusable half of the framework: a server-owned boss bar, a health-and-world driven phase machine
 * with a change hook, and the {@link DreadAura} presence contract. It deliberately does <b>not</b>
 * carry a per-boss class: each Ancient One supplies only attributes, goals, aura numbers and (at
 * most) one phase hook.
 *
 * <p>Verified against the 26.3 patched sources:
 * <ul>
 *   <li>{@code net.minecraft.server.level.ServerBossEvent} is a public {@code BossEvent} with the
 *       constructor {@code (UUID, Component, BossBarColor, BossBarOverlay)} and
 *       {@code addPlayer}/{@code removePlayer}/{@code setProgress}/{@code setName}. The wither uses
 *       exactly this shape ({@code WitherBoss.bossEvent}), so we mirror it.</li>
 *   <li>{@code Mth.createInsecureUUID(RandomSource)} exists (same call the wither makes).</li>
 *   <li>{@code Entity.startSeenByPlayer}/{@code stopSeenByPlayer} are the tracking hooks the wither
 *       overrides; {@code Mob.removeWhenFarAway} is overridden so a persistent boss never despawns
 *       even if the persistence flag is somehow cleared.</li>
 *   <li>{@code customServerAiStep(ServerLevel)} is the server-AI tick the wither uses to push boss
 *       progress.</li>
 * </ul>
 */
public abstract class AncientOne extends Monster implements DreadAura {

    /** Coarse encounter band; driven by health fraction (and overridable with world state). */
    public enum Phase {
        /** Above the second threshold: the presence is merely present. */
        DORMANT,
        /** Between the two thresholds: the encounter has turned. */
        STIRRING,
        /** At or below the enrage threshold: the worst of it. */
        ENRAGED
    }

    /**
     * The non-combat solve state (design/28): a presence can be answered without a kill. This is
     * deliberately small and server-authoritative; the solve rites in {@code rite/RiteEngine} set it
     * through {@link #solve}.
     */
    public enum Solve {
        /** No solve applied. */
        NONE,
        /** {@code soothe_cthulhu}: asleep — awake but no longer hunting or draining. */
        SOOTHED,
        /** {@code draw_away_dunwich}: drawn off and retreating from the settlement. */
        BANISHED,
        /** {@code still_shub_niggurath}: aura suppressed until the solve window lapses. */
        STILLED,
        /**
         * The endgame summon ({@code design/11-endgame.md}): the presence is claimed, not killed and
         * not drawn off. It stops hunting and keeps its aura — the world has tipped.
         */
        SUMMONED
    }

    private static final String TAG_PHASE = "AncientOnePhase";
    private static final String TAG_SOLVE = "AncientOneSolve";
    private static final String TAG_SOLVE_UNTIL = "AncientOneSolveUntil";

    private final ServerBossEvent bossEvent;
    /** The ability controller: wind-up -> interrupt -> execute -> recovery. See {@link BossCombat}. */
    private final BossCombat combat;
    private Phase phase = Phase.DORMANT;
    /** How this presence has been solved without a kill; see {@link #solve}. */
    private Solve solve = Solve.NONE;
    /** Game tick at which {@link Solve#STILLED} lapses; {@code 0} for the other solves. */
    private long solveUntil;
    /** Cached size row; fixed for the life of the entity. */
    private final AncientOneSizes.Size size;

    protected AncientOne(EntityType<? extends AncientOne> type, Level level,
                         BossEvent.BossBarColor color, Component title) {
        super(type, level);
        // Mirrors WitherBoss: a per-instance random UUID so two of the same Ancient One never share
        // a bar, and the player's own display name when one is set.
        this.bossEvent = new ServerBossEvent(Mth.createInsecureUUID(this.random), title, color,
                BossEvent.BossBarOverlay.PROGRESS);
        this.combat = new BossCombat(this);
        this.size = AncientOneSizes.of(EntityType.getKey(type).getPath());
        // A city-scale body cannot use vanilla block collision: a 40-56 block box intersects every
        // street and building, and a 128 block box cannot fit a normal doorway. Like the Ender
        // Dragon, giants ignore block collisions and are kept on the ground by travel() instead.
        this.noPhysics = true;
    }

    // --- Size and scale (single source: AncientOneSizes) ---------------------------------------

    /** The canonical size row for this presence. */
    public final AncientOneSizes.Size size() {
        return this.size;
    }

    /** Body height in blocks; every distance below is derived from it. */
    public final double bodyHeight() {
        return this.getBbHeight();
    }

    /** Whether this presence is a farm boss: local abilities, no arena-wide reach. */
    public final boolean farmable() {
        return this.bodyHeight() <= AncientOneSizes.FARMABLE_MAX_HEIGHT;
    }

    /** Distance multiplier for ability ranges and area radii against the old 8-block baseline. */
    public final double abilityScale() {
        double raw = this.bodyHeight() / AncientOneSizes.DESIGN_BASELINE_HEIGHT;
        return this.farmable() ? Math.min(raw, 2.5) : raw;
    }

    /** Scales a designed distance (ability range, AoE radius, telegraph spread). */
    public final double scaled(double designed) {
        return designed * this.abilityScale();
    }

    /** Melee reach from the centre: about half the body height. */
    public final double meleeReach() {
        return this.size().meleeReach();
    }

    /** How far the non-damaging dread presence is felt: six body heights. */
    public final double dreadRadius() {
        return this.size().dreadRadius();
    }

    /** Range of the action-bar warning: the voice carries about 1.5 body heights. */
    public final double warningRadius() {
        return this.size().warningRadius();
    }

    /** Loudness of the wind-up/sting; vanilla scales the audible radius by this. */
    public final float soundVolume() {
        return this.size().soundVolume();
    }

    /** How many minions a summon ability should raise at this scale. */
    public final int summonCount(int base) {
        if (base <= 0) {
            return 0;
        }
        double factor = this.farmable() ? 1.5 : this.abilityScale();
        int cap = this.farmable() ? 6 : 24;
        return Math.min(cap, Math.max(base, (int) Math.round(base * factor)));
    }

    /** The ability controller; used by abilities that summon tracked minions. */
    public final BossCombat bossCombat() {
        return this.combat;
    }

    /**
     * The abilities this boss can use, in declaration order. The default is none; every boss in the
     * mod overrides it with two (three for the major presences).
     */
    protected List<BossAbility> abilities() {
        return List.of();
    }

    /** Package-boundary bridge for {@link BossCombat}; keeps the subclass-facing method protected. */
    public final List<BossAbility> abilityList() {
        return this.abilities();
    }

    /**
     * Whether this is a major presence (Cthulhu, Azathoth, Yog-Sothoth, Hastur, Nyarlathotep,
     * Shub-Niggurath, The Horror). Major presences get the brief nausea sting on first sighting and
     * the low-health dread pulse; the roaming and farm bosses only get the whisper and Darkness.
     */
    public boolean majorPresence() {
        return false;
    }

    /** The deep sting used by the dread layer: the ambient, falling back to the hurt sound. */
    public SoundEvent dreadSound() {
        SoundEvent ambient = this.getAmbientSound();
        if (ambient != null) {
            return ambient;
        }
        SoundEvent hurt = this.getHurtSound(this.damageSources().generic());
        return hurt != null ? hurt : SoundEvents.WARDEN_ROAR;
    }

    /** The bar, for subclasses that want to restyle it (colour/overlay set at construction). */
    protected final ServerBossEvent bossEvent() {
        return this.bossEvent;
    }

    // --- Boss bar plumbing (the wither's exact pattern) -----------------------------------------

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    // --- Phase machine --------------------------------------------------------------------------

    @Override
    protected void customServerAiStep(ServerLevel level) {
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.updatePhase(level);
        super.customServerAiStep(level);
        // Ability selection needs AI, so it lives in the AI step; stagger/summon maintenance lives in
        // tick() because a staggered boss is deliberately NoAI for 40 ticks.
        this.combat.tickAbilities(level);
        this.tickStomp(level);
        this.steerGiant(level);
    }

    // --- Giant movement and collision -----------------------------------------------------------

    /**
     * A city-scale presence cannot path through streets with a 40-56 block footprint, so it walks
     * straight at its target instead of using {@code GroundPathNavigation}. The AI step sets the
     * wanted position after the vanilla goals, so a melee goal still owns the attack itself.
     */
    private void steerGiant(ServerLevel level) {
        if (this.isNoAi() || this.isSolved() || this.isPassenger() || this.stompWindup > 0) {
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        double reach = Math.max(2.0, this.getBbWidth() * 0.5);
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        if (dx * dx + dz * dz <= reach * reach) {
            this.setSpeed(0.0F);
            this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0);
            this.getNavigation().stop();
            return;
        }
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (this.isCityScale() && this.blockedAhead(level, target)) {
            this.setSpeed(0.0F);
            this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0);
            this.getNavigation().stop();
            if (this.tickCount % 20 == 0) {
                this.smashAhead(level, target);
            }
            return;
        }
        this.setSpeed(1.0F);
        this.getMoveControl().setWantedPosition(target.getX(), this.getY(), target.getZ(), 1.0);
    }

    /** Step height ignores kerbs and staircases but never turns a 68-block body into a climber. */
    /** A city-scale presence never lands an instant hit; its close-range damage is the stomp. */
    @Override
    public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
        if (this.isCityScale()) {
            return false;
        }
        return super.doHurtTarget(level, target);
    }

    /** A stomp wind-up freezes the body, like an ability telegraph. */
    public boolean isWindingUp() {
        return this.stompWindup > 0;
    }

    @Override
    public float maxUpStep() {
        return Math.max(1.0F, Math.min(6.0F, (float) (this.getBbHeight() * 0.05)));
    }

    /**
     * Movement for a no-physics giant: the AI walks normally, then the body is snapped to the
     * highest floor within its step band. A ledge below the band is stepped over; a floor further
     * down is a fall, and the next tick catches it. This is what keeps a 128-block body out of the
     * thin city crust while letting it cross kerbs, rails and rubble.
     */
    @Override
    public void travel(Vec3 input) {
        if (this.isPassenger()) {
            super.travel(input);
            return;
        }
        // A stagger (NoAI), a solve or a wind-up freezes the body: input is dropped, but gravity and
        // the ground snap still keep its feet on the terrain.
        Vec3 motion = this.isNoAi() || this.combat.isTelegraphing() ? Vec3.ZERO : input;
        super.travel(motion);
        this.snapToGround();
    }

    /** Blocks a presence cannot break, so a giant never tunnels out of the world. */
    private static final java.util.Set<net.minecraft.world.level.block.Block> UNBREAKABLE =
            java.util.Set.of(net.minecraft.world.level.block.Blocks.BEDROCK,
                    net.minecraft.world.level.block.Blocks.BARRIER,
                    net.minecraft.world.level.block.Blocks.COMMAND_BLOCK,
                    net.minecraft.world.level.block.Blocks.CHAIN_COMMAND_BLOCK,
                    net.minecraft.world.level.block.Blocks.REPEATING_COMMAND_BLOCK,
                    net.minecraft.world.level.block.Blocks.STRUCTURE_BLOCK,
                    net.minecraft.world.level.block.Blocks.JIGSAW,
                    net.minecraft.world.level.block.Blocks.END_PORTAL,
                    net.minecraft.world.level.block.Blocks.END_PORTAL_FRAME,
                    net.minecraft.world.level.block.Blocks.END_GATEWAY,
                    net.minecraft.world.level.block.Blocks.LIGHT);

    /** Ticks left in the stomp wind-up; 0 means idle. */
    private int stompWindup;
    /** Where the last stomp was aimed. */
    private @Nullable Vec3 stompCentre;
    /** Ticks until the next stomp may start. */
    private int stompCooldown;

    /** City-scale presences (48+ blocks) break what they attack; farm bosses never do. */
    public boolean isCityScale() {
        return this.getBbHeight() >= 48.0;
    }

    /**
     * The close-range attack of a giant: it does not hit instantly, it raises a foot. A ring on the
     * ground marks the impact area for two seconds; a player who leaves the ring takes nothing.
     * This is what makes melee against a 96-block body survivable.
     */
    private void tickStomp(ServerLevel level) {
        if (!this.isCityScale() || this.isNoAi() || this.isSolved() || this.isPassenger()) {
            return;
        }
        if (this.stompCooldown > 0) {
            this.stompCooldown--;
        }
        double radius = Math.max(4.0, Math.min(20.0, this.getBbWidth() * 0.5));
        if (this.stompWindup > 0) {
            this.stompWindup--;
            if (this.stompCentre != null) {
                BossAbilities.ring(this, level, this.stompCentre, radius + 0.5,
                        net.minecraft.core.particles.ParticleTypes.CRIT, 32);
            }
            if (this.stompWindup == 0 && this.stompCentre != null) {
                Vec3 centre = this.stompCentre;
                this.stompCentre = null;
                this.stompCooldown = 90;
                float damage = (float) Math.max(4.0, this.getBbHeight() * 0.25);
                for (net.minecraft.world.entity.player.Player player : BossAbilities.playersAround(this, level, centre, radius)) {
                    BossAbilities.hit(this, level, player, damage);
                }
                smashArea(level, centre, radius, 12);
                level.playSound(null, centre.x, centre.y, centre.z,
                        net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                        net.minecraft.sounds.SoundSource.HOSTILE, 2.0F, 0.5F);
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                        centre.x, centre.y + 1.0, centre.z, 2, 0.5, 0.2, 0.5, 0.0);
            }
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || this.stompCooldown > 0) {
            return;
        }
        double reach = Math.max(8.0, Math.min(24.0, this.getBbWidth() * 0.6));
        if (this.distanceToSqr(target) > reach * reach) {
            return;
        }
        this.stompWindup = 40;
        this.stompCentre = target.position();
        level.playSound(null, this.getX(), this.getY() + 2.0, this.getZ(),
                net.minecraft.sounds.SoundEvents.RAVAGER_ROAR,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.4F, 0.6F);
    }

    /** Breaks every breakable block in a short cylinder; used by the stomp impact. */
    private void smashArea(ServerLevel level, Vec3 centre, double radius, int height) {
        int cx = Mth.floor(centre.x);
        int cz = Mth.floor(centre.z);
        int feet = Mth.floor(centre.y);
        int r = Mth.ceil(radius);
        int budget = 160;
        for (int y = feet - 1; y <= feet + height && budget > 0; y++) {
            for (int dx = -r; dx <= r && budget > 0; dx++) {
                for (int dz = -r; dz <= r && budget > 0; dz++) {
                    if (dx * dx + dz * dz > r * r) {
                        continue;
                    }
                    BlockPos pos = new BlockPos(cx + dx, y, cz + dz);
                    if (!level.isLoaded(pos)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || !state.getFluidState().isEmpty()
                            || UNBREAKABLE.contains(state.getBlock())) {
                        continue;
                    }
                    level.destroyBlock(pos, false, this);
                    budget--;
                }
            }
        }
    }

    /** @return true when the body cannot advance because built blocks are in the way. */
    private boolean blockedAhead(ServerLevel level, LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 1.0e-4) {
            return false;
        }
        double ux = dx / length;
        double uz = dz / length;
        int feet = Mth.floor(this.getY());
        int ahead = Math.max(3, Mth.ceil(this.getBbWidth() * 0.6));
        int layers = Math.min(10, Math.max(2, Mth.ceil(this.getBbHeight() / 6.0)));
        for (int step = 2; step <= ahead; step++) {
            int cx = Mth.floor(this.getX() + ux * step);
            int cz = Mth.floor(this.getZ() + uz * step);
            for (int layer = 0; layer < layers; layer++) {
                BlockPos pos = new BlockPos(cx, feet + 1 + layer * 3, cz);
                if (!level.isLoaded(pos)) {
                    continue;
                }
                BlockState state = level.getBlockState(pos);
                if (!state.isAir() && state.getFluidState().isEmpty()
                        && !UNBREAKABLE.contains(state.getBlock())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The presence stops at the obstruction and smashes it: a short wind-up ring on the ground,
     * then the blocks in front of the body are destroyed. This is an attack, not a passive aura —
     * the body never walks through a house without breaking it first.
     */
    private void smashAhead(ServerLevel level, LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double length = Math.max(1.0e-4, Math.sqrt(dx * dx + dz * dz));
        double centreX = this.getX() + dx / length * this.getBbWidth() * 0.5;
        double centreZ = this.getZ() + dz / length * this.getBbWidth() * 0.5;
        int feet = Mth.floor(this.getY());
        int radius = Math.max(2, Mth.ceil(this.getBbWidth() * 0.45));
        int top = feet + Math.min(40, Mth.ceil(this.getBbHeight() * 0.5));
        BossAbilities.ring(this, level, new Vec3(centreX, feet + 0.2, centreZ), radius + 1,
                net.minecraft.core.particles.ParticleTypes.CLOUD, 28);
        int budget = 220;
        for (int y = feet + 1; y <= top && budget > 0; y++) {
            for (int dxs = -radius; dxs <= radius && budget > 0; dxs++) {
                for (int dzs = -radius; dzs <= radius && budget > 0; dzs++) {
                    if (dxs * dxs + dzs * dzs > radius * radius) {
                        continue;
                    }
                    BlockPos pos = new BlockPos(Mth.floor(centreX) + dxs, y,
                            Mth.floor(centreZ) + dzs);
                    if (!level.isLoaded(pos)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || !state.getFluidState().isEmpty()
                            || UNBREAKABLE.contains(state.getBlock())) {
                        continue;
                    }
                    level.destroyBlock(pos, false, this);
                    budget--;
                }
            }
        }
        level.playSound(null, centreX, feet, centreZ,
                net.minecraft.sounds.SoundEvents.WITHER_BREAK_BLOCK,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.6F, 0.55F);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                centreX, feet + 2.0, centreZ, 6, radius * 0.4, 1.0, radius * 0.4, 0.0);
    }

    private void snapToGround() {
        // Water and lava presences swim: vanilla buoyancy owns the vertical axis there.
        if (this.isInWater() || this.isInLava()) {
            return;
        }
        int x = Mth.floor(this.getX());
        int z = Mth.floor(this.getZ());
        int baseY = Mth.floor(this.getY());
        int step = Mth.ceil(this.maxUpStep());
        int floorY = Integer.MIN_VALUE;
        for (int dy = step; dy >= -8; dy--) {
            BlockPos pos = new BlockPos(x, baseY + dy, z);
            if (!this.level().isLoaded(pos)) {
                break;
            }
            BlockState state = this.level().getBlockState(pos);
            if (!state.getCollisionShape(this.level(), pos).isEmpty()) {
                floorY = pos.getY() + 1;
                break;
            }
        }
        if (floorY != Integer.MIN_VALUE && Math.abs(floorY - this.getY()) <= Math.max(step, 2)) {
            this.setPos(this.getX(), floorY, this.getZ());
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.0, 1.0));
            this.setOnGround(true);
            this.resetFallDistance();
        }
        // Absolute safety net: a body must never fall out of the bottom of the world.
        if (this.getY() < this.level().getMinY() + 1) {
            this.setPos(this.getX(), this.level().getMinY() + 1, this.getZ());
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    /**
     * An Ancient One is effectively immovable: a small mob cannot shove a building-sized body, and
     * the body must never nudge a player out of the world. Player pushes are skipped entirely; the
     * boss still displaces ordinary mobs so a crowd does not stand inside its feet.
     */
    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
        if (!(entity instanceof Player)) {
            super.doPush(entity);
        }
    }

    /** A player leaning on a building-sized body must never be displaced by it. */
    @Override
    public void push(Entity entity) {
        if (!(entity instanceof Player)) {
            super.push(entity);
        }
    }

    /** No riders: there is no safe seat on a 64-128 block body, and no suffocation path. */
    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return false;
    }

    /** Melee reach is half the body height, measured from the centre; the hitbox still starts at the feet. */
    @Override
    protected AABB getAttackBoundingBox(double horizontalExpansion) {
        double expansion = Math.max(horizontalExpansion,
                this.meleeReach() - this.getBbWidth() * 0.5);
        return super.getAttackBoundingBox(expansion);
    }

    @Override
    public void tick() {
        super.tick();
        // Solve maintenance must run even while NoAI is set (which skips customServerAiStep), so it
        // lives here, server-side. A solved presence keeps its state until it lapses or is reloaded.
        if (!this.level().isClientSide()) {
            ServerLevel level = (ServerLevel) this.level();
            this.tickSolve(level);
            this.combat.tickMaintenance(level);
        }
    }

    /**
     * The interrupt loop: an in-progress wind-up is cancelled by enough player damage, and the
     * stagger sets a short +25% vulnerability window. Applied before {@code super} so the extra
     * damage is what the boss actually loses.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        float adjusted = damage * this.combat.damageTakenMultiplier();
        boolean hurt = super.hurtServer(level, source, adjusted);
        if (hurt) {
            this.combat.onDamaged(level, source, adjusted);
        }
        return hurt;
    }

    /**
     * Phase for the current health fraction. Subclasses override to read the world (sanity, taint)
     * or to collapse to fewer bands. Default is the shared two-threshold ladder.
     */
    public Phase phaseFor(ServerLevel level, float healthFraction) {
        if (healthFraction <= ModConfig.BOSS_ENRAGE_HEALTH.get()) {
            return Phase.ENRAGED;
        }
        if (healthFraction <= ModConfig.BOSS_PHASE_TWO_HEALTH.get()) {
            return Phase.STIRRING;
        }
        return Phase.DORMANT;
    }

    private void updatePhase(ServerLevel level) {
        Phase next = this.phaseFor(level, this.getHealth() / this.getMaxHealth());
        if (next != this.phase) {
            Phase previous = this.phase;
            this.phase = next;
            this.onPhaseChanged(level, previous, next);
        }
    }

    /** Called when the band changes; default is inert. The one place a subclass adds enrage work. */
    protected void onPhaseChanged(ServerLevel level, Phase previous, Phase now) {
    }

    /** Current band. */
    public final Phase phase() {
        return this.phase;
    }

    // --- Non-combat solve (design/28: "always a non-combat solve") ------------------------------

    /**
     * Solves this presence without a kill. Called by {@code rite/RiteEngine} on the nearest
     * compatible Ancient One within the rite's small radius (loaded-only). Idempotent per kind:
     * the terminal solves ({@code SOOTHED}/{@code BANISHED}) reject an already-solved presence, and
     * {@link Solve#STILLED} may be refreshed but only reports a <b>new</b> solve (so a rite cannot
     * be farmed for its reward while the quiet holds).
     *
     * @param kind   the solve to apply
     * @param ticks  duration for {@link Solve#STILLED} (ignored by the others)
     * @return whether a <b>new</b> solve was applied
     */
    public final boolean solve(Solve kind, int ticks) {
        if (!this.isAlive() || kind == Solve.NONE) {
            return false;
        }
        if (kind == Solve.STILLED) {
            if (this.solve == Solve.SOOTHED || this.solve == Solve.BANISHED) {
                return false; // never downgrade a terminal solve back to a temporary one
            }
            // Refresh allowed: standing the woods down again simply extends the quiet. The reward
            // path only fires for a NEW stilling, so the rite cannot be farmed while it holds.
            boolean fresh = !this.isSolved();
            this.solve = Solve.STILLED;
            this.solveUntil = this.level().getGameTime() + ticks;
            this.onSolved(kind);
            return fresh;
        }
        if (this.solve != Solve.NONE) {
            return false;
        }
        this.solve = kind;
        this.solveUntil = 0L;
        this.onSolved(kind);
        return true;
    }

    /** Whether the presence is currently quieted by a solve (and {@code STILLED} has not lapsed). */
    public final boolean isSolved() {
        if (this.solve == Solve.STILLED) {
            return this.level().getGameTime() < this.solveUntil;
        }
        return this.solve != Solve.NONE;
    }

    /** @return how this presence was solved (or {@link Solve#NONE}). */
    public final Solve solveKind() {
        return this.solve;
    }

    /** The one place a subclass adds solve-specific world work; default is inert. */
    protected void onSolved(Solve kind) {
    }

    /**
     * Applies the solve each server tick: a solved presence drops its target, never acquires a new
     * one, and does not move under its own AI. A {@link Solve#BANISHED} presence is additionally
     * drawn a short, bounded step away from the performer's settlement each second (the "draw it
     * away" fantasy) until it is far enough, then left be. Everything here is server-side.
     */
    private void tickSolve(ServerLevel level) {
        if (this.solve == Solve.STILLED && level.getGameTime() >= this.solveUntil) {
            this.solve = Solve.NONE;
            this.setNoAi(false); // the woods breathe again
        }
        if (this.solve == Solve.NONE) {
            return;
        }
        if (this.getTarget() != null) {
            this.setTarget(null);
        }
        this.setNoAi(true);
        if (this.solve == Solve.BANISHED && level.getGameTime() % 20L == 0L) {
            this.retreat(level);
        }
    }

    /**
     * One deterministic, bounded retreat step for a banished presence: horizontal-only, away from
     * the nearest player (the settlement being eaten), and it stops once no player is within twice
     * the configured draw distance (it has been drawn off) or the next step would leave a loaded
     * chunk. Chunks are never force-loaded.
     */
    private void retreat(ServerLevel level) {
        double draw = ModConfig.DUNWICH_RETREAT_DISTANCE.get();
        Player nearest = level.getNearestPlayer(this, draw * 2.0);
        if (nearest == null) {
            return; // already drawn far enough; leave it be
        }
        double dx = this.getX() - nearest.getX();
        double dz = this.getZ() - nearest.getZ();
        Direction away = Math.abs(dx) >= Math.abs(dz)
                ? (dx >= 0 ? Direction.EAST : Direction.WEST)
                : (dz >= 0 ? Direction.SOUTH : Direction.NORTH);
        double step = draw / 16.0;
        double nx = this.getX() + away.getStepX() * step;
        double nz = this.getZ() + away.getStepZ() * step;
        if (!level.hasChunkAt(Mth.floor(nx), Mth.floor(nz))) {
            return; // never load/generate a chunk just to retreat
        }
        this.getNavigation().stop();
        this.teleportTo(nx, this.getY(), nz);
    }

    // --- Presence / persistence -----------------------------------------------------------------

    /**
     * Persistent, so a boss never quietly despawns at a chunk border and robs the site of the
     * encounter the design wants it to own. {@code removeWhenFarAway} is also forced false.
     */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.setPersistenceRequired();
        this.applyHealthMultiplier();
        return data;
    }

    /**
     * Applies the global boss-difficulty knob once, at spawn, by scaling the saved max-health base
     * value (so it survives a reload; {@code finalizeSpawn} is not re-run on load). Default 1.0 is
     * a no-op.
     */
    private void applyHealthMultiplier() {
        double multiplier = ModConfig.BOSS_HEALTH_MULTIPLIER.get();
        AttributeInstance maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null && multiplier != 1.0) {
            maxHealth.setBaseValue(maxHealth.getBaseValue() * multiplier);
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString(TAG_PHASE, this.phase.name());
        output.putString(TAG_SOLVE, this.solve.name());
        output.putLong(TAG_SOLVE_UNTIL, this.solveUntil);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String stored = input.getStringOr(TAG_PHASE, Phase.DORMANT.name());
        this.phase = parsePhase(stored);
        this.solve = parseSolve(input.getStringOr(TAG_SOLVE, Solve.NONE.name()));
        this.solveUntil = input.getLongOr(TAG_SOLVE_UNTIL, 0L);
    }

    private static Phase parsePhase(String name) {
        for (Phase value : Phase.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return Phase.DORMANT;
    }

    private static Solve parseSolve(String name) {
        for (Solve value : Solve.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return Solve.NONE;
    }

    // --- Shared melee goals ---------------------------------------------------------------------

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // --- DreadAura defaults ---------------------------------------------------------------------

    /** An Ancient One is singular: a presence never stacks with itself. */
    @Override
    public int auraMaxStack() {
        return 1;
    }
}
