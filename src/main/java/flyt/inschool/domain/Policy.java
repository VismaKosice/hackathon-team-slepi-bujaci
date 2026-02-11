package flyt.inschool.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Policy(
    @JsonProperty("policy_id") String policyId,
    @JsonProperty("scheme_id") String schemeId,
    @JsonProperty("employment_start_date") LocalDate employmentStartDate,
    @JsonProperty("salary") double salary,
    @JsonProperty("part_time_factor") double partTimeFactor,
    @JsonProperty("attainable_pension") Double attainablePension,
    @JsonProperty("projections") List<Projection> projections
) {
    public Policy withSalary(double newSalary) {
        return new Policy(policyId, schemeId, employmentStartDate, newSalary, partTimeFactor, attainablePension, projections);
    }

    public Policy withAttainablePension(double pension) {
        return new Policy(policyId, schemeId, employmentStartDate, salary, partTimeFactor, pension, projections);
    }

    public Policy withProjections(List<Projection> newProjections) {
        return new Policy(policyId, schemeId, employmentStartDate, salary, partTimeFactor, attainablePension, newProjections);
    }

    public record Projection(
        @JsonProperty("date") LocalDate date,
        @JsonProperty("projected_pension") double projectedPension
    ) {}
}
