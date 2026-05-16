package com.sky.agent.runtime.model;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class DefaultModelProviderFactory implements ModelProviderFactory {

    private final ConcurrentHashMap<String, ChatLanguageModel> chatModels = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, EmbeddingModel> embeddingModels = new ConcurrentHashMap<>();

    private final ChatLanguageModel defaultChatModel;
    private final EmbeddingModel defaultEmbeddingModel;

    public DefaultModelProviderFactory(ChatLanguageModel defaultChatModel,
                                       EmbeddingModel defaultEmbeddingModel) {
        this.defaultChatModel = defaultChatModel;
        this.defaultEmbeddingModel = defaultEmbeddingModel;
        chatModels.put("default", defaultChatModel);
    }

    @Override
    public ChatLanguageModel getChatModel(String modelName) {
        ChatLanguageModel model = chatModels.get(modelName);
        if (model == null) {
            log.warn("Model '{}' not found, falling back to default", modelName);
            return defaultChatModel;
        }
        return model;
    }

    @Override
    public EmbeddingModel getEmbeddingModel(String modelName) {
        return defaultEmbeddingModel;
    }

    @Override
    public void registerProvider(String name, ChatLanguageModel model) {
        chatModels.put(name, model);
        log.info("Model provider registered: {}", name);
    }
}
