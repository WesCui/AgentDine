package com.sky.agent.service;

public interface AgentChatService {

    String chat(Long memoryId, Long userId, String userMessage);
}
