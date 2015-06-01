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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import org.apache.commons.lang.mutable.MutableInt;
import org.jfree.data.time.RegularTimePeriod;

/**
 * Class with list of statuses and XY series for chart
 */
public class XYSeriesWithStatusList {

    private final Set<String> statusesSet;
    private Map<RegularTimePeriod, MutableInt> XYSeries;
    private final String lineName;
    private final TimeZone timeZone;
    private final ChartFactory.PeriodName periodName;
    private final Date dateBegin;
    private final Date dateEnd;

    public XYSeriesWithStatusList(Set<String> statusesSet, String lineName, Date dateBegin, Date dateEnd, TimeZone timeZone, ChartFactory.PeriodName periodName) {
        this.statusesSet = statusesSet;
        this.lineName = lineName;
        this.XYSeries = new TreeMap<>();
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
    }

    /**
     * Set of statuses that are used to create this xy series
     * @return set of statuses
     */
    public Set<String> getStatusesSet() {
        return statusesSet;
    }

    /**
     * 
     * @return map with x and y used to generate chart
     */
    public Map<RegularTimePeriod, Integer> getXYSeries() {
        Map<RegularTimePeriod, Integer> map = new HashMap<>();
        
        for (Map.Entry<RegularTimePeriod, MutableInt> entry : XYSeries.entrySet()) {
                map.put(entry.getKey(), entry.getValue().intValue());
            }
        
        return map;
    }

    /**
     * 
     * @return name of this xy series line
     */
    public String getLineName() {
        return lineName;
    }

    /**
     * checks if string with statusName is contained in statuses of this xy series
     * @param statusName name of status to check
     * @return true if statusName is in set of statuses
     */
    public boolean containsStatus(String statusName) {
        return this.statusesSet.contains(statusName);
    }

    /**
     * adds points for each time period between two dates
     * @param dateBegin date for first time period
     * @param dateEnd end date for periods
     */
    public void addYPointsInRange(Date dateBegin, Date dateEnd) {
        if(dateBegin.before(this.dateBegin)) {
            dateBegin = this.dateBegin;
        }
        RegularTimePeriod timePeriod = RegularTimePeriod.createInstance(ChartUtil.getTimePeriodClass(periodName), dateBegin, timeZone);
        MutableInt num;
        while (timePeriod.getEnd().before(dateEnd)) {
            num = XYSeries.get(timePeriod);
            if (num == null) {
                num = new MutableInt(0);
                XYSeries.put(timePeriod, num);
            }
            num.increment();
            timePeriod = timePeriod.next();
        }
        if(dateEnd.equals(this.dateEnd)) {
            num = XYSeries.get(timePeriod);
            num.increment();
        }
    }
    
    /**
     * Checks if two dates are in the same time period
     * @param date1 first Date to check
     * @param date2 second Date
     * @return true if dates are in the same period
     */
    public boolean checkIfChangeInTheSameTimePeriod(Date date1, Date date2) {
        RegularTimePeriod timePeriod1 = RegularTimePeriod.createInstance(ChartUtil.getTimePeriodClass(periodName), date1, timeZone);
        RegularTimePeriod timePeriod2 = RegularTimePeriod.createInstance(ChartUtil.getTimePeriodClass(periodName), date2, timeZone);
        
        return timePeriod1.getStart().equals(timePeriod2.getStart());
    }

}
