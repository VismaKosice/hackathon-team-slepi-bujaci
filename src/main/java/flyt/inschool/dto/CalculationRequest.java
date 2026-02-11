package flyt.inschool.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CalculationRequest {
    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("calculation_instructions")
    private CalculationInstructions calculationInstructions;

    public CalculationRequest() {}

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public CalculationInstructions getCalculationInstructions() {
        return calculationInstructions;
    }

    public void setCalculationInstructions(CalculationInstructions calculationInstructions) {
        this.calculationInstructions = calculationInstructions;
    }

    public static class CalculationInstructions {
        @JsonProperty("mutations")
        private List<CalculationMutation> mutations;

        public CalculationInstructions() {}

        public List<CalculationMutation> getMutations() {
            return mutations;
        }

        public void setMutations(List<CalculationMutation> mutations) {
            this.mutations = mutations;
        }
    }
}
