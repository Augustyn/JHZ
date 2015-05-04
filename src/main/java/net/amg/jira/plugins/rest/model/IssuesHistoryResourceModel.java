package net.amg.jira.plugins.rest.model;

import com.atlassian.jira.issue.Issue;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashSet;

/**
 * Represents a collection of issues requested by the gadget.
 */
@XmlRootElement(name = "issues")
@XmlAccessorType(XmlAccessType.FIELD)
public class IssuesHistoryResourceModel {

    @XmlElement
    private Collection<IssueRepresentation> issues;

    /**
     * Encapsulates IssueRepresentation objects.
     *
     * @param issues requested by gadget
     */
    public IssuesHistoryResourceModel(Iterable<Issue> issues) {
        this.issues = new HashSet<IssueRepresentation>();
        for (Issue issue : issues) {
            this.issues.add(new IssueRepresentation(issue));
        }
    }

    public Collection<IssueRepresentation> getIssues() {
        return issues;
    }

    public void setIssues(Collection<IssueRepresentation> issues) {
        this.issues = issues;
    }
}