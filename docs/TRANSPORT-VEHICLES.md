# Transport vehicles — the train

The scheduled train is a real, walk-in vehicle, not a scaled minecart. This page covers its
dimensions, the physical seat layout, the client models and how boarding places riders. The
schedules, speeds, fares and lifecycle live in [`TRANSPORT.md`](TRANSPORT.md) and
`world/rail/TrainService.java`; the carriage work changes none of them.

## Sizes and consist

| Part | Length | Width | Height (rail to roof) |
|---|---|---|---|
| Locomotive | 16 blocks (engine nose + cab) | 3.0 blocks | 3.8 blocks |
| Passenger wagon | 14 blocks | 3.0 blocks | 3.8 blocks |

The consist is still one locomotive plus `trainCars` wagons (2–4). Cars are laid out on
`TrainTimetable.SPACING = 16` blocks, so a wagon keeps a 2-block coupling gap from its neighbour
and the locomotive a 1-block gap. `TrainTimetable.consistLength` and the trail sampler therefore
stay unchanged in shape; only the spacing constant grew.

The entity hitbox is the **cross-section** (`ModEntities`: 3.0 × 4.0, eye 2.2). A rotated 14-block
body cannot be represented by `EntityDimensions`, so `TrainCar.renderBounds` inflates the
interpolated box by the car's half length and `TrainCarRenderer.getBoundingBoxForCulling` uses it;
the whole carriage is culled as one object instead of popping out at its ends.

Chunk loading follows the same length. The service's spawn ticket is a 9×9 entity-ticking square
(72 blocks each way), enough for the last wagon at 64 blocks behind the station; once the consist
is ticking, the locomotive moves a 5×5 ticket to the chunk under the middle carriage. That is one
ticket, not one per car, and it changes no schedule or speed — only the coverage needed by a
16-block locomotive plus 14-block wagons.

Interior (local frame, rail at y = 0):

| Feature | Height |
|---|---|
| underframe / wheels | −0.125 … 0.56 |
| wooden floor (top) | 0.5625 |
| window band | 1.4375 … 3.0 |
| bench top | 1.125 |
| ceiling | 3.375 |
| roof ridge (top) | 3.875 |

## Seats

Twelve chairs per car — two rows of six, each chair facing its side window (`TrainSeats`).

| Car | Row positions (z, blocks) | Spacing |
|---|---|---|
| Wagon | −5, −3, −1, 1, 3, 5 | 2.0 |
| Locomotive cab | −3.0, −1.4, 0.2, 1.8, 3.4, 5.0 | 1.6 |

The locomotive's engine nose takes the front four blocks, so its six rows sit in the cab from
z = −3 to z = +5. Every car has chairs at `x = ±1.0`.

Boarding is unchanged: `ModConfig.TRAIN_SEATS` (now 12, range 1–12) is clamped by
`TrainTimetable.MAX_SEATS`, and a passenger's index in `getPassengers()` selects
`TrainSeats.offset(index, locomotive)`. Passenger *2k* takes the left chair of row *k* and
passenger *2k+1* the right chair, so the abstract capacity can never exceed the twelve physical
chairs. If an operator lowers `trainSeats`, only the first chairs are used but every rider still
lands on a real bench.

### Rider attachment

`getPassengerAttachmentPoint` returns the **bench top**: `y = 1.125` (nine pixels above the
origin). A riding player's eye is 1.62 blocks higher, at ≈ 2.75, which falls inside the window
band (1.4375 … 3.0). The chair pad spans x = 0.625 … 1.4375 and the wall lining stops at 1.375,
while a player is only 0.6 wide: the rider's box sits inside the empty cabin, not in a wall.

There is **no collision inside the carriage**: the car entity is `noPhysics`, the model is
client-side geometry only, and the cabin is an empty shell between the floor, walls and ceiling.
A seated player looks straight through the window opening; the glass pane is a mostly transparent
cutout texture, not an opaque cube.

Doors are modelled at the car ends (the locomotive has a rear door and a windscreen bulkhead at
the front of the cab) but are decorative: boarding and dismounting remain the fare interaction and
the station teleport at arrival.

## Client models

`client/model/TrainCarModel.java` builds both variants from `ModelPart` cubes, authored at 16
units per block and **not** scaled by the renderer:

- **Wagon** — wooden floor on a riveted underframe; two bogies with eight wheels; solebars,
  buffers and couplers; seven window bays per side with a lower wall band, an upper band, corner
  posts and brass mullions; glass panes inset in the openings; overhanging roof with a ridge;
  doors at both ends; twelve chairs in two facing rows.
- **Locomotive** — the same cab shell (seven window bays, roof, rear door, chairs) plus a front
  bulkhead with windscreen, boiler, smokebox door, three brass boiler bands, funnel and cap, steam
  dome, handrails, cylinder blocks, headlamp with an emissive-looking glow face, and a sloping
  cowcatcher.

The renderer (`client/TrainCarRenderer`) submits the model with `180 − yaw` (the vanilla minecart
convention for a model whose nose points at −Z) and one texture per variant. Layer definitions
are registered in `ClientLayerDefinitions`.

## Textures

`tools/generate_train_textures.py` writes two deterministic 256×256 RGBA atlases:

```
python3 tools/generate_train_textures.py            # both
python3 tools/generate_train_textures.py wagon      # only the wagon
python3 tools/generate_train_textures.py --list
```

The script follows `tools/generate_boss_textures.py`: seeded by `zlib.crc32(name)`, Pillow when
available and a built-in RGBA PNG encoder otherwise. Output goes to
`src/main/resources/assets/eldritch_horror/textures/entity/train_wagon.png` and
`train_locomotive.png`.

Both atlases share one UV layout, mirrored by the `*_U`/`*_V` constants at the top of
`TrainCarModel`. Keep the two in sync when moving regions:

| Region | u, v | size | Used by |
|---|---|---|---|
| floor | 0, 0 | 112×35 | floor and nose deck |
| glass | 116, 0 | 56×52 | window panes, door windows, windscreen |
| frame | 0, 40 | 68×37 | solebars, seat pedestals |
| wall | 72, 56 | 66×46 | lower side wall |
| pillar | 142, 56 | 20×50 | window posts |
| door | 166, 56 | 60×50 | door leaf |
| wheel | 230, 56 | 24×24 | wheels |
| upper | 0, 82 | 66×38 | upper side wall |
| roof | 136, 110 | 110×34 | roof and ridge |
| brass | 0, 124 | 40×40 | bands, mullions, couplers, handles |
| nose | 44, 124 | 80×46 | boiler |
| lantern | 128, 148 | 30×30 | headlamp housing |
| endwall | 0, 174 | 40×50 | car ends and cab bulkhead |
| glow | 44, 174 | 20×16 | headlamp face |
| bench | 76, 180 | 60×42 | seat pads and backs |
| metal | 140, 180 | 102×46 | bogies, buffers, smokebox, funnel |

The **glass** region is transparent except for a few pale reflection streaks. The model uses
`entityCutout`, so transparent texels are discarded: windows are real openings with glazing bars.
No translucent render pass and no light block are needed; readable windows were the priority.
