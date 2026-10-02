package com.sanchous98.eldritchhorror.event;

import java.util.function.Predicate;
import net.minecraft.server.level.ServerPlayer;

/**
 * A data-driven ambient/world event (design/19-events.md): an id, a {@link EventTrigger}, a
 * duration, a selection weight and a cooldown. The actual effect is supplied as a per-tick
 * {@link Effect} so a new event is data (a trigger predicate plus an effect hook), not a new
 * ticker.
 *
 * <p>Instances are immutable and stateless: all run state lives in {@link EventTicker}, so the
 * same definition can be active for many players at once.
 *
 * @param id         stable id (the command and config key)
 * @param trigger    which cheap world fact starts it
 * @param duration   ticks the event runs once started
 * @param weight     relative selection weight when several triggers fire at once
 * @param cooldown   minimum ticks after it ends before it may start again
 * @param triggerTest fires when this is true (e.g. {@code ctx.sanity() < 20}); the ticker owns
 *                    whether the event is enabled and off cooldown
 * @param effect     applied every server tick while the event is active
 */
public record EldritchEvent(String id, EventTrigger trigger, int duration, int weight, int cooldown,
                            Predicate<EventContext> triggerTest, Effect effect) {

    /** The per-tick world effect of an event. Server side, loaded chunks only, bounded by design. */
    @FunctionalInterface
    public interface Effect {
        void tick(ServerPlayer player, EventContext ctx, int elapsed, int duration, int tick);
    }
}
