package flyt.inschool.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Map;

public class DossierMutation extends CalculationMutation {
    @JsonProperty("dossier_id")
    private String dossierId;

    public DossierMutation() {
        super();
    }

    public DossierMutation(String mutationId, String mutationDefinitionName,
                          LocalDate actualAt, String dossierId, Map<String, Object> mutationProperties) {
        super(mutationId, mutationDefinitionName, "DOSSIER", actualAt, mutationProperties);
        this.dossierId = dossierId;
    }

    public String getDossierId() { return dossierId; }
    public void setDossierId(String dossierId) { this.dossierId = dossierId; }
}
