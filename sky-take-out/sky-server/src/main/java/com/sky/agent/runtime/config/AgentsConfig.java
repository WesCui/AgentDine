package com.sky.agent.runtime.config;

import com.sky.agent.runtime.registry.AgentMetadata;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "sky.agent")
public class AgentsConfig {

    private Map<String, ModelDef> models = new HashMap<>();
    private SessionDef session = new SessionDef();
    private MemoryDef memory = new MemoryDef();
    private PruningDef pruning = new PruningDef();
    private List<AgentDef> agents = new ArrayList<>();

    @Data
    public static class ModelDef {
        private String provider;
        private String model;
        private String apiKey;
        private String baseUrl;
        private double temperature = 0.7;
        private int maxTokens = 4096;
        private String timeout = "60s";
    }

    @Data
    public static class SessionDef {
        private String backend = "memory";
        private long ttl = 3600;
        private int maxMessagesPerSession = 50;
    }

    @Data
    public static class MemoryDef {
        private String backend = "redis";
        private long userPreferenceTtl = 1209600;
    }

    @Data
    public static class PruningDef {
        private String strategy = "token_budget";
        private int maxContextTokens = 8000;
        private int maxToolResultLength = 2000;
        private boolean keepSystemPrompt = true;
    }

    @Data
    public static class AgentDef {
        private String name;
        private String displayName;
        private String description;
        private String model = "default";
        private String systemPrompt;
        private List<String> tools = new ArrayList<>();
        private List<String> skills = new ArrayList<>();
        private List<String> subAgents = new ArrayList<>();
        private int maxTokens;
        private float temperature = -1;
        private int maxMessages = 10;
        private Map<String, Object> extra = new HashMap<>();

        public AgentMetadata toMetadata() {
            return AgentMetadata.builder()
                    .name(name)
                    .displayName(displayName)
                    .description(description)
                    .model(model)
                    .systemPrompt(systemPrompt)
                    .tools(tools)
                    .skills(skills)
                    .subAgents(subAgents)
                    .maxTokens(maxTokens)
                    .temperature(temperature)
                    .maxMessages(maxMessages)
                    .extra(extra)
                    .build();
        }
    }
}
