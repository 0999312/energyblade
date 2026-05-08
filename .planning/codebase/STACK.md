# STACK.md

## Languages

| Language | Usage | Notes |
|---|---|---|
| Java | 100% of source | Forge mod development |
| JSON | Config, lang, mixin metadata | Resource files |
| TOML | Mod metadata | `mods.toml` |
| GLSL / GLFW | Client render | Via LWJGL |

## Runtime

- **Minecraft 1.20.1** (target version)
- **Java 17** (JDK 17, configured in `build.gradle:28`)
- **Forge 47.4.0** (via `net.minecraftforge.gradle` plugin)

## Build System

- **Gradle 8.6** (`gradle/wrapper/gradle-wrapper.properties:3`)
- **ForgeGradle** `[6.0.16,6.2)` — main build plugin (`build.gradle:15`)
- **MixinGradle** `0.7-SNAPSHOT` — Mixin build support (`build.gradle:7`)
- **foojay-resolver** `0.7.0` — Toolchain resolution (`settings.gradle:12`)
- **Maven Publish** plugin (`build.gradle:14`)

## Core Dependencies

| Dependency | Version | Scope | Source |
|---|---|---|---|
| **MinecraftForge** | 1.20.1-47.4.0 | Implementation | Forge Maven |
| **Mixin** | 0.8.5 | Annotation processor | SpongePowered |
| **SlashBlade: Resharped** | 1.3.37 (API) / 1.1.28 (libs jar) | Implementation | MMMaven / flatDir `libs/` |

## Optional / Integration Dependencies

| Dependency | Version | Scope | Source |
|---|---|---|---|
| **JEI** | 15.20.0.105 | Compile-only (API) / Runtime (full) | CurseMaven / ModMaven |
| **NBTEdit Reborn** | 5984630 | Implementation | CurseMaven |
| **CoFH Core** | 5374122 | Implementation | CurseMaven |
| **CodeChicken Lib** | 5753868 | Implementation | CurseMaven |
| **Thermal Foundation** | 5443583 | Implementation | CurseMaven |
| **Thermal Expansion** | 5372749 | Implementation | CurseMaven |
| **Thermal Innovation** | 5372751 | Implementation | CurseMaven |
| **Thermal Locomotion** | 5372752 | Implementation | CurseMaven |
| **Thermal Integration** | 5374210 | Implementation | CurseMaven |
| **Thermal Dynamics** | 5372747 | Implementation | CurseMaven |

## Key Forge APIs Used

| API | Purpose | Files |
|---|---|---|
| `@Mod` | Mod entry point | `src/main/java/cn/mmf/energyblade/Energyblade.java:16` |
| `DeferredRegister` | Item registration | `src/main/java/cn/mmf/energyblade/Energyblade.java:21` |
| `ForgeCapabilities.ENERGY` | FE energy capability | `src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` (multiple) |
| `IEnergyStorage` | Energy storage interface | `src/main/java/cn/mmf/energyblade/energy/FEBladeStorage.java:9` |
| `SimpleChannel` | Network packets | `src/main/java/cn/mmf/energyblade/NetworkPacketHandler.java:9` |
| `GatherDataEvent` | Datagen entry point | `src/main/java/cn/mmf/energyblade/data/DataGen.java:19` |
| `DatapackBuiltinEntriesProvider` | Built-in datapack entries | `src/main/java/cn/mmf/energyblade/data/DataGen.java:32` |
| `@EventBusSubscriber` | Auto-register event listeners | `src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java:38` |
| `@SubscribeEvent` | Event handler annotation | Throughout codebase |
| `@OnlyIn(Dist.CLIENT)` | Client-side only code | `src/main/java/cn/mmf/energyblade/client/*` |
| `IClientItemExtensions` | Custom item renderer injection | `src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java:138` |
| `AutoRegisterCapability` | Automatic capability registration | `src/main/java/cn/mmf/energyblade/energy/FEBladeStorage.java:8` |
| `IConditionBuilder` | Recipe conditions (datagen) | `src/main/java/cn/mmf/energyblade/data/SlashBladeRecipeProvider.java:16` |

## External Libraries (Client)

- **LWJGL 3** (GLFW) — Key input detection (`org.lwjgl.glfw.GLFW`)
- **SlF4J** — Logging via `com.mojang.logging.LogUtils`

## Configuration

- `gradle.properties` — Minecraft/Forge version, mod metadata, JEI version, maven publish path
- `src/main/resources/META-INF/mods.toml` — Mod loader metadata (dependencies on Forge, Minecraft, SlashBlade)
- `src/main/resources/pack.mcmeta` — Resource pack format 15 (1.20.1)
- `src/main/resources/energyblade.mixins.json` — Mixin config (empty mixin lists — no active mixins)
- `opencode.json` — AI coding assistant config (MCP tools, agent permissions)
- `run-data/config/*.toml` — Runtime configs for Thermal mods and FML

## Mixin Status

Mixin configuration exists at `src/main/resources/energyblade.mixins.json` but both `mixins` and `client` arrays are empty. No Mixin classes are actually used in the codebase despite MixinGradle being configured.
