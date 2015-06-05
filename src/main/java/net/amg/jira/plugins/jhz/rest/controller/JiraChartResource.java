/*
 * Copyright 2015 AMG.net - Politechnika Łódzka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.amg.jira.plugins.jhz.rest.controller;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.v1.util.CacheControl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.gson.Gson;
import net.amg.jira.plugins.jhz.model.FormField;
import net.amg.jira.plugins.jhz.model.ProjectOrFilter;
import net.amg.jira.plugins.jhz.model.ProjectsType;
import net.amg.jira.plugins.jhz.rest.model.ErrorCollection;
import net.amg.jira.plugins.jhz.rest.model.IssuesHistoryChartModel;
import net.amg.jira.plugins.jhz.rest.model.Table;
import net.amg.jira.plugins.jhz.services.JiraChartServiceImpl;
import net.amg.jira.plugins.jhz.services.SearchServiceImpl;
import net.amg.jira.plugins.jhz.services.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static net.amg.jira.plugins.jhz.model.FormField.daysBackPattern;

/**
 * Resource providing chart generation service
 *
 * @author jarek
 */
@Path("/chart")
public class JiraChartResource {

    private static final Logger logger = LoggerFactory.getLogger(JiraChartResource.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private SearchRequestService searchRequestService;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private ProjectManager projectManager;
    private SearchServiceImpl searchService;
    private Validator validator;
    private JiraChartServiceImpl jiraChartService;

    /**
     * Allows to generate chart to be displayed in the gadget
     *
     * @param project      value of Project field
     * @param date         value of date field
     * @param periodName   value of period field
     * @param issues       values of Issues field
     * @param width        chart width
     * @param height       chart height
     * @param versionLabel value of version field
     * @param table        true if table data is requested
     * @return
     */
    @GET
    @Path("/generate")
    public Response generateChart(
            @QueryParam("project") String project,
            @QueryParam("date") String date,
            @QueryParam("period") String periodName,
            @QueryParam("issues") String issues,
            @QueryParam("width") int width,
            @QueryParam("height") int height,
            @QueryParam("version") String versionLabel,
            @QueryParam("table") boolean table) {
        Map<FormField, String> paramMap = new HashMap<>();
        //In canvas view gadget decides to replace all spaces with '+', thus the following are required:
        issues = issues.replace("+", " ");
        paramMap.put(FormField.PROJECT, project);
        paramMap.put(FormField.ISSUES, issues);
        paramMap.put(FormField.DATE, date);
        paramMap.put(FormField.PERIOD, periodName);
        ErrorCollection errorCollection = validator.validate(paramMap);
        Gson gson = new Gson();
        if (!errorCollection.isEmpty()) {
            String timestamp = generateTimestamp();
            String message = String.format("Invalid rest request parameters: project=%1, issues=%2, date=%3, period=%4",
                    project, issues, date, periodName);
            logger.error(timestamp, message);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(timestamp))
                    .entity(gson.toJson(message)).entity(gson.toJson(errorCollection)).build();
        }
        Date dateBegin = null;

        final Map<String, Set<String>> statusesSets = searchService.getGroupedIssueTypes(issues);
        try {
            dateBegin = getBeginDate(date);
        } catch (ParseException | NumberFormatException ex) {
            String timestamp = generateTimestamp();
            String message = String.format("Unable to parse date for chart generation date=%1", date);
            logger.error(timestamp, message, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(timestamp))
                    .entity(gson.toJson(message)).build();
        }

        final ChartFactory.VersionLabel label = ChartFactory.VersionLabel.valueOf(versionLabel);
        ProjectOrFilter projectOrFilter = null;

        try {
            if (validator.checkIfProject(project)) {
                projectOrFilter = new ProjectOrFilter(ProjectsType.PROJECT, Integer.parseInt(project.split("-")[1]));
            } else {
                projectOrFilter = new ProjectOrFilter(ProjectsType.FILTER, Integer.parseInt(project.split("-")[1]));
            }
        } catch (NumberFormatException ex) {
            String timestamp = generateTimestamp();
            String message = String.format("Unable to parse project or filter: project=%1", project);
            logger.error(timestamp, message, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(timestamp))
                    .entity(gson.toJson(message)).build();
        }

        Chart chart = null;
        try {
            chart = jiraChartService.generateChart(projectOrFilter, ChartFactory.PeriodName.valueOf(periodName.toLowerCase()),
                    label, dateBegin, statusesSets, width, height);
        } catch (IOException | SearchException ex) {
            String timestamp = generateTimestamp();
            String message = String.format("Unable to generate chart with parameters: project=%1, issues=%2, date=%3, period=%4" +
                            " width=%5, height=%6, version=%7, table=%8",
                    project, issues, date, periodName, width, height, versionLabel, table);
            logger.error(timestamp, message, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(timestamp))
                    .entity(gson.toJson(message)).build();
        }

        IssuesHistoryChartModel jiraIssuesHistoryChart = new IssuesHistoryChartModel(chart.getLocation(), getProjectNameOrFilterTitle(projectOrFilter),
                chart.getImageMap(), chart.getImageMapName(), width, height);
        if (table) {
            jiraIssuesHistoryChart.setTable(new Table(jiraChartService.getTable()));
        }
        return Response.ok(jiraIssuesHistoryChart).cacheControl(CacheControl.NO_CACHE).build();
    }

    private Date getBeginDate(String date) throws ParseException, NumberFormatException {
        Date beginningDate;
        if (daysBackPattern.matcher(date).matches()) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -Integer.parseInt(date.replaceAll("[d-]", "")));
            beginningDate = calendar.getTime();
        } else {
            beginningDate = dateFormat.parse(date.replace("/", "-").replace(".", "-"));
        }
        return beginningDate;
    }

    private JiraServiceContextImpl getJiraServiceContext() {
        return new JiraServiceContextImpl(jiraAuthenticationContext.getUser());
    }

    private String getProjectNameOrFilterTitle(ProjectOrFilter projectOrFilterId) {
        switch (projectOrFilterId.getType()) {
            case PROJECT:
                Project aProject = projectManager.getProjectObj(new Long(projectOrFilterId.getId()));
                return null == aProject ? null : aProject.getName();
            case FILTER:
                SearchRequest searchRequest = searchRequestService.getFilter(getJiraServiceContext(),
                        new Long(projectOrFilterId.getId())
                );
                return null == searchRequest ? null : searchRequest.getName();
            default:
                return "gadget.common.anonymous.filter";
        }
    }

    private String generateTimestamp() {
        return "TIMESTAMP: " + new java.text.SimpleDateFormat("MM/dd/yyyy h:mm:ss a").format(new Date());
    }

    @ServiceReference
    public void setSearchService(SearchServiceImpl searchService) {
        this.searchService = searchService;
    }

    @ServiceReference
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @ServiceReference
    public void setJiraChartService(JiraChartServiceImpl jiraChartService) {
        this.jiraChartService = jiraChartService;
    }

    @ServiceReference
    public void setSearchRequestService(SearchRequestService searchRequestService) {
        this.searchRequestService = searchRequestService;
    }

    @ServiceReference
    public void setJiraAuthenticationContext(JiraAuthenticationContext jiraAuthenticationContext) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @ServiceReference
    public void setProjectManager(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }
}
