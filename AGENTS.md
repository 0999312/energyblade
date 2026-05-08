# AGENTS.md

本仓库用于将模组从 `Minecraft 1.20.1 Forge` 迁移到 `Minecraft 1.21.1 NeoForge`。

## 目标

- 迁移目标固定为：`1.20.1 Forge` -> `1.21.1 NeoForge`
- 默认不保留双版本兼容层
- 优先最小改动，优先修正编译错误、行为回归和迁移阻塞

## 前置确认

首次会话或在新环境中启动迁移前，必须先完成前置校验。这是所有后续工作的硬性入口。

### 项目源代码位置

通过以下任一方式获得 1.20.1 Forge 项目源代码：

- 环境变量 `MC_FORGE_PROJECT_PATH`：指向本地项目根目录
- 环境变量 `MC_FORGE_PROJECT_REPO`：指向 GitHub 仓库地址
- 直接向用户询问路径或仓库链接

如果来源是 GitHub 仓库且本地尚未克隆，使用 `git clone` 将代码拉取到本地临时工作目录。

### 前置校验流程

1. 运行 `/prereq` 命令
2. `/prereq` 会自动：
   - 从环境变量或用户输入获取项目源代码路径
   - 校验 Java 21+ / Node.js 20+ / opencode / Gradle wrapper 版本
   - 校验 `loader-docs` 和 `mc-source` 两个 MCP 的连通性
   - 将结果写入 `docs/migration/PREREQUISITES.md`
3. 如果 `/prereq` 报告阻塞，必须先解决阻塞再继续
4. 如果 `/prereq` 报告 `ALL CLEAR`，立即执行 `/scan-port`

### 环境变量速查

```powershell
# 启动 opencode 前设置（任选其一）
$env:MC_FORGE_PROJECT_PATH = "D:\mc-projects\my-mod-1.20.1"
$env:MC_FORGE_PROJECT_REPO   = "https://github.com/user/my-forge-mod"
```

## 强制规则：不要依赖模型记忆处理 Loader API

- 不要凭记忆判断 Forge 或 NeoForge 的 loader API
- 只要涉及以下内容，且 `docs/migration/LOADER_API_MAP.md` 中没有现成结论，就必须先查询再改代码：
  - `net.minecraftforge.*`
  - `cpw.mods.*`
  - `net.neoforged.*`
  - 注册、事件总线、生命周期、capability、数据组件、网络、自定义包、datagen、条件系统、配置系统、模组元数据
- 遇到不确定的 loader API，不允许直接猜测替换写法

## MCP 分工

- `loader-docs_*`：用于解决 `Forge -> NeoForge loader API` 差异
- `mc-source_*`：用于查询 Minecraft 原版源码、映射、版本差异、注册表与第三方模组源码参考

## Loader API 差异处理流程

1. 先从本地代码确认旧 API 的符号、用途、调用位置
2. 优先使用 `loader-diff-research` 子代理
3. `loader-diff-research` 必须优先调用：
   - `loader-docs_search_fabric_docs`
   - `loader-docs_get_example`
4. 查询 `loader-docs_*` 时，始终优先带上：
   - `loader: neoforge`
   - `minecraft_version: 1.21.1`
5. 注意：`search_fabric_docs` 名称是历史遗留，NeoForge 查询仍然使用它，但必须传 `loader: neoforge`
6. 如果 loader 文档查询不足以确认结论，再抓取官方 `https://docs.neoforged.net` 页面作为补充证据
7. 如果替换方案进一步涉及原版类、字段、方法签名或版本差异，再使用 `vanilla-code-research` 子代理
8. 每解决一个 loader API 差异，都要同步记录到 `docs/migration/LOADER_API_MAP.md`

## 查询建议

- 注册：`DeferredRegister`、`creative tab`、`block entity`、`menu type`
- 生命周期与事件：`EventBusSubscriber`、`mod event bus`、`lifecycle event`
- 数据与状态：`capability`、`data component`、`saved data`
- 网络：`custom packet`、`payload`、`spawn sync`
- Datagen：`GatherDataEvent`、`DatapackBuiltinEntriesProvider`、`tags`、`loot`
- 条件与资源：`neoforge:conditions`、`ResourceLocation`、`ResourceKey`

## 迁移文档

- `docs/migration/MIGRATION_PLAN.md`：阶段计划
- `docs/migration/PROGRESS.md`：当前进度、阻塞、下一步
- `docs/migration/LOADER_API_MAP.md`：已确认的 Forge -> NeoForge loader API 映射

## 阶段推进规则

- 每次只推进一个 phase 或一个明确子问题
- 先解决 loader API 差异，再进入具体实现
- 每个 phase 完成后更新 `PROGRESS.md`
- 每次发现新的可复用 loader API 结论后更新 `LOADER_API_MAP.md`

## 上下文控制

- 当会话里已经积累大量搜索结果、源码片段、多个 phase 结论时，必须主动停在稳定检查点
- 暂停前必须更新 `docs/migration/PROGRESS.md`
- 暂停回复末尾必须输出新的 `Next Prompt`

## 验证

- 小改动优先 `./gradlew compileJava`
- 涉及 datagen 时运行 `./gradlew runData`
- 涉及客户端路径时运行 `./gradlew runClient`
- phase 结束时记录本次验证结果
