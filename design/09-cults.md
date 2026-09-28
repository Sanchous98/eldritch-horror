# Cults

Factions the player can join or oppose. Reputation is per-player, ranges `−100…+100`,
and opposed pairs mean favouring one costs another.

| id | Name | Domain | Opposed to | Fantasy |
|---|---|---|---|---|
| `drowned_choir` | The Drowned Choir | Coast, sunken ruins | `unblinking_eye` | Tide-worshippers who trade breath for devotion. |
| `unblinking_eye` | The Order of the Unblinking Eye | Libraries, observatories | `drowned_choir` | Scholars who catalogue the horror to survive it. |
| `hollow_choir` | The Hollow Choir | Deep places, rifts | *none (endgame)* | The cult that wants the horror summoned. |

## The Drowned Choir

- **Domain:** coastlines, drowned temples, tide pools.
- **Services:** water breathing, sunken-ruin access, reagents from the deep.
- **Ranks:** Acolyte → Tidebound → Deep-Sworn → Voice of the Choir.
- **Demands:** offerings of fish/pearls; perform rites at low tide.
- **Taboos:** never harm a tide-priest; never douse a ritual flame.
- **Signature rite:** `drowned_blessing`.

## The Order of the Unblinking Eye

- **Domain:** libraries, observatories, warded vaults.
- **Services:** translations, artefact identification, cleansing rites (costly).
- **Ranks:** Initiate → Witness → Archivist → Keeper.
- **Demands:** donate tomes; report rifts; never perform summoning rites.
- **Taboos:** do not read the *Hollow* texts; do not traffic with the Choir.
- **Signature rite:** `ward_of_the_eye`.

## The Hollow Choir

- **Domain:** deep places, rifts, the Veil.
- **Services:** the strongest summoning rites, blood magic, corruption gifts.
- **Ranks:** Aspirant → Vessel → Hollowed → Chorus-Speaker.
- **Demands:** sacrifice, open rifts, corruption above `Marked`.
- **Taboos:** none the cult admits to.
- **Signature rite:** `summon_star_spawn` (gates the `summoning` skill).

## Reputation mechanics

- Gains: completing cult quests, offerings, performing their signature rite.
- Losses: aiding an opposed cult, breaking a taboo, killing their members.
- **Opposed pairs:** a gain with one applies a lesser loss to its opposite.
- **Decay:** slow drift toward 0 for inactive players (config).
- **Gates:** services unlock at rank thresholds (e.g. `+40` → Deep-Sworn).

## Open questions

- Whether a player can hold high standing with two non-opposed cults simultaneously.
- Whether the Hollow Choir is recruitable before `Marked` corruption.
