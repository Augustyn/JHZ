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

import com.google.gson.Gson;
import net.amg.jira.plugins.jhz.services.Validator;
import net.amg.jira.plugins.jhz.model.FormField;
import net.amg.jira.plugins.jhz.rest.model.ErrorCollection;
import net.amg.jira.plugins.jhz.services.JiraChartService;
import net.amg.jira.plugins.jhz.rest.model.IssuesHistoryChartModel;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.rest.v1.util.CacheControl;
import com.atlassian.jira.timezone.TimeZoneManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static net.amg.jira.plugins.jhz.model.FormField.daysBackPattern;

import net.amg.jira.plugins.jhz.services.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;

/**
 * @author jarek
 */
@Path("/chart")
public class JiraChartResource {

    private static final Logger logger = LoggerFactory.getLogger(JiraChartResource.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final int ISSUES_GROUPS = 5;
    private SearchService searchService;
    private Validator validator;
    private JiraChartService jiraChartService;

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
    public Response generateChart(
            @QueryParam("project") String project,
            @QueryParam("date") String date,
            @QueryParam("period") String periodName,
            @QueryParam("issues") String issues,
            @QueryParam("width") int width,
            @QueryParam("height") int height,
            @QueryParam("version") String versionLabel) {
        Map<FormField, String> paramMap = new HashMap<>();
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

        final Map<String, Set<String>> statusesSets = searchService.getGroupedIssueTypes(issues);
        try {
            dateBegin = getBeginDate(date);
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(JiraChartResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        final ChartFactory.PeriodName period = ChartFactory.PeriodName.valueOf(periodName.toLowerCase());
        final ChartFactory.VersionLabel label = getVersionLabel(versionLabel);
        Chart chart = jiraChartService.generateChart(project, /*TODO refactor chart genertion so it uses the map*/new ArrayList<>(statusesSets.values()), period, label, dateBegin, width, height);

        IssuesHistoryChartModel jiraIssuesHistoryChart = new IssuesHistoryChartModel(chart.getLocation(), "title", chart.getImageMap(), chart.getImageMapName(), width, height);

        return Response.ok(jiraIssuesHistoryChart).cacheControl(CacheControl.NO_CACHE).build();
    }


    private ChartFactory.VersionLabel getVersionLabel(String versionLabel) {
        //TODO te stringi nie mogą być na sztywno
        if (versionLabel.contains("major") || versionLabel.contains("znaczące")) return ChartFactory.VersionLabel.major;
        else if (versionLabel.contains("all") || versionLabel.contains("Wszystkie"))
            return ChartFactory.VersionLabel.all;
        else return ChartFactory.VersionLabel.none;
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
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @ServiceReference
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @ServiceReference
    public void setJiraChartService(JiraChartService jiraChartService) {
        this.jiraChartService = jiraChartService;
    }
}
