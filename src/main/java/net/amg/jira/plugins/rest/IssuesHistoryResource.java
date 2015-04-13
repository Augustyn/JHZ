package net.amg.jira.plugins.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.user.UserManager;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Resource providing all Issues in all Projects for which the requesting User has BROWSE Permission.
 */
@Path("/issues")
public class IssuesHistoryResource {

    private UserManager userManager;
    private PermissionManager permissionManager;
    private UserUtil userUtil;
    private ProjectManager projectManager;
    private IssueManager issueManager;

    /**
     * Used by Spring to inject dependencies
     * @param userManager
     * @param userUtil
     * @param permissionManager
     * @param projectManager
     * @param issueManager
     */
    public IssuesHistoryResource(UserManager userManager, UserUtil userUtil, PermissionManager permissionManager,
                                 ProjectManager projectManager, IssueManager issueManager) {
        this.userManager = userManager;
        this.userUtil = userUtil;
        this.permissionManager = permissionManager;
        this.projectManager = projectManager;
        this.issueManager = issueManager;
    }

    /**
     * Returns all Issues in all Projects for which the requesting User has BROWSE Permission. Response is an
     * IssuesHistoryResourceModel in JSON format.
     * @param request
     * @return JSON representation of IssuesHistoryResource
     */
    @GET
    @AnonymousAllowed
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIssues(@Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        ApplicationUser user = userUtil.getUserByName(username);
        ArrayList<Project> projects = new ArrayList<>();
        projects.addAll(permissionManager.getProjects(Permissions.Permission.BROWSE.getId(), user));
        ArrayList<IssueRepresentation> issues = new ArrayList<>();
        for (Project project : projects) {
            try {
                for (Issue issue : issueManager.getIssueObjects(issueManager.getIssueIdsForProject(project.getId()))) {
                    issues.add(new IssueRepresentation(issue));
                }
            } catch (GenericEntityException e) {
                String message = "Unable to get Issue id for Project "+project;
                Logger.getLogger(this.getClass()).error(message,e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message+" "+e).build();
            }
        }
        IssuesHistoryResourceModel issuesHistoryResource = new IssuesHistoryResourceModel(issues);
        return Response.ok(issuesHistoryResource).build();
    }

    /**
     * Returns all Issues in Projects with given name, for which the requesting User has BROWSE Permission. Response is
     * an IssuesHistoryResourceModel in JSON format.
     * @param projectName
     * @param request
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{projectName}")
    public Response getProjectIssues(@PathParam("projectName") String projectName,@Context HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        ApplicationUser user = userUtil.getUserByName(username);
        ArrayList<Project> projects = new ArrayList<>();
        projects.addAll(permissionManager.getProjects(Permissions.Permission.BROWSE.getId(), user));
        for(Project project : new ArrayList<>(projects)) {
            if(!project.getName().equals(projectName)) {
                projects.remove(project);
            }
        }
        ArrayList<IssueRepresentation> issues = new ArrayList<>();
        for (Project project : projects) {
            try {
                for (Issue issue : issueManager.getIssueObjects(issueManager.getIssueIdsForProject(project.getId()))) {
                    issues.add(new IssueRepresentation(issue));
                }
            } catch (GenericEntityException e) {
                String message = "Unable to get Issue id for Project "+project;
                Logger.getLogger(this.getClass()).error(message,e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message+" "+e).build();
            }
        }
        IssuesHistoryResourceModel issuesHistoryResource = new IssuesHistoryResourceModel(issues);
        return Response.ok(issuesHistoryResource).build();
    }
}