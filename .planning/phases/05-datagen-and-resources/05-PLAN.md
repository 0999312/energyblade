---
phase: 5
plan: 05-datagen-and-resources
type: execute
wave: 1
depends_on: []
files_modified:
  - src/main/java/cn/mmf/energyblade/data/DataGen.java
  - src/main/java/cn/mmf/energyblade/data/SlashBladeRecipeProvider.java
  - src/main/java/cn/mmf/energyblade/data/BuiltInSlashBladeRegistry.java (verify-only, no changes)
autonomous: true
requirements: [GEN-01, GEN-02, GEN-03, GEN-04, GEN-05]
---

# Plan 05-Datagen-Resources: Migrate Datagen To NeoForge API

## Objective

Migrate `DataGen.java` imports (Forge→NeoForge `DatapackBuiltinEntriesProvider`, add missing `EventBusSubscriber` import, remove unused `Mod` import) and rewrite `SlashBladeRecipeProvider.java` for the `RecipeOutput` API (constructor, `buildRecipes` signature, `.save()`, condition/tag package relocations). `BuiltInSlashBladeRegistry.java` requires no changes (pure vanilla API).

## User Story

**As a** developer, **I want to** run `./gradlew compileJava` and see zero datagen-related import errors — `DataGen` compiles with NeoForge imports, and `SlashBladeRecipeProvider` uses the new `RecipeOutput` API.

## Context

Research complete (05-RESEARCH.md). All API mappings documented in `LOADER_API_MAP.md` (LAM-45 through LAM-51). Three tasks: two import/fix tasks on `DataGen.java`, one major rewrite on `SlashBladeRecipeProvider.java`. SlashBlade dependency is a known blocker — `SlashBladeShapedRecipeBuilder.save()` may still target old API.

## Tasks

### Task 1: Fix DataGen.java — Import corrections + constructor call

<task id="01-datagen-imports" type="execute" files="src/main/java/cn/mmf/energyblade/data/DataGen.java" autonomous="true">

<read_first>
- src/main/java/cn/mmf/energyblade/data/DataGen.java
- .planning/phases/05-datagen-and-resources/05-RESEARCH.md
</read_first>

<action>
In `src/main/java/cn/mmf/energyblade/data/DataGen.java`:

1. **Fix DatapackBuiltinEntriesProvider import** (line 12):
   - Old: `import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;`
   - New: `import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;`

2. **Add missing EventBusSubscriber import** (after line 14):
   - Add: `import net.neoforged.fml.common.EventBusSubscriber;`

3. **Remove unused Mod import** (line 15):
   - Delete: `import net.neoforged.fml.common.Mod;`

4. **Update SlashBladeRecipeProvider constructor call** (line 25):
   - Old: `new SlashBladeRecipeProvider(packOutput)`
   - New: `new SlashBladeRecipeProvider(packOutput, lookupProvider)`
   - (Reason: constructor now requires `CompletableFuture<HolderLookup.Provider>` as second param — LAM-47)
</action>

<acceptance_criteria>
- `DataGen.java` does NOT contain `net.minecraftforge.common.data.DatapackBuiltinEntriesProvider`
- `DataGen.java` contains `net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider`
- `DataGen.java` contains `import net.neoforged.fml.common.EventBusSubscriber`
- `DataGen.java` does NOT contain `import net.neoforged.fml.common.Mod`
- `DataGen.java` contains `new SlashBladeRecipeProvider(packOutput, lookupProvider)`
- `DataGen.java` does NOT contain `new SlashBladeRecipeProvider(packOutput)` (without second arg)
</acceptance_criteria>

</task>

### Task 2: Rewrite SlashBladeRecipeProvider.java — RecipeOutput API

<task id="02-recipe-provider" type="execute" files="src/main/java/cn/mmf/energyblade/data/SlashBladeRecipeProvider.java" autonomous="true">

<read_first>
- src/main/java/cn/mmf/energyblade/data/SlashBladeRecipeProvider.java
- .planning/phases/05-datagen-and-resources/05-RESEARCH.md
</read_first>

