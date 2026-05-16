<p align="center">
  <h1 align="center">🤖 灵食 AgentDine</h1>
  <p align="center">
    <strong>基于 LangChain4j + RAG 的多 Agent 智能餐饮协作平台</strong>
  </p>
  <p align="center">
    <img src="https://img.shields.io/badge/Java-11-blue.svg" alt="Java 11">
    <img src="https://img.shields.io/badge/Spring%20Boot-2.7.3-brightgreen.svg" alt="Spring Boot 2.7.3">
    <img src="https://img.shields.io/badge/LangChain4j-0.32.0-orange.svg" alt="LangChain4j 0.32.0">
    <img src="https://img.shields.io/badge/MySQL-8.0-blue.svg" alt="MySQL 8.0">
    <img src="https://img.shields.io/badge/Redis-7.0-red.svg" alt="Redis 7.0">
    <img src="https://img.shields.io/badge/license-MIT-green.svg" alt="License">
  </p>
</p>

---

## 📖 目录

- [项目简介](#项目简介)
- [系统架构](#系统架构)
- [核心特性](#核心特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [API 文档](#api-文档)
- [智能 Agent 系统](#智能-agent-系统)
- [性能优化](#性能优化)
- [部署指南](#部署指南)
- [相关文档](#相关文档)

---

## 项目简介

**灵食 AgentDine**是一款面向 B/C 双端场景的企业级智能餐饮服务平台，围绕商品、套餐、购物车、订单、配送状态、实时通知与经营分析形成完整交易闭环。项目创新性地引入基于 **LangChain4j + RAG** 的多 Agent 智能点餐系统，将用户交互从关键词检索升级为基于意图理解的智能推荐。

### 🎯 业务能力

| 维度 | 指标 |
|------|------|
| 用户规模 | 50,000+ |
| 菜品数量 | 3,000+ |
| 订单规模 | 200,000+ |
| 并发能力 | 300 QPS 稳定运行 |
| 核心错误率 | < 0.5% |
| 菜单推荐命中率 | 90%+ |

### 🏗️ 平台特点

- **双端架构** — C 端用户点餐 + B 端商家管理，JWT 双端独立鉴权
- **Agent 驱动** — 多 Agent 协作 (Orchestrator + 5 个 Specialist Agent)，支持意图路由与 A2A 通信
- **RAG 增强** — 基于向量检索的菜单语义搜索，KnowledgeLoader 自动建库
- **实时推送** — WebSocket 来单提醒 / 催单通知，独立线程池异步分发
- **缓存治理** — 热点菜单缓存命中率 94%+，缓存回源率 < 6%
- **工程完备** — AOP 自动填充、声明式事务、OSS 上传、Nginx 负载均衡

---

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                      前端层 (Frontend)                       │
│  ┌─────────────────────┐  ┌─────────────────────────────┐   │
│  │  agentdine-front  │  │  agentdine-admin-front    │   │
│  │  (C端 — 点餐/Agent) │  │  (B端 — 管理/报表)         │   │
│  └──────────┬──────────┘  └──────────────┬──────────────┘   │
└─────────────┼─────────────────────────────┼──────────────────┘
              │   AG-UI (SSE) + REST + WS   │
┌─────────────┼─────────────────────────────┼──────────────────┐
│             ▼              后端层 (Backend)                   │
│  ┌──────────────────────────────────────────────────────┐    │
│  │              AgentOrchestrator (编排层)               │    │
│  │         意图分类 → 路由调度 → A2A 通信                │    │
│  └────────────────────────┬─────────────────────────────┘    │
│          ┌──────┬─────────┼─────────┬──────────┬─────────┐  │
│          ▼      ▼         ▼         ▼          ▼         ▼  │
│     ┌────────┐┌──────┐┌──────┐┌────────┐┌────────┐        │
│     │点餐推荐││订单  ││客服  ││经营    ││后厨    │        │
│     │Agent  ││Agent ││Agent ││Agent   ││Agent   │        │
│     └────────┘└──────┘└──────┘└────────┘└────────┘        │
│  ┌──────────────────────────────────────────────────────┐    │
│  │        Agent Runtime (运行时框架)                     │    │
│  │  YAML配置 · 多模型 · Skill · Pruning · Tool/会话     │    │
│  └──────────────────────────────────────────────────────┘    │
│  ┌──────────────────────────────────────────────────────┐    │
│  │          交易核心 (sky-server)                        │    │
│  │  商品 · 套餐 · 购物车 · 订单 · 支付 · WebSocket      │    │
│  └──────────────────────────────────────────────────────┘    │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────┼─────────────────────────────────┐
│              ▼              数据层                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │ MySQL 8  │  │ Redis 7  │  │   OSS    │  │  Nginx   │    │
│  │ 交易+会话│  │ 缓存+状态│  │ 文件存储 │  │ 负载均衡 │    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │
└─────────────────────────────────────────────────────────────┘
```

---

## 核心特性

### 🤖 智能多 Agent 系统

- **主控 Orchestrator** — 关键词意图分类，自动路由到 5 个专业 Agent
- **FoodRecommendationAgent** — 基于 RAG 语义检索 + 用户偏好 + 历史订单的个性化推荐
- **OrderManagementAgent** — 订单查询、催单、取消、退款一站式处理
- **CustomerServiceAgent** — 投诉工单创建、退款政策查询、人工客服转接
- **BusinessAnalyticsAgent** — 营业额/用户/订单统计、销量 Top10、经营建议
- **KitchenDisplayAgent** — 后厨出餐队列、库存预警、出餐时间预估

### ⚡ 高性能交易链路

| 优化项 | 优化前 | 优化后 | 提升 |
|--------|--------|--------|------|
| 核心查询响应时间 | ~230ms | ~130ms | ↓43% |
| 慢查询数量 (典型批次) | 基线 | ↓75% | — |
| 菜单查询 QPS | ~260 | 520+ | ↑100% |
| 热点菜单缓存命中率 | — | 94% | — |
| 订单通知延迟 (平均) | ~650ms | <180ms | ↓72% |
| 订单通知 P95 | — | <300ms | — |

### 🔐 工程化能力

- **JWT 双端鉴权** — Admin (`token`) + User (`authentication`) 独立密钥与过期策略
- **AOP 自动填充** — `@AutoFill` 注解驱动 createTime/updateTime/createUser/updateUser
- **全局异常处理** — `@RestControllerAdvice` 统一捕获，`Result<T>` 统一响应体
- **声明式事务** — `@Transactional` 管理订单提交、支付回调等关键流程
- **OSS 文件上传** — 阿里云 OSS 集成，菜品/套餐图片管理

---

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 11 | 开发语言 |
| Spring Boot | 2.7.3 | 应用框架 |
| MyBatis | 2.2.0 | ORM 框架 |
| MySQL | 8.0 | 关系数据库 |
| Redis | 7.0 | 缓存 / 会话 / 状态 |
| LangChain4j | 0.32.0 | AI Agent 框架 |
| WebSocket (JSR 356) | — | 实时推送 |
| JWT (jjwt) | 0.9.1 | 身份认证 |
| Druid | 1.2.1 | 数据库连接池 |
| PageHelper | 1.3.0 | 物理分页 |
| Knife4j (Swagger) | 3.0.2 | API 文档 |
| FastJSON | 1.2.76 | JSON 序列化 |
| Apache POI | 3.16 | Excel 报表导出 |
| 阿里云 OSS SDK | 3.10.2 | 文件存储 |

### 前端 & 部署

| 技术 | 用途 |
|------|------|
| React 19 + TypeScript 6 + Vite 8 | C 端 / B 端前端 |
| Ant Design 6 | UI 组件库 |
| Zustand | 状态管理 |
| Nginx 1.25 | 反向代理 + 负载均衡 |
| Docker + Docker Compose | 容器化部署 |

---

## 项目结构

```
agentdine/
├── mp-weixin/                # 📱 C端微信小程序 (Vue 2 + uni-app)
│   ├── pages/
│   │   ├── index/            # 首页 — 菜品浏览、分类、购物车
│   │   ├── order/            # 下单结算
│   │   ├── agent/            # 🤖 AI 智能点餐对话页
│   │   ├── details/          # 订单详情
│   │   ├── pay/              # 微信支付
│   │   ├── success/          # 下单成功
│   │   ├── address/          # 地址管理
│   │   ├── my/               # 个人中心
│   │   └── historyOrder/     # 历史订单
│   ├── components/           # 公共组件
│   ├── static/               # 静态资源 (40+ 图标)
│   └── app.json              # 小程序配置
│
├── nginx-1.20.2/            # 🌐 Nginx 反向代理
│   ├── conf/nginx.conf       # 路由代理配置
│   └── html/sky/             # B端管理后台 SPA (Vue 2 + Element UI)
│
├── doc/                      # 📚 项目文档
│   ├── 多Agent智能外卖平台技术文档.md
│   ├── AGENT_DESIGN.md
│   └── 项目补充.md
│
├── docker-compose.yml        # 🐳 Docker 编排
├── nginx.conf                # 生产环境 Nginx 配置
│
└── sky-take-out/             # 🖥️ Java 后端
    ├── sky-common/           # 公共模块 — 常量、异常、工具类、JSON 序列化
│   └── src/main/java/com/sky/
│       ├── constant/        # 缓存键、JWT声明、消息常量
│       ├── context/         # ThreadLocal 用户上下文
│       ├── enumeration/     # 操作类型枚举
│       ├── exception/       # 业务异常类
│       ├── json/            # Jackson ObjectMapper
│       ├── properties/      # @ConfigurationProperties 配置类
│       ├── result/          # Result<T> 统一响应体
│       └── utils/           # JWT、OSS、HTTP、微信支付工具
│
├── sky-pojo/                # 传输对象 — Entity、DTO、VO
│   └── src/main/java/com/sky/
│       ├── dto/             # 数据传输对象 (30+)
│       ├── entity/          # 数据库实体 (12个)
│       └── vo/              # 视图对象 (20+)
│
├── sky-server/              # 主服务 — 控制器、服务、Mapper、Agent
│   └── src/main/java/com/sky/
│       ├── agent/           # 🤖 智能 Agent 系统
│       │   ├── orchestrator/     # 编排层 (路由/调度/A2A)
│       │   ├── runtime/          # 运行时框架
│       │   │   ├── config/       # YAML 配置解析
│       │   │   ├── factory/      # Agent 工厂
│       │   │   ├── memory/       # 用户偏好记忆
│       │   │   ├── model/        # 多模型 Provider
│       │   │   ├── protocol/     # AG-UI + A2A 协议
│       │   │   │   ├── agui/     # 前端流式交互
│       │   │   │   └── a2a/      # Agent 间通信
│       │   │   ├── pruning/      # Context 裁剪策略
│       │   │   ├── registry/     # Agent 注册中心
│       │   │   ├── session/      # 会话管理 (内存/Redis)
│       │   │   ├── skill/        # Skill 动态加载
│       │   │   └── tool/         # 工具注册表
│       │   ├── service/          # Agent 服务层
│       │   ├── specialist/       # 专业 Agent (5个)
│       │   └── tools/            # 工具集 (4个工具类)
│       ├── annotation/     # @AutoFill 自定义注解
│       ├── aspect/         # AOP 切面 (自动填充)
│       ├── config/         # 配置类 (Redis、WebSocket、OSS、异步线程池)
│       ├── controller/     # REST 控制器
│       │   ├── admin/      # 管理端 API
│       │   ├── user/       # 用户端 API
│       │   └── notify/     # 支付回调
│       ├── handler/        # 全局异常处理
│       ├── interceptor/    # JWT 拦截器 (Admin + User 双端)
│       ├── mapper/         # MyBatis Mapper 接口
│       ├── service/        # 业务服务层
│       ├── task/           # 定时任务 (订单超时 / WebSocket 心跳)
│       └── websocket/      # WebSocket 服务 (来单提醒 / 催单通知)
│   └── src/main/resources/
│       ├── application.yml          # 主配置
│       ├── application-dev.yml      # 开发环境
│       ├── application-agent.yml    # Agent 系统配置
│       ├── mapper/                  # MyBatis XML (13个)
│       └── skills/                  # Agent Skill Markdown (10个)
│
├── sql/                     # SQL 脚本
│   ├── sky_take_out.sql          # 基础建表
│   ├── agent_system_schema.sql   # Agent 系统表
│   └── performance_indexes.sql   # 性能优化索引
│
├── doc/                     # 📚 项目文档
├── docker-compose.yml       # Docker 编排
├── nginx.conf               # Nginx 配置
├── pom.xml                  # Maven 父 POM
└── README.md
```

---

## 快速开始

### 环境要求

- **JDK 11+**
- **Maven 3.6+**
- **MySQL 8.0**
- **Redis 7.0** (可选，可使用内存会话后端)
- **OpenAI API Key** (或兼容接口，如 DeepSeek / 通义千问)

### 1. 克隆项目

```bash
git clone <your-repo-url>
cd sky-take-out
```

### 2. 初始化数据库

```bash
# 创建数据库并导入基础表结构
mysql -u root -p < sql/sky_take_out.sql

# (可选) 导入 Agent 系统表
mysql -u root -p sky_take_out < sql/agent_system_schema.sql

# (可选) 导入性能优化索引
mysql -u root -p sky_take_out < sql/performance_indexes.sql
```

### 3. 配置环境变量

编辑 `sky-server/src/main/resources/application-dev.yml`：

```yaml
sky:
  datasource:
    host: localhost
    port: 3306
    database: sky_take_out
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379
    password: your_redis_password

langchain4j:
  open-ai:
    chat-model:
      api-key: ${OPENAI_API_KEY:your-api-key}
      model-name: gpt-3.5-turbo
```

Agent 系统配置在 `application-agent.yml` 中，支持自定义：

```yaml
sky:
  agent:
    session:
      backend: memory     # 开发环境使用内存
    pruning:
      strategy: token_budget
      max-context-tokens: 8000
    agents:
      - name: orchestrator
        display-name: 智能外卖助手
        # ... 更多 Agent 配置
```

### 4. 启动服务

```bash
# 方式一：Maven 启动
cd sky-server
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 方式二：Docker Compose 一键启动
docker-compose up -d
```

### 5. 验证

```bash
# 测试 Agent 对话接口
curl -X POST http://localhost:8080/user/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "推荐几个辣的菜"}'

# 测试 AG-UI 流式接口
curl -X POST http://localhost:8080/agui/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "帮我看看我的订单", "sessionId": "test-001"}'

# 查看 API 文档
open http://localhost:8080/doc.html
```

---

## API 文档

启动后访问 Swagger (Knife4j) 文档：

```
http://localhost:8080/doc.html
```

### 主要 API 分组

| 分组 | 前缀 | 说明 |
|------|------|------|
| C端-用户 | `/user/**` | 用户登录、菜品浏览、购物车、下单、地址簿 |
| C端-Agent | `/user/agent/**` | 智能对话 (多 Agent 路由) |
| AG-UI | `/agui/**` | 流式对话 + 状态管理 |
| B端-管理 | `/admin/**` | 菜品/套餐 CRUD、订单管理、报表、缓存监控 |
| 支付回调 | `/notify/**` | 微信支付成功回调 |
| WebSocket | `/ws/{sid}` | 实时推送 (来单提醒/催单) |

### Agent 对话示例

```json
// Request: POST /user/agent/chat
{
  "message": "我想吃辣的，预算30块以内"
}

// Response
{
  "code": 1,
  "msg": "success",
  "data": "根据您的口味偏好（辣味指数 0.8）和预算（30元以内），为您推荐：\n1. 水煮牛肉 — ¥25（川味经典，麻辣鲜香）\n2. 辣子鸡 — ¥22（干香麻辣，下饭首选）\n3. 麻辣小龙虾套餐 — ¥28（含米饭+饮料，性价比高）\n\n需要将哪个加入购物车？"
}
```

---

## 智能 Agent 系统

### Agent 架构

本项目从单体 LangChain4j Agent 演进为 **多 Agent 协作平台**，核心设计：

```
用户消息 → AgentOrchestrator (意图分类)
                │
    ┌───────────┼───────────┬───────────────┐
    ▼           ▼           ▼               ▼
点餐推荐    订单管理     客服工单       经营分析
Agent       Agent        Agent          Agent
    │           │           │               │
    └───────────┴───────────┴───────────────┘
                │
        Agent Runtime (YAML配置 · 多模型 · Skill · Pruning)
```

### 核心组件

| 组件 | 说明 |
|------|------|
| **AgentRegistry** | Agent 注册中心，管理所有 Agent 的元数据与实例 |
| **AgentFactory** | 基于 LangChain4j AiServices 的动态 Agent 工厂 |
| **AgentOrchestrator** | 意图分类 → Agent 路由 → A2A 通信 |
| **ToolRegistry** | 工具注册表，支持 `@Tool` 注解自动注册 |
| **SkillLoader** | Markdown 格式 Skill 动态加载，LRU 淘汰 |
| **SessionManager** | 会话管理，支持 Memory / Redis 后端 |
| **ContextPruningStrategy** | 滑动窗口 / Token 预算裁剪 |
| **ModelProviderFactory** | 多模型支持 (OpenAI / DeepSeek / Qwen) |

### Tool 工具集

| 工具类 | 方法 | 用途 |
|--------|------|------|
| `AgentTools` | `getOnSaleDishes` `getOnSaleSetmeals` `getUserLastOrders` | 菜单查询、历史订单 |
| `OrderTools` | `getUserOrders` `getOrderDetail` `remindOrder` `cancelOrder` | 订单管理 |
| `AnalyticsTools` | `getTurnoverStatistics` `getUserStatistics` `getOrderStatistics` `getSalesTop10` `getCacheMetrics` | 经营分析 |
| `CustomerServiceTools` | `createSupportTicket` `escalateToHuman` `getRefundPolicy` | 客服工单 |

### Skill 技能库

所有 Skill 以 Markdown 格式存储在 `resources/skills/`，运行时动态注入 Prompt：

| Skill | 用途 |
|-------|------|
| `chinese-cuisine-expert` | 中国八大菜系知识 |
| `dietary-constraint-checker` | 饮食禁忌检查 |
| `order-policy` | 订单状态与取消政策 |
| `refund-policy` | 退款条件矩阵 |
| `complaint-handling` | 投诉分级处理流程 |
| `business-analysis` | 经营分析方法论 |
| `report-generator` | 报告生成模板 |
| `kitchen-workflow` | 后厨出餐流程 |
| `intent-classifier` | 意图分类规则 |

### AG-UI 协议

前端通过 SSE (Server-Sent Events) 接收流式事件：

```
event: text_delta       → 文本增量
event: tool_call_start  → 工具调用开始
event: tool_call_result → 工具调用结果
event: state_delta      → 状态变更
event: done             → 对话完成
event: error            → 错误
```

---

## 性能优化

### 数据库优化

- **复合索引** — 订单分页 (`user_id + status + order_time`)、购物车查重 (`user_id + dish_id + setmeal_id`)、默认地址查询
- **关联索引** — 菜品分类索引 (`category_id + status`)、套餐分类索引
- **慢查询治理** — EXPLAIN 执行计划分析 → 索引命中校准 → 回表成本控制

### 缓存策略

- **热点菜单** — `@Cacheable` + 差异化 TTL (菜品 30-60min, 套餐 45-90min)
- **店铺状态** — 手动失效，永久缓存
- **缓存穿透防护** — 热点 Key 互斥重建，回源率 < 6%
- **指标监控** — `CacheMetricsService` 实时命中率统计

### 异步优化

- **WebSocket 推送解耦** — 独立线程池 `ws-notify-` (核心2, 最大4)
- **订单通知** — 从主链路剥离，`@Async` 异步分发
- **连接池调优** — Druid + Redis 连接池合理配置

---

## 部署指南

### Docker Compose 部署

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f sky-server
```

### Nginx 负载均衡

```nginx
upstream sky_server_cluster {
    server sky-server-1:8080 weight=1;
    server sky-server-2:8080 weight=1;
}

# SSE 流式连接 — 关闭缓冲
location /agui/chat {
    proxy_pass http://sky_server_cluster;
    proxy_buffering off;
    proxy_read_timeout 300s;
}

# WebSocket — 升级连接
location /ws/ {
    proxy_pass http://sky_server_cluster;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}
```

### 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `OPENAI_API_KEY` | OpenAI API 密钥 | — |
| `DEEPSEEK_API_KEY` | DeepSeek API 密钥 | — |
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 | — |
| `REDIS_PASSWORD` | Redis 密码 | — |
| `SPRING_PROFILES_ACTIVE` | Spring 环境 | `dev` |

---

## 相关文档

| 文档 | 说明 |
|------|------|
| [多Agent智能外卖平台技术文档](doc/多Agent智能外卖平台技术文档.md) | 完整多 Agent 系统架构设计 |
| [Agent 设计文档](doc/AGENT_DESIGN.md) | LangChain4j RAG Agent 详细方案 |
| [数据库 Schema](sky-take-out/sql/agent_system_schema.sql) | Agent 系统数据库表结构 |
| [性能优化索引](sky-take-out/sql/performance_indexes.sql) | 高频查询复合索引策略 |
| [Agent 配置](sky-take-out/sky-server/src/main/resources/application-agent.yml) | YAML 驱动 Agent 配置 |

---

## 开发计划

- [x] 交易核心链路 (商品/套餐/购物车/订单/支付)
- [x] LangChain4j RAG Agent 单点集成
- [x] 多 Agent 运行时框架 (YAML 驱动 / Factory / Registry / Skill / Pruning)
- [x] AgentOrchestrator 编排层 (意图分类 + A2A 路由)
- [x] 5 个 Specialist Agent (点餐/订单/客服/分析/后厨)
- [x] AG-UI 流式协议 (SSE + 状态管理)
- [x] 可观测性 (AgentMetrics + AuditLogger)
- [x] Docker Compose + Nginx 部署方案
- [ ] 前端 Agent 对话页面 (AG-UI 适配)
- [ ] OpenTelemetry 全链路追踪
- [ ] 多模态输入 (图片点餐、语音点餐)
- [ ] Agent 策略 A/B 实验平台

---

## 许可证

MIT License

---

<p align="center">
  <sub>Built with ❤️ using Spring Boot, LangChain4j, and Multi-Agent Architecture</sub>
</p>
