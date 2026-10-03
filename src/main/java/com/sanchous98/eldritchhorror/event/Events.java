package com.sanchous98.eldritchhorror.event;

import com.sanchous98.eldritchhorror.core.ModConfig;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jspecify.annotations.Nullable;

/**
 * Registry of {@link EldritchEvent}s, mirroring {@code SanitySources}/{@code Locations}: register
 * once at a fixed spot, read from the tick, deterministic order.
 *
 * <p>{@link #enabled} resolves an event's config toggle by id, so each event carries its own switch
 * without the ticker knowing about each one. {@code /eh event <id>} bypasses cooldowns and the
 * master switch, deliberately, so an operator can always observe a definition.
 */
public final class Events {

    private static final List<EldritchEvent> EVENTS = new CopyOnWriteArrayList<>();
    /** O(1) id index over {@link #EVENTS}, kept in sync by {@link #register}. */
    private static final java.util.Map<String, EldritchEvent> BY_ID =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static volatile boolean initialised;

    private Events() {
    }

    /** Registers an event. Registration order is iteration order. */
    public static void register(EldritchEvent event) {
        EVENTS.add(event);
        BY_ID.put(event.id(), event);
    }

    /** Snapshot of all registered events, in registration order. */
    public static List<EldritchEvent> all() {
        init();
        return List.copyOf(EVENTS);
    }

    /** The event with {@code id}, or {@code null}. */
    public static @Nullable EldritchEvent byId(@Nullable String id) {
        init();
        return id == null ? null : BY_ID.get(id);
    }

    /** The per-event config toggle for {@code id}; unknown ids are disabled. */
    public static boolean enabled(String id) {
        return switch (id) {
            case "whisper" -> ModConfig.ENABLE_EVENT_WHISPER.get();
            case "darkness_pulse" -> ModConfig.ENABLE_EVENT_DARKNESS_PULSE.get();
            case "rift_bloom" -> ModConfig.ENABLE_EVENT_RIFT_BLOOM.get();
            case "veil_thin" -> ModConfig.ENABLE_EVENT_VEIL_THIN.get();
            case "hallucination_wave" -> ModConfig.ENABLE_EVENT_HALLUCINATION_WAVE.get();
            case "cleansing_dawn" -> ModConfig.ENABLE_EVENT_CLEANSING_DAWN.get();
            case "cult_procession" -> ModConfig.ENABLE_EVENT_CULT_PROCESSION.get();
            case "blood_moon_rite" -> ModConfig.ENABLE_EVENT_BLOOD_MOON_RITE.get();
            case "star_fall" -> ModConfig.ENABLE_EVENT_STAR_FALL.get();
            case "hollow_call" -> ModConfig.ENABLE_EVENT_HOLLOW_CALL.get();
            default -> false;
        };
    }

    /**
     * Registers the event definitions (idempotent). Called from {@link EventTicker} so the registry
     * is complete as soon as the first event tick runs.
     */
    static void init() {
        if (initialised) {
            return;
        }
        synchronized (Events.class) {
            if (initialised) {
                return;
            }
            register(EventEffects.whisper());
            register(EventEffects.darknessPulse());
            register(EventEffects.riftBloom());
            register(EventEffects.veilThin());
            register(EventEffects.hallucinationWave());
            register(EventEffects.cleansingDawn());
            register(EventEffects.cultProcession());
            register(EventEffects.bloodMoonRite());
            register(EventEffects.starFall());
            register(EventEffects.hollowCall());
            initialised = true;
        }
    }
}
