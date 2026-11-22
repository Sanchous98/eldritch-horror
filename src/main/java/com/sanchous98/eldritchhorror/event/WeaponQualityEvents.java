package com.sanchous98.eldritchhorror.event;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.registry.items.Utility;
import com.sanchous98.eldritchhorror.registry.items.WeaponQuality;
import com.sanchous98.eldritchhorror.registry.items.WeaponQualityStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Tempering ({@code design/16-items.md}): a weapon in the main hand, a whetstone in the off hand,
 * right-click. One attempt consumes the whetstone and rolls the quality ladder in
 * {@link WeaponQuality}: the first two steps are guaranteed, then an attempt can improve, drop one
 * step, or shatter the weapon.
 *
 * <p>The off-hand whetstone keeps the weapon in the hand the player is actually using, and the
 * event fires before the vanilla item use, so the interaction is unambiguous. Only the off hand is
 * consulted: a whetstone elsewhere in the inventory never changes what a right-click does, so
 * carrying one no longer disables a gun, bow or throwable in the main hand. A weapon already at max
 * quality also keeps its normal right-click — the gamble is only taken when there is a step to gain.
 * Server-authoritative: the roll uses the player's own {@code RandomSource}, and the client sees
 * the component change.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class WeaponQualityEvents {

    private WeaponQualityEvents() {
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        ItemStack weapon = player.getMainHandItem();
        if (!WeaponQualityStacks.isGradedWeapon(weapon)) {
            return;
        }
        ItemStack stone = player.getOffhandItem();
        if (!stone.is(Utility.WHETSTONE.get())) {
            return;
        }
        int quality = WeaponQualityStacks.quality(weapon);
        if (WeaponQuality.isMax(quality)) {
            return; // nothing to gain: let the weapon use itself
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (!player.getAbilities().instabuild) {
            stone.shrink(1);
        }
        WeaponQuality.Outcome outcome =
                WeaponQuality.roll(quality, player.getRandom().nextInt(100));
        switch (outcome) {
            case UPGRADE -> {
                WeaponQualityStacks.apply(weapon, quality + 1);
                play(player, SoundEvents.ANVIL_USE, 1.15F);
                player.sendSystemMessage(Component.translatable("quality.eldritch_horror.success",
                        weapon.getHoverName(),
                        Component.translatable(WeaponQuality.nameKey(quality + 1))));
            }
            case DOWNGRADE -> {
                WeaponQualityStacks.apply(weapon, quality - 1);
                play(player, SoundEvents.ANVIL_LAND, 0.8F);
                player.sendSystemMessage(Component.translatable("quality.eldritch_horror.downgrade",
                        weapon.getHoverName(),
                        Component.translatable(WeaponQuality.nameKey(quality - 1))));
            }
            case BREAK -> {
                play(player, SoundEvents.ITEM_BREAK.value(), 0.9F);
                player.sendSystemMessage(Component.translatable("quality.eldritch_horror.broken",
                        weapon.getHoverName()));
                weapon.shrink(1);
                if (weapon.isEmpty()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                }
            }
        }
    }

    private static void play(ServerPlayer player, SoundEvent sound, float pitch) {
        player.level().playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS,
                0.9F, pitch);
    }
}
