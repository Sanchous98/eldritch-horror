# Concurrent independent sessions

Decision: a **session is a whole independent world** — its own cities, bosses, quests, instance
rolls and player list. Sessions run at the same time on the same host, each behind its own port,
each with its own player cap. Nothing gameplay-relevant is shared between them.

## Why processes, not dimensions

Minecraft has one overworld per server process. Dimensions share the world's saved state, players
can move between them, and there is one set of bosses and one city ledger. That is the opposite of
"independent session". A second overworld therefore means a second process with its own world
directory — the only model that gives real isolation.

## Topology

```
template world (pristine, never played)
        │  cp -a on provisioning
        ├── session "s1"  world + config + port 25565  max-players 8
        └── session "s2"  world + config + port 25566  max-players 8
```

- **Template**: the clean merge (`/home/opc/eh-run/world-template`). It is the restore point and
  the source of every new session. It is never started, so it never diverges.
- **Session**: a copy of the template, its own run directory, its own `server.properties` port and
  `max-players`, its own log, its own world.
- **Player cap**: `max-players` on the session's own port. A full session refuses further joins;
  players connect to another port.
- **No proxy required**: each session is reachable directly at `host:<port>`. A proxy can be added
  later for a single entry point and routing, but it is not needed for isolation.

## Session seed and randomness

Each process draws its own session seed at boot (not saved). Instances, dungeon rolls and whisper
targets are derived from it, so the two sessions see different dungeons even though they started
from the same template. The seed is logged once at startup.

## Resource budget

Per session on the current host:

| Resource | Budget | Why |
|---|---|---|
| Heap | 4–5 GB (`-Xmx`) | 2 sessions must fit in 12 GB together with the OS |
| View distance | 6–8 | measured: 4 players at 8 cost ~20 ms/tick |
| Disk | 46 GB per world | city chunks; the template adds one more copy |
| Ports | one per session | 25565, 25566, … |

Current host (2 vCPU / 12 GB / 200 GB, Ampere A1 free tier):

| Copies | Disk | RAM at 2 sessions | Verdict |
|---|---|---|---|
| template + 2 sessions, reflink | ~46 GB + deltas | ~10 GB | **fits, this is the ceiling** |
| template + 2 sessions, plain copies | 138 GB | ~10 GB | fits, tight |
| template + 3 sessions, plain copies | 184 GB | >12 GB | does not fit |

So the free-tier host runs **two** concurrent sessions. More sessions need more RAM and disk (a paid
shape or a second host); the mod does not change.

## Provisioning a session

1. `cp -a --reflink=auto world-template worlds/<name>`. On the current XFS volume reflinks are
   supported, so a fresh session costs kilobytes and only diverging chunks take space; on a
   non-reflink filesystem the same command makes a plain 46 GB copy.
2. Write `server.properties`: unique `port`, `max-players`, `view-distance=8`,
   `simulation-distance=6`, same `level-name`.
3. Same `eh-map.json` and mod config; the session seed is per process.
4. Start the process with `-Xmx4g` (or 5g when only one session runs).
5. Announce `host:<port>` and cap.

## Restore and backups

- Restoring a session = stop it, delete its world, copy the template again. That is the whole
  procedure; nothing session-specific lives outside the world directory.
- Archiving a played session = stop it, tar its world aside, then remove it.
- The template must never be started: starting it would write `level.dat`/`session.lock` and
  change it. All play happens on copies.

## Operational rules

- A session's player cap is enforced at the port, not by the mod.
- The scheduler/timetable, trains and ships stay process-local, so a restart only affects that
  session.
- Cross-session chat and presence do not exist by design; if wanted later, that is a proxy feature.
