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

package net.amg.jira.plugins.jhz.services;

import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.jfreechart.ChartHelper;
import com.atlassian.jira.charts.jfreechart.util.ChartUtil;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.timezone.TimeZoneManager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import net.amg.jira.plugins.jhz.model.XYSeriesWithStatusList;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Component;

/**
 * Class responsible for generating chart using JFreeChart
 *
 * @author jarek
 */
@Component
public class JiraChartServiceImpl implements JiraChartService {

    private final static Logger logger = LoggerFactory.getLogger(JiraChartServiceImpl.class);

    private VersionManager versionManager;
    private SearchService searchService;
    private TimeZoneManager timeZoneManager;
    private ChangeHistoryManager changeHistoryManager;
    private ProjectManager projectManager;

    @Override
    public List<XYSeriesWithStatusList> generateTable(final String projectName, final Map<String,
            Set<String>> statusNames, final ChartFactory.PeriodName periodName, Date dateBegin) {
        return generateMapsForChart(projectName, dateBegin, statusNames, periodName);
    }

    @Override
    public Chart generateChart(
            final String projectName, final ChartFactory.PeriodName periodName,
            final ChartFactory.VersionLabel label, Date dateBegin, Map<String, Set<String>> statusesSets,
            final int width, final int height
    ) {
        List<ValueMarker> versionMarkers = getVersionMarkers(projectName, dateBegin, periodName, label, timeZoneManager.getLoggedInUserTimeZone());

        final Map<String, Object> params = new HashMap<String, Object>();

        List<XYSeriesWithStatusList> listXYSeries = generateMapsForChart(projectName, dateBegin, statusesSets, periodName);

        Map[] dataMaps = new Map[listXYSeries.size()];
        String[] seriesName = new String[listXYSeries.size()];
        for (int i = 0; i < dataMaps.length; i++) {
            dataMaps[i] = listXYSeries.get(i).getXYSeries();
            seriesName[i] = listXYSeries.get(i).getLineName();
        }

        XYDataset issuesHistoryDataset = generateTimeSeries(seriesName, dataMaps);

        ChartHelper helper = new ChartHelper(org.jfree.chart.ChartFactory.createTimeSeriesChart(null, null, null, issuesHistoryDataset, true, false, false));

        XYPlot plot = (XYPlot) helper.getChart().getPlot();
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setLowerBound(0);

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setAutoPopulateSeriesStroke(false);
        renderer.setAutoPopulateSeriesShape(false);
        renderer.setBaseShapesVisible(true);
        renderer.setBaseStroke(new BasicStroke(3));
        renderer.setBaseShape(new Ellipse2D.Double(-2.0, -2.0, 4.0, 4.0));

        NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();
        numberAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.black);

        for (ValueMarker versionMarker : versionMarkers) {
            plot.addDomainMarker(versionMarker);
        }

        try {
            helper.generate(width, height);
        } catch (IOException e) {
            logger.error("Error while generating chart" + e.getMessage());
        }

