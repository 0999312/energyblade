# Requirements: HF Blade Migration (1.20.1 Forge → 1.21.1 NeoForge)

**Defined:** 2026-05-08
**Core Value:** The mod must compile, load, and function identically on NeoForge 1.21.1 — FE-powered SlashBlade with power toggle, energy drain, and custom rendering.

## v1 Requirements

Requirements for the migration. Each maps to a roadmap phase.

### Build & Entry

- [ ] **BLD-01**: Gradle build script upgraded to NeoGradle for NeoForge 1.21.1
- [ ] **BLD-02**: Java toolchain updated to JDK 21
- [ ] **BLD-03**: All dependencies updated to NeoForge 1.21.1 equivalents
- [ ] **BLD-04**: Mod metadata (`mods.toml`) updated for NeoForge format
- [ ] **BLD-05**: Mod entry point (`@Mod` class) migrated to NeoForge bootstrap
- [ ] **BLD-06**: `./gradlew compileJava` succeeds with zero errors

### Registration & Lifecycle

- [ ] **REG-01**: `DeferredRegister` migrated to NeoForge registration API
- [ ] **REG-02**: Event bus references (`FMLJavaModLoadingContext`, `ModEventBus`) migrated
- [ ] **REG-03**: Lifecycle events (`FMLCommonSetupEvent`, `FMLClientSetupEvent`) migrated
- [ ] **REG-04**: `@EventBusSubscriber` annotations updated for NeoForge

### Data & State

- [ ] **DAT-01**: `IEnergyStorage` capability migrated to NeoForge energy system
- [ ] **DAT-02**: Custom capability provider (`FECapabilityProvider`) migrated to attachments
- [ ] **DAT-03**: NBT serialization (`serializeNBT`/`deserializeNBT`) updated if needed
- [ ] **DAT-04**: `getShareTag`/`readShareTag` sync mechanism migrated
- [ ] **DAT-05**: `@AutoRegisterCapability` replaced with NeoForge equivalent

### Networking

- [ ] **NET-01**: `SimpleChannel` (`NetworkRegistry.newSimpleChannel`) migrated to NeoForge payloads
- [ ] **NET-02**: `PowerSwitchPacket` updated to NeoForge custom payload
- [ ] **NET-03**: `PacketDistributor` and `NetworkDirection` migrated
- [ ] **NET-04**: Client→server packet sending path migrated

### Datagen & Resources

- [ ] **GEN-01**: `GatherDataEvent` migrated to NeoForge datagen API
- [ ] **GEN-02**: `DatapackBuiltinEntriesProvider` updated for NeoForge
- [ ] **GEN-03**: `SlashBladeRecipeProvider` / `RecipeProvider` / `FinishedRecipe` migrated
- [ ] **GEN-04**: `IConditionBuilder` migrated to NeoForge conditions
- [ ] **GEN-05**: `./gradlew runData` succeeds producing same output

### Client Systems

- [ ] **CLI-01**: `@OnlyIn(Dist.CLIENT)` annotations migrated to NeoForge client dist
- [ ] **CLI-02**: `IClientItemExtensions` / `initializeClient` migrated
- [ ] **CLI-03**: Custom BEWLR (`EnergyBladeBEWLR`) rendering path migrated
- [ ] **CLI-04**: `ModelEvent.ModifyBakingResult` migrated to NeoForge model events
- [ ] **CLI-05**: Key mapping registration (`RegisterKeyMappingsEvent`) migrated
- [ ] **CLI-06**: `InputEvent.Key` handler migrated
- [ ] **CLI-07**: JEI compat (`JEICompat`) updated for NeoForge JEI API

### Cleanup & Validation

- [ ] **CLN-01**: All `net.minecraftforge.*` imports removed
- [ ] **CLN-02**: Empty Mixin config removed or migrated
- [ ] **CLN-03**: Dead logger removed or wired to actual logging
- [ ] **CLN-04**: `./gradlew runClient` — blade renders, powers up, drains energy correctly
- [ ] **CLN-05**: `LOADER_API_MAP.md` fully populated with all confirmed mappings

## v2 Requirements

Deferred to post-migration.

- **ENH-01**: Energy-as-durability feature (`damageItem` diverts damage to energy)
- **ENH-02**: Unit tests for FEBladeStorage energy math
- **ENH-03**: In-game config for energy capacity/extract rates
- **ENH-04**: Multiple blade variants with different energy profiles

## Out of Scope

| Feature | Reason |
|---------|--------|
| Dual-version compatibility layer | Migration rules explicitly forbid |
| New blade types or mechanics | Port only — preserve 1:1 behavior |
| SlashBlade upstream changes | Dependency must provide its own NeoForge port |
| Forge 1.20.1 maintenance | After migration, 1.20.1 is sunset |
| Performance optimization | Not in scope for migration |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| BLD-01 | Phase 1 | Pending |
| BLD-02 | Phase 1 | Pending |
| BLD-03 | Phase 1 | Pending |
| BLD-04 | Phase 1 | Pending |
| BLD-05 | Phase 1 | Pending |
| BLD-06 | Phase 1 | Pending |
| REG-01 | Phase 2 | Pending |
| REG-02 | Phase 2 | Pending |
| REG-03 | Phase 2 | Pending |
| REG-04 | Phase 2 | Pending |
| DAT-01 | Phase 3 | Pending |
| DAT-02 | Phase 3 | Pending |
| DAT-03 | Phase 3 | Pending |
| DAT-04 | Phase 3 | Pending |
| DAT-05 | Phase 3 | Pending |
| NET-01 | Phase 4 | Pending |
| NET-02 | Phase 4 | Pending |
| NET-03 | Phase 4 | Pending |
| NET-04 | Phase 4 | Pending |
| GEN-01 | Phase 5 | Pending |
| GEN-02 | Phase 5 | Pending |
| GEN-03 | Phase 5 | Pending |
| GEN-04 | Phase 5 | Pending |
| GEN-05 | Phase 5 | Pending |
| CLI-01 | Phase 6 | Pending |
| CLI-02 | Phase 6 | Pending |
| CLI-03 | Phase 6 | Pending |
| CLI-04 | Phase 6 | Pending |
| CLI-05 | Phase 6 | Pending |
| CLI-06 | Phase 6 | Pending |
| CLI-07 | Phase 6 | Pending |
| CLN-01 | Phase 7 | Pending |
| CLN-02 | Phase 7 | Pending |
| CLN-03 | Phase 7 | Pending |
| CLN-04 | Phase 7 | Pending |
| CLN-05 | Phase 7 | Pending |

**Coverage:**
- v1 requirements: 35 total
- Mapped to phases: 35
- Unmapped: 0

---
*Requirements defined: 2026-05-08*
*Last updated: 2026-05-08 after initial definition*
