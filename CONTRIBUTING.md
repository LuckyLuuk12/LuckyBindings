# Contributing to LuckyBindings

---

## Setup

**Requirements:** JDK 21, IntelliJ IDEA (Eclipse unsupported), Git

1. Open the root folder as a project in IntelliJ IDEA.
2. Set Gradle JVM **and** Project SDK to **Java 21**.
3. Reload Gradle, then run a `Minecraft Client` run config to verify.

```bash
./gradlew build                 # build all loaders
./gradlew :fabric:runClient     # run Fabric client
./gradlew :neoforge:runClient   # run NeoForge client
```

---

## Key Rules

- **Write shared code in `common/`** — loader modules (`fabric/`, `neoforge/`, `forge/`) can access it, not the other way around.
- For anything loader-specific, use the `platform/services/` interfaces.
- Always provide a **non-null default value** for config options.
- For `ListOption`/`SetOption`, include **at least one default item** (the UI infers the item type from it).

---

## Releasing

1. Bump `version` in **`gradle.properties`**.
2. Commit, tag, and push:

```bash
git add gradle.properties
git commit -m "chore: bump version to x.y.z"
git tag vx.y.z
git push origin main --tags
```

GitHub Actions builds all loaders and publishes the release automatically.

---

For structure, architecture, and how to add new actions/options see [`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md).