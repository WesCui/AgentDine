package com.sky.agent.runtime.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class DefaultToolRegistry implements ToolRegistry {

    private final ConcurrentHashMap<String, Object> tools = new ConcurrentHashMap<>();

    @Override
    public void register(String toolName, Object toolInstance) {
        tools.put(toolName, toolInstance);
        log.info("Tool registered: {}", toolName);
    }

    @Override
    public Object getTool(String toolName) {
        return tools.get(toolName);
    }

    @Override
    public List<Object> getTools(List<String> toolNames) {
        List<Object> result = new ArrayList<>();
        for (String name : toolNames) {
            Object tool = tools.get(name);
            if (tool != null) {
                result.add(tool);
            }
        }
        return result;
    }

    @Override
    public List<String> listToolNames() {
        return new ArrayList<>(tools.keySet());
    }
}
