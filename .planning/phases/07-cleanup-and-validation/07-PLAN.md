# Phase 7 Plan: Cleanup And Validation

**Phase:** 7
**Goal:** Remove all Forge references, fix remaining code issues, verify end-to-end functionality, finalize LOADER_API_MAP.md.
**Mode:** mvp (vertical slices)
**Date:** 2026-05-09

## Frontmatter

| Field | Value |
|-------|-------|
| phase | 7 |
| requirements | CLN-01, CLN-02, CLN-03, CLN-04, CLN-05 |
| files_modified | BuiltInSlashBladeRegistry.java, JEICompat.java, LOADER_API_MAP.md |
| autonomous | true |
| depends_on | Phase 6 (Client Systems) |

## Prerequisite Check

Before executing any task, verify:
- [x] Phase 6 code changes are complete (per 06-SUMMARY.md)
- [x] LOADER_API_MAP.md entries LAM-01 through LAM-57 confirmed
- [ ] SlashBlade NeoForge 1.21.1 port available (required for `./gradlew runClient` — KNOWN BLOCKER)

---

## Wave 1 — Remaining Code Issues Fix (CLN-01, CLN-04 partial)

Three code issues remain that block compilation independently of SlashBlade/JEI dependencies.

### Task 1: Fix BuiltInSlashBladeRegistry.java — ResourceLocation, BootstapContext typo, getId() (CLN-01, CLN-04)

**Requirements:** CLN-01, CLN-04

**Impacted file:** `src/main/java/cn/mmf/energyblade/data/BuiltInSlashBladeRegistry.java`

<read_first>
- src/main/java/cn/mmf/energyblade/data/BuiltInSlashBladeRegistry.java
- src/main/java/cn/mmf/energyblade/Energyblade.java
- .planning/phases/07-cleanup-and-validation/07-RESEARCH.md
</read_first>

<acceptance_criteria>
1. `grep -n "new ResourceLocation(" src/main/java/cn/mmf/energyblade/data/BuiltInSlashBladeRegistry.java` returns zero matches
2. `grep -n "ResourceLocation.fromNamespaceAndPath" src/main/java/cn/mmf/energyblade/data/BuiltInSlashBladeRegistry.java` returns exactly 4 matches
3. `grep -n "BootstapContext" src/main/java/cn/mmf/energyblade/data/BuiltInSlashBladeRegistry.java` returns zero matches
4. `grep -n "BootstrapContext" src/main/java/cn/mmf/energyblade/data/BuiltInSlashBladeRegistry.java` returns exactly 3 matches (import + parameter + full call)
5. `grep -n "FORGE_ENERGY_BLADE\.getId()" src/main/java/cn/mmf/energyblade/data/BuiltInSlashBladeRegistry.java` returns zero matches
6. `grep -n "BuiltInRegistries.ITEM.getKey" src/main/java/cn/mmf/energyblade/data/BuiltInSlashBladeRegistry.java` returns exactly 1 match
</acceptance_criteria>

<action>
Perform these edits in order:

1. **Fix `BootstapContext` typo → `BootstrapContext`** (lines 12, 19):
   - OLD import: `import net.minecraft.data.worldgen.BootstapContext;`
   - NEW import: `import net.minecraft.data.worldgen.BootstrapContext;`
   - OLD parameter: `public static void registerAll(BootstapContext<SlashBladeDefinition> bootstrap) {`
   - NEW parameter: `public static void registerAll(BootstrapContext<SlashBladeDefinition> bootstrap) {`

