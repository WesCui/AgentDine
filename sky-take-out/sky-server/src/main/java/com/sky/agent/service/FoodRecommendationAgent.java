package com.sky.agent.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 食品推荐智能体接口
 * 定义了与 AI 模型交互的方法
 * 基于 LangChain4j 的 AiService 机制实现
 */
public interface FoodRecommendationAgent {

    /**
     * 与智能体进行对话
     *
     * @param memoryId    记忆ID，通常使用用户ID，用于区分不同用户的对话上下文
     * @param userMessage 用户发送的消息内容
     * @return 智能体的回复内容
     */
    @SystemMessage("你是一个专业的外卖点餐助手。请根据用户的需求和提供的菜单信息(Context)，推荐合适的菜品。不要推荐菜单上没有的菜。")
    String chat(@MemoryId Long memoryId, @UserMessage String userMessage);
}
