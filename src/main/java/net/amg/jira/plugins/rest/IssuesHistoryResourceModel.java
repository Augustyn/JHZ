package net.amg.jira.plugins.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Represents a collection of issues created in accordance to user preferences.
 */
@XmlRootElement(name = "issues")
@XmlAccessorType(XmlAccessType.FIELD)
public class IssuesHistoryResourceModel {

    @XmlElement
    private Collection<IssueRepresentation> issues;

    /**
     * Encapsulates IssueRepresentation objects.
     * @param issues requested by gadget
     */
    public IssuesHistoryResourceModel(Iterable<IssueRepresentation> issues) {
        this.issues = new HashSet<IssueRepresentation>();
        for (IssueRepresentation representation : issues) {
            this.issues.add(representation);
        }
    }

    public Collection<IssueRepresentation> getIssues() {
        return issues;
    }

    public void setIssues(Collection<IssueRepresentation> issues) {
        this.issues = issues;
    }
}