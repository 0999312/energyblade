---
phase: 4
plan: 04-networking-sync
subsystem: networking
tags: [networking, payload, SimpleChannel, CustomPacketPayload, StreamCodec, PacketDistributor]
key-files:
  modified:
    - src/main/java/cn/mmf/energyblade/PowerSwitchPacket.java
    - src/main/java/cn/mmf/energyblade/NetworkPacketHandler.java
    - src/main/java/cn/mmf/energyblade/Energyblade.java
    - src/main/java/cn/mmf/energyblade/client/InputHandler.java
metrics:
  tasks_total: 5
  tasks_completed: 5
  commits: 4
  files_changed: 4
---

# Summary: 04-Networking-Sync

**One-liner:** Migrated Forge SimpleChannel to NeoForge CustomPacketPayload + PayloadRegistrar; PowerSwitchPacket rewritten as record with StreamCodec; FMLCommonSetupEvent removed from Energyblade.

## What Was Built

1. **PowerSwitchPacket.java** — `class` → `record` implementing `CustomPacketPayload`; `StreamCodec.composite(ByteBufCodecs.STRING_UTF8)` replaces manual `toBytes`/decoder; handler uses `IPayloadContext` (no `enqueueWork`/`setPacketHandled`); `ForgeCapabilities` → `Capabilities.EnergyStorage.ITEM`
2. **NetworkPacketHandler.java** — `SimpleChannel` + `NetworkRegistry` → `@EventBusSubscriber` + `@SubscribeEvent` on `RegisterPayloadHandlersEvent`; `registrar.playToServer(TYPE, STREAM_CODEC, handler)`; `PacketDistributor.sendToPlayer` replaces `INSTANCE.send`
3. **Energyblade.java** — Removed `FMLCommonSetupEvent` import, `setup()` method, `modBus.addListener(this::setup)` 
4. **InputHandler.java** — `NetworkPacketHandler.INSTANCE.sendToServer(...)` → `PacketDistributor.sendToServer(...)`
5. **compileJava** — Zero networking errors

## Verification

- Zero `SimpleChannel` references in codebase ✓
- Zero `NetworkRegistry` references in codebase ✓
- Zero `NetworkDirection` references in codebase ✓
- Zero `FMLCommonSetupEvent` in Energyblade ✓
- `CustomPacketPayload` implemented by PowerSwitchPacket ✓

## Deviations

None.

## Requirements Status

| ID | Requirement | Status |
|----|-------------|--------|
| NET-01 | PowerSwitchPacket implements CustomPacketPayload with StreamCodec | ✓ |
| NET-02 | Network registration via RegisterPayloadHandlersEvent + PayloadRegistrar | ✓ |
| NET-03 | Packet sending uses PacketDistributor (no SimpleChannel instance) | ✓ |

## Self-Check: PASSED

- [x] All 5 tasks executed
- [x] Zero SimpleChannel/NetworkRegistry/NetworkDirection
- [x] PowerSwitchPacket is a record implementing CustomPacketPayload
- [x] NetworkPacketHandler uses @SubscribeEvent on RegisterPayloadHandlersEvent
- [x] FMLCommonSetupEvent removed from Energyblade
- [x] InputHandler uses PacketDistributor.sendToServer
