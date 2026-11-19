package com.sanchous98.eldritchhorror.skill;

import net.minecraft.network.chat.Component;

/**
 * One node of the skill tree (design/15-skills.md): a stable id, its cost in points, an optional
 * prerequisite node, and localised name/description. Effects are read by {@link SkillTree} where the
 * relevant system runs.
 *
 * @param id       stable snake_case id (also the localisation suffix)
 * @param cost     points needed to unlock it
 * @param requires id of the node that must be unlocked first, or {@code ""} for none
 */
public record Skill(String id, int cost, String requires) {

    /** @return whether this node needs another node unlocked first. */
    public boolean hasPrerequisite() {
        return !this.requires.isEmpty();
    }

    /** @return the localised skill name ({@code skill.eldritch_horror.<id>}). */
    public Component name() {
        return Component.translatable("skill.eldritch_horror." + this.id);
    }

    /** @return the localised description ({@code skill.eldritch_horror.<id>.desc}). */
    public Component description() {
        return Component.translatable("skill.eldritch_horror." + this.id + ".desc");
    }

    /** @return the localised name of the prerequisite node, or an empty component when none. */
    public Component prerequisiteName() {
        return this.requires.isEmpty() ? Component.empty()
                : Component.translatable("skill.eldritch_horror." + this.requires);
    }
}
