#!/usr/bin/env python3
"""Enrich issue bodies for eldritch-horror.

- Rewrites each epic with a full brief (vision, in/out of scope, definition of done,
  risks, child stories).
- Rewrites each story into a consistent, richer template: Context, Tasks (checklist),
  Acceptance criteria (checklist), Dependencies and Technical notes, derived from the
  story's labels plus its existing Do/Acceptance text.

Idempotent and safe to re-run: only PATCHes an issue when the body actually changes,
and never touches title/labels/milestone.
"""
import json
import re
import sys
import urllib.request

REPO = "Sanchous98/eldritch-horror"
API = "https://api.github.com"


def token():
    with open("/home/dev/.config/gh/hosts.yml") as f:
        m = re.search(r"oauth_token:\s*(gho_\S+)", f.read())
    if not m:
        sys.exit("no oauth token found")
    return m.group(1)


TOKEN = token()


def api(method, path, data=None):
    body = json.dumps(data).encode() if data is not None else None
    req = urllib.request.Request(
        API + path, data=body, method=method,
        headers={
            "Authorization": f"token {TOKEN}",
            "Accept": "application/vnd.github+json",
            "X-GitHub-Api-Version": "2022-11-28",
            **({"Content-Type": "application/json"} if body else {}),
        },
    )
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read().decode() or "null")


def paged(path):
    out, page = [], 1
    sep = "&" if "?" in path else "?"
    while True:
        batch = api("GET", f"{path}{sep}per_page=100&page={page}")
        out.extend(batch)
        if len(batch) < 100:
            return out
        page += 1


