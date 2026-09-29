#!/bin/sh
# Container entrypoint. Normalises a Windows (CRLF) `gradlew` in the bind-mounted
# repo before running the command, so `/bin/sh^M: bad interpreter` cannot happen.
# Harmless when the file is already LF.
set -e

if [ -f /workspace/gradlew ]; then
    sed -i 's/\r$//' /workspace/gradlew 2>/dev/null || true
fi

exec "$@"
