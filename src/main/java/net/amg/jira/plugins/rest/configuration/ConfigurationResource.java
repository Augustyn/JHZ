package net.amg.jira.plugins.rest.configuration;

import com.atlassian.jira.rest.api.messages.TextMessage;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import net.amg.jira.plugins.components.Validator;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * Resource providing validation service for the gadget.
 * Created by Ivo on 18/04/15.
 */
@Path("/configuration")
public class ConfigurationResource {

    Validator validator;

    /**
     * Accepts field values and checks the configuration for errors.
     *
     * @param project    value of Project field
     * @param issues     values of Issues field
     * @param period     value of Period field
     * @param previously value of Previously field
     * @return JAXB object encapsulating errors detected.
     */
    @Path("/validate")
    @GET
    @AnonymousAllowed
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPrefsValidation(@QueryParam("Project") String project, @QueryParam("Issues") String issues
            , @QueryParam("Period") String period, @QueryParam("Previously") String previously) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("Project", project);
        paramMap.put("Issues", issues);
        paramMap.put("Period", period);
        paramMap.put("Previously", previously);
        ErrorCollection errorCollection = validator.validate(paramMap);
        if (errorCollection.isEmpty()) {
            return Response.ok(new TextMessage("No input configuration errors found.")).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(errorCollection).build();
        }
    }

    public Validator getValidator() {
        return validator;
    }

    /**
     * Used by Spring to autowire a component.
     *
     * @param validator
     */
    public void setValidator(Validator validator) {
        this.validator = validator;
    }
}