2. **Replace `new ResourceLocation(modid, path)` with `ResourceLocation.fromNamespaceAndPath(modid, path)`** (lines 22, 24, 25, 36):
   - Line 22: `new ResourceLocation(Energyblade.MODID, "hf_blade")` → `ResourceLocation.fromNamespaceAndPath(Energyblade.MODID, "hf_blade")`
   - Line 24: `new ResourceLocation(Energyblade.MODID, "model/hf_blade.png")` → `ResourceLocation.fromNamespaceAndPath(Energyblade.MODID, "model/hf_blade.png")`
   - Line 25: `new ResourceLocation(Energyblade.MODID, "model/hf_blade.obj")` → `ResourceLocation.fromNamespaceAndPath(Energyblade.MODID, "model/hf_blade.obj")`
   - Line 36: `new ResourceLocation(Energyblade.MODID, id)` → `ResourceLocation.fromNamespaceAndPath(Energyblade.MODID, id)`

3. **Fix `FORGE_ENERGY_BLADE.getId()` → `BuiltInRegistries.ITEM.getKey(FORGE_ENERGY_BLADE.get())`** (line 21):
   - Add import: `import net.minecraft.core.registries.BuiltInRegistries;`
   - OLD: `new SlashBladeDefinition(Energyblade.FORGE_ENERGY_BLADE.getId(),`
   - NEW: `new SlashBladeDefinition(BuiltInRegistries.ITEM.getKey(Energyblade.FORGE_ENERGY_BLADE.get()),`
</action>

---

### Task 2: Fix JEICompat.java — ResourceLocation constructor (CLN-01, CLN-04)

**Requirements:** CLN-01, CLN-04

**Impacted file:** `src/main/java/cn/mmf/energyblade/compat/JEICompat.java`

<read_first>
- src/main/java/cn/mmf/energyblade/compat/JEICompat.java
- .planning/phases/07-cleanup-and-validation/07-RESEARCH.md
</read_first>

<acceptance_criteria>
1. `grep -n "new ResourceLocation(" src/main/java/cn/mmf/energyblade/compat/JEICompat.java` returns zero matches
2. `grep -n "ResourceLocation.fromNamespaceAndPath" src/main/java/cn/mmf/energyblade/compat/JEICompat.java` returns exactly 1 match
</acceptance_criteria>

<action>
Replace `new ResourceLocation(Energyblade.MODID, Energyblade.MODID)` with `ResourceLocation.fromNamespaceAndPath(Energyblade.MODID, Energyblade.MODID)`:

- Line 15 OLD: `return new ResourceLocation(Energyblade.MODID, Energyblade.MODID);`
- Line 15 NEW: `return ResourceLocation.fromNamespaceAndPath(Energyblade.MODID, Energyblade.MODID);`
</action>

---

## Wave 2 — Verification of Already-Clean Items (CLN-01, CLN-02, CLN-03)

CLN-01, CLN-02, CLN-03 were already resolved in previous phases. This wave confirms with grep.

### Task 3: Verify Zero Forge Imports, No Mixin Config, Logger Alive (CLN-01, CLN-02, CLN-03)

**Requirements:** CLN-01, CLN-02, CLN-03

**Impacted files:** none (verification only)

<read_first>
- .planning/phases/07-cleanup-and-validation/07-RESEARCH.md
- src/main/java/cn/mmf/energyblade/Energyblade.java
</read_first>

<acceptance_criteria>
1. `grep -r "net\.minecraftforge\." src/main/java/` returns zero matches (CLN-01)
2. `grep -r "cpw\.mods\." src/main/java/` returns zero matches (CLN-01)
3. `ls src/main/resources/*.mixins.json 2>$null` returns nothing (CLN-02)
4. `grep -n "LogUtils.getLogger()" src/main/java/cn/mmf/energyblade/Energyblade.java | grep -v "//"` returns at least 1 match (CLN-03)
5. `grep -n "getLogger()" src/main/java/cn/mmf/energyblade/Energyblade.java | grep -v "//"` returns exactly 1 match (CLN-03)
</acceptance_criteria>

<action>
Run these verification commands (no file edits needed):

