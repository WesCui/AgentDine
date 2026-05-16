package com.sky.agent.runtime;

import com.sky.agent.runtime.config.AgentsConfig;
import com.sky.agent.runtime.factory.AgentFactory;
import com.sky.agent.runtime.factory.LangChain4jAgentFactory.SimpleAiService;
import com.sky.agent.runtime.protocol.a2a.A2aClient;
import com.sky.agent.runtime.registry.AgentMetadata;
import com.sky.agent.runtime.registry.AgentRegistry;
import com.sky.agent.runtime.tool.ToolRegistry;
import com.sky.agent.tools.AgentTools;
import com.sky.agent.tools.AnalyticsTools;
import com.sky.agent.tools.CustomerServiceTools;
import com.sky.agent.tools.OrderTools;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@Slf4j
public class AgentRuntimeInitializer implements CommandLineRunner {

    @Autowired
    private AgentsConfig agentsConfig;

    @Autowired
    private AgentRegistry agentRegistry;

    @Autowired
    private AgentFactory agentFactory;

    @Autowired
    private ToolRegistry toolRegistry;

    @Autowired
    private AgentTools agentTools;

    @Autowired
    private OrderTools orderTools;

    @Autowired
    private AnalyticsTools analyticsTools;

    @Autowired
    private CustomerServiceTools customerServiceTools;

    @Autowired
    private A2aClient a2aClient;

    @Override
    public void run(String... args) {
        log.info("========================================");
        log.info("Initializing Multi-Agent Runtime...");
        log.info("========================================");

        // Step 1: Register all @Tool methods from tool classes
        registerToolsFromClass(agentTools, AgentTools.class);
        registerToolsFromClass(orderTools, OrderTools.class);
        registerToolsFromClass(analyticsTools, AnalyticsTools.class);
        registerToolsFromClass(customerServiceTools, CustomerServiceTools.class);

        // Step 2: Create and register all agents from YAML config
        if (agentsConfig.getAgents() != null && !agentsConfig.getAgents().isEmpty()) {
            for (AgentsConfig.AgentDef def : agentsConfig.getAgents()) {
                registerAgent(def);
            }
        }

        // Step 3: Print summary
        log.info("========================================");
        log.info("Multi-Agent Runtime initialized successfully");
        log.info("Registered agents: {}", agentRegistry.listAll().size());
        agentRegistry.listAll().forEach(meta ->
                log.info("  - [{}] {} (model={}, tools={}, skills={})",
                        meta.getName(), meta.getDisplayName(),
                        meta.getModel(), meta.getTools(), meta.getSkills()));
        log.info("Registered tools: {}", toolRegistry.listToolNames());
        log.info("========================================");
    }

    private void registerToolsFromClass(Object instance, Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            Tool toolAnn = method.getAnnotation(Tool.class);
            if (toolAnn != null) {
                toolRegistry.register(method.getName(), instance);
                log.debug("Registered tool: {}", method.getName());
            }
        }
    }

    private void registerAgent(AgentsConfig.AgentDef def) {
        AgentMetadata metadata = def.toMetadata();
        if (metadata.getCapabilities() == null) {
            metadata.setCapabilities(java.util.Collections.singletonList(metadata.getName()));
        }

        // Warn about missing tools
        if (metadata.getTools() != null) {
            for (String toolName : metadata.getTools()) {
                if (toolRegistry.getTool(toolName) == null) {
                    log.warn("Tool '{}' referenced by agent '{}' is not registered", toolName, metadata.getName());
                }
            }
        }

        Object agentInstance = agentFactory.createAgent(metadata);
        agentRegistry.register(metadata, agentInstance);

        // Register with A2aClient for inter-agent communication
        if (agentInstance instanceof SimpleAiService) {
            a2aClient.registerAgent(metadata.getName(), (SimpleAiService) agentInstance);
        }
    }
}
