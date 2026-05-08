---
phase: 2
plan: 02-registration-lifecycle
type: execute
wave: 1
depends_on: []
files_modified:
  - src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java
  - src/main/java/cn/mmf/energyblade/client/InputHandler.java
  - src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java
  - src/main/java/cn/mmf/energyblade/data/DataGen.java
autonomous: true
requirements: [REG-01, REG-02, REG-03, REG-04]
---

# Plan 02-Registration-Lifecycle: Migrate Event Subscribers & Lifecycle to NeoForge

## Objective

Migrate all `@EventBusSubscriber`, `@SubscribeEvent`, `@OnlyIn`, and lifecycle event imports across the entire codebase to NeoForge 1.21.1. Replace every legacy Forge import path with its NeoForge equivalent. Update `@EventBusSubscriber` syntax (add explicit `modid`). Verify with `./gradlew compileJava`.

## User Story

**As a** developer, **I want to** run `./gradlew compileJava` and see zero Forge event/annotation import errors, **so that** the mod's event wiring compiles cleanly against NeoForge 1.21.1.

## Context

Phase 2 builds on Phase 1's working build system. All 14 decisions (D-01 through D-14) are locked in CONTEXT.md — package renames only, keep `@EventBusSubscriber` annotations. The RESEARCH.md provides a complete Old→New import table for all 12 Forge imports used in this phase.

## Tasks

### Task 1: Verify Energyblade.java (Phase 1 regression check)

<task id="01-verify-entry" type="verify" files="src/main/java/cn/mmf/energyblade/Energyblade.java" autonomous="true">

<read_first>
- src/main/java/cn/mmf/energyblade/Energyblade.java
</read_first>

<action>
Read Energyblade.java and verify it has zero `net.minecraftforge` imports. Confirm it uses: `@Mod(Energyblade.MODID)`, `IEventBus modBus` constructor, `BuiltInRegistries.ITEM`, `Supplier<Item>`. No changes needed — this is a verification gate.
</action>

<acceptance_criteria>
- `Energyblade.java` contains zero lines matching `net.minecraftforge`
- `Energyblade.java` contains `@Mod(Energyblade.MODID)`
- `Energyblade.java` contains `IEventBus modBus`
- `Energyblade.java` contains `BuiltInRegistries.ITEM`
- `Energyblade.java` contains `java.util.function.Supplier`
- No `RegistryObject` import
</acceptance_criteria>

</task>

### Task 2: Migrate ItemFEBlade.java — Event subscriber + imports

<task id="02-itemblade" type="execute" files="src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java" autonomous="true">

<read_first>
- src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java
- .planning/phases/02-registration-and-lifecycle/02-RESEARCH.md
</read_first>

<action>
In `src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java`, replace these Forge imports with their NeoForge equivalents:

1. `import net.minecraftforge.api.distmarker.Dist;` → `import net.neoforged.api.distmarker.Dist;`
2. `import net.minecraftforge.api.distmarker.OnlyIn;` → `import net.neoforged.api.distmarker.OnlyIn;`
3. `import net.minecraftforge.client.extensions.common.IClientItemExtensions;` → `import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;`
4. `import net.minecraftforge.eventbus.api.SubscribeEvent;` → `import net.neoforged.bus.api.SubscribeEvent;`
5. `import net.minecraftforge.fml.common.Mod.EventBusSubscriber;` → `import net.neoforged.fml.common.EventBusSubscriber;`

Update the `@EventBusSubscriber` annotation (line ~38):
- Old: `@EventBusSubscriber`
- New: `@EventBusSubscriber(modid = Energyblade.MODID)`

Do NOT change any method bodies, SlashBlade event references, or capability code — those are handled in Phases 3-6.
</action>

<acceptance_criteria>
- `ItemFEBlade.java` does NOT contain `net.minecraftforge.api.distmarker`
- `ItemFEBlade.java` contains `import net.neoforged.api.distmarker.Dist`
- `ItemFEBlade.java` contains `import net.neoforged.api.distmarker.OnlyIn`
- `ItemFEBlade.java` contains `import net.neoforged.bus.api.SubscribeEvent`
- `ItemFEBlade.java` contains `import net.neoforged.fml.common.EventBusSubscriber`
- `ItemFEBlade.java` contains `import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions`
- `ItemFEBlade.java` contains `@EventBusSubscriber(modid = Energyblade.MODID)` (with modid parameter)
- `ItemFEBlade.java` does NOT contain `import net.minecraftforge.fml.common.Mod.EventBusSubscriber`
</acceptance_criteria>

