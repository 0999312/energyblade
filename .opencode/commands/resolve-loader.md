---
description: 查询一个 Forge loader API 在 NeoForge 1.21.1 中的替代方案并更新映射文档
agent: build
---
目标固定为将当前项目从 `Minecraft 1.20.1 Forge` 迁移到 `Minecraft 1.21.1 NeoForge`。

请解决以下 loader API 差异：$ARGUMENTS

步骤：

1. 先在本地代码中定位 `$ARGUMENTS` 的引用和用途
2. 调用 `loader-diff-research`
3. 如结果涉及原版类、方法、字段、注册表或版本差异，再调用 `vanilla-code-research`
4. 在 `docs/migration/LOADER_API_MAP.md` 中更新：
   - 如果结论已明确，写入 `Confirmed`
   - 如果仍有缺口，写入或更新 `Open`
5. 在 `docs/migration/PROGRESS.md` 中记录本次新结论
6. 输出：
   - 旧 API
   - NeoForge 替换方案
   - 证据
   - 受影响文件
   - 最小验证方式
