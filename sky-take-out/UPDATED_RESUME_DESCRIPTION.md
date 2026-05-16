# 项目描述（更新版）

## 项目标题

- 推荐标题：基于自研 Agent Runtime 的多 Agent 智能餐饮协作平台
- 备选标题 1：自研 Agent 运行时框架驱动的智能餐饮服务平台
- 备选标题 2：B/C 双端智能餐饮服务与多 Agent 协作平台

---

## 项目简介

2025.03-2025.06 基于自研 Agent Runtime 的多 Agent 智能餐饮协作平台 软件开发

面向 B/C 双端场景构建智能餐饮服务平台，围绕商品、套餐、购物车、订单、配送状态、实时通知与经营分析形成完整交易闭环。核心创新在于：**基于 LangChain4j 底层 LLM 能力，自研了一套完整的 Agent Runtime 运行时框架**——涵盖 YAML 配置驱动引擎、Factory/Registry 可插拔扩展机制、Skill 动态加载系统（10 个 Markdown 技能包）、Context Pruning 裁剪策略（滑动窗口/Token 预算/工具截断）、多模型 Provider（OpenAI/DeepSeek/Qwen）、双后端会话管理（Redis/Memory）、A2A Agent 间通信协议与 AG-UI 前端流式交互协议。在此之上构建了 Orchestrator 编排层与 5 个 Specialist Agent，将传统点餐系统升级为自研框架驱动的多 Agent 智能平台。

**技术栈**：自研 Agent Runtime、LangChain4j、RAG、Spring Boot、MyBatis、MySQL、Redis、WebSocket、JWT、Nginx、Docker

---

## 核心工作

**1. 智能餐饮平台交易核心链路建设**

完成商品、套餐、购物车、订单、实时通知与经营分析等模块落地，基于 5 万级用户、3000+ 菜品、20 万级订单数据完成联调与压测，系统在 300 并发下可稳定运行，核心交易请求错误率控制在 0.5% 以内。

**2. 自研 Agent Runtime 运行时框架（核心创新）**

不依赖第三方 Agent 框架，基于 LangChain4j 底层 LLM 能力自研了一套完整的 Agent Runtime：
- 构建 **Agent Runtime 运行时框架**：YAML 配置驱动 Agent 创建（agents.yaml），Factory/Registry 模式实现可插拔扩展，多模型 Provider 支持（OpenAI / DeepSeek / Qwen），Redis/Memory 双后端会话管理
- 实现 **Orchestrator 编排层**：基于关键词的意图分类器，支持对 5 个 Specialist Agent 的自动路由与 A2A 协议通信
- 设计 **Skill 系统**：10 个 Markdown 格式动态技能包（中国菜系知识、饮食禁忌检查、订单政策、退款策略、投诉处理流程、经营分析方法、报告生成模板、后厨工作流、意图分类规则），支持运行时加载与 LRU 淘汰
- 实现 **Context Pruning 策略**：滑动窗口裁剪 + Token 预算裁剪 + 工具结果截断，确保多轮对话控制在 token 预算内
- 封装 **4 个领域工具类** 20+ `@Tool` 方法：菜单查询（AgentTools）、订单管理（OrderTools）、经营分析（AnalyticsTools）、客服工单（CustomerServiceTools）
- 实现 **AG-UI 流式协议**：SSE 流式事件推送 + 会话状态管理接口（setState/getState），支持前端实时对话交互

**3. 智能点餐 RAG 与 Tool Calling**

基于 LangChain4j 的 `@AiService` + `@Tool` + `ContentRetriever` + `ChatMemory` 实现智能点餐 Agent：在应用启动阶段加载 3000+ 菜品/套餐数据并完成向量化入库（InMemoryEmbeddingStore），结合 EmbeddingStoreContentRetriever（maxResults=5, minScore=0.5）实现菜单语义检索，支持多轮对话记忆（MessageWindowChatMemory）与历史偏好分析，菜单相关问答在测试集上的有效命中率稳定达到 90%+。

**4. 数据库索引优化与性能调优**

