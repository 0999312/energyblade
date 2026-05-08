# Phase 3: Data And State - Context

**Gathered:** 2026-05-08
**Status:** Ready for planning

<domain>
## Phase Boundary

Migrate the Forge Energy storage system and capability provider to NeoForge 1.21.1 data components and energy API. Target files: `FEBladeStorage.java`, `FECapabilityProvider.java`. Update `ItemFEBlade.java` `ICapabilityProvider` reference. Success criteria: energy storage, charging, drain, power toggle, and NBT serialization work via NeoForge APIs without Forge imports.
</domain>

<decisions>
## Implementation Decisions

### Energy Storage API
- **D-01:** Keep `IEnergyStorage` interface — package rename only: `net.minecraftforge.energy.IEnergyStorage` → `net.neoforged.neoforge.energy.IEnergyStorage`. Same methods (`receiveEnergy`, `extractEnergy`, `getEnergyStored`, `getMaxEnergyStored`, `canReceive`, `canExtract`).
- **D-02:** Switch serialization from `INBTSerializable<CompoundTag>` to NeoForge `DataComponent`. FEBladeStorage custom fields (energy, capacity, maxReceive, maxExtract, powerupExtract, standbyExtract, energyDurability, isPowered) become a registered `DataComponentType`.

### the agent's Discretion
- **Capability→DataAttachment bridge:** `FECapabilityProvider` currently extends `NamedBladeStateCapabilityProvider` (SlashBlade) and provides `ForgeCapabilities.ENERGY` + `ItemSlashBlade.BLADESTATE` via `Capability<T>`/`LazyOptional<T>`. NeoForge replaces this with `DataAttachment` registrable via `DeferredRegister`. The researcher/planner determines the exact bridge approach.
- **Item capability injection:** `ItemFEBlade` overrides `initCapabilities(ICapabilityProvider)` — replaced by NeoForge's `DataComponent` on items. The planner determines the exact migration path.
- **`@AutoRegisterCapability` removal:** This Forge annotation has no NeoForge equivalent. Data attachments are registered via `DeferredRegister<DataAttachmentType<?>>`.
- **`ForgeCapabilities.ENERGY` reference:** Replaced by NeoForge's energy capability key (if still needed) or the registered DataAttachment.
- **NBT save/load compatibility:** DataComponent types provide auto-sync via `StreamCodec` — the planner should ensure existing save data (NBT-based) remains loadable or provide a migration path.
</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase Definition & Scope
- `.planning/ROADMAP.md` § Phase 3 — Scope, success criteria, key migration targets
- `.planning/REQUIREMENTS.md` § CAP-01..CAP-06 — Capability and energy storage requirements

### Prior Phase Context
- `.planning/phases/01-build-and-entry/01-SUMMARY.md` — Build system + @Mod entry point established
- `.planning/phases/02-registration-and-lifecycle/02-SUMMARY.md` — Event subscribers migrated, annotation patterns

### Migration Rules & API Maps
- `AGENTS.md` — Migration rules, loader API research process
- `docs/migration/LOADER_API_MAP.md` — All confirmed Forge→NeoForge API mappings

### Source Files (Phase 3 targets)
- `src/main/java/cn/mmf/energyblade/energy/FEBladeStorage.java` — IEnergyStorage impl + INBTSerializable
- `src/main/java/cn/mmf/energyblade/energy/FECapabilityProvider.java` — Capability bridge (ENERGY + BLADESTATE)
- `src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` — ICapabilityProvider usage, energy drain/charge logic
- `src/main/java/cn/mmf/energyblade/Energyblade.java` — DeferredRegister reference pattern

### Codebase Maps
- `.planning/codebase/STACK.md` — Forge Capability/Energy APIs documented
- `.planning/codebase/INTEGRATIONS.md` — SlashBlade capability dependency
</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `FEBladeStorage.java` — Full IEnergyStorage implementation with 4 custom fields; NBT serialization/deserialization; `isPowered` toggle control
- `FECapabilityProvider.java` — Extends SlashBlade's `NamedBladeStateCapabilityProvider`; provides ENERGY + BLADESTATE capabilities; LazyOptional wrapper pattern
- `ItemFEBlade.java` — Overrides `initCapabilities()` to return `FECapabilityProvider`; handles energy drain on `SlashBladeEvent.UpdateEvent`; handles charge on `SlashBladeEvent.HitEvent`; `isPowered` state management

### Established Patterns
- Energy storage is tightly coupled to SlashBlade's capability system (`ItemSlashBlade.BLADESTATE`, `NamedBladeStateCapabilityProvider`)
- NBT serialization uses Forge's `INBTSerializable<CompoundTag>` pattern with explicit put/get
- Capability provider pattern: LazyOptional.of() → getCapability() → check cap == ForgeCapabilities.ENERGY
- Item charging: FE extraction on usage, FE reception from external sources

### Integration Points
- `FECapabilityProvider` extends SlashBlade's base capability provider — migration must preserve this hierarchy
- `ItemSlashBlade.BLADESTATE` capability should still work (SlashBlade handles its own capability migration)
- Energy drain logic in `ItemFEBlade` references `ForgeCapabilities.ENERGY` (needs replacement)
- `EnergyBladeBEWLR` renders energy bar using `ForgeCapabilities.ENERGY` — this is Phase 6 scope but the API key must be correct
</code_context>

<specifics>
## Specific Ideas

- User chose: Keep `IEnergyStorage` interface (package rename only) — minimal diff, all energy logic preserved
- User chose: Switch to `DataComponent` for serialization — FEBladeStorage fields become a registered DataComponentType
- User confirmed done after one area — remaining decisions left to the agent's discretion (researcher/planner)
</specifics>

<deferred>
## Deferred Ideas

- `EnergyBladeBEWLR` energy bar rendering with `ForgeCapabilities.ENERGY` → Phase 6 (Client Systems)
- SlashBlade capability migration (`NamedBladeStateCapabilityProvider`) — handled by SlashBlade's own NeoForge port
- Item charging from Mekanism — Phase 3 ensures the API surface is correct; actual integration testing is Phase 7
</deferred>

---

*Phase: 3-Data And State*
*Context gathered: 2026-05-08*
