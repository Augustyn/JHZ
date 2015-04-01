package net.amg.jira.plugins.rest;

import javax.xml.bind.annotation.*;
@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public class IssuesHistoryResourceModel {

    @XmlElement(name = "value")
    private String message;

    public IssuesHistoryResourceModel() {
    }

    public IssuesHistoryResourceModel(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}