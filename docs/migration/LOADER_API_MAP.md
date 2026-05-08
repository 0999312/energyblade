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

## Open

| ID | 待确认旧 API / 模式 | 文件位置 | 需要确认的问题 | 下一次查询建议 |
|---|---|---|---|---|
