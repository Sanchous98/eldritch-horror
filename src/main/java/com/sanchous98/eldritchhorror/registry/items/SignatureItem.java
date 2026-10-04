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
 * The shared signature item every investigator carries. One item id dispatches to the owner's
 * {@link Investigator} active via {@link SignatureAbilities#activate}, so adding a new investigator
 * never requires a new item.
 *
 * <p>Stack size 1 (set at registration). Right-clicking is server-authoritative: the client path
 * only swings the arm ({@code InteractionResult.SUCCESS}); the server checks the chosen
 * investigator, fires the active and starts the 20-second ({@value #COOLDOWN_TICKS}-tick) cooldown.
 * The cooldown lives here, not in {@code SignatureAbilities}, so the ability method only applies
 * effects (see {@code docs/INVESTIGATORS-CONTRACT.md}).
 */
public class SignatureItem extends Item {

    /** Per-use cooldown; 400 ticks = 20 seconds at 20 TPS. */
    private static final int COOLDOWN_TICKS = 400;

    public SignatureItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        Investigator id = InvestigatorAPI.get(serverPlayer);
        if (id == null) {
            serverPlayer.sendSystemMessage(Component.translatableWithFallback(
                    "investigator.eldritch_horror.no_investigator",
                    "Choose an investigator first."));
            return InteractionResult.SUCCESS;
        }
        SignatureAbilities.activate(serverPlayer, id);
        // 26.3's ItemCooldowns only accepts an ItemStack (the contract's addCooldown(this, 400) is
        // not a valid overload); the held stack resolves to this item's cooldown group.
        serverPlayer.getCooldowns().addCooldown(serverPlayer.getItemInHand(hand), COOLDOWN_TICKS);
        return InteractionResult.SUCCESS;
    }
}
