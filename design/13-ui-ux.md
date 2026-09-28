# 13 — UI / UX

What the player sees and reads. Horror works best with **partial information**, but RPG
players expect legibility — this doc resolves that tension.

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
- Sanity and corruption are the two baseline widgets.
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
