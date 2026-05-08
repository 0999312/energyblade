# Phase 3: Data And State — Research

**Researched:** 2026-05-08
**Status:** Complete

## Key Architecture Insight

NeoForge 1.21.1 has **two independent systems** serving different purposes:

| System | Purpose | Used For |
|--------|---------|----------|
| **Capabilities** | Runtime behavior interfaces (IEnergyStorage, IItemHandler, IFluidHandler) | Blocks, entities, **items** |
| **Data Components** | Persistent serialization (replaces INBTSerializable/CompoundTag) | **Items only** |

**Critical finding:** Capabilities still exist as a standalone system in NeoForge. They are NOT replaced by Data Attachments. Data Attachments are for chunk/entity/blockentity extra data, not items.

## 1. IEnergyStorage — Package Rename Only

| Old (Forge 1.20.1) | New (NeoForge 1.21.1) |
|---|---|
| `net.minecraftforge.energy.IEnergyStorage` | `net.neoforged.neoforge.energy.IEnergyStorage` |

Same 6 methods: `receiveEnergy`, `extractEnergy`, `getEnergyStored`, `getMaxEnergyStored`, `canReceive`, `canExtract`. Base class `EnergyStorage` also exists.

## 2. Capability System — LazyOptional Removed

| Aspect | Forge 1.20.1 | NeoForge 1.21.1 |
|---|---|---|
| Capability key type | `Capability<T>` | `ItemCapability<T, C>` (parameterized with `ResourceLocation`) |
| Lazy resolution | `LazyOptional<T>` | **Removed.** `getCapability()` returns `T` or `null` directly |
| Registration | `@AutoRegisterCapability` or manual | `RegisterCapabilitiesEvent` on mod event bus |
| Item query | `stack.getCapability(CAP, side).ifPresent(...)` | `IEnergyStorage e = stack.getCapability(Capabilities.EnergyStorage.ITEM); if (e != null) { ... }` |
| Item provision | Override `initCapabilities()` on Item subclass | Call `event.registerItem()` in `RegisterCapabilitiesEvent` |

### Capability Registration Syntax

```java
@SubscribeEvent  // on mod event bus
public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    event.registerItem(
        Capabilities.EnergyStorage.ITEM,
        (stack, ctx) -> new FEBladeStorage(stack),
        Energyblade.FORGE_ENERGY_BLADE.get()
    );
}
```

### Energy Capability Key

| Old | New |
|---|---|
| `ForgeCapabilities.ENERGY` | `Capabilities.EnergyStorage.ITEM` (an `ItemCapability<IEnergyStorage, Void>`) |

Import: `net.neoforged.neoforge.capabilities.Capabilities`

## 3. DataComponentType — Replaces INBTSerializable

Register a `DataComponentType<EnergyBladeData>` via `DeferredRegister`:

```java
public static final DeferredRegister.DataComponents REGISTRAR =
    DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, "energyblade");

public record EnergyBladeData(int energy, int capacity, int maxReceive, int maxExtract,
    int powerupExtract, int standbyExtract, boolean energyDurability, boolean isPowered) {}

public static final Supplier<DataComponentType<EnergyBladeData>> ENERGY_BLADE_DATA =
    REGISTRAR.registerComponentType("energy_blade_data",
        builder -> builder.persistent(CODEC).networkSynchronized(STREAM_CODEC));
```

**Read/Write:**
```java
stack.set(ENERGY_BLADE_DATA, new EnergyBladeData(...));
EnergyBladeData data = stack.getOrDefault(ENERGY_BLADE_DATA, DEFAULT);
```

## 4. FEBladeStorage Redesign

FEBladeStorage becomes an IEnergyStorage backed by the ItemStack's DataComponent:

```java
public class FEBladeStorage implements IEnergyStorage {
    private final ItemStack stack;
    // All IEnergyStorage methods read/write via stack.get(ENERGY_BLADE_DATA) / stack.set(...)
}
```

## 5. FECapabilityProvider — Can Be Removed

NeoForge capabilities are registered individually via `RegisterCapabilitiesEvent`. The bridge class pattern (extending NamedBladeStateCapabilityProvider) is no longer needed. SlashBlade's BLADESTATE capability is registered by SlashBlade's own NeoForge version.

## 6. ItemFEBlade Changes

- Remove `initCapabilities()` override
- Add `@SubscribeEvent` handler for `RegisterCapabilitiesEvent`
- Replace all `stack.getCapability(ForgeCapabilities.ENERGY).filter(...).map(...)` with `stack.getCapability(Capabilities.EnergyStorage.ITEM)` + null checks
- `ICapabilityProvider` import → removed

## 7. Complete API Migration Table

| ID | Old Forge API | NeoForge 1.21.1 |
|---|---|---|
| LAM-24 | `net.minecraftforge.energy.IEnergyStorage` | `net.neoforged.neoforge.energy.IEnergyStorage` (same signatures) |
| LAM-25 | `net.minecraftforge.energy.EnergyStorage` | `net.neoforged.neoforge.energy.EnergyStorage` |
| LAM-26 | `ForgeCapabilities.ENERGY` | `Capabilities.EnergyStorage.ITEM` (ItemCapability) |
| LAM-27 | `LazyOptional<T>` | **Removed** — `getCapability()` returns `T` or `null` |
| LAM-28 | `Capability<T>` | `ItemCapability<T, C>` |
| LAM-29 | `@AutoRegisterCapability` | **Removed** — use `RegisterCapabilitiesEvent` |
| LAM-30 | `Item#initCapabilities()` | **Removed** — use `event.registerItem()` |
| LAM-31 | `ICapabilityProvider` | **Removed** — no bridge class needed |
| LAM-32 | `stack.getCapability(CAP, side).ifPresent(...)` | `T v = stack.getCapability(CAP); if (v != null) { ... }` |
| LAM-33 | `INBTSerializable<CompoundTag>` | `DataComponentType<T>` with Codec + StreamCodec |
| LAM-34 | `DeferredRegister` for capabilities | `DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID)` |
| LAM-35 | `net.minecraftforge.common.util.INBTSerializable` | Not used — DataComponent handles serialization |

## 8. Files Modified/Deleted/Created

| File | Action |
|------|--------|
| `energy/FEBladeStorage.java` | Rewrite: IEnergyStorage backed by ItemStack data component |
| `energy/FECapabilityProvider.java` | **Delete** (obsolete pattern) |
| `item/ItemFEBlade.java` | Remove initCapabilities, add RegisterCapabilitiesEvent, fix energy queries |
| `Energyblade.java` | Add DeferredRegister.DataComponents + RegisterCapabilitiesEvent listener |
| `client/render/EnergyBladeBEWLR.java` | Fix `ForgeCapabilities.ENERGY` → `Capabilities.EnergyStorage.ITEM` query |

## 9. Evidence Sources

- NeoForge Capabilities docs: `https://docs.neoforged.net/docs/1.21.1/inventories/capabilities`
- NeoForge Data Components docs: `https://docs.neoforged.net/docs/1.21.1/items/datacomponents`
- GitHub: `neoforged/NeoForge` branch `1.21.1` — `Capabilities.java`, `IEnergyStorage.java`

---

*Phase: 3-Data And State*
*Research completed: 2026-05-08*
