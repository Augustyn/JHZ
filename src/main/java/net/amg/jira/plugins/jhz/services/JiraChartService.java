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
import com.atlassian.jira.issue.search.SearchException;
import net.amg.jira.plugins.jhz.model.ProjectOrFilter;
import net.amg.jira.plugins.jhz.model.XYSeriesWithStatusList;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Responsible for generating chart using JFreeChart
 * Created by Ivo on 31/05/15.
 */
public interface JiraChartService {

    /**
     * Generates chart for Jira using JFreeChart
     *
     * @param projectOrFilter project id of filter or project
     * @param periodName      time period on the chart
     * @param label           labels shown on the chart
     * @param statusesSets    sets of statuses chosen for all chart series
     * @param dateBegin       beginning date from which chart will be drawn
     * @param width           chart width
     * @param height          chart height
     * @return chart data
     */
    public Chart generateChart(final ProjectOrFilter projectOrFilter, final ChartFactory.PeriodName periodName,
                               final ChartFactory.VersionLabel label, Date dateBegin, Map<String, Set<String>> statusesSets,
                               final int width, final int height) throws IOException, SearchException;

    /**
     * Provides issues history data created during chart generation. Will not work if no chart was generated.
     *
     * @return issues history data.
     */
    public List<XYSeriesWithStatusList> getTable();

}
