# Host safety — rootless docker inside the dev container

The dev container runs its **own rootless Docker daemon**, with **no access to the host
`docker.sock`**. Containers spawned here are **children of the dev container's cgroup**, so
they are bounded by its `memory.max` and can never OOM the host.

## Verified facts

- `dockerd-rootless.sh` (rootlesskit + slirp4netns + fuse-overlayfs), storage `overlayfs`.
- Host socket `/var/run/docker.sock` is **not reachable** from here (`no such file`).
- Dev cgroup `memory.max = 6442450944` (6 GiB); a nested `docker run` sees the **same** 6 GiB
  — i.e. it inherits this cgroup and cannot exceed it.
- `sudo` is no longer passwordless.

## Starting the daemon (each session; it does not auto-start)

```bash
export XDG_RUNTIME_DIR=/home/dev/.docker-run
export DOCKER_HOST=unix:///home/dev/.docker-run/docker.sock
# run it as a long-lived background process:
dockerd-rootless.sh --iptables=false --ip6tables=false --bridge=none
```

Then `docker` (CLI) talks to the rootless daemon via `$DOCKER_HOST`.

## Rules

- **Never** point `DOCKER_HOST` at `/var/run/docker.sock`; never mount the host socket.
- Run heavy jobs through `scripts/guarded-run.sh` (refuses on low host memory / high load).
- Keep `mem_limit == memswap_limit` in compose (no swap spill) and prefer small heaps
  (`-Xmx1g`–`-Xmx2g`). One heavy job at a time.
- Port publishing from nested containers uses slirp4netns: bind on `0.0.0.0` inside the nested
  container and publish the port; the dev container must itself publish it to the host.
