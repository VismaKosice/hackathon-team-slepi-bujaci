package flyt.inschool.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class CalculationMutation {
    @JsonProperty("mutation_id")
    private String mutationId;

    @JsonProperty("mutation_definition_name")
    private String mutationDefinitionName;

    @JsonProperty("mutation_type")
    private String mutationType;

    @JsonProperty("actual_at")
    private String actualAt;

    @JsonProperty("dossier_id")
    private String dossierId;

    @JsonProperty("mutation_properties")
    private Map<String, Object> mutationProperties;

    public CalculationMutation() {}

    public String getMutationId() {
        return mutationId;
    }

    public void setMutationId(String mutationId) {
        this.mutationId = mutationId;
    }

    public String getMutationDefinitionName() {
        return mutationDefinitionName;
    }

    public void setMutationDefinitionName(String mutationDefinitionName) {
        this.mutationDefinitionName = mutationDefinitionName;
    }

    public String getMutationType() {
        return mutationType;
    }

    public void setMutationType(String mutationType) {
        this.mutationType = mutationType;
    }

    public String getActualAt() {
        return actualAt;
    }

    public void setActualAt(String actualAt) {
        this.actualAt = actualAt;
    }

    public String getDossierId() {
        return dossierId;
    }

    public void setDossierId(String dossierId) {
        this.dossierId = dossierId;
    }

    public Map<String, Object> getMutationProperties() {
        return mutationProperties;
    }

    public void setMutationProperties(Map<String, Object> mutationProperties) {
        this.mutationProperties = mutationProperties;
    }
}
