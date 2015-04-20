package net.amg.jira.plugins.components;

import net.amg.jira.plugins.rest.configuration.ErrorCollection;
import net.amg.jira.plugins.rest.configuration.ValidationError;

/**
 * A set of field categories constituting gadget configuration.
 * Created by Ivo on 19/04/15.
 */
public enum FormField {

    //TODO more detailed validation

    PROJECT("Project") {
        @Override
        public void validate(ErrorCollection errorCollection, String value) {
            if (value.isEmpty()) {
                errorCollection.addValidationError(new ValidationError(FormField.PROJECT.fieldName,
                        ERROR_PREFIX + "emptyField"));
            }
        }
    }, ISSUES("Issues") {
        public void validate(ErrorCollection errorCollection, String value) {
            if (value.isEmpty()) {
                errorCollection.addValidationError(new ValidationError(FormField.ISSUES.fieldName,
                        ERROR_PREFIX + "emptyField"));
            }
        }
    }, PREVIOUSLY("Previously") {
        public void validate(ErrorCollection errorCollection, String value) {
            if (!value.matches("\\d+") && !value.isEmpty()) {
                errorCollection.addValidationError(new ValidationError(FormField.PREVIOUSLY.fieldName,
                        ERROR_PREFIX + "mustBeNumeric"));
            }
        }
    }, PERIOD("Period") {
        public void validate(ErrorCollection errorCollection, String value) {
            if (value.isEmpty()) {
                errorCollection.addValidationError(new ValidationError(FormField.PERIOD.fieldName,
                        ERROR_PREFIX + "emptyField"));
            }
        }
    };

    private static final String ERROR_PREFIX = "issues.history.gadget.errors.";

    private final String fieldName;

    private FormField(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Checks field value for correctness.
     * @param errorCollection container for detected errors
     * @param value checked
     */
    public abstract void validate(ErrorCollection errorCollection, String value);
}
