# Windows quickstart

**Run the game natively. Do not use Docker on Windows for this project.** Docker Desktop
shares `C:\...` into a Linux VM, which breaks under Minecraft/Gradle builds — you will see
`Input/output error`, `AccessDeniedException`, `host_mnt ... read-only file system`, or CRLF
errors. None of these are project bugs; they are Docker Desktop's Windows filesystem layer.
Native avoids all of them.

## Requirements

- **JDK 25** (Minecraft 26.3 requires Java 25). Check: `java -version`.
  - If missing, install [Eclipse Temurin 25](https://adoptium.net/temurin/releases/?version=25).
- **Minecraft 26.3** client, **NeoForge 26.3** installed (for the last step).
- Git. The wrapper (`gradlew.bat`) downloads Gradle itself.

## Client (play the mod)

From the repo folder in PowerShell:

```powershell
# 1. Make sure nothing is holding the build (a running game, a Gradle daemon, Docker, the IDE).
.\gradlew.bat --stop
taskkill /F /IM java.exe 2>$null

# 2. Launch the dev client with the mod.
.\gradlew.bat runClient
```

The **first** run decompiles Minecraft (~5 min, needs ~4 GB free RAM). Afterwards it opens
the game and **Eldritch Horror** is loaded.

### See the Earth map + cities

Create a **new world** (the generator only applies to fresh chunks), then:

```
/gamemode spectator
/tp 6360 100 -1624     # Tokyo
/tp -5 100 -2344       # London
```

## Server (host a world)

```powershell
.\gradlew.bat runServer
# Minecraft 26.3 client -> Multiplayer -> Direct Connect -> localhost:25565
```

Dev server settings are in `run\server.properties` (git-ignored). For a private dev server:

```properties
white-list=false
enforce-whitelist=false
online-mode=false        # for an offline/dev client; true for a real Microsoft account
```

Or add yourself while it runs, from the server console: `whitelist add <YourName>`.

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `gradlew: /bin/sh^M: bad interpreter` | CRLF line endings | `git pull` (`.gitattributes` forces LF); or `git config --global core.autocrlf false` and re-clone |
| `...minecraft-patched-*.jar is locked` / `AccessDeniedException` | a process holds the jar (running game, Gradle daemon, Docker, antivirus) | quit the game; `.\gradlew.bat --stop`; `taskkill /F /IM java.exe`; `rmdir /s /q build\moddev` |
| `FileHasher ... Input/output error` | you're running Docker on Windows | run natively (this page) |
| `host_mnt/c ... read-only file system` | Docker Desktop file sharing broke | don't use Docker; or reset Docker Desktop and use WSL2 |
| Build dies during **decompile** with no other error | out of RAM | close other apps; ensure ~4 GB free |

**One build at a time.** Never run Docker and a native build together — they share `build/`,
and Windows locks files exclusively.

## If you insist on Docker

Clone the repo **inside WSL2**, not on `C:`:

```bash
wsl -d Ubuntu
cd ~ && git clone git@github.com:Sanchous98/eldritch-horror.git
cd eldritch-horror && docker compose --profile server up server
```

Your Windows Minecraft client still connects to `localhost:25565`. Editing is easiest with
VS Code's **WSL** remote.
