package com.sky.agent.runtime.skill;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SkillLoader {

    private static final String SKILL_PATH = "classpath:skills/*.md";

    private final ConcurrentHashMap<String, SkillContext> skillCache = new ConcurrentHashMap<>();
    private final LinkedHashMap<String, SkillContext> lruCache = new LinkedHashMap<String, SkillContext>(50, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, SkillContext> eldest) {
            if (size() > 50) {
                log.debug("LRU evicting skill: {}", eldest.getKey());
                return true;
            }
            return false;
        }
    };

    public SkillLoader() {
        loadSkillsFromClasspath();
    }

    private void loadSkillsFromClasspath() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(SKILL_PATH);
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename == null || !filename.endsWith(".md")) continue;
                String skillName = filename.replace(".md", "");
                String content = readResource(resource);
                SkillContext ctx = SkillContext.builder()
                        .name(skillName)
                        .content(content)
                        .lifecycle("session")
                        .loadedAt(System.currentTimeMillis())
                        .usageCount(0)
                        .build();
                skillCache.put(skillName, ctx);
                lruCache.put(skillName, ctx);
                log.info("Skill loaded: {}", skillName);
            }
        } catch (Exception e) {
            log.warn("No skill files found at {}, or failed to load: {}", SKILL_PATH, e.getMessage());
        }
    }

    private String readResource(Resource resource) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.error("Failed to read skill resource: {}", resource.getFilename(), e);
            return "";
        }
    }

    public SkillContext getSkill(String skillName) {
        SkillContext ctx = skillCache.get(skillName);
        if (ctx != null) {
            ctx.setUsageCount(ctx.getUsageCount() + 1);
            lruCache.get(skillName); // touch LRU
        }
        return ctx;
    }

    public String injectSkills(String basePrompt, List<String> skillNames) {
        if (skillNames == null || skillNames.isEmpty()) {
            return basePrompt;
        }
        StringBuilder sb = new StringBuilder(basePrompt);
        for (String name : skillNames) {
            SkillContext skill = getSkill(name);
            if (skill != null) {
                sb.append("\n\n--- Skill: ").append(name).append(" ---\n");
                sb.append(skill.getContent());
            } else {
                log.warn("Skill not found: {}", name);
            }
        }
        return sb.toString();
    }

    public List<String> listSkills() {
        return new ArrayList<>(skillCache.keySet());
    }

    public void reload() {
        skillCache.clear();
        lruCache.clear();
        loadSkillsFromClasspath();
    }
}
