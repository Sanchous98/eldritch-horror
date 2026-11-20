# CI

`.github/workflows/build.yml` builds the mod on every push to `main`/`master` and on every
pull request, so a broken compile or test is caught without a local run. The built jar is
uploaded as the `eldritch-horror-jar` workflow artifact.

## What it runs

1. `actions/checkout@v4`
2. `actions/setup-java@v4` — **Temurin JDK 25** (`build.gradle` sets the toolchain to
   Java 25).
3. `gradle/actions/setup-gradle@v4` — caches `~/.gradle`, including the NeoForge/Minecraft
   decompile cache, so subsequent runs are fast.
4. `./gradlew build --no-daemon --console=plain` — Gradle **9.8.0** from the wrapper. This
   includes `compileJava`, `test` (JUnit 5, `useJUnitPlatform()`) and `pmdMain`/`pmdTest`;
   a PMD violation fails the run.

A `concurrency` group cancels superseded runs, and `timeout-minutes: 60` gives the first
run room to decompile Minecraft (~5 min, ~4 GB). No secrets, publishing, or release steps.

## Reproduce locally

CI builds with a normal Gradle invocation. To match it inside the dev container:

```bash
. ./scripts/safe-build.sh bash -lc \
  'docker compose run --rm --no-deps dev ./gradlew build --no-daemon --console=plain'
```
