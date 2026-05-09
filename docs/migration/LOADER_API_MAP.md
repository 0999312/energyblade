# LOADER_API_MAP

## 说明

记录已经确认的 `Forge -> NeoForge 1.21.1` loader API 映射。
只有在查询得到明确证据后才能写入 `Confirmed`。

## 条目格式

| ID | 旧 Forge API / 模式 | 使用意图 | NeoForge 1.21.1 替换方案 | 证据 | 状态 | 备注 |
|---|---|---|---|---|---|---|

## Confirmed

| ID | 旧 Forge API / 模式 | 使用意图 | NeoForge 1.21.1 替换方案 | 证据 | 状态 | 备注 |
|---|---|---|---|---|---|---|
| LAM-01 | `net.minecraftforge.fml.common.Mod` | Mod 入口注解 | `net.neoforged.fml.common.Mod` | docs.neoforged.net | Confirmed | 注解用法相同 `@Mod("modid")` |
| LAM-02 | `FMLJavaModLoadingContext.get().getModEventBus()` | 获取 Mod 事件总线 | `IEventBus` 构造函数参数注入 | docs.neoforged.net § ModFiles | Confirmed | `FMLJavaModLoadingContext` 已完全移除 |
| LAM-03 | `net.minecraftforge.registries.DeferredRegister` | 延迟注册 | `net.neoforged.neoforge.registries.DeferredRegister` | docs.neoforged.net § Registries | Confirmed | 提供 `DeferredRegister.Items` 等特化辅助类 |
| LAM-04 | `net.minecraftforge.registries.ForgeRegistries` | Forge 注册表键 | `net.minecraft.core.registries.BuiltInRegistries` (原版) / `NeoForgeRegistries` (NeoForge) | docs.neoforged.net § Registries | Confirmed | Item/Block 等原版注册表用 BuiltInRegistries |
| LAM-05 | `ForgeRegistries.ITEMS` | 物品注册表键 | `BuiltInRegistries.ITEM` (单数) | docs.neoforged.net § Registries | Confirmed | 1.21.x 重命名为 `ITEM`/`BLOCK` (单数) |
| LAM-06 | `net.minecraftforge.registries.RegistryObject<T>` | 注册项持有包装 | `java.util.function.Supplier<T>` | NeoForge 惯例 | Confirmed | `RegistryObject` 仍然存在但推荐 `Supplier` |
| LAM-07 | `net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent` | 通用设置事件 | `net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent` | docs.neoforged.net § Events | Confirmed | `enqueueWork()` API 不变 |
| LAM-08 | `META-INF/mods.toml` → `modLoader = "javafml"` | Mod 元数据 | `META-INF/neoforge.mods.toml` (modLoader 不变) | docs.neoforged.net § ModFiles | Confirmed | 文件名变更，内容兼容 |
| LAM-09 | `[[dependencies]] modId = "forge"` | Forge 依赖声明 | `modId = "neoforge"` | docs.neoforged.net § ModFiles | Confirmed | |
| LAM-10 | `mandatory = true` / `false` | 必需/可选依赖标记 | `type = "required"` / `"optional"` / `"incompatible"` / `"discouraged"` | docs.neoforged.net § Dependency Configurations | Confirmed | |
| LAM-11 | `net.minecraftforge.gradle` + `minecraft {}` DSL | ForgeGradle 构建插件 | `net.neoforged.moddev` v2.0.141 + `neoForge {}` DSL | NeoForge MDK GitHub | Confirmed | 用户指定使用 moddev 插件 |
| LAM-12 | `fg.deobf()` 依赖包装 | 依赖混淆映射处理 | 完全移除 — ModDevGradle 自动处理重映射 | NeoForge MDK GitHub | Confirmed | 直接用 `implementation`/`compileOnly` |
| LAM-13 | `net.minecraftforge.api.distmarker.Dist` | 客户端/服务端区分 | `net.neoforged.api.distmarker.Dist` | docs.neoforged.net § Sides | Confirmed | `Dist.CLIENT` 不变 |
| LAM-14 | `net.minecraftforge.api.distmarker.OnlyIn` | 仅客户端代码标记 | `net.neoforged.api.distmarker.OnlyIn` | docs.neoforged.net § Sides | Confirmed | |
| LAM-15 | `net.minecraftforge.fml.common.Mod.EventBusSubscriber` (内部类) | 自动注册事件监听器 | `net.neoforged.fml.common.EventBusSubscriber` (顶层类) | docs.neoforged.net § Events | Confirmed | **必须添加 `modid` 参数** |
| LAM-16 | `net.minecraftforge.eventbus.api.SubscribeEvent` | 事件处理器注解 | `net.neoforged.bus.api.SubscribeEvent` | docs.neoforged.net § Events | Confirmed | |
| LAM-17 | `net.minecraftforge.client.event.ModelEvent` | 模型加载/烘焙事件 | `net.neoforged.neoforge.client.event.ModelEvent` | docs.neoforged.net § BakedModels | Confirmed | `ModifyBakingResult` 内部类不变 |
| LAM-18 | `net.minecraftforge.client.event.RegisterKeyMappingsEvent` | 注册按键绑定 | `net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent` | docs.neoforged.net § KeyMappings | Confirmed | |
| LAM-19 | `net.minecraftforge.client.event.InputEvent` | 输入事件 | `net.neoforged.neoforge.client.event.InputEvent` | docs.neoforged.net § KeyMappings | Confirmed | NeoForge 建议用 `ClientTickEvent.Post` 替代 |
| LAM-20 | `net.minecraftforge.client.settings.KeyConflictContext` | 按键冲突上下文 | `net.neoforged.neoforge.client.settings.KeyConflictContext` | docs.neoforged.net § KeyMappings | Confirmed | 现在实现 `IKeyConflictContext` 接口 |
| LAM-21 | `net.minecraftforge.client.settings.KeyModifier` | 按键修饰符 | `net.neoforged.neoforge.client.settings.KeyModifier` | docs.neoforged.net § KeyMappings | Confirmed | |
| LAM-22 | `net.minecraftforge.client.extensions.common.IClientItemExtensions` | 自定义物品渲染 | `net.neoforged.neoforge.client.extensions.common.IClientItemExtensions` | docs.neoforged.net § BER | Confirmed | 注册方式改为 `RegisterClientExtensionsEvent` (Phase 6) |
| LAM-23 | `net.minecraftforge.data.event.GatherDataEvent` | 数据生成入口 | `net.neoforged.neoforge.data.event.GatherDataEvent` | docs.neoforged.net § Resources | Confirmed | Datagen 迁移属于 Phase 5 |
| LAM-24 | `net.minecraftforge.energy.IEnergyStorage` | FE 能量存储接口 | `net.neoforged.neoforge.energy.IEnergyStorage` (相同 6 个方法) | GitHub neoforged/NeoForge | Confirmed | |
| LAM-25 | `net.minecraftforge.energy.EnergyStorage` | FE 能量存储基类 | `net.neoforged.neoforge.energy.EnergyStorage` | GitHub neoforged/NeoForge | Confirmed | 1.21.1 中未弃用 |
| LAM-26 | `ForgeCapabilities.ENERGY` | 能量能力键 | `Capabilities.EnergyStorage.ITEM` (`ItemCapability<IEnergyStorage, Void>`) | docs.neoforged.net § Capabilities | Confirmed | |
| LAM-27 | `LazyOptional<T>` | 延迟能力解析 | **已移除** — `getCapability()` 返回 `T` 或 `null` | docs.neoforged.net § Capabilities | Confirmed | |
| LAM-28 | `Capability<T>` | 能力键类型 | `ItemCapability<T, C>` (参数化) | docs.neoforged.net § Capabilities | Confirmed | |
| LAM-29 | `@AutoRegisterCapability` | 自动注册能力 | **已移除** — 使用 `RegisterCapabilitiesEvent` | docs.neoforged.net § Capabilities | Confirmed | |
| LAM-30 | `Item#initCapabilities()` | 物品能力注入入口 | **已移除** — 使用 `event.registerItem()` | docs.neoforged.net § Capabilities | Confirmed | |
| LAM-31 | `ICapabilityProvider` | 能力桥接接口 | **已移除** — 无需桥接类 | docs.neoforged.net § Capabilities | Confirmed | |
| LAM-32 | `stack.getCapability(CAP, side).ifPresent(...)` | 能力查询 | `T v = stack.getCapability(CAP); if (v != null) { ... }` | docs.neoforged.net § Capabilities | Confirmed | |
| LAM-33 | `INBTSerializable<CompoundTag>` (物品数据) | 能力数据序列化 | `DataComponentType<T>` + `Codec` + `StreamCodec` | docs.neoforged.net § DataComponents | Confirmed | |
| LAM-34 | `DeferredRegister` for cap registration | 能力注册方式 | `DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID)` | docs.neoforged.net § DataComponents | Confirmed | |
| LAM-35 | `net.minecraftforge.common.util.INBTSerializable` | 序列化工具接口 | 弃用 — 数据组件通过 codec 处理序列化 | docs.neoforged.net § DataComponents | Confirmed | |
| LAM-36 | `net.minecraftforge.common.capabilities.ForgeCapabilities` | Forge 能力常量 | `net.neoforged.neoforge.capabilities.Capabilities` | docs.neoforged.net § Capabilities | Confirmed | |
| LAM-37 | `NetworkRegistry.newSimpleChannel` | 网络渠道注册 | `RegisterPayloadHandlersEvent` + `event.registrar("1")` | docs.neoforged.net § Networking | Confirmed | |
| LAM-38 | `SimpleChannel` 实例变量 | 全局网络实例 | **移除** — `PacketDistributor` 按 payload type 路由 | docs.neoforged.net § Networking | Confirmed | |
| LAM-39 | `NetworkDirection.PLAY_TO_SERVER` | 网络方向 | `registrar.playToServer()` | docs.neoforged.net § Networking | Confirmed | |
| LAM-40 | `Supplier<NetworkEvent.Context>` handler | 包处理 | `IPayloadContext` — 无需 `enqueueWork`/`setPacketHandled` | docs.neoforged.net § Networking | Confirmed | |
| LAM-41 | `PacketDistributor.PLAYER.with(...)` | 发送给指定玩家 | `PacketDistributor.sendToPlayer(serverPlayer, payload)` | docs.neoforged.net § Networking | Confirmed | |
| LAM-42 | `FriendlyByteBuf` 手动编解码 | 包序列化 | `StreamCodec<ByteBuf, T>` 配合 `ByteBufCodecs` | docs.neoforged.net § StreamCodecs | Confirmed | |
| LAM-43 | N/A (新增) | Payload 唯一标识 | `CustomPacketPayload.Type<T>` 配合 `ResourceLocation` | docs.neoforged.net § Networking | Confirmed | |
| LAM-44 | N/A (新增) | 包处理上下文 | `IPayloadContext` — 替代 `NetworkEvent.Context` | docs.neoforged.net § Networking | Confirmed | |
| LAM-45 | `net.minecraftforge.common.data.DatapackBuiltinEntriesProvider` | 数据生成中注册内建数据包注册表条目 | `net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider` | NeoForge GitHub 1.21.1 + docs.neoforged.net § Registries | Confirmed | 构造函数和 API 不变 |
| LAM-46 | `Consumer<FinishedRecipe>` + `buildRecipes(Consumer)` | RecipeProvider 食谱构建参数 | `RecipeOutput` + `protected void buildRecipes(RecipeOutput)` | docs.neoforged.net § Recipes § Data Generation | Confirmed | FinishedRecipe 类已移除 |
| LAM-47 | `RecipeProvider(PackOutput)` | RecipeProvider 单参构造 | `RecipeProvider(PackOutput, CompletableFuture<HolderLookup.Provider>)` | docs.neoforged.net § Recipes § Data Generation | Confirmed | |
| LAM-48 | `.save(Consumer<FinishedRecipe>)` | 保存食谱构建结果 | `.save(RecipeOutput)` 或 `.save(RecipeOutput, String)` | docs.neoforged.net § Built-In Recipe Types | Confirmed | |
| LAM-49 | `net.minecraftforge.common.crafting.conditions.IConditionBuilder` | 食谱条件构建器接口 | `net.neoforged.neoforge.common.conditions.IConditionBuilder` + `RecipeOutput#withConditions()` | docs.neoforged.net § Data Load Conditions | Confirmed | |
| LAM-50 | `net.minecraftforge.common.Tags` | Forge 公共标签常量 | `net.neoforged.neoforge.common.Tags` | docs.neoforged.net § Tags | Confirmed | 标签常量路径不变 |
| LAM-51 | `net.minecraft.data.recipes.FinishedRecipe` | 已完成的食谱表示 | **已移除** — 由 `RecipeOutput` 内部处理 | docs.neoforged.net § Recipes | Confirmed | |

