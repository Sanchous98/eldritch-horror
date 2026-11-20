# CI / CD

Two GitHub Actions workflows live in `.github/workflows/`:

- **`build.yml` — CI** on every push to `main`/`master`, every pull request and manual dispatch.
- **`release.yml` — CD** on a `v*` tag (and manual dispatch, which only uploads the artifact).

## CI (`build.yml`)

1. `actions/checkout@v4`
2. `actions/setup-java@v4` — **Temurin JDK 25** (`build.gradle` sets the toolchain to Java 25).
3. `gradle/actions/setup-gradle@v4` — caches `~/.gradle`, including the NeoForge/Minecraft
   decompile cache, so subsequent runs are fast.
4. `./gradlew build --no-daemon --console=plain` — Gradle **9.8.0** from the wrapper. This
   includes `compileJava`, `test` (JUnit 5, `useJUnitPlatform()`) and `pmdMain`/`pmdTest`;
   a PMD violation fails the run (since `8c6d098` it is a build gate, not a manual task).
5. `./gradlew runGameTestServer --no-daemon --console=plain` — the headless 26.3 GameTest suite
   (`data/eldritch_horror/test_instance/*.json`); a failed assertion fails the run.
6. Uploads the mod jar (`eldritch-horror-jar`) and, always, the PMD/JUnit/GameTest reports.

A `concurrency` group cancels superseded runs, and `timeout-minutes: 75` gives the first run room
to decompile Minecraft (~5 min, ~4 GB) and boot the test server. No secrets are needed.

## CD (`release.yml`)

Triggered by a tag such as `v0.1.0`:

1. The same JDK/Gradle setup, then `./gradlew build` (a release cannot skip the tests/linter).
2. Writes `SHA256SUMS.txt` for the jar.
3. `gh release create "$GITHUB_REF_NAME"` publishes the jar and the checksum with generated
   release notes (`GITHUB_TOKEN`, `contents: write`).
4. A manual dispatch (no tag) skips the publish and uploads the same jar/checksum as an artifact.
5. Reports are uploaded on failure too.

The workflow never cancels an in-flight release (`cancel-in-progress: false`).

## Reproduce locally

CI builds with a normal Gradle invocation. To match it inside the dev container:

```bash
. ./scripts/safe-build.sh bash -lc \
  'docker compose run --rm --no-deps dev ./gradlew build --no-daemon --console=plain'
```

and the gameplay suite:

```bash
docker run --rm -e GRADLE_USER_HOME=/home/dev/.gradle \
  -v eldritch-horror-dev_gradle-cache:/home/dev/.gradle \
  -v eldritch-horror-dev_build-out:/workspace/build \
  eldritch-horror-dev:latest ./gradlew runGameTestServer --no-daemon --console=plain
```
