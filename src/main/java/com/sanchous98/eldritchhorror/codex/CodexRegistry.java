package com.sanchous98.eldritchhorror.codex;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.AncientOne;
import com.sanchous98.eldritchhorror.event.EldritchEvent;
import com.sanchous98.eldritchhorror.event.Events;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.rite.RiteDefinition;
import com.sanchous98.eldritchhorror.rite.Rites;
import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Locations;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

/**
 * The codex entry registry: id → {@link CodexEntry}, built once (lazily, then cached) from the
 * registries that already own the content, so an entry is never a hand-written Java class where a
 * registry exists.
 *
 * <ul>
 *   <li>{@link CodexCategory#SITE} / {@link CodexCategory#CITY} — every fixed {@link Location} in
 *       {@link Locations}. The id is the location id ({@code eldritch_horror:site/…} /
 *       {@code eldritch_horror:city/…}); the name is the location's short tail, prettified.</li>
 *   <li>{@link CodexCategory#BESTIARY} — the non-Ancient-One entity types in
 *       {@link ModEntities#ENTITY_TYPES}, named by their vanilla {@code entity.<mod>.<id>} key.</li>
 *   <li>{@link CodexCategory#BOSS} — the {@link AncientOne} types of {@link ModEntities}, same
 *       entity-name key but grouped as bosses.</li>
 *   <li>{@link CodexCategory#RITE} — every {@link RiteDefinition} in {@link Rites}, carrying the
 *       rite's own name. Rite knowledge is <b>derived</b>, never stored (see {@link CodexAPI}).</li>
 *   <li>{@link CodexCategory#EVENT} — every {@link EldritchEvent} in {@link Events}, named by its
 *       {@code event.<mod>.<id>} key.</li>
 * </ul>
 *
 * <p>Server-side and deterministic: the registry is a fixed list built from insertion-ordered
 * registries, so iteration order never depends on {@code Math.random} or hashing.
 */
public final class CodexRegistry {

    private static volatile Map<String, CodexEntry> entries;

    private CodexRegistry() {
    }

    /** The entry for {@code id}, or {@code null} if unknown. */
    public static CodexEntry byId(String id) {
        return all().get(id);
    }

    /** Every entry, keyed by id, in a deterministic registry order. */
    public static Map<String, CodexEntry> all() {
        Map<String, CodexEntry> local = entries;
        if (local == null) {
            synchronized (CodexRegistry.class) {
                local = entries;
                if (local == null) {
                    local = build();
                    entries = local;
                }
            }
        }
        return local;
    }

    /** All entry ids of one category, in registry order. */
    public static List<String> idsIn(CodexCategory category) {
        List<String> out = new ArrayList<>();
        for (CodexEntry entry : all().values()) {
            if (entry.category() == category) {
                out.add(entry.id());
            }
        }
        return out;
    }

    private static Map<String, CodexEntry> build() {
        Map<String, CodexEntry> out = new LinkedHashMap<>();
        // Sites and cities: one entry per fixed location. A city's display name is its real name; a
        // site's is its prettified type tail. The id is always the location id (namespaced).
        for (Location loc : Locations.all()) {
            if (loc instanceof CityLocation city) {
                out.put(loc.id(), new CodexEntry(loc.id(), CodexCategory.CITY,
                        Component.literal(city.city().name())));
            } else {
                out.put(loc.id(), new CodexEntry(loc.id(), CodexCategory.SITE, locationName(loc.id())));
            }
        }
        // Bestiary and bosses: split our entities by their base class. The id is the entity path (the
        // same key the localisation already uses); a null id is skipped rather than invented.
        for (var holder : ModEntities.ENTITY_TYPES.getEntries()) {
            EntityType<?> type = holder.get();
            Identifier key = holder.getId();
            if (key == null || key.getPath().isEmpty()) {
                continue;
            }
            String path = key.getPath();
            boolean boss = ModEntities.ANCIENT_ONES.contains(type);
            CodexCategory category = boss ? CodexCategory.BOSS : CodexCategory.BESTIARY;
            out.put(path, new CodexEntry(path, category, entityName(path)));
        }
        // Rites: the entry id is the rite id, the name is the rite's own display name.
        for (RiteDefinition rite : Rites.all()) {
            out.put(rite.id(), new CodexEntry(rite.id(), CodexCategory.RITE, rite.name()));
        }
        // Events: named by their own id.
        for (EldritchEvent event : Events.all()) {
            out.put(event.id(), new CodexEntry(event.id(), CodexCategory.EVENT, eventName(event.id())));
        }
        // Preserve insertion order (Map.copyOf does not guarantee it), so display order is stable.
        return java.util.Collections.unmodifiableMap(out);
    }

    /**
     * The display name for a location id: its last path segment, prettified. A site id carries a
     * snake_case type ({@code site/cult_stronghold} → {@code Cult Stronghold}).
     */
    private static Component locationName(String id) {
        String tail = id;
        int slash = id.lastIndexOf('/');
        if (slash >= 0) {
            tail = id.substring(slash + 1);
        }
        return CodexEntry.nameOf(tail, prettify(tail));
    }

    /** The vanilla entity display name, keyed by the entity's own id. */
    private static Component entityName(String path) {
        return Component.translatable("entity." + EldritchHorror.MODID + "." + path);
    }

    private static Component eventName(String id) {
        return CodexEntry.nameOf(id, prettify(id));
    }

    /** {@code cult_stronghold} → {@code Cult Stronghold}. */
    private static String prettify(String id) {
        StringBuilder sb = new StringBuilder(id.length());
        boolean upper = true;
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            if (c == '_' || c == '/') {
                sb.append(' ');
                upper = true;
            } else if (upper) {
                sb.append(Character.toUpperCase(c));
                upper = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
