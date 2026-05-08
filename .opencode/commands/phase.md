---
description: 执行一个明确的迁移 phase
agent: build
---
目标固定为将当前项目从 `Minecraft 1.20.1 Forge` 迁移到 `Minecraft 1.21.1 NeoForge`。
当前只推进：$ARGUMENTS

开始前先读取：

- `AGENTS.md`
- `docs/migration/MIGRATION_PLAN.md`
- `docs/migration/PROGRESS.md`
- `docs/migration/LOADER_API_MAP.md`

要求：

1. 只修改当前 phase 直接相关的文件
2. 遇到未确认的 loader API 时，先调用 `loader-diff-research`
3. 遇到未确认的原版源码或签名时，调用 `vanilla-code-research`
4. 完成后运行最小必要验证
5. 更新 `docs/migration/PROGRESS.md`
6. 如果得到新的可复用 loader API 结论，同步更新 `docs/migration/LOADER_API_MAP.md`
7. 如上下文变紧，停在稳定检查点并输出新的 `Next Prompt`
