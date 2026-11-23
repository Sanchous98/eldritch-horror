# Boss combat

Every Ancient One (`entity/AncientOne.java` and its 23 concrete subclasses) fights through the
same server-authoritative ability layer in `entity/combat/`. The old loop — walk up, trade hits —
still exists because melee is unchanged, but it is no longer the whole fight: each presence now
**telegraphs**, **commits**, and can be **interrupted**. The player's answer is movement or burst
damage, and the boss's answer is a phase-gated kit.

This file is the durable reference for the loop, the numbers, and the per-entity table.

**Coverage:** the repo's 24 `extends AncientOne` matches are the `AncientOne` base class itself plus
23 concrete presences. Every one of the 23 is in the table below.

## The loop

`BossCombat` ticks in two places so NoAI never freezes it:

- `AncientOne.customServerAiStep()` → `BossCombat.tickAbilities()` (needs AI; skipped while NoAI);
- `AncientOne.tick()` → `BossCombat.tickMaintenance()` (runs even when NoAI: stagger countdown,
  vulnerability, summon expiry, solve cancellation).

For every attack:

1. **Select.** Only a living, non-creative, non-spectator player in range counts. The controller
   uses the boss's current target, falling back to the nearest player inside its longest ability
   range (Azathoth has no target goals, so this fallback is how it acts at all). An ability is
   eligible when its phase gate is met, its cooldown has elapsed, and its own `canUse` passes.
   Ties are broken by a `RandomSource` seeded from the boss UUID and world tick — never
   `Math.random`, so the same world tick replays the same choice.
2. **Telegraph.** For `telegraphTicks` (minimum 20) the boss stops moving, faces the target, draws
   particles every tick, plays a warning sound at the start, and every player within 32 blocks gets
   an action-bar line naming the attack ("Glaaki's servitors reach for you — move!"). No damage is
   dealt during the wind-up.
3. **Interrupt or commit.**
   - Damage from a player during the wind-up accumulates. At **6% of the boss's max health** the
     attack is cancelled and the boss is **staggered**: NoAI for 40 ticks, a stagger sound/particles,
     and a **3-second vulnerability window** (+25% damage taken, applied in
     `AncientOne.hurtServer`). This is the burst answer.
   - If the player leaves (range ×1.5) or dies, the wind-up is cancelled with a short recovery.
   - Otherwise `execute()` lands **once** at the end of the wind-up.
4. **Recover.** 20–40 ticks (deterministic random) before the next ability, so melee and movement
   still matter between casts.

**Phase ladder.** The base `AncientOne.phaseFor` is now the shared three-band ladder for every
boss (some classes used to collapse it; the ability layer needs the middle band):

| Phase | Health | Gate |
|---|---|---|
| `DORMANT` | above 50% | first ability |
| `STIRRING` | 50% and below | second ability unlocks; first remains available |
| `ENRAGED` | 25% and below | third ability unlocks; **cooldowns ×0.7**, **telegraphs ×0.8** |

The Horror is the exception: its phase rides its escalation stage (ATTENTION → DORMANT/NoAI,
APPROACH → STIRRING, GRASP and DECISION → ENRAGED), because it is invulnerable and never loses
health.

**Summons.** Abilities summon only existing mod entities, with
`EntitySpawnReason.MOB_SUMMONED`, never `setPersistenceRequired()`. `BossCombat` tracks their UUIDs
and discards them after **1200 ticks** (60s), or immediately when the boss dies. A summon that
cannot find a loaded, two-block air pocket with a floor simply does not spawn; the ability's damage
still lands.

**Damage source.** Ability damage uses `level.damageSources().mobAttack(boss)`, so armour,
enchantments, wards and death messages behave exactly as they do for a melee swing. Numbers below
are pre-armour. The target is a full iron kit: roughly 40–60% is absorbed, so a single 9-damage
strike is about 4–5 hearts, and a player survives two or three committed hits but cannot ignore the
boss. Farm bosses deliberately sit at the lower end (4–7 damage, 20–24 tick telegraphs).

**Hitbox-aware reach.** The model pass scales Ancient Ones to roughly 4–9 blocks tall, so teleports
clear the boss's full height (`BossAbilities.bossHeight`) and adjacent cones start at 10–12 blocks
rather than hugging an invisible small hitbox. Long-range calls stay at 14–24 blocks.

## Dread presence

A separate, non-damaging layer in the same controller makes proximity itself feel wrong. It runs in
`tickMaintenance`, never while the boss is solved, never on creative/spectator players, and never
stacks permanently: every effect is a short refresh with a floor on how often it can fire.

