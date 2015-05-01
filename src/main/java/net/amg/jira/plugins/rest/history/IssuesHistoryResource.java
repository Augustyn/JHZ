package net.amg.jira.plugins.rest.history;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.user.UserManager;
import com.google.gson.Gson;
import net.amg.jira.plugins.components.InvalidPreferenceException;
import net.amg.jira.plugins.components.SearchService;
import org.ofbiz.core.entity.GenericEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Resource providing all Issues in all Projects for which the requesting User has BROWSE Permission.
 */
@Path("/issues")
public class IssuesHistoryResource {

    private static final Logger logger = LoggerFactory.getLogger(IssuesHistoryResource.class);

    private final UserManager userManager;
    private final PermissionManager permissionManager;
    private final UserUtil userUtil;
    private final IssueManager issueManager;
    private final SearchService searchService;

    /**
     * Used by Spring to inject dependencies
     *
     * @param userManager
     * @param userUtil
     * @param permissionManager
     * @param issueManager
     * @param searchService
     */
    public IssuesHistoryResource(UserManager userManager, UserUtil userUtil, PermissionManager permissionManager,
                                 IssueManager issueManager, SearchService searchService) {
        this.userManager = userManager;
        this.userUtil = userUtil;
        this.permissionManager = permissionManager;
        this.issueManager = issueManager;
        this.searchService = searchService;
    }

    /**
     * Returns all Issues in all Projects for which the requesting User has BROWSE Permission. Response is an
     * IssuesHistoryResourceModel in JSON format.
     *
     * @param request
     * @return JSON representation of IssuesHistoryResource
     */
    @GET
    @AnonymousAllowed
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIssues(@Context HttpServletRequest request) {
        ApplicationUser user = userUtil.getUserByName(userManager.getRemoteUsername(request));
        List<Project> projects = new ArrayList<>();
        projects.addAll(permissionManager.getProjects(Permissions.Permission.BROWSE.getId(), user));
        List<IssueRepresentation> issues = new ArrayList<>();
        for (Project project : projects) {
            try {
                Collection<Long> idCollection = issueManager.getIssueIdsForProject(project.getId());
                if (!idCollection.isEmpty()) {
                    for (Issue issue : issueManager.getIssueObjects(idCollection)) {
                        issues.add(new IssueRepresentation(issue));
                    }
                }
            } catch (GenericEntityException e) {
                String message = "Unable to get Issue id for Project " + project;
                logger.error(message, e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
            }
        }
        IssuesHistoryResourceModel issuesHistoryResource = new IssuesHistoryResourceModel(issues);
        return Response.ok(issuesHistoryResource).build();
    }

    /**
     * Returns all Issues in Projects with given name, for which the requesting User has BROWSE Permission. Response is
     * an IssuesHistoryResourceModel in JSON format.
     *
     * @param projectName of the Project containing requested issues
     * @param request
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/byProjectName/{projectName: .*}")
    public Response getProjectIssues(@PathParam("projectName") String projectName, @Context HttpServletRequest request) {
        if (projectName.isEmpty()) {
            String cause = "Missing request parameter projectName.";
            logger.debug(cause);
            return Response.status(Response.Status.BAD_REQUEST).entity(cause).build();
        }
        ApplicationUser user = userUtil.getUserByName(userManager.getRemoteUsername(request));
        List<Project> projects = new ArrayList<>();
        for (Project project : permissionManager.getProjects(Permissions.Permission.BROWSE.getId(), user)) {
            if (project.getName().equals(projectName)) {
                projects.add(project);
            }
        }
        List<IssueRepresentation> issues = new ArrayList<>();
        for (Project project : projects) {
            try {
                Collection<Long> idCollection = issueManager.getIssueIdsForProject(project.getId());
                if (!idCollection.isEmpty()) {
                    for (Issue issue : issueManager.getIssueObjects(idCollection)) {
                        issues.add(new IssueRepresentation(issue));
                    }
                }
            } catch (GenericEntityException e) {
                String message = "Unable to get Issue id for Project " + project;
                logger.error(message, e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
            }
        }
        IssuesHistoryResourceModel issuesHistoryResource = new IssuesHistoryResourceModel(issues);
        return Response.ok(issuesHistoryResource).build();
    }

    /**
     * Returns issue history required by the gadget according to the user preferences.
     * @param projectOrFilter id of filter or project, from which the issues are extracted
     * @param issues statuses of the issues to be acquired
     * @param previously date or number of days from the present moment, which constitute the beginning of requested
     *                   issue history
     * @return Response with issue history (IssueHistoryResourceModel) in JSON format.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/issues/history")
    public Response getIssuesHistory(@QueryParam("Project") String projectOrFilter, @QueryParam("Issues") String issues
            , @QueryParam("Previously") String previously) {
        List<Issue> issueList = null;
        try {
            issueList = searchService.findIssues(projectOrFilter, issues, previously);
        } catch (SearchException | InvalidPreferenceException | ParseException e) {
            String message = String.format("Unable to get Issue history for projectOrFilter=%1 issues=%2 previously=%3",
                    projectOrFilter, issues, previously);
            logger.error(message, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
        }
        List<IssueRepresentation> issueRepresentations = new ArrayList<>();
        for (Issue issue : issueList) {
            issueRepresentations.add(new IssueRepresentation(issue));
        }
        Gson gson = new Gson();
        return Response.ok(gson.toJson(new IssuesHistoryResourceModel(issueRepresentations))).build();
    }

    /**
     * Exposes all available statuses in Jira for the currently logged in user
     * @return Response with statuses (StatusRepresentations) in JSON format.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/issues/statuses")
    public Response getStatusTypes() {
        Collection<Status> allStatuses = searchService.findAllStatuses();
        Gson gson = new Gson();
        return Response.ok(gson.toJson(new StatusRepresentations(allStatuses))).build();
    }
}