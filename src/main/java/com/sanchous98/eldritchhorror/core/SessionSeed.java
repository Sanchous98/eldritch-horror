package com.sanchous98.eldritchhorror.core;

import com.sanchous98.eldritchhorror.EldritchHorror;
import java.util.Random;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * The randomness of one running session.
 *
 * <p>A session is one process with one world (see {@code docs/SESSIONS.md}). Everything that should
 * be fresh on every playthrough but stable within it — dungeon layouts, which hidden entrances are
 * lit, whisper targets — is derived from this seed. It is deliberately <b>not saved</b>: restarting
 * the process rolls a new session, and the persistent world (cities, bosses, builds) is untouched.
 *
 * <p>The seed is logged once at startup so a session can be reproduced for debugging.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class SessionSeed {

    /** Drawn once per process; never written to disk. */
    private static final long SEED = new Random().nextLong();

    private SessionSeed() {
    }

    /** @return this session's seed. */
    public static long value() {
        return SEED;
    }

    /**
     * Derives a stable sub-seed for a named subsystem, so two dungeons of the same session differ
     * while the same dungeon is reproducible within the session.
     *
     * @param key a stable name, e.g. {@code "dungeon:paris:cellar-3"}
     * @return a 64-bit seed derived from the session seed and the key
     */
    public static long derive(String key) {
        long hash = 0xCBF29CE484222325L;
        for (int i = 0; i < key.length(); i++) {
            hash ^= key.charAt(i);
            hash *= 0x100000001B3L;
        }
        long mixed = SEED ^ hash;
        // SplitMix64 finaliser: spreads adjacent keys across the whole range.
        mixed = (mixed ^ (mixed >>> 30)) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ (mixed >>> 27)) * 0x94D049BB133111EBL;
        return mixed ^ (mixed >>> 31);
    }

    /** Logs the seed once the server is up, never during class loading or tests. */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        EldritchHorror.LOGGER.info("session: seed {} (not saved; restart rolls a new session)",
                Long.toHexString(SEED));
    }
}
