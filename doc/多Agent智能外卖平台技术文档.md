# 多Agent智能外卖平台技术文档（完整版）

## 一、整体系统架构

```
前端层 (Frontend)
├── sky-takeout-front (C端用户前端)
│   ├── 外卖点餐界面
│   ├── 智能点餐Agent对话界面
│   └── 订单追踪/催单界面
├── sky-takeout-admin-front (B端管理前端)
│   ├── 经营分析仪表盘
│   ├── 订单管理界面
│   └── 菜品/套餐管理界面
    ↓ AG-UI 协议 (SSE 流式 + JSON-RPC)
后端层 (Backend)
├── sky-server (主服务, SpringBoot 2.7.3)
│   ├── sky-takeout-core (交易核心模块)
│   │   ├── 商品/套餐/购物车/订单 CRUD
│   │   ├── WebSocket 实时推送
│   │   └── JWT 双端认证
│   ├── sky-agent-orchestrator (Agent 编排层)
│   │   ├── Agent 注册中心 (Factory/Registry)
│   │   ├── Agent 间路由与调度
│   │   └── 协议适配 (AG-UI + A2A)
│   └── sky-agent-runtime (Agent 运行时框架)
│       ├── YAML 驱动配置
│       ├── 多模型支持 (OpenAI/Gemini/DeepSeek/Qwen)
│       ├── Skill 系统
│       ├── Context Pruning
│       ├── 会话管理 (Redis / MySQL)
│       └── Tool 系统
            ↓
┌───────┴──────────────────────────────────────┐
│          多 Agent 集群 (Specialist Agents)       │
├──────────────────────────────────────────────┤
│  FoodRecommendationAgent  │  点餐推荐专家       │
│  OrderManagementAgent     │  订单管理专家       │
│  CustomerServiceAgent     │  客服工单专家       │
│  BusinessAnalyticsAgent   │  经营分析专家       │
│  KitchenDisplayAgent      │  后厨出餐专家       │
└──────────────────────────────────────────────┘
            ↓
数据层
├── MySQL 8.0 (交易数据 + 会话持久化)
├── Redis 7.0 (缓存 + 会话状态 + 消息队列)
├── Elasticsearch (菜单语义检索 + 日志)
└── OSS (图片/文件存储)
```

## 二、各项目详细分析

### 2.1 sky-takeout-front — C端用户前端

**核心功能：**
- 外卖点餐界面（菜品浏览、套餐选择、购物车、下单）
- 智能点餐 Agent 对话界面（流式对话、推荐卡片、快捷操作）
- 订单追踪与催单界面
- 地址簿管理

**技术栈：**
- React 19 + TypeScript 6 + Vite 8
- React Router 7（路由管理）
- Ant Design 6（UI 组件库）
- Zustand（全局状态管理）
- Axios（HTTP 请求）
- EventSource（SSE 流式接收）

**核心模块：**
- `src/routes/menu/` — 菜单浏览页面
- `src/routes/agent/` — Agent 对话页面
    - `components/chat/ChatBubble.tsx` — 自定义气泡渲染
    - `components/chat/FoodRecommendCard.tsx` — 菜品推荐卡片
    - `components/chat/OrderStatusCard.tsx` — 订单状态卡片
    - `components/chat/QuickActions.tsx` — 快捷操作按钮
- `src/routes/order/` — 订单页面
- `src/routes/address/` — 地址管理页面
- `src/stores/agentStore.ts` — Agent 对话状态管理
- `src/stores/cartStore.ts` — 购物车状态管理

**关键特性：**
- 支持 SSE 流式 Agent 响应
- 支持自定义消息气泡渲染（文本/推荐卡片/订单状态/快捷操作）
- 支持 AG-UI 协议（`/agui/chat`, `/agui/setState`, `/agui/getState`）
- 支持多轮对话上下文保持
- 支持语音输入（扩展规划）

**AG-UI 协议交互流程：**

```
POST /agui/chat
    ↓ SSE Stream
    ├── event: text_delta       (文本增量)
    ├── event: tool_call_start  (工具调用开始)
    ├── event: tool_call_result (工具调用结果)
    ├── event: state_delta      (状态变更)
    ├── event: ui_render        (UI 渲染指令)
    └── event: done             (完成)
```

### 2.2 sky-takeout-admin-front — B端管理前端

**核心功能：**
- 经营分析仪表盘（营业额、用户、订单统计图表）
- 菜品/套餐管理（CRUD、上下架、图片上传）
- 订单管理（接单、拒单、取消、配送、完成）
- 员工管理
- 缓存监控面板

**技术栈：**
- React 19 + TypeScript 6 + Vite 8
- React Router 7
- Ant Design 6 + Ant Design Charts（图表）
- Zustand

**核心组件：**
- `src/routes/dashboard/` — 经营分析仪表盘
    - `TurnoverChart.tsx` — 营业额折线图
    - `OrderStatsCard.tsx` — 订单统计卡片
    - `SalesTop10Chart.tsx` — 销量Top10柱状图
- `src/routes/dish/` — 菜品管理
- `src/routes/order/` — 订单管理
    - `OrderStatusTimeline.tsx` — 订单状态时间线
- `src/routes/cache/` — 缓存监控面板

### 2.3 sky-server — 后端主服务

