# Boss models

Every Ancient One now has a bespoke client model and a procedural texture. The vanilla placeholders
(Ravager, Slime, Wither, Blaze, Spider, Creeper, Warden, Ghast, Iron Golem, Phantom, Villager, Cow,
Hoglin, Silverfish, Endermite, Vex, Witch) are gone from the boss renderers.

This document covers the model kit, the roster and its palettes, how sizes/hitboxes are matched, how
to regenerate textures, and how to add the next boss.

## Layout

| Path | Purpose |
| --- | --- |
| `client/model/BossModelBase.java` | Shared animation kit: idle breathing, sway, walk cycle, wing flap, scale pulse and the attack lunge |
| `client/model/BossRenderState.java` | One render state for the roster; carries `attackProgress`, `attacking`, `lunge` |
| `client/model/BossRenderer.java` | `MobRenderer` base with one texture + one world scale per boss, plus ground compensation |
| `client/model/kit/BossParts.java` | Box/blob/limb/spike/eye/bar builders and the reserved eye UV constants |
| `client/model/kit/Tentacles.java` | Radial crowns, beards and loose tentacle clusters |
| `client/model/kit/SpinedShell.java` | Glaaki's dome of spines and eyes |
| `client/model/kit/TrunkedIdol.java` | Chaugnar Faugn's elephantine head, trunk and tusks |
| `client/model/kit/CrownOfEyes.java` | Mismatched eye scatter |
| `client/model/kit/Wings.java` | Leathery wings for Cthulhu |
| `client/model/kit/SerpentCoils.java` | Coil base + S-neck for Yig |
| `client/model/kit/FlameBody.java` | Fire column/orb for Cthugha and Tulzscha |
| `client/model/kit/WebLimbs.java` | Eight jointed spider/crab legs |
| `client/model/kit/HumanoidRig.java` | Configurable upright rig for the human-shaped presences |
| `client/model/boss/*Model.java` | One model per Ancient One (23) |
| `client/ClientLayerDefinitions.java` | `RegisterLayerDefinitions` listener that bakes every custom layer |
| `client/*Renderer.java` | One thin renderer per Ancient One |
| `tools/generate_boss_textures.py` | Deterministic 128x128 texture generator |

Everything is client-only (`@EventBusSubscriber(value = Dist.CLIENT)`); no server, AI, stat or combat
code depends on the models.

## Scale and hitboxes

The world is 1:1 (1 block = 1 m): Paris is 11 km across, ordinary buildings are 20–30 blocks tall
and the Eiffel Tower is 330. The presences are therefore city-scale, not pet-scale. Every size lives
in **one place**, `entity/AncientOneSizes.java`, and the entity registry, the renderers, the combat
layer and the tests all read it. Nothing hardcodes a boss size.

| Tier | Presences | Height | Width |
| --- | --- | --- | --- |
| Cosmic | Azathoth, Yog-Sothoth, Shub-Niggurath, Nyarlathotep | 112–128 | 40–56 |
| Major | Cthulhu, Hastur, The Horror, Ithaqua, Yig, Rhan-Tegoth, Choir Leviathan, Glaaki, Nyogtha, Atlach-Nacha, Dunwich Horror, Nephren-Ka | 64–96 | 24–40 |
| Lesser / farmable | Chaugnar Faugn, Cthugha, Tulzscha, Hydra, Ithaqual Shade, Abhoth, Zstylzhemghi | 24–32 | 10–16 |

Eye height is pinned to `0.85 × height` in the registry (vanilla's default, kept explicit so the
test can hold it). The hitbox starts at the feet, so a player at ground level can hit the body; the
eye height is where the aim ray lands.

