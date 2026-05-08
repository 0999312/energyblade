---
phase: 2
plan: 02-registration-lifecycle
subsystem: event-bus
tags: [eventbus, subscriber, lifecycle, imports, neoforge, 1.21.1]
key-files:
  modified:
    - src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java
    - src/main/java/cn/mmf/energyblade/client/InputHandler.java
    - src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java
    - src/main/java/cn/mmf/energyblade/data/DataGen.java
metrics:
  tasks_total: 6
  tasks_completed: 6
  commits: 6
  files_changed: 4
  imports_migrated: 22
---

# Summary: 02-Registration-Lifecycle

**One-liner:** Migrated 22 Forge event/lifecycle imports to NeoForge across 4 files; all `@EventBusSubscriber` annotations updated with mandatory `modid` parameter; zero import-resolution errors.

## What Was Built

1. **Energyblade.java** — Verified no regressions from Phase 1 (zero Forge imports, IEventBus injection intact)
2. **ItemFEBlade.java** — 5 imports migrated (Dist, OnlyIn, IClientItemExtensions, SubscribeEvent, EventBusSubscriber); `@EventBusSubscriber` updated to `@EventBusSubscriber(modid = Energyblade.MODID)`
3. **InputHandler.java** — 7 imports migrated (Dist, OnlyIn, InputEvent, KeyConflictContext, KeyModifier, SubscribeEvent, Mod); `@Mod.EventBusSubscriber` → `@EventBusSubscriber(value = Dist.CLIENT, modid = Energyblade.MODID)`; added `Energyblade` import
4. **ClientSetupHandler.java** — 7 imports migrated (Dist, OnlyIn, ModelEvent, RegisterKeyMappingsEvent, SubscribeEvent, Mod, FMLClientSetupEvent); `@Mod.EventBusSubscriber(...bus = Mod.EventBusSubscriber.Bus.MOD)` → `@EventBusSubscriber(...bus = EventBusSubscriber.Bus.MOD, modid = Energyblade.MODID)`
5. **DataGen.java** — 3 imports migrated (GatherDataEvent, SubscribeEvent, Mod); `@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)` → `@EventBusSubscriber(modid = Energyblade.MODID, bus = EventBusSubscriber.Bus.MOD)`
6. **compileJava verification** — Zero import-resolution errors for the 12 migrated Forge import paths

## Verification

- `./gradlew compileJava` — 12 migrated imports resolve cleanly. Errors remain in method bodies (Forge capability API, SlashBlade references) — deferred to Phases 3-7
- Zero `@Mod.EventBusSubscriber` annotations in codebase
- 4 `@EventBusSubscriber(modid = Energyblade.MODID)` annotations
- Zero `net.minecraftforge.api.distmarker` imports
- Zero `net.minecraftforge.eventbus.api.SubscribeEvent` imports

## Deviations

None. All tasks executed as planned.

## Requirements Status

| ID | Requirement | Status |
|----|-------------|--------|
| REG-01 | DeferredRegister uses NeoForge API | ✓ Verified (Phase 1 pattern, unchanged) |
| REG-02 | Event bus references migrated | ✓ Zero `@Mod.EventBusSubscriber`, zero `Mod.EventBusSubscriber.Bus` references |
| REG-03 | Lifecycle events migrated | ✓ FMLClientSetupEvent, RegisterKeyMappingsEvent, ModelEvent on NeoForge imports |
| REG-04 | @EventBusSubscriber annotations updated | ✓ All 4 have `modid = Energyblade.MODID` |

## LOADER_API_MAP Entries

11 new mappings (LAM-13 through LAM-23) recorded in `docs/migration/LOADER_API_MAP.md`.

## Self-Check: PASSED

- [x] All 6 tasks executed with individual commits
- [x] Zero `@Mod.EventBusSubscriber` remaining
- [x] 4 `@EventBusSubscriber` all have explicit `modid`
- [x] 22 imports migrated across 4 files
- [x] No import-resolution errors for migrated paths
- [x] Method body errors deferred to Phases 3-7 (expected)
