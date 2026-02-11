package flyt.inschool.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class Dossier {
    @JsonProperty("dossier_id")
    private String dossierId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("retirement_date")
    private String retirementDate;

    @JsonProperty("persons")
    private List<Person> persons;

    @JsonProperty("policies")
    private List<Policy> policies;

    public Dossier() {
        this.persons = new ArrayList<>();
        this.policies = new ArrayList<>();
    }

    public Dossier(String dossierId, String status) {
        this.dossierId = dossierId;
        this.status = status;
        this.retirementDate = null;
        this.persons = new ArrayList<>();
        this.policies = new ArrayList<>();
    }

    public String getDossierId() {
        return dossierId;
    }

    public void setDossierId(String dossierId) {
        this.dossierId = dossierId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRetirementDate() {
        return retirementDate;
    }

    public void setRetirementDate(String retirementDate) {
        this.retirementDate = retirementDate;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

    public List<Policy> getPolicies() {
        return policies;
    }

    public void setPolicies(List<Policy> policies) {
        this.policies = policies;
    }
}
