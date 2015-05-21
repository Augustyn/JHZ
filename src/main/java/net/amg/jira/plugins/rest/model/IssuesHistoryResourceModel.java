package net.amg.jira.plugins.rest.model;

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