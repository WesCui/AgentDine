package com.sky.agent.runtime.tool;

import java.util.List;

public interface ToolRegistry {

    void register(String toolName, Object toolInstance);

    Object getTool(String toolName);

    List<Object> getTools(List<String> toolNames);

    List<String> listToolNames();
}
