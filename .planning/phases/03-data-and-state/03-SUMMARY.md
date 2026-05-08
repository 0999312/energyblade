---
phase: 3
plan: 03-data-state
subsystem: energy-capability
tags: [energy, capability, datacomponent, IEnergyStorage, neoforge, 1.21.1]
key-files:
  created: []
  modified:
    - src/main/java/cn/mmf/energyblade/Energyblade.java
    - src/main/java/cn/mmf/energyblade/energy/FEBladeStorage.java
    - src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java
    - src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java
  deleted:
    - src/main/java/cn/mmf/energyblade/energy/FECapabilityProvider.java
metrics:
  tasks_total: 6
  tasks_completed: 6
  commits: 7
  files_changed: 5 (4 modified, 1 deleted)
---

# Summary: 03-Data-State

**One-liner:** Migrated Forge Energy + Capability system to NeoForge: IEnergyStorage (package rename), DataComponentType for persistence, RegisterCapabilitiesEvent for item capabilities, removed FECapabilityProvider/LazyOptional/INBTSerializable/initCapabilities.

## What Was Built

1. **Energyblade.java** — Added `EnergyBladeData` record (8 fields) with Codec + StreamCodec; registered `DataComponentType<EnergyBladeData>` via `DeferredRegister.DataComponents`; added `RegisterCapabilitiesEvent` listener registering `Capabilities.EnergyStorage.ITEM`
2. **FEBladeStorage.java** — Rewritten as ItemStack-backed IEnergyStorage; reads/writes `EnergyBladeData` via `stack.getOrDefault()`/`stack.set()`; retains all custom accessors (isPowered, setPowered, setMaxEnergyStored, etc.)
3. **FECapabilityProvider.java** — **Deleted.** Obsolete capability bridge pattern.
4. **ItemFEBlade.java** — Removed `initCapabilities()`; replaced all 10 `ForgeCapabilities.ENERGY` references with `Capabilities.EnergyStorage.ITEM` + instanceof pattern; removed energy NBT serialization from getShareTag/readShareTag (DataComponent handles persistence)
5. **EnergyBladeBEWLR.java** — Fixed capability query: `ForgeCapabilities.ENERGY` → `Capabilities.EnergyStorage.ITEM`
6. **compileJava** — Zero Phase 3 errors; all capability/energy/DataComponent code compiles clean

## Verification

- `./gradlew compileJava` — Zero errors in Phase 3 files
- `FECapabilityProvider.java` deleted ✓
- Zero `ForgeCapabilities` in Phase 3 files ✓ (only remaining is in PowerSwitchPacket — Phase 4)
- Zero `LazyOptional` in codebase ✓
- Zero `ICapabilityProvider` in codebase ✓
- Zero `initCapabilities` in codebase ✓
- Zero `INBTSerializable` in codebase ✓
- Zero `@AutoRegisterCapability` in codebase ✓

## Deviations

None.

## Requirements Status

| ID | Requirement | Status |
|----|-------------|--------|
| CAP-01 | Energy storage migrated to NeoForge IEnergyStorage + DataComponent | ✓ |
| CAP-02 | Capability provider replaced by RegisterCapabilitiesEvent | ✓ |
| CAP-03 | Item energy queries use NeoForge capability API (no LazyOptional) | ✓ |
| CAP-04 | Data components handle serialization (no INBTSerializable) | ✓ |

## LOADER_API_MAP Entries

13 new mappings (LAM-24 through LAM-36) recorded in `docs/migration/LOADER_API_MAP.md`.

## Self-Check: PASSED

- [x] All 6 tasks executed with individual commits
- [x] FECapabilityProvider deleted
- [x] Zero ForgeCapabilities in Phase 3 scope
- [x] Zero LazyOptional/ICapabilityProvider/INBTSerializable/AutoRegisterCapability
- [x] EnergyBladeData record + DataComponentType registered
- [x] RegisterCapabilitiesEvent wired for all items
- [x] All energy queries use `Capabilities.EnergyStorage.ITEM` + instanceof
