# Testing the mod in-game

There are **no automated gameplay tests** — the mod has no GameTest suite, and the container has no
display, so client-side behaviour (HUD, map, tooltips) can only be checked by a human in a real
client. This doc is the manual playbook: how to launch, what commands exist, and a recipe per system.

> After any code change, the dev image bakes the source — run `docker compose build dev` first, or
> the container runs the previous build (see `AGENTS.md`, "Build discipline").

## 1. Two ways to run

### A. Dev client with a window (visual checks)
Needs X11/Wayland + GPU. This is the only way to see the HUD, the world map, tooltips and the
Threshold hub.

```sh
xhost +si:localuser:$(id -un)
docker compose --profile gpu run --rm dev-gpu ./gradlew runClient
```

`dev-gpu` does **not** set `EH_DEV`, so for commands create the singleplayer world with **Allow
Cheats: ON** (that grants operator), or launch with `EH_DEV=true` in the environment.

### B. Dedicated dev server + your own Minecraft 26.3 client
```sh
docker compose --profile server up server    # then Direct Connect -> localhost:25565
```
The `server` profile sets `EH_DEV=true`, so the **first player to join is auto-opped** (`core/DevTools`).
World + `server.properties` persist in the `run-data` volume (`/workspace/run`).

## 2. Quick start

```
/gamemode creative
/give @s eldritch_horror:world_atlas          # fullscreen map (right-click)
/eh investigator set eleanor_vance            # pick one; signature items need it
/give @s eldritch_horror:seers_lens           # Eleanor's signature item (right-click = active)
```
The mod's **creative tab** holds all ~220 items, the 12 signature items, and 22 spawn eggs (all 20
Ancient Ones + 2 others). Spawn eggs are the main tool for the bestiary.

## 3. `/eh` command reference (exact ids)

| Command | Effect |
|---|---|
| `/eh sanity get\|set\|add <v>` | sanity meter (bands `Composed → Uneasy → Fraying → Breaking → Marked`) |
| `/eh corruption get\|set\|add <v>` | corruption meter (bands `Dormant → Touched → Marked → Claimed`) |
| `/eh taint get\|set\|add\|purify` | per-chunk taint under the caller |
| `/eh rep get\|set\|add\|list` | cult reputation (−100…+100) |
| `/eh rite <id>` / `/eh rites` | perform a rite / list known ids |
| `/eh investigator get\|set <id>` | show / debug-reset investigator |
| `/eh cult <id>` / `/eh service <cult> <service>` | cult info / perform a service |
| `/eh codex` / `/eh codex learn <id>` / `/eh codexentries` | codex |
| `/eh event <id>` / `/eh events` | force a world event / list active |
| `/eh home` | debug teleport to the chosen starting city |
| `/eh city` | state of the nearest curated city |

### Rite ids (28)
`ward_of_the_eye`, `drowned_blessing`, `call_the_lesser`, `summon_star_spawn`, `open_rift`,
`close_rift`, `rite_of_cleansing`, `respec` — plus the Ancient One solves (one per presence):
`soothe_cthulhu`, `draw_away_dunwich`, `still_shub_niggurath`, `still_azathoth`, `seal_the_gate`,
`ward_ithaqua`, `appease_yig`, `bind_atlach_nacha`, `deny_nyarlathotep`, `quench_cthugha`,
`still_glaaki`, `sever_hydra`, `bar_nyogtha`, `topple_idol`, `silence_hastur`,
`unmake_nephren_ka`, `seal_abhoth`, `starve_chaugnar_faugn`, `quench_tulzscha`,
`erase_zstylzhemghi`.

### Investigator ids (12) → signature item
`eleanor_vance`→`seers_lens`, `jack_corrigan`→`investigators_badge`, `tom_mallory`→`dog_tags`,
`cormac_blackwood`→`smugglers_token`, `sister_agatha`→`rosary`, `marion_delacroix`→`planchette`,
`vera_nightingale`→`blood_chalice`, `nikolai_volkov`→`forbidden_grimoire`,
`dr_amos_hartley`→`sedative_vial`, `evelyn_ashcombe`→`family_signet`,
`aldous_pemberton`→`antiquarians_compass`, `hazel_quinn`→`press_pass`.

### Cult / event ids
- Cults: `drowned_choir`, `unblinking_eye`, `hollow_choir`.
- Services: `teach_drowned_blessing`, `teach_ward_of_the_eye`, `teach_call_the_lesser`,
  `teach_summon_star_spawn`, `teach_open_rift`, `cleansing`.