| Entity | Hitbox W x H | Renderer scale | Tier | Silhouette |
| --- | --- | --- | --- | --- |
| Azathoth | 56 x 128 | 23.63 | cosmic | blind bubbling nucleus, orbiting bubbles, bone flutes |
| Shub-Niggurath | 52 x 120 | 24.00 | cosmic | tree-thick mass, three goat necks, tentacle curtain |
| Yog-Sothoth | 48 x 116 | 21.02 | cosmic | two rotating gate-rings around a golden core |
| Nyarlathotep | 40 x 112 | 46.40 | cosmic | slim masked figure, ragged cloak, second mask |
| The Horror | 40 x 96 | 16.53 | major | accumulated mass, many eyes/mouths, two tentacle crowns |
| Cthulhu | 40 x 96 | 22.20 | major | winged humanoid, tentacle beard, horns |
| Hastur | 36 x 88 | 27.66 | major | hooded yellow robe, pale mask, jagged crown |
| Ithaqua | 30 x 80 | 30.55 | major | gaunt giant, long arms, wind ring, ice claws |
| Yig | 28 x 72 | 36.80 | major | coiled serpent, upright S-neck, hooded head |
| Rhan-Tegoth | 32 x 72 | 23.04 | major | domed crab-idol, eight legs, two pincers |
| Choir Leviathan | 36 x 72 | 24.48 | major | drowned whale, head of eyes, belly mouths, flukes |
| Glaaki | 30 x 68 | 30.76 | major | spined drowned shell on a tendril skirt |
| Nyogtha | 36 x 68 | 27.20 | major | tar pool with a dozen rearing tendrils |
| Atlach-Nacha | 40 x 64 | 26.35 | major | wide spider, eight legs, pale web crown |
| Dunwich Horror | 26 x 64 | 21.33 | major | bloated body, tentacle crown, four thin legs |
| Nephren-Ka | 24 x 64 | 31.11 | major | bandaged king, double crown, staff |
| Chaugnar Faugn | 14 x 30 | 10.88 | lesser | elephantine humanoid idol, trunk, tusks, heavy arms |
| Cthugha | 12 x 28 | 10.27 | lesser | flame column, orbiting embers, floating eyes |
| Tulzscha | 12 x 28 | 9.72 | lesser | narrow green flame column inside an ember halo |
| Hydra | 16 x 26 | 8.67 | lesser | low body, six fanning necks, four stumpy legs |
| Ithaqual Shade | 10 x 26 | 8.69 | lesser | hooded ghost, tattered lower body, long arms |
| Abhoth | 14 x 24 | 12.00 | lesser | filth pool, budding spawn, thick arms, mouths |
| Zstylzhemghi | 12 x 24 | 9.16 | lesser | dark core inside an orbiting insect swarm |

Renderer scale is derived as `baseScale × height / baseHeight`, so the visible model keeps the same
top-to-hitbox relationship it had at the design size while the body becomes 12–37× larger. Model
origins sit on the ground: the design rig places feet at model y=24 (1.5 blocks), exactly the fixed
`-1.501` translation `LivingEntityRenderer` applies in model space, so scaling keeps the feet on the
floor. Shadows scale with the body, capped at 8× so they stay readable. Textures tile; no new sheets
were needed.

### Movement and collision

A 40–56 block footprint cannot use vanilla block collision — it would intersect every street and
doorway — so an Ancient One sets `noPhysics` and keeps itself on the ground in `AncientOne.travel`:
after the normal AI walk it snaps its feet to the highest floor within its step band (at most 6
blocks, at least 1). Kerbs, rails and rubble are stepped over; a drop is a fall caught on the next
tick; a body can never fall through the thin city crust (there is also a bottom-of-world clamp).
It walks straight at its target (`steerGiant`) rather than asking `GroundPathNavigation` to find a
56-block-wide path. It is not pushable, and it never shoves players, so the hitbox cannot eject
anyone out of the world; ordinary mobs are still displaced. There are no riders, and `noPhysics`
means the body cannot suffocate anything inside it.


## Animation

`BossModelBase.setupAnim` resets every part (26.3 contract), then applies:

- **idle breathing** — a small scale pulse on the body part registered with `breatheWith`;
- **idle sway** — additive sine on tentacles, robes, necks and wings registered with `sway`;
- **walk cycle** — `leg`/`arm` driven by vanilla `walkAnimationPos`/`walkAnimationSpeed`;
- **wing flap** — `flap`, faster while moving;
- **pulse** — `pulse`, for blobs, flames and swarms;
- **attack lunge** — `applyLunge`, driven by `BossRenderState.lunge`, a `sin(pi * progress)` curve
  computed in `BossRenderer.extractRenderState` from `LivingEntity#getSwingAnimation`.

`applyIdle` is the per-boss hook for head tracking, trunk sway, neck curvature, flame flicker, orbit
rotation, etc.

## Textures

All textures are 128x128 RGB PNGs under
`src/main/resources/assets/eldritch_horror/textures/entity/` and are generated by
`tools/generate_boss_textures.py`. The script is deterministic (seed = `zlib.crc32(name)`), so
re-running it reproduces identical pixels.

