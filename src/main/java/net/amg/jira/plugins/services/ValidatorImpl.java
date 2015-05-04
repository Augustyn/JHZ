package net.amg.jira.plugins.services;

import net.amg.jira.plugins.model.FormField;
import net.amg.jira.plugins.rest.model.ErrorCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Simple implementation of Validator.
 */
public class ValidatorImpl implements Validator {

    private static final Logger log = LoggerFactory.getLogger(ValidatorImpl.class);

    public ValidatorImpl() {
    }

    @Override
    public ErrorCollection validate(Map<FormField, String> paramMap) {
        ErrorCollection errorCollection = new ErrorCollection();
        for (Map.Entry<FormField, String> entry : paramMap.entrySet()) {
            entry.getKey().validate(errorCollection, entry.getValue());
        }
        return errorCollection;
    }

    @Override
    public boolean checkIfDate(String value) {
        return FormField.datePattern.matcher(value).matches();
    }

    @Override
    public boolean checkIfProject(String value) {
        return value.startsWith("project-");
    }
}