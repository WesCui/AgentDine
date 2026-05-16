package com.sky.agent.orchestrator;

import com.sky.agent.runtime.config.AgentsConfig;
import com.sky.agent.runtime.factory.LangChain4jAgentFactory.SimpleAiService;
import com.sky.agent.runtime.protocol.a2a.A2aClient;
import com.sky.agent.runtime.protocol.a2a.A2aMessage;
import com.sky.agent.runtime.registry.AgentMetadata;
import com.sky.agent.runtime.registry.AgentRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class AgentOrchestrator {

    private static final Map<String, String> INTENT_ROUTING = new LinkedHashMap<>();

    static {
        INTENT_ROUTING.put("点餐|推荐|想吃|菜单|点菜|有什么菜|吃什么|来点|来一份|来一份|推荐菜|有没有", "food-recommendation");
        INTENT_ROUTING.put("辣|甜|酸|咸|清淡|口味|好吃|招牌|特色|热门|新品", "food-recommendation");
        INTENT_ROUTING.put("订单|下单|我的单|查单|到哪|配送|催单|催一下|怎么还没到|取消|退款|退单|支付|付款", "order-management");
        INTENT_ROUTING.put("投诉|质量问题|不好吃|坏了|异物|态度|太慢|超时|补偿|赔偿|工单", "customer-service");
        INTENT_ROUTING.put("经营|销售|报表|数据|分析|统计|营业额|排行|销量|top|滞销|报告", "business-analytics");
        INTENT_ROUTING.put("后厨|出餐|制作|厨房|备餐|库存|售罄", "kitchen-display");
    }

    @Autowired
    private AgentRegistry agentRegistry;

    @Autowired
    private A2aClient a2aClient;

    @Autowired
    private AgentsConfig agentsConfig;

    /**
     * Route user message to the appropriate specialist agent.
     * If no specific agent matches, returns null (caller handles directly).
     */
    public OrchestrationResult orchestrate(Long memoryId, String userMessage) {
        // Step 1: Classify intent
        String targetAgentName = classifyIntent(userMessage);

        if (targetAgentName == null) {
            return OrchestrationResult.noMatch();
        }

        // Step 2: Get target agent metadata
        AgentMetadata targetMeta = agentRegistry.getMetadata(targetAgentName);
        if (targetMeta == null) {
            log.warn("Orchestrator: target agent '{}' not registered", targetAgentName);
            return OrchestrationResult.noMatch();
        }

        // Step 3: Get orchestrator metadata (for A2A routing context)
        AgentMetadata orchestratorMeta = agentRegistry.getMetadata("orchestrator");

        // Step 4: Route via A2A
        log.info("Orchestrator routing to: {} (intent match: {})", targetAgentName, targetMeta.getDisplayName());

        String response = a2aClient.send(
                orchestratorMeta != null ? orchestratorMeta : targetMeta,
                targetMeta,
                userMessage
        );

        return OrchestrationResult.routed(targetAgentName, targetMeta.getDisplayName(), response);
    }

    /**
     * Simple keyword-based intent classification.
     * Can be upgraded to LLM-based classification later.
     */
    private String classifyIntent(String userMessage) {
        if (userMessage == null || userMessage.isEmpty()) {
            return null;
        }

        String lowerMsg = userMessage.toLowerCase();

        for (Map.Entry<String, String> entry : INTENT_ROUTING.entrySet()) {
            String[] patterns = entry.getKey().split("\\|");
            for (String pattern : patterns) {
                if (lowerMsg.contains(pattern)) {
                    return entry.getValue();
                }
            }
        }

        return null; // no specific target matched
    }

    /**
     * Get all available agent names for routing.
     */
    public List<String> getAvailableAgents() {
        List<AgentMetadata> all = agentRegistry.listAll();
        List<String> names = new ArrayList<>();
        for (AgentMetadata meta : all) {
            names.add(meta.getName());
        }
        return names;
    }
}
