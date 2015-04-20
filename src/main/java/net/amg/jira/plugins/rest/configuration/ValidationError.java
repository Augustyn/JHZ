package net.amg.jira.plugins.rest.configuration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents an error in configuration of specific field
 */
@XmlRootElement
public class ValidationError {

    @XmlElement
    private String field;

    @XmlElement
    private String error;

    @XmlElement
    private Collection<String> params = new ArrayList<>();

    /**
     * @param field  UserPref name to which the error refers to
     * @param error  i18n key of the error message to be displayed
     * @param params optional parameters
     */
    public ValidationError(String field, String error, Collection<String> params) {
        this.field = field;
        this.error = error;
        this.params = params;
    }

    /**
     * @param field UserPref name to which the error refers to
     * @param error i18n key of the error message to be displayed
     */
    public ValidationError(String field, String error) {
        this.field = field;
        this.error = error;
    }
}
