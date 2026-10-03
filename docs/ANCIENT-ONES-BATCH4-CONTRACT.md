# Ancient Ones batch 4 — implementation contract

Adds the last **6 Ancient Ones** (design/28): Hastur, Nephren-Ka, Abhoth, Chaugnar Faugn,
Tulzscha, Zstylzhemghi. Same framework as the existing 14 — **no new engine**. Two writers work
in parallel against these exact names; the main agent wires config/lang/loot/sounds.json, then
builds once.

**Do not run Gradle. Do not edit files outside your ownership.** No per-boss Java class: each
Ancient One is a `ModEntities` registration + `createAttributes()` supplier (one small class is
fine); behaviour composes from the shared `AncientOne`/`DreadAura` base, exactly like the existing
14.

## Ownership

- **Writer A (battlefield: sites + placement):**
  - 4 new site classes in `world/loc/site/`: `YellowCourt.java`, `BlackPyramid.java`,
    `SpawningPool.java`, `TempleOfTheFeaster.java` (model them on `RitualAltarSite`/`RiftScar`
    — implement `Location`, `id()`, `tier()`, `radius()`, `renderRadius()`, `build()`).
  - Edit `world/loc/Locations.java`: register the 4 new sites with fixed coords (pick 4 flat,
    distinct, far-apart land coordinates; reuse the same style as the existing `register(...)`
    lines). Do NOT move the existing 5 sites.
  - Edit `entity/SitePopulationSpawner.java`: add a `case` per new site id that keeps the boss
    present (see "Site-bound spawn" below).
- **Writer B (the 6 bosses):**
  - 6 entity classes in `entity/`: `Hastur.java`, `NephrenKa.java`, `Abhoth.java`,
    `ChaugnarFaugn.java`, `Tulzscha.java`, `Zstylzhemghi.java`.
  - Edit `registry/ModEntities.java`: register the 6 entity types + spawn eggs, add them to
    `ancientOnes()`, and add them to `onAttributeCreation`.
  - Edit `entity/AncientOneSpawner.java` OR nothing (see spawn choice below).
  - Edit `rite/Rites.java`: add 6 solve rites.
  - Edit `rite/RiteEngine.java`: add cases to `solveEnabled`, `solveFor`, `solveTicks` (only for
    STILLED), `giveSolveReward`, `solveMessage`.
- **Main agent:** `core/ModConfig.java` (all new keys), `client/ClientEntityRenderers.java`,
  `registry/ModSounds.java`, `assets/.../sounds.json`, `lang/en_us.json`, loot tables,
  `docs/STATUS.md`.

## The 6 bosses — frozen spec

| Entity id | Class | Site / home | Axis | Solve state | Size (w×h) | Colour | Model reuse | HP / atk / speed |
|---|---|---|---|---|---|---|---|---|
| `hastur` | `Hastur` | `yellow_court` | SANITY | STILLED | 1.6×2.6 | YELLOW | `ModelLayers.WITHER` (placeholder) | 140 / 7 / 0.22 |
| `nephren_ka` | `NephrenKa` | `black_pyramid` | CORRUPTION | SOOTHED | 0.9×2.4 | RED | `ModelLayers.HUSK` (zombie) | 170 / 8 / 0.25 |
| `abhoth` | `Abhoth` | `spawning_pool` | CORRUPTION | SOOTHED | 3.0×2.2 | GREEN | `ModelLayers.SLIME_OUTER` (slime) | 200 / 5 / 0.18 |
| `chaugnar_faugn` | `ChaugnarFaugn` | `temple_of_the_feaster` | SANITY | SOOTHED | 2.2×2.8 | DARK_RED | `ModelLayers.RAVAGER` | 180 / 9 / 0.20 |
| `tulzscha` | `Tulzscha` | `rift_scar` (Veil-centre proxy) | SANITY | SOOTHED | 1.4×2.4 | GREEN | `ModelLayers.BLAZE` | 130 / 7 / 0.30 |
| `zstylzhemghi` | `Zstylzhemghi` | `rift_scar` (eroding court proxy) | CORRUPTION | STILLED | 2.4×3.0 | PURPLE | `ModelLayers.ENDERMAN` | 150 / 6 / 0.24 |

