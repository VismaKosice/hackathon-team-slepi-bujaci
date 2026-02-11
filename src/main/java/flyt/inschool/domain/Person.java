package flyt.inschool.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record Person(
    @JsonProperty("person_id") String personId,
    @JsonProperty("role") PersonRole role,
    @JsonProperty("name") String name,
    @JsonProperty("birth_date") LocalDate birthDate
) {
}
