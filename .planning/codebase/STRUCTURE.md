# STRUCTURE.md

## Root Layout

```
energyblade/
├── .git/                          # Git repository
├── .gitignore
├── .opencode/                     # OpenCode AI assistant config
│   ├── agents/                    # Custom agent definitions
│   ├── commands/                  # Custom slash commands
│   ├── node_modules/              # MCP tool dependencies
│   ├── package.json
│   └── package-lock.json
├── .planning/                     # GSD planning artifacts
│   └── codebase/                  # This mapping output
├── AGENTS.md                      # AI agent instructions (migration rules)
├── build.gradle                   # Gradle build script (179 lines)
├── docs/
│   └── migration/
│       ├── LOADER_API_MAP.md      # Forge→NeoForge API mappings
│       ├── MIGRATION_PLAN.md      # Phase-by-phase migration plan
│       ├── PREREQUISITES.md       # Environment prerequisite checklist
│       └── PROGRESS.md            # Current migration progress/blockers
├── gradle/                        # Gradle wrapper
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties              # Build properties (mod version, MC version, etc.)
├── gradlew / gradlew.bat          # Gradle wrapper scripts
├── libs/                          # Flat directory for local JAR dependencies
│   └── SlashBladeResharped-1.20.1-1.1.28.jar
├── opencode.json                  # OpenCode runtime configuration
├── run-data/                      # Data generation run directory
│   └── config/
│       ├── cofh_core-common.toml
│       ├── fml.toml
│       ├── thermal-common.toml
│       └── thermal_integration-common.toml
├── settings.gradle                # Gradle settings (plugin repos, root project name)
└── src/
    ├── generated/
    │   └── resources/
    │       ├── .cache/            # Datagen cache
    │       └── data/
    │           └── energyblade/
    │               ├── advancements/recipes/combat/hf_blade.json
    │               ├── recipes/hf_blade.json
    │               └── slashblade/named_blades/hf_blade.json
    └── main/
        ├── java/
        │   └── cn/mmf/energyblade/
        │       ├── Energyblade.java                  # Mod entry point + item registration
        │       ├── NetworkPacketHandler.java          # SimpleChannel network setup
        │       ├── PowerSwitchPacket.java             # Client→Server power toggle packet
        │       ├── client/
        │       │   ├── ClientSetupHandler.java        # Client model/key binding setup
        │       │   ├── InputHandler.java              # Key press detection
        │       │   └── render/
        │       │       └── EnergyBladeBEWLR.java      # Custom item renderer (icon + durability bar)
        │       ├── compat/
        │       │   └── JEICompat.java                 # JEI plugin for item subtypes
        │       ├── data/
        │       │   ├── BuiltInSlashBladeRegistry.java # Datapack blade definitions
        │       │   ├── DataGen.java                   # Datagen event listener
        │       │   └── SlashBladeRecipeProvider.java  # Recipe datagen provider
        │       ├── energy/
        │       │   ├── FEBladeStorage.java            # IEnergyStorage implementation
        │       │   └── FECapabilityProvider.java      # Capability provider (energy + blade state)
        │       └── item/
        │           └── ItemFEBlade.java               # Main SlashBlade item class
        └── resources/
            ├── META-INF/
            │   └── mods.toml                          # Forge mod metadata
            ├── assets/
            │   └── energyblade/
            │       ├── lang/
            │       │   ├── en_us.json                 # English translations
            │       │   └── zh_cn.json                 # Chinese translations
            │       └── model/
            │           ├── hf_blade.obj               # 3D blade model (Wavefront OBJ)
            │           └── hf_blade.png               # Blade texture
            ├── energyblade.mixins.json                # Mixin config (no active mixins)
            └── pack.mcmeta                            # Resource pack metadata
```

## Key File Locations

| Purpose | Path |
|---|---|
| Mod entry point | `src/main/java/cn/mmf/energyblade/Energyblade.java` |
| Main item class | `src/main/java/cn/mmf/energyblade/item/ItemFEBlade.java` |
| Energy storage | `src/main/java/cn/mmf/energyblade/energy/FEBladeStorage.java` |
| Capability provider | `src/main/java/cn/mmf/energyblade/energy/FECapabilityProvider.java` |
| Network setup | `src/main/java/cn/mmf/energyblade/NetworkPacketHandler.java` |
| Packet handler | `src/main/java/cn/mmf/energyblade/PowerSwitchPacket.java` |
| Client setup | `src/main/java/cn/mmf/energyblade/client/ClientSetupHandler.java` |
| Key input | `src/main/java/cn/mmf/energyblade/client/InputHandler.java` |
| Custom renderer | `src/main/java/cn/mmf/energyblade/client/render/EnergyBladeBEWLR.java` |
| JEI compat | `src/main/java/cn/mmf/energyblade/compat/JEICompat.java` |
| Datagen entry | `src/main/java/cn/mmf/energyblade/data/DataGen.java` |
| Blade registry defs | `src/main/java/cn/mmf/energyblade/data/BuiltInSlashBladeRegistry.java` |
| Recipe gen | `src/main/java/cn/mmf/energyblade/data/SlashBladeRecipeProvider.java` |
| Mod metadata | `src/main/resources/META-INF/mods.toml` |
| Build config | `build.gradle` |
| Build properties | `gradle.properties` |
| Mixin config | `src/main/resources/energyblade.mixins.json` |

## Package Naming Convention

- **Root package**: `cn.mmf.energyblade`
- **Sub-packages**: `item`, `energy`, `client`, `client.render`, `compat`, `data`
- **Class naming**: PascalCase, descriptive. Examples: `ItemFEBlade`, `FEBladeStorage`, `EnergyBladeBEWLR`, `NetworkPacketHandler`
- **Resource domain**: `energyblade` (matches mod ID)

## Source Set Convention

- `src/main/java/` — Main source
- `src/main/resources/` — Main resources (packed into JAR)
- `src/generated/resources/` — Datagen output (merged into main resources via `build.gradle:81`)
- `run-data/` — Datagen working directory (not in JAR)

## File Count Summary

| Category | Count |
|---|---|
| Java source files | 10 |
| JSON resource files | 6 |
| TOML/MCmeta | 2 |
| Build files | 3 |
| Migration docs | 4 |
| OBJ/PNG assets | 2 |
| **Total tracked source** | ~27 |

This is a small mod (~10 source files, ~500 lines of Java) with a single item, one custom packet, one capability, and standard datagen/JEI integration.
