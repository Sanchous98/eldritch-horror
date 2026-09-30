# 01 — Overview

**Eldritch Horror** is an RPG-flavoured horror mod for Minecraft **26.3 / NeoForge**.
It is not a monster pack: it is a progression game built on three resources and one
engine, wrapped around a cosmic-horror fantasy.

## The pitch

You are a person in a world where something vast and indifferent has noticed the world.
There are three ways to meet it:

- **Understand it** — study, investigate, ward it off. Costs you your mind, slowly.
- **Bargain with it** — join the cults, perform the rites. Costs you your humanity, permanently.
- **Become it** — the endgame. The Hollow Choir's path.

Sanity is what you spend moment to moment. Corruption is what you spend forever.
Reputation is who will still talk to you. Rituals are how you move forward.

## The three axes

| Axis | Nature | Recovery | Gates |
|---|---|---|---|
| **Sanity** | Short-term, regenerating | Rest, light, items | Madness effects, hallucinations |
| **Corruption** | Long-term, near-permanent | Rare, costly cleansing rites | Dark rites, endgame paths |
| **Reputation** | Per-cult standing | Quests, offerings, time | Services, ranks, dialogue |

## The one engine

**Rituals** turn resources into progression: a rite consumes offerings and produces
outcomes (items, entities, rifts, skills, reputation). Cults teach rites; items enable
them; the map hides them; quests point at them.

## Loop

```
Explore / investigate  ->  gain knowledge & reagents
        |                          |
        v                          v
   Sanity drains            perform Rituals  ->  Corruption rises
        |                          |                  |
        v                          v                  v
   Rest / ward / item      unlock content      unlock dark paths
        |                          |                  |
        +-------------> progression / endgame <------+
```

## What it is not

- Not a jump-scare mod: dread is a *system* (drain, information, consequence).
- Not a power fantasy: every strong option has a cost on an axis.
- Not a mob roster: creatures serve the systems, not the reverse.
- **Not a port of the board game.** See *Lineage* below.

## Lineage — an analogue, not a port

This mod is an **analogue of *Eldritch Horror* (Fantasy Flight Games, 2013) in spirit**, not a
translation of its rules. What we keep is the *fantasy*: a world noticed by something vast, a
cast of cults, globe-spanning investigation, mythos escalation, and power that always costs.

What we **deliberately discard** is everything that only exists to make a *board game* work:

| Board game (Eldritch Horror) | This mod (RPG) |
|---|---|
| Rounds, 2 actions/round | Continuous play; no turn economy |
| Dice checks (Will/Strength/Influence…) | Continuous meters, thresholds, effects |
| Fixed board of spaces | A real, generated Earth (see `20-map.md`) |
| Decks: Mythos, Encounters, Assets, Conditions | Ambient systems, loot, world events |
| Tokens: Clue, Omen, Doom, Eldritch | Meters, knowledge, world state |
| "Rest = +1 Health/Sanity" action | Passive recovery near light/settlements |
| No food track | **No food track either** — sanity fills that role |

So the design is **RPG-shaped**: persistent meters (sanity → corruption → reputation), continuous
drain/recovery, data-driven rituals/items, and a world that remembers — never a deck-and-dice loop.
When a board-game idea is referenced, treat it as *inspiration for a feeling*, and re-express it as
a continuous mechanic. Do not import round/action/dice/token mechanics.

## Reading order

This folder is the source of truth. `design/README.md` is the full index. A new
contributor should read **04-pillars** → **02-progression** → the three **03-axes**
docs → **05-ritual-engine** → **06-factions**.