        return new Chart(helper.getLocation(), helper.getImageMapHtml(), helper.getImageMapName(), params);
    }

    private XYDataset generateTimeSeries(String seriesNames[], Map[] maps) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();

        TimeSeries series = null;
        for (int i = 0; i < maps.length; i++) {
            series = new TimeSeries(seriesNames[i]);
            for (Iterator iterator = maps[i].keySet().iterator(); iterator.hasNext(); ) {
                RegularTimePeriod period = (RegularTimePeriod) iterator.next();

                series.add(period, (Integer) maps[i].get(period));
            }
            dataset.addSeries(series);
        }
        return dataset;
    }

    private List<XYSeriesWithStatusList> generateMapsForChart(String projectName, Date dateBegin, Map<String, Set<String>> statusesSets, ChartFactory.PeriodName periodName) {
        
        Date dateEnd = new Date();
        
        List<XYSeriesWithStatusList> seriesWithStatuses = new ArrayList<>();
        
        for(Map.Entry<String, Set<String>> statusSet : statusesSets.entrySet()) {
            seriesWithStatuses.add(new XYSeriesWithStatusList(statusSet.getValue(), statusSet.getKey(), dateBegin, dateEnd, timeZoneManager.getLoggedInUserTimeZone(), periodName));
        }

        List<Issue> allIssues = new ArrayList<>();
        List<ChangeItemBean> allStatusChangesForIssue;
        Date dateStatusChanged;

        try {
            allIssues = searchService.findAllIssues(projectName);
        } catch (SearchException e) {
            logger.error("Unable to get issues" + e.getMessage());
        }

        for (Issue is : allIssues) {

            allStatusChangesForIssue = changeHistoryManager.getChangeItemsForField(is, "status");
            
            if (allStatusChangesForIssue.isEmpty()) {

                for (XYSeriesWithStatusList series : seriesWithStatuses) {

                    if (series.containsStatus(is.getStatusObject().getName())) {

                        if (is.getCreated().after(dateBegin)) {
                            series.addYPointsInRange(is.getCreated(), dateEnd);
                        } else {
                            series.addYPointsInRange(dateBegin, dateEnd);
                        }
                    }

                }
            } else {
                for (XYSeriesWithStatusList series : seriesWithStatuses) {
                    dateStatusChanged = dateEnd;
                    
                    for (int i = allStatusChangesForIssue.size() - 1; i >= 0 && dateStatusChanged.after(dateBegin); i--) {
                        if (series.containsStatus(allStatusChangesForIssue.get(i).getToString()) &&
                                !series.checkIfChangeInTheSameTimePeriod(allStatusChangesForIssue.get(i).getCreated(), dateStatusChanged)) {
                            
                            System.out.println( allStatusChangesForIssue.get(i).getCreated() +  "   " + dateStatusChanged);

                            series.addYPointsInRange(allStatusChangesForIssue.get(i).getCreated(), dateStatusChanged);

                        }
                        dateStatusChanged = allStatusChangesForIssue.get(i).getCreated();
                        
                        if (i == 0 && dateStatusChanged.after(dateBegin) && series.containsStatus(allStatusChangesForIssue.get(i).getFromString())) {
                            series.addYPointsInRange(is.getCreated(), dateStatusChanged);
                        }
                    }
                }
            }

        }

        return seriesWithStatuses;
    }

    private List<ValueMarker> getVersionMarkers(String projectName, Date beginDate, ChartFactory.PeriodName periodName, ChartFactory.VersionLabel versionLabel, TimeZone timeZone) {
        final Set<Version> versions = new HashSet<Version>();

        Long projectID = projectManager.getProjectObj(Long.parseLong(projectName.replace("project-", ""))).getId();

        versions.addAll(versionManager.getVersionsUnarchived(projectID));

        final Class periodClass = ChartUtil.getTimePeriodClass(periodName);
        final List<ValueMarker> markers = new ArrayList<>();
        for (Version version : versions) {
            if (version.getReleaseDate() != null && beginDate.before(version.getReleaseDate())) {
                RegularTimePeriod timePeriod = RegularTimePeriod.createInstance(periodClass, version.getReleaseDate(), timeZone);
                ValueMarker vMarker = new ValueMarker(timePeriod.getFirstMillisecond());

                vMarker.setPaint(Color.GRAY);
                vMarker.setStroke(new BasicStroke(1.2f));
                vMarker.setLabelPaint(Color.GRAY);
                vMarker.setLabel(version.getName());
                vMarker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
                vMarker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
                markers.add(vMarker);
            }
        }
        return markers;
    }

    @ServiceReference
    public void setVersionManager(VersionManager versionManager) {
        this.versionManager = versionManager;
    }

    @ServiceReference
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @ServiceReference
    public void setTimeZoneManager(TimeZoneManager timeZoneManager) {
        this.timeZoneManager = timeZoneManager;
    }

    @ServiceReference
    public void setChangeHistoryManager(ChangeHistoryManager changeHistoryManager) {
        this.changeHistoryManager = changeHistoryManager;
    }

    @ServiceReference
    public void setProjectManager(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }
}

