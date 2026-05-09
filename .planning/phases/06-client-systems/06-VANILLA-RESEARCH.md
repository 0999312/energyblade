# 06-VANILLA-RESEARCH: 1.21.1 Client-Side API Mapping

**Date:** 2026-05-09
**Source:** Minecraft 1.21.1 yarn decompiled

---

## Q1: `ItemProperties.register()` signature

### 1.20.1 (mojmap)
- **Class:** `net.minecraft.client.renderer.item.ItemProperties`
- **Method:** `public static void register(Item item, ResourceLocation id, ClampedItemPropertyFunction function)`

### 1.21.1 (yarn → inferred mojmap)
- **Yarn class:** `net.minecraft.client.item.ModelPredicateProviderRegistry`
- **Mojmap FQN (inferred):** `net.minecraft.client.renderer.item.ItemProperties`
- **`register` methods are `private static` in 1.21.1 vanilla**
- **NeoForge likely opens these via access transformers** — see R-06 for NeoForge's `ItemProperties.register` event wrapper

### Impact on local code
- `ClientSetupHandler.java` line 25: `ItemProperties.register(...)` — must wrap in `FMLClientSetupEvent#enqueueWork()`. NeoForge docs show this working.

---

## Q2: `Minecraft.getInstance().getBlockEntityRenderDispatcher()` and `getEntityModels()`

### `getBlockEntityRenderDispatcher()`
- **Verdict:** Exists unchanged. Return type `BlockEntityRenderDispatcher`.

### `getEntityModels()` → `getEntityModelLoader()`
- **1.20.1:** `getEntityModels()` → `EntityModelSet`
- **1.21.1:** `getEntityModelLoader()` → `EntityModelLoader`
- **Verdict:** Renamed + return type changed.

---

## Q3: `BlockEntityWithoutLevelRenderer` constructor

### 1.20.1
```java
public BlockEntityWithoutLevelRenderer(BlockEntityRenderDispatcher dispatcher)
```

### 1.21.1
```java
public BlockEntityWithoutLevelRenderer(
    BlockEntityRenderDispatcher blockEntityRenderDispatcher,
    EntityModelLoader entityModelLoader
)
```

**Changes:**
1. Constructor now takes TWO parameters
2. `EntityModelSet` → `EntityModelLoader`

---

## Q4: `options.keyShift` → `options.sneakKey`

- **1.20.1:** `Minecraft.getInstance().options.keyShift`
- **1.21.1:** `Minecraft.getInstance().options.sneakKey`

---

## Q5: `InputConstants.Key` method renames

- `getType()` → `getCategory()`
- `getValue()` → `getCode()`
- Enum values `Type.KEYSYM`, `Type.MOUSE` unchanged

---

## Q6: `CompoundTag` FQN

- **1.21.1:** `net.minecraft.nbt.CompoundTag` — unchanged.

---

## Q7: `@Nullable` annotation

- **1.21.1:** `javax.annotation.Nullable` — unchanged.

---

## Summary: All Identified Vanilla Changes

| # | 1.20.1 API | 1.21.1 API | Breaking? |
|---|---|---|---|
| 2b | `getEntityModels()` → `EntityModelSet` | `getEntityModelLoader()` → `EntityModelLoader` | Breaking |
| 3 | `BEWLR(BlockEntityRenderDispatcher)` | `BEWLR(BlockEntityRenderDispatcher, EntityModelLoader)` | Breaking |
| 4 | `options.keyShift` | `options.sneakKey` | Renamed |
| 5a | `Key.getType()` | `Key.getCategory()` | Renamed |
| 5b | `Key.getValue()` | `Key.getCode()` | Renamed |
| 6 | `net.minecraft.nbt.CompoundTag` | `net.minecraft.nbt.CompoundTag` | Same |
| 7 | `javax.annotation.Nullable` | `javax.annotation.Nullable` | Same |

---

*Research completed: 2026-05-09*
