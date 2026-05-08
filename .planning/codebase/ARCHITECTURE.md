# ARCHITECTURE.md

## High-Level Pattern

**Extension pattern** — The mod extends the SlashBlade mod's item system rather than building standalone content. `ItemFEBlade` extends `ItemSlashBlade`, adding Forge Energy (FE) capability to an existing weapon type.

No service layers, no DI, no MVC — standard Minecraft Forge mod architecture.

## Layers

```
┌─────────────────────────────────────────────┐
│  Client Layer                                │
│  client/ClientSetupHandler.java              │
│  client/InputHandler.java                    │
│  client/render/EnergyBladeBEWLR.java         │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────┴──────────────────────────┐
│  Core Layer                                  │
│  Energyblade.java          (entry + registry)│
│  item/ItemFEBlade.java     (item behavior)   │
│  NetworkPacketHandler.java (networking)      │
│  PowerSwitchPacket.java    (custom packet)   │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────┴──────────────────────────┐
│  Energy Capability Layer                     │
│  energy/FEBladeStorage.java       (storage)  │
│  energy/FECapabilityProvider.java (provider) │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────┴──────────────────────────┐
│  Data Generation Layer                       │
│  data/DataGen.java                       (entry)      │
│  data/BuiltInSlashBladeRegistry.java  (registry def) │
│  data/SlashBladeRecipeProvider.java  (recipe gen)   │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  Compatibility Layer                         │
│  compat/JEICompat.java          (JEI plugin) │
└─────────────────────────────────────────────┘
```

## Entry Points

### Mod Entry
- `cn.mmf.energyblade.Energyblade` — annotated `@Mod("energyblade")` (`src/main/java/cn/mmf/energyblade/Energyblade.java:16`)
- Constructor registers `DeferredRegister<Item>` on the mod event bus
- Registers `FMLCommonSetupEvent` listener for network setup

### Client Entry
- `cn.mmf.energyblade.client.ClientSetupHandler` — `@Mod.EventBusSubscriber(value = Dist.CLIENT)` (`src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java:18`)
- Sets up item model properties, key mappings, and baked model modifications

### Datagen Entry
- `cn.mmf.energyblade.data.DataGen` — `@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)` (`src/main/java/cn/mmf/energyblade/data/DataGen.java:17`)
- Listens to `GatherDataEvent` to add recipe provider and SlashBlade definition registry

### JEI Entry
- `cn.mmf.energyblade.compat.JEICompat` — `@JeiPlugin` (`src/main/java/cn/mmf/energyblade/compat/JEICompat.java:10`)

## Data Flow

### Energy Flow (Blade Power-up)
```
User presses V (key binding)
  → InputHandler.onPlayerPostTick() sends PowerSwitchPacket to server
    → PowerSwitchPacket.handle() on server:
      1. Check blade capability present
      2. Extract powerupExtract amount from FEBladeStorage
      3. Set isPowered = true
      4. Play particle effects + sound
    → ItemFEBlade.onSlashBladeUpdate() (fired each tick while held):
      1. If isPowered, extract standbyExtract per tick
      2. If energy available, add max concentration rank points
      3. If energy insufficient, set isPowered = false
```

### Energy Capability Resolution
```
ItemStack.getCapability(ForgeCapabilities.ENERGY)
  → FECapabilityProvider.getCapability()
    → Returns LazyOptional wrapping FEBladeStorage instance
```

### NBT Synchronization
```
Server → Client (via getShareTag/readShareTag):
  getShareTag(): Serializes bladeState + Energy NBT into CompoundTag
  readShareTag(): Deserializes bladeState + Energy NBT on client
```

### Recipe Generation (Datagen)
```
GatherDataEvent
  → SlashBladeRecipeProvider.buildRecipes() — generates shaped SlashBlade recipe
  → DatapackBuiltinEntriesProvider — registers BuiltInSlashBladeRegistry definitions
    → BuiltInSlashBladeRegistry.registerAll() — defines HF_BLADE with model, texture, stats
```

## Key Abstractions

| Abstraction | Role | File |
|---|---|---|
| `IEnergyStorage` | Forge energy storage contract | Implemented by `FEBladeStorage` |
| `ICapabilityProvider` | Capability attachment factory | Implemented by `FECapabilityProvider` |
| `INBTSerializable<CompoundTag>` | NBT persistence contract | Implemented by `FEBladeStorage` and `FECapabilityProvider` |
| `ItemSlashBlade.BLADESTATE` | Blade state capability key | Used throughout for blade identity |
| `SlashBladeDefinition` | Datapack-registered blade definition | Used in `BuiltInSlashBladeRegistry` |

## Event Subscriptions

| Subscriber Class | Event | Purpose |
|---|---|---|
| `Energyblade` | `FMLCommonSetupEvent` | Register network messages |
| `ClientSetupHandler` | `FMLClientSetupEvent` | Register item properties |
| `ClientSetupHandler` | `RegisterKeyMappingsEvent` | Register V key binding |
| `ClientSetupHandler` | `ModelEvent.ModifyBakingResult` | Bake custom blade models |
| `InputHandler` | `InputEvent.Key` | Detect V key press for power toggle |
| `ItemFEBlade` | `SlashBladeEvent.UpdateEvent` | Drain energy on blade tick when powered |
| `ItemFEBlade` | `SlashBladeEvent.HitEvent` | Drain energy on blade hit when powered |
| `ItemFEBlade` | `SlashBladeEvent.PowerBladeEvent` | Signal power state to SlashBlade |
| `DataGen` | `GatherDataEvent` | Generate recipes and registry entries |
