# Working rules (for AI agents on this repo)

> **After a context compaction:** read `docs/STATUS.md` first (the current snapshot), then
> this file. Durable truth lives in the repository (`design/`, `docs/`), never only in chat.

## Host safety — no OOM, ever

**Never use the host `docker.sock`.** A container spawned through it is a *sibling* on the
host daemon, escapes this container's cgroup, and can OOM-kill `dockerd` — which takes every
container on the host down. This happened once (see `docs/HOST-SAFETY.md`). Run everything
**inside** the dev container (`./gradlew …`), bounded by its cgroup.

- **Preflight**: before any heavy job, check free memory and load; refuse if the host is
  tight. Use `scripts/guarded-run.sh <cmd>` — it refuses when the host is unsafe and hard-caps
  the job. Do not bypass it.
- **Hard caps** live in `docker-compose.yml` (`mem_limit == memswap_limit`, `pids_limit`,
  `cpus`). Do not raise them to "make it fit".
- **One heavy job at a time.** Renders are batched with a small heap.
- If the host is loaded, **wait** — do not start anything.

## Context discipline — delegate to subagents
**Do not do bulk work inline.** This project produces large artefacts and long logs
(bake tooling, decompilation, Java sources, build output). Reading all of it into the main
context wastes the session and loses the thread.

**Use subagents for:**
- exploration: "where is X", "how does Y work", mapping a subsystem;
- bulk edits: renaming, applying the same change across many files;
- independent location/structure work (see `docs/STRUCTURES-CONTRACT.md`);
- reading long build/decompile logs and reporting only the verdict + errors;
- generating lists (all locations, all data files) without dumping contents.

**Keep in the main context only:** decisions, short diffs, and results. Summarise, do not
transcribe. When a task is mechanical and self-contained, hand it to a subagent and bring
back the outcome.

## One writer per area

Parallel work is expected (see the contract). Each worker touches **only its own files**
and never edits shared interfaces. The shared interfaces (structure builder, palettes,
registries, the generator) are **frozen**; changes to them land one at a time, with the
integration build green.

## Build discipline

- One Gradle build at a time; they share `build/` and Windows locks files exclusively.
- Verify with `./gradlew compileJava` (fast, warm cache) before claiming done.
- The first build decompiles Minecraft (~5 min, ~4 GB); later builds are quick.

## Where things live

| Concern | File |
|---|---|
| Host safety (no OOM) | `docs/HOST-SAFETY.md` |
| Current status / handoff | `docs/STATUS.md` |
| Design (source of truth) | `design/` |
| Code architecture | `docs/ARCHITECTURE.md` |
| World generation | `docs/WORLDGEN.md` |
| Locations / parallel authoring | `docs/STRUCTURES-CONTRACT.md` |
| Multiplayer & sessions | `docs/MULTIPLAYER.md` |
| Windows/Docker pitfalls | `docs/WINDOWS.md` |
