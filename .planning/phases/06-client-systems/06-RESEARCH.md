# Phase 6 Research: Client Systems

**Date:** 2026-05-09
**Status:** Complete (1 blocked item)

## Research Findings

### 1. FMLClientSetupEvent — Client Setup Event

- **Question:** What replaces `FMLClientSetupEvent` in NeoForge 1.21.1?
- **Old API:** `net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent`
- **New API:** `net.neoforged.fml.event.lifecycle.FMLClientSetupEvent` (same event, different package)
- **Evidence:** NeoForge Events docs (1.21.1) and Models docs (1.21.1)
- **Migration Action:**
  - Import is already correct: `net.neoforged.fml.event.lifecycle.FMLClientSetupEvent`
  - `ItemProperties.register` is not thread-safe — wrap in `event.enqueueWork()`. Current code does NOT do this.

### 2. @EventBusSubscriber — Missing Import in ClientSetupHandler.java

- **Question:** Is `@EventBusSubscriber` annotation correct in NeoForge 1.21.1?
- **Old API:** `net.minecraftforge.fml.common.Mod.EventBusSubscriber` (inner class)
- **New API:** `net.neoforged.fml.common.EventBusSubscriber` (top-level class)
- **Evidence:** LAM-15 confirmed; Events docs (1.21.1)
- **Migration Action:**
  - **CRITICAL:** `ClientSetupHandler.java` imports `net.neoforged.fml.common.Mod` but uses `@EventBusSubscriber`. Fix: replace import with `net.neoforged.fml.common.EventBusSubscriber`.
  - Annotation params `value = Dist.CLIENT, modid = ..., bus = EventBusSubscriber.Bus.MOD` are correct.

### 3. ModelEvent.ModifyBakingResult — Model Baking

- **New API:** `net.neoforged.neoforge.client.event.ModelEvent.ModifyBakingResult`
- **Evidence:** LAM-17 confirmed; BakedModels docs (1.21.1)
- **Migration Action:** Already correct. No change needed.

### 4. RegisterKeyMappingsEvent — Keybinding Registration

- **New API:** `net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent`
- **Evidence:** LAM-18 confirmed; KeyMappings docs (1.21.1)
- **Migration Action:** Already correct. No change needed.

### 5. InputEvent.Key → ClientTickEvent.Post — Key Press Detection

- **Question:** What replaces `InputEvent.Key` for detecting Shift+V toggle?
- **Old API:** `net.neoforged.neoforge.client.event.InputEvent.Key`
- **New API:** `ClientTickEvent.Post` + `KeyMapping#consumeClick()` in a while loop
- **Evidence:** Key Mappings docs (1.21.1) state: "Do not use the InputEvents as an alternative to ClientTickEvent.Post."
- **Migration Action:**
  - Replace `InputEvent.Key` event with `ClientTickEvent.Post`
  - Replace `KEY_CHARGE.isDown()` check with `while (KEY_CHARGE.consumeClick())`
  - `ClientTickEvent.Post` is game bus (default) — `@EventBusSubscriber` with no `bus` param defaults to game bus, which is correct

### 6. KeyConflictContext and KeyModifier

- **New API:** `net.neoforged.neoforge.client.settings.KeyConflictContext` / `KeyModifier`
- **Evidence:** LAM-20, LAM-21 confirmed
- **Migration Action:** Already correct. No change needed.

### 7. EnergyBladeBEWLR — Custom Item Renderer

- **Question:** Is `BlockEntityRenderDispatcher` constructor still valid?
- **New API:** Same pattern, but `EntityModelSet` → `EntityModelLoader` (vanilla change)
- **Evidence:** BER docs (1.21.1); vanilla research (Q3)
- **Migration Action:**
  - Constructor signature: `BlockEntityWithoutLevelRenderer(BlockEntityRenderDispatcher, EntityModelLoader)`
  - `Minecraft.getInstance().getEntityModels()` → `getEntityModelLoader()`

