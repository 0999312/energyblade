# Phase 2: Registration And Lifecycle — Research

**Researched:** 2026-05-08
**Status:** Complete

## 1. Complete Import Migration Table

| # | Old Forge 1.20.1 | New NeoForge 1.21.1 |
|---|---|---|
| 1 | `net.minecraftforge.api.distmarker.Dist` | `net.neoforged.api.distmarker.Dist` |
| 2 | `net.minecraftforge.api.distmarker.OnlyIn` | `net.neoforged.api.distmarker.OnlyIn` |
| 3 | `net.minecraftforge.client.event.ModelEvent` | `net.neoforged.neoforge.client.event.ModelEvent` |
| 4 | `net.minecraftforge.client.event.RegisterKeyMappingsEvent` | `net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent` |
| 5 | `net.minecraftforge.eventbus.api.SubscribeEvent` | `net.neoforged.bus.api.SubscribeEvent` |
| 6 | `net.minecraftforge.fml.common.Mod` | `net.neoforged.fml.common.Mod` |
| 7 | `net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent` | `net.neoforged.fml.event.lifecycle.FMLClientSetupEvent` |
| 8 | `net.minecraftforge.client.event.InputEvent` | `net.neoforged.neoforge.client.event.InputEvent` |
| 9 | `net.minecraftforge.client.settings.KeyConflictContext` | `net.neoforged.neoforge.client.settings.KeyConflictContext` |
| 10 | `net.minecraftforge.client.settings.KeyModifier` | `net.neoforged.neoforge.client.settings.KeyModifier` |
| 11 | `net.minecraftforge.client.extensions.common.IClientItemExtensions` | `net.neoforged.neoforge.client.extensions.common.IClientItemExtensions` |
| 12 | `net.minecraftforge.data.event.GatherDataEvent` | `net.neoforged.neoforge.data.event.GatherDataEvent` |

## 2. Critical Annotation Syntax Changes

### `@Mod.EventBusSubscriber` → `@EventBusSubscriber`

**This is the most important change in Phase 2.** `EventBusSubscriber` is NO LONGER an inner class of `Mod`. It's now a top-level class in `net.neoforged.fml.common`.

| Aspect | Forge 1.20.1 | NeoForge 1.21.1 |
|---|---|---|
| Import | `import net.minecraftforge.fml.common.Mod.EventBusSubscriber;` | `import net.neoforged.fml.common.EventBusSubscriber;` |
| Annotation | `@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)` | `@EventBusSubscriber(value = Dist.CLIENT, modid = "energyblade", bus = EventBusSubscriber.Bus.MOD)` |
| Bus enum | `Mod.EventBusSubscriber.Bus.MOD` | `EventBusSubscriber.Bus.MOD` |
| modid | Implicit from `@Mod` class | **NOW REQUIRED** — explicit `modid` parameter |

### Existing patterns and their replacements

| File | Old | New |
|---|---|---|
| ClientSetupHandler | `@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)` | `@EventBusSubscriber(value = Dist.CLIENT, modid = Energyblade.MODID, bus = EventBusSubscriber.Bus.MOD)` |
| InputHandler | `@Mod.EventBusSubscriber(value = Dist.CLIENT)` | `@EventBusSubscriber(value = Dist.CLIENT, modid = Energyblade.MODID)` |
| ItemFEBlade | `@EventBusSubscriber` (import: `Mod.EventBusSubscriber`) | `@EventBusSubscriber(modid = Energyblade.MODID)` |
| DataGen | `@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)` | `@EventBusSubscriber(modid = Energyblade.MODID, bus = EventBusSubscriber.Bus.MOD)` |

## 3. `Dist.CLIENT` — Unchanged

`net.neoforged.api.distmarker.Dist.CLIENT` — same enum value, different import.

## 4. API Signature Changes (Potential Breakage)

### `RegisterKeyMappingsEvent`
- Signature stays: `event.register(keyMapping)` — but verify the `KeyMapping` constructor is compatible with 1.21.1
- NeoForge docs discourage using `InputEvent` for key checking; prefer `ClientTickEvent.Post`

