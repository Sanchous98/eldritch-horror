package com.sanchous98.eldritchhorror.world.city;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Authoritative per-city state, independent of loaded chunks.
 *
 * <p>The old model derived a city's state from nine sampled chunks and returned {@code THRIVING}
 * when none of them was loaded, so a fallen city nobody was near looked healthy and could not
 * decay. This ledger is the single world-level source of truth: abstract taint grows on a global
 * ticker (accelerated by the doom clock), physical chunk taint is folded in when samples are
 * loaded, and the state machine ({@link CityState#of}) is evaluated from the ledger value.
 *
 * <p>Following the board game's <i>Cities in Ruin</i>, a fallen city is a <b>devastated place</b>,
 * not a lair: no Ancient One is parked in it (Ancient Ones stay world-level), the district keeps
 * its ruins, loses its services, drains sanity and is taken over by the Hollow Choir. Growth stops
 * while the server is empty and for a grace period after a reclaim.
 *
 * <p>The nine-chunk samples stay the physical layer: {@link CityStates#sampledTaint} reads them
 * only when loaded, and the ledger never loads a chunk.
 */
public final class CityLedger extends SavedData {

    /** Taint stored for a city with no entry yet. */
    private static final Entry EMPTY = new Entry(0.0, 0L);
    /** A single ticker step cannot advance more than this (guards a stalled server). */
    private static final long MAX_STEP_TICKS = 1200L;

    private static final Codec<CityLedger> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Entry.CODEC).optionalFieldOf("cities",
                    Map.<String, Entry>of()).forGetter(ledger -> ledger.entries),
            Codec.LONG.optionalFieldOf("last_advance", 0L).forGetter(ledger -> ledger.lastAdvance)
    ).apply(instance, CityLedger::new));

    private static final SavedDataType<CityLedger> TYPE =
            new SavedDataType<>(EldritchHorror.id("city_ledger"), CityLedger::new, CODEC);

    private final Map<String, Entry> entries;
    private long lastAdvance;

    private CityLedger() {
        this.entries = new HashMap<>();
    }

    private CityLedger(Map<String, Entry> entries, long lastAdvance) {
        this.entries = new HashMap<>(entries);
        this.lastAdvance = lastAdvance;
    }

    /** @return the overworld's ledger (created on first read); safe from any dimension. */
    public static CityLedger of(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    /** @return the authoritative district taint for {@code cityId}, {@code 0..1}. */
    public double taint(String cityId) {
        return entry(cityId).taint();
    }

    /** @return the stored entry, or a zeroed default. */
    public Entry entry(String cityId) {
        return this.entries.getOrDefault(cityId, EMPTY);
    }

    /** @return the state derived from the ledger taint. */
    public CityState state(String cityId) {
        return CityState.of(taint(cityId));
    }

    /**
     * One abstract step. Called on the server thread every {@code CityDecay.INTERVAL_TICKS}.
     *
     * @param level overworld level
     * @param now   current game time
     * @param doom  current doom clock, {@code 0..100}
     * @return cities that crossed into {@code FALLEN} during this step
     */
    public List<City> advance(ServerLevel level, long now, double doom) {
        if (this.lastAdvance <= 0L) {
            this.lastAdvance = now;
            return List.of();
        }
        long dt = Math.min(MAX_STEP_TICKS, Math.max(0L, now - this.lastAdvance));
        this.lastAdvance = now;
        if (dt == 0L) {
            return List.of();
        }
        double decayTicks = Math.max(1.0, ModConfig.CITY_DECAY_TICKS.get());
        double boost = ModConfig.CITY_DECAY_DOOM_BOOST.get();
        double doomFactor = 1.0 + Math.clamp(doom / 100.0, 0.0, 1.0) * boost;
        double perTick = doomFactor / decayTicks;
        long grace = ModConfig.CITY_RECLAIM_GRACE_TICKS.get();
        List<City> justFell = new ArrayList<>();

        for (City city : Cities.all()) {
            Entry old = entry(city.id());
            double taint = old.taint();
            boolean decaying = ModConfig.ENABLE_CITY_DECAY.get()
                    && now >= old.lastIntervention() + grace;
            if (decaying) {
                taint = Math.min(1.0, taint + perTick * dt);
            }
            double sampled = CityStates.sampledTaint(level, city);
            if (sampled > taint) {
                taint = sampled;
            }
            if (CityState.of(old.taint()) != CityState.FALLEN
                    && CityState.of(taint) == CityState.FALLEN) {
                justFell.add(city);
            }
            this.entries.put(city.id(), new Entry(taint, old.lastIntervention()));
        }
        setDirty();
        return justFell;
    }

    /**
     * Applies a reclaim: the district drops to the freshly sampled taint and growth pauses for the
     * configured grace period.
     *
     * @param city       reclaimed city
     * @param afterTaint sampled taint after cleansing, or {@code < 0} when nothing was loaded
     * @param now        current game time
     */
    public void onReclaim(City city, double afterTaint, long now) {
        Entry old = entry(city.id());
        double taint = afterTaint >= 0.0
                ? afterTaint
                : old.taint() * ModConfig.RECLAIM_TAINT_FACTOR.get();
        this.entries.put(city.id(), new Entry(Math.clamp(taint, 0.0, 1.0), now));
        setDirty();
    }

    /** Test/admin hook: sets taint immediately and starts the reclaim grace. */
    public void setTaint(City city, double value, long now) {
        this.entries.put(city.id(), new Entry(Math.clamp(value, 0.0, 1.0), now));
        setDirty();
    }

    /** One city's persisted record. */
    public record Entry(double taint, long lastIntervention) {

        private static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("taint", 0.0).forGetter(Entry::taint),
                Codec.LONG.optionalFieldOf("intervention", 0L)
                        .forGetter(Entry::lastIntervention)
        ).apply(instance, Entry::new));
    }
}
