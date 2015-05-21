package net.amg.jira.plugins.jhz.rest.model;

import com.atlassian.jira.issue.status.Status;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashSet;

/**
 * Represents a collection of all available JIRA statuses for the currently logged-in user.
 * Created by Ivo on 01/05/15.
 */
@XmlRootElement(name = "statuses")
@XmlAccessorType(XmlAccessType.FIELD)
public class StatusesResourceModel {

    @XmlElement
    private Collection<StatusRepresentation> statuses;

    /**
     * Encapsulates IssueTypeRepresentation objects.
     *
     * @param statusTypes requested by gadget
     */
    public StatusesResourceModel(Iterable<Status> statusTypes) {
        this.statuses = new HashSet<StatusRepresentation>();
        for (Status status : statusTypes) {
            this.statuses.add(new StatusRepresentation(status));
        }
    }

}
