# 13 — UI / UX

What the player sees and reads. Horror works best with **partial information**, but RPG
players expect legibility — this doc resolves that tension.

## Quest journal & skill tree (implemented, server-side)

- **Quest journal** (`quest/`, design/13): a small seed list (`Quests`) whose progress the existing
  systems advance — taking a starting city, performing a rite, sealing a rift, soothing a presence,
  discovering codex entries — stored per player on the synced `QUESTS` attachment and read with
  `/eh quests`. No client screen yet; the data is synced for one.
- **Skill tree** (`skill/`): points are earned by finishing quests and spent on nodes
  (`SKILL_POINTS`, `SKILLS` attachments), read with `/eh skill` / `/eh skill unlock <id>`. The two
  seed nodes have real effects: `lucid_mind` (+10% max sanity, folded into the attribute modifier)
  and `warded_soul` (−10% corruption gain, folded into `Progression`). No client screen yet.

## Screens

| Surface | Purpose | Horror stance |
|---|---|---|
| **Sanity meter** | Track the mind | Shown as a *stage* by default; exact number is an option |
| **Corruption stage** | Track the mark | Stage only; the world shows the number |
| **Codex** | Lore + known rites/reagents | Rewards discovery; hides the unknown |
| **Skill tree** | Spend points | Fully legible |
| **Quest journal** | Track objectives | Fully legible |
| **Dialogue** | Talk to cults | Branches on reputation |
| **Config screen** | In-game tuning | — |

## HUD

- Extensible widget framework so systems register their own meters.
- Sanity and corruption are the two baseline widgets. They **occupy reclaimed vanilla slots**:
  **sanity sits where the food bar was; corruption where the experience bar was**. The vanilla
  food and experience bars are hidden, and their mechanics are disabled (no hunger, no enchanting
  — see `24-sanity-and-corruption.md`). Health and armour bars are untouched.
- The corruption bar fills like experience but is a *cost*, segmenting by stage.
- Event toasts for notable occurrences (rift opened, rank up, nightmare).

## Codex

- Entries unlock by reading, observing, or being taught.
- Categories: cults, rites, entities, reagents, places, events, the horror.
- Unknown content is either hidden or shown as a locked hint (config).

## Information policy

| Question | Decision |
|---|---|
| Show exact sanity? | No by default; stage only; option to show the number |
| Show exact corruption? | No; stage only |
| Show rite recipes when unknown? | No; hints at most |
| Show entity effects? | With the `divination` skill |

## Notifications

- **Diegetic first:** world cues (sound, fog) before UI toasts.
- Toasts for mechanical events (rank, unlock, quest) to keep RPG legibility.

## Keybinds & input

- Rebindable actions (codex, skill tree, ritual selection).
- Controller support where feasible.
- See the *Keybinds & controller support* story.

## Accessibility

- **Photosensitivity:** reduce/disable flashing and distortion.
- **Colour-blind palettes** for corruption/sanity meters and factions.
- **Motion:** reduce camera shake / overlay movement.
- **Text:** UI scale and high-contrast options.
- All effects that could cause discomfort are behind toggles from the start.

## UX principles

- **Teach by consequence, not walls of text.**
- **Never punish without warning:** the endgame fork and taboos are legible.
- **Localization-ready:** every string keyed, no hardcoded text.

## Open questions

- Whether the meter is always visible or fades when composed.
- How much the codex spoils vs. hints.
