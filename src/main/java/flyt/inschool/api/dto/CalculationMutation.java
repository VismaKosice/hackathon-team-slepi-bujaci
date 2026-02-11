package flyt.inschool.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDate;
import java.util.Map;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "mutation_type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DossierCreationMutation.class, name = "DOSSIER_CREATION"),
    @JsonSubTypes.Type(value = DossierMutation.class, name = "DOSSIER")
})
public abstract class CalculationMutation {
    @JsonProperty("mutation_id")
    private String mutationId;

    @JsonProperty("mutation_definition_name")
    private String mutationDefinitionName;

    @JsonProperty("mutation_type")
    private String mutationType;

    @JsonProperty("actual_at")
    private LocalDate actualAt;

    @JsonProperty("mutation_properties")
    private Map<String, Object> mutationProperties;

    public CalculationMutation() {
    }

    public CalculationMutation(String mutationId, String mutationDefinitionName, String mutationType,
                               LocalDate actualAt, Map<String, Object> mutationProperties) {
        this.mutationId = mutationId;
        this.mutationDefinitionName = mutationDefinitionName;
        this.mutationType = mutationType;
        this.actualAt = actualAt;
        this.mutationProperties = mutationProperties;
    }

    public String getMutationId() { return mutationId; }
    public String getMutationDefinitionName() { return mutationDefinitionName; }
    public String getMutationType() { return mutationType; }
    public LocalDate getActualAt() { return actualAt; }
    public Map<String, Object> getMutationProperties() { return mutationProperties; }

    public void setMutationId(String mutationId) { this.mutationId = mutationId; }
    public void setMutationDefinitionName(String mutationDefinitionName) { this.mutationDefinitionName = mutationDefinitionName; }
    public void setMutationType(String mutationType) { this.mutationType = mutationType; }
    public void setActualAt(LocalDate actualAt) { this.actualAt = actualAt; }
    public void setMutationProperties(Map<String, Object> mutationProperties) { this.mutationProperties = mutationProperties; }
}
