package com.sky.agent.runtime.pruning;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;

import java.util.ArrayList;
import java.util.List;

public class TokenBudgetPruner implements ContextPruningStrategy {

    private final int tokenPerCharEstimate = 4; // rough estimate: 1 token ≈ 4 chars for Chinese

    @Override
    public List<ChatMessage> prune(List<ChatMessage> messages, int maxTokens) {
        if (messages == null || messages.isEmpty()) {
            return messages;
        }

        List<ChatMessage> pruned = new ArrayList<>();
        int tokenBudget = maxTokens;

        // keep system messages first
        for (ChatMessage msg : messages) {
            if (msg instanceof SystemMessage) {
                pruned.add(msg);
                tokenBudget -= estimateTokens(msg);
            }
        }

        // iterate from newest to oldest, keep as many as budget allows
        List<ChatMessage> recent = new ArrayList<>();
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage msg = messages.get(i);
            if (msg instanceof SystemMessage) continue;
            int tokens = estimateTokens(msg);
            if (tokenBudget >= tokens) {
                recent.add(0, msg);
                tokenBudget -= tokens;
            } else {
                break;
            }
        }

        pruned.addAll(recent);
        return pruned;
    }

    @Override
    public String truncateToolResult(String result, int maxLength) {
        if (result == null || result.length() <= maxLength) {
            return result;
        }
        int half = maxLength / 2;
        return result.substring(0, half) + "\n...[truncated]...\n" + result.substring(result.length() - half);
    }

    private int estimateTokens(ChatMessage msg) {
        String text = msg.text();
        return text == null ? 0 : text.length() / tokenPerCharEstimate;
    }
}
