package flyt.inschool.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Situation(
    @JsonProperty("dossier") Dossier dossier
) {
    public Situation withDossier(Dossier newDossier) {
        return new Situation(newDossier);
    }
}
