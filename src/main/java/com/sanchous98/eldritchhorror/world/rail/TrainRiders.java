package com.sanchous98.eldritchhorror.world.rail;

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
 * Riders who logged off mid-voyage on a train ({@code design/30-transport.md}: a logout "ends the
 * ride at the next port" — for a train, the next station).
 *
 * <p>When a passenger disconnects while the consist is under way, the train service records the
 * destination station's boarding point here. On the next login the player is put down there, so a
 * logout between two stations never strands them in the wilderness. The map is tiny,
 * overworld-only and persisted so it survives a restart.
 */
public final class TrainRiders extends SavedData {

    /** A station boarding point in the overworld. */
    public record Drop(double x, double y, double z) {
    }

    private static final Codec<Drop> DROP_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("x").forGetter(Drop::x),
            Codec.DOUBLE.fieldOf("y").forGetter(Drop::y),
            Codec.DOUBLE.fieldOf("z").forGetter(Drop::z)
    ).apply(instance, Drop::new));

    private static final Codec<TrainRiders> CODEC =
            Codec.unboundedMap(Codec.STRING, DROP_CODEC).xmap(TrainRiders::new, TrainRiders::asMap);

    private static final SavedDataType<TrainRiders> TYPE =
            new SavedDataType<>(EldritchHorror.id("train_riders"), TrainRiders::new, CODEC);

    private final Map<String, Drop> drops = new HashMap<>();

    private TrainRiders() {
    }

    private TrainRiders(Map<String, Drop> drops) {
        this.drops.putAll(drops);
    }

    /** @return the overworld's pending-drop ledger (created on first read). */
    public static TrainRiders of(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    /** Records that {@code playerId} should be put down at a station on their next login. */
    public void put(UUID playerId, double x, double y, double z) {
        this.drops.put(playerId.toString(), new Drop(x, y, z));
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
