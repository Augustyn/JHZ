package net.amg.jira.plugins.components;

import net.amg.jira.plugins.rest.configuration.ErrorCollection;
import net.amg.jira.plugins.rest.configuration.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
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
            entry.getKey().validate(errorCollection,entry.getValue());
        }
        return errorCollection;
    }
}