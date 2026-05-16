package com.sky.agent.runtime.registry;

import java.util.List;

public interface AgentRegistry {

    void register(AgentMetadata metadata, Object agentInstance);

    AgentMetadata getMetadata(String agentName);

    Object getInstance(String agentName);

    List<AgentMetadata> listAll();

    List<AgentMetadata> listByCapability(String capability);

    AgentMetadata matchByIntent(String intent);
}
