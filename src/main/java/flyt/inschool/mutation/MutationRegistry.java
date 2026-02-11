package flyt.inschool.mutation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class MutationRegistry {

    private final Map<String, Mutation> mutations = new HashMap<>();

    @Inject
    public MutationRegistry(Instance<Mutation> mutationInstances) {
        for (Mutation mutation : mutationInstances) {
            mutations.put(mutation.getMutationDefinitionName(), mutation);
        }
    }

    public Mutation getMutation(String mutationDefinitionName) {
        return mutations.get(mutationDefinitionName);
    }
}
