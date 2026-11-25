# Cult portals

Status: design decision, not implemented.

## Two kinds of portal

The same frame-and-plane mechanic serves two different outcomes, and the difference must be
readable at a glance:

| | Dungeon portal | Cult portal |
|---|---|---|
| Who opens it | a player, with the family igniter | cultists, through a rite |
| Where it leads | a session instance behind the frame | **into the world**: an Ancient One steps out |
| Colour | per family (green, cyan, amber, violet, indigo) | blood red to void black, pulsing |
| Can players enter | yes | no — the plane is hostile; it pushes back and drains sanity |
| Lifetime | permanent frame, rerolled instance | temporary, collapses after the summoning |

The Ancient One must arrive **in the world**, not in an instance: distance, the timetable, the city
and the other players only mean something if the presence is in the same Earth everyone shares.

## The ritual

- A cult site (altar + offerings) grows through visible stages as city corruption rises:
  whispers and graffiti → gatherings → the frame is built → the plane lights → the sky and the
  city state darken (BESIEGED, then FALLEN).
- The rite has a **countdown the player can see and interrupt**. This is the whole point: the
  summoning is a threat with a window, not a random spawn.
- Ways to stop it: kill the ritual leader, break the frame, extinguish the plane with the counter
  rite, or cleanse the site. Each stage has its own answer; waiting until the plane is fully lit
  leaves only the hardest one.
- If it completes, the plane opens and the Ancient One enters at city scale. The frame collapses
  behind it. The city is now a boss arena and the dread layer covers it as already implemented.

## Why this replaces the spontaneous spawn

The current site trigger spawns a presence when a player is near a site. That reads as a random
ambush. A cult portal gives the same event a build-up, a visible cause and a player-authored
outcome: the city falls because the players did not stop the rite, not because a dice roll said so.
The site trigger becomes the ritual-site watcher (who is gathering, how far along the rite is), not
a spawner.

## Rewards and consequences

- **Stopped**: reputation with the city's factions, cult loot, reduced taint, a saved city.
- **Completed**: a city-scale Ancient One in the world, a fallen city, and a fight that the travel
  timetables now make urgent. Solvable by kill or by rite, as bosses already are.
- The portal leaves a scar (a ruined frame) so the city remembers what happened.

## Implementation hooks

- Reuse the portal block/frame code from the dungeon families, with a hostile "cult" plane that
  rejects players.
- `world/cult/CultRituals` owns the stages and the countdown; `CthulhuSiteTrigger` becomes the
  watcher that starts it.
- The boss enters through the plane (`AncientOne` spawn at the frame, city-scale size already in
  `AncientOneSizes`), then the frame collapses.
- City state and taint react through the existing `CityDecay` / `CityFall` path.
