# PROGRESS

## Target

- From: Minecraft 1.20.1 Forge
- To: Minecraft 1.21.1 NeoForge

## Current Phase

- Name:
- Status: in_progress | blocked | completed
- Last updated:

## Completed

- 

## New Loader API Findings

- 

## Files

- 

## Validation

- compileJava:
- runData:
- runClient:

## Blockers

- 

## Next Action

- 

## Next Prompt

继续当前模组迁移任务。目标固定为将项目从 `Minecraft 1.20.1 Forge` 迁移到 `Minecraft 1.21.1 NeoForge`。开始前先读取 `AGENTS.md`、`docs/migration/MIGRATION_PLAN.md`、`docs/migration/PROGRESS.md`、`docs/migration/LOADER_API_MAP.md`。先总结当前 phase、阻塞和已确认的 loader API 映射，然后只推进 `Next Action`。遇到未确认的 Forge / NeoForge loader API 时必须先调用 `loader-diff-research`；遇到原版类、签名或版本差异问题时调用 `vanilla-code-research`。到达稳定检查点后更新 `PROGRESS.md` 并输出新的 `Next Prompt`。
