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
import com.atlassian.jira.datetime.DateTimeFormatter;
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

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static net.amg.jira.plugins.jhz.model.FormField.daysBackPattern;

/**
 * Resource providing chart generation service
 *
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
    private DateTimeFormatter dateTimeFormatter;

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
        Gson gson = new Gson();
        try {
            issues = URLDecoder.decode(issues,"UTF-8");
        } catch (UnsupportedEncodingException ex) {
            String timestamp = generateTimestamp();
            logger.error("{} Unable to decode issues parameter:{} cause:{}", new Object[]{timestamp,issues, ex});
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(timestamp)).build();
        }
        Map<FormField, String> paramMap = new HashMap<>();
        paramMap.put(FormField.PROJECT, project);
        paramMap.put(FormField.ISSUES, issues);
        paramMap.put(FormField.DATE, date);
        paramMap.put(FormField.PERIOD, periodName);
        paramMap.put(FormField.VERSION, versionLabel);
        ErrorCollection errorCollection = validator.validate(paramMap);
        if (!errorCollection.isEmpty()) {
            String timestamp =  generateTimestamp();
            logger.error("{} Invalid rest request parameters: project={}, issues={}, date={}, period={}",
                    new Object[]{timestamp, project, issues, date, periodName});
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(timestamp))
                    .entity(gson.toJson(errorCollection)).build();
        }
        Date dateBegin = null;

        final Map<String, Set<String>> statusesSets = searchService.getGroupedIssueTypes(issues);
        try {
            dateBegin = getBeginDate(date);
        } catch (ParseException | NumberFormatException ex) {
            String timestamp = generateTimestamp();
            logger.error("{} Unable to parse date for chart generation date={} cause: {}", new Object[]{timestamp,date,ex});
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(timestamp)).build();
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
            logger.error("{} Unable to parse project or filter: project={} cause:{}",new Object[]{timestamp, project,ex});
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(timestamp)).build();
        }

        Chart chart = null;
        try {
            chart = jiraChartService.generateChart(projectOrFilter, ChartFactory.PeriodName.valueOf(periodName.toLowerCase()),
                    label, dateBegin, statusesSets, width, height);
        } catch (IOException | SearchException ex) {
            String timestamp = generateTimestamp();
            logger.error("{} Unable to generate chart with parameters: project={}, issues={}, date={}, period={}" +
                            " width={}, height={}, version={}, table={} cause:{}",
                    new Object[]{timestamp,project, issues, date, periodName, width, height, versionLabel, table, ex});
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(timestamp)).build();
        }

        IssuesHistoryChartModel jiraIssuesHistoryChart = new IssuesHistoryChartModel(chart.getLocation(), getProjectNameOrFilterTitle(projectOrFilter),
                chart.getImageMap(), chart.getImageMapName(), width, height);
        if (table) {
            jiraIssuesHistoryChart.setTable(new Table(jiraChartService.getTable(),dateTimeFormatter));
        }
        return Response.ok(jiraIssuesHistoryChart).cacheControl(CacheControl.NO_CACHE).build();
    }

    @POST
    @Path("{args : (.*)?}")
    public Response postStub() {
        return Response.status(Response.Status.NOT_FOUND).entity("No such resource").build();
    }

    @PUT
    @Path("{args : (.*)?}")
    public Response putStub() {
        return Response.status(Response.Status.NOT_FOUND).entity("No such resource").build();
    }

    @DELETE
    @Path("{args : (.*)?}")
    public Response deleteStub() {
        return Response.status(Response.Status.NOT_FOUND).entity("No such resource").build();
    }

    private Date getBeginDate(String date) throws ParseException, NumberFormatException {
        Date beginningDate;
        if (daysBackPattern.matcher(date).matches()) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -Integer.parseInt(date.replaceAll("[\\wd-]", "")));
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
                        new Long(projectOrFilterId.getId()));
                return null == searchRequest ? null : searchRequest.getName();
            default:
                return "gadget.common.anonymous.filter";
        }
    }

    private String generateTimestamp() {
        return "Timestamp:"+System.currentTimeMillis();
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

    @ServiceReference
    public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }
}
