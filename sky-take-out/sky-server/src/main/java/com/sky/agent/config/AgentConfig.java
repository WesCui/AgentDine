package com.sky.agent.config;

import com.sky.agent.service.FoodRecommendationAgent;
import com.sky.agent.tools.AgentTools;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 智能体配置类
 * 负责配置 LangChain4j 相关的组件，包括向量存储、内容检索器和 AI 服务实例
 */
@Configuration
public class AgentConfig {

    /**
     * 配置向量存储
     * 使用内存存储 (InMemoryEmbeddingStore)
     * 注意：内存存储在应用重启后会丢失数据，适合开发测试
     * 生产环境建议使用持久化向量数据库 (如 Chroma, Milvus, PgVector 等)
     *
     * @return EmbeddingStore 实例
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    /**
     * 配置内容检索器 (RAG 核心组件)
     * 用于根据用户查询从向量数据库中检索相关内容
     *
     * @param embeddingStore 向量存储组件
     * @param embeddingModel 嵌入模型，用于将查询文本转换为向量
     * @return ContentRetriever 实例，配置了最大结果数和最小相似度分数
     */
    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel) // 请求时使用的 Embedding 模型
                .maxResults(5) // 每次检索返回的最大结果数
                .minScore(0.5) // 结果的最低相似度分数
                .build(); // 基于 Embedding Store 的内容检索器
    }


    /**
     * 配置食品推荐智能体 (AiService)
     * 集成了语言模型、RAG 检索能力和工具调用能力
     *
     * @param chatLanguageModel 底层聊天语言模型
     * @param contentRetriever  内容检索器，用于增强上下文
     * @param agentTools        工具集，提供查询订单、菜单等具体功能
     * @return FoodRecommendationAgent 代理实例
     */
    @Bean
    public FoodRecommendationAgent foodRecommendationAgent(ChatLanguageModel chatLanguageModel,
                                                           ContentRetriever contentRetriever,
                                                           AgentTools agentTools) {
        return AiServices.builder(FoodRecommendationAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .contentRetriever(contentRetriever)
                .tools(agentTools)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
}
