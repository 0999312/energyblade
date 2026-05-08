---
phase: 1
plan: 01-build-and-entry
type: execute
wave: 1
depends_on: []
files_modified:
  - gradle/wrapper/gradle-wrapper.properties
  - settings.gradle
  - gradle.properties
  - build.gradle
  - src/main/templates/META-INF/neoforge.mods.toml (create)
  - src/main/resources/META-INF/mods.toml (delete)
  - src/main/java/cn/mmf/energyblade/Energyblade.java
  - src/main/resources/energyblade.mixins.json (delete)
autonomous: true
requirements: [BLD-01, BLD-02, BLD-03, BLD-04, BLD-05, BLD-06]
---

# Plan 01-Build-And-Entry: Migrate Build System & Mod Entry Point to NeoForge 1.21.1

## Objective

Upgrade the Gradle build system, mod metadata, and mod entry point (`Energyblade.java`) so that `./gradlew compileJava` succeeds against NeoForge 1.21.1 with zero errors.

## User Story

**As a** developer, **I want to** run `./gradlew compileJava` and have the mod compile against NeoForge 1.21.1, **so that** all subsequent migration phases have a working build foundation.

## Context

This is Phase 1 of a 1.20.1 Forge → 1.21.1 NeoForge migration. All decisions are locked in `01-CONTEXT.md` and `01-RESEARCH.md`. The walking skeleton for this project is "mod compiles and loads in NeoForge."

---

## Tasks

### Task 1: Update Gradle Wrapper

<task id="01-wrapper" type="execute" files="gradle/wrapper/gradle-wrapper.properties" autonomous="true">

<read_first>
- gradle/wrapper/gradle-wrapper.properties
</read_first>

<action>
Replace the distribution URL in `gradle/wrapper/gradle-wrapper.properties`:

Old: `distributionUrl=https\://services.gradle.org/distributions/gradle-8.6-bin.zip`
New: `distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-bin.zip`
</action>

<acceptance_criteria>
- `gradle/wrapper/gradle-wrapper.properties` contains exactly one `distributionUrl=` line and it matches `gradle-8.13-bin.zip`
- File contains `validateDistributionUrl=true` (existing content preserved)
</acceptance_criteria>

</task>

### Task 2: Update settings.gradle

<task id="02-settings" type="execute" files="settings.gradle" autonomous="true">

<read_first>
- settings.gradle
- .planning/codebase/STACK.md
</read_first>

<action>
Rewrite `settings.gradle`:

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = 'NeoForge'
            url = 'https://maven.neoforged.net/releases'
        }
    }
}

plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '0.8.0'
}

rootProject.name = 'energyblade'
```

Changes:
1. Remove `MinecraftForge` maven — Gradle Plugin Portal serves ModDevGradle
2. Add `NeoForge` maven at `https://maven.neoforged.net/releases` for dependency resolution
3. Bump `foojay-resolver-convention` from `0.7.0` to `0.8.0`
</action>

<acceptance_criteria>
- `settings.gradle` does NOT contain `minecraftforge.net`
- `settings.gradle` contains `maven.neoforged.net/releases`
- `settings.gradle` contains `id 'org.gradle.toolchains.foojay-resolver-convention' version '0.8.0'`
</acceptance_criteria>

</task>

### Task 3: Update gradle.properties

<task id="03-properties" type="execute" files="gradle.properties" autonomous="true">

<read_first>
- gradle.properties
- .planning/phases/01-build-and-entry/01-RESEARCH.md
</read_first>

<action>
Rewrite `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx3G
org.gradle.daemon=false
org.gradle.parallel=true

# Parchment (community mappings)
parchment_minecraft_version=1.21.1
parchment_mappings_version=2024.11.10

# Core versions
minecraft_version=1.21.1
minecraft_version_range=[1.21.1,1.22)
neo_version=21.1.228
neo_version_range=[21.1,)
loader_version_range=[1,)

# JEI
jei_version=19.27.0.340

# Mekanism
mekanism_version=10.7.19.85

# Mod metadata
mod_id=energyblade
mod_name=HF Blade
mod_license=MIT License
mod_version=1.0.3-1.21.1
mod_group_id=cn.mmf.energyblade
mod_authors=Arcomit, Syameimaru Zheng

maven_loc=/D:/maven/MMMaven/repository
```

