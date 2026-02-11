package flyt.inschool.mutation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class MutationRegistry {
    private final Map<String, MutationProcessor> processors = new ConcurrentHashMap<>();

    public MutationRegistry() {
    }

    @Inject
    public void init(
        flyt.inschool.mutation.impl.CreateDossierProcessor createDossier,
        flyt.inschool.mutation.impl.AddPolicyProcessor addPolicy,
        flyt.inschool.mutation.impl.ApplyIndexationProcessor applyIndexation,
        flyt.inschool.mutation.impl.CalculateRetirementBenefitProcessor calculateRetirement
    ) {
        register("create_dossier", createDossier);
        register("add_policy", addPolicy);
        register("apply_indexation", applyIndexation);
        register("calculate_retirement_benefit", calculateRetirement);
    }

    public void register(String name, MutationProcessor processor) {
        processors.put(name, processor);
    }

    public MutationProcessor get(String name) {
        return processors.get(name);
    }
}
