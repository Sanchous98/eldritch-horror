# Advancements — the guided quest tracker

The vanilla **Advancements** screen ("Приключения" / "Advancements") is the mod's guided
content tracker. It is deliberately **not** a second quest journal:

- `quest/QuestJournal` remains the authority on quest progress, prerequisites, cooldowns and
  rewards.
- The advancement tree is read-only presentation. Every node uses
  `{"criteria":{"impossible":{"trigger":"minecraft:impossible"}}}` and is granted by the system
  that owns the progress, through `advancement/Advancements.award`.
- Awarding is idempotent (`PlayerAdvancements#award` is safe to call repeatedly), and an
  advancement whose JSON is absent is silently skipped. That means a quest can exist without a
  node, and adding the node later starts working immediately.

The tab root is **Eldritch Horror**. It is granted on login (and refreshed once a minute) so the
guidance is available even on worlds created before the tab existed.

## Tree

75 data files under `src/main/resources/data/eldritch_horror/advancement/`:

```
root ("Eldritch Horror", black_obol, stone background)
└── prologue/
    ├── choose_investigator        (investigators_badge, task)
    └── first_steps                (gate_fragment, task)          quest: first_steps
        ├── investigation/          the 12 investigator starter chains, 2 nodes each
        │   ├── starter_agatha_grave → starter_agatha_vigil
        │   ├── starter_aldous_notes → starter_aldous_survey
        │   ├── starter_amos_tonics → starter_amos_cases
        │   ├── starter_cormac_rope → starter_cormac_trap
        │   ├── starter_eleanor_notes → starter_eleanor_signs
        │   ├── starter_evelyn_credit → starter_evelyn_patron
        │   ├── starter_hazel_quill → starter_hazel_story
        │   ├── starter_jack_trail → starter_jack_stakeout
        │   ├── starter_marion_circle → starter_marion_commune
        │   ├── starter_nikolai_books → starter_nikolai_insight
        │   ├── starter_tom_kit → starter_tom_line
        │   └── starter_vera_chalk → starter_vera_ward
        ├── cities/                 one node per curated contract, plus the capstone
        │   ├── tokyo … nairobi     (24 tasks, city_map)
        │   └── all_contracts       (world_atlas, challenge; all 24)
        └── threats/
            ├── fallen_city         (cursed_charm, goal)
            └── reclaim_the_city    (order_sigil, goal)           quest: reclaim_the_city
        └── first_rite              (chalk_reagent, goal)         quest: first_rite
            ├── investigation/
            │   ├── lorekeeper      (tome_of_the_eye, goal)        quest: lorekeeper
            │   └── lorekeeper_ii   (star_codex, challenge)        quest: lorekeeper_ii
            ├── cults/
            │   ├── sworn_ally      (cult_token, goal)             quest: sworn_ally
            │   ├── drowned_choir/
            │   │   ├── initiate → member → devoted → inner_circle
            │   ├── unblinking_eye/
            │   │   └── initiate → member → devoted → inner_circle
            │   ├── hollow_choir/
            │   │   └── initiate → member → devoted → inner_circle
            │   ├── open_the_way      (ritual_knife, goal)         quest: open_the_way
            │   ├── awaken_the_horror (void_reagent, challenge)    quest: awaken_the_horror
            │   ├── the_decision      (effigy_doll, challenge)     quest: the_decision
            │   └── the_seal          (order_sigil, challenge)     quest: the_seal
            └── threats/first_ancient_one (eye_of_the_watcher, challenge)
```

`cities/all_contracts` is the only node granted by a derived condition rather than a direct
event: after each contract it checks that all 24 `cities/<key>` nodes are already earned.

### ID scheme

| Area | ID |
|---|---|
| Tab root | `eldritch_horror:root` |
| Prologue | `eldritch_horror:prologue/<name>` |
| Investigator chains | `eldritch_horror:investigation/starter_<investigator>_<task>` |
| Lore | `eldritch_horror:investigation/lorekeeper[_ii]` |
| Cults | `eldritch_horror:cults/<cult>/<rank>` |
| Endgame | `eldritch_horror:cults/<quest>` |
| Cities | `eldritch_horror:cities/<city_key>` |
| Threats | `eldritch_horror:threats/<name>` |