Changes:
1. Removed: `forge_version=47.4.0`, `forge_version_range=[47,)`, `mapping_channel=official`, `mapping_version=1.20.1`
2. Added: `neo_version=21.1.228`, `neo_version_range=[21.1,)`, `parchment_minecraft_version=1.21.1`, `parchment_mappings_version=2024.11.10`
3. Updated: `minecraft_version=1.21.1`, `minecraft_version_range=[1.21.1,1.22)`, `jei_version=19.27.0.340`, `mod_version=1.0.3-1.21.1`
4. Changed: `loader_version_range=[1,)` (was `[47,)`)
5. Added: `mekanism_version=10.7.19.85`, `org.gradle.parallel=true`
</action>

<acceptance_criteria>
- `gradle.properties` does NOT contain `forge_version` or `mapping_channel`
- `gradle.properties` contains `neo_version=21.1.228`
- `gradle.properties` contains `parchment_mappings_version=2024.11.10`
- `gradle.properties` contains `minecraft_version=1.21.1`
- `gradle.properties` contains `jei_version=19.27.0.340`
- `gradle.properties` contains `mekanism_version=10.7.19.85`
</acceptance_criteria>

</task>

### Task 4: Rewrite build.gradle (NeoGradle + ModDevGradle)

<task id="04-build" type="execute" files="build.gradle" autonomous="true">

<read_first>
- build.gradle
- .planning/phases/01-build-and-entry/01-RESEARCH.md § 2 (Complete build.gradle template)
- .planning/codebase/STACK.md
- .planning/codebase/INTEGRATIONS.md
- gradle.properties (updated values)
</read_first>

<action>
Replace the entire content of `build.gradle` with:

```groovy
plugins {
    id 'java-library'
    id 'maven-publish'
    id 'net.neoforged.moddev' version '2.0.141'
    id 'idea'
}

group = mod_group_id
version = mod_version

base {
    archivesName = mod_id
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

neoForge {
    version = project.neo_version

    parchment {
        mappingsVersion = project.parchment_mappings_version
        minecraftVersion = project.parchment_minecraft_version
    }

    runs {
        client {
            client()
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }
        server {
            server()
            programArgument '--nogui'
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }
        gameTestServer {
            type = "gameTestServer"
            systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id
        }
        data {
            data()
            programArguments.addAll '--mod', project.mod_id, '--all',
                    '--output', file('src/generated/resources/').getAbsolutePath(),
                    '--existing', file('src/main/resources/').getAbsolutePath()
        }
        configureEach {
            systemProperty 'forge.logging.markers', 'REGISTRIES'
            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        "${mod_id}" {
            sourceSet(sourceSets.main)
        }
    }
}

sourceSets.main.resources { srcDir 'src/generated/resources' }

configurations {
    runtimeClasspath.extendsFrom localRuntime
}

repositories {
    maven {
        url "https://cursemaven.com"
    }
    maven {
        url = "https://raw.github.com/0999312/MMMaven/main/repository"
    }
    maven {
        name = "ModMaven"
        url = "https://modmaven.dev"
    }
}

dependencies {
    // SlashBlade: Resharped (NeoForge 1.21.1 port)
    implementation "mods.flammpfeil.slashblade:SlashBlade_Resharped:2.0.2-1.21.1"

    // JEI (compile-only API, runtime full jar via localRuntime)
    compileOnly "mezz.jei:jei-${minecraft_version}-common-api:${jei_version}"
    compileOnly "mezz.jei:jei-${minecraft_version}-neoforge-api:${jei_version}"
    localRuntime "mezz.jei:jei-${minecraft_version}-neoforge:${jei_version}"

    // Mekanism (runtime-only for FE charging dev testing)
    implementation "mekanism:Mekanism:${minecraft_version}-${mekanism_version}"
}

var generateModMetadata = tasks.register("generateModMetadata", ProcessResources) {
    var replaceProperties = [
            minecraft_version      : minecraft_version,
            minecraft_version_range: minecraft_version_range,
            neo_version            : neo_version,
            neo_version_range      : neo_version_range,
            loader_version_range   : loader_version_range,
            mod_id                 : mod_id,
            mod_name               : mod_name,
            mod_license            : mod_license,
            mod_version            : mod_version,
            mod_authors            : mod_authors,
    ]
    inputs.properties replaceProperties
    expand replaceProperties
    from "src/main/templates"
    into "build/generated/sources/modMetadata"
}
sourceSets.main.resources.srcDir generateModMetadata
neoForge.ideSyncTask generateModMetadata

tasks.named('jar', Jar).configure {
    manifest {
        attributes([
                "Specification-Title"     : mod_id,
                "Specification-Vendor"    : mod_authors,
                "Specification-Version"   : "1",
                "Implementation-Title"    : project.name,
                "Implementation-Version"  : project.jar.archiveVersion,
                "Implementation-Vendor"   : mod_authors,
                "Implementation-Timestamp": new Date().format("yyyy-MM-dd'T'HH:mm:ssZ")
        ])
    }
}

tasks.register('sourcesJar', Jar) {
    archiveClassifier.set('sources')
    from sourceSets.main.allJava
}

publishing {
    publications {
        register('mavenJava', MavenPublication) {
            artifact jar
            artifact sourcesJar
        }
    }
    repositories {
        maven {
            url "file://${maven_loc}"
        }
    }
}

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
}
```

