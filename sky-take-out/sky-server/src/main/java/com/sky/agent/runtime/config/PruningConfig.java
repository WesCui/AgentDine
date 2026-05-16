package com.sky.agent.runtime.config;

import com.sky.agent.runtime.pruning.ContextPruningStrategy;
import com.sky.agent.runtime.pruning.SlidingWindowPruner;
import com.sky.agent.runtime.pruning.TokenBudgetPruner;
import com.sky.agent.runtime.pruning.ToolResultTruncator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PruningConfig {

    @Autowired
    private AgentsConfig agentsConfig;

    @Bean
    public ContextPruningStrategy contextPruningStrategy() {
        AgentsConfig.PruningDef pruning = agentsConfig.getPruning();
        String strategy = pruning.getStrategy();

        if ("sliding_window".equals(strategy)) {
            return new SlidingWindowPruner(10); // last 10 turns
        }
        // default: token_budget
        return new TokenBudgetPruner();
    }

    @Bean
    public ToolResultTruncator toolResultTruncator() {
        int maxLength = agentsConfig.getPruning().getMaxToolResultLength();
        return new ToolResultTruncator(maxLength);
    }
}
