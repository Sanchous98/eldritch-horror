package com.sanchous98.eldritchhorror.quest;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

/**
 * Event-driven advances for the repeatable side quests (design/13, the "Side / repeatable quests"
 * table of {@code design/10-quests.md}). Ritual-triggered quests stay next to their rite in the
 * rite engine; the two triggers whose source is an ordinary game event live here.
 *
 * <p>Everything goes through {@link QuestJournal#add}, so the {@code requires} gate, the repeatable
 * cooldown and the reward/restart path are the same as for any other quest. Both listeners are
 * cheap and early-out: this fires for every death and every pickup on the server.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class QuestTriggers {

    /** The star-spawn family (the weapon task's tag), credited by {@code star_hunt}. */
    private static final TagKey<EntityType<?>> STAR_SPAWN =
            TagKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("star_spawn"));

    /** The Choir's "pearls" accepted by {@code tide_offering}. */
    private static final ResourceKey<Item> PEARL =
            ResourceKey.create(Registries.ITEM, EldritchHorror.id("pearl_reagent"));

    private QuestTriggers() {
    }

    /**
     * {@code star_hunt}: one credit per kill whose damage source is a player and whose victim is in
     * the {@code eldritch_horror:star_spawn} entity-type tag. Same attribution rule as cult kills, so
     * an environmental death is never credited.
     */
    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!event.getEntity().getType().builtInRegistryHolder().is(STAR_SPAWN)) {
            return;
        }
        QuestJournal.add(player, "star_hunt", 1);
    }

    /**
     * {@code tide_offering}: the number of pearls actually added to the player's inventory. The
     * {@code Post} event carries the stack from before the pickup, so the difference against what
     * is still on the ground is what the player received (a full inventory picks up nothing).
     */
    @SubscribeEvent
    public static void onPickup(ItemEntityPickupEvent.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack original = event.getOriginalStack();
        if (!original.getItem().builtInRegistryHolder().is(PEARL)) {
            return;
        }
        int picked = Math.max(0, original.getCount() - event.getCurrentStack().getCount());
        if (picked > 0) {
            QuestJournal.add(player, "tide_offering", picked);
        }
    }
}
