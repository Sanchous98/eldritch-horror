package com.sanchous98.eldritchhorror.world.city;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.corruption.CorruptionSystem;
import com.sanchous98.eldritchhorror.entity.Worshipper;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

/**
 * Server-side reclaim of a Fallen curated city: the bounded world work behind the {@code
 * reclaim_city} rite (design/21's "expensive rite / quest"). The rite and its gate live in {@code
 * rite/RiteEngine}; this class only performs the effect once the gate has passed.
 *
 * <p>Bounded like the rest of the city systems: it walks the district's chunk square nearest-first,
 * touches at most {@link CityReclaimMath#MAX_CHUNKS} <b>already-loaded</b> chunks ({@code
 * getChunkNow}; nothing is ever force-loaded), multiplies each chunk's taint by the configured
 * {@code reclaimTaintFactor} (default {@code 0.35}), and discards at most {@link
 * CityReclaimMath#MAX_PURGE} loaded Hollow Choir worshippers in the district so {@link CityFall}
 * has nothing left to top up once the state is no longer {@link CityState#FALLEN}. The nine chunks
 * {@code CityStates} reads the state from are cleaned before the nearest-first fill, so a successful
 * rite really does change the state it reports.
 */
public final class CityReclaim {

    /** The cult whose presence a Fallen city is purged of. */
    private static final String CHOIR = "hollow_choir";

    private CityReclaim() {
    }

    /**
     * What one reclaim did.
     *
     * @param before  the city's state before the pass
     * @param after   the city's state after the pass (recomputed from the reduced taint)
     * @param chunks  loaded district chunks touched
     * @param purged  Hollow Choir worshippers removed
     */
    public record Report(CityState before, CityState after, int chunks, int purged) {

        /** @return the chat line announcing the result ({@code rite.eldritch_horror.reclaim_city.done}). */
        public Component message() {
            return Component.translatable("rite.eldritch_horror.reclaim_city.done",
                    this.chunks, this.before.displayName(), this.after.displayName(), this.purged);
        }
    }

    /**
     * Reclaims {@code city}: reduce the loaded district's taint and purge the Choir's foothold, then
     * play the rite's hum over the city centre. The caller announces the report in chat (the rite's
     * result message), so this stays a pure world effect.
     *
     * @return the pass's report (never {@code null}; a district with no loaded chunks reports zero)
     */
    public static Report reclaim(ServerLevel level, City city) {
        CityState before = CityStates.stateOf(level, city);
        int radius = new CityLocation(city).radius();
        int chunks = cleanseDistrict(level, city, radius);
        int purged = purgeChoir(level, city, radius);
        CityState after = CityStates.stateOf(level, city);

        level.playSound(null, city.x(), level.getSeaLevel(), city.z(), ModSounds.RITUAL_HUM.get(),
                SoundSource.AMBIENT, 1.0F, 0.7F);
        return new Report(before, after, chunks, purged);
    }

    /**
     * Multiplies the taint of up to {@link CityReclaimMath#chunkBudget(int)} loaded district chunks
     * by the configured factor. The chunks {@link CityStates} reads the state from are cleaned
     * first (so the derived condition actually changes), then the nearest remaining chunks; chunks
     * that are not loaded are skipped and wait for a later cast.
     *
     * @return the number of loaded chunks actually changed
     */
    private static int cleanseDistrict(ServerLevel level, City city, int radius) {
        double factor = ModConfig.RECLAIM_TAINT_FACTOR.get();
        int centreX = city.x() >> 4;
        int centreZ = city.z() >> 4;
        int limit = CityReclaimMath.chunkBudget(radius);
        Set<Long> seen = new HashSet<>();
        int touched = 0;
        // The derived state is computed from these nine chunks: clean them first, or a bounded pass
        // over the near district could leave the city reading Fallen after a successful rite.
        for (int[] offset : CityReclaimMath.stateSampleOffsets(radius)) {
            if (touched >= limit) {
                break;
            }
            if (clean(level, centreX + offset[0], centreZ + offset[1], factor, seen)) {
                touched++;
            }
        }
        for (long offset : districtOffsets(radius)) {
            if (touched >= limit) {
                break;
            }
            int cx = centreX + (int) (offset >> 32);
            int cz = centreZ + (int) offset;
            if (clean(level, cx, cz, factor, seen)) {
                touched++;
            }
        }
        return touched;
    }

    /**
     * Multiplies one loaded chunk's taint by {@code factor}, once per pass.
     *
     * @return whether this chunk was loaded and changed (a chunk already seen is not changed again)
     */
    private static boolean clean(ServerLevel level, int chunkX, int chunkZ, double factor,
                                 Set<Long> seen) {
        if (!seen.add(((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL))) {
            return false;
        }
        LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
        if (chunk == null) {
            return false; // never force-load: unloaded district chunks wait for a later cast
        }
        CorruptionSystem.setTaint(chunk,
                CityReclaimMath.taintAfter(CorruptionSystem.getTaint(chunk), factor));
        return true;
    }

    /**
     * Chunk offsets in the district, nearest first and deterministic on ties, packed as
     * {@code (dx << 32) | (dz & 0xFFFFFFFFL)}. At most the whole district square is built (~2,600
     * entries for the largest city), never the world.
     */
    private static List<Long> districtOffsets(int radius) {
        int half = (radius + 15) / 16;
        List<Long> offsets = new ArrayList<>();
        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                if (CityReclaimMath.withinDistrict(dx, dz, radius)) {
                    offsets.add(((long) dx << 32) | (dz & 0xFFFFFFFFL));
                }
            }
        }
        offsets.sort(Comparator.comparingLong(CityReclaim::distanceSq));
        return offsets;
    }

    /** Distance from the city centre chunk, used only to order {@link #districtOffsets}. */
    private static long distanceSq(long packed) {
        int dx = (int) (packed >> 32);
        int dz = (int) packed;
        return (long) dx * dx + (long) dz * dz;
    }

    /**
     * Discards loaded Hollow Choir worshippers in the district, up to {@link
     * CityReclaimMath#MAX_PURGE}, so a reclaim does not instantly fall back under the Choir's
     * ten-second takeover.
     */
    private static int purgeChoir(ServerLevel level, City city, int radius) {
        AABB box = new AABB(city.x() - radius, level.getMinY(), city.z() - radius,
                city.x() + radius, level.getMaxY(), city.z() + radius);
        List<Worshipper> choir = level.getEntitiesOfClass(Worshipper.class, box,
                worshipper -> CHOIR.equals(worshipper.cultId()));
        int purged = 0;
        for (Worshipper worshipper : choir) {
            if (purged >= CityReclaimMath.MAX_PURGE) {
                break;
            }
            worshipper.discard();
            purged++;
        }
        return purged;
    }
}
