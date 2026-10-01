package com.sanchous98.eldritchhorror.cult;

import com.sanchous98.eldritchhorror.registry.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * The cult reputation axis: a per-player, per-cult integer in {@code −100…+100}.
 *
 * <p><b>This is a stub.</b> Values go up and down, persist and copy on death, and expose
 * {@link #rank}; opposed pairs apply a cross-consequence — but nothing reads reputation for
 * gameplay yet (no services, quests or dialogue). See {@code design/03c-reputation.md} and
 * {@code design/27-systems-framework.md}.
 */
public final class CultSystem {
    public static final int MIN = -100;
    public static final int MAX = 100;

    /** Generic rank thresholds (value ≥ threshold ⇒ that band); see design/03c-reputation.md. */
    private static final int[] BANDS = {20, 40, 60, 80};

    /** Fraction of a gain that is subtracted from an opposed cult. */
    private static final double OPPOSED_FACTOR = 0.5;

    private CultSystem() {
    }

    /** Current reputation with a cult, clamped. Defaults to 0. */
    public static int get(ServerPlayer player, String cultId) {
        Integer v = player.getData(ModAttachments.REPUTATION.get()).get(cultId);
        return v == null ? 0 : Math.clamp(v, MIN, MAX);
    }

    /** Sets reputation with a cult (clamped), without opposed cross-effects. Returns the value. */
    public static int set(ServerPlayer player, String cultId, int value) {
        Map<String, Integer> map = new HashMap<>(player.getData(ModAttachments.REPUTATION.get()));
        int v = Math.clamp(value, MIN, MAX);
        map.put(cultId, v);
        player.setData(ModAttachments.REPUTATION.get(), Map.copyOf(map));
        return v;
    }

    /**
     * Adds {@code delta} to a cult and applies the opposed cross-consequence to its opposites
     * (a gain costs them {@code OPPOSED_FACTOR × delta}, and vice versa). Returns the new value
     * with the cult itself.
     */
    public static int add(ServerPlayer player, String cultId, int delta) {
        Map<String, Integer> map = new HashMap<>(player.getData(ModAttachments.REPUTATION.get()));
        int v = Math.clamp(map.getOrDefault(cultId, 0) + delta, MIN, MAX);
        map.put(cultId, v);

        CultDefinition def = Cults.byId(cultId);
        if (def != null && !def.opposed().isEmpty()) {
            int spill = (int) Math.round(Math.abs(delta) * OPPOSED_FACTOR) * (delta >= 0 ? -1 : 1);
            for (String other : def.opposed()) {
                int ov = Math.clamp(map.getOrDefault(other, 0) + spill, MIN, MAX);
                map.put(other, ov);
            }
        }
        player.setData(ModAttachments.REPUTATION.get(), Map.copyOf(map));
        return v;
    }

    /**
     * The typed, cult-independent rank for a player, derived from the same bands as
     * {@link #rank}: below {@code 0} is {@link CultRank#OUTSIDER}, otherwise the ladder band. Use
     * this to branch on rank without string matching; {@link #rank} remains for display.
     */
    public static CultRank rankOf(ServerPlayer player, String cultId) {
        return CultRank.of(get(player, cultId));
    }

    /**
     * The cult-specific rank for a player: an {@code Outsider}/{@code Neutral} translatable
     * component below the first band, otherwise the cult's ladder rank for the band. Unknown
     * cult ⇒ the neutral component.
     */
    public static Component rank(ServerPlayer player, String cultId) {
        int v = get(player, cultId);
        if (v < 0) {
            return Component.translatable("rank.eldritch_horror.outsider");
        }
        int band = 0;
        while (band < BANDS.length && v >= BANDS[band]) {
            band++;
        }
        if (band == 0) {
            return Component.translatable("rank.eldritch_horror.neutral");
        }
        CultDefinition def = Cults.byId(cultId);
        if (def == null || def.ranks().isEmpty()) {
            return Component.translatable("rank.eldritch_horror.neutral");
        }
        int idx = Math.min(band - 1, def.ranks().size() - 1);
        return def.ranks().get(idx);
    }
}
