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
