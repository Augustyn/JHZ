package net.amg.jira.plugins.rest.controller;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.status.Status;
import com.google.gson.Gson;
import net.amg.jira.plugins.model.FormField;
import net.amg.jira.plugins.rest.model.ErrorCollection;
import net.amg.jira.plugins.rest.model.IssuesHistoryResourceModel;
import net.amg.jira.plugins.rest.model.StatusesResourceModel;
import net.amg.jira.plugins.services.SearchService;
import net.amg.jira.plugins.services.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.util.*;

/**
 * Resource providing all Issues in all Projects for which the requesting User has BROWSE Permission.
 */
@Path("/issues")
public class IssuesHistoryResource {

    private static final Logger logger = LoggerFactory.getLogger(IssuesHistoryResource.class);

    private final SearchService searchService;
    private final Validator validator;

    /**
     * Used by Spring to inject dependencies
     *
     * @param searchService
     * @param validator
     */
    public IssuesHistoryResource(SearchService searchService, Validator validator) {
        this.searchService = searchService;
        this.validator = validator;
    }

    /**
     * Returns issue history required by the gadget according to the user preferences.
     *
     * @param project id of filter or project, from which the issues are extracted
     * @param issues  statuses of the issues to be acquired
     * @param date    date or number of days from the present moment, which constitute the beginning of requested
     *                issue history
     * @return Response with issue history (IssueHistoryResourceModel) in JSON format.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/history")
    public Response getIssues(@QueryParam("Project") String project, @QueryParam("Issues") String issues
            , @QueryParam("Date") String date) {
        Map<FormField, String> paramMap = new HashMap<>();
        paramMap.put(FormField.PROJECT, project);
        paramMap.put(FormField.ISSUES, issues);
        paramMap.put(FormField.DATE, date);
        ErrorCollection errorCollection = validator.validate(paramMap);
        Gson gson = new Gson();
        if (!errorCollection.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(errorCollection)).build();
        }
        Map<String,List<Issue>> issueList = null;
        try {
            issueList = searchService.findIssues(project, issues, date);
        } catch (SearchException | ParseException e) {
            String timestamp = "TIMESTAMP: " +
                    new java.text.SimpleDateFormat("MM/dd/yyyy h:mm:ss a").format(new Date());
            String message = String.format("Unable to get Issue history for projectOrFilter=%1 issues=%2 previously=%3",
                    project, issues, date);
            logger.error(timestamp, message, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(timestamp))
                    .entity(gson.toJson(message)).build();
        }
        return Response.ok(gson.toJson(new IssuesHistoryResourceModel(issueList))).build();
    }

    /**
     * Exposes all available statuses in Jira for the currently logged in user
     *
     * @return Response with statuses (StatusRepresentations) in JSON format.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/statuses")
    public Response getStatusTypes() {
        Collection<Status> allStatuses = searchService.findAllStatuses();
        Gson gson = new Gson();
        return Response.ok(gson.toJson(new StatusesResourceModel(allStatuses))).build();
    }
}