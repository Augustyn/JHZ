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

import com.atlassian.jira.issue.Issue;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Represents a collection of issues requested by the gadget.
 */
@XmlRootElement(name = "issues")
@XmlAccessorType(XmlAccessType.FIELD)
public class IssuesHistoryResourceModel {

    @XmlElement
    private Collection<IssueGroup> issueGroups;

    /**
     * Encapsulates IssueRepresentation objects.
     *
     * @param issues requested by gadget
     */
    public IssuesHistoryResourceModel(Map<String, List<Issue>> issues) {
        this.issueGroups = new HashSet<>();
        for (String groupName : issues.keySet()) {
            this.issueGroups.add(new IssueGroup(issues.get(groupName), groupName));
        }
    }

    public Collection<IssueGroup> getIssueGroups() {
        return issueGroups;
    }

    public void setIssueGroups(Collection<IssueGroup> issueGroups) {
        this.issueGroups = issueGroups;
    }
}