package com.sanchous98.eldritchhorror.rite;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;
import java.util.Map;

/**
 * Wires tomes to the rite-knowledge system: reading (right-clicking) a tome teaches the rite id(s)
 * it grants, <b>in addition</b> to the sanity/corruption cost applied by
 * {@code ConsumableItem#use}.
 *
 * <p>Item→rite grants come from {@code design/16-items.md} (the four documented tomes) and the
 * per-entry comments in {@code registry/items/Tomes.java} for the remaining knowledge items, since
 * the Tomes table itself only enumerates the ten ids.
 *
 * <p>Server-authoritative: the client branch returns immediately. Registration is idempotent, so a
 * re-read never duplicates — but re-reading still pays the cost again via {@code ConsumableItem}.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class TomeRiteKnowledge {

    /** Tome item path → the rite ids it grants. A tome may grant two (e.g. {@code cult_litanies}). */
    private static final Map<String, List<String>> GRANTS = Map.ofEntries(
            Map.entry("tome_of_the_eye", List.of("ward_of_the_eye")),
            Map.entry("tome_of_tides", List.of("drowned_blessing")),
            Map.entry("hollow_text", List.of("call_the_lesser")),
            Map.entry("star_codex", List.of("summon_star_spawn")),
            Map.entry("codex_of_wards", List.of("close_rift")),
            Map.entry("bone_ledger", List.of("rite_of_cleansing")),
            Map.entry("atlas_of_the_veil", List.of("open_rift")),
            Map.entry("watchers_diary", List.of("respec")),
            Map.entry("cult_litanies", List.of("call_the_lesser", "open_rift")),
            Map.entry("fragment_page", List.of("ward_of_the_eye")));

    private TomeRiteKnowledge() {
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        // Fires once per hand: without this guard a tome held in the offhand pays its cost twice.
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        Identifier key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!EldritchHorror.MODID.equals(key.getNamespace())) {
            return;
        }
        List<String> rites = GRANTS.get(key.getPath());
        if (rites == null) {
            return;
        }
        for (String riteId : rites) {
            RiteKnowledge.add(player, riteId);
        }
    }
}
