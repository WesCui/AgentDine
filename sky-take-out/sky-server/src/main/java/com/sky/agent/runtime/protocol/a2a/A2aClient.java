package com.sky.agent.runtime.protocol.a2a;

import com.sky.agent.runtime.factory.LangChain4jAgentFactory.SimpleAiService;
import com.sky.agent.runtime.registry.AgentMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class A2aClient {

    private final Map<String, SimpleAiService> agentProxies = new ConcurrentHashMap<>();

    public void registerAgent(String agentName, SimpleAiService agent) {
        agentProxies.put(agentName, agent);
    }

    public void unregisterAgent(String agentName) {
        agentProxies.remove(agentName);
    }

    /**
     * Send a message to a target agent and get the response.
     */
    public String send(AgentMetadata from, AgentMetadata to, String message) {
        SimpleAiService targetAgent = agentProxies.get(to.getName());
        if (targetAgent == null) {
            log.warn("A2A: target agent '{}' not available", to.getName());
            return "对不起，" + to.getDisplayName() + "当前不可用，请稍后重试。";
        }

        log.info("A2A: {} → {} ({})", from.getDisplayName(), to.getDisplayName(),
                message.length() > 80 ? message.substring(0, 80) + "..." : message);

        try {
            String response = targetAgent.chat(message);
            log.info("A2A: {} ← {} ({} chars)", from.getDisplayName(), to.getDisplayName(),
                    response != null ? response.length() : 0);
            return response;
        } catch (Exception e) {
            log.error("A2A error: {} → {}: {}", from.getName(), to.getName(), e.getMessage());
            return "处理请求时发生错误: " + e.getMessage();
        }
    }

    /**
     * Broadcast a message to multiple agents (fire-and-forget).
     */
    public void broadcast(AgentMetadata from, String message, String... targetAgentNames) {
        for (String name : targetAgentNames) {
            SimpleAiService agent = agentProxies.get(name);
            if (agent != null) {
                try {
                    agent.chat(message);
                } catch (Exception e) {
                    log.warn("A2A broadcast to '{}' failed: {}", name, e.getMessage());
                }
            }
        }
    }
}
