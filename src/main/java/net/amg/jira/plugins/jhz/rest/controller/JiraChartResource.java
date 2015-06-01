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

import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.rest.v1.util.CacheControl;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
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
import org.jfree.data.time.RegularTimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

import static net.amg.jira.plugins.jhz.model.FormField.daysBackPattern;
import net.amg.jira.plugins.jhz.model.XYSeriesWithStatusList;

/**
 * @author jarek
 */
@Path("/chart")
public class JiraChartResource {

    private static final Logger logger = LoggerFactory.getLogger(JiraChartResource.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private SearchServiceImpl searchService;
    private Validator validator;
    private JiraChartServiceImpl jiraChartService;
    private TimeZoneManager timeZoneManager;

    /**
     * Allows to generate chart which will be displayed later
     *
     * @param project      value of Project field
     * @param date         value of date field
     * @param periodName   value of period field
     * @param issues       values of Issues field
     * @param width        chart width
     * @param height       chart height
     * @param versionLabel value of version field
     * @return
     */
    @GET
    @Path("/generate")
    @AnonymousAllowed
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
        versionLabel = versionLabel.replace("+", " ");
        paramMap.put(FormField.PROJECT, project);
        paramMap.put(FormField.ISSUES, issues);
        paramMap.put(FormField.DATE, date);
        paramMap.put(FormField.PERIOD, periodName);
        ErrorCollection errorCollection = validator.validate(paramMap);
        Gson gson = new Gson();
        if (!errorCollection.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(errorCollection)).build();
        }
        Date dateBegin = null;
        Date currentDate = new Date();

        final Map<String, Set<String>> statusesSets = searchService.getGroupedIssueTypes(issues);
        try {
            dateBegin = getBeginDate(date);
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(JiraChartResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        //TODO FIXME
        final ChartFactory.VersionLabel label = ChartFactory.VersionLabel.all;
        ProjectOrFilter projectOrFilter = null;
        if(validator.checkIfProject(project)) {
            projectOrFilter = new ProjectOrFilter(ProjectsType.PROJECT,Integer.parseInt(project.split("-")[1]));
        } else {
            projectOrFilter = new ProjectOrFilter(ProjectsType.FILTER,Integer.parseInt(project.split("-")[1]));
        }
        Chart chart = jiraChartService.generateChart(projectOrFilter, ChartFactory.PeriodName.valueOf(periodName.toLowerCase()), label, dateBegin, statusesSets, width, height);

        IssuesHistoryChartModel jiraIssuesHistoryChart = new IssuesHistoryChartModel(chart.getLocation(), "title", chart.getImageMap(), chart.getImageMapName(), width, height);
        if(table) {
            jiraIssuesHistoryChart.setTable(new Table(jiraChartService.getTable()));
        }
        return Response.ok(jiraIssuesHistoryChart).cacheControl(CacheControl.NO_CACHE).build();
    }

    private Date getBeginDate(String date) throws ParseException {
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
    public void setTimeZoneManager(TimeZoneManager timeZoneManager) {
        this.timeZoneManager = timeZoneManager;
    }
}
