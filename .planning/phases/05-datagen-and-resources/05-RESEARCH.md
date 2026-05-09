# Research: Phase 5 — Datagen And Resources

**Phase:** 5 — Datagen And Resources
**Researched:** 2026-05-08
**Status:** ## RESEARCH COMPLETE

## Research Questions

1. **RQ-01**: `DatapackBuiltinEntriesProvider` migration (Forge → NeoForge package)
2. **RQ-02**: `RecipeProvider` / `FinishedRecipe` → `RecipeOutput` API changes
3. **RQ-03**: `IConditionBuilder` NeoForge package relocation
4. **RQ-04**: `net.minecraftforge.common.Tags` → NeoForge equivalent
5. **RQ-05**: `GatherDataEvent` API stability in 1.21.1
6. **RQ-06**: SlashBlade dependency availability for 1.21.1

---

## RQ-01: DatapackBuiltinEntriesProvider Migration

**Finding:** Only the import package changes. Constructor signature, API, and usage pattern are identical.

| Element | Forge 1.20.1 | NeoForge 1.21.1 |
|---------|-------------|-----------------|
| Import | `net.minecraftforge.common.data.DatapackBuiltinEntriesProvider` | `net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider` |
| Constructor | `(PackOutput, CompletableFuture<HolderLookup.Provider>, RegistrySetBuilder, Set<String>)` | **Same** |
| `getName()` override | Available | **Same** |
| `RegistrySetBuilder` | `net.minecraft.core.RegistrySetBuilder` (vanilla) | **Same** |

**Evidence:** NeoForge GitHub 1.21.1 branch source + docs.neoforged.net § Registries § Data Generation for Datapack Registries

**Action:** Change one import line in `DataGen.java:12`. No logic changes needed.

---

## RQ-02: RecipeProvider / FinishedRecipe → RecipeOutput

**Finding:** Vanilla Minecraft 1.21 replaced `FinishedRecipe` with `RecipeOutput`. RecipeProvider constructor now requires `HolderLookup.Provider`.

| Element | Forge 1.20.1 | NeoForge 1.21.1 |
|---------|-------------|-----------------|
| `buildRecipes` signature | `public void buildRecipes(Consumer<FinishedRecipe>)` | `protected void buildRecipes(RecipeOutput)` |
| `RecipeProvider` constructor | `(PackOutput)` | `(PackOutput, CompletableFuture<HolderLookup.Provider>)` |
| `.save()` call | `.save(consumer)` | `.save(output)` or `.save(output, "id")` |
| `FinishedRecipe` class | Used as type parameter | **Removed** |

**Evidence:** docs.neoforged.net § Recipes § Data Generation + Built-In Recipe Types

**Action:** Update `SlashBladeRecipeProvider.java`:
- Method signature: `Consumer<FinishedRecipe>` → `RecipeOutput`
- Constructor: add `CompletableFuture<HolderLookup.Provider>` param
- `.save(consumer)` → `.save(output)`
- Remove `import java.util.function.Consumer` and `import net.minecraft.data.recipes.FinishedRecipe`
- Add `import net.minecraft.data.recipes.RecipeOutput`, `import java.util.concurrent.CompletableFuture`, `import net.minecraft.core.HolderLookup`

**Blocker Risk:** `SlashBladeShapedRecipeBuilder` is from the external `mods.flammpfeil.slashblade` dependency. If that dependency's `.save()` method still expects `Consumer<FinishedRecipe>`, this file cannot compile until the SlashBlade dependency is updated to 1.21.1.

---

## RQ-03: IConditionBuilder Migration

**Finding:** Package relocation only. Usage changes slightly with `RecipeOutput#withConditions()`.

| Element | Forge 1.20.1 | NeoForge 1.21.1 |
|---------|-------------|-----------------|
| Import | `net.minecraftforge.common.crafting.conditions.IConditionBuilder` | `net.neoforged.neoforge.common.conditions.IConditionBuilder` |
| Usage | Implement interface for static helpers | Same, plus `RecipeOutput#withConditions()` for conditional output |

**Evidence:** docs.neoforged.net § Data Load Conditions § Datagen

**Action:** Change import. If conditions are actively used, wrap output with `.withConditions()`.

---

## RQ-04: Tags Package Relocation

**Finding:** Package relocation only. Tag constants unchanged.

| Element | Forge 1.20.1 | NeoForge 1.21.1 |
|---------|-------------|-----------------|
| Import | `net.minecraftforge.common.Tags` | `net.neoforged.neoforge.common.Tags` |
| Tag refs | `Tags.Items.STORAGE_BLOCKS_REDSTONE` | Same constant path |

**Evidence:** docs.neoforged.net § Tags

**Action:** Change one import line in `SlashBladeRecipeProvider.java:13`.

---

## RQ-05: GatherDataEvent API Stability

