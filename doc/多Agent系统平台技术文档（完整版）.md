# 多Agent系统平台技术文档（完整版）

## 一、整体系统架构

```Plain Text
前端层 (Frontend)
├── agents-platform-front (统一Agent大前端)
└── ads_front (广告前端)
    ↓ AG-UI 协议
后端层 (Backend)
├── ads (广告服务)
│   ├── ads_agent_service (Agent服务)
│   ├── 流式聊天接口
│   └── 会话管理、状态管理
    ↓ A2A 协议
├── act_agent (营销活动Agent)
│   ├── 活动方案生成
│   ├── 营销页面创建
│   ├── 落地页生成
│   └── 集成 pagemaker_agent (页匠)
    ↓
└── runtime (Agent运行时框架)
    ├── YAML驱动配置
    ├── 多模型支持 (OpenAI/Gemini/DeepSeek)
    ├── Skill系统
    ├── Context Pruning
    └── AG-UI/A2A 协议支持
```

## 二、各项目详细分析

### 2\.1 agents\-platform\-front \- 统一 Agent 大前端

**核心功能：**

- 统一 Agent 大前端平台

- 集成多个业务领域 Agent（创作、活动等）

- 提供一致的用户体验

**技术栈：**

- React 19 \+ TypeScript 6 \+ Vite 8

- React Router 7（路由管理）

- Ant Design 6（UI 组件库）

- Zustand（全局状态管理）

- Axios（HTTP 请求）

- Sass（SCSS 语法）

**核心模块：**

- `src/routes/agents/` \- Agent 聊天页面

    - `components/act/` \- 活动相关组件

    - `components/ads/` \- 广告相关组件

    - `components/midasbuy\_application/` \- 应用相关组件

- `panelStore\.ts` \- 面板状态管理

- `threadStore\.ts` \- 线程状态管理

**关键特性：**

- 支持多 Agent 集成

- 支持面板展示（详情、步骤等）

- 支持自定义气泡渲染

- 支持自定义面板渲染

- 支持 AG\-UI 协议

**配置系统：**

- `config\.ts` \- Agent 配置

- 支持动态请求体构建

- 支持自定义请求头

- 支持 forwardedProps 透传

### 2\.2 ads\_front \- 广告前端

**核心功能：**

- 提供广告 Agent 的用户界面

- 支持实时流式聊天

- 支持多种交互组件（选择卡片、表单、图片展示等）

**技术栈：**

- React \+ TypeScript \+ Vite

- React Router（路由管理）

- 自定义组件库

**核心组件：**

- `src/pages/AdAgent/` \- Agent 聊天页面

    - `index\.tsx` \- 主页面

    - `GuideChoiceCard\.tsx` \- 引导选择卡片

    - `InlineFormCollector\.tsx` \- 内联表单收集器

    - `PictureShow\.tsx` \- 图片展示

    - `HistoryDrawer\.tsx` \- 历史记录抽屉

    - `BackgroundTaskCard\.tsx` \- 后台任务卡片

**关键特性：**

- 支持实时 SSE 流式响应

- 支持多种消息类型渲染

- 支持表单收集和验证

- 支持历史会话管理

### 2\.3 ads \- 广告服务后端

**核心功能：**

- 提供广告相关的后端服务

- 实现 AdsAgentService，与 runtime 交互

- 提供流式聊天接口（AG\-UI 协议）

- 支持会话管理和状态管理

- 知识库同步和管理

**技术架构：**

- 基于 tRPC\-Go 框架

- 集成 runtime 的 AG\-UI 协议

- 使用 Protocol Buffers 定义接口

- 支持 OpenTelemetry 链路追踪

**核心模块：**

- `pkg/service/ads\_agent\_service/` \- Agent 服务实现

- `pkg/logic/ads\_agent/` \- Agent 逻辑层

- `pkg/service/streamerservice/` \- 流式服务

- `pkg/service/crontab/` \- 定时任务（知识库同步）

- `pkg/remotecfg/` \- 远程配置（Rainbow）

**关键接口：**

- `/ads/v1/artifact` \- Artifact 下载

- `/ads\_photo/v1/bg\_task\_status` \- 后台任务状态查询

**状态管理：**

- 通过 runtime 的`/agui/setState`和`/agui/getState`管理会话状态

- 支持多轮对话上下文保持

