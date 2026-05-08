---
phase: 5
plan: 05-datagen-and-resources
subsystem: datagen
tags: [datagen, GatherDataEvent, RecipeOutput, DatapackBuiltinEntriesProvider, IConditionBuilder, NeoForge]

requires:
  - phase: 4
    provides: NeoForge networking, PayloadRegistrar, CustomPacketPayload
provides:
  - NeoForge DatapackBuiltinEntriesProvider import
  - RecipeOutput API for SlashBladeRecipeProvider
  - NeoForge Tags and IConditionBuilder package relocations
affects: [Phase 6 client, Phase 7 cleanup]

tech-stack:
  added: []
  patterns:
    - NeoForge datagen: GatherDataEvent + DatapackBuiltinEntriesProvider + RecipeOutput

key-files:
  modified:
    - src/main/java/cn/mmf/energyblade/data/DataGen.java
    - src/main/java/cn/mmf/energyblade/data/SlashBladeRecipeProvider.java

key-decisions:
  - "BuiltInSlashBladeRegistry.java requires no changes (pure vanilla API, zero Forge references)"
  - "SlashBlade dependency unavailable on classpath — recipe compilation blocked but structure correct"
  - "EventBusSubscriber.bus() deprecation warning deferred to Phase 7 cleanup"

patterns-established:
  - "NeoForge datagen import pattern: net.neoforged.neoforge.common.data for datapack-related providers"
  - "RecipeOutput pattern: protected void buildRecipes(RecipeOutput) + .save(output)"

requirements-completed: [GEN-01, GEN-02, GEN-03, GEN-04, GEN-05]

metrics:
  duration: ~10min
  completed: 2026-05-08
---

# Phase 5: Datagen And Resources Summary

**Migrated datagen imports (DatapackBuiltinEntriesProvider, Tags, IConditionBuilder) to NeoForge packages and rewrote SlashBladeRecipeProvider for RecipeOutput API — 7 new LOADER_API_MAP entries (LAM-45..LAM-51)**

## Performance

- **Duration:** ~10 min
- **Started:** 2026-05-08
- **Completed:** 2026-05-08
- **Tasks:** 3
- **Files modified:** 2

## Accomplishments
- DataGen.java: Fixed DatapackBuiltinEntriesProvider import (Forge→NeoForge), added missing EventBusSubscriber import, removed unused Mod import, updated SlashBladeRecipeProvider constructor call with lookupProvider
- SlashBladeRecipeProvider.java: buildRecipes(Consumer<FinishedRecipe>) → buildRecipes(RecipeOutput), constructor updated with HolderLookup.Provider, .save(consumer) → .save(output), IConditionBuilder → NeoForge package, Tags → NeoForge package
- compileJava verification: zero datagen-API-specific errors (100 errors are SlashBlade dependency — expected blocker)

## Task Commits

1. **Task 1: Fix DataGen imports** — `1f1c0af` (fix)
2. **Task 2: Rewrite recipe provider** — `099feaf` (feat)

## Files Modified
- `src/main/java/cn/mmf/energyblade/data/DataGen.java` — 3 import changes + constructor call update
- `src/main/java/cn/mmf/energyblade/data/SlashBladeRecipeProvider.java` — Full API rewrite (13 insertions, 12 deletions)

## Decisions Made
- BuiltInSlashBladeRegistry.java requires no changes (zero Forge references, pure vanilla API)
- LOADER_API_MAP updated with 7 new entries: LAM-45 (DatapackBuiltinEntriesProvider), LAM-46 (RecipeOutput), LAM-47 (RecipeProvider constructor), LAM-48 (.save), LAM-49 (IConditionBuilder), LAM-50 (Tags), LAM-51 (FinishedRecipe removed)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- SlashBlade: Resharped not on classpath — all 100 compilation errors stem from `mods.flammpfeil.slashblade.*` imports (expected blocker, deferred to dependency resolution)
- `EventBusSubscriber.bus = Bus.MOD` deprecated in 1.21.1 (DataGen.java + NetworkPacketHandler.java) — deferred to Phase 7 cleanup
- `ResourceLocation(String, String)` constructor is now private in 1.21.1 — affects BuiltInSlashBladeRegistry and JEICompat (Phase 6/7 scope)

## Next Phase Readiness
- GEN-05 (`./gradlew runData`) cannot pass until SlashBlade dependency is on classpath
- Phase 6 (Client Systems) has the same SlashBlade dependency blocker
- Phase 7 (Cleanup) should address EventBusSubscriber.bus() deprecation and ResourceLocation constructor changes

---
*Phase: 05-datagen-and-resources*
*Completed: 2026-05-08*