- Events: `whisper`, `darkness_pulse`, `rift_bloom`, `veil_thin`, `hallucination_wave`,
  `cleansing_dawn`, `cult_procession`, `blood_moon_rite`, `star_fall`, `hollow_call`.
- Sites: `black_pyramid`, `cult_stronghold`, `drowned_temple`, `order_vault`, `rift_scar`,
  `ritual_altar_site`, `spawning_pool`, `temple_of_the_feaster`, `yellow_court`.

## 4. Testing recipes

- **Prologue / hub.** Only a first-time player (no `PROLOGUE_DONE`) enters the Threshold. There is no
  in-game reset — test on a fresh world or a new player name, or set `enablePrologue=false` and use
  `/eh investigator set` directly.
- **Rites + offerings.** Rites now **consume reagents** (all-or-nothing). For a fast sweep of all 28,
  set `requireRiteReagents=false` in the config (see §6) and use `/eh rite <id>`. With it on, put the
  listed reagents in your inventory; the altar lists each known rite's offerings on right-click.
- **Sanity / corruption.** `/eh sanity add -40` and watch the HUD stage + the `madness`/`marked`
  effects; `/eh corruption add 40` for `corrupted`. Darkness drains; a city recovers.
- **City state.** Stand in a city, `/eh taint add 0.5`, then `/eh city` → `besieged`/`fallen`;
  observe the recovery rate drop (Dr. Amos keeps +50% while Thriving).
- **Events.** `/eh event rift_bloom` sends a chat line (`enableEventNotifications`) and applies its
  effect; `/eh events` lists active. (`rift_bloom` needs a rift nearby, or it ends immediately.)
- **Bestiary / Ancient Ones.** Spawn eggs; each Ancient One has a corruption/sanity aura and a
  non-combat solve — learn the matching rite from a tome, then `/eh rite soothe_<name>` near it.
- **Rites that place world features.** `open_rift`/`close_rift` place a `rift_anchor` and move taint.
- **Cults.** `/eh rep set drowned_choir 60` then `/eh service drowned_choir cleansing` (needs the
  rank). Interact with a `worshipper` for the read-only report.
- **Aldous loot passive.** Be `aldous_pemberton`, kill a mob → 35% chance of a bonus drop.
- **Morok (poles).** Fly past `|z| > 32768` → escalating darkness/nausea/damage + sanity drain.
- **Map.** `world_atlas` right-click (works while pointing at a block); markers = you, home city, all
  cities, codex-discovered sites.
- **Taint world change.** `enableTaintWorld=true`, taint a chunk past `taintWorldThreshold`, watch a
  few natural surface blocks convert; `/eh taint purify` reverts.

## 5. Dev system properties

Set on the run (e.g. `JAVA_TOOL_OPTIONS`/`GRADLE_OPTS`, or `programArguments` in `build.gradle`):

- `-Deh.dev=true` / `EH_DEV=true` — auto-op the first joining player.
- `-Deh.debugCity=Rio` — force-load one curated city at server start.
- `-Deh.forceload=order_vault:96,ritual_altar_site:96` — force-load a disc (radius optional, default
  96) around named locations.
- `-Deh.renderCities=all` (or `sites`, or a name list) — server-side PNG previews to `run/render/`
  (no client needed); `-Deh.renderExit=true` shuts down when the slice finishes.

## 6. Config

Config is the **`SYNCED`** type. It defaults to `config/eldritch_horror-synced.toml` (a
`<world>/syncedconfig/` file overrides it). It holds hundreds of toggles:

- Master switches: `enableSanity`, `enableCorruption`, `enableEvents`, `enablePrologue`, `enableCodex`.
- Testing aids: `requireRiteReagents`, `ritualCooldownTicks`.
- Per-content gates: `enableSanity<X>`, `enableCorruption<X>`, `enable<X>Spawns`, `enableSite<X>`,
  `enableRite<X>`, each event's `enableEvent<X>`, city-state thresholds (`cityStateUneasyTaint`, …).

## 7. What cannot be verified here

- **Client rendering** (HUD, map layout, tooltips, hub geometry): only via `runClient`; the container
  has no display. Compile + dedicated-server smoke is the ceiling in CI.
- **Automated regression**: none. GameTest is configured (`gameTestServer`) but no test class exists.
