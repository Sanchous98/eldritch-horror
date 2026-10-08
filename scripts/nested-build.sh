#!/usr/bin/env bash
# Build + verify the mod when only a nested rootless Docker daemon is available.
#
# The dev container runs its own dockerd (see docs/HOST-SAFETY.md). Its default slirp network
# has no working DNS/route, so containers spawned here cannot reach Maven/Mojang. Running the
# build container with --network host shares the outer container's network (which does have
# internet) and lets Gradle resolve NeoForge/Minecraft.
#
# The dev image bakes the source tree, so this rebuilds the image first (fast, layer-cached),
# then runs build + pmdMain. Usage: scripts/nested-build.sh [extra gradle args...]
set -euo pipefail

cd "$(dirname "$0")/.."
export XDG_RUNTIME_DIR="${XDG_RUNTIME_DIR:-/home/dev/.docker-run}"
export DOCKER_HOST="${DOCKER_HOST:-unix:///home/dev/.docker-run/docker.sock}"

if ! docker info >/dev/null 2>&1; then
    echo "nested-build: docker daemon not reachable at $DOCKER_HOST" >&2
    echo "start it (see docs/HOST-SAFETY.md) : dockerd-rootless.sh --iptables=false --ip6tables=false" >&2
    exit 1
fi

echo "nested-build: rebuilding image (source is baked in)"
docker build -f Dockerfile.dev -t eldritch-horror-dev:latest . >/dev/null

echo "nested-build: running build + pmdMain"
docker run --rm --network host \
    -e GRADLE_USER_HOME=/home/dev/.gradle -e EH_DEV=true \
    -v eldritch-horror-dev_gradle-cache:/home/dev/.gradle \
    -v eldritch-horror-dev_build-out:/workspace/build \
    -v eldritch-horror-dev_run-data:/workspace/run \
    eldritch-horror-dev:latest \
    ./gradlew build pmdMain --no-build-cache --no-daemon --console=plain "$@" \
    2>&1 | grep -iE -e "error:" -e "symbol:" -e "location:" -e "FAILED" -e "BUILD" -e "violation" \
    | tail -60
