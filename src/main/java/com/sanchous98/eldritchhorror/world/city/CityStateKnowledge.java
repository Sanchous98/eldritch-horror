package com.sanchous98.eldritchhorror.world.city;

import com.sanchous98.eldritchhorror.registry.ModAttachments;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * Server-authoritative access to the city states a player has discovered, layered over the synced
 * {@link ModAttachments#CITY_STATES} attachment (design/21: the map shows a settlement's condition).
 * The world map reads the attachment on the client and colours markers by it.
 *
 * <p>Values are {@link CityState} ordinals; the map is an immutable snapshot, so writes are
 * read-modify-write.
 */
public final class CityStateKnowledge {

    private CityStateKnowledge() {
    }

    /** @return the discovered state of {@code cityId}, or {@code null} if the player has not seen it. */
    public static @Nullable CityState state(ServerPlayer player, String cityId) {
        Integer ordinal = player.getData(ModAttachments.CITY_STATES).get(cityId);
        return ordinal == null ? null : CityState.byOrdinal(ordinal);
    }

    /** Records {@code city}'s current state for {@code player}. No-op when unchanged. */
    public static void record(ServerPlayer player, City city, CityState state) {
        Map<String, Integer> current = player.getData(ModAttachments.CITY_STATES);
        Integer existing = current.get(city.id());
        if (existing != null && existing == state.ordinal()) {
            return;
        }
        Map<String, Integer> next = new HashMap<>(current);
        next.put(city.id(), state.ordinal());
        player.setData(ModAttachments.CITY_STATES, Map.copyOf(next));
    }
}
