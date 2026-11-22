# City briefs — 24 curated cities

One authored plan per curated city (`world/loc/city/plan/<City>Plan.java`), one city agent at a
time. The frozen rules are in `docs/CITY-PLAN-CONTRACT.md`; the first full example is
`docs/city-briefs/shanghai.md`. This file is the identity index so the 24 cities do not blur
together.

| City | Landmark | Districts | Two local quests | Secret |
|---|---|---|---|---|
| Shanghai | Pearl Tower | Bund, Nanjing Road, Old City, French Concession, Pudong docks | The Bund Cargo; The Vanishing Violinist | cellar under the Old City tea house |
| Beijing | Hall of Supreme Harmony | Forbidden City, hutongs, Temple of Heaven, silk market, canal | The Missing Eunuch; The Sealed Decree | well/tunnel under a hutong courtyard |
| Tokyo | five-level pagoda | Asakusa, Ginza, Yanaka, docks, shrine hill | The Cursed Film Reel; The Missing Miko | crypt under the pagoda |
| Seoul | Gyeongbokgung throne hall | hanok village, Namdaemun market, modern quarter, river | The Royal Ghost; The Missing Scholar | tomb under the palace garden |
| Bangkok | Thai temple | Grand Palace, klong market, Chinatown, backpacker street | The Stolen Amulet; The River Spirit | temple crypt behind the ordination hall |
| Jakarta | colonial town hall | Old Batavia, harbour, market, kampung | The Drowned Ledger; The Missing Sailor | warehouse cellar in the old port |
| Delhi | domed mausoleum | Chandni Chowk, Red Fort, bazaar, railway | The Missing Bride; The Stolen Relic | stepwell beneath the bazaar |
| Mumbai | Gateway of India | docks, Crawford Market, Malabar Hill, mill district | The Dock Strike; The Missing Actor | mill cellar |
| Istanbul | Hagia Sophia | Grand Bazaar, Galata, Bosphorus, cistern | The Smuggled Icon; The Missing Dragoman | underground cistern |
| Cairo | great mosque | Khan el-Khalili, Nile corniche, Citadel, City of the Dead | The Stolen Papyrus; The Missing Archaeologist | tomb in the City of the Dead |
| Lagos | courtyard palace | marina, market, lagoon, colonial quarter | The Oil Ledger; The Missing Fisherman | shrine on the lagoon |
| Nairobi | KICC tower | railway station, market, colonial club, outskirts | The Missing Surveyor; The Poached Relic | under the railway station |
| Cape Town | Cape Dutch manor | harbour, Bo-Kaap, Company's Garden, District Six | The Missing Mountaineer; The Sealed Letter | wine cellar |
| Moscow | St Basil's | Red Square, metro, Arbat, industrial district | The Stolen Icon; The Missing Commissar | metro tunnel |
| London | St Paul's + Big Ben | docks, East End, British Museum, Underground | The Missing Expedition; The Cursed Mummy | sewer/Underground junction |
| Paris | Notre-Dame | Louvre, Montmartre, catacombs, river | The Stolen Painting; The Missing Dancer | catacombs |
| Rome | St Peter's | Forum, Trastevere, catacombs, Vatican | The Stolen Relic; The Missing Priest | catacombs |
| Sydney | Opera House sails | harbour, The Rocks, Bondi, rail yards | The Missing Sailor; The Smuggled Cargo | sea cave |
| New York | Empire State | Manhattan grid, docks, Harlem, subway | The Missing Reporter; The Bootlegger | subway tunnel |
| Los Angeles | mission church | Hollywood, downtown, oil fields, beach | The Missing Starlet; The Cult Studio | studio basement |
| Mexico City | cathedral | Zócalo, canals, market, ruins | The Missing Curator; The Stolen Idol | Aztec tunnel |
| Rio de Janeiro | Christ the Redeemer | Copacabana, favela, port, botanical garden | The Missing Dancer; The Smuggled Relic | hillside cave |
| Buenos Aires | beaux-arts palace | Plaza de Mayo, tango hall, port, cemetery | The Missing Heiress; The Stolen Tango | crypt |
| Lima | colonial cathedral | Plaza Mayor, port, barrios, ruins | The Missing Archaeologist; The Stolen Quipu | coastal tomb |

## Rules for a city agent

- Own exactly one file: `src/main/java/com/sanchous98/eldritchhorror/world/loc/city/plan/<City>Plan.java`.
  Never edit `CityPlans`, `CityLocation`, `StructureBuilder`, styles or other cities.
- `halfExtent()` returns 300..400 (the user chose the current 600–800 footprint).
- Place every block through `StructureBuilder`; no `RandomSource`, no `Math.random`, no static
  mutable state, no chunk-local reads. Authored arrays/loops are fine; randomness is not.
- Include the arrival gate + signpost, three or more districts, the landmark, 6+ POIs, 2 local
  quests, 3+ NPC anchors, 4+ readable signs, and one secret. Use `CityPoi` for NPC/quest/POI
  anchors; the runtime hook comes later.
- Keep streets/doors connected; do not overlap a building with a street, quay or landmark.
- Include a `STATION` `CityPoi` (rail yard/station at the edge, with a straight track approach)
  for every city, and a `PORT` `CityPoi` (harbour/dock at the water edge) for coastal cities. The
  transport pass will connect the authored station/port to the rail and ship networks.
- The lead adds the `CityPlans.register(new <City>Plan())` line after the agent returns.
- A later quest pass adds a 3–5 step starting chain per city and may adjust the plan (extra door,
  cellar, witness, item spawn). Keep `QUEST_BOARD`/`NPC`/`SECRET`/`SIGN` anchors explicit.
