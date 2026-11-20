package com.sanchous98.eldritchhorror.world.transport;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sanchous98.eldritchhorror.EldritchHorror;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/**
 * Riders who logged off mid-voyage ({@code design/30-transport.md}: "log off — the latter ends the
 * ride at the next port").
 *
 * <p>When a passenger disconnects while the packet is under way, {@link ShipService} records the
 * destination harbour's shore point here. On the next login the player is put down there, so an
 * ocean logout never leaves them treading water. The map is tiny, overworld-only and persisted so it
 * survives a restart.
 */
public final class ShipRiders extends SavedData {

    /** A shore point in the overworld. */
    public record Drop(double x, double z) {
    }

    private static final Codec<Drop> DROP_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("x").forGetter(Drop::x),
            Codec.DOUBLE.fieldOf("z").forGetter(Drop::z)
    ).apply(instance, Drop::new));

    private static final Codec<ShipRiders> CODEC =
            Codec.unboundedMap(Codec.STRING, DROP_CODEC).xmap(ShipRiders::new, ShipRiders::asMap);

    private static final SavedDataType<ShipRiders> TYPE =
            new SavedDataType<>(EldritchHorror.id("ship_riders"), ShipRiders::new, CODEC);

    private final Map<String, Drop> drops = new HashMap<>();

    private ShipRiders() {
    }

    private ShipRiders(Map<String, Drop> drops) {
        this.drops.putAll(drops);
    }

    /** @return the overworld's pending-drop ledger (created on first read). */
    public static ShipRiders of(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    /** Records that {@code playerId} should be put ashore at {@code (x, z)} on their next login. */
    public void put(UUID playerId, double x, double z) {
        this.drops.put(playerId.toString(), new Drop(x, z));
        this.setDirty();
    }

    /** @return the pending drop for this player, removing it, or {@code null}. */
    public @Nullable Drop take(UUID playerId) {
        Drop drop = this.drops.remove(playerId.toString());
        if (drop != null) {
            this.setDirty();
        }
        return drop;
    }

    /** @return how many logouts are waiting to be resolved (diagnostics). */
    public int pending() {
        return this.drops.size();
    }

    private Map<String, Drop> asMap() {
        return Map.copyOf(this.drops);
    }
}
