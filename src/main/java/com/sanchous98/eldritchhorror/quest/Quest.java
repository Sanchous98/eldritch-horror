package com.sanchous98.eldritchhorror.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

/**
 * One journal quest, loaded from {@code data/<namespace>/quest/<file>.json} (design/13). Stable id,
 * the count needed to finish it, a coarse act/ordering stage, the id of the quest it follows (a soft
 * prerequisite used for the "next objective" hint) and an optional item + reputation gift paid on
 * completion.
 *
 * <p>Localised name/description stay as lang keys ({@code quest.eldritch_horror.<id>} and
 * {@code ….desc}), so the data file holds numbers and ids, not prose — a translator's file stays the
 * single place for words.
 *
 * <p>Three optional additions carry the investigator field-kit chains (design/14):
 * {@code investigator} restricts the quest to one investigator (empty = everyone),
 * {@code trigger}/{@code param} let the generic event watchers in {@link QuestTriggers} advance it
 * (empty trigger = the code advances it by id, as the main chain does), and
 * {@code reward_kit}/{@code reward_signature} pay the owner's starting kit (minus the signature) and
 * the signature item itself, read from the one data table in {@code Investigators}.
 *
 * @param id              stable snake_case id (also the localisation suffix)
 * @param target          the count at which the quest is complete
 * @param stage           act/ordering bucket: the journal lists by stage, then by id
 * @param requires        quest id that should be finished first ({@code ""} for none)
 * @param rewardItem      item path paid on completion ({@code ""} for none); namespaced ids are honoured
 * @param rewardCount     how many of {@link #rewardItem()} are paid
 * @param rewardRepCult   cult id that gains standing on completion ({@code ""} for none)
 * @param rewardRepAmount how much standing {@link #rewardRepCult()} gains
 * @param repeat          how often the quest reopens ({@link Repeat#ONCE} when the field is absent)
 * @param investigator    investigator id this quest belongs to ({@code ""} for everyone)
 * @param trigger         watched event type ({@code ""} when the code advances it explicitly)
 * @param param           id the event must carry (item, entity, rite; {@code ""} for any)
 * @param rewardKit       investigator whose starting kit (minus signature) is paid ({@code ""} none)
 * @param rewardSignature whether this quest pays its investigator's signature item
 */
public record Quest(String id, int target, int stage, String requires, String rewardItem,
                    int rewardCount, String rewardRepCult, int rewardRepAmount, Repeat repeat,
                    String investigator, String trigger, String param, String rewardKit,
                    boolean rewardSignature) {

    /** Datapack codec: the schema of one {@code quest/*.json} file. */
    public static final Codec<Quest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(Quest::id),
            Codec.INT.fieldOf("target").forGetter(Quest::target),
            Codec.INT.optionalFieldOf("stage", 0).forGetter(Quest::stage),
            Codec.STRING.optionalFieldOf("requires", "").forGetter(Quest::requires),
            Codec.STRING.optionalFieldOf("reward_item", "").forGetter(Quest::rewardItem),
            Codec.INT.optionalFieldOf("reward_count", 0).forGetter(Quest::rewardCount),
            Codec.STRING.optionalFieldOf("reward_rep_cult", "").forGetter(Quest::rewardRepCult),
            Codec.INT.optionalFieldOf("reward_rep_amount", 0).forGetter(Quest::rewardRepAmount),
            Repeat.codec().optionalFieldOf("repeat", Repeat.ONCE).forGetter(Quest::repeat),
            Codec.STRING.optionalFieldOf("investigator", "").forGetter(Quest::investigator),
            Codec.STRING.optionalFieldOf("trigger", "").forGetter(Quest::trigger),
            Codec.STRING.optionalFieldOf("param", "").forGetter(Quest::param),
            Codec.STRING.optionalFieldOf("reward_kit", "").forGetter(Quest::rewardKit),
            Codec.BOOL.optionalFieldOf("reward_signature", false).forGetter(Quest::rewardSignature)
    ).apply(instance, Quest::new));

    /** @return whether finishing this quest pays an item. */
    public boolean paysItem() {
        return !this.rewardItem.isEmpty() && this.rewardCount > 0;
    }

    /** @return whether finishing this quest pays cult standing (design/03c: quests grant rep). */
    public boolean paysReputation() {
        return !this.rewardRepCult.isEmpty() && this.rewardRepAmount != 0;
    }

    /** @return whether this quest belongs to the investigator {@code investigatorId}. */
    public boolean forInvestigator(String investigatorId) {
        return this.investigator.isEmpty() || this.investigator.equals(investigatorId);
    }

    /** @return whether this quest is advanced by the generic watcher {@code watched}. */
    public boolean watches(String watched) {
        return !this.trigger.isEmpty() && this.trigger.equals(watched);
    }

    /** @return whether the event's {@code value} satisfies this quest's {@link #param()}. */
    public boolean matchesParam(String value) {
        return this.param.isEmpty() || this.param.equals(value);
    }

    /** @return the localised quest name ({@code quest.eldritch_horror.<id>}). */
    public Component name() {
        return Component.translatable("quest.eldritch_horror." + this.id);
    }

    /** @return the localised one-line description ({@code quest.eldritch_horror.<id>.desc}). */
    public Component description() {
        return Component.translatable("quest.eldritch_horror." + this.id + ".desc");
    }
}