**核心功能：**
- 提供交易核心链路服务（商品/套餐/购物车/订单/支付）
- 实现多 Agent 编排与运行时框架
- 提供 AG-UI 协议流式接口
- 提供 A2A 协议 Agent 间通信
- 支持会话管理与状态持久化
- 知识库（菜单）同步与管理
- WebSocket 实时推送（来单提醒、催单通知）

**技术架构：**
- Spring Boot 2.7.3 + MyBatis + MySQL 8.0
- Redis 7.0（缓存 + 会话 + 消息队列）
- LangChain4j（Agent 核心框架）
- WebSocket（实时推送）
- JWT（双端认证）
- AOP（公共字段自动填充 + 缓存切面）
- OpenTelemetry（链路追踪）

**核心模块（改造后）：**

```
sky-server/src/main/java/com/sky/
├── SkyApplication.java                 # SpringBoot 启动类
├── agent/
│   ├── orchestrator/                   # Agent 编排层
│   │   ├── AgentOrchestrator.java      # 编排器（路由/调度/协调）
│   │   ├── AgentRegistry.java         # Agent 注册中心
│   │   ├── AgentFactory.java          # Agent 工厂
│   │   └── AgentMetadata.java         # Agent 元数据
│   ├── runtime/                        # Agent 运行时
│   │   ├── RuntimeConfig.java         # 运行时配置（YAML 驱动）
│   │   ├── model/
│   │   │   ├── ModelProvider.java     # 模型提供者接口
│   │   │   ├── OpenAiModelProvider.java
│   │   │   ├── GeminiModelProvider.java
│   │   │   ├── DeepSeekModelProvider.java
│   │   │   └── QwenModelProvider.java
│   │   ├── session/
│   │   │   ├── SessionManager.java    # 会话管理接口
│   │   │   ├── RedisSessionManager.java
│   │   │   ├── JdbcSessionManager.java
│   │   │   └── InMemorySessionManager.java
│   │   ├── memory/
│   │   │   ├── MemoryManager.java     # 记忆管理
│   │   │   └── UserPreferenceMemory.java
│   │   ├── skill/
│   │   │   ├── SkillLoader.java       # Skill 动态加载器
│   │   │   ├── SkillRegistry.java     # Skill 注册表
│   │   │   └── SkillContext.java      # Skill 上下文
│   │   ├── pruning/
│   │   │   ├── SlidingWindowPruner.java
│   │   │   ├── TokenBudgetPruner.java
│   │   │   ├── ToolResultTruncator.java
│   │   │   └── ContextPruningStrategy.java
│   │   ├── tool/
│   │   │   ├── ToolRegistry.java      # 工具注册表
│   │   │   ├── ToolInvoker.java       # 工具调用器
│   │   │   └── ToolResultFormatter.java
│   │   ├── protocol/
│   │   │   ├── agui/
│   │   │   │   ├── AguiController.java    # AG-UI 协议端点
│   │   │   │   ├── AguiEventHandler.java # SSE 事件处理
│   │   │   │   └── AguiStateManager.java # 状态管理
│   │   │   └── a2a/
│   │   │       ├── A2aClient.java         # A2A 客户端
│   │   │       ├── A2aServer.java         # A2A 服务端
│   │   │       └── A2aMessage.java        # A2A 消息格式
│   │   └── config/
│   │       └── AgentsConfig.java      # YAML 配置解析
│   ├── specialist/                     # 专业 Agent 实现
│   │   ├── FoodRecommendationAgent.java    # 点餐推荐
│   │   ├── OrderManagementAgent.java       # 订单管理
│   │   ├── CustomerServiceAgent.java       # 客服工单
│   │   ├── BusinessAnalyticsAgent.java     # 经营分析
│   │   └── KitchenDisplayAgent.java        # 后厨出餐
│   ├── shared/                         # Agent 共享组件
│   │   ├── ToolProvider.java           # 共享工具提供者
│   │   ├── KnowledgeBase.java          # 知识库管理
│   │   └── PromptTemplates.java        # 提示词模板
│   └── config/
│       ├── LangChain4jConfig.java      # LangChain4j 全局配置
│       └── AgentOrchestratorConfig.java # 编排器配置
├── controller/                         # 原有控制器（保持不变）
├── service/                            # 原有服务层（保持不变）
├── mapper/                             # 原有 Mapper（保持不变）
├── config/                             # 原有配置（保持不变）
├── interceptor/                        # JWT 拦截器（保持不变）
├── websocket/                          # WebSocket（保持不变）
└── task/                               # 定时任务（保持不变）
```

#### Agent 注册中心设计：

```java
// AgentRegistry — 管理所有 Agent 的注册与发现
@Component
public class AgentRegistry {
    // agentName -> AgentMetadata 映射
    private final ConcurrentHashMap<String, AgentMetadata> agents = new ConcurrentHashMap<>();

    // agentName -> AiServices 实例
    private final ConcurrentHashMap<String, Object> agentInstances = new ConcurrentHashMap<>();

    public void register(AgentMetadata metadata, Object agentInstance) { ... }
    public AgentMetadata getMetadata(String agentName) { ... }
    public Object getInstance(String agentName) { ... }
    public List<AgentMetadata> listAll() { ... }
    public List<AgentMetadata> listByCapability(String capability) { ... }
}

// AgentMetadata — 描述每个 Agent 的元数据
public class AgentMetadata {
    private String name;              // food-recommendation
    private String displayName;       // 点餐推荐Agent
    private String description;       // 基于用户偏好与菜单数据，智能推荐菜品
    private List<String> capabilities;// ["dish.recommend", "menu.search", "preference.analysis"]
    private String model;             // gpt-4o
    private List<String> tools;       // ["getOnSaleDishes", "getUserPreferences"]
    private List<String> skills;      // ["chinese-cuisine-expert"]
    private List<String> subAgents;   // ["nutrition-analyzer"]
    private int maxTokens;            // 4096
    private float temperature;        // 0.7
}
```

