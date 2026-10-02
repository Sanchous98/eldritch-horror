package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Map;
import java.util.Set;
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

    /**
     * Per-player reputation with each cult: a map of cult id → value (−100…+100), persisted,
     * copied on death, and synced to the owner so a future cult screen can read it.
     * See {@code design/09-cults.md}.
     */
    public static final Supplier<AttachmentType<Map<String, Integer>>> REPUTATION =
            ATTACHMENT_TYPES.register("reputation",
                    () -> AttachmentType.<Map<String, Integer>>builder(Map::of)
                            .serialize(com.mojang.serialization.Codec
                                    .unboundedMap(com.mojang.serialization.Codec.STRING,
                                            com.mojang.serialization.Codec.INT)
                                    .fieldOf("rep"))
                            .sync((holder, to) -> holder == to,
                                    ByteBufCodecs.<RegistryFriendlyByteBuf, String, Integer,
                                                    Map<String, Integer>>map(
                                                    java.util.HashMap::new,
                                                    ByteBufCodecs.STRING_UTF8,
                                                    ByteBufCodecs.VAR_INT))
                            .copyOnDeath()
                            .build());

    /**
     * Per-player set of known rite ids, persisted, copied on death, and synced to the owner so a
     * future rite/codex screen can read it. Mirrors the synced-map {@code REPUTATION} pattern using
     * a set codec: {@code Set.copyOf} is an immutable snapshot, so callers add through
     * {@link com.sanchous98.eldritchhorror.rite.RiteKnowledge}. See {@code design/26-rituals-and-occult.md}.
     */
    public static final Supplier<AttachmentType<Set<String>>> RITE_KNOWLEDGE =
            ATTACHMENT_TYPES.register("rite_knowledge",
                    () -> AttachmentType.<Set<String>>builder(() -> Set.of())
                            .serialize(com.mojang.serialization.Codec
                                    .unboundedMap(com.mojang.serialization.Codec.STRING,
                                            com.mojang.serialization.Codec.BOOL)
                                    .xmap(set -> Set.copyOf(set.keySet()), key -> {
                                        java.util.Map<String, Boolean> map = new java.util.HashMap<>();
                                        for (String k : key) {
                                            map.put(k, Boolean.TRUE);
                                        }
                                        return map;
                                    })
                                    .fieldOf("known"))
                            .sync((holder, to) -> holder == to,
                                    ByteBufCodecs.collection(
                                            java.util.HashSet<String>::new, ByteBufCodecs.STRING_UTF8)
                                            .map(java.util.Set::copyOf, java.util.HashSet::new))
                            .copyOnDeath()
                            .build());

    /**
     * Per-player set of discovered codex entry ids, persisted, copied on death, and synced to the
     * owner so a future codex screen can read it. Mirrors {@link #RITE_KNOWLEDGE} exactly: the value
     * is an immutable {@link Set#copyOf} snapshot, so content adds through
     * {@link com.sanchous98.eldritchhorror.codex.CodexAPI} (read-modify-write). Rite entries are
     * <b>derived</b> from {@code RITE_KNOWLEDGE} rather than stored here, so learning a rite is not
     * duplicated. See {@code design/22-map-and-knowledge.md}.
     */
    public static final Supplier<AttachmentType<Set<String>>> LORE =
            ATTACHMENT_TYPES.register("lore",
                    () -> AttachmentType.<Set<String>>builder(() -> Set.of())
                            .serialize(com.mojang.serialization.Codec
                                    .unboundedMap(com.mojang.serialization.Codec.STRING,
                                            com.mojang.serialization.Codec.BOOL)
                                    .xmap(set -> Set.copyOf(set.keySet()), key -> {
                                        java.util.Map<String, Boolean> map = new java.util.HashMap<>();
                                        for (String k : key) {
                                            map.put(k, Boolean.TRUE);
                                        }
                                        return map;
                                    })
                                    .fieldOf("known"))
                            .sync((holder, to) -> holder == to,
                                    ByteBufCodecs.collection(
                                            java.util.HashSet<String>::new, ByteBufCodecs.STRING_UTF8)
                                            .map(java.util.Set::copyOf, java.util.HashSet::new))
                            .copyOnDeath()
                            .build());

    private ModAttachments() {
    }
}
