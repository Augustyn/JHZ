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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a row corresponding to a single period in an issues history table
 * Created by Ivo on 31/05/15.
 */
@XmlRootElement
public class TableEntry {

    @XmlElement
    private Date period;

    @XmlElement
    private List<IssueCount> issueCount;

    public TableEntry(Date period) {
        this.period = period;
        this.issueCount = new ArrayList<>();
    }

    public Date getPeriod() {
        return period;
    }

    public void setPeriod(Date period) {
        this.period = period;
    }

    public List<IssueCount> getIssueCount() {
        return issueCount;
    }

    public void setIssueCount(List<IssueCount> issueCount) {
        this.issueCount = issueCount;
    }
}