What was removed vs old build.gradle:
1. `buildscript { }` block (MixinGradle classpath)
2. `apply plugin: 'org.spongepowered.mixin'`
3. `net.minecraftforge.gradle` plugin
4. `mixin { }` block
5. `minecraft "net.minecraftforge:forge:..."` — replaced by `neoForge { version }`
6. `fg.deobf()` wrapping — ModDevGradle handles remapping automatically
7. NBTEdit dependency (D-07)
8. All Thermal Series dependencies (8 mods, D-07)
9. `annotationProcessor 'org.spongepowered:mixin:0.8.5:processor'` (D-05)
10. `eclipse` plugin
11. `flatDir { dir 'libs' }` (D-09)
12. `finalizedBy 'reobfJar'` (handled natively by NeoGradle)
13. Mixin JVM args from runs (`-Dmixin.config=...`, `mixin.env.remapRefMap`, etc.)
14. `property 'forge.logging.console.level'` → `logLevel = org.slf4j.event.Level.DEBUG`
15. `tasks.named('processResources', ProcessResources).configure` → `generateModMetadata` task

What was added:
- `net.neoforged.moddev` plugin v2.0.141
- `neoForge { }` block with runs, parchment mappings, mods config
- `configurations { runtimeClasspath.extendsFrom localRuntime }`
- JEI via `localRuntime` (replaces `runtimeOnly`)
- Mekanism dependency
- `generateModMetadata` task (replaces old processResources)
- `neoForge.ideSyncTask` integration
- `java-library` plugin
- NeoForge maven in settings.gradle (not build.gradle)
</action>

<acceptance_criteria>
- `build.gradle` does NOT contain `net.minecraftforge.gradle`
- `build.gradle` does NOT contain `org.spongepowered.mixin`
- `build.gradle` does NOT contain `fg.deobf`
- `build.gradle` does NOT contain `mixingradle`
- `build.gradle` does NOT contain `reobfJar`
- `build.gradle` does NOT contain `nbtedit` or `thermal` (all 8 Thermal mods removed)
- `build.gradle` contains `net.neoforged.moddev' version '2.0.141'`
- `build.gradle` contains `neo_version` (in `neoForge { version = ... }`)
- `build.gradle` contains `mezz.jei:jei-${minecraft_version}-common-api:${jei_version}`
- `build.gradle` contains `mekanism:Mekanism:${minecraft_version}-${mekanism_version}`
- `build.gradle` contains `SlashBlade_Resharped:2.0.2-1.21.1`
- `build.gradle` contains `generateModMetadata` task
- `build.gradle` contains `neoForge.ideSyncTask`
</acceptance_criteria>

</task>

### Task 5: Create neoforge.mods.toml Template

<task id="05-modstoml" type="execute" files="src/main/templates/META-INF/neoforge.mods.toml" autonomous="true">

<read_first>
- src/main/resources/META-INF/mods.toml (old file, for content reference)
- .planning/phases/01-build-and-entry/01-RESEARCH.md § 7
</read_first>

