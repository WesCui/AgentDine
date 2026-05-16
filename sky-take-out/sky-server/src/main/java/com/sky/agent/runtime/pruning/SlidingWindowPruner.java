package com.sky.agent.runtime.pruning;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;

import java.util.ArrayList;
import java.util.List;

public class SlidingWindowPruner implements ContextPruningStrategy {

    private final int maxTurns;

    public SlidingWindowPruner(int maxTurns) {
        this.maxTurns = maxTurns;
    }

    @Override
    public List<ChatMessage> prune(List<ChatMessage> messages, int maxTokens) {
        if (messages == null || messages.isEmpty()) {
            return messages;
        }

        List<ChatMessage> pruned = new ArrayList<>();
        // keep system messages
        for (ChatMessage msg : messages) {
            if (msg instanceof SystemMessage) {
                pruned.add(msg);
            }
        }

        // keep last N turns (2 messages per turn: user + assistant)
        int keepCount = maxTurns * 2;
        int start = Math.max(0, messages.size() - keepCount);
        for (int i = start; i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);
            if (!(msg instanceof SystemMessage)) {
                pruned.add(msg);
            }
        }

        return pruned;
    }

    @Override
    public String truncateToolResult(String result, int maxLength) {
        if (result == null || result.length() <= maxLength) {
            return result;
        }
        return result.substring(0, maxLength) + "...[truncated]";
    }
}
