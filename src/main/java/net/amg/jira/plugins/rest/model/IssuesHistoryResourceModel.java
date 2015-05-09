package net.amg.jira.plugins.rest.model;

import com.atlassian.jira.issue.Issue;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

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
    public IssuesHistoryResourceModel(List<List<Issue>> issues) {
        this.issueGroups = new HashSet<>();
        int groupCounter=0;
        for (List<Issue> issueGroup : issues) {
            this.issueGroups.add(new IssueGroup(issueGroup,groupCounter));
            groupCounter++;
        }
    }

    public Collection<IssueGroup> getIssueGroups() {
        return issueGroups;
    }

    public void setIssueGroups(Collection<IssueGroup> issueGroups) {
        this.issueGroups = issueGroups;
    }
}