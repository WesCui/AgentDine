package com.sky.agent.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrchestrationResult {

    private boolean routed;
    private String targetAgentName;
    private String targetAgentDisplayName;
    private String response;

    public static OrchestrationResult noMatch() {
        return OrchestrationResult.builder().routed(false).build();
    }

    public static OrchestrationResult routed(String agentName, String displayName, String response) {
        return OrchestrationResult.builder()
                .routed(true)
                .targetAgentName(agentName)
                .targetAgentDisplayName(displayName)
                .response(response)
                .build();
    }
}