Notes:
- `hastur`: aura is stronger for a player who has the `yellow_sign`-style item? No — keep it
  simple: SANITY drain, and the solve STILLED. If a `ModelLayers.WITHER` is awkward, reuse
  `ModelLayers.RAVAGER`; pick one and be consistent between Java and its renderer.
- `tulzscha` GREEN and `cthugha` is also flame — differentiate by colour (GREEN vs ORANGE) and
  by axis (SANITY vs CORRUPTION).
- All six use `BossEvent.BossBarColor` values that exist in 26.3 (no BLACK): use
  `YELLOW`, `RED`, `GREEN`, `DARK_RED`, `GREEN`, `PURPLE` respectively.
- Killable config: `HASTUR_KILLABLE`, `NEPHREN_KA_KILLABLE`, `ABHOTH_KILLABLE`,
  `CHAUGNAR_FAUGN_KILLABLE`, `TULZSCHA_KILLABLE`, `ZSTYLZHEMGHI_KILLABLE` (all default true).
- Aura config: `ENABLE_SANITY_HASTUR` / `SANITY_HASTUR_RATE` / `SANITY_HASTUR_RADIUS`, and the
  corruption analogues for Nephren-Ka/Abhoth/Zstylzhemghi; `ENABLE_SANITY_TULZSCHA` /
  `SANITY_TULZSCHA_RATE` / `SANITY_TULZSCHA_RADIUS`. Writer B reads these via `ModConfig.<KEY>.get()`.
- Site-bound spawn config (mirror `SITE_NYARLATHOTEP_*`): `ENABLE_SITE_HASTUR`,
  `SITE_HASTUR_COUNT`, `SITE_HASTUR_CAP` (and likewise for the other three site bosses).
  Tulzscha/Zstylzhemghi reuse the existing `rift_scar` case with `ENABLE_SITE_TULZSCHA` /
  `SITE_TULZSCHA_COUNT` / `SITE_TULZSCHA_CAP` and the Zstylzhemghi analogues.

## Site ids (frozen — Writer A must use exactly these)

- `eldritch_horror:site/yellow_court` → Hastur — **(28000, -20000)**
- `eldritch_horror:site/black_pyramid` → Nephren-Ka — **(-2000, -10000)**
- `eldritch_horror:site/spawning_pool` → Abhoth — **(-36000, -24000)**
- `eldritch_horror:site/temple_of_the_feaster` → Chaugnar Faugn — **(46000, 8000)**

These coordinates were chosen from the baked map: each is flat land (elevation std < 1 over a
400-block window), at least 8000 blocks from every existing site/city and ≥20000 from each other,
inside the world border. Register them in `Locations.init` with the same `register(new X(x,z), x, z)`
shape; do NOT move the existing five.

Tulzscha and Zstylzhemghi attach to the existing `eldritch_horror:site/rift_scar`.

## Site-bound spawn (Writer A)

In `SitePopulationSpawner.populate`, add cases exactly like the Nyarlathotep one:

```java
case "eldritch_horror:site/yellow_court" -> BestiarySupport.topUp(level, cache, player,
        ModEntities.HASTUR.get(), Hastur.class, radius,
        ModConfig.SITE_HASTUR_CAP.get(),
        ModConfig.ENABLE_SITE_HASTUR.get() ? ModConfig.SITE_HASTUR_COUNT.get() : 0,
        tick, siteGate(cx, cz, reach, SitePopulationSpawner::anywhere));
```

