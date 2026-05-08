# Phase 2 Discussion Log

**Date:** 2026-05-08
**Phase:** 2 — Registration And Lifecycle

## Gray Areas Presented

1. **Event subscriber migration** — `@EventBusSubscriber` annotations vs explicit `IEventBus.addListener()`
2. **DeferredRegister placement** — Static fields vs constructor-based
3. **Client lifecycle events** — Package renames vs restructure
4. **Creative tab registration** — Add now or defer

## Discussion

### User Selection
User chose: "Use the recommended approach, and then consider whether there are any other key points to take into account."

### Recommended Approaches (confirmed by user)

| Area | Recommendation | Rationale |
|------|---------------|-----------|
| Event subscriber migration | Keep `@EventBusSubscriber` — package renames only | Minimal diff, NeoForge MDK convention |
| DeferredRegister placement | Keep static fields, register in constructor | Matches Phase 1's Energyblade.java pattern |
| Client lifecycle events | Package renames only — no restructuring | Events exist under same names in `net.neoforged.*` |
| Creative tab | Defer — scope creep | Mod never had one; new capability, not migration parity |

### Additional Points Raised
- Files not touched in Phase 1 (`ClientSetupHandler`, `InputHandler`, `ItemFEBlade`, `DataGen`) still have full Forge imports — Phase 2 must update ALL event-related annotations across codebase
- `@OnlyIn(Dist.CLIENT)` → `net.neoforged.api.distmarker.Dist` / `net.neoforged.api.distmarker.OnlyIn`
- `EventBusSubscriber.Bus.FORGE` → `EventBusSubscriber.Bus.GAME` (not used in this mod)
- `IClientItemExtensions` — import in Phase 2 scope but full migration in Phase 6

### Confirmation
User confirmed: "Proceed with recommendations" — lock all approaches, write CONTEXT.md.

## Decisions Captured

| ID | Decision |
|----|----------|
| D-01 | Keep `@EventBusSubscriber` annotations — package renames only |
| D-02 | `Mod.EventBusSubscriber` → `net.neoforged.fml.common.EventBusSubscriber` |
| D-03 | `SubscribeEvent` → `net.neoforged.bus.api.SubscribeEvent` |
| D-04 | `Bus.MOD` stays unchanged (different import) |
| D-05 | Annotation name drops `Mod.` prefix |
| D-06 | `Dist` → `net.neoforged.api.distmarker.Dist` |
| D-07 | `OnlyIn` → `net.neoforged.api.distmarker.OnlyIn` |
| D-08 | `@OnlyIn(Dist.CLIENT)` — identical annotation, different import |
| D-09 | DeferredRegister: static fields + constructor registration |
| D-10 | `FMLClientSetupEvent` — package rename only |
| D-11 | `RegisterKeyMappingsEvent` → `net.neoforged.client.event.RegisterKeyMappingsEvent` |
| D-12 | `ModelEvent` → `net.neoforged.client.event.ModelEvent` |
| D-13 | `InputEvent` → `net.neoforged.client.event.InputEvent` |
| D-14 | Creative tab — deferred (future enhancement) |

## Deferred Ideas
- Creative tab registration
- `KeyConflictContext`/`KeyModifier` equivalents (Phase 6)
- `IClientItemExtensions` full migration (Phase 6)
