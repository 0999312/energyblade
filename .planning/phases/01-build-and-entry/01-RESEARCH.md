# Phase 1: Build And Entry — Research

**Researched:** 2026-05-08
**Status:** Complete

## 1. NeoForge 1.21.1 Version & MDK

### Core Versions

| Property | Value | Source |
|---|---|---|
| Minecraft | `1.21.1` | |
| NeoForge | `21.1.228` | NeoForge Maven releases (latest `21.1.x` stable) |
| NeoGradle Plugin | `net.neoforged.moddev` version `2.0.141` | Gradle Plugin Portal |
| Gradle Wrapper | `8.13` (minimum `8.8+`; MDK ships `9.2.1`) | MDK `gradle-wrapper.properties` |
| Java Toolchain | JDK 21 | Minecraft 1.21+ ships Java 21 to users |
| Parchment mappings | `2024.11.10` for `1.21.1` | ParchmentMC |

### NeoForge Maven

- **NeoForge releases:** `https://maven.neoforged.net/releases/net/neoforged/neoforge/`
- **ModDevGradle plugin:** Published to Gradle Plugin Portal (NOT NeoForge Maven)

---

## 2. build.gradle — Complete Rewrite

### 2.1 Plugin Declaration

```groovy
plugins {
    id 'java-library'
    id 'maven-publish'
    id 'net.neoforged.moddev' version '2.0.141'
    id 'idea'
}
```

**Removed:** `net.minecraftforge.gradle`, `org.spongepowered.mixin`, `eclipse`, `buildscript { }` block entirely.

### 2.2 neoForge Block (replaces minecraft { })

```groovy
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
```

### 2.3 Dependencies (Removals & Additions)

**Removed (entirely):**
- `minecraft "net.minecraftforge:forge:..."` — handled by `neoForge { version }`
- `fg.deobf()` wrapping — ModDevGradle remaps automatically
- `buildscript { dependencies { classpath mixingradle } }`
- `apply plugin: 'org.spongepowered.mixin'`
- NBTEdit Reborn (`curse.maven:nbtedit-reborn-678133:5984630`)
- All Thermal Series mods (CoFH Core, CodeChicken Lib, Thermal Foundation/Expansion/Innovation/Locomotion/Integration/Dynamics)

**Added:**
- `implementation "mods.flammpfeil.slashblade:SlashBlade_Resharped:2.0.2-1.21.1"` (D-10)
- `implementation "mekanism:Mekanism:1.21.1-10.7.19.85"` (D-08)
- JEI updated to `19.27.0.340` (D-06)

**Kept:**
- `annotationProcessor 'org.spongepowered:mixin:0.8.5:processor'` (only if mixins used; per D-05, this is removed too)

### 2.4 processResources → generateModMetadata

```groovy
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
```

Template files move from `src/main/resources/META-INF/mods.toml` → `src/main/templates/META-INF/neoforge.mods.toml`.

### 2.5 Jar Manifest

```groovy
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
    // NO finalizedBy 'reobfJar' — ModDevGradle handles obfuscation
}
```

### 2.6 Publishing

```groovy
publishing {
    publications {
        register('mavenJava', MavenPublication) {
            from components.java
        }
    }
    repositories {
        maven {
            url "file://${project.projectDir}/repo"
        }
    }
}
```

### 2.7 localRuntime Configuration

```groovy
configurations {
    runtimeClasspath.extendsFrom localRuntime
}
```

---

## 3. settings.gradle

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

**Key change:** MinecraftForge maven removed. NeoForge maven added for dependency lookup (NeoForge itself, not the plugin).

---

## 4. gradle.properties — Updated Properties

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

**Removed:** `forge_version`, `forge_version_range`, `mapping_channel`, `mapping_version`.

---

## 5. gradle-wrapper.properties

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

From `gradle-8.6` → `gradle-8.13` (ModDevGradle requires `8.8+`).

---

## 6. @Mod Entry Point Migration

### Import Changes

| Old (Forge 1.20.1) | New (NeoForge 1.21.1) |
|---|---|
| `net.minecraftforge.fml.common.Mod` | `net.neoforged.fml.common.Mod` |
| `net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext` | *(removed)* |
| `net.minecraftforge.registries.DeferredRegister` | `net.neoforged.neoforge.registries.DeferredRegister` |
| `net.minecraftforge.registries.ForgeRegistries` | `net.minecraft.core.registries.BuiltInRegistries` |
| `net.minecraftforge.registries.RegistryObject` | `java.util.function.Supplier` |
| `net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent` | `net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent` |

### Structural Change: Constructor Injection

`FMLJavaModLoadingContext` is removed. The mod event bus (`IEventBus`) is **injected as a constructor parameter**.

```java
// Forge 1.20.1
public Energyblade() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
}

// NeoForge 1.21.1
public Energyblade(IEventBus modBus) {
    ITEMS.register(modBus);
    modBus.addListener(this::setup);
}
```

### Registration: `BuiltInRegistries.ITEM`

For vanilla registries (items, blocks, etc.), use `net.minecraft.core.registries.BuiltInRegistries` instead of `ForgeRegistries`.

Note: Vanilla 1.21.x renamed `BuiltInRegistries.ITEMS` → `BuiltInRegistries.ITEM` (singular).

### Complete Target Code

