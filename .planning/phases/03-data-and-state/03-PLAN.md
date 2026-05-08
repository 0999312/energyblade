---
phase: 3
plan: 03-data-state
type: execute
wave: 1
depends_on: []
files_modified:
  - src/main/java/cn/mmf/energyblade/energy/FEBladeStorage.java (rewrite)
  - src/main/java/cn/mmf/energyblade/Energyblade.java
  - src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java
  - src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java
  - src/main/java/cn/mmf/energyblade/energy/FECapabilityProvider.java (delete)
autonomous: true
requirements: [CAP-01, CAP-02, CAP-03, CAP-04]
---

# Plan 03-Data-State: Migrate Energy Storage & Capabilities to NeoForge

## Objective

Migrate the Forge Energy storage system and capability provider to NeoForge 1.21.1. Replace `IEnergyStorage` (package rename), remove `LazyOptional`/`ICapabilityProvider`/`@AutoRegisterCapability`, introduce `DataComponentType` for persistence, and wire capabilities via `RegisterCapabilitiesEvent`.

## User Story

**As a** developer, **I want to** run `./gradlew compileJava` and see the energy storage, capability registration, and item queries all using NeoForge APIs without LazyOptional/ICapabilityProvider/initCapabilities.

## Context

Phase 3 is the most complex migration — it touches the core energy system and replaces the entire capability provision model. D-01 (keep IEnergyStorage) and D-02 (DataComponent for serialization) are locked. RESEARCH.md provides exact import paths and API syntax.

## Tasks

### Task 1: Create EnergyBladeData record + DataComponentType registration

<task id="01-datacomponent" type="execute" files="src/main/java/cn/mmf/energyblade/Energyblade.java" autonomous="true">

<read_first>
- src/main/java/cn/mmf/energyblade/Energyblade.java
- .planning/phases/03-data-and-state/03-RESEARCH.md § 3
</read_first>

<action>
In `Energyblade.java`:

1. Add imports:
```java
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import io.netty.buffer.ByteBuf;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.bus.api.SubscribeEvent;
```

2. Add the EnergyBladeData record INSIDE the Energyblade class (as a nested type):
```java
public record EnergyBladeData(int energy, int capacity, int maxReceive, int maxExtract,
        int powerupExtract, int standbyExtract, boolean energyDurability, boolean isPowered) {
    public static final EnergyBladeData DEFAULT = new EnergyBladeData(0, 2000000, 20000, 20000, 1000, 100, false, false);

    public static final Codec<EnergyBladeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("energy").forGetter(EnergyBladeData::energy),
            Codec.INT.fieldOf("capacity").forGetter(EnergyBladeData::capacity),
            Codec.INT.fieldOf("maxReceive").forGetter(EnergyBladeData::maxReceive),
            Codec.INT.fieldOf("maxExtract").forGetter(EnergyBladeData::maxExtract),
            Codec.INT.fieldOf("powerupExtract").forGetter(EnergyBladeData::powerupExtract),
            Codec.INT.fieldOf("standbyExtract").forGetter(EnergyBladeData::standbyExtract),
            Codec.BOOL.fieldOf("energyDurability").forGetter(EnergyBladeData::energyDurability),
            Codec.BOOL.fieldOf("isPowered").forGetter(EnergyBladeData::isPowered)
    ).apply(instance, EnergyBladeData::new));

    public static final StreamCodec<ByteBuf, EnergyBladeData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, EnergyBladeData::energy,
            ByteBufCodecs.INT, EnergyBladeData::capacity,
            ByteBufCodecs.INT, EnergyBladeData::maxReceive,
            ByteBufCodecs.INT, EnergyBladeData::maxExtract,
            ByteBufCodecs.INT, EnergyBladeData::powerupExtract,
            ByteBufCodecs.INT, EnergyBladeData::standbyExtract,
            ByteBufCodecs.BOOL, EnergyBladeData::energyDurability,
            ByteBufCodecs.BOOL, EnergyBladeData::isPowered,
            EnergyBladeData::new
    );
}
```

3. Add DataComponentType registration:
```java
public static final DeferredRegister.DataComponents DATA_COMPONENTS =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

public static final Supplier<DataComponentType<EnergyBladeData>> ENERGY_BLADE_DATA =
        DATA_COMPONENTS.registerComponentType("energy_blade_data",
                builder -> builder.persistent(EnergyBladeData.CODEC)
                        .networkSynchronized(EnergyBladeData.STREAM_CODEC));
```

