package com.sanchous98.eldritchhorror.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client → server: spend skill points on a node (design/13, skill tree). The skill screen sends one
 * of these when a known, not-yet-unlocked node is selected; the server answers through
 * {@code network/ModPayloads} by unlocking it via {@code SkillTree.unlock} and reporting the result
 * in chat. The server re-validates the id and the points, so a stale screen cannot cheat.
 *
 * @param skillId the id the client wants to unlock (validated server-side)
 */
public record SkillUnlockPayload(String skillId) implements CustomPacketPayload {

    /** The payload id used by the registrar; namespaced by the mod id. */
    public static final CustomPacketPayload.Type<SkillUnlockPayload> TYPE =
            CustomPacketPayload.createType("eldritch_horror:skill_unlock");

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillUnlockPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, SkillUnlockPayload::skillId,
                    SkillUnlockPayload::new);

    @Override
    public CustomPacketPayload.Type<SkillUnlockPayload> type() {
        return TYPE;
    }
}
