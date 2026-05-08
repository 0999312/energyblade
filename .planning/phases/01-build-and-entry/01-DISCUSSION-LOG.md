# Phase 1: Build And Entry - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-05-08
**Phase:** 1-Build And Entry
**Areas discussed:** Build script approach, Mixin strategy, Optional dependencies, SlashBlade dependency

---

## Build Script Approach

| Option | Description | Selected |
|--------|-------------|----------|
| Full MDK template | Replace build.gradle with NeoForge 1.21.1 MDK structure — new plugin block, neoForge DSL, neoGradle runs | ✓ |
| Minimal line-by-line | Keep existing structure, replace specific parts only | |

**User's choice:** Full MDK Template, but use moddev (moddevgradle) — specifically `net.neoforged.moddev`, not `net.neoforged.gradle.userdev`.

### Gradle Wrapper

| Option | Description | Selected |
|--------|-------------|----------|
| MDK default | Upgrade to whatever NeoForge 1.21.1 MDK ships with | ✓ |
| Keep 8.6 | Keep current Gradle wrapper version | |

**User's choice:** MDK default (Recommended).

### Run Configs

| Option | Description | Selected |
|--------|-------------|----------|
| Keep all 4 | client, server, data, gameTestServer | ✓ |
| Essential only | client, server, data only | |

**User's choice:** Keep all 4.

### Build Artifacts (Resources, JAR, Publishing)

| Option | Description | Selected |
|--------|-------------|----------|
| Adapt to MDK conventions | Preserve intent (variable expansion, JAR manifest, publishing) but follow MDK patterns | ✓ |
| Keep all existing logic | Faithfully reproduce current logic in new format | |

**User's choice:** Adapt to MDK conventions (Recommended).

---

## Mixin Strategy

| Option | Description | Selected |
|--------|-------------|----------|
| Strip completely | Delete energyblade.mixins.json, remove MixinGradle entirely | ✓ |
| Migrate to NeoForge mixin | Keep config, migrate to NeoForge's native mixin support | |

**User's choice:** Strip completely (Recommended).

---

## Optional Dependencies

### Core Decision

| Option | Description | Selected |
|--------|-------------|----------|
| Keep JEI only | JEI compileOnly, remove NBTEdit + Thermal mods | |
| Comment all out | Move deps to comments | |
| Remove all | Clean start | |
| Custom | Keep JEI, remove everything else & add MEK | ✓ |

**User's choice:** Keep JEI, remove everything else & add MEK. Mekanism as implementation (runtime only) — for FE charging dev testing, replacing Thermal mods.

### Repositories

| Option | Description | Selected |
|--------|-------------|----------|
| Start fresh | Only add repos actually needed | |
| Preserve existing repos | Keep CurseMaven, MMMaven, ModMaven, libs/ | |
| Keep existing, remove flatDir | Keep CurseMaven, MMMaven, ModMaven; remove flatDir libs/ | ✓ |

**User's choice:** Keep the existing repo, remove flatDir libs/.

### JEI Version

| Option | Description | Selected |
|--------|-------------|----------|
| Defer to research | Researcher looks up correct NeoForge 1.21.1 JEI artifact | ✓ |
| I'll specify it | User provides exact JEI version | |

**User's choice:** Defer to research (Recommended).

---

## SlashBlade Dependency

| Option | Description | Selected |
|--------|-------------|----------|
| Assume NeoForge port exists | Reference a NeoForge coordinate, deal with failure at compileJava | |
| Stub for compile only | Create minimal stub JAR with needed signatures | |
| Block on upstream port | Wait for SlashBlade NeoForge port | |
| Specific version | User provided exact version | ✓ |

**User's choice:** `implementation "mods.flammpfeil.slashblade:SlashBlade_Resharped:2.0.2-1.21.1"` from MMMaven. User confirmed this NeoForge 1.21.1 version exists.

---

## the agent's Discretion

- SlashBlade dependency handling (user confirmed specific version after discussion)
- Exact NeoForge 1.21.1 version number — researcher determines from Maven
- Exact MDK Gradle wrapper version — use whatever the MDK ships
- processResources / JAR manifest adaptation details — follow MDK patterns
- `mods.toml` format conversion — researcher confirms NeoForge 1.21.1 spec
- JEI NeoForge 1.21.1 artifact coordinates — researcher to find
- Mekanism NeoForge 1.21.1 artifact coordinates — researcher to find

## Deferred Ideas

None — discussion stayed within phase scope.
