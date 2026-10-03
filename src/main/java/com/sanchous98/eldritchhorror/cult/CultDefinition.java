package com.sanchous98.eldritchhorror.cult;

import net.minecraft.network.chat.Component;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * A data-driven cult (faction): identity, domain, an ordered rank ladder, services, demands,
 * taboos, its signature rite, and the cults it opposes.
 *
 * <p>Definitions are plain data (a Java registry for now; a datapack later) so cultists, quests
 * and services — all deferred — can be driven from the same source. See {@code design/06-factions.md}
 * and {@code design/09-cults.md}.
 *
 * @param id           stable snake_case id (also the reputation key)
 * @param name         display name
 * @param domain       short flavour of where the cult operates
 * @param ranks        translatable rank names, low → high (the ladder order matters)
 * @param opposed      ids of cults this one opposes (cross-reputation cost)
 * @param signatureRite the rite that grants reputation with this cult
 * @param fantasy      one-line pitch
 * @param services     rank-gated services this cult offers (see {@link CultService})
 */
public record CultDefinition(
        String id,
        String name,
        String domain,
        List<Component> ranks,
        List<String> opposed,
        String signatureRite,
        String fantasy,
        List<CultService> services) {

    /** Rank names in ladder order. */
    public List<Component> ranks() {
        return List.copyOf(ranks);
    }

    /** Rank-gated services, in declaration order. */
    public List<CultService> services() {
        return List.copyOf(services);
    }

    /** @return the service with {@code serviceId}, or {@code null} if this cult does not offer it. */
    public @Nullable CultService service(String serviceId) {
        for (CultService service : services) {
            if (service.id().equals(serviceId)) {
                return service;
            }
        }
        return null;
    }
}
