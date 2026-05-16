package com.sky.agent.runtime.factory;

import com.sky.agent.runtime.model.ModelProviderFactory;
import com.sky.agent.runtime.registry.AgentMetadata;
import com.sky.agent.runtime.skill.SkillLoader;
import com.sky.agent.runtime.tool.ToolRegistry;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class LangChain4jAgentFactory implements AgentFactory {

    @Autowired
    private ModelProviderFactory modelProviderFactory;

    @Autowired
    private ToolRegistry toolRegistry;

    @Autowired
    private SkillLoader skillLoader;

    @Autowired
    private ContentRetriever contentRetriever;

    @Override
    public Object createAgent(AgentMetadata metadata) {
        ChatLanguageModel chatModel = modelProviderFactory.getChatModel(metadata.getModel());

        // 加载 Skill 并注入 System Prompt
        String systemPrompt = metadata.getSystemPrompt();
        if (metadata.getSkills() != null && !metadata.getSkills().isEmpty()) {
            systemPrompt = skillLoader.injectSkills(systemPrompt, metadata.getSkills());
        }
        final String finalPrompt = systemPrompt != null ? systemPrompt : "";

        // 获取工具列表
        List<Object> tools = new ArrayList<>();
        if (metadata.getTools() != null) {
            tools.addAll(toolRegistry.getTools(metadata.getTools()));
        }

        int maxMessages = metadata.getMaxMessages() > 0 ? metadata.getMaxMessages() : 10;

        // Use AiServices builder with systemMessageProvider
        AiServices<SimpleAiService> builder = AiServices.builder(SimpleAiService.class)
                .chatLanguageModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(maxMessages))
                .systemMessageProvider(memoryId -> finalPrompt);

        if (contentRetriever != null) {
            builder.contentRetriever(contentRetriever);
        }
        if (!tools.isEmpty()) {
            builder.tools(tools.toArray(new Object[0]));
        }

        SimpleAiService agent = builder.build();
        log.info("Agent created: {} (model={}, tools={}, skills={}, memorySize={})",
                metadata.getDisplayName(), metadata.getModel(),
                metadata.getTools(), metadata.getSkills(), maxMessages);

        return agent;
    }

    @Override
    public boolean supports(AgentMetadata metadata) {
        return true;
    }

    /**
     * Simple generic AiService interface for dynamically built agents.
     * The system message is injected via systemMessageProvider.
     */
    public interface SimpleAiService {
        String chat(String userMessage);
    }
}
