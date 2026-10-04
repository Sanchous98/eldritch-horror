package com.sanchous98.eldritchhorror.world.threshold;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/**
 * Protects the Threshold hub from destruction. The hub is authored geometry (platform, pedestals,
 * gate arches, barrier) and is the one-time prologue's whole point, so a player must not be able to
 * mine or blow it up — whether by accident or to farm the frames.
 *
 * <p>Server-authoritative and dimension-scoped: only the {@code eldritch_horror:threshold} level is
 * protected, so ordinary building in the Overworld is untouched. The stamp itself uses
 * {@code ServerLevel#setBlock} directly and never fires these player events, so protection cannot
 * block the initial placement.
 *
 * <p>{@code BreakBlockEvent} covers player mining; {@code ExplosionEvent.Start} cancels any
 * explosion in the dimension (TNT, creepers, the star-fall crater is in the Overworld, so it is
 * unaffected).
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class ThresholdProtection {

    private ThresholdProtection() {
    }

    /** Cancel player block-breaking anywhere in the Threshold dimension. */
    @SubscribeEvent
    public static void onBreak(BreakBlockEvent event) {
        if (event.getLevel() instanceof Level level && level.dimension().equals(Threshold.DIMENSION)) {
            event.setCanceled(true);
            event.setNotifyClient(true);
        }
    }

    /** Cancel explosions in the Threshold, so the hub cannot be blown apart. */
    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent.Start event) {
        if (event.getLevel().dimension().equals(Threshold.DIMENSION)) {
            event.setCanceled(true);
        }
    }
}