# ------------------------------------------------------------------ epics
EPIC_BODIES = {
"Epic: Core Framework & Platform": """## Vision
Provide the shared plumbing every other system builds on, so feature work never
re-invents config, networking, persistence, datapack loading or test harnesses.

## In scope
- Config organisation (COMMON / SERVER / CLIENT) and reload behaviour.
- Networking framework (packet registration, `StreamCodec` helpers, sync policy).
- Persistence helpers for player attachments and chunk/level saved data.
- Datapack registry loading + validation conventions.
- Debug commands, diagnostics, and the test harness (unit + GameTest).

## Out of scope
- Any gameplay system itself (those are separate epics) — this epic only supplies
  the primitives and conventions they consume.

## Definition of done
- A new system can add config, a sync packet, persisted state and a datapack type
  by following documented conventions, with no changes to this epic's code.
- `./gradlew build` and the GameTest suite pass in CI.

## Risks / notes
- Version coupling: NeoForge internals change between MC versions; keep the surface small.
- Prefer NeoForge attachments over custom capability plumbing on 1.21.1.

## Related
- `docs/ARCHITECTURE.md` (layers, conventions, open questions).""",

"Epic: Character & Progression": """## Vision
Give the player an RPG *build*: choices that change how they engage sanity,
corruption, rituals and cults — not just gear stats.

## In scope
- XP/levels and a data-driven skill tree (nodes, prerequisites, costs).
- Archetypes (Investigator / Occultist / Cultist) and cross-archetype synergies.
- Passive perks and an active-ability framework (cooldown, cost, targets).
- Knowledge/lore unlocks as progression, and an expensive respec rite.

## Out of scope
- The sanity/corruption meters themselves (they live in their own epics); this epic
  consumes their attributes to grant build choices.

## Definition of done
- A player can level, spend points in a skill tree, choose an archetype, and feel the
  difference in at least two systems (e.g. sanity drain, rite access).
- Progression persists and migrates across versions.

## Risks / notes
- Balance is the main risk: no single dominant build; leave knobs in config/data.
- Keep the tree data-driven so content packs can extend it.

## Related
- `docs/DESIGN.md` §The three axes, §Progression.""",

"Epic: Sanity System": """## Vision
The mind is the first thing the horror takes. Sanity is a regenerating meter that
drains near the eldritch and recovers with rest and light; at the bottom it lies to you.

## In scope
- A `max_sanity` attribute plus synced per-player current sanity.
- A composable `SanitySource` SPI so content declares its own drain/regen.
- Threshold behaviour: whispers, hallucinations, `Madness`, `Marked`.
- Items/food that move sanity, difficulty scaling, and the client distortion overlay.

## Out of scope
- Corruption (a separate, durable axis) — sanity can be restored; corruption mostly cannot.

## Definition of done
- With the system enabled, a player in darkness/near rifts measurably drains, and
  recovering sanity clears the low-sanity effects.
- All rates come from config, not magic numbers.

## Risks / notes
- Keep drain composable; do not centralise every rule in one tick handler.
- Server-authoritative values, client only renders.

## Related
- `docs/DESIGN.md` §Sanity; `docs/ARCHITECTURE.md` §sanity.""",

"Epic: Corruption & Taint": """## Vision
Power leaves a mark. Corruption is the durable cost axis: it grows from forbidden
knowledge and rituals, barely recedes, and gates the darker content the cults offer.

## In scope
- Per-player corruption stages (Dormant → Touched → Marked → Claimed) and effects.
- A per-chunk corruption field driving block conversion and ambient spawns.
- Spread from rifts/altars, cleansing/redemption rites, resistance, per-dimension rules.

## Out of scope
- Sanity (recoverable) and the rituals that grant corruption (see those epics).

## Definition of done
- A placed rift visibly corrupts surroundings; high corruption gates content and
  changes how NPCs react; a cleansing rite can partially reverse it at real cost.

## Risks / notes
- The per-chunk field is the performance hotspot: tick only near players, bound memory.
- Corruption should be a *cost*, never a free power source.

## Related
- `docs/DESIGN.md` §Corruption; `docs/ARCHITECTURE.md` §corruption.""",

"Epic: Rituals & Occultism": """## Vision
Rituals are the mod's quest-like progression engine: named, data-driven recipes
performed at an altar that unlock content, summon entities and open rifts.

## In scope
- A `RitualDefinition` datapack type (pattern, offerings, conditions, outcomes).
- A block-pattern matcher and an atomic resolver.
- Outcome types as a registry; failure/backlash that always costs something.
- Rite learning/teaching, tiers, altar upgrades and discovery hints.

## Out of scope
- The cults that teach the rites (separate epic) and the world structures (world epic).

## Definition of done
- A player can learn, set up and perform a tier-1 rite end-to-end, and a failed rite
  costs sanity or corruption rather than silently no-opping.

## Risks / notes
- Decide matcher strategy early (structure templates vs. bespoke) — it shapes tooling.
- Outcomes must be data-extensible so content packs add their own.

## Related
- `docs/DESIGN.md` §Rituals; `docs/ARCHITECTURE.md` §ritual.""",

"Epic: Cults & Factions": """## Vision
Factions remember. Cults offer power for loyalty and oppose each other, so helping
one closes a door elsewhere — the social axis of the RPG.

## In scope
- A `CultDefinition` datapack type (rites, demands, taboos, ranks).
- Per-player reputation with opposed-pair effects, ranks/promotion, decay.
- Cultist NPCs with worship AI/schedules, trading, territory, strongholds, sacrifice.

## Out of scope
- The generic dialogue/quest engine they use (separate epic).

## Definition of done
- A player can gain and lose standing with the three seed cults, unlock a service by
  rank, and see an opposed-cult consequence.

## Risks / notes
- Keep cults data-driven; no subclass per cult.
- Reputation must be per-player (multiplayer-safe).

## Related
- `docs/DESIGN.md` §Cult reputation.""",

"Epic: Quest & Dialogue System": """## Vision
A generic engine so cults and content can hand out objectives and conversations as
data, without bespoke code per quest.

## In scope
- Datapack dialogue trees + a conversation UI with choices/persistence.
- Quest definitions, objective types (kill/gather/rite/reach/talk), chains, repeatables.
- Reputation-gated dialogue and integrated rewards (items, rep, XP).

## Out of scope
- The cult definitions themselves; this epic provides the machinery they use.

## Definition of done
- A designer can write a branching dialogue and a multi-step quest entirely in JSON
  and have it work in-game, including a gated branch.

## Risks / notes
- Localization from day one: no hardcoded strings.
- Keep objective types extensible via a registry.

## Related
- `docs/ARCHITECTURE.md`; `docs/DESIGN.md` §Progression.""",

"Epic: World, Dimensions & Biomes": """## Vision
The places horror lives: corrupted biomes, cult strongholds, rifts that link places,
and the eldritch Veil dimension.

## In scope
- Corrupted biomes + worldgen features (rifts, idols, ruins) and loot tables.
- Cult stronghold jigsaw structures.
- The Veil dimension, stable travel to/from it, per-dimension sanity/corruption rules.

## Out of scope
- The entities that inhabit these places (bestiary epic) and the rites that open rifts.

## Definition of done
- A world generates corrupted biomes and at least one stronghold; a player can travel
  to the Veil and back; per-dimension rules apply from data.

## Risks / notes
- Worldgen compatibility is fragile — keep features configurable and datapack-driven.
- Dimension ambience is client-side (see art/audio epic).

## Related
- `docs/DESIGN.md`; `docs/ARCHITECTURE.md`.""",

"Epic: Bestiary & Bosses": """## Vision
The things that should not be: swarms, star-spawn, zealots, and the endgame bosses —
with AI, abilities and drops that make encounters feel eldritch, not generic.

## In scope
- A roster of creatures (swarm, star-spawn, shoggoth, zealots) with AI goals/abilities.
- Difficulty-scaled spawns tied to corruption/stage, per-entity loot and vocals.
- Boss encounters with phases (Choir Leviathan, The Horror) and arenas.

## Out of scope
- The player's sanity/corruption mechanics they interact with.

## Definition of done
- Each entity spawns appropriately, behaves distinctly, and drops correct loot; the
  two bosses have documented, implemented encounters.

## Risks / notes
- Share AI goals via a library; avoid copy-paste per mob.
- Bosses must be solvable via the endgame paths, not only combat.

## Related
- `docs/DESIGN.md` §Bestiary.""",

"Epic: Gear, Items & Economy": """## Vision
What you carry and trade: artefacts and reagents, altar-based crafting, occult
inscriptions, cursed items and cult trade — the material economy of the occult.

## In scope
- Artefacts, reagents, currency/valuables, rarity tiers, tomes/scrolls.
- Altar-based crafting recipes and occult inscriptions (enchant-like upgrades).
- Cursed items with drawbacks + gear that moves sanity/corruption.
- Cult/merchant trading (reputation-gated) and item lore/tooltips.

## Out of scope
- The rituals that consume reagents (ritual epic) and the trade UI (UI epic).

## Definition of done
- A player can gather reagents, craft through an altar, equip an artefact that changes
  a meter, and buy/sell with a cult at the required standing.

## Risks / notes
- Every item needs a lang entry and a tooltip; treat tooltips as documentation.
- Balance artefacts as sidegrades with costs, not pure upgrades.

## Related
- `docs/DESIGN.md`; `docs/ARCHITECTURE.md`.""",

"Epic: UI, Codex & Accessibility": """## Vision
Everything the player reads and clicks: a codex that rewards discovery, a shared
screen framework, config-in-game, and accessibility so the horror is fair.

## In scope
- Codex/lore journal, shared menu framework, in-game config screen, info panels/tooltips.
- Extensible HUD framework, event toasts, map markers, keybinds/controller support.
- Accessibility (photosensitivity, colour-blind palettes, motion/overlay options).

## Out of scope
- The world systems that feed the UI.

## Definition of done
- A new system can register a codex entry and a HUD widget via the framework; all UI
  text is localized; accessibility options change rendering.

## Risks / notes
- The distortion/overlay effects need accessibility gates from the start.
- No hardcoded strings; all text keyed for translation.

## Related
- `docs/ARCHITECTURE.md` §client.""",

"Epic: Art, Audio & Presentation": """## Vision
The look and the sound that carry the dread: textures and models, particles and VFX,
a horror soundscape, context music, and colour grading.

## In scope
- Mod icon, block/item textures, entity models/animations.
- Custom particles and rift/corruption VFX, colour grading/post effects.
- Soundscape (whispers, chants, altar hum) and music/ambience tracks.
- UI art/iconography and release media (screenshots/trailer).

## Out of scope
- Gameplay logic; this epic makes the existing systems legible and awful to hear.

## Definition of done
- Every block/item/entity is textured and animated; sounds trigger on the right events;
  the icon shows in the mods list.

## Risks / notes
- Audio is a large part of horror — budget for it early, not as polish.
- Keep VFX behind accessibility options.

## Related
- `docs/DESIGN.md`.""",

"Epic: Compatibility, Localization & Release": """## Vision
Ship it: integrate with the ecosystem, translate it, publish it, and support servers
and future updates without breaking saves.

## In scope
- Release workflow (tag → jar + Release), Modrinth/CurseForge publishing, update checker.
- JEI/EMI and common-mod compatibility; dedicated-server support and testing.
- Translation framework + first languages; config/save migration; authoring docs.

## Out of scope
- Feature work; this epic is the path to a stable, installable release.

## Definition of done
- A tagged build publishes a jar; the mod runs on a dedicated server and alongside the
  common recipe-viewer mods; at least two languages ship.

## Risks / notes
- Migration must never lose progression/corruption on upgrade.
- Treat datapack authoring docs as a product surface.

## Related
- `README.md`; `.github/workflows/`.""",

"Epic: Balance, QA & Performance": """## Vision
Make it good and make it fast: tune the economies, automate the tests, stress the
server, and run a triage process that keeps quality high.

## In scope
- Balance passes for sanity and corruption economies.
- Performance work on chunk corruption ticks and AI/spawns.
- Playtest scripts, regression GameTests, multiplayer stress tests, bug triage process.

## Out of scope
- New features; this epic hardens what exists.

## Definition of done
- No single dominant build; server tick remains stable under load; regression tests
  cover the core systems; triage process is documented and used.

## Risks / notes
- Measure before tuning; keep every balance value in config/data.
- Performance regressions are easiest to catch with an automated benchmark.

## Related
- `docs/DESIGN.md` (numbers), `docs/ARCHITECTURE.md` (performance notes).""",
}

