package com.sanchous98.eldritchhorror;

import com.mojang.logging.LogUtils;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModAttachments;
import com.sanchous98.eldritchhorror.registry.ModAttributes;
import com.sanchous98.eldritchhorror.registry.ModBlocks;
import com.sanchous98.eldritchhorror.registry.ModEffects;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.registry.ModItems;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import com.sanchous98.eldritchhorror.registry.ModWorldGen;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import org.slf4j.Logger;

/**
 * Entry point for Eldritch Horror.
 *
 * <p>This class deliberately stays thin: it wires up {@code DeferredRegister}s and config and
 * nothing else. Gameplay lives in feature packages, one per RPG system:
 *
 * <ul>
 *   <li>{@code sanity} — the Sanity attribute and its effects (the core RPG meter).</li>
 *   <li>{@code corruption} — exposure/taint that grows from forbidden knowledge and rituals.</li>
 *   <li>{@code cult} — factions, reputation, and the NPCs that gate rituals.</li>
 *   <li>{@code ritual} — multi-block/altar ritual definitions and resolution.</li>
 *   <li>{@code content} — blocks, items, entities, effects, sounds.</li>
 *   <li>{@code client} — client-only setup (HUD, overlays, particles).</li>
 * </ul>
 *
 * See {@code docs/ARCHITECTURE.md} and {@code docs/DESIGN.md} for the intended shape.
 */
@Mod(EldritchHorror.MODID)
public final class EldritchHorror {
    /** The mod id. Must match {@code mod_id} in gradle.properties and neoforge.mods.toml. */
    public static final String MODID = "eldritch_horror";

    public static final Logger LOGGER = LogUtils.getLogger();

    /** Creates a namespaced {@link Identifier} under this mod's id. */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public EldritchHorror(IEventBus modEventBus, ModContainer modContainer) {
        // Content registries. Add new DeferredRegisters here (and in registry/).
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModItems.TABS.register(modEventBus);
        ModItems.registerCategories();
        ModBlocks.registerCategories();
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModAttributes.ATTRIBUTES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModEffects.MOB_EFFECTS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModWorldGen.CHUNK_GENERATORS.register(modEventBus);
        ModWorldGen.BIOME_SOURCES.register(modEventBus);

        // Config. Sanity/corruption knobs are gameplay rules, so they live in the SERVER config
        // (synced from server to clients) rather than COMMON — see design/27-systems-framework.md.
        modContainer.registerConfig(Type.SERVER, ModConfig.SPEC);

        // Gameplay event listeners (sanity ticking, corruption spread, ritual validation, ...)
        // should be registered here, e.g. NeoForge.EVENT_BUS.register(SomeListener.class).
    }
}