### 8. IClientItemExtensions Registration — RegisterClientExtensionsEvent

- **Question:** How to register custom item renderers in NeoForge 1.21.1?
- **Old API:** `Item#initializeClient(Consumer<IClientItemExtensions>)`
- **New API:** `RegisterClientExtensionsEvent` on the mod event bus
- **Evidence:** LAM-22; BER docs (1.21.1)
- **Migration Action:**
  - Remove `initializeClient` override from `ItemFEBlade.java`
  - Create `IClientItemExtensions` implementation with `EnergyBladeBEWLR` instance
  - Register via `@SubscribeEvent` for `RegisterClientExtensionsEvent` on mod bus

### 9. getShareTag/readShareTag — Item Data Synchronization

- **Question:** Are `getShareTag`/`readShareTag` still needed?
- **Status:** Likely removed in NeoForge 1.21.1 — data components handle sync
- **Migration Action:** Remove the overrides. In NeoForge 1.21.1, the `Item` class no longer has these methods.

### 10. CompoundTag Import

- **New API:** `net.minecraft.nbt.CompoundTag` (unchanged)
- **Migration Action:** Add `import net.minecraft.nbt.CompoundTag;` to `ItemFEBlade.java`

### 11. getCapability() Pattern — LazyOptional Removed

- **Old API:** `getCapability(CAP).ifPresent(...)` / `.isPresent()`
- **New API:** `getCapability(CAP)` returns `T` or `null`
- **Evidence:** LAM-27, LAM-32 confirmed
- **Migration Action:**
  - `InputHandler.java`: `.isPresent()` → `!= null`
  - `ItemFEBlade.java`: `.ifPresent(state -> {...})` → null check
  - `JEICompat.java`: same update

### 12. JEICompat — JEI Plugin Registration

**RESEARCH BLOCKED**

- **Question:** How are JEI plugins registered in NeoForge 1.21.1?
- **Blockers:** JEI is a third-party mod. Loader docs don't cover it.
- **Known:** `@JeiPlugin` annotation and `IModPlugin` interface pattern likely unchanged
- **Recommendation:** Verify JEI NeoForge 1.21.1 API compatibility separately. The `getCapability` LazyOptional patterns also need updating.

## Vanilla Changes (from 06-VANILLA-RESEARCH.md)

| Old API | New API | Breaking? |
|---------|---------|-----------|
| `options.keyShift` | `options.sneakKey` | Renamed |
| `getEntityModels()` → `EntityModelSet` | `getEntityModelLoader()` → `EntityModelLoader` | Breaking |
| `BEWLR(BlockEntityRenderDispatcher)` | `BEWLR(BlockEntityRenderDispatcher, EntityModelLoader)` | Breaking (2 params) |
| `InputConstants.Key.getType()` | `InputConstants.Key.getCategory()` | Renamed |
| `InputConstants.Key.getValue()` | `InputConstants.Key.getCode()` | Renamed |
| `javax.annotation.Nullable` | `javax.annotation.Nullable` | Same |

## Evidence Sources

1. NeoForge Events docs (1.21.1): `@EventBusSubscriber`, `FMLClientSetupEvent`
2. NeoForge Key Mappings docs (1.21.1): `RegisterKeyMappingsEvent`, `ClientTickEvent.Post`, `KeyConflictContext`, `KeyModifier`
3. NeoForge BER docs (1.21.1): `BlockEntityWithoutLevelRenderer`, `RegisterClientExtensionsEvent`, `IClientItemExtensions`
4. NeoForge Models docs (1.21.1): `FMLClientSetupEvent` + `ItemProperties.register`
5. NeoForge Baked Models docs (1.21.1): `ModelEvent.ModifyBakingResult`
6. NeoForge Data Components docs (1.21.1): `DataComponentType`, `networkSynchronized`
7. `docs/migration/LOADER_API_MAP.md` — LAM-01 through LAM-51 confirmed
8. Minecraft 1.21.1 vanilla source (yarn decompiled)

---

*Research completed: 2026-05-09*