| Layer | Radius | Cadence | DORMANT | STIRRING | ENRAGED |
|---|---|---|---|---|---|
| **First sighting** | 40 | on entering range; at most once per player per boss per **5 minutes** | deep sting (boss ambient/hurt, pitch 0.5), Darkness 3s, whispered action bar with the name | Darkness 4s, same sting/whisper | Darkness 5s, same sting/whisper |
| **Proximity aura** | 24 | every 5s | 1 faint smoke particle at the player's eye line, just ahead of their gaze | 2 soul particles | 3 sculk-soul particles |

**Major presences** (Cthulhu, Azathoth, Yog-Sothoth, Hastur, Nyarlathotep, Shub-Niggurath, The
Horror) add brief **Nausea** to the first sighting (2s / 3s / 4s by phase) and, while **ENRAGED**,
a **Slowness I (2s) + Darkness (1.5s)** pulse to the proximity aura. The roaming nine and the farm
bosses get only the whisper, the sting and the particles: no nausea, no slow, no damage.

The deep sting reuses the boss's own ambient (falling back to its hurt sound), so each presence
sounds like itself rather than sharing a generic scare cue.

## Per-entity table

`T` = telegraph ticks, `CD` = cooldown ticks, `R` = range in blocks, `r` = effect radius.
All three phases are cumulative; ENRAGED can still use the earlier abilities.
† = farm boss: short telegraphs, moderate damage, no heal, no invulnerability, no fight reset.