### 2\.4 act\_agent \- 营销活动 Agent

**核心功能：**

- 营销活动 Specialist Agent，负责生成活动方案、营销页面和落地页

- 通过 A2A 协议暴露给 unified\_entry（统一入口）

- 内部集成 pagemaker\_agent（调用页匠）作为本地 sub\-agent

**技术架构：**

- 基于 Google ADK\-Go 框架构建

- 使用 runtime 框架作为运行时

- 支持 AG\-UI 协议用于前端流式交互

- 支持 A2A 协议用于 Agent 间通信

**核心组件：**

- `cmd/main\.go` \- 进程入口，配置 Launcher

- `pkg/pagemaker\_agent/` \- 页匠 ADK Agent 封装

- `pkg/pagemaker/` \- 页匠 HTTP 客户端

- `prompts/act\_agent\.md` \- LLM 指令文件

- `conf/agents\.yaml` \- Agent 配置文件

**工作流程：**

```Plain Text
用户请求 → act_agent（主控）
        ↓
调用 pagemaker_agent（sub-agent）
        ↓
调用页匠服务（HTTP/SSE）
        ↓
返回活动方案/页面/落地页
```

**关键特性：**

- 自动转发用户消息给 pagemaker\_agent

- 支持活动流程状态管理（activity\_flow）

- 支持 UI 决策渲染（按钮、选择卡片等）

- 支持会话持久化（MySQL）

### 2\.5 runtime \- Agent 运行时框架

**核心功能：**

- Go 语言实现的 Agent 框架库（非独立二进制）

- 封装 Google ADK\-Go，提供 YAML 驱动的配置系统

- 提供可插拔的子系统（模型、会话、工具、技能等）

**技术架构：**

- 基于 tRPC\-Go 作为服务器框架

- 使用 Factory/Registry 模式实现可扩展性

- 支持 YAML 配置文件（agents\.yaml）

**核心子系统：**

|子系统|功能|注册工厂|
|---|---|---|
|agent|Agent 创建和管理|`agent\.Register\(\)`|
|model|模型后端（OpenAI/Gemini/DeepSeek/Proxy）|`model\.Register\(\)`|
|session|会话管理（内存 / MySQL/Redis）|`session\.Register\(\)`|
|artifact|Artifact 管理|`artifact\.Register\(\)`|
|memory|记忆服务|`memory\.Register\(\)`|
|tool|工具集成|`tool\.Register\(\)`|
|plugin|插件系统|`plugin\.Register\(\)`|
|skill|技能系统（Markdown 动态指令包）|\-|
|a2a|Agent\-to\-Agent 协议|\-|
|workflow|YAML 驱动的 Agent 编排|\-|
|evaluation|评估系统|\-|

**技能系统（Skill System）：**

- Markdown\-based 动态指令包

- 支持运行时加载 / 卸载

- 支持 LRU 淘汰策略

- 支持生命周期管理（session/turn/choice）

**Context Pruning：**

- `SlidingWindowPruner` \- 保留最近 N 轮对话

- `MaxContentsPruner` \- 保留最近 N 个内容项

- `ToolResultTruncator` \- 截断长工具调用结果

- `KeepEndsWindowPruner` \- 保留首尾，删除中间

**协议支持：**

- AG\-UI \- 前端流式交互协议

- A2A \- Agent\-to\-Agent 通信协议

- HTTP/SSE \- 服务器推送事件

**配置系统（agents\.yaml）：**

- 基于 YAML 文件驱动 Agent 创建

- 支持动态加载 / 卸载技能

- 支持多模型后端切换

- 支持多会话服务后端（内存 / MySQL/Redis）

## 三、项目间关系

### 3\.1 前端与后端交互

```Plain Text
agents-platform-front / ads_front
        ↓ (AG-UI 协议)
        ↓ (SSE 流式响应)
ads（后端服务）
        ↓ (内部调用)
runtime（AG-UI Handler）
```

### 3\.2 Agent 间通信

```Plain Text
unified_entry（统一入口）
        ↓ (A2A 协议)
act_agent（营销活动Agent）
        ↓ (内部 sub-agent)
pagemaker_agent（页匠Agent）
        ↓ (HTTP/SSE)
页匠服务（外部服务）
```

### 3\.3 Runtime 框架支撑

