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
            // Also teaches the Leviathan/Cthulhu sooth: the drowned-temple presence is answered, not fought.
            // Also the sealed pool (seal_abhoth) and the quenched green flame (quench_tulzscha).
            Map.entry("tome_of_tides", List.of("drowned_blessing", "soothe_cthulhu", "still_azathoth",
                    "still_glaaki", "sever_hydra", "seal_abhoth", "quench_tulzscha")),
            // Also teaches appease_yig and quench_cthugha: the ashen presences are answered by ritual.
            // Also the feaster (starve_chaugnar_faugn): the hunger is answered by feeding it nothing.
            Map.entry("hollow_text", List.of("call_the_lesser", "appease_yig", "quench_cthugha",
                    "starve_chaugnar_faugn")),
            // Also teaches the Shub-Niggurath stilling, the gate, and the toppling/barring rites.
            Map.entry("star_codex", List.of("summon_star_spawn", "still_shub_niggurath", "seal_the_gate",
                    "topple_idol", "bar_nyogtha")),
            // Also teaches the binding of Atlach-Nacha and the denial of the trusted face.
            // Also the unspoken name (silence_hastur) and the forgotten name (erase_zstylzhemghi).
            Map.entry("codex_of_wards", List.of("close_rift", "bind_atlach_nacha", "deny_nyarlathotep",
                    "silence_hastur", "erase_zstylzhemghi")),
            // Also teaches the Dunwich Horror's drawing-away; a cleansing text answers both blights.
            // Also the remembering king (unmake_nephren_ka): a ledger of names unmakes one.
            Map.entry("bone_ledger", List.of("rite_of_cleansing", "draw_away_dunwich", "unmake_nephren_ka")),
            Map.entry("atlas_of_the_veil", List.of("open_rift")),
            // Also teaches ward_ithaqua: the walking wind is turned aside, not fought.
            Map.entry("watchers_diary", List.of("respec", "ward_ithaqua")),
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
        if (key == null) {
            return;
        }
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
