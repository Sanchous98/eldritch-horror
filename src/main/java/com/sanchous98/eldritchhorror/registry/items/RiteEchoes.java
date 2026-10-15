package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.rite.RiteDefinition;
import com.sanchous98.eldritchhorror.rite.Rites;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * The chalice of echoes' memory (design/16): the id of the last rite that <b>took effect</b> for each
 * player, written by {@code rite/RiteEngine.perform} on success and read by the chalice. In-memory
 * only — an echo is a divination, not a save: it resets with the JVM (the same shape as the rite
 * cooldown map). Only ids are stored; the definition is resolved through {@link Rites}, so learning
 * or replacing a rite is never duplicated here.
 */
public final class RiteEchoes {

    private static final Map<UUID, String> LAST = new ConcurrentHashMap<>();

    private RiteEchoes() {
    }

    /** Records {@code riteId} as the player's most recent successful rite. */
    public static void record(ServerPlayer player, String riteId) {
        LAST.put(player.getUUID(), riteId);
    }

    /** The last successful rite's definition for {@code player}, or {@code null} if none yet. */
    public static @Nullable RiteDefinition last(ServerPlayer player) {
        return Rites.byId(LAST.get(player.getUUID()));
    }
}
