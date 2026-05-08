# Phase 1: Build And Entry - Context

**Gathered:** 2026-05-08
**Status:** Ready for planning

## Phase Boundary

Upgrade build system and mod entry point to compile against NeoForge 1.21.1. Deliverables: working Gradle build (NeoGradle + moddev plugin), NeoForge-compatible `mods.toml`, and updated `Energyblade.java` entry point. Success criterion: `./gradlew compileJava` succeeds with zero errors.

## Implementation Decisions

### Build Script Approach
- **D-01:** Use NeoForge MDK template structure with `net.neoforged.moddev` (moddevgradle) plugin, NOT `net.neoforged.gradle.userdev`
- **D-02:** Gradle wrapper upgraded to whatever version the NeoForge 1.21.1 MDK ships with
- **D-03:** Keep all 4 run configs (client, server, data, gameTestServer) in the new build
- **D-04:** Adapt processResources (mods.toml expansion), JAR manifest attributes, and Maven publishing to NeoGradle/MDK conventions — preserve intent, not exact syntax

### Mixin Strategy
- **D-05:** Strip mixin infrastructure entirely — delete `energyblade.mixins.json`, remove MixinGradle plugin, classpath dependency, and all mixin-related JVM args/run config properties

### Optional Dependencies
- **D-06:** Keep JEI as compileOnly (needed for Phase 6 `JEICompat`). Researcher must find correct NeoForge 1.21.1 JEI artifact coordinates (different from 1.20.1 Forge JEI)
- **D-07:** Remove all NBTEdit and Thermal Series dependencies (8 total: CoFH Core, CodeChicken Lib, Thermal Foundation/Expansion/Innovation/Locomotion/Integration/Dynamics)
- **D-08:** Add Mekanism as implementation (runtime only) for FE charging dev testing
- **D-09:** Keep CurseMaven, MMMaven, ModMaven repository declarations; remove flatDir `libs/` repository

### SlashBlade Dependency
- **D-10:** `implementation "mods.flammpfeil.slashblade:SlashBlade_Resharped:2.0.2-1.21.1"` from MMMaven — user confirmed this NeoForge 1.21.1 port version exists

### the agent's Discretion
- Exact NeoForge 1.21.1 version number — researcher to determine current release
- Exact MDK Gradle wrapper version — use whatever the MDK ships
- processResources / JAR manifest adaptation specifics — follow MDK conventions
- `mods.toml` format conversion details — researcher to confirm NeoForge 1.21.1 mods.toml spec

## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Migration Rules & Scope
- `AGENTS.md` — Migration rules, loader API research process, MCP tool usage, phase advancement rules
- `.planning/PROJECT.md` — Project definition, constraints, key decisions, out of scope
- `.planning/REQUIREMENTS.md` § BLD-01..BLD-06 — Build & entry requirements with traceability

### Phase Definition
- `.planning/ROADMAP.md` § Phase 1 — Scope, success criteria, key migration targets
- `docs/migration/MIGRATION_PLAN.md` § Phase 1 — Migration plan details
- `docs/migration/LOADER_API_MAP.md` — Confirmed Forge→NeoForge API mappings (currently empty, to be populated)
- `docs/migration/PROGRESS.md` — Current progress tracker

### Source Files (must migrate)
- `build.gradle` — ForgeGradle build script → NeoGradle
- `gradle.properties` — Version properties (forge_version → neoforge_version, etc.)
- `settings.gradle` — MinecraftForge maven → NeoForge maven
- `src/main/resources/META-INF/mods.toml` — Mod metadata for NeoForge format
- `src/main/java/cn/mmf/energyblade/Energyblade.java` — `@Mod` entry point, DeferredRegister, FML events

### Source Files (must remove)
- `src/main/resources/energyblade.mixins.json` — Empty mixin config, D-05

### Codebase Maps
- `.planning/codebase/STACK.md` — Build system details, dependencies, Forge APIs used
- `.planning/codebase/ARCHITECTURE.md` — Entry points, layers, event subscriptions
- `.planning/codebase/INTEGRATIONS.md` — SlashBlade, JEI, Thermal dependency details

## Existing Code Insights

### Reusable Assets
- Current `build.gradle` structure — reference for processResources variable expansion (mod_id, mod_version, etc.), JAR manifest attributes, and Maven publishing logic to adapt
- `gradle.properties` — all mod metadata properties preserved, only version strings change

### Established Patterns
- ForgeGradle `minecraft {}` DSL → NeoGradle `neoForge {}` or `minecraft {}` DSL
- `fg.deobf()` dependency wrapping → NeoForge native dependency handling
- `reobfJar` finalization → handled natively by NeoGradle
- `buildscript {}` MixinGradle classpath → removed entirely (D-05)
- `FMLJavaModLoadingContext` bootstrap → NeoForge `@Mod` / `IEventBus` pattern (research needed)

### Integration Points
- `Energyblade.java:27-30` — Constructor registers `DeferredRegister` on mod event bus and sets up `FMLCommonSetupEvent` listener for network
- `mods.toml:1` — `modLoader = "javafml"` must change for NeoForge
- `mods.toml:15` — `forge` dependency block replaced with `neoforge`
- All `net.minecraftforge.*` imports in `Energyblade.java` must be replaced with NeoForge equivalents

## Specific Ideas

- User specifically requested `net.neoforged.moddev` (moddevgradle) plugin, not the older userdev plugin
- User confirmed SlashBlade:Resharped NeoForge port exists at version `2.0.2-1.21.1` on MMMaven
- Mekanism runtime dependency requested as replacement for Thermal mods (FE charging dev test)

## Deferred Ideas

None — discussion stayed within phase scope.

---

*Phase: 1-Build And Entry*
*Context gathered: 2026-05-08*
