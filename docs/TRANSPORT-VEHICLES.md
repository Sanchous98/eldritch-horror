# Transport vehicles

Art and passenger layout for the scheduled public-transport vehicles. Schedules, speeds,
lifecycle and fares stay in `docs/TRANSPORT.md` and `design/30-transport.md`; this file records
the physical vehicle.

## Ocean liner (ship)

A real 91-block vessel — a location that moves — not a boat and not a scaled dinghy. Client model:
`client/model/CoastalPacketModel` (one renderer: `client/CoastalPacketRenderer`, one 2048x2048
texture: `textures/entity/coastal_packet.png`, generated deterministically by
`tools/generate_ship_texture.py`). The entity id stays `coastal_packet`; the physical layout lives
in `entity/ShipGeometry` and `entity/ShipSeats`, which the entity, the collision parts, the model
and the tests all read.

### Dimensions (blocks)

| Part | Size |
|---|---|
| Deck length (stem to stern counter) | 91.0 |
| Beam | 18.0 |
| Hull depth (keel to deck) | 9.15 (deck 7.25 above the sea) |
| Walkable main deck | open foredeck and aft deck, 18 wide, ~22 long each |
| Bulwark + cap rail | 1.15 above the deck |
| Cabin block (deckhouse) | 44 long, 16 wide, 7.2 tall |
| Promenade / boat deck | the cabin roof, 13.6 above the origin |
| Navigation bridge | 10 x 14, roof at 16.6 above the origin |
| Funnels | three, 4 x 4, tops 20.6 above the origin |
| Masts | fore and aft, 18 blocks above the deck |
| Lifeboats | eight, hanging at the promenade deck edge |
| Gangway | 7 steps + landing on each beam amidships, 7 blocks out from the deck edge |
| Entity hitbox | the 18 x 1 deck-planking slab (`ModEntities.COASTAL_PACKET`), expanded per heading |

### One AABB, and why it is the deck

An entity can only own one axis-aligned box. The hull's box is built by
`CoastalPacket.makeBoundingBox` from `ShipGeometry.hullBox(...)`:

* its top is exactly `DECK_Y = 6.4` above the origin (7.25 above the sea) — **the deck the player
  stands on is the top face of the hull box**, never a 10-block-tall box whose roof is mislabelled
  a deck;
* it is one block thick, the planking itself. A tall solid box would be a 77 x 77 wall at a
  diagonal heading and would trap players standing on a quay inside it; the floor slab keeps the
  ship's collision where the deck actually is;
* the 91 x 18 rectangle is enclosed at the *current heading*, so the box always contains the whole
  deck as the ship turns (the box is the smallest axis-aligned enclosure of the rotated deck).

The solid cabin block and bridge are carried by nine invisible `ShipCollisionPart` entities (a
2 x 4 tiling of the 16 x 44 deckhouse plus one bridge). Each part is the axis-aligned enclosure of
its rotated local box; the chunks overlap so the cabin wall has no seam at a diagonal heading, and
the narrow beam strips keep the invisible enclosure from swallowing the open decks. Parts
never save, never take damage and are never pushable. They are pickable only so a right-click on
the white cabin forwards to the hull's boarding interaction; a `NoopRenderer` keeps them invisible.
The hull moves them after it moves itself, so they are never a tick behind. There is exactly one
vessel entity; the parts exist for collision and boarding only.

### Walkable deck while moving

The hull and every part return `canBeCollidedWith = true`, so players collide with them and stand
on the deck/roofs. Every server tick `CoastalPacket` captures each non-riding player standing on a
walkable surface (feet within 0.2 of the surface, inside the hull-local footprint) together with
their hull-local position, moves the vessel, then re-seats those players at `hull position +
rotate(local)`. Keeping the *local* coordinate (rather than a world delta) makes the carry correct
through turns and through the longitude seam. Riders are true passengers: their attachment points
come from `ShipSeats`, so they cannot fall through the deck while it moves.

Surface heights (`ShipGeometry.surfaceY`): main deck 6.4, promenade 13.6, bridge roof 16.6.

### Boarding from the pier

The gangways are modelled as seven-step approaches with landings on both beams amidships,
from the deck edge down to about one block above the waterline, so the vessel reads as reachable
from a quay instead of something to jump into. While boarding, the vessel lies broadside to the quay (`ShipGeometry.dockedYaw`): her length runs
along the shore and the starboard gangway faces the land, so a 91-block ship does not bury her
stern in the beach. On departure she swings her head toward the course at four degrees per tick
instead of snapping. Boarding itself is unchanged: the fare interaction (`CoastalPacket.tryBoard`,
shared with `/eh ship board`) checks the window, a free seat and the fare, then seats the player;
no parkour is involved. The white cabin parts are pickable and forward their interaction to the
hull, so the ship is a large click target from the pier. A rider dismounts beside their own seat
on the deck.

### Passenger layout

`CoastalPacket.SEATS = 32`, sixteen on the foredeck and sixteen on the aft deck, four across on
each of four rows per side. Seat offsets are entity-local `{x, attachmentY, z}` with +Z toward the
bow; the attachment is `deck - 0.3 + 0.6` so the rider's feet sit 0.3 blocks into the planking
and the eye just clears the bulwark.

| Seats | Where | Local `{x, z}` |
|---|---|---|
| 1-16 | Foredeck, four rows | `{6.2, 2.2, -2.2, -6.2}` at `z = 25, 30, 35, 40` |
| 17-32 | Aft deck, four rows | the same mirrored at `z = -25, -30, -35, -40` |

Config: `shipSeats` defaults to 32 (range 1-32). The fare, boarding window, timetable, ports,
lifecycle, immunity, never-saved and never-pushable rules and the chunk-ticket machinery are
unchanged. The ticket radius grew to 3 chunks (7 x 7 = 112 blocks) because the vessel's half
diagonal is 46.4 blocks; a radius-2 ticket would leave the bow or stern in an unloaded chunk.

### Model contents

Stepped dark hull with a red boot topping, a planked main deck and promenade, a solid bulwark with
stanchions and a cap rail, a 44 x 16 white deckhouse with two window bands and forward/aft window
bays, a promenade deck with rail, a bridge with wrap-around windows and wings, three funnels with
red bands and steam pipes, two masts with yards and stays, eight lifeboats on davits, 48 portholes,
32 deck chairs, a striped canvas awning over the aft deck, cargo cranes, ventilators, a binnacle,
hawse pipes and the gangways. Eight lanterns are a separate baked layer drawn with the eyes render
type, so they stay lit at night without making the whole ship full-bright.

## Train

The scheduled train is a real, walk-in vehicle, not a scaled minecart. This page covers its
dimensions, the physical seat layout, the client models and how boarding places riders. The
schedules, speeds, fares and lifecycle live in [`TRANSPORT.md`](TRANSPORT.md) and
`world/rail/TrainService.java`; the carriage work changes none of them.

### Sizes and consist

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

### Seats

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

### Client models

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

### Textures

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
