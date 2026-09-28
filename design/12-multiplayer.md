# 12 — Multiplayer & World Rules

How the systems behave with more than one player, and how world-level state is scoped.

## Per-player vs. world state

| State | Scope | Notes |
|---|---|---|
| Sanity | Per player | Individual by default |
| Corruption (player) | Per player | Drives individual stage gating |
| Corruption (chunk) | World / per chunk | Shared environment |
| Reputation | Per player | Cults hold per-player views |
| Known rites / codex | Per player | Knowledge is personal |
| Skill tree / class | Per player | — |
| Rifts | World | Shared, open/close globally |
| Events | World | Affect nearby players |

## Open question — sanity semantics

Three options for sanity in a group:

1. **Individual** (default) — each player's mind is their own. Simple, fair, but lonely.
2. **Averaged** — a shared pool; one player's panic drags the group. Atmospheric, harder.
3. **Shared** — one pool for the party. Strong co-op pressure, punishing.

**Recommendation:** individual for first release; expose a server config for averaged/shared.

## Corruption

- Player corruption is individual; **chunk** corruption is shared, so living in a
  corrupted area costs everyone.
- Cleansing a chunk benefits all; opening a rift hurts all.

## Reputation

- Individual. A cult may welcome one player and attack another.
- Cult events are world-level (a procession is visible to everyone).

## Dedicated servers

- All gameplay must run server-side; the client only renders (HUD, overlays, hallucinations).
- No client-only logic in common code.
- Test on a dedicated server before release (see the *Dedicated server support* story).

## Scaling

- Sanity/corruption difficulty scales with player count only via config, not automatically.
- Event frequency caps are needed for busy servers (`19-events.md`).

## Open questions

- Whether the endgame is per-player or per-world (see `11-endgame.md`).
- Whether chunk corruption should be per-dimension.
