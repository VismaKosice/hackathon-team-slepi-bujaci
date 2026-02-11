package flyt.inschool.mutation;

@FunctionalInterface
public interface MutationProcessor {
    MutationResult process(MutationContext context);
}
