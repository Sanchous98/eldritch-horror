package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.registry.ModItems;

/** Currencies and trade tokens spent with cults and factions. Stub content; behaviour not wired yet. */
public final class Currency {
    private Currency() {
    }

    public static void init() {
        // Cult reward currency; spend with cults to buy tiers and services.
        ModItems.add("mark_of_favour", 64);
        // Ruin and structure loot; the common coin of the occult economy.
        ModItems.add("relic_coin", 64);
        // Basic cult scrip, handed out as an initiation token.
        ModItems.add("cult_token", 64);
        // Dark coin from the drowned Choironomy; accepted by the Hollow.
        ModItems.add("black_obol", 64);
        // Order-issued scrip for sanctioned trades and vault purchases.
        ModItems.add("order_scrip", 64);
        // Sealed trade mark used for barter with neutral traders.
        ModItems.add("barter_seal", 64);
    }
}
