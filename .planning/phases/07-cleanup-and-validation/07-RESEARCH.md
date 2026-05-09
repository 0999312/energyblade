# Phase 7 Research: Cleanup And Validation

**Date:** 2026-05-09
**Status:** Complete

## Research Findings

### CLN-01: Remove Forge References
**Finding:** Zero `net.minecraftforge.*` or `cpw.mods.*` imports found in the entire codebase.
**Status:** ✓ Already clean — all Forge imports were migrated in Phases 1-6.

### CLN-02: Mixin Config
**Finding:** No `.mixins.json` file found in `src/main/resources/`.
**Status:** ✓ No mixin config to remove — was already cleaned up or never existed.

### CLN-03: Dead Logger
**Finding:** Logger in `Energyblade.java` is alive: `private static final Logger LOGGER = LogUtils.getLogger();` with a getter `getLogger()` at line 84-85. It's imported from `org.slf4j.Logger` (NeoForge standard).
**Status:** ✓ Logger is properly wired.

### CLN-04: End-to-End Verification (`runClient`)
**Finding:** `./gradlew compileJava` fails with 100+ errors — ALL from missing SlashBlade (`mods.flammpfeil.slashblade.*`) and JEI (`mezz.jei.api.*`) dependencies.
**Status:** ◆ Blocked — cannot verify `runClient` without SlashBlade NeoForge 1.21.1 port.

### CLN-05: LOADER_API_MAP.md Finalization
**Finding:** LAM-01 through LAM-57 confirmed. Phase 6 added LAM-52 through LAM-57. No remaining open entries.
**Status:** ✓ Essentially complete — needs final review pass.

### Remaining Code Issues Found

#### 1. `new ResourceLocation(String, String)` — Deprecated Constructor
`ResourceLocation(modid, path)` constructor became `private` in Minecraft 1.21.1. 5 occurrences need migration to `ResourceLocation.fromNamespaceAndPath(modid, path)`:

| File | Line(s) | Fix |
|------|---------|-----|
| `JEICompat.java` | 15 | `new ResourceLocation(MODID, MODID)` → `ResourceLocation.fromNamespaceAndPath(MODID, MODID)` |
| `BuiltInSlashBladeRegistry.java` | 22, 24, 25, 36 | `new ResourceLocation(MODID, path)` → `ResourceLocation.fromNamespaceAndPath(MODID, path)` |

#### 2. `BootstapContext` Typo in BuiltInSlashBladeRegistry.java
Line 12: `import net.minecraft.data.worldgen.BootstapContext;` — should be `BootstrapContext`. This is a pre-existing typo from the original Forge code.

#### 3. `FORGE_ENERGY_BLADE.getId()` — Supplier Pattern
Line 21: `Energyblade.FORGE_ENERGY_BLADE.getId()` — `Supplier<Item>` doesn't have `getId()`. In 1.21.1, item IDs are accessed via `BuiltInRegistries.ITEM.getKey(item)`. Pre-existing issue.

#### 4. TODO Comment
ItemFEBlade.java line 102: `// TODO 电量耐久适配消耗` — energy-as-durability feature. This is a deferred enhancement (ENH-01 in requirements), not a migration issue.
