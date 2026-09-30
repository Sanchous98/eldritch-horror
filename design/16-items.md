# Items

Categories: **reagents**, **artefacts**, **tomes**, **gear**, **currency**, **cursed**.
IDs are `snake_case`. Every item needs a lang entry and a tooltip.

## Reagents

Material components consumed by rites.

| id | Name | Source | Used by |
|---|---|---|---|
| `salt_reagent` | Consecrated Salt | Order vaults, warding | `ward_of_the_eye`, `close_rift` |
| `chalk_reagent` | Ritual Chalk | Crafted | `ward_of_the_eye`, `close_rift` |
| `bone_reagent` | Grave Bone | Skeletons, ossuaries | `call_the_lesser` |
| `star_reagent` | Star-Iron Fragment | Meteors, the Veil | `summon_star_spawn` |
| `void_reagent` | Void Residue | Rifts, corrupted chunks | `open_rift` |
| `silver_reagent` | Moonlit Silver | Order trade, ruined vaults | `rite_of_cleansing`, `respec` |
| `pearl_reagent` | Drowned Pearl | Coast, Choir trade | `drowned_blessing` |
| `blood_offering` | Blood Offering | Player (costs health) or mobs | summoning/open rites |

## Artefacts

Unique-effect items (sidegrades with a cost).

| id | Name | Effect | Drawback |
|---|---|---|---|
| `eye_of_the_watcher` | Eye of the Watcher | See rifts and entity effects through walls | Sanity drains while equipped |
| `drowned_lung` | Drowned Lung | Breathe underwater indefinitely | Corruption +slowly |
| `mirror_shard` | Mirror Shard | Reveals a nearby rite/secret | Occasional sanity jolt |
| `warden_sigil` | Warden's Sigil | −40% corruption spread within 16 blocks | −10% ritual power |
| `prophet_mask` | Prophet's Mask | +25% summoning power | Villagers flee; sanity floor lower |

## Tomes

Knowledge items. Reading grants a known rite or lore and applies a cost.

| id | Name | Grants | Cost |
|---|---|---|---|
| `tome_of_the_eye` | Tome of the Unblinking Eye | `ward_of_the_eye` | `sanity −10` |
| `tome_of_tides` | Tome of Tides | `drowned_blessing` | `sanity −10` |
| `hollow_text` | Hollow Text | `call_the_lesser` | `sanity −15`, `corruption +8` |
| `star_codex` | Star Codex | `summon_star_spawn` | `sanity −20`, `corruption +10` |

## Gear

| id | Name | Slot | Effect |
|---|---|---|---|
| `occult_tool` | Occultist's Implement | mainhand | +ritual speed |
| `silver_blade` | Silver Blade | mainhand | +damage vs. eldritch |
| `investigator_coat` | Investigator's Coat | chest | −sanity drain in darkness |
| `choir_robe` | Choir Robe | chest | +reputation gain, +corruption gain |

## Currency

| id | Name | Source |
|---|---|---|
| `mark_of_favour` | Mark of Favour | Cult rewards; spend with cults |
| `relic_coin` | Relic Coin | Ruins, structures |

## Cursed items

| id | Name | Boon | Curse |
|---|---|---|---|
| `hollow_pact_blade` | Hollow Pact Blade | Massive damage | Sanity drain on kill; corruption +1/kill |
| `grasping_idol` | Grasping Idol | Attracts loot | Attracts hostile eldritch entities |

## Full registry (content stubs, generated)

Every registered item id, grouped by its registry class. These are **content stubs**: they
exist and appear in the creative tab; behaviour is not wired yet (`27-systems-framework.md`).
Effects noted in code comments; tooltips are deferred.

### Reagents (19)

ritual components consumed by rites

| id | Name |
|---|---|
| `salt_reagent` | Consecrated Salt |
| `chalk_reagent` | Ritual Chalk |
| `bone_reagent` | Grave Bone |
| `star_reagent` | Star-Iron Fragment |
| `void_reagent` | Void Residue |
| `silver_reagent` | Moonlit Silver |
| `pearl_reagent` | Drowned Pearl |
| `blood_offering` | Blood Offering |
| `grave_dust` | Grave Dust |
| `black_candle` | Black Candle |
| `ritual_ash` | Ritual Ash |
| `ichor_vial` | Ichor Vial |
| `moonwater` | Moonwater |
| `mandrake_root` | Mandrake Root |
| `iron_nail` | Iron Nail |
| `consecrated_oil` | Consecrated Oil |
| `veil_dust` | Veil Dust |
| `effigy_doll` | Effigy Doll |
| `spirit_ash` | Spirit Ash |

### Consumables (15)

tonics and restoratives — NOT food (no hunger system)

| id | Name |
|---|---|
| `sanity_tincture` | Sanity Tincture |
| `soothing_tea` | Soothing Tea |
| `dream_syrup` | Dream Syrup |
| `valerian_tonic` | Valerian Tonic |
| `lucid_draught` | Lucid Draught |
| `salt_purge` | Salt Purge |
| `blessed_water` | Blessed Water |
| `moonwater_flask` | Moonwater Flask |
| `penitents_ash` | Penitent's Ash |
| `sight_salve` | Sight Salve |
| `wardsalt_bomb` | Wardsalt Bomb |
| `incense_stick` | Incense Stick |
| `herbal_bundle` | Herbal Bundle |
| `fortifying_stew` | Fortifying Stew |
| `iron_broth` | Iron Broth |