围绕订单分页、超时扫描、购物车查重、默认地址查询等高频访问路径设计复合索引与关联索引策略，并通过 SQL 压测、EXPLAIN 执行计划与慢查询分析持续校准索引命中、扫描行数和回表成本，推动核心查询从低效扫描向索引驱动访问收敛，保证数据库在更高数据规模下的稳定响应；优化后相关接口平均响应时间由约 230ms 降至 130ms 左右，典型压测批次中的慢查询条数下降约 75%。

**5. Redis 缓存治理与异步通知机制**

构建 Redis 缓存治理方案与异步通知机制，围绕热点菜单查询、店铺状态读取和 WebSocket 实时推送，推进缓存命中率、回源率、热点 key 分析和线程池链路指标建设，其中热点菜单查询缓存命中率稳定在 94% 左右，菜单查询类核心接口 QPS 由约 260 提升至 520+。同时将来单提醒、催单通知等 WebSocket 推送从订单状态变更主链路中剥离，基于独立线程池（核心2/最大4/CallerRunsPolicy）异步分发消息，订单通知平均延迟由约 650ms 降至 180ms 以内，P95 控制在 300ms 内。

**6. 工程化链路与安全加固**

基于 JWT 双端鉴权（Admin + User 独立密钥与过期策略）、AOP 公共字段自动填充（`@AutoFill` 注解驱动）、声明式事务与 OSS 资源上传能力，完善认证、安全、数据一致性与文件服务等工程化链路；实现 Nginx 多实例负载、缓存一致性与热点 Key 互斥重建，在热点访问场景下将数据库缓存回源率控制在 6% 以内；配置 `.gitignore` 安全规则，移除敏感凭证并生成 `.example` 配置模板；完成 Docker Compose 与 Nginx 生产部署配置。

---

## 量化指标

| 维度 | 指标 |
|------|------|
| Agent 架构 | 1 Orchestrator + 5 Specialist Agent |
| 领域工具 | 4 个工具类，20+ `@Tool` 方法 |
| Skill 技能库 | 10 个 Markdown 技能包 |
| RAG 向量条目 | 3000+ 菜品/套餐 |
| Context 裁剪策略 | 滑动窗口 / Token 预算 / 工具结果截断 |
| 模型支持 | OpenAI / DeepSeek / Qwen（可插拔） |
| 会话后端 | Memory / Redis（双模式） |
| 并发能力 | 300 QPS 稳定 |
| 核心错误率 | < 0.5% |
| 菜单推荐命中率 | 90%+ |
| 接口响应时间 | 230ms → 130ms（↓43%） |
| 慢查询降低 | ↓75% |
| 菜单缓存命中率 | ~94% |
| 菜单查询 QPS | 260 → 520+（↑100%） |
| 通知延迟（均值） | 650ms → 180ms（↓72%） |
| 通知延迟（P95） | < 300ms |
| 缓存回源率 | < 6% |

---

## 面试可展开点（更新）

- **自研 Runtime**：为什么不直接用 LangChain4j 而是自研一套 Runtime？YAML 配置驱动 vs 硬编码的优劣？Factory/Registry 模式如何实现可插拔扩展？
- **Skill 系统**：Markdown 格式的设计考量、如何注入 System Prompt、LRU 淘汰策略如何生效
- **Context Pruning**：滑动窗口 vs Token 预算各适用什么场景？工具结果截断如何保证不丢失关键信息？
- **AG-UI 协议**：为什么选择 SSE 而非 WebSocket？事件类型如何设计？状态管理如何实现？
- **AI 应用落地**：为什么选 LangChain4j 作为底层 LLM 框架（而非 Python 生态）、自研 Runtime 的分层设计思想、RAG 的 Embedding 模型与向量存储方案、`@Tool` 如何接入业务服务层
- **数据库优化**：复合索引如何对应具体查询路径、EXPLAIN 执行计划如何分析、回表成本如何量化
- **Redis 治理**：JSON 序列化方案、差异化 TTL 与随机过期、热点 Key 互斥重建机制
- **并发与稳定性**：WebSocket 异步线程池配置、`ConcurrentHashMap` 会话管理、ThreadLocal 清理
