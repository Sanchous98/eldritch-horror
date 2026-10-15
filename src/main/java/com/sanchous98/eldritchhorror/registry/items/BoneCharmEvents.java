package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Bone Charm (design/16): while a player carries one, the first <b>fatal</b> blow is drunk by the
 * dead. The hook is the player damage path, {@link LivingDamageEvent.Pre} — after armour and
 * enchantment reduction but before absorption — so a hit that would not kill is never eaten. The
 * charm shatters (one leaves the inventory) and the holder pays corruption +{@value #CORRUPTION}
 * and a sanity jolt −{@value #SANITY}; the message is localised (en/ru).
 *
 * <p>Player-only by construction: the event is only acted on for a {@link ServerPlayer}. A mob or a
 * bot is not protected.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class BoneCharmEvents {

    /** The charm's price, paid when it takes the blow. */
    private static final double CORRUPTION = 5.0;
    private static final double SANITY = 10.0;

    private BoneCharmEvents() {
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive()) {
            return;
        }
        // Absorption is applied after this event, so only damage at or past health + absorption is
        // truly fatal; anything less is a hit the player survives on their own.
        if (event.getNewDamage() < player.getHealth() + player.getAbsorptionAmount()) {
            return;
        }
        if (!consumeOne(player)) {
            return;
        }
        event.setNewDamage(0.0F);
        CorruptionAPI.add(player, CORRUPTION);
        SanityAPI.add(player, -SANITY);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 0.8F);
        player.sendSystemMessage(Component.translatableWithFallback(
                "death.eldritch_horror.bone_charm",
                "The bone charm splinters: the blow is drunk by the dead (corruption +%s, sanity −%s).",
                (int) CORRUPTION, (int) SANITY));
    }

    /** Removes one bone charm from {@code player}'s inventory; {@code false} when they carry none. */
    private static boolean consumeOne(ServerPlayer player) {
        Item charm = BuiltInRegistries.ITEM.getValue(EldritchHorror.id("bone_charm"));
        if (charm == Items.AIR) {
            return false;
        }
        for (ItemStack stack : player.getInventory()) {
            if (stack.getItem() == charm) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }
}