```powershell
# CLN-01: Verify no Forge imports
rg --no-heading "net\.minecraftforge\." src/main/java/ | Measure-Object | Select-Object -ExpandProperty Count
# Expected: 0

rg --no-heading "cpw\.mods\." src/main/java/ | Measure-Object | Select-Object -ExpandProperty Count
# Expected: 0

# CLN-02: Verify no mixin config
Test-Path src/main/resources/energyblade.mixins.json
# Expected: False

Test-Path src/main/resources/*.mixins.json
# Expected: no results

# CLN-03: Verify logger is alive
rg --no-heading "LogUtils.getLogger\(\)" src/main/java/cn/mmf/energyblade/Energyblade.java
# Expected: 1 match (line 28)

rg --no-heading "LOGGER" src/main/java/cn/mmf/energyblade/Energyblade.java
# Expected: 2 matches (declaration line 28, getter reference line 85)
```
</action>

---

## Wave 3 — LOADER_API_MAP.md Finalization (CLN-05)

### Task 4: Finalize LOADER_API_MAP.md (CLN-05)

**Requirements:** CLN-05

**Impacted file:** `docs/migration/LOADER_API_MAP.md`

<read_first>
- docs/migration/LOADER_API_MAP.md
- .planning/phases/07-cleanup-and-validation/07-RESEARCH.md
</read_first>

<acceptance_criteria>
1. `grep -c "^| LAM-" docs/migration/LOADER_API_MAP.md` returns at least 57 (all existing entries preserved)
2. No "TBD" or "???" placeholder in any `Confirmed` entry
3. All entries in `Confirmed` sections have non-empty "证据" column
4. No `Open` entries remain (if any were resolved in Phase 7)
5. Phase 7 code fixes documented: add LAM-58 (ResourceLocation constructor), LAM-59 (Supplier.getId → BuiltInRegistries.ITEM.getKey), LAM-60 (BootstapContext → BootstrapContext) in a new "Confirmed (Phase 7)" section
</acceptance_criteria>

<action>
1. Review all 57 existing LAM entries for consistency — verify each has a non-empty "证据" and "替换方案" column
2. Add a new `## Confirmed (Phase 7)` section at the end of the file with these three entries:

```
| LAM-58 | `new ResourceLocation(String namespace, String path)` | 根据 namespace + path 创建 ResourceLocation | `ResourceLocation.fromNamespaceAndPath(namespace, path)` | 1.21.1 SDK 源码 — 构造函数已改为 private | Confirmed | 1.21.1 禁止直接 new，必须使用工厂方法 |
| LAM-59 | `Supplier<Item>.getId()` 获取物品 ResourceLocation | 通过注册表获取物品 ID | `BuiltInRegistries.ITEM.getKey(item)` | 1.21.1 SDK 源码 — Supplier 接口无 getId() 方法 | Confirmed | 1.21.1 通过 BuiltInRegistries 查询 key |
| LAM-60 | `net.minecraft.data.worldgen.BootstapContext` (typo) | 数据生成注册表引导上下文 | `net.minecraft.data.worldgen.BootstrapContext` (正确拼写) | 1.21.1 SDK 源码 | Confirmed | 原 Forge 代码中的笔误，迁移时修正 |
```
</action>

---

## Wave 4 — Compile Verification And Block Documentation (CLN-04)

### Task 5: Compile Verification And runClient Blocker Documentation (CLN-04)

**Requirements:** CLN-04

**Impacted files:** none (verification only)

<read_first>
- .planning/phases/07-cleanup-and-validation/07-RESEARCH.md
- docs/migration/PROGRESS.md
</read_first>

<acceptance_criteria>
1. `./gradlew compileJava` is attempted
2. If SlashBlade/JEI deps are missing: errors are all `mods.flammpfeil.slashblade.*` or `mezz.jei.api.*` (no errors from energyblade code itself)
3. `docs/migration/PROGRESS.md` is updated with blocker status for CLN-04
4. The runClient blocker is clearly documented as:
   - **Blocker:** SlashBlade NeoForge 1.21.1 port not available
   - **Blocker:** JEI NeoForge 1.21.1 port not available (secondary)
   - **Impact:** runClient cannot execute; compileJava fails 100+ errors all from missing deps