### Tomes (10)

knowledge items; reading grants a rite at a cost

| id | Name |
|---|---|
| `tome_of_the_eye` | Tome of the Unblinking Eye |
| `tome_of_tides` | Tome of Tides |
| `hollow_text` | Hollow Text |
| `star_codex` | Star Codex |
| `codex_of_wards` | Codex of Wards |
| `bone_ledger` | Bone Ledger |
| `atlas_of_the_veil` | Atlas of the Veil |
| `watchers_diary` | Watcher's Diary |
| `cult_litanies` | Cult Litanies |
| `fragment_page` | Fragment Page |

### Artifacts (12)

unique-effect items (sidegrades with a cost)

| id | Name |
|---|---|
| `eye_of_the_watcher` | Eye of the Watcher |
| `drowned_lung` | Drowned Lung |
| `mirror_shard` | Mirror Shard |
| `warden_sigil` | Warden's Sigil |
| `prophet_mask` | Prophet's Mask |
| `lantern_of_steadfast` | Lantern of the Steadfast |
| `wardstone_pendant` | Wardstone Pendant |
| `compass_of_quiet` | Compass of Quiet |
| `ring_of_focus` | Ring of Focus |
| `chalice_of_echoes` | Chalice of Echoes |
| `veil_lens` | Veil Lens |
| `bone_charm` | Bone Charm |

### Weapons / gear (20)

weapons and worn gear

| id | Name |
|---|---|
| `occult_tool` | Occultist's Implement |
| `silver_blade` | Silver Blade |
| `hollow_pact_blade` | Hollow Pact Blade |
| `ritual_knife` | Ritual Knife |
| `wardens_maul` | Warden's Maul |
| `bone_club` | Bone Club |
| `star_iron_sword` | Star-Iron Sword |
| `tidal_trident` | Tidal Trident |
| `sacrificial_dagger` | Sacrificial Dagger |
| `censer_mace` | Censer Mace |
| `eye_scepter` | Eye Scepter |
| `veil_bow` | Veil Bow |
| `investigator_coat` | Investigator's Coat |
| `choir_robe` | Choir Robe |
| `wardens_plate` | Warden's Plate |
| `drowned_wetsuit` | Drowned Wetsuit |
| `cultist_habit` | Cultist's Habit |
| `occultists_hat` | Occultist's Hat |
| `boots_of_silence` | Boots of Silence |
| `gloves_of_precision` | Gloves of Precision |

### Conditions (14)

item-represented statuses (Bane/Boon/Deal/Illness/Madness)

| id | Name |
|---|---|
| `cursed_charm` | Cursed Charm |
| `marked_sigil` | Marked Sigil |
| `debt_note` | Debt Note |
| `dark_pact` | Dark Pact |
| `haunted_relic` | Haunted Relic |
| `diseased_token` | Diseased Token |
| `paranoia_token` | Paranoia Token |
| `hallucination_echo` | Hallucination Echo |
| `blessing_of_isis` | Blessing of Isis |
| `holy_water` | Holy Water |
| `holy_cross` | Holy Cross |
| `king_james_bible` | King James Bible |
| `kerosene` | Kerosene |
| `lantern` | Lantern |
| `grasping_idol` | Grasping Idol |

### Currency (6)

social/economy tokens

| id | Name |
|---|---|
| `mark_of_favour` | Mark of Favour |
| `relic_coin` | Relic Coin |
| `cult_token` | Cult Token |
| `black_obol` | Black Obol |
| `order_scrip` | Order Scrip |
| `barter_seal` | Barter Seal |

### Navigation (8)

maps, lenses and navigation aids

| id | Name |
|---|---|
| `city_map` | City Map |
| `world_atlas` | World Atlas |
| `cartographers_lens` | Cartographer's Lens |
| `compass_of_longing` | Compass of Longing |
| `sextant` | Sextant |
| `veil_compass` | Veil Compass |
| `marked_map` | Marked Map |
| `route_ledger` | Route Ledger |

### Factions (8)

cult/faction insignia and contracts

| id | Name |
|---|---|
| `cult_insignia` | Cult Insignia |
| `order_sigil` | Order Sigil |
| `choir_reed` | Choir Reed |
| `wardens_badge` | Wardens' Badge |
| `blood_contract` | Blood Contract |
| `oath_ring` | Oath Ring |
| `reputation_ledger` | Reputation Ledger |
| `faction_seal` | Faction Seal |

### Keys (6)

progression / endgame keys and foci

| id | Name |
|---|---|
| `veil_key` | Veil Key |
| `gate_fragment` | Gate Fragment |
| `banishment_focus` | Banishment Focus |
| `summoning_focus` | Summoning Focus |
| `ninth_sigil` | Ninth Sigil |
| `hollow_crown` | Hollow Crown |

### Utility (9)

field utility and atmosphere

| id | Name |
|---|---|
| `hand_lantern` | Hand Lantern |
| `oil_flask` | Oil Flask |
| `tinderbox` | Tinderbox |
| `bedroll` | Bedroll |
| `tent_kit` | Tent Kit |
| `water_skin` | Water Skin |
| `field_journal` | Field Journal |
| `rope_coil` | Rope Coil |
| `debug_tool` | Debug Tool |