#### YAML 驱动配置文件设计（agents.yaml）：

```yaml
# agents.yaml — Agent System Configuration
version: "1.0"

# 全局模型配置
models:
  default:
    provider: openai
    model: gpt-4o
    temperature: 0.7
    maxTokens: 4096
  fast:
    provider: deepseek
    model: deepseek-chat
    temperature: 0.3
    maxTokens: 2048
  vision:
    provider: openai
    model: gpt-4o-mini
    temperature: 0.5
    maxTokens: 2048

# 会话配置
session:
  backend: redis                # 可选: redis, jdbc, memory
  ttl: 3600                     # 秒
  maxMessagesPerSession: 50

# 记忆配置
memory:
  backend: redis
  userPreferenceTtl: 1209600    # 14天

# Context Pruning 配置
pruning:
  strategy: token_budget        # 可选: sliding_window, token_budget
  maxContextTokens: 8000
  maxToolResultLength: 2000
  keepSystemPrompt: true

# Agent 定义
agents:
  # ===== 主控 Agent (Orchestrator) =====
  - name: orchestrator
    displayName: 智能外卖助手
    description: 主控Agent，负责理解用户意图并路由到专业Agent
    model: default
    systemPrompt: |
      你是苍穹外卖智能助手。请分析用户意图：
      1. 点餐相关 → 委托给 food-recommendation
      2. 订单查询/修改 → 委托给 order-management
      3. 投诉/退款/问题 → 委托给 customer-service
      4. 经营数据查询 → 委托给 business-analytics
    tools:
      - transferToAgent
    skills:
      - intent-classifier
    subAgents:
      - food-recommendation
      - order-management
      - customer-service
      - business-analytics

  # ===== 点餐推荐 Agent =====
  - name: food-recommendation
    displayName: 点餐推荐助手
    description: 基于用户偏好、历史订单与实时菜单，智能推荐菜品
    model: default
    temperature: 0.7
    systemPrompt: |
      你是苍穹外卖专业点餐推荐助手。请根据：
      1. 用户的口味偏好与历史订单
      2. 当前在售菜品与套餐信息
      3. 用户提出的具体需求（预算、口味、人数等）
      推荐最合适的菜品。不要推荐已下架的菜品。
      回复格式：
      - 先简要分析用户的偏好
      - 给出 3-5 个推荐项，含价格和推荐理由
      - 最后询问是否需要调整或下单
    tools:
      - getOnSaleDishes
      - getOnSaleSetmeals
      - getUserLastOrders
      - getUserPreferences
      - semanticMenuSearch
      - addToCart
    skills:
      - chinese-cuisine-expert
      - dietary-constraint-checker
    contextRetriever:
      maxResults: 5
      minScore: 0.5
    chatMemory:
      maxMessages: 20

  # ===== 订单管理 Agent =====
  - name: order-management
    displayName: 订单管理助手
    description: 处理订单查询、催单、取消、退款等订单相关操作
    model: fast
    temperature: 0.3
    systemPrompt: |
      你是苍穹外卖订单管理助手。你可以帮助用户：
      1. 查询订单状态和历史
      2. 发起催单请求
      3. 处理订单取消与退款
      4. 解答配送相关问题
      请根据用户提供的订单号或最近订单进行处理。
    tools:
      - getUserOrders
      - getOrderDetail
      - cancelOrder
      - remindOrder
      - getDeliveryStatus
    skills:
      - order-policy
    chatMemory:
      maxMessages: 15

  # ===== 客服工单 Agent =====
  - name: customer-service
    displayName: 客服助手
    description: 处理用户投诉、退款申请、问题反馈
    model: default
    temperature: 0.5
    systemPrompt: |
      你是苍穹外卖客服助手。请耐心处理用户的问题：
      1. 记录用户投诉与反馈
      2. 根据退款政策判断是否满足退款条件
      3. 创建工单并跟踪处理进度
      4. 对于无法处理的问题，转接人工客服
    tools:
      - createSupportTicket
      - getUserOrders
      - getRefundPolicy
      - escalateToHuman
    skills:
      - complaint-handling
      - refund-policy
    chatMemory:
      maxMessages: 30

  # ===== 经营分析 Agent =====
  - name: business-analytics
    displayName: 经营分析助手
    description: 提供经营数据分析、销售趋势预测、菜品优化建议
    model: default
    temperature: 0.2
    systemPrompt: |
      你是苍穹外卖经营分析助手。你可以帮助商家：
      1. 查询营业额、用户、订单统计
      2. 分析销量Top10和滞销菜品
      3. 给出经营优化建议
      4. 生成指定时段经营报告
    tools:
      - getTurnoverStatistics
      - getUserStatistics
      - getOrderStatistics
      - getSalesTop10Report
      - generateBusinessReport
      - getCacheMetrics
    skills:
      - business-analysis
      - report-generator
    chatMemory:
      maxMessages: 10

  # ===== 后厨出餐 Agent =====
  - name: kitchen-display
    displayName: 后厨助手
    description: 管理后厨出餐队列、库存预警、菜品制作状态
    model: fast
    temperature: 0.1
    systemPrompt: |
      你是苍穹外卖后厨助手。你可以：
      1. 展示当前待制作订单队列
      2. 更新菜品制作状态
      3. 发出库存/售罄预警
      4. 计算预计出餐时间
    tools:
      - getPendingOrders
      - updateOrderStatus
      - getInventoryStatus
      - estimatePreparationTime
    skills:
      - kitchen-workflow
    chatMemory:
      maxMessages: 20
```

