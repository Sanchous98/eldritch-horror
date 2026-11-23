package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
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

    /**
     * Per-player corruption <b>floor</b>, 0..{@code CorruptionSystem.DEFAULT_MAX}: the meter can
     * never sit below it. Pinned at the Claimed band by the endgame Summon outcome (design/11:
     * "corruption Claimed becomes permanent") and cleared by Banish. Synced and copied on death like
     * the meter it guards; written only through {@code CorruptionSystem}.
     */
    public static final Supplier<AttachmentType<Double>> CORRUPTION_FLOOR =
            ATTACHMENT_TYPES.register("corruption_floor", () -> AttachmentType.<Double>builder(() -> 0.0)
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
     * Per-chunk ruin stage already applied by {@code CityRuins}, 0..3 (state ordinal). Lets a
     * loaded chunk be upgraded once per stage instead of rescanned on every visit. Persisted with
     * the chunk; never synced to clients.
     */
    public static final Supplier<AttachmentType<Integer>> RUIN_STAGE =
            ATTACHMENT_TYPES.register("ruin_stage", () -> AttachmentType.<Integer>builder(() -> 0)
                    .serialize(com.mojang.serialization.Codec.INT.fieldOf("stage"))
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

    /**
     * Per-player chosen investigator id ({@code ""} = none chosen), synced to the owner and copied
     * on death so the prologue choice survives a respawn. Written only through
     * {@link com.sanchous98.eldritchhorror.investigator.InvestigatorAPI}. See
     * {@code design/29-prologue.md}.
     */
    public static final Supplier<AttachmentType<String>> INVESTIGATOR =
            ATTACHMENT_TYPES.register("investigator",
                    () -> AttachmentType.<String>builder(() -> "")
                            .serialize(com.mojang.serialization.Codec.STRING.fieldOf("id"))
                            .sync((holder, to) -> holder == to, ByteBufCodecs.STRING_UTF8)
                            .copyOnDeath()
                            .build());

    /**
     * Whether the one-time Threshold prologue has been completed, synced to the owner and copied on
     * death. Set only by {@link com.sanchous98.eldritchhorror.investigator.Prologue#complete}. See
     * {@code design/29-prologue.md}.
     */
    public static final Supplier<AttachmentType<Boolean>> PROLOGUE_DONE =
            ATTACHMENT_TYPES.register("prologue_done",
                    () -> AttachmentType.<Boolean>builder(() -> false)
                            .serialize(com.mojang.serialization.Codec.BOOL.fieldOf("done"))
                            .sync((holder, to) -> holder == to, ByteBufCodecs.BOOL)
                            .copyOnDeath()
                            .build());

    /**
     * The curated-city id the player began in (the prologue gate they took), or {@code ""}. Set by
     * {@link com.sanchous98.eldritchhorror.investigator.Prologue#complete}; drives the respawn point
     * and {@code /eh home}. See {@code design/29-prologue.md}.
     */
    public static final Supplier<AttachmentType<String>> HOME_CITY =
            ATTACHMENT_TYPES.register("home_city",
                    () -> AttachmentType.<String>builder(() -> "")
                            .serialize(com.mojang.serialization.Codec.STRING.fieldOf("id"))
                            .sync((holder, to) -> holder == to, ByteBufCodecs.STRING_UTF8)
                            .copyOnDeath()
                            .build());

    /**
     * Per-player set of rift marker positions the player has discovered, encoded as {@code "x,y,z"}
     * strings. Persisted, copied on death, and synced to the owner so the world map can draw them
     * (design/22). Written only through
     * {@link com.sanchous98.eldritchhorror.event.RiftKnowledge}. Mirrors {@link #LORE}.
     */
    public static final Supplier<AttachmentType<Set<String>>> KNOWN_RIFTS =
            ATTACHMENT_TYPES.register("known_rifts",
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
     * Per-player mirror of the currently running world events, {@code id@elapsedSeconds} strings,
     * synced to the owner so the active-events journal can render without a custom packet
     * (design/19). Refreshed once a second by {@code EventTicker}; cleared on logout by the ticker.
     */
    public static final Supplier<AttachmentType<List<String>>> ACTIVE_EVENTS =
            ATTACHMENT_TYPES.register("active_events",
                    () -> AttachmentType.<List<String>>builder(() -> List.of())
                            .serialize(com.mojang.serialization.Codec.STRING.listOf()
                                    .fieldOf("events"))
                            .sync((holder, to) -> holder == to,
                                    ByteBufCodecs.collection(
                                            java.util.ArrayList<String>::new,
                                            ByteBufCodecs.STRING_UTF8)
                                            .map(List::copyOf, java.util.ArrayList::new))
                            .build());

    /**
     * Per-player map of curated-city id → discovered {@code CityState} ordinal, synced to the owner
     * so the world map can colour city markers by their condition (design/21, design/22). Written
     * only through {@link com.sanchous98.eldritchhorror.world.city.CityStateKnowledge}.
     */
    public static final Supplier<AttachmentType<Map<String, Integer>>> CITY_STATES =
            ATTACHMENT_TYPES.register("city_states",
                    () -> AttachmentType.<Map<String, Integer>>builder(Map::of)
                            .serialize(com.mojang.serialization.Codec
                                    .unboundedMap(com.mojang.serialization.Codec.STRING,
                                            com.mojang.serialization.Codec.INT)
                                    .fieldOf("states"))
                            .sync((holder, to) -> holder == to,
                                    ByteBufCodecs.<RegistryFriendlyByteBuf, String, Integer,
                                                    Map<String, Integer>>map(
                                                    java.util.HashMap::new,
                                                    ByteBufCodecs.STRING_UTF8,
                                                    ByteBufCodecs.VAR_INT))
                            .copyOnDeath()
                            .build());

    /**
     * Per-player quest progress: {@code questId -> count}, synced to the owner (design/13, quest
     * journal). A quest is complete when its count reaches its target (see
     * {@link com.sanchous98.eldritchhorror.quest.Quests}). Written only through
     * {@link com.sanchous98.eldritchhorror.quest.QuestJournal}.
     */
    public static final Supplier<AttachmentType<Map<String, Integer>>> QUESTS =
            ATTACHMENT_TYPES.register("quests",
                    () -> AttachmentType.<Map<String, Integer>>builder(Map::of)
                            .serialize(com.mojang.serialization.Codec
                                    .unboundedMap(com.mojang.serialization.Codec.STRING,
                                            com.mojang.serialization.Codec.INT)
                                    .fieldOf("progress"))
                            .sync((holder, to) -> holder == to,
                                    ByteBufCodecs.<RegistryFriendlyByteBuf, String, Integer,
                                                    Map<String, Integer>>map(
                                                    java.util.HashMap::new,
                                                    ByteBufCodecs.STRING_UTF8,
                                                    ByteBufCodecs.VAR_INT))
                            .copyOnDeath()
                            .build());

    /**
     * Per-player last completion of each repeatable quest, {@code questId -> epoch seconds}, synced
     * to the owner and copied on death (design/13, the repeatable quests of
     * {@code design/10-quests.md}). Written only through
     * {@link com.sanchous98.eldritchhorror.quest.QuestJournal}.
     *
     * <p>Stored as wall-clock epoch seconds rather than ticks so a cooldown keeps running while the
     * server is stopped. An absent entry means "never completed", which is also what an existing
     * save reads, so no old quest starts on cooldown. Synced because the journal resets a
     * repeatable's progress to 0 on completion; without the timestamp the entry would render as
     * ready while it is still cooling down (see {@code client.JournalScreen}).
     */
    /**
     * Per-player progress through each city's local contract (design/21): a flat map with
     * {@code step:<cityId>}, {@code kills:<cityId>} and {@code done:<cityId>} keys. Persisted,
     * copied on death and synced to the owner. Written only through
     * {@link com.sanchous98.eldritchhorror.world.city.quest.CityQuests}.
     */
    public static final Supplier<AttachmentType<Map<String, Integer>>> CITY_QUESTS =
            ATTACHMENT_TYPES.register("city_quests",
                    () -> AttachmentType.<Map<String, Integer>>builder(Map::of)
                            .serialize(com.mojang.serialization.Codec
                                    .unboundedMap(com.mojang.serialization.Codec.STRING,
                                            com.mojang.serialization.Codec.INT)
                                    .fieldOf("progress"))
                            .sync((holder, to) -> holder == to,
                                    ByteBufCodecs.<RegistryFriendlyByteBuf, String, Integer,
                                                    Map<String, Integer>>map(
                                                    java.util.HashMap::new,
                                                    ByteBufCodecs.STRING_UTF8,
                                                    ByteBufCodecs.VAR_INT))
                            .copyOnDeath()
                            .build());

    public static final Supplier<AttachmentType<Map<String, Long>>> QUEST_COMPLETIONS =
            ATTACHMENT_TYPES.register("quest_completions",
                    () -> AttachmentType.<Map<String, Long>>builder(Map::of)
                            .serialize(com.mojang.serialization.Codec
                                    .unboundedMap(com.mojang.serialization.Codec.STRING,
                                            com.mojang.serialization.Codec.LONG)
                                    .fieldOf("completed"))
                            .sync((holder, to) -> holder == to,
                                    ByteBufCodecs.<RegistryFriendlyByteBuf, String, Long,
                                                    Map<String, Long>>map(
                                                    java.util.HashMap::new,
                                                    ByteBufCodecs.STRING_UTF8,
                                                    ByteBufCodecs.VAR_LONG))
                            .copyOnDeath()
                            .build());

    /**
     * Per-player set of unlocked skill ids, synced to the owner (design/13, skill tree). Written only
     * through {@link com.sanchous98.eldritchhorror.skill.SkillTree}.
     */
    public static final Supplier<AttachmentType<Set<String>>> SKILLS =
            ATTACHMENT_TYPES.register("skills",
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
                                    .fieldOf("unlocked"))
                            .sync((holder, to) -> holder == to,
                                    ByteBufCodecs.collection(
                                            java.util.HashSet<String>::new, ByteBufCodecs.STRING_UTF8)
                                            .map(java.util.Set::copyOf, java.util.HashSet::new))
                            .copyOnDeath()
                            .build());

    /** Per-player unspent skill points, synced to the owner (design/13). */
    public static final Supplier<AttachmentType<Integer>> SKILL_POINTS =
            ATTACHMENT_TYPES.register("skill_points",
                    () -> AttachmentType.<Integer>builder(() -> 0)
                            .serialize(com.mojang.serialization.Codec.INT.fieldOf("points"))
                            .sync((holder, to) -> holder == to, ByteBufCodecs.VAR_INT)
                            .copyOnDeath()
                            .build());

    private ModAttachments() {
    }
}
