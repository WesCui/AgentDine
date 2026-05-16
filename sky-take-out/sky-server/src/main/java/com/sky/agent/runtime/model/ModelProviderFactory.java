package com.sky.agent.runtime.model;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

public interface ModelProviderFactory {

    ChatLanguageModel getChatModel(String modelName);

    EmbeddingModel getEmbeddingModel(String modelName);

    void registerProvider(String name, ChatLanguageModel model);
}
