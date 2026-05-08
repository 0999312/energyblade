# INTEGRATIONS.md

## External Mod Dependencies (Hard)

### SlashBlade: Resharped (mandatory)

- **Role**: Parent mod — this mod extends SlashBlade's item system
- **Dependency declaration**: `src/main/resources/META-INF/mods.toml:29-34` (mandatory, version `[1.3.37,)`)
- **API surface used**:
  - `ItemSlashBlade` — Base item class extended by `ItemFEBlade` (`src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java:39`)
  - `ItemTierSlashBlade` — SlashBlade weapon tier (`src/main/java/cn/mmf/energyblade/Energyblade.java:25`)
  - `SlashBladeTEISR` — Base item renderer extended by `EnergyBladeBEWLR` (`src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java:22`)
  - `NamedBladeStateCapabilityProvider` — Base capability provider for blade state NBT (`src/main/java/cn/mmf/energyblade/energy/FECapabilityProvider.java:14`)
  - `BladeModelManager`, `WavefrontObject`, `BladeRenderState` — Custom OBJ model rendering (`src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java`)
  - `SlashBladeEvent.UpdateEvent`, `SlashBladeEvent.HitEvent`, `SlashBladeEvent.PowerBladeEvent` — Event hooks (`src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java:179-229`)
  - `SlashBladeShapedRecipeBuilder`, `RequestDefinition`, `SlashBladeIngredient` — Recipe datagen (`src/main/java/cn/mmf/energyblade/data/SlashBladeRecipeProvider.java`)
  - `SlashBladeDefinition`, `PropertiesDefinition`, `RenderDefinition` — Datapack registry (`src/main/java/cn/mmf/energyblade/data/BuiltInSlashBladeRegistry.java`)
  - `CapabilityConcentrationRank` — Concentration rank interaction (`src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java:193`)
  - `CarryType`, `SwordType`, `SlashArtsRegistry` — Blade configuration (`src/main/java/cn/mmf/energyblade/data/BuiltInSlashBladeRegistry.java`)
- **Availability**: JAR in `libs/` (1.1.28), API from MMMaven (1.3.37)

### Forge Energy API (mandatory, via Forge)

- **API used**: `IEnergyStorage`, `ForgeCapabilities.ENERGY`
- **Key files**:
  - `src/main/java/cn/mmf/energyblade/energy/FEBladeStorage.java` — Custom energy storage implementation
  - `src/main/java/cn/mmf/energyblade/energy/FECapabilityProvider.java` — Capability provider for energy
  - `src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` — Item interaction with energy capability

## External Mod Dependencies (Soft / Optional)

### JEI (Just Enough Items)

- **Role**: Recipe viewer integration
- **Integration file**: `src/main/java/cn/mmf/energyblade/compat/JEICompat.java`
- **JEI API used**:
  - `@JeiPlugin` / `IModPlugin` — Plugin registration
  - `ISubtypeRegistration` — Item subtype interpreter (makes NBT-varying blade items appear as single JEI entry)
- **Scope**: Compile-only API, Runtime full jar. Mod works without JEI.

### Thermal Series (CoFH Core, Thermal Foundation/Expansion/Innovation/Locomotion/Integration/Dynamics)

- **Role**: Energy provider mods — provide FE energy to charge the blade
- **Integration**: No direct API calls. The blade accepts energy from any Forge Energy source. Thermal mods serve as the canonical FE charging source in the development environment.
- **Scope**: Implementation dependency only (not compile-time).

### NBTEdit Reborn

- **Role**: In-game NBT editor (development utility)
- **Integration**: No direct API calls. Listed as implementation dependency.
- **Scope**: Development convenience only.

### CodeChicken Lib

- **Role**: Library used by Thermal series mods
- **Integration**: No direct API calls. Transitive dependency of Thermal mods.

## Development Tool Integrations

### MCP (Model Context Protocol) Tools

Configured in `opencode.json:27-50`:

- **mc-source** (`@mcdxai/minecraft-dev-mcp`) — Minecraft 原版源码、映射、签名查询
- **loader-docs** (`mcmodding-mcp`) — Forge/NeoForge loader API 文档与示例查询

### AI Agent System

Configured in `opencode.json:56-93`:
- `build` agent — Primary agent for code changes, can delegate to research agents
- `plan` agent — Planning agent, read-only analysis, can delegate to research agents
- Sub-agents available: `loader-diff-research`, `vanilla-code-research`, `port-review`

## No External Services

This mod has no external API calls, no databases, no webhooks, no auth providers. It is a purely client-server Minecraft mod with local dependency resolution.
