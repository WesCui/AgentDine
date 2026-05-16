package com.sky.agent.runtime.pruning;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

public interface ContextPruningStrategy {

    List<ChatMessage> prune(List<ChatMessage> messages, int maxTokens);

    String truncateToolResult(String result, int maxLength);
}
