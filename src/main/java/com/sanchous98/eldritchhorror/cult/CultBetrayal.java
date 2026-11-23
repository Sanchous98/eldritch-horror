package com.sanchous98.eldritchhorror.cult;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.entity.AncientOne;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Betrayal and redemption for initiated cultists who attack an Ancient One.
 *
 * <p>Striking a presence is an act against the patron, and the world reacts in ways the player can
 * see rather than a hidden resistance:
 *
 * <ul>
 *   <li><b>Standing</b> — every cult the player is initiated in loses reputation (the pact frays).</li>
 *   <li><b>Severed</b> — for a while, rites fail with a message: the patron's whisper is drowned by
 *       the presence. The cultist must fight with mundane means, which is the real difficulty.</li>
 *   <li><b>Attention</b> — the Ancient One switches its target to the traitor.</li>
 *   <li><b>Redemption</b> — corruption drops: fighting the dark is a way back for an occultist.</li>
 * </ul>
 *
 * <p>Rate-limited per player so a long fight pays the price once, not every hit.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class CultBetrayal {

    private static final Map<UUID, Long> LAST = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> SEVERED = new ConcurrentHashMap<>();

    private CultBetrayal() {
    }

    /** @return whether the player's patron is currently severed (rites fail). */
    public static boolean isSevered(ServerPlayer player) {
        Long until = SEVERED.get(player.getUUID());
        return until != null && player.level().getGameTime() < until;
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!ModConfig.ENABLE_CULT_BETRAYAL.get()
                || !(event.getEntity() instanceof AncientOne boss)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!CultSystem.isInitiated(player)) {
            return;
        }
        // The presence notices a traitor every time it is struck, not only on the first blow.
        if (boss instanceof Mob mob) {
            mob.setTarget(player);
        }
        long now = player.level().getGameTime();
        Long last = LAST.get(player.getUUID());
        if (last != null && now - last < ModConfig.CULT_BETRAYAL_COOLDOWN.get()) {
            return;
        }
        LAST.put(player.getUUID(), now);
        int loss = ModConfig.CULT_BETRAYAL_REPUTATION.get();
        for (CultDefinition cult : Cults.all()) {
            if (CultSystem.rankOf(player, cult.id()).atLeast(CultRank.INITIATE)) {
                CultSystem.add(player, cult.id(), -loss);
            }
        }
        double relief = ModConfig.CULT_BETRAYAL_REDEMPTION_CORRUPTION.get();
        CorruptionAPI.add(player, -relief);
        SEVERED.put(player.getUUID(), now + ModConfig.CULT_BETRAYAL_SEVERED_TICKS.get());
        player.sendSystemMessage(Component.translatable(
                "cult.betrayal.eldritch_horror", loss, relief));
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        LAST.remove(id);
        SEVERED.remove(id);
    }
}
