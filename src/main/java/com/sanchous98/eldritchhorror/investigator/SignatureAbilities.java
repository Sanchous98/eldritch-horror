package com.sanchous98.eldritchhorror.investigator;

import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.corruption.TaintAPI;
import com.sanchous98.eldritchhorror.entity.AncientOne;
import com.sanchous98.eldritchhorror.event.Rifts;
import com.sanchous98.eldritchhorror.registry.ModEffects;
import com.sanchous98.eldritchhorror.rite.RiteDefinition;
import com.sanchous98.eldritchhorror.rite.RiteKnowledge;
import com.sanchous98.eldritchhorror.rite.Rites;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Locations;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

/**
 * The 12 investigator <b>active</b> abilities (see {@code docs/INVESTIGATORS-CONTRACT.md}). One
 * shared signature item dispatches here by the owner's {@link Investigator}, so adding an
 * investigator needs no new behaviour class beyond one branch in {@link #activate}.
 *
 * <p>Everything here is <b>server-side</b>, bounded, and loaded-chunks-only:
 * <ul>
 *   <li>world lookups use {@link Rifts#near} (which never force-loads a chunk) and
 *       {@code ServerLevel.getEntitiesOfClass} (loaded entities only);</li>
 *   <li>sanity/corruption/taint/rites go through the public
 *       {@code SanityAPI}/{@code CorruptionAPI}/{@code TaintAPI}/{@code RiteKnowledge} facades, so
 *       content never touches an attachment directly;</li>
 *   <li>timed effects are plain vanilla {@link MobEffectInstance}s. The item applies the cooldown;
 *       this method applies only the effects and a short message.</li>
 * </ul>
 */
public final class SignatureAbilities {

    /** Chunk radius searched for the nearest rift ({@code Read the Signs}, {@code Stake Out}). */
    private static final int RIFT_CHUNK_RADIUS = 4;
    /** Cap on rift anchors collected per scan, so a rift field cannot blow up the list. */
    private static final int RIFT_MAX = 64;

    /** Radius searched for the nearest Ancient One ({@code Commune}); loaded entities only. */
    private static final double ANCIENT_ONE_RADIUS = 48.0;

    /** Per-chunk taint cleansed by {@code Benediction} across its 3x3 patch. */
    private static final double BENEDICTION_TAINT = 0.25;

    private SignatureAbilities() {
    }

    /**
     * Applies {@code id}'s active ability for {@code player}. Called from the signature item; the
     * item owns the cooldown, this owns the effect. Never throws for a missing world query — an
     * empty result just produces a "none nearby" message.
     */
    public static void activate(ServerPlayer player, Investigator id) {
        ServerLevel level = player.level();
        switch (id) {
            case ELEANOR_VANCE -> readTheSigns(player, level);
            case JACK_CORRIGAN -> stakeOut(player, level);
            case TOM_MALLORY -> {
                effect(player, MobEffects.RESISTANCE, 240);
                effect(player, MobEffects.REGENERATION, 240);
                message(player, id, "Hold the Line: you brace for 12 seconds.");
            }
            case CORMAC_BLACKWOOD -> {
                effect(player, MobEffects.INVISIBILITY, 120);
                effect(player, MobEffects.SPEED, 120);
                message(player, id, "Dirty Trick: you slip away for 6 seconds.");
            }
            case SISTER_AGATHA -> benediction(player, level);
            case MARION_DELACROIX -> commune(player, level);
            case VERA_NIGHTINGALE -> {
                SanityAPI.add(player, -15.0);
                CorruptionAPI.add(player, 10.0);
                message(player, id, "Blood Offering: sanity spent, corruption gained.");
            }
            case NIKOLAI_VOLKOV -> forbiddenInsight(player);
            case DR_AMOS_HARTLEY -> {
                player.removeEffect(ModEffects.MADNESS);
                player.removeEffect(ModEffects.MARKED);
                SanityAPI.add(player, 20.0);
                message(player, id, "Sedate: the madness recedes; sanity restored.");
            }
            case EVELYN_ASHCOMBE -> {
                effect(player, MobEffects.RESISTANCE, 400);
                message(player, id, "Buy Time: resistance for 20 seconds.");
            }
            case ALDOUS_PEMBERTON -> survey(player);
            case HAZEL_QUINN -> {
                CorruptionAPI.add(player, -5.0);
                effect(player, MobEffects.SPEED, 160);
                message(player, id, "Expos\u00e9: your corruption eases and you move quickly.");
            }
        }
    }

    /** Eleanor Vance — reveal the nearest rift direction/block distance; Night Vision 15s. */
    private static void readTheSigns(ServerPlayer player, ServerLevel level) {
        BlockPos nearest = nearestRift(player, level);
        if (nearest == null) {
            message(player, Investigator.ELEANOR_VANCE, "Read the Signs: no rifts close by.");
        } else {
            message(player, Investigator.ELEANOR_VANCE,
                    "Read the Signs: nearest rift " + bearing(player, nearest) + ".");
        }
        effect(player, MobEffects.NIGHT_VISION, 300);
    }