<action>
1. Create directory `src/main/templates/META-INF/` (if it doesn't exist)
2. Write `src/main/templates/META-INF/neoforge.mods.toml`:

```toml
modLoader = "javafml"
loaderVersion = "${loader_version_range}"
license = "${mod_license}"

[[mods]]
modId = "${mod_id}"
version = "${mod_version}"
displayName = "${mod_name}"
authors = "${mod_authors}"
description ='''
HF(FE Energy) SlashBlade for SlashBlade:Resharped
'''

[[dependencies.${mod_id}]]
modId = "neoforge"
type = "required"
versionRange = "${neo_version_range}"
ordering = "NONE"
side = "BOTH"

[[dependencies.${mod_id}]]
modId = "minecraft"
type = "required"
versionRange = "${minecraft_version_range}"
ordering = "NONE"
side = "BOTH"

[[dependencies.${mod_id}]]
modId = "slashblade"
type = "required"
versionRange = "[2.0.2-1.21.1,)"
ordering = "NONE"
side = "BOTH"
```

Changes from old `mods.toml`:
- Filename: `mods.toml` → `neoforge.mods.toml`
- `modId = "forge"` → `modId = "neoforge"`
- `mandatory = true` → `type = "required"`
- SlashBlade version range: `[1.3.37,)` → `[2.0.2-1.21.1,)`
- Template variables preserved exactly (expanded by `generateModMetadata` task)
</action>

<acceptance_criteria>
- `src/main/templates/META-INF/neoforge.mods.toml` exists
- Content contains `modId = "neoforge"` (NOT `"forge"`)
- Content contains `type = "required"` (NOT `mandatory = true`)
- Content contains `versionRange = "[2.0.2-1.21.1,)"` for slashblade
- Content does NOT contain `mandatory = true`
</acceptance_criteria>

</task>

### Task 6: Delete Old mods.toml

<task id="06-delete-old-toml" type="execute" files="src/main/resources/META-INF/mods.toml" autonomous="true">

<read_first>
- src/main/resources/META-INF/mods.toml (verify it exists and content matches old format)
</read_first>

<action>
Delete `src/main/resources/META-INF/mods.toml`. From now on, `neoforge.mods.toml` in `src/main/templates/` is the template source, expanded by `generateModMetadata` at build time.
</action>

<acceptance_criteria>
- `src/main/resources/META-INF/mods.toml` does NOT exist
- `src/main/templates/META-INF/neoforge.mods.toml` exists (verified from Task 5)
</acceptance_criteria>

</task>

### Task 7: Migrate Energyblade.java Entry Point

<task id="07-entrypoint" type="execute" files="src/main/java/cn/mmf/energyblade/Energyblade.java" autonomous="true">

<read_first>
- src/main/java/cn/mmf/energyblade/Energyblade.java
- .planning/phases/01-build-and-entry/01-RESEARCH.md § 6
- .planning/phases/01-build-and-entry/01-CONTEXT.md § Implementation Decisions
</read_first>

<action>
Replace the entire content of `src/main/java/cn/mmf/energyblade/Energyblade.java`:

```java
package cn.mmf.energyblade;

import cn.mmf.energyblade.item.ItemFEBlade;
import com.mojang.logging.LogUtils;
import mods.flammpfeil.slashblade.item.ItemTierSlashBlade;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Mod(Energyblade.MODID)
public class Energyblade {
    public static final String MODID = "energyblade";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, MODID);
    public static final Supplier<Item> FORGE_ENERGY_BLADE = ITEMS.register("forge_energy_blade",
            () -> new ItemFEBlade(new ItemTierSlashBlade(40, 4F), 4, -2.4F, (new Item.Properties())));

    public Energyblade(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkPacketHandler::registerMessage);
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
```

Import changes:
- `net.minecraftforge.fml.common.Mod` → `net.neoforged.fml.common.Mod`
- `net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext` → *(removed)*
- `net.minecraftforge.registries.DeferredRegister` → `net.neoforged.neoforge.registries.DeferredRegister`
- `net.minecraftforge.registries.ForgeRegistries` → `net.minecraft.core.registries.BuiltInRegistries`
- `net.minecraftforge.registries.RegistryObject` → `java.util.function.Supplier`
- `net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent` → `net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent`
- Added: `net.neoforged.bus.api.IEventBus`
- Added: `java.util.function.Supplier`

Structural changes:
- Constructor: `public Energyblade()` → `public Energyblade(IEventBus modBus)` 
- `ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus())` → `ITEMS.register(modBus)`
- `FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup)` → `modBus.addListener(this::setup)`
- `RegistryObject<Item>` → `Supplier<Item>` for `FORGE_ENERGY_BLADE` field
- `ForgeRegistries.ITEMS` → `BuiltInRegistries.ITEM` (singular)
</action>

<acceptance_criteria>
- `Energyblade.java` does NOT contain `net.minecraftforge` (no Forge imports)
- `Energyblade.java` contains `net.neoforged.fml.common.Mod`
- `Energyblade.java` contains `net.neoforged.bus.api.IEventBus`
- `Energyblade.java` contains `public Energyblade(IEventBus modBus)`
- `Energyblade.java` contains `BuiltInRegistries.ITEM`
- `Energyblade.java` contains `java.util.function.Supplier` (NOT `RegistryObject`)
- `Energyblade.java` does NOT contain `FMLJavaModLoadingContext`
- `Energyblade.java` does NOT contain `ForgeRegistries`
</acceptance_criteria>

</task>

### Task 8: Delete Mixin Artifacts

<task id="08-mixin-cleanup" type="execute" files="src/main/resources/energyblade.mixins.json" autonomous="true">

<read_first>
- src/main/resources/energyblade.mixins.json (verify empty mixin/empty client array per D-05)
</read_first>

<action>
1. Delete `src/main/resources/energyblade.mixins.json`

Note: `build.gradle` already has no MixinGradle plugin, no `mixin { }` block, no mixin annotation processor, and no mixin JVM args — these were all removed in Task 4.
</action>

<acceptance_criteria>
- `src/main/resources/energyblade.mixins.json` does NOT exist
</acceptance_criteria>

</task>

### Task 9: Verify Compilation

<task id="09-verify" type="verify" files="" autonomous="false">

<read_first>
- build.gradle (post-Task 4)
- src/main/java/cn/mmf/energyblade/Energyblade.java (post-Task 7)
</read_first>

<action>
Run the Gradle compilation:
```
./gradlew compileJava --no-daemon
```

The build must succeed with zero errors.
</action>

<acceptance_criteria>
- `./gradlew compileJava --no-daemon` exits with code 0
- No compilation errors in output
- BUILD SUCCESSFUL appears in output
</acceptance_criteria>

</task>

---

## Verification

After all tasks complete:

```bash
./gradlew compileJava --no-daemon
```

Must output: `BUILD SUCCESSFUL` with zero errors.

### Spot Checks

| # | Check | Expected |
|---|-------|----------|
| 1 | `grep -r "net.minecraftforge" src/main/java/` | Zero matches |
| 2 | `grep -r "FMLJavaModLoadingContext" src/main/java/` | Zero matches |
| 3 | `grep "modId.*forge" src/main/templates/META-INF/neoforge.mods.toml` | Zero matches |
| 4 | `test -f src/main/templates/META-INF/neoforge.mods.toml && echo OK` | OK |
| 5 | `test ! -f src/main/resources/META-INF/mods.toml && echo OK` | OK |
| 6 | `test ! -f src/main/resources/energyblade.mixins.json && echo OK` | OK |

## Success Criteria

- [x] BLD-01: Gradle build script uses NeoGradle (ModDevGradle plugin)
- [x] BLD-02: Java toolchain set to JDK 21
- [x] BLD-03: All dependencies point to NeoForge 1.21.1 equivalents
- [x] BLD-04: Mod metadata in `neoforge.mods.toml` NeoForge format
- [x] BLD-05: Mod entry point uses NeoForge bootstrap (`IEventBus` constructor injection)
- [ ] BLD-06: `./gradlew compileJava` succeeds with zero errors (verified in Task 9)

## must_haves

- `./gradlew compileJava --no-daemon` → BUILD SUCCESSFUL, zero errors
- `Energyblade.java` has zero `net.minecraftforge` imports
- `neoforge.mods.toml` has `modId = "neoforge"` (not `"forge"`)
- `build.gradle` uses `net.neoforged.moddev` plugin

## Notes

- `NetworkPacketHandler.registerMessage` is called in `setup()` — it will fail to compile in Phase 1 (it references Forge `SimpleChannel`). This is EXPECTED. Phase 4 (Networking And Sync) will resolve this.
- The `ItemFEBlade` class still has many Forge imports — those are handled in Phases 2-6. Phase 1 only targets the build system and `Energyblade.java` entry point.
- `compileJava` may produce warnings about unused imports in other files — these are fine and addressed in later phases.

## PLANNING COMPLETE
