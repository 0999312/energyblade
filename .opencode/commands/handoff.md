---
description: 在上下文紧张或阶段结束时整理交接信息并生成下一阶段提示词
agent: build
---
读取：

- `AGENTS.md`
- `docs/migration/MIGRATION_PLAN.md`
- `docs/migration/PROGRESS.md`
- `docs/migration/LOADER_API_MAP.md`

要求：

1. 更新 `docs/migration/PROGRESS.md`
2. 输出：
   - 当前 phase
   - 已完成事项
   - 本阶段新增的 loader API 结论
   - 仍未解决的阻塞
   - 下一步最小动作
   - 可直接复制的 `Next Prompt`
3. 不继续修改业务代码
