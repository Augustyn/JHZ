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
    }, DATE("Date") {
        public void validate(ErrorCollection errorCollection, String value) {

            if(value.isEmpty()){
                errorCollection.addValidationError(new ValidationError(FormField.DATE.fieldName,
                        ERROR_PREFIX + "emptyField"));
            }else {
                if (!value.matches("-?\\d{1,4}d") &&
                        !value.matches("^[1-2]\\d{3}[/\\-[.]](0[1-9]|1[012])[/\\-[.]](0[1-9]|[12][0-9]|3[01])$") ) {

                    errorCollection.addValidationError(new ValidationError(FormField.DATE.fieldName,
                            ERROR_PREFIX + "wrongFormat"));
                }
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
