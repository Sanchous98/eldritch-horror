package com.sanchous98.eldritchhorror.entity.combat;

import com.sanchous98.eldritchhorror.entity.AncientOne;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Small, shared building blocks for the boss ability layer: particle telegraphs, cones, pulls,
 * knockbacks, teleports and the single damage path. Everything is server-side and uses the boss as
 * the damage cause, so armour, wards and death messages behave as they do for a melee swing.
 */
public final class BossAbilities {

    /** Half-angle of a "cone" attack: 60° each side of the boss's facing. */
    public static final double CONE_HALF_ANGLE = 60.0;

    private BossAbilities() {
    }

    /** A small puff at the boss, used by the default telegraph. */
    public static void telegraphBurst(ServerLevel level, AncientOne boss, ParticleOptions particle, int count) {
        level.sendParticles(particle, boss.getX(), boss.getEyeY(), boss.getZ(), count,
                1.1, 0.8, 1.1, 0.01);
    }

    /** A puff at the target, for abilities that visibly gather over the player. */
    public static void telegraphAt(ServerLevel level, Player target, ParticleOptions particle,
                                   int count, double spread) {
        level.sendParticles(particle, target.getX(), target.getY() + 1.0, target.getZ(), count,
                spread, 0.5, spread, 0.01);
    }