and add Tulzscha + Zstylzhemghi inside the existing `"eldritch_horror:site/rift_scar"` case,
next to Azathoth/Atlach-Nacha, with the same shape. Use `anywhere` (or `darkOrTainted` if it
reads better for that site — be consistent with the site's fiction).

## Solve rites (Writer B)

Add to `Rites` (mirror the existing solve entries), entity id in `grant`:

| rite id | tier | sanity | corruption | state | message intent |
|---|---|---|---|---|---|
| `silence_hastur` | 3 | -15 | 0 | STILLED | the name is unspoken; the court stills for a time |
| `unmake_nephren_ka` | 2 | -12 | -8 | SOOTHED | the king forgets you; your cult no longer doubts |
| `seal_abhoth` | 3 | -15 | -10 | SOOTHED | the pool is sealed; killing was the trap, sealing is not |
| `starve_chaugnar_faugn` | 2 | -12 | 0 | SOOTHED | the hunger is fed nothing; it sleeps |
| `quench_tulzscha` | 3 | -18 | 0 | SOOTHED | the green flame closes its eye; memory returns |
| `erase_zstylzhemghi` | 4 | -20 | -12 | STILLED | the forgotten name is forgotten again; erosion pauses |

`RiteEngine` switch additions (match by the grant string = entity registry path):
- `solveEnabled`: `case "hastur" -> ModConfig.ENABLE_RITE_SILENCE_HASTUR.get();` etc.
- `solveFor`: `hastur` → STILLED, `nephren_ka` → SOOTHED, `abhoth` → SOOTHED,
  `chaugnar_faugn` → SOOTHED, `tulzscha` → SOOTHED, `zstylzhemghi` → STILLED.
- `solveTicks` (STILLED only): `hastur` → `ModConfig.RITE_SILENCE_HASTUR_TICKS.get()`,
  `zstylzhemghi` → `ModConfig.RITE_ERASE_ZSTYLZHEMGHI_TICKS.get()`.
- `giveSolveReward`: pick from existing currency items (see `Currency.java`): `hastur` →
  `cult_token`, `nephren_ka` → `order_scrip`, `abhoth` → `black_obol`, `chaugnar_faugn` →
  `barter_seal`, `tulzscha` → `void_reagent`, `zstylzhemghi` → `relic_coin` (verify these ids
  exist; if one does not, use `mark_of_favour`).
- `solveMessage`: one sentence each, in the design's voice (a presence, not a kill).

## Sounds (Writer B references; main wires)

Each boss needs `ModSounds.HASTUR_AMBIENT/HURT/DEATH` (`register("entity.hastur.ambient")` etc.)
and its entity returns them from `getAmbientSound/getHurtSound/getDeathSound`. Reuse a vanilla
`"type": "event"` sound in `sounds.json` (no `.ogg` files exist in this repo — follow the
existing pattern). Main agent edits both files.

## Renderers (main agent)

One `client/<Name>Renderer.java` per boss, mirroring `CthulhuRenderer`, reusing the model layer
from the table. Main wires all six in `ClientEntityRenderers`.

## Lang (main agent)

`entity.eldritch_horror.<id>` + `item.eldritch_horror.<id>_spawn_egg` +
`subtitles.entity.eldritch_horror.<id>.ambient|hurt|death` + `rite.eldritch_horror.<rite_id>`.

## Loot (main agent)

`data/eldritch_horror/loot_table/entities/<id>.json`, modelled on `rhan_tegoth.json`.

## Rules

- `@NullMarked` is package-level; annotate `@Nullable` only where something can be null.
- No deprecated APIs; JDK `Math.clamp`.
- Deterministic, server-authoritative, loaded-chunks-only (use the shared `BestiarySupport.topUp`
  and `siteGate`; never `Math.random`).
- Every new site must be registered in `Locations.init` or the codex/spawner will not see it
  (the codex auto-lists all `Locations`, so a registered site gets a SITE entry for free).
- Keep the boss aura in `AncientOne`/`DreadAura` shape: `isAuraActive()` combines `!isSolved() &&
  masterAuraEnabled() && ModConfig.ENABLE_<AXIS>_<BOSS>.get()`.

## Deferred (do NOT do now)

- A real `the_veil` dimension for Tulzscha/Zstylzhemghi; they use the `rift_scar` as a proxy.
- New sound `.ogg` assets; new art/models (the user's job).
