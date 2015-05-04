package net.amg.jira.plugins.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a collection of errors found in field configuration of the gadget.
 */
@XmlRootElement
public class ErrorCollection {

    /**
     * Generic error messages
     */
    @XmlElement
    private Collection<String> errorMessages = new ArrayList<String>();

    /**
     * Errors specific to a certain field.
     */
    @XmlElement
    private Collection<ValidationError> errors = new ArrayList<ValidationError>();

    public void addErrorMessage(String message) {
        errorMessages.add(message);
    }

    public void addValidationError(ValidationError validationError) {
        errors.add(validationError);
    }

    public boolean isEmpty() {
        return errorMessages.isEmpty() && errors.isEmpty();
    }
}