<action>
Replace the entire content of `src/main/java/cn/mmf/energyblade/data/SlashBladeRecipeProvider.java`:

```java
package cn.mmf.energyblade.data;

import java.util.concurrent.CompletableFuture;

import mods.flammpfeil.slashblade.init.SBItems;
import mods.flammpfeil.slashblade.recipe.RequestDefinition;
import mods.flammpfeil.slashblade.recipe.SlashBladeIngredient;
import mods.flammpfeil.slashblade.recipe.SlashBladeShapedRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

public class SlashBladeRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public SlashBladeRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        SlashBladeShapedRecipeBuilder.shaped(BuiltInSlashBladeRegistry.HF_BLADE.location())
        .pattern("SLJ")
        .pattern("LBL")
        .pattern("JLS")
        .define('B',
                SlashBladeIngredient
                        .of(RequestDefinition.Builder.newInstance().refineCount(10).build()))
        .define('S', Ingredient.of(SBItems.proudsoul_sphere))
        .define('J', Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE))
        .define('L', Ingredient.of(Tags.Items.STORAGE_BLOCKS_IRON))
        .unlockedBy(getHasName(SBItems.slashblade), has(SBItems.slashblade)).save(output);

    }

}
```

Changes:
- Remove `import java.util.function.Consumer;`
- Remove `import net.minecraft.data.recipes.FinishedRecipe;`
- Add `import java.util.concurrent.CompletableFuture;`
- Add `import net.minecraft.core.HolderLookup;`
- Add `import net.minecraft.data.recipes.RecipeOutput;`
- `import net.minecraftforge.common.Tags;` → `import net.neoforged.neoforge.common.Tags;`
- `import net.minecraftforge.common.crafting.conditions.IConditionBuilder;` → `import net.neoforged.neoforge.common.conditions.IConditionBuilder;`
- Constructor: `SlashBladeRecipeProvider(PackOutput)` → `SlashBladeRecipeProvider(PackOutput, CompletableFuture<HolderLookup.Provider>)`
- `super(output)` → `super(output, registries)`
- `public void buildRecipes(Consumer<FinishedRecipe> consumer)` → `protected void buildRecipes(RecipeOutput output)`
- `.save(consumer)` → `.save(output)`
</action>

<acceptance_criteria>
- `SlashBladeRecipeProvider.java` does NOT contain `import java.util.function.Consumer`
- `SlashBladeRecipeProvider.java` does NOT contain `import net.minecraft.data.recipes.FinishedRecipe`
- `SlashBladeRecipeProvider.java` does NOT contain `net.minecraftforge.common.Tags`
- `SlashBladeRecipeProvider.java` does NOT contain `net.minecraftforge.common.crafting.conditions.IConditionBuilder`
- `SlashBladeRecipeProvider.java` contains `import java.util.concurrent.CompletableFuture`
- `SlashBladeRecipeProvider.java` contains `import net.minecraft.core.HolderLookup`
- `SlashBladeRecipeProvider.java` contains `import net.minecraft.data.recipes.RecipeOutput`
- `SlashBladeRecipeProvider.java` contains `import net.neoforged.neoforge.common.Tags`
- `SlashBladeRecipeProvider.java` contains `import net.neoforged.neoforge.common.conditions.IConditionBuilder`
- Constructor signature: `public SlashBladeRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)`
- `buildRecipes` signature: `protected void buildRecipes(RecipeOutput output)`
- Method body uses `.save(output)` not `.save(consumer)`
</acceptance_criteria>

</task>

### Task 3: Verify compilation

<task id="03-verify" type="verify" files="" autonomous="true">

<read_first>
- build.gradle
</read_first>

<action>
Run: `./gradlew compileJava --no-daemon`

Expected: Zero errors related to datagen imports, `DatapackBuiltinEntriesProvider`, `RecipeOutput`, `FinishedRecipe`, `IConditionBuilder`, or `Tags`.

