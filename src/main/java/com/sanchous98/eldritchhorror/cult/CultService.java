package com.sanchous98.eldritchhorror.cult;

import net.minecraft.network.chat.Component;

/**
 * A rank-gated cult service: the concrete payoff of reputation, seeded per {@code design/09-cults.md}
 * ("services unlock at rank thresholds"). Plain data on {@link CultDefinition#services()}, resolved
 * server-side by {@link CultServices}.
 *
 * <p>Most services are reputation-gated and free ({@link Kind#TEACH}, {@link Kind#CLEANSE}); a
 * {@link Kind#TRADE} also prices the exchange in one item ({@link #costItem()}, {@link #costCount()})
 * and grants another ({@link #target()}, {@link #grantCount()}); a {@link Kind#OFFER} spends the
 * declared item and pays reputation instead, which is how standing climbs from zero. That is what
 * makes the mod's currencies ({@code black_obol}, {@code cult_token}, …) spendable rather than dead
 * weight.
 *
 * @param id         stable snake_case id (localisation suffix {@code service.eldritch_horror.<id>})
 * @param minRank    lowest {@link CultRank} that unlocks the service
 * @param kind       what the service does when performed
 * @param target     rite id taught by {@link Kind#TEACH}, or granted item id for {@link Kind#TRADE}
 * @param costItem   item id spent by {@link Kind#TRADE}/{@link Kind#OFFER} ({@code ""} for free services)
 * @param costCount  how many of {@link #costItem()} are spent
 * @param grantCount how many of {@link #target()} a {@link Kind#TRADE} grants, or how much
 *                   reputation a {@link Kind#OFFER} pays
 */
public record CultService(String id, CultRank minRank, Kind kind, String target,
                          String costItem, int costCount, int grantCount) {

    /** A free, rank-gated service (the seeded {@code TEACH}/{@code CLEANSE} shape). */
    public CultService(String id, CultRank minRank, Kind kind, String target) {
        this(id, minRank, kind, target, "", 0, 0);
    }

    /** A paid exchange: {@code costCount} of {@code costItem} for {@code grantCount} of {@code grantItem}. */
    public static CultService trade(String id, CultRank minRank, String costItem, int costCount,
                                    String grantItem, int grantCount) {
        return new CultService(id, minRank, Kind.TRADE, grantItem, costItem, costCount, grantCount);
    }

    /**
     * A demanded-item offering: spend {@code count} of {@code item} to earn {@code reputation} points
     * with the cult (design/03c: "Offer a demanded item: +5 per offering"). All-or-nothing, like a
     * trade; the gain still crosses the opposed pairs.
     */
    public static CultService offer(String id, CultRank minRank, String item, int count, int reputation) {
        return new CultService(id, minRank, Kind.OFFER, "", item, count, reputation);
    }

    /** The kinds of service the system can resolve today. */
    public enum Kind {
        /** Teach {@link #target} to the caller via {@code RiteKnowledge} (design/26: cult-taught rites). */
        TEACH,
        /** Lower the caller's corruption and the local chunk taint (the Order's cleansing). */
        CLEANSE,
        /** Spend {@link #costItem} to grant {@link #target} (the cults buy and sell). */
        TRADE,
        /**
         * Spend {@link #costItem} to earn {@link #grantCount} reputation — the cult's demanded
         * offering (design/03c). Available from {@link CultRank#NEUTRAL}, so standing can be built
         * from zero without a rite or a quest.
         */
        OFFER
    }

    /** @return whether this service charges an item. */
    public boolean paid() {
        return !this.costItem.isEmpty() && this.costCount > 0;
    }

    /** @return the localised display name ({@code service.eldritch_horror.<id>}). */
    public Component name() {
        return Component.translatable("service.eldritch_horror." + this.id);
    }
}
