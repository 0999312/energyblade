# Walking Skeleton — HF Blade Migration

**Phase:** 1
**Generated:** 2026-05-08

## Capability Proven End-to-End

`./gradlew compileJava` succeeds against NeoForge 1.21.1 — the build system compiles the mod entry point with zero errors, proving the entire toolchain (Gradle + NeoGradle + NeoForge MCP + dependencies) is wired correctly.

## Architectural Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Framework | NeoForge 21.1.228 (Minecraft 1.21.1) | Migration target; latest stable 1.21.1 release |
| Build plugin | `net.neoforged.moddev` v2.0.141 (ModDevGradle) | User-specified; replaces ForgeGradle + MixinGradle |
| Mappings | Parchment 2024.11.10 for 1.21.1 | Community-readable parameter names |
| Java version | JDK 21 | Minecraft 1.21+ ships Java 21 to end users |
| Gradle version | 8.13 | ModDevGradle requires 8.8+; 8.13 is known stable |
| Mod metadata format | `neoforge.mods.toml` in `src/main/templates/` | NeoForge convention; auto-expanded by `generateModMetadata` |
| Registry registration | Constructor-injected `IEventBus` + `DeferredRegister` | NeoForge 1.21.1 canonical pattern; replaces `FMLJavaModLoadingContext` |
| Registry key source | `BuiltInRegistries.ITEM` (vanilla) for items | Minecraft-native registries; `NeoForgeRegistries` for NeoForge-only types |
| Mixin strategy | Stripped entirely | No active mixins in 1.20.1 codebase; NeoForge bundles Mixin 0.8.7 natively |
| Dependency resolution | MMMaven + ModMaven + CurseMaven; no flatDir | `libs/` directory removed; all deps from remote repos |
| Optional runtime deps | `localRuntime` configuration (`runtimeClasspath.extendsFrom`) | MDK convention for dev-only mod deps (JEI) |
| Publishing target | Local Maven at `maven_loc` path | Preserves existing publishing workflow |

## Stack Touched in Phase 1

- [x] Project scaffold — Gradle 8.13 + `net.neoforged.moddev` + `java-library` + `maven-publish` + `idea`
- [x] Build toolchain — JDK 21 + Parchment mappings + NeoForge 21.1.228
- [x] Mod metadata — `neoforge.mods.toml` with NeoForge/Minecraft/SlashBlade dependencies
- [x] Mod entry point — `Energyblade.java` with `IEventBus` constructor injection + `DeferredRegister`
- [x] Dependency resolution — SlashBlade (MMMaven), JEI (compileOnly/localRuntime), Mekanism (implementation)
- [x] Compilation verification — `./gradlew compileJava` proving end-to-end build works

## Out of Scope (Deferred to Later Slices)

- Registration & Lifecycle migration (`DeferredRegister` in other files, `@EventBusSubscriber`, creative tab) — Phase 2
- Energy capability / data attachment system (`FEBladeStorage`, `FECapabilityProvider`) — Phase 3
- Network packet system (`SimpleChannel` → NeoForge payload) — Phase 4
- Datagen migration (recipe provider, registry builder, conditions) — Phase 5
- Client systems (renderer, keybindings, model baking, JEI compat) — Phase 6
- Import cleanup, Forge reference removal, end-to-end validation — Phase 7
- `ItemFEBlade.java` Forge imports — left untouched in Phase 1 (compilation errors expected)
- `NetworkPacketHandler.java` — left untouched (will not compile until Phase 4)
- Any behavioral testing (`runClient`, crafting, combat) — deferred to Phase 7

## Subsequent Slice Plan

Each later phase adds one vertical slice on top of this skeleton without altering its architectural decisions:

- Phase 2: Migrate item registration, event bus, and lifecycle events to NeoForge
- Phase 3: Migrate capability/energy system to NeoForge data components and attachments
- Phase 4: Migrate custom packet system to NeoForge payload API
- Phase 5: Migrate recipe and registry datagen to NeoForge data generation API
- Phase 6: Migrate renderer, input handling, keybindings, and model baking to NeoForge client
- Phase 7: Remove all Forge references, verify end-to-end functionality