</task>

### Task 3: Migrate InputHandler.java — Event subscriber + imports

<task id="03-input" type="execute" files="src/main/java/cn/mmf/energyblade/client/InputHandler.java" autonomous="true">

<read_first>
- src/main/java/cn/mmf/energyblade/client/InputHandler.java
- .planning/phases/02-registration-and-lifecycle/02-RESEARCH.md
</read_first>

<action>
In `src/main/java/cn/mmf/energyblade/client/InputHandler.java`, replace these Forge imports:

1. `import net.minecraftforge.api.distmarker.Dist;` → `import net.neoforged.api.distmarker.Dist;`
2. `import net.minecraftforge.api.distmarker.OnlyIn;` → `import net.neoforged.api.distmarker.OnlyIn;`
3. `import net.minecraftforge.client.event.InputEvent;` → `import net.neoforged.neoforge.client.event.InputEvent;`
4. `import net.minecraftforge.client.settings.KeyConflictContext;` → `import net.neoforged.neoforge.client.settings.KeyConflictContext;`
5. `import net.minecraftforge.client.settings.KeyModifier;` → `import net.neoforged.neoforge.client.settings.KeyModifier;`
6. `import net.minecraftforge.eventbus.api.SubscribeEvent;` → `import net.neoforged.bus.api.SubscribeEvent;`
7. `import net.minecraftforge.fml.common.Mod;` → `import net.neoforged.fml.common.Mod;`

Update the `@Mod.EventBusSubscriber` annotation:
- Old: `@Mod.EventBusSubscriber(value = Dist.CLIENT)`
- New: `@EventBusSubscriber(value = Dist.CLIENT, modid = Energyblade.MODID)`

Update the `@OnlyIn` annotation (line ~22):
- Old: `@OnlyIn(Dist.CLIENT)` with `import net.minecraftforge.api.distmarker.OnlyIn`
- New: same annotation, different import (already updated above)
</action>

<acceptance_criteria>
- `InputHandler.java` does NOT contain `net.minecraftforge.api.distmarker`
- `InputHandler.java` does NOT contain `net.minecraftforge.client.event`
- `InputHandler.java` does NOT contain `net.minecraftforge.client.settings`
- `InputHandler.java` does NOT contain `net.minecraftforge.eventbus.api`
- `InputHandler.java` contains `import net.neoforged.api.distmarker.Dist`
- `InputHandler.java` contains `import net.neoforged.api.distmarker.OnlyIn`
- `InputHandler.java` contains `import net.neoforged.neoforge.client.event.InputEvent`
- `InputHandler.java` contains `import net.neoforged.neoforge.client.settings.KeyConflictContext`
- `InputHandler.java` contains `import net.neoforged.neoforge.client.settings.KeyModifier`
- `InputHandler.java` contains `import net.neoforged.bus.api.SubscribeEvent`
- `InputHandler.java` contains `import net.neoforged.fml.common.Mod`
- `InputHandler.java` contains `@EventBusSubscriber(value = Dist.CLIENT, modid = Energyblade.MODID)`
- `InputHandler.java` does NOT contain `@Mod.EventBusSubscriber`
</acceptance_criteria>

</task>

### Task 4: Migrate ClientSetupHandler.java — Event subscriber + imports + lifecycle events

<task id="04-client" type="execute" files="src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java" autonomous="true">

<read_first>
- src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java
- .planning/phases/02-registration-and-lifecycle/02-RESEARCH.md
</read_first>

<action>
In `src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java`, replace these Forge imports:

