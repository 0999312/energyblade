# HF Blade — Minecraft Mod Migration

## What This Is

Migrate the **HF Blade (energyblade)** Minecraft mod from **1.20.1 Forge** to **1.21.1 NeoForge**. The mod adds a Forge Energy-powered SlashBlade weapon that consumes FE to maintain a powered state, granting combat benefits. The migration follows a 7-phase plan covering build system, registration, capabilities, networking, datagen, client systems, and validation.

## Core Value

The mod must compile, load, and function identically on NeoForge 1.21.1 — the FE-powered SlashBlade must charge from energy sources, toggle power state via keybind, drain energy while active, and render correctly with the custom OBJ model and energy durability bar.

## Requirements

### Validated

- ✓ FE energy storage system (FEBladeStorage) tracks energy/capacity/power-state — existing (`src/main/java/cn/mmf/energyblade/energy/FEBladeStorage.java`)
- ✓ Item registration via DeferredRegister with custom SlashBlade tier — existing (`src/main/java/cn/mmf/energyblade/Energyblade.java`)
- ✓ Capability provider combining BLADESTATE + ENERGY capabilities — existing (`src/main/java/cn/mmf/energyblade/energy/FECapabilityProvider.java`)
- ✓ Network packet for power toggle (client→server) with particle/sound feedback — existing (`src/main/java/cn/mmf/energyblade/PowerSwitchPacket.java`)
- ✓ Custom BEWLR rendering energy-based durability bar on item icon — existing (`src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java`)
- ✓ Datagen for SlashBlade recipe and built-in blade registry definition — existing (`src/main/java/cn/mmf/energyblade/data/`)
- ✓ JEI integration for item subtype interpretation — existing (`src/main/java/cn/mmf/energyblade/compat/JEICompat.java`)
- ✓ Client keybinding (Shift+V) for power toggle — existing (`src/main/java/cn/mmf/energyblade/client/InputHandler.java`)
- ✓ SlashBlade event hooks (UpdateEvent, HitEvent, PowerBladeEvent) for energy drain and power state — existing (`src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java`)
- ✓ Chinese + English localization — existing (`src/main/resources/assets/energyblade/lang/`)

### Active

- [x] **MIG-01**: Build system upgraded to NeoForge 1.21.1 Gradle plugins and dependencies
- [x] **MIG-02**: Mod entry point and registration migrated to NeoForge APIs
- [x] **MIG-03**: Event bus and lifecycle events migrated to NeoForge equivalents
- [x] **MIG-04**: Capability system (IEnergyStorage, capability provider) migrated to NeoForge data components / attachments
- [x] **MIG-05**: Network system (SimpleChannel, packet registration) migrated to NeoForge payload system
- [x] **MIG-06**: Datagen (GatherDataEvent, recipe/provider) migrated to NeoForge datagen APIs
- [x] **MIG-07**: Client systems (renderer, input, keymapping, model baking) migrated to NeoForge client APIs
- [x] **MIG-08**: All Forge imports and annotations cleaned up — zero Forge references remain
- [x] **MIG-09**: Compilation succeeds with `./gradlew compileJava`
- [x] **MIG-10**: Datagen runs successfully with `./gradlew runData`
- [x] **MIG-11**: Client loads and blade renders/behaves correctly with `./gradlew runClient`

### Out of Scope

- Dual-version compatibility (1.20.1 + 1.21.1) — per migration rules: no compatibility layer
- New features beyond what the 1.20.1 version provides — this is a port, not an expansion
- SlashBlade: Resharped upstream porting — the dependency must already be available for NeoForge 1.21.1
- Unit tests / GameTest framework — not in original codebase, deferred to post-migration

## Context

This is a brownfield migration of a small (~10 Java files, ~500 LOC) Forge mod. The codebase has been mapped in `.planning/codebase/`. The migration is governed by AGENTS.md rules:

- Target: 1.20.1 Forge → 1.21.1 NeoForge (fixed)
- No dual-version compatibility layer
- Minimal changes — fix compilation, behavior regressions, migration blockers
- Loader API differences must be researched via `loader-docs` MCP before making changes
- Each confirmed API mapping gets recorded in `docs/migration/LOADER_API_MAP.md`
- Phases advance sequentially — one phase at a time

The mod depends heavily on **SlashBlade: Resharped** — this dependency MUST be ported to NeoForge 1.21.1 before this migration can complete. Without it, the migration is blocked at Phase 3+.

## Constraints

- **Java**: 21+ (NeoForge 1.21.1 requirement, up from 17)
- **Gradle**: Must use NeoGradle (NG) instead of ForgeGradle
- **Mappings**: Mojang official mappings (already used in 1.20.1)
- **Dependencies**: SlashBlade: Resharped must be available on NeoForge 1.21.1
- **Tooling**: OpenCode with MCP tools (`mc-source`, `loader-docs`)
- **Scope**: Port only — no feature additions or redesign
- **Mixin**: Empty mixin config should be removed or migrated (current: no active mixins)

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Use coarse granularity (3-5 phases) | Small codebase, phased migration plan already exists | — Pending |
| Parallel execution where possible | Build/datagen/independent phases can run in parallel | — Pending |
| Research before each phase | Loader API differences are complex and need verification | — Pending |
| Plan check + verifier enabled | Migration correctness is critical | — Pending |
| Use inherit model profile | Consistency with current session | — Pending |
| Commit planning docs to git | Track migration progress in version control | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-05-08 after initialization*
