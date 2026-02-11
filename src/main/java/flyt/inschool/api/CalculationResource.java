package flyt.inschool.api;

import flyt.inschool.api.dto.CalculationRequest;
import flyt.inschool.api.dto.CalculationResponse;
import flyt.inschool.api.dto.ErrorResponse;
import flyt.inschool.service.CalculationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
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
            if (request == null || request.calculationInstructions() == null) {
                return Response.status(400)
                    .entity(new ErrorResponse(400, "Invalid request structure"))
                    .build();
            }

            if (request.calculationInstructions().mutations() == null
                || request.calculationInstructions().mutations().isEmpty()) {
                return Response.status(400)
                    .entity(new ErrorResponse(400, "Mutations list cannot be empty"))
                    .build();
            }

            CalculationResponse response = calculationService.process(request);
            return Response.ok(response).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                .entity(new ErrorResponse(500, "Internal server error: " + e.getMessage()))
                .build();
        }
    }
}
