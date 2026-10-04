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

See [`design/`](design/README.md) for the complete design bible (source of truth), and
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
> The very first build also **decompiles Minecraft** with **Vineflower** (a Fernflower fork, so its
> log says `org.jetbrains.java.decompiler.*`), which is memory- and CPU-heavy and the slowest step.
> It is a single pass with no percentage output; if the build dies during `decompile` with no other
> error, the container ran out of RAM — raise `DEV_MEM` (see below). Once the NeoForm cache is warm,
> later builds are quick.

## Development with Docker

> **Windows users: read [`docs/WINDOWS.md`](docs/WINDOWS.md) first.** Docker Desktop's
> file sharing breaks Minecraft/Gradle builds on Windows; run the game natively instead.

If you'd rather not install a JDK (or want a clean, reproducible toolchain), use the
dev container. It uses JDK 25 and keeps the Gradle cache in a named volume, so the
one-time NeoForge decompile happens **once** instead of on every run.

> **Windows:** `gradlew` must have LF line endings, or the Linux container fails with
> `/bin/sh^M: bad interpreter`. `.gitattributes` enforces this (`gradlew text eol=lf`),
> but if you cloned **before** that rule existed, refresh your checkout:
> ```powershell
> git pull
> git rm -r --cached . ; git reset --hard
> ```
> (or just re-clone). Do not let an editor or Git (`core.autocrlf`) convert `gradlew` to CRLF.

The source is **copied into the image** and all mutable state lives in **named volumes**
(`gradle-cache`, `build-out`, `run-data`) — there is no host bind mount. That is deliberate:
Docker Desktop on Windows cannot share `C:\` paths, so bind mounts are avoided on every OS
for a setup that behaves identically everywhere.

> **Rebuild the image after changing code:** `docker compose build dev` (or add `--build`
> to `up`). Since the source is baked in, edits aren't picked up until you rebuild.

> **Windows:** Docker Desktop's `C:\` file sharing is unreliable for this project. Prefer
> the native path in [`docs/WINDOWS.md`](docs/WINDOWS.md); if you use Docker, this
> baked-image setup (no bind mount) is the one that works.

```sh
docker compose build dev                             # rebuild after code changes
docker compose run --rm dev ./gradlew build          # build the mod
docker compose run --rm dev ./gradlew runData        # run data generators
docker compose --profile server up server            # dedicated server on :25565
docker compose run --rm dev bash                     # a shell in the environment
```

Build output and the server world persist in the named volumes across `run --rm`. To move
files in/out (e.g. edit `server.properties`, or copy the built jar to the host):

```sh
docker compose run --rm dev bash                     # world/config are at /workspace/run
docker compose cp dev:/workspace/build/libs/eldritch_horror-0.1.0.jar ./build/libs/
```

### Dev server (connect your own client)

Run a dedicated server in the container and join it from your normal Minecraft client
(Minecraft **26.3**):

```sh
docker compose --profile server up server
# then: Multiplayer -> Direct Connect -> localhost:25565
```

The port is published (`SERVER_PORT` overrides 25565). The world and logs land in `run/`
in the repo. The Earth map is server-side, so the server generates it — your client needs
nothing special. Stop with Ctrl-C.

### Windows: run natively (recommended)

Docker Desktop on Windows shares your `C:\...` project into a Linux VM over 9p/virtiofs,
which is slow and **fails under heavy builds** with `FileHasher: Input/output error` or
`AccessDeniedException` (Windows locks files exclusively). This is a Docker Desktop
limitation, not the project. Two reliable paths:

**Native (simplest):** with JDK 25 installed, run on the host — no Docker.

```powershell
.\gradlew.bat runClient      # opens the game with the mod
.\gradlew.bat runServer      # dedicated server on localhost:25565
```

**Or Docker with the repo inside WSL2** (avoids the 9p boundary entirely):

```bash
# in an Ubuntu WSL2 shell
cd ~ && git clone git@github.com:Sanchous98/eldritch-horror.git
cd eldritch-horror && docker compose --profile server up server
# connect from Windows Minecraft to localhost:25565
```

If a build fails with `Input/output error`, clear the stale build state and retry:

```powershell
docker compose --profile server down -v
rmdir /s /q .gradle
rmdir /s /q build
```

**Pick one path.** Do not run a Docker build and a host build at the same time — they
share the repo's `build/` folder, and Windows locks files exclusively.

If you hit `…minecraft-patched-….jar is locked` (or `AccessDeniedException` on a `.tmp`
move), a process still holds the jar open. Close it and clear the artifact:

```powershell
# stop any dev game / server (close the window, or Ctrl-C the terminal)
# stop Docker if it was running:
docker compose --profile server down
docker ps -a

# kill stray Java/Gradle holders, then clear the locked artifact
taskkill /F /IM java.exe
rmdir /s /q build\moddev

# retry
.\gradlew.bat runClient
```

A running dev client/server keeps the patched jar on its classpath, so you cannot rebuild
while it is open — always quit the game first.

#### "Cannot connect: not in server's whitelist"

The dev server's settings live in `run\server.properties`. For a private dev server:

```properties
white-list=false
enforce-whitelist=false
online-mode=false        # only for an offline/dev client; true for a real account
```

Or add yourself while the server runs, from its console: `whitelist add <YourName>`.
The server files (`run\server.properties`, `run\world\`, `run\ops.json`) are git-ignored and
safe to edit.

### Dev client in Docker (Linux)

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
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — code layout, layers and conventions.
- [`docs/WORLDGEN.md`](docs/WORLDGEN.md) — the Overworld map generator (continents, oceans, vanilla biomes).

## Roadmap & progress

The project board (linked below) is the full backlog; `docs/STATUS.md` is the current handoff
snapshot.

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
