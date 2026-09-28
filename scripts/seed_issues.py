#!/usr/bin/env python3
"""Seed GitHub issues (epics + stories) for eldritch-horror.

Idempotent: skips any issue whose title already exists. Uses the GitHub REST API
with a token from ~/.config/gh/hosts.yml (same source the other tooling uses).
"""
import json
import re
import subprocess
import sys
import urllib.request

REPO = "Sanchous98/eldritch-horror"
API = "https://api.github.com"


def token():
    with open("/home/dev/.config/gh/hosts.yml") as f:
        m = re.search(r"oauth_token:\s*(gho_\S+)", f.read())
    if not m:
        sys.exit("no oauth token found in ~/.config/gh/hosts.yml")
    return m.group(1)


TOKEN = token()


def api(method, path, data=None):
    body = json.dumps(data).encode() if data is not None else None
    req = urllib.request.Request(
        API + path,
        data=body,
        method=method,
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
        sys.exit(f"{method} {path} -> {e.code}: {e.read().decode()}")


# ---------------------------------------------------------------- labels
LABELS = {
    "epic": ("5319e7", "A large body of work that can be broken into stories"),
    "story": ("0e8a16", "A unit of deliverable work"),
    "build": ("1d76db", "Build, toolchain, CI"),
    "sanity": ("6f42c1", "Sanity RPG axis"),
    "corruption": ("8a2be2", "Corruption RPG axis"),
    "cult": ("c2e0c6", "Cults / factions / reputation"),
    "ritual": ("b60205", "Forbidden rituals engine"),
    "content": ("fbca04", "Blocks, items, entities, sounds, art"),
    "client": ("f9d0c4", "Client rendering, HUD, overlays"),
    "data": ("006b75", "Datapack / data-driven systems"),
    "networking": ("d4c5f9", "Packets / client-server sync"),
    "tech-debt": ("e6e6e6", "Internal quality, refactoring"),
}

# ---------------------------------------------------------------- milestones
MILESTONES = [
    ("M1 — Foundations", "Build green, registries proven, client boots with an empty mod."),
    ("M2 — Sanity", "The first RPG axis: meter, drain/regen, madness thresholds, HUD."),
    ("M3 — Corruption", "Durable exposure axis: per-player + per-chunk, block conversion."),
    ("M4 — Rituals & Cults", "Data-driven rituals, cult factions, reputation, altar + ritual slice."),
    ("M5 — Content & Endgame", "Bestiary, the horror, endgame paths, polish."),
    ("Backlog", "Undecided, research, or nice-to-have."),
]

# ---------------------------------------------------------------- issues
# (title, milestone, labels, body)
ISSUES = [
    # ------------------------------------------------ M0 build / harness
    (
        "Add a GitHub Actions release workflow (tag -> jar + release)",
        "Backlog",
        ["build", "story"],
        "`build.yml` compiles on push/PR but nothing publishes an artifact for players.\n\n"
        "**Do**\n"
        "- On `v*` tags: run `./gradlew build`, create a GitHub Release, attach `build/libs/*.jar`.\n"
        "- Optionally generate a changelog from commits.\n\n"
        "**Acceptance**\n"
        "- Pushing a tag produces a Release with the mod jar attached.",
    ),
    (
        "Add an access transformer config",
        "M1 — Foundations",
        ["build", "story"],
        "Some vanilla members we will need (e.g. certain `Player`/`MobEffect` internals) are not public.\n\n"
        "**Do**\n"
        "- Create `src/main/resources/META-INF/accesstransformer.cfg` (empty is fine to start).\n"
        "- Declare it in `neoforge.mods.toml` only if the auto-detection path isn't enough.\n\n"
        "**Acceptance**\n"
        "- File exists, build still passes, and at least one widened member is used or documented.",
    ),
    (
        "Add the mod logo / icon",
        "M1 — Foundations",
        ["content", "story"],
        "The mods list shows a placeholder.\n\n"
        "**Do**\n"
        "- Add `src/main/resources/eldritch_horror.png` (128x128).\n"
        "- Uncomment `logoFile` in `src/main/templates/META-INF/neoforge.mods.toml`.\n\n"
        "**Acceptance**\n"
        "- In-game mods list shows the icon.",
    ),

    # ------------------------------------------------ 1.1 attribute registration
    (
        "Register the Sanity attributes",
        "M2 — Sanity",
        ["sanity", "story"],
        "Sanity needs to be modifiable by other mods/equipment.\n\n"
        "**Do**\n"
        "- Add a `ModAttributes` DeferredRegister for `attributes`.\n"
        "- Register a `max_sanity` `Attribute` (`RangedAttribute`, default 100, range 0..100).\n"
        "- Register it from `EldritchHorror`.\n\n"
        "**Acceptance**\n"
        "- `eldritch_horror:max_sanity` appears in the attribute registry and on players.\n"
        "- `/attribute` can read it.",
    ),
    (
        "Sanity state storage + client sync",
        "M2 — Sanity",
        ["sanity", "networking", "story"],
        "We need per-player *current* sanity (attributes only give the max) and it must reach the client.\n\n"
        "**Do**\n"
        "- Add a `SanityAttachment` (`AttachmentType`) holding current/max sanity.\n"
        "- Add an S2C packet to sync it; send on change and on player join/respawn.\n"
        "- Decide and document whether to use NeoForge's automatic attachment sync or a manual packet.\n\n"
        "**Acceptance**\n"
        "- A debug command can set sanity and every client sees it after a reconnect.",
    ),
    (
        "Sanity source SPI (composable drain/regen)",
        "M2 — Sanity",
        ["sanity", "tech-debt", "story"],
        "Hard-coding every drain rule in one tick handler will rot.\n\n"
        "**Do**\n"
        "- Define a `SanitySource` interface: `tick(Player, SanityState) -> delta`.\n"
        "- Register sources so content can add its own.\n\n"
        "**Acceptance**\n"
        "- At least two sources registered; a source can be added without editing core.",
    ),
    (
        "Sanity drain/regen rules",
        "M2 — Sanity",
        ["sanity", "story"],
        "Implement the baseline rules from `docs/DESIGN.md`.\n\n"
        "**Rules**\n"
        "- Daylight/lit: +regen; darkness: drain (doubled underground); sleeping: burst regen.\n"
        "- Near altar: drain. Nearby rift: scaled drain. Seeing an eldritch entity: burst + sustained.\n"
        "- Reading a forbidden tome: burst drain + corruption.\n\n"
        "**Acceptance**\n"
        "- Rules are config-gated and their rates come from config, not magic numbers.",
    ),
    (
        "Madness thresholds and effects",
        "M2 — Sanity",
        ["sanity", "content", "story"],
        "Sanity must *do* something at the low end.\n\n"
        "**Do**\n"
        "- Register a `Madness` mob effect.\n"
        "- <40: whispers + screen pulse; <20: `Madness`, periodic hallucinations; 0: `Marked`.\n\n"
        "**Acceptance**\n"
        "- Crossing each threshold is observable and reversible on recovery.",
    ),
    (
        "Sanity HUD meter",
        "M2 — Sanity",
        ["sanity", "client", "story"],
        "Players need to see sanity (design open-question #1: always vs. below a threshold).\n\n"
        "**Do**\n"
        "- Render a meter from the synced value; decide visibility policy and document it.\n\n"
        "**Acceptance**\n"
        "- Meter matches the server value; no flicker; respects the chosen visibility policy.",
    ),

    # ------------------------------------------------ corruption
    (
        "Corruption stage model + per-player storage",
        "M3 — Corruption",
        ["corruption", "story"],
        "Corruption is the durable cost axis.\n\n"
        "**Do**\n"
        "- `CorruptionStage` enum (Dormant/Touched/Marked/Claimed) with ranges.\n"
        "- Per-player value (attachment) + sync.\n\n"
        "**Acceptance**\n"
        "- Stage transitions are testable; high corruption gates later rites.",
    ),
    (
        "Per-chunk corruption field",
        "M3 — Corruption",
        ["corruption", "data", "story"],
        "Environmental corruption drives block conversion and ambient spawns.\n\n"
        "**Do**\n"
        "- Persist a per-chunk value (`LevelChunk` attachment or saved-data map — decide).\n"
        "- Only tick near players; define cleanup for unloaded chunks.\n\n"
        "**Acceptance**\n"
        "- Values survive a reload; no unbounded growth in memory.",
    ),
    (
        "Corruption spread + block conversion",
        "M3 — Corruption",
        ["corruption", "data", "story"],
        "The world should visibly rot.\n\n"
        "**Do**\n"
        "- Spread from rifts/altars; convert a small block set over time.\n"
        "- Gate behind `enableCorruptionSpread`.\n\n"
        "**Acceptance**\n"
        "- A placed rift visibly corrupts its surroundings and stops when disabled.",
    ),

    # ------------------------------------------------ ritual
    (
        "RitualDefinition datapack type",
        "M4 — Rituals & Cults",
        ["ritual", "data", "story"],
        "Rites are data, per `docs/ARCHITECTURE.md`.\n\n"
        "**Do**\n"
        "- Register a datapack registry + `RitualDefinition` (pattern, offerings, conditions, outcomes).\n"
        "- Add a codec + a first JSON under `data/eldritch_horror/ritual/`.\n\n"
        "**Acceptance**\n"
        "- `/reload` loads definitions; invalid JSON fails with a useful error.",
    ),
    (
        "Block-pattern matcher",
        "M4 — Rituals & Cults",
        ["ritual", "story"],
        "Validate the altar + surrounding pattern.\n\n"
        "**Do**\n"
        "- Match `RitualDefinition.pattern` against the world around an altar.\n"
        "- Decide: reuse structure templates vs. a purpose-built matcher (design open-question #3).\n\n"
        "**Acceptance**\n"
        "- Matcher has unit tests for a match, a near-miss, and rotation.",
    ),
    (
        "Ritual resolver + outcomes",
        "M4 — Rituals & Cults",
        ["ritual", "story"],
        "Execute or refuse a rite.\n\n"
        "**Do**\n"
        "- Validate conditions, consume offerings atomically, run registered outcomes\n"
        "  (spawn/grant/transform/curse/open-rift).\n"
        "- On failure: apply the `on_failure` cost (never a silent no-op).\n\n"
        "**Acceptance**\n"
        "- Success and failure paths both have tests; a failed rite costs sanity/corruption.",
    ),
    (
        "Altar block + first playable ritual",
        "M4 — Rituals & Cults",
        ["ritual", "content", "story"],
        "The vertical slice that proves the engine.\n\n"
        "**Do**\n"
        "- A ritual altar block using the eldritch stone family.\n"
        "- One tier-1 ritual end-to-end (teach -> perform -> outcome).\n\n"
        "**Acceptance**\n"
        "- A player can learn and perform the ritual in a dev world.",
    ),

    # ------------------------------------------------ cult
    (
        "CultDefinition datapack type",
        "M4 — Rituals & Cults",
        ["cult", "data", "story"],
        "Cults are data-driven factions.\n\n"
        "**Do**\n"
        "- Register a datapack registry + `CultDefinition` (rites, demands, taboos, ranks).\n"
        "- Seed the three design cults (Drowned Choir, Unblinking Eye, Hollow Choir).\n\n"
        "**Acceptance**\n"
        "- `/reload` loads cults; definitions validate.",
    ),
    (
        "Per-player cult reputation + sync",
        "M4 — Rituals & Cults",
        ["cult", "networking", "story"],
        "Reputation gates content and cults remember.\n\n"
        "**Do**\n"
        "- Reputation storage per player per cult (`-100..+100`) + sync.\n"
        "- Opposed-pair effects (helping one costs another).\n\n"
        "**Acceptance**\n"
        "- A command can shift reputation; opposed pair reacts.",
    ),
    (
        "Cultist NPC base + behaviour",
        "M4 — Rituals & Cults",
        ["cult", "content", "story"],
        "Faction NPCs that trade, teach, and guard.\n\n"
        "**Do**\n"
        "- A cultist entity with goals driven by its `CultDefinition` (not one subclass per cult).\n"
        "- Teach a rite / trade reagents / defend the altar.\n\n"
        "**Acceptance**\n"
        "- A cultist teaches the first ritual and its trades are reputation-gated.",
    ),

    # ------------------------------------------------ content / endgame
    (
        "Rift block + ambient horror events",
        "M5 — Content & Endgame",
        ["content", "story"],
        "The horror is a presence before it is an entity.\n\n"
        "**Do**\n"
        "- A rift block that drains sanity, opens ambient events, and seeds corruption.\n\n"
        "**Acceptance**\n"
        "- A rift is placeable, visible, and drives sanity/corruption sources.",
    ),
    (
        "First eldritch entity (The Watcher)",
        "M5 — Content & Endgame",
        ["content", "story"],
        "An entity that follows and drains rather than fights.\n\n"
        "**Do**\n"
        "- Model, renderer, attributes, spawn rules; drains sanity while in line of sight.\n\n"
        "**Acceptance**\n"
        "- It spawns via a ritual, follows at distance, and is not conventionally killable early.",
    ),
    (
        "The Horror + endgame paths",
        "M5 — Content & Endgame",
        ["content", "story"],
        "The payoff: banish or summon.\n\n"
        "**Do**\n"
        "- Endgame entity + the two endgame ritual paths (design open-question #4).\n\n"
        "**Acceptance**\n"
        "- Both paths are reachable in a dev world and have distinct outcomes.",
    ),
    (
        "Forbidden tomes + knowledge progression",
        "Backlog",
        ["content", "data", "story"],
        "Knowledge-as-progression: reading costs sanity, grants corruption and recipes.\n\n"
        "**Do**\n"
        "- A tome item that teaches a rite / reveals lore and applies the cost.\n\n"
        "**Acceptance**\n"
        "- Reading a tome grants a known rite and costs sanity+corruption.",
    ),
    (
        "Horror soundscape",
        "Backlog",
        ["content", "client", "story"],
        "Whispers, chants, altar hum, entity vocals.\n\n"
        "**Do**\n"
        "- `sounds.json` + registered `SoundEvent`s that `ModSounds` already scaffolds.\n\n"
        "**Acceptance**\n"
        "- Sounds play at the right triggers and are configurable.",
    ),

    # ------------------------------------------------ research / decisions
    (
        "Decide multiplayer semantics for sanity/corruption",
        "Backlog",
        ["sanity", "corruption", "story"],
        "Design open-question #3: shared, averaged, or individual?\n\n"
        "**Do**\n"
        "- Decide and document the model; note impacts on sync and balance.\n\n"
        "**Acceptance**\n"
        "- Decision recorded in `docs/DESIGN.md`.",
    ),
    (
        "Decide sanity storage mechanism (attachment vs capability)",
        "Backlog",
        ["sanity", "story"],
        "Architecture open-question #1.\n\n"
        "**Do**\n"
        "- Confirm NeoForge attachment sync ergonomics; record the choice.\n\n"
        "**Acceptance**\n"
        "- Decision recorded in `docs/ARCHITECTURE.md`.",
    ),
    (
        "Decide ritual matching strategy",
        "Backlog",
        ["ritual", "story"],
        "Architecture open-question #3: structure templates vs. bespoke matcher.\n\n"
        "**Acceptance**\n"
        "- Decision recorded, with consequences for tooling.",
    ),
]


def ensure_labels():
    existing = {l["name"] for l in api("GET", f"/repos/{REPO}/labels?per_page=100")}
    for name, (color, desc) in LABELS.items():
        if name in existing:
            continue
        api("POST", f"/repos/{REPO}/labels", {"name": name, "color": color, "description": desc})
        print(f"  label + {name}")


def ensure_milestones():
    existing = {m["title"]: m["number"] for m in api("GET", f"/repos/{REPO}/milestones?state=all&per_page=100")}
    out = dict(existing)
    for title, desc in MILESTONES:
        if title in out:
            continue
        m = api("POST", f"/repos/{REPO}/milestones", {"title": title, "description": desc})
        out[title] = m["number"]
        print(f"  milestone + {title}")
    return out


def main():
    print("labels:")
    ensure_labels()
    print("milestones:")
    milestones = ensure_milestones()

    existing = {i["title"] for i in api("GET", f"/repos/{REPO}/issues?state=all&per_page=100")}
    created = 0
    for title, milestone, labels, body in ISSUES:
        if title in existing:
            print(f"  skip  {title}")
            continue
        api(
            "POST",
            f"/repos/{REPO}/issues",
            {
                "title": title,
                "body": body,
                "labels": ["story"] + [l for l in labels if l != "story"],
                "milestone": milestones[milestone],
            },
        )
        created += 1
        print(f"  issue + {title}")
    print(f"\ncreated {created} issue(s)")


if __name__ == "__main__":
    main()
