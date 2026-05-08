# CONCERNS.md

## Migration Status (Primary Concern)

The entire codebase is in **pre-migration baseline state** for a 1.20.1 Forge → 1.21.1 NeoForge migration.

- **MIGRATION_PLAN.md** defines 7 phases (Phase 0–7), but none are started
- **PROGRESS.md** shows all fields empty — no phase active, no blockers identified, no validation done
- **LOADER_API_MAP.md** has zero confirmed API mappings (both `Confirmed` and `Open` tables are empty)
- **PREREQUISITES.md** has all checklist items blank — environment not yet verified

**Impact**: Every Forge API call (capability system, networking, registration, event bus, datagen, client extensions) will need to be identified and mapped to NeoForge 1.21.1 equivalents before any code changes begin.

## Specific Migration Risks by Component

### 1. Capability System (HIGH RISK)
NeoForge 1.21.1 has a significantly different capability system from Forge 1.20.1. Affected files:
- `src/main/java/cn/mmf/energyblade/energy/FEBladeStorage.java` — Uses `IEnergyStorage`, `INBTSerializable`, `@AutoRegisterCapability`
- `src/main/java/cn/mmf/energyblade/energy/FECapabilityProvider.java` — Uses `ICapabilityProvider`, `LazyOptional`
- `src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` — Uses `ForgeCapabilities.ENERGY`, `getCapability()`, `LazyOptional`

### 2. Networking (HIGH RISK)
Forge's `SimpleChannel` and `NetworkRegistry` have changed in NeoForge. Affected files:
- `src/main/java/cn/mmf/energyblade/NetworkPacketHandler.java` — `NetworkRegistry.newSimpleChannel()`, `messageBuilder()`, `PacketDistributor`
- `src/main/java/cn/mmf/energyblade/PowerSwitchPacket.java` — `NetworkEvent.Context`, `FriendlyByteBuf` read/write

### 3. Event Bus System (MEDIUM RISK)
NeoForge uses a different event bus structure. Affected:
- `@Mod.EventBusSubscriber` annotations across multiple files
- `FMLJavaModLoadingContext.get().getModEventBus()` in constructor
- `FMLCommonSetupEvent`, `FMLClientSetupEvent` may have NeoForge equivalents

### 4. Registration System (MEDIUM RISK)
`DeferredRegister` API changed. Affected:
- `src/main/java/cn/mmf/energyblade/Energyblade.java` — `DeferredRegister.create(ForgeRegistries.ITEMS, MODID)`, registration on event bus

### 5. Datagen (MEDIUM RISK)
API changes for `GatherDataEvent`, `DatapackBuiltinEntriesProvider`. Affected:
- `src/main/java/cn/mmf/energyblade/data/DataGen.java`
- `src/main/java/cn/mmf/energyblade/data/SlashBladeRecipeProvider.java` — `RecipeProvider`, `FinishedRecipe`, `IConditionBuilder`

### 6. Client Extensions (LOW-MEDIUM RISK)
`IClientItemExtensions`, `@OnlyIn(Dist.CLIENT)`. Affected:
- `src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` — `initializeClient()`
- `src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java`
- `src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java`

### 7. SlashBlade Dependency (BLOCKER RISK)
The parent mod `SlashBlade: Resharped` must also be available for NeoForge 1.21.1. If it has not been ported, this mod cannot be migrated. The mod depends heavily on:
- `ItemSlashBlade` class hierarchy
- SlashBlade capability system (`BLADESTATE`, `NamedBladeStateCapabilityProvider`)
- SlashBlade events (`SlashBladeEvent.UpdateEvent/HitEvent/PowerBladeEvent`)
- SlashBlade render system (`SlashBladeTEISR`, `BladeModelManager`, `BladeRenderState`)
- SlashBlade recipe system (`SlashBladeShapedRecipeBuilder`, `RequestDefinition`)
- SlashBlade datapack registry (`SlashBladeDefinition`, `REGISTRY_KEY`)

## Technical Debt

### Empty Mixin Configuration
`src/main/resources/energyblade.mixins.json` has both `mixins` and `client` arrays empty despite:
- MixinGradle plugin configured in `build.gradle`
- Mixin annotation processor dependency declared
- Mixin run arguments passed to client/server configs
- Mixin refmap file referenced

This is dead configuration that adds build complexity and runtime overhead without providing any functionality.

### Dead Logger
`Energyblade.LOGGER` is declared but never called. No logging exists anywhere in the codebase despite the SLF4J dependency.

### Legacy JAR in libs/
`libs/SlashBladeResharped-1.20.1-1.1.28.jar` is a local JAR while `build.gradle:106` references version `1.3.37` from MMMaven. This local JAR may be stale or unused.

### TODO Comment in Source
`src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java:161`:
```java
// TODO 电量耐久适配消耗
return super.damageItem(arg0, arg1, arg2, arg3);
```
The energy-as-durability feature (`energyDurability` field) is partially implemented — `isDamageable()` returns false when energy durability is active, but `damageItem()` does not divert damage to energy consumption. Items with `energyDurability=true` would be effectively indestructible in the current implementation.

### JEI Compatibility Fragility
`src/main/java/cn/mmf/energyblade/compat/JEICompat.java:21-24` manually calls `deserializeNBT()` on the BLADESTATE capability to sync NBT before reading the translation key. This is a workaround that might break if SlashBlade changes its NBT serialization format.

## Security

- No network input validation beyond `isSpectator()` check — packet handler trusts all received data
- No server-side validation that the player actually holds the item they claim to modify
- No rate limiting on the power toggle packet

## Performance

- No known performance issues — the mod is small and event handlers are lightweight
- Single packet type with minimal data transfer
- Energy extraction per tick is constant-time arithmetic

## Missing Artifacts

- No `src/test/` directory or test infrastructure
- No CI/CD configuration
- No license file (despite `mod_license=MIT License` in `gradle.properties`)
- No `README.md`
- No changelog
