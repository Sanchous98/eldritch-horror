# Host safety — hard guarantees against OOM

This is not a guideline. It is the reason the dev container can no longer take the host
down. Read it before running anything heavy.

## What happened (root cause)

The dev container (`devbox-main`) had **`/var/run/docker.sock` mounted read-write** plus
passwordless `sudo`. A `docker compose run` therefore created a container that is a
**sibling** on the host daemon, **not a child of the dev container's cgroup**. The
`mem_limit` on the dev container bounded only the dev container; the containers it spawned
were unbounded by it. Under memory pressure the kernel OOM-killed **dockerd**, which took
down every container on the host.

**A memory limit inside a container with a docker socket is not a guarantee.** The socket
escapes the cgroup.

## The guarantee: break the escape

Two changes on the host (see the snippet below), in order of importance:

1. **Remove the docker socket from the dev container.** With no socket, everything the
   agent runs is a process **inside** the dev container, bounded by its cgroup. Nothing it
   does can exceed the cap or affect other containers.
2. **Give the dev container a hard cgroup cap** with no swap spill and a high OOM score, so
   even under pressure the kernel reclaims/kills *this* container and never `dockerd`:
   - `mem_limit` == `memswap_limit` (equal → **no swap**, cannot spill past the cap),
   - `pids_limit` (fork-bomb guard),
   - `cpus`,
   - `oom_score_adj` high (killed first, never the daemon).

If isolated build containers are genuinely needed, use a **rootless nested dockerd inside
the dev container** — its containers are then children of the dev container's cgroup and
inherit the cap. Never mount the host socket again.

## Host snippet (apply in `/app/data/projects/devbox/docker-compose.yml`)

```yaml
services:
  devbox-main:
    mem_limit: 6g
    memswap_limit: 6g        # == mem_limit: NO swap spill
    mem_reservation: 2g
    pids_limit: 2048
    cpus: 2
    oom_score_adj: 500       # kernel reclaims/kills me before dockerd
    # and DELETE this line (the whole problem):
    #   - /var/run/docker.sock:/var/run/docker.sock
```

Also remove the dev user's passwordless `sudo` (e.g. drop it from `/etc/sudoers.d`).

## What the agent does (in this repo)

- **Never** mounts or uses the host docker socket. Runs builds and the dev server
  **inside** the dev container (`./gradlew …`), so they are bounded by the container cgroup.
- Uses `scripts/guarded-run.sh`, which **refuses to start** a heavy job unless the host is
  safe (free memory / load), and always runs with explicit hard caps.
- One heavy job at a time; renders are batched and use a small heap.

See `AGENTS.md` for the working rule.