A city key is the curated city id without its coordinates:
`tokyo_50881_-12993` → `tokyo`, `new_york_-26936_-14837` → `new_york`, `cape_town_6711_12349`
→ `cape_town`. `Advancements.cityKey` is the single implementation of that rule.

## How awarding works

`com.sanchous98.eldritchhorror.advancement.Advancements` is the only bridge to vanilla:

```java
Advancements.award(player, "prologue/first_rite");          // relative to the mod
Advancements.award(player, "eldritch_horror:threats/fallen_city"); // absolute is fine too
Advancements.awardQuest(player, questId);                   // journal hook
Advancements.awardCityContract(player, city);               // contract + capstone
Advancements.awardCultMilestones(player, cultId, rank);     // rank ladder
```

Hooks (all existing systems, no polling of vanilla criteria):

| Trigger | Location | Advancement |
|---|---|---|
| Journal quest completed (including starter tasks and repeatables) | `QuestJournal.add`, completion block | `awardQuest` → mapped path or `quests/<id>` |
| Curated city contract completed | `CityQuests.reward` | `cities/<city_key>` |
| Cult reputation reaches a rank | `CultSystem.notifyRank` | `cults/<cult>/<rank>` (all rungs up to the reached rank) |
| Investigator chosen at a pedestal | `InvestigatorAPI.grant` | `prologue/choose_investigator` |
| Ancient One killed by a player | `Advancements.onKill` | `threats/first_ancient_one` |
| Player inside a curated city that is `FALLEN` | `CityStateDiscovery` → `Advancements.onCityState` | `threats/fallen_city` |
| Login / once-a-minute refresh | `Advancements.onLogin` / `onServerTick` | `root` |

Quest paths are explicit for the relocated chapters (`first_steps`, `first_rite`, starter tasks,
lorekeeper, endgame, `reclaim_the_city`). Everything else falls back to
`eldritch_horror:quests/<questId>`, so a new quest needs only a matching JSON file. A quest
without a file is ignored; it never blocks completion or rewards.

## Adding an advancement

1. Put a JSON file under `src/main/resources/data/eldritch_horror/advancement/<chapter>/<name>.json`:

```json
{
  "parent": "eldritch_horror:prologue/first_steps",
  "display": {
    "icon": { "id": "eldritch_horror:black_obol" },
    "title": { "translate": "advancement.eldritch_horror.example.title" },
    "description": { "translate": "advancement.eldritch_horror.example.description" },
    "frame": "task",
    "show_toast": true,
    "announce_to_chat": true,
    "hidden": false
  },
  "criteria": {
    "impossible": { "trigger": "minecraft:impossible" }
  }
}
```

Only a root (`parent` absent) may have `"background"`; in 26.3 it is an asset id without
`textures/` or `.png`, e.g. `"minecraft:gui/advancements/backgrounds/stone"`.

2. Add `advancement.eldritch_horror.example.title` / `.description` to **both**
   `assets/eldritch_horror/lang/en_us.json` and `ru_ru.json`. Quest-backed nodes should reuse
   `quest.eldritch_horror.<quest>` and `.desc` instead of duplicating the wording.
3. Call `Advancements.award(player, "<chapter>/<name>")` from the system that owns the event.
4. Validate the static tree:

```bash
python3 tools/gen_advancements.py --check
```

The generator (`tools/gen_advancements.py`) owns the checked-in tree and the city/rank/lang
tables. Regenerating rewrites the 75 advancement files and merges missing language keys; edit the
generator when a branch changes, then run `python3 tools/gen_advancements.py`.

## Design notes

- Icons come from the mod's own items (`black_obol`, `silver_reagent`, `blood_offering`,
  `star_reagent`, `chalk_reagent`, signature items, `city_map`, `world_atlas`, …).
- Frames carry weight: `task` for a step, `goal` for a chain milestone, `challenge` for a capstone,
  endgame node or the first Ancient One kill.
- The tree never grants rewards. Skill points, items, reputation and journal state are paid only
  by `QuestJournal` / `CityQuests` / `CultSystem`; the advancement screen only tells the player
  what to do next.
- The journal stays authoritative: an advancement cannot be completed by a vanilla criterion, and
  a quest can never be completed by an advancement.
