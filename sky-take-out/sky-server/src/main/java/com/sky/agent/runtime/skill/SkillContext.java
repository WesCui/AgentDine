package com.sky.agent.runtime.skill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillContext {

    private String name;
    private String description;
    private String content;
    private String lifecycle; // session, turn, choice
    private long loadedAt;
    private int usageCount;
}
