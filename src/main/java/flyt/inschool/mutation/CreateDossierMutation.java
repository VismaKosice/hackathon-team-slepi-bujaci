package flyt.inschool.mutation;

import flyt.inschool.dto.CalculationResponse.CalculationMessage;
import flyt.inschool.model.Dossier;
import flyt.inschool.model.Person;
import flyt.inschool.model.Situation;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CreateDossierMutation extends BaseMutation {

    @Override
    public String getMutationDefinitionName() {
        return "create_dossier";
    }

    @Override
    public List<CalculationMessage> validate(Situation situation, Map<String, Object> properties) {
        List<CalculationMessage> messages = new ArrayList<>();

        // Check if dossier already exists
        if (situation.getDossier() != null) {
            messages.add(createCriticalMessage("DOSSIER_ALREADY_EXISTS", 
                "A dossier already exists in the situation"));
            return messages;
        }

        // Validate birth_date
        String birthDate = getStringProperty(properties, "birth_date");
        if (!isValidDate(birthDate)) {
            messages.add(createCriticalMessage("INVALID_BIRTH_DATE", 
                "birth_date is not a valid date or is in the future"));
            return messages;
        }

        LocalDate birth = parseDate(birthDate);
        if (birth.isAfter(LocalDate.now())) {
            messages.add(createCriticalMessage("INVALID_BIRTH_DATE", 
                "birth_date is not a valid date or is in the future"));
            return messages;
        }

        // Validate name
        String name = getStringProperty(properties, "name");
        if (isNullOrBlank(name)) {
            messages.add(createCriticalMessage("INVALID_NAME", "name is empty or blank"));
            return messages;
        }

        return messages;
    }

    @Override
    public List<CalculationMessage> apply(Situation situation, Map<String, Object> properties) {
        List<CalculationMessage> messages = new ArrayList<>();

        String dossierId = getStringProperty(properties, "dossier_id");
        String personId = getStringProperty(properties, "person_id");
        String name = getStringProperty(properties, "name");
        String birthDate = getStringProperty(properties, "birth_date");

        // Create dossier
        Dossier dossier = new Dossier(dossierId, "ACTIVE");

        // Create participant
        Person participant = new Person(personId, "PARTICIPANT", name, birthDate);
        dossier.getPersons().add(participant);

        situation.setDossier(dossier);

        return messages;
    }
}