The top-left corner of every sheet is reserved:

- `(0,0)-(16,16)` — pale eye region (`BossParts.EYE_U/EYE_V`);
- `(16,0)-(28,12)` — near-black pupil/maw region (`BossParts.PUPIL_U/PUPIL_V`).

Each texture is built from a base colour, a lighter shade, a darker shade, an accent, an eye colour
and a pupil colour, then layered with a vertical gradient, ~120 mottling blobs, veins/cracks,
pustules, fine grain and speckles.

### Palettes

| Texture | Base | Accent | Look |
| --- | --- | --- | --- |
| dunwich_horror | 120,130,104 | 196,210,164 | sickly grey-green, half-invisible |
| glaaki | 42,62,48 | 118,152,108 | green-black drowned chitin |
| chaugnar_faugn | 116,108,92 | 188,174,134 | grey-ochre temple stone |
| cthulhu | 44,86,74 | 146,176,130 | deep sea green, pale tentacles |
| azathoth | 46,34,58 | 126,190,120 | violet void, radioactive green |
| yog_sothoth | 96,64,34 | 226,176,92 | orange-gold gates, void shadow |
| hastur | 138,118,44 | 230,212,122 | yellow robe, pale mask |
| nyarlathotep | 38,30,34 | 146,38,42 | black robe, ivory mask, red eye |
| shub_niggurath | 52,38,28 | 128,54,34 | matted black-brown goat mass |
| the_horror | 32,24,38 | 104,62,138 | void black and purple |
| atlach_nacha | 30,28,34 | 158,40,52 | black spider, crimson web |
| ithaqua | 156,178,192 | 232,244,250 | frost white-blue |
| yig | 34,76,46 | 172,196,92 | emerald serpent scales |
| cthugha | 152,46,22 | 250,176,58 | orange-red fire |
| hydra | 54,82,54 | 140,164,84 | swamp green scales |
| nyogtha | 24,20,26 | 86,60,102 | tar black with violet sheen |
| rhan_tegoth | 92,82,62 | 176,148,82 | crab shell and gold |
| tulzscha | 36,96,42 | 156,236,110 | green flame |
| zstylzhemghi | 52,44,30 | 164,120,44 | brown-black chitin swarm |
| abhoth | 112,100,92 | 178,138,128 | grey-pink filth |
| nephren_ka | 162,148,112 | 216,186,84 | bandage ivory and gold |
| choir_leviathan | 42,58,68 | 108,140,138 | drowned blue-grey whale |
| ithaqual_shade | 146,162,178 | 216,234,242 | pale cold ghost |

### Regenerate

```bash
python3 tools/generate_boss_textures.py                 # all 23
python3 tools/generate_boss_textures.py glaaki cthulhu   # selected
python3 tools/generate_boss_textures.py --list           # names
```

Pillow is used when installed. If it is missing the script falls back to a small built-in PNG
encoder; the pixels are the same. The script prints each written file, size and encoder.

## Adding the next boss

1. Add a palette to `PALETTES` in `tools/generate_boss_textures.py` and run the script.
2. Create `client/model/boss/YourBossModel.java`:
   - `public static final ModelLayerLocation LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "your_boss"), "main");`
   - `createBodyLayer()` builds the rig with `BossParts` / kit builders at the design size;
   - the constructor fetches parts with `root.getChild(...)` and registers
     `breatheWith`, `sway`, `leg`/`arm`, `flap`, `pulse`;
   - override `applyIdle` and `applyLunge` for the boss's signature motion.
3. Register the layer in `ClientLayerDefinitions`.
4. Replace the renderer with a `BossRenderer<YourBoss, YourBossModel>` subclass passing texture,
   shadow and the renderer scale.
5. Keep the renderer registration in `ClientEntityRenderers` (the class name is unchanged).
6. Add one row to `entity/AncientOneSizes.java` with the tier and hitbox; `ModEntities` builds
   `.sized(...)` and the eye height from it, and the test fails until the row exists. `BossRenderer`
   takes `SIZE.renderScale()`/`SIZE.shadow(base)` from the same row. Add the row to the table above.

Names are checked at construction time (`ModelPart#getChild` throws), so the build gate is the
compile plus a quick client spawn of the boss.
