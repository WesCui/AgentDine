package com.sky.agent.service.impl;

import com.sky.agent.orchestrator.AgentOrchestrator;
import com.sky.agent.orchestrator.OrchestrationResult;
import com.sky.agent.runtime.AgentAuditLogger;
import com.sky.agent.runtime.AgentMetrics;
import com.sky.agent.service.AgentChatService;
import com.sky.agent.service.FoodRecommendationAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class AgentChatServiceImpl implements AgentChatService {

    private static final int LOG_PREVIEW_MAX_LENGTH = 120;

    @Autowired
    private FoodRecommendationAgent foodRecommendationAgent;

    @Autowired(required = false)
    private AgentOrchestrator agentOrchestrator;

    @Autowired
    private AgentMetrics agentMetrics;

    @Autowired
    private AgentAuditLogger auditLogger;

    @Override
    public String chat(Long memoryId, Long userId, String userMessage) {
        String requestId = UUID.randomUUID().toString().replace("-", "");
        long startTime = System.currentTimeMillis();

        log.info("agent chat start, requestId={}, userId={}, memoryId={}, messageLength={}, messagePreview={}",
                requestId, userId, memoryId, safeLength(userMessage), preview(userMessage));

        try {
            String response;
            String agentName = "food-recommendation";

            // Try orchestrator first (multi-agent routing)
            if (agentOrchestrator != null) {
                OrchestrationResult result = agentOrchestrator.orchestrate(memoryId, userMessage);
                if (result.isRouted()) {
                    response = result.getResponse();
                    agentName = result.getTargetAgentName();
                    long costMs = System.currentTimeMillis() - startTime;
                    agentMetrics.recordAgentCall(agentName, costMs);
                    auditLogger.logConversation(requestId, agentName, userId, userMessage, response, costMs);
                    log.info("agent chat routed, requestId={}, agent={}, costMs={}, responseLength={}",
                            requestId, agentName, costMs, safeLength(response));
                    return response;
                }
            }

            // Fall back to direct FoodRecommendationAgent
            response = foodRecommendationAgent.chat(memoryId, userMessage);
            long costMs = System.currentTimeMillis() - startTime;

            agentMetrics.recordAgentCall(agentName, costMs);
            auditLogger.logConversation(requestId, agentName, userId, userMessage, response, costMs);

            log.info("agent chat success (direct), requestId={}, userId={}, memoryId={}, costMs={}, responseLength={}, responsePreview={}",
                    requestId, userId, memoryId, costMs, safeLength(response), preview(response));
            return response;
        } catch (Exception ex) {
            long costMs = System.currentTimeMillis() - startTime;
            agentMetrics.recordAgentError("food-recommendation");
            log.error("agent chat failed, requestId={}, userId={}, memoryId={}, costMs={}, error={}",
                    requestId, userId, memoryId, costMs, ex.getMessage(), ex);
            throw ex;
        }
    }

    private int safeLength(String content) {
        return content == null ? 0 : content.length();
    }

    private String preview(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        if (content.length() <= LOG_PREVIEW_MAX_LENGTH) {
            return content;
        }
        return content.substring(0, LOG_PREVIEW_MAX_LENGTH) + "...";
    }
}
