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
package net.amg.jira.plugins.jhz.model;

import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.jfreechart.util.ChartUtil;
import org.apache.commons.lang.mutable.MutableInt;
import org.jfree.data.time.RegularTimePeriod;

import java.util.*;

/**
 * Class with list of statuses and map used to create XY series for chart
 */
public class XYSeriesWithStatusList {

    private final int STATUSES_IN_LEGEND = 3;
    private final Set<String> statusesSet;
    private Map<RegularTimePeriod, MutableInt> XYSeries;
    private String lineName;
    private final TimeZone timeZone;
    private final ChartFactory.PeriodName periodName;
    private final Date dateBegin;
    private final Date dateEnd;
    private StringBuilder sb;

    /**
     * Creates XY series for chosen statuses, that is later used to generate chart
     * 
     * @param statusesSet set of statuses chosen to be shown
     * @param dateBegin beginning date of requested history
     * @param dateEnd ending date of requested history
     * @param timeZone current user time zone
     * @param periodName name of period used on chart
     */
    public XYSeriesWithStatusList(Set<String> statusesSet, Date dateBegin, Date dateEnd, TimeZone timeZone, ChartFactory.PeriodName periodName) {
        this.statusesSet = statusesSet;
        this.XYSeries = new TreeMap<>();
        sb = new StringBuilder();
        this.periodName = periodName;
        this.timeZone = timeZone;
        this.dateBegin = dateBegin;
        this.dateEnd = dateEnd;
        initSeries(dateBegin, dateEnd);
    }

    private void initSeries(Date dateBegin, Date dateEnd) {
        RegularTimePeriod timePeriod = RegularTimePeriod.createInstance(ChartUtil.getTimePeriodClass(periodName), dateBegin, timeZone);
        while (timePeriod.getStart().before(dateEnd)) {
            XYSeries.put(timePeriod, new MutableInt(0));
            timePeriod = timePeriod.next();
        }
        Iterator<String> iterator = statusesSet.iterator();
        for (int i = 0; i < STATUSES_IN_LEGEND && iterator.hasNext(); i++) {
            sb.append(iterator.next());
            if(iterator.hasNext()) sb.append(", ");
        }
        if(iterator.hasNext()) sb.append("...");
        this.lineName = sb.toString();
    }

    /**
     * Returns set of statuses that are used to create this XY series
     *
     * @return set of statuses
     */
    public Set<String> getStatusesSet() {
        return statusesSet;
    }

    /**
     * Returns map with x and y series used to generate chart
     * 
     * @return x and y series map
     */
    public Map<RegularTimePeriod, Integer> getXYSeries() {
        Map<RegularTimePeriod, Integer> map = new HashMap<>();

        for (Map.Entry<RegularTimePeriod, MutableInt> entry : XYSeries.entrySet()) {
            map.put(entry.getKey(), entry.getValue().intValue());
        }

        return map;
    }

    /**
     * Returns name of chart line created for chosen statuses
     * 
     * @return name of xy series
     */
    public String getLineName() {
        return lineName;
    }

    /**
     * Checks if string with statusName is contained in status list of this xy series
     *
     * @param statusName name of status to check
     * @return true if statusName is in set of statuses
     */
    public boolean containsStatus(String statusName) {
        return this.statusesSet.contains(statusName);
    }

    /**
     * Adds points for each time period between two dates
     *
     * @param dateBegin date for first time period
     * @param dateEnd   end date for periods
     */
    public void addYPointsInRange(Date dateBegin, Date dateEnd) {
        if (dateBegin.before(this.dateBegin)) {
            dateBegin = this.dateBegin;
        }
        RegularTimePeriod timePeriod = RegularTimePeriod.createInstance(ChartUtil.getTimePeriodClass(periodName), dateBegin, timeZone);
        MutableInt num;
        while (timePeriod.getEnd().before(dateEnd)) {
            num = XYSeries.get(timePeriod);
            if (num != null) {
                num.increment();
            }
            timePeriod = timePeriod.next();
        }
        if (dateEnd.equals(this.dateEnd)) {
            num = XYSeries.get(timePeriod);
            if (num != null) {
                num.increment();
            }
        }
    }

    /**
     * Checks if two dates are in the same time period
     *
     * @param date1 first Date to check
     * @param date2 second Date
     * @return true if dates are in the same period
     */
    public boolean checkIfChangeInTheSameTimePeriod(Date date1, Date date2) {
        RegularTimePeriod timePeriod1 = RegularTimePeriod.createInstance(ChartUtil.getTimePeriodClass(periodName), date1, timeZone);
        RegularTimePeriod timePeriod2 = RegularTimePeriod.createInstance(ChartUtil.getTimePeriodClass(periodName), date2, timeZone);

        return timePeriod1.getStart().equals(timePeriod2.getStart()) && !date2.equals(dateEnd);
    }
}
