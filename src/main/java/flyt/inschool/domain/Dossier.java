package flyt.inschool.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Dossier(
    @JsonProperty("dossier_id") String dossierId,
    @JsonProperty("status") DossierStatus status,
    @JsonProperty("retirement_date") LocalDate retirementDate,
    @JsonProperty("persons") List<Person> persons,
    @JsonProperty("policies") List<Policy> policies
) {
    public Dossier withStatus(DossierStatus newStatus) {
        return new Dossier(dossierId, newStatus, retirementDate, persons, policies);
    }

    public Dossier withRetirement(LocalDate retirementDate) {
        return new Dossier(dossierId, DossierStatus.RETIRED, retirementDate, persons, policies);
    }

    public Dossier addPolicy(Policy policy) {
        List<Policy> updatedPolicies = new ArrayList<>(policies);
        updatedPolicies.add(policy);
        return new Dossier(dossierId, status, retirementDate, persons, updatedPolicies);
    }

    public Dossier updatePolicies(List<Policy> updatedPolicies) {
        return new Dossier(dossierId, status, retirementDate, persons, updatedPolicies);
    }

    public int getPolicyCount() {
        return policies.size();
    }
}