4. Register DATA_COMPONENTS and add RegisterCapabilitiesEvent listener in constructor:
```java
public Energyblade(IEventBus modBus) {
    ITEMS.register(modBus);
    DATA_COMPONENTS.register(modBus);
    modBus.addListener(this::setup);
    modBus.addListener(this::registerCapabilities);
}

private void registerCapabilities(final RegisterCapabilitiesEvent event) {
    event.registerItem(
            Capabilities.EnergyStorage.ITEM,
            (stack, ctx) -> new FEBladeStorage(stack),
            FORGE_ENERGY_BLADE.get()
    );
}
```
</action>

<acceptance_criteria>
- `Energyblade.java` contains `DataComponentType` import
- `Energyblade.java` contains `EnergyBladeData` record with 8 fields
- `Energyblade.java` contains `DATA_COMPONENTS.registerComponentType("energy_blade_data", ...)`
- `Energyblade.java` contains `modBus.addListener(this::registerCapabilities)`
- `Energyblade.java` contains `RegisterCapabilitiesEvent` handler
- `Energyblade.java` contains `Capabilities.EnergyStorage.ITEM`
- `Energyblade.java` contains `DeferredRegister.createDataComponents`
</acceptance_criteria>

</task>

### Task 2: Rewrite FEBladeStorage — ItemStack-backed IEnergyStorage

<task id="02-storage" type="execute" files="src/main/java/cn/mmf/energyblade/energy/FEBladeStorage.java" autonomous="true">

<read_first>
- src/main/java/cn/mmf/energyblade/energy/FEBladeStorage.java
- src/main/java/cn/mmf/energyblade/Energyblade.java (for EnergyBladeData reference)
- .planning/phases/03-data-and-state/03-RESEARCH.md § 4
</read_first>

<action>
Rewrite `FEBladeStorage.java` to be an ItemStack-backed IEnergyStorage:

```java
package cn.mmf.energyblade.energy;

import cn.mmf.energyblade.Energyblade;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.minecraft.world.item.ItemStack;

public class FEBladeStorage implements IEnergyStorage {
    private final ItemStack stack;

    public FEBladeStorage(ItemStack stack) {
        this.stack = stack;
    }

    private Energyblade.EnergyBladeData getData() {
        return stack.getOrDefault(Energyblade.ENERGY_BLADE_DATA.get(), Energyblade.EnergyBladeData.DEFAULT);
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        if (!canReceive()) return 0;
        Energyblade.EnergyBladeData data = getData();
        int energyReceived = Math.min(data.capacity() - data.energy(), Math.min(data.maxReceive(), toReceive));
        if (energyReceived > 0 && !simulate) {
            int newEnergy = data.energy() + energyReceived;
            stack.set(Energyblade.ENERGY_BLADE_DATA.get(), new Energyblade.EnergyBladeData(
                    newEnergy, data.capacity(), data.maxReceive(), data.maxExtract(),
                    data.powerupExtract(), data.standbyExtract(), data.energyDurability(), data.isPowered()));
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        if (!canExtract()) return 0;
        Energyblade.EnergyBladeData data = getData();
        int energyExtracted = Math.min(data.energy(), Math.min(data.maxExtract(), toExtract));
        if (energyExtracted > 0 && !simulate) {
            int newEnergy = data.energy() - energyExtracted;
            boolean powered = newEnergy > 0 && data.isPowered();
            stack.set(Energyblade.ENERGY_BLADE_DATA.get(), new Energyblade.EnergyBladeData(
                    newEnergy, data.capacity(), data.maxReceive(), data.maxExtract(),
                    data.powerupExtract(), data.standbyExtract(), data.energyDurability(), powered));
        }
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        return getData().energy();
    }

    @Override
    public int getMaxEnergyStored() {
        return getData().capacity();
    }

    @Override
    public boolean canReceive() {
        return getData().maxReceive() > 0;
    }

    @Override
    public boolean canExtract() {
        return getData().maxExtract() > 0;
    }

    // Custom accessors
    private void updateData(Energyblade.EnergyBladeData data) {
        stack.set(Energyblade.ENERGY_BLADE_DATA.get(), data);
    }

    public boolean isPowered() {
        return getData().isPowered();
    }

    public void setPowered(boolean powered) {
        Energyblade.EnergyBladeData data = getData();
        stack.set(Energyblade.ENERGY_BLADE_DATA.get(), new Energyblade.EnergyBladeData(
                data.energy(), data.capacity(), data.maxReceive(), data.maxExtract(),
                data.powerupExtract(), data.standbyExtract(), data.energyDurability(), powered));
    }

    public void setMaxEnergyStored(int capacity) {
        Energyblade.EnergyBladeData data = getData();
        stack.set(Energyblade.ENERGY_BLADE_DATA.get(), new Energyblade.EnergyBladeData(
                data.energy(), capacity, data.maxReceive(), data.maxExtract(),
                data.powerupExtract(), data.standbyExtract(), data.energyDurability(), data.isPowered()));
    }

    public void setPowerupExtract(int powerupExtract) {
        Energyblade.EnergyBladeData data = getData();
        stack.set(Energyblade.ENERGY_BLADE_DATA.get(), new Energyblade.EnergyBladeData(
                data.energy(), data.capacity(), data.maxReceive(), data.maxExtract(),
                powerupExtract, data.standbyExtract(), data.energyDurability(), data.isPowered()));
    }

    public int getPowerupExtract() {
        return getData().powerupExtract();
    }

    public int getStandbyExtract() {
        return getData().standbyExtract();
    }

    public void setStandbyExtract(int standbyExtract) {
        Energyblade.EnergyBladeData data = getData();
        stack.set(Energyblade.ENERGY_BLADE_DATA.get(), new Energyblade.EnergyBladeData(
                data.energy(), data.capacity(), data.maxReceive(), data.maxExtract(),
                data.powerupExtract(), standbyExtract, data.energyDurability(), data.isPowered()));
    }

    public boolean isEnergyDurability() {
        return getData().energyDurability();
    }
}
```

