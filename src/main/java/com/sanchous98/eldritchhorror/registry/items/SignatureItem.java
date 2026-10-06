package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.investigator.Investigator;
import com.sanchous98.eldritchhorror.investigator.InvestigatorAPI;
import com.sanchous98.eldritchhorror.investigator.SignatureAbilities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * A per-investigator signature item. Each of the twelve is bound to its {@link Investigator} and
 * dispatches to {@link SignatureAbilities#activate} for that owner, so the active is identified by
 * the item's own identity rather than a shared id checked against the holder.
 *
 * <p>Stack size 1 (set at registration). Right-clicking is server-authoritative: the client path
 * only swings the arm ({@code InteractionResult.SUCCESS}); the server fires the active and starts
 * the 20-second ({@value #COOLDOWN_TICKS}-tick) cooldown. The cooldown lives here, not in
 * {@code SignatureAbilities}, so the ability method only applies effects
 * (see {@code docs/INVESTIGATORS-CONTRACT.md}).
 */
public class SignatureItem extends Item {

    /** Per-use cooldown; 400 ticks = 20 seconds at 20 TPS. */
    private static final int COOLDOWN_TICKS = 400;

    /** The investigator whose active this item fires. */
    private final Investigator investigator;

    public SignatureItem(Properties properties, Investigator investigator) {
        super(properties);
        this.investigator = investigator;
    }

    /** @return the investigator this item belongs to. */
    public Investigator investigator() {
        return this.investigator;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        Investigator chosen = InvestigatorAPI.get(serverPlayer);
        if (chosen == null) {
            serverPlayer.sendSystemMessage(Component.translatableWithFallback(
                    "investigator.eldritch_horror.no_investigator",
                    "Choose an investigator first."));
            return InteractionResult.SUCCESS;
        }
        // Fire the item owner's active; using someone else's signature item does nothing.
        if (chosen == this.investigator) {
            SignatureAbilities.activate(serverPlayer, this.investigator);
            // 26.3's ItemCooldowns only accepts an ItemStack (the contract's addCooldown(this, 400)
            // is not a valid overload); the held stack resolves to this item's cooldown group.
            serverPlayer.getCooldowns().addCooldown(serverPlayer.getItemInHand(hand), COOLDOWN_TICKS);
        }
        return InteractionResult.SUCCESS;
    }
}
