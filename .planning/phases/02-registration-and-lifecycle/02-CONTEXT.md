# Phase 2: Registration And Lifecycle - Context

**Gathered:** 2026-05-08
**Status:** Ready for planning

<domain>
## Phase Boundary

Migrate all item registration, event bus wiring, and lifecycle event listeners to NeoForge 1.21.1 APIs. Update `@EventBusSubscriber`, `@SubscribeEvent`, `DeferredRegister` calls, and client lifecycle events across the entire codebase — package renames only, no structural changes. Files: `Energyblade.java` (already migrated in Phase 1, verify), `ClientSetupHandler.java`, `InputHandler.java`, `ItemFEBlade.java`, `DataGen.java`.

Success criteria: All files with `@EventBusSubscriber` compile without Forge imports; `DeferredRegister` uses NeoForge registries; client lifecycle events fire correctly.
</domain>

<decisions>
## Implementation Decisions

### Event Subscriber Migration
- **D-01:** Keep `@EventBusSubscriber` annotations — package renames only. Do NOT convert to explicit `IEventBus.addListener()` calls. The annotation-based approach is the NeoForge MDK convention and minimizes diff.
- **D-02:** `net.minecraftforge.fml.common.Mod.EventBusSubscriber` → `net.neoforged.fml.common.EventBusSubscriber`
- **D-03:** `net.minecraftforge.eventbus.api.SubscribeEvent` → `net.neoforged.bus.api.SubscribeEvent`
- **D-04:** `Mod.EventBusSubscriber.Bus.MOD` stays `Bus.MOD` in NeoForge (unchanged value, different import)
- **D-05:** `Mod.EventBusSubscriber(value = Dist.CLIENT, bus = ...)` → `@EventBusSubscriber(value = Dist.CLIENT, bus = ...)` (annotation name drops `Mod.` prefix, value unchanged)

### Client Distinction Annotations
- **D-06:** `net.minecraftforge.api.distmarker.Dist` → `net.neoforged.api.distmarker.Dist`
- **D-07:** `net.minecraftforge.api.distmarker.OnlyIn` → `net.neoforged.api.distmarker.OnlyIn`
- **D-08:** `@OnlyIn(Dist.CLIENT)` → identical annotation, different import

### DeferredRegister Placement
- **D-09:** Keep `DeferredRegister` as static fields, register in constructor where a constructor exists. Energyblade.java already uses this pattern (static `ITEMS` field, registered in `Energyblade(IEventBus modBus)` constructor). Other files with `@EventBusSubscriber` (no constructor) keep annotation-based registration.

### Client Lifecycle Events
- **D-10:** `net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent` → `net.neoforged.fml.event.lifecycle.FMLClientSetupEvent` (package rename only, class name identical)
- **D-11:** `net.minecraftforge.client.event.RegisterKeyMappingsEvent` → `net.neoforged.client.event.RegisterKeyMappingsEvent`
- **D-12:** `net.minecraftforge.client.event.ModelEvent` → `net.neoforged.client.event.ModelEvent` (class and inner class `ModifyBakingResult` unchanged)
- **D-13:** `net.minecraftforge.client.event.InputEvent` (in InputHandler.java) → `net.neoforged.client.event.InputEvent`

### Creative Tab
- **D-14:** Defer — the mod never had a creative tab in 1.20.1 (items appear in search only). Adding one is a new capability, not migration parity. If desired later, it's its own phase or enhancement.

### the agent's Discretion
- Exact import organization (order, grouping) — follow existing file conventions
- Whether to add `SuppressWarnings` for deprecated API usage during transition — not needed, just migrate directly
- Whether `IClientItemExtensions` (in ItemFEBlade.java, line 25) should be migrated now or deferred to Phase 6 — the import is in ItemFEBlade.java which is targeted in Phase 2, but the actual implementation migration belongs to Phase 6 (Client Systems). The import update can be done in Phase 2 for compilation, with full migration in Phase 6.
</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition & Scope
- `.planning/ROADMAP.md` § Phase 2 — Scope, success criteria, key migration targets
- `.planning/REQUIREMENTS.md` § REG-01..REG-04 — Registration & lifecycle requirements

### Prior Phase Context
- `.planning/phases/01-build-and-entry/01-CONTEXT.md` — Build system decisions (D-01 through D-10), locked migration rules
- `.planning/phases/01-build-and-entry/01-SUMMARY.md` — What was built in Phase 1, established patterns
- `.planning/phases/01-build-and-entry/01-RESEARCH.md` § 6 — @Mod entry point migration research, import mappings

