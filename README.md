# Eldritch Horror

An RPG-flavoured **eldritch horror** mod for **Minecraft 26.3 / NeoForge**. Sanity, corruption,
cults, forbidden rituals, and things that should not be — built as a progression system rather
than a monster pack.

> **Status: skeleton.** This repository currently contains the build, mod metadata, package
> layout, design documents and compiling placeholder registries. No gameplay is implemented yet.

## Design at a glance

The mod is built on three RPG axes and one progression engine:

| System | What it is | Player-facing |
|---|---|---|
| **Sanity** | A per-player meter that drains near the eldritch and recovers with rest/light. | The "health" of your mind; at zero, hallucinations and madness. |
| **Corruption** | A durable exposure value from forbidden knowledge and rituals; barely reversible. | The cost axis: power always leaves a mark. |
| **Cult reputation** | Faction standing gating rites, reagents and services. | Social progression; cults oppose each other. |
| **Rituals** | Data-driven altar recipes with a block pattern + offerings + outcomes. | Quest-like advancement and the mod's boss/content unlocks. |

See [`design/`](design/README.md) for the complete design bible (source of truth), or
[`docs/DESIGN.md`](docs/DESIGN.md) for a short summary and
[`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for how the code is laid out.

## Requirements

- **JDK 25** (Minecraft 26.3 requires Java 25).
- No Gradle install needed — the wrapper is committed (`./gradlew`).

**Or skip the local install entirely — see [Development with Docker](#development-with-docker).**

## Build & run

```sh
./gradlew build                 # compile + build the mod jar (build/libs/)
./gradlew runClient             # launch a dev client with the mod
./gradlew runServer             # launch a dev server
./gradlew runData               # run data generators into src/generated/resources/
```

> The Gradle wrapper and ModDevGradle will download the Minecraft/NeoForge artifacts on first run.
> The very first build also **decompiles Minecraft** (26.3 uses IntelliJ's Fernflower; earlier
> versions used Vineflower), which is memory-heavy and the slowest step. If the build dies during
> `decompile` with no other error, the host ran out of RAM — give Docker/Gradle more memory. Once
> the NeoForm cache is warm, later builds are quick.

## Development with Docker

If you'd rather not install a JDK (or want a clean, reproducible toolchain), use the
dev container. It uses JDK 21 and keeps the Gradle cache in a named volume, so the
one-time NeoForge decompile happens **once** instead of on every run.

```sh
docker compose run --rm dev ./gradlew build          # build the mod
docker compose run --rm dev ./gradlew runData        # run data generators
docker compose run --rm dev ./gradlew runServer      # headless dev server
docker compose run --rm dev bash                     # a shell in the environment
```

The repo is bind-mounted at `/workspace`, so edits on the host are picked up instantly.
Artifacts land in `build/` on your host, owned by you (the image matches your `UID`/`GID`).

> **The bind mount resolves on the Docker daemon's host.** That is fine locally (and under
> Docker Desktop), but if you point `DOCKER_HOST`/a context at a **remote** daemon, the
> mounted path is read from *that* machine, not yours. Run the dev container against a
> local daemon (or clone the repo on the remote host first).

### Dev client (needs a display + GPU)

`runClient` opens a window, so it needs X11/Wayland and a GPU — on Linux:

```sh
xhost +si:localuser:$(id -un)                        # let the container use your display
docker compose --profile gpu run --rm dev-gpu ./gradlew runClient
```

The `gpu` profile forwards `DISPLAY` and the X11 socket. For an NVIDIA GPU, install the
[NVIDIA Container Toolkit](https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/latest/install-guide.html)
and uncomment the `deploy:` block in `docker-compose.yml`. On macOS/Windows Docker
Desktop there is no GPU/X11 pass-through; run the client natively there and use Docker
only for builds.

> **Memory:** the first in-container build decompiles Minecraft (26.3 uses IntelliJ's
> Fernflower), which is memory- and CPU-heavy. The compose service is capped at
> `cpus: 3` / `mem_limit: 4g` by default — raise them via `DEV_CPUS` / `DEV_MEM` if the
> decompile is too slow or gets OOM-killed, e.g. `DEV_MEM=8g docker compose run --rm dev ./gradlew build`.
> **Note:** these limits must be set on the object that *runs the build* (the container),
> not on a shell that merely calls `docker` — a container talking to a separate Docker
> daemon does not pass its own cgroup limits to the build.

## Project layout

```
build.gradle                    ModDevGradle build (NeoForge 2.0.x plugin)
settings.gradle                 Plugin/toolchain resolution
gradle.properties               versions + mod metadata (single source of truth)
src/main/templates/…/neoforge.mods.toml   mod metadata template (${…} from gradle.properties)
src/main/java/…/eldritchhorror/
    EldritchHorror.java         @Mod entry point (thin: registers + config only)
    core/                       config and shared plumbing
    registry/                   DeferredRegisters (blocks, items, entities, effects, sounds)
    sanity/  corruption/  cult/  ritual/    one package per RPG system
    client/                     client-only setup (HUD, overlays, renderers)
src/main/resources/             assets, lang, pack.mcmeta
docs/                           design + architecture
```

## Contributing

The project is early; conventions are deliberately small:

- Keep the `@Mod` class thin — feature code belongs in its system package.
- One `DeferredRegister` per registry, in `registry/`, registered from the entry point.
- Never reference a `client/` class from common code path.

## Design & content

- [`design/`](design/) — **the complete game design bible (source of truth)**: overview, pillars,
  progression, the three axes (sanity/corruption/reputation), the ritual engine, factions, horror
  & atmosphere, endgame, multiplayer, UI/UX, and all content (rituals, cults, quests, classes,
  skills, items, mobs, bosses, events, map).
- [`docs/DESIGN.md`](docs/DESIGN.md) — a short summary of the systems.
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — code layout, layers and conventions.

## Roadmap & progress

Implementation is tracked as GitHub **issues** (one per story) grouped into
**milestones** (`M1 Foundations` → `M5 Content & Endgame`, plus `Backlog`), and mirrored
onto a Project board: [**Eldritch Horror — Roadmap**](https://github.com/users/Sanchous98/projects/2).

Each issue carries a scoped body (**Do** / **Acceptance**) and labels (`sanity`,
`corruption`, `cult`, `ritual`, `content`, `client`, `build`, `data`, `networking`,
`tech-debt`, `story`). The board adds **Priority** (P0–P3) and **Area** fields.

The seeding is scripted and idempotent, so the roadmap can be re-applied or adapted:

```sh
python3 scripts/seed_issues.py     # labels, milestones, issues (needs repo scope)
python3 scripts/setup_project.py   # project board + fields (needs project scope)
```


## License

MIT — see [`LICENSE`](LICENSE).