1. `import net.minecraftforge.api.distmarker.Dist;` → `import net.neoforged.api.distmarker.Dist;`
2. `import net.minecraftforge.api.distmarker.OnlyIn;` → `import net.neoforged.api.distmarker.OnlyIn;`
3. `import net.minecraftforge.client.event.ModelEvent;` → `import net.neoforged.neoforge.client.event.ModelEvent;`
4. `import net.minecraftforge.client.event.RegisterKeyMappingsEvent;` → `import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;`
5. `import net.minecraftforge.eventbus.api.SubscribeEvent;` → `import net.neoforged.bus.api.SubscribeEvent;`
6. `import net.minecraftforge.fml.common.Mod;` → `import net.neoforged.fml.common.Mod;`
7. `import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;` → `import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;`

Update the `@Mod.EventBusSubscriber` annotation (line ~18):
- Old: `@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)`
- New: `@EventBusSubscriber(value = Dist.CLIENT, modid = Energyblade.MODID, bus = EventBusSubscriber.Bus.MOD)`

Update the `@OnlyIn` annotation (line ~19):
- Old: `@OnlyIn(Dist.CLIENT)` with Forge import
- New: same annotation, different import (already updated)

Do NOT change any method bodies, model references, or `FMLClientSetupEvent` callback logic.
</action>

<acceptance_criteria>
- `ClientSetupHandler.java` does NOT contain `net.minecraftforge.api.distmarker`
- `ClientSetupHandler.java` does NOT contain `net.minecraftforge.client.event`
- `ClientSetupHandler.java` does NOT contain `net.minecraftforge.eventbus.api`
- `ClientSetupHandler.java` does NOT contain `net.minecraftforge.fml.common.Mod`
- `ClientSetupHandler.java` contains `import net.neoforged.api.distmarker.Dist`
- `ClientSetupHandler.java` contains `import net.neoforged.api.distmarker.OnlyIn`
- `ClientSetupHandler.java` contains `import net.neoforged.neoforge.client.event.ModelEvent`
- `ClientSetupHandler.java` contains `import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent`
- `ClientSetupHandler.java` contains `import net.neoforged.bus.api.SubscribeEvent`
- `ClientSetupHandler.java` contains `import net.neoforged.fml.common.Mod`
- `ClientSetupHandler.java` contains `import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent`
- `ClientSetupHandler.java` contains `@EventBusSubscriber(value = Dist.CLIENT, modid = Energyblade.MODID, bus = EventBusSubscriber.Bus.MOD)`
- `ClientSetupHandler.java` does NOT contain `@Mod.EventBusSubscriber`
</acceptance_criteria>

</task>

### Task 5: Migrate DataGen.java — Event subscriber + imports

<task id="05-datagen" type="execute" files="src/main/java/cn/mmf/energyblade/data/DataGen.java" autonomous="true">

<read_first>
- src/main/java/cn/mmf/energyblade/data/DataGen.java
- .planning/phases/02-registration-and-lifecycle/02-RESEARCH.md
</read_first>

<action>
In `src/main/java/cn/mmf/energyblade/data/DataGen.java`, replace these Forge imports:

1. `import net.minecraftforge.data.event.GatherDataEvent;` → `import net.neoforged.neoforge.data.event.GatherDataEvent;`
2. `import net.minecraftforge.eventbus.api.SubscribeEvent;` → `import net.neoforged.bus.api.SubscribeEvent;`
3. `import net.minecraftforge.fml.common.Mod;` → `import net.neoforged.fml.common.Mod;`

Update the `@Mod.EventBusSubscriber` annotation (line ~17):
- Old: `@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)`
- New: `@EventBusSubscriber(modid = Energyblade.MODID, bus = EventBusSubscriber.Bus.MOD)`

Do NOT change any method bodies, datagen provider references, or `DatapackBuiltinEntriesProvider` — those are Phase 5 scope.
</action>

<acceptance_criteria>
- `DataGen.java` does NOT contain `net.minecraftforge.data.event`
- `DataGen.java` does NOT contain `net.minecraftforge.eventbus.api`
- `DataGen.java` does NOT contain `net.minecraftforge.fml.common.Mod`
- `DataGen.java` contains `import net.neoforged.neoforge.data.event.GatherDataEvent`
- `DataGen.java` contains `import net.neoforged.bus.api.SubscribeEvent`
- `DataGen.java` contains `import net.neoforged.fml.common.Mod`
- `DataGen.java` contains `@EventBusSubscriber(modid = Energyblade.MODID, bus = EventBusSubscriber.Bus.MOD)`
- `DataGen.java` does NOT contain `@Mod.EventBusSubscriber`
</acceptance_criteria>