Removed imports: `net.minecraftforge.energy.IEnergyStorage`, `net.minecraftforge.common.util.INBTSerializable`, `net.minecraftforge.common.capabilities.AutoRegisterCapability`, `net.minecraft.nbt.CompoundTag`. Removed: `@AutoRegisterCapability` annotation, `serializeNBT`/`deserializeNBT` methods, all mutable fields (energy, capacity, etc.) — now backed by ItemStack DataComponent.
</action>

<acceptance_criteria>
- `FEBladeStorage.java` does NOT contain `net.minecraftforge` imports
- `FEBladeStorage.java` contains `net.neoforged.neoforge.energy.IEnergyStorage`
- `FEBladeStorage.java` contains `ItemStack stack` field
- `FEBladeStorage.java` contains `stack.getOrDefault(Energyblade.ENERGY_BLADE_DATA.get(), ...)`
- `FEBladeStorage.java` contains `stack.set(Energyblade.ENERGY_BLADE_DATA.get(), ...)`
- `FEBladeStorage.java` does NOT contain `INBTSerializable` or `serializeNBT` or `deserializeNBT`
- `FEBladeStorage.java` does NOT contain `@AutoRegisterCapability`
- `FEBladeStorage.java` does NOT contain `CompoundTag import`
</acceptance_criteria>

</task>

### Task 3: Delete FECapabilityProvider (obsolete pattern)

<task id="03-delete-provider" type="execute" files="src/main/java/cn/mmf/energyblade/energy/FECapabilityProvider.java" autonomous="true">

<read_first>
- src/main/java/cn/mmf/energyblade/energy/FECapabilityProvider.java
</read_first>

<action>
Delete `src/main/java/cn/mmf/energyblade/energy/FECapabilityProvider.java`. The bridge class pattern is obsolete — NeoForge capabilities are registered individually via `RegisterCapabilitiesEvent` (done in Task 1). SlashBlade's BLADESTATE capability is registered by SlashBlade's own NeoForge version.
</action>

<acceptance_criteria>
- `src/main/java/cn/mmf/energyblade/energy/FECapabilityProvider.java` does NOT exist
</acceptance_criteria>

</task>

### Task 4: Update ItemFEBlade — Capability registration + energy queries

<task id="04-item" type="execute" files="src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java" autonomous="true">

<read_first>
- src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java
- .planning/phases/03-data-and-state/03-RESEARCH.md § 6
</read_first>

<action>
In `ItemFEBlade.java`:

1. **Remove imports:**
```java
// DELETE these imports:
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
```

2. **Add imports:**
```java
import net.neoforged.neoforge.capabilities.Capabilities;
```

3. **Remove `initCapabilities()` method** (entire method body). Capability provision is handled by `RegisterCapabilitiesEvent` in Energyblade.java (Task 1).

4. **Fix energy capability queries** — replace all `ForgeCapabilities.ENERGY` patterns:

