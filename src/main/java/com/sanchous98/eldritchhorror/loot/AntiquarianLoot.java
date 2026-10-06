package com.sanchous98.eldritchhorror.loot;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.investigator.Investigator;
import com.sanchous98.eldritchhorror.investigator.InvestigatorAPI;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * Aldous Pemberton's passive: the Antiquarian finds better loot. When <b>he</b> kills a living
 * entity, a chance roll adds one bonus item drawn from a small curated pool on top of the normal
 * drop. Server-side and bounded: a single extra stack, a fixed chance, and only for the matching
 * investigator (a different player killing the same mob is unaffected).
 *
 * <p>Scope note: chest/site loot is not yet a shipped system, so this improves the loot that does
 * exist (mob drops). When site chests land, the same pool can extend their tables.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class AntiquarianLoot {

    /** Chance an eligible kill yields one extra item. */
    private static final float BONUS_CHANCE = 0.35f;

    /** Equal-weight bonus pool, by registry path (relic_coin weighted 3× as the common result). */
    private static final String[] POOL = {
            "relic_coin", "relic_coin", "relic_coin",
            "silver_reagent", "star_reagent",
            "mark_of_favour", "barter_seal", "order_scrip",
    };

    private AntiquarianLoot() {
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getLastHurtByPlayer();
        if (!(killer instanceof ServerPlayer player)
                || InvestigatorAPI.get(player) != Investigator.ALDOUS_PEMBERTON) {
            return;
        }
        if (player.getRandom().nextFloat() >= BONUS_CHANCE) {
            return;
        }
        String itemId = POOL[player.getRandom().nextInt(POOL.length)];
        Item item = BuiltInRegistries.ITEM.getValue(EldritchHorror.id(itemId));
        if (item == net.minecraft.world.item.Items.AIR) {
            return; // unregistered pool id (never expected); this registry is defaulted, not null
        }
        ItemEntity drop = new ItemEntity(victim.level(), victim.getX(), victim.getY() + 0.5,
                victim.getZ(), new ItemStack(item));
        event.getDrops().add(drop);
    }
}
