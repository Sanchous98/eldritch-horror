#!/usr/bin/env python3
"""Expand the eldritch-horror roadmap into an epic -> story backlog.

Idempotent: skips existing labels, milestones, epics, stories and sub-issue links.
Creates ~14 epics and ~145 stories, each story linked to its epic as a GitHub
sub-issue, with a milestone and labels.

Needs the `repo` scope (issues/milestones/labels). Sub-issue linking uses the
REST sub_issues endpoint and degrades gracefully if unavailable.
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


def api(method, path, data=None, allow_fail=False):
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
    try:
        with urllib.request.urlopen(req) as r:
            return json.loads(r.read().decode() or "null")
    except urllib.error.HTTPError as e:
        if allow_fail:
            return None
        sys.exit(f"{method} {path} -> {e.code}: {e.read().decode()}")


# ------------------------------------------------------------------ labels
NEW_LABELS = {
    "progression": ("0e8a16", "Character progression: levels, skills, archetypes"),
    "quest": ("c2e0c6", "Quests and objectives"),
    "dialogue": ("bfd4f2", "Dialogue system"),
    "worldgen": ("006b75", "World generation, structures, biomes"),
    "dimension": ("5319e7", "Dimensions and travel"),
    "bestiary": ("b60205", "Mobs and creatures"),
    "boss": ("d93f0b", "Boss encounters"),
    "economy": ("fbca04", "Items, artefacts, crafting, trading"),
    "ui": ("1d76db", "Menus, HUD, codex"),
    "accessibility": ("c5def5", "Accessibility options"),
    "audio": ("f9d0c4", "Sound and music"),
    "art": ("d4c5f9", "Textures, models, particles, VFX"),
    "localization": ("bfdadc", "Translation and i18n"),
    "compatibility": ("0e8a16", "Interop with other mods/platforms"),
    "balance": ("e99695", "Tuning and game balance"),
    "performance": ("8a2be2", "Performance and profiling"),
    "testing": ("c2e0c6", "Tests and QA"),
}

MILESTONES = [
    ("M6 — Character & Progression", "Levels, skill trees, archetypes, knowledge."),
    ("M7 — World & Dimensions", "Corrupted biomes, rifts, the veil, structures."),
    ("M8 — Quest & Dialogue", "Faction quests, dialogue trees, objectives."),
    ("M9 — Gear & Economy", "Artefacts, reagents, crafting, trading."),
    ("M10 — UI & Codex", "Codex, HUD framework, menus, accessibility."),
    ("M11 — Art & Audio", "Textures, models, particles, soundscape, music."),
    ("M12 — Polish, Balance & Release", "Compatibility, localization, balance, QA, release."),
]

# ------------------------------------------------------------------ epics
# (key, title, milestone, labels, description)
EPICS = [
    ("core", "Epic: Core Framework & Platform", "M1 — Foundations",
     ["build", "tech-debt"],
     "Shared plumbing every system builds on: config, networking, persistence, "
     "datapack loading conventions, commands, tests and diagnostics."),
    ("progression", "Epic: Character & Progression", "M6 — Character & Progression",
     ["progression"],
     "RPG character growth: levels, a data-driven skill tree, archetypes, perks, "
     "active abilities and knowledge as progression."),
    ("sanity", "Epic: Sanity System", "M2 — Sanity",
     ["sanity"],
     "The mind meter: attributes, per-player state, composable drain/regen, "
     "madness thresholds, hallucinations and the HUD."),
    ("corruption", "Epic: Corruption & Taint", "M3 — Corruption",
     ["corruption"],
     "The durable cost axis: per-player stages, a per-chunk field, spread and "
     "block conversion, cleansing and resistance."),
    ("ritual", "Epic: Rituals & Occultism", "M4 — Rituals & Cults",
     ["ritual"],
     "The progression engine: data-driven rite definitions, pattern matching, "
     "offerings, conditions, outcomes and backlash."),
    ("cult", "Epic: Cults & Factions", "M4 — Rituals & Cults",
     ["cult"],
     "Factions: data-driven cult definitions, reputation, ranks, worship AI, "
     "territory, strongholds and inter-cult conflict."),
    ("quest", "Epic: Quest & Dialogue System", "M8 — Quest & Dialogue",
     ["quest", "dialogue"],
     "A generic quest and dialogue engine so cults and content can hand out "
     "objectives and conversations as data."),
    ("world", "Epic: World, Dimensions & Biomes", "M7 — World & Dimensions",
     ["worldgen", "dimension"],
     "The places: corrupted biomes, worldgen features, cult strongholds, rifts "
     "and the eldritch Veil dimension."),
    ("bestiary", "Epic: Bestiary & Bosses", "M5 — Content & Endgame",
     ["bestiary", "boss"],
     "Creatures: swarms, star-spawn, zealots, and the boss encounters including "
     "the Horror itself."),
    ("economy", "Epic: Gear, Items & Economy", "M9 — Gear & Economy",
     ["economy"],
     "Things you carry and trade: artefacts, reagents, altar crafting, "
     "inscriptions, cursed items and cult trade."),
    ("ui", "Epic: UI, Codex & Accessibility", "M10 — UI & Codex",
     ["ui", "accessibility"],
     "Everything the player reads: codex, menus, HUD framework, notifications "
     "and accessibility options."),
    ("art", "Epic: Art, Audio & Presentation", "M11 — Art & Audio",
     ["art", "audio"],
     "The look and sound: textures, models, particles, soundscape, music and "
     "visual effects."),
    ("compat", "Epic: Compatibility, Localization & Release", "M12 — Polish, Balance & Release",
     ["compatibility", "localization"],
     "Shipping: integrations, translations, publishing platforms, server "
     "support and config migration."),
    ("qa", "Epic: Balance, QA & Performance", "M12 — Polish, Balance & Release",
     ["balance", "performance", "testing"],
     "Making it good and fast: economy tuning, automated tests, stress testing "
     "and a bug triage process."),
]

# ------------------------------------------------------------------ stories
# key -> list of (title, labels, do, accept)
STORIES = {
    "core": [
        ("Split config into COMMON / SERVER / CLIENT", ["build"], "Move ModConfig into per-type specs.", "Each type loads and is reachable from the right side."),
        ("Registry & resource-id helpers", ["build"], "Central id()/registration conventions.", "New registries follow the pattern; no raw id strings."),
        ("Networking framework", ["networking"], "Packet registry + StreamCodec helpers (S2C/C2S).", "A test packet round-trips client<->server."),
        ("Persistence layer for attachments & saved data", ["data"], "Wrap AttachmentType and SavedData usage.", "Player/chunk state persists across a reload."),
        ("Debug command suite (/eldritch ...)", ["build"], "Subcommands for sanity, corruption, reputation, rituals.", "Each system is inspectable and mutable from commands."),
        ("GameTest harness", ["testing"], "Register a GameTest namespace + a smoke test.", "runGameTestServer passes in CI."),
        ("Unit test setup (JUnit)", ["testing"], "Add a fast pure-logic test task.", "Unit tests run in CI."),
        ("Throttled tick utilities", ["performance", "tech-debt"], "Per-player/per-chunk throttled ticking helpers.", "No per-tick allocation; used by >=2 systems."),
        ("Datapack loader conventions", ["data"], "Shared codec/validator helpers for datapack registries.", "Cults and rituals load through the helper."),
        ("Diagnostics & gated debug logging", ["tech-debt"], "Config-gated verbose logging.", "Verbose logs toggle via config."),
        ("Content error reporting", ["tech-debt", "data"], "Validate datapack content with file-level errors.", "A malformed file names its path in the log."),
    ],
    "progression": [
        ("Player level & XP model", ["progression"], "XP sources, curve, level cap.", "XP accrues from rites/kills/quests and persists."),
        ("Skill tree data format", ["progression", "data"], "Datapack skill nodes with prerequisites.", "A skill tree loads from JSON."),
        ("Skill tree UI", ["progression", "ui"], "Screen to view and spend points.", "Points are spendable and state is synced."),
        ("Archetypes / classes", ["progression"], "Investigator / Occultist / Cultist archetypes.", "Choosing an archetype changes available skills."),
        ("Perk & ability registry", ["progression"], "Registered passive/active abilities.", "A perk applies its effect."),
        ("Active ability framework", ["progression"], "Abilities with cooldown and cost.", "An ability fires, costs and cools down."),
        ("Knowledge / lore unlock tracking", ["progression", "data"], "Unlocked codex entries as progression.", "Lore unlocks gate content and show in the codex."),
        ("Attribute progression", ["progression"], "Raise max sanity / corruption resistance.", "Skills/equipment modify the attributes."),
        ("Respec ritual", ["progression", "ritual"], "A costly rite to reset progression.", "Respec refunds points and applies a cost."),
        ("Archetype synergies", ["progression"], "Cross-archetype bonuses.", "A synergy applies only when both nodes are taken."),
        ("Progression persistence & migration", ["progression", "tech-debt"], "Versioned progression save format.", "Old saves migrate without loss."),
        ("Progression balance pass", ["progression", "balance"], "Tune the curve and costs.", "A documented curve with no dominant build."),
    ],
    "sanity": [
        ("Hallucination system", ["sanity", "client"], "Fake mobs, sounds and whispers at low sanity.", "Hallucinations appear/vanish without server entities."),
        ("Rest & nightmare mechanics", ["sanity"], "Sleep restores sanity unless corrupted.", "Nightmares trigger at high corruption."),
        ("Insanity episodes", ["sanity"], "Temporary conditions at thresholds.", "An episode triggers, applies effects and ends."),
        ("Sanity-affecting items & food", ["sanity", "economy"], "Consumables that restore or drain sanity.", "Items apply values and show tooltips."),
        ("Multiplayer sanity semantics", ["sanity"], "Decide and implement individual/shared model.", "Behaviour is consistent for N players."),
        ("Sanity difficulty scaling", ["sanity", "balance"], "Config-scaled drain by difficulty.", "Peaceful and hard differ per config."),
        ("Client distortion overlay", ["sanity", "client"], "Shader/overlay tied to sanity.", "Overlay intensity tracks the synced value."),
    ],
    "corruption": [
        ("Corruption stage effects", ["corruption"], "Marked/Claimed penalties (villagers, sleep).", "Effects are keyed to the stage."),
        ("Corruption sources registry", ["corruption", "data"], "Content declares its taint.", "Adding a source needs no core edit."),
        ("Tainted flora & fauna", ["corruption", "bestiary"], "Corruption-specific plants and mobs.", "Tainted variants spawn in corrupt chunks."),
        ("Cleansing / redemption rites", ["corruption", "ritual"], "Rare, costly ways to reduce corruption.", "A cleansing rite lowers corruption and applies a cost."),
        ("Corruption weather & ambience", ["corruption", "art"], "Fog, particles and events by corruption.", "Ambience scales with local corruption."),
        ("Corruption resistance mechanics", ["corruption"], "Ways to slow corruption gain.", "Resistance reduces gain, never removes it."),
        ("Per-dimension corruption rules", ["corruption", "dimension"], "Different rules per dimension.", "Rules are read from data."),
        ("Corruption visualization tool", ["corruption", "testing"], "Debug view of the chunk corruption field.", "A dev command renders the field."),
    ],
    "ritual": [
        ("Rite learning & teaching", ["ritual", "cult"], "Teach rites via tomes and cults.", "A known-rite check gates performance."),
        ("Ritual categories & tiers", ["ritual"], "Tiered rite taxonomy.", "Tier is gated by corruption/progression."),
        ("Offerings & blood magic variants", ["ritual"], "Blood and variant offerings.", "Offerings are validated and consumed."),
        ("Ritual failure & backlash", ["ritual"], "Consequences for failed rites.", "Backlash applies sanity/corruption."),
        ("Rift opening/closing outcomes", ["ritual", "worldgen"], "Open/close rifts via rites.", "Rift state changes and persists."),
        ("Ritual outcome registry", ["ritual", "data"], "Extensible outcome types.", "A new outcome registers without core edits."),
        ("Altar tiers & upgrades", ["ritual", "content"], "Better altars unlock better rites.", "Altar tier gates rites."),
        ("Ritual conditions engine", ["ritual"], "Time/moon/weather/dimension gates.", "Conditions evaluate and allow or block."),
        ("Rite discovery & hints", ["ritual", "ui"], "Surface hints for unknown rites.", "Hints appear in the codex."),
    ],
    "cult": [
        ("Cult ranks & promotion", ["cult"], "Cult rank progression.", "Rank gates services."),
        ("Worship AI & schedules", ["cult", "bestiary"], "Cultists perform scheduled worship.", "Rituals occur on schedule."),
        ("Cult territory control", ["cult", "worldgen"], "Cults claim regions.", "Territory affects spawns and reputation."),
        ("Cult questlines", ["cult", "quest"], "Faction quest lines.", "Quests unlock via reputation."),
        ("Cult merchants & inventories", ["cult", "economy"], "Trades and stock.", "Trades are reputation-gated."),
        ("Sacrifice mechanics", ["cult", "ritual"], "Offer mobs/items for standing.", "Sacrifice adjusts reputation and corruption."),
        ("Inter-cult conflict events", ["cult"], "Opposed cults go to war.", "Events trigger on reputation thresholds."),
        ("Cult strongholds", ["cult", "worldgen"], "Cult base structures.", "Strongholds generate and are defended."),
        ("Reputation decay & forgiveness", ["cult", "balance"], "Standing drifts and can be restored.", "Decay/restore rules are configurable."),
    ],
    "quest": [
        ("Dialogue data format & runtime", ["dialogue", "data"], "Datapack dialogue trees.", "Dialogue loads and runs."),
        ("Dialogue UI", ["dialogue", "ui"], "Conversation screen with choices.", "Choices are selectable and state synced."),
        ("Quest data format", ["quest", "data"], "Objectives and rewards as data.", "A quest loads and tracks."),
        ("Quest journal UI", ["quest", "ui"], "Track active and completed quests.", "The journal reflects server state."),
        ("Quest objective types", ["quest"], "kill / gather / rite / reach / talk.", "Each type completes correctly."),
        ("Branching dialogue & choices", ["dialogue"], "Choices change outcomes.", "Branches persist per player."),
        ("Quest chains & prerequisites", ["quest"], "Sequenced quest chains.", "A chain advances in order."),
        ("Repeatable & dynamic quests", ["quest"], "Procedural repeatables.", "Repeatables regenerate."),
        ("Faction-gated dialogue", ["dialogue", "cult"], "Reputation gates lines.", "Gated lines show/hide correctly."),
        ("Localization-ready dialogue", ["dialogue", "localization"], "All dialogue strings translatable.", "No hardcoded strings."),
        ("Quest rewards & reputation", ["quest", "economy"], "Integrate rewards with rep/xp/items.", "Rewards grant items, rep and xp."),
        ("Debug quest commands", ["quest", "testing"], "Inspect and advance quests.", "Commands manipulate quest state."),
    ],
    "world": [
        ("The Veil dimension", ["dimension", "worldgen"], "An eldritch plane.", "Travel works and rules differ from the Overworld."),
        ("Corrupted biomes", ["worldgen"], "Overworld corrupted biomes.", "Biomes generate with features."),
        ("Worldgen features (rifts, idols, ruins)", ["worldgen"], "Configured/placed features.", "Features place in the world."),
        ("Cult stronghold generation", ["worldgen", "cult"], "Jigsaw structures.", "Strongholds generate in-world."),
        ("Dimension travel", ["dimension"], "Portals/rites to enter and leave.", "Travel is stable and reversible."),
        ("Dimension ambience (fog/sky/sound)", ["dimension", "art"], "Per-dimension client ambience.", "Client ambience switches with dimension."),
        ("Ambient horror world events", ["worldgen"], "Global events scaling with corruption.", "Events trigger world-wide."),
        ("Structure loot tables", ["worldgen", "economy"], "Loot for generated structures.", "Loot drops appropriate items."),
        ("Biome-dependent spawns", ["worldgen", "bestiary"], "Spawn rules per biome.", "Spawns respect biome rules."),
        ("Rift network & linking", ["worldgen", "dimension"], "Linked rifts that open/close.", "Rifts connect and close."),
        ("Per-dimension sanity/corruption rules", ["dimension", "sanity", "corruption"], "Data-driven dimension rules.", "Rules load from data."),
        ("Worldgen datapack config", ["worldgen", "data"], "Configurable generation.", "Config alters generation."),
    ],
    "bestiary": [
        ("Star-spawn mob", ["bestiary"], "A summoned horror-adjacent mob.", "Spawns via a rite and behaves."),
        ("Shoggoth-type entity", ["bestiary"], "An amorphous horror.", "Moves, attacks and drains sanity."),
        ("Lesser eldritch swarm", ["bestiary"], "Weak creatures in packs.", "Spawns in packs."),
        ("Cult zealots", ["bestiary", "cult"], "Hostile cult combatants.", "Combat scales with cult rank."),
        ("Boss: The Horror", ["boss", "bestiary"], "The endgame boss.", "The encounter is reachable and has phases."),
        ("Boss: Choir Leviathan", ["boss", "bestiary"], "A coastal boss.", "A boss fight with mechanics."),
        ("Entity AI goals library", ["bestiary", "tech-debt"], "Reusable AI goals.", "Goals are shared across mobs."),
        ("Spawn rules & difficulty scaling", ["bestiary", "balance"], "Difficulty-based spawns.", "Scaling follows corruption/stage."),
        ("Per-entity loot", ["bestiary", "economy"], "Drops for each entity.", "Each entity has a loot table."),
        ("Entity special abilities", ["bestiary"], "Unique abilities applying effects.", "Abilities apply their effects."),
        ("Entity vocalizations", ["bestiary", "audio"], "Sounds per entity event.", "Sounds play on the right events."),
        ("Boss encounter design", ["boss"], "Arenas and phase design.", "The encounter is documented and implemented."),
    ],
    "economy": [
        ("Artefacts", ["economy"], "Unique-effect items.", "Artefacts apply their effects."),
        ("Reagent taxonomy", ["economy", "ritual"], "Item categories for rites.", "Reagents are used by rites."),
        ("Altar-based crafting", ["economy", "ritual"], "Recipes that run through altars.", "Recipes resolve through an altar."),
        ("Occult inscriptions", ["economy"], "Enchant-like gear upgrades.", "Inscriptions apply to gear."),
        ("Gear sanity/corruption effects", ["economy", "sanity", "corruption"], "Equipment alters the axes.", "Wearing gear changes the rates."),
        ("Trading with merchants", ["economy", "cult"], "Cult/merchant trades.", "Trades work and are reputation-gated."),
        ("Currency & valuables", ["economy"], "A trade currency.", "Currency is earned and spent."),
        ("Cursed items", ["economy"], "Items with drawbacks.", "Curses apply their cost."),
        ("Item rarity tiers", ["economy", "ui"], "Item rarity.", "Rarity displays and gates."),
        ("Tomes & scrolls", ["economy", "progression"], "Knowledge items.", "Reading grants knowledge and applies a cost."),
        ("Occult tool & weapon set", ["economy"], "A thematic gear set.", "Tools and weapons function."),
        ("Item lore & tooltips", ["economy", "ui"], "Flavour and mechanical info.", "Tooltips show correct effects."),
    ],
    "ui": [
        ("Codex / lore journal", ["ui"], "A lore browser.", "Entries unlock and display."),
        ("Shared menu/screen framework", ["ui"], "Common styling and layout.", "Screens share the framework."),
        ("In-game config screen", ["ui"], "Edit config in-game.", "Values are editable in-game."),
        ("Info panels & tooltips", ["ui"], "Detail panels.", "Panels show correct data."),
        ("Accessibility options", ["accessibility"], "Photosensitivity, colorblind, motion.", "Options change rendering accordingly."),
        ("Extensible HUD framework", ["ui"], "Widget registration.", "Meters register via the framework."),
        ("Event notifications / toasts", ["ui"], "Toasts for notable events.", "Events raise toasts."),
        ("Map markers for rifts & cults", ["ui", "worldgen"], "Markers on the map.", "Markers reflect the world."),
        ("Localization-ready UI text", ["ui", "localization"], "All UI strings keyed.", "No hardcoded UI strings."),
        ("Keybinds & controller support", ["ui", "accessibility"], "Input handling.", "Actions are rebindable."),
        ("Creative / debug tooling UI", ["ui", "testing"], "Dev inspection UI.", "Developers can inspect systems in-game."),
    ],
    "art": [
        ("Mod logo / icon", ["art"], "A 128x128 icon.", "The icon shows in the mods list."),
        ("Block & item textures", ["art"], "Textures for all content.", "Every block/item is textured."),
        ("Entity models & animations", ["art", "bestiary"], "Mob art.", "Entities render and animate."),
        ("Particle effects", ["art"], "Custom particles.", "Particles appear at the right triggers."),
        ("Horror soundscape", ["audio"], "Whispers, chants, altar hum.", "Sounds trigger at the right events."),
        ("Music & ambience tracks", ["audio"], "Context music.", "Music plays per context."),
        ("UI art & iconography", ["art", "ui"], "Icons and frames.", "UI is visually consistent."),
        ("Color grading & post effects", ["art", "client"], "Screen grading.", "Grading changes per context."),
        ("Visual effects for rifts/corruption", ["art"], "VFX tied to world state.", "VFX match the state."),
        ("Marketing assets (screenshots/trailer)", ["art"], "Release media.", "Assets are ready for release."),
    ],
    "compat": [
        ("Release workflow (tag -> jar + release)", ["build", "compatibility"], "Publish on tags.", "A tag produces a release with the jar."),
        ("JEI / EMI integration", ["compatibility"], "Recipe viewer support.", "Rites and recipes are visible."),
        ("Mod compatibility (worldgen/biomes)", ["compatibility", "worldgen"], "Coexist with common mods.", "No conflicts with common mods."),
        ("Translation framework & first languages", ["localization"], "i18n plus initial languages.", "Strings translatable in >1 language."),
        ("Modrinth / CurseForge publishing", ["compatibility"], "Publish to platforms.", "The mod is listed on platforms."),
        ("Update checker", ["compatibility"], "updateJSON support.", "An update notice works."),
        ("Dedicated server support & testing", ["compatibility", "testing"], "Server-side testing.", "It runs on a dedicated server."),
        ("Config & save migration", ["compatibility", "tech-debt"], "Versioned migration.", "Old configs/saves migrate."),
        ("Datapack authoring documentation", ["compatibility", "data"], "Guide for third-party content.", "Third parties can add content."),
        ("License & attribution", ["compatibility"], "MIT plus credits.", "License and credits are present."),
        ("Issue & PR templates", ["compatibility", "testing"], "Contribution templates.", "Templates are available on GitHub."),
    ],
    "qa": [
        ("Balance pass: sanity economy", ["balance", "sanity"], "Tune sanity rates.", "Values are documented."),
        ("Balance pass: corruption economy", ["balance", "corruption"], "Tune corruption gain/loss.", "Values are documented."),
        ("Performance: chunk corruption ticking", ["performance", "corruption"], "Optimize chunk ticks.", "No measurable lag."),
        ("Performance: AI & spawns", ["performance", "bestiary"], "Optimize spawns and AI.", "Server tick stays stable."),
        ("Playtest scripts", ["testing"], "Guided test scenarios.", "Testers follow the scripts."),
        ("Regression GameTests", ["testing"], "Automated coverage of systems.", "Tests cover the core systems."),
        ("Stress testing (many players)", ["testing", "performance"], "Load testing.", "Stable under load."),
        ("Bug triage process", ["testing"], "Labels and workflow.", "The process is documented."),
    ],
}

# ------------------------------------------------------------------ existing mapping
EXISTING_TO_EPIC = [
    ("release workflow", "compat"),
    ("access transformer", "core"),
    ("mod logo", "art"),
    ("sanity", "sanity"),
    ("corruption", "corruption"),
    ("ritual", "ritual"),
    ("cult", "cult"),
    ("rift block", "world"),
    ("first eldritch entity", "bestiary"),
    ("the horror", "bestiary"),
    ("forbidden tomes", "economy"),
    ("soundscape", "art"),
    ("decide", "core"),
]


def epic_key_for(title):
    low = title.lower()
    for needle, key in EXISTING_TO_EPIC:
        if needle in low:
            return key
    return "core"


def ensure(collection, path, item, key="title"):
    existing = {x[key] for x in api("GET", path)}
    return item in existing


def main():
    print("labels:")
    existing_labels = {l["name"] for l in api("GET", f"/repos/{REPO}/labels?per_page=100")}
    for name, (color, desc) in NEW_LABELS.items():
        if name not in existing_labels:
            api("POST", f"/repos/{REPO}/labels", {"name": name, "color": color, "description": desc})
            print(f"  + {name}")

    print("milestones:")
    ms = {m["title"]: m["number"] for m in api("GET", f"/repos/{REPO}/milestones?state=all&per_page=100")}
    for title, desc in MILESTONES:
        if title not in ms:
            m = api("POST", f"/repos/{REPO}/milestones", {"title": title, "description": desc})
            ms[title] = m["number"]
            print(f"  + {title}")

    all_issues = api("GET", f"/repos/{REPO}/issues?state=all&per_page=100&filter=all")
    by_title = {i["title"]: i for i in all_issues}

    print("epics:")
    epic_numbers = {}
    for key, title, milestone, labels, desc in EPICS:
        if title in by_title:
            epic_numbers[key] = by_title[title]
            continue
        issue = api("POST", f"/repos/{REPO}/issues", {
            "title": title, "body": desc, "labels": ["epic"] + labels,
            "milestone": ms[milestone],
        })
        by_title[title] = issue
        epic_numbers[key] = issue
        print(f"  + {title}")

    print("stories:")
    made = 0
    for key, epic in epic_numbers.items():
        for title, labels, do, accept in STORIES.get(key, []):
            if title in by_title:
                continue
            body = f"**Do**\n- {do}\n\n**Acceptance**\n- {accept}\n"
            issue = api("POST", f"/repos/{REPO}/issues", {
                "title": title, "body": body,
                "labels": ["story"] + [l for l in labels if l != "story"],
                "milestone": epic.get("milestone") and epic["milestone"]["number"],
            })
            by_title[title] = issue
            made += 1
            api("POST", f"/repos/{REPO}/issues/{epic['number']}/sub_issues",
                {"sub_issue_id": issue["id"]}, allow_fail=True)
            print(f"  + [{key}] {title}")

    # Link the original 27 stories under their epics.
    print("linking existing stories:")
    for title, issue in by_title.items():
        if title.startswith("Epic:") or title in {t for k in STORIES for t, *_ in STORIES[k]}:
            continue
        key = epic_key_for(title)
        epic = epic_numbers.get(key)
        if epic and api("POST", f"/repos/{REPO}/issues/{epic['number']}/sub_issues",
                        {"sub_issue_id": issue["id"]}, allow_fail=True):
            print(f"  ~ {key} <- {title}")

    print(f"\ncreated {made} new stor(ies)")


if __name__ == "__main__":
    main()
