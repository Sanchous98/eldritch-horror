package com.sanchous98.eldritchhorror.network;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.skill.Skill;
import com.sanchous98.eldritchhorror.skill.SkillTree;
import com.sanchous98.eldritchhorror.skill.Skills;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Payload registration for the mod's client ↔ server traffic. All of it is registered here, in
 * common code, so both sides know the types before a connection is accepted.
 *
 * <ul>
 *   <li>{@link QuestSyncPayload} — server → client. Registered <b>without</b> a handler on purpose:
 *       the client handler lives in {@code client/ClientPayloadHandlers} and is attached through
 *       NeoForge's client-only {@code RegisterClientPayloadHandlersEvent}, so the dedicated server
 *       never resolves a client class.</li>
 *   <li>{@link SkillUnlockPayload} — client → server, handled here by re-validating through
 *       {@link SkillTree#unlock}, which is common (and server-authoritative) code.</li>
 * </ul>
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class ModPayloads {

    /** Channel version; both sides must agree (the NeoForge handshake compares it). */
    private static final String VERSION = "1";

    private ModPayloads() {
    }

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToClient(QuestSyncPayload.TYPE, QuestSyncPayload.STREAM_CODEC);
        registrar.playToServer(SkillUnlockPayload.TYPE, SkillUnlockPayload.STREAM_CODEC,
                ModPayloads::onSkillUnlock);
    }

    /**
     * Unlocks the requested node for its sender. A stale or forged id, an already-unlocked node and
     * a short point total all fall through {@link SkillTree#unlock} and are reported to the player;
     * a sender that has since gone (or is not a player) is ignored rather than answered.
     */
    private static void onSkillUnlock(SkillUnlockPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        boolean unlocked = SkillTree.unlock(player, payload.skillId());
        Skill skill = Skills.byId(payload.skillId());
        Component name = skill == null ? Component.literal(payload.skillId()) : skill.name();
        player.sendSystemMessage(Component.translatable(
                unlocked ? "skill.eldritch_horror.unlocked" : "skill.eldritch_horror.failed", name));
    }
}