### Migration Rules & API Maps
- `AGENTS.md` — Migration rules, loader API research process, MCP tool usage
- `docs/migration/LOADER_API_MAP.md` — LAM-01 through LAM-12 (confirmed API mappings from Phase 1)
- `docs/migration/MIGRATION_PLAN.md` § Phase 2 — Migration plan details

### Codebase Maps
- `.planning/codebase/STACK.md` — Key Forge APIs: `@EventBusSubscriber`, `@SubscribeEvent`, `DeferredRegister`
- `.planning/codebase/ARCHITECTURE.md` — Entry points, event subscriptions table, layers
- `.planning/codebase/INTEGRATIONS.md` — SlashBlade dependency, Forge Energy API usage

### Source Files (Phase 2 targets)
- `src/main/java/cn/mmf/energyblade/Energyblade.java` — Verify Phase 1 migration is complete
- `src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java` — 5 event handlers on Forge APIs
- `src/main/java/cn/mmf/energyblade/client/InputHandler.java` — `InputEvent.Key` handler, `@EventBusSubscriber`
- `src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` — `@EventBusSubscriber`, `IClientItemExtensions`, SlashBlade events
- `src/main/java/cn/mmf/energyblade/data/DataGen.java` — `@EventBusSubscriber`, `GatherDataEvent`
</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `Energyblade.java` — Already migrated to `IEventBus` constructor injection, serves as reference pattern for DeferredRegister usage
- `ClientSetupHandler.java` — `@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)` with `@SubscribeEvent` handlers for `FMLClientSetupEvent`, `RegisterKeyMappingsEvent`, `ModelEvent.ModifyBakingResult`
- `InputHandler.java` — `@Mod.EventBusSubscriber(value = Dist.CLIENT)` with `@OnlyIn(Dist.CLIENT)`, `@SubscribeEvent` on `InputEvent.Key`
- `ItemFEBlade.java` — `@EventBusSubscriber` (without `Mod.` prefix — already shortened), multiple `@SubscribeEvent` handlers for SlashBlade events, `@OnlyIn(Dist.CLIENT)` on `initializeClient`
- `DataGen.java` — `@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)` with `@SubscribeEvent` on `GatherDataEvent`

### Established Patterns
- `@EventBusSubscriber` is the primary event wiring pattern across all source files
- Static `DeferredRegister` + constructor registration established in Energyblade.java (Phase 1)
- `@OnlyIn(Dist.CLIENT)` used on classes and methods for client-side-only code
- Client event handlers use `public static` methods (required by `@EventBusSubscriber` annotation)

### Integration Points
- `ItemFEBlade.java` depends on `ItemSlashBlade` (SlashBlade) — extends it, subscribes to `SlashBladeEvent.*`, uses `CapabilityConcentrationRank`
- `ClientSetupHandler.java` depends on `SlashBlade`, `BladeModel` for `bakeBlade()` call
- `InputHandler.java` uses `KeyConflictContext` and `KeyModifier` from Forge client settings (needs NeoForge equivalents)
- `DataGen.java` depends on `GatherDataEvent` + `DatapackBuiltinEntriesProvider` — datagen migration is Phase 5, but the event listener wiring must be correct in Phase 2
</code_context>

<specifics>
## Specific Ideas

- User confirmed: "Use the recommended approach" for all areas — package renames only, keep existing patterns, no structural changes
- User asked to consider additional key points beyond the gray areas — addressed in the "Additional key points" section of the discussion
- `IClientItemExtensions` in ItemFEBlade.java: import can be updated in Phase 2 for compilation purposes, but the full BEWLR/rendering migration is Phase 6 scope
</specifics>

<deferred>
## Deferred Ideas

- **Creative tab** — Mod never had one in 1.20.1. Adding a creative tab (likely via `DeferredRegister` + `CreativeModeTab`) is a new capability, not migration parity. If desired, this is a future enhancement phase.
- **`InputHandler` KeyConflictContext/KeyModifier** — These Forge-specific client settings classes may not have direct NeoForge equivalents. If they're removed in NeoForge, the keybinding code may need restructuring — but this falls under Phase 6 (Client Systems).
</deferred>

---

*Phase: 2-Registration And Lifecycle*
*Context gathered: 2026-05-08*
