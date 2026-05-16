# 智能外卖推荐 Agent 详细设计方案

## 1. 概述
本方案旨在为 `AgentDine` 项目集成一个基于 LLM (大语言模型) 的智能推荐 Agent。该 Agent 将利用 RAG (检索增强生成) 技术，结合店铺现有的菜品数据，为用户提供个性化的点餐建议。

## 2. 技术栈
- **核心框架**: [LangChain4j](https://github.com/langchain4j/langchain4j) (Java 版 LLM 应用开发框架)
- **LLM 提供商**: OpenAI (GPT-3.5/4) 或 阿里云 DashScope (通义千问) 等兼容接口。
- **RAG 技术**:
    - **Embedding 模型**: `all-minilm-l6-v2` (本地运行，免费且速度快) 或 OpenAI Embedding。
    - **向量存储**: `InMemoryEmbeddingStore` (内存存储，适合中小规模数据，可持久化到文件) 或 `Chroma` / `Milvus`。
- **工具调用 (Tool Use / MCP)**: 利用 LangChain4j 的 `@Tool` 机制，赋予 Agent 查询用户历史订单、查询菜品详情等能力。

## 3. 架构设计

### 3.1 模块划分
建议在 `sky-server` 模块下新增 `agent` 包：
```
com.sky.agent
├── config           # 配置类 (LLM, EmbeddingStore)
├── controller       # AgentController (对外接口)
├── service          # AgentService (核心逻辑)
├── tools            # AgentTools (提供给 AI 的工具方法)
└── rag              # RAG 相关 (数据加载器, 检索器)
```

### 3.2 核心流程
1.  **数据初始化 (RAG Ingestion)**:
    -   系统启动时，读取数据库中 `status=1` (起售) 的 `Dish` (菜品) 和 `Setmeal` (套餐)。
    - 建议的 Embedding 文本模板: `"菜名: {{name}}, 分类: {{category}}, 价格: {{price}}, 描述: {{description}}"`。
    - 使用 Embedding 模型将文本转换为向量，存入向量数据库 (EmbeddingStore)。

2.  **用户交互 (Chat Loop)**:
    -   用户通过 API 发送消息 (例如："今天好热，想吃点清淡的")。
    -   **Retrieval**: 系统将用户查询转化为向量，在向量库中搜索最相似的菜品信息。
    -   **Context Injection**: 将搜索到的菜品信息作为 "Context" 注入 Prompt。
    -   **Tool Execution (Optional)**: 如果用户问 "我上周吃了什么"，Agent 自动调用 `OrderService.queryHistory` 工具。
    -   **Generation**: LLM 根据 Context 和 User Query 生成最终建议。

## 4. 详细实现步骤

### 步骤 1: 引入依赖
在 `sky-server/pom.xml` 中添加 LangChain4j 相关依赖。

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-spring-boot-starter</artifactId>
    <version>0.32.0</version>
</dependency>
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai-spring-boot-starter</artifactId>
    <version>0.32.0</version>
</dependency>
<!-- 本地 Embedding 模型 -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-embeddings-all-minilm-l6-v2</artifactId>
    <version>0.32.0</version>
</dependency>
```

### 步骤 2: RAG 数据加载器 (KnowledgeLoader)
创建一个 Component，实现 `CommandLineRunner`，在应用启动时执行。
- 注入 `DishMapper`, `SetmealMapper`。
- 查询所有起售菜品。
- 构建 `TextSegment` 并调用 `EmbeddingStore.add()`。

### 步骤 3: 定义工具集 (AgentTools)
创建一个 Bean，包含 Agent 可调用的方法。
```java
@Component
public class AgentTools {
    @Autowired
    private OrderMapper orderMapper;

    @Tool("查询当前用户的历史订单，用于了解用户口味偏好")
    public List<String> getUserLastOrders(Long userId) {
        // ...调用 Mapper 查询最近订单并返回菜品名称列表
    }
    
    @Tool("获取店铺的热销榜单")
    public List<String> getTopSellingDishes() {
        // ...查询销量最高的菜品
    }
}
```

### 步骤 4: 定义 Agent 接口 (AiService)
```java
@AiService
public interface FoodRecommendationAgent {
    
    @SystemMessage("你是一个专业的外卖点餐助手。请根据用户的需求和提供的菜单信息(Context)，推荐合适的菜品。不要推荐菜单上没有的菜。")
    String chat(@UserMessage String question, @MemoryId Long memoryId);
}
```
*注：需要在配置类中组装 AiService，通过 `ContentRetriever` (RAG) 和 `AgentTools` 增强它。*

### 步骤 5: 控制层暴露 (AgentController)
```java
@RestController
@RequestMapping("/user/agent")
public class AgentController {
    @Autowired RecommendationAgent agent;
    
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody AgentChatDTO dto) {
        Long userId = BaseContext.getCurrentId();
        String response = agent.chat(dto.getMessage(), userId);
        return Result.success(response);
    }
}
```

## 5. 即将执行的操作
我们将按照上述计划，逐步在代码中实现该功能。首先会修改 `pom.xml` 添加依赖，然后创建相应的 Java 类。

