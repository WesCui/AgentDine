package com.sky.agent.runtime.registry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentMetadata {

    private String name;
    private String displayName;
    private String description;
    private List<String> capabilities;
    private String model;
    private String systemPrompt;
    private List<String> tools;
    private List<String> skills;
    private List<String> subAgents;
    private int maxTokens;
    private float temperature;
    private int maxMessages;
    private Map<String, Object> extra;
}