    /** A flat ring of particles, for area attacks that need a readable footprint. */
    public static void ring(ServerLevel level, Vec3 centre, double radius, ParticleOptions particle,
                            int points) {
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2.0 * i / points;
            level.sendParticles(particle,
                    centre.x + Math.cos(angle) * radius,
                    centre.y,
                    centre.z + Math.sin(angle) * radius,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /** A line of particles from the boss to the target, used by ranged attacks. */
    public static void line(ServerLevel level, Vec3 from, Vec3 to, ParticleOptions particle, int points) {
        for (int i = 0; i <= points; i++) {
            Vec3 point = from.lerp(to, i / (double) points);
            level.sendParticles(particle, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /** Living, non-creative players in a sphere around {@code centre}. */
    public static List<Player> playersAround(ServerLevel level, Vec3 centre, double radius) {
        AABB box = new AABB(centre.x - radius, centre.y - radius, centre.z - radius,
                centre.x + radius, centre.y + radius, centre.z + radius);
        return level.getEntitiesOfClass(Player.class, box, BossAbilities::isValidTarget);
    }

    /** Whether this player can be a target of a boss ability at all. */
    public static boolean isValidTarget(Player player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    /**
     * Applies {@code action} to every valid player inside a horizontal cone in front of the boss.
     * The cone is measured from the boss's look direction and ignores height, so a player on a
     * small ledge still counts.
     */
    public static void cone(ServerLevel level, AncientOne boss, double range, double halfAngleDegrees,
                            Consumer<Player> action) {
        Vec3 facing = new Vec3(boss.getLookAngle().x, 0.0, boss.getLookAngle().z);
        if (facing.lengthSqr() < 1.0E-4) {
            facing = new Vec3(0.0, 0.0, 1.0);
        }
        Vec3 unit = facing.normalize();
        double threshold = Math.cos(Math.toRadians(halfAngleDegrees));
        for (Player player : playersAround(level, boss.position(), range)) {
            Vec3 to = new Vec3(player.getX() - boss.getX(), 0.0, player.getZ() - boss.getZ());
            if (to.lengthSqr() < 1.0E-4 || unit.dot(to.normalize()) >= threshold) {
                action.accept(player);
            }
        }
    }

    /** Damage from the boss with the normal mob-attack source (armour and wards apply). */
    public static void hit(AncientOne boss, ServerLevel level, Player target, float damage) {
        if (isValidTarget(target)) {
            target.hurtServer(level, level.damageSources().mobAttack(boss), damage);
        }
    }

    /** Soft area damage around a point, skipping the boss itself. */
    public static void damageAround(AncientOne boss, ServerLevel level, Vec3 centre, double radius,
                                    float damage) {
        for (Player player : playersAround(level, centre, radius)) {
            hit(boss, level, player, damage);
        }
    }

    /** Drags the target a short, readable distance toward the boss. */
    public static void pull(AncientOne boss, Player target, double strength, double lift) {
        Vec3 toward = new Vec3(boss.getX() - target.getX(), 0.0, boss.getZ() - target.getZ());
        if (toward.lengthSqr() < 1.0E-4) {
            return;
        }
        Vec3 impulse = toward.normalize().scale(strength);
        target.push(impulse.x, lift, impulse.z);
    }

    /** Throws the target away from the boss (cones, sweeps, shock waves). */
    public static void knockAway(AncientOne boss, Player target, double strength, double lift) {
        Vec3 away = new Vec3(target.getX() - boss.getX(), 0.0, target.getZ() - boss.getZ());
        if (away.lengthSqr() < 1.0E-4) {
            return;
        }
        Vec3 impulse = away.normalize().scale(strength);
        target.push(impulse.x, lift, impulse.z);
    }

    /** Freezes the target in place for combat purposes without applying vanilla ice damage. */
    public static void freeze(Player target, int ticks) {
        target.setTicksFrozen(Math.max(target.getTicksFrozen(), ticks));
    }

    /** Freezes every valid player in a sphere (the cold presences' area slow). */
    public static void freezeAround(ServerLevel level, Vec3 centre, double radius, int ticks) {
        for (Player player : playersAround(level, centre, radius)) {
            freeze(player, ticks);
        }
    }

    /** Steps the boss to a safe spot behind the target, falling back to its current spot if blocked. */
    public static void teleportBehind(AncientOne boss, ServerLevel level, Player target) {
        Vec3 look = target.getLookAngle();
        Vec3 behind = new Vec3(-look.x, 0.0, -look.z);
        if (behind.lengthSqr() < 1.0E-4) {
            behind = new Vec3(0.0, 0.0, 1.0);
        }
        double distance = Math.max(2.0, boss.getBbWidth() + 1.0);
        Vec3 spot = target.position().add(behind.normalize().scale(distance));
        BlockPos pos = BlockPos.containing(spot.x, target.getY(), spot.z);
        if (isStandable(level, pos, bossHeight(boss))) {
            boss.teleportTo(spot.x, target.getY(), spot.z);
        }
        boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }

    /** Steps the boss next to the target (gate, leap, mask), preferring the target's left side. */
    public static void teleportBeside(AncientOne boss, ServerLevel level, Player target) {
        Vec3 look = target.getLookAngle();
        Vec3 side = new Vec3(-look.z, 0.0, look.x);
        if (side.lengthSqr() < 1.0E-4) {
            side = new Vec3(1.0, 0.0, 0.0);
        }
        double distance = Math.max(2.5, boss.getBbWidth() + 1.5);
        Vec3 spot = target.position().add(side.normalize().scale(distance));
        BlockPos pos = BlockPos.containing(spot.x, target.getY(), spot.z);
        if (isStandable(level, pos, bossHeight(boss))) {
            boss.teleportTo(spot.x, target.getY(), spot.z);
        }
        boss.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }

    /**
     * Whether a {@code height}-block air column with a floor exists at {@code pos}. The height is
     * derived from the boss's hitbox so the model agent's larger Ancient Ones do not teleport into
     * a ceiling.
     */
    public static boolean isStandable(ServerLevel level, BlockPos pos, int height) {
        if (!level.isLoaded(pos) || !level.isLoaded(pos.above(height - 1))) {
            return false;
        }
        BlockState floor = level.getBlockState(pos.below());
        if (floor.canBeReplaced()) {
            return false;
        }
        for (int dy = 0; dy < height; dy++) {
            if (!level.getBlockState(pos.above(dy)).canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    /** The number of blocks a boss's hitbox needs, capped so a bug cannot scan the whole column. */
    public static int bossHeight(AncientOne boss) {
        return Math.min(12, Math.max(2, (int) Math.ceil(boss.getBbHeight())));
    }

    /** Applies a vanilla effect with ambient=false, visible particles, for readability. */
    public static void effect(Player target, Holder<MobEffect> effect, int ticks, int amplifier) {
        target.addEffect(new MobEffectInstance(effect, ticks, amplifier, false, true, true));
    }

    /** Applies one effect to every valid player in a sphere (area fear, darkness, poison). */
    public static void effectAround(ServerLevel level, Vec3 centre, double radius, Holder<MobEffect> effect,
                                    int ticks, int amplifier) {
        for (Player player : playersAround(level, centre, radius)) {
            effect(player, effect, ticks, amplifier);
        }
    }

    /** The shared "brine and dream" debuff pair, handy for drowned presences. */
    public static void brine(Player target, int ticks) {
        effect(target, MobEffects.SLOWNESS, ticks, 1);
        effect(target, MobEffects.NAUSEA, ticks + 40, 0);
    }

    /** Ignites a target for a short, readable time (no permanent fire). */
    public static void burn(Player target, float seconds) {
        target.igniteForSeconds(seconds);
    }

    /** Poison/withier style damage-over-time, used by the serpent and ichor presences. */
    public static void venom(Player target, int ticks, int amplifier) {
        effect(target, MobEffects.POISON, ticks, amplifier);
    }

    /** Convenience for the telegraphed area marker used by rings and novas. */
    public static void areaMarker(ServerLevel level, Vec3 centre, double radius) {
        ring(level, centre, radius, ParticleTypes.SMOKE, Math.max(8, (int) (radius * 6.0)));
    }
}
