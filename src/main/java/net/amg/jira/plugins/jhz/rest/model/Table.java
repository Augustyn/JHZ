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

package net.amg.jira.plugins.jhz.rest.model;

import org.jfree.data.time.RegularTimePeriod;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

/**
 * Created by Ivo on 31/05/15.
 */
@XmlRootElement
public class Table {

    @XmlElement
    List<TableEntry> entries;

    @XmlElement
    Set<String> groupNames;

    public Table(Map<String, Map<RegularTimePeriod, Integer>> history) {
        groupNames = history.keySet();
        entries = new ArrayList<>();
        Iterator<Map<RegularTimePeriod, Integer>> iterator = history.values().iterator();
        Map<RegularTimePeriod, Integer> periods = iterator.next();
        for (RegularTimePeriod period : periods.keySet()) {
            TableEntry entry = new TableEntry(new Date(period.getLastMillisecond()));
            for (String groupName : history.keySet()) {
                entry.getIssueCount().add(new IssueCount(history.get(groupName).get(period)));
            }
            entries.add(entry);
        }
        Collections.sort(entries, new Comparator<TableEntry>() {
            @Override
            public int compare(TableEntry o1, TableEntry o2) {
                return o2.getPeriod().compareTo(o1.getPeriod());
            }
        });
    }

    public List<TableEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<TableEntry> entries) {
        this.entries = entries;
    }

    public Set<String> getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(Set<String> groupNames) {
        this.groupNames = groupNames;
    }
}
