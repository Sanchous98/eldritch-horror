package com.sanchous98.eldritchhorror.quest;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
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
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Event-driven advances for quests that watch ordinary game events (design/13, the "Side /
 * repeatable quests" table of {@code design/10-quests.md}), plus the investigator field-kit tasks
 * of {@code design/14-classes.md}.
 *
 * <p>Data-driven quests declare a {@code trigger}/{@code param} pair; {@link #advance} matches the
 * loaded definitions and routes every hit through {@link QuestJournal#add}, so the prerequisite
 * gate, the investigator gate, the repeatable cooldown and rewards stay in one place. Ritual and
 * codex discoveries call the two small entry points below from the systems that own them.
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
     * Advances every loaded quest that watches {@code trigger} and accepts {@code param}. Cheap:
     * the registry is small and fully loaded, and each call ends in {@link QuestJournal#add}.
     */
    public static void advance(ServerPlayer player, String trigger, String param, int amount) {
        if (amount <= 0) {
            return;
        }
        for (Quest quest : Quests.all()) {
            if (quest.watches(trigger) && quest.matchesParam(param)) {
                QuestJournal.add(player, quest.id(), amount);
            }
        }
    }

    /** A codex entry was newly discovered (a site/boss/event, not a rite learned from a book). */
    public static void onDiscovery(ServerPlayer player, String entryId) {
        advance(player, "codex", entryId, 1);
    }

    /** {@code riteId} took effect at an altar. */
    public static void onRite(ServerPlayer player, String riteId) {
        advance(player, "rite", riteId, 1);
    }

    /**
     * {@code star_hunt}: one credit per kill whose damage source is a player and whose victim is in
     * the {@code eldritch_horror:star_spawn} entity-type tag. Same attribution rule as cult kills, so
     * an environmental death is never credited. Also feeds the generic {@code kill} trigger.
     */
    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        EntityType<?> type = event.getEntity().getType();
        if (type.builtInRegistryHolder().is(STAR_SPAWN)) {
            QuestJournal.add(player, "star_hunt", 1);
        }
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (key != null) {
            advance(player, "kill", key.toString(), 1);
        }
    }

    /**
     * {@code tide_offering}: the number of pearls actually added to the player's inventory. The
     * {@code Post} event carries the stack from before the pickup, so the difference against what
     * is still on the ground is what the player received (a full inventory picks up nothing). The
     * same delta feeds every generic {@code pickup} quest for that item.
     */
    @SubscribeEvent
    public static void onPickup(ItemEntityPickupEvent.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack original = event.getOriginalStack();
        int picked = Math.max(0, original.getCount() - event.getCurrentStack().getCount());
        if (picked <= 0) {
            return;
        }
        if (original.getItem().builtInRegistryHolder().is(PEARL)) {
            QuestJournal.add(player, "tide_offering", picked);
        }
        Identifier key = BuiltInRegistries.ITEM.getKey(original.getItem());
        if (key != null) {
            advance(player, "pickup", key.toString(), picked);
        }
    }

    /** Generic {@code craft} trigger: the id and count of the stack the crafting grid produced. */
    @SubscribeEvent
    public static void onCraft(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack crafted = event.getCrafting();
        if (crafted.isEmpty()) {
            return;
        }
        Identifier key = BuiltInRegistries.ITEM.getKey(crafted.getItem());
        if (key != null) {
            advance(player, "craft", key.toString(), crafted.getCount());
        }
    }
}
