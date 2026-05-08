---
phase: 4
plan: 04-networking-sync
type: execute
wave: 1
depends_on: []
files_modified:
  - src/main/java/cn/mmf/energyblade/NetworkPacketHandler.java (rewrite)
  - src/main/java/cn/mmf/energyblade/PowerSwitchPacket.java (rewrite)
  - src/main/java/cn/mmf/energyblade/Energyblade.java
  - src/main/java/cn/mmf/energyblade/client/InputHandler.java
autonomous: true
requirements: [NET-01, NET-02, NET-03]
---

# Plan 04-Networking-Sync: Migrate SimpleChannel to NeoForge Payload API

## Objective

Migrate the Forge `SimpleChannel` networking system to NeoForge's `CustomPacketPayload` + `PayloadRegistrar` API. Rewrite `PowerSwitchPacket` as a record, replace `NetworkPacketHandler` with event-based registration, fix packet sending in `InputHandler`, and remove the obsolete `FMLCommonSetupEvent` from `Energyblade`.

## User Story

**As a** developer, **I want to** run `./gradlew compileJava` and see zero networking import errors — `PowerSwitchPacket` compiles as a NeoForge payload, the network registration uses `RegisterPayloadHandlersEvent`, and packet sending uses `PacketDistributor`.

## Context

Phase 4 completes the core API migration surface. After this phase, all major Forge→NeoForge API gaps are resolved (build, registration, events, energy/capabilities, networking). RESEARCH.md provides exact before/after code.

## Tasks

### Task 1: Rewrite PowerSwitchPacket — Record + CustomPacketPayload + StreamCodec

<task id="01-packet" type="execute" files="src/main/java/cn/mmf/energyblade/PowerSwitchPacket.java" autonomous="true">

<read_first>
- src/main/java/cn/mmf/energyblade/PowerSwitchPacket.java
- .planning/phases/04-networking-and-sync/04-RESEARCH.md
</read_first>

<action>
Replace the entire content of `src/main/java/cn/mmf/energyblade/PowerSwitchPacket.java`:

```java
package cn.mmf.energyblade;

import cn.mmf.energyblade.energy.FEBladeStorage;
import io.netty.buffer.ByteBuf;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PowerSwitchPacket(String message) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PowerSwitchPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Energyblade.MODID, "power_switch"));

    public static final StreamCodec<ByteBuf, PowerSwitchPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    PowerSwitchPacket::message,
                    PowerSwitchPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final PowerSwitchPacket data, final IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        if (player.isSpectator())
            return;

        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getCapability(ItemSlashBlade.BLADESTATE) == null)
            return;

        var energy = mainHandItem.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energy instanceof FEBladeStorage bladeFE) {
            if (!bladeFE.isPowered()
                    && bladeFE.extractEnergy(bladeFE.getPowerupExtract(), true) == bladeFE.getPowerupExtract()) {
                bladeFE.extractEnergy(bladeFE.getPowerupExtract(), false);
                bladeFE.setPowered(true);
                ServerLevel serverLevel = player.serverLevel();
                var random = serverLevel.random;
                for (int i = 0; i < 32; ++i) {
                    double xDist = (random.nextFloat() * 2.0F - 1.0F);
                    double yDist = (random.nextFloat() * 2.0F - 1.0F);
                    double zDist = (random.nextFloat() * 2.0F - 1.0F);
                    if (!(xDist * xDist + yDist * yDist + zDist * zDist > 1.0D)) {
                        double x = player.getX(xDist / 4.0D);
                        double y = player.getY(0.5D + yDist / 4.0D);
                        double z = player.getZ(zDist / 4.0D);
                        serverLevel.sendParticles(ParticleTypes.PORTAL, x, y, z, 0, xDist, yDist + 0.2D, zDist, 1);
                    }
                }
                player.playNotifySound(SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 2.5F, 1F);
            } else {
                bladeFE.setPowered(false);
                player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1F, 1F);
            }
        }
    }
}
```

Changes:
- `class` → `record`
- Implements `CustomPacketPayload` with `type()` method
- `StreamCodec.composite` replaces constructor(FriendlyByteBuf) + toBytes(FriendlyByteBuf)
- Handler: `static void handle(PowerSwitchPacket data, IPayloadContext context)` replaces `handle(Supplier<NetworkEvent.Context>)`
- `context.player()` replaces `ctx.get().getSender()`
- No `enqueueWork()` / `setPacketHandled()` needed
- `ForgeCapabilities.ENERGY` → `Capabilities.EnergyStorage.ITEM`
- `getCapability(...).isPresent()` → `getCapability(...) == null`
</action>

