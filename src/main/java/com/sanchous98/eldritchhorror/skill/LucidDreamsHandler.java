package com.sanchous98.eldritchhorror.skill;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.codex.CodexAPI;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.rite.RiteDefinition;
import com.sanchous98.eldritchhorror.rite.RiteKnowledge;
import com.sanchous98.eldritchhorror.rite.Rites;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

/**
 * {@link Skills#LUCID_DREAMS}: waking from a natural sleep can teach one random unknown rite of tier
 * ≤ 2 through {@link CodexAPI#learn}, so the codex and journal see it like any other discovery.
 *
 * <p>The chance is gated by {@link ModConfig#LUCID_DREAMS_CHANCE}. The endgame fork rites are never
 * dream-taught ({@link LucidDreamPool#EXCLUDED}), and a player who knows every eligible rite simply
 * dreams of nothing.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class LucidDreamsHandler {

    private LucidDreamsHandler() {
    }

    /** Rolls a dream-rite on a natural wake-up when the player has Lucid Dreams unlocked. */
    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        // wakeImmediately == true means a manual/forced wake; natural sleep-through passes false.
        if (event.wakeImmediately() || !SkillTree.has(player, Skills.LUCID_DREAMS)) {
            return;
        }
        double chance = ModConfig.LUCID_DREAMS_CHANCE.get();
        if (chance <= 0.0 || player.getRandom().nextDouble() >= chance) {
            return;
        }
        List<LucidDreamPool.Entry> entries = new ArrayList<>();
        for (RiteDefinition rite : Rites.all()) {
            entries.add(new LucidDreamPool.Entry(rite.id(), rite.tier()));
        }
        List<String> pool = LucidDreamPool.eligible(entries, RiteKnowledge.known(player));
        if (pool.isEmpty()) {
            return;
        }
        String id = pool.get(player.getRandom().nextInt(pool.size()));
        if (!CodexAPI.learn(player, id)) {
            return;
        }
        RiteDefinition learned = Rites.byId(id);
        Component name = learned == null ? Component.literal(id) : learned.name();
        player.sendSystemMessage(Component.translatable(
                "skill.eldritch_horror.lucid_dreams.learned", name));
    }
}
