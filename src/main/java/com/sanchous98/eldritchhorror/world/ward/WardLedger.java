package com.sanchous98.eldritchhorror.world.ward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/**
 * The world's ward ledger (design/26, "Warding and sealing"): the small set of havens raised by the
 * {@code ward_of_the_eye} rite. Like {@link com.sanchous98.eldritchhorror.endgame.RiftLedger}, it
 * is per-level {@link SavedData}, so a ward survives a restart and every player in the level shares
 * it. Expiry uses the level's game time, which is itself saved.
 *
 * <p>The list is capped at {@value #MAX_WARDS} entries; raising a ward when full evicts the oldest,
 * so repair-spam cannot grow the save without bound. Raising again at the same block refreshes that
 * ward in place rather than stacking a duplicate.
 *
 * <p>A ward covers a horizontal cylinder around its centre (see {@link WardMath#covers}); its
 * radius is stored per ward so a config change does not retroactively resize an existing haven.
 */
public final class WardLedger extends SavedData {

    /** Hard cap on stored wards; the oldest entry is evicted first. */
    public static final int MAX_WARDS = 64;

    /** One haven: where it is, how far it reaches, and when it lapses. */
    public record Ward(int x, int y, int z, int radius, long expiresAtGameTime) {

        /** Canonical constructor: a stored radius is never allowed to be non-positive. */
        public Ward {
            radius = Math.max(1, radius);
        }

        /** @return the ward's centre as a block position. */
        public BlockPos pos() {
            return new BlockPos(this.x, this.y, this.z);
        }
    }

    private static final Codec<Ward> WARD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(Ward::x),
            Codec.INT.fieldOf("y").forGetter(Ward::y),
            Codec.INT.fieldOf("z").forGetter(Ward::z),
            Codec.INT.fieldOf("radius").forGetter(Ward::radius),
            Codec.LONG.fieldOf("expires").forGetter(Ward::expiresAtGameTime)
    ).apply(instance, Ward::new));

    private static final Codec<WardLedger> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WARD_CODEC.listOf().optionalFieldOf("wards", List.of()).forGetter(WardLedger::wardList)
    ).apply(instance, WardLedger::new));

    private static final SavedDataType<WardLedger> TYPE =
            new SavedDataType<>(EldritchHorror.id("ward_ledger"), WardLedger::new, CODEC);

    /** Oldest first, so eviction is a remove(0). */
    private final List<Ward> wards = new ArrayList<>();

    private WardLedger() {
    }

    private WardLedger(List<Ward> wards) {
        this.wards.addAll(wards);
        while (this.wards.size() > MAX_WARDS) {
            this.wards.remove(0);
        }
    }

    /** @return this level's ward ledger (created on first read). */
    public static WardLedger of(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    /**
     * Raises (or refreshes) a ward centred on {@code pos}.
     *
     * @return the stored ward, or {@code null} when wards are disabled in the config
     */
    public @Nullable Ward raise(ServerLevel level, BlockPos pos, int radius, int durationTicks) {
        if (!ModConfig.ENABLE_WARDS.get()) {
            return null;
        }
        long now = level.getGameTime();
        prune(now);
        Ward ward = new Ward(pos.getX(), pos.getY(), pos.getZ(), radius,
                now + Math.max(1, durationTicks));
        for (int i = 0; i < this.wards.size(); i++) {
            Ward existing = this.wards.get(i);
            if (existing.x() == ward.x() && existing.y() == ward.y()
                    && existing.z() == ward.z()) {
                this.wards.set(i, ward);
                this.setDirty();
                return ward;
            }
        }
        while (this.wards.size() >= MAX_WARDS) {
            this.wards.remove(0);
        }
        this.wards.add(ward);
        this.setDirty();
        return ward;
    }

    /**
     * Whether {@code player} stands inside an unexpired ward. The single guard for every
     * <em>ambient</em> sanity drain that does not run through {@code SanitySources} (presence auras,
     * a husk crowd, Shub-Niggurath's stillness): the haven is a place, so it must shelter from those
     * too. Rite and item costs are deliberately <b>not</b> routed through this.
     *
     * @return whether the player is sheltered by a ward right now
     */
    public static boolean shelters(net.minecraft.server.level.ServerPlayer player) {
        if (!ModConfig.ENABLE_WARDS.get()
                || !(player.level() instanceof ServerLevel level)) {
            return false;
        }
        return of(level).wardAt(level, player.blockPosition()) != null;
    }

    /**
     * The nearest unexpired ward covering {@code pos}, or {@code null}. Horizontal distance only;
     * expired entries are pruned on the way past.
     */
    public @Nullable Ward wardAt(ServerLevel level, BlockPos pos) {
        if (!ModConfig.ENABLE_WARDS.get()) {
            return null;
        }
        prune(level.getGameTime());
        Ward best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Ward ward : this.wards) {
            double dx = pos.getX() - ward.x();
            double dz = pos.getZ() - ward.z();
            if (!WardMath.covers(dx, dz, ward.radius())) {
                continue;
            }
            double distance = dx * dx + dz * dz;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = ward;
            }
        }
        return best;
    }

    /** Drops expired wards. Called from reads, so a lapsed haven costs nothing after the first tick. */
    public int prune(long now) {
        int removed = 0;
        var iterator = this.wards.iterator();
        while (iterator.hasNext()) {
            if (WardMath.expired(now, iterator.next().expiresAtGameTime())) {
                iterator.remove();
                removed++;
            }
        }
        if (removed > 0) {
            this.setDirty();
        }
        return removed;
    }

    /** @return an immutable snapshot of every stored ward, oldest first (for the debug command). */
    public List<Ward> wards() {
        return List.copyOf(this.wards);
    }

    /** The serialised shape (also the codec's getter). */
    private List<Ward> wardList() {
        return List.copyOf(this.wards);
    }
}
