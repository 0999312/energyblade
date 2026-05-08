# CONVENTIONS.md

## Code Style

### Formatting
- **Indentation**: Tabs (visible throughout all Java files)
- **Braces**: Same-line opening brace (K&R style)
- **Line endings**: Mixed CRLF (Windows development)
- **File encoding**: UTF-8 (`build.gradle:178`)
- **No trailing whitespace enforcement** visible

### Imports
- No wildcard imports — all imports are explicit
- Import order: `com.mojang` → internal packages → `net.minecraft` → `net.minecraftforge` → third-party → `java.*` / `javax.*`

### Naming
- **Classes**: PascalCase (`ItemFEBlade`, `EnergyBladeBEWLR`, `FECapabilityProvider`)
- **Methods**: camelCase (`getEnergyStored`, `canReceive`, `isPowered`)
- **Constants**: UPPER_SNAKE_CASE (`MODID`, `INSTANCE`, `PROTOCOL_VERSION`, `MAX_BAR_WIDTH`)
- **Fields**: camelCase (no Hungarian notation or `m_` prefix)
- **Parameters**: camelCase, sometimes abbreviated (`p_172550_` for Mojang-mapped parameters in overrides)

### Comments
- Chinese comments for domain logic (`// FE能量拔刀剑`, `// 拔刀剑：重锋(前置)`)
- English comments for mechanical notes (`// compile against the JEI API but do not include it at runtime`)
- No Javadoc on any method
- No package-info.java files

## Patterns

### Registration Pattern
```java
public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
public static final RegistryObject<Item> FORGE_ENERGY_BLADE = ITEMS.register("forge_energy_blade",
    () -> new ItemFEBlade(...));
// Registered in constructor: ITEMS.register(eventBus);
```
Standard Forge `DeferredRegister` pattern. Registered in mod constructor on the mod event bus.

### Event Subscriber Pattern
Two variants used:
1. **Static inner class** — `@Mod.EventBusSubscriber` on class, `@SubscribeEvent` on static methods (`ClientSetupHandler:18`, `DataGen:17`, `ItemFEBlade:38`)
2. **Instance method** — `@Mod` constructor with lambda registration (`Energyblade.java:28`)

### Capability Pattern
```java
public class FECapabilityProvider extends NamedBladeStateCapabilityProvider {
    private final LazyOptional<IEnergyStorage> lazyOptional;
    // getCapability() dispatches: ENERGY → lazyOptional, BLADESTATE → super
    // serializeNBT/deserializeNBT delegate to both super and energyStorage
}
```
Extended from parent mod's capability provider. Uses `LazyOptional` with manual NBT serialization.

### Network Packet Pattern
```java
// Registration (FMLCommonSetupEvent enqueueWork):
INSTANCE = NetworkRegistry.newSimpleChannel(...);
INSTANCE.messageBuilder(PowerSwitchPacket.class, nextID(), PLAY_TO_SERVER)
    .encoder(...).decoder(...).consumerNetworkThread(...).add();

// Usage:
NetworkPacketHandler.INSTANCE.sendToServer(new PowerSwitchPacket("triggered"));
```
SimpleChannel with sequential ID counter. `PLAY_TO_SERVER` direction only.

### Client Item Extension Pattern
```java
@Override
public void initializeClient(Consumer<IClientItemExtensions> consumer) {
    consumer.accept(new IClientItemExtensions() {
        // Override getCustomRenderer() to return custom BEWLR
    });
}
```
Forge's `IClientItemExtensions` interface for custom item rendering.

## Error Handling
- No try-catch blocks anywhere in the codebase
- No exception propagation strategy
- No validation of external inputs beyond null checks
- `@Nullable` annotations from `javax.annotation` used on override methods
- `if (player == null)` guard checks present
- `isSpectator()` check in packet handler prevents spectator NPE

## State Management
- Item state stored in NBT on `ItemStack` via `CompoundTag`
- Energy data persisted through `serializeNBT()` / `deserializeNBT()` on both `FEBladeStorage` and `FECapabilityProvider`
- `getShareTag()` / `readShareTag()` for client-server sync
- No custom save data format — uses standard Minecraft NBT compound tags

## Logging
- SLF4J logger via `com.mojang.logging.LogUtils.getLogger()` (`src/main/java/cn/mmf/energyblade/Energyblade.java:19`)
- Logger is declared but never actually used in the codebase (no `LOGGER.info/warn/error` calls)

## Dependency Management
- Local JARs via `flatDir` in `libs/` directory
- CurseMaven for mod dependencies (`curse.maven:` notation)
- MMMaven (custom maven) for SlashBlade API artifacts
- ModMaven as JEI fallback mirror
- `fg.deobf()` wrapping for Forge-mod dependencies