</acceptance_criteria>

<action>
1. Run `./gradlew compileJava` with timeout 300000ms
2. Inspect the output:
   - If zero errors: CLN-04 compile check passes → proceed to `./gradlew runClient`
   - If errors: verify ALL errors originate from `mods.flammpfeil.slashblade.*` or `mezz.jei.api.*` packages ONLY
3. If only dependency errors, mark CLN-04 as "Code Complete — Blocked on Dependencies"
4. Update `docs/migration/PROGRESS.md` with current status:
   - Set TimeBlock: 2026-05-09
   - Set Status: blocked (SlashBlade/JEI NeoForge 1.21.1 port)
   - Record compileJava result
5. If runClient is possible (deps resolved), run `./gradlew runClient` and verify:
   - Mod loads without crash
   - Blade appears in creative tab / inventory
   - Energy tooltip displays correctly
   - Power toggle keybinding works
</action>

---

## Verification (Phase-Level)

After all waves complete, run these checks:

```powershell
# Verify zero Forge imports anywhere in codebase
rg --no-heading "net\.minecraftforge\.|cpw\.mods\." src/main/java/
# Expected: zero matches

# Verify all ResourceLocation constructors are fixed
rg --no-heading "new ResourceLocation\(" src/main/java/
# Expected: zero matches

# Verify LOADER_API_MAP.md has 60 LAM entries
rg -c "^| LAM-" docs/migration/LOADER_API_MAP.md
# Expected: 60

# Verify BuiltInSlashBladeRegistry has no remaining old patterns
rg --no-heading "BootstapContext|FORGE_ENERGY_BLADE\.getId\(\)" src/main/java/cn/mmf/energyblade/data/BuiltInSlashBladeRegistry.java
# Expected: zero matches

# Attempt compile
./gradlew compileJava
```

## must_haves (Goal-Backward Verification)

These conditions MUST be true for the phase to be considered complete:

1. Zero `net.minecraftforge.*` or `cpw.mods.*` imports in entire codebase (CLN-01) — verified by grep
2. No `.mixins.json` file exists in `src/main/resources/` (CLN-02) — verified by file check
3. Logger is `LogUtils.getLogger()` with a public getter (CLN-03) — verified by grep
4. All 5 `new ResourceLocation(String, String)` replaced with `ResourceLocation.fromNamespaceAndPath()` (CLN-01, CLN-04) — verified by grep
5. `BootstapContext` typo fixed to `BootstrapContext` (CLN-01, CLN-04) — verified by grep
6. `FORGE_ENERGY_BLADE.getId()` replaced with `BuiltInRegistries.ITEM.getKey()` (CLN-01, CLN-04) — verified by grep
7. LOADER_API_MAP.md contains LAM-01 through LAM-60 with all fields populated (CLN-05)
8. `./gradlew compileJava` attempted; if deps missing, errors are ONLY from `mods.flammpfeil.slashblade.*` / `mezz.jei.api.*` (CLN-04)
9. runClient blocker documented in `docs/migration/PROGRESS.md` (CLN-04)

## Risk Register

| Risk | Impact | Mitigation |
|------|--------|------------|
| SlashBlade NeoForge 1.21.1 port not available | Blocks CLN-04 `runClient` verification | Document as known blocker; mark phase 7 code as complete except for runtime verification |
| JEI NeoForge 1.21.1 port not available | Secondary blocker for CLN-04 (JEI compat) | JEI is `compileOnly` dependency — compile fails without it; document as co-blocker |
| `ResourceLocation.fromNamespaceAndPath` availability | Confirmed in 1.21.1 — no risk | Static factory method available since 1.21 |
| `BuiltInRegistries.ITEM.getKey()` availability | Confirmed in 1.21.1 — no risk | Standard vanilla API |