| Boss (`/eh boss <id>`) | DORMANT | STIRRING | ENRAGED |
|---|---|---|---|
| **Glaaki** `glaaki` | **Drowned Grip** T28 CD160 R16: pull 3 blocks, Slowness II + Nausea, 6 dmg | **Servitors' Call** T30 CD220 R24: 2 Drowned Thralls | **Lake Dream** T36 CD240 R20: Blindness + Darkness + Nausea, 5 dmg r4 |
| **Ithaqua** `ithaqua` | **Walking Wind** T26 CD140 R14: cone knockback + Levitation/Slow Falling, 6 dmg | **Bitter Cold** T28 CD170 R12: freeze + Weakness + 5 dmg r5 | — |
| **Cthugha** † `cthugha` | **Flame Burst** T20 CD140 R12: ignite 4s + 6 dmg r5 | **Living Flame** T22 CD180 R20: 2 Ashen Wisps + ignite target, 4 dmg | — |
| **Yig** `yig` | **Serpent's Curse** T22 CD160 R14: Poison II + 4 dmg + 1 Risen Husk | **Ash Cloud** T24 CD180 R12: Blindness + Wither + 4 dmg r5 | — |
| **Dunwich Horror** `dunwich_horror` | **Unnatural Speed** T24 CD150 R18: teleport behind, 9 dmg + Nausea | **Wind of the Whateley** T28 CD180 R12: cone knockback + Nausea + 5 dmg | — |
| **Hydra** † `hydra` | **Acid Spit** T20 CD120 R24 (line of sight): 7 dmg + Poison | **Many Heads** T22 CD160 R10: cone 8 dmg + Weakness | — |
| **Shub-Niggurath** `shub_niggurath` | **Thousand Young** T30 CD200 R24: 3 Lesser Swarms | **Entangling Roots** T32 CD190 R16: Slowness IV + 6 dmg r5 | **The Woods Breathe** T38 CD240 R18: 8 dmg + Poison + Nausea r7 |
| **Nyogtha** `nyogtha` | **Tendrils from Below** T28 CD160 R14: pull down, Darkness + Slowness, 5 dmg | **Black Ichor** T30 CD180 R12: cone Poison + Wither + 5 dmg | — |
| **Rhan-Tegoth** `rhan_tegoth` | **Ice Shell** T24 CD200 R12: Resistance + frost nova 6 dmg r6 + Slowness | **Leap** T26 CD170 R20: teleport beside target, 9 dmg + Slowness | — |
| **Abhoth** † `abhoth` | **Birthing Filth** T20 CD160 R20: 2 Shambler Oozes | **Pool Surge** T22 CD150 R12: pull + Poison + Nausea + 5 dmg | — |
| **Atlach-Nacha** `atlach_nacha` | **Web Snare** T24 CD150 R14: Slowness IV + 5 dmg | **Brood Call** T26 CD190 R20: 2 Weaver Spawns | — |
| **Azathoth** `azathoth` | **Blind Flare** T32 CD200 R20: 6 dmg + Blindness + Nausea r6 | **Unmade Will** T36 CD210 R24: 5 dmg + Weakness + Nausea r8 | **Idiot God's Wail** T44 CD260 R16: 9 dmg + Darkness + knockback r7 |
| **Chaugnar Faugn** † `chaugnar_faugn` | **Feeding Trunk** T20 CD140 R14: pull + Hunger + 5 dmg | **Gorging Slam** T22 CD160 R12: cone 7 dmg + Nausea | — |
| **Choir Leviathan** `choir_leviathan` | **Tide Sweep** T26 CD160 R12: cone knockback + Slowness + 6 dmg | **Choir's Answer** T30 CD200 R24: 1 Wail Husk | — |
| **Cthulhu** `cthulhu` | **Brine Surge** T30 CD180 R14: cone knockback + Slowness + 6 dmg | **Dream Grip** T32 CD190 R16: pull + Nausea + Blindness + 5 dmg | **The Dreamer Wakes** T40 CD240 R18: 9 dmg + Darkness + Nausea r7 |
| **Hastur** `hastur` | **The Name** T32 CD190 R18: 4 dmg + Blindness + Nausea r5 | **Yellow Sign** T34 CD200 R16: Glowing + Weakness + 6 dmg | **King in Yellow** T42 CD250 R14: cone 9 dmg + Wither + Blindness |
| **Ithaqual Shade** † `ithaqual_shade` | **Killing Frost** T20 CD130 R12: freeze + Weakness + 4 dmg | **Whiteout** T22 CD150 R12: cone knockback + Blindness + 4 dmg | — |
| **Nephren-Ka** `nephren_ka` | **Pharaoh's Curse** T26 CD160 R14: Wither + Weakness + 5 dmg | **Sandstorm** T28 CD170 R12: cone knockback + Blindness + 4 dmg | — |
| **Nyarlathotep** `nyarlathotep` | **False Face** T26 CD170 R18: teleport behind, 8 dmg + Nausea | **Thousand Masks** T30 CD200 R24: 2 Night Hags + Blindness | **Betrayal** T38 CD240 R14: cone 9 dmg + Glowing + Weakness |
| **Tulzscha** † `tulzscha` | **Green Flare** T20 CD130 R14: ignite 4s + 6 dmg r5 | **Memory Burn** T22 CD150 R12: Blindness + Nausea + 4 dmg r5 | — |
| **Yog-Sothoth** `yog_sothoth` | **Gate Step** T30 CD190 R20: teleport beside target, 7 dmg + Slowness | **Key and Gate** T34 CD200 R18: pull + Levitation + 6 dmg | **The Gate Opens** T44 CD260 R16: 10 dmg + Wither + Blindness r7 |
| **Zstylzhemghi** † `zstylzhemghi` | **Erosion** T20 CD140 R12: cone Wither + Weakness + 4 dmg | **Vermin Call** T22 CD160 R20: 2 Lesser Swarms | — |
| **The Horror** `the_horror` | *(ATTENTION: NoAI, no abilities)* | **Reality Tear** T32 CD200 R20: 7 dmg + Darkness + Nausea r6; **Answering Swarm** T34 CD220 R24: 2 Lesser Swarms | **Grasp** T40 CD240 R16: pull + 10 dmg + Slowness IV (GRASP) |

## Testing

Requires gamemaster permissions (the command is gated the same way as the other state-mutating
`/eh` commands).

```
/eh boss list                 # all 23 ids, in table order
/eh boss glaaki               # summon at the caller, print its ability ids
/eh boss the_horror           # major presence, three abilities
```

Then, in a test world:

1. Stand 10–15 blocks away and let the boss acquire you. Watch for the particle wind-up, the
   warning sound, and the action-bar line. Step out of the ring/cone and confirm nothing lands.
2. Stand in it and confirm the hit: roughly 2–5 hearts through an iron kit.
3. Attack during the wind-up with a strong weapon. At 6% of max health the attack cancels, the
   boss freezes for 40 ticks and takes +25% damage for 3 seconds. Hit it then.
4. Break line of sight or run past `range ×1.5` during a wind-up to confirm it cancels cleanly.
5. For a farm boss (†), confirm the fight still ends in a normal kill: no heal, no invulnerability,
   no reset, and the summons expire after 60s if ignored.

`/eh boss` does not start a server by itself; it is a command for an already-running dev server.
