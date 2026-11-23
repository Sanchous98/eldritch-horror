package com.sanchous98.eldritchhorror.quest;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.advancement.Advancements;
import com.sanchous98.eldritchhorror.cult.CultDefinition;
import com.sanchous98.eldritchhorror.cult.CultSystem;
import com.sanchous98.eldritchhorror.cult.Cults;
import com.sanchous98.eldritchhorror.investigator.Investigator;
import com.sanchous98.eldritchhorror.investigator.InvestigatorAPI;
import com.sanchous98.eldritchhorror.investigator.Investigators;
import com.sanchous98.eldritchhorror.registry.ModAttachments;
import com.sanchous98.eldritchhorror.registry.items.SignatureItems;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Server-authoritative quest progress, layered over the synced {@link ModAttachments#QUESTS}
 * attachment (design/13). The value is an immutable snapshot, so writes are read-modify-write.
 *
 * <p>Progress is advanced by the systems a quest watches (a rite performed, a codex entry found, a
 * rift sealed, a presence soothed, a starting city taken); completion is announced in chat and pays
 * the quest's skill point plus its optional item gift, both defined by the loaded data.
 *
 * <p>A quest's {@code requires} is a hard gate here, not a display hint: a watched system may fire
 * for a player who has not earned the quest yet (learning a codex entry before the first rite), and
 * that call must change nothing. Refusals are silent because those triggers are frequent.
 *
 * <p>Repeatables ({@link Repeat}) pay again on each completion after their cooldown, measured from
 * the previous completion in {@link ModAttachments#QUEST_COMPLETIONS}. Their progress is reset to 0
 * when completed and stays frozen until the cooldown elapses, so a daily counter never sits full.
 */
public final class QuestJournal {

    private QuestJournal() {
    }

    /** @return the player's current progress toward {@code questId} ({@code 0} if untouched). */
    public static int progress(ServerPlayer player, String questId) {
        return player.getData(ModAttachments.QUESTS).getOrDefault(questId, 0);
    }

    /** @return whether {@code player} has finished {@code questId}. */
    public static boolean complete(ServerPlayer player, String questId) {
        Quest quest = Quests.byId(questId);
        return quest != null && progress(player, questId) >= quest.target();
    }

    /**
     * @return when {@code player} last completed {@code questId}, in epoch seconds, or
     *     {@link QuestRules#NEVER_COMPLETED} when it has never been completed.
     */
    public static long lastCompleted(ServerPlayer player, String questId) {
        Long stamp = player.getData(ModAttachments.QUEST_COMPLETIONS).get(questId);
        return stamp == null ? QuestRules.NEVER_COMPLETED : stamp;
    }

    /**
     * @return seconds left before {@code quest} can be completed again, or 0 when it is available
     *     now (always 0 for a once-only quest).
     */
    public static long cooldownSeconds(ServerPlayer player, Quest quest) {
        long period = quest.repeat().periodSeconds();
        if (period <= 0L) {
            return 0L;
        }
        return QuestRules.remainingSeconds(nowSeconds(), lastCompleted(player, quest.id()), period);
    }

    /**
     * Advances {@code questId} by {@code delta}, capped at its target, and announces the completion
     * once. Unknown ids are ignored, as is any quest whose prerequisite is unmet or whose repeatable
     * cooldown has not elapsed; the refusal is silent.
     *
     * <p>A completed once-only quest is left alone. A repeatable whose cooldown has elapsed starts a
     * fresh cycle from 0 and pays its rewards again; while cooling down it cannot advance at all.
     *
     * @return whether this call finished the quest
     */
    public static boolean add(ServerPlayer player, String questId, int delta) {
        Quest quest = Quests.byId(questId);
        if (quest == null || delta <= 0) {
            return false;
        }
        // Investigator chains only advance for their owner: everyone sees the journal, but another
        // investigator's field-kit tasks are not theirs to earn.
        Investigator chosen = InvestigatorAPI.get(player);
        if (!quest.forInvestigator(chosen == null ? "" : chosen.id())) {
            return false;
        }
        // Hard prerequisite gate: an id missing from the loaded data is unmet, so a dangling
        // `requires` can never quietly open the quest.
        if (!QuestRules.prerequisiteMet(quest.requires(), prerequisite -> complete(player, prerequisite))) {
            return false;
        }
        int current = progress(player, questId);
        boolean done = current >= quest.target();
        if (done && !quest.repeat().repeatable()) {
            return false; // once-only and already done
        }
        long now = nowSeconds();
        long period = quest.repeat().periodSeconds();
        if (!QuestRules.isAvailable(now, lastCompleted(player, questId), period)) {
            return false; // repeatable, still cooling down: progress stays frozen
        }
        if (done) {
            current = 0; // repeatable cooldown elapsed: a new cycle starts from zero
        }
        int next = Math.min(quest.target(), current + delta);
        Map<String, Integer> map = new HashMap<>(player.getData(ModAttachments.QUESTS));
        map.put(questId, next);
        player.setData(ModAttachments.QUESTS, Map.copyOf(map));
        if (next >= quest.target()) {
            player.sendSystemMessage(Component.translatable(
                    "quest.eldritch_horror.complete", quest.name()));
            // Finishing a quest earns a skill point and, when the data says so, an item gift.
            com.sanchous98.eldritchhorror.skill.SkillTree.addPoints(player, 1);
            grantReward(player, quest);
            // The journal stays authoritative; the advancement tab is a read-only guide mirroring it.
            Advancements.awardQuest(player, quest.id());
            if (quest.repeat().repeatable()) {
                restartCycle(player, quest, now);
            }
            return true;
        }
        return false;
    }

    /**
     * Closes a repeatable's cycle: progress back to 0 and the completion stamped, so the reward can
     * be earned again only after a full cooldown and the counter never stays full.
     */
    private static void restartCycle(ServerPlayer player, Quest quest, long now) {
        Map<String, Integer> progress = new HashMap<>(player.getData(ModAttachments.QUESTS));
        progress.put(quest.id(), 0);
        player.setData(ModAttachments.QUESTS, Map.copyOf(progress));
        Map<String, Long> completions = new HashMap<>(player.getData(ModAttachments.QUEST_COMPLETIONS));
        completions.put(quest.id(), now);
        player.setData(ModAttachments.QUEST_COMPLETIONS, Map.copyOf(completions));
    }

    /** @return the current wall-clock time in seconds, the unit of the cooldown fields. */
    private static long nowSeconds() {
        return System.currentTimeMillis() / 1000L;
    }

    /**
     * Pays an investigator's field kit after a starter task. The kit comes from the single
     * {@link Investigators} table, so a quest cannot drift from the roster: {@code kit} pays every
     * piece except the signature item, {@code signature} pays only the signature item.
     */
    private static void grantFieldKit(ServerPlayer player, String investigatorId, boolean signature) {
        Investigator owner = Investigator.byId(investigatorId);
        if (owner == null) {
            return;
        }
        String signatureId = SignatureItems.signatureId(owner);
        int paid = 0;
        for (ItemStack stack : Investigators.starterKit(owner)) {
            if (stack.isEmpty()) {
                continue;
            }
            Identifier key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (key == null) {
                continue;
            }
            boolean isSignature = key.getPath().equals(signatureId);
            if (signature ? !isSignature : isSignature) {
                continue;
            }
            ItemStack copy = stack.copy();
            if (!player.getInventory().add(copy)) {
                player.drop(copy, false, Prediction.SERVER_ONLY);
            }
            paid += copy.getCount();
        }
        if (paid > 0) {
            player.sendSystemMessage(Component.translatable(signature
                    ? "quest.eldritch_horror.reward.signature"
                    : "quest.eldritch_horror.reward.kit", owner.displayName()));
        }
    }

    /**
     * Hands over a quest's completion gifts, each only if its data defines one: the item (currency
     * that makes the cult trades, and so the ritual reagents, reachable by play) and the reputation
     * (design/03c: "complete a cult quest" is a gain source; the opposed pairs still cost).
     */
    private static void grantReward(ServerPlayer player, Quest quest) {
        if (!quest.rewardKit().isEmpty() || quest.rewardSignature()) {
            String owner = quest.rewardKit().isEmpty() ? quest.investigator() : quest.rewardKit();
            grantFieldKit(player, owner, quest.rewardSignature());
        }
        if (quest.paysItem()) {
            String path = quest.rewardItem();
            Identifier id = path.contains(":")
                    ? Identifier.tryParse(path)
                    : EldritchHorror.id(path);
            Item item = id == null ? null : BuiltInRegistries.ITEM.getValue(id);
            if (item != null && item != Items.AIR) {
                ItemStack stack = new ItemStack(item, quest.rewardCount());
                // Inventory#add consumes the passed stack, so keep a copy for the message.
                ItemStack granted = stack.copy();
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false, Prediction.SERVER_ONLY);
                }
                player.sendSystemMessage(Component.translatable(
                        "quest.eldritch_horror.reward", granted.getCount(), granted.getHoverName()));
            }
        }
        if (quest.paysReputation()) {
            CultDefinition cult = Cults.byId(quest.rewardRepCult());
            int before = CultSystem.get(player, quest.rewardRepCult());
            int after = CultSystem.add(player, quest.rewardRepCult(), quest.rewardRepAmount());
            player.sendSystemMessage(Component.translatable("quest.eldritch_horror.reward.rep",
                    after - before, cult == null ? quest.rewardRepCult() : cult.name(), after));
        }
    }
}
