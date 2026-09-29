#!/bin/sh
# Container entrypoint.
#
# Runs as root so it can:
#   1. normalise a Windows (CRLF) `gradlew` in case the source was copied from such a checkout;
#   2. make the named-volume mount points writable by the dev user — a freshly created
#      named volume is root-owned unless the image directory already existed with the right
#      ownership, and Gradle must write /workspace/build and /workspace/run.
# Then it drops privileges and runs the requested command as the dev user.
set -e

DEV_UID="${DEV_UID:-1000}"
DEV_GID="${DEV_GID:-1000}"

if [ -f /workspace/gradlew ]; then
    sed -i 's/\r$//' /workspace/gradlew 2>/dev/null || true
fi

for d in /workspace/build /workspace/run "${GRADLE_USER_HOME:-/home/dev/.gradle}"; do
    mkdir -p "$d" 2>/dev/null || true
    chown -R "$DEV_UID:$DEV_GID" "$d" 2>/dev/null || true
done

# Drop privileges. Some container runtimes forbid initgroups; --clear-groups avoids it,
# and if setpriv is unavailable we simply run as-is (already running as the intended user
# in most cases).
if command -v setpriv >/dev/null 2>&1; then
    setpriv --reuid="$DEV_UID" --regid="$DEV_GID" --clear-groups "$@" \
        || exec "$@"
else
    exec "$@"
fi
