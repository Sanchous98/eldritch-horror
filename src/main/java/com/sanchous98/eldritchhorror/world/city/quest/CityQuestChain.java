package com.sanchous98.eldritchhorror.world.city.quest;

import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.loc.city.plan.CityPlan;
import com.sanchous98.eldritchhorror.world.loc.city.plan.CityPlans;
import com.sanchous98.eldritchhorror.world.loc.city.plan.CityPoi;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.Nullable;

/**
 * One curated city's local contract: the short 3-5 step chain a player can work through in that
 * city ({@code design/21-settlements.md}, {@code docs/CITY-PLAN-CONTRACT.md} "Quest chains").
 *
 * <p>The chain is derived from the plan's authored {@link CityPoi} anchors, so it is layout-aware
 * without new per-city data: the contact (the quest giver), the hidden place and the coordinates
 * of every objective come from the blocks the city actually placed. A plan without a contact still
 * gets a usable chain (the notice board stands in as the giver), and the item/threat objectives are
 * chosen deterministically from the city id so two visits to the same city always ask for the same
 * thing.
 *
 * <p>Objective kinds:
 * <ul>
 *   <li>{@link Kind#TALK} — reach the contact who gives the contract (the usual first step);</li>
 *   <li>{@link Kind#BOARD} — stand at the notice board (only when the plan has no NPC);</li>
 *   <li>{@link Kind#GATHER} — carry N of a reagent the district trades in;</li>
 *   <li>{@link Kind#HUNT} — kill N district threats (counted only inside the city);</li>
 *   <li>{@link Kind#SECRET} — reach the authored hidden place, or the far landmark when the plan
 *       authors none.</li>
 * </ul>
 */
public record CityQuestChain(City city, List<Step> steps) {

    /** How the player satisfies one step. */
    public enum Kind {
        BOARD,
        TALK,
        GATHER,
        HUNT,
        SECRET
    }

    /**
     * One objective.
     *
     * @param kind what to do
     * @param id stable step id ({@code board}, {@code contact}, {@code gather}, {@code hunt}, ...)
     * @param x absolute world X of the objective (for reach steps)
     * @param z absolute world Z of the objective (for reach steps)
     * @param item registry path of the item for {@link Kind#GATHER}, else {@code ""}
     * @param entity registry path of the mob for {@link Kind#HUNT}, else {@code ""}
     * @param count how many items/kills the step needs (1 for reach steps)
     * @param anchor authored POI id this step was derived from, else {@code ""}
     */
    public record Step(Kind kind, String id, int x, int z, String item, String entity, int count,
                       String place, String anchor) {
    }

    /** Reagents a city contract may ask for; all are common chest/mob finds, none are boss-only. */
    private static final String[] GATHER_POOL = {
            "salt_reagent", "chalk_reagent", "bone_reagent", "moonwater"
    };

    /** District threats a contract may ask the player to clear. */
    private static final String[] HUNT_POOL = {
            "risen_husk", "tainted_fauna", "veil_stalker"
    };

    /** Builds the chain for {@code city} from its authored plan (or a centre-only fallback). */
    public static CityQuestChain of(City city) {
        CityPlan plan = CityPlans.forCity(city.name());
        List<CityPoi> pois = plan == null ? List.of() : plan.pois();
        CityPoi board = first(pois, CityPoi.Kind.QUEST_BOARD);
        CityPoi contact = first(pois, CityPoi.Kind.NPC);
        CityPoi secret = first(pois, CityPoi.Kind.SECRET);
        if (secret == null) {
            secret = farLandmark(pois);
        }

        List<Step> steps = new ArrayList<>(5);
        // The contract starts with the person who gives it. A plan without an authored NPC still
        // gets a usable first step: the notice board stands in as the giver.
        if (contact != null) {
            steps.add(reach(Kind.TALK, "contact", contact, city, contact.id(), pois));
        } else if (board != null) {
            steps.add(reach(Kind.BOARD, "board", board, city, "notice_board", pois));
        }

        int hash = Math.abs(city.id().hashCode());
        String item = GATHER_POOL[hash % GATHER_POOL.length];
        // 2..4: enough to make the player look, small enough for one ruin chest to cover it.
        int itemCount = 2 + hash / 7 % 3;
        steps.add(new Step(Kind.GATHER, "gather", city.x(), city.z(), item, "", itemCount, "", ""));
        String entity = HUNT_POOL[hash / 3 % HUNT_POOL.length];
        int kills = 2 + hash / 11 % 2;
        steps.add(new Step(Kind.HUNT, "hunt", city.x(), city.z(), "", entity, kills, "", ""));
        if (secret != null) {
            steps.add(reach(Kind.SECRET, "secret", secret, city, secret.id(), pois));
        }
        return new CityQuestChain(city, List.copyOf(steps));
    }

