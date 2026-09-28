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

## Reading order

This folder is the source of truth. `design/README.md` is the full index. A new
contributor should read **04-pillars** → **02-progression** → the three **03-axes**
docs → **05-ritual-engine** → **06-factions**.