```java
package cn.mmf.energyblade;

import com.mojang.logging.LogUtils;
import cn.mmf.energyblade.item.ItemFEBlade;
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

---

## 7. mods.toml → neoforge.mods.toml

### Format Changes

| Old (Forge 1.20.1) | New (NeoForge 1.21.1) |
|---|---|
| Filename: `META-INF/mods.toml` | `META-INF/neoforge.mods.toml` |
| `modLoader = "javafml"` | `modLoader = "javafml"` (unchanged) |
| Dependency on `"forge"` | Dependency on `"neoforge"` |
| `mandatory = true` | `type = "required"` |
| `mandatory = false` | `type = "optional"` |

### Target Content

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

---

## 8. Mixin Removal (D-05)

| Action | Detail |
|---|---|
| Delete file | `src/main/resources/energyblade.mixins.json` |
| Remove from `build.gradle` | `buildscript { }` block, `apply plugin: 'org.spongepowered.mixin'`, `mixin { }` block, `annotationProcessor 'org.spongepowered:mixin:0.8.5:processor'` |
| Remove JVM args from runs | `arg "-mixin.config=...`, `property 'mixin.env.*'` |

---

## 9. Dependency Coordinates

### JEI (D-06)
| Maven Coordinate | Version |
|---|---|
| `mezz.jei:jei-1.21.1-common-api` | `19.27.0.340` |
| `mezz.jei:jei-1.21.1-neoforge-api` | `19.27.0.340` |
| `mezz.jei:jei-1.21.1-neoforge` | `19.27.0.340` |

### Mekanism (D-08)
`mekanism:Mekanism:1.21.1-10.7.19.85` (`implementation`)

### SlashBlade (D-10)
`mods.flammpfeil.slashblade:SlashBlade_Resharped:2.0.2-1.21.1` (`implementation`)

---

## 10. Repository Configuration

```groovy
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
```

**Removed:** `flatDir { dir 'libs' }` (D-09).

---

## 11. Files to Delete

| File | Reason |
|---|---|
| `src/main/resources/energyblade.mixins.json` | D-05: Empty mixin config, no active mixins |
| `libs/` directory references | D-09: flatDir removed |

## 12. Files to Create

| New File | Based On |
|---|---|
| `src/main/templates/META-INF/neoforge.mods.toml` | Old `src/main/resources/META-INF/mods.toml` |

## 13. Files to Modify

| File | Nature of Change |
|---|---|
| `build.gradle` | Complete rewrite: plugin, neoForge block, deps, processResources, jar, publishing |
| `settings.gradle` | Remove MinecraftForge maven, add NeoForge maven, bump foojay-resolver |
| `gradle.properties` | Replace forge_* with neo_*, update versions, add parchment |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle 8.6 → 8.13 |
| `src/main/java/cn/mmf/energyblade/Energyblade.java` | Import changes + constructor injection |

---

## 14. LOADER_API_MAP Entries to Record

| ID | Old Forge API | Usage | NeoForge 1.21.1 Replacement | Evidence |
|---|---|---|---|---|
| LAM-01 | `net.minecraftforge.fml.common.Mod` | Mod entry annotation | `net.neoforged.fml.common.Mod` | docs.neoforged.net |
| LAM-02 | `FMLJavaModLoadingContext.get().getModEventBus()` | Get mod event bus | `IEventBus` constructor parameter | docs.neoforged.net |
| LAM-03 | `net.minecraftforge.registries.DeferredRegister` | Deferred registration | `net.neoforged.neoforge.registries.DeferredRegister` | docs.neoforged.net |
| LAM-04 | `net.minecraftforge.registries.ForgeRegistries` | Registry keys | `net.minecraft.core.registries.BuiltInRegistries` | docs.neoforged.net |
| LAM-05 | `ForgeRegistries.ITEMS` | Item registry key | `BuiltInRegistries.ITEM` (singular) | docs.neoforged.net |
| LAM-06 | `net.minecraftforge.registries.RegistryObject<T>` | Holder wrapper | `java.util.function.Supplier<T>` | NeoForge convention |
| LAM-07 | `net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent` | Setup event | `net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent` | docs.neoforged.net |
| LAM-08 | `mods.toml` → `modLoader = "javafml"` | Mod metadata | `neoforge.mods.toml` (same `modLoader`) | docs.neoforged.net |
| LAM-09 | `[[dependencies]] modId = "forge"` | NeoForge dependency | `modId = "neoforge"` | docs.neoforged.net |
| LAM-10 | `mandatory = true` in `mods.toml` | Required dep | `type = "required"` | docs.neoforged.net |
| LAM-11 | `net.minecraftforge.gradle` plugin | Build plugin | `net.neoforged.moddev` v2.0.141 | MDK repo |
| LAM-12 | `fg.deobf()` dependency wrapping | Remap handling | Removed entirely (automatic) | MDK repo |

---

## 15. Evidence Sources

1. **NeoForge Docs — Mod Files:** https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles/
2. **NeoForge Docs — Registries:** https://docs.neoforged.net/docs/1.21.1/concepts/registries/
3. **NeoForge Docs — Events:** https://docs.neoforged.net/docs/1.21.1/concepts/events/
4. **NeoForge Docs — Items:** https://docs.neoforged.net/docs/1.21.1/items/
5. **NeoForge MDK (ModDevGradle):** https://github.com/NeoForgeMDKs/MDK-1.21-ModDevGradle
6. **ModDevGradle README:** https://github.com/neoforged/ModDevGradle
7. **NeoForge Maven:** https://maven.neoforged.net/releases/net/neoforged/neoforge/
8. **JEI Maven metadata:** https://maven.blamejared.com/mezz/jei/
9. **Mekanism Maven metadata:** https://modmaven.dev/mekanism/Mekanism/maven-metadata.xml

---

*Phase: 1-Build And Entry*
*Research completed: 2026-05-08*
