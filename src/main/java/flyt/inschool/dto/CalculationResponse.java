package flyt.inschool.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import flyt.inschool.model.Situation;
import java.util.ArrayList;
import java.util.List;

public class CalculationResponse {
    @JsonProperty("calculation_metadata")
    private CalculationMetadata calculationMetadata;

    @JsonProperty("calculation_result")
    private CalculationResult calculationResult;

    public CalculationResponse() {}

    public CalculationMetadata getCalculationMetadata() {
        return calculationMetadata;
    }

    public void setCalculationMetadata(CalculationMetadata calculationMetadata) {
        this.calculationMetadata = calculationMetadata;
    }

    public CalculationResult getCalculationResult() {
        return calculationResult;
    }

    public void setCalculationResult(CalculationResult calculationResult) {
        this.calculationResult = calculationResult;
    }

    public static class CalculationMetadata {
        @JsonProperty("calculation_id")
        private String calculationId;

        @JsonProperty("tenant_id")
        private String tenantId;

        @JsonProperty("calculation_started_at")
        private String calculationStartedAt;

        @JsonProperty("calculation_completed_at")
        private String calculationCompletedAt;

        @JsonProperty("calculation_duration_ms")
        private long calculationDurationMs;

        @JsonProperty("calculation_outcome")
        private String calculationOutcome;

        public CalculationMetadata() {}

        public String getCalculationId() {
            return calculationId;
        }

        public void setCalculationId(String calculationId) {
            this.calculationId = calculationId;
        }

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getCalculationStartedAt() {
            return calculationStartedAt;
        }

        public void setCalculationStartedAt(String calculationStartedAt) {
            this.calculationStartedAt = calculationStartedAt;
        }

        public String getCalculationCompletedAt() {
            return calculationCompletedAt;
        }

        public void setCalculationCompletedAt(String calculationCompletedAt) {
            this.calculationCompletedAt = calculationCompletedAt;
        }

        public long getCalculationDurationMs() {
            return calculationDurationMs;
        }

        public void setCalculationDurationMs(long calculationDurationMs) {
            this.calculationDurationMs = calculationDurationMs;
        }

        public String getCalculationOutcome() {
            return calculationOutcome;
        }

        public void setCalculationOutcome(String calculationOutcome) {
            this.calculationOutcome = calculationOutcome;
        }
    }

    public static class CalculationResult {
        @JsonProperty("messages")
        private List<CalculationMessage> messages;

        @JsonProperty("initial_situation")
        private SituationSnapshot initialSituation;

        @JsonProperty("mutations")
        private List<ProcessedMutation> mutations;

        @JsonProperty("end_situation")
        private SituationSnapshot endSituation;

        public CalculationResult() {
            this.messages = new ArrayList<>();
            this.mutations = new ArrayList<>();
        }

        public List<CalculationMessage> getMessages() {
            return messages;
        }

        public void setMessages(List<CalculationMessage> messages) {
            this.messages = messages;
        }

        public SituationSnapshot getInitialSituation() {
            return initialSituation;
        }

        public void setInitialSituation(SituationSnapshot initialSituation) {
            this.initialSituation = initialSituation;
        }

        public List<ProcessedMutation> getMutations() {
            return mutations;
        }

        public void setMutations(List<ProcessedMutation> mutations) {
            this.mutations = mutations;
        }

        public SituationSnapshot getEndSituation() {
            return endSituation;
        }

        public void setEndSituation(SituationSnapshot endSituation) {
            this.endSituation = endSituation;
        }
    }

    public static class CalculationMessage {
        @JsonProperty("level")
        private String level;

        @JsonProperty("code")
        private String code;

        @JsonProperty("message")
        private String message;

        public CalculationMessage() {}

        public CalculationMessage(String level, String code, String message) {
            this.level = level;
            this.code = code;
            this.message = message;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class SituationSnapshot {
        @JsonProperty("mutation_id")
        private String mutationId;

        @JsonProperty("mutation_index")
        private Integer mutationIndex;

        @JsonProperty("actual_at")
        private String actualAt;

        @JsonProperty("situation")
        private Situation situation;

        public SituationSnapshot() {}

        public String getMutationId() {
            return mutationId;
        }

        public void setMutationId(String mutationId) {
            this.mutationId = mutationId;
        }

        public Integer getMutationIndex() {
            return mutationIndex;
        }

        public void setMutationIndex(Integer mutationIndex) {
            this.mutationIndex = mutationIndex;
        }

        public String getActualAt() {
            return actualAt;
        }

        public void setActualAt(String actualAt) {
            this.actualAt = actualAt;
        }

        public Situation getSituation() {
            return situation;
        }

        public void setSituation(Situation situation) {
            this.situation = situation;
        }
    }

    public static class ProcessedMutation {
        @JsonProperty("mutation")
        private CalculationMutation mutation;

        @JsonProperty("calculation_message_indexes")
        private List<Integer> calculationMessageIndexes;

        public ProcessedMutation() {
            this.calculationMessageIndexes = new ArrayList<>();
        }

        public ProcessedMutation(CalculationMutation mutation) {
            this.mutation = mutation;
            this.calculationMessageIndexes = new ArrayList<>();
        }

        public CalculationMutation getMutation() {
            return mutation;
        }

        public void setMutation(CalculationMutation mutation) {
            this.mutation = mutation;
        }

        public List<Integer> getCalculationMessageIndexes() {
            return calculationMessageIndexes;
        }

        public void setCalculationMessageIndexes(List<Integer> calculationMessageIndexes) {
            this.calculationMessageIndexes = calculationMessageIndexes;
        }
    }
}
