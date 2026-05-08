# Roadmap: HF Blade Migration

**Created:** 2026-05-08
**Granularity:** Coarse (7 phases)
**Mode:** yolo (auto-advance)

---

### Phase 1: Build And Entry

**Goal:** Upgrade build system and mod entry point to compile against NeoForge 1.21.1.

**Mode:** mvp

**Success Criteria:**
1. `./gradlew compileJava` succeeds with zero errors (BLD-06)
2. Gradle wrapper, plugins, and dependencies all NeoForge 1.21.1 compatible
3. Mod metadata (`mods.toml`) loads correctly in NeoForge

**Requirements:** BLD-01, BLD-02, BLD-03, BLD-04, BLD-05, BLD-06

**Key Migration Targets:**
- `build.gradle` — ForgeGradle → NeoGradle, JDK 17 → 21, dependencies
- `gradle.properties` — forge_version → neoforge_version
- `settings.gradle` — MinecraftForge maven → NeoForge maven
- `src/main/resources/META-INF/mods.toml` — modLoader, loaderVersion
- `cn.mmf.energyblade.Energyblade` — `@Mod` entry point

---

### Phase 2: Registration And Lifecycle

**Goal:** Migrate item registration, event bus, and lifecycle events to NeoForge.

**Mode:** mvp

**Success Criteria:**
1. Item registered via NeoForge `DeferredRegister` API
2. All `@EventBusSubscriber` annotations compile and events fire
3. `FMLCommonSetupEvent` equivalent triggers network registration

**Requirements:** REG-01, REG-02, REG-03, REG-04

**Key Migration Targets:**
- `cn.mmf.energyblade.Energyblade` — registration + mod event bus
- `cn.mmf.energyblade.client.ClientSetupHandler` — client lifecycle events
- `cn.mmf.energyblade.item.ItemFEBlade` — `@EventBusSubscriber` annotations

---

### Phase 3: Data And State

**Goal:** Migrate capability/energy system to NeoForge data components and attachments.

**Mode:** mvp

**Success Criteria:**
1. `FEBladeStorage` energy data persists through NBT
2. Energy capability is attachable to `ItemStack` via NeoForge API
3. `ForgeCapabilities.ENERGY` references replaced with NeoForge equivalents

**Requirements:** DAT-01, DAT-02, DAT-03, DAT-04, DAT-05

**Key Migration Targets:**
- `cn.mmf.energyblade.energy.FEBladeStorage` — `IEnergyStorage` replacement
- `cn.mmf.energyblade.energy.FECapabilityProvider` — `ICapabilityProvider` → attachment system
- `cn.mmf.energyblade.item.ItemFEBlade` — `getCapability()` calls, `getShareTag`/`readShareTag`

**Blocker Risk:** SlashBlade must be available on NeoForge — its `BLADESTATE` capability must also be migrated.

---

### Phase 4: Networking And Sync

**Goal:** Migrate custom packet system to NeoForge payload API.

**Mode:** mvp

**Success Criteria:**
1. `PowerSwitchPacket` sent from client to server successfully
2. Server handles power toggle with particles and sound
3. `NetworkPacketHandler` replaced with NeoForge network setup

**Requirements:** NET-01, NET-02, NET-03, NET-04

**Key Migration Targets:**
- `cn.mmf.energyblade.NetworkPacketHandler` — `SimpleChannel` → NeoForge network
- `cn.mmf.energyblade.PowerSwitchPacket` — `FriendlyByteBuf` → `StreamCodec`
- `cn.mmf.energyblade.client.InputHandler` — sendToServer path

---

### Phase 5: Datagen And Resources

**Goal:** Migrate recipe and registry datagen to NeoForge data generation API.

**Mode:** mvp

**Success Criteria:**
1. `./gradlew runData` produces identical `src/generated/` output
2. SlashBlade recipe generated correctly
3. Built-in blade registry definition registered in datapack

**Requirements:** GEN-01, GEN-02, GEN-03, GEN-04, GEN-05

**Key Migration Targets:**
- `cn.mmf.energyblade.data.DataGen` — `GatherDataEvent` → NeoForge equivalent
- `cn.mmf.energyblade.data.SlashBladeRecipeProvider` — recipe gen API changes
- `cn.mmf.energyblade.data.BuiltInSlashBladeRegistry` — `RegistrySetBuilder` changes

---

### Phase 6: Client Systems

**Goal:** Migrate renderer, input handling, keybindings, and model baking to NeoForge client.

**Mode:** mvp

**Success Criteria:**
1. `EnergyBladeBEWLR` renders blade icon with energy durability bar
2. Shift+V keybinding toggles power state
3. Blade model bakes correctly in client
4. JEI compatibility loads without errors

**Requirements:** CLI-01, CLI-02, CLI-03, CLI-04, CLI-05, CLI-06, CLI-07

**Key Migration Targets:**
- `cn.mmf.energyblade.client.ClientSetupHandler` — model baking, key registration
- `cn.mmf.energyblade.client.InputHandler` — key input handling
- `cn.mmf.energyblade.client.render.EnergyBladeBEWLR` — `IClientItemExtensions`
- `cn.mmf.energyblade.compat.JEICompat` — JEI NeoForge API
- `cn.mmf.energyblade.item.ItemFEBlade` — `@OnlyIn` annotations, client methods

---

### Phase 7: Cleanup And Validation

**Goal:** Remove all Forge references, verify end-to-end functionality.

**Mode:** mvp

**Success Criteria:**
1. Zero `net.minecraftforge.*` or `cpw.mods.*` imports in codebase
2. `./gradlew runClient` loads mod, blade functions identically to 1.20.1
3. `LOADER_API_MAP.md` fully documented with all confirmed API mappings

**Requirements:** CLN-01, CLN-02, CLN-03, CLN-04, CLN-05

**Key Migration Targets:**
- All Java files — import cleanup, annotation cleanup
- `src/main/resources/energyblade.mixins.json` — remove or migrate
- `docs/migration/LOADER_API_MAP.md` — finalize documentation
- Full integration test: craft blade → charge with FE → toggle power → combat

---

*Last updated: 2026-05-08 after roadmap creation*