# ------------------------------------------------------------------ story derivation
AREA_CONTEXT = {
    "Sanity": "Part of the sanity axis: a regenerating meter that drains near the eldritch.",
    "Corruption": "Part of the corruption axis: a durable exposure value that gates dark content.",
    "Cult": "Part of the faction layer: cults remember and oppose each other.",
    "Ritual": "Part of the ritual engine: data-driven altar rites with costs.",
    "Progression": "Part of character progression: levels, skills and archetypes.",
    "Quest": "Part of the quest/dialogue engine used by cults and content.",
    "World": "Part of the world layer: biomes, rifts, structures and dimensions.",
    "Bestiary": "Part of the bestiary: creatures and boss encounters.",
    "Economy": "Part of the gear/items economy: artefacts, reagents and trade.",
    "UI": "Part of the player-facing UI: codex, menus, HUD and accessibility.",
    "Art & Audio": "Part of presentation: textures, models, particles and sound.",
    "Build": "Part of the build/platform layer.",
    "Compat": "Part of shipping: integration, localization and release.",
    "QA": "Part of quality: balance, tests and performance.",
    "Core": "Part of the core framework shared by all systems.",
    "Research": "A design decision that unblocks implementation.",
}
AREA_NOTES = {
    "Sanity": ["State is server-authoritative; client only renders the synced value.",
               "Use the `SanitySource` SPI rather than a bespoke tick handler.",
               "All rates live in config."],
    "Corruption": ["Persist per-chunk state carefully; bound memory and tick only near players.",
                   "Corruption is a cost axis — never a free buff.",],
    "Cult": ["Cults are datapack-defined; no subclass per cult.",
             "Reputation is per-player for multiplayer safety."],
    "Ritual": ["Rites are datapack JSON; outcomes register via a registry.",
               "A failed rite must cost something — never a silent no-op."],
    "Progression": ["Keep the skill tree data-driven so content packs can extend it.",
                    "Progression is persisted and versioned for migration."],
    "Quest": ["Objectives and dialogue are data; no hardcoded strings.",
              "Objective types register via a registry."],
    "World": ["Worldgen is compatibility-sensitive: make features configurable/datapack-driven.",
              "Dimension ambience is client-side."],
    "Bestiary": ["Share AI goals via a small library.",
                 "Tune spawns against corruption/stage, not difficulty alone."],
    "Economy": ["Every item needs a lang key and a tooltip.",
                "Prefer sidegrades with costs over pure upgrades."],
    "UI": ["All UI text is keyed for translation.",
           "Distortion/VFX must honour accessibility options."],
    "Art & Audio": ["Sounds/VFX trigger from server events where possible.",
                    "Keep heavy effects behind accessibility toggles."],
    "Build": ["Keep the NeoForge surface small — internals change between versions."],
    "Compat": ["Never break save/config compatibility on upgrade."],
    "QA": ["Measure before tuning; keep values in config/data."],
    "Core": ["Follow the conventions in docs/ARCHITECTURE.md."],
    "Research": ["Record the decision and its consequences in the relevant doc."],
}
AREA_DEPS = {
    "Sanity": ["Core Framework & Platform"],
    "Corruption": ["Core Framework & Platform"],
    "Cult": ["Core Framework & Platform", "Quest & Dialogue System"],
    "Ritual": ["Core Framework & Platform"],
    "Progression": ["Core Framework & Platform"],
    "Quest": ["Core Framework & Platform"],
    "World": ["Core Framework & Platform"],
    "Bestiary": ["Core Framework & Platform"],
    "Economy": ["Core Framework & Platform", "Rituals & Occultism"],
    "UI": ["Core Framework & Platform"],
    "Art & Audio": [],
    "Build": [],
    "Compat": ["Balance, QA & Performance"],
    "QA": [],
    "Core": [],
    "Research": [],
}
AREA_REF = {
    "Sanity": "design/03a-sanity.md",
    "Corruption": "design/03b-corruption.md",
    "Cult": "design/06-factions.md and design/09-cults.md",
    "Ritual": "design/05-ritual-engine.md and design/08-rituals.md",
    "Progression": "design/02-progression.md",
    "Quest": "design/10-quests.md",
    "World": "design/20-map.md",
    "Bestiary": "design/17-mobs.md and design/18-bosses.md",
    "Economy": "design/16-items.md",
    "UI": "design/13-ui-ux.md",
    "Art & Audio": "design/07-horror-atmosphere.md",
    "Build": "docs/ARCHITECTURE.md",
    "Compat": "design/12-multiplayer.md and README.md",
    "QA": "design/04-pillars.md",
    "Core": "docs/ARCHITECTURE.md",
    "Research": "design/README.md (Open questions)",
}
LABEL_TO_AREA = [
    ("sanity", "Sanity"), ("corruption", "Corruption"), ("cult", "Cult"),
    ("ritual", "Ritual"), ("dialogue", "Quest"), ("quest", "Quest"),
    ("dimension", "World"), ("worldgen", "World"),
    ("boss", "Bestiary"), ("bestiary", "Bestiary"),
    ("economy", "Economy"), ("accessibility", "UI"), ("ui", "UI"),
    ("localization", "Compat"), ("compatibility", "Compat"),
    ("balance", "QA"), ("performance", "QA"), ("testing", "QA"),
    ("audio", "Art & Audio"), ("art", "Art & Audio"),
    ("progression", "Progression"), ("networking", "Core"), ("data", "Core"),
    ("build", "Build"), ("tech-debt", "Core"),
]