#### Agent 间通信协议 (A2A)：

```java
// A2A 消息格式
public class A2aMessage {
    String messageId;           // UUID
    String fromAgent;           // 发送方 Agent 名称
    String toAgent;             // 接收方 Agent 名称
    String type;                // QUERY, RESULT, TRANSFER, BROADCAST
    String content;             // 消息内容
    Map<String, Object> context; // 上下文（用户ID、会话ID等）
    Map<String, String> metadata; // 元数据（优先级、超时等）
    long timestamp;             // 时间戳
}

// AgentOrchestrator 中的路由逻辑
public AgentResponse routeAndExecute(A2aMessage message) {
    // 1. 提取用户意图
    String intent = classifyIntent(message.getContent());

    // 2. 匹配目标 Agent
    AgentMetadata target = registry.matchByCapability(intent);

    // 3. 委托给目标 Agent
    if (target.getSubAgents() != null && !target.getSubAgents().isEmpty()) {
        return orchestrateMultiAgent(target, message);
    } else {
        return invokeDirectly(target, message);
    }
}
```

#### Skill 系统设计：

Skill 是 Markdown 格式的动态指令包，可在运行时加载/卸载，用于增强 Agent 的领域知识：

```
skills/
├── chinese-cuisine-expert.md    # 中国菜系知识
├── dietary-constraint-checker.md # 饮食禁忌检查
├── order-policy.md              # 订单政策
├── complaint-handling.md        # 投诉处理流程
├── refund-policy.md             # 退款政策
├── business-analysis.md         # 经营分析方法
├── report-generator.md          # 报告生成模板
├── kitchen-workflow.md          # 后厨工作流
└── intent-classifier.md         # 意图分类规则
```

Skill 生命周期管理：
- `session` — 会话级别，对话结束释放
- `turn` — 单轮对话级别
- `choice` — 单次选择级别

```java
// SkillLoader — 从 classpath 或外部路径动态加载 Skill
@Component
public class SkillLoader {
    private final Map<String, SkillContext> loadedSkills = new LRUMap<>(50);

    public SkillContext load(String skillName) { ... }
    public void unload(String skillName) { ... }
    public String injectSkillIntoPrompt(String basePrompt, List<String> skillNames) { ... }
}
```

#### Context Pruning 策略：

```java
// 策略接口
public interface ContextPruningStrategy {
    List<ChatMessage> prune(List<ChatMessage> messages, int maxTokens);
}

// 滑动窗口策略：保留最近N轮对话
public class SlidingWindowPruner implements ContextPruningStrategy {
    private final int maxTurns;  // 默认10轮

    @Override
    public List<ChatMessage> prune(List<ChatMessage> messages, int maxTokens) {
        // 保留 system prompt + 最近 N 轮对话
    }
}

// Token 预算策略：按 Token 数量动态裁剪
public class TokenBudgetPruner implements ContextPruningStrategy {
    private final Tokenizer tokenizer;

    @Override
    public List<ChatMessage> prune(List<ChatMessage> messages, int maxTokens) {
        // 按 token 预算裁剪，保留 system prompt + 尽可能多的最近消息
    }
}

// 工具结果截断器
public class ToolResultTruncator {
    // 超过 maxLength 的工具调用结果自动截断，保留前N字符 + 后N字符
    public String truncate(String toolResult, int maxLength) { ... }
}
```

### 2.4 Agent 运行时框架详细设计

#### 核心子系统：

| 子系统 | 功能 | 注册方式 |
|---|---|---|
| agent | Agent 创建、注册、生命周期管理 | `AgentRegistry.register()` |
| model | 模型后端（OpenAI/Gemini/DeepSeek/Qwen） | `ModelProviderFactory` |
| session | 会话管理（内存/Redis/MySQL） | `SessionManager` 接口 |
| memory | 用户偏好记忆服务 | `MemoryManager` 接口 |
| tool | 工具注册、调用、结果格式化 | `ToolRegistry.register()` |
| skill | Markdown 动态指令包加载 | `SkillLoader.load()` |
| pruning | Context 裁剪策略 | `ContextPruningStrategy` 接口 |
| retriever | RAG 知识库检索 | `ContentRetriever` Bean |
| protocol | AG-UI / A2A 协议适配 | 协议层独立组件 |

#### 多模型支持：