    /** Jack Corrigan — mark the nearest rift direction; Resistance 8s. */
    private static void stakeOut(ServerPlayer player, ServerLevel level) {
        BlockPos nearest = nearestRift(player, level);
        if (nearest == null) {
            message(player, Investigator.JACK_CORRIGAN, "Stake Out: no rifts close by.");
        } else {
            message(player, Investigator.JACK_CORRIGAN,
                    "Stake Out: nearest rift " + bearing(player, nearest) + ".");
        }
        effect(player, MobEffects.RESISTANCE, 160);
    }

    /** Sister Agatha — cleanse the 3x3-chunk taint patch around the player; sanity +5. */
    private static void benediction(ServerPlayer player, ServerLevel level) {
        ChunkPos center = player.chunkPosition();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                TaintAPI.add(level, new ChunkPos(center.x() + dx, center.z() + dz),
                        -BENEDICTION_TAINT);
            }
        }
        SanityAPI.add(player, 5.0);
        message(player, Investigator.SISTER_AGATHA,
                "Benediction: the local taint is cleansed; sanity restored.");
    }

    /** Marion Delacroix — sense the nearest loaded Ancient One direction/distance. */
    private static void commune(ServerPlayer player, ServerLevel level) {
        AABB bounds = player.getBoundingBox().inflate(ANCIENT_ONE_RADIUS);
        List<AncientOne> presences = level.getEntitiesOfClass(AncientOne.class, bounds);
        AncientOne nearest = null;
        double best = Double.MAX_VALUE;
        for (AncientOne presence : presences) {
            double d = presence.distanceToSqr(player);
            if (d < best) {
                best = d;
                nearest = presence;
            }
        }
        if (nearest == null) {
            message(player, Investigator.MARION_DELACROIX, "Commune: no presence answers.");
            return;
        }
        message(player, Investigator.MARION_DELACROIX,
                "Commune: a presence " + bearing(player, nearest.blockPosition()) + ".");
    }

    /** Nikolai Volkov — teach one random rite the player does not already know. */
    private static void forbiddenInsight(ServerPlayer player) {
        List<RiteDefinition> unknown = Rites.all().stream()
                .filter(rite -> !RiteKnowledge.knows(player, rite.id()))
                .toList();
        if (unknown.isEmpty()) {
            message(player, Investigator.NIKOLAI_VOLKOV,
                    "Forbidden Insight: you already know every rite.");
            return;
        }
        RandomSource random = player.getRandom();
        RiteDefinition chosen = unknown.get(random.nextInt(unknown.size()));
        RiteKnowledge.add(player, chosen.id());
        message(player, Investigator.NIKOLAI_VOLKOV,
                "Forbidden Insight: you learn " + chosen.name().getString() + ".");
    }

    /** Aldous Pemberton — reveal the nearest registered site/city direction/distance. */
    private static void survey(ServerPlayer player) {
        Location nearest = null;
        double best = Double.MAX_VALUE;
        for (Location loc : Locations.all()) {
            double dx = Locations.xOf(loc) - player.getX();
            double dz = Locations.zOf(loc) - player.getZ();
            double d = dx * dx + dz * dz;
            if (d < best) {
                best = d;
                nearest = loc;
            }
        }
        if (nearest == null) {
            message(player, Investigator.ALDOUS_PEMBERTON, "Survey: no known places to report.");
            return;
        }
        BlockPos site = new BlockPos(Locations.xOf(nearest), player.getBlockY(), Locations.zOf(nearest));
        message(player, Investigator.ALDOUS_PEMBERTON,
                "Survey: nearest place " + bearing(player, site) + ".");
    }

    /** The nearest rift anchor within the scan radius, or {@code null} when none is loaded. */
    private static BlockPos nearestRift(ServerPlayer player, ServerLevel level) {
        List<BlockPos> rifts = Rifts.near(level, player.blockPosition(), RIFT_CHUNK_RADIUS, RIFT_MAX);
        BlockPos nearest = null;
        double best = Double.MAX_VALUE;
        for (BlockPos rift : rifts) {
            double d = player.distanceToSqr(rift.getX() + 0.5, rift.getY() + 0.5, rift.getZ() + 0.5);
            if (d < best) {
                best = d;
                nearest = rift;
            }
        }
        return nearest;
    }

    /** {@code "<direction>, N blocks"} from the player to {@code target}. */
    private static String bearing(ServerPlayer player, BlockPos target) {
        int dx = target.getX() - player.getBlockX();
        int dz = target.getZ() - player.getBlockZ();
        Direction direction = Direction.getNearest(dx, 0, dz, Direction.NORTH);
        int distance = (int) Math.round(Math.sqrt(player.distanceToSqr(
                target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5)));
        return direction.getName() + ", " + distance + " blocks";
    }

    /** Applies one timed vanilla effect (ambient, invisible, no icon) at amplifier 0. */
    private static void effect(ServerPlayer player, Holder<MobEffect> effect, int ticks) {
        player.addEffect(new MobEffectInstance(effect, ticks, 0, true, false, false));
    }

    /**
     * Sends the ability's short system message, keyed per investigator so the main agent's lang can
     * localise it; the fallback keeps it readable without lang.
     */
    private static void message(ServerPlayer player, Investigator id, String fallback) {
        player.sendSystemMessage(Component.translatableWithFallback(
                "investigator.eldritch_horror.active." + id.id(), fallback));
    }
}