## Open

| ID | 待确认旧 API / 模式 | 文件位置 | 需要确认的问题 | 下一次查询建议 |
|---|---|---|---|---|

## Confirmed (Phase 6)

| ID | 旧 Forge API / 模式 | 使用意图 | NeoForge 1.21.1 替换方案 | 证据 | 状态 | 备注 |
|---|---|---|---|---|---|---|
| LAM-52 | `Item#initializeClient(Consumer<IClientItemExtensions>)` | 注册 BEWLR 自定义渲染器 | `RegisterClientExtensionsEvent` + `event.registerItem(new IClientItemExtensions() {...}, items)` | docs.neoforged.net § BER | Confirmed | BEWLR 实例化转移到 IClientItemExtensions 实现中 |
| LAM-53 | `InputEvent.Key` + `KeyMapping#isDown()` | 检测按键按下 | `ClientTickEvent.Post` + `while (KeyMapping#consumeClick())` | docs.neoforged.net § KeyMappings | Confirmed | NeoForge 强烈建议不使用 InputEvent |
| LAM-54 | `Item#getShareTag(ItemStack)` / `Item#readShareTag(ItemStack, CompoundTag)` | 物品 NBT 数据同步 | **已移除** — 使用 DataComponentType 的 `networkSynchronized` StreamCodec | docs.neoforged.net § DataComponents | Confirmed | Item 类中不再存在这些方法 |
| LAM-55 | `BlockEntityWithoutLevelRenderer(BlockEntityRenderDispatcher)` (单参构造) | BEWLR 构造 | `BlockEntityWithoutLevelRenderer(BlockEntityRenderDispatcher, EntityModelSet)` (双参构造) | 1.21.1 SDK 源码 | Confirmed | EntityModelSet 参数为新增必需参数 |
| LAM-56 | `@EventBusSubscriber` 无 modid 参数 | 自动注册事件监听器 | `@EventBusSubscriber(modid = "modid")` — modid 参数必须提供 | docs.neoforged.net § Events | Confirmed | 已在 Phase 2 应用 |
| LAM-57 | N/A (新增) | 注册 IClientItemExtensions | `RegisterClientExtensionsEvent` 在 MOD 事件总线上触发 | docs.neoforged.net § BER | Confirmed | 替代 Item#initializeClient |

## Confirmed (Phase 7)

| ID | 旧 Forge API / 模式 | 使用意图 | NeoForge 1.21.1 替换方案 | 证据 | 状态 | 备注 |
|---|---|---|---|---|---|---|
| LAM-58 | `new ResourceLocation(String, String)` | 创建资源位置 | `ResourceLocation.fromNamespaceAndPath(String, String)` | 1.21.1 SDK 源码 | Confirmed | 构造函数在 1.21.1 变为 private |
| LAM-59 | `net.minecraft.data.worldgen.BootstapContext` (typo) | 数据生成引导上下文 | `net.minecraft.data.worldgen.BootstrapContext` | 1.21.1 SDK 源码 | Confirmed | 原始 Forge 代码中的拼写错误 |
| LAM-60 | `Supplier<Item>.getId()` | 获取注册项 ResourceLocation | `BuiltInRegistries.ITEM.getKey(supplier.get())` | 1.21.1 SDK 源码 | Confirmed | Supplier 不提供 getId() 方法；改用注册表查询 |
