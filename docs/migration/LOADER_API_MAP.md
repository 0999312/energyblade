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

## Open

| ID | 待确认旧 API / 模式 | 文件位置 | 需要确认的问题 | 下一次查询建议 |
|---|---|---|---|---|
