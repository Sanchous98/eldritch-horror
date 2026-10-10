package com.sanchous98.eldritchhorror.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

/**
 * One journal quest, loaded from {@code data/<namespace>/quest/<file>.json} (design/13). Stable id,
 * the count needed to finish it, a coarse act/ordering stage, the id of the quest it follows (a soft
 * prerequisite used for the "next objective" hint) and an optional item gift paid on completion.
 *
 * <p>Localised name/description stay as lang keys ({@code quest.eldritch_horror.<id>} and
 * {@code ….desc}), so the data file holds numbers and ids, not prose — a translator's file stays the
 * single place for words.
 *
 * @param id          stable snake_case id (also the localisation suffix)
 * @param target      the count at which the quest is complete
 * @param stage       act/ordering bucket: the journal lists by stage, then by id
 * @param requires    quest id that should be finished first ({@code ""} for none)
 * @param rewardItem  item path paid on completion ({@code ""} for none); namespaced ids are honoured
 * @param rewardCount how many of {@link #rewardItem()} are paid
 */
public record Quest(String id, int target, int stage, String requires, String rewardItem, int rewardCount) {

    /** Datapack codec: the schema of one {@code quest/*.json} file. */
    public static final Codec<Quest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(Quest::id),
            Codec.INT.fieldOf("target").forGetter(Quest::target),
            Codec.INT.optionalFieldOf("stage", 0).forGetter(Quest::stage),
            Codec.STRING.optionalFieldOf("requires", "").forGetter(Quest::requires),
            Codec.STRING.optionalFieldOf("reward_item", "").forGetter(Quest::rewardItem),
            Codec.INT.optionalFieldOf("reward_count", 0).forGetter(Quest::rewardCount)
    ).apply(instance, Quest::new));

    /** @return whether finishing this quest pays an item. */
    public boolean paysItem() {
        return !this.rewardItem.isEmpty() && this.rewardCount > 0;
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