def area_for(issue):
    if issue["title"].lower().startswith("decide"):
        return "Research"
    labels = {l["name"] for l in issue["labels"]}
    for key, area in LABEL_TO_AREA:
        if key in labels:
            return area
    return "Core"


DO_RE = re.compile(r"\*\*Do\*\*\s*\n(.*?)(?:\n\n\*\*Acceptance\*\*|\Z)", re.S)
AC_RE = re.compile(r"\*\*Acceptance\*\*\s*\n(.*)", re.S)


def parse(body):
    """Return (preamble, do_text, accept_text) from an existing body."""
    do = DO_RE.search(body)
    ac = AC_RE.search(body)
    do_text = do.group(1).strip() if do else ""
    ac_text = ac.group(1).strip() if ac else ""
    pre = body.split("**Do**")[0].strip() if "**Do**" in body else ""
    pre = re.sub(r"^#+\s.*$", "", pre, flags=re.M).strip()
    return pre, do_text, ac_text


def clean_bullet(text):
    text = text.strip()
    text = re.sub(r"^[-*]\s*", "", text)
    return text.split("\n")[0].strip()


def render_story(issue, area):
    pre, do, ac = parse(issue["body"] or "")
    do = clean_bullet(do) or "Implement the change described by the title."
    ac = clean_bullet(ac) or "The change is implemented and verified."
    labels = {l["name"] for l in issue["labels"]}

    lines = ["## Context", AREA_CONTEXT.get(area, AREA_CONTEXT["Core"])]
    if pre:
        lines.append("")
        lines.append(pre)
    lines += ["", "## Tasks", f"- [ ] {do}"]
    if "balance" in labels:
        lines.append("- [ ] Document the chosen values and where they live (config/data).")
    if labels & {"ui", "quest", "dialogue", "economy", "content"}:
        lines.append("- [ ] Add `en_us` lang entries for any player-facing text.")
    if labels & {"data"}:
        lines.append("- [ ] Validate the data shape and report errors with the file path.")
    lines.append("- [ ] Add a debug command or GameTest covering the behaviour.")
    lines.append("- [ ] Update `docs/` if the design or an open question is resolved.")

    lines += ["", "## Acceptance criteria", f"- [ ] {ac}"]
    if "networking" in labels:
        lines.append("- [ ] Verified for multiple players / reconnect.")
    if labels & {"client"}:
        lines.append("- [ ] No client-only code is referenced from common/server paths.")
    lines.append("- [ ] Behaviour is deterministic and covered by a test or documented manual check.")

    deps = AREA_DEPS.get(area, [])
    lines += ["", "## Dependencies"]
    lines.append("- " + (", ".join(deps) if deps else "None — can start immediately."))

    lines += ["", "## Technical notes"]
    for n in AREA_NOTES.get(area, AREA_NOTES["Core"]):
        lines.append(f"- {n}")

    lines += ["", "## References", f"- {AREA_REF.get(area, AREA_REF['Core'])}"]
    return "\n".join(lines)


def main():
    issues = [i for i in paged(f"/repos/{REPO}/issues?state=all") if "pull_request" not in i]
    epics = {i["title"]: i for i in issues if i["title"].startswith("Epic:")}
    updated = 0
    for issue in issues:
        if issue["title"] in EPIC_BODIES:
            new = EPIC_BODIES[issue["title"]]
        elif issue["title"].startswith("Epic:"):
            continue
        else:
            new = render_story(issue, area_for(issue))
        if (issue["body"] or "").strip() == new.strip():
            continue
        api("PATCH", f"/repos/{REPO}/issues/{issue['number']}", {"body": new})
        updated += 1
        print(f"  ~ {issue['title']}")
    print(f"\nenriched {updated} issue(s); {len(epics)} epics present")


if __name__ == "__main__":
    main()