    /** The authored anchor of a given kind, or {@code null}. */
    private static @Nullable CityPoi first(List<CityPoi> pois, CityPoi.Kind kind) {
        for (CityPoi poi : pois) {
            if (poi.kind() == kind) {
                return poi;
            }
        }
        return null;
    }

    /** The authored landmark farthest from the centre (the "other end of town" fallback secret). */
    private static @Nullable CityPoi farLandmark(List<CityPoi> pois) {
        CityPoi best = null;
        int bestDistance = -1;
        for (CityPoi poi : pois) {
            if (poi.kind() != CityPoi.Kind.LANDMARK) {
                continue;
            }
            int distance = poi.dx() * poi.dx() + poi.dz() * poi.dz();
            if (distance > bestDistance) {
                bestDistance = distance;
                best = poi;
            }
        }
        return best;
    }

    /** A reach step at {@code poi}'s authored anchor (1 block = 1 block, relative to the centre). */
    private static Step reach(Kind kind, String id, @Nullable CityPoi poi, City city, String anchor,
                              List<CityPoi> pois) {
        int x = city.x() + (poi == null ? 0 : poi.dx());
        int z = city.z() + (poi == null ? 0 : poi.dz());
        return new Step(kind, id, x, z, "", "", 1,
                poi == null ? "" : placeName(poi, pois), poi == null ? "" : anchor);
    }

    /**
     * A diegetic place name: the authored POI (its {@code data} hint, else its id) anchored to the
     * nearest authored landmark, e.g. {@code "imam at the Great Mosque"}. A notice board is named
     * after its own location instead ({@code "bazaar_quest_board"} -> {@code "bazaar"}), because
     * boards author quest ids in {@code data}, not a place.
     */
    private static String placeName(CityPoi poi, List<CityPoi> pois) {
        String own = poi.kind() == CityPoi.Kind.QUEST_BOARD ? boardName(poi) : ownName(poi);
        if (poi.kind() != CityPoi.Kind.NPC && poi.kind() != CityPoi.Kind.SECRET) {
            return own;
        }
        CityPoi landmark = nearestLandmark(pois, poi);
        if (landmark == null) {
            return own;
        }
        String name = titleCase(stripKind(landmark.id()));
        if (name.isEmpty() || own.equalsIgnoreCase(name) || own.contains(name.toLowerCase(Locale.ROOT))) {
            return own;
        }
        return own + " at the " + name;
    }

    /** The authored hint of a POI, humanised; falls back to its id with the kind suffix stripped. */
    private static String ownName(CityPoi poi) {
        String raw = poi.data();
        int semi = raw.indexOf(';');
        if (semi >= 0) {
            raw = raw.substring(0, semi);
        }
        raw = raw.trim();
        if (raw.isEmpty()) {
            raw = stripKind(poi.id());
        }
        return raw.replace('_', ' ').toLowerCase(Locale.ROOT);
    }

    /** A notice board's location, taken from its id ("bazaar_quest_board" -> "bazaar"). */
    private static String boardName(CityPoi poi) {
        String raw = stripKind(poi.id()).trim();
        return raw.isEmpty() ? ownName(poi) : raw.replace('_', ' ').toLowerCase(Locale.ROOT);
    }

    /** The nearest authored landmark to {@code poi}, by horizontal distance, or {@code null}. */
    private static @Nullable CityPoi nearestLandmark(List<CityPoi> pois, CityPoi poi) {
        CityPoi best = null;
        long bestDistance = Long.MAX_VALUE;
        for (CityPoi candidate : pois) {
            if (candidate.kind() != CityPoi.Kind.LANDMARK) {
                continue;
            }
            long dx = candidate.dx() - poi.dx();
            long dz = candidate.dz() - poi.dz();
            long distance = dx * dx + dz * dz;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    /** Humanised landmark name with its words capitalised ("great_mosque" -> "Great Mosque"). */
    private static String titleCase(String raw) {
        String[] words = raw.replace('_', ' ').replace('-', ' ').trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }

    /** Removes the generic kind suffix from an authored id. */
    private static String stripKind(String id) {
        return id.replaceAll("(?i)_(quest_board|board|sign|npc)$", "");
    }

    /** Lowercase registry path without the namespace, for matching inventory/entity output. */
    public static String shortId(String registryPath) {
        int colon = registryPath.indexOf(':');
        return (colon >= 0 ? registryPath.substring(colon + 1) : registryPath).toLowerCase(Locale.ROOT);
    }
}
