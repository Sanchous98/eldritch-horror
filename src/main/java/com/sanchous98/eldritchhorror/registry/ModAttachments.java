package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * NeoForge attachment registry: the server-authoritative per-entity / per-chunk state the RPG
 * systems live on.
 *
 * <p>This milestone registers the two baseline meters as plain values that go up and down, persist
 * and copy on death, but which <b>do not yet affect gameplay</b> (see
 * {@code design/27-systems-framework.md}). Sanity and corruption are the stub layer that items,
 * rites and effects will later read and write through {@code SanityAPI}/{@code CorruptionAPI}.
 */
public final class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, EldritchHorror.MODID);

    /** Per-player sanity, 0..{@code SanitySystem.DEFAULT_MAX}. Synced to the owner for the HUD. */
    public static final Supplier<AttachmentType<Double>> SANITY =
            ATTACHMENT_TYPES.register("sanity", () -> AttachmentType.<Double>builder(() -> 100.0)
                    .serialize(com.mojang.serialization.Codec.DOUBLE.fieldOf("value"))
                    .sync(ByteBufCodecs.DOUBLE)
                    .copyOnDeath()
                    .build());

    /** Per-player corruption, 0..{@code CorruptionSystem.DEFAULT_MAX}. Synced to the owner. */
    public static final Supplier<AttachmentType<Double>> CORRUPTION =
            ATTACHMENT_TYPES.register("corruption", () -> AttachmentType.<Double>builder(() -> 0.0)
                    .serialize(com.mojang.serialization.Codec.DOUBLE.fieldOf("value"))
                    .sync(ByteBufCodecs.DOUBLE)
                    .copyOnDeath()
                    .build());

    /** Per-chunk taint field, 0..1, shared by all players. Attached to {@code LevelChunk}. */
    public static final Supplier<AttachmentType<Double>> TAINT =
            ATTACHMENT_TYPES.register("taint", () -> AttachmentType.<Double>builder(() -> 0.0)
                    .serialize(com.mojang.serialization.Codec.DOUBLE.fieldOf("value"))
                    .build());

    private ModAttachments() {
    }
}
