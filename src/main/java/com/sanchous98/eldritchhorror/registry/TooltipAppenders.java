package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.registry.items.ConsumableItem;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.tooltip.TooltipLocation;
import net.neoforged.neoforge.event.RegisterTooltipAppendersEvent;

/**
 * Registers the mod's item tooltip lines through NeoForge's non-deprecated tooltip hook.
 *
 * <p>Vanilla's {@code Item.appendHoverText} is {@code @Deprecated} in 26.3 (the engine now layers
 * tooltip appenders). NeoForge exposes the replacement surface, {@link RegisterTooltipAppendersEvent}
 * — a mod-bus event (so {@code @EventBusSubscriber} routes it correctly, exactly like the attribute
 * event). One appender handles every {@link ConsumableItem}: it appends the stored sanity/corruption
 * deltas as colour-coded lines. Nothing is read or written for gameplay here — presentation only.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class TooltipAppenders {

    private TooltipAppenders() {
    }

    @SubscribeEvent
    public static void onRegisterTooltipAppenders(RegisterTooltipAppendersEvent event) {
        // POST_CUSTOM sits just after the item's own custom tooltip, matching where the old
        // appendHoverText override drew its lines.
        event.registerAppender(TooltipLocation.POST_CUSTOM, (stack, context, display, player, flag, builder) -> {
            if (stack.getItem() instanceof ConsumableItem consumable) {
                appendDelta(builder, "tooltip.eldritch_horror.sanity", consumable.sanityDelta());
                appendDelta(builder, "tooltip.eldritch_horror.corruption", consumable.corruptionDelta());
            }
        });
    }

    /** Emits one colour-coded "label +N"/"label −N" line when {@code delta} is non-zero. */
    private static void appendDelta(Consumer<Component> builder, String key, double delta) {
        if (delta == 0) {
            return;
        }
        long n = Math.round(delta);
        ChatFormatting colour = n > 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
        builder.accept(Component.translatable(key, n).withStyle(colour));
    }
}
