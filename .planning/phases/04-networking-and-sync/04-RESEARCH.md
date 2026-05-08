# Phase 4: Networking And Sync — Research

**Researched:** 2026-05-08
**Status:** Complete

## Key Architecture Change

Forge's `SimpleChannel` + `NetworkRegistry` is completely replaced by NeoForge's `PayloadRegistrar` + `CustomPacketPayload` system. No channel instance needed — `PacketDistributor` routes directly by payload type.

## 1. Registration: `NetworkRegistry.newSimpleChannel` → `RegisterPayloadHandlersEvent`

| Aspect | Forge 1.20.1 | NeoForge 1.21.1 |
|---|---|---|
| Entry point | `NetworkRegistry.newSimpleChannel(...)` → `SimpleChannel` instance | `@SubscribeEvent` on `RegisterPayloadHandlersEvent` → `event.registrar("1")` |
| Channel instance | `public static SimpleChannel INSTANCE` | **Not needed** — `PacketDistributor` routes by type |
| Protocol version | `PROTOCOL_VERSION` string with lambda comparison | `event.registrar("1")` string param |
| Message registration | `.messageBuilder(...).encoder(...).decoder(...).consumerNetworkThread(...).add()` | `registrar.playToServer(TYPE, STREAM_CODEC, handler)` |
| Direction | `NetworkDirection.PLAY_TO_SERVER` | `playToServer()` / `playToClient()` / `playBidirectional()` |
| ID counter | `private static int ID` + `nextID()` | **Not needed** — Type's `ResourceLocation` is unique ID |

## 2. Payload: Class → Record implementing `CustomPacketPayload`

PowerSwitchPacket becomes a `record` implementing `CustomPacketPayload`:

```java
public record PowerSwitchPacket(String message) implements CustomPacketPayload {
    public static final Type<PowerSwitchPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Energyblade.MODID, "power_switch"));
    
    public static final StreamCodec<ByteBuf, PowerSwitchPacket> STREAM_CODEC =
        StreamCodec.composite(ByteBufCodecs.STRING_UTF8, PowerSwitchPacket::message, PowerSwitchPacket::new);
    
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    
    public static void handle(PowerSwitchPacket data, IPayloadContext context) { ... }
}
```

## 3. StreamCodec: Replaces manual encode/decode

`StreamCodec.composite` with `ByteBufCodecs.STRING_UTF8` replaces `toBytes(FriendlyByteBuf)` / constructor `PowerSwitchPacket(FriendlyByteBuf)`. `FriendlyByteBuf` still exists but StreamCodec uses `ByteBuf`.

## 4. Handler: `IPayloadContext` replaces `Supplier<NetworkEvent.Context>`

| Old | New |
|---|---|
| `ctx.get().getSender()` | `context.player()` (cast to ServerPlayer) |
| `ctx.get().enqueueWork(() -> {...})` | **Not needed** — `playToServer` runs on main thread by default |
| `ctx.get().setPacketHandled(true)` | **Not needed** |

## 5. Sending: No channel instance needed

| Old | New |
|---|---|
| `INSTANCE.sendToServer(packet)` | `PacketDistributor.sendToServer(packet)` |
| `INSTANCE.send(PacketDistributor.PLAYER.with(...), msg)` | `PacketDistributor.sendToPlayer(serverPlayer, payload)` |

## 6. Energyblade.java: Remove `setup()` + `FMLCommonSetupEvent`

`registerMessage()` is no longer called from `setup()` — `NetworkPacketHandler` uses `@EventBusSubscriber` + `@SubscribeEvent` to auto-register on `RegisterPayloadHandlersEvent`.

## 7. PowerSwitchPacket: Fix capability queries

`ForgeCapabilities.ENERGY` → `Capabilities.EnergyStorage.ITEM`; `.isPresent()` → `== null` check.

## 8. Files Modified

| File | Changes |
|---|---|
| `NetworkPacketHandler.java` | Complete rewrite: SimpleChannel → PayloadRegistrar |
| `PowerSwitchPacket.java` | Complete rewrite: class → record, CustomPacketPayload, StreamCodec |
| `Energyblade.java` | Remove `setup()`, `FMLCommonSetupEvent` import, `modBus.addListener(this::setup)` |
| `InputHandler.java` | `INSTANCE.sendToServer(...)` → `PacketDistributor.sendToServer(...)` |

## 9. New LAM Entries

| ID | Old | New |
|---|---|---|
| LAM-37 | `NetworkRegistry.newSimpleChannel` | `RegisterPayloadHandlersEvent` + `event.registrar()` |
| LAM-38 | `SimpleChannel` instance | Removed — `PacketDistributor` routes by payload type |
| LAM-39 | `NetworkDirection.PLAY_TO_SERVER` | `registrar.playToServer()` |
| LAM-40 | `Supplier<NetworkEvent.Context>` handler | `IPayloadContext` — no enqueueWork needed |
| LAM-41 | `PacketDistributor.PLAYER.with(...)` | `PacketDistributor.sendToPlayer(serverPlayer, payload)` |
| LAM-42 | `FriendlyByteBuf` encode/decode | `StreamCodec<ByteBuf, T>` with ByteBufCodecs |
| LAM-43 | `CustomPacketPayload.Type` | N/A — new in NeoForge |
| LAM-44 | `IPayloadContext` | N/A — new in NeoForge; replaces `NetworkEvent.Context` |

---

*Phase: 4-Networking And Sync*
*Research completed: 2026-05-08*
