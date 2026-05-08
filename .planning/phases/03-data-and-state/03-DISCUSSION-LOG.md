# Phase 3 Discussion Log

**Date:** 2026-05-08
**Phase:** 3 — Data And State

## Gray Areas Presented

1. **Energy storage API** — Keep IEnergyStorage or extend EnergyStorage base class
2. **Capability → DataAttachment** — Bridge approach for FECapabilityProvider
3. **NBT serialization** — Keep INBTSerializable or switch to DataComponent
4. **Item capability injection** — ICapabilityProvider → DataComponent on items

## Discussion

### Area 1: Energy Storage API

**Question 1:** IEnergyStorage interface vs EnergyStorage base class?
**Answer:** Keep IEnergyStorage (Recommended) — package rename only to `net.neoforged.neoforge.energy.IEnergyStorage`

**Question 2:** Serialization — INBTSerializable vs DataComponent?
**Answer:** Switch to DataComponent — FEBladeStorage fields become a registered DataComponentType

**Completion check:** User chose "Done — write context" after Energy area. Remaining 3 areas left to agent discretion.

## Decisions Captured

| ID | Decision |
|----|----------|
| D-01 | Keep `IEnergyStorage` interface — package rename to `net.neoforged.neoforge.energy.IEnergyStorage` |
| D-02 | Switch to DataComponent for serialization — register DataComponentType for FEBladeStorage fields |

## Agent Discretion Areas
- Capability→DataAttachment bridge for FECapabilityProvider
- Item capability injection migration (ICapabilityProvider → DataComponent)
- `@AutoRegisterCapability` replacement
- `ForgeCapabilities.ENERGY` → NeoForge energy capability key
- NBT save data compatibility during migration

## Deferred Ideas
- EnergyBladeBEWLR rendering → Phase 6
- SlashBlade capability migration → SlashBlade project
- Mekanism integration testing → Phase 7
