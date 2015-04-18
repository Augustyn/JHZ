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

    private final Map<String, FieldValidator> fieldValidatorMap = new HashMap<>();

    public ValidatorImpl() {
        fieldValidatorMap.put("Project", projectValidator);
        fieldValidatorMap.put("Issues", issuesValidator);
        fieldValidatorMap.put("Previously", previouslyValidator);
        fieldValidatorMap.put("Period", periodValidator);
    }

    @Override
    public ErrorCollection validate(Map<String, String> paramMap) {
        ErrorCollection errorCollection = new ErrorCollection();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            fieldValidatorMap.get(entry.getKey()).validate(entry.getValue(), errorCollection);
        }
        return errorCollection;
    }

    //TODO detailed validation

    private FieldValidator projectValidator = new FieldValidator() {
        @Override
        public void validate(String value, ErrorCollection errorCollection) {
            if (value.isEmpty()) {
                errorCollection.addValidationError(new ValidationError("Project", "issues.history.gadget.errors.emptyField"));
            }
        }
    };
    private FieldValidator issuesValidator = new FieldValidator() {
        @Override
        public void validate(String value, ErrorCollection errorCollection) {
            if (value.isEmpty()) {
                errorCollection.addValidationError(new ValidationError("Issues", "issues.history.gadget.errors.emptyField"));
            }
        }
    };
    private FieldValidator periodValidator = new FieldValidator() {
        @Override
        public void validate(String value, ErrorCollection errorCollection) {
            if (value.isEmpty()) {
                errorCollection.addValidationError(new ValidationError("Period", "issues.history.gadget.errors.emptyField"));
            }
        }
    };
    private FieldValidator previouslyValidator = new FieldValidator() {
        @Override
        public void validate(String value, ErrorCollection errorCollection) {
            if (!value.matches("\\d+") && !value.isEmpty()) {
                errorCollection.addValidationError(new ValidationError("Previously", "issues.history.gadget.errors.mustBeNumeric"));
            }
        }
    };
}