**Finding:** The GatherDataEvent API surface is **identical** between Forge 1.20.1 and NeoForge 1.21.1. Only import packages changed (already done in Phase 1-4).

| API Call | Status |
|----------|--------|
| `event.getGenerator()` | Unchanged |
| `event.getLookupProvider()` | Unchanged |
| `event.includeServer()` | Unchanged |
| `dataGenerator.getPackOutput()` | Unchanged |
| `dataGenerator.addProvider(boolean, DataProvider)` | Unchanged |

**Note:** Later NeoForge versions (1.21.9+) introduced `GatherDataEvent.Client`/`GatherDataEvent.Server` sub-events and `event.createProvider()` helpers. These do NOT apply to 1.21.1.

**Issues found in current DataGen.java:**
1. Line 12: `import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider` — needs NeoForge package (RQ-01)
2. Line 17: `@EventBusSubscriber` used but missing `import net.neoforged.fml.common.EventBusSubscriber`
3. Line 15: `import net.neoforged.fml.common.Mod` — unused, should be removed

**Evidence:** docs.neoforged.net § Resources (1.21.1 docs page)

---

## RQ-06: SlashBlade Dependency

**Finding:** The `SlashBladeShapedRecipeBuilder` used in `SlashBladeRecipeProvider.java` comes from `mods.flammpfeil.slashblade`. If this dependency has not been ported to NeoForge 1.21.1, the `.save()` call will fail because it expects `Consumer<FinishedRecipe>` (1.20.1 API) rather than `RecipeOutput` (1.21.1 API).

**Risk Level:** HIGH — This is a cross-dependency issue. If SlashBlade isn't available for 1.21.1, the recipe provider cannot compile regardless of our code changes.

**Mitigation:** Check if SlashBlade: Resharped has a 1.21.1 NeoForge version. If not, the recipe provider may need to be temporarily disabled or stubbed out.

---

## LOADER_API_MAP — New Entries

| ID | 旧 Forge API / 模式 | 使用意图 | NeoForge 1.21.1 替换方案 | 证据 | 状态 |
|---|---|---|---|---|---|
| LAM-45 | `net.minecraftforge.common.data.DatapackBuiltinEntriesProvider` | 数据生成中注册内建数据包注册表条目 | `net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider` | NeoForge GitHub 1.21.1 + docs.neoforged.net § Registries | Confirmed |
| LAM-46 | `Consumer<FinishedRecipe>` + `buildRecipes(Consumer)` | RecipeProvider 食谱构建参数 | `RecipeOutput` + `protected void buildRecipes(RecipeOutput)` | docs.neoforged.net § Recipes § Data Generation | Confirmed |
| LAM-47 | `RecipeProvider(PackOutput)` | RecipeProvider 单参构造 | `RecipeProvider(PackOutput, CompletableFuture<HolderLookup.Provider>)` | docs.neoforged.net § Recipes § Data Generation | Confirmed |
| LAM-48 | `.save(Consumer<FinishedRecipe>)` | 保存食谱构建结果 | `.save(RecipeOutput)` 或 `.save(RecipeOutput, String)` | docs.neoforged.net § Built-In Recipe Types | Confirmed |
| LAM-49 | `net.minecraftforge.common.crafting.conditions.IConditionBuilder` | 食谱条件构建器 | `net.neoforged.neoforge.common.conditions.IConditionBuilder` + `RecipeOutput#withConditions()` | docs.neoforged.net § Data Load Conditions | Confirmed |
| LAM-50 | `net.minecraftforge.common.Tags` | Forge 公共标签常量 | `net.neoforged.neoforge.common.Tags` | docs.neoforged.net § Tags | Confirmed |
| LAM-51 | `net.minecraft.data.recipes.FinishedRecipe` | 已完成的食谱表示 | **已移除** — 由 `RecipeOutput` 内部处理 | docs.neoforged.net § Recipes | Confirmed |

---

## Affected Files Summary

| File | Changes | Risk |
|------|---------|------|
| `DataGen.java` | 1 import fix (DatapackBuiltinEntriesProvider), add EventBusSubscriber import, remove unused Mod import | Low |
| `SlashBladeRecipeProvider.java` | 5+ changes: method sig, constructor, `.save()`, imports for IConditionBuilder/Tags/FinishedRecipe | **HIGH — SlashBlade dependency** |
| `BuiltInSlashBladeRegistry.java` | No changes needed (no Forge references) | None |

---

## Validation Architecture

- **Compile check:** `./gradlew compileJava` — verifies all import and API changes
- **Datagen run:** `./gradlew runData` — verifies generated output matches expected format
- **SlashBlade blocker:** If dependency unavailable, recipe compilation will fail — verify separately

---

*Research: 2026-05-08 via loader-diff-research agents + official NeoForge docs*
