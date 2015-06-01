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
import java.util.TreeMap;

import org.apache.commons.lang.mutable.MutableInt;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
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
    private Map<String, Map<RegularTimePeriod, Integer>> table;

    @Override
    public Map<String, Map<RegularTimePeriod, Integer>> getTable() {
        return table;
    }

    @Override
    public Chart generateChart(final String projectName, final Map<String, Set<String>> statusNames,
                               final ChartFactory.PeriodName periodName, final ChartFactory.VersionLabel label, Date dateBegin,
                               final int width, final int height) {
        List<ValueMarker> versionMarkers = getVersionMarkers(projectName, dateBegin, periodName, label);
        final Map<String, Object> params = new HashMap<String, Object>();
        final Class timePeriodClass = ChartUtil.getTimePeriodClass(periodName);
        Map<String, Map<RegularTimePeriod, Integer>> chartMap = generateMapsForChart(projectName, dateBegin, statusNames, timePeriodClass, timeZoneManager.getLoggedInUserTimeZone());
        table = chartMap;
        Map[] dataMaps = chartMap.values().toArray(new Map[0]);
        String[] seriesName = chartMap.keySet().toArray(new String[0]);

        XYDataset issuesHistoryDataset = generateTimeSeries(seriesName, dataMaps);
        ChartHelper helper = new ChartHelper(org.jfree.chart.ChartFactory.createTimeSeriesChart(null, null, null, issuesHistoryDataset, true, false, false));
        XYPlot plot = (XYPlot) helper.getChart().getPlot();

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

    private Map<String, Map<RegularTimePeriod, Integer>> generateMapsForChart(String projectName, Date dateBegin, Map<String, Set<String>> statuses, Class timePeriodClass, TimeZone timeZone) {
        Map<String, TreeMap<RegularTimePeriod, MutableInt>> chartPeriods = new HashMap<>();
        for (String groupName : statuses.keySet()) {
            chartPeriods.put(groupName, new TreeMap<RegularTimePeriod, MutableInt>());
        }
        Date currentDate = new Date();
        List<Issue> allIssues = new ArrayList<>();
        List<ChangeItemBean> changeItems;
        RegularTimePeriod timePeriod;
        try {
            allIssues = searchService.findAllIssues(projectName);
        } catch (SearchException e) {
            logger.error("Unable to get issues" + e.getMessage());
        }
        for (Issue isssue : allIssues) {
            changeItems = changeHistoryManager.getChangeItemsForField(isssue, "status");
            if (changeItems.isEmpty()) {
                for (String groupName : statuses.keySet()) {
                    if (statuses.get(groupName).contains(isssue.getStatusObject().getName())) {
                        if (isssue.getCreated().after(dateBegin)) {
                            timePeriod = RegularTimePeriod.createInstance(timePeriodClass, isssue.getCreated(), timeZone);
                        } else {
                            timePeriod = RegularTimePeriod.createInstance(timePeriodClass, dateBegin, timeZone);
                        }
                        MutableInt num = chartPeriods.get(groupName).get(timePeriod);
                        if (num == null) {
                            num = new MutableInt(0);
                            chartPeriods.get(groupName).put(timePeriod, num);
                        }
                        num.increment();
                    }
                }
            } else {
                for (String groupName : statuses.keySet()) {
                    int index = changeItems.size() - 1;
                    while (index >= 0 && changeItems.get(index).getCreated().after(dateBegin)) {
                        if (statuses.get(groupName).contains(changeItems.get(index).getToString())) {
                            timePeriod = RegularTimePeriod.createInstance(timePeriodClass, changeItems.get(index).getCreated(), timeZone);
                            MutableInt num = chartPeriods.get(groupName).get(timePeriod);
                            if (num == null) {
                                num = new MutableInt(0);
                                chartPeriods.get(groupName).put(timePeriod, num);
                            }
                            num.increment();
                        }
                        index--;
                    }

                    if (index < 0) {
                        if (statuses.get(groupName).contains(changeItems.get(0).getFromString())) {
                            if (isssue.getCreated().after(dateBegin)) {
                                timePeriod = RegularTimePeriod.createInstance(timePeriodClass, isssue.getCreated(), timeZone);
                            } else {
                                timePeriod = RegularTimePeriod.createInstance(timePeriodClass, dateBegin, timeZone);
                            }
                            MutableInt num = chartPeriods.get(groupName).get(timePeriod);
                            if (num == null) {
                                num = new MutableInt(0);
                                chartPeriods.get(groupName).put(timePeriod, num);
                            }
                            num.increment();
                        }
                    } else {
                        if (changeItems.size() < (index + 1) && dateBegin.before(currentDate) && statuses.get(groupName).contains(changeItems.get(index + 1).getFromString())) {
                            timePeriod = RegularTimePeriod.createInstance(timePeriodClass, dateBegin, timeZone);
                            MutableInt num = chartPeriods.get(groupName).get(timePeriod);
                            if (num == null) {
                                num = new MutableInt(0);
                                chartPeriods.get(groupName).put(timePeriod, num);
                            }
                            num.increment();
                        }
                    }

                }
            }
        }
        Map<String, Map<RegularTimePeriod, Integer>> chartMap = new HashMap<>();
        for (String groupName : statuses.keySet()) {
            chartMap.put(groupName, new HashMap<RegularTimePeriod, Integer>());
        }
        Integer temp;
        for (String groupName : statuses.keySet()) {
            temp = 0;
            for (Map.Entry<RegularTimePeriod, MutableInt> entry : chartPeriods.get(groupName).entrySet()) {
                chartMap.get(groupName).put(entry.getKey(), entry.getValue().intValue());
            }
            timePeriod = RegularTimePeriod.createInstance(timePeriodClass, dateBegin, timeZone);

            while (timePeriod.getStart().before(currentDate)) {
                if (chartMap.get(groupName).get(timePeriod) != null) {
                    temp = chartMap.get(groupName).get(timePeriod);
                } else {
                    chartMap.get(groupName).put(timePeriod, temp);
                }
                timePeriod = timePeriod.next();
            }
        }

        return chartMap;
    }

    private List<ValueMarker> getVersionMarkers(String projectName, Date beginDate, ChartFactory.PeriodName periodName, ChartFactory.VersionLabel versionLabel) {
        final Set<Version> versions = new HashSet<Version>();

        Long projectID = projectManager.getProjectObj(Long.parseLong(projectName.replace("project-", ""))).getId();

        versions.addAll(versionManager.getVersionsUnarchived(projectID));

        final Class periodClass = ChartUtil.getTimePeriodClass(periodName);
        final List<ValueMarker> markers = new ArrayList<>();
        for (Version version : versions) {
            if (version.getReleaseDate() != null && beginDate.before(version.getReleaseDate())) {
                RegularTimePeriod timePeriod = RegularTimePeriod.createInstance(periodClass, version.getReleaseDate(), timeZoneManager.getLoggedInUserTimeZone());
                ValueMarker vMarker = new ValueMarker(timePeriod.getFirstMillisecond());

                vMarker.setPaint(Color.GRAY);
                vMarker.setStroke(new BasicStroke(1.2f));
                vMarker.setLabelPaint(Color.GRAY);
                vMarker.setLabel(version.getName());

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

