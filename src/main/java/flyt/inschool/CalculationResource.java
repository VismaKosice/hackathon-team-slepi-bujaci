package flyt.inschool;

import flyt.inschool.dto.CalculationRequest;
import flyt.inschool.dto.CalculationResponse;
import flyt.inschool.service.CalculationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/calculation-requests")
public class CalculationResource {

    @Inject
    CalculationService calculationService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response processCalculationRequest(CalculationRequest request) {
        try {
            CalculationResponse response = calculationService.processCalculationRequest(request);
            return Response.ok(response).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Internal server error: " + e.getMessage()))
                .build();
        }
    }

    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