```yaml
# 在 agents.yaml 中配置
models:
  openai-gpt4o:
    provider: openai
    apiKey: ${OPENAI_API_KEY}
    modelName: gpt-4o
    baseUrl: https://api.openai.com
    timeout: 60s
  deepseek-chat:
    provider: deepseek
    apiKey: ${DEEPSEEK_API_KEY}
    modelName: deepseek-chat
    baseUrl: https://api.deepseek.com
    timeout: 30s
  qwen-max:
    provider: qwen
    apiKey: ${QWEN_API_KEY}
    modelName: qwen-max
    baseUrl: https://dashscope.aliyuncs.com/compatible-mode/v1
    timeout: 30s
```

#### 会话管理（分布式）：

```java
// Redis 会话管理器 — 支持分布式部署
@Component
@ConditionalOnProperty(name = "agent.session.backend", havingValue = "redis")
public class RedisSessionManager implements SessionManager {
    // Key: agent:session:{sessionId}:messages
    // 使用 Redis List 存储消息历史
    // 支持 TTL 自动过期
    // 支持 LRU 淘汰

    @Override
    public List<ChatMessage> getMessages(String sessionId) { ... }

    @Override
    public void addMessage(String sessionId, ChatMessage message) { ... }

    @Override
    public void clearSession(String sessionId) { ... }

    @Override
    public void setState(String sessionId, String key, Object value) { ... }

    @Override
    public Object getState(String sessionId, String key) { ... }
}
```

### 2.5 数据库设计（新增表）

```sql
-- Agent 会话表
CREATE TABLE agent_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL UNIQUE,
    agent_name VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    status TINYINT DEFAULT 1,        -- 1:active, 0:archived
    message_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_agent (user_id, agent_name),
    INDEX idx_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Agent 消息历史表（用于 MySQL 会话后端）
CREATE TABLE agent_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    role VARCHAR(16) NOT NULL,        -- system, user, assistant, tool
    content TEXT NOT NULL,
    tool_calls JSON,
    metadata JSON,
    token_count INT DEFAULT 0,
    created_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_session_time (session_id, created_at),
    FOREIGN KEY (session_id) REFERENCES agent_sessions(session_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户偏好记忆表
CREATE TABLE user_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    taste_preferences JSON,          -- {"spicy": 0.8, "sweet": 0.3, ...}
    favorite_categories JSON,        -- [1, 3, 5]
    budget_range VARCHAR(32),        -- "15-30"
    dietary_restrictions JSON,       -- ["no_peanut", "vegetarian"]
    order_frequency INT DEFAULT 0,
    avg_order_amount DECIMAL(10,2),
    preferred_time_slots JSON,       -- ["11:00-12:00", "17:30-18:30"]
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Agent 工具调用审计日志
CREATE TABLE agent_tool_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    agent_name VARCHAR(64) NOT NULL,
    tool_name VARCHAR(128) NOT NULL,
    tool_input JSON,
    tool_output TEXT,
    execution_time_ms INT,
    status VARCHAR(16),              -- success, error, timeout
    error_message TEXT,
    created_at DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_session (session_id),
    INDEX idx_agent_time (agent_name, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 客服工单表（CustomerServiceAgent 使用）
CREATE TABLE support_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(64),
    ticket_type VARCHAR(32),         -- complaint, refund, feedback, other
    status VARCHAR(16) DEFAULT 'open', -- open, processing, resolved, closed, escalated
    priority TINYINT DEFAULT 2,      -- 1:low, 2:normal, 3:high, 4:urgent
    title VARCHAR(256),
    description TEXT,
    resolution TEXT,
    assigned_to VARCHAR(64),
    escalated_to_human BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME,
    INDEX idx_user (user_id),
    INDEX idx_status (status, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 三、项目间关系

### 3.1 前端与后端交互

```
sky-takeout-front / sky-takeout-admin-front
        ↓ (AG-UI 协议)
        ↓ (SSE 流式响应 + JSON-RPC 状态管理)
