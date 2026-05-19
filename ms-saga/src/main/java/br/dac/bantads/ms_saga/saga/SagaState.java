package br.dac.bantads.ms_saga.saga;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class SagaState {

    private String type;
    private String currentStep;
    private String status;
    private String startPayload;
    private Map<String, Object> context;
    private List<String> stepsCompleted;
    private Instant createdAt;

    public static SagaState start(String type, String startPayload) {
        return SagaState.builder()
                .type(type)
                .status("RUNNING")
                .startPayload(startPayload)
                .context(new HashMap<>())
                .stepsCompleted(new ArrayList<>())
                .createdAt(Instant.now())
                .build();
    }
}