<acceptance_criteria>
- `PowerSwitchPacket.java` is a `record`, not a `class`
- `PowerSwitchPacket.java` implements `CustomPacketPayload`
- `PowerSwitchPacket.java` contains `TYPE` with `ResourceLocation.fromNamespaceAndPath(Energyblade.MODID, "power_switch")`
- `PowerSwitchPacket.java` contains `STREAM_CODEC` using `StreamCodec.composite`
- `PowerSwitchPacket.java` does NOT contain `FriendlyByteBuf` or `toBytes`
- `PowerSwitchPacket.java` does NOT contain `Supplier<NetworkEvent.Context>` or `setPacketHandled`
- `PowerSwitchPacket.java` does NOT contain `ForgeCapabilities`
- `PowerSwitchPacket.java` does NOT contain `enqueueWork`
- Handler signature: `public static void handle(final PowerSwitchPacket data, final IPayloadContext context)`
</acceptance_criteria>

</task>

### Task 2: Rewrite NetworkPacketHandler — PayloadRegistrar + RegisterPayloadHandlersEvent

<task id="02-handler" type="execute" files="src/main/java/cn/mmf/energyblade/NetworkPacketHandler.java" autonomous="true">

<read_first>
- src/main/java/cn/mmf/energyblade/NetworkPacketHandler.java
- .planning/phases/04-networking-and-sync/04-RESEARCH.md
</read_first>

<action>
Replace the entire content of `src/main/java/cn/mmf/energyblade/NetworkPacketHandler.java`:

```java
package cn.mmf.energyblade;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Energyblade.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkPacketHandler {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                PowerSwitchPacket.TYPE,
                PowerSwitchPacket.STREAM_CODEC,
                PowerSwitchPacket::handle
        );
    }

    public static void sendToClientPlayer(PowerSwitchPacket message, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, message);
        }
    }
}
```

Changes:
- `SimpleChannel INSTANCE` + `PROTOCOL_VERSION` + `ID` + `nextID()` → removed
- `registerMessage()` → `@SubscribeEvent` on `RegisterPayloadHandlersEvent`
- `NetworkRegistry.newSimpleChannel` → `event.registrar("1")`
- `.messageBuilder(...).encoder(...).decoder(...).consumerNetworkThread(...)` → `registrar.playToServer(TYPE, STREAM_CODEC, handler)`
- `INSTANCE.send(PacketDistributor.PLAYER.with(...), msg)` → `PacketDistributor.sendToPlayer(serverPlayer, message)`
</action>

<acceptance_criteria>
- `NetworkPacketHandler.java` does NOT contain `SimpleChannel`
- `NetworkPacketHandler.java` does NOT contain `NetworkRegistry`
- `NetworkPacketHandler.java` does NOT contain `NetworkDirection`
- `NetworkPacketHandler.java` does NOT contain `messageBuilder` or `encoder` or `decoder`
- `NetworkPacketHandler.java` contains `@EventBusSubscriber(modid = Energyblade.MODID, bus = EventBusSubscriber.Bus.MOD)`
- `NetworkPacketHandler.java` contains `@SubscribeEvent` on `register(RegisterPayloadHandlersEvent)`
- `NetworkPacketHandler.java` contains `registrar.playToServer`
- `NetworkPacketHandler.java` contains `PacketDistributor.sendToPlayer`
- `NetworkPacketHandler.java` contains `PowerSwitchPacket.TYPE`
- `NetworkPacketHandler.java` contains `PowerSwitchPacket.STREAM_CODEC`
</acceptance_criteria>

</task>

### Task 3: Update Energyblade.java — Remove FMLCommonSetupEvent + setup()

<task id="03-energyblade" type="execute" files="src/main/java/cn/mmf/energyblade/Energyblade.java" autonomous="true">

<read_first>
- src/main/java/cn/mmf/energyblade/Energyblade.java
</read_first>

<action>
In `src/main/java/cn/mmf/energyblade/Energyblade.java`:

1. Remove `import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;`
2. Remove `modBus.addListener(this::setup);` from constructor
3. Remove the entire `setup()` method
4. Remove `import net.neoforged.bus.api.SubscribeEvent;` if it was only needed for `setup`