</task>

### Task 6: Verify Compilation

<task id="06-verify" type="verify" files="" autonomous="true">

<read_first>
- build.gradle
</read_first>

<action>
Run the Gradle compilation:
```
./gradlew compileJava --no-daemon
```

Count remaining `net.minecraftforge` compilation errors (not in method bodies — those are deferred-phase errors). The goal is to have ZERO import-resolution errors for the files modified in this phase. Errors in method bodies referencing SlashBlade, capability, or Forge API classes are expected and belong to Phases 3-7.
</action>

<acceptance_criteria>
- `./gradlew compileJava --no-daemon` executes
- No `net.minecraftforge.fml.common.Mod` import errors
- No `net.minecraftforge.eventbus.api.SubscribeEvent` import errors
- No `net.minecraftforge.api.distmarker` import errors
- No `net.minecraftforge.client.event` import errors in the 4 modified files
- Errors in method bodies (Forge API usage like `ForgeCapabilities`, `IEnergyStorage`, `SlashBladeEvent`) are expected and OK
</acceptance_criteria>

</task>

---

## Verification

After all tasks complete:

```bash
./gradlew compileJava --no-daemon
```

Expected: zero import-resolution errors for the 12 Forge imports migrated. Method body errors (Forge capability/energy/event APIs) are deferred to Phases 3-7.

### Spot Checks

| # | Check | Expected |
|---|-------|----------|
| 1 | `grep -rn "@Mod\.EventBusSubscriber" src/main/java/` | Zero matches |
| 2 | `grep -rn "import net.minecraftforge.fml.common.Mod.EventBusSubscriber" src/main/java/` | Zero matches |
| 3 | `grep -rn "import net.minecraftforge.eventbus.api.SubscribeEvent" src/main/java/` | Zero matches |
| 4 | `grep -rn "import net.minecraftforge.api.distmarker" src/main/java/` | Zero matches |
| 5 | `grep -rn "@EventBusSubscriber(modid = Energyblade.MODID)" src/main/java/` | 4 matches (ItemFEBlade, InputHandler, ClientSetupHandler, DataGen) |
| 6 | `grep -rn "import net.neoforged.bus.api.SubscribeEvent" src/main/java/` | 4+ matches |

## Success Criteria

- [ ] REG-01: `DeferredRegister` uses NeoForge registration API (verified in Phase 1, unchanged in Phase 2)
- [ ] REG-02: Event bus references migrated — zero `@Mod.EventBusSubscriber`, zero `Mod.EventBusSubscriber.Bus` references
- [ ] REG-03: Lifecycle events migrated — `FMLClientSetupEvent`, `RegisterKeyMappingsEvent`, `ModelEvent` use NeoForge imports
- [ ] REG-04: `@EventBusSubscriber` annotations updated — all have `modid = Energyblade.MODID` parameter

## must_haves

- Zero `@Mod.EventBusSubscriber` annotations in codebase (all migrated to `@EventBusSubscriber`)
- Every `@EventBusSubscriber` has explicit `modid = Energyblade.MODID`
- Zero `net.minecraftforge.api.distmarker` imports
- Zero `net.minecraftforge.eventbus.api.SubscribeEvent` imports
- `./gradlew compileJava` shows zero import-resolution errors for the 12 migrated paths

## Notes

- Method bodies still contain Forge API references (`ForgeCapabilities.ENERGY`, `IEnergyStorage`, `Capability`, `SlashBladeEvent.*`) — these compile errors are expected and addressed in Phases 3-6
- `IClientItemExtensions` import is updated but the full client extensions migration (RegisterClientExtensionsEvent) is Phase 6
- `GatherDataEvent` import is updated but datagen provider migration is Phase 5
- `KeyConflictContext` changed to `IKeyConflictContext` interface — actual implementation changes belong to Phase 6

## PLANNING COMPLETE
