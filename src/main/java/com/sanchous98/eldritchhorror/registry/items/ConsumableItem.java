package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;

import java.util.function.Consumer;

/**
 * A reusable use-to-apply item: right-clicking applies fixed sanity/corruption deltas.
 *
 * <p>All effects are applied <b>server-side only</b>, through {@link SanityAPI}/
 * {@link CorruptionAPI} (content never touches the attachment directly —
 * {@code design/27-systems-framework.md}). The client path returns success so the swing animates
 * but changes no state. Deterministic: a fixed delta, no randomness.
 *
 * <p>When {@code consumed} is {@code true} (tonics) one item leaves the stack (unless the player
 * has infinite materials). When {@code false} (tomes are knowledge, not consumables) the stack is
 * left intact and only the cost is paid.
 */
@NullMarked
public class ConsumableItem extends Item {
    private final double sanity;
    private final double corruption;
    private final boolean consumed;

    /**
     * A consumed tonic: applies the deltas and shrinks the stack by one.
     *
     * @param properties the item properties (stack size etc.)
     * @param sanity     the sanity delta applied on use (may be negative)
     * @param corruption the corruption delta applied on use (may be negative)
     */
    public ConsumableItem(Item.Properties properties, double sanity, double corruption) {
        this(properties, sanity, corruption, true);
    }

    /**
     * @param consumed whether using the item consumes one from the stack (false for tomes)
     */
    public ConsumableItem(Item.Properties properties, double sanity, double corruption, boolean consumed) {
        super(properties);
        this.sanity = sanity;
        this.corruption = corruption;
        this.consumed = consumed;
    }

    /** The sanity delta applied on use (client-visible tooltip source). */
    public double sanityDelta() {
        return this.sanity;
    }

    /** The corruption delta applied on use (client-visible tooltip source). */
    public double corruptionDelta() {
        return this.corruption;
    }

    /**
     * Renders the stored sanity/corruption deltas as colour-coded, translatable tooltip lines.
     * Purely client-side presentation: no gameplay state is read or written here.
     */
    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context,
                                TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        appendDelta(builder, "tooltip.eldritch_horror.sanity", this.sanityDelta());
        appendDelta(builder, "tooltip.eldritch_horror.corruption", this.corruptionDelta());
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

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Server-authoritative: only the server may mutate sanity/corruption. The client just
        // plays the swing; returning SUCCESS here does not change state.
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        SanityAPI.add((ServerPlayer) player, this.sanity);
        CorruptionAPI.add((ServerPlayer) player, this.corruption);

        // Consume one unless the player has infinite materials (creative) or the item persists.
        if (this.consumed && !player.hasInfiniteMaterials()) {
            stack.shrink(1);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5F, 1.0F);

        return InteractionResult.SUCCESS;
    }
}
