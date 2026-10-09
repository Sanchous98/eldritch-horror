package com.sanchous98.eldritchhorror.quest;

import net.minecraft.network.chat.Component;

/**
 * One journal quest (design/13, quest journal): a stable id, the progress count needed to finish it,
 * and localised name/description. Progress is stored per player on the synced
 * {@code QUESTS} attachment and advanced by the systems the quest watches.
 *
 * @param id     stable snake_case id (also the localisation suffix)
 * @param target the count at which the quest is complete
 */
public record Quest(String id, int target) {

    /** @return the localised quest name ({@code quest.eldritch_horror.<id>}). */
    public Component name() {
        return Component.translatable("quest.eldritch_horror." + this.id);
    }

    /** @return the localised one-line description ({@code quest.eldritch_horror.<id>.desc}). */
    public Component description() {
        return Component.translatable("quest.eldritch_horror." + this.id + ".desc");
    }
}
