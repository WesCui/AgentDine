package com.sky.agent.runtime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
public class AgentAuditLogger {

    /**
     * Log a tool call for audit purposes.
     */
    public void logToolCall(String sessionId, String agentName, String toolName,
                            String input, String output, long executionTimeMs,
                            boolean success, String errorMessage) {
        log.info("AUDIT | tool_call | session={} | agent={} | tool={} | input={} | output={} | duration={}ms | status={} {}",
                sessionId, agentName, toolName,
                truncate(input, 200),
                truncate(output, 200),
                executionTimeMs,
                success ? "OK" : "FAIL",
                errorMessage != null ? "| error=" + errorMessage : "");
    }

    /**
     * Log an agent conversation turn.
     */
    public void logConversation(String sessionId, String agentName, Long userId,
                                 String userMessage, String response, long latencyMs) {
        log.info("AUDIT | conversation | session={} | agent={} | userId={} | userMsgLen={} | responseLen={} | latency={}ms",
                sessionId, agentName, userId,
                userMessage != null ? userMessage.length() : 0,
                response != null ? response.length() : 0,
                latencyMs);
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "null";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "...";
    }
}
