package com.sanchous98.eldritchhorror.skill;

import com.sanchous98.eldritchhorror.registry.ModAttachments;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-authoritative skill tree access, layered over the synced {@link ModAttachments#SKILLS}
 * (unlocked ids) and {@link ModAttachments#SKILL_POINTS} attachments (design/13). Points are earned
 * by finishing quests; unlocking a node spends them and applies its effect.
 *
 * <p>Effects are read where their system runs (maximum sanity at the attribute, corruption gain in
 * {@code Progression}), so nothing here is a no-op.
 */
public final class SkillTree {

    /** Maximum-sanity bonus fraction from {@link Skills#LUCID_MIND}. */
    private static final double LUCID_MIND_SANITY = 0.10;
    /** Additional maximum-sanity bonus fraction from {@link Skills#IRON_WILL}. */
    private static final double IRON_WILL_SANITY = 0.15;
    /** Corruption-gain multiplier from {@link Skills#WARDED_SOUL}. */
    private static final double WARDED_SOUL_CORRUPTION = 0.90;
    /** Chance that {@link Skills#FRUGAL_RITES} saves a successful rite's offerings. */
    private static final double FRUGAL_RITES_REFUND = 0.25;

    private SkillTree() {
    }

    /** @return the player's unspent skill points. */
    public static int points(ServerPlayer player) {
        return player.getData(ModAttachments.SKILL_POINTS);
    }

    /** Adds {@code delta} skill points (never below zero). */
    public static void addPoints(ServerPlayer player, int delta) {
        int next = Math.max(0, points(player) + delta);
        player.setData(ModAttachments.SKILL_POINTS, next);
    }

    /** @return whether {@code player} has unlocked {@code skillId}. */
    public static boolean has(ServerPlayer player, String skillId) {
        return player.getData(ModAttachments.SKILLS).contains(skillId);
    }

    /**
     * Unlocks {@code skillId} for {@code player} if it is known, not already unlocked, and there are
     * enough points. Spends the cost and applies the effect (re-applying the max-sanity attribute).
     *
     * @return {@code true} if this call unlocked it
     */
    public static boolean unlock(ServerPlayer player, String skillId) {
        Skill skill = Skills.byId(skillId);
        if (skill == null || has(player, skillId) || points(player) < skill.cost()) {
            return false;
        }
        Set<String> current = player.getData(ModAttachments.SKILLS);
        Set<String> next = new HashSet<>(current);
        next.add(skillId);
        player.setData(ModAttachments.SKILLS, Set.copyOf(next));
        addPoints(player, -skill.cost());
        // Re-apply the max-sanity modifier so a skill that changes it takes effect immediately.
        com.sanchous98.eldritchhorror.investigator.InvestigatorAPI.reapply(player);
        return true;
    }

    /** @return the player's maximum-sanity bonus fraction from skills. */
    public static double maxSanityBonus(ServerPlayer player) {
        double bonus = has(player, Skills.LUCID_MIND) ? LUCID_MIND_SANITY : 0.0;
        if (has(player, Skills.IRON_WILL)) {
            bonus += IRON_WILL_SANITY;
        }
        return bonus;
    }

    /**
     * @return the chance (0..1) that a successful rite's offerings are spared, from
     *     {@link Skills#FRUGAL_RITES}; read by the rite engine at the point of consumption.
     */
    public static double offeringsRefundChance(ServerPlayer player) {
        return has(player, Skills.FRUGAL_RITES) ? FRUGAL_RITES_REFUND : 0.0;
    }

    /** @return the player's corruption-gain multiplier from skills ({@code 1.0} = none). */
    public static double corruptionGainMultiplier(ServerPlayer player) {
        return has(player, Skills.WARDED_SOUL) ? WARDED_SOUL_CORRUPTION : 1.0;
    }
}