The constructor becomes:
```java
public Energyblade(IEventBus modBus) {
    ITEMS.register(modBus);
    DATA_COMPONENTS.register(modBus);
    modBus.addListener(this::registerCapabilities);
}
```
</action>

<acceptance_criteria>
- `Energyblade.java` does NOT contain `FMLCommonSetupEvent`
- `Energyblade.java` does NOT contain `modBus.addListener(this::setup)`
- `Energyblade.java` does NOT contain `private void setup(`
- `Energyblade.java` constructor registers ITEMS, DATA_COMPONENTS, and registerCapabilities only
</acceptance_criteria>

</task>

### Task 4: Update InputHandler.java — Fix packet sending

<task id="04-input" type="execute" files="src/main/java/cn/mmf/energyblade/client/InputHandler.java" autonomous="true">

<read_first>
- src/main/java/cn/mmf/energyblade/client/InputHandler.java
</read_first>

<action>
In `src/main/java/cn/mmf/energyblade/client/InputHandler.java`:

1. Add import: `import net.neoforged.neoforge.network.PacketDistributor;`
2. Find and replace the old sending pattern with the new one:

Old: `NetworkPacketHandler.INSTANCE.sendToServer(new PowerSwitchPacket("triggered"));`
New: `PacketDistributor.sendToServer(new PowerSwitchPacket("triggered"));`
</action>

<acceptance_criteria>
- `InputHandler.java` contains `PacketDistributor.sendToServer`
- `InputHandler.java` does NOT contain `NetworkPacketHandler.INSTANCE`
</acceptance_criteria>

</task>

### Task 5: Verify Compilation

<task id="05-verify" type="verify" files="" autonomous="true">

<read_first>
- build.gradle
</read_first>

<action>
Run: `./gradlew compileJava --no-daemon`

Expected: zero networking-related errors. The errors that remain should be only SlashBlade/Forge API references in deferred-phase files.
</action>

<acceptance_criteria>
- `./gradlew compileJava --no-daemon` executes
- No `SimpleChannel` or `NetworkRegistry` errors
- No `Supplier<NetworkEvent>` errors
- No `ForgeCapabilities` errors (resolved in Phase 3)
</acceptance_criteria>

</task>

---

## Verification

```bash
./gradlew compileJava --no-daemon
```

### Spot Checks

| # | Check | Expected |
|---|-------|----------|
| 1 | `grep -rn "SimpleChannel" src/main/java/` | Zero matches |
| 2 | `grep -rn "NetworkRegistry" src/main/java/` | Zero matches |
| 3 | `grep -rn "NetworkDirection" src/main/java/` | Zero matches |
| 4 | `grep -rn "Supplier<NetworkEvent" src/main/java/` | Zero matches |
| 5 | `grep -rn "FMLCommonSetupEvent" src/main/java/` | Zero matches |
| 6 | `grep "CustomPacketPayload" src/main/java/cn/mmf/energyblade/PowerSwitchPacket.java` | Found |
| 7 | `grep "RegisterPayloadHandlersEvent" src/main/java/cn/mmf/energyblade/NetworkPacketHandler.java` | Found |
| 8 | `grep "PacketDistributor.sendToServer" src/main/java/cn/mmf/energyblade/client/InputHandler.java` | Found |

## Success Criteria

- [ ] NET-01: PowerSwitchPacket implements CustomPacketPayload with StreamCodec
- [ ] NET-02: Network registration uses RegisterPayloadHandlersEvent + PayloadRegistrar
- [ ] NET-03: Packet sending uses PacketDistributor (no SimpleChannel instance)

## must_haves

- Zero `SimpleChannel` references in codebase
- Zero `NetworkRegistry` references in codebase
- `PowerSwitchPacket` is a `record` implementing `CustomPacketPayload`
- `NetworkPacketHandler` uses `@SubscribeEvent` on `RegisterPayloadHandlersEvent`
- Zero `FMLCommonSetupEvent` in Energyblade.java

## Notes

- `PowerSwitchPacket` accesses `ItemSlashBlade.BLADESTATE` which is SlashBlade's capability — this will fail to compile without SlashBlade on classpath, but the code structure is correct
- `IPayloadContext` runs on main thread by default for `playToServer` — no `enqueueWork` needed
- `StreamCodec.STRING_UTF8` handles the single String field — replaces manual `readUtf`/`writeUtf`

## PLANNING COMPLETE
