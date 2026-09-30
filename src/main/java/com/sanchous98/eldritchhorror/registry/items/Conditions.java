package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.registry.ModItems;

/**
 * Conditions (FFG Condition traits: <em>Bane</em>, <em>Boon</em>, <em>Deal</em>,
 * <em>Exposure</em>, <em>Illness</em>, <em>Madness</em>, <em>Pursuit</em>). Intended behaviour
 * noted per item; not wired yet.
 *
 * <p>These are item-represented statuses for now: each stands in for a condition that will
 * instead be an attachment/effect once the sanity and corruption systems land
 * (see {@code design/27-systems-framework.md}). Stacks are 1 — they are flags, not bulk goods.
 */
public final class Conditions {
    private Conditions() {
    }

    public static void init() {
        // Cursed Charm — Bane (Exposure). −sanity while carried; can be cleansed by rite.
        ModItems.add("cursed_charm", 1);
        // Marked Sigil — Bane (Pursuit). Eldritch entities track the bearer across chunks.
        ModItems.add("marked_sigil", 1);
        // Debt Note — Deal. Owed to a cult; unpaid deadline costs reputation and sanity.
        ModItems.add("debt_note", 1);
        // Dark Pact — Deal. Boon: +power to dark rites. Bane: sanity floor lowered, corruption climbs.
        ModItems.add("dark_pact", 1);
        // Haunted Relic — Bane (Madness). Random sanity jolts and phantom sounds while held.
        ModItems.add("haunted_relic", 1);
        // Diseased Token — Bane (Illness). Periodic weakness and hunger-style penalties.
        ModItems.add("diseased_token", 1);
        // Paranoia Token — Bane (Madness). Nearby players/entities read as hostile; sanity drain.
        ModItems.add("paranoia_token", 1);
        // Hallucination Echo — Bane (Madness). Spawns false entities/sounds; sanity −.
        ModItems.add("hallucination_echo", 1);

        // Blessing of Isis — Boon. Sanity recovery aura; cleanses one Bane on use.
        ModItems.add("blessing_of_isis", 1);
        // Holy Water — Boon. Throwable cleanse; removes Exposure/curse stacks in an area.
        ModItems.add("holy_water", 1);
        // Holy Cross — Boon. Ward against pursuit; repels lesser spawns, −corruption nearby.
        ModItems.add("holy_cross", 1);
        // King James Bible — Boon. Strong ward vs. madness; sanity recovery while read.
        ModItems.add("king_james_bible", 1);
        // Kerosene — Boon/utility. Fuel for lanterns and fire rites; burns corruption fields.
        ModItems.add("kerosene", 1);
        // Lantern — Boon. Light source that suppresses darkness sanity drain; burns kerosene.
        ModItems.add("lantern", 1);
        // Grasping Idol — Cursed. Boon: attracts loot to the bearer. Bane: attracts hostile
        // eldritch entities. (design/16-items.md, Cursed items.)
        ModItems.add("grasping_idol", 1);
    }
}
