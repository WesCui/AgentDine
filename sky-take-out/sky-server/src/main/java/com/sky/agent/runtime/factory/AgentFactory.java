package com.sky.agent.runtime.factory;

import com.sky.agent.runtime.registry.AgentMetadata;

public interface AgentFactory {

    Object createAgent(AgentMetadata metadata);

    boolean supports(AgentMetadata metadata);
}
