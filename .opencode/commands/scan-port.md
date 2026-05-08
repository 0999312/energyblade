---
description: 盘点当前仓库的 Forge 到 NeoForge 迁移范围并初始化迁移文档
agent: build
---
目标固定为将当前项目从 `Minecraft 1.20.1 Forge` 迁移到 `Minecraft 1.21.1 NeoForge`。

## 前置检查

在开始盘点前，先确认 `docs/migration/PREREQUISITES.md` 已存在且所有检查项为通过。

如果该文件不存在或有阻塞项，立即终止并提示："请先运行 /prereq 完成前置校验。"

## 读取上下文

开始前先读取：

- `AGENTS.md`
- `docs/migration/PREREQUISITES.md`
- `docs/migration/MIGRATION_PLAN.md`
- `docs/migration/PROGRESS.md`
- `docs/migration/LOADER_API_MAP.md`

## 要求

1. 扫描与迁移相关的本地内容：
   - Gradle 与依赖
   - 模组元数据
   - 注册与生命周期
   - 事件总线
   - capability / 数据组件 / NBT
   - 网络与同步
   - datagen 与资源
   - 客户端系统
   - 第三方依赖与互操作
2. 识别所有明显的 Forge loader API 痕迹，并把未确认项写入 `docs/migration/LOADER_API_MAP.md` 的 `Open` 表
3. 按阶段填充或更新 `docs/migration/MIGRATION_PLAN.md`
4. 更新 `docs/migration/PROGRESS.md`，将当前阶段设为 `Phase 0 Baseline Scan`
5. 输出：
   - 推荐 phase 列表
   - 每个 phase 的受影响文件
   - 第一阶段最小动作
