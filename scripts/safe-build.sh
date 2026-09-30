#!/usr/bin/env bash
# Memory-safe wrapper for Gradle/Docker work in the dev container.
#
# Why: the rootless dockerd and its build containers share this container's cgroup
# (6 GiB). Lingering containers + a Gradle daemon can push the cgroup over the limit and
# the OOM killer takes down dockerd. This wrapper:
#   1. prunes stopped containers and kills stray gradle JVMs before running,
#   2. refuses to start if the host or cgroup is already tight (unless FORCE=1),
#   3. runs the given command once, serially,
#   4. prunes again afterwards.
#
# Usage:
#   scripts/safe-build.sh ./gradlew compileJava --no-daemon --console=plain
#   scripts/safe-build.sh bash -lc 'docker compose build dev && docker compose run --rm dev ./gradlew build'
set -uo pipefail

export XDG_RUNTIME_DIR=${XDG_RUNTIME_DIR:-/home/dev/.docker-run}
export DOCKER_HOST=${DOCKER_HOST:-unix:///home/dev/.docker-run/docker.sock}

CG_MAX=$(cat /sys/fs/cgroup/memory.max 2>/dev/null || echo 0)
CG_CUR=$(cat /sys/fs/cgroup/memory.current 2>/dev/null || echo 0)
HOST_AVAIL_MB=$(free -m | awk '/^Mem:/{print $7}')
HOST_SWAP_USED_MB=$(free -m | awk '/^Swap:/{print $3}')

if [ "${FORCE:-0}" != "1" ]; then
  # Refuse when the cgroup is >80% full or the host has <400 MB available.
  if [ "$CG_MAX" -gt 0 ] && [ "$CG_CUR" -gt $(( CG_MAX * 8 / 10 )) ]; then
    echo "safe-build: cgroup at $(( CG_CUR / 1048576 )) MiB / $(( CG_MAX / 1048576 )) MiB — too full, refusing." >&2
    exit 3
  fi
  if [ "$HOST_AVAIL_MB" -lt 400 ]; then
    echo "safe-build: host available ${HOST_AVAIL_MB} MiB — too tight, refusing (FORCE=1 to override)." >&2
    exit 3
  fi
fi

echo "safe-build: cgroup $(( CG_CUR / 1048576 ))/$(( CG_MAX / 1048576 )) MiB, host avail ${HOST_AVAIL_MB} MiB, swap used ${HOST_SWAP_USED_MB} MiB"

# 1. pre-clean
docker container prune -f >/dev/null 2>&1 || true
for c in $(docker ps -q 2>/dev/null); do
  docker exec "$c" pkill -f gradlew >/dev/null 2>&1 || true
done

# 3. run once
"$@"
rc=$?

# 4. post-clean
docker container prune -f >/dev/null 2>&1 || true

echo "safe-build: exit=$rc"
exit $rc
