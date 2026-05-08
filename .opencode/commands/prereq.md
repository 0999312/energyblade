---
description: 校验迁移前置依赖：Java / Node / opencode / 项目源代码 / MCP 连通性
agent: build
---
你是 Minecraft 模组迁移前置校验代理。

目标固定为将项目从 `Minecraft 1.20.1 Forge` 迁移到 `Minecraft 1.21.1 NeoForge`。

## 步骤 1：定位项目源代码

按优先级获取项目源代码位置：

1. 环境变量 `MC_FORGE_PROJECT_PATH`——本地项目路径
2. 环境变量 `MC_FORGE_PROJECT_REPO`——GitHub 仓库地址
3. 如果上述均未设置，直接询问用户："请提供 1.20.1 Forge 项目的本地路径或 GitHub 仓库地址"
4. 记录到 `docs/migration/PREREQUISITES.md` 的「项目源代码」表

如果来源是 GitHub 仓库且本地工作副本尚未克隆：
- 在 `C:\Users\mr099\AppData\Local\Temp\opencode\mc-port-sources\<repo-name>` 下运行 `git clone`
- 克隆后记录本地路径

## 步骤 2：校验环境依赖

依次运行以下检查，并把结果填入 `docs/migration/PREREQUISITES.md`：

| 检查项 | 命令 |
|---|---|
| Java 21+ | `java -version` |
| Node.js 20+ | `node -v` |
| opencode | `opencode --version` |
| Gradle wrapper | `./gradlew --version`（在项目根目录执行）|

任一检查失败即标记为阻塞。

## 步骤 3：校验 MCP 连通性

1. 调用 `loader-docs_search_fabric_docs`，查询：
   - `query: "DeferredRegister"`
   - `loader: neoforge`
   - `minecraft_version: 1.21.1`
   - `limit: 1`
2. 调用 `mc-source_list_minecraft_versions`，确认能列出可用版本

任一调用返回结果即标记为可用。

## 步骤 4：更新文档并输出摘要

1. 在 `docs/migration/PREREQUISITES.md` 中填写所有检查结果
2. 如果有阻塞项，填入「阻塞记录」表并停止
3. 如果全部通过，输出如下摘要：

```
PREREQUISITES: ALL CLEAR

- 项目源代码: [路径或 URL]
- Java: [版本]
- Gradle: ready
- MCP: all reachable

Next: /scan-port
```

## 注意事项

- 不要修改任何业务代码
- 不要修改 build.gradle 或 gradle 配置
- 校验结果是幂等的——重复运行 `/prereq` 不会重复克隆或重复写入