```Plain Text
act_agent / ads
        ↓ (import)
runtime（框架库）
        ↓ (封装)
Google ADK-Go（核心框架）
```

## 四、核心技术亮点

### 4\.1 协议支持

- AG\-UI 协议 \- 前端流式交互

- A2A 协议 \- Agent 间通信

- HTTP/SSE \- 服务器推送事件

- tRPC \- 内部服务通信

### 4\.2 配置驱动

- YAML 配置文件驱动 Agent 创建

- 支持动态加载 / 卸载技能

- 支持多模型后端切换

- 支持多会话服务后端（内存 / MySQL/Redis）

### 4\.3 可扩展性

- Factory/Registry 模式

- 插件系统

- 工具系统

- 技能系统

### 4\.4 可观测性

- OpenTelemetry 集成

- 链路追踪

- 日志系统

- 监控指标（Prometheus）

### 4\.5 前端现代化

- React 19 \+ TypeScript 6

- Vite 8（快速构建）

- Zustand（轻量级状态管理）

- Ant Design 6（企业级 UI）

## 五、部署架构

### 5\.1 开发环境

本地开发：

- agents\-platform\-front: `npm run dev`（端口 5173）

- ads\_front: `npm run dev`（端口 5174）

- ads: `go run main\.go`（端口 8082/8084）

- act\_agent: `bash start\.sh`（端口 18792）

### 5\.2 生产环境

Docker 部署：

- 各项目提供 Dockerfile

- 使用 \[pack\.sh\]\(pack\.sh\) 构建二进制

- 使用 docker build 构建镜像

- 通过环境变量配置

## 六、总结

这是一个企业级 AI Agent 营销创作平台，具有如下特点：

1. **完整性** \- 从前端到后端，从框架到应用，覆盖完整链路

2. **模块化** \- 各项目职责清晰，边界明确

3. **可扩展** \- 基于 Factory/Registry 模式，易于扩展

4. **配置驱动** \- YAML 配置驱动，无需修改代码

5. **协议标准** \- 支持 AG\-UI、A2A 等标准协议

6. **现代化** \- 使用最新的技术栈（React 19、Vite 8、Go 1\.25）

7. **可观测** \- 集成 OpenTelemetry，支持链路追踪

该平台可以用于：

- 营销活动方案生成

- 广告创意创作

- 落地页生成

- 多轮对话交互

- Agent 间协作

## 七、快速开始

### 启动 act\_agent

```bash
cd /data/workspace/act_agent
export AGENTS_DB_TARGET="dsn://user:pass@tcp(host:3306)/kagent?parseTime=true&charset=utf8mb4"
export GEMINI_API_KEY="your-api-key"
export PAGEMAKER_URL="http://wenshuaicui-any2.devcloud.woa.com:18790"
bash start.sh
```

## 九、关键技术决策

### 为什么选择 Google ADK\-Go?

- 官方 Agent 框架，社区活跃

- 支持多种 LLM 后端

- 内置 Agent 编排能力

- 支持流式响应

### 为什么选择 tRPC\-Go?

- 腾讯内部标准 RPC 框架

- 支持多种协议（HTTP/gRPC/tRPC）

- 集成丰富的中间件

- 支持服务发现和负载均衡

### 为什么选择 YAML 配置驱动？

- 声明式配置，易于理解

- 支持动态加载

- 支持环境变量注入

- 支持配置校验

### 为什么选择 AG\-UI 协议？

- 标准化的 Agent \- 前端交互协议

- 支持流式响应

- 支持多轮对话

- 支持状态管理

## 十、未来规划

### 短期（1\-3 个月）

* [ ] 完善单元测试覆盖率

* [ ] 优化 Context Pruning 策略

* [ ] 支持更多 LLM 后端

* [ ] 优化前端性能

### 中期（3\-6 个月）

* [ ] 支持多模态输入（图片、语音）

* [ ] 支持 Agent 市场

* [ ] 支持分布式部署

* [ ] 完善监控和告警

### 长期（6\-12 个月）

* [ ] 支持联邦学习

* [ ] 支持多租户隔离

* [ ] 支持边缘计算

* [ ] 构建生态系统

---

**文档版本：** v1\.0
**最后更新：** 2026\-05\-16
**作者：** AI Assistant

> （注：文档部分内容可能由 AI 生成）
