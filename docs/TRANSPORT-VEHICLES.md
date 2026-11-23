# Transport vehicles

Art and passenger layout for the scheduled public-transport vehicles. Schedules, speeds,
lifecycle and fares stay in `docs/TRANSPORT.md` and `design/30-transport.md`; this file records
the physical vehicle.

## Coastal packet (ship)

A real deck-and-cabin hull, not a scaled vanilla boat. Client model:
`client/model/CoastalPacketModel` (Java `ModelPart` cubes, one renderer:
`client/CoastalPacketRenderer`). Texture:
`textures/entity/coastal_packet.png`, generated deterministically by
`tools/generate_ship_texture.py` (1024x1024, one painted rectangle per material).

### Dimensions (blocks)

| Part | Size |
|---|---|
| Overall model length | 19.5 including the bowsprit (17.75 hull) |
| Beam | 5.5 |
| Walkable deck | 5.0 wide; 0.75 above the entity origin, rendered about 1.3 above the sea |
| Bulwark + cap rail | 0.875 above the deck |
| Cabin | 4.25 long, 4.5 wide, roof/poop deck 2.75 above the origin |
| Mast | 7.75 above the origin (6.25 above the deck), two yards and two square sails |
| Funnel | 4.5 above the origin, on the cabin roof |
| Entity hitbox | 5.5 x 3.6 (`ModEntities.COASTAL_PACKET`) |

The hull is built from fourteen 16-unit strake segments, a stepped bow and stern counter, a
keel, rudder, sheer strake, six portholes, a raised forecastle with its own rail, deck cargo,
an anchor and a stepping bowsprit. The cabin has four windows per side (plus front/rear
windows and a door), a railed poop deck on the roof, and a ship's wheel on the front bulkhead.
Six lanterns sit on a second baked layer and are drawn with the eyes render type, so they stay
lit at night without making the whole ship full-bright.

### Passenger layout

`CoastalPacket.SEATS = 14`. Seat offsets are entity-local `{x, z}`, with +Z toward the bow
(the model's +X). Two benches run along the bulwarks; two more are inside the cabin.

| Seats | Where | Offset `{x, z}` |
|---|---|---|
| 1-10 | Port/starboard railing benches | `{+/-2.06, 3.25 .. -3.25}`, five per side |
| 11-14 | Cabin side benches, under the roof | `{+/-1.55, -4.4}` and `{+/-1.55, -6.0}` |

Rider feet sit at `RIDE_HEIGHT = 3.23` above the entity origin: that is the renderer's 2.9-block
buoyancy lift plus 0.33, so the deck is at 3.65 and a rider sits 0.42 into it (legs hidden inside
the hull, the vanilla-boat illusion). The eye is about 1.2 above the planking: 0.33 above the cap
rail, so a seated passenger sees the sea over the rail. The cabin roof underside is 0.55 above
the eyeline, so no head or camera is inside the roof.

The lift exists because an `AbstractBoat` floats with 65% of its hitbox height below the surface;
with a 3.6-block hitbox that is 2.34 blocks, so `CoastalPacketRenderer` translates the model by
2.9 blocks to give the hull visible freeboard and the seat heights include the same offset.

Config: `shipSeats` defaults to 14 (range 1-14). Boarding, dismount, empty-hull despawn and
chunk tickets are unchanged from the original packet.
