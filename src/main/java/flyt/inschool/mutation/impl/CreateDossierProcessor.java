package flyt.inschool.mutation.impl;

import flyt.inschool.domain.*;
import flyt.inschool.mutation.MutationContext;
import flyt.inschool.mutation.MutationProcessor;
import flyt.inschool.mutation.MutationResult;
import flyt.inschool.validation.DateValidator;
import flyt.inschool.validation.MessageCode;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CreateDossierProcessor implements MutationProcessor {

    @Override
    public MutationResult process(MutationContext context) {
        Map<String, Object> props = context.mutation().getMutationProperties();

        // Validate: dossier already exists
        if (context.currentSituation().dossier() != null) {
            context.validationContext().addCritical(MessageCode.DOSSIER_ALREADY_EXISTS);
            return new MutationResult(context.currentSituation(), true);
        }

        // Extract properties
        String dossierId = (String) props.get("dossier_id");
        String personId = (String) props.get("person_id");
        String name = (String) props.get("name");
        LocalDate birthDate = LocalDate.parse((String) props.get("birth_date"));

        // Validate: name not empty
        if (name == null || name.trim().isEmpty()) {
            context.validationContext().addCritical(MessageCode.INVALID_NAME);
            return new MutationResult(context.currentSituation(), true);
        }

        // Validate: birth date not in future
        if (!DateValidator.isValidBirthDate(birthDate)) {
            context.validationContext().addCritical(MessageCode.INVALID_BIRTH_DATE);
            return new MutationResult(context.currentSituation(), true);
        }

        // Create person
        Person person = new Person(personId, PersonRole.PARTICIPANT, name, birthDate);
        List<Person> persons = List.of(person);

        // Create dossier
        Dossier dossier = new Dossier(
            dossierId,
            DossierStatus.ACTIVE,
            null,  // retirement_date is null initially
            persons,
            new ArrayList<>()  // empty policies list
        );

        // Create new situation
        Situation newSituation = new Situation(dossier);

        return new MutationResult(newSituation, false);
    }
}
