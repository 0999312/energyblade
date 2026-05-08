# PREREQUISITES

## 环境依赖清单

| 依赖项 | 最低版本 | 检查命令 | 状态 |
|---|---|---|---|
| Java | 21 | `java -version` | |
| Node.js | 20 | `node -v` | |
| opencode | 最新 | `opencode --version` | |
| Gradle | 项目内置 wrapper | `./gradlew --version` | |
| MCP: mc-source | 自动拉取 | `/prereq` 内自动校验 | |
| MCP: loader-docs | 自动拉取 | `/prereq` 内自动校验 | |

## 项目源代码

| 属性 | 值 |
|---|---|
| 来源类型 | local \| github |
| 来源值 | |
| 编译状态 | |

### 来源类型说明

- **local**: 本地文件系统路径，如 `D:\mc-mods\slashedblade-1.20.1`
- **github**: GitHub 仓库地址，如 `https://github.com/user/repo`（支持默认分支或 `?ref=branch`）

### 环境变量（推荐）

在启动 opencode 前设置以下环境变量，`/prereq` 会自行补入：

```powershell
$env:MC_FORGE_PROJECT_PATH = "D:\path\to\forge-project"
# 或者
$env:MC_FORGE_PROJECT_REPO = "https://github.com/user/forge-mod"
```

也可以在 opencode 内直接告诉代理路径或仓库链接，`/prereq` 会自动记录。

## 首次确认结果

| 检查项 | 结果 | 备注 |
|---|---|---|
| Java 21+ 可用 | | |
| Node.js 20+ 可用 | | |
| opencode 可用 | | |
| Gradle wrapper 可用 | | |
| 项目源代码可访问 | | |
| mc-source MCP 可达 | | |
| loader-docs MCP 可达 | | |

## 阻塞记录

| 阻塞项 | 影响 | 解决方式 |
|---|---|---|
