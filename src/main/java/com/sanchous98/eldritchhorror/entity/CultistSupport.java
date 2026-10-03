package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.cult.CultDefinition;
import com.sanchous98.eldritchhorror.cult.CultRank;
import com.sanchous98.eldritchhorror.cult.CultSystem;
import com.sanchous98.eldritchhorror.cult.Cults;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jspecify.annotations.Nullable;

/**
 * Small shared helper for the cult faction ({@code design/25-bestiary-and-entities.md},
 * "Cultists" family; {@code design/09-cults.md}). It owns three things the five cult mobs share so
 * none of them re-implements them:
 *
 * <ul>
 *   <li>picking an owning cult id and a member rank band from the {@link Cults} registry
 *       ({@link #randomCult}, {@link #randomRank}) — the mob's identity;</li>
 *   <li>reading the <b>player's</b> reputation through the existing public
 *       {@link CultSystem} API only ({@link #hostileTo}) — no {@code cult/} interface changes;</li>
 *   <li>applying the config-gated rank scaling the zealot uses ({@link #applyRankScaling}).</li>
 * </ul>
 *
 * <p>Everything here is deterministic-friendly ({@link RandomSource} passed in) and
 * server-side only. The cult mobs deliberately do <b>not</b> live on this class's own synced data:
 * a NeoForge entity accessor must be declared in the entity that holds it, so each mob stores its
 * own cult id/rank in a {@link CultIdentity} field.
 */
public final class CultistSupport {

    private CultistSupport() {
    }

    /** First registered cult id, used as the default when none has been assigned yet. */
    public static String defaultCult() {
        List<CultDefinition> all = Cults.all();
        return all.isEmpty() ? "drowned_choir" : all.getFirst().id();
    }

    /** A cult id drawn from the registry, so every NPC belongs to a real faction. */
    public static String randomCult(RandomSource random) {
        List<CultDefinition> all = Cults.all();
        return all.isEmpty() ? "drowned_choir" : all.get(random.nextInt(all.size())).id();
    }

    /**
     * A member rank band, weighted low so most cultists are rank-and-file and the Inner Circle is
     * rare. {@code OUTSIDER} is never used: that band is the player's <em>hostile</em> standing, not
     * a cult membership rank.
     */
    public static CultRank randomRank(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 40) {
            return CultRank.NEUTRAL;
        }
        if (roll < 70) {
            return CultRank.INITIATE;
        }
        if (roll < 88) {
            return CultRank.MEMBER;
        }
        if (roll < 97) {
            return CultRank.DEVOTED;
        }
        return CultRank.INNER_CIRCLE;
    }

    /**
     * Whether {@code player} is hostile to {@code cultId} by reputation: true when their standing is
     * below {@code threshold} (the config's {@code cultHostileBelow}, default {@code 0} — i.e. below
     * the {@code OUTSIDER} band). Unknown cults are never hostile.
     */
    public static boolean hostileTo(ServerPlayer player, @Nullable String cultId, int threshold) {
        return cultId != null && Cults.byId(cultId) != null
                && CultSystem.get(player, cultId) < threshold;
    }

    /**
     * Applies the config-gated rank scaling: {@code MAX_HEALTH} and {@code ATTACK_DAMAGE} are
     * multiplied by {@code 1 + rank.ordinal() × perBand} and the mob is healed to its new maximum.
     * A no-op when disabled or the attribute is absent; safe to call from {@code finalizeSpawn}
     * (the values are saved with the mob's attributes, so a reload keeps them).
     */
    public static void applyRankScaling(LivingEntity mob, CultRank rank, boolean enabled, double perBand) {
        if (!enabled) {
            return;
        }
        double multiplier = 1.0 + rank.ordinal() * perBand;
        scale(mob, Attributes.MAX_HEALTH, multiplier);
        scale(mob, Attributes.ATTACK_DAMAGE, multiplier);
        mob.setHealth(mob.getMaxHealth());
    }

    private static void scale(LivingEntity mob, Holder<Attribute> attribute, double multiplier) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(instance.getBaseValue() * multiplier);
        }
    }
}