sky-server (后端主服务)
        ├── POST /agui/chat         (流式对话)
        ├── POST /agui/setState      (状态写入)
        ├── POST /agui/getState      (状态读取)
        ├── REST /api/**             (交易核心 API)
        ├── WS /ws/{sid}             (WebSocket 实时推送)
        └── POST /notify/paySuccess  (微信支付回调)
                ↓ (A2A 协议)
        [Agent 编排层 内部调度]
```

### 3.2 Agent 间通信流程

```
用户输入 → AgentOrchestrator (主控 Agent)
        ↓ 意图分类
        ├── "我想点个辣的" → FoodRecommendationAgent
        │       ↓ 需要营养分析
        │   [A2A] → NutritionAnalyzer (子Agent)
        │       ↓ 返回推荐结果
        │       ↓ 用户确认下单
        │   [A2A] → OrderManagementAgent
        │
        ├── "我的订单到哪了" → OrderManagementAgent
        │       ↓ 需要配送信息
        │   [Tool] → getDeliveryStatus
        │       ↓ 配送到店
        │   [A2A] → KitchenDisplayAgent (通知后厨)
        │
        ├── "我要投诉" → CustomerServiceAgent
        │       ↓ 查询订单
        │   [A2A] → OrderManagementAgent
        │       ↓ 需要退款
        │   [Tool] → createSupportTicket + processRefund
        │       ↓ 复杂问题
        │   [Tool] → escalateToHuman (转人工)
        │
        └── "今天的销售情况" → BusinessAnalyticsAgent
                ↓ 查询数据
            [Tool] → getTurnoverStatistics, getOrderStatistics
                ↓ 生成图表
            [Tool] → generateBusinessReport
```

### 3.3 Agent 运行时框架支撑

```
所有 Specialist Agent
        ↓ (import / @Autowired)
sky-agent-runtime (框架库)
        ├── AgentRegistry        (注册中心)
        ├── AgentFactory         (工厂创建)
        ├── ModelProviderFactory (模型工厂)
        ├── SessionManager       (会话管理)
        ├── MemoryManager        (记忆管理)
        ├── SkillLoader          (技能加载)
        ├── ContextPruner        (上下文裁剪)
        └── ToolRegistry         (工具注册)
                ↓ (封装 / 集成)
        LangChain4j (核心 Agent 框架)
                ↓ (调用)
        LLM APIs (OpenAI / DeepSeek / Qwen / ...)
```

## 四、核心技术亮点

### 4.1 协议支持
- **AG-UI 协议** — 标准化 Agent-前端流式交互（SSE + JSON-RPC 状态管理）
- **A2A 协议** — Agent 间通信协议（JSON 消息 + 异步回调）
- **HTTP/SSE** — 服务器推送事件，低延迟流式响应
- **WebSocket** — 双向实时推送（来单提醒、催单通知）
- **REST** — 交易核心 API（商品/订单/支付等）

### 4.2 配置驱动
- **YAML 配置文件**（agents.yaml）驱动 Agent 创建
- 支持动态加载/卸载 Skill（Markdown 指令包）
- 支持多模型后端切换（OpenAI/Gemini/DeepSeek/Qwen）
- 支持多会话后端切换（内存/Redis/MySQL）
- 支持环境变量注入（API Key、连接串等）
- 支持运行时热重载（通过配置中心）

### 4.3 可扩展性
- **Factory/Registry 模式** — Agent/Model/Session/Tool/Skill 均可插拔
- **插件系统** — 支持第三方 Agent 扩展
- **工具系统** — `@Tool` 注解自动注册，支持同步/异步调用
- **技能系统** — Markdown 格式动态指令包，LRU 淘汰
- **子 Agent 委托** — Agent 可声明 subAgents，实现分层协作

### 4.4 可观测性
- OpenTelemetry 集成 — 全链路追踪
- Agent 工具调用审计日志（agent_tool_logs 表）
- 缓存命中率/回源率实时监控
- Prometheus 指标暴露（QPS、延迟、错误率）
- 慢查询分析 + EXPLAIN 执行计划

### 4.5 性能优化
- **Context Pruning** — 滑动窗口 + Token 预算 + 工具结果截断
- **Redis 缓存** — 热点菜单缓存命中率 94%+，回源率 <6%
- **异步通知** — WebSocket 推送独立线程池，P95 <300ms
- **复合索引** — 订单分页/超时扫描/购物车查重/默认地址查询
- **连接池调优** — Druid + Redis 连接池合理配置

### 4.6 安全与工程化
- JWT 双端鉴权（admin + user）
- AOP 公共字段自动填充（创建/更新时间、操作人）
- 声明式事务管理
- 全局异常处理 + 统一响应体（Result<T>）
- SQL 注入防护（MyBatis 参数化查询）
- OSS 文件上传 + CDN 加速

## 五、部署架构

### 5.1 开发环境

```
# 后端服务
cd sky-server
mvn spring-boot:run -Dspring.profiles.active=dev

# C端前端
cd sky-takeout-front
npm run dev            # 端口 5173

# B端前端
cd sky-takeout-admin-front
npm run dev            # 端口 5174

# 中间件
docker-compose up -d   # MySQL 3306, Redis 6379, Elasticsearch 9200
```

### 5.2 生产环境（Docker 部署）

```yaml
# docker-compose.yml
version: '3.8'
services:
  sky-server:
    build: ./sky-server
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - DEEPSEEK_API_KEY=${DEEPSEEK_API_KEY}
    depends_on:
      - mysql
      - redis
      - elasticsearch

  sky-takeout-front:
    build: ./sky-takeout-front
    ports:
      - "80:80"
    depends_on:
      - sky-server

  sky-takeout-admin-front:
    build: ./sky-takeout-admin-front
    ports:
      - "81:80"
    depends_on:
      - sky-server

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7.0-alpine
    command: redis-server --appendonly yes

  elasticsearch:
    image: elasticsearch:8.0.0
    environment:
      - discovery.type=single-node

  nginx:
    image: nginx:1.25
    ports:
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl

volumes:
  mysql_data:
```

### 5.3 Nginx 负载均衡

```nginx
upstream sky_server_cluster {
    server sky-server-1:8080 weight=1;
    server sky-server-2:8080 weight=1;
    ip_hash;  # 会话保持
}

server {
    listen 443 ssl;
    server_name api.sky-takeout.com;

    # SSE 流式连接特殊配置
    location /agui/chat {
        proxy_pass http://sky_server_cluster;
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 300s;
        chunked_transfer_encoding on;
    }

    location /ws/ {
        proxy_pass http://sky_server_cluster;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    location / {
        proxy_pass http://sky_server_cluster;
    }
}
```

## 六、Agent 工作流程示例

### 6.1 智能点餐推荐流程（完整链路）

```
用户: "我想吃辣的，预算30块以内，有什么推荐？"
    ↓
[1] AgentOrchestrator 意图分类
    → 识别为 "dish.recommend" → 路由到 FoodRecommendationAgent
    ↓
[2] FoodRecommendationAgent 分析需求
    ├── 提取关键词: 辣、预算30
    ├── Tool: getUserPreferences(userId)
    │   → 返回: {taste: {spicy: 0.8}, budget: "15-30", favorites: [1,3]}
    ├── Tool: getUserLastOrders(userId)
    │   → 返回: ["麻辣香锅", "水煮鱼", "口水鸡"]
    ├── Tool: getOnSaleDishes()
    │   → 返回: [麻辣小龙虾(28元), 水煮牛肉(25元), 辣子鸡(22元), ...]
    └── RAG: semanticMenuSearch("辣 30元以内")
        → 返回: 相关菜品向量 Top5
    ↓
[3] LLM 推理生成推荐
    → 分析: 用户偏好辣味(0.8)，历史点过川菜，预算30元
    → 推荐:
      1. 水煮牛肉(25元) — 川味经典，您之前点过类似菜品
      2. 辣子鸡(22元) — 麻辣鲜香，在预算内
      3. 麻辣小龙虾套餐(28元) — 含米饭+饮料，性价比高
    → 附带: 快捷操作按钮 [加入购物车] [换一批] [调整口味]
    ↓
[4] 用户: "第一个和第三个加入购物车"
    → Tool: addToCart(dishes=[{id:101, qty:1}, {id:301, qty:1}])
    → A2A → OrderManagementAgent 创建订单草稿
    → 返回: 购物车摘要 + [去结算] 按钮
```

### 6.2 多 Agent 协作流程（催单场景）

```
用户: "我的订单怎么还没到？催一下"
    ↓
[1] AgentOrchestrator → 路由到 OrderManagementAgent
    ↓
[2] OrderManagementAgent
    ├── Tool: getUserOrders(userId, status="DELIVERY_IN_PROGRESS")
    │   → 返回: {orderId: 8823, orderNumber: "SK202405161234", status: 4}
    ├── Tool: getDeliveryStatus(orderId=8823)
    │   → 返回: {rider: "张三", eta: "15分钟", location: "距您1.2km"}
    ├── Tool: remindOrder(orderId=8823)
    │   → WebSocket: 向商家推送催单通知 (type=2)
    │   → 返回: "已催单，商家已收到提醒"
    └── 生成回复: "您的订单SK202405161234正在配送中，骑手张三距您约1.2公里，预计15分钟送达。已为您向商家发送催单提醒。"
    ↓
[3] 后台: WebSocketNotificationService
    → @Async 异步推送到商家端 WebSocket
    → 日志记录到 agent_tool_logs
```

### 6.3 经营分析 Agent 工作流程

```
商家: "分析上周的销售情况，生成经营报告"
    ↓
[1] AgentOrchestrator → 路由到 BusinessAnalyticsAgent
    ↓
[2] BusinessAnalyticsAgent
    ├── Tool: getTurnoverStatistics(begin="2026-05-09", end="2026-05-16")
    │   → 返回: 每日营业额数据
    ├── Tool: getOrderStatistics(begin="2026-05-09", end="2026-05-16")
    │   → 返回: 每日订单数、有效订单数、完成率
    ├── Tool: getSalesTop10(begin="2026-05-09", end="2026-05-16")
    │   → 返回: 销量Top10菜品
    └── Tool: generateBusinessReport(data)
        → 生成结构化报告 + 经营建议
    ↓
[3] 返回报告:
    "## 上周经营分析报告 (5/9-5/16)
    ### 关键指标
    - 总营业额: ¥158,320 (↑12.5%)
    - 有效订单: 2,847单 (↑8.3%)
    - 订单完成率: 96.2% (↑1.1%)

    ### 销量Top3
    1. 麻辣小龙虾 — 486单
    2. 番茄牛腩饭 — 412单
    3. 宫保鸡丁套餐 — 389单

    ### AI 建议
    - 周末(5/13-14) 营业额下降8%，建议推出周末特惠活动
    - 饮品搭配率仅23%，建议优化套餐搭配策略
    - 麻婆豆腐连续3周销量增长，可考虑作为主推菜品"
```

## 七、关键技术决策

### 为什么选择 LangChain4j 作为核心 Agent 框架？

- Java 生态原生支持，与 Spring Boot 无缝集成
- 内置 ChatMemory、ContentRetriever（RAG）、Tool Calling 支持
- 支持多种 LLM 后端（OpenAI、Gemini、DeepSeek、Ollama 等）
- 提供 `@AiService` 和 `@Tool` 注解，声明式 Agent 定义
- 社区活跃，持续更新

### 为什么选择 Spring Boot + MyBatis？

- 团队技能栈匹配，学习成本低
- Spring 生态丰富（Cache、Async、Scheduling、AOP、Security）
- MyBatis 灵活控制 SQL，便于性能调优
- 成熟的中间件集成（Redis、Elasticsearch、WebSocket）

### 为什么选择 YAML 配置驱动？

- 声明式配置，易于理解和维护
- 支持动态加载（配置中心）
- 支持环境变量注入（K8s ConfigMap/Secret）
- 支持配置校验（启动时检查完整性）
- 非技术人员也能调整 Agent 行为（修改 System Prompt）

### 为什么选择 AG-UI 协议？

- 标准化的 Agent-前端交互协议
- 支持 SSE 流式响应，低延迟
- 支持状态管理（setState/getState）
- 支持 UI 渲染指令（推荐卡片、订单状态卡）
- 支持多轮对话上下文保持

### 为什么选择 A2A 协议？

- Agent 间通信标准化
- 支持 Agent 委托与结果汇总
- 支持异步回调
- 支持分布式 Agent 部署
- 可追踪、可审计

## 八、改造实施计划

### Phase 1: Agent Runtime 框架搭建（1-2周）
- [x] 梳理现有 Agent 代码
- [ ] 创建 `sky-agent-runtime` 模块（YAML 配置、Agent 注册中心、工厂模式）
- [ ] 实现 `AgentRegistry` + `AgentFactory`
- [ ] 实现 YAML 配置解析器（`AgentsConfig.java`）
- [ ] 实现多模型 Provider 工厂
- [ ] 实现 Redis 会话管理器（`RedisSessionManager`）
- [ ] 实现 Skill 加载器（`SkillLoader` + `SkillRegistry`）
- [ ] 实现 Context Pruning 策略

### Phase 2: A2A 协议与编排层（1周）
- [ ] 实现 A2A 消息格式与客户端/服务端
- [ ] 实现 `AgentOrchestrator` 编排器（意图分类 + 路由 + 委托）
- [ ] 实现意图分类器（可先基于关键词规则，后续迁移到 LLM）
- [ ] 实现 `transferToAgent` 工具

### Phase 3: 专业 Agent 扩展（2周）
- [ ] 重构 `FoodRecommendationAgent`（接入 YAML 配置 + A2A）
- [ ] 新增 `OrderManagementAgent`（订单查询/催单/取消/退款）
- [ ] 新增 `CustomerServiceAgent`（工单创建/退款政策/人工转接）
- [ ] 新增 `BusinessAnalyticsAgent`（经营分析/报告生成）
- [ ] 新增 `KitchenDisplayAgent`（后厨队列/出餐状态）
- [ ] 编写各 Agent 的 Skill Markdown 文件

### Phase 4: AG-UI 协议与前端适配（2周）
- [ ] 实现 AG-UI 协议端点（`AguiController`）
- [ ] 实现 SSE 事件处理器（`AguiEventHandler`）
- [ ] 实现 AG-UI 状态管理（`AguiStateManager`）
- [ ] 改造前端 Agent 对话页面（SSE 流式接收 + 卡片渲染）
- [ ] 新增 `FoodRecommendCard.tsx`、`OrderStatusCard.tsx` 等组件

### Phase 5: 可观测性与运维（1周）
- [ ] 集成 OpenTelemetry（链路追踪 + 指标导出）
- [ ] 实现 Agent 工具调用审计日志
- [ ] 实现 Prometheus 指标暴露（agent.chat.count, agent.tool.call.duration 等）
- [ ] 配置 Grafana 仪表盘
- [ ] 编写 Docker Compose 生产部署配置
- [ ] 编写 Nginx 负载均衡配置

### Phase 6: 测试与压测（1周）
- [ ] 单元测试（各 Agent、Tool、Pruning 策略）
- [ ] 集成测试（Agent 间协作流程）
- [ ] 端到端测试（前端 → 后端 → Agent → 数据库）
- [ ] 性能压测（并发 Agent 对话、工具调用延迟）
- [ ] 菜单语义检索准确率测试（目标 90%+）

## 九、未来规划

### 短期（1-3个月）
- [ ] 完善单元测试覆盖率（目标 80%+）
- [ ] 优化 Context Pruning 策略（引入语义摘要裁剪）
- [ ] 支持更多 LLM 后端（Claude、通义千问、文心一言）
- [ ] 优化前端性能（SSE 流式渲染、虚拟滚动）
- [ ] 引入 Embedding 模型本地部署（BGE-small-zh）

### 中期（3-6个月）
- [ ] 支持多模态输入（图片点餐、语音点餐）
- [ ] 支持 Agent 市场（第三方 Agent 接入）
- [ ] 支持分布式 Agent 部署（不同 Agent 部署在不同实例）
- [ ] 完善监控和告警（Agent 异常检测、成本监控）
- [ ] 引入用户画像系统（协同过滤 + 内容推荐融合）

### 长期（6-12个月）
- [ ] 支持强化学习优化推荐策略（在线学习）
- [ ] 支持多租户隔离（SaaS 化部署）
- [ ] 支持边缘计算（门店本地 Agent）
- [ ] 构建 Agent 生态系统（开放 API + 开发者文档）
- [ ] 实现 A/B 实验平台（Agent 策略效果对比）

---

**文档版本：** v1.0
**最后更新：** 2026-05-16
**作者：** AI Assistant

> 本文档基于苍穹外卖项目现状与多Agent系统平台参考架构设计，描述了将单体 LangChain4j Agent 改造为企业级多Agent智能平台的技术方案。