Known acceptable errors: SlashBlade dependency compilation errors (`ItemSlashBlade.BLADESTATE`, `SlashBladeShapedRecipeBuilder.save()`) if SlashBlade: Resharped NeoForge 1.21.1 is not yet on classpath.
</action>

<acceptance_criteria>
- `./gradlew compileJava --no-daemon` executes
- Zero errors containing `DatapackBuiltinEntriesProvider`
- Zero errors containing `FinishedRecipe`
- Zero errors containing `buildRecipes`
- Zero errors containing `IConditionBuilder`
</acceptance_criteria>

</task>

---

## Verification

```bash
./gradlew compileJava --no-daemon
```

### Spot Checks

| # | Check | Expected |
|---|-------|----------|
| 1 | `grep -rn "net.minecraftforge.common.data.DatapackBuiltinEntriesProvider" src/main/java/` | Zero matches |
| 2 | `grep -rn "net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider" src/main/java/cn/mmf/energyblade/data/DataGen.java` | One match |
| 3 | `grep -rn "import net.neoforged.fml.common.EventBusSubscriber" src/main/java/cn/mmf/energyblade/data/DataGen.java` | One match |
| 4 | `grep -rn "import net.neoforged.fml.common.Mod" src/main/java/cn/mmf/energyblade/data/DataGen.java` | Zero matches |
| 5 | `grep -rn "Consumer<FinishedRecipe>" src/main/java/` | Zero matches |
| 6 | `grep -rn "net.minecraftforge.common.Tags" src/main/java/` | Zero matches |
| 7 | `grep -rn "net.minecraftforge.common.crafting.conditions.IConditionBuilder" src/main/java/` | Zero matches |
| 8 | `grep -rn "RecipeOutput" src/main/java/cn/mmf/energyblade/data/SlashBladeRecipeProvider.java` | Found |
| 9 | `grep -rn "CompletableFuture<HolderLookup.Provider>" src/main/java/cn/mmf/energyblade/data/SlashBladeRecipeProvider.java` | Found |

## Success Criteria

- [ ] GEN-01: `GatherDataEvent` uses NeoForge imports; no forge-common-data references
- [ ] GEN-02: `DatapackBuiltinEntriesProvider` import points to `net.neoforged.neoforge.common.data`
- [ ] GEN-03: `SlashBladeRecipeProvider` uses `RecipeOutput` (no `Consumer<FinishedRecipe>`)
- [ ] GEN-04: `IConditionBuilder` import points to `net.neoforged.neoforge.common.conditions`
- [ ] GEN-05: `./gradlew compileJava` succeeds for datagen code (SlashBlade dependency errors acceptable)

## must_haves

- Zero `net.minecraftforge.common.data.DatapackBuiltinEntriesProvider` references
- Zero `Consumer<FinishedRecipe>` references in codebase
- Zero `net.minecraftforge.common.Tags` references in codebase
- Zero `net.minecraftforge.common.crafting.conditions.IConditionBuilder` references in codebase
- `DataGen.java` has correct EventBusSubscriber import, no unused Mod import
- `DataGen.java` constructor call passes `lookupProvider` as second argument

## Notes

- `BuiltInSlashBladeRegistry.java` requires no changes (zero Forge references, pure vanilla API — `RegistrySetBuilder`, `BootstapContext`, `ResourceKey`, `ResourceLocation`)
- **Blocker risk (HIGH):** `SlashBladeShapedRecipeBuilder.save()` is from external `mods.flammpfeil.slashblade`. If SlashBlade: Resharped 1.21.1 is not on classpath, this call will fail to compile. The structural changes are correct regardless of dependency availability.
- `GatherDataEvent` API (lines 20-25) is identical between Forge 1.20.1 and NeoForge 1.21.1 — no changes needed beyond import package fixes.
- `IConditionBuilder` is imported but no condition-wrapping is applied in the recipe (the blade recipe has no mod-loaded conditions). The import is kept for interface conformance and future use.

## PLANNING COMPLETE
