package com.sky.agent.runtime.registry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class DefaultAgentRegistry implements AgentRegistry {

    private final ConcurrentHashMap<String, AgentMetadata> metadataMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> instanceMap = new ConcurrentHashMap<>();

    @Override
    public void register(AgentMetadata metadata, Object agentInstance) {
        metadataMap.put(metadata.getName(), metadata);
        instanceMap.put(metadata.getName(), agentInstance);
        log.info("Agent registered: {} ({})", metadata.getDisplayName(), metadata.getName());
    }

    @Override
    public AgentMetadata getMetadata(String agentName) {
        return metadataMap.get(agentName);
    }

    @Override
    public Object getInstance(String agentName) {
        return instanceMap.get(agentName);
    }

    @Override
    public List<AgentMetadata> listAll() {
        return new ArrayList<>(metadataMap.values());
    }

    @Override
    public List<AgentMetadata> listByCapability(String capability) {
        List<AgentMetadata> result = new ArrayList<>();
        for (AgentMetadata meta : metadataMap.values()) {
            if (meta.getCapabilities() != null && meta.getCapabilities().contains(capability)) {
                result.add(meta);
            }
        }
        return result;
    }

    @Override
    public AgentMetadata matchByIntent(String intent) {
        for (AgentMetadata meta : metadataMap.values()) {
            if (meta.getCapabilities() != null) {
                for (String cap : meta.getCapabilities()) {
                    if (cap.equalsIgnoreCase(intent) || intent.contains(cap)) {
                        return meta;
                    }
                }
            }
        }
        return null;
    }
}
