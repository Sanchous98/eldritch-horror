#!/usr/bin/env bash
# guarded-run.sh — refuse to start a heavy job unless the host is safe, and always run it
# with explicit hard memory/CPU caps so it can never OOM the host.
#
# Why: a docker socket escapes the cgroup, so a container spawned via the host daemon is not
# bounded by this container's limit and can OOM-kill dockerd (taking every container down).
# This script (a) never touches the host socket, (b) hard-caps the job, (c) checks free memory.
#
# Usage:  scripts/guarded-run.sh <command> [args...]
# Env:
#   GUARD_MIN_FREE_MB   minimum host free+available MB to start (default 4096)
#   GUARD_MAX_LOAD      maximum 1-min load average to start (default: nproc)
#   GUARD_MEM_MB        mem cap for the job itself if it is a docker run (default 2048)
set -euo pipefail

MIN_FREE_MB="${GUARD_MIN_FREE_MB:-4096}"
MAX_LOAD="${GUARD_MAX_LOAD:-$(nproc)}"
MEM_MB="${GUARD_MEM_MB:-2048}"

log() { printf '[guard] %s\n' "$*" >&2; }

# --- availability check -------------------------------------------------------------
if [ -r /proc/meminfo ]; then
  avail_kb=$(awk '/MemAvailable/{print $2}' /proc/meminfo)
  avail_mb=$((avail_kb / 1024))
else
  avail_mb=$MIN_FREE_MB
fi
load1=$(awk '{print $1}' /proc/loadavg 2>/dev/null || echo 0)

log "host: available=${avail_mb}MB load1=${load1} (need free>=${MIN_FREE_MB}MB, load<=${MAX_LOAD})"

if [ "$avail_mb" -lt "$MIN_FREE_MB" ]; then
  log "REFUSING: only ${avail_mb}MB available (< ${MIN_FREE_MB}MB). Free memory first, or wait."
  exit 3
fi
# integer compare for load (strip decimals)
load_int=${load1%.*}
if [ "${load_int:-0}" -gt "$MAX_LOAD" ]; then
  log "REFUSING: load ${load1} > ${MAX_LOAD}. Wait for the host to calm down."
  exit 4
fi

# --- forbid the escape hatch --------------------------------------------------------
if [ -n "${DOCKER_HOST:-}" ]; then
  log "WARNING: DOCKER_HOST is set (${DOCKER_HOST}); this job may escape the cgroup."
fi
if [ -S /var/run/docker.sock ]; then
  log "WARNING: a docker socket is visible. Prefer running jobs directly in this container."
fi

# If the job is a `docker run`, inject a hard cap defensively.
if [ "${1:-}" = "docker" ] && [ "${2:-}" = "run" ]; then
  shift 2
  log "running docker container with --memory=${MEM_MB}m --pids-limit=1024"
  exec docker run --memory="${MEM_MB}m" --memory-swap="${MEM_MB}m" --pids-limit=1024 "$@"
fi

log "starting: $*"
exec "$@"
