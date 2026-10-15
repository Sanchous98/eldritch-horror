package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.corruption.TaintAPI;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Veil Lens (design/16). <b>Use:</b> a server-side readout on the action bar — the local taint
 * field ({@link TaintAPI#getAt}) and the holder's corruption ({@link CorruptionAPI#get}).
 * <b>"Prolonged use drains sanity"</b> is a short sustained drain started by the use:
 * {@value #DRAIN_SECONDS} seconds at −{@value #DRAIN_PER_SECOND}/s, applied from the lens's own tick,
 * so it only runs while the lens is carried (dropping it interrupts the drain).
 *
 * <p>client-only (design/16): "see corruption fields and Veil seams" is a client renderer; this
 * item reports the same numbers in text.
 */
public final class VeilLensItem extends Item {

    /** The sustained drain after each look. */
    private static final double DRAIN_PER_SECOND = 0.25;
    private static final int DRAIN_SECONDS = 8;
    /** Remaining drain per player, counted in ticks; in-memory, dropped when spent. */
    private static final Map<UUID, Integer> DRAIN = new ConcurrentHashMap<>();

    public VeilLensItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal(String.format(Locale.ROOT,
                    "The veil lens reads: local taint %.2f, corruption %.1f.",
                    TaintAPI.getAt(serverPlayer), CorruptionAPI.get(serverPlayer))), true);
            DRAIN.put(serverPlayer.getUUID(), DRAIN_SECONDS * 20);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner,
                              @Nullable EquipmentSlot slot) {
        if (owner instanceof ServerPlayer player && level.getGameTime() % 20 == 0) {
            UUID id = player.getUUID();
            Integer left = DRAIN.get(id);
            if (left != null) {
                SanityAPI.add(player, -DRAIN_PER_SECOND);
                if (left > 20) {
                    DRAIN.put(id, left - 20);
                } else {
                    DRAIN.remove(id); // the last pulse ends the drain
                }
            }
        }
    }
}
