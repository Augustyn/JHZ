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

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Represents group of issue representations for a single gadet graph.
 * Created by Ivo on 09/05/15.
 */
@Immutable
@XmlRootElement
public class IssueGroup {

    @XmlElement
    private String name;

    @XmlElement
    private Collection<IssueRepresentation> issues;

    public IssueGroup(List<Issue> issueGroup, String name) {
        this.name = name;
        issues = new HashSet<>();
        for (Issue issue : issueGroup) {
            issues.add(new IssueRepresentation(issue));
        }
    }
}