Old pattern:
```java
stack.getCapability(ForgeCapabilities.ENERGY).filter(FEBladeStorage.class::isInstance)
        .map(FEBladeStorage.class::cast).ifPresent(storage -> { ... });
```
New pattern:
```java
if (stack.getCapability(Capabilities.EnergyStorage.ITEM) instanceof FEBladeStorage storage) {
    // use storage directly
}
```

5. **Fix any remaining `LazyOptional` references** — if there are `LazyOptional.of()`, `lazyOptional.cast()`, etc. — remove them.
</action>

<acceptance_criteria>
- `ItemFEBlade.java` does NOT contain `ForgeCapabilities`
- `ItemFEBlade.java` does NOT contain `ICapabilityProvider`
- `ItemFEBlade.java` does NOT contain `LazyOptional`
- `ItemFEBlade.java` does NOT contain `initCapabilities`
- `ItemFEBlade.java` contains `Capabilities.EnergyStorage.ITEM`
- `ItemFEBlade.java` contains `instanceof FEBladeStorage storage`
- No `stack.getCapability(ForgeCapabilities.ENERGY)` remaining
</acceptance_criteria>

</task>

### Task 5: Update EnergyBladeBEWLR — Fix energy capability query

<task id="05-bewlr" type="execute" files="src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java" autonomous="true">

<read_first>
- src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java
</read_first>

<action>
In `EnergyBladeBEWLR.java`:
1. Replace `import net.minecraftforge.common.capabilities.ForgeCapabilities;` → `import net.neoforged.neoforge.capabilities.Capabilities;`
2. Replace `stack.getCapability(ForgeCapabilities.ENERGY)...` → `stack.getCapability(Capabilities.EnergyStorage.ITEM)...`
3. Remove any `LazyOptional` usage (if present) — replace with direct null check + instanceof
</action>

<acceptance_criteria>
- `EnergyBladeBEWLR.java` does NOT contain `ForgeCapabilities`
- `EnergyBladeBEWLR.java` contains `Capabilities.EnergyStorage.ITEM`
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

Expected: zero errors in the 5 files modified in this phase. Errors in other files (datagen providers, SlashBlade-referenced classes, etc.) are deferred to Phases 5-7.
</action>

<acceptance_criteria>
- `./gradlew compileJava --no-daemon` executes
- No errors in `FEBladeStorage.java`
- No errors in `Energyblade.java`
- No errors in `ItemFEBlade.java` (capability-related)
- No errors in `EnergyBladeBEWLR.java` (capability query)
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
| 1 | `test ! -f src/main/java/cn/mmf/energyblade/energy/FECapabilityProvider.java` | PASS (file deleted) |
| 2 | `grep -rn "ForgeCapabilities" src/main/java/` | Zero matches |
| 3 | `grep -rn "LazyOptional" src/main/java/` | Zero matches |
| 4 | `grep -rn "ICapabilityProvider" src/main/java/` | Zero matches |
| 5 | `grep -rn "initCapabilities" src/main/java/` | Zero matches |
| 6 | `grep -rn "INBTSerializable" src/main/java/` | Zero matches |
| 7 | `grep -rn "@AutoRegisterCapability" src/main/java/` | Zero matches |
| 8 | `grep -rn "Capabilities.EnergyStorage.ITEM" src/main/java/` | 3+ matches (Energyblade, ItemFEBlade, EnergyBladeBEWLR) |

## Success Criteria

- [ ] CAP-01: Energy storage migrated to NeoForge IEnergyStorage + DataComponent
- [ ] CAP-02: Capability provider replaced by RegisterCapabilitiesEvent
- [ ] CAP-03: Item energy queries use NeoForge capability API (no LazyOptional)
- [ ] CAP-04: Data components handle serialization (no INBTSerializable)

## must_haves

- Zero `ForgeCapabilities` remaining in codebase
- Zero `LazyOptional` remaining in codebase
- Zero `ICapabilityProvider` remaining in codebase
- Zero `INBTSerializable` remaining (replaced by DataComponent)
- `FECapabilityProvider.java` deleted
- `FEBladeStorage` has zero `net.minecraftforge` imports
- Energy capability registered via `RegisterCapabilitiesEvent`

## Notes

- `FECapabilityProvider` extends `NamedBladeStateCapabilityProvider` (SlashBlade) — this is removed. SlashBlade's NeoForge version handles its own capability registration.
- `EnergyBladeData` record is immutable — all mutations write a new record to the ItemStack via `stack.set()`.
- Default energy values (capacity 2000000, maxReceive/maxExtract 20000, etc.) are set in EnergyBladeData.DEFAULT.

## PLANNING COMPLETE
