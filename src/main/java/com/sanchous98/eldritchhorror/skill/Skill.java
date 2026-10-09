package com.sanchous98.eldritchhorror.skill;

import net.minecraft.network.chat.Component;

/**
 * One node of the skill tree (design/13, skill tree): a stable id, its cost in points, and localised
 * name/description. Effects are read by {@link SkillTree} where the relevant system runs.
 *
 * @param id   stable snake_case id (also the localisation suffix)
 * @param cost points needed to unlock it
 */
public record Skill(String id, int cost) {

    /** @return the localised skill name ({@code skill.eldritch_horror.<id>}). */
    public Component name() {
        return Component.translatable("skill.eldritch_horror." + this.id);
    }

    /** @return the localised description ({@code skill.eldritch_horror.<id>.desc}). */
    public Component description() {
        return Component.translatable("skill.eldritch_horror." + this.id + ".desc");
    }
}
