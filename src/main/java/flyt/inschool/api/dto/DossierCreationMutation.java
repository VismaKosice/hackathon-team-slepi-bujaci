package flyt.inschool.api.dto;

import java.time.LocalDate;
import java.util.Map;

public class DossierCreationMutation extends CalculationMutation {
    public DossierCreationMutation() {
        super();
    }

    public DossierCreationMutation(String mutationId, String mutationDefinitionName,
                                   LocalDate actualAt, Map<String, Object> mutationProperties) {
        super(mutationId, mutationDefinitionName, "DOSSIER_CREATION", actualAt, mutationProperties);
    }
}
