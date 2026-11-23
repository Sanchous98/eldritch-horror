# City contracts — guidance and flow

The runtime for the 3–5 step contract every curated city offers
(`world/city/quest/CityQuests`, `CityQuestChain`, `ContractWaypoint`, `ContractCompass`).
Authored anchors live in the city plans (`docs/CITY-PLAN-CONTRACT.md`); this page covers how the
player is guided, not how the cities are built.

## Shape of a chain

1. **TALK — the contact.** The contract starts when the player enters the city, and the first
   objective is the NPC who gives it. Standing at that NPC advances the chain and the contact
   speaks the next task in chat.
2. **GATHER** — carry N of a reagent from the district pool.
3. **HUNT** — kill N district threats inside the city.
4. **SECRET** — reach the authored hidden place (or the plan's far landmark).

A plan without an authored NPC still works: the notice board becomes the giver and the first step
is `BOARD`. The board is otherwise scenery and rumour — it never gates the chain.

## Locator bar (primary guidance)

`ContractWaypoint` keeps a gold dot on the vanilla locator bar (gamerule `locatorBar`). It sends
`ClientboundTrackedWaypointPacket` only to the contract owner's connection:

| Moment | Packet |
|---|---|
| contract start / first session tick | `addWaypointPosition(uuid, goldIcon, objective)` |
| every step advance | `updateWaypointPosition(uuid, goldIcon, objective)` |
| contract complete | `removeWaypoint(uuid)` |

The uuid is stable per city contract:
`UUID.nameUUIDFromBytes("eh_cityquest:" + city.id())`. The position is the objective’s X/Z and the
surface Y (`Heightmap.MOTION_BLOCKING_NO_LEAVES`), so the dot stays on the street even for an
underground secret. The icon uses `WaypointStyleAssets.DEFAULT` with a gold RGB (`0xFFAA00`).

`ContractWaypoint` caches the last (city, step) sent per player so the 1 Hz contract ticker does
not re-send an unchanged dot; the cache is cleared on logout, and the next tick re-sends
`addWaypointPosition` for a fresh session.

`GATHER` and `HUNT` steps have no place of their own, so their dot sits on the city centre (the
district). When the contact hands over one of those tasks, the spoken line names the task and the
bearing to the district; for a `BOARD`/`SECRET` hand-off it uses the normal place-and-bearing
description.

## Text hints

`CityQuests.describe` / `objective` / `direction` / `bearing` are the spoken guidance:

- place names combine the authored POI with the nearest authored `LANDMARK`
  (`"imam at the Great Mosque"`, `"necropolis at the Arrival Gate"`);
- a notice board names its own location (`"bazaar"`);
- `describe` appends the eight-way bearing and rounded distance from the city centre;
- the locator-bar / compass lore uses the bearing **from the player**, which is the practical
  direction once inside the streets.

All strings are in `assets/eldritch_horror/lang/{en_us,ru_ru}.json` under
`cityquest.eldritch_horror.*`.

## Optional extras

Both are off by default; the locator dot is the primary marker.

| Config | Default | Effect |
|---|---|---|
| `enableCityQuestCompass` | `false` | Gives a marked `minecraft:compass` (`eh_contract_compass=1` in `CUSTOM_DATA`) whose `LODESTONE_TRACKER` needle points at the objective. It is reused per player (never duplicated), retargeted on every step, removed on completion, and kept plain with `ENCHANTMENT_GLINT_OVERRIDE=false`. |
| `enableCityQuestBeacon` | `false` | While a `BOARD`/`TALK`/`SECRET` step is active and the player is within 160 blocks, every 40 ticks sends 2–4 `SOUL_FIRE_FLAME`/`END_ROD` particles in a 1×3 column at the objective, visible to the owner only. |

The compass tracker is created with `tracked=false`: vanilla clears a tracked lens when no
lodestone POI exists at the target, which would silently blank the needle. Non-tracked lenses
still read their `target` for the needle, so the objective survives.

## What is untouched

The journal and the advancement tree are unchanged. `CityQuests.reward` still calls
`Advancements.awardCityContract(player, city)` on completion; no board or intermediate step is an
advancement node.
