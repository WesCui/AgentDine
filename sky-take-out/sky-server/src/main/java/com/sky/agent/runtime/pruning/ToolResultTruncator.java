package com.sky.agent.runtime.pruning;

public class ToolResultTruncator {

    private final int maxLength;

    public ToolResultTruncator(int maxLength) {
        this.maxLength = maxLength;
    }

    public String truncate(String toolResult) {
        if (toolResult == null || toolResult.length() <= maxLength) {
            return toolResult;
        }
        int half = maxLength / 2;
        return toolResult.substring(0, half)
                + "\n...[tool output truncated, " + (toolResult.length() - maxLength) + " chars removed]...\n"
                + toolResult.substring(toolResult.length() - half);
    }
}
