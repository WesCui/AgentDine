package com.sky.agent.runtime.memory;

import java.util.Map;

public interface MemoryManager {

    void savePreference(Long userId, String key, Object value);

    Object getPreference(Long userId, String key);

    Map<String, Object> getAllPreferences(Long userId);

    void clearPreferences(Long userId);
}
