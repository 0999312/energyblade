# MIGRATION_PLAN

## Target

- From: Minecraft 1.20.1 Forge
- To: Minecraft 1.21.1 NeoForge

## Rules

- 不做双版本兼容层
- 每次只推进一个 phase
- 先解决 loader API 差异，再进入具体实现
- 每个 phase 完成后更新 `PROGRESS.md`
- 每解决一个 loader API 差异后更新 `LOADER_API_MAP.md`

## Phase 0 Baseline Scan

- Gradle / metadata
- 注册与生命周期
- event bus
- capability / data
- network
- datagen
- client
- 第三方依赖

## Phase 1 Build And Entry

- 升级构建脚本与模组入口
- 确认最小编译基线

## Phase 2 Registration And Lifecycle

- 注册系统
- 事件总线
- 生命周期事件

## Phase 3 Data And State

- capability
- 数据组件
- NBT / 持久化状态

## Phase 4 Networking And Sync

- packet
- payload
- spawn / entity sync

## Phase 5 Datagen And Resources

- tags
- loot
- recipes
- models
- lang

## Phase 6 Client Systems

- renderer
- overlay
- input
- particle
- model hook

## Phase 7 Cleanup And Validation

- 清理旧 Forge 残留
- 编译与运行验证
