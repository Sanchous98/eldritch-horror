package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.registry.ModItems;

/** Faction insignia, contracts and reputation ledgers. Stub content; behaviour not wired yet. */
public final class Factions {
    private Factions() {
    }

    public static void init() {
        // Cult insignia; marks initiation and rank within a cult.
        ModItems.add("cult_insignia", 1);
        // Order sigil; proof of standing with the Unblinking Eye.
        ModItems.add("order_sigil", 1);
        // Choir reed; token and instrument of the Drowned Choir.
        ModItems.add("choir_reed", 1);
        // Wardens' badge; authority token of the Wardens.
        ModItems.add("wardens_badge", 1);
        // Blood contract; a binding pledge between a player and a faction.
        ModItems.add("blood_contract", 1);
        // Oath ring; seals a sworn allegiance.
        ModItems.add("oath_ring", 1);
        // Reputation ledger; records per-faction standing (03c-reputation).
        ModItems.add("reputation_ledger", 16);
        // Faction seal; stamps documents and unlocks sealed trades.
        ModItems.add("faction_seal", 1);
    }
}