### `KeyConflictContext`
- Now implements `IKeyConflictContext` interface
- Built-in instances: `KeyConflictContext.UNIVERSAL`, `.GUI`, `.IN_GAME` (same names)

### `IClientItemExtensions`
- Registration path changed: now uses `RegisterClientExtensionsEvent` on the mod bus instead of overriding `initializeClient()` directly
- This change belongs to Phase 6 (Client Systems) — Phase 2 only updates the import

### `GatherDataEvent`
- Same usage pattern, but this is Phase 5 scope — Phase 2 only updates the import and `@EventBusSubscriber`

## 5. Files To Modify

| File | Changes |
|---|---|
| `Energyblade.java` | Verify no regressions from Phase 1 migration |
| `ClientSetupHandler.java` | Replace imports #1–7, fix `@EventBusSubscriber` syntax, add `modid` |
| `InputHandler.java` | Replace imports #1–2, #5–6, #8–10, fix `@EventBusSubscriber` syntax, add `modid` |
| `ItemFEBlade.java` | Replace imports #1–2, #5–6, #11, fix `@EventBusSubscriber` syntax, add `modid` |
| `DataGen.java` | Replace imports #5–6, #12, fix `@EventBusSubscriber` syntax, add `modid` |

## 6. Evidence Sources

- [NeoForge Events docs](https://docs.neoforged.net/docs/1.21.1/concepts/events/) — `@EventBusSubscriber`, `@SubscribeEvent`, bus types
- [NeoForge Sides docs](https://docs.neoforged.net/docs/1.21.1/concepts/sides/) — `Dist` / `Dist.CLIENT`
- [NeoForge Key Mappings docs](https://docs.neoforged.net/docs/1.21.1/misc/keymappings/) — KeyConflictContext, KeyModifier, InputEvent deprecation
- [NeoForge Baked Models docs](https://docs.neoforged.net/docs/1.21.1/resources/client/models/bakedmodel/) — ModelEvent
- [NeoForge BER docs](https://docs.neoforged.net/docs/1.21.1/blockentities/ber/) — IClientItemExtensions, RegisterClientExtensionsEvent
- Phase 1 RESEARCH.md § 6 (existing LAM mappings)

## 7. New LOADER_API_MAP Entries

| ID | Old Forge API | NeoForge 1.21.1 Replacement |
|---|---|---|
| LAM-13 | `net.minecraftforge.api.distmarker.Dist` | `net.neoforged.api.distmarker.Dist` |
| LAM-14 | `net.minecraftforge.api.distmarker.OnlyIn` | `net.neoforged.api.distmarker.OnlyIn` |
| LAM-15 | `net.minecraftforge.fml.common.Mod.EventBusSubscriber` (inner class) | `net.neoforged.fml.common.EventBusSubscriber` (top-level class) + mandatory `modid` parameter |
| LAM-16 | `net.minecraftforge.eventbus.api.SubscribeEvent` | `net.neoforged.bus.api.SubscribeEvent` |
| LAM-17 | `net.minecraftforge.client.event.ModelEvent` | `net.neoforged.neoforge.client.event.ModelEvent` |
| LAM-18 | `net.minecraftforge.client.event.RegisterKeyMappingsEvent` | `net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent` |
| LAM-19 | `net.minecraftforge.client.event.InputEvent` | `net.neoforged.neoforge.client.event.InputEvent` |
| LAM-20 | `net.minecraftforge.client.settings.KeyConflictContext` | `net.neoforged.neoforge.client.settings.KeyConflictContext` (now `IKeyConflictContext`) |
| LAM-21 | `net.minecraftforge.client.settings.KeyModifier` | `net.neoforged.neoforge.client.settings.KeyModifier` |
| LAM-22 | `net.minecraftforge.client.extensions.common.IClientItemExtensions` | `net.neoforged.neoforge.client.extensions.common.IClientItemExtensions` |
| LAM-23 | `net.minecraftforge.data.event.GatherDataEvent` | `net.neoforged.neoforge.data.event.GatherDataEvent` |

---

*Phase: 2-Registration And Lifecycle*
*Research completed: 2026-05-08*
