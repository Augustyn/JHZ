package net.amg.jira.plugins.rest;

import com.atlassian.jira.issue.Issue;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a single Issue.
 */
@Immutable
@XmlRootElement
public class IssueRepresentation {
    @XmlElement
    private Long id;

    @XmlElement
    private String key;

    @XmlElement
    private String statusName;

    private IssueRepresentation() {
    }

    /**
     * Extracts information required by the gadget from an Issue object.
     * @param issue
     */
    public IssueRepresentation(Issue issue) {
        this.id = issue.getId();
        this.key = issue.getKey();
        this.statusName = issue.getStatusObject().getSimpleStatus().getName();
    }
}
